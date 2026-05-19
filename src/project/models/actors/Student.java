package project.models.actors;

import java.util.*;

import project.models.enums.Role;
import project.models.enums.School;
import project.models.others.*;
import project.storage.Database;

public class Student extends User {
    private static final long serialVersionUID = 1L;
    private static final int MAX_CREDITS = 21;
    private static final int MAX_FAIL_COUNT = 3;

    private double gpa;
    private int yearOfStudy;
    private int totalCredits;
    private int failCount;
    private String organization;
    private boolean isOrganizationHead;
    private School school;
    private List<String> registeredCourseIds = new ArrayList<>();

    public Student() { super(); setRole(Role.STUDENT); }
    public Student(String id, String password, String firstName, String lastName, String email) {
        super(id, password, firstName, lastName, email, Role.STUDENT);
    }

    public boolean registerForCourse(Course course) {
        if (registeredCourseIds == null) registeredCourseIds = new ArrayList<>();
        if (totalCredits + course.getCredits() > MAX_CREDITS) {
            System.out.println("[DENIED] Credit limit (21) would be exceeded for " + getFullName());
            return false;
        }
        if (registeredCourseIds.contains(course.getCourseCode())) {
            System.out.println("[INFO] Already registered for " + course.getCourseCode());
            return false;
        }
        registeredCourseIds.add(course.getCourseCode());
        totalCredits += course.getCredits();
        log("Registered for course " + course.getCourseCode());
        System.out.println("[Registered] " + getFullName() + " → " + course.getCourseName());
        return true;
    }

    public boolean dropCourse(Course course) {
        if (registeredCourseIds.remove(course.getCourseCode())) {
            totalCredits -= course.getCredits();
            log("Dropped course " + course.getCourseCode());
            return true;
        }
        return false;
    }

    public void incrementFailCount() {
        failCount++;
        log("Failed a course. Total fails: " + failCount);
        if (failCount > MAX_FAIL_COUNT) {
            System.out.println("[ALERT] " + getFullName() + " has exceeded the maximum fail count (" + MAX_FAIL_COUNT + ")!");
        }
    }

    public boolean hasExceededFailLimit() { return failCount > MAX_FAIL_COUNT; }

    public void viewTranscript() {
        recalculateGpa();
        recalculateFailCount();
        System.out.println("=== TRANSCRIPT: " + getFullName() + " (Year " + yearOfStudy + ") ===");
        Database db = Database.getInstance();
        for (String courseCode : getRegisteredCourseIds()) {
            Course c = db.getCourseByCode(courseCode);
            if (c != null) {
                Mark m = c.getLatestMark(getId());
                if (m != null) System.out.printf("  %-20s %5.1f  %-3s%n", courseCode, m.getTotal(), m.getLetterGrade());
                else System.out.printf("  %-20s  --  (no mark)%n", courseCode);
            }
        }
        System.out.printf("  GPA: %.2f | Credits: %d | Fails: %d%n", gpa, totalCredits, failCount);
    }

    public void recalculateGpa() {
        Database db = Database.getInstance();
        double totalPoints = 0; int count = 0;
        for (String code : registeredCourseIds) {
            Course c = db.getCourseByCode(code);
            if (c != null) {
                Mark m = c.getLatestMark(getId());
                if (m != null) { totalPoints += m.getGradePoints(); count++; }
            }
        }
        this.gpa = (count > 0) ? totalPoints / count : 0.0;
    }

    public void recalculateFailCount() {
        Database db = Database.getInstance();
        int fails = 0;
        for (String code : getRegisteredCourseIds()) {
            Course c = db.getCourseByCode(code);
            if (c != null) {
                Mark m = c.getLatestMark(getId());
                if (m != null && !m.isPassed()) fails++;
            }
        }
        this.failCount = fails;
    }

    public double getGpa() { return gpa; }
    public void setGpa(double v) { this.gpa = v; }
    public int getYearOfStudy() { return yearOfStudy; }
    public void setYearOfStudy(int v) { this.yearOfStudy = v; }
    public int getTotalCredits() { return totalCredits; }
    public void setTotalCredits(int v) { this.totalCredits = v; }
    public int getFailCount() { return failCount; }
    public void setFailCount(int v) { this.failCount = v; }
    public String getOrganization() { return organization; }
    public void setOrganization(String v) { this.organization = v; }
    public boolean isOrganizationHead() { return isOrganizationHead; }
    public void setOrganizationHead(boolean v) { this.isOrganizationHead = v; }
    public School getSchool() { return school; }
    public void setSchool(School v) { this.school = v; }
    public List<String> getRegisteredCourseIds() {
        if (registeredCourseIds == null) registeredCourseIds = new ArrayList<>();
        return registeredCourseIds;
    }
    public void setRegisteredCourseIds(List<String> v) { this.registeredCourseIds = v; }

    @Override public String toString() {
        return "Student[id=" + getId() + ", name=" + getFullName() + ", school=" + school +
               ", year=" + yearOfStudy + ", gpa=" + String.format("%.2f", gpa) + ", credits=" + totalCredits + "]";
    }
}
