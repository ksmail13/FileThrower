package com.dropbox.john.dropbox_mobile;

/**
 * Created by John on 2014-11-12.
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import android.os.AsyncTask;


public class NetworkTask extends AsyncTask<Void, Void, Void> {

    String dstAddress;
    int dstPort;
    String response;

    NetworkTask(String addr, int port) {
        dstAddress = addr;
        dstPort = port;
    }

    @Override
    protected Void doInBackground(Void... arg0) {

        try {
            Socket socket = new Socket(dstAddress, dstPort);




            InputStream inputStream = socket.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream( 1024);
            byte[] buffer = new byte[1024];

            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }



            socket.close();
            response = byteArrayOutputStream.toString("UTF-8");

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        System.out.println(response);
        super.onPostExecute(result);
    }

}