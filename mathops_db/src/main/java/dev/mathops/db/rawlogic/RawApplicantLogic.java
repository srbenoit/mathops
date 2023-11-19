package dev.mathops.db.rawlogic;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.db.Cache;
import dev.mathops.db.rawrecord.RawApplicant;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to look up applicant records.
 *
 * <pre>
 * Table:  'applicant'
 *
 * Column name          Type              Nulls   Key
 * -------------------  ----------------  ------  -----
 * stu_id               char(9)           no      PK
 * first_name           char(30)          no
 * last_name            char(30)          no
 * birthdate            date              yes
 * ethnicity            char(2)           yes
 * gender               char(1)           yes
 * college              char(2)           yes
 * prog_study           char(16)          yes
 * hs_code              char(6)           yes
 * tr_credits           char(5)           yes
 * resident             char(4)           yes
 * resident_state       char(4)           yes
 * resident_county      char(6)           yes
 * hs_gpa               char(4)           yes
 * hs_class_rank        smallint          yes
 * hs_size_class        smallint          yes
 * act_score            smallint          yes
 * sat_score            smallint          yes
 * pidm                 integer           no
 * apln_term            char(6)           no
 * </pre>
 */
public final class RawApplicantLogic extends AbstractRawLogic<RawApplicant> {

    /** A single instance. */
    public static final RawApplicantLogic INSTANCE = new RawApplicantLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawApplicantLogic() {

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
    public boolean insert(final Cache cache, final RawApplicant record) throws SQLException {

        if (record.stuId == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final boolean result;

        if (record.stuId.startsWith("99")) {
            result = false;
        } else {
            final int aplnValue = record.aplnTerm.toNumeric();
            final String apln = Integer.toString(aplnValue);

            final String sql = SimpleBuilder.concat(
                    "INSERT INTO applicant (stu_id,first_name,last_name,birthdate,ethnicity,gender,college,prog_study,",
                    "hs_code,tr_credits,resident,resident_state,resident_county,hs_gpa,hs_class_rank,hs_size_class,",
                    "act_score,sat_score,pidm,apln_term) VALUES (",
                    sqlStringValue(record.stuId), ",",
                    sqlStringValue(record.firstName), ",",
                    sqlStringValue(record.lastName), ",",
                    sqlDateValue(record.birthdate), ",",
                    sqlStringValue(record.ethnicity), ",",
                    sqlStringValue(record.gender), ",",
                    sqlStringValue(record.college), ",",
                    sqlStringValue(record.progStudy), ",",
                    sqlStringValue(record.hsCode), ",",
                    sqlStringValue(record.trCredits), ",",
                    sqlStringValue(record.resident), ",",
                    sqlStringValue(record.residentState), ",",
                    sqlStringValue(record.residentCounty), ",",
                    sqlStringValue(record.hsGpa), ",",
                    sqlIntegerValue(record.hsClassRank), ",",
                    sqlIntegerValue(record.hsSizeClass), ",",
                    sqlIntegerValue(record.actScore), ",",
                    sqlIntegerValue(record.satScore), ",",
                    sqlIntegerValue(record.pidm), ",",
                    sqlStringValue(apln), ")");

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
    public boolean delete(final Cache cache, final RawApplicant record) throws SQLException {

        final boolean result;

        final HtmlBuilder sql = new HtmlBuilder(100);

        sql.add("DELETE FROM applicant WHERE stu_id=", sqlStringValue(record.stuId));

        try (final Statement stmt = cache.conn.createStatement()) {
            result = stmt.executeUpdate(sql.toString()) == 1;

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
    public List<RawApplicant> queryAll(final Cache cache) throws SQLException {

        return executeQuery(cache, "SELECT * FROM applicant");
    }

    /**
     * Gets all applicant records for a student.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return the list of admin_hold records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawApplicant> queryByStudent(final Cache cache, final String stuId)
            throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM applicant WHERE stu_id=", sqlStringValue(stuId));

        return executeQuery(cache, sql);
    }

    /**
     * Executes a query that returns a list of records.
     *
     * @param cache the data cache
     * @param sql   the SQL to execute
     * @return the list of matching records
     * @throws SQLException if there is an error accessing the database
     */
    private static List<RawApplicant> executeQuery(final Cache cache, final String sql) throws SQLException {

        final List<RawApplicant> result = new ArrayList<>(50);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawApplicant.fromResultSet(rs));
            }
        }

        return result;
    }
}
