package project.models.actors;

import project.models.enums.Role;
import project.models.enums.School;
import project.models.enums.TeacherTitle;

/**
 * Represents a university teacher. Can hold a research title (PROFESSOR, MASTER, PHD),
 * which automatically grants researcher status when registered.
 */
public class Teacher extends Employee {
    private static final long serialVersionUID = 1L;
    private TeacherTitle title;
    private School school;

    public Teacher() {}
    public Teacher(String id, String password, String firstName, String lastName, String email, double salary, TeacherTitle title) {
        super(id, password, firstName, lastName, email, salary, Role.TEACHER);
        this.title = title;
    }

    public TeacherTitle getTitle() { return title; }
    public School getSchool() { return school; }
    public void setSchool(School v) { this.school = v; }
    public void setTitle(TeacherTitle v) { this.title = v; }

    @Override public String toString() {
        return "Teacher[id=" + getId() + ", name=" + getFullName() + ", title=" + title + "]";
    }
}
