package project.patterns;

import java.util.Comparator;
import project.models.others.ResearchPaper;


/**
 * Strategy pattern: a catalogue of ready-made {@link java.util.Comparator}s for sorting
 * {@link project.models.others.ResearchPaper} collections. Pass any constant to
 * {@link project.models.others.Researcher#printPapers} or
 * {@link project.storage.Database#printAllResearchPapers}.
 */
public class PaperComparators {

    /** Most-cited papers first. */
    public static final Comparator<ResearchPaper> BY_CITATIONS_DESC = (a, b) -> Integer.compare(b.getCitations(), a.getCitations());

    /** Least-cited papers first. */
    public static final Comparator<ResearchPaper> BY_CITATIONS_ASC =
            Comparator.comparingInt(ResearchPaper::getCitations);

    /** Newest papers first; papers with a null publish date sort last. */
    public static final Comparator<ResearchPaper> BY_DATE_DESC = (a, b) -> {
                if (a.getPublishDate() == null && b.getPublishDate() == null) return 0;
                if (a.getPublishDate() == null) return 1;
                if (b.getPublishDate() == null) return -1;
                return b.getPublishDate().compareTo(a.getPublishDate());
            };

    /** Longest papers (by page count) first. */
    public static final Comparator<ResearchPaper> BY_LENGTH_DESC = (a, b) -> Integer.compare(b.getPages(), a.getPages());

    /** Alphabetical by title, case-insensitive. */
    public static final Comparator<ResearchPaper> BY_TITLE = Comparator.comparing(ResearchPaper::getTitle, String.CASE_INSENSITIVE_ORDER);
}
