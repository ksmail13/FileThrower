package dropbox.server;


import dropbox.server.Communicate.ServerManager;

public class Main {
    public static final String VERSION = "0.1.0";

    public static void main(String[] args) throws ClassNotFoundException {
        System.out.printf("Dropbox Server %s\n", VERSION);

        Class.forName("org.postgresql.Driver");
        ServerManager manager = ServerManager.getInstance();
        manager.startServer();
    }
}
