package dropbox.server.Account;

import com.sun.org.apache.xpath.internal.operations.Bool;
import dropbox.common.Message;
import dropbox.common.MessageType;
import dropbox.common.MessageWrapper;
import dropbox.server.Base.ManagerBase;
import dropbox.server.Util.DatabaseConnector;
import dropbox.server.Util.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mapdb.Atomic;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import sun.rmi.runtime.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.sql.PreparedStatement;
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


    @Override
    public void receiveMessage(SocketChannel sc, Message msg) throws IOException {
        JSONParser parser = new JSONParser();
        JSONObject resultJson = null;
        Message message = null;
        Boolean procedureResult = false;
        try {
            Object parseResult = parser.parse(msg.msg);
            if(parseResult instanceof JSONObject) {
                JSONObject parsedObject = (JSONObject)parseResult;
                String subCategory = (String)parsedObject.get(Message.SUBCATEGORY_KEY);
                resultJson = new JSONObject();

                if("login".equals(subCategory)) {

                    procedureResult = login(sc, (String) parsedObject.get("id"), (String) parsedObject.get("password"));
                }
                else if("logout".equals(subCategory)){}
                else if("create".equals(subCategory)) {
                    procedureResult = createAccount(sc, (String) parsedObject.get("id"), (String) parsedObject.get("password"),(String) parsedObject.get("email"));
                }

            }
        } catch (ParseException e) {
            Logger.errorLogging(e);
        } catch (ClassCastException cce) {
            Logger.errorLogging(cce);
        } finally {
            resultJson.put("result",procedureResult);
        }

        if(resultJson != null) {
            message = new Message();
            message.messageType = MessageType.Result;
            message.msg = resultJson.toJSONString();

            sc.write(ByteBuffer.wrap(MessageWrapper.messageToByteArray(message)));
        }
    }


    public Boolean login(SocketChannel sc, String id, String password) {
        DatabaseConnector dbConn = DatabaseConnector.getConnector();
        String loginQuery = String.format("select i.infoid, a.id, i.name, a.email \n" +
                "from infobase as i right join accountinfo as a \n" +
                "on a.accountid = i.infoid where a.id='%s' and a.password='%s';", id, password);
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

    private Boolean createAccount(SocketChannel sc, String id, String password, String email) {
        Boolean res = false;

        DatabaseConnector dbConn = DatabaseConnector.getConnector();
        String newKey = AccountInfo.keyGenerate();
        String createQuery = String.format("insert into infobase values(%s);" +
                "insert into accoutinfo values(%s, %s, %s, %s", newKey, newKey, id, password, email);
        if(dbConn.modify(createQuery)) {
            AccountInfo newAccount = new AccountInfo(newKey,"", id, email, "");
            session.put(sc, newAccount);
            res = true;
        }

        return res;
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
