package dev.mathops.db.rec;

import dev.mathops.text.builder.HtmlBuilder;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * An immutable raw "assignment" record.
 */
public final class AssignmentRec extends RecBase implements Comparable<AssignmentRec> {

    /** A field name for serialization of records. */
    public static final String TABLE_NAME = "homework";

    /** A field name for serialization of records. */
    private static final String FLD_ASSIGNMENT_ID = "assignment_id";

    /** A field name for serialization of records. */
    private static final String FLD_ASSIGNMENT_TYPE = "assignment_type";

    /** A field name for serialization of records. */
    private static final String FLD_COURSE_ID = "course_id";

    /** A field name for serialization of records. */
    private static final String FLD_UNIT = "unit";

    /** A field name for serialization of records. */
    private static final String FLD_OBJECTIVE = "objective";

    /** A field name for serialization of records. */
    private static final String FLD_TREE_REF = "tree_ref";

    /** A field name for serialization of records. */
    private static final String FLD_TITLE = "title";

    /** A field name for serialization of records. */
    private static final String FLD_WHEN_ACTIVE = "when_active";

    /** A field name for serialization of records. */
    private static final String FLD_WHEN_PULLED = "when_pulled";

    /** The 'assignment_id' field value. */
    public final String assignmentId;

    /** The 'assignment_type' field value. */
    public final String assignmentType;

    /** The 'course_id' field value. */
    public final String courseId;

    /** The 'unit' field value. */
    public final Integer unit;

    /** The 'objective' field value. */
    public final Integer objective;

    /** The 'tree_ref' field value. */
    public final String treeRef;

    /** The 'title' field value. */
    public final String title;

    /** The 'when_active' field value. */
    public final LocalDateTime whenActive;

    /** The 'when_pulled' field value. */
    public final LocalDateTime whenPulled;

    /**
     * Constructs a new {@code AssignmentRec}.
     *
     * @param theAssignmentId   the assignment ID
     * @param theCourseId       the course ID
     * @param theUnit           the unit
     * @param theObjective      the objective
     * @param theTitle          the assignment title
     * @param theTreeRef        the tree reference
     * @param theAssignmentType the assignment type
     * @param theWhenActive     the date the assignment was made active
     * @param theWhenPulled     the date the assignment was pulled from service
     */
    public AssignmentRec(final String theAssignmentId, final String theAssignmentType,
                         final String theCourseId, final Integer theUnit, final Integer theObjective,
                         final String theTreeRef, final String theTitle, final LocalDateTime theWhenActive,
                         final LocalDateTime theWhenPulled) {

        super();

        this.assignmentId = theAssignmentId;
        this.assignmentType = theAssignmentType;
        this.courseId = theCourseId;
        this.unit = theUnit;
        this.objective = theObjective;
        this.treeRef = theTreeRef;
        this.title = theTitle;
        this.whenActive = theWhenActive;
        this.whenPulled = theWhenPulled;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final AssignmentRec o) {

        int result = compareAllowingNull(this.courseId, o.courseId);

        if (result == 0) {
            result = compareAllowingNull(this.unit, o.unit);
            if (result == 0) {
                result = compareAllowingNull(this.objective, o.objective);
                if (result == 0) {
                    result = compareAllowingNull(this.assignmentType, o.assignmentType);
                    if (result == 0) {
                        result = compareAllowingNull(this.assignmentId, o.assignmentId);
                    }
                }
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

        appendField(htm, FLD_ASSIGNMENT_ID, this.assignmentId);
        htm.add(DIVIDER);
        appendField(htm, FLD_ASSIGNMENT_TYPE, this.assignmentType);
        htm.add(DIVIDER);
        appendField(htm, FLD_COURSE_ID, this.courseId);
        htm.add(DIVIDER);
        appendField(htm, FLD_UNIT, this.unit);
        htm.add(DIVIDER);
        appendField(htm, FLD_OBJECTIVE, this.objective);
        htm.add(DIVIDER);
        appendField(htm, FLD_TREE_REF, this.treeRef);
        htm.add(DIVIDER);
        appendField(htm, FLD_TITLE, this.title);
        htm.add(DIVIDER);
        appendField(htm, FLD_WHEN_ACTIVE, this.whenActive);
        htm.add(DIVIDER);
        appendField(htm, FLD_WHEN_PULLED, this.whenPulled);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.assignmentId)
               + Objects.hashCode(this.assignmentType)
               + Objects.hashCode(this.courseId)
               + Objects.hashCode(this.unit)
               + Objects.hashCode(this.objective)
               + Objects.hashCode(this.treeRef)
               + Objects.hashCode(this.title)
               + Objects.hashCode(this.whenActive)
               + Objects.hashCode(this.whenPulled);
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
        } else if (obj instanceof final AssignmentRec rec) {
            equal = Objects.equals(this.assignmentId, rec.assignmentId)
                    && Objects.equals(this.assignmentType, rec.assignmentType)
                    && Objects.equals(this.courseId, rec.courseId)
                    && Objects.equals(this.unit, rec.unit)
                    && Objects.equals(this.objective, rec.objective)
                    && Objects.equals(this.treeRef, rec.treeRef)
                    && Objects.equals(this.title, rec.title)
                    && Objects.equals(this.whenActive, rec.whenActive)
                    && Objects.equals(this.whenPulled, rec.whenPulled);
        } else {
            equal = false;
        }

        return equal;
    }
}
