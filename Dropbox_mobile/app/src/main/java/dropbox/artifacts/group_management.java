package dropbox.artifacts;

import android.app.Activity;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import dropbox.common.*;
/**
 * Created by John on 2014-11-06.
 */
public class group_management extends Activity{
    String data;
    String result;
    public boolean make_group(String user_id, String group_id,String comment)
    {
        org.json.simple.JSONObject obj = new org.json.simple.JSONObject();
        obj.put("name",group_id);
        obj.put("comment",comment);
        obj.put(Message.SUBCATEGORY_KEY,"create");

        data = obj.toString();



        Thread group_list= new Thread( new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.msg = data;
                msg.messageType = MessageType.Group;
                MySocket ms = new MySocket();
                try {
                    ms.send_msg(msg);
                    result = ms.receive_msg();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        group_list.start();
        try {
            group_list.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        System.out.println(result);

        JSONObject order = null;
        try {
            order = new JSONObject(result);
            result = order.getString("result");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result.equals("true");
    }







    public boolean invite_group(String user_id, String group_id, String add_id)
    {
        org.json.simple.JSONObject obj = new org.json.simple.JSONObject();
        obj.put("groupid",group_id);
        obj.put("inviteid",add_id);
        obj.put(Message.SUBCATEGORY_KEY,"addmember");

        data = obj.toString();


        Thread group_invite= new Thread( new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.msg = data;
                msg.messageType = MessageType.Group;
                MySocket ms = new MySocket();
                try {
                    ms.send_msg(msg);
                    result = ms.receive_msg();


                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        group_invite.start();
        try {
            group_invite.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result.contains("true");

    }
    public void exit_group(String user_id, String group_id)
    {
        org.json.simple.JSONObject obj = new org.json.simple.JSONObject();
        obj.put("groupid",group_id);

        obj.put(Message.SUBCATEGORY_KEY,"exitgroup");

        data = obj.toString();


        Thread group_exit= new Thread( new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.msg = data;
                msg.messageType = MessageType.Group;
                MySocket ms = new MySocket();
                try {
                    ms.send_msg(msg);
                    result = ms.receive_msg();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        group_exit.start();
        try {
            group_exit.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



    }
    public void enter_group(String group_id)
    {


    }
    public void delete_group(String user_id,String group_id)
    {
        org.json.simple.JSONObject obj = new org.json.simple.JSONObject();
        obj.put("groupid",group_id);

        obj.put(Message.SUBCATEGORY_KEY,"delete");

        data = obj.toString();


        Thread group_delete= new Thread( new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.msg = data;
                msg.messageType = MessageType.Group;
                MySocket ms = new MySocket();
                try {
                    ms.send_msg(msg);
                    result = ms.receive_msg();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        group_delete.start();
        try {
            group_delete.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }


    public ArrayList load_list() {

        ArrayList<HashMap<String,String>> list =new ArrayList<HashMap<String, String>>();


        org.json.simple.JSONObject obj = new org.json.simple.JSONObject();

        obj.put(Message.SUBCATEGORY_KEY,"grouplist");

        data = obj.toString();

        Thread group_list= new Thread( new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.msg = data;
                msg.messageType = MessageType.Group;
                MySocket ms = new MySocket();
                try {
                    ms.send_msg(msg);
                    result = ms.receive_msg();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        group_list.start();
        try {
            group_list.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String listdata=null;

        JSONObject order = null;
        try {
            order = new JSONObject(result);
            listdata = order.getString("grouplist");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // 서버로 부터 그룹 목록을 받음

 //       String listdata = "[{\"Group_ID\":\"a\",\"Group_Master\":\"1\",\"Comment\":\"123\"},{\"Group_ID\":\"b\",\"Group_Master\":\"2\",\"Comment\":\"456\"},{\"Group_ID\":\"c\",\"Group_Master\":\"3\",\"Comment\":\"aadsfe\"}]";
        try{

            JSONArray ja = new JSONArray(listdata);
            for (int i = 0; i < ja.length(); i++){
                JSONObject order2 = ja.getJSONObject(i);
                HashMap<String, String> item =   new HashMap<String, String>();
                item.put("Group_ID",order2.getString("groupid"));
                item.put("Group_Name",order2.getString("groupname"));
                item.put("Group_Master",order2.getString("mastername"));
                item.put("Comment",order2.getString("comment"));

                list.add(item);
            }
        }
        catch (JSONException e){ ;}

        return list;
    }

    public ArrayList load_member(String groupid) {


        org.json.simple.JSONObject obj = new org.json.simple.JSONObject();

        obj.put(Message.SUBCATEGORY_KEY,"memberlist");
        obj.put("groupid",groupid);

        data = obj.toString();

        Thread mem_list= new Thread( new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.msg = data;
                msg.messageType = MessageType.Group;
                MySocket ms = new MySocket();
                try {
                    ms.send_msg(msg);
                    result = ms.receive_msg();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        mem_list.start();
        try {
            mem_list.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String listdata=null;
        JSONObject order = null;
        try {
            order = new JSONObject(result);
            listdata = order.getString("grouplist");
        } catch (JSONException e) {
            e.printStackTrace();
        }



        ArrayList<String> list = new ArrayList<String>();
     //   String data = "[{\"Member_ID\":\"123\"},{\"Member_ID\":\"345\"},{\"Member_ID\":\"123\"},{\"Member_ID\":\"345\"},{\"Member_ID\":\"123\"},{\"Member_ID\":\"345\"},{\"Member_ID\":\"123\"},{\"Member_ID\":\"345\"},{\"Member_ID\":\"afe\"},{\"Member_ID\":\"asef\"},{\"Member_ID\":\"qwef\"},{\"Member_ID\":\"fqewf\"},{\"Member_ID\":\"qef\"}]";
        try{


            JSONArray ja = new JSONArray(listdata);
            for (int i = 0; i < ja.length(); i++){
                JSONObject order2 = ja.getJSONObject(i);

                String item = order2.getString("id");


                list.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }
}
