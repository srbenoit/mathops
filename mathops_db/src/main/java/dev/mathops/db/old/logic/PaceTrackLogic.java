package dev.mathops.db.old.logic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawlogic.RawSttermLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.rec.TermRec;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.TreeSet;

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
            // Do not count dropped or "ignored" registrations, as well as Incompletes that are not counted toward pace
            final String status = test.openStatus;
            counts = !("D".equals(status) || "G".equals(status) ||
                       ("Y".equals(test.iInProgress) && "N".equals(test.iCounted)));
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

        final Collection<String> foundSections = new TreeSet<>(); // Must be a sorted set - see below....

        for (final RawStcourse test : registrations) {
            if (isApplicableCourse(test.course)) {
                if (test.synthetic || "OT".equals(test.instrnType)) {
                    continue;
                }
                if ("N".equals(test.iInProgress)) {
                    final String status = test.openStatus;
                    if ((!"D".equals(status) && !"G".equals(status))) {
                        foundSections.add(test.sect);
                    }
                }
            }
        }

        if (foundSections.isEmpty()) {
            // Look next at counted incompletes (if we get here, the student has ONLY incompletes)
            for (final RawStcourse test : registrations) {
                if (isApplicableCourse(test.course)) {
                    if (test.synthetic || "OT".equals(test.instrnType)) {
                        continue;
                    }
                    if ("Y".equals(test.iInProgress) && "Y".equals(test.iCounted)) {
                        foundSections.add(test.sect);
                    }
                }
            }
        }

        if (foundSections.isEmpty()) {
            // Last check is for non-counted incompletes (if we get here, the student has ONLY non-counted incompletes)
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
                        foundSections.add(test.sect);
                    }
                }
            }
        }

        // Students might have a mix of online (001 and 002) and face-to-face (higher) - if that occurs, we want to
        // assign their pace track based on the online section, then do overrides for deadlines in the face-to-face.
        // We can achieve this by sorting the section numbers we find numerically and taking the lowest, and because
        // we used a TreeSet for section numbers, we just need to use the first entry

        final String sect = foundSections.isEmpty() ? null : foundSections.iterator().next();

        // Default track is "A" (used for all 3-course, 4-course, and 5-course pace students)
        String track = "A";

        // SUMMER: Everyone is in track A except section 002 of MATH 117, which is track B (face-to-face)
        if ("002".equals(sect)) {
            track = "B";
        }
