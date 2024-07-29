package dev.mathops.app.ops.snapin.messaging.tosend;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.logic.PaceTrackLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.svc.term.TermRec;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A class that scans for all students with active-term registrations, and categorizes them by their mix of
 * registrations (after excluding OT registrations).
 *
 * <ul>
 * <li>Working on Incomplete that is not counted toward pace
 *
 * <li>5-course
 * <li>5-course with first course forfeit
 * <li>5-course with first two courses forfeit
 * <li>5-course with first three courses forfeit
 * <li>5-course with first four courses forfeit
 *
 * <li>4-course
 * <li>4-course with first course forfeit
 * <li>4-course with first two courses forfeit
 * <li>4-course with first three courses forfeit
 *
 * <li>3-course
 * <li>3-course with first course forfeit
 * <li>3-course with first two courses forfeit
 *
 * <li>2-course, track A
 * <li>2-course, track A with first course forfeit
 *
 * <li>2-course, track B
 * <li>2-course, track B with first course forfeit
 *
 * <li>2-course, track C
 * <li>2-course, track C with first course forfeit
 *
 * <li>1-course, track A
 *
 * <li>1-course, track B
 *
 * <li>1-course, track C
 * </ul>
 */
public final class MessagePopulationBuilder {

    /** A pace track constant. */
    private static final String TRACK_A = "A";

    /** A pace track constant. */
    private static final String TRACK_B = "B";

    /** A pace track constant. */
    private static final String TRACK_C = "C";

    /** The cache. */
    private final Cache cache;

    /** The courses and sections to include. */
    private final Map<String, ? extends List<String>> includeCourseSections;

    /**
     * Map from section number to map from student ID student registration list for those students who have a
     * non-counted Incomplete that has not yet been finished (hence cannot work on other courses until that's done).
     */
    private final Population nonCountedIncomplete;

    /**
     * Map from section number to map from student ID student registration list for those students in the 5-course
     * pace.
     */
    public final Population five;

    /**
     * Map from section number to map from student ID student registration list for those students in the 5-course pace
     * who have forfeited at least one course.
     */
    public final Population fiveWithForfeit;

    /**
     * Map from section number to map from student ID student registration list for those students in the 4-course
     * pace.
     */
    public final Population four;

    /**
     * Map from section number to map from student ID student registration list for those students in the 4-course pace
     * who have forfeited at least one course.
     */
    public final Population fourWithForfeit;

    /**
     * Map from section number to map from student ID student registration list for those students in the 3-course
     * pace.
     */
    public final Population three;

    /**
     * Map from section number to map from student ID student registration list for those students in the 3-course pace
     * who have forfeited at least one course.
     */
    public final Population threeWithForfeit;

    /**
     * Map from section number to map from student ID student registration list for those students in the 2-course pace,
     * track A.
     */
    public final Population twoA;

    /**
     * Map from section number to map from student ID student registration list for those students in the 2-course pace,
     * track A, who have forfeited at least one course.
     */
    public final Population twoAWithForfeit;

    /**
     * Map from section number to map from student ID student registration list for those students in the 2-course pace,
     * track B.
     */
    public final Population twoB;

    /**
     * Map from section number to map from student ID student registration list for those students in the 2-course pace,
     * track B, who have forfeited at least one course.
     */
    public final Population twoBWithForfeit;

    /**
     * Map from section number to map from student ID student registration list for those students in the 2-course pace,
     * track C.
     */
    public final Population twoC;

    /**
     * Map from section number to map from student ID student registration list for those students in the 2-course pace,
     * track C, who have forfeited at least one course.
     */
    public final Population twoCWithForfeit;

    /**
     * Map from section number to map from student ID student registration list for those students in the 1-course pace,
     * track A.
     */
    public final Population oneA;

    /**
     * Map from section number to map from student ID student registration list for those students in the 1-course pace,
     * track B.
     */
    public final Population oneB;

    /**
     * Map from section number to map from student ID student registration list for those students in the 1-course pace,
     * track C.
     */
    public final Population oneC;

    /** The total number of students classified. */
    int totalStudents;

