package dev.mathops.web.websocket.proctor;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.logic.placement.PlacementLogic;
import dev.mathops.db.logic.placement.PlacementStatus;
import dev.mathops.db.logic.tutorial.ELMTutorialStatus;
import dev.mathops.db.logic.tutorial.PrecalcTutorialLogic;
import dev.mathops.db.logic.tutorial.PrecalcTutorialStatus;
import dev.mathops.db.old.logic.HoldsStatus;
import dev.mathops.db.old.logic.PrerequisiteLogic;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawExam;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.rec.TermRec;
import dev.mathops.session.ISessionManager;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.SessionManager;
import dev.mathops.session.SessionResult;
import dev.mathops.session.sitelogic.servlet.FinalExamAvailability;
import dev.mathops.session.sitelogic.servlet.FinalExamEligibilityTester;
import dev.mathops.session.sitelogic.servlet.UnitExamAvailability;
import dev.mathops.session.sitelogic.servlet.UnitExamEligibilityTester;
import dev.mathops.text.builder.HtmlBuilder;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import oracle.jdbc.proxy.annotation.OnError;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * The WebSocket service for the Mathematics Proctoring System (MPS).
 * <h2>Exchanges</h2>
 *
 * <pre>
 * Receive:  !{Login-Session-Id}
 * Send:     ERROR
 * Send:     CONNECTED-NO-SESSION
 *           {
 *               "categories" : [
 *                   {
 *                       "title" : "Precalculus Course Exams",
 *                       "exams": [
 *                           {
 *                               "id" : "171UE",
 *                               "label" : "Unit 1 Exam"
 *                           }
 *                       ]
 *                   },
 *                   {
 *                       "title" : "Tutorial Exams",
 *                       "exams": [
 *                           {
 *                               "id" : "MT4UE",
 *                               "label" : "ELM Exam"
 *                           }
 *                       ]
 *                   },
 *                   {
 *                       "title" : "Math Placement Tool and Course Challenge Exams",
 *                       "exams": [
 *                           {
 *                               "id" : "MPTRW",
 *                               "label" : "Math Placement Tool",
 *                               "note" : "One-time $15 fee",
 *                           }
 *                       ]
 *                   }
 *               ]
 *           }
 * Send:     SESSION
 *           {
 *               "psid"   : "abc123"
 *               "stuid"  : "888888888"
 *               "examid" : "MPTRW"
 *               "state"  : "{Some constant from EProctoringSessionState}"
 *           }
 *
 * Receive:  S{ExamId} ("Start session")
 * Send:     ERROR
 * Send:     SESSION
 *           {
 *               "psid" : "abc123"
 *               "stuid"  : "888888888"
 *               "examid" : "MPTRW"
 *               "state" : "AWAITING_STUDENT_PHOTO"
 *           }
 *
 * Receive:  P ("captured photo")
 * Send:     ERROR
 * Send:     SESSION
 *           {
 *               "psid" : "abc123"
 *               "stuid"  : "888888888"
 *               "examid" : "MPTRW"
 *               "state" : "AWAITING_STUDENT_ID"
 *           }
 *
 * Receive:  I ("captured ID")
 * Send:     ERROR
 * Send:     SESSION
 *           {
 *               "psid" : "abc123"
 *               "stuid"  : "888888888"
 *               "examid" : "MPTRW"
 *               "state" : "SHOWING_INSTRUCTIONS"
 *           }
 * </pre>
 */
@ServerEndpoint("/ws/mps")
public final class MPSEndpoint {

    /** Characters allowed in session ID, in lexical order. */
    public static final String LEXICAL_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /** Length of a proctoring session ID. */
    private static final int PSID_LENGTH = ISessionManager.SESSION_ID_LEN + 1;

    /** Prefix for log messages. */
    private static final String LOG_PREFIX = "MPS WebSocket endpoint: ";

    /** Timeout after no activity when session is terminated. */
    public static final long SESSION_TIMEOUT_MS = 1000 * 60 * 180;

    /** WebSocket session associated with connection. */
    private Session session;

    /** The site profile. */
    private final Profile siteProfile;

