package project.patterns;

import java.io.Serializable;
import java.util.*;
import project.models.others.ResearchPaper;
import project.models.others.Researcher;
import project.storage.Database;


public class ResearcherDecorator implements Researcher, Serializable {
    private static final long serialVersionUID = 1L;

    private final String wrappedUserId;
    private final String displayName;
    private final List<ResearchPaper> papers = new ArrayList<>();

    public ResearcherDecorator(String userId, String displayName) {
        this.wrappedUserId = userId;
        this.displayName = displayName;
    }

    public void addResearchPaper(ResearchPaper paper) {
        papers.add(paper);
        project.models.others.News news = new project.models.others.News("New Research Paper by " + displayName, displayName + " published: " + paper.getTitle(), true);
        Database.getInstance().addNews(news);
    }

    @Override
    public double calculateHIndex() {
        if (papers.isEmpty()) return 0;
        List<Integer> cits = new ArrayList<>();
        for (ResearchPaper p : papers) cits.add(p.getCitations());
        cits.sort(Collections.reverseOrder());
        int h = 0;
        for (int i = 0; i < cits.size(); i++) { if (cits.get(i) >= i + 1) h = i + 1; else break; }
        return h;
    }

    @Override
    public void printPapers(Comparator<ResearchPaper> comparator) {
        List<ResearchPaper> sorted = new ArrayList<>(papers);
        sorted.sort(comparator);
        System.out.println("=== Research Papers: " + displayName + " (decorated) ===");
        for (ResearchPaper p : sorted) System.out.println("  " + p.getCitation("Plain Text"));
    }

    @Override public List<ResearchPaper> getResearchPapersList() { return papers; }

    public String getWrappedUserId() { return wrappedUserId; }
    public String getDisplayName() { return displayName; }
}
