package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A raw "applicant" record.
 */
public final class RawApplicant extends RecBase implements Comparable<RawApplicant> {

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_FIRST_NAME = "first_name";

    /** A field name. */
    private static final String FLD_LAST_NAME = "last_name";

    /** A field name. */
    private static final String FLD_BIRTHDATE = "birthdate";

    /** A field name. */
    private static final String FLD_ETHNICITY = "ethnicity";

    /** A field name. */
    private static final String FLD_GENDER = "gender";

    /** A field name. */
    private static final String FLD_COLLEGE = "college";

    /** A field name. */
    private static final String FLD_PROG_STUDY = "prog_study";

    /** A field name. */
    private static final String FLD_HS_CODE = "hs_code";

    /** A field name. */
    private static final String FLD_TR_CREDITS = "tr_credits";

    /** A field name. */
    private static final String FLD_RESIDENT = "resident";

    /** A field name. */
    private static final String FLD_RESIDENT_STATE = "resident_state";

    /** A field name. */
    private static final String FLD_RESIDENT_COUNTY = "resident_county";

    /** A field name. */
    private static final String FLD_HS_GPA = "hs_gpa";

    /** A field name. */
    private static final String FLD_HS_CLASS_RANK = "hs_class_rank";

    /** A field name. */
    private static final String FLD_HS_SIZE_CLASS = "hs_size_class";

    /** A field name. */
    private static final String FLD_ACT_SCORE = "act_score";

    /** A field name. */
    private static final String FLD_SAT_SCORE = "sat_score";

    /** A field name. */
    private static final String FLD_PIDM = "pidm";

    /** A field name. */
    private static final String FLD_APLN_TERM = "apln_term";

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'first_name' field value. */
    public String firstName;

    /** The 'last_name' field value. */
    public String lastName;

    /** The 'birthdate' field value. */
    public LocalDate birthdate;

    /** The 'ethnicity' field value. */
    public String ethnicity;

    /** The 'gender' field value. */
    public String gender;

    /** The 'college' field value. */
    public String college;

    /** The 'prog_study' field value. */
    public String progStudy;

    /** The 'hs_code' field value. */
    public String hsCode;

    /** The 'tr_credits' field value. */
    public String trCredits;

    /** The 'resident' field value. */
    public String resident;

    /** The 'resident_state' field value. */
    public String residentState;

    /** The 'resident_county' field value. */
    public String residentCounty;

    /** The 'hs_gpa' field value. */
    public String hsGpa;

    /** The 'hs_class_rank' field value. */
    public Integer hsClassRank;

    /** The 'hs_size_class' field value. */
    public Integer hsSizeClass;

    /** The 'act_score' field value. */
    public Integer actScore;

    /** The 'sat_score' field value. */
    public Integer satScore;

    /** The 'pidm' field value. */
    public Integer pidm;

    /** The 'apln_term' field value. */
    public TermKey aplnTerm;

    /**
     * Constructs a new {@code RawApplicant}.
     */
    public RawApplicant() {

        super();
    }

