package project.models.actors;

import java.util.List;
import java.util.stream.Collectors;

import project.models.enums.RequestStatus;
import project.models.enums.Role;
import project.models.others.Request;
import project.storage.Database;

/**
 * Tech support specialist. Manages and resolves employee requests.
 * Request lifecycle: VIEWED (on open) → ACCEPTED / REJECTED → DONE.
 */
public class TechSupportSpecialist extends Employee {
    private static final long serialVersionUID = 1L;

    public TechSupportSpecialist() {}
    public TechSupportSpecialist(String id, String password, String firstName, String lastName, String email, double salary) {
        super(id, password, firstName, lastName, email, salary, Role.TECH_SUPPORT);
    }

    public List<Request> getNewRequests() {
        List<Request> all = Database.getInstance().getAllRequests();
        List<Request> pending = all.stream().filter(r -> r.getStatus() == RequestStatus.VIEWED).collect(Collectors.toList());
        System.out.println("[TechSupport] " + pending.size() + " new request(s) found.");
        return pending;
    }

    public void acceptRequest(String requestId) {
        updateRequestStatus(requestId, RequestStatus.ACCEPTED);
    }
    public void rejectRequest(String requestId) {
        updateRequestStatus(requestId, RequestStatus.REJECTED);
    }
    public void markAsDone(String requestId) {
        updateRequestStatus(requestId, RequestStatus.DONE);
    }

    public void viewRequest(String requestId) {
        Request r = Database.getInstance().getRequestById(requestId);
        if (r == null) { System.out.println("[TechSupport] Request not found: " + requestId); return; }
        r.setStatus(RequestStatus.VIEWED);
        System.out.println("=== Request Detail ===");
        System.out.println("  ID:          " + r.getRequestId());
        System.out.println("  Title:       " + r.getTitle());
        System.out.println("  Message:     " + r.getDescription());
        System.out.println("  Urgency:     " + r.getUrgencyLevel());
        System.out.println("  Status:      " + r.getStatus());
        System.out.println("  Submitted:   " + r.getCreatedAt());
        System.out.println("  Requester:   " + r.getRequesterId());
    }

    private void updateRequestStatus(String requestId, RequestStatus newStatus) {
        Request r = Database.getInstance().getRequestById(requestId);
        if (r != null) {
            r.setStatus(newStatus);
            log("Request " + requestId + " → " + newStatus);
            System.out.println("[TechSupport] Request " + requestId + " set to " + newStatus);
        }
    }

    @Override public String toString() {
        return "TechSupportSpecialist[id=" + getId() + ", name=" + getFullName() + "]";
    }
}
