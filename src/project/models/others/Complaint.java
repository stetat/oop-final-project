package project.models.others;

import java.io.Serializable;
import java.util.Objects;
import project.enums.UrgencyLevel;

/** A formal teacher complaint about a student, sent to dean. */
public class Complaint implements Serializable {
    private static final long serialVersionUID = 1L;
    private String teacherId;
    private String studentId;
    private UrgencyLevel urgency;
    private String content;
    private String complaintId;

    public Complaint() {}
    public Complaint(String teacherId, String studentId, UrgencyLevel urgency, String content) {
        this.teacherId = teacherId; this.studentId = studentId;
        this.urgency = urgency; this.content = content;
        this.complaintId = "COMP-" + System.currentTimeMillis();
    }

    public String getComplaintId() { return complaintId; }
    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String v) { this.teacherId = v; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String v) { this.studentId = v; }
    public UrgencyLevel getUrgency() { return urgency; }
    public void setUrgency(UrgencyLevel v) { this.urgency = v; }
    public String getContent() { return content; }
    public void setContent(String v) { this.content = v; }

    @Override public int hashCode() { return Objects.hash(complaintId, teacherId, studentId); }
    @Override public boolean equals(Object o) {
        if (this == o) return true; if (!(o instanceof Complaint)) return false;
        return Objects.equals(complaintId, ((Complaint) o).complaintId);
    }
    @Override public String toString() {
        return "Complaint[" + urgency + " | teacher=" + teacherId + " | student=" + studentId + " | " + content + "]";
    }
}
