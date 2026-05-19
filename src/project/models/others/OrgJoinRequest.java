package project.models.others;

import project.models.enums.UrgencyLevel;

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
