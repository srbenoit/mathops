package dev.mathops.app.catalog;

import dev.mathops.db.type.CatalogCourseNumber;

import java.util.Arrays;
import java.util.List;

/**
 * A node in a prerequisite tree in which the student must complete some minimum number of credits from a list of
 * courses, possibly with a minimum grade requirement.
 */
final class CreditCountPrerequisiteNode extends AbstractPrerequisiteNode {

    /** The minimum number of credits. */
    private final int minCredits;

    /** Course minimum grade. */
    private final String minGrade;

    /** Course number. */
    private final List<CatalogCourseNumber> courses;

    /**
     * Constructs a new {@code CreditCountPrerequisiteNode}.
     *
     * @param theMinCredits the minimum number of credits
     * @param theMinGrade   the optional minimum grade (null if none)
     * @param theCourses    the set of courses from which to select
     * @throws IllegalArgumentException if {@code theChildren} is null or empty
     */
    CreditCountPrerequisiteNode(final int theMinCredits, final String theMinGrade,
                                final CatalogCourseNumber... theCourses) {

        super();

        if (theCourses == null || theCourses.length == 0) {
            throw new IllegalArgumentException("Course list may not be null or empty");
        }

        this.minCredits = theMinCredits;
        this.minGrade = theMinGrade;
        this.courses = Arrays.asList(theCourses);
    }

    /**
     * Gets the minimum number of credits required.
     *
     * @return the minimum number of credits
     */
    public int getMinCredits() {

        return this.minCredits;
    }

    /**
     * Gets the minimum grade required.
     *
     * @return the minimum grade
     */
    public String getMinGrade() {

        return this.minGrade;
    }

    /**
     * Gets the number of courses in this option.
     *
     * @return the number of courses
     */
    public int getNumChildren() {

        return this.courses.size();
    }

    /**
     * Gets a particular course.
     *
     * @param index the 0-based index
     * @return the course
     */
    public CatalogCourseNumber getChild(final int index) {

        return this.courses.get(index);
    }
}
