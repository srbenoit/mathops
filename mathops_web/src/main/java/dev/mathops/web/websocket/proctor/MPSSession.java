package dev.mathops.web.websocket.proctor;

import dev.mathops.db.rawrecord.RawStudent;

/**
 * A Mathematics Proctoring Service session.
 */
public final class MPSSession {

    /**
     * The 20-character proctoring session ID, unique for every proctoring session globally. Used as a prefix on web
     * page URLs to ensure page refreshes re-connect to the same session.
     */
    final String proctoringSessionId;

    /** The student record. */
    final RawStudent student;

    /** The course ID with which the exam is associated. */
    public String courseId;

    /** The exam ID being proctored. */
    public String examId;

    /** The state of the proctoring session. */
    /* default */ EProctoringSessionState state;

    /**
     * Flag to indicate student just started the assessment (as opposed to clicking on a problem number during the
     * assessment).
     */
    public boolean justStarted;

    /** The timestamp when this session will time out. */
    public long timeout;

    /**
     * Constructs a new {@code MPSSession}.
     *
     * @param theProcSessionId the proctoring session ID
     * @param theStudent             the student
     */
    /* default */ MPSSession(final String theProcSessionId, final RawStudent theStudent) {

        this.proctoringSessionId = theProcSessionId;
        this.student = theStudent;
        this.state = EProctoringSessionState.AWAITING_STUDENT_PHOTO;
    }

    /**
     * Generates the string representation of the session.
     */
    @Override
    public String toString() {

        return "PS:" + this.courseId + "/" + this.examId + "(" + this.state.name() + ")";
    }
}
