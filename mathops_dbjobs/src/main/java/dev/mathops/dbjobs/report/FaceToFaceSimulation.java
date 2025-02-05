package dev.mathops.dbjobs.report;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.DbConnection;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.text.builder.HtmlBuilder;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * This class simulates student registrations in face-to-face sections, the collection of students into cohorts, the
 * assignment of rooms and instructors, and the delivery of these courses.
 */
public final class FaceToFaceSimulation {

    /** The fraction of the student population expected to want face-to-face." */
    private static final float PORTION_OF_POPULATION_IN_F2F = 1.0f;

    /** The maximum number of registrations per meeting time. */
    private static final int MAX_REGS_PER_MEETING_TIME = 400;

    /** The maximum capacity of a room. */
    private static final int ROOM_CAP = 10000;

//    /** The list of courses. */
//    public static final String[] COURSES = {RawRecordConstants.M117, RawRecordConstants.M118,
//            RawRecordConstants.M124, RawRecordConstants.M125, RawRecordConstants.M126};

    /** A standard meeting time. */
    private static final MeetingTime M08 = new MeetingTime(DayOfWeek.MONDAY, Integer.valueOf(800));

    /** A standard meeting time. */
    private static final MeetingTime M09 = new MeetingTime(DayOfWeek.MONDAY, Integer.valueOf(900));

    /** A standard meeting time. */
    private static final MeetingTime M10 = new MeetingTime(DayOfWeek.MONDAY, Integer.valueOf(1000));

    /** A standard meeting time. */
    private static final MeetingTime M11 = new MeetingTime(DayOfWeek.MONDAY, Integer.valueOf(1100));

    /** A standard meeting time. */
    private static final MeetingTime M12 = new MeetingTime(DayOfWeek.MONDAY, Integer.valueOf(1200));

    /** A standard meeting time. */
    private static final MeetingTime M13 = new MeetingTime(DayOfWeek.MONDAY, Integer.valueOf(1300));

    /** A standard meeting time. */
    private static final MeetingTime M14 = new MeetingTime(DayOfWeek.MONDAY, Integer.valueOf(1400));

    /** A standard meeting time. */
    private static final MeetingTime M15 = new MeetingTime(DayOfWeek.MONDAY, Integer.valueOf(1500));

    /** A standard meeting time. */
    private static final MeetingTime M16 = new MeetingTime(DayOfWeek.MONDAY, Integer.valueOf(1600));

    /** A standard meeting time. */
    private static final MeetingTime T08 = new MeetingTime(DayOfWeek.TUESDAY, Integer.valueOf(800));

    /** A standard meeting time. */
    private static final MeetingTime T09 = new MeetingTime(DayOfWeek.TUESDAY, Integer.valueOf(930));

    /** A standard meeting time. */
    private static final MeetingTime T11 = new MeetingTime(DayOfWeek.TUESDAY, Integer.valueOf(1100));

    /** A standard meeting time. */
    private static final MeetingTime T12 = new MeetingTime(DayOfWeek.TUESDAY, Integer.valueOf(1230));

    /** A standard meeting time. */
    private static final MeetingTime T14 = new MeetingTime(DayOfWeek.TUESDAY, Integer.valueOf(1400));

    /** A standard meeting time. */
    private static final MeetingTime T15 = new MeetingTime(DayOfWeek.TUESDAY, Integer.valueOf(1530));

    /** A standard meeting time. */
    private static final MeetingTime W08 = new MeetingTime(DayOfWeek.WEDNESDAY, Integer.valueOf(800));

    /** A standard meeting time. */
    private static final MeetingTime W09 = new MeetingTime(DayOfWeek.WEDNESDAY, Integer.valueOf(900));

    /** A standard meeting time. */
    private static final MeetingTime W10 = new MeetingTime(DayOfWeek.WEDNESDAY, Integer.valueOf(1000));

    /** A standard meeting time. */
    private static final MeetingTime W11 = new MeetingTime(DayOfWeek.WEDNESDAY, Integer.valueOf(1100));

    /** A standard meeting time. */
    private static final MeetingTime W12 = new MeetingTime(DayOfWeek.WEDNESDAY, Integer.valueOf(1200));

    /** A standard meeting time. */
    private static final MeetingTime W13 = new MeetingTime(DayOfWeek.WEDNESDAY, Integer.valueOf(1300));

    /** A standard meeting time. */
    private static final MeetingTime W14 = new MeetingTime(DayOfWeek.WEDNESDAY, Integer.valueOf(1400));

    /** A standard meeting time. */
    private static final MeetingTime W15 = new MeetingTime(DayOfWeek.WEDNESDAY, Integer.valueOf(1500));

    /** A standard meeting time. */
    private static final MeetingTime W16 = new MeetingTime(DayOfWeek.WEDNESDAY, Integer.valueOf(1600));

    /** A standard meeting time. */
    private static final MeetingTime R08 = new MeetingTime(DayOfWeek.THURSDAY, Integer.valueOf(800));

    /** A standard meeting time. */
    private static final MeetingTime R09 = new MeetingTime(DayOfWeek.THURSDAY, Integer.valueOf(930));

    /** A standard meeting time. */
    private static final MeetingTime R11 = new MeetingTime(DayOfWeek.THURSDAY, Integer.valueOf(1100));

    /** A standard meeting time. */
    private static final MeetingTime R12 = new MeetingTime(DayOfWeek.THURSDAY, Integer.valueOf(1230));

    /** A standard meeting time. */
    private static final MeetingTime R14 = new MeetingTime(DayOfWeek.THURSDAY, Integer.valueOf(1400));

    /** A standard meeting time. */
    private static final MeetingTime R15 = new MeetingTime(DayOfWeek.THURSDAY, Integer.valueOf(1530));

    /** A standard meeting time. */
    private static final MeetingTime F08 = new MeetingTime(DayOfWeek.FRIDAY, Integer.valueOf(800));

