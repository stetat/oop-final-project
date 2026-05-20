package project;

import java.util.*;
import project.models.actors.*;
import project.models.enums.CourseType;
import project.models.enums.LessonType;
import project.models.enums.ManagerType;
import project.models.enums.School;
import project.models.enums.TeacherTitle;
import project.models.enums.UrgencyLevel;
import project.models.errors.InvalidSupervisorException;
import project.models.errors.NotResearcherException;
import project.models.others.*;
import project.patterns.*;
import project.services.*;
import project.storage.Database;


/** Entry point for the KBTU University System. Seeds the database and runs all demos before launching the interactive CLI. */
public class Main {

    /**
     * Seeds the database, runs ten feature demos, then starts the interactive menu.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════╗");
        System.out.println("║   KBTU University System   ║");
        System.out.println("╚════════════════════════════╝\n");

        Database db = Database.getInstance();
        seedDatabase(db);

        System.out.println("\n━━━━━━━━ DEMO 1: AUTHENTICATION ━━━━━━━━");
        demoAuthentication();

        System.out.println("\n━━━━━━━━ DEMO 2: COURSE REGISTRATION ━━━━━━━━");
        demoCourseRegistration(db);

        System.out.println("\n━━━━━━━━ DEMO 3: MARK ASSIGNMENT & TRANSCRIPT ━━━━━━━━");
        demoMarks(db);

        System.out.println("\n━━━━━━━━ DEMO 4: RESEARCH (h-index, papers, supervisor) ━━━━━━━━");
        demoResearch(db);

        System.out.println("\n━━━━━━━━ DEMO 5: OBSERVER PATTERN (Journal) ━━━━━━━━");
        demoObserver(db);

        System.out.println("\n━━━━━━━━ DEMO 6: COMPLAINTS & TECH SUPPORT ━━━━━━━━");
        demoComplaintsAndTechSupport(db);

        System.out.println("\n━━━━━━━━ DEMO 7: MANAGER REPORT & NEWS ━━━━━━━━");
        demoManagerActions(db);

        System.out.println("\n━━━━━━━━ DEMO 8: SERIALIZATION (Save & Reload) ━━━━━━━━");
        demoSerialization(db);

        System.out.println("\n━━━━━━━━ DEMO 9: FACTORY & DECORATOR PATTERNS ━━━━━━━━");
        demoPatterns();

        System.out.println("\n━━━━━━━━ DEMO 10: COMPARATORS (Strategy) ━━━━━━━━");
        demoComparators(db);

        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║   All demos completed successfully!              ║");
        System.out.println("╚══════════════════════════════════════════════════╝");

        // Launch interactive CLI — users can now login and use the system
        InteractiveMenu.run();
    }


    /**
     * Populates the database with a fixed set of users and courses if it hasn't been seeded already.
     * Skips silently when {@code ADMIN01} is already present (i.e. data was loaded from disk).
     *
     * @param db the singleton database instance
     */
    static void seedDatabase(Database db) {
        if (db.getUserById("ADMIN01") != null) {
            System.out.println("[Seed] Database already populated — skipping seed.");
            return;
        }
        System.out.println("[Seed] Populating database...");

        Admin admin = new Admin("ADMIN01", "admin123", "Diana", "Seitkali", "d.seitkali@kbtu.kz", 300000);
        db.saveUser(admin);

        Manager manager = UserFactory.createManager("MGR01", "mgr123", "Aigerim", "Bekova", "a.bekova@kbtu.kz", 250000, ManagerType.OR);
        db.saveUser(manager);

        Teacher prof = UserFactory.createTeacher("TCH01", "tch123", "Pakizar", "Shamoi", "p.shamoi@kbtu.kz", 350000, TeacherTitle.PROFESSOR);
        prof.setSchool(School.SITE);
        Date d2020 = new GregorianCalendar(2020, 1, 1).getTime();
        Date d2022 = new GregorianCalendar(2022, 5, 15).getTime();
        Date d2023 = new GregorianCalendar(2023, 9, 20).getTime();
        ResearcherDecorator profRd = new ResearcherDecorator(prof);
        profRd.addResearchPaper(new ResearchPaper("LMS Logs and Student Performance", "Shamoi, P.", "Springer", 45, "10.1007/s12345", d2022, 18));
        profRd.addResearchPaper(new ResearchPaper("Influence of Retaking a Course", "Shamoi, P., Izbassar, A.", "IEEE", 31, "10.1109/abc", d2023, 12));
        profRd.addResearchPaper(new ResearchPaper("Deep Learning in Education", "Shamoi, P.", "Elsevier", 20, "10.1016/xyz", d2020, 22));
        db.saveUser(profRd);

        Teacher lector = UserFactory.createTeacher("TCH02", "tch456", "Assylzhan", "Izbassar", "a.izbassar@kbtu.kz", 200000, TeacherTitle.LECTOR);
        lector.setSchool(School.SITE);
        db.saveUser(lector);

        Student student1 = new Student("STU01", "stu123", "Amir", "Nurlanov", "a.nurlanov@kbtu.kz");
        student1.setYearOfStudy(2);
        student1.setSchool(School.SITE);
        db.saveUser(student1);

        Student student2 = new Student("STU02", "stu456", "Zarina", "Alieva", "z.alieva@kbtu.kz");
        student2.setYearOfStudy(3);
        student2.setSchool(School.SITE);
        db.saveUser(student2);

        GraduateStudent phd = UserFactory.createGraduateStudent("PHD01", "phd123", "Bekzat", "Seilov", "b.seilov@kbtu.kz", true);
        phd.setSchool(School.SITE);
        db.saveUser(new ResearcherDecorator(phd));

        GraduateStudent master = UserFactory.createGraduateStudent("MST01", "mst123", "Nurgul", "Dzhaksybekova", "n.dzhaksybekova@kbtu.kz", false);
        master.setSchool(School.SITE);
        db.saveUser(new ResearcherDecorator(master));

        TechSupportSpecialist tech = new TechSupportSpecialist("TECH01", "tech123", "Marat", "Omarov", "m.omarov@kbtu.kz", 180000);
        db.saveUser(tech);

        Course oop = new Course("CS101", "Object-Oriented Programming", 6, CourseType.MAJOR, 2, School.SITE);
        Course ml  = new Course("CS305", "Machine Learning", 6, CourseType.MAJOR, 3, School.SITE);
        Course eng = new Course("EN101", "Academic English", 3, CourseType.FREE_ELECTIVE, 1, School.BS);
        Course ds  = new Course("CS201", "Data Structures", 5, CourseType.MAJOR, 2, School.SITE);
        oop.addLesson(new Lesson(LessonType.LECTURE, "Room 201", "Mon 09:00-10:30"));
        oop.addLesson(new Lesson(LessonType.PRACTICE, "Lab 105", "Wed 11:00-12:30"));
        oop.addInstructorId(prof.getId());
        ml.addInstructorId(prof.getId());
        ds.addInstructorId(lector.getId());
        db.saveCourse(oop); db.saveCourse(ml); db.saveCourse(eng); db.saveCourse(ds);

        System.out.println("[Seed] Done. Users: " + db.getAllUsers().size() + ", Courses: " + db.getAllCourses().size());
    }