    /** The session manager. */
    private final MPSSessionManager mgr;

    /** The student. */
    private RawStudent student;

    /** The proctoring session. */
    private MPSSession ps;

    /**
     * Constructs a new {@code MPSEndpoint}.
     */
    public MPSEndpoint() {

        // Log.info(LOG_PREFIX, "endpoint created");

        this.mgr = MPSSessionManager.getInstance();

        this.siteProfile = DatabaseConfig.getDefault().getWebProfile(Contexts.COURSE_HOST, Contexts.MPS_PATH);
    }

    /**
     * Called when the socket is opened.
     *
     * @param theSession the session
     */
    @OnOpen
    public void start(final Session theSession) {

        Log.info(LOG_PREFIX, "websocket opened");

        this.session = theSession;
    }

    /**
     * Called when the socket is closed.
     *
     * @param theSession the session
     */
    @OnClose
    public void onClose(final Session theSession) { // This actually is used, but via reflection

        Log.info(LOG_PREFIX, "websocket closed");

        this.session = null;
    }

    /**
     * Called when a text message arrives.
     *
     * @param message the message
     * @throws IOException if there is an error writing the response
     */
    @OnMessage
    public void incoming(final String message)
            throws IOException {

        Log.info(LOG_PREFIX, "websocket received message: ", message);

        // First character is a message type, followed by message data

        if (!message.isEmpty()) {
            try {
                final char ch = message.charAt(0);

                if (ch == '!') {
                    processConnect(message.substring(1));
                } else if (ch == '?') {
                    processQuery();
                } else if (ch == 'S') {
                    processStart(message.substring(1));
                } else if (ch == 'P') {
                    processPhoto();
                } else if (ch == 'I') {
                    processId();
                } else if (ch == 'E') {
                    processEnvironment();
                } else if (ch == 'A') {
                    processAssessment();
                } else if (ch == 'F') {
                    processFinished();
                } else if (ch == 'X') {
                    processStartOver(message.substring(1));
                } else if (ch == 'R') {
                    processRejoin();
                } else if (ch == '~') {
                    processPing();
                } else if (ch == '.') {
                    processKeepalive();
                } else {
                    Log.warning(LOG_PREFIX, "Unexpected message type :" + ch);
                }
            } catch (final Exception ex) {
                Log.warning(ex);
                throw ex;
            }
        }
    }

