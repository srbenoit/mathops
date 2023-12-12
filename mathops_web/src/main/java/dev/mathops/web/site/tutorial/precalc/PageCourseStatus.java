
package dev.mathops.web.site.tutorial.precalc;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.TemporalUtils;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawlogic.RawCourseLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawrecord.RawCourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.session.ExamWriter;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Generates the content of the web page that displays the student's status in a single course.
 */
enum PageCourseStatus {
    ;

    /**
     * Displays the page that shows the student's current status is a course.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @param logic   the course site logic
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final PrecalcTutorialSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session,
                             final PrecalcTutorialSiteLogic logic) throws IOException, SQLException {

        final String course = req.getParameter("course");

        if (AbstractSite.isParamInvalid(course)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  course='", course, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, site.getTitle(), session, false, "Precalculus Tutorial",
                    "/precalc-tutorial/home.html", Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false, true);

            htm.sDiv("menupanel");
            TutorialMenu.buildMenu(session, logic, htm);
            htm.sDiv("panel");

            doCourseStatusContent(cache, logic, course, htm);

            htm.eDiv(); // (end "panel" div)
            htm.eDiv(); // (end "menupanel" div)
            Page.endOrdinaryPage(cache, site, htm, true);

            AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML,
                    htm.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Starts the page that shows the student's current status is a course.
     *
     * @param cache    the data cache
     * @param logic    the course site logic
     * @param courseId the course for which to generate the status page
     * @param htm      the {@code HtmlBuilder} to which to append the HTML
     * @throws SQLException if there is an error accessing the database
     */
    private static void doCourseStatusContent(final Cache cache, final PrecalcTutorialSiteLogic logic,
                                              final String courseId, final HtmlBuilder htm) throws SQLException {

        if (logic.hasTutorAccess()) {
            tutorStatusContent(cache, logic, courseId, htm);
        } else {
            final RawCourse eligibleCourse = logic.getEligibleCourse();

            if (eligibleCourse == null) {
                htm.sDiv("error").add("Unable to query course status.").eDiv();
            } else {
                final String associatedCourse = PrecalcTutorialSiteLogic.getAssociatedCourse(eligibleCourse);
                htm.sH(2).add("Precalculus Tutorial to place out of ", associatedCourse).eH(2);

                htm.sDiv("indent11");
                statusContent(cache, logic, htm);
                htm.eDiv(); // indent11
            }
        }
    }

