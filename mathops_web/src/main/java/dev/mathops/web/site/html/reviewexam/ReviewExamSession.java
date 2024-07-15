package dev.mathops.web.site.html.reviewexam;

import dev.mathops.assessment.InstructionalCache;
import dev.mathops.assessment.document.template.AbstractDocObjectTemplate;
import dev.mathops.assessment.document.template.DocColumn;
import dev.mathops.assessment.document.template.DocNonwrappingSpan;
import dev.mathops.assessment.document.template.DocParagraph;
import dev.mathops.assessment.document.template.DocText;
import dev.mathops.assessment.document.template.DocWrappingSpan;
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
import dev.mathops.commons.parser.xml.XmlEscaper;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.cfg.WebSiteProfile;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.old.logic.PlacementLogic;
import dev.mathops.db.old.logic.PlacementStatus;
import dev.mathops.db.old.rawlogic.RawExamLogic;
import dev.mathops.db.old.rawlogic.RawMpeCreditLogic;
import dev.mathops.db.old.rawlogic.RawMpecrDeniedLogic;
import dev.mathops.db.old.rawlogic.RawMpscorequeueLogic;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawStqaLogic;
import dev.mathops.db.old.rawlogic.RawStsurveyqaLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawlogic.RawSurveyqaLogic;
import dev.mathops.db.old.rawlogic.RawUsersLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawCusection;
import dev.mathops.db.old.rawrecord.RawExam;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawMpecrDenied;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStqa;
import dev.mathops.db.old.rawrecord.RawStsurveyqa;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.rawrecord.RawSurveyqa;
import dev.mathops.db.old.rawrecord.RawUsers;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.session.ExamWriter;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.servlet.ReviewExamEligibilityTester;
import dev.mathops.session.txn.handlers.AbstractHandlerBase;
import dev.mathops.session.txn.handlers.StudentExamAnswerRec;
import dev.mathops.session.txn.handlers.StudentExamRec;
import dev.mathops.session.txn.messages.AvailableExam;
import dev.mathops.session.txn.messages.GetExamReply;
import dev.mathops.session.txn.messages.GetReviewExamReply;
import dev.mathops.web.site.html.HtmlSessionBase;

import jakarta.servlet.ServletRequest;
import java.awt.Font;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A user session used to take review exams online. It takes as arguments a session ID, student name, and assignment ID
 * and presents the exam to the student.
 */
public final class ReviewExamSession extends HtmlSessionBase {

    /** The timeout duration (4 hours), in milliseconds. */
    private static final long TIMEOUT = (long) (4 * 60 * 60 * 1000);

    /** The achieved score. */
    private Integer score;

    /** The mastery score. */
    private Integer masteryScore;

    /** The state of the review exam. */
    private EReviewExamState state;

    /** The currently active item. */
    private int currentItem;

    /** Flag indicating assignment is practice. */
    public final boolean practice;

    /** Flag indicate exam has been started (controls button label on instructions). */
    private boolean started;

    /** An error encountered while grading the exam, null if none. */
    private String gradingError;

    /** Timestamp when exam will time out. */
    private long timeout;

    /**
     * Constructs a new {@code ReviewExamSession}. This is called when the user clicks a button to start a review exam.
     * It stores data but does not generate the HTML until the page is actually generated.
     *
     * @param cache            the data cache
     * @param theSiteProfile   the site profile
     * @param theSessionId     the session ID
     * @param theStudentId     the student ID
     * @param theExamId        the exam ID being worked on
     * @param isPractice       {@code true} if the exam is practice
     * @param theRedirectOnEnd the URL to which to redirect at the end of the exam
     * @throws SQLException if there is an error accessing the database
     */
    public ReviewExamSession(final Cache cache, final WebSiteProfile theSiteProfile,
                             final String theSessionId, final String theStudentId, final String theExamId,
                             final boolean isPractice, final String theRedirectOnEnd) throws SQLException {

        super(cache, theSiteProfile, theSessionId, theStudentId, theExamId, theRedirectOnEnd);

        this.practice = isPractice;
        this.state = EReviewExamState.INITIAL;
        this.currentItem = -1;
        this.started = false;
        this.timeout = System.currentTimeMillis() + TIMEOUT;
    }

    /**
     * Constructs a new {@code ReviewExamSession}. This is called when the user clicks a button to start a review exam.
     * It stores data but does not generate the HTML until the page is actually generated.
     *
     * @param cache            the data cache
     * @param theSiteProfile   the site profile
     * @param theSessionId     the session ID
     * @param theStudentId     the student ID
     * @param theExamId        the exam ID being worked on
     * @param isPractice       {@code true} if the exam is practice
     * @param theRedirectOnEnd the URL to which to redirect at the end of the exam
     * @param theState         the session state
     * @param theScore         the score
     * @param theMastery       the mastery score
     * @param theStarted       true if exam has been started
     * @param theItem          the current item
     * @param theTimeout       the timeout
     * @param theExam          the exam
     * @param theError         the grading error
     * @throws SQLException if there is an error accessing the database
     */
    ReviewExamSession(final Cache cache, final WebSiteProfile theSiteProfile,
                      final String theSessionId, final String theStudentId, final String theExamId,
                      final boolean isPractice, final String theRedirectOnEnd, final EReviewExamState theState,
                      final Integer theScore, final Integer theMastery, final boolean theStarted,
                      final int theItem, final long theTimeout, final ExamObj theExam, final String theError)
            throws SQLException {

        super(cache, theSiteProfile, theSessionId, theStudentId, theExamId, theRedirectOnEnd);

        this.practice = isPractice;
        this.state = theState;
        this.score = theScore;
        this.masteryScore = theMastery;
        this.started = theStarted;
        this.currentItem = theItem;
        this.timeout = theTimeout;
        setExam(theExam);
        this.gradingError = theError;
    }

    /**
     * Gets the exam state.
     *
     * @return the exam state
     */
    public EReviewExamState getState() {

        synchronized (this) {
            return this.state;
        }
    }

    /**
     * Gets the current item.
     *
     * @return the current item
     */
    public int getItem() {

        synchronized (this) {
            return this.currentItem;
        }
    }

    /**
     * Tests whether the exam has been started.
     *
     * @return {@code true} if the exam is started
     */
    public boolean isStarted() {

        synchronized (this) {
            return this.started;
        }
    }

    /**
     * Gets the time remaining in the exam.
     *
     * @return the time remaining, in milliseconds (0 if the exam has not been started)
     */
    public long getTimeRemaining() {

        synchronized (this) {
            return this.timeout == 0L ? 0L : this.timeout - System.currentTimeMillis();
        }
    }

    /**
     * Tests whether this session is timed out.
     *
     * @return {@code true} if timed out
     */
    public boolean isTimedOut() {

        synchronized (this) {
            return System.currentTimeMillis() >= this.timeout;
        }
    }

