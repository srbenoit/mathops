package dev.mathops.web.site.html;

import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.logic.WebViewData;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.WebSiteProfile;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.session.ExamWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * A base class for HTML session classes.
 */
public class HtmlSessionBase {

    /** Formatter that provides compact format that includes milliseconds. */
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("MM/dd HH:mm:ss.SSS", Locale.US);

    /** The website profile in which this session is running. */
    private WebSiteProfile siteProfile;

    /** The host from the context. */
    private final String host;

    /** The path from the context. */
    private final String path;

    /** The session ID. */
    public final String sessionId;

    /** The student data. */
    private StudentData studentData;

    /** The student record. */
    private RawStudent student;

    /** URL to which to redirect at the end of the assignment. */
    public final String redirectOnEnd;

    /** The exam ID being worked on. */
    public final String version;

    /** The exam writer. */
    private ExamWriter writer;

    /** The exam itself. */
    private ExamObj exam;

    /** The active term. */
    protected final TermRec active;

    /** State of a forced termination request. */
    private EForceTerminateState forceTerminate;

    /** The path where exam files are stored. */
    private File examPath;

    /**
     * Constructs a new {@code HtmlSessionBase}.
     *
     * @param theStudentData   the student data object
     * @param theSiteProfile   the website profile
     * @param theSessionId     the session ID
     * @param theExamId        the ID of the exam
     * @param theRedirectOnEnd the URL to which to redirect at the end of the exam
     * @throws SQLException if there is an error accessing the database
     */
    protected HtmlSessionBase(final StudentData theStudentData, final WebSiteProfile theSiteProfile,
                              final String theSessionId, final String theExamId,
                              final String theRedirectOnEnd) throws SQLException {

        if (theSiteProfile == null) {
            throw new IllegalArgumentException("Site profile may not nbe null");
        }
        if (theSessionId == null) {
            throw new IllegalArgumentException("Session ID may not be null");
        }
        if (theStudentData == null) {
            throw new IllegalArgumentException("Student ID may not be null");
        }
        if (theExamId == null) {
            throw new IllegalArgumentException("Exam ID may not be null");
        }
        if (theRedirectOnEnd == null) {
            throw new IllegalArgumentException("Redirect on end may not be null");
        }

        this.siteProfile = theSiteProfile;
        this.host = theSiteProfile.host;
        this.path = theSiteProfile.path;
        this.sessionId = theSessionId;

        this.version = theExamId;
        this.redirectOnEnd = theRedirectOnEnd;
        this.forceTerminate = EForceTerminateState.NONE;

        this.writer = new ExamWriter();

        this.studentData = theStudentData;
        this.student = theStudentData.getStudentRecord();
        if (this.student == null) {
            throw new SQLException("Student not found");
        }

        this.active = theStudentData.getSystemData().getActiveTerm();
    }

    /**
     * Deserialize the object, which creates the transient {@code Context} and {@code ExamWriter}.
     *
     * @param in the input stream from which to read
     * @throws IOException            if there is an error reading from the stream
     * @throws ClassNotFoundException if the default deserialization finds an invalid class
     */
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {

        in.defaultReadObject();

        this.siteProfile = ContextMap.getDefaultInstance().getWebSiteProfile(this.host, this.path);
        this.writer = new ExamWriter();
    }

    /**
     * Gets the website profile.
     *
     * @return the website profile
     */
    protected final WebSiteProfile getSiteProfile() {

        return this.siteProfile;
    }

    /**
     * Gets the database profile.
     *
     * @return the database profile
     */
    protected final DbProfile getDbProfile() {

        return this.siteProfile.dbProfile;
    }

    /**
     * Sets the exam object.
     *
     * @param theExam the exam object
     */
    protected final void setExam(final ExamObj theExam) {

        this.exam = theExam;

        if (theExam != null && theExam.serialNumber != null && this.active != null) {
            final String studentId = this.studentData.getStudentId();
            final long serialValue = theExam.serialNumber.longValue();

            this.examPath = this.writer.makeExamPath(this.active.term.shortString, studentId,
                    serialValue);
        } else {
            this.examPath = null;
        }
    }

    /**
     * Gets the exam object.
     *
     * @return the exam object
     */
    public final ExamObj getExam() {

        return this.exam;
    }

