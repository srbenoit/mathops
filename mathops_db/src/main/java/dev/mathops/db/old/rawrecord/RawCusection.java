package dev.mathops.db.old.rawrecord;

import dev.mathops.commons.CoreConstants;
import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A raw "cusection" record.
 */
public final class RawCusection extends RawTermRecordBase implements Comparable<RawCusection> {

    /** The table name. */
    public static final String TABLE_NAME = "cusection";

    /** Prefix for course numbers. */
    private static final String PREFIX = "M";

    /** Slash character. */
    private static final String SLASH = CoreConstants.SLASH;

    /** A field name. */
    private static final String FLD_COURSE = "course";

    /** A field name. */
    private static final String FLD_SECT = "sect";

    /** A field name. */
    private static final String FLD_UNIT = "unit";

    /** A field name. */
    private static final String FLD_TIMEOUT = "timeout";

    /** A field name. */
    private static final String FLD_RE_MASTERY_SCORE = "re_mastery_score";

    /** A field name. */
    private static final String FLD_UE_MASTERY_SCORE = "ue_mastery_score";

    /** A field name. */
    private static final String FLD_HW_MASTERY_SCORE = "hw_mastery_score";

    /** A field name. */
    private static final String FLD_HW_MOVEON_SCORE = "hw_moveon_score";

    /** A field name. */
    private static final String FLD_NBR_ATMPTS_ALLOW = "nbr_atmpts_allow";

    /** A field name. */
    private static final String FLD_ATMPTS_PER_REVIEW = "atmpts_per_review";

    /** A field name. */
    private static final String FLD_FIRST_TEST_DT = "first_test_dt";

    /** A field name. */
    private static final String FLD_LAST_TEST_DT = "last_test_dt";

    /** A field name. */
    private static final String FLD_BEGIN_TEST_PERIOD = "begin_test_period";

    /** A field name. */
    private static final String FLD_END_TEST_PERIOD = "end_test_period";

    /** A field name. */
    private static final String FLD_COUPON_COST = "coupon_cost";

    /** A field name. */
    private static final String FLD_LAST_COUPON_DT = "last_coupon_dt";

    /** A field name. */
    private static final String FLD_SHOW_TEST_WINDOW = "show_test_window";

    /** A field name. */
    private static final String FLD_UNPROCTORED_EXAM = "unproctored_exam";

    /** A field name. */
    private static final String FLD_RE_POINTS_ONTIME = "re_points_ontime";

    /** The 'course' field value. */
    public String course;

    /** The 'sect' field value. */
    public String sect;

    /** The 'unit' field value. */
    public Integer unit;

    /** The 'timeout' field value. */
    public Integer timeout;

    /** The 're_mastery_score' field value. */
    public Integer reMasteryScore;

    /** The 'ue_mastery_score' field value. */
    public Integer ueMasteryScore;

    /** The 'hw_mastery_score' field value. */
    public Integer hwMasteryScore;

    /** The 'hw_moveon_score' field value. */
    public Integer hwMoveonScore;

    /** The 'nbr_atmpts_allow' field value. */
    public Integer nbrAtmptsAllow;

    /** The 'atmpts_per_review' field value. */
    public Integer atmptsPerReview;

    /** The 'first_test_dt' field value. */
    public LocalDate firstTestDt;

    /** The 'last_test_dt' field value. */
    public LocalDate lastTestDt;

    /** The 'begin_test_period' field value. */
    public Integer beginTestPeriod;

    /** The 'end_test_period' field value. */
    public Integer endTestPeriod;

    /** The 'coupon_cost' field value. */
    public Integer couponCost;

    /** The 'last_coupon_dt' field value. */
    public LocalDate lastCouponDt;

    /** The 'show_test_window' field value. */
    public String showTestWindow;

    /** The 'unproctored_exam' field value. */
    public String unproctoredExam;

