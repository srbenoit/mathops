package dev.mathops.dbjobs.report;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawlogic.RawFfrTrnsLogic;
import dev.mathops.db.old.rawlogic.RawMpeCreditLogic;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStmpeLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawFfrTrns;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStmpe;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Generates a report (in either HTML format or as a list of comma-separated records) of placement results for students,
 * which can be specified either by a special student category or directly with a collection of student IDs.
 */
public final class HtmlCsvPlacementReport {

    /** The special_stus category used to select report population. */
    private final String category;

    /** The list of student IDs on which to report. */
    private final Collection<String> studentIds;

    private final ESortOrder sort;

    /** The database profile through which to access the database. */
    private final Profile profile;

    /**
     * Constructs a new {@code PlacementReport}.
     *
     * @param theCategory the special_stus category used to select report population
     * @param theSort     the sort order
     */
    public HtmlCsvPlacementReport(final String theCategory, final ESortOrder theSort) {

        this.category = theCategory;
        this.studentIds = null;
        this.sort = theSort;

        final DatabaseConfig config = DatabaseConfig.getDefault();
        this.profile = config.getCodeProfile(Contexts.BATCH_PATH);
    }

    /**
     * Constructs a new {@code PlacementReport}.
     *
     * @param theStudentIds the list of student IDs on which to report
     * @param theSort       the sort order
     */
    public HtmlCsvPlacementReport(final Collection<String> theStudentIds, final ESortOrder theSort) {

        this.category = null;
        this.studentIds = theStudentIds;
        this.sort = theSort;

        final DatabaseConfig config = DatabaseConfig.getDefault();
        this.profile = config.getCodeProfile(Contexts.BATCH_PATH);
    }

    /**
     * Generates the report content.
     *
     * @param html a collection to which to add HTML report lines
     * @param csv  a collection to which to add comma-separated values lines
     */
    public void generate(final Collection<? super String> html, final Collection<? super String> csv) {

        if (this.profile == null) {
            Log.warning("Unable to find production profile.");
        } else {
            final Cache cache = new Cache(this.profile);

            try {
                final LocalDate now = LocalDate.now();

                html.add("<style>");
                html.add(".result-table {font-size:90%;}");
                html.add(".result-table tr:nth-child(2n+1) {background-color:#ddd; border-bottom:1px solid gray;}");
                html.add(".result-table tr:nth-child(2n+0) {background-color:#eee;}");
                html.add(".result-table tr th, td {padding-left: 6px; padding-right: 6px;}");
                html.add("</style>");

                html.add("<p style='text-align:center; font-weight:bold;'>");
                html.add("** C O N F I D E N T I A L **<br/>");
                html.add("Colorado State University<br/>");
                html.add("Department of Mathematics<br/>");
                html.add("Math Placement Tool Status and Results<br/>");
                html.add("Report Date: " + TemporalUtils.FMT_MDY.format(now) + "<br/>");
                html.add("</p>");

                html.add("<table class='result-table'>");
                html.add("<tr> <th>Name:</th> <th>Student ID</th> <th>Attempts:</th> <th>First:</th> "
                         + "<th>Latest:</th> <th>Results:</th> </tr>");

                csv.add("Name," //
                        + "Student ID," //
                        + "Applic. Term," //
                        + "Applic. Year," //
                        + "MPT Attempts," //
                        + "First Attempt," //
                        + "Last Attempt," //
                        + "OK for 117/127," //
                        + "Out of 117," //
                        + "Out of 118," //
                        + "Out Of 124," //
                        + "Out Of 125," //
                        + "Out Of 126," //
                        + "Eligible for 160");

                // Get the list of students whose status to process (sorted by name)
                final List<RawStudent> students = gatherStudents(cache);

                for (final RawStudent stu : students) {
                    processStudent(stu, cache, html, csv);
                }

                html.add("</table>");
            } catch (final SQLException ex) {
                html.add("EXCEPTION: " + ex.getMessage());
            }
        }
    }

