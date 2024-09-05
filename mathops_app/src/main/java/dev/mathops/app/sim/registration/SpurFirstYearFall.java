package dev.mathops.app.sim.registration;

import dev.mathops.app.sim.courses.Course;
import dev.mathops.app.sim.courses.EMeetingDays;
import dev.mathops.app.sim.courses.OfferedCourse;
import dev.mathops.app.sim.courses.OfferedSection;
import dev.mathops.app.sim.courses.OfferedSectionMeetingTime;
import dev.mathops.app.sim.courses.SpurCourses;
import dev.mathops.app.sim.rooms.SpurRooms;
import dev.mathops.app.sim.students.SpurStudents;
import dev.mathops.app.sim.students.StudentClassPreferences;
import dev.mathops.app.sim.students.StudentPopulation;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * A simulation of the Spur first-year Fall semester.
 */
final class SpurFirstYearFall {

    /** The population size. */
    private static final int POPULATION_SIZE = 160;

    /** The size of a general classroom. */
    private static final int CLASSROOM_SIZE = 40;

    /** The size of a general lab. */
    private static final int LAB_SIZE = 26;

    /** A meeting time label. */
    private static final String CLASS = "Class";

    /** A meeting time label. */
    private static final String LAB = "Lab";

    /** A meeting time label. */
    private static final String RECITATION = "Recitation";

    /** A classroom ID. */
    private static final String CLASSROOM_1 = SpurRooms.CLASSROOM_1.getId();

    /** A classroom ID. */
    private static final String CLASSROOM_2 = SpurRooms.CLASSROOM_2.getId();

    /** A classroom ID. */
    private static final String LAB_1 = SpurRooms.LAB_1.getId();

    /**
     * Constructs a new {@code SpurFirstYearFall}.
     */
    private SpurFirstYearFall() {

        // No action
    }

