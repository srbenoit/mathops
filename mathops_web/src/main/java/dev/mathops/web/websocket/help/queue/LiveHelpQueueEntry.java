package dev.mathops.web.websocket.help.queue;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.parser.ParsingException;
import dev.mathops.commons.parser.xml.EmptyElement;
import dev.mathops.web.websocket.help.StudentKey;

/**
 * Information on a single entry in the live help queue.
 *
 * <p>
 * A queue entry will have one of the following:
 * <ul>
 * <li>A course ID, and optional unit and objective numbers, to request general help.
 * <li>A session ID of an active homework session
 * <li>A session ID of an active past exam session
 * <li>A lecture ID
 * </ul>
 */
public final class LiveHelpQueueEntry {

    /** Object on which to synchronize creation of entries. */
    private static final Object SYNCH = new Object();

    /** The last entry ID used. */
    private static long lastId;

    /** The entry ID - system timestamp at creation time, adjusted to ensure uniqueness. */
    /* default */ final Long id;

    /** Student key. */
    public final StudentKey student;

    /** The ID of course in which student is seeking help (M 117, for example). */
    private final String courseId;

    /** A unit number (-1 if none). */
    private final int unit;

    /** An objective number (-1 if none). */
    private final int objective;

    /** The homework session ID. */
    private final String homeworkSessionId;

    /** The past exam session ID. */
    private final String pastExamSessionId;

    /** The ID of the media object. */
    private final String mediaId;

    /** Timestamp when request was queued. */
    /* default */ final long whenQueued;

    /** Last time this entry was "touched" to indicate it is still an active request. */
    private long lastTouch;

    /** The student web socket. */
    public final HelpQueueWebSocket studentWebSocket;

    /**
     * Constructs a new {@code LiveHelpQueueEntry}.
     *
     * @param theId                the entry ID
     * @param theStudent           the student information
     * @param theCourseId          the ID of course in which student is seeking help (M 117, for example)
     * @param theUnit              the unit number
     * @param theObjective         the objective
     * @param theHomeworkSessionId the homework session ID
     * @param thePastExamSessionId the past exam session ID
     * @param theMediaId           the ID of the media object
     * @param theWhenQueued        the server local time when request was queued
     * @param theStudentWebSocket  the student web socket
     * @throws IllegalArgumentException if the student ID, screen name, or queue date/time is null
     */
    private LiveHelpQueueEntry(final Long theId, final StudentKey theStudent,
                               final String theCourseId, final int theUnit, final int theObjective,
                               final String theHomeworkSessionId, final String thePastExamSessionId,
                               final String theMediaId, final long theWhenQueued,
                               final HelpQueueWebSocket theStudentWebSocket) throws IllegalArgumentException {

        if (theId == null || theStudent == null) {
            throw new IllegalArgumentException("Invalid arguments to construction of LiveHelpQueueEntry");
        }

        this.id = theId;
        this.student = theStudent;
        this.courseId = theCourseId;
        this.unit = theUnit;
        this.objective = theObjective;
        this.homeworkSessionId = theHomeworkSessionId;
        this.pastExamSessionId = thePastExamSessionId;
        this.mediaId = theMediaId;
        this.whenQueued = theWhenQueued;
        this.studentWebSocket = theStudentWebSocket;
    }

    /**
     * Generates a new entry ID
     *
     * @return the new ID
     */
    private static Long makeId() {

        final Long id;

        synchronized (SYNCH) {
            long newId = System.currentTimeMillis();
            if (newId <= lastId) {
                newId = lastId + 1L;
            }
            lastId = newId;
            id = Long.valueOf(newId);
        }

        return id;
    }

    /**
     * Constructs a new {@code LiveHelpQueueEntry} for a course/unit/objective.
     *
     * @param theStudent          the student information
     * @param theCourseId         the ID of course in which student is seeking help (M 117, for example)
     * @param theUnit             the unit number
     * @param theObjective        the objective
     * @param theWhenQueued       the timestamp when request was queued
     * @param theStudentWebSocket the student web socket
     * @return the constructed {@code LiveHelpQueueEntryInfo}
     * @throws IllegalArgumentException if the student ID, screen name, queue date/time, or course ID is null
     */
    /* default */
    static LiveHelpQueueEntry forCourse(final StudentKey theStudent,
                                        final String theCourseId, final int theUnit, final int theObjective,
                                        final long theWhenQueued, final HelpQueueWebSocket theStudentWebSocket)
            throws IllegalArgumentException {

        if (theCourseId == null) {
            throw new IllegalArgumentException("Course ID may not be null");
        }

        return new LiveHelpQueueEntry(makeId(), theStudent, theCourseId, theUnit, theObjective,
                null, null, null, theWhenQueued, theStudentWebSocket);
    }

