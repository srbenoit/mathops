package dev.mathops.dbjobs.report.analytics.longitudinal;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.dbjobs.report.analytics.longitudinal.data.EnrollmentRec;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * A utility class to analyze success in course sequences (success in one course after completing another).
 */
final class SequenceSuccess {

    /** A commonly-used string. */
    private static final String CSV_HEADER1 = SimpleBuilder.concat(
            ",N,Pass %,Avg. Grade,N,Pass %,Avg. Grade,N,Pass %,Avg. Grade,N,Pass %,Avg. Grade,N,Pass %,Avg. Grade,",
            "N,Pass %,Avg. Grade,N,Pass %,Avg. Grade,N,Pass %,Avg. Grade,N,Pass %,Avg. Grade");

    /** A commonly-used string. */
    private static final String CSV_HEADER2 = SimpleBuilder.concat(
            "Year,,CSU (A),CSU (A),,CSU (B),CSU (B),,CSU (C/D),CSU (C/D),,CSU,CSU,,Transfer (A),Transfer (A),,",
            "Transfer (B),Transfer (B),,Transfer (C/D),Transfer (C/D),,Transfer,Transfer,,AP/IB/CLEP,AP/IB/CLEP");

    /** The last 2 digits for a Spring term. */
    private static final int SPRING_TERM = 10;

    /** The last 2 digits for a Summer term. */
    private static final int SUMMER_TERM = 60;

    /** The last 2 digits for a Fall term. */
    private static final int FALL_TERM = 90;

    /** Threshold grade score that is considered an "A". */
    private static final double A_THRESHOLD = 3.5;

    /** Threshold grade score that is considered a "B". */
    private static final double B_THRESHOLD = 2.5;

    /** A decimal formatter. */
    private final DecimalFormat format;

    /** The directory in which to write CSV files. */
    private final File targetDir;

    /** The ordered set of terms represented. */
    private final Collection<Integer> terms;

    /** Data for students who took first course in the prior term. */
    private final ClassifiedData priorTerm;

    /** Data for students who took first course in any earlier term. */
    private final ClassifiedData allEarlierTerms;

    /** The number of students found who took the second course locally. */
    private int numWithSecond = 0;

    /** The number of students who took the first course in the term before the second course. */
    private int numWithFirstPrior = 0;

    /** The number of students who took the first course in any earlier term. */
    private int numWithFirstAny = 0;

    /**
     * Constructs a new {@code SequenceSuccess}.
     *
     * @param theTargetDir the directory in which to write CSV files
     */
    SequenceSuccess(final File theTargetDir) {

        this.format = new DecimalFormat("0.00");
        this.targetDir = theTargetDir;

        this.terms = new TreeSet<>();
        this.priorTerm = new ClassifiedData();
        this.allEarlierTerms = new ClassifiedData();
    }

    /**
     * Given a starting term and a term code, finds the year before the first year to present.
     *
     * @param startTerm the start term
     * @param termCode  the term code
     * @return the year before the first year to include
     */
    private static int getYearBeforeStartYear(final int startTerm, final int termCode) {

        final int startTermCode = startTerm % 100;
        return (termCode > startTermCode) ? (startTerm / 100) - 1 : (startTerm / 100);
    }

