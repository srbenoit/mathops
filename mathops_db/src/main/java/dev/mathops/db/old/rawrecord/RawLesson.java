package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.Objects;

/**
 * An immutable record of a lesson.
 */
public final class RawLesson extends RecBase implements Comparable<RawLesson> {

    /** A field name. */
    private static final String FLD_LESSON_ID = "lesson_id";

    /** A field name. */
    private static final String FLD_LESSON_TYPE = "lesson_type";

    /** A field name. */
    private static final String FLD_DESCR = "descr";

    /** The lesson ID. */
    public String lessonId;

    /** The lesson type ("C" for course, "R" for refresher). */
    public String lessonType;

    /** The description. */
    public String descr;

    /**
     * Constructs a new {@code RawLesson}.
     */
    private RawLesson() {

        super();
    }

    /**
     * Constructs a new {@code RawLesson}.
     *
     * @param theLessonId    the lesson ID
     * @param theLessonType  the lesson type
     * @param theDescription the description
     */
    public RawLesson(final String theLessonId, final String theLessonType,
                     final String theDescription) {

        super();

        this.lessonId = theLessonId;
        this.lessonType = theLessonType;
        this.descr = theDescription;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final RawLesson o) {

        return this.lessonId.compareTo(o.lessonId);
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

        appendField(htm, FLD_LESSON_ID, this.lessonId);
        htm.add(DIVIDER);
        appendField(htm, FLD_LESSON_TYPE, this.lessonType);
        htm.add(DIVIDER);
        appendField(htm, FLD_DESCR, this.descr);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.lessonId)
                + Objects.hashCode(this.lessonType)
                + Objects.hashCode(this.descr);
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
        } else if (obj instanceof final RawLesson rec) {
            equal = Objects.equals(this.lessonId, rec.lessonId)
                    && Objects.equals(this.lessonType, rec.lessonType)
                    && Objects.equals(this.descr, rec.descr);
        } else {
            equal = false;
        }

        return equal;
    }
}
