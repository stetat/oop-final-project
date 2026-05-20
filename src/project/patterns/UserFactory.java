package project.patterns;

import project.models.actors.*;
import project.models.enums.ManagerType;
import project.models.enums.TeacherTitle;


/**
 * Factory (Factory Method pattern) for creating all user types.
 * Centralises object construction so callers don't need to know which constructor to call.
 */
public class UserFactory {

    public enum UserType { STUDENT, GRADUATE_STUDENT, TEACHER, MANAGER, ADMIN, TECH_SUPPORT }

    /**
     * Creates a user for simple types (Student, GraduateStudent, Admin, TechSupport).
     * Use the dedicated methods for Teacher and Manager, which require extra parameters.
     *
     * @param type      the kind of user to create
     * @param id        unique login ID
     * @param password  plain-text password
     * @param firstName first name
     * @param lastName  last name
     * @param email     university e-mail
     * @return the newly created user
     * @throws IllegalArgumentException if called with TEACHER or MANAGER
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

    /**
     * Creates a Teacher with the given academic title.
     *
     * @param salary starting annual salary in KZT
     * @param title  academic rank; PROFESSOR/MASTER/PHD grant researcher status automatically
     */
    public static Teacher createTeacher(String id, String password, String firstName, String lastName, String email, double salary, TeacherTitle title) {
        return new Teacher(id, password, firstName, lastName, email, salary, title);
    }

    /**
     * Creates a Manager of a specific type.
     *
     * @param salary starting annual salary in KZT
     * @param type   OR (Office of Registrar) or DEPARTMENTS
     */
    public static Manager createManager(String id, String password, String firstName, String lastName, String email, double salary, ManagerType type) {
        return new Manager(id, password, firstName, lastName, email, salary, type);
    }

    /**
     * Creates a GraduateStudent.
     *
     * @param isPhD {@code true} for a PhD student, {@code false} for a Master's student
     */
    public static GraduateStudent createGraduateStudent(String id, String password, String firstName, String lastName, String email, boolean isPhD) {
        return new GraduateStudent(id, password, firstName, lastName, email, isPhD);
    }
}
