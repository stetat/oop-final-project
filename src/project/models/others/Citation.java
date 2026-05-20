package project.models.others;

import java.io.Serializable;
import java.util.Date;

/**
 * Records who cited a research paper and what they wrote about it.
 * Attached to {@link ResearchPaper#getPaperCitations()} for qualitative citation tracking.
 */
public class Citation implements Serializable {
    private static final long serialVersionUID = 1L;
    private String citerId;
    private String citerName;
    private String text;
    private Date createdAt = new Date();

    public Citation() {}
    public Citation(String citerId, String citerName, String text) {
        this.citerId = citerId; this.citerName = citerName; this.text = text;
    }

    public String getCiterId()   { return citerId; }
    public String getCiterName() { return citerName; }
    public String getText()      { return text; }
    public Date   getCreatedAt() { return createdAt; }

    @Override public String toString() {
        return "  [" + citerName + "] " + text;
    }
}