//
//        if ("001".equals(sect) || "801".equals(sect) || "809".equals(sect)) {
//            // In Fall/Spring, 001 is the normal full-semester online section
//            if (pace == 1) {
//                // Students whose single course is MATH 117 or 124 are track A, the others are track B
//                for (final RawStcourse test : registrations) {
//                    if ((RawRecordConstants.M118.equals(test.course)
//                         || RawRecordConstants.M125.equals(test.course)
//                         || RawRecordConstants.M126.equals(test.course))
//                        && isCountedTowardPace(test)) {
//                        track = "B";
//                        break;
//                    }
//                }
//            } else if (pace == 2) {
//                // If a student in 2 courses has MATH 125, they are track A; otherwise track B
//                track = "B";
//                for (final RawStcourse test : registrations) {
//                    if (RawRecordConstants.M125.equals(test.course) && isCountedTowardPace(test)) {
//                        track = "A";
//                        break;
//                    }
//                }
//            }
//        } else if ("002".equals(sect)) {
//            // In Fall/Spring, 002 is a "late-start" section: track C (only 1 or 2 course pace)
//            track = "C";
//        } else if ("003".equals(sect)) {
//            // An in-person section, one of the following:
//            //   116 + 117 or 116 + 117 + 118 (Track D)
//            //   125 or 125 + 126 (track F)
//            //   126 (track H)
//            boolean has125 = false;
//            boolean has126 = false;
//            for (final RawStcourse test : registrations) {
//                if (isCountedTowardPace(test)) {
//                    if (RawRecordConstants.M125.equals(test.course)) {
//                        has125 = true;
//                    } else if (RawRecordConstants.M126.equals(test.course)) {
//                        has126 = true;
//                    }
//                }
//            }
//            if (has126) {
//                track = has125 ? "F" : "H";
//            } else if (has125) {
//                track = "F";
//            } else {
//                track = "D";
//            }
//        } else if ("004".equals(sect)) {
//            // An in-person MATH 125 section (track F)
//            track = "F";
//        } else if ("005".equals(sect)) {
//            // An in-person section, one of the following:
//            //   116 + 117 or 116 + 117 + 118 (Track E)
//            //   125 or 125 + 126 (track F)
//            track = "E";
//            for (final RawStcourse test : registrations) {
//                if ((RawRecordConstants.M125.equals(test.course) || RawRecordConstants.M126.equals(test.course))
//                    && isCountedTowardPace(test)) {
//                    track = "F";
//                }
//            }
//        } else if ("006".equals(sect)) {
//            // An in-person MATH 117 / 118 section (track E)
//            track = "E";
//        }

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

        final int pace = determinePace(registrations);

        return determinePaceTrack(registrations, pace);
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
        final SequencedCollection<RawStcourse> open = new ArrayList<>(numRegs);
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
                    first = open.getFirst().course;
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
     * Given a list of in-pace registrations, determines the student's pacing structure.
     *
     * @param cache         the data cache
     * @param stuId         the student ID
     * @param registrations the list of registrations (this collection could include dropped/withdrawn/ignored
     *                      registrations or incompletes not counted in pace - such records will be ignored when
     *                      computing pace track)
     * @param warnings      a list to which to add warnings (null to skip gathering warnings)
     * @return the pacing structure; null none can be determined
     * @throws SQLException if there is an error accessing the database
     */
    public static String determinePacingStructure(final Cache cache, final String stuId,
                                                  final Iterable<RawStcourse> registrations,
                                                  final Collection<? super String> warnings)
            throws SQLException {

        final SystemData systemData = cache.getSystemData();
        final TermRec active = systemData.getActiveTerm();

        final Collection<String> pacingStructures = new HashSet<>(5);

        for (final RawStcourse reg : registrations) {
            if (isCountedTowardPace(reg)) {

                final RawCsection csection = systemData.getCourseSection(reg.course, reg.sect, active.term);
                if (csection == null) {
                    if (warnings != null) {
                        warnings.add("No CSECTION record found for " + reg.course + " section " + reg.sect);
                    }
                } else if (csection.pacingStructure != null) {
                    pacingStructures.add(csection.pacingStructure);
                }
            }
        }

        final RawStudent student = RawStudentLogic.query(cache, stuId, false);

        String result = null;

        final int count = pacingStructures.size();
        if (count > 1) {
            if (warnings != null) {
                warnings.add("Student " + stuId + " has registrations with different pacing structures.");
            }

            if (pacingStructures.contains("M")) {
                result = "M";
            } else if (pacingStructures.contains("O")) {
                result = "O";
            } else if (pacingStructures.contains("S")) {
                result = "S";
            } else if (warnings != null) {
                warnings.add("Student " + stuId + " has no recognized pacing structures.");
            }
        } else if (count == 1) {
            result = pacingStructures.iterator().next();
            if (student.pacingStructure == null) {
                if (warnings != null) {
                    warnings.add("Student " + stuId + " registration had pacing structure " + result
                                 + " but student record has null (fixed)");
                }
                RawStudentLogic.updatePacingStructure(cache, stuId, result);
            } else if (!student.pacingStructure.equals(result)) {
                if (warnings != null) {
                    warnings.add("Student " + stuId + " registration had pacing structure " + result
                                 + " but student record has " + student.pacingStructure + " (fixed)");
                }
                RawStudentLogic.updatePacingStructure(cache, stuId, result);
            }
        } else if (warnings != null) {
            warnings.add("Unable to determine any pacing structure for student " + stuId);
        }

        return result;
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

        final TermRec active = cache.getSystemData().getActiveTerm();

        if (Objects.nonNull(active)) {
            final int pace = determinePace(registrations);

            final RawStterm existing = RawSttermLogic.query(cache, active.term, studentId);

            if (pace == 0) {
                if (Objects.nonNull(existing)) {
                    Log.info("Deleting STTERM <", existing.termKey.shortString, ",", existing.stuId, ",",
                            existing.pace, ",", existing.paceTrack, ",", existing.firstCourse, ">");

                    RawSttermLogic.delete(cache, existing);
                }
            } else {
                final String first = determineFirstCourse(registrations);
                final String track = determinePaceTrack(registrations, pace);

                if (existing == null) {
                    final Integer paceInteger = Integer.valueOf(pace);
                    final RawStterm newRec = new RawStterm(active.term, studentId, paceInteger, track, first, null,
                            null, null);

                    Log.info("Inserting STTERM <", newRec.termKey.shortString, ",", newRec.stuId, ",",
                            newRec.pace, ",", newRec.paceTrack, ",", newRec.firstCourse, ">");

                    RawSttermLogic.insert(cache, newRec);

                } else if (!(existing.pace.intValue() == pace && existing.paceTrack.equals(track)
                             && existing.firstCourse.equals(first))) {

                    final String paceStr = Integer.toString(pace);
                    Log.info("Updating STTERM <", active.term.shortString, ",", studentId, ",", paceStr, ",", track,
                            ",", first, ">");

                    RawSttermLogic.updatePaceTrackFirstCourse(cache, studentId, active.term, pace, track, first);
                }
            }
        }
    }
}
