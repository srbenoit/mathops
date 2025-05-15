package dev.mathops.dbjobs.eos.grading;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.installation.EPath;
import dev.mathops.commons.installation.PathList;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawlogic.RawCsectionLogic;
import dev.mathops.db.old.rawlogic.RawDontSubmitLogic;
import dev.mathops.db.old.rawlogic.RawParametersLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawSthomeworkLogic;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawDontSubmit;
import dev.mathops.db.old.rawrecord.RawParameters;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A utility to generate the file that gets uploaded to Banner to submit dates of last attendance for grades of U or F.
 *
 * <p>
 * NOTE: according to Student Financial Services (SFS), the User's Exam can be counted as an academic event, but ONLY
 * for the first course in which the student is enrolled.  Iif the student completed another course, it CANNOT be
 * counted for the F/U course.
 */
public class CreateLastAttendanceFile implements Runnable {

    /** The data cache. */
    private final Cache cache;

    /** The report path. */
    private final File reportPath;

    /**
     * Constructs a new {@code CreateLastAttendanceFile}.
     *
     * @param theCache      the data cache
     * @param theReportPath the report path
     */
    private CreateLastAttendanceFile(final Cache theCache, final File theReportPath) {

        this.cache = theCache;
        this.reportPath = theReportPath;
    }

    /**
     * Runs the process.
     */
    public void run() {

        Log.info("Generating final attendance date file...");

        final SystemData systemData = this.cache.getSystemData();

        try {
            final TermRec active = systemData.getActiveTerm();

            if (active == null) {
                Log.warning("ERROR: Unable to query the active term");
            } else {
                final RawParameters parameters = getParameters();
                if (parameters != null) {
                    final List<RawCsection> sections = getSections(active.term);
                    if (sections != null) {
                        final List<RawStcourse> gradesToSubmit = getRegistrations(active.term);
                        generateFile(parameters, gradesToSubmit, sections);
                    }
                }
            }
        } catch (final SQLException ex) {
            Log.warning("ERROR: Unable to query the active term", ex);
        }
    }

    /**
     * Retrieves the "parameters" record with information for grade submission.
     *
     * @return the parameters record; null on failure
     */
    private RawParameters getParameters() {

        RawParameters result = null;

        try {
            final RawParameters row = RawParametersLogic.query(this.cache, "FINALGRADES");

            if (row.parm1 == null) {
                Log.warning("ERROR: Parameters row missing parameter 1.");
            } else if (row.parm2 == null) {
                Log.warning("ERROR: Parameters row missing parameter 2.");
            } else if (row.parm3 == null) {
                Log.warning("ERROR: Parameters row missing parameter 3.");
            } else {
                result = row;
            }
        } catch (final SQLException ex) {
            Log.warning("ERROR: Failed to query for parameters for FINALGRADES.", ex);
        }

        return result;
    }

