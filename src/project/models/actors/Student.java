package project.models.actors;

import java.util.*;
import project.enums.Role;
import project.models.others.*;
import project.storage.Database;

/**
 * University student. Enforces: max 21 credits, max 3 course failures.
 * Can optionally implement Researcher (bachelor students CAN be researchers per requirements).
 */
public class Student extends User implements Researcher {
    private static final long serialVersionUID = 1L;
    private static final int MAX_CREDITS = 21;
    private static final int MAX_FAIL_COUNT = 3;

    private double gpa;
    private int yearOfStudy;
    private int totalCredits;
    private int failCount;
    private String organization;
    private boolean isOrganizationHead;
    private List<String> registeredCourseIds = new ArrayList<>(); // course codes
    private List<ResearchPaper> researchPapers = new ArrayList<>();
    private boolean isResearcher = false; // bachelor students optionally

    public Student() { super(); setRole(Role.STUDENT); }
    public Student(String id, String password, String firstName, String lastName, String email) {
        super(id, password, firstName, lastName, email, Role.STUDENT);
    }

    // Course Registration

    /**
     * Registers for a course if credit limit not exceeded.
     * @return true if registered successfully
     */
    public boolean registerForCourse(Course course) {
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

    /** Called when a course is failed. Enforces max 3 fail rule. */
    public void incrementFailCount() {
        failCount++;
        log("Failed a course. Total fails: " + failCount);
        if (failCount > MAX_FAIL_COUNT) {
            System.out.println("[ALERT] " + getFullName() + " has exceeded the maximum fail count (" + MAX_FAIL_COUNT + ")!");
        }
    }

    public boolean hasExceededFailLimit() { return failCount > MAX_FAIL_COUNT; }

    /** Prints full transcript for this student. */
    public void viewTranscript() {
        System.out.println("=== TRANSCRIPT: " + getFullName() + " (Year " + yearOfStudy + ") ===");
        Database db = Database.getInstance();
        for (String courseCode : registeredCourseIds) {
            Course c = db.getCourseByCode(courseCode);
            if (c != null) {
                Mark m = c.getLatestMark(getId());
                if (m != null) System.out.printf("  %-20s %5.1f  %-3s%n", courseCode, m.getTotal(), m.getLetterGrade());
                else System.out.printf("  %-20s  --  (no mark)%n", courseCode);
            }
        }
        System.out.printf("  GPA: %.2f | Credits: %d | Fails: %d%n", gpa, totalCredits, failCount);
    }

    /** Views info about a teacher for a specific course. */
    public void viewTeacherInfo(Teacher teacher) {
        System.out.println("Teacher: " + teacher.getFullName() + " | Title: " + teacher.getTitle() + " | H-Index: " + teacher.calculateHIndex());
    }

    /** Rates a teacher (1-5 stars, stored in DB). */
    public void rateTeacher(Teacher teacher, int rating) {
        if (rating < 1 || rating > 5) { System.out.println("Rating must be 1-5."); return; }
        Database.getInstance().addTeacherRating(teacher.getId(), rating);
        log("Rated teacher " + teacher.getId() + " with " + rating);
        System.out.println("[Rating] " + getFullName() + " rated " + teacher.getFullName() + ": " + rating + "/5");
    }

    // Researcher implementation (optional for bachelor students)

    @Override public double calculateHIndex() {
        if (researchPapers.isEmpty()) return 0;
        List<Integer> cits = new ArrayList<>();
        for (ResearchPaper p : researchPapers) cits.add(p.getCitations());
        cits.sort(Collections.reverseOrder());
        int h = 0;
        for (int i = 0; i < cits.size(); i++) { if (cits.get(i) >= i + 1) h = i + 1; else break; }
        return h;
    }

    @Override public void printPapers(Comparator<ResearchPaper> comparator) {
        List<ResearchPaper> sorted = new ArrayList<>(researchPapers);
        sorted.sort(comparator);
        System.out.println("=== Research Papers: " + getFullName() + " ===");
        for (ResearchPaper p : sorted) System.out.println("  " + p.getCitation("Plain Text"));
    }

    @Override public List<ResearchPaper> getResearchPapersList() { return researchPapers; }

    public void addResearchPaper(ResearchPaper paper) {
        researchPapers.add(paper);
        News news = new News("New Research Paper by Student " + getFullName(), getFullName() + " published: " + paper.getTitle(), true);
        Database.getInstance().addNews(news);
    }

    /** Recalculates GPA based on all course marks in Database. */
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

    // Getters / Setters
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
    public List<String> getRegisteredCourseIds() { return registeredCourseIds; }
    public void setRegisteredCourseIds(List<String> v) { this.registeredCourseIds = v; }
    public List<ResearchPaper> getResearchPapers() { return researchPapers; }
    public void setResearchPapers(List<ResearchPaper> v) { this.researchPapers = v; }
    public boolean isResearcher() { return isResearcher; }
    public void setResearcher(boolean v) { this.isResearcher = v; }

    @Override public String toString() {
        return "Student[id=" + getId() + ", name=" + getFullName() + ", year=" + yearOfStudy + ", gpa=" + String.format("%.2f", gpa) + ", credits=" + totalCredits + "]";
    }
}
