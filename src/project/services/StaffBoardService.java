package project.services;

import java.util.List;
import project.models.others.StaffBulletin;
import project.storage.Database;

public class StaffBoardService {

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

    public void postToStaffBoard(String authorId, String authorName, String title, String body) {
        Database db = Database.getInstance();
        db.addStaffBulletin(new StaffBulletin(authorId, authorName, title, body));
        db.saveToDisk();
        System.out.println("[Board] Posted.");
    }
}
