package dev.mathops.dbjobs.report;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.TemporalUtils;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawlogic.RawMpeCreditLogic;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawlogic.RawStmpeLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.old.rawrecord.RawStmpe;
import dev.mathops.db.old.rawrecord.RawStudent;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
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

    /** The database profile through which to access the database. */
    private final DbProfile dbProfile;

    /** The Primary database context. */
    private final DbContext primaryCtx;

    /**
     * Constructs a new {@code PlacementReport}.
     *
     * @param theCategory the special_stus category used to select report population
     */
    public HtmlCsvPlacementReport(final String theCategory) {

        this.category = theCategory;
        this.studentIds = null;

        final ContextMap map = ContextMap.getDefaultInstance();
        this.dbProfile = map.getCodeProfile(Contexts.BATCH_PATH);
        this.primaryCtx = this.dbProfile.getDbContext(ESchemaUse.PRIMARY);
    }

    /**
     * Constructs a new {@code PlacementReport}.
     *
     * @param theStudentIds the list of student IDs on which to report
     */
    public HtmlCsvPlacementReport( final Collection<String> theStudentIds) {

        this.category = null;
        this.studentIds = theStudentIds;

        final ContextMap map = ContextMap.getDefaultInstance();
        this.dbProfile = map.getCodeProfile(Contexts.BATCH_PATH);
        this.primaryCtx = this.dbProfile.getDbContext(ESchemaUse.PRIMARY);
    }

    /**
     * Generates the report content.
     *
     * @param html a collection to which to add HTML report lines
     * @param csv  a collection to which to add comma-separated values lines
     */
    public void generate(final Collection<? super String> html, final Collection<? super String> csv) {

        if (this.dbProfile == null) {
            Log.warning("Unable to create production context.");
        } else if (this.primaryCtx == null) {
            Log.warning("Unable to create PRIMARY database context.");
        } else {
            try {
                final DbConnection conn = this.primaryCtx.checkOutConnection();
                final Cache cache = new Cache(this.dbProfile, conn);
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
                            + "MPT Attempts," //
                            + "First Attempt," //
                            + "Last Attempt," //
                            + "OK for 117/127," //
                            + "Out of 117," //
                            + "Out of 118," //
                            + "Out Of 124," //
                            + "Out Of 125," //
                            + "Out Of 126," //
                            + "Ready for 160");

                    // Get the list of students whose status to process (sorted by name)
                    final List<RawStudent> students = gatherStudents(cache);

                    for (final RawStudent stu : students) {
                        processStudent(stu, cache, html, csv);
                    }

                    html.add("</table>");
                } finally {
                    this.primaryCtx.checkInConnection(conn);
                }
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

        final HtmlBuilder reportLine = new HtmlBuilder(200);
        final HtmlBuilder csvLine = new HtmlBuilder(200);

        csvLine.add("\"", stu.lastName, ", ", stu.firstName, "\",", stu.stuId, ",");

        reportLine.add("<tr><td>", stu.lastName, ", ", stu.firstName, "</td><td>", stu.stuId, "</td>");

        final StringBuilder results = new StringBuilder(100);

        // Count attempts, track earliest and most recent attempt
        final List<RawStmpe> attempts = RawStmpeLogic.queryLegalByStudent(cache, stu.stuId);
        final int numAttempts = attempts.size();
        LocalDate firstAttempt = null;
        LocalDate lastAttempt = null;
        for (final RawStmpe attempt : attempts) {
            if (firstAttempt == null || firstAttempt.isAfter(attempt.examDt)) {
                firstAttempt = attempt.examDt;
            }
            if (lastAttempt == null || lastAttempt.isBefore(attempt.examDt)) {
                lastAttempt = attempt.examDt;
            }
        }

        final String firstAttemptDate = firstAttempt == null ? "N/A"
                : TemporalUtils.FMT_MDY_COMPACT_FIXED.format(firstAttempt);

        final String lastAttemptDate = lastAttempt == null ? "N/A"
                : TemporalUtils.FMT_MDY_COMPACT_FIXED.format(lastAttempt);

        csvLine.add(numAttempts);
        csvLine.add(CoreConstants.COMMA_CHAR);
        csvLine.add(firstAttemptDate);
        csvLine.add(CoreConstants.COMMA_CHAR);
        csvLine.add(lastAttemptDate);
        csvLine.add(CoreConstants.COMMA_CHAR);

        reportLine.add("<td>", Integer.toString(numAttempts), "</td><td>", firstAttemptDate, "</td><td>",
                lastAttemptDate, "</td>");

        if (numAttempts == 0) {
            results.append("*** No Placement Tool Attempt ***");
            csvLine.add("no,no,no,no,no,no,no");
        } else {
            final List<RawMpeCredit> mpecredlist =
                    RawMpeCreditLogic.queryByStudent(cache, stu.stuId);
            final Iterator<RawMpeCredit> iter = mpecredlist.iterator();
            while (iter.hasNext()) {
                final RawMpeCredit test = iter.next();
                final String placed = test.examPlaced;
                if ((!"P".equals(placed) && !"C".equals(placed))) {
                    iter.remove();
                }
            }

            if (mpecredlist.isEmpty()) {
                if (numAttempts == 1) {
                    results.append("*** 1 MPT attempt, no placement earned");
                } else {
                    results.append("*** ").append(numAttempts).append(" MPT attempts, no placement earned");
                }
                csvLine.add("no,no,no,no,no,no,no");
            } else {
                results.append("Placed out of MATH ");
                Collections.sort(mpecredlist);

                boolean comma = false;
                boolean has100C = false;
                boolean has117 = false;
                boolean has118 = false;
                boolean has124 = false;
                boolean has125 = false;
                boolean has126 = false;
                boolean hasOthers = false;
                for (final RawMpeCredit creditrow : mpecredlist) {
                    final String crs = creditrow.course;

                    if ("M 100C".equals(crs)) {
                        has100C = true;
                    } else {
                        if (RawRecordConstants.M117.equals(crs)) {
                            has117 = true;
                        } else if (RawRecordConstants.M118.equals(crs)) {
                            has118 = true;
                        } else if (RawRecordConstants.M124.equals(crs)) {
                            has124 = true;
                        } else if (RawRecordConstants.M125.equals(crs)) {
                            has125 = true;
                        } else if (RawRecordConstants.M126.equals(crs)) {
                            has126 = true;
                        }
                        hasOthers = true;
                        if (comma) {
                            results.append(", ");
                        }
                        results.append(crs.substring(2));
                        comma = true;
                    }
                }

                if (hasOthers) {
                    csvLine.add("n/a,");
                    if (has117) {
                        csvLine.add("YES,");
                    } else {
                        csvLine.add("no,");
                    }
                    if (has118) {
                        csvLine.add("YES,");
                    } else {
                        csvLine.add("no,");
                    }
                    if (has124) {
                        csvLine.add("YES,");
                    } else {
                        csvLine.add("no,");
                    }
                    if (has125) {
                        csvLine.add("YES,");
                    } else {
                        csvLine.add("no,");
                    }
                    if (has126) {
                        csvLine.add("YES,");
                    } else {
                        csvLine.add("no,");
                    }
                    if (has124 && has126) {
                        csvLine.add("YES");
                    } else {
                        csvLine.add("no");
                    }
                } else {
                    results.setLength(0);

                    if (has100C) {
                        results.append("OK for MATH 101, 105, 117, and 127");
                        csvLine.add("YES,no,no,no,no,no,no");
                    } else {
                        results.append("OK for MATH 101 & 105 *only*");
                        csvLine.add("no,no,no,no,no,no,no");
                    }
                }
            }
        }
        reportLine.add("<td>", results, "</td></tr>");

        html.add(reportLine.toString());
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
            students = new ArrayList<>(this.studentIds.size());

            for (final String id : this.studentIds) {
                final RawStudent stu = RawStudentLogic.query(cache, id, false);
                if (stu == null) {
                    Log.warning("Student ", id, " was not found");
                } else {
                    students.add(stu);
                }
            }
        }

        Collections.sort(students);

        return students;
    }
}
