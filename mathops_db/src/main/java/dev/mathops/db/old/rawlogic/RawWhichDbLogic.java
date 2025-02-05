package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawWhichDb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A utility class to work with which_db records. Note that it is not possible to change this table through code, since
 * this table's data identifies the database.
 *
 * <pre>
 * Table:  'which_db'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * descr                char(6)                   no      PK
 * </pre>
 */
public enum RawWhichDbLogic {
    ;

    /**
     * Gets the first record found in a query of all records (there should be only one).
     *
     * @param cache the data cache
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawWhichDb query(final Cache cache) throws SQLException {

        RawWhichDb result = null;

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery("SELECT * FROM which_db")) {

            if (rs.next()) {
                result = RawWhichDb.fromResultSet(rs);
                if (rs.next()) {
                    Log.warning("Multiple 'which_db' rows found!");
                }
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }
}
