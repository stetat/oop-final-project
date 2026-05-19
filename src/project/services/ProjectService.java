package project.services;

import java.util.List;

import project.models.actors.User;
import project.models.enums.RequestStatus;
import project.models.others.*;
import project.patterns.ResearcherDecorator;
import project.storage.Database;

public class ProjectService {

    public void listProjects() {
        Database db = Database.getInstance();
        List<ResearchProject> projects = db.getAllProjects();
        if (projects.isEmpty()) { System.out.println("[Projects] No research projects yet."); return; }
        System.out.println("=== RESEARCH PROJECTS ===");
        for (ResearchProject p : projects) {
            System.out.println("  " + p);
            if (!p.getParticipants().isEmpty()) {
                StringBuilder names = new StringBuilder();
                for (ResearcherDecorator r : p.getParticipants()) {
                    if (names.length() > 0) names.append(", ");
                    names.append(r.getWrappedUser().getFullName());
                }
                System.out.println("    Participants: " + names);
            }
        }
    }

    public void createProject(User user, String topic) {
        Database db = Database.getInstance();
        ResearcherDecorator rd = getResearcherOf(user);
        if (rd == null) { System.out.println("[Project] You must be a researcher to create a project."); return; }
        if (topic == null || topic.isBlank()) { System.out.println("[Project] Topic cannot be empty."); return; }
        ResearchProject proj = new ResearchProject(topic);
        proj.setOwnerId(user.getId());
        proj.addParticipant(rd);
        db.addProject(proj);
        db.saveToDisk();
        System.out.println("[Project] Created: " + proj);
    }

    public void joinProject(User user, String projectId) {
        Database db = Database.getInstance();
        ResearcherDecorator rd = getResearcherOf(user);
        if (rd == null) { System.out.println("[Project] You must be a researcher to join a project."); return; }
        ResearchProject proj = db.getProjectById(projectId);
        if (proj == null) { System.out.println("[Project] Project not found: " + projectId); return; }
        if (proj.getParticipants().stream().anyMatch(p -> p.getWrappedUser().getId().equals(user.getId()))) {
            System.out.println("[Project] You are already a member of this project."); return;
        }
        boolean alreadyPending = db.getAllRequests().stream()
                .anyMatch(r -> r instanceof ProjectJoinRequest
                        && r.getRequesterId().equals(user.getId())
                        && ((ProjectJoinRequest) r).getProjectId().equals(projectId)
                        && r.getStatus() == RequestStatus.VIEWED);
        if (alreadyPending) {
            System.out.println("[Project] A pending join request for this project already exists."); return;
        }
        ProjectJoinRequest req = new ProjectJoinRequest(user.getId(), user.getFullName(), projectId, proj.getTopic());
        db.addProjectJoinRequest(req);
        db.saveToDisk();
        System.out.println("[Project] Join request #" + req.getRequestId() + " sent for '" + proj.getTopic() + "'. Awaiting owner approval.");
    }

    public void listProjectJoinRequests(String ownerId) {
        Database db = Database.getInstance();
        List<ProjectJoinRequest> reqs = db.getProjectJoinRequestsForOwner(ownerId);
        if (reqs.isEmpty()) { System.out.println("[Project] No pending join requests for your projects."); return; }
        System.out.println("=== Pending Project Join Requests ===");
        for (ProjectJoinRequest r : reqs) {
            ResearchProject proj = db.getProjectById(r.getProjectId());
            String topic = proj != null ? proj.getTopic() : r.getProjectId();
            System.out.printf("  #%-6s  %-20s  →  %s%n", r.getRequestId(), r.getDescription().split("'")[0].trim(), topic);
        }
    }

    public void handleProjectJoinRequest(String ownerId, String reqId, boolean accept) {
        Database db = Database.getInstance();
        Request r = db.getRequestById(reqId);
        if (!(r instanceof ProjectJoinRequest)) {
            System.out.println("[Project] Join request not found: " + reqId); return;
        }
        if (r.getStatus() != RequestStatus.VIEWED) {
            System.out.println("[Project] Already processed: " + r.getStatus()); return;
        }
        ProjectJoinRequest pjr = (ProjectJoinRequest) r;
        ResearchProject proj = db.getProjectById(pjr.getProjectId());
        if (proj == null) { System.out.println("[Project] Project no longer exists."); return; }
        if (!ownerId.equals(proj.getOwnerId())) {
            System.out.println("[Project] You are not the owner of this project."); return;
        }
        if (accept) {
            User requester = db.getUserById(pjr.getRequesterId());
            ResearcherDecorator rd = getResearcherOf(requester);
            if (rd == null) { System.out.println("[Project] Requester is no longer a researcher."); return; }
            proj.addParticipant(rd);
            r.setStatus(RequestStatus.ACCEPTED);
            db.saveToDisk();
            System.out.println("[Project] Accepted: " + requester.getFullName() + " joined '" + proj.getTopic() + "'.");
        } else {
            r.setStatus(RequestStatus.REJECTED);
            db.saveToDisk();
            System.out.println("[Project] Rejected join request #" + reqId + ".");
        }
    }

    private ResearcherDecorator getResearcherOf(User u) {
        if (u == null) return null;
        if (u instanceof ResearcherDecorator) return (ResearcherDecorator) u;
        User fromDb = Database.getInstance().getUserById(u.getId());
        return (fromDb instanceof ResearcherDecorator) ? (ResearcherDecorator) fromDb : null;
    }
}
