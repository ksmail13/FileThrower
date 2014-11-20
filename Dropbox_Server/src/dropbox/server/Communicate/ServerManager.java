package dropbox.server.Communicate;

/**
 * Created by micky on 2014. 11. 8..
 * 서버 인스턴스를 생성하고 관리하는 클래스
 */
public class ServerManager {
    public static final String DEFAULT_IP = "127.0.0.1";
    public static final int DEFAULT_PORT = 8080;

    private static ServerManager managerInstance;

    public static ServerManager getInstance() {
        if(managerInstance == null) {
            managerInstance = new ServerManager();
        }

        return managerInstance;
    }

    private RelayServer relayServer;
    private FileServer fileServer;

    private ServerManager() {

    }

    public void startServer() {
        relayServer = new RelayServer(DEFAULT_IP, DEFAULT_PORT);
        relayServer.startServer();
    }

}
