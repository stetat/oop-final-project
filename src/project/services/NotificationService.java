package project.services;

import java.util.List;
import project.models.others.Notification;
import project.storage.Database;

/** Retrieves and displays notifications for a user, marking them as read on view. */
public class NotificationService {

    /**
     * Prints all notifications for the given user (newest first) and marks unread ones as read.
     *
     * @param userId the recipient's ID
     */
    public void viewNotifications(String userId) {
        Database db = Database.getInstance();
        List<Notification> notifs = db.getNotificationsForUser(userId);
        if (notifs.isEmpty()) { System.out.println("[Notifications] No notifications."); return; }
        System.out.println("=== NOTIFICATIONS ===");
        for (int i = notifs.size() - 1; i >= 0; i--)
            System.out.println("  " + notifs.get(i));
        boolean anyUnread = notifs.stream().anyMatch(n -> !n.isRead());
        if (anyUnread) {
            notifs.forEach(Notification::markRead);
            db.saveToDisk();
        }
    }
}