    /** Demonstrates successful login, failed login with wrong password, login with unknown ID, and logout. */
    static void demoAuthentication() {
        User u = AuthService.login("STU01", "stu123");
        System.out.println("Logged in as: " + u);

        AuthService.login("STU01", "wrongpass");

        AuthService.login("UNKNOWN", "pass");

        AuthService.logout();

        AuthService.login("TCH01", "tch123");
        System.out.println("Current user role: " + AuthService.getCurrentUser().getRole());
        AuthService.logout();
    }

    /**
     * Demonstrates manager-approved course registration, credit-limit enforcement,
     * and over-credit rejection.
     *
     * @param db the database instance
     */
    static void demoCourseRegistration(Database db) {
        Student s1 = (Student) db.getUserById("STU01");
        Manager mgr = (Manager) db.getUserById("MGR01");
        Course oop = db.getCourseByCode("CS101");
        Course ds = db.getCourseByCode("CS201");
        Course ml = db.getCourseByCode("CS305");
        Course eng = db.getCourseByCode("EN101");

        ManagerService managerService = new ManagerService();
        System.out.println("Credits before: " + s1.getTotalCredits());
        managerService.approveRegistration(mgr, s1, oop);  
        managerService.approveRegistration(mgr, s1, ds);   
        managerService.approveRegistration(mgr, s1, eng);  

        managerService.approveRegistration(mgr, s1, ml);   

        Course extra = new Course("CS999", "Extra Course", 5, CourseType.MINOR, 2);
        db.saveCourse(extra);
        managerService.approveRegistration(mgr, s1, extra); 

        System.out.println("Total credits: " + s1.getTotalCredits() + " / 21 max");

        Student s2 = (Student) db.getUserById("STU02");
        managerService.approveRegistration(mgr, s2, oop);
        managerService.approveRegistration(mgr, s2, ml);
    }

