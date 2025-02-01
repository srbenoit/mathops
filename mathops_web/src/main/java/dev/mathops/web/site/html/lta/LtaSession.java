package dev.mathops.web.site.html.lta;

import dev.mathops.assessment.InstructionalCache;
import dev.mathops.assessment.document.template.AbstractDocObjectTemplate;
import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.assessment.exam.ExamProblem;
import dev.mathops.assessment.exam.ExamSection;
import dev.mathops.assessment.exam.ExamSubtest;
import dev.mathops.assessment.exam.ExamSubtestProblem;
import dev.mathops.assessment.htmlgen.ExamObjConverter;
import dev.mathops.assessment.htmlgen.ProblemConverter;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.assessment.problem.template.ProblemAutoCorrectTemplate;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.assessment.variable.VariableBoolean;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.Cache;
import dev.mathops.db.old.cfg.WebSiteProfile;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawSthomeworkLogic;
import dev.mathops.db.old.rawlogic.RawSthwqaLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.old.rawrecord.RawSthwqa;
import dev.mathops.db.rec.AssignmentRec;
import dev.mathops.db.rec.TermRec;
import dev.mathops.session.ExamWriter;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.servlet.LtaEligibilityTester;
import dev.mathops.session.txn.handlers.AbstractHandlerBase;
import dev.mathops.session.txn.messages.GetExamReply;
import dev.mathops.session.txn.messages.GetReviewExamReply;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;
import dev.mathops.text.parser.xml.XmlEscaper;
import dev.mathops.web.site.html.HtmlSessionBase;
import jakarta.servlet.ServletRequest;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A user session used to take learning target assignments online. It takes as arguments a session ID, student name, and
 * assignment ID and presents the learning target assignment to the student. When the assignment is complete, the
 * student submits it for grading, after which the student is shown a summary page of the results.
 *
 * <p>
 * A learning target assignment is stored in the "homework" table with a homework type of "ST".  However, it will be
 * presented in a manner similar to a Review Exam.  Students can switch between items and can submit the assignment as a
 * whole rather than having to proceed linearly.  Also, once they get a question correct, they do not have to redo that
 * question (although they can practice that item as much as they like).
 */
public final class LtaSession extends HtmlSessionBase {

    /** The timeout duration (2 hours), in milliseconds. */
    private static final long TIMEOUT = 2 * 60 * 60 * 1000;

    /** The background color for header/footer. */
    private static final String HEADER_BG_COLOR = "#EFEFF2";

    /** The background color for main question area. */
    private static final String MAIN_BG_COLOR = "#F5F5F5";

    /** The outline color for screen areas. */
    private static final String OUTLINE_COLOR = "#B3B3B3";

    /** The state of the assignment. */
    private ELtaState state;

    /** The currently active section. */
    private final int currentSection;

    /** The currently active item. */
    private int currentItem;

    /** Minimum mastery score for the assignment. */
    private Integer minMastery;

    /** Timestamp when exam will time out. */
    private long timeout;

    /** Flag indicate exam has been started (controls button label on instructions). */
    private boolean started;

    /** The achieved score. */
    private final Integer score;

    /** An error encountered while grading the exam, null if none. */
    private String gradingError;

    /**
     * Constructs a new {@code LtaSession}. This is called when the user clicks a button to start an assignment. It
     * stores data but does not generate the HTML until the page is actually generated.
     *
     * @param cache            the data cache
     * @param theSiteProfile   the site profile
     * @param theSessionId     the session ID
     * @param theStudentId     the student ID
     * @param theExamId        the assignment ID being worked on
     * @param theRedirectOnEnd the URL to which to redirect at the end of the assignment
     * @throws SQLException if there is an error accessing the database
     */
    public LtaSession(final Cache cache, final WebSiteProfile theSiteProfile, final String theSessionId,
                      final String theStudentId, final String theExamId, final String theRedirectOnEnd)
            throws SQLException {

        super(cache, theSiteProfile, theSessionId, theStudentId, theExamId, theRedirectOnEnd);

        this.state = ELtaState.INITIAL;
        this.currentSection = -1;
        this.currentItem = -1;
        this.started = false;
        this.score = null;
        this.timeout = System.currentTimeMillis() + TIMEOUT;
    }

