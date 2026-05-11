package project.models.others;

import java.io.Serializable;
import java.util.Objects;
import java.util.Vector;

import project.errors.NotResearcherException;

/**
 * Represents a university research project with a topic, published papers, and participant researchers.
 * Enforces that only Researcher instances can join — throws NotResearcherException otherwise.
 */
public class ResearchProject implements Serializable {
    private static final long serialVersionUID = 1L;
    private String topic;
    private Vector<ResearchPaper> publishedPapers;
    private Vector<Researcher> participants;  // fixed: should be Researcher, not ResearchPaper

    public ResearchProject() {
        publishedPapers = new Vector<>();
        participants = new Vector<>();
    }
    public ResearchProject(String topic) {
        this();
        this.topic = topic;
    }

    public void addParticipant(Researcher researcher) {
        if (!participants.contains(researcher)) participants.add(researcher);
    }

    public void addUser(Object user) throws NotResearcherException {
        if (!(user instanceof Researcher))
            throw new NotResearcherException("User [" + user + "] is not a Researcher and cannot join a ResearchProject.");
        addParticipant((Researcher) user);
    }

    public void addPaper(ResearchPaper paper) { if (paper != null) publishedPapers.add(paper); }
    public void removePaper(ResearchPaper paper) { publishedPapers.remove(paper); }

    public String getTopic() { return topic; }
    public void setTopic(String t) { this.topic = t; }
    public Vector<ResearchPaper> getPublishedPapers() { return publishedPapers; }
    public void setPublishedPapers(Vector<ResearchPaper> p) { this.publishedPapers = p; }
    public Vector<Researcher> getParticipants() { return participants; }
    public void setParticipants(Vector<Researcher> p) { this.participants = p; }

    @Override public int hashCode() { return Objects.hash(topic, publishedPapers); }
    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ResearchProject)) return false;
        ResearchProject o = (ResearchProject) obj;
        return Objects.equals(topic, o.topic) && Objects.equals(publishedPapers, o.publishedPapers);
    }
    @Override public String toString() {
        return "ResearchProject[topic=" + topic + ", papers=" + publishedPapers.size() + ", participants=" + participants.size() + "]";
    }
}
