package project.models.others;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a student's academic mark for a course.
 * Total = firstAttestation (30%) + secondAttestation (30%) + finalExam (40%).
 * Pass threshold: total >= 50.
 */
public class Mark implements Serializable {
    private static final long serialVersionUID = 1L;
    private double firstAttestation;
    private double secondAttestation;
    private double finalExam;
    private String courseCode;
    private String studentId;

    public Mark() {}
    public Mark(double first, double second, double finalExam) {
        this.firstAttestation = first;
        this.secondAttestation = second;
        this.finalExam = finalExam;
    }
    public Mark(String studentId, String courseCode, double first, double second, double finalExam) {
        this(first, second, finalExam);
        this.studentId = studentId;
        this.courseCode = courseCode;
    }

    public double getTotal() { return firstAttestation + secondAttestation + finalExam; }

    public String getLetterGrade() {
        double t = getTotal();
        if (t >= 95) return "A+";
        if (t >= 90) return "A";
        if (t >= 85) return "A-";
        if (t >= 80) return "B+";
        if (t >= 75) return "B";
        if (t >= 70) return "B-";
        if (t >= 65) return "C+";
        if (t >= 60) return "C";
        if (t >= 55) return "C-";
        if (t >= 50) return "D+";
        if (t >= 45) return "D";
        return "F";
    }

    public double getGradePoints() {
        String g = getLetterGrade();
        switch (g) {
            case "A+": return 4.0; case "A": return 4.0; case "A-": return 3.67;
            case "B+": return 3.33; case "B": return 3.0; case "B-": return 2.67;
            case "C+": return 2.33; case "C": return 2.0; case "C-": return 1.67;
            case "D+": return 1.33; case "D": return 1.0; default: return 0.0;
        }
    }

    public boolean isPassed() { return getTotal() >= 50.0; }

    public double getFirstAttestation() { return firstAttestation; }
    public void setFirstAttestation(double v) { this.firstAttestation = v; }
    public double getSecondAttestation() { return secondAttestation; }
    public void setSecondAttestation(double v) { this.secondAttestation = v; }
    public double getFinalExam() { return finalExam; }
    public void setFinalExam(double v) { this.finalExam = v; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String c) { this.courseCode = c; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String s) { this.studentId = s; }

    @Override public int hashCode() { return Objects.hash(courseCode, studentId, firstAttestation, secondAttestation, finalExam); }
    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Mark)) return false;
        Mark o = (Mark) obj;
        return Objects.equals(courseCode, o.courseCode) && Objects.equals(studentId, o.studentId)
               && Double.compare(firstAttestation, o.firstAttestation) == 0
               && Double.compare(secondAttestation, o.secondAttestation) == 0
               && Double.compare(finalExam, o.finalExam) == 0;
    }
    @Override public String toString() {
        return "Mark[course=" + courseCode + ", student=" + studentId + ", total=" + getTotal() + ", grade=" + getLetterGrade() + "]";
    }
}
