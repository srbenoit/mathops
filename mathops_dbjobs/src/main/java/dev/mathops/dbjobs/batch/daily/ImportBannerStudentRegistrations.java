package dev.mathops.dbjobs.batch.daily;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.enums.EDisciplineActionType;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.logic.PaceTrackLogic;
import dev.mathops.db.old.logic.PrerequisiteLogic;
import dev.mathops.db.old.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.old.rawlogic.RawDisciplineLogic;
import dev.mathops.db.old.rawlogic.RawDupRegistrLogic;
import dev.mathops.db.old.rawlogic.RawFfrTrnsLogic;
import dev.mathops.db.old.rawlogic.RawMpeCreditLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawSttermLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawDiscipline;
import dev.mathops.db.old.rawrecord.RawDupRegistr;
import dev.mathops.db.old.rawrecord.RawFfrTrns;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.schema.AbstractImpl;
import dev.mathops.db.rec.TermRec;
import dev.mathops.dbjobs.report.SpecialOpenReport;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * A class that performs an import of student registration data from Banner.
 */
public final class ImportBannerStudentRegistrations {

    /** A commonly used string. */
    private static final String STUDENT = "  Student ";

    /** A commonly used string. */
    private static final String DELETED = "  Deleted ";

    /** A commonly used string. */
    private static final String FOR = " for ";

    /** A commonly used string. */
    private static final String SECTION = " section ";

    /** Debug flag - set to 'true' to print changes rather than performing them. */
    private static final boolean DEBUG = false;

    /** The database profile through which to access the database. */
    private final Profile profile;

    /**
     * Constructs a new {@code ImportBannerStudentRegistrations}.
     */
    public ImportBannerStudentRegistrations() {

        final DatabaseConfig config = DatabaseConfig.getDefault();
        this.profile = config.getCodeProfile(Contexts.BATCH_PATH);
    }

    /**
     * Executes the job.
     *
     * @return a report
     */
    public String execute() {

        final HtmlBuilder htm = new HtmlBuilder(1000);

        final Cache cache = new Cache(this.profile);

        try {
            final TermRec active = cache.getSystemData().getActiveTerm();

            final Collection<String> report = new ArrayList<>(10);

            executeInTerm(cache, active, report);

            htm.addln("<pre>");
            for (final String rep : report) {
                htm.addln(rep);
            }
            htm.addln("</pre>");
        } catch (final SQLException ex) {
            Log.warning(ex);
        }

        return htm.toString();
    }

    /**
     * Executes the query against Banner term table matching the active term and loads data into the primary schema.
     *
     * @param cache  the data cache
     * @param active the active term
     * @param report a list of strings to which to add report output lines
     */
    private void executeInTerm(final Cache cache, final TermRec active, final Collection<? super String> report) {

        report.add(CoreConstants.EMPTY);
        report.add("Retrieving data from Banner:");

        final Login liveLogin = this.profile.getLogin(ESchema.LIVE);
        final DbConnection bannerConn = liveLogin.checkOutConnection();

        try {
            final List<RawStcourse> regs = new ArrayList<>(3500);

            if (active.term.name == ETermName.SPRING) {
                report.add("  Querying under the SPRING term");
                queryBanner(cache, active, bannerConn, regs, "SPR");
            } else if (active.term.name == ETermName.SUMMER) {
                report.add("  Querying under the SUMMER term");
                queryBanner(cache, active, bannerConn, regs, "SMR");
            } else if (active.term.name == ETermName.FALL) {
                report.add("  Querying under the FALL term");
                queryBanner(cache, active, bannerConn, regs, "FAL");
            } else {
                report.add("  Active term has invalid term name: " + active.term.name);
            }

            report.add("  Retrieved " + regs.size() + " registrations.");

            processList(cache, active, regs, report);

            report.add(CoreConstants.EMPTY);
            report.add("Job completed");

        } catch (final SQLException ex) {
            Log.warning(ex);
            report.add("Unable to perform query: " + ex.getMessage());
        } finally {
            liveLogin.checkInConnection(bannerConn);
        }
    }

