package project.models.others;

import java.util.Comparator;
import java.util.List;

/** Capability interface for users who can publish and analyse research papers. */
public interface Researcher {

    /**
     * Calculates the h-index: the largest h where at least h papers each have ≥ h citations.
     *
     * @return the h-index value (0 if no papers)
     */
    double calculateHIndex();

    /**
     * Prints all research papers to stdout in the given order.
     *
     * @param comparator the sort order to apply before printing
     */
    void printPapers(Comparator<ResearchPaper> comparator);

    /** Returns the full list of research papers associated with this researcher. */
    List<ResearchPaper> getResearchPapersList();
}
