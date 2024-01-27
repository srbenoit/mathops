package dev.mathops.web.site.tutorial.elm;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.logic.ELMTutorialStatus;
import dev.mathops.db.old.rawlogic.RawCsectionLogic;
import dev.mathops.db.old.rawlogic.RawCusectionLogic;
import dev.mathops.db.old.rawlogic.RawExamLogic;
import dev.mathops.db.old.rawrecord.RawCunit;
import dev.mathops.db.old.rawrecord.RawCuobjective;
import dev.mathops.db.old.rawrecord.RawCusection;
import dev.mathops.db.old.rawrecord.RawExam;
import dev.mathops.db.old.rawrecord.RawLesson;
import dev.mathops.db.old.rawrecord.RawLessonComponent;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.servlet.CourseLesson;
import dev.mathops.session.sitelogic.servlet.StudentCourseStatus;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Map;

/**
 * Generates the content of the web page that displays the outline of a course tailored to a student's position and
 * status in the course.
 */
enum PageOutline {
    ;

    /**
     * Starts the page that shows the course outline with student progress.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @param status  the student status with respect to the ELM Tutorial
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final ElmTutorialSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                      final ELMTutorialStatus status) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, site.getTitle(), session, false, "Entry Level Mathematics Tutorial",
                "/elm-tutorial/home.html", Page.ADMIN_BAR, null, false, true);

        htm.sDiv("menupanel");
        TutorialMenu.buildMenu(cache, session, status, htm);
        htm.sDiv("panel");

        doOutlinePage(cache, site, session, status, htm);

        htm.eDiv(); // panel
        htm.eDiv(); // menupanel
        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates the HTML of the tutorial outline.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param session the user's login session information
     * @param status  the student status with respect to the ELM Tutorial
     * @param htm     the {@code HtmlBuilder} to which to append the HTML
     * @throws SQLException if there is an error accessing the database
     */
    private static void doOutlinePage(final Cache cache, final ElmTutorialSite site,
                                      final ImmutableSessionInfo session, final ELMTutorialStatus status,
                                      final HtmlBuilder htm)
            throws SQLException {

        final String studentId = session.getEffectiveUserId();
        final StudentCourseStatus courseStatus = new StudentCourseStatus(site.getDbProfile());

        if (courseStatus.gatherData(cache, session, studentId, RawRecordConstants.M100T, false,
                false) && courseStatus.getCourse().courseName != null) {

            htm.add("<h2 class='title' style='margin-bottom:3px;'>");
            htm.add(courseStatus.getCourse().courseName);
            htm.addln("</h2>");

            doCourseMedia(courseStatus, htm);

            final String topmatter =
                    RawCsectionLogic.getTopmatter(courseStatus.getCourseSection().course);

            if (topmatter != null) {
                htm.sP().add(topmatter).eP();
            }

            htm.div("clear");
            htm.div("vgap");
            htm.hr();

            boolean told = false;
            for (int i = 1; i < 5; ++i) {
                told = doUnit(cache, site, status, courseStatus, courseStatus.getCourseUnit(i),
                        told, htm);
            }
        } else {
            htm.sP().add("FAILED TO GET COURSE DATA").br();
            if (courseStatus.getErrorText() != null) {
                htm.add(courseStatus.getErrorText());
            }
            htm.eP();
        }
    }

