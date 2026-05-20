package project.services;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import project.models.actors.*;
import project.models.enums.CourseType;
import project.models.enums.RequestStatus;
import project.models.enums.School;
import project.models.others.*;
import project.patterns.ResearcherDecorator;
import project.storage.Database;

/** Handles course browsing, student registration/dropping, and manager approval workflows. */
public class CourseService {

    /**
     * Prints all courses. Cross-school courses are labelled "FREE ELECTIVE" for the given student.
     *
     * @param viewer the student viewing the list (used to determine cross-school courses); may be null
     */
    public void listAllCourses(Student viewer) {
        Database db = Database.getInstance();
        System.out.println("=== AVAILABLE COURSES ===");
        Collection<Course> courses = db.getAllCourses();
        if (courses.isEmpty()) { System.out.println("  (no courses yet)"); return; }
        for (Course c : courses) {
            boolean crossSchool = viewer != null
                    && viewer.getSchool() != null
                    && c.getSchool() != null
                    && c.getSchool() != viewer.getSchool();
            String typeLabel = crossSchool ? "FREE ELECTIVE" : String.valueOf(c.getCourseType());
            String schoolTag = c.getSchool() != null ? " [" + c.getSchool() + "]" : "";
            System.out.printf("  %-8s %-35s %2dcr  %-15s yr%d%s%n",
                    c.getCourseCode(), c.getCourseName(), c.getCredits(),
                    typeLabel, c.getTargetYear(), schoolTag);
            if (!c.getInstructorIds().isEmpty()) {
                String names = c.getInstructorIds().stream()
                        .map(id -> { User u = db.getUserById(id); return u != null ? u.getFullName() : id; })
                        .collect(Collectors.joining(", "));
                System.out.println("           Instructor(s): " + names);
            }
            c.getLessons().forEach(l -> System.out.println("           " + l.getType() + " @ " + l.getRoom() + " " + l.getTime()));
        }
    }

    public void listAllCourses() {
        listAllCourses(null);
    }

    /**
     * Lists all courses, marking with {@code *} the ones the given teacher instructs.
     *
     * @param teacher the teacher whose courses to highlight
     */
    public void listCoursesForTeacher(Teacher teacher) {
        Database db = Database.getInstance();
        System.out.println("=== MY COURSES ===");
        for (Course c : db.getAllCourses()) {
            boolean mine = c.getInstructorIds().contains(teacher.getId());
            System.out.printf("  %s%-8s %-35s %2dcr%n",
                    mine ? "*" : " ", c.getCourseCode(), c.getCourseName(), c.getCredits());
        }
    }

    /**
     * Submits a course registration request for a student. Validates fail limit,
     * duplicate enrollment, credit cap, and existing pending requests before creating the request.
     *
     * @param student the student requesting enrollment
     * @param code    the course code (e.g. "CS101")
     */
    public void studentRegister(Student student, String code) {
        Database db = Database.getInstance();
        if (student.hasExceededFailLimit()) {
            System.out.println("[Denied] You have exceeded the maximum of 3 course failures and cannot register for new courses.");
            return;
        }
        Course course = db.getCourseByCode(code);
        if (course == null) { System.out.println("[Course] Not found: " + code); return; }
        if (student.getRegisteredCourseIds().contains(code)) {
            System.out.println("[Registration] Already enrolled in " + code); return;
        }
        boolean alreadyPending = db.getAllRequests().stream()
                .anyMatch(r -> r instanceof CourseRegistrationRequest
                        && r.getRequesterId().equals(student.getId())
                        && ((CourseRegistrationRequest) r).getCourseCode().equals(code)
                        && r.getStatus() == RequestStatus.VIEWED);
        if (alreadyPending) {
            System.out.println("[Registration] A pending request for " + code + " already exists."); return;
        }
        if (student.getTotalCredits() + course.getCredits() > 21) {
            System.out.println("[Registration] Credit limit (21) would be exceeded."); return;
        }
        CourseRegistrationRequest crReq = new CourseRegistrationRequest(student.getId(), code);
        db.addCourseRegRequest(crReq);
        db.saveToDisk();
        System.out.println("[Registration] Request #" + crReq.getRequestId() + " submitted for "
                + course.getCourseName() + ". Awaiting manager approval.");
    }

    /**
     * Drops a student from a course and persists the change.
     *
     * @param student the student dropping the course
     * @param code    the course code to drop
     */
    public void studentDrop(Student student, String code) {
        Database db = Database.getInstance();
        Course course = db.getCourseByCode(code);
        if (course == null) { System.out.println("[Course] Not found: " + code); return; }
        boolean dropped = student.dropCourse(course);
        System.out.println(dropped ? "[Dropped] " + code : "[Error] Not registered in " + code);
        if (dropped) db.saveToDisk();
    }

