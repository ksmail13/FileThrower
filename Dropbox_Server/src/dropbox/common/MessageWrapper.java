package dropbox.common;

import java.io.Serializable;

/**
 * Created by micky on 2014. 11. 8..
 */
public class MessageWrapper implements Serializable{
    public static final int MESSAGE_SIZE = 4096;
    public MessageType messageType;
    public int size;
    public byte[] buf;
}

