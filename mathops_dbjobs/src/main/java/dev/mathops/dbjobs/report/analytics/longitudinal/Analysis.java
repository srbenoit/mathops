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
        summary.generateReport(201400, 202480, "MATH156", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "MATH157", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "MATH159", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "MATH160", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "MATH161", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "MATH255", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "MATH256", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "MATH261", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "MATH340", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "FIN200", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "STAT100", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "STAT201", enrollments, studentTerms, sections, onlineSections);
        summary.generateReport(201400, 202480, "STAT204", enrollments, studentTerms, sections, onlineSections);

        final SequenceSuccess sequenceSuccess = new SequenceSuccess(this.targetDir);

        // Looking for pairings with N >= 200 (N >= 50 for recent courses like MATH 120/127)

        sequenceSuccess.generateReport(201400, enrollments, "MATH117", sections, "AREC202", sections); // N=2917
        sequenceSuccess.generateReport(201400, enrollments, "MATH117", sections, "CHEM107", sections); // N=5035
        sequenceSuccess.generateReport(201400, enrollments, "MATH117", sections, "ECON202", sections); // N=9291
        sequenceSuccess.generateReport(201400, enrollments, "MATH117", sections, "ECON204", sections); // N=7291
        sequenceSuccess.generateReport(201400, enrollments, "MATH117", sections, "FIN200", sections); // N=837
        sequenceSuccess.generateReport(201400, enrollments, "MATH117", sections, "MATH118", sections); // N=1755
        sequenceSuccess.generateReport(201400, enrollments, "MATH117", sections, "MATH124", sections); // N=7270
        sequenceSuccess.generateReport(201400, enrollments, "MATH117", sections, "MATH125", sections); // N=7801
        sequenceSuccess.generateReport(201400, enrollments, "MATH117", sections, "MATH126", sections); // N=2036
        sequenceSuccess.generateReport(201400, enrollments, "MATH117", sections, "MATH141", sections); // N=5100
        sequenceSuccess.generateReport(201400, enrollments, "MATH117", sections, "MATH155", sections); // N=3480
        sequenceSuccess.generateReport(201400, enrollments, "MATH117", sections, "MATH160", sections); // N=2377
        sequenceSuccess.generateReport(201400, enrollments, "MATH117", sections, "STAT301", sections); // N=7696
        sequenceSuccess.generateReport(201400, enrollments, "MATH117", sections, "STAT307", sections); // N=2095
        sequenceSuccess.generateReport(201400, enrollments, "MATH117", sections, "STAT311", sections); // N=755

        sequenceSuccess.generateReport(201400, enrollments, "MATH118", sections, "AREC202", sections); // N=2971
        sequenceSuccess.generateReport(201400, enrollments, "MATH118", sections, "BZ220", sections); // N=3370
        sequenceSuccess.generateReport(201400, enrollments, "MATH118", sections, "CHEM105", sections); // N=418
        sequenceSuccess.generateReport(201400, enrollments, "MATH118", sections, "CHEM111", sections); // N=10568
        sequenceSuccess.generateReport(201400, enrollments, "MATH118", sections, "CS156", sections); // N=331
        sequenceSuccess.generateReport(201400, enrollments, "MATH118", sections, "CS160", sections); // N=746
        sequenceSuccess.generateReport(201400, enrollments, "MATH118", sections, "ECON202", sections); // N=9694
        sequenceSuccess.generateReport(201400, enrollments, "MATH118", sections, "ECON204", sections); // N=7952
        sequenceSuccess.generateReport(201400, enrollments, "MATH118", sections, "FIN200", sections); // N=886
        sequenceSuccess.generateReport(201400, enrollments, "MATH118", sections, "MATH124", sections); // N=6763
        sequenceSuccess.generateReport(201400, enrollments, "MATH118", sections, "MATH125", sections); // N=9122
        sequenceSuccess.generateReport(201400, enrollments, "MATH118", sections, "MATH126", sections); // N=2387
        sequenceSuccess.generateReport(201400, enrollments, "MATH118", sections, "MATH141", sections); // N=6169
        sequenceSuccess.generateReport(201400, enrollments, "MATH118", sections, "MATH155", sections); // N=4315
        sequenceSuccess.generateReport(201400, enrollments, "MATH118", sections, "MATH160", sections); // N=2926
        sequenceSuccess.generateReport(201400, enrollments, "MATH118", sections, "NR220", sections); // N=1820
        sequenceSuccess.generateReport(201400, enrollments, "MATH118", sections, "STAT301", sections); // N=8864
        sequenceSuccess.generateReport(201400, enrollments, "MATH118", sections, "STAT307", sections); // N=2451
        sequenceSuccess.generateReport(201400, enrollments, "MATH118", sections, "STAT311", sections); // N=804

        sequenceSuccess.generateReport(201400, enrollments, "MATH120", sections, "CHEM111", sections); // N=59
        sequenceSuccess.generateReport(201400, enrollments, "MATH120", sections, "ECON202", sections); // N=56
        sequenceSuccess.generateReport(201400, enrollments, "MATH120", sections, "ECON204", sections); // N=67
        sequenceSuccess.generateReport(201400, enrollments, "MATH120", sections, "MATH125", sections); // N=76

        sequenceSuccess.generateReport(201400, enrollments, "MATH124", sections, "AREC202", sections); // N=2525
        sequenceSuccess.generateReport(201400, enrollments, "MATH124", sections, "BZ220", sections); // N=3836
        sequenceSuccess.generateReport(201400, enrollments, "MATH124", sections, "CHEM113", sections); // N=10205
        sequenceSuccess.generateReport(201400, enrollments, "MATH124", sections, "CS152", sections); // N=508
        sequenceSuccess.generateReport(201400, enrollments, "MATH124", sections, "CS163", sections); // N=1579
        sequenceSuccess.generateReport(201400, enrollments, "MATH124", sections, "FIN200", sections); // N=786
        sequenceSuccess.generateReport(201400, enrollments, "MATH124", sections, "FW260", sections); // N=1261
        sequenceSuccess.generateReport(201400, enrollments, "MATH124", sections, "GEOL232", sections); // N=316
        sequenceSuccess.generateReport(201400, enrollments, "MATH124", sections, "MATH141", sections); // N=3252
        sequenceSuccess.generateReport(201400, enrollments, "MATH124", sections, "MATH155", sections); // N=5090
        sequenceSuccess.generateReport(201400, enrollments, "MATH124", sections, "MATH160", sections); // N=3725
        sequenceSuccess.generateReport(201400, enrollments, "MATH124", sections, "MATH161", sections); // N=4646
        sequenceSuccess.generateReport(201400, enrollments, "MATH124", sections, "PH121", sections); // N=6526
        sequenceSuccess.generateReport(201400, enrollments, "MATH124", sections, "STAT301", sections); // N=9471
        sequenceSuccess.generateReport(201400, enrollments, "MATH124", sections, "STAT307", sections); // N=2913
        sequenceSuccess.generateReport(201400, enrollments, "MATH124", sections, "STAT311", sections); // N=769

        sequenceSuccess.generateReport(201400, enrollments, "MATH125", sections, "AREC202", sections); // N=1267
        sequenceSuccess.generateReport(201400, enrollments, "MATH125", sections, "BZ220", sections); // N=3202
        sequenceSuccess.generateReport(201400, enrollments, "MATH125", sections, "CS152", sections); // N=370
        sequenceSuccess.generateReport(201400, enrollments, "MATH125", sections, "FIN200", sections); // N=381
        sequenceSuccess.generateReport(201400, enrollments, "MATH125", sections, "GEOL250", sections); // N=241
        sequenceSuccess.generateReport(201400, enrollments, "MATH125", sections, "GEOL372", sections); // N=262
        sequenceSuccess.generateReport(201400, enrollments, "MATH125", sections, "MATH126", sections); // N=868
        sequenceSuccess.generateReport(201400, enrollments, "MATH125", sections, "MATH155", sections); // N=5085
        sequenceSuccess.generateReport(201400, enrollments, "MATH125", sections, "MATH160", sections); // N=3527
        sequenceSuccess.generateReport(201400, enrollments, "MATH125", sections, "MATH161", sections); // N=3713
        sequenceSuccess.generateReport(201400, enrollments, "MATH125", sections, "PH121", sections); // N=6254
        sequenceSuccess.generateReport(201400, enrollments, "MATH125", sections, "STAT301", sections); // N=6786
        sequenceSuccess.generateReport(201400, enrollments, "MATH125", sections, "STAT307", sections); // N=2417
        sequenceSuccess.generateReport(201400, enrollments, "MATH125", sections, "STAT311", sections); // N=296

        sequenceSuccess.generateReport(201400, enrollments, "MATH126", sections, "AREC202", sections); // N=983
        sequenceSuccess.generateReport(201400, enrollments, "MATH126", sections, "CS152", sections); // N=349
        sequenceSuccess.generateReport(201400, enrollments, "MATH126", sections, "FIN200", sections); // N=262
        sequenceSuccess.generateReport(201400, enrollments, "MATH126", sections, "MATH141", sections); // N=1086
        sequenceSuccess.generateReport(201400, enrollments, "MATH126", sections, "MATH155", sections); // N=1606
        sequenceSuccess.generateReport(201400, enrollments, "MATH126", sections, "MATH160", sections); // N=3982
        sequenceSuccess.generateReport(201400, enrollments, "MATH126", sections, "MATH161", sections); // N=4057
        sequenceSuccess.generateReport(201400, enrollments, "MATH126", sections, "MATH255", sections); // N=387
        sequenceSuccess.generateReport(201400, enrollments, "MATH126", sections, "STAT301", sections); // N=4096
        sequenceSuccess.generateReport(201400, enrollments, "MATH126", sections, "STAT307", sections); // N=1354
        sequenceSuccess.generateReport(201400, enrollments, "MATH126", sections, "STAT311", sections); // N=226

        sequenceSuccess.generateReport(201400, enrollments, "MATH127", sections, "BZ220", sections); // N=81
        sequenceSuccess.generateReport(201400, enrollments, "MATH127", sections, "CHEM111", sections); // N=234
        sequenceSuccess.generateReport(201400, enrollments, "MATH127", sections, "CHEM113", sections); // N=116
        sequenceSuccess.generateReport(201400, enrollments, "MATH127", sections, "ECON202", sections); // N=53
        sequenceSuccess.generateReport(201400, enrollments, "MATH127", sections, "MATH155", sections); // N=149
        sequenceSuccess.generateReport(201400, enrollments, "MATH127", sections, "MATH160", sections); // N=139
        sequenceSuccess.generateReport(201400, enrollments, "MATH127", sections, "STAT301", sections); // N=71

        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "CIVE202", sections); // N=1297
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "CIVE260", sections); // N=3506
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "CIVE261", sections); // N=3262
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "CS220", sections); // N=1582
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "DSCI369", sections); // N=511
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "ECE103", sections); // N=1167
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "MATH161", sections); // N=6750
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "MATH261", sections); // N=6045
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "MATH369", sections); // N=2930
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "MECH105", sections); // N=2193
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "MECH237", sections); // N=1364
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "MECH262", sections); // N=317
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "MECH408", sections); // N=415
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "PH141", sections); // N=5363
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "PH142", sections); // N=4643
        sequenceSuccess.generateReport(201400, enrollments, "MATH160", sections, "STAT315", sections); // N=2144

        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "ECE202", sections); // N=911
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "ECE204", sections); // N=2456
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "MATH261", sections); // N=6079
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "MATH301", sections); // N=584
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "MATH317", sections); // N=733
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "MATH331", sections); // N=250
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "MATH340", sections); // N=5674
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "MATH369", sections); // N=2982
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "MECH337", sections); // N=2079
        sequenceSuccess.generateReport(201400, enrollments, "MATH161", sections, "PH142", sections); // N=3970

        sequenceSuccess.generateReport(201400, enrollments, "MATH261", sections, "CBE210", sections); // N=781
        sequenceSuccess.generateReport(201400, enrollments, "MATH261", sections, "ECE303", sections); // N=833
        sequenceSuccess.generateReport(201400, enrollments, "MATH261", sections, "MATH340", sections); // N=5684
        sequenceSuccess.generateReport(201400, enrollments, "MATH261", sections, "MECH337", sections); // N=2054
        sequenceSuccess.generateReport(201400, enrollments, "MATH261", sections, "PH314", sections); // N=295
        sequenceSuccess.generateReport(201400, enrollments, "MATH261", sections, "STAT420", sections); // N=392

        sequenceSuccess.generateReport(201400, enrollments, "MATH340", sections, "CBE310", sections); // N=754
        sequenceSuccess.generateReport(201400, enrollments, "MATH340", sections, "CBE330", sections); // N=756
        sequenceSuccess.generateReport(201400, enrollments, "MATH340", sections, "CBE331", sections); // N=743
        sequenceSuccess.generateReport(201400, enrollments, "MATH340", sections, "CIVE300", sections); // N=835
        sequenceSuccess.generateReport(201400, enrollments, "MATH340", sections, "ECE303", sections); // N=549
        sequenceSuccess.generateReport(201400, enrollments, "MATH340", sections, "ECE341", sections); // N=519
        sequenceSuccess.generateReport(201400, enrollments, "MATH340", sections, "MATH332", sections); // N=265
        sequenceSuccess.generateReport(201400, enrollments, "MATH340", sections, "MECH307", sections); // N=2028
        sequenceSuccess.generateReport(201400, enrollments, "MATH340", sections, "MECH324", sections); // N=1934
        sequenceSuccess.generateReport(201400, enrollments, "MATH340", sections, "MECH342", sections); // N=1875
        sequenceSuccess.generateReport(201400, enrollments, "MATH340", sections, "MECH417", sections); // N=415
        sequenceSuccess.generateReport(201400, enrollments, "MATH340", sections, "PH451", sections); // N=200

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
