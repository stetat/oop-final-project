package project.services;

import java.util.List;
import java.util.stream.Collectors;

import project.models.actors.Teacher;
import project.models.actors.User;
import project.models.enums.UrgencyLevel;
import project.models.others.Complaint;
import project.storage.Database;

public class ComplaintService {

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

    public List<Complaint> getAllComplaints() {
        return Database.getInstance().getAllComplaints();
    }

    public List<Complaint> getComplaintsAbout(String studentId) {
        return Database.getInstance().getAllComplaints().stream()
                .filter(c -> studentId.equals(c.getStudentId()))
                .collect(Collectors.toList());
    }
}
