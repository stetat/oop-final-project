package project.models.errors;

/** Thrown when a proposed research supervisor doesn't meet the minimum h-index requirement. */
public class InvalidSupervisorException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * @param msg explanation of why the supervisor is invalid (includes the h-index values)
     */
    public InvalidSupervisorException(String msg) { super(msg); }
}
