package project.models.actors;

import java.io.Serializable;
import java.util.*;

import project.patterns.JournalObserver;
import project.patterns.ResearchJournal;
import project.models.enums.LanguageType;
import project.models.enums.Role;
import project.models.others.ResearchPaper;

/**
 * Abstract base for all users. Implements JournalObserver (Observer pattern).
 * Serializable for data persistence (Singleton Database pattern).
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

    public User() {}
    public User(String id, String password, String firstName, String lastName, String email, Role role) {
        this.id = id; this.password = password; this.firstName = firstName;
        this.lastName = lastName; this.email = email; this.role = role;
    }

    public void switchLanguage(LanguageType language) {
        this.language = language;
        log("Switched language to " + language);
    }

    public void subscribeToJournal(ResearchJournal journal) { journal.subscribe(this); }
    public void unsubscribeFromJournal(ResearchJournal journal) { journal.unsubscribe(this); }

    @Override
    public void onNewPaperPublished(String journalName, ResearchPaper paper) {
        System.out.println("[Notification → " + firstName + " " + lastName + "] " + "New paper in '" + journalName + "': " + paper.getTitle());
    }

    public void log(String action) {
        activityLog.add(new Date() + " | " + id + " | " + action);
    }
    public List<String> getActivityLog() { return activityLog; }

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
