#-*-coding:UTF-8-*-
from filemanager import FileInfo, checkfile, gen_filelist, filepathnormalize
from encryptedsocket import EncryptedSocket
from ftplib import FTP, error_perm
try:
    # python 3.x
    from urllib.request import urlopen
except ImportError:
    # python 2.7.x
    from urllib2 import urlopen
import socket
import os
import sys
import pickle
import time
import log


class Client(object):
    """
    클라이언트 클래스
    클라이언트로 사용자가 업데이트/동기화를 설정하면
    해당 작업으로 분기해 실행한다.
    동기화는 서버에 동기화 요청을 하고 서버로 부터 파일 정보를 받아서 현재 파일 정보와 대조한 후 
    필요한 파일을 ftp서버로 부터 다운받는다.
    업데이트는 서버로 업데이트 요청을 하고 서버로 허가가 떨어지면 파일 데이터를 송신한다.
    이후 서버로 부터 필요한 파일에 대한 정보가 수신되면 ftp서버로 파일을 업로드한다.
    """
    
    def __init__(self, address, path='.'):
        self.serv_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.address = address
        self.connect(address)
        self.__setpath(path)

    def connect(self, address):
        self.serv_sock.connect(address)
        log.logging("connect server {0}:{1}".format(address[0], address[1]))
    
    def close(self):
        self.serv_sock.close()
    
    def request_update(self):
        """
        서버로 update메시지를 보내 업데이트 요청을 하고
        서버의 파일리스트를 수신한다.
        그 리스트와 현재 폴더의 파일들을 비교하고 필요한 서버 파일을 업로드한다.
        """
        # 소켓을 암호화 소켓으로 감싼다.
        sock = EncryptedSocket(self.serv_sock)
        # update메시지를 함호화해서 전송
        sock.sendall("update")
            
        log.logging("request update to server")
        
        #서버의 파일리스트 수신
        buf = sock.recvall()
        
        if not buf:
            log.logging("lost connection.", target=sys.stderr)
            return False
        if "Server is update please retry later" == buf:
            log.logging(buf)
            return
            
        f_list = gen_filelist(buf, update=True)
        
#        print f_list
        # ftp 서버 접속
        f = FTPManager(self.address)
        f.connect()
        # 서버의 파일삭제
        self.__serv_file_delete(f_list['delete'], f)
        # 필요한 파일 업로드
        self.__file_upload(f_list['update'], f)
        # 서버사용을 마치고 연결 해제
        f.closeserver()
        
        log.logging("finish update files")
        return True
        
    def request_sync(self):
        """
        서버로 sync메시지를 보내 동기화를 요청을 하고
        서버의 파일리스트를 수신한다.
        그 리스트와 현재 폴더의 파일들을 비교하고 필요한 서버파일을 다운로드한다.
        """
        # 소켓을 암호화 소켓으로 감싼다.
        sock = EncryptedSocket(self.serv_sock)
        # sync메시지를 암호화 해서 전송
        sock.sendall("sync")
            
        log.logging("request sync to server")
        # 서버의 파일리스트 수신
        buf = sock.recvall()
        
        if not buf :
            log.logging("lost connection.", target=sys.stderr)
            return False            
        
        # 역피클(역직렬화) 시켜 파일 정보가 들어있는 리스트로 만든다.
        f_list = gen_filelist(buf)
        
        # ftp 서버 접속
        f = FTPManager(self.address)
        f.connect()
        # 파일 삭제
        self.__file_delete(f_list['delete'])
        # 필요한 파일 다운로드
        self.__file_down(f_list['update'], f)
        # 서버사용을 마치고 연결 해제
        f.closeserver()
        log.logging("finish sync files")
        return True
            
    def __setpath(self, path='.'):
        """
        작업 경로를 설정한다.
        """
        self.dir_path = path
        # 경로가 없으면 생성
        if not os.path.exists(path):
            os.makedirs(path)
        os.chdir(path)
        
    def __file_down(self, f_list, ftpmanager):
        """
        ftp서버에서 파일리스트에 있는 파일들을 다운로드한다.
        """
        # 진행상황 확인용 변수 초기화
        ftpmanager.dwed_cnt = 0
        ftpmanager.dw_cnt = len(f_list)
        for f_info in f_list:
            try:
                
                ftpmanager.dwed_cnt+=1
                if f_info.size == 'dir':
                    ftpmanager.downdir(f_info.path)
                else:
                    ftpmanager.downfile(f_info.path)
                    os.utime(filepathnormalize(f_info.path), (f_info.a_time, f_info.m_time))
            except KeyboardInterrupt:
                ftpmanager.abort()
                log.logging('cancel download %s'%f_info)
                return
        else:
            print

        
    def __file_upload(self, f_list, ftpmanager):
        """
        ftp서버에서 파일리스트에 있는 파일들을 업로드한다.
        """
        # 진행상황 확인용 변수 초기화
        ftpmanager.uped_cnt = 0
        ftpmanager.up_cnt = len(f_list)
        for f_info in f_list:
            
            ftpmanager.uped_cnt += 1
            try:
                ftpmanager.upfile(f_info.path)
            except KeyboardInterrupt:
                ftpmanager.abort()
                log.logging('cancel upload %s'%f_info.path)
                return
        else:
            print
        
        encsock = EncryptedSocket(self.serv_sock)
        encsock.sendall(pickle.dumps(f_list))
    
    def __file_delete(self, f_list):
        """
        클라이언트의 필요없는 파일을 지운다.
        """
        for f_path in f_list:
            if os.path.isfile(f_path):
                os.remove(f_path)
            else:
                try:
                    os.rmdir(f_path)
                except OSError as oe:
                    pass
                
    def __serv_file_delete(self, f_list, ftpmanager):
        """
        서버파일 삭제요청
        """
        for f_path in f_list:
            ftpmanager.delfile(f_path)
        
        
