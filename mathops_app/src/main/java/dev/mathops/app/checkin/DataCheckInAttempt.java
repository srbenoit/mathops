package dev.mathops.app.checkin;

import dev.mathops.core.builder.SimpleBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A data class to aggregate all information relating to a single student's attempt to check in. An instance of this
 * class is created each time a new student attempts to check in, and it is populated as various tests and checks are
 * performed, and when the staff member indicates the selected exam.
 */
final class DataCheckInAttempt {

    /** Data on the student requesting an exam. */
    final DataStudent studentData;

    /** A map from course numbers object to container for status of exams in that course, including challenge exams. */
    private final Map<CourseNumbers, DataCourseExams> courseExams;

    /** A container for status of non-course exams. */
    final DataNonCourseExams nonCourseExams;

    /** The selected course, unit, exam ID, and the reserved seat. */
    final DataSelections selections;

    /** An error message in the event that an error occurs. */
    String[] error = null;

    /**
     * Constructs a new {@code StudentCheckInInfo}.
     *
     * @param theStudentData data on the student requesting an exam
     */
    DataCheckInAttempt(final DataStudent theStudentData) {

        this.studentData = theStudentData;

        this.courseExams = new HashMap<>(CourseNumbers.NUM_COURSES);
        for (final CourseNumbers numbers : CourseNumbers.COURSES) {
            final DataCourseExams data = new DataCourseExams(numbers);
            this.courseExams.put(numbers, data);
        }

        this.nonCourseExams = new DataNonCourseExams();
        this.selections = new DataSelections();
    }

    /**
     * Gets the {@code DataCourseExams} object based on a course numbers object.
     *
     * @param numbers the numbers object
     * @return the corresponding {@code DataCourseExams} object; {@code null} if none matches the given numbers object
     */
    DataCourseExams getCourseExams(final CourseNumbers numbers) {

        return this.courseExams.get(numbers);
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final String errorStr = Arrays.toString(this.error);

        return SimpleBuilder.concat("DataOnCheckInAttempt{studentData=", this.studentData, ", courseExams=",
                this.courseExams, ", nonCourseExams=", this.nonCourseExams, ", selections='", this.selections,
                 ", error=" ,errorStr, "}");
    }
}
