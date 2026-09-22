package com.hostel.server;

import com.hostel.util.DBConnection;
import com.sun.net.httpserver.HttpExchange;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;

/**
 * GET /api/health
 *
 * Every cloud host needs a cheap URL it can call every few seconds to ask
 * "is this program still alive?". If the answer stops coming, the host
 * restarts the container.
 *
 * It is also the first thing YOU should open after deploying: if this
 * works but nothing else does, the server is fine and the problem is the
 * database or CORS. That one fact saves hours of guessing.
 *
 * Response: { "success": true, "status": "UP", "database": "UP" }
 */
public class HealthHandler extends BaseHandler {

    @Override
    protected void handleRequest(HttpExchange exchange) throws Exception {

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, JsonUtil.errorResponse("Use GET"));
            return;
        }

        String databaseStatus;
        boolean databaseUp;

        try (Connection con = DBConnection.getConnection()) {
            // isValid() sends a cheap ping instead of running a real query
            databaseUp = con.isValid(5);
            databaseStatus = databaseUp ? "UP" : "DOWN";
        } catch (SQLException e) {
            databaseUp = false;
            databaseStatus = "DOWN: " + e.getMessage();
        }

        /*
         * The server itself reports UP even when the database is DOWN.
         * That is deliberate - it lets you see in one request that the
         * Java program started correctly and only the database link is
         * broken, which is by far the most common deployment problem.
         */
        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("success", Boolean.TRUE);
        body.put("status", "UP");
        body.put("database", databaseStatus);

        sendJson(exchange, databaseUp ? 200 : 503, JsonUtil.toJson(body));
    }
}
