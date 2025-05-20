package dev.mathops.dbjobs.batch;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.logic.mathplan.MathPlanLogic;
import dev.mathops.db.old.logic.mathplan.MathPlanPlacementStatus;
import dev.mathops.db.old.rawlogic.RawMpscorequeueLogic;
import dev.mathops.db.old.rawlogic.RawStmathplanLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawMpscorequeue;
import dev.mathops.db.old.rawrecord.RawStmathplan;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A job that searches for all students with a "WLCM5" MathPlan outcome, and then make sures that student has a
 * corresponding "MPL" test score in SORTEST.  If the SORTEST record is missing, a record is inserted.
 *
 * <p>
 * The logic for an individual student is as follows:
 * <pre>
 * IF
 *     the student has any MATH PLAN 'WLCM5' response records
 * THEN
 *     IF
 *         the student's program on record is any that needs only AUCC-1B
 *     THEN
 *         the student should have a "MPL" test score of "1" to indicate Math Placement is not needed
 *     ELSE IF
 *         the student's Math Plan recommendation is any AUCC-1B course
 *     THEN
 *         the student should have a "MPL" test score of "1" to indicate Math Placement is not needed
 *     ELSE IF
 *         the student has completed the Math Placement tool
 *     THEN
 *         the student should have a "MPL" test score of "1" to indicate Math Placement is not needed
 *     ELSE IF
 *         the student has any MATH transfer credit that clears 1B or satisfies the prerequisite for MATH 117
 *     THEN
 *         the student should have a "MPL" test score of "1" to indicate Math Placement is not needed
 *     ELSE
 *         the student should have a "MPL" test score of "2" to indicate Math Placement is needed
 *     END IF
 * ELSE
 *     the student should not have any "MPL" test score at all, to indicate the Math Plan is not yet complete
 * END IF
 * </pre>
 */
public final class BulkUpdateMPLTestScores {

    private static final List<String> MAJORS_NEEDING_ONLY_AUCC = Arrays.asList("ECHE-BS", "FACS-BS", "FACS-FCSZ-BS",
            "FACS-IDSZ-BS", "HDFS-BS", "HDFS-ECPZ-BS", "HDFS-HDEZ-BS", "HDFS-LADZ-BS", "HDFS-PHPZ-BS", "HDFS-PISZ-BS"
            , "SOWK-BSW", "HDFS-BS", "HDFS-ECPZ-BS", "HDFS-HDEZ-BS", "HDFS-LADZ-BS", "HDFS-PHPZ-BS", "HDFS-PISZ-BS",
            "SOWK-BSW", "SOWK-ADSZ-BSW", "ANTH-BA", "ANTH-ARCZ-BA", "ANTH-BIOZ-BA", "ANTH-CLTZ-BA", "ARTI-BA",
            "ARTI-ARTZ-BA", "ARTI-IVSZ-BA", "ARTM-BFA", "ARTM-DRAZ-BF", "ARTM-ELAZ-BF", "ARTM-FIBZ-BF", "ARTM-GRDZ-BF",
            "ARTM-METZ-BF", "ARTM-PNTZ-BF", "ARTM-PHIZ-BF", "ARTM-POTZ-BF", "ARTM-PRTZ-BF", "ARTM-SCLZ-BF",
            "ARTM-AREZ-BF", "CMST-BA", "CMST-TCLZ-BA", "DNCE-BA", "DANC-BFA", "ENGL-BA", "ENGL-CRWZ-BA", "ENGL-ENEZ-BA",
            "ENGL-LINZ-BA", "ENGL-LITZ-BA", "ENGL-WRLZ-BA", "ETST-BA", "ETST-COIZ-BA", "ETST-RPRZ-BA", "ETST-SOTZ-BA",
            "GEOG-BS", "HIST-BA", "HIST-GENZ-BA", "HIST-LNGZ-BA", "HIST-SBSZ-BA", "HIST-SSTZ-BA", "HIST-DPUZ-BA",
            "JAMC-BA", "LLAC-BA", "LLAC-LFRZ-BA", "LLAC-LGEZ-BA", "LLAC-LSPZ-BA", "LLAC-SPPZ-BA", "MUSI-BA", "MUSC-BM",
            "MUSC-COMZ-BM", "MUSC-MUEZ-BM", "MUSC-MUTZ-BM", "MUSC-PERZ-BM", "PHIL-BA", "PHIL-GNPZ-BA", "PHIL-GPRZ-BA",
            "PHIL-PSAZ-BA", "POLS-BA", "POLS-EPAZ-BA", "POLS-GPPZ-BA", "POLS-ULPZ-BA", "POLS-LPGZ-BA", "POLS-PPSZ-BA",
            "SOCI-BA", "SOCI-CRCZ-BA", "SOCI-ENSZ-BA", "SOCI-GNSZ-BA", "THTR-BA", "THTR-MUSZ-BA", "THTR-PRFZ-BA",
            "THTR-CDTZ-BA", "THTR-LDTZ-BA", "THTR-PDTZ-BA", "THTR-SDSZ-BA", "THTR-SDTZ-BA", "WGST-BA", "INST-BA",
            "INST-ASTZ-BA", "INST-EUSZ-BA", "INST-GBLZ-BA", "INST-LTSZ-BA", "INST-MEAZ-BA", "ILAR-BA",
            // Below are not in catalog
            "THTR-DTHZ-BA", "CMST-DD-BA", "HDFS-DHDZ-BS", "HDFS-DECZ-BS", "SOCI-DGSZ-BA", "ANTH-DD-BA", "HDFS-DPHZ-BS",
            "JAMC-DD-BA", "ILAR-DD-BA", "POLS-DD-BA", "DANC-DEDZ-BF", "HDFS-LEPZ-BS", "HDFS-DPIZ-BS", "ENGL-LANZ-BA",
            "HDFS-DLAZ-BS", "SOWK-ADSZ-BW", "HDFS-DLEZ-BS", "SPCM-TCLZ-BA",
            "MUS0", // Pre-music
            "UNLA", // Assuming liberal arts
            "DNC0", // Assuming Dance
            "THR0" // Assuming Theatre
    );

