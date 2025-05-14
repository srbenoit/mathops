package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * A raw "pacing_structure" record.
 */
public final class RawPacingStructure extends RawTermRecordBase implements Comparable<RawPacingStructure> {

    /** The table name. */
    public static final String TABLE_NAME = "pacing_structure";

    /** A field name. */
    private static final String FLD_PACING_STRUCTURE = "pacing_structure";

    /** A field name. */
    private static final String FLD_DEF_PACE_TRACK = "def_pace_track";

    /** A field name. */
    private static final String FLD_REQUIRE_LICENSED = "require_licensed";

    /** A field name. */
    private static final String FLD_REQUIRE_PARTIC = "require_partic";

    /** A field name. */
    private static final String FLD_MAX_PARTIC_MISSED = "max_partic_missed";

    /** A field name. */
    private static final String FLD_ALLOW_INC = "allow_inc";

    /** A field name. */
    private static final String FLD_MAX_COURSES = "max_courses";

    /** A field name. */
    private static final String FLD_NBR_OPEN_ALLOWED = "nbr_open_allowed";

    /** A field name. */
    private static final String FLD_REQUIRE_UNIT_EXAMS = "require_unit_exams";

    /** A field name. */
    private static final String FLD_USE_MIDTERMS = "use_midterms";

    /** A field name. */
    private static final String FLD_ALLOW_COUPONS = "allow_coupons";

    /** A field name. */
    private static final String FLD_COUPONS_AFTER_WINDOW = "coupons_after_window";

    /** A field name. */
    private static final String FLD_USERS_PROGRESS_CR = "users_progress_cr";

    /** A field name. */
    private static final String FLD_HW_PROGRESS_CR = "hw_progress_cr";

    /** A field name. */
    private static final String FLD_RE_PROGRESS_CR = "re_progress_cr";

    /** A field name. */
    private static final String FLD_UE_PROGRESS_CR = "ue_progress_cr";

    /** A field name. */
    private static final String FLD_FIN_PROGRESS_CR = "fin_progress_cr";

    /** A field name. */
    private static final String FLD_PACING_NAME = "pacing_name";

    /** A field name. */
    private static final String FLD_SCHEDULE_SOURCE = "schedule_source";

    /** A field name. */
    private static final String FLD_SR_DUE_DATE_ENFORCED = "sr_due_date_enforced";

    /** A field name. */
    private static final String FLD_RE_DUE_DATE_ENFORCED = "re_due_date_enforced";

    /** A field name. */
    private static final String FLD_UE_DUE_DATE_ENFORCED = "ue_due_date_enforced";

    /** A field name. */
    private static final String FLD_FE_DUE_DATE_ENFORCED = "fe_due_date_enforced";

    /** A field name. */
    private static final String FLD_FIRST_OBJ_AVAIL = "first_obj_avail";

    /** A field name. */
    private static final String FLD_FREE_EXTENSION_DAYS = "free_extension_days";

    /** The pacing structure to use when a student has none specified. */
    public static final String DEF_PACING_STRUCTURE = "M";

    /** The pacing structure to use for guest logins. */
    public static final String GUEST_PACING_STRUCTURE = DEF_PACING_STRUCTURE;

    /** The 'pacing_structure' field value. */
    public String pacingStructure;

    /** The 'def_pace_track' field value. */
    public String defPaceTrack;

    /** The 'require_licensed' field value. */
    public String requireLicensed;

    /** The 'require_partic' field value. */
    public String requirePartic;

    /** The 'max_partic_missed' field value. */
    public Integer maxParticMissed;

    /** The 'allow_inc' field value. */
    public String allowInc;

    /** The 'max_courses' field value. */
    public Integer maxCourses;

    /** The 'nbr_open_allowed' field value. */
    public Integer nbrOpenAllowed;

    /** The 'require_unit_exams' field value. */
    public String requireUnitExams;

    /** The 'use_midterms' field value. */
    public String useMidterms;