    /**
     * Constructs a new {@code LiveHelpQueueEntry} for a homework session.
     *
     * @param theStudent           the student information
     * @param theHomeworkSessionId the homework session ID
     * @param theWhenQueued        the timestamp when request was queued
     * @param theStudentWebSocket  the student web socket
     * @return the constructed {@code LiveHelpQueueEntryInfo}
     * @throws IllegalArgumentException if the student ID, screen name, queue date/time, or homework session ID is null
     */
    /* default */
    static LiveHelpQueueEntry forHomework(final StudentKey theStudent,
                                          final String theHomeworkSessionId, final long theWhenQueued,
                                          final HelpQueueWebSocket theStudentWebSocket) throws IllegalArgumentException {

        if (theHomeworkSessionId == null) {
            throw new IllegalArgumentException("Session ID may not be null");
        }

        return new LiveHelpQueueEntry(makeId(), theStudent, null, -1, -1, theHomeworkSessionId,
                null, null, theWhenQueued, theStudentWebSocket);
    }

    /**
     * Constructs a new {@code LiveHelpQueueEntry} for a past exam session.
     *
     * @param theStudent           the student information
     * @param thePastExamSessionId the past exam session ID
     * @param theWhenQueued        the timestamp when request was queued
     * @param theStudentWebSocket  the student web socket
     * @return the constructed {@code LiveHelpQueueEntryInfo}
     * @throws IllegalArgumentException if the screen name, queue date/time, or past exam session ID is null
     */
    /* default */
    static LiveHelpQueueEntry forPastExam(final StudentKey theStudent,
                                          final String thePastExamSessionId, final long theWhenQueued,
                                          final HelpQueueWebSocket theStudentWebSocket) throws IllegalArgumentException {

        if (thePastExamSessionId == null) {
            throw new IllegalArgumentException("Session ID may not be null");
        }

        return new LiveHelpQueueEntry(makeId(), theStudent, null, -1, -1, null,
                thePastExamSessionId, null, theWhenQueued, theStudentWebSocket);
    }

    /**
     * Constructs a new {@code LiveHelpQueueEntry} for a media object.
     *
     * @param theStudent          the student ID
     * @param theMediaId          the ID of the media object
     * @param theWhenQueued       the timestamp when request was queued
     * @param theStudentWebSocket the student web socket
     * @return the constructed {@code LiveHelpQueueEntryInfo}
     * @throws IllegalArgumentException if the screen name, queue date/time, or media ID is null
     */
    /* default */
    static LiveHelpQueueEntry forMedia(final StudentKey theStudent,
                                       final String theMediaId, final long theWhenQueued,
                                       final HelpQueueWebSocket theStudentWebSocket) throws IllegalArgumentException {

        if (theMediaId == null) {
            throw new IllegalArgumentException("Media ID may not be null");
        }

        return new LiveHelpQueueEntry(makeId(), theStudent, null, -1, -1, null, null, theMediaId,
                theWhenQueued, theStudentWebSocket);
    }

    /**
     * "Touches" the entry to indicate it is still an active requests and should not be closed due to inactivity.
     */
    /* default */ void touch() {

        this.lastTouch = System.currentTimeMillis();
    }

    /**
     * Gets the last instant the entry was "touched" to indicate it is still active. When a student is waiting for help,
     * their browser will poll the server periodically (every few seconds) to indicate they are still waiting, to allow
     * the server to detect browser closure or loss of network connection. Requests that have not been "touched"
     * recently can be considered stale and deleted.
     *
     * <p>
     * Alternatively, stale requests could be temporarily moved to a "stale" queue, and if the network connection is
     * restored within some longer timeout, they could be restored to an appropriate position in the queue (based on
     * queue time).
     *
     * @return the last touch timestamp
     */
    public long getLastTouch() {

        return this.lastTouch;
    }

