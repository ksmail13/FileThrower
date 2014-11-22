package dropbox.server.Account;

import dropbox.server.Base.InfoBase;

/**
 * Created by micky on 2014. 11. 21..
 */
public class AccountInfo extends InfoBase {
    protected String u_id;
    protected String email;
    protected String password;


    @Override
    public String getInsertQueryString() {

        return super.getInsertQueryString() + String.format("Insert into AccountInfo(accountId, id, email, password) values (%s, %s, %s, %s)",
                id, u_id, email, password);
    }

    @Override
    public String getSelectQueryString() {
        return null;
    }

    @Override
    public String keyGenerate() {
        int num = (int)Math.round(Math.random()*100000000);

        return String.format("A%9d",num);
    }
}
