package com.hostel.server;

import com.hostel.util.DBConnection;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * ApiServer — the HTTP entry point for the React frontend.
 *
 * Uses Java's built-in HttpServer (com.sun.net.httpserver) so there are
 * ZERO external dependencies beyond the MySQL JDBC driver.
 *
 * To start:
 *   javac -d bin -sourcepath src src\com\hostel\server\ApiServer.java
 *   java -ea -cp "bin;lib\*" com.hostel.server.ApiServer
 *
 * The server runs on port 8080 and exposes all REST endpoints
 * that the React frontend calls.
 *
 * NOTE: This is a SEPARATE entry point from HostelApp.java.
 * - Run HostelApp to use the console menu.
 * - Run ApiServer to serve the React frontend.
 * Both use the SAME Service → DAO → JDBC → MySQL path.
 */
public class ApiServer {

    /**
     * The port to listen on.
     *
     * A cloud host does NOT let you choose your own port. It picks one,
     * puts it in the PORT environment variable, and routes public traffic
     * to it. If we ignored that and hard-coded 8080, the host would send
     * requests to a port nothing is listening on, and every request would
     * fail with "502 Bad Gateway".
     *
     * On your own computer PORT is not set, so we fall back to 8080.
     */
    private static int resolvePort() {
        String fromEnv = System.getenv("PORT");
        if (fromEnv != null && !fromEnv.trim().isEmpty()) {
            try {
                return Integer.parseInt(fromEnv.trim());
            } catch (NumberFormatException e) {
                System.out.println("PORT was not a number (" + fromEnv + "), using 8080.");
            }
        }
        return 8080;
    }

    private static final int PORT = resolvePort();

    public static void main(String[] args) throws IOException {
        /*
         * "0.0.0.0" means "accept connections on every network interface".
         * The default binds to localhost only, which works on your laptop
         * but makes the server unreachable inside a container - the host
         * would never be able to forward traffic to it.
         */
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);

        // ────── Register all endpoint handlers ──────

        // Health check - the cloud host calls this to see if we are alive
        server.createContext("/api/health", new HealthHandler());

        // Auth
        server.createContext("/api/auth/login", new AuthHandler());

        // Dashboard
        server.createContext("/api/dashboard", new DashboardHandler());

        // Students
        server.createContext("/api/students", new StudentHandler());

        // Blocks
        server.createContext("/api/blocks", new BlockHandler());

        // Rooms
        server.createContext("/api/rooms", new RoomHandler());

        // Allocations
        server.createContext("/api/allocations", new AllocationHandler());

        // Complaints
        server.createContext("/api/complaints", new ComplaintHandler());

        // Fees
        server.createContext("/api/fees", new FeeHandler());

        // Waiting List
        server.createContext("/api/waiting-list", new WaitingListHandler());

        // Reports
        server.createContext("/api/reports", new ReportHandler());

        /*
         * A small thread pool instead of the default single thread.
         * With setExecutor(null) every request is served one at a time,
         * so one slow database query blocks everybody else. Ten threads
         * is plenty for a college project and stops the health check
         * from timing out while a report is running.
         */
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("=================================================");
        System.out.println("   Hostel Management API Server");
        System.out.println("   Listening on port " + PORT);
        System.out.println("=================================================");
        DBConnection.printConfig();
        System.out.println("Allowed browser origin: " + BaseHandler.allowedOriginDescription());
        System.out.println();
        System.out.println("Registered endpoints:");
        System.out.println("  GET    /api/health");
        System.out.println("  POST   /api/auth/login");
        System.out.println("  GET    /api/dashboard");
        System.out.println("  CRUD   /api/students");
        System.out.println("  CRUD   /api/blocks");
        System.out.println("  CRUD   /api/rooms");
        System.out.println("  GET/POST/PUT  /api/allocations");
        System.out.println("  CRUD   /api/complaints");
        System.out.println("  CRUD   /api/fees");
        System.out.println("  GET/POST/DEL  /api/waiting-list");
        System.out.println("  GET    /api/reports/*");
    }
}
