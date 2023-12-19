package dev.mathops.db.old.logic;

import dev.mathops.core.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawlogic.RawSttermLogic;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.old.svc.term.TermLogic;
import dev.mathops.db.old.svc.term.TermRec;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Logic relating to the calculation of pace track.
 */
public enum PaceTrackLogic {
    ;

    /** A commonly used integer. */
    private static final Integer ONE = Integer.valueOf(1);

    /**
     * Tests whether a course ID is "applicable" to being counted toward pace.
     *
     * @param courseId the course ID
     * @return true if it should count toward pace
     */
    private static boolean isApplicableCourse(final String courseId) {

        return RawRecordConstants.M117.equals(courseId)
                || RawRecordConstants.M118.equals(courseId)
                || RawRecordConstants.M124.equals(courseId)
                || RawRecordConstants.M125.equals(courseId)
                || RawRecordConstants.M126.equals(courseId)
                || RawRecordConstants.MATH117.equals(courseId)
                || RawRecordConstants.MATH118.equals(courseId)
                || RawRecordConstants.MATH124.equals(courseId)
                || RawRecordConstants.MATH125.equals(courseId)
                || RawRecordConstants.MATH126.equals(courseId);
    }

    /**
     * Given a list of registrations, determines the student's pace.
     *
     * @param registrations the list of registrations (this collection could include dropped/withdrawn/ignored
     *                      registrations or incompletes not counted in pace - such records will be ignored when
     *                      computing pace)
     * @return the pace track
     */
    public static int determinePace(final Iterable<RawStcourse> registrations) {

        int pace = 0;

        for (final RawStcourse test : registrations) {
            if (isApplicableCourse(test.course) && isCountedTowardPace(test)) {
                ++pace;
            }
        }

        return pace;
    }

    /**
     * Tests whether a registration should count toward the pace.
     *
     * @param test the registration
     * @return true if the registration should count toward pace; false if not
     */
    private static boolean isCountedTowardPace(final RawStcourse test) {

        final boolean counts;

        if (test.synthetic || "OT".equals(test.instrnType)) {
            // Do not count "credit by challenge exam" registration
            counts = false;
        } else {
            // Do not count dropped or "ignored" registrations, as well as Incompletes that
            // are not counted toward pace
            final String status = test.openStatus;
            counts = !"D".equals(status) && !"G".equals(status) &&
                    (!"Y".equals(test.iInProgress) || !"N".equals(test.iCounted));
        }

        return counts;
    }

