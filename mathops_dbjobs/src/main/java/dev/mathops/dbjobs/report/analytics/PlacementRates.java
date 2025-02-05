package dev.mathops.dbjobs.report.analytics;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.rawlogic.RawFfrTrnsLogic;
import dev.mathops.db.old.rawlogic.RawMpeCreditLogic;
import dev.mathops.db.old.rawlogic.RawPrereqLogic;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawlogic.RawStmpeLogic;
import dev.mathops.db.old.rawrecord.RawFfrTrns;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawPrereq;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.old.rawrecord.RawStmpe;
import dev.mathops.db.type.TermKey;
import dev.mathops.dbjobs.batch.IdentifyEngineering;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class calculates, for a population identified by SPECIAL_STUS records, summary placement outcomes over a period
 * of time.  It will generate a report with the number of students in the population, the number that completed the Math
 * Placement Tool during that time, and their ultimate results, including placement tutorial activities.
 */
final class PlacementRates {

    /** The data cache. */
    private final Cache cache;

    /**
     * Constructs a new {@code PlacementRates}.
     *
     * @param theCache the data cache
     */
    private PlacementRates(final Cache theCache) {

        this.cache = theCache;
    }

    /**
     * Executes the analysis for a specified term.
     *
     * @param profile         the profile
     * @param term            the term for which the SPECIAL_STUS records were generated
     * @param specialCategory the SPECIAL_STUS category used to identify a list of students
     */
    private static void executeWithProfile(final Profile profile, final TermKey term, final String specialCategory) {

        if (profile == null) {
            Log.warning("Unable to create production profile.");
        } else {
            final Cache cache = new Cache(profile);
            Log.info("Connected to " + profile.id);

            try {
                final PlacementRates obj = new PlacementRates(cache);
                obj.calculate(term, specialCategory);
            } catch (final SQLException ex) {
                Log.warning(ex);
            }
        }
    }