    /**
     * Present the course media.
     *
     * @param courseStatus the student's status in the course
     * @param htm          the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doCourseMedia(final StudentCourseStatus courseStatus, final HtmlBuilder htm) {

        final Map<String, Map<String, String>> media = courseStatus.getMedia();

        if (!media.isEmpty()) {
            for (final Map.Entry<String, Map<String, String>> entry : media.entrySet()) {
                htm.sDiv("vlines");
                htm.sH(5).add(entry.getKey()).eH(5);

                final Map<String, String> values = entry.getValue();

                for (final Map.Entry<String, String> e : values.entrySet()) {
                    final String url = e.getValue();

                    htm.sDiv();

                    if (url.endsWith(".pdf")) {
                        htm.add("<img src='/images/pdf.png' alt=''/>");
                    } else if (url.endsWith(".xls")) {
                        htm.add("<img src='/images/excel.png' alt=''/>");
                    } else if (url.endsWith(".txt")) {
                        htm.add("<img src='/images/text.png' alt=''/>");
                    } else if (url.endsWith(".html")) {
                        htm.add("<img src='/images/html.png' alt=''/>");
                    } else if (url.endsWith(".doc")) {
                        htm.add("<img src='/images/word.png' alt=''/>");
                    } else if (url.endsWith(".zip")) {
                        htm.add("<img src='/images/zip.png' alt=''/>");
                    }

                    htm.addln(" <a class='linkbtn' href='", url, "'>", e.getKey(), "</a>").eDiv();
                }

                htm.eDiv();
            }

            htm.div("clear");
        }
    }

    /**
     * Present a unit of instruction.
     *
     * @param cache        the data cache
     * @param site         the course site
     * @param status       the student status with respect to the ELM Tutorial
     * @param courseStatus the student's status in the course
     * @param unit         the unit model
     * @param told         {@code true} if the user has already been told why they cannot proceed
     * @param htm          the {@code HtmlBuilder} to which to append the HTML
     * @return the new value for {@code told}
     * @throws SQLException if there is an error accessing the database
     */
    private static boolean doUnit(final Cache cache, final ElmTutorialSite site, final ELMTutorialStatus status,
                                  final StudentCourseStatus courseStatus, final RawCunit unit,
                                  final boolean told, final HtmlBuilder htm) throws SQLException {

        final int unitNum = unit.unit.intValue();

        doUnitHeading(courseStatus, unitNum, htm);

        final boolean newTold = doUnitLessons(cache, site, courseStatus, unitNum, htm, told);

        final RawCusection cusect = courseStatus.getCourseSectionUnit(unitNum);
        if (cusect == null) {
            Log.warning("No course section unit for unit " + unitNum);
        } else {
            doUnitReviewExam(cache, status, courseStatus, unitNum, htm);
        }

        htm.hr();

        return newTold;
    }

