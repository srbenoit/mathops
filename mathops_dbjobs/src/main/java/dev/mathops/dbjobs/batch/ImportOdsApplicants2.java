package dev.mathops.dbjobs.batch;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawlogic.RawApplicantLogic;
import dev.mathops.db.old.rawrecord.RawApplicant;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A class that performs an import of applicant data from the ODS and populates the APPLICANTS table.  It does not
 * update the STUDENT table at all.
 */
public final class ImportOdsApplicants2 {

    /** Debug flag - set to 'true' to print changes rather than performing them. */
    private static final boolean DEBUG = true;

    /** The database profile through which to access the database. */
    private final DbProfile dbProfile;

    /** The Primary database context. */
    private final DbContext primaryCtx;

    /** The ODS database context. */
    private final DbContext odsCtx;

    /**
     * Constructs a new {@code ImportOdsApplicants2}.
     */
    private ImportOdsApplicants2() {

        final ContextMap map = ContextMap.getDefaultInstance();

        this.dbProfile = map.getCodeProfile(Contexts.BATCH_PATH);
        this.primaryCtx = this.dbProfile.getDbContext(ESchemaUse.PRIMARY);
        this.odsCtx = this.dbProfile.getDbContext(ESchemaUse.ODS);
    }

    /**
     * Executes the job.
     *
     * @return a report
     */
    public String execute() {

        final Collection<String> report = new ArrayList<>(10);

        if (this.dbProfile == null) {
            report.add("Unable to create production context.");
        } else if (this.primaryCtx == null) {
            report.add("Unable to create primary database context.");
        } else if (this.odsCtx == null) {
            report.add("Unable to create ODS database context.");
        } else {
            try {
                final DbConnection conn = this.primaryCtx.checkOutConnection();
                final Cache cache = new Cache(this.dbProfile, conn);

                try {
                    final TermRec active = cache.getSystemData().getActiveTerm();

                    if (active == null) {
                        report.add("Failed to query the active term.");
                    } else {
                        executeInTerm(cache, active, report);
                    }
                } catch (final SQLException ex) {
                    Log.warning(ex);
                    report.add("Unable to perform query");
                } finally {
                    this.primaryCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                report.add("Unable to obtain connection to ODS database");
            }
        }

        final HtmlBuilder htm = new HtmlBuilder(1000);
        htm.addln("<pre>");
        for (final String rep : report) {
            htm.addln(rep);
        }
        htm.addln("</pre>");

        return htm.toString();
    }

    /**
     * Executes the query against the ODS term table matching the active term and loads data into the primary schema.
     *
     * @param cache  the data cache
     * @param active the active term
     * @param report a list of strings to which to add report output lines
     * @throws SQLException if there is an error querying the database
     */
    private void executeInTerm(final Cache cache, final TermRec active, final Collection<? super String> report)
            throws SQLException {

        final DbConnection odsConn = this.odsCtx.checkOutConnection();

        try {
            Map<String, ApplicantRecord> applicants = null;

            if (active.term.name == ETermName.SPRING) {
                report.add("Processing under the SPRING term");
                applicants = queryApplicants(odsConn, "SPR", report);
            } else if (active.term.name == ETermName.SUMMER) {
                report.add("Processing under the SUMMER term");
                applicants = queryApplicants(odsConn, "SMR", report);
            } else if (active.term.name == ETermName.FALL) {
                report.add("Processing under the FALL term");
                applicants = queryApplicants(odsConn, "FAL", report);
            } else {
                report.add("Active term has invalid term name: " + active.term.name);
            }

            if (applicants != null) {
                final String msg = "Found " + applicants.size() + " applicants.";
                report.add(msg);
                Log.info(msg);

                processList(cache, applicants, report);
            }

            report.add("Job completed");
        } catch (final SQLException ex) {
            Log.warning(ex);
            report.add("Unable to perform query: " + ex.getMessage());
        } finally {
            this.odsCtx.checkInConnection(odsConn);
        }
    }

    /**
     * Queries applicant records from the ODS given the name of the term info table.
     *
     * @param odsConn the ODS database connection
     * @param suffix  the suffix to append to the name of the term-dependent table
     * @param report  a list of strings to which to add report output lines
     * @return a map from CSU ID to applicant record
     * @throws SQLException if there is an error performing the query
     */
    private static Map<String, ApplicantRecord> queryApplicants(final DbConnection odsConn, final String suffix,
                                                                final Collection<? super String> report)
            throws SQLException {

        final Map<String, ApplicantRecord> result = new HashMap<>(80000);
        final Map<String, Map<String, Integer>> decisions = new HashMap<>(100);

        final int curYear = LocalDate.now().getYear();
        final String start = curYear - 4 + "10";
        final String end = curYear + 2 + "90";

        try (final Statement stmt = odsConn.createStatement()) {

            final String sql = "SELECT "
                               + " A.CSU_ID csuid, "
                               + " A.FIRST_NAME first, "
                               + " A.LAST_NAME last, "
                               + " A.PREFERRED_FIRST_NAME pref, "
                               + " A.MIDDLE_NAME middle, "
                               + " A.BIRTH_DATE bday, "
                               + " A.GENDER gender, "
                               + " A.RESIDENCY_STATE resstate, "
                               + " A.RESIDENCY_COUNTY rescounty, "
                               + " A.HS_CODE hscode, "
                               + " A.HS_GPA hsgpa, "
                               + " A.HS_CLASS_RANK hsrank, "
                               + " A.HS_CLASS_SIZE hssize, "
                               + " A.ACT_MATH act, "
                               + " A.SAT_MATH sat, "
                               + " A.SATR_MATH satr, "
                               + " A.EMAIL email, "

                               + " B.PIDM pidm, "
                               + " B.TERM appterm, "
                               + " B.APLCT_LATEST_DECN decision, "
                               + " B.ADMITTED_FLAG admitted, "
                               + " B.ADM_TYPE admType, "
                               + " B.ADM_COLLEGE admCollege, "
                               + " B.ADM_DEPT admDept, "
                               + " B.ADM_PROGRAM_OF_STUDY admProgram, "
                               + " B.ADM_CAMPUS admCampus, "
                               + " B.ADM_RESIDENCY admResidency, "
                               + " B.APLN_DATE aplnDate, "

                               + " C.STUDENT_CLASS cls, "
                               + " C.PRIMARY_COLLEGE college, "
                               + " C.PRIMARY_DEPARTMENT dept, "
                               + " C.PROGRAM_OF_STUDY program, "
                               + " C.ANTICIPATED_GRAD_TERM gradterm, "
                               + " C.RESIDENCY res, "
                               + " C.CAMPUS campus "

                               + "FROM CSUBAN.CSUG_GP_ADMISSIONS A "
                               + "   INNER JOIN CSUBAN.CSUS_APPLICANT B ON A.PIDM = B.PIDM "
                               + "   LEFT JOIN CSUBAN.CSUS_TERM_INFO_" + suffix
                               + " C ON C.PIDM = A.PIDM "

                               + "WHERE (B.APLN_STATUS <> 'U') "
                               + "  AND (B.APLCT_LATEST_DECN IS NULL OR B.APLCT_LATEST_DECN<>'RA') "
                               + "  AND (B.APLN_COUNT_PRIORITY_FLAG = 'Y') "
                               + "  AND (B.TERM Between '" + start + "' And '" + end + "') "
                               + "  AND (A.LAST_NAME Not Like '-Purge%') "
                               + "  AND (A.MULTI_SOURCE = 'CSU') "
                               + "  AND (   (B.ADM_CAMPUS = 'MC')"
                               + "       OR ((B.STUDENT_LEVEL = 'GR') And (B.STUDENT_TYPE = 'N')) "
                               + "       OR ((B.STUDENT_LEVEL = 'UG') "
                               + "            And (B.STUDENT_TYPE In ('N','T','R'))) "
                               + "       OR ((B.STUDENT_LEVEL In ('UG','GR')) "
                               + "           And (B.STUDENT_TYPE = 'E') "
                               + "           And (SUBSTR(B.ADM_PROGRAM_OF_STUDY,1,2) = 'N2')))";

            try (final ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {

                    final String csuId = rs.getString("csuid");

                    if (csuId == null) {
                        report.add("ODS record had null CSU ID");
                    } else if (csuId.length() == 9) {

                        String firstName = rs.getString("first");
                        if (firstName != null) {
                            firstName = firstName.replace("�", "'").replace("\u2019", "'");
                        }

                        String lastName = rs.getString("last");
                        if (lastName != null) {
                            lastName = lastName.replace("�", "'").replace("\u2019", "'");
                        }

                        String prefName = rs.getString("pref");
                        if (prefName != null) {
                            prefName = prefName.replace("�", "'").replace("\u2019", "'");
                        }

                        String middleName = rs.getString("middle");
                        if (middleName != null && !middleName.isEmpty()) {
                            middleName = middleName.replace("�", "'").replace("\u2019", "'");
                        }

                        final Date birthDate = rs.getDate("bday");
                        final String gender = rs.getString("gender");

                        final String hsCode = rs.getString("hscode");
                        final String hsGpa = rs.getString("hsgpa");

                        final int hsClassRankInt = rs.getInt("hsrank");
                        final Integer hsClassRank = rs.wasNull() ? null : Integer.valueOf(hsClassRankInt);

                        final int hsClassSizeInt = rs.getInt("hssize");
                        final Integer hsClassSize = rs.wasNull() ? null : Integer.valueOf(hsClassSizeInt);

                        final int actMathInt = rs.getInt("act");
                        final Integer actMath = rs.wasNull() ? null : Integer.valueOf(actMathInt);

                        final int satMathInt = rs.getInt("sat");
                        final Integer satMath = rs.wasNull() ? null : Integer.valueOf(satMathInt);

                        final int satrMathInt = rs.getInt("satr");
                        final Integer satrMath = rs.wasNull() ? null : Integer.valueOf(satrMathInt);

                        final Integer usedSat = satrMath == null ? satMath : satrMath;

                        final String email = rs.getString("email");

                        final String admType = rs.getString("admType");

                        String programOfStudy = rs.getString("program");
                        if (programOfStudy == null) {
                            programOfStudy = rs.getString("admProgram");
                        }

                        final String stuClass = rs.getString("cls");

                        String residency = rs.getString("res");
                        if (residency == null) {
                            residency = rs.getString("admResidency");
                        }
                        final String residencyState = rs.getString("resstate");
                        final String residencyCounty = rs.getString("rescounty");

                        final int pidmInt = rs.getInt("pidm");
                        final Integer pidm = rs.wasNull() ? null : Integer.valueOf(pidmInt);

                        final String appTerm = rs.getString("appterm");

                        TermKey applicationTerm = null;
                        if (appTerm != null && appTerm.length() == 6) {
                            final String year = appTerm.substring(0, 4);

                            try {
                                final int yearValue = Integer.parseInt(year);
                                if (appTerm.endsWith("90")) {
                                    applicationTerm = new TermKey(ETermName.FALL, yearValue);
                                } else if (appTerm.endsWith("10")) {
                                    applicationTerm = new TermKey(ETermName.SPRING, yearValue);
                                } else if (appTerm.endsWith("60")) {
                                    applicationTerm = new TermKey(ETermName.SUMMER, yearValue);
                                }
                            } catch (final NumberFormatException ex) {
                                Log.warning(ex);
                                report.add("Warning: Invalid application term for " + csuId + ": " + appTerm);
                            }
                        }

                        final Date applicationDate = rs.getDate("aplnDate");

                        final String gradTerm = rs.getString("gradterm");

                        String campus = rs.getString("campus");
                        if (campus == null) {
                            campus = rs.getString("admCampus");
                        }

                        final String decision = rs.getString("decision");
                        final String admitted = rs.getString("admitted");

                        final Map<String, Integer> inner = decisions.computeIfAbsent(decision, s -> new HashMap<>(5));
                        final Integer count = inner.get(admitted);
                        if (count == null) {
                            inner.put(admitted, Integer.valueOf(1));
                        } else {
                            inner.put(admitted, Integer.valueOf(count.intValue() + 1));
                        }

                        if (admitted != null) {
                            final boolean isAdmitted = decision != null || "Y".equals(admitted);

                            if (isAdmitted) {
                                final ApplicantRecord newRec = new ApplicantRecord(csuId, firstName, lastName, prefName,
                                        middleName, birthDate, gender, email, admType, stuClass, hsCode, residency,
                                        residencyState, residencyCounty, hsGpa, hsClassRank, hsClassSize, actMath,
                                        usedSat, pidm, applicationTerm, applicationDate, gradTerm, campus, isAdmitted,
                                        programOfStudy);

                                final ApplicantRecord existing = result.get(csuId);

                                if (existing == null || newRec.isMoreRecentThan(existing)) {
                                    result.put(csuId, newRec);
                                }
                            }
                        }
                    } else {
                        report.add("ODS record had bad student ID: '" + csuId + "'");
                    }
                }
            }
        }

        return result;
    }

    /**
     * Processes a list of applicant records.
     *
     * @param cache      the data cache
     * @param applicants the list of applicants from the ODS
     * @param report     a list of strings to which to add report output lines
     * @throws SQLException if there is an error accessing the database
     */
    private static void processList(final Cache cache, final Map<String, ApplicantRecord> applicants,
                                    final Collection<? super String> report) throws SQLException {

        try {
            final List<RawApplicant> currentApplicants = RawApplicantLogic.queryAll(cache);
            final int numCurrent = currentApplicants.size();
            final Map<String, RawApplicant> currentSorted = new HashMap<>(numCurrent);
            for (final RawApplicant applicant : currentApplicants) {
                currentSorted.put(applicant.stuId, applicant);
            }

            int total = currentSorted.size();

            report.add("There are " + applicants.size() + " admitted records from ODS");
            report.add("There are " + total + " records in local APPLICANTS table");

            int numInserted = 0;
            int numUpdated = 0;
            int numDeleted = 0;

            final Set<String> studentIds = new HashSet<>(120000);

            for (final ApplicantRecord record : applicants.values()) {

                if (studentIds.contains(record.csuId)) {
                    Log.warning("Multiple rows for " + record.csuId);
                } else {
                    studentIds.add(record.csuId);
                }


                final RawApplicant existing = currentSorted.get(record.csuId);

                if (existing == null) {
                    // INSERT A NEW RECORD
                    insertNew(cache, record, report);
                    ++numInserted;
                    ++total;
                } else {
                    // RECONCILE WITH EXISTING RECORD
                    numUpdated += reconcile(cache, record, existing, report);
                    currentSorted.remove(record.csuId);
                }
            }

            // Any rows left from the local table can be deleted
            for (final RawApplicant toDelete : currentSorted.values()) {
                report.add("Deleting obsolete record for " + toDelete.stuId);

                if (!DEBUG) {
                    RawApplicantLogic.delete(cache, toDelete);
                }
                ++numDeleted;
                --total;
            }

            report.add(CoreConstants.EMPTY);
            report.add("  Number of APPLICANT records inserted:  " + numInserted);
            report.add("  Number of APPLICANT records updated:   " + numUpdated);
            report.add("  Number of APPLICANT records deleted:   " + numDeleted);
            report.add("  Final number of APPLICANT records:     " + total);

        } catch (final SQLException ex) {
            Log.warning(ex);
            final String msg = ex.getMessage();
            report.add("Database exception: " + msg);
        }
    }

    /**
     * Inserts a new record in the local APPLICANT table.
     *
     * @param cache   the data cache
     * @param odsData the ODS record
     * @throws SQLException if there is an error accessing the database
     */
    private static void insertNew(final Cache cache, final ApplicantRecord odsData,
                                  final Collection<? super String> report) throws SQLException {

        final LocalDate birthDate = odsData.birthDate == null ? null : odsData.birthDate.toLocalDate();

        final RawApplicant newRow = new RawApplicant(odsData.csuId, odsData.firstName, odsData.lastName, birthDate,
                null, odsData.gender, odsData.college, odsData.programOfStudy, odsData.hsCode, null,
                odsData.residency, odsData.residencyState, odsData.residencyCounty, odsData.hsGpa,
                odsData.hsClassRank, odsData.hsClassSize, odsData.actMath, odsData.satMath, odsData.pidm,
                odsData.applicationTerm);

        report.add("Inserting applicant record for " + odsData.csuId);

        if (!DEBUG) {
            RawApplicantLogic.insert(cache, newRow);
        }
    }

    /**
     * Reconciles ODS and local data for a single applicant.
     *
     * @param cache     the data cache
     * @param odsData   the ODS record
     * @param localData the local record
     * @return 1 if the record was updated; 0 if not (the number of records updated)
     * @throws SQLException if there is an error accessing the database
     */
    private static int reconcile(final Cache cache, final ApplicantRecord odsData, final RawApplicant localData,
                                 final Collection<? super String> report)
            throws SQLException {

        final LocalDate birthDate = odsData.birthDate == null ? null : odsData.birthDate.toLocalDate();

        final boolean same = Objects.equals(odsData.firstName, localData.firstName)
                             && Objects.equals(odsData.lastName, localData.lastName)
                             && Objects.equals(birthDate, localData.birthdate)
                             && Objects.equals(odsData.gender, localData.gender)
                             && Objects.equals(odsData.college, localData.college)
                             && Objects.equals(odsData.programOfStudy, localData.progStudy)
                             && Objects.equals(odsData.hsCode, localData.hsCode)
                             && Objects.equals(odsData.residency, localData.resident)
                             && Objects.equals(odsData.residencyState, localData.residentState)
                             && Objects.equals(odsData.residencyCounty, localData.residentCounty)
                             && Objects.equals(odsData.hsGpa, localData.hsGpa)
                             && Objects.equals(odsData.hsClassRank, localData.hsClassRank)
                             && Objects.equals(odsData.hsClassSize, localData.hsSizeClass)
                             && Objects.equals(odsData.actMath, localData.actScore)
                             && Objects.equals(odsData.satMath, localData.satScore)
                             && Objects.equals(odsData.pidm, localData.pidm)
                             && Objects.equals(odsData.applicationTerm, localData.aplnTerm);

        int result = 0;

        if (!same) {
            report.add("Updating applicant record for " + odsData.csuId);

            if (!DEBUG) {
                RawApplicantLogic.delete(cache, localData);

                final RawApplicant newRow = new RawApplicant(odsData.csuId, odsData.firstName, odsData.lastName,
                        birthDate, null, odsData.gender, odsData.college, odsData.programOfStudy, odsData.hsCode, null,
                        odsData.residency, odsData.residencyState, odsData.residencyCounty, odsData.hsGpa,
                        odsData.hsClassRank, odsData.hsClassSize, odsData.actMath, odsData.satMath, odsData.pidm,
                        odsData.applicationTerm);

                RawApplicantLogic.insert(cache, newRow);
                result = 1;
            }
        }

        return result;
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        DbConnection.registerDrivers();

        final ImportOdsApplicants2 job = new ImportOdsApplicants2();

        Log.fine(job.execute());
    }

    /**
     * An applicant record (corresponds to the "applicant" table in the math schema)
     */
    private static final class ApplicantRecord {

        /** The CSU ID. */
        final String csuId;

        /** The first name. */
        final String firstName;

        /** The last name. */
        final String lastName;

        /** The preferred name. */
        final String prefName;

        /** The middle initial. */
        final String middleInitial;

        /** The birthdate. */
        final Date birthDate;

        /** The gender. */
        final String gender;

        /** The email. */
        final String email;

        /** The admit type. */
        final String admType;

        /** The college. */
        String college;

        /** The department. */
        String department;

        /** The program of study. */
        String programOfStudy;

        /** The student class. */
        final String studentClass;

        /** The high school code. */
        final String hsCode;

        /** The residency. */
        final String residency;

        /** The residency state. */
        final String residencyState;

        /** The residency state. */
        final String residencyCounty;

        /** The high school GPA. */
        final String hsGpa;

        /** The high school class rank. */
        final Integer hsClassRank;

        /** The high school class size. */
        final Integer hsClassSize;

        /** The ACT math score. */
        final Integer actMath;

        /** The SAT math score */
        final Integer satMath;

        /** The PIDM. */
        final Integer pidm;

        /** The application term. */
        final TermKey applicationTerm;

        /** The application date. */
        final Date applicationDate;

        /** The anticipated graduation term. */
        final String gradTerm;

        /** The campus. */
        final String campus;

        /** True if student is admitted; false if still an applicant. */
        final Boolean admitted;

        /**
         * Constructs a new {@code ApplicantRecord}.
         *
         * @param theCsuId            the CSU ID
         * @param theFirstName        the first name
         * @param theLastName         the last name
         * @param thePrefName         the preferred name
         * @param theMiddleName       the middle name
         * @param theBirthDate        the birthdate
         * @param theGender           the gender
         * @param theEmail            the email
         * @param theAdmType          the admit type
         * @param theStudentClass     the student class
         * @param theHsCode           the high school code
         * @param theResidency        the residency
         * @param theResidencyState   the residency state
         * @param theResidencyCountry the residency country
         * @param theHsGpa            the high school GPA
         * @param theHsClassRank      the high school class rank
         * @param theHsClassSize      the high school class size
         * @param theActMath          the ACT math score
         * @param theSatMath          the SAT math score
         * @param thePidm             the PIDM
         * @param theApplicationTerm  the application term
         * @param theApplicationDate  the application date
         * @param theGradTerm         the anticipated graduation term
         * @param theCampus           the campus
         * @param theAdmitted         true if student is admitted; false if still an applicant
         * @param theProgram          the program of study
         */
        ApplicantRecord(final String theCsuId, final String theFirstName, final String theLastName,
                        final String thePrefName, final String theMiddleName, final Date theBirthDate,
                        final String theGender, final String theEmail, final String theAdmType,
                        final String theStudentClass, final String theHsCode, final String theResidency,
                        final String theResidencyState, final String theResidencyCountry, final String theHsGpa,
                        final Integer theHsClassRank, final Integer theHsClassSize, final Integer theActMath,
                        final Integer theSatMath, final Integer thePidm, final TermKey theApplicationTerm,
                        final Date theApplicationDate, final String theGradTerm, final String theCampus,
                        final Boolean theAdmitted, final String theProgram) {

            this.csuId = theCsuId;
            this.firstName = prune(theFirstName, 30);
            this.lastName = prune(theLastName, 30);
            this.prefName = prune(thePrefName, 30);
            this.middleInitial = prune(theMiddleName, 1);
            this.birthDate = theBirthDate;
            this.gender = prune(theGender, 1);
            this.email = prune(theEmail, 60);
            this.admType = prune(theAdmType, 2);
            this.studentClass = prune(theStudentClass, 2);
            this.hsCode = prune(theHsCode, 6);
            this.residency = prune(theResidency, 4);
            this.residencyState = prune(theResidencyState, 4);
            this.residencyCounty = prune(theResidencyCountry, 6);
            this.hsGpa = prune(theHsGpa, 4);
            this.hsClassRank = theHsClassRank;
            this.hsClassSize = theHsClassSize;
            this.actMath = theActMath;
            this.satMath = theSatMath;
            this.pidm = thePidm;
            this.applicationTerm = theApplicationTerm;
            this.applicationDate = theApplicationDate;
            this.gradTerm = theGradTerm;
            this.campus = prune(theCampus, 20);
            this.admitted = theAdmitted;
            this.programOfStudy = theProgram;
        }

        /**
         * Prunes a string, so it does not exceed a maximum length.
         *
         * @param str    the string to prune
         * @param maxLen the maximum length
         * @return the pruned string
         */
        static String prune(final String str, final int maxLen) {

            String result = null;

            if (str != null) {
                final String trimmed = str.trim();
                result = trimmed.length() > maxLen ? trimmed.substring(0, maxLen).trim() : trimmed;
            }

            return result;
        }

        /**
         * Tests whether this record is "more recent than" another. Used when there are multiple records for a student
         * to determine which one wins.
         *
         * @param other the record against which to compare
         * @return true if this record is more recent (should win and be used)
         */
        boolean isMoreRecentThan(final ApplicantRecord other) {

            Boolean moreRecent = null;

            // Test 1: later application term should win
            if (this.applicationTerm == null) {
                if (other.applicationTerm != null) {
                    moreRecent = Boolean.FALSE;
                }
            } else if (other.applicationTerm == null) {
                moreRecent = Boolean.TRUE;
            } else {
                final int comp = this.applicationTerm.compareTo(other.applicationTerm);
                if (comp > 0) {
                    moreRecent = Boolean.TRUE;
                } else if (comp < 0) {
                    moreRecent = Boolean.FALSE;
                }
            }

            if (moreRecent == null) {
                // Test 2: latest application date should win
                if (this.applicationDate == null) {
                    if (other.applicationDate != null) {
                        moreRecent = Boolean.FALSE;
                    }
                } else if (other.applicationDate == null) {
                    moreRecent = Boolean.TRUE;
                } else {
                    final int comp = this.applicationDate.compareTo(other.applicationDate);
                    if (comp > 0) {
                        moreRecent = Boolean.TRUE;
                    } else if (comp < 0) {
                        moreRecent = Boolean.FALSE;
                    }
                }
            }

            if (moreRecent == null) {
                Log.warning("Multiple applicant records for ", other.csuId, " for ", this.applicationTerm);

                // Test 3: favor the record with more information
                final int myScore = score();
                final int otherScore = other.score();

                if (myScore > otherScore) {
                    moreRecent = Boolean.TRUE;
                } else if (myScore < otherScore) {
                    moreRecent = Boolean.FALSE;
                } else { // Test 3: later anticipated graduation term should win
                    final Boolean hashCompare = Boolean.valueOf(hashCode() >= other.hashCode());

                    if (this.gradTerm == null) {
                        if (other.gradTerm == null) {
                            // Last resort: Make arbitrary but repeatable decision
                            moreRecent = hashCompare;
                        } else {
                            moreRecent = Boolean.FALSE;
                        }
                    } else if (other.gradTerm == null) {
                        moreRecent = Boolean.TRUE;
                    } else {
                        final int comp = this.gradTerm.compareTo(other.gradTerm);
                        if (comp > 0) {
                            moreRecent = Boolean.TRUE;
                        } else if (comp < 0) {
                            moreRecent = Boolean.FALSE;
                        } else {
                            // Last resort: Make arbitrary but repeatable decision
                            moreRecent = hashCompare;
                        }
                    }
                }
            }

            return moreRecent.booleanValue();
        }

        /**
         * Generates a "completeness" score based on the number of fields populated.
         *
         * @return the score
         */
        private int score() {

            return (this.college == null ? 0 : 1) + (this.department == null ? 0 : 1)
                   + (this.programOfStudy == null ? 0 : 1) + (this.studentClass == null ? 0 : 1)
                   + (this.hsCode == null ? 0 : 1) + (this.hsGpa == null ? 0 : 1)
                   + (this.hsClassRank == null ? 0 : 1) + (this.hsClassSize == null ? 0 : 1)
                   + (this.actMath == null ? 0 : 1) + (this.satMath == null ? 0 : 1)
                   + (this.gradTerm == null ? 0 : 1) + (this.campus == null ? 0 : 1);
        }

        /**
         * Builds a {@code RawStudent} record for the applicant.
         *
         * @return the student record
         */
        RawStudent toStudent() {

            final String res;
            if (this.residencyState == null) {
                res = this.residency;
            } else if (this.residency == null) {
                res = this.residencyState;
            } else {
                final String r1 = this.residency;
                final String r2 = this.residencyState;
                res = r2.length() >= r1.length() ? r2 : r1;
            }

            TermKey studentGradTerm = null;
            if (this.gradTerm != null) {
                final String grad = this.gradTerm;
                if (grad.length() == 6) {
                    final int year = Integer.parseInt(grad.substring(0, 4));

                    if (year > 2000 && year < 3000) {
                        final int code = Integer.parseInt(grad.substring(4));

                        if (code == 10) {
                            studentGradTerm = new TermKey(ETermName.SPRING, year);
                        } else if (code == 60) {
                            studentGradTerm = new TermKey(ETermName.SUMMER, year);
                        } else if (code == 90) {
                            studentGradTerm = new TermKey(ETermName.FALL, year);
                        } else {
                            Log.warning("Invalid Grad term = '", grad, "'");
                        }
                    } else {
                        Log.warning("Invalid Grad term = '", grad, "'");
                    }
                } else {
                    try {
                        studentGradTerm = new TermKey(grad);
                    } catch (final IllegalArgumentException ex) {
                        Log.warning(ex);
                    }
                }
            }

            LocalDate bday = null;
            if (this.birthDate != null) {
                bday = this.birthDate.toLocalDate();
            }

            final String gpa = cleanGpa();

            return new RawStudent(this.csuId, this.pidm, this.lastName, this.firstName,
                    this.prefName, this.middleInitial, this.applicationTerm, this.studentClass,
                    this.college, this.department, this.programOfStudy, null, studentGradTerm, null,
                    this.hsCode, gpa, this.hsClassRank, this.hsClassSize, this.actMath, this.satMath,
                    null, res, bday, null, this.gender, "N", null, null, null,
                    "N", this.campus, this.email, null, null, this.admType,
                    "N", null, LocalDate.now(), null, null);
        }

        /**
         * Cleans the "GPA" field, removing trailing ".0" or "." if present"
         *
         * @return the cleaned GPA string.
         */
        private String cleanGpa() {

            String cleaned = this.hsGpa;

            if (cleaned != null) {
                final int dot = cleaned.indexOf('.');
                if (dot >= 0) {
                    int last = cleaned.length() - 1;
                    while (last >= 0 && cleaned.charAt(last) == '0') {
                        cleaned = cleaned.substring(0, last);
                        --last;
                    }
                    while (last >= 0 && cleaned.charAt(last) == '.') {
                        cleaned = cleaned.substring(0, last);
                        --last;
                    }
                }
            }

            return cleaned;
        }
    }
}
