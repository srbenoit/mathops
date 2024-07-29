package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawrecord.RawEtext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with etext_key records.
 *
 * <pre>
 * Table:  'etext_key'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * etext_id             char(6)                   no      PK
 * retention            char(1)                   no
 * purchase_url         varchar(140)              yes
 * refund_period        smallint                  yes
 * key_entry            char(1)                   no
 * active               char(1)                   no
 * button_label         char(80)                  yes
 * </pre>
 */
public final class RawEtextLogic extends AbstractRawLogic<RawEtext> {

    /** A single instance. */
    public static final RawEtextLogic INSTANCE = new RawEtextLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawEtextLogic() {

        super();
    }

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public boolean insert(final Cache cache, final RawEtext record) throws SQLException {

        if (record.etextId == null) {
            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat("INSERT INTO etext ",
                "(etext_id,retention,purchase_url,refund_period,key_entry,active,button_label) VALUES (",
                sqlStringValue(record.etextId), ",",
                sqlStringValue(record.retention), ",",
                sqlStringValue(record.purchaseUrl), ",",
                sqlIntegerValue(record.refundPeriod), ",",
                sqlStringValue(record.keyEntry), ",",
                sqlStringValue(record.active), ",",
                sqlStringValue(record.buttonLabel), ")");

        try (final Statement stmt = cache.conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }

            return result;
        }
    }

    /**
     * Deletes a record.
     *
     * @param cache  the data cache
     * @param record the record to delete
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public boolean delete(final Cache cache, final RawEtext record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM etext ",
                "WHERE etext_id=", sqlStringValue(record.etextId));

        try (final Statement stmt = cache.conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }

            return result;
        }
    }

    /**
     * Gets all records.
     *
     * @param cache the data cache
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public List<RawEtext> queryAll(final Cache cache) throws SQLException {

        final String sql = "SELECT * FROM etext";

        final List<RawEtext> result = new ArrayList<>(20);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawEtext.fromResultSet(rs));
            }
        }

        return result;
    }

    /**
     * Queries for a single etext.
     *
     * @param cache   the data cache
     * @param etextId the etext ID for which to query
     * @return the complete set of records in the database
     * @throws SQLException if there is an error performing the query
     */
    public static RawEtext query(final Cache cache, final String etextId) throws SQLException {

        RawEtext result = null;

        final String sql = SimpleBuilder.concat(
                "SELECT * FROM etext WHERE etext_id=", sqlStringValue(etextId));

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                result = RawEtext.fromResultSet(rs);
            }
        }

        return result;
    }
}
