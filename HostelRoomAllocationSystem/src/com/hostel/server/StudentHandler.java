package com.hostel.server;

import com.hostel.exception.DuplicateRegisterNumberException;
import com.hostel.exception.InvalidStudentDataException;
import com.hostel.exception.StudentNotFoundException;
import com.hostel.model.Student;
import com.hostel.service.StudentService;
import com.sun.net.httpserver.HttpExchange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Student REST endpoints.
 *
 * GET    /api/students              → list all (optional ?search=keyword)
 * GET    /api/students/unallocated  → students without a room
 * GET    /api/students/{id}         → single student
 * POST   /api/students              → create
 * PUT    /api/students/{id}         → update
 * DELETE /api/students/{id}         → delete
 */
public class StudentHandler extends BaseHandler {

    private final StudentService studentService = new StudentService();

    @Override
    protected void handleRequest(HttpExchange exchange) throws Exception {
        String p = path(exchange);
        String m = method(exchange);

        // GET /api/students/unallocated
        if ("GET".equals(m) && p.equals("/api/students/unallocated")) {
            handleGetUnallocated(exchange);
            return;
        }

        // Routes with an ID: /api/students/{id}
        if (p.matches("/api/students/\\d+")) {
            int id = extractId(p, "/api/students/");
            switch (m) {
                case "GET":    handleGetById(exchange, id); return;
                case "PUT":    handleUpdate(exchange, id);  return;
                case "DELETE": handleDelete(exchange, id);  return;
                default:
                    sendJson(exchange, 405, JsonUtil.errorResponse("Method not allowed"));
                    return;
            }
        }

        // Routes without an ID: /api/students
        if (p.equals("/api/students")) {
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
        String search = queryParam(exchange, "search");
        ArrayList<Student> students;
        if (search != null && !search.trim().isEmpty()) {
            students = studentService.searchStudents(search);
        } else {
            students = studentService.getAllStudents();
        }
        sendJson(exchange, 200, JsonUtil.successData(students));
    }

    private void handleGetById(HttpExchange exchange, int id) throws Exception {
        try {
            Student student = studentService.getStudentById(id);
            sendJson(exchange, 200, JsonUtil.successData(student));
        } catch (StudentNotFoundException e) {
            sendJson(exchange, 404, JsonUtil.errorResponse(e.getMessage()));
        }
    }

    private void handleGetUnallocated(HttpExchange exchange) throws Exception {
        ArrayList<Student> students = studentService.getStudentsWithoutRoom();
        sendJson(exchange, 200, JsonUtil.successData(students));
    }

    private void handleCreate(HttpExchange exchange) throws Exception {
        HashMap<String, Object> body = readBody(exchange);
        Student student = mapToStudent(body);

        try {
            int newId = studentService.addStudent(student);

            LinkedHashMap<String, Object> data = new LinkedHashMap<>();
            data.put("studentId", newId);
            sendJson(exchange, 201, JsonUtil.successResponse("Student created successfully", data));

        } catch (InvalidStudentDataException e) {
            sendJson(exchange, 400, JsonUtil.errorResponse(e.getMessage()));
        } catch (DuplicateRegisterNumberException e) {
            sendJson(exchange, 409, JsonUtil.errorResponse(e.getMessage()));
        }
    }

    private void handleUpdate(HttpExchange exchange, int id) throws Exception {
        HashMap<String, Object> body = readBody(exchange);
        Student student = mapToStudent(body);
        student.setStudentId(id);

        try {
            studentService.updateStudent(student);
            sendJson(exchange, 200, JsonUtil.successResponse("Student updated successfully"));

        } catch (InvalidStudentDataException e) {
            sendJson(exchange, 400, JsonUtil.errorResponse(e.getMessage()));
        } catch (StudentNotFoundException e) {
            sendJson(exchange, 404, JsonUtil.errorResponse(e.getMessage()));
        }
    }

    private void handleDelete(HttpExchange exchange, int id) throws Exception {
        try {
            boolean deleted = studentService.deleteStudent(id);
            if (deleted) {
                sendJson(exchange, 200, JsonUtil.successResponse("Student deleted successfully"));
            } else {
                sendJson(exchange, 409, JsonUtil.errorResponse(
                        "Cannot delete: student has an active room allocation. Check out first."));
            }
        } catch (StudentNotFoundException e) {
            sendJson(exchange, 404, JsonUtil.errorResponse(e.getMessage()));
        }
    }

    /** Maps a JSON body HashMap to a Student object. */
    private Student mapToStudent(HashMap<String, Object> body) {
        Student s = new Student();
        s.setRegisterNumber(JsonUtil.getString(body, "registerNumber"));
        s.setName(JsonUtil.getString(body, "name"));
        s.setDepartment(JsonUtil.getString(body, "department"));
        s.setYear(JsonUtil.getInt(body, "year", 1));
        s.setGender(JsonUtil.getString(body, "gender"));
        s.setPhone(JsonUtil.getString(body, "phone"));
        s.setEmail(JsonUtil.getString(body, "email"));
        String address = JsonUtil.getString(body, "address");
        s.setAddress(address != null ? address : "");
        return s;
    }
}
