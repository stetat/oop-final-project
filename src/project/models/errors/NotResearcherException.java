package project.models.errors;

/** Thrown when a non-researcher user tries to join or act within a research project. */
public class NotResearcherException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * @param msg description of the offending user and context
     */
    public NotResearcherException(String msg) { super(msg); }
}
