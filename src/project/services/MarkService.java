package project.services;

import project.models.actors.Student;
import project.models.actors.Teacher;
import project.models.actors.User;
import project.models.others.Course;
import project.models.others.Mark;
import project.patterns.ResearcherDecorator;
import project.storage.Database;

/** Handles mark assignment and the downstream effects on student GPA and fail count. */
public class MarkService {

    /**
     * Creates a mark and records it in the course. If the total is below 50 the student's
     * fail counter is incremented and an alert is printed if the limit is exceeded.
     * GPA is recalculated and the student is saved to disk afterwards.
     *
     * @param teacher    the teacher assigning the mark
     * @param course     the course the mark belongs to
     * @param studentId  the student's ID
     * @param first      attestation 1 score (0–30)
     * @param second     attestation 2 score (0–30)
     * @param finalExam  final exam score (0–40)
     */
    public void assignMark(Teacher teacher, Course course, String studentId,
                           double first, double second, double finalExam) {
        Database db = Database.getInstance();
        Mark mark = new Mark(studentId, course.getCourseCode(), first, second, finalExam);
        course.assignMark(studentId, mark);
        teacher.log("Assigned mark to student " + studentId + " in " + course.getCourseCode());
        System.out.println("[Mark] " + mark);

        User su = db.getUserById(studentId);
        User base = (su instanceof ResearcherDecorator) ? ((ResearcherDecorator) su).getWrappedUser() : su;
        if (base instanceof Student) {
            Student s = (Student) base;
            if (mark.getTotal() < 50.0) {
                s.incrementFailCount();
                System.out.println("[Fail] " + s.getFullName() + " failed. Total fails: " + s.getFailCount());
                if (s.hasExceededFailLimit())
                    System.out.println("[ALERT] " + s.getFullName() + " has exceeded the maximum of 3 failures!");
            }
            s.recalculateGpa();
            db.saveUser(s);
        }
        db.saveToDisk();
    }
}
