package project;

import java.util.*;
import java.util.Comparator;
import java.util.stream.Collectors;
import project.models.actors.*;
import project.models.enums.CourseType;
import project.models.enums.LanguageType;
import project.models.enums.ManagerType;
import project.models.enums.School;
import project.models.enums.TeacherTitle;
import project.models.enums.UrgencyLevel;
import project.models.others.*;
import project.patterns.*;
import project.patterns.ResearcherDecorator;
import project.services.*;
import project.storage.Database;

/**
 * Interactive CLI for the KBTU University System.
 *
 * Multi-process sync strategy (option 1 — file polling):
 *   • handleRoleMenu() calls db.reloadFromDisk() before every command, then
 *     re-fetches the current user by ID so mutations always target the live
 *     in-map object.
 *   • Every method that writes to the DB ends with db.saveToDisk() so the
 *     next reload in any process picks up the change.
 */
public class InteractiveMenu {

    private static final Scanner sc = new Scanner(System.in);
    private static final Database db = Database.getInstance();

    // Services
    private static final AdminService         adminService         = new AdminService();
    private static final ComplaintService     complaintService     = new ComplaintService();
    private static final CourseService        courseService        = new CourseService();
    private static final JournalService       journalService       = new JournalService();
    private static final ManagerService       managerService       = new ManagerService();
    private static final MarkService          markService          = new MarkService();
    private static final MessageService       messageService       = new MessageService();
    private static final NotificationService  notificationService  = new NotificationService();
    private static final OrganizationService  organizationService  = new OrganizationService();
    private static final ProjectService       projectService       = new ProjectService();
    private static final RequestService       requestService       = new RequestService();
    private static final ResearchService      researchService      = new ResearchService();
    private static final StaffBoardService    staffBoardService    = new StaffBoardService();
    private static final TeacherRatingService teacherRatingService = new TeacherRatingService();
    private static final UserLookupService    userLookupService    = new UserLookupService();

    // ─── Entry Point ───────────────────────────────────────────────────────────

    public static void run() {
        printBanner();
        while (true) {
            if (!AuthService.isLoggedIn()) handleAuthPrompt();
            else                           handleRoleMenu();
        }
    }

    // ─── Auth ──────────────────────────────────────────────────────────────────

    private static void handleAuthPrompt() {
        db.reloadFromDisk();
        System.out.println("\n" + I18n.get("auth.prompt"));
        System.out.print("> ");
        String cmd = readLine();
        switch (cmd) {
            case "login":    doLogin();    break;
            case "register": doRegister(); break;
            case "quit": case "exit":
                System.out.println(I18n.get("auth.goodbye"));
                db.saveToDisk();
                System.exit(0);
            default:
                System.out.println(I18n.get("auth.unknown"));
        }
    }

    private static void doLogin() {
        System.out.print(I18n.get("auth.id"));       String id   = readLine();
        System.out.print(I18n.get("auth.password")); String pass = readLine();
        User user = AuthService.login(id, pass);
        if (user == null) {
            System.out.println(I18n.get("auth.login_fail"));
        } else {
            I18n.setLang(user.getLanguage());
            System.out.println(I18n.get("auth.welcome") + user.getFullName() + "!");
        }
    }

    private static void doRegister() {
        System.out.println(I18n.get("auth.register_hdr"));
        System.out.print(I18n.get("auth.id"));        String id    = readLine();
        if (db.getUserById(id) != null) { System.out.println(I18n.get("auth.id_taken")); return; }
        System.out.print(I18n.get("auth.password"));  String pass  = readLine();
        System.out.print(I18n.get("auth.firstname")); String first = readLine();
        System.out.print(I18n.get("auth.lastname"));  String last  = readLine();
        System.out.print(I18n.get("auth.email"));     String email = readLine();
        System.out.println(I18n.get("auth.role_list"));
        System.out.print("  > ");                     String role  = readLine().toUpperCase();

        User newUser;
        try {
            switch (role) {
                case "STUDENT":
                    System.out.println(I18n.get("auth.school_list")); System.out.print("  > ");
                    { Student s = new Student(id, pass, first, last, email);
                      s.setSchool(School.valueOf(readLine().toUpperCase()));
                      newUser = s; } break;
                case "GRADUATE":
                    System.out.print(I18n.get("auth.phd"));
                    String phdAns = readLine();
                    System.out.println(I18n.get("auth.school_list")); System.out.print("  > ");
                    { GraduateStudent gs = UserFactory.createGraduateStudent(id, pass, first, last, email,
                            phdAns.equalsIgnoreCase("yes") || phdAns.equalsIgnoreCase("иә") || phdAns.equalsIgnoreCase("да"));
                      gs.setSchool(School.valueOf(readLine().toUpperCase()));
                      newUser = new ResearcherDecorator(gs); } break;
                case "TEACHER":
                    System.out.println(I18n.get("auth.title_list")); System.out.print("  > ");
                    { TeacherTitle tt = TeacherTitle.valueOf(readLine().toUpperCase());
                      Teacher nt = UserFactory.createTeacher(id, pass, first, last, email, 200000, tt);
                      newUser = isResearchTitle(tt) ? new ResearcherDecorator(nt) : nt; } break;
                case "MANAGER":
                    System.out.println(I18n.get("auth.mtype_list")); System.out.print("  > ");
                    newUser = UserFactory.createManager(id, pass, first, last, email, 200000,
                            ManagerType.valueOf(readLine().toUpperCase())); break;
                case "ADMIN":
                    newUser = new Admin(id, pass, first, last, email, 300000); break;
                case "TECHSUPPORT":
                    newUser = new TechSupportSpecialist(id, pass, first, last, email, 180000); break;
                default:
                    System.out.println(I18n.get("auth.unknown_role")); return;
            }
        } catch (IllegalArgumentException e) {
            System.out.println(I18n.get("auth.invalid") + e.getMessage()); return;
        }
        db.saveUser(newUser);
        db.saveToDisk();
        System.out.println(I18n.get("auth.registered") + newUser);
    }

    // ─── Role Router ───────────────────────────────────────────────────────────

    private static void handleRoleMenu() {
        db.reloadFromDisk();

        String currentId = AuthService.getCurrentUser().getId();
        User stored = db.getUserById(currentId);
        if (stored == null) { AuthService.logout(); return; }

        ResearcherDecorator rd = (stored instanceof ResearcherDecorator) ? (ResearcherDecorator) stored : null;
        User user = (rd != null) ? rd.getWrappedUser() : stored;

        I18n.setLang(user.getLanguage());

        System.out.println("\n══════════════════════════════════");
        System.out.println("  " + user.getFullName() + "  [" + user.getRole() + "]");
        System.out.println("══════════════════════════════════");

        if      (user instanceof Admin)                 showAdminMenu((Admin) user);
        else if (user instanceof Manager)               showManagerMenu((Manager) user);
        else if (user instanceof Teacher)               showTeacherMenu((Teacher) user, rd);
        else if (user instanceof TechSupportSpecialist) showTechSupportMenu((TechSupportSpecialist) user);
        else if (user instanceof GraduateStudent)       showGradStudentMenu((GraduateStudent) user, rd);
        else if (user instanceof Student)               showStudentMenu((Student) user, rd);
        else                                            AuthService.logout();
    }

    private static boolean isResearchTitle(TeacherTitle t) {
        return t == TeacherTitle.PROFESSOR || t == TeacherTitle.MASTER || t == TeacherTitle.PHD;
    }

    private static User baseUser(User u) {
        return (u instanceof ResearcherDecorator) ? ((ResearcherDecorator) u).getWrappedUser() : u;
    }