    /**
     * Queries registration records from Banner.
     *
     * @param cache      the data cache
     * @param active     the active term
     * @param bannerConn the database connection to the Live (Banner) database
     * @param regs       a list to which to add student course record
     * @param suffix     the suffix to append to the names of the term-dependent tables
     * @throws SQLException if there is an error performing the query
     */
    private static void queryBanner(final Cache cache, final TermRec active, final DbConnection bannerConn,
                                    final Collection<? super RawStcourse> regs, final String suffix)
            throws SQLException {

        final int curYear = active.term.year.intValue();
        final String term;

        if ("SPR".equals(suffix)) {
            term = curYear + "10";
        } else if ("SMR".equals(suffix)) {
            term = curYear + "60";
        } else {
            term = curYear + "90";
        }

        final List<RawCsection> csections = cache.getSystemData().getCourseSections(active.term);

        try (final Statement stmt = bannerConn.createStatement()) {

            final String regSql =
                    "SELECT * FROM CSUS_MATH_REGISTRATION_" + suffix
                    + " WHERE TERM = '" + term + "' "
                    + "  AND SUBJECT_CODE='MATH'"
                    + "  AND (COURSE_NUMBER='117' OR "
                    + "       COURSE_NUMBER='118' OR "
                    + "       COURSE_NUMBER='124' OR "
                    + "       COURSE_NUMBER='125' OR "
                    + "       COURSE_NUMBER='126')";

            final LocalDate today = LocalDate.now();

            try (final ResultSet rs = stmt.executeQuery(regSql)) {
                while (rs.next()) {

                    final String csuId = rs.getString("CSU_ID");

                    final RawStudent stu = RawStudentLogic.query(cache, csuId, false);
                    if (stu == null) {
                        Log.info("Student record missing for ", csuId, " querying Banner.");
                        if (!DEBUG) {
                            RawStudentLogic.query(cache, csuId, true);
                        }
                    }

                    final String courseNum = AbstractImpl.getString(rs, "COURSE_NUMBER");
                    final String sect = rs.getString("SECTION");

                    // Based on the course number (like "MATH 125") and section, determine whether
                    // we should an old format like "M 125" or a new format like "MATH 125". This
                    // is based on which of the two has a matching CSECTION record.

                    String actualCourseId = null;
                    for (final RawCsection test : csections) {
                        if (test.sect.equals(sect)) {
                            final String shortId = "M " + courseNum;
                            final String longId = "MATH " + courseNum;

                            if (test.course.equals(shortId) || test.course.equals(longId)) {
                                actualCourseId = test.course;
                            }
                        }
                    }

                    if (actualCourseId == null) {
                        Log.warning("No course/section record for MATH ", courseNum, " (", sect, ")");
                    } else {

                        final String credType = AbstractImpl.getString(rs, "COURSE_CREDIT_TYPE");
                        String instr = null;
                        if ("M".equals(credType)) {
                            instr = "RI";
                        } else if ("MC".equals(credType)) {
                            instr = "CE";
                        }

                        final RawStcourse regRec = new RawStcourse(active.term,
                                csuId, // stu_id
                                actualCourseId, // course
                                sect, // sect
                                null, // paceOrder
                                null, // openStatus
                                rs.getString("GRADING_OPTION"), // gradingOption
                                "N", // completed
                                null, // score
                                null, // courseGrade
                                null, // prereqSatis
                                "Y", // initClassRoll
                                "N", // stuProvided
                                "Y", // finalClassRoll
                                null, // examPlaced
                                null, // zeroUnit
                                null, // timeoutFactor
                                null, // forfeitI
                                "N", // i-in-progress
                                null, // i-counted
                                "N", // ctrlTest
                                null, // deferredFDt
                                Integer.valueOf(0), // bypassTimeout
                                instr, // instrnType
                                rs.getString("REGISTRATION_STATUS"), // registrationStatus
                                today, // lastRollDate
                                null, // iTermKey
                                null); // iDeadline

                        regs.add(regRec);

                        if (DEBUG) {
                            // Log.fine("Registration for ", regRec.stuId, " in ", regRec.course, SECTION, regRec.sect);
                            // Log.fine(" Grading: ", regRec.gradingOption);
                            // Log.fine(" Status: ", regRec.registrationStatus);
                            // Log.fine(" Instruction:", regRec.instrnType);
                        }
                    }
                }
            }
        }
    }

    /**
     * Processes a list of registrations.
     *
     * @param cache      the data
     * @param active     the active term
     * @param bannerRegs the collection of registrations from Banner
     * @param report     a list of strings to which to add report output lines
     * @throws SQLException if there was an error accessing the database
     */
    private static void processList(final Cache cache, final TermRec active, final List<RawStcourse> bannerRegs,
                                    final Collection<? super String> report) throws SQLException {

        logGradingOptions(bannerRegs, report);

        deleteHolds(cache, report);

        reconcile(cache, active, bannerRegs, report);

        processFcr(cache, report);

        SpecialOpenReport.generate(cache, report);

        report.add(CoreConstants.EMPTY);
        SetHolds.setHolds(cache);

        // Generate statistics
        final List<RawStcourse> allregs = RawStcourseLogic.queryByTerm(cache, active.term, true, false);

        // Map<sect, Map<grading_option, Map<open_status, Map<completed, count>>>>
        final Map<String, Map<String, Map<String, Map<String, Integer>>>> map = new TreeMap<>();

        for (final RawStcourse test : allregs) {
            if ("Y".equals(test.iInProgress)) {
                continue;
            }

            Map<String, Map<String, Map<String, Integer>>> sectMap = map.get(test.sect);
            if (sectMap == null) {
                sectMap = new TreeMap<>();
                map.put(test.sect == null ? CoreConstants.SPC : test.sect, sectMap);
            }

            Map<String, Map<String, Integer>> gradingMap = sectMap.get(test.gradingOption);
            if (gradingMap == null) {
                gradingMap = new TreeMap<>();
                sectMap.put(test.gradingOption == null ? CoreConstants.SPC : test.gradingOption,
                        gradingMap);
            }

            final String openStr = test.openStatus == null ? CoreConstants.SPC : test.openStatus;

            final Map<String, Integer> openMap = gradingMap.computeIfAbsent(openStr, s -> new TreeMap<>());

            final Integer count = openMap.get(test.completed);
            if (count == null) {
                openMap.put(test.completed, Integer.valueOf(1));
            } else {
                openMap.put(test.completed, Integer.valueOf(count.intValue() + 1));
            }
        }

        report.add(CoreConstants.EMPTY);
        report.add("Registration Statistics:");

        report.add(CoreConstants.EMPTY);
        report.add("  Section:  Grading Option:  Open Status:  Completed:  Count:");

        final HtmlBuilder line = new HtmlBuilder(100);
        for (final Map.Entry<String, Map<String, Map<String, Map<String, Integer>>>> sect : map.entrySet()) {
            report.add("  --------  ---------------  ------------  ----------  ------");

            for (final Map.Entry<String, Map<String, Map<String, Integer>>> grading : sect.getValue().entrySet()) {

                for (final Map.Entry<String, Map<String, Integer>> open : grading.getValue().entrySet()) {

                    for (final Map.Entry<String, Integer> completed : open.getValue().entrySet()) {

                        line.add("  ").add(sect.getKey()).padToLength(12);
                        line.add(grading.getKey()).padToLength(29);
                        line.add(open.getKey()).padToLength(43);
                        line.add(completed.getKey()).padToLength(55);
                        line.add(completed.getValue());

                        report.add(line.toString());
                        line.reset();
                    }
                }
            }
        }
    }

