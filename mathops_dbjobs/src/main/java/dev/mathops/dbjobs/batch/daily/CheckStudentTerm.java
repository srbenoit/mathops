package dev.mathops.dbjobs.batch.daily;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.logic.PaceTrackLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawSttermLogic;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.rec.TermRec;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Scans all active registrations in the database, and all existing STTERM records, and scans for any STTERM records
 * that are inconsistent with the registration data.
 */
public final class CheckStudentTerm {

    /** A commonly used string. */
    private static final String STUDENT = "  Student ";

    /** True to print actions but make no data changes. */
    private static final boolean DEBUG = false;

    /** The database profile through which to access the database. */
    private final Profile profile;

    /**
     * Constructs a new {@code CheckStudentTerm}.
     */
    public CheckStudentTerm() {

        final DatabaseConfig config = DatabaseConfig.getDefault();
        this.profile = config.getCodeProfile(Contexts.BATCH_PATH);
    }

    /**
     * Executes the job.
     *
     * @return the report
     */
    public String execute() {

        final Collection<String> report = new ArrayList<>(10);

        if (this.profile == null) {
            report.add("Unable to create production profile.");
        } else {
            final Cache cache = new Cache(this.profile);

            try {
                final TermRec active = cache.getSystemData().getActiveTerm();
                report.add("Active term is " + active.term.longString + ".");

                final List<RawStcourse> allRegs = RawStcourseLogic.queryByTerm(cache, active.term, false, false);
                report.add("Queried " + allRegs.size() + " active registrations.");

                final Map<String, List<RawStcourse>> stuRegs =
                        new HashMap<>(allRegs.size() / 2);
                for (final RawStcourse reg : allRegs) {
                    // Remove non-counted incompletes and "ignored"
                    if ("G".equals(reg.openStatus) || ("Y".equals(reg.iInProgress) && "N".equals(reg.iCounted))) {
                        continue;
                    }

                    final String studentId = reg.stuId;
                    final List<RawStcourse> inner = stuRegs.computeIfAbsent(studentId, s -> new ArrayList<>(5));
                    inner.add(reg);
                }
                report.add("Found " + stuRegs.size() + " distinct students with registrations.");

                final List<RawStterm> allStTerms = RawSttermLogic.queryAllByTerm(cache, active.term);

                report.add("Queried " + allStTerms.size() + " student term records.");
                report.add(CoreConstants.EMPTY);

                // For all students that have registrations, check their record
                for (final Map.Entry<String, List<RawStcourse>> entry : stuRegs.entrySet()) {
                    final String studentId = entry.getKey();
                    final List<RawStcourse> regs = entry.getValue();

                    final int pace = PaceTrackLogic.determinePace(regs);
                    final String track = PaceTrackLogic.determinePaceTrack(regs, pace);
                    final String first = PaceTrackLogic.determineFirstCourse(regs);

                    RawStterm existing = null;
                    for (final RawStterm test : allStTerms) {
                        if (test.stuId.equals(studentId)) {
                            existing = test;
                            break;
                        }
                    }

                    if (existing == null) {
                        report.add(STUDENT + studentId + " did not have an STTERM record - adding.");

                        if (!DEBUG) {
                            final RawStterm newRec = new RawStterm(active.term, studentId, Integer.valueOf(pace),
                                    track, first, null, null, null);
                            RawSttermLogic.insert(cache, newRec);
                        }
                    } else {
                        boolean diff = false;

                        if (pace != existing.pace.intValue()) {
                            report.add(STUDENT + studentId + " had incorrect pace (was " + existing.pace
                                       + ", changing to " + pace + ").");
                            diff = true;
                        }
                        if (!track.equals(existing.paceTrack)) {
                            report.add(STUDENT + studentId + " had incorrect pace track (was "
                                       + existing.paceTrack + ", changing to " + track + ").");
                            diff = true;
                        }
                        if (!Objects.equals(first, existing.firstCourse)) {
                            report.add(STUDENT + studentId + " had incorrect first course (was "
                                       + existing.firstCourse + ", changing to " + first + ").");
                            diff = true;
                        }

                        if (diff && !DEBUG) {
                            RawSttermLogic.updatePaceTrackFirstCourse(cache, studentId, active.term, pace,
                                    track, first);
                        }

                        allStTerms.remove(existing);
                    }
                }

                // At this point, all records will have been removed from "allStTerms" that
                // matched students with current-term registrations. Everything that remains in
                // "allStTerms" should be deleted.

                for (final RawStterm toDelete : allStTerms) {
                    if (!DEBUG) {
                        RawSttermLogic.delete(cache, toDelete);
                    }
                    report.add(STUDENT + toDelete.stuId + " STTERM record deleted.");
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
            }
        }

        final HtmlBuilder htm = new HtmlBuilder(1000);
        for (final String rep : report) {
            htm.addln(rep);
        }

        return htm.toString();
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        DbConnection.registerDrivers();
        final CheckStudentTerm job = new CheckStudentTerm();

        Log.fine(job.execute());
    }
}
