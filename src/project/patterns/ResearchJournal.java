package project.patterns;

import java.io.Serializable;
import java.util.*;
import project.models.others.ResearchPaper;


/**
 * A named research journal (Observer pattern subject).
 * When a paper is published, all subscribed {@link JournalObserver}s are notified.
 * The subscriber list is transient — subscriptions must be re-established after deserialization.
 */
public class ResearchJournal implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private List<ResearchPaper> papers = new ArrayList<>();
    private transient List<JournalObserver> subscribers = new ArrayList<>(); // transient: not serialized

    public ResearchJournal(String name) { this.name = name; }

    /**
     * Registers an observer to receive notifications for future papers.
     * Duplicate subscriptions are ignored.
     *
     * @param observer the observer to add
     */
    public void subscribe(JournalObserver observer) {
        if (!subscribers.contains(observer)) subscribers.add(observer);
    }
    /**
     * Removes an observer so it no longer receives publication events.
     *
     * @param observer the observer to remove
     */
    public void unsubscribe(JournalObserver observer) { subscribers.remove(observer); }

    /**
     * Adds the paper to this journal and notifies all current subscribers.
     *
     * @param paper the paper being published
     */
    public void publishPaper(ResearchPaper paper) {
        papers.add(paper);
        System.out.println("[Journal: " + name + "] New paper published: " + paper.getTitle());
        for (JournalObserver obs : subscribers)
            obs.onNewPaperPublished(name, paper);
    }

    public String getName() { return name; }
    public List<ResearchPaper> getPapers() { return papers; }

    /** Re-initializes the transient subscriber list after deserialization. */
    private Object readResolve() {
        if (subscribers == null) subscribers = new ArrayList<>();
        return this;
    }
}
