package dev.mathops.dbjobs.report.analytics;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.log.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * An examination of longitudinal performance of students based on placement results, precalculus course outcomes.
 */
final class LongitudinalPerformance {

    /** A line delimiter character. */
    private static final int QUOTE = '"';

    /** The expected number of fields in each line. */
    private static final int EXPECTED_FIELD_COUNT = 45;

    /** The expected number of students. */
    private static final int EXPECTED_NUM_STUDENTS = 60000;

    /** A character used to indicate "TRUE" in a flag field. */
    private static final int FLAG_TRUE_CHAR = '1';

    /** A character used to indicate "FALSE" in a flag field. */
    private static final int FLAG_FALSE_CHAR = '0';

    /** A container for fields as they are parsed from the data file. */
    private final List<String> fields;

    /** A map from PID to cohort data. */
    private final Map<String, StudentCohortData> cohortData;

    /** A map from PID to a (sorted) map from term sequence to term data. */
    private final Map<String, Map<Integer, TermData>> termData;

    /** A map from PID to a (sorted) map from term sequence to term data. */
    private final Map<String, Map<Integer, List<RegData>>> regData;

    /** A container for the data about a student that does not change between lines in the data file. */
    private record StudentCohortData(String pid, String fallTerm, String term, String applyType, String fullTime,
                                     String collegeName, String deptName, String programDesc, boolean female,
                                     boolean rm, boolean firstGen, boolean pell, boolean resident, Float hsGpa,
                                     boolean srsFlag, boolean struggledMathHs, boolean lowMathTestScore,
                                     boolean dfGradeMath, boolean mathFlags, boolean lowGpa) {
    }

    /** A container for the data about a single term for a student. */
    private record TermData(String pid, Integer termSeq, String termCalc, String masterTerm, boolean censusFlag,
                            boolean deceased, String censusCollege, String censusDept, String censusProgram,
                            String censusClass, boolean eotFlag, Float eotTermGpa, Float eotCsuGpa,
                            Boolean eotProbation, Boolean persisted, Boolean graduated) {
    }

    /** A container for the data about a single course registration. */
    private record RegData(String pid, Integer termSeq, String course, String section, String instructionType,
                           String college, String collegeCode, String dept, String deptCode, String gradeGroup,
                           Float gradePoints, String grade) {
    }

    /**
     * Constructs a new {@code LongitudinalPerformance}.
     */
    private LongitudinalPerformance() {

        this.fields = new ArrayList<>(EXPECTED_FIELD_COUNT);
        this.cohortData = new HashMap<>(EXPECTED_NUM_STUDENTS);
        this.termData = new HashMap<>(EXPECTED_NUM_STUDENTS);
        this.regData = new HashMap<>(EXPECTED_NUM_STUDENTS);
    }

    /**
     * Executes the job.
     *
     * @return a report
     */
    private String execute() {
        final File sourceDir = new File("F:\\OneDrive - Colostate\\Desktop\\SSI\\analysis\\");
        final File sourceData = new File(sourceDir, "export.txt");

        final Collection<String> report = new ArrayList<>(50);

        if (sourceData.exists()) {
            if (isSourceDataLoaded(sourceData, report)) {
                final int numStudents = this.cohortData.size();

                final String numStudentsStr = Integer.toString(numStudents);
                final String numStudentsMsg = Res.fmt(Res.NUM_STUDENTS_FOUND, numStudentsStr);
                report.add(numStudentsMsg);

                int numTerms = 0;
                for (final Map<Integer, TermData> map : this.termData.values()) {
                    numTerms += map.size();
                }

                final String numTermsStr = Integer.toString(numTerms);
                final String numTermsMsg = Res.fmt(Res.NUM_TERMS_FOUND, numTermsStr);
                report.add(numTermsMsg);

                // TODO:
            }
        } else {
            final String absPath = sourceData.getAbsolutePath();
            final String notFoundMsg = Res.fmt(Res.SOURCE_FILE_NOT_FOUND, absPath);
            report.add(notFoundMsg);
        }

        final int numReportLines = report.size();
        final HtmlBuilder htm = new HtmlBuilder(numReportLines * 40);
        for (final String rep : report) {
            htm.addln(rep);
        }

        return htm.toString();
    }

