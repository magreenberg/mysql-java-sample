package demo;

import java.sql.*;
import java.util.concurrent.ThreadFactory;

class MysqlDemo {

    private static Connection getConnection(final String baseUrl, final String User, final String password) {
        Connection con = null;
        try {
            con = DriverManager.getConnection(baseUrl, User, password);
        } catch (SQLException sqe) {
            System.err.println("Unable to connect to the database");
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

    private static void createEmployees(Connection conn) throws SQLException {
        String sqlCreate = "CREATE TABLE IF NOT EXISTS employees" + "  (id    integer not null,"
                + "   first_name varchar(255) not null, " + "   last_name varchar(255) not null,"
                + "   email_address varchar(255) not null, primary key(id) " + ")";

        Statement stmt = conn.createStatement();
        stmt.execute(sqlCreate);
    }

    private static void addEmployee(Connection conn, int id, String firstName, String lastName, String eMail) {
        // the mysql insert statement
        String query = " insert into employees (id, first_name, last_name, email_address)" + " values (?, ?, ?, ?)";
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

    private static void deleteEmployee(Connection conn, int id) {
        // the mysql insert statement
        String query = " delete from employees where id = ?";
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
            final ResultSet rs = stmt.executeQuery("select * from employees");
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
            dbPassword = System.getenv("MYSQL_ROOT_PASSWORD");
            dbHost = System.getenv("MYSQL_HOST");
            dbPort = System.getenv("MYSQL_PORT");
            if (dbUserName == null || dbPassword == null || dbHost == null || dbPort == null) {
                System.out.println("Usage: " + MysqlDemo.class.getName() + " dbusername password host port");
                System.exit(1);
            }
        } else {
            dbUserName = args[0];
            dbPassword = args[1];
            dbHost = args[2];
            dbPort = args[3];
        }
        final Connection conn = getConnection("jdbc:mysql://" + dbHost + ":" + dbPort, dbUserName, dbPassword);

        try {
            createDatabase(conn, args[0]);
            createEmployees(conn);
            addEmployee(conn, 1, "David", "BenGurion", "dbg@negev.il");
            addEmployee(conn, 2, "Theodore", "Herzl", "ted@basil.eu");
            addEmployee(conn, 3, "Moshe", "Dayan", "moshe@dayan.co.il");
            dumpEmployees(conn);
            deleteEmployee(conn, 3);
            dumpEmployees(conn);
            deleteDatabase(conn, args[0]);
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