    /**
     * Constructs a new {@code LtaSession}. This method is used during parsing of persisted sessions.
     *
     * @param cache            the data cache
     * @param theSiteProfile   the site profile
     * @param theSessionId     the session ID
     * @param theStudentId     the student ID
     * @param theExamId        the assignment ID being worked on
     * @param theRedirectOnEnd the URL to which to redirect at the end of the assignment
     * @param theState         the session state
     * @param theMinMastery    the minimum mastery score
     * @param theTimeout       the timeout
     * @param theStarted       true if exam has been started
     * @param theScore         the score achieved
     * @param theError         the grading error
     * @param theHomework      the homework
     * @throws SQLException if there is an error accessing the database
     */
    LtaSession(final Cache cache, final WebSiteProfile theSiteProfile, final String theSessionId,
               final String theStudentId, final String theExamId, final String theRedirectOnEnd,
               final ELtaState theState, final int theSection, final int theItem, final Integer theMinMastery,
               final long theTimeout, final boolean theStarted, final Integer theScore, final String theError,
               final ExamObj theHomework)
            throws SQLException {

        super(cache, theSiteProfile, theSessionId, theStudentId, theExamId, theRedirectOnEnd);

        this.state = theState;
        this.currentSection = theSection;
        this.currentItem = theItem;
        this.minMastery = theMinMastery;
        this.timeout = theTimeout;
        this.started = theStarted;
        this.score = theScore;
        this.gradingError = theError;

        setExam(theHomework);
    }

    /**
     * Gets the learning target assignment state.
     *
     * @return the learning target assignment state
     */
    public ELtaState getState() {

        return this.state;
    }

    /**
     * Gets the current section number.
     *
     * @return the current item number
     */
    public int getCurrentSection() {

        return this.currentSection;
    }

    /**
     * Gets the current section number.
     *
     * @return the current item number
     */
    public int getCurrentItem() {

        return this.currentItem;
    }

    /**
     * Gets the time remaining before the assignment times out.
     *
     * @return the time remaining, in milliseconds (0 if the exam has not been started)
     */
    public long getTimeRemaining() {

        return this.timeout == 0L ? 0L : this.timeout - System.currentTimeMillis();
    }

    /**
     * Tests whether this session is timed out.
     *
     * @return {@code true} if timed out
     */
    public boolean isTimedOut() {

        return System.currentTimeMillis() >= this.timeout;
    }

    /**
     * Tests whether this assignment has been started.
     *
     * @return {@code true} if started
     */
    public boolean isStarted() {

        return this.started;
    }

