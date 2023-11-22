package dev.mathops.web.site.html.placementexam;

import dev.mathops.assessment.InstructionalCache;
import dev.mathops.assessment.document.template.AbstractDocObjectTemplate;
import dev.mathops.assessment.document.template.DocColumn;
import dev.mathops.assessment.document.template.DocParagraph;
import dev.mathops.assessment.document.template.DocText;
import dev.mathops.assessment.exam.ExamGradingCondition;
import dev.mathops.assessment.exam.ExamGradingRule;
import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.assessment.exam.ExamOutcome;
import dev.mathops.assessment.exam.ExamOutcomeAction;
import dev.mathops.assessment.exam.ExamOutcomePrereq;
import dev.mathops.assessment.exam.ExamOutcomeValidation;
import dev.mathops.assessment.exam.ExamProblem;
import dev.mathops.assessment.exam.ExamSection;
import dev.mathops.assessment.exam.ExamSubtest;
import dev.mathops.assessment.exam.ExamSubtestProblem;
import dev.mathops.assessment.formula.ErrorValue;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.htmlgen.ExamObjConverter;
import dev.mathops.assessment.htmlgen.ProblemConverter;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.assessment.variable.AbstractVariable;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.assessment.variable.VariableBoolean;
import dev.mathops.assessment.variable.VariableInteger;
import dev.mathops.assessment.variable.VariableReal;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.TemporalUtils;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.core.log.LogBase;
import dev.mathops.core.parser.xml.XmlEscaper;
import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.DbContext;
import dev.mathops.db.cfg.DbProfile;
import dev.mathops.db.cfg.ESchemaUse;
import dev.mathops.db.cfg.WebSiteProfile;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.logic.PlacementLogic;
import dev.mathops.db.logic.PlacementStatus;
import dev.mathops.db.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.rawlogic.RawExamLogic;
import dev.mathops.db.rawlogic.RawMpeCreditLogic;
import dev.mathops.db.rawlogic.RawMpeLogLogic;
import dev.mathops.db.rawlogic.RawMpecrDeniedLogic;
import dev.mathops.db.rawlogic.RawMpscorequeueLogic;
import dev.mathops.db.rawlogic.RawPendingExamLogic;
import dev.mathops.db.rawlogic.RawStmpeLogic;
import dev.mathops.db.rawlogic.RawStmpeqaLogic;
import dev.mathops.db.rawlogic.RawStsurveyqaLogic;
import dev.mathops.db.rawlogic.RawStudentLogic;
import dev.mathops.db.rawrecord.RawAdminHold;
import dev.mathops.db.rawrecord.RawExam;
import dev.mathops.db.rawrecord.RawMpeCredit;
import dev.mathops.db.rawrecord.RawMpeLog;
import dev.mathops.db.rawrecord.RawMpecrDenied;
import dev.mathops.db.rawrecord.RawPendingExam;
import dev.mathops.db.rawrecord.RawStmpe;
import dev.mathops.db.rawrecord.RawStmpeqa;
import dev.mathops.db.rawrecord.RawStsurveyqa;
import dev.mathops.db.rawrecord.RawStudent;
import dev.mathops.db.svc.term.TermRec;
import dev.mathops.session.ExamWriter;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.txn.handlers.AbstractHandlerBase;
import dev.mathops.session.txn.handlers.StudentExamAnswerRec;
import dev.mathops.session.txn.handlers.StudentExamRec;
import dev.mathops.session.txn.handlers.StudentSurveyAnswer;
import dev.mathops.session.txn.messages.AvailableExam;
import dev.mathops.session.txn.messages.GetExamReply;
import dev.mathops.web.site.html.HtmlSessionBase;

import javax.servlet.ServletRequest;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A user session used to take placement exams online. It takes as arguments a session ID, student name, and assignment
 * ID and presents the exam to the student.
 */
public final class PlacementExamSession extends HtmlSessionBase {

    /** The purge time duration (5 hours), in milliseconds. */
    private static final long PURGE_TIMEOUT = (long) (5 * 60 * 60 * 1000);

    /** True if the session is proctored. */
    public final boolean proctored;

    /** The achieved score. */
    private Integer score;

    /** The mastery score. */
    private Integer masteryScore;

    /** The state of the unit exam. */
    private EPlacementExamState state;

    /** The currently active profile page. */
    private int profilePage;

    /** The currently active section. */
    private int currentSect;

    /** The currently active item. */
    private int currentItem;

    /** Flag indicate exam has been started (controls button label on instructions). */
    private boolean started;

    /** An error encountered while grading the exam, null if none. */
    private String gradingError;

    /** Timestamp when exam will time out. */
    private long timeout;

    /** Timestamp when exam will time out. */
    private long purgeTime;

    /** Error message. */
    private String error;

    /** Profile responses. */
    private final String[] profileResponses;

    /**
     * Constructs a new {@code PlacementExamSession}. This is called when the user clicks a button to start a placement
     * exam. It stores data but does not generate the HTML until the page is actually generated.
     *
     * @param cache            the data cache
     * @param theSiteProfile   the site profile
     * @param theSessionId     the session ID
     * @param theStudentId     the student ID
     * @param isProctored      true if the session is proctored
     * @param theExamId        the exam ID being worked on
     * @param theRedirectOnEnd the URL to which to redirect at the end of the exam
     * @throws SQLException if there is an error accessing the database
     */
    public PlacementExamSession(final Cache cache, final WebSiteProfile theSiteProfile, final String theSessionId,
                                final String theStudentId, final boolean isProctored, final String theExamId,
                                final String theRedirectOnEnd) throws SQLException {

        super(cache, theSiteProfile, theSessionId, theStudentId, theExamId, theRedirectOnEnd);

        this.proctored = isProctored;
        this.state = EPlacementExamState.INITIAL;
        this.profilePage = 1;
        this.currentSect = -1;
        this.currentItem = -1;
        this.started = false;
        this.timeout = (long) 0L;
        this.purgeTime = System.currentTimeMillis() + PURGE_TIMEOUT;

        this.profileResponses = new String[15];
    }

    /**
     * Constructs a new {@code PlacementExamSession}. This is called when the user clicks a button to start a placement
     * exam. It stores data but does not generate the HTML until the page is actually generated.
     *
     * @param cache            the data cache
     * @param theSiteProfile   the website profile
     * @param theSessionId     the session ID
     * @param theStudentId     the student ID
     * @param isProctored      true if the session is proctored
     * @param theExamId        the exam ID being worked on
     * @param theRedirectOnEnd the URL to which to redirect at the end of the exam
     * @param theState         the session state
     * @param theScore         the score
     * @param theMastery       the mastery score
     * @param theStarted       true if exam has been started
     * @param theProfilePage   the current profile page
     * @param theSect          the current section
     * @param theItem          the current item
     * @param theTimeout       the timeout
     * @param thePurgeTime     the purge time
     * @param theExam          the exam
     * @param theError         the grading error
     * @throws SQLException if there is an error accessing the database
     */
    PlacementExamSession(final Cache cache, final WebSiteProfile theSiteProfile, final String theSessionId,
                         final String theStudentId, final boolean isProctored, final String theExamId,
                         final String theRedirectOnEnd, final EPlacementExamState theState, final Integer theScore,
                         final Integer theMastery, final boolean theStarted, final int theProfilePage,
                         final int theSect, final int theItem, final long theTimeout, final long thePurgeTime,
                         final ExamObj theExam, final String theError) throws SQLException {

        super(cache, theSiteProfile, theSessionId, theStudentId, theExamId, theRedirectOnEnd);

        this.proctored = isProctored;
        this.state = theState;
        this.score = theScore;
        this.masteryScore = theMastery;
        this.started = theStarted;
        this.profilePage = theProfilePage;
        this.currentSect = theSect;
        this.currentItem = theItem;
        this.timeout = theTimeout;
        this.purgeTime = Math.max(thePurgeTime, theTimeout + 60000L);
        setExam(theExam);
        this.gradingError = theError;

        this.profileResponses = new String[15];

        loadStudentInfo(cache);
    }

    /**
     * Gets the exam state.
     *
     * @return the exam state
     */
    public EPlacementExamState getState() {

        return this.state;
    }

//    /**
//     * Gets the currently active profile page.
//     *
//     * @return the profile page
//     */
//    public int getProfilePage() {
//
//        return this.profilePage;
//    }

    /**
     * Gets the currently active section.
     *
     * @return the section
     */
    public int getCurrentSect() {

        return this.currentSect;
    }

    /**
     * Gets the currently active item.
     *
     * @return the item
     */
    public int getCurrentItem() {

        return this.currentItem;
    }

    /**
     * Tests whether the exam is started.
     *
     * @return {@code true} if the exam is started
     */
    public boolean isStarted() {

        return this.started;
    }

    /**
     * Gets the time remaining in the exam.
     *
     * @return the time remaining, in milliseconds (0 if the exam has not been started)
     */
    public long getTimeRemaining() {

        return this.timeout == 0L ? 0L : this.timeout - System.currentTimeMillis();
    }

    /**
     * Gets the time until the exam will be purged.
     *
     * @return the time before purge (milliseconds)
     */
    public long getTimeUntilPurge() {

        return this.purgeTime - System.currentTimeMillis();
    }

    /**
     * Gets the error message associated with the session.
     *
     * @return the error message
     */
    public String getError() {

        return this.error;
    }

    /**
     * Tests whether this session is timed out.
     *
     * @return {@code true} if timed out
     */
    public boolean isTimedOut() {

        return this.timeout > 0L && System.currentTimeMillis() >= this.timeout;
    }

    /**
     * Tests whether this session can be purged.
     *
     * @return {@code true} if session can be purged
     */
    boolean isPurgable() {

        return System.currentTimeMillis() >= this.purgeTime;
    }

