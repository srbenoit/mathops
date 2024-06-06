package dev.mathops.web.site.html.challengeexam;

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
import dev.mathops.assessment.variable.VariableReal;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogBase;
import dev.mathops.commons.parser.xml.XmlEscaper;
import dev.mathops.db.logic.ELiveRefreshes;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.logic.DbConnection;
import dev.mathops.db.logic.DbContext;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.cfg.WebSiteProfile;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.logic.ChallengeExamLogic;
import dev.mathops.db.logic.ChallengeExamStatus;
import dev.mathops.db.old.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.old.rawlogic.RawExamLogic;
import dev.mathops.db.old.rawlogic.RawMpeCreditLogic;
import dev.mathops.db.old.rawlogic.RawMpeLogLogic;
import dev.mathops.db.old.rawlogic.RawMpecrDeniedLogic;
import dev.mathops.db.old.rawlogic.RawMpscorequeueLogic;
import dev.mathops.db.old.rawlogic.RawPendingExamLogic;
import dev.mathops.db.old.rawlogic.RawStchallengeLogic;
import dev.mathops.db.old.rawlogic.RawStchallengeqaLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawExam;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawMpeLog;
import dev.mathops.db.old.rawrecord.RawMpecrDenied;
import dev.mathops.db.old.rawrecord.RawPendingExam;
import dev.mathops.db.old.rawrecord.RawStchallenge;
import dev.mathops.db.old.rawrecord.RawStchallengeqa;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.session.ExamWriter;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.txn.handlers.AbstractHandlerBase;
import dev.mathops.session.txn.handlers.StudentExamAnswerRec;
import dev.mathops.session.txn.handlers.StudentExamRec;
import dev.mathops.session.txn.messages.AvailableExam;
import dev.mathops.session.txn.messages.GetExamReply;
import dev.mathops.web.site.html.HtmlSessionBase;

import jakarta.servlet.ServletRequest;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A user session used to take challenge exams online. It takes as arguments a session ID, student name, and assignment
 * ID and presents the exam to the student.
 */
public final class ChallengeExamSession extends HtmlSessionBase {

    /** The purge time duration (5 hours), in milliseconds. */
    private static final long PURGE_TIMEOUT = (long) (5 * 60 * 60 * 1000);

    /** The achieved score. */
    private Integer score;

    /** The state of the unit exam. */
    private EChallengeExamState state;

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

    /**
     * Constructs a new {@code ChallengeExamSession}. This is called when the user clicks a button to start a challenge
     * exam. It stores data but does not generate the HTML until the page is actually generated.
     *
     * @param cache            the data cache
     * @param theSiteProfile   the site profile
     * @param theSessionId     the session ID
     * @param theStudentId     the student ID
     * @param theExamId        the exam ID being worked on
     * @param theRedirectOnEnd the URL to which to redirect at the end of the exam
     * @throws SQLException if there is an error accessing the database
     */
    public ChallengeExamSession(final Cache cache, final WebSiteProfile theSiteProfile,
                                final String theSessionId, final String theStudentId, final String theExamId,
                                final String theRedirectOnEnd) throws SQLException {

        super(cache, theSiteProfile, theSessionId, theStudentId, theExamId, theRedirectOnEnd);

        this.state = EChallengeExamState.INITIAL;
        this.currentSect = -1;
        this.currentItem = -1;
        this.started = false;
        this.timeout = 0L;
        this.purgeTime = System.currentTimeMillis() + PURGE_TIMEOUT;
    }

    /**
     * Constructs a new {@code ChallengeExamSession}. This is called when the user clicks a button to start a challenge
     * exam. It stores data but does not generate the HTML until the page is actually generated.
     *
     * @param cache            the data cache
     * @param theSiteProfile   the website profile
     * @param theSessionId     the session ID
     * @param theStudentId     the student ID
     * @param theExamId        the exam ID being worked on
     * @param theRedirectOnEnd the URL to which to redirect at the end of the exam
     * @param theState         the session state
     * @param theScore         the score
     * @param theStarted       true if exam has been started
     * @param theSect          the current section
     * @param theItem          the current item
     * @param theTimeout       the timeout
     * @param thePurgeTime     the purge time
     * @param theExam          the exam
     * @param theError         the grading error
     * @throws SQLException if there is an error accessing the database
     */
    ChallengeExamSession(final Cache cache, final WebSiteProfile theSiteProfile,
                         final String theSessionId, final String theStudentId, final String theExamId,
                         final String theRedirectOnEnd, final EChallengeExamState theState, final Integer theScore,
                         final boolean theStarted, final int theSect, final int theItem, final long theTimeout,
                         final long thePurgeTime, final ExamObj theExam, final String theError) throws SQLException {

        super(cache, theSiteProfile, theSessionId, theStudentId, theExamId, theRedirectOnEnd);

        this.state = theState;
        this.score = theScore;
        this.started = theStarted;
        this.currentSect = theSect;
        this.currentItem = theItem;
        this.timeout = theTimeout;
        this.purgeTime = Math.max(thePurgeTime, theTimeout + 60000L);
        setExam(theExam);
        this.gradingError = theError;

        loadStudentInfo(cache);
    }

