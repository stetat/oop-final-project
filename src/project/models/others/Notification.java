package project.models.others;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;
    private String recipientId;
    private String message;
    private boolean read = false;
    private Date createdAt = new Date();

    public Notification() {}
    public Notification(String recipientId, String message) {
        this.recipientId = recipientId;
        this.message = message;
    }

    public String getRecipientId() { return recipientId; }
    public String getMessage()     { return message; }
    public boolean isRead()        { return read; }
    public void markRead()         { this.read = true; }
    public Date getCreatedAt()     { return createdAt; }

    @Override public String toString() {
        String date = createdAt != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm").format(createdAt) : "?";
        return (read ? "  " : "* ") + "[" + date + "] " + message;
    }
}
