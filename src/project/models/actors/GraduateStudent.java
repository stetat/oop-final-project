package project.models.actors;

import java.util.*;

import project.models.errors.InvalidSupervisorException;
import project.models.others.*;
import project.patterns.ResearcherDecorator;
import project.storage.Database;

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