    /**
     * Gets the exam state.
     *
     * @return the exam state
     */
    public EChallengeExamState getState() {

        return this.state;
    }

//    /**
//     * Gets the currently active section.
//     *
//     * @return the section
//     */
//    public int getCurrentSect() {
//
//        return this.currentSect;
//    }

//    /**
//     * Gets the currently active item.
//     *
//     * @return the item
//     */
//    public int getCurrentItem() {
//
//        return this.currentItem;
//    }

    /**
     * Tests whether the exam is started.
     *
     * @return {@code true} if the exam is started
     */
    public boolean isStarted() {

        return this.started;
    }

//    /**
//     * Gets the time remaining in the exam.
//     *
//     * @return the time remaining, in milliseconds (0 if the exam has not been started)
//     */
//    public long getTimeRemaining() {
//
//        return this.timeout == 0L ? 0L : this.timeout - System.currentTimeMillis();
//    }

//    /**
//     * Gets the time until the exam will be purged.
//     *
//     * @return the time before purge (milliseconds)
//     */
//    public long getTimeUntilPurge() {
//
//        return this.purgeTime - System.currentTimeMillis();
//    }

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
     * @param req   the servlet request
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    public void generateHtml(final Cache cache, final ServletRequest req, final HtmlBuilder htm)
            throws SQLException {

        switch (this.state) {
            case INITIAL:
                doInitial(cache, htm);
                break;

            case ERROR:
                appendErrorHtml(htm);
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
                htm.addln("<div style='text-align:center; color:navy;'>").add("Unsupported state.").eDiv();
                appendFooter(htm, "close", "Close", null, null, null, null);
                htm.eDiv(); // outer DIV from header
                break;
        }
    }