    /**
     * Generates HTML for the assignment based on its current state.
     *
     * @param cache the data cache
     * @param now   the date/time to consider "now"
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    public void generateHtml(final Cache cache, final ZonedDateTime now, final HtmlBuilder htm)
            throws SQLException {

        this.timeout = System.currentTimeMillis() + TIMEOUT;

        switch (this.state) {
            case INITIAL:
                doInitial(cache, now, htm);
                break;

            case INSTRUCTIONS:
                appendInstructionsHtml(htm);
                break;

            case ITEM_NN:
                appendAssignmentHtml(htm);
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
                appendLtaFooter(htm, "close", "Close", null, null, null, null);
                htm.eDiv(); // outer DIV from header
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

        final LtaEligibilityTester ltaTest = new LtaEligibilityTester(this.studentId);
        final AssignmentRec avail = cache.getSystemData().getActiveAssignment(this.version);

        final HtmlBuilder reasons = new HtmlBuilder(100);
        final Collection<RawAdminHold> holds = new ArrayList<>(1);

        String error = null;
        if (avail == null) {
            error = "Learning target assignment " + this.version + " not found.";
        } else if (ltaTest.isLtaEligible(cache, now, avail.courseId, avail.unit, avail.objective, reasons, holds)) {

            final Long serial = Long.valueOf(AbstractHandlerBase.generateSerialNumber(false));
            this.minMastery = ltaTest.getMinMasteryScore();
            final ExamObj theExamObj = InstructionalCache.getExam(avail.treeRef);

            if (theExamObj == null) {
                Log.warning("Unable to load template for ", avail.treeRef);
                error = "Unable to load assignment";
            } else if (theExamObj.ref == null) {
                error = "Unable to load assignment template";
            } else {
                boolean alreadyPassed = false;
                final Set<Integer> alreadyCorrect = new HashSet<>(10);

                // See if the student has already passed this assignment.  If they have, they get the complete
                // assignment as practice.  If not, they get only the questions they have not yet answered correctly.

                final List<RawSthomework> allHw = RawSthomeworkLogic.queryByStudentCourseUnit(cache, this.studentId,
                        avail.courseId, avail.unit, false);
                for (final RawSthomework hw : allHw) {
                    if (hw.version.equals(avail.assignmentId) && "Y".equals(hw.passed)) {
                        alreadyPassed = true;
                        break;
                    }
                }

                if (!alreadyPassed) {
                    // Gather a list of item numbers that the student has already gotten correct
                    final List<RawSthwqa> allQa = RawSthwqaLogic.queryByStudent(cache, this.studentId);
                    for (final RawSthwqa qa : allQa) {
                        if (qa.version.equals(avail.assignmentId) && "Y".equals(qa.ansCorrect)) {
                            alreadyCorrect.add(qa.questionNbr);
                        }
                    }
                }

                final int numProblems = theExamObj.getNumProblems();
                if (Integer.valueOf(-1).equals(this.minMastery)) {
                    this.minMastery = Integer.valueOf(numProblems);
                }
//                Log.info("Learning target assignment has " + numProblems + " problems, minMastery=" + this
//                .minMastery);

                theExamObj.realizationTime = System.currentTimeMillis();
                theExamObj.serialNumber = serial;

                final int count = theExamObj.getNumSections();
                if (count > 0) {
                    final ExamSection sect = theExamObj.getSection(0);
                    sect.enabled = true;

                    // Replace problems the student already has correct with "auto-correct" items
                    if (!alreadyCorrect.isEmpty()) {
                        final int numProb = sect.getNumProblems();
                        for (int i = 0; i < numProb; ++i) {
                            final ExamProblem prob = sect.getProblem(i);

                            if (alreadyCorrect.contains(prob.problemId)) {
                                prob.clearProblems();
                                prob.addProblem(new ProblemAutoCorrectTemplate(1));
                            }
                        }
                    }

                    final EvalContext evalContext = theExamObj.getEvalContext();

                    final ExamSection examSect = theExamObj.getSection(0);
                    for (int attempt = 1; attempt <= 5; attempt++) {
                        if (examSect.realize(evalContext)) {
                            examSect.passed = false;
                            examSect.mastered = false;
                            examSect.score = Long.valueOf(0L);
                            break;
                        }
                    }

                    theExamObj.getSection(0).enabled = true;
                    this.currentItem = 0;
                    theExamObj.presentationTime = System.currentTimeMillis();
                    setExam(theExamObj);

                    // Write the record of the exam...

                    final TermRec term = cache.getSystemData().getActiveTerm();
                    final GetReviewExamReply reply = new GetReviewExamReply();
                    reply.masteryScore = this.minMastery;
                    reply.status = GetExamReply.SUCCESS;
                    reply.presentedExam = theExamObj;
                    reply.studentId = this.studentId;

                    if (new ExamWriter().writePresentedExam(this.studentId, term, reply.presentedExam, reply.toXml())) {
                        if (theExamObj.instructions == null) {
                            this.state = ELtaState.ITEM_NN;
                            appendAssignmentHtml(htm);
                        } else {
                            this.state = ELtaState.INSTRUCTIONS;
                            appendInstructionsHtml(htm);
                        }
                    } else {
                        error = "Unable to write presented exam";
                    }
                } else {
                    error = "Learning target assignment has no sections";
                }
            }
        } else if (reasons.length() == 0) {
            error = "Not eligible for learning target assignment.";
        } else {
            error = "Not eligible for learning target assignment: " + reasons;
        }

        if (error != null) {
            htm.add(error);
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

        final ExamObj exam = getExam();

        if (exam.instructionsHtml == null && exam.instructions != null) {
            ExamObjConverter.populateExamHtml(exam, new int[]{1});
        }

        if (exam.instructionsHtml != null) {
            htm.addln(exam.instructionsHtml);
        }

        endMain(htm);

        if (this.started) {
            appendLtaFooter(htm, "score", "I am finished.  Submit the assignment for grading.", null, null, "nav_0",
                    "Go to question 1");
        } else {
            appendLtaFooter(htm, "nav_0", "Begin the assignment...", null, null, null, null);
        }
        htm.eDiv(); // outer DIV from header
    }

    /**
     * Appends the HTML for the assignment, showing the current item.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private void appendAssignmentHtml(final HtmlBuilder htm) {

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
        appendLtaFooter(htm, "score", "I am finished.  Submit the assignment for grading.",
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

        htm.addln("Do you wish to submit the assignment for grading?<br><br>");
        htm.add("  <input class='smallbtn' type='submit' name='Y' value='Yes - Submit the assignment'/> &nbsp;");
        htm.add("  <input class='smallbtn' type='submit' name='N' value='No - Return to the assignment'/>");

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
            if (this.minMastery != null) {
                if (this.score.intValue() >= this.minMastery.intValue()) {
                    htm.addln("This is a passing score.");
                } else {
                    htm.addln("This is not a passing score.");
                }
            }
        }

        htm.eSpan().eDiv();

        endMain(htm);
        appendLtaFooter(htm, "solutions", "View the assignment solutions.", null, null, null, null);
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

        final ExamObj exam = getExam();
        final ExamSection sect = exam.getSection(0);
        if (this.currentItem == -1) {
            if (exam.instructionsHtml != null) {
                htm.addln(exam.instructionsHtml);
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
        appendLtaFooter(htm, "close", "Close",
                prevCmd, "Review Question " + (this.currentItem),
                nextCmd, "Review Question " + (this.currentItem + 2));
        htm.eDiv(); // outer DIV from header
    }

    /**
     * Appends the header.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private void appendHeader(final HtmlBuilder htm) {

        htm.sDiv(null, "style='display:flex; flex-flow:row wrap; margin:0 6px 8px 6px;'");

        htm.sDiv(null, "style='flex: 1 100%; display:inline-block; background-color:", HEADER_BG_COLOR,
                "; border:1px solid ", OUTLINE_COLOR, "; margin:1px;'");

        htm.add("<h1 style='text-align:center; font-family:sans-serif; font-size:18pt; ",
                "font-weight:bold; color:#36648b; text-shadow:2px 1px #ccc; margin:0; padding:4pt;'>");

        final ExamObj exam = getExam();
        if (this.state == ELtaState.SOLUTION_NN) {
            htm.add(exam.examName + " Solutions");
        } else {
            htm.add(exam.examName);
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

        htm.addln("<main style='flex:1 1 73%; flex-grow: 4; display:block; width:75%; margin:1px; padding:2px; ",
                "border:1px solid ", OUTLINE_COLOR, ";'>");

        htm.addln(" <input type='hidden' name='currentItem' value='", Integer.toString(this.currentItem), "'>");

        htm.sDiv(null, "style='padding:8px; min-height:100%; background:", MAIN_BG_COLOR,
                "; font-family:serif; font-size:" + AbstractDocObjectTemplate.DEFAULT_BASE_FONT_SIZE + "px;'");
    }

    /**
     * Ends the "main" section.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void endMain(final HtmlBuilder htm) {

        htm.eDiv().addln("</main>");

        // Put this here so clicks can't call this until the page is loaded (and presumably item
        // answers have been installed in fields so this submit won't lose answers)

        htm.addln("<script>");
        htm.addln("function invokeAct(action) {");
        htm.addln("  if (actionAllowed == \"fine\") {");
        htm.addln("    document.getElementById(\"lta_act\").value = action;");
        htm.addln("    document.getElementById(\"lta_form\").submit();");
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

        htm.addln("<nav style='flex:1 1 140px; display:block; width:25%; background-color:white; ",
                "border:1px solid ", OUTLINE_COLOR, "; margin:1px; padding:6pt; font-size:14pt;'>");

        if ((this.state == ELtaState.INSTRUCTIONS) || (this.state == ELtaState.SOLUTION_NN && this.currentItem == -1)) {
            htm.sDiv(null, "style='background:#7FFF7F;'");
        } else {
            htm.sDiv();
        }

        final ExamObj exam = getExam();

        if (exam.instructions != null) {
            htm.add("<a style='font-family:serif;'");
            if (!disabled) {
                htm.addln(" href='javascript:invokeAct(\"instruct\");'");
            }
            htm.addln(">Instructions</a>");
            htm.eDiv();
        }

        final ExamSection sect = exam.getSection(0);
        final int numProblems = sect.getNumProblems();

        for (int p = 0; p < numProblems; ++p) {
            final ExamProblem ep = sect.getPresentedProblem(p);

            if (this.currentItem == p && (this.state == ELtaState.ITEM_NN || this.state == ELtaState.SOLUTION_NN)) {
                htm.sDiv(null, "style='background:#7FFF7F;'");
            } else {
                htm.sDiv();
            }

            if (this.state == ELtaState.ITEM_NN || this.state == ELtaState.INSTRUCTIONS
                    || this.state == ELtaState.SUBMIT_NN || this.state == ELtaState.COMPLETED) {
                // When interacting or instructions, mark the ones that have been answered

                if (ep.getSelectedProblem() instanceof ProblemAutoCorrectTemplate) {
                    htm.add("<img src='images/check.png'> ");
                } else {
                    htm.add("<input type='checkbox' onclick='return false;' ",
                            "style='height:17px; width:17px; position:relative; top:2px;'");
                    if (ep.getSelectedProblem().isAnswered()) {
                        htm.add(" checked");
                    }
                    htm.add("> ");
                }
            } else if (this.state == ELtaState.SOLUTION_NN) {
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
            htm.addln("> Question ", Integer.valueOf(p + 1), "</a>");
            htm.eDiv();
        }

        htm.addln("</nav>");
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
    private static void appendLtaFooter(final HtmlBuilder htm, final String command,
                                        final String label, final String prevCmd, final String prevLabel,
                                        final String nextCmd,
                                        final String nextLabel) {

        htm.sDiv(null, "style='flex: 1 100%; order:99; background-color:", HEADER_BG_COLOR,
                "; display:block; border:1px solid ", OUTLINE_COLOR,
                "; margin:1px; padding:0 12px; text-align:center;'");

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
     * Appends an empty footer.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void appendEmptyFooter(final HtmlBuilder htm) {

        htm.sDiv(null, "style='flex: 1 100%; display:block; background-color:", HEADER_BG_COLOR,
                "; border:1px solid ", OUTLINE_COLOR, "; margin:1px; padding:6pt; text-align:center;'");

        htm.eDiv();

        // End grid div
        htm.eDiv();
    }

    /**
     * Called when a POST is received on the page hosting the homework.
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

        // The following synchronizes on the store's map - this CANNOT be done while in a block
        // synchronized on a session since doing so risks a race/deadlock.

        if (redirect != null) {
            LtaSessionStore.getInstance().removeLtaSession(session.loginSessionId, this.version);
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
            this.state = ELtaState.ITEM_NN;
            this.started = true;
            appendExamLog("Starting learning target assignment exam");
        } else if (req.getParameter("score") != null) {
            appendExamLog("'score' action received - confirming submit.");
            this.state = ELtaState.SUBMIT_NN;
        } else {
            final String act = req.getParameter("action");

            // Navigation ...
            final ExamSection sect = getExam().getSection(0);
            final int numProblems = sect.getNumProblems();
            for (int i = 0; i < numProblems; ++i) {
                if (("nav_" + i).equals(act)) {
                    this.currentItem = i;
                    this.state = ELtaState.ITEM_NN;
                    this.started = true;
                    appendExamLog("Starting learning target assignment");
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
            this.state = ELtaState.SUBMIT_NN;
        } else {
            final String act = req.getParameter("action");

            if ("instruct".equals(act)) {
                this.state = ELtaState.INSTRUCTIONS;
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
            this.state = ELtaState.ITEM_NN;
        } else if (req.getParameter("Y") != null) {
            appendExamLog("Submit confirmed, scoring...");
            this.gradingError = scoreAndRecordCompletion(cache, now);
            this.state = ELtaState.COMPLETED;
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
            this.state = ELtaState.SOLUTION_NN;
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

//        Log.info("Scoring learning target assignment");

        final String stuId = this.studentId;

        if ("GUEST".equals(stuId) || "AACTUTOR".equals(stuId)) {
            error = "Guest login assignments will not be recorded.";
        } else if (stuId.startsWith("99")) {
            error = "Test student assignments will not be recorded.";
        } else {
            Log.info("Writing updated learning target assignment state");

            final Object[][] answers = getExam().exportState();
            loadStudentInfo(cache);

            // Write the updated exam state out somewhere permanent
            new ExamWriter().writeUpdatedExam(stuId, this.active, answers, false);

            error = finalizeAssignment(cache, now, answers);
        }

        if (error != null) {
            Log.warning(error);
        }

        return error;
    }

    /**
     * Finalize the assignment record on the server.
     *
     * @param cache   the data cache
     * @param now     the date/time to consider now
     * @param answers the submitted answers
     * @return {@code null} on success; an error message on any failure
     * @throws SQLException if there is an error accessing the database
     */
    private String finalizeAssignment(final Cache cache, final ChronoZonedDateTime<LocalDate> now,
                                      final Object[][] answers) throws SQLException {

        final ExamObj exam = getExam();
        final String crsId = exam.course;

        // Store the presentation and completion times in the exam object
        if (answers[0].length == 4) {
            // If exam has both presentation and completion time, compute the duration as seen by
            // the client, then adjust for the server's clock
            if (answers[0][2] != null && answers[0][3] != null) {
                final long duration = ((Long) answers[0][3]).longValue() - ((Long) answers[0][2]).longValue();

                if (duration >= 0L && duration < 43200L) {
                    exam.presentationTime = System.currentTimeMillis() - duration;
                } else {
                    // Time was not reasonable, so set to 0 time.
                    Log.warning("Client gave assignment duration as " + duration);
                    exam.presentationTime = System.currentTimeMillis();
                }
            } else {
                exam.presentationTime = exam.realizationTime;
            }

            // Set the completion time
            exam.finalizeExam();
        } else {
            Log.warning("Answers[0] was not length 4: ", Arrays.toString(answers[0]));
        }

        // See if the exam has already been inserted
        final Long ser = exam.serialNumber;
        final LocalDateTime start = TemporalUtils.toLocalDateTime(exam.realizationTime);
        final LocalDateTime finish = TemporalUtils.toLocalDateTime(exam.completionTime);

        final List<RawSthomework> existing = RawSthomeworkLogic.getHomeworks(cache, this.studentId, crsId, false, "ST");
        for (final RawSthomework test : existing) {
            if (test.serialNbr.equals(ser)) {
                return "This assignment has already been recorded.";
            }
        }

        final EvalContext params = new EvalContext();

        final VariableBoolean param1 = new VariableBoolean("proctored");
        param1.setValue(Boolean.FALSE);
        params.addVariable(param1);

        Log.info("Grading learning target assignment ", exam.examVersion, " for student ", this.studentId);

        final AssignmentRec assignmentRec = cache.getSystemData().getActiveAssignment(this.version);
        if (assignmentRec == null) {
            return "Learning target assignment " + this.version + " not found!";
        }

        // Begin preparing the database object to store exam results
        final RawSthomework sthw = new RawSthomework();
        sthw.serialNbr = exam.serialNumber;
        sthw.version = exam.examVersion;
        sthw.stuId = this.studentId;
        sthw.hwDt = finish.toLocalDate();
        sthw.startTime = Integer.valueOf(TemporalUtils.minuteOfDay(start.toLocalTime()));
        sthw.finishTime = Integer.valueOf(TemporalUtils.minuteOfDay(finish.toLocalTime()));
        sthw.timeOk = "Y";
        sthw.hwType = assignmentRec.assignmentType;
        sthw.course = exam.course;
        sthw.unit = assignmentRec.unit;
        sthw.objective = assignmentRec.objective;
        sthw.hwCoupon = "N";

        final RawStcourse stcourse = RawStcourseLogic.getRegistration(cache, this.studentId, exam.course);
        if (stcourse == null) {
            if (RawSpecialStusLogic.isSpecialType(cache, this.studentId, now.toLocalDate(), "TUTOR", "M384", "ADMIN")) {
                sthw.sect = "001";
            } else {
                return SimpleBuilder.concat("Unable to look up course registration for ", this.studentId, " in ",
                        exam.course);
            }
        } else {
            sthw.sect = stcourse.sect;
        }

        // Score items
        final int totalScore = buildAnswerList(answers);
        final ExamSection sect0 = getExam().getSection(0);
        if (sect0 != null) {
            sect0.score = Long.valueOf(totalScore);
        }

        sthw.hwScore = Integer.valueOf(totalScore);
        if (this.minMastery == null || totalScore >= this.minMastery.intValue()) {
            sthw.passed = "Y";
        } else {
            sthw.passed = "N";
        }

        final String error = recordQuestionAnswers(cache, assignmentRec, exam.serialNumber);

        if (error == null) {
            RawSthomeworkLogic.insert(cache, sthw);
        }

        return error;
    }

