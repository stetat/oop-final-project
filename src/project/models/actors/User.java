package project.models.actors;

import java.io.Serializable;
import java.util.*;

import project.patterns.JournalObserver;
import project.patterns.ResearchJournal;
import project.models.enums.LanguageType;
import project.models.enums.Role;
import project.models.others.ResearchPaper;

/**
 * Abstract base for all users in the KBTU system.
 * Implements {@link JournalObserver} so any user can subscribe to research journals.
 * Serializable for persistence via the Singleton {@link project.storage.Database}.
 */
public abstract class User implements Serializable, JournalObserver {
    private static final long serialVersionUID = 1L;
    private String id;
    private Role role;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private LanguageType language = LanguageType.EN;
    private List<String> activityLog = new ArrayList<>();

    /** No-arg constructor required for Java serialization. */
    public User() {}

    /**
     * Creates a fully-initialized user.
     *
     * @param id        unique login ID (e.g. "STU01")
     * @param password  plain-text password
     * @param firstName first name
     * @param lastName  last name
     * @param email     university e-mail address
     * @param role      the user's system role
     */
    public User(String id, String password, String firstName, String lastName, String email, Role role) {
        this.id = id; this.password = password; this.firstName = firstName;
        this.lastName = lastName; this.email = email; this.role = role;
    }

    /**
     * Changes the UI language for this user and writes the change to the activity log.
     *
     * @param language the new preferred language
     */
    public void switchLanguage(LanguageType language) {
        this.language = language;
        log("Switched language to " + language);
    }

    /** Registers this user as an observer of the given journal. */
    public void subscribeToJournal(ResearchJournal journal) { journal.subscribe(this); }

    /** Removes this user from the journal's observer list. */
    public void unsubscribeFromJournal(ResearchJournal journal) { journal.unsubscribe(this); }

    /**
     * Called by the journal when a new paper is published.
     * Prints a notification message to stdout.
     *
     * @param journalName name of the journal that published the paper
     * @param paper       the newly published paper
     */
    @Override
    public void onNewPaperPublished(String journalName, ResearchPaper paper) {
        System.out.println("[Notification → " + firstName + " " + lastName + "] " + "New paper in '" + journalName + "': " + paper.getTitle());
    }

    /**
     * Appends a timestamped entry to this user's activity log.
     *
     * @param action short description of what happened (e.g. "Registered for course CS101")
     */
    public void log(String action) {
        activityLog.add(new Date() + " | " + id + " | " + action);
    }
    /** Returns the full activity log as a list of timestamped strings. */
    public List<String> getActivityLog() { return activityLog; }

    /** Returns the user's full name as "First Last". */
    public String getFullName() { return firstName + " " + lastName; }

    public String getId() { return id; }
    public void setId(String v) { this.id = v; }
    public Role getRole() { return role; }
    public void setRole(Role v) { this.role = v; }
    public String getPassword() { return password; }
    public void setPassword(String v) { this.password = v; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String v) { this.firstName = v; }
    public String getLastName() { return lastName; }
    public void setLastName(String v) { this.lastName = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public LanguageType getLanguage() { return language; }
    public void setLanguage(LanguageType v) { this.language = v; }

    @Override public int hashCode() { return Objects.hash(id); }
    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof User)) return false;
        return Objects.equals(id, ((User) obj).id);
    }
    @Override public String toString() {
        return getClass().getSimpleName() + "[id=" + id + ", name=" + firstName + " " + lastName + ", role=" + role + "]";
    }
}
