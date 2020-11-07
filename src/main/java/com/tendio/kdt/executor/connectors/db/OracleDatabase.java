package com.tendio.kdt.executor.connectors.db;


import com.tendio.kdt.TestProperties;
import com.tendio.kdt.executor.exception.InterruptedTestCaseException;

import javax.annotation.Nullable;
import java.sql.*;

public class OracleDatabase {
    private static final String JDBC_URL = TestProperties.getProperty("test.db.jdbc.url");
    private static final String USER = TestProperties.getProperty("test.db.user");
    private static final String PASSWORD = TestProperties.getProperty("test.db.password");
    private static Connection connection;
/*    private static ThreadLocal<Connection> connection = new ThreadLocal<>();

    public static Connection getConnection() {
        if (connection.get() == null) {
            Connection con = getConnection();
            Pool.cons.add(con);
            connection.set(con);
        }
        return connection.get();
    }*/

    private static Connection getConnection() {
        return connection != null ? connection : connect(JDBC_URL, USER, PASSWORD);
    }

    private static Connection connect(String jdbcUrl, String user, String password) {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection = DriverManager.getConnection(jdbcUrl, user, password);
        } catch (SQLException | ClassNotFoundException e) {
            //rethrow runtime exception just to interrupt single test case
            throw new InterruptedTestCaseException(e);
        }
        return connection;
    }

    @Nullable
    public static ResultSet executeQuery(String query) {
        return getResultSet(query);
    }

    public static int executeUpdateQuery(String query) {
        try (PreparedStatement s = getConnection().prepareStatement(query)) {
            return s.executeUpdate();
        } catch (SQLException e) {
            rethrowSqlException(e, query);
        }
        return 0;
    }

    @Nullable
    public static ResultSet executeCall(String query) {
        return getResultSet(query);
    }

    private static ResultSet getResultSet(String query) {
        try {
            PreparedStatement s = getConnection().prepareStatement(query);
            return s.executeQuery();
        } catch (SQLException e) {
            rethrowSqlException(e, query);
        }
        return null;
    }

    private static void rethrowSqlException(SQLException e, String query) {
        String message = String.format("Couldn`t execute query: %s. Cause: %s", query, e.getMessage());
        throw new InterruptedTestCaseException(message);
    }

}

