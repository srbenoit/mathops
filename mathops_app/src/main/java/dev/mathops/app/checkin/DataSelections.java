package dev.mathops.app.checkin;

import dev.mathops.db.schema.legacy.rec.RawClientPc;
import dev.mathops.db.schema.legacy.rec.RawExam;
import dev.mathops.text.builder.SimpleBuilder;

/**
 * A data class to store the selected exam's course, unit, and ID, and the reserved seat.  The former are populated
 * when the staff member selects an exam, and the latter when the seat is reserved.
 */
final class DataSelections {

    /** The selected course. */
    String course = null;

    /** The selected unit. */
    int unit = 0;

    /** The selected exam record. */
    RawExam exam = null;

    /** The reserved seat. */
    RawClientPc reservedSeat = null;

    /**
     * Constructs a new {@code DataSelections}.
     */
    DataSelections() {

        // No action
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final String unitStr = Integer.toString(this.unit);

        return SimpleBuilder.concat("DataSelections{course='", this.course, "', unit=", unitStr, ", exam=", this.exam,
                ", reservedSeat=", this.reservedSeat, "}");
    }
}
