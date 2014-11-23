package dropbox.server.FileManage;

import dropbox.common.Message;
import dropbox.server.Base.ManagerBase;
import org.json.simple.JSONObject;

import java.nio.channels.SocketChannel;

/**
 * Created by micky on 2014. 11. 21..
 */
public class FileManager extends ManagerBase {
    public final static String DBNAME = "AccountManager";

    private static FileManager manager = null;


    @Override
    public JSONObject messageHandling(SocketChannel sc, JSONObject parsedObject) {
        return null;
    }

    private FileManager() {

    }

}
