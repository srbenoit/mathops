package dev.mathops.web.site.canvas.courses;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawCsectionLogic;
import dev.mathops.db.old.rawlogic.RawSthomeworkLogic;
import dev.mathops.db.old.rawlogic.RawSttermLogic;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.rec.AssignmentRec;
import dev.mathops.db.rec.MasteryAttemptRec;
import dev.mathops.db.rec.MasteryExamRec;
import dev.mathops.db.rec.StandardMilestoneRec;
import dev.mathops.db.rec.StudentStandardMilestoneRec;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.reclogic.AssignmentLogic;
import dev.mathops.db.reclogic.MasteryAttemptLogic;
import dev.mathops.db.reclogic.MasteryExamLogic;
import dev.mathops.db.reclogic.StandardMilestoneLogic;
import dev.mathops.db.reclogic.StudentStandardMilestoneLogic;
import dev.mathops.db.reclogic.TermLogic;
import dev.mathops.db.type.TermKey;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.canvas.CanvasPageUtils;
import dev.mathops.web.site.canvas.CanvasSite;
import dev.mathops.web.site.canvas.ECanvasPanel;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * This page shows the "Assignments" content.
 */
public enum PageAssignments {
    ;

    /**
     * Starts the page that shows the status of all assignments and grades.
     *
     * @param cache    the data cache
     * @param site     the owning site
     * @param courseId the course ID
     * @param req      the request
     * @param resp     the response
     * @param session  the user's login session information
     * @param metadata the metadata object with course structure data
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final CanvasSite site, final String courseId, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session,
                             final Metadata metadata) throws IOException,
            SQLException {

        final String stuId = session.getEffectiveUserId();
        final RawStcourse registration = CanvasPageUtils.confirmRegistration(cache, stuId, courseId);

        if (registration == null) {
            final String homePath = site.makeRootPath("home.html");
            resp.sendRedirect(homePath);
        } else {
            final MetadataCourse metaCourse = metadata.getCourse(registration.course);
            if (metaCourse == null) {
                // TODO: Error display, course not part of this system rather than a redirect to Home
                final String homePath = site.makeRootPath("home.htm");
                resp.sendRedirect(homePath);
            } else {
                final TermRec active = TermLogic.get(cache).queryActive(cache);
                final List<RawCsection> csections = RawCsectionLogic.queryByTerm(cache, active.term);

                RawCsection csection = null;
                for (final RawCsection test : csections) {
                    if (registration.course.equals(test.course) && registration.sect.equals(test.sect)) {
                        csection = test;
                        break;
                    }
                }

                if (csection == null) {
                    final String homePath = site.makeRootPath("home.html");
                    resp.sendRedirect(homePath);
                } else {
                    presentAssignments(cache, site, req, resp, session, registration, active.term, csection,
                            metaCourse);
                }
            }
        }
    }

    /**
     * Presents the "Assignments" information.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     * @param termKey      the term key
     * @param csection     the course section information
     * @param metaCourse   the metadata object with course structure data
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void presentAssignments(final Cache cache, final CanvasSite site, final ServletRequest req,
                                   final HttpServletResponse resp, final ImmutableSessionInfo session,
                                   final RawStcourse registration, final TermKey termKey, final RawCsection csection,
                                   final MetadataCourse metaCourse) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        final String siteTitle = site.getTitle();

        CanvasPageUtils.startPage(htm, siteTitle);

        // Emit the course number and section at the top
        CanvasPageUtils.emitCourseTitleAndSection(htm, metaCourse, csection);

        htm.sDiv("pagecontainer");

        CanvasPageUtils.emitLeftSideMenu(htm, metaCourse, null, ECanvasPanel.ASSIGNMENTS);

        htm.sDiv("flexmain");

        htm.sH(2).add("Assignments").eH(2);
        htm.sP().add("This page shows every assignment in the course, grouped by module.").eP();
        htm.hr();

        // Determine the student's pace and track
        final RawStterm stterm = RawSttermLogic.query(cache, termKey, registration.stuId);
        final Integer index = registration.paceOrder;

        if (stterm == null || index == null) {
            htm.sP().add("Error: Unable to determine your deadline schedule.  Please contact the Precalculus Center ",
                    "at <a class='ulink' href='mailto:precalc_math@colostate.edu'>precalc_math@colostate.edu</a> to ",
                    "report this error (please include your CSU ID number and the course number).");
        } else {
            // Load all milestones for the student's pace/track/index and any student overrides
            final List<StandardMilestoneRec> milestones = StandardMilestoneLogic.get(cache).queryByPaceTrackPaceIndex(
                    cache, stterm.paceTrack, stterm.pace, index);

            final List<StudentStandardMilestoneRec> stuMilestones = StudentStandardMilestoneLogic.get(
                    cache).queryByStuPaceTrackPaceIndex(cache, registration.stuId, stterm.paceTrack, stterm.pace,
                    index);

            // Load all the assignments associated with the course
            final List<AssignmentRec> assignments = AssignmentLogic.get(cache).queryActiveByCourse(cache,
                    registration.course, "ST");

            // Load all mastery exams associated with the course
            final List<MasteryExamRec> exams = MasteryExamLogic.get(cache).queryActiveByCourse(cache,
                    registration.course);

            // Load all the student submitted homework
            final List<RawSthomework> sthw = RawSthomeworkLogic.queryByStudentCourse(cache, registration.stuId,
                    registration.course, false);

            // Load all mastery exams associated with the course
            final List<MasteryAttemptRec> stexams = MasteryAttemptLogic.get(cache).queryByStudent(cache,
                    registration.stuId);

            final Integer o1 = Integer.valueOf(1);
            final Integer o2 = Integer.valueOf(2);
            final Integer o3 = Integer.valueOf(3);

            // Gather the status of all exams and homeworks

            final HomeworkStatus[][] hwStatus = new HomeworkStatus[9][4];
            final ExamStatus[][] examStatus = new ExamStatus[9][4];

            for (int module = 1; module <= 8; ++module) {
                final Integer m = Integer.valueOf(module);

                hwStatus[module][1] = findHomework(m, o1, assignments, sthw);
                hwStatus[module][2] = findHomework(m, o2, assignments, sthw);
                hwStatus[module][3] = findHomework(m, o3, assignments, sthw);

                examStatus[module][1] = findExam(m, o1, hwStatus[module][1], exams, stexams, milestones, stuMilestones);
                examStatus[module][2] = findExam(m, o2, hwStatus[module][2], exams, stexams, milestones, stuMilestones);
                examStatus[module][3] = findExam(m, o3, hwStatus[module][3], exams, stexams, milestones, stuMilestones);
            }

            // Show each module's homework assignments with their status

            for (int module = 1; module <= 8; ++module) {
                final Integer moduleObj = Integer.valueOf(module);

                startModule(htm, "Module " + moduleObj + " Homework Assignments");

                htm.sDiv("module-item");
                if (hwStatus[module][1] != null) {
                    emitHw(htm, hwStatus[module][1], moduleObj, o1);
                }
                if (hwStatus[module][2] != null) {
                    emitHw(htm, hwStatus[module][2], moduleObj, o2);
                }
                if (hwStatus[module][3] != null) {
                    emitHw(htm, hwStatus[module][3], moduleObj, o3);
                }
                htm.eDiv(); // module-item

                endModule(htm);
            }

            // Show the overall status of the course mastery exam.

            startModule(htm, "Course Mastery Exam");

            htm.sDiv("module-item");
            htm.sP().add("The Course Mastery Exam has six questions from each of the eight modules, or 48 questions ",
                    "total.").eP();

            htm.sP().add("There is a <u>deadline date</u> for each module's questions.").eP();

            htm.sP().add("Your score on the Course Mastery Exam is <b>3 points</b> for every question answered ",
                    "correctly \"on time\" (on or before its deadline), and <b>2 points</b> for every question ",
                    "answered correctly \"late\" (after its deadline).  ").eP();

            htm.sP().add("When a Homework Assignment is passed, the corresponding questions on the Course ",
                    "Mastery Exam are unlocked.").eP();

            htm.sP().add("Any time you have questions unlocked, you can ask to take the Course Mastery Exam in the ",
                    "Precalculus Center (Weber 138).  You will be given an exam with all unlocked questions that ",
                    "you have not already answered correctly.").eP();

            htm.sP().add("You have unlimited attempts on the Course Mastery Exam, through the last day of ",
                    "classes.").eP();

            htm.sP().add("The maximum possible score is 144 points.  Your grade in the course is based only on your ",
                    "score on this exam.").eP();

            htm.sTable("grades", "style='margin-left: 20px;'");
            htm.sTr().sTh().add("Score").eTh().sTh().add("Earned Grade").eTh().eTr();
            htm.sTr().sTd().add("134 - 144").eTh().sTh().add("A").eTh().eTr();
            htm.sTr().sTd().add("120 - 133").eTh().sTh().add("B").eTh().eTr();
            htm.sTr().sTd().add("108 - 119").eTh().sTh().add("C").eTh().eTr();
            htm.sTr().sTd().add("107 or less").eTh().sTh().add("U").eTh().eTr();
            htm.eTable();

            htm.eDiv(); // module-item

            htm.sDiv("module-item");

            // Show exam statistics

            int score = 0;
            int onTime = 0;
            int late = 0;
            int unlocked = 0;
            int locked = 0;

            final int[] counts = new int[3];

            for (int module = 1; module <= 8; ++module) {

                final AssignmentStatus exam1 = examStatus[module][1];
                classifyExams(counts, exam1);
                score += exam1.pointsEarned();
                onTime += counts[0];
                late += counts[1];
                if (counts[2] > 0) {
                    final AssignmentStatus hw1 = hwStatus[module][1];
                    if (hw1.whenCompleted() == null) {
                        locked += counts[2];
                    } else {
                        unlocked += counts[2];
                    }
                }

                Arrays.fill(counts, 0);
                final AssignmentStatus exam2 = examStatus[module][2];
                classifyExams(counts, exam2);
                score += exam2.pointsEarned();
                onTime += counts[0];
                late += counts[1];
                if (counts[2] > 0) {
                    final AssignmentStatus hw2 = hwStatus[module][2];
                    if (hw2.whenCompleted() == null) {
                        locked += counts[2];
                    } else {
                        unlocked += counts[2];
                    }
                }

                Arrays.fill(counts, 0);
                final AssignmentStatus exam3 = examStatus[module][3];
                classifyExams(counts, exam3);
                score += exam3.pointsEarned();
                onTime += counts[0];
                late += counts[1];
                if (counts[2] > 0) {
                    final AssignmentStatus hw3 = hwStatus[module][3];
                    if (hw3.whenCompleted() == null) {
                        locked += counts[2];
                    } else {
                        unlocked += counts[2];
                    }
                }
            }

            // Show exam statistics

            htm.sTable();
            htm.sTr().sTd().add("Total Points Earned So Far: ").eTd()
                    .sTd().add(Integer.toString(score)).eTd().eTr();
            htm.sTr().sTd().add("Questions Answered Correctly On-Time: ").eTd()
                    .sTd().add(Integer.toString(onTime)).eTd().eTr();
            htm.sTr().sTd().add("Questions Answered Correctly Late: ").eTd()
                    .sTd().add(Integer.toString(late)).eTd().eTr();
            htm.sTr().sTd().add("Questions Currently Unlocked: ").eTd()
                    .sTd().add(Integer.toString(unlocked)).eTd().eTr();
            htm.sTr().sTd().add("Questions Still Locked: ").eTd()
                    .sTd().add(Integer.toString(locked)).eTd().eTr();
            htm.eTable();

            htm.eDiv(); // module-item

            htm.sDiv("module-item");

            // Show details of each module's questions

            for (int module = 1; module <= 8; ++module) {

                final AssignmentStatus exam1 = examStatus[module][1];
                final AssignmentStatus exam2 = examStatus[module][2];
                final AssignmentStatus exam3 = examStatus[module][3];

                Arrays.fill(counts, 0);
                classifyExams(counts, exam1);
                classifyExams(counts, exam2);
                classifyExams(counts, exam3);

                htm.addln("<img class='assignment-icon' src='/www/images/etext/video_icon22.png' alt=''/>");
                htm.sDiv("assignment-title");

                htm.sDiv("module-item-block");
                final Integer moduleObj = Integer.valueOf(module);
                htm.addln("Module ", moduleObj, " Exam Questions:").br();

                htm.add("<small>");

                final LocalDate dueDate = exam1.dueDate() == null ? (exam2.dueDate() == null
                        ? exam3.dueDate() : exam2.dueDate()) : exam1.dueDate();

                if (dueDate == null) {
                    // A misconfiguration, but try our best...
                    if (hw.pointsPossible() > 0) {
                        if (hw.whenCompleted() == null) {
                            htm.add("--/" + hw.pointsPossible() + " pts");
                        } else {
                            htm.add(hw.pointsEarned() + "/" + hw.pointsPossible() + " pts (passed on ",
                                    TemporalUtils.FMT_MDY.format(hw.whenCompleted()), ")");
                        }
                    } else if (hw.whenCompleted() == null) {
                        htm.add("(not yet passed)");
                    } else {
                        htm.add("passed on ", TemporalUtils.FMT_MDY.format(hw.whenCompleted()), ")");
                    }
                } else {
                    htm.add("<b>Due</b> ", TemporalUtils.FMT_MDY.format(dueDate));

                    if (hw.pointsPossible() > 0) {
                        if (hw.whenCompleted() == null) {
                            htm.add(" | --/" + hw.pointsPossible() + " pts");
                        } else {
                            htm.add(" | ", hw.pointsEarned() + "/" + hw.pointsPossible() + " pts (passed on ",
                                    TemporalUtils.FMT_MDY.format(hw.whenCompleted()), ")");
                        }
                    } else if (hw.whenCompleted() == null) {
                        htm.add("(not yet passed)");
                    } else {
                        htm.add("passed on ", TemporalUtils.FMT_MDY.format(hw.whenCompleted()), ")");
                    }
                }

                htm.add("&nbsp;</small>");
                htm.eDiv();

                htm.eDiv(); // assignment-title

            }

            htm.eDiv(); // module-item

            endModule(htm);
        }

        CanvasPageUtils.endPage(htm);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
    }

    /**
     * Determines how many questions were answered "on time" and how many were answered "late" based on exam scores.
     *
     * @param counts a 3-int array whose [0] entry is the number answered on time, [1] entry is the number answered
     *               late, and [2] is the number not yet answered correctly.
     * @param exam   the exam status
     */
    private static void classifyExams(final int[] counts, final AssignmentStatus exam) {

        switch (exam.pointsEarned()) {
            case 6:
                counts[0] += 2;
                break;
            case 5:
                ++counts[0];
                ++counts[1];
                break;
            case 4:
                counts[1] += 2;
                break;
            case 3:
                ++counts[0];
                ++counts[2];
                break;
            case 2:
                ++counts[1];
                ++counts[2];
                break;
            default:
                counts[2] += 2;
                break;
        }
    }