    /**
     * Generates HTML for the exam based on its current state.
     *
     * @param cache the data cache
     * @param now   the date/time to consider "now"
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    public void generateHtml(final Cache cache, final ZonedDateTime now, final HtmlBuilder htm)
            throws SQLException {

        synchronized (this) {
            this.timeout = System.currentTimeMillis() + TIMEOUT;

            switch (this.state) {
                case INITIAL:
                    doInitial(cache, now, htm);
                    break;

                case INSTRUCTIONS:
                    appendInstructionsHtml(htm);
                    break;

                case ITEM_NN:
                    appendExamHtml(htm);
                    break;

                case SUBMIT_NN:
                    appendSubmitConfirm(htm);
                    break;

                case COMPLETED:
                    appendCompletedHtml(htm);
                    break;

                case SOLUTION_NN:
                    appendSolutionHtml(htm);
                    break;

                default:
                    appendHeader(htm);
                    htm.addln("<div style='text-align:center; color:navy;'>").add("Unsupported state.").eDiv();
                    appendFooter(htm, "close", "Close", null, null, null, null);
                    htm.eDiv(); // outer DIV from header
                    break;
            }
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

        final String error;

        // Look up the exam and store it in an AvailableExam object.
        final AvailableExam avail = new AvailableExam();

        avail.exam = RawExamLogic.query(cache, this.version);

        if (avail.exam == null) {
            error = "No exam found with the requested version";
        } else {
            final String type = avail.exam.examType;

            boolean eligible;
            final HtmlBuilder reasons = new HtmlBuilder(100);
            final Collection<RawAdminHold> holds = new ArrayList<>(1);

            if ("R".equals(type) || "Q".equals(type)) {

                final String theVersion = avail.exam.version;

                if (this.practice) {
                    eligible = true;
                } else if ("R".equals(type)) {
                    final ReviewExamEligibilityTester examtest = new ReviewExamEligibilityTester(this.studentId);

                    eligible = examtest.isExamEligible(cache, now, avail, reasons, holds);
                    this.masteryScore = examtest.getMasteryScore(type);
                } else if ("UOOOO".equals(theVersion)) {
                    eligible = true;
                    // FIXME: This changes from term to term!
                    this.masteryScore = Integer.valueOf(16);
                } else if ("17ELM".equals(theVersion) || "7TELM".equals(theVersion)) {
                    // This exam had to be marked as EnumExamType.QUALIFYING_VAL to avoid a
                    // key duplication, but is treated as a review exam

                    final ReviewExamEligibilityTester examtest = new ReviewExamEligibilityTester(this.studentId);

                    eligible = examtest.isExamEligible(cache, now, avail, reasons, holds);

                    // Adjust mastery scores since ELM exam is twice as long
                    if (examtest.getMasteryScore(type) != null) {
                        if (examtest.getMasteryScore(type).intValue() == 8) {
                            this.masteryScore = Integer.valueOf(16);
                        } else if (examtest.getMasteryScore(type).intValue() == 7) {
                            this.masteryScore = Integer.valueOf(14);
                        } else if (examtest.getMasteryScore(type).intValue() == 6) {
                            this.masteryScore = Integer.valueOf(12);
                        } else {
                            this.masteryScore = examtest.getMasteryScore(type);
                        }
                    }
                } else {
                    try {
                        final RawStudent stu = getStudent();
                        final PlacementStatus placementStat = new PlacementLogic(cache, stu.stuId, stu.aplnTerm,
                                now).status;

                        eligible = placementStat.attemptsRemaining > 0;
                    } catch (final SQLException ex) {
                        Log.warning(ex);
                        eligible = false;
                    }

                    this.masteryScore = null;
                }

                if (eligible) {
                    // Generate a serial number for the exam
                    final long serial = AbstractHandlerBase.generateSerialNumber(this.practice);

                    final GetReviewExamReply reply = new GetReviewExamReply();
                    reply.status = GetExamReply.SUCCESS;

                    buildPresentedExam(avail.exam.treeRef, serial, reply, this.active);
                    error = reply.error;

                    if (error == null && "R".equals(type)) {

                        final String singular = "M 101".equals(avail.exam.course) ? "quiz" : "exam";
                        final String plural = "M 101".equals(avail.exam.course) ? "quizzes" : "exams";

                        final DocColumn newInstr = new DocColumn();
                        newInstr.tag = "instructions";

                        DocParagraph para = new DocParagraph();
                        para.setColorName("navy");
                        para.add(new DocText("Instructions:"));
                        newInstr.add(para);

                        para = new DocParagraph();
                        para.add(new DocText("This " + singular + " consists of " + getExam().getNumProblems()
                                + " questions. Your score will be based on the number of questions answered correctly. "
                                + "There is at least one correct response to each question. To correctly answer a "
                                + "question on this " + singular + ", you must choose "));

                        final DocWrappingSpan all = new DocWrappingSpan();
                        all.tag = "span";
                        all.setFontStyle(Integer.valueOf(Font.BOLD | Font.ITALIC));
                        all.add(new DocText("ALL"));
                        para.add(all);

                        para.add(new DocText(" correct responses to that question."));
                        newInstr.add(para);

                        newInstr.add(new DocParagraph());
                        newInstr.add(new DocParagraph());

                        final boolean hasProctored;

                        if (RawRecordConstants.M100T.equals(avail.exam.course)) {
                            hasProctored = avail.exam.unit.intValue() > 3;
                        } else {
                            hasProctored = true;
                        }

                        if (hasProctored) {
                            para = new DocParagraph();
                            para.setColorName("navy");

                            final DocNonwrappingSpan warn = new DocNonwrappingSpan();
                            warn.tag = "span";
                            warn.setFontStyle(Integer.valueOf(Font.BOLD | Font.ITALIC));
                            warn.add(new DocText("Reminder concerning " + plural
                                    + " taken in the Precalculus Center:"));
                            para.add(warn);
                            newInstr.add(para);

                            para = new DocParagraph();
                            para.add(new DocText("You are "));

                            final DocNonwrappingSpan not = new DocNonwrappingSpan();
                            not.tag = "span";
                            not.setFontStyle(Integer.valueOf(Font.BOLD | Font.ITALIC));
                            not.add(new DocText("NOT"));
                            para.add(not);

                            para.add(new DocText(" permitted to use reference materials of any kind during a proctored "
                                    + singular + ". Possession of reference materials, even if unintentional, will "
                                    + "result in an academic penalty and a University Disciplinary Hearing."));
                            newInstr.add(para);
                        }

                        // Apply time limit factor adjustment
                        if (getExam().allowedSeconds != null) {

                            final RawStudent stu = RawStudentLogic.query(cache, this.studentId, false);

                            if (stu != null && stu.timelimitFactor != null) {
                                avail.timelimitFactor = stu.timelimitFactor;

                                double secs = (double) getExam().allowedSeconds.intValue();

                                secs *= stu.timelimitFactor.doubleValue();

                                getExam().allowedSeconds = Long.valueOf(Math.round(secs));
                            }
                        }

                        getExam().instructions = newInstr;
                        getExam().getSection(0).enabled = true;
                    }
                } else {
                    error = reasons.toString();
                    Log.info("Not eligible for review exam: ", error);
                }
            } else {
                Log.info("Exam version '", this.version, "' is not a review exam.");
                error = "Requested exam is not a review exam.";
            }
        }

        if (error == null) {
            getExam().presentationTime = System.currentTimeMillis();
            this.state = EReviewExamState.INSTRUCTIONS;

            appendInstructionsHtml(htm);
        } else {
            Log.warning(error);
            htm.add(error);
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
    private void buildPresentedExam(final String ref, final long serial, final GetReviewExamReply reply,
                                    final TermRec term) {

        final ExamObj theExam = InstructionalCache.getExam(ref);

        if (theExam == null) {
            reply.status = GetExamReply.CANNOT_LOAD_EXAM_TEMPLATE;
            Log.warning("Unable to load template for " + ref);
        } else if (theExam.ref == null) {
            reply.status = GetExamReply.CANNOT_LOAD_EXAM_TEMPLATE;
            Log.warning("Errors loading exam template");
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

                        if (prb == null || prb.id == null) {
                            Log.warning("Exam " + ref + " section " + onSect + " problem " + onProb + " choice " + i
                                    + " getProblem() returned " + prb);
                        } else {
                            prb = InstructionalCache.getProblem(prb.id);

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
            appendFooter(htm, "nav_0", "Begin the exam...", null, null, null, null);
        }
        htm.eDiv(); // outer DIV from header
    }

    /**
     * Appends the HTML for the exam, showing the current item.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private void appendExamHtml(final HtmlBuilder htm) {

        appendHeader(htm);
        appendNav(htm, false);
        startMain(htm);

        final ExamSection sect = getExam().getSection(0);
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

        final String prevCmd = this.currentItem == 0 ? null : "nav_" + (this.currentItem - 1);
        final String nextCmd = this.currentItem >= (sect.getNumProblems() - 1) ? null : "nav_" + (this.currentItem + 1);

        endMain(htm);
        appendFooter(htm, "score", "I am finished.  Submit the exam for grading.",
                prevCmd, "Go to Question " + (this.currentItem),
                nextCmd, "Go to Question " + (this.currentItem + 2));
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

        final ExamObj exam = getExam();
        final ExamSection sect = exam.getSection(0);
        int numAnswered = 0;

        final int numProblems = sect.getNumProblems();
        for (int i = 0; i < numProblems; ++i) {
            final ExamProblem ep = sect.getProblem(i);
            if (ep.getSelectedProblem().isAnswered()) {
                ++numAnswered;
            }
        }

        htm.sDiv(null, "style='margin:20pt; padding:20pt; border:1px solid black; "
                + "background:white; text-align:center; color:Green;'");

        if (numAnswered == numProblems) {
            htm.sSpan(null, "style='color:green'");
            htm.addln("You have answered all ", Integer.toString(numProblems), " questions.").br().br();
        } else {
            htm.sSpan(null, "style='color:FireBrick'");
            htm.addln("You have only answered ", Integer.toString(numAnswered), " out of ",
                    Integer.toString(numProblems), " questions.").br().br();
        }

        if (exam.course.startsWith("MATH ")) {
            htm.addln("Do you wish to submit the assignment for grading?<br><br>");
            htm.add("  <input class='smallbtn' type='submit' name='Y' value='Yes - Submit the assignment'/> &nbsp;");
            htm.add("  <input class='smallbtn' type='submit' name='N' value='No - Return to the assignment'/>");
        } else {
            htm.addln("Do you wish to submit the review exam for grading?<br><br>");
            htm.add("  <input class='smallbtn' type='submit' name='Y' value='Yes - Submit the Exam'/> &nbsp;");
            htm.add("  <input class='smallbtn' type='submit' name='N' value='No - Return to the Exam'/>");
        }

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

        htm.sDiv(null, "style='margin:20pt; padding:20pt; border:1px solid black; "
                + "background:white; text-align:center; color:Green;'");

        htm.sSpan("style='color:green;'");
        htm.addln("Exam completed.").br().br();

        if (this.score != null) {
            htm.addln("Your score on this exam was ", this.score).br().br();
            if (this.masteryScore != null) {
                if (this.score.intValue() >= this.masteryScore.intValue()) {
                    htm.addln("This is a passing score.");
                } else {
                    htm.addln("This is not a passing score.");
                }
            }
        } else if (this.gradingError != null) {
            htm.addln(this.gradingError);
        }

        htm.eSpan().eDiv();

        endMain(htm);
        appendFooter(htm, "solutions", "View the exam solutions.", null, null, null, null);
        htm.eDiv(); // outer DIV from header
    }

    /**
     * Appends the HTML for the current item with solutions.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private void appendSolutionHtml(final HtmlBuilder htm) {

        appendHeader(htm);
        appendNav(htm, false);
        startMain(htm);

        final ExamSection sect = getExam().getSection(0);
        if (this.currentItem == -1) {
            if (getExam().instructionsHtml != null) {
                htm.addln(getExam().instructionsHtml);
            }
        } else {
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
                    htm.addln(p.insertAnswers(p.solutionHtml));
                }
            }
        }

        final String prevCmd = this.currentItem == 0 ? null : "nav_" + (this.currentItem - 1);
        final String nextCmd = this.currentItem >= (sect.getNumProblems() - 1) ? null : "nav_" + (this.currentItem + 1);

        endMain(htm);
        appendFooter(htm, "close", "Close",
                prevCmd, "Review Question " + (this.currentItem), //
                nextCmd, "Review Question " + (this.currentItem + 2));
        htm.eDiv(); // outer DIV from header
    }

    /**
     * Appends the header.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private void appendHeader(final HtmlBuilder htm) {

        htm.sDiv(null, "style='display:flex; flex-flow:row wrap; margin:0 6px 12px 6px;'");

        htm.sDiv(null, "style='flex: 1 100%; display:inline-block; background-color:AliceBlue; "
                + "border:1px solid SteelBlue; margin:1px;'");

        htm.add("<h1 style='text-align:center; font-family:sans-serif; font-size:18pt; ",
                "font-weight:bold; color:#36648b; text-shadow:2px 1px #ccc; margin:0; padding:4pt;'>");

        if (this.state == EReviewExamState.SOLUTION_NN) {
            htm.add(getExam().examName + " Solutions");
        } else {
            htm.add(getExam().examName);
        }
        htm.eH(1);

        htm.eDiv();
    }

    /**
     * Starts the "main" section.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private void startMain(final HtmlBuilder htm) {

        htm.addln("<main style='flex:1 1 73%; display:block; width:75%; margin:1px; padding:2px; ",
                "border:1px solid SteelBlue;'>");

        htm.addln(" <input type='hidden' name='currentItem' value='",
                Integer.toString(this.currentItem), "'>");

        htm.sDiv(null, "style='padding:8px; min-height:100%; border:1px solid #b3b3b3; "
                + "background:#f8f8f8; font-family:serif; font-size:"
                + AbstractDocObjectTemplate.DEFAULT_BASE_FONT_SIZE + "px;'");
    }

    /**
     * Ends the "main" section.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void endMain(final HtmlBuilder htm) {

        htm.eDiv();
        htm.addln("</main>");

        // Put this here so clicks can't call this until the page is loaded (and presumably item
        // answers have been installed in fields so this submit won't lose answers)

        htm.addln("<script>");
        htm.addln("function invokeAct(action) {");
        htm.addln("  if (actionAllowed == \"fine\") {");
        htm.addln("    document.getElementById(\"review_exam_act\").value = action;");
        htm.addln("    document.getElementById(\"review_exam_form\").submit();");
        htm.addln("  }");
        htm.addln("}");
        htm.addln("</script>");

        htm.addln("<script defer='true' async='false'>");
        htm.addln("  var actionAllowed = \"fine\";");
        htm.addln("</script>");
    }

    /**
     * Appends the navigation section.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param disabled {@code true} to disable the controls
     */
    private void appendNav(final HtmlBuilder htm, final boolean disabled) {

        htm.addln("<nav style='flex:1 1 22%; display:block; width:25%; background-color:AliceBlue; ",
                "border:1px solid SteelBlue; margin:1px; padding:6pt; font-size:14pt;'>");

        if ((this.state == EReviewExamState.INSTRUCTIONS)
                || (this.state == EReviewExamState.SOLUTION_NN && this.currentItem == -1)) {
            htm.sDiv(null, "style='background:#7FFF7F;'");
        } else {
            htm.sDiv();
        }

        htm.add("<a style='font-family:serif;'");
        if (!disabled) {
            htm.addln(" href='javascript:invokeAct(\"instruct\");'");
        }
        htm.addln(">Instructions</a>");
        htm.eDiv();

        final ExamSection sect = getExam().getSection(0);
        if (sect.shortName == null) {
            htm.addln("<h2 style='padding:6pt 0 3pt 0;color:SteelBlue'>", sect.sectionName, "</h2>");
        } else {
            htm.addln("<h2 style='padding:6pt 0 3pt 0;color:SteelBlue'>", sect.shortName, "</h2>");
        }

        final int numProblems = sect.getNumProblems();

        for (int p = 0; p < numProblems; ++p) {
            final ExamProblem ep = sect.getPresentedProblem(p);

            if (this.currentItem == p && (this.state == EReviewExamState.ITEM_NN
                    || this.state == EReviewExamState.SOLUTION_NN)) {
                htm.sDiv(null, "style='background:#7FFF7F;'");
            } else {
                htm.sDiv();
            }

            if (this.state == EReviewExamState.ITEM_NN
                    || this.state == EReviewExamState.INSTRUCTIONS
                    || this.state == EReviewExamState.SUBMIT_NN
                    || this.state == EReviewExamState.COMPLETED) {
                // When interacting or instructions, mark the ones that have been answered

                if (ep.getSelectedProblem().isAnswered()) {
                    htm.add("<input type='checkbox' disabled checked> ");
                } else {
                    htm.add("<input type='checkbox' disabled> ");
                }
            } else if (this.state == EReviewExamState.SOLUTION_NN) {
                // When interacting or instructions, mark the ones that were correct

                if (ep.getSelectedProblem().score <= 0.0) {
                    htm.add("<img src='images/redx.png'> ");
                } else {
                    htm.add("<img src='images/check.png'> ");
                }
            }

            htm.add("<a style='font-family:serif;'");
            if (!disabled) {
                htm.add(" href='javascript:invokeAct(\"nav_", Integer.toString(p), "\");'");
            }
            htm.add(">");
            if (ep.problemName == null) {
                htm.add(Integer.valueOf(p + 1));
            } else {
                htm.add(ep.problemName);
            }
            htm.addln("</a>");
            htm.eDiv();
        }

        htm.addln("</nav>");
    }

