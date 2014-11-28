package dropbox.artifacts;

/**
 * Created by micky on 14. 11. 22.
 */

import java.io.*;


import org.apache.commons.net.ftp.*;


public class MyFtpClient {
    static String server = "10.0.26.191";
    static int port = 8081;
    static String id = "test";
    static String password = "test";
    FTPClient ftpClient= new FTPClient();

    public MyFtpClient() {



    }


    // 계정과 패스워드로 로그인
    public void login(String user, String password) {
        try {

            ftpClient.login(user, password);
            System.out.println("ftp login");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

    // 서버로부터 로그아웃
    public void logout() {
        try {
            ftpClient.logout();
            System.out.println("ftp logout");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }


    // 서버로 연결
    public void connect(String group_id) {

        try {

            ftpClient.setControlEncoding("UTF-8");
            ftpClient.connect(server, port);
            //ftpClient.log



            int reply;
            // 연결 시도후, 성공했는지 응답 코드 확인
            reply = ftpClient.getReplyCode();
            ftpClient.login(id, password);



            ftpClient.makeDirectory(group_id);
            this.cd(group_id);

            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                System.out.println("서버로부터 연결을 거부당했습니다");

            }
            System.out.println("ftp connect succ");
        } catch (IOException ioe) {
            ioe.printStackTrace();
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException f) {
                    //
                }
            }
            System.out.println("서버에 연결할 수 없습니다");

        }
    }


    // FTP의 ls 명령, 모든 파일 리스트를 가져온다
    public FTPFile[] list(String group) {
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


    public File get(String source,String file_id) {

        File downloadFile = new File("/storage/emulated/0/Dropbox/"+file_id);	// 단말기에 저장될 위치
        FileOutputStream fos = null;

        File file = new File("/storage/emulated/0/Dropbox");
        if( !file.exists() )  // 원하는 경로에 폴더가 있는지 확인
            file.mkdirs();
        try{

            fos = new FileOutputStream(downloadFile);
            if(ftpClient.retrieveFile(source, fos)==true)	// ftp에서 다운받을 파일명, fos = 로컬의 저장할 위치
                System.out.println("download success"+file_id);
            else System.out.println("download fail");
        }
        catch(IOException e){
            e.printStackTrace();
        }

        return null;

    }
    public void delete(String group,String file) throws IOException {
        ftpClient.deleteFile(file);

    }

    public void rename(String group,String file, String new_name) throws IOException {

        String ex = file.substring(file.lastIndexOf(".") + 1);

        ftpClient.rename(file,new_name+"."+ex);

    }
    public void upload(String group, String file,String file_path) throws IOException {

        File path = new File(file_path); // 업로드 할 파일이 있는 경로(예제는 sd카드 사진 폴더)
        ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
        if (path.isFile()) {
            FileInputStream ifile = new FileInputStream(path);

            ftpClient.storeFile(file,ifile);

        }


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
    public void disconnect() {
        try {
            ftpClient.disconnect();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}