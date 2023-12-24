package dev.mathops.app.catalog;

import dev.mathops.core.EqualityTests;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.db.type.CatalogCourseNumber;

import java.util.Objects;

/**
 * A simple (leaf) node in a prerequisite tree.
 */
final class SimplePrerequisiteNode extends AbstractPrerequisiteNode {

    /** Course number. */
    private final CatalogCourseNumber courseNumber;

    /** Course minimum grade. */
    private final String minGrade;

    /** True if the course can be taken concurrently. */
    private final boolean concurrentAllowed;

    /**
     * Constructs a new {@code SimplePrerequisiteNode} with no minimum grade, that cannot be taken concurrently.
     *
     * @param theCourseNumber the course number, like "MATH 126"
     */
    SimplePrerequisiteNode(final CatalogCourseNumber theCourseNumber) {

        super();

        if (theCourseNumber == null) {
            throw new IllegalArgumentException("Course number may not be null");
        }

        this.courseNumber = theCourseNumber;
        this.minGrade = null;
        this.concurrentAllowed = false;
    }

    /**
     * Constructs a new {@code SimplePrerequisiteNode} with a minimum grade requirement.
     *
     * @param theCourseNumber the course prefix, like "MATH 126"
     * @param theMinGrade     the minimum grade, like "B"
     */
    SimplePrerequisiteNode(final CatalogCourseNumber theCourseNumber, final String theMinGrade) {

        super();

        if (theCourseNumber == null) {
            throw new IllegalArgumentException("Course number may not be null");
        }

        this.courseNumber = theCourseNumber;
        this.minGrade = theMinGrade;
        this.concurrentAllowed = false;
    }

    /**
     * Constructs a new {@code SimplePrerequisiteNode} that can be taken concurrently.  Such a course should not have a
     * minimum grade requirement.
     *
     * @param theCourseNumber     the course prefix, like "MATH 126"
     * @param isConcurrentAllowed true if the course can be taken concurrently
     */
    SimplePrerequisiteNode(final CatalogCourseNumber theCourseNumber, final boolean isConcurrentAllowed) {

        super();

        if (theCourseNumber == null) {
            throw new IllegalArgumentException("Course number may not be null");
        }

        this.courseNumber = theCourseNumber;
        this.minGrade = null;
        this.concurrentAllowed = isConcurrentAllowed;
    }

    /**
     * Constructs a new {@code SimplePrerequisiteNode} with a minimum grade that can be taken concurrently.
     *
     * @param theCourseNumber     the course prefix, like "MATH 126"
     * @param theMinGrade         the minimum grade, like "B"
     * @param isConcurrentAllowed true if the course can be taken concurrently
     */
    SimplePrerequisiteNode(final CatalogCourseNumber theCourseNumber, final String theMinGrade,
                           final boolean isConcurrentAllowed) {

        super();

        if (theCourseNumber == null) {
            throw new IllegalArgumentException("Course number may not be null");
        }

        this.courseNumber = theCourseNumber;
        this.minGrade = theMinGrade;
        this.concurrentAllowed = isConcurrentAllowed;
    }

    /**
     * Generates a string serialization of the record. Each concrete subclass should have a constructor that accepts a
     * single {@code String} to reconstruct the object from this string.
     *
     * @return the string
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(50);

        htm.add(this.courseNumber);

        if (this.minGrade != null) {
            htm.add(" with min grade ", this.minGrade);
        }

        if (this.concurrentAllowed) {
            htm.add(", concurrent allowed");
        }

        htm.add('.');

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return this.courseNumber.hashCode() + Objects.hashCode(this.minGrade)
                + Boolean.hashCode(this.concurrentAllowed);
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
        } else if (obj instanceof final SimplePrerequisiteNode rec) {
            equal = this.concurrentAllowed == rec.concurrentAllowed
                    && this.courseNumber.equals(rec.courseNumber)
                    && Objects.equals(this.minGrade, rec.minGrade);
        } else {
            equal = false;
        }

        return equal;
    }
}

