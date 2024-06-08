package dev.mathops.web.site.course;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.logic.WebViewData;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
import dev.mathops.session.sitelogic.data.SiteDataCfgCourse;
import dev.mathops.session.sitelogic.data.SiteDataCfgCourseStatus;
import dev.mathops.session.sitelogic.data.SiteDataCfgExamStatus;
import dev.mathops.session.sitelogic.data.SiteDataCourse;
import dev.mathops.session.sitelogic.data.SiteDataStatus;
import dev.mathops.session.sitelogic.servlet.StudentCourseStatus;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * This page shows the student's status in a standards-based course. This includes the mastery status of all standards
 * and activities, the point total, the grading scale, and a list of "what's next" items.
 */
enum PageCourseStatus {
    ;

    /**
     * Starts the page that shows the course outline with student progress.
     *
     * @param data    the web view data
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @param logic   the course site logic
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final WebViewData data, final CourseSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                      final CourseSiteLogic logic) throws IOException, SQLException {

        final String course = req.getParameter("course");

        if (AbstractSite.isParamInvalid(course)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  course='", course, "'");
            PageError.doGet(data, site, req, resp, session, "No course ID provided for status display");
        } else if (course == null) {
            PageError.doGet(data, site, req, resp, session, "No course ID provided for status display");
        } else {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, site.getTitle(), session, false,
                    Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false, true);

            htm.sDiv("menupanelu");
            CourseMenu.buildMenu(data, site, session, logic, htm);
            htm.sDiv("panelu");

            doStatus(data, site, session, logic, course, htm);

            htm.eDiv(); // panelu
            htm.eDiv(); // menupanelu

            final SystemData systemData = data.getSystemData();
            Page.endOrdinaryPage(systemData, site, htm, true);

            final byte[] bytes = htm.toString().getBytes(StandardCharsets.UTF_8);
            AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, bytes);
        }
    }

    /**
     * Creates the HTML status page.
     *
     * @param data     the web view data
     * @param site     the owning site
     * @param session  the user's login session information
     * @param logic    the course site logic
     * @param courseId the course for which to generate the status page
     * @param htm      the {@code HtmlBuilder} to which to append the HTML
     * @throws SQLException if there is an error accessing the database
     */
    private static void doStatus(final WebViewData data, final CourseSite site,
                                 final ImmutableSessionInfo session, final CourseSiteLogic logic, final String courseId,
                                 final HtmlBuilder htm) throws SQLException {

        final String userId = session.getEffectiveUserId();
        final StudentCourseStatus courseStatus = new StudentCourseStatus(site.getDbProfile());

        final StudentData studentData = data.getEffectiveUser();

        if (courseStatus.gatherData(studentData, session, userId, courseId, false, false)
                && courseStatus.getCourse().courseName != null) {

            final RawCsection csection = courseStatus.getCourseSection();
            final String section = csection.sect;

            htm.sH(2, "title");
            if ("Y".equals(courseStatus.getCourseSection().courseLabelShown)) {
                htm.add(courseStatus.getCourse().courseLabel);
                htm.add(": ");
            }
            htm.add(courseStatus.getCourse().courseName);
            if (section != null) {
                htm.br().add("<small>Section ", section, "</small>");
            }
            htm.eH(2);

            htm.sDiv("coursestatus");

            final RawStcourse reg = courseStatus.getStudentCourse();

            if ("F".equals(reg.courseGrade)) {
                htm.sP("red");
                htm.addln(" A progress report for this course is not available.  ", //
                        "Please contact the Precalculus Center at ",
                        "<a class='ulink2' href='mailto:precalc_math@colostate.edu'>precalc_math@colostate.edu",
                        "</a> with questions concerning your grade in this course.");
                htm.sP();
            } else {
                masteryContent(logic, reg, htm);
            }

            htm.eDiv();
        } else {
            htm.sP().add("FAILED TO GET COURSE DATA 1").br();
            if (courseStatus.getErrorText() != null) {
                htm.add(courseStatus.getErrorText());
            }
            htm.eP();
        }
    }

