package com.hostel.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * Base class for all API endpoint handlers.
 *
 * Provides:
 *   - CORS headers on every response (React runs on a different port)
 *   - OPTIONS preflight handling
 *   - JSON request body parsing
 *   - JSON response sending
 *   - Centralized error handling
 *
 * Each subclass overrides handleRequest() and focuses only on the
 * business logic, never worrying about CORS or JSON plumbing.
 */
public abstract class BaseHandler implements HttpHandler {

    /** React dev server origin. */
    private static final String ALLOWED_ORIGIN = "http://localhost:5173";

    @Override
    public final void handle(HttpExchange exchange) throws IOException {
        // Add CORS headers to every response
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", ALLOWED_ORIGIN);
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "86400");

        // Handle preflight
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        try {
            handleRequest(exchange);
        } catch (Exception e) {
            System.err.println("[ERROR] " + exchange.getRequestURI() + " - " + e.getMessage());
            sendJson(exchange, 500, JsonUtil.errorResponse(
                    "Internal server error: " + e.getMessage()));
        }
    }

    /** Subclasses implement this to handle the actual request. */
    protected abstract void handleRequest(HttpExchange exchange) throws Exception;

    // ================================================================
    // HELPERS
    // ================================================================

    /** Reads and parses the JSON request body into a HashMap. */
    protected HashMap<String, Object> readBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        return JsonUtil.parseJson(body);
    }

    /** Sends a JSON response with the given HTTP status code. */
    protected void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    /** Returns the HTTP method (GET, POST, PUT, DELETE). */
    protected String method(HttpExchange exchange) {
        return exchange.getRequestMethod().toUpperCase();
    }

    /** Returns the request path without query string. */
    protected String path(HttpExchange exchange) {
        return exchange.getRequestURI().getPath();
    }

    /** Returns the query string, or empty string if none. */
    protected String query(HttpExchange exchange) {
        String q = exchange.getRequestURI().getQuery();
        return q == null ? "" : q;
    }

    /**
     * Extracts a path parameter.
     * For example, extractId("/api/students/5", "/api/students/") returns 5.
     */
    protected int extractId(String fullPath, String prefix) {
        String remainder = fullPath.substring(prefix.length());
        // Remove any trailing path segments
        int slash = remainder.indexOf('/');
        if (slash >= 0) {
            remainder = remainder.substring(0, slash);
        }
        return Integer.parseInt(remainder);
    }

    /** Extracts a query parameter value by name. */
    protected String queryParam(HttpExchange exchange, String name) {
        String q = query(exchange);
        if (q.isEmpty()) return null;
        for (String pair : q.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equals(name)) {
                return java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}
