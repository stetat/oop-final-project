package project.models.others;

import project.models.enums.UrgencyLevel;

/**
 * A student's request to join an existing student organization.
 * The organization lead approves or rejects it via {@link project.services.OrganizationService}.
 */
public class OrgJoinRequest extends Request {
    private static final long serialVersionUID = 1L;
    private String orgId;

    public OrgJoinRequest(String requesterId, String requesterName, String orgId, String orgName) {
        super("Org Join: " + orgName,
              requesterName + " requests to join organization '" + orgName + "'",
              UrgencyLevel.LOW, requesterId);
        this.orgId = orgId;
    }

    public String getOrgId() { return orgId; }
}
