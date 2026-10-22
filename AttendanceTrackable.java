public interface AttendanceTrackable {
    void markAttendance(boolean isPresent);
    double getPercentage();
    boolean isDefaulter();
    String getStatus();
    int getClassesNeededFor75();
    int getClassesCanMiss();
    String getAdvisoryMessage();
}

