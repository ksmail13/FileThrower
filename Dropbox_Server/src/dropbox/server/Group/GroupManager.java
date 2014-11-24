package dropbox.server.Group;

import dropbox.common.Message;
import dropbox.server.Account.AccountInfo;
import dropbox.server.Account.AccountManager;
import dropbox.server.Base.ManagerBase;
import dropbox.server.Util.DatabaseConnector;
import dropbox.server.Util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.nio.channels.SocketChannel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by micky on 2014. 11. 22..
 * 그룹을 관리하는 클래스
 */
public class GroupManager extends ManagerBase {
    /**
     * 캐싱할 때 사용하는 키
     */
    public static final String DBNAME = "GroupManager";
    public static final String MEMBERLIST_DBNAME = "MemberManager";
    /**
     * 싱글턴 패턴을 위한 static member
     */
    private static GroupManager manager = null;
    public static GroupManager getManager() {
        if(manager == null)
            manager = new GroupManager();

        return manager;
    }

    /**
     * 데이터 캐싱객체
     */
    private GroupCache cache = new GroupCache();
    /**
     * 그룹멤버 캐싱 객체
     */
    private GroupMemberCache memberCache = new GroupMemberCache();

    /**
     * 수신한 메시지를 처리한다.
     * @param sc
     * @param parsedObject
     * @return
     */
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

    /**
     * 데이터베이스로 부터 그룹의 멤버리스트를 받아온다.
     * @param parsedObject
     * @return
     */
    private JSONObject getGroupMemberList(JSONObject parsedObject) {
        JSONObject result = new JSONObject();

//
//            ResultSet rs = DatabaseConnector.getConnector().select(
//                    String.format("select gmi.accountid, a.id, gmi.permission, gmi.accept " +
//                            "from groupmemberinfo as gmi, accountinfo as a " +
//                            "where gmi.accountid= a.accountid and gmi.groupid='%s';", parsedObject.get("groupid")));
        JSONArray arr = new JSONArray();

        List<GroupMemberInfo> memberlist = memberCache.get((String) parsedObject.get("groupid"));

        for(GroupMemberInfo info : memberlist) {
            JSONObject obj = new JSONObject();
            obj.put("accountid", info.accountInfo.getId());
            obj.put("id", info.accountInfo.getU_id());
            obj.put("permission", info.permission);
            obj.put("accept", info.accept);

            arr.add(obj);
        }
        result.put("grouplist", arr);
        return result;
    }

    /**
     * 데이터 베이스로 부터 자신이 속한 그룹의 정보를 받아온다.
     * @param loginInfo
     * @param parsedObject
     * @return
     */
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
        GroupMemberInfo groupMember = new GroupMemberInfo(
                target
                , AccountManager.getManager().getUserInfo((String)parsedObject.get("inviteId"))
                ,'U'
                ,false);
        target.addGroupMember(groupMember);
        result.put("result", DatabaseConnector.getConnector().insert(groupMember));
        return result;
    }

    /**
     * 그룹을 생성하고 그룹을 생성한 사용자를 생성한 그룹에 넣는다.
     * @param sc
     * @param parsedObject
     * @return
     */
    private JSONObject createGroup(SocketChannel sc, JSONObject parsedObject) {
        JSONObject result = new JSONObject();
        GroupInfo newGroup = new GroupInfo(GroupInfo.keyGenerate()
                , (parsedObject.get("name") instanceof String)? (String)parsedObject.get("name") : ""
                , (parsedObject.get("comment") instanceof String)? (String)parsedObject.get("comment"):"");
        GroupMemberInfo newGroupMember = new GroupMemberInfo(newGroup, AccountManager.getManager().getLoginInfo(sc), 'M',true);
        // 그룹을 생성하고
        // 생성한 그룹에 그룹을 생성한 사용자를 추가한다.
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

    public GroupInfo getGroupInfo(String groupId) {
        return cache.get(groupId);
    }

    public List<GroupMemberInfo> getGroupMemberList(String groupId) {
        return memberCache.get(groupId);
    }

    /**
     * 그룹 정보 캐싱하는 클래스
     * MMDB를 사용해 데이터를 캐싱하고 데이터가 없을 때 데이터베이스로 부터 데이터를 불러온다.
     */
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
                    String selectQuery = String.format("select g.groupid, i.name, g.comment from groupinfo as g left join infobase as i on i.infoid = g.groupid where groupid='%s'",groupId);
                    ResultSet rs = DatabaseConnector.getConnector().select(selectQuery);
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

    class GroupMemberCache {
        Map<String, List<GroupMemberInfo>> cache;

        public GroupMemberCache() {
            initCache();
        }

        private void initCache() {
            DB db = DBMaker.newMemoryDB().transactionDisable().closeOnJvmShutdown().make();
            cache=db.getTreeMap(MEMBERLIST_DBNAME);
        }

        public List<GroupMemberInfo> get(String groupId) {
            List<GroupMemberInfo> list = cache.get(groupId);
            if(list == null) {
                try {
                    String selectQuery = String.format("select accountid, permission, accept from groupmemberinfo where groupid='%s';", groupId);
                    ResultSet rs = DatabaseConnector.getConnector().select(selectQuery);
                    list = new LinkedList<GroupMemberInfo>();
                    while(rs.next()) {
                        GroupMemberInfo memberInfo = new GroupMemberInfo(getGroupInfo(groupId)
                                , AccountManager.getManager().getUserInfo(rs.getString("accountid"))
                                ,rs.getString("permission").charAt(0)
                                , rs.getBoolean("permission"));

                        list.add(memberInfo);
                    }

                    cache.put(groupId, list);
                } catch (SQLException e) {
                    Logger.errorLogging(e);
                }
            }
            return list;
        }

        public void put(String groupId, List<GroupMemberInfo> memberInfoList) {
            cache.put(groupId, memberInfoList);
        }

        public void addUser(String groupId, GroupMemberInfo memberInfo) {
            List<GroupMemberInfo> list = get(groupId);
            list.add(memberInfo);

            DatabaseConnector dbConn = DatabaseConnector.getConnector();
            dbConn.insert(memberInfo);
        }
    }
}
