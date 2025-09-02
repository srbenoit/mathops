package dev.mathops.web.host.placement.tutorial.precalc;

import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.Cache;
import dev.mathops.db.schema.legacy.impl.RawStexamLogic;
import dev.mathops.db.schema.legacy.rec.RawCourse;
import dev.mathops.db.schema.legacy.rec.RawCunit;
import dev.mathops.db.schema.legacy.rec.RawCuobjective;
import dev.mathops.db.schema.legacy.rec.RawLesson;
import dev.mathops.db.schema.legacy.rec.RawStexam;
import dev.mathops.db.schema.legacy.rec.RawStudent;
import dev.mathops.db.schema.main.rec.TermRec;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A container for the status of a student in a single Precalculus Tutorial course.
 */
final class PrecalcTutorialCourseStatus {

    /** A unit number. */
    private static final Integer UNIT0 = Integer.valueOf(0);

    /** A unit number. */
    private static final Integer UNIT1 = Integer.valueOf(1);

    /** A unit number. */
    private static final Integer UNIT2 = Integer.valueOf(2);

    /** A unit number. */
    private static final Integer UNIT3 = Integer.valueOf(3);

    /** A unit number. */
    private static final Integer UNIT4 = Integer.valueOf(4);

    /** The student record. */
    private final RawStudent student;

    /** The course. */
    private RawCourse course;

    /** The course units. */
    private final Map<Integer, RawCunit> courseUnits;

    /** The unit objectives. */
    private final Map<Integer, Map<Integer, RawCuobjective>> unitObjectives;

    /** The unit objectives. */
    private final Map<Integer, Map<Integer, RawLesson>> unitLessons;

    /** The date the student last attempted the Skills Review exam, {@code null} if not yet attempted. */
    private LocalDateTime whenAttemptedSR;

    /** The date the student last passed the Skills Review exam, {@code null} if not yet passed. */
    private LocalDateTime whenPassedSR;

    /**
     * A map from unit number to the date the student last attempted a Unit review exam, {@code null} if not yet
     * attempted.
     */
    private final Map<Integer, LocalDateTime> whenAttemptedRE;

    /**
     * A map from unit number to the date the student last passed a Unit review exam, {@code null} if not yet passed.
     */
    private final Map<Integer, LocalDateTime> whenPassedRE;

    /** Flag indicating student is eligible for the unit 4 proctored exam. */
    private boolean eligibleForProctored;

    /** Flag indicating student must retake unit 4 review exam to become eligible for more tries on proctored exam. */
    private boolean mustRetakeRE4;

