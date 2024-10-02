package dev.mathops.app.sim.schedule;

import dev.mathops.app.sim.courses.Course;
import dev.mathops.app.sim.rooms.ERoomUsage;
import dev.mathops.app.sim.rooms.RoomSchedule;

/**
 * A section that will meet in a room on some combination of weekdays.
 */
class AbstractSection {

    /** The section ID. */
    private final int id;

    /** The room in which the section will meet. */
    private final RoomSchedule room;

    /** The course. */
    private final Course course;

    /** The number of seats needed. */
    private final int numSeats;

    /** The usage. */
    private final ERoomUsage usage;

    /** The number of blocks. */
    private final int blocksPerDay;

    /** The block index when this section starts, -1 if the section has not been scheduled. */
    int blockIndex;

    /**
     * Constructs a new {@code AbstractSection}.
     *
     * @param theId           the section iD
     * @param theRoom         the room in which the section will meet
     * @param theCourse       the course
     * @param theNumSeats     the number seats needed
     * @param theUsage        the usage
     * @param theBlocksPerDay the number of blocks the course will meet each day
     */
    AbstractSection(final int theId, final RoomSchedule theRoom, final Course theCourse, final int theNumSeats,
                    final ERoomUsage theUsage, final int theBlocksPerDay) {

        this.id = theId;
        this.room = theRoom;
        this.course = theCourse;
        this.numSeats = theNumSeats;
        this.usage = theUsage;
        this.blocksPerDay = theBlocksPerDay;
        this.blockIndex = -1;
    }

    /**
     * Gets the section ID.
     *
     * @return the ID
     */
    final int id() {

        return this.id;
    }

    /**
     * Gets the room in which the section will meet.
     * <p>
     * Return the room
     */
    final RoomSchedule room() {

        return this.room;
    }

    /**
     * Gets the course.
     *
     * @return the course
     */
    final Course course() {

        return this.course;
    }

    /**
     * Gets the number of seats needed.
     *
     * @return the number of seats
     */
    final int numSeats() {

        return this.numSeats;
    }

    /**
     * Gets the usage.
     *
     * @return the usage
     */
    final ERoomUsage usage() {

        return this.usage;
    }

    /**
     * Gets the number of 75-minute blocks the course will meet per day.
     *
     * @return the number of 75-minute blocks
     */
    public final int blocksPerDay() {

        return this.blocksPerDay;
    }

    /**
     * Clears the block index when this section starts.
     */
    public void clearBlockIndex() {

        this.blockIndex = -1;
    }

    /**
     * Sets the block index when this section starts.
     *
     * @param theBlockIndex the block index
     */
    public void setBlockIndex(final int theBlockIndex) {

        this.blockIndex = theBlockIndex;
    }

    /**
     * Gets the block index when this section starts.
     *
     * @return the block index; -1 if this section has not been scheduled
     */
    public int getBlocksIndex() {

        return this.blockIndex;
    }
}
