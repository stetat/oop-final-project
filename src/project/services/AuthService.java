package project.services;

import project.models.actors.User;
import project.models.enums.LanguageType;
import project.storage.Database;

/**
 * Stateless authentication gateway. Holds the currently logged-in user in a static field
 * and validates credentials against the {@link project.storage.Database}.
 */
public class AuthService {
    private static User currentUser = null;

    /**
     * Attempts to log in with the given credentials.
     *
     * @param id       the user's ID
     * @param password the plain-text password to check
     * @return the authenticated user, or {@code null} if credentials are invalid
     */
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

    /** Clears the current session. The next action will require a fresh login. */
    public static void logout() {
        System.out.println("[AuthService] Logged out: " + (currentUser != null ? currentUser.getId() : "none"));
        currentUser = null;
    }

    /** Returns the user who is currently logged in, or {@code null} if no session is active. */
    public static User getCurrentUser() { return currentUser; }

    /** Returns {@code true} if a user is currently logged in. */
    public static boolean isLoggedIn() { return currentUser != null; }

    /**
     * Checks whether the logged-in user has the given role.
     *
     * @param role the role to test
     * @return {@code true} if logged in and the user's role matches
     */
    public static boolean hasRole(project.models.enums.Role role) {
        return currentUser != null && currentUser.getRole() == role;
    }

    /**
     * Changes the UI language for the given user and saves the change to disk.
     *
     * @param user the user whose preference should be updated
     * @param lang the new language
     */
    public static void switchLanguage(User user, LanguageType lang) {
        Database db = Database.getInstance();
        User live = db.getUserById(user.getId());
        if (live != null) live.switchLanguage(lang);
        db.saveToDisk();
    }
}
