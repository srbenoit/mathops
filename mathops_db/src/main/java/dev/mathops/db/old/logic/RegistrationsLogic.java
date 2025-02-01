package dev.mathops.db.old.logic;

import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.rec.RecBase;
import dev.mathops.db.rec.TermRec;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Logic relating to student registrations.  This class gathers all active-term registrations for a student and sorts
 * them into
 * <ul>
 * <li>Those that contribute toward the student's "pace" (active registrations and "counted" incompletes)
 * <li>Incompletes that are not counted
 * <li>Dropped registrations
 * <li>Ignored registrations
 * <li>Registrations for credit by exam
 * </ul>
 * <p>
 * It can also gather past-term registrations.
 */
public enum RegistrationsLogic {
    ;

    /** The list of legacy course IDs. */
    private static final List<String> LEGACY_COURSE_IDS = Arrays.asList(RawRecordConstants.M117,
            RawRecordConstants.M118, RawRecordConstants.M124, RawRecordConstants.M125, RawRecordConstants.M126);

    /** The list of standards-based course IDs. */
    private static final List<String> STANDARDS_BASED_COURSE_IDS = Arrays.asList(RawRecordConstants.MATH117,
            RawRecordConstants.MATH118, RawRecordConstants.MATH124, RawRecordConstants.MATH125,
            RawRecordConstants.MATH126);

    /** A comparator to order counted Incompletes by deadline date, then course ID. */
    private static final Comparator<RawStcourse> COUNTED_INCOMPLETE_COMPARATOR = new CountedIncompleteComparator();

    /** A comparator that uses only course ID. */
    private static final Comparator<RawStcourse> COURSE_ID_COMPARATOR = new CourseIDComparator();

    /**
     * Gathers all active term registrations for a student.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return a record with categorized registrations the student has in the active term
     * @throws SQLException if there is an error accessing the database
     */
    public static ActiveTermRegistrations gatherActiveTermRegistrations(final Cache cache, final String stuId)
            throws SQLException {

        final List<RawStcourse> inPace;
        final List<RawStcourse> uncountedIncompletes;
        final List<RawStcourse> dropped;
        final List<RawStcourse> ignored;
        final List<RawStcourse> creditByExam;
        final List<String> warnings = new ArrayList<>(1);

        final TermRec active = cache.getSystemData().getActiveTerm();

        if (active == null) {
            inPace = new ArrayList<>(0);
            uncountedIncompletes = new ArrayList<>(0);
            dropped = new ArrayList<>(0);
            ignored = new ArrayList<>(0);
            creditByExam = new ArrayList<>(0);
        } else {
            final List<RawStcourse> all = RawStcourseLogic.getActiveForStudent(cache, stuId, active.term);

            int numInPace = 0;
            int numUncounted = 0;
            int numDropped = 0;
            int numIgnored = 0;
            int numCredit = 0;
            for (final RawStcourse row : all) {
                final String open = row.openStatus;
                if ("D".equals(open)) {
                    ++numDropped;
                } else if ("OT".equals(row.instrnType)) {
                    ++numCredit;
                } else if ("G".equals(open)) {
                    ++numIgnored;
                } else if ("Y".equals(row.iInProgress)) {
                    if ("Y".equals(row.iCounted)) {
                        ++numInPace;
                    } else {
                        ++numUncounted;
                    }
                } else {
                    ++numInPace;
                }
            }

            inPace = new ArrayList<>(numInPace);
            uncountedIncompletes = new ArrayList<>(numUncounted);
            dropped = new ArrayList<>(numDropped);
            ignored = new ArrayList<>(numIgnored);
            creditByExam = new ArrayList<>(numCredit);

            final List<RawStcourse> inPaceToSort = new ArrayList<>(numInPace);
            for (final RawStcourse row : all) {
                final String open = row.openStatus;
                if ("D".equals(open)) {
                    dropped.add(row);
                } else if ("OT".equals(row.instrnType)) {
                    creditByExam.add(row);
                } else if ("G".equals(open)) {
                    ignored.add(row);
                } else if ("Y".equals(row.iInProgress)) {
                    if ("Y".equals(row.iCounted)) {
                        inPaceToSort.add(row);
                    } else {
                        uncountedIncompletes.add(row);
                    }
                } else {
                    inPaceToSort.add(row);
                }
            }

            if (numInPace > 0) {
                sortPacedRegistrations(inPaceToSort, inPace, warnings);
            }
        }

        return new ActiveTermRegistrations(inPace, uncountedIncompletes, dropped, ignored, creditByExam, warnings);
    }

