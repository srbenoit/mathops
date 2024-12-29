package dev.mathops.db.old.logic.mathplan.data;

import dev.mathops.commons.log.Log;
import dev.mathops.db.old.rawrecord.RawCourse;
import dev.mathops.text.builder.HtmlBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * An immutable record of a list of courses from which a student may select one course, or some number of credits.
 *
 * <p>
 * Some "course groups" consist of single courses. This allows us to treat all requirements in a major as a collection
 * of course groups, where the student must satisfy every course group, but may select one course or some number of
 * credits from each group.
 *
 * <p>
 * Course groups have an ordering based on the lowest course number required to satisfy the group. If two groups have
 * the same lowest course number, the group with the longer set of options is considered "less than" the group with
 * fewer options.
 */
public final class CourseGroup implements Serializable, Comparable<CourseGroup> {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 5219650494634655012L;

    /** The group code. */
    final String groupCode;

    /** The number of credits needed, if multiple courses can be selected (null if not). */
    final Integer nbrCredits;

    /**
     * If the lowest possible course numbers are chosen from the list of options, this is the highest course number from
     * that "easiest" list.
     */
    final String lowestLastCourse;

    /** The course numbers, sorted. */
    final List<String> courseNumbers;

    /**
     * Constructs a new {@code CourseGroup}.
     *
     * @param theGroupCode        the group code
     * @param theNumCredits       the number of credits needed, if multiple courses can be selected (null if not)
     * @param theLowestLastCourse if the lowest possible course numbers are chosen from the list of options, this is the
     *                            highest course number from that "easiest" list
     * @param theCourseNumbers    the course numbers
     */
    public CourseGroup(final String theGroupCode, final Integer theNumCredits,
                       final String theLowestLastCourse, final String... theCourseNumbers) {

        if (theGroupCode == null) {
            throw new IllegalArgumentException("Group code may not be null");
        }
        if (theLowestLastCourse == null) {
            throw new IllegalArgumentException("Lowest last course may not be null");
        }

        this.groupCode = theGroupCode;
        this.nbrCredits = theNumCredits;
        this.lowestLastCourse = theLowestLastCourse;

        if (theCourseNumbers == null) {
            this.courseNumbers = new ArrayList<>(0);
        } else {
            this.courseNumbers = new ArrayList<>(theCourseNumbers.length);
            final List<String> numbers = Arrays.asList(theCourseNumbers);
            this.courseNumbers.addAll(numbers);
        }
    }

    /**
     * Constructs a new {@code CourseGroup}.
     *
     * @param theGroupCode        the group code
     * @param theNumCredits       the number of credits needed, if multiple courses can be selected (null if not)
     * @param theLowestLastCourse if the lowest possible course numbers are chosen from the list of options, this is the
     *                            highest course number from that "easiest" list
     * @param theCourseNumbers    the course numbers
     */
    CourseGroup(final String theGroupCode, final Integer theNumCredits,
                final String theLowestLastCourse, final Collection<String> theCourseNumbers) {

        if (theGroupCode == null) {
            throw new IllegalArgumentException("Group code may not be null");
        }
        if (theLowestLastCourse == null) {
            throw new IllegalArgumentException("Lowest last course may not be null");
        }

        this.groupCode = theGroupCode;
        this.nbrCredits = theNumCredits;
        this.lowestLastCourse = theLowestLastCourse;

        if (theCourseNumbers == null) {
            this.courseNumbers = new ArrayList<>(0);
        } else {
            this.courseNumbers = new ArrayList<>(theCourseNumbers);
        }
    }

    /**
     * Tests whether a set of courses satisfies the requirements of this group.
     *
     * @param courses    the list of courses
     * @param courseData a map from course ID to course data - used to get number of credits for each course
     * @return {@code true} if the course set satisfies this group
     */
    boolean isSatisfiedBy(final Collection<String> courses, final Map<String, RawCourse> courseData) {

        boolean satisfied = false;

        if (this.nbrCredits == null) {
            // Satisfied if any course in "courseNumbers" is also in "courses"
            for (final String test : this.courseNumbers) {
                if (courses.contains(test)) {
                    satisfied = true;
                    break;
                }
            }
        } else {
            // Add up the credits of all matching courses
            int total = 0;
            for (final String test : this.courseNumbers) {
                if (courses.contains(test)) {
                    final RawCourse data = courseData.get(test);
                    if (data != null) {
                        total += data.nbrCredits.intValue();
                    }
                }
            }
            satisfied = total >= this.nbrCredits.intValue();
        }

        return satisfied;
    }