    /**
     * Constructs a new {@code RawApplicant}.
     *
     * @param theStuId          the 'stu_id' field value
     * @param theFirstName      the 'first_name' field value
     * @param theLastName       the 'last_name' field value
     * @param theBirthdate      the 'birthdate' field value
     * @param theEthnicity      the 'ethnicity' field value
     * @param theGender         the 'gender' field value
     * @param theCollege        the 'college' field value
     * @param theProgStudy      the 'program_study' field value
     * @param theHsCode         the 'hs_code' field value
     * @param theTrCredits      the 'tr_credits' field value
     * @param theResident       the 'resident' field value
     * @param theResidentState  the 'resident_state' field value
     * @param theResidentCounty the 'resident_county' field value
     * @param theHsGpa          the 'hs_gpa' field value
     * @param theHsClassRank    the 'hs_class_rank' field value
     * @param theHsSizeClass    the 'hs_size_class' field value
     * @param theActScore       the 'act_score' field value
     * @param theSatScore       the 'sat_score ' field value
     * @param thePidm           the 'pidm' field value
     * @param theAplnTerm       the 'apln_term' field value
     */
    public RawApplicant(final String theStuId, final String theFirstName, final String theLastName,
                        final LocalDate theBirthdate, final String theEthnicity, final String theGender,
                        final String theCollege, final String theProgStudy, final String theHsCode,
                        final String theTrCredits, final String theResident, final String theResidentState,
                        final String theResidentCounty, final String theHsGpa, final Integer theHsClassRank,
                        final Integer theHsSizeClass, final Integer theActScore, final Integer theSatScore,
                        final Integer thePidm, final TermKey theAplnTerm) {

        super();

        this.stuId = theStuId;
        this.firstName = theFirstName;
        this.lastName = theLastName;
        this.birthdate = theBirthdate;
        this.ethnicity = theEthnicity;
        this.gender = theGender;
        this.college = theCollege;
        this.progStudy = theProgStudy;
        this.hsCode = theHsCode;
        this.trCredits = theTrCredits;
        this.resident = theResident;
        this.residentState = theResidentState;
        this.residentCounty = theResidentCounty;
        this.hsGpa = theHsGpa;
        this.hsClassRank = theHsClassRank;
        this.hsSizeClass = theHsSizeClass;
        this.actScore = theActScore;
        this.satScore = theSatScore;
        this.pidm = thePidm;
        this.aplnTerm = theAplnTerm;
    }

