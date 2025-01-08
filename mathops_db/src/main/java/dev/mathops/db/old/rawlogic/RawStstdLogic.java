package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawrecord.RawStstd;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with student standard records.
 *
 * <p>
 * There is currently no table for this data - it is hard-coded into this class.
 */
public enum RawStstdLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     */
    public static boolean insert(final Cache cache, final RawStstd record) {

        Log.warning("Unable to insert ststd record: no underlying table.");

        return false;
    }

    /**
     * Deletes a record.
     *
     * @param cache  the data cache
     * @param record the record to delete
     * @return {@code true} if successful; {@code false} if not
     */
    public static boolean delete(final Cache cache, final RawStstd record) {

        Log.warning("Unable to delete ststd record: no underlying table.");

        return false;
    }

    /**
     * Gets all records.
     *
     * @param cache the data cache
     * @return the list of records
     */
    public static List<RawStstd> queryAll(final Cache cache) {

        final List<RawStstd> result = new ArrayList<>(500);

        Log.warning("Unable to query all ststd records: no underlying table.");

        return result;
    }

    /**
     * Queries all standards for a student.
     *
     * @param cache    the data cache
     * @param theStuId the student ID
     * @return the list of records that matched the criteria, a zero-length list if none matched
     */
    public static List<RawStstd> queryByStudent(final Cache cache, final String theStuId) {

        List<RawStstd> result;

        return new ArrayList<>(0);
    }
}
