package com.hostel.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * The single place where a JDBC connection to MySQL is created.
 *
 * Why one class?  If the password or the database name changes, we edit
 * ONE file instead of hunting through every DAO.
 *
 * The 4 steps of JDBC:
 *   1. Load the driver            -> Class.forName(...)
 *   2. Open a connection          -> DriverManager.getConnection(...)
 *   3. Create + execute statement -> PreparedStatement / Statement
 *   4. Close everything           -> close()
 * Steps 1 and 2 are here. Steps 3 and 4 happen inside the DAO classes.
 */
public class DBConnection {

    // ================================================================
    //  CHANGE THESE THREE LINES WHEN YOU MOVE TO ANOTHER COMPUTER
    // ================================================================
    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DATABASE = "hostel_management";

    private static final String USERNAME = "root";
    private static final String PASSWORD = "admin123";
    // ================================================================

    /**
     * The JDBC URL, built from the settings above.
     *
     * The two connection parameters are not decoration - without them
     * MySQL 8 refuses to let us in:
     *
     * sslMode=DISABLED
     *     Turns off TLS. A local college project does not need an
     *     encrypted connection, and this keeps the setup simple.
     *     (The old way of writing this was useSSL=false, which is
     *     deprecated and ignored by newer drivers.)
     *
     * allowPublicKeyRetrieval=true
     *     MySQL 8 logs users in with "caching_sha2_password". The first
     *     time an account connects after the server starts, the driver
     *     must encrypt the password using the server's RSA public key,
     *     so it has to ask the server for that key. The driver refuses
     *     to do that by default, and you get the confusing error
     *     "Public Key Retrieval is not allowed".
     *     This setting permits it. It is safe here because the database
     *     is on the same machine - over a real network you would use
     *     TLS instead of turning this on.
     */
    private static final String URL =
            "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
            + "?sslMode=DISABLED"
            + "&allowPublicKeyRetrieval=true";

    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    /**
     * A static block runs ONCE, when the class is first loaded by the JVM.
     * That is the perfect place to register the MySQL driver.
     */
    static {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println("FATAL: MySQL JDBC driver not found on the classpath.");
            System.out.println("Add mysql-connector-j-x.x.x.jar to the lib folder and");
            System.out.println("include it with -cp when you compile and run.");
        }
    }

    /**
     * Opens and returns a new connection.
     * The CALLER is responsible for closing it (we use try-with-resources
     * in the DAO classes so it closes automatically).
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    /**
     * A quick self-test used by the "Test database connection" menu option.
     * Demonstrates a plain Statement (no user input, so no injection risk)
     * and a ResultSet.
     */
    public static boolean testConnection() {
        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT DATABASE(), VERSION()")) {

            if (rs.next()) {
                System.out.println("Connected to database : " + rs.getString(1));
                System.out.println("MySQL server version  : " + rs.getString(2));
                return true;
            }
            return false;

        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
            return false;
        }
    }
}
