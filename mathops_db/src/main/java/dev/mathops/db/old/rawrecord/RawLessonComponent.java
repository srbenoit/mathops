package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.Objects;

/**
 * An immutable record of an association of a component with a lesson.
 */
public final class RawLessonComponent extends RecBase implements Comparable<RawLessonComponent> {

    /** A field name. */
    private static final String FLD_LESSON_ID = "lesson_id";

    /** A field name. */
    private static final String FLD_SEQ_NBR = "seq_nbr";

    /** A field name. */
    private static final String FLD_TYPE = "type";

    /** A field name. */
    private static final String FLD_XML_DATA = "xml_data";

    /** The lesson ID. */
    public String lessonId;

    /** The sequence number. */
    public Integer seqNbr;

    /** The component type. */
    public String type;

    /** The XML data. */
    public String xmlData;

    /**
     * Constructs a new {@code RawLessonComponent}.
     */
    private RawLessonComponent() {

        super();
    }

    /**
     * Constructs a new {@code RawLessonComponent}.
     *
     * @param theLessonId       the lesson ID
     * @param theSequenceNumber the sequence number
     * @param theType           the component type
     * @param theXmlData        the XML data
     */
    public RawLessonComponent(final String theLessonId, final Integer theSequenceNumber,
                              final String theType, final String theXmlData) {

        super();

        this.lessonId = theLessonId;
        this.seqNbr = theSequenceNumber;
        this.type = theType;
        this.xmlData = theXmlData;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final RawLessonComponent o) {

        int result = this.lessonId.compareTo(o.lessonId);

        if (result == 0) {
            result = this.seqNbr.compareTo(o.seqNbr);
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

        appendField(htm, FLD_LESSON_ID, this.lessonId);
        htm.add(DIVIDER);
        appendField(htm, FLD_SEQ_NBR, this.seqNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_TYPE, this.type);
        htm.add(DIVIDER);
        appendField(htm, FLD_XML_DATA, this.xmlData);

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
                + Objects.hashCode(this.seqNbr)
                + Objects.hashCode(this.type)
                + Objects.hashCode(this.xmlData);
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
        } else if (obj instanceof final RawLessonComponent rec) {
            equal = Objects.equals(this.lessonId, rec.lessonId)
                    && Objects.equals(this.seqNbr, rec.seqNbr)
                    && Objects.equals(this.type, rec.type)
                    && Objects.equals(this.xmlData, rec.xmlData);
        } else {
            equal = false;
        }

        return equal;
    }
}
