package project;

import java.util.*;
import project.enums.*;
import project.errors.*;
import project.models.actors.*;
import project.models.others.*;
import project.patterns.*;
import project.services.*;
import project.storage.Database;


public class Main {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║   KBTU Research-Oriented University System       ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

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
    }

    //sample data

    static void seedDatabase(Database db) {
        System.out.println("[Seed] Populating database...");

        // Admin
        Admin admin = new Admin("ADMIN01", "admin123", "Diana", "Seitkali", "d.seitkali@kbtu.kz", 300000);
        db.saveUser(admin);

        // Manager (OR type)
        Manager manager = UserFactory.createManager("MGR01", "mgr123", "Aigerim", "Bekova", "a.bekova@kbtu.kz", 250000, ManagerType.OR);
        db.saveUser(manager);

        // Professor
        Teacher prof = UserFactory.createTeacher("TCH01", "tch123", "Pakizar", "Shamoi", "p.shamoi@kbtu.kz", 350000, TeacherTitle.PROFESSOR);
        // Add research papers with varied citations for h-index demo
        Date d2020 = new GregorianCalendar(2020, 1, 1).getTime();
        Date d2022 = new GregorianCalendar(2022, 5, 15).getTime();
        Date d2023 = new GregorianCalendar(2023, 9, 20).getTime();
        prof.addResearchPaper(new ResearchPaper("LMS Logs and Student Performance", "Shamoi, P.", "Springer", 45, "10.1007/s12345", d2022, 18));
        prof.addResearchPaper(new ResearchPaper("Influence of Retaking a Course", "Shamoi, P., Izbassar, A.", "IEEE", 31, "10.1109/abc", d2023, 12));
        prof.addResearchPaper(new ResearchPaper("Deep Learning in Education", "Shamoi, P.", "Elsevier", 20, "10.1016/xyz", d2020, 22));
        db.saveUser(prof);

        // Lector teacher (NOT automatically a researcher)
        Teacher lector = UserFactory.createTeacher("TCH02", "tch456", "Assylzhan", "Izbassar", "a.izbassar@kbtu.kz", 200000, TeacherTitle.LECTOR);
        db.saveUser(lector);

        // Bachelor student
        Student student1 = new Student("STU01", "stu123", "Amir", "Nurlanov", "a.nurlanov@kbtu.kz");
        student1.setYearOfStudy(2);
        db.saveUser(student1);

        Student student2 = new Student("STU02", "stu456", "Zarina", "Alieva", "z.alieva@kbtu.kz");
        student2.setYearOfStudy(3);
        db.saveUser(student2);

        // PhD Graduate student
        GraduateStudent phd = UserFactory.createGraduateStudent("PHD01", "phd123", "Bekzat", "Seilov", "b.seilov@kbtu.kz", true);
        db.saveUser(phd);

        // Master student
        GraduateStudent master = UserFactory.createGraduateStudent("MST01", "mst123", "Nurgul", "Dzhaksybekova", "n.dzhaksybekova@kbtu.kz", false);
        db.saveUser(master);

        // TechSupport
        TechSupportSpecialist tech = new TechSupportSpecialist("TECH01", "tech123", "Marat", "Omarov", "m.omarov@kbtu.kz", 180000);
        db.saveUser(tech);

        // Courses
        Course oop = new Course("CS101", "Object-Oriented Programming", 6, CourseType.MAJOR, 2);
        Course ml = new Course("CS305", "Machine Learning", 6, CourseType.MAJOR, 3);
        Course eng = new Course("EN101", "Academic English", 3, CourseType.FREE_ELECTIVE, 1);
        Course ds = new Course("CS201", "Data Structures", 5, CourseType.MAJOR, 2);
        oop.addLesson(new Lesson(LessonType.LECTURE, "Room 201", "Mon 09:00-10:30"));
        oop.addLesson(new Lesson(LessonType.PRACTICE, "Lab 105", "Wed 11:00-12:30"));
        oop.addInstructorId(prof.getId());
        ml.addInstructorId(prof.getId());
        ds.addInstructorId(lector.getId());
        db.saveCourse(oop); db.saveCourse(ml); db.saveCourse(eng); db.saveCourse(ds);

        System.out.println("[Seed] Done. Users: " + db.getAllUsers().size() + ", Courses: " + db.getAllCourses().size());
    }

    // Demo methods

    static void demoAuthentication() {
        // Valid login
        User u = AuthService.login("STU01", "stu123");
        System.out.println("Logged in as: " + u);

        // Wrong password
        AuthService.login("STU01", "wrongpass");

        // Non-existent user
        AuthService.login("UNKNOWN", "pass");

        AuthService.logout();

        // Login as professor
        AuthService.login("TCH01", "tch123");
        System.out.println("Current user role: " + AuthService.getCurrentUser().getRole());
        AuthService.logout();
    }

    static void demoCourseRegistration(Database db) {
        Student s1 = (Student) db.getUserById("STU01");
        Manager mgr = (Manager) db.getUserById("MGR01");
        Course oop = db.getCourseByCode("CS101");
        Course ds = db.getCourseByCode("CS201");
        Course ml = db.getCourseByCode("CS305");
        Course eng = db.getCourseByCode("EN101");

        System.out.println("Credits before: " + s1.getTotalCredits());
        mgr.approveRegistration(s1, oop);  // 6 cr → 6 total
        mgr.approveRegistration(s1, ds);   // 5 cr → 11 total
        mgr.approveRegistration(s1, eng);  // 3 cr → 14 total

        // This should FAIL — would reach 20 but still OK
        mgr.approveRegistration(s1, ml);   // 6 cr → 20 total — OK

        // Create a new 5-credit course to exceed 21
        Course extra = new Course("CS999", "Extra Course", 5, CourseType.MINOR, 2);
        db.saveCourse(extra);
        mgr.approveRegistration(s1, extra); // 5 cr → 25 — should be DENIED

        System.out.println("Total credits: " + s1.getTotalCredits() + " / 21 max");

        // Register student2 for one course
        Student s2 = (Student) db.getUserById("STU02");
        mgr.approveRegistration(s2, oop);
        mgr.approveRegistration(s2, ml);
    }

    static void demoMarks(Database db) {
        Teacher prof = (Teacher) db.getUserById("TCH01");
        Student s1 = (Student) db.getUserById("STU01");
        Student s2 = (Student) db.getUserById("STU02");
        Course oop = db.getCourseByCode("CS101");
        Course ml = db.getCourseByCode("CS305");

        // Assign marks
        prof.putMark(oop, s1.getId(), 25.0, 22.0, 35.0); // total=82 → B+
        prof.putMark(ml, s1.getId(), 18.0, 15.0, 20.0); // total=53 → D+ (passed)
        prof.putMark(oop, s2.getId(), 28.0, 27.0, 38.0); // total=93 → A

        // Fail scenario — total < 50
        Course ds = db.getCourseByCode("CS201");
        Teacher lector = (Teacher) db.getUserById("TCH02");
        lector.putMark(ds, s1.getId(), 10.0, 8.0, 12.0); // total=30 → F (fail)
        s1.incrementFailCount(); // student failed this course

        // Recalculate GPA and view transcripts
        s1.recalculateGpa();
        s2.recalculateGpa();
        s1.viewTranscript();
        s2.viewTranscript();
    }

    static void demoResearch(Database db) {
        Teacher prof = (Teacher) db.getUserById("TCH01");
        GraduateStudent phd = (GraduateStudent) db.getUserById("PHD01");
        GraduateStudent master = (GraduateStudent) db.getUserById("MST01");

        System.out.println("Professor h-index: " + prof.calculateHIndex());
        prof.printPapers(PaperComparators.BY_CITATIONS_DESC);

        // PhD sets supervisor with sufficient h-index (professor h=3 with our data)
        try {
            phd.setResearchSupervisor(prof);
            System.out.println("PhD supervisor set successfully!");
        } catch (InvalidSupervisorException e) {
            System.out.println("[Exception] " + e.getMessage());
        }

        // Add a paper for PhD student
        Date d2024 = new GregorianCalendar(2024, 3, 10).getTime();
        ResearchPaper phdPaper = new ResearchPaper("Neural Nets for Student Performance",
                "Seilov, B., Shamoi, P.", "IEEE", 5, "10.1109/phd1", d2024, 10);
        phd.addDiplomaProject(phdPaper);
        System.out.println("PhD h-index: " + phd.calculateHIndex());

        // Try assigning supervisor with h-index < 3 (lector has 0 papers)
        Teacher lector = (Teacher) db.getUserById("TCH02");
        System.out.println("Lector h-index: " + lector.calculateHIndex());
        try {
            master.setResearchSupervisor(lector); // Should throw InvalidSupervisorException
        } catch (InvalidSupervisorException e) {
            System.out.println("[Expected Exception] " + e.getMessage());
        }

        // ResearchProject — add researcher and non-researcher
        ResearchProject project = new ResearchProject("AI in Education");
        project.addParticipant(prof);
        project.addParticipant(phd);
        System.out.println("Research project: " + project);

        try {
            project.addUser("NonResearcher String"); // Should throw NotResearcherException
        } catch (NotResearcherException e) {
            System.out.println("[Expected Exception] " + e.getMessage());
        }

        try {
            project.addUser(prof); // OK
            System.out.println("Added professor to project again (no-op).");
        } catch (NotResearcherException e) {
            System.out.println("[Unexpected] " + e.getMessage());
        }

        // ResearchService demo
        ResearchService rs = new ResearchService();
        rs.announceTopCitedResearcher();
    }

    static void demoObserver(Database db) {
        // Create a journal
        ResearchJournal journal = new ResearchJournal("KBTU Journal of Computing");
        db.addJournal(journal);

        // Users subscribe (Observer pattern)
        Student s1 = (Student) db.getUserById("STU01");
        Student s2 = (Student) db.getUserById("STU02");
        Teacher prof = (Teacher) db.getUserById("TCH01");
        s1.subscribeToJournal(journal);
        s2.subscribeToJournal(journal);
        prof.subscribeToJournal(journal);

        System.out.println("Subscribed 3 users to '" + journal.getName() + "'");

        // Publish paper — triggers notifications to all subscribers
        ResearchPaper newPaper = new ResearchPaper("Blockchain in University Systems", "Bekova, A.", "KBTU Journal", 2, "10.1234/kbtu2024", new GregorianCalendar(2024, 1, 1).getTime(), 8);
        journal.publishPaper(newPaper);

        // Unsubscribe one user and publish again
        s1.unsubscribeFromJournal(journal);
        ResearchPaper newPaper2 = new ResearchPaper("OOP Best Practices", "Izbassar, A.", "KBTU Journal", 3, "10.1234/kbtu2025", new GregorianCalendar(2025, 6, 1).getTime(), 14);
        journal.publishPaper(newPaper2); // Only s2 and prof notified now
    }

    static void demoComplaintsAndTechSupport(Database db) {
        Teacher prof = (Teacher) db.getUserById("TCH01");
        TechSupportSpecialist tech = (TechSupportSpecialist) db.getUserById("TECH01");

        // Teacher sends complaint about student
        prof.sendComplaint("STU01", UrgencyLevel.MEDIUM, "Student missed 3 consecutive labs.");
        prof.sendComplaint("STU02", UrgencyLevel.LOW, "Late submissions.");

        System.out.println("\nAll complaints:");
        db.getAllComplaints().forEach(c -> System.out.println("  " + c));

        // Teacher sends a tech support request
        Request req1 = new Request("Projector in Room 201 broken", "The projector shows distorted images since Monday.", UrgencyLevel.HIGH, prof.getId());
        Request req2 = new Request("Printer out of toner", "Lab 105 printer needs toner replacement.", UrgencyLevel.LOW, prof.getId());
        db.addRequest(req1); db.addRequest(req2);

        // TechSupport handles requests
        List<Request> pending = tech.getNewRequests();
        if (!pending.isEmpty()) {
            tech.viewRequest(pending.get(0).getRequestId());
            tech.acceptRequest(pending.get(0).getRequestId());
            if (pending.size() > 1) tech.rejectRequest(pending.get(1).getRequestId());
        }
        System.out.println("\nAll requests after handling:");
        db.getAllRequests().forEach(r -> System.out.println("  " + r));

        // Employee messaging
        Manager mgr = (Manager) db.getUserById("MGR01");
        prof.sendMessage(mgr, "Please check student Amir's registration status.");
        System.out.println("\nMessages for manager:");
        db.getMessagesForUser(mgr.getId()).forEach(m -> System.out.println("  " + m));
    }

    static void demoManagerActions(Database db) {
        Manager mgr = (Manager) db.getUserById("MGR01");
        mgr.createStatisticalReport();

        // Add and view news
        mgr.addNews(new News("Semester Registration Open", "Course registration for Spring 2025 is now open.", false));
        mgr.addNews(new News("Research Grant Awarded", "KBTU received a major research grant from Ministry of Education.", true));

        System.out.println("\nAll News (research pinned first):");
        db.getAllNews().forEach(n -> System.out.println("  " + n));

        // View students sorted different ways
        mgr.viewStudents("gpa");
        mgr.viewTeachers();

        // Rate teacher
        Student s1 = (Student) db.getUserById("STU01");
        Teacher prof = (Teacher) db.getUserById("TCH01");
        s1.rateTeacher(prof, 5);
        s1.rateTeacher(prof, 4);
        System.out.println("Average rating for " + prof.getFullName() + ": " + db.getAverageRating(prof.getId()));
    }

    static void demoSerialization(Database db) {
        System.out.println("Saving database to disk...");
        db.saveToDisk();

        // Reset singleton and reload
        Database.resetInstance();
        System.out.println("Reloading database from disk...");
        Database reloaded = Database.getInstance(); // auto-loads from file
        System.out.println("Reloaded users: " + reloaded.getAllUsers().size());
        System.out.println("Reloaded courses: " + reloaded.getAllCourses().size());
        User reloadedStudent = reloaded.getUserById("STU01");
        if (reloadedStudent instanceof Student) {
            System.out.println("Reloaded student: " + reloadedStudent);
            ((Student) reloadedStudent).viewTranscript();
        }
    }

    static void demoPatterns() {
        System.out.println("--- Pattern 1: Singleton ---");
        Database db1 = Database.getInstance();
        Database db2 = Database.getInstance();
        System.out.println("Same instance? " + (db1 == db2)); // true

        System.out.println("\n--- Pattern 2: Factory ---");
        Teacher t = UserFactory.createTeacher("TCH99", "pass", "Test", "Teacher", "t@kbtu.kz", 200000, TeacherTitle.SENIOR_LECTOR);
        System.out.println("Factory created: " + t + " | isResearcher=" + t.isResearcher());

        System.out.println("\n--- Pattern 3: Strategy (Comparators) ---");
        System.out.println("Available comparators: BY_CITATIONS_DESC, BY_DATE_DESC, BY_LENGTH_DESC, BY_TITLE");
        Teacher prof = (Teacher) Database.getInstance().getUserById("TCH01");
        if (prof != null) {
            System.out.println("Papers by date (newest first):");
            prof.printPapers(PaperComparators.BY_DATE_DESC);
        }

        System.out.println("\n--- Pattern 4: Decorator ---");
        ResearcherDecorator decorated = new ResearcherDecorator("ADMIN01", "Diana Seitkali (Admin)");
        decorated.addResearchPaper(new ResearchPaper("Admin Research Paper", "Seitkali, D.", "KBTU Review", 10, "10.9999/test", new GregorianCalendar(2023, 1, 1).getTime(), 5));
        System.out.println("Decorated admin h-index: " + decorated.calculateHIndex());
        decorated.printPapers(PaperComparators.BY_CITATIONS_ASC);

        System.out.println("\n--- Pattern 5: Observer (Journal) ---");
        System.out.println("See Demo 5 above — users notified via onNewPaperPublished()");
    }

    static void demoComparators(Database db) {
        Teacher prof = (Teacher) db.getUserById("TCH01");
        if (prof == null) return;
        System.out.println("Papers sorted by CITATIONS DESC:");
        prof.printPapers(PaperComparators.BY_CITATIONS_DESC);
        System.out.println("Papers sorted by DATE DESC (newest first):");
        prof.printPapers(PaperComparators.BY_DATE_DESC);
        System.out.println("Papers sorted by LENGTH DESC (most pages first):");
        prof.printPapers(PaperComparators.BY_LENGTH_DESC);
        System.out.println("ResearchPaper getCitation() Plain Text:");
        System.out.println("  " + prof.getResearchPapers().get(0).getCitation("Plain Text"));
        System.out.println("ResearchPaper getCitation() BibTeX:");
        System.out.println(prof.getResearchPapers().get(0).getCitation("Bibtex"));
    }
}