    /** The 'rePointsOntime' field value. */
    public Integer rePointsOntime;

    /**
     * Constructs a new {@code RawCusection}.
     */
    private RawCusection() {

        super();
    }

    /**
     * Constructs a new {@code RawCusection}.
     *
     * @param theTermKey         the term key
     * @param theCourse          the course
     * @param theSect            the section
     * @param theUnit            the unit
     * @param theTimeout         the timeout
     * @param theReMasteryStore  the review exam mastery score
     * @param theUeMasteryScore  the unit exam mastery score
     * @param theHwMasteryScore  the homework mastery score
     * @param theHwMoveonScore   the homework move-on score
     * @param theNbrAtmptsAllow  the total number of unit/final exam attempts allowed
     * @param theAtmptsPerReview the number of unit exam attempts per review exam
     * @param theFirstTestDt     the first date the unit exam can be taken
     * @param theLastTestDt      the last date the unit exam can be taken
     * @param theBeginTestPeriod the beginning of the test period
     * @param theEndTestPeriod   the end of the test period
     * @param theCouponCost      the cost in coupons to take the unit exam outside the testing window
     * @param theLastCouponDt    the last date unit exam can be taken with coupons
     * @param theShowTestWindow  "Y" to show the testing window
     * @param theUnproctoredExam the "Y" to allow the unit exam to be taken unproctored
     * @param theRePointsOntime  the number of points awarded for completing the review exam on time
     */
    public RawCusection(final TermKey theTermKey, final String theCourse, final String theSect,
                        final Integer theUnit, final Integer theTimeout, final Integer theReMasteryStore,
                        final Integer theUeMasteryScore, final Integer theHwMasteryScore,
                        final Integer theHwMoveonScore, final Integer theNbrAtmptsAllow,
                        final Integer theAtmptsPerReview, final LocalDate theFirstTestDt,
                        final LocalDate theLastTestDt, final Integer theBeginTestPeriod,
                        final Integer theEndTestPeriod, final Integer theCouponCost,
                        final LocalDate theLastCouponDt, final String theShowTestWindow,
                        final String theUnproctoredExam, final Integer theRePointsOntime) {

        super(theTermKey);

        this.course = theCourse;
        this.sect = theSect;
        this.unit = theUnit;
        this.timeout = theTimeout;
        this.reMasteryScore = theReMasteryStore;
        this.ueMasteryScore = theUeMasteryScore;
        this.hwMasteryScore = theHwMasteryScore;
        this.hwMoveonScore = theHwMoveonScore;
        this.nbrAtmptsAllow = theNbrAtmptsAllow;
        this.atmptsPerReview = theAtmptsPerReview;
        this.firstTestDt = theFirstTestDt;
        this.lastTestDt = theLastTestDt;
        this.beginTestPeriod = theBeginTestPeriod;
        this.endTestPeriod = theEndTestPeriod;
        this.couponCost = theCouponCost;
        this.lastCouponDt = theLastCouponDt;
        this.showTestWindow = theShowTestWindow;
        this.unproctoredExam = theUnproctoredExam;
        this.rePointsOntime = theRePointsOntime;
    }

