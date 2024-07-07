package dev.mathops.session.sitelogic.servlet;

import dev.mathops.commons.CoreConstants;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.rawlogic.RawPacingStructureLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawCusection;
import dev.mathops.db.old.rawrecord.RawPacingStructure;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.svc.term.TermRec;

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

            final TermRec active = cache.getSystemData().getActiveTerm();

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
                final SystemData systemData = cache.getSystemData();

                this.cuSection = systemData.getCourseUnitSection(RawRecordConstants.M100U, "1", Integer.valueOf(1),
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
     * Gets the student's middle initial.
     *
     * @return the student's middle initial, or {@code null} if no student loaded
     */
    public String getMiddleInitial() {

        return this.student == null ? null : this.student.middleInitial;
    }

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

//    /**
//     * Main method for testing.
//     *
//     * @param args Command-line arguments.
//     */
//    public static void main(final String... args) {
//
//        final ContextMap map = ContextMap.getDefaultInstance();
//        DbConnection.registerDrivers();
//
//        final WebSiteProfile siteProfile = map.getWebSiteProfile(Contexts.PRECALC_HOST, Contexts.INSTRUCTION_PATH);
//        if (siteProfile != null) {
//            final StudentInfo bean = new StudentInfo(siteProfile.dbProfile);
//
//            final DbContext ctx = siteProfile.dbProfile.getDbContext(ESchemaUse.PRIMARY);
//            try {
//                final DbConnection conn = ctx.checkOutConnection();
//                final Cache cache = new Cache(siteProfile.dbProfile, conn);
//
//                try {
//                    bean.gatherData(cache, "guest");
//                    Log.info(bean.getFirstName());
//                    Log.info(bean.getLastName());
//                    Log.info(Boolean.toString(bean.isLicensed()));
//                } finally {
//                    ctx.checkInConnection(conn);
//                }
//            } catch (final SQLException ex) {
//                Log.warning(ex);
//            }
//        }
//    }
}
