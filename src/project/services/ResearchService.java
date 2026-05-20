package project.services;

import java.util.*;

import project.models.actors.*;
import project.models.enums.School;
import project.models.errors.InvalidSupervisorException;
import project.models.others.*;
import project.patterns.PaperComparators;
import project.patterns.ResearchJournal;
import project.patterns.ResearcherDecorator;
import project.storage.Database;

/** Orchestrates all research-related operations: papers, citations, supervisors, and rankings. */
public class ResearchService {

    /**
     * Assigns a research supervisor to a graduate student.
     * Prints an error message if the supervisor doesn't meet the h-index requirement.
     *
     * @param grad       the graduate student needing supervision
     * @param supervisor the proposed supervisor
     */
    public void setSupervisor(GraduateStudent grad, ResearcherDecorator supervisor) {
        try {
            grad.setResearchSupervisor(supervisor);
            Database.getInstance().saveToDisk();
            System.out.println("[Supervisor] Assigned: " + supervisor.getWrappedUser().getFullName());
        } catch (InvalidSupervisorException e) {
            System.out.println("[Exception] " + e.getMessage());
        }
    }

    /**
     * Attaches a diploma/thesis paper to a graduate student's profile and saves to disk.
     *
     * @param grad  the graduate student
     * @param paper the paper to attach
     */
    public void addDiplomaProject(GraduateStudent grad, ResearchPaper paper) {
        grad.addDiplomaProject(paper);
        Database.getInstance().saveToDisk();
        System.out.println("[Diploma] Added: " + paper.getTitle());
    }

    /**
     * Publishes a paper for a researcher. Optionally links it to a project and/or a journal.
     * Journal publication triggers notifications to all journal subscribers.
     *
     * @param user        the researcher publishing the paper
     * @param paper       the paper to publish
     * @param projectId   the project to link to, or {@code null}/blank to skip
     * @param journalName the journal to publish in, or {@code null}/blank to skip
     */
    public void addResearcherPaper(User user, ResearchPaper paper, String projectId, String journalName) {
        Database db = Database.getInstance();
        ResearcherDecorator researcher = getResearcherOf(user);
        if (researcher == null) { System.out.println("[Error] You don't have researcher status yet."); return; }

        if (projectId != null && !projectId.isBlank()) {
            ResearchProject proj = db.getProjectById(projectId);
            if (proj == null) {
                System.out.println("[Warning] Project not found: " + projectId + ". Paper saved without a project.");
            } else {
                boolean isMember = proj.getParticipants().stream()
                        .anyMatch(p -> p.getWrappedUser().getId().equals(user.getId()));
                if (!isMember) {
                    System.out.println("[Project] You are not a member of project '" + proj.getTopic()
                            + "'. Join it first with 'joinproject'.");
                } else {
                    paper.setProjectId(projectId);
                    proj.addPaper(paper);
                    System.out.println("[Project] Linked to: " + proj.getTopic());
                }
            }
        }

        researcher.addResearchPaper(paper);
        user.log("Published paper: " + paper.getTitle());

        if (journalName != null && !journalName.isBlank()) {
            ResearchJournal sysJournal = db.getJournalByName(journalName);
            if (sysJournal == null) {
                System.out.println("[Journal] Not found: " + journalName + ". Paper saved without journal publication.");
            } else {
                sysJournal.publishPaper(paper);
                String notifMsg = "New paper in '" + sysJournal.getName() + "': \"" + paper.getTitle() + "\" by " + paper.getAuthors();
                for (String subscriberId : db.getSubscribersForJournal(sysJournal.getName())) {
                    if (!subscriberId.equals(user.getId()))
                        db.addNotification(new Notification(subscriberId, notifMsg));
                }
                System.out.println("[Journal] Published to: " + sysJournal.getName());
            }
        }

        db.saveToDisk();
    }

    /**
     * Increments the citation count of the paper with the given DOI (owned by the researcher).
     *
     * @param researcher the researcher who owns the paper
     * @param doi        the DOI of the paper to cite
     */
    public void recordCitation(ResearcherDecorator researcher, String doi) {
        Database db = Database.getInstance();
        if (researcher == null) { System.out.println("[Error] No researcher context."); return; }
        ResearchPaper paper = researcher.getResearchPapersList().stream()
                .filter(p -> doi.equals(p.getDoi()))
                .findFirst().orElse(null);
        if (paper == null) { System.out.println("[Error] No paper found with DOI: " + doi); return; }
        researcher.addCitation(paper);
        db.saveToDisk();
    }

