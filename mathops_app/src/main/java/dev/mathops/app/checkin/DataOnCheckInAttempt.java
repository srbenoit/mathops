package dev.mathops.app.checkin;

import dev.mathops.db.old.rawrecord.RawClientPc;
import dev.mathops.db.old.rawrecord.RawExam;
import dev.mathops.db.old.rawrecord.RawRecordConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * A data class to aggregate all information relating to a single student's attempt to check in. An instance of this
 * class is created each time a new student attempts to check in, and it is populated as various tests and checks are
 * performed, and when the staff member indicates the selected exam.
 */
final class DataOnCheckInAttempt {

    /** Data on the student requesting an exam. */
    final DataOnStudent studentData;

    /** A map from NEW course ID to object indicating the status of exams in that course, including challenge exams. */
    final Map<String, CheckInDataCourseExams> courseExams;

    /** A list of exams to display, some of which may be available. */
    final Map<String, ExamStatus> availableExams;

    /**
     * Flag indicating student is to be allowed to test only in a course that has its "incomplete in progress" flag
     * set.
     */
    boolean incompleteOnly = false;

    /** The selected course. */
    String selectedCourse = null;

    /** The selected unit. */
    int selectedUnit = 0;

    /** The selected exam record. */
    RawExam selectedExam = null;

    /** The reserved seat. */
    RawClientPc reservedSeat = null;

    /** An error message in the event that an error occurs. */
    String[] error = null;

    /**
     * Constructs a new {@code StudentCheckinInfo}.
     *
     * @param theStudentData data on the student requesting an exam
     */
    DataOnCheckInAttempt(final DataOnStudent theStudentData) {

        this.studentData = theStudentData;

        this.availableExams = new HashMap<>(40);
        this.courseExams = new HashMap<>(5);

        final CheckInDataCourseExams math117 = new CheckInDataCourseExams(RawRecordConstants.MATH117);
        final CheckInDataCourseExams math118 = new CheckInDataCourseExams(RawRecordConstants.MATH118);
        final CheckInDataCourseExams math124 = new CheckInDataCourseExams(RawRecordConstants.MATH124);
        final CheckInDataCourseExams math125 = new CheckInDataCourseExams(RawRecordConstants.MATH125);
        final CheckInDataCourseExams math126 = new CheckInDataCourseExams(RawRecordConstants.MATH126);

        this.courseExams.put(RawRecordConstants.MATH117, math117);
        this.courseExams.put(RawRecordConstants.MATH118, math118);
        this.courseExams.put(RawRecordConstants.MATH124, math124);
        this.courseExams.put(RawRecordConstants.MATH125, math125);
        this.courseExams.put(RawRecordConstants.MATH126, math126);
    }

    /**
     * Gets the {@code CheckInDataCourseExams} object based on a (NEW) course ID.
     *
     * @param key the course ID (such as "MATH 117")
     * @return the {@code CheckInDataCourseExams} object
     */
    CheckInDataCourseExams getCourseExams(final String key) {

        return this.courseExams.get(key);
    }
}
