package project.models.others;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import project.enums.RequestStatus;
import project.enums.UrgencyLevel;

/** Tech-support request submitted by any employee. Status lifecycle: VIEWED → ACCEPTED/REJECTED → DONE. */
public class Request implements Serializable {
    private static final long serialVersionUID = 1L;
    private String requestId;
    private String title;
    private String description;
    private RequestStatus status;
    private UrgencyLevel urgencyLevel;
    private Date createdAt;
    private String requesterId;

    public Request() { createdAt = new Date(); status = RequestStatus.VIEWED; }
    public Request(String title, String description, UrgencyLevel urgency, String requesterId) {
        this(); this.title = title; this.description = description;
        this.urgencyLevel = urgency; this.requesterId = requesterId;
        this.requestId = "REQ-" + System.currentTimeMillis();
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String v) { this.requestId = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus v) { this.status = v; }
    public UrgencyLevel getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(UrgencyLevel v) { this.urgencyLevel = v; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date v) { this.createdAt = v; }
    public String getRequesterId() { return requesterId; }
    public void setRequesterId(String v) { this.requesterId = v; }

    @Override public int hashCode() { return Objects.hash(requestId); }
    @Override public boolean equals(Object o) {
        if (this == o) return true; if (!(o instanceof Request)) return false;
        return Objects.equals(requestId, ((Request) o).requestId);
    }
    @Override public String toString() {
        return "Request[" + requestId + " | " + title + " | " + urgencyLevel + " | " + status + "]";
    }
}
