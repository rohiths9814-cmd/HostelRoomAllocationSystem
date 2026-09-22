package com.hostel.server;

import com.hostel.exception.InvalidStudentDataException;
import com.hostel.model.HostelBlock;
import com.hostel.service.BlockService;
import com.sun.net.httpserver.HttpExchange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Block REST endpoints.
 *
 * GET    /api/blocks          → list all blocks
 * POST   /api/blocks          → create block
 * PUT    /api/blocks/{id}     → update block
 * DELETE /api/blocks/{id}     → delete block
 */
public class BlockHandler extends BaseHandler {

    private final BlockService blockService = new BlockService();

    @Override
    protected void handleRequest(HttpExchange exchange) throws Exception {
        String p = path(exchange);
        String m = method(exchange);

        if (p.matches("/api/blocks/\\d+")) {
            int id = extractId(p, "/api/blocks/");
            switch (m) {
                case "PUT":    handleUpdate(exchange, id); return;
                case "DELETE": handleDelete(exchange, id); return;
                default:
                    sendJson(exchange, 405, JsonUtil.errorResponse("Method not allowed"));
                    return;
            }
        }

        if (p.equals("/api/blocks")) {
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
        ArrayList<HostelBlock> blocks = blockService.getAllBlocks();
        sendJson(exchange, 200, JsonUtil.successData(blocks));
    }

    private void handleCreate(HttpExchange exchange) throws Exception {
        HashMap<String, Object> body = readBody(exchange);
        HostelBlock block = new HostelBlock();
        block.setBlockName(JsonUtil.getString(body, "blockName"));
        block.setGender(JsonUtil.getString(body, "gender"));
        block.setFloors(JsonUtil.getInt(body, "floors", 1));

        try {
            int newId = blockService.addBlock(block);
            LinkedHashMap<String, Object> data = new LinkedHashMap<>();
            data.put("blockId", newId);
            sendJson(exchange, 201, JsonUtil.successResponse("Block created successfully", data));
        } catch (InvalidStudentDataException e) {
            sendJson(exchange, 400, JsonUtil.errorResponse(e.getMessage()));
        }
    }

    private void handleUpdate(HttpExchange exchange, int id) throws Exception {
        HashMap<String, Object> body = readBody(exchange);
        HostelBlock block = new HostelBlock();
        block.setBlockId(id);
        block.setBlockName(JsonUtil.getString(body, "blockName"));
        block.setGender(JsonUtil.getString(body, "gender"));
        block.setFloors(JsonUtil.getInt(body, "floors", 1));

        try {
            blockService.updateBlock(block);
            sendJson(exchange, 200, JsonUtil.successResponse("Block updated successfully"));
        } catch (InvalidStudentDataException e) {
            sendJson(exchange, 400, JsonUtil.errorResponse(e.getMessage()));
        }
    }

    private void handleDelete(HttpExchange exchange, int id) throws Exception {
        boolean deleted = blockService.deleteBlock(id);
        if (deleted) {
            sendJson(exchange, 200, JsonUtil.successResponse("Block deleted successfully"));
        } else {
            sendJson(exchange, 409, JsonUtil.errorResponse(
                    "Cannot delete: block still has rooms assigned to it"));
        }
    }
}