    /**
     * Processes a single student record.
     *
     * @param stu   the student record
     * @param cache the data cache
     * @param html  a collection to which to add HTML report output lines
     * @param csv   a collection to which to add tab-separated result records
     * @throws SQLException if there is an error accessing the database
     */
    private static void processStudent(final RawStudent stu, final Cache cache,
                                       final Collection<? super String> html,
                                       final Collection<? super String> csv) throws SQLException {

        final List<RawStcourse> regs = RawStcourseLogic.queryByStudent(cache, stu.stuId, true, false);
        final List<RawFfrTrns> transfer = RawFfrTrnsLogic.queryByStudent(cache, stu.stuId);
        final List<RawMpeCredit> mpeCred = RawMpeCreditLogic.queryByStudent(cache, stu.stuId);
        Collections.sort(mpeCred);

        String howPlacedInto117 = null;
        String how117Satisfied = null;
        String how118Satisfied = null;
        String how124Satisfied = null;
        String grade124 = null;
        String how125Satisfied = null;
        String how126Satisfied = null;
        String grade126 = null;

        for (final RawStcourse reg : regs) {
            if (!"Y".equals(reg.completed)) {
                continue;
            }
            final String grade = reg.courseGrade;
            if ("A".equals(grade) || "B".equals(grade) || "C".equals(grade) || "D".equals(grade) || "S".equals(grade)) {
                final String course = reg.course;

                if (RawRecordConstants.M117.equals(course)) {
                    how117Satisfied = "By Course";
                } else if (RawRecordConstants.M118.equals(course)) {
                    how118Satisfied = "By Course";
                } else if (RawRecordConstants.M124.equals(course)) {
                    how124Satisfied = "By Course";
                    grade124 = grade;
                } else if (RawRecordConstants.M125.equals(course) || RawRecordConstants.MATH125.equals(course)) {
                    how125Satisfied = "By Course";
                } else if (RawRecordConstants.M126.equals(course) || RawRecordConstants.MATH126.equals(course)) {
                    how126Satisfied = "By Course";
                    grade126 = grade;
                }
            }
        }

        for (final RawFfrTrns trns : transfer) {
            final String course = trns.course;

            if (RawRecordConstants.M002.equals(course) || "M 055".equals(course) || "M 099".equals(course)
                || RawRecordConstants.M100C.equals(course)) {
                howPlacedInto117 = "By Xfer";
            } else if (RawRecordConstants.M117.equals(course)) {
                if (how117Satisfied == null) {
                    how117Satisfied = "By Xfer";
                }
            } else if (RawRecordConstants.M118.equals(course)) {
                if (how118Satisfied == null) {
                    how118Satisfied = "By Xfer";
                }
            } else if (RawRecordConstants.M124.equals(course)) {
                if (how124Satisfied == null) {
                    how124Satisfied = "By Xfer";
                    grade124 = "?";
                }
            } else if (RawRecordConstants.M125.equals(course)) {
                if (how125Satisfied == null) {
                    how125Satisfied = "By Xfer";
                }
            } else if (RawRecordConstants.M126.equals(course)) {
                if (how126Satisfied == null) {
                    how126Satisfied = "By Xfer";
                    grade126 = "?";
                }
            }
        }

        boolean placedInto117 = false;
        boolean placedOut117 = false;
        boolean placedOut118 = false;
        boolean placedOut124 = false;
        boolean placedOut125 = false;
        boolean placedOut126 = false;

        for (final RawMpeCredit creditrow : mpeCred) {
            final String crs = creditrow.course;

            if ("M 100C".equals(crs)) {
                placedInto117 = true;
                if (howPlacedInto117 == null) {
                    howPlacedInto117 = "By MPT";
                }
            } else if (RawRecordConstants.M117.equals(crs)) {
                placedOut117 = true;
                if (how117Satisfied == null) {
                    how117Satisfied = "By MPT";
                }
            } else if (RawRecordConstants.M118.equals(crs)) {
                placedOut118 = true;
                if (how118Satisfied == null) {
                    how118Satisfied = "By MPT";
                }
            } else if (RawRecordConstants.M124.equals(crs)) {
                placedOut124 = true;
                if (how124Satisfied == null) {
                    how124Satisfied = "By MPT";
                    grade124 = "P";
                } else if ("C".equals(grade124) || "D".equals(grade124) || "S".equals(grade124)) {
                    grade124 = "P";
                }
            } else if (RawRecordConstants.M125.equals(crs)) {
                placedOut125 = true;
                if (how125Satisfied == null) {
                    how125Satisfied = "By MPT";
                }
            } else if (RawRecordConstants.M126.equals(crs)) {
                placedOut126 = true;
                if (how126Satisfied == null) {
                    how126Satisfied = "By MPT";
                    grade126 = "P";
                } else if ("C".equals(grade126) || "D".equals(grade126) || "S".equals(grade126)) {
                    grade126 = "P";
                }
            }
        }

        // Count attempts, track earliest and most recent attempt
        final List<RawStmpe> attempts = RawStmpeLogic.queryLegalByStudent(cache, stu.stuId);
        final int numAttempts = attempts.size();
        LocalDate firstTry = null;
        LocalDate lastTry = null;
        for (final RawStmpe attempt : attempts) {
            if (firstTry == null || firstTry.isAfter(attempt.examDt)) {
                firstTry = attempt.examDt;
            }
            if (lastTry == null || lastTry.isBefore(attempt.examDt)) {
                lastTry = attempt.examDt;
            }
        }

        final String firstTryDate = firstTry == null ? "N/A" : TemporalUtils.FMT_MDY_COMPACT_FIXED.format(firstTry);
        final String lastTryDate = lastTry == null ? "N/A" : TemporalUtils.FMT_MDY_COMPACT_FIXED.format(lastTry);

        //
        // Generate report record
        //
        final HtmlBuilder reportLine = new HtmlBuilder(200);

        final String numAttemptsStr = Integer.toString(numAttempts);
        reportLine.add("<tr><td>", stu.lastName, ", ", stu.firstName, "</td><td>", stu.stuId, "</td><td>",
                numAttemptsStr, "</td><td>", firstTryDate, "</td><td>", lastTryDate, "</td><td>");

        if (numAttempts == 0) {
            reportLine.add("*** No MPT Attempt ***");
        } else if (placedInto117) {
            if (placedOut117) {
                if (placedOut118) {
                    if (placedOut124) {
                        if (placedOut125) {
                            if (placedOut126) {
                                reportLine.add("Placed out of MATH 117,118,124,125,126");
                            } else {
                                reportLine.add("Placed out of MATH 117,118,124,125");
                            }
                        } else {
                            reportLine.add("Placed out of MATH 117,118,124");
                        }
                    } else if (placedOut125) {
                        if (placedOut126) {
                            reportLine.add("Placed out of MATH 117,118,125,126");
                        } else {
                            reportLine.add("Placed out of MATH 117,118,125");
                        }
                    } else {
                        reportLine.add("Placed out of MATH 117,118");
                    }
                } else {
                    reportLine.add("Placed out of MATH 117");
                }
            } else {
                reportLine.add("OK for MATH 101/105, STAT 100/201/204, and MATH 117/120/127");
            }
        } else {
            reportLine.add("OK for MATH 101/105 and STAT 100/201/204 only");
        }
        reportLine.add("</td></tr>");

        final String reportLineStr = reportLine.toString();
        html.add(reportLineStr);

        //
        // Generate CSV file record
        //

        final HtmlBuilder csvLine = new HtmlBuilder(200);

        csvLine.add("\"", stu.lastName, ", ", stu.firstName, "\",");
        csvLine.add(stu.stuId);
        csvLine.add(CoreConstants.COMMA_CHAR);
        csvLine.add(stu.aplnTerm.termCode);
        csvLine.add(CoreConstants.COMMA_CHAR);
        csvLine.add(stu.aplnTerm.year);
        csvLine.add(CoreConstants.COMMA_CHAR);
        csvLine.add(numAttempts);
        csvLine.add(CoreConstants.COMMA_CHAR);
        csvLine.add(firstTryDate);
        csvLine.add(CoreConstants.COMMA_CHAR);
        csvLine.add(lastTryDate);
        csvLine.add(CoreConstants.COMMA_CHAR);

        if (howPlacedInto117 == null) {
            csvLine.add("no,");
        } else {
            csvLine.add(howPlacedInto117, CoreConstants.COMMA);
        }
        if (how117Satisfied == null) {
            csvLine.add("no,");
        } else {
            csvLine.add(how117Satisfied, CoreConstants.COMMA);
        }
        if (how118Satisfied == null) {
            csvLine.add("no,");
        } else {
            csvLine.add(how118Satisfied, CoreConstants.COMMA);
        }
        if (how124Satisfied == null) {
            csvLine.add("no,");
        } else {
            csvLine.add(how124Satisfied, CoreConstants.COMMA);
        }
        if (how125Satisfied == null) {
            csvLine.add("no,");
        } else {
            csvLine.add(how125Satisfied, CoreConstants.COMMA);
        }
        if (how126Satisfied == null) {
            csvLine.add("no,");
        } else {
            csvLine.add(how126Satisfied, CoreConstants.COMMA);
        }

        // Ready for 160?
        if (how124Satisfied == null || how126Satisfied == null) {
            csvLine.add("no");
        } else {
            final boolean b124 = "A".equals(grade124) || "B".equals(grade124) || "P".equals(grade124);
            final boolean b126 = "A".equals(grade126) || "B".equals(grade126) || "P".equals(grade126);

            if (b124 && b126) {
                csvLine.add("YES");
            } else if (b124) {
                csvLine.add("no (126 grade)");
            } else if (b126) {
                csvLine.add("no (124 grade)");
            } else {
                csvLine.add("no (124 and 126 grades)");
            }
        }

        csv.add(csvLine.toString());
    }

