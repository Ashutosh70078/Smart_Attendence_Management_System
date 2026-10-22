public class InvalidAttendanceDataException extends IllegalArgumentException {
    private final int attended;
    private final int total;

    public InvalidAttendanceDataException(int attended, int total) {
        super(String.format("Invalid attendance data: attended classes (%d) cannot exceed total classes held (%d), and neither can be negative.", attended, total));
        this.attended = attended;
        this.total = total;
    }

    public InvalidAttendanceDataException(String message) {
        super(message);
        this.attended = -1;
        this.total = -1;
    }

    public int getAttended() {
        return attended;
    }

    public int getTotal() {
        return total;
    }
}

