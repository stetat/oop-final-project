package project.models.actors;

import java.util.*;
import project.enums.ManagerType;
import project.enums.Role;
import project.models.others.*;
import project.storage.Database;

/**
 * University manager (OR = Office of Registrar, DEPARTMENTS = Department Manager).
 * Manages course registration approvals, teacher assignments, news, and statistical reports.
 */
public class Manager extends Employee {
    private static final long serialVersionUID = 1L;
    private ManagerType type;

    public Manager() {}
    public Manager(String id, String password, String firstName, String lastName, String email, double salary, ManagerType type) {
        super(id, password, firstName, lastName, email, salary, Role.MANAGER);
        this.type = type;
    }

    /** Approves a student's course registration request. */
    public void approveRegistration(Student student, Course course) {
        boolean ok = student.registerForCourse(course);
        log("Approved registration: " + student.getId() + " → " + course.getCourseCode());
        System.out.println("[Manager " + getFullName() + "] Registration " + (ok ? "APPROVED" : "FAILED") + ": " + student.getFullName() + " → " + course.getCourseName());
    }

    public void rejectRegistration(Student student, Course course) {
        log("Rejected registration: " + student.getId() + " → " + course.getCourseCode());
        System.out.println("[Manager] Rejected: " + student.getFullName() + " → " + course.getCourseName());
    }

    /** Assigns a teacher to a course. */
    public void assignCourse(Teacher teacher, Course course) {
        course.addInstructorId(teacher.getId());
        Database.getInstance().saveCourse(course);
        log("Assigned " + teacher.getId() + " to " + course.getCourseCode());
        System.out.println("[Manager] Assigned " + teacher.getFullName() + " → " + course.getCourseName());
    }

    public void unassignCourse(Teacher teacher, Course course) {
        course.removeInstructorId(teacher.getId());
        Database.getInstance().saveCourse(course);
        log("Unassigned " + teacher.getId() + " from " + course.getCourseCode());
    }

    /** Creates statistical academic performance report. */
    public void createStatisticalReport() {
        Database db = Database.getInstance();
        System.out.println("=== ACADEMIC PERFORMANCE REPORT ===");
        List<Student> students = db.getAllStudents();
        if (students.isEmpty()) { System.out.println("No students found."); return; }
        // Sort by GPA descending
        students.sort((a, b) -> Double.compare(b.getGpa(), a.getGpa()));
        double totalGpa = 0;
        for (Student s : students) {
            System.out.printf("  %-20s GPA: %.2f  Credits: %d  Fails: %d%n", s.getFullName(), s.getGpa(), s.getTotalCredits(), s.getFailCount());
            totalGpa += s.getGpa();
        }
        System.out.printf("  Average GPA: %.2f%n", students.isEmpty() ? 0 : totalGpa / students.size());
    }

    public void addNews(News news) {
        Database.getInstance().addNews(news);
        log("Added news: " + news.getTitle());
        System.out.println("[News Added] " + news.getTitle());
    }

    public void removeNews(String newsId) {
        Database.getInstance().removeNews(newsId);
        log("Removed news id=" + newsId);
    }

    /** View students sorted by given field: "gpa", "name", "year". */
    public void viewStudents(String sortBy) {
        List<Student> students = new ArrayList<>(Database.getInstance().getAllStudents());
        switch (sortBy.toLowerCase()) {
            case "gpa":  students.sort((a, b) -> Double.compare(b.getGpa(), a.getGpa())); break;
            case "name": students.sort(Comparator.comparing(User::getLastName)); break;
            case "year": students.sort(Comparator.comparingInt(Student::getYearOfStudy)); break;
        }
        System.out.println("=== STUDENTS (sorted by " + sortBy + ") ===");
        students.forEach(s -> System.out.println("  " + s));
    }

    public void viewTeachers() {
        System.out.println("=== TEACHERS ===");
        Database.getInstance().getAllTeachers().forEach(t -> System.out.println("  " + t));
    }

    public ManagerType getType() { return type; }
    public void setType(ManagerType v) { this.type = v; }

    @Override public String toString() {
        return "Manager[id=" + getId() + ", name=" + getFullName() + ", type=" + type + "]";
    }
}