    /**
     * Generates HTML for the exam based on its current state.
     *
     * @param cache the data cache
     * @param now   the date/time to consider "now"
     * @param req   the servlet request
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    public void generateHtml(final Cache cache, final ZonedDateTime now, final ServletRequest req,
                             final HtmlBuilder htm) throws SQLException {

        switch (this.state) {
            case INITIAL:
                doInitial(cache, now, htm);
                break;

            case ERROR:
                appendErrorHtml(htm);
                break;

            case PROFILE:
                appendProfileHtml(htm);
                break;

            case INSTRUCTIONS:
                appendInstructionsHtml(htm);
                break;

            case ITEM_NN:
                appendExamHtml(req, htm);
                break;

            case SUBMIT_NN:
                appendSubmitConfirm(htm);
                break;

            case COMPLETED:
                appendCompletedHtml(htm);
                break;

            default:
                appendHeader(htm);
                htm.addln("<div style='text-align:center; color:navy;'>");
                htm.addln("Unsupported state.");
                htm.eDiv();
                appendFooter(htm, "close", "Close", null, null, null, null);
                break;
        }
    }

    /**
     * Processes a request for the page while in the INITIAL state, which generates the assignment, then sends its
     * HTML.
     *
     * @param cache the data cache
     * @param now   the date/time to consider "now"
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private void doInitial(final Cache cache, final ZonedDateTime now, final HtmlBuilder htm) throws SQLException {

        // Look up the exam and store it in an AvailableExam object.
        final AvailableExam avail = new AvailableExam();

        Log.info("Retrieving exam ", this.version);

        avail.exam = RawExamLogic.query(cache, this.version);

        if (avail.exam == null) {
            Log.warning("Exam ", this.version, " was not found by ExamCache");
            this.error = "There was no assessment found with the requested version.";
        } else {
            final String type = avail.exam.examType;
            if ("Q".equals(type)) {

                this.error = loadStudentInfo(cache);
                if (this.error == null) {
                    try {
                        final GetExamReply reply = new GetExamReply();
                        LogBase.setSessionInfo("TXN", this.studentId);

                        // We need to verify the exam and fill in the remaining fields in
                        // AvailableExam
                        final List<RawAdminHold> holds = new ArrayList<>(1);
                        reply.status = GetExamReply.SUCCESS;

                        final PlacementStatus placementStat = new PlacementLogic(cache, getStudent().stuId,
                                getStudent().aplnTerm, now).status;

                        final boolean eligible = placementStat.attemptsRemaining > 0;

                        if (eligible) {
                            // Generate a serial number for the exam
                            final long serial = AbstractHandlerBase.generateSerialNumber(false);

                            buildPresentedExam(avail.exam.treeRef, serial, reply, this.active);

                            if (reply.presentedExam == null) {
                                this.error = "The system was unable to create your session: " + reply.error;
                            } else {
                                final DocColumn newInstr = new DocColumn();
                                newInstr.tag = "instructions";

                                final DocParagraph para = new DocParagraph();
                                para.setColorName("navy");

                                para.add(new DocText("Instructions:"));
                                newInstr.add(para);

                                // Log the fact that a placement exam was begun
                                final LocalDateTime start = TemporalUtils.toLocalDateTime(
                                        Instant.ofEpochMilli(reply.presentedExam.realizationTime));
                                final int startTime = start.getHour() * 60 + start.getMinute();

                                final RawMpeLog mpelog = new RawMpeLog(this.studentId, this.active.academicYear,
                                        avail.exam.course, avail.exam.version, start.toLocalDate(), null, null,
                                        Long.valueOf(serial), Integer.valueOf(startTime), null);

                                RawMpeLogLogic.INSTANCE.insert(cache, mpelog);

                                // Apply time limit factor adjustment
                                if (reply.presentedExam.allowedSeconds != null) {

                                    final RawStudent stu = RawStudentLogic.query(cache, this.studentId, false);

                                    if (stu != null && stu.timelimitFactor != null) {
                                        avail.timelimitFactor = stu.timelimitFactor;

                                        int secs = reply.presentedExam.allowedSeconds.intValue();

                                        secs = (int) ((double) secs * stu.timelimitFactor.doubleValue());

                                        reply.presentedExam.allowedSeconds = Long.valueOf((long) secs);
                                    }
                                }

                                // TODO: Populate holds list...

                                if (!holds.isEmpty()) {
                                    final int numHolds = holds.size();
                                    reply.holds = new String[numHolds];
                                    for (int i = 0; i < numHolds; ++i) {
                                        reply.holds[i] = RawAdminHoldLogic.getStudentMessage(holds.get(i).holdId);
                                    }
                                }

                                final LocalDateTime realized = TemporalUtils.toLocalDateTime(
                                        Instant.ofEpochMilli(reply.presentedExam.realizationTime));
                                final LocalTime tm = realized.toLocalTime();
                                final int min = tm.getHour() * 60 + tm.getMinute();

                                final RawPendingExam pending = new RawPendingExam(Long.valueOf(serial),
                                        reply.presentedExam.examVersion, this.studentId, realized.toLocalDate(), null,
                                        Integer.valueOf(min), null, null, null, null, avail.exam.course,
                                        avail.exam.unit, avail.exam.examType, avail.timelimitFactor, "STU");

                                RawPendingExamLogic.INSTANCE.insert(cache, pending);
                            }
                        }
                    } catch (final SQLException ex) {
                        Log.warning(ex);
                        this.error = "Unable to query placement status.";
                    }
                } else {
                    Log.warning("Assessment version '", this.version, "' is not a placement assessment.");
                    this.error = "Requested assessment is not a placement assessment.";
                }
            }
        }

        if (this.error == null) {
            getExam().presentationTime = System.currentTimeMillis();
            this.state = EPlacementExamState.PROFILE;
            appendProfileHtml(htm);
        } else {
            this.state = EPlacementExamState.ERROR;
            Log.warning(this.error);
            appendErrorHtml(htm);
        }
    }

    /**
     * Attempt to construct a realized exam and install it in the reply message. On errors, the reply message errors
     * field will be set to the cause of the error.
     *
     * @param ref    the reference to the exam to be loaded
     * @param serial the serial number to associate with the exam
     * @param reply  the reply message to populate with the realized exam or the error status
     * @param term   the term under which to file the presented exam
     */
    private void buildPresentedExam(final String ref, final long serial, final GetExamReply reply, final TermRec term) {

        final ExamObj theExam = InstructionalCache.getExam(ref);

        if (theExam == null) {
            reply.status = GetExamReply.CANNOT_LOAD_EXAM_TEMPLATE;
            Log.warning("Unable to load template for " + ref);
        } else if (theExam.ref == null) {
            reply.status = GetExamReply.CANNOT_LOAD_EXAM_TEMPLATE;
            Log.warning("Errors loading assessment template");
        } else {

            // Now we must add the exam's problems, so it can be realized.
            final int numSect = theExam.getNumSections();

            for (int onSect = 0; onSect < numSect; ++onSect) {
                final ExamSection esect = theExam.getSection(onSect);
                final int numProb = esect.getNumProblems();

                for (int onProb = 0; onProb < numProb; ++onProb) {
                    final ExamProblem eprob = esect.getProblem(onProb);

                    final int num = eprob.getNumProblems();

                    for (int i = 0; i < num; ++i) {
                        AbstractProblemTemplate prb = eprob.getProblem(i);

                        if (prb == null || prb.ref == null) {
                            Log.warning("Exam " + ref + " section " + onSect + " problem " + onProb + " choice " + i
                                    + " getProblem() returned " + prb);
                        } else {
                            prb = InstructionalCache.getProblem(prb.ref);

                            if (prb != null) {
                                eprob.setProblem(i, prb);
                            }
                        }
                    }
                }
            }

            final RawStudent stu = getStudent();
            Log.info("Testing for time limit update: ", theExam.allowedSeconds);

            if (theExam.allowedSeconds != null) {
                int secs = theExam.allowedSeconds.intValue();

                if (stu.timelimitFactor != null) {
                    Log.info("Applying time limit factor : ", stu.timelimitFactor);

                    secs = (int) ((double) secs * stu.timelimitFactor.doubleValue());
                    theExam.allowedSeconds = Long.valueOf((long) secs);

                    Log.info("After time limit update: ", theExam.allowedSeconds);
                }
            }

            if (theExam.realize(true, true, serial)) {
                reply.presentedExam = theExam;
                reply.status = GetExamReply.SUCCESS;
                reply.studentId = this.studentId;
                setExam(theExam);

                if (!new ExamWriter().writePresentedExam(this.studentId, term, reply.presentedExam, reply.toXml())) {
                    Log.warning("Unable to cache exam " + ref);
                    reply.presentedExam = null;
                    reply.status = GetExamReply.CANNOT_REALIZE_EXAM;
                }
            } else {
                Log.warning("Unable to realize " + ref);
                reply.status = GetExamReply.CANNOT_REALIZE_EXAM;
            }
        }

        if (reply.status == GetExamReply.SUCCESS) {
            reply.presentedExam = getExam();
        }
    }

