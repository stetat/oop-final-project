package project.services;

import java.util.List;
import project.models.actors.Employee;
import project.models.actors.User;
import project.models.others.Message;
import project.patterns.ResearcherDecorator;
import project.storage.Database;

/** Handles sending and retrieving direct messages between users. */
public class MessageService {

    /**
     * Prints all messages addressed to the given user.
     *
     * @param userId the recipient's ID
     */
    public void viewMessages(String userId) {
        Database db = Database.getInstance();
        List<Message> msgs = db.getMessagesForUser(userId);
        if (msgs.isEmpty()) { System.out.println("[Messages] No messages."); return; }
        System.out.println("=== MESSAGES ===");
        for (Message m : msgs) {
            User sender = db.getUserById(m.getSenderId());
            String from = sender != null ? sender.getFullName() : m.getSenderId();
            System.out.println("  [" + m.getSentAt() + "] From: " + from);
            System.out.println("    " + m.getContent());
        }
    }

    /**
     * Sends a message from one user to another. The receiver must exist in the database.
     *
     * @param senderId   the sender's ID
     * @param receiverId the recipient's ID
     * @param text       the message body
     */
    public void sendMessage(String senderId, String receiverId, String text) {
        Database db = Database.getInstance();
        User recv = db.getUserById(receiverId);
        if (recv == null) { System.out.println("[Message] User not found: " + receiverId); return; }
        db.addMessage(new Message(senderId, receiverId, text.trim()));
        db.saveToDisk();
        System.out.println("[Message] Sent to " + recv.getFullName() + ": " + text.trim());
    }

    /**
     * Sends a message to a recipient who must be an {@link project.models.actors.Employee}.
     * Prints an error if the recipient is a non-employee (e.g. a student).
     *
     * @param senderId   the sender's ID
     * @param receiverId the intended employee recipient's ID
     * @param text       the message body
     */
    public void sendMessageToEmployee(String senderId, String receiverId, String text) {
        Database db = Database.getInstance();
        User recv = db.getUserById(receiverId);
        User base = (recv instanceof ResearcherDecorator) ? ((ResearcherDecorator) recv).getWrappedUser() : recv;
        if (!(base instanceof Employee)) {
            System.out.println("[Message] Recipient is not an employee: " + receiverId);
            return;
        }
        db.addMessage(new Message(senderId, receiverId, text.trim()));
        db.saveToDisk();
        System.out.println("[Message] Sent to " + recv.getFullName() + ": " + text.trim());
    }
}