    /**
     * Logs the grading option, section, and instruction types of registration records to the report.
     *
     * @param regs   the collection of registrations from Banner
     * @param report a list of strings to which to add report output lines
     */
    private static void logGradingOptions(final Iterable<RawStcourse> regs, final Collection<? super String> report) {

        // Map from grading option to map from section to map from instruction type to count
        final Map<String, Map<String, Map<String, Integer>>> data = new TreeMap<>();
        for (final RawStcourse reg : regs) {

            final String grading = reg.gradingOption == null ? "null" : reg.gradingOption;

            final Map<String, Map<String, Integer>> inner = data.computeIfAbsent(grading, s -> new TreeMap<>());

            final String sect = reg.sect == null ? "null" : reg.sect;

            final Map<String, Integer> inner2 = inner.computeIfAbsent(sect, s -> new TreeMap<>());

            final String instr = reg.instrnType == null ? "null" : reg.instrnType;

            final Integer count = inner2.get(instr);
            if (count == null) {
                inner2.put(instr, Integer.valueOf(1));
            } else {
                inner2.put(instr, Integer.valueOf(count.intValue() + 1));
            }
        }

        report.add(CoreConstants.EMPTY);
        report.add("  Grading Option:   Section:   Instruction Type:   Count:");
        report.add("  ---------------   --------   -----------------   ------");

        String badOption = null;
        final HtmlBuilder htm = new HtmlBuilder(100);
        for (final Map.Entry<String, Map<String, Map<String, Integer>>> entry1 : data.entrySet()) {

            final String opt = entry1.getKey();
            if (!"I".equals(opt) && !"V".equals(opt)) {
                badOption = opt;
            }

            for (final Map.Entry<String, Map<String, Integer>> entry2 : entry1.getValue().entrySet()) {
                for (final Map.Entry<String, Integer> entry3 : entry2.getValue().entrySet()) {

                    htm.add("  ", opt).padToLength(20);
                    htm.add(entry2.getKey()).padToLength(31);
                    htm.add(entry3.getKey()).padToLength(51);
                    htm.add(entry3.getValue());

                    report.add(htm.toString());
                    htm.reset();
                }
            }
        }

        if (badOption != null) {
            report.add("  NOTICE:  UNEXPECTED GRADING OPTION IS PRESENT (" + badOption + ")");
        }
    }

    /**
     * Deletes holds that will be re-calculated during reconciliation.
     *
     * @param cache  the data cache
     * @param report a list of strings to which to add report output lines
     * @throws SQLException if there was an error accessing the database
     */
    private static void deleteHolds(final Cache cache, final Collection<? super String> report) throws SQLException {

        report.add(CoreConstants.EMPTY);
        report.add("Clearing holds that will be recomputed during reconciliation:");

        int total = 0;

        if (!DEBUG) {
            int count;

            count = RawAdminHoldLogic.deleteAllByHoldId(cache, "07");
            report.add(DELETED + count + " holds with ID '07'");

            count = RawAdminHoldLogic.deleteAllByHoldId(cache, "23");
            report.add(DELETED + count + " holds with ID '23'");

            count = RawAdminHoldLogic.deleteAllByHoldId(cache, "25");
            report.add(DELETED + count + " holds with ID '25'");

            count = RawAdminHoldLogic.deleteAllByHoldId(cache, "26");
            report.add(DELETED + count + " holds with ID '26'");

            count = RawAdminHoldLogic.deleteAllByHoldId(cache, "27");
            report.add(DELETED + count + " holds with ID '27'");

            count = RawAdminHoldLogic.deleteAllByHoldId(cache, "29");
            report.add(DELETED + count + " holds with ID '29'");

            total += count;
        }

        if (total == 0) {
            report.add("  (there were no applicable holds to delete)");
        }
    }

    /**
     * Performs reconciliation between data loaded from Banner and existing database records.
     *
     * @param cache      the data cache
     * @param active     the active term
     * @param bannerRegs the registrations queried from Banner
     * @param report     a list of strings to which to add report output lines
     * @throws SQLException if there was an error accessing the database
     */
    private static void reconcile(final Cache cache, final TermRec active, final List<RawStcourse> bannerRegs,
                                  final Collection<? super String> report) throws SQLException {

        // Get all non-Dropped/non-Incomplete registrations in our database
        final Collection<RawStcourse> incompletes = new ArrayList<>(10);

        final List<RawStcourse> dbRegs = queryDbRegs(cache, active, incompletes, report);

        reconcilePlacementRows(cache, active, bannerRegs, dbRegs, report);

        fixBadInsructionTypes(cache, active, bannerRegs, report);

        // Sort Banner registration data into lists for each student
        final Map<String, List<RawStcourse>> bannerRegMap = new HashMap<>(bannerRegs.size());
        for (final RawStcourse test : bannerRegs) {
            final String stuId = test.stuId;
            final List<RawStcourse> list = bannerRegMap.computeIfAbsent(stuId, s -> new ArrayList<>(5));
            list.add(test);
        }

        reconcileRegistrations(cache, active, bannerRegMap, dbRegs, incompletes, report);

        // Go through each student and update the "stterm" table with correct pace and pace track
        updateStudentTermRecords(cache, report);
    }

