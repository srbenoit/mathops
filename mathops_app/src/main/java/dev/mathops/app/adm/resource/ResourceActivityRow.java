package dev.mathops.app.adm.resource;

import java.time.LocalDateTime;

/**
 * The data for one row in the resource activity table.
 */
class ResourceActivityRow implements Comparable<ResourceActivityRow> {

    /** The activity type. */
    final String activityType;

    /** The student ID. */
    final String studentId;

    /** The resource ID. */
    final String resourceId;

    /** The resource type. */
    final String resourceType;

    /** The date/time of the activity. */
    final LocalDateTime activityDateTime;

    /**
     * Constructs a new {@code ResourceActivityRow}.
     *
     * @param theActivityType     the activity type
     * @param theStudentId        the student ID
     * @param theResourceId       the resource ID
     * @param theActivityDateTime the date/time the resource was lent
     * @param theResourceType     the resource type
     */
    ResourceActivityRow(final String theActivityType, final String theStudentId, final String theResourceId,
                        final LocalDateTime theActivityDateTime, final String theResourceType) {

        if (theActivityDateTime == null) {
            throw new IllegalArgumentException("Activity date type may not be null");
        }

        this.activityType = theActivityType;
        this.studentId = theStudentId;
        this.resourceId = theResourceId;
        this.activityDateTime = theActivityDateTime;
        this.resourceType = theResourceType;
    }

    /**
     * Compares this row to another for order. Order is based on activity date/time (which may not be null).
     */
    @Override
    public int compareTo(final ResourceActivityRow o) {

        return this.activityDateTime.compareTo(o.activityDateTime);
    }
}
