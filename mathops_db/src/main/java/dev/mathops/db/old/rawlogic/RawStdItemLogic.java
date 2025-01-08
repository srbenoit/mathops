package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawrecord.RawStdItem;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "std_item" records.
 *
 * <p>
 * There is currently no table for this data - it is hard-coded into this class.
 */
public enum RawStdItemLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     */
    public static boolean insert(final Cache cache, final RawStdItem record) {

        Log.warning("Unable to insert std_item record: no underlying table.");

        return false;
    }

    /**
     * Deletes a record.
     *
     * @param cache  the data cache
     * @param record the record to delete
     * @return {@code true} if successful; {@code false} if not
     */
    public static boolean delete(final Cache cache, final RawStdItem record) {

        Log.warning("Unable to delete std_item record: no underlying table.");

        return false;
    }

    /**
     * Gets all records.
     *
     * @param cache the data cache
     * @return the list of records
     */
    public static List<RawStdItem> queryAll(final Cache cache) {

        final List<RawStdItem> result = new ArrayList<>(0);

        Log.warning("Unable to query all std_item records: no underlying table.");

        return result;
    }

    /**
     * Queries all items attached to a standard.
     *
     * @param cache        the data cache
     * @param stdId        the standard ID
     * @param masteryGroup the mastery group number
     * @return the list of records that matched the criteria, a zero-length list if none matched
     */
    public static List<RawStdItem> queryByMasteryGroup(final Cache cache, final String stdId,
                                                       final Integer masteryGroup) {

        List<RawStdItem> result;

        return new ArrayList<>(0);
    }
}
