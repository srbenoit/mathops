package dev.mathops.app.sim.registration;

import dev.mathops.app.sim.courses.Course;
import dev.mathops.app.sim.courses.EMeetingDays;
import dev.mathops.app.sim.courses.OfferedCourse;
import dev.mathops.app.sim.courses.OfferedSection;
import dev.mathops.app.sim.courses.OfferedSectionMeetingTime;
import dev.mathops.app.sim.students.StudentClassPreferences;
import dev.mathops.app.sim.students.StudentPopulation;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * A base class for Spur registration simulations.
 */
enum SpurRegSim {
    ;

    /** Number of times to simulate registration. */
    private static final int NUM_TRIALS = 1000;

    /**
     * Simulates registration.
     *
     * @param offeredCourses the map of offered courses
     * @param population     the student population
     * @param printResult    true to print the resulting schedule
     * @return the average quality score
     */
    static double simulateRegistrations(final Map<Course, OfferedCourse> offeredCourses,
                                        final StudentPopulation population, final boolean printResult) {

        final int popSize = population.getSize();

        final Map<StudentClassPreferences, Integer> counts = population.getCounts();
        final List<StudentClassPreferences> unordered = new ArrayList<>(popSize);
        final Collection<StudentClassPreferences> randomized = new ArrayList<>(popSize);
        final Collection<EnrollingStudent> students = new ArrayList<>(popSize);

        for (final OfferedCourse offeredCourse : offeredCourses.values()) {
            for (final List<OfferedSection> sections : offeredCourse.getSectionsLists()) {
                for (final OfferedSection sect : sections) {
                    sect.clearCollisions();
                }
            }
        }

        final long seed = System.currentTimeMillis() + System.nanoTime();
        final Random rnd = new Random(seed);

        // Run 100 trial registration processes, and track average results...

        double totalQuality = 0.0;
        for (int trial = 0; trial < NUM_TRIALS; ++trial) {
            // Generate a randomly ordered list of student preferences
            unordered.clear();
            for (final Map.Entry<StudentClassPreferences, Integer> entry : counts.entrySet()) {
                final StudentClassPreferences prefs = entry.getKey();
                final int count = entry.getValue().intValue();
                for (int i = 0; i < count; ++i) {
                    unordered.add(prefs);
                }
            }

            randomized.clear();
            int remaining = unordered.size();
            while (remaining > 0) {
                final int index = rnd.nextInt(remaining);
                final StudentClassPreferences chosen = unordered.remove(index);
                randomized.add(chosen);
                --remaining;
            }

            // Simulate the registration process
            students.clear();
            double trialQuality = 0.0;
            for (final StudentClassPreferences prefs : randomized) {
                final EnrollingStudent student = registerStudent(prefs, offeredCourses, rnd);
                trialQuality += student.getQuality();
                students.add(student);
            }

            final int numStudents = students.size();
            final double avgTrialQuality = trialQuality / (double) numStudents;
            totalQuality += avgTrialQuality;

            for (final OfferedCourse offeredCourse : offeredCourses.values()) {
                for (final List<OfferedSection> sectionsList : offeredCourse.getSectionsLists()) {
                    for (final OfferedSection sect : sectionsList) {
                        sect.captureEnrollment();
                        sect.clearEnrolledStudents();
                    }
                }
            }
        }

        if (printResult) {
            Log.fine(CoreConstants.CRLF);
            for (final OfferedCourse offeredCourse : offeredCourses.values()) {
                final Course course = offeredCourse.getCourse();
                Log.fine(course.courseId, ": ");
                for (final List<OfferedSection> sections : offeredCourse.getSectionsLists()) {
                    for (final OfferedSection section : sections) {

                        final int enrolled = section.averageEnrollment();
                        final String enrolledStr = Integer.toString(enrolled);

                        final int capacity = section.getTotalSeats();
                        final String capacityStr = Integer.toString(capacity);

                        Log.fine("    ", enrolledStr, "/", capacityStr, " ", section);

                        for (final Map.Entry<OfferedSection, Integer> entry : section.getCollisions().entrySet()) {
                            if (entry.getValue().intValue() > 10000) {
                                Log.fine("        ", entry.getValue(), " collisions with ", entry.getKey());
                            }
                        }
                    }
                }
            }
        }

        final double avgQuality = totalQuality / (double) NUM_TRIALS;

        return avgQuality;
    }

