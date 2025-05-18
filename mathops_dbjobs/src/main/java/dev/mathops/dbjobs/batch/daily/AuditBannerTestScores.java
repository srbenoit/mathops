package dev.mathops.dbjobs.batch.daily;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawlogic.RawMpscorequeueLogic;
import dev.mathops.db.old.rawlogic.RawStchallengeLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawStmpeLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawMpscorequeue;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStchallenge;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStmpe;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Identifies all students who have submitted any sort of exam since June 17th (when the automatic upload process
 * began).
 *
 * <p>
 * This also checks the "preferred first name" from Banner and updates in local data if different.
 */
public final class AuditBannerTestScores {

    /** Student ID to diagnose, null to skip diagnosis. */
    private static final String DIAGNOSE_STU = null;

    /** Date before which scores have already been audited. */
    private static final LocalDate START_DATE = LocalDate.of(2025, Month.MAY, 18);

    /** A commonly used string. */
    private static final String ERROR = "ERROR: ";

    /** The database profile through which to access the database. */
    private final Profile profile;

    /**
     * Constructs a new {@code AuditBannerTestScores}.
     */
    public AuditBannerTestScores() {

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
            Log.warning("Unable to create production profile.");
        } else {
            final Cache cache = new Cache(this.profile);

            try {
                execute(cache, report);
            } catch (final SQLException ex) {
                report.add("EXCEPTION: " + ex.getMessage());
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
     * Executes the query against the ODS and loads data into the primary schema.
     *
     * @param cache  the data cache
     * @param report a list of strings to which to add report output lines
     * @throws SQLException if there is an error accessing the database
     */
    private void execute(final Cache cache, final Collection<? super String> report) throws SQLException {

        final LocalDate start = START_DATE;
        final LocalDate today = LocalDate.now();

        report.add(SimpleBuilder.concat("Auditing from ", TemporalUtils.FMT_MDY.format(start), " to ",
                TemporalUtils.FMT_MDY.format(today)));

        int nDays = 1;
        LocalDate counter = start;
        while (counter.isBefore(today)) {
            ++nDays;
            counter = counter.plusDays(1L);
        }

        // Placement attempts

        final Collection<List<RawStmpe>> placementAttempts = new ArrayList<>(10);
        RawStmpeLogic.getHistory(cache, placementAttempts, nDays, today);
        int count = 0;
        for (final List<RawStmpe> list : placementAttempts) {
            count += list.size();
        }
        report.add("Retrieved " + count + " placement attempts over " + nDays + " days");

        // Challenge attempts

        final Collection<List<RawStchallenge>> challengeAttempts = new ArrayList<>(10);
        RawStchallengeLogic.getHistory(cache, challengeAttempts, nDays, today);
        count = 0;
        for (final List<RawStchallenge> list : challengeAttempts) {
            count += list.size();
        }
        report.add("Retrieved " + count + " challenge attempts over " + nDays + " days");

        // Ordinary exam attempts

        final Collection<List<RawStexam>> exams = new ArrayList<>(10);

        try {
            RawStexamLogic.getHistory(cache, exams, nDays, today,
                    RawRecordConstants.M100T, RawRecordConstants.M1170, RawRecordConstants.M1180,
                    RawRecordConstants.M1240, RawRecordConstants.M1250, RawRecordConstants.M1260);

            count = 0;
            for (final List<RawStexam> list : exams) {
                count += list.size();
            }
            report.add("Retrieved " + count + " tutorial exams");
        } catch (final SQLException ex) {
            report.add("Failed to get student exams: " + ex.getMessage());
        }

        // Gather the set of student IDs to check
        final Set<String> studentIds = gatherStudentIds(placementAttempts, challengeAttempts, exams);
        report.add("There are " + studentIds.size() + " students to check.");

        final Collection<RawStmpe> placements = new ArrayList<>(10);
        final Collection<RawStchallenge> challenges = new ArrayList<>(10);
        final Collection<RawStexam> elmUnit3ReviewPassed = new ArrayList<>(10);
        final Collection<RawStexam> elmUnit4ExamPassed = new ArrayList<>(10);
        final Collection<RawStexam> pt117Unit4ExamPassed = new ArrayList<>(10);
        final Collection<RawStexam> pt118Unit4ExamPassed = new ArrayList<>(10);
        final Collection<RawStexam> pt124Unit4ExamPassed = new ArrayList<>(10);
        final Collection<RawStexam> pt125Unit4ExamPassed = new ArrayList<>(10);
        final Collection<RawStexam> pt126Unit4ExamPassed = new ArrayList<>(10);

        for (final String stuId : studentIds) {
            boolean spc = true;

            placements.clear();
            challenges.clear();
            elmUnit3ReviewPassed.clear();
            elmUnit4ExamPassed.clear();
            pt117Unit4ExamPassed.clear();
            pt118Unit4ExamPassed.clear();
            pt124Unit4ExamPassed.clear();
            pt125Unit4ExamPassed.clear();
            pt126Unit4ExamPassed.clear();

            final RawStudent student = RawStudentLogic.query(cache, stuId, false);
            if (student == null) {
                report.add(CoreConstants.SPC);
                report.add("ERROR: Unable to query student " + stuId);
                continue;
            }
            if (student.pidm == null) {
                report.add(CoreConstants.SPC);
                report.add("ERROR: Student " + stuId + " has no internal ID");
                continue;
            }

            // Collect all exams that would have posted test score and gather the set of student IDs

            for (final List<RawStmpe> list : placementAttempts) {
                for (final RawStmpe att : list) {
                    if (stuId.equals(att.stuId)) {
                        placements.add(att);
                    }
                }
            }

            for (final List<RawStchallenge> list : challengeAttempts) {
                for (final RawStchallenge att : list) {
                    if (stuId.equals(att.stuId) && "Y".equals(att.passed)) {
                        challenges.add(att);
                    }
                }
            }

            for (final List<RawStexam> list : exams) {
                for (final RawStexam e : list) {
                    if (stuId.equals(e.stuId)) {
                        if (RawRecordConstants.M100T.equals(e.course)) {
                            if (Integer.valueOf(3).equals(e.unit)) {
                                if ("R".equals(e.examType) && ("Y".equals(e.passed) || "P".equals(e.passed))) {
                                    elmUnit3ReviewPassed.add(e);
                                }
                            } else if (Integer.valueOf(4).equals(e.unit) && "U".equals(e.examType)
                                       && ("Y".equals(e.passed) || "P".equals(e.passed))) {
                                elmUnit4ExamPassed.add(e);
                            }
                        } else if (RawRecordConstants.M1170.equals(e.course)) {
                            if (Integer.valueOf(4).equals(e.unit) && "U".equals(e.examType)
                                && ("Y".equals(e.passed) || "P".equals(e.passed))) {
                                pt117Unit4ExamPassed.add(e);
                            }
                        } else if (RawRecordConstants.M1180.equals(e.course)) {
                            if (Integer.valueOf(4).equals(e.unit) && "U".equals(e.examType)
                                && ("Y".equals(e.passed) || "P".equals(e.passed))) {
                                pt118Unit4ExamPassed.add(e);
                            }
                        } else if (RawRecordConstants.M1240.equals(e.course)) {
                            if (Integer.valueOf(4).equals(e.unit) && "U".equals(e.examType)
                                && ("Y".equals(e.passed) || "P".equals(e.passed))) {
                                pt124Unit4ExamPassed.add(e);
                            }
                        } else if (RawRecordConstants.M1250.equals(e.course)) {
                            if (Integer.valueOf(4).equals(e.unit) && "U".equals(e.examType)
                                && ("Y".equals(e.passed) || "P".equals(e.passed))) {
                                pt125Unit4ExamPassed.add(e);
                            }
                        } else if (RawRecordConstants.M1260.equals(e.course)
                                   && Integer.valueOf(4).equals(e.unit) && "U".equals(e.examType)
                                   && ("Y".equals(e.passed) || "P".equals(e.passed))) {
                            pt126Unit4ExamPassed.add(e);
                        }
                    }
                }
            }

            // Get the list of test scores on record for this student that have occurred since the
            // start date
            List<RawMpscorequeue> sortest;

            boolean needs00Score2 = true;
            boolean needs00Score4 = true;

            final Login liveLogin = this.profile.getLogin(ESchema.LIVE);
            try {
                final DbConnection liveConn = liveLogin.checkOutConnection();
                try {
                    sortest = RawMpscorequeueLogic.querySORTESTByStudent(liveConn, student.pidm);

                    final Iterator<RawMpscorequeue> iter = sortest.iterator();
                    while (iter.hasNext()) {
                        final RawMpscorequeue rec = iter.next();

                        if ("MPL".equals(rec.testCode)) {
                            iter.remove();
                        } else {
                            if ("MC00".equals(rec.testCode)) {
                                if ("2".equals(rec.testScore)) {
                                    needs00Score2 = false;
                                } else if ("4".equals(rec.testScore)) {
                                    needs00Score4 = false;
                                }
                            }

                            if (rec.testDate.toLocalDate().isBefore(start)) {
                                iter.remove();
                            }
                        }
                    }
                } finally {
                    liveLogin.checkInConnection(liveConn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                sortest = new ArrayList<>(0);
            }

            if (DIAGNOSE_STU != null && DIAGNOSE_STU.equals(stuId)) {
                for (final RawMpscorequeue row : sortest) {
                    Log.info(DIAGNOSE_STU, " has SORTEST row for ", row.testCode, "/", row.testScore, " on ",
                            row.testDate);
                }
                for (final RawStmpe row : placements) {
                    Log.info(DIAGNOSE_STU, " has placement on ", row.examDt);
                }
                for (final RawStchallenge row : challenges) {
                    Log.info(DIAGNOSE_STU, " has challenge on ", row.examDt);
                }
                for (final RawStexam row : elmUnit3ReviewPassed) {
                    Log.info(DIAGNOSE_STU, " has ELM3 on ", row.examDt);
                }
                for (final RawStexam row : elmUnit4ExamPassed) {
                    Log.info(DIAGNOSE_STU, " has ELM4 on ", row.examDt);
                }
            }

            int numSortest = sortest.size();

            // Reconcile. Scan all exams that should have emitted a test score, and delete the test
            // scores as we match them up. Report any missing. At the end, report any scores that
            // have no legitimate source.

            for (final RawStmpe p : placements) {

                boolean need00 = true;
                boolean need17 = true;
                boolean need18 = true;
                boolean need24 = true;
                boolean need25 = true;
                boolean need26 = true;

                for (int i = 0; i < numSortest; ++i) {
                    final RawMpscorequeue row = sortest.get(i);

                    if (need00 && "MC00".equals(row.testCode) && row.testDate.toLocalDate().equals(p.examDt)) {
                        sortest.remove(i);
                        --i;
                        --numSortest;
                        need00 = false;

                    } else if (need17 && "MC17".equals(row.testCode) && row.testDate.toLocalDate().equals(p.examDt)) {
                        sortest.remove(i);
                        --i;
                        --numSortest;
                        need17 = false;
                    } else if (need18 && "MC18".equals(row.testCode) && row.testDate.toLocalDate().equals(p.examDt)) {
                        sortest.remove(i);
                        --i;
                        --numSortest;
                        need18 = false;
                    } else if (need24 && "MC24".equals(row.testCode) && row.testDate.toLocalDate().equals(p.examDt)) {
                        sortest.remove(i);
                        --i;
                        --numSortest;
                        need24 = false;
                    } else if (need25 && "MC25".equals(row.testCode) && row.testDate.toLocalDate().equals(p.examDt)) {
                        sortest.remove(i);
                        --i;
                        --numSortest;
                        need25 = false;
                    } else if (need26 && "MC26".equals(row.testCode) && row.testDate.toLocalDate().equals(p.examDt)) {
                        sortest.remove(i);
                        --i;
                        --numSortest;
                        need26 = false;
                    }
                }

                final LocalDateTime fin = p.getFinishDateTime();

                if (fin != null) {
                    if (need00) {
                        if (spc) {
                            report.add(CoreConstants.SPC);
                            spc = false;
                        }
                        report.add(ERROR + stuId + " does not have 'MC00' score for placement attempt on "
                                   + TemporalUtils.FMT_MDY_AT_HM_A.format(fin));
                    }
                    if (need17) {
                        if (spc) {
                            report.add(CoreConstants.SPC);
                            spc = false;
                        }
                        report.add(ERROR + stuId + " does not have 'MC17' score for placement attempt on "
                                   + TemporalUtils.FMT_MDY_AT_HM_A.format(fin));
                    }
                    if (need18) {
                        if (spc) {
                            report.add(CoreConstants.SPC);
                            spc = false;
                        }
                        report.add(ERROR + stuId + " does not have 'MC18' score for placement attempt on "
                                   + TemporalUtils.FMT_MDY_AT_HM_A.format(fin));
                    }
                    if (need24) {
                        if (spc) {
                            report.add(CoreConstants.SPC);
                            spc = false;
                        }
                        report.add(ERROR + stuId + " does not have 'MC24' score for placement attempt on "
                                   + TemporalUtils.FMT_MDY_AT_HM_A.format(fin));
                    }
                    if (need25) {
                        if (spc) {
                            report.add(CoreConstants.SPC);
                            spc = false;
                        }
                        report.add(ERROR + stuId + " does not have 'MC25' score for placement attempt on "
                                   + TemporalUtils.FMT_MDY_AT_HM_A.format(fin));
                    }
                    if (need26) {
                        if (spc) {
                            report.add(CoreConstants.SPC);
                            spc = false;
                        }
                        report.add(ERROR + stuId + " does not have 'MC26' score for placement attempt on "
                                   + TemporalUtils.FMT_MDY_AT_HM_A.format(fin));
                    }
                }
            }

            for (final RawStchallenge challenge : challenges) {
                final String course = challenge.course;
                String testCode = null;

                switch (course) {
                    case RawRecordConstants.M117, RawRecordConstants.MATH117 -> testCode = "MC17";
                    case RawRecordConstants.M118, RawRecordConstants.MATH118 -> testCode = "MC18";
                    case RawRecordConstants.M124, RawRecordConstants.MATH124 -> testCode = "MC24";
                    case RawRecordConstants.M125, RawRecordConstants.MATH125 -> testCode = "MC25";
                    case RawRecordConstants.M126, RawRecordConstants.MATH126 -> testCode = "MC26";
                    case null, default -> {
                        if (spc) {
                            report.add(CoreConstants.SPC);
                            spc = false;
                        }
                        report.add("ERROR: Unexpected course '" + course + "' in challenge exam record");
                    }
                }

                if (testCode != null) {

                    boolean needChal = true;

                    for (int i = 0; i < numSortest; ++i) {
                        final RawMpscorequeue row = sortest.get(i);
                        if (testCode.equals(row.testCode) && row.testDate.toLocalDate().equals(challenge.examDt)
                            && "2".equals(row.testScore)) {
                            sortest.remove(i);
                            --i;
                            --numSortest;
                            needChal = false;
                            break;
                        }
                    }

                    if (needChal) {
                        if (spc) {
                            report.add(CoreConstants.SPC);
                            spc = false;
                        }
                        report.add(ERROR + stuId + " does not have '" + testCode + "' score for challenge attempt on "
                                   + TemporalUtils.FMT_MDY.format(challenge.examDt));
                    }
                }
            }

            for (final RawStexam e3 : elmUnit3ReviewPassed) {

                boolean need = needs00Score2 && needs00Score4;

                for (int i = 0; i < numSortest; ++i) {
                    final RawMpscorequeue row = sortest.get(i);
                    if ("MC00".equals(row.testCode) && row.testDate.toLocalDate().equals(e3.examDt)
                        && "4".equals(row.testScore)) {
                        sortest.remove(i);
                        --i;
                        --numSortest;
                        need = false;
                        break;
                    }
                }

                if (need && e3.getFinishDateTime() != null) {
                    if (spc) {
                        report.add(CoreConstants.SPC);
                        spc = false;
                    }
                    report.add(ERROR + stuId + " does not have 'MC00' score 4 for ELM Review exam 3 on "
                               + TemporalUtils.FMT_MDY_AT_HM_A.format(e3.getFinishDateTime()));
                }
            }

            for (final RawStexam e4 : elmUnit4ExamPassed) {

                boolean need = needs00Score2;

                for (int i = 0; i < numSortest; ++i) {
                    final RawMpscorequeue row = sortest.get(i);
                    if ("MC00".equals(row.testCode) && row.testDate.toLocalDate().equals(e4.examDt)
                        && "2".equals(row.testScore)) {
                        sortest.remove(i);
                        --i;
                        --numSortest;
                        need = false;
                        break;
                    }
                }

                if (need && e4.getFinishDateTime() != null) {
                    if (spc) {
                        report.add(CoreConstants.SPC);
                        spc = false;
                    }
                    report.add(ERROR + stuId + " does not have 'MC00' score 2 for ELM Exam on "
                               + TemporalUtils.FMT_MDY_AT_HM_A.format(e4.getFinishDateTime()));
                }
            }

            for (final RawStexam p17 : pt117Unit4ExamPassed) {

                boolean need = true;

                for (int i = 0; i < numSortest; ++i) {
                    final RawMpscorequeue row = sortest.get(i);
                    if ("MC17".equals(row.testCode) && row.testDate.toLocalDate().equals(p17.examDt)
                        && "1".equals(row.testScore)) {
                        sortest.remove(i);
                        --i;
                        --numSortest;
                        need = false;
                        break;
                    }
                }

                if (need && p17.getFinishDateTime() != null) {
                    if (spc) {
                        report.add(CoreConstants.SPC);
                        spc = false;
                    }
                    report.add(ERROR + stuId + " does not have 'MC17' score 1 for Precalc Tutorial Exam on "
                               + TemporalUtils.FMT_MDY_AT_HM_A.format(p17.getFinishDateTime()));
                }
            }

            for (final RawStexam p18 : pt118Unit4ExamPassed) {

                boolean need = true;

                for (int i = 0; i < numSortest; ++i) {
                    final RawMpscorequeue row = sortest.get(i);
                    if ("MC18".equals(row.testCode) && row.testDate.toLocalDate().equals(p18.examDt)
                        && "1".equals(row.testScore)) {
                        sortest.remove(i);
                        --i;
                        --numSortest;
                        need = false;
                        break;
                    }
                }

                if (need && p18.getFinishDateTime() != null) {
                    if (spc) {
                        report.add(CoreConstants.SPC);
                        spc = false;
                    }
                    report.add(ERROR + stuId + " does not have 'MC18' score 1 for Precalc Tutorial Exam on "
                               + TemporalUtils.FMT_MDY_AT_HM_A.format(p18.getFinishDateTime()));
                }
            }

            for (final RawStexam p24 : pt124Unit4ExamPassed) {

                boolean need = true;

                for (int i = 0; i < numSortest; ++i) {
                    final RawMpscorequeue row = sortest.get(i);
                    if ("MC24".equals(row.testCode) && row.testDate.toLocalDate().equals(p24.examDt)
                        && "1".equals(row.testScore)) {
                        sortest.remove(i);
                        --i;
                        --numSortest;
                        need = false;
                        break;
                    }
                }

                if (need && p24.getFinishDateTime() != null) {
                    if (spc) {
                        report.add(CoreConstants.SPC);
                        spc = false;
                    }
                    report.add(ERROR + stuId + " does not have 'MC24' score 1 for Precalc Tutorial Exam on "
                               + TemporalUtils.FMT_MDY_AT_HM_A.format(p24.getFinishDateTime()));
                }
            }

            for (final RawStexam p25 : pt125Unit4ExamPassed) {

                boolean need = true;

                for (int i = 0; i < numSortest; ++i) {
                    final RawMpscorequeue row = sortest.get(i);
                    if ("MC25".equals(row.testCode) && row.testDate.toLocalDate().equals(p25.examDt)
                        && "1".equals(row.testScore)) {
                        sortest.remove(i);
                        --i;
                        --numSortest;
                        need = false;
                        break;
                    }
                }

                if (need && p25.getFinishDateTime() != null) {
                    if (spc) {
                        report.add(CoreConstants.SPC);
                        spc = false;
                    }
                    report.add(ERROR + stuId + " does not have 'MC25' score 1 for Precalc Tutorial Exam on "
                               + TemporalUtils.FMT_MDY_AT_HM_A.format(p25.getFinishDateTime()));
                }
            }

            for (final RawStexam p26 : pt126Unit4ExamPassed) {

                boolean need = true;

                for (int i = 0; i < numSortest; ++i) {
                    final RawMpscorequeue row = sortest.get(i);
                    if ("MC26".equals(row.testCode) && row.testDate.toLocalDate().equals(p26.examDt)
                        && "1".equals(row.testScore)) {
                        sortest.remove(i);
                        --i;
                        --numSortest;
                        need = false;
                        break;
                    }
                }

                if (need && p26.getFinishDateTime() != null) {
                    if (spc) {
                        report.add(CoreConstants.SPC);
                        spc = false;
                    }
                    report.add(ERROR + stuId + " does not have 'MC26' score 1 for Precalc Tutorial Exam on "
                               + TemporalUtils.FMT_MDY_AT_HM_A.format(p26.getFinishDateTime()));
                }
            }

            for (int i = 0; i < numSortest; ++i) {
                final RawMpscorequeue row = sortest.get(i);

                if (spc) {
                    report.add(CoreConstants.SPC);
                    spc = false;
                }
                report.add(ERROR + stuId + " has extra SORTEST record: '" + row.testCode + "', score = "
                           + row.testScore + " on " + TemporalUtils.FMT_MDY_AT_HM_A.format(row.testDate));
            }
        }
    }

    /**
     * Gathers the set of unique student IDs represented by collections of placement attempts, challenge attempts, and
     * exam records.
     *
     * @param placementAttempts the placement attempts
     * @param challengeAttempts the challenge attempts
     * @param exams             the exam records
     * @return the set of student IDs
     */
    private static Set<String> gatherStudentIds(final Iterable<? extends List<RawStmpe>> placementAttempts,
                                                final Iterable<? extends List<RawStchallenge>> challengeAttempts,
                                                final Iterable<? extends List<RawStexam>> exams) {

        final Set<String> studentIds = new TreeSet<>();

        for (final List<RawStmpe> list : placementAttempts) {
            for (final RawStmpe att : list) {
                studentIds.add(att.stuId);
            }
        }

        for (final List<RawStchallenge> list : challengeAttempts) {
            for (final RawStchallenge att : list) {
                studentIds.add(att.stuId);
            }
        }

        for (final List<RawStexam> list : exams) {
            for (final RawStexam exam : list) {
                studentIds.add(exam.stuId);
            }
        }

        studentIds.remove("888888888");
        studentIds.remove("823251213");

        return studentIds;
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        DbConnection.registerDrivers();
        final AuditBannerTestScores job = new AuditBannerTestScores();

        Log.fine(job.execute());
    }
}
