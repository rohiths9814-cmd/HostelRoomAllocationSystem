package com.hostel.server;

import com.hostel.dao.ComplaintDAO;
import com.hostel.dao.FeeDAO;
import com.hostel.dao.RoomDAO;
import com.hostel.dao.StudentDAO;
import com.hostel.dao.WaitingListDAO;
import com.hostel.dao.AllocationDAO;
import com.hostel.model.HostelFee;
import com.hostel.model.Room;
import com.sun.net.httpserver.HttpExchange;

import java.util.LinkedHashMap;

/**
 * GET /api/dashboard
 *
 * Aggregates statistics from multiple services into a single response.
 * The React dashboard calls this once and displays all the stat cards.
 *
 * Response: {
 *   "success": true,
 *   "data": {
 *     "totalStudents": 5,
 *     "totalRooms": 7,
 *     "totalBeds": 22,
 *     "occupiedBeds": 3,
 *     "availableBeds": 19,
 *     "fullRooms": 0,
 *     "pendingComplaints": 1,
 *     "pendingFees": 2,
 *     "waitingStudents": 0,
 *     "occupancyPercent": 13.6
 *   }
 * }
 */
public class DashboardHandler extends BaseHandler {

    private final StudentDAO studentDAO = new StudentDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final AllocationDAO allocationDAO = new AllocationDAO();
    private final ComplaintDAO complaintDAO = new ComplaintDAO();
    private final FeeDAO feeDAO = new FeeDAO();
    private final WaitingListDAO waitingListDAO = new WaitingListDAO();

    @Override
    protected void handleRequest(HttpExchange exchange) throws Exception {
        if (!"GET".equals(method(exchange))) {
            sendJson(exchange, 405, JsonUtil.errorResponse("Method not allowed"));
            return;
        }

        int totalStudents = studentDAO.countAll();
        int totalRooms = roomDAO.countAll();
        int totalBeds = roomDAO.totalBeds();
        int occupiedBeds = roomDAO.totalOccupiedBeds();
        int availableBeds = totalBeds - occupiedBeds;
        int fullRooms = roomDAO.countByStatus(Room.FULL);
        int pendingComplaints = complaintDAO.countByStatus()
                .getOrDefault("PENDING", 0);
        int pendingFees = feeDAO.countByStatus(HostelFee.PENDING);
        int waitingStudents = waitingListDAO.countWaiting();

        double occupancyPercent = 0.0;
        if (totalBeds > 0) {
            occupancyPercent = (occupiedBeds * 100.0) / totalBeds;
        }

        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("totalStudents", totalStudents);
        data.put("totalRooms", totalRooms);
        data.put("totalBeds", totalBeds);
        data.put("occupiedBeds", occupiedBeds);
        data.put("availableBeds", availableBeds);
        data.put("fullRooms", fullRooms);
        data.put("pendingComplaints", pendingComplaints);
        data.put("pendingFees", pendingFees);
        data.put("waitingStudents", waitingStudents);
        data.put("occupancyPercent", occupancyPercent);

        sendJson(exchange, 200, JsonUtil.successData(data));
    }
}