    /**
     * Constructs a new {@code PrecalcTutorialCourseStatus}.
     *
     * @param theCache    the cache
     * @param theCourseId the course ID
     */
    PrecalcTutorialCourseStatus(final Cache theCache, final RawStudent theStudent, final String theCourseId) {

        this.student = theStudent;

        final SystemData systemData = theCache.getSystemData();

        try {
            this.course = systemData.getCourse(theCourseId);
        } catch (final SQLException ex) {
            Log.warning("Failed to query course ", theCourseId, ex);
        }

        TermRec activeTerm = null;
        try {
            activeTerm = systemData.getActiveTerm();
        } catch (final SQLException ex) {
            Log.warning("Failed to query active term", ex);
        }

        this.courseUnits = new HashMap<>(5);
        try {
            if (activeTerm != null) {
                final List<RawCunit> cunits = systemData.getCourseUnits(theCourseId, activeTerm.term);

                for (final RawCunit cunit : cunits) {
                    this.courseUnits.put(cunit.unit, cunit);
                }
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to query course ", theCourseId, " units", ex);
        }

        this.unitObjectives = new HashMap<>(5);
        try {
            if (activeTerm != null) {
                for (final Integer unit : new Integer[]{UNIT0, UNIT1, UNIT2, UNIT3, UNIT4}) {
                    final List<RawCuobjective> objectives = systemData.getCourseUnitObjectives(theCourseId, unit,
                            activeTerm.term);
                    final Map<Integer, RawCuobjective> objMap = new TreeMap<>();
                    this.unitObjectives.put(unit, objMap);
                    for (final RawCuobjective obj : objectives) {
                        objMap.put(obj.objective, obj);
                    }
                }
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to query ", theCourseId, " unit objectives", ex);
        }

        this.unitLessons = new HashMap<>(5);
        for (final Map.Entry<Integer, Map<Integer, RawCuobjective>> entry : this.unitObjectives.entrySet()) {
            final Integer unit = entry.getKey();
            final Map<Integer, RawCuobjective> inner = entry.getValue();

            final Map<Integer, RawLesson> lessonMap = new HashMap<>(inner.size());
            this.unitLessons.put(unit, lessonMap);

            for (final RawCuobjective obj : inner.values()) {
                if (obj.lessonId != null) {
                    final RawLesson lesson = systemData.getLesson(obj.lessonId);
                    if (lesson != null) {
                        lessonMap.put(obj.objective, lesson);
                    }
                }
            }
        }

        this.whenAttemptedRE = new HashMap<>(4);
        this.whenPassedRE = new HashMap<>(4);

        try {
            final List<RawStexam> allExams = RawStexamLogic.queryByStudentCourse(theCache, this.student.stuId,
                    theCourseId, false);

            // Find latest date each review exam was passed, and test whether the proctored exam has been passed
            LocalDateTime whenAttemptedR1 = null;
            LocalDateTime whenAttemptedR2 = null;
            LocalDateTime whenAttemptedR3 = null;
            LocalDateTime whenAttemptedR4 = null;
            LocalDateTime whenPassedR1 = null;
            LocalDateTime whenPassedR2 = null;
            LocalDateTime whenPassedR3 = null;
            LocalDateTime whenPassedR4 = null;

            for (final RawStexam exam : allExams) {
                if ("R".equals(exam.examType) || "Q".equals(exam.examType)) {
                    final int unit = exam.unit.intValue();
                    final int finishTime = exam.finishTime.intValue();

                    final LocalTime endTime;
                    final LocalDateTime whenFinished;

                    if (finishTime < 1440) {
                        endTime = LocalTime.of(finishTime / 60, finishTime % 60);
                        whenFinished = LocalDateTime.of(exam.examDt, endTime);
                    } else {
                        endTime = LocalTime.of((finishTime - 1440) / 60, (finishTime - 1440)% 60);
                        whenFinished = LocalDateTime.of(exam.examDt.plusDays(1), endTime);
                    }

                    final boolean passed = "Y".equals(exam.passed);

                    if (unit == 0) {
                        this.whenAttemptedSR = getLatest(this.whenAttemptedSR, whenFinished);
                        if (passed) {
                            this.whenPassedSR = getLatest(this.whenPassedSR, whenFinished);
                        }
                    } else if (unit == 1) {
                        whenAttemptedR1 = getLatest(whenAttemptedR1, whenFinished);
                        if (passed) {
                            whenPassedR1 = getLatest(whenPassedR1, whenFinished);
                        }
                    } else if (unit == 2) {
                        whenAttemptedR2 = getLatest(whenAttemptedR2, whenFinished);
                        if (passed) {
                            whenPassedR2 = getLatest(whenPassedR2, whenFinished);
                        }
                    } else if (unit == 3) {
                        whenAttemptedR3 = getLatest(whenAttemptedR3, whenFinished);
                        if (passed) {
                            whenPassedR3 = getLatest(whenPassedR3, whenFinished);
                        }
                    } else if (unit == 4) {
                        whenAttemptedR4 = getLatest(whenAttemptedR4, whenFinished);
                        if (passed) {
                            whenPassedR4 = getLatest(whenPassedR4, whenFinished);
                        }
                    }
                }
            }

            if (whenAttemptedR1 != null) {
                this.whenAttemptedRE.put(UNIT1, whenAttemptedR1);
            }
            if (whenAttemptedR2 != null) {
                this.whenAttemptedRE.put(UNIT2, whenAttemptedR2);
            }
            if (whenAttemptedR3 != null) {
                this.whenAttemptedRE.put(UNIT3, whenAttemptedR3);
            }
            if (whenAttemptedR4 != null) {
                this.whenAttemptedRE.put(UNIT4, whenAttemptedR4);
            }
            if (whenPassedR1 != null) {
                this.whenPassedRE.put(UNIT1, whenPassedR1);
            }
            if (whenPassedR2 != null) {
                this.whenPassedRE.put(UNIT2, whenPassedR2);
            }
            if (whenPassedR3 != null) {
                this.whenPassedRE.put(UNIT3, whenPassedR3);
            }
            if (whenPassedR4 != null) {
                this.whenPassedRE.put(UNIT4, whenPassedR4);
            }

            if (whenPassedR4 != null) {
                // Student may be eligible for proctored exam - unless two failed tries since last passing RE
                int failCount = 0;
                for (final RawStexam exam : allExams) {
                    if ("U".equals(exam.examType)) {

                        if ("N".equals(exam.passed)) {
                            final int unit = exam.unit.intValue();
                            final int finishTime = exam.finishTime.intValue();
                            final LocalTime endTime = LocalTime.of(finishTime / 60, finishTime % 60);
                            final LocalDateTime whenFinished = LocalDateTime.of(exam.examDt, endTime);
                            if (unit == 4 && whenFinished.isAfter(whenPassedR4)) {
                                ++failCount;
                            }
                        }
                    }
                }

                if (failCount < 2) {
                    this.eligibleForProctored = true;
                } else {
                    this.mustRetakeRE4 = true;
                }
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to query student exams", ex);
        }
    }

    /**
     * Given a current latest date/time (which may be null), and a new date/time, returns the latter of the two, or the
     * new date/time if the current date/time was null;
     *
     * @param currentLatest the current latest date/time (could be {@code null})
     * @param newDateTime   the new date/time (must not be {@code null})
     * @return the latter of the two (never {@code null})
     */
    private static LocalDateTime getLatest(final LocalDateTime currentLatest, final LocalDateTime newDateTime) {

        final LocalDateTime result;

        if (currentLatest == null || currentLatest.isBefore(newDateTime)) {
            result = newDateTime;
        } else {
            result = currentLatest;
        }

        return result;
    }

    /**
     * Gets the course record.
     *
     * @return the course record
     */
    RawCourse getCourse() {

        return this.course;
    }

    /**
     * Gets a course unit record.
     *
     * @param unit the unit
     * @return the course unit record
     */
    RawCunit getCourseUnit(final Integer unit) {

        return this.courseUnits.get(unit);
    }

    /**
     * Gets a map from objective number to the objective object for a unit.
     *
     * @param unit the unit
     * @return the map
     */
    Map<Integer, RawCuobjective> getUnitObjectives(final Integer unit) {

        return this.unitObjectives.get(unit);
    }

    /**
     * Gets a map from objective number to the lesson object for a unit.
     *
     * @param unit the unit
     * @return the map
     */
    Map<Integer, RawLesson> getUnitLessons(final Integer unit) {

        return this.unitLessons.get(unit);
    }

    /**
     * Gets the date/time when the student most recently attempted the Skills Review exam.
     *
     * @return the date/time; {@code null} if that exam has not been attempted
     */
    LocalDateTime getWhenAttemptedSR() {

        return this.whenAttemptedSR;
    }

    /**
     * Gets the date/time when the student most recently passed the Skills Review exam.
     *
     * @return the date/time; {@code null} if that exam has not been passed
     */
    LocalDateTime getWhenPassedSR() {

        return this.whenPassedSR;
    }

    /**
     * Gets the date/time when the student most recently attempted a Unit Review exam.
     *
     * @param unit the unit
     * @return the date/time; {@code null} if that exam has not been attempted
     */
    LocalDateTime getWhenAttemptedRE(final Integer unit) {

        return this.whenAttemptedRE.get(unit);
    }

    /**
     * Gets the date/time when the student most recently passed a Unit Review exam.
     *
     * @param unit the unit
     * @return the date/time; {@code null} if that exam has not been passed
     */
    LocalDateTime getWhenPassedRE(final Integer unit) {

        return this.whenPassedRE.get(unit);
    }

    /**
     * Tests whether the student is eligible for the unit 4 proctored exam.
     *
     * @return {@code true} if eligible; {@code false} if not
     */
    boolean isEligibleForProctored() {

        return this.eligibleForProctored;
    }

    /**
     * Tests whether the student needs to re-take the Unit 4 Review Exam to try to earn more attempts on the proctored
     * exam.
     *
     * @return {@code true} if unit 4 retake is needed; {@code false} if not
     */
    boolean isRE4RetakeNeeded() {

        return this.mustRetakeRE4;
    }
}