    /**
     * Extracts a "cusection" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawCusection fromResultSet(final ResultSet rs) throws SQLException {

        final RawCusection result = new RawCusection();

        result.course = getStringField(rs, FLD_COURSE);
        result.sect = getStringField(rs, FLD_SECT);
        result.unit = getIntegerField(rs, FLD_UNIT);
        result.termKey = getTermAndYear(rs, FLD_TERM, FLD_TERM_YR);
        result.timeout = getIntegerField(rs, FLD_TIMEOUT);
        result.reMasteryScore = getIntegerField(rs, FLD_RE_MASTERY_SCORE);
        result.ueMasteryScore = getIntegerField(rs, FLD_UE_MASTERY_SCORE);
        result.hwMasteryScore = getIntegerField(rs, FLD_HW_MASTERY_SCORE);
        result.hwMoveonScore = getIntegerField(rs, FLD_HW_MOVEON_SCORE);
        result.nbrAtmptsAllow = getIntegerField(rs, FLD_NBR_ATMPTS_ALLOW);
        result.atmptsPerReview = getIntegerField(rs, FLD_ATMPTS_PER_REVIEW);
        result.firstTestDt = getDateField(rs, FLD_FIRST_TEST_DT);
        result.lastTestDt = getDateField(rs, FLD_LAST_TEST_DT);
        result.beginTestPeriod = getIntegerField(rs, FLD_BEGIN_TEST_PERIOD);
        result.endTestPeriod = getIntegerField(rs, FLD_END_TEST_PERIOD);
        result.couponCost = getIntegerField(rs, FLD_COUPON_COST);
        result.lastCouponDt = getDateField(rs, FLD_LAST_COUPON_DT);
        result.showTestWindow = getStringField(rs, FLD_SHOW_TEST_WINDOW);
        result.unproctoredExam = getStringField(rs, FLD_UNPROCTORED_EXAM);
        result.rePointsOntime = getIntegerField(rs, FLD_RE_POINTS_ONTIME);

        return result;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final RawCusection o) {

        int result = this.termKey.compareTo(o.termKey);

        if (result == 0) {
            result = this.course.compareTo(o.course);
            if (result == 0) {
                result = this.sect.compareTo(o.sect);
                if (result == 0) {
                    result = this.unit.compareTo(o.unit);
                }
            }
        }

        return result;
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

        appendField(htm, FLD_TERM, this.termKey);
        htm.add(DIVIDER);
        appendField(htm, FLD_COURSE, this.course);
        htm.add(DIVIDER);
        appendField(htm, FLD_SECT, this.sect);
        htm.add(DIVIDER);
        appendField(htm, FLD_UNIT, this.unit);
        htm.add(DIVIDER);
        appendField(htm, FLD_TIMEOUT, this.timeout);
        htm.add(DIVIDER);
        appendField(htm, FLD_RE_MASTERY_SCORE, this.reMasteryScore);
        htm.add(DIVIDER);
        appendField(htm, FLD_UE_MASTERY_SCORE, this.ueMasteryScore);
        htm.add(DIVIDER);
        appendField(htm, FLD_HW_MASTERY_SCORE, this.hwMasteryScore);
        htm.add(DIVIDER);
        appendField(htm, FLD_HW_MOVEON_SCORE, this.hwMoveonScore);
        htm.add(DIVIDER);
        appendField(htm, FLD_NBR_ATMPTS_ALLOW, this.nbrAtmptsAllow);
        htm.add(DIVIDER);
        appendField(htm, FLD_ATMPTS_PER_REVIEW, this.atmptsPerReview);
        htm.add(DIVIDER);
        appendField(htm, FLD_FIRST_TEST_DT, this.firstTestDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_LAST_TEST_DT, this.lastTestDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_BEGIN_TEST_PERIOD, this.beginTestPeriod);
        htm.add(DIVIDER);
        appendField(htm, FLD_END_TEST_PERIOD, this.endTestPeriod);
        htm.add(DIVIDER);
        appendField(htm, FLD_COUPON_COST, this.couponCost);
        htm.add(DIVIDER);
        appendField(htm, FLD_LAST_COUPON_DT, this.lastCouponDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_SHOW_TEST_WINDOW, this.showTestWindow);
        htm.add(DIVIDER);
        appendField(htm, FLD_UNPROCTORED_EXAM, this.unproctoredExam);
        htm.add(DIVIDER);
        appendField(htm, FLD_RE_POINTS_ONTIME, this.rePointsOntime);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.termKey)
               + Objects.hashCode(this.course)
               + Objects.hashCode(this.sect)
               + Objects.hashCode(this.unit)
               + Objects.hashCode(this.timeout)
               + Objects.hashCode(this.reMasteryScore)
               + Objects.hashCode(this.ueMasteryScore)
               + Objects.hashCode(this.hwMasteryScore)
               + Objects.hashCode(this.hwMoveonScore)
               + Objects.hashCode(this.nbrAtmptsAllow)
               + Objects.hashCode(this.atmptsPerReview)
               + Objects.hashCode(this.firstTestDt)
               + Objects.hashCode(this.lastTestDt)
               + Objects.hashCode(this.beginTestPeriod)
               + Objects.hashCode(this.endTestPeriod)
               + Objects.hashCode(this.couponCost)
               + Objects.hashCode(this.lastCouponDt)
               + Objects.hashCode(this.showTestWindow)
               + Objects.hashCode(this.unproctoredExam)
               + Objects.hashCode(this.rePointsOntime);
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
        } else if (obj instanceof final RawCusection rec) {
            equal = Objects.equals(this.termKey, rec.termKey)
                    && Objects.equals(this.course, rec.course)
                    && Objects.equals(this.sect, rec.sect)
                    && Objects.equals(this.unit, rec.unit)
                    && Objects.equals(this.timeout, rec.timeout)
                    && Objects.equals(this.reMasteryScore, rec.reMasteryScore)
                    && Objects.equals(this.ueMasteryScore, rec.ueMasteryScore)
                    && Objects.equals(this.hwMasteryScore, rec.hwMasteryScore)
                    && Objects.equals(this.hwMoveonScore, rec.hwMoveonScore)
                    && Objects.equals(this.nbrAtmptsAllow, rec.nbrAtmptsAllow)
                    && Objects.equals(this.atmptsPerReview, rec.atmptsPerReview)
                    && Objects.equals(this.firstTestDt, rec.firstTestDt)
                    && Objects.equals(this.lastTestDt, rec.lastTestDt)
                    && Objects.equals(this.beginTestPeriod, rec.beginTestPeriod)
                    && Objects.equals(this.endTestPeriod, rec.endTestPeriod)
                    && Objects.equals(this.couponCost, rec.couponCost)
                    && Objects.equals(this.lastCouponDt, rec.lastCouponDt)
                    && Objects.equals(this.showTestWindow, rec.showTestWindow)
                    && Objects.equals(this.unproctoredExam, rec.unproctoredExam)
                    && Objects.equals(this.rePointsOntime, rec.rePointsOntime);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Generate the topmatter associated with a record (FIXME: this is a hardcode until the possible score fields exist
     * in the database).
     *
     * @param record the record
     * @return the topmatter (null if none)
     */
    public static String getTopmatter(final RawCusection record) {

        String topmatter = null;

        if (RawRecordConstants.M100T.equals(record.course)) {
            topmatter = getM100tTopmatter(record.unit);
        } else if (RawRecordConstants.M117.equals(record.course)) {
            topmatter = getPrecalcTopmatter("117", record.unit);
        } else if (RawRecordConstants.M118.equals(record.course)) {
            topmatter = getPrecalcTopmatter("118", record.unit);
        } else if (RawRecordConstants.M124.equals(record.course)) {
            topmatter = getPrecalcTopmatter("124", record.unit);
        } else if (RawRecordConstants.M125.equals(record.course)) {
            topmatter = getPrecalcTopmatter("125", record.unit);
        } else if (RawRecordConstants.M126.equals(record.course)) {
            topmatter = getPrecalcTopmatter("126", record.unit);
        }

        return topmatter;
    }

