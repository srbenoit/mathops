package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * A raw "grading_std" record.
 */
public final class RawGradingStd extends RecBase implements Comparable<RawGradingStd> {

    /** The table name. */
    public static final String TABLE_NAME = "grading_std";

    /** A field name. */
    private static final String FLD_GRADING_STD = "grading_std";

    /** A field name. */
    private static final String FLD_ONLY_OVER_MASTERY = "only_over_mastery";

    /** A field name. */
    private static final String FLD_ALLOW_POINT_COUPONS = "allow_point_coupons";

    /** A field name. */
    private static final String FLD_MAX_COUPON_POINTS = "max_coupon_points";

    /** A field name. */
    private static final String FLD_COUPON_FACTOR = "coupon_factor";

    /** The 'grading_std' field value. */
    public String gradingStd;

    /** The 'only_over_mastery' field value. */
    public String onlyOverMastery;

    /** The 'allow_point_coupons' field value. */
    public String allowPointCoupons;

    /** The 'max_coupon_points' field value. */
    public Integer maxCouponPoints;

    /** The 'coupon_factor' field value. */
    public String couponFactor;

    /**
     * Constructs a new {@code RawGradingStd}.
     */
    private RawGradingStd() {

        super();
    }

    /**
     * Constructs a new {@code RawGradingStd}.
     *
     * @param theGradingStd        The 'grading_std' field value
     * @param theOnlyOverMastery   the 'only_over_mastery' field value
     * @param theAllowPointCoupons the 'allow_point_coupons' field value
     * @param theMaxCouponPoints   the 'max_coupon_points' field value
     * @param theCouponFactor      the 'coupon_factor' field value
     */
    public RawGradingStd(final String theGradingStd, final String theOnlyOverMastery, final String theAllowPointCoupons,
                         final Integer theMaxCouponPoints, final String theCouponFactor) {

        super();

        this.gradingStd = theGradingStd;
        this.onlyOverMastery = theOnlyOverMastery;
        this.allowPointCoupons = theAllowPointCoupons;
        this.maxCouponPoints = theMaxCouponPoints;
        this.couponFactor = theCouponFactor;
    }

    /**
     * Extracts a "grading_std" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawGradingStd fromResultSet(final ResultSet rs) throws SQLException {

        final RawGradingStd result = new RawGradingStd();

        result.gradingStd = getStringField(rs, FLD_GRADING_STD);
        result.onlyOverMastery = getStringField(rs, FLD_ONLY_OVER_MASTERY);
        result.allowPointCoupons = getStringField(rs, FLD_ALLOW_POINT_COUPONS);
        result.maxCouponPoints = getIntegerField(rs, FLD_MAX_COUPON_POINTS);
        result.couponFactor = getStringField(rs, FLD_COUPON_FACTOR);

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
    public int compareTo(final RawGradingStd o) {

        return compareAllowingNull(this.gradingStd, o.gradingStd);
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

        appendField(htm, FLD_GRADING_STD, this.gradingStd);
        htm.add(DIVIDER);
        appendField(htm, FLD_ONLY_OVER_MASTERY, this.onlyOverMastery);
        htm.add(DIVIDER);
        appendField(htm, FLD_ALLOW_POINT_COUPONS, this.allowPointCoupons);
        htm.add(DIVIDER);
        appendField(htm, FLD_MAX_COUPON_POINTS, this.maxCouponPoints);
        htm.add(DIVIDER);
        appendField(htm, FLD_COUPON_FACTOR, this.couponFactor);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.gradingStd)
               + Objects.hashCode(this.onlyOverMastery)
               + Objects.hashCode(this.allowPointCoupons)
               + Objects.hashCode(this.maxCouponPoints)
               + Objects.hashCode(this.couponFactor);
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
        } else if (obj instanceof final RawGradingStd rec) {
            equal = Objects.equals(this.gradingStd, rec.gradingStd)
                    && Objects.equals(this.onlyOverMastery, rec.onlyOverMastery)
                    && Objects.equals(this.allowPointCoupons, rec.allowPointCoupons)
                    && Objects.equals(this.maxCouponPoints, rec.maxCouponPoints)
                    && Objects.equals(this.couponFactor, rec.couponFactor);
        } else {
            equal = false;
        }

        return equal;
    }
}
