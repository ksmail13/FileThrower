package dropbox.server.Communicate;

import dropbox.server.Util.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * Created by micky on 2014. 11. 8..
 * 중계 서버를 구동하는 인스턴스
 * 클라이언트로부터 메시지를 받아 파싱한 후 그에 대응하는 작업을 진행한다.
 * 중계 서버 인스턴스는 하나의 프로세스에 하나만 존재한다.
 */
public class RelayServer {
    // static
    private static RelayServer instance = null;

    /**
     * 아이피와 포트 정보를 기반으로 서버 인스턴스를 가져온다.
     * @param address 아이피 주소
     * @param port 포트 번호
     * @return 서버 인스턴스
     */
    public static RelayServer getInstance(String address, int port) {
        getInstance(new InetSocketAddress(address, port));

        return instance;
    }

    /**
     * 소켓 address bind클래스를 사용해 객체를 초기화한다.
     * @param socketAddr 주소 데이터
     * @return 서버 인스턴스
     */
    public static RelayServer getInstance(InetSocketAddress socketAddr) {
        if(instance == null) {
            instance = new RelayServer(socketAddr);
        }

        return instance;
    }

    // static end

    private Selector selector = null;
    private ServerSocketChannel serverSockChennel = null;
    private ServerSocket serverSocket = null;

    protected RelayServer(InetSocketAddress serverSocketAddress) {
        initServer(serverSocketAddress);
    }

    /**
     * 서버 내부 인스턴스를 초기화한다.
     * @param serverSocketAddress 서버의 주소정보
     */
    private void initServer(InetSocketAddress serverSocketAddress) {
        try {
            selector = Selector.open();
            serverSockChennel = ServerSocketChannel.open();
            serverSockChennel.configureBlocking(false);
            serverSocket = serverSockChennel.socket();

            serverSocket.bind(serverSocketAddress);

            serverSockChennel.register(selector, SelectionKey.OP_ACCEPT);
        }
        catch(IOException ioe) {
            Logger.errorLogging("IOException", ioe);
        }
    }

    public void startServer() {
        Logger.logging("Server Start");

        try {
            while(true) {
                selector.select();
                Iterator<SelectionKey> kit = selector.selectedKeys().iterator();
                while (kit.hasNext()) {
                    SelectionKey selectKey = kit.next();
                    if(selectKey.isAcceptable()) {
                        connectAcceptClient(selectKey);
                    }
                    else if(selectKey.isReadable()){

                    }
                    kit.remove();
                }
            }
        }
        catch(IOException ioe) {
            Logger.errorLogging("", ioe);
        }

    }

    /**
     * 클라이언트의 연결을 받는다.
     * @param key
     * @return
     */
    protected boolean connectAcceptClient(SelectionKey key) {
        ServerSocketChannel serverChannel = (ServerSocketChannel)key.channel();

        try {
            SocketChannel sc = serverChannel.accept();
            registerChannel(sc, SelectionKey.OP_READ);
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
     * 셀렉터에 소켓을 등록한다.
     * @param sc 등록할 소켓 체널
     * @param ops 리스닝할 이벤트
     * @throws ClosedChannelException
     * @throws IOException
     */
    protected void registerChannel(SocketChannel sc, int ops)
            throws ClosedChannelException, IOException {
        sc.configureBlocking(false);
        sc.register(selector, ops);
    }
}
