package project.services;

import java.util.List;

import project.models.actors.Student;
import project.models.actors.User;
import project.models.enums.RequestStatus;
import project.models.others.*;
import project.patterns.ResearcherDecorator;
import project.storage.Database;

public class OrganizationService {

    public void createOrganization(Student student, String name) {
        Database db = Database.getInstance();
        Organization existing = db.getAllOrganizations().stream()
                .filter(o -> student.getId().equals(o.getLeadId())).findFirst().orElse(null);
        if (existing != null) {
            System.out.println("[Error] You already lead organization '" + existing.getName() + "'.");
            return;
        }
        if (name == null || name.isEmpty()) { System.out.println("[Error] Name cannot be empty."); return; }
        boolean nameTaken = db.getAllOrganizations().stream().anyMatch(o -> o.getName().equalsIgnoreCase(name));
        if (nameTaken) { System.out.println("[Error] An organization with that name already exists."); return; }
        Organization org = new Organization(name, student.getId());
        db.addOrganization(org);
        student.setOrganization(name);
        student.setOrganizationHead(true);
        db.saveUser(student);
        db.saveToDisk();
        System.out.println("[Created] Organization '" + name + "' (ID: " + org.getId() + "). You are the lead.");
    }

    public void listOrganizations() {
        Database db = Database.getInstance();
        List<Organization> orgs = db.getAllOrganizations();
        if (orgs.isEmpty()) { System.out.println("[Info] No organizations yet."); return; }
        System.out.println("=== Organizations ===");
        for (Organization o : orgs) {
            User lead = db.getUserById(o.getLeadId());
            String leadName = lead != null ? lead.getFullName() : o.getLeadId();
            System.out.printf("  [%s] %-30s  Lead: %-20s  Members: %d%n",
                    o.getId(), o.getName(), leadName, o.getMemberIds().size());
        }
    }

    public void joinOrganization(Student student, String orgId) {
        Database db = Database.getInstance();
        Organization org = db.getOrganizationById(orgId);
        if (org == null) { System.out.println("[Error] Organization not found: " + orgId); return; }
        if (org.isMember(student.getId())) {
            System.out.println("[Info] You are already a member of '" + org.getName() + "'."); return;
        }
        OrgJoinRequest req = new OrgJoinRequest(student.getId(), student.getFullName(), org.getId(), org.getName());
        db.addOrgJoinRequest(req);
        db.saveToDisk();
        System.out.println("[Sent] Join request submitted to '" + org.getName() + "' (request ID: " + req.getRequestId() + ").");
    }

    public void viewOrgMembers(String userId) {
        Database db = Database.getInstance();
        Organization org = db.getAllOrganizations().stream()
                .filter(o -> o.isMember(userId)).findFirst().orElse(null);
        if (org == null) { System.out.println("[Info] You are not a member of any organization."); return; }
        System.out.println("=== Members of '" + org.getName() + "' ===");
        for (String mid : org.getMemberIds()) {
            User u = db.getUserById(mid);
            String roleTag = org.getLeadId().equals(mid) ? " [Lead]" : "";
            System.out.println("  " + (u != null ? u.getFullName() + " (" + mid + ")" : mid) + roleTag);
        }
    }

    public void listOrgJoinRequests(String leadId) {
        Database db = Database.getInstance();
        List<OrgJoinRequest> reqs = db.getOrgJoinRequestsForLead(leadId);
        if (reqs.isEmpty()) { System.out.println("[Info] No pending organization join requests."); return; }
        System.out.println("=== Pending Org Join Requests ===");
        reqs.forEach(r -> System.out.printf("  [%s] %s → %s%n",
                r.getRequestId(), r.getTitle(), r.getDescription()));
    }

    public void handleOrgJoinRequest(Student lead, String reqId, boolean accept) {
        Database db = Database.getInstance();
        Request r = db.getRequestById(reqId);
        if (!(r instanceof OrgJoinRequest)) { System.out.println("[Error] Request not found: " + reqId); return; }
        OrgJoinRequest req = (OrgJoinRequest) r;
        Organization org = db.getOrganizationById(req.getOrgId());
        if (org == null || !lead.getId().equals(org.getLeadId())) {
            System.out.println("[Denied] You are not the lead of this organization."); return;
        }
        if (accept) {
            org.addMember(req.getRequesterId());
            User applicantUser = db.getUserById(req.getRequesterId());
            User base = (applicantUser instanceof ResearcherDecorator)
                    ? ((ResearcherDecorator) applicantUser).getWrappedUser() : applicantUser;
            if (base instanceof Student) {
                ((Student) base).setOrganization(org.getName());
                db.saveUser(base);
            }
            req.setStatus(RequestStatus.ACCEPTED);
            db.addNotification(new Notification(req.getRequesterId(),
                    "You have been accepted into organization '" + org.getName() + "'."));
            System.out.println("[Accepted] " + req.getRequesterId() + " joined '" + org.getName() + "'.");
        } else {
            req.setStatus(RequestStatus.REJECTED);
            db.addNotification(new Notification(req.getRequesterId(),
                    "Your request to join '" + org.getName() + "' was rejected."));
            System.out.println("[Rejected] Request " + reqId + " rejected.");
        }
        db.saveToDisk();
    }
}