    /**
     * Sorts a list of "in-pace" registrations and places the results in a target list.
     *
     * @param toSort   the list of registrations to sort
     * @param sorted   the target list to which to add sorted registrations (empty on entry)
     * @param warnings a list to which to add warnings
     */
    private static void sortPacedRegistrations(final List<RawStcourse> toSort, final Collection<RawStcourse> sorted,
                                               final Collection<? super String> warnings) {

        // Do "counted" incompletes first
        int numCounted = 0;
        int numCompletedCounted = 0;
        int numOpenCounted = 0;
        for (final RawStcourse row : toSort) {
            if ("Y".equals(row.iInProgress)) {
                ++numCounted;

                if ("Y".equals(row.completed)) {
                    ++numCompletedCounted;
                } else if ("Y".equals(row.openStatus)) {
                    ++numOpenCounted;
                }
            }
        }

        if (numCounted == 1) {
            for (final RawStcourse row : toSort) {
                if ("Y".equals(row.iInProgress)) {
                    sorted.add(row);
                }
            }
        } else if (numCounted > 1) {
            // There are multiple counted Incompletes - we need to get them into the correct order.

            // If there are any with completed='Y', those go first, followed by any with open_status='Y', and within
            // each set, we order by "i_deadline_dt" first, then course number second.

            if (numCompletedCounted == 1) {
                for (final RawStcourse row : toSort) {
                    if ("Y".equals(row.iInProgress) && "Y".equals(row.completed)) {
                        sorted.add(row);
                    }
                }
            } else if (numCompletedCounted > 1) {
                // Sort all completed/counted by i_deadline_dt then course number
                final List<RawStcourse> temp = new ArrayList<>(numCompletedCounted);
                for (final RawStcourse row : toSort) {
                    if ("Y".equals(row.iInProgress) && "Y".equals(row.completed)) {
                        temp.add(row);
                    }
                }
                temp.sort(COUNTED_INCOMPLETE_COMPARATOR);
                sorted.addAll(temp);
            }

            // At this point, we have processed all "counted, completed" incompletes - next look for open

            if (numOpenCounted == 1) {
                for (final RawStcourse row : toSort) {
                    if ("Y".equals(row.iInProgress) && !"Y".equals(row.completed) && "Y".equals(row.openStatus)) {
                        sorted.add(row);
                    }
                }
            } else if (numOpenCounted > 1) {
                // Sort all open/counted by i_deadline_dt then course number
                final List<RawStcourse> temp = new ArrayList<>(numOpenCounted);
                for (final RawStcourse row : toSort) {
                    if ("Y".equals(row.iInProgress) && !"Y".equals(row.completed) && "Y".equals(row.openStatus)) {
                        temp.add(row);
                    }
                }
                temp.sort(COUNTED_INCOMPLETE_COMPARATOR);
                sorted.addAll(temp);
            }

            // Sort all non-open/counted by i_deadline_dt then course number
            final List<RawStcourse> temp = new ArrayList<>(numOpenCounted);
            for (final RawStcourse row : toSort) {
                if ("Y".equals(row.iInProgress) && !"Y".equals(row.completed) && !"Y".equals(row.openStatus)) {
                    temp.add(row);
                }
            }
            temp.sort(COUNTED_INCOMPLETE_COMPARATOR);
            sorted.addAll(temp);
        }

        // Incompletes are finished - move on to non-Incomplete registrations.
        toSort.removeIf(row -> "Y".equals(row.iInProgress));

        // If pace_order fields are populated, we honor those, but if something in pace order does not make sense, we
        // generate a warning.  If there is a gap in the pace order fields, or multiple rows with the same pace order,
        // all records beyond the invalid pace order should have their pace order cleared.  For registrations without
        // pace order set, we order based on the following heuristic:

        // If any are completed, order those first, followed by those that are open but not completed; then those that
        // are not yet open. There can be "forfeit" courses with open_status='N' completed='N' within the list -
        // these should be treated as "closed" and can be grouped with "completed" courses and ordered first.  Within
        // these groups, in the absence of pace order information, we order by course number.

        // NOTE: Some course sections imply an ordering (MATH 125 in the first half of the term then 126 in the second)
        // This logic does NOT dig into "csection" start and end dates to try to validate this.  The only situation
        // where this could cause trouble would be MATH 126 in the first half, MATh 124 in the second.

        for (int order = 1; order < 5; ++order) {
            RawStcourse found = null;
            boolean duplicate = false;
            for (final RawStcourse row : toSort) {
                if (row.paceOrder != null && row.paceOrder.intValue() == order) {
                    if (found == null) {
                        found = row;
                    } else {
                        duplicate = true;
                        break;
                    }
                }
            }

            if (duplicate) {
                // This order and all higher orders are "invalid"
                warnings.add("Multiple registrations found with pace_order = " + order);
                for (final RawStcourse row : toSort) {
                    if (row.paceOrder != null && row.paceOrder.intValue() >= order) {
                        warnings.add("Ignoring pace_order that was set to " + row.paceOrder);
                        row.paceOrder = null;
                    }
                }
            } else if (found == null) {
                // This all higher orders are "invalid"
                for (final RawStcourse row : toSort) {
                    if (row.paceOrder != null && row.paceOrder.intValue() > order) {
                        warnings.add("There was no record with pace order " + order
                                     + ", ignoring pace order that was set to " + row.paceOrder);
                        row.paceOrder = null;
                    }
                }
            } else {
                toSort.remove(found);
                sorted.add(found);
            }
        }

        if (!toSort.isEmpty()) {
            // There are rows with no "pace order".

            // Collect all that are completed first
            final List<RawStcourse> temp = new ArrayList<>(numOpenCounted);
            for (final RawStcourse row : toSort) {
                if ("Y".equals(row.completed)) {
                    temp.add(row);
                }
            }
            temp.sort(COURSE_ID_COMPARATOR);
            sorted.addAll(temp);
            toSort.removeIf(row -> "Y".equals(row.completed));

            // Collect all that have open_status = 'N' next
            temp.clear();
            for (final RawStcourse row : toSort) {
                if ("N".equals(row.openStatus)) {
                    temp.add(row);
                }
            }
            temp.sort(COURSE_ID_COMPARATOR);
            sorted.addAll(temp);
            toSort.removeIf(row -> "N".equals(row.openStatus));

            // Next add those with open_status = 'Y'
            temp.clear();
            for (final RawStcourse row : toSort) {
                if ("Y".equals(row.openStatus)) {
                    temp.add(row);
                }
            }
            temp.sort(COURSE_ID_COMPARATOR);
            sorted.addAll(temp);
            toSort.removeIf(row -> "Y".equals(row.openStatus));

            // Add the rest
            toSort.sort(COURSE_ID_COMPARATOR);
            sorted.addAll(toSort);
        }

        // Sanity checks: there should not be any with open_status = 'Y' after any with open_status NULL, and there
        // should not be any completed = 'Y' after any with completed = 'N'.  Presumably, open_status = 'N' could
        // occur anywhere in a list.

        boolean foundOpenStatusNull = false;
        boolean foundNonCompleted = false;
        for (final RawStcourse row : sorted) {
            if ("Y".equals(row.completed)) {
                if (foundNonCompleted) {
                    warnings.add("Registration for " + row.course
                                 + " is marked as completed, but was ordered after a non-completed registration");
                }
            } else if (!"N".equals(row.openStatus)) {
                // There can be forfeits that are not completed before a completed course...
                foundNonCompleted = true;
            }

            if (row.openStatus == null) {
                foundOpenStatusNull = true;
            } else if (foundOpenStatusNull && "Y".equals(row.openStatus)) {
                warnings.add("Registration for " + row.course
                             + " is open by was ordered after a course that is not yet open.");
            }
        }
    }

