import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FirebaseManager {
    private static final int CONNECT_TIMEOUT_MS = 6000;
    private static final int READ_TIMEOUT_MS = 8000;

    public static boolean testConnection(FirebaseConfig config) {
        if (config == null || !config.isConfigured()) {
            return false;
        }

        HttpURLConnection conn = null;
        try {
            String urlStr = getEndpointUrl(config, "students.json");
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setRequestProperty("Accept", "application/json");

            int code = conn.getResponseCode();
            return code == 200 || code == 401 || code == 403;
        } catch (Exception e) {
            System.err.println("[FirebaseManager] Connection test failed: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    public static ArrayList<Student> fetchStudents(FirebaseConfig config) throws Exception {
        ArrayList<Student> list = new ArrayList<>();
        if (config == null || !config.isConfigured()) {
            return list;
        }

        HttpURLConnection conn = null;
        try {
            String urlStr = getEndpointUrl(config, "students.json");
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setRequestProperty("Accept", "application/json");

            int code = conn.getResponseCode();
            if (code != 200) {
                String errBody = readStream(conn.getErrorStream());
                throw new Exception("Firebase HTTP " + code + ": " + errBody);
            }

            String body = readStream(conn.getInputStream());
            if (body == null || body.trim().isEmpty() || body.trim().equals("null")) {
                return list;
            }

            return parseStudentsJson(body);
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    public static boolean saveAllStudents(FirebaseConfig config, List<Student> students) {
        if (config == null || !config.isConfigured() || students == null) {
            return false;
        }

        HttpURLConnection conn = null;
        try {
            String urlStr = getEndpointUrl(config, "students.json");
            String jsonPayload = serializeStudentsToJson(students);
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int code = conn.getResponseCode();
            return code == 200;
        } catch (Exception e) {
            System.err.println("[FirebaseManager] Failed to save all students: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static String readStream(InputStream is) {
        if (is == null) return "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private static String getEndpointUrl(FirebaseConfig config, String path) {
        String base = config.getDatabaseUrl();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        String url = base + "/" + path;
        String auth = config.getAuthToken();
        if (auth != null && !auth.isEmpty() && !auth.equals("YOUR_AUTH_TOKEN")) {
            url += (url.contains("?") ? "&" : "?") + "auth=" + auth;
        }
        return url;
    }

    private static String serializeStudentsToJson(List<Student> list) {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < list.size(); i++) {
            Student s = list.get(i);
            sb.append("\"").append(s.getRollNo()).append("\":");
            sb.append(serializeSingleStudent(s));
            if (i < list.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private static String serializeSingleStudent(Student s) {
        return "{" +
                "\"rollNo\":" + s.getRollNo() + "," +
                "\"name\":\"" + escape(s.getName()) + "\"," +
                "\"totalClasses\":" + s.getTotalClasses() + "," +
                "\"attendedClasses\":" + s.getAttendedClasses() +
                "}";
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static ArrayList<Student> parseStudentsJson(String json) {
        ArrayList<Student> list = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{[^{}]*?\"rollNo\"\\s*:\\s*(\\d+)[^{}]*?\"name\"\\s*:\\s*\"([^\"]*)\"[^{}]*?\"totalClasses\"\\s*:\\s*(\\d+)[^{}]*?\"attendedClasses\"\\s*:\\s*(\\d+)[^{}]*?\\}");
        Matcher matcher = pattern.matcher(json);

        while (matcher.find()) {
            try {
                int roll = Integer.parseInt(matcher.group(1));
                String name = matcher.group(2);
                int total = Integer.parseInt(matcher.group(3));
                int attended = Integer.parseInt(matcher.group(4));
                list.add(new Student(roll, name, total, attended));
            } catch (Exception ignored) {
            }
        }

        if (list.isEmpty() && json.contains("{") && json.contains("}")) {
            Pattern blockPattern = Pattern.compile("\\{([^{}]+)\\}");
            Matcher blockMatcher = blockPattern.matcher(json);
            while (blockMatcher.find()) {
                String block = blockMatcher.group(1);
                Integer roll = extractInt(block, "rollNo");
                String name = extractString(block, "name");
                Integer total = extractInt(block, "totalClasses");
                Integer attended = extractInt(block, "attendedClasses");

                if (roll != null && name != null && total != null && attended != null) {
                    try {
                        list.add(new Student(roll, name, total, attended));
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        return list;
    }

    private static Integer extractInt(String block, String key) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*(\\d+)");
        Matcher m = p.matcher(block);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return null;
    }

    private static String extractString(String block, String key) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher m = p.matcher(block);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }
}
