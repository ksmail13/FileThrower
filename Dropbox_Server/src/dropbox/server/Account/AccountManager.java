package dropbox.server.Account;

import dropbox.common.Message;
import dropbox.server.Base.ManagerBase;
import dropbox.server.Util.DatabaseConnector;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.nio.channels.SocketChannel;
import java.util.Map;

/**
 * Created by micky on 2014. 11. 21..
 */
public class AccountManager extends ManagerBase {
    public final static String DBNAME = "AccountManager";
    public final static String SESSION = "session";
    private static AccountManager managerInstance = null;

    public static AccountManager getManager() {
        if(managerInstance == null) {
            managerInstance = new AccountManager();
        }
        return managerInstance;
    }

    protected Map<SocketChannel, AccountInfo> session;

    private AccountManager() {
        DB db = DBMaker.newMemoryDB().transactionDisable().closeOnJvmShutdown().make();
        session = db.getTreeMap(SESSION);
    }

    public boolean login(SocketChannel sc, String id, String password) {
        DatabaseConnector dbConn = DatabaseConnector.getConnector();
        AccountInfo result = dbConn.loginCheck(id, password);
        if(result != null) {
            session.put(sc, result);
        }
        return result != null;
    }


    @Override
    public void receiveMessage(Message msg) {

    }
}
