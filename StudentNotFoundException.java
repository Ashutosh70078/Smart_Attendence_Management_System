public class StudentNotFoundException extends Exception {
    private final int rollNo;

    public StudentNotFoundException(int rollNo) {
        super("Student with Roll Number " + rollNo + " was not found in the records.");
        this.rollNo = rollNo;
    }

    public StudentNotFoundException(int rollNo, String message) {
        super(message);
        this.rollNo = rollNo;
    }

    public int getRollNo() {
        return rollNo;
    }
}

