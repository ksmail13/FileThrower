package com.dropbox.john.Dropbox_Mobile.Artifacts;

import org.apache.commons.net.ftp.FTPFile;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by John on 2014-11-06.
 */
public class file_management {


    String up_path;
    String in_group;
    String in_file;
    String name;

    ArrayList<HashMap<String,String>> list =new ArrayList<HashMap<String, String>>();

    public ArrayList load_list(String group_id) throws InterruptedException {

        in_group= group_id;
        // 서버로 부터 파일 목록을 받음
        Thread list_Thread = new Thread(){
            @Override
            public void run() {
                MyFtpClient ftp = new MyFtpClient();

                ftp.connect();
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
        /*
        String data = "[{\"name\":\"a\",\"size\":\"1\",\"date\":\"2\",\"uploader\":\"3\"}, {\"name\":\"b\",\"size\":\"1\",\"date\":\"2\",\"uploader\":\"3\"}]";


        try{

            JSONArray ja = new JSONArray(data);
            for (int i = 0; i < ja.length(); i++){
                JSONObject order = ja.getJSONObject(i);
                HashMap<String, String> item =   new HashMap<String, String>();
                item.put("name",order.getString("name"));
                item.put("date",order.getString("date"));
                item.put("size",order.getString("size"));
                item.put("uploader",order.getString("uploader"));

                list.add(item);
            }
        }
        catch (JSONException e){ ;}
*/

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
                ftp.connect();

                ftp.get("home\\"+in_group+"\\"+in_file,in_file);

                ftp.logout();
                ftp.disconnect();
            }
        }).start();

    }




    public void upload(String user_id, String group_id, String path) throws IOException{
        org.json.simple.JSONObject obj = new org.json.simple.JSONObject();
        obj.put("Group_ID",group_id);
        obj.put("User_ID",user_id);
        obj.put("Path",path);

        up_path= path;
        String data = obj.toString();


        new Thread(new Runnable() {
            @Override
            public void run() {
                MyFtpClient ftp = new MyFtpClient();
                ftp.connect();
                try {
                    ftp.upload(up_path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ftp.logout();
                ftp.disconnect();
            }
        }).start();

    }
    public void delete(String group_id,String file_id) throws IOException {
        org.json.simple.JSONObject obj = new org.json.simple.JSONObject();
        obj.put("Group_ID",group_id);
        obj.put("File_ID",file_id);

        String data = obj.toString();
        in_group=group_id;
        in_file=file_id;

        new Thread(new Runnable() {
            @Override
            public void run() {
                MyFtpClient ftp = new MyFtpClient();
                ftp.connect();
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
                ftp.connect();
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


