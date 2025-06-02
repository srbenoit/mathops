package dev.mathops.web.host.placement.tutorial.precalc;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawCunit;
import dev.mathops.db.old.rawrecord.RawCuobjective;
import dev.mathops.db.old.rawrecord.RawExam;
import dev.mathops.db.old.rawrecord.RawLesson;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * Generates the content of the web page that displays the outline of a course tailored to a student's position and
 * status in the course.
 */
enum PageCourseOutline {
    ;

    /**
     * Starts the page that shows the course outline with student progress.
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
            PageError.doGet(cache, site, req, resp, session, "No course provided for course outline");
        } else if (course == null) {
            PageError.doGet(cache, site, req, resp, session, "No course provided for course outline");
        } else {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, site.getTitle(), session, false, "Precalculus Tutorial",
                    "/precalc-tutorial/home.html", Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false, true);

            htm.sDiv("menupanel");
            TutorialMenu.buildMenu(session, logic, htm);
            htm.sDiv("panel");

            doCourseOutlinePage(cache, course, htm, logic);

            htm.eDiv(); // (end "panel" div)
            htm.eDiv(); // (end "menupanel" div)
            Page.endOrdinaryPage(cache, site, htm, true);

            AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
        }
    }

    /**
     * Creates the HTML of the course outline.
     *
     * @param cache    the data cache
     * @param courseId the course for which to generate the status page
     * @param htm      the {@code HtmlBuilder} to which to append the HTML
     * @param logic    the course site logic
     * @throws SQLException if there is an error accessing the database
     */
    private static void doCourseOutlinePage(final Cache cache, final String courseId, final HtmlBuilder htm,
                                            final PrecalcTutorialSiteLogic logic) throws SQLException {

        final RawStudent student = logic.getStudent();
        final PrecalcTutorialCourseStatus tutStatus = new PrecalcTutorialCourseStatus(cache, student, courseId);

        final String associatedCourse = PrecalcTutorialSiteLogic.getAssociatedCourse(tutStatus.getCourse());
        htm.add("<h2 class='title' style='margin-bottom:3px;'>");
        htm.add("Precalculus Tutorial to place out of ", associatedCourse);
        htm.eH(2);

        doCourseMedia(htm);

        final String top = RawCsection.getTopmatter(tutStatus.getCourse().course);
        if (top != null) {
            htm.sP().add(top).eP();
        }

        htm.sDiv("clear").br().eDiv();
        htm.hr();

        doUnits(cache, logic, tutStatus, htm);
    }

    /**
     * Present the course media.
     *
     * @param htm the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doCourseMedia(final HtmlBuilder htm) {

        htm.sDiv("vlines");
        htm.addln("<h5>Tutorial Overview</h5>");
        htm.addln("<img src='/images/pdf.png' alt=''/> ",
                "<a class='linkbtn' href='/www/media/precalc_tutorial.pdf'>Instructions</a>");
        htm.eDiv(); // vlines

        htm.div("clear");
    }

    /**
     * Present each unit's topics with status.
     *
     * @param cache     the data cache
     * @param tutStatus the student's status in the tutorial
     * @param htm       the {@code HtmlBuilder} to which to append the HTML
     * @throws SQLException if there is an error accessing the database
     */
    private static void doUnits(final Cache cache, final PrecalcTutorialSiteLogic logic,
                                final PrecalcTutorialCourseStatus tutStatus, final HtmlBuilder htm)
            throws SQLException {

        doGatewayUnit(cache, logic, tutStatus, htm);

        boolean told = doInstructionUnit(cache, logic, tutStatus, Integer.valueOf(1), false, htm);
        told = doInstructionUnit(cache, logic, tutStatus, Integer.valueOf(2), told, htm);
        told = doInstructionUnit(cache, logic, tutStatus, Integer.valueOf(3), told, htm);
        doInstructionUnit(cache, logic, tutStatus, Integer.valueOf(4), told, htm);
    }

