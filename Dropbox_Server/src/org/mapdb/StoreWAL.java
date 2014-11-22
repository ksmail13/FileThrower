/*
 *  Copyright (c) 2012 Jan Kotek
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.mapdb;

import java.io.DataInput;
import java.io.IOError;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.zip.CRC32;

/**
 * Write-Ahead-Log
 */
public class StoreWAL extends StoreDirect {

    protected static final long LOG_MASK_OFFSET = 0x0000FFFFFFFFFFFFL;

    protected static final byte WAL_INDEX_LONG = 101;
    protected static final byte WAL_LONGSTACK_PAGE = 102;
    protected static final byte WAL_PHYS_ARRAY_ONE_LONG = 103;

    protected static final byte WAL_PHYS_ARRAY = 104;
    protected static final byte WAL_SKIP_REST_OF_BLOCK = 105;


    /** last instruction in log file */
    protected static final byte WAL_SEAL = 111;
    /** added to offset 8 into log file, indicates that log was synced and closed*/
    protected static final long LOG_SEAL = 4566556446554645L;

    public static final String TRANS_LOG_FILE_EXT = ".t";

    protected static final long[] TOMBSTONE = new long[0];
    protected static final long[] PREALLOC = new long[0];

    protected Volume log;

    protected volatile long logSize;

    protected final LongConcurrentHashMap<long[]> modified = new LongConcurrentHashMap<long[]>();
    protected final LongMap<byte[]> longStackPages = new LongHashMap<byte[]>();
    protected final long[] indexVals = new long[IO_USER_START/8];
    protected final boolean[] indexValsModified = new boolean[indexVals.length];

    protected boolean replayPending = true;


    protected final AtomicInteger logChecksum = new AtomicInteger();

    public StoreWAL(
            String fileName,
            Fun.Function1<Volume,String> volFac,
            Fun.Function1<Volume,String> indexVolFac,
            boolean readOnly,
            boolean deleteFilesAfterClose,
            int spaceReclaimMode,
            boolean syncOnCommitDisabled,
            boolean checksum,
            boolean compress,
            byte[] password,
            int sizeIncrement) {
        super(fileName, volFac, indexVolFac,
                readOnly, deleteFilesAfterClose,
                spaceReclaimMode, syncOnCommitDisabled,
                checksum, compress, password,
                sizeIncrement);

        this.log = volFac.run(fileName+TRANS_LOG_FILE_EXT);

        boolean allGood = false;
        structuralLock.lock();

        try{
            reloadIndexFile();
            if(verifyLogFile()){
                replayLogFile();
            }
            replayPending = false;
            checkHeaders();
            if(!readOnly)
                logReset();
            allGood = true;
        }finally{
            if(!allGood) {
                //exception was thrown, try to unlock files
                if (log!=null) {
                    log.close();
                    log = null;
                }
                if (index!=null) {
                    index.close();
                    index = null;
                }
                if (phys!=null) {
                    phys.close();
                    phys = null;
                }
            }
            structuralLock.unlock();
        }
    }


    public StoreWAL(String fileName) {
        this(   fileName,
                fileName==null || fileName.isEmpty()?Volume.memoryFactory():Volume.fileFactory(),
                fileName==null || fileName.isEmpty()?Volume.memoryFactory():Volume.fileFactory(),
                false,
                false,
                CC.DEFAULT_FREE_SPACE_RECLAIM_Q,
                false,
                false,
                false,
                null,
                0
        );
    }

    @Override
    protected void checkHeaders() {
        if(replayPending) return;
        super.checkHeaders();
    }

    protected void reloadIndexFile() {
        if(CC.PARANOID && ! ( structuralLock.isHeldByCurrentThread()))
            throw new AssertionError();
        logSize = 16;
        modified.clear();
        longStackPages.clear();
        indexSize = index.getLong(IO_INDEX_SIZE);
        physSize = index.getLong(IO_PHYS_SIZE);
        freeSize = index.getLong(IO_FREE_SIZE);
        for(int i = 0;i<IO_USER_START;i+=8){
            indexVals[i/8] = index.getLong(i);
        }
        Arrays.fill(indexValsModified, false);

        logChecksum.set(0);

        maxUsedIoList=IO_USER_START-8;
        while(indexVals[((int) (maxUsedIoList / 8))]!=0 && maxUsedIoList>IO_FREE_RECID)
            maxUsedIoList-=8;
    }

