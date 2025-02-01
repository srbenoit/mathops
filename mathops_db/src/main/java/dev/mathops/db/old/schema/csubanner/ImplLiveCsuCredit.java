package dev.mathops.db.old.schema.csubanner;

import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.old.ifaces.ILiveCsuCredit;
import dev.mathops.db.old.schema.AbstractImpl;
import dev.mathops.db.rec.LiveCsuCredit;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementation of the {@code ILiveCsuCredit} interface for the CSU Banner schema.
 */
public final class ImplLiveCsuCredit extends AbstractImpl<LiveCsuCredit> implements ILiveCsuCredit {

    /** A single instance. */
    public static final ImplLiveCsuCredit INSTANCE = new ImplLiveCsuCredit();

    /** The name of the primary table. */
    private static final String TABLE_NAME = "Artificial";

    /**
     * Constructs a new {@code ImplLiveCsuCredit}.
     */
    private ImplLiveCsuCredit() {

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
    public List<LiveCsuCredit> queryAll(final DbConnection conn) throws SQLException {

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
    public List<LiveCsuCredit> query(final DbConnection conn, final String studentId)
            throws SQLException {

        final HtmlBuilder builder = new HtmlBuilder(1000);

        builder.add("select i.spriden_id CSU_ID, csu.term TERM, csu.course COURSE,");
        builder.add("       csu.credits CREDITS, csu.grade GRADE");
        builder.add(" from spriden i");
        builder.add(" join (select n.shrtckn_pidm pidm, n.shrtckn_term_code term,");
        builder.add("               n.shrtckn_subj_code||n.shrtckn_crse_numb course,");
        builder.add("               k.shrtckg_credit_hours credits,");
        builder.add("               k.shrtckg_grde_code_final grade");
        builder.add("        from shrtckn n");
        builder.add("        join shrtckg k on k.shrtckg_pidm = n.shrtckn_pidm");
        builder.add("        where k.shrtckg_term_code = n.shrtckn_term_code");
        builder.add("          and k.shrtckg_tckn_seq_no = n.shrtckn_seq_no");
        builder.add("          and k.shrtckg_seq_no =");
        builder.add("              (select max(k3.shrtckg_seq_no)");
        builder.add("               from shrtckg k3");
        builder.add("               where k3.shrtckg_pidm = k.shrtckg_pidm");
        builder.add("                 and k3.shrtckg_term_code = k.shrtckg_term_code");
        builder.add("                 and k3.shrtckg_tckn_seq_no = k.shrtckg_tckn_seq_no)");
        builder.add("      ) csu on csu.pidm = i.spriden_pidm");
        builder.add(" where i.spriden_change_ind is null ");
        builder.add("   and i.spriden_id = '", studentId, "'");

        return executeSimpleQuery(conn, builder.toString());
    }

    /**
     * Builds a {@code LiveCsuCredit} from a {@code ResultSet}.
     *
     * @param conn the connection, in case the construction of records from the result set requires further queries
     * @param rs   the result set from which to construct the object
     * @return the {@code LiveCsuCredit} object
     * @throws SQLException if there was an error retrieving a field
     */
    @Override
    protected LiveCsuCredit constructFromResultSet(final DbConnection conn, final ResultSet rs) throws SQLException {

        final String theGrade = getString(rs, "GRADE");
        final String actualGrade;

        if (theGrade == null || theGrade.isEmpty()) {
            actualGrade = theGrade;
        } else {
            final char first = theGrade.charAt(0);

            if (first == 'R' || first == 'X') {
                actualGrade = theGrade.substring(1);
            } else if (theGrade.startsWith("AM")) {
                actualGrade = theGrade.substring(2);
            } else {
                actualGrade = theGrade;
            }
        }

        final String theCourse = getString(rs, "COURSE");
        final String actualCourse;

        if (theCourse == null || theCourse.isEmpty()) {
            actualCourse = theCourse;
        } else {
            final char first = theCourse.charAt(0);

            if (first == 'M' && theCourse.length() < 5) {
                actualCourse = "M " + theCourse.substring(1);
            } else if (theCourse.startsWith("MATH")) {
                actualCourse = "M " + theCourse.substring(4);
            } else {
                actualCourse = theCourse;
            }
        }

        try {
            return new LiveCsuCredit(ImplLiveStudent.convertTermCode(getString(rs, "TERM")),
                    getString(rs, "CSU_ID"), actualCourse,
                    getDouble(rs, "CREDITS"), actualGrade);
        } catch (final IllegalArgumentException ex) {
            throw new SQLException(ex);
        }
    }
}