    /**
     * Emits an assignment entry for a homework.
     *
     * @param htm       the {@code HtmlBuilder} to which to append
     * @param hw        the assignment status
     * @param module    the module number
     * @param objective the objective number
     */
    private static void emitHw(final HtmlBuilder htm, final AssignmentStatus hw, final Integer module,
                               final Integer objective) {

        htm.addln("<img class='assignment-icon' src='/www/images/etext/video_icon22.png' alt=''/>");
        htm.sDiv("assignment-title");

        htm.sDiv("module-item-block");
        htm.addln("<a class='ulink2' href='homework.html'><b>Homework ", module, ".", objective, "</b></a>").br();

        htm.add("<small>");
        if (hw.dueDate() == null) {
            if (hw.pointsPossible() > 0) {
                if (hw.whenCompleted() == null) {
                    htm.add("--/" + hw.pointsPossible() + " pts");
                } else {
                    htm.add(hw.pointsEarned() + "/" + hw.pointsPossible() + " pts (passed on ",
                            TemporalUtils.FMT_MDY.format(hw.whenCompleted()), ")");
                }
            } else if (hw.whenCompleted() == null) {
                htm.add("(not yet passed)");
            } else {
                htm.add("passed on ", TemporalUtils.FMT_MDY.format(hw.whenCompleted()), ")");
            }
        } else {
            htm.add("<b>Due</b> ", TemporalUtils.FMT_MDY.format(hw.dueDate()));
            if (hw.pointsPossible() > 0) {
                if (hw.whenCompleted() == null) {
                    htm.add(" | --/" + hw.pointsPossible() + " pts");
                } else {
                    htm.add(" | ", hw.pointsEarned() + "/" + hw.pointsPossible() + " pts (passed on ",
                            TemporalUtils.FMT_MDY.format(hw.whenCompleted()), ")");
                }
            } else if (hw.whenCompleted() == null) {
                htm.add("(not yet passed)");
            } else {
                htm.add("passed on ", TemporalUtils.FMT_MDY.format(hw.whenCompleted()), ")");
            }
        }
        htm.add("&nbsp;</small>");
        htm.eDiv();

        htm.eDiv(); // assignment-title
    }

