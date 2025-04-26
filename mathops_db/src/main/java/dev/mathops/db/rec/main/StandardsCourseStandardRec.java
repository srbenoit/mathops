package dev.mathops.db.rec.main;

import dev.mathops.db.DataDict;
import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

/**
 * An immutable raw "standards course standard" record.
 *
 * <p>
 * Each record represents a standard within a standards-based course.
 *
 * <p>
 * The primary key on the underlying table is the course ID, module number, and standard number.
 */
public final class StandardsCourseStandardRec extends RecBase implements Comparable<StandardsCourseStandardRec> {

    /** The table name for serialization of records. */
    public static final String TABLE_NAME = "standards_course_standard";

    /** The 'course_id' field value. */
    public final String courseId;

    /** The 'module_nbr' field value. */
    public final Integer moduleNbr;

    /** The 'standard_nbr' field value. */
    public final Integer standardNbr;

    /** The 'learning_objective' field value. */
    public final String learningObjective;

    /** The 'is_essential' field value. */
    public final String isEssential;

    /**
     * Constructs a new {@code StandardsCourseStandardRec}.
     *
     * @param theCourseId          the course ID
     * @param theModuleNbr         the module number
     * @param theStandardNbr       the standard number
     * @param theLearningObjective the learning objective, in "I can ..." form.
     * @param theIsEssential       "Y" if the standard is "essential", "N" of not
     */
    public StandardsCourseStandardRec(final String theCourseId, final Integer theModuleNbr,
                                      final Integer theStandardNbr, final String theLearningObjective,
                                      final String theIsEssential) {

        super();

        if (theCourseId == null) {
            throw new IllegalArgumentException("Course ID may not be null");
        }
        if (theModuleNbr == null) {
            throw new IllegalArgumentException("Module number may not be null");
        }
        if (theStandardNbr == null) {
            throw new IllegalArgumentException("Standard number may not be null");
        }
        if (theLearningObjective == null) {
            throw new IllegalArgumentException("Learning objective may not be null");
        }
        if (theIsEssential == null) {
            throw new IllegalArgumentException("Essential flag may not be null");
        }

        this.courseId = theCourseId;
        this.moduleNbr = theModuleNbr;
        this.standardNbr = theStandardNbr;
        this.learningObjective = theLearningObjective;
        this.isEssential = theIsEssential;
    }

    /**
     * Compares two records for order.  Order is based on course ID, then module number.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final StandardsCourseStandardRec o) {

        int result = this.courseId.compareTo(o.courseId);

        if (result == 0) {
            result = this.moduleNbr.compareTo(o.moduleNbr);

            if (result == 0) {
                result = this.standardNbr.compareTo(o.standardNbr);
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

        appendField(htm, DataDict.FLD_COURSE_ID, this.courseId);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_MODULE_NBR, this.moduleNbr);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_STANDARD_NBR, this.standardNbr);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_LEARNING_OBJECTIVE, this.learningObjective);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_IS_ESSENTIAL, this.isEssential);

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
               + this.moduleNbr.hashCode()
               + this.standardNbr.hashCode()
               + this.learningObjective.hashCode()
               + this.isEssential.hashCode();
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
        } else if (obj instanceof final StandardsCourseStandardRec rec) {
            equal = this.courseId.equals(rec.courseId)
                    && this.moduleNbr.equals(rec.moduleNbr)
                    && this.standardNbr.equals(rec.standardNbr)
                    && this.learningObjective.equals(rec.learningObjective)
                    && this.isEssential.equals(rec.isEssential);
        } else {
            equal = false;
        }

        return equal;
    }
}