    /**
     * A container for categorized registrations for a single student in the active term.
     *
     * @param inPace               the list of "in-pace" registrations (registrations will be sorted in pace order in
     *                             this list, with all counted Incompletes first)
     * @param uncountedIncompletes the list of Incompletes that do not count toward pace
     * @param dropped              the list of dropped registrations
     * @param ignored              the list of ignored registrations (open_status = 'G')
     * @param creditByExam         the list of registrations that represent credit by exam
     * @param warnings             a list of warnings of registration issues detected
     */
    public record ActiveTermRegistrations(List<RawStcourse> inPace, List<RawStcourse> uncountedIncompletes,
                                          List<RawStcourse> dropped, List<RawStcourse> ignored,
                                          List<RawStcourse> creditByExam, List<String> warnings) {

        /**
         * Tests whether this student has any courses in their "paced" list that are standards-based.
         *
         * @return true if the student has at least one standards-based course
         */
        public boolean hasStandardsBased() {

            boolean hasStandardsBased = false;

            for (final RawStcourse row : this.inPace) {
                if (STANDARDS_BASED_COURSE_IDS.contains(row.course)) {
                    hasStandardsBased = true;
                    break;
                }
            }

            return hasStandardsBased;
        }

        /**
         * Tests whether this student has any courses in their "paced" list that are legacy mastery courses.
         *
         * @return true if the student has at least one legacy mastery course
         */
        public boolean hasLegacy() {

            boolean hasLegacy = false;

            for (final RawStcourse row : this.inPace) {
                if (LEGACY_COURSE_IDS.contains(row.course)) {
                    hasLegacy = true;
                    break;
                }
            }

            return hasLegacy;
        }
    }