    /**
     * Present the link to access the gateway exam, if configured.
     *
     * @param cache     the data cache
     * @param logic     the tutorial logic
     * @param tutStatus the student's status in the tutorial
     * @param htm       the {@code HtmlBuilder} to which to append the HTML
     * @throws SQLException if there is an error accessing the database
     */
    private static void doGatewayUnit(final Cache cache, final PrecalcTutorialSiteLogic logic,
                                      final PrecalcTutorialCourseStatus tutStatus, final HtmlBuilder htm)
            throws SQLException {

        htm.sDiv(null, "style='float:left;padding-right:5px;'");
        htm.addln("<img src='/images/stock-jump-to-32.png' alt=''/>");
        htm.eDiv();
        htm.sH(3).add("<span class='green' style='position:relative;top:3px;'>Skills Review</span>").eH(3);
        htm.div("clear");

        final String courseId = tutStatus.getCourse().course;

        final SystemData systemData = cache.getSystemData();

        final RawExam revExam = systemData.getActiveExamByCourseUnitType(courseId, Integer.valueOf(0), "R");
        final String version;
        if (RawRecordConstants.M1170.equals(courseId) && !logic.hasPlacedInto117()) {
            version = "7TELM";
        } else if (revExam == null) {
            version = null;
        } else {
            version = revExam.version;
        }

        if (logic.hasTutorAccess()) {
            htm.addln("<div class='green' style='padding-left:37px;'>",
                    "Based on your course status, you are not required to pass the Skills Review Exam. ",
                    "However, you may practice the exam.</div>");

            htm.addln("<form method='get' action='run_review.html'>");
            htm.sDiv("exambtnlow");
            htm.addln("<input type='hidden' name='course' value='", courseId, "'/>");
            htm.addln("<input type='hidden' name='exam' value='", version, "'/>");
            htm.addln("<input type='hidden' name='mode' value='practice'/>");
            htm.addln("<input class='btn' type='submit' value='Practice Skills Review Exam'/> &nbsp;");
            htm.eDiv(); // exambtnlow
            htm.addln("</form>");
        } else {
            final boolean attemptedSr = tutStatus.getWhenAttemptedSR() != null;

            if (attemptedSr) {
                // Exam has been attempted, show the review materials links
                if ("7TELM".equals(version)) {
                    doElmLessons(htm);
                } else {
                    doUnitLessons(tutStatus, Integer.valueOf(0), htm, true, true);
                }

                if (version != null) {
                    htm.addln("<form method='get' action='run_review.html'>");
                    htm.sDiv("exambtn");
                    htm.addln("<input type='hidden' name='course' value='", courseId, "'/>");
                    htm.addln("<input type='hidden' name='exam' value='", version, "'/>");
                    htm.addln("<input class='btn' type='submit' value='Skills Review Exam'/> &nbsp;");

                    final boolean passedSr = tutStatus.getWhenPassedSR() != null;
                    if (passedSr) {
                        htm.addln("<img src='/images/check.png' alt=''/>");
                        htm.addln("<span class='green'>Passed</span>");
                    } else {
                        htm.addln("<span class='red'>Not Yet Passed</span>");
                    }

                    htm.eDiv(); // exambtn
                    htm.addln("</form>");
                }
            } else if (version != null) {
                htm.addln("<form method='get' action='run_review.html'>");
                htm.sDiv("exambtnlow");
                htm.addln("<input type='hidden' name='course' value='", courseId, "'/>");
                htm.addln("<input type='hidden' name='exam' value='", version, "'/>");
                htm.addln("<input class='btn' type='submit' value='Skills Review Exam'/> &nbsp;");
                htm.addln("<span class='red'>Not Yet Attempted</span>");
                htm.eDiv(); // exambtnlow
                htm.addln("</form>");
            }
        }

        htm.hr();
    }

    /**
     * Present a unit of instruction.
     *
     * @param cache     the data cache
     * @param logic     the course site logic
     * @param tutStatus the student's status in the tutorial
     * @param unit      the unit model
     * @param told      {@code true} if the user has already been told why they cannot proceed
     * @param htm       the {@code HtmlBuilder} to which to append the HTML
     * @return the new value for {@code told}
     * @throws SQLException if there is an error accessing the database
     */
    private static boolean doInstructionUnit(final Cache cache, final PrecalcTutorialSiteLogic logic,
                                             final PrecalcTutorialCourseStatus tutStatus, final Integer unit,
                                             final boolean told, final HtmlBuilder htm) throws SQLException {

        doUnitHeading(tutStatus, unit, htm);

        // Show the links to the lessons in the unit, with student's status as needed
        final boolean enabled = logic.hasTutorAccess() || tutStatus.getWhenPassedSR() != null;
        final boolean newTold = doUnitLessons(tutStatus, unit, htm, told, enabled);

        doUnitReviewExam(cache, logic, tutStatus, unit, htm);

//        if (unit.intValue() == 4) {
//            doPUUnitExam(cache, tutStatus, htm);
//        }

        htm.hr();

        return newTold;
    }

