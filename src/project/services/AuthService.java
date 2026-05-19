package project.services;

import project.models.actors.User;
import project.models.enums.LanguageType;
import project.storage.Database;

/**
 * Authentication service. All system access must go through this gateway per requirements.
 * Validates credentials against the Database.
 */
public class AuthService {
    private static User currentUser = null;

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

    public static boolean hasRole(project.models.enums.Role role) {
        return currentUser != null && currentUser.getRole() == role;
    }

    public static void switchLanguage(User user, LanguageType lang) {
        Database db = Database.getInstance();
        User live = db.getUserById(user.getId());
        if (live != null) live.switchLanguage(lang);
        db.saveToDisk();
    }
}