    /**
     * Processes a connect request from a proctor session.
     *
     * <p>
     * The message data should consist of the login ID of the login session under which the connection is being made.
     *
     * @param lsid the login session ID
     * @throws IOException if there is an error writing the response
     */
    private void processConnect(final String lsid) throws IOException {

        // Log.info("Connect message with login session ID: ", lsid);

        final SessionResult result = SessionManager.getInstance().validate(lsid);

        if (result.error != null) {
            Log.warning(LOG_PREFIX, "Unable to validate login session:",
                    result.error);
            this.session.getBasicRemote().sendText("ERROR");
        } else if (result.session == null) {
            Log.warning(LOG_PREFIX, "Unable to validate login session.");
            this.session.getBasicRemote().sendText("ERROR");
        } else {
            final String studentId = result.session.getEffectiveUserId();

            final Cache cache = new Cache(this.siteProfile);

            try {
                this.student = RawStudentLogic.query(cache, studentId, false);

                if (this.student == null) {
                    Log.warning(LOG_PREFIX, "Unable to look up student ", studentId);
                    this.session.getBasicRemote().sendText("ERROR");
                } else {
                    this.ps = this.mgr.getSessionForStudent(studentId);

                    if (this.ps == null) {
                        final List<ExamCategory> avail = findAvailableExams(cache, result.session);

                        final HtmlBuilder msg = new HtmlBuilder(100);
                        msg.addln("CONNECTED-NO-SESSION{");
                        msg.addln(" \"categories\" : [");

                        final int numCategories = avail.size();
                        for (int i = 0; i < numCategories; ++i) {
                            final ExamCategory cat = avail.get(i);

                            msg.addln("  {");
                            msg.addln("   \"title\" : \"", cat.name, "\",");
                            msg.addln("   \"exams\" : [");

                            final int numExams = cat.exams.size();
                            for (int j = 0; j < numExams; ++j) {
                                final ExamEntry exam = cat.exams.get(j);

                                msg.addln("    {");
                                msg.addln("     \"id\" : \"", exam.examId,
                                        "\",");
                                msg.add("     \"label\" : \"", exam.buttonLabel,
                                        CoreConstants.QUOTE);
                                if (exam.note == null) {
                                    msg.addln();
                                } else {
                                    msg.addln(",");
                                    msg.addln("     \"note\" : \"", exam.note,
                                            CoreConstants.QUOTE);
                                }
                                if (j == numExams - 1) {
                                    msg.addln("    }");
                                } else {
                                    msg.addln("    },");
                                }
                            }
                            msg.addln("   ]");

                            if (i == numCategories - 1) {
                                msg.addln("  }");
                            } else {
                                msg.addln("  },");
                            }
                        }
                        msg.addln(" ]");
                        msg.addln("}");

                        Log.info(LOG_PREFIX, "sending '", msg.toString(),
                                "'");
                        this.session.getBasicRemote().sendText(msg.toString());

                    } else {
                        final HtmlBuilder msg = new HtmlBuilder(100);

                        msg.addln("CONNECTED-SESSION{")
                                .addln(" \"psid\" : \"", this.ps.proctoringSessionId, "\",")
                                .addln(" \"stuid\" : \"", this.ps.student.stuId, "\",")
                                .addln(" \"courseid\" : \"", this.ps.courseId, "\",")
                                .addln(" \"examid\" : \"", this.ps.examId, "\",")
                                .addln(" \"state\" : \"", this.ps.state.name(), CoreConstants.QUOTE)
                                .addln("}");

                        Log.info(LOG_PREFIX, "sending '", msg.toString(),
                                "'");

                        this.session.getBasicRemote().sendText(msg.toString());
                    }
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
            }
        }
    }

    /**
     * Processes a terminate request from a proctor session.
     *
     * <p>
     * The message data should consist of the login ID of the login session under which the connection is being made.
     *
     * @param cache the data cache
     * @param lsid  the login session ID
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private void processTerminate(final Cache cache, final String lsid) throws IOException, SQLException {

        // Log.info("Connect message with login session ID: ", lsid);

        final SessionResult result = SessionManager.getInstance().validate(lsid);

        if (result.error != null) {
            Log.warning(LOG_PREFIX, "Unable to validate login session:", result.error);
            this.session.getBasicRemote().sendText("ERROR");
        } else if (result.session == null) {
            Log.warning(LOG_PREFIX, "Unable to validate login session.");
            this.session.getBasicRemote().sendText("ERROR");
        } else {
            final String studentId = result.session.getEffectiveUserId();

            this.student = RawStudentLogic.query(cache, studentId, false);
            if (this.student == null) {
                Log.warning(LOG_PREFIX, "Unable to look up student " + studentId);
                this.session.getBasicRemote().sendText("ERROR");
            } else {
                final List<ExamCategory> avail = findAvailableExams(cache, result.session);

                final HtmlBuilder msg = new HtmlBuilder(100);
                msg.addln("TERMINATED{");
                msg.addln(" \"categories\" : [");

                final int numCategories = avail.size();
                for (int i = 0; i < numCategories; ++i) {
                    final ExamCategory cat = avail.get(i);

                    msg.addln("  {");
                    msg.addln("   \"title\" : \"", cat.name, "\",");
                    msg.addln("   \"exams\" : [");

                    final int numExams = cat.exams.size();
                    for (int j = 0; j < numExams; ++j) {
                        final ExamEntry exam = cat.exams.get(j);

                        msg.addln("    {");
                        msg.addln("     \"id\" : \"", exam.examId, "\",");
                        msg.add("     \"label\" : \"", exam.buttonLabel, CoreConstants.QUOTE);
                        if (exam.note == null) {
                            msg.addln();
                        } else {
                            msg.addln(CoreConstants.COMMA);
                            msg.addln("     \"note\" : \"", exam.note, CoreConstants.QUOTE);
                        }
                        if (j == numExams - 1) {
                            msg.addln("    }");
                        } else {
                            msg.addln("    },");
                        }
                    }
                    msg.addln("   ]");

                    if (i == numCategories - 1) {
                        msg.addln("  }");
                    } else {
                        msg.addln("  },");
                    }
                }

                msg.addln(" ]");
                msg.addln("}");

                // Log.info(LOG_PREFIX, "sending '", msg.toString(), "'");
                this.session.getBasicRemote().sendText(msg.toString());
            }
        }
    }

    /**
     * Processes a query of session status.
     *
     * @throws IOException if there is an error writing the response
     */
    private void processQuery() throws IOException {

        sendSessionInfo();
    }

    /**
     * Sends a message to the browser with the current session state.
     *
     * @throws IOException if there is an error writing
     */
    private void sendSessionInfo() throws IOException {

        final HtmlBuilder msg = new HtmlBuilder(100);

        msg.addln("SESSION{")
                .addln(" \"psid\" : \"", this.ps.proctoringSessionId, "\",")
                .addln(" \"stuid\" : \"", this.ps.student.stuId, "\",")
                .addln(" \"courseid\" : \"", this.ps.courseId, "\",")
                .addln(" \"examid\" : \"", this.ps.examId, "\",")
                .addln(" \"state\" : \"", this.ps.state.name(), CoreConstants.QUOTE)
                .addln("}");

        // Log.info(LOG_PREFIX, "sending '", msg.toString(), "'");

        this.session.getBasicRemote().sendText(msg.toString());
    }

    /**
     * Determines the list of exams for which the student is eligible.
     *
     * @param cache        the data cache
     * @param loginSession the session
     * @return a map from category to the list of available exam IDs
     * @throws SQLException if there is an error accessing the database
     */
    private List<ExamCategory> findAvailableExams(final Cache cache, final ImmutableSessionInfo loginSession)
            throws SQLException {

        final String studentId = this.student.stuId;

        final List<ExamCategory> result = new ArrayList<>(3);

        final SystemData systemData = cache.getSystemData();
        final TermRec active = systemData.getActiveTerm();

        // Check for course exams
        final List<ExamEntry> courseExams = new ArrayList<>(10);

        final List<RawStcourse> regs = RawStcourseLogic.getActiveForStudent(cache, studentId, active.term);

        final boolean notRamwork = !RawSpecialStusLogic.isSpecialType(cache, studentId, LocalDate.now(),
                RawSpecialStus.RAMWORK);

        // Eliminate placement credit registrations, and (if not RAMWORK), RI courses
        final Iterator<RawStcourse> regIter = regs.iterator();
        while (regIter.hasNext()) {
            final RawStcourse next = regIter.next();
            final String course = next.course;

            if ("OT".equals(next.instrnType)) {
                regIter.remove();
            } else if ((!RawRecordConstants.M117.equals(course) && !RawRecordConstants.M118.equals(course)
                        && !RawRecordConstants.M124.equals(course) && !RawRecordConstants.M125.equals(course)
                        && !RawRecordConstants.M126.equals(course))) {
                regIter.remove();
            } else if (notRamwork && "RI".equals(next.instrnType)) {
                regIter.remove();
            }
        }

        if (!regs.isEmpty()) {
            // Put all the course-related exams first, since that's the most likely goal for
            // students who are eligible for such exams

            final UnitExamEligibilityTester unitElig = new UnitExamEligibilityTester(studentId);
            final FinalExamEligibilityTester finalElig = new FinalExamEligibilityTester(studentId);
            final HtmlBuilder reasons = new HtmlBuilder(100);
            final Collection<RawAdminHold> holds = new ArrayList<>(2);

            // Add available proctored exams for any courses that are open
            for (final RawStcourse reg : regs) {

                if ("Y".equals(reg.openStatus)) {
                    final String course = reg.course;

                    final String lbl = course.replace("M ", "MATH ");

                    final RawExam u1 = systemData.getActiveExamByCourseUnitType(course, Integer.valueOf(1), "U");
                    if (u1 != null && unitElig.isExamEligible(cache, loginSession,
                            new UnitExamAvailability(course, Integer.valueOf(1)), reasons, holds)) {
                        courseExams.add(new ExamEntry(u1.version, lbl + " - " + u1.buttonLabel, null));
                    } else {
                        Log.warning("Unit 1 in course not available: ", reasons.toString());
                    }

                    final RawExam u2 = systemData.getActiveExamByCourseUnitType(course, Integer.valueOf(2), "U");
                    if ((u2 != null) && unitElig.isExamEligible(cache, loginSession,
                            new UnitExamAvailability(course, Integer.valueOf(2)), reasons, holds)) {
                        courseExams.add(new ExamEntry(u2.version, lbl + " - " + u2.buttonLabel, null));
                    } else {
                        Log.warning("Unit 2 in course not available: ", reasons.toString());
                    }

                    final RawExam u3 = systemData.getActiveExamByCourseUnitType(course, Integer.valueOf(3), "U");
                    if ((u3 != null) && unitElig.isExamEligible(cache, loginSession,
                            new UnitExamAvailability(course, Integer.valueOf(3)), reasons, holds)) {
                        courseExams.add(new ExamEntry(u3.version, lbl + " - " + u3.buttonLabel, null));
                    } else {
                        Log.warning("Unit 3 in course not available: ", reasons.toString());
                    }

                    final RawExam u4 = systemData.getActiveExamByCourseUnitType(course, Integer.valueOf(4), "U");
                    if ((u4 != null) && unitElig.isExamEligible(cache, loginSession,
                            new UnitExamAvailability(course, Integer.valueOf(4)), reasons, holds)) {
                        courseExams.add(new ExamEntry(u4.version, lbl + " - " + u4.buttonLabel, null));
                    } else {
                        Log.warning("Unit 4 in course not available: ", reasons.toString());
                    }

                    final RawExam fe = systemData.getActiveExamByCourseUnitType(course, Integer.valueOf(5), "F");
                    if ((fe != null) && finalElig.isExamEligible(cache, loginSession,
                            new FinalExamAvailability(course, Integer.valueOf(5)), reasons, holds)) {
                        courseExams.add(new ExamEntry(fe.version, lbl + " - " + fe.buttonLabel, null));
                    } else {
                        Log.warning("Final in course not available: ", reasons.toString());
                    }
                }
            }
        }

        if (!courseExams.isEmpty()) {
            result.add(new ExamCategory("Precalculus Course Exams", courseExams));
        }

        // Check for Tutorial exams
        final List<ExamEntry> tutorialExams = new ArrayList<>(10);

        final ELMTutorialStatus elm = ELMTutorialStatus.of(cache, studentId, loginSession.getNow(),
                HoldsStatus.of(cache, studentId));
        if (elm.eligibleForElmExam) {
            tutorialExams.add(new ExamEntry("MT4UE", "ELM Exam", null));
        }

        final PrerequisiteLogic prereq = new PrerequisiteLogic(cache, studentId);
        final PrecalcTutorialLogic precalc =
                new PrecalcTutorialLogic(cache, studentId, loginSession.getNow().toLocalDate(), prereq);
        final PrecalcTutorialStatus precalcStatus = precalc.status;
        if (precalcStatus.eligiblePrecalcExamCourses != null) {
            final Collection<RawExam> exams = new ArrayList<>(10);
            for (final String course : precalcStatus.eligiblePrecalcExamCourses) {
                exams.addAll(systemData.getActiveExams(course));
            }
            for (final RawExam ex : exams) {
                if ("U".equals(ex.examType) && ex.unit.intValue() == 4) {
                    tutorialExams.add(new ExamEntry(ex.version, ex.buttonLabel, null));
                }
            }
        }

        if (!tutorialExams.isEmpty()) {
            result.add(new ExamCategory("Tutorial Exams", tutorialExams));
        }

        // Check for Placement exams
        final List<ExamEntry> placementExams = new ArrayList<>(10);

        final PlacementLogic placement = new PlacementLogic(cache, studentId, this.student.aplnTerm,
                loginSession.getNow());
        final PlacementStatus placementStatus = placement.status;
        if (placementStatus.attemptsRemaining > 0) {
            final RawExam ex = systemData.getActiveExam("MPTRW");
            if (ex != null) {
                placementExams.add(new ExamEntry(ex.version, ex.buttonLabel, "One-time $15 fee"));
            }
        }

        if (!placementExams.isEmpty()) {
            result.add(new ExamCategory("Math Placement Tool and Course Challenge Exams", placementExams));
        }

        return result;
    }

    /**
     * Processes a request to start a new proctoring session (once an exam has been selected).
     *
     * <p>
     * The message data should be the exam ID.
     *
     * @param examId the exam ID
     * @throws IOException if there is an error writing the response
     */
    private void processStart(final String examId) throws IOException {

        if (this.ps == null) {
            // Construct a session ID so lexical sort is also date/time sort.
            final LocalDateTime now = LocalDateTime.now();
            final char[] prefix = new char[6];
            // Ensure 1st character is a letter, in case that's needed in some context
            prefix[0] = LEXICAL_CHARS.charAt(now.getYear() % 100 + 10);
            prefix[1] = LEXICAL_CHARS.charAt(now.getMonthValue());
            prefix[2] = LEXICAL_CHARS.charAt(now.getDayOfMonth());
            prefix[3] = LEXICAL_CHARS.charAt(now.getHour());
            prefix[4] = LEXICAL_CHARS.charAt(now.getMinute());
            prefix[5] = LEXICAL_CHARS.charAt(now.getSecond());

            // Create a new proctoring session in the "PICKING EXAM" state
            final String psid = new String(prefix) + CoreConstants.newId(PSID_LENGTH - prefix.length);

            if (this.student == null) {
                Log.warning("Received START request with no student set.");
                this.session.getBasicRemote().sendText("ERROR");
            } else {
                final Cache cache = new Cache(this.siteProfile);
                final SystemData systemData = cache.getSystemData();

                try {
                    final RawExam exam = systemData.getActiveExam(examId);

                    if (exam == null) {
                        Log.warning("Received START request with bad exam ID: " + examId);
                        this.session.getBasicRemote().sendText("ERROR");
                    } else {
                        final MPSSession newSession = new MPSSession(psid, this.student);
                        newSession.courseId = exam.course;
                        newSession.examId = examId;
                        newSession.timeout = System.currentTimeMillis() + SESSION_TIMEOUT_MS;

                        final LocalDateTime newTimeout = LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(newSession.timeout), ZoneId.systemDefault());
                        Log.info("Updating timeout on session ", newSession, " to ",
                                TemporalUtils.FMT_MDY_HMS.format(newTimeout));

                        this.mgr.addSession(newSession);
                        this.ps = newSession;

                        sendSessionInfo();
                    }
                } catch (final SQLException ex) {
                    Log.warning("Failed to connect to the database", ex);
                    this.session.getBasicRemote().sendText("ERROR");
                }
            }
        } else {
            Log.warning("Attempt to create new proctoring session when one exists");
            this.session.getBasicRemote().sendText("ERROR");
        }
    }

