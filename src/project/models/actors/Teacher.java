package project.models.actors;

import java.util.*;
import project.enums.Role;
import project.enums.TeacherTitle;
import project.enums.UrgencyLevel;
import project.models.others.*;
import project.storage.Database;

/**
 * University teacher. Professors ARE always Researchers (enforced in constructor).
 * Tutors/Lectors can optionally be researchers.
 * Implements Researcher interface for h-index and paper management.
 */
public class Teacher extends Employee implements Researcher {
    private static final long serialVersionUID = 1L;
    private TeacherTitle title;
    private List<ResearchPaper> researchPapers = new ArrayList<>();
    private boolean isResearcher;

    public Teacher() {}
    public Teacher(String id, String password, String firstName, String lastName, String email, double salary, TeacherTitle title) {
        super(id, password, firstName, lastName, email, salary, Role.TEACHER);
        this.title = title;
        // Professors are always researchers per requirements
        this.isResearcher = (title == TeacherTitle.PROFESSOR);
    }

    // Researcher implementation

    /**
     * H-index: largest h such that h papers each have ≥ h citations.
     */
    @Override
    public double calculateHIndex() {
        if (researchPapers == null || researchPapers.isEmpty()) return 0;
        List<Integer> citations = new ArrayList<>();
        for (ResearchPaper p : researchPapers) citations.add(p.getCitations());
        citations.sort(Collections.reverseOrder());
        int h = 0;
        for (int i = 0; i < citations.size(); i++) {
            if (citations.get(i) >= i + 1) h = i + 1; else break;
        }
        return h;
    }

    @Override
    public void printPapers(Comparator<ResearchPaper> comparator) {
        List<ResearchPaper> sorted = new ArrayList<>(researchPapers);
        sorted.sort(comparator);
        System.out.println("=== Research Papers: " + getFullName() + " ===");
        for (ResearchPaper p : sorted) System.out.println("  " + p.getCitation("Plain Text"));
    }

    @Override public List<ResearchPaper> getResearchPapersList() { return researchPapers; }

    public void addResearchPaper(ResearchPaper paper) {
        researchPapers.add(paper);
        // Auto-generate announcement news (requirement: when researcher publishes a paper)
        News news = new News("New Research Paper by " + getFullName(), "Prof. " + getFullName() + " published: " + paper.getTitle(), true);
        Database.getInstance().addNews(news);
        log("Published paper: " + paper.getTitle());
    }

    // Course / Teaching actions

    /** Puts a mark for a student in a course. Updates student GPA if needed. */
    public void putMark(Course course, String studentId, double first, double second, double finalExam) {
        Mark mark = new Mark(studentId, course.getCourseCode(), first, second, finalExam);
        course.assignMark(studentId, mark);
        log("Assigned mark to student " + studentId + " in " + course.getCourseCode());
        System.out.println("[Mark] " + mark);
    }

    /** Sends a formal complaint about a student to the dean. */
    public void sendComplaint(String studentId, UrgencyLevel urgency, String content) {
        Complaint complaint = new Complaint(this.getId(), studentId, urgency, content);
        Database.getInstance().addComplaint(complaint);
        log("Sent complaint about student " + studentId + " [" + urgency + "]");
        System.out.println("[Complaint filed] " + complaint);
    }

    /** Sends a message to another employee. */
    public void sendMessage(Employee receiver, String content) {
        Message msg = new Message(this.getId(), receiver.getId(), content);
        Database.getInstance().addMessage(msg);
        log("Sent message to " + receiver.getId());
        System.out.println("[Message sent] " + msg);
    }

    // Getters / Setters
    public TeacherTitle getTitle() { return title; }
    public void setTitle(TeacherTitle v) {
        this.title = v;
        if (v == TeacherTitle.PROFESSOR) this.isResearcher = true;
    }
    public boolean isResearcher() { return isResearcher; }
    public void setResearcher(boolean v) { this.isResearcher = v; }
    public List<ResearchPaper> getResearchPapers() { return researchPapers; }
    public void setResearchPapers(List<ResearchPaper> v) { this.researchPapers = v; }

    @Override public String toString() {
        return "Teacher[id=" + getId() + ", name=" + getFullName() + ", title=" + title + ", hIndex=" + calculateHIndex() + "]";
    }
}
