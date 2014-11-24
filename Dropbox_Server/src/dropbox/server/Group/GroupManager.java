package dropbox.server.Group;

import dropbox.common.Message;
import dropbox.server.Account.AccountInfo;
import dropbox.server.Account.AccountManager;
import dropbox.server.Base.ManagerBase;
import dropbox.server.Util.DatabaseConnector;
import dropbox.server.Util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Atomic;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.nio.channels.SocketChannel;
import java.security.acl.Group;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by micky on 2014. 11. 22..
 */
public class GroupManager extends ManagerBase {
    public final static String DBNAME = "GroupManager";
    private static GroupManager manager = null;
    public static GroupManager getManager() {
        if(manager == null)
            manager = new GroupManager();

        return manager;
    }

    private GroupCache cache = new GroupCache();

    @Override
    public JSONObject messageHandling(SocketChannel sc, JSONObject parsedObject) {
        JSONObject result = null;
        String subCategory = (String) parsedObject.get(Message.SUBCATEGORY_KEY);
        if("memberlist".equals(subCategory)) {
            result = getGroupMemberList(parsedObject);
        } else if("grouplist".equals(subCategory)) {
            result = getGroupList(AccountManager.getManager().getLoginInfo(sc),parsedObject);
        } else if("create".equals(subCategory)) {
            result = createGroup(sc, parsedObject);
        } else if("addmember".equals(subCategory)) {
            result = addGroupMember(parsedObject);
        } else if("change".equals(subCategory)) {

        } else if("exitgroup".equals(subCategory)) {

        } else if("delete".equals(subCategory)) {

        }

        result.put(Message.SUBCATEGORY_KEY, subCategory);
        return result;
    }

    private JSONObject getGroupMemberList(JSONObject parsedObject) {
        JSONObject result = new JSONObject();
        try {
            ResultSet rs = DatabaseConnector.getConnector().select(
                    String.format("select gmi.accountid, a.id, gmi.permission, gmi.accept " +
                            "from groupmemberinfo as gmi, accountinfo as a " +
                            "where gmi.accountid= a.accountid and gmi.groupid='%s';", parsedObject.get("groupid")));
            JSONArray arr = new JSONArray();

            while(rs.next()) {
                JSONObject obj = new JSONObject();
                obj.put("accountid", rs.getString("accountid"));
                obj.put("id", rs.getString("id"));
                obj.put("permission", rs.getString("permission"));
                obj.put("accept", rs.getBoolean("accept"));

                arr.add(obj);
            }
            result.put("grouplist", arr);
        } catch (SQLException e) {
            Logger.errorLogging(e);
        }
        return result;
    }

    private JSONObject getGroupList(AccountInfo loginInfo, JSONObject parsedObject) {
        JSONObject result = new JSONObject();
        try {
            ResultSet rs = DatabaseConnector.getConnector().select(
                    String.format("select * from accountgroupinfo where userid='%s';", loginInfo.getId()));
            JSONArray arr = new JSONArray();

            while(rs.next()) {
                JSONObject obj = new JSONObject();
                obj.put("groupid", rs.getString("groupid"));
                obj.put("groupname", rs.getString("groupname"));
                obj.put("permission", rs.getString("permission"));
                obj.put("accept", rs.getBoolean("accept"));
                obj.put("comment", rs.getString("comment"));
                obj.put("masterid", rs.getString("masterid"));
                obj.put("mastername", rs.getString("masteruid"));

                arr.add(obj);
            }
            result.put("grouplist", arr);
        } catch (SQLException e) {
            Logger.errorLogging(e);
        }
        return result;
    }

    private JSONObject addGroupMember(JSONObject parsedObject) {
        JSONObject result;
        result = new JSONObject();
        GroupInfo target = cache.get((String)parsedObject.get("groupid"));
        GroupPeopleInfo groupMember = new GroupPeopleInfo(
                target
                , AccountManager.getManager().getUserInfo((String)parsedObject.get("inviteId"))
                ,'U'
                ,false);
        target.addGroupMember(groupMember);
        result.put("result", DatabaseConnector.getConnector().insert(groupMember));
        return result;
    }

    private JSONObject createGroup(SocketChannel sc, JSONObject parsedObject) {
        JSONObject result = new JSONObject();
        GroupInfo newGroup = new GroupInfo(GroupInfo.keyGenerate()
                , (parsedObject.get("name") instanceof String)? (String)parsedObject.get("name") : ""
                , (parsedObject.get("comment") instanceof String)? (String)parsedObject.get("comment"):"");
        GroupPeopleInfo newGroupMember = new GroupPeopleInfo(newGroup, AccountManager.getManager().getLoginInfo(sc), 'M',false);
        result.put("result",
                DatabaseConnector.getConnector().insert(newGroup, false)
                        && DatabaseConnector.getConnector().insert(newGroupMember, false));
        result.put("groupname", newGroup.getName());
        result.put("groupid", newGroup.getId());
        if((Boolean)result.get("result")) {
            try {
                DatabaseConnector.getConnector().commit();
            } catch (SQLException e) {
                try {
                    DatabaseConnector.getConnector().rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            cache.put(newGroup.getId(), newGroup);
        }

        return result;
    }

    class GroupCache {
        Map<String, GroupInfo> cache;

        public GroupCache() {
            initCache();
        }

        private void initCache() {
            DB db = DBMaker.newMemoryDB().transactionDisable().closeOnJvmShutdown().make();
            cache = db.getTreeMap(GroupManager.DBNAME);
        }

        public GroupInfo get(String groupId) {
            GroupInfo info = cache.get(groupId);
            if(info == null) {
                try {
                    ResultSet rs = DatabaseConnector.getConnector().select("select g.groupid, i.name, g.comment from groupinfo as g left join infobase as i on i.infoid = g.groupid where groupid='%s'");
                    while(rs.next()) {
                        info = new GroupInfo(rs.getString("groupId"), rs.getString("name"), rs.getString("comment"));
                        cache.put(groupId, info);
                    }
                } catch (SQLException e) {
                    Logger.errorLogging(e);
                }
            }

            return info;
        }

        public void put(String id, GroupInfo groupInfo) {
            cache.put(id, groupInfo);
        }
    }
}