    /**
     * Loads source data.
     *
     * @param sourceData the source data file
     * @param report     a list to which to add report lines
     * @return {@code true} if successful; {@code false} if not
     */
    private boolean isSourceDataLoaded(final File sourceData, final Collection<? super String> report) {

        boolean ok = false;

        try (final BufferedReader buffered = new BufferedReader(new FileReader(sourceData, StandardCharsets.UTF_8))) {

            // Skip the first line (column headers)
            buffered.readLine();

            String line = buffered.readLine();
            int recordCount = 0;
            while (line != null) {
                ++recordCount;
                processSourceLine(recordCount, line, report);
                line = buffered.readLine();
            }

            final String recordCountStr = Integer.toString(recordCount);
            final String recordCountMsg = Res.fmt(Res.NUM_RECORDS_FOUND, recordCountStr);
            final String reportLine = SimpleBuilder.concat(recordCountMsg);
            report.add(reportLine);
            ok = true;
        } catch (final IOException ex) {
            final String readErrorMsg = Res.get(Res.CANT_READ_SOURCE_FILE);
            Log.warning(readErrorMsg, ex);
        }

        return ok;
    }

    /**
     * Processes one line of source data.
     *
     * @param lineNumber the line number, for error logging
     * @param line       the line to process
     * @param report     a list to which to add report lines
     */
    private void processSourceLine(final int lineNumber, final String line, final Collection<? super String> report) {

        // All fields are wrapped in quotes, so break line into fields first
        this.fields.clear();
        int position = 0;
        while (true) {
            final int openQuote = line.indexOf(QUOTE, position);
            if (openQuote == -1) {
                break;
            }
            final int start = openQuote + 1;

            final int closeQuote = line.indexOf(QUOTE, start);
            if (closeQuote == -1) {
                report.add("*** Line " + lineNumber + " has unmatched quotation mark.");
                break;
            }

            final String contents = line.substring(start, closeQuote);
            this.fields.add(contents);
            position = closeQuote + 1;
        }

        final int fieldsFound = this.fields.size();
        if (fieldsFound == EXPECTED_FIELD_COUNT) {
            processStudentFields(lineNumber, report);
            processTermFields(lineNumber, report);
            processRegFields(lineNumber, report);
        } else {
            report.add("*** Line " + lineNumber + " has " + fieldsFound + " fields (expected 45)");
        }
    }

    /**
     * Processes the student-related field data for one line of source data.
     *
     * @param lineNumber the line number, for error logging
     * @param report     a list to which to add report lines
     */
    private void processStudentFields(final int lineNumber, final Collection<? super String> report) {

        final String pid = this.fields.get(0);
        final String fallTerm = this.fields.get(1);
        final String term = this.fields.get(2);
        final String applyType = this.fields.get(3);
        final String fullTime = this.fields.get(4);
        final String collegeName = this.fields.get(5);
        final String deptName = this.fields.get(6);
        final String programDesc = this.fields.get(7);

        final boolean isFemale = isFlagTrue(8, lineNumber, report);
        final boolean isRm = isFlagTrue(9, lineNumber, report);
        final boolean isFirstGen = isFlagTrue(10, lineNumber, report);
        final boolean isPell = isFlagTrue(11, lineNumber, report);
        final boolean isResident = isFlagTrue(12, lineNumber, report);
        final Float hsGpaFloat = parseGpa(13, lineNumber, report);
        final boolean isSrsFlag = isFlagTrue(14, lineNumber, report);
        final boolean isStruggledMathHs = isFlagTrue(15, lineNumber, report);
        final boolean isLowMathTestScore = isFlagTrue(16, lineNumber, report);
        final boolean isDfGradeMath = isFlagTrue(17, lineNumber, report);
        final boolean isMathFlags = isFlagTrue(18, lineNumber, report);
        final boolean isLowGpa = isFlagTrue(19, lineNumber, report);

        final StudentCohortData row = new StudentCohortData(pid, fallTerm, term, applyType, fullTime, collegeName,
                deptName, programDesc, isFemale, isRm, isFirstGen, isPell, isResident, hsGpaFloat, isSrsFlag,
                isStruggledMathHs, isLowMathTestScore, isDfGradeMath, isMathFlags, isLowGpa);

        final StudentCohortData existing = this.cohortData.get(pid);
        if (existing == null) {
            this.cohortData.put(pid, row);
        } else if (!existing.equals(row)) {
            report.add("*** Cohort data for " + pid + " changed: ");
            report.add("    " + existing);
            report.add("    " + row);
        }
    }

