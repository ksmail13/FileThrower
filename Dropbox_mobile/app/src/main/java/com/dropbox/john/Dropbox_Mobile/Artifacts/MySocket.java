package com.dropbox.john.Dropbox_Mobile.Artifacts;

import common.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

import common.MessageType;
import common.MessageWrapper;

/**
 * Created by micky on 14. 11. 22.
 */
public class MySocket {

    private static Socket socket=null;

    public MySocket() throws IOException {
        if(socket==null)
            socket = new Socket("192.168.0.4",8080);
    }
    public void send_msg(String str_msg) throws IOException {

        byte[] buf;
        Message msg = new Message();
        msg.msg = str_msg;
        msg.messageType = MessageType.FileSync;


        buf = MessageWrapper.messageToByteArray(msg);

        OutputStream outputStream = socket.getOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        printStream.print(buf);

        printStream.close();

        System.out.println("Succc");

    }
    public String receive_msg() throws IOException {

        ByteArrayOutputStream byteArrayOutputStream =
                new ByteArrayOutputStream(4096);
        byte[] buffer = new byte[4096];

        int bytesRead;
        InputStream inputStream = socket.getInputStream();

    /*
     * notice:
     * inputStream.read() will block if no data return
     */
        while ((bytesRead = inputStream.read(buffer)) != -1){
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }
        System.out.println(byteArrayOutputStream.toString());

        return byteArrayOutputStream.toString();
    }
}
