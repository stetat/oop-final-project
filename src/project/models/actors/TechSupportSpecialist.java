package project.models.actors;

import java.util.List;
import java.util.stream.Collectors;
import project.enums.RequestStatus;
import project.enums.Role;
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

    /** Returns all pending (VIEWED) requests. Marks them as VIEWED on access. */
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
        if (r != null) {
            r.setStatus(RequestStatus.VIEWED);
            System.out.println("[Request Detail] " + r);
        } else System.out.println("[TechSupport] Request not found: " + requestId);
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
