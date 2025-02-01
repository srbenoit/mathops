package dev.mathops.db.old.schema.csubanner;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.ifaces.ILiveStudent;
import dev.mathops.db.rec.LiveStudent;
import dev.mathops.db.old.schema.AbstractImpl;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementation of the {@code ILiveStudent} interface for the CSU Banner schema.
 */
public final class ImplLiveStudent extends AbstractImpl<LiveStudent> implements ILiveStudent {

    /** The single instance. */
    public static final ImplLiveStudent INSTANCE = new ImplLiveStudent();

    /** Numeric code for the Spring term. */
    private static final int SP_CODE = 10;

    /** Numeric code for the Summer term. */
    private static final int SM_CODE = 60;

    /** Numeric code for the Fall term. */
    private static final int FA_CODE = 90;

    /** The name of the primary table. */
    private static final String TABLE_NAME = "Artificial";

    /**
     * Constructs a new {@code ImplLiveStudent}.
     */
    private ImplLiveStudent() {

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
    public List<LiveStudent> queryAll(final DbConnection conn) throws SQLException {

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
    public List<LiveStudent> query(final DbConnection conn, final String studentId) throws SQLException {

        final HtmlBuilder builder = new HtmlBuilder(1000);

        builder.add("select distinct");
        builder.add("    i.spriden_id                    CSUID,");
        builder.add("    i.spriden_pidm                  PIDM,");
        builder.add("    i.spriden_last_name             LAST_NAME,");
        builder.add("    i.spriden_first_name            FIRST_NAME,");
        builder.add("    p.spbpers_pref_first_name       PREF_FIRST,");
        builder.add("    i.spriden_mi                    MI,");
        builder.add("    stu.collcode                    COLL_CODE,");
        builder.add("    stu.deptcode                    DEPT_CODE,");
        builder.add("    stu.progcode                    PROG_CODE,");
        builder.add("    stu.majrcode                    MAJOR_CODE,");
        builder.add("    hs.hscode                       HS_CODE,");
        builder.add("    hs.hsgpa                        HS_GPA,");
        builder.add("    hs.hsrank                       HS_CLASS_RANK,");
        builder.add("    hs.hssize                       HS_CLASS_SIZE,");
        builder.add("    act.testscore                   ACT_SCORE,");
        builder.add("    sat.testscore                   SAT_SCORE,");
        builder.add("    satr.testscore                  SATR_SCORE,");
        builder.add("    stu.resd                        RESIDENCY,");
        builder.add("    stu.admitterm                   ADMIT_TERM,");
        builder.add("    stu.admittype                   ADMIT_TYPE,");
        builder.add("    stu.graddate                    GRAD_DATE,");
        builder.add("    p.spbpers_birth_date            BIRTHDATE,");
        builder.add("    p.spbpers_sex                   GENDER,");
        builder.add("    e.proper_name_email             EMAIL,");
        builder.add("    e.state                         ADDR_STATE,");
        builder.add("    adv.email                       ADVISOR_EMAIL_ADDRESS,");
        builder.add("    stu.campus                      STUDENT_CAMPUS");
        builder.add(" from spriden i");
        builder.add(" join csug_spbpers_web_v p on p.spbpers_pidm = i.spriden_pidm");
        builder.add(" left join (select g.sgbstdn_pidm pidm,");
        builder.add("                   g.sgbstdn_term_code_eff,");
        builder.add("                   g.sgbstdn_camp_code       campus,");
        builder.add("                   g.sgbstdn_program_1       progcode,");
        builder.add("                   g.sgbstdn_majr_code_1     majrcode,");
        builder.add("                   g.sgbstdn_coll_code_1     collcode,");
        builder.add("                   g.sgbstdn_term_code_admit admitterm,");
        builder.add("                   g.sgbstdn_exp_grad_date   graddate,");
        builder.add("                   g.sgbstdn_admt_code       admittype,");
        builder.add("                   g.sgbstdn_dept_code       deptcode,");
        builder.add("                   g.sgbstdn_resd_code       resd");
        builder.add("           from sgbstdn g");
        builder.add("           where g.sgbstdn_term_code_eff = ");
        builder.add("                 (select max(g1.sgbstdn_term_code_eff)");
        builder.add("                  from sgbstdn g1");
        builder.add("                  where g1.sgbstdn_pidm = g.sgbstdn_pidm");
        builder.add("                    and g1.sgbstdn_stst_code='AS')");
        builder.add("          ) stu on stu.pidm = i.spriden_pidm");
        builder.add(" left join (select h.sorhsch_pidm pidm,");
        builder.add("            h.sorhsch_sbgi_code hscode, v.sovsbgv_desc hsdesc,");
        builder.add("            h.sorhsch_gpa hsgpa, h.sorhsch_class_rank hsrank,");
        builder.add("            h.sorhsch_class_size hssize");
        builder.add("           from sorhsch h");
        builder.add("                left join sovsbgv v on");
        builder.add("                     v.sovsbgv_code = h.sorhsch_sbgi_code");
        builder.add("           where h.sorhsch_activity_date =");
        builder.add("               (select max(h1.sorhsch_activity_date) from sorhsch h1");
        builder.add("                where h1.sorhsch_pidm = h.sorhsch_pidm)");
        builder.add("          ) hs on hs.pidm = i.spriden_pidm");
        builder.add(" left join (select ta.sortest_pidm pidm,");
        builder.add("                   ta.sortest_test_score testscore");
        builder.add("           from sortest ta");
        builder.add("           where ta.sortest_test_score =");
        builder.add("               (select max(ta1.sortest_test_score) from sortest ta1");
        builder.add("                where ta1.sortest_pidm = ta.sortest_pidm");
        builder.add("                  and ta1.sortest_tesc_code = ta.sortest_tesc_code)");
        builder.add("             and ta.sortest_tesc_code = 'A02'");
        builder.add("          ) act on act.pidm = i.spriden_pidm");
        builder.add(" left join (select ts.sortest_pidm pidm,");
        builder.add("                   ts.sortest_test_score testscore");
        builder.add("           from sortest ts");
        builder.add("           where ts.sortest_test_score =");
        builder.add("               (select max(ts1.sortest_test_score) from sortest ts1");
        builder.add("                where ts1.sortest_pidm = ts.sortest_pidm");
        builder.add("                  and ts1.sortest_tesc_code = ts.sortest_tesc_code)");
        builder.add("             and ts.sortest_tesc_code = 'S02'");
        builder.add("          ) sat on sat.pidm = i.spriden_pidm");
        builder.add(" left join (select tsr.sortest_pidm pidm,");
        builder.add("                   tsr.sortest_test_score testscore");
        builder.add("           from sortest tsr");
        builder.add("           where tsr.sortest_test_score =");
        builder.add("             (select max(tsr1.sortest_test_score) from sortest tsr1");
        builder.add("              where tsr1.sortest_pidm = tsr.sortest_pidm");
        builder.add("                and tsr1.sortest_tesc_code = tsr.sortest_tesc_code)");
        builder.add("           and tsr.sortest_tesc_code = 'S12'");
        builder.add("          ) satr on satr.pidm = i.spriden_pidm ");
        builder.add(" left join csuban.midp_directory e");
        builder.add("        on  e.pidm = i.spriden_pidm");
        builder.add(" left join (select a.sgradvr_pidm pidm, ea.proper_name_email email");
        builder.add("       from sgradvr a ");
        builder.add("       left join csuban.midp_directory ea on");
        builder.add("                 ea.pidm = a.sgradvr_advr_pidm");
        builder.add("       where a.sgradvr_advr_code = 'MAJR'");
        builder.add("         and a.sgradvr_prim_ind = 'Y'");
        builder.add("         and a.sgradvr_term_code_eff =");
        builder.add("             (select max(a1.sgradvr_term_code_eff) from sgradvr a1");
        builder.add("              where a1.sgradvr_pidm = a.sgradvr_pidm)");
        builder.add("          ) adv on adv.pidm = i.spriden_pidm");
        builder.add("  where i.spriden_change_ind is null");
        builder.add("    and i.spriden_id = '", studentId, "'");

        return executeSimpleQuery(conn, builder.toString());
    }

    /**
     * Converts a term code into a {@code TermKey}.
     *
     * @param termCode the term code
     * @return {@code TermKey} if successful; {@code null} if not
     */
    static TermKey convertTermCode(final String termCode) {

        TermKey result = null;

        if (termCode != null && termCode.length() == 6) {
            try {
                ETermName name = null;

                final int whichTerm = Integer.parseInt(termCode.substring(4));
                if (whichTerm == SP_CODE) {
                    name = ETermName.SPRING;
                } else if (whichTerm == SM_CODE) {
                    name = ETermName.SUMMER;
                } else if (whichTerm == FA_CODE) {
                    name = ETermName.FALL;
                } else {
                    Log.warning(Res.fmt(Res.BAD_TERM, termCode));
                }

                if (name != null) {
                    result = new TermKey(name, Integer.valueOf(termCode.substring(0, 4)));
                }
            } catch (final NumberFormatException ex) {
                Log.warning(Res.fmt(Res.BAD_TERM, termCode), ex);
            }
        } else if (termCode == null) {
            Log.warning(Res.get(Res.NULL_TERM));
        } else {
            Log.warning(Res.fmt(Res.BAD_TERM, termCode));
        }

        return result;
    }

    /**
     * Builds a {@code LiveStudent} from a {@code ResultSet}.
     *
     * @param conn the connection, in case the construction of records from the result set requires further queries
     * @param rs   the result set from which to construct the object
     * @return the {@code LiveStudent} object
     * @throws SQLException if there was an error retrieving a field
     */
    @Override
    protected LiveStudent constructFromResultSet(final DbConnection conn, final ResultSet rs) throws SQLException {

        final String pidmString = getString(rs, "PIDM");
        Integer pidm = null;
        if (pidmString != null) {
            try {
                pidm = Integer.valueOf(pidmString);
            } catch (final NumberFormatException ex) {
                Log.warning("Invalid PIDM: ", pidmString);
            }
        }

        final String admitTermStr = getString(rs, "ADMIT_TERM");
        final TermKey admitTerm = admitTermStr == null ? null : convertTermCode(admitTermStr);

        try {
            String mi = getString(rs, "MI");
            if (mi != null && mi.length() > 1) {
                mi = mi.substring(0, 1);
            }

            return new LiveStudent(
                    getString(rs, "CSUID"),
                    pidm,
                    getString(rs, "LAST_NAME"),
                    getString(rs, "FIRST_NAME"),
                    getString(rs, "PREF_FIRST"),
                    mi,
                    getString(rs, "COLL_CODE"),
                    getString(rs, "DEPT_CODE"),
                    getString(rs, "PROG_CODE"),
                    getString(rs, "MAJOR_CODE"),
                    null,
                    getString(rs, "HS_CODE"),
                    getString(rs, "HS_GPA"),
                    getInteger(rs, "HS_CLASS_RANK"),
                    getInteger(rs, "HS_CLASS_SIZE"),
                    getInteger(rs, "ACT_SCORE"),
                    getInteger(rs, "SAT_SCORE"),
                    getInteger(rs, "SATR_SCORE"),
                    getString(rs, "RESIDENCY"), admitTerm,
                    getString(rs, "ADMIT_TYPE"),
                    getDate(rs, "GRAD_DATE"),
                    getDate(rs, "BIRTHDATE"),
                    getString(rs, "GENDER"),
                    getString(rs, "EMAIL"),
                    getString(rs, "ADVISOR_EMAIL_ADDRESS"),
                    getString(rs, "ADDR_STATE"),
                    getString(rs, "STUDENT_CAMPUS"));
        } catch (final IllegalArgumentException ex) {
            throw new SQLException(ex);
        }
    }
}