    /** Finds the most-cited researcher in the system and posts a news item announcing them. */
    public void announceTopCitedResearcher() {
        ResearcherDecorator top = Database.getInstance().getTopCitedResearcher();
        if (top == null) { System.out.println("[Research] No researchers found."); return; }
        int total = top.getResearchPapersList().stream().mapToInt(ResearchPaper::getCitations).sum();
        String title = "Top Cited Researcher: " + top.getWrappedUser().getFullName();
        String content = top.getWrappedUser().getFullName() + " leads with " + total + " total citations.";
        News news = new News(title, content, true);
        Database.getInstance().addNews(news);
        System.out.println("[Research Announcement] " + title);
    }

    /** Prints all research papers across the system sorted by citation count (highest first). */
    public void printTopPapersByCitations() {
        Database.getInstance().printAllResearchPapers(PaperComparators.BY_CITATIONS_DESC);
    }

    /** Prints all research papers across the system sorted by publish date (newest first). */
    public void printPapersByDate() {
        Database.getInstance().printAllResearchPapers(PaperComparators.BY_DATE_DESC);
    }

    /**
     * Adds a paper directly for a teacher who already has researcher status in the database.
     * Prints an error if the teacher is not a researcher.
     *
     * @param teacher the teacher (must be wrapped in a ResearcherDecorator in the DB)
     * @param paper   the paper to add
     */
    public void addPaper(Teacher teacher, ResearchPaper paper) {
        Database db = Database.getInstance();
        User inDb = db.getUserById(teacher.getId());
        if (!(inDb instanceof ResearcherDecorator)) {
            System.out.println("[Research] " + teacher.getFullName() + " does not have a research title.");
            return;
        }
        ((ResearcherDecorator) inDb).addResearchPaper(paper);
        teacher.log("Published paper: " + paper.getTitle());
    }

    /**
     * Returns all researchers sorted by total citation count descending.
     *
     * @return sorted list; most-cited researcher is first
     */
    public List<ResearcherDecorator> getResearchersByTotalCitations() {
        List<ResearcherDecorator> researchers = Database.getInstance().getAllResearchers();
        researchers.sort((a, b) -> {
            int sumA = a.getResearchPapersList().stream().mapToInt(ResearchPaper::getCitations).sum();
            int sumB = b.getResearchPapersList().stream().mapToInt(ResearchPaper::getCitations).sum();
            return Integer.compare(sumB, sumA);
        });
        return researchers;
    }

    /**
     * Resolves the {@link ResearcherDecorator} for a user: checks the object itself first,
     * then falls back to the database entry.
     *
     * @return the decorator, or {@code null} if the user has no researcher status
     */
    private ResearcherDecorator getResearcherOf(User u) {
        if (u == null) return null;
        if (u instanceof ResearcherDecorator) return (ResearcherDecorator) u;
        User fromDb = Database.getInstance().getUserById(u.getId());
        return (fromDb instanceof ResearcherDecorator) ? (ResearcherDecorator) fromDb : null;
    }

    /**
     * Prints the top-cited researcher for the given school along with their h-index.
     *
     * @param school the faculty to filter by
     */
    public void printTopCitedBySchool(School school) {
        ResearcherDecorator top = Database.getInstance().getTopCitedResearcherBySchool(school);
        if (top == null) { System.out.println("[Top] No researchers found in " + school + "."); return; }
        int total = top.getResearchPapersList().stream().mapToInt(ResearchPaper::getCitations).sum();
        System.out.println("[Top " + school + "] " + top.getWrappedUser().getFullName()
                + " — " + total + " total citations, h-index=" + (int) top.calculateHIndex());
    }

    /**
     * Prints the researcher with the most citations from papers published in {@code year}.
     *
     * @param year the publication year (e.g. 2023)
     */
    public void printTopCitedByYear(int year) {
        ResearcherDecorator top = Database.getInstance().getTopCitedResearcherOfYear(year);
        if (top == null) { System.out.println("[Top] No papers found for year " + year + "."); return; }
        int total = (int) top.getResearchPapersList().stream()
                .filter(p -> p.getPublishDate() != null
                        && new java.util.Calendar.Builder().setInstant(p.getPublishDate()).build()
                                   .get(java.util.Calendar.YEAR) == year)
                .mapToInt(ResearchPaper::getCitations).sum();
        System.out.println("[Top " + year + "] " + top.getWrappedUser().getFullName()
                + " — " + total + " citations in " + year + ", h-index=" + (int) top.calculateHIndex());
    }

    /**
     * Attaches a textual citation note to a paper (who cited it and what they wrote).
     *
     * @param user  the user adding the citation note
     * @param paper the paper being cited
     * @param text  the citation text
     */
    public void addTextCitation(User user, ResearchPaper paper, String text) {
        paper.addPaperCitation(new Citation(user.getId(), user.getFullName(), text));
        Database.getInstance().saveToDisk();
        System.out.println("[Citation] Added.");
    }
}
