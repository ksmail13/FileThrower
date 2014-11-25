package dropbox.server.Communicate;

import dropbox.server.Util.Logger;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.*;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by micky on 2014. 11. 8..
 * FTP기반의 파일 서버
 */
public class FileServer {
    private FtpServerFactory serverFactory;
    private FtpServer server;

    private static final String ID="test";
    private static final String PASSWORD = "test";

    private static final String MAINPATH = "/Users/micky/Documents/temp";

    private static final Ftplet ftpHandler = new Ftplet() {

        @Override
        public void init(FtpletContext ftpletContext)
                throws FtpException
        {
            //System.out.println("init");
            //System.out.println("Thread #" + Thread.currentThread().getId());
        }

        @Override
        public void destroy()
        {
            //System.out.println("destroy");
            //System.out.println("Thread #" + Thread.currentThread().getId());
        }

        @Override
        public FtpletResult beforeCommand(FtpSession session, FtpRequest request)
                throws FtpException, IOException
        {
            //Logger.logging("beforeCommand " + session.getUserArgument() + " : " + session.toString() + " | " + request.getArgument() + " : " + request.getCommand() + " : " + request.getRequestLine());
            //Logger.logging("Thread #" + Thread.currentThread().getId());

            //do something
            return FtpletResult.DEFAULT;//...or return accordingly
        }

        @Override
        public FtpletResult afterCommand(FtpSession session, FtpRequest request, FtpReply reply)
                throws FtpException, IOException
        {
            Logger.logging("afterCommand " + session.getUserArgument() + " : " + session.toString() + " | " + request.getArgument() + " : " + request.getCommand() + " : " + request.getRequestLine() + " | " + reply.getMessage() + " : " + reply.toString());
            Logger.logging("Thread #" + Thread.currentThread().getId());

            //do something
            return FtpletResult.DEFAULT;//...or return accordingly
        }

        @Override
        public FtpletResult onConnect(FtpSession session)
                throws FtpException, IOException
        {
            //System.out.println("onConnect " + session.getUserArgument() + " : " + session.toString());
            //System.out.println("Thread #" + Thread.currentThread().getId());

            //do something
            return FtpletResult.DEFAULT;//...or return accordingly
        }

        @Override
        public FtpletResult onDisconnect(FtpSession session)
                throws FtpException, IOException
        {
            //System.out.println("onDisconnect " + session.getUserArgument() + " : " + session.toString());
            //System.out.println("Thread #" + Thread.currentThread().getId());

            //do something
            return FtpletResult.DEFAULT;//...or return accordingly
        }
    };

    private static final PasswordEncryptor encryptor = new PasswordEncryptor() {
        @Override
        public String encrypt(String s) {
            return s;
        }

        @Override
        public boolean matches(String s, String s1) {
            return s.equals(s1);
        }
    };

    public FileServer(int port) {
        initServer(port);
    }

    private void initServer(int port) {
        serverFactory = new FtpServerFactory();
        ListenerFactory factory = new ListenerFactory();
        factory.setPort(port);
        serverFactory.addListener("default", factory.createListener());

        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        userManagerFactory.setPasswordEncryptor(encryptor);

        addFTPUser(userManagerFactory, ID, PASSWORD, MAINPATH);

        Map<String, Ftplet> m = new HashMap<String, Ftplet>();
        m.put("miaFtplet", ftpHandler);

        serverFactory.setFtplets(m);
        //Map<String, Ftplet> mappa = serverFactory.getFtplets();
        //System.out.println(mappa.size());
        //System.out.println("Thread #" + Thread.currentThread().getId());
        //System.out.println(mappa.toString());
        server = serverFactory.createServer();
    }

    private void addFTPUser(PropertiesUserManagerFactory userManagerFactory, String id, String password, String path) {
        //Let's add a user, since our myusers.properties files is empty on our first test run
        BaseUser user = new BaseUser();
        user.setName(id);
        user.setPassword(password);
        user.setHomeDirectory(path);

        List<Authority> authorities = new ArrayList<Authority>();
        authorities.add(new WritePermission());
        user.setAuthorities(authorities);
        UserManager um = userManagerFactory.createUserManager();
        try {
            um.save(user);
        } catch (FtpException e) {
            Logger.errorLogging(e);
        }

        serverFactory.setUserManager(um);

    }

    /**
     * ftp서버 시작
     */
    public void startServer() {
        try
        {
            //Your FTP server starts listening for incoming FTP-connections, using the configuration options previously set
            server.start();
        }
        catch (FtpException ex)
        {
            //Deal with exception as you need
            Logger.errorLogging(ex);
        }
    }

    public void stopServer() {
        server.stop();
    }

}
