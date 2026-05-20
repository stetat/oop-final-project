package project.services;

import java.util.*;
import project.models.actors.*;
import project.patterns.ResearcherDecorator;
import project.storage.Database;
import project.models.others.Course;
import project.models.others.News;

/** Manager-level operations: course/teacher management, reports, news, and student views. */
public class ManagerService {

    /**
     * Directly enrolls a student in a course and logs the approval.
     *
     * @param manager the approving manager
     * @param student the student to enroll
     * @param course  the course to enroll them in
     */
    public void approveRegistration(Manager manager, Student student, Course course) {
        boolean ok = student.registerForCourse(course);
        manager.log("Approved registration: " + student.getId() + " → " + course.getCourseCode());
        System.out.println("[Manager " + manager.getFullName() + "] Registration " +
            (ok ? "APPROVED" : "FAILED") + ": " + student.getFullName() + " → " + course.getCourseName());
    }

    /**
     * Logs a rejected registration (no enrollment change is made).
     *
     * @param manager the rejecting manager
     * @param student the student whose request was rejected
     * @param course  the course they were rejected from
     */
    public void rejectRegistration(Manager manager, Student student, Course course) {
        manager.log("Rejected registration: " + student.getId() + " → " + course.getCourseCode());
        System.out.println("[Manager] Rejected: " + student.getFullName() + " → " + course.getCourseName());
    }

    /**
     * Assigns a teacher to instruct a course and saves the change.
     *
     * @param manager the manager performing the assignment
     * @param teacher the teacher to assign
     * @param course  the course to assign them to
     */
    public void assignCourse(Manager manager, Teacher teacher, Course course) {
        course.addInstructorId(teacher.getId());
        Database.getInstance().saveCourse(course);
        manager.log("Assigned " + teacher.getId() + " to " + course.getCourseCode());
        System.out.println("[Manager] Assigned " + teacher.getFullName() + " → " + course.getCourseName());
    }

    /**
     * Removes a teacher from a course's instructor list.
     *
     * @param manager the manager performing the removal
     * @param teacher the teacher to unassign
     * @param course  the course to remove them from
     */
    public void unassignCourse(Manager manager, Teacher teacher, Course course) {
        course.removeInstructorId(teacher.getId());
        Database.getInstance().saveCourse(course);
        manager.log("Unassigned " + teacher.getId() + " from " + course.getCourseCode());
    }

    /**
     * Publishes a news item and persists it.
     *
     * @param manager the manager posting the news
     * @param news    the news item to add
     */
    public void addNews(Manager manager, News news) {
        Database.getInstance().addNews(news);
        manager.log("Added news: " + news.getTitle());
        System.out.println("[News Added] " + news.getTitle());
        Database.getInstance().saveToDisk();
    }

    /**
     * Removes a news item by its ID.
     *
     * @param manager the manager requesting the removal
     * @param newsId  the ID of the news item to delete
     */
    public void removeNews(Manager manager, String newsId) {
        Database.getInstance().removeNews(newsId);
        manager.log("Removed news id=" + newsId);
        Database.getInstance().saveToDisk();
    }

    /** Prints an academic performance report for all undergraduates, sorted by GPA descending. */
    public void createStatisticalReport() {
        Database db = Database.getInstance();
        System.out.println("=== ACADEMIC PERFORMANCE REPORT ===");
        List<Student> students = db.getAllStudents();
        if (students.isEmpty()) { System.out.println("No students found."); return; }
        students.sort((a, b) -> Double.compare(b.getGpa(), a.getGpa()));
        double totalGpa = 0;
        for (Student s : students) {
            System.out.printf("  %-20s GPA: %.2f  Credits: %d  Fails: %d%n",
                s.getFullName(), s.getGpa(), s.getTotalCredits(), s.getFailCount());
            totalGpa += s.getGpa();
        }
        System.out.printf("  Average GPA: %.2f%n", totalGpa / students.size());
    }