    /**
     * Assigns attestation and final-exam marks for several students and courses,
     * then recalculates GPAs and prints transcripts.
     *
     * @param db the database instance
     */
    static void demoMarks(Database db) {
        User profUser = db.getUserById("TCH01");
        Teacher prof = profUser instanceof ResearcherDecorator
            ? (Teacher) ((ResearcherDecorator) profUser).getWrappedUser()
            : (Teacher) profUser;
        Student s1 = (Student) db.getUserById("STU01");
        Student s2 = (Student) db.getUserById("STU02");
        Course oop = db.getCourseByCode("CS101");
        Course ml = db.getCourseByCode("CS305");

        MarkService markService = new MarkService();
        markService.assignMark(prof, oop, s1.getId(), 25.0, 22.0, 35.0); 
        markService.assignMark(prof, ml, s1.getId(), 18.0, 15.0, 20.0); 
        markService.assignMark(prof, oop, s2.getId(), 28.0, 27.0, 38.0); 

        Course ds = db.getCourseByCode("CS201");
        Teacher lector = (Teacher) db.getUserById("TCH02");
        markService.assignMark(lector, ds, s1.getId(), 10.0, 8.0, 12.0); 

        s1.recalculateGpa();
        s2.recalculateGpa();
        s1.viewTranscript();
        s2.viewTranscript();
    }

    /**
     * Demonstrates h-index calculation, supervisor assignment (including invalid-supervisor exceptions),
     * research projects, and top-cited-researcher announcements.
     *
     * @param db the database instance
     */
    static void demoResearch(Database db) {
        ResearcherDecorator profRd = (ResearcherDecorator) db.getUserById("TCH01");
        ResearcherDecorator phdRd  = (ResearcherDecorator) db.getUserById("PHD01");
        ResearcherDecorator masterRd = (ResearcherDecorator) db.getUserById("MST01");
        GraduateStudent phd    = (GraduateStudent) phdRd.getWrappedUser();
        GraduateStudent master = (GraduateStudent) masterRd.getWrappedUser();

        System.out.println("Professor h-index: " + profRd.calculateHIndex());
        profRd.printPapers(PaperComparators.BY_CITATIONS_DESC);

        try {
            phd.setResearchSupervisor(profRd);
            System.out.println("PhD supervisor set successfully!");
        } catch (InvalidSupervisorException e) {
            System.out.println("[Exception] " + e.getMessage());
        }

        Date d2024 = new GregorianCalendar(2024, 3, 10).getTime();
        ResearchPaper phdPaper = new ResearchPaper("Neural Nets for Student Performance",
                "Seilov, B., Shamoi, P.", "IEEE", 5, "10.1109/phd1", d2024, 10);
        phd.getDiplomaProjects().add(phdPaper);
        phdRd.addResearchPaper(phdPaper);
        System.out.println("PhD h-index: " + phdRd.calculateHIndex());

        System.out.println("Lector: not a researcher (h-index = 0)");
        try {
            master.setResearchSupervisor(null); 
        } catch (InvalidSupervisorException e) {
            System.out.println("[Expected Exception] " + e.getMessage());
        }

        ResearchProject project = new ResearchProject("AI in Education");
        project.addParticipant(profRd);
        project.addParticipant(phdRd);
        System.out.println("Research project: " + project);

        try {
            project.addUser("NonResearcher String"); 
        } catch (NotResearcherException e) {
            System.out.println("[Expected Exception] " + e.getMessage());
        }

        try {
            project.addUser(profRd); 
            System.out.println("Added professor to project again (no-op).");
        } catch (NotResearcherException e) {
            System.out.println("[Unexpected] " + e.getMessage());
        }

        ResearchService rs = new ResearchService();
        rs.announceTopCitedResearcher();
    }

