package dev.mathops.web.host.testing.adminsys.genadmin.student;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.schema.legacy.RawCsection;
import dev.mathops.db.schema.legacy.RawCunit;
import dev.mathops.db.schema.legacy.RawCusection;
import dev.mathops.db.schema.legacy.RawExam;
import dev.mathops.db.schema.legacy.RawStcourse;
import dev.mathops.db.schema.legacy.RawStudent;
import dev.mathops.db.rec.TermRec;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.servlet.StudentCourseScores;
import dev.mathops.session.sitelogic.servlet.StudentCourseStatus;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.host.testing.adminsys.AdminSite;
import dev.mathops.web.host.testing.adminsys.genadmin.EAdminTopic;
import dev.mathops.web.host.testing.adminsys.genadmin.GenAdminPage;
import dev.mathops.web.host.testing.adminsys.genadmin.PageError;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * A page that shows the student's current status in all courses in which they are enrolled.
 */
public enum PageStudentCourseStatus {
    ;

    /**
     * Shows the student course status page (the student ID must be available in a request parameter named "stu").
     *
     * @param cache   the data cache
     * @param site    the site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final AdminSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String studentId = req.getParameter("stu");

        if (AbstractSite.isParamInvalid(studentId)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  studentId='", studentId, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else if (studentId == null) {
            PageError.doGet(cache, site, req, resp, session, "Student not found.");
        } else {
            final RawStudent student = RawStudentLogic.query(cache, studentId, false);

            if (student == null) {
                PageError.doGet(cache, site, req, resp, session, "Student not found.");
            } else {
                emitPageContent(cache, site, req, resp, session, student);
            }
        }
    }

    /**
     * Shows the student status page for a provided student.
     *
     * @param cache   the data cache
     * @param site    the site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @param student the student for which to present information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitPageContent(final Cache cache, final AdminSite site,
                                        final ServletRequest req, final HttpServletResponse resp,
                                        final ImmutableSessionInfo session, final RawStudent student)
            throws IOException, SQLException {

        final HtmlBuilder htm = GenAdminPage.startGenAdminPage(cache, site, session, true);

        GenAdminPage.emitNavBlock(EAdminTopic.STUDENT_STATUS, htm);

        htm.sP("studentname").add("<strong class='largeish'>", student.getScreenName(), "</strong> (", student.stuId,
                ") &nbsp; <a class='ulink' href='student.html'>Clear</a>").eP();

        htm.addln("<nav class='menu'>");

        menuButton(htm, false, student.stuId, EAdminStudentCommand.STUDENT_INFO);
        menuButton(htm, false, student.stuId, EAdminStudentCommand.PLACEMENT);
        menuButton(htm, true, student.stuId, EAdminStudentCommand.REGISTRATIONS);
        menuButton(htm, false, student.stuId, EAdminStudentCommand.ACTIVITY);
        menuButton(htm, false, student.stuId, EAdminStudentCommand.MATH_PLAN);

        htm.add("</nav>");

        htm.addln("<main class='info'>");
        emitCourseStatus(cache, site, session, htm, student);
        htm.addln("</main>");

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

    /**
     * Starts a navigation button.
     *
     * @param htm       the {@code HtmlBuilder} to which to append
     * @param selected  true if the button is selected
     * @param studentId the student ID
     * @param cmd       the command
     */
    private static void menuButton(final HtmlBuilder htm, final boolean selected,
                                   final String studentId, final EAdminStudentCommand cmd) {

        htm.addln("<form action='", cmd.url, "' method='post'>");

        htm.addln("<input type='hidden' name='stu' value='", studentId, "'/>");

        htm.add("<button type='submit'");
        if (selected) {
            htm.add(" class='menu selected' disabled");
        } else {
            htm.add(" class='menu'");
        }
        htm.add('>').add(cmd.label).add("</button>");

        htm.addln("</form>");
    }

