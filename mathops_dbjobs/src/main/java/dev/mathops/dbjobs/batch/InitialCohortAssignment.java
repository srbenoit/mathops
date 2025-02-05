package dev.mathops.dbjobs.batch;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.logic.PaceTrackLogic;
import dev.mathops.db.old.rawlogic.RawCohortLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawSttermLogic;
import dev.mathops.db.old.rawrecord.RawCohort;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.rec.TermRec;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class examines all "student course" records, determines the number of students in each possible combination of
 * courses, and divides each group into cohort so there are no more than N students in each cohort. Cohorts will grow
 * and shrink over time as students move between cohorts, due to changing their registrations, or when students who are
 * in mixtures where they can choose which course to open next, and make a specified choice.
 */
final class InitialCohortAssignment {

    /** The target initial cohort size. */
    private static final int MAX_INITIAL_COHORT_SIZE = 25;

    /** The active term. */
    private final TermRec active;

    /**
     * Constructs a new {@code InitialCohortAssignment}.
     *
     * @param cache the data cache
     * @throws SQLException if there is an error accessing the database
     */
    private InitialCohortAssignment(final Cache cache) throws SQLException {

        this.active = cache.getSystemData().getActiveTerm();
    }

    /**
     * Computes initial cohort assignments, and stores the assigned cohort in a "stterm" record for each student with at
     * least one active (non-placement) registration.
     *
     * @param cache the data cache
     * @throws SQLException if there was an error accessing the database
     */
    private void initialCohortAssignments(final Cache cache) throws SQLException {

        // Get all registration records
        final List<RawStcourse> allRegs = RawStcourseLogic.queryActiveForActiveTerm(cache);

        // Organize registrations by student
        final Map<String, List<RawStcourse>> sortedRegs = new HashMap<>(2000);
        for (final RawStcourse test : allRegs) {
            if (test.iDeadlineDt != null && "N".equals(test.iCounted)) {
                continue;
            }

            final List<RawStcourse> list = sortedRegs.computeIfAbsent(test.stuId, s -> new ArrayList<>(10));
            list.add(test);
        }

        // Within each student's registration list, sort courses into the order defined by "pace
        // order", or in a default (numerical) order in the absence of pace order field values.
        // Then, determine pace track for each student.

        final Map<String, String> tracks = new HashMap<>(sortedRegs.size());
        for (final Map.Entry<String, List<RawStcourse>> entry : sortedRegs.entrySet()) {

            final List<RawStcourse> unsorted = entry.getValue();
            final List<RawStcourse> sorted = new ArrayList<>(unsorted.size());

            boolean found = true;
            while (found && !unsorted.isEmpty()) {
                found = false;
                final Iterator<RawStcourse> iter = unsorted.iterator();
                while (iter.hasNext()) {
                    final RawStcourse test = iter.next();
                    if (test.paceOrder != null && test.paceOrder.intValue() == 1) {
                        found = true;
                        sorted.add(test);
                        iter.remove();
                        break;
                    }
                }
            }
            if (!unsorted.isEmpty()) {
                Collections.sort(unsorted);
                sorted.addAll(unsorted);
            }

            entry.setValue(sorted);
            tracks.put(entry.getKey(), PaceTrackLogic.determinePaceTrack(sorted));
        }

        // For each student, generate their cohort prefix from their course list, and classify all
        // students by their cohort prefix
        final Map<String, Map<String, List<RawStcourse>>> cohortPrefixMap = getCohortPrefixMap(sortedRegs);

        // For each cohort prefix, determine cohort size, and assign students to cohorts
        for (final Map.Entry<String, Map<String, List<RawStcourse>>> entry : cohortPrefixMap.entrySet()) {

            final String prefix = entry.getKey();
            final Map<String, List<RawStcourse>> students = entry.getValue();

            final int numCohorts = (students.size() + MAX_INITIAL_COHORT_SIZE - 1) / MAX_INITIAL_COHORT_SIZE;

            final int studentsPerCohort = (students.size() + numCohorts - 1) / numCohorts;

            final List<String> studentIds = new ArrayList<>(students.keySet());
            final String key = studentIds.getFirst();
            final List<RawStcourse> typical = students.get(key);

            assignToCohorts(cache, studentIds, studentsPerCohort, prefix, typical.size(), tracks,
                    typical.getFirst().course);
        }
    }

    /**
     * Generates a map from students ID to cohort prefixes.
     *
     * @param sortedRegs the sorted registrations for each student
     * @return the prefix map
     */
    private static Map<String, Map<String, List<RawStcourse>>> getCohortPrefixMap(
            final Map<String, List<RawStcourse>> sortedRegs) {

        final Map<String, Map<String, List<RawStcourse>>> cohortPrefixMap = new HashMap<>(sortedRegs.size());

        final HtmlBuilder accum = new HtmlBuilder(7);
        for (final Map.Entry<String, List<RawStcourse>> entry : sortedRegs.entrySet()) {
            final String stuId = entry.getKey();
            final List<RawStcourse> regs = entry.getValue();

            for (final RawStcourse reg : regs) {
                final String courseId = reg.course;
                accum.add(courseId.charAt(courseId.length() - 1));
            }
            accum.add('.');

            final String prefix = accum.toString();
            accum.reset();

            final Map<String, List<RawStcourse>> inner = cohortPrefixMap.computeIfAbsent(prefix,
                    s -> new HashMap<>(10));
            inner.put(stuId, regs);
        }

        return cohortPrefixMap;
    }

