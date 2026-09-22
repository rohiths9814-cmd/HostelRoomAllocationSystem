package com.hostel.server;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Minimal JSON serializer / deserializer.
 *
 * Uses ONLY classes from java.lang and java.util — no external library.
 * It can handle the data shapes used in this project: strings, numbers,
 * booleans, nulls, arrays, and nested objects.
 *
 * WHY NOT Jackson or Gson?
 * The project constraints forbid external libraries beyond the JDBC driver.
 * This keeps the project pure Core Java.
 */
public class JsonUtil {

    // ================================================================
    // SERIALIZE: Java object → JSON string
    // ================================================================

    /** Converts any supported Java object to a JSON string. */
    public static String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof String) {
            return "\"" + escapeString((String) obj) + "\"";
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        if (obj instanceof LocalDate) {
            return "\"" + obj.toString() + "\"";
        }
        if (obj instanceof Map) {
            return mapToJson((Map<?, ?>) obj);
        }
        if (obj instanceof ArrayList) {
            return listToJson((ArrayList<?>) obj);
        }
        // For model objects, use getter-based reflection
        return objectToJson(obj);
    }

    private static String mapToJson(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(escapeString(entry.getKey().toString())).append("\":");
            sb.append(toJson(entry.getValue()));
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private static String listToJson(ArrayList<?> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(toJson(list.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Converts a Java object to JSON by calling its public getter methods.
     * A method like getName() becomes "name": "value".
     */
    private static String objectToJson(Object obj) {
        StringBuilder sb = new StringBuilder("{");
        Method[] methods = obj.getClass().getMethods();
        boolean first = true;

        for (Method m : methods) {
            String name = m.getName();
            // Skip non-getters and Object.getClass()
            if (m.getParameterCount() != 0) continue;
            if (name.equals("getClass")) continue;

            String key = null;
            if (name.startsWith("get") && name.length() > 3) {
                key = name.substring(3, 4).toLowerCase() + name.substring(4);
            } else if (name.startsWith("is") && name.length() > 2
                    && m.getReturnType() == boolean.class) {
                key = name.substring(2, 3).toLowerCase() + name.substring(3);
            }

            if (key == null) continue;

            try {
                Object value = m.invoke(obj);
                if (!first) sb.append(",");
                sb.append("\"").append(key).append("\":");
                sb.append(toJson(value));
                first = false;
            } catch (Exception e) {
                // Skip methods that fail
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private static String escapeString(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // ================================================================
    // DESERIALIZE: JSON string → Java Map
    // ================================================================

    /**
     * Parses a JSON object string into a HashMap.
     * Only handles flat objects (no nested objects) which is all
     * we need for request bodies in this project.
     */
    public static HashMap<String, Object> parseJson(String json) {
        HashMap<String, Object> map = new LinkedHashMap<>();
        if (json == null || json.trim().isEmpty()) return map;

        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) return map;

        // Strip the outer braces
        json = json.substring(1, json.length() - 1).trim();
        if (json.isEmpty()) return map;

        // Parse key-value pairs
        int i = 0;
        while (i < json.length()) {
            // Skip whitespace
            while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
            if (i >= json.length()) break;

            // Parse key (expect a quoted string)
            if (json.charAt(i) != '"') { i++; continue; }
            i++; // skip opening quote
            int keyStart = i;
            while (i < json.length() && json.charAt(i) != '"') {
                if (json.charAt(i) == '\\') i++; // skip escaped char
                i++;
            }
            String key = json.substring(keyStart, i);
            i++; // skip closing quote

            // Skip colon and whitespace
            while (i < json.length() && (json.charAt(i) == ':' || Character.isWhitespace(json.charAt(i)))) i++;

            // Parse value
            Object value = null;
            if (i < json.length()) {
                char c = json.charAt(i);
                if (c == '"') {
                    // String value
                    i++; // skip opening quote
                    StringBuilder sb = new StringBuilder();
                    while (i < json.length() && json.charAt(i) != '"') {
                        if (json.charAt(i) == '\\' && i + 1 < json.length()) {
                            i++;
                            switch (json.charAt(i)) {
                                case 'n': sb.append('\n'); break;
                                case 't': sb.append('\t'); break;
                                case '"': sb.append('"'); break;
                                case '\\': sb.append('\\'); break;
                                default: sb.append(json.charAt(i));
                            }
                        } else {
                            sb.append(json.charAt(i));
                        }
                        i++;
                    }
                    value = sb.toString();
                    i++; // skip closing quote
                } else if (c == 't' || c == 'f') {
                    // Boolean
                    if (json.substring(i).startsWith("true")) {
                        value = Boolean.TRUE;
                        i += 4;
                    } else {
                        value = Boolean.FALSE;
                        i += 5;
                    }
                } else if (c == 'n') {
                    // null
                    value = null;
                    i += 4;
                } else if (c == '-' || Character.isDigit(c)) {
                    // Number
                    int numStart = i;
                    boolean isFloat = false;
                    if (c == '-') i++;
                    while (i < json.length() && (Character.isDigit(json.charAt(i)) || json.charAt(i) == '.')) {
                        if (json.charAt(i) == '.') isFloat = true;
                        i++;
                    }
                    String numStr = json.substring(numStart, i);
                    if (isFloat) {
                        value = Double.parseDouble(numStr);
                    } else {
                        long l = Long.parseLong(numStr);
                        if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
                            value = (int) l;
                        } else {
                            value = l;
                        }
                    }
                }
            }

            map.put(key, value);

            // Skip comma
            while (i < json.length() && (json.charAt(i) == ',' || Character.isWhitespace(json.charAt(i)))) i++;
        }

        return map;
    }

    // ================================================================
    // HELPERS for extracting typed values from a parsed map
    // ================================================================

    public static String getString(HashMap<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? null : v.toString();
    }

    public static int getInt(HashMap<String, Object> map, String key, int defaultValue) {
        Object v = map.get(key);
        if (v == null) return defaultValue;
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return defaultValue; }
    }

    public static double getDouble(HashMap<String, Object> map, String key, double defaultValue) {
        Object v = map.get(key);
        if (v == null) return defaultValue;
        if (v instanceof Number) return ((Number) v).doubleValue();
        try { return Double.parseDouble(v.toString()); } catch (Exception e) { return defaultValue; }
    }

    public static boolean getBoolean(HashMap<String, Object> map, String key, boolean defaultValue) {
        Object v = map.get(key);
        if (v == null) return defaultValue;
        if (v instanceof Boolean) return (Boolean) v;
        return Boolean.parseBoolean(v.toString());
    }

    // ================================================================
    // RESPONSE BUILDERS
    // ================================================================

    /** Builds a standard success response JSON string. */
    public static String successResponse(String message) {
        HashMap<String, Object> map = new LinkedHashMap<>();
        map.put("success", Boolean.TRUE);
        map.put("message", message);
        return toJson(map);
    }

    public static String successResponse(String message, Object data) {
        HashMap<String, Object> map = new LinkedHashMap<>();
        map.put("success", Boolean.TRUE);
        map.put("message", message);
        map.put("data", data);
        return toJson(map);
    }

    public static String successData(Object data) {
        HashMap<String, Object> map = new LinkedHashMap<>();
        map.put("success", Boolean.TRUE);
        map.put("data", data);
        return toJson(map);
    }

    public static String errorResponse(String message) {
        HashMap<String, Object> map = new LinkedHashMap<>();
        map.put("success", Boolean.FALSE);
        map.put("message", message);
        return toJson(map);
    }
}