    /**
     * Populates the selected problem with submitted answers.
     *
     * @param answers the list of the student's answers
     */
    private int buildAnswerList(final Object[][] answers) {

        int totalScore = 0;

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

                        final boolean correct = selected.isCorrect(answers[id]);

                        if (correct) {
                            selected.score = 1.0;
                            ++totalScore;
                        } else {
                            selected.score = 0.0;
                        }
                    }
                }
            }
        }

        return totalScore;
    }

    /**
     * Write the series of STHWQA records to the database to record the student's answers on the homework assignment.
     *
     * @param cache        the data cache
     * @param hw           the homework assignment being submitted
     * @param serialNumber the serial number of the homework submission
     * @return {@code null} if the method succeeded; an error message otherwise
     * @throws SQLException if there is an error accessing the database
     */
    private String recordQuestionAnswers(final Cache cache, final AssignmentRec hw,
                                         final Long serialNumber) throws SQLException {

        final Object[][] answers = getExam().exportState();

        // answers[0] is time stamps, so we start at 1
        String error = null;
        final int numAns = answers.length;
        for (int i = 1; i < numAns; ++i) {

            final ExamProblem prob = getExam().getProblem(i);
            if (prob == null) {
                continue;
            }

            final AbstractProblemTemplate selected = prob.getSelectedProblem();
            if (selected == null) {
                continue;
            }

            // construct the answer string
            final char[] ans = "     ".toCharArray();

            if (answers[i] != null) {

                final int rowLen = answers[i].length;
                for (int j = 0; j < rowLen; ++j) {

                    if (answers[i][j] instanceof Long) {
                        final int index = ((Long) answers[i][j]).intValue();

                        if (index >= 1 && index <= 5) {
                            ans[index - 1] = (char) ('A' + index - 1);
                        }
                    }
                }
            }

            final String obj = hw.objective == null ? null : hw.objective.toString();
            final LocalDateTime fin = TemporalUtils.toLocalDateTime(getExam().completionTime);
            final int finTime = TemporalUtils.minuteOfDay(fin);

            final boolean isCorrect = selected.isCorrect(answers[i]);
            selected.score = isCorrect ? 1.0 : 0.0;

            final RawSthwqa sthwqa = new RawSthwqa(serialNumber, Integer.valueOf(i), Integer.valueOf(1), obj,
                    new String(ans), this.studentId, hw.assignmentId, isCorrect ? "Y" : "N", fin.toLocalDate(),
                    Integer.valueOf(finTime));

            if (!RawSthwqaLogic.insert(cache, sthwqa)) {
                error = "There was an error recording the assignment score.";
                break;
            }
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
            final WebSiteProfile siteProfile = getSiteProfile();

            xml.addln("<lta-session>");
            xml.addln(" <host>", siteProfile.host, "</host>");
            xml.addln(" <path>", siteProfile.path, "</path>");
            xml.addln(" <session>", this.sessionId, "</session>");
            xml.addln(" <student>", this.studentId, "</student>");
            xml.addln(" <assign-id>", this.version, "</assign-id>");
            xml.addln(" <state>", this.state.name(), "</state>");
            xml.addln(" <cur-sect>", Integer.toString(this.currentSection), "</cur-sect>");
            xml.addln(" <cur-item>", Integer.toString(this.currentItem), "</cur-item>");
            if (this.minMastery != null) {
                xml.addln(" <mastery>", this.minMastery, "</mastery>");
            }
            xml.addln(" <redirect>", XmlEscaper.escape(this.redirectOnEnd), "</redirect>");
            xml.addln(" <timeout>", Long.toString(this.timeout), "</timeout>");
            if (this.started) {
                xml.addln(" <started/>");
            }
            if (this.score != null) {
                xml.addln(" <score>", this.score, "</score>");
            }
            if (this.gradingError != null) {
                xml.addln(" <error>", this.gradingError, "</error>");
            }
            getExam().appendXml(xml, 1);

            // Homeworks can be re-generated, so include ALL problems
            final int numSect = getExam().getNumSections();
            for (int i = 0; i < numSect; ++i) {
                final ExamSection sect = getExam().getSection(i);
                final int numProb = sect.getNumProblems();
                for (int j = 0; j < numProb; ++j) {
                    final ExamProblem prob = sect.getProblem(j);
                    if (prob == null) {
                        Log.warning("NO possible ExamProblem for section " + i + " problem " + j);
                    } else {
                        xml.addln(" <problems sect='", Integer.toString(i), "' prob='", Integer.toString(j), "'>");
                        final int numP = prob.getNumProblems();

                        for (int k = 0; k < numP; ++k) {
                            final AbstractProblemTemplate p = prob.getProblem(k);
                            if (p == null) {
                                Log.warning("Missing possible problem " + k);
                            } else {
                                p.appendXml(xml, 2);
                            }
                        }
                        xml.addln(" </problems>");
                    }
                }
            }

            for (int i = 0; i < numSect; ++i) {
                final String iStr = Integer.toString(i);
                final ExamSection sect = getExam().getSection(i);
                final int numProb = sect.getNumProblems();

                for (int j = 0; j < numProb; ++j) {
                    final String jStr = Integer.toString(j);
                    final ExamProblem prob = sect.getProblem(j);

                    if (prob == null) {
                        Log.warning("No selected ExamProblem for section ", iStr, " problem ", jStr);
                    } else {
                        final AbstractProblemTemplate selected = prob.getSelectedProblem();
                        if (selected == null) {
                            Log.warning("No selected AbstractProblem for section ", iStr, " problem ", jStr);
                        } else {
                            xml.addln(" <selected-problem sect='", iStr, "' prob='", jStr, "'>");
                            selected.appendXml(xml, 2);
                            xml.addln(" </selected-problem>");
                        }
                    }
                }
            }
            xml.addln("</lta-session>");
        }
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

            LtaSessionStore.getInstance().removeLtaSession(this.sessionId, this.version);
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
                this.state = ELtaState.COMPLETED;

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
                this.state = ELtaState.COMPLETED;

                if (getExam() != null) {
                    setExam(null);
                }
            }

            // The following synchronizes on the store's map - this CANNOT be done while in a block
            // synchronized on a session since doing so risks a race/deadlock.

            LtaSessionStore.getInstance().removeLtaSession(this.sessionId, this.version);
        } else {
            appendExamLog("Forced submit requested, but requester is not ADMINISTRATOR");
        }
    }
}
