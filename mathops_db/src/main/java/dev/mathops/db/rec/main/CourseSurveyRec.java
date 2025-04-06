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

    /** The 'open_week' field value. */
    public final Integer openWeek;

    /** The 'open_day' field value. */
    public final Integer openDay;

    /** The 'close_week' field value. */
    public final Integer closeWeek;

    /** The 'close_day' field value. */
    public final Integer closeDay;

    /**
     * Constructs a new {@code CourseSurveyRec}.
     *
     * @param theSurveyId  the survey ID
     * @param theOpenWeek  the percentage of the course term when the survey opens
     * @param theCloseWeek the percentage of the course term when the survey closes
     */
    public CourseSurveyRec(final String theSurveyId, final Integer theOpenWeek, final Integer theOpenDay,
                           final Integer theCloseWeek, final Integer theCloseDay) {

        super();

        if (theSurveyId == null) {
            throw new IllegalArgumentException("Survey ID may not be null");
        }

        this.surveyId = theSurveyId;
        this.openWeek = theOpenWeek;
        this.openDay = theOpenDay;
        this.closeWeek = theCloseWeek;
        this.closeDay = theCloseDay;
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
        appendField(htm, DataDict.FLD_OPEN_WEEK, this.openWeek);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_OPEN_DAY, this.openDay);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_CLOSE_WEEK, this.closeWeek);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_CLOSE_DAY, this.closeDay);

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
               + Objects.hashCode(this.openWeek)
               + Objects.hashCode(this.openDay)
               + Objects.hashCode(this.closeWeek)
               + Objects.hashCode(this.closeDay);
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
                    && Objects.equals(this.openWeek, rec.openWeek)
                    && Objects.equals(this.openDay, rec.openDay)
                    && Objects.equals(this.closeWeek, rec.closeWeek)
                    && Objects.equals(this.closeDay, rec.closeDay);
        } else {
            equal = false;
        }

        return equal;
    }
}