    /**
     * Processes the term-related field data for one line of source data.
     *
     * @param lineNumber the line number, for error logging
     * @param report     a list to which to add report lines
     */
    private void processTermFields(final int lineNumber, final Collection<? super String> report) {

        final String pid = this.fields.get(0);
        final Integer termSeq = parseTermSeq(lineNumber, report);
        final String termCalc = this.fields.get(21);
        final String masterTerm = this.fields.get(22);
        final boolean isCensusFlag = isFlagTrue(23, lineNumber, report);
        final boolean isDeceased = isFlagTrue(24, lineNumber, report);
        final String censusCollege = this.fields.get(25);
        final String censusDept = this.fields.get(26);
        final String censusProgram = this.fields.get(27);
        final String censusClass = this.fields.get(28);
        final boolean isEotFlag = isFlagTrue(29, lineNumber, report);
        final Float termGpaFloat = parseGpa(30, lineNumber, report);
        final Float csuGpaFloat = parseGpa(31, lineNumber, report);
        final Boolean eotProbation = parseBoolean(32, lineNumber, report);
        final Boolean isPersisted = parseBoolean(33, lineNumber, report);
        final Boolean isGraduated = parseBoolean(34, lineNumber, report);

        final TermData termRow = new TermData(pid, termSeq, termCalc, masterTerm, isCensusFlag, isDeceased,
                censusCollege, censusDept, censusProgram, censusClass, isEotFlag, termGpaFloat, csuGpaFloat,
                eotProbation, isPersisted, isGraduated);

        final Map<Integer, TermData> inner = this.termData.computeIfAbsent(pid, s -> new TreeMap<>());

        final TermData existing = inner.get(termSeq);
        if (existing == null) {
            inner.put(termSeq, termRow);
        } else if (!existing.equals(termRow)) {
            report.add("*** Term data for " + pid + "/" + termSeq + " changed: ");
            report.add("    " + existing);
            report.add("    " + termRow);
        }
    }

    /**
     * Processes the registration-related field data for one line of source data.
     *
     * @param lineNumber the line number, for error logging
     * @param report     a list to which to add report lines
     */
    private void processRegFields(final int lineNumber, final Collection<? super String> report) {

        final String pid = this.fields.get(0);
        final Integer termSeq = parseTermSeq(lineNumber, report);
        final String course = this.fields.get(35);
        final String section = this.fields.get(36);
        final String instructionType = this.fields.get(37);
        final String college = this.fields.get(38);
        final String collegeCode = this.fields.get(39);
        final String dept = this.fields.get(40);
        final String deptCode = this.fields.get(41);
        final String gradeGroup = this.fields.get(42);
        final Float gradePoints = parseGpa(43, lineNumber, report);
        final String grade = this.fields.get(44);

        final RegData regRow = new RegData(pid, termSeq, course, section, instructionType, college, collegeCode,
                dept, deptCode, gradeGroup, gradePoints, grade);

        final Map<Integer, List<RegData>> inner = this.regData.computeIfAbsent(pid, s -> new TreeMap<>());
        final List<RegData> innerList = inner.computeIfAbsent(termSeq, k -> new ArrayList<>(6));
        innerList.add(regRow);
    }