    protected  void logReset() {
        if(CC.PARANOID && ! ( structuralLock.isHeldByCurrentThread()))
            throw new AssertionError();
        log.truncate(16);
        log.ensureAvailable(16);
        log.putInt(0, HEADER);
        log.putUnsignedShort(4, STORE_VERSION);
        log.putUnsignedShort(6, expectedMasks());
        log.putLong(8, 0L);
        logSize = 16;
    }


    @Override
    public  long preallocate() {
        final long ioRecid;
        final long logPos;

        newRecidLock.readLock().lock();

        try{
            structuralLock.lock();

            try{
                ioRecid = freeIoRecidTake(false);
                logPos = logSize;
                //now get space in log
                logSize+=1+8+8; //space used for index val
                log.ensureAvailable(logSize);

            }finally{
                structuralLock.unlock();
            }
            final Lock lock = locks[Store.lockPos(ioRecid)].writeLock();
            lock.lock();

            try{

                //write data into log
                walIndexVal(logPos, ioRecid, MASK_DISCARD);
                modified.put(ioRecid, PREALLOC);
            }finally{
                lock.unlock();
            }
        }finally{
            newRecidLock.readLock().unlock();
        }

        long recid =  (ioRecid-IO_USER_START)/8;
        if(CC.PARANOID && ! (recid>0))
            throw new AssertionError();
        return recid;
    }



    @Override
    public <A> long put(A value, Serializer<A> serializer) {
        if(serializer == null)
            throw new NullPointerException();
        if(CC.PARANOID && ! (value!=null))
            throw new AssertionError();
        DataIO.DataOutputByteArray out = serialize(value, serializer);

        final long ioRecid;
        final long[] physPos;
        final long[] logPos;

        newRecidLock.readLock().lock();

        try{
            structuralLock.lock();

            try{
                ioRecid = freeIoRecidTake(false);
                //first get space in phys
                physPos = physAllocate(out.pos,false,false);
                //now get space in log
                logPos = logAllocate(physPos);

            }finally{
                structuralLock.unlock();
            }

            final Lock lock = locks[Store.lockPos(ioRecid)].writeLock();
            lock.lock();

            try{
                //write data into log
                walIndexVal((logPos[0]&LOG_MASK_OFFSET) - 1-8-8-1-8, ioRecid, physPos[0]|MASK_ARCHIVE);
                walPhysArray(out, physPos, logPos);

                modified.put(ioRecid,logPos);
                recycledDataOuts.offer(out);
            }finally{
                lock.unlock();
            }
        }finally{
            newRecidLock.readLock().unlock();
        }

        long recid =  (ioRecid-IO_USER_START)/8;
        if(CC.PARANOID && ! (recid>0))
            throw new AssertionError();
        return recid;
    }

    protected void walPhysArray(DataIO.DataOutputByteArray out, long[] physPos, long[] logPos) {
        //write byte[] data
        int outPos = 0;
        int logC = 0;
        CRC32 crc32  = new CRC32();

        for(int i=0;i<logPos.length;i++){
            int c =  i==logPos.length-1 ? 0: 8;
            final long pos = logPos[i]&LOG_MASK_OFFSET;
            int size = (int) (logPos[i]>>>48);

            byte header = c==0 ? WAL_PHYS_ARRAY : WAL_PHYS_ARRAY_ONE_LONG;
            log.putByte(pos -  8 - 1, header);
            log.putLong(pos -  8, physPos[i]);

            if(c>0){
                log.putLong(pos, physPos[i + 1]);
            }
            log.putData(pos+c, out.buf, outPos, size - c);

            crc32.reset();
            crc32.update(out.buf,outPos, size-c);
            logC |= DataIO.longHash(pos | header | physPos[i] | (c > 0 ? physPos[i + 1] : 0) | crc32.getValue());

            outPos +=size-c;
            if(CC.PARANOID && ! (logSize>=outPos))
                throw new AssertionError();
        }
        logChecksumAdd(logC);
        if(CC.PARANOID && ! (outPos==out.pos))
            throw new AssertionError();
    }


