# Research University System (KBTU)

A console-based university management system for KBTU, written in pure Java (no external dependencies). It models the day-to-day life of a research university — courses, marks, research papers, journals, complaints, tech support — with role-based menus, three interface languages, and persistent storage via Java serialization.

## Features

- **Authentication & registration** — login/logout with role-based menus, plus in-app registration for new users.
- **Course management** — course catalog, manager-approved registration with a 21-credit limit, course drops, lessons (lecture/practice) with rooms and schedules.
- **Marks & transcripts** — teachers assign attestation and final-exam marks; GPA is recalculated automatically and full transcripts can be printed.
- **Research** — research papers with citations, h-index calculation, supervisor assignment with validation (custom exceptions), research projects with join requests, top-cited researcher announcements, and citation export in Plain Text or BibTeX.
- **Research journals** — users subscribe to journals and get notified when new papers are published (Observer pattern).
- **Complaints & tech support** — teachers file complaints about students with urgency levels; tech support specialists accept/reject repair requests.
- **Manager tools** — statistical academic reports, news feed (research news pinned first), student/teacher listings, teacher ratings.
- **Organizations & messaging** — student organizations with join requests, direct messages, and notifications.
- **Internationalization** — full UI in English, Kazakh, and Russian (`I18n`), switchable at runtime.
- **Persistence** — the whole database is serialized to `kbtu_database.dat` with an atomic write-to-temp-then-rename strategy to survive crashes.

## Design Patterns

| Pattern | Where | Purpose |
|---|---|---|
| Singleton | `storage/Database` | Single shared in-memory database, lazily loaded from disk |
| Factory | `patterns/UserFactory` | Creates teachers, managers, graduate students, etc. |
| Decorator | `patterns/ResearcherDecorator` | Adds researcher capabilities (papers, h-index) to any `User` without changing its class; the decorated user is stored in the database |
| Observer | `patterns/ResearchJournal` + `JournalObserver` | Journal subscribers are notified on each new publication |
| Strategy | `patterns/PaperComparators` | Interchangeable sort orders for papers: by citations, date, page count, or title |

## Project Structure

```
src/project/
├── Main.java              # Entry point: seeds the DB, runs 10 feature demos, starts the CLI
├── InteractiveMenu.java   # Role-based interactive console menu
├── I18n.java              # EN / KZ / RU string table
├── models/
│   ├── actors/            # User hierarchy: Student, GraduateStudent, Teacher, Manager,
│   │                      #   Admin, TechSupportSpecialist, Employee
│   ├── enums/             # Role, School, CourseType, TeacherTitle, UrgencyLevel, ...
│   ├── errors/            # InvalidSupervisorException, NotResearcherException
│   └── others/            # Course, Mark, ResearchPaper, ResearchProject, Complaint,
│                          #   Request, News, Message, Organization, ...
├── patterns/              # UserFactory, ResearcherDecorator, ResearchJournal,
│                          #   JournalObserver, PaperComparators
├── services/              # AuthService, CourseService, MarkService, ResearchService,
│                          #   ManagerService, ComplaintService, JournalService, ...
└── storage/
    └── Database.java      # Singleton, serialized to kbtu_database.dat
```

Other directories:

- `bin/diagrams/` — UML class and use-case diagrams (PlantUML sources + rendered SVGs)
- `docs/` — generated Javadoc (open `docs/index.html` in a browser)

## Requirements

- Java 21 (JDK)
- No external libraries or build tools required

## Build & Run

From the project root:

```bash
# Compile
javac -d out $(find src -name '*.java')

# Run
java -cp out project.Main
```

Or in Eclipse: import the project and run `project.Main`.

On startup the program seeds the database (if empty), runs ten demos showcasing every subsystem, then launches the interactive menu where you can `login`, `register`, or `quit`.

## Demo Accounts

| ID | Password | Role |
|---|---|---|
| `ADMIN01` | `admin123` | Admin |
| `MGR01` | `mgr123` | Manager (Office of Registrar) |
| `TCH01` | `tch123` | Teacher (Professor, researcher) |
| `TCH02` | `tch456` | Teacher (Lector) |
| `STU01` | `stu123` | Student |
| `STU02` | `stu456` | Student |
| `PHD01` | `phd123` | Graduate student (PhD, researcher) |
| `MST01` | `mst123` | Graduate student (Master's, researcher) |
| `TECH01` | `tech123` | Tech support specialist |

## Persistence

All state lives in `kbtu_database.dat`, created next to the working directory on first save. Delete the file to reset the system to a fresh seed on the next run.