    /**
     * Appends the HTML indicating an error.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private void appendErrorHtml(final HtmlBuilder htm) {

        htm.div("gap3");

        htm.sDiv("indent33");
        htm.sP().add("We were unable to initialize the Math Placement Tool", CoreConstants.COLON).eP();
        htm.sP().add(this.error).eP();
        htm.eDiv();

        htm.add("<input type='hidden' name='action' value='close'/>");
        htm.addln("<div style='margin:1px 0 0 40pt; padding:6pt; text-align:left;'>");
        htm.add("  <input type='submit' value='Close'/>");
        htm.eDiv();
    }

    /**
     * Appends the HTML for the exam profile.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private void appendProfileHtml(final HtmlBuilder htm) {

        htm.sDiv("indent11");
        htm.sP().add("Before you begin, we ask that you take a moment to provide some background information...").eP();
        htm.addln("<hr style='height:1px; border:0; background:#b3b3b3; margin:0 0 6pt 0;'>");

        htm.sDiv("indent11");

        if (this.profilePage == 1) {
            emitProfilePage1(htm);
        } else if (this.profilePage == 2) {
            emitProfilePage2(htm);
        } else if (this.profilePage == 3) {
            emitProfilePage3(htm);
        }

        htm.eDiv();
        htm.eDiv();
    }

    /**
     * Emits the HTML for profile page 1.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitProfilePage1(final HtmlBuilder htm) {

        // TODO: support multiple columns - responsively...

        htm.sH(3).add("Preparation").eH(3);

        // Question 1
        htm.sP().add("How much time did you spend preparing for the Math Placement Tool?").eP();

        htm.sP("indent2");
        htm.add("<input type='radio' id='q1_1' name='q1' value='1' onclick='disable_question2()'/> ");
        htm.addln("<label for='q1_1'>None at all</label>").br();
        htm.add("<input type='radio' id='q1_2' name='q1' value='2' onclick='enable_question2()'/> ");
        htm.addln("<label for='q1_2'>Less than 2 hours</label>").br();
        htm.add("<input type='radio' id='q1_3' name='q1' value='3' onclick='enable_question2()'/> ");
        htm.addln("<label for='q1_3'>2 to 5 hours</label>").br();
        htm.add("<input type='radio' id='q1_4' name='q1' value='4' onclick='enable_question2()'/> ");
        htm.addln("<label for='q1_4'>5 to 10 hours</label>").br();
        htm.add("<input type='radio' id='q1_5' name='q1' value='5' onclick='enable_question2()'/> ");
        htm.addln("<label for='q1_5'>More than 10 hours</label>").br();
        htm.eP();

        // Question 2
        htm.addln("<div id='survey_q2' style='display:none;'>");
        htm.sP().add("What resources did you use to prepare? (Mark all that apply.)").eP();
        htm.sP("indent2");
        htm.add("<input type='checkbox' id='q2_1' name='q2_1' value='1' disabled onclick='enable_profile_next()'/> ");
        htm.addln("<label for='q2_1'>The study guide on the Math Department web site</label>").br();
        htm.add("<input type='checkbox' id='q2_2' name='q2_2' value='2' disabled onclick='enable_profile_next()'/> ");
        htm.addln("<label for='q2_2'>Tutors</label>").br();
        htm.add("<input type='checkbox' id='q2_3' name='q2_3' value='3' disabled onclick='enable_profile_next()'/> ");
        htm.addln("<label for='q2_3'>Textbook(s)</label>").br();
        htm.add("<input type='checkbox' id='q2_4' name='q2_4' value='4' disabled onclick='enable_profile_next()'/> ");
        htm.addln("<label for='q2_4'>Materials from past math courses</label>").br();
        htm.eP();
        htm.eDiv();

        htm.addln("<div style='margin:1px 0 0 40pt; padding:6pt; text-align:left;'>");
        htm.add("  <input type='submit' id='goto2' name='goto2' value='Next...' disabled/>");
        htm.eDiv();

        htm.addln("<script>");
        htm.addln("function enable_profile_next() {");
        htm.addln("  document.getElementById(\"goto2\").disabled = false;");
        htm.addln("}");
        htm.addln("function disable_question2() {");
        htm.addln("  document.getElementById(\"survey_q2\").style.display = \"none\";");
        htm.addln("  document.getElementById(\"q2_1\").disabled = true;");
        htm.addln("  document.getElementById(\"q2_2\").disabled = true;");
        htm.addln("  document.getElementById(\"q2_3\").disabled = true;");
        htm.addln("  document.getElementById(\"q2_4\").disabled = true;");
        htm.addln("  enable_profile_next();");
        htm.addln("}");
        htm.addln("function enable_question2() {");
        htm.addln("  document.getElementById(\"survey_q2\").style.display = \"\";");
        htm.addln("  document.getElementById(\"q2_1\").disabled = false;");
        htm.addln("  document.getElementById(\"q2_2\").disabled = false;");
        htm.addln("  document.getElementById(\"q2_3\").disabled = false;");
        htm.addln("  document.getElementById(\"q2_4\").disabled = false;");
        htm.addln("}");
        htm.addln("</script>");
    }

    /**
     * Emits the HTML for profile page 2.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitProfilePage2(final HtmlBuilder htm) {

        htm.sH(3).add("Mathematics Background").eH(3);

        // Question 1
        htm.sP().add("How long has it been since you completed your last math class?").eP();
        htm.sP("indent2");
        htm.add("<input type='radio' id='q3_1' name='q3' value='1' onclick='enable_question4()'/> ");
        htm.addln("<label for='q3_1'>Currently enrolled</label>").br();
        htm.add("<input type='radio' id='q3_2' name='q3' value='2' onclick='enable_question4()'/> ");
        htm.addln("<label for='q3_2'>Less than 3 months</label>").br();
        htm.add("<input type='radio' id='q3_3' name='q3' value='3' onclick='enable_question4()'/> ");
        htm.addln("<label for='q3_3'>3 to 9 months</label>").br();
        htm.add("<input type='radio' id='q3_4' name='q3' value='4' onclick='enable_question4()'/> ");
        htm.addln("<label for='q3_4'>9 months to 2 years</label>").br();
        htm.add("<input type='radio' id='q3_5' name='q3' value='5' onclick='enable_question4()'/> ");
        htm.addln("<label for='q3_5'>2 to 5 years</label>").br();
        htm.add("<input type='radio' id='q3_6' name='q3' value='6' onclick='enable_question4()'/> ");
        htm.addln("<label for='q3_6'>More than 5 years</label").br();
        htm.eP();

        // Question 2
        htm.addln("<div id='survey_q4' style='display:none;'>");
        htm.sP().add("What final grade have you typically earned in the math classes you have taken?").eP();
        htm.sP("indent2");
        htm.add("<input type='radio' id='q4_1' name='q4' value='1' disabled onclick='enable_profile_next()'/> ");
        htm.addln("<label for='q4_1'>A</label>").br();
        htm.add("<input type='radio' id='q4_2' name='q4' value='2' disabled onclick='enable_profile_next()'/> ");
        htm.addln("<label for='q4_2'>A or B</label>").br();
        htm.add("<input type='radio' id='q4_3' name='q4' value='3' disabled onclick='enable_profile_next()'/> ");
        htm.addln("<label for='q4_3'>B</label>").br();
        htm.add("<input type='radio' id='q4_4' name='q4' value='4' disabled onclick='enable_profile_next()'/> ");
        htm.addln("<label for='q4_4'>B or C</label>").br();
        htm.add("<input type='radio' id='q4_5' name='q4' value='5' disabled onclick='enable_profile_next()'/> ");
        htm.addln("<label for='q4_5'>C</label>").br();
        htm.add("<input type='radio' id='q4_6' name='q4' value='6' disabled onclick='enable_profile_next()'/> ");
        htm.addln("<label for='q4_6'>C- or lower</label>").br();
        htm.eP();
        htm.eDiv();

        htm.addln("<div style='margin:1px 0 0 40pt; padding:6pt; text-align:left;'>");
        htm.add("  <input type='submit' id='goto3' name='goto3' value='Next...' disabled/>");
        htm.eDiv();

        htm.addln("<script>");
        htm.addln("function enable_profile_next() {");
        htm.addln("  document.getElementById(\"goto3\").disabled = false;");
        htm.addln("}");
        htm.addln("function enable_question4() {");
        htm.addln("  document.getElementById(\"survey_q4\").style.display = \"\";");
        htm.addln("  document.getElementById(\"q4_1\").disabled = false;");
        htm.addln("  document.getElementById(\"q4_2\").disabled = false;");
        htm.addln("  document.getElementById(\"q4_3\").disabled = false;");
        htm.addln("  document.getElementById(\"q4_4\").disabled = false;");
        htm.addln("  document.getElementById(\"q4_5\").disabled = false;");
        htm.addln("  document.getElementById(\"q4_6\").disabled = false;");
        htm.addln("}");
        htm.addln("</script>");
    }

    /**
     * Emits the HTML for profile page 3.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitProfilePage3(final HtmlBuilder htm) {

        htm.sH(3).add("High School Mathematics Courses").eH(3);

        // Question 1
        htm.sP().add("What math classes did you complete during high school? (Mark all that apply)").eP();

        htm.sP("indent2");
        htm.add("<input type='checkbox' id='q5_1' name='q5_1' value='1' onclick='enable_question6()'/> ");
        htm.addln("<label for='q5_1'>Algebra I</label>").br();

        htm.add("<input type='checkbox' id='q5_2' name='q5_2' value='2' onclick='enable_question6()'/> ");
        htm.addln("<label for='q5_2'>Geometry</label>").br();

        htm.add("<input type='checkbox' id='q5_3' name='q5_3' value='3' onclick='enable_question6()'/> ");
        htm.addln("<label for='q5_3'>Algebra II</label>").br();

        htm.add("<input type='checkbox' id='q5_4' name='q5_4' value='4' onclick='enable_question6()'/> ");
        htm.addln("<label for='q5_4'>Integrated Math I</label>").br();

        htm.add("<input type='checkbox' id='q5_5' name='q5_5' value='5' onclick='enable_question6()'/> ");
        htm.addln("<label for='q5_5'>Integrated Math II</label>").br();

        htm.add("<input type='checkbox' id='q5_6' name='q5_6' value='6' onclick='enable_question6()'/> ");
        htm.addln("<label for='q5_6'>Integrated Math III</label>").br();

        htm.add("<input type='checkbox' id='q5_7' name='q5_7' value='7' onclick='enable_question6()'/> ");
        htm.addln("<label for='q5_7'>Precalculus or Trigonometry</label>").br();

        htm.add("<input type='checkbox' id='q5_8' name='q5_8' value='8' onclick='enable_question6()'/> ");
        htm.addln("<label for='q5_8'>Calculus</label>").br();

        htm.add("<input type='checkbox' id='q5_9' name='q5_9' value='9' onclick='enable_question6()'/> ");
        htm.addln("<label for='q5_9'>Other high school math class</label>").br();

        // Question 2
        htm.addln("<div id='survey_q6' style='display:none;'>");
        htm.sP().add("Have you completed any College math courses? (Mark all that apply)").eP();

        htm.sP("indent2");
        htm.add("<input type='checkbox' id='q6_1' name='q6_1' value='1'/> ");
        htm.addln("<label for='q6_1'>College Math for Liberal Arts</label>").br();

        htm.add("<input type='checkbox' id='q6_2' name='q6_2' value='2'/> ");
        htm.addln("<label for='q6_2'>College Elementary Algebra</label>").br();

        htm.add("<input type='checkbox' id='q6_3' name='q6_3' value='3'/> ");
        htm.addln("<label for='q6_3'>College Intermediate Algebra</label>").br();

        htm.add("<input type='checkbox' id='q6_4' name='q6_4' value='4'/> ");
        htm.addln("<label for='q6_4'>College Algebra</label>").br();

        htm.add("<input type='checkbox' id='q6_5' name='q6_5' value='5'/> ");
        htm.addln("<label for='q6_5'>College Precalculus or Trigonometry</label>").br();

        htm.add("<input type='checkbox' id='q6_6' name='q6_6' value='6'/> ");
        htm.addln("<label for='q6_6'>College Calculus</label>").br();

        htm.add("<input type='checkbox' id='q6_7' name='q6_7' value='7'/> ");
        htm.addln("<label for='q6_7'>Other college math class</label>").br();
        htm.eP();
        htm.eDiv();

        htm.addln("<div style='margin:1px 0 0 40pt; padding:6pt; text-align:left;'>");
        htm.add("  <input type='submit' id='instruct' name='instruct' disabled value='Finish...'/>");
        htm.eDiv();

        htm.addln("<script>");
        htm.addln("function enable_question6() {");
        htm.addln("  document.getElementById(\"instruct\").disabled = false;");
        htm.addln("  document.getElementById(\"survey_q6\").style.display = \"\";");
        htm.addln("}");
        htm.addln("</script>");
    }

    /**
     * Appends the HTML for the exam instructions.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private void appendInstructionsHtml(final HtmlBuilder htm) {

        appendHeader(htm);
        appendNav(htm, false);
        startMain(htm);

        if (getExam().instructionsHtml == null && getExam().instructions != null) {
            ExamObjConverter.populateExamHtml(getExam(), new int[]{1});
        }

        if (getExam().instructionsHtml != null) {
            htm.addln(getExam().instructionsHtml);
        }

        endMain(htm);

        if (this.started) {
            appendFooter(htm, "score", "I am finished.  Generate my placement results.", null, null, "nav_0_0",
                    "Go to Question 1");
        } else {
            appendFooter(htm, "nav_0_0", "Begin...", null, null, null, null);
        }
    }

    /**
     * Appends the HTML for the exam, showing the current item.
     *
     * @param req the servlet request
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private void appendExamHtml(final ServletRequest req, final HtmlBuilder htm) {

        appendHeader(htm);
        appendNav(htm, false);
        startMain(htm);

        final ExamSection sect;

        if (this.currentItem == -1) {
            sect = getExam().getSection(0);
        } else {
            sect = getExam().getSection(this.currentSect);
            final ExamProblem ep = sect.getPresentedProblem(this.currentItem);
            if (ep == null) {
                htm.addln("No presented problem.");
            } else {
                final AbstractProblemTemplate p = ep.getSelectedProblem();
                if (p == null) {
                    htm.addln("No selected problem.");
                } else {
                    if (p.questionHtml == null) {
                        ProblemConverter.populateProblemHtml(p, new int[]{1});
                    }
                    htm.addln(p.insertAnswers(p.questionHtml));
                }
            }
        }

        endMain(htm);

        final String scrollTop = req.getParameter("nav-scroll-top");
        if (scrollTop != null && !scrollTop.isEmpty()) {
            try {
                final double st = Double.parseDouble(scrollTop);

                htm.addln("<script>");
                htm.addln("  document.getElementById(\"exam-outline-nav\").scrollTop = " + st + ";");
                htm.addln("  let sel = document.getElementById(\"selected_menu_item\");");
                htm.addln("  if (sel) {");
                htm.addln("      sel.scrollIntoView();");
                htm.addln("  }");
                htm.addln("</script>");

            } catch (final NumberFormatException ex) {
                Log.warning("Failed to parse nav-scroll-top=", scrollTop, ex);
            }
        }

        final String prevCmd;
        final String prevLbl;

        if (this.currentItem == 0) {
            if (this.currentSect == 0) {
                prevCmd = null;
                prevLbl = null;
            } else {
                final ExamSection psect = getExam().getSection(this.currentSect - 1);
                if (psect == null) {
                    prevCmd = null;
                    prevLbl = null;
                } else {
                    prevCmd = "nav_" + (this.currentSect - 1) + "_" + (psect.getNumProblems() - 1);
                    prevLbl = psect.getPresentedProblem(psect.getNumProblems() - 1).problemName;
                }
            }
        } else {
            prevCmd = "nav_" + this.currentSect + "_" + (this.currentItem - 1);
            prevLbl = sect.getPresentedProblem(this.currentItem - 1).problemName;
        }

        final String nextCmd;
        final String nextLbl;

        if (this.currentItem >= (sect.getNumProblems() - 1)) {

            if (this.currentSect == getExam().getNumSections() - 1) {
                nextCmd = null;
                nextLbl = null;
            } else {
                final ExamSection nsect = getExam().getSection(this.currentSect + 1);
                if (nsect == null) {
                    nextCmd = null;
                    nextLbl = null;
                } else {
                    nextCmd = "nav_" + (this.currentSect + 1) + "_0";
                    nextLbl = nsect.getPresentedProblem(0).problemName;
                }
            }
        } else {
            nextCmd = "nav_" + this.currentSect + "_" + (this.currentItem + 1);
            nextLbl = sect.getPresentedProblem(this.currentItem + 1).problemName;
        }

        endMain(htm);
        appendFooter(htm, "score", "I am finished.  Generate my placement results.", prevCmd,
                "Go to Question " + prevLbl, nextCmd, "Go to Question " + nextLbl);
    }

    /**
     * Appends the HTML for a message asking the use to confirm submission.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private void appendSubmitConfirm(final HtmlBuilder htm) {

        appendHeader(htm);
        appendNav(htm, true);
        startMain(htm);

        int total = 0;
        int numAnswered = 0;

        final int numSections = getExam().getNumSections();
        for (int sectInt = 0; sectInt < numSections; ++sectInt) {
            final ExamSection sect = getExam().getSection(sectInt);

            final int numProblems = sect.getNumProblems();
            for (int i = 0; i < numProblems; ++i) {
                final ExamProblem ep = sect.getProblem(i);
                ++total;
                if (ep.getSelectedProblem().isAnswered()) {
                    ++numAnswered;
                }
            }
        }

        htm.addln("<div style='margin:20pt; padding:20pt; border:1px solid black; ",
                "background:white; text-align:center; color:Green;'>");

        if (numAnswered == total) {
            htm.addln("<span style='color:green'>");
            htm.addln("You have answered all ", Integer.toString(total), " questions.").br().br();
        } else {
            htm.addln("<span style='color:FireBrick'>");
            htm.addln("You have only answered ", Integer.toString(numAnswered), " out of ",
                    Integer.toString(total), " questions.").br().br();
        }

        htm.addln("Submit your work and generate your placement results?").br().br();
        htm.add("  <input class='smallbtn' type='submit' name='Y' value='Yes - Submit my work'/> &nbsp;");
        htm.add("  <input class='smallbtn' type='submit' name='N' value='No - I will continue working'/>");

        htm.eSpan().eDiv();

        endMain(htm);
        appendEmptyFooter(htm);
    }

    /**
     * Appends the HTML displaying result after the assignment is completed.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private void appendCompletedHtml(final HtmlBuilder htm) {

        appendHeader(htm);
        appendNav(htm, true);
        startMain(htm);

        htm.addln("<div style='margin:20pt; padding:20pt; border:1px solid black; ",
                "background:white; text-align:center; color:Green;'>");

        htm.addln("<span style='color:green;'>");
        htm.addln("Math Placement Tool completed.");
        htm.br().br();

        htm.eSpan().eDiv();

        endMain(htm);
        appendFooter(htm, "close", "Close", null, null, null, null);
    }

    // AliceBlue --> #ECECE4
    // SteelBlue --> #1E4D2B

    /**
     * Appends the header.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private void appendHeader(final HtmlBuilder htm) {

        htm.sDiv(null, "style='display:flex; flex-flow:row wrap; margin:0 6px 12px 6px;'");

        htm.sDiv(null, "style='flex: 1 100%; display:block; "
                        + "background-color:#ECECE4; border-top:1px solid #1E4D2B; ",
                "border-bottom:1px solid #1E4D2B; margin:1px;'");

        htm.add("<h1 style='text-align:center; font-family:sans-serif; font-size:18pt; ",
                "font-weight:bold; color:#1E4D2B; text-shadow:2px 1px #ccc; padding:4pt;",
                "margin-bottom:2px;'>");
        htm.add(getExam().examName);
        htm.eH(1);

        // Countdown timer - re-synchronized on each refresh
        htm.addln("<hr style='height:1px; border:0; background:#b3b3b3; margin:0 1%;'>");
        htm.addln("<div style='text-align:center;margin-top:2px;'>");

        if (this.timeout > 0L && (this.state == EPlacementExamState.INSTRUCTIONS
                || this.state == EPlacementExamState.SUBMIT_NN
                || this.state == EPlacementExamState.ITEM_NN)) {

            final long now = System.currentTimeMillis();

            if (this.timeout <= now) {
                htm.add("Time Expired.");

                // Exam should auto-submit - add an onLoad handler to the form
                htm.addln("<script>");
                htm.addln("  window.addEventListener(\"load\", timeoutSubmit, false);");
                htm.addln("  function timeoutSubmit() {");
                htm.addln("    document.getElementById(\"placement_exam_act\").value=\"timeout\";");
                htm.addln("    document.getElementById(\"placement_exam_form\").submit();");
                htm.addln("  }");
            } else {
                final int sec = (int) Math.round((double) (this.timeout - now) / 1000.0);
                final int ss = sec % 60;
                final int mm = sec / 60 % 60;
                final int hr = sec / 3600;

                htm.add("Time Remaining: ");
                htm.add("<code id='unit-exam-timer'>");
                htm.add(hr).add(':');
                if (mm > 9) {
                    htm.add(mm).add(':');
                } else {
                    htm.add('0').add(mm).add(':');
                }
                if (ss > 9) {
                    htm.add(ss);
                } else {
                    htm.add('0').add(ss);
                }
                htm.add("</code>");

                final long remain = Math.max(0L, this.timeout - now);

                // Start a script that will update the timer

                htm.addln("<script>");
                htm.addln(" let end = new Date().getTime() + ",
                        Long.toString(remain), ";");

                htm.addln(" setInterval(function() {");
                htm.addln("   let now = new Date().getTime();");
                htm.addln("   let sec = Math.round((end - now + 500) / 1000);");
                htm.addln("   if (sec <= 0) {");
                htm.addln(
                        "     document.getElementById(\"unit-exam-timer\").innerHTML=\"00:00:00\";");
                htm.addln(
                        "     document.getElementById(\"placement_exam_act\").value=\"timeout\";");
                htm.addln("     document.getElementById(\"placement_exam_form\").submit();");
                htm.addln("   } else {");
                htm.addln("     let ss = sec % 60;");
                htm.addln("     sec = sec - ss;");
                htm.addln("     let mm = (sec / 60) % 60;");
                htm.addln("     sec = sec - mm * 60;");
                htm.addln("     let hh = sec / 3600;");
                htm.addln("     if (hh > 0) {");
                htm.addln("       document.getElementById(\"unit-exam-timer\").innerHTML=",
                        "hh + \":\" + (\"0\" + mm).slice(-2) + \":\" + (\"0\" + ss).slice(-2);");
                htm.addln("   } else {");
                htm.addln("       document.getElementById(\"unit-exam-timer\").innerHTML=",
                        "(\"0\" + mm).slice(-2) + \":\" + (\"0\" + ss).slice(-2);");
                htm.addln("     }");
                htm.addln("   }");
                htm.addln(" }, 1000);");
            }
            htm.addln("</script>");
        } else if (this.state == EPlacementExamState.COMPLETED) {
            htm.addln("&nbsp;");
        } else if (getExam().allowedSeconds == null) {
            htm.addln("This exam has no time limit.");
        } else {
            final int sec = (int) getExam().allowedSeconds.longValue();
            final int ss = sec % 60;
            final int mm = sec / 60 % 60;
            final int hr = sec / 3600;

            htm.add("Time Limit: ");

            boolean comma = false;
            if (hr > 0) {
                htm.add(hr).add(hr > 1 ? " hours" : " hour");
                comma = true;
            }

            if (mm > 0) {
                if (comma) {
                    htm.add(", ");
                }
                htm.add(mm).add(mm > 1 ? " minutes" : " minute");
                comma = true;
            }

            if (ss > 0) {
                if (comma) {
                    htm.add(", ");
                }
                htm.add(ss).add(mm > 1 ? " seconds" : " second");
            }
        }
        htm.eDiv();
        htm.eDiv();
    }

    /**
     * Starts the "main" section.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private void startMain(final HtmlBuilder htm) {

        htm.addln("<main style='flex:1 1 73%; margin:1px; padding:2px; ",
                "border:1px solid #1E4D2B; max-height: calc(100vh - 250px);'>");

        htm.addln(" <input type='hidden' name='currentSect' value='", Integer.toString(this.currentSect), "'>");
        htm.addln(" <input type='hidden' name='currentItem' value='", Integer.toString(this.currentItem), "'>");

        htm.addln("<div style='padding:8px; min-height:100%; border:1px solid #b3b3b3; ",
                "background:white; font-family:\"Times New Roman\",Times,serif; font-size:",
                Float.toString(AbstractDocObjectTemplate.DEFAULT_BASE_FONT_SIZE),
                "px; max-height: calc(100vh - 256px); overflow-x:hidden; overflow-y:scroll;'>");
    }

    /**
     * Ends the "main" section.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void endMain(final HtmlBuilder htm) {

        htm.eDiv();
        htm.addln("</main>");
    }

    /**
     * Appends the navigation section.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param disabled {@code true} to disable the controls
     */
    private void appendNav(final HtmlBuilder htm, final boolean disabled) {

        htm.add("<input type='hidden' id='nav-scroll-top' name='nav-scroll-top'/>");

        htm.addln("<script>");

        htm.addln("function invokeAct(action) {");
        htm.addln("  document.getElementById('placement_exam_act').value = action;");
        htm.addln("  document.getElementById('placement_exam_form').submit();");
        htm.addln("}");

        htm.addln("function navScrolled() {");
        htm.addln("  document.getElementById(\"nav-scroll-top\").value = ",
                "document.getElementById(\"exam-outline-nav\").scrollTop;");
        htm.addln("}");

        htm.addln("</script>");

        htm.addln("<nav id='exam-outline-nav' style='flex:1 1 22%; display:block; ",
                "background-color:#F9F9F0; border:1px solid #1E4D2B; margin:1px; ",
                "padding:8px 0 8px 8px; font-size:13pt; max-height:calc(100vh - 250px); ",
                "height: calc(100vh - 250px); overflow-x:hidden; overflow-y:scroll;' ",
                "onscroll='navScrolled();'>");

        if (this.state == EPlacementExamState.INSTRUCTIONS) {
            htm.addln("<div id='selected_menu_item' style='background:#7FFF7F;'>");
        } else {
            htm.sDiv();
        }

        htm.add("<a class='ulink' style='font-family:\"Times New Roman\",Times,serif;'");
        if (!disabled) {
            htm.addln(" href='javascript:invokeAct(\"instruct\");'");
        }
        htm.addln(">Instructions</a>");
        htm.eDiv();

        final int numSections = getExam().getNumSections();
        for (int sectIndex = 0; sectIndex < numSections; ++sectIndex) {
            final ExamSection sect = getExam().getSection(sectIndex);

            if (sect.shortName == null) {
                htm.addln("<h2 style='font-size:12pt; padding:6pt 0 3pt 0;color:#1E4D2B'>", sect.sectionName, "</h2>");
            } else {
                htm.addln("<h2 style='font-size:12pt; padding:6pt 0 3pt 0;color:#1E4D2B'>", sect.shortName, "</h2>");
            }

            final int numProblems = sect.getNumProblems();

            for (int p = 0; p < numProblems; ++p) {
                final ExamProblem ep = sect.getPresentedProblem(p);

                if (this.currentSect == sectIndex && this.currentItem == p
                        && this.state == EPlacementExamState.ITEM_NN) {
                    htm.addln("<div id='selected_menu_item' style='background:#7FFF7F;'>");
                } else {
                    htm.sDiv();
                }

                if (this.state == EPlacementExamState.ITEM_NN
                        || this.state == EPlacementExamState.INSTRUCTIONS
                        || this.state == EPlacementExamState.SUBMIT_NN
                        || this.state == EPlacementExamState.COMPLETED) {
                    // When interacting or instructions, mark the ones that have been answered

                    if (ep.getSelectedProblem().isAnswered()) {
                        htm.add("<input type='checkbox' disabled checked> ");
                    } else {
                        htm.add("<input type='checkbox' disabled> ");
                    }
                }

                htm.add("<a class='ulink' style='font-family:\"Times New Roman\",Times,serif;'");
                if (!disabled) {
                    htm.add(" href='javascript:invokeAct(\"nav_", Integer.toString(sectIndex),
                            "_", Integer.toString(p), "\");'");
                }
                htm.add(">");
                if (ep.problemName == null) {
                    htm.add("Item ", Integer.valueOf(p + 1));
                } else {
                    htm.add(ep.problemName);
                }
                htm.addln("</a>");
                htm.eDiv();
            }
        }

        htm.addln("</nav>");
    }