    /** The 'allow_coupons' field value. */
    public String allowCoupons;

    /** The 'coupons_after_window' field value. */
    public String couponsAfterWindow;

    /** The 'users_progress_cr' field value. */
    public Integer usersProgressCr;

    /** The 'hw_progress_cr' field value. */
    public Integer hwProgressCr;

    /** The 're_progress_cr' field value. */
    public Integer reProgressCr;

    /** The 'ue_progress_cr' field value. */
    public Integer ueProgressCr;

    /** The 'fin_progress_cr' field value. */
    public Integer finProgressCr;

    /** The 'pacing_name' field value. */
    public String pacingName;

    /** The 'schedule_source' field value ('pace' or 'f2f'). */
    public String scheduleSource;

    /** The 'sr_due_date_enforced' field value. */
    public String srDueDateEnforced;

    /** The 're_due_date_enforced' field value. */
    public String reDueDateEnforced;

    /** The 'ue_due_date_enforced' field value. */
    public String ueDueDateEnforced;

    /** The 'fe_due_date_enforced' field value. */
    public String feDueDateEnforced;

    /** The 'first_obj_avail' field value. */
    public String firstObjAvail;

    /** The 'free_extension_days' field value. */
    public Integer freeExtensionDays;

    /**
     * Constructs a new {@code RawPacingStructure}.
     */
    private RawPacingStructure() {

        super();
    }

    /**
     * Constructs a new {@code RawPacingStructure}.
     *
     * @param theTermKey            the term key
     * @param thePacingStructure    the pacing structure ID
     * @param theDefPaceTrack       the default pace track
     * @param theRequireLicensed    the "Y" if the pacing structure requires student be licensed; "N" if not
     * @param theRequirePartic      "Y" if participation is required; "N" if not
     * @param theMaxParticMissed    the maximum number of participation credits the student can miss
     * @param theAllowInc           "Y" to allow incompletes; "N" otherwise
     * @param theMaxCourses         the maximum number of courses the student may register for
     * @param theNbrOpenAllowed     the maximum number of courses that may be open concurrently
     * @param theRequireUnitExams   "Y" if unit exams are required; "N" if not
     * @param theUseMidterms        the "Y" to use midterms rather than unit exams; "N" for unit exams
     * @param theAllowCoupons       "Y" to allow coupons for testing outside a window
     * @param theCouponsAfterWindow "Y" to allow coupons to be used after window closes
     * @param theUsersProgressCr    the number of progress credits for a user's exam
     * @param theHwProgressCr       the number of progress credits for a homework
     * @param theReProgressCr       the number of progress credits for a review exam
     * @param theUeProgressCr       the number of progress credits for a unit exam
     * @param theFinProgressCr      the number of progress credits for a final exam
     * @param thePacingName         the name of the pacing structure
     * @param theScheduleSource     the source for schedule data
     * @param theSrDueDateEnforced  "Y" if due date is enforced on Skills review, "N" if not
     * @param theReDueDateEnforced  "Y" if due date is enforced on Review exam, "N" if not
     * @param theUDueDateEnforced   "Y" if due date is enforced on Unit exam, "N" if not
     * @param theFeDueDateEnforced  "Y" if due date is enforced on Final exam, "N" if not
     * @param theFirstObjAvail      "Y" if first objective is always available; "N" if not
     * @param theFreeExtensionDays  the number of "free" extension days the student can take on request
     */
    public RawPacingStructure(final TermKey theTermKey, final String thePacingStructure, final String theDefPaceTrack,
                              final String theRequireLicensed, final String theRequirePartic,
                              final Integer theMaxParticMissed, final String theAllowInc, final Integer theMaxCourses,
                              final Integer theNbrOpenAllowed, final String theRequireUnitExams,
                              final String theUseMidterms, final String theAllowCoupons,
                              final String theCouponsAfterWindow, final Integer theUsersProgressCr,
                              final Integer theHwProgressCr, final Integer theReProgressCr,
                              final Integer theUeProgressCr, final Integer theFinProgressCr, final String thePacingName,
                              final String theScheduleSource, final String theSrDueDateEnforced,
                              final String theReDueDateEnforced, final String theUDueDateEnforced,
                              final String theFeDueDateEnforced, final String theFirstObjAvail,
                              final Integer theFreeExtensionDays) {

        super(theTermKey);

        this.pacingStructure = thePacingStructure;
        this.defPaceTrack = theDefPaceTrack;
        this.requireLicensed = theRequireLicensed;
        this.requirePartic = theRequirePartic;
        this.maxParticMissed = theMaxParticMissed;
        this.allowInc = theAllowInc;
        this.maxCourses = theMaxCourses;
        this.nbrOpenAllowed = theNbrOpenAllowed;
        this.requireUnitExams = theRequireUnitExams;
        this.useMidterms = theUseMidterms;
        this.allowCoupons = theAllowCoupons;
        this.couponsAfterWindow = theCouponsAfterWindow;
        this.usersProgressCr = theUsersProgressCr;
        this.hwProgressCr = theHwProgressCr;
        this.reProgressCr = theReProgressCr;
        this.ueProgressCr = theUeProgressCr;
        this.finProgressCr = theFinProgressCr;
        this.pacingName = thePacingName;
        this.scheduleSource = theScheduleSource;
        this.srDueDateEnforced = theSrDueDateEnforced;
        this.reDueDateEnforced = theReDueDateEnforced;
        this.ueDueDateEnforced = theUDueDateEnforced;
        this.feDueDateEnforced = theFeDueDateEnforced;
        this.firstObjAvail = theFirstObjAvail;
        this.freeExtensionDays = theFreeExtensionDays;
    }

