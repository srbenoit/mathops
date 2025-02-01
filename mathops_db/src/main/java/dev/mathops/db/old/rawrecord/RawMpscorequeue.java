package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A raw "mpscorequeue" record.
 */
public final class RawMpscorequeue extends RecBase {

    /** A field name. */
    private static final String FLD_PIDM = "pidm";

    /** A field name. */
    private static final String FLD_TEST_CODE = "test_code";

    /** A field name. */
    private static final String FLD_TEST_DATE = "test_date";

    /** A field name. */
    private static final String FLD_TEST_SCORE = "test_score";

    /** The 'pidm' field value. */
    public Integer pidm;

    /** The 'test_code' field value. */
    public String testCode;

    /** The 'test_date' field value. */
    public LocalDateTime testDate;

    /** The 'test_score' field value. */
    public String testScore;

    /**
     * Constructs a new {@code RawMpscorequeue}.
     */
    private RawMpscorequeue() {

        super();
    }

    /**
     * Constructs a new {@code RawMpscorequeue}.
     *
     * @param thePidm      the pidm
     * @param theTestCode  the test code
     * @param theTestDate  the test date
     * @param theTestScore the test score
     */
    public RawMpscorequeue(final Integer thePidm, final String theTestCode,
                           final LocalDateTime theTestDate, final String theTestScore) {

        super();

        this.pidm = thePidm;
        this.testCode = theTestCode;
        this.testDate = theTestDate;
        this.testScore = theTestScore;
    }

    /**
     * Extracts an "mpscorequeue" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawMpscorequeue fromResultSet(final ResultSet rs) throws SQLException {

        final RawMpscorequeue result = new RawMpscorequeue();

        result.pidm = getIntegerField(rs, FLD_PIDM);
        result.testCode = getStringField(rs, FLD_TEST_CODE);
        result.testDate = getDateTimeField(rs, FLD_TEST_DATE);
        result.testScore = getStringField(rs, FLD_TEST_SCORE);

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

        appendField(htm, FLD_PIDM, this.pidm);
        htm.add(DIVIDER);
        appendField(htm, FLD_TEST_CODE, this.testCode);
        htm.add(DIVIDER);
        appendField(htm, FLD_TEST_DATE, this.testDate);
        htm.add(DIVIDER);
        appendField(htm, FLD_TEST_SCORE, this.testScore);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.pidm)
                + Objects.hashCode(this.testCode)
                + Objects.hashCode(this.testDate)
                + Objects.hashCode(this.testScore);
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
        } else if (obj instanceof final RawMpscorequeue rec) {
            equal = Objects.equals(this.pidm, rec.pidm)
                    && Objects.equals(this.testCode, rec.testCode)
                    && Objects.equals(this.testDate, rec.testDate)
                    && Objects.equals(this.testScore, rec.testScore);
        } else {
            equal = false;
        }

        return equal;
    }
}
