package com.hostel.server;

import com.hostel.exception.InvalidStudentDataException;
import com.hostel.exception.RoomNotFoundException;
import com.hostel.model.Allocation;
import com.hostel.model.HostelBlock;
import com.hostel.model.Room;
import com.hostel.service.AllocationService;
import com.hostel.service.BlockService;
import com.hostel.service.RoomService;
import com.sun.net.httpserver.HttpExchange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Room REST endpoints.
 *
 * GET    /api/rooms                    → list all (optional ?status=AVAILABLE)
 * GET    /api/rooms/available          → rooms with free beds
 * GET    /api/rooms/{id}               → single room
 * GET    /api/rooms/{id}/occupants     → who lives in this room
 * POST   /api/rooms                    → create
 * PUT    /api/rooms/{id}               → update
 * PUT    /api/rooms/{id}/maintenance   → toggle maintenance
 * DELETE /api/rooms/{id}               → delete
 */
public class RoomHandler extends BaseHandler {

    private final RoomService roomService = new RoomService();
    private final BlockService blockService = new BlockService();
    private final AllocationService allocationService = new AllocationService();

    @Override
    protected void handleRequest(HttpExchange exchange) throws Exception {
        String p = path(exchange);
        String m = method(exchange);

        // GET /api/rooms/available
        if ("GET".equals(m) && p.equals("/api/rooms/available")) {
            handleGetAvailable(exchange);
            return;
        }

        // /api/rooms/{id}/occupants
        if (p.matches("/api/rooms/\\d+/occupants") && "GET".equals(m)) {
            int id = extractId(p, "/api/rooms/");
            handleGetOccupants(exchange, id);
            return;
        }

        // /api/rooms/{id}/maintenance
        if (p.matches("/api/rooms/\\d+/maintenance") && "PUT".equals(m)) {
            int id = extractId(p, "/api/rooms/");
            handleSetMaintenance(exchange, id);
            return;
        }

        // Routes with an ID: /api/rooms/{id}
        if (p.matches("/api/rooms/\\d+")) {
            int id = extractId(p, "/api/rooms/");
            switch (m) {
                case "GET":    handleGetById(exchange, id); return;
                case "PUT":    handleUpdate(exchange, id);  return;
                case "DELETE": handleDelete(exchange, id);  return;
                default:
                    sendJson(exchange, 405, JsonUtil.errorResponse("Method not allowed"));
                    return;
            }
        }

        // Routes without an ID: /api/rooms
        if (p.equals("/api/rooms")) {
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
        ArrayList<Room> rooms;
        if (status != null && !status.trim().isEmpty()) {
            rooms = roomService.getRoomsByStatus(status.toUpperCase());
        } else {
            rooms = roomService.getAllRooms();
        }

        // Enrich rooms with block names
        HashMap<Integer, HostelBlock> blockMap = blockService.getBlockMap();
        ArrayList<LinkedHashMap<String, Object>> enriched = new ArrayList<>();
        for (int i = 0; i < rooms.size(); i++) {
            enriched.add(enrichRoom(rooms.get(i), blockMap));
        }

        sendJson(exchange, 200, JsonUtil.successData(enriched));
    }

    private void handleGetAvailable(HttpExchange exchange) throws Exception {
        ArrayList<Room> rooms = roomService.getRoomsWithFreeBed();
        HashMap<Integer, HostelBlock> blockMap = blockService.getBlockMap();
        ArrayList<LinkedHashMap<String, Object>> enriched = new ArrayList<>();
        for (int i = 0; i < rooms.size(); i++) {
            enriched.add(enrichRoom(rooms.get(i), blockMap));
        }
        sendJson(exchange, 200, JsonUtil.successData(enriched));
    }

    private void handleGetById(HttpExchange exchange, int id) throws Exception {
        try {
            Room room = roomService.getRoomById(id);
            HashMap<Integer, HostelBlock> blockMap = blockService.getBlockMap();
            sendJson(exchange, 200, JsonUtil.successData(enrichRoom(room, blockMap)));
        } catch (RoomNotFoundException e) {
            sendJson(exchange, 404, JsonUtil.errorResponse(e.getMessage()));
        }
    }

    private void handleGetOccupants(HttpExchange exchange, int roomId) throws Exception {
        ArrayList<Allocation> occupants = allocationService.getRoomOccupants(roomId);
        sendJson(exchange, 200, JsonUtil.successData(occupants));
    }

    private void handleCreate(HttpExchange exchange) throws Exception {
        HashMap<String, Object> body = readBody(exchange);
        Room room = new Room();
        room.setRoomNumber(JsonUtil.getString(body, "roomNumber"));
        room.setBlockId(JsonUtil.getInt(body, "blockId", 0));
        room.setFloor(JsonUtil.getInt(body, "floor", 1));
        room.setCapacity(JsonUtil.getInt(body, "capacity", 1));

        try {
            int newId = roomService.addRoom(room);
            LinkedHashMap<String, Object> data = new LinkedHashMap<>();
            data.put("roomId", newId);
            sendJson(exchange, 201, JsonUtil.successResponse("Room created successfully", data));
        } catch (InvalidStudentDataException e) {
            sendJson(exchange, 400, JsonUtil.errorResponse(e.getMessage()));
        }
    }

    private void handleUpdate(HttpExchange exchange, int id) throws Exception {
        HashMap<String, Object> body = readBody(exchange);
        Room room = new Room();
        room.setRoomId(id);
        room.setRoomNumber(JsonUtil.getString(body, "roomNumber"));
        room.setBlockId(JsonUtil.getInt(body, "blockId", 0));
        room.setFloor(JsonUtil.getInt(body, "floor", 1));
        room.setCapacity(JsonUtil.getInt(body, "capacity", 1));
        String status = JsonUtil.getString(body, "status");
        if (status != null) room.setStatus(status);

        try {
            roomService.updateRoom(room);
            sendJson(exchange, 200, JsonUtil.successResponse("Room updated successfully"));
        } catch (InvalidStudentDataException e) {
            sendJson(exchange, 400, JsonUtil.errorResponse(e.getMessage()));
        } catch (RoomNotFoundException e) {
            sendJson(exchange, 404, JsonUtil.errorResponse(e.getMessage()));
        }
    }

    private void handleSetMaintenance(HttpExchange exchange, int id) throws Exception {
        HashMap<String, Object> body = readBody(exchange);
        boolean under = JsonUtil.getBoolean(body, "underMaintenance", true);

        try {
            boolean result = roomService.setMaintenance(id, under);
            if (result) {
                sendJson(exchange, 200, JsonUtil.successResponse(
                        under ? "Room set to maintenance" : "Room maintenance cleared"));
            } else {
                sendJson(exchange, 409, JsonUtil.errorResponse(
                        "Cannot set maintenance: students are still in the room"));
            }
        } catch (RoomNotFoundException e) {
            sendJson(exchange, 404, JsonUtil.errorResponse(e.getMessage()));
        }
    }

    private void handleDelete(HttpExchange exchange, int id) throws Exception {
        try {
            boolean deleted = roomService.deleteRoom(id);
            if (deleted) {
                sendJson(exchange, 200, JsonUtil.successResponse("Room deleted successfully"));
            } else {
                sendJson(exchange, 409, JsonUtil.errorResponse(
                        "Cannot delete: room still has students or active allocations"));
            }
        } catch (RoomNotFoundException e) {
            sendJson(exchange, 404, JsonUtil.errorResponse(e.getMessage()));
        }
    }

    /** Adds blockName and availableBeds to the room data. */
    private LinkedHashMap<String, Object> enrichRoom(Room room, HashMap<Integer, HostelBlock> blockMap) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("roomId", room.getRoomId());
        map.put("roomNumber", room.getRoomNumber());
        map.put("blockId", room.getBlockId());
        HostelBlock block = blockMap.get(room.getBlockId());
        map.put("blockName", block != null ? block.getBlockName() : "");
        map.put("blockGender", block != null ? block.getGender() : "");
        map.put("floor", room.getFloor());
        map.put("capacity", room.getCapacity());
        map.put("occupiedBeds", room.getOccupiedBeds());
        map.put("availableBeds", room.getCapacity() - room.getOccupiedBeds());
        map.put("status", room.getStatus());
        return map;
    }
}
