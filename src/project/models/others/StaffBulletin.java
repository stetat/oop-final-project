package project.models.others;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A staff bulletin board post. Any employee can post; all staff can read.
 * Displayed in reverse chronological order (newest first).
 */
public class StaffBulletin implements Serializable {
    private static final long serialVersionUID = 1L;
    private String authorId;
    private String authorName;
    private String title;
    private String body;
    private Date postedAt;

    public StaffBulletin() { postedAt = new Date(); }
    public StaffBulletin(String authorId, String authorName, String title, String body) {
        this();
        this.authorId = authorId;
        this.authorName = authorName;
        this.title = title;
        this.body = body;
    }

    public String getAuthorId()   { return authorId; }
    public String getAuthorName() { return authorName; }
    public String getTitle()      { return title; }
    public String getBody()       { return body; }
    public Date   getPostedAt()   { return postedAt; }

    @Override public String toString() {
        String date = postedAt != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm").format(postedAt) : "?";
        return "[" + date + "]  " + authorName + ":  " + title;
    }
}
