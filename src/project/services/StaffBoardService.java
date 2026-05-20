package project.services;

import java.util.List;
import project.models.others.StaffBulletin;
import project.storage.Database;

/** Manages the employee-only staff bulletin board. */
public class StaffBoardService {

    /** Prints all bulletin board posts in reverse-chronological order. */
    public void viewStaffBoard() {
        Database db = Database.getInstance();
        List<StaffBulletin> board = db.getAllStaffBulletins();
        if (board.isEmpty()) { System.out.println("[Board] No posts yet."); return; }
        System.out.println("=== STAFF BULLETIN BOARD ===");
        for (int i = board.size() - 1; i >= 0; i--) {
            StaffBulletin b = board.get(i);
            System.out.println("  " + b);
            System.out.println("    " + b.getBody());
        }
    }

    /**
     * Creates and saves a new bulletin board post.
     *
     * @param authorId   the poster's user ID
     * @param authorName the poster's display name
     * @param title      headline of the post
     * @param body       full text of the post
     */
    public void postToStaffBoard(String authorId, String authorName, String title, String body) {
        Database db = Database.getInstance();
        db.addStaffBulletin(new StaffBulletin(authorId, authorName, title, body));
        db.saveToDisk();
        System.out.println("[Board] Posted.");
    }
}
