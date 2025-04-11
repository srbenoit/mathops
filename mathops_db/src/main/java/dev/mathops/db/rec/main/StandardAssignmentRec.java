package dev.mathops.db.rec.main;

import dev.mathops.db.DataDict;
import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.Objects;

/**
 * An immutable raw "standard assignment" record.
 *
 * <p>
 * Each record represents an assignment associated with a standard in a module of a standards-based course.
 *
 * <p>
 * The primary key on the underlying table is the assignment ID.
 */
public final class StandardAssignmentRec extends RecBase implements Comparable<StandardAssignmentRec> {

    /** An assignment type string. */
    public static final String HOMEWORK = "HW";

    /** An assignment type string. */
    public static final String MASTERY_EXAM = "MA";

    /** The table name for serialization of records. */
    public static final String TABLE_NAME = "standard_assignment";

    /** The 'assignment_id' field value. */
    public final String assignmentId;

    /** The 'assignment_type' field value. */
    public final String assignmentType;

    /** The 'course_id' field value. */
    public final String courseId;

    /** The 'module_nbr' field value. */
    public final Integer moduleNbr;

    /** The 'standard_nbr' field value. */
    public final Integer standardNbr;

    /** The 'pts_possible' field value. */
    public final Integer ptsPossible;

    /** The 'min_passing_score' field value. */
    public final Integer minPassingScore;

    /** The 'tree_ref' field value. */
    public final String treeRef;

    /**
     * Constructs a new {@code StandardAssignmentRec}.
     *
     * @param theAssignmentId    the assignment ID
     * @param theAssignmentType  the assignment type
     * @param theCourseId        the course ID
     * @param theModuleNbr       the module number
     * @param theStandardNbr     the standard number
     * @param thePtsPossible     the number of points possible on the assignment
     * @param theMinPassingScore the minimum passing score
     * @param theTreeRef         the tree reference used to look up the assignment
     */
    public StandardAssignmentRec(final String theAssignmentId, final String theAssignmentType, final String theCourseId,
                                 final Integer theModuleNbr, final Integer theStandardNbr, final Integer thePtsPossible,
                                 final Integer theMinPassingScore, final String theTreeRef) {

        super();

        if (theAssignmentId == null) {
            throw new IllegalArgumentException("Assignment ID may not be null");
        }
        if (theAssignmentType == null) {
            throw new IllegalArgumentException("Assignment type may not be null");
        }
        if (theCourseId == null) {
            throw new IllegalArgumentException("Course ID may not be null");
        }
        if (theModuleNbr == null) {
            throw new IllegalArgumentException("Module number may not be null");
        }
        if (theStandardNbr == null) {
            throw new IllegalArgumentException("Standard number may not be null");
        }
        if (theTreeRef == null) {
            throw new IllegalArgumentException("Tree reference may not be null");
        }

        this.assignmentId = theAssignmentId;
        this.assignmentType = theAssignmentType;
        this.courseId = theCourseId;
        this.moduleNbr = theModuleNbr;
        this.standardNbr = theStandardNbr;
        this.ptsPossible = thePtsPossible;
        this.minPassingScore = theMinPassingScore;
        this.treeRef = theTreeRef;
    }

    /**
     * Compares two records for order.  Order is based on assignment ID.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final StandardAssignmentRec o) {

        return this.assignmentId.compareTo(o.assignmentId);
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

        appendField(htm, DataDict.FLD_ASSIGNMENT_ID, this.assignmentId);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_ASSIGNMENT_TYPE, this.assignmentType);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_COURSE_ID, this.courseId);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_MODULE_NBR, this.moduleNbr);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_STANDARD_NBR, this.standardNbr);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_PTS_POSSIBLE, this.ptsPossible);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_MIN_PASSING_SCORE, this.minPassingScore);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_TREE_REF, this.treeRef);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return this.assignmentId.hashCode()
               + this.assignmentType.hashCode()
               + this.courseId.hashCode()
               + this.moduleNbr.hashCode()
               + this.standardNbr.hashCode()
               + Objects.hashCode(this.ptsPossible)
               + Objects.hashCode(this.minPassingScore)
               + this.treeRef.hashCode();
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
        } else if (obj instanceof final StandardAssignmentRec rec) {
            equal = this.assignmentId.equals(rec.assignmentId)
                    && this.assignmentType.equals(rec.assignmentType)
                    && this.courseId.equals(rec.courseId)
                    && this.moduleNbr.equals(rec.moduleNbr)
                    && this.standardNbr.equals(rec.standardNbr)
                    && Objects.equals(this.ptsPossible, rec.ptsPossible)
                    && Objects.equals(this.minPassingScore, rec.minPassingScore)
                    && this.treeRef.equals(rec.treeRef);
        } else {
            equal = false;
        }

        return equal;
    }
}