    /**
     * Demonstrates the Observer (publish–subscribe) pattern via a research journal:
     * three users subscribe, a paper is published (triggering notifications), one unsubscribes,
     * then a second paper is published.
     *
     * @param db the database instance
     */
    static void demoObserver(Database db) {
        ResearchJournal journal = new ResearchJournal("KBTU Journal of Computing");
        db.addJournal(journal);

        Student s1 = (Student) db.getUserById("STU01");
        Student s2 = (Student) db.getUserById("STU02");
        User prof = db.getUserById("TCH01"); 
        s1.subscribeToJournal(journal);
        s2.subscribeToJournal(journal);
        prof.subscribeToJournal(journal);

        System.out.println("Subscribed 3 users to '" + journal.getName() + "'");

        ResearchPaper newPaper = new ResearchPaper("Blockchain in University Systems", "Bekova, A.", "KBTU Journal", 2, "10.1234/kbtu2024", new GregorianCalendar(2024, 1, 1).getTime(), 8);
        journal.publishPaper(newPaper);

        s1.unsubscribeFromJournal(journal);
        ResearchPaper newPaper2 = new ResearchPaper("OOP Best Practices", "Izbassar, A.", "KBTU Journal", 3, "10.1234/kbtu2025", new GregorianCalendar(2025, 6, 1).getTime(), 14);
        journal.publishPaper(newPaper2); 
    }

    /**
     * Files two teacher complaints, handles tech-support requests (accept/reject),
     * and sends a message from a teacher to the manager.
     *
     * @param db the database instance
     */
    static void demoComplaintsAndTechSupport(Database db) {
        ResearcherDecorator profRd = (ResearcherDecorator) db.getUserById("TCH01");
        Teacher prof = (Teacher) profRd.getWrappedUser();
        TechSupportSpecialist tech = (TechSupportSpecialist) db.getUserById("TECH01");

        ComplaintService complaintService = new ComplaintService();
        complaintService.fileComplaint(prof, "STU01", UrgencyLevel.MEDIUM, "Student missed 3 consecutive labs.");
        complaintService.fileComplaint(prof, "STU02", UrgencyLevel.LOW, "Late submissions.");

        System.out.println("\nAll complaints:");
        new ComplaintService().getAllComplaints().forEach(c -> System.out.println("  " + c));

        Request req1 = new Request("Projector in Room 201 broken", "The projector shows distorted images since Monday.", UrgencyLevel.HIGH, prof.getId());
        Request req2 = new Request("Printer out of toner", "Lab 105 printer needs toner replacement.", UrgencyLevel.LOW, prof.getId());
        db.addRequest(req1); db.addRequest(req2);

        List<Request> pending = tech.getNewRequests();
        if (!pending.isEmpty()) {
            tech.viewRequest(pending.get(0).getRequestId());
            tech.acceptRequest(pending.get(0).getRequestId());
            if (pending.size() > 1) tech.rejectRequest(pending.get(1).getRequestId());
        }
        System.out.println("\nAll requests after handling:");
        db.getAllRequests().forEach(r -> System.out.println("  " + r));

        Manager mgr = (Manager) db.getUserById("MGR01");
        db.addMessage(new Message(prof.getId(), mgr.getId(), "Please check student Amir's registration status."));
        System.out.println("\nMessages for manager:");
        db.getMessagesForUser(mgr.getId()).forEach(m -> System.out.println("  " + m));
    }

    /**
     * Generates an academic performance report, adds news items, lists students/teachers,
     * and demonstrates teacher rating.
     *
     * @param db the database instance
     */
    static void demoManagerActions(Database db) {
        Manager mgr = (Manager) db.getUserById("MGR01");
        ManagerService managerService = new ManagerService();
        managerService.createStatisticalReport();

        boolean hasNews = db.getAllNews().stream().anyMatch(n -> "Semester Registration Open".equals(n.getTitle()));
        if (!hasNews) {
            managerService.addNews(mgr, new News("Semester Registration Open", "Course registration for Spring 2025 is now open.", false));
            managerService.addNews(mgr, new News("Research Grant Awarded", "KBTU received a major research grant from Ministry of Education.", true));
        }

        System.out.println("\nAll News (research pinned first):");
        db.getAllNews().forEach(n -> System.out.println("  " + n));

        managerService.viewStudents("gpa");
        managerService.viewTeachers();

        User prof = db.getUserById("TCH01"); 
        if (db.getAverageRating(prof.getId()) == 0.0) {
            db.addTeacherRating(prof.getId(), 5);
            db.addTeacherRating(prof.getId(), 4);
        }
        System.out.println("Average rating for " + prof.getFullName() + ": " + db.getAverageRating(prof.getId()));
    }

