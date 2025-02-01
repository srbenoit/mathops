package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * An immutable record of an analytics student record.
 */
public final class AnalyticsStudent extends RecBase implements Comparable<AnalyticsStudent> {

    /** A field name. */
    private static final String FLD_PIDM = "pidm";

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_LAST_NAME = "last_name";

    /** A field name. */
    private static final String FLD_FIRST_NAME = "first_name";

    /** A field name. */
    private static final String FLD_BIRTH_DATE = "birth_date";

    /** A field name. */
    private static final String FLD_GPA = "gpa";

    /** A field name. */
    private static final String FLD_SEX = "sex";

    /** A field name. */
    private static final String FLD_HISP_LAT_RACE = "hisp_lat_race";

    /** A field name. */
    private static final String FLD_AMER_IND_RACE = "maer_ind_race";

    /** A field name. */
    private static final String FLD_ASIAN_RACE = "asian_race";

    /** A field name. */
    private static final String FLD_BLACK_RACE = "black_race";

    /** A field name. */
    private static final String FLD_HAWAIIAN_RACE = "hawaiian_race";

    /** A field name. */
    private static final String FLD_WHITE_RACE = "white_race";

    /** A field name. */
    private static final String FLD_MULTI_RACE = "multi_race";

    /** A field name. */
    private static final String FLD_APLN_TERM = "apln_term";

    /** A field name. */
    private static final String FLD_ADMIT_TERM = "admit_term";

    /** A field name. */
    private static final String FLD_ADMIT_PROGRAM = "admit_program";

    /** A field name. */
    private static final String FLD_HS_CODE = "hs_code";

    /** A field name. */
    private static final String FLD_HS_GRAD_DATE = "hs_grad_date";

    /** A field name. */
    private static final String FLD_HS_GPA = "hs_gpa";

    /** A field name. */
    private static final String FLD_HS_CLASS_RANK = "hs_class_rank";

    /** A field name. */
    private static final String FLD_HS_CLASS_SIZE = "hs_class_size";

    /** A field name. */
    private static final String FLD_ACT_MATH = "act_math";

    /** A field name. */
    private static final String FLD_SAT_MATH = "sat_math";

    /** A field name. */
    private static final String FLD_SATR_MATH = "satr_math";

    /** The student's internal ID (PIDM). */
    private Integer pidm;

    /** The student ID. */
    private String stuId;

    /** The student's last name (upper case). */
    private String lastName;

    /** The student's first name (upper case). */
    private String firstName;

    /** The student's birthdate. */
    private LocalDate birthDate;

    /** The student's current CSU GPA. */
    private Float gpa;

    /** The student sex (M, F, U). */
    private String sex;

    /** The student Hispanic/Latino race flag ("Y" if Hispanic/Latino). */
    private String hispLatRace;

    /** The student American Indian race flag ("Y" if American Indian). */
    private String amerIndRace;

    /** The student Asian race flag ("Y" if Asian). */
    private String asianRace;

    /** The student Black race flag ("Y" if Black). */
    private String blackRace;

    /** The student Hawaiian race flag ("Y" if Hawaiian). */
    private String hawaiianRace;

    /** The student White race flag ("Y" if White). */
    private String whiteRace;

    /** The student multi-race race flag ("Y" if multi-racial). */
    private String multiRace;

    /** The student's application term, in "202310" format. */
    private Integer aplnTerm;

    /** The student's admission term, in "202310" format, null if not admitted. */
    private Integer admitTerm;

    /** The program into which the student was admitted. */
    private String admitProgram;

    /** The student's high school code. */
    private String hsCode;

    /** The student's high school graduation date. */
    private LocalDate hsGradDate;

    /** The student's high school GPA. */
    private Float hsGpa;

    /** The high school class rank. */
    private Integer hsClassRank;

    /** The high school class size. */
    private Integer hsClassSize;

    /** The ACT math score. */
    private Integer actMath;

    /** The SAT math score. */
    private Integer satMath;

    /** The SAT (revised) math score. */
    private Integer satrMath;

    /**
     * Constructs a new {@code AnalyticsStudent}.
     */
    private AnalyticsStudent() {

        super();
    }

