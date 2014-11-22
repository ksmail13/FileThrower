package dropbox.server.Group;

import dropbox.server.Base.InfoBase;

import java.util.LinkedList;

/**
 * Created by micky on 2014. 11. 21..
 */
public class GroupInfo extends InfoBase {
    protected LinkedList<GroupPeopleInfo> groupPeopleList = new LinkedList<GroupPeopleInfo>();

    @Override
    public String keyGenerate() {
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
