package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A raw "stresource" record.
 */
public final class RawStresource extends RecBase implements Comparable<RawStresource> {

    /** The table name. */
    public static final String TABLE_NAME = "stresource";

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_RESOURCE_ID = "resource_id";

    /** A field name. */
    private static final String FLD_LOAN_DT = "loan_dt";

    /** A field name. */
    private static final String FLD_START_TIME = "start_time";

    /** A field name. */
    private static final String FLD_DUE_DT = "due_dt";

    /** A field name. */
    private static final String FLD_RETURN_DT = "return_dt";

    /** A field name. */
    private static final String FLD_FINISH_TIME = "finish_time";

    /** A field name. */
    private static final String FLD_TIMES_DISPLAY = "times_display";

    /** A field name. */
    private static final String FLD_CREATE_DT = "create_dt";

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'resource_id' field value. */
    public String resourceId;

    /** The 'loan_dt' field value. */
    public LocalDate loanDt;

    /** The 'start_time' field value. */
    public Integer startTime;

    /** The 'due_dt' field value. */
    public LocalDate dueDt;

    /** The 'return_dt' field value. */
    public LocalDate returnDt;

    /** The 'finish_time' field value. */
    public Integer finishTime;

    /** The 'times_display' field value. */
    public Integer timesDisplay;

    /** The 'create_dt' field value. */
    public LocalDate createDt;

    /**
     * Constructs a new {@code RawStresource}.
     */
    private RawStresource() {

        super();
    }

    /**
     * Constructs a new {@code RawStresource}.
     *
     * @param theStuId        the student ID
     * @param theResourceId   the resource ID
     * @param theLoanDt       the date of the loan
     * @param theStartTime    the time (minutes past midnight) of the loan
     * @param theDueDt        the date the resource is due
     * @param theReturnDt     the date the resource was returned
     * @param theFinishTime   the time (minutes past midnight) of the resource was returned
     * @param theTimesDisplay the number of times this has been displayed
     * @param theCreateDt     the date the record was created
     */
    public RawStresource(final String theStuId, final String theResourceId,
                         final LocalDate theLoanDt, final Integer theStartTime, final LocalDate theDueDt,
                         final LocalDate theReturnDt, final Integer theFinishTime, final Integer theTimesDisplay,
                         final LocalDate theCreateDt) {

        super();

        this.stuId = theStuId;
        this.resourceId = theResourceId;
        this.loanDt = theLoanDt;
        this.startTime = theStartTime;
        this.dueDt = theDueDt;
        this.returnDt = theReturnDt;
        this.finishTime = theFinishTime;
        this.timesDisplay = theTimesDisplay;
        this.createDt = theCreateDt;
    }

    /**
     * Extracts a "stresource" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawStresource fromResultSet(final ResultSet rs) throws SQLException {

        final RawStresource result = new RawStresource();

        result.stuId = getStringField(rs, FLD_STU_ID);
        result.resourceId = getStringField(rs, FLD_RESOURCE_ID);
        result.loanDt = getDateField(rs, FLD_LOAN_DT);
        result.startTime = getIntegerField(rs, FLD_START_TIME);
        result.dueDt = getDateField(rs, FLD_DUE_DT);
        result.returnDt = getDateField(rs, FLD_RETURN_DT);
        result.finishTime = getIntegerField(rs, FLD_FINISH_TIME);
        result.timesDisplay = getIntegerField(rs, FLD_TIMES_DISPLAY);
        result.createDt = getDateField(rs, FLD_CREATE_DT);

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
    public int compareTo(final RawStresource o) {

        int result = this.stuId.compareTo(o.stuId);

        if (result == 0) {
            result = this.loanDt.compareTo(o.loanDt);
            if (result == 0) {
                result = this.resourceId.compareTo(o.resourceId);
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

        appendField(htm, FLD_STU_ID, this.stuId);
        htm.add(DIVIDER);
        appendField(htm, FLD_RESOURCE_ID, this.resourceId);
        htm.add(DIVIDER);
        appendField(htm, FLD_LOAN_DT, this.loanDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_START_TIME, this.startTime);
        htm.add(DIVIDER);
        appendField(htm, FLD_DUE_DT, this.dueDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_RETURN_DT, this.returnDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_FINISH_TIME, this.finishTime);
        htm.add(DIVIDER);
        appendField(htm, FLD_TIMES_DISPLAY, this.timesDisplay);
        htm.add(DIVIDER);
        appendField(htm, FLD_CREATE_DT, this.createDt);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.stuId)
                + Objects.hashCode(this.resourceId)
                + Objects.hashCode(this.loanDt)
                + Objects.hashCode(this.startTime)
                + Objects.hashCode(this.dueDt)
                + Objects.hashCode(this.returnDt)
                + Objects.hashCode(this.finishTime)
                + Objects.hashCode(this.timesDisplay)
                + Objects.hashCode(this.createDt);
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
        } else if (obj instanceof final RawStresource rec) {
            equal = Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.resourceId, rec.resourceId)
                    && Objects.equals(this.loanDt, rec.loanDt)
                    && Objects.equals(this.startTime, rec.startTime)
                    && Objects.equals(this.dueDt, rec.dueDt)
                    && Objects.equals(this.returnDt, rec.returnDt)
                    && Objects.equals(this.finishTime, rec.finishTime)
                    && Objects.equals(this.timesDisplay, rec.timesDisplay)
                    && Objects.equals(this.createDt, rec.createDt);
        } else {
            equal = false;
        }

        return equal;
    }
}