    /**
     * Processes the indication that the photo has been captured.
     *
     * @throws IOException if there is an error writing the response
     */
    private void processPhoto() throws IOException {

        if (this.ps == null) {
            Log.warning("Photo received while proctoring session is null");
        } else {
            Log.info("Photo received - switching to 'awaiting staudent ID'");

            this.ps.state = EProctoringSessionState.AWAITING_STUDENT_ID;
            this.ps.timeout = System.currentTimeMillis() + SESSION_TIMEOUT_MS;

            final LocalDateTime newTimeout = LocalDateTime.ofInstant(Instant.ofEpochMilli(this.ps.timeout),
                    ZoneId.systemDefault());
            Log.info("Updating timeout on session ", this.ps, " to ", TemporalUtils.FMT_MDY_HMS.format(newTimeout));

            sendSessionInfo();
        }
    }

    /**
     * Processes the indication that the ID has been captured.
     *
     * @throws IOException if there is an error writing the response
     */
    private void processId() throws IOException {

        if (this.ps == null) {
            Log.warning("ID received while proctoring session is null");
        } else {
            Log.info("ID received - switching to 'scanning environment'");

            // To Bypass environment scan, switch to "SHOWING_INSTRUCTIONS" below.
            this.ps.state = EProctoringSessionState.ENVIRONMENT;
            // this.ps.state = EProctoringSessionState.SHOWING_INSTRUCTIONS;

            this.ps.timeout = System.currentTimeMillis() + SESSION_TIMEOUT_MS;

            final LocalDateTime newTimeout = LocalDateTime.ofInstant(Instant.ofEpochMilli(this.ps.timeout),
                    ZoneId.systemDefault());
            Log.info("Updating timeout on session ", this.ps, " to ", TemporalUtils.FMT_MDY_HMS.format(newTimeout));

            sendSessionInfo();
        }
    }