    /**
     * Given a list of registrations and a known pace, determines the student's pace track.
     *
     * @param registrations the list of registrations (this collection could include dropped/withdrawn/ignored
     *                      registrations or incompletes not counted in pace - such records will be ignored when
     *                      computing pace track)
     * @param pace          the pace (from {@code determinePace})
     * @return the pace track
     */
    public static String determinePaceTrack(final Iterable<RawStcourse> registrations, final int pace) {

        // Determine section number - look first for non-Incompletes, then for Incompletes
        String sect = null;
        for (final RawStcourse test : registrations) {
            if (isApplicableCourse(test.course)) {
                if (test.synthetic || "OT".equals(test.instrnType)) {
                    continue;
                }
                if ("N".equals(test.iInProgress)) {
                    final String status = test.openStatus;

                    if ((!"D".equals(status) && !"G".equals(status))) {
                        sect = test.sect;
                        break;
                    }
                }
            }
        }

        if (sect == null) {
            // Look next at counted incompletes (if we get here, the student has ONLY incompletes)
            for (final RawStcourse test : registrations) {
                if (isApplicableCourse(test.course)) {
                    if (test.synthetic || "OT".equals(test.instrnType)) {
                        continue;
                    }
                    if ("Y".equals(test.iInProgress) && "Y".equals(test.iCounted)) {

                        sect = test.sect;
                        break;
                    }
                }
            }
        }

        if (sect == null) {
            // Last check is for non-counted incompletes (if we get here, the student has ONLY
            // non-counted incompletes)
            for (final RawStcourse test : registrations) {
                if (isApplicableCourse(test.course)) {
                    if (test.synthetic || "OT".equals(test.instrnType)) {
                        continue;
                    }
                    final String status = test.openStatus;
                    if ("D".equals(status) || "G".equals(status)) {
                        continue;
                    }

                    if ("Y".equals(test.iInProgress) && "N".equals(test.iCounted)) {
                        sect = test.sect;
                    }
                }
            }
        }

        // Default track is "A"
        String track = "A";

        if ("002".equals(sect)) {
            // 002 is a "late-start" section - track C (only 1 or 2 course pace)
            track = "C";
        } else if ("003".equals(sect) || "004".equals(sect) || "005".equals(sect) || "006".equals(sect)
                || "007".equals(sect)) {
            // In-person sections - if "MATH 125" or "MATH 126" is included, use track E, otherwise, use track D

            track = "D";
            for (final RawStcourse test : registrations) {
                if ((RawRecordConstants.MATH125.equals(test.course)
                        || RawRecordConstants.MATH126.equals(test.course))
                        && isCountedTowardPace(test)) {
                    track = "E";
                    break;
                }
            }
        } else if (pace == 2) {
            // 2-course students who have MATH 117 (Fall) or MATH 125 (Spring) are track "A".  Otherwise, track "B".
            // Summer has only track A.
            track = "B";
            for (final RawStcourse test : registrations) {
                if (RawRecordConstants.M125.equals(test.course) && isCountedTowardPace(test)) {
                    track = "A";
                    break;
                }
            }
        } else if (pace == 1) {
            // 1-course students who have MATH 117 or {MATH 125 (Fall) or MATH 124 (Spring)} are
            // track "A". Otherwise, track "B".  Summer has only track A.
            track = "B";
            for (final RawStcourse test : registrations) {
                if ((RawRecordConstants.M117.equals(test.course)
                        || RawRecordConstants.M124.equals(test.course))
                        && isCountedTowardPace(test)) {
                    track = "A";
                    break;
                }
            }
        }

        return track;
    }

    /**
     * Given a list of registrations, determines the student's pace track.
     *
     * @param registrations the list of registrations (this collection could include dropped/withdrawn/ignored
     *                      registrations or incompletes not counted in pace - such records will be ignored when
     *                      computing pace track)
     * @return the pace track
     */
    public static String determinePaceTrack(final Iterable<RawStcourse> registrations) {

        return determinePaceTrack(registrations, determinePace(registrations));
    }

    /**
     * Given a list of registrations, determines the student's first course.
     *
     * @param registrations the list of registrations (this collection could include dropped/withdrawn/ignored
     *                      registrations or incompletes not counted in pace - such records will be ignored when
     *                      computing first course)
     * @return the first course ID
     */
    public static String determineFirstCourse(final Collection<RawStcourse> registrations) {

        // Scan registrations into "open" and "not open" courses, filtering out non-counted
        // incompletes and "Ignored" courses.

        final int numRegs = registrations.size();
        final List<RawStcourse> open = new ArrayList<>(numRegs);
        final Collection<RawStcourse> notOpen = new ArrayList<>(numRegs);

        for (final RawStcourse test : registrations) {
            if (isApplicableCourse(test.course) && isCountedTowardPace(test)) {

                if ("Y".equals(test.openStatus)) {
                    open.add(test);
                    if (test.paceOrder == null) {
                        Log.warning("Student ", test.stuId, " registration in ", test.course,
                                " is open but pace order is NULL!");
                    }
                } else {
                    notOpen.add(test);
                }
            }
        }

        // Identify first course as follows
        // (1) If there are open courses:
        // - - - If one has "order= 1" choose that one
        // - - - If not, choose the one with the lowest order number
        // (2) If there are no open courses:
        // - - - Choose the lowest course number whose prerequisite is satisfied
        // - - - If no prerequisites are satisfied, choose the lowest number

        String first = null;
        if (open.isEmpty()) {

            for (final RawStcourse reg : notOpen) {
                if (("Y".equals(reg.prereqSatis) || "P".equals(reg.prereqSatis))
                        && (first == null || reg.course.compareTo(first) < 0)) {
                    first = reg.course;
                }
            }

            if (first == null) {
                // No prereq satisfied for any course! Take the lowest course number
                first = lowestCourseNumber(notOpen);
            }

        } else {
            for (final RawStcourse reg : open) {
                if (ONE.equals(reg.paceOrder)) {
                    first = reg.course;
                    break;
                }
            }

            if (first == null) {
                if (open.size() == 1) {
                    first = open.get(0).course;
                } else {
                    // There were multiple open courses, but none marked as order 1 - this could occur if the student
                    // completes a course then drops it after starting course 3. In this case, take the lowest order
                    // number

                    int lowest = 1000;
                    for (final RawStcourse reg : open) {
                        final Integer order = reg.paceOrder;
                        if (order != null && order.intValue() < lowest) {
                            first = reg.course;
                            lowest = order.intValue();
                        }
                    }

                    if (first == null) {
                        // None of them had a pace order! choose the one with the lowest number as an emergency fallback
                        first = lowestCourseNumber(open);
                    }
                }
            }
        }

        if (first == null) {
            // All registrations must be ignored or incompletes not counted - use that one.
            for (final RawStcourse reg : registrations) {
                if ("Y".equals(reg.iInProgress) && "N".equals(reg.iCounted)) {
                    first = reg.course;
                }
            }
        }

        return first;
    }

