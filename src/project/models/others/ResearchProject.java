package project.models.others;

import java.io.Serializable;
import java.util.Objects;
import java.util.Vector;

import project.models.errors.NotResearcherException;
import project.patterns.ResearcherDecorator;

public class ResearchProject implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String ownerId;
    private String topic;
    private Vector<ResearchPaper> publishedPapers;
    private Vector<ResearcherDecorator> participants;

    public ResearchProject() {
        publishedPapers = new Vector<>();
        participants = new Vector<>();
    }
    public ResearchProject(String topic) {
        this();
        this.topic = topic;
    }

    public void addParticipant(ResearcherDecorator researcher) {
        if (!participants.contains(researcher)) participants.add(researcher);
    }

    public void addUser(Object user) throws NotResearcherException {
        if (!(user instanceof ResearcherDecorator))
            throw new NotResearcherException("User [" + user + "] is not a Researcher and cannot join a ResearchProject.");
        addParticipant((ResearcherDecorator) user);
    }

    public void addPaper(ResearchPaper paper) { if (paper != null) publishedPapers.add(paper); }
    public void removePaper(ResearchPaper paper) { publishedPapers.remove(paper); }

    public String getId()    { return id; }
    public void setId(String id) { this.id = id; }
    public String getOwnerId()       { return ownerId; }
    public void setOwnerId(String o) { this.ownerId = o; }
    public String getTopic() { return topic; }
    public void setTopic(String t) { this.topic = t; }
    public Vector<ResearchPaper> getPublishedPapers() { return publishedPapers; }
    public void setPublishedPapers(Vector<ResearchPaper> p) { this.publishedPapers = p; }
    public Vector<ResearcherDecorator> getParticipants() { return participants; }
    public void setParticipants(Vector<ResearcherDecorator> p) { this.participants = p; }

    @Override public int hashCode() { return Objects.hash(topic, publishedPapers); }
    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ResearchProject)) return false;
        ResearchProject o = (ResearchProject) obj;
        return Objects.equals(topic, o.topic) && Objects.equals(publishedPapers, o.publishedPapers);
    }
    @Override public String toString() {
        return "[" + id + "] " + topic + "  (papers=" + publishedPapers.size() + ", participants=" + participants.size() + ")";
    }
}
