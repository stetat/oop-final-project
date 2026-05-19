package project.services;

import java.util.ArrayList;
import java.util.List;

import project.models.actors.User;
import project.models.enums.RequestStatus;
import project.models.enums.UrgencyLevel;
import project.models.others.Request;
import project.models.others.ResearcherRequest;
import project.patterns.ResearcherDecorator;
import project.storage.Database;

public class RequestService {

    public void createRequest(String requesterId, String title, String desc, UrgencyLevel level) {
        Database db = Database.getInstance();
        Request req = new Request(title, desc, level, requesterId);
        db.addRequest(req);
        db.saveToDisk();
        System.out.println("[Request] Submitted: " + req.getRequestId());
    }

    public void requestResearcherRole(User user) {
        Database db = Database.getInstance();
        if (db.getUserById(user.getId()) instanceof ResearcherDecorator) {
            System.out.println("[Researcher] You are already a researcher."); return;
        }
        boolean alreadyPending = db.getAllRequests().stream()
                .anyMatch(r -> r instanceof ResearcherRequest
                        && r.getRequesterId().equals(user.getId())
                        && r.getStatus() == RequestStatus.VIEWED);
        if (alreadyPending) {
            System.out.println("[Researcher] You already have a pending request."); return;
        }
        ResearcherRequest req = new ResearcherRequest(user.getId());
        db.addResearcherRequest(req);
        db.saveToDisk();
        System.out.println("[Researcher] Request submitted: " + req.getRequestId() + ". Awaiting manager approval.");
    }

    public void listResearcherRequests() {
        Database db = Database.getInstance();
        List<Request> pending = new ArrayList<>();
        for (Request r : db.getAllRequests())
            if (r instanceof ResearcherRequest && r.getStatus() == RequestStatus.VIEWED) pending.add(r);
        if (pending.isEmpty()) { System.out.println("[Researcher] No pending researcher requests."); return; }
        System.out.println("=== Pending Researcher Requests ===");
        for (Request r : pending) {
            User u = db.getUserById(r.getRequesterId());
            String name = u != null ? u.getFullName() + " [" + u.getRole() + "]" : r.getRequesterId();
            System.out.println("  " + r.getRequestId() + "  |  " + name);
        }
    }

    public void handleResearcherRequest(String reqId, boolean approve) {
        Database db = Database.getInstance();
        Request r = db.getRequestById(reqId);
        if (r == null || !(r instanceof ResearcherRequest)) {
            System.out.println("[Researcher] Request not found: " + reqId); return;
        }
        if (r.getStatus() != RequestStatus.VIEWED) {
            System.out.println("[Researcher] Request already processed: " + r.getStatus()); return;
        }
        r.setStatus(approve ? RequestStatus.ACCEPTED : RequestStatus.REJECTED);
        if (approve) {
            User u = db.getUserById(r.getRequesterId());
            User base = (u instanceof ResearcherDecorator) ? ((ResearcherDecorator) u).getWrappedUser() : u;
            if (base != null) {
                db.promoteToResearcher(base);
                System.out.println("[Researcher] " + base.getFullName() + " is now a researcher.");
            }
        } else {
            System.out.println("[Researcher] Request " + reqId + " rejected.");
        }
        db.saveToDisk();
    }
}
