import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FirebaseConfig {
    public static final String CONFIG_FILE_PATH = "firebase-config.json";

    private String apiKey = "";
    private String authDomain = "";
    private String projectId = "";
    private String storageBucket = "";
    private String messagingSenderId = "";
    private String appId = "";
    private String databaseUrl = "";
    private String authToken = "";

    private static FirebaseConfig instance;

    public FirebaseConfig() {
        loadFromFile();
    }

    public static synchronized FirebaseConfig getInstance() {
        if (instance == null) {
            instance = new FirebaseConfig();
        }
        return instance;
    }

    public boolean isConfigured() {
        return projectId != null && !projectId.trim().isEmpty() && !projectId.equals("YOUR_PROJECT_ID");
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
    }

    public String getAuthDomain() {
        return authDomain;
    }

    public void setAuthDomain(String authDomain) {
        this.authDomain = authDomain != null ? authDomain.trim() : "";
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId != null ? projectId.trim() : "";
        if (this.databaseUrl.isEmpty() && !this.projectId.isEmpty()) {
            this.databaseUrl = "https://" + this.projectId + "-default-rtdb.firebaseio.com";
        }
    }

    public String getStorageBucket() {
        return storageBucket;
    }

    public void setStorageBucket(String storageBucket) {
        this.storageBucket = storageBucket != null ? storageBucket.trim() : "";
    }

    public String getMessagingSenderId() {
        return messagingSenderId;
    }

    public void setMessagingSenderId(String messagingSenderId) {
        this.messagingSenderId = messagingSenderId != null ? messagingSenderId.trim() : "";
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId != null ? appId.trim() : "";
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken != null ? authToken.trim() : "";
    }

    public String getDatabaseUrl() {
        if ((databaseUrl == null || databaseUrl.trim().isEmpty()) && projectId != null && !projectId.isEmpty()) {
            return "https://" + projectId + "-default-rtdb.firebaseio.com";
        }
        return databaseUrl != null ? databaseUrl.trim() : "";
    }

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl != null ? databaseUrl.trim() : "";
    }

    public synchronized boolean loadFromFile() {
        File file = new File(CONFIG_FILE_PATH);
        if (!file.exists()) {
            saveDefaultTemplate();
            return false;
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            System.err.println("[FirebaseConfig] Error reading " + CONFIG_FILE_PATH + ": " + e.getMessage());
            return false;
        }

        Map<String, String> map = parseSimpleJson(sb.toString());
        if (map.containsKey("apiKey")) this.apiKey = map.get("apiKey");
        if (map.containsKey("authDomain")) this.authDomain = map.get("authDomain");
        if (map.containsKey("projectId")) this.projectId = map.get("projectId");
        if (map.containsKey("storageBucket")) this.storageBucket = map.get("storageBucket");
        if (map.containsKey("messagingSenderId")) this.messagingSenderId = map.get("messagingSenderId");
        if (map.containsKey("appId")) this.appId = map.get("appId");
        if (map.containsKey("databaseUrl")) this.databaseUrl = map.get("databaseUrl");
        if (map.containsKey("authToken")) this.authToken = map.get("authToken");

        return isConfigured();
    }

    public synchronized boolean saveToFile() {
        File file = new File(CONFIG_FILE_PATH);
        String json = "{\n" +
                "  \"apiKey\": \"" + escapeJson(apiKey) + "\",\n" +
                "  \"authDomain\": \"" + escapeJson(authDomain) + "\",\n" +
                "  \"projectId\": \"" + escapeJson(projectId) + "\",\n" +
                "  \"storageBucket\": \"" + escapeJson(storageBucket) + "\",\n" +
                "  \"messagingSenderId\": \"" + escapeJson(messagingSenderId) + "\",\n" +
                "  \"appId\": \"" + escapeJson(appId) + "\",\n" +
                "  \"databaseUrl\": \"" + escapeJson(getDatabaseUrl()) + "\",\n" +
                "  \"authToken\": \"" + escapeJson(authToken) + "\"\n" +
                "}\n";

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(json);
            return true;
        } catch (IOException e) {
            System.err.println("[FirebaseConfig] Error saving " + CONFIG_FILE_PATH + ": " + e.getMessage());
            return false;
        }
    }

    private void saveDefaultTemplate() {
        this.apiKey = "YOUR_API_KEY";
        this.authDomain = "YOUR_PROJECT_ID.firebaseapp.com";
        this.projectId = "YOUR_PROJECT_ID";
        this.storageBucket = "YOUR_PROJECT_ID.appspot.com";
        this.messagingSenderId = "YOUR_MESSAGING_SENDER_ID";
        this.appId = "YOUR_APP_ID";
        this.databaseUrl = "https://YOUR_PROJECT_ID-default-rtdb.firebaseio.com";
        this.authToken = "";
        saveToFile();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private Map<String, String> parseSimpleJson(String json) {
        Map<String, String> map = new HashMap<>();
        String[] lines = json.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("\"") && line.contains(":")) {
                int colonIdx = line.indexOf(":");
                String key = line.substring(1, colonIdx).replace("\"", "").trim();
                String val = line.substring(colonIdx + 1).trim();
                if (val.endsWith(",")) val = val.substring(0, val.length() - 1).trim();
                if (val.startsWith("\"") && val.endsWith("\"") && val.length() >= 2) {
                    val = val.substring(1, val.length() - 1);
                }
                map.put(key, val);
            }
        }
        return map;
    }
}

