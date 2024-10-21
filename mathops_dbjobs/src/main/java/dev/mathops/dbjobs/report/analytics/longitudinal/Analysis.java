package dev.mathops.dbjobs.report.analytics.longitudinal;

import dev.mathops.commons.log.Log;
import dev.mathops.dbjobs.report.analytics.longitudinal.data.EnrollmentRec;
import dev.mathops.dbjobs.report.analytics.longitudinal.data.StudentTermRec;
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

        final String[][] sections = {
                {"1", "01", "001", "101"},
                {"2", "02", "002", "102"},
                {"3", "03", "003", "103"},
                {"4", "04", "004", "104"},
                {"5", "05", "005", "105"},
                {"6", "06", "006", "106"},
                {"7", "07", "007", "107"},
                {"8", "08", "008", "108"},
                {"9", "09", "009", "109"},
                {"10", "010", "110"},
                {"11", "011", "111"},
                {"12", "012", "112"},
                {"13", "013", "113"},
                {"14", "014", "114"},
                {"15", "015", "115"},
                {"16", "016", "116"},
                {"17", "017", "117"},
                {"18", "018", "118"},
                {"19", "019", "119"},
                {"20", "020", "120"},
                {"21", "021", "121"},
                {"22", "022", "122"},
                {"23", "023", "123"},
                {"24", "024", "124"},
                {"25", "025", "125"},
                {"26", "026", "126"},
                {"27", "027", "127"},
                {"28", "028", "128"},
                {"29", "029", "129"},
                {"30", "030", "130"},
                {"31", "031", "131"},
                {"32", "032", "132"},
                {"33", "033", "133"},
                {"34", "034", "134"},
                {"35", "035", "135"},
                {"36", "036", "136"},
                {"37", "037", "137"},
                {"38", "038", "138"},
                {"39", "039", "139"},
                {"40", "040", "140"}};

        final String[][] onlineSections = {
                {"401"},
                {"801", "809"}};

        final CourseSummary summary = new CourseSummary(this.targetDir);
        summary.generateReport(201400, 202480, "MATH101", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "MATH105", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "MATH116", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "MATH117", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "MATH118", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "MATH120", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "MATH124", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "MATH125", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "MATH126", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "MATH127", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "MATH141", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "MATH155", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "MATH157", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "MATH159", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "MATH160", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "MATH161", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "MATH255", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "MATH261", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "MATH340", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "FIN200", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "STAT100", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "STAT201", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "STAT204", enrollments, studentTerms, sections, onlineSections);

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
