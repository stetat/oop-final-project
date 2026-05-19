package project.patterns;

import java.util.*;

import project.models.actors.User;
import project.models.enums.LanguageType;
import project.models.enums.Role;
import project.models.others.News;
import project.models.others.ResearchPaper;
import project.models.others.Researcher;
import project.patterns.ResearchJournal;
import project.storage.Database;


public class ResearcherDecorator extends User implements Researcher {
    private static final long serialVersionUID = 1L;

    private static final int[] CITATION_MILESTONES = {5, 10, 25, 50, 100};

    private final User wrappedUser;
    private final List<ResearchPaper> extraPapers = new ArrayList<>();

    public ResearcherDecorator(User user) {
        this.wrappedUser = user;
    }

    // ── Delegate all User state to wrappedUser ───────────────────────────────
    @Override public String getId()              { return wrappedUser.getId(); }
    @Override public void setId(String v)        { wrappedUser.setId(v); }
    @Override public Role getRole()              { return wrappedUser.getRole(); }
    @Override public void setRole(Role v)        { wrappedUser.setRole(v); }
    @Override public String getPassword()        { return wrappedUser.getPassword(); }
    @Override public void setPassword(String v)  { wrappedUser.setPassword(v); }
    @Override public String getFirstName()       { return wrappedUser.getFirstName(); }
    @Override public void setFirstName(String v) { wrappedUser.setFirstName(v); }
    @Override public String getLastName()        { return wrappedUser.getLastName(); }
    @Override public void setLastName(String v)  { wrappedUser.setLastName(v); }
    @Override public String getEmail()           { return wrappedUser.getEmail(); }
    @Override public void setEmail(String v)     { wrappedUser.setEmail(v); }
    @Override public LanguageType getLanguage()        { return wrappedUser.getLanguage(); }
    @Override public void setLanguage(LanguageType v)  { wrappedUser.setLanguage(v); }
    @Override public String getFullName()              { return wrappedUser.getFullName(); }
    @Override public void log(String action)           { wrappedUser.log(action); }
    @Override public List<String> getActivityLog()     { return wrappedUser.getActivityLog(); }
    @Override public void switchLanguage(LanguageType l) { wrappedUser.switchLanguage(l); }
    @Override public void subscribeToJournal(ResearchJournal journal)   { wrappedUser.subscribeToJournal(journal); }
    @Override public void unsubscribeFromJournal(ResearchJournal journal) { wrappedUser.unsubscribeFromJournal(journal); }
    @Override public void onNewPaperPublished(String journalName, ResearchPaper paper) {
        wrappedUser.onNewPaperPublished(journalName, paper);
    }

    @Override public int hashCode() { return wrappedUser.hashCode(); }
    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof ResearcherDecorator)
            return wrappedUser.equals(((ResearcherDecorator) obj).wrappedUser);
        return wrappedUser.equals(obj);
    }


    public void addResearchPaper(ResearchPaper paper) {
        extraPapers.add(paper);
        String name = wrappedUser.getFullName();
        Database.getInstance().addNews(
            new News("New Research Paper by " + name, name + " published: " + paper.getTitle(), true));
        System.out.println("[News] Research paper published: " + paper.getTitle());
    }

    public void addCitation(ResearchPaper paper) {
        if (!extraPapers.contains(paper)) { System.out.println("[Citation] Paper not found in this researcher's list."); return; }
        int before = paper.getCitations();
        paper.setCitations(before + 1);
        int after = paper.getCitations();
        System.out.println("[Citation] " + paper.getTitle() + " now has " + after + " citation(s).");
        for (int milestone : CITATION_MILESTONES) {
            if (before < milestone && after >= milestone) {
                String name = wrappedUser.getFullName();
                Database.getInstance().addNews(new News(
                    "\"" + paper.getTitle() + "\" hits " + milestone + " citations!",
                    name + "'s paper \"" + paper.getTitle() + "\" has reached " + milestone + " citations.",
                    true));
                System.out.println("[Milestone] " + paper.getTitle() + " reached " + milestone + " citations!");
            }
        }
    }

    @Override
    public double calculateHIndex() {
        List<Integer> cits = new ArrayList<>();
        for (ResearchPaper p : getResearchPapersList()) cits.add(p.getCitations());
        if (cits.isEmpty()) return 0;
        cits.sort(Collections.reverseOrder());
        int h = 0;
        for (int i = 0; i < cits.size(); i++) { if (cits.get(i) >= i + 1) h = i + 1; else break; }
        return h;
    }

    @Override
    public void printPapers(Comparator<ResearchPaper> comparator) {
        List<ResearchPaper> all = new ArrayList<>(getResearchPapersList());
        all.sort(comparator);
        System.out.println("=== Research Papers: " + wrappedUser.getFullName() + " ===");
        for (ResearchPaper p : all) System.out.println("  " + p.getCitation("Plain Text"));
    }

    @Override public List<ResearchPaper> getResearchPapersList() { return extraPapers; }

    public User getWrappedUser() { return wrappedUser; }

    @Override public String toString() {
        return "Researcher[" + wrappedUser + "]";
    }
}
