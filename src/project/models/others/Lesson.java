package project.models.others;

import java.io.Serializable;
import java.util.Objects;

import project.models.enums.LessonType;

/** Represents a scheduled lesson (lecture or practice) with room and time. */
public class Lesson implements Serializable {
    private static final long serialVersionUID = 1L;
    private LessonType type;
    private String room;
    private String time;

    public Lesson() {}
    public Lesson(LessonType type, String room, String time) {
        this.type = type; this.room = room; this.time = time;
    }

    public LessonType getType() { return type; }
    public void setType(LessonType t) { this.type = t; }
    public String getRoom() { return room; }
    public void setRoom(String r) { this.room = r; }
    public String getTime() { return time; }
    public void setTime(String t) { this.time = t; }

    @Override public int hashCode() { return Objects.hash(room, time, type); }
    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Lesson)) return false;
        Lesson o = (Lesson) obj;
        return Objects.equals(room, o.room) && Objects.equals(time, o.time) && type == o.type;
    }
    @Override public String toString() { return "Lesson[" + type + " | " + room + " | " + time + "]"; }
}