    /** A standard meeting time. */
    private static final MeetingTime F09 = new MeetingTime(DayOfWeek.FRIDAY, Integer.valueOf(900));

    /** A standard meeting time. */
    private static final MeetingTime F10 = new MeetingTime(DayOfWeek.FRIDAY, Integer.valueOf(1000));

    /** A standard meeting time. */
    private static final MeetingTime F11 = new MeetingTime(DayOfWeek.FRIDAY, Integer.valueOf(1100));

    /** A standard meeting time. */
    private static final MeetingTime F12 = new MeetingTime(DayOfWeek.FRIDAY, Integer.valueOf(1200));

    /** A standard meeting time. */
    private static final MeetingTime F13 = new MeetingTime(DayOfWeek.FRIDAY, Integer.valueOf(1300));

    /** A standard meeting time. */
    private static final MeetingTime F14 = new MeetingTime(DayOfWeek.FRIDAY, Integer.valueOf(1400));

    /** A standard meeting time. */
    private static final MeetingTime F15 = new MeetingTime(DayOfWeek.FRIDAY, Integer.valueOf(1500));

    /** A standard meeting time. */
    private static final MeetingTime F16 = new MeetingTime(DayOfWeek.FRIDAY, Integer.valueOf(1600));

    /** All possible meeting times. */
    private static final List<MeetingTime> ALL_MEETING_TIMES = new ArrayList<>(45);

    /** Map from course mix, like "678456" to a count of students in that mix. */
    private final Map<String, Integer> countsByCourseMix;

    /** The list of all students. */
    private final List<Student> students;

    /** The list of all registrations. */
    private final List<Registration> registrations;

    /** The list of meeting times in block 1 in which sections will be scheduled. */
    private final List<BlockMeetingTime> block1MeetingTimes;

    /** The list of meeting times in block 2 in which sections will be scheduled. */
    private final List<BlockMeetingTime> block2MeetingTimes;

    /** The list of meeting times in block 3 in which sections will be scheduled. */
    private final List<BlockMeetingTime> block3MeetingTimes;

    /** The next section number for each course ID. */
    private final Map<String, Integer> nextSectionNumber;

    /** A random number generator. */
    private final Random rnd;

    /*
     * Static initialization.
     */
    static {
        ALL_MEETING_TIMES.add(M08);
        ALL_MEETING_TIMES.add(M09);
        ALL_MEETING_TIMES.add(M10);
        ALL_MEETING_TIMES.add(M11);
        ALL_MEETING_TIMES.add(M12);
        ALL_MEETING_TIMES.add(M13);
        ALL_MEETING_TIMES.add(M14);
        ALL_MEETING_TIMES.add(M15);
        ALL_MEETING_TIMES.add(M16);
        ALL_MEETING_TIMES.add(T08);
        ALL_MEETING_TIMES.add(T09);
        ALL_MEETING_TIMES.add(T11);
        ALL_MEETING_TIMES.add(T12);
        ALL_MEETING_TIMES.add(T14);
        ALL_MEETING_TIMES.add(T15);
        ALL_MEETING_TIMES.add(W08);
        ALL_MEETING_TIMES.add(W09);
        ALL_MEETING_TIMES.add(W10);
        ALL_MEETING_TIMES.add(W11);
        ALL_MEETING_TIMES.add(W12);
        ALL_MEETING_TIMES.add(W13);
        ALL_MEETING_TIMES.add(W14);
        ALL_MEETING_TIMES.add(W15);
        ALL_MEETING_TIMES.add(W16);
        ALL_MEETING_TIMES.add(R08);
        ALL_MEETING_TIMES.add(R09);
        ALL_MEETING_TIMES.add(R11);
        ALL_MEETING_TIMES.add(R12);
        ALL_MEETING_TIMES.add(R14);
        ALL_MEETING_TIMES.add(R15);
        ALL_MEETING_TIMES.add(F08);
        ALL_MEETING_TIMES.add(F09);
        ALL_MEETING_TIMES.add(F10);
        ALL_MEETING_TIMES.add(F11);
        ALL_MEETING_TIMES.add(F12);
        ALL_MEETING_TIMES.add(F13);
        ALL_MEETING_TIMES.add(F14);
        ALL_MEETING_TIMES.add(F15);
        ALL_MEETING_TIMES.add(F16);
    }

