package dev.mathops.session.sitelogic.servlet;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.DbContext;
import dev.mathops.db.cfg.ContextMap;
import dev.mathops.db.cfg.DbProfile;
import dev.mathops.db.cfg.ESchemaUse;
import dev.mathops.db.cfg.WebSiteProfile;
import dev.mathops.db.rawlogic.RawCusectionLogic;
import dev.mathops.db.rawlogic.RawPacingStructureLogic;
import dev.mathops.db.rawlogic.RawStudentLogic;
import dev.mathops.db.rawrecord.RawCusection;
import dev.mathops.db.rawrecord.RawPacingStructure;
import dev.mathops.db.rawrecord.RawRecordConstants;
import dev.mathops.db.rawrecord.RawStudent;
import dev.mathops.db.svc.term.TermLogic;
import dev.mathops.db.svc.term.TermRec;

import java.sql.SQLException;

/**
 * Gathers information about a student.
 */
public final class StudentInfo extends LogicBase {

    /** The student for which information is being gathered. */
    private String studentId;

    /** The queried student data. */
    private RawStudent student;

    /** The queried pacing structure data. */
    private RawPacingStructure pacingStructure;

    /** The configuration of the licensing exam. */
    private RawCusection cuSection;

    /**
     * Constructs a new {@code StudentInfo}.
     *
     * @param theDbProfile the database profile under which this site is accessed
     */
    public StudentInfo(final DbProfile theDbProfile) {

        super(theDbProfile);

        this.studentId = null;
        this.student = null;
        this.pacingStructure = null;
        this.cuSection = null;
    }

    /**
     * Sets the student ID and collects the student information.
     *
     * @param cache        the data cache
     * @param theStudentId the ID of the student for which to gather information
     * @throws SQLException if there is an error accessing the database
     */
    public void gatherData(final Cache cache, final String theStudentId) throws SQLException {

        if (theStudentId == null) {
            setErrorText("No student ID provided");
        } else {
            this.studentId = theStudentId;

            final TermRec active = TermLogic.get(cache).queryActive(cache);

            if (active == null) {
                setErrorText("Unable to query the current term.");
            } else {
                doGatherData(cache, active);
            }
        }
    }

    /**
     * Collects the student information.
     *
     * @param cache  the data cache
     * @param active the active term
     * @throws SQLException if there is an error accessing the database
     */
    private void doGatherData(final Cache cache, final TermRec active) throws SQLException {

        if ("GUEST".equalsIgnoreCase(this.studentId) || "AACTUTOR".equalsIgnoreCase(this.studentId)) {

            this.student = RawStudentLogic.makeFakeStudent(this.studentId, CoreConstants.EMPTY, "Guest");

        } else {
            this.student = RawStudentLogic.query(cache, this.studentId, true);

            if (this.student == null) {
                setErrorText("Student not found");
            } else {
                this.cuSection = RawCusectionLogic.query(cache, RawRecordConstants.M100U, "1", Integer.valueOf(1),
                        active.term);
                if (this.cuSection == null) {
                    setErrorText("No data for course M 100U section 1 unit 1");
                } else if (this.student.pacingStructure != null) {
                    this.pacingStructure = RawPacingStructureLogic.query(cache, this.student.pacingStructure);

                    if (this.pacingStructure == null) {
                        setErrorText("No data for pacing structure");
                    }
                }
            }
        }
    }

    /**
     * Gets the student's last name.
     *
     * @return the student's last name, or {@code null} if no student loaded
     */
    private String getLastName() {

        return this.student == null ? null : this.student.lastName;
    }

    /**
     * Gets the student's first name.
     *
     * @return the student's first name, or {@code null} if no student loaded
     */
    private String getFirstName() {

        return this.student == null ? null : this.student.firstName;
    }

    /**
     * Gets the student's middle initial.
     *
     * @return the student's middle initial, or {@code null} if no student loaded
     */
    public String getMiddleInitial() {

        return this.student == null ? null : this.student.middleInitial;
    }

