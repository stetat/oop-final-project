package project.patterns;

import project.enums.ManagerType;
import project.enums.TeacherTitle;
import project.models.actors.*;


public class UserFactory {

    public enum UserType { STUDENT, GRADUATE_STUDENT, TEACHER, MANAGER, ADMIN, TECH_SUPPORT }

    /**
     * Creates a basic user (Student, Admin, TechSupport, etc.) with common params.
     * For Teacher/Manager, use the specialized methods below.
     */
    public static User createUser(UserType type, String id, String password, String firstName, String lastName, String email) {
        switch (type) {
            case STUDENT: return new Student(id, password, firstName, lastName, email);
            case GRADUATE_STUDENT: return new GraduateStudent(id, password, firstName, lastName, email);
            case ADMIN: return new Admin(id, password, firstName, lastName, email, 200000);
            case TECH_SUPPORT: return new TechSupportSpecialist(id, password, firstName, lastName, email, 150000);
            default: throw new IllegalArgumentException("Use specialized factory method for: " + type);
        }
    }

    public static Teacher createTeacher(String id, String password, String firstName, String lastName, String email, double salary, TeacherTitle title) {
        return new Teacher(id, password, firstName, lastName, email, salary, title);
    }

    public static Manager createManager(String id, String password, String firstName, String lastName, String email, double salary, ManagerType type) {
        return new Manager(id, password, firstName, lastName, email, salary, type);
    }

    public static GraduateStudent createGraduateStudent(String id, String password, String firstName, String lastName, String email, boolean isPhD) {
        return new GraduateStudent(id, password, firstName, lastName, email, isPhD);
    }
}
