package dropbox.server.Account;

import dropbox.common.Message;
import dropbox.common.MessageType;
import dropbox.server.Base.ManagerBase;
import dropbox.server.Util.DatabaseConnector;
import dropbox.server.Util.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.nio.channels.SocketChannel;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    private Map<SocketChannel, AccountInfo> session;


    private AccountManager() {
        DB db = DBMaker.newMemoryDB().transactionDisable().closeOnJvmShutdown().make();
        session = db.getTreeMap(SESSION);
    }

    public boolean login(SocketChannel sc, String id, String password) {
        DatabaseConnector dbConn = DatabaseConnector.getConnector();
        String loginQuery = String.format("select i.infoid, a.id, i.name, a.email \n" +
                "from infobase as i right join accountinfo as a \n" +
                "on a.accountid = i.infoid and a.id='%s' and a.password='%s';", id, password);
        try {
            ResultSet result = dbConn.select(loginQuery);
            AccountInfo newAccount = AccountInfo.createAccount(result);
            if(newAccount != null) {
                session.put(sc, newAccount);
                return true;
            }

        } catch (SQLException e) {
            Logger.errorLogging(e);
            e.printStackTrace();
        }

        return false;
    }


    @Override
    public void receiveMessage(SocketChannel sc, Message msg) {
        JSONParser parser = new JSONParser();
        JSONObject result = null;
        Message message = null;
        try {
            Object parseResult = parser.parse(msg.msg);
            if(parseResult instanceof JSONObject) {
                JSONObject parsedObject = (JSONObject)parseResult;
                if("login".equals(parsedObject.get("SubCategory"))) {
                    result = new JSONObject();
                    result.put("result", login(sc, (String)parsedObject.get("id"), (String)parsedObject.get("password")));
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(result != null) {
            message = new Message();
            message.messageType = MessageType.Result;
            message.msg = result.toJSONString();
        }
    }

    /**
     * 소켓에 연동된 로그인 정보를 받아온다.
     * @param sc 확인할 소켓
     * @return 연결된 계정
     */
    public AccountInfo getLoginInfo(SocketChannel sc) {
        return session.get(sc);
    }


}