    /**
     * Gets the student's ACT score.
     *
     * @return the student's ACT score, or {@code null} if none
     */
    public Integer getActScore() {

        return this.student == null ? null : this.student.actScore;
    }

    /**
     * Gets the student's SAT score.
     *
     * @return the student's SAT score, or {@code null} if none
     */
    public Integer getSatScore() {

        return this.student == null ? null : this.student.satScore;
    }

//    /**
//     * Gets the student's AP score.
//     *
//     * @return the student's AP score, or {@code null} if none
//     */
//    public String getApScore() {
//
//        return this.student == null ? null : this.student.apScore;
//    }

    /**
     * Tests whether the student has passed the licensing exam.
     *
     * @return {@code true} if the student is licensed
     */
    public boolean isLicensed() {

        return this.student != null && "Y".equals(this.student.licensed);
    }

    /**
     * Tests whether the student must pass the licensing exam.
     *
     * @return {@code true} if the student must pass the exam
     */
    public boolean isRequireLicensed() {

        return this.pacingStructure == null || "Y".equals(this.pacingStructure.requireLicensed);
    }

//    /**
//     * Gets the date when the licensing exam will become available.
//     *
//     * @return the string representation of the date
//     */
//    public String getLicenseAvailableDate() {
//
//        final LocalDate now = LocalDate.now();
//        String result;
//
//        if (this.cuSection == null) {
//            result = "now";
//        } else {
//            LocalDate day = this.cuSection.firstTestDt;
//
//            if (now.isBefore(day)) {
//                result = TemporalUtils.FMT_MDY.format(this.cuSection.firstTestDt);
//            } else {
//                day = this.cuSection.lastTestDt;
//
//                if (now.isAfter(day)) {
//                    result = "at the beginning of the next term";
//                } else {
//                    result = "now";
//                }
//            }
//        }
//
//        return result;
//    }

//    /**
//     * Gets the pacing structure ID.
//     *
//     * @return the pacing structure ID
//     */
//    public String getPacingStructureId() {
//
//        if (this.student != null) {
//            return this.student.pacingStructure;
//        }
//
//        return RawPacingStructure.DEF_PACING_STRUCTURE;
//    }

    /**
     * Gets the department the student is registered in.
     *
     * @return the student's department
     */
    public String getDepartment() {

        return this.student == null ? null : this.student.dept;
    }

    /**
     * Gets the college the student is registered in.
     *
     * @return the student's college
     */
    public String getCollege() {

        return this.student == null ? null : this.student.college;
    }

    ///**
    // * Gets the student's major.
    // *
    // * @return the student's major
    // */
    // public String getMajor() {
    //
    // return (this.student == null) ? null : this.student.getMajor();
    // }

    ///**
    // * Gets the student's minor.
    // *
    // * @return the student's minor
    // */
    // public String getMinor() {
    //
    // return (this.student == null) ? null : this.student.getMinor();
    // }

    /**
     * Main method for testing.
     *
     * @param args Command-line arguments.
     */
    public static void main(final String... args) {

        final ContextMap map = ContextMap.getDefaultInstance();
        DbConnection.registerDrivers();

        final WebSiteProfile siteProfile = map.getWebSiteProfile(Contexts.PRECALC_HOST, Contexts.INSTRUCTION_PATH);
        if (siteProfile != null) {
            final StudentInfo bean = new StudentInfo(siteProfile.dbProfile);

            final DbContext ctx = siteProfile.dbProfile.getDbContext(ESchemaUse.PRIMARY);
            try {
                final DbConnection conn = ctx.checkOutConnection();
                final Cache cache = new Cache(siteProfile.dbProfile, conn);

                try {
                    bean.gatherData(cache, "guest");
                    Log.info(bean.getFirstName());
                    Log.info(bean.getLastName());
                    Log.info(Boolean.toString(bean.isLicensed()));
                } finally {
                    ctx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
            }
        }
    }
}