    protected void walIndexVal(long logPos, long ioRecid, long indexVal) {
        if(CC.PARANOID && ! ( locks[Store.lockPos(ioRecid)].writeLock().isHeldByCurrentThread()))
            throw new AssertionError();
        if(CC.PARANOID && ! (logSize>=logPos+1+8+8))
            throw new AssertionError();
        log.putByte(logPos, WAL_INDEX_LONG);
        log.putLong(logPos + 1, ioRecid);
        log.putLong(logPos + 9, indexVal);

        logChecksumAdd(DataIO.longHash(logPos | WAL_INDEX_LONG | ioRecid | indexVal));
    }


    protected long[] logAllocate(long[] physPos) {
        if(CC.PARANOID && ! ( structuralLock.isHeldByCurrentThread()))
            throw new AssertionError();
        logSize+=1+8+8; //space used for index val

        long[] ret = new long[physPos.length];
        for(int i=0;i<physPos.length;i++){
            long size = physPos[i]>>>48;
            //would overlaps Volume Block?
            logSize+=1+8; //space used for WAL_PHYS_ARRAY
            ret[i] = (size<<48) | logSize;

            logSize+=size;
            checkLogRounding();
        }
        log.ensureAvailable(logSize);
        return ret;
    }

    protected void checkLogRounding() {
        if(CC.PARANOID && ! ( structuralLock.isHeldByCurrentThread()))
            throw new AssertionError();
        if((logSize& SLICE_SIZE_MOD_MASK)+MAX_REC_SIZE*2> SLICE_SIZE){
            log.ensureAvailable(logSize+1);
            log.putByte(logSize, WAL_SKIP_REST_OF_BLOCK);
            logSize += SLICE_SIZE - (logSize& SLICE_SIZE_MOD_MASK);
        }
    }


    @Override
    protected <A> A get2(long ioRecid, Serializer<A> serializer) throws IOException {
        if(CC.PARANOID && ! ( locks[Store.lockPos(ioRecid)].getWriteHoldCount()==0||
                locks[Store.lockPos(ioRecid)].writeLock().isHeldByCurrentThread()))
            throw new AssertionError();

        //check if record was modified in current transaction
        long[] r = modified.get(ioRecid);
        //no, read main version
        if(r==null) return super.get2(ioRecid, serializer);
        //check for tombstone (was deleted in current trans)
        if(r==TOMBSTONE || r==PREALLOC || r.length==0) return null;

        //was modified in current transaction, so read it from trans log
        if(r.length==1){
            //single record
            final int size = (int) (r[0]>>>48);
            DataInput in = log.getDataInput(r[0]&LOG_MASK_OFFSET, size);
            return deserialize(serializer,size,in);
        }else{
            //linked record
            int totalSize = 0;
            for(int i=0;i<r.length;i++){
                int c =  i==r.length-1 ? 0: 8;
                totalSize+=  (int) (r[i]>>>48)-c;
            }
            byte[] b = new byte[totalSize];
            int pos = 0;
            for(int i=0;i<r.length;i++){
                int c =  i==r.length-1 ? 0: 8;
                int size = (int) (r[i]>>>48) -c;
                log.getDataInput((r[i] & LOG_MASK_OFFSET) + c, size).readFully(b,pos,size);
                pos+=size;
            }
            if(pos!=totalSize)throw new AssertionError();

            return deserialize(serializer,totalSize, new DataIO.DataInputByteArray(b));
        }
    }

    @Override
    protected void update2(DataIO.DataOutputByteArray out, long ioRecid) {
        final long[] physPos;
        final long[] logPos;

        long indexVal = 0;
        long[] linkedRecords = getLinkedRecordsFromLog(ioRecid);
        if (linkedRecords == null) {
            indexVal = index.getLong(ioRecid);
            linkedRecords = getLinkedRecordsIndexVals(indexVal);
        } else if (linkedRecords == PREALLOC) {
            linkedRecords = null;
        }

        structuralLock.lock();

        try {

            //free first record pointed from indexVal
            if ((indexVal >>> 48) > 0)
                freePhysPut(indexVal, false);

            //if there are more linked records, free those as well
            if (linkedRecords != null) {
                for (int i = 0; i < linkedRecords.length && linkedRecords[i] != 0; i++) {
                    freePhysPut(linkedRecords[i], false);
                }
            }


            //first get space in phys
            physPos = physAllocate(out.pos, false, false);
            //now get space in log
            logPos = logAllocate(physPos);

        } finally {
            structuralLock.unlock();
        }

        //write data into log
        walIndexVal((logPos[0] & LOG_MASK_OFFSET) - 1 - 8 - 8 - 1 - 8, ioRecid, physPos[0] | MASK_ARCHIVE);
        walPhysArray(out, physPos, logPos);

        modified.put(ioRecid, logPos);
    }

