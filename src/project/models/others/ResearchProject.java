package project.models.others;

import java.io.Serializable;
import java.util.Objects;
import java.util.Vector;

import project.models.errors.NotResearcherException;
import project.patterns.ResearcherDecorator;

/**
 * A collaborative research project. Only users with researcher status
 * ({@link project.patterns.ResearcherDecorator}) can participate or publish papers under it.
 */
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

    /**
     * Adds a researcher to the participant list. Duplicates are silently ignored.
     *
     * @param researcher the researcher to add
     */
    public void addParticipant(ResearcherDecorator researcher) {
        if (!participants.contains(researcher)) participants.add(researcher);
    }

    /**
     * Type-safe entry point for adding a participant. Throws if the object is not
     * a {@link project.patterns.ResearcherDecorator}.
     *
     * @param user the user to add
     * @throws NotResearcherException if the user has no researcher status
     */
    public void addUser(Object user) throws NotResearcherException {
        if (!(user instanceof ResearcherDecorator))
            throw new NotResearcherException("User [" + user + "] is not a Researcher and cannot join a ResearchProject.");
        addParticipant((ResearcherDecorator) user);
    }

    /** Attaches a paper to this project. Null values are ignored. */
    public void addPaper(ResearchPaper paper) { if (paper != null) publishedPapers.add(paper); }

    /** Removes a paper from this project's published list. */
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