    /**
     * Processes the indication that the environment has been scanned.
     *
     * @throws IOException if there is an error writing the response
     */
    private void processEnvironment() throws IOException {

        if (this.ps == null) {
            Log.warning("Environment received while proctoring session is null");
        } else {
            Log.info("environment scanned - switching to 'showing instructions'");

            this.ps.state = EProctoringSessionState.SHOWING_INSTRUCTIONS;
            this.ps.timeout = System.currentTimeMillis() + SESSION_TIMEOUT_MS;

            final LocalDateTime newTimeout = LocalDateTime.ofInstant(Instant.ofEpochMilli(this.ps.timeout),
                    ZoneId.systemDefault());
            Log.info("Updating timeout on session ", this.ps, " to ", TemporalUtils.FMT_MDY_HMS.format(newTimeout));

            sendSessionInfo();
        }
    }

    /**
     * Processes the indication that the assessment should be started/joined.
     *
     * @throws IOException if there is an error writing the response
     */
    private void processAssessment() throws IOException {

        if (this.ps == null) {
            Log.warning("Assessment started while proctoring session is null");
        } else {
            Log.info("assessment started");

            this.ps.justStarted = true;
            this.ps.state = EProctoringSessionState.ASSESSMENT;
            this.ps.timeout = System.currentTimeMillis() + SESSION_TIMEOUT_MS;

            final LocalDateTime newTimeout = LocalDateTime.ofInstant(Instant.ofEpochMilli(this.ps.timeout),
                    ZoneId.systemDefault());
            Log.info("Updating timeout on session ", this.ps, " to ", TemporalUtils.FMT_MDY_HMS.format(newTimeout));

            sendSessionInfo();
        }
    }

