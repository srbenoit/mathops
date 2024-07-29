package dev.mathops.db.old.schema.csubanner;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.EqualityTests;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.rec.LiveReg;
import dev.mathops.db.old.schema.AbstractImpl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * The base class for live registration implementations.
 */
abstract class AbstractImplLiveReg extends AbstractImpl<LiveReg> {

    /** Numeric code for the Spring term. */
    private static final int SP_CODE = 10;

    /** Numeric code for the Summer term. */
    private static final int SM_CODE = 60;

    /** Numeric code for the Fall term. */
    private static final int FA_CODE = 90;

    /**
     * Constructs a new {@code AbstractImplLiveReg}.
     */
    AbstractImplLiveReg() {

        super();
    }

    /**
     * Handles request to clean (delete all rows from) a table in the live registration database, an operation which is
     * not permitted.
     *
     * @param cache the data cache
     * @throws SQLException if there is an error performing the query
     */
    @Override
    public final void clean(final Cache cache) throws SQLException {

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
    public final int count(final DbConnection conn) throws SQLException {

        return defaultCount(conn);
    }

    /**
     * Queries for all rows (not advisable).
     *
     * @param conn the database connection, checked out to this thread
     * @return the list of models that matched the criteria, a zero-length array if none matched
     * @throws SQLException if there is an error performing the query
     */
    @Override
    public final List<LiveReg> queryAll(final DbConnection conn) throws SQLException {

        return defaultQueryAll(conn);
    }

    /**
     * Builds a {@code LiveReg} from a {@code ResultSet}.
     *
     * @param conn the connection, in case the construction of records from the result set requires further queries
     * @param rs   the result set from which to construct the object
     * @return the {@code LiveReg} object
     * @throws SQLException if there was an error retrieving a field
     */
    @Override
    protected final LiveReg constructFromResultSet(final DbConnection conn, final ResultSet rs) throws SQLException {

        // NOTE: This is the STCOURSE term
        final TermKey term = convertTermCode(getString(rs, "TERM"));
        final TermKey gradTerm = convertTermCode(getString(rs, "ANTIC_GRAD_TERM"));

        String lastName = getString(rs, "LAST_NAME");
        if (lastName.length() > 30) {
            lastName = lastName.substring(0, 30);
        }

        String firstName = getString(rs, "FIRST_NAME");
        if (firstName.length() > 30) {
            firstName = firstName.substring(0, 30);
        }

        String trCred = getString(rs, "TRANSFER_CREDITS");
        final int index = trCred == null ? -1 : trCred.indexOf('.');
        if (trCred != null && index != -1) {
            // There is a decimal, so trim any trailing zeros
            while (trCred.charAt(trCred.length() - 1) == '0') {
                trCred = trCred.substring(0, trCred.length() - 1);
            }
            // Trim the decimal if it is the last character
            if (trCred.charAt(trCred.length() - 1) == '.') {
                trCred = trCred.substring(0, trCred.length() - 1);
            }
        }

        String gpa = getString(rs, "HS_GPA");
        if (gpa != null) {
            if (gpa.length() > 4) {
                gpa = gpa.substring(0, 4);
            }
        }

        final String subject = getString(rs, "SUBJECT_CODE");
        final String courseNum = getString(rs, "COURSE_NUMBER");
        final String sect = getString(rs, "SECTION");

        // FIXME: For MATH 125 and 126, sections 00N where N is greater than 2 will be the new courses, which use
        // FIXME: the full "MATH" prefix rather than just "M".  This is hard to get from data right now.

        final String courseId;
        if (courseNum == null) {
            courseId = EqualityTests.isNullOrEmpty(subject) ? CoreConstants.EMPTY : subject.charAt(0)
                    + CoreConstants.SPC;
        } else {
            final boolean isHighSection = sect.startsWith("00") && !"001".equals(sect) && !"002".equals(sect);
            if (isHighSection && ("125".equals(courseNum) || "126".equals(courseNum))) {
                courseId = EqualityTests.isNullOrEmpty(subject) ? CoreConstants.EMPTY : subject
                        + CoreConstants.SPC + courseNum;
            } else {
                courseId = EqualityTests.isNullOrEmpty(subject) ? CoreConstants.EMPTY : subject.charAt(0)
                        + CoreConstants.SPC + courseNum;
            }
        }

//        Log.info("SUBJECT_CODE=", subject, ", COURSE_NUMBER=", courseNum, " --> ", courseId);

        final String resid = "C".equals(getString(rs, "RESIDENCY")) ? "CO" : null;

        final String credType = getString(rs, "COURSE_CREDIT_TYPE");
        String instr = null;
        if ("M".equals(credType)) {
            instr = "RI";
        } else if ("MC".equals(credType)) {
            instr = "CE";
        }

        String xfer = null;
        if (!"0".equals(trCred)) {
            xfer = trCred;
        }

        return new LiveReg(term, gradTerm,
                getString(rs, "CSU_ID"),
                getInteger(rs, "PIDM"),
                lastName,
                firstName,
                getString(rs, "CLASS_LEVEL"),
                getString(rs, "COLLEGE"),
                getString(rs, "DEPT"),
                getString(rs, "MAJOR"),
                xfer,
                getString(rs, "HS_CODE"),
                gpa,
                getInteger(rs, "HS_CLASS_RANK"),
                getInteger(rs, "HS_CLASS_SIZE"),
                getInteger(rs, "ACT_SCORE"),
                getInteger(rs, "SAT_SCORE"),
                getInteger(rs, "SATR_SCORE"),
                getString(rs, "AP_SCORE"),
                resid,
                getDate(rs, "BIRTHDATE"),
                getString(rs, "GENDER"),
                getString(rs, "EMAIL"),
                getString(rs, "ADVISOR_EMAIL_ADDRESS"),
                getString(rs, "STUDENT_CAMPUS"),
                getString(rs, "ADMIT_TYPE"),
                courseId,
                sect,
                getString(rs, "GRADING_OPTION"),
                getString(rs, "REGISTRATION_STATUS"),
                instr);
    }

    /**
     * Converts a term code into term key.
     *
     * @param termCode the term code
     * @return the {@code TermKey} if successful; {@code null} if not
     */
    private static TermKey convertTermCode(final String termCode) {

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
        } else {
            Log.warning(Res.fmt(Res.BAD_TERM, termCode), termCode);
        }

        return result;
    }
}
