package project.models.actors;

import java.util.*;
import project.errors.InvalidSupervisorException;
import project.models.others.*;

/**
 * Graduate student (Master or PhD). ALWAYS a Researcher.
 * Has diploma projects and a research supervisor whose h-index must be >= 3.
 */
public class GraduateStudent extends Student {
    private static final long serialVersionUID = 1L;
    private static final double MIN_SUPERVISOR_H_INDEX = 3.0;

    private List<ResearchPaper> diplomaProjects = new ArrayList<>();
    private Researcher researchSupervisor; // Teacher or other Researcher
    private String supervisorId;
    private boolean isPhD;

    public GraduateStudent() { super(); setResearcher(true); }
    public GraduateStudent(String id, String password, String firstName, String lastName, String email) {
        super(id, password, firstName, lastName, email);
        setResearcher(true); // Graduate students ARE always researchers
    }
    public GraduateStudent(String id, String password, String firstName, String lastName, String email, boolean isPhD) {
        this(id, password, firstName, lastName, email);
        this.isPhD = isPhD;
    }

    /**
     * Sets research supervisor. Throws InvalidSupervisorException if supervisor's h-index < 3.
     */
    public void setResearchSupervisor(Researcher supervisor) throws InvalidSupervisorException {
        double hIdx = supervisor.calculateHIndex();
        if (hIdx < MIN_SUPERVISOR_H_INDEX) {
            throw new InvalidSupervisorException(
                "Supervisor h-index (" + hIdx + ") is below minimum required (" + MIN_SUPERVISOR_H_INDEX + ")." + " Cannot assign as supervisor for " + getFullName());
        }
        this.researchSupervisor = supervisor;
        if (supervisor instanceof User) this.supervisorId = ((User) supervisor).getId();
        log("Research supervisor assigned (h-index=" + hIdx + ")");
        System.out.println("[Supervisor Assigned] " + getFullName() + " → supervisor h-index=" + hIdx);
    }

    public void addDiplomaProject(ResearchPaper paper) {
        diplomaProjects.add(paper);
        addResearchPaper(paper);
    }

    public List<ResearchPaper> getDiplomaProjects() { return diplomaProjects; }
    public void setDiplomaProjects(List<ResearchPaper> v) { this.diplomaProjects = v; }
    public Researcher getResearchSupervisor() { return researchSupervisor; }
    public String getSupervisorId() { return supervisorId; }
    public boolean isPhD() { return isPhD; }
    public void setPhD(boolean v) { this.isPhD = v; }

    @Override public String toString() {
        return "GraduateStudent[id=" + getId() + ", name=" + getFullName() + ", type=" + (isPhD ? "PhD" : "Master") + ", hIndex=" + calculateHIndex() + "]";
    }
}
