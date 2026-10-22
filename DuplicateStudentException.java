public class DuplicateStudentException extends Exception {
    private final int rollNo;

    public DuplicateStudentException(int rollNo) {
        super("Student with Roll Number " + rollNo + " already exists in the system.");
        this.rollNo = rollNo;
    }

    public DuplicateStudentException(int rollNo, String message) {
        super(message);
        this.rollNo = rollNo;
    }

    public int getRollNo() {
        return rollNo;
    }
}