    /**
     * Appends an empty footer.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void appendEmptyFooter(final HtmlBuilder htm) {

        htm.addln("<div style='flex: 1 100%; display:block; background-color:#ECECE4; ",
                "border:1px solid #1E4D2B; margin:1px; padding:6pt; text-align:center;'>");

        htm.eDiv();

        // End grid div
        htm.eDiv();
    }

    /**
     * Called when a POST is received on the page hosting the unit exam.
     *
     * @param cache   the data cache
     * @param session the user session
     * @param req     the servlet request
     * @param htm     the {@code HtmlBuilder} to which to append
     * @return a URL to which to redirect; {@code null} to present the generated HTML
     * @throws SQLException if there is an error accessing the database
     */
    public String processPost(final Cache cache, final ImmutableSessionInfo session,
                              final ServletRequest req, final HtmlBuilder htm) throws SQLException {

        String redirect = null;

        switch (this.state) {
            case PROFILE:
                processPostProfile(cache, session, req, htm);
                break;

            case INSTRUCTIONS:
                processPostInstructions(cache, session, req, htm);
                break;

            case ITEM_NN:
                processPostInteracting(cache, session, req, htm);
                break;

            case SUBMIT_NN:
                processPostSubmit(cache, session, req, htm);
                break;

            case COMPLETED:
                redirect = processPostCompleted(cache, session, req, htm);
                break;

            case ERROR:
                redirect = processPostError(cache, session, req, htm);
                break;

            case INITIAL:
            default:
                generateHtml(cache, session.getNow(), req, htm);
                break;
        }

        return redirect;
    }

