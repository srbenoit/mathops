package dev.mathops.dbjobs.report.analytics.longitudinal;

import dev.mathops.commons.collection.IntValuedMap;
import dev.mathops.commons.log.Log;
import dev.mathops.dbjobs.report.analytics.longitudinal.data.MajorProgramRec;
import dev.mathops.dbjobs.report.analytics.longitudinal.data.StudentTermRec;
import dev.mathops.dbjobs.report.analytics.longitudinal.datacollection.FetchMajorAndProgramData;
import dev.mathops.dbjobs.report.analytics.longitudinal.datacollection.FetchStudentTermData;
import dev.mathops.text.builder.HtmlBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * An analysis of how students flow into and out of majors.  The analysis queries the declared majors of all students
 * over a period of time, and looks for changes of major from term to term.  It tracks the number of each unique
 * transition, then generates a report for every represented major of flows into and out of that major. The student's
 * initial major is counted as a transition from no major to having a major, and is included in the report.
 */
public final class MajorFlowAnalysis {

    /** A code to represent a transition into an initial major. */
    private static final String INITIAL_PROGRAM = "INITIAL";

    /** The start term (YYYYNN, where NN is 10 for Spring, 60 for Summer, 90 for Fall) */
    private static final int START_TERM = 200500;

    /** The end term (YYYYNN, where NN is 10 for Spring, 60 for Summer, 90 for Fall) */
    private static final int END_TERM = 202490;

    /** The directory in which to write CSV files. */
    private final File targetDir;

    /**
     * Constructs a new {@code MajorFlowAnalysis}.
     *
     * @param theTargetDir the directory in which to write CSV files
     */
    private MajorFlowAnalysis(final File theTargetDir) {

        this.targetDir = theTargetDir;
    }