    /**
     * Saves the database to disk, reloads it, then verifies that a student's data
     * (including transcript) survived the round-trip.
     *
     * @param db the database instance
     */
    static void demoSerialization(Database db) {
        System.out.println("Saving database to disk...");
        db.saveToDisk();
        System.out.println("Reloading database from disk...");
        db.reloadFromDisk();
        System.out.println("Reloaded users: " + db.getAllUsers().size());
        System.out.println("Reloaded courses: " + db.getAllCourses().size());
        User reloadedStudent = db.getUserById("STU01");
        if (reloadedStudent instanceof Student) {
            System.out.println("Reloaded student: " + reloadedStudent);
            ((Student) reloadedStudent).viewTranscript();
        }
    }

    /** Exercises all five design patterns: Singleton, Factory, Strategy (comparators), Decorator, and Observer. */
    static void demoPatterns() {
        System.out.println("--- Pattern 1: Singleton ---");
        Database db1 = Database.getInstance();
        Database db2 = Database.getInstance();
        System.out.println("Same instance? " + (db1 == db2)); 

        System.out.println("\n--- Pattern 2: Factory ---");
        Teacher t = UserFactory.createTeacher("TCH99", "pass", "Test", "Teacher", "t@kbtu.kz", 200000, TeacherTitle.SENIOR_LECTOR);
        System.out.println("Factory created: " + t + " | researcher=false (not yet promoted)");

        System.out.println("\n--- Pattern 3: Strategy (Comparators) ---");
        System.out.println("Available comparators: BY_CITATIONS_DESC, BY_DATE_DESC, BY_LENGTH_DESC, BY_TITLE");
        User profUser = Database.getInstance().getUserById("TCH01");
        if (profUser instanceof ResearcherDecorator) {
            System.out.println("Papers by date (newest first):");
            ((ResearcherDecorator) profUser).printPapers(PaperComparators.BY_DATE_DESC);
        }

        System.out.println("\n--- Pattern 4: Decorator ---");
        Admin admin = (Admin) Database.getInstance().getUserById("ADMIN01");
        ResearcherDecorator decorated = new ResearcherDecorator(admin);
        decorated.addResearchPaper(new ResearchPaper("Admin Research Paper", "Seitkali, D.", "KBTU Review", 10, "10.9999/test", new GregorianCalendar(2023, 1, 1).getTime(), 5));
        System.out.println("Decorated admin h-index: " + decorated.calculateHIndex());
        decorated.printPapers(PaperComparators.BY_CITATIONS_ASC);

        System.out.println("\n--- Pattern 5: Observer (Journal) ---");
        System.out.println("See Demo 5 above — users notified via onNewPaperPublished()");
    }

    /**
     * Prints the professor's papers sorted by citations, date, and page count,
     * then shows both Plain Text and BibTeX citation formats.
     *
     * @param db the database instance
     */
    static void demoComparators(Database db) {
        User profUser = db.getUserById("TCH01");
        if (!(profUser instanceof ResearcherDecorator)) return;
        ResearcherDecorator prof = (ResearcherDecorator) profUser;
        System.out.println("Papers sorted by CITATIONS DESC:");
        prof.printPapers(PaperComparators.BY_CITATIONS_DESC);
        System.out.println("Papers sorted by DATE DESC (newest first):");
        prof.printPapers(PaperComparators.BY_DATE_DESC);
        System.out.println("Papers sorted by LENGTH DESC (most pages first):");
        prof.printPapers(PaperComparators.BY_LENGTH_DESC);
        if (!prof.getResearchPapersList().isEmpty()) {
            System.out.println("ResearchPaper getCitation() Plain Text:");
            System.out.println("  " + prof.getResearchPapersList().get(0).getCitation("Plain Text"));
            System.out.println("ResearchPaper getCitation() BibTeX:");
            System.out.println(prof.getResearchPapersList().get(0).getCitation("Bibtex"));
        }
    }
}