    /**
     * Called when a POST is received while in the PROFILE state.
     *
     * @param cache   the data cache
     * @param session the user session
     * @param req     the servlet request
     * @param htm     the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private void processPostProfile(final Cache cache, final ImmutableSessionInfo session,
                                    final ServletRequest req, final HtmlBuilder htm) throws SQLException {

        if (this.state != EPlacementExamState.ERROR) {
            if (this.profilePage == 1) {
                this.profileResponses[0] = req.getParameter("q1");
                int resources = 0;
                if (req.getParameter("q2_1") != null) {
                    resources += 8;
                }
                if (req.getParameter("q2_2") != null) {
                    resources += 4;
                }
                if (req.getParameter("q2_3") != null) {
                    resources += 2;
                }
                if (req.getParameter("q2_4") != null) {
                    resources += 1;
                }
                this.profileResponses[1] = Integer.toString(resources);
            } else if (this.profilePage == 2) {
                this.profileResponses[2] = req.getParameter("q3");
                this.profileResponses[3] = req.getParameter("q4");
            } else if (this.profilePage == 3) {
                int hs = 4;
                if (req.getParameter("q5_1") != null) {
                    this.profileResponses[hs] = "4";
                    ++hs;
                }
                if (req.getParameter("q5_2") != null) {
                    this.profileResponses[hs] = "6";
                    ++hs;
                }
                if (req.getParameter("q5_3") != null) {
                    this.profileResponses[hs] = "9";
                    ++hs;
                }
                if (req.getParameter("q5_4") != null) {
                    this.profileResponses[hs] = "5";
                    ++hs;
                }
                if (req.getParameter("q5_5") != null) {
                    this.profileResponses[hs] = "7";
                    ++hs;
                }
                if (req.getParameter("q5_6") != null) {
                    this.profileResponses[hs] = "10";
                    ++hs;
                }
                if (req.getParameter("q5_7") != null) {
                    this.profileResponses[hs] = "13";
                    ++hs;
                }
                if (req.getParameter("q5_8") != null) {
                    this.profileResponses[hs] = "15";
                    ++hs;
                }
                if (req.getParameter("q5_9") != null) {
                    this.profileResponses[hs] = "1";
                    ++hs;
                }

                int index = 13;
                if (req.getParameter("q6_1") != null) {
                    this.profileResponses[index] = "3";
                    ++index;
                } else if (req.getParameter("q6_2") != null) {
                    this.profileResponses[index] = "8";
                    ++index;
                } else if (req.getParameter("q6_3") != null) {
                    this.profileResponses[index] = "11";
                    ++index;
                } else if (req.getParameter("q6_4") != null) {
                    this.profileResponses[index] = "12";
                    ++index;
                } else if (req.getParameter("q6_5") != null) {
                    this.profileResponses[index] = "14";
                    ++index;
                } else if (req.getParameter("q6_6") != null) {
                    this.profileResponses[index] = "16";
                    ++index;
                } else if (req.getParameter("q6_7") != null) {
                    this.profileResponses[index] = "2";
                    ++index;
                }

                if (hs == 4 && index == 13) {
                    this.profileResponses[4] = "0";
                }
            }

            if (req.getParameter("goto2") != null) {
                this.profilePage = 2;
            } else if (req.getParameter("goto3") != null) {
                this.profilePage = 3;
            } else if (req.getParameter("instruct") != null) {
                // Store responses
                final LocalDateTime now = LocalDateTime.now();

                final int count = this.profileResponses.length;
                for (int i = 0; i < count; ++i) {
                    final String answer = this.profileResponses[i];

                    if (answer != null && !answer.isEmpty()) {
                        // Record the answer
                        final RawStsurveyqa ans = new RawStsurveyqa(this.studentId, this.version,
                                now.toLocalDate(), Integer.valueOf(i + 1), answer,
                                Integer.valueOf(TemporalUtils.minuteOfDay(now)));

                        RawStsurveyqaLogic.INSTANCE.insert(cache, ans);
                    }
                }

                appendExamLog("Processed profile submission, moving to Instructions");
                this.state = EPlacementExamState.INSTRUCTIONS;
            }
        }

        generateHtml(cache, session.getNow(), req, htm);
    }

    /**
     * Called when a POST is received while in the INSTRUCTIONS state.
     *
     * @param cache   the data cache
     * @param session the user session
     * @param req     the servlet request
     * @param htm     the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private void processPostInstructions(final Cache cache, final ImmutableSessionInfo session,
                                         final ServletRequest req, final HtmlBuilder htm) throws SQLException {

        if (this.state != EPlacementExamState.ERROR) {
            if (req.getParameter("nav_0") != null) {
                this.currentSect = 0;
                this.currentItem = 0;
                this.state = EPlacementExamState.ITEM_NN;
                this.started = true;
                if (this.timeout == 0L && getExam().allowedSeconds != null) {
                    final long now = System.currentTimeMillis();
                    this.timeout = now + 1000L * getExam().allowedSeconds.longValue();
                    this.purgeTime = now + PURGE_TIMEOUT;
                }
                final String msg = "Starting placement exam, duration is " + getExam().allowedSeconds;
                Log.info(msg);
                appendExamLog(msg);
            } else if (req.getParameter("score") != null) {
                final String msg = "'score' action received - confirming submit.";
                Log.info(msg);
                appendExamLog(msg);
                this.state = EPlacementExamState.SUBMIT_NN;
            } else {
                final String act = req.getParameter("action");

                if ("timeout".equals(act)) {
                    final String msg = "'timeout' action received - scoring exam.";
                    Log.info(msg);
                    appendExamLog(msg);
                    writeExamRecovery(cache);
                    this.gradingError = scoreAndRecordCompletion(cache, session.getNow());
                    this.state = EPlacementExamState.COMPLETED;
                } else if (act != null) {
                    navigate(act, true);
                }
            }
        }

        generateHtml(cache, session.getNow(), req, htm);
    }

    /**
     * Navigates to a section/problem specified by an action of the form "nav_1_2" where the "1" is the section index,
     * and the "2" is the presented problem index within that section.
     *
     * @param act        the action
     * @param startTimer true to start the exam timer if not already started
     */
    private void navigate(final String act, final boolean startTimer) {

        if (this.state != EPlacementExamState.ERROR) {
            // Navigation ...
            final int last = act.lastIndexOf('_');
            if (act.startsWith("nav_") && last > 3) {
                try {
                    final int parsed = Integer.parseInt(act.substring(4, last));

                    if (parsed >= 0 && parsed < getExam().getNumSections()) {
                        final ExamSection sect = getExam().getSection(parsed);

                        final int p = Integer.parseInt(act.substring(last + 1));

                        if (p >= 0 && p < sect.getNumProblems()) {

                            this.currentSect = parsed;
                            this.currentItem = p;
                            this.state = EPlacementExamState.ITEM_NN;
                            this.started = true;
                            if (startTimer && this.timeout == 0L && getExam().allowedSeconds != null) {

                                final String msg = "Starting placement exam timer, duration is "
                                        + getExam().allowedSeconds;
                                Log.info(msg);
                                appendExamLog(msg);

                                this.timeout = System.currentTimeMillis()
                                        + 1000L * getExam().allowedSeconds.longValue();
                            }
                        }
                    }
                } catch (final NumberFormatException ex) {
                    Log.warning("Invalid nav string: ", act, ex);
                }
            }
        }
    }

    /**
     * Called when a POST is received while in the INTERACTING state.
     *
     * @param cache   the data cache
     * @param session the user session
     * @param req     the servlet request
     * @param htm     the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private void processPostInteracting(final Cache cache, final ImmutableSessionInfo session,
                                        final ServletRequest req, final HtmlBuilder htm) throws SQLException {

        if (this.state != EPlacementExamState.ERROR) {

            if (this.currentItem != -1) {
                final String reqItem = req.getParameter("currentItem");

                if (Integer.toString(this.currentItem).equals(reqItem)) {

                    final ExamProblem ep = getExam().getSection(this.currentSect).getPresentedProblem(this.currentItem);
                    if (ep == null) {
                        Log.warning("  No exam problem found!");
                    } else {
                        final AbstractProblemTemplate p = ep.getSelectedProblem();
                        // Log.warning(CoreConstants.SPC, p.getClass().getName(), " extracting answers");
                        p.extractAnswers(req.getParameterMap());

                        final Object[] answers = p.getAnswer();

                        final HtmlBuilder builder = new HtmlBuilder(100);
                        builder.add("Sect ", Integer.toString(this.currentSect), ", Item ",
                                Integer.toString(this.currentItem), " answers {");
                        if (answers != null) {
                            for (final Object o : answers) {
                                builder.add(CoreConstants.SPC, o, " (", o.getClass().getSimpleName(), ")");
                            }
                            builder.add(" }, correct=", p.isCorrect(answers) ? "Y" : "N");
                        } else {
                            builder.add("}");
                        }
                        final String msg = builder.toString();
                        Log.info(msg);
                        appendExamLog(msg);
                    }
                } else {
                    Log.warning("POST received with currentItem='", reqItem, "' when current item was ",
                            Integer.toString(this.currentItem));
                }
            }

            if (req.getParameter("score") != null) {
                final String msg = "'score' action received - confirming submit.";
                Log.info(msg);
                appendExamLog(msg);
                this.state = EPlacementExamState.SUBMIT_NN;
            } else {
                final String act = req.getParameter("action");

                if ("instruct".equals(act)) {
                    this.state = EPlacementExamState.INSTRUCTIONS;
                } else if ("timeout".equals(act)) {
                    final String msg = "'timeout' action received - scoring exam.";
                    Log.info(msg);
                    appendExamLog(msg);
                    writeExamRecovery(cache);
                    this.gradingError = scoreAndRecordCompletion(cache, session.getNow());
                    this.state = EPlacementExamState.COMPLETED;
                } else if (act != null) {
                    navigate(act, false);
                }
            }
        }

        generateHtml(cache, session.getNow(), req, htm);
    }

    /**
     * Called when a POST is received while in the SUBMIT_NN state.
     *
     * @param cache   the data cache
     * @param session the user session
     * @param req     the servlet request
     * @param htm     the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private void processPostSubmit(final Cache cache, final ImmutableSessionInfo session,
                                   final ServletRequest req, final HtmlBuilder htm) throws SQLException {

        if (this.state != EPlacementExamState.ERROR) {
            if (req.getParameter("N") != null) {
                final String msg = "Submit canceled, returning to exam";
                Log.info(msg);
                appendExamLog(msg);
                this.state = EPlacementExamState.ITEM_NN;
            } else if (req.getParameter("Y") != null) {
                final String msg = "Submit confirmed, scoring...";
                Log.info(msg);
                appendExamLog(msg);
                writeExamRecovery(cache);
                this.gradingError = scoreAndRecordCompletion(cache, session.getNow());
                this.state = EPlacementExamState.COMPLETED;
            }
        }

        generateHtml(cache, session.getNow(), req, htm);
    }

    /**
     * Called when a POST is received while in the COMPLETED state.
     *
     * @param cache   the data cache
     * @param session the user session
     * @param req     the servlet request
     * @param htm     the {@code HtmlBuilder} to which to append
     * @return a string to which to redirect; null if no redirection should occur
     * @throws SQLException if there is an error accessing the database
     */
    private String processPostCompleted(final Cache cache, final ImmutableSessionInfo session,
                                        final ServletRequest req, final HtmlBuilder htm) throws SQLException {

        String redirect = null;

        if (this.state != EPlacementExamState.ERROR) {
            if (req.getParameter("close") != null) {
                final String msg = "Closing placement exam session";
                Log.info(msg);
                appendExamLog(msg);
                final PlacementExamSessionStore store = PlacementExamSessionStore.getInstance();
                store.removePlacementExamSessionForStudent(this.studentId);

                setExam(null);

                redirect = this.redirectOnEnd;
            } else {
                generateHtml(cache, session.getNow(), req, htm);
            }
        }

        return redirect;
    }

