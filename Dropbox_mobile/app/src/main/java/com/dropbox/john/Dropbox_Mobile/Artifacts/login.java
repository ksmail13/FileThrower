package com.dropbox.john.Dropbox_Mobile.Artifacts;

/**
 * Created by John on 2014-11-06.
 */

import android.app.Activity;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import common.MessageWrapper;


public class login extends Activity{
    private String ID;
    private String Password;
    private int result=2;
    public login(String id, String password)
    {
            ID = id;
            Password = password;
    }
    public int correct_user() throws IOException, ClassNotFoundException {

        org.json.simple.JSONObject obj = new org.json.simple.JSONObject();

        obj.put("ID",ID);
        obj.put("PW",Password);
        String data = obj.toString();


   //     MySocket ms = new MySocket();
   //     ms.send_msg(data);

     //   result = Integer.parseInt(ms.receive_msg());




        return result;
    }


}