    /**
     * Queries all registration records from the local database.
     *
     * @param cache       the data cache
     * @param active      the active term
     * @param incompletes a list to which to add incompletes
     * @param report      a list of strings to which to add report output lines
     * @return the list of registrations found
     * @throws SQLException if there is an error querying
     */
    private static List<RawStcourse> queryDbRegs(final Cache cache, final TermRec active,
                                                 final Collection<? super RawStcourse> incompletes,
                                                 final Collection<? super String> report) throws SQLException {

        // Get all non-Dropped registrations in our database (includes OT registrations)
        final List<RawStcourse> dbRegs = RawStcourseLogic.queryByTerm(cache, active.term, true, false);

        // Move local-database records with "I In Progress = 'Y'" or an "I Deadline Dt" into a
        // separate list
        int numInc = 0;
        final Iterator<RawStcourse> iter = dbRegs.iterator();
        while (iter.hasNext()) {
            final RawStcourse next = iter.next();
            if ("Y".equals(next.iInProgress) || next.iDeadlineDt != null) {
                incompletes.add(next);
                iter.remove();
                ++numInc;
            }
        }

        if (numInc > 0) {
            report.add("  Disregarding " + numInc + " Incomplete records in database.");
        }

        return dbRegs;
    }

    /**
     * Reconciles placement (section 550) rows with data from Banner. On exit, there should be no 550/OT rows in either
     * list, and the database should be correct.
     *
     * @param cache   the data cache
     * @param active  the active term
     * @param odsRegs the registrations queried from Banner
     * @param dbRegs  the registrations currently in our database
     * @param report  a list of strings to which to add report output lines
     * @throws SQLException if there is an error accessing the database
     */
    private static void reconcilePlacementRows(final Cache cache, final TermRec active,
                                               final Iterable<RawStcourse> odsRegs,
                                               final Iterable<RawStcourse> dbRegs,
                                               final Collection<? super String> report)
            throws SQLException {

        int numAdded = 0;
        int numRemoved = 0;

        report.add(CoreConstants.EMPTY);
        report.add("Updating placement credit records:");

        final LocalDate today = LocalDate.now();

        final Iterator<RawStcourse> odsIter = odsRegs.iterator();
        while (odsIter.hasNext()) {
            final RawStcourse nextOds = odsIter.next();

            if ("550".equals(nextOds.sect)) {

                boolean searching = true;
                final Iterator<RawStcourse> dbIter = dbRegs.iterator();
                while (dbIter.hasNext()) {
                    final RawStcourse nextDb = dbIter.next();

                    if (nextDb.stuId.equals(nextOds.stuId) && nextDb.course.equals(nextOds.course)
                        && nextDb.sect.equals(nextOds.sect)) {

                        dbIter.remove();
                        searching = false;
                        break;
                    }
                }

                if (searching) {
                    final String courseId = nextOds.course;
                    final String studentId = nextOds.stuId;

                    final RawStcourse newRec = new RawStcourse(active.term, // Term
                            studentId, // stuId
                            courseId, // course
                            nextOds.sect, // sect
                            null, // paceOrder
                            null, // openStatus
                            nextOds.gradingOption, // gradingOption
                            "N", // completed
                            null, // score
                            null, // courseGrade
                            null, // prereqSatis
                            "N", // initClassRoll
                            "N", // stuProvided
                            "Y", // finalClassRoll
                            "M", // examPlaced
                            null, // zeroUnit
                            null, // timelimitFactor
                            null, // forfeitI
                            "N", // iInProgress
                            null, // iCounted
                            "N", // ctrlTest
                            null, // deferredFDt
                            Integer.valueOf(0), // bypassTimeout
                            "OT", // instrnType
                            nextOds.registrationStatus, // registrationStatus
                            today, // lastClasRollDt
                            null, // iTermKey
                            null); // iDeadlineDate

                    final List<RawMpeCredit> mpeCredit = RawMpeCreditLogic.queryByStudent(cache, studentId);

                    // Make sure there is documentation in our system that supports the existence
                    // of a placement credit registration

                    String examPlaced = null;
                    LocalDate dateRefused = null;

                    if (RawRecordConstants.M117.equals(courseId)) {
                        for (final RawMpeCredit test : mpeCredit) {
                            if (RawRecordConstants.M117.equals(test.course) || "M 120A".equals(test.course)) {
                                examPlaced = test.examPlaced;
                                dateRefused = test.dtCrRefused;
                            }
                        }
                    } else if (RawRecordConstants.M118.equals(courseId)) {
                        for (final RawMpeCredit test : mpeCredit) {
                            if (RawRecordConstants.M118.equals(test.course) || "M 121".equals(test.course)) {
                                examPlaced = test.examPlaced;
                                dateRefused = test.dtCrRefused;
                            }
                        }
                    } else {
                        for (final RawMpeCredit test : mpeCredit) {
                            if (courseId.equals(test.course)) {
                                examPlaced = test.examPlaced;
                                dateRefused = test.dtCrRefused;
                            }
                        }
                    }

                    if ("C".equals(examPlaced)) {
                        if (dateRefused != null) {
                            report.add("  MPE credit refused for " + courseId + "by: " + studentId);
                            final List<RawAdminHold> stuHolds = RawAdminHoldLogic.queryByStudent(cache, studentId);
                            ensureHoldExists(cache, studentId, "26", stuHolds, report);
                        }
                    } else {
                        final List<RawFfrTrns> transfer = RawFfrTrnsLogic.queryByStudent(cache, studentId);

                        for (final RawFfrTrns test : transfer) {
                            if (test.course.equals(courseId)) {
                                // TODO:
                            }
                        }

                        report.add("  NO record in MPE_CREDIT or FFR_TRNS for " + courseId + FOR + studentId);
                        final List<RawAdminHold> stuHolds = RawAdminHoldLogic.queryByStudent(cache, studentId);
                        ensureHoldExists(cache, studentId, "27", stuHolds, report);
                    }

                    report.add("  Placement credit record to be added to STCOURSE for " + newRec.stuId + " / "
                               + newRec.course + SECTION + newRec.sect + " (" + newRec.instrnType + ")");
                    if (!DEBUG) {
                        RawStcourseLogic.insert(cache, newRec);
                    }
                    ++numAdded;
                }

                odsIter.remove();
            }
        }

        // At this point, there should be no more 550 rows in the "odsRegs" list, and any that
        // still exist in the "dbRegs" list are bad and should go away.

        final Iterator<RawStcourse> dbIter = dbRegs.iterator();
        while (dbIter.hasNext()) {
            final RawStcourse nextDb = dbIter.next();

            if ("550".equals(nextDb.sect)) {

                // Need to delete!
                try {
                    report.add("  Placement credit record in STCOURSE for " + nextDb.stuId + " / " + nextDb.course
                               + " is not present in Banner data and will be deleted.");
                    if (!DEBUG && !RawStcourseLogic.delete(cache, nextDb)) {
                        report.add("  *** ERROR deleting STCOURSE placement credit record.");
                    }
                } catch (final SQLException ex) {
                    Log.warning(ex);
                    report.add("  *** ERROR deleting STCOURSE placement credit record: " + ex.getMessage());
                }

                dbIter.remove();
                ++numRemoved;
            }
        }

        if (numAdded == 0) {
            report.add("  (no placement credit records needed to be created)");
        } else {
            report.add("  Created " + numAdded + " placement credit records in STCOURSE.");
        }

        if (numRemoved == 0) {
            report.add("  (no placement credit records needed to be removed)");
        } else {
            report.add(DELETED + numRemoved + " placement credit records from STCOURSE.");
        }
    }

