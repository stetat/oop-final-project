package project.models.others;

import java.io.Serializable;
import java.util.*;
import project.enums.CourseType;

/**
 * Represents a university course. Contains lessons, marks per student, and instructor list.
 * Supports multiple instructors (e.g., separate lecture and practice teachers).
 */
public class Course implements Serializable {
    private static final long serialVersionUID = 1L;
    private String courseCode;
    private String courseName;
    private int credits;
    private CourseType courseType;
    private int targetYear;
    private Vector<Lesson> lessons;
    private Map<String, Vector<Mark>> marks;
    private Vector<String> instructorIds;

    public Course() {
        lessons = new Vector<>(); marks = new HashMap<>(); instructorIds = new Vector<>();
    }
    public Course(String courseCode, String courseName, int credits) {
        this(); this.courseCode = courseCode; this.courseName = courseName; this.credits = credits;
    }
    public Course(String courseCode, String courseName, int credits, CourseType type, int targetYear) {
        this(courseCode, courseName, credits); this.courseType = type; this.targetYear = targetYear;
    }

    public void addLesson(Lesson lesson) { if (lesson != null) lessons.add(lesson); }

    /** Assigns a mark to a student (by studentId). */
    public void assignMark(String studentId, Mark mark) {
        marks.computeIfAbsent(studentId, k -> new Vector<>()).add(mark);
    }

    public void addInstructorId(String teacherId) { if (!instructorIds.contains(teacherId)) instructorIds.add(teacherId); }
    public void removeInstructorId(String teacherId) { instructorIds.remove(teacherId); }

    /** Returns latest mark for a student, or null. */
    public Mark getLatestMark(String studentId) {
        Vector<Mark> m = marks.get(studentId);
        return (m != null && !m.isEmpty()) ? m.lastElement() : null;
    }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String v) { this.courseCode = v; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String v) { this.courseName = v; }
    public int getCredits() { return credits; }
    public void setCredits(int v) { this.credits = v; }
    public CourseType getCourseType() { return courseType; }
    public void setCourseType(CourseType v) { this.courseType = v; }
    public int getTargetYear() { return targetYear; }
    public void setTargetYear(int v) { this.targetYear = v; }
    public Vector<Lesson> getLessons() { return lessons; }
    public void setLessons(Vector<Lesson> v) { this.lessons = v; }
    public Map<String, Vector<Mark>> getMarks() { return marks; }
    public void setMarks(Map<String, Vector<Mark>> v) { this.marks = v; }
    public Vector<String> getInstructorIds() { return instructorIds; }
    public void setInstructorIds(Vector<String> v) { this.instructorIds = v; }

    @Override public int hashCode() { return Objects.hash(courseCode, courseName, courseType, credits, targetYear); }
    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Course)) return false;
        Course o = (Course) obj;
        return Objects.equals(courseCode, o.courseCode) && Objects.equals(courseName, o.courseName);
    }
    @Override public String toString() {
        return "Course[" + courseCode + " | " + courseName + " | " + credits + " cr | " + courseType + " | yr" + targetYear + "]";
    }
}
