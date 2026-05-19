package project.models.others;

import java.util.Comparator;
import java.util.List;

public interface Researcher {
    /**
     * Calculates h-index: largest h such that h papers have >= h citations.
     */
    double calculateHIndex();
    void printPapers(Comparator<ResearchPaper> comparator);
    List<ResearchPaper> getResearchPapersList();
}
