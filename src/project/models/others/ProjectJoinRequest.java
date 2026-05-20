package project.models.others;

import project.models.enums.UrgencyLevel;

/**
 * A researcher's request to join an existing research project.
 * The project owner approves or rejects it via {@link project.services.ProjectService}.
 */
public class ProjectJoinRequest extends Request {
    private static final long serialVersionUID = 1L;
    private String projectId;

    public ProjectJoinRequest(String requesterId, String requesterName, String projectId, String projectTopic) {
        super("Project Join: " + projectTopic,
              requesterName + " requests to join project '" + projectTopic + "'",
              UrgencyLevel.LOW, requesterId);
        this.projectId = projectId;
    }

    public String getProjectId() { return projectId; }
}
