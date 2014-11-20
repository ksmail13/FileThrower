package com.dropbox.john.dropbox_mobile;

/**
 * Created by John on 2014-11-06.
 */
public class login {
    private String ID;
    private String Password;
    public login(String id, String password)
    {
            ID = id;
            Password = password;
    }
    public int correct_user()
    {

        org.json.simple.JSONObject obj = new org.json.simple.JSONObject();
        obj.put("ID",ID);
        obj.put("PW",Password);
        String data = obj.toString();


        return 1;
    }


}
