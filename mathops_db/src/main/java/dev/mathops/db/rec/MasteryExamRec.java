package dev.mathops.db.rec;

import dev.mathops.text.builder.HtmlBuilder;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A "mastery exam" record.
 */
public final class MasteryExamRec extends RecBase implements Comparable<MasteryExamRec> {

    /** The table name. */
    public static final String TABLE_NAME = "mastery_exam";

    /** A field name for serialization of records. */
    private static final String FLD_EXAM_ID = "exam_id";

    /** A field name for serialization of records. */
    private static final String FLD_EXAM_TYPE = "exam_type";

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
    private static final String FLD_BUTTON_LABEL = "button_label";

    /** A field name for serialization of records. */
    private static final String FLD_WHEN_ACTIVE = "when_active";

    /** A field name for serialization of records. */
    private static final String FLD_WHEN_PULLED = "when_pulled";

    /** The 'exam_id' field value. */
    public String examId;

    /** The 'exam_type' field value. */
    public String examType;

    /** The 'course_id' field value. */
    public String courseId;

    /** The 'unit' field value. */
    public Integer unit;

    /** The 'objective' field value. */
    public Integer objective;

    /** The 'tree_ref' field value. */
    public String treeRef;

    /** The 'title' field value. */
    public String title;

    /** The 'button_label' field value. */
    public String buttonLabel;

    /** The 'when_active' field value. */
    public LocalDateTime whenActive;

    /** The 'when_pulled' field value. */
    public LocalDateTime whenPulled;

    /**
     * Constructs a new {@code MasteryExamRec}.
     */
    public MasteryExamRec() {

        super();
    }

    /**
     * Constructs a new {@code MasteryExamRec}.
     *
     * @param theAssignmentId   the version
     * @param theCourseId       the course
     * @param theUnit           the unit
     * @param theObjective      the objective
     * @param theTreeRef        the tree reference
     * @param theTitle          the homework title
     * @param theButtonLabel    the button label
     * @param theAssignmentType the homework type
     * @param theWhenActive     the date the homework was made active
     * @param theWhenPulled     the date the homework was pulled from service
     */
    public MasteryExamRec(final String theAssignmentId, final String theAssignmentType,
                          final String theCourseId, final Integer theUnit, final Integer theObjective,
                          final String theTreeRef, final String theTitle, final String theButtonLabel,
                          final LocalDateTime theWhenActive, final LocalDateTime theWhenPulled) {

        super();

        this.examId = theAssignmentId;
        this.examType = theAssignmentType;
        this.courseId = theCourseId;
        this.unit = theUnit;
        this.objective = theObjective;
        this.treeRef = theTreeRef;
        this.title = theTitle;
        this.buttonLabel = theButtonLabel;
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
    public int compareTo(final MasteryExamRec o) {

        int result = compareAllowingNull(this.courseId, o.courseId);

        if (result == 0) {
            result = compareAllowingNull(this.unit, o.unit);
            if (result == 0) {
                result = compareAllowingNull(this.objective, o.objective);
                if (result == 0) {
                    result = compareAllowingNull(this.examType, o.examType);
                    if (result == 0) {
                        result = compareAllowingNull(this.examId, o.examId);
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

        appendField(htm, FLD_EXAM_ID, this.examId);
        htm.add(DIVIDER);
        appendField(htm, FLD_EXAM_TYPE, this.examType);
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
        appendField(htm, FLD_BUTTON_LABEL, this.buttonLabel);
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

        return Objects.hashCode(this.examId) //
                + Objects.hashCode(this.examType) //
                + Objects.hashCode(this.courseId) //
                + Objects.hashCode(this.unit) //
                + Objects.hashCode(this.objective) //
                + Objects.hashCode(this.treeRef) //
                + Objects.hashCode(this.title) //
                + Objects.hashCode(this.buttonLabel) //
                + Objects.hashCode(this.whenActive) //
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
        } else if (obj instanceof final MasteryExamRec rec) {
            equal = Objects.equals(this.examId, rec.examId)
                    && Objects.equals(this.examType, rec.examType)
                    && Objects.equals(this.courseId, rec.courseId)
                    && Objects.equals(this.unit, rec.unit)
                    && Objects.equals(this.objective, rec.objective)
                    && Objects.equals(this.treeRef, rec.treeRef)
                    && Objects.equals(this.title, rec.title)
                    && Objects.equals(this.buttonLabel, rec.buttonLabel)
                    && Objects.equals(this.whenActive, rec.whenActive)
                    && Objects.equals(this.whenPulled, rec.whenPulled);
        } else {
            equal = false;
        }

        return equal;
    }
}
