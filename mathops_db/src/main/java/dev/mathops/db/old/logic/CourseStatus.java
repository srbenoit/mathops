package dev.mathops.db.old.logic;

import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawStcourse;

import java.time.LocalDate;

/**
 * A data class containing student status relative to an in-progress course.
 */
public final class CourseStatus {

    /** The student course record. */
    public final RawStcourse stcourse;

    /** The course section. */
    public final RawCsection csection;

    /** The legacy course status. */
    public final LegacyCourseStatus legacyStatus;

    /**
     * Constructs a new {@code CourseStatus}.
     *
     * @param theStcourse     the student course record
     * @param theCsection     the course section record
     * @param theLegacyStatus the status if this is a legacy course registration
     */
    CourseStatus(final RawStcourse theStcourse, final RawCsection theCsection,
                 final LegacyCourseStatus theLegacyStatus) {

        this.stcourse = theStcourse;
        this.csection = theCsection;
        this.legacyStatus = theLegacyStatus;
    }

    /**
     * A container for status associated with a "Legacy" course.
     */
    public static class LegacyCourseStatus {

        /** The due date for the Review exams. */
        public final LocalDate[] reDueDates;

        /** The due date for the Final exam. */
        public final LocalDate feDueDate;

        /** The date the Review exams were passed. */
        public final boolean[] reOnTimes;

        /** The best passing scores on Unit exams. */
        public final int[] bestPassingUE;

        /** The best passing score on FE. */
        public final int bestPassingFE;

        /** The best failing scores on Unit exams. */
        public final int[] bestFailedUE;

        /** The best failing score on FE. */
        public final int bestFailedFE;

        /** The total score. */
        public final int totalScore;

        /** The number of attempts on Unit exams. */
        public final int[] numUE;

        /** The number of attempts on FE. */
        public final int numFE;

        /**
         * Constructs a new {@code LegacyCourseStatus}.
         *
         * @param theRe1DueDate    the due date for the RE1 exam
         * @param theRe2DueDate    the due date for the RE2 exam
         * @param theRe3DueDate    the due date for the RE3 exam
         * @param theRe4DueDate    the due date for the RE4 exam
         * @param theFeDueDate     the due date for the FE exam
         * @param isRe1OnTime      true of RE1 was passed on time; false if late
         * @param isRe2OnTime      true of RE2 was passed on time; false if late
         * @param isRe3OnTime      true of RE3 was passed on time; false if late
         * @param isRe4OnTime      true of RE4 was passed on time; false if late
         * @param theBestPassingU1 the best passing score on U1 exam
         * @param theBestPassingU2 the best passing score on U2 exam
         * @param theBestPassingU3 the best passing score on U3 exam
         * @param theBestPassingU4 the best passing score on U4 exam
         * @param theBestPassingFE the best passing score on FE exam
         * @param theBestFailedU1  the best failing score on U1 exam
         * @param theBestFailedU2  the best failing score on U2 exam
         * @param theBestFailedU3  the best failing score on U3 exam
         * @param theBestFailedU4  the best failing score on U4 exam
         * @param theBestFailedFE  the best failing score on FE exam
         * @param theTotalScore    the current total score
         * @param theNumU1         the number of attempts on U1 exam
         * @param theNumU2         the number of attempts on U2 exam
         * @param theNumU3         the number of attempts on U3 exam
         * @param theNumU4         the number of attempts on U4 exam
         * @param theNumFE         the number of attempts on FE exam
         */
        LegacyCourseStatus(final LocalDate theRe1DueDate, final LocalDate theRe2DueDate, final LocalDate theRe3DueDate,
                           final LocalDate theRe4DueDate, final LocalDate theFeDueDate, final boolean isRe1OnTime,
                           final boolean isRe2OnTime, final boolean isRe3OnTime, final boolean isRe4OnTime,
                           final int theBestPassingU1, final int theBestPassingU2, final int theBestPassingU3,
                           final int theBestPassingU4, final int theBestPassingFE, final int theBestFailedU1,
                           final int theBestFailedU2, final int theBestFailedU3, final int theBestFailedU4,
                           final int theBestFailedFE, final int theTotalScore, final int theNumU1, final int theNumU2,
                           final int theNumU3, final int theNumU4, final int theNumFE) {

            this.reDueDates = new LocalDate[] {theRe1DueDate, theRe2DueDate, theRe3DueDate, theRe4DueDate};
            this.feDueDate = theFeDueDate;
            this.reOnTimes = new boolean[] {isRe1OnTime, isRe2OnTime, isRe3OnTime, isRe4OnTime};
            this.bestPassingUE = new int[] {theBestPassingU1, theBestPassingU2, theBestPassingU3, theBestPassingU4};
            this.bestPassingFE = theBestPassingFE;
            this.bestFailedUE = new int[] {theBestFailedU1, theBestFailedU2, theBestFailedU3, theBestFailedU4};
            this.bestFailedFE = theBestFailedFE;
            this.totalScore = theTotalScore;
            this.numUE = new int[] {theNumU1, theNumU2, theNumU3, theNumU4};
            this.numFE = theNumFE;
        }
    }
}