    /**
     * Generates the topmatter value for the M 100T course (hardcoded here until the database supports the field).
     *
     * @param unit the unit number
     * @return the top matter
     */
    private static String getM100tTopmatter(final Integer unit) {

        String topmatter = null;

        final int unitValue = unit == null ? -1 : unit.intValue();

        if (unitValue == 1) {
            topmatter = setUnitVidProb("M100T/1MTU1.pdf");
        } else if (unitValue == 2) {
            topmatter = setUnitVidProb("M100T/1MTU2.pdf");
        } else if (unitValue == 3) {
            topmatter = setUnitVidProb("M100T/1MTU3.pdf");
        } else if (unitValue == 4) {
            topmatter = setUnitVidProb("M100T/1MTU4.pdf");
        }

        return topmatter;
    }

    /**
     * Generates the topmatter value for a Precalculus course (hardcoded here until the database supports the field).
     *
     * @param number the course number (117 through 126)
     * @param unit   the unit number
     * @return the top matter
     */
    private static String getPrecalcTopmatter(final String number, final Integer unit) {

        String topmatter = null;

        final int unitValue = unit == null ? -1 : unit.intValue();

        if (unitValue == 1) {
            topmatter = setUnitVidProb(PREFIX + number + SLASH + number + "U1.pdf");
        } else if (unitValue == 2) {
            topmatter = setUnitVidProb(PREFIX + number + SLASH + number + "U2.pdf");
        } else if (unitValue == 3) {
            topmatter = setUnitVidProb(PREFIX + number + SLASH + number + "U3.pdf");
        } else if (unitValue == 4) {
            topmatter = setUnitVidProb(PREFIX + number + SLASH + number + "U4.pdf");
        } else if (unitValue == 5) {
            topmatter = toComplete("MATH " + number);
        }

        return topmatter;
    }

