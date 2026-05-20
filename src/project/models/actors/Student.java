package project.models.actors;

import java.util.*;

import project.models.enums.Role;
import project.models.enums.School;
import project.models.others.*;
import project.storage.Database;

/**
 * Undergraduate student. Manages course enrollment (capped at 21 credits),
 * tracks GPA from marks, and can join student organizations.
 * Exceeding 3 course failures blocks further registration.
 */
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

    /**
     * Attempts to enrol the student in the given course.
     * Rejects if the 21-credit cap would be exceeded or the student is already enrolled.
     *
     * @param course the course to register for
     * @return {@code true} if registration succeeded, {@code false} otherwise
     */
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

    /**
     * Drops the student from the given course and reduces their total credits.
     *
     * @param course the course to drop
     * @return {@code true} if the course was found and removed, {@code false} if not enrolled
     */
    public boolean dropCourse(Course course) {
        if (registeredCourseIds.remove(course.getCourseCode())) {
            totalCredits -= course.getCredits();
            log("Dropped course " + course.getCourseCode());
            return true;
        }
        return false;
    }

    /**
     * Increments the failure counter and alerts if the student has exceeded the 3-failure limit.
     * Called automatically by {@link project.services.MarkService} when a mark below 50 is assigned.
     */
    public void incrementFailCount() {
        failCount++;
        log("Failed a course. Total fails: " + failCount);
        if (failCount > MAX_FAIL_COUNT) {
            System.out.println("[ALERT] " + getFullName() + " has exceeded the maximum fail count (" + MAX_FAIL_COUNT + ")!");
        }
    }

    /** Returns {@code true} if the student has failed more than 3 courses and cannot register for new ones. */
    public boolean hasExceededFailLimit() { return failCount > MAX_FAIL_COUNT; }

    /**
     * Prints a formatted transcript to stdout showing marks, letter grades, GPA, credits, and fail count.
     * Recalculates GPA and fail count from the database before printing.
     */
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

    /** Recomputes GPA from the latest mark in every registered course. Call this before displaying grades. */
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

    /** Re-counts failed courses from the database. A course is failed when its total mark is below 50. */
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
