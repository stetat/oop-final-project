package project.patterns;

import project.models.others.ResearchPaper;

/**
 * PATTERN: Observer
 * Used for university research journal subscriptions.
 * Subscribers are notified when a new paper is published.
 */
public interface JournalObserver {
    void onNewPaperPublished(String journalName, ResearchPaper paper);
}
