package dev.mathops.db.rec.term;

import dev.mathops.db.DataDict;
import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

/**
 * An immutable raw "course survey response item choice" record.
 *
 * <p>
 * Each record represents a student's response to a multiple-choice or multiple-selection item on a course survey.  Each
 * choice will have an associated integer value - the response value will be the bitwise OR of the selected choice(s).
 *
 * <p>
 * The primary key on the underlying table is the serial number of the response and the item number.
 */
public final class CourseSurveyResponseItemChoiceRec extends RecBase implements Comparable<CourseSurveyResponseItemChoiceRec> {

    /** The table name. */
    public static final String TABLE_NAME = "course_survey_response_item_choice";

    /** The 'serial_nbr' field value. */
    public final Integer serialNbr;

    /** The 'item_nbr' field value. */
    public final Integer itemNbr;

    /** The 'response_choice' field value. */
    public final Integer responseChoice;

    /**
     * Constructs a new {@code CourseSurveyResponseItemChoice}.
     *
     * @param theSerialNbr      the student ID
     * @param theItemNbr        the item number
     * @param theResponseChoice the response choice (a bitwise OR of values associated with all selected choices)
     */
    public CourseSurveyResponseItemChoiceRec(final Integer theSerialNbr, final Integer theItemNbr,
                                             final Integer theResponseChoice) {

        super();

        if (theSerialNbr == null) {
            throw new IllegalArgumentException("Serial number may not be null");
        }
        if (theItemNbr == null) {
            throw new IllegalArgumentException("Item number may not be null");
        }
        if (theResponseChoice == null) {
            throw new IllegalArgumentException("Response choice not be null");
        }

        this.serialNbr = theSerialNbr;
        this.itemNbr = theItemNbr;
        this.responseChoice = theResponseChoice;
    }

    /**
     * Compares two records for order.  Order is based on serial number then item number.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final CourseSurveyResponseItemChoiceRec o) {

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
        appendField(htm, DataDict.FLD_RESPONSE_CHOICE, this.responseChoice);

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
               + this.responseChoice.hashCode();
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
        } else if (obj instanceof final CourseSurveyResponseItemChoiceRec rec) {
            equal = this.serialNbr.equals(rec.serialNbr)
                    && this.itemNbr.equals(rec.itemNbr)
                    && this.responseChoice.equals(rec.responseChoice);
        } else {
            equal = false;
        }

        return equal;
    }
}
