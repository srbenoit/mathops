package dev.mathops.db.rec.main;

import dev.mathops.db.DataDict;
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

    /** Integer constant indicating a textbook can be lent to a student in the course. */
    public static final int TEXTBOOK = 0x0001;

    /** Integer constant indicating a calculator (and manual) can be lent to a student in the course. */
    public static final int CALCULATOR = 0x0002;

    /** Integer constant indicating a laptop can be lent to a student in the course. */
    public static final int LAPTOP = 0x0004;

    /** Integer constant indicating headphones can be lent to a student in the course. */
    public static final int HEADPHONES = 0x0008;

    /** The table name for serialization of records. */
    public static final String TABLE_NAME = "standards_course";

    /** The 'course_id' field value. */
    public final String courseId;

    /** The 'course_title' field value. */
    public final String courseTitle;

    /** The 'mbr_modules' field value. */
    public final Integer nbrModules;

    /** The 'room' field value. */
    public final Integer nbrCredits;

    /** The 'allow_lend' field value. */
    public final Integer allowLend;

    /** The 'metadata_path' field value. */
    public final String metadataPath;

    /**
     * Constructs a new {@code StandardsCourseRec}.
     *
     * @param theCourseId     the course ID
     * @param theCourseTitle  the course title
     * @param theNbrModules   the number of modules in the course
     * @param theNbrCredits   the number of credits the course carries
     * @param theAllowLend    a bitwise OR of a set of flags indicating resources we can lend to students in the course
     * @param theMetadataPath for metadata-based courses, the relative path of metadata, like "05_trig/MATH_125.json"
     */
    public StandardsCourseRec(final String theCourseId, final String theCourseTitle, final Integer theNbrModules,
                              final Integer theNbrCredits, final Integer theAllowLend, final String theMetadataPath) {

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
        if (theAllowLend == null) {
            throw new IllegalArgumentException("Allowed lends may not be null");
        }

        this.courseId = theCourseId;
        this.courseTitle = theCourseTitle;
        this.nbrModules = theNbrModules;
        this.nbrCredits = theNbrCredits;
        this.allowLend = theAllowLend;
        this.metadataPath = theMetadataPath;
    }

    /**
     * Compares two records for order.  Order is based on course ID.
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

        appendField(htm, DataDict.FLD_COURSE_ID, this.courseId);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_COURSE_TITLE, this.courseTitle);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_NBR_MODULES, this.nbrModules);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_NBR_CREDITS, this.nbrCredits);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_ALLOW_LEND, this.allowLend);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_METADATA_PATH, this.metadataPath);

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
               + this.allowLend.hashCode()
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
                    && this.allowLend.equals(rec.allowLend)
                    && Objects.equals(this.metadataPath, rec.metadataPath);
        } else {
            equal = false;
        }

        return equal;
    }
}
