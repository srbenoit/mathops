package dev.mathops.app.sim.registration;

import dev.mathops.commons.builder.HtmlBuilder;

/**
 * An assignment that occupies some block(s) of time in a {@code Room}.
 */
record RoomAssignment(int id, Room room, Course course, int numSeats, ERoomUsage usage, int num25MinBlocks,
                      EAssignmentType type) {
    

    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(100);
        builder.add("Assignment ");
        builder.add(this.id);
        builder.add(" of ");
        builder.add(this.num25MinBlocks);
        builder.add(" blocks for ", this.course.courseId, " (", this.usage, ") with ");
        builder.add(this.numSeats);
        builder.add(" seats used.");

        return builder.toString();
    }
}
