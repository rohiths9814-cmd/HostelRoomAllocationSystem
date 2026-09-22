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
    //  SETTINGS
    //
    //  Each setting is read from an ENVIRONMENT VARIABLE first, and
    //  falls back to the local value if that variable is not set.
    //
    //  Why? Because a cloud host (Railway, Render, AWS...) does not let
    //  you edit .java files - it hands your program its database details
    //  through environment variables at startup. Reading them this way
    //  means the SAME code runs on your laptop and in the cloud, with no
    //  password ever committed to git.
    //
    //  On your own computer you can ignore all of this: no environment
    //  variables are set, so the defaults below are used.
    //  CHANGE THE DEFAULTS to match your local MySQL.
    // ================================================================

    private static final String HOST = env("DB_HOST", "MYSQLHOST", "localhost");
    private static final String PORT = env("DB_PORT", "MYSQLPORT", "3306");
    private static final String DATABASE = env("DB_NAME", "MYSQLDATABASE", "hostel_management");
    private static final String USERNAME = env("DB_USER", "MYSQLUSER", "root");
    private static final String PASSWORD = env("DB_PASSWORD", "MYSQLPASSWORD", "admin123");

    /**
     * DISABLED on a local machine, REQUIRED when the database is reached
     * over the public internet. Set DB_SSL_MODE=REQUIRED in the cloud.
     */
    private static final String SSL_MODE = env("DB_SSL_MODE", null, "DISABLED");

    /**
     * Reads a setting from the environment.
     *
     * primaryName   - the name we chose, e.g. DB_HOST
     * railwayName   - the name Railway's MySQL plugin uses, e.g. MYSQLHOST
     *                 (pass null when there is no such variable)
     * fallback      - what to use on a machine where neither is set
     *
     * Checking both names means Railway works with no extra configuration,
     * while any other host can use the plain DB_* names.
     */
    private static String env(String primaryName, String railwayName, String fallback) {
        String value = System.getenv(primaryName);
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }
        if (railwayName != null) {
            value = System.getenv(railwayName);
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return fallback;
    }

    /**
     * The JDBC URL.
     *
     * If JDBC_URL is set it is used exactly as given - useful when a host
     * hands you one complete connection string. Otherwise it is built
     * from the settings above.
     *
     * The two connection parameters are not decoration - without them
     * MySQL 8 refuses to let us in:
     *
     * sslMode
     *     DISABLED turns off TLS, which is fine when the database is on
     *     the same machine. Over the public internet use REQUIRED, or
     *     anybody between you and the server can read the traffic.
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
     *     This setting permits it. It is only needed when TLS is off.
     */
    private static final String URL = buildUrl();

    private static String buildUrl() {
        String override = System.getenv("JDBC_URL");
        if (override != null && !override.trim().isEmpty()) {
            return override.trim();
        }
        return "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
                + "?sslMode=" + SSL_MODE
                + "&allowPublicKeyRetrieval=true"
                + "&connectTimeout=10000";
    }

    /** Prints where we are connecting, WITHOUT ever printing the password. */
    public static void printConfig() {
        System.out.println("Database host : " + HOST + ":" + PORT);
        System.out.println("Database name : " + DATABASE);
        System.out.println("Database user : " + USERNAME);
        System.out.println("SSL mode      : " + SSL_MODE);
    }

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