    /**
     * Processes a request for the page while in the INITIAL state, which generates the assignment, then sends its
     * HTML.
     *
     * @param cache the data cache
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private void doInitial(final Cache cache, final HtmlBuilder htm) throws SQLException {

        // Look up the exam and store it in an AvailableExam object.
        final AvailableExam avail = new AvailableExam();

        Log.info("Retrieving exam ", this.version);

        avail.exam = RawExamLogic.query(cache, this.version);

        if (avail.exam == null) {
            Log.warning("Exam ", this.version, " was not found by ExamCache");
            this.error = "There was no assessment found with the requested version.";
        } else {
            final String type = avail.exam.examType;
            if ("CH".equals(type)) {

                this.error = loadStudentInfo(cache);
                if (this.error == null) {
                    final GetExamReply reply = new GetExamReply();
                    LogBase.setSessionInfo("TXN", this.studentId);

                    // We need to verify the exam and fill in the remaining fields in AvailableExam
                    final List<RawAdminHold> holds = new ArrayList<>(1);
                    reply.status = GetExamReply.SUCCESS;

                    // FIXME: Move this up, use for student-related data throughout
                    final StudentData studentData = new StudentData(cache, getStudent().stuId, ELiveRefreshes.NONE);

                    final ChallengeExamStatus challengeStatStat =
                            new ChallengeExamLogic(studentData).getStatus(avail.exam.course);

                    final boolean eligible = this.version.equals(challengeStatStat.availableExamId);

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

                                final RawStudent stu =
                                        RawStudentLogic.query(cache, this.studentId, false);

                                if (stu != null && stu.timelimitFactor != null) {
                                    avail.timelimitFactor = stu.timelimitFactor;

                                    long secs = reply.presentedExam.allowedSeconds.longValue();

                                    secs = (long) ((double) secs * stu.timelimitFactor.doubleValue());

                                    reply.presentedExam.allowedSeconds = Long.valueOf(secs);
                                }
                            }

                            if (!holds.isEmpty()) {
                                final int numHolds = holds.size();
                                reply.holds = new String[numHolds];
                                for (int i = 0; i < numHolds; ++i) {
                                    reply.holds[i] = RawAdminHoldLogic.getStudentMessage(holds.get(i).holdId);
                                }
                            }

                            final LocalDateTime startDtTm = TemporalUtils.toLocalDateTime(
                                    Instant.ofEpochMilli(reply.presentedExam.realizationTime));
                            final LocalTime startTm = startDtTm.toLocalTime();
                            final int startMin = startTm.getHour() * 60 + startTm.getMinute();

                            final RawPendingExam pending = new RawPendingExam(Long.valueOf(serial),
                                    reply.presentedExam.examVersion, this.studentId,
                                    startDtTm.toLocalDate(), null, Integer.valueOf(startMin), null,
                                    null, null, null, avail.exam.course, avail.exam.unit,
                                    avail.exam.examType, avail.timelimitFactor, "STU");

                            RawPendingExamLogic.INSTANCE.insert(cache, pending);
                        }
                    }
                } else {
                    Log.warning("Assessment version '", this.version, "' is not a challenge exam.");
                    this.error = "Requested assessment is not a challenge exam.";
                }
            }
        }

        if (this.error == null) {
            getExam().presentationTime = System.currentTimeMillis();
            this.state = EChallengeExamState.INSTRUCTIONS;
            appendInstructionsHtml(htm);
        } else {
            this.state = EChallengeExamState.ERROR;
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
    private void buildPresentedExam(final String ref, final long serial, final GetExamReply reply,
                                    final TermRec term) {

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
                            Log.warning("Exam " + ref + " section " + onSect + " problem " + onProb
                                    + " choice " + i + " getProblem() returned " + prb);
                        } else {
                            prb = InstructionalCache.getProblem(prb.ref);

                            if (prb != null) {
                                eprob.setProblem(i, prb);
                            }
                        }
                    }
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
        htm.sP().add("We were unable to initialize the Challenge Exam", CoreConstants.COLON).eP();
        htm.sP().add(this.error).eP();
        htm.eDiv();

        htm.add("<input type='hidden' name='action' value='close'/>");
        htm.addln("<div style='margin:1px 0 0 40pt; padding:6pt; text-align:left;'>");
        htm.add("  <input type='submit' value='Close'/>");
        htm.eDiv();
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
            appendFooter(htm, "score", "I am finished.  Submit the exam for grading.", null, null, "nav_0",
                    "Go to question 1");
        } else {
            appendFooter(htm, "nav_0", "Begin...", null, null, null, null);
        }
        htm.eDiv(); // outer DIV from header
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
                htm.addln("  document.getElementById(\"exam-outline-nav\").", "scrollTop = " + st + ";");
                htm.addln("</script>");

            } catch (final NumberFormatException ex) {
                Log.warning("Failed to parse nav-scroll-top=", scrollTop, ex);
            }
        }

        final String prevCmd = this.currentItem == 0 ? null : "nav_" + (this.currentItem - 1);
        final String nextCmd = this.currentItem >= (sect.getNumProblems() - 1) ? null : "nav_" + (this.currentItem + 1);

        endMain(htm);
        appendFooter(htm, "score", "I am finished.  Submit the exam for grading.",
                prevCmd, "Go to Question " + (this.currentItem), nextCmd, "Go to Question " + (this.currentItem + 2));
        htm.eDiv(); // outer DIV from header
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
        for (int sect = 0; sect < numSections; ++sect) {
            final ExamSection examSect = getExam().getSection(sect);

            final int numProblems = examSect.getNumProblems();
            for (int i = 0; i < numProblems; ++i) {
                final ExamProblem ep = examSect.getProblem(i);
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
            htm.addln("You have only answered ", Integer.toString(numAnswered), " out of ", Integer.toString(total),
                    " questions.").br().br();
        }

        htm.addln("Submit your work and grade your exam?").br().br();
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
        htm.addln("Challenge Exam completed.");
        htm.br().br();

        htm.eSpan().eDiv();

        endMain(htm);
        appendFooter(htm, "close", "Close", null, null, null, null);
        htm.eDiv(); // outer DIV from header
    }

    /**
     * Appends the header.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private void appendHeader(final HtmlBuilder htm) {

        htm.sDiv(null, "style='display:flex; flex-flow:row wrap; margin:0 6px 12px 6px;'");

        htm.sDiv(null, "style='flex: 1 100%; display:block; "
                + "background-color:AliceBlue; border:1px solid SteelBlue; margin:1px;'");

        htm.add("<h1 style='text-align:center; font-family:sans-serif; font-size:18pt; ",
                "font-weight:bold; color:#36648b; text-shadow:2px 1px #ccc; padding:4pt;'>");
        htm.add(getExam().examName);
        htm.eH(1);

        // Countdown timer - re-synchronized on each refresh
        htm.addln("<hr style='height:1px; border:0; background:#b3b3b3; margin:0 1%;'>");
        htm.addln("<div style='text-align:center;margin-top:2px;'>");

        if (this.timeout > 0L && (this.state == EChallengeExamState.INSTRUCTIONS
                || this.state == EChallengeExamState.SUBMIT_NN || this.state == EChallengeExamState.ITEM_NN)) {

            final long now = System.currentTimeMillis();

            if (this.timeout <= now) {
                htm.add("Time Expired.");

                // Exam should auto-submit - add an onLoad handler to the form
                htm.addln("<script>");
                htm.addln("  window.addEventListener(\"load\",timeoutSubmit,false);");
                htm.addln("  function timeoutSubmit() {");
                htm.addln("    document.getElementById(\"unit_exam_act\").value=\"timeout\";");
                htm.addln("    document.getElementById(\"challenge_exam_form\").submit();");
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
                htm.addln(" let end = new Date().getTime() + ", Long.toString(remain), ";");

                htm.addln(" setInterval(function() {");
                htm.addln("   let now = new Date().getTime();");
                htm.addln("   let sec = Math.round((end - now + 500) / 1000);");
                htm.addln("   if (sec <= 0) {");
                htm.addln("     document.getElementById(\"unit-exam-timer\").innerHTML=\"00:00:00\";");
                htm.addln("     document.getElementById(\"unit_exam_act\").value=\"timeout\";");
                htm.addln("     document.getElementById(\"challenge_exam_form\").submit();");
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
        } else if (this.state == EChallengeExamState.COMPLETED) {
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

        htm.eDiv(); // center
        htm.eDiv(); // flex top bar

        // NOTE: the outer DIV is still open - footer will close
    }

    /**
     * Starts the "main" section.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private void startMain(final HtmlBuilder htm) {

        htm.addln("<main style='flex:1 1 73%; margin:1px; padding:2px; ",
                "border:1px solid SteelBlue; max-height: calc(100vh - 225px);'>");

        htm.addln(" <input type='hidden' name='currentSect' value='", Integer.toString(this.currentSect), "'>");
        htm.addln(" <input type='hidden' name='currentItem' value='", Integer.toString(this.currentItem), "'>");

        htm.addln("<div style='padding:8px;min-height:100%;border:1px solid #b3b3b3;background:#f5f5f5;",
                "font-family:\"Times New Roman\",Times,serif;font-size:",
                Float.toString(AbstractDocObjectTemplate.DEFAULT_BASE_FONT_SIZE),
                "px; max-height: calc(100vh - 145px); overflow-x:hidden; overflow-y:scroll;'>");
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
        htm.addln("  document.getElementById(\"unit_exam_act\").value = action;");
        htm.addln("  document.getElementById(\"challenge_exam_form\").submit();");
        htm.addln("}");

        htm.addln("function navScrolled() {");
        htm.addln("  document.getElementById(\"nav-scroll-top\").value = ",
                "document.getElementById(\"exam-outline-nav\").scrollTop;");
        htm.addln("}");

        htm.addln("</script>");

        htm.addln("<nav id='exam-outline-nav' style='flex:1 1 22%; display:block; background-color:AliceBlue; ",
                "border:1px solid SteelBlue; margin:1px; padding:8px 0 8px 8px; font-size:13pt; ",
                "max-height:calc(100vh - 225px); height: calc(100vh - 225px); overflow-x:hidden; overflow-y:scroll;' ",
                "onscroll='navScrolled();'>");

        if (this.state == EChallengeExamState.INSTRUCTIONS) {
            htm.addln("<div style='background:#7FFF7F;'>");
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
        for (int index = 0; index < numSections; ++index) {
            final ExamSection sect = getExam().getSection(index);

            if (sect.shortName == null) {
                htm.addln("<h2 style='font-size:12pt; padding:6pt 0 3pt 0;color:SteelBlue'>", sect.sectionName,
                        "</h2>");
            } else {
                htm.addln("<h2 style='font-size:12pt; padding:6pt 0 3pt 0;color:SteelBlue'>", sect.shortName,
                        "</h2>");
            }

            final int numProblems = sect.getNumProblems();

            for (int p = 0; p < numProblems; ++p) {
                final ExamProblem ep = sect.getPresentedProblem(p);

                if (this.currentSect == index && this.currentItem == p && this.state == EChallengeExamState.ITEM_NN) {
                    htm.addln("<div style='background:#7FFF7F;'>");
                } else {
                    htm.sDiv();
                }

                if (this.state == EChallengeExamState.ITEM_NN || this.state == EChallengeExamState.INSTRUCTIONS
                        || this.state == EChallengeExamState.SUBMIT_NN
                        || this.state == EChallengeExamState.COMPLETED) {
                    // When interacting or instructions, mark the ones that have been answered

                    if (ep.getSelectedProblem().isAnswered()) {
                        htm.add("<input type='checkbox' disabled checked> ");
                    } else {
                        htm.add("<input type='checkbox' disabled> ");
                    }
                }

                htm.add("<a class='ulink' style='font-family:\"Times New Roman\",Times,serif;'");
                if (!disabled) {
                    htm.add(" href='javascript:invokeAct(\"nav_", Integer.toString(index), "_", Integer.toString(p),
                            "\");'");
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

        htm.addln("<div style='flex:1 100%;display:block;background-color:AliceBlue;",
                "border:1px solid SteelBlue;margin:1px;padding:6pt;text-align:center;'>");

        htm.eDiv();

        // End grid div
        htm.eDiv();
    }

    /**
     * Called when a POST is received on the page hosting the unit exam.
     *
     * @param cache the data cache
     * @param req   the servlet request
     * @param htm   the {@code HtmlBuilder} to which to append
     * @return a URL to which to redirect; {@code null} to present the generated HTML
     * @throws SQLException if there is an error accessing the database
     */
    public String processPost(final Cache cache, final ServletRequest req, final HtmlBuilder htm) throws SQLException {

        String redirect = null;

        switch (this.state) {
            case INSTRUCTIONS:
                processPostInstructions(cache, req, htm);
                break;

            case ITEM_NN:
                processPostInteracting(cache, req, htm);
                break;

            case SUBMIT_NN:
                processPostSubmit(cache, req, htm);
                break;

            case COMPLETED:
                redirect = processPostCompleted(cache, req, htm);
                break;

            case ERROR:
                redirect = processPostError(cache, req, htm);
                break;

            case INITIAL:
            default:
                generateHtml(cache, req, htm);
                break;
        }

        return redirect;
    }

