package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.old.rawrecord.RawFinalCroll;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "final_croll" records.
 *
 * <pre>
 * Table:  'final_croll'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * stu_id               char(9)                   no      PK
 * course               char(6)                   no      PK
 * sect                 char(4)                   no      PK
 * term                 char(2)                   no      PK
 * term_yr              smallint                  no      PK
 * pace_order           smallint                  yes
 * open_status          char(1)                   yes
 * grading_option       char(2)                   yes
 * completed            char(1)                   no
 * score                smallint                  yes
 * course_grade         char(2)                   yes
 * prereq_satis         char(1)                   yes
 * init_class_roll      char(1)                   no
 * stu_provided         char(1)                   yes
 * final_class_roll     char(1)                   no
 * exam_placed          char(1)                   yes
 * zero_unit            smallint                  yes
 * timeout_factor       decimal(3,2)              yes
 * forfeit_i            char(1)                   yes
 * i_in_progress        char(1)                   no
 * i_counted            char(1)                   yes
 * ctrl_test            char(1)                   no
 * deferred_f_dt        date                      yes
 * bypass_timeout       smallint                  no
 * instrn_type          char(2)                   yes
 * registration_stat+   char(2)                   yes
 * last_class_roll_dt   date                      yes      PK
 * i_term               char(2)                   yes
 * i_term_yr            smallint                  yes
 * i_deadline_dt        date                      yes
 * </pre>
 */
public final class RawFinalCrollLogic extends AbstractRawLogic<RawFinalCroll> {

    /** A single instance. */
    public static final RawFinalCrollLogic INSTANCE = new RawFinalCrollLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawFinalCrollLogic() {

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
    public boolean insert(final Cache cache, final RawFinalCroll record) throws SQLException {

        if (record.stuId == null || record.course == null || record.sect == null || record.termKey == null
                || record.completed == null || record.initClassRoll == null || record.finalClassRoll == null
                || record.iInProgress == null || record.ctrlTest == null || record.bypassTimeout == null
                || record.lastClassRollDt == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO final_croll (stu_id,course,sect,term,term_yr,pace_order,open_status,grading_option,",
                "completed,score,course_grade,prereq_satis,init_class_roll,stu_provided,final_class_roll,exam_placed,",
                "zero_unit,timeout_factor,forfeit_i,i_in_progress,i_counted,ctrl_test,deferred_f_dt,bypass_timeout,",
                "instrn_type,registration_status,last_class_roll_dt,i_term,i_term_yr,i_deadline_dt) VALUES (",
                sqlStringValue(record.stuId), ",",
                sqlStringValue(record.course), ",",
                sqlStringValue(record.sect), ",",
                sqlStringValue(record.termKey.termCode), ",",
                sqlIntegerValue(record.termKey.shortYear), ",",
                sqlIntegerValue(record.paceOrder), ",",
                sqlStringValue(record.openStatus), ",",
                sqlStringValue(record.gradingOption), ",",
                sqlStringValue(record.completed), ",",
                sqlIntegerValue(record.score), ",",
                sqlStringValue(record.courseGrade), ",",
                sqlStringValue(record.prereqSatis), ",",
                sqlStringValue(record.initClassRoll), ",",
                sqlStringValue(record.stuProvided), ",",
                sqlStringValue(record.finalClassRoll), ",",
                sqlStringValue(record.examPlaced), ",",
                sqlIntegerValue(record.zeroUnit), ",",
                sqlFloatValue(record.timeoutFactor), ",",
                sqlStringValue(record.forfeitI), ",",
                sqlStringValue(record.iInProgress), ",",
                sqlStringValue(record.iCounted), ",",
                sqlStringValue(record.ctrlTest), ",",
                sqlDateValue(record.deferredFDt), ",",
                sqlIntegerValue(record.bypassTimeout), ",",
                sqlStringValue(record.instrnType), ",",
                sqlStringValue(record.registrationStatus), ",",
                sqlDateValue(record.lastClassRollDt), ",",
                sqlStringValue(record.iTermKey == null ? null : record.iTermKey.termCode), ",",
                sqlIntegerValue(record.iTermKey == null ? null : record.iTermKey.shortYear), ",",
                sqlDateValue(record.iDeadlineDt), ")");

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
    public boolean delete(final Cache cache, final RawFinalCroll record) throws SQLException {

        final boolean result;

        final HtmlBuilder builder = new HtmlBuilder(100);

        builder.add("DELETE FROM final_croll",
                " WHERE stu_id=", sqlStringValue(record.stuId),
                "   AND course=", sqlStringValue(record.course),
                "   AND sect=", sqlStringValue(record.sect),
                "   AND term=", sqlStringValue(record.termKey.termCode),
                "   AND term_yr=", sqlIntegerValue(record.termKey.shortYear));

        if (record.openStatus == null) {
            builder.add("   AND open_status IS NULL");
        } else {
            builder.add("   AND open_status=", sqlStringValue(record.openStatus));
        }

        if (record.lastClassRollDt == null) {
            builder.add("   AND last_class_roll_dt IS NULL");
        } else {
            builder.add("   AND last_class_roll_dt=", sqlDateValue(record.lastClassRollDt));
        }

        final String sql = builder.toString();

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
    public List<RawFinalCroll> queryAll(final Cache cache) throws SQLException {

        final String sql = "SELECT * FROM final_croll";

        final List<RawFinalCroll> result = new ArrayList<>(1000);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawFinalCroll.fromResultSet(rs));
            }
        }

        return result;
    }
}
