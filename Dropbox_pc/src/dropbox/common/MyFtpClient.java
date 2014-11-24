package dropbox.common;

import java.io.*;

import com.oroinc.net.ftp.*;
import com.oroinc.net.*;

public class MyFtpClient {
    static String server = "192.168.0.4";
    static int port = 8081;
    static String id = "test";
    static String password = "test";
    FTPClient ftpClient;

    public MyFtpClient(String server, int port, String id, String password) {
        this.server = server;
        this.port = port;
        ftpClient = new FTPClient();
    }


    // 계정과 패스워드로 로그인
    public boolean login(String user, String password) {
        try {
            this.connect();
            System.out.println("FTP Login");
            return ftpClient.login(user, password);
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return false;
    }

    // 서버로부터 로그아웃
    private boolean logout() {
        try {
            return ftpClient.logout();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return false;
    }

    // 서버로 연결
    public void connect() {
        try {
            ftpClient.connect(server, port);
            int reply;
            // 연결 시도후, 성공했는지 응답 코드 확인
            reply = ftpClient.getReplyCode();
            System.out.println(reply);
            if(!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                System.err.println("서버로부터 연결을 거부당했습니다");
                System.exit(1);
            }
        }
        catch (IOException ioe) {
            if(ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch(IOException f) {
                    //
                }
            }
            System.err.println("서버에 연결할 수 없습니다");
            System.exit(1);
        }
    }

    // FTP의 ls 명령, 모든 파일 리스트를 가져온다
    public FTPFile[] list() {
        FTPFile[] files = null;
        try {
            files = this.ftpClient.listFiles();
            return files;
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }

    // 파일을 전송 받는다
    public File get(String dir, String groupName, String fileName) {
        OutputStream output = null;
        String source = dir+"/"+groupName+"/"+fileName;
        String target = "/"+groupName+"/"+fileName;
        try {
            File local = new File(source);
            output = new FileOutputStream(local);
        }
        catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        }
        
        File file = new File(target);
        try {
            if (ftpClient.retrieveFile(target, output)) {
                return file;
            }
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }
    
    //파일 전송 
    public void send(String dir, String groupName, String fileName) throws IOException{
    	String source = dir+"/"+fileName;
    	String target = "/"+groupName+"/"+fileName;
    	
    	System.out.println("Transmit Ori : " + source);
    	System.out.println("To server : " + target);
    	
    	//File put_file = new File(source); // 저장시켰던 파일을 서버로 전송
        //InputStream inputStream = new FileInputStream(put_file);
    	FileInputStream fis = new FileInputStream(source);
    	
    	try{
    		if(!ftpClient.storeFile(target, fis)){
    			System.out.println("Excep, makedir");
        		ftpClient.makeDirectory("/"+groupName);
        		ftpClient.storeFile(target, fis);
    		}
    	}
    	catch(com.oroinc.io.CopyStreamException cse){
    		System.out.println("Excep, makedir");
    		ftpClient.makeDirectory("/"+groupName);
    		ftpClient.storeFile(target, fis);
    	}
    	
        fis.close();
        ftpClient.disconnect();
    }

    // 서버 디렉토리 이동
    public void cd(String path) {
        try {
            ftpClient.changeWorkingDirectory(path);
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    // 서버로부터 연결을 닫는다
    private void disconnect() {
        try {
            ftpClient.disconnect();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}