    /**
     * Attempts to extract a boolean value from a flag string, which should contain "1" or "0".
     *
     * @param fieldIndex the index of the flag field in the fields list
     * @param lineNumber the line number, for error logging
     * @param report     a list to which to add report lines
     * @return true if the string contained "1"; false otherwise
     */
    private boolean isFlagTrue(final int fieldIndex, final int lineNumber, final Collection<? super String> report) {

        final boolean result;

        final String flagString = this.fields.get(fieldIndex);
        if (flagString.length() == 1) {
            final int flagChar = flagString.charAt(0);
            if (flagChar == FLAG_TRUE_CHAR) {
                result = true;
            } else {
                if (flagChar != FLAG_FALSE_CHAR) {
                    final String lineNumberStr = Integer.toString(lineNumber);
                    final String fieldIndexStr = Integer.toString(fieldIndex);
                    final String badFlagMsg = Res.fmt(Res.BAD_FLAG_VALUE, lineNumberStr, flagString, fieldIndexStr);
                    report.add(badFlagMsg);
                }
                result = false;
            }
        } else {
            final String lineNumberStr = Integer.toString(lineNumber);
            final String fieldIndexStr = Integer.toString(fieldIndex);
            final String badFlagMsg = Res.fmt(Res.BAD_FLAG_VALUE, lineNumberStr, flagString, fieldIndexStr);
            report.add(badFlagMsg);
            result = false;
        }

        return result;
    }

    /**
     * Attempts to interpret a field string as a float.
     *
     * @param fieldIndex the index of the field to parse in the fields list
     * @param lineNumber the line number, for error logging
     * @param report     a list to which to add report lines
     * @return the parsed value if successful; {@code null} if not
     */
    private Float parseGpa(final int fieldIndex, final int lineNumber, final Collection<? super String> report) {

        Float result = null;

        final String toParse = this.fields.get(fieldIndex);
        if (!toParse.isBlank()) {
            try {
                result = Float.valueOf(toParse);
            } catch (final NumberFormatException ex) {
                report.add("*** Line " + lineNumber + " has '" + toParse + "' when expecting GPA");
            }
        }

        return result;
    }

    /**
     * Attempts to interpret a field string as a boolean.
     *
     * @param fieldIndex the index of the field to parse in the fields list
     * @param lineNumber the line number, for error logging
     * @param report     a list to which to add report lines
     * @return the parsed value if successful; {@code null} if not
     */
    private Boolean parseBoolean(final int fieldIndex, final int lineNumber, final Collection<? super String> report) {

        Boolean result = null;

        final String toParse = this.fields.get(fieldIndex);
        if (!toParse.isBlank()) {
            if (toParse.length() == 1) {
                final int flagChar = toParse.charAt(0);
                if (flagChar == FLAG_TRUE_CHAR) {
                    result = Boolean.TRUE;
                } else if (flagChar == FLAG_FALSE_CHAR) {
                    result = Boolean.FALSE;
                } else {
                    final String lineNumberStr = Integer.toString(lineNumber);
                    final String badFlagMsg = Res.fmt(Res.BAD_FLAG_VALUE, lineNumberStr, toParse);
                    report.add(badFlagMsg);
                }
            } else {
                final String lineNumberStr = Integer.toString(lineNumber);
                final String badFlagMsg = Res.fmt(Res.BAD_FLAG_VALUE, lineNumberStr, toParse);
                report.add(badFlagMsg);
            }
        }

        return result;
    }

    /**
     * Attempts to interpret the term sequence field string as an integer.
     *
     * @param lineNumber the line number, for error logging
     * @param report     a list to which to add report lines
     * @return the parsed value if successful; {@code null} if not
     */
    private Integer parseTermSeq(final int lineNumber, final Collection<? super String> report) {

        Integer result = null;

        final String toParse = this.fields.get(20);
        if (!toParse.isBlank()) {
            try {
                result = Integer.valueOf(toParse);
            } catch (final NumberFormatException ex) {
                report.add("*** Line " + lineNumber + " has '" + toParse + "' when expecting integer term sequence");
            }
        }

        return result;
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return "LongitudinalPerformance{fields size=" + this.fields.size() + " cohortData size="
                + this.cohortData.size() + "}";
    }

    /**
     * Main method to run the job.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        final LongitudinalPerformance job = new LongitudinalPerformance();

        final String report = job.execute();

        Log.info(report);
    }
}
