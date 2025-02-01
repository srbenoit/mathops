package dev.mathops.db.rec;

import dev.mathops.text.builder.HtmlBuilder;

import java.util.Objects;

/**
 * A "report permissions" record.
 */
public final class ReportPermsRec extends RecBase implements Comparable<ReportPermsRec> {

    /** The table name. */
    public static final String TABLE_NAME = "report_perms";

    /** A field name for serialization of records. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name for serialization of records. */
    private static final String FLD_RPT_ID = "rpt_id";

    /** A field name for serialization of records. */
    private static final String FLD_PERM_LEVEL = "perm_level";

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'rpt_id' field value. */
    public String rptId;

    /** The 'perm_level' field value. */
    public Integer permLevel;

    /**
     * Constructs a new {@code ReportPermsRec}.
     */
    public ReportPermsRec() {

        super();
    }

    /**
     * Constructs a new {@code ReportPermsRec}.
     *
     * @param theStuId     the student ID
     * @param theRptId     the report ID
     * @param thePermLevel the permission level
     */
    public ReportPermsRec(final String theStuId, final String theRptId, final Integer thePermLevel) {

        super();

        this.stuId = theStuId;
        this.rptId = theRptId;
        this.permLevel = thePermLevel;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final ReportPermsRec o) {

        int result = compareAllowingNull(this.stuId, o.stuId);

        if (result == 0) {
            result = compareAllowingNull(this.rptId, o.rptId);
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
        appendField(htm, FLD_RPT_ID, this.rptId);
        htm.add(DIVIDER);
        appendField(htm, FLD_PERM_LEVEL, this.permLevel);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.stuId) //
                + Objects.hashCode(this.rptId) //
                + Objects.hashCode(this.permLevel);
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
        } else if (obj instanceof final ReportPermsRec rec) {
            equal = Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.rptId, rec.rptId)
                    && Objects.equals(this.permLevel, rec.permLevel);
        } else {
            equal = false;
        }

        return equal;
    }
}
