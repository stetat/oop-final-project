package project.services;

import java.util.List;
import project.models.actors.User;
import project.patterns.ResearchJournal;
import project.storage.Database;

/** Manages research journals: creation, listing, and subscription. */
public class JournalService {

    /**
     * Creates a new research journal with the given name.
     * Does nothing if a journal with that name already exists.
     *
     * @param name the journal name (must be non-blank)
     */
    public void createJournal(String name) {
        Database db = Database.getInstance();
        if (name == null || name.isBlank()) { System.out.println("[Journal] Name cannot be empty."); return; }
        if (db.getJournalByName(name) != null) { System.out.println("[Journal] A journal with that name already exists."); return; }
        db.addJournal(new ResearchJournal(name));
        db.saveToDisk();
        System.out.println("[Journal] Created: " + name);
    }

    /** Prints all journals in the system along with their paper counts. */
    public void listJournals() {
        Database db = Database.getInstance();
        List<ResearchJournal> journals = db.getAllJournals();
        if (journals.isEmpty()) { System.out.println("[Journal] No journals yet."); return; }
        System.out.println("=== JOURNALS ===");
        journals.forEach(j -> System.out.println("  " + j.getName() + "  (" + j.getPapers().size() + " papers)"));
    }

    /**
     * Subscribes a user to a journal so they receive notifications when new papers are published.
     * Prints the available journals and an error if the name is not found.
     *
     * @param user        the user who wants to subscribe
     * @param journalName the journal to subscribe to
     */
    public void subscribeJournal(User user, String journalName) {
        Database db = Database.getInstance();
        ResearchJournal journal = db.getJournalByName(journalName.trim());
        if (journal == null) {
            System.out.println("[Journal] Not found: '" + journalName + "'");
            listJournals();
            return;
        }
        user.subscribeToJournal(journal);
        db.addJournalSubscription(journal.getName(), user.getId());
        db.saveToDisk();
        System.out.println("[Journal] Subscribed to: " + journal.getName());
    }
}
