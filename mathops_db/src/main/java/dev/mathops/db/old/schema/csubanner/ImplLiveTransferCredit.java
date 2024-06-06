package dev.mathops.db.old.schema.csubanner;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.logic.DbConnection;
import dev.mathops.db.old.ifaces.ILiveTransferCredit;
import dev.mathops.db.old.rec.LiveTransferCredit;
import dev.mathops.db.old.schema.AbstractImpl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementation of the {@code ILiveTransferCredit} interface for the CSU Banner schema.
 */
public final class ImplLiveTransferCredit
        extends AbstractImpl<LiveTransferCredit> implements ILiveTransferCredit {

    /** The name of the primary table. */
    private static final String TABLE_NAME = "Artificial";

    /**
     * Constructs a new {@code ImplLiveTransferCredit}.
     */
    public ImplLiveTransferCredit() {

        super();
    }

    /**
     * Gets the name of the primary table.
     *
     * @return the table name
     */
    @Override
    public String getTableName() {

        return TABLE_NAME;
    }

    /**
     * Handles request to clean (delete all rows from) a table in the live registration database, an operation which is
     * not permitted.
     *
     * @param cache the data cache
     * @throws SQLException if there is an error performing the query
     */
    @Override
    public void clean(final Cache cache) throws SQLException {

        throw new SQLException(Res.get(Res.CANT_CLEAN_LIVE));
    }

    /**
     * Returns the total number of objects in the database.
     *
     * @param conn the database connection, checked out to this thread
     * @return the number of records found
     * @throws SQLException if there is an error performing the query
     */
    @Override
    public int count(final DbConnection conn) throws SQLException {

        return defaultCount(conn);
    }

    /**
     * Queries for all rows (not recommended).
     *
     * @param conn the database connection, checked out to this thread
     * @return the list of models that matched the criteria, a zero-length array if none matched
     * @throws SQLException if there is an error performing the query
     */
    @Override
    public List<LiveTransferCredit> queryAll(final DbConnection conn) throws SQLException {

        return defaultQueryAll(conn);
    }

    /**
     * Queries for a student.
     *
     * @param conn      the database connection, checked out to this thread
     * @param studentId the student ID
     * @return the list of models that matched the criteria, a zero-length array if none matched
     * @throws SQLException if there is an error performing the query
     */
    @Override
    public List<LiveTransferCredit> query(final DbConnection conn, final String studentId)
            throws SQLException {

        final HtmlBuilder builder = new HtmlBuilder(1000);

        builder.add("SELECT i.spriden_id CSU_ID, t.shrtrce_term_code_eff TERM,");
        builder.add("  t.shrtrce_subj_code||t.shrtrce_crse_numb TRANSFERRED,");
        builder.add("  t.shrtrce_credit_hours CREDITS, t.shrtrce_grde_code GRADE");
        builder.add(" FROM spriden i, shrtrce t");
        builder.add(" WHERE t.shrtrce_pidm=i.spriden_pidm");
        builder.add("   AND i.spriden_change_ind IS NULL");
        builder.add("   AND i.spriden_id='", studentId, "'");

        return executeSimpleQuery(conn, builder.toString());
    }

    /**
     * Builds a {@code LiveTransferCredit} from a {@code ResultSet}.
     *
     * @param conn the connection, in case the construction of records from the result set requires further queries
     * @param rs   the result set from which to construct the object
     * @return the {@code LiveTransferCredit} object
     * @throws SQLException if there was an error retrieving a field
     */
    @Override
    protected LiveTransferCredit constructFromResultSet(final DbConnection conn,
                                                        final ResultSet rs) throws SQLException {

        try {
            return new LiveTransferCredit(getString(rs, "CSU_ID"),
                    ImplLiveStudent.convertTermCode(getString(rs, "TERM")),
                    getString(rs, "TRANSFERRED"),
                    getDouble(rs, "CREDITS"),
                    getString(rs, "GRADE"));
        } catch (final IllegalArgumentException ex) {
            throw new SQLException(ex);
        }
    }
}
