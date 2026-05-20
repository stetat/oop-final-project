package project.services;

import java.util.List;
import java.util.stream.Collectors;

import project.models.actors.Teacher;
import project.models.actors.User;
import project.models.enums.UrgencyLevel;
import project.models.others.Complaint;
import project.storage.Database;

/** Manages formal complaints filed by teachers about students. */
public class ComplaintService {

    /**
     * Files a complaint about a student on behalf of a teacher.
     * Only teachers may call this; other callers receive an error and {@code null} is returned.
     *
     * @param sender    the teacher filing the complaint
     * @param studentId the student being reported
     * @param urgency   how urgent the issue is
     * @param content   description of the complaint
     * @return the created complaint, or {@code null} if the sender is not a teacher
     */
    public Complaint fileComplaint(User sender, String studentId, UrgencyLevel urgency, String content) {
        if (!(sender instanceof Teacher)) {
            System.out.println("[ComplaintService] Only teachers are allowed to file complaints.");
            return null;
        }
        Complaint complaint = new Complaint(sender.getId(), studentId, urgency, content);
        Database.getInstance().addComplaint(complaint);
        sender.log("Filed complaint about student " + studentId + " [" + urgency + "]");
        System.out.println("[Complaint filed] " + complaint);
        Database.getInstance().saveToDisk();
        return complaint;
    }

    /** Returns all complaints stored in the database. */
    public List<Complaint> getAllComplaints() {
        return Database.getInstance().getAllComplaints();
    }

    /**
     * Returns all complaints that target the given student.
     *
     * @param studentId the student's ID to filter by
     */
    public List<Complaint> getComplaintsAbout(String studentId) {
        return Database.getInstance().getAllComplaints().stream()
                .filter(c -> studentId.equals(c.getStudentId()))
                .collect(Collectors.toList());
    }
}