    /**
     * Returns the lowest course number in a (nonempty) list of registrations.
     *
     * @param regs the registrations
     * @return the lowest course number
     */
    private static String lowestCourseNumber(final Iterable<RawStcourse> regs) {

        String lowest = null;
        String lowestNumber = null;

        for (final RawStcourse reg : regs) {
            final String courseNum;
            if (reg.course.startsWith("MATH ")) {
                courseNum = reg.course.substring(5);
            } else if (reg.course.startsWith("M ")) {
                courseNum = reg.course.substring(2);
            } else {
                courseNum = reg.course;
            }

            if (lowestNumber == null || courseNum.compareTo(lowestNumber) < 0) {
                lowestNumber = courseNum;
                lowest = reg.course;
            }
        }

        return lowest;
    }

    /**
     * Updates the student term record for a student based on a list of registrations.
     *
     * @param cache         the data cache
     * @param studentId     the student ID
     * @param registrations the list of registrations (this collection could include dropped/withdrawn/ignored
     *                      registrations or incompletes not counted in pace - such records will be ignored when
     *                      computing pace)
     * @throws SQLException if there is an error accessing the database
     */
    public static void updateStudentTerm(final Cache cache, final String studentId,
                                         final Collection<RawStcourse> registrations) throws SQLException {

        final TermRec active = TermLogic.get(cache).queryActive(cache);

        if (active != null) {
            final int pace = determinePace(registrations);

            final RawStterm existing = RawSttermLogic.query(cache, active.term, studentId);

            if (pace == 0) {
                if (existing != null) {
                    Log.info("Deleting STTERM <", existing.termKey.shortString, ",", existing.stuId, ",",
                            existing.pace, ",", existing.paceTrack, ",", existing.firstCourse, ">");

                    RawSttermLogic.INSTANCE.delete(cache, existing);
                }
            } else {
                final String first = determineFirstCourse(registrations);
                final String track = determinePaceTrack(registrations, pace);

                if (existing == null) {
                    final RawStterm newRec = new RawStterm(active.term, studentId, Integer.valueOf(pace), track, first,
                            null, null, null);

                    Log.info("Inserting STTERM <", newRec.termKey.shortString, ",", newRec.stuId, ",",
                            newRec.pace, ",", newRec.paceTrack, ",", newRec.firstCourse, ">");

                    RawSttermLogic.INSTANCE.insert(cache, newRec);

                } else if (existing.pace.intValue() != pace || !existing.paceTrack.equals(track)
                        || !existing.firstCourse.equals(first)) {

                    Log.info("Updating STTERM <", active.term.shortString, ",", studentId, ",", Integer.toString(pace),
                            ",", track, ",", first, ">");

                    RawSttermLogic.updatePaceTrackFirstCourse(cache, studentId, active.term, pace, track, first);
                }
            }
        }
    }
}