    /**
     * Constructs a new {@code FaceToFaceSimulation}.
     */
    private FaceToFaceSimulation() {

        // Fall 2022 data, counting all MATH 127 students as " 78456", and guessing at numbers
        // that would choose to take MATH 116.

        this.countsByCourseMix = new HashMap<>(30);
        this.countsByCourseMix.put("     6", Integer.valueOf(127));
        this.countsByCourseMix.put("    5 ", Integer.valueOf(446));
        this.countsByCourseMix.put("    56", Integer.valueOf(162));
        this.countsByCourseMix.put("   4  ", Integer.valueOf(336));
        this.countsByCourseMix.put("   4 6", Integer.valueOf(35));
        this.countsByCourseMix.put("   45 ", Integer.valueOf(132));
        this.countsByCourseMix.put("   456", Integer.valueOf(141));
        this.countsByCourseMix.put("  8   ", Integer.valueOf(214));
        this.countsByCourseMix.put("  8  6", Integer.valueOf(1));
        this.countsByCourseMix.put("  8 5 ", Integer.valueOf(98));
        this.countsByCourseMix.put("  8 56", Integer.valueOf(3));
        this.countsByCourseMix.put("  84  ", Integer.valueOf(301));
        this.countsByCourseMix.put("  84 6", Integer.valueOf(2));
        this.countsByCourseMix.put("  845 ", Integer.valueOf(112));
        this.countsByCourseMix.put("  8456", Integer.valueOf(85));
        this.countsByCourseMix.put(" 7    ", Integer.valueOf(159));
        this.countsByCourseMix.put("67    ", Integer.valueOf(159));
        this.countsByCourseMix.put(" 7   6", Integer.valueOf(1));
        this.countsByCourseMix.put(" 7  5 ", Integer.valueOf(1));
        this.countsByCourseMix.put(" 7 4  ", Integer.valueOf(6));
        this.countsByCourseMix.put(" 78   ", Integer.valueOf(419));
        this.countsByCourseMix.put("678   ", Integer.valueOf(410));
        this.countsByCourseMix.put(" 78 5 ", Integer.valueOf(52));
        this.countsByCourseMix.put("678 5 ", Integer.valueOf(21));
        this.countsByCourseMix.put(" 784  ", Integer.valueOf(279));
        this.countsByCourseMix.put("6784  ", Integer.valueOf(103));
        this.countsByCourseMix.put(" 7845 ", Integer.valueOf(16));
        this.countsByCourseMix.put(" 78456", Integer.valueOf(44));
        this.countsByCourseMix.put("678456", Integer.valueOf(32));

        this.rnd = new Random(System.currentTimeMillis());

        this.students = new ArrayList<>(4000);
        this.registrations = new ArrayList<>(9000);

        this.block1MeetingTimes = new ArrayList<>(ALL_MEETING_TIMES.size());
        this.block2MeetingTimes = new ArrayList<>(ALL_MEETING_TIMES.size());
        this.block3MeetingTimes = new ArrayList<>(ALL_MEETING_TIMES.size());

        this.nextSectionNumber = new HashMap<>(6);
        this.nextSectionNumber.put(RawRecordConstants.M117, Integer.valueOf(3));
        this.nextSectionNumber.put(RawRecordConstants.M118, Integer.valueOf(3));
        this.nextSectionNumber.put(RawRecordConstants.M124, Integer.valueOf(3));
        this.nextSectionNumber.put(RawRecordConstants.M125, Integer.valueOf(3));
        this.nextSectionNumber.put(RawRecordConstants.M126, Integer.valueOf(3));
    }

    /**
     * Runs the simulation.
     */
    private void go() {

        final Collection<String> report = new ArrayList<>(100);

        // Build a list of student registrations from the course counts, and divide into blocks
        // within the semester.
        buildStudentsAndRegistrations(report);

        // Give each student some random "used" meeting times for courses they have added before
        // adding precalculus courses
        assignRandomUsedMeetingTimes(report);

        // Assign each registration to a meeting time, adding new meeting times as we find
        // situations where no meeting time fits a student's schedule
        assignBlockMeetingTimes(report);

        // Create sections
        createSections(report);

        // Assign rooms
        assignRooms(report);

        // Print the schedule
        printSchedule(report);

        for (final String s : report) {
            Log.fine(s);
        }
    }

