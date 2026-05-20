package project.services;

import project.models.actors.Student;
import project.models.actors.User;
import project.patterns.ResearcherDecorator;
import project.storage.Database;

/** Allows students to submit ratings for their teachers. */
public class TeacherRatingService {

    /**
     * Records a 1–5 star rating from a student for a teacher and prints the updated average.
     *
     * @param student   the student submitting the rating
     * @param teacherId the teacher's ID
     * @param rating    an integer from 1 to 5 inclusive
     */
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
