package project.models.actors;

import java.util.*;

import project.models.errors.InvalidSupervisorException;
import project.models.others.*;
import project.patterns.ResearcherDecorator;
import project.storage.Database;

/**
 * A master's or PhD student. Extends Student with a research supervisor and diploma projects.
 * When created, the system automatically wraps this object in a {@link project.patterns.ResearcherDecorator}.
 */
public class GraduateStudent extends Student {
    private static final long serialVersionUID = 1L;
    private static final double MIN_SUPERVISOR_H_INDEX = 3.0;

    private List<ResearchPaper> diplomaProjects = new ArrayList<>();
    private ResearcherDecorator researchSupervisor;
    private String supervisorId;
    private boolean isPhD;

    public GraduateStudent() { super(); }
    public GraduateStudent(String id, String password, String firstName, String lastName, String email) {
        super(id, password, firstName, lastName, email);
    }
    public GraduateStudent(String id, String password, String firstName, String lastName, String email, boolean isPhD) {
        this(id, password, firstName, lastName, email);
        this.isPhD = isPhD;
    }

    /**
     * Assigns a research supervisor. The supervisor must be a researcher with an h-index ≥ 3.
     *
     * @param supervisor the proposed supervisor (must be non-null and qualified)
     * @throws InvalidSupervisorException if the supervisor is null or has insufficient h-index
     */
    public void setResearchSupervisor(ResearcherDecorator supervisor) throws InvalidSupervisorException {
        if (supervisor == null) throw new InvalidSupervisorException("Supervisor has no research title.");
        double hIdx = supervisor.calculateHIndex();
        if (hIdx < MIN_SUPERVISOR_H_INDEX) {
            throw new InvalidSupervisorException(
                "Supervisor h-index (" + hIdx + ") is below minimum required (" + MIN_SUPERVISOR_H_INDEX + ")." +
                " Cannot assign as supervisor for " + getFullName());
        }
        this.researchSupervisor = supervisor;
        this.supervisorId = supervisor.getWrappedUser().getId();
        log("Research supervisor assigned (h-index=" + hIdx + ")");
        System.out.println("[Supervisor Assigned] " + getFullName() + " → supervisor h-index=" + hIdx);
    }

    /**
     * Adds a diploma/thesis paper. Also propagates the paper to this student's
     * {@link project.patterns.ResearcherDecorator} in the database so it shows up in research listings.
     *
     * @param paper the paper to attach to this student's profile
     */
    public void addDiplomaProject(ResearchPaper paper) {
        diplomaProjects.add(paper);
        // Also add to the ResearcherDecorator wrapping this student, if one exists in DB
        User inDb = Database.getInstance().getUserById(getId());
        if (inDb instanceof ResearcherDecorator) {
            ((ResearcherDecorator) inDb).addResearchPaper(paper);
        }
    }

    public List<ResearchPaper> getDiplomaProjects() { return diplomaProjects; }
    public void setDiplomaProjects(List<ResearchPaper> v) { this.diplomaProjects = v; }
    public ResearcherDecorator getResearchSupervisor() { return researchSupervisor; }
    public String getSupervisorId() { return supervisorId; }
    public boolean isPhD() { return isPhD; }
    public void setPhD(boolean v) { this.isPhD = v; }

    @Override public String toString() {
        return "GraduateStudent[id=" + getId() + ", name=" + getFullName() + ", type=" + (isPhD ? "PhD" : "Master") + "]";
    }
}