    /**
     * Populates the lists of students and registrations. The students in the resulting lis will not have used meeting
     * times set, and the registrations in the resulting list will not have a meeting time or section assigned.
     *
     * @param report a report to which to write status information
     */
    private void buildStudentsAndRegistrations(final Collection<? super String> report) {

        int studentIndex = 0;

        final List<String> courseIds = new ArrayList<>(6);
        for (final Map.Entry<String, Integer> entry : this.countsByCourseMix.entrySet()) {
            final String mix = entry.getKey();
            final int count = Math.round((float) entry.getValue().intValue() * PORTION_OF_POPULATION_IN_F2F);

            courseIds.clear();
            if (mix.charAt(1) == '7') {
                courseIds.add(RawRecordConstants.M117);
            }
            if (mix.charAt(2) == '8') {
                courseIds.add(RawRecordConstants.M118);
            }
            if (mix.charAt(3) == '4') {
                courseIds.add(RawRecordConstants.M124);
            }
            if (mix.charAt(4) == '5') {
                courseIds.add(RawRecordConstants.M125);
            }
            if (mix.charAt(5) == '6') {
                courseIds.add(RawRecordConstants.M126);
            }

            final int numCourses = courseIds.size();
            for (int i = 0; i < count; ++i) {

                final Student stu = new Student(studentIndex);
                this.students.add(stu);

                final double random = this.rnd.nextDouble();

                if (numCourses == 1) {
                    // Assume 45% will choose block 1, 30% block 2, and 25% block 3
                    final int block = random < 0.45 ? 1 : random < 0.75 ? 2 : 3;
                    stu.registrations.add(new Registration(stu, block, courseIds.getFirst()));
                } else if (numCourses == 2) {
                    // Assume 40% will choose block 1-2, 10% block 1-3, 30% block 2-3,
                    // 10% doubling up in block 1, 5% each doubling up in blocks 2 and 3
                    final int block1;
                    final int block2;
                    if (random < 0.40) {
                        block1 = 1;
                        block2 = 2;
                    } else if (random < 0.50) {
                        block1 = 1;
                        block2 = 3;
                    } else if (random < 0.80) {
                        block1 = 2;
                        block2 = 3;
                    } else if (random < 0.90) {
                        block1 = 1;
                        block2 = 1;
                    } else if (random < 0.95) {
                        block1 = 2;
                        block2 = 2;
                    } else {
                        block1 = 3;
                        block2 = 3;
                    }
                    stu.registrations.add(new Registration(stu, block1, courseIds.get(0)));
                    stu.registrations.add(new Registration(stu, block2, courseIds.get(1)));
                } else if (numCourses == 3) {
                    // 75% choose 1-2-3,
                    // 6% choose 1-1-2,
                    // 5% choose 1-2-2,
                    // 5% choose 2-2-3,
                    // 5% choose 2-3-3,
                    // 2% choose 1-1-3,
                    // 2% choose 1-3-3
                    final int block1;
                    final int block2;
                    final int block3;
                    if (random < 0.75) {
                        block1 = 1;
                        block2 = 2;
                        block3 = 3;
                    } else if (random < 0.81) {
                        block1 = 1;
                        block2 = 1;
                        block3 = 2;
                    } else if (random < 0.86) {
                        block1 = 1;
                        block2 = 2;
                        block3 = 2;
                    } else {
                        if (random < 0.91) {
                            block1 = 2;
                            block2 = 2;
                        } else if (random < 0.96) {
                            block1 = 2;
                            block2 = 3;
                        } else if (random < 0.98) {
                            block1 = 1;
                            block2 = 1;
                        } else {
                            block1 = 1;
                            block2 = 3;
                        }
                        block3 = 3;
                    }

                    stu.registrations.add(new Registration(stu, block1, courseIds.get(0)));
                    stu.registrations.add(new Registration(stu, block2, courseIds.get(1)));
                    stu.registrations.add(new Registration(stu, block3, courseIds.get(2)));
                } else if (numCourses == 4) {
                    // 30% choose 1-1-2-3,
                    // 25% choose 1-2-2-3,
                    // 25% choose 1-2-3-3,
                    // 10% choose 1-1-2-2,
                    // 5% choose 2-2-3-3,
                    // 5% choose 1-1-3-3
                    final int block1;
                    final int block2;
                    final int block3;
                    final int block4;

                    if (random < 0.30) {
                        block1 = 1;
                        block2 = 1;
                        block3 = 2;
                        block4 = 3;
                    } else if (random < 0.55) {
                        block1 = 1;
                        block2 = 2;
                        block3 = 2;
                        block4 = 3;
                    } else if (random < 0.80) {
                        block1 = 1;
                        block2 = 2;
                        block3 = 3;
                        block4 = 3;
                    } else if (random < 0.90) {
                        block1 = 1;
                        block2 = 1;
                        block3 = 2;
                        block4 = 2;
                    } else {
                        if (random < 0.95) {
                            block1 = 2;
                            block2 = 2;
                        } else {
                            block1 = 1;
                            block2 = 1;
                        }
                        block3 = 3;
                        block4 = 3;
                    }

                    stu.registrations.add(new Registration(stu, block1, courseIds.get(0)));
                    stu.registrations.add(new Registration(stu, block2, courseIds.get(1)));
                    stu.registrations.add(new Registration(stu, block3, courseIds.get(2)));
                    stu.registrations.add(new Registration(stu, block4, courseIds.get(3)));
                } else if (numCourses == 5) {
                    // 36% choose 1-1-2-2-3,
                    // 32% choose 1-1-2-3-3,
                    // 32% choose 1-2-2-3-3
                    final int block1;
                    final int block2;
                    final int block3;
                    final int block4;
                    final int block5 = 3;

                    if (random < 0.36) {
                        block1 = 1;
                        block2 = 1;
                        block3 = 2;
                        block4 = 2;
                    } else {
                        if (random < 0.68) {
                            block1 = 1;
                            block2 = 1;
                        } else {
                            block1 = 1;
                            block2 = 2;
                        }
                        block3 = 2;
                        block4 = 3;
                    }
                    stu.registrations.add(new Registration(stu, block1, courseIds.get(0)));
                    stu.registrations.add(new Registration(stu, block2, courseIds.get(1)));
                    stu.registrations.add(new Registration(stu, block3, courseIds.get(2)));
                    stu.registrations.add(new Registration(stu, block4, courseIds.get(3)));
                    stu.registrations.add(new Registration(stu, block5, courseIds.get(4)));
                } else {
                    // 100% choose 1-1-2-2-3-3
                    stu.registrations.add(new Registration(stu, 1, courseIds.get(0)));
                    stu.registrations.add(new Registration(stu, 1, courseIds.get(1)));
                    stu.registrations.add(new Registration(stu, 2, courseIds.get(2)));
                    stu.registrations.add(new Registration(stu, 2, courseIds.get(3)));
                    stu.registrations.add(new Registration(stu, 3, courseIds.get(4)));
                    stu.registrations.add(new Registration(stu, 3, courseIds.get(5)));
                }

                this.registrations.addAll(stu.registrations);
                ++studentIndex;
            }
        }

        report.add("Constructed " + this.registrations.size() + " registrations for "
                + this.students.size() + " distinct students");
    }

