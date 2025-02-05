package dev.mathops.dbjobs.report.cron;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawlogic.RawMpeCreditLogic;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawlogic.RawStmpeLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.old.rawrecord.RawStmpe;
import dev.mathops.db.old.rawrecord.RawStudent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Generates a report of placement results for incoming engineering students based on a list provided by Engineering.
 */
public final class PlacementReport {

    /** The name of files to generate ('.txt' and '.csv' extensions will be added). */
    private final String filename;

    /** The special_stus category used to select report population. */
    private final String category;

    /** The list of student IDs on which to report. */
    private final Collection<String> studentIds;

    /** The database profile through which to access the database. */
    private final Profile profile;

    /**
     * Constructs a new {@code PlacementReport}.
     *
     * @param theFilename the name of files to generate ('.txt' and '.csv' extensions will be added)
     * @param theCategory the special_stus category used to select report population
     */
    public PlacementReport(final String theFilename, final String theCategory) {

        this.filename = theFilename;
        this.category = theCategory;
        this.studentIds = null;

        final DatabaseConfig config = DatabaseConfig.getDefault();
        this.profile = config.getCodeProfile(Contexts.BATCH_PATH);
    }

    /**
     * Executes the job.
     */
    public void execute() {

        final Collection<String> report = new ArrayList<>(10);
        final Collection<String> csv = new ArrayList<>(10);
        generate(report, csv);

        final File file1 = new File("/opt/zircon/reports/" + this.filename + ".txt");
        try (final FileWriter fw = new FileWriter(file1, StandardCharsets.UTF_8)) {
            for (final String rep : report) {
                fw.write(rep);
                fw.write(CoreConstants.CRLF);
            }
        } catch (final IOException ex) {
            Log.warning(ex);
        }

        final File file2 = new File("/opt/zircon/reports/" + this.filename + ".csv");
        try (final FileWriter fw = new FileWriter(file2, StandardCharsets.UTF_8)) {
            for (final String rep : csv) {
                fw.write(rep);
                fw.write(CoreConstants.CRLF);
            }
        } catch (final IOException ex) {
            Log.warning(ex);
        }

        Log.info("Report complete, written to: ", file1.getAbsolutePath(), " and ", file2.getAbsolutePath());
    }

    /**
     * Generates the report content.
     *
     * @param report a collection to which to add report lines
     * @param csv    a collection to which to add comma-separated values lines
     */
    public void generate(final Collection<String> report, final Collection<String> csv) {

        if (this.profile == null) {
            Log.warning("Unable to create production context.");
        } else {
            final Cache cache = new Cache(this.profile);

            try {
                final LocalDate now = LocalDate.now();

                report.add("                      ** C O N F I D E N T I A L **");
                report.add("                        COLORADO STATE UNIVERSITY");
                report.add("                        DEPARTMENT OF MATHEMATICS");
                report.add("                      MATHEMATICS PLACEMENT RESULTS");
                report.add("                         Report Date:   " + TemporalUtils.FMT_MDY.format(now));
                report.add(CoreConstants.EMPTY);
                report.add(CoreConstants.EMPTY);
                report.add("NAME                  STUDENT ID  RESULTS");
                report.add("---------------       ----------  ------------------------------------------");

                csv.add("Name," //
                        + "Student ID," //
                        + "MPT Attempts," //
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
                    processStudent(stu, cache, report, csv);
                }
            } catch (final SQLException ex) {
                report.add("EXCEPTION: " + ex.getMessage());
            }
        }
    }

    /**
     * Processes a single student record.
     *
     * @param stu    the student record
     * @param cache  the data cache
     * @param report a list of strings to which to add report output lines
     * @param csv    a list of strings to which to add tab-separated result records
     * @throws SQLException if there is an error accessing the database
     */
    private static void processStudent(final RawStudent stu, final Cache cache,
                                       final Collection<? super String> report,
                                       final Collection<? super String> csv) throws SQLException {

        final StringBuilder reportLine = new StringBuilder(100);
        final StringBuilder csvLine = new StringBuilder(100);

        String name = stu.lastName + ", " + stu.firstName;
        csvLine.append('"');
        csvLine.append(name);
        csvLine.append('"');
        csvLine.append(CoreConstants.COMMA_CHAR);
        csvLine.append(stu.stuId);
        csvLine.append(CoreConstants.COMMA_CHAR);

        if (name.length() > 20) {
            name = name.substring(0, 20);
        }
        reportLine.append(name);
        reportLine.append(" ".repeat(20 - name.length()));
        reportLine.append("  ");
        reportLine.append(stu.stuId);
        reportLine.append("   ");

        final StringBuilder results = new StringBuilder(100);

        // Count attempts
        final List<RawStmpe> attempts = RawStmpeLogic.queryLegalByStudent(cache, stu.stuId);
        final int numAttempts = attempts.size();

        csvLine.append(numAttempts);
        csvLine.append(CoreConstants.COMMA_CHAR);

        if (numAttempts == 0) {
            results.append("*** No Placement Tool Attempt ***");
            csvLine.append("no,no,no,no,no,no,no");
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
                csvLine.append("no,no,no,no,no,no,no");
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
                    csvLine.append("n/a,");
                    if (has117) {
                        csvLine.append("YES,");
                    } else {
                        csvLine.append("no,");
                    }
                    if (has118) {
                        csvLine.append("YES,");
                    } else {
                        csvLine.append("no,");
                    }
                    if (has124) {
                        csvLine.append("YES,");
                    } else {
                        csvLine.append("no,");
                    }
                    if (has125) {
                        csvLine.append("YES,");
                    } else {
                        csvLine.append("no,");
                    }
                    if (has126) {
                        csvLine.append("YES,");
                    } else {
                        csvLine.append("no,");
                    }
                    if (has124 && has126) {
                        csvLine.append("YES");
                    } else {
                        csvLine.append("no");
                    }
                } else {
                    results.setLength(0);

                    if (has100C) {
                        results.append("OK for MATH 101/105, STAT 100/201/204, and MATH 117/120/127");
                        csvLine.append("YES,no,no,no,no,no,no");
                    } else {
                        results.append("OK for MATH 101/105 and STAT 100/201/204 *only*");
                        csvLine.append("no,no,no,no,no,no,no");
                    }
                }
            }
        }
        reportLine.append(results);

        report.add(reportLine.toString());
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

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        // final PlacementReport job1 = new PlacementReport("engr_plc_results", "ENGRPLC");
        // job1.execute();

        final PlacementReport job2 = new PlacementReport("orient_plc_results", "ORIENTN");
        job2.execute();
    }
}
