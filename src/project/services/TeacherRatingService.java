package project.services;

import project.models.actors.Student;
import project.models.actors.User;
import project.patterns.ResearcherDecorator;
import project.storage.Database;

public class TeacherRatingService {

    public void rateTeacher(Student student, String teacherId, int rating) {
        Database db = Database.getInstance();
        if (rating < 1 || rating > 5) { System.out.println("[Rating] Rating must be between 1 and 5."); return; }
        User u = db.getUserById(teacherId);
        User base = (u instanceof ResearcherDecorator) ? ((ResearcherDecorator) u).getWrappedUser() : u;
        if (!(base instanceof project.models.actors.Teacher)) {
            System.out.println("[Rating] Teacher not found: " + teacherId); return;
        }
        db.addTeacherRating(teacherId, rating);
        student.log("Rated teacher " + teacherId + " with " + rating);
        System.out.printf("[Rated] %s — new average: %.2f / 5.0%n", u.getFullName(), db.getAverageRating(teacherId));
        db.saveToDisk();
    }
}
