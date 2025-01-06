package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * A raw "milestone" record.
 */
public final class RawMilestone extends RawTermRecordBase implements Comparable<RawMilestone> {

    /** The desired sort order of milestone types. */
    private static final List<String> TYPE_ORDER = List.of("RE", "FE", "F1");

    /** The table name. */
    public static final String TABLE_NAME = "milestone";

    /** A field name. */
    private static final String FLD_PACE = "pace";

    /** A field name. */
    private static final String FLD_PACE_TRACK = "pace_track";

    /** A field name. */
    private static final String FLD_MS_NBR = "ms_nbr";

    /** A field name. */
    private static final String FLD_MS_TYPE = "ms_type";

    /** A field name. */
    private static final String FLD_MS_DATE = "ms_date";

    /** A field name. */
    private static final String FLD_NBR_ATMPTS_ALLOW = "nbr_atmpts_allow";

    /** A milestone type. */
    public static final String USERS_EXAM = "US";

    /** A milestone type. */
    public static final String SKILLS_REVIEW = "SR";

    /** A milestone type. */
    public static final String HOMEWORK_1 = "H1";

    /** A milestone type. */
    public static final String HOMEWORK_2 = "H2";

    /** A milestone type. */
    public static final String HOMEWORK_3 = "H3";

    /** A milestone type. */
    public static final String HOMEWORK_4 = "H4";

    /** A milestone type. */
    public static final String HOMEWORK_5 = "H5";

    /** A milestone type. */
    public static final String UNIT_REVIEW_EXAM = "RE";

    /** A milestone type. */
    public static final String UNIT_EXAM = "UE";

    /** A milestone type. */
    public static final String FINAL_EXAM = "FE";

    /** A milestone type. */
    public static final String FINAL_LAST_TRY = "F1";

    /** A milestone type. */
    public static final String STANDARD_MASTERY = "ST";

    /** A milestone type. */
    public static final String EXPLORATION = "EX";

    /** A milestone type. */
    public static final String EXPLORATION_1_DAY_LATE = "E1";

    /** A milestone type. */
    public static final String TOUCHPOINT_1R1 = "T1R1";

    /** A milestone type. */
    public static final String TOUCHPOINT_1R3 = "T1R3";

    /** A milestone type. */
    public static final String TOUCHPOINT_1FE = "T1FE";

    /** A milestone type. */
    public static final String TOUCHPOINT_1F1 = "T1F1";

    /** A milestone type. */
    public static final String TOUCHPOINT_2R3 = "T2R3";

    /** A milestone type. */
    public static final String TOUCHPOINT_2FE = "T2FE";

    /** A milestone type. */
    public static final String TOUCHPOINT_2F1 = "T2F1";

    /** A milestone type. */
    public static final String TOUCHPOINT_3R3 = "T3R3";

    /** A milestone type. */
    public static final String TOUCHPOINT_3FE = "T3FE";

    /** A milestone type. */
    public static final String TOUCHPOINT_3F1 = "T3F1";

    /** A milestone type. */
    public static final String TOUCHPOINT_4R3 = "T4R3";

    /** A milestone type. */
    public static final String TOUCHPOINT_4FE = "T4FE";

    /** A milestone type. */
    public static final String TOUCHPOINT_4F1 = "T4F1";

    /** A milestone type. */
    public static final String TOUCHPOINT_5R3 = "T5R3";

    /** A milestone type. */
    public static final String TOUCHPOINT_5FE = "T5FE";

    /** A milestone type. */
    public static final String TOUCHPOINT_5F1 = "T5F1";

    /** The 'pace' field value. */
    public Integer pace;

    /** The 'pace_track' field value. */
    public String paceTrack;

    /** The 'ms_nbr' field value. */
    public Integer msNbr;

    /** The 'ms_type' field value. */
    public String msType;

    /** The 'ms_date' field value. */
    public LocalDate msDate;

    /** The 'nbr_atmpts_allow' field value. */
    public Integer nbrAtmptsAllow;

    /**
     * Constructs a new {@code RawMilestone}.
     */
    public RawMilestone() {

        super();
    }