    /**
     * Called when a POST is received while in the ERROR state.
     *
     * @param cache   the data cache
     * @param session the user session
     * @param req     the servlet request
     * @param htm     the {@code HtmlBuilder} to which to append
     * @return a string to which to redirect; null if no redirection should occur
     * @throws SQLException if there is an error accessing the database
     */
    private String processPostError(final Cache cache, final ImmutableSessionInfo session,
                                    final ServletRequest req, final HtmlBuilder htm) throws SQLException {

        String redirect = null;

        if (this.state != EPlacementExamState.ERROR) {
            if ("close".equals(req.getParameter("action"))) {
                final String msg = "Closing exam session with error: " + this.error;
                Log.info(msg);
                appendExamLog(msg);
                final PlacementExamSessionStore store = PlacementExamSessionStore.getInstance();
                store.removePlacementExamSessionForStudent(this.studentId);

                final ExamObj examObj = getExam();
                if (examObj != null) {
                    RawPendingExamLogic.delete(cache, examObj.serialNumber, this.studentId);
                    setExam(null);
                }

                redirect = this.redirectOnEnd;
            } else {
                generateHtml(cache, session.getNow(), req, htm);
            }
        }

        return redirect;
    }

    /**
     * Performs a forced abort of a placement exam session.
     *
     * @param cache   the data cache
     * @param session the login session requesting the forced abort
     * @throws SQLException if there is an error accessing the database
     */
    public void forceAbort(final Cache cache, final ImmutableSessionInfo session)
            throws SQLException {

        if (session.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
            final String msg = "Forced abort requested";
            Log.info(msg);
            appendExamLog(msg);
            writeExamRecovery(cache);
            final PlacementExamSessionStore store = PlacementExamSessionStore.getInstance();
            store.removePlacementExamSessionForStudent(this.studentId);

            final ExamObj examObj = getExam();
            if (examObj != null) {
                RawPendingExamLogic.delete(cache, examObj.serialNumber, this.studentId);
                setExam(null);
            }
        } else {
            final String msg = "Forced abort requested, but requester is not ADMINISTRATOR";
            Log.info(msg);
            appendExamLog(msg);
        }
    }

    /**
     * Performs a forced submit of a placement exam session.
     *
     * @param cache   the data cache
     * @param session the login session requesting the forced submit
     * @throws SQLException if there is an error accessing the database
     */
    public void forceSubmit(final Cache cache, final ImmutableSessionInfo session)
            throws SQLException {

        if (session.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
            final String msg = "Forced submit requested";
            Log.info(msg);
            appendExamLog(msg);
            writeExamRecovery(cache);
            this.gradingError = scoreAndRecordCompletion(cache, session.getNow());
            this.state = EPlacementExamState.COMPLETED;

            final PlacementExamSessionStore store = PlacementExamSessionStore.getInstance();
            store.removePlacementExamSessionForStudent(this.studentId);

            final ExamObj examObj = getExam();
            if (examObj != null) {
                RawPendingExamLogic.delete(cache, examObj.serialNumber, this.studentId);
                setExam(null);
            }
        } else {
            final String msg = "Forced submit requested, but requester is not ADMINISTRATOR";
            Log.info(msg);
            appendExamLog(msg);
        }
    }

    /**
     * Scores the submitted exam and records the results.
     *
     * @param cache the data cache
     * @param now   the date/time to consider now
     * @return {@code null} on success; an error message on any failure
     * @throws SQLException if there is an error accessing the database
     */
    String scoreAndRecordCompletion(final Cache cache, final ZonedDateTime now) throws SQLException {

        final String err;

        final String stuId = this.studentId;

        if ("GUEST".equals(stuId) || "AACTUTOR".equals(stuId) || "ETEXT".equals(stuId)) {

            err = "Guest login exams will not be recorded.";
        } else if (stuId.startsWith("99")) {

            err = "Test student exams will not be recorded.";
        } else {
            Log.info("Writing updated exam state");
            final Object[][] answers = getExam().exportState();

            loadStudentInfo(cache);

            // Write the updated exam state out somewhere permanent
            new ExamWriter().writeUpdatedExam(stuId, this.active, answers, false);
            err = finalizeExam(cache, now, answers);
        }

        if (err != null) {
            Log.warning(err);
        }

        return err;
    }

    /**
     * Finalize the exam record on the server, running all grading processing and applying result to the student's
     * record.
     *
     * @param cache   the data cache
     * @param now     the date/time to consider now
     * @param answers the submitted answers
     * @return {@code null} on success; an error message on any failure
     * @throws SQLException if there is an error accessing the database
     */
    private String finalizeExam(final Cache cache, final ZonedDateTime now,
                                final Object[][] answers) throws SQLException {

        final ExamObj examObj = getExam();

        if (examObj == null) {
            return "No exam found";
        }

        if (answers == null || answers.length == 0) {
            return "No answers provided";
        }

        // Store the presentation and completion times in the exam object
        if (answers[0].length == 4) {
            // If exam has both presentation and completion time, compute the duration as seen by
            // the client, then adjust for the server's clock
            if (answers[0][2] != null && answers[0][3] != null) {
                final long duration = ((Long) answers[0][3]).longValue() - ((Long) answers[0][2]).longValue();

                // FIXME: This test may have been deleted in similar classes...
                if (duration >= 0L && duration < 43200L) {
                    examObj.presentationTime = System.currentTimeMillis() - duration;
                } else {
                    // Time was not reasonable, so set to 0 time.
                    Log.warning("Client gave exam duration as " + duration);
                    examObj.presentationTime = System.currentTimeMillis();
                }
            } else {
                examObj.presentationTime = examObj.realizationTime;
            }

            // Set the completion time
            examObj.finalizeExam();
        } else {
            return "Size of answer metadata is not correct";
        }

        // See if the exam has already been inserted
        final Long ser = examObj.serialNumber;
        final LocalDateTime start = TemporalUtils.toLocalDateTime(examObj.realizationTime);

        final List<RawStmpe> existing = RawStmpeLogic.queryLegalByStudent(cache, this.studentId);

        for (final RawStmpe test : existing) {
            if (test.getStartDateTime() != null && test.serialNbr.equals(ser)
                    && test.getStartDateTime().equals(start)) {
                Log.warning("Submitted placement exam for student ", this.studentId, ", exam " + this.version,
                        ": serial=", test.serialNbr, " submitted a second time - ignoring");
                return "Exam submitted a second time - ignoring.";
            }
        }

        final EvalContext params = new EvalContext();

        final AbstractVariable param1 = new VariableBoolean("proctored");
        param1.setValue(Boolean.valueOf(this.proctored));
        params.addVariable(param1);

        Log.info("Grading placement exam for student ", this.studentId, ", exam ", examObj.examVersion);

        RawPendingExamLogic.delete(cache, examObj.serialNumber, this.studentId);

        loadSatActSurvey(cache, params);

        final RawExam examRec = RawExamLogic.query(cache, this.version);
        if (examRec == null) {
            return "Exam " + this.version + " not found!";
        }

        // Begin preparing the database object to store exam results
        final StudentExamRec stexam = new StudentExamRec();
        stexam.studentId = this.studentId;
        stexam.examType = examRec.examType;
        stexam.course = examObj.course;
        try {
            stexam.unit = Integer.valueOf(examObj.courseUnit);
        } catch (final NumberFormatException ex) {
            Log.warning("Failed to parse unit", ex);
        }
        stexam.examId = examObj.examVersion;
        stexam.proctored = false;
        stexam.start = TemporalUtils.toLocalDateTime(examObj.realizationTime);
        stexam.finish = TemporalUtils.toLocalDateTime(examObj.completionTime);
        stexam.serialNumber = examObj.serialNumber;

        // Generate the list of survey answers, store in exam record
        buildSurveyAnswerList(answers, stexam);

        // Generate the list of problem answers, store in exam record
        buildAnswerList(answers, stexam);

        // Determine problem and subtest scores, add to the parameter set
        computeSubtestScores(stexam, params);

        // Determine grading rule results, and add them to the parameter set for use in outcome
        // processing.
        evaluateGradingRules(stexam, params);

        determineOutcomes(cache, stexam, params);

        // We have now assembled the student exam record, so insert into the database.
        return insertPlacement(now, stexam);
    }

    /**
     * Retrieve the student's ACT and SAT scores and relevant survey answers from the database and store them in the
     * parameter set as "student-ACT-math", "student-SAT-math", "hours-preparing", "time-since-last-math", and
     * "highest-math-taken", "resources-used-preparing", and "typical-math-grade". If the values are not populated in
     * the database, the parameters will be added with default values.
     *
     * @param cache  the data cache
     * @param params the parameter set to which to add the parameters
     * @return true if successful; false on a database error
     * @throws SQLException if there is an error accessing the database
     */
    private boolean loadSatActSurvey(final Cache cache, final EvalContext params) throws SQLException {

        final int act = getStudent().actScore == null ? 0 : getStudent().actScore.intValue();
        final int sat = getStudent().satScore == null ? 0 : getStudent().satScore.intValue();

        final VariableInteger param1 = new VariableInteger("student-ACT-math");
        param1.setValue(Long.valueOf((long) act));
        params.addVariable(param1);

        final VariableInteger param2 = new VariableInteger("student-SAT-math");
        param2.setValue(Long.valueOf((long) sat));
        params.addVariable(param2);

        // Get answers to placement exam survey, which are used for validation
        // FIXME: Use PPPPP if proctored?
        final List<RawStsurveyqa> answers =
                RawStsurveyqaLogic.queryLatestByStudentProfile(cache, this.studentId, "POOOO");

        int prep = 0;
        int resources = 0;
        int course;
        int taken = 0;
        int since = 6;
        int typical = 9;
        for (final RawStsurveyqa answer : answers) {

            if (answer.surveyNbr == null) {
                continue;
            }

            final int questionNumber = answer.surveyNbr.intValue();
            final String ans = answer.stuAnswer;

            try {
                if (questionNumber == 1) {
                    // Question 1: Hours spent preparing
                    prep = Long.valueOf(ans).intValue();
                } else if (questionNumber == 2) {
                    // Question 2: Resources used
                    resources = Long.valueOf(ans).intValue();
                } else if (questionNumber == 3) {
                    // Question 3: Time since last math course
                    since = Long.valueOf(ans).intValue();
                } else if (questionNumber == 4) {
                    // Question 4: Typical math grade
                    typical = Long.valueOf(ans).intValue();
                } else if (questionNumber >= 5) {
                    // Question 5+: Courses taken (find highest)
                    course = Long.valueOf(ans).intValue();

                    if (course > taken) {
                        taken = course;
                    }
                }
            } catch (final NumberFormatException ex) {
                Log.warning("Failed to parse question answer '", ans, "'");
            }
        }

        final VariableInteger param3 = new VariableInteger("hours-preparing");
        param3.setValue(Long.valueOf((long) prep));
        params.addVariable(param3);

        final VariableInteger param4 = new VariableInteger("time-since-last-math");
        param4.setValue(Long.valueOf((long) since));
        params.addVariable(param4);

        final VariableInteger param5 = new VariableInteger("highest-math-taken");
        param5.setValue(Long.valueOf((long) taken));
        params.addVariable(param5);

        final VariableInteger param6 = new VariableInteger("resources-used-preparing");
        param6.setValue(Long.valueOf((long) resources));
        params.addVariable(param6);

        final VariableInteger param7 = new VariableInteger("typical-math-grade");
        param7.setValue(Long.valueOf((long) typical));
        params.addVariable(param7);

        return true;
    }

    /**
     * Assemble a list of the student's answers and store them with the exam record that is being prepared for database
     * insertion.
     *
     * @param answers the list of the student's answers
     * @param stexam  the exam record that will be inserted into the database
     * @return true if succeeded; false otherwise
     */
    private boolean buildSurveyAnswerList(final Object[][] answers, final StudentExamRec stexam) {

        final Iterator<ExamSection> sections = getExam().sections();

        while (sections.hasNext()) {
            final ExamSection sect = sections.next();

            if (!"survey".equalsIgnoreCase(sect.sectionName)) {
                continue;
            }

            final Iterator<ExamProblem> problems = sect.problems();

            while (problems.hasNext()) {
                final ExamProblem prob = problems.next();

                final StudentSurveyAnswer stanswer = new StudentSurveyAnswer();
                final int id = prob.problemId;

                if (answers[id] != null) {
                    final char[] answerStr = "     ".toCharArray();

                    final int rowLen = answers[id].length;
                    for (int i = 0; i < rowLen; ++i) {

                        if (answers[id][i] instanceof Long) {
                            final int index = ((Long) answers[id][i]).intValue();

                            if (index >= 1 && index <= 5) {
                                answerStr[index - 1] = (char) ('A' + index - 1);
                            }
                        }
                    }

                    stanswer.id = id;
                    stanswer.studentAnswer = String.valueOf(answerStr);

                    final String key = Integer.toString(id / 100) + id / 10 % 10 + id % 10;
                    stexam.surveys.put(key, stanswer);
                }
            }
        }

        return true;
    }

