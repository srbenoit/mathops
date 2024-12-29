package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * A raw "pacing_rules" record.
 */
public final class RawPacingRules extends RawTermRecordBase implements Comparable<RawPacingRules> {

    /** The table name. */
    public static final String TABLE_NAME = "pacing_rules";

    // /** Lecture. */
    // public static final String ACTIVITY_LECTURE = "LE";

    /** Homework. */
    public static final String ACTIVITY_HOMEWORK = "HW";

    /** Skills Review Exam. */
    public static final String ACTIVITY_SR_EXAM = "SR";

    /** Unit Review Exam. */
    public static final String ACTIVITY_UNIT_REV_EXAM = "RE";

    /** Unit Exam. */
    public static final String ACTIVITY_UNIT_EXAM = "UE";

    /** Final Exam. */
    public static final String ACTIVITY_FINAL_EXAM = "FE";

    /** Video lecture accessed. */
    public static final String LECT_VIEWED = "LECT";

    // /** Homework attempted. */
    // public static final String HW_ATMT = "HW_A";

    /** Homework passed. */
    public static final String HW_PASS = "HW_P";

    /** Homework mastered. */
    public static final String HW_MSTR = "HW_M";

    // /** Skills Review Exam attempted. */
    // public static final String SR_ATMT = "SR_A";

    // /** Skills Review Exam passed. */
    // public static final String SR_PASS = "SR_P";

    // /** Skills Review Exam mastered. */
    // public static final String SR_MSTR = "SR_M";

    // /** Unit Review Exam attempted. */
    // public static final String UR_ATMT = "RE_A";

    /** Unit Review Exam passed. */
    public static final String UR_PASS = "RE_P";

    /** Unit Review Exam mastered. */
    public static final String UR_MSTR = "RE_M";

    // /** Unit Exam attempted. */
    // public static final String UE_ATMT = "UE_A";

    /** Unit Exam passed. */
    public static final String UE_PASS = "UE_P";

    /** Unit Exam mastered. */
    public static final String UE_MSTR = "UE_M";

    // /** Unit Terminal Exam attempted. */
    // public static final String TE_ATMT = "TE_A";

    /** Unit Terminal Exam passed. */
    public static final String TE_PASS = "TE_P";

    /** Unit Terminal Exam mastered. */
    public static final String TE_MSTR = "TE_M";

    /** A field name. */
    private static final String FLD_PACING_STRUCTURE = "pacing_structure";

    /** A field name. */
    private static final String FLD_ACTIVITY_TYPE = "activity_type";

    /** A field name. */
    private static final String FLD_REQUIREMENT = "requirement";

    /** The 'pacing_structure' field value. */
    public String pacingStructure;

    /** The 'activity_type' field value. */
    public String activityType;

    /** The 'requirement' field value. */
    public String requirement;

    /**
     * Constructs a new {@code RawPacingRules}.
     */
    private RawPacingRules() {

        super();
    }

    /**
     * Constructs a new {@code RawPacingRules}.
     *
     * @param theTermKey         the term key
     * @param thePacingStructure the pacing structure ID
     * @param theActivityType    the activity type
     * @param theRequirement     the requirement
     */
    public RawPacingRules(final TermKey theTermKey, final String thePacingStructure,
                          final String theActivityType, final String theRequirement) {

        super(theTermKey);

        this.pacingStructure = thePacingStructure;
        this.activityType = theActivityType;
        this.requirement = theRequirement;
    }

    /**
     * Extracts a "remote_mpe" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawPacingRules fromResultSet(final ResultSet rs) throws SQLException {

        final RawPacingRules result = new RawPacingRules();

        result.termKey = getTermAndYear(rs, FLD_TERM, FLD_TERM_YR);
        result.pacingStructure = getStringField(rs, FLD_PACING_STRUCTURE);
        result.activityType = getStringField(rs, FLD_ACTIVITY_TYPE);
        result.requirement = getStringField(rs, FLD_REQUIREMENT);

        return result;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object.
     */
    @Override
    public int compareTo(final RawPacingRules o) {

        int result = this.termKey.compareTo(o.termKey);

        if (result == 0) {
            result = this.pacingStructure.compareTo(o.pacingStructure);
            if (result == 0) {
                result = this.activityType.compareTo(o.activityType);
                if (result == 0) {
                    result = this.requirement.compareTo(o.requirement);
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
        appendField(htm, FLD_PACING_STRUCTURE, this.pacingStructure);
        htm.add(DIVIDER);
        appendField(htm, FLD_ACTIVITY_TYPE, this.activityType);
        htm.add(DIVIDER);
        appendField(htm, FLD_REQUIREMENT, this.requirement);

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
                + Objects.hashCode(this.pacingStructure)
                + Objects.hashCode(this.activityType)
                + Objects.hashCode(this.requirement);
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
        } else if (obj instanceof final RawPacingRules rec) {
            equal = Objects.equals(this.termKey, rec.termKey)
                    && Objects.equals(this.pacingStructure, rec.pacingStructure)
                    && Objects.equals(this.activityType, rec.activityType)
                    && Objects.equals(this.requirement, rec.requirement);
        } else {
            equal = false;
        }

        return equal;
    }
}
