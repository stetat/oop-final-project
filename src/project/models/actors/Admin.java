package project.models.actors;

import project.enums.Role;
import project.storage.Database;

/**
 * System administrator. Full control over user management and system log access.
 */
public class Admin extends Employee {
    private static final long serialVersionUID = 1L;

    public Admin() {}
    public Admin(String id, String password, String firstName, String lastName, String email, double salary) {
        super(id, password, firstName, lastName, email, salary, Role.ADMIN);
    }

    public void addUser(User user) {
        Database.getInstance().saveUser(user);
        log("Added user: " + user.getId());
        System.out.println("[Admin] Added user: " + user);
    }

    public void removeUser(User user) {
        Database.getInstance().removeUser(user.getId());
        log("Removed user: " + user.getId());
        System.out.println("[Admin] Removed user: " + user.getId());
    }

    public void updateUser(User user) {
        Database.getInstance().saveUser(user); // overwrite
        log("Updated user: " + user.getId());
        System.out.println("[Admin] Updated user: " + user.getId());
    }

    /** Views all user activity logs from the Database. */
    public void viewLogFiles() {
        System.out.println("=== SYSTEM LOG FILES ===");
        Database.getInstance().getAllUsers().forEach(u -> u.getActivityLog().forEach(entry -> System.out.println("  " + entry)));
    }

    @Override public String toString() {
        return "Admin[id=" + getId() + ", name=" + getFullName() + "]";
    }
}