    /**
     * Gathers a list of course section records for the active term that are not flagged as "Bogus".
     *
     * @param activeTermKey the active term key
     * @return the list of registrations
     */
    private List<RawCsection> getSections(final TermKey activeTermKey) {

        List<RawCsection> result = null;

        try {
            final List<RawCsection> all = RawCsectionLogic.queryByTerm(this.cache, activeTermKey);
            final int size = all.size();
            result = new ArrayList<>(size);
            for (final RawCsection row : all) {
                if ("N".equals(row.bogus)) {
                    result.add(row);
                }
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to query course section records for the active term.", ex);
        }

        return result;
    }

    /**
     * Builds the list of registrations for which grades are to be submitted.  This omits sections that are in the
     * DONT_SUBMIT table for the active term.
     *
     * @param activeTermKey the active term key
     * @return the list of registrations
     */
    private List<RawStcourse> getRegistrations(final TermKey activeTermKey) {

        List<RawStcourse> result = null;

        try {
            final List<RawDontSubmit> dontSubmit = RawDontSubmitLogic.queryByTerm(this.cache, activeTermKey);
            for (final RawDontSubmit dont : dontSubmit) {
                Log.info("Don't submit in course: ", dont.course, " sect ", dont.sect);
            }

            try {
                final List<RawStcourse> regs = RawStcourseLogic.queryByTerm(this.cache, activeTermKey, true, false);
                result = filterRegs(regs, dontSubmit);
            } catch (final SQLException ex) {
                Log.warning("Failed to query STUDENT_COURSE records for the active term.");
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to query DONT_SUBMIT records for the active term.");
        }

        return result;
    }

    /**
     * Given a list of registrations and a list of sections for which grades are NOT to be submitted, generates a
     * filtered list of registrations with a grade of "F" or "U", that are not in the "don't submit" set, and that have
     * final_class_roll = 'Y' and where i_in_progress is not 'Y' and open status is not "D".
     *
     * @param regs       the list of all registrations to search
     * @param dontSubmit the list of sections for which grades are not to be submitted
     * @return the filtered list of registrations
     */
    private static List<RawStcourse> filterRegs(final Collection<RawStcourse> regs,
                                                final Iterable<RawDontSubmit> dontSubmit) {

        final int size = regs.size();
        final List<RawStcourse> result = new ArrayList<>(size);

        int numConsidered = 0;
        int numExcluded = 0;
        for (final RawStcourse reg : regs) {
            if ("Y".equals(reg.iInProgress) || "D".equals(reg.openStatus)) {
                continue;
            }
            if ("Y".equals(reg.finalClassRoll)) {
                final String grade = reg.courseGrade;
                if ("F".equals(grade) || "U".equals(grade)) {
                    ++numConsidered;
                    boolean excluded = false;
                    for (final RawDontSubmit dont : dontSubmit) {
                        if (dont.course.equals(reg.course) && dont.sect.equals(reg.sect)) {
                            excluded = true;
                            break;
                        }
                    }
                    if (excluded) {
                        ++numExcluded;
                    } else {
                        result.add(reg);
                    }
                }
            }
        }

        // Sort the list by student then course (the built-in ordering for RawStcourse)
        result.sort(null);

        final String numConsideredStr = Integer.toString(numConsidered);
        Log.info("Considered ", numConsideredStr, " registrations");

        final String numExcludedStr = Integer.toString(numExcluded);
        Log.info("Excluded ", numExcludedStr, " registrations based on DONT_SUBMIT records");

        final int numIncluded = result.size();
        final String numIncludedStr = Integer.toString(numIncluded);
        Log.info("Result: ", numIncludedStr, " registrations will be screened for inclusion in output file.");

        return result;
    }

    /**
     * Generates the API file.
     *
     * @param parameters     the parameters for the FINAL GRADING program
     * @param gradesToSubmit the list of registrations with grades to submit
     * @param sections       the list of course section records
     */
    private void generateFile(final RawParameters parameters,
                              final List<RawStcourse> gradesToSubmit, final List<RawCsection> sections) {

        final String instr = parameters.parm1.trim();
        final String instrId = parameters.parm2.trim();
        final String termCode = parameters.parm3.trim();

        final HtmlBuilder htm = new HtmlBuilder(1000);

        String lastStuId = "x";
        List<RawStexam> exams = null;
        List<RawSthomework> homeworks = null;

        boolean newline = false;
        int count = 0;
        for (final RawStcourse reg : gradesToSubmit) {
            RawCsection sect = null;
            for (final RawCsection test : sections) {
                if (test.course.equals(reg.course) && test.sect.equals(reg.sect)) {
                    sect = test;
                    break;
                }
            }

            if (sect == null) {
                Log.warning("ERROR: Failed to find course section for ", reg.course, " sect ", reg.sect);
            } else if (reg.courseGrade == null) {
                Log.warning("ERROR: Grade is null for ", reg.stuId, " in ", reg.course, " sect ", reg.sect);
            } else {
                LocalDate last = sect.ariesStartDt;
                boolean attended = false;

                final String crn = sect.sectionId.trim();

                if (reg.stuId.equals(lastStuId)) {
                    // This is not the first course for the student, consider only exams in the course
                    if (exams != null && homeworks != null) {
                        for (final RawStexam exam : exams) {
                            if (reg.course.equals(exam.course)) {
                                if (last.isBefore(exam.examDt)) {
                                    last = exam.examDt;
                                    attended = true;
                                }
                            }
                        }
                        for (final RawSthomework homework : homeworks) {
                            if (reg.course.equals(homework.course)) {
                                if (last.isBefore(homework.hwDt)) {
                                    last = homework.hwDt;
                                    attended = true;
                                }
                            }
                        }
                    }
                } else {
                    exams = getStudentExams(this.cache, reg.stuId);
                    homeworks = getStudentHomrework(this.cache, reg.stuId);

                    // This is the first course for the student, allow the User's exam to count as an activity
                    if (exams != null && homeworks != null) {
                        for (final RawStexam exam : exams) {
                            if (reg.course.equals(exam.course) || "M 100U".equals(exam.course)) {
                                if (last.isBefore(exam.examDt)) {
                                    last = exam.examDt;
                                    attended = true;
                                }
                            }
                        }
                        for (final RawSthomework homework : homeworks) {
                            if (reg.course.equals(homework.course)) {
                                if (last.isBefore(homework.hwDt)) {
                                    last = homework.hwDt;
                                    attended = true;
                                }
                            }
                        }
                    }

                    lastStuId = reg.stuId;
                }

                // It is possible a section lets student test after the end date - if that happens, set the last
                // attendance date back to the Aries end date
                if (last.isAfter(sect.ariesEndDt)) {
                    last = sect.ariesEndDt;
                }

                if (newline) {
                    htm.addln();
                }

                final String attendedStr = attended ? "1" : "0";
                final String lastStr = TemporalUtils.FMT_MDY_COMPACT_FIXED.format(last);
                htm.add(reg.stuId, ",", termCode, ",", crn, ",", lastStr, ",", attendedStr);
                ++count;
                newline = true;
            }
        }

        final String reportString = htm.toString();

        final File reportFile = new File(this.reportPath, "AREGORLA.AREGS519_01.DAT");
        final String reportPath = reportFile.getAbsolutePath();

        try (final FileWriter writer = new FileWriter(reportFile)) {
            writer.write(reportString);
            final String countStr = Integer.toString(count);
            Log.info("Emitted ", countStr, " records to last attendance file ", reportPath);
        } catch (final IOException ex) {
            Log.warning("ERROR: failed to write ", reportPath, ex);
        }
    }

    /**
     * Queries student exams for a student.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return the list of student exam records
     */
    private List<RawStexam> getStudentExams(final Cache cache, final String stuId) {

        List<RawStexam> result = null;

        try {
            result = RawStexamLogic.queryByStudent(cache, stuId, true);
        } catch (final SQLException ex) {
            Log.warning("ERROR: failed to query student exams for ", stuId);
        }

        return result;
    }

    /**
     * Queries student homework records for a student.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return the list of student homework records
     */
    private List<RawSthomework> getStudentHomrework(final Cache cache, final String stuId) {

        List<RawSthomework> result = null;

        try {
            result = RawSthomeworkLogic.queryByStudent(cache, stuId, true);
        } catch (final SQLException ex) {
            Log.warning("ERROR: failed to query student homeworks for ", stuId);
        }

        return result;
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        final File reportPath = PathList.getInstance().get(EPath.REPORT_PATH);
        if (reportPath == null) {
            Log.warning("ERROR: Unable to determine report path.");
        } else {

            DbConnection.registerDrivers();

            final DatabaseConfig config = DatabaseConfig.getDefault();
            final Profile profile = config.getCodeProfile(Contexts.BATCH_PATH);
            final Cache cache = new Cache(profile);

            final Runnable obj = new CreateLastAttendanceFile(cache, reportPath);
            obj.run();
        }
    }
}