    /**
     * Attempts to find a homework assignment for a unit and objective and construct an assignment status object for
     * it.
     *
     * @param unit           the unit (module number, from 1)
     * @param objective      the objective (learning target number, from 1)
     * @param assignments    the list of assignments for the course
     * @param stuAssignments the list of student assignment submissions so far
     * @return the constructed assignment status object; {@code null} if the assignment could not be found
     */
    private static HomeworkStatus findHomework(final Integer unit, final Integer objective,
                                               final Iterable<AssignmentRec> assignments,
                                               final Iterable<RawSthomework> stuAssignments) {

        AssignmentRec foundAssignment = null;

        for (final AssignmentRec test : assignments) {
            if (unit.equals(test.unit) && objective.equals(test.objective)) {
                foundAssignment = test;
                break;
            }
        }

        HomeworkStatus result = null;

        if (foundAssignment != null) {
            LocalDate firstPassed = null;
            for (final RawSthomework test : stuAssignments) {
                if ("Y".equals(test.passed) && test.version.equals(foundAssignment.assignmentId)) {
                    if (firstPassed == null || firstPassed.isAfter(test.hwDt)) {
                        firstPassed = test.hwDt;
                    }
                }
            }

            result = new HomeworkStatus(unit, objective, firstPassed);
        }

        return result;
    }

