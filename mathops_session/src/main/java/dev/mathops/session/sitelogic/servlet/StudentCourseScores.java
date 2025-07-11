package dev.mathops.session.sitelogic.servlet;

import java.util.Arrays;

/**
 * A data object to track all sources of points for a student in a course.
 * <p>
 * Points can come from the raw scores on unit and final exams, point values assigned for completion of unit or review
 * exams on-time or late, and points from coupons. This class also supports calculating the student's total course score
 * from these constituent parts.
 */
public final class StudentCourseScores {

    /** A small number to prevent erroneous round-off. */
    private static final float EPSILON = 0.0001f;

    /** The highest raw unit and final exam scores for the student. */
    private final int[] rawUeScores;

    /** The highest passing unit and final exam scores for the student. */
    private final int[] passingUeScores;

    /** The counted unit and final exam scores for the student. */
    private final int[] countedUeScores;

    /** Points awarded for completion of unit and final exams on time. */
    private final int[] ueOntimePoints;

    /** Points awarded for completion of unit and final exams late. */
    private final int[] ueLatePoints;

    /** Weight to apply to each unit exam. */
    private final double[] ueWeights;

    /** The gateway and review exam scores for the student. */
    private final int[] reScores;

    /** Points awarded for completion of gateway and review exams on time. */
    private final int[] reOntimePoints;

    /** Points awarded for completion of gateway and review exams late. */
    private final int[] reLatePoints;

//    /** The coupon point scale to be applied to {@code numCouponPoints}. */
//    private double couponScale;

//    /** The maximum number of points that may come from scaled coupon points. */
//    private int maxCouponPoints;

//    /** The number of scaled points earned toward course score. */
//    private int earnedCouponPoints;

    /** The computed total course score. */
    private int totalScore;

    /**
     * Constructs a new {@code StudentCourseScores}.
     *
     * @param maxUnit the maximum unit number (units are numbered from 0 to this value).
     */
    StudentCourseScores(final int maxUnit) {

        this.rawUeScores = new int[maxUnit + 1];
        this.passingUeScores = new int[maxUnit + 1];
        this.countedUeScores = new int[maxUnit + 1];
        this.ueOntimePoints = new int[maxUnit + 1];
        this.ueLatePoints = new int[maxUnit + 1];
        this.ueWeights = new double[maxUnit + 1];
        this.reScores = new int[maxUnit + 1];
        this.reOntimePoints = new int[maxUnit + 1];
        this.reLatePoints = new int[maxUnit + 1];
//        this.couponScale = 1.0;
//        this.maxCouponPoints = 0;
        this.totalScore = 0;

        Arrays.fill(this.ueWeights, 1.0);
    }

//    /**
//     * Gets the maximum unit number.
//     *
//     * @return the maximum unit number
//     */
//    public int getMaxUnit() {
//
//        return this.rawUeScores.length - 1;
//    }

    /**
     * Sets the raw unit exam score for a unit.
     *
     * @param unit     the unit
     * @param rawScore the raw score
     */
    void setRawUnitExamScore(final int unit, final int rawScore) {

        this.rawUeScores[unit] = rawScore;
    }

    /**
     * Gets the raw unit exam score for a unit.
     *
     * @param unit the unit
     * @return the raw score
     */
    public int getRawUnitExamScore(final int unit) {

        return this.rawUeScores[unit];
    }

    /**
     * Sets the passing unit exam score for a unit.
     *
     * @param unit     the unit
     * @param rawScore the passing score
     */
    void setPassingUnitExamScore(final int unit, final int rawScore) {

        this.passingUeScores[unit] = rawScore;
    }

//    /**
//     * Gets the counted unit exam score for a unit.
//     *
//     * @param unit the unit
//     * @return the counted score
//     */
//    public int getCountedUnitExamScore(final int unit) {
//
//        return this.countedUeScores[unit];
//    }

    /**
     * Gets the passing unit exam score for a unit.
     *
     * @param unit the unit
     * @return the passing score
     */
    int getPassingUnitExamScore(final int unit) {

        return this.passingUeScores[unit];
    }

