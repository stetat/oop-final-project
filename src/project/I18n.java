package project;

import java.util.*;

import project.models.enums.LanguageType;

/**
 * In-code string table for KZ / EN / RU.
 * Call I18n.setLang() when a user switches language; all subsequent
 * I18n.get() calls return strings in the new language.
 * Falls back to EN if a key is missing in the active language.
 */
public class I18n {

    private static LanguageType lang = LanguageType.EN;
    private static final Map<LanguageType, Map<String, String>> T = new EnumMap<>(LanguageType.class);

    static {
        Map<String, String> en = new LinkedHashMap<>();
        Map<String, String> kz = new LinkedHashMap<>();
        Map<String, String> ru = new LinkedHashMap<>();
        T.put(LanguageType.EN, en);
        T.put(LanguageType.KZ, kz);
        T.put(LanguageType.RU, ru);

        d(en,kz,ru, "auth.prompt",
                "Commands: login | register | quit",
                "Командалар: login | register | quit",
                "Команды: login | register | quit");
        d(en,kz,ru, "auth.unknown",
                "Unknown command. Use: login, register, quit",
                "Белгісіз команда. Пайдаланыңыз: login, register, quit",
                "Неизвестная команда. Используйте: login, register, quit");
        d(en,kz,ru, "auth.id",           "  ID: ",             "  ID: ",                   "  ID: ");
        d(en,kz,ru, "auth.password",     "  Password: ",       "  Құпия сөз: ",             "  Пароль: ");
        d(en,kz,ru, "auth.login_fail",
                "  Login failed. Check your ID and password.",
                "  Кіру сәтсіз. ID мен құпия сөзді тексеріңіз.",
                "  Вход не выполнен. Проверьте ID и пароль.");
        d(en,kz,ru, "auth.welcome",      "  Welcome, ",        "  Қош келдіңіз, ",          "  Добро пожаловать, ");
        d(en,kz,ru, "auth.register_hdr", "  --- REGISTER ---", "  --- ТІРКЕЛУ ---",          "  --- РЕГИСТРАЦИЯ ---");
        d(en,kz,ru, "auth.id_taken",     "  ID already taken.","  Бұл ID бос емес.",         "  Этот ID уже занят.");
        d(en,kz,ru, "auth.firstname",    "  First name: ",     "  Аты: ",                   "  Имя: ");
        d(en,kz,ru, "auth.lastname",     "  Last name: ",      "  Тегі: ",                  "  Фамилия: ");
        d(en,kz,ru, "auth.email",        "  Email: ",          "  Электрондық пошта: ",      "  Электронная почта: ");
        d(en,kz,ru, "auth.role_list",
                "  Role: STUDENT | GRADUATE | TEACHER | MANAGER | ADMIN | TECHSUPPORT",
                "  Рөл: STUDENT | GRADUATE | TEACHER | MANAGER | ADMIN | TECHSUPPORT",
                "  Роль: STUDENT | GRADUATE | TEACHER | MANAGER | ADMIN | TECHSUPPORT");
        d(en,kz,ru, "auth.phd",
                "  PhD student? (yes/no): ",
                "  PhD студенті? (иә/жоқ): ",
                "  Студент PhD? (да/нет): ");
        d(en,kz,ru, "auth.title_list",
                "  Title: TUTOR | LECTOR | SENIOR_LECTOR | PROFESSOR | MASTER | PHD",
                "  Лауазым: TUTOR | LECTOR | SENIOR_LECTOR | PROFESSOR | MASTER | PHD",
                "  Звание: TUTOR | LECTOR | SENIOR_LECTOR | PROFESSOR | MASTER | PHD");
        d(en,kz,ru, "auth.mtype_list",
                "  Manager type: OR | DEPARTMENTS",
                "  Менеджер түрі: OR | DEPARTMENTS",
                "  Тип менеджера: OR | DEPARTMENTS");
        d(en,kz,ru, "auth.school_list",
                "  School: SITE | BS | ISE | SEPI | KMA | SAM | SG | SMST | SNSS",
                "  Мектеп: SITE | BS | ISE | SEPI | KMA | SAM | SG | SMST | SNSS",
                "  Школа: SITE | BS | ISE | SEPI | KMA | SAM | SG | SMST | SNSS");
        d(en,kz,ru, "auth.unknown_role", "  Unknown role.",    "  Белгісіз рөл.",            "  Неизвестная роль.");
        d(en,kz,ru, "auth.invalid",      "  Invalid input: ",  "  Қате енгізу: ",            "  Неверный ввод: ");
        d(en,kz,ru, "auth.registered",   "  Registered: ",     "  Тіркелді: ",               "  Зарегистрирован: ");
        d(en,kz,ru, "auth.goodbye",
                "Saving and exiting...",
                "Сақталуда және шығуда...",
                "Сохранение и выход...");

        d(en,kz,ru, "hdr.commands",      "  --- Commands ---",
                "  --- Командалар ---",
                "  --- Команды ---");
        d(en,kz,ru, "hdr.courses",       "=== AVAILABLE COURSES ===",
                "=== ҚОЛЖЕТІМДІ КУРСТАР ===",
                "=== ДОСТУПНЫЕ КУРСЫ ===");
        d(en,kz,ru, "hdr.courses_mine",  "=== ALL COURSES (yours marked with *) ===",
                "=== БАРЛЫҚ КУРСТАР (* — сіздікі) ===",
                "=== ВСЕ КУРСЫ (* — ваши) ===");
        d(en,kz,ru, "hdr.students",      "=== STUDENTS ===",
                "=== СТУДЕНТТЕР ===",
                "=== СТУДЕНТЫ ===");
        d(en,kz,ru, "hdr.marks",         "=== MARKS: ",
                "=== БАҒАЛАР: ",
                "=== ОЦЕНКИ: ");
        d(en,kz,ru, "hdr.news",          "=== NEWS (Research pinned first) ===",
                "=== ЖАҢАЛЫҚТАР (Зерттеу жаңалықтары жоғарыда) ===",
                "=== НОВОСТИ (исследовательские в приоритете) ===");
        d(en,kz,ru, "hdr.journals",      "=== RESEARCH JOURNALS ===",
                "=== ЗЕРТТЕУ ЖУРНАЛДАРЫ ===",
                "=== НАУЧНЫЕ ЖУРНАЛЫ ===");
        d(en,kz,ru, "hdr.messages",      "=== MESSAGES ===",
                "=== ХАБАРЛАМАЛАР ===",
                "=== СООБЩЕНИЯ ===");
        d(en,kz,ru, "hdr.all_requests",  "=== ALL REQUESTS ===",
                "=== БАРЛЫҚ ӨТІНІМДЕР ===",
                "=== ВСЕ ЗАЯВКИ ===");
        d(en,kz,ru, "hdr.all_users",     "=== ALL USERS ===",
                "=== БАРЛЫҚ ПАЙДАЛАНУШЫЛАР ===",
                "=== ВСЕ ПОЛЬЗОВАТЕЛИ ===");
        d(en,kz,ru, "hdr.diploma",       "=== Diploma Projects ===",
                "=== Диплом жобалары ===",
                "=== Дипломные проекты ===");
        d(en,kz,ru, "hdr.assign_mark",   "  --- ASSIGN MARK ---",
                "  --- БАҒА ҚОЮ ---",
                "  --- ВЫСТАВИТЬ ОЦЕНКУ ---");
        d(en,kz,ru, "hdr.complaint",     "  --- SEND COMPLAINT ---",
                "  --- ШАҒЫМ ЖІБЕРУ ---",
                "  --- ОТПРАВИТЬ ЖАЛОБУ ---");
        d(en,kz,ru, "hdr.add_paper",     "  --- ADD RESEARCH PAPER ---",
                "  --- ЗЕРТТЕУ МАҚАЛАСЫН ҚОСУ ---",
                "  --- ДОБАВИТЬ НАУЧНУЮ СТАТЬЮ ---");
        d(en,kz,ru, "hdr.add_diploma",   "  --- ADD DIPLOMA / RESEARCH PAPER ---",
                "  --- ДИПЛОМ / МАҚАЛАНЫ ҚОСУ ---",
                "  --- ДОБАВИТЬ ДИПЛОМ / СТАТЬЮ ---");
        d(en,kz,ru, "hdr.add_course",    "  --- ADD COURSE ---",
                "  --- КУРС ҚОСУ ---",
                "  --- ДОБАВИТЬ КУРС ---");
        d(en,kz,ru, "hdr.add_user",      "  --- ADD USER (Admin) ---",
                "  --- ПАЙДАЛАНУШЫ ҚОСУ (Әкімші) ---",
                "  --- ДОБАВИТЬ ПОЛЬЗОВАТЕЛЯ (Админ) ---");
        d(en,kz,ru, "hdr.tech_request",  "  --- TECH SUPPORT REQUEST ---",
                "  --- ТЕХНИКАЛЫҚ ҚОЛДАУ ӨТІНІМІ ---",
                "  --- ЗАЯВКА В ТЕХПОДДЕРЖКУ ---");

        d(en,kz,ru, "lbl.course_code",   "  Course code: ",       "  Курс коды: ",             "  Код курса: ");
        d(en,kz,ru, "lbl.course_code_eg","  Course code (e.g. CS201): ","  Курс коды (мыс. CS201): ","  Код курса (напр. CS201): ");
        d(en,kz,ru, "lbl.student_id",    "  Student ID: ",        "  Студент ID: ",            "  ID студента: ");
        d(en,kz,ru, "lbl.teacher_id",    "  Teacher ID: ",        "  Оқытушы ID: ",            "  ID преподавателя: ");
        d(en,kz,ru, "lbl.att1",          "  Attestation 1 (0-30): ","  1-аттестация (0-30): ",  "  Аттестация 1 (0-30): ");
        d(en,kz,ru, "lbl.att2",          "  Attestation 2 (0-30): ","  2-аттестация (0-30): ",  "  Аттестация 2 (0-30): ");
        d(en,kz,ru, "lbl.final_exam",    "  Final exam (0-40): ", "  Қорытынды емтихан (0-40): ","  Финальный экзамен (0-40): ");
        d(en,kz,ru, "lbl.urgency",       "  Urgency: LOW | MEDIUM | HIGH",
                "  Шұғылдылық: LOW | MEDIUM | HIGH",
                "  Срочность: LOW | MEDIUM | HIGH");
        d(en,kz,ru, "lbl.reason",        "  Reason: ",            "  Себебі: ",                "  Причина: ");
        d(en,kz,ru, "lbl.title",         "  Title: ",             "  Тақырыбы: ",              "  Название: ");
        d(en,kz,ru, "lbl.content",       "  Content: ",           "  Мазмұны: ",               "  Содержание: ");
        d(en,kz,ru, "lbl.description",   "  Description: ",       "  Сипаттамасы: ",           "  Описание: ");
        d(en,kz,ru, "lbl.is_research",   "  Is research news? (yes/no): ",
                "  Зерттеу жаңалығы ма? (иә/жоқ): ",
                "  Научная новость? (да/нет): ");
        d(en,kz,ru, "lbl.journal",       "  Journal: ",           "  Журнал: ",                "  Журнал: ");
        d(en,kz,ru, "lbl.authors",       "  Authors: ",           "  Авторлар: ",              "  Авторы: ");
        d(en,kz,ru, "lbl.citations",     "  Citations: ",         "  Сілтемелер: ",            "  Цитирования: ");
        d(en,kz,ru, "lbl.doi",           "  DOI: ",               "  DOI: ",                   "  DOI: ");
        d(en,kz,ru, "lbl.pages",         "  Pages: ",             "  Беттер: ",                "  Страниц: ");
        d(en,kz,ru, "lbl.target_year",   "  Target year: ",       "  Оқу жылы: ",              "  Целевой год: ");
        d(en,kz,ru, "lbl.credits",       "  Credits: ",           "  Кредиттер: ",             "  Кредиты: ");
        d(en,kz,ru, "lbl.course_type",   "  Type: MAJOR | MINOR | FREE_ELECTIVE",
                "  Түрі: MAJOR | MINOR | FREE_ELECTIVE",
                "  Тип: MAJOR | MINOR | FREE_ELECTIVE");
        d(en,kz,ru, "lbl.school",
                "  School: ",
                "  Мектеп: ",
                "  Школа: ");
        d(en,kz,ru, "msg.invalid_school",
                "Invalid school. Use: SITE BS ISE SEPI KMA SAM SG SMST SNSS",
                "Қате мектеп. Пайдаланыңыз: SITE BS ISE SEPI KMA SAM SG SMST SNSS",
                "Неверная школа. Используйте: SITE BS ISE SEPI KMA SAM SG SMST SNSS");
        d(en,kz,ru, "lbl.phd",           "  PhD? (yes/no): ",     "  PhD? (иә/жоқ): ",         "  PhD? (да/нет): ");
        d(en,kz,ru, "lbl.instructor",    "           Instructor(s): ",
                "           Оқытушы(лар): ",
                "           Преподаватель(и): ");

        d(en,kz,ru, "msg.unknown_cmd",
                "Unknown command. Type 'help' to see commands.",
                "Белгісіз команда. Командалар тізімі үшін 'help' теріңіз.",
                "Неизвестная команда. Введите 'help' для списка команд.");
        d(en,kz,ru, "msg.course_nf",     "Course not found: ",    "Курс табылмады: ",          "Курс не найден: ");
        d(en,kz,ru, "msg.student_nf",    "Student not found: ",   "Студент табылмады: ",       "Студент не найден: ");
        d(en,kz,ru, "msg.teacher_nf",    "Teacher not found: ",   "Оқытушы табылмады: ",       "Преподаватель не найден: ");
        d(en,kz,ru, "msg.user_nf",       "User not found: ",      "Пайдаланушы табылмады: ",   "Пользователь не найден: ");
        d(en,kz,ru, "msg.employee_nf",   "Employee not found: ",  "Қызметкер табылмады: ",     "Сотрудник не найден: ");
        d(en,kz,ru, "msg.researcher_nf", "Researcher not found: ","Зерттеуші табылмады: ",     "Исследователь не найден: ");
        d(en,kz,ru, "msg.no_instructor", "No instructor assigned.",
                "Оқытушы тағайындалмаған.",
                "Преподаватель не назначен.");
        d(en,kz,ru, "msg.dropped",       "Dropped ",              "Тасталды: ",                "Отчислен: ");
        d(en,kz,ru, "msg.not_registered","Not registered for ",   "Тіркелмеген: ",             "Не зарегистрирован на: ");
        d(en,kz,ru, "msg.no_courses",    "  No courses in the system.",
                "  Жүйеде курстар жоқ.",
                "  В системе нет курсов.");
        d(en,kz,ru, "msg.no_courses_reg","  No courses registered.",
                "  Тіркелген курстар жоқ.",
                "  Нет зарегистрированных курсов.");
        d(en,kz,ru, "msg.no_papers",     "No papers yet.",        "Мақалалар жоқ.",            "Статей нет.");
        d(en,kz,ru, "msg.no_news",       "No news available.",    "Жаңалықтар жоқ.",           "Нет новостей.");
        d(en,kz,ru, "msg.news_nf",       "News not found.",       "Жаңалық табылмады.",        "Новость не найдена.");
        d(en,kz,ru, "news.prompt",
                "Enter news ID to view/add comments, or 'back':",
                "Пікір қосу үшін ID енгізіңіз, немесе 'back':",
                "Введите ID новости для комментариев или 'back':");
        d(en,kz,ru, "news.no_comments",  "No comments yet.",      "Пікірлер жоқ.",             "Комментариев нет.");
        d(en,kz,ru, "news.add_comment",
                "Add a comment (press Enter to skip): ",
                "Пікір қалдырыңыз (өту үшін Enter): ",
                "Оставьте комментарий (Enter — пропустить): ");
        d(en,kz,ru, "msg.no_journals",   "No journals in the system.",
                "Жүйеде журналдар жоқ.",
                "В системе нет журналов.");
        d(en,kz,ru, "msg.no_messages",   "No messages.",          "Хабарламалар жоқ.",         "Нет сообщений.");
        d(en,kz,ru, "msg.no_complaints", "No complaints.",        "Шағымдар жоқ.",             "Жалоб нет.");
        d(en,kz,ru, "msg.no_supervisor", "No supervisor assigned.",
                "Жетекші тағайындалмаған.",
                "Научный руководитель не назначен.");
        d(en,kz,ru, "msg.mark_assigned", "  Mark assigned. Total=",
                "  Баға қойылды. Жалпы=",
                "  Оценка выставлена. Итого=");
        d(en,kz,ru, "msg.comment_added", "Comment added.",        "Пікір қосылды.",            "Комментарий добавлен.");
        d(en,kz,ru, "msg.subscribed",    "Subscribed to: ",       "Жазылды: ",                 "Подписан на: ");
        d(en,kz,ru, "msg.journal_nf",    "Journal not found: '",  "Журнал табылмады: '",       "Журнал не найден: '");
        d(en,kz,ru, "msg.avail_journals","Available journals:",   "Қол жетімді журналдар:",    "Доступные журналы:");
        d(en,kz,ru, "msg.course_exists", "Course code already exists.",
                "Бұл курс коды бар.",
                "Такой код курса уже существует.");
        d(en,kz,ru, "msg.course_added",  "Course added: ",        "Курс қосылды: ",            "Курс добавлен: ");
        d(en,kz,ru, "msg.req_submitted", "[Request submitted] ID: ",
                "[Өтінім жіберілді] ID: ",
                "[Заявка подана] ID: ");
        d(en,kz,ru, "msg.lang_set",      "Language set to: ",     "Тіл орнатылды: ",           "Язык установлен: ");
        d(en,kz,ru, "msg.invalid_lang",  "Invalid language. Use: KZ, EN, RU",
                "Қате тіл. Пайдаланыңыз: KZ, EN, RU",
                "Неверный язык. Используйте: KZ, EN, RU");
        d(en,kz,ru, "msg.invalid_urgency","Invalid urgency level.",
                "Қате шұғылдылық деңгейі.",
                "Неверный уровень срочности.");
        d(en,kz,ru, "msg.invalid_type",  "Invalid course type.",  "Қате курс түрі.",           "Неверный тип курса.");
        d(en,kz,ru, "msg.invalid_int",   "  (invalid number, defaulting to 0)",
                "  (қате сан, 0 қолданылады)",
                "  (неверное число, используется 0)");
        d(en,kz,ru, "msg.invalid_double","  (invalid number, defaulting to 0.0)",
                "  (қате сан, 0.0 қолданылады)",
                "  (неверное число, используется 0.0)");
        d(en,kz,ru, "msg.hindex",        "H-index: ",             "H-индекс: ",                "H-индекс: ");
        d(en,kz,ru, "msg.avg_rating",    "Average rating for ",   "Орташа рейтинг: ",          "Средний рейтинг для ");
        d(en,kz,ru, "msg.diploma_added", "Diploma project added: ","Диплом жобасы қосылды: ",  "Дипломный проект добавлен: ");
        d(en,kz,ru, "msg.logged_out",    "Logged out.",           "Шықтыңыз.",                 "Вы вышли.");
        d(en,kz,ru, "msg.rating_range",  "Rating must be a number 1-5.",
                "Баға 1-5 аралығындағы сан болуы тиіс.",
                "Рейтинг должен быть числом от 1 до 5.");
        d(en,kz,ru, "msg.id_taken",      "ID already taken.",     "Бұл ID бос емес.",          "Этот ID уже занят.");
        d(en,kz,ru, "msg.invalid_input", "Invalid input: ",       "Қате енгізу: ",             "Неверный ввод: ");
        d(en,kz,ru, "msg.send_ok",       "[Message sent] → ",     "[Хабарлама жіберілді] → ",  "[Сообщение отправлено] → ");
        d(en,kz,ru, "msg.usage",         "Usage: ",               "Қолданыс: ",                "Использование: ");

        d(en,kz,ru, "help.courses",
                "courses          - list all available courses",
                "courses          - барлық қол жетімді курстар",
                "courses          - список доступных курсов");
        d(en,kz,ru, "help.register",
                "register <code>  - register for a course",
                "register <код>   - курсқа тіркелу",
                "register <код>   - записаться на курс");
        d(en,kz,ru, "help.drop",
                "drop <code>      - drop a course",
                "drop <код>       - курстан шығу",
                "drop <код>       - отчислиться с курса");
        d(en,kz,ru, "help.marks",
                "marks            - view your marks",
                "marks            - бағаларды қарау",
                "marks            - просмотр оценок");
        d(en,kz,ru, "help.transcript",
                "transcript       - view your full transcript",
                "transcript       - транскриптті қарау",
                "transcript       - просмотр транскрипта");
        d(en,kz,ru, "help.teacher",
                "teacher          - view teacher info for your courses",
                "teacher          - оқытушы туралы ақпарат",
                "teacher          - информация о преподавателе");
        d(en,kz,ru, "help.rate",
                "rate <id> <1-5>  - rate a teacher",
                "rate <id> <1-5>  - оқытушыны бағалау",
                "rate <id> <1-5>  - оценить преподавателя");
        d(en,kz,ru, "help.news",
                "news             - view university news",
                "news             - университет жаңалықтары",
                "news             - новости университета");
        d(en,kz,ru, "help.comment",
                "comment <newsId> - add a comment to a news item",
                "comment <newsId> - жаңалыққа пікір қосу",
                "comment <newsId> - добавить комментарий к новости");
        d(en,kz,ru, "help.journals",
                "journals         - list research journals",
                "journals         - зерттеу журналдары",
                "journals         - научные журналы");
        d(en,kz,ru, "help.subscribe",
                "subscribe <name> - subscribe to a journal",
                "subscribe <ат>   - журналға жазылу",
                "subscribe <ном>  - подписаться на журнал");
        d(en,kz,ru, "help.messages",
                "messages         - view messages sent to you",
                "messages         - хабарламаларды қарау",
                "messages         - просмотр сообщений");
        d(en,kz,ru, "help.send",
                "send <id> <text> - send a message to an employee",
                "send <id> <мәтін> - хабарлама жіберу",
                "send <id> <текст> - отправить сообщение");
        d(en,kz,ru, "help.request",
                "request          - create a tech-support request",
                "request          - техникалық қолдауға өтінім",
                "request          - заявка в техподдержку");
        d(en,kz,ru, "help.allpapers",
                "allpapers        - view all research papers (sorted)",
                "allpapers        - барлық ғылыми мақалаларды қарау",
                "allpapers        - все научные статьи (с сортировкой)");
        d(en,kz,ru, "help.topcited",
                "topcited         - top cited researcher by school or year",
                "topcited         - мектеп/жыл бойынша үздік зерттеуші",
                "topcited         - лучший исследователь по школе или году");
        d(en,kz,ru, "help.becomeresearcher",
                "becomeresearcher - request researcher status from manager",
                "becomeresearcher - менеджерге зерттеуші мәртебесін сұрау",
                "becomeresearcher - запросить статус исследователя у менеджера");
        d(en,kz,ru, "help.projects",
                "projects         - list all research projects",
                "projects         - барлық зерттеу жобаларын қарау",
                "projects         - список всех исследовательских проектов");
        d(en,kz,ru, "help.createproject",
                "createproject    - create a new research project",
                "createproject    - жаңа зерттеу жобасын жасау",
                "createproject    - создать новый исследовательский проект");
        d(en,kz,ru, "help.joinproject",
                "joinproject      - send a join request for a research project",
                "joinproject      - жобаға қосылу сұрауын жіберу",
                "joinproject      - отправить запрос на вступление в проект");
        d(en,kz,ru, "help.projectreqs",
                "projectreqs      - view join requests for your projects",
                "projectreqs      - сіздің жобаңызға қосылу өтінімдері",
                "projectreqs      - заявки на вступление в ваши проекты");
        d(en,kz,ru, "help.acceptjoin",
                "acceptjoin <id>  - accept a project join request",
                "acceptjoin <id>  - жобаға қосылу өтінімін мақұлдау",
                "acceptjoin <id>  - принять запрос на вступление в проект");
        d(en,kz,ru, "help.rejectjoin",
                "rejectjoin <id>  - reject a project join request",
                "rejectjoin <id>  - жобаға қосылу өтінімін қабылдамау",
                "rejectjoin <id>  - отклонить запрос на вступление в проект");
        d(en,kz,ru, "help.createorg",
                "createorg        - create a student organization",
                "createorg        - студенттік ұйым құру",
                "createorg        - создать студенческую организацию");
        d(en,kz,ru, "help.orgs",
                "orgs             - view all organizations",
                "orgs             - барлық ұйымдарды көру",
                "orgs             - просмотреть все организации");
        d(en,kz,ru, "help.joinorg",
                "joinorg          - send a join request to an organization",
                "joinorg          - ұйымға қосылу сұрауын жіберу",
                "joinorg          - отправить запрос на вступление в организацию");
        d(en,kz,ru, "help.orgreqs",
                "orgreqs          - view join requests for your organization",
                "orgreqs          - ұйымыңызға кіру сұраулары",
                "orgreqs          - заявки на вступление в вашу организацию");
        d(en,kz,ru, "help.acceptorg",
                "acceptorg <id>   - accept an org join request",
                "acceptorg <id>   - ұйымға қосылу өтінімін мақұлдау",
                "acceptorg <id>   - принять запрос на вступление в организацию");
        d(en,kz,ru, "help.rejectorg",
                "rejectorg <id>   - reject an org join request",
                "rejectorg <id>   - ұйымға қосылу өтінімін қабылдамау",
                "rejectorg <id>   - отклонить запрос на вступление в организацию");
        d(en,kz,ru, "help.orgmembers",
                "orgmembers       - view members of your organization",
                "orgmembers       - ұйым мүшелерін көру",
                "orgmembers       - просмотреть членов организации");
        d(en,kz,ru, "help.pendingregs",
                "pendingregs      - list pending course registration requests (with IDs)",
                "pendingregs      - күтілімдегі курсқа тіркелу өтінімдері",
                "pendingregs      - список заявок на запись курсов");
        d(en,kz,ru, "help.approvereg",
                "approvereg <id>  - approve a course registration request",
                "approvereg <id>  - курсқа тіркелу өтінімін мақұлдау",
                "approvereg <id>  - одобрить заявку на запись курса");
        d(en,kz,ru, "help.rejectreg",
                "rejectreg <id>   - reject a course registration request",
                "rejectreg <id>   - курсқа тіркелу өтінімін қабылдамау",
                "rejectreg <id>   - отклонить заявку на запись курса");
        d(en,kz,ru, "help.researchreqs",
                "researchreqs     - list pending researcher role requests",
                "researchreqs     - зерттеуші өтінімдерін қарау",
                "researchreqs     - список заявок на статус исследователя");
        d(en,kz,ru, "help.approvereq",
                "approvereq <id>  - approve a researcher request",
                "approvereq <id>  - зерттеуші өтінімін мақұлдау",
                "approvereq <id>  - одобрить заявку на исследователя");
        d(en,kz,ru, "help.rejectresreq",
                "rejectresreq <id>- reject a researcher request",
                "rejectresreq <id>- зерттеуші өтінімін қабылдамау",
                "rejectresreq <id>- отклонить заявку на исследователя");
        d(en,kz,ru, "help.language",
                "language <KZ|EN|RU> - switch language",
                "language <KZ|EN|RU> - тілді ауыстыру",
                "language <KZ|EN|RU> - сменить язык");
        d(en,kz,ru, "help.logout",
                "logout           - logout",
                "logout           - шығу",
                "logout           - выход");
        d(en,kz,ru, "help.help",
                "help             - show this menu",
                "help             - командалар тізімі",
                "help             - список команд");

        d(en,kz,ru, "help.supervisor",
                "supervisor       - choose research supervisor from list",
                "supervisor       - ғылыми жетекші тізімнен таңдау",
                "supervisor       - выбрать науч. руководителя из списка");
        d(en,kz,ru, "help.mysupervisor",
                "mysupervisor     - view current supervisor",
                "mysupervisor     - жетекшіні қарау",
                "mysupervisor     - мой руководитель");
        d(en,kz,ru, "help.adddiploma",
                "adddiploma       - add a diploma/research paper",
                "adddiploma       - диплом жобасын қосу",
                "adddiploma       - добавить дипломный проект");
        d(en,kz,ru, "help.diploma",
                "diploma          - view diploma projects",
                "diploma          - диплом жобаларын қарау",
                "diploma          - дипломные проекты");
        d(en,kz,ru, "help.papers",
                "papers           - view all your research papers",
                "papers           - зерттеу мақалаларын қарау",
                "papers           - мои научные статьи");
        d(en,kz,ru, "help.hindex",
                "hindex           - calculate your h-index",
                "hindex           - h-индексті есептеу",
                "hindex           - вычислить h-индекс");

        d(en,kz,ru, "help.mark",
                "mark             - assign a mark to a student",
                "mark             - студентке баға қою",
                "mark             - выставить оценку студенту");
        d(en,kz,ru, "help.complaint",
                "complaint        - send a complaint about a student to the dean",
                "complaint        - декан атына шағым жіберу",
                "complaint        - жалоба декану на студента");
        d(en,kz,ru, "help.addpaper",
                "addpaper         - add a research paper",
                "addpaper         - зерттеу мақаласын қосу",
                "addpaper         - добавить научную статью");
        d(en,kz,ru, "help.cite",
                "cite             - record a new citation for one of your papers",
                "cite             - мақалаға сілтеме қосу",
                "cite             - добавить цитирование к статье");
        d(en,kz,ru, "help.student",
                "student <id>     - view student details",
                "student <id>     - студент туралы ақпарат",
                "student <id>     - информация о студенте");
        d(en,kz,ru, "help.students",
                "students [gpa|name|year] - list students sorted by field (default: name)",
                "students [gpa|name|year] - студенттер тізімі",
                "students [gpa|name|year] - список студентов");
        d(en,kz,ru, "help.teachers",
                "teachers                 - list all teachers",
                "teachers         - оқытушылар тізімі",
                "teachers         - список преподавателей");

        d(en,kz,ru, "help.approve",
                "approve <stuId> <code>   - approve student course registration",
                "approve <stuId> <код>    - тіркелуді мақұлдау",
                "approve <stuId> <код>    - одобрить запись на курс");
        d(en,kz,ru, "help.reject_reg",
                "reject <stuId> <code>    - reject student course registration",
                "reject <stuId> <код>     - тіркелуді қабылдамау",
                "reject <stuId> <код>     - отклонить запись на курс");
        d(en,kz,ru, "help.assign",
                "assign <tchId> <code>    - assign teacher to a course",
                "assign <tchId> <код>     - оқытушыны тағайындау",
                "assign <tchId> <код>     - назначить преподавателя");
        d(en,kz,ru, "help.unassign",
                "unassign <tchId> <code>  - unassign teacher from a course",
                "unassign <tchId> <код>   - оқытушыны алып тастау",
                "unassign <tchId> <код>   - снять преподавателя");
        d(en,kz,ru, "help.addcourse",
                "addcourse                - add a new course",
                "addcourse        - жаңа курс қосу",
                "addcourse        - добавить курс");
        d(en,kz,ru, "help.all_courses",
                "courses                  - list all courses",
                "courses          - барлық курстарды қарау",
                "courses          - все курсы");
        d(en,kz,ru, "help.report",
                "report                   - generate academic performance report",
                "report           - академиялық есеп",
                "report           - академический отчёт");
        d(en,kz,ru, "help.addnews",
                "addnews                  - add a news item",
                "addnews          - жаңалық қосу",
                "addnews          - добавить новость");
        d(en,kz,ru, "help.removenews",
                "removenews <newsId>      - remove a news item",
                "removenews <id>  - жаңалықты жою",
                "removenews <id>  - удалить новость");
        d(en,kz,ru, "help.createjournal",
                "createjournal    - create a new research journal",
                "createjournal    - жаңа журнал жасау",
                "createjournal    - создать новый журнал");
        d(en,kz,ru, "help.complaints",
                "complaints               - view all complaints",
                "complaints       - шағымдарды қарау",
                "complaints       - просмотр жалоб");

        d(en,kz,ru, "help.users",
                "users                    - list all users",
                "users            - пайдаланушылар тізімі",
                "users            - список пользователей");
        d(en,kz,ru, "help.adduser",
                "adduser                  - add a new user",
                "adduser          - жаңа пайдаланушы қосу",
                "adduser          - добавить пользователя");
        d(en,kz,ru, "help.removeuser",
                "removeuser <id>          - remove a user by ID",
                "removeuser <id>  - пайдаланушыны жою",
                "removeuser <id>  - удалить пользователя");
        d(en,kz,ru, "help.logs",
                "logs                     - view all user activity logs",
                "logs             - жүйе журналдарын қарау",
                "logs             - журналы активности");
        d(en,kz,ru, "help.save",
                "save                     - save database to disk",
                "save             - дерекқорды сақтау",
                "save             - сохранить базу данных");
        d(en,kz,ru, "help.notifications",
                "notifications    - view your journal notifications",
                "notifications    - журнал хабарламаларын қарау",
                "notifications    - уведомления о новых статьях");
        d(en,kz,ru, "help.board",
                "board            - read staff bulletin board",
                "board            - қызметкерлер хабарлама тақтасы",
                "board            - доска объявлений для сотрудников");
        d(en,kz,ru, "help.postboard",
                "postboard        - post a message to the staff board",
                "postboard        - хабарлама жіберу",
                "postboard        - опубликовать сообщение на доске");

        d(en,kz,ru, "help.requests",
                "requests         - view all pending (VIEWED) requests",
                "requests         - жаңа өтінімдерді қарау",
                "requests         - новые заявки");
        d(en,kz,ru, "help.allrequests",
                "allrequests      - view all requests with status",
                "allrequests      - барлық өтінімдер",
                "allrequests      - все заявки");
        d(en,kz,ru, "help.view_req",
                "view <reqId>     - view a specific request",
                "view <id>        - өтінімді қарау",
                "view <id>        - просмотр заявки");
        d(en,kz,ru, "help.accept_req",
                "accept <reqId>   - accept a request",
                "accept <id>      - өтінімді қабылдау",
                "accept <id>      - принять заявку");
        d(en,kz,ru, "help.reject_req",
                "reject <reqId>   - reject a request",
                "reject <id>      - өтінімді қабылдамау",
                "reject <id>      - отклонить заявку");
        d(en,kz,ru, "help.done",
                "done <reqId>     - mark a request as DONE",
                "done <id>        - орындалды деп белгілеу",
                "done <id>        - отметить как выполнено");
    }

    /**
     * Returns the localized string for {@code key} in the active language,
     * falling back to English if the key is missing.
     *
     * @param key the message key (e.g. {@code "auth.welcome"})
     * @return the translated string, or {@code "[key]"} if not found anywhere
     */
    public static String get(String key) {
        Map<String, String> active = T.getOrDefault(lang, T.get(LanguageType.EN));
        String val = active.get(key);
        if (val == null) val = T.get(LanguageType.EN).get(key);
        return val != null ? val : "[" + key + "]";
    }

    /**
     * Changes the active UI language for all subsequent {@link #get} calls.
     *
     * @param l the language to switch to
     */
    public static void setLang(LanguageType l) { lang = l; }

    /** Returns the currently active UI language. */
    public static LanguageType getLang()        { return lang; }

    /** Inserts one translated key-value triple into the EN, KZ, and RU maps. */
    private static void d(Map<String,String> en, Map<String,String> kz, Map<String,String> ru,
                           String key, String enVal, String kzVal, String ruVal) {
        en.put(key, enVal);
        kz.put(key, kzVal);
        ru.put(key, ruVal);
    }
}
