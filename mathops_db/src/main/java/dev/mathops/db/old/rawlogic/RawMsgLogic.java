package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.old.rawrecord.RawMsg;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with 'msg' records.
 *
 * <pre>
 * Table:  'msg'
 *
 * Column name          Type              Nulls   Key
 * -------------------  ----------------  ------  -----
 * term                 char(2)           no      PK
 * term_yr              smallint          no      PK
 * touch_point          char(3)           no      PK
 * msg_code             char(8)           no      PK
 * subject              char(50)          yes
 * template             text              yes
 * </pre>
 */
public final class RawMsgLogic extends AbstractRawLogic<RawMsg> {

    /** A single instance. */
    public static final RawMsgLogic INSTANCE = new RawMsgLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawMsgLogic() {

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
    public boolean insert(final Cache cache, final RawMsg record) throws SQLException {

        if (record.termKey == null || record.touchPoint == null || record.msgCode == null) {
            throw new SQLException("Null value in primary key field.");
        }

        // FIXME: This needs to be a prepared statement! Subject and template text can include
        // apostrophes or SQL escapes.

        final String sql = SimpleBuilder.concat("INSERT INTO msg (",
                "term,term_yr,touch_point,msg_code,subject,template) VALUES (?,?,?,?,?,?)");

        // Normal SQL cannot insert into "Text" field - need a Prepared Statement

        try (final PreparedStatement ps = cache.conn.prepareStatement(sql)) {

            ps.setString(1, record.termKey.termCode);
            ps.setInt(2, record.termKey.shortYear.intValue());
            ps.setString(3, record.touchPoint);
            ps.setString(4, record.msgCode);
            ps.setString(5, record.subject);
            ps.setString(6, record.template);

            final boolean result = ps.executeUpdate() == 1;

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
    public boolean delete(final Cache cache, final RawMsg record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM msg ",
                "WHERE term=", sqlStringValue(record.termKey.termCode),
                "  AND term_yr=", sqlIntegerValue(record.termKey.shortYear),
                "  AND touch_point=", sqlStringValue(record.touchPoint),
                "  AND msg_code=", sqlStringValue(record.msgCode));

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
    public List<RawMsg> queryAll(final Cache cache) throws SQLException {

        final String sql = "SELECT * FROM msg";

        final List<RawMsg> result = new ArrayList<>(100);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawMsg.fromResultSet(rs));
            }
        }

        return result;
    }
}
