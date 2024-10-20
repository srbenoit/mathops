package dev.mathops.dbjobs.report.analytics.longitudinal;

import dev.mathops.commons.log.Log;
import dev.mathops.dbjobs.report.analytics.longitudinal.datacollection.FetchEnrollmentData;
import dev.mathops.dbjobs.report.analytics.longitudinal.datacollection.FetchStudentTermData;

import java.io.File;
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
     * @param enrollmentsFile  the file with enrollments data
     * @param studentTermsFile the file with student term data
     */
    public void execute(final File enrollmentsFile, final File studentTermsFile) {

        Log.fine("Performing analysis");

        final Map<String, List<EnrollmentRec>> enrollments = FetchEnrollmentData.load(enrollmentsFile);
        final Map<String, List<StudentTermRec>> studentTerms = FetchStudentTermData.load(studentTermsFile);

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

        final CourseFacts facts = new CourseFacts(this.targetDir);
        facts.generateReport(201400, 202480, "MATH101", enrollments, studentTerms, sections);
        facts.generateReport(201400, 202480, "MATH105", enrollments, studentTerms, sections);
        facts.generateReport(201400, 202480, "MATH116", enrollments, studentTerms, sections);
        facts.generateReport(201400, 202480, "MATH117", enrollments, studentTerms, sections);
        facts.generateReport(201400, 202480, "MATH118", enrollments, studentTerms, sections);
        facts.generateReport(201400, 202480, "MATH120", enrollments, studentTerms, sections);
        facts.generateReport(201400, 202480, "MATH124", enrollments, studentTerms, sections);
        facts.generateReport(201400, 202480, "MATH125", enrollments, studentTerms, sections);
        facts.generateReport(201400, 202480, "MATH126", enrollments, studentTerms, sections);
        facts.generateReport(201400, 202480, "MATH127", enrollments, studentTerms, sections);
        facts.generateReport(201400, 202480, "MATH141", enrollments, studentTerms, sections);
        facts.generateReport(201400, 202480, "MATH155", enrollments, studentTerms, sections);
        facts.generateReport(201400, 202480, "MATH160", enrollments, studentTerms, sections);
        facts.generateReport(201400, 202480, "MATH161", enrollments, studentTerms, sections);
        facts.generateReport(201400, 202480, "MATH255", enrollments, studentTerms, sections);
        facts.generateReport(201400, 202480, "MATH261", enrollments, studentTerms, sections);
        facts.generateReport(201400, 202480, "MATH340", enrollments, studentTerms, sections);

        final SequenceSuccess sequenceSuccess = new SequenceSuccess(this.targetDir);

        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "BIOM200", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "BZ348", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "CIVE202", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "CIVE260", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "CIVE261", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "CS220", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "DSCI369", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "ECE103", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "MATH161", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "MATH230", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "MATH235", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "MATH261", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "MATH301", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "MATH317", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "MATH331", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "MATH360", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "MATH366", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "MATH369", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "MECH105", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "MECH237", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "MECH262", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "MECH408", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "PH141", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "PH142", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "STAT315", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "ECE202", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "ECE204", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "GEOL452", sections);

        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "CBE210", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "ECE303", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "ENGR337", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "MATH230", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "MATH235", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "MATH261", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "MATH301", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "MATH317", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "MATH331", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "MATH340", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "MATH345", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "MATH360", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "MATH366", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "MATH369", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "MATH469", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "MECH262", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "MECH408", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "MECH337", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "MECH421", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "MECH428", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "PH142", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "PH245", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "PH314", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "PH327", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "PH353", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "PH361", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "STAT420", sections);

        sequenceSuccess.generateReport(201400, enrollments, "MATH261", sections, "CBE210", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH261", sections, "CHEM474", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH261", sections, "DSCI320", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH261", sections, "ECE303", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH261", sections, "ENGR337", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH261", sections, "MATH340", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH261", sections, "MATH345", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH261", sections, "MATH450", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH261", sections, "MATH470", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH261", sections, "MATH474", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH261", sections, "MECH337", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH261", sections, "MECH421", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH261", sections, "MECH428", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH261", sections, "PH314", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH261", sections, "PH327", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH261", sections, "PH353", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH261", sections, "PH361", sections);
        sequenceSuccess.generateReport(201400, enrollments, "MATH261", sections, "STAT420", sections);

        Log.fine("Analysis completed");
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        final File dir = new File("C:\\opt\\zircon\\data");
        final File enrollmentsFile = new File(dir, "enrollments.json");
        final File studentTermsFile = new File(dir, "student_terms.json");

        final Analysis job = new Analysis(dir);

        job.execute(enrollmentsFile, studentTermsFile);
    }

}