    /**
     * Simulate the registration of a single enrolling student.
     *
     * @param prefs          the class preferences
     * @param offeredCourses the set of offered courses
     * @param rnd            a random number generator
     * @return the enrolling student record
     */
    static EnrollingStudent registerStudent(final StudentClassPreferences prefs,
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
                sect.recordCollision(existing);
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
     * Finds the permutation of three classes that gives the highest quality score.
     *
     * @param count          a string to emit to logs to help track progress
     * @param offeredCourses the map from course to offered course information
     * @param population     the student population
     * @param course1        the first course to permute
     * @param course2        the second course to permute
     * @param course3        the third course to permute
     */
    static void findBest(final int count, final Map<Course, OfferedCourse> offeredCourses,
                         final StudentPopulation population,
                         final Course course1, final Course course2, final Course course3) {

        final String countStr = Integer.toString(count);
        Log.fine("Permutation ", countStr);

        final double score1 = simulateRegistrations(offeredCourses, population, false);
        final String score1Str = Double.toString(score1);
        Log.fine("   score 1 = ", score1Str);

        swap(offeredCourses, course1, course2);

        final double score2 = simulateRegistrations(offeredCourses, population, false);
        final String score2Str = Double.toString(score2);
        Log.fine("   score 2 = ", score2Str);

        swap(offeredCourses, course1, course3);

        final double score3 = simulateRegistrations(offeredCourses, population, false);
        final String score3Str = Double.toString(score3);
        Log.fine("   score 3 = ", score3Str);

        swap(offeredCourses, course1, course2);

        final double score4 = simulateRegistrations(offeredCourses, population, false);
        final String score4Str = Double.toString(score4);
        Log.fine("   score 4 = ", score4Str);

        swap(offeredCourses, course1, course3);

        final double score5 = simulateRegistrations(offeredCourses, population, false);
        final String score5Str = Double.toString(score5);
        Log.fine("   score 5 = ", score5Str);

        swap(offeredCourses, course1, course2);

        final double score6 = simulateRegistrations(offeredCourses, population, false);
        final String score6Str = Double.toString(score6);
        Log.fine("   score 6 = ", score6Str);

        double max = score1;
        int whichIsMax = 1;
        if (score2 > max) {
            max = score2;
            whichIsMax = 2;
        }
        if (score3 > max) {
            max = score3;
            whichIsMax = 3;
        }
        if (score4 > max) {
            max = score4;
            whichIsMax = 4;
        }
        if (score5 > max) {
            max = score5;
            whichIsMax = 5;
        }
        if (score6 > max) {
            max = score6;
            whichIsMax = 6;
        }
        final String maxStr = Double.toString(max);
        Log.fine("   *** BEST = ", maxStr);

        // Put the course offerings into the "best" configuration
        if (whichIsMax == 1) {
            swap(offeredCourses, course1, course3);
        } else if (whichIsMax == 2) {
            swap(offeredCourses, course1, course3);
            swap(offeredCourses, course1, course2);
        } else if (whichIsMax == 3) {
            swap(offeredCourses, course2, course3);
        } else if (whichIsMax == 4) {
            swap(offeredCourses, course2, course3);
            swap(offeredCourses, course1, course2);
        } else if (whichIsMax == 5) {
            swap(offeredCourses, course1, course2);
        }
    }

    /**
     * Swaps the section times for two courses
     *
     * @param offeredCourses a map from course ID offered course data
     * @param course1        the first course
     * @param course2        the second course
     */
    static void swap(final Map<Course, OfferedCourse> offeredCourses, final Course course1,
                     final Course course2) {

        final OfferedCourse offeredCourse1 = offeredCourses.get(course1);
        final OfferedCourse offeredCourse2 = offeredCourses.get(course2);

        final List<List<OfferedSection>> list1 = offeredCourse1.getSectionsLists();
        final List<List<OfferedSection>> list2 = offeredCourse2.getSectionsLists();

        final List<OfferedSection> inner1 = list1.getFirst();
        final List<OfferedSection> inner2 = list2.getFirst();

        final OfferedSection section1 = inner1.getFirst();
        final OfferedSection section2 = inner2.getFirst();

        final List<OfferedSectionMeetingTime> times1 = section1.getMeetingTimes();
        final List<OfferedSectionMeetingTime> times2 = section2.getMeetingTimes();
        final Collection<OfferedSectionMeetingTime> temp = new ArrayList<>(10);
        temp.addAll(times1);
        times1.clear();
        times1.addAll(times2);
        times2.clear();
        times2.addAll(temp);
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
        final int daysPerWeekStayHome = (startM == null ? 1 : 0) + (startT == null ? 1 : 0)
                                        + (startW == null ? 1 : 0) + (startR == null ? 1 : 0)
                                        + (startF == null ? 1 : 0);

        final double factor2 = 1.0 + daysPerWeekStayHome;

        // Factor 3 is the number of minutes a day the student is NOT on campus
        double factor3 = 1.0;

        if (startM != null && endM != null) {
            final int minutesFreeStart = (startM.getHour() * 60 + startM.getMinute()) - (60 * 8);
            final int minutesFreeEnd = (60 * 17) - (endM.getHour() * 60 + endM.getMinute());
            final int totalFree = minutesFreeStart + minutesFreeEnd;
            factor3 += totalFree;
        }

        if (startT != null && endT != null) {
            final int minutesFreeStart = (startT.getHour() * 60 + startT.getMinute()) - (60 * 8);
            final int minutesFreeEnd = (60 * 17) - (endT.getHour() * 60 + endT.getMinute());
            final int totalFree = minutesFreeStart + minutesFreeEnd;
            factor3 += totalFree;
        }

        if (startW != null && endW != null) {
            final int minutesFreeStart = (startW.getHour() * 60 + startW.getMinute()) - (60 * 8);
            final int minutesFreeEnd = (60 * 17) - (endW.getHour() * 60 + endW.getMinute());
            final int totalFree = minutesFreeStart + minutesFreeEnd;
            factor3 += totalFree;
        }

        if (startR != null && endR != null) {
            final int minutesFreeStart = (startR.getHour() * 60 + startR.getMinute()) - (60 * 8);
            final int minutesFreeEnd = (60 * 17) - (endR.getHour() * 60 + endR.getMinute());
            final int totalFree = minutesFreeStart + minutesFreeEnd;
            factor3 += totalFree;
        }

        if (startF != null && endF != null) {
            final int minutesFreeStart = (startF.getHour() * 60 + startF.getMinute()) - (60 * 8);
            final int minutesFreeEnd = (60 * 17) - (endF.getHour() * 60 + endF.getMinute());
            final int totalFree = minutesFreeStart + minutesFreeEnd;
            factor3 += totalFree;
        }

//        Log.fine("  Factor 1 = " + (float) factor1 + ", factor 2 = " + (float) factor2 + ", factor 3 = "
//                 + (float) factor3);

        return factor1 * factor2 * factor3;
    }
}