    /**
     * Gathers the list of students to process by querying the SPECIAL_STUS table for records with the specified special
     * category, then accumulating a list of the corresponding STUDENT records.
     *
     * @param cache the data cache
     * @return the list of students, sorted by last name, first name, initial, preferred name, then student ID
     * @throws SQLException if there is an error accessing the database
     */
    private List<RawStudent> gatherStudents(final Cache cache) throws SQLException {

        final List<RawStudent> students;

        if (this.studentIds == null) {
            final LocalDate today = LocalDate.now();
            final List<RawSpecialStus> specials = RawSpecialStusLogic.queryActiveByType(cache, this.category, today);

            students = new ArrayList<>(specials.size());
            for (final RawSpecialStus spec : specials) {
                final RawStudent stu = RawStudentLogic.query(cache, spec.stuId, false);
                if (stu == null) {
                    Log.warning("Student ", spec.stuId, " exists in SPECIAL_STUS but not in STUDENT");
                } else {
                    students.add(stu);
                }
            }
        } else {
            final int size = this.studentIds.size();
            students = new ArrayList<>(size);

            for (final String id : this.studentIds) {
                final RawStudent stu = RawStudentLogic.query(cache, id, false);
                if (stu == null) {
                    Log.warning("Student ", id, " was not found");
                    if (this.sort == ESortOrder.PRESERVE_ORDER) {
                        final RawStudent fakeStudent = new RawStudent();
                        fakeStudent.stuId = id;
                        fakeStudent.lastName = "*** INVALID ***";
                        fakeStudent.firstName = CoreConstants.EMPTY;
                        students.add(fakeStudent);
                    }
                } else {
                    students.add(stu);
                }
            }
        }

        if (this.sort == ESortOrder.LAST_NAME) {
            // Last name is the default sort for the "RawStudent" class
            students.sort(null);
        } else if (this.sort == ESortOrder.CSUID) {
            // Last name is the default sort for the "RawStudent" class
            students.sort(RawStudent.CSUID_COMPARATOR);
        }

        return students;
    }
}
