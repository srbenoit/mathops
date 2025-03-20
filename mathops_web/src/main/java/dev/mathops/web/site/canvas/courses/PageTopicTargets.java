package dev.mathops.web.site.canvas.courses;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawCsectionLogic;
import dev.mathops.db.old.rawlogic.RawSthomeworkLogic;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.rec.AssignmentRec;
import dev.mathops.db.rec.MasteryAttemptRec;
import dev.mathops.db.rec.MasteryExamRec;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.reclogic.AssignmentLogic;
import dev.mathops.db.reclogic.MasteryAttemptLogic;
import dev.mathops.db.reclogic.MasteryExamLogic;
import dev.mathops.db.reclogic.TermLogic;
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
import java.util.List;

/**
 * This page shows the learning targets and exam status associated with a topic module.
 */
public enum PageTopicTargets {
    ;

    /**
     * Starts the page that shows the course outline with student progress.
     *
     * @param cache    the data cache
     * @param site     the owning site
     * @param courseId the course ID
     * @param topicId  the topic ID
     * @param req      the request
     * @param resp     the response
     * @param session  the user's login session information
     * @param metadata the metadata object with course structure data
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final CanvasSite site, final String courseId,
                             final String topicId, final ServletRequest req, final HttpServletResponse resp,
                             final ImmutableSessionInfo session, final Metadata metadata)
            throws IOException, SQLException {

        final String stuId = session.getEffectiveUserId();
        final RawStcourse registration = CanvasPageUtils.confirmRegistration(cache, stuId, courseId);

        if (registration == null) {
            Log.warning("No registration found for student ", stuId, " in course ", courseId);
            final String homePath = site.makeRootPath("home.html");
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
                Log.warning("No course section record for ", courseId, " section ", registration.sect);
                final String homePath = site.makeRootPath("home.html");
                resp.sendRedirect(homePath);
            } else {
                final MetadataCourse metaCourse = metadata.getCourse(registration.course);
                if (metaCourse == null) {
                    Log.warning("No course metadata for ", courseId);
                    // TODO: Error display, course not part of this system rather than a redirect to Home
                    final String homePath = site.makeRootPath("home.htm");
                    resp.sendRedirect(homePath);
                } else {
                    boolean seeking = true;
                    for (final MetadataCourseModule metaCourseModule : metaCourse.modules) {
                        if (topicId.equals(metaCourseModule.id)) {
                            presentTargetsPage(cache, site, req, resp, registration, csection, metaCourse,
                                    metaCourseModule);
                            seeking = false;
                            break;
                        }
                    }
                    if (seeking) {
                        Log.warning("No course topic metadata for topic ", topicId, " in ", courseId);
                        final String homePath = site.makeRootPath("home.html");
                        resp.sendRedirect(homePath);
                    }
                }
            }
        }
    }

    /**
     * Presents  a course and module.
     *
     * @param cache            the data cache
     * @param site             the owning site
     * @param req              the request
     * @param resp             the response
     * @param registration     the student registration record
     * @param csection         the course section configuration record
     * @param metaCourse       metadata related to the course
     * @param metaCourseModule metadata related to the topic module within the course
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void presentTargetsPage(final Cache cache, final CanvasSite site, final ServletRequest req,
                                   final HttpServletResponse resp, final RawStcourse registration,
                                   final RawCsection csection, final MetadataCourse metaCourse,
                                   final MetadataCourseModule metaCourseModule) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        final String siteTitle = site.getTitle();

        CanvasPageUtils.startPage(htm, siteTitle);

        // Emit the course number and section at the top
        CanvasPageUtils.emitCourseTitleAndSection(htm, metaCourse, csection);

        htm.sDiv("pagecontainer");

        CanvasPageUtils.emitLeftSideMenu(htm, metaCourse, "../", ECanvasPanel.MODULES);

        htm.sDiv("flexmain");

        final MetadataTopic meta = metaCourseModule.topicMetadata;

        htm.sH(2);
        if (meta.thumbnailFile != null) {
            final String imageUrl = "/media/" + metaCourseModule.directory + "/" + meta.thumbnailFile;
            if (meta.thumbnailAltText == null) {
                htm.addln("<img class='module-thumb' src='", imageUrl, "'/>");
            } else {
                htm.addln("<img class='module-thumb' src='", imageUrl, "' alt='", meta.thumbnailAltText, "'/>");
            }
        }
        htm.sDiv("module-title");
        htm.add(metaCourseModule.heading, " Learning Targets").br();
        htm.addln("<div style='color:#D9782D; margin-top:6px;'>", meta.title, "</div>");
        htm.addln("<a class='smallbtn' href='module.html'>Open Textbook Chapter</a>");
        htm.eDiv();
        htm.eH(2);

        htm.hr();
        htm.div("vgap0");

        htm.sDiv("left");
        htm.addln("<img class='module-thumb' src='/www/images/etext/target_thumb.png' ",
                "alt='A dartboard with several magnetic darts.'/>");
        htm.eDiv();

        htm.sH(3).add("Module Learning Targets").eH(3);
        htm.sDiv("clear").eDiv();

        htm.sP("indent").add("Each Learning Target has a two-question exam.  You need to answer both ",
                "questions correctly on this exam to complete the learning target.").eP();

        htm.sP("indent").add("Learning Target exams are locked until you pass the homework assignment for the ",
                "Learning Target.").eP();

        htm.sP("indent").add("Once you have passed the learning target homework assignment, you have unlimited ",
                "attempts on Learning Target exam.").eP();

        htm.sP("indent").add("If you answer one of the two questions correctly twice, but still need to get the ",
                "other question correct, you will not have to keep re-doing the question you got correct twice (it ",
                "will be removed from the exam).").eP();

        htm.div("vgap");

        try {
            final Integer unit = Integer.parseInt(metaCourseModule.id.substring(1));

            final List<AssignmentRec> assignments = AssignmentLogic.get(cache).queryActiveByCourse(cache,
                    registration.course, "ST");

            final List<RawSthomework> sthw = RawSthomeworkLogic.getHomeworks(cache, registration.stuId,
                    registration.course, false, "ST");

            final List<MasteryExamRec> exams = MasteryExamLogic.get(cache).queryActiveByCourse(cache,
                    registration.course);

            final List<MasteryAttemptRec> stexams = MasteryAttemptLogic.get(cache).queryByStudent(cache,
                    registration.stuId);

            for (final MasteryExamRec exam : exams) {
                if (unit.equals(exam.unit) && exam.objective.intValue() > 0) {

                    // Find the corresponding homework assignment
                    for (final AssignmentRec assignment : assignments) {

                        if (assignment.unit.equals(exam.unit) && assignment.objective.equals(exam.objective)) {
                            presentTarget(htm, exam, stexams, assignment, sthw);
                        }
                    }
                }
            }
            htm.hr();
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            htm.sP().add("Error: Unable to load homework assignments for module ", metaCourseModule.id).eP();
        }


        htm.sP();
        htm.add("All Learning Target exams can be taken in the <strong>Precalculus Center</strong> (Weber 238).");
        htm.eP();

        htm.eDiv(); // flexmain
        htm.eDiv(); // pagecontainer

        CanvasPageUtils.endPage(htm);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
    }

    /**
     * Presents a single homework assignment.
     *
     * @param htm        the {@code HtmlBuilder} to which to append
     * @param exam       the mastery exam
     * @param stexam     the set of all submitted student exams in this course
     * @param assignment the corresponding assignment
     * @param sthw       the set of all submitted student assignments in this course
     */
    private static void presentTarget(final HtmlBuilder htm, final MasteryExamRec exam,
                                      final List<MasteryAttemptRec> stexam, final AssignmentRec assignment,
                                      final List<RawSthomework> sthw) {

        htm.hr();
        htm.sH(4).add("Learning Target Exam: ", exam.title).eH(4);

        int numAttempts = 0;
        int topScore = 0;
        boolean passed = false;
        for (final MasteryAttemptRec attempt : stexam) {
            if (attempt.examId.equals(exam.examId)) {
                if ("Y".equals(attempt.passed)) {
                    passed = true;
                    ++numAttempts;
                    topScore = Math.max(topScore, attempt.examScore.intValue());
                } else if ("N".equals(attempt.passed)) {
                    ++numAttempts;
                    topScore = Math.max(topScore, attempt.examScore.intValue());
                }
                // Other values for "passed" indicate an "ignored" attempt
            }
        }

        htm.sP("indent");
        if (passed) {
            htm.add("<img src='/www/images/etext/box_checked_18.png' alt='Box with check mark'/> &nbsp; ",
                    "You have Completed this Learning Target.");
        } else {
            // See of the exam is "unlocked"
            boolean unlocked = false;
            for (final RawSthomework attempt : sthw) {
                if (attempt.version.equals(assignment.assignmentId)) {
                    if ("Y".equals(attempt.passed)) {
                        unlocked = true;
                        break;
                    }
                }
            }

            if (unlocked) {
                if (numAttempts == 0) {
                    htm.add("<img src='/www/images/etext/box_unchecked_18.png' alt='Empty box'/> &nbsp; ",
                            "You have not yet taken this Learning Target Exam.");
                } else if (numAttempts == 1) {
                    htm.add("<img src='/www/images/etext/box_unchecked_18.png' alt='Empty box'/> &nbsp; ",
                            "You have attempted this Learning Target Exam 1 time.");
                } else {
                    final String count = Integer.toString(numAttempts);
                    htm.add("<img src='/www/images/etext/box_unchecked_18.png' alt='Empty box'/> &nbsp; ",
                            "You have attempted this Learning Target Exam ", count, " times.");
                }
            } else {
                htm.add("<img src='/www/images/etext/box_unchecked_18.png' alt='Empty box'/> &nbsp; ",
                        "You need to pass the ", assignment.title, " homework assignment to unlock the ",
                        exam.title, " Learning Target Exam");
            }
        }
        htm.eP();
    }
}
