package dropbox.server.Account;

import dropbox.common.Message;
import dropbox.server.Base.ManagerBase;
import dropbox.server.Util.DatabaseConnector;
import dropbox.server.Util.Logger;
import dropbox.server.Util.SocketChannelWrapper;
import org.json.simple.JSONObject;
import org.mapdb.DBMaker;

import java.nio.channels.SocketChannel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
            else {
                Logger.errorLogging("fail get login account" , null);
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

    public AccountInfo getUserInfo(String userId) {
        return cache.get(userId);
    }

    public SocketChannel getSession(AccountInfo accountInfo) {
        Logger.debugLogging("session sessionCount:"+session.sessionCount());
        return session.getAccountConnectedSocket(accountInfo);
    }

    public AccountInfo getUserInfoById(String inviteid) {
        AccountInfo info = null;

        for(String key : cache.getKeySet()) {
            AccountInfo temp = cache.get(key);
            if(inviteid.equals(temp.getU_id()))
                return temp;
        }
        String query = String.format("select * from infobase as i right join accountinfo as a on i.infoid=a.accountid where id='%s'", inviteid);
        try {
            ResultSet rs = DatabaseConnector.getConnector().select(query);
            while(rs.next()) {
                info = new AccountInfo(rs.getString("accountid"), rs.getString("id"), rs.getString("email"), "");
            }
            cache.put(info.getId(), info);
        } catch (SQLException e) {
            Logger.errorLogging(e);
        }

        return info;
    }

    /**
     * 계정과 현재 연결된 소켓을 관리하는 클래스
     */
    class SessionManager {
        public final static String SESSION = "sessionAccount";
        public final static String ACCOUNTSESSION = "accountSession";
        Map<SocketChannelWrapper, AccountInfo> sessionAccount;
        Map<AccountInfo, SocketChannelWrapper> accountSession;

        SessionManager() {
            sessionInit();
        }

        private void sessionInit() {
            //DB db = DBMaker.newMemoryDirectDB().transactionDisable().closeOnJvmShutdown().make();
//            sessionAccount = db.getTreeMap(SESSION);
//            accountSession = db.getTreeMap(ACCOUNTSESSION);

            sessionAccount = new HashMap<SocketChannelWrapper, AccountInfo>();
            accountSession = new HashMap<AccountInfo, SocketChannelWrapper>();
        }

        public AccountInfo get(SocketChannelWrapper scw) {
            return sessionAccount.get(scw);
        }

        public AccountInfo get(SocketChannel sc) {
            SocketChannelWrapper scw = new SocketChannelWrapper(sc);
            scw.setSocketChannel(sc);

            return get(scw);
        }

        public SocketChannel getAccountConnectedSocket(AccountInfo accountInfo) {
            SocketChannelWrapper scw = accountSession.get(accountInfo);
            if(scw != null)
                return scw.getSocketChannel();
            else
                return null;
        }

        public void put(SocketChannelWrapper scw, AccountInfo newAccount) {
            sessionAccount.put(scw, newAccount);
            accountSession.put(newAccount, scw);
            Logger.debugLogging(String.format("sessionAccount sessionCount %d accountSession %d", sessionAccount.size(), accountSession.size()));
        }

        /**
         * 소켓에 대응되는 계정정보를 저장한다.
         * 계정정보에 대응되는 소켓을 저장한다.
         * @param sc
         * @param newAccount
         */
        public void put(SocketChannel sc, AccountInfo newAccount) {
            put(new SocketChannelWrapper(sc), newAccount);
        }

        /**
         * 세션 제거
         * @param sc
         */
        public void remove(SocketChannel sc) {
            AccountInfo accountInfo = get(sc);
            if(accountInfo == null) {
                Logger.errorLogging("accountInfo is empty", new NullPointerException());
                return;
            }
            // 로그아웃한 시간을 기록한다.
            String logoutHistory = String.format("insert into history (jobtype, time, accountid) values('logout', now(), '%s');", accountInfo.getId());
            DatabaseConnector.getConnector().modify(logoutHistory);
            accountSession.remove(accountInfo);
            sessionAccount.remove(new SocketChannelWrapper(sc));
        }

        public int sessionCount() {
            return accountSession.size();
        }
    }

    class AccountCache {
        Map<String, AccountInfo> cache;

        AccountCache() {
            initCache();
        }

        private void initCache() {
            cache = DBMaker.newMemoryDirectDB().transactionDisable().closeOnJvmShutdown().make().getTreeMap(DBNAME);
        }

        public AccountInfo get(String accountId) {
            AccountInfo accountInfo = cache.get(accountId);
            if(accountInfo == null) {
                try {
                    String serarchQuery = String.format("select i.infoid, a.id, i.name, a.email \n" +
                            "from infobase as i right join accountinfo as a \n" +
                            "on a.accountid = i.infoid where a.accountid='%s';", accountId);
                    ResultSet rs = DatabaseConnector.getConnector().select(serarchQuery);
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

        public Set<String> getKeySet() {
            return cache.keySet();
        }

        public void put(String id, AccountInfo newAccount) {
            cache.put(id, newAccount);
        }
    }
}
