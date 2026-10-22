import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AttendanceManager {
    private ArrayList<Student> studentList;
    private final List<DataChangeListener> listeners = new CopyOnWriteArrayList<>();
    private final ExecutorService bgExecutor = Executors.newSingleThreadExecutor();

    public interface DataChangeListener {
        void onDataChanged();
    }

    public AttendanceManager() {
        FirebaseConfig config = FirebaseConfig.getInstance();
        if (config.isConfigured()) {
            try {
                ArrayList<Student> cloudList = FirebaseManager.fetchStudents(config);
                if (cloudList != null && !cloudList.isEmpty()) {
                    this.studentList = cloudList;
                    FileManager.saveStudents(cloudList);
                    System.out.println("[AttendanceManager] Initialized with " + cloudList.size() + " students from Firebase Cloud DB.");
                } else {
                    this.studentList = FileManager.loadStudents();
                    if (!this.studentList.isEmpty()) {
                        syncToFirebaseAsync();
                    }
                }
            } catch (Exception e) {
                System.err.println("[AttendanceManager] Firebase load notice: " + e.getMessage() + ". Using local CSV cache.");
                this.studentList = FileManager.loadStudents();
            }
        } else {
            this.studentList = FileManager.loadStudents();
        }
    }

    public synchronized ArrayList<Student> getStudents() {
        return new ArrayList<>(studentList);
    }

    public synchronized void registerStudent(int roll, String name, int total, int attended)
            throws DuplicateStudentException, InvalidAttendanceDataException {
        if (findStudentByRoll(roll) != null) {
            throw new DuplicateStudentException(roll);
        }
        Student student = new Student(roll, name, total, attended);
        studentList.add(student);
        FileManager.saveStudents(studentList);
        syncToFirebaseAsync();
        notifyListeners();
    }

    public synchronized void modifyStudent(int roll, String newName, int total, int attended)
            throws StudentNotFoundException, InvalidAttendanceDataException {
        Student student = findStudentByRoll(roll);
        if (student == null) {
            throw new StudentNotFoundException(roll);
        }
        if (attended > total || attended < 0 || total < 0) {
            throw new InvalidAttendanceDataException(attended, total);
        }
        student.setName(newName);
        student.setTotalClasses(total);
        student.setAttendedClasses(attended);
        FileManager.saveStudents(studentList);
        syncToFirebaseAsync();
        notifyListeners();
    }

    public synchronized void removeStudent(int roll) throws StudentNotFoundException {
        Student toRemove = findStudentByRoll(roll);
        if (toRemove == null) {
            throw new StudentNotFoundException(roll);
        }
        studentList.remove(toRemove);
        FileManager.saveStudents(studentList);
        syncToFirebaseAsync();
        notifyListeners();
    }

    public synchronized Student findStudentByRoll(int roll) {
        for (Student s : studentList) {
            if (s.getRollNo() == roll) {
                return s;
            }
        }
        return null;
    }

    public synchronized ArrayList<Student> searchStudents(String query) {
        ArrayList<Student> result = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return getStudents();
        }
        String q = query.trim().toLowerCase();
        for (Student s : studentList) {
            if (String.valueOf(s.getRollNo()).contains(q) || s.getName().toLowerCase().contains(q)) {
                result.add(s);
            }
        }
        return result;
    }

    public synchronized boolean markBatchAttendance(boolean[] attendanceArray) {
        if (attendanceArray == null || attendanceArray.length != studentList.size()) {
            return false;
        }
        for (int i = 0; i < studentList.size(); i++) {
            studentList.get(i).markAttendance(attendanceArray[i]);
        }
        boolean saved = FileManager.saveStudents(studentList);
        if (saved) {
            syncToFirebaseAsync();
            notifyListeners();
        }
        return saved;
    }

    public synchronized boolean syncToFirebase() {
        FirebaseConfig config = FirebaseConfig.getInstance();
        if (!config.isConfigured()) return false;
        return FirebaseManager.saveAllStudents(config, studentList);
    }

    public void syncToFirebaseAsync() {
        bgExecutor.submit(() -> {
            try {
                FirebaseConfig config = FirebaseConfig.getInstance();
                if (config.isConfigured()) {
                    FirebaseManager.saveAllStudents(config, getStudents());
                }
            } catch (Exception e) {
                System.err.println("[AttendanceManager] Async Firebase sync notice: " + e.getMessage());
            }
        });
    }

    public synchronized boolean syncFromFirebase() {
        FirebaseConfig config = FirebaseConfig.getInstance();
        if (!config.isConfigured()) return false;
        try {
            ArrayList<Student> cloudList = FirebaseManager.fetchStudents(config);
            if (cloudList != null && !cloudList.isEmpty()) {
                this.studentList = cloudList;
                FileManager.saveStudents(cloudList);
                notifyListeners();
                return true;
            }
        } catch (Exception e) {
            System.err.println("[AttendanceManager] Pull from Firebase failed: " + e.getMessage());
        }
        return false;
    }

    public boolean isFirebaseConfigured() {
        return FirebaseConfig.getInstance().isConfigured();
    }

    public String getStorageStatus() {
        if (isFirebaseConfigured()) {
            return "Firebase Cloud DB: Active (" + FirebaseConfig.getInstance().getProjectId() + ") | Local Cache: data/students.csv";
        } else {
            return "Storage: data/students.csv (Local Mode) | Firebase: Unconfigured";
        }
    }

    public synchronized ArrayList<Student> getDefaulters() {
        ArrayList<Student> defaulters = new ArrayList<>();
        for (Student s : studentList) {
            if (s.isDefaulter()) {
                defaulters.add(s);
            }
        }
        return defaulters;
    }

    public synchronized ArrayList<Student> getEligibleStudents() {
        ArrayList<Student> eligible = new ArrayList<>();
        for (Student s : studentList) {
            if (!s.isDefaulter()) {
                eligible.add(s);
            }
        }
        return eligible;
    }

    public synchronized double getAverageAttendancePercentage() {
        if (studentList.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Student s : studentList) {
            sum += s.getPercentage();
        }
        return sum / studentList.size();
    }

    public synchronized boolean checkAndAutoSync() {
        if (FileManager.hasFileChangedOnDisk()) {
            reloadData();
            return true;
        }
        return false;
    }

    public synchronized void reloadData() {
        this.studentList = FileManager.loadStudents();
        notifyListeners();
    }

    public void addDataChangeListener(DataChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeDataChangeListener(DataChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (DataChangeListener l : listeners) {
            try {
                l.onDataChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
