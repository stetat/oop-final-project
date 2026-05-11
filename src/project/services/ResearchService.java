package project.services;

import java.util.*;
import project.models.actors.User;
import project.models.others.*;
import project.patterns.PaperComparators;
import project.storage.Database;

/**
 * Service for research-related operations: finding top researchers,
 * printing global paper lists, and generating announcements.
 */
public class ResearchService {

    public void announceTopCitedResearcher() {
        User top = Database.getInstance().getTopCitedResearcher();
        if (top == null) { System.out.println("[Research] No researchers found."); return; }
        int total = ((Researcher) top).getResearchPapersList().stream().mapToInt(ResearchPaper::getCitations).sum();
        String title = "Top Cited Researcher: " + top.getFullName();
        String content = top.getFullName() + " leads with " + total + " total citations.";
        News news = new News(title, content, true);
        Database.getInstance().addNews(news);
        System.out.println("[Research Announcement] " + title);
    }

    public void printTopPapersByCitations() {
        Database.getInstance().printAllResearchPapers(PaperComparators.BY_CITATIONS_DESC);
    }

    public void printPapersByDate() {
        Database.getInstance().printAllResearchPapers(PaperComparators.BY_DATE_DESC);
    }

    public List<Researcher> getResearchersByTotalCitations() {
        List<Researcher> researchers = Database.getInstance().getAllResearchers();
        researchers.sort((a, b) -> {
            int sumA = a.getResearchPapersList().stream().mapToInt(ResearchPaper::getCitations).sum();
            int sumB = b.getResearchPapersList().stream().mapToInt(ResearchPaper::getCitations).sum();
            return Integer.compare(sumB, sumA);
        });
        return researchers;
    }
}
