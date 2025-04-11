package dev.mathops.db.rec.term;

import dev.mathops.db.DataDict;
import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * An immutable raw "course survey response" record.
 *
 * <p>
 * Each record represents a student's response to a course survey (which may include several item responses).
 *
 * <p>
 * The primary key on the underlying table is the serial number of the response.
 */
public final class CourseSurveyResponseRec extends RecBase implements Comparable<CourseSurveyResponseRec> {

    /** The table name. */
    public static final String TABLE_NAME = "course_survey_response";

    /** The 'serial_nbr' field value. */
    public final Integer serialNbr;

    /** The 'survey_id' field value. */
    public final String surveyId;

    /** The 'student_id' field value. */
    public final String studentId;

    /** The 'response_date' field value. */
    public final LocalDate responseDate;

    /** The 'response_time' field value. */
    public final LocalTime responseTime;

    /**
     * Constructs a new {@code CourseSurveyResponse}.
     *
     * @param theSerialNbr    the student ID
     * @param theSurveyId     the survey ID
     * @param theStudentId    the student ID
     * @param theResponseDate the response date
     * @param theResponseTime the response time
     */
    public CourseSurveyResponseRec(final Integer theSerialNbr, final String theSurveyId, final String theStudentId,
                                   final LocalDate theResponseDate, final LocalTime theResponseTime) {

        super();

        if (theSerialNbr == null) {
            throw new IllegalArgumentException("Serial number may not be null");
        }
        if (theSurveyId == null) {
            throw new IllegalArgumentException("Survey ID may not be null");
        }
        if (theStudentId == null) {
            throw new IllegalArgumentException("Student ID may not be null");
        }
        if (theResponseDate == null) {
            throw new IllegalArgumentException("Response date may not be null");
        }
        if (theResponseTime == null) {
            throw new IllegalArgumentException("Response time may not be null");
        }

        this.serialNbr = theSerialNbr;
        this.surveyId = theSurveyId;
        this.studentId = theStudentId;
        this.responseDate = theResponseDate;
        this.responseTime = theResponseTime;
    }

    /**
     * Compares two records for order.  Order is based on serial number.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final CourseSurveyResponseRec o) {

        return compareAllowingNull(this.serialNbr, o.serialNbr);
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
        appendField(htm, DataDict.FLD_SURVEY_ID, this.surveyId);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_STUDENT_ID, this.studentId);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_RESPONSE_DATE, this.responseDate);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_RESPONSE_TIME, this.responseTime);

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
               + this.surveyId.hashCode()
               + this.studentId.hashCode()
               + this.responseDate.hashCode()
               + this.responseTime.hashCode();
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
        } else if (obj instanceof final CourseSurveyResponseRec rec) {
            equal = this.serialNbr.equals(rec.serialNbr)
                    && this.surveyId.equals(rec.surveyId)
                    && this.studentId.equals(rec.studentId)
                    && this.responseDate.equals(rec.responseDate)
                    && this.responseTime.equals(rec.responseTime);
        } else {
            equal = false;
        }

        return equal;
    }
}