    /**
     * Divide a list of student into to cohorts of a specified size.
     *
     * @param cache            the data cache
     * @param studentIds       the list of students IDs
     * @param numPerCohort     the size of each cohort
     * @param cohortNamePrefix the prefix for the cohort name (cohort names will be this prefix followed by the string
     *                         representation of an integer, starting at 1 and increasing by 1 for each additional
     *                         cohort needed
     * @param pace             the student's pace (to create a "stterm" record)
     * @param paceTracks       a map from student ID to student's pace track (to create a "stterm" record)
     * @param firstCourse      the student's first (lowest-numbered) course
     * @throws SQLException if there was an error accessing the database
     */
    private void assignToCohorts(final Cache cache, final Collection<String> studentIds,
                                 final int numPerCohort, final String cohortNamePrefix, final int pace,
                                 final Map<String, String> paceTracks, final String firstCourse) throws SQLException {

        if (!studentIds.isEmpty()) {
            int count = 0;
            int cohort = 1;
            String cohortName = cohortNamePrefix + cohort;

            for (final String stuId : studentIds) {
                assignStudentToCohort(cache, stuId, pace, paceTracks.get(stuId), firstCourse,
                        cohortName);

                ++count;
                if (count == numPerCohort) {
                    createCohort(cache, cohortName, count);
                    ++cohort;
                    cohortName = cohortNamePrefix + cohort;
                    count = 0;
                }
            }

            if (count > 0) {
                createCohort(cache, cohortName, count);
            }
        }
    }

    /**
     * Assigns a student to a cohort by creating (or updating) a "stterm" record for the student.
     *
     * @param cache       the data cache
     * @param stuId       the student ID
     * @param pace        the student's pace
     * @param track       the student's pace track
     * @param firstCourse the student's lowest numbered course
     * @param cohort      the cohort to which the student is assigned
     * @throws SQLException if there is an error accessing the database
     */
    private void assignStudentToCohort(final Cache cache, final String stuId, final int pace, final String track,
                                       final String firstCourse, final String cohort) throws SQLException {

        boolean failed;

        final RawStterm record = RawSttermLogic.query(cache, this.active.term, stuId);

        if (record == null) {
            failed = !RawSttermLogic.insert(cache, new RawStterm(this.active.term, stuId,
                    Integer.valueOf(pace), track, firstCourse, cohort, null, null));
        } else if (record.termKey.equals(this.active.term)) {
            failed = !RawSttermLogic.updateCohort(cache, stuId, this.active.term, cohort);

            if (record.pace == null || record.pace.intValue() != pace
                || !track.equals(record.paceTrack) || !firstCourse.equals(record.firstCourse)) {
                if (!RawSttermLogic.updatePaceTrackFirstCourse(cache, stuId, this.active.term,
                        pace, track, firstCourse)) {
                    failed = true;
                }
            }
        } else {
            Log.warning("Existing STTERM record for ", stuId,
                    " does not match active term!");
            failed = !RawSttermLogic.insert(cache, new RawStterm(this.active.term, stuId,
                    Integer.valueOf(pace), track, firstCourse, cohort, null, null));
        }

        if (failed) {
            Log.warning("Failed to assign student to cohort");
        }
    }

    /**
     * Creates a new cohort record. If a cohort record already exists with the given cohort ID (i.e., this program was
     * run more than once), the existing cohort record's size is updated.
     *
     * @param cache      the data cache
     * @param cohortId   the cohort ID
     * @param cohortSize the cohort size
     * @throws SQLException if there was an error accessing the database
     */
    private static void createCohort(final Cache cache, final String cohortId,
                                     final int cohortSize) throws SQLException {

        final RawCohort existing = RawCohortLogic.query(cache, cohortId);

        final boolean failed;

        if (existing == null) {
            failed = !RawCohortLogic.insert(cache,
                    new RawCohort(cohortId, Integer.valueOf(cohortSize), null));
        } else {
            failed = !RawCohortLogic.updateCohortSize(cache, cohortId, Integer.valueOf(cohortSize));
        }

        if (failed) {
            Log.warning("Failed to create/update cohort record");
        }
    }

    /**
     * Executes the job.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        DbConnection.registerDrivers();

        final DatabaseConfig config = DatabaseConfig.getDefault();
        final Profile profile = config.getCodeProfile(Contexts.BATCH_PATH);
        final Cache cache = new Cache(profile);

        try {
            final InitialCohortAssignment obj = new InitialCohortAssignment(cache);
            obj.initialCohortAssignments(cache);
        } catch (final SQLException ex) {
            Log.warning(ex);
        }
    }
}
