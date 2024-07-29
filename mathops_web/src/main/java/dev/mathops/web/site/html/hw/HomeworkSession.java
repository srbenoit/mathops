package dev.mathops.web.site.html.hw;

import dev.mathops.assessment.InstructionalCache;
import dev.mathops.assessment.document.template.AbstractDocObjectTemplate;
import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.assessment.exam.ExamProblem;
import dev.mathops.assessment.exam.ExamSection;
import dev.mathops.assessment.htmlgen.ProblemConverter;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.xml.XmlEscaper;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.Cache;
import dev.mathops.db.old.cfg.WebSiteProfile;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawSthomeworkLogic;
import dev.mathops.db.old.rawlogic.RawSthwqaLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.old.rawrecord.RawSthwqa;
import dev.mathops.db.old.rec.AssignmentRec;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.servlet.HomeworkEligibilityTester;
import dev.mathops.session.txn.handlers.AbstractHandlerBase;
import dev.mathops.web.site.html.HtmlSessionBase;

import jakarta.servlet.ServletRequest;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A user session used to take homework assignments online. It takes as arguments a session ID, student name, and
 * assignment ID and presents the homework assignment to the student. When the assignment is complete, the student
 * submits it for grading, after which the student is shown a summary page of the results.
 */
public final class HomeworkSession extends HtmlSessionBase {

    /** The timeout duration (2 hours), in milliseconds. */
    private static final long TIMEOUT = (long) (2 * 60 * 60 * 1000);

    /** The state of the assignment. */
    private EHomeworkState state;

    /** The currently active section. */
    private int currentSection;

    /** Minimum move-on score for the homework. */
    private Integer minMoveOn;

    /** Minimum mastery score for the homework. */
    private Integer minMastery;

    /** Flag indicating assignment is practice. */
    public final boolean practice;

    /** Timestamp when exam will time out. */
    private long timeout;

    /** Number of incorrect answers in a row entered. */
    private int incorrect;

    /**
     * Constructs a new {@code HomeworkSession}. This is called when the user clicks a button to start an assignment. It
     * stores data but does not generate the HTML until the page is actually generated.
     *
     * @param cache            the data cache
     * @param theSiteProfile   the site profile
     * @param theSessionId     the session ID
     * @param theStudentId     the student ID
     * @param theExamId        the exam ID being worked on
     * @param isPractice       {@code true} if the assignment is practice (homework otherwise)
     * @param theRedirectOnEnd the URL to which to redirect at the end of the assignment
     * @throws SQLException if there is an error accessing the database
     */
    public HomeworkSession(final Cache cache, final WebSiteProfile theSiteProfile, final String theSessionId,
                           final String theStudentId, final String theExamId, final boolean isPractice,
                           final String theRedirectOnEnd) throws SQLException {

        super(cache, theSiteProfile, theSessionId, theStudentId, theExamId, theRedirectOnEnd);

        this.practice = isPractice;
        this.state = EHomeworkState.INITIAL;
        this.timeout = System.currentTimeMillis() + TIMEOUT;
        this.incorrect = 0;
    }

    /**
     * Constructs a new {@code HomeworkSession}. This is called when the user clicks a button to start an assignment. It
     * stores data but does not generate the HTML until the page is actually generated.
     *
     * @param cache            the data cache
     * @param theSiteProfile   the site profile
     * @param theSessionId     the session ID
     * @param theStudentId     the student ID
     * @param theExamId        the exam ID being worked on
     * @param isPractice       {@code true} if the assignment is practice (homework otherwise)
     * @param theRedirectOnEnd the URL to which to redirect at the end of the assignment
     * @param theState         the session state
     * @param theSect          the current section
     * @param theMinMoveOn     the minimum move-on score
     * @param theMinMastery    the minimum mastery score
     * @param theTimeout       the timeout
     * @param theIncorrect     the number of incorrect responses in a row
     * @param theHomework      the homework
     * @throws SQLException if there is an error accessing the database
     */
    HomeworkSession(final Cache cache, final WebSiteProfile theSiteProfile,
                    final String theSessionId, final String theStudentId, final String theExamId,
                    final boolean isPractice, final String theRedirectOnEnd, final EHomeworkState theState,
                    final int theSect, final Integer theMinMoveOn, final Integer theMinMastery,
                    final long theTimeout, final int theIncorrect, final ExamObj theHomework)
            throws SQLException {

        super(cache, theSiteProfile, theSessionId, theStudentId, theExamId, theRedirectOnEnd);

        this.practice = isPractice;
        this.state = theState;
        this.currentSection = theSect;
        this.minMoveOn = theMinMoveOn;
        this.minMastery = theMinMastery;
        this.timeout = theTimeout;
        this.incorrect = theIncorrect;
        setExam(theHomework);
    }