    /**
     * Attempts to find a mastery exam for a unit and objective and construct an assignment status object for it.
     *
     * @param unit          the unit (module number, from 1)
     * @param objective     the objective (learning target number, from 1)
     * @param hwStatus      the homework status for the unit/objective
     * @param exams         the list of mastery exams for the course
     * @param stuExams      the list of student mastery attempts so far
     * @param milestones    the list of milestones in the course
     * @param stuMilestones the list of student milestone overrides
     * @return the constructed assignment status object; {@code null} if the assignment could not be found
     */
    private static ExamStatus findExam(final Integer unit, final Integer objective,
                                       final HomeworkStatus hwStatus,
                                       final Iterable<MasteryExamRec> exams,
                                       final Iterable<MasteryAttemptRec> stuExams,
                                       final Iterable<StandardMilestoneRec> milestones,
                                       final Iterable<StudentStandardMilestoneRec> stuMilestones) {

        MasteryExamRec foundExam = null;

        for (final MasteryExamRec test : exams) {
            if (unit.equals(test.unit) && objective.equals(test.objective)) {
                foundExam = test;
                break;
            }
        }

        ExamStatus result = null;

        if (foundExam != null) {

            // Determine the first date the exam was passed
            LocalDate firstPassed = null;
            for (final MasteryAttemptRec test : stuExams) {
                if ("Y".equals(test.passed) && test.examId.equals(foundExam.examId)) {
                    final LocalDate examDate = test.whenFinished.toLocalDate();
                    if (firstPassed == null || firstPassed.isAfter(examDate)) {
                        firstPassed = examDate;
                    }
                }
            }

            // Determine the original due date
            LocalDate dueDate = null;
            for (final StandardMilestoneRec test : milestones) {
                if ("MA".equals(test.msType) && unit.equals(test.unit) && objective.equals(test.objective)) {
                    dueDate = test.msDate;
                    break;
                }
            }
            if (dueDate != null) {
                for (final StudentStandardMilestoneRec test : stuMilestones) {
                    if ("MA".equals(test.msType) && unit.equals(test.unit) && objective.equals(test.objective)) {
                        dueDate = test.msDate;
                        break;
                    }
                }
            }




//            private record ExamStatus(Integer unit, Integer objective, LocalDate dueDate, int pointsEarned,
//                                      int numAnsweredOnTime, int numAnsweredLate, int numUnlocked, int numLocked)

            result = new ExamStatus(unit, objective, dueDate, pointsEarned,
                    numOnTime, numLate, numUnlocked, numLocked);
        }

        return result;
    }