    /**
     * Constructs a new {@code AnalyticsStudent}.
     *
     * @param thePidm         the student's internal ID (PIDM)
     * @param theStuId        the student ID
     * @param theLastName     the student's last name
     * @param theFirstName    the student's first name
     * @param theBirthDate    the student's birthdate
     * @param theGpa          the student's current CSU GPA
     * @param theSex          the student's sex (M, F, or U)
     * @param theHispLatRace  the student Hispanic/Latino race flag ("Y" if Hispanic/Latino)
     * @param theAmerIndRace  the student American Indian race flag ("Y" if American Indian)
     * @param theAsianRace    the student Asian race flag ("Y" if Asian)
     * @param theBlackRace    the student Black race flag ("Y" if Black)
     * @param theHawaiianRace the student Hawaiian race flag ("Y" if Hawaiian)
     * @param theWhiteRace    the student White race flag ("Y" if White)
     * @param theMultiRace    the student multi-racial race flag ("Y" if multi-racial)
     * @param theAplnTerm     the student's application term, in "202310" format
     * @param theAdmitTerm    the student's admission term, in "202310" format
     * @param theAdmitProgram the program to which the student was admitted
     * @param theHsCode       the student's high school code
     * @param theHsGradDate   the student's high school graduation date
     * @param theHsGpa        the student's high school GPA
     * @param theHsClassRank  the student's high school class rank
     * @param theHsClassSize  the student's high school class size
     * @param theActMath      the ACT math score
     * @param theSatMath      the SAT math score
     * @param theSatrMath     the revised SAT math score
     */
    public AnalyticsStudent(final Integer thePidm, final String theStuId, final String theFirstName,
                            final String theLastName, final LocalDate theBirthDate, final Float theGpa,
                            final String theSex, final String theHispLatRace, final String theAmerIndRace,
                            final String theAsianRace, final String theBlackRace, final String theHawaiianRace,
                            final String theWhiteRace, final String theMultiRace, final Integer theAplnTerm,
                            final Integer theAdmitTerm, final String theAdmitProgram, final String theHsCode,
                            final LocalDate theHsGradDate, final Float theHsGpa, final Integer theHsClassRank,
                            final Integer theHsClassSize, final Integer theActMath, final Integer theSatMath,
                            final Integer theSatrMath) {

        super();

        this.pidm = thePidm;
        this.stuId = theStuId;
        this.lastName = theLastName;
        this.firstName = theFirstName;
        this.birthDate = theBirthDate;
        this.gpa = theGpa;
        this.sex = theSex;
        this.hispLatRace = theHispLatRace;
        this.amerIndRace = theAmerIndRace;
        this.asianRace = theAsianRace;
        this.blackRace = theBlackRace;
        this.hawaiianRace = theHawaiianRace;
        this.whiteRace = theWhiteRace;
        this.multiRace = theMultiRace;
        this.aplnTerm = theAplnTerm;
        this.admitTerm = theAdmitTerm;
        this.admitProgram = theAdmitProgram;
        this.hsCode = theHsCode;
        this.hsGradDate = theHsGradDate;
        this.hsGpa = theHsGpa;
        this.hsClassRank = theHsClassRank;
        this.hsClassSize = theHsClassSize;
        this.actMath = theActMath;
        this.satMath = theSatMath;
        this.satrMath = theSatrMath;
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.pidm)
                + Objects.hashCode(this.stuId)
                + Objects.hashCode(this.lastName)
                + Objects.hashCode(this.firstName)
                + Objects.hashCode(this.birthDate)
                + Objects.hashCode(this.gpa)
                + Objects.hashCode(this.sex)
                + Objects.hashCode(this.hispLatRace)
                + Objects.hashCode(this.amerIndRace)
                + Objects.hashCode(this.asianRace)
                + Objects.hashCode(this.blackRace)
                + Objects.hashCode(this.hawaiianRace)
                + Objects.hashCode(this.whiteRace)
                + Objects.hashCode(this.multiRace)
                + Objects.hashCode(this.aplnTerm)
                + Objects.hashCode(this.admitTerm)
                + Objects.hashCode(this.admitProgram)
                + Objects.hashCode(this.hsCode)
                + Objects.hashCode(this.hsGradDate)
                + Objects.hashCode(this.hsGpa)
                + Objects.hashCode(this.hsClassRank)
                + Objects.hashCode(this.hsClassSize)
                + Objects.hashCode(this.actMath)
                + Objects.hashCode(this.satMath)
                + Objects.hashCode(this.satrMath);
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
        } else if (obj instanceof final AnalyticsStudent stu) {
            equal = Objects.equals(this.pidm, stu.pidm)
                    && Objects.equals(this.stuId, stu.stuId)
                    && Objects.equals(this.lastName, stu.lastName)
                    && Objects.equals(this.firstName, stu.firstName)
                    && Objects.equals(this.birthDate, stu.birthDate)
                    && Objects.equals(this.gpa, stu.gpa)
                    && Objects.equals(this.sex, stu.sex)
                    && Objects.equals(this.hispLatRace, stu.hispLatRace)
                    && Objects.equals(this.amerIndRace, stu.amerIndRace)
                    && Objects.equals(this.asianRace, stu.asianRace)
                    && Objects.equals(this.blackRace, stu.blackRace)
                    && Objects.equals(this.hawaiianRace, stu.hawaiianRace)
                    && Objects.equals(this.whiteRace, stu.whiteRace)
                    && Objects.equals(this.multiRace, stu.multiRace)
                    && Objects.equals(this.aplnTerm, stu.aplnTerm)
                    && Objects.equals(this.admitTerm, stu.admitTerm)
                    && Objects.equals(this.admitProgram, stu.admitProgram)
                    && Objects.equals(this.hsCode, stu.hsCode)
                    && Objects.equals(this.hsGradDate, stu.hsGradDate)
                    && Objects.equals(this.hsGpa, stu.hsGpa)
                    && Objects.equals(this.hsClassRank, stu.hsClassRank)
                    && Objects.equals(this.hsClassSize, stu.hsClassSize)
                    && Objects.equals(this.actMath, stu.actMath)
                    && Objects.equals(this.satMath, stu.satMath)
                    && Objects.equals(this.satrMath, stu.satrMath);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Extracts a {@code AnalyticsStudent} record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static AnalyticsStudent fromResultSet(final ResultSet rs) throws SQLException {

        final AnalyticsStudent result = new AnalyticsStudent();

        result.pidm = getIntegerField(rs, FLD_PIDM);
        result.stuId = getStringField(rs, FLD_STU_ID);
        result.lastName = getStringField(rs, FLD_FIRST_NAME);
        result.firstName = getStringField(rs, FLD_LAST_NAME);
        result.birthDate = getDateField(rs, FLD_BIRTH_DATE);
        result.gpa = getFloatField(rs, FLD_GPA);
        result.sex = getStringField(rs, FLD_SEX);
        result.hispLatRace = getStringField(rs, FLD_HISP_LAT_RACE);
        result.amerIndRace = getStringField(rs, FLD_AMER_IND_RACE);
        result.asianRace = getStringField(rs, FLD_ASIAN_RACE);
        result.blackRace = getStringField(rs, FLD_BLACK_RACE);
        result.hawaiianRace = getStringField(rs, FLD_HAWAIIAN_RACE);
        result.whiteRace = getStringField(rs, FLD_WHITE_RACE);
        result.multiRace = getStringField(rs, FLD_MULTI_RACE);
        result.aplnTerm = getIntegerField(rs, FLD_APLN_TERM);
        result.admitTerm = getIntegerField(rs, FLD_ADMIT_TERM);
        result.admitProgram = getStringField(rs, FLD_ADMIT_PROGRAM);
        result.hsCode = getStringField(rs, FLD_HS_CODE);
        result.hsGradDate = getDateField(rs, FLD_HS_GRAD_DATE);
        result.hsGpa = getFloatField(rs, FLD_HS_GPA);
        result.hsClassRank = getIntegerField(rs, FLD_HS_CLASS_RANK);
        result.hsClassSize = getIntegerField(rs, FLD_HS_CLASS_SIZE);
        result.actMath = getIntegerField(rs, FLD_ACT_MATH);
        result.satMath = getIntegerField(rs, FLD_SAT_MATH);
        result.satrMath = getIntegerField(rs, FLD_SATR_MATH);

        // theAdmitProgram, theHsCode, theHsGradDate, theHsGpa, theHsClassRank, theHsClassSize,
        // theActMath, theSatMath, theSatrMath, theStatus);

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
    public int compareTo(final AnalyticsStudent o) {

        int result = 0;

        if (this.lastName != null && o.lastName != null) {
            result = this.lastName.compareTo(o.lastName);
        }
        if (result == 0 && this.firstName != null && o.firstName != null) {
            result = this.firstName.compareTo(o.firstName);
        }
        if (result == 0) {
            result = this.stuId.compareTo(o.stuId);
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

        appendField(htm, FLD_PIDM, this.pidm);
        htm.add(DIVIDER);
        appendField(htm, FLD_STU_ID, this.stuId);

        return htm.toString();
    }
}