    /**
     * Gets the homework state.
     *
     * @return the homework state
     */
    public EHomeworkState getState() {

        return this.state;
    }

//    /**
//     * Gets the current section.
//     *
//     * @return the current section
//     */
//    public int getCurrentSection() {
//
//        return this.currentSection;
//    }

    /**
     * Gets the time remaining in the exam.
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
     * Generates HTML for the homework based on its current state.
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

            case INTERACTING:
                appendHomeworkHtml(htm);
                break;

            case INCORRECT_MSG:
                appendIncorrectMsgHtml(htm, "next", "Try another problem...");
                break;

            case INCORRECT_SHOW_ANS:
                appendIncorrectMsgHtml(htm, "answer", "View answer...");
                break;

            case INCORRECT_SHOW_SOL:
                appendIncorrectMsgHtml(htm, "solution", "View solution...");
                break;

            case SHOW_ANSWER:
                appendHomeworkAnswerHtml(htm);
                break;

            case SHOW_SOLUTION:
                appendHomeworkSolutionHtml(htm);
                break;

            case CORRECT_NEXT:
                appendCorrectMsgHtml(htm, "next", "Go to the next problem...");
                break;

            case CORRECT_SUBMIT:
                appendCorrectMsgHtml(htm, "submit", "Submit the assignment...");
                break;

            case COMPLETED:
                appendHeader(htm);
                htm.addln("<div style='text-align:center; color:navy;'>");
                htm.addln("The assignment has been recorded.");
                htm.addln("</div>");
                appendFooter(htm, "close", "Close");
                break;

            default:
                appendHeader(htm);
                htm.addln("<div style='text-align:center; color:navy;'>");
                htm.addln("Unsupported state.");
                htm.addln("</div>");
                appendFooter(htm, "close", "Close");
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
    private void doInitial(final Cache cache, final ZonedDateTime now, final HtmlBuilder htm)
            throws SQLException {

        final HomeworkEligibilityTester hwtest = new HomeworkEligibilityTester(this.studentId);

        String error = null;

        final SystemData systemData = cache.getSystemData();

        final AssignmentRec avail = systemData.getActiveAssignment(this.version);

        final HtmlBuilder reasons = new HtmlBuilder(100);
        final Collection<RawAdminHold> holds = new ArrayList<>(1);

        if (avail == null) {
            error = "Homework " + this.version + " not found.";
        } else if (hwtest.isHomeworkEligible(cache, now, avail, reasons, holds, this.practice)) {

            final Long serial = Long.valueOf(AbstractHandlerBase.generateSerialNumber(this.practice));

            this.minMoveOn = hwtest.getMinMoveOnScore();
            this.minMastery = hwtest.getMinMasteryScore();

            final ExamObj theHomework = InstructionalCache.getExam(avail.treeRef);

            if (theHomework == null) {
                Log.warning("Unable to load template for ", avail.treeRef);
                error = "Unable to load assignment";
            } else if (theHomework.ref == null) {
                error = "Unable to load assignment template";
            } else {
                final int numProblems = theHomework.getNumProblems();

                if (Integer.valueOf(-1).equals(this.minMoveOn)) {
                    this.minMoveOn = Integer.valueOf(numProblems);
                }
                if (Integer.valueOf(-1).equals(this.minMastery)) {
                    this.minMastery = Integer.valueOf(numProblems);
                }
                Log.info("Homework assignment has " + numProblems + " problems, minMoveOn=" + this.minMoveOn
                        + ", minMastery=" + this.minMastery);

                theHomework.realizationTime = System.currentTimeMillis();
                theHomework.serialNumber = serial;

                final int count = theHomework.getNumSections();
                if (count > 0) {
                    theHomework.getSection(0).enabled = true;
                    final EvalContext evalContext = theHomework.getEvalContext();

                    final ExamSection sect = theHomework.getSection(0);
                    for (int attempt = 1; attempt <= 5; attempt++) {
                        if (sect.realize(evalContext)) {
                            sect.passed = false;
                            sect.mastered = false;
                            sect.score = Long.valueOf(0L);

                            break;
                        }
                    }
                } else {
                    error = "Homework assignment has no questions";
                }

                setExam(theHomework);
            }

            if (error == null) {
                String sect = "001";

                final List<RawCsection> csections = systemData.getCourseSections(this.active.term);
                csections.sort(null);
                for (final RawCsection test : csections) {
                    if (test.course.equals(avail.courseId)) {
                        sect = test.sect;
                        break;
                    }
                }

                if (!this.practice) {
                    final RawStcourse stcourse = RawStcourseLogic.getRegistration(cache, this.studentId,
                            avail.courseId);

                    if (stcourse == null) {
                        boolean isSpecial = false;

                        // 'TUTOR', 'ADMIN' special student types automatically in section "001" for
                        // 117, 118, 124, 125, 126.
                        if (RawRecordConstants.M117.equals(avail.courseId)
                                || RawRecordConstants.M118.equals(avail.courseId)
                                || RawRecordConstants.M124.equals(avail.courseId)
                                || RawRecordConstants.M125.equals(avail.courseId)
                                || RawRecordConstants.M126.equals(avail.courseId)
                                || RawRecordConstants.MATH117.equals(avail.courseId)
                                || RawRecordConstants.MATH118.equals(avail.courseId)
                                || RawRecordConstants.MATH124.equals(avail.courseId)
                                || RawRecordConstants.MATH125.equals(avail.courseId)
                                || RawRecordConstants.MATH126.equals(avail.courseId)) {

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

                        if (!isSpecial) {
                            error = "You are not registered in this course";
                        }
                    } else {
                        sect = stcourse.sect;
                    }
                }

                if (error == null) {
                    final LocalDateTime localNow = LocalDateTime.now();
                    final int startTime = TemporalUtils.minuteOfDay(localNow);

                    final RawSthomework sthw = new RawSthomework(serial, getExam().examVersion, this.studentId,
                            LocalDate.now(), Integer.valueOf(0), Integer.valueOf(startTime), Integer.valueOf(startTime),
                            "Y", "N", avail.assignmentType, avail.courseId, sect, avail.unit, avail.objective, "N",
                            null, null);

                    if (!RawSthomeworkLogic.INSTANCE.insert(cache, sthw)) {
                        error = "Failed to record start of assignment";
                    }
                }
            }
        } else if (reasons.length() == 0) {
            error = "Not eligible for homework.";
        } else {
            error = "Not eligible for homework: " + reasons;
        }

        if (error == null) {
            getExam().presentationTime = System.currentTimeMillis();
            this.currentSection = 0;
            this.state = EHomeworkState.INTERACTING;

            appendHomeworkHtml(htm);
        } else {
            htm.add(error);
        }
    }

    /**
     * Appends the HTML for the assignment.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private void appendHomeworkHtml(final HtmlBuilder htm) {

        appendHeader(htm);

        final ExamProblem ep = getExam().getSection(this.currentSection).getPresentedProblem(0);
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

        appendFooter(htm, "grade", "Submit this problem for grading.");
    }

    /**
     * Appends the HTML for the assignment after an incorrect response has been entered.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param command the submit button name (command)
     * @param label   the button label
     */
    private void appendIncorrectMsgHtml(final HtmlBuilder htm, final String command,
                                        final String label) {

        appendHeader(htm);

        final ExamProblem ep = getExam().getSection(this.currentSection).getPresentedProblem(0);
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
                htm.addln(p.insertAnswers(p.disabledHtml));
            }
        }

        htm.addln("<div style='text-align:center; color:FireBrick;'>");
        htm.addln("Your answer is not correct.");
        htm.addln("</div>");

        // TODO: Add the Live Help request button here if available.

        appendFooter(htm, command, label);
    }

    /**
     * Appends the HTML for the assignment after a correct response has been entered.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param command the submit button name (command)
     * @param label   the button label
     */
    private void appendCorrectMsgHtml(final HtmlBuilder htm, final String command,
                                      final String label) {

        appendHeader(htm);

        final ExamProblem ep = getExam().getSection(this.currentSection).getPresentedProblem(0);
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
                htm.addln(p.insertAnswers(p.disabledHtml));
            }
        }

        htm.addln("<div style='text-align:center; color:green;'>");
        htm.addln("Your answer is correct.");
        htm.addln("</div>");

        appendFooter(htm, command, label);
    }

    /**
     * Appends the HTML for the assignment with answer shown.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private void appendHomeworkAnswerHtml(final HtmlBuilder htm) {

        appendHeader(htm);

        final ExamProblem ep = getExam().getSection(this.currentSection).getPresentedProblem(0);
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

                htm.addln(p.insertAnswers(p.answerHtml));
            }
        }

        appendFooter(htm, "try-again", "Try another problem...");
    }

    /**
     * Appends the HTML for the assignment with solution shown.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private void appendHomeworkSolutionHtml(final HtmlBuilder htm) {

        appendHeader(htm);

        final ExamProblem ep = getExam().getSection(this.currentSection).getPresentedProblem(0);
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

        appendFooter(htm, "try-again", "Try another problem...");
    }

    /**
     * Appends the header.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private void appendHeader(final HtmlBuilder htm) {

        final ExamObj exam = getExam();

        htm.addln("<div style='display:block; background-color:AliceBlue; border:1px solid SteelBlue; margin:1px;'>");

        htm.addln("<h1 style='text-align:center; font-family:sans-serif; font-size:18pt; font-weight:bold; ",
                "color:#36648b; text-shadow:2px 1px #ccc; margin:0; padding:6px 12px;'>", exam.examName, "</h1>");

        if (exam.getNumSections() > 1 && !this.practice) {
            htm.addln("<hr style='height:1px; border:0; background:#b3b3b3; margin:0 1%;'>");

            htm.addln("<nav>");
            final int numSect = exam.getNumSections();
            for (int i = 0; i < numSect; ++i) {
                final ExamSection sect = getExam().getSection(i);

                // Alice Blue = #f0f8ff
                // Navy = #000080
                // Dimmed = #787CBF
                // Highlight = #FFFF7F

                if (this.currentSection == i) {
                    htm.addln("<h2 style='font-family:sans-serif; font-size:11pt; color:#000080; font-weight:bold; ",
                            "background:#FFFF7F; margin:1pt; padding:0;'> &rtrif;", sect.sectionName, "</h2>");
                } else {
                    htm.addln("<h2 style='font-family:sans-serif; font-size:11pt; color:#787CBF; font-weight:bold; ",
                            "margin:1pt; padding:0;'> <span style='display:hidden;'>&rtrif;</span>", sect.sectionName,
                            "</h2>");
                }
            }
            htm.addln("</nav>");
        }
        htm.eDiv(); // header div

        htm.addln("<main style='margin:3px 1px; padding:2px; border:1px solid SteelBlue;'>");
        htm.addln("<div style='padding:6pt; border:1px solid #b3b3b3; background:#f8f8f8;font-family:serif; font-size:",
                Float.toString(AbstractDocObjectTemplate.DEFAULT_BASE_FONT_SIZE), "px;'>");
    }

    /**
     * Appends the footer.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param command the submit button name (command)
     * @param label   the button label
     */
    private static void appendFooter(final HtmlBuilder htm, final String command, final String label) {

        htm.eDiv();
        htm.addln("</main>");

        htm.addln("<div style='display:block; background-color:AliceBlue; ",
                "border:1px solid SteelBlue; margin:1px; padding:0; text-align:center;'>");
        htm.add("  <input class='smallbtn' type='submit' name='", command, "' value='", label, "'/>");
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
            case INTERACTING:
                processPostInteracting(cache, session, req, htm);
                break;

            case CORRECT_NEXT:
                processCorrectNext(cache, session, req, htm);
                break;

            case CORRECT_SUBMIT:
                processCorrectSubmit(cache, session, req, htm);
                break;

            case INCORRECT_MSG:
            case SHOW_ANSWER:
            case SHOW_SOLUTION:
                processPostIncorrectShowAnsShowSol(cache, session, req, htm);
                break;

            case INCORRECT_SHOW_ANS:
                processPostIncorrectShowAns(cache, session, req, htm);
                break;

            case INCORRECT_SHOW_SOL:
                processPostIncorrectShowSol(cache, session, req, htm);
                break;

            case COMPLETED:
                redirect = processPostCompleted(cache, session, req, htm);
                break;

            case INITIAL:
            default:
                generateHtml(cache, session.getNow(), htm);
                break;
        }

        // The following synchronizes on the store's map - this CANNOT be done while in a block
        // synchronized on a session since doing so risks a race/deadlock.

        if (redirect != null) {
            HomeworkSessionStore.getInstance().removeHomeworkSession(session.loginSessionId, this.version);
        }

        return redirect;
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
    private void processPostInteracting(final Cache cache, final ImmutableSessionInfo session, final ServletRequest req,
                                        final HtmlBuilder htm) throws SQLException {

        if (req.getParameter("grade") != null) {

            Log.info(" Grading assignment question");

            // Let the current problem process the POST and extract answers
            final ExamObj exam = getExam();
            final ExamSection sect = exam.getSection(this.currentSection);
            if (sect != null) {
                final Map<String, String[]> params = req.getParameterMap();
                sect.getPresentedProblem(0).getSelectedProblem().extractAnswers(params);

                boolean correct = gradeSection(this.currentSection);
                if (correct && this.practice) {
                    correct = sect.score != null && sect.score.longValue() > 0L;
                }
                Log.info(" Correct = " + correct);

                final int score;
                if (correct) {
                    this.incorrect = 0;
                    if (this.currentSection + 1 == exam.getNumSections()) {
                        this.state = this.practice ? EHomeworkState.CORRECT_NEXT : EHomeworkState.CORRECT_SUBMIT;
                    } else {
                        this.state = EHomeworkState.CORRECT_NEXT;
                    }
                    score = this.currentSection + 1;
                } else {
                    ++this.incorrect;
                    final boolean showSolutions;
                    if ("M 100R".equals(exam.course)) {
                        showSolutions = false;
                    } else {
                        showSolutions = this.practice
                                || RawRecordConstants.M100T.equals(exam.course)
                                || RawRecordConstants.M1170.equals(exam.course)
                                || RawRecordConstants.M1180.equals(exam.course)
                                || RawRecordConstants.M1240.equals(exam.course)
                                || RawRecordConstants.M1250.equals(exam.course)
                                || RawRecordConstants.M1260.equals(exam.course);
                    }
                    score = this.currentSection;

                    this.state = showSolutions ? EHomeworkState.INCORRECT_SHOW_SOL : EHomeworkState.INCORRECT_SHOW_ANS;
                }

                if (!this.practice) {
                    // Update the "sthomework" record
                    final Long ser = exam.serialNumber;
                    final String ver = exam.examVersion;
                    final String stu = session.getEffectiveUserId();

                    final LocalDateTime localNow = LocalDateTime.now();
                    final int finish = TemporalUtils.minuteOfDay(localNow);
                    RawSthomeworkLogic.updateFinishTimeScore(cache, ser, ver, stu, finish, score, "N");
                }
            }
        }

        generateHtml(cache, session.getNow(), htm);
    }

    /**
     * Called when a POST is received while in the CORRECT_NEXT state.
     *
     * @param cache   the data cache
     * @param session the user session
     * @param req     the servlet request
     * @param htm     the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private void processCorrectNext(final Cache cache, final ImmutableSessionInfo session,
                                    final ServletRequest req, final HtmlBuilder htm) throws SQLException {

        if (req.getParameter("next") != null) {
            if (!this.practice) {
                ++this.currentSection;
            }

            final ExamSection sect = getExam().getSection(this.currentSection);
            final EvalContext evalContext = getExam().getEvalContext();
            for (int attempt = 0; attempt < 5; ++attempt) {
                if (sect.realize(evalContext)) {
                    sect.passed = false;
                    sect.mastered = false;
                    sect.score = Long.valueOf(0L);
                    break;
                }
            }

            this.state = EHomeworkState.INTERACTING;
        }

        generateHtml(cache, session.getNow(), htm);
    }

    /**
     * Called when a POST is received while in the CORRECT_SUBMIT state.
     *
     * @param cache   the data cache
     * @param session the user session
     * @param req     the servlet request
     * @param htm     the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private void processCorrectSubmit(final Cache cache, final ImmutableSessionInfo session,
                                      final ServletRequest req, final HtmlBuilder htm) throws SQLException {

        if (req.getParameter("submit") != null) {
            recordCompletion(cache, session.getNow());
            this.state = EHomeworkState.COMPLETED;
        }

        generateHtml(cache, session.getNow(), htm);
    }

    /**
     * Called when a POST is received while in the INCORRECT, SHOW_ANSWER, or SHOW_SOLUTION state.
     *
     * @param cache   the data cache
     * @param session the user session
     * @param req     the servlet request
     * @param htm     the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private void processPostIncorrectShowAnsShowSol(final Cache cache, final ImmutableSessionInfo session,
                                                    final ServletRequest req, final HtmlBuilder htm)
            throws SQLException {

        if (req.getParameter("try-again") != null) {

            // Re-generate the current section
            final ExamSection sect = getExam().getSection(this.currentSection);
            final EvalContext evalContext = getExam().getEvalContext();
            for (int attempt = 0; attempt < 5; ++attempt) {
                if (sect.realize(evalContext)) {
                    sect.passed = false;
                    sect.mastered = false;
                    sect.score = Long.valueOf(0L);
                    break;
                }
            }

            this.state = EHomeworkState.INTERACTING;
        }

        generateHtml(cache, session.getNow(), htm);
    }

    /**
     * Called when a POST is received while in the INCORRECT_SHOW_ANS state.
     *
     * @param cache   the data cache
     * @param session the user session
     * @param req     the servlet request
     * @param htm     the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private void processPostIncorrectShowAns(final Cache cache, final ImmutableSessionInfo session,
                                             final ServletRequest req, final HtmlBuilder htm) throws SQLException {

        if (req.getParameter("answer") != null) {
            this.state = EHomeworkState.SHOW_ANSWER;
        }

        generateHtml(cache, session.getNow(), htm);
    }

    /**
     * Called when a POST is received while in the INCORRECT_SHOW_SOL state.
     *
     * @param cache   the dat cache
     * @param session the user session
     * @param req     the servlet request
     * @param htm     the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private void processPostIncorrectShowSol(final Cache cache, final ImmutableSessionInfo session,
                                             final ServletRequest req, final HtmlBuilder htm) throws SQLException {

        if (req.getParameter("solution") != null) {
            this.state = EHomeworkState.SHOW_SOLUTION;
        }

        generateHtml(cache, session.getNow(), htm);
    }

    /**
     * Called when a POST is received while in the COMPLETED state.
     *
     * @param cache   the data cache
     * @param session the user session
     * @param req     the servlet request
     * @param htm     the {@code HtmlBuilder} to which to append
     * @return the URL to which to redirect of assessment is finished; {@code null} if not
     * @throws SQLException if there is an error accessing the database
     */
    private String processPostCompleted(final Cache cache, final ImmutableSessionInfo session,
                                        final ServletRequest req, final HtmlBuilder htm) throws SQLException {

        String redirect = null;

        if (req.getParameter("close") != null) {
            setExam(null);
            redirect = this.redirectOnEnd;
        } else {
            generateHtml(cache, session.getNow(), htm);
        }

        return redirect;
    }

    /**
     * Grades the current section, and optionally move to a different section.
     *
     * @param section the section being worked
     * @return {@code true} if answer was correct
     */
    private boolean gradeSection(final int section) {

        final boolean correct;

        Log.info("Grading section ", Integer.toString(section));

        // Get the active section
        final ExamSection sect = getExam().getSection(section);
        if (sect == null) {
            Log.warning("Unable to obtain current section to grade");
            correct = false;
        } else {
            // Count the number of correct answers
            final int numProb = sect.getNumProblems();
            int score = 0;

            for (int i = 0; i < numProb; ++i) {
                final ExamProblem prob = sect.getPresentedProblem(i);
                final AbstractProblemTemplate selected = prob.getSelectedProblem();
                if (selected.isAnswered() && selected.isCorrect(selected.getAnswer())) {
                    ++score;
                }
            }

            // Store the score
            if ((sect.score == null) || (sect.score.intValue() < score)) {
                sect.score = Long.valueOf((long) score);
            }

            // Determine the per-section move-on score, by dividing the assignment
            final int actualMinMoveOn;
            if (this.minMoveOn == null) {
                actualMinMoveOn = 0;
            } else if (this.minMoveOn.intValue() == -1) {
                actualMinMoveOn = sect.getNumProblems();
            } else {
                actualMinMoveOn = this.minMoveOn.intValue() / getExam().getNumSections();
            }

            correct = score > 0 && score >= actualMinMoveOn;
            if (correct) {
                sect.passed = true;
            }

            // Do the same with mastery score
            final int actualMinMastery;
            if (this.minMastery == null) {
                actualMinMastery = 0;
            } else if (this.minMastery.intValue() == -1) {
                actualMinMastery = sect.getNumProblems();
            } else {
                actualMinMastery = this.minMastery.intValue() / getExam().getNumSections();
            }

            if (score >= actualMinMastery) {
                sect.mastered = true;
                sect.passed = true;
            }

            Log.info(" Score = " + score + ", min move-on = " + actualMinMoveOn + ", correct = " + correct +
                    ", min mastery = " + actualMinMastery + ", mastered = " + sect.mastered);
        }

        return correct;
    }

    /**
     * Records completion of a homework assignment.
     *
     * @param cache the data cache
     * @param now   the date/time to consider "now"
     * @return an error message on failure; {@code null} on success.
     * @throws SQLException if there is an error accessing the database
     */
    private String recordCompletion(final Cache cache, final ChronoZonedDateTime<LocalDate> now) throws SQLException {

        final String error;

        final String stuId = this.studentId;

        if ("GUEST".equals(stuId) || "AACTUTOR".equals(stuId)) {
            error = "Guest login homework will not be recorded.";
        } else if ("ETEXT".equals(stuId)) {
            error = "Practice homeworks will not be recorded.";
        } else if (stuId.startsWith("99")) {
            error = "Test student homework will not be recorded.";
        } else {
            final long timestamp = now.toInstant().toEpochMilli();
            getExam().completionTime = timestamp;

            long dur = timestamp - getExam().presentationTime;
            if (dur > 86400000L || dur < 0L) {
                dur = 0L;
                getExam().presentationTime = timestamp - dur;
            }

            error = finalizeHomework(cache, now);
        }

        return error;
    }

    /**
     * Finalize the homework record on the server, running all grading processing and applying result to the student's
     * record.
     *
     * @param cache the data cache
     * @param now   the date/time to consider "now"
     * @return {@code null} if finalization succeeded; an error message otherwise
     * @throws SQLException if there is an error accessing the database
     */
    private String finalizeHomework(final Cache cache, final ChronoZonedDateTime<LocalDate> now) throws SQLException {

        // From the homework version, look up the course, unit in the homework table, then use that to fetch the
        // course/unit/section data for the student. This gives us the minimum move-on and mastery scores.

        final SystemData systemData = cache.getSystemData();

        final TermRec activeTerm = systemData.getActiveTerm();
        if (activeTerm == null) {
            return "Unable to lookup active term to submit homework.";
        }

        final String ver = getExam().examVersion;
        final AssignmentRec hw = systemData.getActiveAssignment(ver);
        if (hw == null) {
            return "Assignment has been removed from the course!";
        }

        RawStcourse stcourse = RawStcourseLogic.getRegistration(cache, this.studentId, hw.courseId);

        if (stcourse == null) {
            boolean isSpecial = false;

            // 'TUTOR', 'ADMIN' special student types automatically in section "001" for
            // 117, 118, 124, 125, 126.
            if (RawRecordConstants.M117.equals(hw.courseId)
                    || RawRecordConstants.M118.equals(hw.courseId)
                    || RawRecordConstants.M124.equals(hw.courseId)
                    || RawRecordConstants.M125.equals(hw.courseId)
                    || RawRecordConstants.M126.equals(hw.courseId)
                    || RawRecordConstants.MATH117.equals(hw.courseId)
                    || RawRecordConstants.MATH118.equals(hw.courseId)
                    || RawRecordConstants.MATH124.equals(hw.courseId)
                    || RawRecordConstants.MATH125.equals(hw.courseId)
                    || RawRecordConstants.MATH126.equals(hw.courseId)) {

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
                final List<RawCsection> sections = systemData.getCourseSectionsByCourse(hw.courseId, activeTerm.term);

                if (sections.isEmpty()) {
                    return "No sections configured";
                } else {
                    // Create a fake STCOURSE record
                    sections.sort(null);
                    final RawCsection sect = sections.getFirst();

                    stcourse = new RawStcourse(activeTerm.term, // term
                            this.studentId, // stuId
                            hw.courseId, // course
                            sect.sect, // sect
                            null, // paceOrder
                            "Y", // openStatus
                            null, // gradingOption,
                            "N", // completed
                            null, // score
                            null, // courseGrade
                            "Y", // prereqSatis
                            "N", // initialClassRoll
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
                            null, // lastClassRollDate
                            null, // iTermKey
                            null); // iDeadlineDt

                    stcourse.synthetic = true;
                }
            } else {
                return "You are not registered in this course!";
            }
        }

        // Compute the score
        int score = 0;
        final int numSect = getExam().getNumSections();
        for (int i = 0; i < numSect; i++) {
            final Long value = getExam().getSection(i).score;
            if (value != null) {
                score += value.intValue();
            }
        }

        final String error;

        // Compare the scores to the minimum mastery and move-on scores
        if (this.minMastery == null || score >= this.minMastery.intValue()) {

            // Record a "passing" homework record
            error = recordMasteredHomework(cache, hw, score);
        } else if (this.minMoveOn == null || score >= this.minMoveOn.intValue()) {

            // Record a "not-passing" homework record
            error = recordNonMasteredHomework(cache, hw, stcourse, score);
        } else {
            // No entry in the database.
            error = "Your score was not sufficient to move on.";
        }

        return error;
    }

    /**
     * Record a passing (mastered) homework assignment in the database. This creates a new STHOMEWORK record. If this is
     * the first passed homework in this unit, its passed field in set to "Y". Otherwise, the passed field is set to
     * "2", "3", "4", etc.
     *
     * @param cache the data cache
     * @param hw    the homework assignment being submitted
     * @param score the score
     * @return {@code null} if the method succeeded; an error message otherwise
     * @throws SQLException if there is an error accessing the database
     */
    private String recordMasteredHomework(final Cache cache, final AssignmentRec hw,
                                          final int score) throws SQLException {

        // We must first find any existing PASSED homework for this course, unit and objective, and
        // determine what to set this new record's PASSED field to.

        final List<RawSthomework> exist = RawSthomeworkLogic.getHomeworks(cache, this.studentId, hw.courseId, hw.unit,
                true, hw.assignmentType);

        int max = 0;
        boolean searching = true;
        for (final RawSthomework rawSthomework : exist) {

            // Ignore if not for this objective
            if (!rawSthomework.objective.equals(hw.objective)) {
                continue;
            }

            int which = 0;
            if ("Y".equals(rawSthomework.passed)) {
                which = 1;
                searching = false;
            } else if (Character.isDigit(rawSthomework.passed.charAt(0))) {
                try {
                    which = Long.valueOf(rawSthomework.passed).intValue();
                } catch (final NumberFormatException ex) {
                    Log.warning("Failed to parse integer");
                }
            }

            if (which > max) {
                max = which;
            }

            // If we didn't find a "Y" row, reset max to 0, since we want to put this row in
            // as "Y", even if there are numeric rows.
            if (searching) {
                max = 0;
            }
        }

        final String passed;

        if (("LB".equals(hw.assignmentType)) || (max == 0)) {
            passed = "Y";
        } else {
            passed = Integer.toString(max + 1);
        }

        final ExamObj exam = getExam();
        final LocalDateTime end = TemporalUtils.toLocalDateTime(exam.completionTime);
        final int endTime = TemporalUtils.minuteOfDay(end);

        final String error;
        if (RawSthomeworkLogic.updateFinishTimeScore(cache, exam.serialNumber, exam.examVersion, this.studentId,
                endTime, score, passed)) {
            error = recordQuestionAnswers(cache, hw, exam.serialNumber);
        } else {
            error = "Failed to record assignment credit";
        }

        return error;
    }

    /**
     * Record a non-mastered homework assignment in the database. This creates a new STHOMEWORK record with the passed
     * field set to "N".
     *
     * @param cache    the data cache
     * @param hw       the homework assignment being submitted
     * @param stcourse the student course registration information
     * @param score    the score
     * @return {@code null} if the method succeeded; an error message otherwise
     * @throws SQLException if there is an error accessing the database
     */
    private String recordNonMasteredHomework(final Cache cache, final AssignmentRec hw,
                                             final RawStcourse stcourse, final int score) throws SQLException {

        final ExamObj exam = getExam();
        final LocalDateTime start = TemporalUtils.toLocalDateTime(exam.presentationTime);
        final LocalDateTime end = TemporalUtils.toLocalDateTime(exam.completionTime);

        final int startTime = TemporalUtils.minuteOfDay(start);
        final int endTime = TemporalUtils.minuteOfDay(end);

        final RawSthomework sthw = new RawSthomework(Long.valueOf(AbstractHandlerBase.generateSerialNumber(false)),
                exam.examVersion, this.studentId, end.toLocalDate(), Integer.valueOf(score), Integer.valueOf(startTime),
                Integer.valueOf(endTime), "Y", "N", hw.assignmentType, hw.courseId, stcourse.sect, hw.unit,
                hw.objective, "N", null, null);

        final String error;
        if (RawSthomeworkLogic.INSTANCE.insert(cache, sthw)) {
            error = recordQuestionAnswers(cache, hw, sthw.serialNbr);
        } else {
            error = "Failed to record assignment credit";
        }

        return error;
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

            final RawSthwqa sthwqa = new RawSthwqa(serialNumber, Integer.valueOf(i), Integer.valueOf(1), obj,
                    new String(ans), this.studentId, hw.assignmentId, selected.isCorrect(answers[i]) ? "Y" : "N",
                    fin.toLocalDate(), Integer.valueOf(finTime));

            if (!RawSthwqaLogic.INSTANCE.insert(cache, sthwqa)) {
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
            xml.addln("<homework-session>");
            xml.addln(" <host>", getSiteProfile().host, "</host>");
            xml.addln(" <path>", getSiteProfile().path, "</path>");
            xml.addln(" <session>", this.sessionId, "</session>");
            xml.addln(" <student>", this.studentId, "</student>");
            xml.addln(" <assign-id>", this.version, "</assign-id>");
            xml.addln(" <state>", this.state.name(), "</state>");
            xml.addln(" <cur-sect>", Integer.toString(this.currentSection),
                    "</cur-sect>");
            if (this.minMoveOn != null) {
                xml.addln(" <moveon>", this.minMoveOn, "</moveon>");
            }
            if (this.minMastery != null) {
                xml.addln(" <mastery>", this.minMastery, "</mastery>");
            }
            if (this.practice) {
                xml.addln(" <practice/>");
            }
            xml.addln(" <redirect>", XmlEscaper.escape(this.redirectOnEnd), "</redirect>");
            xml.addln(" <timeout>", Long.toString(this.timeout), "</timeout>");
            xml.addln(" <incorrect>", Integer.toString(this.incorrect), "</incorrect>");
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
            xml.addln("</homework-session>");
        }
    }

    /**
     * Performs a forced abort of a homework session.
     *
     * @param session the login session requesting the forced abort
     */
    public void forceAbort(final ImmutableSessionInfo session) {

        if (session.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
            appendExamLog("Forced abort requested");

            if (getExam() != null) {
                setExam(null);
            }

            final HomeworkSessionStore store = HomeworkSessionStore.getInstance();
            store.removeHomeworkSession(this.sessionId, this.version);
        } else {
            appendExamLog("Forced abort requested, but requester is not ADMINISTRATOR");
        }
    }
}