    /**
     * Present the header in the course outline for a unit.
     *
     * @param tutStatus the student's status in the tutorial
     * @param unit      the unit number
     * @param htm       the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doUnitHeading(final PrecalcTutorialCourseStatus tutStatus, final Integer unit,
                                      final HtmlBuilder htm) {

        final RawCunit courseUnit = tutStatus.getCourseUnit(unit);

        htm.sDiv(null, "style='float:left;padding-right:5px;'")
                .addln(" <img src='/images/stock-jump-to-32.png' alt=''/>").eDiv();

        htm.sDiv();
        htm.addln(" <a name='unit", unit, "'></a>");
        htm.sH(3).add("<span class='green' style='position:relative;top:3px;'>");

        if (courseUnit.unit != null) {
            htm.add(courseUnit.unit, ": ");
        }
        if (courseUnit.unitDesc != null) {
            htm.add(courseUnit.unitDesc);
        }
        htm.eSpan().eH(3).eDiv();

        htm.div("gap");
    }

    /**
     * Present the list of lessons in a unit.
     *
     * @param tutStatus the student's status in the tutorial
     * @param unit      the unit
     * @param htm       the {@code HtmlBuilder} to which to append the HTML
     * @param told      {@code true} if user has already been told why he can't move on
     * @param enabled   {@code true} if the link should be enabled
     * @return the new value for the {@code told} input
     */
    private static boolean doUnitLessons(final PrecalcTutorialCourseStatus tutStatus,
                                         final Integer unit, final HtmlBuilder htm, final boolean told,
                                         final boolean enabled) {

        boolean newTold = told;

        htm.sDiv("indent33");
        htm.sTable();

        final Map<Integer, RawCuobjective> objectives = tutStatus.getUnitObjectives(unit);
        final Map<Integer, RawLesson> lessons = tutStatus.getUnitLessons(unit);

        for (final Map.Entry<Integer, RawCuobjective> entry : objectives.entrySet()) {

            final RawCuobjective objective = entry.getValue();
            final RawLesson lesson = lessons.get(entry.getKey());

            if (objective == null || lesson == null) {
                continue;
            }

            final Integer seqNum = objective.objective;
            final String lessNum = objective.lessonNbr;

            htm.sTr();

            if (enabled) {
                htm.add("<td class='open' style='text-align:right;font-family: factoria-medium,sans-serif;'>");
                if (lessNum != null && unit.intValue() > 0) {
                    htm.add(lessNum, ":&nbsp;");
                }
                htm.add("</td><td style='white-space:nowrap;'>");
                htm.add("<a class='linkbtn' href='lesson.html?course=", tutStatus.getCourse().course, "&unit=", unit,
                        "&lesson=", seqNum);
                htm.add("'>");
                htm.addln(lesson.descr, "</a> &nbsp; </td>");
            } else {
                htm.add("<td class='open dim' style='text-align:right;font-family: factoria-medium,sans-serif;'>");
                if (lessNum != null) {
                    htm.add(lessNum, ":&nbsp;");
                }
                htm.add("</td><td class='dim' style='white-space:nowrap;'>");
                htm.addln(lesson.descr, " &nbsp; </td>");

                if (!newTold) {
                    htm.addln("<td class='red'>Skills Review Exam not yet passed.</td>");
                    newTold = true;
                }
            }

            htm.eTr();
        }

        htm.eTable();
        htm.eDiv(); // indent33

        return newTold;
    }

