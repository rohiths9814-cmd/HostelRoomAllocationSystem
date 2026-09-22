package com.hostel.server;

import com.hostel.exception.InvalidStudentDataException;
import com.hostel.exception.StudentNotFoundException;
import com.hostel.model.Complaint;
import com.hostel.service.ComplaintService;
import com.sun.net.httpserver.HttpExchange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Complaint REST endpoints.
 *
 * GET    /api/complaints             → all complaints (optional ?status=PENDING)
 * POST   /api/complaints             → create complaint
 * PUT    /api/complaints/{id}        → update status
 * DELETE /api/complaints/{id}        → delete complaint
 */
public class ComplaintHandler extends BaseHandler {

    private final ComplaintService complaintService = new ComplaintService();

    @Override
    protected void handleRequest(HttpExchange exchange) throws Exception {
        String p = path(exchange);
        String m = method(exchange);

        if (p.matches("/api/complaints/\\d+")) {
            int id = extractId(p, "/api/complaints/");
            switch (m) {
                case "PUT":    handleUpdateStatus(exchange, id); return;
                case "DELETE": handleDelete(exchange, id);       return;
                default:
                    sendJson(exchange, 405, JsonUtil.errorResponse("Method not allowed"));
                    return;
            }
        }

        if (p.equals("/api/complaints")) {
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
        ArrayList<Complaint> complaints;
        if (status != null && !status.trim().isEmpty()) {
            complaints = complaintService.getComplaintsByStatus(status.toUpperCase());
        } else {
            complaints = complaintService.getAllComplaints();
        }
        sendJson(exchange, 200, JsonUtil.successData(complaints));
    }

    private void handleCreate(HttpExchange exchange) throws Exception {
        HashMap<String, Object> body = readBody(exchange);
        int studentId = JsonUtil.getInt(body, "studentId", 0);
        String category = JsonUtil.getString(body, "category");
        String description = JsonUtil.getString(body, "description");

        if (studentId == 0) {
            sendJson(exchange, 400, JsonUtil.errorResponse("studentId is required"));
            return;
        }

        try {
            int complaintId = complaintService.createComplaint(studentId, category, description);
            LinkedHashMap<String, Object> data = new LinkedHashMap<>();
            data.put("complaintId", complaintId);
            sendJson(exchange, 201, JsonUtil.successResponse("Complaint registered", data));
        } catch (StudentNotFoundException e) {
            sendJson(exchange, 404, JsonUtil.errorResponse(e.getMessage()));
        } catch (InvalidStudentDataException e) {
            sendJson(exchange, 400, JsonUtil.errorResponse(e.getMessage()));
        }
    }

    private void handleUpdateStatus(HttpExchange exchange, int id) throws Exception {
        HashMap<String, Object> body = readBody(exchange);
        String newStatus = JsonUtil.getString(body, "status");

        if (newStatus == null || newStatus.trim().isEmpty()) {
            sendJson(exchange, 400, JsonUtil.errorResponse("status is required"));
            return;
        }

        try {
            boolean result = complaintService.updateStatus(id, newStatus.toUpperCase());
            if (result) {
                sendJson(exchange, 200, JsonUtil.successResponse("Complaint status updated"));
            } else {
                sendJson(exchange, 409, JsonUtil.errorResponse(
                        "Status update rejected (complaint may already be resolved)"));
            }
        } catch (InvalidStudentDataException e) {
            sendJson(exchange, 400, JsonUtil.errorResponse(e.getMessage()));
        }
    }

    private void handleDelete(HttpExchange exchange, int id) throws Exception {
        boolean deleted = complaintService.deleteComplaint(id);
        if (deleted) {
            sendJson(exchange, 200, JsonUtil.successResponse("Complaint deleted"));
        } else {
            sendJson(exchange, 404, JsonUtil.errorResponse("Complaint not found"));
        }
    }
}
