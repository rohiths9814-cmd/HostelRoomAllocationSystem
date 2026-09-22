package com.hostel.server;

import com.hostel.exception.InvalidStudentDataException;
import com.hostel.exception.StudentNotFoundException;
import com.hostel.model.HostelFee;
import com.hostel.service.FeeService;
import com.sun.net.httpserver.HttpExchange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Fee REST endpoints.
 *
 * GET    /api/fees                    → all fees (optional ?status=PENDING)
 * GET    /api/fees/student/{id}       → fees for a specific student
 * POST   /api/fees                    → add fee record
 * PUT    /api/fees/{id}/pay           → mark a fee as paid
 * DELETE /api/fees/{id}               → delete fee record
 */
public class FeeHandler extends BaseHandler {

    private final FeeService feeService = new FeeService();

    @Override
    protected void handleRequest(HttpExchange exchange) throws Exception {
        String p = path(exchange);
        String m = method(exchange);

        // GET /api/fees/student/{id}
        if ("GET".equals(m) && p.matches("/api/fees/student/\\d+")) {
            int studentId = extractId(p, "/api/fees/student/");
            handleGetByStudent(exchange, studentId);
            return;
        }

        // PUT /api/fees/{id}/pay
        if ("PUT".equals(m) && p.matches("/api/fees/\\d+/pay")) {
            int id = extractId(p, "/api/fees/");
            handleMarkPaid(exchange, id);
            return;
        }

        // /api/fees/{id}
        if (p.matches("/api/fees/\\d+")) {
            int id = extractId(p, "/api/fees/");
            if ("DELETE".equals(m)) {
                handleDelete(exchange, id);
                return;
            }
            sendJson(exchange, 405, JsonUtil.errorResponse("Method not allowed"));
            return;
        }

        // /api/fees
        if (p.equals("/api/fees")) {
            switch (m) {
                case "GET":  handleGetAll(exchange); return;
                case "POST": handleCreate(exchange); return;
                default:
                    sendJson(exchange, 405, JsonUtil.errorResponse("Method not allowed"));
                    return;
            }
        }

        sendJson(exchange, 404, JsonUtil.errorResponse("Endpoint not found"));
    }

    private void handleGetAll(HttpExchange exchange) throws Exception {
        String status = queryParam(exchange, "status");
        ArrayList<HostelFee> fees;
        if (status != null && !status.trim().isEmpty()) {
            if (HostelFee.PENDING.equalsIgnoreCase(status)) {
                fees = feeService.getPendingFees();
            } else {
                fees = feeService.getPaidFees();
            }
        } else {
            fees = feeService.getAllFees();
        }
        sendJson(exchange, 200, JsonUtil.successData(fees));
    }

    private void handleGetByStudent(HttpExchange exchange, int studentId) throws Exception {
        try {
            ArrayList<HostelFee> fees = feeService.getFeesByStudent(studentId);
            sendJson(exchange, 200, JsonUtil.successData(fees));
        } catch (StudentNotFoundException e) {
            sendJson(exchange, 404, JsonUtil.errorResponse(e.getMessage()));
        }
    }

    private void handleCreate(HttpExchange exchange) throws Exception {
        HashMap<String, Object> body = readBody(exchange);
        int studentId = JsonUtil.getInt(body, "studentId", 0);
        double amount = JsonUtil.getDouble(body, "amount", 0.0);

        if (studentId == 0) {
            sendJson(exchange, 400, JsonUtil.errorResponse("studentId is required"));
            return;
        }

        try {
            int feeId = feeService.addFee(studentId, amount);
            LinkedHashMap<String, Object> data = new LinkedHashMap<>();
            data.put("feeId", feeId);
            sendJson(exchange, 201, JsonUtil.successResponse("Fee record added", data));
        } catch (StudentNotFoundException e) {
            sendJson(exchange, 404, JsonUtil.errorResponse(e.getMessage()));
        } catch (InvalidStudentDataException e) {
            sendJson(exchange, 400, JsonUtil.errorResponse(e.getMessage()));
        }
    }

    private void handleMarkPaid(HttpExchange exchange, int id) throws Exception {
        try {
            boolean result = feeService.markAsPaid(id);
            if (result) {
                sendJson(exchange, 200, JsonUtil.successResponse("Payment recorded"));
            } else {
                sendJson(exchange, 409, JsonUtil.errorResponse("Fee was already paid"));
            }
        } catch (InvalidStudentDataException e) {
            sendJson(exchange, 400, JsonUtil.errorResponse(e.getMessage()));
        }
    }

    private void handleDelete(HttpExchange exchange, int id) throws Exception {
        boolean deleted = feeService.deleteFee(id);
        if (deleted) {
            sendJson(exchange, 200, JsonUtil.successResponse("Fee record deleted"));
        } else {
            sendJson(exchange, 404, JsonUtil.errorResponse("Fee record not found"));
        }
    }
}
