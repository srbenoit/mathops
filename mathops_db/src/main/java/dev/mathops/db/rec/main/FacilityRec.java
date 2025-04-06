package dev.mathops.db.rec.main;

import dev.mathops.db.DataDict;
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

    /** The 'facility_id' field value. */
    public final String facilityId;

    /** The 'facility_name' field value. */
    public final String facilityName;

    /** The 'building_name' field value. */
    public final String buildingName;

    /** The 'room_nbr' field value. */
    public final String roomNbr;

    /**
     * Constructs a new {@code FacilityRec}.
     *
     * @param theFacilityId   the facility ID
     * @param theFacilityName the facility name
     * @param theBuildingName the building ID (null if the facility is virtual)
     * @param theRoomNbr      the room number (null if facility is virtual)
     */
    public FacilityRec(final String theFacilityId, final String theFacilityName, final String theBuildingName,
                       final String theRoomNbr) {

        super();

        if (theFacilityId == null) {
            throw new IllegalArgumentException("Facility ID may not be null");
        }
        if (theFacilityName == null) {
            throw new IllegalArgumentException("Facility name may not be null");
        }

        this.facilityId = theFacilityId;
        this.facilityName = theFacilityName;
        this.buildingName = theBuildingName;
        this.roomNbr = theRoomNbr;
    }

    /**
     * Compares two records for order.  Order is based only on facility ID.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final FacilityRec o) {

        return this.facilityId.compareTo(o.facilityId);
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

        appendField(htm, DataDict.FLD_FACILITY_ID, this.facilityId);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_FACILITY_NAME, this.facilityName);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_BUILDING_NAME, this.buildingName);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_ROOM_NBR, this.roomNbr);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return this.facilityId.hashCode()
               + this.facilityName.hashCode()
               + Objects.hashCode(this.buildingName)
               + Objects.hashCode(this.roomNbr);
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
            equal = this.facilityId.equals(rec.facilityId)
                    && this.facilityName.equals(rec.facilityName)
                    && Objects.equals(this.buildingName, rec.buildingName)
                    && Objects.equals(this.roomNbr, rec.roomNbr);
        } else {
            equal = false;
        }

        return equal;
    }
}