    private static final List<String> MAJORS_NEEDING_MORE = Arrays.asList(
            "AGBI-BS", "AGBI-ENTZ-BS", "AGBI-PLPZ-BS", "AGBI-WEEZ-BS", "AGBU-BS", "AGBU-AECZ-BS", "AGBU-FRCZ-BS",
            "AGBU-FSSZ-BS", "AGED-BS", "AGED-AGLZ-BS", "AGED-TDLZ-BS", "ANIM-BS", "ENRE-BS", "ENHR-BS", "ENHR-LDAZ-BS",
            "ENHR-NALZ-BS", "ENHR-TURZ-BS", "EQSC-BS", "HORT-BS", "HORT-CEHZ-BS", "HORT-HBMZ-BS", "HORT-HFCZ-BS",
            "HORT-HOSZ-BS", "LDAR-BS", "LSBM-BS", "SOCR-BS", "SOCR-PBTZ-BS", "SOCR-SESZ-BS", "SOCR-SAMZ-BS", "BUSA-BS",
            "BUSA-ACCZ-BS", "BUSA-FINZ-BS", "BUSA-FPLZ-BS", "BUSA-HRMZ-BS", "BUSA-INSZ-BS", "BUSA-MINZ-BS",
            "BUSA-MKTZ-BS", "BUSA-REAZ-BS", "BUSA-SCMZ-BS", "BUSA-SUSZ-BS", "CBEG-DUAL", "CBEG-BMEC-BS", "CPEG-BMEP-BS",
            "ELEG-BMEE-BS", "ELEG-BMEL-BS", "MECH-BMEM-BS", "CBEG-BS", "CBEG-MLMZ-BS", "CBEG-BIMZ-BS", "CBEG-SSEZ-BS",
            "CIVE-BS", "CPEG-BS", "CPEG-AESZ-BS", "CPEG-EISZ-BS", "CPEG-NDTZ-BS", "CPEG-VICZ-BS", "ELEG-BS",
            "ELEG-ELEZ-BS", "ELEG-LOEZ-BS", "ELEG-ASPZ-BS", "ENVE-BS", "MECH-BS", "MECH-ACEZ-BS", "MECH-ADMZ-BS",
            "MECH-ASU-BS", "APAM-BS", "APAM-ADAZ-BS", "APAM-MDSZ-BS", "APAM-PDVZ-BS", "CTMG-BS", "FMST-BS", "HAES-BS",
            "HAES-HPRZ-BS", "HAES-SPMZ-BS", "HAES-EXSZ-BS", "HSMG-BS", "IARD-BS", "IARD-IADZ-BS", "IARD-IPRZ-BS",
            "NAFS-BS", "NAFS-DNMZ-BS", "NAFS-FSCZ-BS", "NAFS-NFTZ-BS", "NAFS-PHNZ-BS", "NUTR-BS", "NUTR-DINZ-BS",
            "NUTR-PHLZ-BS", "NUTR-SNWZ-BS", "ECON-BA", "ECSS-BS", "FWCB-BS", "FWCB-CNVZ-BS", "FWCB-FASZ-BS",
            "FWCB-WDBZ-BS", "FRRS-BS", "FRRS-FRBZ-BS", "FRRS-FRFZ-BS", "FRRS-FMGZ-BS", "FRRS-RFMZ-BS", "FRRS-RCMZ-BS",
            "GEOL-BS", "GEOL-EVGZ-BS", "GEOL-GEOZ-BS", "GEOL-GPYZ-BS", "GEOL-HYDZ-BS", "HDNR-BS", "NRRT-BS",
            "NRRT-GLTZ-BS", "NRRT-NRTZ-BS", "NRMG-BS", "RECO-BS", "WRSC-BS", "WRSC-WSDZ-BS", "WRSC-WSSZ-BS",
            "WRSC-WSUZ-BS", "BCHM-BS", "BCHM-ASBZ-BS", "BCHM-DTSZ-BS", "BCHM-HMSZ-BS", "BCHM-PPHZ-BS", "BLSC-BS",
            "BLSC-BLSZ-BS", "BLSC-BTNZ-BS", "CHEM-BS", "CHEM-ECHZ-BS", "CHEM-FCHZ-BS", "CHEM-HSCZ-BS", "CHEM-SCHZ-BS",
            "CHEM-MTRZ-BS", "CPSC-BS", "CPSC-CPSZ-BS", "CPSC-HCCZ-BS", "CPSC-AIMZ-BS", "CPSC-CSYZ-BS", "CPSC-NSCZ-BS",
            "CPSC-SEGZ-BS", "CPSC-CSEZ-BS", "CPSC-DCCZ-BS", "DSCI-BS", "DSCI-CSCZ-BS", "DSCI-ECNZ-BS", "DSCI-MATZ-BS",
            "DSCI-STSZ-BS", "DSCI-NEUZ-BS", "DSCI-DCMZ-BS", "MATH-BS", "MATH-ALSZ-BS", "MATH-AMTZ-BS", "MATH-GNMZ-BS",
            "MATH-MTEZ-BS", "MATH-CPMZ-BS", "NSCI-BS", "NSCI-BLEZ-BS", "NSCI-CHEZ-BS", "NSCI-GLEZ-BS", "NSCI-PHSZ-BS",
            "NSCI-PHEZ-BS", "PHYS-BS", "PHYS-APPZ-BS", "PHYS-PHYZ-BS", "PSYC-BS", "PSYC-ADCZ-BS", "PSYC-CCPZ-BS",
            "PSYC-GPSZ-BS", "PSYC-IOPZ-BS", "PSYC-MBBZ-BS", "PSYC-AACZ-BS", "STAT-BS", "ZOOL-BS", "BIOM-BS",
            "BIOM-APHZ-BS", "BIOM-EPHZ-BS", "BIOM-MIDZ-BS", "NERO-BS", "NERO-BCNZ-BS", "NERO-CMNZ-BS", "HEMG-BS",
            "FAFS-BS", "FAFS-FSTZ-BS", "FAFS-FSIZ-BS", "HLPH-DD-BS",
            // Below are not in catalog
            "PSYC-GDSZ-BS", "CPSC-DCSZ-BS", "HORT-DHBZ-BS", "BUSA-OIMZ-BS", "AGBU-DD-BS", "WSSS-WSDZ-BS", "WSSS-BS",
            "NRTM-NRTZ-BS", "NRTM-GLTZ-BS", "FESV-DD-BS", "CHEM-ACSZ-BS", "WSSS-WSSZ-BS", "EVHL-BS",
            "WSSS-WSUZ-BS", "NAFS-NUSZ-BS", "ENRE-DD-BS", "ECON-DD-BA", "SOCR-DSAZ-BS", "CPSC-DSEZ-BS", "MICR-BS",
            "N2CP-CPSY-UG", "NAFS-FSNZ-BS", "NRTM-DNRZ-BS", "BUSA-DACZ-BS", "CPSC-DHCZ-BS", "CPSC-DCYZ-BS",
            "CPSC-DAIZ-BS", "SOCR-SOEZ-BS", "APCT-CPTZ-BS", "BCHM-GBCZ-BS", "CPSC-DNSZ-BS", "FRST-FMGZ-BS",
            "SOCR-PBGZ-BS", "SOCR-ISCZ-BS", "CPSC-CFCZ-BS", "SOCR-APMZ-BS", "NAFS-FSYZ-BS",
            "CTM0", // Pre-construction management
            "EXPL", "EXLA", // Exploratory studies: Land, Plant, and Animal Science
            "EXHF", // Exploratory studies: Health, Life, and Food
            "EXOM", // Exploratory studies: Organization, Management, Ent.
            "EXNR", // Exploratory studies: Environmental and Natural Sci.
            "EXPE", // Exploratory studies: Physical Science and Engineering
            "EXTC", // Exploratory studies: Education and Teaching
            "EXPO", // Exploratory studies: ???
            "EXCO", // Exploratory studies: ???
            "EXAD", // Exploratory studies: ???
            "EXGS", // Exploratory studies: ???
            "USEG", // Exploratory studies: Engineering
            "USBU", // Exploratory studies: Business interest
            "USBS", // Exploratory studies: Life Sciences
            "USCS", // Exploratory studies: IT
            "USJC", // Exploratory studies: ???
            "IAD0", // Unknown - assume it needs some math...
            "EGOP", // Unknown - assume it needs some math...
            "CSOR", // Unknown - assume it needs some math...
            "N2IE-SI", // Unknown - assume it needs some math...
            "GUES-CEUG", // Unknown - assume it needs some math...
            "N2EG-ENGX-UG", // Unknown - assume it needs some math...
            "GRAD-UG", // Unknown - assume it needs some math...
            "SPCL-UG", // Unknown - assume it needs some math...
            "CTED-UG", // Unknown - assume it needs some math...
            "FCST-UG",  // Unknown - assume it needs some math...
            "SSAS-UG"  // Unknown - assume it needs some math....m
    );

