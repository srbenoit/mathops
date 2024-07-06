package dev.mathops.web.site.admin.office;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.enums.EExamStructure;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.old.rawlogic.RawCsectionLogic;
import dev.mathops.db.old.rawlogic.RawExamLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawSttermLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawCunit;
import dev.mathops.db.old.rawrecord.RawCusection;
import dev.mathops.db.old.rawrecord.RawExam;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.rec.MasteryAttemptRec;
import dev.mathops.db.old.rec.MasteryExamRec;
import dev.mathops.db.old.rec.StandardMilestoneRec;
import dev.mathops.db.old.rec.StudentStandardMilestoneRec;
import dev.mathops.db.old.reclogic.AssignmentLogic;
import dev.mathops.db.old.reclogic.MasteryAttemptLogic;
import dev.mathops.db.old.reclogic.MasteryExamLogic;
import dev.mathops.db.old.reclogic.StandardMilestoneLogic;
import dev.mathops.db.old.reclogic.StudentStandardMilestoneLogic;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.servlet.StudentCourseScores;
import dev.mathops.session.sitelogic.servlet.StudentCourseStatus;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

/**
 * Pages that displays summary information about a single selected student, with buttons to go to detail-oriented
 * pages.
 */
enum PageStudentInfo {
    ;

    /**
     * Shows the student information page (the student ID must be available in a request parameter named "stu").
     *
     * @param cache   the data cache
     * @param site    the site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final AdminSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String studentId = req.getParameter("stu");

        if (AbstractSite.isParamInvalid(studentId)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  studentId='", studentId, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else if (studentId == null) {
            PageHome.doGet(cache, site, req, resp, session, "Student not found.");
        } else {
            final RawStudent student = RawStudentLogic.query(cache, studentId, false);

            if (student == null) {
                PageHome.doGet(cache, site, req, resp, session, "Student not found.");
            } else {
                doStudentInfoPage(cache, site, req, resp, session, student);
            }
        }
    }

    /**
     * Shows the student information page for a provided student.
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
    static void doStudentInfoPage(final Cache cache, final AdminSite site,
                                  final ServletRequest req, final HttpServletResponse resp,
                                  final ImmutableSessionInfo session, final RawStudent student)
            throws IOException, SQLException {

        final HtmlBuilder htm = OfficePage.startOfficePage(cache, site, session, true);

        htm.sP("studentname").add("<strong>", student.getScreenName(), "</strong> &nbsp; <strong><code>",
                student.stuId, "</code></strong>").eP();

        if (session.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {

            htm.sDiv("narrowstack");
            htm.addln("<form method='get' action='student_info.html'>");
            htm.addln("<input type='hidden' name='stu' value='", student.stuId, "'/>");
            htm.addln("<button class='navlit'>Registrations</button>");
            htm.addln("</form>");

            htm.addln("<form method='get' action='student_schedule.html'>");
            htm.addln("<input type='hidden' name='stu' value='", student.stuId, "'/>");
            htm.addln("<button class='nav'>Schedule</button>");
            htm.addln("</form>");

            htm.addln("<form method='get' action='student_activity.html'>");
            htm.addln("<input type='hidden' name='stu' value='", student.stuId, "'/>");
            htm.addln("<button class='nav'>Activity</button>");
            htm.addln("</form>");

            htm.addln("<form method='get' action='student_exams.html'>");
            htm.addln("<input type='hidden' name='stu' value='", student.stuId, "'/>");
            htm.addln("<button class='nav'>Exams</button>");
            htm.addln("</form>");

            htm.addln("<form method='get' action='student_placement.html'>");
            htm.addln("<input type='hidden' name='stu' value='", student.stuId, "'/>");
            htm.addln("<button class='nav'>Placement</button>");
            htm.addln("</form>");

            htm.eDiv(); // narrowstack

            htm.sDiv("detail");
            emitStudentInfo(cache, site, session, htm, student);
            htm.eDiv(); // detail
        }

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Emits general student information and status.
     *
     * @param cache   the data cache
     * @param site    the site
     * @param session the login session
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param student the student record
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitStudentInfo(final Cache cache, final AdminSite site,
                                        final ImmutableSessionInfo session, final HtmlBuilder htm,
                                        final RawStudent student) throws SQLException {

        final TermRec active = cache.getSystemData().getActiveTerm();

        if (active == null) {
            htm.addln("ERROR: unable to query active term");
        } else {
            // This query returns Forfeit and placement credit rows, but not Dropped rows
            final List<RawStcourse> allPastAndCurrent = RawStcourseLogic.queryByStudent(cache, student.stuId, true,
                    true);
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
                final StudentCourseStatus stat = new StudentCourseStatus(site.getDbProfile());

                for (final RawStcourse reg : allPastAndCurrent) {
                    if (!reg.termKey.equals(active.term)) {
                        continue;
                    }

                    if ("OT".equals(reg.instrnType) || "AP".equals(reg.instrnType)) {

                        htm.sP(null, "style='margin-left:20px;'");
                        htm.add("<strong>", reg.course.replace("M ", "MATH "), "</strong> - [Placement Credit]");
                        htm.eP();
                    } else {
                        final String open = reg.openStatus;
                        final String fullCourse = reg.course.replace("M ", "MATH ");
                        if ("D".equals(open)) {
                            htm.sDiv();
                            htm.add("<strong>", fullCourse, "</strong> (", reg.sect, ") - dropped");
                            htm.eDiv();
                        } else {
                            final boolean compl = "Y".equals(reg.completed);

                            htm.add("<details open>");

                            htm.add("<summary style='min-width:688px;'><strong>");
                            htm.add(reg.course.replace("M ", "MATH "), "</strong> (", reg.sect, ")");

                            final boolean inc = "Y".equals(reg.iInProgress);
                            if (inc) {
                                if (reg.iTermKey == null) {
                                    htm.add(" - INCOMPLETE");
                                } else {
                                    htm.add(" - INCOMPLETE from ", reg.iTermKey.longString);
                                }
                            }

                            if ("G".equals(open)) {
                                htm.add(" - [<strong>Forfeit</strong>]");
                            } else if ("N".equals(open)) {
                                if (compl) {
                                    htm.add(" - [<strong>Completed, no longer open</strong>]");
                                } else {
                                    htm.add(" - [<strong>Forfeit</strong>]");
                                }
                            } else if (compl) {
                                if ("Y".equals(open)) {
                                    htm.add(" - [<strong>Completed</strong>, but still open]");
                                } else {
                                    htm.add(" - [<strong>Completed</strong>]");
                                }
                            } else if ("Y".equals(open)) {
                                htm.add(" - [<strong>In Progress</strong>]");
                            } else {
                                htm.add(" - [", open == null ? "Not started" : open, "]");
                            }

                            htm.add(" &nbsp; Pace Order: ", reg.paceOrder == null ? CoreConstants.DASH : reg.paceOrder);

                            htm.addln("</summary>");

                            htm.sDiv(null, "style='background:#ffffe4;margin:0 0 15px 0;line-height:140%;"
                                    + "padding:3px;border-width:0 1px 1px 1px;border-color:#888;"
                                    + "border-style:solid;font-size:13px;text-align:left;min-width:688px;'");

                            final String pre = reg.prereqSatis;
                            boolean status = false;
                            if ((!"Y".equals(pre) && !"P".equals(pre))) {
                                htm.addln("<strong>Prerequisite not satisfied.</strong>").br();
                                status = true;
                                if (stat.gatherData(cache, session, student.stuId, reg.course, false, false)) {
                                    if (reg.course.startsWith("MATH ")) {
                                        emitNewCourseDeadlines(cache, htm, reg);
                                    } else {
                                        emitOldCourseDeadlines(htm, stat);
                                    }
                                }
                            }

                            if (inc) {
                                htm.addln("Counted in pace? ", "Y".equals(reg.iCounted) ? "Yes" : "No").br();
                            }

                            if (reg.openStatus == null) {
                                if (!status) {
                                    htm.addln("<strong>Course not started.</strong>").br();
                                    if (stat.gatherData(cache, session, student.stuId, reg.course, false, false)) {
                                        if (reg.course.startsWith("MATH ")) {
                                            emitNewCourseDeadlines(cache, htm, reg);
                                        } else {
                                            emitOldCourseDeadlines(htm, stat);
                                        }
                                    }
                                }
                            } else if (stat.gatherData(cache, session, student.stuId, reg.course, false, false)) {

                                if (reg.course.startsWith("MATH ")) {
                                    emitNewCourseDeadlines(cache, htm, reg);
                                } else {
                                    emitOldCourseProgress(cache, htm, stat, reg, compl);
                                    htm.div("vgap");
                                    emitOldCourseDeadlines(htm, stat);
                                }
                            }

                            htm.eDiv().addln("</details>");
                        }
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

                    htm.add("<strong>", reg.course.replace("M ", "MATH "),
                            "</strong> (", reg.sect, ") - ", reg.termKey.longString);

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
    private static void emitOldCourseProgress(final Cache cache, final HtmlBuilder htm, final StudentCourseStatus stat,
                                              final RawStcourse reg, final boolean completed) throws SQLException {

        final int maxUnit = stat.getMaxUnit();
        final RawCsection csect = stat.getCourseSection();
        final EExamStructure examStruct = csect == null ? null : RawCsectionLogic.getExamStructure(csect);

        // Show student progress in the class
        htm.sTable("report", "style='margin:0;line-height:1;min-width:678px;'");

        // Top-level header for unit title
        htm.sTr();
        for (int i = 0; i <= maxUnit; ++i) {
            final RawCunit cunit = stat.getCourseUnit(i);
            final RawCusection cusect = stat.getCourseSectionUnit(i);
            final String alt = (i & 0x01) == 0x01 ? " class='alt'"
                    : CoreConstants.EMPTY;

            if (cunit == null && cusect == null) {
                continue;
            }

            final int numLessons = stat.getNumLessons(i);
            int numHw = 0;
            if (cunit != null) {
                for (int j = 0; j < numLessons; ++j) {
                    if (AssignmentLogic.get(cache).queryActive(cache, reg.course,
                            Integer.valueOf(i), Integer.valueOf(j + 1), "HW") != null) {
                        ++numHw;
                    }
                }
            }

            if (cunit == null || "SR".equals(cunit.unitType)) {
                final RawExam sr = cunit == null ? null : RawExamLogic
                        .queryActiveByCourseUnitType(cache, reg.course, Integer.valueOf(i), "R");
                final int cols = numHw + (sr == null ? 0 : 1);
                htm.add("<th ", alt, " colspan=" + cols + ">Pre").eTh();
            } else if ("INST".equals(cunit.unitType)) {
                final RawExam ur = RawExamLogic.queryActiveByCourseUnitType(cache, reg.course,
                        Integer.valueOf(i), "R");
                RawExam ue = null;

                if (examStruct == EExamStructure.UNIT_ONLY || examStruct == EExamStructure.UNIT_FINAL) {
                    ue = RawExamLogic.queryActiveByCourseUnitType(cache, reg.course, Integer.valueOf(i), "U");
                }

                final int cols = numHw + (ur == null ? 0 : 1) + (ue == null ? 0 : 1);

                htm.add("<th ", alt, " colspan=" + cols + ">Unit ", cunit.unit).eTh();

            } else if ("FIN".equals(cunit.unitType)
                    && (examStruct == EExamStructure.UNIT_FINAL)) {

                final RawExam ue = RawExamLogic.queryActiveByCourseUnitType(cache, reg.course,
                        Integer.valueOf(i), "F");

                final int cols = numHw + (ue == null ? 0 : 1);

                htm.add("<th class='special' colspan=" + cols + ">Final").eTh();
            }
        }

        final String maxAlt = (maxUnit & 0x01) == 0x01 ? " class='alt'"
                : CoreConstants.EMPTY;
        htm.add("<th ", maxAlt, ">Total").eTh();

        htm.eTr();

        // Second level with objectives
        htm.sTr();
        for (int i = 0; i <= maxUnit; ++i) {
            final RawCunit cunit = stat.getCourseUnit(i);
            final RawCusection cusect = stat.getCourseSectionUnit(i);
            final String alt = (i & 0x01) == 0x01 ? " class='alt'" : CoreConstants.EMPTY;

            if (cunit == null && cusect == null) {
                continue;
            }

            final int numLessons = stat.getNumLessons(i);
            int numHw = 0;
            if (cunit != null) {
                for (int j = 0; j < numLessons; ++j) {
                    if (AssignmentLogic.get(cache).queryActive(cache, reg.course,
                            Integer.valueOf(i), Integer.valueOf(j + 1), "HW") != null) {
                        htm.add("<th style='padding:2px 1px;'", alt,
                                ">H").eTh();
                        ++numHw;
                    }
                }
            }

            if (cunit == null || "SR".equals(cunit.unitType)) {

                if (RawExamLogic.queryActiveByCourseUnitType(cache, reg.course, Integer.valueOf(i),
                        "R") != null) {
                    htm.add("<th style='padding:2px 1px;' class='special'>UR").eTh();
                } else if (numHw == 0) {
                    htm.sTh().eTh();
                }
            } else if ("INST".equals(cunit.unitType)) {

                int count = 0;
                if (RawExamLogic.queryActiveByCourseUnitType(cache, reg.course, Integer.valueOf(i),
                        "R") != null) {
                    htm.add("<th style='padding:2px 1px;' class='special'>UR").eTh();
                    ++count;
                }

                if ((examStruct == EExamStructure.UNIT_ONLY
                        || examStruct == EExamStructure.UNIT_FINAL)
                        && (RawExamLogic.queryActiveByCourseUnitType(cache, reg.course,
                        Integer.valueOf(i), "U") != null)) {
                    htm.add("<th style='padding:2px 1px;' class='special'>UE").eTh();
                    ++count;
                }

                if (numHw + count == 0) {
                    htm.sTh().eTh();
                }

            } else if ("FIN".equals(cunit.unitType)
                    && (examStruct == EExamStructure.UNIT_FINAL)) {

                int count = 0;
                if (RawExamLogic.queryActiveByCourseUnitType(cache, reg.course, Integer.valueOf(i),
                        "F") != null) {
                    htm.add("<th style='padding:2px 1px;' class='special'>FE").eTh();
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
        for (int i = 0; i <= maxUnit; ++i) {
            final RawCunit cunit = stat.getCourseUnit(i);
            final RawCusection cusect = stat.getCourseSectionUnit(i);

            if (cunit == null && cusect == null) {
                continue;
            }

            final int numLessons = stat.getNumLessons(i);
            int numHw = 0;
            if (cunit != null) {
                for (int j = 0; j < numLessons; ++j) {
                    if (AssignmentLogic.get(cache).queryActive(cache, reg.course,
                            Integer.valueOf(i), Integer.valueOf(j + 1), "HW") != null) {
                        htm.sTd("ctr", "style='padding:2px 1px;'");
                        final String status = stat.getHomeworkStatus(i, j + 1);
                        if ("Completed".equals(status)
                                || "May Move On".equals(status)) {
                            htm.add("<img src='/images/bullet-green.png'/><br/>&nbsp;");
                        }
                        htm.eTd();
                        ++numHw;
                    }
                }
            }

            if (cunit == null || "SR".equals(cunit.unitType)) {
                htm.sTd("ctr", "style='padding:2px 1px;'");
                if ((RawExamLogic.queryActiveByCourseUnitType(cache, reg.course, Integer.valueOf(i),
                        "R") != null) && stat.isReviewPassed(i)) {
                    final boolean ontime = stat.isReviewPassedOnTime(i);
                    final Integer pts = ontime ? cusect.rePointsOntime : null;
                    final int ptsInt = pts == null ? 0 : pts.intValue();

                    if (ontime) {
                        if (ptsInt == 0) {
                            htm.add("<img src='/images/bullet-green.png'/><br/>&nbsp;");
                        } else {
                            htm.add("<img src='/images/bullet-green.png'/><br/><b>" + ptsInt + "</b>");
                        }
                    } else {
                        htm.add("<img src='/images/bullet-orange.png'/><br/>&nbsp;");
                    }
                }
                htm.eTd();
            } else if ("INST".equals(cunit.unitType)) {

                int count = 0;
                if (RawExamLogic.queryActiveByCourseUnitType(cache, reg.course, Integer.valueOf(i), "R") != null) {

                    htm.sTd("ctr", "style='padding:2px 1px;'");
                    if (stat.isReviewPassed(i)) {
                        final boolean ontime = stat.isReviewPassedOnTime(i);
                        final Integer pts = ontime ? cusect.rePointsOntime : null;
                        final int ptsInt = pts == null ? 0 : pts.intValue();

                        if (ontime) {
                            if (ptsInt == 0) {
                                htm.add("<img src='/images/bullet-green.png'/><br/>&nbsp;");
                            } else {
                                htm.add("<img src='/images/bullet-green.png'/><br/><b>" + ptsInt + "</b>");
                            }
                        } else {
                            htm.add("<img src='/images/bullet-orange.png'/><br/>&nbsp;");
                        }
                    }
                    ++count;
                    htm.eTd();
                }

                if ((examStruct == EExamStructure.UNIT_ONLY
                        || examStruct == EExamStructure.UNIT_FINAL)
                        && (RawExamLogic.queryActiveByCourseUnitType(cache, reg.course,
                        Integer.valueOf(i), "U") != null)) {

                    htm.sTd("ctr", "style='padding:2px 1px;'");
                    if (stat.isProctoredPassed(i)) {
                        final boolean ontime = stat.isProctoredPassedOnTime(i);
                        final int score = scores.getRawUnitExamScore(i);

                        if (ontime) {
                            if (score == 0) {
                                htm.add("<img src='/images/bullet-green.png'/><br/>&nbsp;");
                            } else {
                                htm.add("<img src='/images/bullet-green.png'/><br/><b>" + score + "</b>");
                            }
                        } else if (score == 0) {
                            htm.add("<img src='/images/bullet-orange.png'/><br/>&nbsp;");
                        } else {
                            htm.add("<img src='/images/bullet-orange.png'/><br/><b>" + score + "</b>");
                        }
                    }
                    htm.eTd();
                    ++count;
                }

                if (numHw + count == 0) {
                    htm.sTd().eTd();
                }

            } else if ("FIN".equals(cunit.unitType)
                    && (examStruct == EExamStructure.UNIT_FINAL)) {

                int count = 0;
                if (RawExamLogic.queryActiveByCourseUnitType(cache, reg.course, Integer.valueOf(i),
                        "F") != null) {
                    if (stat.isProctoredPassed(i)) {
                        final boolean ontime = stat.isProctoredPassedOnTime(i);
                        final int score = scores.getRawUnitExamScore(i);

                        htm.sTd("ctr", "style='padding:2px 1px;'");
                        if (ontime) {
                            if (score == 0) {
                                htm.add("<img src='/images/bullet-green.png'/><br/>&nbsp;");
                            } else {
                                htm.add("<img src='/images/bullet-green.png'/><br/><b>" + score + "</b>");
                            }
                        } else if (score == 0) {
                            htm.add("<img src='/images/bullet-orange.png'/><br/>&nbsp;");
                        } else {
                            htm.add("<img src='/images/bullet-orange.png'/><br/><b>" + score + "</b>");
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

        htm.sTd("ctr", "style='padding:2px 1px;'").add("<b>" + total);
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
    private static void emitOldCourseDeadlines(final HtmlBuilder htm, final StudentCourseStatus stat) {

        // Show student progress in the class
        htm.sTable("report", "style='margin:0;line-height:1;'");

        final int maxUnit = stat.getMaxUnit();

        htm.sTr();
        htm.sTh().add("Milestone").eTh();
        htm.sTh().add("Deadline").eTh();
        htm.sTh().add("Notes").eTh();
        htm.eTr();

        final LocalDate today = LocalDate.now();

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
                    htm.sTd("pad").add("Unit " + i + " Review").eTd();
                    htm.sTd("pad").add(TemporalUtils.FMT_WMDY.format(reviewDeadline)).eTd();
                    emitNotesCell(htm, reviewDeadline, today);
                    htm.eTr();
                }

                final LocalDate unitDeadline = stat.getUnitExamDeadline(i);
                if (unitDeadline != null && reviewDeadline != null) {
                    htm.sTr();
                    htm.sTd("pad").add("Unit " + i + " Exam").eTd();
                    htm.sTd("pad").add(TemporalUtils.FMT_WMDY.format(unitDeadline)).eTd();
                    emitNotesCell(htm, reviewDeadline, today);
                    htm.eTr();
                }
            } else if ("FIN".equals(cunit.unitType)) {
                final LocalDate reviewDeadline = stat.getReviewExamDeadline(i);
                if (reviewDeadline != null) {
                    htm.sTr();
                    htm.sTd("pad").add("Final Review").eTd();
                    htm.sTd("pad").add(TemporalUtils.FMT_WMDY.format(reviewDeadline)).eTd();
                    emitNotesCell(htm, reviewDeadline, today);
                    htm.eTr();
                }

                final LocalDate finalDeadline = stat.getUnitExamDeadline(i);
                if (finalDeadline != null) {
                    htm.sTr();
                    htm.sTd("pad").add("Final Exam").eTd();
                    htm.sTd("pad").add(TemporalUtils.FMT_WMDY.format(finalDeadline)).eTd();
                    emitNotesCell(htm, finalDeadline, today);
                    htm.eTr();
                }
            }
        }

        htm.eTable();
    }

    /**
     * Emits a cell with a description of the date's relative position to today.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param deadline the deadline date
     * @param today    today's date
     */
    private static void emitNotesCell(final HtmlBuilder htm, final ChronoLocalDate deadline,
                                      final ChronoLocalDate today) {

        if (deadline.equals(today)) {
            htm.sTd("pad").add("Today").eTd();
        } else if (deadline.isBefore(today)) {
            final long ago = ChronoUnit.DAYS.between(deadline, today);
            if (ago == 1L) {
                htm.sTd("pad").add("Yesterday").eTd();
            } else {
                htm.sTd("pad").add(ago + " days ago").eTd();
            }
        } else {
            final long hence = ChronoUnit.DAYS.between(today, deadline);
            if (hence == 1L) {
                htm.sTd("pad").add("Tomorrow").eTd();
            } else {
                htm.sTd("pad").add(hence + " days from now").eTd();
            }
        }
    }

