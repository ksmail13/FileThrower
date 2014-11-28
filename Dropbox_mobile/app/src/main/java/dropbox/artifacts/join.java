package dropbox.artifacts;

/**
 * Created by John on 2014-11-06.
        */

import org.json.JSONException;
import org.json.simple.JSONObject;

import java.io.IOException;

import dropbox.common.Message;
import dropbox.common.MessageType;

public class join {

    String Email, ID, PW, Confirm_PW;
    String data;
    String result;
    public join(String email, String id, String pw, String confirm_pw) {
        Email = email;
        ID = id;
        PW = pw;
        Confirm_PW = confirm_pw;

    }

    public int correct_info() throws IOException {

        if (PW.equals(Confirm_PW) == true) {
            if (send_info().equals("true") == true)//id 중복확인
            {

                return 3;
            } else return 2;
        } else return 1;

    }

    public String send_info() throws IOException {
        JSONObject obj = new JSONObject();

        obj.put("id", ID);
        obj.put("email", Email);
        obj.put("password", PW);
        obj.put(Message.SUBCATEGORY_KEY,"create");
        data = obj.toString();

        Thread join = new Thread( new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.msg = data;
                msg.messageType = MessageType.Account;
                MySocket ms = new MySocket();
                try {
                    ms.send_msg(msg);
                    result = ms.receive_msg();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        join.start();
        try {
            join.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        org.json.JSONObject order = null;
        try {
            order = new org.json.JSONObject(result);
            result = order.getString("result");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}