    /**
     * Tests whether satisfying one group will automatically satisfy another. Only groups that require the choice of one
     * course from a list are considered. This method only returns {@code true} of choosing ANY course from such a group
     * will satisfy all requirements of this group.
     *
     * @param groups     the set of course groups (which may contain this group, in which case that item will be
     *                   ignored)
     * @param courseData a map from course ID to course data - used to get number of credits for each course
     * @return {@code true} if the course set satisfies this group
     */
    boolean isSatisfiedByGroup(final Iterable<CourseGroup> groups, final Map<String, RawCourse> courseData) {

        boolean satisfied = false;

        for (final CourseGroup group : groups) {
            if (group == this || group.nbrCredits != null) {
                continue;
            }

            boolean anyCourseWorks = true;
            for (final String courseId : group.courseNumbers) {
                final RawCourse crs = courseData.get(courseId);
                if (crs == null) {
                    anyCourseWorks = false;
                    break;
                }

                if (this.courseNumbers.contains(courseId)) {
                    if (this.nbrCredits != null && crs.nbrCredits.intValue() < this.nbrCredits.intValue()) {
                        anyCourseWorks = false;
                        break;
                    }
                } else {
                    anyCourseWorks = false;
                    break;
                }
            }

            if (anyCourseWorks) {
                satisfied = true;
                break;
            }
        }

        return satisfied;
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return this.groupCode.hashCode() + Objects.hashCode(this.nbrCredits)
                + Objects.hashCode(this.lowestLastCourse)
                + Objects.hashCode(this.courseNumbers);
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
        } else if (obj instanceof final CourseGroup group) {
            equal = this.groupCode.equals(group.groupCode)
                    && Objects.equals(this.nbrCredits, group.nbrCredits)
                    && Objects.equals(this.lowestLastCourse, group.lowestLastCourse)
                    && Objects.equals(this.courseNumbers, group.courseNumbers);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Generates a string representation of the group.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return this.groupCode;
    }

    /**
     * Generates a string representation of the group.
     *
     * @param courses the map from course ID to course data
     * @return the string representation
     */
    public String toString(final Map<String, RawCourse> courses) {

        final HtmlBuilder htm = new HtmlBuilder(50);

        if (this.nbrCredits == null) {
            final String msg = Res.get(Res.SELECT_ONE_COURSE_FROM);
            htm.add(msg);
        } else {
            final String msg = Res.fmt(Res.SELECT_CREDITS_FROM, this.nbrCredits);
            htm.add(msg);
        }

        if (MathPlanConstants.AUCC3.equals(this.groupCode) || MathPlanConstants.AUCC2.equals(this.groupCode)) {
            htm.add(" any courses that satisfy the <a href='https://catalog.colostate.edu/general-catalog/",
                    "all-university-core-curriculum/aucc/#Fundamental-Competencies'>AUCC Quantitative ",
                    "Reasoning</a> requirement, such as ");

            final String[] numbers = {"M 101", "M 105", "S 100", "S 201", "S 204"};
            boolean comma = false;
            for (final String nbr : numbers) {
                final RawCourse crs = courses.get(nbr);
                if (crs != null) {
                    if (comma) {
                        htm.add(", ");
                    }
                    final String url = crs.getCatalogUrl();
                    if (url == null) {
                        htm.add(crs.courseLabel);
                    } else {
                        htm.add("<a href='", url, "' target='_blank'>", crs.courseLabel, "</a>");
                    }
                    comma = true;
                }
            }
            htm.add(" or any Precalculus course.");
        } else {
            htm.add(" (");

            boolean comma = false;
            for (final String course : this.courseNumbers) {
                final RawCourse crs = courses.get(course);
                if (crs == null) {
                    Log.warning("No Math Plan course '", course, "'");
                } else {
                    if (comma) {
                        htm.add(", ");
                    }
                    if (crs.getCatalogUrl() == null) {
                        htm.add(crs.courseLabel);
                    } else {
                        htm.add("<a href='", crs.getCatalogUrl(), "' target='_blank'>", crs.courseLabel, "</a>");
                    }
                    comma = true;
                }
            }
            htm.add(')');
        }

        return htm.toString();
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final CourseGroup o) {

        int result = this.lowestLastCourse.compareTo(o.lowestLastCourse);

        if (result == 0) {
            // Shorter (more restrictive) lists are considered higher
            final int count = this.courseNumbers.size();
            final int oCount = o.courseNumbers.size();
            result = -Integer.compare(count, oCount);
        }

        return result;
    }
}
