package dev.mathops.db.old;

import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;

import java.sql.SQLException;
import java.util.List;

/**
 * The base class for interfaces used to query and manage models.
 *
 * @param <R> the type of record this object manages
 */
public interface IDataObject<R> extends IDataDomainObject {

    /**
     * Returns the total number of objects in the database.
     *
     * @param conn the database connection, checked out to this thread
     * @return the number of objects in the database
     * @throws SQLException if there is an error performing the query
     */
    int count(DbConnection conn) throws SQLException;

    /**
     * Queries the database table to verify we are connected to a "TEST" database, then deletes all records in the
     * table.
     *
     * @param cache the data cache
     * @throws SQLException if there is an error performing the query
     */
    void clean(Cache cache) throws SQLException;

    /**
     * Queries every record in the database, in no particular order.
     *
     * @param conn the database connection, checked out to this thread
     * @return the complete set of records in the database
     * @throws SQLException if there is an error performing the query
     */
    List<R> queryAll(DbConnection conn) throws SQLException;
}