    /**
     * Prints the latest mark for every course the student is registered in, plus their GPA.
     *
     * @param student the student whose marks to display
     */
    public void studentViewMarks(Student student) {
        Database db = Database.getInstance();
        student.recalculateGpa();
        System.out.println("=== MARKS: " + student.getFullName() + " ===");
        List<String> ids = student.getRegisteredCourseIds();
        if (ids == null || ids.isEmpty()) { System.out.println("  (no courses registered)"); return; }
        for (String code : ids) {
            Course c = db.getCourseByCode(code);
            if (c == null) { System.out.printf("  %-10s  (course data unavailable)%n", code); continue; }
            Mark m = c.getLatestMark(student.getId());
            if (m != null)
                System.out.printf("  %-10s  Att1=%.1f  Att2=%.1f  Final=%.1f  Total=%.1f  %s%n",
                        code, m.getFirstAttestation(), m.getSecondAttestation(),
                        m.getFinalExam(), m.getTotal(), m.getLetterGrade());
            else
                System.out.printf("  %-10s  (no mark yet)%n", code);
        }
        System.out.printf("  GPA: %.2f | Credits: %d%n", student.getGpa(), student.getTotalCredits());
    }

    /** Prints all course registration requests that are still in the VIEWED (pending) state. */
    public void listPendingRegistrations() {
        Database db = Database.getInstance();
        List<Request> pending = db.getAllRequests().stream()
                .filter(r -> r instanceof CourseRegistrationRequest && r.getStatus() == RequestStatus.VIEWED)
                .collect(Collectors.toList());
        if (pending.isEmpty()) { System.out.println("[Registration] No pending registration requests."); return; }
        System.out.println("=== Pending Course Registrations ===");
        for (Request r : pending) {
            User u = db.getUserById(r.getRequesterId());
            String name = u != null ? u.getFullName() : r.getRequesterId();
            System.out.printf("  #%-4s  %-20s [%s]  →  %s%n",
                    r.getRequestId(), name, r.getRequesterId(),
                    ((CourseRegistrationRequest) r).getCourseCode());
        }
    }

    /**
     * Approves a pending course registration request and enrolls the student.
     *
     * @param manager the manager approving the request
     * @param reqId   the request ID to approve
     */
    public void managerApproveReg(Manager manager, String reqId) {
        Database db = Database.getInstance();
        Request r = db.getRequestById(reqId);
        if (!(r instanceof CourseRegistrationRequest)) {
            System.out.println("[Registration] Request not found: " + reqId); return;
        }
        if (r.getStatus() != RequestStatus.VIEWED) {
            System.out.println("[Registration] Already processed: " + r.getStatus()); return;
        }
        CourseRegistrationRequest crr = (CourseRegistrationRequest) r;
        User su = db.getUserById(crr.getRequesterId());
        Student s = (su instanceof ResearcherDecorator)
                ? (Student) ((ResearcherDecorator) su).getWrappedUser() : (Student) su;
        Course course = db.getCourseByCode(crr.getCourseCode());
        if (s == null) { System.out.println("[Registration] Student not found."); return; }
        if (course == null) { System.out.println("[Registration] Course not found: " + crr.getCourseCode()); return; }
        r.setStatus(RequestStatus.ACCEPTED);
        boolean ok = s.registerForCourse(course);
        manager.log("Approved registration: " + s.getId() + " → " + course.getCourseCode());
        System.out.println("[Manager " + manager.getFullName() + "] Registration "
                + (ok ? "APPROVED" : "FAILED") + ": " + s.getFullName() + " → " + course.getCourseName());
        db.saveUser(s);
        db.saveToDisk();
    }

    /**
     * Rejects a pending course registration request.
     *
     * @param manager the manager rejecting the request
     * @param reqId   the request ID to reject
     */
    public void managerRejectReg(Manager manager, String reqId) {
        Database db = Database.getInstance();
        Request r = db.getRequestById(reqId);
        if (!(r instanceof CourseRegistrationRequest)) {
            System.out.println("[Registration] Request not found: " + reqId); return;
        }
        if (r.getStatus() != RequestStatus.VIEWED) {
            System.out.println("[Registration] Already processed: " + r.getStatus()); return;
        }
        CourseRegistrationRequest crr = (CourseRegistrationRequest) r;
        User su = db.getUserById(crr.getRequesterId());
        Student s = (su instanceof ResearcherDecorator)
                ? (Student) ((ResearcherDecorator) su).getWrappedUser() : (Student) su;
        Course course = db.getCourseByCode(crr.getCourseCode());
        r.setStatus(RequestStatus.REJECTED);
        if (s != null && course != null)
            manager.log("Rejected registration: " + s.getId() + " → " + course.getCourseCode());
        System.out.println("[Registration] Request #" + reqId + " rejected.");
        db.saveToDisk();
    }

    /**
     * Creates and persists a new course. Rejects if the course code already exists.
     *
     * @param code       unique course code (e.g. "CS201")
     * @param name       full course name
     * @param credits    credit hours
     * @param courseType MAJOR, MINOR, or FREE_ELECTIVE
     * @param targetYear the year of study this course targets
     * @param school     the faculty this course belongs to
     */
    public void addCourse(String code, String name, int credits, CourseType courseType, int targetYear, School school) {
        Database db = Database.getInstance();
        if (db.getCourseByCode(code) != null) { System.out.println("[Course] Course code already exists: " + code); return; }
        Course course = new Course(code, name, credits, courseType, targetYear, school);
        db.saveCourse(course);
        db.saveToDisk();
        System.out.println("[Course] Added: " + course);
    }
}
