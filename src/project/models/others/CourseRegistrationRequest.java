package project.models.others;

import project.models.enums.UrgencyLevel;

public class CourseRegistrationRequest extends Request {
    private static final long serialVersionUID = 1L;
    private String courseCode;

    public CourseRegistrationRequest(String studentId, String courseCode) {
        super("Course Registration: " + courseCode,
              "Student " + studentId + " requests enrollment in " + courseCode,
              UrgencyLevel.LOW, studentId);
        this.courseCode = courseCode;
    }

    public String getCourseCode() { return courseCode; }
}
