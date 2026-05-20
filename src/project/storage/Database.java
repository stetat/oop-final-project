package project.storage;

import java.io.*;
import java.util.*;

import project.models.actors.*;
import project.models.enums.RequestStatus;
import project.models.enums.School;
import project.models.others.*;
import project.patterns.ResearchJournal;
import project.patterns.ResearcherDecorator;


/**
 * Singleton in-memory database for the KBTU university system.
 * Persists all data to {@code kbtu_database.dat} via Java serialization.
 * Always access through {@link #getInstance()}; never construct directly.
 */
public class Database implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String DATA_FILE = "kbtu_database.dat";

    // Singleton instance
    private static transient Database instance;

    private Map<String, User> users = new HashMap<>();
    private Map<String, Course> courses = new HashMap<>();
    private List<News> newsList = new ArrayList<>();
    private int newsIdCounter = 0;
    private List<Request> requests = new ArrayList<>();
    private int researcherReqIdCounter = 0;
    private int courseRegIdCounter = 0;
    private List<Complaint> complaints = new ArrayList<>();
    private List<Message> messages = new ArrayList<>();
    private List<ResearchJournal> journals = new ArrayList<>();
    private Map<String, List<Integer>> teacherRatings = new HashMap<>();
    private List<ResearchProject> projects = new ArrayList<>();
    private int projectIdCounter = 0;
    private int projectJoinReqIdCounter = 0;
    private List<StaffBulletin> staffBoard = new ArrayList<>();
    private Map<String, List<String>> journalSubscriptions = new HashMap<>();
    private List<Notification> notifications = new ArrayList<>();
    private List<Organization> organizations = new ArrayList<>();
    private int orgIdCounter = 0;
    private int orgJoinReqIdCounter = 0;

    private Database() {}

    /**
     * Returns the single shared Database instance, loading it from disk on first call.
     * Creates an empty database if no saved file exists.
     */
    public static Database getInstance() {
        if (instance == null) {
            instance = loadFromDisk();
            if (instance == null) instance = new Database();
        }
        return instance;
    }

    /**
     * Serializes the current database state to {@code kbtu_database.dat} using an atomic
     * write-to-temp-then-rename strategy to avoid corruption on crash.
     */
    public void saveToDisk() {
        File tmp = new File(DATA_FILE + ".tmp");
        File dst = new File(DATA_FILE);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tmp))) {
            oos.writeObject(this);
        } catch (IOException e) {
            System.out.println("[DB] Save failed: " + e.getMessage());
            System.err.println("[DB] Save failed: " + e.getMessage());
            return;
        }
        if (!tmp.renameTo(dst)) {
            try {
                java.nio.file.Files.move(tmp.toPath(), dst.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                System.out.println("[DB] Atomic rename failed: " + e.getMessage());
                return;
            }
        }
        System.out.println("[DB] Saved.");
    }

    /** Deserializes the database from disk; returns {@code null} if the file doesn't exist or fails to load. */
    private static Database loadFromDisk() {
        File f = new File(DATA_FILE);
        if (!f.exists()) return null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            Database db = (Database) ois.readObject();
            System.out.println("[DB] Database loaded from " + DATA_FILE);
            return db;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("[DB] Load failed: " + e.getMessage());
            System.err.println("[DB] Load failed: " + e.getMessage());
            return null;
        }
    }

    /** Clears the in-memory singleton so the next {@link #getInstance()} call reloads from disk. Useful in tests. */
    public static void resetInstance() { instance = null; }

    /**
     * Refreshes all collections from disk without replacing the singleton instance.
     * Used by the interactive menu to pick up changes made by other processes.
     */
    public void reloadFromDisk() {
        File f = new File(DATA_FILE);
        if (!f.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            Database loaded = (Database) ois.readObject();
            this.users = loaded.users != null ? loaded.users : new HashMap<>();
            this.courses = loaded.courses != null ? loaded.courses : new HashMap<>();
            this.newsList = loaded.newsList != null ? loaded.newsList : new ArrayList<>();
            this.newsIdCounter = loaded.newsIdCounter;
            this.requests = loaded.requests != null ? loaded.requests : new ArrayList<>();
            this.researcherReqIdCounter = loaded.researcherReqIdCounter;
            this.courseRegIdCounter = loaded.courseRegIdCounter;
            this.complaints = loaded.complaints != null ? loaded.complaints : new ArrayList<>();
            this.messages = loaded.messages != null ? loaded.messages : new ArrayList<>();
            this.journals = loaded.journals != null ? loaded.journals : new ArrayList<>();
            this.teacherRatings = loaded.teacherRatings != null ? loaded.teacherRatings : new HashMap<>();
            this.projects = loaded.projects != null ? loaded.projects : new ArrayList<>();
            this.projectIdCounter = loaded.projectIdCounter;
            this.projectJoinReqIdCounter = loaded.projectJoinReqIdCounter;
            this.staffBoard = loaded.staffBoard != null ? loaded.staffBoard : new ArrayList<>();
            this.journalSubscriptions = loaded.journalSubscriptions != null ? loaded.journalSubscriptions : new HashMap<>();
            this.notifications = loaded.notifications != null ? loaded.notifications : new ArrayList<>();
            this.organizations = loaded.organizations != null ? loaded.organizations : new ArrayList<>();
            this.orgIdCounter = loaded.orgIdCounter;
            this.orgJoinReqIdCounter = loaded.orgJoinReqIdCounter;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("[DB] Reload error (keeping current state): " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /** Inserts or updates a user entry (keyed by their ID). */
    public void saveUser(User user) { users.put(user.getId(), user); }

    /** Permanently removes a user by ID. */
    public void removeUser(String id) { users.remove(id); }

    /** Returns the user with the given ID, or {@code null} if not found. */
    public User getUserById(String id) { return users.get(id); }

    /** Returns all users currently in the database. */
    public Collection<User> getAllUsers() { return users.values(); }

    /** Returns all undergraduate students (excludes graduate students). */
    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        for (User u : users.values()) {
            User base = (u instanceof ResearcherDecorator) ? ((ResearcherDecorator) u).getWrappedUser() : u;
            if (base instanceof Student && !(base instanceof GraduateStudent)) list.add((Student) base);
        }
        return list;
    }
    /** Returns all students, including graduate students. */
    public List<Student> getAllStudentsIncludingGrad() {
        List<Student> list = new ArrayList<>();
        for (User u : users.values()) {
            User base = (u instanceof ResearcherDecorator) ? ((ResearcherDecorator) u).getWrappedUser() : u;
            if (base instanceof Student) list.add((Student) base);
        }
        return list;
    }
    /** Returns all teachers, unwrapping any {@link ResearcherDecorator} wrappers. */
    public List<Teacher> getAllTeachers() {
        List<Teacher> list = new ArrayList<>();
        for (User u : users.values()) {
            User base = (u instanceof ResearcherDecorator) ? ((ResearcherDecorator) u).getWrappedUser() : u;
            if (base instanceof Teacher) list.add((Teacher) base);
        }
        return list;
    }

    /**
     * Wraps a plain user in a {@link ResearcherDecorator} and saves the result.
     * Does nothing if the user is already a researcher.
     *
     * @param user the user to promote
     */
    public void promoteToResearcher(User user) {
        if (user instanceof ResearcherDecorator) return;
        users.put(user.getId(), new ResearcherDecorator(user));
    }

    public void saveCourse(Course course) { courses.put(course.getCourseCode(), course); }
    public void removeCourse(String code) { courses.remove(code); }
    public Course getCourseByCode(String code) { return courses.get(code); }
    public Collection<Course> getAllCourses() { return courses.values(); }

    /**
     * Assigns a sequential ID to the news item and adds it to the list.
     *
     * @param news the news item to store
     */
    public void addNews(News news) {
        news.setNewsId(String.valueOf(newsIdCounter++));
        newsList.add(news);
    }
    public void removeNews(String newsId) { newsList.removeIf(n -> n.getNewsId().equals(newsId)); }
    /** Returns all news items sorted by publish date ascending. */
    public List<News> getAllNews() {
        List<News> sorted = new ArrayList<>(newsList);
        sorted.sort((a, b) -> {
            if (a.getPublishedAt() == null) return -1;
            if (b.getPublishedAt() == null) return 1;
            return a.getPublishedAt().compareTo(b.getPublishedAt());
        });
        return sorted;
    }

    public void addRequest(Request req) { requests.add(req); }
    public void addCourseRegRequest(CourseRegistrationRequest req) {
        req.setRequestId(String.valueOf(courseRegIdCounter++));
        requests.add(req);
    }
    public void addResearcherRequest(ResearcherRequest req) {
        req.setRequestId("RES-" + researcherReqIdCounter++);
        requests.add(req);
    }
    public void addProjectJoinRequest(ProjectJoinRequest req) {
        req.setRequestId("PJ-" + projectJoinReqIdCounter++);
        requests.add(req);
    }
    /**
     * Returns all pending (VIEWED) project-join requests for projects owned by {@code ownerId}.
     *
     * @param ownerId the project owner's user ID
     */
    public List<ProjectJoinRequest> getProjectJoinRequestsForOwner(String ownerId) {
        List<ProjectJoinRequest> result = new ArrayList<>();
        for (Request r : requests) {
            if (!(r instanceof ProjectJoinRequest)) continue;
            ProjectJoinRequest pjr = (ProjectJoinRequest) r;
            if (r.getStatus() != RequestStatus.VIEWED) continue;
            ResearchProject proj = getProjectById(pjr.getProjectId());
            if (proj != null && ownerId.equals(proj.getOwnerId())) result.add(pjr);
        }
        return result;
    }
    public List<Request> getAllRequests() { return requests; }
    public Request getRequestById(String id) {
        return requests.stream().filter(r -> r.getRequestId().equals(id)).findFirst().orElse(null);
    }

    public void addComplaint(Complaint c) { complaints.add(c); }
    public List<Complaint> getAllComplaints() { return complaints; }

    public void addMessage(Message m) { messages.add(m); }
    public List<Message> getMessagesForUser(String userId) {
        List<Message> result = new ArrayList<>();
        for (Message m : messages) if (userId.equals(m.getReceiverId())) result.add(m);
        return result;
    }

    public void addJournal(ResearchJournal j) { journals.add(j); }
    public List<ResearchJournal> getAllJournals() { return journals; }
    public ResearchJournal getJournalByName(String n) {
        return journals.stream().filter(j -> j.getName().equals(n)).findFirst().orElse(null);
    }

    public void addProject(ResearchProject p) {
        if (projects == null) projects = new ArrayList<>();
        p.setId(String.valueOf(projectIdCounter++));
        projects.add(p);
    }
    public List<ResearchProject> getAllProjects() {
        if (projects == null) projects = new ArrayList<>();
        return projects;
    }
    public ResearchProject getProjectById(String id) {
        if (projects == null) return null;
        return projects.stream().filter(p -> id.equals(p.getId())).findFirst().orElse(null);
    }

    /**
     * Records a 1–5 rating submitted by a student for a teacher.
     *
     * @param teacherId the teacher's ID
     * @param rating    the score (expected 1–5)
     */
    public void addTeacherRating(String teacherId, int rating) {
        teacherRatings.computeIfAbsent(teacherId, k -> new ArrayList<>()).add(rating);
    }
    /**
     * Returns the mean of all submitted ratings for a teacher, or 0.0 if none exist.
     *
     * @param teacherId the teacher's ID
     */
    public double getAverageRating(String teacherId) {
        List<Integer> r = teacherRatings.getOrDefault(teacherId, Collections.emptyList());
        return r.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }

    public List<ResearcherDecorator> getAllResearchers() {
        List<ResearcherDecorator> list = new ArrayList<>();
        for (User u : users.values()) {
            if (u instanceof ResearcherDecorator) list.add((ResearcherDecorator) u);
        }
        return list;
    }

    /**
     * Returns the researcher in the given school with the highest total citation count,
     * or {@code null} if no researchers belong to that school.
     *
     * @param school the faculty/school to filter by
     */
    public ResearcherDecorator getTopCitedResearcherBySchool(School school) {
        ResearcherDecorator top = null; int max = -1;
        for (ResearcherDecorator r : getAllResearchers()) {
            User base = r.getWrappedUser();
            School s = (base instanceof Teacher) ? ((Teacher) base).getSchool()
                     : (base instanceof Student)  ? ((Student)  base).getSchool()
                     : null;
            if (school != s) continue;
            int total = r.getResearchPapersList().stream().mapToInt(ResearchPaper::getCitations).sum();
            if (total > max) { max = total; top = r; }
        }
        return top;
    }

    /**
     * Returns the researcher whose papers published in {@code year} have the most total citations,
     * or {@code null} if no papers were published that year.
     *
     * @param year the publication year to filter by (e.g. 2023)
     */
    public ResearcherDecorator getTopCitedResearcherOfYear(int year) {
        ResearcherDecorator top = null; int max = -1;
        for (ResearcherDecorator r : getAllResearchers()) {
            int total = r.getResearchPapersList().stream()
                .filter(p -> p.getPublishDate() != null
                        && new java.util.Calendar.Builder()
                               .setInstant(p.getPublishDate()).build()
                               .get(java.util.Calendar.YEAR) == year)
                .mapToInt(ResearchPaper::getCitations).sum();
            if (total > max) { max = total; top = r; }
        }
        return top;
    }

    /**
     * Returns the researcher with the highest total citation count across all their papers,
     * or {@code null} if no researchers exist.
     */
    public ResearcherDecorator getTopCitedResearcher() {
        ResearcherDecorator top = null; int maxCitations = -1;
        for (ResearcherDecorator r : getAllResearchers()) {
            int total = r.getResearchPapersList().stream().mapToInt(ResearchPaper::getCitations).sum();
            if (total > maxCitations) { maxCitations = total; top = r; }
        }
        return top;
    }

    public void addJournalSubscription(String journalName, String userId) {
        journalSubscriptions.computeIfAbsent(journalName, k -> new ArrayList<>());
        if (!journalSubscriptions.get(journalName).contains(userId))
            journalSubscriptions.get(journalName).add(userId);
    }
    public void removeJournalSubscription(String journalName, String userId) {
        List<String> subs = journalSubscriptions.get(journalName);
        if (subs != null) subs.remove(userId);
    }
    public List<String> getSubscribersForJournal(String journalName) {
        if (journalSubscriptions == null) return Collections.emptyList();
        return journalSubscriptions.getOrDefault(journalName, Collections.emptyList());
    }

    public void addNotification(Notification n) {
        if (notifications == null) notifications = new ArrayList<>();
        notifications.add(n);
    }
    public List<Notification> getNotificationsForUser(String userId) {
        if (notifications == null) return Collections.emptyList();
        List<Notification> result = new ArrayList<>();
        for (Notification n : notifications)
            if (userId.equals(n.getRecipientId())) result.add(n);
        return result;
    }

    public void addStaffBulletin(StaffBulletin b) {
        if (staffBoard == null) staffBoard = new ArrayList<>();
        staffBoard.add(b);
    }
    public List<StaffBulletin> getAllStaffBulletins() {
        if (staffBoard == null) staffBoard = new ArrayList<>();
        return staffBoard;
    }

    public void addOrganization(Organization org) {
        if (organizations == null) organizations = new ArrayList<>();
        org.setId(String.valueOf(orgIdCounter++));
        organizations.add(org);
    }
    public List<Organization> getAllOrganizations() {
        if (organizations == null) organizations = new ArrayList<>();
        return organizations;
    }
    public Organization getOrganizationById(String id) {
        if (organizations == null) return null;
        return organizations.stream().filter(o -> id.equals(o.getId())).findFirst().orElse(null);
    }
    public void addOrgJoinRequest(OrgJoinRequest req) {
        req.setRequestId("OJ-" + orgJoinReqIdCounter++);
        requests.add(req);
    }
    /**
     * Returns all pending (VIEWED) organization-join requests for organizations led by {@code leadId}.
     *
     * @param leadId the organization leader's user ID
     */
    public List<OrgJoinRequest> getOrgJoinRequestsForLead(String leadId) {
        List<OrgJoinRequest> result = new ArrayList<>();
        for (Request r : requests) {
            if (!(r instanceof OrgJoinRequest)) continue;
            if (r.getStatus() != RequestStatus.VIEWED) continue;
            OrgJoinRequest ojr = (OrgJoinRequest) r;
            Organization org = getOrganizationById(ojr.getOrgId());
            if (org != null && leadId.equals(org.getLeadId())) result.add(ojr);
        }
        return result;
    }

    /**
     * Prints every research paper in the system, sorted by the given comparator.
     *
     * @param comparator sort order (e.g. {@link project.patterns.PaperComparators#BY_CITATIONS_DESC})
     */
    public void printAllResearchPapers(Comparator<ResearchPaper> comparator) {
        List<ResearchPaper> all = new ArrayList<>();
        for (ResearcherDecorator r : getAllResearchers()) all.addAll(r.getResearchPapersList());
        all.sort(comparator);
        System.out.println("=== ALL RESEARCH PAPERS ===");
        all.forEach(p -> System.out.println("  " + p.getCitation("Plain Text")));
    }
}
