package dropbox.server.Group;

import dropbox.server.Account.AccountInfo;

/**
 * Created by micky on 2014. 11. 21..
 */
public class GroupPeopleInfo {
    protected String groupId;
    protected AccountInfo accountId;
    protected char permission;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public AccountInfo getAccountId() {
        return accountId;
    }

    public void setAccountId(AccountInfo accountId) {
        this.accountId = accountId;
    }

    public char getPermission() {
        return permission;
    }

    public void setPermission(char permission) {
        this.permission = permission;
    }
}
