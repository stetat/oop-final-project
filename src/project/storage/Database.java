package project.storage;

import java.io.*;
import java.util.*;
import project.models.actors.*;
import project.models.others.*;
import project.patterns.ResearchJournal;


public class Database implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String DATA_FILE = "kbtu_database.dat";

    // Singleton instance
    private static transient Database instance;

    // Data collections
    private Map<String, User> users = new HashMap<>();
    private Map<String, Course> courses = new HashMap<>();
    private List<News> newsList = new ArrayList<>();
    private List<Request> requests = new ArrayList<>();
    private List<Complaint> complaints = new ArrayList<>();
    private List<Message> messages = new ArrayList<>();
    private List<ResearchJournal> journals = new ArrayList<>();
    private Map<String, List<Integer>> teacherRatings = new HashMap<>();

    private Database() {} // private constructor enforces Singleton

    /** Returns the singleton instance; loads from disk if exists, else creates fresh. */
    public static Database getInstance() {
        if (instance == null) {
            instance = loadFromDisk();
            if (instance == null) instance = new Database();
        }
        return instance;
    }

    /** Saves entire database to disk via Java Serialization. */
    public void saveToDisk() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(this);
            System.out.println("[DB] Database saved to " + DATA_FILE);
        } catch (IOException e) {
            System.err.println("[DB] Save failed: " + e.getMessage());
        }
    }

    /** Loads database from disk. Returns null if file doesn't exist. */
    private static Database loadFromDisk() {
        File f = new File(DATA_FILE);
        if (!f.exists()) return null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            Database db = (Database) ois.readObject();
            System.out.println("[DB] Database loaded from " + DATA_FILE);
            return db;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[DB] Load failed: " + e.getMessage());
            return null;
        }
    }

    /** Resets the in-memory instance (useful for testing). Does NOT delete disk file. */
    public static void resetInstance() { instance = null; }

    // User CRUD
    public void saveUser(User user) { users.put(user.getId(), user); }
    public void removeUser(String id) { users.remove(id); }
    public User getUserById(String id) { return users.get(id); }
    public Collection<User> getAllUsers() { return users.values(); }

    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        for (User u : users.values()) if (u instanceof Student && !(u instanceof GraduateStudent)) list.add((Student) u);
        return list;
    }
    public List<Student> getAllStudentsIncludingGrad() {
        List<Student> list = new ArrayList<>();
        for (User u : users.values()) if (u instanceof Student) list.add((Student) u);
        return list;
    }
    public List<Teacher> getAllTeachers() {
        List<Teacher> list = new ArrayList<>();
        for (User u : users.values()) if (u instanceof Teacher) list.add((Teacher) u);
        return list;
    }

    //Course CRUD
    public void saveCourse(Course course) { courses.put(course.getCourseCode(), course); }
    public void removeCourse(String code) { courses.remove(code); }
    public Course getCourseByCode(String code) { return courses.get(code); }
    public Collection<Course> getAllCourses() { return courses.values(); }

    // News
    public void addNews(News news) { newsList.add(news); }
    public void removeNews(String newsId) { newsList.removeIf(n -> n.getNewsId().equals(newsId)); }
    public List<News> getAllNews() {
        List<News> sorted = new ArrayList<>(newsList);
        Collections.sort(sorted);
        return sorted;
    }

    // Requests
    public void addRequest(Request req) { requests.add(req); }
    public List<Request> getAllRequests() { return requests; }
    public Request getRequestById(String id) {
        return requests.stream().filter(r -> r.getRequestId().equals(id)).findFirst().orElse(null);
    }

    // Complaints
    public void addComplaint(Complaint c) { complaints.add(c); }
    public List<Complaint> getAllComplaints() { return complaints; }

    // Messages
    public void addMessage(Message m) { messages.add(m); }
    public List<Message> getMessagesForUser(String userId) {
        List<Message> result = new ArrayList<>();
        for (Message m : messages) if (userId.equals(m.getReceiverId())) result.add(m);
        return result;
    }

    // Research Journals
    public void addJournal(ResearchJournal j) { journals.add(j); }
    public List<ResearchJournal> getAllJournals() { return journals; }
    public ResearchJournal getJournalByName(String n) {
        return journals.stream().filter(j -> j.getName().equals(n)).findFirst().orElse(null);
    }

    // Teacher Ratings
    public void addTeacherRating(String teacherId, int rating) {
        teacherRatings.computeIfAbsent(teacherId, k -> new ArrayList<>()).add(rating);
    }
    public double getAverageRating(String teacherId) {
        List<Integer> r = teacherRatings.getOrDefault(teacherId, Collections.emptyList());
        return r.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }

    // Research utilities

    /** Returns all Researcher objects in the system (Teachers who are researchers + Grad students). */
    public List<Researcher> getAllResearchers() {
        List<Researcher> list = new ArrayList<>();
        for (User u : users.values()) {
            if (u instanceof Researcher) {
                if (u instanceof Teacher && ((Teacher) u).isResearcher()) list.add((Researcher) u);
                else if (u instanceof GraduateStudent) list.add((Researcher) u);
                else if (u instanceof Student && ((Student) u).isResearcher()) list.add((Researcher) u);
            }
        }
        return list;
    }

    /** Finds the top-cited researcher by total citations. */
    public User getTopCitedResearcher() {
        User top = null; int maxCitations = -1;
        for (Researcher r : getAllResearchers()) {
            int total = r.getResearchPapersList().stream().mapToInt(ResearchPaper::getCitations).sum();
            if (total > maxCitations) { maxCitations = total; top = (User) r; }
        }
        return top;
    }

    /** Prints all research papers of all researchers, sorted by comparator. */
    public void printAllResearchPapers(Comparator<ResearchPaper> comparator) {
        List<ResearchPaper> all = new ArrayList<>();
        for (Researcher r : getAllResearchers()) all.addAll(r.getResearchPapersList());
        all.sort(comparator);
        System.out.println("=== ALL RESEARCH PAPERS ===");
        all.forEach(p -> System.out.println("  " + p.getCitation("Plain Text")));
    }
}
