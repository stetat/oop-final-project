package project.models.others;

import java.util.Comparator;
import java.util.List;

public interface Researcher {
    /**
     * Calculates h-index: largest h such that h papers have >= h citations.
     */
    double calculateHIndex();
    /**
     * Prints research papers sorted by given comparator.
     */
    void printPapers(Comparator<ResearchPaper> comparator);
    /**
     * Returns this researcher's list of research papers.
     */
    List<ResearchPaper> getResearchPapersList();
}
