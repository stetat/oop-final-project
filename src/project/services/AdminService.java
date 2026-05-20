package project.services;

import project.models.actors.Admin;
import project.models.actors.User;
import project.storage.Database;

/** Service for admin operations: managing users and viewing system activity logs. */
public class AdminService {

    /**
     * Persists a new user and logs the action on the admin's activity log.
     *
     * @param admin the admin performing the action
     * @param user  the user to add
     */
    public void addUser(Admin admin, User user) {
        Database.getInstance().saveUser(user);
        admin.log("Added user: " + user.getId());
        System.out.println("[Admin] Added user: " + user);
        Database.getInstance().saveToDisk();
    }

    /**
     * Deletes a user from the database and logs the action.
     *
     * @param admin the admin performing the action
     * @param user  the user to remove
     */
    public void removeUser(Admin admin, User user) {
        Database.getInstance().removeUser(user.getId());
        admin.log("Removed user: " + user.getId());
        System.out.println("[Admin] Removed user: " + user.getId());
        Database.getInstance().saveToDisk();
    }

    /**
     * Overwrites a user's stored data with the given object's current state.
     *
     * @param admin the admin performing the action
     * @param user  the user with updated fields
     */
    public void updateUser(Admin admin, User user) {
        Database.getInstance().saveUser(user);
        admin.log("Updated user: " + user.getId());
        System.out.println("[Admin] Updated user: " + user.getId());
    }

    /** Prints every activity log entry for every user in the database. */
    public void viewLogFiles() {
        System.out.println("=== SYSTEM LOG FILES ===");
        Database.getInstance().getAllUsers().forEach(u ->
            u.getActivityLog().forEach(entry -> System.out.println("  " + entry)));
    }
}
