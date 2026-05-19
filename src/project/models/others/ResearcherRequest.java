package project.models.others;

import project.models.enums.UrgencyLevel;

public class ResearcherRequest extends Request {
    private static final long serialVersionUID = 1L;

    public ResearcherRequest(String requesterId) {
        super("Researcher Role Request",
              "User " + requesterId + " is requesting researcher status.",
              UrgencyLevel.MEDIUM,
              requesterId);
    }
}
