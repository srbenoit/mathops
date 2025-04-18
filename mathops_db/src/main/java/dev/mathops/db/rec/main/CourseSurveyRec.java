package dev.mathops.db.rec.main;

import dev.mathops.db.DataDict;
import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.Objects;

/**
 * An immutable raw "course survey" record.
 *
 * <p>
 * Each record represents a survey that can be attached to a course.  Surveys open at specified times in the course
 * (specified as a percentage of the section's course date range complete).  Multiple surveys can be attached to a
 * course.
 *
 * <p>
 * The primary key on the underlying table is the survey ID.
 */
public final class CourseSurveyRec extends RecBase implements Comparable<CourseSurveyRec> {

    /** The table name for serialization of records. */
    public static final String TABLE_NAME = "course_survey";

    /** The 'survey_id' field value. */
    public final String surveyId;

    /** The 'survey_title' field value. */
    public final String surveyTitle;

    /** The 'prompt_html' field value. */
    public final String promptHtml;

    /**
     * Constructs a new {@code CourseSurveyRec}.
     *
     * @param theSurveyId    the survey ID
     * @param theSurveyTitle the survey title
     * @param thePromptHtml  content to display before the first item
     */
    public CourseSurveyRec(final String theSurveyId, final String theSurveyTitle, final String thePromptHtml) {

        super();

        if (theSurveyId == null) {
            throw new IllegalArgumentException("Survey ID may not be null");
        }

        this.surveyId = theSurveyId;
        this.surveyTitle = theSurveyTitle;
        this.promptHtml = thePromptHtml;
    }

    /**
     * Compares two records for order.  Order is based only on survey ID.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final CourseSurveyRec o) {

        return this.surveyId.compareTo(o.surveyId);
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
        appendField(htm, DataDict.FLD_SURVEY_TITLE, this.surveyTitle);
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
               + Objects.hashCode(this.surveyTitle)
               + Objects.hashCode(this.promptHtml);
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
        } else if (obj instanceof final CourseSurveyRec rec) {
            equal = this.surveyId.equals(rec.surveyId)
                    && Objects.equals(this.surveyTitle, rec.surveyTitle)
                    && Objects.equals(this.promptHtml, rec.promptHtml);
        } else {
            equal = false;
        }

        return equal;
    }
}
