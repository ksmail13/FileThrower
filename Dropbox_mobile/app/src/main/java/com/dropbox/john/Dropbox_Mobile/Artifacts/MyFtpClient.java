package com.dropbox.john.Dropbox_Mobile.Artifacts;

/**
 * Created by micky on 14. 11. 22.
 */

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.SocketAddress;


import org.apache.commons.net.ftp.*;


public class MyFtpClient {
    static String server = "14.40.76.156";
    static int port = 21;
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
    public void connect() {

        try {

            ftpClient.setControlEncoding("UTF-8");
            ftpClient.connect(server, port);
            //ftpClient.log

            int reply;
            // 연결 시도후, 성공했는지 응답 코드 확인
            reply = ftpClient.getReplyCode();
            ftpClient.login(id, password);
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

            files = this.ftpClient.listFiles("home\\"+group);

            return files;
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }

    // 파일을 전송 받는다


    public File get(String source,String file_id) {
        File downloadFile = new File("/storage/emulated/0/Download/"+file_id);	// 단말기에 저장될 위치
        FileOutputStream fos = null;
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
        ftpClient.deleteFile("home\\"+group+"\\"+file);

    }

    public void rename(String group,String file, String new_name) throws IOException {

        String ex = file.substring(file.lastIndexOf(".") + 1);

        ftpClient.rename("home\\"+group+"\\"+file,"home\\"+group+"\\"+new_name+"."+ex);

    }
    public void upload(String file_path) throws IOException {

        File path = new File(file_path); // 업로드 할 파일이 있는 경로(예제는 sd카드 사진 폴더)

        if (path.isFile()) {
            FileInputStream ifile = new FileInputStream(path);

            ftpClient.appendFile(path.getName(), ifile); // ftp 해당 파일이 없다면 새로쓰기
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