    /**
     * Extracts a "student" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawApplicant fromResultSet(final ResultSet rs) throws SQLException {

        final RawApplicant result = new RawApplicant();

        result.stuId = getStringField(rs, FLD_STU_ID);
        result.firstName = getStringField(rs, FLD_FIRST_NAME);
        result.lastName = getStringField(rs, FLD_LAST_NAME);
        result.birthdate = getDateField(rs, FLD_BIRTHDATE);
        result.ethnicity = getStringField(rs, FLD_ETHNICITY);
        result.gender = getStringField(rs, FLD_GENDER);
        result.college = getStringField(rs, FLD_COLLEGE);
        result.progStudy = getStringField(rs, FLD_PROG_STUDY);
        result.hsCode = getStringField(rs, FLD_HS_CODE);
        result.trCredits = getStringField(rs, FLD_TR_CREDITS);
        result.resident = getStringField(rs, FLD_RESIDENT);
        result.residentState = getStringField(rs, FLD_RESIDENT_STATE);
        result.residentCounty = getStringField(rs, FLD_RESIDENT_COUNTY);
        result.hsGpa = getStringField(rs, FLD_HS_GPA);
        result.hsClassRank = getIntegerField(rs, FLD_HS_CLASS_RANK);
        result.hsSizeClass = getIntegerField(rs, FLD_HS_SIZE_CLASS);
        result.actScore = getIntegerField(rs, FLD_ACT_SCORE);
        result.satScore = getIntegerField(rs, FLD_SAT_SCORE);
        result.pidm = getIntegerField(rs, FLD_PIDM);

        final String termStr = getStringField(rs, FLD_APLN_TERM);
        result.aplnTerm = termStr == null ? null : TermKey.parseNumericString(termStr);

        return result;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final RawApplicant o) {

        int result = compareAllowingNull(this.lastName.toUpperCase(), o.lastName.toUpperCase());

        if (result == 0) {
            result = compareAllowingNull(this.firstName.toUpperCase(), o.firstName.toUpperCase());
            if (result == 0) {
                result = compareAllowingNull(this.stuId, o.stuId);
            }
        }

        return result;
    }

    /**
     * Generates a string serialization of the record. Each concrete subclass should have a constructor that accepts a
     * single {@code String} to reconstruct the object from this string.
     *
     * @return the string
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(40);

        appendField(htm, FLD_STU_ID, this.stuId);
        htm.add(DIVIDER);
        appendField(htm, FLD_FIRST_NAME, this.firstName);
        htm.add(DIVIDER);
        appendField(htm, FLD_LAST_NAME, this.lastName);
        htm.add(DIVIDER);
        appendField(htm, FLD_BIRTHDATE, this.birthdate);
        htm.add(DIVIDER);
        appendField(htm, FLD_ETHNICITY, this.ethnicity);
        htm.add(DIVIDER);
        appendField(htm, FLD_GENDER, this.gender);
        htm.add(DIVIDER);
        appendField(htm, FLD_COLLEGE, this.college);
        htm.add(DIVIDER);
        appendField(htm, FLD_PROG_STUDY, this.progStudy);
        htm.add(DIVIDER);
        appendField(htm, FLD_HS_CODE, this.hsCode);
        htm.add(DIVIDER);
        appendField(htm, FLD_TR_CREDITS, this.trCredits);
        htm.add(DIVIDER);
        appendField(htm, FLD_RESIDENT, this.resident);
        htm.add(DIVIDER);
        appendField(htm, FLD_RESIDENT_STATE, this.residentState);
        htm.add(DIVIDER);
        appendField(htm, FLD_RESIDENT_COUNTY, this.residentCounty);
        htm.add(DIVIDER);
        appendField(htm, FLD_HS_GPA, this.hsGpa);
        htm.add(DIVIDER);
        appendField(htm, FLD_HS_CLASS_RANK, this.hsClassRank);
        htm.add(DIVIDER);
        appendField(htm, FLD_HS_SIZE_CLASS, this.hsSizeClass);
        htm.add(DIVIDER);
        appendField(htm, FLD_ACT_SCORE, this.actScore);
        htm.add(DIVIDER);
        appendField(htm, FLD_SAT_SCORE, this.satScore);
        htm.add(DIVIDER);
        appendField(htm, FLD_PIDM, this.pidm);
        htm.add(DIVIDER);
        appendField(htm, FLD_APLN_TERM, this.aplnTerm);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.stuId)
                + Objects.hashCode(this.firstName)
                + Objects.hashCode(this.lastName)
                + Objects.hashCode(this.birthdate)
                + Objects.hashCode(this.ethnicity)
                + Objects.hashCode(this.gender)
                + Objects.hashCode(this.college)
                + Objects.hashCode(this.progStudy)
                + Objects.hashCode(this.hsCode)
                + Objects.hashCode(this.trCredits)
                + Objects.hashCode(this.resident)
                + Objects.hashCode(this.residentState)
                + Objects.hashCode(this.residentCounty)
                + Objects.hashCode(this.hsGpa)
                + Objects.hashCode(this.hsClassRank)
                + Objects.hashCode(this.hsSizeClass)
                + Objects.hashCode(this.actScore)
                + Objects.hashCode(this.satScore)
                + Objects.hashCode(this.pidm)
                + Objects.hashCode(this.aplnTerm);
    }

    /**
     * Tests whether this object is equal to another.
     *
     * @param obj the other object
     * @return true if equal; false if not
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final RawApplicant rec) {
            equal = Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.firstName, rec.firstName)
                    && Objects.equals(this.lastName, rec.lastName)
                    && Objects.equals(this.birthdate, rec.birthdate)
                    && Objects.equals(this.ethnicity, rec.ethnicity)
                    && Objects.equals(this.gender, rec.gender)
                    && Objects.equals(this.college, rec.college)
                    && Objects.equals(this.progStudy, rec.progStudy)
                    && Objects.equals(this.hsCode, rec.hsCode)
                    && Objects.equals(this.trCredits, rec.trCredits)
                    && Objects.equals(this.resident, rec.resident)
                    && Objects.equals(this.residentState, rec.residentState)
                    && Objects.equals(this.residentCounty, rec.residentCounty)
                    && Objects.equals(this.hsGpa, rec.hsGpa)
                    && Objects.equals(this.hsClassRank, rec.hsClassRank)
                    && Objects.equals(this.hsSizeClass, rec.hsSizeClass)
                    && Objects.equals(this.actScore, rec.actScore)
                    && Objects.equals(this.satScore, rec.satScore)
                    && Objects.equals(this.pidm, rec.pidm)
                    && Objects.equals(this.aplnTerm, rec.aplnTerm);
        } else {
            equal = false;
        }

        return equal;
    }
}
