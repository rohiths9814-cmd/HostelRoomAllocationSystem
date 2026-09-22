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

    /**
     * WHICH WEBSITES ARE ALLOWED TO CALL THIS API.
     *
     * A browser refuses to let a page at site A read a response from
     * site B unless site B says it is allowed. That permission is the
     * Access-Control-Allow-Origin header, and this is CORS.
     *
     * Locally the React dev server is http://localhost:5173, so that is
     * the default. Once the frontend is deployed its address changes to
     * something like https://hostel-frontend.vercel.app - and if this
     * list is not updated, EVERY page will show a network error even
     * though the API itself is working perfectly.
     *
     * Set the ALLOWED_ORIGINS environment variable in the cloud, as a
     * comma separated list, for example:
     *     https://my-hostel.vercel.app,http://localhost:5173
     */
    private static final String[] ALLOWED_ORIGINS = readAllowedOrigins();

    private static String[] readAllowedOrigins() {
        String fromEnv = System.getenv("ALLOWED_ORIGINS");
        if (fromEnv == null || fromEnv.trim().isEmpty()) {
            return new String[]{"http://localhost:5173", "http://localhost:4173"};
        }
        String[] parts = fromEnv.split(",");
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        return parts;
    }

    /** Used by the startup banner so you can see what was configured. */
    public static String allowedOriginDescription() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ALLOWED_ORIGINS.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(ALLOWED_ORIGINS[i]);
        }
        return sb.toString();
    }

    /**
     * Returns the origin to echo back, or null if this site is not allowed.
     *
     * "*" is accepted as a wildcard meaning "any website", which is easy
     * but means anyone can call your API from their own page.
     */
    private static String matchOrigin(String requestOrigin) {
        for (int i = 0; i < ALLOWED_ORIGINS.length; i++) {
            if ("*".equals(ALLOWED_ORIGINS[i])) {
                return "*";
            }
            if (ALLOWED_ORIGINS[i].equalsIgnoreCase(requestOrigin)) {
                return ALLOWED_ORIGINS[i];
            }
        }
        return null;
    }

    @Override
    public final void handle(HttpExchange exchange) throws IOException {
        // Add CORS headers to every response
        String requestOrigin = exchange.getRequestHeaders().getFirst("Origin");
        String allowed = (requestOrigin == null) ? null : matchOrigin(requestOrigin);

        if (allowed != null) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", allowed);
            exchange.getResponseHeaders().set("Vary", "Origin");
        }
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, X-Api-Key");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "86400");

        // Handle preflight
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        // Optional shared-secret check. Off unless API_KEY is set.
        if (!apiKeyAccepted(exchange)) {
            sendJson(exchange, 401, JsonUtil.errorResponse("Unauthorized"));
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

    /**
     * A very small shared-secret check.
     *
     * If the API_KEY environment variable is not set (your laptop), every
     * request is allowed and nothing changes. If it IS set (the cloud),
     * a request must carry the same value in an X-Api-Key header.
     *
     * BE HONEST ABOUT WHAT THIS DOES: the React app has to send the key,
     * so the key ends up inside the JavaScript that every visitor
     * downloads. Anyone who opens DevTools can read it. It stops random
     * bots and casual snooping, it does NOT make the API private.
     * Real protection needs a login that issues a token per user, which
     * is beyond the scope of this project.
     */
    private boolean apiKeyAccepted(HttpExchange exchange) {
        String expected = System.getenv("API_KEY");
        if (expected == null || expected.trim().isEmpty()) {
            return true;                       // no key configured, allow everything
        }
        // The health check must stay open or the host thinks we are dead.
        if (exchange.getRequestURI().getPath().startsWith("/api/health")) {
            return true;
        }
        String provided = exchange.getRequestHeaders().getFirst("X-Api-Key");
        return expected.trim().equals(provided);
    }

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
