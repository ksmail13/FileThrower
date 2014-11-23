package dropbox.server.Group;

import dropbox.server.Base.InfoBase;

import java.util.LinkedList;

/**
 * Created by micky on 2014. 11. 21..
 */
public class GroupInfo extends InfoBase {
    protected final LinkedList<GroupPeopleInfo> groupPeopleList = new LinkedList<GroupPeopleInfo>();
    protected final String comment;

    public GroupInfo(String id, String name, String comment) {
        super(id, name);
        this.comment = comment;
    }

    public static String keyGenerate() {
        return null;
    }

    @Override
    public String getInsertQueryString() {
        StringBuilder sb = new StringBuilder();
        for(GroupPeopleInfo info : groupPeopleList) {
            sb.append("\""+info.getGroupId()+"\"");
            sb.append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        return super.getInsertQueryString() +
                String.format("Insert into GroupInfo(groupId, groupMemberList) values (%s,'{%s}')", id, sb.toString());
    }

    @Override
    public String getSelectQueryString() {
        return null;
    }
}