    @Override
    protected void delete2(long ioRecid){
        final long logPos;

        long indexVal = 0;
        long[] linkedRecords = getLinkedRecordsFromLog(ioRecid);
        if(linkedRecords==null){
            indexVal = index.getLong(ioRecid);
            if(indexVal==MASK_DISCARD) return;
            linkedRecords = getLinkedRecordsIndexVals(indexVal);
        }

        structuralLock.lock();

        try{
            logPos = logSize;
            checkLogRounding();
            logSize+=1+8+8; //space used for index val
            log.ensureAvailable(logSize);

            //free first record pointed from indexVal
            if((indexVal>>>48)>0)
                freePhysPut(indexVal,false);

            //if there are more linked records, free those as well
            if(linkedRecords!=null){
                for(int i=0; i<linkedRecords.length &&linkedRecords[i]!=0;i++){
                    freePhysPut(linkedRecords[i],false);
                }
            }

        }finally {
            structuralLock.unlock();
        }
        walIndexVal(logPos,ioRecid, MASK_DISCARD|MASK_ARCHIVE);
        modified.put(ioRecid, TOMBSTONE);
    }

    @Override
    public void commit() {
        lockAllWrite();
        try{
            if(serializerPojo!=null && serializerPojo.hasUnsavedChanges()){
                serializerPojo.save(this);
            }

            if(!logDirty()){
                return;
            }

            //dump long stack pages
            int crc = 0;
            LongMap.LongMapIterator<byte[]> iter = longStackPages.longMapIterator();
            while(iter.moveToNext()){
                if(CC.PARANOID && ! (iter.key()>>>48==0))
                    throw new AssertionError();
                final byte[] array = iter.value();
                final long pageSize = ((array[0]&0xFF)<<8)|(array[1]&0xFF) ;
                if(CC.PARANOID && ! (array.length==pageSize))
                    throw new AssertionError();
                final long firstVal = (pageSize<<48)|iter.key();
                log.ensureAvailable(logSize+1+8+pageSize);

                crc |= DataIO.longHash(logSize | WAL_LONGSTACK_PAGE | firstVal);

                log.putByte(logSize, WAL_LONGSTACK_PAGE);
                logSize+=1;
                log.putLong(logSize, firstVal);
                logSize+=8;

                //put array
                CRC32 crc32  = new CRC32();
                crc32.update(array);
                crc |= crc32.getValue();
                log.putData(logSize,array,0,array.length);
                logSize+=array.length;

                checkLogRounding();
            }


            for(int i=IO_FREE_RECID;i<IO_USER_START;i+=8){
                if(!indexValsModified[i/8]) continue;
                log.ensureAvailable(logSize + 17);
                logSize+=17;
                walIndexVal(logSize-17, i,indexVals[i/8]);
                //no need to update crc, since IndexVal already does it
            }

            //seal log file
            log.ensureAvailable(logSize + 1 + 3*6 + 8+4);
            long indexChecksum = indexHeaderChecksumUncommited();
            crc|= DataIO.longHash(logSize | WAL_SEAL | indexSize | physSize | freeSize | indexChecksum);
            log.putByte(logSize, WAL_SEAL);
            logSize+=1;
            log.putSixLong(logSize, indexSize);
            logSize+=6;
            log.putSixLong(logSize,physSize);
            logSize+=6;
            log.putSixLong(logSize,freeSize);
            logSize+=6;
            log.putLong(logSize, indexChecksum);
            logSize+=8;
            log.putInt(logSize, crc|logChecksum.get());
            logSize+=4;

            //write mark it was sealed
            log.putLong(8, LOG_SEAL);

            //and flush log file
            if(!syncOnCommitDisabled) log.sync();

            replayLogFile();
            reloadIndexFile();
        }finally {
            unlockAllWrite();
        }

    }

    protected boolean logDirty() {

        if(logSize!=16 || !longStackPages.isEmpty() || !modified.isEmpty())
            return true;

        for(boolean b: indexValsModified){
            if(b)
                return true;
        }

        return false;
    }