    /**
     * Show the content of the course status page.
     *
     * @param cache    the data cache
     * @param logic    the course site logic
     * @param courseId the course ID
     * @param htm      the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void tutorStatusContent(final Cache cache, final PrecalcTutorialSiteLogic logic,
                                           final String courseId, final HtmlBuilder htm) throws SQLException {

        final RawCourse course = RawCourseLogic.query(cache, courseId);

        if (course == null) {
            Log.warning("Failed to query for course ", courseId);
        } else {
            htm.hr();
            htm.sH(3).add("Tutorial Progress:").eH(3);

            for (int unit = 0; unit <= 4; ++unit) {
                unitProgress(cache, logic, course, unit, htm, true);
            }
        }
    }

    /**
     * Show the content of the course status page.
     *
     * @param cache the data cache
     * @param logic the course site logic
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void statusContent(final Cache cache, final PrecalcTutorialSiteLogic logic,
                                      final HtmlBuilder htm) throws SQLException {

        final RawCourse course = logic.getEligibleCourse();

        htm.hr();
        htm.addln(" <strong>Tutorial Deadline:</strong>").br();
        htm.sP("indent11");

        final LocalDate deadline = logic.getDeadline(cache);
        if (deadline == null) {
            htm.addln("Unable to determine the date by which you need to complete this tutorial. Please inform the ",
                    "Precalculus Center of this issue with an email to <a class='ulink2' ",
                    "href='mailto:precalc_math@colostate.edu'>precalc_math@colostate.edu</a>.");
        } else {
            final String associatedCourse = PrecalcTutorialSiteLogic.getAssociatedCourse(course);

            htm.addln("In order to place out of ", associatedCourse, ", you must earn a passing ",
                    "score on the proctored <strong>Precalculus Tutorial Exam</strong> by ",
                    "<strong>", TemporalUtils.FMT_MDY.format(deadline), "</strong>.");
        }
        htm.eP(); // indent11

        // Show progress by unit
        htm.hr();
        htm.sH(3).add("Tutorial Progress:").eH(3);

        for (int unit = 0; unit <= 4; ++unit) {
            unitProgress(cache, logic, course, unit, htm, false);
        }
    }

    /**
     * Shows the progress of the user in one unit.
     *
     * @param cache       the data cache
     * @param logic       the course site logic
     * @param course      the course
     * @param unit        the unit
     * @param htm         the {@code HtmlBuilder} to which to append
     * @param tutorAccess true if the student has TUTOR access
     * @throws SQLException if there is an error accessing the database
     */
    private static void unitProgress(final Cache cache, final PrecalcTutorialSiteLogic logic, final RawCourse course,
                                     final int unit, final HtmlBuilder htm, final boolean tutorAccess)
            throws SQLException {

        final Integer unitInteger = Integer.valueOf(unit);

        htm.sDiv();
        htm.add(" <strong> &bull; ");
        if (unit == 0) {
            htm.add("Skills Review");
        } else {
            htm.add("Unit ", unitInteger, ": ");
        }
        htm.addln("</strong>");
        htm.eDiv();

        if (unit == 0) {
            showSkillsReviewProgress(cache, logic, course.course, unitInteger, htm, tutorAccess);
        } else {
            showUnitProgress(cache, logic, course.course, unitInteger, htm, tutorAccess);
        }

        if (!tutorAccess) {
            final String studentId = logic.getStudentId();
            final TermRec active = logic.getActiveTerm();

            final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId, course.course, unitInteger, false,
                    RawStexamLogic.ALL_EXAM_TYPES);

            for (final RawStexam exam : exams) {
                final String path = ExamWriter.makeWebExamPath(active.term.shortString, studentId,
                        exam.serialNbr.longValue());

                htm.sDiv("indent2");
                htm.addln(" <a class='ulink' href='see_past_exam.html?course=", course, "&exam=", exam.version,
                        "&xml=", path, CoreConstants.SLASH, ExamWriter.EXAM_FILE,
                        "&upd=", path, CoreConstants.SLASH, ExamWriter.ANSWERS_FILE,
                        "'>View the ", exam.getExamLabel(), "</a>");
                htm.eDiv();
            }
        }

