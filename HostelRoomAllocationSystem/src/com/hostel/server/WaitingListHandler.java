package com.hostel.server;

import com.hostel.exception.AllocationException;
import com.hostel.exception.StudentNotFoundException;
import com.hostel.model.WaitingEntry;
import com.hostel.service.WaitingListService;
import com.sun.net.httpserver.HttpExchange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Waiting list REST endpoints.
 *
 * GET    /api/waiting-list         → list all waiting entries
 * POST   /api/waiting-list         → add student to waiting list
 * DELETE /api/waiting-list/{id}    → remove from waiting list
 */
public class WaitingListHandler extends BaseHandler {

    private final WaitingListService waitingListService = new WaitingListService();

    @Override
    protected void handleRequest(HttpExchange exchange) throws Exception {
        String p = path(exchange);
        String m = method(exchange);

        if (p.matches("/api/waiting-list/\\d+")) {
            int id = extractId(p, "/api/waiting-list/");
            if ("DELETE".equals(m)) {
                handleRemove(exchange, id);
                return;
            }
            sendJson(exchange, 405, JsonUtil.errorResponse("Method not allowed"));
            return;
        }

        if (p.equals("/api/waiting-list")) {
            switch (m) {
                case "GET":  handleGetAll(exchange); return;
                case "POST": handleAdd(exchange);    return;
                default:
                    sendJson(exchange, 405, JsonUtil.errorResponse("Method not allowed"));
                    return;
            }
        }

        sendJson(exchange, 404, JsonUtil.errorResponse("Endpoint not found"));
    }

    private void handleGetAll(HttpExchange exchange) throws Exception {
        ArrayList<WaitingEntry> entries = waitingListService.getWaitingList();
        sendJson(exchange, 200, JsonUtil.successData(entries));
    }

    private void handleAdd(HttpExchange exchange) throws Exception {
        HashMap<String, Object> body = readBody(exchange);
        int studentId = JsonUtil.getInt(body, "studentId", 0);
        int priority = JsonUtil.getInt(body, "priority", 5);

        if (studentId == 0) {
            sendJson(exchange, 400, JsonUtil.errorResponse("studentId is required"));
            return;
        }

        try {
            int waitingId = waitingListService.addToWaitingList(studentId, priority);
            LinkedHashMap<String, Object> data = new LinkedHashMap<>();
            data.put("waitingId", waitingId);
            sendJson(exchange, 201, JsonUtil.successResponse("Added to waiting list", data));
        } catch (StudentNotFoundException e) {
            sendJson(exchange, 404, JsonUtil.errorResponse(e.getMessage()));
        } catch (AllocationException e) {
            sendJson(exchange, 409, JsonUtil.errorResponse(e.getMessage()));
        }
    }

    private void handleRemove(HttpExchange exchange, int id) throws Exception {
        boolean removed = waitingListService.removeFromWaitingList(id);
        if (removed) {
            sendJson(exchange, 200, JsonUtil.successResponse("Removed from waiting list"));
        } else {
            sendJson(exchange, 404, JsonUtil.errorResponse("Waiting list entry not found"));
        }
    }
}
