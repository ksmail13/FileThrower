package dropbox.artifacts;

import android.os.AsyncTask;

import dropbox.common.Message;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import dropbox.common.MessageWrapper;

/**
 * Created by micky on 14. 11. 22.
 */
public class MySocket  {

    private static Socket socket=null;


    public MySocket() {
        if (socket == null) {
            try {
                socket = new Socket("10.0.26.191", 8080);
                System.out.println("succ");
            } catch (IOException e) {
                System.out.println("fail");
                e.printStackTrace();
            }
        }
        System.out.println("socket");
    }

    public void send_msg(Message msg) throws IOException {

        byte[] buf;



        buf = MessageWrapper.messageToByteArray(msg);


        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(buf);


        try {
            System.out.println("send message" + MessageWrapper.byteArrayToMessage(buf).msg);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
    public String receive_msg() throws IOException {

        System.out.println("connect :" + socket.isConnected());
        String msg=null;

        byte[] bytes= new byte[4096];

        if(socket.getInputStream().read(bytes,0,4096)!=-1)
        {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            msg = parse(buffer);
            if(msg.contains("upload") || msg.contains("upcomplete") || msg.contains("sync"))
            {
                System.out.println("!-receive pass message" +msg);
                return receive_msg();

            }
        }

        System.out.println("receive message" +msg);

        return msg;
    }


    private String parse(ByteBuffer buffer) {
        try {

            int offset = buffer.arrayOffset();
            byte[] buf = buffer.array();

            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buf,4, buf.length));
            Message msg = (Message)ois.readObject();



            switch (msg.messageType) {
                // file request
                case Account:

                    break;

            }
            return msg.msg;

        } catch (IOException e) {

        } catch (ClassNotFoundException cnfe) {

        }
        return null;
    }

}