    protected long indexHeaderChecksumUncommited() {
        long ret = 0;

        for(int offset = 0;offset<IO_USER_START;offset+=8){
            if(offset == IO_INDEX_SUM) continue;
            long indexVal;

            if(offset==IO_INDEX_SIZE){
                indexVal = indexSize;
            }else if(offset==IO_PHYS_SIZE){
                indexVal = physSize;
            }else if(offset==IO_FREE_SIZE){
                indexVal = freeSize;
            }else
                indexVal = indexVals[offset / 8];

            ret +=  indexVal + DataIO.longHash(indexVal + offset) ;
        }

        return ret;
    }

    protected boolean verifyLogFile() {
        if(CC.PARANOID && ! ( structuralLock.isHeldByCurrentThread()))
            throw new AssertionError();

        if(readOnly && log==null)
            return false;

        logSize = 0;



        //read headers
        if (log.isEmpty() ||
                (log.getFile()!=null && log.getFile().length()<16) ||
                log.getInt(0) != HEADER || log.getLong(8) != LOG_SEAL) {
            return false;
        }

        if (log.getUnsignedShort(4) > STORE_VERSION) {
            throw new IOError(new IOException("New store format version, please use newer MapDB version"));
        }

        if (log.getUnsignedShort(6) != expectedMasks())
            throw new IllegalArgumentException("Log file created with different features. Please check compression, checksum or encryption");

        try {
            final CRC32 crc32 = new CRC32();

            //all good, calculate checksum
            logSize = 16;
            byte ins = log.getByte(logSize);
            logSize += 1;
            int crc = 0;

            while (ins != WAL_SEAL){
                if (ins == WAL_INDEX_LONG) {
                    long ioRecid = log.getLong(logSize);
                    logSize += 8;
                    long indexVal = log.getLong(logSize);
                    logSize += 8;
                    crc |= DataIO.longHash((logSize - 1 - 8 - 8) | WAL_INDEX_LONG | ioRecid | indexVal);
                } else if (ins == WAL_PHYS_ARRAY) {
                    final long offset2 = log.getLong(logSize);
                    logSize += 8;
                    final int size = (int) (offset2 >>> 48);

                    byte[] b = new byte[size];
                    log.getDataInput(logSize, size).readFully(b);

                    crc32.reset();
                    crc32.update(b);

                    crc |= DataIO.longHash(logSize | WAL_PHYS_ARRAY | offset2 | crc32.getValue());

                    logSize += size;
                } else if (ins == WAL_PHYS_ARRAY_ONE_LONG) {
                    final long offset2 = log.getLong(logSize);
                    logSize += 8;
                    final int size = (int) (offset2 >>> 48) - 8;

                    final long nextPageLink = log.getLong(logSize);
                    logSize += 8;

                    byte[] b = new byte[size];
                    log.getDataInput(logSize, size).readFully(b);
                    crc32.reset();
                    crc32.update(b);

                    crc |= DataIO.longHash((logSize) | WAL_PHYS_ARRAY_ONE_LONG | offset2 | nextPageLink | crc32.getValue());

                    logSize += size;
                } else if (ins == WAL_LONGSTACK_PAGE) {
                    final long offset = log.getLong(logSize);
                    logSize += 8;
                    final long origLogSize = logSize;
                    final int size = (int) (offset >>> 48);

                    crc |= DataIO.longHash(origLogSize | WAL_LONGSTACK_PAGE | offset);

                    byte[] b = new byte[size];
                    log.getDataInput(logSize, size).readFully(b);
                    crc32.reset();
                    crc32.update(b);
                    crc |= crc32.getValue();

                    log.getDataInput(logSize, size).readFully(b);
                    logSize+=size;
                } else if (ins == WAL_SKIP_REST_OF_BLOCK) {
                    logSize += SLICE_SIZE - (logSize & SLICE_SIZE_MOD_MASK);
                } else {
                    return false;
                }

                ins = log.getByte(logSize);
                logSize += 1;
            }

            long indexSize = log.getSixLong(logSize);
            logSize += 6;
            long physSize = log.getSixLong(logSize);
            logSize += 6;
            long freeSize = log.getSixLong(logSize);
            logSize += 6;
            long indexSum = log.getLong(logSize);
            logSize += 8;
            crc |= DataIO.longHash((logSize - 1 - 3 * 6 - 8) | indexSize | physSize | freeSize | indexSum);

            final int realCrc = log.getInt(logSize);
            logSize += 4;

            logSize = 0;
            if(CC.PARANOID && !  (structuralLock.isHeldByCurrentThread()))
                throw new AssertionError();

            //checksum is broken, so disable it
            return true;
        } catch (IOException e) {
            LOG.log(Level.INFO, "Revert corrupted Write-Ahead-Log.",e);
            return false;
        }catch(IOError e){
            LOG.log(Level.INFO, "Revert corrupted Write-Ahead-Log.",e);
            return false;
        }
    }



