package dev.mathops.dbjobs.report;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.logic.PaceTrackLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawStmilestoneLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStmilestone;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates a report of course progress for students specified by a list of course/sections, or a list of student IDs.
 */
public final class HtmlCsvCourseProgressReport {

    /** The student IDs to include in the report (if null, all students are included). */
    private final List<String> studentIds;

    /** The course/sections to include (only course ID and section number are used; null includes all). */
    private final List<RawCsection> sections;

    /** The sub-header text for the report. */
    private final String subheader;

    /** The database profile through which to access the database. */
    private final Profile profile;

    /**
     * Constructs a new {@code HtmlCsvCourseProgressReport} with a list of student Ids and a list of
     *
     * @param theStudentIds the student IDs to include in the report (if null, all students are included)
     * @param theSections   the course/sections to include (only course ID and section number are used; null includes
     *                      all)
     * @param theSubhead    the sub-header text for the report
     */
    public HtmlCsvCourseProgressReport(final List<String> theStudentIds, final List<RawCsection> theSections,
                                       final String theSubhead) {

        this.studentIds = theStudentIds == null ? null : new ArrayList<>(theStudentIds);
        this.sections = theSections == null ? null : new ArrayList<>(theSections);
        this.subheader = theSubhead;

        final DatabaseConfig config = DatabaseConfig.getDefault();
        this.profile = config.getCodeProfile(Contexts.BATCH_PATH);
    }

    /**
     * Executes the job.
     *
     * @param report a collection to which to add HTML report lines
     * @param csv    a collection to which to add CSV report records
     */
    public void generate(final Collection<? super String> report, final Collection<? super String> csv) {

        if (this.profile == null) {
            Log.warning("Unable to find production profile.");
        } else {
            final Cache cache = new Cache(this.profile);
            try {
                execute(cache, report, csv);
            } catch (final SQLException ex) {
                final String msg = ex.getMessage();
                report.add("EXCEPTION: " + msg);
            }
        }
    }

    /**
     * Executes the query against the ODS and loads data into the primary schema.
     *
     * @param cache the data cache
     * @param html  a list of strings to which to add HTML report output lines
     * @param csv   a list of strings to which to add CSV report records
     * @throws SQLException if there is an error accessing the database
     */
    private void execute(final Cache cache, final Collection<? super String> html,
                         final Collection<? super String> csv) throws SQLException {

        final LocalDate now = LocalDate.now();

        html.add("<style>");
        html.add(".result-table {font-size:90%;}");
        html.add(
                ".result-table tr {display:grid; cursor:pointer; grid-template-columns: repeat(4, 1fr); " +
                "justify-content: flex-start;}");
        html.add(".result-table td:first-child:hover {cursor: default;background-color: #fff;}");
        html.add(".result-table tr:nth-child(2n+1) {background-color:#ddd; border-bottom:1px solid gray;}");
        html.add(".result-table tr:nth-child(2n+0) {background-color:#eee;}");
        html.add(".result-table tr th,td {padding-left: 6px; padding-right: 6px;}");
        html.add(
                ".expanded-row-content {display: grid; grid-column: 1/-1; justify-content: flex-start; " +
                "background-color:#fff;}");
        html.add(".hide-row {display: none;}");
        html.add("</style>");

        html.add("<style>");
        html.add(".inner-table {display:inline-block;}");
        html.add(".inner-table tr {display:table-row;}");
        html.add("</style>");

        html.add("<script>");
        html.add("const toggleRow = (element) => {");
        html.add("  element.getElementsByClassName('expanded-row-content')[0].classList.toggle('hide-row');");
        html.add("}");
        html.add("</script>");

        html.add("<p style='text-align:center; font-weight:bold;'>");
        html.add("** C O N F I D E N T I A L **<br/>");
        html.add("Colorado State University<br/>");
        html.add("Department of Mathematics<br/>");
        if (this.subheader != null) {
            html.add(this.subheader + "<br/>");
        }
        final String nowStr = TemporalUtils.FMT_MDY.format(now);
        html.add("Report Date: " + nowStr + "<br/>");
        html.add("</p>");

        html.add("<table class='result-table'>");
        html.add("<tr> <th>Name:</th> <th>Student ID</th> <th>Major:</th> <th>Email:</th></tr>");

        final Map<String, List<RawStcourse>> registrations = findRelevantRegistrations(cache);

        for (final Map.Entry<String, List<RawStcourse>> entry : registrations.entrySet()) {
            final String stuId = entry.getKey();
            final List<RawStcourse> regs = entry.getValue();
            // Remove "forfeit" courses and Incomplete courses not counted in pace
            regs.removeIf(r -> "G".equals(r.openStatus) || ("Y".equals(r.iInProgress) && "N".equals(r.iCounted)));
            processStudent(cache, stuId, regs, html);
        }

        html.add("</table>");
    }

