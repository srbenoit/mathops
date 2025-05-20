package dev.mathops.dbjobs.report.cron;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.logic.PaceTrackLogic;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawStmilestoneLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStmilestone;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Generates a report of precalculus course progress for student athletes.
 */
public final class PrecalcProgressReport {

    /** The name of files to generate ('.txt' extension will be added). */
    private final String filename;

    /** The special_stus category used to select report population. */
    private final String category;

    /** The sub-header text for the report. */
    private final String subheader;

    /** The database profile through which to access the database. */
    private final Profile profile;

    /**
     * Constructs a new {@code PrecalcProgressReport}.
     *
     * @param theFilename  the name of files to generate ('.txt' extension will be added)
     * @param theCategory  the special_stus category used to select report population
     * @param theSubheader the subheader text for the report
     */
    public PrecalcProgressReport(final String theFilename, final String theCategory, final String theSubheader) {

        this.filename = theFilename;
        this.category = theCategory;
        this.subheader = theSubheader;

        final DatabaseConfig config = DatabaseConfig.getDefault();
        this.profile = config.getCodeProfile(Contexts.BATCH_PATH);
    }

    /**
     * Executes the job.
     */
    public void execute() {

        final Collection<String> report = new ArrayList<>(10);

        if (this.profile == null) {
            Log.warning("Unable to create production context.");
        } else {
            final Cache cache = new Cache(this.profile);

            try {
                execute(cache, report);
            } catch (final SQLException ex) {
                report.add("EXCEPTION: " + ex.getMessage());
            }
        }

        final File file1 = new File("/opt/zircon/reports/" + this.filename + ".txt");
        try (final FileWriter fw = new FileWriter(file1, StandardCharsets.UTF_8)) {
            for (final String rep : report) {
                fw.write(rep);
                fw.write(CoreConstants.CRLF);
            }
        } catch (final IOException ex) {
            Log.warning(ex);
        }

        Log.info("Report complete, written to: ", file1.getAbsolutePath());
    }

    /**
     * Executes the query against the ODS and loads data into the primary schema.
     *
     * @param cache  the data cache
     * @param report a list of strings to which to add report output lines
     * @throws SQLException if there is an error accessing the database
     */
    private void execute(final Cache cache, final Collection<? super String> report) throws SQLException {

        final LocalDate now = LocalDate.now();

        final int pad = Math.max(0, 36 - this.subheader.length() / 2);

        report.add("                      ** C O N F I D E N T I A L **");
        report.add("          COLORADO STATE UNIVERSITY - DEPARTMENT OF MATHEMATICS");
        report.add("                                     ".substring(0, pad) + this.subheader);
        report.add("                         Report Date:   " + TemporalUtils.FMT_MDY.format(now));
        report.add(CoreConstants.EMPTY);
        report.add(CoreConstants.EMPTY);

        // Get the list of students whose status to process (sorted by name)
        final List<RawStudent> students = gatherStudents(cache);

        for (final RawStudent stu : students) {
            processStudent(cache, stu, report);
        }
    }