    /**
     * Emits the student's course status.
     *
     * @param cache   the data cache
     * @param site    the site
     * @param session the login session
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param student the student
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitCourseStatus(final Cache cache, final AdminSite site,
                                         final ImmutableSessionInfo session, final HtmlBuilder htm,
                                         final RawStudent student) throws SQLException {

        final TermRec active = cache.getSystemData().getActiveTerm();

        if (active == null) {
            htm.addln("ERROR: unable to query active term");
        } else {
            // This query returns Forfeit and placement credit rows, but not Dropped rows
            final List<RawStcourse> allPastAndCurrent =
                    RawStcourseLogic.queryByStudent(cache, student.stuId, true, false);
            Collections.sort(allPastAndCurrent);

            int numCurrent = 0;
            int numPast = 0;
            for (final RawStcourse reg : allPastAndCurrent) {
                if (reg.termKey.equals(active.term)) {
                    ++numCurrent;
                } else {
                    ++numPast;
                }
            }

            htm.sH(4).add(active.term.longString, " Registrations:").eH(4);

            if (numCurrent == 0) {
                htm.sP(null, "style='margin-left:20px;'")
                        .add("(student has no registrations this term)").eP();
            } else {
                final StudentCourseStatus stat = new StudentCourseStatus(site.getSite().profile);

                for (final RawStcourse reg : allPastAndCurrent) {
                    if (!reg.termKey.equals(active.term)) {
                        continue;
                    }

                    if ("OT".equals(reg.instrnType)
                            || "AP".equals(reg.instrnType)) {

                        htm.sP(null, "style='margin-left:20px;'");
                        htm.add("<strong>", reg.course.replace("M ", "MATH "),
                                "</strong> - [Placement Credit]");
                        htm.eP();
                    } else {
                        htm.add("<details>",
                                "<summary style='padding:2px 0 2px 6px;margin-top:6px;background:#ffffe4;",
                                "border:1px solid #888;'><strong>");
                        htm.add(reg.course.replace("M ", "MATH "), "</strong> (", reg.sect, ")");

                        final boolean inc = "Y".equals(reg.iInProgress);
                        if (inc) {
                            if (reg.iTermKey == null) {
                                htm.add(" - INCOMPLETE");
                            } else {
                                htm.add(" - INCOMPLETE from ", reg.iTermKey.longString);
                            }
                        }

                        boolean showProgress = false;
                        final String open = reg.openStatus;
                        final boolean compl = "Y".equals(reg.completed);

                        if ("G".equals(open)) {
                            htm.add(" - [<strong>Forfeit</strong>, but still counted for pace]");
                        } else if (compl) {
                            if ("Y".equals(open)) {
                                htm.add(" - [<strong>Completed</strong>, but still open]");
                                showProgress = true;
                            } else {
                                htm.add(" - [<strong>Completed</strong>]");
                            }
                        } else if ("Y".equals(open)) {
                            htm.add(" - [<strong>In Progress</strong>]");
                            showProgress = true;
                        } else {
                            htm.add(" - [", open == null
                                    ? "Not started" : open, "]");
                        }

                        htm.addln("</summary>");

                        htm.sDiv(null,
                                "style='background:#ffffe4;margin:0 0 20px 20px;line-height:140%;"
                                        + "padding:3px;border-width:0 1px 1px 1px;border-color:#888;"
                                        + "border-style:solid;font-size:13px;'");

                        htm.sP(null, "style='margin-top:6px;'");
                        htm.addln("Pace Order: ",
                                reg.paceOrder == null ? CoreConstants.DASH : reg.paceOrder, "<br/>");

                        final String pre = reg.prereqSatis;
                        htm.addln("Prerequisite Satisfied: ", pre == null ? "No"
                                : ("Y".equals(pre) || "P".equals(pre)) ? "Yes" : "No", "<br/>"); //$NON-NLS-5$

                        if (inc) {
                            htm.addln("Counted in pace? ", "Y".equals(reg.iCounted) ? "Yes" : "No", "<br/>");
                        }
                        htm.eP();

                        if (showProgress && stat.gatherData(cache, session, student.stuId,
                                reg.course, false, false)) {

                            emitCourseProgress(cache, htm, stat, reg, compl);
                            htm.div("vgap");
                            emitCourseDeadlines(htm, stat);
                        }

                        htm.eDiv().addln("</details>");
                    }
                }
            }

            htm.div("vgap2");
            htm.sH(4).add("Previous Registrations:").eH(4);
            htm.sP(null, "style='margin-left:24px;'");

            if (numPast == 0) {
                htm.add("(student has no registrations from earlier terms)");
            } else {
                for (final RawStcourse reg : allPastAndCurrent) {
                    if (reg.termKey.equals(active.term)) {
                        continue;
                    }

                    htm.add("<strong>", reg.course.replace("M ", "MATH "), "</strong> (", reg.sect, ") - ",
                            reg.termKey.longString);

                    final boolean compl = "Y".equals(reg.completed);

                    htm.add(" - [");
                    if (compl) {
                        htm.add("Completed with score ", reg.score, ", grade ", reg.courseGrade);
                    } else {
                        htm.add("Not completed, grade ", reg.courseGrade);
                    }
                    htm.addln("]<br/>");
                }
            }

            htm.eP();
        }
    }

    /**
     * Emits a table that shows a summary of the student's progress in the course.
     *
     * @param cache     the data cache
     * @param htm       the {@code HtmlBuilder} to which to append
     * @param stat      the course status data container
     * @param reg       the registration
     * @param completed true if course is completed
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitCourseProgress(final Cache cache, final HtmlBuilder htm, final StudentCourseStatus stat,
                                           final RawStcourse reg, final boolean completed) throws SQLException {

        final int maxUnit = stat.getMaxUnit();
        final RawCsection csect = stat.getCourseSection();

        final SystemData systemData = cache.getSystemData();

        // Show student progress in the class
        htm.sTable("report", "style='margin:0;line-height:1;'");

        // Top-level header for unit title
        htm.sTr();
        for (int unit = 0; unit <= maxUnit; ++unit) {
            final RawCunit cunit = stat.getCourseUnit(unit);
            final RawCusection cusect = stat.getCourseSectionUnit(unit);
            final String alt = (unit & 0x01) == 0x01 ? " class='alt'" : CoreConstants.EMPTY;

            if (cunit == null && cusect == null) {
                continue;
            }

            final int numLessons = stat.getNumLessons(unit);
            int numHw = 0;
            if (cunit != null) {
                for (int obj = 0; obj < numLessons; ++obj) {
                    if (systemData.getActiveAssignment(reg.course, Integer.valueOf(unit), Integer.valueOf(obj), "HW")
                            != null) {
                        ++numHw;
                    }
                }
            }

            if (cunit == null || "SR".equals(cunit.unitType)) {
                final RawExam sr = cunit == null ? null : systemData.getActiveExamByCourseUnitType(reg.course,
                        Integer.valueOf(unit), "R");
                final int cols = numHw + (sr == null ? 0 : 1);
                htm.add("<th ", alt, " colspan=" + cols + ">Skills Review").eTh();
            } else if ("INST".equals(cunit.unitType)) {
                final RawExam ur = systemData.getActiveExamByCourseUnitType(reg.course,
                        Integer.valueOf(unit), "R");
                final RawExam ue = systemData.getActiveExamByCourseUnitType(reg.course,
                        Integer.valueOf(unit), "U");

                final int cols = numHw + (ur == null ? 0 : 1) + (ue == null ? 0 : 1);

                htm.add("<th ", alt, " colspan=" + cols + ">Unit ", cunit.unit).eTh();

            } else if ("FIN".equals(cunit.unitType)) {

                final RawExam ue = systemData.getActiveExamByCourseUnitType(reg.course,
                        Integer.valueOf(unit), "F");

                final int cols = numHw + (ue == null ? 0 : 1);

                htm.add("<th class='special' colspan=" + cols + ">Final").eTh();
            }
        }

        final String maxAlt = (maxUnit & 0x01) == 0x01 ? " class='alt'" : CoreConstants.EMPTY;
        htm.add("<th ", maxAlt, ">Total").eTh();

        htm.eTr();

        // Second level with objectives
        htm.sTr();
        for (int unit = 0; unit <= maxUnit; ++unit) {
            final RawCunit cunit = stat.getCourseUnit(unit);
            final RawCusection cusect = stat.getCourseSectionUnit(unit);
            final String alt = (unit & 0x01) == 0x01 ? " class='alt'" : CoreConstants.EMPTY;

            if (cunit == null && cusect == null) {
                continue;
            }

            final int numLessons = stat.getNumLessons(unit);
            int numHw = 0;
            if (cunit != null) {
                for (int obj = 0; obj < numLessons; ++obj) {
                    if (systemData.getActiveAssignment(reg.course, Integer.valueOf(unit), Integer.valueOf(obj), "HW")
                            != null) {
                        htm.add("<th ", alt, ">H").eTh();
                        ++numHw;
                    }
                }
            }

            if (cunit == null || "SR".equals(cunit.unitType)) {

                if (systemData.getActiveExamByCourseUnitType(reg.course, Integer.valueOf(unit), "R") != null) {
                    htm.add("<th class='special'>UR").eTh();
                } else if (numHw == 0) {
                    htm.sTh().eTh();
                }
            } else if ("INST".equals(cunit.unitType)) {

                int count = 0;
                if (systemData.getActiveExamByCourseUnitType(reg.course, Integer.valueOf(unit), "R") != null) {
                    htm.add("<th class='special'>UR").eTh();
                    ++count;
                }

                if (systemData.getActiveExamByCourseUnitType(reg.course, Integer.valueOf(unit), "U") != null) {
                    htm.add("<th class='special'>UE").eTh();
                    ++count;
                }

                if (numHw + count == 0) {
                    htm.sTh().eTh();
                }

            } else if ("FIN".equals(cunit.unitType)) {

                int count = 0;
                if (systemData.getActiveExamByCourseUnitType(reg.course, Integer.valueOf(unit), "F") != null) {
                    htm.add("<th class='special'>FE").eTh();
                    ++count;
                }

                if (numHw + count == 0) {
                    htm.sTh().eTh();
                }
            }
        }
        htm.add("<th ", maxAlt, ">Score").eTh();
        htm.eTr();

        final StudentCourseScores scores = stat.getScores();

        // Student status
        htm.sTr();
        for (int unit = 0; unit <= maxUnit; ++unit) {
            final RawCunit cunit = stat.getCourseUnit(unit);
            final RawCusection cusect = stat.getCourseSectionUnit(unit);

            if (cunit == null && cusect == null) {
                continue;
            }

            final int numLessons = stat.getNumLessons(unit);
            int numHw = 0;
            if (cunit != null) {
                for (int obj = 0; obj < numLessons; ++obj) {
                    if (systemData.getActiveAssignment(reg.course, Integer.valueOf(unit), Integer.valueOf(obj), "HW")
                            != null) {
                        final String status = stat.getHomeworkStatus(unit, obj);
                        if ("Completed".equals(status) || "May Move On".equals(status)) {
                            htm.sTd().add("<img src='/images/check.png'/>").eTd();
                        } else {
                            htm.sTd().eTd();
                        }
                        ++numHw;
                    }
                }
            }

            if (cunit == null || "SR".equals(cunit.unitType)) {

                if ((systemData.getActiveExamByCourseUnitType(reg.course, Integer.valueOf(unit),
                        "R") != null) && stat.isReviewPassed(unit)) {
                    final boolean ontime = stat.isReviewPassedOnTime(unit);
                    final Integer pts = ontime ? cusect.rePointsOntime : null;
                    final int ptsInt = pts == null ? 0 : pts.intValue();

                    htm.sTd();
                    if (ontime) {
                        if (ptsInt == 0) {
                            htm.add("<img src='/images/check.png'/>");
                        } else {
                            htm.add("<img src='/images/check.png'/> <b>" + ptsInt + "</b>");
                        }
                    } else {
                        htm.add("<img src='/images/check.png'/> (LATE)");
                    }
                    htm.eTd();
                } else {
                    htm.sTd().eTd();
                }
            } else if ("INST".equals(cunit.unitType)) {

                int count = 0;
                if (systemData.getActiveExamByCourseUnitType(reg.course, Integer.valueOf(unit), "R") != null) {

                    if (stat.isReviewPassed(unit)) {
                        final boolean ontime = stat.isReviewPassedOnTime(unit);
                        final Integer pts = ontime ? cusect.rePointsOntime : null;
                        final int ptsInt = pts == null ? 0 : pts.intValue();

                        htm.sTd();
                        if (ontime) {
                            if (ptsInt == 0) {
                                htm.add("<img src='/images/check.png'/>");
                            } else {
                                htm.add("<img src='/images/check.png'/> <b>" + ptsInt + "</b>");
                            }
                        } else {
                            htm.add("<img src='/images/check.png'/> (LATE)");
                        }
                        htm.eTd();
                    } else {
                        htm.sTd().eTd();
                    }
                    ++count;
                }

                if ((systemData.getActiveExamByCourseUnitType(reg.course, Integer.valueOf(unit), "U") != null)) {

                    if (stat.isProctoredPassed(unit)) {
                        final boolean ontime = stat.isProctoredPassedOnTime(unit);
                        final int score = scores.getRawUnitExamScore(unit);

                        htm.sTd();
                        if (ontime) {
                            if (score == 0) {
                                htm.add("<img src='/images/check.png'/>");
                            } else {
                                htm.add("<img src='/images/check.png'/> <b>" + score + "</b>");
                            }
                        } else if (score == 0) {
                            htm.add("<img src='/images/check.png'/> (LATE)");
                        } else {
                            htm.add("<img src='/images/check.png'/> <b>" + score + "</b> (LATE)");

                        }
                        htm.eTd();

                    } else {
                        htm.sTd().eTd();
                    }
                    ++count;
                }

                if (numHw + count == 0) {
                    htm.sTd().eTd();
                }

            } else if ("FIN".equals(cunit.unitType)) {

                int count = 0;
                if (systemData.getActiveExamByCourseUnitType(reg.course, Integer.valueOf(unit),
                        "F") != null) {
                    if (stat.isProctoredPassed(unit)) {
                        final boolean ontime = stat.isProctoredPassedOnTime(unit);
                        final int score = scores.getRawUnitExamScore(unit);

                        htm.sTd();
                        if (ontime) {
                            if (score == 0) {
                                htm.add("<img src='/images/check.png'/>");
                            } else {
                                htm.add("<img src='/images/check.png'/> <b>" + score + "</b>");

                            }
                        } else if (score == 0) {
                            htm.add("<img src='/images/check.png'/> (LATE)");
                        } else {
                            htm.add("<img src='/images/check.png'/> <b>" + score + "</b> (LATE)");
                        }
                        htm.eTd();
                    } else {
                        htm.sTd().eTd();
                    }
                    ++count;
                }

                if (numHw + count == 0) {
                    htm.sTd().eTd();
                }
            }
        }

        final int total = scores.getTotalScore();

        String grade = null;
        if (completed && csect != null) {
            if (csect.aMinScore != null && total >= csect.aMinScore.intValue()) {
                grade = "A";
            } else if (csect.bMinScore != null && total >= csect.bMinScore.intValue()) {
                grade = "B";
            } else if (csect.cMinScore != null && total >= csect.cMinScore.intValue()) {
                grade = "C";
            } else if (csect.dMinScore != null && total >= csect.dMinScore.intValue()) {
                grade = "D";
            } else {
                grade = "U";
            }
        }

        htm.sTd().add("<b>" + total);
        if (grade != null) {
            htm.add("(", grade, ")");
        }
        htm.add("</b>").eTd();

        htm.eTr();

        htm.eTable();
    }

    /**
     * Emits a table that shows deadlines for the course (with overrides, if any).
     *
     * @param htm  the {@code HtmlBuilder} to which to append
     * @param stat the course status data container
     */
    private static void emitCourseDeadlines(final HtmlBuilder htm, final StudentCourseStatus stat) {

        // Show student progress in the class
        htm.sTable("report", "style='margin:0;line-height:1;'");

        final int maxUnit = stat.getMaxUnit();

        htm.sTr();
        htm.sTh().add("Milestone").eTh();
        htm.sTh().add("Deadline").eTh();
        htm.eTr();

        for (int i = 0; i <= maxUnit; ++i) {
            final RawCunit cunit = stat.getCourseUnit(i);
            final RawCusection cusect = stat.getCourseSectionUnit(i);

            if (cunit == null || cusect == null) {
                continue;
            }

            if ("INST".equals(cunit.unitType)) {
                final LocalDate reviewDeadline = stat.getReviewExamDeadline(i);
                if (reviewDeadline != null) {
                    htm.sTr();
                    htm.sTd().add("Unit " + i + " Review").eTd();
                    htm.sTd().add(TemporalUtils.FMT_WMDY.format(reviewDeadline)).eTd();
                    htm.eTr();
                }

                final LocalDate unitDeadline = stat.getUnitExamDeadline(i);
                if (unitDeadline != null) {
                    htm.sTr();
                    htm.sTd().add("Unit " + i + " Exam").eTd();
                    htm.sTd().add(TemporalUtils.FMT_WMDY.format(unitDeadline)).eTd();
                    htm.eTr();
                }
            } else if ("FIN".equals(cunit.unitType)) {
                final LocalDate reviewDeadline = stat.getReviewExamDeadline(i);
                if (reviewDeadline != null) {
                    htm.sTr();
                    htm.sTd().add("Final Review").eTd();
                    htm.sTd().add(TemporalUtils.FMT_WMDY.format(reviewDeadline)).eTd();
                    htm.eTr();
                }

                final LocalDate midtermDeadline = stat.getUnitExamDeadline(i);
                if (midtermDeadline != null) {
                    htm.sTr();
                    htm.sTd().add("Final Exam").eTd();
                    htm.sTd().add(TemporalUtils.FMT_WMDY.format(midtermDeadline)).eTd();
                    htm.eTr();
                }
            }
        }

        htm.eTable();
    }
}