    /**
     * Appends an empty footer.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void appendEmptyFooter(final HtmlBuilder htm) {

        htm.sDiv(null, "style='flex: 1 100%; display:block; background-color:AliceBlue; "
                + "border:1px solid SteelBlue; margin:1px; padding:6pt; text-align:center;'");

        htm.eDiv();

        // End grid div
        htm.eDiv();
    }

    /**
     * Called when a POST is received on the page hosting the review exam.
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

        synchronized (this) {
            switch (this.state) {
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
                    processPostCompleted(cache, session, req, htm);
                    break;

                case SOLUTION_NN:
                    redirect = processPostSolution(cache, session, req, htm);
                    break;

                case INITIAL:
                default:
                    generateHtml(cache, session.getNow(), htm);
                    break;
            }
        }

        // The following synchronizes on the store's map - this CANNOT be done while in a block
        // synchronized on a session since doing so risks a race/deadlock.

        if (redirect != null) {
            ReviewExamSessionStore.getInstance().removeReviewExamSession(session.loginSessionId, this.version);
        }

        return redirect;
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

        if (req.getParameter("nav_0") != null) {
            this.currentItem = 0;
            this.state = EReviewExamState.ITEM_NN;
            this.started = true;
            appendExamLog("Starting review exam");
        } else if (req.getParameter("score") != null) {
            appendExamLog("'score' action received - confirming submit.");
            this.state = EReviewExamState.SUBMIT_NN;
        } else {
            final String act = req.getParameter("action");

            // Navigation ...
            final ExamSection sect = getExam().getSection(0);
            final int numProblems = sect.getNumProblems();
            for (int i = 0; i < numProblems; ++i) {
                if (("nav_" + i).equals(act)) {
                    this.currentItem = i;
                    this.state = EReviewExamState.ITEM_NN;
                    this.started = true;
                    appendExamLog("Starting review exam");
                    break;
                }
            }
        }

        generateHtml(cache, session.getNow(), htm);
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

        if (this.currentItem != -1) {
            final String reqItem = req.getParameter("currentItem");

            if (Integer.toString(this.currentItem).equals(reqItem)) {
                final ExamProblem ep = getExam().getSection(0).getPresentedProblem(this.currentItem);
                if (ep != null) {
                    final AbstractProblemTemplate p = ep.getSelectedProblem();
                    final Map<String, String[]> params = req.getParameterMap();
                    p.extractAnswers(params);

                    final Object[] answers = p.getAnswer();
                    final HtmlBuilder builder = new HtmlBuilder(100);
                    builder.add("Item ", Integer.toString(this.currentItem), " answers {");
                    if (answers != null) {
                        for (final Object o : answers) {
                            builder.add(CoreConstants.SPC, o, " (", o.getClass().getSimpleName(), ")");
                        }
                        builder.add(" }, correct=", p.isCorrect(answers) ? "Y" : "N");
                    } else {
                        builder.add("}");
                    }
                    appendExamLog(builder.toString());
                }
            } else {
                Log.warning("POST received with currentItem='", reqItem, "' when current item was ",
                        Integer.toString(this.currentItem));
            }
        }

        if (req.getParameter("score") != null) {
            appendExamLog("'score' action received - confirming submit.");
            this.state = EReviewExamState.SUBMIT_NN;
        } else {
            final String act = req.getParameter("action");

            if ("instruct".equals(act)) {
                this.state = EReviewExamState.INSTRUCTIONS;
            } else {
                // Navigation ...
                final ExamSection sect = getExam().getSection(0);
                final int numProblems = sect.getNumProblems();
                for (int i = 0; i < numProblems; ++i) {
                    if (("nav_" + i).equals(act)) {
                        this.currentItem = i;
                        break;
                    }
                }
            }
        }

        final ZonedDateTime now = session.getNow();
        generateHtml(cache, now, htm);
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

        final ZonedDateTime now = session.getNow();

        if (req.getParameter("N") != null) {
            appendExamLog("Submit canceled, returning to exam");
            this.state = EReviewExamState.ITEM_NN;
        } else if (req.getParameter("Y") != null) {
            appendExamLog("Submit confirmed, scoring...");
            this.gradingError = scoreAndRecordCompletion(cache, now);
            this.state = EReviewExamState.COMPLETED;
        }

        generateHtml(cache, now, htm);
    }

    /**
     * Called when a POST is received while in the COMPLETED state.
     *
     * @param cache   the data cache
     * @param session the user session
     * @param req     the servlet request
     * @param htm     the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private void processPostCompleted(final Cache cache, final ImmutableSessionInfo session,
                                      final ServletRequest req, final HtmlBuilder htm) throws SQLException {

        if (req.getParameter("solutions") != null) {
            appendExamLog("Moving to solutions...");
            this.currentItem = 0;
            this.state = EReviewExamState.SOLUTION_NN;
        }

        final ZonedDateTime now = session.getNow();
        generateHtml(cache, now, htm);
    }

    /**
     * Called when a POST is received while in the SOLUTIONS state.
     *
     * @param cache   the data cache
     * @param session the user session
     * @param req     the servlet request
     * @param htm     the {@code HtmlBuilder} to which to append
     * @return a string to which to redirect; null if no redirection should occur
     * @throws SQLException if there is an error accessing the database
     */
    private String processPostSolution(final Cache cache, final ImmutableSessionInfo session,
                                       final ServletRequest req, final HtmlBuilder htm) throws SQLException {

        String redirect = null;

        if (req.getParameter("close") != null) {
            appendExamLog("Closing session.");
            setExam(null);
            redirect = this.redirectOnEnd;
        } else {
            final String act = req.getParameter("action");

            if ("instruct".equals(act)) {
                this.currentItem = -1;
            } else {
                // Navigation ...
                final ExamSection sect = getExam().getSection(0);
                final int numProblems = sect.getNumProblems();
                for (int i = 0; i < numProblems; ++i) {
                    if (("nav_" + i).equals(act)) {
                        this.currentItem = i;
                        break;
                    }
                }
            }

            generateHtml(cache, session.getNow(), htm);
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
    public void forceAbort(final Cache cache, final ImmutableSessionInfo session) throws SQLException {

        if (session.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
            appendExamLog("Forced abort requested");
            synchronized (this) {
                writeExamRecovery(cache);
                if (getExam() != null) {
                    setExam(null);
                }
            }

            // The following synchronizes on the store's map - this CANNOT be done while in a block
            // synchronized on a session since doing so risks a race/deadlock.

            ReviewExamSessionStore.getInstance().removeReviewExamSession(this.sessionId, this.version);
        } else {
            appendExamLog("Forced abort requested, but requester is not ADMINISTRATOR");
        }
    }

    /**
     * Performs a forced submit of a placement exam session.
     *
     * @param cache   the data cache
     * @param session the login session requesting the forced submit
     * @throws SQLException if there is an error accessing the database
     */
    public void forceSubmit(final Cache cache, final ImmutableSessionInfo session) throws SQLException {

        if (session == null) {
            appendExamLog("Review exam timed out after being started - submitting");

            synchronized (this) {
                writeExamRecovery(cache);
                this.gradingError = scoreAndRecordCompletion(cache, ZonedDateTime.now());
                this.state = EReviewExamState.COMPLETED;

                if (getExam() != null) {
                    setExam(null);
                }
            }
            // No need to remove from store - store is making this call and will remove.

        } else if (session.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
            appendExamLog("Forced submit requested");

            synchronized (this) {
                writeExamRecovery(cache);
                this.gradingError = scoreAndRecordCompletion(cache, session.getNow());
                this.state = EReviewExamState.COMPLETED;

                if (getExam() != null) {
                    setExam(null);
                }
            }

            // The following synchronizes on the store's map - this CANNOT be done while in a block
            // synchronized on a session since doing so risks a race/deadlock.

            ReviewExamSessionStore.getInstance().removeReviewExamSession(this.sessionId, this.version);
        } else {
            appendExamLog("Forced submit requested, but requester is not ADMINISTRATOR");
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
    private String scoreAndRecordCompletion(final Cache cache,
                                            final ChronoZonedDateTime<LocalDate> now) throws SQLException {

        final String error;

        final String stuId = this.studentId;

        if ("GUEST".equals(stuId) || "AACTUTOR".equals(stuId)) {
            error = "Guest login exams will not be recorded.";
        } else if ("ETEXT".equals(stuId)) {
            error = "Practice exams will not be recorded.";
        } else if (stuId.startsWith("99")) {
            error = "Test student exams will not be recorded.";
        } else {
            Log.info("Writing updated exam state");

            final Object[][] answers = getExam().exportState();

            loadStudentInfo(cache);

            // Write the updated exam state out somewhere permanent
            new ExamWriter().writeUpdatedExam(stuId, this.active, answers, false);

            error = finalizeExam(cache, now, answers);
        }

        if (error != null) {
            Log.warning(error);
        }

        return error;
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
    private String finalizeExam(final Cache cache, final ChronoZonedDateTime<LocalDate> now,
                                final Object[][] answers) throws SQLException {

        final String crsId = getExam().course;

        final SystemData systemData = cache.getSystemData();

        final Boolean courseIsTutorial = systemData.isCourseTutorial(crsId);
        if (courseIsTutorial == null) {
            return "No data for course '" + getExam().course + "'";
        }

        // Store the presentation and completion times in the exam object
        if (answers[0].length == 4) {
            // If exam has both presentation and completion time, compute the duration as seen by
            // the client, then adjust for the server's clock
            if (answers[0][2] != null && answers[0][3] != null) {
                final long duration = ((Long) answers[0][3]).longValue() - ((Long) answers[0][2]).longValue();

                if (duration >= 0L && duration < 43200L) {
                    getExam().presentationTime = System.currentTimeMillis() - duration;
                } else {
                    // Time was not reasonable, so set to 0 time.
                    Log.warning("Client gave exam duration as " + duration);
                    getExam().presentationTime = System.currentTimeMillis();
                }
            } else {
                getExam().presentationTime = getExam().realizationTime;
            }

            // Set the completion time
            getExam().finalizeExam();
        }

        // See if the exam has already been inserted
        final Long ser = getExam().serialNumber;
        final LocalDateTime start = TemporalUtils.toLocalDateTime(getExam().realizationTime);

        final List<RawStexam> existing = RawStexamLogic.getExams(cache, this.studentId, crsId, true);
        for (final RawStexam test : existing) {
            if (test.serialNbr.equals(ser) && test.getStartDateTime().equals(start)) {
                return "This exam has already been submitted.";
            }
        }

        final EvalContext params = new EvalContext();

        final VariableBoolean param1 = new VariableBoolean("proctored");
        param1.setValue(Boolean.FALSE);
        params.addVariable(param1);

        Log.info("Grading review exam for student ", this.studentId, ", exam ", getExam().examVersion);

        final RawExam examRec = RawExamLogic.query(cache, this.version);
        if (examRec == null) {
            return "Exam " + this.version + " not found!";
        }

        String exType = examRec.examType;

        // FIXME: Make sure 17ELM is recorded as a review exam and not a Q exam
        if ("17ELM".equals(examRec.version) || "7TELM".equals(examRec.version)) {
            exType = "R";
        }

        // Begin preparing the database object to store exam results
        final StudentExamRec stexam = new StudentExamRec();
        stexam.studentId = this.studentId;
        stexam.examType = exType;
        stexam.course = getExam().course;
        try {
            stexam.unit = Integer.valueOf(getExam().courseUnit);
        } catch (final NumberFormatException ex) {
            Log.warning("Failed to parse unit", ex);
        }
        stexam.examId = getExam().examVersion;
        stexam.proctored = false;
        stexam.start = TemporalUtils.toLocalDateTime(getExam().realizationTime);
        stexam.finish = TemporalUtils.toLocalDateTime(getExam().completionTime);
        stexam.serialNumber = getExam().serialNumber;

        if ("Q".equals(exType)) {
            if (RawRecordConstants.M100U.equals(stexam.course)) {
                // FIXME: Hardcode
                final RawCusection result = systemData.getCourseUnitSection(stexam.course, "1", stexam.unit,
                        this.active.term);
                if (result == null) {
                    return "Unable to look up course section";
                }
                stexam.masteryScore = result.ueMasteryScore;
            }
        } else {
            RawStcourse stcourse = RawStcourseLogic.getRegistration(cache, stexam.studentId, stexam.course);

            if (stcourse == null) {
                if (courseIsTutorial.booleanValue()) {

                    // Create a fake STCOURSE record
                    stcourse = new RawStcourse(this.active.term, // term
                            stexam.studentId, // stuId
                            stexam.course, // course
                            "1", // sect
                            null, // paceOrder
                            "Y", // openStatus
                            null, // gradingOption,
                            "N", // completed
                            null, // score
                            null, // courseGrade
                            "Y", // prereqSatisfied
                            "N", // initClasRroll
                            "N", // stuProvided
                            "N", // finalClassRoll
                            null, // examPlaced
                            null, // zeroUnit
                            null, // timeoutFactor
                            null, // forfeitI
                            "N", // iInProgress
                            null, // iCounted
                            "N", // ctrlText
                            null, // deferredFDt
                            Integer.valueOf(0), // bypassTimeout
                            null, // instrnType
                            null, // registrationStatus
                            null, // lastClassRollDt
                            null, // iTermKey
                            null); // iDeadlineDt

                    stcourse.synthetic = true;

                } else {
                    boolean isSpecial = false;

                    // 'TUTOR', 'ADMIN' special student types automatically in section "001"
                    // for 117, 118, 124, 125, 126.
                    if (RawRecordConstants.M117.equals(stexam.course)
                            || RawRecordConstants.M118.equals(stexam.course)
                            || RawRecordConstants.M124.equals(stexam.course)
                            || RawRecordConstants.M125.equals(stexam.course)
                            || RawRecordConstants.M126.equals(stexam.course)
                            || RawRecordConstants.MATH117.equals(stexam.course)
                            || RawRecordConstants.MATH118.equals(stexam.course)
                            || RawRecordConstants.MATH124.equals(stexam.course)
                            || RawRecordConstants.MATH125.equals(stexam.course)
                            || RawRecordConstants.MATH126.equals(stexam.course)) {

                        final List<RawSpecialStus> specials = RawSpecialStusLogic
                                .queryActiveByStudent(cache, this.studentId, now.toLocalDate());

                        for (final RawSpecialStus special : specials) {
                            final String type = special.stuType;

                            if ("TUTOR".equals(type) || "M384".equals(type) || "ADMIN".equals(type)) {
                                isSpecial = true;
                                break;
                            }
                        }
                    }

                    if (isSpecial) {
                        final List<RawCsection> sections = systemData.getCourseSectionsByCourse(stexam.course,
                                this.active.term);

                        if (sections.isEmpty()) {
                            return "No sections configured";
                        } else {
                            // Create a fake STCOURSE record
                            sections.sort(null);
                            final RawCsection sect = sections.getFirst();

                            stcourse = new RawStcourse(this.active.term, // term
                                    stexam.studentId, // stuId
                                    stexam.course, // course
                                    sect.sect, // sect
                                    null, // paceOrder
                                    "Y", // openStatus
                                    null, // gradingOption,
                                    "N", // completed
                                    null, // score
                                    null, // courseGrade
                                    "Y", // prereqSatis
                                    "N", // initClassRoll
                                    "N", // stuProvided
                                    "N", // finalClassRoll
                                    null, // examPaced
                                    null, // zeroUnit
                                    null, // timeoutFactor
                                    null, // forfeitI
                                    "N", // iInProgress
                                    null, // iCounted
                                    "N", // ctrlTest
                                    null, // deferredFDt
                                    Integer.valueOf(0), // bypassTimeout
                                    null, // instrnType
                                    null, // registrationStatus
                                    null, // lastClassRollDate
                                    null, // iTermKey
                                    null); // iDeadlineDt

                            stcourse.synthetic = true;
                        }
                    } else{
                        return "Unable to look up course registration.";
                    }
                }
            }

            RawCusection cusect;

            if ("Y".equals(stcourse.iInProgress)) {
                cusect = systemData.getCourseUnitSection(stexam.course, stcourse.sect, stexam.unit, stcourse.iTermKey);
                if (cusect == null) {
                    cusect = systemData.getCourseUnitSection(stexam.course, stcourse.sect, stexam.unit,
                            this.active.term);
                }
            } else {
                cusect = systemData.getCourseUnitSection(stexam.course, stcourse.sect, stexam.unit, this.active.term);
            }

            if (cusect == null) {
                return "Unable to look up course section.";
            }

            if ("U".equals(exType) || "F".equals(exType)) {
                stexam.masteryScore = cusect.ueMasteryScore;
            } else if ("R".equals(exType)) {
                stexam.masteryScore = cusect.reMasteryScore;
            }

            // FIXME: Double the mastery score for longer skills review exams
            if (stexam.masteryScore != null && ("17ELM".equals(examRec.version) || "7TELM".equals(examRec.version))) {

                // Double scores for the extended exams
                stexam.masteryScore = Integer.valueOf(stexam.masteryScore.intValue() << 1);
            }
        }

        // Generate the list of problem answers, store in exam record
        buildAnswerList(answers, stexam);

        // Determine problem and subtest scores, add to the parameter set
        computeSubtestScores(stexam, params);

        // Determine grading rule results, and add them to the parameter set for use in outcome
        // processing.
        final boolean passed = evaluateGradingRules(stexam, params);

        determineOutcomes(cache, stexam, params);

        // We have now assembled the student exam record, so insert into the database.
        return insertStudentExam(cache, stexam, passed);
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

                if (selected != null && selected.id != null) {
                    final StudentExamAnswerRec stanswer = new StudentExamAnswerRec();
                    stanswer.id = id;
                    stanswer.subtest = subtest.subtestName;
                    stanswer.treeRef = selected.id;

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
            Log.info("  Subtest '", subtest.subtestName, "' score ", subtest.score);

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
                param.setValue(Boolean.TRUE);
            } else {
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
                            rule.result = Boolean.TRUE;

                            stexam.examGrades.put(rule.gradingRuleName, Boolean.TRUE);

                            final VariableBoolean param = new VariableBoolean(rule.gradingRuleName);
                            param.setValue(Boolean.TRUE);
                            params.addVariable(param);

                            break;
                        }
                    }
            }

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

                    // See if the outcome should be awarded based on grading rules
                    if (doOutcome) {

                        // Test for needed prerequisites; if any are not satisfied deny the outcome
                        final Iterator<ExamOutcomePrereq> prereqs = outcome.getPrereqs();

                        while (prereqs.hasNext() && whyDeny == null) {
                            final ExamOutcomePrereq prereq = prereqs.next();
                            formula = prereq.prerequisiteFormula;

                            if (formula != null) {
                                result = formula.evaluate(params);

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

                                    stexam.earnedPlacement.add(action.course);
                                } else if (RawMpecrDeniedLogic.DENIED_BY_VAL.equals(whyDeny)) {

                                    // FIX PER CONVERSATION WITH KEN JAN 12 2017:
                                    // Go ahead and award the result (with "U" as the how-valid
                                    // field) but keep result in "denied" for record-keeping

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

                                    stexam.earnedCredit.add(action.course);

                                } else if (RawMpecrDeniedLogic.DENIED_BY_VAL.equals(whyDeny)) {

                                    // FIX PER CONVERSATION WITH KEN JAN 12 2017:
                                    // Go ahead and award the result (with "U" as the how-valid
                                    // field) but keep result in "denied" for record-keeping

                                    stexam.earnedCredit.add(action.course);
                                    if (!stexam.deniedCredit.containsKey(action.course)) {
                                        stexam.deniedCredit.put(action.course, whyDeny);
                                    }
                                    validBy = "U";

                                } else if (outcome.logDenial && !stexam.deniedCredit.containsKey(action.course)) {
                                    stexam.deniedCredit.put(action.course, whyDeny);
                                }
                            } else if (ExamOutcomeAction.INDICATE_LICENSED.equals(action.type)) {

                                final RawStudent stu = RawStudentLogic.query(cache, this.studentId, false);
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
                    Log.warning("Outcome formula [", outcome.condition.toString(), "] did not evaluate to boolean:",
                            getExam().toXmlString(0));
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
     * Insert this object into the database.
     *
     * @param cache       the data cache
     * @param stexam      the StudentExam object with exam data to be inserted
     * @param usersPassed {@code true} if the score was passing; {@code false} if not (used only for user's exam)
     * @return {@code null} on success; an error message on any failure
     * @throws SQLException if there is an error accessing the database
     */
    private String insertStudentExam(final Cache cache, final StudentExamRec stexam,
                                     final boolean usersPassed) throws SQLException {

        String error;

        if (RawRecordConstants.M100T.equals(stexam.course)
                || RawRecordConstants.M1170.equals(stexam.course)
                || RawRecordConstants.M1180.equals(stexam.course)
                || RawRecordConstants.M1240.equals(stexam.course)
                || RawRecordConstants.M1250.equals(stexam.course)
                || RawRecordConstants.M1260.equals(stexam.course)) {
            error = insertExam(cache, stexam);

            if (error == null) {
                error = insertPlacementResults(cache, stexam);
            }
        } else if (RawRecordConstants.M100U.equals(stexam.course)) {
            error = insertUsersExam(cache, stexam, usersPassed);
            if (error == null) {
                error = insertExam(cache, stexam);
            }
        } else {
            error = insertExam(cache, stexam);
        }

        return error;
    }

    /**
     * Insert a standard (non-placement) exam object into the database.
     *
     * @param cache  the data cache
     * @param stexam the StudentExam object with exam data to be inserted
     * @return {@code null} on success; an error message on any failure
     * @throws SQLException if there is an error accessing the database
     */
    private static String insertExam(final Cache cache, final StudentExamRec stexam) throws SQLException {

        Log.info("Trying to insert stexam record");

        final Object passedVal = stexam.examGrades.get("passed");

        final String passed;
        if (stexam.serialNumber != null && stexam.serialNumber.intValue() < 0) {
            passed = "C";
        } else if (passedVal instanceof Boolean) {
            passed = Boolean.TRUE.equals(passedVal) ? "Y" : "N";
        } else {
            passed = "N";
        }

        final LocalDateTime start = stexam.start;
        final LocalDateTime fin = stexam.finish;

        final int startInt = start.getHour() * 60 + start.getMinute();
        final int finInt = fin.getHour() * 60 + fin.getMinute();

        final RawStexam record = new RawStexam(stexam.serialNumber, stexam.examId, stexam.studentId, fin.toLocalDate(),
                stexam.subtestScores.get("score"), stexam.masteryScore, Integer.valueOf(startInt),
                Integer.valueOf(finInt), "Y", passed, null, stexam.course, stexam.unit, stexam.examType, "N",
                stexam.examSource, null);

        String error = null;
        if (RawStexamLogic.INSTANCE.insert(cache, record)) {

            RawStexamLogic.recalculateFirstPassed(cache, stexam.studentId, stexam.course, stexam.unit, stexam.examType);

            // Loop through answers, inserting records.
            int question = 1;

            for (final StudentExamAnswerRec ansrec : stexam.answers.values()) {
                final RawStqa answer = new RawStqa(stexam.serialNumber, Integer.valueOf(question), Integer.valueOf(1),
                        ansrec.objective, ansrec.studentAnswer, stexam.studentId, stexam.examId,
                        ansrec.correct ? "Y" : "N", fin.toLocalDate(), ansrec.subtest, Integer.valueOf(finInt));

                RawStqaLogic.INSTANCE.insert(cache, answer);
                ++question;
            }
        } else {
            error = "Failed to innsert exam record.";
        }

        return error;
    }

    /**
     * Insert a placement exam object into the database.
     *
     * @param cache  the data cache
     * @param stexam the StudentExam object with exam data to be inserted
     * @return {cod null} if object inserted, an error message if an error occurred
     * @throws SQLException if there is an error accessing the database
     */
    private String insertPlacementResults(final Cache cache, final StudentExamRec stexam) throws SQLException {

        // Indicate all required placements.
        for (final String placeIn : stexam.earnedPlacement) {
            if (stexam.earnedCredit.contains(placeIn)) {
                // If credit is awarded, we don't award placement too
                continue;
            }

            final RawMpeCredit credit = new RawMpeCredit(stexam.studentId, placeIn, "P", stexam.finish.toLocalDate(),
                    null, stexam.serialNumber, stexam.examId, null);

            RawMpeCreditLogic.INSTANCE.apply(cache, credit);
        }

        // Record all ignored placement results
        for (final Map.Entry<String, String> entry : stexam.deniedPlacement.entrySet()) {
            final RawMpecrDenied denied = new RawMpecrDenied(stexam.studentId, entry.getKey(), "P",
                    stexam.finish.toLocalDate(), entry.getValue(), stexam.serialNumber, stexam.examId, null);

            RawMpecrDeniedLogic.INSTANCE.insert(cache, denied);
        }

        // Passing ELM exam unit 3 review sends score of "4" for MC00 test code to Banner

        if (RawRecordConstants.M100T.equals(stexam.course) && Integer.valueOf(3).equals(stexam.unit)
                && stexam.subtestScores.get("score") != null && stexam.subtestScores.get("score").intValue() >= 14) {

            // Send results to BANNER, or store in queue table
            final RawStudent stu = getStudent();

            if (stu == null) {
                RawMpscorequeueLogic.logActivity("Unable to upload placement result for student " + stexam.studentId
                        + ": student record not found");
            } else {
                final DbContext liveCtx = getDbProfile().getDbContext(ESchemaUse.LIVE);
                final DbConnection liveConn = liveCtx.checkOutConnection();
                try {
                    RawMpscorequeueLogic.INSTANCE.postELMUnit3ReviewPassed(cache, liveConn, stu.pidm, stexam.finish);
                } finally {
                    liveCtx.checkInConnection(liveConn);
                }
            }
        }

        return null;
    }

    /**
     * Insert a users exam object into the database.
     *
     * @param cache  the data cache
     * @param stexam the StudentExam object with exam data to be inserted
     * @param passed {@code true} if the score was passing; {@code false} if not
     * @return {cod null} if object inserted, an error message if an error occurred
     * @throws SQLException if there is an error accessing the database
     */
    private String insertUsersExam(final Cache cache, final StudentExamRec stexam,
                                   final boolean passed) throws SQLException {

        final RawStudent stu = RawStudentLogic.query(cache, stexam.studentId, false);
        if (stu == null) {
            return "User's exam for student " + this.studentId + ", student not found";
        }

        // Look up the calculus section reported
        // FIXME: Hardcode question number
        String calc = CoreConstants.SPC;

        final List<RawStsurveyqa> resp = RawStsurveyqaLogic.queryLatestByStudentProfile(cache, stexam.studentId,
                "UOOOO");

        String answer = null;
        for (final RawStsurveyqa rawStsurveyqa : resp) {
            if (rawStsurveyqa.surveyNbr.intValue() == 6) {
                answer = rawStsurveyqa.stuAnswer;
                break;
            }
        }

        if (answer != null) {
            final List<RawSurveyqa> possible = RawSurveyqaLogic.queryByVersionAndQuestion(cache, "UOOOO",
                    Integer.valueOf(1));

            for (final RawSurveyqa rawSurveyqa : possible) {
                if (answer.equals(rawSurveyqa.answer)) {
                    calc = rawSurveyqa.answerMeaning;
                    RawStudentLogic.updateCourseOrder(cache, stu.stuId, calc);
                    break;
                }
            }
        }

        final RawUsers attempt =
                new RawUsers(this.active.term, stexam.studentId, stexam.serialNumber, stexam.examId,
                        stexam.finish.toLocalDate(), stexam.subtestScores.get("score"), calc, passed ? "Y" : "N");

        String error = null;
        if (!RawUsersLogic.INSTANCE.insert(cache, attempt)) {
            error = "Failed to inser record of User's exam";
        }

        return error;
    }

    /**
     * Appends the XML representation of this session to an {@code HtmlBuilder}.
     *
     * @param xml the {@code HtmlBuilder} to which to append
     */
    void appendXml(final HtmlBuilder xml) {

        if (getExam() != null) {
            xml.addln("<review-exam-session>");
            xml.addln(" <host>", getSiteProfile().host, "</host>");
            xml.addln(" <path>", getSiteProfile().path, "</path>");
            xml.addln(" <session>", this.sessionId, "</session>");
            xml.addln(" <student>", this.studentId, "</student>");
            xml.addln(" <exam-id>", this.version, "</exam-id>");
            if (this.score != null) {
                xml.addln(" <score>", this.score, "</score>");
            }
            if (this.masteryScore != null) {
                xml.addln(" <mastery>", this.masteryScore, "</mastery>");
            }
            xml.addln(" <state>", this.state.name(), "</state>");
            xml.addln(" <cur-item>", Integer.toString(this.currentItem), "</cur-item>");
            if (this.practice) {
                xml.addln(" <practice/>");
            }
            xml.addln(" <redirect>", XmlEscaper.escape(this.redirectOnEnd), "</redirect>");
            if (this.started) {
                xml.addln(" <started/>");
            }
            if (this.gradingError != null) {
                xml.addln(" <error>", this.gradingError, "</error>");
            }
            xml.addln(" <timeout>", Long.toString(this.timeout), "</timeout>");
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
            xml.addln("</review-exam-session>");
        }
    }
}
