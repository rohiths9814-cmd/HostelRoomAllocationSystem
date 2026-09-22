package com.hostel.server;

import com.hostel.exception.AllocationException;
import com.hostel.exception.RoomFullException;
import com.hostel.exception.RoomNotFoundException;
import com.hostel.exception.StudentNotFoundException;
import com.hostel.model.Allocation;
import com.hostel.service.AllocationService;
import com.sun.net.httpserver.HttpExchange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Allocation REST endpoints.
 *
 * GET  /api/allocations                  → active allocations
 * GET  /api/allocations/all              → all allocations (including history)
 * GET  /api/allocations/student/{id}     → allocation history for a student
 * POST /api/allocations                  → allocate a room
 * PUT  /api/allocations/transfer         → transfer student to another room
 * PUT  /api/allocations/checkout         → check student out
 */
public class AllocationHandler extends BaseHandler {

    private final AllocationService allocationService = new AllocationService();

    @Override
    protected void handleRequest(HttpExchange exchange) throws Exception {
        String p = path(exchange);
        String m = method(exchange);

        // GET /api/allocations/all
        if ("GET".equals(m) && p.equals("/api/allocations/all")) {
            ArrayList<Allocation> all = allocationService.getAllAllocations();
            sendJson(exchange, 200, JsonUtil.successData(all));
            return;
        }

        // GET /api/allocations/student/{id}
        if ("GET".equals(m) && p.matches("/api/allocations/student/\\d+")) {
            int studentId = extractId(p, "/api/allocations/student/");
            ArrayList<Allocation> history = allocationService.getStudentHistory(studentId);
            sendJson(exchange, 200, JsonUtil.successData(history));
            return;
        }

        // PUT /api/allocations/transfer
        if ("PUT".equals(m) && p.equals("/api/allocations/transfer")) {
            handleTransfer(exchange);
            return;
        }

        // PUT /api/allocations/checkout
        if ("PUT".equals(m) && p.equals("/api/allocations/checkout")) {
            handleCheckout(exchange);
            return;
        }

        // GET /api/allocations — active allocations
        if ("GET".equals(m) && p.equals("/api/allocations")) {
            ArrayList<Allocation> active = allocationService.getActiveAllocations();
            sendJson(exchange, 200, JsonUtil.successData(active));
            return;
        }

        // POST /api/allocations — allocate room
        if ("POST".equals(m) && p.equals("/api/allocations")) {
            handleAllocate(exchange);
            return;
        }

        sendJson(exchange, 404, JsonUtil.errorResponse("Endpoint not found"));
    }

    /**
     * POST /api/allocations
     * Request: { "studentId": 1, "roomId": 3 }
     *
     * This calls AllocationService.allocateRoom() which runs a
     * TRANSACTION: insert allocation + update room occupancy.
     */
    private void handleAllocate(HttpExchange exchange) throws Exception {
        HashMap<String, Object> body = readBody(exchange);
        int studentId = JsonUtil.getInt(body, "studentId", 0);
        int roomId = JsonUtil.getInt(body, "roomId", 0);

        if (studentId == 0 || roomId == 0) {
            sendJson(exchange, 400, JsonUtil.errorResponse("studentId and roomId are required"));
            return;
        }

        try {
            Allocation allocation = allocationService.allocateRoom(studentId, roomId);
            LinkedHashMap<String, Object> data = new LinkedHashMap<>();
            data.put("allocationId", allocation.getAllocationId());
            data.put("studentName", allocation.getStudentName());
            data.put("roomNumber", allocation.getRoomNumber());
            sendJson(exchange, 201, JsonUtil.successResponse("Room allocated successfully", data));

        } catch (StudentNotFoundException e) {
            sendJson(exchange, 404, JsonUtil.errorResponse(e.getMessage()));
        } catch (RoomNotFoundException e) {
            sendJson(exchange, 404, JsonUtil.errorResponse(e.getMessage()));
        } catch (RoomFullException e) {
            sendJson(exchange, 409, JsonUtil.errorResponse(e.getMessage()));
        } catch (AllocationException e) {
            sendJson(exchange, 409, JsonUtil.errorResponse(e.getMessage()));
        }
    }

    /**
     * PUT /api/allocations/transfer
     * Request: { "studentId": 1, "newRoomId": 5 }
     *
     * Calls AllocationService.transferStudent() — a 4-step transaction.
     */
    private void handleTransfer(HttpExchange exchange) throws Exception {
        HashMap<String, Object> body = readBody(exchange);
        int studentId = JsonUtil.getInt(body, "studentId", 0);
        int newRoomId = JsonUtil.getInt(body, "newRoomId", 0);

        if (studentId == 0 || newRoomId == 0) {
            sendJson(exchange, 400, JsonUtil.errorResponse("studentId and newRoomId are required"));
            return;
        }

        try {
            Allocation fresh = allocationService.transferStudent(studentId, newRoomId);
            LinkedHashMap<String, Object> data = new LinkedHashMap<>();
            data.put("allocationId", fresh.getAllocationId());
            data.put("studentName", fresh.getStudentName());
            data.put("roomNumber", fresh.getRoomNumber());
            sendJson(exchange, 200, JsonUtil.successResponse("Student transferred successfully", data));

        } catch (StudentNotFoundException e) {
            sendJson(exchange, 404, JsonUtil.errorResponse(e.getMessage()));
        } catch (RoomNotFoundException e) {
            sendJson(exchange, 404, JsonUtil.errorResponse(e.getMessage()));
        } catch (RoomFullException e) {
            sendJson(exchange, 409, JsonUtil.errorResponse(e.getMessage()));
        } catch (AllocationException e) {
            sendJson(exchange, 409, JsonUtil.errorResponse(e.getMessage()));
        }
    }

    /**
     * PUT /api/allocations/checkout
     * Request: { "studentId": 1 }
     *
     * Calls AllocationService.checkoutStudent() — a 2-step transaction.
     */
    private void handleCheckout(HttpExchange exchange) throws Exception {
        HashMap<String, Object> body = readBody(exchange);
        int studentId = JsonUtil.getInt(body, "studentId", 0);

        if (studentId == 0) {
            sendJson(exchange, 400, JsonUtil.errorResponse("studentId is required"));
            return;
        }

        try {
            boolean result = allocationService.checkoutStudent(studentId);
            if (result) {
                sendJson(exchange, 200, JsonUtil.successResponse("Checkout completed"));
            } else {
                sendJson(exchange, 409, JsonUtil.errorResponse("Checkout failed"));
            }
        } catch (StudentNotFoundException e) {
            sendJson(exchange, 404, JsonUtil.errorResponse(e.getMessage()));
        } catch (AllocationException e) {
            sendJson(exchange, 409, JsonUtil.errorResponse(e.getMessage()));
        }
    }
}
