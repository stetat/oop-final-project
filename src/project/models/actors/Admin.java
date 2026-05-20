package project.models.actors;

import project.models.enums.Role;

/** System administrator — has full access to user management and activity logs. */
public class Admin extends Employee {
    private static final long serialVersionUID = 1L;

    public Admin() {}
    public Admin(String id, String password, String firstName, String lastName, String email, double salary) {
        super(id, password, firstName, lastName, email, salary, Role.ADMIN);
    }

    @Override public String toString() {
        return "Admin[id=" + getId() + ", name=" + getFullName() + "]";
    }
}