    /**
     * Runs the simulation.
     */
    private static void runSimulation() {

        // Set up the offered courses

        final Map<Course, OfferedCourse> offeredCourses = new HashMap<>(20);

        // SEMINAR

        final OfferedCourse courseSeminar = new OfferedCourse(SpurCourses.SEMINAR);
        offeredCourses.put(SpurCourses.SEMINAR, courseSeminar);

        final Collection<OfferedSection> sectionsSeminar = new ArrayList<>(4);
        sectionsSeminar.add(new OfferedSection(courseSeminar, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.T, CLASSROOM_1,
                        LocalTime.of(15, 30), LocalTime.of(16, 20))));
        sectionsSeminar.add(new OfferedSection(courseSeminar, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.R, CLASSROOM_1,
                        LocalTime.of(15, 30), LocalTime.of(16, 20))));
        sectionsSeminar.add(new OfferedSection(courseSeminar, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.F, CLASSROOM_1,
                        LocalTime.of(9, 0), LocalTime.of(9, 50))));
        sectionsSeminar.add(new OfferedSection(courseSeminar, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.F, CLASSROOM_1,
                        LocalTime.of(10, 0), LocalTime.of(10, 50))));
        sectionsSeminar.add(new OfferedSection(courseSeminar, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_2,
                        LocalTime.of(8, 30), LocalTime.of(9, 15))));
        courseSeminar.addSectionsList(sectionsSeminar);

        // LIFE 102

        final OfferedCourse courseLife102 = new OfferedCourse(SpurCourses.LIFE102);
        offeredCourses.put(SpurCourses.LIFE102, courseLife102);

        final Collection<OfferedSection> sectionsLife102Class = new ArrayList<>(4);
        sectionsLife102Class.add(new OfferedSection(courseLife102, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                        LocalTime.of(10, 0), LocalTime.of(10, 50))));
        sectionsLife102Class.add(new OfferedSection(courseLife102, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                        LocalTime.of(12, 0), LocalTime.of(12, 50))));
        sectionsLife102Class.add(new OfferedSection(courseLife102, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                        LocalTime.of(14, 0), LocalTime.of(14, 50))));
        sectionsLife102Class.add(new OfferedSection(courseLife102, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                        LocalTime.of(16, 0), LocalTime.of(16, 50))));
        courseLife102.addSectionsList(sectionsLife102Class);

        final Collection<OfferedSection> sectionsLife102Lab = new ArrayList<>(4);
        sectionsLife102Lab.add(new OfferedSection(courseLife102, LAB_SIZE,
                new OfferedSectionMeetingTime(LAB, EMeetingDays.R, LAB_1,
                        LocalTime.of(8, 0), LocalTime.of(10, 45))));
        sectionsLife102Lab.add(new OfferedSection(courseLife102, LAB_SIZE,
                new OfferedSectionMeetingTime(LAB, EMeetingDays.T, LAB_1,
                        LocalTime.of(11, 0), LocalTime.of(13, 45))));
        sectionsLife102Lab.add(new OfferedSection(courseLife102, LAB_SIZE,
                new OfferedSectionMeetingTime(LAB, EMeetingDays.R, LAB_1,
                        LocalTime.of(11, 0), LocalTime.of(13, 45))));
        sectionsLife102Lab.add(new OfferedSection(courseLife102, LAB_SIZE,
                new OfferedSectionMeetingTime(LAB, EMeetingDays.T, LAB_1,
                        LocalTime.of(14, 0), LocalTime.of(16, 45))));
        sectionsLife102Lab.add(new OfferedSection(courseLife102, LAB_SIZE,
                new OfferedSectionMeetingTime(LAB, EMeetingDays.R, LAB_1,
                        LocalTime.of(14, 0), LocalTime.of(16, 45))));
        courseLife102.addSectionsList(sectionsLife102Lab);

        // MATH 112

        final OfferedCourse courseMath112 = new OfferedCourse(SpurCourses.MATH112);
        offeredCourses.put(SpurCourses.MATH112, courseMath112);

        final Collection<OfferedSection> sectionsMath112Class = new ArrayList<>(4);
        sectionsMath112Class.add(new OfferedSection(courseMath112, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                        LocalTime.of(9, 0), LocalTime.of(9, 50))));
        sectionsMath112Class.add(new OfferedSection(courseMath112, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                        LocalTime.of(11, 0), LocalTime.of(11, 50))));
        sectionsMath112Class.add(new OfferedSection(courseMath112, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                        LocalTime.of(13, 0), LocalTime.of(13, 50))));
        sectionsMath112Class.add(new OfferedSection(courseMath112, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                        LocalTime.of(15, 0), LocalTime.of(15, 50))));
        courseMath112.addSectionsList(sectionsMath112Class);

        // CS 150B

        final OfferedCourse courseCs150b = new OfferedCourse(SpurCourses.CS150B);
        offeredCourses.put(SpurCourses.CS150B, courseCs150b);

        final Collection<OfferedSection> sectionsCs150bClass = new ArrayList<>(2);
        sectionsCs150bClass.add(new OfferedSection(courseCs150b, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MW, CLASSROOM_1,
                        LocalTime.of(9, 0), LocalTime.of(9, 50))));
        sectionsCs150bClass.add(new OfferedSection(courseCs150b, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MW, CLASSROOM_1,
                        LocalTime.of(10, 0), LocalTime.of(10, 50))));
        courseCs150b.addSectionsList(sectionsCs150bClass);

        final Collection<OfferedSection> sectionsCs150bRecitation = new ArrayList<>(2);
        sectionsCs150bRecitation.add(new OfferedSection(courseCs150b, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(RECITATION, EMeetingDays.T, CLASSROOM_1,
                        LocalTime.of(9, 0), LocalTime.of(10, 45))));
        sectionsCs150bRecitation.add(new OfferedSection(courseCs150b, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(RECITATION, EMeetingDays.R, CLASSROOM_1,
                        LocalTime.of(9, 0), LocalTime.of(10, 45))));
        courseCs150b.addSectionsList(sectionsCs150bRecitation);

        // IDEA 110

        final OfferedCourse courseIdea110 = new OfferedCourse(SpurCourses.IDEA110);
        offeredCourses.put(SpurCourses.IDEA110, courseIdea110);

        final Collection<OfferedSection> sectionsIdea110Class = new ArrayList<>(2);
        sectionsIdea110Class.add(new OfferedSection(courseIdea110, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                        LocalTime.of(12, 0), LocalTime.of(12, 50))));
        sectionsIdea110Class.add(new OfferedSection(courseIdea110, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                        LocalTime.of(14, 0), LocalTime.of(14, 50))));
        courseIdea110.addSectionsList(sectionsIdea110Class);

        // HDFS 101

        final OfferedCourse courseHdfs101 = new OfferedCourse(SpurCourses.HDFS101);
        offeredCourses.put(SpurCourses.HDFS101, courseHdfs101);

        final Collection<OfferedSection> sectionsHdfs101Class = new ArrayList<>(2);
        sectionsHdfs101Class.add(new OfferedSection(courseHdfs101, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                        LocalTime.of(11, 0), LocalTime.of(11, 50))));
        sectionsHdfs101Class.add(new OfferedSection(courseHdfs101, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                        LocalTime.of(13, 0), LocalTime.of(13, 50))));
        courseHdfs101.addSectionsList(sectionsHdfs101Class);

        // AGRI 116

        final OfferedCourse courseAgri116 = new OfferedCourse(SpurCourses.AGRI116);
        offeredCourses.put(SpurCourses.AGRI116, courseAgri116);

        final Collection<OfferedSection> sectionsAgri116Class = new ArrayList<>(2);
        sectionsAgri116Class.add(new OfferedSection(courseAgri116, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_2,
                        LocalTime.of(11, 0), LocalTime.of(12, 15))));
        sectionsAgri116Class.add(new OfferedSection(courseAgri116, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_2,
                        LocalTime.of(12, 30), LocalTime.of(13, 45))));
        courseAgri116.addSectionsList(sectionsAgri116Class);

        // AB 111

        final OfferedCourse courseAb111 = new OfferedCourse(SpurCourses.AB111);
        offeredCourses.put(SpurCourses.AB111, courseAb111);

        final Collection<OfferedSection> sectionsAb111Class = new ArrayList<>(2);
        sectionsAb111Class.add(new OfferedSection(courseAb111, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_2,
                        LocalTime.of(14, 0), LocalTime.of(15, 15))));
        courseAb111.addSectionsList(sectionsAb111Class);

        // EHRS 220

        final OfferedCourse courseEhrs220 = new OfferedCourse(SpurCourses.EHRS220);
        offeredCourses.put(SpurCourses.EHRS220, courseEhrs220);

        final Collection<OfferedSection> sectionsEhrs220Class = new ArrayList<>(2);
        sectionsEhrs220Class.add(new OfferedSection(courseEhrs220, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_2,
                        LocalTime.of(15, 30), LocalTime.of(16, 45))));
        courseEhrs220.addSectionsList(sectionsEhrs220Class);

        // POLS 131

        final OfferedCourse coursePols131 = new OfferedCourse(SpurCourses.POLS131);
        offeredCourses.put(SpurCourses.POLS131, coursePols131);

        final Collection<OfferedSection> sectionsPols131Class = new ArrayList<>(2);
        sectionsPols131Class.add(new OfferedSection(coursePols131, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                        LocalTime.of(16, 0), LocalTime.of(16, 50))));
        coursePols131.addSectionsList(sectionsPols131Class);

        // AREC 222

        final OfferedCourse courseArec222 = new OfferedCourse(SpurCourses.AREC222);
        offeredCourses.put(SpurCourses.AREC222, courseArec222);

        final Collection<OfferedSection> sectionsArec222Class = new ArrayList<>(2);
        sectionsArec222Class.add(new OfferedSection(courseArec222, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_2,
                        LocalTime.of(9, 30), LocalTime.of(10, 45))));
        sectionsArec222Class.add(new OfferedSection(courseArec222, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_1,
                        LocalTime.of(12, 30), LocalTime.of(13, 45))));
        courseArec222.addSectionsList(sectionsArec222Class);

        // SPCM 100

        final OfferedCourse courseSpcm100 = new OfferedCourse(SpurCourses.SPCM100);
        offeredCourses.put(SpurCourses.SPCM100, courseSpcm100);

        final Collection<OfferedSection> sectionsSpcm100Class = new ArrayList<>(2);
        sectionsSpcm100Class.add(new OfferedSection(courseSpcm100, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_1,
                        LocalTime.of(11, 0), LocalTime.of(12, 15))));
        sectionsSpcm100Class.add(new OfferedSection(courseSpcm100, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_1,
                        LocalTime.of(14, 0), LocalTime.of(15, 15))));
        courseSpcm100.addSectionsList(sectionsSpcm100Class);

        // BZ 101

        final OfferedCourse courseBz101 = new OfferedCourse(SpurCourses.BZ101);
        offeredCourses.put(SpurCourses.BZ101, courseBz101);

        final Collection<OfferedSection> sectionsBz101Class = new ArrayList<>(2);
        sectionsBz101Class.add(new OfferedSection(courseBz101, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                        LocalTime.of(15, 0), LocalTime.of(15, 50))));
        courseBz101.addSectionsList(sectionsBz101Class);

        // Generate the students who will register (160 students using the Fall preference distribution)

        final StudentPopulation population = new StudentPopulation(SpurStudents.SPUR_FALL_DISTRIBUTION,
                POPULATION_SIZE);
        final Map<StudentClassPreferences, Integer> counts = population.getCounts();

        // Generate a list of student preferences in random order

        final List<StudentClassPreferences> unordered = new ArrayList<>(POPULATION_SIZE);
        for (final Map.Entry<StudentClassPreferences, Integer> entry : counts.entrySet()) {
            final StudentClassPreferences prefs = entry.getKey();
            final int count = entry.getValue().intValue();
            for (int i = 0; i < count; ++i) {
                unordered.add(prefs);
            }
        }
        final long seed = System.currentTimeMillis() + System.nanoTime();
        final Random rnd = new Random(seed);
        int remaining = unordered.size();
        final Collection<StudentClassPreferences> randomized = new ArrayList<>(remaining);
        while (remaining > 0) {
            final int index = rnd.nextInt(remaining);
            randomized.add(unordered.get(index));
            --remaining;
        }

        // Simulate the registration process

        final Collection<EnrollingStudent> students = new ArrayList<>(POPULATION_SIZE);
        for (final StudentClassPreferences prefs : randomized) {
            final EnrollingStudent student = registerStudent(prefs, offeredCourses, rnd);
            students.add(student);
        }

        // Print results

        double totalQuality = 0.0;
        for (final EnrollingStudent student : students) {
            totalQuality += student.getQuality();
//            Log.info(student);
        }
        Log.info("*** Average quality score = ", totalQuality / students.size());

        Log.fine(CoreConstants.CRLF);
        for (final OfferedCourse offeredCourse : offeredCourses.values()) {
            final Course course = offeredCourse.getCourse();
            Log.fine(course.courseId, ": ");
            for (final List<OfferedSection> sections : offeredCourse.getSectionsLists()) {
                for (final OfferedSection section : sections) {
                    final int enrolled = section.getEnrollment();
                    final String enrolledStr = Integer.toString(enrolled);

                    final int capacity = section.getTotalSeats();
                    final String capacityStr = Integer.toString(capacity);

                    Log.fine("    ", enrolledStr, "/", capacityStr, " ", section);
                }
            }
        }
    }

    /**
     * Simulate the registration of a single enrolling student.
     *
     * @param prefs          the class preferences
     * @param offeredCourses the set of offered courses
     * @param rnd            a random number generator
     * @return the enrolling student record
     */
    private static EnrollingStudent registerStudent(final StudentClassPreferences prefs,
                                                    final Map<Course, OfferedCourse> offeredCourses, final Random rnd) {

        int totalCredits = 0;

        final Collection<Course> registeredForCourses = new ArrayList<>(6);
        final List<List<OfferedSection>> possibleSectionChoices = new ArrayList<>(100);

        // Add any courses marked as "mandatory"

        for (final Map.Entry<Course, OfferedCourse> entry : offeredCourses.entrySet()) {
            final Course course = entry.getKey();
            if (course.mandatory) {
                final OfferedCourse offeredCourse = entry.getValue();
                if (isRegistrationPossible(possibleSectionChoices, offeredCourse)) {
                    registeredForCourses.add(course);
                    totalCredits += course.numCredits;
                } else {
                    Log.warning("Unable to register for MANDATORY course: ", course.courseId);
                }
            }
        }

        // Attempt to choose a course we have not already chosen until we have reached a target number of credits in
        // the student's schedule

        for (int j = 0; j < 1000; ++j) {
            final Course selected = prefs.pick(rnd);

            if (registeredForCourses.contains(selected)) {
                continue;
            }

            final OfferedCourse offeredCourse = offeredCourses.get(selected);
            if (Objects.nonNull(offeredCourse)) {
                final int credits = selected.numCredits;
                if (totalCredits + credits > prefs.maxCredits) {
                    break;
                }

                if (isRegistrationPossible(possibleSectionChoices, offeredCourse)) {
                    registeredForCourses.add(selected);
                    totalCredits += credits;

                    if (totalCredits >= prefs.minCredits) {
                        final int span = prefs.maxCredits - prefs.minCredits + 1;
                        final int delta = totalCredits - prefs.minCredits + 1;
                        // Two rolls to break out to skew toward the lower end of the credit counts.
                        if (rnd.nextInt(span) < delta) {
                            break;
                        }
                        if (rnd.nextInt(span) < delta) {
                            break;
                        }
                    }
                }
            }
        }

        // At this point, we the student has added everything they want to and can.  We may have many possible schedules
        // open to the student, so pick the most "desirable"

        EnrollingStudent result = null;

        final List<OfferedSection> bestSchedule = pickBestSchedule(possibleSectionChoices, prefs);

        if (Objects.nonNull(bestSchedule)) {
            final double quality = calculateQuality(bestSchedule, prefs);
            result = new EnrollingStudent(prefs, bestSchedule, quality);

            for (final OfferedSection sect : bestSchedule) {
                sect.addEnrolledStudent(result);
            }
        }

        if (totalCredits < prefs.minCredits) {
            Log.warning("Unable to reach minimum desired credits");
        }

        return result;
    }

    /**
     * Tests whether an offered course has sections that are compatible with the student's current list of possible
     * section choices.  If so, the offered course is added to the possible section choices.
     *
     * @param possibleSectionChoices the current set of possible section choices (one entry for every possible list of
     *                               sections this student could choose that do not conflict with each other)
     * @param offeredCourse          the new offered course to attempt to add
     * @return true if the new course was added; false if it could not be added
     */
    private static boolean isRegistrationPossible(final List<List<OfferedSection>> possibleSectionChoices,
                                                  final OfferedCourse offeredCourse) {

        final boolean result;

        final List<List<OfferedSection>> offeredSectionsLists = offeredCourse.getSectionsLists();

        if (possibleSectionChoices.isEmpty()) {
            // This is the first course - no need to test compatibility with existing track
            List<List<OfferedSection>> newPossibleTracks = new ArrayList<>(100);

            final int numLists = offeredSectionsLists.size();
            final List<OfferedSection> firstList = offeredSectionsLists.getFirst();
            for (final OfferedSection sect : firstList) {
                final List<OfferedSection> newPossibleTrack = new ArrayList<>(5);
                if (sect.getSeatsRemaining() > 0) {
                    newPossibleTrack.add(sect);
                    newPossibleTracks.add(newPossibleTrack);
                }
            }

            for (int i = 1; i < numLists; ++i) {
                final List<OfferedSection> nextList = offeredSectionsLists.get(i);
                newPossibleTracks = mergePossibleSections(newPossibleTracks, nextList);
            }

            if (newPossibleTracks.isEmpty()) {
                result = false;
            } else {
                possibleSectionChoices.addAll(newPossibleTracks);
                result = true;
            }
        } else {
            List<List<OfferedSection>> newPossibleTracks = new ArrayList<>(possibleSectionChoices);
            for (final List<OfferedSection> nextList : offeredSectionsLists) {
                newPossibleTracks = mergePossibleSections(newPossibleTracks, nextList);
            }

            if (newPossibleTracks.isEmpty()) {
                result = false;
            } else {
                possibleSectionChoices.clear();
                possibleSectionChoices.addAll(newPossibleTracks);
                result = true;
            }
        }

        return result;
    }

    /**
     * Given a current set of possible tracks (sequences of sections that do not conflict) and a set of choices for a
     * new section that is to be added, tests all new section choices for compatibility with each current sequence. The
     * result is a new list of possible tracks that include a section from the section choices.  If every possible
     * choice of section from the new section choices conflicted with some existing section, the returned list will be
     * empty.
     *
     * @param currentPossibleTracks the current set of all possible tracks
     * @param newSectionChoices     the set of choices available for the new section to add
     * @return the list of new possible tracks with the section included
     */
    private static List<List<OfferedSection>> mergePossibleSections(
            final Iterable<? extends List<OfferedSection>> currentPossibleTracks,
            final Iterable<OfferedSection> newSectionChoices) {

        final List<List<OfferedSection>> result = new ArrayList<>(100);

        for (final List<OfferedSection> track : currentPossibleTracks) {
            for (final OfferedSection sect : newSectionChoices) {
                if (sect.getSeatsRemaining() > 0 && isCompatible(track, sect)) {
                    final int count = track.size();
                    final List<OfferedSection> newTrack = new ArrayList<>(count + 1);
                    newTrack.addAll(track);
                    newTrack.add(sect);
                    result.add(newTrack);
                }
            }
        }

        return result;
    }

    /**
     * Tests whether a section is compatible (does not conflict) with a track.
     *
     * @param track the track
     * @param sect  the section
     * @return true if the section could be added to the track without conflict
     */
    private static boolean isCompatible(final Iterable<OfferedSection> track, final OfferedSection sect) {

        boolean compatible = true;

        for (final OfferedSection existing : track) {
            if (existing.hasConflict(sect)) {
                compatible = false;
                break;
            }
        }

        return compatible;
    }

    /**
     * Given a list of possible schedules, assigns each one a "quality score" and returns the list with the highest
     * score.
     *
     * @param possibleSectionChoices the list of possible lists of section choices
     * @param prefs                  the student class preferences
     * @return the section choice list with the highest quality score
     */
    private static List<OfferedSection> pickBestSchedule(
            final Iterable<? extends List<OfferedSection>> possibleSectionChoices,
            final StudentClassPreferences prefs) {

        double bestQuality = -1.0;
        List<OfferedSection> bestList = null;

        for (final List<OfferedSection> potential : possibleSectionChoices) {
            final double quality = calculateQuality(potential, prefs);

            if (quality > bestQuality) {
                bestList = potential;
                bestQuality = quality;
            }
        }

        return bestList;
    }

    /**
     * Compute a quality score for a potential schedule.
     *
     * @param potential the potential schedule
     * @param prefs     the preferences
     * @return the quality score
     */
    private static double calculateQuality(final Iterable<OfferedSection> potential,
                                           final StudentClassPreferences prefs) {

        // Factor 1 is the preference assigned to each course (this will be a number from 0.0 to 1.0, higher is better

        double factor1 = 0.1;
        int totalCredits = 0;
        for (final OfferedSection sect : potential) {
            final Course course = sect.getOfferedCourse().getCourse();
            totalCredits += course.numCredits;
            factor1 += prefs.getPreference(course);
        }

        // If the student could not take their desired number of credits, cut factor 1 in half
        if (totalCredits < prefs.minCredits) {
            factor1 *= 0.5;
        }

        // Look at the spans of time the student needs to be on campus each day.

        LocalTime startM = null;
        LocalTime endM = null;
        LocalTime startT = null;
        LocalTime endT = null;
        LocalTime startW = null;
        LocalTime endW = null;
        LocalTime startR = null;
        LocalTime endR = null;
        LocalTime startF = null;
        LocalTime endF = null;

        for (final OfferedSection sect : potential) {
            for (final OfferedSectionMeetingTime meetingTime : sect.getMeetingTimes()) {
                final EMeetingDays days = meetingTime.meetingDays();
                final LocalTime start = meetingTime.startTime();
                final LocalTime end = meetingTime.endTime();

                if (days.includesMonday()) {
                    if (startM == null || startM.isAfter(start)) {
                        startM = start;
                    }
                    if (endM == null || endM.isBefore(end)) {
                        endM = end;
                    }
                }
                if (days.includesTuesday()) {
                    if (startT == null || startT.isAfter(start)) {
                        startT = start;
                    }
                    if (endT == null || endT.isBefore(end)) {
                        endT = end;
                    }
                }

                if (days.includesWednesday()) {
                    if (startW == null || startW.isAfter(start)) {
                        startW = start;
                    }
                    if (endW == null || endW.isBefore(end)) {
                        endW = end;
                    }
                }
                if (days.includesThursday()) {
                    if (startR == null || startR.isAfter(start)) {
                        startR = start;
                    }
                    if (endR == null || endR.isBefore(end)) {
                        endR = end;
                    }
                }
                if (days.includesFriday()) {
                    if (startF == null || startF.isAfter(start)) {
                        startF = start;
                    }
                    if (endF == null || endF.isBefore(end)) {
                        endF = end;
                    }
                }
            }
        }

        // Factor 2 is based on the number of days a week the student does NOT need to be on campus
        final double factor2 = (startM == null ? 1.0 : 0.0) + (startT == null ? 1.0 : 0.0)
                               + (startW == null ? 1.0 : 0.0) + (startR == null ? 1.0 : 0.0)
                               + (startF == null ? 1.0 : 0.0);

        // Factor 3 is the number of hours a day the student is NOT on campus
        double factor3 = 1.0;

        if (startM != null && endM != null) {
            final int minutesFreeStart = (startM.getHour() * 60 + startM.getMinute()) - (60 * 8);
            final int minutesFreeEnd = (60 * 17) - (endM.getHour() * 60 + endM.getMinute());
            final int totalFree = minutesFreeStart + minutesFreeEnd;
            factor3 += (double) totalFree;
        }

        if (startT != null && endT != null) {
            final int minutesFreeStart = (startT.getHour() * 60 + startT.getMinute()) - (60 * 8);
            final int minutesFreeEnd = (60 * 17) - (endT.getHour() * 60 + endT.getMinute());
            final int totalFree = minutesFreeStart + minutesFreeEnd;
            factor3 += (double) totalFree;
        }

        if (startW != null && endW != null) {
            final int minutesFreeStart = (startW.getHour() * 60 + startW.getMinute()) - (60 * 8);
            final int minutesFreeEnd = (60 * 17) - (endW.getHour() * 60 + endW.getMinute());
            final int totalFree = minutesFreeStart + minutesFreeEnd;
            factor3 += (double) totalFree;
        }

        if (startR != null && endR != null) {
            final int minutesFreeStart = (startR.getHour() * 60 + startR.getMinute()) - (60 * 8);
            final int minutesFreeEnd = (60 * 17) - (endR.getHour() * 60 + endR.getMinute());
            final int totalFree = minutesFreeStart + minutesFreeEnd;
            factor3 += (double) totalFree;
        }

        if (startF != null && endF != null) {
            final int minutesFreeStart = (startF.getHour() * 60 + startF.getMinute()) - (60 * 8);
            final int minutesFreeEnd = (60 * 17) - (endF.getHour() * 60 + endF.getMinute());
            final int totalFree = minutesFreeStart + minutesFreeEnd;
            factor3 += (double) totalFree;
        }

        return factor1 * factor2 * factor3;
    }

    /**
     * Main method to execute the simulation.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        runSimulation();
    }
}