    /**
     * Assigns each student a random number (1-3) of meeting times during the week that they have "used" and cannot
     * schedule any Precalculus courses.
     *
     * @param report a report to which to write status information
     */
    private void assignRandomUsedMeetingTimes(final Collection<? super String> report) {

        final List<MeetingTime> chooseFrom = new ArrayList<>(ALL_MEETING_TIMES);

        int total = 0;
        for (final Student stu : this.students) {

            final int numToAssign = this.rnd.nextInt(3) + 1;
            for (int i = 0; i < numToAssign; ++i) {
                final MeetingTime chosen = chooseFrom.get(this.rnd.nextInt(chooseFrom.size()));

                if (chosen == M08 || chosen == W08 || chosen == F08) {
                    stu.usedMeetingTimes1.add(M08);
                    stu.usedMeetingTimes1.add(W08);
                    stu.usedMeetingTimes1.add(F08);
                } else if (chosen == M09 || chosen == W09 || chosen == F09) {
                    stu.usedMeetingTimes1.add(M09);
                    stu.usedMeetingTimes1.add(W09);
                    stu.usedMeetingTimes1.add(F09);
                } else if (chosen == M10 || chosen == W10 || chosen == F10) {
                    stu.usedMeetingTimes1.add(M10);
                    stu.usedMeetingTimes1.add(W10);
                    stu.usedMeetingTimes1.add(F10);
                } else if (chosen == M11 || chosen == W11 || chosen == F11) {
                    stu.usedMeetingTimes1.add(M11);
                    stu.usedMeetingTimes1.add(W11);
                    stu.usedMeetingTimes1.add(F11);
                } else if (chosen == M12 || chosen == W12 || chosen == F12) {
                    stu.usedMeetingTimes1.add(M12);
                    stu.usedMeetingTimes1.add(W12);
                    stu.usedMeetingTimes1.add(F12);
                } else if (chosen == M13 || chosen == W13 || chosen == F13) {
                    stu.usedMeetingTimes1.add(M13);
                    stu.usedMeetingTimes1.add(W13);
                    stu.usedMeetingTimes1.add(F13);
                } else if (chosen == M14 || chosen == W14 || chosen == F14) {
                    stu.usedMeetingTimes1.add(M14);
                    stu.usedMeetingTimes1.add(W14);
                    stu.usedMeetingTimes1.add(F14);
                } else if (chosen == M15 || chosen == W15 || chosen == F15) {
                    stu.usedMeetingTimes1.add(M15);
                    stu.usedMeetingTimes1.add(W15);
                    stu.usedMeetingTimes1.add(F15);
                } else if (chosen == M16 || chosen == W16 || chosen == F16) {
                    stu.usedMeetingTimes1.add(M16);
                    stu.usedMeetingTimes1.add(W16);
                    stu.usedMeetingTimes1.add(F16);
                } else if (chosen == T08 || chosen == R08) {
                    stu.usedMeetingTimes1.add(T08);
                    stu.usedMeetingTimes1.add(R08);
                } else if (chosen == T09 || chosen == R09) {
                    stu.usedMeetingTimes1.add(T09);
                    stu.usedMeetingTimes1.add(R09);
                } else if (chosen == T11 || chosen == R11) {
                    stu.usedMeetingTimes1.add(T11);
                    stu.usedMeetingTimes1.add(R11);
                } else if (chosen == T12 || chosen == R12) {
                    stu.usedMeetingTimes1.add(T12);
                    stu.usedMeetingTimes1.add(R12);
                } else if (chosen == T14 || chosen == R14) {
                    stu.usedMeetingTimes1.add(T14);
                    stu.usedMeetingTimes1.add(R14);
                } else if (chosen == T15 || chosen == R15) {
                    stu.usedMeetingTimes1.add(T15);
                    stu.usedMeetingTimes1.add(R15);
                }
            }
            total += numToAssign;

            chooseFrom.clear();
            chooseFrom.addAll(ALL_MEETING_TIMES);
        }

        final float avg = (float) total / (float) this.students.size();

        report.add("Assigned " + total + " random 'used' meeting times over all students (" + avg
                + " per student, average)");
    }

    /**
     * Sweeps through the list of registrations, and selects a meeting time from the active meeting times that fits the
     * student's schedule (adding these to the student's 'used' meeting times as we go). When there is no active meeting
     * time that fits a student's schedule, a new one is created.
     *
     * <p>
     * This process increments the total registrations assigned to each meeting time, and when a meeting time exceeds
     * some upper bound on registrations, it is considered "full" and no longer available.
     *
     * @param report a report to which to write status information
     */
    private void assignBlockMeetingTimes(final Collection<? super String> report) {

        final List<BlockMeetingTime> free = new ArrayList<>(ALL_MEETING_TIMES.size());

        for (final Student stu : this.students) {
            for (final Registration reg : stu.registrations) {

                // Select the block list based on the registration's block
                final List<BlockMeetingTime> blockList = reg.block == 1 ? this.block1MeetingTimes
                        : reg.block == 2 ? this.block2MeetingTimes : this.block3MeetingTimes;
                final List<MeetingTime> usedTimes = reg.block == 1 ? stu.usedMeetingTimes1
                        : reg.block == 2 ? stu.usedMeetingTimes2 : stu.usedMeetingTimes3;

                // Scan for block meeting times that the student has available
                free.clear();
                for (final BlockMeetingTime test : blockList) {
                    if (test.registrations.size() < MAX_REGS_PER_MEETING_TIME
                            && !usedTimes.contains(test.meetingTime)) {
                        free.add(test);
                    }
                }

                final BlockMeetingTime selected;
                if (free.isEmpty()) {
                    // There is no block meeting time that will work - need to add a new one!
                    final List<MeetingTime> potential = new ArrayList<>(40);
                    potential.add(M10);
                    potential.add(M11);
                    potential.add(M12);
                    potential.add(M13);
                    potential.add(M14);
                    potential.add(M15);
                    potential.add(T11);
                    potential.add(T12);
                    potential.add(T14);
                    potential.add(W10);
                    potential.add(W11);
                    potential.add(W12);
                    potential.add(W13);
                    potential.add(W14);
                    potential.add(W15);
                    potential.add(R11);
                    potential.add(R12);
                    potential.add(R14);
                    potential.add(F10);
                    potential.add(F11);
                    potential.add(F12);
                    potential.add(F13);
                    potential.add(F14);
                    potential.add(F15);

                    for (final BlockMeetingTime test : blockList) {
                        potential.remove(test.meetingTime);
                    }

                    if (potential.isEmpty()) {
                        // We've used up all the "nice" times - add "less nice" times

                        potential.add(M08);
                        potential.add(M09);
                        potential.add(M16);
                        potential.add(T08);
                        potential.add(T09);
                        potential.add(T15);
                        potential.add(W08);
                        potential.add(W09);
                        potential.add(W16);
                        potential.add(R08);
                        potential.add(R09);
                        potential.add(R15);
                        potential.add(F08);
                        potential.add(F09);
                        potential.add(F16);

                        for (final BlockMeetingTime test : blockList) {
                            potential.remove(test.meetingTime);
                        }
                    }

                    if (potential.isEmpty()) {
                        report.add(CoreConstants.EMPTY);
                        report.add("*** FAILURE: all block " + reg.block + " meeting times filled to max capacity!");
                        break;
                    }

                    final MeetingTime time = potential.get(this.rnd.nextInt(potential.size()));
                    final BlockMeetingTime blockTime = new BlockMeetingTime(time);
                    blockList.add(blockTime);
                    selected = blockTime;
                } else if (free.size() == 1) {
                    // There is a single block that is free - use that one
                    selected = free.getFirst();
                } else {
                    // Multiple blocks are free - choose one at random
                    selected = free.get(this.rnd.nextInt(free.size()));
                }

                usedTimes.add(selected.meetingTime);

                reg.blockMeetingTime = selected;
                selected.registrations.add(reg);
            }
        }

        // Cancel any block meeting times with less than 10 students and try to re-assign those
        // students
        cancelSmallBlockMeetingTimes(this.block1MeetingTimes);
        cancelSmallBlockMeetingTimes(this.block2MeetingTimes);
        cancelSmallBlockMeetingTimes(this.block3MeetingTimes);

        Collections.sort(this.block1MeetingTimes);
        Collections.sort(this.block2MeetingTimes);
        Collections.sort(this.block3MeetingTimes);

        report.add(CoreConstants.EMPTY);
        report.add("Registrations have been assigned to meeting times within blocks:");
        report.add(CoreConstants.EMPTY);
        report.add("    BLOCK 1 (weeks 1-5):");
        for (final BlockMeetingTime blockTime : this.block1MeetingTimes) {
            report.add("        " + blockTime.meetingTime + " (" + blockTime.registrations.size() + " registrations)");
        }
        report.add("    BLOCK 2 (weeks 6-10):");
        for (final BlockMeetingTime blockTime : this.block2MeetingTimes) {
            report.add("        " + blockTime.meetingTime + " (" + blockTime.registrations.size() + " registrations)");
        }
        report.add("    BLOCK 3 (weeks 11-15):");
        for (final BlockMeetingTime blockTime : this.block3MeetingTimes) {
            report.add("        " + blockTime.meetingTime + " (" + blockTime.registrations.size() + " registrations)");
        }
    }

