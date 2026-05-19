package project.services;

import java.util.*;
import project.models.actors.*;
import project.models.others.*;
import project.patterns.ResearcherDecorator;
import project.storage.Database;
import project.models.others.Course;
import project.models.others.News;

public class ManagerService {

    public void approveRegistration(Manager manager, Student student, Course course) {
        boolean ok = student.registerForCourse(course);
        manager.log("Approved registration: " + student.getId() + " → " + course.getCourseCode());
        System.out.println("[Manager " + manager.getFullName() + "] Registration " +
            (ok ? "APPROVED" : "FAILED") + ": " + student.getFullName() + " → " + course.getCourseName());
    }

    public void rejectRegistration(Manager manager, Student student, Course course) {
        manager.log("Rejected registration: " + student.getId() + " → " + course.getCourseCode());
        System.out.println("[Manager] Rejected: " + student.getFullName() + " → " + course.getCourseName());
    }

    public void assignCourse(Manager manager, Teacher teacher, Course course) {
        course.addInstructorId(teacher.getId());
        Database.getInstance().saveCourse(course);
        manager.log("Assigned " + teacher.getId() + " to " + course.getCourseCode());
        System.out.println("[Manager] Assigned " + teacher.getFullName() + " → " + course.getCourseName());
    }

    public void unassignCourse(Manager manager, Teacher teacher, Course course) {
        course.removeInstructorId(teacher.getId());
        Database.getInstance().saveCourse(course);
        manager.log("Unassigned " + teacher.getId() + " from " + course.getCourseCode());
    }

    public void addNews(Manager manager, News news) {
        Database.getInstance().addNews(news);
        manager.log("Added news: " + news.getTitle());
        System.out.println("[News Added] " + news.getTitle());
        Database.getInstance().saveToDisk();
    }

    public void removeNews(Manager manager, String newsId) {
        Database.getInstance().removeNews(newsId);
        manager.log("Removed news id=" + newsId);
        Database.getInstance().saveToDisk();
    }

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

    public void viewTeachers() {
        System.out.println("=== TEACHERS ===");
        Database.getInstance().getAllTeachers().forEach(t -> System.out.println("  " + t));
    }

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