    protected void replayLogFile(){
        if(CC.PARANOID && ! ( structuralLock.isHeldByCurrentThread()))
            throw new AssertionError();

        if(readOnly && log==null)
            return; //TODO how to handle log replay if we are readonly?

        logSize = 0;


        //read headers
        if(log.isEmpty() || log.getInt(0)!=HEADER ||
                log.getUnsignedShort(4)>STORE_VERSION || log.getLong(8) !=LOG_SEAL ||
                log.getUnsignedShort(6)!=expectedMasks()){
            //wrong headers, discard log
            logReset();
            return;
        }

        if(CC.LOG_STORE && LOG.isLoggable(Level.FINE))
            LOG.log(Level.FINE,"Replay WAL started {0}",log);

        //all good, start replay
        logSize=16;
        byte ins = log.getByte(logSize);
        logSize+=1;

        while(ins!=WAL_SEAL){
            if(ins == WAL_INDEX_LONG){
                long ioRecid = log.getLong(logSize);
                logSize+=8;
                long indexVal = log.getLong(logSize);
                logSize+=8;
                index.ensureAvailable(ioRecid+8);
                index.putLong(ioRecid, indexVal);
            }else if(ins == WAL_PHYS_ARRAY||ins == WAL_LONGSTACK_PAGE || ins == WAL_PHYS_ARRAY_ONE_LONG){
                long offset = log.getLong(logSize);
                logSize+=8;
                final int size = (int) (offset>>>48);
                offset = offset&MASK_OFFSET;

                //transfer buffer directly from log file without copying into memory
                phys.ensureAvailable(offset+size);
                log.transferInto(logSize,phys,offset,size);

                logSize+=size;
            }else if(ins == WAL_SKIP_REST_OF_BLOCK){
                logSize += SLICE_SIZE -(logSize& SLICE_SIZE_MOD_MASK);
            }else{
                throw new AssertionError("unknown trans log instruction '"+ins +"' at log offset: "+(logSize-1));
            }

            ins = log.getByte(logSize);
            logSize+=1;
        }
        index.putLong(IO_INDEX_SIZE,log.getSixLong(logSize));
        logSize+=6;
        index.putLong(IO_PHYS_SIZE,log.getSixLong(logSize));
        logSize+=6;
        index.putLong(IO_FREE_SIZE,log.getSixLong(logSize));
        logSize+=6;
        index.putLong(IO_INDEX_SUM,log.getLong(logSize));
        logSize+=8;



        //flush dbs
        if(!syncOnCommitDisabled){
            phys.sync();
            index.sync();
        }

        if(CC.LOG_STORE && LOG.isLoggable(Level.FINE))
            LOG.log(Level.FINE,"Replay WAL done at size {0,number,integer}",logSize);

        logReset();

        if(CC.PARANOID && ! ( structuralLock.isHeldByCurrentThread()))
            throw new AssertionError();
    }



    @Override
    public void rollback() throws UnsupportedOperationException {
        lockAllWrite();
        try{
            //discard trans log
            logReset();

            reloadIndexFile();
        }finally {
            unlockAllWrite();
        }
    }

    protected long[] getLinkedRecordsFromLog(long ioRecid){
        if(CC.PARANOID && ! ( locks[Store.lockPos(ioRecid)].writeLock().isHeldByCurrentThread()))
            throw new AssertionError();
        long[] ret0 = modified.get(ioRecid);
        if(ret0==PREALLOC) return ret0;

        if(ret0!=null && ret0!=TOMBSTONE){
            long[] ret = new long[ret0.length];
            for(int i=0;i<ret0.length;i++){
                long offset = ret0[i] & LOG_MASK_OFFSET;
                //offset now points to log file, read phys offset from log file
                ret[i] =  log.getLong(offset-8);
            }
            return ret;
        }
        return null;
    }