    /**
     * Assemble a list of the student's answers and store them with the exam record that is being prepared for database
     * insertion.
     *
     * @param answers the list of the student's answers
     * @param stexam  the exam record that will be inserted into the database
     */
    private void buildAnswerList(final Object[][] answers, final StudentExamRec stexam) {

        final Iterator<ExamSubtest> subtests = getExam().subtests();

        while (subtests.hasNext()) {
            final ExamSubtest subtest = subtests.next();
            final Iterator<ExamSubtestProblem> problems = subtest.getSubtestProblems();

            while (problems.hasNext()) {
                final ExamSubtestProblem subtestprob = problems.next();
                final int id = subtestprob.problemId;

                final ExamProblem problem = getExam().getProblem(id);
                final AbstractProblemTemplate selected =
                        problem == null ? null : problem.getSelectedProblem();

                if (selected != null && selected.ref != null) {
                    final StudentExamAnswerRec stanswer = new StudentExamAnswerRec();
                    stanswer.id = id;
                    stanswer.subtest = subtest.subtestName;
                    stanswer.treeRef = selected.ref;

                    // FIXME: Get actual sub-objective relating to problem
                    stanswer.objective = "0";

                    if (answers[id] != null) {
                        selected.recordAnswer(answers[id]);
                        final char[] answerStr = "     ".toCharArray();

                        final int rowLen = answers[id].length;
                        for (int i = 0; i < rowLen; ++i) {

                            if (answers[id][i] instanceof Long) {
                                final int index = ((Long) answers[id][i]).intValue();

                                if (index >= 1 && index <= 5) {
                                    answerStr[index - 1] = (char) ('A' + index - 1);
                                }
                            }
                        }

                        stanswer.studentAnswer = String.valueOf(answerStr);
                        final boolean correct = selected.isCorrect(answers[id]);
                        stanswer.correct = correct;

                        if (correct) {
                            selected.score = 1.0;
                            stanswer.score = 1.0;
                        } else {
                            selected.score = 0.0;
                            stanswer.score = 0.0;
                            stexam.missed.put(Integer.valueOf(id), stanswer.objective);
                        }
                    } else {
                        stexam.missed.put(Integer.valueOf(id), stanswer.objective);
                    }

                    final String key = subtest.subtestName + CoreConstants.DOT + id / 100 + id / 10 % 10 + id % 10;
                    stexam.answers.put(key, stanswer);
                }
            }
        }
    }

    /**
     * Given a particular exam problem and a set of student responses, compute the student's subtest score.
     *
     * @param stexam the student exam record being populated
     * @param params the parameter set to which to add the subtest score parameters
     */
    private void computeSubtestScores(final StudentExamRec stexam, final EvalContext params) {

        final Iterator<ExamSubtest> subtests = getExam().subtests();

        while (subtests.hasNext()) {
            double subtestScore = 0.0;
            final ExamSubtest subtest = subtests.next();

            final Iterator<ExamSubtestProblem> problems = subtest.getSubtestProblems();

            while (problems.hasNext()) {
                final ExamSubtestProblem problem = problems.next();
                final int id = problem.problemId;
                final String key = subtest.subtestName + CoreConstants.DOT + id / 100 + id / 10 % 10 + id % 10;
                final StudentExamAnswerRec answer = stexam.answers.get(key);

                if (answer != null && answer.correct) {
                    subtestScore += answer.score * problem.weight;
                }
            }

            subtest.score = Double.valueOf(subtestScore);

            // Store the subtest score in the exam record
            stexam.subtestScores.put(subtest.subtestName, Integer.valueOf((int) subtest.score.doubleValue()));

            appendExamLog("  Subtest '" + subtest.subtestName + "' score " + subtest.score);

            final VariableReal param = new VariableReal(subtest.subtestName);
            param.setValue(Double.valueOf(subtestScore));
            params.addVariable(param);
        }
    }

    /**
     * Evaluate the formulae for grading rules based on subtest scores.
     *
     * @param stexam the student exam record being populated
     * @param params the parameter set to which to add the subtest score parameters
     * @return {@code true} if there is a "passed" rule that evaluates to true
     */
    private boolean evaluateGradingRules(final StudentExamRec stexam, final EvalContext params) {

        this.score = stexam.subtestScores.get("score");

        // If we have a "score" subtest, and we have a mastery score in the record, then automatically create a
        // "passed" grading rule. Then, as we go through, if there is another explicit "passed" grading rule, it will
        // override this one.
        if (stexam.masteryScore != null && this.score != null) {
            final VariableBoolean param = new VariableBoolean("passed");

            if (this.score.intValue() >= stexam.masteryScore.intValue()) {
                appendExamLog("  Marking exam as PASSED");
                param.setValue(Boolean.TRUE);
            } else {
                appendExamLog("  Marking exam as NOT PASSED");
                param.setValue(Boolean.FALSE);
            }

            params.addVariable(param);
            stexam.examGrades.put("passed", param.getValue());
        }

        final Iterator<ExamGradingRule> rules = getExam().gradingRules();

        while (rules.hasNext()) {
            final ExamGradingRule rule = rules.next();
            final Iterator<ExamGradingCondition> conditions = rule.getGradingConditions();

            boolean pass = false;

            while (conditions.hasNext()) {
                final ExamGradingCondition condition = conditions.next();
                final Formula formula = condition.gradingConditionFormula;
                final Object result = formula.evaluate(params);

                if (result instanceof ErrorValue) {
                    rule.result = result;
                    Log.severe("Error evaluating grading rule ", rule.gradingRuleName, " [", formula.toString(), "]: ",
                            result.toString(), "\n", getExam().toXmlString(0));

                    break;
                } else // Insert TRUE boolean parameter if result is PASS
                    if (ExamGradingRule.PASS_FAIL.equals(rule.getGradingRuleType()) && result instanceof Boolean) {
                        pass = ((Boolean) result).booleanValue();

                        if (pass) {
                            rule.result = Boolean.TRUE;

                            stexam.examGrades.put(rule.gradingRuleName, Boolean.TRUE);

                            final VariableBoolean param = new VariableBoolean(rule.gradingRuleName);
                            param.setValue(Boolean.TRUE);
                            params.addVariable(param);

                            break;
                        }
                    }
            }

            appendExamLog("  Grading rule '" + rule.gradingRuleName + "': " + (pass ? "PASS" : "FAIL"));

            // If no passing indication, record a fail
            if (ExamGradingRule.PASS_FAIL.equals(rule.getGradingRuleType()) && !pass) {
                rule.result = Boolean.FALSE;

                stexam.examGrades.put(rule.gradingRuleName, Boolean.FALSE);
                final VariableBoolean param = new VariableBoolean(rule.gradingRuleName);
                param.setValue(Boolean.FALSE);
                params.addVariable(param);
            }
        }

        final AbstractVariable result = params.getVariable("passed");

        final boolean passed;
        if (result != null && result.getValue() instanceof Boolean) {
            passed = ((Boolean) result.getValue()).booleanValue();
        } else {
            passed = false;
        }

        return passed;
    }

    /**
     * Evaluate the formulae for grading rules based on subtest scores.
     *
     * @param cache  the data cache
     * @param stexam the student exam record being populated
     * @param params the parameter set to which to add the subtest score parameters
     * @throws SQLException if there is an error accessing the database
     */
    private void determineOutcomes(final Cache cache, final StudentExamRec stexam,
                                   final EvalContext params) throws SQLException {

        final Iterator<ExamOutcome> outcomes = getExam().examOutcomes();

        String validBy = null;

        while (outcomes.hasNext()) {
            String whyDeny = null;
            String howValid = null;

            final ExamOutcome outcome = outcomes.next();
            Formula formula = outcome.condition;

            if (formula != null) {
                Object result = formula.evaluate(params);

                if (result instanceof Boolean) {

                    final boolean doOutcome = ((Boolean) result).booleanValue();

                    Log.info("    OUTCOME: ", outcome.condition.toString() + " = " + doOutcome);

                    // See if the outcome should be awarded based on grading rules
                    if (doOutcome) {

                        // Test for needed prerequisites; if any are not satisfied deny the outcome
                        final Iterator<ExamOutcomePrereq> prereqs = outcome.getPrereqs();

                        while (prereqs.hasNext() && whyDeny == null) {
                            final ExamOutcomePrereq prereq = prereqs.next();
                            formula = prereq.prerequisiteFormula;

                            if (formula != null) {
                                result = formula.evaluate(params);
                                Log.info("      PREREQ: ", formula, " = ", result);

                                if (result instanceof Boolean) {
                                    if (!((Boolean) result).booleanValue()) {
                                        whyDeny = RawMpecrDeniedLogic.DENIED_BY_PREREQ;
                                    }
                                } else {
                                    whyDeny = RawMpecrDeniedLogic.DENIED_BY_PREREQ;
                                    Log.severe("Outcome prerequisite evaluated to ", result.toString(), "\n",
                                            getExam().toXmlString(0));
                                }
                            } else {
                                Log.severe("Outcome prerequisite has no formula\n", getExam().toXmlString(0));
                            }
                        }

                        // Next, we test validation rules
                        if (whyDeny == null) {
                            final Iterator<ExamOutcomeValidation> valids = outcome.getValidations();

                            while (valids.hasNext() && howValid == null) {
                                final ExamOutcomeValidation valid = valids.next();
                                formula = valid.validationFormula;

                                if (formula != null) {
                                    result = formula.evaluate(params);
                                    Log.info("      VALID-IF[", valid.howValidated, "]: ", formula, " = ", result);

                                    if (result instanceof Boolean) {
                                        if (((Boolean) result).booleanValue()) {
                                            howValid = valid.howValidated;
                                            validBy = howValid;
                                        }
                                    } else {
                                        Log.severe("Validation formula evaluated to ", result.toString(), "\n",
                                                getExam().toXmlString(0));
                                    }
                                } else {
                                    Log.severe("Outcome validation has no formula\n", getExam().toXmlString(0));
                                }
                            }

                            if (howValid == null) {
                                Log.info("      Denying outcome due to lack of validation");
                                whyDeny = RawMpecrDeniedLogic.DENIED_BY_VAL;
                            }
                        }

                        // Award (or deny) the outcome.
                        final Iterator<ExamOutcomeAction> actions = outcome.getActions();

                        while (actions.hasNext()) {
                            final ExamOutcomeAction action = actions.next();

                            if (ExamOutcomeAction.INDICATE_PLACEMENT.equals(action.type)) {

                                // Award or deny placement
                                if (whyDeny == null) {
                                    Log.info("      TAKING ACTION: ", action.type, CoreConstants.SPC, action.course);

                                    stexam.earnedPlacement.add(action.course);
                                } else if (RawMpecrDeniedLogic.DENIED_BY_VAL.equals(whyDeny)) {
                                    Log.info("      RETAINING NON-VALIDATED ACTION: ", action.type, CoreConstants.SPC,
                                            action.course);

                                    stexam.earnedPlacement.add(action.course);
                                    if (outcome.logDenial && !stexam.deniedPlacement.containsKey(action.course)) {
                                        stexam.deniedPlacement.put(action.course, whyDeny);
                                    }
                                    validBy = "U";

                                } else if (outcome.logDenial && !stexam.deniedPlacement.containsKey(action.course)) {
                                    stexam.deniedPlacement.put(action.course, whyDeny);
                                }
                            } else if (ExamOutcomeAction.INDICATE_CREDIT.equals(action.type)) {

                                // Award or deny credit
                                if (whyDeny == null) {
                                    Log.info("      TAKING ACTION: ", action.type, CoreConstants.SPC, action.course);

                                    stexam.earnedCredit.add(action.course);

                                } else if (RawMpecrDeniedLogic.DENIED_BY_VAL.equals(whyDeny)) {
                                    Log.info("      RETAINING NON-VALIDATED ACTION: ", action.type, CoreConstants.SPC,
                                            action.course);

                                    if (!stexam.earnedCredit.contains(action.course)) {
                                        if (this.proctored) {
                                            stexam.earnedCredit.add(action.course);
                                        } else {
                                            Log.info("      NOT RETAINING CREDIT SINCE UNPROCTORED");
                                        }
                                    }
                                    if (!stexam.deniedCredit.containsKey(action.course)) {
                                        stexam.deniedCredit.put(action.course, whyDeny);
                                    }
                                    validBy = "U";

                                } else if (outcome.logDenial && !stexam.deniedCredit.containsKey(action.course)) {
                                    stexam.deniedCredit.put(action.course, whyDeny);
                                }
                            } else if (ExamOutcomeAction.INDICATE_LICENSED.equals(action.type)) {
                                final RawStudent stu = RawStudentLogic.query(cache, this.studentId, true);

                                if (stu != null && "N".equals(stu.licensed)) {
                                    RawStudentLogic.updateLicensed(cache, stu.stuId, "Y");
                                }
                            }
                        }
                    }
                } else if (result instanceof ErrorValue) {
                    Log.warning("Error evaluating outcome formula [", outcome.condition.toString(), "]: ",
                            result.toString(), ", ", getExam().toXmlString(0));
                } else {
                    Log.warning("Outcome formula [", outcome.condition.toString(),
                            "] did not evaluate to boolean:", getExam().toXmlString(0));
                }
            } else {
                Log.warning("Outcome has no formula:", getExam().toXmlString(0));
            }
        }

        if (validBy != null) {
            stexam.howValidated = validBy.charAt(0);
        }
    }

