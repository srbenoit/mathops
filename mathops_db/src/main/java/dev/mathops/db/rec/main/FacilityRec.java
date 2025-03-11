package dev.mathops.db.rec.main;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.Objects;

/**
 * An immutable raw "facility" record.
 *
 * <p>
 * Each record represents a "facility", which can be a physical space like a classroom or testing center, or a virtual
 * space like online help hours.  Facilities can have a schedule of hours when they are open/available and may have
 * scheduled closures (holidays, weather-related closures, etc.).
 *
 * <p>
 * The primary key on the underlying table is the facility ID.
 */
public final class FacilityRec extends RecBase implements Comparable<FacilityRec> {

    /** The table name for serialization of records. */
    public static final String TABLE_NAME = "facility";

    /** A field name for serialization of records. */
    private static final String FLD_FACILITY = "facility";

    /** A field name for serialization of records. */
    private static final String FLD_NAME = "name";

    /** A field name for serialization of records. */
    private static final String FLD_BUILDING = "building";

    /** A field name for serialization of records. */
    private static final String FLD_ROOM = "room";

    /** The 'facility' field value. */
    public final String facility;

    /** The 'name' field value. */
    public final String name;

    /** The 'building' field value. */
    public final String building;

    /** The 'room' field value. */
    public final String room;

    /**
     * Constructs a new {@code FacilityRec}.
     *
     * @param theFacility the facility ID
     * @param theName     the facility name
     * @param theBuilding the building ID (null if the facility is virtual)
     * @param theRoom     the room number (null if facility is virtual)
     */
    public FacilityRec(final String theFacility, final String theName,
                       final String theBuilding, final String theRoom) {

        super();

        if (theFacility == null) {
            throw new IllegalArgumentException("Facility ID may not be null");
        }
        if (theName == null) {
            throw new IllegalArgumentException("Facility name may not be null");
        }

        this.facility = theFacility;
        this.name = theName;
        this.building = theBuilding;
        this.room = theRoom;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final FacilityRec o) {

        return this.facility.compareTo(o.facility);
    }

    /**
     * Generates a string serialization of the record. Each concrete subclass should have a constructor that accepts a
     * single {@code String} to reconstruct the object from this string.
     *
     * @return the string
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(40);

        appendField(htm, FLD_FACILITY, this.facility);
        htm.add(DIVIDER);
        appendField(htm, FLD_NAME, this.name);
        htm.add(DIVIDER);
        appendField(htm, FLD_BUILDING, this.building);
        htm.add(DIVIDER);
        appendField(htm, FLD_ROOM, this.room);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return this.facility.hashCode()
               + this.name.hashCode()
               + Objects.hashCode(this.building)
               + Objects.hashCode(this.room);
    }

    /**
     * Tests whether this object is equal to another.
     *
     * @param obj the other object
     * @return true if equal; false if not
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final FacilityRec rec) {
            equal = this.facility.equals(rec.facility)
                    && this.name.equals(rec.name)
                    && Objects.equals(this.building, rec.building)
                    && Objects.equals(this.room, rec.room);
        } else {
            equal = false;
        }

        return equal;
    }
}
