package project.services;

import java.util.ArrayList;
import java.util.List;
import project.models.actors.Student;
import project.models.actors.Teacher;
import project.models.actors.User;
import project.models.others.Course;
import project.patterns.ResearcherDecorator;
import project.storage.Database;

/** Read-only views of students, teachers, and course-to-teacher relationships. */
public class UserLookupService {

    /**
     * Prints all students (including graduate students) sorted by the given field.
     *
     * @param sortBy {@code "gpa"}, {@code "year"}, or any other value for alphabetical by last name
     */
    public void listStudents(String sortBy) {
        Database db = Database.getInstance();
        List<Student> students = new ArrayList<>(db.getAllStudentsIncludingGrad());
        switch (sortBy == null ? "name" : sortBy.toLowerCase()) {
            case "gpa":  students.sort((a, b) -> Double.compare(b.getGpa(), a.getGpa())); break;
            case "year": students.sort((a, b) -> Integer.compare(a.getYearOfStudy(), b.getYearOfStudy())); break;
            default:     students.sort((a, b) -> a.getLastName().compareTo(b.getLastName())); break;
        }
        System.out.println("=== STUDENTS (sorted by " + sortBy + ") ===");
        students.forEach(s -> System.out.println("  " + s));
    }

    /**
     * Prints a student's transcript plus their organization membership.
     *
     * @param studentId the student's ID
     */
    public void viewStudentDetails(String studentId) {
        Database db = Database.getInstance();
        User u = db.getUserById(studentId);
        User base = (u instanceof ResearcherDecorator) ? ((ResearcherDecorator) u).getWrappedUser() : u;
        if (!(base instanceof Student)) { System.out.println("[Error] Student not found: " + studentId); return; }
        Student s = (Student) base;
        s.viewTranscript();
        System.out.println("  Organization: " + (s.getOrganization() != null ? s.getOrganization() : "none")
                + (s.isOrganizationHead() ? " (HEAD)" : ""));
    }

    /**
     * Prints a teacher's profile including rating and, if a researcher, h-index and paper count.
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
     * Returns all courses the student is currently registered in.
     *
     * @param student the student to look up
     * @return list of enrolled courses (never null, may be empty)
     */
    public List<Course> getCoursesForStudent(Student student) {
        Database db = Database.getInstance();
        List<Course> result = new ArrayList<>();
        if (student.getRegisteredCourseIds() != null) {
            for (String code : student.getRegisteredCourseIds()) {
                Course c = db.getCourseByCode(code);
                if (c != null) result.add(c);
            }
        }
        return result;
    }

    /**
     * Prints the profile of every instructor assigned to a course.
     *
     * @param courseCode the course code to look up
     */
    public void viewTeacherForCourse(String courseCode) {
        Database db = Database.getInstance();
        Course course = db.getCourseByCode(courseCode);
        if (course == null || course.getInstructorIds().isEmpty()) {
            System.out.println("[Info] No instructor assigned to this course.");
            return;
        }
        for (String tid : course.getInstructorIds()) {
            viewTeacherDetails(tid);
        }
    }
}
