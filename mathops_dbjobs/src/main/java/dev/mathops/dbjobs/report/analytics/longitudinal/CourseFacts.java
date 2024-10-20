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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A utility class to gather facts about a single course.
 */
final class CourseFacts {

    /** A decimal formatter. */
    private final DecimalFormat format;

    /** The directory in which to write CSV files. */
    private final File targetDir;

    /**
     * Constructs a new {@code SequenceSuccess}.
     *
     * @param theTargetDir the directory in which to write CSV files
     */
    CourseFacts(final File theTargetDir) {

        this.format = new DecimalFormat("0.00");
        this.targetDir = theTargetDir;
    }

    /**
     * Analysis of success in a second course based on how student completed a first course.
     *
     * @param startTerm    the starting term
     * @param endTerm      the ending term
     * @param course       the course ID
     * @param enrollments  the set of enrollment records
     * @param studentTerms the set of student term records
     * @param sections     the list of sections of interest
     */
    void generateReport(final int startTerm, final int endTerm, final String course,
                        final Map<String, ? extends List<EnrollmentRec>> enrollments,
                        final Map<String, ? extends List<StudentTermRec>> studentTerms,
                        final Collection<String> sections) {

        final HtmlBuilder csv = new HtmlBuilder(10000);

        csv.addln("Data summary for ", course);
        csv.addln("(Percentages for letter grades are relative to those given a grade and not to total enrollments)");
        csv.addln();

        final Map<Integer, List<EnrollmentRec>> applicableEnrollments = gatherEnrollments(course, startTerm, endTerm,
                enrollments, sections);

        // Block 1: Summary of enrollments by term and overall success rates

        emitCourseSummaryBlock(applicableEnrollments, 90, "Fall", csv);
        emitCourseSummaryBlock(applicableEnrollments, 10, "Spring", csv);
        emitCourseSummaryBlock(applicableEnrollments, 60, "Summer", csv);

        // Block 2: TODO: Do the same, but disaggregated by population, once we have population data

        // Block 3: Majors

        emitMajorsSummaryBlock(applicableEnrollments, studentTerms, 90, "Fall", csv);
        emitMajorsSummaryBlock(applicableEnrollments, studentTerms, 10, "Spring", csv);
        emitMajorsSummaryBlock(applicableEnrollments, studentTerms, 60, "Summer", csv);

        // Write the CSV file

        final String filename = SimpleBuilder.concat("Summary_", course, ".csv");
        final String csvString = csv.toString();

        final File output = new File(this.targetDir, filename);
        try (final FileWriter writer = new FileWriter(output, StandardCharsets.UTF_8)) {
            writer.write(csvString);
        } catch (final IOException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Scans the set of all enrollments for those that are "applicable", meaning they are for the course of interest and
     * are not transfer or AP/IB/CLEP credit, and organize by term.
     *
     * @param course    the course of interest
     * @param startTerm the start term
     * @param endTerm   the end term
     * @param records   the list of records to scan
     * @param sections  the list of sections of interest
     * @return a map from term code to the list of applicable enrollments from that term
     */
    private static Map<Integer, List<EnrollmentRec>> gatherEnrollments(
            final String course, final int startTerm, final int endTerm, final Map<String, ?
            extends List<EnrollmentRec>> records, final Collection<String> sections) {

        // Use a map with automatically sorted keys.
        final Map<Integer, List<EnrollmentRec>> result = new TreeMap<>();

        for (final List<EnrollmentRec> list : records.values()) {
            for (final EnrollmentRec rec : list) {
                final String recCourse = rec.course();
                final String recSect = rec.section();

                if (course.equals(recCourse) && !(rec.isTransfer() || rec.isApIbClep()) && rec.isGradable()
                    && sections.contains(recSect)) {
                    final int term = rec.academicPeriod();

                    if (term >= startTerm && term <= endTerm) {
                        final Integer key = Integer.valueOf(term);

                        final List<EnrollmentRec> termList = result.computeIfAbsent(key,
                                integer -> new ArrayList<>(1000));
                        termList.add(rec);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Emits a block of rows in the CSV file with summary data for the course in a specific type of term (Fall, Spring,
     * Summer).
     *
     * @param applicableEnrollments the list of enrollments
     * @param termCode              the term code (10 for Spring, 60 for Summer, 90 for Fall)
     * @param termLabel             the term label
     * @param csv                   the CSV file contents to which to append
     */
    private void emitCourseSummaryBlock(final Map<Integer, List<EnrollmentRec>> applicableEnrollments,
                                        final int termCode, final String termLabel, final HtmlBuilder csv) {

        csv.addln("Summary statistics for ", termLabel, " terms:");
        csv.addln();

        csv.addln("Year,# Sections,Avg Sect Size,Max Sect Size,# Enrollments,# Withdraws,% Withdraws,",
                "# Given Grade,% Given Grade,% A,% B,% C,% D,% U/F,Avg. GPA,DFW of all enrolled,",
                "DFW of those given grade");

        final Map<String, Integer> sections = new HashMap<>(20);
        final PopulationPerformance performance = new PopulationPerformance("ALL");

        for (final Map.Entry<Integer, List<EnrollmentRec>> entry : applicableEnrollments.entrySet()) {
            final Integer term = entry.getKey();
            final int termValue = term.intValue();

            final int code = termValue % 100;
            if (code == termCode) {
                performance.clear();
                sections.clear();

                final List<EnrollmentRec> enrollments = entry.getValue();
                for (final EnrollmentRec rec : enrollments) {
                    performance.recordEnrollment(rec);

                    final String sect = rec.section();

                    final Integer existing = sections.get(sect);
                    if (existing == null) {
                        sections.put(sect, Integer.valueOf(1));
                    } else {
                        final int i = existing.intValue();
                        sections.put(sect, Integer.valueOf(i + 1));
                    }
                }

                Log.fine("Section sizes for ", term);
                for (final Map.Entry<String, Integer> sectEntry : sections.entrySet()) {
                    Log.fine("    ", sectEntry.getKey(), ": ", sectEntry.getValue());
                }

                final double percentWithdraw = performance.getPercentWithdrawal();
                final double percentCompleting = performance.getPercentCompleting();

                final double percentA = performance.getPercentA();
                final double percentB = performance.getPercentB();
                final double percentC = performance.getPercentC();
                final double percentD = performance.getPercentD();
                final double percentF = performance.getPercentF();

                final double dfw = performance.getDfw();
                final double dfwWithGrade = performance.getDfwWithGrade();

                final int totalEnrollments = performance.getTotalEnrollments();

                final int numSections = sections.size();
                final String numSectionsStr = Integer.toString(numSections);

                final double avgSectSize = (double) totalEnrollments / (double) numSections;
                final String avgSectSizeStr = this.format.format(avgSectSize);

                final String yearStr = Integer.toString(termValue / 100);

                String maxSectSizeStr = "-";
                if (numSections > 0) {
                    int maxSect = Integer.MIN_VALUE;
                    for (final Integer size : sections.values()) {
                        final int sizeInt = size.intValue();
                        maxSect = Math.max(maxSect, sizeInt);
                    }
                    maxSectSizeStr = Integer.toString(maxSect);
                }

                final String totalEnrollmentsStr = Integer.toString(totalEnrollments);

                final int numW = performance.getNumW();
                final String numWStr = Integer.toString(numW);
                final String pctWStr = this.format.format(percentWithdraw);

                final int numWithGrade = performance.getNumWithGrade();
                final String numWithGradeStr = Integer.toString(numWithGrade);
                final String pctWithGradeStr = this.format.format(percentCompleting);

                final String pctAStr = this.format.format(percentA);
                final String pctBStr = this.format.format(percentB);
                final String pctCStr = this.format.format(percentC);
                final String pctDStr = this.format.format(percentD);
                final String pctFStr = this.format.format(percentF);

                final double avgGpa = performance.getAverageGpa();
                final String avgGpaStr = this.format.format(avgGpa);

                final String dfwStr = this.format.format(dfw);
                final String dfwWithGradeStr = this.format.format(dfwWithGrade);

                csv.addln(yearStr, ",", numSectionsStr, ",", avgSectSizeStr, ",", maxSectSizeStr, ",",
                        totalEnrollmentsStr, ",", numWStr, ",", pctWStr, ",", numWithGradeStr, ",",
                        pctWithGradeStr, ",", pctAStr, ",", pctBStr, ",", pctCStr, ",", pctDStr, ",", pctFStr, ",",
                        avgGpaStr, ",", dfwStr, ",", dfwWithGradeStr);
            }
        }
        csv.addln();
    }

    /**
     * Emits a block of rows in the CSV file with summary data for the course in a specific type of term (Fall, Spring,
     * Summer).
     *
     * @param applicableEnrollments the list of enrollments, organized by term
     * @param studentTerms          the set of student term records for each student
     * @param termCode              the term code (10 for Spring, 60 for Summer, 90 for Fall)
     * @param termLabel             the term label
     * @param csv                   the CSV file contents to which to append
     */
    private void emitMajorsSummaryBlock(final Map<Integer, List<EnrollmentRec>> applicableEnrollments,
                                        final Map<String, ? extends List<StudentTermRec>> studentTerms,
                                        final int termCode, final String termLabel, final HtmlBuilder csv) {

        csv.addln("Summary statistics for ", termLabel, " terms:");
        csv.addln();

        csv.addln("Year,Major,# Enrollments,# Withdraws,% Withdraws,# Given Grade,% Given Grade,% A,% B,% C,% D,",
                "% U/F,Avg. GPA,DFW of all enrolled,DFW of those given grade");

        final Map<String, PopulationPerformance> majorPerformance = new TreeMap<>();

        for (final Map.Entry<Integer, List<EnrollmentRec>> entry : applicableEnrollments.entrySet()) {
            final Integer term = entry.getKey();
            final int termValue = term.intValue();

            final int code = termValue % 100;
            if (code == termCode) {
                final List<EnrollmentRec> enrollments = entry.getValue();

                for (final EnrollmentRec rec : enrollments) {
                    final String studentId = rec.studentId();
                    String major;

                    final List<StudentTermRec> termRecs = studentTerms.get(studentId);

                    if (termRecs == null) {
                        if (rec.gradeValue() != null) {
                            Log.warning("Student ", studentId, " has grade but has no Student Term records");
                        }
                        major = "(no data)";
                    } else {
                        StudentTermRec found = null;
                        for (final StudentTermRec termRec : termRecs) {
                            if (termRec.academicPeriod() == termValue) {
                                found = termRec;
                                break;
                            }
                        }

                        if (found == null) {
                            if (rec.gradeValue() != null) {
                                Log.warning("Student ", studentId, " has grade but has no Student Term record for ",
                                        term);
                            }
                            major = "(no data)";
                        } else {
                            major = found.major();
                            if (major == null) {
                                major = "(none)";
                            }
                        }
                    }

                    final String finalMajor = major;
                    final PopulationPerformance performance = majorPerformance.computeIfAbsent(finalMajor,
                            x -> new PopulationPerformance(finalMajor));
                    performance.recordEnrollment(rec);
                }

                final List<PopulationPerformance> sorted = new ArrayList<>(majorPerformance.values());
                sorted.sort(null);

                int total = 0;
                for (final PopulationPerformance performance : sorted) {
                    total += performance.totalEnrollments;
                }
                final int threshold = total / 40;
                final PopulationPerformance other = new PopulationPerformance("(others)");

                boolean first = true;
                for (final PopulationPerformance performance : sorted) {
                    final int totalEnrollments = performance.getTotalEnrollments();
                    if (totalEnrollments > threshold) {

                        final double percentWithdraw = performance.getPercentWithdrawal();
                        final double percentCompleting = performance.getPercentCompleting();

                        final double percentA = performance.getPercentA();
                        final double percentB = performance.getPercentB();
                        final double percentC = performance.getPercentC();
                        final double percentD = performance.getPercentD();
                        final double percentF = performance.getPercentF();

                        final double dfw = performance.getDfw();
                        final double dfwWithGrade = performance.getDfwWithGrade();

                        final String totalEnrollmentsStr = Integer.toString(totalEnrollments);

                        final int numW = performance.getNumW();
                        final String numWStr = Integer.toString(numW);
                        final String pctWStr = this.format.format(percentWithdraw);

                        final int numWithGrade = performance.getNumWithGrade();
                        final String numWithGradeStr = Integer.toString(numWithGrade);
                        final String pctWithGradeStr = this.format.format(percentCompleting);

                        final String pctAStr = this.format.format(percentA);
                        final String pctBStr = this.format.format(percentB);
                        final String pctCStr = this.format.format(percentC);
                        final String pctDStr = this.format.format(percentD);
                        final String pctFStr = this.format.format(percentF);

                        final double avgGpa = performance.getAverageGpa();
                        final String avgGpaStr = this.format.format(avgGpa);

                        final String dfwStr = this.format.format(dfw);
                        final String dfwWithGradeStr = this.format.format(dfwWithGrade);

                        final String yearStr = first ? Integer.toString(termValue / 100) : CoreConstants.EMPTY;
                        final String major = performance.getMajor();

                        csv.addln(yearStr, ",", major, ",", totalEnrollmentsStr, ",", numWStr, ",",
                                pctWStr, ",", numWithGradeStr, ",", pctWithGradeStr, ",", pctAStr, ",", pctBStr, ",",
                                pctCStr, ",", pctDStr, ",", pctFStr, ",", avgGpaStr, ",", dfwStr, ",", dfwWithGradeStr);

                        first = false;
                    } else {
                        other.accumulate(performance);
                    }
                }

                final int otherEnrollments = other.getTotalEnrollments();

                if (otherEnrollments > 0) {
                    final double percentWithdraw = other.getPercentWithdrawal();
                    final double percentCompleting = other.getPercentCompleting();

                    final double percentA = other.getPercentA();
                    final double percentB = other.getPercentB();
                    final double percentC = other.getPercentC();
                    final double percentD = other.getPercentD();
                    final double percentF = other.getPercentF();

                    final double dfw = other.getDfw();
                    final double dfwWithGrade = other.getDfwWithGrade();

                    final String otherEnrollmentsStr = Integer.toString(otherEnrollments);

                    final int numW = other.getNumW();
                    final String numWStr = Integer.toString(numW);
                    final String pctWStr = this.format.format(percentWithdraw);

                    final int numWithGrade = other.getNumWithGrade();
                    final String numWithGradeStr = Integer.toString(numWithGrade);
                    final String pctWithGradeStr = this.format.format(percentCompleting);

                    final String pctAStr = this.format.format(percentA);
                    final String pctBStr = this.format.format(percentB);
                    final String pctCStr = this.format.format(percentC);
                    final String pctDStr = this.format.format(percentD);
                    final String pctFStr = this.format.format(percentF);

                    final double avgGpa = other.getAverageGpa();
                    final String avgGpaStr = this.format.format(avgGpa);

                    final String dfwStr = this.format.format(dfw);
                    final String dfwWithGradeStr = this.format.format(dfwWithGrade);

                    final String yearStr = first ? Integer.toString(termValue / 100) : CoreConstants.EMPTY;
                    final String major = other.getMajor();

                    csv.addln(yearStr, ",", major, ",", otherEnrollmentsStr, ",", numWStr, ",",
                            pctWStr, ",", numWithGradeStr, ",", pctWithGradeStr, ",", pctAStr, ",", pctBStr, ",",
                            pctCStr, ",", pctDStr, ",", pctFStr, ",", avgGpaStr, ",", dfwStr, ",", dfwWithGradeStr);

                    first = false;
                }
            }
        }
        csv.addln();
    }
}
