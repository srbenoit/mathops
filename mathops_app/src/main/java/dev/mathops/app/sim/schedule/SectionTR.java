package dev.mathops.app.sim.schedule;

import dev.mathops.app.sim.courses.Course;
import dev.mathops.app.sim.courses.EMeetingDays;
import dev.mathops.app.sim.rooms.ERoomUsage;
import dev.mathops.app.sim.rooms.RoomSchedule;
import dev.mathops.commons.builder.HtmlBuilder;

/**
 * A section that will meet in a room on some combination of Monday, Wednesday, and Friday, for an integer number of
 * hours.
 */
public final class SectionTR extends AbstractSection {

    /** The days the section meets (some combination of Tuesday and Thursday). */
    private final EMeetingDays meetingDays;

    /**
     * Constructs a new {@code SectionTR}.
     *
     * @param theId           the section iD
     * @param theMeetingDays  the days the section meets
     * @param theRoom         the room in which the section will meet
     * @param theCourse       the course
     * @param theNumSeats     the number seats needed
     * @param theUsage        the usage
     * @param theBlocksPerDay the number of 75-minute blocks the course will meet each day
     */
    public SectionTR(final int theId, final EMeetingDays theMeetingDays, final RoomSchedule theRoom, final Course theCourse,
                     final int theNumSeats, final ERoomUsage theUsage, final int theBlocksPerDay) {

        super(theId, theRoom, theCourse, theNumSeats, theUsage, theBlocksPerDay);

        this.meetingDays = theMeetingDays;
    }

    /**
     * Gets the days the section meets (some combination of Tuesday and Thursday).
     *
     * @return the meeting days
     */
    public EMeetingDays meetingDays() {

        return this.meetingDays;
    }

    /**
     * Generates a string representation of the section.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(100);

        final int id = id();
        final Course course = course();
        final RoomSchedule room = room();
        final int numSeats = numSeats();
        final int blocksPerDay = blocksPerDay();
        final ERoomUsage usage = usage();

        builder.add("Sect ");
        builder.add(id);
        builder.add(" of ", course, " (", usage, ") meets ", this.meetingDays, " in ", room, " for (");
        if (blocksPerDay == 1) {
            builder.add("1) 75-min block");
        } else {
            builder.add(blocksPerDay);
            builder.add(") 75-min blocks");
        }
        builder.add(" with ");
        builder.add(numSeats);
        builder.add(" seats used.");

        return builder.toString();
    }
}