    /**
     * Emits a table that shows deadlines for the course (with overrides, if any).
     *
     * @param cache the cache
     * @param htm   the {@code HtmlBuilder} to which to append
     * @param reg   the student registration record
     */
    private static void emitNewCourseDeadlines(final Cache cache, final HtmlBuilder htm,
                                               final RawStcourse reg) throws SQLException {

        final TermRec active = cache.getSystemData().getActiveTerm();
        final RawStterm stterm = RawSttermLogic.query(cache, active.term, reg.stuId);

        if (stterm == null) {
            htm.sP().add("No STTERM record found").eP();
        } else {
            htm.sTable("report", "style='margin:0;line-height:1;'");

            htm.sTr();
            htm.sTh().add("Milestone").eTh();
            htm.sTh().add("Deadline").eTh();
            htm.sTh().add("Notes").eTh();
            htm.eTr();

            final List<StandardMilestoneRec> milestones = StandardMilestoneLogic.get(cache).queryByPaceTrackPace(cache,
                    stterm.paceTrack, stterm.pace);
            milestones.sort(null);

            final List<StudentStandardMilestoneRec> overrides =
                    StudentStandardMilestoneLogic.get(cache).queryByStuPaceTrackPace(cache, reg.stuId,
                            stterm.paceTrack, stterm.pace);

            final List<MasteryExamRec> allMastery = MasteryExamLogic.get(cache).queryAll(cache);
            final List<MasteryAttemptRec> allAttempts = MasteryAttemptLogic.get(cache).queryByStudent(cache, reg.stuId);

            StudentStandardMilestoneRec override;
            for (final StandardMilestoneRec ms : milestones) {

                final String cls = (ms.unit.intValue() & 0x01) == 0x01 ? "odd" : "even";

                override = null;
                for (final StudentStandardMilestoneRec test : overrides) {
                    if (test.paceIndex.equals(ms.paceIndex) && test.unit.equals(ms.unit)
                            && test.objective.equals(ms.objective) && test.msType.equals(ms.msType)) {
                        override = test;
                        break;
                    }
                }

                final String msDateStr = TemporalUtils.FMT_MDY.format(ms.msDate);
                final String overrideDateStr = override == null ? CoreConstants.EMPTY
                        : TemporalUtils.FMT_MDY.format(override.msDate);

                String statusStr = "Unknown";
                if (reg != null && "MA".equals(ms.msType)) {
                    MasteryExamRec masteryExam = null;
                    for (final MasteryExamRec rec : allMastery) {
                        if (rec.courseId.equals(reg.course) && rec.unit.equals(ms.unit)
                                && rec.objective.equals(ms.objective)) {
                            masteryExam = rec;
                            break;
                        }
                    }

                    if (masteryExam != null) {
                        LocalDateTime whenPassed = null;
                        LocalDateTime whenAttempted = null;

                        for (final MasteryAttemptRec attempt : allAttempts) {
                            if (attempt.examId.equals(masteryExam.examId) && attempt.whenFinished != null) {
                                if (whenAttempted == null || whenAttempted.isAfter(attempt.whenFinished)) {
                                    whenAttempted = attempt.whenFinished;
                                }
                                if ("Y".equals(attempt.passed) &&
                                        (whenPassed == null || whenPassed.isAfter(attempt.whenFinished))) {
                                    whenPassed = attempt.whenFinished;
                                }
                            }
                        }

                        if (whenPassed != null) {
                            statusStr = "Mastered on " + TemporalUtils.FMT_MDY.format(whenPassed);
                        } else if (whenAttempted != null) {
                            statusStr = "Attempted on " + TemporalUtils.FMT_MDY.format(whenAttempted);
                        } else {
                            statusStr = "Not Yet Attempted";
                        }
                    }
                }

                if ("MA".equals(ms.msType)) {
                    final String msLabel = "Target " + ms.unit + "." + ms.objective + " Mastery";
                    final String dtLabel = overrideDateStr.isBlank() ? msDateStr :
                            (msDateStr + " (" + overrideDateStr + ")");

                    htm.sTr().sTd(cls).add(msLabel).eTd()
                            .sTd(cls).add(dtLabel).eTd()
                            .sTd(cls).add(statusStr).eTd().eTr();
                }
            }

            htm.eTable();
        }
    }
}
