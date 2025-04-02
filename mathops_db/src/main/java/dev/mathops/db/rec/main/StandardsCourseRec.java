package dev.mathops.db.rec.main;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.Objects;

/**
 * An immutable raw "standards course" record.
 *
 * <p>
 * Each record represents a standards-based course.
 *
 * <p>
 * The primary key on the underlying table is the course ID.
 */
public final class StandardsCourseRec extends RecBase implements Comparable<StandardsCourseRec> {

    /** The table name for serialization of records. */
    public static final String TABLE_NAME = "standards_course";

    /** A field name for serialization of records. */
    private static final String FLD_COURSE_ID = "course_id";

    /** A field name for serialization of records. */
    private static final String FLD_COURSE_TITLE = "course_title";

    /** A field name for serialization of records. */
    private static final String FLD_NBR_MODULES = "nbr_modules";

    /** A field name for serialization of records. */
    private static final String FLD_NBR_CREDITS = "nbr_credits";

    /** A field name for serialization of records. */
    private static final String FLD_METADATA_PATH = "metadata_path";

    /** The 'course_id' field value. */
    public final String courseId;

    /** The 'course_title' field value. */
    public final String courseTitle;

    /** The 'mbr_modules' field value. */
    public final Integer nbrModules;

    /** The 'room' field value. */
    public final Integer nbrCredits;

    /** The 'metadata_path' field value. */
    public final String metadataPath;

    /**
     * Constructs a new {@code StandardsCourseRec}.
     *
     * @param theCourseId     the course ID
     * @param theCourseTitle  the course title
     * @param theNbrModules   the number of modules in the course
     * @param theNbrCredits   the number of credits the course carries
     * @param theMetadataPath for metadata-based courses, the relative path of metadata, like "05_trig/MATH_125.json"
     */
    public StandardsCourseRec(final String theCourseId, final String theCourseTitle, final Integer theNbrModules,
                              final Integer theNbrCredits, final String theMetadataPath) {

        super();

        if (theCourseId == null) {
            throw new IllegalArgumentException("Course ID may not be null");
        }
        if (theCourseTitle == null) {
            throw new IllegalArgumentException("Course title may not be null");
        }
        if (theNbrModules == null) {
            throw new IllegalArgumentException("Number of modules may not be null");
        }
        if (theNbrCredits == null) {
            throw new IllegalArgumentException("Number of credits may not be null");
        }

        this.courseId = theCourseId;
        this.courseTitle = theCourseTitle;
        this.nbrModules = theNbrModules;
        this.nbrCredits = theNbrCredits;
        this.metadataPath = theMetadataPath;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final StandardsCourseRec o) {

        return this.courseId.compareTo(o.courseId);
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

        appendField(htm, FLD_COURSE_ID, this.courseId);
        htm.add(DIVIDER);
        appendField(htm, FLD_COURSE_TITLE, this.courseTitle);
        htm.add(DIVIDER);
        appendField(htm, FLD_NBR_MODULES, this.nbrModules);
        htm.add(DIVIDER);
        appendField(htm, FLD_NBR_CREDITS, this.nbrCredits);
        htm.add(DIVIDER);
        appendField(htm, FLD_METADATA_PATH, this.metadataPath);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return this.courseId.hashCode()
               + this.courseTitle.hashCode()
               + this.nbrModules.hashCode()
               + this.nbrCredits.hashCode()
               + Objects.hashCode(this.metadataPath);
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
        } else if (obj instanceof final StandardsCourseRec rec) {
            equal = this.courseId.equals(rec.courseId)
                    && this.courseTitle.equals(rec.courseTitle)
                    && this.nbrModules.equals(rec.nbrModules)
                    && this.nbrCredits.equals(rec.nbrCredits)
                    && Objects.equals(this.metadataPath, rec.metadataPath);
        } else {
            equal = false;
        }

        return equal;
    }
}
