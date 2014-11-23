package dropbox.server.Account;

import dropbox.server.Base.InfoBase;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by micky on 2014. 11. 21..
 */
public class AccountInfo extends InfoBase {
    protected String u_id;
    protected String email;
    protected String password;

    public static AccountInfo createAccount(ResultSet dbresult) {
        AccountInfo newAccount = null;
        try {
            if(dbresult.first()) {
                newAccount = new AccountInfo();
                newAccount.id = dbresult.getString("accountid");
                newAccount.u_id = dbresult.getString("id");
                newAccount.name = dbresult.getString("name");
                newAccount.email = dbresult.getString("email");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return newAccount;
    }


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