    /**
     * Present the header in the course outline for a unit.
     *
     * @param courseStatus the student's status in the course
     * @param unitNum      the unit number
     * @param htm          the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doUnitHeading(final StudentCourseStatus courseStatus, final int unitNum,
                                      final HtmlBuilder htm) {

        final RawCunit courseUnit = courseStatus.getCourseUnit(unitNum);
        final RawCusection courseSecUnit = courseStatus.getCourseSectionUnit(unitNum);

        htm.sDiv(null, "style='float:left;padding-right:5px;'");
        htm.addln(" <img src='/images/stock-jump-to-32.png' alt=''/>");
        htm.eDiv();

        htm.sDiv();
        htm.addln(" <a name='unit", Integer.toString(unitNum), "'></a>");

        htm.add(" <h3><span class='green' style='position:relative;top:3px;'>Unit ", Integer.toString(unitNum));
        if (courseUnit.unitDesc != null) {
            htm.add(": ", courseUnit.unitDesc);
        }
        htm.addln("</span></h3>").eDiv();

        if (courseSecUnit != null) {
            final String topmatter = RawCusectionLogic.getTopmatter(courseSecUnit);
            if (topmatter != null) {
                htm.add(topmatter);
            }
        }

        htm.div("clear");
        htm.div("gap");
    }

    /**
     * Present the list of lessons in a unit.
     *
     * @param cache        the data cache
     * @param site         the course site
     * @param courseStatus the student's status in the course
     * @param unit         the unit
     * @param htm          the {@code HtmlBuilder} to which to append the HTML
     * @param told         {@code true} if user has already been told why he can't move on
     * @return the new value for the {@code told} input
     * @throws SQLException if there is an error accessing the database
     */
    private static boolean doUnitLessons(final Cache cache, final ElmTutorialSite site,
                                         final StudentCourseStatus courseStatus, final int unit, final HtmlBuilder htm,
                                         final boolean told) throws SQLException {

        final int count = courseStatus.getNumLessons(unit);
        final CourseLesson less = new CourseLesson(site.getDbProfile());
        final String courseId = courseStatus.getCourse().course;

        htm.sDiv("indent33");
        htm.addln("<table>");

        for (int i = 0; i < count; ++i) {
            final RawCuobjective culesson = courseStatus.getCourseUnitObjective(unit, i);
            final RawLesson lesson = courseStatus.getLesson(unit, i);
            final Integer seqNum = culesson.objective;
            final String lessNum = culesson.lessonNbr;
            final String status = courseStatus.getHomeworkStatus(unit, seqNum.intValue());

            // Show any "PREMED" media components for the lesson in the outline page
            if (less.gatherData(cache, courseId, Integer.valueOf(unit), culesson.objective)) {
                final int numComp = less.getNumComponents();

                boolean hasPre = false;
                for (int j = 0; j < numComp; ++j) {
                    final RawLessonComponent comp = less.getLessonComponent(j);
                    if ("PREMED".equals(comp.type)) {
                        hasPre = true;
                        break;
                    }
                }

                if (hasPre) {
                    htm.add("<tr><td style='height:8px;'></td></tr>");

                    for (int j = 0; j < numComp; ++j) {
                        final RawLessonComponent comp = less.getLessonComponent(j);
                        if ("PREMED".equals(comp.type)) {
                            final String xml = comp.xmlData;
                            htm.add("<tr><td colspan='2' class='open' style='white-space:nowrap;'>");
                            htm.add(xml.replace("%%MODE%%", "course"));
                            htm.addln("</td></tr>");
                        }
                    }

                    htm.add("<tr><td style='height:4px;'></td></tr>");
                }
            }

            htm.addln("<tr>");

            htm.add("<td class='open' style='text-align:right;font-family: factoria-medium,sans-serif;'>");
            if (lessNum != null) {
                htm.add(lessNum, ":&nbsp;");
            }
            htm.add("</td><td style='white-space:nowrap;'>");
            htm.add("<a class='linkbtn' href='lesson.html?unit=", Integer.toString(unit), "&lesson=", seqNum);
            htm.add("'>");
            htm.addln(lesson.descr, "</a> &nbsp; </td>");

            if ("May Move On".equals(status) || "Completed".equals(status)) {
                htm.add("<td class='green'><img src='/images/check.png' alt=''/> ", status, "</td>");

            }
        }
        htm.addln("</tr>");

        htm.addln("</table>");
        htm.eDiv();

        return told;
    }