    /**
     * Analysis of success in a second course based on how student completed a first course.
     *
     * @param startTerm            the starting term
     * @param endTerm              the ending term
     * @param records              the set of student course records
     * @param firstCourse          the course ID of the first course in the sequence
     * @param firstCourseSections  the list of sections of interest in the first course
     * @param secondCourse         the course ID of the second course in the sequence
     * @param secondCourseSections the list of sections of interest in the second course
     */
    void generate(final int startTerm, final int endTerm,
                  final Map<String, List<EnrollmentRec>> records, final String firstCourse,
                  final String[][] firstCourseSections, final String secondCourse,
                  final String[][] secondCourseSections) {

        Log.fine("");
        Log.fine("Analysis of outcomes in ", secondCourse, " with respect to ", firstCourse);

        gatherData(startTerm, records, firstCourse, firstCourseSections, secondCourse, secondCourseSections);

        final String numWithSecondStr = Integer.toString(this.numWithSecond);
        final String numWithFirstPriorStr = Integer.toString(this.numWithFirstPrior);
        final String numWithFirstAnyStr = Integer.toString(this.numWithFirstAny);

        Log.fine("    Found ", numWithSecondStr, " students who took ", secondCourse, " at CSU");
        Log.fine("    ", numWithFirstPriorStr, " had credit for ", firstCourse, " the prior term");
        Log.fine("    ", numWithFirstAnyStr, " had credit for ", firstCourse, " in any earlier term");

        final HtmlBuilder csv = new HtmlBuilder(10000);

        final String blockHeading = SimpleBuilder.concat(",", firstCourse, " at CSU (A),,,",
                firstCourse, " at CSU (B),,,", firstCourse, " at CSU (C/D),,,", firstCourse, " at CSU (ALL),,,",
                firstCourse, " transfer (A),,,", firstCourse, " transfer (B),,,", firstCourse, " transfer (C/D),,,",
                firstCourse, " transfer (ALL),,,", firstCourse, " via AP/IB/CLEP");

        csv.addln("Pass rates in:,,,,,,,,,,,", secondCourse);
        csv.addln("As function of:,,,,,,,,,,,", firstCourse);
        csv.addln("Total students with ", secondCourse, ":,,,,,,,,,,,", numWithSecondStr);
        csv.addln("Subset with ", firstCourse, " in prior term:,,,,,,,,,,,", numWithFirstPriorStr);
        csv.addln("Subset with ", firstCourse, " in any earlier term:,,,,,,,,,,,", numWithFirstAnyStr);
        csv.addln();

        csv.addln("Reports for individual years [students with credit in ", firstCourse, " in the prior term]");
        csv.addln();
        generateOneYearRows(startTerm, endTerm, blockHeading, csv, firstCourse, secondCourse, this.priorTerm);

        csv.addln("Reports based on average over last three terms [students with credit in ", firstCourse,
                " in the prior term]");
        csv.addln();
        generateThreeYearRows(startTerm, endTerm, blockHeading, csv, firstCourse, secondCourse, this.priorTerm);

        csv.addln("Reports for individual years [students with credit in ", firstCourse, " in the any earlier term]");
        csv.addln();
        generateOneYearRows(startTerm, endTerm, blockHeading, csv, firstCourse, secondCourse, this.allEarlierTerms);

        csv.addln("Reports based on average over last three terms [students with credit in ", firstCourse,
                " in any earlier term]");
        csv.addln();
        generateThreeYearRows(startTerm, endTerm, blockHeading, csv, firstCourse, secondCourse, this.allEarlierTerms);

        final String filename = SimpleBuilder.concat("Sequence_", firstCourse, "_", secondCourse, ".csv");
        final String csvString = csv.toString();

        final File output = new File(this.targetDir, filename);
        try (final FileWriter writer = new FileWriter(output, StandardCharsets.UTF_8)) {
            writer.write(csvString);
        } catch (final IOException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Generates one-year data rows for a term, first/second course combination, and list of classified data.
     *
     * @param startTerm    the starting term
     * @param blockHeading the heading to emit if there are rows to present
     * @param csv          the CSV file builder to which to write
     * @param firstCourse  the first course code
     * @param secondCourse the second course code
     * @param classified   the classified data
     */
    private void generateOneYearRows(final int startTerm, final int endTerm, final String blockHeading,
                                     final HtmlBuilder csv, final String firstCourse, final String secondCourse,
                                     final ClassifiedData classified) {

        final int startYear = startTerm / 100;
        final int endYear = endTerm / 100;

        boolean needsHeader = true;
        final List<Integer> keys = new ArrayList<>(3);

        for (int year = startYear; year <= endYear; ++year) {

            // Gather the keys for the current year.
            for (final Integer key : this.terms) {
                final int value = key.intValue();
                final int keyYear = value / 100;
                if (keyYear == year) {
                    keys.add(key);
                }
            }

            if (needsHeader) {
                csv.addln(blockHeading);
                csv.addln(CSV_HEADER1);
                csv.addln(CSV_HEADER2);
                needsHeader = false;
            }

            final String yearLabel = Integer.toString(year);
            if (keys.isEmpty()) {
                csv.addln(yearLabel);
            } else {
                addYearRow(csv, yearLabel, keys, firstCourse, secondCourse, classified);
            }

            keys.clear();
        }

        csv.addln();
    }

    /**
     * Generates three-year-average data rows for a term, first/second course combination, and list of classified data.
     *
     * @param startTerm    the starting term
     * @param endTerm      the ending term
     * @param blockHeading the heading to emit if there are rows to present
     * @param csv          the CSV file builder to which to write
     * @param firstCourse  the first course code
     * @param secondCourse the second course code
     * @param classified   the classified data
     */
    private void generateThreeYearRows(final int startTerm, final int endTerm, final String blockHeading,
                                       final HtmlBuilder csv, final String firstCourse, final String secondCourse,
                                       final ClassifiedData classified) {

        final int startYear = startTerm / 100;
        final int endYear = endTerm / 100;

        boolean needsHeader = true;
        final List<Integer> keys = new ArrayList<>(9);

        for (int year3 = startYear + 2; year3 <= endYear; ++year3) {
            final int year1 = year3 - 2;
            final int year2 = year3 - 1;

            for (final Integer key : this.terms) {
                final int value = key.intValue();
                final int keyYear = value / 100;
                if (keyYear == year1 || keyYear == year2 || keyYear == year3) {
                    keys.add(key);
                }
            }

            if (needsHeader) {
                csv.addln(blockHeading);
                csv.addln(CSV_HEADER1);
                csv.addln(CSV_HEADER2);
                needsHeader = false;
            }

            final String yearLabel = year1 + "-" + year3;
            if (keys.isEmpty()) {
                csv.addln(yearLabel);
            } else {
                addYearRow(csv, yearLabel, keys, firstCourse, secondCourse, classified);
            }

            keys.clear();
        }

        csv.addln();
    }

    /**
     * Gathers data.
     *
     * @param earliestSecondCourseTerm the earliest term for which to look for the second course
     * @param records                  the list of all student course records
     * @param firstCourse              the course ID of the first course in the sequence
     * @param firstCourseSections      the list of sections of interest in the first course
     * @param secondCourse             the course ID of the second course in the sequence
     * @param secondCourseSections     the list of sections of interest in the second course
     */
    private void gatherData(final int earliestSecondCourseTerm, final Map<String, List<EnrollmentRec>> records,
                            final String firstCourse, final String[][] firstCourseSections,
                            final String secondCourse, final String[][] secondCourseSections) {

        this.terms.clear();
        this.priorTerm.clear();
        this.allEarlierTerms.clear();

        this.numWithSecond = 0;
        this.numWithFirstPrior = 0;
        this.numWithFirstAny = 0;

        // Find all students who took the second course locally (one of the sections of interest)
        for (final Map.Entry<String, List<EnrollmentRec>> entry : records.entrySet()) {
            final List<EnrollmentRec> list = entry.getValue();

            final EnrollmentRec earliestSecond = findEarliestSecond(earliestSecondCourseTerm, list,
                    secondCourse, secondCourseSections);

            if (earliestSecond != null) {
                ++this.numWithSecond;

                final EnrollmentRec latestFirst = findLatestFirstBeforeSecond(earliestSecond, list,
                        firstCourse, firstCourseSections);

                if (latestFirst != null) {
                    final int secondTerm = earliestSecond.academicPeriod();
                    final Integer key = Integer.valueOf(secondTerm);

                    if (this.terms.add(key)) {
                        this.priorTerm.createKey(key);
                        this.allEarlierTerms.createKey(key);
                    }

                    ++this.numWithFirstAny;

                    final boolean inPriorTerm = isPriorTerm(latestFirst, earliestSecond);
                    if (inPriorTerm) {
                        ++this.numWithFirstPrior;

                        final Map<Integer, List<EnrollmentRec>> targetPrior = selectTargetMap(latestFirst,
                                this.priorTerm);

                        if (targetPrior == null) {
                            Log.warning("Unable to identify target prior-term map for ", latestFirst);
                        } else {
                            final List<EnrollmentRec> targetList = targetPrior.get(key);
                            targetList.add(earliestSecond);
                        }
                    }

                    final Map<Integer, List<EnrollmentRec>> targetAny = selectTargetMap(latestFirst,
                            this.allEarlierTerms);

                    if (targetAny == null) {
                        Log.warning("Unable to identify target any-term map for ", latestFirst);
                    } else {
                        final List<EnrollmentRec> targetList = targetAny.get(key);
                        targetList.add(earliestSecond);
                    }
                }
            }
        }
    }

    /**
     * Tests whether a section number appears in a sections array.
     *
     * @param sections an array of arrays of section numbers
     * @param section  the section number for which to search
     * @return true if the section number was found
     */
    private static boolean containsSection(final String[][] sections, final String section) {

        boolean found = false;

        for (final String[] inner : sections) {
            for (final String test : inner) {
                if (test.equals(section)) {
                    found = true;
                    break;
                }
            }
        }

        return found;
    }

    /**
     * Tests whether the first course was taken in a "prior" term to the second course.
     *
     * @param first  the first course
     * @param second the second course
     * @return true if the first course was taken in the prior term to the second course
     */
    private static boolean isPriorTerm(final EnrollmentRec first, final EnrollmentRec second) {

        boolean isPrior = false;

        final int firstTerm = first.academicPeriod();
        final int secondTerm = second.academicPeriod();

        final int firstYear = firstTerm / 100;
        final int secondYear = secondTerm / 100;

        if (firstTerm == secondTerm) {
            // We treat the same term as "prior" since AP or transfer credit could get booked then.
            isPrior = true;
        } else if (secondYear - firstYear <= 1) {
            final int firstPart = firstTerm % 100;
            final int secondPart = secondTerm % 100;

            if (secondPart == SPRING_TERM) {
                // Second course was taken in Spring, prior term is the Fall term of the prior year
                isPrior = firstYear == (secondYear - 1) && firstPart == FALL_TERM;
            } else if (secondPart == FALL_TERM) {
                // Second course was taken in Fall, prior term can be either Spring or Summer of the same year
                isPrior = firstYear == secondYear && (firstPart == SPRING_TERM || firstPart == SUMMER_TERM);
            } else if (secondPart == SUMMER_TERM) {
                // Second course was taken in Summer, prior term is Spring of the same year
                isPrior = firstYear == secondYear && firstPart == SPRING_TERM;
            }
        }

        return isPrior;
    }

    /**
     * Scans a list of student course records and finds the earliest occurrence of the second course in the sequence (if
     * it was one of the sections of interest).
     *
     * @param earliestSecondCourseTerm the earliest term for which to look for the second course
     * @param list                     the list of student course records to scan
     * @param secondCourse             the second course
     * @param secondCourseSections     the list of sections of interest
     * @return the earliest matching record of the second course found; null if none was found
     */
    private static EnrollmentRec findEarliestSecond(final int earliestSecondCourseTerm,
                                                    final Iterable<EnrollmentRec> list,
                                                    final String secondCourse,
                                                    final String[][] secondCourseSections) {

        EnrollmentRec earliestSecond = null;

        for (final EnrollmentRec rec : list) {
            final int term = rec.academicPeriod();

            if (term >= earliestSecondCourseTerm) {
                final String course = rec.course();
                final String sect = rec.section();

                final boolean courseMatch = secondCourse.equals(course)
                                            || ("MATH157".equals(secondCourse) && "MATH180A3".equals(course))
                                            || ("MATH159".equals(secondCourse) && "MATH180A4".equals(course))
                                            || ("MATH156".equals(secondCourse) && "MATH180A5".equals(course))
                                            || ("MATH116".equals(secondCourse) && "MATH181A1".equals(course));

                if (courseMatch && !rec.isTransfer() && containsSection(secondCourseSections, sect)) {
                    if (earliestSecond == null) {
                        earliestSecond = rec;
                    } else {
                        final int existing = earliestSecond.academicPeriod();
                        if (term < existing) {
                            earliestSecond = rec;
                        }
                    }
                }
            }
        }

        return earliestSecond;
    }

    /**
     * Searches for the latest time a first course was completed that is not later than the second course.
     *
     * @param earliestSecond      the earliest record of the second course in the sequence
     * @param list                the list of student course records
     * @param firstCourse         the first course
     * @param firstCourseSections the list of sections of interest
     * @return the latest course record if one was found; null if not
     */
    private EnrollmentRec findLatestFirstBeforeSecond(final EnrollmentRec earliestSecond,
                                                      final Iterable<EnrollmentRec> list,
                                                      final String firstCourse,
                                                      final String[][] firstCourseSections) {

        // Identify the year and term when the second course was taken
        final int secondTerm = earliestSecond.academicPeriod();

        EnrollmentRec latestFirst = null;

        // Scan the list for the first course, tracking the latest found.
        for (final EnrollmentRec rec : list) {
            final String course = rec.course();
            final int term = rec.academicPeriod();
            final String sect = rec.section();

            final boolean courseMatch = firstCourse.equals(course)
                                        || ("MATH157".equals(firstCourse) && "MATH180A3".equals(course))
                                        || ("MATH159".equals(firstCourse) && "MATH180A4".equals(course))
                                        || ("MATH156".equals(firstCourse) && "MATH180A5".equals(course))
                                        || ("MATH116".equals(firstCourse) && "MATH181A1".equals(course));

            if (courseMatch) {
                if (rec.isApIbClep()) {
                    if (term <= secondTerm) {
                        latestFirst = chooseLatest(latestFirst, rec);
                    }
                } else if (rec.isTransfer()) {
                    if (rec.gradeValue() != null && term <= secondTerm) {
                        latestFirst = chooseLatest(latestFirst, rec);
                    }
                } else if (rec.gradeValue() != null && term < secondTerm
                           && containsSection(firstCourseSections, sect)) {
                    final double gradeNumeric = rec.gradeValue().doubleValue();
                    if (gradeNumeric > 0.9) {
                        // Local course, not failed
                        latestFirst = chooseLatest(latestFirst, rec);
                    }
                }
            }
        }

        return latestFirst;
    }

    /**
     * Chooses the latest of two records.  Of they have the same term, AP credit is returned with the highest priority,
     * then local course credit, then transfer credit.  If they have the same term and grade score, the first is
     * returned.
     *
     * @param test1 the first record
     * @param test2 the second record
     * @return the latest record
     */
    private static EnrollmentRec chooseLatest(final EnrollmentRec test1, final EnrollmentRec test2) {

        final EnrollmentRec result;

        if (test1 == null) {
            result = test2;
        } else if (test2 == null) {
            result = test1;
        } else {
            final int term1 = test1.academicPeriod();
            final int term2 = test2.academicPeriod();

            if (term1 > term2) {
                result = test1;
            } else if (term1 < term2) {
                result = test2;
            } else {
                // The two records occur in the same term.

                final boolean ap1 = test1.isApIbClep();
                final boolean ap2 = test2.isApIbClep();

                if (ap1) {
                    if (ap2) {
                        // Both are AP/IB/CLEP; choose that with the higher grade
                        final Double grade1 = test1.gradeValue();
                        final Double grade2 = test2.gradeValue();

                        if (grade1 == null) {
                            result = grade2 == null ? test1 : test2;
                        } else if (grade2 == null) {
                            result = test1;
                        } else {
                            result = grade1.doubleValue() >= grade2.doubleValue() ? test1 : test2;
                        }
                    } else {
                        // First is AP/IB/CLEP, but second is not
                        result = test1;
                    }
                } else if (ap2) {
                    // Second is AP/IB/CLEP, but first is not
                    result = test2;
                } else {
                    // Neither is AP/IB/CLEP
                    final boolean transfer1 = test1.isTransfer();
                    final boolean transfer2 = test2.isTransfer();

                    if (transfer1 && !transfer2) {
                        result = test2;
                    } else if (transfer2 && !transfer1) {
                        result = test1;
                    } else {
                        // Either both are local courses, or both are transfer.
                        final Double grade1 = test1.gradeValue();
                        final Double grade2 = test2.gradeValue();

                        if (grade1 == null) {
                            result = grade2 == null ? test1 : test2;
                        } else if (grade2 == null) {
                            result = test1;
                        } else {
                            result = grade1.doubleValue() >= grade2.doubleValue() ? test1 : test2;
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Given the outcome from the preceding course, selects a map to which to add result data.  The target is chosen
     * based on the grade earned in the first course and how it was completed (taken locally, transferred, or via
     * AP/IB/CLEP).
     *
     * @param precedingFirst the student course record for the
     * @param classified     the classified data
     * @return the target map
     */
    private Map<Integer, List<EnrollmentRec>> selectTargetMap(final EnrollmentRec precedingFirst,
                                                              final ClassifiedData classified) {

        Map<Integer, List<EnrollmentRec>> target = null;

        if (precedingFirst.isApIbClep()) {
            target = classified.ap();
        } else if (precedingFirst.isTransfer()) {

            final Double gv = precedingFirst.gradeValue();
            if (gv != null) {
                final double gvValue = gv.doubleValue();

                if (gvValue > A_THRESHOLD) {
                    target = classified.transferA();
                } else if (gvValue > B_THRESHOLD) {
                    target = classified.transferB();
                } else {
                    target = classified.transferCD();
                }
            }
        } else {
            final Double gv = precedingFirst.gradeValue();

            if (gv != null) {
                final double gvValue = gv.doubleValue();

                if (gvValue > A_THRESHOLD) {
                    target = classified.localA();
                } else if (gvValue > B_THRESHOLD) {
                    target = classified.localB();
                } else {
                    target = classified.localCD();
                }
            }
        }

        return target;
    }

    /**
     * Adds a single row to the CSV file.  Each row represents performance in a single term.
     *
     * @param csv          the CSV
     * @param yearLabel    the year label
     * @param keys         the keys associated with the year
     * @param firstCourse  the first course
     * @param secondCourse the second course
     * @param classified   the classified data
     */
    private void addYearRow(final HtmlBuilder csv, final String yearLabel, final List<Integer> keys,
                            final String firstCourse, final String secondCourse, final ClassifiedData classified) {

        int localACount = 0;
        int localBCount = 0;
        int localCCount = 0;
        int transferACount = 0;
        int transferBCount = 0;
        int transferCCount = 0;
        int apCount = 0;

        int localAPass = 0;
        int localBPass = 0;
        int localCPass = 0;
        int transferAPass = 0;
        int transferBPass = 0;
        int transferCPass = 0;
        int apPass = 0;

        double localAGrade = 0.0;
        double localBGrade = 0.0;
        double localCGrade = 0.0;
        double localTotalGrade = 0.0;
        double transferAGrade = 0.0;
        double transferBGrade = 0.0;
        double transferCGrade = 0.0;
        double transferTotalGrade = 0.0;
        double apGrade = 0.0;

        for (final Integer key : keys) {
            final List<EnrollmentRec> localAList = classified.localA().get(key);
            if (localAList != null) {
                localACount += localAList.size();

                for (final EnrollmentRec rec : localAList) {
                    if (rec.gradeValue() != null) {
                        localAGrade += rec.gradeValue().doubleValue();
                        localTotalGrade += rec.gradeValue().doubleValue();
                    }
                    if (rec.isPassed()) {
                        ++localAPass;
                    }
                }
            }

            final List<EnrollmentRec> localBList = classified.localB().get(key);
            if (localBList != null) {
                localBCount += localBList.size();

                for (final EnrollmentRec rec : localBList) {
                    if (rec.gradeValue() != null) {
                        localBGrade += rec.gradeValue().doubleValue();
                        localTotalGrade += rec.gradeValue().doubleValue();
                    }
                    if (rec.isPassed()) {
                        ++localBPass;
                    }
                }
            }

            final List<EnrollmentRec> localCList = classified.localCD().get(key);
            if (localCList != null) {
                localCCount += localCList.size();

                for (final EnrollmentRec rec : localCList) {
                    if (rec.gradeValue() != null) {
                        localCGrade += rec.gradeValue().doubleValue();
                        localTotalGrade += rec.gradeValue().doubleValue();
                    }
                    if (rec.isPassed()) {
                        ++localCPass;
                    }
                }
            }

            final List<EnrollmentRec> transferAList = classified.transferA().get(key);
            if (transferAList != null) {
                transferACount += transferAList.size();

                for (final EnrollmentRec rec : transferAList) {
                    if (rec.gradeValue() != null) {
                        transferAGrade += rec.gradeValue().doubleValue();
                        transferTotalGrade += rec.gradeValue().doubleValue();
                    }
                    if (rec.isPassed()) {
                        ++transferAPass;
                    }
                }
            }

            final List<EnrollmentRec> transferBList = classified.transferB().get(key);
            if (transferBList != null) {
                transferBCount += transferBList.size();

                for (final EnrollmentRec rec : transferBList) {
                    if (rec.gradeValue() != null) {
                        transferBGrade += rec.gradeValue().doubleValue();
                        transferTotalGrade += rec.gradeValue().doubleValue();
                    }
                    if (rec.isPassed()) {
                        ++transferBPass;
                    }
                }
            }

            final List<EnrollmentRec> transferCList = classified.transferCD().get(key);
            if (transferCList != null) {
                transferCCount += transferCList.size();

                for (final EnrollmentRec rec : transferCList) {
                    if (rec.gradeValue() != null) {
                        transferCGrade += rec.gradeValue().doubleValue();
                        transferTotalGrade += rec.gradeValue().doubleValue();
                    }
                    if (rec.isPassed()) {
                        ++transferCPass;
                    }
                }
            }

            final List<EnrollmentRec> apList = classified.ap().get(key);
            if (apList != null) {
                apCount += apList.size();

                for (final EnrollmentRec rec : apList) {
                    if (rec.gradeValue() != null) {
                        apGrade += rec.gradeValue().doubleValue();
                    }
                    if (rec.isPassed()) {
                        ++apPass;
                    }
                }
            }
        }

        final int localCount = localACount + localBCount + localCCount;
        final int transferCount = transferACount + transferBCount + transferCCount;

        final String localACountStr = Integer.toString(localACount);
        final String localBCountStr = Integer.toString(localBCount);
        final String localCCountStr = Integer.toString(localCCount);
        final String localCountStr = Integer.toString(localCount);
        final String transferACountStr = Integer.toString(transferACount);
        final String transferBCountStr = Integer.toString(transferBCount);
        final String transferCCountStr = Integer.toString(transferCCount);
        final String transferCountStr = Integer.toString(transferCount);
        final String apCountStr = Integer.toString(apCount);

        if (localAPass > 0) {
            localAGrade = localAGrade / (double) localAPass;
        }
        if (localBPass > 0) {
            localBGrade = localBGrade / (double) localBPass;
        }
        if (localCPass > 0) {
            localCGrade = localCGrade / (double) localCPass;
        }

        if (transferAPass > 0) {
            transferAGrade = transferAGrade / (double) transferAPass;
        }
        if (transferBPass > 0) {
            transferBGrade = transferBGrade / (double) transferBPass;
        }
        if (transferCPass > 0) {
            transferCGrade = transferCGrade / (double) transferCPass;
        }

        if (apPass > 0) {
            apGrade = apGrade / (double) apPass;
        }

        final int localPass = localAPass + localBPass + localCPass;
        if (localPass > 0) {
            localTotalGrade = localTotalGrade / (double) (localPass);
        }

        final int transferPass = transferAPass + transferBPass + transferCPass;
        if (transferPass > 0) {
            transferTotalGrade = transferTotalGrade / (double) (transferPass);
        }

        final double passRateWithLocalA = 100.0 * (double) localAPass / (double) localACount;
        final String passRateWithLocalAStr = localACount == 0 ? CoreConstants.EMPTY : fmt(passRateWithLocalA);
        final double passRateWithLocalB = 100.0 * (double) localBPass / (double) localBCount;
        final String passRateWithLocalBStr = localBCount == 0 ? CoreConstants.EMPTY : fmt(passRateWithLocalB);
        final double passRateWithLocalC = 100.0 * (double) localCPass / (double) localCCount;
        final String passRateWithLocalCStr = localCCount == 0 ? CoreConstants.EMPTY : fmt(passRateWithLocalC);
        final double passRateWithLocal = 100.0 * (double) (localPass) / (double) localCount;
        final String passRateWithLocalStr = localCount == 0 ? CoreConstants.EMPTY : fmt(passRateWithLocal);

        final double passRateWithTransferA = 100.0 * (double) transferAPass / (double) transferACount;
        final String passRateWithTransferAStr = transferACount == 0 ? CoreConstants.EMPTY : fmt(passRateWithTransferA);
        final double passRateWithTransferB = 100.0 * (double) transferBPass / (double) transferBCount;
        final String passRateWithTransferBStr = transferBCount == 0 ? CoreConstants.EMPTY : fmt(passRateWithTransferB);
        final double passRateWithTransferC = 100.0 * (double) transferCPass / (double) transferCCount;
        final String passRateWithTransferCStr = transferCCount == 0 ? CoreConstants.EMPTY : fmt(passRateWithTransferC);
        final double passRateWithTransfer = 100.0 * (double) (transferPass) / (double) transferCount;
        final String passRateWithTransferStr = transferCount == 0 ? CoreConstants.EMPTY : fmt(passRateWithTransfer);

        final double passRateWithAp = 100.0 * (double) apPass / (double) apCount;
        final String passRateWithApStr = apCount == 0 ? CoreConstants.EMPTY : fmt(passRateWithAp);

        final String localAGradeStr = localACount == 0 ? CoreConstants.EMPTY : fmt(localAGrade);
        final String localBGradeStr = localBCount == 0 ? CoreConstants.EMPTY : fmt(localBGrade);
        final String localCGradeStr = localCCount == 0 ? CoreConstants.EMPTY : fmt(localCGrade);
        final String localTotalGradeStr = localCount == 0 ? CoreConstants.EMPTY : fmt(localTotalGrade);

        final String transferAGradeStr = transferACount == 0 ? CoreConstants.EMPTY : fmt(transferAGrade);
        final String transferBGradeStr = transferBCount == 0 ? CoreConstants.EMPTY : fmt(transferBGrade);
        final String transferCGradeStr = transferCCount == 0 ? CoreConstants.EMPTY : fmt(transferCGrade);
        final String transferTotalGradeStr = transferCount == 0 ? CoreConstants.EMPTY : fmt(transferTotalGrade);

        final String apGradeStr = apCount == 0 ? CoreConstants.EMPTY : fmt(apGrade);

        csv.addln(yearLabel,
                ",", localACountStr, ",", passRateWithLocalAStr, ",", localAGradeStr,
                ",", localBCountStr, ",", passRateWithLocalBStr, ",", localBGradeStr,
                ",", localCCountStr, ",", passRateWithLocalCStr, ",", localCGradeStr,
                ",", localCountStr, ",", passRateWithLocalStr, ",", localTotalGradeStr,
                ",", transferACountStr, ",", passRateWithTransferAStr, ",", transferAGradeStr,
                ",", transferBCountStr, ",", passRateWithTransferBStr, ",", transferBGradeStr,
                ",", transferCCountStr, ",", passRateWithTransferCStr, ",", transferCGradeStr,
                ",", transferCountStr, ",", passRateWithTransferStr, ",", transferTotalGradeStr,
                ",", apCountStr, ",", passRateWithApStr, ",", apGradeStr);
    }

    /**
     * Formats a floating-point number.
     *
     * @param number the number to format
     * @return the formatted number
     */
    private String fmt(final double number) {

        return Double.isNaN(number) ? CoreConstants.EMPTY : this.format.format(number);
    }
}