    /**
     * Processes the indication that the assessment should be ended.
     *
     * @throws IOException if there is an error writing the response
     */
    private void processFinished() throws IOException {

        if (this.ps == null) {
            Log.warning("Assessment finished while proctoring session is null");
        } else {
            Log.info("assessment finished");

            this.mgr.endSession(this.ps);
            this.ps = null;
        }

        this.session.getBasicRemote().sendText("CLOSED");
    }

    /**
     * Processes the indication that the user wants to start over.
     *
     * @param lsid the login session ID
     * @throws IOException if there is an error writing the response
     */
    private void processStartOver(final String lsid) throws IOException {

        if (this.ps != null) {
            this.mgr.endSession(this.ps);
            this.ps = null;
        }

        final Cache cache = new Cache(this.siteProfile);

        try {
            processTerminate(cache, lsid);
        } catch (final SQLException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Called when the user asks to rejoin an existing session.
     *
     * @throws IOException if there is an error writing the response
     */
    private void processRejoin() throws IOException {

        if (this.ps == null) {
            Log.warning("Session rejoined while proctoring session is null");
        } else {
            Log.info("session rejoined");

            this.ps.timeout = System.currentTimeMillis() + SESSION_TIMEOUT_MS;

            final LocalDateTime newTimeout = LocalDateTime.ofInstant(Instant.ofEpochMilli(this.ps.timeout),
                    ZoneId.systemDefault());
            Log.info("Updating timeout on session ", this.ps, " to ", TemporalUtils.FMT_MDY_HMS.format(newTimeout));

            sendSessionInfo();
        }
    }

    /**
     * Called when a "ping" is received on a session, updating its timeout.
     *
     * @throws IOException if there is an error writing the response
     */
    private void processPing() throws IOException {

        if (this.ps != null) {
            this.ps.timeout = System.currentTimeMillis() + SESSION_TIMEOUT_MS;

            final LocalDateTime newTimeout = LocalDateTime.ofInstant(Instant.ofEpochMilli(this.ps.timeout),
                    ZoneId.systemDefault());
            Log.info("Updating timeout on session ", this.ps, " to ", TemporalUtils.FMT_MDY_HMS.format(newTimeout));

            sendSessionInfo();
        }
    }

    /**
     * Called when a "keepalive" is received on a session, updating its timeout.
     */
    private void processKeepalive() {

        if (this.ps != null) {
            this.ps.timeout = System.currentTimeMillis() + SESSION_TIMEOUT_MS;

            final LocalDateTime newTimeout = LocalDateTime
                    .ofInstant(Instant.ofEpochMilli(this.ps.timeout), ZoneId.systemDefault());
            Log.info("Updating timeout on session ", this.ps, " to ", TemporalUtils.FMT_MDY_HMS.format(newTimeout));
        }
    }

    /**
     * Called when there is an error on the connection.
     *
     * @param t the error
     */
    @OnError
    public void onError(final Throwable t) { // This actually is used, but via reflection

        Log.warning(LOG_PREFIX, "websocket Error: " + t.toString(), t);
    }

//    /**
//     * Main method to test logic.
//     *
//     * @param args command-line arguments
//     */
//    public static void main(final String... args) {
//
//        final ContextMap map = ContextMap.getDefaultInstance();
//
//        final WebSiteProfile webProfile = map.getWebSiteProfile(Contexts.COURSE_HOST, Contexts.MPS_PATH);
//        if (webProfile != null) {
//            final DbContext dbCtx = webProfile.dbProfile.getDbContext(ESchemaUse.PRIMARY);
//
//            try {
//                final DbConnection conn = dbCtx.checkOutConnection();
//                final Cache cache = new Cache(webProfile.dbProfile, conn);
//
//                final MPSEndpoint end = new MPSEndpoint();
//
//                end.student = RawStudentLogic.query(cache, "836825053", false);
//
//                final LiveSessionInfo live = new LiveSessionInfo("ABCDEFG", "eid", ERole.STUDENT);
//                live.setUserInfo("836825053", "STEVE", "TEST-PROD", "STEVE TEST-PROD");
//
//                final ImmutableSessionInfo sess = new ImmutableSessionInfo(live);
//
//                final List<ExamCategory> avail = end.findAvailableExams(cache, sess);
//
//                for (final ExamCategory category : avail) {
//                    Log.info("Category: ", category.name);
//                    for (final ExamEntry exam : category.exams) {
//                        Log.info(" Exam: ", exam.examId);
//                    }
//                }
//            } catch (final SQLException ex) {
//                Log.warning(ex);
//            }
//        }
//    }

    /**
     * An entry in the list of exams for which the student is eligible.
     */
    static final class ExamEntry {

        /** The exam ID. */
        final String examId;

        /** The button label. */
        final String buttonLabel;

        /** An optional note to follow the button label. */
        final String note;

        /**
         * Constructs a new {@code ExamEntry}.
         *
         * @param theExamId      the exam ID
         * @param theButtonLabel the button label
         * @param theNote        an optional note to follow the button label
         */
        ExamEntry(final String theExamId, final String theButtonLabel, final String theNote) {

            this.examId = theExamId;
            this.buttonLabel = theButtonLabel;
            this.note = theNote;
        }
    }

    /**
     * A category of available exams.
     */
    static final class ExamCategory {

        /** The category name. */
        final String name;

        /** The available exams in this category. */
        final List<ExamEntry> exams;

        /**
         * Constructs a new {@code ExamCategory}.
         *
         * @param theName  the category name
         * @param theExams the list of available exams
         */
        ExamCategory(final String theName, final List<ExamEntry> theExams) {

            this.name = theName;
            this.exams = theExams;
        }
    }
}
