package com.hostel.server;

import com.hostel.dao.*;
import com.hostel.model.*;
import com.sun.net.httpserver.HttpExchange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Report REST endpoints.
 *
 * GET /api/reports/summary          → overall statistics
 * GET /api/reports/block-vacancy    → vacancy by block
 * GET /api/reports/complaint-stats  → complaint count by status and category
 * GET /api/reports/fee-stats        → fee totals (pending, collected)
 */
public class ReportHandler extends BaseHandler {

    private final StudentDAO studentDAO = new StudentDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final BlockDAO blockDAO = new BlockDAO();
    private final AllocationDAO allocationDAO = new AllocationDAO();
    private final ComplaintDAO complaintDAO = new ComplaintDAO();
    private final FeeDAO feeDAO = new FeeDAO();
    private final WaitingListDAO waitingListDAO = new WaitingListDAO();

    @Override
    protected void handleRequest(HttpExchange exchange) throws Exception {
        String p = path(exchange);
        if (!"GET".equals(method(exchange))) {
            sendJson(exchange, 405, JsonUtil.errorResponse("Method not allowed"));
            return;
        }

        switch (p) {
            case "/api/reports/summary":         handleSummary(exchange);        return;
            case "/api/reports/block-vacancy":    handleBlockVacancy(exchange);   return;
            case "/api/reports/complaint-stats":  handleComplaintStats(exchange); return;
            case "/api/reports/fee-stats":        handleFeeStats(exchange);       return;
            default:
                sendJson(exchange, 404, JsonUtil.errorResponse("Report not found"));
        }
    }

    private void handleSummary(HttpExchange exchange) throws Exception {
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("totalStudents", studentDAO.countAll());
        data.put("totalBlocks", blockDAO.countAll());
        data.put("totalRooms", roomDAO.countAll());
        data.put("availableRooms", roomDAO.countByStatus(Room.AVAILABLE));
        data.put("partialRooms", roomDAO.countByStatus(Room.PARTIALLY_OCCUPIED));
        data.put("fullRooms", roomDAO.countByStatus(Room.FULL));
        data.put("maintenanceRooms", roomDAO.countByStatus(Room.MAINTENANCE));
        int totalBeds = roomDAO.totalBeds();
        int occupied = roomDAO.totalOccupiedBeds();
        data.put("totalBeds", totalBeds);
        data.put("occupiedBeds", occupied);
        data.put("freeBeds", totalBeds - occupied);
        data.put("activeAllocations", allocationDAO.countActive());
        data.put("waitingStudents", waitingListDAO.countWaiting());
        double pct = totalBeds > 0 ? (occupied * 100.0) / totalBeds : 0.0;
        data.put("occupancyPercent", pct);

        sendJson(exchange, 200, JsonUtil.successData(data));
    }

    private void handleBlockVacancy(HttpExchange exchange) throws Exception {
        ArrayList<Room> rooms = roomDAO.findAll();
        HashMap<Integer, HostelBlock> blockMap = blockDAO.findAllAsMap();

        // Aggregate: blockId -> [totalBeds, occupiedBeds]
        HashMap<Integer, int[]> totals = new HashMap<>();
        for (int i = 0; i < rooms.size(); i++) {
            Room r = rooms.get(i);
            int[] arr = totals.get(r.getBlockId());
            if (arr == null) {
                arr = new int[]{0, 0};
                totals.put(r.getBlockId(), arr);
            }
            arr[0] += r.getCapacity();
            arr[1] += r.getOccupiedBeds();
        }

        ArrayList<LinkedHashMap<String, Object>> result = new ArrayList<>();
        for (HashMap.Entry<Integer, int[]> entry : totals.entrySet()) {
            HostelBlock block = blockMap.get(entry.getKey());
            int[] counts = entry.getValue();
            LinkedHashMap<String, Object> row = new LinkedHashMap<>();
            row.put("blockId", entry.getKey());
            row.put("blockName", block != null ? block.getBlockName() : "Block " + entry.getKey());
            row.put("gender", block != null ? block.getGender() : "-");
            row.put("totalBeds", counts[0]);
            row.put("occupiedBeds", counts[1]);
            row.put("freeBeds", counts[0] - counts[1]);
            result.add(row);
        }

        sendJson(exchange, 200, JsonUtil.successData(result));
    }

    private void handleComplaintStats(HttpExchange exchange) throws Exception {
        HashMap<String, Integer> byStatus = complaintDAO.countByStatus();
        HashMap<String, Integer> byCategory = complaintDAO.countByCategory();
        int total = complaintDAO.countAll();

        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("total", total);
        data.put("byStatus", byStatus);
        data.put("byCategory", byCategory);

        sendJson(exchange, 200, JsonUtil.successData(data));
    }

    private void handleFeeStats(HttpExchange exchange) throws Exception {
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("pendingCount", feeDAO.countByStatus(HostelFee.PENDING));
        data.put("paidCount", feeDAO.countByStatus(HostelFee.PAID));
        data.put("totalPending", feeDAO.totalPendingAmount());
        data.put("totalCollected", feeDAO.totalCollectedAmount());

        sendJson(exchange, 200, JsonUtil.successData(data));
    }
}