    /**
     * A comparator to order {@code RawStcourse} records for counted Incompletes by i_deadline_dt first, then course ID
     * second.
     */
    static class CountedIncompleteComparator implements Comparator<RawStcourse> {

        /**
         * Constructs a new {@code CountedIncompleteComparator}
         */
        CountedIncompleteComparator() {

            // No action
        }

        /**
         * Compares two records for order.
         *
         * @param o1 the first object to be compared
         * @param o2 the second object to be compared
         * @return a negative integer, zero, or a positive integer as the first object is less than, equal to, or
         *         greater than the second object
         */
        @Override
        public int compare(final RawStcourse o1, final RawStcourse o2) {

            int result = RecBase.compareAllowingNull(o1.iDeadlineDt, o2.iDeadlineDt);

            if (result == 0) {
                result = RecBase.compareAllowingNull(o1.course, o2.course);
            }

            return result;
        }
    }

    /**
     * A comparator to order {@code RawStcourse} records by course ID.
     */
    static class CourseIDComparator implements Comparator<RawStcourse> {

        /**
         * Constructs a new {@code CourseIDComparator}
         */
        CourseIDComparator() {

            // No action
        }

        /**
         * Compares two records for order.
         *
         * @param o1 the first object to be compared
         * @param o2 the second object to be compared
         * @return a negative integer, zero, or a positive integer as the first object is less than, equal to, or
         *         greater than the second object
         */
        @Override
        public int compare(final RawStcourse o1, final RawStcourse o2) {

            return RecBase.compareAllowingNull(o1.course, o2.course);
        }
    }
}
