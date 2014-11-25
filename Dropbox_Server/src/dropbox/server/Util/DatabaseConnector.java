package dropbox.server.Util;

import dropbox.server.Base.Queriable;
import sun.rmi.runtime.Log;

import java.sql.*;

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

    public final static String DB_ADDR = "jdbc:postgresql://10.0.29.4:5432/dropbox";
//    public final static String DB_ADDR = "jdbc:postgresql://192.168.0.25:5432/dropbox";
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
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ResultSet select(String selectquery) throws SQLException {
        Statement st = conn.createStatement();
        selectquery = selectquery.trim();
        Logger.debugLogging("select query :"+selectquery);
        return st.executeQuery(selectquery);
    }

    public boolean modify(String query) {
        return modify(query, true);
    }

    public boolean modify(String query, boolean commit) {
        boolean result = false;
        query = query.trim();
        Logger.logging("modify query:" +query);
        try {
            PreparedStatement pst = conn.prepareStatement(query);
            pst.execute();
            result = true;
            if(commit) commit();
        } catch (SQLException e) {
            Logger.errorLogging(e);
            try {
                rollback();
            } catch (SQLException e1) {
                Logger.errorLogging(e1);
            }
        }

        return result;
    }

    public boolean insert(Queriable infoObject) {
        return modify(infoObject.getInsertQueryString());
    }

    public boolean insert(Queriable infoObject, boolean commit) {
        return modify(infoObject.getInsertQueryString(), commit);
    }

    public void commit() throws SQLException {
        conn.commit();
    }
    public void rollback() throws SQLException {
        conn.rollback();
    }

}
