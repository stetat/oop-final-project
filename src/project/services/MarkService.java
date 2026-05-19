package project.services;

import project.models.actors.Student;
import project.models.actors.Teacher;
import project.models.actors.User;
import project.models.others.Course;
import project.models.others.Mark;
import project.patterns.ResearcherDecorator;
import project.storage.Database;

public class MarkService {

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
