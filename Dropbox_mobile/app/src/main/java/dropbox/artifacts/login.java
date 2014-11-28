package dropbox.artifacts;

/**
 * Created by John on 2014-11-06.
 */

import android.app.Activity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import dropbox.common.Message;
import dropbox.common.MessageType;


public class login extends Activity{
    private String ID;
    private String Password;
    private String result;
    String data;
    public login(String id, String password)
    {
            ID = id;
            Password = password;
    }
    public String correct_user() throws IOException, ClassNotFoundException {

        org.json.simple.JSONObject obj = new org.json.simple.JSONObject();

        obj.put("id",ID);
        obj.put("password",Password);
        obj.put("SubCategory","login");
         data = obj.toString();



        Thread login = new Thread( new Runnable() {
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
        login.start();
        try {
            login.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



        JSONObject order = null;
        try {
            order = new JSONObject(result);
            result = order.getString("result");
        } catch (JSONException e) {
            e.printStackTrace();
        }




        return result;
    }


}
