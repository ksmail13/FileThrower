package com.dropbox.john.Dropbox_Mobile.Artifacts;

/**
 * Created by John on 2014-11-06.
        */

import android.os.AsyncTask;

import org.json.*;
import org.json.simple.*;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class join {

    String Email, ID, PW, Confirm_PW;

    public join(String email, String id, String pw, String confirm_pw) {
        Email = email;
        ID = id;
        PW = pw;
        Confirm_PW = confirm_pw;

    }

    public int correct_info() throws IOException {

        if (PW.equals(Confirm_PW) == true) {
            if (duplicate_id() == true)//id 중복확인
            {
                send_info();
                return 3;
            } else return 2;
        } else return 1;

    }

    public boolean duplicate_id() {
        JSONObject obj = new JSONObject();

        obj.put("ID", ID);
        String data = obj.toString();


        return true;
    }

    public void send_info() throws IOException {
        JSONObject obj = new JSONObject();

        obj.put("ID", ID);
        obj.put("Email", Email);
        obj.put("PW", PW);
        String data = obj.toString();


        MySocket ms = new MySocket();
        ms.send_msg(data);

    }



}