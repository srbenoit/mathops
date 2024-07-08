package dev.mathops.app.adm.resource;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * The data for one row in the student resource loan history table.
 */
class StudentResourceLoanRow {

    /** The resource ID. */
    final String resourceId;

    /** The date/time of the loan. */
    final LocalDateTime loanDateTime;

    /** The due date. */
    final LocalDate dueDate;

    /** The date/time the resource was returned. */
    final LocalDateTime returnDateTime;

    /** The resource type. */
    final String resourceType;

    /**
     * Constructs a new {@code StudentResourceLoanRow}.
     *
     * @param theResourceId     the resource ID
     * @param theLoanDateTime   the date/time the resource was lent
     * @param theDueDate        the due date
     * @param theReturnDateTime the date/time the resource was returned
     * @param theResourceType   the resource type
     */
    StudentResourceLoanRow(final String theResourceId, final LocalDateTime theLoanDateTime, final LocalDate theDueDate,
                           final LocalDateTime theReturnDateTime, final String theResourceType) {

        this.resourceId = theResourceId;
        this.loanDateTime = theLoanDateTime;
        this.dueDate = theDueDate;
        this.returnDateTime = theReturnDateTime;
        this.resourceType = theResourceType;
    }
}
