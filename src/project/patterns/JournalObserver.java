package project.patterns;

import project.models.others.ResearchPaper;

/**
 * Observer interface for research journal subscriptions (Observer pattern).
 * Any user can implement this to be notified when a new paper is published.
 */
public interface JournalObserver {

    /**
     * Invoked by {@link ResearchJournal#publishPaper} for every subscriber.
     *
     * @param journalName the name of the journal that published the paper
     * @param paper       the newly published paper
     */
    void onNewPaperPublished(String journalName, ResearchPaper paper);
}
