package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
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
public enum RawFinalCrollLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawFinalCroll record) throws SQLException {

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
                LogicUtils.sqlStringValue(record.stuId), ",",
                LogicUtils.sqlStringValue(record.course), ",",
                LogicUtils.sqlStringValue(record.sect), ",",
                LogicUtils.sqlStringValue(record.termKey.termCode), ",",
                LogicUtils.sqlIntegerValue(record.termKey.shortYear), ",",
                LogicUtils.sqlIntegerValue(record.paceOrder), ",",
                LogicUtils.sqlStringValue(record.openStatus), ",",
                LogicUtils.sqlStringValue(record.gradingOption), ",",
                LogicUtils.sqlStringValue(record.completed), ",",
                LogicUtils.sqlIntegerValue(record.score), ",",
                LogicUtils.sqlStringValue(record.courseGrade), ",",
                LogicUtils.sqlStringValue(record.prereqSatis), ",",
                LogicUtils.sqlStringValue(record.initClassRoll), ",",
                LogicUtils.sqlStringValue(record.stuProvided), ",",
                LogicUtils.sqlStringValue(record.finalClassRoll), ",",
                LogicUtils.sqlStringValue(record.examPlaced), ",",
                LogicUtils.sqlIntegerValue(record.zeroUnit), ",",
                LogicUtils.sqlFloatValue(record.timeoutFactor), ",",
                LogicUtils.sqlStringValue(record.forfeitI), ",",
                LogicUtils.sqlStringValue(record.iInProgress), ",",
                LogicUtils.sqlStringValue(record.iCounted), ",",
                LogicUtils.sqlStringValue(record.ctrlTest), ",",
                LogicUtils.sqlDateValue(record.deferredFDt), ",",
                LogicUtils.sqlIntegerValue(record.bypassTimeout), ",",
                LogicUtils.sqlStringValue(record.instrnType), ",",
                LogicUtils.sqlStringValue(record.registrationStatus), ",",
                LogicUtils.sqlDateValue(record.lastClassRollDt), ",",
                LogicUtils.sqlStringValue(record.iTermKey == null ? null : record.iTermKey.termCode), ",",
                LogicUtils.sqlIntegerValue(record.iTermKey == null ? null : record.iTermKey.shortYear), ",",
                LogicUtils.sqlDateValue(record.iDeadlineDt), ")");

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql) == 1;

            if (result) {
                conn.commit();
            } else {
                conn.rollback();
            }

            return result;
        } finally {
            Cache.checkInConnection(conn);
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
    public static boolean delete(final Cache cache, final RawFinalCroll record) throws SQLException {

        final boolean result;

        final HtmlBuilder builder = new HtmlBuilder(100);

        builder.add("DELETE FROM final_croll",
                " WHERE stu_id=", LogicUtils.sqlStringValue(record.stuId),
                "   AND course=", LogicUtils.sqlStringValue(record.course),
                "   AND sect=", LogicUtils.sqlStringValue(record.sect),
                "   AND term=", LogicUtils.sqlStringValue(record.termKey.termCode),
                "   AND term_yr=", LogicUtils.sqlIntegerValue(record.termKey.shortYear));

        if (record.openStatus == null) {
            builder.add("   AND open_status IS NULL");
        } else {
            builder.add("   AND open_status=", LogicUtils.sqlStringValue(record.openStatus));
        }

        if (record.lastClassRollDt == null) {
            builder.add("   AND last_class_roll_dt IS NULL");
        } else {
            builder.add("   AND last_class_roll_dt=", LogicUtils.sqlDateValue(record.lastClassRollDt));
        }

        final String sql = builder.toString();

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement()) {
            result = stmt.executeUpdate(sql) == 1;

            if (result) {
                conn.commit();
            } else {
                conn.rollback();
            }
        } finally {
            Cache.checkInConnection(conn);
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
    public static List<RawFinalCroll> queryAll(final Cache cache) throws SQLException {

        final String sql = "SELECT * FROM final_croll";

        final List<RawFinalCroll> result = new ArrayList<>(1000);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawFinalCroll.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }
}
