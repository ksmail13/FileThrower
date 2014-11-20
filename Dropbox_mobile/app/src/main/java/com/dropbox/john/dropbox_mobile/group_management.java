package com.dropbox.john.dropbox_mobile;

import android.app.Activity;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
/**
 * Created by John on 2014-11-06.
 */
public class group_management extends Activity{
    public void make_group(String user_id, String group_id)
    {
        org.json.simple.JSONObject obj = new org.json.simple.JSONObject();
        obj.put("Group_ID",group_id);
        obj.put("User_ID",user_id);
        String data = obj.toString();


    }
    public void invite_group(String user_id, String group_id, String add_id)
    {
        org.json.simple.JSONObject obj = new org.json.simple.JSONObject();
        obj.put("Group_ID",group_id);
        obj.put("Add_ID",add_id);
        String data = obj.toString();

    }
    public void exit_group(String user_id, String group_id)
    {
        org.json.simple.JSONObject obj = new org.json.simple.JSONObject();
        obj.put("Group_ID",group_id);
        obj.put("User_ID",user_id);
        String data = obj.toString();

    }
    public void enter_group(String group_id)
    {
        org.json.simple.JSONObject obj = new org.json.simple.JSONObject();
        obj.put("Group_ID",group_id);
        String data = obj.toString();

    }
    public void delete_group(String user_id,String group_id)
    {
        org.json.simple.JSONObject obj = new org.json.simple.JSONObject();
        obj.put("Group_ID",group_id);
        String data = obj.toString();

    }


    public ArrayList load_list() {

        ArrayList<HashMap<String,String>> list =new ArrayList<HashMap<String, String>>();


        // 서버로 부터 그룹 목록을 받음

        String data = "[{\"Group_ID\":\"a\",\"Group_Master\":\"1\"},{\"Group_ID\":\"b\",\"Group_Master\":\"2\"},{\"Group_ID\":\"c\",\"Group_Master\":\"3\"}]";
        try{

            JSONArray ja = new JSONArray(data);
            for (int i = 0; i < ja.length(); i++){
                JSONObject order = ja.getJSONObject(i);
                HashMap<String, String> item =   new HashMap<String, String>();
                item.put("Group_ID",order.getString("Group_ID"));
                item.put("Group_Master",order.getString("Group_Master"));
                System.out.println(item);
                list.add(item);
            }
        }
        catch (JSONException e){ ;}

        return list;
    }

    public ArrayList load_member() {
        ArrayList<String> list = new ArrayList<String>();
        String data = "[{\"Member_ID\":\"123\"},{\"Member_ID\":\"345\"},{\"Member_ID\":\"afe\"},{\"Member_ID\":\"asef\"},{\"Member_ID\":\"qwef\"},{\"Member_ID\":\"fqewf\"},{\"Member_ID\":\"qef\"}]";
        try{

            JSONArray ja = new JSONArray(data);
            for (int i = 0; i < ja.length(); i++){
                JSONObject order = ja.getJSONObject(i);

                String item = order.getString("Member_ID");

                System.out.println(item);
                list.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }
}
