package dev.mathops.db.rawlogic;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.rawrecord.RawStcunit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "stcunit" records.
 *
 * <pre>
 * Table:  'stcunit'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * stu_id               char(9)                   no      PK
 * course               char(6)                   no      PK
 * unit                 smallint                  no      PK
 * review_status        char(1)                   no
 * review_score         smallint                  yes
 * review_points        smallint                  yes
 * proctored_status     char(1)                   no
 * proctored_score      smallint                  yes
 * proctored_points     smallint                  yes
 * </pre>
 */
public final class RawStcunitLogic extends AbstractRawLogic<RawStcunit> {

    /** A single instance. */
    public static final RawStcunitLogic INSTANCE = new RawStcunitLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawStcunitLogic() {

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
    public boolean insert(final Cache cache, final RawStcunit record) throws SQLException {

        if (record.stuId == null || record.course == null || record.unit == null
                || record.reviewStatus == null || record.proctoredStatus == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final boolean result;

        if (record.stuId.startsWith("99")) {
            Log.info("Skipping insert of stcunit for test student:");
            Log.info("  Student ID: ", record.stuId);
            result = false;
        } else {
            final String sql = SimpleBuilder.concat(
                    "INSERT INTO stcunit (stu_id,course,unit,review_status,",
                    "review_score,review_points,proctored_status,proctored_score,",
                    "proctored_points) VALUES (",
                    sqlStringValue(record.stuId), ",",
                    sqlStringValue(record.course), ",",
                    sqlIntegerValue(record.unit), ",",
                    sqlStringValue(record.reviewStatus), ",",
                    sqlIntegerValue(record.reviewScore), ",",
                    sqlIntegerValue(record.reviewPoints), ",",
                    sqlStringValue(record.proctoredStatus), ",",
                    sqlIntegerValue(record.proctoredScore), ",",
                    sqlIntegerValue(record.proctoredPoints), ")");

            try (final Statement stmt = cache.conn.createStatement()) {
                result = stmt.executeUpdate(sql) == 1;

                if (result) {
                    cache.conn.commit();
                } else {
                    cache.conn.rollback();
                }
            }
        }

        return result;
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
    public boolean delete(final Cache cache, final RawStcunit record) throws SQLException {

        final boolean result;

        final String sql = SimpleBuilder.concat("DELETE FROM stcunit ",
                "WHERE stu_id=", sqlStringValue(record.stuId),
                "  AND course=", sqlStringValue(record.course),
                "  AND unit=", sqlIntegerValue(record.unit));

        try (final Statement stmt = cache.conn.createStatement()) {
            result = stmt.executeUpdate(sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }
        }

        return result;
    }

    /**
     * Gets all records.
     *
     * @param cache the data cache
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public List<RawStcunit> queryAll(final Cache cache) throws SQLException {

        final String sql = "SELECT * FROM stcunit";

        final List<RawStcunit> result = new ArrayList<>(500);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawStcunit.fromResultSet(rs));
            }
        }

        return result;
    }
}