    /**
     * Scans the registration records from Banner. For any that have a missing or unrecognized instruction type, try to
     * look up the instruction type from the "CSECTION" table and populate. If a record has no corresponding row in the
     * CSECTION table, remove it from consideration.
     * <p>
     * NOTE: by the time this method is called, there should be no 550/OT records in {@code odsRegs}.
     *
     * @param cache   the data cache
     * @param active  the active term
     * @param odsRegs the registrations queried from Banner
     * @param report  a list of strings to which to add report output lines
     * @throws SQLException if there was an error accessing the database
     */
    private static void fixBadInsructionTypes(final Cache cache, final TermRec active,
                                              final List<RawStcourse> odsRegs,
                                              final Collection<? super String> report) throws SQLException {

        report.add(CoreConstants.EMPTY);
        report.add("Validating instruction types in Banner registration records:");

        int numBad = 0;

        final int size = odsRegs.size();
        for (int i = 0; i < size; ++i) {
            final RawStcourse test = odsRegs.get(i);

            if (test.instrnType == null) {
                final String instrnType = cache.getSystemData().getInstructionType(test.course, test.sect, active.term);

                if (instrnType != null) {
                    report.add("  Updating instruction type to " + instrnType + FOR + test.course + SECTION
                               + test.sect);
                    test.instrnType = instrnType;
                    odsRegs.set(i, test);
                }

                ++numBad;
            }
        }

        if (numBad == 0) {
            report.add("  (All " + odsRegs.size() + " Banner records had valid instruction type)");
        }
    }

    /**
     * Reconciles placement (section 550) rows with data from Banner. On exit, there should be no 550/OT rows in either
     * list, and the database should be correct.
     *
     * @param cache        the data cache
     * @param active       the active term
     * @param bannerRegMap map from student ID to list of Banner registrations for that student
     * @param dbRegs       the registrations currently in our database
     * @param incompletes  incompletes from the current database
     * @param report       a list of strings to which to add report output lines
     * @throws SQLException if there is an error querying
     */
    private static void reconcileRegistrations(final Cache cache, final TermRec active,
                                               final Map<String, List<RawStcourse>> bannerRegMap,
                                               final Collection<RawStcourse> dbRegs,
                                               final Iterable<RawStcourse> incompletes,
                                               final Collection<? super String> report) throws SQLException {

        report.add(CoreConstants.EMPTY);
        report.add("Reconciling registrations for " + bannerRegMap.size() + " distinct students.");

        // Clear the list of duplicate registrations
        if (!DEBUG) {
            RawDupRegistrLogic.deleteAll(cache);
        }

        final Collection<RawStcourse> stuDbRegs = new ArrayList<>(5);

        for (final Map.Entry<String, List<RawStcourse>> entry : bannerRegMap.entrySet()) {

            final String stuId = entry.getKey();

            // Find all existing records for this student (and remove them from the "dbRegs" list as we go)
            stuDbRegs.clear();
            final Iterator<RawStcourse> iter = dbRegs.iterator();
            while (iter.hasNext()) {
                final RawStcourse next = iter.next();
                if (stuId.equals(next.stuId)) {
                    stuDbRegs.add(next);
                    iter.remove();
                }
            }

            // Query all holds for the student
            final List<RawAdminHold> stuHolds = RawAdminHoldLogic.queryByStudent(cache, stuId);

            // For each Banner registration, match a DB registration - if DB has none, add one;
            // otherwise update DB registration if needed
            for (final RawStcourse bannerReg : entry.getValue()) {
                matchCourse(cache, active, bannerReg, stuDbRegs, stuHolds, incompletes, report);
            }

            for (final RawStcourse dropped : stuDbRegs) {
                Log.info("  Marking " + dropped.course, " as dropped for ", stuId);

                if (!DEBUG && RawStcourseLogic.updateOpenStatusAndFinalClassRoll(cache, dropped.stuId, dropped.course,
                        dropped.sect, dropped.termKey, "D", "N", dropped.lastClassRollDt)) {

                    dropped.openStatus = "D";
                    dropped.finalClassRoll = "N";
                }
            }
        }

        // If there remain DB registrations that had no Banner registration, mark them as dropped and not on final
        // class roll.
        final Collection<String> studentIds = new HashSet<>(dbRegs.size());
        for (final RawStcourse obsolete : dbRegs) {
            report.add("  Marking as dropped registration for " + obsolete.stuId + " in " + obsolete.course + SECTION
                       + obsolete.sect);

            if (!DEBUG && RawStcourseLogic.updateOpenStatusAndFinalClassRoll(cache, obsolete.stuId,
                    obsolete.course, obsolete.sect, obsolete.termKey, "D", "N", obsolete.lastClassRollDt)) {

                obsolete.openStatus = "D";
                obsolete.finalClassRoll = "N";
            }

            studentIds.add(obsolete.stuId);
        }

        for (final String studentId : studentIds) {
            final List<RawStcourse> regs = RawStcourseLogic.getActiveForStudent(cache, studentId, active.term);
            PaceTrackLogic.updateStudentTerm(cache, studentId, regs);
        }
    }