    /**
     * Scans for block meeting times with few (less than 10) registrations, and cancel them (removing them from the
     * supplied list).
     *
     * @param list the list to scan
     */
    private static void cancelSmallBlockMeetingTimes(final Iterable<BlockMeetingTime> list) {

        final Iterator<BlockMeetingTime> iter1 = list.iterator();
        while (iter1.hasNext()) {
            final BlockMeetingTime time = iter1.next();

            if (time.registrations.size() < 50) {
                iter1.remove();
                cancelBlockMeetingTime(time, list);
            }
        }
    }

    /**
     * Attempts to re-assign students in a block meeting time to others
     *
     * @param time the time to cancel
     * @param list the list of alternate times available (this list will not contain the time being canceled)
     */
    private static void cancelBlockMeetingTime(final BlockMeetingTime time, final Iterable<BlockMeetingTime> list) {

        for (final Registration reg : time.registrations) {

            final Student stu = reg.student;
            final List<MeetingTime> used;

            if (reg.block == 1) {
                used = stu.usedMeetingTimes1;
            } else if (reg.block == 2) {
                used = stu.usedMeetingTimes2;
            } else {
                used = stu.usedMeetingTimes3;
            }

            used.remove(reg.blockMeetingTime.meetingTime);
            BlockMeetingTime newTime = null;
            for (final BlockMeetingTime test : list) {
                if (!used.contains(test.meetingTime)) {
                    newTime = test;
                    break;
                }
            }

            if (newTime == null) {
                Log.warning("No meeting times for student " + stu.studentIndex + " for " + reg.course);
            } else {
                used.add(newTime.meetingTime);
                reg.blockMeetingTime = newTime;
            }
        }
    }

    /**
     * Scans the block meeting times for each block, sorts by course ID, and then creates some number of sections so
     * each section's enrollment falls below some room capacity.
     *
     * @param report a report to which to write status information
     */
    private void createSections(final Collection<? super String> report) {

        createBlockSections(this.block1MeetingTimes);
        createBlockSections(this.block2MeetingTimes);
        createBlockSections(this.block3MeetingTimes);

        report.add(CoreConstants.EMPTY);
        report.add("Sections created:");

        final Integer next117 = this.nextSectionNumber.get(RawRecordConstants.M117);
        final int num117 = next117.intValue() - 3;
        report.add("    MATH 117 has " + num117 + " sections");

        final Integer next118 = this.nextSectionNumber.get(RawRecordConstants.M118);
        final int num118 = next118.intValue() - 3;
        report.add("    MATH 118 has " + num118 + " sections");

        final Integer next124 = this.nextSectionNumber.get(RawRecordConstants.M124);
        final int num124 = next124.intValue() - 3;
        report.add("    MATH 124 has " + num124 + " sections");

        final Integer next125 = this.nextSectionNumber.get(RawRecordConstants.M125);
        final int num125 = next125.intValue() - 3;
        report.add("    MATH 125 has " + num125 + " sections");

        final Integer next126 = this.nextSectionNumber.get(RawRecordConstants.M126);
        final int num126 = next126.intValue() - 3;
        report.add("    MATH 126 has " + num126 + " sections");
    }

    /**
     * Scans the block meeting times for each block, sorts by course ID, and then creates some number of sections so
     * each section's enrollment falls below some room capacity.
     *
     * @param blockTimes the list of block meeting times
     */
    private void createBlockSections(final Iterable<BlockMeetingTime> blockTimes) {

        final List<Registration> m116 = new ArrayList<>(100);
        final List<Registration> m117 = new ArrayList<>(100);
        final List<Registration> m118 = new ArrayList<>(100);
        final List<Registration> m124 = new ArrayList<>(100);
        final List<Registration> m125 = new ArrayList<>(100);
        final List<Registration> m126 = new ArrayList<>(100);

        for (final BlockMeetingTime blockTime : blockTimes) {

            // Sort regs in the block time into lists by course...
            for (final Registration reg : blockTime.registrations) {
                if (RawRecordConstants.M117.equals(reg.course)) {
                    m117.add(reg);
                } else if (RawRecordConstants.M118.equals(reg.course)) {
                    m118.add(reg);
                } else if (RawRecordConstants.M124.equals(reg.course)) {
                    m124.add(reg);
                } else if (RawRecordConstants.M125.equals(reg.course)) {
                    m125.add(reg);
                } else if (RawRecordConstants.M126.equals(reg.course)) {
                    m126.add(reg);
                }
            }

            // Create sections and assign regs to them.
            buildSections(m116, blockTime);
            buildSections(m117, blockTime);
            buildSections(m118, blockTime);
            buildSections(m124, blockTime);
            buildSections(m125, blockTime);
            buildSections(m126, blockTime);

            m116.clear();
            m117.clear();
            m118.clear();
            m124.clear();
            m125.clear();
            m126.clear();
        }
    }

