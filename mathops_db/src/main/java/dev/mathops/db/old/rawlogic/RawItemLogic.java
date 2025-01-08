package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawrecord.RawItem;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with item records.
 *
 * <p>
 * There is currently no table for this data - it is hard-coded into this class.
 */
public enum RawItemLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     */
    public static boolean insert(final Cache cache, final RawItem record) {

        Log.warning("Unable to insert item record: no underlying table.");

        return false;
    }

    /**
     * Deletes a record.
     *
     * @param cache  the data cache
     * @param record the record to delete
     * @return {@code true} if successful; {@code false} if not
     */
    public static boolean delete(final Cache cache, final RawItem record) {

        Log.warning("Unable to delete item record: no underlying table.");

        return false;
    }

    /**
     * Gets all records.
     *
     * @param cache the data cache
     * @return the list of records
     */
    public static List<RawItem> queryAll(final Cache cache) {

        final List<RawItem> result = new ArrayList<>(50);

        Log.warning("Unable to query all item records: no underlying table.");

        return result;
    }

    /**
     * Queries for an item by its ID.
     *
     * @param cache  the data cache
     * @param itemId the item ID
     * @return the matching record; null if none found
     */
    public static RawItem query(final Cache cache, final String itemId) {

        return null;
    }
}
