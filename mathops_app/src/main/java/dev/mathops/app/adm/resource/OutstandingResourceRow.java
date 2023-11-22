package dev.mathops.app.adm.resource;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * The data for one row in the student resource loan history table.
 */
/* default */ class OutstandingResourceRow {

    /** The student ID. */
    /* default */ final String studentId;

    /** The student name. */
    /* default */ final String studentName;

    /** The resource ID. */
    /* default */ final String resourceId;

    /** The date/time of the loan. */
    /* default */ final LocalDateTime loanDateTime;

    /** The due date. */
    /* default */ final LocalDate dueDate;

    /** The resource type. */
    /* default */ final String resourceType;

    /**
     * Constructs a new {@code OutstandingResourceRow}.
     *
     * @param theStudentId    the student ID
     * @param theStudentName  the student name
     * @param theResourceId   the resource ID
     * @param theLoanDateTime the date/time the resource was lent
     * @param theDueDate      the due date
     * @param theResourceType the resource type
     */
    /* default */ OutstandingResourceRow(final String theStudentId, final String theStudentName,
                                         final String theResourceId, final LocalDateTime theLoanDateTime,
                                         final LocalDate theDueDate,
                                         final String theResourceType) {

        this.studentId = theStudentId;
        this.studentName = theStudentName;
        this.resourceId = theResourceId;
        this.loanDateTime = theLoanDateTime;
        this.dueDate = theDueDate;
        this.resourceType = theResourceType;
    }
}
