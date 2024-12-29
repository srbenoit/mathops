package dev.mathops.dbjobs.report.analytics.longitudinal;

import dev.mathops.commons.log.Log;
import dev.mathops.dbjobs.report.analytics.longitudinal.data.EnrollmentRec;
import dev.mathops.dbjobs.report.analytics.longitudinal.data.StudentTermRec;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;

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
import java.util.TreeSet;

/**
 * A utility class to gather facts about a single course.
 */
final class CourseSummary {

    /** A decimal formatter. */
    private final DecimalFormat format;

    /** The directory in which to write CSV files. */
    private final File targetDir;

    /**
     * Constructs a new {@code SequenceSuccess}.
     *
     * @param theTargetDir the directory in which to write CSV files
     */
    CourseSummary(final File theTargetDir) {

        this.format = new DecimalFormat("0.00");
        this.targetDir = theTargetDir;
    }

    /**
     * Analysis of success in a second course based on how student completed a first course.
     *
     * @param startTerm      the starting term
     * @param endTerm        the ending term
     * @param course         the course ID
     * @param enrollments    the set of enrollment records
     * @param studentTerms   the set of student term records
     * @param campusSections the list of sections of interest
     * @param onlineSections the list of online sections of interest
     * @param majors         data on all major codes
     */
    void generate(final int startTerm, final int endTerm, final String course,
                  final Map<String, ? extends List<EnrollmentRec>> enrollments,
                  final Map<String, ? extends List<StudentTermRec>> studentTerms,
                  final String[][] campusSections, final String[][] onlineSections,
                  final Map<String, String> majors) {

        final Map<Integer, List<EnrollmentRec>> campusEnrollments = gatherEnrollments(course, startTerm, endTerm,
                enrollments, campusSections);
        final Map<Integer, List<EnrollmentRec>> onlineEnrollments = gatherEnrollments(course, startTerm, endTerm,
                enrollments, onlineSections);

        final HtmlBuilder csv = new HtmlBuilder(10000);

        csv.addln("Data summary for ", course);
        csv.addln("(Percentages for letter grades are relative to those given a grade and not to total enrollments)");
        csv.addln();

        // Block 1: Summary of enrollments by term and overall success rates

        emitCourseSummaryBlock(startTerm, campusEnrollments, campusSections, 90, "Fall", csv);
        emitCourseSummaryBlock(startTerm, campusEnrollments, campusSections, 10, "Spring", csv);
        emitCourseSummaryBlock(startTerm, campusEnrollments, campusSections, 60, "Summer", csv);

        emitCourseSummaryBlock(startTerm, onlineEnrollments, onlineSections, 90, "Fall (Online)", csv);
        emitCourseSummaryBlock(startTerm, onlineEnrollments, onlineSections, 10, "Spring (Online)", csv);
        emitCourseSummaryBlock(startTerm, onlineEnrollments, onlineSections, 60, "Summer (Online)", csv);

        // Block 2: TODO: Do the same, but disaggregated by population, once we have population data

        // Block 3: Enrollments in on-campus vs. online
        emitEnrollments(startTerm, endTerm, campusEnrollments, onlineEnrollments, 90, "Fall", csv);
        emitEnrollments(startTerm, endTerm, campusEnrollments, onlineEnrollments, 10, "Spring", csv);
        emitEnrollments(startTerm, endTerm, campusEnrollments, onlineEnrollments, 60, "Summer", csv);

        // Block 4: Majors

        final Collection<String> majorCodes = new TreeSet<>();
        emitMajorsSummaryBlock(campusEnrollments, studentTerms, "On-campus", majorCodes, csv);
        emitMajorsSummaryBlock(onlineEnrollments, studentTerms, "Online", majorCodes, csv);
        emitMajorNames(majorCodes, majors, csv);

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
            final String course, final int startTerm, final int endTerm,
            final Map<String, ? extends List<EnrollmentRec>> records, final String[][] sections) {

        // Use a map with automatically sorted keys.
        final Map<Integer, List<EnrollmentRec>> result = new TreeMap<>();

        for (final List<EnrollmentRec> list : records.values()) {
            for (final EnrollmentRec rec : list) {
                final String recCourse = rec.course();
                final String recSect = rec.section();

                final boolean courseMatch = course.equals(recCourse)
                                            || ("MATH157".equals(course) && "MATH180A3".equals(recCourse))
                                            || ("MATH159".equals(course) && "MATH180A4".equals(recCourse))
                                            || ("MATH156".equals(course) && "MATH180A5".equals(recCourse))
                                            || ("MATH116".equals(course) && "MATH181A1".equals(recCourse));

                if (courseMatch && !(rec.isTransfer() || rec.isApIbClep()) && rec.isGradable()
                    && containsSection(sections, recSect)) {
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
     * Tests whether a section number appears in a sections array.
     *
     * @param sections an array of arrays of section numbers
     * @param section  the section number for which to search
     * @return true if the section number was found
     */
    private static boolean containsSection(final String[][] sections, final String section) {

        boolean found = false;

        outer:
        for (final String[] inner : sections) {
            for (final String test : inner) {
                if (test.equals(section)) {
                    found = true;
                    break outer;
                }
            }
        }

        return found;
    }

    /**
     * Given a set of section numbers of interest (organized into rows, where all section numbers in a row are
     * considered as part of the same section), this method returns the first section number (called the "canonical"
     * section number) of the row in which the specified section number appears.
     *
     * @param sections an array of arrays of section numbers
     * @param section  the section number for which to search
     * @return the canonical section number
     */
    private static String canonicalSection(final String[][] sections, final String section) {

        String canonical = null;

        outer:
        for (final String[] inner : sections) {
            for (final String test : inner) {
                if (test.equals(section)) {
                    canonical = inner[0];
                    break outer;
                }
            }
        }

        return canonical;
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
     * Emits a block of rows in the CSV file with summary data for the course in a specific type of term (Fall, Spring,
     * Summer).
     *
     * @param startTerm             the starting term
     * @param applicableEnrollments the list of enrollments
     * @param sections              the list of sections of interest
     * @param termCode              the term code (10 for Spring, 60 for Summer, 90 for Fall)
     * @param termLabel             the term label
     * @param csv                   the CSV file contents to which to append
     */
    private void emitCourseSummaryBlock(final int startTerm,
                                        final Map<Integer, List<EnrollmentRec>> applicableEnrollments,
                                        final String[][] sections, final int termCode, final String termLabel,
                                        final HtmlBuilder csv) {

        csv.addln("Summary statistics for ", termLabel, " terms:");
        csv.addln();

        csv.addln("Year,# of Sections,Avg. Section Size,Max. Section Size,Total Enrollment,# Withdraws,% Withdraws,",
                "# Given Grade,% Given Grade,% A,% B,% C,% D,% U/F,Avg. GPA,\"Success Rate, All Enrolled\",",
                "\"Success Rate, Non-withdraw\",\"DFW, All Enrolled\",\"DF, Non-withdraw\"");

        final Map<String, Integer> sectionsMap = new HashMap<>(20);
        final PopulationPerformance performance = new PopulationPerformance("ALL");

        int lastYear = getYearBeforeStartYear(startTerm, termCode);

        for (final Map.Entry<Integer, List<EnrollmentRec>> entry : applicableEnrollments.entrySet()) {
            final Integer term = entry.getKey();
            final int termValue = term.intValue();

            final int code = termValue % 100;
            if (code == termCode) {
                performance.clear();
                sectionsMap.clear();

                final List<EnrollmentRec> enrollments = entry.getValue();
                for (final EnrollmentRec rec : enrollments) {
                    performance.recordEnrollment(rec);

                    final String sect = rec.section();
                    final String canonical = canonicalSection(sections, sect);

                    final Integer existing = sectionsMap.get(canonical);
                    if (existing == null) {
                        sectionsMap.put(canonical, Integer.valueOf(1));
                    } else {
                        final int i = existing.intValue();
                        sectionsMap.put(canonical, Integer.valueOf(i + 1));
                    }
                }

                final double percentWithdraw = performance.getPercentWithdrawal();
                final double percentCompleting = performance.getPercentCompleting();

                final double percentA = performance.getPercentA();
                final double percentB = performance.getPercentB();
                final double percentC = performance.getPercentC();
                final double percentD = performance.getPercentD();
                final double percentF = performance.getPercentF();

                final double dfw = performance.getDfw();
                final double dfNonWithdraw = performance.getDfwWithGrade();
                final double success = 100.0 - dfw;
                final double successNonWithdraw = 100.0 - dfNonWithdraw;

                final int totalEnrollments = performance.getTotalEnrollments();

                final int numSections = sectionsMap.size();
                final String numSectionsStr = Integer.toString(numSections);

                final double avgSectSize = (double) totalEnrollments / (double) numSections;
                final String avgSectSizeStr = this.format.format(avgSectSize);

                final int yearInt = termValue / 100;
                final String yearStr = Integer.toString(yearInt);

                String maxSectSizeStr = "-";
                if (numSections > 0) {
                    int maxSect = Integer.MIN_VALUE;
                    for (final Integer size : sectionsMap.values()) {
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

                final String successStr = this.format.format(success);
                final String successNonWithdrawStr = this.format.format(successNonWithdraw);
                final String dfwStr = this.format.format(dfw);
                final String dfNonWithdrawStr = this.format.format(dfNonWithdraw);

                while (yearInt > (lastYear + 1)) {
                    ++lastYear;
                    final String emptyYearStr = Integer.toString(lastYear);
                    csv.addln(emptyYearStr);
                }

                csv.addln(yearStr, ",", numSectionsStr, ",", avgSectSizeStr, ",", maxSectSizeStr, ",",
                        totalEnrollmentsStr, ",", numWStr, ",", pctWStr, ",", numWithGradeStr, ",",
                        pctWithGradeStr, ",", pctAStr, ",", pctBStr, ",", pctCStr, ",", pctDStr, ",", pctFStr, ",",
                        avgGpaStr, ",", successStr, ",", successNonWithdrawStr, ",", dfwStr, ",", dfNonWithdrawStr);
                lastYear = yearInt;
            }
        }
        csv.addln();
    }

    /**
     * Emits a block of rows in the CSV file with data that compares enrollments in on-campus sections vs. online
     * sections by term, then by year
     *
     * @param startTerm         the starting term
     * @param endTerm           the ending term
     * @param campusEnrollments the list of on-campus enrollments, organized by term
     * @param onlineEnrollments the list of online enrollments, organized by term
     * @param termCode          the  term code
     * @param termLabel         the term label
     * @param csv               the CSV file contents to which to append
     */
    private void emitEnrollments(final int startTerm, final int endTerm,
                                 final Map<Integer, List<EnrollmentRec>> campusEnrollments,
                                 final Map<Integer, List<EnrollmentRec>> onlineEnrollments,
                                 final int termCode, final String termLabel, final HtmlBuilder csv) {

        final List<Integer> years = getYearsRepresented(startTerm, endTerm, termCode, campusEnrollments,
                onlineEnrollments);

        int lastYear = getYearBeforeStartYear(startTerm, termCode);

        csv.addln("On-Campus and Online Enrollment Trends for ", termLabel, " terms:");
        csv.addln();

        csv.addln("Year,On-Campus,Online");

        for (final Integer year : years) {
            final int yearValue = year.intValue();

            int onCampusCount = 0;
            for (final Map.Entry<Integer, List<EnrollmentRec>> entry : campusEnrollments.entrySet()) {
                final Integer term = entry.getKey();
                final int termValue = term.intValue();
                final int codeValue = termValue % 100;
                if (yearValue == termValue / 100 && codeValue == termCode) {
                    onCampusCount += entry.getValue().size();
                }
            }

            int onlineCount = 0;
            for (final Map.Entry<Integer, List<EnrollmentRec>> entry : onlineEnrollments.entrySet()) {
                final Integer term = entry.getKey();
                final int termValue = term.intValue();
                final int codeValue = termValue % 100;
                if (yearValue == termValue / 100 && codeValue == termCode) {
                    onlineCount += entry.getValue().size();
                }
            }

            final String onCampusCountStr = Integer.toString(onCampusCount);
            final String onlineCountStr = Integer.toString(onlineCount);

            final int yearInt = year.intValue();
            while (yearInt > (lastYear + 1)) {
                ++lastYear;
                final String emptyYearStr = Integer.toString(lastYear);
                csv.addln(emptyYearStr);
            }

            csv.addln(year, ",", onCampusCountStr, ",", onlineCountStr);
            lastYear = yearInt;
        }
        csv.addln();
    }

    /**
     * Gets the list of years for which there is enrollment data.
     *
     * @param startTerm         the starting term
     * @param endTerm           the ending term
     * @param termCode          the term of interest
     * @param campusEnrollments the list of on-campus enrollments
     * @param onlineEnrollments the list of online enrollments
     * @return the list if years, in ascending order
     */
    private static List<Integer> getYearsRepresented(final int startTerm, final int endTerm, final int termCode,
                                                     final Map<Integer, List<EnrollmentRec>> campusEnrollments,
                                                     final Map<Integer, List<EnrollmentRec>> onlineEnrollments) {

        final List<Integer> years = new ArrayList<>(15);

        for (final Integer term : campusEnrollments.keySet()) {
            final int termValue = term.intValue();
            final int code = termValue % 100;

            if (termCode == code && termValue >= startTerm && termValue <= endTerm) {
                final int year = termValue / 100;

                final Integer yearObj = Integer.valueOf(year);
                if (!years.contains(yearObj)) {
                    years.add(yearObj);
                }
            }
        }

        for (final Integer term : onlineEnrollments.keySet()) {
            final int termValue = term.intValue();
            final int code = termValue % 100;

            if (termCode == code && termValue >= startTerm && termValue <= endTerm) {
                final int year = termValue / 100;
                final Integer yearObj = Integer.valueOf(year);
                if (!years.contains(yearObj)) {
                    years.add(yearObj);
                }
            }
        }

        years.sort(null);

        return years;
    }

    /**
     * Emits a block of rows in the CSV file with summary data for the course in a specific type of term (Fall, Spring,
     * Summer).
     *
     * @param applicableEnrollments the list of enrollments, organized by term
     * @param studentTerms          the set of student term records for each student
     * @param label                 the  label ("on-campus" or "online")
     * @param majorCodes            a set to which to add all major codes emitted
     * @param csv                   the CSV file contents to which to append
     */
    private void emitMajorsSummaryBlock(final Map<Integer, ? extends List<EnrollmentRec>> applicableEnrollments,
                                        final Map<String, ? extends List<StudentTermRec>> studentTerms,
                                        final String label, final Collection<? super String> majorCodes,
                                        final HtmlBuilder csv) {

        csv.addln("Student Distribution by Major (", label, " - averaged over all years):");
        csv.addln();

        csv.addln("Major,Total Enrollment,# Withdraws,% Withdraws,# Given Grade,% Given Grade,% A,% B,% C,% D,",
                "% U/F,Avg. GPA,\"Success Rate, All Enrolled\",\"Success Rate, Non-withdraw\",\"DFW, All Enrolled\",",
                "\"DF, Non-withdraw\"");

        final Map<String, PopulationPerformance> majorPerformance = new TreeMap<>();

        for (final List<EnrollmentRec> list : applicableEnrollments.values()) {
            for (final EnrollmentRec rec : list) {

                final int recPeriod = rec.academicPeriod();
                final String studentId = rec.studentId();
                String major = null;

                final List<StudentTermRec> termRecs = studentTerms.get(studentId);

                // NOTE: student term records won't exist for terms in which there is only transfer or AP/IB/CLEP,
                // and for deceased students

                if (termRecs != null) {
                    for (final StudentTermRec termRec : termRecs) {
                        if (termRec.academicPeriod() == recPeriod) {
                            major = termRec.major();
                            break;
                        }
                    }
                }

                if (major != null) {
                    final String finalMajor = major;
                    final PopulationPerformance performance = majorPerformance.computeIfAbsent(finalMajor,
                            x -> new PopulationPerformance(finalMajor));
                    performance.recordEnrollment(rec);
                }
            }
        }

        final List<PopulationPerformance> sorted = new ArrayList<>(majorPerformance.values());
        sorted.sort(null);

        int total = 0;
        for (final PopulationPerformance performance : sorted) {
            total += performance.totalEnrollments;
        }

        final int threshold = (int) Math.ceil((double) total / 50.0);
        final PopulationPerformance other = new PopulationPerformance("(others)");

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
                final double dfNonWithdraw = performance.getDfwWithGrade();
                final double success = 100.0 - dfw;
                final double successNonWithdraw = 100.0 - dfNonWithdraw;

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

                final String successStr = this.format.format(success);
                final String successNonWithdrawStr = this.format.format(successNonWithdraw);
                final String dfwStr = this.format.format(dfw);
                final String dfNonWithdrawStr = this.format.format(dfNonWithdraw);

                final String major = performance.getMajor();

                majorCodes.add(major);

                csv.addln(major, ",", totalEnrollmentsStr, ",", numWStr, ",", pctWStr, ",", numWithGradeStr, ",",
                        pctWithGradeStr, ",", pctAStr, ",", pctBStr, ",", pctCStr, ",", pctDStr, ",", pctFStr, ",",
                        avgGpaStr, ",", successStr, ",", successNonWithdrawStr, ",", dfwStr, ",", dfNonWithdrawStr);
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
            final double dfNonWithdraw = other.getDfwWithGrade();
            final double success = 100.0 - dfw;
            final double successNonWithdraw = 100.0 - dfNonWithdraw;

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

            final String successStr = this.format.format(success);
            final String successNonWithdrawStr = this.format.format(successNonWithdraw);
            final String dfwStr = this.format.format(dfw);
            final String dfNonWithdrawStr = this.format.format(dfNonWithdraw);

            csv.addln("(others),", otherEnrollmentsStr, ",", numWStr, ",", pctWStr, ",", numWithGradeStr, ",",
                    pctWithGradeStr, ",", pctAStr, ",", pctBStr, ",", pctCStr, ",", pctDStr, ",", pctFStr, ",",
                    avgGpaStr, ",", successStr, ",", successNonWithdrawStr, ",", dfwStr, ",", dfNonWithdrawStr);
        }

        csv.addln();
    }

    /**
     * Scans enrollments for the complete srt of majors represented, then emits a list of major codes and their
     * descriptions, sorted on major code.
     *
     * @param majorCodes the sorted set of names of major codes to emit
     * @param majors     a map from major code to major name
     * @param csv        the CSV file contents to which to append
     */
    private static void emitMajorNames(final Iterable<String> majorCodes,
                                       final Map<String, String> majors, final HtmlBuilder csv) {

        csv.addln("Names of majors represented:");
        csv.addln();

        csv.addln("Major,Description");

        for (final String majorCode : majorCodes) {
            final String desc = majors.get(majorCode);
            if (desc != null) {
                csv.addln(majorCode, ",\"", desc, "\"");
            }
        }

        csv.addln();
    }
}
