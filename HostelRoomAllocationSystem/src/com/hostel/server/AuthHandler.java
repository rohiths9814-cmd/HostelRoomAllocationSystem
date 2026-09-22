package com.hostel.server;

import com.hostel.service.AdminService;
import com.sun.net.httpserver.HttpExchange;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * POST /api/auth/login
 *
 * Request:  { "username": "admin", "password": "admin123" }
 * Response: { "success": true, "message": "...", "admin": { "id": 1, "username": "admin" } }
 *
 * This handler calls AdminService.login() — the SAME service the console
 * menu uses. The regex validation and database check happen inside the
 * service layer, not here.
 */
public class AuthHandler extends BaseHandler {

    private final AdminService adminService = new AdminService();

    @Override
    protected void handleRequest(HttpExchange exchange) throws Exception {
        if (!"POST".equals(method(exchange))) {
            sendJson(exchange, 405, JsonUtil.errorResponse("Method not allowed"));
            return;
        }

        HashMap<String, Object> body = readBody(exchange);
        String username = JsonUtil.getString(body, "username");
        String password = JsonUtil.getString(body, "password");

        if (username == null || username.trim().isEmpty()) {
            sendJson(exchange, 400, JsonUtil.errorResponse("Username is required"));
            return;
        }
        if (password == null || password.trim().isEmpty()) {
            sendJson(exchange, 400, JsonUtil.errorResponse("Password is required"));
            return;
        }

        try {
            boolean success = adminService.login(username, password);
            if (success) {
                LinkedHashMap<String, Object> adminMap = new LinkedHashMap<>();
                adminMap.put("id", adminService.getLoggedInAdmin().getUserId());
                adminMap.put("username", adminService.getLoggedInAdmin().getUsername());

                LinkedHashMap<String, Object> response = new LinkedHashMap<>();
                response.put("success", Boolean.TRUE);
                response.put("message", "Login successful");
                response.put("admin", adminMap);

                sendJson(exchange, 200, JsonUtil.toJson(response));
            } else {
                sendJson(exchange, 401, JsonUtil.errorResponse("Invalid username or password"));
            }
        } catch (Exception e) {
            sendJson(exchange, 500, JsonUtil.errorResponse("Login failed: " + e.getMessage()));
        }
    }
}