    /**
     * Shows student mastery status for all standards in the course.
     *
     * @param logic the course site logic
     * @param reg   the course registration
     * @param htm   the {@code HtmlBuilder} to which to append
     */
    private static void masteryContent(final CourseSiteLogic logic, final RawStcourse reg,
                                       final HtmlBuilder htm) {

        final String courseId = reg.course;
        final String sect = reg.sect;

        // Show incomplete status if this is an incomplete.
        if (reg.iTermKey != null) {
            final TermRec incTerm = logic.data.registrationData.getRegistrationTerm(courseId, sect);

            htm.sDiv("indent11");
            htm.sP("red");
            htm.add("<strong>This course is an incomplete from the ", incTerm.term.longString,
                    " semester.</strong>");
            htm.eP();
            htm.eDiv();
        }

        final SiteDataCourse courseData = logic.data.courseData;
        final SiteDataCfgCourse cfgCourse = courseData.getCourse(courseId, sect);
        final Integer maxUnit = courseData.getMaxUnit(courseId);
        int numStandards = 0;

        if (cfgCourse != null && maxUnit != null) {
            final SiteDataStatus status = logic.data.statusData;

            htm.sDiv("indent22");
            htm.addln("<details open>");
            htm.add("<summary style='font-family:factoria-medium,sans-serif;",
                    "font-weight:300;font-size:1.17rem;color:#196F43;margin-bottom:.4em;'>",
                    "Completion of Course Standards", //
                    "</summary>");

            htm.sTable("scoretable");

            // Show the first 5 chapters
            for (int i = 1; i <= 5; ++i) {
                final int startIndex = (i - 1) << 2;
                final String stdNumber = Integer.toString(i);

                htm.sTr();
                htm.sTd("scoreh2")
                        .add("Unit&nbsp;", Integer.toString(i), ":").eTd();

                final SiteDataCfgExamStatus u1Status = status.getExamStatus(courseId, //
                        Integer.valueOf(startIndex + 1), "UE");

                final SiteDataCfgExamStatus u2Status = status.getExamStatus(courseId, //
                        Integer.valueOf(startIndex + 2), "UE");

                final SiteDataCfgExamStatus u3Status = status.getExamStatus(courseId, //
                        Integer.valueOf(startIndex + 3), "UE");

                numStandards = emitStandardStatusTableCell(htm, stdNumber + ".1",
                        u1Status, numStandards);
                numStandards = emitStandardStatusTableCell(htm, stdNumber + ".2",
                        u2Status, numStandards);
                numStandards = emitStandardStatusTableCell(htm, stdNumber + ".3",
                        u3Status, numStandards);
                htm.eTr();
            }

            // Synthesis activities after chapter 5
            final SiteDataCfgExamStatus activity1Status = status.getExamStatus(courseId, //
                    Integer.valueOf(41), "UE");

            htm.sTr();
            htm.sTd("scoreh").add("Activity&nbsp;1:").eTd();
            emitActivityStatusTableCell(htm, activity1Status);
            htm.eTr();

            // Show the second 5 chapters
            for (int i = 6; i <= 10; ++i) {
                final int startIndex = (i - 1) << 2;
                final String stdNumber = Integer.toString(i);

                htm.sTr();
                htm.sTd("scoreh2")
                        .add("Unit&nbsp;", Integer.toString(i), ":").eTd();

                final SiteDataCfgExamStatus u1Status = status.getExamStatus(courseId, //
                        Integer.valueOf(startIndex + 1), "UE");

                final SiteDataCfgExamStatus u2Status = status.getExamStatus(courseId, //
                        Integer.valueOf(startIndex + 2), "UE");

                final SiteDataCfgExamStatus u3Status = status.getExamStatus(courseId, //
                        Integer.valueOf(startIndex + 3), "UE");

                numStandards = emitStandardStatusTableCell(htm, stdNumber + ".1",
                        u1Status, numStandards);
                numStandards = emitStandardStatusTableCell(htm, stdNumber + ".2",
                        u2Status, numStandards);
                numStandards = emitStandardStatusTableCell(htm, stdNumber + ".3",
                        u3Status, numStandards);
                htm.eTr();
            }

            // Synthesis activities after chapter 10
            final SiteDataCfgExamStatus activity2Status = status.getExamStatus(courseId, //
                    Integer.valueOf(42), "UE");

            htm.sTr();
            htm.sTd("scoreh").add("Activity&nbsp;2:").eTd();
            emitActivityStatusTableCell(htm, activity2Status);
            htm.eTr();

            // Show number of standards completed and total score

            final SiteDataCfgCourseStatus courseStatus = status.getCourseStatus(courseId);
            final int total = courseStatus == null ? 0 : courseStatus.totalScore;

            htm.sTr("totals");
            htm.sTd("scoreh", "colspan='2'").add(
                    "<strong>Standards Completed: ", Integer.toString(numStandards),
                    "</strong>").eTd();

            htm.sTd("scoreh", "colspan='2'").add(
                    "<strong>Total Points: ", Integer.toString(total),
                    "</strong>").eTd();
            htm.eTr();

            htm.eTable();

            //

            // Show the grading scale if the section indicates to do so.
            final RawCsection csection = cfgCourse.courseSection;
            if ("Y".equals(csection.displayGradeScale)) {

                final int maxPossible;
                if (csection.aMinScore.intValue() == 65) {
                    maxPossible = 72;
                } else if (csection.aMinScore.intValue() == 43) {
                    maxPossible = 48;
                } else if (csection.aMinScore.intValue() == 153) {
                    maxPossible = 170;
                } else {
                    maxPossible = Math.round(csection.aMinScore.floatValue() * 0.9f);
                }

                htm.div("vgap");

                htm.add("<strong>Grading Scale</strong>: &nbsp;");

                htm.add(csection.aMinScore, "&nbsp;-&nbsp;",
                        Integer.toString(maxPossible), "&nbsp;=&nbsp;A,&nbsp ");

                htm.add(csection.bMinScore, "&nbsp;-&nbsp;",
                        Integer.toString(csection.aMinScore.intValue() - 1), //
                        "&nbsp;=&nbsp;B,&nbsp ");

                htm.add(csection.cMinScore, "&nbsp;-&nbsp;",
                        Integer.toString(csection.bMinScore.intValue() - 1), //
                        "&nbsp;=&nbsp;C,&nbsp ");

                if (csection.dMinScore != null) {
                    htm.add(csection.dMinScore, "&nbsp;-&nbsp;",
                            Integer.toString(csection.cMinScore.intValue() - 1), //
                            "&nbsp;=&nbsp;D,&nbsp ");
                }

                htm.add("Fewer&nbsp;than&nbsp;24&nbsp;standards&nbsp;completed&nbsp;=&nbsp;U");
            }

            if (RawRecordConstants.M124.equals(courseId)
                    || RawRecordConstants.M126.equals(courseId)) {
                htm.div("vgap");

                htm.sDiv("blue");
                htm.addln("Please note: The prerequisites for <b>MATH 156</b> ",
                        "(Mathematics for Computational Science I) and <b>MATH 160</b> ",
                        "(Calculus for Physical Scientists I) <b>require</b> a grade of ",
                        "<b>B or higher</b> in both MATH 124 and MATH 126.");
                htm.eDiv();
            }

            // Show incomplete deadline date if applicable
            if (reg.iDeadlineDt != null //
                    && !"Y".equals(reg.iCounted)
                    && "Y".equals(reg.iInProgress)) {

                htm.div("vgap");

                htm.sDiv("red");
                htm.addln(" <strong>Course Deadline:</strong><br/>");
                htm.addln(" &nbsp; &nbsp; You have until ",
                        TemporalUtils.FMT_MDY.format(reg.iDeadlineDt), " to complete the course.");
                htm.eDiv();
            }

            htm.div("vgap");
            htm.addln("</details>");
            htm.eDiv();
        }
    }