    /**
     * Called when a POST is received while in the INSTRUCTIONS state.
     *
     * @param cache the data cache
     * @param req   the servlet request
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private void processPostInstructions(final Cache cache, final ServletRequest req,
                                         final HtmlBuilder htm) throws SQLException {

        if (this.state != EChallengeExamState.ERROR) {
            if (req.getParameter("nav_0") != null) {
                this.currentSect = 0;
                this.currentItem = 0;
                this.state = EChallengeExamState.ITEM_NN;
                this.started = true;
                if (this.timeout == 0L && getExam().allowedSeconds != null) {
                    final long timestamp = System.currentTimeMillis();
                    this.timeout = timestamp + 1000L * getExam().allowedSeconds.longValue();
                    this.purgeTime = timestamp + PURGE_TIMEOUT;
                }
                appendExamLog("Starting challenge exam, duration is " + getExam().allowedSeconds);
            } else if (req.getParameter("score") != null) {
                appendExamLog("'score' action received - confirming submit.");
                this.state = EChallengeExamState.SUBMIT_NN;
            } else {
                final String act = req.getParameter("action");

                if ("timeout".equals(act)) {
                    appendExamLog("'timeout' action received - scoring exam.");
                    writeExamRecovery(cache);
                    this.gradingError = scoreAndRecordCompletion(cache);
                    this.state = EChallengeExamState.COMPLETED;
                } else if (act != null) {
                    navigate(act, true);
                }
            }
        }

        generateHtml(cache, req, htm);
    }

    /**
     * Navigates to a section/problem specified by an action of the form "nav_1_2" where the "1" is the section index,
     * and the "2" is the presented problem index within that section.
     *
     * @param act        the action
     * @param startTimer true to start the exam timer if not already started
     */
    private void navigate(final String act, final boolean startTimer) {

        if (this.state != EChallengeExamState.ERROR) {
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
                            this.state = EChallengeExamState.ITEM_NN;
                            this.started = true;
                            if (startTimer && this.timeout == 0L && getExam().allowedSeconds != null) {

                                appendExamLog("Starting challenge exam timer, duration is "
                                        + getExam().allowedSeconds);

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
     * @param cache the data cache
     * @param req   the servlet request
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private void processPostInteracting(final Cache cache, final ServletRequest req,
                                        final HtmlBuilder htm) throws SQLException {

        if (this.state != EChallengeExamState.ERROR) {

            if (this.currentItem != -1) {
                final String reqItem = req.getParameter("currentItem");

                if (Integer.toString(this.currentItem).equals(reqItem)) {

                    final ExamProblem ep = getExam().getSection(this.currentSect).getPresentedProblem(this.currentItem);
                    if (ep == null) {
                        Log.warning("  No exam problem found!");
                    } else {
                        final AbstractProblemTemplate p = ep.getSelectedProblem();
                        // Log.warning(CoreConstants.SPC, p.getClass().getName(),
                        // " extracting answers");
                        p.extractAnswers(req.getParameterMap());

                        final Object[] answers = p.getAnswer();

                        final HtmlBuilder inner = new HtmlBuilder(100);
                        inner.add("Sect ", Integer.toString(this.currentSect), ", Item ",
                                Integer.toString(this.currentItem), " answers {");
                        if (answers != null) {
                            for (final Object o : answers) {
                                inner.add(CoreConstants.SPC, o, " (", o.getClass().getSimpleName(), ")");
                            }
                            inner.add(" }, correct=", p.isCorrect(answers) ? "Y" : "N");
                        } else {
                            inner.add("}");
                        }
                        appendExamLog(inner.toString());
                    }
                } else {
                    Log.warning("POST received with currentItem='", reqItem, "' when current item was ",
                            Integer.toString(this.currentItem));
                }
            }

            if (req.getParameter("score") != null) {
                appendExamLog("'score' action received - confirming submit.");
                this.state = EChallengeExamState.SUBMIT_NN;
            } else {
                final String act = req.getParameter("action");

                if ("instruct".equals(act)) {
                    this.state = EChallengeExamState.INSTRUCTIONS;
                } else if ("timeout".equals(act)) {
                    appendExamLog("'timeout' action received - scoring exam.");
                    writeExamRecovery(cache);
                    this.gradingError = scoreAndRecordCompletion(cache);
                    this.state = EChallengeExamState.COMPLETED;
                } else if (act != null) {
                    navigate(act, false);
                }
            }
        }

        generateHtml(cache, req, htm);
    }

    /**
     * Called when a POST is received while in the SUBMIT_NN state.
     *
     * @param cache the data cache
     * @param req   the servlet request
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private void processPostSubmit(final Cache cache, final ServletRequest req,
                                   final HtmlBuilder htm) throws SQLException {

        if (this.state != EChallengeExamState.ERROR) {
            if (req.getParameter("N") != null) {
                appendExamLog("Submit canceled, returning to exam");
                this.state = EChallengeExamState.ITEM_NN;
            } else if (req.getParameter("Y") != null) {
                appendExamLog("Submit confirmed, scoring...");
                writeExamRecovery(cache);
                this.gradingError = scoreAndRecordCompletion(cache);
                this.state = EChallengeExamState.COMPLETED;
            }
        }

        generateHtml(cache, req, htm);
    }

    /**
     * Called when a POST is received while in the COMPLETED state.
     *
     * @param cache the data cache
     * @param req   the servlet request
     * @param htm   the {@code HtmlBuilder} to which to append
     * @return a string to which to redirect; null if no redirection should occur
     * @throws SQLException if there is an error accessing the database
     */
    private String processPostCompleted(final Cache cache, final ServletRequest req,
                                        final HtmlBuilder htm) throws SQLException {

        String redirect = null;

        if (this.state != EChallengeExamState.ERROR) {
            if (req.getParameter("close") != null) {
                appendExamLog("Closing exam session");
                final ChallengeExamSessionStore store = ChallengeExamSessionStore.getInstance();
                store.removeChallengeExamSessionForStudent(this.studentId);

                setExam(null);

                redirect = this.redirectOnEnd;
            } else {
                generateHtml(cache, req, htm);
            }
        }

        return redirect;
    }

    /**
     * Called when a POST is received while in the ERROR state.
     *
     * @param cache the cache
     * @param req   the servlet request
     * @param htm   the {@code HtmlBuilder} to which to append
     * @return a string to which to redirect; null if no redirection should occur
     * @throws SQLException if there is an error accessing the database
     */
    private String processPostError(final Cache cache, final ServletRequest req,
                                    final HtmlBuilder htm) throws SQLException {

        String redirect = null;

        if (this.state != EChallengeExamState.ERROR) {
            if ("close".equals(req.getParameter("action"))) {
                appendExamLog("Closing exam session with error: " + this.error);
                final ChallengeExamSessionStore store = ChallengeExamSessionStore.getInstance();
                store.removeChallengeExamSessionForStudent(this.studentId);

                final ExamObj examObj = getExam();
                if (examObj != null) {
                    RawPendingExamLogic.delete(cache, examObj.serialNumber, this.studentId);
                    setExam(null);
                }

                redirect = this.redirectOnEnd;
            } else {
                generateHtml(cache, req, htm);
            }
        }

        return redirect;
    }

    /**
     * Performs a forced abort of a challenge exam session.
     *
     * @param cache   the data cache
     * @param session the login session requesting the forced abort
     * @throws SQLException if there is an error accessing the database
     */
    public void forceAbort(final Cache cache,
                           final ImmutableSessionInfo session) throws SQLException {

        if (session.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
            appendExamLog("Forced abort requested");
            writeExamRecovery(cache);
            final ChallengeExamSessionStore store = ChallengeExamSessionStore.getInstance();
            store.removeChallengeExamSessionForStudent(this.studentId);

            final ExamObj examObj = getExam();
            if (examObj != null) {
                RawPendingExamLogic.delete(cache, examObj.serialNumber, this.studentId);
                setExam(null);
            }
        } else {
            appendExamLog("Forced abort requested, but requester is not ADMINISTRATOR");
        }
    }

    /**
     * Performs a forced submit of a challenge exam session.
     *
     * @param cache   the data cache
     * @param session the login session requesting the forced submit
     * @throws SQLException if there is an error accessing the database
     */
    public void forceSubmit(final Cache cache,
                            final ImmutableSessionInfo session) throws SQLException {

        if (session.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
            appendExamLog("Forced submit requested");
            writeExamRecovery(cache);
            this.gradingError = scoreAndRecordCompletion(cache);
            this.state = EChallengeExamState.COMPLETED;

            final ChallengeExamSessionStore store = ChallengeExamSessionStore.getInstance();
            store.removeChallengeExamSessionForStudent(this.studentId);

            final ExamObj examObj = getExam();
            if (examObj != null) {
                RawPendingExamLogic.delete(cache, examObj.serialNumber, this.studentId);
                setExam(null);
            }
        } else {
            appendExamLog("Forced submit requested, but requester is not ADMINISTRATOR");
        }
    }

    /**
     * Scores the submitted exam and records the results.
     *
     * @param cache the data cache
     * @return {@code null} on success; an error message on any failure
     * @throws SQLException if there is an error accessing the database
     */
    String scoreAndRecordCompletion(final Cache cache) throws SQLException {

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

            err = finalizeExam(cache, answers);
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
     * @param answers the submitted answers
     * @return {@code null} on success; an error message on any failure
     * @throws SQLException if there is an error accessing the database
     */
    private String finalizeExam(final Cache cache, final Object[][] answers) throws SQLException {

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

                examObj.presentationTime = System.currentTimeMillis() - duration;
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

        final List<RawStchallenge> existing =
                RawStchallengeLogic.queryByStudent(cache, this.studentId);

        for (final RawStchallenge test : existing) {
            if (test.startTime == null) {
                continue;
            }

            final int startMin = test.startTime.intValue();
            final LocalDateTime testStart = LocalDateTime.of(test.examDt, LocalTime.of(startMin / 60, startMin % 60));

            if (test.serialNbr.equals(ser) && testStart.equals(start)) {
                Log.warning("Submitted challenge exam for student ", this.studentId, ", exam ", this.version,
                        ": serial=", test.serialNbr, " submitted a second time - ignoring");
                return "Exam submitted a second time - ignoring.";
            }
        }

        final EvalContext params = new EvalContext();

        final AbstractVariable param1 = new VariableBoolean("proctored");
        param1.setValue(Boolean.TRUE);
        params.addVariable(param1);

        Log.info("Grading challenge exam for student ", this.studentId, ", exam ", examObj.examVersion);

        RawPendingExamLogic.delete(cache, examObj.serialNumber, this.studentId);

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

        // Generate the list of problem answers, store in exam record
        buildAnswerList(answers, stexam);

        // Determine problem and subtest scores, add to the parameter set
        computeSubtestScores(stexam, params);

        // Determine grading rule results, and add them to the parameter set for use in outcome
        // processing.
        evaluateGradingRules(stexam, params);

        determineOutcomes(cache, stexam, params);

        // We have now assembled the student exam record, so insert into the database.
        return insertChallenge(cache, stexam);
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
                final AbstractProblemTemplate selected = problem == null ? null : problem.getSelectedProblem();

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

                        final int len = answers[id].length;
                        for (int i = 0; i < len; ++i) {

                            if (answers[id][i] instanceof Long) {
                                final int index = ((Long) answers[id][i]).intValue();

                                if (index >= 1 && index <= 5) {
                                    answerStr[index - 1] = (char) ((int) 'A' + index - 1);
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

                    final String key = subtest.subtestName + CoreConstants.DOT + (id / 100) + (id / 10 % 10)
                            + (id % 10);
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
                final String key = subtest.subtestName + CoreConstants.DOT + (id / 100) + (id / 10 % 10) + (id % 10);
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

        // If we have a "score" subtest, and we have a mastery score in the record, then
        // automatically create a "passed" grading rule. Then, as we go through, if there is
        // another explicit "passed" grading rule, it will override this one.
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
                    Log.severe("Error evaluating grading rule ", rule.gradingRuleName, " [", formula.toString(),
                            "]: ", result.toString(), "\n", getExam().toXmlString(0));

                    break;
                } else // Insert TRUE boolean parameter if result is PASS
                    if (ExamGradingRule.PASS_FAIL.equals(rule.getGradingRuleType()) && result instanceof Boolean) {
                        pass = ((Boolean) result).booleanValue();

                        if (pass) {
                            rule.result = result;

                            stexam.examGrades.put(rule.gradingRuleName, result);

                            final VariableBoolean param = new VariableBoolean(rule.gradingRuleName);
                            param.setValue(result);
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
                                Log.info("      PREREQ: ", formula + " = " + result);

                                if (result instanceof final Boolean resultBool) {
                                    if (!resultBool.booleanValue()) {
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

                                    // FIX PER CONVERSATION WITH KEN JAN 12 2017:
                                    // Go ahead and award the result (with "U" as the how-valid
                                    // field) but keep result in "denied" for record-keeping

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

                                    Log.info("      RETAINING NON-VALIDATED ACTION: ",
                                            action.type, CoreConstants.SPC, action.course);

                                    stexam.earnedCredit.add(action.course);
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
     * @param cache  the data cache
     * @param stexam the StudentExam object with exam data to be inserted
     * @return an error message if an error occurred
     * @throws SQLException if there was an error accessing the database
     */
    private String insertChallenge(final Cache cache, final StudentExamRec stexam) throws SQLException {

        final Object passing = stexam.examGrades.get("passing");

        final int startMin = stexam.start.getHour() * 60 + stexam.start.getMinute();
        final int finishMin = stexam.finish.getHour() * 60 + stexam.finish.getMinute();

        final RawStchallenge attempt = new RawStchallenge(stexam.studentId, stexam.course, stexam.examId,
                this.active.academicYear, stexam.finish.toLocalDate(), Integer.valueOf(startMin),
                Integer.valueOf(finishMin), getStudent().lastName, getStudent().firstName, getStudent().middleInitial,
                null, stexam.serialNumber, this.score, Boolean.TRUE.equals(passing) ? "Y" : "N", "P");

        for (final StudentExamAnswerRec ansrec : stexam.answers.values()) {
            final int finishTime = stexam.finish.getHour() * 60 + stexam.finish.getMinute();

            final RawStchallengeqa answer =
                    new RawStchallengeqa(stexam.studentId, stexam.course, stexam.examId, stexam.finish.toLocalDate(),
                            Integer.valueOf(finishTime), Integer.valueOf(ansrec.id), ansrec.studentAnswer,
                            ansrec.correct ? "Y" : "N");

            if (!RawStchallengeqaLogic.INSTANCE.insert(cache, answer)) {
                Log.warning("Failed to insert challenge attempt answer");
            }
        }

        // Update the placement log record
        final LocalDateTime start = stexam.start;

        final int startTime = start.getHour() * 60 + start.getMinute();
        RawMpeLogLogic.indicateFinished(cache, stexam.studentId, start.toLocalDate(), Integer.valueOf(startTime),
                stexam.finish.toLocalDate(), stexam.recovered == null ? null : stexam.recovered.toLocalDate());

        insertPlacementResults(cache, stexam);

        // Last thing is to insert the actual exam row. We do this last so other jobs can know
        // that if they see a row in this table, the associated data will be present and complete.
        if (!RawStchallengeLogic.INSTANCE.insert(cache, attempt)) {
            return "Failed to insert student challenge exam record";
        }

        return null;
    }

    /**
     * Insert a challenge exam object into the database.
     *
     * @param cache  the data cache
     * @param stexam the StudentExam object with exam data to be inserted
     * @throws SQLException if there is an error accessing the database
     */
    private void insertPlacementResults(final Cache cache, final StudentExamRec stexam)
            throws SQLException {

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
        for (final String placeIn : stexam.deniedCredit.keySet()) {
            final RawMpecrDenied denied = new RawMpecrDenied(stexam.studentId, placeIn, "C",
                    stexam.finish.toLocalDate(), stexam.deniedPlacement.get(placeIn), stexam.serialNumber,
                    stexam.examId, stexam.proctored ? "RM" : null);

            RawMpecrDeniedLogic.INSTANCE.insert(cache, denied);
        }

        // Record all ignored placement results
        for (final Map.Entry<String, String> entry : stexam.deniedPlacement.entrySet()) {
            final RawMpecrDenied denied = new RawMpecrDenied(stexam.studentId, entry.getKey(), "P",
                    stexam.finish.toLocalDate(), entry.getValue(), stexam.serialNumber,
                    stexam.examId, stexam.proctored ? "RM" : null);

            RawMpecrDeniedLogic.INSTANCE.insert(cache, denied);
        }

        // Send results to BANNER, or store in queue table
        final RawStudent stu = getStudent();

        if (stu == null) {
            RawMpscorequeueLogic.logActivity("Unable to upload challenge result for student " + stexam.studentId
                    + ": student record not found");
        } else if (!stexam.earnedCredit.isEmpty()) {

            final DbContext liveCtx = getDbProfile().getDbContext(ESchemaUse.LIVE);
            final DbConnection liveConn = liveCtx.checkOutConnection();
            try {
                for (final String course : stexam.earnedCredit) {
                    RawMpscorequeueLogic.INSTANCE.postChallengeCredit(cache, liveConn, stu.pidm, course, stexam.finish);
                }
            } finally {
                liveCtx.checkInConnection(liveConn);
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
            xml.addln("<challenge-exam-session>");
            xml.addln(" <host>", getSiteProfile().host, "</host>");
            xml.addln(" <path>", getSiteProfile().path, "</path>");
            xml.addln(" <session>", this.sessionId, "</session>");
            xml.addln(" <student>", this.studentId, "</student>");
            xml.addln(" <exam-id>", this.version, "</exam-id>");
            if (this.score != null) {
                xml.addln(" <score>", this.score, "</score>");
            }
            xml.addln(" <state>", this.state.name(), "</state>");
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
            xml.addln("</challenge-exam-session>");
        }
    }
}
