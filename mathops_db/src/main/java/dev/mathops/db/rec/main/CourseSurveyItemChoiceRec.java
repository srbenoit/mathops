package dev.mathops.db.rec.main;

import dev.mathops.db.DataDict;
import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

/**
 * An immutable raw "course survey item choice" record.
 *
 * <p>
 * Each record represents a possible choice for a multiple-choice or multiple-selection item on a course survey.
 *
 * <p>
 * The primary key on the underlying table is the survey ID, item number, and choice number.
 */
public final class CourseSurveyItemChoiceRec extends RecBase implements Comparable<CourseSurveyItemChoiceRec> {

    /** The table name for serialization of records. */
    public static final String TABLE_NAME = "course_survey_item_choice";

    /** The 'survey_id' field value. */
    public final String surveyId;

    /** The 'item_nbr' field value. */
    public final Integer itemNbr;

    /** The 'choice_nbr' field value. */
    public final Integer choiceNbr;

    /** The 'choice_html' field value. */
    public final String choiceHtml;

    /**
     * Constructs a new {@code CourseSurveyItemChoiceRec}.
     *
     * @param theSurveyId   the survey ID
     * @param theItemNbr    the item number
     * @param theChoiceNbr  the choice number
     * @param theChoiceHtml the HTML of the choice
     */
    public CourseSurveyItemChoiceRec(final String theSurveyId, final Integer theItemNbr, final Integer theChoiceNbr,
                                     final String theChoiceHtml) {

        super();

        if (theSurveyId == null) {
            throw new IllegalArgumentException("Survey ID may not be null");
        }
        if (theItemNbr == null) {
            throw new IllegalArgumentException("Item number may not be null");
        }
        if (theChoiceNbr == null) {
            throw new IllegalArgumentException("Choice number may not be null");
        }
        if (theChoiceHtml == null) {
            throw new IllegalArgumentException("Choice HTML may not be null");
        }

        this.surveyId = theSurveyId;
        this.itemNbr = theItemNbr;
        this.choiceNbr = theChoiceNbr;
        this.choiceHtml = theChoiceHtml;
    }

    /**
     * Compares two records for order.  Order is based only on survey ID then item number then choice number.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final CourseSurveyItemChoiceRec o) {

        int result = this.surveyId.compareTo(o.surveyId);

        if (result == 0) {
            result = this.itemNbr.compareTo(o.itemNbr);

            if (result == 0) {
                result = this.choiceNbr.compareTo(o.choiceNbr);
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

        appendField(htm, DataDict.FLD_SURVEY_ID, this.surveyId);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_ITEM_NBR, this.itemNbr);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_CHOICE_NBR, this.choiceNbr);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_CHOICE_HTML, this.choiceHtml);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return this.surveyId.hashCode()
               + this.itemNbr.hashCode()
               + this.choiceNbr.hashCode()
               + this.choiceHtml.hashCode();
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
        } else if (obj instanceof final CourseSurveyItemChoiceRec rec) {
            equal = this.surveyId.equals(rec.surveyId)
                    && this.itemNbr.equals(rec.itemNbr)
                    && this.choiceNbr.equals(rec.choiceNbr)
                    && this.choiceHtml.equals(rec.choiceHtml);
        } else {
            equal = false;
        }

        return equal;
    }
}
