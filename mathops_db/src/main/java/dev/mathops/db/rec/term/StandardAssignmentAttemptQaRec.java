package dev.mathops.db.rec.term;

import dev.mathops.db.DataDict;
import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.Objects;

/**
 * An immutable raw "standard assignment attempt question/answer" record.
 *
 * <p>
 * Each record represents a student answer to a single question on an attempt on a standard assignment (homework or
 * mastery exam).
 *
 * <p>
 * The primary key on the underlying table is the serial number and question number.
 */
public final class StandardAssignmentAttemptQaRec extends RecBase implements Comparable<StandardAssignmentAttemptQaRec> {

    /** The table name for serialization of records. */
    public static final String TABLE_NAME = "standard_assignment_attempt_qa";

    /** The 'serial_nbr' field value. */
    public final Integer serialNbr;

    /** The 'question_nbr' field value. */
    public final Integer questionNbr;

    /** The 'points' field value. */
    public final Integer points;

    /** The 'item_id' field value. */
    public final String itemId;

    /**
     * Constructs a new {@code StandardAssignmentAttemptQaRec}.
     *
     * @param theSerialNbr   the unique serial number for the attempt (negative for practice mode)
     * @param theQuestionNbr the question number
     * @param thePoints      the points earned
     * @param theItemId      the item ID
     */
    public StandardAssignmentAttemptQaRec(final Integer theSerialNbr, final Integer theQuestionNbr,
                                          final Integer thePoints, final String theItemId) {

        super();

        if (theSerialNbr == null) {
            throw new IllegalArgumentException("Serial number may not be null");
        }
        if (theQuestionNbr == null) {
            throw new IllegalArgumentException("Question number may not be null");
        }
        if (thePoints == null) {
            throw new IllegalArgumentException("Points may not be null");
        }

        this.serialNbr = theSerialNbr;
        this.questionNbr = theQuestionNbr;
        this.points = thePoints;
        this.itemId = theItemId;
    }

    /**
     * Compares two records for order.  Order is based on serial number (which should be monotonically increasing, up to
     * the negation used to indicate practice mode).
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final StandardAssignmentAttemptQaRec o) {

        int result = this.serialNbr.compareTo(o.serialNbr);

        if (result == 0) {
            result = this.questionNbr.compareTo(o.questionNbr);
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

        appendField(htm, DataDict.FLD_SERIAL_NBR, this.serialNbr);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_QUESTION_NBR, this.questionNbr);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_POINTS, this.points);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_ITEM_ID, this.itemId);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return this.serialNbr.hashCode()
               + this.questionNbr.hashCode()
               + this.points.hashCode()
               + Objects.hashCode(this.itemId);
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
        } else if (obj instanceof final StandardAssignmentAttemptQaRec rec) {
            equal = this.serialNbr.equals(rec.serialNbr)
                    && this.questionNbr.equals(rec.questionNbr)
                    && this.points.equals(rec.points)
                    && Objects.equals(this.itemId, rec.itemId);
        } else {
            equal = false;
        }

        return equal;
    }
}