    /**
     * Extracts a "remote_mpe" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawPacingStructure fromResultSet(final ResultSet rs) throws SQLException {

        final RawPacingStructure result = new RawPacingStructure();

        result.termKey = getTermAndYear(rs, FLD_TERM, FLD_TERM_YR);
        result.pacingStructure = getStringField(rs, FLD_PACING_STRUCTURE);
        result.defPaceTrack = getStringField(rs, FLD_DEF_PACE_TRACK);
        result.requireLicensed = getStringField(rs, FLD_REQUIRE_LICENSED);
        result.requirePartic = getStringField(rs, FLD_REQUIRE_PARTIC);
        result.maxParticMissed = getIntegerField(rs, FLD_MAX_PARTIC_MISSED);
        result.allowInc = getStringField(rs, FLD_ALLOW_INC);
        result.maxCourses = getIntegerField(rs, FLD_MAX_COURSES);
        result.nbrOpenAllowed = getIntegerField(rs, FLD_NBR_OPEN_ALLOWED);
        result.requireUnitExams = getStringField(rs, FLD_REQUIRE_UNIT_EXAMS);
        result.useMidterms = getStringField(rs, FLD_USE_MIDTERMS);
        result.allowCoupons = getStringField(rs, FLD_ALLOW_COUPONS);
        result.couponsAfterWindow = getStringField(rs, FLD_COUPONS_AFTER_WINDOW);
        result.usersProgressCr = getIntegerField(rs, FLD_USERS_PROGRESS_CR);
        result.hwProgressCr = getIntegerField(rs, FLD_HW_PROGRESS_CR);
        result.reProgressCr = getIntegerField(rs, FLD_RE_PROGRESS_CR);
        result.ueProgressCr = getIntegerField(rs, FLD_UE_PROGRESS_CR);
        result.finProgressCr = getIntegerField(rs, FLD_FIN_PROGRESS_CR);
        result.pacingName = getStringField(rs, FLD_PACING_NAME);
        result.scheduleSource = getStringField(rs, FLD_SCHEDULE_SOURCE);
        result.srDueDateEnforced = getStringField(rs, FLD_SR_DUE_DATE_ENFORCED);
        result.reDueDateEnforced = getStringField(rs, FLD_RE_DUE_DATE_ENFORCED);
        result.ueDueDateEnforced = getStringField(rs, FLD_UE_DUE_DATE_ENFORCED);
        result.feDueDateEnforced = getStringField(rs, FLD_FE_DUE_DATE_ENFORCED);
        result.firstObjAvail = getStringField(rs, FLD_FIRST_OBJ_AVAIL);

        // FIXME: Convert hardcode into database field
        // NOTE: We use 2 for Fall/Spring, 1 for Summer
        result.freeExtensionDays = Integer.valueOf(1);

        return result;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object.
     */
    @Override
    public int compareTo(final RawPacingStructure o) {

        int result = this.termKey.compareTo(o.termKey);

        if (result == 0) {
            result = this.pacingStructure.compareTo(o.pacingStructure);
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

        appendField(htm, FLD_TERM, this.termKey);
        htm.add(DIVIDER);
        appendField(htm, FLD_PACING_STRUCTURE, this.pacingStructure);
        htm.add(DIVIDER);
        appendField(htm, FLD_DEF_PACE_TRACK, this.defPaceTrack);
        htm.add(DIVIDER);
        appendField(htm, FLD_REQUIRE_LICENSED, this.requireLicensed);
        htm.add(DIVIDER);
        appendField(htm, FLD_REQUIRE_PARTIC, this.requirePartic);
        htm.add(DIVIDER);
        appendField(htm, FLD_MAX_PARTIC_MISSED, this.maxParticMissed);
        htm.add(DIVIDER);
        appendField(htm, FLD_ALLOW_INC, this.allowInc);
        htm.add(DIVIDER);
        appendField(htm, FLD_MAX_COURSES, this.maxCourses);
        htm.add(DIVIDER);
        appendField(htm, FLD_NBR_OPEN_ALLOWED, this.nbrOpenAllowed);
        htm.add(DIVIDER);
        appendField(htm, FLD_REQUIRE_UNIT_EXAMS, this.requireUnitExams);
        htm.add(DIVIDER);
        appendField(htm, FLD_USE_MIDTERMS, this.useMidterms);
        htm.add(DIVIDER);
        appendField(htm, FLD_ALLOW_COUPONS, this.allowCoupons);
        htm.add(DIVIDER);
        appendField(htm, FLD_COUPONS_AFTER_WINDOW, this.couponsAfterWindow);
        htm.add(DIVIDER);
        appendField(htm, FLD_USERS_PROGRESS_CR, this.usersProgressCr);
        htm.add(DIVIDER);
        appendField(htm, FLD_HW_PROGRESS_CR, this.hwProgressCr);
        htm.add(DIVIDER);
        appendField(htm, FLD_RE_PROGRESS_CR, this.reProgressCr);
        htm.add(DIVIDER);
        appendField(htm, FLD_UE_PROGRESS_CR, this.ueProgressCr);
        htm.add(DIVIDER);
        appendField(htm, FLD_FIN_PROGRESS_CR, this.finProgressCr);
        htm.add(DIVIDER);
        appendField(htm, FLD_PACING_NAME, this.pacingName);
        htm.add(DIVIDER);
        appendField(htm, FLD_SCHEDULE_SOURCE, this.scheduleSource);
        htm.add(DIVIDER);
        appendField(htm, FLD_SR_DUE_DATE_ENFORCED, this.srDueDateEnforced);
        htm.add(DIVIDER);
        appendField(htm, FLD_RE_DUE_DATE_ENFORCED, this.reDueDateEnforced);
        htm.add(DIVIDER);
        appendField(htm, FLD_UE_DUE_DATE_ENFORCED, this.ueDueDateEnforced);
        htm.add(DIVIDER);
        appendField(htm, FLD_FE_DUE_DATE_ENFORCED, this.feDueDateEnforced);
        htm.add(DIVIDER);
        appendField(htm, FLD_FIRST_OBJ_AVAIL, this.firstObjAvail);
        htm.add(DIVIDER);
        appendField(htm, FLD_FREE_EXTENSION_DAYS, this.freeExtensionDays);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.termKey)
               + Objects.hashCode(this.pacingStructure)
               + Objects.hashCode(this.defPaceTrack)
               + Objects.hashCode(this.requireLicensed)
               + Objects.hashCode(this.requirePartic)
               + Objects.hashCode(this.maxParticMissed)
               + Objects.hashCode(this.allowInc)
               + Objects.hashCode(this.maxCourses)
               + Objects.hashCode(this.nbrOpenAllowed)
               + Objects.hashCode(this.requireUnitExams)
               + Objects.hashCode(this.useMidterms)
               + Objects.hashCode(this.allowCoupons)
               + Objects.hashCode(this.couponsAfterWindow)
               + Objects.hashCode(this.usersProgressCr)
               + Objects.hashCode(this.hwProgressCr)
               + Objects.hashCode(this.reProgressCr)
               + Objects.hashCode(this.ueProgressCr)
               + Objects.hashCode(this.finProgressCr)
               + Objects.hashCode(this.pacingName)
               + Objects.hashCode(this.scheduleSource)
               + Objects.hashCode(this.srDueDateEnforced)
               + Objects.hashCode(this.reDueDateEnforced)
               + Objects.hashCode(this.ueDueDateEnforced)
               + Objects.hashCode(this.feDueDateEnforced)
               + Objects.hashCode(this.firstObjAvail)
               + Objects.hashCode(this.freeExtensionDays);
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
        } else if (obj instanceof final RawPacingStructure rec) {
            equal = Objects.equals(this.termKey, rec.termKey)
                    && Objects.equals(this.pacingStructure, rec.pacingStructure)
                    && Objects.equals(this.defPaceTrack, rec.defPaceTrack)
                    && Objects.equals(this.requireLicensed, rec.requireLicensed)
                    && Objects.equals(this.requirePartic, rec.requirePartic)
                    && Objects.equals(this.maxParticMissed, rec.maxParticMissed)
                    && Objects.equals(this.allowInc, rec.allowInc)
                    && Objects.equals(this.maxCourses, rec.maxCourses)
                    && Objects.equals(this.nbrOpenAllowed, rec.nbrOpenAllowed)
                    && Objects.equals(this.requireUnitExams, rec.requireUnitExams)
                    && Objects.equals(this.useMidterms, rec.useMidterms)
                    && Objects.equals(this.allowCoupons, rec.allowCoupons)
                    && Objects.equals(this.couponsAfterWindow, rec.couponsAfterWindow)
                    && Objects.equals(this.usersProgressCr, rec.usersProgressCr)
                    && Objects.equals(this.hwProgressCr, rec.hwProgressCr)
                    && Objects.equals(this.reProgressCr, rec.reProgressCr)
                    && Objects.equals(this.ueProgressCr, rec.ueProgressCr)
                    && Objects.equals(this.finProgressCr, rec.finProgressCr)
                    && Objects.equals(this.pacingName, rec.pacingName)
                    && Objects.equals(this.scheduleSource, rec.scheduleSource)
                    && Objects.equals(this.srDueDateEnforced, rec.srDueDateEnforced)
                    && Objects.equals(this.reDueDateEnforced, rec.reDueDateEnforced)
                    && Objects.equals(this.ueDueDateEnforced, rec.ueDueDateEnforced)
                    && Objects.equals(this.feDueDateEnforced, rec.feDueDateEnforced)
                    && Objects.equals(this.firstObjAvail, rec.firstObjAvail)
                    && Objects.equals(this.freeExtensionDays, rec.freeExtensionDays);
        } else {
            equal = false;
        }

        return equal;
    }
}
