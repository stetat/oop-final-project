package project.models.others;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/** A direct message sent from one user to another within the university system. */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private String messageId;
    private String content;
    private boolean isRead;
    private Date sentAt;
    private String senderId;    
    private String receiverId;  

    public Message() { sentAt = new Date(); }
    public Message(String senderId, String receiverId, String content) {
        this(); this.senderId = senderId; this.receiverId = receiverId; this.content = content;
        this.messageId = "MSG-" + System.currentTimeMillis();
    }
    /** Marks this message as read. */
    public void markAsRead() { this.isRead = true; }

    public String getMessageId() { return messageId; }
    public void setMessageId(String v) { this.messageId = v; }
    public String getContent() { return content; }
    public void setContent(String v) { this.content = v; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean v) { this.isRead = v; }
    public Date getSentAt() { return sentAt; }
    public void setSentAt(Date v) { this.sentAt = v; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String v) { this.senderId = v; }
    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String v) { this.receiverId = v; }

    @Override public int hashCode() { return Objects.hash(messageId, senderId, receiverId, content); }
    @Override public boolean equals(Object o) {
        if (this == o) return true; if (!(o instanceof Message)) return false;
        Message m = (Message) o; return Objects.equals(messageId, m.messageId);
    }
    @Override public String toString() {
        return "Message[" + messageId + " from=" + senderId + " to=" + receiverId + " read=" + isRead + "]";
    }
}