class FTPManager(object):
    """
    FTP서버와 직접 연결할 객체
    서버로 다운로드 업로드를 한다.
    """
    SERV_ID = 'filesync'
    SERV_PASSWD = 'r82lskd09'
    
    def __init__(self, serv_adr):
        self.serv_adr = serv_adr
        self.ftp = FTP()
    
    def connect(self):
        """
        서버와 연결한다.
        """
        self.ftp.connect(self.serv_adr[0], int(self.serv_adr[1])-1)
        self.ftp.login(self.SERV_ID, self.SERV_PASSWD)
        
        # 서버에 파일의 사이즈를 요청하기 위해 바이너리모드로 변경한다.
        self.ftp.voidcmd('TYPE I')
        
    def abort(self):
        self.ftp.abort()
    
    def downdir(self, f):
        """
        빈 폴더만 생성한다.
        """
        en_path = filepathnormalize(f)
        os.makedirs(en_path)            
    
    def downfile(self, f):
        """
        ftp 서버로 부터 파일을 다운로드한다.
        """
        en_path = filepathnormalize(f)
        # 파일 경로를 폴더명과 파일명으로 나눈다.
        f_dir, f_name = os.path.dirname(en_path), os.path.basename(en_path)
        
        #log.logging("download start:{}".format(f_name))
        
        # 하위 폴더가 존재한다면 폴더를 생성한다.
        if not os.path.exists(f_dir):
            os.makedirs(f_dir)
        
        # 다운받을 파일 생성
        d_file = open(en_path, "wb")
                    
        try:
            # 파일을 요청한다. 데이터를 바로 저장하게 한다.
            filesize = self.ftp.size(f)
            self.ftp.retrbinary("RETR %s"%f, self.writedatatofile(d_file, filesize))
        except error_perm as e:
            log.logging(e)
        
        # 파일, 소켓 사용을 끝냈으므로 닫는다.
        d_file.close()

        # 파일을 다운로드 한 후 데이터의 생성, 접근시간을 서버의 것과 동일하게 설정한다.
        #os.utime(en_path, (f_info.a_time-1000, f_info.m_time))
        #log.logging("download finish:{}".format(f_name))
    
    def upfile(self, f):
        """
        ftp서버로 파일을 업로드한다.
        """
        #log.logging("upload start:%s" % f)
        
        f_dirs = os.path.dirname(f).split('/')
        
        if os.path.isdir(f):
            f_dirs.append(os.path.basename(f))
            self.mkdstoserver(f_dirs)
        else:
            up_file = BinaryFileReader(f, self.uploadprogress)
            
            # 파일을 전송한다.
            try:
                self.ftp.storbinary("STOR %s"%f, up_file)
            except error_perm as e:
                # 만약 파일이 들어갈 디렉토리가 서버에 존재하지 않으면 디렉토리를 만들어준다.
                if 'No such file or directory' in e.message:
                    self.mkdstoserver(f_dirs)
                    self.ftp.storbinary("STOR %s"%f, up_file)
                else:
                    raise e
            up_file.close()
            #log.logging("upload finish:%s" % f)
        
    def mkdstoserver(self, f_dirs):
