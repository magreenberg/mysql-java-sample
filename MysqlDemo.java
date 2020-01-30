import java.sql.*;

class MysqlDemo {

    private static Connection getConnection(final String baseUrl, final String dbName, final String User,
            final String password) {
        Connection con = null;
        try {
            con = DriverManager.getConnection(baseUrl + "/" + dbName, User, password);
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

    private static void createEmployees(Connection conn) throws SQLException {
        String sqlCreate = "CREATE TABLE IF NOT EXISTS employees" + "  (id    integer not null,"
                + "   first_name varchar(255) not null, " + "   last_name varchar(255) not null," + "   email_address varchar(255) not null, primary key(id) "
                + ")";

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

    public static void main(final String args[]) {
        final Connection conn = getConnection("jdbc:mysql://localhost:3306", "demo", "root", "cpSKeh4Fd4OWwin1");

        try {
            createEmployees(conn);
            addEmployee(conn, 1, "David", "BenGurion", "dbg@negev.il");
            addEmployee(conn, 2, "Theodore", "Herzl", "ted@basil.eu");

            final Statement stmt = conn.createStatement();
            final ResultSet rs = stmt.executeQuery("select * from employees");
            while (rs.next())
                System.out.println(
                        rs.getInt(1) + "  " + rs.getString(2) + "  " + rs.getString(3) + "  " + rs.getString(4));
            conn.close();
        } catch (final Exception e) {
            System.out.println(e);
        }
    }
}