package dropbox.common;


import java.io.*;

/**
 * Created by micky on 2014. 11. 8..
 */
public class MessageWrapper{
    public static final int MESSAGE_SIZE = 4096;

    public static byte[] messageToByteArray(Message msg) {
        byte[] newMessage=null;
        byte[] messageLength = null;
        byte[] temp;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(msg);
            temp = baos.toByteArray();
            // 4 is size of int
            newMessage = new byte[temp.length+4];
            messageLength = ByteConverter.intToByteArray(temp.length+4);
            System.arraycopy(messageLength,0,newMessage,0,messageLength.length);
            System.arraycopy(temp, 0, newMessage, messageLength.length, temp.length);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return newMessage;
    }

    public static Message byteArrayToMessage(byte[] buf) throws IOException, ClassNotFoundException {
        Message msg = null;

        ByteArrayInputStream bis = new ByteArrayInputStream(buf, 4, buf.length-4);
        ObjectInputStream ois = new ObjectInputStream(bis);

        msg = (Message)ois.readObject();

        return msg;
    }
}

