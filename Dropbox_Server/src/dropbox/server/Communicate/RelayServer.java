package dropbox.server.Communicate;

import dropbox.common.ByteConverter;
import dropbox.common.Message;
import dropbox.common.MessageWrapper;
import dropbox.server.Account.AccountInfo;
import dropbox.server.Account.AccountManager;
import dropbox.server.FileManage.FileManager;
import dropbox.server.Group.GroupManager;
import dropbox.server.Util.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.Iterator;

/**
 * Created by micky on 2014. 11. 8..
 * 중계 서버를 구동하는 인스턴스
 * 클라이언트로부터 메시지를 받아 파싱한 후 그에 대응하는 작업을 진행한다.
 * 중계 서버 인스턴스는 하나의 프로세스에 하나만 존재한다.
 */
public class RelayServer implements Runnable{

    // static end

    private Selector selector = null;

    private Thread relayThread = null;

    RelayServer(String ip, int port){
        this(new InetSocketAddress(ip, port));
    }

    RelayServer(InetSocketAddress serverSocketAddress) {
        initServer(serverSocketAddress);
    }



    /**
     * 서버 내부 인스턴스를 초기화한다.
     * @param serverSocketAddress 서버의 주소정보
     */
    private void initServer(InetSocketAddress serverSocketAddress) {
        try {
            selector = Selector.open();
            ServerSocketChannel serverSockChennel = ServerSocketChannel.open();
            serverSockChennel.configureBlocking(false);
            ServerSocket serverSocket = serverSockChennel.socket();

            serverSocket.bind(serverSocketAddress);

            //serverSockChennel.register(selector, SelectionKey.OP_ACCEPT);
            registerChannel(serverSockChennel, SelectionKey.OP_ACCEPT);
        }
        catch(IOException ioe) {
            Logger.errorLogging("IOException", ioe);
        }
    }

    /**
     * 서버 구동
     */
    public void startServer() {
        relayThread = new Thread(this);
        relayThread.setDaemon(true);
        relayThread.start();
    }

    public void stopServer() {
        relayThread.interrupt();
    }

    /**
     * 클라이언트의 연결을 받는다.
     * @param key
     * @return
     */
     private boolean connectAcceptClient(SelectionKey key) {
        ServerSocketChannel serverChannel = (ServerSocketChannel)key.channel();

        try {
            SocketChannel sc = serverChannel.accept();
            registerChannel(sc, SelectionKey.OP_READ);
            Logger.logging("Connect form "+sc.getRemoteAddress());
        }
        catch(ClosedChannelException cce) {
            Logger.errorLogging("Selector was closed", cce);
        }
        catch(IOException ioe) {
            Logger.errorLogging(ioe);
            return false;
        }
        return true;
    }

    /**
     * 소켓연결을 끊는다.
     * @param socketChannel
     * @throws IOException
     */
    private void disconnect(SocketChannel socketChannel) {
        try {
            Logger.logging("user "+socketChannel.getRemoteAddress()+" is disconnect");
            AccountManager.getManager().deleteSession(socketChannel);
            socketChannel.close();

        } catch (IOException e) {
            Logger.errorLogging(e);
        }
    }

    /**
     * 셀렉터에 이벤트와 소켓을 등록한다.
     * @param sc 등록할 소켓 체널
     * @param ops 리스닝할 이벤트
     * @throws IOException
     */
    private void registerChannel(AbstractSelectableChannel sc, int ops)
            throws IOException {

        if(sc == null) {
            Logger.errorLogging("invalid socket", null);
            return ;
        }

        sc.configureBlocking(false);
        sc.register(selector, ops);
    }

    private void receiveMessage(SelectionKey key) {
        Logger.logging("recv start");
        SocketChannel sc = (SocketChannel)key.channel();
        // 메모리를 직접할당해 가비지 컬렉션에 잡히지 않게 처리해
        // 나중에 full gc에 걸리는 시간을 줄인다.
        ByteBuffer buffer = ByteBuffer.allocate(MessageWrapper.MESSAGE_SIZE);

        AccountInfo loginInfo = AccountManager.getManager().getLoginInfo(sc);
        if(loginInfo != null) {
            Logger.logging(String.format("message from user : %s!", loginInfo.getEmail()));
        }
        try {
            int read =0;
            int readbytes = 0;
            int messageSize = 10000;
            boolean firstread = true;

            // 현재 소켓의 수신이 가능하고 전체 메시지 길이 보다 전체
            // 데이터를 수신한다.
            readloop:
            while(key.isReadable() && readbytes < messageSize) {
                read = sc.read(buffer);
                // 소켓 연결이 끊어진 상태라면 정식으로 연결을 종료한다.
                if (read==-1) {
                    disconnect(sc);
                    return;
                }
                // 수신한 데이터 체크
                readbytes += read;
                // 첫수신이면 전체 메시지 길이가 얼마나 되는지 확인한다.
                if(firstread) {
                    byte[] temp = buffer.array();
                    messageSize = ByteConverter.byteArrayToInt(temp[0], temp[1], temp[2], temp[3]);
                    if(messageSize> 10000) messageSize=10000;
                    firstread = false;
                }

                Logger.debugLogging("receive read" + read + "  readbytes = " + readbytes + " messagesize " + messageSize);
            }

            // 수신한 데이터를 파싱한다.
            parse(sc, buffer);
            buffer.clear();

        } catch(IOException ioe) {
            Logger.errorLogging(ioe);
            if(ioe.getMessage().contains("Connection reset by peer")) {
                disconnect(sc);
            }
        }
    }

    //private void parse(InputStream inputStream) {
    private void parse(SocketChannel sc, ByteBuffer buffer) {
        try {
            Message msg = MessageWrapper.byteArrayToMessage(buffer.array());

            Logger.logging(msg.messageType+"");
            Logger.logging(msg.msg);

            switch (msg.messageType) {
                // file request
                case File:
                    FileManager.getManager().receiveMessage(sc, msg);
                    break;
                // account request
                case Account:
                    AccountManager.getManager().receiveMessage(sc, msg);
                    break;
                // group request
                case Group:
                    GroupManager.getManager().receiveMessage(sc, msg);
                    break;
            }

        } catch (IOException e) {
            Logger.errorLogging(e);
        } catch (ClassNotFoundException cnfe) {
            Logger.errorLogging(cnfe);
        }
    }


    /**
     * 실제 select함수를 실행하는 부분
     */
    @Override
    public void run() {
        Logger.logging("Server Start");

        while(true) {

            try {
                Logger.logging("selecting!");
                selector.select();
                Iterator<SelectionKey> kit = selector.selectedKeys().iterator();
                while (kit.hasNext()) {
                    SelectionKey selectKey = kit.next();
                    if (selectKey.isAcceptable()) {
                        connectAcceptClient(selectKey);
                    } else if (selectKey.isReadable()) {
                        receiveMessage(selectKey);
                    }
                    kit.remove();
                }

            } catch(IOException ioe) {
                Logger.errorLogging(ioe);
            } catch (Exception e) {
                Logger.errorLogging(e);
            }
        }
    }
}