    /** Debug flag - true to skip (but print) updates; false to actually perform updates. */
    private static final boolean DEBUG = false;

    /** The test code. */
    private static final String TEST_CODE = "MPL";

    /** A commonly used integer. */
    private static final Integer ONE = Integer.valueOf(1);

    /** The database profile through which to access the database. */
    private final Profile profile;

    /**
     * Constructs a new {@code BulkUpdateMPLTestScores}.
     */
    public BulkUpdateMPLTestScores() {

        final DatabaseConfig config = DatabaseConfig.getDefault();
        this.profile = config.getCodeProfile(Contexts.BATCH_PATH);
    }

    /**
     * Executes the job.
     *
     * @return the report
     */
    public String execute() {

        final Collection<String> report = new ArrayList<>(10);

        if (this.profile == null) {
            final String msg = "Unable to create production profile.";
            Log.warning(msg);
            report.add(msg);
        } else {
            final Cache cache = new Cache(this.profile);

            try {
                execute(cache, report);
            } catch (final SQLException ex) {
                final String exMsg = ex.getMessage();
                final String msg = HtmlBuilder.concat("EXCEPTION: ", exMsg);
                Log.warning(msg);
                report.add(msg);
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
     * Executes the job.
     *
     * @param cache  the data cache
     * @param report a list of strings to which to add report output lines
     * @throws SQLException if there is an error accessing the database
     */
    public void execute(final Cache cache, final Collection<? super String> report) throws SQLException {

        // Determine the list of students who should have MPL test scores of some kind
        final String msg1 = "Scanning student MathPlan status...";
        Log.fine(msg1);
        report.add(msg1);

        final List<RawStmathplan> allStMathPlan = RawStmathplanLogic.queryAll(cache);
        final int size = allStMathPlan.size();
        final String sizeStr = Integer.toString(size);

        final String msg2 = HtmlBuilder.concat("    Found ", sizeStr, " MathPlan responses");
        report.add(msg2);
        Log.fine(msg2);

        final Map<String, RawStmathplan> latest1 = new HashMap<>(25000);

        // Find the most recent "WLCM5" row with survey_nbr='1', use that as date/time of Math Plan completion - we
        // scan for the most recent such record.

        for (final RawStmathplan row : allStMathPlan) {
            if ("WLCM5".equals(row.version) && ONE.equals(row.surveyNbr)) {
                final LocalDateTime when = row.getWhen();
                final RawStmathplan existing1 = latest1.get(row.stuId);
                if (existing1 == null) {
                    latest1.put(row.stuId, row);
                } else {
                    final LocalDateTime existingWhen = existing1.getWhen();
                    if (existingWhen == null || when.isAfter(existingWhen)) {
                        latest1.put(row.stuId, row);
                    }
                }
            }
        }

        final int size1 = latest1.size();
        final String size1Str = Integer.toString(size1);
        final String msg3 = HtmlBuilder.concat("    Found ", size1Str, " 'WLCM5' question 1 responses");
        Log.fine(msg3);
        report.add(msg3);

        final Collection<String> stuIds = new HashSet<>(25000);
        final Set<String> keys1 = latest1.keySet();
        stuIds.addAll(keys1);

        final int sizeAll = stuIds.size();
        final String sizeAllStr = Integer.toString(sizeAll);
        final String msg4 = HtmlBuilder.concat("    Found ", sizeAllStr, " distinct students with responses");
        Log.fine(msg4);
        report.add(msg4);

        // Compare results with SORTEST table
        Log.fine(CoreConstants.EMPTY);
        final String msg5 = "Scanning SORTEST table...";
        Log.fine(msg5);
        report.add(msg5);

        final Login login = this.profile.getLogin(ESchema.LIVE);

        final DbConnection liveConn = login.checkOutConnection();
        final LocalDateTime now = LocalDateTime.now();
        try {
            int count1 = 0;
            int count2 = 0;

            for (final String stuId : stuIds) {
                RawStudent student = RawStudentLogic.query(cache, stuId, false);
                if (student == null) {
                    final String msg = HtmlBuilder.concat("   WARNING: Student ", stuId, " needed to be retrieved");
                    Log.fine(msg);
                    report.add(msg);
                    student = RawStudentLogic.query(cache, stuId, true);
                }

                if (student == null) {
                    final String msg = HtmlBuilder.concat("   ERROR: Student ", stuId, " not found!");
                    Log.warning(msg);
                    report.add(msg);
                } else if (student.pidm == null) {
                    final String msg = HtmlBuilder.concat("   ERROR: Student ", stuId, " has no PIDM!");
                    Log.warning(msg);
                    report.add(msg);
                } else {
                    final List<RawMpscorequeue> existing = RawMpscorequeueLogic.querySORTESTByStudent(liveConn,
                            student.pidm);

                    RawMpscorequeue mostRecent = null;
                    for (final RawMpscorequeue test : existing) {
                        if (TEST_CODE.equals(test.testCode)) {
                            if (mostRecent == null || mostRecent.testDate.isBefore(test.testDate)) {
                                mostRecent = test;
                            }
                        }
                    }

                    String wantValue = null;
                    if (isProgramOnlyAUCC(student, report)) {
                        wantValue = "1";
                    } else {
                        final MathPlanPlacementStatus status = MathPlanLogic.getMathPlacementStatus(cache, stuId);

                        if (latest1.containsKey(stuId)) {
                            if (status.isPlacementComplete) {
                                wantValue = "1";
                            } else if (status.isPlacementNeeded) {
                                wantValue = "2";
                            } else {
                                wantValue = "1";
                            }
                        }
                    }

                    boolean doInsert = false;
                    if (wantValue == null) {
                        if (mostRecent != null) {
                            final String msg = HtmlBuilder.concat("Student ", stuId,
                                    " who has not completed MathPlan has a MPL score of ",
                                    mostRecent.testScore);
                            Log.warning(msg);
                            report.add(msg);
                        }
                    } else if (mostRecent == null) {
                        // Insert the new score
                        doInsert = true;
                    } else if (!wantValue.equals(mostRecent.testScore)) {
                        // Score has changed - insert a new score
                        doInsert = true;
                    }

                    if (doInsert) {
                        // Score has changed - insert a new score
                        if (DEBUG) {
                            final String msg = HtmlBuilder.concat("   Need to insert MPL=", wantValue,
                                    " test score " + "for ", stuId);
                            Log.fine(msg);
                            report.add(msg);
                        } else {
                            final String msg = HtmlBuilder.concat("   Inserting MPL=", wantValue, " test score for ",
                                    stuId);
                            Log.fine(msg);
                            report.add(msg);

                            final RawMpscorequeue toInsert = new RawMpscorequeue(student.pidm, TEST_CODE, now,
                                    wantValue);
                            if (!RawMpscorequeueLogic.insertSORTEST(liveConn, toInsert)) {
                                final String msg6 = HtmlBuilder.concat("   ERROR: Failed to insert MPL=", wantValue,
                                        " test score for ", stuId);
                                Log.warning(msg6);
                                report.add(msg6);
                            }
                        }
                        if ("2".equals(wantValue)) {
                            ++count2;
                        } else {
                            ++count1;
                        }
                    }
                }
            }

            final String count1Str = Integer.toString(count1);
            final String msg6 = HtmlBuilder.concat("    Found ", count1Str, " to update to score 1");
            Log.fine(msg6);
            report.add(msg6);

            final String count2Str = Integer.toString(count2);
            final String msg7 = HtmlBuilder.concat("    Found ", count2Str, " to update to score 2");
            Log.fine(msg7);
            report.add(msg7);
        } finally {
            login.checkInConnection(liveConn);
        }
    }

    /**
     * Tests whether the student's program code is one of those that needs only AUCC-1B.
     *
     * @param student the student record
     * @return true if this code only needs AUCC 1B
     */
    private boolean isProgramOnlyAUCC(final RawStudent student, final Collection<? super String> report) {

        final boolean auccOnly;

        final String programCode = student.programCode;

        if (programCode == null || programCode.isBlank()) {
            auccOnly = false;
        } else if (MAJORS_NEEDING_ONLY_AUCC.contains(programCode)) {
            auccOnly = true;
        } else if (MAJORS_NEEDING_MORE.contains(programCode)) {
            auccOnly = false;
        } else if (programCode.endsWith("-GR")
                   || programCode.endsWith("-MS")
                   || programCode.endsWith("-MA")
                   || programCode.endsWith("-MFA")
                   || programCode.endsWith("-CT")
                   || programCode.endsWith("-SI")
                   || programCode.endsWith("-MAS")
                   || programCode.endsWith("-M")
                   || programCode.endsWith("-ME")
                   || programCode.endsWith("-MM")
                   || programCode.endsWith("-MBA")
                   || programCode.endsWith("-DVM")
                   || programCode.endsWith("-MTM")
                   || programCode.endsWith("-MOT")
                   || programCode.endsWith("-MCS")
                   || programCode.endsWith("-MAGR")
                   || programCode.endsWith("-MAPD")
                   || programCode.endsWith("-MPSM")
                   || programCode.endsWith("-MPSP")
                   || programCode.endsWith("-MACC")
                   || programCode.endsWith("-MACP")
                   || programCode.endsWith("-MCIS")
                   || programCode.endsWith("-MCMM")
                   || programCode.endsWith("-MPPA")
                   || programCode.endsWith("-MFIN")
                   || programCode.endsWith("-MIOP")
                   || programCode.endsWith("-MFWC")
                   || programCode.endsWith("-MSM")
                   || programCode.endsWith("-MSW")
                   || programCode.endsWith("-MASW")
                   || programCode.endsWith("-MED")
                   || programCode.endsWith("-DOT")
                   || programCode.endsWith("-MCL")
                   || programCode.endsWith("MCIN")
                   || programCode.endsWith("-PHD")) {
            // Don't force grad students through the placement tool...
            auccOnly = true;
        } else {
            final int ddIndex = programCode.indexOf("-DD-");

            if (ddIndex == -1) {
                final String msg = HtmlBuilder.concat("Unrecognized program code: ", programCode, ", student ",
                        student.stuId, " college is ", student.college, " and department is ", student.dept);
                Log.fine(msg);
                report.add(msg);
                auccOnly = false;
            } else {
                final String newCode = programCode.substring(0, ddIndex) + programCode.substring(ddIndex + 3);

                if (MAJORS_NEEDING_ONLY_AUCC.contains(newCode)) {
                    auccOnly = true;
                } else if (MAJORS_NEEDING_MORE.contains(newCode)) {
                    auccOnly = false;
                } else {
                    final String msg = HtmlBuilder.concat("Unrecognized program code: ", programCode, ", student ",
                            student.stuId, " college is ", student.college, " and department is ", student.dept);
                    Log.fine(msg);
                    report.add(msg);
                    auccOnly = false;
                }
            }
        }

        return auccOnly;
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        DbConnection.registerDrivers();
        final BulkUpdateMPLTestScores job = new BulkUpdateMPLTestScores();

        final String log = job.execute();
        Log.fine(log);
    }
}
