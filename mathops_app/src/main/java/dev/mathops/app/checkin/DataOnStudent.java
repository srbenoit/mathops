package dev.mathops.app.checkin;

import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.old.rawrecord.RawStudent;

import java.util.List;

/**
 * Data on the student requesting an exam.
 */
public final class DataOnStudent {

    /** The student's ID. */
    final String stuId;

    /** The student record from the database. */
    final RawStudent student;

    /** The student term record from the database. */
    final RawStterm studentTerm;

    /** A list of hold messages to show to the student. */
    final List<String> holdsToShow;

    /** A list of special categories the student is a member of. */
    final List<String> specialTypes;

    /**
     * Constructs a new {@code DataOnStudent}.
     *
     * @param theStudent the student record
     * @param theStudentTerm the student term record
     * @param theHolds the list of holds to display to the student
     * @param theSpecialTypes the list of special categories to which the student belongs
     */
    public DataOnStudent(final RawStudent theStudent, final RawStterm theStudentTerm, final List<String> theHolds,
                         final List<String> theSpecialTypes) {

        this.stuId = theStudent.stuId;
        this.student = theStudent;
        this.studentTerm = theStudentTerm;
        this.holdsToShow = theHolds;
        this.specialTypes = theSpecialTypes;
    }
}
