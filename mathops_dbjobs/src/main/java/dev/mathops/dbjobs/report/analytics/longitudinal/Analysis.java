package dev.mathops.dbjobs.report.analytics.longitudinal;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.ParsingException;
import dev.mathops.commons.parser.json.JSONObject;
import dev.mathops.commons.parser.json.JSONParser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A longitudinal analysis.
 */
public final class Analysis {

    /** The directory in which to write CSV files. */
    private final File targetDir;

    /**
     * Constructs a new {@code LongitudinalAnalysis}.
     *
     * @param theTargetDir the directory in which to write CSV files
     */
    private Analysis(final File theTargetDir) {

        this.targetDir = theTargetDir;
    }

    /**
     * Executes the job.
     *
     * @param source the file with source data
     * @return the report
     */
    public String execute(final File source) {

        final HtmlBuilder report = new HtmlBuilder(10000);

        report.add("Processing");

        final List<String> sections = List.of(
                "001", "002", "003", "004", "005", "006", "007", "008", "009", "010",
                "011", "012", "013", "014", "015", "016", "017", "018", "019", "020",
                "021", "022", "023", "024", "025", "026", "027", "028", "029", "030",
                "031", "032", "033", "034", "035", "036", "037", "038", "039", "040",
                "01", "02", "03", "04", "05", "06", "07", "08", "09",
                "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
                "21", "22", "23", "24", "25", "26", "27", "28", "29", "30",
                "31", "32", "33", "34", "35", "36", "37", "38", "39", "40",
                "101", "102", "103", "104", "105", "106", "107", "108", "109", "110",
                "111", "112", "113", "114", "115", "116", "117", "118", "119", "120",
                "121", "122", "123", "124", "125", "126", "127", "128", "129", "130",
                "131", "132", "133", "134", "135", "136", "137", "138", "139", "140");

        final Map<String, List<StudentCourseRecord>> records = load(source, report);

        final SequenceSuccess sequenceSuccess = new SequenceSuccess(this.targetDir);

        sequenceSuccess.generateReport(201400, records, "MATH160", sections, "MATH161", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH161", sections, "MATH261", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH261", sections, "MATH340", sections, report);

        sequenceSuccess.generateReport(201400, records, "MATH160", sections, "BIOM200", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH160", sections, "CS220", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH160", sections, "CIVE202", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH160", sections, "CIVE260", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH160", sections, "CIVE261", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH160", sections, "DSCI369", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH160", sections, "ECE103", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH160", sections, "ENGR102", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH161", sections, "GEOL446", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH160", sections, "MATH151", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH160", sections, "MATH152", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH160", sections, "MATH158", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH160", sections, "MATH271", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH160", sections, "MATH348", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH160", sections, "BZ348", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH160", sections, "MECH105", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH160", sections, "MECH237", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH160", sections, "PH141", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH160", sections, "STAT315", sections, report);

        sequenceSuccess.generateReport(201400, records, "MATH161", sections, "ECE202", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH161", sections, "ECE204", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH161", sections, "ECE340", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH161", sections, "CONE201", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH161", sections, "GEOL440", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH161", sections, "GEOL442", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH161", sections, "GEOL452", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH161", sections, "MATH230", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH161", sections, "MATH235", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH161", sections, "MATH301", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH161", sections, "MATH317", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH161", sections, "MATH331", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH161", sections, "MATH360", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH161", sections, "MATH366", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH161", sections, "MATH369", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH161", sections, "MATH463", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH161", sections, "MATH469", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH161", sections, "MECH262", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH161", sections, "MECH408", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH161", sections, "PH142", sections, report);
        sequenceSuccess.generateReport(201400, records, "MATH161", sections, "PH245", sections, report);

        report.addln();
        report.addln("Job completed");

        return report.toString();
    }

    /**
     * Queries transfer records from the ODS for the Spring semester.
     *
     * @param source the file with source data
     * @param report a list to which to add report lines
     * @return a map from student ID to the list of student course records for that student
     */
    private static Map<String, List<StudentCourseRecord>> load(final File source, final HtmlBuilder report) {

        Map<String, List<StudentCourseRecord>> result = null;

        final String data = FileLoader.loadFileAsString(source, true);
        if (data != null) {
            try {
                final Object parsed = JSONParser.parseJSON(data);

                if (parsed instanceof final Object[] array) {
                    final String arrayLenStr = Integer.toString(array.length);
                    report.addln("    Loaded ", arrayLenStr, " records from JSON file");
                    result = new HashMap<>(100000);

                    try {
                        for (final Object obj : array) {
                            if (obj instanceof final JSONObject json) {
                                final StudentCourseRecord rec = StudentCourseRecord.parse(json);
                                final String stuId = rec.studentId();

                                final List<StudentCourseRecord> list = result.computeIfAbsent(stuId,
                                        s -> new ArrayList<>(50));
                                list.add(rec);
                            } else {
                                report.addln("    Row in JSON file is not JSON object.");
                            }
                        }

                        final int numStudents = result.size();
                        final String numStudentsStr = Integer.toString(numStudents);
                        report.addln("    Loaded data for ", numStudentsStr, " students");
                    } catch (final IllegalArgumentException ex) {
                        report.addln("    Unable to interpret a record in the JSON file.");
                    }
                } else {
                    report.addln("    Unable to interpret JSON file.");
                }
            } catch (final ParsingException ex) {
                report.addln("    Unable to load JSON file.");
                Log.warning("Failed to parse", ex);
            }
        }

        return result;
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        final File dir = new File("C:\\opt\\zircon\\data");
        final File source = new File(dir, "longitudinal.json");

        final Analysis job = new Analysis(dir);

        Log.fine(job.execute(source));
    }

}