    /**
     * Emits the HTML to start a module.
     *
     * @param htm   the {@code HtmlBuilder} to which to append
     * @param title the module title
     */
    private static void startModule(final HtmlBuilder htm, final String title) {

        htm.addln("<details open class='module'>");
        htm.addln("  <summary class='module-summary'>", title, "</summary>");
    }

    /**
     * Emits the HTML to end a module.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void endModule(final HtmlBuilder htm) {

        htm.addln("</details>");
    }

    /**
     * A container for a homework assignment.
     *
     * @param unit          the unit (module number, from 1)
     * @param objective     the objective (learning target number, from 1)
     * @param whenCompleted the date the assignment was completed (null if not yet completed)
     */
    private record HomeworkStatus(Integer unit, Integer objective, LocalDate whenCompleted) {
    }

    /**
     * A container for a section of the mastery exam.
     *
     * @param unit              the unit (module number, from 1)
     * @param objective         the objective (learning target number, from 1)
     * @param dueDate           the due date ({@code null} if none)
     * @param pointsEarned      the number of points earned
     * @param numAnsweredOnTime the number of questions answered on time
     * @param numAnsweredLate   the number of questions answered late
     * @param numUnlocked       the number of questions unlocked
     * @param numLocked         the number of questions still locked
     */
    private record ExamStatus(Integer unit, Integer objective, LocalDate dueDate, int pointsEarned,
                              int numAnsweredOnTime, int numAnsweredLate, int numUnlocked, int numLocked) {
    }
}
