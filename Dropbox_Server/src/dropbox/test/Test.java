package dropbox.test;

import dropbox.server.Util.DatabaseConnector;

import javax.xml.crypto.Data;
import java.sql.SQLException;

/**
 * Created by micky on 2014. 11. 22..
 */
public class Test {
    public static void main(String[] args) {
        DatabaseConnector connector = DatabaseConnector.getConnector();

        try {
            connector.selectTest("select * from infobase as i left join accountinfo as a on i.infoid = a.accountid");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
