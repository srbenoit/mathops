package dev.mathops.app.ops.snapin.canvas.model;

/**
 * An assignment within a course.
 */
public final class Assignment {

    /** The assignment ID. */
    public final long id;

    /** The assignment name. */
    public final String name;

    /**
     * Constructs a new {@code Assignment}.
     *
     * @param theId the assignment ID
     * @param theName the assignment name
     */
    public Assignment(final long theId, final String theName) {

        this.id = theId;
        this.name = theName;
    }
}
