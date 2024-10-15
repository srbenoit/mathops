package dev.mathops.dbjobs.report.analytics.longitudinal;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.log.Log;

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
    private static final String IND8 = "        ";

    /** A commonly-used string. */
    private static final String AFTER = " after ";

    /** A commonly-used string. */
    private static final String IN = " in ";

    /** A commonly-used string. */
    private static final String PCT_PASSED = "% passed ";

    /** A commonly-used string. */
    private static final String AVG_GRADE_OF = "Average grade of ";

    /** A commonly-used string. */
    private static final String PASSED = " passed ";

    /** A commonly-used string. */
    private static final String TRANSFERRED = " transferred ";

    /** A commonly-used string. */
    private static final String AT_CSU_WITH_A = " at CSU with an A";

    /** A commonly-used string. */
    private static final String AT_CSU_WITH_B = " at CSU with a B";

    /** A commonly-used string. */
    private static final String AT_CSU_WITH_C = " at CSU with a C or D";

    /** A commonly-used string. */
    private static final String AT_CSU_WITH_ANY = " at CSU (any grade)";

    /** A commonly-used string. */
    private static final String TRANSFER_A = " transfer with an A";

    /** A commonly-used string. */
    private static final String TRANSFER_B = " transfer with a B";

    /** A commonly-used string. */
    private static final String TRANSFER_C = " transfer with an C or D";

    /** A commonly-used string. */
    private static final String TRANSFER_ANY = " transfer (any grade)";

    /** A commonly-used string. */
    private static final String VIA_AP = " via AP/IB/CLEP";

    /** A commonly-used string. */
    private static final String CSV_HEADER = SimpleBuilder.concat(
            "Year,Term,N,Pass %,Avg. Grade,N,Pass %,Avg. Grade,N,Pass %,Avg. Grade,N,Pass %,Avg. Grade,N,Pass %,",
            "Avg. Grade,N,Pass %,Avg. Grade,N,Pass %,Avg. Grade,N,Pass %,Avg. Grade,N,Pass %,Avg. Grade");

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

    /** The initial size for lists. */
    private static final int INIT_SIZE = 20;

    /** A decimal formatter. */
    private final DecimalFormat format;

    /** The directory in which to write CSV files. */
    private final File targetDir;

    /** The ordered set of terms represented. */
    private final Collection<Integer> terms;

    private final ClassifiedData priorTerm;

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
     * Analysis of success in a second course based on how student completed a first course.
     *
     * @param earliestSecondCourseTerm the earliest term for which to look for the second course
     * @param records                  the set of student course records
     * @param firstCourse              the course ID of the first course in the sequence
     * @param firstCourseSections      the list of sections of interest in the first course
     * @param secondCourse             the course ID of the second course in the sequence
     * @param secondCourseSections     the list of sections of interest in the second course
     * @param report                   the report
     */
    void generateReport(final int earliestSecondCourseTerm,
                        final Map<String, List<StudentCourseRecord>> records, final String firstCourse,
                        final Collection<String> firstCourseSections, final String secondCourse,
                        final Collection<String> secondCourseSections, final HtmlBuilder report) {

        report.addln();
        report.addln("Analysis of outcomes in ", secondCourse, " with respect to ", firstCourse);

        gatherData(earliestSecondCourseTerm, records, firstCourse, firstCourseSections, secondCourse,
                secondCourseSections);

        final String numWithSecondStr = Integer.toString(this.numWithSecond);
        final String numWithFirstPriorStr = Integer.toString(this.numWithFirstPrior);
        final String numWithFirstAnyStr = Integer.toString(this.numWithFirstAny);

        report.addln("    Found ", numWithSecondStr, " students who took ", secondCourse, " at CSU");
        report.addln("    ", numWithFirstPriorStr, " had credit for ", firstCourse, " the prior term");
        report.addln("    ", numWithFirstAnyStr, " had credit for ", firstCourse, " in any earlier term");

        final HtmlBuilder csv = new HtmlBuilder(10000);

        final String header2 = SimpleBuilder.concat(firstCourse, " at CSU (A),,,", firstCourse, " at CSU (B),,,",
                firstCourse, " at CSU (C/D),,,", firstCourse, " at CSU (ALL),,,", firstCourse, " transfer (A),,,",
                firstCourse, " transfer (B),,,", firstCourse, " transfer (C/D),,,", firstCourse, " transfer (ALL),,,",
                firstCourse, " via AP/IB/CLEP");

        csv.addln("Pass rates in:,", secondCourse);
        csv.addln("As function of:,", firstCourse);
        csv.addln("Total students with ", secondCourse, ":,", numWithSecondStr);
        csv.addln("Subset with ", firstCourse, " in prior term:,", numWithFirstPriorStr);
        csv.addln("Subset with ", firstCourse, " in any earlier term:,", numWithFirstAnyStr);
        csv.addln();

        csv.addln("Reports for individual terms [students with credit in ", firstCourse, " in the prior term]");
        csv.addln();

        csv.addln("Fall Semesters,,", header2);
        generateOneYearRows(FALL_TERM, report, csv, firstCourse, secondCourse, this.priorTerm);
        csv.addln("Spring Semesters,,", header2);
        generateOneYearRows(SPRING_TERM, report, csv, firstCourse, secondCourse, this.priorTerm);
        csv.addln("Summer Semesters,,", header2);
        generateOneYearRows(SUMMER_TERM, report, csv, firstCourse, secondCourse, this.priorTerm);

        csv.addln("Reports based on average over last three terms [students with credit in ", firstCourse,
                " in the prior term]");
        csv.addln();

        csv.addln("Fall Semesters,,", header2);
        generateThreeYearRows(FALL_TERM, report, csv, firstCourse, secondCourse, this.priorTerm);
        csv.addln("Spring Semesters,,", header2);
        generateThreeYearRows(SPRING_TERM, report, csv, firstCourse, secondCourse, this.priorTerm);
        csv.addln("Summer Semesters,,", header2);
        generateThreeYearRows(SUMMER_TERM, report, csv, firstCourse, secondCourse, this.priorTerm);

        csv.addln("Reports for individual terms [students with credit in ", firstCourse, " in the any earlier term]");
        csv.addln();

        csv.addln("Fall Semesters,,", header2);
        generateOneYearRows(FALL_TERM, report, csv, firstCourse, secondCourse, this.allEarlierTerms);
        csv.addln("Spring Semesters,,", header2);
        generateOneYearRows(SPRING_TERM, report, csv, firstCourse, secondCourse, this.allEarlierTerms);
        csv.addln("Summer Semesters,,", header2);
        generateOneYearRows(SUMMER_TERM, report, csv, firstCourse, secondCourse, this.allEarlierTerms);

        csv.addln("Reports based on average over last three terms [students with credit in ", firstCourse,
                " in any earlier term]");
        csv.addln();

        csv.addln("Fall Semesters,,", header2);
        generateThreeYearRows(FALL_TERM, report, csv, firstCourse, secondCourse, this.allEarlierTerms);
        csv.addln("Spring Semesters,,", header2);
        generateThreeYearRows(SPRING_TERM, report, csv, firstCourse, secondCourse, this.allEarlierTerms);
        csv.addln("Summer Semesters,,", header2);
        generateThreeYearRows(SUMMER_TERM, report, csv, firstCourse, secondCourse, this.allEarlierTerms);

        final String filename = SimpleBuilder.concat("Sequence_", firstCourse, "_", secondCourse, ".csv");
        final String csvString = csv.toString();

        final File output = new File(this.targetDir, filename);
        try (final FileWriter writer = new FileWriter(output, StandardCharsets.UTF_8)) {
            writer.write(csvString);
        } catch (final IOException ex) {
            Log.warning(ex);
        }

        report.addln();
    }

    /**
     * Generates one-year data rows for a term, first/second course combination, and list of classified data.
     *
     * @param termCode     the term code
     * @param report       the report builder to which to write
     * @param csv          the CSV file builder to which to write
     * @param firstCourse  the first course code
     * @param secondCourse the second course code
     * @param classified   the classified data
     */
    private void generateOneYearRows(final int termCode, final HtmlBuilder report, final HtmlBuilder csv,
                                     final String firstCourse, final String secondCourse,
                                     final ClassifiedData classified) {

        csv.addln(CSV_HEADER);

        for (final Integer key : this.terms) {
            final int value = key.intValue();
            final int code = value % 100;
            if (code == termCode) {
                addOneYearRow(report, csv, key, firstCourse, secondCourse, classified);
            }
        }

        csv.addln();
    }

    /**
     * Generates three-year-average data rows for a term, first/second course combination, and list of classified data.
     *
     * @param termCode     the term code
     * @param report       the report builder to which to write
     * @param csv          the CSV file builder to which to write
     * @param firstCourse  the first course code
     * @param secondCourse the second course code
     * @param classified   the classified data
     */
    private void generateThreeYearRows(final int termCode, final HtmlBuilder report, final HtmlBuilder csv,
                                       final String firstCourse, final String secondCourse,
                                       final ClassifiedData classified) {

        csv.addln(CSV_HEADER);

        Integer key1 = null;
        Integer key2 = null;
        for (final Integer key : this.terms) {
            final int value = key.intValue();
            final int code = value % 100;
            if (code == termCode) {
                if (key1 != null) {
                    addThreeYearRow(report, csv, key1, key2, key, firstCourse, secondCourse, classified);
                }
                key1 = key2;
                key2 = key;
            }
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
    private void gatherData(final int earliestSecondCourseTerm, final Map<String, List<StudentCourseRecord>> records,
                            final String firstCourse, final Collection<String> firstCourseSections,
                            final String secondCourse, final Collection<String> secondCourseSections) {

        this.terms.clear();
        this.priorTerm.clear();
        this.allEarlierTerms.clear();

        this.numWithSecond = 0;
        this.numWithFirstPrior = 0;
        this.numWithFirstAny = 0;

        // Find all students who took the second course locally (one of the sections of interest)
        for (final Map.Entry<String, List<StudentCourseRecord>> entry : records.entrySet()) {
            final List<StudentCourseRecord> list = entry.getValue();

            final StudentCourseRecord earliestSecond = findEarliestSecond(earliestSecondCourseTerm, list,
                    secondCourse, secondCourseSections);

            if (earliestSecond != null) {
                ++this.numWithSecond;

                final StudentCourseRecord latestFirst = findLatestFirstBeforeSecond(earliestSecond, list,
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

                        final Map<Integer, List<StudentCourseRecord>> targetPrior = selectTargetMap(latestFirst,
                                this.priorTerm);

                        if (targetPrior == null) {
                            Log.warning("Unable to identify target prior-term map for ", latestFirst);
                        } else {
                            final List<StudentCourseRecord> targetList = targetPrior.get(key);
                            targetList.add(earliestSecond);
                        }
                    }

                    final Map<Integer, List<StudentCourseRecord>> targetAny = selectTargetMap(latestFirst,
                            this.allEarlierTerms);

                    if (targetAny == null) {
                        Log.warning("Unable to identify target any-term map for ", latestFirst);
                    } else {
                        final List<StudentCourseRecord> targetList = targetAny.get(key);
                        targetList.add(earliestSecond);
                    }
                }
            }
        }
    }

    /**
     * Tests whether the first course was taken in a "prior" term to the second course.
     *
     * @param first  the first course
     * @param second the second course
     * @return true if the first course was taken in the prior term to the second course
     */
    private static boolean isPriorTerm(final StudentCourseRecord first, final StudentCourseRecord second) {

        boolean isPrior = false;

        final int firstTerm = first.academicPeriod();
        final int secondTerm = second.academicPeriod();

        final int firstYear = firstTerm / 100;
        final int secondYear = secondTerm / 100;

        if (firstTerm == secondTerm) {
            // We treat the same term as "prior" since AP or transfer credit could get booked then.
            isPrior = true;
        } else if (firstYear - secondYear <= 1) {
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
    private static StudentCourseRecord findEarliestSecond(final int earliestSecondCourseTerm,
                                                          final Iterable<StudentCourseRecord> list,
                                                          final String secondCourse,
                                                          final Collection<String> secondCourseSections) {

        StudentCourseRecord earliestSecond = null;

        for (final StudentCourseRecord rec : list) {
            final int term = rec.academicPeriod();

            if (term >= earliestSecondCourseTerm) {
                final String course = rec.course();
                final String sect = rec.section();

                if (secondCourse.equals(course) && !rec.transfer() && secondCourseSections.contains(sect)) {
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
    private StudentCourseRecord findLatestFirstBeforeSecond(final StudentCourseRecord earliestSecond,
                                                            final Iterable<StudentCourseRecord> list,
                                                            final String firstCourse,
                                                            final Collection<String> firstCourseSections) {

        // Identify the year and term when the second course was taken
        final int secondTerm = earliestSecond.academicPeriod();

        StudentCourseRecord latestFirst = null;

        // Scan the list for the first course, tracking the latest found.
        for (final StudentCourseRecord rec : list) {
            final String course = rec.course();
            final int term = rec.academicPeriod();
            final String sect = rec.section();

            if (firstCourse.equals(course)) {
                if (rec.apIbClep()) {
                    if (term <= secondTerm) {
                        latestFirst = chooseLatest(latestFirst, rec);
                    }
                } else if (rec.transfer()) {
                    if (rec.gradeValue() != null && term <= secondTerm) {
                        latestFirst = chooseLatest(latestFirst, rec);
                    }
                } else if (rec.gradeValue() != null && term < secondTerm && firstCourseSections.contains(sect)) {
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
    private static StudentCourseRecord chooseLatest(final StudentCourseRecord test1, final StudentCourseRecord test2) {

        final StudentCourseRecord result;

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

                final boolean ap1 = test1.apIbClep();
                final boolean ap2 = test2.apIbClep();

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
                    final boolean transfer1 = test1.transfer();
                    final boolean transfer2 = test2.transfer();

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
    private Map<Integer, List<StudentCourseRecord>> selectTargetMap(final StudentCourseRecord precedingFirst,
                                                                    final ClassifiedData classified) {

        Map<Integer, List<StudentCourseRecord>> target = null;

        if (precedingFirst.apIbClep()) {
            target = classified.ap();
        } else if (precedingFirst.transfer()) {

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
     * Adds a single row to the report, and a corresponding row to the CSV file.  Each row represents performance in a
     * single term.
     *
     * @param report       the report
     * @param csv          the CSV
     * @param key          the term key
     * @param firstCourse  the first course
     * @param secondCourse the second course
     * @param classified   the classified data
     */
    private void addOneYearRow(final HtmlBuilder report, final HtmlBuilder csv, final Integer key,
                               final String firstCourse, final String secondCourse,
                               final ClassifiedData classified) {

        final List<StudentCourseRecord> localAList = classified.localA().get(key);
        final int localACount = localAList.size();
        final String localACountStr = Integer.toString(localACount);

        final List<StudentCourseRecord> localBList = classified.localB().get(key);
        final int localBCount = localBList.size();
        final String localBCountStr = Integer.toString(localBCount);

        final List<StudentCourseRecord> localCList = classified.localCD().get(key);
        final int localCCount = localCList.size();
        final String localCCountStr = Integer.toString(localCCount);

        final int localCount = localACount + localBCount + localCCount;
        final String localCountStr = Integer.toString(localCount);

        final List<StudentCourseRecord> transferAList = classified.transferA().get(key);
        final int transferACount = transferAList.size();
        final String transferACountStr = Integer.toString(transferACount);

        final List<StudentCourseRecord> transferBList = classified.transferB().get(key);
        final int transferBCount = transferBList.size();
        final String transferBCountStr = Integer.toString(transferBCount);

        final List<StudentCourseRecord> transferCList = classified.transferCD().get(key);
        final int transferCCount = transferCList.size();
        final String transferCCountStr = Integer.toString(transferCCount);

        final int transferCount = transferACount + transferBCount + transferCCount;
        final String transferCountStr = Integer.toString(transferCount);

        final List<StudentCourseRecord> apList = classified.ap().get(key);
        final int apCount = apList.size();
        final String apCountStr = Integer.toString(apCount);

        report.addln();
        report.addln("    ", key, ":");
        report.addln();
        report.addln(IND8, localACountStr, PASSED, firstCourse, AT_CSU_WITH_A);
        report.addln(IND8, localBCountStr, PASSED, firstCourse, AT_CSU_WITH_B);
        report.addln(IND8, localCCountStr, PASSED, firstCourse, AT_CSU_WITH_C);
        report.addln(IND8, transferACountStr, TRANSFERRED, firstCourse, " in with an A");
        report.addln(IND8, transferBCountStr, TRANSFERRED, firstCourse, " in with a B");
        report.addln(IND8, transferCCountStr, TRANSFERRED, firstCourse, " in with a C or D");
        report.addln(IND8, apCountStr, " had AP/IB/CLEP credit for ", firstCourse);

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

        if (localAList != null) {
            for (final StudentCourseRecord rec : localAList) {
                if (rec.gradeValue() != null) {
                    localAGrade += rec.gradeValue().doubleValue();
                    localTotalGrade += rec.gradeValue().doubleValue();
                }
                if (rec.passed()) {
                    ++localAPass;
                }
            }
        }
        if (localBList != null) {
            for (final StudentCourseRecord rec : localBList) {
                if (rec.gradeValue() != null) {
                    localBGrade += rec.gradeValue().doubleValue();
                    localTotalGrade += rec.gradeValue().doubleValue();
                }
                if (rec.passed()) {
                    ++localBPass;
                }
            }
        }
        if (localCList != null) {
            for (final StudentCourseRecord rec : localCList) {
                if (rec.gradeValue() != null) {
                    localCGrade += rec.gradeValue().doubleValue();
                    localTotalGrade += rec.gradeValue().doubleValue();
                }
                if (rec.passed()) {
                    ++localCPass;
                }
            }
        }
        if (transferAList != null) {
            for (final StudentCourseRecord rec : transferAList) {
                if (rec.gradeValue() != null) {
                    transferAGrade += rec.gradeValue().doubleValue();
                    transferTotalGrade += rec.gradeValue().doubleValue();
                }
                if (rec.passed()) {
                    ++transferAPass;
                }
            }
        }
        if (transferBList != null) {
            for (final StudentCourseRecord rec : transferBList) {
                if (rec.gradeValue() != null) {
                    transferBGrade += rec.gradeValue().doubleValue();
                    transferTotalGrade += rec.gradeValue().doubleValue();
                }
                if (rec.passed()) {
                    ++transferBPass;
                }
            }
        }
        if (transferCList != null) {
            for (final StudentCourseRecord rec : transferCList) {
                if (rec.gradeValue() != null) {
                    transferCGrade += rec.gradeValue().doubleValue();
                    transferTotalGrade += rec.gradeValue().doubleValue();
                }
                if (rec.passed()) {
                    ++transferCPass;
                }
            }
        }
        if (apList != null) {
            for (final StudentCourseRecord rec : apList) {
                if (rec.gradeValue() != null) {
                    apGrade += rec.gradeValue().doubleValue();
                }
                if (rec.passed()) {
                    ++apPass;
                }
            }
        }

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
        final String passRateWithLocalAStr = fmt(passRateWithLocalA);
        final double passRateWithLocalB = 100.0 * (double) localBPass / (double) localBCount;
        final String passRateWithLocalBStr = fmt(passRateWithLocalB);
        final double passRateWithLocalC = 100.0 * (double) localCPass / (double) localCCount;
        final String passRateWithLocalCStr = fmt(passRateWithLocalC);
        final double passRateWithLocal = 100.0 * (double) (localPass) / (double) localCount;
        final String passRateWithLocalStr = fmt(passRateWithLocal);

        final double passRateWithTransferA = 100.0 * (double) transferAPass / (double) transferACount;
        final String passRateWithTransferAStr = fmt(passRateWithTransferA);
        final double passRateWithTransferB = 100.0 * (double) transferBPass / (double) transferBCount;
        final String passRateWithTransferBStr = fmt(passRateWithTransferB);
        final double passRateWithTransferC = 100.0 * (double) transferCPass / (double) transferCCount;
        final String passRateWithTransferCStr = fmt(passRateWithTransferC);
        final double passRateWithTransfer = 100.0 * (double) (transferPass) / (double) transferCount;
        final String passRateWithTransferStr = fmt(passRateWithTransfer);

        final double passRateWithAp = 100.0 * (double) apPass / (double) apCount;
        final String passRateWithApStr = fmt(passRateWithAp);

        final String localAGradeStr = fmt(localAGrade);
        final String localBGradeStr = fmt(localBGrade);
        final String localCGradeStr = fmt(localCGrade);
        final String localTotalGradeStr = fmt(localTotalGrade);

        final String transferAGradeStr = fmt(transferAGrade);
        final String transferBGradeStr = fmt(transferBGrade);
        final String transferCGradeStr = fmt(transferCGrade);
        final String transferTotalGradeStr = fmt(transferTotalGrade);

        final String apGradeStr = fmt(apGrade);

        report.addln();
        report.addln(IND8, passRateWithLocalAStr, PCT_PASSED, secondCourse, AFTER, firstCourse, AT_CSU_WITH_A);
        report.addln(IND8, passRateWithLocalBStr, PCT_PASSED, secondCourse, AFTER, firstCourse, AT_CSU_WITH_B);
        report.addln(IND8, passRateWithLocalCStr, PCT_PASSED, secondCourse, AFTER, firstCourse, AT_CSU_WITH_C);
        report.addln(IND8, passRateWithLocalStr, PCT_PASSED, secondCourse, AFTER, firstCourse, AT_CSU_WITH_ANY);
        report.addln(IND8, passRateWithTransferAStr, PCT_PASSED, secondCourse, AFTER, firstCourse, TRANSFER_A);
        report.addln(IND8, passRateWithTransferBStr, PCT_PASSED, secondCourse, AFTER, firstCourse, TRANSFER_B);
        report.addln(IND8, passRateWithTransferCStr, PCT_PASSED, secondCourse, AFTER, firstCourse, TRANSFER_C);
        report.addln(IND8, passRateWithTransferStr, PCT_PASSED, secondCourse, AFTER, firstCourse, TRANSFER_ANY);
        report.addln(IND8, passRateWithApStr, PCT_PASSED, secondCourse, AFTER, firstCourse, VIA_AP);

        report.addln();
        report.addln(IND8, AVG_GRADE_OF, localAGradeStr, IN, secondCourse, AFTER, firstCourse, AT_CSU_WITH_A);
        report.addln(IND8, AVG_GRADE_OF, localBGradeStr, IN, secondCourse, AFTER, firstCourse, AT_CSU_WITH_B);
        report.addln(IND8, AVG_GRADE_OF, localCGradeStr, IN, secondCourse, AFTER, firstCourse, AT_CSU_WITH_C);
        report.addln(IND8, AVG_GRADE_OF, localTotalGradeStr, IN, secondCourse, AFTER, firstCourse, AT_CSU_WITH_ANY);
        report.addln(IND8, AVG_GRADE_OF, transferAGradeStr, IN, secondCourse, AFTER, firstCourse, TRANSFER_A);
        report.addln(IND8, AVG_GRADE_OF, transferBGradeStr, IN, secondCourse, AFTER, firstCourse, TRANSFER_B);
        report.addln(IND8, AVG_GRADE_OF, transferCGradeStr, IN, secondCourse, AFTER, firstCourse, TRANSFER_C);
        report.addln(IND8, AVG_GRADE_OF, transferTotalGradeStr, IN, secondCourse, AFTER, firstCourse, TRANSFER_ANY);
        report.addln(IND8, AVG_GRADE_OF, apGradeStr, IN, secondCourse, AFTER, firstCourse, VIA_AP);

        final int keyValue = key.intValue();
        final String year = Integer.toString(keyValue / 100);

        final int termValue = keyValue % 100;
        final String term = termValue == SPRING_TERM ? "Spring" : (termValue == FALL_TERM ? "Fall" :
                (termValue == SUMMER_TERM ? "Summer" : Integer.toString(termValue)));

        csv.addln(year, ",", term,
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
     * Adds a single row to the report, and a corresponding row to the CSV file.  Each row represents performance in a
     * single term.
     *
     * @param report       the report
     * @param csv          the CSV
     * @param key1         the first term key
     * @param key2         the second term key
     * @param key3         the third term key
     * @param firstCourse  the first course
     * @param secondCourse the second course
     * @param classified   the classified data
     */
    private void addThreeYearRow(final HtmlBuilder report, final HtmlBuilder csv, final Integer key1,
                                 final Integer key2, final Integer key3, final String firstCourse,
                                 final String secondCourse, final ClassifiedData classified) {

        final List<StudentCourseRecord> localAList = new ArrayList<>(100);
        localAList.addAll(classified.localA().get(key1));
        localAList.addAll(classified.localA().get(key2));
        localAList.addAll(classified.localA().get(key3));
        final int localACount = localAList.size();
        final String localACountStr = Integer.toString(localACount);

        final List<StudentCourseRecord> localBList = new ArrayList<>(100);
        localBList.addAll(classified.localB().get(key1));
        localBList.addAll(classified.localB().get(key2));
        localBList.addAll(classified.localB().get(key3));
        final int localBCount = localBList.size();
        final String localBCountStr = Integer.toString(localBCount);

        final List<StudentCourseRecord> localCList = new ArrayList<>(100);
        localCList.addAll(classified.localCD().get(key1));
        localCList.addAll(classified.localCD().get(key2));
        localCList.addAll(classified.localCD().get(key3));
        final int localCCount = localCList.size();
        final String localCCountStr = Integer.toString(localCCount);

        final int localCount = localACount + localBCount + localCCount;
        final String localCountStr = Integer.toString(localCount);

        final List<StudentCourseRecord> transferAList = new ArrayList<>(100);
        transferAList.addAll(classified.transferA().get(key1));
        transferAList.addAll(classified.transferA().get(key2));
        transferAList.addAll(classified.transferA().get(key3));
        final int transferACount = transferAList.size();
        final String transferACountStr = Integer.toString(transferACount);

        final List<StudentCourseRecord> transferBList = new ArrayList<>(100);
        transferBList.addAll(classified.transferB().get(key1));
        transferBList.addAll(classified.transferB().get(key2));
        transferBList.addAll(classified.transferB().get(key3));
        final int transferBCount = transferBList.size();
        final String transferBCountStr = Integer.toString(transferBCount);

        final List<StudentCourseRecord> transferCList = new ArrayList<>(100);
        transferCList.addAll(classified.transferCD().get(key1));
        transferCList.addAll(classified.transferCD().get(key2));
        transferCList.addAll(classified.transferCD().get(key3));
        final int transferCCount = transferCList.size();
        final String transferCCountStr = Integer.toString(transferCCount);

        final int transferCount = transferACount + transferBCount + transferCCount;
        final String transferCountStr = Integer.toString(transferCount);

        final List<StudentCourseRecord> apList = new ArrayList<>(100);
        apList.addAll(classified.ap().get(key1));
        apList.addAll(classified.ap().get(key2));
        apList.addAll(classified.ap().get(key3));
        final int apCount = apList.size();
        final String apCountStr = Integer.toString(apCount);

        report.addln();
        report.addln("    ", key1, "/", key2, ",", key3, ":");
        report.addln();
        report.addln(IND8, localACountStr, PASSED, firstCourse, AT_CSU_WITH_A);
        report.addln(IND8, localBCountStr, PASSED, firstCourse, AT_CSU_WITH_B);
        report.addln(IND8, localCCountStr, PASSED, firstCourse, AT_CSU_WITH_C);
        report.addln(IND8, transferACountStr, TRANSFERRED, firstCourse, " in with an A");
        report.addln(IND8, transferBCountStr, TRANSFERRED, firstCourse, " in with a B");
        report.addln(IND8, transferCCountStr, TRANSFERRED, firstCourse, " in with a C or D");
        report.addln(IND8, apCountStr, " had AP/IB/CLEP credit for ", firstCourse);

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

        if (localAList != null) {
            for (final StudentCourseRecord rec : localAList) {
                if (rec.gradeValue() != null) {
                    localAGrade += rec.gradeValue().doubleValue();
                    localTotalGrade += rec.gradeValue().doubleValue();
                }
                if (rec.passed()) {
                    ++localAPass;
                }
            }
        }
        if (localBList != null) {
            for (final StudentCourseRecord rec : localBList) {
                if (rec.gradeValue() != null) {
                    localBGrade += rec.gradeValue().doubleValue();
                    localTotalGrade += rec.gradeValue().doubleValue();
                }
                if (rec.passed()) {
                    ++localBPass;
                }
            }
        }
        if (localCList != null) {
            for (final StudentCourseRecord rec : localCList) {
                if (rec.gradeValue() != null) {
                    localCGrade += rec.gradeValue().doubleValue();
                    localTotalGrade += rec.gradeValue().doubleValue();
                }
                if (rec.passed()) {
                    ++localCPass;
                }
            }
        }
        if (transferAList != null) {
            for (final StudentCourseRecord rec : transferAList) {
                if (rec.gradeValue() != null) {
                    transferAGrade += rec.gradeValue().doubleValue();
                    transferTotalGrade += rec.gradeValue().doubleValue();
                }
                if (rec.passed()) {
                    ++transferAPass;
                }
            }
        }
        if (transferBList != null) {
            for (final StudentCourseRecord rec : transferBList) {
                if (rec.gradeValue() != null) {
                    transferBGrade += rec.gradeValue().doubleValue();
                    transferTotalGrade += rec.gradeValue().doubleValue();
                }
                if (rec.passed()) {
                    ++transferBPass;
                }
            }
        }
        if (transferCList != null) {
            for (final StudentCourseRecord rec : transferCList) {
                if (rec.gradeValue() != null) {
                    transferCGrade += rec.gradeValue().doubleValue();
                    transferTotalGrade += rec.gradeValue().doubleValue();
                }
                if (rec.passed()) {
                    ++transferCPass;
                }
            }
        }
        if (apList != null) {
            for (final StudentCourseRecord rec : apList) {
                if (rec.gradeValue() != null) {
                    apGrade += rec.gradeValue().doubleValue();
                }
                if (rec.passed()) {
                    ++apPass;
                }
            }
        }

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
        final String passRateWithLocalAStr = fmt(passRateWithLocalA);
        final double passRateWithLocalB = 100.0 * (double) localBPass / (double) localBCount;
        final String passRateWithLocalBStr = fmt(passRateWithLocalB);
        final double passRateWithLocalC = 100.0 * (double) localCPass / (double) localCCount;
        final String passRateWithLocalCStr = fmt(passRateWithLocalC);
        final double passRateWithLocal = 100.0 * (double) (localPass) / (double) localCount;
        final String passRateWithLocalStr = fmt(passRateWithLocal);

        final double passRateWithTransferA = 100.0 * (double) transferAPass / (double) transferACount;
        final String passRateWithTransferAStr = fmt(passRateWithTransferA);
        final double passRateWithTransferB = 100.0 * (double) transferBPass / (double) transferBCount;
        final String passRateWithTransferBStr = fmt(passRateWithTransferB);
        final double passRateWithTransferC = 100.0 * (double) transferCPass / (double) transferCCount;
        final String passRateWithTransferCStr = fmt(passRateWithTransferC);
        final double passRateWithTransfer = 100.0 * (double) (transferPass) / (double) transferCount;
        final String passRateWithTransferStr = fmt(passRateWithTransfer);

        final double passRateWithAp = 100.0 * (double) apPass / (double) apCount;
        final String passRateWithApStr = fmt(passRateWithAp);

        final String localAGradeStr = fmt(localAGrade);
        final String localBGradeStr = fmt(localBGrade);
        final String localCGradeStr = fmt(localCGrade);
        final String localTotalGradeStr = fmt(localTotalGrade);

        final String transferAGradeStr = fmt(transferAGrade);
        final String transferBGradeStr = fmt(transferBGrade);
        final String transferCGradeStr = fmt(transferCGrade);
        final String transferTotalGradeStr = fmt(transferTotalGrade);

        final String apGradeStr = fmt(apGrade);

        report.addln();
        report.addln(IND8, passRateWithLocalAStr, PCT_PASSED, secondCourse, AFTER, firstCourse, AT_CSU_WITH_A);
        report.addln(IND8, passRateWithLocalBStr, PCT_PASSED, secondCourse, AFTER, firstCourse, AT_CSU_WITH_B);
        report.addln(IND8, passRateWithLocalCStr, PCT_PASSED, secondCourse, AFTER, firstCourse, AT_CSU_WITH_C);
        report.addln(IND8, passRateWithLocalStr, PCT_PASSED, secondCourse, AFTER, firstCourse, AT_CSU_WITH_ANY);
        report.addln(IND8, passRateWithTransferAStr, PCT_PASSED, secondCourse, AFTER, firstCourse, TRANSFER_A);
        report.addln(IND8, passRateWithTransferBStr, PCT_PASSED, secondCourse, AFTER, firstCourse, TRANSFER_B);
        report.addln(IND8, passRateWithTransferCStr, PCT_PASSED, secondCourse, AFTER, firstCourse, TRANSFER_C);
        report.addln(IND8, passRateWithTransferStr, PCT_PASSED, secondCourse, AFTER, firstCourse, TRANSFER_ANY);
        report.addln(IND8, passRateWithApStr, PCT_PASSED, secondCourse, AFTER, firstCourse, VIA_AP);

        report.addln();
        report.addln(IND8, AVG_GRADE_OF, localAGradeStr, IN, secondCourse, AFTER, firstCourse, AT_CSU_WITH_A);
        report.addln(IND8, AVG_GRADE_OF, localBGradeStr, IN, secondCourse, AFTER, firstCourse, AT_CSU_WITH_B);
        report.addln(IND8, AVG_GRADE_OF, localCGradeStr, IN, secondCourse, AFTER, firstCourse, AT_CSU_WITH_C);
        report.addln(IND8, AVG_GRADE_OF, localTotalGradeStr, IN, secondCourse, AFTER, firstCourse, AT_CSU_WITH_ANY);
        report.addln(IND8, AVG_GRADE_OF, transferAGradeStr, IN, secondCourse, AFTER, firstCourse, TRANSFER_A);
        report.addln(IND8, AVG_GRADE_OF, transferBGradeStr, IN, secondCourse, AFTER, firstCourse, TRANSFER_B);
        report.addln(IND8, AVG_GRADE_OF, transferCGradeStr, IN, secondCourse, AFTER, firstCourse, TRANSFER_C);
        report.addln(IND8, AVG_GRADE_OF, transferTotalGradeStr, IN, secondCourse, AFTER, firstCourse, TRANSFER_ANY);
        report.addln(IND8, AVG_GRADE_OF, apGradeStr, IN, secondCourse, AFTER, firstCourse, VIA_AP);

        final int key1Value = key1.intValue();
        final int year1 = key1Value / 100;
        final int key3Value = key3.intValue();
        final int year3 = key3Value / 100;
        final String year1Str = Integer.toString(year1);
        final String year3Str = Integer.toString(year3);
        final String years = SimpleBuilder.concat(year1Str, "-", year3Str);

        final int termValue = key1Value % 100;
        final String term = termValue == SPRING_TERM ? "Spring" : (termValue == FALL_TERM ? "Fall" :
                (termValue == SUMMER_TERM ? "Summer" : Integer.toString(termValue)));

        csv.addln(years, ",", term,
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