    @Override
    protected long longStackTake(long ioList, boolean recursive) {
        if(CC.PARANOID && ! ( structuralLock.isHeldByCurrentThread()))
            throw new AssertionError();
        if(CC.PARANOID && ! (ioList>=IO_FREE_RECID && ioList<IO_USER_START))
            throw new AssertionError("wrong ioList: "+ioList);


        long dataOffset = indexVals[((int) ioList/8)];
        if(dataOffset == 0)
            return 0; //there is no such list, so just return 0

        long pos = dataOffset>>>48;
        dataOffset &= MASK_OFFSET;
        byte[] page = longStackGetPage(dataOffset);

        if(pos<8) throw new AssertionError();

        final long ret = longStackGetSixLong(page, (int) pos);

        //was it only record at that page?
        if(pos == 8){
            //yes, delete this page
            long next = longStackGetSixLong(page,2);
            long size = ((page[0]&0xFF)<<8) | (page[1]&0xFF);
            if(CC.PARANOID && ! (size == page.length))
                throw new AssertionError();
            if(next !=0){
                //update index so it points to previous page
                byte[] nextPage = longStackGetPage(next); //TODO this page is not modifed, but is added to LOG
                long nextSize = ((nextPage[0]&0xFF)<<8) | (nextPage[1]&0xFF);
                if(CC.PARANOID && ! ((nextSize-8)%6==0))
                    throw new AssertionError();
                indexVals[((int) ioList/8)]=((nextSize-6)<<48)|next;
                indexValsModified[((int) ioList/8)]=true;
            }else{
                //zero out index
                indexVals[((int) ioList/8)]=0L;
                indexValsModified[((int) ioList/8)]=true;
                if(maxUsedIoList==ioList){
                    //max value was just deleted, so find new maxima
                    while(indexVals[((int) maxUsedIoList/8)]==0 && maxUsedIoList>IO_FREE_RECID){
                        maxUsedIoList-=8;
                    }
                }
            }
            //put space used by this page into free list
            freePhysPut((size<<48) | dataOffset, true);
            if(CC.PARANOID && ! (dataOffset>>>48==0))
                throw new AssertionError();
            longStackPages.remove(dataOffset);
        }else{
            //no, it was not last record at this page, so just decrement the counter
            pos-=6;
            indexVals[((int) ioList/8)] = (pos<<48)| dataOffset;
            indexValsModified[((int) ioList/8)] = true;
        }

        //System.out.println("longStackTake: "+ioList+" - "+ret);

        return ret;

    }

    @Override
    protected void longStackPut(long ioList, long offset, boolean recursive) {
        if(CC.PARANOID && ! ( structuralLock.isHeldByCurrentThread()))
            throw new AssertionError();
        if(CC.PARANOID && ! (offset>>>48==0))
            throw new AssertionError();
        if(CC.PARANOID && ! (ioList>=IO_FREE_RECID && ioList<=IO_USER_START))
            throw new AssertionError("wrong ioList: "+ioList);

        long dataOffset = indexVals[((int) ioList/8)];
        long pos = dataOffset>>>48;
        dataOffset &= MASK_OFFSET;

        if(dataOffset == 0){ //empty list?
            //yes empty, create new page and fill it with values
            final long listPhysid = freePhysTake((int) LONG_STACK_PREF_SIZE,true,true) &MASK_OFFSET;
            if(listPhysid == 0) throw new AssertionError();
            if(CC.PARANOID && ! (listPhysid>>>48==0))
                throw new AssertionError();
            //set previous Free Index List page to zero as this is first page
            //also set size of this record
            byte[] page = new byte[(int) LONG_STACK_PREF_SIZE];
            page[0] = (byte) (0xFF & (page.length>>>8));
            page[1] = (byte) (0xFF & (page.length));
            longStackPutSixLong(page,2,0L);
            //set  record
            longStackPutSixLong(page, 8, offset);
            //and update index file with new page location
            indexVals[((int) ioList/8)] = ( 8L << 48) | listPhysid;
            indexValsModified[((int) ioList/8)] = true;
            if(maxUsedIoList<=ioList) maxUsedIoList=ioList;
            longStackPages.put(listPhysid,page);
        }else{
            byte[] page = longStackGetPage(dataOffset);
            long size = ((page[0]&0xFF)<<8)|(page[1]&0xFF);

            if(CC.PARANOID && ! (pos+6<=size))
                throw new AssertionError();
            if(pos+6==size){ //is current page full?
                long newPageSize = LONG_STACK_PREF_SIZE;
                if(ioList == size2ListIoRecid(LONG_STACK_PREF_SIZE)){
                    //TODO double allocation fix needs more investigation
                    newPageSize = LONG_STACK_PREF_SIZE_ALTER;
                }
                //yes it is full, so we need to allocate new page and write our number there
                final long listPhysid = freePhysTake((int) newPageSize,true,true) &MASK_OFFSET;
                if(listPhysid == 0) throw new AssertionError();

                byte[] newPage = new byte[(int) newPageSize];

                //set current page size
                newPage[0] = (byte) (0xFF & (newPageSize>>>8));
                newPage[1] = (byte) (0xFF & (newPageSize));
                //set location to previous page and
                longStackPutSixLong(newPage,2,dataOffset&MASK_OFFSET);


                //set the value itself
                longStackPutSixLong(newPage, 8, offset);
                if(CC.PARANOID && ! (listPhysid>>>48==0))
                    throw new AssertionError();
                longStackPages.put(listPhysid,newPage);

                //and update index file with new page location and number of records
                indexVals[((int) ioList/8)] = (8L<<48) | listPhysid;
                indexValsModified[((int) ioList/8)] = true;
            }else{
                //there is space on page, so just write offset and increase the counter
                pos+=6;
                longStackPutSixLong(page, (int) pos,offset);
                indexVals[((int) ioList/8)] = (pos<<48)| dataOffset;
                indexValsModified[((int) ioList/8)] = true;
            }
        }
    }

