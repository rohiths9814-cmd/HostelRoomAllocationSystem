package com.hostel.server;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

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

    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // ────── Register all endpoint handlers ──────

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

        // Use default executor (single-threaded for simplicity)
        server.setExecutor(null);
        server.start();

        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║   Hostel Management API Server                ║");
        System.out.println("║   Running on http://localhost:" + PORT + "           ║");
        System.out.println("║   Press Ctrl+C to stop                        ║");
        System.out.println("╚════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Registered endpoints:");
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
