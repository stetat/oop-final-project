package project.services;

import java.util.List;
import project.models.actors.Employee;
import project.models.actors.User;
import project.models.others.Message;
import project.patterns.ResearcherDecorator;
import project.storage.Database;

public class MessageService {

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

    public void sendMessage(String senderId, String receiverId, String text) {
        Database db = Database.getInstance();
        User recv = db.getUserById(receiverId);
        if (recv == null) { System.out.println("[Message] User not found: " + receiverId); return; }
        db.addMessage(new Message(senderId, receiverId, text.trim()));
        db.saveToDisk();
        System.out.println("[Message] Sent to " + recv.getFullName() + ": " + text.trim());
    }

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