    /**
     * Processes a single course registration from Banner. If a record of the registration exists in the database, it is
     * tested and possibly updated; otherwise, a record will be created.
     *
     * <p>
     * The matching record (if found) will be removed from {@code stuDbRegs}.
     *
     * @param cache       the data cache
     * @param active      the active term
     * @param bannerReg   the Banner course registration record
     * @param stuDbRegs   the list of all course registrations from our database that have not already been matched
     * @param stuHolds    the list of holds on the student account
     * @param incompletes incompletes from the current database
     * @param report      a list of strings to which to add report output lines
     * @throws SQLException if there is an error querying
     */
    private static void matchCourse(final Cache cache, final TermRec active,
                                    final RawStcourse bannerReg, final Collection<RawStcourse> stuDbRegs,
                                    final Iterable<RawAdminHold> stuHolds, final Iterable<RawStcourse> incompletes,
                                    final Collection<? super String> report) throws SQLException {

        final String stuId = bannerReg.stuId;

        final List<RawDiscipline> allDiscipline = RawDisciplineLogic.queryByStudent(cache, stuId);

        // See if the student is banned from re-registering for the class
        boolean addHold5 = false;
        boolean addHold4 = false;
        for (final RawDiscipline test : allDiscipline) {
            if (bannerReg.course.equals(test.course)) {
                if (EDisciplineActionType.DEFERRED_F_GRADE.code.equals(test.actionType)) {
                    addHold5 = true;
                } else if (EDisciplineActionType.CANT_REREGISTER.code.equals(test.actionType)) {
                    addHold4 = true;
                }
            }
        }

        if (addHold5) {
            report.add("  Deferred F course registration - research " + bannerReg.course + FOR + stuId);
            ensureHoldExists(cache, stuId, "05", stuHolds, report);
        }

        if (addHold4) {
            report.add("  Can never re-register in " + bannerReg.course + FOR + stuId);
            ensureHoldExists(cache, stuId, "04", stuHolds, report);
        }

        // See if there is an existing record
        RawStcourse existing = null;
        for (final RawStcourse reg : stuDbRegs) {
            if (reg.course.equals(bannerReg.course)) {
                existing = reg;
                break;
            }
        }

        if (existing == null) {
            // Need to add the registration
            addRegistration(cache, active, bannerReg, report);
        } else {
            stuDbRegs.remove(existing);
        }

        // See if there is also an Incomplete for this course in this term
        for (final RawStcourse test : incompletes) {

            if (test.stuId.equals(bannerReg.stuId) && test.course.equals(bannerReg.course)) {

                final RawDupRegistr dup = new RawDupRegistr(bannerReg);
                if (!DEBUG) {
                    RawDupRegistrLogic.insert(cache, dup);
                }

                report.add(
                        "  *** DUPLICATE loaded into DUP_REGISTR " + bannerReg.course + SECTION + bannerReg.sect + FOR
                        + bannerReg.stuId);
                break;
            }
        }

        if (existing != null) {
            final RawCsection odsCsect = cache.getSystemData().getCourseSection(bannerReg.course, bannerReg.sect,
                    active.term);

            if (odsCsect == null) {
                report.add(
                        "  *** ERROR: STCOURSE section " + bannerReg.sect + " not in CSECTION for " + bannerReg.course
                        + " ID: " + bannerReg.stuId);
            } else if (bannerReg.sect.equals(existing.sect)) {

                if (!DEBUG) {
                    if (bannerReg.gradingOption != null && !bannerReg.gradingOption.equals(existing.gradingOption)) {

                        report.add("  Updating grading option from " + existing.gradingOption + " to "
                                   + bannerReg.gradingOption + FOR + bannerReg.course + SECTION + bannerReg.sect + ": "
                                   + bannerReg.stuId);

                        if (RawStcourseLogic.updateGradingOption(cache, existing.stuId, existing.course, existing.sect,
                                existing.termKey, bannerReg.gradingOption)) {

                            existing.gradingOption = bannerReg.gradingOption;
                        }
                    }

                    if (odsCsect.instrnType != null && !odsCsect.instrnType.equals(existing.instrnType)) {

                        report.add("  Updating instruction type from " + existing.instrnType + " to "
                                   + odsCsect.instrnType + FOR + bannerReg.course + SECTION + bannerReg.sect + ": "
                                   + bannerReg.stuId);

                        if (RawStcourseLogic.updateInstructionType(cache, existing.stuId, existing.course,
                                existing.sect, existing.termKey, odsCsect.instrnType)) {

                            existing.instrnType = odsCsect.instrnType;
                        }
                    }

                    final LocalDate now = LocalDate.now();

                    if ((!"Y".equals(existing.finalClassRoll) || !now.equals(existing.lastClassRollDt))
                        && RawStcourseLogic.updateOpenStatusAndFinalClassRoll(cache, existing.stuId,
                            existing.course, existing.sect, existing.termKey, existing.openStatus, "Y", now)) {

                        existing.finalClassRoll = "Y";
                        existing.lastClassRollDt = now;
                    }
                }

            } else {
                // Registration is changing sections

                report.add(
                        "SECTION CHANGE for ID: " + bannerReg.stuId + " in " + bannerReg.course + " from " + existing.sect
                        + " to " + bannerReg.sect);

                // Mark old record as "dropped"

                if (!DEBUG && RawStcourseLogic.updateOpenStatusAndFinalClassRoll(cache, existing.stuId, existing.course,
                        existing.sect, existing.termKey, "D", "N", existing.lastClassRollDt)) {

                    // TODO:
                }

                // Create new record (update existing record to new section and insert)

                existing.sect = bannerReg.sect;
                existing.openStatus = null;
                existing.finalClassRoll = "Y";
                existing.lastClassRollDt = LocalDate.now();

                if (!DEBUG) {
                    RawStcourseLogic.insert(cache, existing);

                    final List<RawStcourse> regs = RawStcourseLogic.getActiveForStudent(cache, bannerReg.stuId,
                            active.term);
                    PaceTrackLogic.updateStudentTerm(cache, bannerReg.stuId, regs);
                }
            }
        }
    }

