package dropbox.server.Group;

import dropbox.server.Base.InfoBase;

import java.util.LinkedList;

/**
 * Created by micky on 2014. 11. 21..
 */
public class GroupInfo extends InfoBase {
    protected final LinkedList<GroupMemberInfo> groupPeopleList = new LinkedList<GroupMemberInfo>();
    protected final String comment;

    public GroupInfo(String id, String name, String comment) {
        super(id, name);
        this.comment = comment;
    }

    public static String keyGenerate() {
        return InfoBase.keyGenerate("G");
    }

    public void addGroupMember(GroupMemberInfo newMember) {
        groupPeopleList.add(newMember);
    }

    @Override
    public String getInsertQueryString() {
//        StringBuilder sb = new StringBuilder();
//        for(GroupMemberInfo info : groupPeopleList) {
//            sb.append("\""+info.getGroupInfo()+"\"");
//            sb.append(",");
//        }
//        sb.deleteCharAt(sb.length()-1);
        return super.getInsertQueryString() +
                String.format("Insert into groupinfo(groupId, comment) values ('%s','%s')", id, comment);
    }

    @Override
    public String getSelectQueryString() {
        return null;
    }
}