    /**
     * Present the button for the unit review exam along with status.
     *
     * @param cache        the data cache
     * @param status       the student status with respect to the ELM Tutorial
     * @param courseStatus the student's status in the course
     * @param unitNum      the unit
     * @param htm          the {@code HtmlBuilder} to which to append the HTML
     * @throws SQLException if there is an error accessing the database
     */
    private static void doUnitReviewExam(final Cache cache, final ELMTutorialStatus status,
                                         final StudentCourseStatus courseStatus, final int unitNum,
                                         final HtmlBuilder htm) throws SQLException {

        final String courseId = courseStatus.getCourse().course;
        final boolean reviewAvail = courseStatus.isReviewExamAvailable(unitNum);
        final RawExam reviewExam = RawExamLogic.queryActiveByCourseUnitType(cache, courseId,
                Integer.valueOf(unitNum), "R");
        final RawExam unitExam = RawExamLogic.queryActiveByCourseUnitType(cache, courseId,
                Integer.valueOf(unitNum), "U");

        final String label;
        final String version;

        if (reviewExam == null) {
            label = null;
            version = null;
        } else {
            label = reviewExam.buttonLabel;
            version = reviewExam.version;
        }

        if (version != null) {
            htm.addln("<form method='get' action='run_review.html'>");
            htm.sDiv("exambtn");

            if (reviewAvail) {
                htm.addln(" <input type='hidden' name='exam' value='", version, "'/>");
                htm.addln(" <input class='btn' type='submit' value='", label, "'/> &nbsp;");

                final String revtatus = courseStatus.getReviewStatus(unitNum);

                if ("Passed".equals(revtatus)) {
                    htm.addln(" <img src='/images/check.png' alt=''/> <span class='green'>Passed</span>").br();
                } else {
                    htm.addln(" <span class='red'>", revtatus, "</span>").br();
                }
            } else {
                htm.add(" <input class='btn' type='submit' disabled='disabled' value='", label, "'/> &nbsp; ");

                final String reason = courseStatus.getReviewReason(unitNum);

                if (reason != null) {
                    htm.add("<span class='red'>", reason, "</span>").br();
                }
                htm.addln();
            }
            htm.eDiv();
            htm.addln("</form>");

            if (unitExam != null) {
                // If the student can take their unit exam through ProctorU, and that exam is
                // available, offer them the option to take it.
                if (unitNum == 4 && status.eligibleForElmExam && !status.elmExamPassed) {
                    htm.div("vgap");

                    htm.sDiv("indent22");
                    htm.addln("<strong>You're almost done! You only need to pass the proctored ELM Exam.</strong>");
                    htm.eDiv();
                    htm.div("vgap");

                    htm.sDiv("note");

                    htm.add("You are eligible to take the <strong>ELM Exam</strong> in the  Precalculus Center.").br()
                            .add("&nbsp; &nbsp;<a class='btn' href='instructions_elm_tc.html'>Tell me more...</a>");

                    htm.div("vgap");
                    htm.addln("You are also eligible to take the <strong>ELM Exam</strong> through the ProctorU ",
                                    "proctoring service.").br()
                            .add("&nbsp; &nbsp; <a class='btn' href='instructions_elm_pu.html'>Tell me more...</a>");

                    htm.eDiv(); // note
                }

                final int curScore = courseStatus.getScores().getRawUnitExamScore(unitNum);
                final int perfectScore = courseStatus.getPerfectScore(unitNum);

                if (status.elmExamPassed) {
                    htm.div("vgap");
                    htm.sDiv("indent11");
                    htm.add("<img src='/images/check.png' alt=''/> ",
                            "<span style='background-color:#FF9; padding:3px;'>",
                            "You have <b class='blue'>passed</b> the <b>ELM Exam</b> with a score of ",
                            Integer.toString(curScore), " out of ", Integer.toString(perfectScore), ".</span>");

                    htm.div("vgap2");

                    htm.addln("Click on <a class='ulink' href='tutorial_status.html'>",
                            "View my Current Status</a> to view your exams and solutions.");
                    htm.eDiv(); // indent11
                } else {
                    final int timesTaken = courseStatus.getProctoredTimesTaken(unitNum);

                    if (timesTaken > 0) {
                        htm.div("vgap2");
                        htm.sDiv("indent22");
                        htm.add(" You have taken the ELM Exam ");
                        if (timesTaken == 1) {
                            htm.addln("one time. ");
                        } else {
                            htm.addln(Integer.toString(timesTaken), " times. ");
                        }
                        htm.addln(" Your current score is ", Integer.toString(curScore), " out of ",
                                Integer.toString(perfectScore));
                        htm.div("vgap");

                        if (!status.eligibleForElmExam && status.failedElmExamsSinceLastPassingReview >= 2) {
                            htm.addln("You must retake and pass the <b>Unit 4 Review Exam</b> to earn two more ",
                                    "attempts on the <b>ELM Exam</b>.");
                            htm.div("vgap");
                        }

                        htm.addln("Click on <a class='ulink' href='tutorial_status.html'>",
                                "View my Current Status</a> to view your exams and solutions.");
                        htm.eDiv();
                    } else {
                        htm.div("vgap");
                        htm.sDiv("indent22");
                        htm.addln("After all four Review Exams have been passed, you will be eligible to take the ",
                                "proctored <b>ELM Exam</b> to complete the ELM Tutorial.");
                        htm.eDiv();
                    }
                }
            }
        }
    }
}
