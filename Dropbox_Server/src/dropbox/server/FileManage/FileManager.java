package dropbox.server.FileManage;

import dropbox.common.Message;
import dropbox.server.Base.ManagerBase;

/**
 * Created by micky on 2014. 11. 21..
 */
public class FileManager extends ManagerBase {
    public final static String DBNAME = "AccountManager";

    private static FileManager manager = null;


    private FileManager() {

    }

    @Override
    public void receiveMessage(Message msg) {

    }
}