    /**
     * Processes a single course registration from Banner. If a record of the registration exists in the database, it is
     * tested and possibly updated; otherwise, a record will be created.
     *
     * <p>
     * The matching record (if found) will be removed from {@code stuDbRegs}.
     *
     * @param cache     the data cache
     * @param active    the active term
     * @param bannerReg the Banner course registration record
     * @param report    a list of strings to which to add report output lines
     * @throws SQLException if there is an error accessing the database
     */
    private static void addRegistration(final Cache cache, final TermRec active, final RawStcourse bannerReg,
                                        final Collection<? super String> report) throws SQLException {

        final String instrnType = cache.getSystemData().getInstructionType(bannerReg.course, bannerReg.sect,
                active.term);

        String prereq = bannerReg.prereqSatis;
        String placedByExam = null;

        if ("888".equals(bannerReg.sect)) {
            placedByExam = "A";
        } else {
            final PrerequisiteLogic logic = new PrerequisiteLogic(cache, bannerReg.stuId);
            if (logic.hasSatisfiedPrerequisitesFor(bannerReg.course)) {
                prereq = "Y";
            }
        }

        final RawStcourse toInsert = new RawStcourse(active.term,
                bannerReg.stuId,
                bannerReg.course,
                bannerReg.sect,
                bannerReg.paceOrder,
                bannerReg.openStatus,
                bannerReg.gradingOption,
                "N", // completed
                null, // score
                null, // courseGrade
                prereq, // prereqSatis
                "Y", // initClassRoll
                "N", // stuProvided
                "Y", // finalClassRoll
                placedByExam, // examPlaced
                bannerReg.zeroUnit, // zeroUnit
                null, // timeoutFactor
                null, // forfeitI
                "N", // iInProgress
                null, // iCounted
                "N", // ctrlTest
                null, // deferredDDt
                Integer.valueOf(0), // bypassTimeout
                instrnType, // instrnType
                null, // registrationStatus
                LocalDate.now(), // lastClassRollDt
                null, // iTermKey
                null); // iDeadlineDt

        if (!DEBUG) {
            RawStcourseLogic.insert(cache, toInsert);
        }

        report.add("  STCOURSE row created for ID: " + toInsert.stuId + CoreConstants.SPC + toInsert.course
                   + CoreConstants.SPC + toInsert.sect + CoreConstants.SPC + toInsert.instrnType);

        final List<RawStcourse> regs = RawStcourseLogic.getActiveForStudent(cache, bannerReg.stuId, active.term);

        PaceTrackLogic.updateStudentTerm(cache, bannerReg.stuId, regs);
    }

    /**
     * Verifies that an administrative hold exists on a student's record. If it exists, its hold date is updated. If
     * not, a new hold record is created.
     *
     * <p>
     * If we create a new hold with severity "F", we update the student record if needed to ensure the hold severity is
     * reflected there as well.
     *
     * @param cache         the data cache
     * @param studentId     the student ID
     * @param holdId        the hold ID
     * @param existingHolds the list of existing holds on the student account
     * @param report        a list of strings to which to add report output lines
     * @throws SQLException if there is an error accessing the database
     */
    private static void ensureHoldExists(final Cache cache, final String studentId, final String holdId,
                                         final Iterable<RawAdminHold> existingHolds,
                                         final Collection<? super String> report) throws SQLException {

        RawAdminHold found = null;
        for (final RawAdminHold test : existingHolds) {
            if (test.holdId.equals(holdId)) {
                found = test;
                break;
            }
        }

        if (found == null) {
            // Add new hold
            found = new RawAdminHold(studentId, holdId, "F", Integer.valueOf(0), LocalDate.now());

            report.add("  Adding hold " + holdId + " to student " + studentId);

            if (!DEBUG) {
                RawAdminHoldLogic.insert(cache, found);

                final RawStudent stuRec = RawStudentLogic.query(cache, studentId, false);

                if (stuRec != null && !"F".equals(stuRec.sevAdminHold)) {
                    RawStudentLogic.updateHoldSeverity(cache, stuRec.stuId, "F");
                }
            }
        } else {
            // Update date hold was applied
            report.add("  Re-applying  hold " + holdId + " for student " + studentId);

            if (!DEBUG) {
                found = new RawAdminHold(studentId, holdId, "F", Integer.valueOf(0), LocalDate.now());
                RawAdminHoldLogic.insert(cache, found);
            }
        }
    }

