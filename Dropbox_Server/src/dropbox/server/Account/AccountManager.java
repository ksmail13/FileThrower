package dropbox.server.Account;

import dropbox.common.Message;
import dropbox.server.Base.ManagerBase;
import dropbox.server.Util.DatabaseConnector;
import dropbox.server.Util.Logger;
import dropbox.server.Util.SocketChannelWrapper;
import org.json.simple.JSONObject;
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
    private static AccountManager managerInstance = null;

    public static AccountManager getManager() {
        if(managerInstance == null) {
            managerInstance = new AccountManager();
        }
        return managerInstance;
    }

    private SessionManager session;
    private AccountCache cache;

    private AccountManager() {
        session = new SessionManager();
        cache = new AccountCache();
    }

    @Override
    public JSONObject messageHandling(SocketChannel sc, JSONObject parsedObject) {
        JSONObject resultJson = new JSONObject();
        String subCategory = (String)parsedObject.get(Message.SUBCATEGORY_KEY);

        if("login".equals(subCategory)) {
            resultJson.put("result", login(sc, (String) parsedObject.get("id"), (String) parsedObject.get("password")));
        } else if("create".equals(subCategory)) {
            resultJson.put("result", createAccount(sc, (String) parsedObject.get("id"), (String) parsedObject.get("password"), (String) parsedObject.get("email")));
        } else if("logout".equals(subCategory)) { }

        resultJson.put("uid", parsedObject.get("id"));

        resultJson.put(Message.SUBCATEGORY_KEY, subCategory);
        return resultJson;
    }


    public Boolean login(SocketChannel sc, String id, String password) {
        DatabaseConnector dbConn = DatabaseConnector.getConnector();
        String loginQuery = String.format("select i.infoid, a.id, i.name, a.email \n" +
                "from infobase as i right join accountinfo as a \n" +
                "on a.accountid = i.infoid where a.id='%s' and a.password='%s';", id, password);
        try {
            ResultSet result = dbConn.select(loginQuery);
            AccountInfo loginAccount = AccountInfo.createAccount(result);
            if(loginAccount != null) {
                SocketChannelWrapper scw = new SocketChannelWrapper(sc);
                session.put(scw, loginAccount);
                cache.put(loginAccount.getId(), loginAccount);

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
        AccountInfo newAccount = new AccountInfo(newKey, id, email, password);

        if(dbConn.insert(newAccount)) {
            SocketChannelWrapper scw = new SocketChannelWrapper(sc);
            session.put(scw, newAccount);
            cache.put(newAccount.getId(), newAccount);
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
        SocketChannelWrapper scw = new SocketChannelWrapper(sc);
        return session.get(scw);
    }

    public void deleteSession(SocketChannel sc) {
        session.remove(sc);
    }

    public AccountInfo getUserInfo(String inviteId) {
        return cache.get(inviteId);
    }

    /**
     * 계정과 현재 연결된 소켓을 관리하는 클래스
     */
    class SessionManager {
        public final static String SESSION = "session";

        Map<SocketChannelWrapper, AccountInfo> session;

        SessionManager() {
            sessionInit();
        }

        private void sessionInit() {
            DB db = DBMaker.newMemoryDB().transactionDisable().closeOnJvmShutdown().make();
            session = db.getTreeMap(SESSION);

        }

        public AccountInfo get(SocketChannelWrapper scw) {
            return session.get(scw);
        }

        public AccountInfo get(SocketChannel sc) {
            SocketChannelWrapper scw = new SocketChannelWrapper(sc);
            scw.setSocketChannel(sc);

            return get(scw);
        }

        public void put(SocketChannelWrapper scw, AccountInfo newAccount) {
            session.put(scw, newAccount);
        }

        public void put(SocketChannel sc, AccountInfo newAccount) {
            session.put(new SocketChannelWrapper(sc), newAccount);
        }

        public void remove(SocketChannel sc) {
            session.remove(new SocketChannelWrapper(sc));
        }
    }

    class AccountCache {
        Map<String, AccountInfo> cache;

        AccountCache() {
            initCache();
        }

        private void initCache() {
            cache = DBMaker.newMemoryDB().transactionDisable().closeOnJvmShutdown().make().getTreeMap(DBNAME);
        }

        public AccountInfo get(String accountId) {
            AccountInfo accountInfo = cache.get(accountId);
            if(accountInfo == null) {
                try {
                    ResultSet rs = DatabaseConnector.getConnector().select(String.format("select i.infoid, a.id, i.name, a.email \n" +
                            "from infobase as i right join accountinfo as a \n" +
                            "on a.accountid = i.infoid where a.id='%s';", accountId));
                    while(rs.next()) {
                        accountInfo = new AccountInfo(rs.getString("infoid")
                        , rs.getString("id")
                        , rs.getString("name")
                        , rs.getString("email"));

                        cache.put(accountInfo.getId(), accountInfo);
                    }
                } catch (SQLException e) {
                    Logger.errorLogging(e);
                }
            }

            return accountInfo;
        }

        public void put(String id, AccountInfo newAccount) {
            cache.put(id, newAccount);
        }
    }
}
