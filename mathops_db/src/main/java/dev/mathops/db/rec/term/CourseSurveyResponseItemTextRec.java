package dev.mathops.db.rec.term;

import dev.mathops.db.DataDict;
import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

/**
 * An immutable raw "course survey response item text" record.
 *
 * <p>
 * Each record represents a student's response to a text item on a course survey.
 *
 * <p>
 * The primary key on the underlying table is the serial number of the response and the item number.
 */
public final class CourseSurveyResponseItemTextRec extends RecBase implements Comparable<CourseSurveyResponseItemTextRec> {

    /** The table name. */
    public static final String TABLE_NAME = "course_survey_response_item_text";

    /** The 'serial_nbr' field value. */
    public final Integer serialNbr;

    /** The 'item_nbr' field value. */
    public final Integer itemNbr;

    /** The 'response_text' field value. */
    public final String responseText;

    /**
     * Constructs a new {@code CourseSurveyResponseItemChoice}.
     *
     * @param theSerialNbr    the student ID
     * @param theItemNbr      the item number
     * @param theResponseText the response text
     */
    public CourseSurveyResponseItemTextRec(final Integer theSerialNbr, final Integer theItemNbr,
                                           final String theResponseText) {

        super();

        if (theSerialNbr == null) {
            throw new IllegalArgumentException("Serial number may not be null");
        }
        if (theItemNbr == null) {
            throw new IllegalArgumentException("Item number may not be null");
        }
        if (theResponseText == null) {
            throw new IllegalArgumentException("Response text not be null");
        }

        this.serialNbr = theSerialNbr;
        this.itemNbr = theItemNbr;
        this.responseText = theResponseText;
    }

    /**
     * Compares two records for order.  Order is based on serial number then item number.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final CourseSurveyResponseItemTextRec o) {

        int result = this.serialNbr.compareTo(o.serialNbr);

        if (result == 0) {
            result = this.itemNbr.compareTo(o.itemNbr);
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
        appendField(htm, DataDict.FLD_ITEM_NBR, this.itemNbr);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_RESPONSE_TEXT, this.responseText);

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
               + this.itemNbr.hashCode()
               + this.responseText.hashCode();
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
        } else if (obj instanceof final CourseSurveyResponseItemTextRec rec) {
            equal = this.serialNbr.equals(rec.serialNbr)
                    && this.itemNbr.equals(rec.itemNbr)
                    && this.responseText.equals(rec.responseText);
        } else {
            equal = false;
        }

        return equal;
    }
}
