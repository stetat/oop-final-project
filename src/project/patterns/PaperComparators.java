package project.patterns;

import java.util.Comparator;
import project.models.others.ResearchPaper;


public class PaperComparators {

    /** Sort by citations descending (most cited first). */
    public static final Comparator<ResearchPaper> BY_CITATIONS_DESC = (a, b) -> Integer.compare(b.getCitations(), a.getCitations());

    public static final Comparator<ResearchPaper> BY_CITATIONS_ASC =
            Comparator.comparingInt(ResearchPaper::getCitations);

    public static final Comparator<ResearchPaper> BY_DATE_DESC = (a, b) -> {
                if (a.getPublishDate() == null && b.getPublishDate() == null) return 0;
                if (a.getPublishDate() == null) return 1;
                if (b.getPublishDate() == null) return -1;
                return b.getPublishDate().compareTo(a.getPublishDate());
            };

    public static final Comparator<ResearchPaper> BY_LENGTH_DESC = (a, b) -> Integer.compare(b.getPages(), a.getPages());

    public static final Comparator<ResearchPaper> BY_TITLE = Comparator.comparing(ResearchPaper::getTitle, String.CASE_INSENSITIVE_ORDER);
}
