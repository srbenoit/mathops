package dev.mathops.db.rec.main;

import dev.mathops.db.DataDict;
import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.Objects;

/**
 * An immutable raw "standards course module" record.
 *
 * <p>
 * Each record represents a module within a standards-based course.
 *
 * <p>
 * The primary key on the underlying table is the course ID and module number.
 */
public final class StandardsCourseModuleRec extends RecBase implements Comparable<StandardsCourseModuleRec> {

    /** The table name for serialization of records. */
    public static final String TABLE_NAME = "standards_course_module";

    /** The 'course_id' field value. */
    public final String courseId;

    /** The 'module_nbr' field value. */
    public final Integer moduleNbr;

    /** The 'nbr_standards' field value. */
    public final Integer nbrStandards;

    /** The 'nbr_essential' field value. */
    public final Integer nbrEssential;

    /** The 'module_path' field value. */
    public final String modulePath;

    /**
     * Constructs a new {@code StandardsCourseModuleRec}.
     *
     * @param theCourseId     the course ID
     * @param theModuleNbr    the module number
     * @param theNbrStandards the number of standards in the module
     * @param theNbrEssential the number of "essential" standards in the module
     * @param theModulePath   the relative path of the module, like "05_trig/01_angles"
     */
    public StandardsCourseModuleRec(final String theCourseId, final Integer theModuleNbr, final Integer theNbrStandards,
                                    final Integer theNbrEssential, final String theModulePath) {

        super();

        if (theCourseId == null) {
            throw new IllegalArgumentException("Course ID may not be null");
        }
        if (theModuleNbr == null) {
            throw new IllegalArgumentException("Module number may not be null");
        }
        if (theNbrStandards == null) {
            throw new IllegalArgumentException("Number of standards may not be null");
        }
        if (theNbrEssential == null) {
            throw new IllegalArgumentException("Number of essential standards may not be null");
        }

        this.courseId = theCourseId;
        this.moduleNbr = theModuleNbr;
        this.nbrStandards = theNbrStandards;
        this.nbrEssential = theNbrEssential;
        this.modulePath = theModulePath;
    }

    /**
     * Compares two records for order.  Order is based on course ID, then module number.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final StandardsCourseModuleRec o) {

        int result = this.courseId.compareTo(o.courseId);

        if (result == 0) {
            result = this.moduleNbr.compareTo(o.moduleNbr);
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
        appendField(htm, DataDict.FLD_NBR_STANDARDS, this.nbrStandards);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_NBR_ESSENTIAL, this.nbrEssential);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_MODULE_PATH, this.modulePath);

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
               + this.nbrStandards.hashCode()
               + this.nbrEssential.hashCode()
               + Objects.hashCode(this.modulePath);
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
        } else if (obj instanceof final StandardsCourseModuleRec rec) {
            equal = this.courseId.equals(rec.courseId)
                    && this.moduleNbr.equals(rec.moduleNbr)
                    && this.nbrStandards.equals(rec.nbrStandards)
                    && this.nbrEssential.equals(rec.nbrEssential)
                    && Objects.equals(this.modulePath, rec.modulePath);
        } else {
            equal = false;
        }

        return equal;
    }
}
