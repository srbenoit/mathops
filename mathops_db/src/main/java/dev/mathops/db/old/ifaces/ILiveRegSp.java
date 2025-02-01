package dev.mathops.db.old.ifaces;

import dev.mathops.db.DbConnection;
import dev.mathops.db.old.IDataObject;
import dev.mathops.db.rec.LiveReg;

import java.sql.SQLException;
import java.util.List;

/**
 * An interface for management of {@code LiveReg} records.
 */
public interface ILiveRegSp extends IDataObject<LiveReg> {

    /**
     * Queries for a particular student.
     *
     * @param conn      the database connection, checked out to this thread
     * @param studentId the student ID
     * @return the list of models that matched the criteria, a zero-length array if none matched
     * @throws SQLException if there is an error performing the query
     */
    List<LiveReg> query(DbConnection conn, String studentId) throws SQLException;
}