#        log.logging(f_dirs)
        f_dir = f_dirs[0]+'/'+f_dirs[1]
        for dirname in f_dirs[2:]:
            try:
#               log.logging(f_dir)
                self.ftp.mkd(f_dir)
            except error_perm as e2:
                if 'File exists' in e2.message:
                    pass
                else:
                    raise e2
            f_dir += ('/'+dirname)
            
#       log.logging(f_dir)
        try:
            self.ftp.mkd(f_dir)
        except error_perm as e2:
            if 'File exists' in e2.message:
                pass
            else:
                raise e2

    def delfile(self, f):
        """
        서버의 f경로의 파일을 삭제한다.
        """
        try:        
            self.ftp.delete(f)
        except error_perm as e:
            if 'No such file' in e.message:
                pass
    
    def writedatatofile(self, file, filesize):
        """
        데이터를 수신하고 파일에 저장하는 클로저를 생성하는 함수
        """
        # 지금까지 받은 바이트의 갯수를 저장할 변수
        self.getbytes = 0
        def recvdata(buf):
            self.getbytes = self.getbytes + len(buf)
            progress = 1.0*self.getbytes/filesize
            
            print "\rDownloading {} {}/{}files complete".format(log.generateprogress(progress, 30), \
                self.dwed_cnt, self.dw_cnt),
            
            sys.stdout.flush()
            file.write(buf)
        return recvdata
        
    def uploadprogress(self, readbyte, totalbyte):
        
        print "\rUploading {} {}/{}files complete".format(log.generateprogress((readbyte, totalbyte), 30), \
            self.uped_cnt, self.up_cnt),
        sys.stdout.flush()
    
    def closeserver(self):
        self.ftp.quit()
         
    @classmethod
    def simpledownfiles(cls, serverIp, f_list):
        for f_path in f_list:
            u = urlopen('ftp://{}:{}@{}:{}/{}'.format(cls.SERV_ID, cls.SERV_PASSWD, serverIp, cls.SERV_PORT, f_path))
        
class BinaryFileReader(object):
    """
    파일을 바이너리로 불러온다.
    파일을 read할 때마다 콜백함수를 호출한다.
    """
    def __init__(self, f_path, readcallback):
        self.file = open(f_path, 'rb')
        self.filesize = os.stat(f_path).st_size
        self.readbyte = 0
        self.readcallback = readcallback
        
    def __del__(self):
        #print 'file close'
        if not self.file.closed:
            self.file.close()
        
    def read(self, bufsize):
        """
        파일을 읽고 콜백함수를 호출한다.
        """
        buf = self.file.read(bufsize)
        self.readbyte += len(buf)
        if self.readcallback:
            self.readcallback(self.readbyte, self.filesize)
            
        return buf
    
    def close(self):
        self.file.close()

def main():
    """
    클라이언트 루틴을 생성한다.
    """
    if len(sys.argv) < 3:
        print >> sys.stderr, 'python client.py ip port [path]'
        raise SystemExit(1)
    
    cur_path = None
    if len(sys.argv) >= 4:
        cur_path = sys.argv[3]
    else:
        cur_path = '.'
    
    c = Client((sys.argv[1], int(sys.argv[2])), path=cur_path)
    try:
        while True:
            work = raw_input("어떤 작업을 하시겠습니까?\nupdate(u) sync(s) Quit(q):")
            if work == 's' or work == 'S' or work == 'sync' or work == 'Sync' :
                c.request_sync()
            elif work == 'u' or work == 'U' or work == 'update' or work == 'Update' :
                c.request_update()
            elif work == 'q' or work == 'Q' or work == 'Quit' or work == 'quit' :
                print 'bye'
                break;
    except KeyboardInterrupt:
        print 'bye'
    
    c.close()
    
if __name__=='__main__':
    main()
