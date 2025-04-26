package dev.mathops.db.rec.term;

import dev.mathops.db.DataDict;
import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.Objects;

/**
 * An immutable raw "standards course grading system" record.
 *
 * <p>
 * Each record represents a grading system that can be selected for a section of a standards-based course.
 *
 * <p>
 * The primary key on the underlying table is the grading system ID.
 */
public final class StandardsCourseGradingSystemRec extends RecBase
        implements Comparable<StandardsCourseGradingSystemRec> {

    /** The table name for serialization of records. */
    public static final String TABLE_NAME = "standards_course_grading_system";

    /** The 'grading_system_id' field value. */
    public final String gradingSystemId;

    /** The 'nbr_standards' field value. */
    public final Integer nbrStandards;

    /** The 'min_standards' field value. */
    public final Integer minStandards;

    /** The 'max_unmastered_essential' field value. */
    public final Integer maxUnmasteredEssential;

    /** The 'homework_pts' field value. */
    public final Integer homeworkPts;

    /** The 'on_time_mastery_pts' field value. */
    public final Integer onTimeMasteryPts;

    /** The 'late_mastery_pts' field value. */
    public final Integer lateMasteryPts;

    /** The 'a_min_score' field value. */
    public final Integer aMinScore;

    /** The 'b_min_score' field value. */
    public final Integer bMinScore;

    /** The 'c_min_score' field value. */
    public final Integer cMinScore;

    /** The 'd_min_score' field value. */
    public final Integer dMinScore;

    /** The 'u_min_score' field value. */
    public final Integer uMinScore;

    /** The 'min_standards_for_inc' field value. */
    public final Integer minStandardsForInc;

    /**
     * Constructs a new {@code StandardsCourseGradingSystemRec}.
     *
     * @param theGradingSystemId        the grading system ID
     * @param theNbrStandards           the number of standards in the course
     * @param theMinStandards           the minimum number of standards that must be mastered to complete the course
     * @param theMaxUnmasteredEssential the maximum number of "essential" standards that can be unmastered to complete
     *                                  the course
     * @param theHomeworkPts            the number of points awarded for completing a homework assignment
     * @param theOnTimeMasteryPts       the number of points awarded for mastering a standard by its due date
     * @param theLateMasteryPts         the number of points awarded for mastering a standard after due date
     * @param theAMinScore              the minimum score needed to earn an A grade
     * @param theBMinScore              the minimum score needed to earn a B grade
     * @param theCMinScore              the minimum score needed to earn a C grade
     * @param theDMinScore              the minimum score needed to earn a D grade (null if D is not awarded)
     * @param theUMinScore              the minimum score needed to earn a U grade (null if all scores below letter
     *                                  grades earn a U grade, non-null if an F grade is given to students who master
     *                                  fewer than this number of standards)
     * @param theMinStandardsForInc     the minimum number of standards that must be completed to become eligible for an
     *                                  Incomplete if the course was not completed
     */
    public StandardsCourseGradingSystemRec(final String theGradingSystemId, final Integer theNbrStandards,
                                           final Integer theMinStandards, final Integer theMaxUnmasteredEssential,
                                           final Integer theHomeworkPts, final Integer theOnTimeMasteryPts,
                                           final Integer theLateMasteryPts, final Integer theAMinScore,
                                           final Integer theBMinScore, final Integer theCMinScore,
                                           final Integer theDMinScore, final Integer theUMinScore,
                                           final Integer theMinStandardsForInc) {

        super();

        if (theGradingSystemId == null) {
            throw new IllegalArgumentException("Grading System ID may not be null");
        }
        if (theNbrStandards == null) {
            throw new IllegalArgumentException("Number of standards may not be null");
        }
        if (theMinStandards == null) {
            throw new IllegalArgumentException("Minimum number of standards to complete may not be null");
        }
        if (theMaxUnmasteredEssential == null) {
            throw new IllegalArgumentException("Maximum unmastered essential standards may not be null");
        }
        if (theHomeworkPts == null) {
            throw new IllegalArgumentException("Homework points may not be null");
        }
        if (theOnTimeMasteryPts == null) {
            throw new IllegalArgumentException("On-time standard mastery points may not be null");
        }
        if (theLateMasteryPts == null) {
            throw new IllegalArgumentException("Late standard mastery points to complete may not be null");
        }
        if (theAMinScore == null) {
            throw new IllegalArgumentException("Minimum score for A grade may not be null");
        }
        if (theBMinScore == null) {
            throw new IllegalArgumentException("Minimum score for B grade may not be null");
        }
        if (theCMinScore == null) {
            throw new IllegalArgumentException("Minimum score for C grade may not be null");
        }

        this.gradingSystemId = theGradingSystemId;
        this.nbrStandards = theNbrStandards;
        this.minStandards = theMinStandards;
        this.maxUnmasteredEssential = theMaxUnmasteredEssential;
        this.homeworkPts = theHomeworkPts;
        this.onTimeMasteryPts = theOnTimeMasteryPts;
        this.lateMasteryPts = theLateMasteryPts;
        this.aMinScore = theAMinScore;
        this.bMinScore = theBMinScore;
        this.cMinScore = theCMinScore;
        this.dMinScore = theDMinScore;
        this.uMinScore = theUMinScore;
        this.minStandardsForInc = theMinStandardsForInc;
    }

    /**
     * Compares two records for order.  Order is based on grading system ID.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final StandardsCourseGradingSystemRec o) {

        return this.gradingSystemId.compareTo(o.gradingSystemId);
    }

    /**
     * Generates a string serialization of the record. Each concrete subclass should have a constructor that accepts a
     * single {@code String} to reconstruct the object from this string.
     *
     * @return the string
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(40);

        appendField(htm, DataDict.FLD_GRADING_SYSTEM_ID, this.gradingSystemId);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_NBR_STANDARDS, this.nbrStandards);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_MIN_STANDARDS, this.minStandards);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_MAX_UNMASTERED_ESSENTIAL, this.maxUnmasteredEssential);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_HOMEWORK_PTS, this.homeworkPts);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_ON_TIME_MASTERY_PTS, this.onTimeMasteryPts);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_LATE_MASTERY_PTS, this.lateMasteryPts);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_A_MIN_SCORE, this.aMinScore);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_B_MIN_SCORE, this.bMinScore);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_C_MIN_SCORE, this.cMinScore);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_D_MIN_SCORE, this.dMinScore);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_U_MIN_SCORE, this.uMinScore);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_MIN_STANDARDS_FOR_INC, this.minStandardsForInc);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return this.gradingSystemId.hashCode()
               + this.nbrStandards.hashCode()
               + this.minStandards.hashCode()
               + this.maxUnmasteredEssential.hashCode()
               + this.homeworkPts.hashCode()
               + this.onTimeMasteryPts.hashCode()
               + this.lateMasteryPts.hashCode()
               + this.aMinScore.hashCode()
               + this.bMinScore.hashCode()
               + this.cMinScore.hashCode()
               + Objects.hashCode(this.dMinScore)
               + Objects.hashCode(this.uMinScore)
               + Objects.hashCode(this.minStandardsForInc);
    }

    /**
     * Tests whether this object is equal to another.
     *
     * @param obj the other object
     * @return true if equal; false if not
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final StandardsCourseGradingSystemRec rec) {
            equal = this.gradingSystemId.equals(rec.gradingSystemId)
                    && this.nbrStandards.equals(rec.nbrStandards)
                    && this.maxUnmasteredEssential.equals(rec.maxUnmasteredEssential)
                    && this.homeworkPts.equals(rec.homeworkPts)
                    && this.onTimeMasteryPts.equals(rec.onTimeMasteryPts)
                    && this.lateMasteryPts.equals(rec.lateMasteryPts)
                    && this.aMinScore.equals(rec.aMinScore)
                    && this.bMinScore.equals(rec.bMinScore)
                    && this.cMinScore.equals(rec.cMinScore)
                    && Objects.equals(this.dMinScore, rec.dMinScore)
                    && Objects.equals(this.uMinScore, rec.uMinScore)
                    && Objects.equals(this.minStandardsForInc, rec.minStandardsForInc);
        } else {
            equal = false;
        }

        return equal;
    }
}