    /**
     * Executes the job.
     *
     * @param studentTermsFile the file with student term data
     * @param majorsFile       the file with majors data
     */
    public void execute(final File studentTermsFile, final File majorsFile) {

        Log.fine("Performing analysis");

        // Build a lookup of names for each program code
        final List<MajorProgramRec> programs = FetchMajorAndProgramData.load(majorsFile);
        final int numPrograms = programs.size();

        final Map<String, MajorProgramRec> programCodes = new TreeMap<>();
        for (final MajorProgramRec rec : programs) {
            final String program = rec.program();
            programCodes.put(program, rec);
        }

        // Create a map from student ID to the list of all student term records for that student
        final Map<String, List<StudentTermRec>> stuTerms = FetchStudentTermData.load(studentTermsFile);

        // Identify the earliest and latest terms found
        int earliest = Integer.MAX_VALUE;
        int latest = 0;
        for (final List<StudentTermRec> list : stuTerms.values()) {
            for (final StudentTermRec rec : list) {
                int term = rec.academicPeriod();
                if (term < earliest) {
                    earliest = term;
                }
                if (term > latest) {
                    latest = term;
                }
            }
        }

        Log.info("Earliest term: ", earliest);
        Log.info("Latest term: ", latest);

        // Create two maps to track transitions from PROGRAM A to PROGRAM B.

        // The "inbound" map is a map from PROGRAM B to the map from PROGRAM A to a count of those transitions.
        final Map<String, IntValuedMap<String>> inbound = new HashMap<>(numPrograms);

        // The "outbound" map is a map from PROGRAM A to the map from PROGRAM B to a count of those transitions.
        final Map<String, IntValuedMap<String>> outbound = new HashMap<>(numPrograms);

        int totalSwitches = 0;
        for (final List<StudentTermRec> trajectory : stuTerms.values()) {
            totalSwitches += accumulateTrajectory(trajectory, inbound, outbound);
        }

        final List<Flow> flows = new ArrayList<>(100);

        // Generate the report

        final HtmlBuilder out = new HtmlBuilder(10000);
        for (final Map.Entry<String, MajorProgramRec> programEntry : programCodes.entrySet()) {
            final String programCode = programEntry.getKey();

            final int codeLen = programCode.length();
            if (codeLen == 4) {
                // This is one of the "fake" codes
                Log.info("Skipping '", programCode, "'");
                continue;
            }

            final IntValuedMap<String> inboundCounts = inbound.get(programCode);
            if (inboundCounts == null) {
                // No one participated in the program - skip
                continue;
            }
            final List<String> inboundKeys = inboundCounts.getKeys();

            final IntValuedMap<String> outboundCounts = outbound.get(programCode);
            final List<String> outboundKeys = outboundCounts == null ? new ArrayList<>(0) : outboundCounts.getKeys();

            final MajorProgramRec rec = programEntry.getValue();
            final String programDesc = rec.programDesc();
            final String prefix = programCode.substring(0, 4);

            out.addln();
            out.addln("***** REPORT FOR ", programCode, " (", programDesc, ")");
            out.addln();

            // First output is the number of students who had this as their first major

            final int numInitial = inboundCounts.get(INITIAL_PROGRAM, 0);
            final String numInitialStr = Integer.toString(numInitial);
            out.addln("\tStudents with ", programCode, " as their initial program: ", numInitialStr);
            out.addln();

            // Next output is a list (in decreasing count order) of programs with the same prefix from which students
            // entered the target program

            flows.clear();
            int total1 = 0;
            for (final String key : inboundKeys) {
                if (INITIAL_PROGRAM.equals(key)) {
                    continue;
                }
                if (key.startsWith(prefix)) {
                    final Flow flow = new Flow(key, inboundCounts.get(key, 0));
                    flows.add(flow);
                    total1 += flow.count();
                }
            }
            if (!flows.isEmpty()) {
                flows.sort(null);

                final String total1Str = Integer.toString(total1);
                out.addln("\tFlows into ", programCode, " from other '", prefix, "-*' programs (", total1Str, "):");

                for (final Flow flow : flows) {
                    final String source = flow.program();
                    final MajorProgramRec sourceRec = programCodes.get(source);
                    final String countStr = Integer.toString(flow.count());

                    if (sourceRec == null) {
                        out.addln("\t\t", countStr, " from ", source);
                    } else {
                        final String sourceDesc = sourceRec.programDesc();
                        out.addln("\t\t", countStr, " from ", source, " (", sourceDesc, ")");
                    }
                }

                out.addln();
            }

            // Next output is a list (in decreasing count order) of programs with the same prefix to which students
            // changed

            flows.clear();
            int total2 = 0;
            for (final String key : outboundKeys) {
                if (key.startsWith(prefix)) {
                    final Flow flow = new Flow(key, outboundCounts.get(key, 0));
                    flows.add(flow);
                    total2 += flow.count();
                }
            }
            if (!flows.isEmpty()) {
                flows.sort(null);

                final String total2Str = Integer.toString(total2);
                out.addln("\tFlows out of ", programCode, " to other '", prefix, "-*' programs (", total2Str, "):");

                for (final Flow flow : flows) {
                    final String source = flow.program();
                    final MajorProgramRec sourceRec = programCodes.get(source);
                    final String countStr = Integer.toString(flow.count());

                    if (sourceRec == null) {
                        out.addln("\t\t", countStr, " to ", source);
                    } else {
                        final String sourceDesc = sourceRec.programDesc();
                        out.addln("\t\t", countStr, " to ", source, " (", sourceDesc, ")");
                    }
                }

                out.addln();
            }

            // Next output is a list (in decreasing count order) of programs with different prefixes from which students
            // entered the target program

            flows.clear();
            int total3 = 0;
            for (final String key : inboundKeys) {
                if (INITIAL_PROGRAM.equals(key)) {
                    continue;
                }
                if (!key.startsWith(prefix)) {
                    final Flow flow = new Flow(key, inboundCounts.get(key, 0));
                    flows.add(flow);
                    total3 += flow.count();
                }
            }
            if (!flows.isEmpty()) {
                flows.sort(null);

                final String total3Str = Integer.toString(total3);
                out.addln("\tFlows into ", programCode, " from external programs (", total3Str, "):");

                for (final Flow flow : flows) {
                    final String source = flow.program();
                    final MajorProgramRec sourceRec = programCodes.get(source);
                    final String countStr = Integer.toString(flow.count());

                    if (sourceRec == null) {
                        out.addln("\t\t", countStr, " from ", source);
                    } else {
                        final String sourceDesc = sourceRec.programDesc();
                        out.addln("\t\t", countStr, " from ", source, " (", sourceDesc, ")");
                    }
                }

                out.addln();
            }

            // Next output is a list (in decreasing count order) of programs with the different prefixes to which
            // students changed

            flows.clear();
            int total4 = 0;
            for (final String key : outboundKeys) {
                if (!key.startsWith(prefix)) {
                    final Flow flow = new Flow(key, outboundCounts.get(key, 0));
                    flows.add(flow);
                    total4 += flow.count();
                }
            }
            if (!flows.isEmpty()) {
                flows.sort(null);

                final String total4Str = Integer.toString(total4);
                out.addln("\tFlows out of ", programCode, " to external programs (", total4Str, "):");

                for (final Flow flow : flows) {
                    final String source = flow.program();
                    final MajorProgramRec sourceRec = programCodes.get(source);
                    final String countStr = Integer.toString(flow.count());

                    if (sourceRec == null) {
                        out.addln("\t\t", countStr, " to ", source);
                    } else {
                        final String sourceDesc = sourceRec.programDesc();
                        out.addln("\t\t", countStr, " to ", source, " (", sourceDesc, ")");
                    }
                }

                out.addln();
            }
        }

        final File outFile = new File(this.targetDir, "major_flow_analysis.txt");

        Log.fine("Writing output to ", outFile.getAbsolutePath());

        try (final FileWriter writer = new FileWriter(outFile)) {
            writer.write(out.toString());
        } catch (final IOException ex) {
            Log.warning(ex);
        }

        Log.fine("Analysis completed");
    }

