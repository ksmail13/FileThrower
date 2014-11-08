package dropbox.common;

import java.io.Serializable;

/**
 * Created by micky on 2014. 11. 8..
 */
public class MessageWrapper implements Serializable{
    MessageType messageType;
    int size;
    byte[] buf;
}

