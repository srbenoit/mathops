package dev.mathops.app.checkin;

import dev.mathops.db.rawrecord.RawClientPc;
import dev.mathops.db.rawrecord.RawExam;
import dev.mathops.db.rawrecord.RawStterm;
import dev.mathops.db.rawrecord.RawStudent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A data class to aggregate all information relating to a single student's attempt to check in. An instance of this
 * class is created each time a new student attempts to check in, and it is populated as various tests and checks are
 * performed, and when the staff member indicates the selected exam.
 */
class StudentCheckinInfo {

    /** The ID of the student for whom this record is being constructed. */
    public final String studentId;

    /** A list of exams that are available to the student. */
    public final Map<String, AvailableExam> availableExams;

    /** The student record from the database. */
    public RawStudent studentRecord;

    /** The student term record from the database. */
    public RawStterm studentTermRecord;

    /**
     * Flag indicating student is to be allowed to test only in a course that has its "incomplete in progress" flag
     * set.
     */
    public boolean incompleteOnly;

    /** A list of hold messages to show to the student. */
    public List<String> holdsToShow;

    /** A list of special categories the student is a member of. */
    public List<String> specials;

    /** The selected course. */
    public String course;

    /** The selected unit. */
    public int unit;

    /** The selected exam record. */
    public RawExam exam;

    /** The reserved seat. */
    public RawClientPc reservedSeat;

    /** An error message in the event that an error occurs. */
    public String[] error;

    /**
     * Constructs a new {@code StudentCheckinInfo}.
     *
     * @param theStudentId the ID of the student for whom this record is being constructed
     */
    StudentCheckinInfo(final String theStudentId) {

        this.studentId = theStudentId;
        this.availableExams = new HashMap<>(40);
    }
}