    /**
     * Generates the topmatter to have a single PDF file link labeled "Unit Video Problems".
     *
     * @param file the file (a relative path below the /media directory on the streaming server)
     * @return the top matter
     */
    private static String setUnitVidProb(final String file) {

        final HtmlBuilder top = new HtmlBuilder(300);

        top.addln("<p class='indent33'><img src='/images/pdf.png' alt='' style='padding-right:3px;'/> ",
                "<a target='_blank' href='https://nibbler.math.colostate.edu/media/", file,
                "'>Unit Video Problems</a></p>");

        return top.toString();
    }

    /**
     * Generates the topmatter to instructions for completing the final exam in a resident course.
     *
     * @param course the course label
     * @return the top matter
     */
    private static String toComplete(final String course) {

        final HtmlBuilder top = new HtmlBuilder(300);

        top.div("clear");

        top.sP("indent33");
        top.addln(" To complete ", course, ", you must pass the <b>Final Exam</b> with a score of 16 or better.");
        top.eP();

        top.sP("indent33");
        top.addln(" The best way to review for the <b>Final Exam</b> is to practice the <b>Review Exams</b>.");
        top.eP();

        top.sP("indent33");
        top.addln(" Remember that you have <strong>unlimited attempts</strong> on the Final Exam until its deadline. ",
                "If you don't pass the Final Exam by its deadline, but were eligible for the Final Exam on the ",
                "deadline, you get one extra attempt the next day the Center is open.  Once you pass the Final Exam, ",
                "you can continue re-taking it to try to raise your score until the last day of classes.");
        top.eP();

        return top.toString();
    }

    /**
     * Given a mastery score, attempts to deduce the possible score (FIXME: this is a hardcode until the possible score
     * fields exist in the database).
     *
     * @param mastery the mastery score (could be null)
     * @return the deduced possible score (null if input is null)
     */
    public static Integer masteryToPossible(final Integer mastery) {

        Integer possible = null;

        if (mastery != null) {
            if (mastery.intValue() == 8 || mastery.intValue() == 7) {
                possible = Integer.valueOf(10);
            } else if (mastery.intValue() == 11 || mastery.intValue() == 12) {
                possible = Integer.valueOf(15);
            } else if (mastery.intValue() == 16 || mastery.intValue() == 14) {
                possible = Integer.valueOf(20);
            } else {
                possible = Integer.valueOf(mastery.intValue() * 10 / 8);
            }
        }

        return possible;
    }
}