    /**
     * Gets the student data object.
     *
     * @return the student data object
     */
    public final StudentData getStudentData() {

        return this.studentData;
    }

    /**
     * Gets the student record.
     *
     * @return the student record
     */
    public final RawStudent getStudent() {

        return this.student;
    }

    /**
     * Writes a line to a log file for the exam.
     *
     * @param message the message to log
     */
    protected final void appendExamLog(final String message) {

        if (this.examPath != null) {
            final File log = new File(this.examPath, "exam_activity.log");

            try (final FileWriter out = new FileWriter(log, Charset.defaultCharset(), log.exists())) {

                final LocalDateTime now = LocalDateTime.now();
                final String nowStr = DATE_FMT.format(now);
                out.write(nowStr + CoreConstants.SPC + message + CoreConstants.CRLF);

            } catch (final IOException ex) {
                Log.warning("Failed to append '", message, "' to ", log.getAbsolutePath(), ex);
            }
        }
    }

    /**
     * Appends the footer.
     *
     * @param htm       the {@code HtmlBuilder} to which to append
     * @param command   the submit button name (command)
     * @param label     the button label
     * @param prevCmd   the button name (command) for the "previous" button, null if not present
     * @param prevLabel the button label for the "previous" button
     * @param nextCmd   the button name (command) for the "next" button, null if not present
     * @param nextLabel the button label for the "next" button
     */
    protected static void appendFooter(final HtmlBuilder htm, final String command, final String label,
                                       final String prevCmd, final String prevLabel, final String nextCmd,
                                       final String nextLabel) {

        htm.sDiv(null, "style='flex: 1 100%; order:99; background-color:AliceBlue; display:block; "
                + "border:1px solid SteelBlue; margin:1px; padding:0 12px; text-align:center;'");

        if (prevCmd != null || nextCmd != null) {
            if (prevCmd != null) {
                htm.sDiv("left");
                htm.add("<a class='smallbtn' href='javascript:invokeAct(\"", prevCmd, "\");'");
                htm.add(">", prevLabel, "</a>");
                htm.eDiv();
            }
            if (nextCmd != null) {
                htm.sDiv("right");
                htm.add("<a class='smallbtn' href='javascript:invokeAct(\"", nextCmd, "\");'");
                htm.add(">", nextLabel, "</a>");
                htm.eDiv();
            }

            htm.div("clear");
        }

        if (command != null && command.startsWith("nav")) {
            htm.add("<a class='btn' href='javascript:invokeAct(\"", command, "\");'");
            htm.add(">", label, "</a>");
        } else {
            htm.add(" <input class='btn' type='submit' name='", command, "' value='", label, "'/>");
        }

        htm.eDiv();
    }

    /**
     * Writes a recovery file for the exam to the student exam directory.
     *
     * @throws SQLException if there is an error accessing the database
     */
    public final void writeExamRecovery() throws SQLException {

        if (this.exam != null && this.active != null) {
            Log.info("Writing updated exam state");
            final Object[][] answers = this.exam.exportState();

            // Write the updated exam state somewhere permanent
            final String studentId = this.studentData.getStudentId();
            this.writer.writeUpdatedExam(studentId, this.active, answers, true);
        }
    }

    /**
     * Gets the state of a forced termination. Sessions not being terminated have state NONE.
     *
     * <p>
     * When a forced termination without scoring is requested, state moves to ABORT_WITHOUT_SCORING_REQUESTED and the
     * requesting user is prompted to confirm. When they confirm, state moves to ABORT_WITHOUT_SCORING_CONFIRMED. They
     * are then asked to verify their confirmation, after which the session is terminated without performing scoring.
     *
     * <p>
     * When a forced termination with scoring is requested, state moves to SUBMIT_AND_SCORE_REQUESTED and the requesting
     * user is prompted to confirm. When they confirm, state moves to SUBMIT_AND_SCORE_CONFIRMED. They are then asked to
     * verify their confirmation, after which the session is force-submitted and scored.
     *
     * @return the current state
     */
    public final EForceTerminateState getForceTerminate() {

        return this.forceTerminate;
    }

    /**
     * Sets the state of a forced termination.
     *
     * @param theForceTerminate the new state
     */
    public final void setForceTerminate(final EForceTerminateState theForceTerminate) {

        this.forceTerminate = theForceTerminate;
    }
}
