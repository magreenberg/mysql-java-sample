package demo;

import java.sql.*;
import java.util.concurrent.ThreadFactory;

class MysqlDemo {

    private static String DBNAME = "mysqldemo";
    private static String TABLE_NAME = "employees";

    private static Connection getConnection(final String baseUrl, final String user, final String password) {
        Connection con = null;
        try {
            con = DriverManager.getConnection(baseUrl, user, password);
        } catch (SQLException sqe) {
            System.err.println(
                    "Unable to connect to the database using: " + baseUrl + " (" + user + "/" + password + ")");
            System.err.println(sqe);
            System.exit(1);
        }
        return con;
    }

    void closeConnection(final Connection con) {
        try {
            con.close();
        } catch (Exception e) {
            // ignore
        }
    }

    private static void createDatabase(Connection conn, String dbName) throws SQLException {
        try {
            Statement stmt = conn.createStatement();
            String sql = "CREATE DATABASE " + dbName;
            stmt.executeUpdate(sql);
        } catch (SQLException sqe) {
            if (sqe.getMessage().indexOf("database exists") == -1) {
                System.err.println("Failed to create database " + dbName);
                System.err.println(sqe);
                System.exit(1);
            }
        }
        try {
            Statement stmt = conn.createStatement();
            String sql = "USE " + dbName;
            stmt.executeUpdate(sql);
        } catch (SQLException sqe) {
            System.err.println("Failed to select database " + dbName);
            System.err.println(sqe);
            System.exit(1);
        }
    }

    private static void deleteDatabase(Connection conn, String dbName) throws SQLException {
        try {
            Statement stmt = conn.createStatement();
            String sql = "DROP DATABASE " + dbName;
            stmt.executeUpdate(sql);
        } catch (SQLException sqe) {
            System.err.println("Failed to delete database " + dbName);
            System.err.println(sqe);
        }
    }

    private static void createTable(Connection conn) throws SQLException {
        String sqlCreate = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "  (id    integer not null,"
                + "   first_name varchar(255) not null, " + "   last_name varchar(255) not null,"
                + "   email_address varchar(255) not null, primary key(id) " + ")";

        Statement stmt = conn.createStatement();
        stmt.execute(sqlCreate);
    }

    private static void addEmployee(Connection conn, int id, String firstName, String lastName, String eMail) {
        // the mysql insert statement
        String query = " insert into " + TABLE_NAME + " (id, first_name, last_name, email_address)"
                + " values (?, ?, ?, ?)";
        try {
            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setInt(1, id);
            preparedStmt.setString(2, firstName);
            preparedStmt.setString(3, lastName);
            preparedStmt.setString(4, eMail);

            // execute the preparedstatement
            preparedStmt.execute();
        } catch (SQLException sqe) {
            System.err.println("Failed to insert record for " + firstName + " " + lastName);
            System.err.println(sqe);
        }
    }

    private static void updateEmployee(Connection conn, int id, String firstName, String lastName, String eMail) {
        // the mysql insert statement
        String query = " update " + TABLE_NAME + " set first_name = ?, last_name = ?, email_address = ? where id = ?";
        try {
            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString(1, firstName);
            preparedStmt.setString(2, lastName);
            preparedStmt.setString(3, eMail);
            preparedStmt.setInt(4, id);

            // execute the preparedstatement
            preparedStmt.execute();
        } catch (SQLException sqe) {
            System.err.println("Failed to update record " + id);
            System.err.println(sqe);
        }
    }

    private static void deleteEmployee(Connection conn, int id) {
        // the mysql insert statement
        String query = " delete from " + TABLE_NAME + " where id = ?";
        try {
            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setInt(1, id);
            // execute the preparedstatement
            preparedStmt.execute();
        } catch (SQLException sqe) {
            System.err.println("Failed to delete record " + id);
            System.err.println(sqe);
        }
    }

    private static void dumpEmployees(Connection conn) {
        System.out.println("---");
        try {
            final Statement stmt = conn.createStatement();
            final ResultSet rs = stmt.executeQuery("select * from " + TABLE_NAME);
            while (rs.next())
                System.out.println(
                        rs.getInt(1) + "  " + rs.getString(2) + "  " + rs.getString(3) + "  " + rs.getString(4));
        } catch (SQLException sqe) {
            System.err.println("Failed to dumpe records");
            System.err.println(sqe);
        }
    }

    public static void main(final String args[]) {

        String dbUserName;
        String dbPassword;
        String dbHost;
        String dbPort;

        if (args.length != 4) {
            dbUserName = System.getenv("MYSQL_USER");
            if (dbUserName == null) {
                dbUserName = "root";
            }
            dbPassword = System.getenv("MYSQL_PASSWORD");
            dbHost = System.getenv("MYSQL_HOST");
            dbPort = System.getenv("MYSQL_PORT");
            if (dbPort == null) {
                dbPort = "3306";
            }
        } else {
            dbUserName = args[0];
            dbPassword = args[1];
            dbHost = args[2];
            dbPort = args[3];
        }
        if (dbPassword == null || dbHost == null) {
            System.out.println("database password or hostname not provided.");
            System.out.println("Usage: " + MysqlDemo.class.getName() + " dbusername password host port (" + dbUserName
                    + "," + dbPassword + "," + dbHost + "," + dbPort + ")");
            System.out.println(
                    "Alternatively, set environment variables: MYSQL_USER, MYSQL_PASSWORD, MYSQL_HOST, MYSQL_PORT");
            System.exit(1);
        }

        final Connection conn = getConnection("jdbc:mysql://" + dbHost + ":" + dbPort, dbUserName, dbPassword);

        try {
            createDatabase(conn, DBNAME);
            createTable(conn);
            addEmployee(conn, 1, "David", "BenGurion", "dbg@negev.il");
            addEmployee(conn, 2, "Theodore", "Herzl", "ted@basil.eu");
            addEmployee(conn, 3, "Moshe", "Dayan", "moshe@dayan.co.il");
            dumpEmployees(conn);
            updateEmployee(conn, 2, "Chaim", "Weizmann", "chaimg@gov.il");
            deleteEmployee(conn, 3);
            dumpEmployees(conn);
            deleteDatabase(conn, DBNAME);
            System.out.println(("All database CRUD operations completed successfully!"));
            System.out.println(("Press \"^c\" to exit."));
            try {
                Object obj = new Object();
                synchronized (obj) {
                    obj.wait();
                }
            } catch (InterruptedException e) {
                System.exit(0);
            }
        } catch (final Exception e) {
            System.out.println(e);
        } finally {
            try {
                conn.close();
            } catch (SQLException sqle) {
                // ignore
            }
        }
    }
}