    /**
     * Emits a table cell for the status table for a single standard.
     *
     * @param htm          the {@code HtmlBuilder} to which to append
     * @param lbl          the standard label
     * @param status       the exam status data
     * @param numStandards the number of standards that had been mastered before this standard
     * @return the number of standards that are mastered ({@code numStandards} plus either 0 or 1 depending on whether
     *         the current standard has been mastered)
     */
    private static int emitStandardStatusTableCell(final HtmlBuilder htm, final String lbl,
                                                   final SiteDataCfgExamStatus status, final int numStandards) {

        int result = numStandards;

        htm.sTd("scored")
                .add("<span style='display:inline-block;width:30px;text-align:right;'>",
                        lbl, "</span>");

        if (status != null) {
            if (status.highestPassingScore > 0) {
                htm.add("<input type='checkbox' checked onclick='return false;'/>");
                if (status.passedOnTime) {
                    htm.add("<input type='text' size='3' value='5 pts' readonly/>");
                } else {
                    htm.add("<input type='text' size='3' value='4 pts' readonly/>");
                }
                ++result;
            } else {
                if (status.eligible) {
                    htm.add("<input type='checkbox' onclick='return false;'/>");
                } else {
                    htm.add("<input type='checkbox' disabled/>");
                }
                htm.add("<input type='text' size='3' disabled/>");
            }
        } else {
            htm.add("<input type='checkbox' disabled/>");
            htm.add("<input type='text' size='3' disabled/>");
        }
        htm.eTd();

        return result;
    }

    /**
     * Emits a table cell for the status table for a single synthesis activity.
     *
     * @param htm    the {@code HtmlBuilder} to which to append
     * @param status the exam status data
     */
    private static void emitActivityStatusTableCell(final HtmlBuilder htm,
                                                    final SiteDataCfgExamStatus status) {

        htm.sTd("scored", "colspan='3'")
                .add("<span style='display:inline-block;width:30px;'></span>");

        if (status != null) {
            if (status.highestPassingScore > 0) {
                htm.add("<input type='checkbox' checked onclick='return false;'/>");
                if (status.passedOnTime) {
                    htm.add("<input type='text' size='3' value='5 pts' readonly/>");
                } else {
                    htm.add("<input type='text' size='3' value='4 pts' readonly/>");
                }
            } else {
                if (status.eligible) {
                    htm.add("<input type='checkbox' onclick='return false;'/>");
                } else {
                    htm.add("<input type='checkbox' disabled/>");
                }
                htm.add("<input type='text' size='3' disabled/>");
            }
        } else {
            htm.add("<input type='checkbox' disabled/>");
            htm.add("<input type='text' size='3' disabled/>");
        }
        htm.eTd();
    }
}
