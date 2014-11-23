package dropbox.server.Base;

import dropbox.common.Message;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * Created by micky on 2014. 11. 22..
 */
public abstract class ManagerBase {

    public static ManagerBase getInstance() {
        return null;
    }

    public abstract void receiveMessage(SocketChannel sc, Message msg) throws IOException;

    protected ManagerBase() { }
}