    /**
     * Based on the specified list of students and sections, identifies the set of student registrations over which to
     * report.
     *
     * @return a map from student ID to the list of that student's registrations
     * @throws SQLException if there is an error accessing the database
     */
    private Map<String, List<RawStcourse>> findRelevantRegistrations(final Cache cache) throws SQLException {

        final Map<String, List<RawStcourse>> registrations = new HashMap<>(500);

        final TermRec active = cache.getSystemData().getActiveTerm();

        final List<RawStcourse> allRegs = RawStcourseLogic.queryByTerm(cache, active.term, false, false);

        for (final RawStcourse reg : allRegs) {
            if ((this.sections == null || isSectionIncluded(reg))
                && (this.studentIds == null || isStudentIncluded(reg))) {
                final String stuId = reg.stuId;
                final List<RawStcourse> inner = registrations.computeIfAbsent(stuId, s -> new ArrayList<>(5));
                inner.add(reg);
            }
        }

        return registrations;
    }

    /**
     * Called when {@code this.sections} is non-null to check whether a single registration matches any of the specified
     * course sections.
     *
     * @param reg the registration
     * @return true if included; false if not
     */
    private boolean isSectionIncluded(final RawStcourse reg) {

        boolean included = false;

        for (final RawCsection test : this.sections) {
            if (test.course.equals(reg.course) && test.sect.equals(reg.sect)) {
                included = true;
                break;
            }
        }

        return included;
    }

    /**
     * Called when {@code this.studentIds} is non-null to check whether a single registration matches any of the
     * specified student IDs.
     *
     * @param reg the registration
     * @return true if included; false if not
     */
    private boolean isStudentIncluded(final RawStcourse reg) {

        boolean included = false;

        for (final String test : this.studentIds) {
            if (test.equals(reg.stuId)) {
                included = true;
                break;
            }
        }

        return included;
    }