    /**
     * Lists all undergraduates sorted by the given field.
     *
     * @param sortBy {@code "gpa"}, {@code "year"}, or any other value for alphabetical by last name
     */
    public void viewStudents(String sortBy) {
        List<Student> students = new ArrayList<>(Database.getInstance().getAllStudents());
        switch (sortBy.toLowerCase()) {
            case "gpa":  students.sort((a, b) -> Double.compare(b.getGpa(), a.getGpa())); break;
            case "year": students.sort((a, b) -> Integer.compare(a.getYearOfStudy(), b.getYearOfStudy())); break;
            default:     students.sort((a, b) -> a.getLastName().compareTo(b.getLastName())); break;
        }
        System.out.println("=== STUDENTS (sorted by " + sortBy + ") ===");
        students.forEach(s -> System.out.println("  " + s));
    }

    /** Prints all teachers in the database. */
    public void viewTeachers() {
        System.out.println("=== TEACHERS ===");
        Database.getInstance().getAllTeachers().forEach(t -> System.out.println("  " + t));
    }

    /**
     * Prints detailed info for a teacher including rating and, if a researcher, h-index and paper count.
     *
     * @param teacherId the teacher's ID
     */
    public void viewTeacherDetails(String teacherId) {
        Database db = Database.getInstance();
        User u = db.getUserById(teacherId);
        User base = (u instanceof ResearcherDecorator) ? ((ResearcherDecorator) u).getWrappedUser() : u;
        if (!(base instanceof Teacher)) { System.out.println("[Error] Teacher not found: " + teacherId); return; }
        Teacher t = (Teacher) base;
        System.out.println("  Name:    " + t.getFullName());
        System.out.println("  ID:      " + t.getId());
        System.out.println("  Email:   " + t.getEmail());
        System.out.println("  Title:   " + t.getTitle());
        System.out.println("  School:  " + t.getSchool());
        System.out.printf( "  Rating:  %.2f / 5.0%n", db.getAverageRating(t.getId()));
        if (u instanceof ResearcherDecorator) {
            ResearcherDecorator rd = (ResearcherDecorator) u;
            System.out.printf("  H-Index: %.0f  |  Papers: %d%n",
                    rd.calculateHIndex(), rd.getResearchPapersList().size());
        }
    }

    /**
     * Adds a comment to a news item, prefixing it with the commenter's name.
     *
     * @param newsId     the ID of the news item to comment on
     * @param viewerName the display name of the commenter
     * @param comment    the comment text
     */
    public void addCommentToNews(String newsId, String viewerName, String comment) {
        Database db = Database.getInstance();
        db.getAllNews().stream()
            .filter(n -> n.getNewsId().equals(newsId))
            .findFirst()
            .ifPresent(n -> {
                n.addComment("[" + viewerName + "] " + comment);
                db.saveToDisk();
                System.out.println("[News] Comment added.");
            });
    }

    /**
     * Convenience method that looks up teacher and course by ID/code before delegating to
     * {@link #assignCourse}.
     *
     * @param manager   the approving manager
     * @param teacherId the teacher's ID
     * @param code      the course code
     */
    public void assignCourseById(Manager manager, String teacherId, String code) {
        Database db = Database.getInstance();
        User u = db.getUserById(teacherId);
        User base = (u instanceof ResearcherDecorator) ? ((ResearcherDecorator) u).getWrappedUser() : u;
        if (!(base instanceof Teacher)) { System.out.println("[Error] Teacher not found: " + teacherId); return; }
        Course course = db.getCourseByCode(code);
        if (course == null) { System.out.println("[Error] Course not found: " + code); return; }
        assignCourse(manager, (Teacher) base, course);
        db.saveToDisk();
    }

    /**
     * Convenience method that looks up teacher and course by ID/code before delegating to
     * {@link #unassignCourse}.
     *
     * @param manager   the manager performing the removal
     * @param teacherId the teacher's ID
     * @param code      the course code
     */
    public void unassignCourseById(Manager manager, String teacherId, String code) {
        Database db = Database.getInstance();
        User u = db.getUserById(teacherId);
        User base = (u instanceof ResearcherDecorator) ? ((ResearcherDecorator) u).getWrappedUser() : u;
        if (!(base instanceof Teacher)) { System.out.println("[Error] Teacher not found: " + teacherId); return; }
        Course course = db.getCourseByCode(code);
        if (course == null) { System.out.println("[Error] Course not found: " + code); return; }
        unassignCourse(manager, (Teacher) base, course);
        db.saveToDisk();
    }
}
