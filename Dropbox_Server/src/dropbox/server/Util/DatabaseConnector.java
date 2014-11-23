package dropbox.server.Util;

import dropbox.server.Account.AccountInfo;

import java.sql.*;
import java.util.Map;
import java.util.Properties;

/**
 * Created by micky on 2014. 11. 21..
 */
public class DatabaseConnector {
    private static DatabaseConnector connector = null;

    public static DatabaseConnector getConnector() {
        if(connector == null) {
            connector = new DatabaseConnector();
        }

        return connector;
    }

    public final static String DB_ADDR = "jdbc:postgresql://10.0.30.59:5432/dropbox";
    public final static String ID = "postgres";
    public final static String PASSWORD = "qufdmltnarufdldu";

    private Connection conn;


    private DatabaseConnector() {
        connect();
    }

    /**
     * 데이터 베이스와 연결
     */
    private void connect() {
        try {
            conn = DriverManager.getConnection(DB_ADDR, ID, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ResultSet select(String selectquery) throws SQLException {
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(selectquery);
        return rs;
    }

    public boolean modify(String query) {
        boolean result = false;
        try {
            PreparedStatement pst = conn.prepareStatement(query);
            pst.executeQuery();
            result = true;
        } catch (SQLException e) {
            Logger.errorLogging(e);
        }

        return result;
    }

    public static void main(String[] args) {

    }
}
