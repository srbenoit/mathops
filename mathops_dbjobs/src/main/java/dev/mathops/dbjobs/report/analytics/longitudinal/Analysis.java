package dev.mathops.dbjobs.report.analytics.longitudinal;

import dev.mathops.commons.log.Log;
import dev.mathops.dbjobs.report.analytics.longitudinal.data.EnrollmentRec;
import dev.mathops.dbjobs.report.analytics.longitudinal.data.MajorProgramRec;
import dev.mathops.dbjobs.report.analytics.longitudinal.data.StudentTermRec;
import dev.mathops.dbjobs.report.analytics.longitudinal.datacollection.FetchEnrollmentData;
import dev.mathops.dbjobs.report.analytics.longitudinal.datacollection.FetchMajorAndProgramData;
import dev.mathops.dbjobs.report.analytics.longitudinal.datacollection.FetchStudentTermData;
import dev.mathops.dbjobs.report.analytics.longitudinal.major.MajorStatistics;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A longitudinal analysis.
 */
public final class Analysis {

    /** The start term (YYYYNN, where NN is 10 for Spring, 60 for Summer, 90 for Fall) */
    private static final int START_TERM = 201480;

    /** The end term (YYYYNN, where NN is 10 for Spring, 60 for Summer, 90 for Fall) */
    private static final int END_TERM = 202480;

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
     * @param majorsFile       the file with majors data
     */
    public void execute(final File enrollmentsFile, final File studentTermsFile, final File majorsFile) {

        Log.fine("Performing analysis");

        final Map<String, List<EnrollmentRec>> enrollments = FetchEnrollmentData.load(enrollmentsFile);
        final Map<String, List<StudentTermRec>> stuTerms = FetchStudentTermData.load(studentTermsFile);
        final List<MajorProgramRec> programs = FetchMajorAndProgramData.load(majorsFile);

        final Map<String, String> majors = new HashMap<>(200);
        for (final MajorProgramRec rec : programs) {
            final String majorCode = rec.major();
            final String desc = rec.majorDesc();
            majors.put(majorCode, desc);
        }

        final String[][] sects = {
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

        final String[][] onlineSects = {
                {"401"},
                {"801", "809"}};

        final String[][] allSects = {
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
                {"40", "040", "140"},
                {"401"},
                {"801", "809"}};

        final String[][] specialSects = {
                {"003"},
                {"004"},
                {"005"},
                {"006"},
                {"007"}};

        final CourseSummary summary = new CourseSummary(this.targetDir);
//        summary.generate(START_TERM, END_TERM, "MATH101", enrollments, stuTerms, sects, onlineSects, majors);
//        summary.generate(START_TERM, END_TERM, "MATH105", enrollments, stuTerms, sects, onlineSects, majors);
//        summary.generate(START_TERM, END_TERM, "MATH116", enrollments, stuTerms, sects, onlineSects, majors);
//        summary.generate(START_TERM, END_TERM, "MATH117", enrollments, stuTerms, sects, onlineSects, majors);
//        summary.generate(START_TERM, END_TERM, "MATH118", enrollments, stuTerms, sects, onlineSects, majors);
//        summary.generate(START_TERM, END_TERM, "MATH120", enrollments, stuTerms, sects, onlineSects, majors);
//        summary.generate(START_TERM, END_TERM, "MATH124", enrollments, stuTerms, sects, onlineSects, majors);
//        summary.generate(START_TERM, END_TERM, "MATH125", enrollments, stuTerms, sects, onlineSects, majors);
        summary.generate(START_TERM, END_TERM, "MATH125", enrollments, stuTerms, specialSects, onlineSects, majors);
//        summary.generate(START_TERM, END_TERM, "MATH126", enrollments, stuTerms, sects, onlineSects, majors);
        summary.generate(START_TERM, END_TERM, "MATH126", enrollments, stuTerms, specialSects, onlineSects, majors);
//        summary.generate(START_TERM, END_TERM, "MATH127", enrollments, stuTerms, sects, onlineSects, majors);
//        summary.generate(START_TERM, END_TERM, "MATH141", enrollments, stuTerms, sects, onlineSects, majors);
//        summary.generate(START_TERM, END_TERM, "MATH155", enrollments, stuTerms, sects, onlineSects, majors);
//        summary.generate(START_TERM, END_TERM, "MATH156", enrollments, stuTerms, sects, onlineSects, majors);
//        summary.generate(START_TERM, END_TERM, "MATH157", enrollments, stuTerms, sects, onlineSects, majors);
//        summary.generate(START_TERM, END_TERM, "MATH159", enrollments, stuTerms, sects, onlineSects, majors);
//        summary.generate(START_TERM, END_TERM, "MATH160", enrollments, stuTerms, sects, onlineSects, majors);
//        summary.generate(START_TERM, END_TERM, "MATH161", enrollments, stuTerms, sects, onlineSects, majors);
//        summary.generate(START_TERM, END_TERM, "MATH255", enrollments, stuTerms, sects, onlineSects, majors);
//        summary.generate(START_TERM, END_TERM, "MATH256", enrollments, stuTerms, sects, onlineSects, majors);
//        summary.generate(START_TERM, END_TERM, "MATH261", enrollments, stuTerms, sects, onlineSects, majors);
//        summary.generate(START_TERM, END_TERM, "MATH340", enrollments, stuTerms, sects, onlineSects, majors);
//        summary.generate(START_TERM, END_TERM, "STAT100", enrollments, stuTerms, sects, onlineSects, majors);

        // Looking for pairings with N >= 200 (N >= 50 for recent courses like MATH 120/127)

//        final SequenceSuccess seqSuccess = new SequenceSuccess(this.targetDir);
//
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH117", allSects, "AREC202", allSects); // N=2917
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH117", allSects, "CHEM107", allSects); // N=5035
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH117", allSects, "ECON202", allSects); // N=9291
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH117", allSects, "ECON204", allSects); // N=7291
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH117", allSects, "FIN200", allSects); // N=837
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH117", allSects, "MATH118", allSects); // N=1755
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH117", allSects, "MATH124", allSects); // N=7270
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH117", allSects, "MATH125", allSects); // N=7801
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH117", allSects, "MATH126", allSects); // N=2036
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH117", allSects, "MATH141", allSects); // N=5100
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH117", allSects, "MATH155", allSects); // N=3480
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH117", allSects, "MATH160", allSects); // N=2377
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH117", allSects, "STAT301", allSects); // N=7696
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH117", allSects, "STAT307", allSects); // N=2095
//
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH118", allSects, "AREC202", allSects); // N=2971
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH118", allSects, "BZ220", allSects); // N=3370
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH118", allSects, "CHEM105", allSects); // N=418
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH118", allSects, "CHEM111", allSects); // N=10568
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH118", allSects, "ECON202", allSects); // N=9694
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH118", allSects, "ECON204", allSects); // N=7952
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH118", allSects, "FIN200", allSects); // N=886
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH118", allSects, "MATH124", allSects); // N=6763
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH118", allSects, "MATH125", allSects); // N=9122
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH118", allSects, "MATH126", allSects); // N=2387
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH118", allSects, "MATH141", allSects); // N=6169
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH118", allSects, "MATH155", allSects); // N=4315
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH118", allSects, "MATH160", allSects); // N=2926
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH118", allSects, "NR220", allSects); // N=1820
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH118", allSects, "STAT301", allSects); // N=8864
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH118", allSects, "STAT307", allSects); // N=2451
//
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH120", allSects, "CHEM111", allSects); // N=59
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH120", allSects, "ECON202", allSects); // N=56
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH120", allSects, "ECON204", allSects); // N=67
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH120", allSects, "MATH125", allSects); // N=76
//
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH124", allSects, "AREC202", allSects); // N=2525
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH124", allSects, "BZ220", allSects); // N=3836
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH124", allSects, "CHEM113", allSects); // N=10205
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH124", allSects, "CS152", allSects); // N=508
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH124", allSects, "CS163", allSects); // N=1579
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH124", allSects, "FIN200", allSects); // N=786
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH124", allSects, "FW260", allSects); // N=1261
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH124", allSects, "GEOL232", allSects); // N=316
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH124", allSects, "MATH141", allSects); // N=3252
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH124", allSects, "MATH155", allSects); // N=5090
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH124", allSects, "MATH160", allSects); // N=3725
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH124", allSects, "MATH161", allSects); // N=4646
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH124", allSects, "PH121", allSects); // N=6526
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH124", allSects, "STAT301", allSects); // N=9471
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH124", allSects, "STAT307", allSects); // N=2913
//
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH125", allSects, "AREC202", allSects); // N=1267
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH125", allSects, "BZ220", allSects); // N=3202
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH125", allSects, "CS152", allSects); // N=370
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH125", allSects, "FIN200", allSects); // N=381
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH125", allSects, "GEOL250", allSects); // N=241
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH125", allSects, "GEOL372", allSects); // N=262
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH125", allSects, "MATH126", allSects); // N=868
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH125", allSects, "MATH155", allSects); // N=5085
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH125", allSects, "MATH160", allSects); // N=3527
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH125", allSects, "MATH161", allSects); // N=3713
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH125", allSects, "PH121", allSects); // N=6254
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH125", allSects, "STAT301", allSects); // N=6786
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH125", allSects, "STAT307", allSects); // N=2417
//
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH126", allSects, "AREC202", allSects); // N=983
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH126", allSects, "CS152", allSects); // N=349
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH126", allSects, "FIN200", allSects); // N=262
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH126", allSects, "MATH141", allSects); // N=1086
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH126", allSects, "MATH155", allSects); // N=1606
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH126", allSects, "MATH160", allSects); // N=3982
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH126", allSects, "MATH161", allSects); // N=4057
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH126", allSects, "MATH255", allSects); // N=387
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH126", allSects, "STAT301", allSects); // N=4096
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH126", allSects, "STAT307", allSects); // N=1354
//
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH127", allSects, "BZ220", allSects); // N=81
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH127", allSects, "CHEM111", allSects); // N=234
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH127", allSects, "CHEM113", allSects); // N=116
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH127", allSects, "ECON202", allSects); // N=53
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH127", allSects, "MATH155", allSects); // N=149
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH127", allSects, "MATH160", allSects); // N=139
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH127", allSects, "STAT301", allSects); // N=71
//
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH160", allSects, "CIVE202", allSects); // N=1297
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH160", allSects, "CIVE260", allSects); // N=3506
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH160", allSects, "CIVE261", allSects); // N=3262
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH160", allSects, "CS220", allSects); // N=1582
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH160", allSects, "DSCI369", allSects); // N=511
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH160", allSects, "ECE103", allSects); // N=1167
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH160", allSects, "MATH161", allSects); // N=6750
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH160", allSects, "MATH261", allSects); // N=6045
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH160", allSects, "MATH369", allSects); // N=2930
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH160", allSects, "MECH105", allSects); // N=2193
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH160", allSects, "MECH237", allSects); // N=1364
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH160", allSects, "MECH262", allSects); // N=317
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH160", allSects, "MECH408", allSects); // N=415
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH160", allSects, "PH141", allSects); // N=5363
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH160", allSects, "PH142", allSects); // N=4643
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH160", allSects, "STAT315", allSects); // N=2144
//
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH161", allSects, "ECE202", allSects); // N=911
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH161", allSects, "ECE204", allSects); // N=2456
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH161", allSects, "MATH261", allSects); // N=6079
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH161", allSects, "MATH301", allSects); // N=584
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH161", allSects, "MATH317", allSects); // N=733
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH161", allSects, "MATH331", allSects); // N=250
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH161", allSects, "MATH340", allSects); // N=5674
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH161", allSects, "MATH369", allSects); // N=2982
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH161", allSects, "MECH337", allSects); // N=2079
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH161", allSects, "PH142", allSects); // N=3970
//
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH261", allSects, "CBE210", allSects); // N=781
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH261", allSects, "ECE303", allSects); // N=833
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH261", allSects, "MATH340", allSects); // N=5684
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH261", allSects, "MECH337", allSects); // N=2054
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH261", allSects, "PH314", allSects); // N=295
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH261", allSects, "STAT420", allSects); // N=392
//
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH340", allSects, "CBE310", allSects); // N=754
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH340", allSects, "CBE330", allSects); // N=756
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH340", allSects, "CBE331", allSects); // N=743
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH340", allSects, "CIVE300", allSects); // N=835
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH340", allSects, "ECE303", allSects); // N=549
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH340", allSects, "ECE341", allSects); // N=519
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH340", allSects, "MATH332", allSects); // N=265
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH340", allSects, "MECH307", allSects); // N=2028
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH340", allSects, "MECH324", allSects); // N=1934
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH340", allSects, "MECH342", allSects); // N=1875
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH340", allSects, "MECH417", allSects); // N=415
//        seqSuccess.generate(START_TERM, END_TERM, enrollments, "MATH340", allSects, "PH451", allSects); // N=200

        // Analysis by math required in program (not yet useful)

//        final Map<String, List<String>> studentsByTerminalCourse = AssembleProgramData.classifyStudents(enrollments,
//                stuTerms);
//
//        final ProgramFlows flows = new ProgramFlows(this.targetDir);
//        flows.generate(studentsByTerminalCourse, enrollments, stuTerms);

        // Analysis by major/program

        final File majorsDir = new File(this.targetDir, "majors");
        if (majorsDir.exists() || majorsDir.mkdirs()) {
            final MajorStatistics majorStats = new MajorStatistics(majorsDir);
            majorStats.generate(enrollments, stuTerms);
        } else {
            Log.warning("Unable to create directory for Majors analysis");
        }

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
        final File majorsFile = new File(dir, "majors_programs.json");

        final Analysis job = new Analysis(dir);

        job.execute(enrollmentsFile, studentTermsFile, majorsFile);
    }

}