    /**
     * Constructs a new {@code RawMilestone}.
     *
     * @param theTermKey        the term key
     * @param thePace           the pace
     * @param thePaceTrack      the pace track
     * @param theMsNbr          the milestone number
     * @param theMsType         the milestone type
     * @param theMsDate         the milestone date
     * @param theNbrAtmptsAllow the number of attempts allowed
     */
    public RawMilestone(final TermKey theTermKey, final Integer thePace, final String thePaceTrack,
                        final Integer theMsNbr, final String theMsType, final LocalDate theMsDate,
                        final Integer theNbrAtmptsAllow) {

        super(theTermKey);

        this.pace = thePace;
        this.paceTrack = thePaceTrack;
        this.msNbr = theMsNbr;
        this.msType = theMsType;
        this.msDate = theMsDate;
        this.nbrAtmptsAllow = theNbrAtmptsAllow;
    }

    /**
     * Extracts a "milestone" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawMilestone fromResultSet(final ResultSet rs) throws SQLException {

        final RawMilestone result = new RawMilestone();

        result.termKey = getTermAndYear(rs, FLD_TERM, FLD_TERM_YR);
        result.pace = getIntegerField(rs, FLD_PACE);
        result.paceTrack = getStringField(rs, FLD_PACE_TRACK);
        result.msNbr = getIntegerField(rs, FLD_MS_NBR);
        result.msType = getStringField(rs, FLD_MS_TYPE);
        result.msDate = getDateField(rs, FLD_MS_DATE);
        result.nbrAtmptsAllow = getIntegerField(rs, FLD_NBR_ATMPTS_ALLOW);

        return result;
    }

    /**
     * Gets the course index from the milestone number (the second digit is index).
     *
     * @return the index (1 to pace)
     */
    public int getIndex() {

        return (this.msNbr.intValue() / 10) % 10;
    }

    /**
     * Gets the unit from the milestone number (the last digit is index).
     *
     * @return the unit (1 to 5)
     */
    public int getUnit() {

        return this.msNbr.intValue() % 10;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final RawMilestone o) {

        int result = compareAllowingNull(this.termKey, o.termKey);

        if (result == 0) {
            result = compareAllowingNull(this.paceTrack, o.paceTrack);

            if (result == 0) {
                result = compareAllowingNull(this.msNbr, o.msNbr);

                if (result == 0) {
                    result = compareAllowingNull(this.msDate, this.msDate);

                    if (result == 0) {

                        if (this.msType == null || o.msType == null) {
                            result = compareAllowingNull(this.msType, o.msType);
                        } else {
                            final int myTypeIndex = TYPE_ORDER.indexOf(this.msType);
                            final int oTypeIndex = TYPE_ORDER.indexOf(o.msType);
                            result = Integer.compare(myTypeIndex, oTypeIndex);
                        }
                    }
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
        appendField(htm, FLD_PACE, this.pace);
        htm.add(DIVIDER);
        appendField(htm, FLD_PACE_TRACK, this.paceTrack);
        htm.add(DIVIDER);
        appendField(htm, FLD_MS_NBR, this.msNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_MS_TYPE, this.msType);
        htm.add(DIVIDER);
        appendField(htm, FLD_MS_DATE, this.msDate);
        htm.add(DIVIDER);
        appendField(htm, FLD_NBR_ATMPTS_ALLOW, this.nbrAtmptsAllow);

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
               + Objects.hashCode(this.pace)
               + Objects.hashCode(this.paceTrack)
               + Objects.hashCode(this.msNbr)
               + Objects.hashCode(this.msType)
               + Objects.hashCode(this.msDate)
               + Objects.hashCode(this.nbrAtmptsAllow);
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
        } else if (obj instanceof final RawMilestone rec) {
            equal = Objects.equals(this.termKey, rec.termKey)
                    && Objects.equals(this.pace, rec.pace)
                    && Objects.equals(this.paceTrack, rec.paceTrack)
                    && Objects.equals(this.msNbr, rec.msNbr)
                    && Objects.equals(this.msType, rec.msType)
                    && Objects.equals(this.msDate, rec.msDate)
                    && Objects.equals(this.nbrAtmptsAllow, rec.nbrAtmptsAllow);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Gets the friendly string name of the milestone type.
     *
     * @return the string name
     */
    public String getTypeString() {

        return switch (this.msType) {
            case "RE" -> "Review Exam";
            case "UE" -> "Unit Exam";
            case "FE" -> "Final Exam";
            case "F1" -> "Final +1";
            case "SR" -> "Skills Review";
            case "H1" -> "Homework 1";
            case "H2" -> "Homework 2";
            case "H3" -> "Homework 3";
            case "H4" -> "Homework 4";
            case "H5" -> "Homework 5";
            case null, default -> this.msType;
        };
    }
}
