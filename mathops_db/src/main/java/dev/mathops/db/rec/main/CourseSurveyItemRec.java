package dev.mathops.db.rec.main;

import dev.mathops.db.DataDict;
import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

/**
 * An immutable raw "course survey item" record.
 *
 * <p>
 * Each record represents a single item on a course survey.
 *
 * <p>
 * The primary key on the underlying table is the survey ID and item number.
 */
public final class CourseSurveyItemRec extends RecBase implements Comparable<CourseSurveyItemRec> {

    /** Integer constant for item type. */
    public static final int MULTIPLE_CHOICE = 1;

    /** Integer constant for item type. */
    public static final int MULTIPLE_SELECTION = 2;

    /** Integer constant for item type (1=Strongly Disagree, 2=Disagree, 3=No Opinion, 4=Agree, 5=Strongly Agree) */
    public static final int LIKERT_AGREE = 3;

    /** Integer constant for item type (1=Very good, 2=Pretty good, 3=Not very good, 4=Very bad) */
    public static final int LIKERT_EFFICAFCY = 4;

    /** Integer constant for item type. */
    public static final int TEXT = 9;

    /** The table name for serialization of records. */
    public static final String TABLE_NAME = "course_survey_item";

    /** The 'survey_id' field value. */
    public final String surveyId;

    /** The 'item_nbr' field value. */
    public final Integer itemNbr;

    /** The 'item_type' field value. */
    public final Integer itemType;

    /** The 'prompt_html' field value. */
    public final String promptHtml;

    /**
     * Constructs a new {@code CourseSurveyItemRec}.
     *
     * @param theSurveyId   the survey ID
     * @param theItemNbr    the item number
     * @param theItemType   the item type
     * @param thePromptHtml the HTML of the prompt
     */
    public CourseSurveyItemRec(final String theSurveyId, final Integer theItemNbr, final Integer theItemType,
                               final String thePromptHtml) {

        super();

        if (theSurveyId == null) {
            throw new IllegalArgumentException("Survey ID may not be null");
        }
        if (theItemNbr == null) {
            throw new IllegalArgumentException("Item number may not be null");
        }
        if (theItemType == null) {
            throw new IllegalArgumentException("Item type may not be null");
        }
        if (thePromptHtml == null) {
            throw new IllegalArgumentException("Prompt HTML may not be null");
        }

        this.surveyId = theSurveyId;
        this.itemNbr = theItemNbr;
        this.itemType = theItemType;
        this.promptHtml = thePromptHtml;
    }

    /**
     * Compares two records for order.  Order is based only on survey ID then item number.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final CourseSurveyItemRec o) {

        int result = this.surveyId.compareTo(o.surveyId);

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

        appendField(htm, DataDict.FLD_SURVEY_ID, this.surveyId);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_ITEM_NBR, this.itemNbr);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_ITEM_TYPE, this.itemType);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_PROMPT_HTML, this.promptHtml);

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
               + this.itemType.hashCode()
               + this.promptHtml.hashCode();
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
        } else if (obj instanceof final CourseSurveyItemRec rec) {
            equal = this.surveyId.equals(rec.surveyId)
                    && this.itemNbr.equals(rec.itemNbr)
                    && this.itemType.equals(rec.itemType)
                    && this.promptHtml.equals(rec.promptHtml);
        } else {
            equal = false;
        }

        return equal;
    }
}