    /**
     * Insert a placement exam object into the database.
     *
     * @param now    the date/time to consider as "now"
     * @param stexam the StudentExam object with exam data to be inserted
     * @return an error message if an error occurred
     */
    private String insertPlacement(final ZonedDateTime now, final StudentExamRec stexam) {

        final DbProfile dbProfile = getDbProfile();
        final DbContext ctx = dbProfile.getDbContext(ESchemaUse.PRIMARY);

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final int[] attempts = RawStmpeLogic.countLegalAttempts(cache, stexam.studentId, stexam.examId);

                boolean deny = false;
                if (this.proctored) {
                    if (attempts[1] >= 2) {
                        Log.info("Max proctored attempts already used, denying");
                        deny = true;
                    }
                } else if (attempts[0] >= 1) {
                    Log.info("Max unproctored attempts already used, denying");
                    deny = true;
                }

                if (deny) {
                    // Attempt was not legal; deny all placement & credit awards. Reason for denial
                    // now becomes 'I'.
                    stexam.howValidated = ' ';
                    stexam.deniedPlacement.replaceAll((s, v) -> "I");
                    stexam.deniedCredit.replaceAll((s, v) -> "I");
                    for (final String s : stexam.earnedPlacement) {
                        stexam.deniedPlacement.put(s, "I");
                    }
                    for (final String s : stexam.earnedCredit) {
                        stexam.deniedCredit.put(s, "I");
                    }
                    stexam.earnedPlacement.clear();
                    stexam.earnedCredit.clear();

                    // Since we have detected an illegal placement exam attempt, we place a hold 18
                    // on the student account. TODO: Fix hardcode
                    RawAdminHold hold = RawAdminHoldLogic.query(cache, stexam.studentId, "18");

                    if (hold == null) {
                        // No hold, so create a new one
                        hold = new RawAdminHold(stexam.studentId, "18", "F", Integer.valueOf(0), LocalDate.now());

                        if (RawAdminHoldLogic.INSTANCE.insert(cache, hold) && !"F".equals(getStudent().sevAdminHold)) {
                            RawStudentLogic.updateHoldSeverity(cache, getStudent().stuId, "F");
                        }
                    } else {
                        // Already a hold 18, but update its date to now
                        hold = new RawAdminHold(stexam.studentId, "18", "F", Integer.valueOf(0), LocalDate.now());
                        RawAdminHoldLogic.updateAdminHoldDate(cache, hold);
                    }
                }

                // Finally, we insert the placement attempt record.
                if (stexam.finish == null) {
                    stexam.finish = now.toLocalDateTime();
                }

                boolean placed = false;
                final String result;
                if (!stexam.earnedCredit.isEmpty() || !stexam.earnedPlacement.isEmpty()) {

                    if (deny) {
                        // Illegal attempt, so store attempt number
                        result = Integer.toString(attempts[0] + attempts[1] + 1);
                    } else {
                        result = "Y";
                        placed = true;
                    }
                } else {
                    result = "N";
                }

                String howValidated = null;
                if (this.proctored) {
                    howValidated = "P";
                } else if (placed && stexam.howValidated != ' ') {
                    howValidated = Character.toString(stexam.howValidated);
                }

                final RawStmpe attempt = new RawStmpe(stexam.studentId, stexam.examId,
                        this.active.academicYear, stexam.finish.toLocalDate(),
                        Integer.valueOf(TemporalUtils.minuteOfDay(stexam.start)),
                        Integer.valueOf(TemporalUtils.minuteOfDay(stexam.finish)),
                        getStudent().lastName, getStudent().firstName, getStudent().middleInitial, null,
                        stexam.serialNumber, stexam.subtestScores.get("A"),
                        stexam.subtestScores.get("117"),
                        stexam.subtestScores.get("118"),
                        stexam.subtestScores.get("124"),
                        stexam.subtestScores.get("125"),
                        stexam.subtestScores.get("126"), result, howValidated);

                for (final StudentExamAnswerRec ansrec : stexam.answers.values()) {
                    final RawStmpeqa answer =
                            new RawStmpeqa(attempt.stuId, attempt.version, attempt.examDt, attempt.finishTime,
                                    Integer.valueOf(ansrec.id), ansrec.studentAnswer, ansrec.correct ? "Y" : "N",
                                    ansrec.subtest, ansrec.treeRef);

                    if (!RawStmpeqaLogic.INSTANCE.insert(cache, answer)) {
                        Log.warning("Failed to insert placement attempt answer");
                    }
                }

                // Update the placement log record
                final LocalDateTime start = stexam.start;

                final int startTime = TemporalUtils.minuteOfDay(start);

                RawMpeLogLogic.indicateFinished(cache, attempt.stuId, attempt.examDt, Integer.valueOf(startTime),
                        attempt.examDt, stexam.recovered == null ? null : stexam.recovered.toLocalDate());

                insertPlacementResults(cache, stexam, deny);

                // Last thing is to insert the actual STMPE row. We do this last so other jobs can know that if they
                // see a row in this table, the associated data will be present and complete.
                if (!RawStmpeLogic.INSTANCE.insert(cache, attempt)) {
                    return "Failed to insert student placement exam record";
                }
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            return "Failed to insert student placement exam record";
        }

        return null;
    }

    /**
     * Insert a placement exam object into the database.
     *
     * @param cache  the data cache
     * @param stexam the StudentExam object with exam data to be inserted
     * @param deny   true if results have been denied
     * @throws SQLException if there is an error accessing the database
     */
    private void insertPlacementResults(final Cache cache, final StudentExamRec stexam,
                                        final boolean deny) throws SQLException {

        // Indicate all required placements.
        for (final String placeIn : stexam.earnedPlacement) {
            if (stexam.earnedCredit.contains(placeIn)) {
                // If credit is awarded, we don't award placement too
                continue;
            }

            final RawMpeCredit credit = new RawMpeCredit(stexam.studentId, placeIn, "P", stexam.finish.toLocalDate(),
                    null, stexam.serialNumber, stexam.examId, stexam.proctored ? "RM" : null);

            RawMpeCreditLogic.INSTANCE.apply(cache, credit);
        }

        // Indicate all earned credit.
        for (final String placeIn : stexam.earnedCredit) {
            final RawMpeCredit credit = new RawMpeCredit(stexam.studentId, placeIn, "C", stexam.finish.toLocalDate(),
                    null, stexam.serialNumber, stexam.examId, stexam.proctored ? "RM" : null);

            RawMpeCreditLogic.INSTANCE.apply(cache, credit);
        }

        // Record all ignored credit results
        for (final Map.Entry<String, String> entry : stexam.deniedCredit.entrySet()) {
            final RawMpecrDenied denied = new RawMpecrDenied(stexam.studentId, entry.getKey(), "C",
                    stexam.finish.toLocalDate(), entry.getValue(), stexam.serialNumber, stexam.examId,
                    stexam.proctored ? "RM" : null);

            RawMpecrDeniedLogic.INSTANCE.insert(cache, denied);
        }

        // Record all ignored placement results
        for (final Map.Entry<String, String> entry : stexam.deniedPlacement.entrySet()) {
            final RawMpecrDenied denied = new RawMpecrDenied(stexam.studentId, entry.getKey(), "P",
                    stexam.finish.toLocalDate(), entry.getValue(), stexam.serialNumber, stexam.examId,
                    stexam.proctored ? "RM" : null);

            RawMpecrDeniedLogic.INSTANCE.insert(cache, denied);
        }

        if (!deny) {
            // Send results to BANNER, or store in queue table
            final RawStudent stu = getStudent();

            if (stu == null) {
                RawMpscorequeueLogic.logActivity("Unable to upload placement result for student " + stexam.studentId
                        + ": student record not found");
            } else {
                final DbContext liveCtx = getDbProfile().getDbContext(ESchemaUse.LIVE);
                final DbConnection liveConn = liveCtx.checkOutConnection();
                try {
                    RawMpscorequeueLogic.INSTANCE.postPlacementToolResult(cache, liveConn, stu.pidm,
                            new ArrayList<>(stexam.earnedPlacement), stexam.finish);
                } finally {
                    liveCtx.checkInConnection(liveConn);
                }
            }
        }
    }

    /**
     * Appends the XML representation of this session to an {@code HtmlBuilder}.
     *
     * @param xml the {@code HtmlBuilder} to which to append
     */
    void appendXml(final HtmlBuilder xml) {

        if (getExam() != null) {
            xml.addln("<placement-exam-session>");
            xml.addln(" <host>", getSiteProfile().host, "</host>");
            xml.addln(" <path>", getSiteProfile().path, "</path>");
            xml.addln(" <session>", this.sessionId, "</session>");
            xml.addln(" <student>", this.studentId, "</student>");
            xml.addln(" <proctored>", Boolean.toString(this.proctored), "</proctored>");
            xml.addln(" <exam-id>", this.version, "</exam-id>");
            if (this.score != null) {
                xml.addln(" <score>", this.score, "</score>");
            }
            if (this.masteryScore != null) {
                xml.addln(" <mastery>", this.masteryScore, "</mastery>");
            }
            xml.addln(" <state>", this.state.name(), "</state>");
            xml.addln(" <profile>", Integer.toString(this.profilePage), "</profile>");
            xml.addln(" <cur-sect>", Integer.toString(this.currentSect), "</cur-sect>");
            xml.addln(" <cur-item>", Integer.toString(this.currentItem), "</cur-item>");
            xml.addln(" <redirect>", XmlEscaper.escape(this.redirectOnEnd), "</redirect>");
            if (this.started) {
                xml.addln(" <started/>");
            }
            if (this.gradingError != null) {
                xml.addln(" <error>", this.gradingError, "</error>");
            }
            xml.addln(" <timeout>", Long.toString(this.timeout), "</timeout>");
            xml.addln(" <purge>", Long.toString(this.purgeTime), "</purge>");
            getExam().appendXml(xml, 1);

            final int numSect = getExam().getNumSections();
            for (int i = 0; i < numSect; ++i) {
                final ExamSection sect = getExam().getSection(i);
                final int numProb = sect.getNumProblems();
                for (int j = 0; j < numProb; ++j) {
                    final ExamProblem prob = sect.getProblem(j);
                    if (prob != null) {
                        final AbstractProblemTemplate selected = prob.getSelectedProblem();
                        if (selected != null) {
                            xml.addln(" <selected-problem sect='", Integer.toString(i), "' prob='",
                                    Integer.toString(j), "'>");
                            selected.appendXml(xml, 2);
                            xml.addln(" </selected-problem>");
                        }
                    }
                }
            }
            xml.addln("</placement-exam-session>");
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

        htm.sDiv(null, "style='flex: 1 100%; order:99; background-color:#ECECE4; "
                + "display:block; border:1px solid #1E4D2B; margin:1px; padding:0 12px; text-align:center;'");

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
}