    /**
     * Processes a single student record.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @param regs  the list of student registrations
     * @param html  a collection to which to add HTML report lines
     * @throws SQLException if there is an error accessing the database
     */
    private static void processStudent(final Cache cache, final String stuId, final List<RawStcourse> regs,
                                       final Collection<? super String> html) throws SQLException {

        // Order them by "pace order" if present, by course number if not
        final List<RawStcourse> ordered = new ArrayList<>(regs.size());
        for (int order = 1; order <= 5; ++order) {
            final Integer orderKey = Integer.valueOf(order);
            boolean missing = true;
            for (int i = 0; i < regs.size(); ++i) {
                final RawStcourse reg = regs.get(i);
                if (orderKey.equals(reg.paceOrder)) {
                    ordered.add(reg);
                    regs.remove(reg);
                    missing = false;
                }
            }
            if (missing) {
                break;
            }
        }

        if (!regs.isEmpty()) {
            // Courses remain with no pace order (or with a gap in pace order) - sort what remains by course number
            final String[] courses = {RawRecordConstants.M117, RawRecordConstants.M118,
                    RawRecordConstants.M124, RawRecordConstants.M125, RawRecordConstants.M126};

            for (final String course : courses) {
                for (int i = 0; i < regs.size(); ++i) {
                    final RawStcourse reg = regs.get(i);
                    if (course.equals(reg.course)) {
                        ordered.add(reg);
                        regs.remove(reg);
                    }
                }
            }
        }

        if (!regs.isEmpty()) {
            // Should never happen - log warning if it does
            Log.warning("Precalc Progress Report: rows in stcourse were not sorted:");
            for (final RawStcourse row : regs) {
                Log.warning("    ", row.stuId, ": ", row.course, " sect ", row.sect);
            }
        }

        final SystemData systemData = cache.getSystemData();
        final TermRec activeTerm = systemData.getActiveTerm();
        final TermKey activeKey = activeTerm == null ? null : activeTerm.term;

        final RawStudent stu = ordered.isEmpty() || activeKey == null ? null
                : RawStudentLogic.query(cache, stuId, false);

        if (stu != null) {
            final int pace = PaceTrackLogic.determinePace(ordered);
            final Integer paceObj = Integer.valueOf(pace);
            final String track = PaceTrackLogic.determinePaceTrack(ordered, pace);

            final List<RawMilestone> milestones = systemData.getMilestones(activeKey, paceObj, track);

            final List<RawStmilestone> stmilestones = RawStmilestoneLogic.getStudentMilestones(cache, activeKey, track,
                    stu.stuId);
            stmilestones.sort(null);

            // Generate report
            final String rowHtml = SimpleBuilder.concat("<tr onClick='toggleRow(this)'>",
                    "<td>", stu.lastName, ", ", stu.firstName, "</td>",
                    "<td>", stu.stuId, "</td>",
                    "<td>", (stu.programCode == null ? CoreConstants.EMPTY : stu.programCode), "</td>",
                    "<td>", (stu.stuEmail == null ? CoreConstants.EMPTY : stu.stuEmail), "</td>",
                    "<td class='expanded-row-content hide-row'>");
            html.add(rowHtml);

            html.add("<table class='inner-table'>");
            html.add(" <tr><th>Pace</th> <th>Order</th> <th>Course</th> <th>Sect</th> <th>Unit</th> "
                     + "<th>Item</th> <th>Due</th> <th>Completed</th> <th>On time?</th></tr>");

            final LocalDate today = LocalDate.now();
            final LocalDate yesterday = today.minusDays(1L);

            final int size = ordered.size();
            for (int i = 0; i < size; ++i) {
                final int order = i + 1;
                final RawStcourse reg = ordered.get(i);

                final List<RawStexam> stexams =
                        RawStexamLogic.getExams(cache, stu.stuId, reg.course, true, "R", "F");

                // Unit review exams
                for (int unit = 1; unit <= 4; ++unit) {
                    final int msnbr = pace * 100 + order * 10 + unit;

                    LocalDate due = null;
                    for (final RawMilestone test : milestones) {
                        if (test.msNbr.intValue() == msnbr && "RE".equals(test.msType)) {
                            due = test.msDate;
                            break;
                        }
                    }
                    for (final RawStmilestone test : stmilestones) {
                        if (test.msNbr.intValue() == msnbr && "RE".equals(test.msType)) {
                            due = test.msDate;
                            // Don't break - student milestones are sorted by deadline date, and if there are
                            // multiple, we want
                            // the later date
                        }
                    }

                    LocalDate done = null;
                    for (final RawStexam stexam : stexams) {
                        if (stexam.course.equals(reg.course) && stexam.unit.intValue() == unit
                            && "R".equals(stexam.examType) && "Y".equals(stexam.passed)) {
                            if (done == null || done.isAfter(stexam.examDt)) {
                                done = stexam.examDt;
                            }
                        }
                    }

                    final boolean late = due != null && done != null && done.isAfter(due);
                    String lateMsg = CoreConstants.EMPTY;
                    if (done == null) {
                        if (due != null) {
                            if (yesterday.isAfter(due)) {
                                lateMsg = "late";
                            } else if (today.isAfter(due)) {
                                lateMsg = "DUE!";
                            }
                        }
                    } else if (late) {
                        lateMsg = "late";
                    } else {
                        lateMsg = "OK";
                    }

                    final String paceStr = Integer.toString(pace);
                    final String orderStr = Integer.toString(order);
                    final String unitStr = Integer.toString(unit);
                    final String dueStr = due == null ? "Unknown" : TemporalUtils.FMT_MDY_COMPACT_FIXED.format(due);
                    final String doneStr = done == null ? CoreConstants.EMPTY :
                            TemporalUtils.FMT_MDY_COMPACT_FIXED.format(
                                    done);

                    html.add(SimpleBuilder.concat(
                            " <tr><td>", (i == 0 && unit == 1 ? paceStr : CoreConstants.EMPTY), "</td> <td>",
                            (unit == 1 ? orderStr : CoreConstants.EMPTY), "</td> <td>",
                            (unit == 1 ? reg.course : CoreConstants.EMPTY), "</td> <td>",
                            (unit == 1 ? reg.sect : CoreConstants.EMPTY), "</td> <td>", unitStr,
                            "</td> <td>RE</td> <td>", dueStr, "</td> <td>", doneStr, "</td> <td>", lateMsg,
                            "</td></tr>"));
                }

                // Final exam
                final int msnbr = pace * 100 + order * 10 + 5;

                LocalDate due = null;
                for (final RawMilestone test : milestones) {
                    if (test.msNbr.intValue() == msnbr && "FE".equals(test.msType)) {
                        due = test.msDate;
                        break;
                    }
                }
                for (final RawStmilestone test : stmilestones) {
                    if (test.msNbr.intValue() == msnbr && "FE".equals(test.msType)) {
                        due = test.msDate;
                        // Don't break - student milestones are sorted by deadline date, and if there are multiple,
                        // we want
                        // the later date
                    }
                }

                LocalDate done = null;
                for (final RawStexam stexam : stexams) {
                    if (stexam.course.equals(reg.course) && stexam.unit.intValue() == 5
                        && "F".equals(stexam.examType) && "Y".equals(stexam.passed)) {
                        if (done == null || done.isAfter(stexam.examDt)) {
                            done = stexam.examDt;
                        }
                    }
                }

                String lateMsg = CoreConstants.EMPTY;
                if (done == null) {
                    if (due != null && today.isAfter(due)) {
                        lateMsg = "DUE!";
                    }
                } else if ("Y".equals(reg.completed)) {
                    lateMsg = "passed!";
                } else {
                    lateMsg = "retesting";
                }

                html.add(SimpleBuilder.concat(
                        " <tr><td></td> <td></td> <td></td> <td></td> <td>5</td> <td>FE</td> <td>",
                        (due == null ? "Unknown" : TemporalUtils.FMT_MDY_COMPACT_FIXED.format(due)), "</td> <td>",
                        (done == null ? CoreConstants.EMPTY : TemporalUtils.FMT_MDY_COMPACT_FIXED.format(done)),
                        "</td> <td>", lateMsg, "</td></tr>"));

                html.add("</table>");
            }

            html.add("</td></tr>");
        }
    }
}
