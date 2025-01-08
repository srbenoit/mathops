package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawrecord.RawStd;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with standard records.
 *
 * <p>
 * There is currently no table for this data - it is hard-coded into this class.
 */
public enum RawStdLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     */
    public static boolean insert(final Cache cache, final RawStd record) {

        Log.warning("Unable to insert std record: no underlying table.");

        return false;
    }

    /**
     * Deletes a record.
     *
     * @param cache  the data cache
     * @param record the record to delete
     * @return {@code true} if successful; {@code false} if not
     */
    public static boolean delete(final Cache cache, final RawStd record) {

        Log.warning("Unable to delete std record: no underlying table.");

        return false;
    }

    /**
     * Gets all records.
     *
     * @param cache the data cache
     * @return the list of records
     */
    public static List<RawStd> queryAll(final Cache cache) {

        final List<RawStd> result = new ArrayList<>(100);

        Log.warning("Unable to query all std records: no underlying table.");

        return result;
    }
}