    /**
     * Updates all student term records in case pace or pace track changes.
     *
     * @param cache  the data cache
     * @param report the list to which to add lines of the report
     * @throws SQLException if there is an error accessing the database
     */
    private static void updateStudentTermRecords(final Cache cache,
                                                 final Collection<? super String> report) throws SQLException {

        final TermRec active = cache.getSystemData().getActiveTerm();

        // NOTE: we ignore "OT" and dropped since they can't contribute to pace/track
        final List<RawStcourse> regs = RawStcourseLogic.queryByTerm(cache, active.term, false, false);

        // Collect all registrations into a list per student
        final Map<String, List<RawStcourse>> studentRegs = new HashMap<>(2500);
        for (final RawStcourse reg : regs) {
            final List<RawStcourse> list = studentRegs.computeIfAbsent(reg.stuId, s -> new ArrayList<>(6));
            list.add(reg);
        }

        for (final Map.Entry<String, List<RawStcourse>> entry : studentRegs.entrySet()) {

            final String stuId = entry.getKey();
            final List<RawStcourse> stuRegs = entry.getValue();

            final int pace = PaceTrackLogic.determinePace(stuRegs);
            final String track = PaceTrackLogic.determinePaceTrack(stuRegs, pace);
            final String first = PaceTrackLogic.determineFirstCourse(stuRegs);

            final RawStterm existing = RawSttermLogic.query(cache, active.term, stuId);

            if (first != null) {
                if (existing == null) {
                    final RawStterm newRec = new RawStterm(active.term, stuId, Integer.valueOf(pace), track, first,
                            null, null, null);
                    report.add(STUDENT + stuId + " did not have an STTERM record - adding.");
                    if (!DEBUG) {
                        RawSttermLogic.insert(cache, newRec);
                    }
                } else {
                    boolean diff = false;

                    if (pace != existing.pace.intValue()) {
                        report.add(STUDENT + stuId + " had incorrect pace (was " + existing.pace + ", changing to "
                                   + pace + ").");
                        diff = true;
                    }
                    if (!track.equals(existing.paceTrack)) {
                        report.add(STUDENT + stuId + " had incorrect pace track (was " + existing.paceTrack
                                   + ", changing to " + track + ").");
                        diff = true;
                    }
                    if (!Objects.equals(first, existing.firstCourse)) {
                        report.add(STUDENT + stuId + " had incorrect first course (was " + existing.firstCourse
                                   + ", changing to " + first + ").");
                        diff = true;
                    }

                    if (diff && !DEBUG) {
                        RawSttermLogic.updatePaceTrackFirstCourse(cache, stuId, active.term, pace, track, first);
                    }
                }
            }
        }
    }

    /**
     * Generate report entries showing statistics on registrations, duplicates.
     *
     * @param cache  the data cache
     * @param report a list of strings to which to add report output lines
     * @throws SQLException if there is an error accessing the database
     */
    private static void processFcr(final Cache cache, final Collection<? super String> report) throws SQLException {

        report.add(CoreConstants.EMPTY);
        report.add("Statistics:");

        final List<RawStcourse> all = RawStcourseLogic.queryActiveForActiveTerm(cache);

        int riCount = 0;
        int ceCount = 0;
        int otCount = 0;
        int incCount = 0;

        for (final RawStcourse test : all) {

            if ("Y".equals(test.iInProgress)) {
                ++incCount;
            } else if ("Y".equals(test.finalClassRoll)) {

                final String type = test.instrnType;

                if ("RI".equals(type)) {
                    ++riCount;
                } else if ("CE".equals(type)) {
                    ++ceCount;
                } else if ("OT".equals(type)) {
                    ++otCount;
                }
            }
        }

        report.add("  Total RI     : " + riCount);
        report.add("  Total CE     : " + ceCount);
        report.add("  Total OT     : " + otCount);
        report.add("  Total INC    : " + incCount);
        report.add("  Total Overall: " + (riCount + ceCount + otCount + incCount));
        report.add(CoreConstants.EMPTY);

        final List<RawDupRegistr> allDups = RawDupRegistrLogic.queryAll(cache);

        report.add("  Duplicate total: " + allDups.size());

        int riDupCount = 0;
        int ceDupCount = 0;
        int otDupCount = 0;
        for (final RawDupRegistr test : allDups) {
            final String type = test.instrnType;

            if ("RI".equals(type)) {
                ++riDupCount;
            } else if ("CE".equals(type)) {
                ++ceDupCount;
            } else if ("OT".equals(type)) {
                ++otDupCount;
            }
        }

        report.add("  RI Duplicates  : " + riDupCount);
        report.add("  CE Duplicates  : " + ceDupCount);
        report.add("  OT Duplicates  : " + otDupCount);
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        DbConnection.registerDrivers();
        final ImportBannerStudentRegistrations job = new ImportBannerStudentRegistrations();

        Log.info(job.execute());
    }
}
