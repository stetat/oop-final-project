package project.patterns;

import java.io.Serializable;
import java.util.*;
import project.models.others.ResearchPaper;


public class ResearchJournal implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private List<ResearchPaper> papers = new ArrayList<>();
    private transient List<JournalObserver> subscribers = new ArrayList<>(); // transient: not serialized

    public ResearchJournal(String name) { this.name = name; }

    public void subscribe(JournalObserver observer) {
        if (!subscribers.contains(observer)) subscribers.add(observer);
    }
    public void unsubscribe(JournalObserver observer) { subscribers.remove(observer); }

    public void publishPaper(ResearchPaper paper) {
        papers.add(paper);
        System.out.println("[Journal: " + name + "] New paper published: " + paper.getTitle());
        for (JournalObserver obs : subscribers)
            obs.onNewPaperPublished(name, paper);
    }

    public String getName() { return name; }
    public List<ResearchPaper> getPapers() { return papers; }

    private Object readResolve() {
        if (subscribers == null) subscribers = new ArrayList<>();
        return this;
    }
}
