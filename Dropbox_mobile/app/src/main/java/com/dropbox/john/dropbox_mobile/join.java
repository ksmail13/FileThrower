package com.dropbox.john.dropbox_mobile;

/**
 * Created by John on 2014-11-06.
        */

import org.json.simple.*;

public class join {

    String Name,ID,PW,Confirm_PW;
    public join (String name, String id, String pw, String confirm_pw)
    {
        Name = name;
        ID = id;
        PW = pw;
        Confirm_PW = confirm_pw;

    }
    public int correct_info()
    {
        System.out.println(PW+" "+Confirm_PW);
        if(PW.equals(Confirm_PW)==true)
        {
            if(duplicate_id())//id 중복확인
            {
                send_info();
                return 3;
            }
            else return 2;
        }
        else return 1;

    }

    public boolean duplicate_id()
    {
        JSONObject obj = new JSONObject();
        obj.put("ID",ID);
        String data = obj.toString();



        System.out.println(data);

        return true;
    }

    public void send_info()
    {
        JSONObject obj = new JSONObject();
        obj.put("ID",ID);
        obj.put("Name",Name);
        obj.put("PW",PW);
        String data = obj.toString();

        System.out.println(data);
    }
}