    /**
     * Processes a single student record.
     *
     * @param cache the data cache
     * @param stu   the student record
     * @param rpt   a list of strings to which to add report output lines
     * @throws SQLException if there is an error accessing the database
     */
    private static void processStudent(final Cache cache, final RawStudent stu,
                                       final Collection<? super String> rpt) throws SQLException {

        final SystemData systemData = cache.getSystemData();

        TermKey activeKey = null;
        final TermRec active = systemData.getActiveTerm();
        if (active != null) {
            activeKey = active.term;
        }
        Log.info("Active term is ", activeKey);

        final List<RawStcourse> regs = RawStcourseLogic.queryByStudent(cache, stu.stuId, activeKey, false, false);

        // Remove "forfeit" courses and Incomplete courses not counted in pace
        regs.removeIf(next -> "G".equals(next.openStatus) || ("Y".equals(next.iInProgress)
                                                              && "N".equals(next.iCounted)));

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

        if (!ordered.isEmpty() && activeKey != null) {

            final int pace = PaceTrackLogic.determinePace(ordered);
            final Integer paceObj = Integer.valueOf(pace);
            final String track = PaceTrackLogic.determinePaceTrack(ordered, pace);

            final List<RawMilestone> milestones = systemData.getMilestones(activeKey, paceObj, track);

            final List<RawStmilestone> stmilestones = RawStmilestoneLogic.getStudentMilestones(cache, activeKey, track,
                    stu.stuId);
            stmilestones.sort(null);

            // Generate report

            final String name = (stu.lastName + ", " + stu.firstName + "                        ").substring(0, 26);

            final HtmlBuilder htm = new HtmlBuilder(100);
            htm.add(name, "  ", stu.stuId, "    ", stu.programCode, "    ", stu.stuEmail);
            rpt.add(htm.toString());
            rpt.add("------------------------------------------------------------------------------");
            rpt.add("  Pace   Order   Course   Unit   Item Due   Deadline    Completed?  On-time?");

            final LocalDate today = LocalDate.now();
            final LocalDate yesterday = today.minusDays(1L);

            final int size = ordered.size();
            for (int i = 0; i < size; ++i) {
                final int order = i + 1;
                final RawStcourse reg = ordered.get(i);

                final List<RawStexam> stexams = RawStexamLogic.getExams(cache, stu.stuId, reg.course, true, "R", "F");

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
                            // Don't break - student milestones are sorted by deadline date, and if there are multiple,
                            // we want the later date
                        }
                    }

                    LocalDate completed = null;
                    for (final RawStexam stexam : stexams) {
                        if (stexam.course.equals(reg.course) && stexam.unit.intValue() == unit
                            && "R".equals(stexam.examType) && "Y".equals(stexam.passed)) {
                            if (completed == null || completed.isAfter(stexam.examDt)) {
                                completed = stexam.examDt;
                            }
                        }
                    }

                    final boolean late = due != null && completed != null && completed.isAfter(due);

                    //

                    htm.reset();
                    htm.add("   ").add(pace).add("       ").add(order).add("     ").add(reg.course).add("      ")
                            .add(unit).add("       RE      ");

                    if (due == null) {
                        htm.add("Unknown   ");
                    } else {
                        htm.add(TemporalUtils.FMT_MDY_COMPACT_FIXED.format(due));
                    }

                    if (completed == null) {
                        if (due != null) {
                            if (yesterday.isAfter(due)) {
                                htm.add("                late");
                            } else if (today.isAfter(due)) {
                                htm.add("                DUE!");
                            }
                        }
                    } else {
                        htm.add("  ", TemporalUtils.FMT_MDY_COMPACT_FIXED.format(completed));
                        htm.add(late ? "    late" : "     OK");
                    }

                    rpt.add(htm.toString());
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
                        // Don't break - student milestones are sorted by deadline date, and if there are multiple, we
                        // want the later date
                    }
                }

                LocalDate completed = null;
                for (final RawStexam stexam : stexams) {
                    if (stexam.course.equals(reg.course) && stexam.unit.intValue() == 5
                        && "F".equals(stexam.examType) && "Y".equals(stexam.passed)) {
                        if (completed == null || completed.isAfter(stexam.examDt)) {
                            completed = stexam.examDt;
                        }
                    }
                }

                //

                htm.reset();
                htm.add("   ").add(pace).add("       ").add(order).add("     ").add(reg.course)
                        .add("      5       FE      ");

                if (due == null) {
                    htm.add("Unknown   ");
                } else {
                    htm.add(TemporalUtils.FMT_MDY_COMPACT_FIXED.format(due));
                }

                if (completed == null) {
                    if (due != null && today.isAfter(due)) {
                        htm.add("                DUE!");
                    }
                } else {
                    htm.add("  ", TemporalUtils.FMT_MDY_COMPACT_FIXED.format(completed));
                    if ("Y".equals(reg.completed)) {
                        htm.add("    passed!");
                    } else {
                        htm.add("    retesting");
                    }
                }

                rpt.add(htm.toString());
            }

            rpt.add(CoreConstants.EMPTY);
            rpt.add(CoreConstants.EMPTY);
        }
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

        final LocalDate today = LocalDate.now();
        final List<RawSpecialStus> specials = RawSpecialStusLogic.queryActiveByType(cache, this.category, today);

        final int count = specials.size();
        final List<RawStudent> students = new ArrayList<>(count);
        for (final RawSpecialStus spec : specials) {
            final RawStudent stu = RawStudentLogic.query(cache, spec.stuId, false);
            if (stu == null) {
                Log.warning("Student ", spec.stuId, " exists in SPECIAL_STUS but not in STUDENT");
            } else {
                students.add(stu);
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

        final PrecalcProgressReport job = new PrecalcProgressReport("athletes_summary",
                RawSpecialStus.ATHLETE, "PRECALCULUS PROGRESS REPORT FOR REGISTERED STUDENT ATHLETES");

//        final PrecalcProgressReport job =
//                new PrecalcProgressReport("engineering_summary", RawSpecialStus.ENGRSTU,
//                        "PRECALCULUS PROGRESS REPORT FOR REGISTERED ENGINEERING STUDENTS");

//        final PrecalcProgressReport job =
//                new PrecalcProgressReport("m116_summary", RawSpecialStus.M116,
//                        "PRECALCULUS PROGRESS REPORT FOR STUDENTS IN MATH 116");

        job.execute();
    }
}