    /**
     * Constructs a new {@code MessagePopulationScanner}.
     *
     * @param theCache                 the cache
     * @param theIncCourseSections a map from course ID to a list of section IDs to include
     */
    MessagePopulationBuilder(final Cache theCache,
                             final Map<String, ? extends List<String>> theIncCourseSections) {

        this.cache = theCache;
        this.includeCourseSections = theIncCourseSections;

        this.nonCountedIncomplete = new Population();

        this.five = new Population();
        this.fiveWithForfeit = new Population();
        this.four = new Population();
        this.fourWithForfeit = new Population();
        this.three = new Population();
        this.threeWithForfeit = new Population();
        this.twoA = new Population();
        this.twoAWithForfeit = new Population();
        this.twoB = new Population();
        this.twoBWithForfeit = new Population();
        this.twoC = new Population();
        this.twoCWithForfeit = new Population();
        this.oneA = new Population();
        this.oneB = new Population();
        this.oneC = new Population();
    }

    /**
     * Performs the scan.
     *
     * @throws SQLException if there is an error accessing the database
     */
    public void scan() throws SQLException {

        final TermRec activeTerm = this.cache.getSystemData().getActiveTerm();

        if (activeTerm != null) {
            // Get all non-dropped, non-challenge-credit registrations in the active term (this
            // will include Incompletes)
            final List<RawStcourse> allRegs =
                    RawStcourseLogic.queryByTerm(this.cache, activeTerm.term, false, false);

            final Iterator<RawStcourse> iter = allRegs.iterator();
            while (iter.hasNext()) {
                final RawStcourse row = iter.next();
                final List<String> sections = this.includeCourseSections.get(row.course);
                if (sections == null || !sections.contains(row.sect)) {
                    iter.remove();
                }
            }

            // Group registrations by student
            final Map<String, List<RawStcourse>> byStudent = groupByStudent(allRegs);

            // Filter to ensure at most one record per course
            filter(byStudent);

            // Classify by student situation (Incomplete, or by pace schedule)
            classify(byStudent);
        }
    }

    /**
     * Categorizes registrations by student.
     *
     * @param allRegs the list of all registrations
     * @return a map from student ID to the list of all registrations for that student
     */
    private static Map<String, List<RawStcourse>> groupByStudent(final Iterable<RawStcourse> allRegs) {

        final Map<String, List<RawStcourse>> result = new HashMap<>(1500);

        for (final RawStcourse reg : allRegs) {
            if ("G".equals(reg.openStatus)) {
                continue;
            }

            final String stuId = reg.stuId;
            final List<RawStcourse> list = result.computeIfAbsent(stuId, s -> new ArrayList<>(5));
            list.add(reg);
        }

        return result;
    }

    /**
     * Filters registration lists to ensure there is at most one record per course per student, and reports any problems
     * found.
     *
     * @param byStudent the complete map from student to registration list
     */
    private static void filter(final Map<String, List<RawStcourse>> byStudent) {

        final Collection<RawStcourse> found = new ArrayList<>(5);

        for (final Map.Entry<String, List<RawStcourse>> entry : byStudent.entrySet()) {

            final String stuId = entry.getKey();
            final List<RawStcourse> regs = entry.getValue();

            RawStcourse m117 = null;
            RawStcourse m118 = null;
            RawStcourse m124 = null;
            RawStcourse m125 = null;
            RawStcourse m126 = null;

            for (final RawStcourse reg : regs) {
                final String course = reg.course;

                switch (course) {
                    case RawRecordConstants.M117 -> m117 = reg;
                    case RawRecordConstants.M118 -> m118 = reg;
                    case RawRecordConstants.M124 -> m124 = reg;
                    case RawRecordConstants.M125 -> m125 = reg;
                    case RawRecordConstants.M126 -> m126 = reg;
                    case null, default -> Log.warning("Unrecognized course: ", course, " for student ", stuId);
                }
            }

            if (m117 != null) {
                found.add(m117);
            }
            if (m118 != null) {
                found.add(m118);
            }
            if (m124 != null) {
                found.add(m124);
            }
            if (m125 != null) {
                found.add(m125);
            }
            if (m126 != null) {
                found.add(m126);
            }

            if (found.size() < regs.size()) {
                Log.warning("Student ", stuId + " has invalid registration");
                regs.clear();
                regs.addAll(found);
            }

            found.clear();
        }
    }

