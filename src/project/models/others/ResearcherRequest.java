package project.models.others;

import project.models.enums.UrgencyLevel;

/**
 * A user's request to be granted researcher status.
 * A manager must approve it before the user gains access to research features.
 */
public class ResearcherRequest extends Request {
    private static final long serialVersionUID = 1L;

    public ResearcherRequest(String requesterId) {
        super("Researcher Role Request",
              "User " + requesterId + " is requesting researcher status.",
              UrgencyLevel.MEDIUM,
              requesterId);
    }
}
