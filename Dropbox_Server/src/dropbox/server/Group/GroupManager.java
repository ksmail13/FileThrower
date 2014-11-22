package dropbox.server.Group;

import dropbox.common.Message;
import dropbox.server.Base.ManagerBase;

/**
 * Created by micky on 2014. 11. 22..
 */
public class GroupManager extends ManagerBase {
    public final static String DBNAME = "AccountManager";
    private static GroupManager manager = null;
    public static GroupManager getManager() {
        if(manager == null)
            manager = new GroupManager();

        return manager;
    }



    @Override
    public void receiveMessage(Message msg) {

    }
}