    /**
     * Classifies students by type, adding their registration lists to the appropriate map.
     *
     * @param byStudent the complete map from student to registration list
     */
    private void classify(final Map<String, List<RawStcourse>> byStudent) {

        this.totalStudents = 0;

        for (final Map.Entry<String, List<RawStcourse>> entry : byStudent.entrySet()) {

            final String stuId = entry.getKey();
            final List<RawStcourse> regs = entry.getValue();

            String iSection = null;
            String section = null;
            int pace = 0;
            int numForfeit = 0;

            for (final RawStcourse reg : regs) {
                if ("Y".equals(reg.iInProgress) && "N".equals(reg.iCounted)) {
                    iSection = reg.sect;
                    break;
                }

                if ("N".equals(reg.openStatus)) {
                    ++numForfeit;
                } else if (section == null) {
                    section = reg.sect;
                } else if (!section.equals(reg.sect)) {
                    if (section.compareTo(reg.sect) > 0) {
                        section = reg.sect;
                    }
                    Log.warning("Student ", stuId, " has mixture of section numbers");
                }

                ++pace;
            }

            final int checkPace = PaceTrackLogic.determinePace(regs);
            if (checkPace != pace) {
                Log.warning("Questionable pace calculator for student ", stuId, " (",
                        Integer.toString(pace), " or ", Integer.toString(checkPace), "?)");
            }

            if (iSection == null) {
                if (section != null) {
                    if (pace == 5) {
                        if (numForfeit == 0) {
                            addToPopulation(this.five, section, stuId, regs);
                        } else {
                            addToPopulation(this.fiveWithForfeit, section, stuId, regs);
                        }
                    } else if (pace == 4) {
                        if (numForfeit == 0) {
                            addToPopulation(this.four, section, stuId, regs);
                        } else {
                            addToPopulation(this.fourWithForfeit, section, stuId, regs);
                        }
                    } else if (pace == 3) {
                        if (numForfeit == 0) {
                            addToPopulation(this.three, section, stuId, regs);
                        } else {
                            addToPopulation(this.threeWithForfeit, section, stuId, regs);
                        }
                    } else {
                        final String paceTrack = PaceTrackLogic.determinePaceTrack(regs);

                        if (pace == 2) {
                            if (TRACK_A.equals(paceTrack)) {
                                if (numForfeit == 0) {
                                    addToPopulation(this.twoA, section, stuId, regs);
                                } else {
                                    addToPopulation(this.twoAWithForfeit, section, stuId, regs);
                                }
                            } else if (TRACK_B.equals(paceTrack)) {
                                if (numForfeit == 0) {
                                    addToPopulation(this.twoB, section, stuId, regs);
                                } else {
                                    addToPopulation(this.twoBWithForfeit, section, stuId, regs);
                                }
                            } else if (TRACK_C.equals(paceTrack)) {
                                if (numForfeit == 0) {
                                    addToPopulation(this.twoC, section, stuId, regs);
                                } else {
                                    addToPopulation(this.twoCWithForfeit, section, stuId, regs);
                                }
                            }
                        } else if (pace == 1) {
                            if (TRACK_A.equals(paceTrack)) {
                                addToPopulation(this.oneA, section, stuId, regs);
                            } else if (TRACK_B.equals(paceTrack)) {
                                addToPopulation(this.oneB, section, stuId, regs);
                            } else if (TRACK_C.equals(paceTrack)) {
                                addToPopulation(this.oneC, section, stuId, regs);
                            }
                        } else {
                            Log.warning("Student ", stuId + " has bad pace: ",
                                    Integer.toString(pace));
                        }
                    }
                }
            } else {
                addToPopulation(this.nonCountedIncomplete, iSection, stuId, regs);
            }
        }
    }

    /**
     * Adds a student's registration list to a population under a specified section number.
     *
     * @param population the population to which to add the list
     * @param section    the section number
     * @param stuId      the student ID
     * @param regs       the list of registrations
     */
    private void addToPopulation(final Population population, final String section,
                                 final String stuId, final List<RawStcourse> regs) {

        PopulationSection inner = population.sections.get(section);
        if (inner == null) {
            inner = new PopulationSection();
            population.sections.put(section, inner);
        }
        inner.students.put(stuId, regs);

        ++this.totalStudents;
    }
}