    /**
     * Sets the counted unit exam score for a unit.
     *
     * @param unit         the unit
     * @param countedScore the counted score
     */
    void setCountedUnitExamScore(final int unit, final int countedScore) {

        this.countedUeScores[unit] = countedScore;
    }

    /**
     * Sets the number of points awarded for completing a unit exam on time.
     *
     * @param unit      the unit
     * @param numPoints the number of points
     */
    void setOntimeUnitExamScore(final int unit, final int numPoints) {

        this.ueOntimePoints[unit] = numPoints;
    }

    /**
     * Sets the number of points awarded for completing a unit exam late.
     *
     * @param unit      the unit
     * @param numPoints the number of points
     */
    void setLateUnitExamScore(final int unit, final int numPoints) {

        this.ueLatePoints[unit] = numPoints;
    }

//    /**
//     * Sets the weight by which to scale unit exam points when calculating total score.
//     *
//     * @param unit   the unit
//     * @param weight the weight (1.0 if no scaling)
//     */
//    void setUnitExamWeight(final int unit, final double weight) {
//
//        this.ueWeights[unit] = weight;
//    }

    /**
     * Sets the number of points awarded for completing a review exam on time.
     *
     * @param unit      the unit
     * @param numPoints the number of points
     */
    void setOntimeReviewExamScore(final int unit, final int numPoints) {

        this.reOntimePoints[unit] = numPoints;
    }

    /**
     * Sets the number of points awarded for completing a review exam late.
     *
     * @param unit      the unit
     * @param numPoints the number of points
     */
    void setLateReviewExamScore(final int unit, final int numPoints) {

        this.reLatePoints[unit] = numPoints;
    }

//    /**
//     * Sets the scale to be applied to the number of accumulated coupon points to generate a point contribution to
//     * total course score, subject to a possible maximum number of coupon points.
//     *
//     * @param scale the scale
//     */
//    public void setCouponScale(final float scale) {
//
//        this.couponScale = scale;
//    }

//    /**
//     * Gets the scale to be applied to the number of accumulated coupon points to generate a point contribution to
//     * total course score, subject to a possible maximum number of coupon points.
//     *
//     * @return the number of points
//     */
//    public double getCouponScale() {
//
//        return this.couponScale;
//    }

//    /**
//     * Sets the maximum number of points the student can earn toward their course total from scaled coupon points.
//     *
//     * @param maxPoints the maximum number of points
//     */
//    public void setMaxCouponPoints(final int maxPoints) {
//
//        this.maxCouponPoints = maxPoints;
//    }

//    /**
//     * Gets the maximum number of points the student can earn toward their course total from scaled coupon points.
//     *
//     * @return the maximum number of points
//     */
//    public int getMaxCouponPoints() {
//
//        return this.maxCouponPoints;
//    }

    /**
     * Calculates the total course score and stores it.
     */
    void calculateTotalScore() {

        int score = 0;

        final int count = this.rawUeScores.length;
        for (int i = 0; i < count; ++i) {
            score += (int) Math.floor((double) this.countedUeScores[i] * this.ueWeights[i] + (double) EPSILON);
            score += this.ueOntimePoints[i];
            score += this.ueLatePoints[i];
            score += this.reScores[i];
            score += this.reOntimePoints[i];
            score += this.reLatePoints[i];

            // Log.info("UNIT " + i + //
            // ": Unit = ", Math.floor(this.countedUeScores[i] * this.ueWeights[i] + EPSILON) //
            // + ": UE ontime = " + this.ueOntimePoints[i] //
            // + ": UE late = " + this.ueLatePoints[i] //
            // + ": RE score = " + this.reScores[i] //
            // + ": RE ontime = " + this.reOntimePoints[i] //
            // + ": RE late = " + this.reLatePoints[i]);
        }

        this.totalScore = score;
    }

//    /**
//     * Gets the number of points (after scaling) earned toward total score from coupon points.
//     *
//     * @return the number of earned coupon points
//     */
//    public int getEarnedCouponPoints() {
//
//        return this.earnedCouponPoints;
//    }

    /**
     * Gets the calculated total score.
     *
     * @return the calculated total score
     */
    public int getTotalScore() {

        return this.totalScore;
    }
}
