public class Student extends Person implements AttendanceTrackable, Comparable<Student> {
    private int totalClasses;
    private int attendedClasses;

    public Student(int rollNo, String name) {
        this(rollNo, name, 0, 0);
    }

    public Student(int rollNo, String name, int totalClasses, int attendedClasses) {
        super(rollNo, name);
        if (totalClasses < 0 || attendedClasses < 0 || attendedClasses > totalClasses) {
            throw new InvalidAttendanceDataException(attendedClasses, totalClasses);
        }
        this.totalClasses = totalClasses;
        this.attendedClasses = attendedClasses;
    }

    @Override
    public String getRole() {
        return "Student";
    }

    @Override
    public String getDisplayDetails() {
        return String.format("Roll No: %d | Name: %s | Attendance: %d/%d (%.2f%%) | Status: %s",
                getRollNo(), getName(), attendedClasses, totalClasses, getPercentage(), getStatus());
    }

    @Override
    public void markAttendance(boolean isPresent) {
        this.totalClasses++;
        if (isPresent) {
            this.attendedClasses++;
        }
    }

    @Override
    public double getPercentage() {
        if (totalClasses <= 0) {
            return 0.0;
        }
        return ((double) attendedClasses / totalClasses) * 100.0;
    }

    @Override
    public boolean isDefaulter() {
        if (totalClasses == 0) {
            return false;
        }
        return getPercentage() < 75.0;
    }

    @Override
    public String getStatus() {
        if (totalClasses == 0) {
            return "No Classes Held";
        }
        return isDefaulter() ? "Critical / Defaulter" : "Eligible for Exam";
    }

    @Override
    public int getClassesNeededFor75() {
        if (totalClasses == 0 || !isDefaulter()) {
            return 0;
        }
        int needed = (int) Math.ceil((3.0 * totalClasses - 4.0 * attendedClasses));
        return needed > 0 ? needed : 1;
    }

    @Override
    public int getClassesCanMiss() {
        if (totalClasses == 0 || isDefaulter()) {
            return 0;
        }
        int canMiss = (int) Math.floor((4.0 * attendedClasses / 3.0) - totalClasses);
        return Math.max(0, canMiss);
    }

    @Override
    public String getAdvisoryMessage() {
        if (totalClasses == 0) {
            return "Session just started. Attend all upcoming lectures.";
        }
        if (isDefaulter()) {
            int needed = getClassesNeededFor75();
            return "Need to attend next " + needed + " consecutive classes to reach 75%.";
        } else {
            int canMiss = getClassesCanMiss();
            if (canMiss > 0) {
                return "Safe Zone: Can afford to miss up to " + canMiss + " classes.";
            } else {
                return "Safe Zone: Maintain regular attendance to stay >= 75%.";
            }
        }
    }

    public int getRollNo() {
        return getId();
    }

    public void setRollNo(int rollNo) {
        setId(rollNo);
    }

    public int getTotalClasses() {
        return totalClasses;
    }

    public void setTotalClasses(int totalClasses) {
        this.totalClasses = Math.max(0, totalClasses);
        if (this.attendedClasses > this.totalClasses) {
            this.attendedClasses = this.totalClasses;
        }
    }

    public int getAttendedClasses() {
        return attendedClasses;
    }

    public void setAttendedClasses(int attendedClasses) {
        this.attendedClasses = Math.max(0, Math.min(attendedClasses, this.totalClasses));
    }

    @Override
    public int compareTo(Student other) {
        if (other == null) return 1;
        return Integer.compare(this.getRollNo(), other.getRollNo());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return getRollNo() == student.getRollNo();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getRollNo());
    }

    @Override
    public String toString() {
        return "Student{" +
                "rollNo=" + getRollNo() +
                ", name='" + getName() + '\'' +
                ", totalClasses=" + totalClasses +
                ", attendedClasses=" + attendedClasses +
                ", percentage=" + String.format("%.2f", getPercentage()) + "%" +
                '}';
    }
}

