package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawrecord.RawCustd;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with course unit standard records.
 *
 * <p>
 * There is currently no table for this data - it is hard-coded into this class.
 */
public enum RawCustdLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     */
    public static boolean insert(final Cache cache, final RawCustd record) {

        Log.warning("Unable to insert custd record: no underlying table.");

        return false;
    }

    /**
     * Deletes a record.
     *
     * @param cache  the data cache
     * @param record the record to delete
     * @return {@code true} if successful; {@code false} if not
     */
    public static boolean delete(final Cache cache, final RawCustd record) {

        Log.warning("Unable to delete custd record: no underlying table.");

        return false;
    }

    /**
     * Gets all records.
     *
     * @param cache the data cache
     * @return the list of records
     */
    public static List<RawCustd> queryAll(final Cache cache) {

        final List<RawCustd> result = new ArrayList<>(0);

        Log.warning("Unable to query all custd records: no underlying table.");

        return result;
    }

    /**
     * Queries all standards for a particular course. Results are sorted by course, then unit, then index.
     *
     * @param cache       the data cache
     * @param theCourseId the ID of the course
     * @return the list of records that matched the criteria, a zero-length list if none matched
     */
    public static List<RawCustd> queryByCourse(final Cache cache, final String theCourseId) {

        // TODO:
        return new ArrayList<>(0);
    }
}
