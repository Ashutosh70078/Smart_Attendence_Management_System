import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FileManager {
    public static final String DEFAULT_FILE_PATH = "data/students.csv";
    public static final String DEFAULT_COURSE = "Smart Attendance Management System";
    private static volatile long lastKnownModified = 0;

    public static synchronized ArrayList<Student> loadStudents() {
        return loadStudentsFromFile(DEFAULT_FILE_PATH);
    }

    public static synchronized ArrayList<Student> loadStudentsFromFile(String filePath) {
        ArrayList<Student> list = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            if (filePath.equals(DEFAULT_FILE_PATH)) {
                createDefaultFile(file);
            } else {
                return list;
            }
        }

        lastKnownModified = file.lastModified();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }
                line = line.trim();
                if (line.isEmpty() || line.toLowerCase().startsWith("rollno")) {
                    continue;
                }

                String[] parts = parseCsvLine(line);
                if (parts.length >= 4) {
                    try {
                        int roll = Integer.parseInt(parts[0].trim());
                        String name = parts[1].trim();
                        int total = Integer.parseInt(parts[2].trim());
                        int attended = Integer.parseInt(parts[3].trim());
                        list.add(new Student(roll, name, total, attended));
                    } catch (NumberFormatException | InvalidAttendanceDataException ignored) {
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[FileManager] Error reading file: " + e.getMessage());
        }

        return list;
    }

    public static synchronized boolean saveStudents(ArrayList<Student> list) {
        return saveStudentsToFile(DEFAULT_FILE_PATH, list);
    }

    public static synchronized boolean saveStudentsToFile(String filePath, ArrayList<Student> list) {
        if (list == null) {
            return false;
        }

        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        File tempFile = new File(filePath + ".tmp");
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8))) {
            pw.println("RollNo,Name,TotalClasses,AttendedClasses");
            for (Student s : list) {
                String safeName = escapeCsv(s.getName());
                pw.println(s.getRollNo() + "," + safeName + "," + s.getTotalClasses() + "," + s.getAttendedClasses());
            }
            pw.flush();
        } catch (IOException e) {
            System.err.println("[FileManager] Error saving to temp file: " + e.getMessage());
            return false;
        }

        if (file.exists()) {
            file.delete();
        }
        boolean renamed = tempFile.renameTo(file);
        if (renamed) {
            lastKnownModified = file.lastModified();
        }
        return renamed;
    }

    public static boolean hasFileChangedOnDisk() {
        File file = new File(DEFAULT_FILE_PATH);
        if (!file.exists()) return false;
        long currentModified = file.lastModified();
        return currentModified > lastKnownModified;
    }

    public static boolean exportToExcelCSV(File destination, ArrayList<Student> list, String reportTitle) {
        return exportToExcelCSV(destination, list, reportTitle, DEFAULT_COURSE);
    }

    public static boolean exportToExcelCSV(File destination, ArrayList<Student> list, String reportTitle, String courseName) {
        if (destination == null || list == null) return false;

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(destination), StandardCharsets.UTF_8);
             PrintWriter pw = new PrintWriter(writer)) {

            pw.write('\uFEFF');

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timestamp = sdf.format(new Date());

            int totalStudents = list.size();
            int defaultersCount = 0;
            double totalPctSum = 0;

            for (Student s : list) {
                if (s.isDefaulter()) defaultersCount++;
                totalPctSum += s.getPercentage();
            }
            int eligibleCount = totalStudents - defaultersCount;
            double avgAttendance = totalStudents > 0 ? (totalPctSum / totalStudents) : 0.0;

            pw.println("=========================================================================================");
            pw.println("NOIDA INSTITUTE OF ENGINEERING AND TECHNOLOGY (NIET), GREATER NOIDA");
            pw.println("SMART ATTENDANCE MANAGEMENT SYSTEM");
            pw.println("OFFICIAL ATTENDANCE & ELIGIBILITY REPORT - " + reportTitle.toUpperCase());
            pw.println("Generated On: " + timestamp);
            pw.println("=========================================================================================");
            pw.println();

            pw.println("--- SUMMARY METRICS ---");
            pw.println("Total Students Evaluated," + totalStudents);
            pw.println("Eligible for University Exam (>= 75%)," + eligibleCount);
            pw.println("Critical Defaulters (< 75%)," + defaultersCount);
            pw.println("Batch Average Attendance," + String.format("%.2f%%", avgAttendance));
            pw.println();

            pw.println("--- STUDENT ACADEMIC ATTENDANCE ROSTER ---");
            pw.println("Roll No,Student Name,Attended Classes,Total Classes,Attendance %,Status,Advisory Note");

            for (Student s : list) {
                String advisory = s.getAdvisoryMessage().replace(",", ";");
                pw.println(s.getRollNo() + ","
                        + escapeCsv(s.getName()) + ","
                        + s.getAttendedClasses() + ","
                        + s.getTotalClasses() + ","
                        + String.format("%.2f%%", s.getPercentage()) + ","
                        + escapeCsv(s.getStatus()) + ","
                        + escapeCsv(advisory));
            }

            pw.println();
            pw.println("=========================================================================================");
            pw.println("Authorized Signatory: Academic Administration, NIET Greater Noida");
            pw.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static String[] parseCsvLine(String line) {
        ArrayList<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString());
        return tokens.toArray(new String[0]);
    }

    private static void createDefaultFile(File file) {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            pw.println("RollNo,Name,TotalClasses,AttendedClasses");
            pw.println("101,Aarav Sharma,26,24");
            pw.println("102,Priya Singh,26,18");
            pw.println("103,Krish Kumar,26,25");
            pw.println("104,Govind Kumar,26,21");
            pw.println("105,Ashutosh Jitendra Singh,26,26");
            pw.println("106,Suman Kumar Gorain,26,22");
            pw.println("107,Preety Kumari,26,17");
            pw.println("108,Rohan Verma,26,14");
            pw.flush();
            lastKnownModified = file.lastModified();
        } catch (IOException e) {
            System.err.println("[FileManager] Error creating default file: " + e.getMessage());
        }
    }
}