    /**
     * Builds sections needed to serve the registrations for a single course within a single block meeting time.
     *
     * @param blockCourseRegs the list of registrations in a single course in the block
     * @param blockTime       the block meeting time
     */
    private void buildSections(final List<Registration> blockCourseRegs,
                               final BlockMeetingTime blockTime) {

        if (!blockCourseRegs.isEmpty()) {
            final String course = blockCourseRegs.getFirst().course;

            int count = blockCourseRegs.size();

            final int numSections = (count + ROOM_CAP - 1) / ROOM_CAP;
            final int perSection = (count + numSections - 1) / numSections;

            for (int i = 0; i < numSections; ++i) {

                final Integer next = this.nextSectionNumber.get(course);
                final int sectNum = next.intValue();
                this.nextSectionNumber.put(course, Integer.valueOf(sectNum + 1));

                final int enrollment = Math.min(count, perSection);

                final Section sect = new Section(course, sectNum, blockTime, ROOM_CAP, enrollment);

                for (int j = 0; j < enrollment; ++j) {
                    final Registration reg = blockCourseRegs.removeFirst();
                    reg.section = sect;
                }

                count -= enrollment;
                if (enrollment > 5) {
                    blockTime.sections.add(sect);
                }
            }
        }
    }

    /**
     * Assigns room numbers and tracks the largest room number so we can see the number of rooms needed to accommodate
     * all sections.
     *
     * @param report a report to which to write status information
     */
    private void assignRooms(final Collection<? super String> report) {

        final int numRoomsBlock1 = assignBlockRooms(this.block1MeetingTimes);
        final int numRoomsBlock2 = assignBlockRooms(this.block2MeetingTimes);
        final int numRoomsBlock3 = assignBlockRooms(this.block3MeetingTimes);

        report.add(CoreConstants.EMPTY);
        report.add("Rooms assigned:");
        report.add("    Block 1 (Weeks  1- 5) requires " + numRoomsBlock1 + " rooms");
        report.add("    Block 2 (Weeks  6-10) requires " + numRoomsBlock2 + " rooms");
        report.add("    Block 3 (Weeks 11-15) requires " + numRoomsBlock3 + " rooms");
    }

    /**
     * Assigns rooms within a block.
     *
     * @param blockTimes the block meeting times
     * @return the number of rooms needed over all blocks
     */
    private static int assignBlockRooms(final Iterable<BlockMeetingTime> blockTimes) {

        int maxRooms = 0;

        for (final BlockMeetingTime blockTime : blockTimes) {

            int numRooms = 0;
            for (final Section sect : blockTime.sections) {
                ++numRooms;
                sect.roomNumber = numRooms;
            }
            maxRooms = Math.max(maxRooms, numRooms);
        }

        return maxRooms;
    }

    /**
     * Prints the schedule.
     *
     * @param report a report to which to write status information
     */
    private void printSchedule(final Collection<? super String> report) {

        report.add(CoreConstants.EMPTY);
        report.add(CoreConstants.EMPTY);
        report.add("------------------------------------------------------------------------");
        report.add("Completed Schedule:");
        report.add("------------------------------------------------------------------------");

        report.add(CoreConstants.EMPTY);
        report.add("BLOCK 1 (Weeks 1 - 5):");
        int totalInstructorHours = printBlockSchedule(this.block1MeetingTimes, report);

        report.add(CoreConstants.EMPTY);
        report.add("BLOCK 2 (Weeks 6 - 10):");
        totalInstructorHours += printBlockSchedule(this.block2MeetingTimes, report);

        report.add(CoreConstants.EMPTY);
        report.add("BLOCK 3 (Weeks 11 - 15):");
        totalInstructorHours += printBlockSchedule(this.block3MeetingTimes, report);

        report.add(CoreConstants.EMPTY);
        report.add("Total meeting times per week: " + totalInstructorHours);

        // A single FTE instructor resource can teach 12 "hours" per term
        final int fte = (totalInstructorHours + 11) / 12;
        report.add("Total FTE instructors needed: " + fte);
    }

    /**
     * Prints the schedule for a single block.
     *
     * @param blockMeetingTimes the list of meeting times in the block
     * @param report            a report to which to write status information
     * @return the total number of instructor hours needed
     */
    private static int printBlockSchedule(final Iterable<BlockMeetingTime> blockMeetingTimes,
                                          final Collection<? super String> report) {

        int totalInstructorHours = 0;
        final HtmlBuilder htm = new HtmlBuilder(30);

        for (final BlockMeetingTime blockTime : blockMeetingTimes) {
            report.add("    " + blockTime.meetingTime + ":");
            for (final Section sect : blockTime.sections) {
                htm.add("    Section ");
                if (sect.sectionNumber < 10) {
                    htm.add("00");
                } else if (sect.sectionNumber < 100) {
                    htm.add("0");
                }
                htm.add(Integer.toString(sect.sectionNumber));
                htm.add(" of ");
                htm.add(sect.course);
                htm.add(" in room ");
                htm.add(Integer.toString(sect.roomNumber));
                htm.add(" with enrollment ");
                htm.add(Integer.toString(sect.enrollment));

                report.add(htm.toString());
                htm.reset();

                ++totalInstructorHours;
            }
        }

        return totalInstructorHours;
    }

