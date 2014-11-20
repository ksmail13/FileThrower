package com.dropbox.john.dropbox_mobile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by John on 2014-11-06.
 */
public class file_management {



    public ArrayList load_list() {

        ArrayList<HashMap<String,String>> list =new ArrayList<HashMap<String, String>>();


        // 서버로 부터 파일 목록을 받음

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

        return list;
    }


    public void download(String group_id, String file_id)
    {


    }
    public void upload(String user_id, String group_id, String path)
    {


    }
    public boolean delete(String group_id,String file_id)
    {


        return false;
    }
    public void rename(String group_id, String file_id)
    {

    }
}


