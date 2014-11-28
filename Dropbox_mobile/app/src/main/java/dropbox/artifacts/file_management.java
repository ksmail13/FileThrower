package dropbox.artifacts;

import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import dropbox.common.Message;
import dropbox.common.MessageType;

/**
 * Created by John on 2014-11-06.
 */
public class file_management {


    String up_path;
    String in_group;
    String in_file;
    String name;
    String data;


    ArrayList<HashMap<String,String>> list =new ArrayList<HashMap<String, String>>();

    public ArrayList load_list(String group_id) throws InterruptedException {

        in_group= group_id;
        // 서버로 부터 파일 목록을 받음
        Thread list_Thread = new Thread(){
            @Override
            public void run() {
                MyFtpClient ftp = new MyFtpClient();

                ftp.connect(in_group);
                FTPFile[] files;

                files = ftp.list(in_group);
                list.clear();
                for(int i=0;i<files.length;i++)
                {

                    HashMap<String, String> item =   new HashMap<String, String>();
                    item.put("name",files[i].getName());
                    item.put("size",Long.toString(files[i].getSize())+"byte");
                    list.add(item);


                }

                ftp.logout();
                ftp.disconnect();
            }
        };

        list_Thread.start();
        list_Thread.join();


        return list;
    }


    public void download(String group_id, final String file_id)
    {
        org.json.simple.JSONObject obj = new org.json.simple.JSONObject();
        obj.put("Group_ID",group_id);
        obj.put("File_ID",file_id);

        String data = obj.toString();

        in_group=group_id;
        in_file = file_id;
        new Thread(new Runnable() {
            @Override
            public void run() {
                MyFtpClient ftp = new MyFtpClient();
                ftp.connect(in_group);

                ftp.get(in_file,in_file);

                ftp.logout();
                ftp.disconnect();
            }
        }).start();

    }




    public void upload(String user_id, String group_id, String file_id,long file_size,String path) throws IOException{


        up_path= path;
        in_group=group_id;
        in_file=file_id;



        org.json.simple.JSONObject obj = new org.json.simple.JSONObject();
        obj.put("filesize",file_size);
        obj.put("filename",file_id);
        obj.put("groupid",group_id);

        obj.put(Message.SUBCATEGORY_KEY,"upload");

        data = obj.toString();


        Thread upload= new Thread( new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.msg = data;
                msg.messageType = MessageType.File;
                MySocket ms = new MySocket();
                try {
                    ms.send_msg(msg);
                    ms.receive_msg();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        upload.start();
        /*
        try {
            upload.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
*/



        new Thread(new Runnable() {
            @Override
            public void run() {
                MyFtpClient ftp = new MyFtpClient();
                ftp.connect(in_group);
                try {
                    ftp.upload(in_group,in_file,up_path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ftp.logout();
                ftp.disconnect();

                org.json.simple.JSONObject obj2 = new org.json.simple.JSONObject();

                obj2.put("filename",in_file);
                obj2.put("groupid",in_group);

                obj2.put(Message.SUBCATEGORY_KEY,"upcomplete");

                data = obj2.toString();

                Message msg = new Message();
                msg.msg = data;
                msg.messageType = MessageType.File;
                MySocket ms = new MySocket();
                try {
                    ms.send_msg(msg);
                    ms.receive_msg();
                    ms.receive_msg();

                } catch (IOException e) {
                    e.printStackTrace();
                }



            }
        }).start();

    }
    public void delete(String group_id,String file_id) throws IOException {

        in_group=group_id;
        in_file=file_id;

        org.json.simple.JSONObject obj = new org.json.simple.JSONObject();
        obj.put("filesize",file_id);
        obj.put("groupid",group_id);

        obj.put(Message.SUBCATEGORY_KEY,"delete");

        data = obj.toString();


        Thread delete= new Thread( new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.msg = data;
                msg.messageType = MessageType.File;
                MySocket ms = new MySocket();
                try {
                    ms.send_msg(msg);
                    //      ms.receive_msg();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        delete.start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                MyFtpClient ftp = new MyFtpClient();
                ftp.connect(in_group);
                try {
                    ftp.delete(in_group,in_file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ftp.logout();
                ftp.disconnect();
            }
        }).start();




    }

    public void rename(String new_name, String group_id, String file_id)
    {
        org.json.simple.JSONObject obj = new org.json.simple.JSONObject();
        obj.put("Group_ID",group_id);
        obj.put("File_ID",file_id);
        obj.put("New_name",new_name);

        String data = obj.toString();

        name = new_name;
        in_group=group_id;
        in_file=file_id;

        new Thread(new Runnable() {
            @Override
            public void run() {
                MyFtpClient ftp = new MyFtpClient();
                ftp.connect(in_group);
                try {
                    ftp.rename(in_group,in_file,name);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ftp.logout();
                ftp.disconnect();
            }
        }).start();
    }
}