    /**
     * Serializes the entry in JSON format to a {@code HtmlBuilder}.
     *
     * @param htm the {@code HtmlBuilder} to which to write
     */
    /* default */ void toJSON(final HtmlBuilder htm) {

        final int sec = (int) (Math.max(0L, System.currentTimeMillis() - this.whenQueued) / 1000L);
        final int mm = sec / 60;
        final int s1 = sec % 60 / 10;
        final int s2 = sec % 10;

        htm.add("{ \"id\": \"", this.id, "\", \"stu\": \"",
                this.student.studentId, "\", \"screen\": \"",
                this.student.screenName, CoreConstants.QUOTE);

        if (this.courseId != null) {
            htm.add(", \"course\": \"", this.courseId, CoreConstants.QUOTE);
        }
        if (this.unit >= 0) {
            htm.add(", \"unit\": ", Integer.toString(this.unit));
        }
        if (this.objective >= 0) {
            htm.add(", \"obj\": ", Integer.toString(this.objective));
        }
        if (this.homeworkSessionId != null) {
            htm.add(", \"hw\": \"", this.homeworkSessionId, CoreConstants.QUOTE);
        }
        if (this.pastExamSessionId != null) {
            htm.add(", \"past\": \"", this.pastExamSessionId, CoreConstants.QUOTE);
        }
        if (this.mediaId != null) {
            htm.add(", \"media\": \"", this.mediaId, CoreConstants.QUOTE);
        }
        htm.add(", \"queued\": ", Long.toString(this.whenQueued),
                ", \"wait\": \"", Integer.toString(mm), CoreConstants.COLON,
                Integer.toString(s1), Integer.toString(s2), "\"}");
    }

    /**
     * Serializes the entry in XML format to a {@code HtmlBuilder}.
     *
     * @param htm the {@code HtmlBuilder} to which to write
     */
    /* default */ void toXML(final HtmlBuilder htm) {

        htm.openElement(0, "entry");

        htm.addAttribute("id", this.id, 0);
        htm.addAttribute("stu", this.student.studentId, 0);
        htm.addAttribute("first", this.student.firstName, 0);
        htm.addAttribute("last", this.student.lastName, 0);
        htm.addAttribute("screen", this.student.screenName, 0);

        htm.addAttribute("course", this.courseId, 0);
        if (this.unit >= 0) {
            htm.addAttribute("unit", Integer.toString(this.unit), 0);
        }
        if (this.objective >= 0) {
            htm.addAttribute("obj", Integer.toString(this.objective), 0);
        }
        htm.addAttribute("hw", this.homeworkSessionId, 0);
        htm.addAttribute("past", this.pastExamSessionId, 0);
        htm.addAttribute("media", this.mediaId, 0);
        htm.addAttribute("queued", Long.toString(this.whenQueued), 0);

        htm.closeEmptyElement(false);
    }

    /**
     * Attempts to parse a {@code LiveHelpQueueEntry} from a serialized XML string.
     *
     * @param elem the element to parse
     * @return the reconstructed {@code LiveHelpQueueEntry}
     * @throws ParsingException if the XML could not be parsed
     */
    /* default */
    static LiveHelpQueueEntry parseXML(final EmptyElement elem)
            throws ParsingException {

        final Long id = elem.getRequiredLongAttr("id");
        final String stu = elem.getRequiredStringAttr("stu");
        final String first = elem.getRequiredStringAttr("first");
        final String last = elem.getRequiredStringAttr("last");
        final String screen = elem.getRequiredStringAttr("screen");
        final long when = elem.getRequiredLongAttr("queued").longValue();

        final String course = elem.getStringAttr("course");
        final int unit = elem.getIntegerAttr("unit", Integer.valueOf(-1)).intValue();
        final int obj = elem.getIntegerAttr("obj", Integer.valueOf(-1)).intValue();
        final String hw = elem.getStringAttr("hw");
        final String past = elem.getStringAttr("past");
        final String media = elem.getStringAttr("media");

        final LiveHelpQueueEntry result;

        final StudentKey key = new StudentKey(stu, first, last, screen);

        if (course != null) {
            result =
                    new LiveHelpQueueEntry(id, key, course, unit, obj, null, null, null, when, null);
        } else if (hw != null) {
            result = new LiveHelpQueueEntry(id, key, null, -1, -1, hw, null, null, when, null);
        } else if (past != null) {
            result = new LiveHelpQueueEntry(id, key, null, -1, -1, null, past, null, when, null);
        } else if (media != null) {
            result = new LiveHelpQueueEntry(id, key, null, -1, -1, null, null, media, when, null);
        } else {
            throw new ParsingException(elem, "Failed to parse LiveHelpQueueEntry");
        }

        return result;
    }
}
