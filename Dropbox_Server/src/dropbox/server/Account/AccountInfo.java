package dropbox.server.Account;

import dropbox.server.Base.InfoBase;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by micky on 2014. 11. 21..
 */
public class AccountInfo extends InfoBase {
    protected final String u_id;
    protected final String email;
    protected final String password;

    public String getU_id() {
        return u_id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public static AccountInfo createAccount(ResultSet dbresult) {
        AccountInfo newAccount = null;
        try {
            if(dbresult.next()) {
                newAccount = new AccountInfo(dbresult.getString("infoid"),
                        dbresult.getString("id"),
                        dbresult.getString("email"), "");

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return newAccount;
    }

    public AccountInfo(String id, String u_id, String email, String password) {
        super(id, "");
        this.u_id = u_id;
        this.email = email;
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountInfo)) return false;
        if (!super.equals(o)) return false;

        AccountInfo that = (AccountInfo) o;

        if (email != null ? !email.equals(that.email) : that.email != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (u_id != null ? !u_id.equals(that.u_id) : that.u_id != null) return false;

        return true;
    }


    @Override
    public String getInsertQueryString() {
        return super.getInsertQueryString() + String.format("Insert into AccountInfo(accountId, id, email, password) values ('%s', '%s', '%s', '%s')",
                id, u_id, email, password);
    }

    @Override
    public String getSelectQueryString() {
        return null;
    }

    public static String keyGenerate() {

        return InfoBase.keyGenerate("A");
    }

}
