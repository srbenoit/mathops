package dev.mathops.db.old.rawlogic;

import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rec.RecBase;

import java.sql.SQLException;
import java.util.List;

/**
 * The base class for logic modules.
 *
 * @param <T> the record type
 */
abstract class AbstractRawLogic<T extends RecBase> extends AbstractLogicModule {

    /**
     * Constructs a new {@code AbstractRawLogic}.
     */
    AbstractRawLogic() {

        super();
    }

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    public abstract boolean insert(Cache cache, T record) throws SQLException;

    /**
     * Deletes a record.
     *
     * @param cache  the data cache
     * @param record the record to delete
     * @return {@code true} if successful; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    public abstract boolean delete(Cache cache, T record) throws SQLException;

    /**
     * Gets all records.
     *
     * @param cache the data cache
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    public abstract List<T> queryAll(final Cache cache) throws SQLException;
}