    /**
     * Emits links for the ELM Tutorial review materials.
     *
     * @param htm the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doElmLessons(final HtmlBuilder htm) {

        htm.sDiv("indent33");
        htm.sTable();

        htm.sTr();
        htm.add("<td class='open' style='text-align:right;font-family: factoria-medium,sans-serif;'></td>",
                "<td style='white-space:nowrap;'><a class='linkbtn' href='elm_lesson.html?unit=1'>",
                "Skills Review Materials - Part 1</a> &nbsp; </td>");
        htm.eTr();

        htm.sTr();
        htm.add("<td class='open' style='text-align:right;font-family: factoria-medium,sans-serif;'></td>",
                "<td style='white-space:nowrap;'><a class='linkbtn' href='elm_lesson.html?unit=2'>",
                "Skills Review Materials - Part 2</a> &nbsp; </td>");
        htm.eTr();

        htm.sTr();
        htm.add("<td class='open' style='text-align:right;font-family: factoria-medium,sans-serif;'></td>",
                "<td style='white-space:nowrap;'><a class='linkbtn' href='elm_lesson.html?unit=3'>",
                "Skills Review Materials - Part 3</a> &nbsp; </td>");
        htm.eTr();

        htm.sTr();
        htm.add("<td class='open' style='text-align:right;font-family: factoria-medium,sans-serif;'></td>",
                "<td style='white-space:nowrap;'><a class='linkbtn' href='elm_lesson.html?&unit=4'>",
                "Skills Review Materials - Part 4</a> &nbsp; </td>");
        htm.eTr();

        htm.eTable();
        htm.eDiv(); // indent33
    }

    /**
     * Present the button for the unit review exam along with status.
     *
     * @param cache     the data cache
     * @param logic     the tutorial logic
     * @param tutStatus the student's status in the tutorial
     * @param unit      the unit
     * @param htm       the {@code HtmlBuilder} to which to append the HTML
     * @throws SQLException if there is an error accessing the database
     */
    private static void doUnitReviewExam(final Cache cache, final PrecalcTutorialSiteLogic logic,
                                         final PrecalcTutorialCourseStatus tutStatus,
                                         final Integer unit, final HtmlBuilder htm) throws SQLException {

        final String courseId = tutStatus.getCourse().course;

        final SystemData systemData = cache.getSystemData();

        final RawExam reviewExam = systemData.getActiveExamByCourseUnitType(courseId, unit, "R");

        String label;
        final String examId;
        if (reviewExam == null) {
            label = null;
            examId = null;
        } else {
            label = reviewExam.buttonLabel;
            examId = reviewExam.version;
        }

        if (examId != null) {
            htm.addln("<form method='get' action='run_review.html'>");
            htm.sDiv("exambtn");

            final boolean reviewAvail;
            final String reviewReason;
            final int unitValue = unit.intValue();
            if (logic.hasTutorAccess()) {
                reviewAvail = true;
                reviewReason = CoreConstants.EMPTY;
                label = "Practice " + label;
            } else if (unitValue == 1) {
                reviewAvail = tutStatus.getWhenPassedSR() != null;
                reviewReason = reviewAvail ? CoreConstants.EMPTY : "Skills Review Exam not yet passed";
            } else {
                final Integer priorUnit = Integer.valueOf(unitValue - 1);
                reviewAvail = tutStatus.getWhenPassedRE(priorUnit) != null;
                reviewReason = reviewAvail ? CoreConstants.EMPTY
                        : ("Unit " + priorUnit + " Review Exam not yet passed");
            }

            if (reviewAvail) {
                htm.addln(" <input type='hidden' name='course' value='", tutStatus.getCourse().course, "'/>");
                htm.addln(" <input type='hidden' name='exam' value='", examId, "'/>");
                htm.addln(" <input class='btn' type='submit' value='", label, "'/> &nbsp;");

                if (logic.hasTutorAccess()) {
                    htm.addln(" <input type='hidden' name='mode' value='practice'/>");
                } else {
                    final boolean passedReview = tutStatus.getWhenPassedRE(unit) != null;
                    if (passedReview) {
                        htm.addln(" <img src='/images/check.png' alt=''/>");
                        htm.addln(" <span class='green'>Passed</span>").br();
                    } else {
                        final boolean attemptedReview = tutStatus.getWhenAttemptedRE(unit) != null;
                        if (attemptedReview) {
                            htm.addln(" <span class='red'>Not yet passed</span>").br();
                        } else {
                            htm.addln(" <span class='red'>Not yet attempted</span>").br();
                        }
                    }
                }

                htm.eDiv(); // exambtn
                htm.addln("</form>");

                if (unit.intValue() == 4) {
                    if (tutStatus.isEligibleForProctored()) {
                        htm.div("vgap");
                        htm.sDiv("indent22");
                        htm.addln(" You are eligible to take the <b>Precalculus Tutorial Exam</b>");
                        htm.addln(" in the Precalculus Center.<br/>");
                        htm.addln(" &nbsp; &nbsp;");
                        htm.addln(" <a class='smallbtn' href='instructions_precalc_tc.html?course=",
                                courseId.replace(CoreConstants.SPC, "%20"), "'>",
                                "Tell me how to take the Precalculus exam in the Precalculus Center...</a>");
                        htm.eDiv(); // indent22

                        htm.div("vgap");
                        htm.sDiv("indent22");
                        htm.addln(" You are also eligible to take the <b>Precalculus Tutorial Exam</b>");
                        htm.addln(" through the ProctorU proctoring service.<br/>");
                        htm.addln(" &nbsp; &nbsp;");
                        htm.addln(" <a class='smallbtn' href='instructions_precalc_pu.html?course=",
                                courseId.replace(CoreConstants.SPC, "%20"), "'>",
                                "Tell me how to take the Precalculus exam using ProctorU...</a>");
                        htm.eDiv(); // indent22
                    } else if (tutStatus.isRE4RetakeNeeded()) {
                        htm.div("vgap");
                        htm.sDiv("indent22");
                        htm.addln(" In order to attempt the Precalculus Tutorial Exam again, you will need to ",
                                "retake and pass the Unit 4 Review Exam.");
                        htm.eDiv(); // indent22
                    }
                }
            } else {
                htm.add(" <input class='btn' type='submit' disabled='disabled' value='", label, "'/> &nbsp; ");
                htm.add("<span class='red'>", reviewReason, "</span>").br();
                htm.eDiv(); // exambtn
                htm.addln("</form>");
            }
        }
    }

//    /**
//     * Show a link to ProctorU and a button to go to the proctor login screen to take the exam.
//     *
//     * @param cache     the data cache
//     * @param tutStatus the student's status in the tutorial
//     * @param htm       the {@code HtmlBuilder} to which to append the HTML
//     * @throws SQLException if there is an error accessing the database
//     */
//    private static void doPUUnitExam(final Cache cache, final PrecalcTutorialCourseStatus tutStatus,
//                                     final HtmlBuilder htm) throws SQLException {
//
//        final String courseId = tutStatus.getCourse().course;
//        final boolean unitAvail = tutStatus.isEligibleForProctored();
//
//        final SystemData systemData = cache.getSystemData();
//
//        final RawExam unitExam = systemData.getActiveExamByCourseUnitType(courseId, Integer.valueOf(4), "U");
//
//        final String version = unitExam == null ? null : unitExam.version;
//
//        if (version != null && unitAvail) {
//            htm.div("vgap");
//            htm.sDiv("indent22");
//            htm.addln("You may take the proctored Precalculus Tutorial exam using the ProctorU Proctoring service. ",
//                    "To schedule an exam through ProctorU, please visit the ",
//                    "<a class='ulink' target='_blank' href='https://go.proctoru.com/'>ProctorU web site</a>.");
//            htm.eDiv(); // indent22
//
//            htm.div("vgap");
//            htm.sDiv("indent22");
//            htm.addln(" When your proctor has verified your identity, click the button below to begin the exam.");
//
//            htm.addln("<form method='get' action='proctor_login.html'>");
//            htm.sDiv("exambtn");
//
//            htm.addln(" <input type='hidden' name='course' value='", tutStatus.getCourse().course, "'/>");
//            htm.addln(" <input type='hidden' name='exam' value='", version, "'/>");
//            htm.addln(" <input class='btn' type='submit' value='Proctored Precalculus Tutorial Exam'/> &nbsp;");
//
//            if (tutStatus.hasAttemptedProctored()) {
//                htm.addln(" <span class='red'>Not Yet Passed</span>").br();
//            } else {
//                htm.addln(" <span class='red'>Not Yet Attempted</span>").br();
//            }
//
//            htm.addln();
//            htm.eDiv(); // exambtn
//            htm.addln("</form>");
//
//            htm.eDiv(); // indent22
//        }
//    }
}