    private static Teacher asTeacher(User u) {
        User base = baseUser(u);
        return (base instanceof Teacher) ? (Teacher) base : null;
    }

    private static Student asStudent(User u) {
        User base = baseUser(u);
        return (base instanceof Student) ? (Student) base : null;
    }

    private static ResearcherDecorator getResearcherOf(User u) {
        if (u == null) return null;
        if (u instanceof ResearcherDecorator) return (ResearcherDecorator) u;
        User fromDb = db.getUserById(u.getId());
        if (fromDb instanceof ResearcherDecorator) return (ResearcherDecorator) fromDb;
        return null;
    }

    // ─── Student Menu ──────────────────────────────────────────────────────────

    private static void showStudentMenu(Student student, ResearcherDecorator rd) {
        List<String> h = new ArrayList<>(Arrays.asList(
            I18n.get("help.courses"),  I18n.get("help.register"), I18n.get("help.drop"),
            I18n.get("help.marks"),    I18n.get("help.transcript"), I18n.get("help.teacher"),
            I18n.get("help.rate"),     I18n.get("help.news"),
            I18n.get("help.journals"), I18n.get("help.subscribe"), I18n.get("help.notifications"), I18n.get("help.messages"),
            I18n.get("help.send"),     I18n.get("help.request")
        ));
        if (rd != null) {
            h.add(I18n.get("help.papers"));        h.add(I18n.get("help.addpaper"));
            h.add(I18n.get("help.cite"));          h.add(I18n.get("help.hindex"));
            h.add(I18n.get("help.createproject")); h.add(I18n.get("help.joinproject"));
            h.add(I18n.get("help.projectreqs"));   h.add(I18n.get("help.acceptjoin")); h.add(I18n.get("help.rejectjoin"));
        } else {
            h.add(I18n.get("help.becomeresearcher"));
        }
        h.add(I18n.get("help.projects")); h.add(I18n.get("help.allpapers")); h.add(I18n.get("help.topcited"));
        h.add(I18n.get("help.createorg")); h.add(I18n.get("help.orgs")); h.add(I18n.get("help.joinorg"));
        h.add(I18n.get("help.orgmembers"));
        if (student.isOrganizationHead()) {
            h.add(I18n.get("help.orgreqs")); h.add(I18n.get("help.acceptorg")); h.add(I18n.get("help.rejectorg"));
        }
        h.add(I18n.get("help.language")); h.add(I18n.get("help.logout")); h.add(I18n.get("help.help"));
        printHelp(h.toArray(new String[0]));

        System.out.print("\nstudent> ");
        String input = readLine();
        db.reloadFromDisk();
        { User _f = db.getUserById(student.getId());
          rd = (_f instanceof ResearcherDecorator) ? (ResearcherDecorator) _f : null;
          student = (Student) baseUser(_f); }
        if (student == null) { AuthService.logout(); return; }
        String[] parts = input.split(" ", 3);
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "courses":    courseService.listAllCourses(student); break;
            case "register":
                if (parts.length < 2) { usage("register <code>"); break; }
                courseService.studentRegister(student, parts[1].toUpperCase()); break;
            case "drop":
                if (parts.length < 2) { usage("drop <code>"); break; }
                courseService.studentDrop(student, parts[1].toUpperCase()); break;
            case "marks":      courseService.studentViewMarks(student); break;
            case "transcript": student.viewTranscript(); break;
            case "teacher":    studentViewTeacher(student); break;
            case "rate":
                if (parts.length < 3) { usage("rate <teacherId> <1-5>"); break; }
                try { teacherRatingService.rateTeacher(student, parts[1], Integer.parseInt(parts[2].trim())); }
                catch (NumberFormatException e) { System.out.println(I18n.get("msg.rating_range")); } break;
            case "news":       viewNews(student); break;
            case "journals":       journalService.listJournals(); break;
            case "subscribe":
                if (parts.length < 2) { usage("subscribe <journal name>"); break; }
                journalService.subscribeJournal(student, input.substring("subscribe ".length())); break;
            case "notifications":  notificationService.viewNotifications(student.getId()); break;
            case "messages":       messageService.viewMessages(student.getId()); break;
            case "send":
                if (parts.length < 3) { usage("send <employeeId> <text>"); break; }
                messageService.sendMessage(student.getId(), parts[1], input.substring(cmd.length() + parts[1].length() + 2)); break;
            case "request":          createRequest(student.getId()); break;
            case "becomeresearcher": requestService.requestResearcherRole(student); break;
            case "allpapers":        printAllResearchPapersInteractive(student); break;
            case "topcited":         printTopCitedResearcher(); break;
            case "papers":        printPapersInteractive(rd); break;
            case "addpaper":      addResearcherPaper(student); break;
            case "cite":          recordCitation(rd); break;
            case "projects":      listProjectsInteractive(); break;
            case "createproject": createProjectInteractive(student); break;
            case "joinproject":   joinProjectInteractive(student); break;
            case "projectreqs":   projectService.listProjectJoinRequests(student.getId()); break;
            case "acceptjoin":
                if (parts.length < 2) { usage("acceptjoin <id>"); break; }
                projectService.handleProjectJoinRequest(student.getId(), parts[1], true); break;
            case "rejectjoin":
                if (parts.length < 2) { usage("rejectjoin <id>"); break; }
                projectService.handleProjectJoinRequest(student.getId(), parts[1], false); break;
            case "hindex":
                System.out.println(I18n.get("msg.hindex") + (rd != null ? rd.calculateHIndex() : 0)); break;
            case "createorg":  createOrganizationInteractive(student); break;
            case "orgs":       organizationService.listOrganizations(); break;
            case "joinorg":    joinOrganizationInteractive(student); break;
            case "orgmembers": organizationService.viewOrgMembers(student.getId()); break;
            case "orgreqs":    organizationService.listOrgJoinRequests(student.getId()); break;
            case "acceptorg":
                if (parts.length < 2) { usage("acceptorg <id>"); break; }
                organizationService.handleOrgJoinRequest(student, parts[1], true); break;
            case "rejectorg":
                if (parts.length < 2) { usage("rejectorg <id>"); break; }
                organizationService.handleOrgJoinRequest(student, parts[1], false); break;
            case "language":
                if (parts.length < 2) { usage("language <KZ|EN|RU>"); break; }
                switchLanguage(student, parts[1]); break;
            case "logout":     AuthService.logout(); System.out.println(I18n.get("msg.logged_out")); break;
            case "help":       break;
            default:           System.out.println(I18n.get("msg.unknown_cmd"));
        }
    }

    // ─── Graduate Student Menu ─────────────────────────────────────────────────

    private static void showGradStudentMenu(GraduateStudent grad, ResearcherDecorator rd) {
        printHelp(new String[]{
            I18n.get("help.courses"),    I18n.get("help.register"),   I18n.get("help.drop"),
            I18n.get("help.marks"),      I18n.get("help.transcript"), I18n.get("help.teacher"),
            I18n.get("help.rate"),       I18n.get("help.supervisor"), I18n.get("help.mysupervisor"),
            I18n.get("help.adddiploma"),  I18n.get("help.diploma"),   I18n.get("help.papers"),
            I18n.get("help.addpaper"),   I18n.get("help.cite"),      I18n.get("help.hindex"),
            I18n.get("help.projects"),   I18n.get("help.createproject"), I18n.get("help.joinproject"),
            I18n.get("help.projectreqs"), I18n.get("help.acceptjoin"),  I18n.get("help.rejectjoin"),
            I18n.get("help.news"),       I18n.get("help.journals"),
            I18n.get("help.subscribe"),  I18n.get("help.notifications"), I18n.get("help.messages"), I18n.get("help.send"),
            I18n.get("help.request"),    I18n.get("help.allpapers"),  I18n.get("help.topcited"),
            I18n.get("help.createorg"),  I18n.get("help.orgs"),       I18n.get("help.joinorg"),
            I18n.get("help.orgmembers"), I18n.get("help.orgreqs"),    I18n.get("help.acceptorg"),  I18n.get("help.rejectorg"),
            I18n.get("help.language"),   I18n.get("help.logout"),     I18n.get("help.help")
        });
        System.out.print("\ngrad> ");
        String input = readLine();
        db.reloadFromDisk();
        { User _f = db.getUserById(grad.getId());
          rd = (_f instanceof ResearcherDecorator) ? (ResearcherDecorator) _f : null;
          grad = (GraduateStudent) baseUser(_f); }
        if (grad == null) { AuthService.logout(); return; }
        String[] parts = input.split(" ", 3);
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "courses":    courseService.listAllCourses(grad); break;
            case "register":
                if (parts.length < 2) { usage("register <code>"); break; }
                courseService.studentRegister(grad, parts[1].toUpperCase()); break;
            case "drop":
                if (parts.length < 2) { usage("drop <code>"); break; }
                courseService.studentDrop(grad, parts[1].toUpperCase()); break;
            case "marks":      courseService.studentViewMarks(grad); break;
            case "transcript": grad.viewTranscript(); break;
            case "teacher":    studentViewTeacher(grad); break;
            case "rate":
                if (parts.length < 3) { usage("rate <teacherId> <1-5>"); break; }
                try { teacherRatingService.rateTeacher(grad, parts[1], Integer.parseInt(parts[2].trim())); }
                catch (NumberFormatException e) { System.out.println(I18n.get("msg.rating_range")); } break;
            case "supervisor":
                setSupervisorInteractive(grad); break;
            case "mysupervisor":
                ResearcherDecorator sup = grad.getResearchSupervisor();
                System.out.println(sup != null
                    ? "Supervisor: " + sup.getWrappedUser().getFullName() + " | H-Index: " + sup.calculateHIndex()
                    : I18n.get("msg.no_supervisor")); break;
            case "adddiploma": addDiplomaProjectInteractive(grad); break;
            case "diploma": {
                System.out.println(I18n.get("hdr.diploma") + grad.getFullName() + " ===");
                List<ResearchPaper> gradPapers = rd != null
                        ? rd.getResearchPapersList()
                        : grad.getDiplomaProjects();
                if (gradPapers.isEmpty()) { System.out.println("  (no papers)"); break; }
                for (int i = 0; i < gradPapers.size(); i++)
                    System.out.printf("  [%d] %s%n", i + 1, gradPapers.get(i).getCitation("Plain Text"));
                break;
            }
            case "papers":        printPapersInteractive(rd); break;
            case "addpaper":      addResearcherPaper(grad); break;
            case "cite":          recordCitation(rd); break;
            case "hindex":        System.out.println(I18n.get("msg.hindex") + (rd != null ? rd.calculateHIndex() : 0)); break;
            case "projects":      listProjectsInteractive(); break;
            case "createproject": createProjectInteractive(grad); break;
            case "joinproject":   joinProjectInteractive(grad); break;
            case "projectreqs":   projectService.listProjectJoinRequests(grad.getId()); break;
            case "acceptjoin":
                if (parts.length < 2) { usage("acceptjoin <id>"); break; }
                projectService.handleProjectJoinRequest(grad.getId(), parts[1], true); break;
            case "rejectjoin":
                if (parts.length < 2) { usage("rejectjoin <id>"); break; }
                projectService.handleProjectJoinRequest(grad.getId(), parts[1], false); break;
            case "news":       viewNews(grad); break;
            case "journals":       journalService.listJournals(); break;
            case "subscribe":
                if (parts.length < 2) { usage("subscribe <journal name>"); break; }
                journalService.subscribeJournal(grad, input.substring("subscribe ".length())); break;
            case "notifications":  notificationService.viewNotifications(grad.getId()); break;
            case "messages":       messageService.viewMessages(grad.getId()); break;
            case "send":
                if (parts.length < 3) { usage("send <employeeId> <text>"); break; }
                messageService.sendMessage(grad.getId(), parts[1], input.substring(cmd.length() + parts[1].length() + 2)); break;
            case "request":    createRequest(grad.getId()); break;
            case "allpapers":  printAllResearchPapersInteractive(grad); break;
            case "topcited":   printTopCitedResearcher(); break;
            case "createorg":  createOrganizationInteractive(grad); break;
            case "orgs":       organizationService.listOrganizations(); break;
            case "joinorg":    joinOrganizationInteractive(grad); break;
            case "orgmembers": organizationService.viewOrgMembers(grad.getId()); break;
            case "orgreqs":    organizationService.listOrgJoinRequests(grad.getId()); break;
            case "acceptorg":
                if (parts.length < 2) { usage("acceptorg <id>"); break; }
                organizationService.handleOrgJoinRequest(grad, parts[1], true); break;
            case "rejectorg":
                if (parts.length < 2) { usage("rejectorg <id>"); break; }
                organizationService.handleOrgJoinRequest(grad, parts[1], false); break;
            case "language":
                if (parts.length < 2) { usage("language <KZ|EN|RU>"); break; }
                switchLanguage(grad, parts[1]); break;
            case "logout":     AuthService.logout(); System.out.println(I18n.get("msg.logged_out")); break;
            case "help":       break;
            default:           System.out.println(I18n.get("msg.unknown_cmd"));
        }
    }

    // ─── Teacher Menu ──────────────────────────────────────────────────────────

    private static void showTeacherMenu(Teacher teacher, ResearcherDecorator rd) {
        List<String> h = new ArrayList<>(Arrays.asList(
            I18n.get("help.courses"),   I18n.get("help.students"),  I18n.get("help.student"),
            I18n.get("help.mark"),      I18n.get("help.complaint"), I18n.get("help.messages"),
            I18n.get("help.send")
        ));
        if (rd != null) {
            h.add(I18n.get("help.papers"));        h.add(I18n.get("help.addpaper"));
            h.add(I18n.get("help.cite"));          h.add(I18n.get("help.hindex"));
            h.add(I18n.get("help.createproject")); h.add(I18n.get("help.joinproject"));
            h.add(I18n.get("help.projectreqs"));   h.add(I18n.get("help.acceptjoin")); h.add(I18n.get("help.rejectjoin"));
        } else {
            h.add(I18n.get("help.becomeresearcher"));
        }
        h.addAll(Arrays.asList(
            I18n.get("help.projects"),  I18n.get("help.news"),      I18n.get("help.journals"),
            I18n.get("help.subscribe"), I18n.get("help.notifications"), I18n.get("help.request"),   I18n.get("help.allpapers"),
            I18n.get("help.topcited"),  I18n.get("help.board"),     I18n.get("help.postboard"),
            I18n.get("help.language"),  I18n.get("help.logout"),    I18n.get("help.help")
        ));
        printHelp(h.toArray(new String[0]));
        System.out.print("\nteacher> ");
        String input = readLine();
        db.reloadFromDisk();
        { User _f = db.getUserById(teacher.getId());
          rd = (_f instanceof ResearcherDecorator) ? (ResearcherDecorator) _f : null;
          teacher = (Teacher) baseUser(_f); }
        if (teacher == null) { AuthService.logout(); return; }
        String[] parts = input.split(" ", 3);
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "courses":    courseService.listCoursesForTeacher(teacher); break;
            case "students":   userLookupService.listStudents(parts.length >= 2 ? parts[1].toLowerCase() : "name"); break;
            case "student":
                if (parts.length < 2) { usage("student <id>"); break; }
                userLookupService.viewStudentDetails(parts[1]); break;
            case "mark":       assignMark(teacher); break;
            case "complaint":  sendComplaint(teacher); break;
            case "messages":   messageService.viewMessages(teacher.getId()); break;
            case "send":
                if (parts.length < 3) { usage("send <employeeId> <text>"); break; }
                sendMessageFromTeacher(teacher, parts[1], input.substring(cmd.length() + parts[1].length() + 2)); break;
            case "papers":        printPapersInteractive(rd); break;
            case "addpaper":      addResearcherPaper(teacher); break;
            case "cite":          recordCitation(rd); break;
            case "hindex":        System.out.println(I18n.get("msg.hindex") + (rd != null ? rd.calculateHIndex() : 0)); break;
            case "projects":      listProjectsInteractive(); break;
            case "createproject": createProjectInteractive(teacher); break;
            case "joinproject":   joinProjectInteractive(teacher); break;
            case "projectreqs":   projectService.listProjectJoinRequests(teacher.getId()); break;
            case "acceptjoin":
                if (parts.length < 2) { usage("acceptjoin <id>"); break; }
                projectService.handleProjectJoinRequest(teacher.getId(), parts[1], true); break;
            case "rejectjoin":
                if (parts.length < 2) { usage("rejectjoin <id>"); break; }
                projectService.handleProjectJoinRequest(teacher.getId(), parts[1], false); break;
            case "news":       viewNews(teacher); break;
            case "journals":       journalService.listJournals(); break;
            case "subscribe":
                if (parts.length < 2) { usage("subscribe <journal name>"); break; }
                journalService.subscribeJournal(teacher, input.substring("subscribe ".length())); break;
            case "notifications":  notificationService.viewNotifications(teacher.getId()); break;
            case "request":        createRequest(teacher.getId()); break;
            case "becomeresearcher": requestService.requestResearcherRole(teacher); break;
            case "allpapers":        printAllResearchPapersInteractive(teacher); break;
            case "board":            staffBoardService.viewStaffBoard(); break;
            case "postboard":        postToStaffBoardInteractive(teacher); break;
            case "language":
                if (parts.length < 2) { usage("language <KZ|EN|RU>"); break; }
                switchLanguage(teacher, parts[1]); break;
            case "logout":     AuthService.logout(); System.out.println(I18n.get("msg.logged_out")); break;
            case "help":       break;
            default:           System.out.println(I18n.get("msg.unknown_cmd"));
        }
    }

    // ─── Manager Menu ──────────────────────────────────────────────────────────

    private static void showManagerMenu(Manager manager) {
        printHelp(new String[]{
            I18n.get("help.students"),    I18n.get("help.teachers"),    I18n.get("help.assign"),      I18n.get("help.unassign"),
            I18n.get("help.addcourse"),   I18n.get("help.all_courses"), I18n.get("help.report"),
            I18n.get("help.news"),        I18n.get("help.addnews"),     I18n.get("help.removenews"),  I18n.get("help.createjournal"),
            I18n.get("help.pendingregs"), I18n.get("help.approvereg"),  I18n.get("help.rejectreg"),
            I18n.get("help.complaints"),  I18n.get("help.researchreqs"),I18n.get("help.approvereq"),
            I18n.get("help.rejectresreq"),I18n.get("help.messages"),    I18n.get("help.send"),
            I18n.get("help.request"),     I18n.get("help.projects"),    I18n.get("help.allpapers"),
            I18n.get("help.topcited"),    I18n.get("help.notifications"), I18n.get("help.board"), I18n.get("help.postboard"),
            I18n.get("help.language"),    I18n.get("help.logout"),        I18n.get("help.help")
        });
        System.out.print("\nmanager> ");
        String input = readLine();
        db.reloadFromDisk();
        manager = (Manager) db.getUserById(manager.getId());
        if (manager == null) { AuthService.logout(); return; }
        String[] parts = input.split(" ", 3);
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "students":
                userLookupService.listStudents(parts.length >= 2 ? parts[1] : "name"); break;
            case "teachers":   viewTeacherInteractive(); break;
            case "assign":
                if (parts.length < 3) { usage("assign <teacherId> <courseCode>"); break; }
                managerAssign(manager, parts[1], parts[2]); break;
            case "unassign":
                if (parts.length < 3) { usage("unassign <teacherId> <courseCode>"); break; }
                managerUnassign(manager, parts[1], parts[2]); break;
            case "addcourse":  managerAddCourseInteractive(); break;
            case "courses":    courseService.listAllCourses(); break;
            case "report":     managerService.createStatisticalReport(); break;
            case "news":       viewNews(manager); break;
            case "addnews":    managerAddNewsInteractive(manager); break;
            case "removenews":
                if (parts.length < 2) { usage("removenews <newsId>"); break; }
                managerService.removeNews(manager, parts[1]); break;
            case "createjournal": createJournalInteractive(); break;
            case "pendingregs": courseService.listPendingRegistrations(); break;
            case "approvereg":
                if (parts.length < 2) { usage("approvereg <id>"); break; }
                courseService.managerApproveReg(manager, parts[1]); break;
            case "rejectreg":
                if (parts.length < 2) { usage("rejectreg <id>"); break; }
                courseService.managerRejectReg(manager, parts[1]); break;
            case "complaints":
                List<Complaint> cs = complaintService.getAllComplaints();
                if (cs.isEmpty()) { System.out.println(I18n.get("msg.no_complaints")); break; }
                cs.forEach(c -> System.out.println("  " + c)); break;
            case "researchreqs":  requestService.listResearcherRequests(); break;
            case "approvereq":
                if (parts.length < 2) { usage("approvereq <reqId>"); break; }
                requestService.handleResearcherRequest(parts[1], true); break;
            case "rejectresreq":
                if (parts.length < 2) { usage("rejectresreq <reqId>"); break; }
                requestService.handleResearcherRequest(parts[1], false); break;
            case "messages":   messageService.viewMessages(manager.getId()); break;
            case "send":
                if (parts.length < 3) { usage("send <employeeId> <text>"); break; }
                messageService.sendMessage(manager.getId(), parts[1], input.substring(cmd.length() + parts[1].length() + 2)); break;
            case "request":    createRequest(manager.getId()); break;
            case "projects":   listProjectsInteractive(); break;
            case "allpapers":  printAllResearchPapersInteractive(manager); break;
            case "topcited":   printTopCitedResearcher(); break;
            case "notifications": notificationService.viewNotifications(manager.getId()); break;
            case "board":      staffBoardService.viewStaffBoard(); break;
            case "postboard":  postToStaffBoardInteractive(manager); break;
            case "language":
                if (parts.length < 2) { usage("language <KZ|EN|RU>"); break; }
                switchLanguage(manager, parts[1]); break;
            case "logout":     AuthService.logout(); System.out.println(I18n.get("msg.logged_out")); break;
            case "help":       break;
            default:           System.out.println(I18n.get("msg.unknown_cmd"));
        }
    }

    // ─── Admin Menu ────────────────────────────────────────────────────────────

    private static void showAdminMenu(Admin admin) {
        printHelp(new String[]{
            I18n.get("help.users"),     I18n.get("help.adduser"),   I18n.get("help.removeuser"),
            I18n.get("help.logs"),      I18n.get("help.news"),      I18n.get("help.messages"),
            I18n.get("help.send"),      I18n.get("help.projects"),  I18n.get("help.allpapers"),
            I18n.get("help.topcited"),  I18n.get("help.notifications"), I18n.get("help.board"), I18n.get("help.postboard"),
            I18n.get("help.save"),      I18n.get("help.language"),  I18n.get("help.logout"),    I18n.get("help.help")
        });
        System.out.print("\nadmin> ");
        String input = readLine();
        db.reloadFromDisk();
        admin = (Admin) db.getUserById(admin.getId());
        if (admin == null) { AuthService.logout(); return; }
        String[] parts = input.split(" ", 3);
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "users":
                System.out.println(I18n.get("hdr.all_users"));
                db.getAllUsers().forEach(u -> System.out.println("  " + u)); break;
            case "adduser":    doRegisterAdmin(admin); break;
            case "removeuser":
                if (parts.length < 2) { usage("removeuser <id>"); break; }
                User toRemove = db.getUserById(parts[1]);
                if (toRemove == null) { System.out.println(I18n.get("msg.user_nf") + parts[1]); break; }
                adminService.removeUser(admin, toRemove); break;
            case "logs":       adminService.viewLogFiles(); break;
            case "news":       viewNews(admin); break;
            case "messages":   messageService.viewMessages(admin.getId()); break;
            case "send":
                if (parts.length < 3) { usage("send <employeeId> <text>"); break; }
                messageService.sendMessage(admin.getId(), parts[1], input.substring(cmd.length() + parts[1].length() + 2)); break;
            case "projects":   listProjectsInteractive(); break;
            case "allpapers":  printAllResearchPapersInteractive(admin); break;
            case "topcited":   printTopCitedResearcher(); break;
            case "notifications": notificationService.viewNotifications(admin.getId()); break;
            case "board":      staffBoardService.viewStaffBoard(); break;
            case "postboard":  postToStaffBoardInteractive(admin); break;
            case "save":       db.saveToDisk(); break;
            case "language":
                if (parts.length < 2) { usage("language <KZ|EN|RU>"); break; }
                switchLanguage(admin, parts[1]); break;
            case "logout":     AuthService.logout(); System.out.println(I18n.get("msg.logged_out")); break;
            case "help":       break;
            default:           System.out.println(I18n.get("msg.unknown_cmd"));
        }
    }

    // ─── Tech Support Menu ─────────────────────────────────────────────────────

    private static void showTechSupportMenu(TechSupportSpecialist tech) {
        printHelp(new String[]{
            I18n.get("help.requests"),   I18n.get("help.allrequests"), I18n.get("help.view_req"),
            I18n.get("help.accept_req"), I18n.get("help.reject_req"),  I18n.get("help.done"),
            I18n.get("help.messages"),   I18n.get("help.send"),        I18n.get("help.projects"),
            I18n.get("help.allpapers"),  I18n.get("help.topcited"),    I18n.get("help.notifications"),
            I18n.get("help.board"),      I18n.get("help.postboard"),   I18n.get("help.language"),   I18n.get("help.logout"), I18n.get("help.help")
        });
        System.out.print("\ntechsupport> ");
        String input = readLine();
        db.reloadFromDisk();
        tech = (TechSupportSpecialist) db.getUserById(tech.getId());
        if (tech == null) { AuthService.logout(); return; }
        String[] parts = input.split(" ", 2);
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "requests":
                tech.getNewRequests().forEach(r -> System.out.println("  " + r)); break;
            case "allrequests":
                System.out.println(I18n.get("hdr.all_requests"));
                db.getAllRequests().forEach(r -> System.out.println("  " + r)); break;
            case "view":
                if (parts.length < 2) { usage("view <reqId>"); break; }
                tech.viewRequest(parts[1]);
                db.saveToDisk(); break;
            case "accept":
                if (parts.length < 2) { usage("accept <reqId>"); break; }
                tech.acceptRequest(parts[1]);
                db.saveToDisk(); break;
            case "reject":
                if (parts.length < 2) { usage("reject <reqId>"); break; }
                tech.rejectRequest(parts[1]);
                db.saveToDisk(); break;
            case "done":
                if (parts.length < 2) { usage("done <reqId>"); break; }
                tech.markAsDone(parts[1]);
                db.saveToDisk(); break;
            case "messages":   messageService.viewMessages(tech.getId()); break;
            case "send":
                if (parts.length < 2) { usage("send <employeeId> <text>"); break; }
                String[] sp = parts[1].split(" ", 2);
                if (sp.length < 2) { usage("send <employeeId> <text>"); break; }
                messageService.sendMessage(tech.getId(), sp[0], sp[1]); break;
            case "projects":   listProjectsInteractive(); break;
            case "allpapers":  printAllResearchPapersInteractive(tech); break;
            case "topcited":   printTopCitedResearcher(); break;
            case "notifications": notificationService.viewNotifications(tech.getId()); break;
            case "board":      staffBoardService.viewStaffBoard(); break;
            case "postboard":  postToStaffBoardInteractive(tech); break;
            case "language":
                if (parts.length < 2) { usage("language <KZ|EN|RU>"); break; }
                switchLanguage(tech, parts[1]); break;
            case "logout":     AuthService.logout(); System.out.println(I18n.get("msg.logged_out")); break;
            case "help":       break;
            default:           System.out.println(I18n.get("msg.unknown_cmd"));
        }
    }

    // ─── Interactive Helpers (collect input, call service) ─────────────────────

    private static void setSupervisorInteractive(GraduateStudent grad) {
        List<Teacher> teachers = db.getAllTeachers();
        if (teachers.isEmpty()) { System.out.println("[Supervisor] No teachers found."); return; }
        System.out.println("=== Available Teachers ===");
        for (Teacher t : teachers) {
            ResearcherDecorator tRd = getResearcherOf(t);
            String hStr = tRd != null
                    ? String.format("h=%.0f", tRd.calculateHIndex())
                    : "not a researcher";
            System.out.printf("  %-10s %-30s [%s]  %s%n",
                    t.getId(), t.getFullName(), t.getTitle(), hStr);
        }
        System.out.print("Enter teacher ID (or 'back'): ");
        String input = readLine();
        if (input.equalsIgnoreCase("back") || input.isEmpty()) return;
        User u = db.getUserById(input);
        ResearcherDecorator dec = getResearcherOf(u);
        if (dec == null) { System.out.println(I18n.get("msg.researcher_nf") + input); return; }
        researchService.setSupervisor(grad, dec);
    }

    private static void addDiplomaProjectInteractive(GraduateStudent grad) {
        System.out.println(I18n.get("hdr.add_diploma"));
        System.out.print(I18n.get("lbl.title"));     String title   = readLine();
        System.out.print(I18n.get("lbl.authors"));   String authors = readLine();
        System.out.print(I18n.get("lbl.journal"));   String journal = readLine();
        System.out.print(I18n.get("lbl.citations")); int    cites   = readInt();
        System.out.print(I18n.get("lbl.doi"));       String doi     = readLine();
        System.out.print(I18n.get("lbl.pages"));     int    pages   = readInt();
        ResearchPaper paper = new ResearchPaper(title, authors, journal, cites, doi, new Date(), pages);
        researchService.addDiplomaProject(grad, paper);
        System.out.println(I18n.get("msg.diploma_added") + paper.getTitle());
    }

    private static void addResearcherPaper(User user) {
        ResearcherDecorator researcher = getResearcherOf(user);
        if (researcher == null) { System.out.println("[Error] You don't have researcher status yet."); return; }
        System.out.println(I18n.get("hdr.add_paper"));
        System.out.print(I18n.get("lbl.title"));     String title   = readLine();
        System.out.print(I18n.get("lbl.authors"));   String authors = readLine();
        System.out.print(I18n.get("lbl.journal"));   String journal = readLine();
        System.out.print(I18n.get("lbl.citations")); int    cites   = readInt();
        System.out.print(I18n.get("lbl.doi"));       String doi     = readLine();
        System.out.print(I18n.get("lbl.pages"));     int    pages   = readInt();
        ResearchPaper paper = new ResearchPaper(title, authors, journal, cites, doi, new Date(), pages);

        String projectId = null;
        List<ResearchProject> projects = db.getAllProjects();
        if (!projects.isEmpty()) {
            System.out.println("=== Research Projects ===");
            projects.forEach(p -> System.out.println("  " + p));
            System.out.print("Link to project ID (or 'skip'): ");
            String pid = readLine();
            if (!pid.equalsIgnoreCase("skip") && !pid.isBlank()) projectId = pid;
        } else {
            System.out.println("[Project] No projects exist yet. Use 'createproject' to create one.");
        }

        String journalName = null;
        List<ResearchJournal> allJournals = db.getAllJournals();
        if (!allJournals.isEmpty()) {
            System.out.println("=== System Journals ===");
            allJournals.forEach(j -> System.out.println("  " + j.getName() + "  (" + j.getPapers().size() + " papers)"));
            System.out.print("Publish to a journal? (enter name or 'skip'): ");
            String jname = readLine().trim();
            if (!jname.equalsIgnoreCase("skip") && !jname.isBlank()) journalName = jname;
        }

        researchService.addResearcherPaper(user, paper, projectId, journalName);
    }

    private static void recordCitation(ResearcherDecorator researcher) {
        if (researcher == null || researcher.getResearchPapersList().isEmpty()) {
            System.out.println(I18n.get("msg.no_papers")); return;
        }
        researcher.printPapers(PaperComparators.BY_CITATIONS_DESC);
        System.out.print(I18n.get("lbl.doi"));
        String doi = readLine();
        researchService.recordCitation(researcher, doi);
    }

    private static void printPapersInteractive(ResearcherDecorator researcher) {
        if (researcher == null || researcher.getResearchPapersList().isEmpty()) {
            System.out.println(I18n.get("msg.no_papers")); return;
        }
        researcher.printPapers(pickPaperComparator());
    }

    private static void listProjectsInteractive() {
        List<ResearchProject> projects = db.getAllProjects();
        if (projects.isEmpty()) { System.out.println("[Projects] No research projects yet."); return; }
        while (true) {
            projectService.listProjects();
            System.out.println("Enter project ID to view its papers, or 'back':");
            System.out.print("  > ");
            String input = readLine();
            if (input.equalsIgnoreCase("back") || input.isEmpty()) return;
            ResearchProject proj = db.getProjectById(input.trim());
            if (proj == null) { System.out.println("[Project] Not found: " + input); continue; }
            System.out.println("\n  === Papers in project: " + proj.getTopic() + " ===");
            if (proj.getPublishedPapers().isEmpty()) {
                System.out.println("  No papers linked to this project yet.");
            } else {
                for (ResearchPaper rp : proj.getPublishedPapers()) {
                    System.out.println("  " + rp.getCitation("Plain Text"));
                    System.out.println("    Citations: " + rp.getCitations() + "  Pages: " + rp.getPages());
                }
            }
        }
    }

    private static void createProjectInteractive(User user) {
        ResearcherDecorator rd = getResearcherOf(user);
        if (rd == null) { System.out.println("[Project] You must be a researcher to create a project."); return; }
        System.out.print("Project topic: ");
        String topic = readLine();
        projectService.createProject(user, topic);
    }

    private static void joinProjectInteractive(User user) {
        ResearcherDecorator rd = getResearcherOf(user);
        if (rd == null) { System.out.println("[Project] You must be a researcher to join a project."); return; }
        List<ResearchProject> projects = db.getAllProjects();
        if (projects.isEmpty()) { System.out.println("[Project] No projects exist yet."); return; }
        projectService.listProjects();
        System.out.print("Enter project ID to send a join request (or 'back'): ");
        String pid = readLine();
        if (pid.equalsIgnoreCase("back") || pid.isBlank()) return;
        projectService.joinProject(user, pid);
    }

    private static void createOrganizationInteractive(Student student) {
        System.out.print("Organization name: ");
        String name = readLine();
        organizationService.createOrganization(student, name);
    }

    private static void joinOrganizationInteractive(Student student) {
        List<Organization> orgs = db.getAllOrganizations();
        if (orgs.isEmpty()) { System.out.println("[Info] No organizations exist yet."); return; }
        System.out.println("=== Organizations ===");
        for (int i = 0; i < orgs.size(); i++) {
            Organization o = orgs.get(i);
            User lead = db.getUserById(o.getLeadId());
            System.out.printf("  [%d] %s  (Lead: %s, Members: %d)%n",
                    i + 1, o.getName(), lead != null ? lead.getFullName() : o.getLeadId(), o.getMemberIds().size());
        }
        System.out.print("Select organization number: ");
        int idx;
        try { idx = Integer.parseInt(readLine().trim()) - 1; }
        catch (NumberFormatException e) { System.out.println("[Error] Enter a number."); return; }
        if (idx < 0 || idx >= orgs.size()) { System.out.println("[Error] Out of range."); return; }
        organizationService.joinOrganization(student, orgs.get(idx).getId());
    }

    private static void postToStaffBoardInteractive(User author) {
        System.out.print("Title: ");   String title = readLine();
        if (title.isBlank()) { System.out.println("[Board] Title cannot be empty."); return; }
        System.out.print("Message: "); String body  = readLine();
        if (body.isBlank())  { System.out.println("[Board] Message cannot be empty."); return; }
        staffBoardService.postToStaffBoard(author.getId(), author.getFullName(), title, body);
    }

    private static void createJournalInteractive() {
        System.out.print("Journal name: ");
        String name = readLine().trim();
        journalService.createJournal(name);
    }

    private static void createRequest(String requesterId) {
        System.out.println(I18n.get("hdr.tech_request"));
        System.out.print(I18n.get("lbl.title"));       String title  = readLine();
        System.out.print(I18n.get("lbl.description")); String desc   = readLine();
        System.out.println(I18n.get("lbl.urgency"));
        System.out.print("  > ");                      String urgStr = readLine().toUpperCase();
        try {
            requestService.createRequest(requesterId, title, desc, UrgencyLevel.valueOf(urgStr));
        } catch (IllegalArgumentException e) {
            System.out.println(I18n.get("msg.invalid_urgency"));
        }
    }

    // ─── Teacher Actions ───────────────────────────────────────────────────────

    private static void assignMark(Teacher teacher) {
        System.out.println(I18n.get("hdr.assign_mark"));
        courseService.listCoursesForTeacher(teacher);
        System.out.print(I18n.get("lbl.course_code")); String code = readLine().toUpperCase();
        Course course = db.getCourseByCode(code);
        if (course == null) { System.out.println(I18n.get("msg.course_nf") + code); return; }
        System.out.print(I18n.get("lbl.student_id")); String studentId = readLine();
        Student s = asStudent(db.getUserById(studentId));
        if (s == null) { System.out.println(I18n.get("msg.student_nf") + studentId); return; }
        System.out.print(I18n.get("lbl.att1"));       double a1 = readDouble();
        System.out.print(I18n.get("lbl.att2"));       double a2 = readDouble();
        System.out.print(I18n.get("lbl.final_exam")); double fe = readDouble();
        markService.assignMark(teacher, course, studentId, a1, a2, fe);
        System.out.println(I18n.get("msg.mark_assigned") + (a1 + a2 + fe));
    }

    private static void sendComplaint(Teacher teacher) {
        System.out.println(I18n.get("hdr.complaint"));
        userLookupService.listStudents("name");
        System.out.print(I18n.get("lbl.student_id")); String sid    = readLine();
        System.out.println(I18n.get("lbl.urgency"));
        System.out.print("  > ");                      String urgStr = readLine().toUpperCase();
        System.out.print(I18n.get("lbl.reason"));      String reason = readLine();
        try {
            complaintService.fileComplaint(teacher, sid, UrgencyLevel.valueOf(urgStr), reason);
        } catch (IllegalArgumentException e) {
            System.out.println(I18n.get("msg.invalid_urgency"));
        }
    }

    private static void sendMessageFromTeacher(Teacher teacher, String receiverId, String text) {
        messageService.sendMessageToEmployee(teacher.getId(), receiverId, text);
    }

    // ─── Manager Actions ───────────────────────────────────────────────────────

    private static void viewTeacherInteractive() {
        List<Teacher> teachers = db.getAllTeachers();
        if (teachers.isEmpty()) { System.out.println("[Info] No teachers found."); return; }
        System.out.println("=== Teachers ===");
        for (int i = 0; i < teachers.size(); i++) {
            Teacher t = teachers.get(i);
            System.out.printf("  [%d] %s (%s) — %s%n", i + 1, t.getFullName(), t.getId(), t.getTitle());
        }
        System.out.print("Select number (or 0 to cancel): ");
        int idx;
        try { idx = Integer.parseInt(readLine().trim()) - 1; }
        catch (NumberFormatException e) { System.out.println("[Error] Enter a number."); return; }
        if (idx < 0 || idx >= teachers.size()) return;
        managerService.viewTeacherDetails(teachers.get(idx).getId());
    }

    private static void managerAssign(Manager manager, String teacherId, String code) {
        managerService.assignCourseById(manager, teacherId, code);
    }

    private static void managerUnassign(Manager manager, String teacherId, String code) {
        managerService.unassignCourseById(manager, teacherId, code);
    }

    private static void managerAddCourseInteractive() {
        System.out.println(I18n.get("hdr.add_course"));
        System.out.print(I18n.get("lbl.course_code_eg")); String code = readLine().toUpperCase();
        System.out.print(I18n.get("lbl.title"));           String name    = readLine();
        System.out.print(I18n.get("lbl.credits"));         int    credits = readInt();

        CourseType courseType;
        while (true) {
            System.out.println(I18n.get("lbl.course_type")); System.out.print("  > ");
            try { courseType = CourseType.valueOf(readLine().toUpperCase()); break; }
            catch (IllegalArgumentException e) { System.out.println(I18n.get("msg.invalid_type")); }
        }

        System.out.print(I18n.get("lbl.target_year")); int year = readInt();

        School school;
        while (true) {
            System.out.println(I18n.get("auth.school_list")); System.out.print("  > ");
            try { school = School.valueOf(readLine().toUpperCase()); break; }
            catch (IllegalArgumentException e) { System.out.println(I18n.get("msg.invalid_school")); }
        }

        courseService.addCourse(code, name, credits, courseType, year, school);
    }

    private static void managerAddNewsInteractive(Manager manager) {
        System.out.print(I18n.get("lbl.title"));       String title   = readLine();
        System.out.print(I18n.get("lbl.content"));     String content = readLine();
        System.out.print(I18n.get("lbl.is_research")); String ans     = readLine();
        boolean isResearch = ans.equalsIgnoreCase("yes") || ans.equalsIgnoreCase("иә") || ans.equalsIgnoreCase("да");
        managerService.addNews(manager, new News(title, content, isResearch));
    }

    private static void doRegisterAdmin(Admin admin) {
        System.out.println(I18n.get("hdr.add_user"));
        System.out.print(I18n.get("auth.id"));        String id    = readLine();
        if (db.getUserById(id) != null) { System.out.println(I18n.get("msg.id_taken")); return; }
        System.out.print(I18n.get("auth.password"));  String pass  = readLine();
        System.out.print(I18n.get("auth.firstname")); String first = readLine();
        System.out.print(I18n.get("auth.lastname"));  String last  = readLine();
        System.out.print(I18n.get("auth.email"));     String email = readLine();
        System.out.println(I18n.get("auth.role_list"));
        System.out.print("  > ");                     String role  = readLine().toUpperCase();

        User newUser;
        try {
            switch (role) {
                case "STUDENT":
                    System.out.println(I18n.get("auth.school_list")); System.out.print("  > ");
                    { Student s = new Student(id, pass, first, last, email);
                      s.setSchool(School.valueOf(readLine().toUpperCase()));
                      newUser = s; } break;
                case "GRADUATE":
                    System.out.print(I18n.get("lbl.phd"));
                    String ans = readLine();
                    System.out.println(I18n.get("auth.school_list")); System.out.print("  > ");
                    { GraduateStudent gs = UserFactory.createGraduateStudent(id, pass, first, last, email,
                            ans.equalsIgnoreCase("yes") || ans.equalsIgnoreCase("иә") || ans.equalsIgnoreCase("да"));
                      gs.setSchool(School.valueOf(readLine().toUpperCase()));
                      newUser = new ResearcherDecorator(gs); } break;
                case "TEACHER":
                    System.out.println(I18n.get("auth.title_list")); System.out.print("  > ");
                    { TeacherTitle tt2 = TeacherTitle.valueOf(readLine().toUpperCase());
                      Teacher nt2 = UserFactory.createTeacher(id, pass, first, last, email, 200000, tt2);
                      newUser = isResearchTitle(tt2) ? new ResearcherDecorator(nt2) : nt2; } break;
                case "MANAGER":
                    System.out.println(I18n.get("auth.mtype_list")); System.out.print("  > ");
                    newUser = UserFactory.createManager(id, pass, first, last, email, 200000,
                            ManagerType.valueOf(readLine().toUpperCase())); break;
                case "ADMIN":
                    newUser = new Admin(id, pass, first, last, email, 300000); break;
                case "TECHSUPPORT":
                    newUser = new TechSupportSpecialist(id, pass, first, last, email, 180000); break;
                default:
                    System.out.println(I18n.get("auth.unknown_role")); return;
            }
        } catch (IllegalArgumentException e) {
            System.out.println(I18n.get("msg.invalid_input") + e.getMessage()); return;
        }
        adminService.addUser(admin, newUser);
    }

    // ─── Researcher Interactive Methods ────────────────────────────────────────

    private static Comparator<ResearchPaper> pickPaperComparator() {
        System.out.println("Sort by: [1] citations  [2] date published  [3] article length (pages)");
        System.out.print("  > ");
        switch (readLine().trim()) {
            case "2": return PaperComparators.BY_DATE_DESC;
            case "3": return PaperComparators.BY_LENGTH_DESC;
            default:  return PaperComparators.BY_CITATIONS_DESC;
        }
    }

    private static void printTopCitedResearcher() {
        System.out.println("Top cited by: [1] school  [2] year");
        System.out.print("  > ");
        String choice = readLine().trim();
        if (choice.equals("1")) {
            System.out.println("Schools: SITE BS ISE SEPI KMA SAM SG SMST SNSS");
            System.out.print("  Enter school: ");
            try {
                researchService.printTopCitedBySchool(School.valueOf(readLine().trim().toUpperCase()));
            } catch (IllegalArgumentException e) { System.out.println(I18n.get("msg.invalid_school")); }
        } else {
            System.out.print("  Enter year (e.g. 2023): ");
            try {
                researchService.printTopCitedByYear(Integer.parseInt(readLine().trim()));
            } catch (NumberFormatException e) { System.out.println("[Error] Invalid year."); }
        }
    }

    private static void printAllResearchPapersInteractive(User viewer) {
        Comparator<ResearchPaper> comp = pickPaperComparator();
        List<ResearchPaper> all = new ArrayList<>();
        for (ResearcherDecorator r : db.getAllResearchers())
            all.addAll(r.getResearchPapersList());
        all.sort(comp);
        if (all.isEmpty()) { System.out.println(I18n.get("msg.no_papers")); return; }

        while (true) {
            System.out.println("=== ALL RESEARCH PAPERS ===");
            for (int i = 0; i < all.size(); i++)
                System.out.printf("  [%d] %s%n", i, all.get(i).getCitation("Plain Text"));
            System.out.println("Enter paper number to view citations, or 'back':");
            System.out.print("  > ");
            String input = readLine();
            if (input.equalsIgnoreCase("back") || input.isEmpty()) return;
            int idx;
            try { idx = Integer.parseInt(input.trim()); }
            catch (NumberFormatException e) { System.out.println("[Error] Invalid number."); continue; }
            if (idx < 0 || idx >= all.size()) { System.out.println("[Error] Out of range."); continue; }

            ResearchPaper p = all.get(idx);
            System.out.println("\n  Title:    " + p.getTitle());
            System.out.println("  Authors:  " + p.getAuthors());
            System.out.println("  Journal:  " + p.getJournal());
            System.out.println("  Citations:" + p.getCitations());
            List<Citation> cits = p.getPaperCitations();
            if (cits.isEmpty()) {
                System.out.println("  No text citations yet.");
            } else {
                System.out.println("  Text Citations:");
                cits.forEach(c -> System.out.println("    " + c));
            }

            System.out.print("Add a text citation (or 'skip'): ");
            String citText = readLine();
            if (!citText.equalsIgnoreCase("skip") && !citText.isBlank()) {
                researchService.addTextCitation(viewer, p, citText);
                System.out.println("[Citation] Added.");
            }
        }
    }

    // ─── Shared UI Helpers ─────────────────────────────────────────────────────

    private static void studentViewTeacher(Student student) {
        List<String> ids = student.getRegisteredCourseIds();
        if (ids == null || ids.isEmpty()) { System.out.println(I18n.get("msg.no_courses_reg")); return; }
        System.out.println("=== Your Courses ===");
        for (int i = 0; i < ids.size(); i++) {
            Course c = db.getCourseByCode(ids.get(i));
            String name = c != null ? c.getCourseName() : "(unavailable)";
            System.out.printf("  [%d] %s — %s%n", i + 1, ids.get(i), name);
        }
        System.out.print("Select course number: ");
        int idx;
        try { idx = Integer.parseInt(readLine().trim()) - 1; }
        catch (NumberFormatException e) { System.out.println("[Error] Enter a number."); return; }
        if (idx < 0 || idx >= ids.size()) { System.out.println("[Error] Out of range."); return; }
        Course course = db.getCourseByCode(ids.get(idx));
        if (course == null) { System.out.println("[Error] Course data unavailable."); return; }
        if (course.getInstructorIds().isEmpty()) { System.out.println(I18n.get("msg.no_instructor")); return; }
        List<String> tids = course.getInstructorIds();
        if (tids.size() == 1) {
            userLookupService.viewTeacherDetails(tids.get(0));
        } else {
            System.out.println("=== Instructors for " + course.getCourseName() + " ===");
            for (int i = 0; i < tids.size(); i++) {
                User u = db.getUserById(tids.get(i));
                String tname = u != null ? u.getFullName() : tids.get(i);
                System.out.printf("  [%d] %s%n", i + 1, tname);
            }
            System.out.print("Select instructor number: ");
            int tidx;
            try { tidx = Integer.parseInt(readLine().trim()) - 1; }
            catch (NumberFormatException e) { System.out.println("[Error] Enter a number."); return; }
            if (tidx < 0 || tidx >= tids.size()) { System.out.println("[Error] Out of range."); return; }
            userLookupService.viewTeacherDetails(tids.get(tidx));
        }
    }

    private static void viewNews(User viewer) {
        while (true) {
            db.reloadFromDisk();
            List<News> newsList = db.getAllNews();
            if (newsList.isEmpty()) { System.out.println(I18n.get("msg.no_news")); return; }
            System.out.println(I18n.get("hdr.news"));
            for (News n : newsList) {
                System.out.printf("  [%s] [%s] %s%n",
                        n.getNewsId(), n.isResearchNews() ? "RESEARCH" : "GENERAL", n.getTitle());
                System.out.println("      " + n.getContent());
                System.out.println("      Comments: " + n.getComments().size());
            }
            System.out.println(I18n.get("news.prompt"));
            System.out.print("> ");
            String input = readLine();
            if (input.equalsIgnoreCase("back") || input.isEmpty()) return;

            db.reloadFromDisk();
            final String newsId = input;
            News target = db.getAllNews().stream()
                    .filter(n -> n.getNewsId().equals(newsId))
                    .findFirst().orElse(null);
            if (target == null) { System.out.println(I18n.get("msg.news_nf")); continue; }

            System.out.println("\n  --- '" + target.getTitle() + "' ---");
            if (target.getComments().isEmpty()) {
                System.out.println("  " + I18n.get("news.no_comments"));
            } else {
                target.getComments().forEach(c -> System.out.println("    " + c));
            }
            System.out.print("\n" + I18n.get("news.add_comment"));
            String comment = readLine();
            if (!comment.isBlank()) {
                managerService.addCommentToNews(target.getNewsId(), viewer.getFullName(), comment);
                System.out.println(I18n.get("msg.comment_added"));
            }
        }
    }

    private static void switchLanguage(User user, String lang) {
        try {
            LanguageType lt = LanguageType.valueOf(lang.toUpperCase());
            AuthService.switchLanguage(user, lt);
            I18n.setLang(lt);
            System.out.println(I18n.get("msg.lang_set") + lt);
        } catch (IllegalArgumentException e) {
            System.out.println(I18n.get("msg.invalid_lang"));
        }
    }

    // ─── UI Helpers ────────────────────────────────────────────────────────────

    private static void printBanner() {
        System.out.println("\n╔═══════════════════════════════════════════════════════╗");
        System.out.println("║        KBTU INTERACTIVE UNIVERSITY SYSTEM             ║");
        System.out.println("║  Seeded accounts (id / password):                     ║");
        System.out.println("║    Admin     : ADMIN01 / admin123                     ║");
        System.out.println("║    Manager   : MGR01   / mgr123                       ║");
        System.out.println("║    Professor : TCH01   / tch123                       ║");
        System.out.println("║    Lector    : TCH02   / tch456                       ║");
        System.out.println("║    Student 1 : STU01   / stu123                       ║");
        System.out.println("║    Student 2 : STU02   / stu456                       ║");
        System.out.println("║    PhD       : PHD01   / phd123                       ║");
        System.out.println("║    Master    : MST01   / mst123                       ║");
        System.out.println("║    TechSupp  : TECH01  / tech123                      ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝");
    }

    private static void printHelp(String[] commands) {
        System.out.println("\n" + I18n.get("hdr.commands"));
        for (String c : commands) System.out.println("  " + c);
    }

    private static void usage(String example) {
        System.out.println(I18n.get("msg.usage") + example);
    }

    private static String readLine() {
        try { return sc.nextLine().trim(); } catch (NoSuchElementException e) { return ""; }
    }

    private static int readInt() {
        try { return Integer.parseInt(sc.nextLine().trim()); }
        catch (Exception e) { System.out.println(I18n.get("msg.invalid_int")); return 0; }
    }

    private static double readDouble() {
        try { return Double.parseDouble(sc.nextLine().trim()); }
        catch (Exception e) { System.out.println(I18n.get("msg.invalid_double")); return 0.0; }
    }
}
