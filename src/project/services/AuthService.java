package project.services;

import project.models.actors.User;
import project.storage.Database;

/**
 * Authentication service. All system access must go through this gateway per requirements.
 * Validates credentials against the Database.
 */
public class AuthService {
    private static User currentUser = null;

    /** Attempts login. Returns the User on success, null on failure. */
    public static User login(String id, String password) {
        User user = Database.getInstance().getUserById(id);
        if (user != null && password != null && password.equals(user.getPassword())) {
            currentUser = user;
            System.out.println("[AuthService] Login successful: " + user);
            return user;
        }
        System.out.println("[AuthService] Login FAILED for id=" + id);
        return null;
    }

    public static void logout() {
        System.out.println("[AuthService] Logged out: " + (currentUser != null ? currentUser.getId() : "none"));
        currentUser = null;
    }

    public static User getCurrentUser() { return currentUser; }
    public static boolean isLoggedIn() { return currentUser != null; }

    /** Checks if current user has required role. */
    public static boolean hasRole(project.enums.Role role) {
        return currentUser != null && currentUser.getRole() == role;
    }
}
