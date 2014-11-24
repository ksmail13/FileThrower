package dropbox.server.Group;

import dropbox.server.Account.AccountInfo;
import dropbox.server.Base.Queriable;

/**
 * Created by micky on 2014. 11. 21..
 */
public class GroupMemberInfo implements Queriable{
    protected GroupInfo groupInfo;
    protected AccountInfo accountInfo;
    protected char permission;

    protected boolean accept = false;

    public GroupMemberInfo(GroupInfo groupInfo, AccountInfo accountInfo, char permission, boolean accept) {
        this.groupInfo = groupInfo;
        this.accountInfo = accountInfo;
        this.permission = permission;
        this.accept = accept;
    }

    public GroupInfo getGroupInfo() {
        return groupInfo;
    }

    public void setGroupInfo(GroupInfo groupInfo) {
        this.groupInfo = groupInfo;
    }

    public AccountInfo getAccountInfo() {
        return accountInfo;
    }

    public void setAccountInfo(AccountInfo accountInfo) {
        this.accountInfo = accountInfo;
    }

    public char getPermission() {
        return permission;
    }

    public void setPermission(char permission) {
        this.permission = permission;
    }

    @Override
    public String getInsertQueryString() {
        return String.format("insert into groupmemberinfo (groupid, accountid, permission, accept) values ('%s', '%s', '%c', '%s');", groupInfo.getId(), accountInfo.getId(), permission, accept);
    }

    @Override
    public String getSelectQueryString() {
        return null;
    }


}
