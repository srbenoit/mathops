package dev.mathops.dbjobs.report.usage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A histogram for a single day, with slots for each hour.
 */
final class DayHistogram {

    /** A commonly-used constant. */
    private static final int HOURS_PER_DAY = 24;

    /** A commonly-used constant. */
    private static final int MINUTES_PER_HOUR = 60;

    /** Total number of exams that finish on the day. */
    private int totalExams = 0;

    /** Total number of homeworks that finish on the day. */
    private int totalHomeworks = 0;

    /** Number of exams in progress during the hour. */
    private final int[] numExams;

    /** Number of homeworks in progress during the hour. */
    private final int[] numHomeworks;

    /** Number of minutes of exams in progress during the hour. */
    private final int[] examMinutes;

    /** Number of minutes of homeworks in progress during the hour. */
    private final int[] homeworkMinutes;

    /** The set of student IDs who were active during each hour. */
    private final List<Set<String>> studentIds;

    /**
     * Constructs a new {@code DayHistogram}.
     */
    DayHistogram() {

        this.numExams = new int[HOURS_PER_DAY];
        this.numHomeworks = new int[HOURS_PER_DAY];
        this.examMinutes = new int[HOURS_PER_DAY];
        this.homeworkMinutes = new int[HOURS_PER_DAY];

        this.studentIds = new ArrayList<>(HOURS_PER_DAY);
        for (int i = 0; i < HOURS_PER_DAY; ++i) {
            this.studentIds.add(new HashSet<>(100));
        }
    }

    /**
     * Gets the total number of exams that ended this day.
     *
     * @return the total number of exams
     */
    int getTotalExams() {

        return this.totalExams;
    }

    /**
     * Gets the total number of homework assignments that ended this day.
     *
     * @return the total number of homeworks
     */
    int getTotalHomeworks() {

        return this.totalHomeworks;
    }

    /**
     * Gets the number of exams whose time overlapped the hour.
     *
     * @param hour the hour
     * @return the number of exams
     */
    int getNumExams(final int hour) {

        return this.numExams[hour];
    }

    /**
     * Gets the number of exams whose time overlapped the hour.
     *
     * @param hour the hour
     * @return the number of exams
     */
    int getNumHomeworks(final int hour) {

        return this.numHomeworks[hour];
    }

    /**
     * Gets the number of minutes of exam during the hour.
     *
     * @param hour the hour
     * @return the number of minutes
     */
    int getExamMinutes(final int hour) {

        return this.examMinutes[hour];
    }

    /**
     * Gets the number of minutes of homework during the hour.
     *
     * @param hour the hour
     * @return the number of minutes
     */
    int getHomeworkMinutes(final int hour) {

        return this.homeworkMinutes[hour];
    }

    /**
     * Gets the set of active students in an hour.
     *
     * @param hour the hour
     * @return the set of active students
     */
    Set<String> getActiveStudents(final int hour) {

        return this.studentIds.get(hour);
    }

    /**
     * Records an exam.
     *
     * @param startTime     the start time (minutes after midnight)
     * @param endTime       the end time (minutes after midnight)
     * @param finishedToday if the exam being recorded finished during this day
     */
    void recordExam(final int startTime, final int endTime, final boolean finishedToday) {

        for (int i = 0; i < HOURS_PER_DAY; ++i) {
            final int hourBegin = i * MINUTES_PER_HOUR;
            final int hourEnd = (i + 1) * MINUTES_PER_HOUR;

            if (startTime < hourEnd && endTime >= hourBegin) {
                ++this.numExams[i];
                if (startTime <= hourBegin) {
                    if (endTime > hourEnd) {
                        this.examMinutes[i] += MINUTES_PER_HOUR;
                    } else {
                        this.examMinutes[i] += endTime - hourBegin;
                    }
                } else if (endTime > hourEnd) {
                    this.examMinutes[i] += hourEnd - startTime;
                } else {
                    this.examMinutes[i] += endTime - startTime;
                }
            }
        }

        if (finishedToday) {
            ++this.totalExams;
        }
    }

    /**
     * Records that a student was active during a minute.
     *
     * @param studentId the student ID
     * @param hour      the hour
     */
    void recordActivity(final String studentId, final int hour) {

        this.studentIds.get(hour).add(studentId);
    }

    /**
     * Records a homework.
     *
     * @param startTime     the start time (minutes after midnight)
     * @param endTime       the end time (minutes after midnight)
     * @param finishedToday if the exam being recorded finished during this day
     */
    void recordHomework(final int startTime, final int endTime, final boolean finishedToday) {

        for (int i = 0; i < HOURS_PER_DAY; ++i) {
            final int hourBegin = i * MINUTES_PER_HOUR;
            final int hourEnd = (i + 1) * MINUTES_PER_HOUR;

            if (startTime < hourEnd && endTime >= hourBegin) {
                ++this.numHomeworks[i];
                if (startTime <= hourBegin) {
                    if (endTime > hourEnd) {
                        this.homeworkMinutes[i] += MINUTES_PER_HOUR;
                    } else {
                        this.homeworkMinutes[i] += endTime - hourBegin;
                    }
                } else if (endTime > hourEnd) {
                    this.homeworkMinutes[i] += hourEnd - startTime;
                } else {
                    this.homeworkMinutes[i] += endTime - startTime;
                }
            }
        }
        if (finishedToday) {
            ++this.totalHomeworks;
        }
    }
}