    //TODO move those two methods into Volume.ByteArrayVol
    protected static long longStackGetSixLong(byte[] page, int pos) {
        return
                ((long) (page[pos++] & 0xff) << 40) |
                        ((long) (page[pos++ ] & 0xff) << 32) |
                        ((long) (page[pos++] & 0xff) << 24) |
                        ((long) (page[pos++] & 0xff) << 16) |
                        ((long) (page[pos++] & 0xff) << 8) |
                        ((long) (page[pos] & 0xff));
    }


    protected static void longStackPutSixLong(byte[] page, int pos, long value) {
        if(CC.PARANOID && (value>>>48)!=0)
            throw new AssertionError("value does not fit");
        page[pos++] = (byte) (0xff & (value >> 40));
        page[pos++] = (byte) (0xff & (value >> 32));
        page[pos++] = (byte) (0xff & (value >> 24));
        page[pos++] = (byte) (0xff & (value >> 16));
        page[pos++] = (byte) (0xff & (value >> 8));
        page[pos] = (byte) (0xff & (value));

    }


    protected byte[] longStackGetPage(long offset) {
        if(CC.PARANOID && ! (offset>=16))
            throw new AssertionError();
        if(CC.PARANOID && ! (offset>>>48==0))
            throw new AssertionError();

        byte[] ret = longStackPages.get(offset);
        if(ret==null){
            //read page size
            int size = phys.getUnsignedShort(offset);
            if(CC.PARANOID && ! (size>=8+6))
                throw new AssertionError();
            ret = new byte[size];
            try {
                phys.getDataInput(offset,size).readFully(ret);
            } catch (IOException e) {
                throw new IOError(e);
            }

            //and load page
            longStackPages.put(offset,ret);
        }

        return ret;
    }

    @Override
    public void close() {
        if(serializerPojo!=null && serializerPojo.hasUnsavedChanges()){
            serializerPojo.save(this);
        }

        lockAllWrite();
        try{
            if(log !=null){
                log.sync();
                log.close();
                if(deleteFilesAfterClose){
                    log.deleteFile();
                }
            }

            index.sync();
            phys.sync();

            index.close();
            phys.close();
            if(deleteFilesAfterClose){
                index.deleteFile();
                phys.deleteFile();
            }
            index = null;
            phys = null;
        }finally {
            unlockAllWrite();
        }
    }

    @Override protected void compactPreUnderLock() {
        if(CC.PARANOID && ! ( structuralLock.isLocked()))
            throw new AssertionError();
        if(logDirty())
            throw new DBException(DBException.Code.ENGINE_COMPACT_UNCOMMITED);
    }

    @Override protected void compactPostUnderLock() {
        if(CC.PARANOID && ! ( structuralLock.isLocked()))
            throw new AssertionError();
        reloadIndexFile();
    }


    @Override
    public boolean canRollback(){
        return true;
    }

    protected void logChecksumAdd(int cs) {
        for(;;){
            int old = logChecksum.get();
            if(logChecksum.compareAndSet(old,old|cs))
                return;
        }
    }



}