    /**
     * Executes the job.
     *
     * @param term            the term for which the SPECIAL_STUS records were generated
     * @param specialCategory the SPECIAL_STUS category used to identify a list of students
     * @throws SQLException if there is an error accessing the database
     */
    private void calculate(final TermKey term, final String specialCategory) throws SQLException {

        final File desktop = new File("F:/OneDrive - Colostate/Desktop/");
        final HtmlBuilder htm = new HtmlBuilder(10000);

        final List<RawStmpe> allStmpe = RawStmpeLogic.queryAll(this.cache);
        final List<RawMpeCredit> allMpeCredit = RawMpeCreditLogic.queryAll(this.cache);
        final List<RawFfrTrns> allTransfer = RawFfrTrnsLogic.queryAll(this.cache);
        final List<RawPrereq> allPrereq = RawPrereqLogic.queryAll(this.cache);

        final LocalDate today = LocalDate.now();
        final List<RawSpecialStus> specials = RawSpecialStusLogic.queryActiveByType(this.cache, specialCategory, today);

        final int total = specials.size();

        final List<StudentStatus> done160 = new ArrayList<>(100);
        final List<StudentStatus> readyFor160 = new ArrayList<>(100);
        final List<StudentStatus> readyFor126 = new ArrayList<>(100);
        final List<StudentStatus> readyFor124125 = new ArrayList<>(100);
        final List<StudentStatus> readyFor118 = new ArrayList<>(100);
        final List<StudentStatus> readyFor117 = new ArrayList<>(100);
        final List<StudentStatus> notReady = new ArrayList<>(100);
        final List<StudentStatus> noAttempt = new ArrayList<>(100);

        for (final RawSpecialStus row : specials) {
            final String stuId = row.stuId;
            final StudentStatus status = computeStatus(stuId, allStmpe, allMpeCredit, allTransfer, allPrereq);

            if (status.doneWith160()) {
                done160.add(status);
            } else if (status.readyFor160()) {
                readyFor160.add(status);
            } else if (status.readyFor126()) {
                readyFor126.add(status);
            } else if (status.readyFor124() || status.readyFor125()) {
                readyFor124125.add(status);
            } else if (status.readyFor118()) {
                readyFor118.add(status);
            } else if (status.readyFor117()) {
                readyFor117.add(status);
            } else if (status.numMptTries() == 0) {
                noAttempt.add(status);
            } else {
                notReady.add(status);
            }
        }

        final String heading = SimpleBuilder.concat("Placement Outcomes for Engineering Applicants in the ",
                term.longString, " term:");
        htm.addln(heading);
        htm.addln("-".repeat(heading.length()));
        htm.addln();

        final int numNotAttempted = noAttempt.size();
        final int numTried = total - numNotAttempted;

        final DecimalFormat fmt = new DecimalFormat("#0.00");

        htm.addln(total + " students were found with a ", term.longString, " application term and with an Engineering");
        htm.addln("program of study.");
        htm.addln();

        final String numNotAttemptedStr = Integer.toString(numNotAttempted);
        htm.addln("Of these, ", numNotAttemptedStr, " never attempted Math Placement and had no transfer credit that");
        htm.addln("cleared any Math prerequisites.  We will consider this as 'melt'.");
        htm.addln();

        final String numTriedStr = Integer.toString(numTried);
        htm.addln("This leaves ", numTriedStr, " who either tried Math Placement or had some math transfer credit.");
        htm.addln("The analysis below considers only this population.");
        htm.addln();
        htm.addln();

        htm.addln("Overall Readiness:");
        htm.addln("------------------");

        final int numDone = done160.size();
        final double pctDone = ((double) numDone * 100.0) / (double) numTried;

        final int num160 = readyFor160.size();
        final double pct160 = ((double) num160 * 100.0) / (double) numTried;

        final int num126 = readyFor126.size();
        final double pct126 = ((double) num126 * 100.0) / (double) numTried;

        final int num124125 = readyFor124125.size();
        final double pct124125 = ((double) num124125 * 100.0) / (double) numTried;

        final int num118 = readyFor118.size();
        final double pct118 = ((double) num118 * 100.0) / (double) numTried;

        final int num117 = readyFor117.size();
        final double pct117 = ((double) num117 * 100.0) / (double) numTried;

        final int numNot = notReady.size();
        final double pctNot = ((double) numNot * 100.0) / (double) numTried;

        final String numDoneStr = formatNumber(numDone, 8);
        htm.add(numDoneStr + " (");
        final String pctDoneStr = fmt.format(pctDone);
        if (pctDoneStr.length() < 5) {
            htm.add(CoreConstants.SPC);
        }
        htm.addln(pctDoneStr + "%) already had transfer credit for MATH 160");

        final String num160Str = formatNumber(num160, 8);
        htm.add(num160Str + " (");
        final String pct160Str = fmt.format(pct160);
        if (pct160Str.length() < 5) {
            htm.add(CoreConstants.SPC);
        }
        htm.addln(pct160Str + "%) were ready for MATH 160");

        final String num126Str = formatNumber(num126, 8);
        htm.add(num126Str + " (");
        final String pct126Str = fmt.format(pct126);
        if (pct126Str.length() < 5) {
            htm.add(CoreConstants.SPC);
        }
        htm.addln(pct126Str + "%) were ready for MATH 126, but not MATH 160");

        final String num125Str = formatNumber(num124125, 8);
        htm.add(num125Str + " (");
        final String pct125Str = fmt.format(pct124125);
        if (pct125Str.length() < 5) {
            htm.add(CoreConstants.SPC);
        }
        htm.addln(pct125Str + "%) were ready for MATH 124 and MATH 125, but not MATH 126");

        final String num118Str = formatNumber(num118, 8);
        htm.add(num118Str + " (");
        final String pct118Str = fmt.format(pct118);
        if (pct118Str.length() < 5) {
            htm.add(CoreConstants.SPC);
        }
        htm.addln(pct118Str + "%) were ready for MATH 118, but not MATH 124 or MATH 125");

        final String num117Str = formatNumber(num117, 8);
        htm.add(num117Str + " (");
        final String pct117Str = fmt.format(pct117);
        if (pct117Str.length() < 5) {
            htm.add(CoreConstants.SPC);
        }
        htm.addln(pct117Str + "%) were ready for MATH 117, but not MATH 118");

        final String numNotStr = formatNumber(numNot, 8);
        htm.add(numNotStr + " (");
        final String pctNotStr = fmt.format(pctNot);
        if (pctNotStr.length() < 5) {
            htm.add(CoreConstants.SPC);
        }
        htm.addln(pctNotStr + "%) were not ready for MATH 117");
        htm.addln();

        htm.addln("NOTE: readiness for MATH 124, MATH 125, MATH 126 or MATH 160 implies eligibility");
        htm.addln("      for CHEM 111/112.");
        htm.addln();
        htm.addln();

        htm.addln("Detailed Analysis:");
        htm.addln("------------------");

        detailedReadyFor160(htm, readyFor160);
        detailedReadyFor126(htm, readyFor126);
        detailedReadyFor124125(htm, readyFor124125);
        detailedReadyFor118(htm, readyFor118);
        detailedReadyFor117(htm, readyFor117);
        detailedNotReady(htm, notReady);

        //

        final String filename = "Engineering_Placement_Rates_" + term.shortString + ".txt";

        final String reportString = htm.toString();
        final File target = new File(desktop, filename);
        try (final FileWriter w = new FileWriter(target, StandardCharsets.UTF_8)) {
            w.write(reportString);
        } catch (final IOException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Computed and emits a detailed analysis of students who were ready for MATH 160.
     *
     * @param htm         the {@code HtmlBuilder} to which to append
     * @param readyFor160 the list of students who were ready for MATH 160
     */
    private static void detailedReadyFor160(final HtmlBuilder htm, final Collection<StudentStatus> readyFor160) {

        final int count = readyFor160.size();

        htm.addln("1. Of the " + count + " students who were ready for MATH 160:");
        htm.addln();

        int numPlaceAll = 0;
        int numPlace124Tutorial126 = 0;
        int numPlace124Transfer126 = 0;
        int numPlace126Tutorial124 = 0;
        int numPlace126Transfer124 = 0;
        int numTutorialAll = 0;
        int numTutorial126Transfer124 = 0;
        int numTutorial124Transfer126 = 0;
        int numTransferAll = 0;

        for (final StudentStatus record : readyFor160) {
            if (record.m124Place()) {
                if (record.m126Place()) {
                    ++numPlaceAll;
                } else if (record.tutorial126()) {
                    ++numPlace124Tutorial126;
                } else {
                    ++numPlace124Transfer126;
                }
            } else if (record.m126Place()) {
                if (record.tutorial124()) {
                    ++numPlace126Tutorial124;
                } else {
                    ++numPlace126Transfer124;
                }
            } else if (record.tutorial126()) {
                if (record.tutorial124()) {
                    ++numTutorialAll;
                } else {
                    ++numTutorial126Transfer124;
                }
            } else if (record.tutorial124()) {
                ++numTutorial124Transfer126;
            } else {
                ++numTransferAll;
            }
        }

        final String str1 = formatNumber(numPlaceAll, 8);
        htm.addln(str1, " were ready due to Math Placement");

        final String str2 = formatNumber(numPlace124Tutorial126, 8);
        htm.addln(str2, " placed out of MATH 124, and completed the Precalculus Tutorial for MATH 126");

        final String str3 = formatNumber(numPlace126Tutorial124, 8);
        htm.addln(str3, " placed out of MATH 126, and completed the Precalculus Tutorial for MATH 124");

        final String str4 = formatNumber(numTutorialAll, 8);
        htm.addln(str4, " completed the Precalculus Tutorials for MATH 124 and MATH 126");

        final String str5 = formatNumber(numPlace124Transfer126, 8);
        htm.addln(str5, " placed out of MATH 124 and had transfer credit for MATH 126");

        final String str6 = formatNumber(numPlace126Transfer124, 8);
        htm.addln(str6, " placed out of MATH 126 and had transfer credit for MATH 124");

        final String str7 = formatNumber(numTutorial126Transfer124, 8);
        htm.addln(str7, " had transfer credit for MATH 124 and completed the Precalculus Tutorial for MATH 126");

        final String str8 = formatNumber(numTutorial124Transfer126, 8);
        htm.addln(str8, " had transfer credit for MATH 126 and completed the Precalculus Tutorial for MATH 124");

        final String str9 = formatNumber(numTransferAll, 8);
        htm.addln(str9, " had transfer credit for MATH 124 and MATH 126");

        htm.addln();
    }

    /**
     * Computed and emits a detailed analysis of students who were ready for MATH 126 (but not MATH 160).
     *
     * @param htm         the {@code HtmlBuilder} to which to append
     * @param readyFor126 the list of students who were ready for MATH 126
     */
    private static void detailedReadyFor126(final HtmlBuilder htm, final Collection<StudentStatus> readyFor126) {

        final int count = readyFor126.size();

        htm.addln("2. Of the " + count + " students who were ready for MATH 126 (but not for MATH 160):");
        htm.addln();

        int numPlace125 = 0;
        int numTutorial125 = 0;
        int numTransfer125 = 0;

        for (final StudentStatus record : readyFor126) {
            if (record.m125Place()) {
                ++numPlace125;
            } else if (record.tutorial125()) {
                ++numTutorial125;
            } else {
                ++numTransfer125;
            }
        }

        final String str1 = formatNumber(numPlace125, 8);
        htm.addln(str1, " placed out of MATH 125");

        final String str2 = formatNumber(numTutorial125, 8);
        htm.addln(str2, " completed the Precalculus Tutorial for MATH 125");

        final String str3 = formatNumber(numTransfer125, 8);
        htm.addln(str3, " had transfer credit for MATH 125");

        htm.addln();
    }

    /**
     * Computed and emits a detailed analysis of students who were ready for MATH 124 and MATH 125 (but not MATH 126).
     *
     * @param htm            the {@code HtmlBuilder} to which to append
     * @param readyFor124125 the list of students who were ready for MATH 124 and MATH 125
     */
    private static void detailedReadyFor124125(final HtmlBuilder htm, final Collection<StudentStatus> readyFor124125) {

        final int count = readyFor124125.size();

        htm.addln("3. Of the " + count + " students who were ready for MATH 124 and MATH 125 (but not for MATH 126):");
        htm.addln();

        int numPlace118 = 0;
        int numTutorial118 = 0;
        int numTransfer118 = 0;

        for (final StudentStatus record : readyFor124125) {
            if (record.m118Place()) {
                ++numPlace118;
            } else if (record.tutorial118()) {
                ++numTutorial118;
            } else {
                ++numTransfer118;
            }
        }

        final String str1 = formatNumber(numPlace118, 8);
        htm.addln(str1, " placed out of MATH 118");

        final String str2 = formatNumber(numTutorial118, 8);
        htm.addln(str2, " completed the Precalculus Tutorial for MATH 118");

        final String str3 = formatNumber(numTransfer118, 8);
        htm.addln(str3, " had transfer credit for MATH 118");

        htm.addln();
    }

    /**
     * Computed and emits a detailed analysis of students who were ready for MATH 118 (but not MATH 124 or MATH 125).
     *
     * @param htm         the {@code HtmlBuilder} to which to append
     * @param readyFor118 the list of students who were ready for MATH 118
     */
    private static void detailedReadyFor118(final HtmlBuilder htm, final Collection<StudentStatus> readyFor118) {

        final int count = readyFor118.size();

        htm.addln("4. Of the " + count + " students who were ready for MATH 118 (but not for MATH 124 or MATH 125):");
        htm.addln();

        int numPlace117 = 0;
        int numTutorial117 = 0;
        int numTransfer117 = 0;

        for (final StudentStatus record : readyFor118) {
            if (record.m117Place()) {
                ++numPlace117;
            } else if (record.tutorial117()) {
                ++numTutorial117;
            } else {
                ++numTransfer117;
            }
        }

        final String str1 = formatNumber(numPlace117, 8);
        htm.addln(str1, " placed out of MATH 117");

        final String str2 = formatNumber(numTutorial117, 8);
        htm.addln(str2, " completed the Precalculus Tutorial for MATH 117");

        final String str3 = formatNumber(numTransfer117, 8);
        htm.addln(str3, " had transfer credit for MATH 117");

        htm.addln();
    }

    /**
     * Computed and emits a detailed analysis of students who were ready for MATH 117 (but not MATH 118).
     *
     * @param htm         the {@code HtmlBuilder} to which to append
     * @param readyFor117 the list of students who were ready for MATH 117
     */
    private static void detailedReadyFor117(final HtmlBuilder htm, final Collection<StudentStatus> readyFor117) {

        final int count = readyFor117.size();

        htm.addln("5. Of the " + count + " students who were ready for MATH 117 (but not for MATH 118):");
        htm.addln();

        int numPlace100C = 0;
        int numTutorial100C = 0;
        int numTransfer100C = 0;

        for (final StudentStatus record : readyFor117) {
            if (record.m100cPlace()) {
                ++numPlace100C;
            } else if (record.tutorial100C()) {
                ++numTutorial100C;
            } else {
                ++numTransfer100C;
            }
        }

        final String str1 = formatNumber(numPlace100C, 8);
        htm.addln(str1, " placed into MATH 117");

        final String str2 = formatNumber(numTutorial100C, 8);
        htm.addln(str2, " completed the ELM Tutorial");

        final String str3 = formatNumber(numTransfer100C, 8);
        htm.addln(str3, " had transfer credit that satisfies the prerequisite for MATH 117");

        htm.addln();
    }

    /**
     * Computed and emits a detailed analysis of students who were not ready for MATH 117.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param notReady the list of students who were not ready for MATH 117
     */
    private static void detailedNotReady(final HtmlBuilder htm, final Collection<StudentStatus> notReady) {

        final int count = notReady.size();

        htm.addln("6. Of the " + count + " students who were ready for MATH 117 (but not for MATH 118):");
        htm.addln();

        int zeroTries = 0;
        int oneTry = 0;
        int twoTries = 0;

        for (final StudentStatus record : notReady) {
            final int tries = record.numMptTries();

            if (tries == 0) {
                ++zeroTries;
            } else if (tries == 1) {
                ++oneTry;
            } else {
                ++twoTries;
            }
        }

        final String str1 = formatNumber(oneTry, 8);
        htm.addln(str1, " used only one attempt on the Math Placement Tool");

        final String str2 = formatNumber(twoTries, 8);
        htm.addln(str2, " used both attempts on the Math Placement Tool");

        if (zeroTries > 0) {
            final String str3 = formatNumber(zeroTries, 8);
            htm.addln(str3, " never attempted the Math Placement Tool");
        }

        htm.addln();
    }

    /**
     * Formats a number to occupy a specified field width, right-justified within that field.
     *
     * @param number the number
     * @param width  the field width
     * @return the formatted string
     */
    private static String formatNumber(final int number, final int width) {

        final String numberStr = Integer.toString(number);
        final int len = numberStr.length();

        return CoreConstants.SPC.repeat(width - len) + numberStr;
    }

    /**
     * Computes the status of a single user.
     *
     * @param stuId        the student ID
     * @param allStmpe     all STMPE records
     * @param allMpeCredit all MPECREDIT records
     * @param allTransfer  all FRR_TRNS records
     * @param allPrereq    all PREREQ records
     * @return the student's status
     */
    private StudentStatus computeStatus(final String stuId, final List<RawStmpe> allStmpe,
                                        final List<RawMpeCredit> allMpeCredit,
                                        final List<RawFfrTrns> allTransfer, final List<RawPrereq> allPrereq) {

        // Count the number of attempts on Math Placement, and what courses they placed out of

        boolean readyFor117 = false;
        boolean readyFor118 = false;
        boolean readyFor124 = false;
        boolean readyFor125 = false;
        boolean readyFor126 = false;
        boolean readyFor160 = false;
        boolean doneWith160 = false;

        int numMptTries = 0;
        boolean m100cPlace = false;
        boolean m117Place = false;
        boolean m118Place = false;
        boolean m124Place = false;
        boolean m125Place = false;
        boolean m126Place = false;

        for (final RawStmpe test : allStmpe) {
            if (test.stuId.equals(stuId)) {
                ++numMptTries;

                for (final RawMpeCredit cr : allMpeCredit) {
                    if (cr.serialNbr != null && cr.serialNbr.equals(test.serialNbr)) {
                        final String course = cr.course;

                        if ("M 100C".equals(course)) {
                            m100cPlace = true;
                            readyFor117 = true;
                        } else if ("M 117".equals(course)) {
                            m100cPlace = true;
                            m117Place = true;
                            readyFor118 = true;
                        } else if ("M 118".equals(course)) {
                            m100cPlace = true;
                            m117Place = true;
                            m118Place = true;
                            readyFor124 = true;
                            readyFor125 = true;
                        } else if ("M 124".equals(course)) {
                            m100cPlace = true;
                            m117Place = true;
                            m118Place = true;
                            m124Place = true;
                        } else if ("M 125".equals(course)) {
                            m100cPlace = true;
                            m117Place = true;
                            m118Place = true;
                            m125Place = true;
                            readyFor126 = true;
                        } else if ("M 126".equals(course)) {
                            m100cPlace = true;
                            m117Place = true;
                            m118Place = true;
                            m125Place = true;
                            m126Place = true;
                        }
                    }
                }
            }
        }

        boolean xfer117 = false;
        boolean xfer118 = false;
        boolean xfer124 = false;
        boolean xfer125 = false;
        boolean xfer126 = false;
        boolean xfer160 = false;

        // Get a list of transfer credit and what prereqs that satisfies
        for (final RawFfrTrns test : allTransfer) {
            if (test.stuId.equals(stuId)) {
                final String course = test.course;

                switch (course) {
                    case "M 160" -> {
                        xfer160 = true;
                        doneWith160 = true;
                    }
                    case "M 117" -> {
                        xfer117 = true;
                        readyFor118 = true;
                    }
                    case "M 118" -> {
                        xfer118 = true;
                        readyFor124 = true;
                        readyFor125 = true;
                    }
                    case "M 124" -> xfer124 = true;
                    case "M 125" -> {
                        xfer125 = true;
                        readyFor126 = true;
                    }
                    case "M 126" -> xfer126 = true;
                    case "M 120" -> {
                        xfer117 = true;
                        xfer118 = true;
                        xfer124 = true;
                        readyFor125 = true;
                    }
                    case "M 127" -> {
                        xfer117 = true;
                        xfer118 = true;
                        xfer124 = true;
                        xfer125 = true;
                        xfer126 = true;
                        readyFor160 = true;
                    }
                    case null, default -> {
                        for (final RawPrereq prereq : allPrereq) {
                            if (prereq.prerequisite.equals(course)) {
                                final String prereqFor = prereq.course;

                                if ("M 117".equals(prereqFor)) {
                                    readyFor117 = true;
                                } else if ("M 118".equals(prereqFor)) {
                                    readyFor118 = true;
                                } else if ("M 124".equals(prereqFor)) {
                                    readyFor124 = true;
                                } else if ("M 125".equals(prereqFor)) {
                                    readyFor125 = true;
                                } else if ("M 126".equals(prereqFor)) {
                                    readyFor126 = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        // Find other placement results not associated with MPT attempts (assume these are from tutorials)

        boolean tutorial100C = false;
        boolean tutorial117 = false;
        boolean tutorial118 = false;
        boolean tutorial124 = false;
        boolean tutorial125 = false;
        boolean tutorial126 = false;

        for (final RawMpeCredit cr : allMpeCredit) {
            if (cr.stuId.equals(stuId)) {
                final String course = cr.course;

                if ("M 100C".equals(course) && !m100cPlace) {
                    tutorial100C = true;
                    readyFor117 = true;
                } else if ("M 117".equals(course) && !m117Place) {
                    tutorial117 = true;
                    readyFor118 = true;
                } else if ("M 118".equals(course) && !m118Place) {
                    tutorial118 = true;
                    readyFor124 = true;
                    readyFor125 = true;
                } else if ("M 124".equals(course) && !m124Place) {
                    tutorial124 = true;
                } else if ("M 125".equals(course) && !m125Place) {
                    tutorial125 = true;
                    readyFor126 = true;
                } else if ("M 126".equals(course) && !m126Place) {
                    tutorial126 = true;
                }
            }
        }

        if ((xfer124 || m124Place || tutorial124) && (xfer126 || m126Place || tutorial126)) {
            readyFor160 = true;
        }

        return new StudentStatus(numMptTries, readyFor117, readyFor118, readyFor124, readyFor125, readyFor126,
                readyFor160, doneWith160, m100cPlace, m117Place, m118Place, m124Place, m125Place, m126Place, xfer117,
                xfer118, xfer124, xfer125, xfer126, xfer160, tutorial100C, tutorial117, tutorial118, tutorial124,
                tutorial125, tutorial126);
    }

    /** A container for student status. */
    record StudentStatus(int numMptTries, boolean readyFor117, boolean readyFor118, boolean readyFor124,
                         boolean readyFor125, boolean readyFor126, boolean readyFor160, boolean doneWith160,
                         boolean m100cPlace, boolean m117Place, boolean m118Place, boolean m124Place, boolean m125Place,
                         boolean m126Place, boolean xfer117, boolean xfer118, boolean xfer124, boolean xfer125,
                         boolean xfer126, boolean xfer160, boolean tutorial100C, boolean tutorial117,
                         boolean tutorial118, boolean tutorial124, boolean tutorial125, boolean tutorial126) {
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        DbConnection.registerDrivers();
        final DatabaseConfig config = DatabaseConfig.getDefault();
        final Profile profile = config.getCodeProfile(Contexts.BATCH_PATH);

        final TermKey term = new TermKey(ETermName.SUMMER, 2023);

        executeWithProfile(profile, term, IdentifyEngineering.SPECIAL_CATEGORY);
    }
}
