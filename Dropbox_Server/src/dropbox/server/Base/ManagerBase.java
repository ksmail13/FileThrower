package dropbox.server.Base;

import dropbox.common.Message;

/**
 * Created by micky on 2014. 11. 22..
 */
public abstract class ManagerBase {

    public static ManagerBase getInstance() {
        return null;
    }

    public abstract void receiveMessage(Message msg);

    protected ManagerBase() { }
}
