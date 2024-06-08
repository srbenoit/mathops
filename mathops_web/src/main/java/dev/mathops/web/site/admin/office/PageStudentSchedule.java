package dev.mathops.web.site.admin.office;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawlogic.RawPaceAppealsLogic;
import dev.mathops.db.old.rawlogic.RawStmilestoneLogic;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawPaceAppeals;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStmilestone;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.rec.MasteryAttemptRec;
import dev.mathops.db.old.rec.MasteryExamRec;
import dev.mathops.db.old.rec.StandardMilestoneRec;
import dev.mathops.db.old.rec.StudentStandardMilestoneRec;
import dev.mathops.db.old.reclogic.MasteryExamLogic;
import dev.mathops.db.old.reclogic.StandardMilestoneLogic;
import dev.mathops.db.old.reclogic.StudentStandardMilestoneLogic;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.session.ISessionManager;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.LiveSessionInfo;
import dev.mathops.session.sitelogic.data.SiteData;
import dev.mathops.session.sitelogic.data.SiteDataCfgExamStatus;
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
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

/**
 * Pages that displays a record of a student's schedule, deadlines, and appeals.
 */
enum PageStudentSchedule {
    ;

    /**
     * Shows the student information page (the student ID must be available in a request parameter named "stu").
     *
     * @param studentData the student data object
     * @param site        the site
     * @param req         the request
     * @param resp        the response
     * @param session     the user's login session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final StudentData studentData, final AdminSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String studentId = req.getParameter("stu");

        if (AbstractSite.isParamInvalid(studentId)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  studentId='", studentId, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else if (studentId == null) {
            PageHome.doGet(studentData, site, req, resp, session, "Student not found.");
        } else {
            final RawStudent student = studentData.getStudentRecord();

            if (student == null) {
                PageHome.doGet(studentData, site, req, resp, session, "Student not found.");
            } else {
                doStudentSchedulePage(studentData, site, req, resp, session, student);
            }
        }
    }

    /**
     * Shows the student schedule page for a provided student.
     *
     * @param studentData the student data object
     * @param site        the site
     * @param req         the request
     * @param resp        the response
     * @param session     the user's login session information
     * @param student     the student for which to present information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private static void doStudentSchedulePage(final StudentData studentData, final AdminSite site,
                                              final ServletRequest req, final HttpServletResponse resp,
                                              final ImmutableSessionInfo session, final RawStudent student)
            throws IOException, SQLException {

        final HtmlBuilder htm = OfficePage.startOfficePage(studentData, site, session, true);

        final String screenName = student.getScreenName();
        htm.sP("studentname").add("<strong>", screenName, "</strong> &nbsp; <strong><code>", student.stuId,
                        "</code></strong>").eP();

        if (session.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {

            htm.sDiv("narrowstack");
            htm.addln("<form method='get' action='student_info.html'>");
            htm.addln("<input type='hidden' name='stu' value='", student.stuId, "'/>");
            htm.addln("<button class='nav'>Registrations</button>");
            htm.addln("</form>");

            htm.addln("<form method='get' action='student_schedule.html'>");
            htm.addln("<input type='hidden' name='stu' value='", student.stuId, "'/>");
            htm.addln("<button class='navlit'>Schedule</button>");
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
            emitStudentSchedule(studentData, site, htm, student);
            htm.eDiv(); // detail
        }

        final SystemData systemData = studentData.getSystemData();
        Page.endOrdinaryPage(systemData, site, htm, true);

        final byte[] bytes = htm.toString().getBytes(StandardCharsets.UTF_8);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, bytes);
    }

    /**
     * Emits the student's schedule history.
     *
     * @param studentData the student data object
     * @param site        the site
     * @param htm         the {@code HtmlBuilder} to which to append
     * @param student     the student
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitStudentSchedule(final StudentData studentData, final AdminSite site, final HtmlBuilder htm,
                                            final RawStudent student) throws SQLException {

        final SiteData data = new SiteData(site.getDbProfile(), ZonedDateTime.now(),
                RawRecordConstants.M117, RawRecordConstants.M118, RawRecordConstants.M124,
                RawRecordConstants.M125, RawRecordConstants.M126, RawRecordConstants.MATH125,
                RawRecordConstants.MATH126);

        final LiveSessionInfo live =
                new LiveSessionInfo(CoreConstants.newId(ISessionManager.SESSION_ID_LEN), "none", ERole.STUDENT);

        final String screenName = student.getScreenName();
        live.setUserInfo(student.stuId, student.firstName, student.lastName, screenName);

        final ImmutableSessionInfo session = new ImmutableSessionInfo(live);
        if (data.load(session)) {
            emitStudentSchedule(studentData, data, student.stuId, htm);
        } else {
            final String error = data.getError();
            htm.sP("red").addln("Failed to load student data: ", error).eP();
        }
    }

    /**
     * Emits the student's schedule history.
     *
     * @param studentData the student data object
     * @param data        the site data
     * @param studentId   the student ID
     * @param htm         the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitStudentSchedule(final StudentData studentData, final SiteData data,
                                            final String studentId, final HtmlBuilder htm) throws SQLException {

        final List<RawStcourse> regs = data.registrationData.getPaceRegistrations();
        boolean newCourses = false;

        // Make sure each course has a pace order
        int max = 0;
        int numMissing = 0;
        if (regs != null) {
            for (final RawStcourse sc : regs) {
                final String courseId = sc.course;
                if (RawRecordConstants.MATH117.equals(courseId)
                        || RawRecordConstants.MATH118.equals(courseId)
                        || RawRecordConstants.MATH124.equals(courseId)
                        || RawRecordConstants.MATH125.equals(courseId)
                        || RawRecordConstants.MATH126.equals(courseId)) {
                    newCourses = true;
                }
                if (sc.paceOrder == null) {
                    ++numMissing;
                } else {
                    max = Math.max(max, sc.paceOrder.intValue());
                }
            }
        }
        if (numMissing > 0) {
            final String[] courses = {RawRecordConstants.M117, RawRecordConstants.M118,
                    RawRecordConstants.M124, RawRecordConstants.M125, RawRecordConstants.M126};
            for (final String course : courses) {
                final int size = regs.size();
                for (int i = 0; i < size; ++i) {
                    final RawStcourse reg = regs.get(i);

                    if (reg.paceOrder == null && course.equals(reg.course)) {
                        reg.paceOrder = Integer.valueOf(max + 1);
                        regs.set(i, reg);
                        ++max;
                    }
                }
            }
        }

        if (newCourses) {
            emitStudentScheduleNew(studentData, data, studentId, max, htm);
        } else {
            emitStudentScheduleOld(studentData, data, studentId, max, htm);
        }
    }

    /**
     * Emits the student's schedule for an "old" course.
     *
     * @param studentData the student data object
     * @param data        the site data
     * @param studentId   the student ID
     * @param htm         the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitStudentScheduleOld(final StudentData studentData, final SiteData data,
                                               final String studentId,
                                               final int max, final HtmlBuilder htm) throws SQLException {

        final TermRec active = studentData.getActiveTerm();
        final String key = active.term.shortString;

        final RawStterm stterm = data.milestoneData.getStudentTerm(key);
        if (stterm == null || stterm.paceTrack == null) {
            htm.sH(4).add(Integer.toString(max), "-course pace").eH(4);
        } else {
            htm.sH(4).add(Integer.toString(max), "-course pace - track ", stterm.paceTrack).eH(4);
        }

        final List<RawMilestone> ms = data.milestoneData.getMilestones(active.term);
        final List<RawStmilestone> stms = data.milestoneData.getStudentMilestones(active.term);

        htm.sTable("report");
        htm.sTr().sTh().add("Course").eTh()
                .sTh().add("Order").eTh()
                .sTh().add("Milestone").eTh()
                .sTh().add("Orig. Date").eTh()
                .sTh().add("Adjusted Date").eTh()
                .sTh().add("Status").eTh()
                .sTh().add("Actions").eTh().eTr();

        final LocalDate today = LocalDate.now();

        final List<RawStcourse> regs = data.registrationData.getPaceRegistrations();
        for (int order = 1; order <= max; ++order) {
            RawStcourse reg = null;
            for (final RawStcourse sc : regs) {
                if (sc.paceOrder.intValue() == order) {
                    reg = sc;
                    break;
                }
            }
            if (reg == null) {
                continue;
            }

            htm.sTr().sTd(null, "colspan='7'", "style='padding:1px;background:white;'").eTd().eTr();

            final String cls = (order & 0x01) == 0x01 ? "odd" : "even";

            for (final RawMilestone milestone : ms) {
                final Integer num = milestone.msNbr;
                final int index = num.intValue() / 10 % 10;
                if (index != order) {
                    continue;
                }

                RawStmilestone ov = null;
                for (final RawStmilestone test : stms) {
                    if (test.msNbr.equals(milestone.msNbr) && test.msType.equals(milestone.msType)) {
                        ov = test;
                        break;
                    }
                }

                htm.sTr().sTd(cls).add(reg.course).eTd().sTd(cls).add(reg.paceOrder).eTd();

                final Integer unit = Integer.valueOf(milestone.msNbr.intValue() % 10);
                String status = CoreConstants.EMPTY;

                final LocalDate deadline = ov == null ? milestone.msDate : ov.msDate;

                // Generate a nice representation of the milestone, and based on the milestone type
                // determine the student's status
                htm.sTd(cls);
                if ("FE".equals(milestone.msType)) {
                    htm.add("Final Exam");

                    final SiteDataCfgExamStatus finalStatus =
                            data.statusData.getExamStatus(reg.course, unit, "F");

                    if (finalStatus.passedOnTime) {
                        status = "PASSED (on-time)";
                    } else if (finalStatus.firstPassingDate == null) {
                        if (finalStatus.totalAttemptsSoFar > 0) {
                            status = "Attempted " + finalStatus.totalAttemptsSoFar + " times";
                        } else if (deadline.isBefore(today)) {
                            status = "Overdue";
                        }
                    } else {
                        status = "PASSED (late " + TemporalUtils.FMT_MD.format(finalStatus.firstPassingDate) + ")";
                    }

                } else if ("F1".equals(milestone.msType)) {
                    htm.add("Final Exam - Last try");

                    final SiteDataCfgExamStatus finalStatus =
                            data.statusData.getExamStatus(reg.course, unit, "F");

                    if (finalStatus.firstPassingDate == null) {
                        // TODO: see if student is eligible for last try
                    } else {
                        status = "n/a";
                    }

                } else if ("RE".equals(milestone.msType)) {
                    htm.add("Unit " + unit + " Review");

                    final SiteDataCfgExamStatus reviewStatus =
                            data.statusData.getExamStatus(reg.course, unit, "R");

                    if (reviewStatus.passedOnTime) {
                        status = "PASSED (on-time)";
                    } else if (reviewStatus.firstPassingDate == null) {
                        if (reviewStatus.totalAttemptsSoFar > 0) {
                            status = "Attempted " + reviewStatus.totalAttemptsSoFar + " times";
                        } else if (deadline.isBefore(today)) {
                            status = "Overdue";
                        }
                    } else {
                        status = "PASSED (late " + TemporalUtils.FMT_MD.format(reviewStatus.firstPassingDate) + ")";
                    }

                } else if ("UE".equals(milestone.msType)) {
                    htm.add("Unit " + unit + " Exam");

                    final SiteDataCfgExamStatus unitStatus =
                            data.statusData.getExamStatus(reg.course, unit, "U");

                    if (unitStatus.passedOnTime) {
                        status = "PASSED (on-time)";
                    } else if (unitStatus.firstPassingDate == null) {
                        if (unitStatus.totalAttemptsSoFar > 0) {
                            status = "Attempted " + unitStatus.totalAttemptsSoFar + " times";
                        } else if (deadline.isBefore(today)) {
                            status = "Overdue";
                        }
                    } else {
                        status = "PASSED (late " + TemporalUtils.FMT_MD.format(unitStatus.firstPassingDate) + ")";
                    }
                }

                htm.eTd();

                htm.sTd(cls).add(TemporalUtils.FMT_MD.format(milestone.msDate)).eTd(); //
                if (ov == null) {
                    htm.sTd(cls).eTd();
                } else {
                    htm.sTd(cls).add(TemporalUtils.FMT_MD.format(ov.msDate)).eTd();
                }

                htm.sTd("Overdue".equals(status) ? "red" : cls).add(status).eTd();

                if (stterm != null) {
                    htm.sTd(cls).add("<form action='student_schedule.html' method='POST' style='display:inline;' >",
                            "<input type='hidden' name='act' value='appealform'/>",
                            "<input type='hidden' name='stu' value='", studentId, "'/>",
                            "<input type='hidden' name='c' value='", reg.course, "'/>",
                            "<input type='hidden' name='p' value='", Integer.toString(max), "'/>",
                            "<input type='hidden' name='t' value='", stterm.paceTrack, "'/>",
                            "<input type='hidden' name='o' value='", Integer.toString(order), "'/>",
                            "<input type='hidden' name='m' value='", milestone.msNbr, "'/>",
                            "<input type='hidden' name='y' value='", milestone.msType, "'/>",
                            "<input type='hidden' name='x' value='",
                            TemporalUtils.FMT_MDY.format(milestone.msDate), "'/>",
                            "<input type='submit' value='Appeal'/>",
                            "</form>").eTd();
                }

                htm.eTr();
            }
        }

        htm.eTable();

        htm.sH(4).add("Deadline Appeals").eH(4);

        final List<RawPaceAppeals> appeals = studentData.getDeadlineAppeals();

        if (appeals.isEmpty()) {
            htm.sP().add("(none)").eP();
        } else {
            Collections.sort(appeals);
            for (final RawPaceAppeals appeal : appeals) {
                htm.sP().add("<details><summary>", TemporalUtils.FMT_MDY.format(appeal.appealDt), ", Pace ",
                        appeal.pace, " Track ", appeal.paceTrack);

                final Integer number = appeal.msNbr;
                if (number != null && appeal.msType != null) {
                    final int course = number.intValue() / 10 % 10;
                    final int unit = number.intValue() % 10;
                    switch (appeal.msType) {
                        case RawMilestone.FINAL_EXAM:
                            htm.add(", Course " + course, " Final Exam");
                            break;
                        case RawMilestone.FINAL_LAST_TRY:
                            htm.add(", Course " + course, " Final Last Try");
                            break;
                        case RawMilestone.SKILLS_REVIEW:
                            htm.add(", Course " + course, " Skills Review");
                            break;
                        case RawMilestone.UNIT_EXAM:
                            htm.add(", Course " + course, " Unit " + unit, "Exam");
                            break;
                        case RawMilestone.UNIT_REVIEW_EXAM:
                            htm.add(", Course " + course, " Unit " + unit, " Review");
                            break;
                        case RawMilestone.STANDARD_MASTERY:
                            htm.add(", Course " + course, " Unit " + unit, " Standard Mastery");
                            break;
                        case RawMilestone.EXPLORATION:
                            htm.add(", Course " + course, " Unit " + unit, " Exploration");
                            break;
                        case RawMilestone.EXPLORATION_1_DAY_LATE:
                            htm.add(", Course " + course, " Unit " + unit, " Exploration (1 day late)");
                            break;
                        case RawMilestone.TOUCHPOINT_1F1:
                        case RawMilestone.TOUCHPOINT_1FE:
                        case RawMilestone.TOUCHPOINT_1R1:
                        case RawMilestone.TOUCHPOINT_1R3:
                        case RawMilestone.TOUCHPOINT_2F1:
                        case RawMilestone.TOUCHPOINT_2FE:
                        case RawMilestone.TOUCHPOINT_2R3:
                        case RawMilestone.TOUCHPOINT_3F1:
                        case RawMilestone.TOUCHPOINT_3FE:
                        case RawMilestone.TOUCHPOINT_3R3:
                        case RawMilestone.TOUCHPOINT_4F1:
                        case RawMilestone.TOUCHPOINT_4FE:
                        case RawMilestone.TOUCHPOINT_4R3:
                        case RawMilestone.TOUCHPOINT_5F1:
                        case RawMilestone.TOUCHPOINT_5FE:
                        case RawMilestone.TOUCHPOINT_5R3:
                            htm.add(", Communications touchpoint");
                            break;
                        default:
                            htm.add(", Unknown milestone type");
                            break;
                    }
                }

                htm.addln("</summary>");

                htm.addln("<ul>");
                htm.addln("<li>Relief given? : ", "Y".equals(appeal.reliefGiven) ? "Yes" : "No", "</li>");
                if (appeal.msDate != null) {
                    htm.addln("<li>Milestone Date : ", appeal.msDate, "</li>");
                }
                if (appeal.newDeadlineDt != null) {
                    htm.addln("<li>New Deadline : ", appeal.newDeadlineDt, "</li>");
                }
                if (appeal.nbrAtmptsAllow != null) {
                    htm.addln("<li>Attempts Allowed : ", appeal.nbrAtmptsAllow, "</li>");
                }
                if (appeal.circumstances != null) {
                    htm.addln("<li>Circumstances : ", appeal.circumstances, "</li>");
                }
                if (appeal.comment != null) {
                    htm.addln("<li>Comment : ", appeal.comment, "</li>");
                }
                if (appeal.interviewer != null) {
                    htm.addln("<li>Interviewer : ", appeal.interviewer, "</li>");
                }
                htm.addln("</ul>");
                htm.sP().addln("</details>").eP();
            }
        }
    }

    /**
     * Emits the student's schedule for a "new" course.
     *
     * @param studentData the student data object
     * @param data        the site data
     * @param studentId   the student ID
     * @param htm         the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitStudentScheduleNew(final StudentData studentData, final SiteData data,
                                               final String studentId, final int max, final HtmlBuilder htm)
            throws SQLException {

        final TermRec active = studentData.getActiveTerm();
        final String key = active.term.shortString;

        final RawStterm stterm = data.milestoneData.getStudentTerm(key);
        if (stterm == null) {
            htm.sP().add("No STTERM record found").eP();
        } else {
            final List<StandardMilestoneRec> milestones = StandardMilestoneLogic.get(cache).queryByPaceTrackPace(cache,
                    stterm.paceTrack, stterm.pace);
            milestones.sort(null);

            final List<StudentStandardMilestoneRec> overrides =
                    StudentStandardMilestoneLogic.get(cache).queryByStuPaceTrackPace(cache, studentId,
                            stterm.paceTrack, stterm.pace);

            final List<MasteryExamRec> allMastery = MasteryExamLogic.get(cache).queryAll(cache);
            final List<MasteryAttemptRec> allAttempts = studentData.getMasteryAttempts();

            htm.sTable("report");
            htm.sTr().sTh().add("Course").eTh()
                    .sTh().add("Pace Index").eTh()
                    .sTh().add("Module").eTh()
                    .sTh().add("Objective").eTh()
                    .sTh().add("Milestone").eTh()
                    .sTh().add("Orig. Date").eTh()
                    .sTh().add("Adjusted Date").eTh()
                    .sTh().add("Status").eTh()
                    .sTh().add("Actions").eTh().eTr();

            StudentStandardMilestoneRec override = null;
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

                final RawStcourse reg = getCourseForIndex(data, ms.paceIndex);
                final String courseLabel = reg == null ? "Course " + ms.paceIndex : reg.course;

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

                htm.sTr().sTd(cls).add(courseLabel).eTd()
                        .sTd(cls).add(ms.paceIndex).eTd()
                        .sTd(cls).add(ms.unit).eTd()
                        .sTd(cls).add(ms.objective).eTd()
                        .sTd(cls).add(ms.msType).eTd()
                        .sTd(cls).add(msDateStr).eTd()
                        .sTd(cls).add(overrideDateStr).eTd()
                        .sTd(cls).add(statusStr).eTd();

                htm.sTd(cls).add("<form action='student_schedule.html' method='POST' style='display:inline;' >",
                        "<input type='hidden' name='act' value='stdappealform'/>",
                        "<input type='hidden' name='stu' value='", studentId, "'/>",
                        "<input type='hidden' name='crs' value='", reg.course, "'/>",
                        "<input type='hidden' name='pac' value='", stterm.pace, "'/>",
                        "<input type='hidden' name='trk' value='", stterm.paceTrack, "'/>",
                        "<input type='hidden' name='idx' value='", ms.paceIndex, "'/>",
                        "<input type='hidden' name='unt' value='", ms.unit, "'/>",
                        "<input type='hidden' name='obj' value='", ms.objective, "'/>",
                        "<input type='hidden' name='typ' value='", ms.msType, "'/>",
                        "<input type='hidden' name='dat' value='", TemporalUtils.FMT_MDY.format(ms.msDate), "'/>",
                        "<input type='submit' value='Appeal'/>",
                        "</form>").eTd().eTr();
            }

            htm.eTable();
        }
    }

    /**
     * Gets the course ID having a specified pace index.
     *
     * @param data      the site data
     * @param paceIndex the pace index (1 for first course)
     * @return the registration record
     */
    private static RawStcourse getCourseForIndex(final SiteData data, final Integer paceIndex) {

        RawStcourse result = null;

        final List<RawStcourse> regs = data.registrationData.getPaceRegistrations();
        if (regs != null) {
            for (final RawStcourse reg : regs) {
                if (paceIndex.equals(reg.paceOrder)) {
                    result = reg;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Processes a POST to the student schedule page.
     *
     * @param studentData the student data object
     * @param site        the site
     * @param req         the request
     * @param resp        the response
     * @param session     the user's login session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doPost(final StudentData studentData, final AdminSite site, final ServletRequest req,
                       final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String studentId = req.getParameter("stu");
        final String action = req.getParameter("act");

        if (AbstractSite.isParamInvalid(studentId) || AbstractSite.isParamInvalid(action)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  studentId='", studentId, "'");
            Log.warning("  act='", action, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else if ("appealform".equals(action)) {
            presentAppealForm(studentData, site, req, resp, session);
        } else if ("appeal".equals(action)) {
            processAppeal(studentData, site, req, resp, session);
        } else {
            doGet(studentData, site, req, resp, session);
        }
    }

    /**
     * Presents the form through which to enter an appeal.
     *
     * @param studentData the student data object
     * @param site        the site
     * @param req         the request
     * @param resp        the response
     * @param session     the user's login session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private static void presentAppealForm(final StudentData studentData, final AdminSite site,
                                          final ServletRequest req, final HttpServletResponse resp,
                                          final ImmutableSessionInfo session) throws IOException, SQLException {

        final String studentId = req.getParameter("stu");
        final String course = req.getParameter("c");
        final String pace = req.getParameter("p");
        final String track = req.getParameter("t");
        final String order = req.getParameter("o");
        final String milestone = req.getParameter("m");
        final String type = req.getParameter("y");
        final String origDate = req.getParameter("x");

        if (AbstractSite.isParamInvalid(course) || AbstractSite.isParamInvalid(pace)
                || AbstractSite.isParamInvalid(track) || AbstractSite.isParamInvalid(order)
                || AbstractSite.isParamInvalid(milestone) || AbstractSite.isParamInvalid(type)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  c='", course, "'");
            Log.warning("  p='", pace, "'");
            Log.warning("  t='", track, "'");
            Log.warning("  o='", order, "'");
            Log.warning("  m='", milestone, "'");
            Log.warning("  y='", type, "'");
            Log.warning("  x='", origDate, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else if (pace == null || track == null || order == null || milestone == null) {
            PageHome.doGet(studentData, site, req, resp, session, "Missing data to take action.");
        } else if (studentId == null) {
            PageHome.doGet(studentData, site, req, resp, session, "Student not found.");
        } else {
            final RawStudent student = studentData.getStudentRecord();

            if (student == null) {
                PageHome.doGet(studentData, site, req, resp, session, "Student not found.");
            } else {
                final HtmlBuilder htm = new HtmlBuilder(2000);
                Page.startOrdinaryPage(htm, OfficePage.getSiteTitle(), null, false, "Precalculus Center",
                        "home.html", Page.NO_BARS, null, false, true);

                htm.sDiv("floatnav");
                htm.addln("<form method='get' action='home.html'>");
                htm.add("<button class='nav'>Home</button>");
                htm.addln("</form>");
                htm.eDiv(); // floatnav

                htm.sH(1).add("Office System").eH(1);
                htm.sH(3).add("Logged in as ", session.getEffectiveScreenName()).eH(3).hr();

                htm.div("vgap");

                htm.sP("studentname").add("<strong>", student.getScreenName(), "</strong> &nbsp; <strong><code>",
                        student.stuId,
                        "</code></strong>").eP();

                if (session.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {

                    htm.sDiv("narrowstack");
                    htm.addln("<form method='get' action='student_info.html'>");
                    htm.addln("<input type='hidden' name='stu' value='", student.stuId, "'/>");
                    htm.addln("<button class='nav'>Registrations</button>");
                    htm.addln("</form>");

                    htm.addln("<form method='get' action='student_schedule.html'>");
                    htm.addln("<input type='hidden' name='stu' value='", student.stuId, "'/>");
                    htm.addln("<button class='navlit'>Schedule</button>");
                    htm.addln("</form>");

                    htm.addln("<form method='get' action='student_activity.html'>");
                    htm.addln("<input type='hidden' name='stu' value='", student.stuId, "'/>");
                    htm.addln("<button class='nav'>Activity</button>");
                    htm.addln("</form>");

                    htm.addln("<form method='get' action='student_placement.html'>");
                    htm.addln("<input type='hidden' name='stu' value='", student.stuId, "'/>");
                    htm.addln("<button class='nav'>Placement</button>");
                    htm.addln("</form>");
                    htm.eDiv(); // narrowstack

                    htm.sDiv("detail");
                    emitAppealForm(session, student, course, pace, track, order, milestone, type, origDate, htm);
                    htm.eDiv(); // detail
                }

                Page.endOrdinaryPage(studentData, site, htm, true);
                AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    /**
     * Emits the appeal form.
     *
     * @param session   the login session
     * @param student   the student
     * @param course    the course in which the appeal is being added
     * @param pace      the pace
     * @param track     the pace track
     * @param order     the order (index of the course within the pace)
     * @param milestone the milestone
     * @param type      the milestone type
     * @param origDate  the original milestone date
     * @param htm       the {@code HtmlBuilder} to which to append
     */
    private static void emitAppealForm(final ImmutableSessionInfo session, final RawStudent student,
                                       final String course, final String pace, final String track, final String order,
                                       final String milestone, final String type, final String origDate,
                                       final HtmlBuilder htm) {

        final String descriptor;

        if (RawMilestone.FINAL_LAST_TRY.equals(type)) {
            descriptor = "Final Exam Last Try";
        } else if (RawMilestone.FINAL_EXAM.equals(type)) {
            descriptor = "Final Exam";
        } else if (RawMilestone.UNIT_REVIEW_EXAM.equals(type)) {
            descriptor = "Unit " + milestone.charAt(1) + " Review Exam";
        } else if (RawMilestone.STANDARD_MASTERY.equals(type)) {
            descriptor = "Unit " + milestone.charAt(1) + " Standard Mastery";
        } else if (RawMilestone.EXPLORATION.equals(type)) {
            descriptor = "Unit " + milestone.charAt(1) + " Exploration";
        } else if (RawMilestone.EXPLORATION_1_DAY_LATE.equals(type)) {
            descriptor = "Unit " + milestone.charAt(1) + " Exploration (1 day late)";
        } else {
            descriptor = type;
        }

        htm.sH(3).add("Deadline appeal for ", course.replace("M ", "MATH "),
                " (course ", order, " of ", pace, "), ", descriptor).eH(3);
        htm.div("vgap");

        htm.addln("<form action='student_schedule.html' method='POST'>");

        htm.addln("<input type='hidden' name='act' value='appeal'/>");
        htm.addln("<input type='hidden' name='stu' value='", student.stuId, "'/>");
        htm.addln("<input type='hidden' name='p' value='", pace, "'/>");
        htm.addln("<input type='hidden' name='t' value='", track, "'/>");
        htm.addln("<input type='hidden' name='o' value='", order, "'/>");
        htm.addln("<input type='hidden' name='m' value='", milestone, "'/>");
        htm.addln("<input type='hidden' name='y' value='", type, "'/>");

        htm.sP().addln("<div style='display:inline-block;width:140px;'>",
                "Interviewer:</div> <input type='text' name='iv' value='", session.lastName, "'/>").eP();

        htm.sP().addln("<div style='display:inline-block;width:140px;'>",
                "Relief given?</div> <input type='checkbox' name='rg'/>").eP();

        htm.sP().addln("<div style='display:inline-block;width:140px;'>Original deadline:</div> ", origDate).eP();

        htm.sP().addln("<div style='display:inline-block;width:140px;'>",
                "New deadline:</div> <input type='date' name='dl'/>").eP();

        htm.sP().addln("<div style='display:inline-block;width:140px;'>",
                "Attempts allowed:</div> <input type='number' name='aa'/>").eP();

        htm.sP().addln("Circumstances:<br/><textarea name='ci' cols='80' rows='4' maxlength='200'></textarea>").eP();

        htm.sP().addln("Comment:<br/><textarea name='co' cols='80' rows='4' maxlength='200'></textarea>").eP();

        htm.sP().addln("<button class='btn' type='submit'>Submit</button> &nbsp; ")
                .addln("<a class='btn' href='student_schedule.html?stu=", student.stuId, "'>Cancel</a>").eP();

        htm.addln("</form>");
    }

    /**
     * Presents the form through which to enter an appeal.
     *
     * @param studentData the student data object
     * @param site        the site
     * @param req         the request
     * @param resp        the response
     * @param session     the user's login session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private static void processAppeal(final StudentData studentData, final AdminSite site,
                                      final ServletRequest req, final HttpServletResponse resp,
                                      final ImmutableSessionInfo session) throws IOException, SQLException {

        final String studentId = req.getParameter("stu");
        final String pace = req.getParameter("p");
        final String track = req.getParameter("t");
        final String order = req.getParameter("o");
        final String milestone = req.getParameter("m");
        final String type = req.getParameter("y");
        final String dl = req.getParameter("dl");
        final String aa = req.getParameter("aa");
        final String ci = req.getParameter("ci");
        final String co = req.getParameter("co");
        final String iv = req.getParameter("iv");

        if (AbstractSite.isParamInvalid(pace) || AbstractSite.isParamInvalid(track)
                || AbstractSite.isParamInvalid(order) || AbstractSite.isParamInvalid(milestone)
                || AbstractSite.isParamInvalid(type) || AbstractSite.isParamInvalid(dl)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  p='", pace, "'");
            Log.warning("  t='", track, "'");
            Log.warning("  o='", order, "'");
            Log.warning("  m='", milestone, "'");
            Log.warning("  y='", type, "'");
            Log.warning("  dl='", dl, "'");
            Log.warning("  aa='", aa, "'");
            Log.warning("  iv='", iv, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else if (studentId == null || pace == null || track == null || order == null || milestone == null
                || type == null) {
            doGet(studentData, site, req, resp, session);
        } else {
            final TermRec active = studentData.getActiveTerm();

            try {
                final Integer intPace = Integer.valueOf(pace);
                final Integer intMs = Integer.valueOf(milestone);

                // Find the original milestone record
                RawMilestone ms = null;
                final List<RawMilestone> all = studentData.getMilestones(active.term, intPace, track);

                for (final RawMilestone test : all) {
                    if (test.msNbr.equals(intMs) && test.msType.equals(type)) {
                        ms = test;
                        break;
                    }
                }

                final LocalDate newDate = LocalDate.parse(dl);

                if (ms != null) {
                    final String relief = req.getParameter("rg") == null ? "N" : "Y";
                    Integer attempts = null;
                    if (aa != null && !aa.isEmpty()) {
                        attempts = Integer.valueOf(aa);
                    }

                    final LocalDate now = LocalDate.now();
                    final RawPaceAppeals appeal = new RawPaceAppeals(active.term, studentId, now, relief,
                            intPace, track, intMs, type, ms.msDate, newDate, attempts, ci, co, iv);

                    final Cache cache = studentData.getCache();
                    RawPaceAppealsLogic.INSTANCE.insert(cache, appeal);
                    studentData.forgetDeadlineAppeals();

                    // If there is an existing STMILESTONE, update it (if the new deadline later)
                    // Otherwise, create new one.

                    RawStmilestone sms = null;
                    final List<RawStmilestone> allsms = studentData.getStudentMilestones(active.term, track);
                    for (final RawStmilestone test : allsms) {
                        if (test.msNbr.equals(intMs) && test.msType.equals(type)) {
                            sms = test;
                            break;
                        }
                    }

                    Integer allowed = null;
                    if (aa != null && !aa.isEmpty()) {
                        allowed = Integer.valueOf(aa);
                    }

                    if (sms == null) {
                        RawStmilestoneLogic.INSTANCE.insert(cache, new RawStmilestone(active.term, studentId, track,
                                intMs, type, newDate, allowed));
                    } else if (newDate.isAfter(sms.msDate)) {
                        RawStmilestoneLogic.update(cache, new RawStmilestone(active.term, studentId, track, intMs, type,
                                newDate, allowed));
                    } else {
                        Log.warning("New appeal for exam that has already been appealed, but with same or earlier ",
                                "deadline - not update StudentMilestone");
                    }
                    studentData.forgetStudentMilestones();
                }

                doGet(studentData, site, req, resp, session);
            } catch (final NumberFormatException | DateTimeParseException ex) {
                doGet(studentData, site, req, resp, session);
                Log.warning(ex);
            }
        }
    }
}