    /**
     * Accumulates the set of transitions for a single student.
     *
     * @param trajectory a list of student term records for a single student
     * @param inbound    a map from PROGRAM B to a map from PROGRAM A to the count of those transitions
     * @param outbound   a map from PROGRAM A to a map from PROGRAM B to the count of those transitions
     * @return the number of times the student switched majors after their initial declaration
     */
    private static int accumulateTrajectory(final Iterable<StudentTermRec> trajectory,
                                            final Map<? super String, IntValuedMap<String>> inbound,
                                            final Map<? super String, IntValuedMap<String>> outbound) {

        String currentProgram = null;
        int numSwitches = 0;

        for (final StudentTermRec rec : trajectory) {
            final String termProgram = rec.program();

            if (termProgram != null) {
                final String priorProgram = currentProgram == null ? INITIAL_PROGRAM : currentProgram;

                if (!termProgram.equals(priorProgram)) {
                    // PROGRAM A is "priorProgram", PROGRAM B is "termProgram"

                    final IntValuedMap<String> inboundCounter = inbound.computeIfAbsent(termProgram,
                            x -> new IntValuedMap<>());
                    inboundCounter.increment(priorProgram, 0);

                    final IntValuedMap<String> outboundCounter = outbound.computeIfAbsent(priorProgram,
                            x -> new IntValuedMap<>());
                    outboundCounter.increment(termProgram, 0);

                    if (currentProgram != null) {
                        ++numSwitches;
                    }
                }

                currentProgram = termProgram;
            }
        }

        return numSwitches;
    }

    /**
     * A record of a flow into or out of a program.
     *
     * @param program the program
     * @param count   the number of students
     */
    record Flow(String program, int count) implements Comparable<Flow> {

        /**
         * Compares two {@code Flow} records for order.
         *
         * @param o the object to be compared
         * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater
         *         than the specified object
         */
        @Override
        public int compareTo(final Flow o) {

            // Note reversed order - we're sorting in decreasing order of count
            int result = Integer.compare(o.count(), this.count);

            if (result == 0) {
                result = this.program.compareTo(o.program());
            }

            return result;
        }
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        final File dir = new File("C:\\opt\\zircon\\data");
        final File studentTermsFile = new File(dir, "student_terms.json");
        final File majorsFile = new File(dir, "majors_programs.json");

        final MajorFlowAnalysis job = new MajorFlowAnalysis(dir);

        job.execute(studentTermsFile, majorsFile);
    }
}