    /**
     * Possible class meeting times. Note: this class has a natural ordering that is inconsistent with equals.
     */
    private static final class MeetingTime implements Comparable<MeetingTime> {

        /** The weekday. */
        private final DayOfWeek weekday;

        /** The hour of the day. */
        private final Integer hour;

        /**
         * Constructs a new {@code MeetingTime}.
         *
         * @param theWeekday the weekday
         * @param theHour    the hour
         */
        private MeetingTime(final DayOfWeek theWeekday, final Integer theHour) {

            this.weekday = theWeekday;
            this.hour = theHour;
        }

        /**
         * Generates the string representation of the meeting time.
         */
        @Override
        public String toString() {

            final HtmlBuilder builder = new HtmlBuilder(20);

            if (this.weekday == DayOfWeek.MONDAY) {
                builder.add("Mon. ");
            } else if (this.weekday == DayOfWeek.TUESDAY) {
                builder.add("Tue. ");
            } else if (this.weekday == DayOfWeek.WEDNESDAY) {
                builder.add("Wed. ");
            } else if (this.weekday == DayOfWeek.THURSDAY) {
                builder.add("Thu. ");
            } else {
                builder.add("Fri. ");
            }

            builder.add(this.hour.toString());

            return builder.toString();
        }

        /**
         * Compares this object to another {@code MeetingTime} for order.
         *
         * @param obj the meeting time against which to compare
         * @return -1, 0, or 1 as this object is less than, equal to, or greater than {@code o}, respectively
         */
        @Override
        public int compareTo(final MeetingTime obj) {

            int result = this.weekday.compareTo(obj.weekday);

            if (result == 0) {
                result = this.hour.compareTo(obj.hour);
            }

            return result;
        }
    }

    /**
     * A student.
     */
    private static final class Student {

        /** The student index. */
        private final int studentIndex;

        /** The used meeting times. */
        private final List<MeetingTime> usedMeetingTimes1;

        /** The used meeting times. */
        private final List<MeetingTime> usedMeetingTimes2;

        /** The used meeting times. */
        private final List<MeetingTime> usedMeetingTimes3;

        /** The student's precalculus registrations. */
        private final List<Registration> registrations;

        /**
         * Constructs a new {@code Student}.
         *
         * @param theStudentIndex the student index
         */
        private Student(final int theStudentIndex) {

            this.studentIndex = theStudentIndex;

            this.usedMeetingTimes1 = new ArrayList<>(8);
            this.usedMeetingTimes2 = new ArrayList<>(8);
            this.usedMeetingTimes3 = new ArrayList<>(8);
            this.registrations = new ArrayList<>(6);
        }
    }

    /**
     * A registration.
     */
    static final class Registration {

        /** The student index. */
        final Student student;

        /** The block in the semester (1, 2, or 3). */
        final int block;

        /** The course. */
        final String course;

        /** The block meeting time (filled in when assigned). */
        BlockMeetingTime blockMeetingTime;

        /** The section (filled in when assigned). */
        Section section;

        /**
         * Constructs a new {@code Registration}.
         *
         * @param theStudent the student
         * @param theBlock   the block in the semester (1, 2, or 3)
         * @param theCourse  the course
         */
        Registration(final Student theStudent, final int theBlock, final String theCourse) {

            this.student = theStudent;
            this.block = theBlock;
            this.course = theCourse;
        }
    }

    /**
     * A meeting time within a block.
     */
    private static final class BlockMeetingTime implements Comparable<BlockMeetingTime> {

        /** The meeting time. */
        private final MeetingTime meetingTime;

        /** The registrations assigned to the meeting time. */
        private final List<Registration> registrations;

        /** The sections created to support the registrations. */
        private final List<Section> sections;

        /**
         * Constructs a new {@code BlockMeetingTime}
         *
         * @param theMeetingTime the meeting time
         */
        private BlockMeetingTime(final MeetingTime theMeetingTime) {

            this.meetingTime = theMeetingTime;
            this.registrations = new ArrayList<>(MAX_REGS_PER_MEETING_TIME);
            this.sections = new ArrayList<>(10);
        }

        /**
         * Compares this object to another {@code BlockMeetingTime} for order.
         *
         * @param obj the meeting time against which to compare
         * @return -1, 0, or 1 as this object is less than, equal to, or greater than {@code o}, respectively
         */
        @Override
        public int compareTo(final BlockMeetingTime obj) {

            return this.meetingTime.compareTo(obj.meetingTime);
        }
    }

    /**
     * A section.
     */
    private static final class Section {

        /** The course ID. */
        private final String course;

        /** The section number. */
        private final int sectionNumber;

        /** The meeting time. */
        final BlockMeetingTime blockMeetingTime;

        /** The section capacity. */
        final int capacity;

        /** The section enrollment. */
        private final int enrollment;

        /** The room number (from 1 to N), when assigned. */
        private int roomNumber;

        /**
         * Constructs a new {@code Section}.
         *
         * @param theCourse           the course ID
         * @param theSectionNumber   the section number
         * @param theBlockMeetingTime the meeting time
         * @param theCapacity         the capacity
         * @param theEnrollment       the enrollment
         */
        private Section(final String theCourse, final int theSectionNumber,
                        final BlockMeetingTime theBlockMeetingTime, final int theCapacity,
                        final int theEnrollment) {

            this.course = theCourse;
            this.sectionNumber = theSectionNumber;
            this.blockMeetingTime = theBlockMeetingTime;
            this.capacity = theCapacity;
            this.enrollment = theEnrollment;
        }
    }

    /**
     * Main method to launch the simulation.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        DbConnection.registerDrivers();
        new FaceToFaceSimulation().go();
    }
}
