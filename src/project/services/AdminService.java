package project.services;

import project.models.actors.Admin;
import project.models.actors.User;
import project.storage.Database;

public class AdminService {

    public void addUser(Admin admin, User user) {
        Database.getInstance().saveUser(user);
        admin.log("Added user: " + user.getId());
        System.out.println("[Admin] Added user: " + user);
        Database.getInstance().saveToDisk();
    }

    public void removeUser(Admin admin, User user) {
        Database.getInstance().removeUser(user.getId());
        admin.log("Removed user: " + user.getId());
        System.out.println("[Admin] Removed user: " + user.getId());
        Database.getInstance().saveToDisk();
    }

    public void updateUser(Admin admin, User user) {
        Database.getInstance().saveUser(user);
        admin.log("Updated user: " + user.getId());
        System.out.println("[Admin] Updated user: " + user.getId());
    }

    public void viewLogFiles() {
        System.out.println("=== SYSTEM LOG FILES ===");
        Database.getInstance().getAllUsers().forEach(u ->
            u.getActivityLog().forEach(entry -> System.out.println("  " + entry)));
    }
}
