package dev.mathops.db.old.rec;

import dev.mathops.db.enums.ECatalogCoursePrereqType;
import dev.mathops.db.type.CatalogCourseNumber;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.Objects;

/**
 * A prerequisite associated with a catalog course.  Logically, a prerequisite is a tree structure that may have AND
 * plus OR subtrees.  In the database, each record represents a node in this tree.  A node has a type, which can be
 * "Simple", meaning this is a leaf node defining a course, with possible minimum grade and flag specifying if it can
 * be taken concurrently, or "And" or "Or", which each define a branch node with child nodes.
 *
 * <p>
 * Each record has a unique ID, and child nodes have a parent node ID to create the tree structure.
 */
public final class CatalogCoursePrereqRec extends RecBase {

    /** A field name for serialization of records. */
    private static final String FLD_PREREQ_ID = "prereq_id";

    /** A field name for serialization of records. */
    private static final String FLD_PARENT_PREREQ_ID = "parent_prereq_id";

    /** A field name for serialization of records. */
    private static final String FLD_PREREQ_TYPE = "prereq_type";

    /** A field name for serialization of records. */
    private static final String FLD_COURSE_ID = "course_id";

    /** A field name for serialization of records. */
    private static final String FLD_MIN_GRADE = "min_grade";

    /** A field name for serialization of records. */
    private static final String FLD_CONCURRENT = "concurrent";

    /** The prerequisite ID field. */
    private String prereqId;

    /** The parent prerequisite ID field (null in top-level nodes). */
    private String parentPrereqId;

    /** The prerequisite type. */
    private ECatalogCoursePrereqType prereqType;

    /** The course number field value, such as "MATH 126" . */
    private CatalogCourseNumber courseId;

    /** The minimum grade needed. */
    private String minGrade;

    /** "Y" if the course can be taken concurrently; "N" if not. */
    private String concurrent;

    /**
     * Constructs a new {@code CatalogCoursePrerequisiteRec}.
     */
    private CatalogCoursePrereqRec() {

        super();
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

        appendField(htm, FLD_PREREQ_ID, this.prereqId);
        htm.add(DIVIDER);
        appendField(htm, FLD_PARENT_PREREQ_ID, this.parentPrereqId);
        htm.add(DIVIDER);
        appendField(htm, FLD_PREREQ_TYPE, this.prereqType);
        htm.add(DIVIDER);
        appendField(htm, FLD_COURSE_ID, this.courseId);
        htm.add(DIVIDER);
        appendField(htm, FLD_MIN_GRADE, this.minGrade);
        htm.add(DIVIDER);
        appendField(htm, FLD_CONCURRENT, this.concurrent);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.prereqId)
                + Objects.hashCode(this.parentPrereqId)
                + Objects.hashCode(this.prereqType)
                + Objects.hashCode(this.courseId)
                + Objects.hashCode(this.minGrade)
                + Objects.hashCode(this.concurrent);
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
        } else if (obj instanceof final CatalogCoursePrereqRec rec) {
            equal = Objects.equals(this.prereqId, rec.prereqId)
                    && Objects.equals(this.parentPrereqId, rec.parentPrereqId)
                    && this.prereqType == rec.prereqType
                    && Objects.equals(this.courseId, rec.courseId)
                    && Objects.equals(this.minGrade, rec.minGrade)
                    && Objects.equals(this.concurrent, rec.concurrent);
        } else {
            equal = false;
        }

        return equal;
    }
}