        htm.div("vgap");
    }

    /**
     * Shows the progress for a Skills Review unit.
     *
     * @param cache       the data cache
     * @param logic       the course site logic
     * @param courseId    the course ID
     * @param unit        the unit
     * @param htm         the {@code HtmlBuilder} to which to append
     * @param tutorAccess true if the student has TUTOR access
     * @throws SQLException if there is an error accessing the database
     */
    private static void showSkillsReviewProgress(final Cache cache, final PrecalcTutorialSiteLogic logic,
                                                 final String courseId, final Integer unit, final HtmlBuilder htm,
                                                 final boolean tutorAccess)
            throws SQLException {

        if (tutorAccess) {
            htm.addln("Based on your course status, you are not required to pass the Skills Review Exam.");
        } else {
            htm.sDiv("indent11");

            final List<RawStexam> allTaken = RawStexamLogic.getExams(cache, logic.getStudentId(), courseId, unit,
                    false, "R", "Q");

            if (allTaken.isEmpty()) {
                htm.addln("You have <strong>not yet taken</strong> the Skills Review Exam.");
            } else {
                boolean passed = false;
                for (final RawStexam taken : allTaken) {
                    if ("Y".equals(taken.passed)) {
                        passed = true;
                        break;
                    }
                }

                if (passed) {
                    htm.addln("You have <strong>passed</strong> the Skills Review Exam.");
                } else {
                    htm.addln("You have <strong>not yet passed</strong> the Skills Review Exam.");
                }
            }

            htm.eDiv(); // indent11
        }
    }

    /**
     * Shows the progress for an instructional unit.
     *
     * @param cache       the data cache
     * @param logic       the course site logic
     * @param courseId    the course ID
     * @param unit        the unit
     * @param htm         the {@code HtmlBuilder} to which to append
     * @param tutorAccess true if the student has TUTOR access
     * @throws SQLException if there is an error accessing the database
     */
    private static void showUnitProgress(final Cache cache, final PrecalcTutorialSiteLogic logic,
                                         final String courseId, final Integer unit, final HtmlBuilder htm,
                                         final boolean tutorAccess) throws SQLException {

        if (tutorAccess) {
            if (unit.intValue() == 4) {
                htm.addln("Based on your course status, you are not required to pass the Unit ", unit,
                        " Review Exam or the associated Precalculus Tutorial exam.");
            } else {
                htm.addln("Based on your course status, you are not required to pass the Unit ", unit, " Review Exam.");
            }
        } else {
            htm.sDiv("indent11");

            final List<RawStexam> allReviews = RawStexamLogic.getExams(cache, logic.getStudentId(), courseId, unit,
                    false, "R");

            boolean eligibleForProctored = false;
            if (allReviews.isEmpty()) {
                htm.addln("You have <strong>not yet taken</strong> the Unit ", unit, " Review Exam.").br();
            } else {
                boolean passed = false;
                for (final RawStexam review : allReviews) {
                    if ("Y".equals(review.passed)) {
                        passed = true;
                        break;
                    }
                }

                if (passed) {
                    htm.addln("You have <strong>passed</strong> the Unit ", unit, " Review Exam.").br();
                    eligibleForProctored = true;
                } else {
                    htm.addln("You have <strong>not yet passed</strong> the Unit ", unit, " Review Exam.").br();
                }
            }

            if (unit.intValue() == 4) {
                final List<RawStexam> allProctored = RawStexamLogic.getExams(cache, logic.getStudentId(), courseId,
                        unit, false, "U");

                if (allProctored.isEmpty()) {
                    htm.addln("You have <strong>not yet taken</strong> the Precalculus Tutorial Exam.").br();
                } else {
                    boolean passed = false;
                    int failCount = 0;
                    int bestScore = 0;
                    for (final RawStexam proctored : allProctored) {
                        if ("Y".equals(proctored.passed)) {
                            passed = true;
                        } else if ("N".equals(proctored.passed)) {
                            ++failCount;
                            bestScore = Math.max(bestScore, proctored.examScore.intValue());
                        }
                    }

                    // NOTE: We really should never see the "passed" message below, because by that time, the
                    // student would no longer be eligible for this tutorial...

                    if (passed) {
                        htm.addln("You have <strong>passed</strong> the Precalculus Tutorial Exam.").br();
                    } else {
                        htm.add("You have attempted the Precalculus Tutorial Exam ");
                        if (failCount > 1) {
                            htm.addln(Integer.toString(failCount), " times.");
                        } else {
                            htm.addln("1 time.").br();
                        }

                        htm.sDiv("indent1");
                        htm.addln("Your best score on the Precalculus Tutorial Exam: <b style='color:blue'>",
                                Integer.toString(bestScore), "</b> (out of <strong>20</strong> possible, minimum ",
                                "required score is <strong>14</strong>).");
                        htm.eDiv(); // indent1

                        if (eligibleForProctored) {
                            htm.sDiv("indent1");
                            htm.addln("Your are eligible to take the Precalculus Tutorial Exam.");
                            htm.eDiv(); // indent1

                            // FIXME: Enforce "re-pass unit 4 review after 2 failed tries on proctored" rule.
                        }
                    }
                }
            }

            htm.eDiv(); // indent11
        }
    }
}
