package dev.mathops.app.ops.snapin.canvas.model;

import java.util.ArrayList;
import java.util.List;

/**
 * An assignment group within a course.
 */
public final class AssignmentGroup {

    /** The group ID. */
    public final long id;

    /** The group name. */
    public final String name;

    /** The assignments that exist in the group. */
    public final List<Assignment> assignments;

    /**
     * Constructs a new {@code AssignmentGroup}.
     *
     * @param theId the group ID
     * @param theName the group name
     */
    public AssignmentGroup(final long theId, final String theName) {

        this.id = theId;
        this.name = theName;
        this.assignments = new ArrayList<>(10);
    }
}
