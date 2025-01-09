package dev.mathops.dbjobs.report.analytics.longitudinal.major;

import dev.mathops.commons.log.Log;
import dev.mathops.dbjobs.report.analytics.longitudinal.data.EnrollmentRec;
import dev.mathops.dbjobs.report.analytics.longitudinal.data.StudentTermRec;
import dev.mathops.text.builder.HtmlBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A utility class to analyze statistics for each major.
 */
public final class MajorStatistics {

    /** A commonly used integer. */
    private static final Integer ONE = Integer.valueOf(1);

    /** The list of MATH courses for which to report statistics. */
    private static final List<String> FOUNDATIONAL = Arrays.asList("MATH101", "MATH105", "MATH116", "MATH117",
            "MATH118", "MATH120", "MATH124", "MATH125", "MATH126", "MATH127", "MATH141", "MATH155", "MATH156",
            "MATH157", "MATH159", "MATH160", "MATH161", "MATH255", "MATH261", "MATH340");

    /** A commonly used string array. */
    private static final String[][] M_124 = {{"MATH124"}};

    /** A commonly used string array. */
    private static final String[][] M_125 = {{"MATH125"}};

    /** A commonly used string array. */
    private static final String[][] M_124_125 = {{"MATH124", "MATH125"}};

    /** A commonly used string array. */
    private static final String[][] M_124_126 = {{"MATH124", "MATH126"}};

    /** A commonly used string array. */
    private static final String[][] M_141 = {{"MATH141"}};

    /** A commonly used string array. */
    private static final String[][] M_124_141 = {{"MATH124", "MATH141"}};

    /** A commonly used string array. */
    private static final String[][] M_141_OR_155_OR_160 = {{"MATH141"}, {"MATH155"}, {"MATH160"}};

    /** A commonly used string array. */
    private static final String[][] M_155 = {{"MATH155"}};

    /** A commonly used string array. */
    private static final String[][] M_155_OR_160 = {{"MATH155"}, {"MATH160"}};

    /** A commonly used string array. */
    private static final String[][] M_156_OR_160 = {{"MATH156"}, {"MATH160"}};

    /** A commonly used string array. */
    private static final String[][] M_161 = {{"MATH161"}};

    /** A commonly used string array. */
    private static final String[][] M_161_OR_271 = {{"MATH161"}, {"MATH271"}};

    /** A commonly used string array. */
    private static final String[][] M_255 = {{"MATH255"}};

    /** A commonly used string array. */
    private static final String[][] M_161_OR_255 = {{"MATH161"}, {"MATH255"}};

    /** A commonly used string array. */
    private static final String[][] M_256 = {{"MATH256"}};

    /** A commonly used string array. */
    private static final String[][] M_161_OR_256 = {{"MATH161"}, {"MATH256"}};

    /** A commonly used string array. */
    private static final String[][] M_256_OR_261 = {{"MATH256"}, {"MATH261"}};

    /** A commonly used string array. */
    private static final String[][] M_261 = {{"MATH261"}};

    /** A commonly used string array. */
    private static final String[][] M_340 = {{"MATH340"}};

    /** A commonly used string array. */
    private static final String[][] M3_117_118_120_124_125_126_127_141_155_160 = {{"", "MATH117", "MATH118", "MATH120",
            "MATH124", "MATH125", "MATH126", "MATH127", "MATH141", "MATH155", "MATH160"}};

    /** A commonly used string array. */
    private static final String[][] M3_117_118_120_124_141_155_160 = {{"", "MATH117", "MATH118", "MATH120", "MATH124",
            "MATH141", "MATH155", "MATH160"}};

    /** A commonly used string array. */
    private static final String[][] M3_118_124_125_126_155_160 = {{"", "MATH118", "MATH124", "MATH125", "MATH126",
            "MATH155", "MATH160"}};

    /** A commonly used string array. */
    private static final String[][] M3_117_118_120_124 = {{"", "MATH117", "MATH118", "MATH120", "MATH124"}};

    /** A commonly used string array. */
    private static final String[][] M3_117_118_125_141 = {{"", "MATH117", "MATH118", "MATH125", "MATH141"}};

    /** A commonly used string array. */
    private static final String[][] M3_AUCC = {{"", "MATH101", "MATH105", "MATH117", "MATH118", "MATH120", "MATH124",
            "MATH125", "MATH126", "MATH127", "MATH141", "MATH155", "MATH156", "MATH157", "MATH159", "MATH160",
            "MATH161", "MATH255", "FIN200", "STAT100", "STAT201", "STAT204"}};

    /** A decimal formatter. */
    private final DecimalFormat format;

    /** The directory in which to write CSV files. */
    private final File targetDir;

    /**
     * Constructs a new {@code ProgramFlows}.
     *
     * @param theTargetDir the directory in which to write CSV files
     */
    public MajorStatistics(final File theTargetDir) {

        this.format = new DecimalFormat("0.00");
        this.targetDir = theTargetDir;
    }

    /**
     * Analysis of student flows through programs
     *
     * @param allEnrollments  a map from student ID to the list of all enrollments for that student
     * @param allStudentTerms a map from student ID to the list of all student term records for that student
     */
    public void generate(final Map<String, ? extends List<EnrollmentRec>> allEnrollments,
                         final Map<String, ? extends List<StudentTermRec>> allStudentTerms) {

        // Programs that require through MATH 340:

        final List<String> programsCBEG = Arrays.asList("CBEG-BS", "CBEG-AVMZ-BS", "CBEG-BIMZ-BS", "CBEG-BMEC-BS",
                "CBEG-MLMZ-BS", "CBEG-SSEZ-BS");
        generateMajor("Chemical and Biological Engineering", programsCBEG, M_340, 4, 16, allEnrollments,
                allStudentTerms);

        final List<String> programsCIVE = List.of("CIVE-BS");
        generateMajor("Civil Engineering", programsCIVE, M_340, 4, 16, allEnrollments, allStudentTerms);

        final List<String> programsCPEG = Arrays.asList("CPEG-BS", "CPEG-AESZ-BS", "CPEG-BMEP-BS", "CPEG-EISZ-BS",
                "CPEG-NDTZ-BS");
        generateMajor("Computer Engineering", programsCPEG, M_340, 4, 16, allEnrollments, allStudentTerms);

        final List<String> programsELEG = Arrays.asList("ELEG-BS", "ELEG-ASPZ-BS", "ELEG-BMEE-BS", "ELEG-BMEL-BS",
                "ELEG-ELEZ-BS", "ELEG-LOEZ-BS");
        generateMajor("Electrical Engineering", programsELEG, M_340, 4, 16, allEnrollments, allStudentTerms);

        final List<String> programsENVE = List.of("ENVE-BS");
        generateMajor("Environmental Engineering", programsENVE, M_340, 4, 16, allEnrollments, allStudentTerms);

        final List<String> programsMECH = Arrays.asList("MECH-BS", "MECH-ACEZ-BS", "MECH-ADMZ-BS",
                "MECH-BMEM-BS");
        generateMajor("Mechanical Engineering", programsMECH, M_340, 4, 16, allEnrollments, allStudentTerms);

        final List<String> programsGEOL340 = Arrays.asList("GEOL-GPYZ-BS", "GEOL-HYDZ-BS");
        generateMajor("Geology (Geophysics and Hydrogeology Concentrations)", programsGEOL340, M_340, 4, 16,
                allEnrollments, allStudentTerms);

        final List<String> programsMATH = Arrays.asList("MATH-BS", "MATH-ALSZ-BS", "MATH-AMTZ-BS");
        generateMajor("Mathematics (Actuarial and Applied Math Concentration)", programsMATH,
                M_340, 4, 16, allEnrollments, allStudentTerms);

        final List<String> programsPHYS = Arrays.asList("PHYS-BS", "PHYS-APPZ-BS", "PHYS-PHYZ-BS");
        generateMajor("Physics", programsPHYS, M_340, 4, 16, allEnrollments, allStudentTerms);

        // Programs that require through MATH 261:

        final List<String> programsSTAT = List.of("STAT-BS");
        generateMajor("Statistics", programsSTAT, M_256_OR_261, 2, 8, allEnrollments, allStudentTerms);

        final List<String> programsMATHED = List.of("MATH-GNMZ-BS", "MATH-MTEZ-BS");
        generateMajor("Mathematics (General Math and Math Education Concentration)", programsMATHED, M_261, 3, 12,
                allEnrollments,
                allStudentTerms);

        final List<String> programsNSCIPHEZ = List.of("NSCI-PHEZ-BS");
        generateMajor("Natural Science (Physics Education Concentration)", programsNSCIPHEZ, M_261, 3, 12,
                allEnrollments, allStudentTerms);

        // Programs that require through MATH 161 or 256 or 255

        final List<String> programsCHEM = Arrays.asList("CHEM-BS", "CHEM-ECHZ-BS", "CHEM-FCHZ-BS", "CHEM-HSCZ-BS",
                "CHEM-MTRZ-BS");
        generateMajor("Chemistry", programsCHEM, M_161_OR_271, 2, 8, allEnrollments, allStudentTerms);

        final List<String> programsNSCICHEZ = List.of("NSCI-CHEZ-BS");
        generateMajor("Natural Science (Chemistry Education Concentration)", programsNSCICHEZ, M_161_OR_271, 2, 8,
                allEnrollments, allStudentTerms);

        final List<String> programsNEROCMNZ = List.of("NERO-CMNZ-BS");
        generateMajor("Neuroscience (Cell and Molecular Neuroscience Concentration)", programsNEROCMNZ, M_255, 2, 8,
                allEnrollments, allStudentTerms);

        final List<String> programsBCHM = Arrays.asList("BCHM-BS", "BCHM-ASBZ-BS", "BCHM-GBCZ-BS", "BCHM-HMSZ-BS",
                "BCHM-PPHZ-BS");
        generateMajor("Biochemistry", programsBCHM, M_161_OR_255, 2, 8, allEnrollments, allStudentTerms);

        final List<String> programsNSCIGLEZ = Arrays.asList("NSCI-GLEZ-BS", "NSCI-PHSZ-BS");
        generateMajor("Natural Science (Geology Education or Physical Science Education Concentration)",
                programsNSCIGLEZ, M_161_OR_255, 2, 8, allEnrollments, allStudentTerms);

        final List<String> programsWSSSWSSZ = List.of("WSSS-WSSZ-BS");
        generateMajor("Watershed Science and Sustainability (Watershed Science Concentration)",
                programsWSSSWSSZ, M_161_OR_255, 2, 8, allEnrollments, allStudentTerms);

        final List<String> programsDSCI = Arrays.asList("DSCI-BS", "DSCI-CSCZ-BS", "DSCI-ECNZ-BS", "DSCI-MATZ-BS",
                "DSCI-NEUZ-BS", "DSCI-STSZ-BS");
        generateMajor("Data Science", programsDSCI, M_256, 2, 8, allEnrollments, allStudentTerms);

        final List<String> programsCPSCAIMZ = List.of("CPSC-AIMZ-BS");
        generateMajor("Computer Science (Artificial Intelligence and Machine Learning Concentration)",
                programsCPSCAIMZ, M_161_OR_256, 2, 8, allEnrollments, allStudentTerms);

        final List<String> programsMATHCPMZ = List.of("MATH-CPMZ-BS");
        generateMajor("Mathematics (Computational Mathematics Concentration)", programsMATHCPMZ,
                M_161_OR_256, 4, 16, allEnrollments, allStudentTerms);

        final List<String> programsGEOLGEOZ = List.of("GEOL-BS", "GEOL-GEOZ-BS");
        generateMajor("Geology (Geology Concentration)", programsGEOLGEOZ, M_161, 2, 8, allEnrollments,
                allStudentTerms);

        // Programs that require a Calculus course (MATH 141, MATH 155, MATH 156, MATH 160)

        final List<String> programsCPSC = Arrays.asList("CPSC-BS", "CPSC-CPSZ-BS", "CPSC-CSYZ-BS", "CPSC-DCSZ-BS",
                "CPSC-DNSZ-BS", "CPSC-HCCZ-BS", "CPSC-NSCZ-BS", "CPSC-SEGZ-BS");
        generateMajor("Computer Science (except Artificial Intelligence and Machine Learning Concentration)",
                programsCPSC, M_156_OR_160, 2, 8, allEnrollments, allStudentTerms);

        final List<String> programsBIOMAPHZ = List.of("BIOM-APHZ-BS");
        generateMajor("Biomedical Sciences (Anatomy and Physiology Concentration)",
                programsBIOMAPHZ, M_155_OR_160, 1, 4, allEnrollments, allStudentTerms);

        final List<String> programsBLSC = Arrays.asList("BLSC-BS", "BLSC-BLSZ-BS", "BLSC-BTNZ-BS");
        generateMajor("Biological Science", programsBLSC, M_155_OR_160, 1, 4, allEnrollments, allStudentTerms);

        final List<String> programsECSS = List.of("ECSS-BS");
        generateMajor("Ecosystem Science and Sustainability", programsECSS, M_155_OR_160, 1, 4, allEnrollments,
                allStudentTerms);

        final List<String> programsFWCB = Arrays.asList("FWCB-BS", "FWCB-CNVZ-BS", "FWCB-FASZ-BS", "FWCB-WDBZ-BS");
        generateMajor("Fish, Wildlife, and Conservation Biology", programsFWCB, M_155_OR_160, 1, 4, allEnrollments,
                allStudentTerms);

        final List<String> programsNSCIBLEZ = List.of("NSCI-BLEZ-BS");
        generateMajor("Natural Science (Biology Education Concentration)", programsNSCIBLEZ, M_155_OR_160, 1, 4,
                allEnrollments, allStudentTerms);

        final List<String> programsWSSS = Arrays.asList("WSSS-BS", "WSSS-WSDZ-BS");
        generateMajor("Watershed Science and Sustainability (Watershed Data Concentration)",
                programsWSSS, M_155_OR_160, 1, 4, allEnrollments, allStudentTerms);

        final List<String> programsZOOL = List.of("ZOOL-BS");
        generateMajor("Zoology", programsZOOL, M_155_OR_160, 1, 4, allEnrollments, allStudentTerms);

        final List<String> programsFRRSFRBZ = List.of("FRRS-FRBZ-BS");
        generateMajor("Forest and Rangeland Stewardship (Forest Biology Concentration)", programsFRRSFRBZ, M_155, 1, 4,
                allEnrollments, allStudentTerms);

        final List<String> programsFRRSRFMZ = List.of("FRRS-BS", "FRRS-RFMZ-BS", "FRRS-FRFZ-BS", "FRRS-FMGZ-BS");
        generateMajor("Forest and Rangeland Stewardship (except Forest Biology Concentration)",
                programsFRRSRFMZ, M_141, 1, 4, allEnrollments, allStudentTerms);

        final List<String> programsNEROBCNZ = List.of("NERO-BS", "NERO-BCNZ-BS");
        generateMajor("Neuroscience (Behavioral and Cognitive Neuroscience Concentration)", programsNEROBCNZ, M_155, 1,
                4, allEnrollments, allStudentTerms);

        final List<String> programsECON = List.of("ECON-BA", "ECON-DD-BA");
        generateMajor("Economics", programsECON, M_141_OR_155_OR_160, 1, 4, allEnrollments, allStudentTerms);

        final List<String> programsWSSSWSUZ = List.of("WSSS-WSUZ-BS");
        generateMajor("Watershed Science and Sustainability (Watershed Sustainability Concentration)",
                programsWSSSWSUZ, M_141_OR_155_OR_160, 1, 4, allEnrollments, allStudentTerms);

        final List<String> programsCTMG = List.of("CTMG-BS");
        generateMajor("Construction Management", programsCTMG, M_141, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programsAGBU = Arrays.asList("AGBU-BS", "AGBU-DD-BS", "AGBU-AECZ-BS");
        generateMajor("Agricultural Business (general and Agricultural Economics Concentration)", programsAGBU,
                M_124_141, 2, 6, allEnrollments, allStudentTerms);

        // Programs that require specific Precalculus courses

        final List<String> programHORTHOSZ = List.of("HORT-HOSZ-BS");
        generateMajor("Horticulture (Horticultural Science Concentration)", programHORTHOSZ, M_124_126, 1, 3,
                allEnrollments, allStudentTerms);

        final List<String> programENHRLDAZ = Arrays.asList("ENHR-BS", "ENHR-LDAZ-BS");
        generateMajor("Environmental Horticulture (general and Landscape Design and Contracting Concentration)",
                programENHRLDAZ, M_124_126, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programNRMG = List.of("NRMG-BS");
        generateMajor("Natural Resources Management", programNRMG, M_125, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programHAES = Arrays.asList("HAES-BS", "HAES-EXSZ-BS", "HAES-HPRZ-BS");
        generateMajor("Health and Exercise Science", programHAES, M_124_125, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programAGBUFRCZ = Arrays.asList("AGBU-FRCZ-BS", "AGBU-FSSZ-BS");
        generateMajor("Agricultural Business (Farm and Ranch Management and Food Systems Concentrations)",
                programAGBUFRCZ, M_124, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programAPAM = Arrays.asList("APAM-BS", "APAM-ADAZ-BS", "APAM-MDSZ-BS", "APAM-PDVZ-BS");
        generateMajor("Apparel and Merchandising", programAPAM, M_124, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programENHR = Arrays.asList("ENHR-NALZ-BS", "ENHR-TURZ-BS");
        generateMajor("Environmental Horticulture (except Landscape Design and Contracting Concentration)",
                programENHR, M_124, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programENRE = Arrays.asList("ENRE-BS", "ENRE-DD-BS");
        generateMajor("Environmental and Natural Resource Economics", programENRE, M_124, 1, 3, allEnrollments,
                allStudentTerms);

        final List<String> programHDNR = List.of("HDNR-BS");
        generateMajor("Human Dimensions of Natural Resources", programHDNR, M_124, 1, 3, allEnrollments,
                allStudentTerms);

        final List<String> programHORT = Arrays.asList("HORT-BS", "HORT-CEHZ-BS", "HORT-DHBZ-BS", "HORT-HBMZ-BS");
        generateMajor("Horticulture", programHORT, M_124, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programIARD = Arrays.asList("IARD-BS", "IARD-IADZ-BS", "IARD-IPRZ-BS");
        generateMajor("Interior Architecture and Design", programIARD, M_124, 1, 3, allEnrollments, allStudentTerms);

        // Omit this one - 15 majors over 10 years
        // final List<String> programLSBM = Arrays.asList("LSBM-BS");
        // generateMajor("Livestock Business Management", programLSBM, M_124, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programNRTM = Arrays.asList("NRTM-GLTZ-BS", "NRTM-NRTZ-BS");
        generateMajor("Natural Resource Tourism", programNRTM, M_124, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programPSYC = Arrays.asList("PSYC-BS", "PSYC-AACZ-BS", "PSYC-ADCZ-BS", "PSYC-CCPZ-BS",
                "PSYC-GPSZ-BS", "PSYC-IOPZ-BS", "PSYC-MBBZ-BS");
        generateMajor("Psychology", programPSYC, M_124, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programSOCR = Arrays.asList("SOCR-BS", "SOCR-SAMZ-BS", "SOCR-SESZ-BS");
        generateMajor("Soil and Crop Sciences (general)", programSOCR, M_124_125, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programSOCRPBTZ = List.of("SOCR-PBTZ-BS");
        generateMajor("Soil and Crop Sciences (Plant Biotechnology Concentration)", programSOCRPBTZ, M_124_125, 2, 4,
                allEnrollments, allStudentTerms);

        // Programs allowing 3 credits from a pick-list

        final List<String> programANIM = List.of("ANIM-BS");
        generateMajor("Animal Science", programANIM, M3_117_118_120_124_125_126_127_141_155_160, 1, 3, allEnrollments,
                allStudentTerms);

        final List<String> programBUSA = Arrays.asList("BUSA-BS", "BUSA-ACCZ-BS", "BUSA-FINZ-BS", "BUSA-FPLZ-BS",
                "BUSA-HRMZ-BS", "BUSA-INSZ-BS", "BUSA-MINZ-BS", "BUSA-MKTZ-BS", "BUSA-OIMZ-BS", "BUSA-REAZ-BS",
                "BUSA-SCMZ-BS");
        generateMajor("Business Administration", programBUSA, M3_117_118_120_124_125_126_127_141_155_160, 1, 3,
                allEnrollments, allStudentTerms);

        final List<String> programEQSC = List.of("EQSC-BS");
        generateMajor("Equine Science", programEQSC, M3_117_118_120_124_125_126_127_141_155_160, 1, 3, allEnrollments,
                allStudentTerms);

        final List<String> programAGEDTDLZ = List.of("AGED-TDLZ-BS");
        generateMajor("Agricultural Education (Teacher Development Concentrations)", programAGEDTDLZ,
                M3_117_118_120_124_141_155_160, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programAGEDAGLZ = Arrays.asList("AGED-BS", "AGED-AGLZ-BS");
        generateMajor("Agricultural Education (Agricultural Literacy Concentrations)", programAGEDAGLZ,
                M3_117_118_120_124, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programBIOM = Arrays.asList("BIOM-BS", "BIOM-EPHZ-BS", "BIOM-MIDZ-BS");
        generateMajor("Biomedical Sciences (except Anatomy and Physiology Concentration)", programBIOM,
                M3_118_124_125_126_155_160, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programRECO = List.of("RECO-BS");
        generateMajor("Restoration Ecology", programRECO, M3_117_118_125_141, 1, 3, allEnrollments, allStudentTerms);

        // Programs that allow any 3 credits of AUCC

        final List<String> programANTH = Arrays.asList("ANTH-BA", "ANTH-ARCZ-BA", "ANTH-BIOZ-BA", "ANTH-CLTZ-BA",
                "ANTH-DD-BA");
        generateMajor("Anthropology", programANTH, M3_AUCC, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programART = Arrays.asList("ARTI-BA", "ARTI-AREZ-BA", "ARTI-ARTZ-BA", "ARTI-IVSZ-BA",
                "ARTI-STDZ-BA", "ARTM-BFA", "ARTM-AREZ-BF", "ARTM-DRAZ-BF", "ARTM-ELAZ-BF", "ARTM-FIBZ-BF",
                "ARTM-GRDZ-BF", "ARTM-METZ-BF", "ARTM-PHIZ-BF", "ARTM-PNTZ-BF", "ARTM-POTZ-BF", "ARTM-PRTZ-BF",
                "ARTM-SCLZ-BF");
        generateMajor("Art", programART, M3_AUCC, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programCMST = Arrays.asList("CMST-BA", "CMST-DD-BA", "CMST-TCLZ-BA");
        generateMajor("Communication Studies", programCMST, M3_AUCC, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programDANC = Arrays.asList("DANC-BFA", "DANC-DEDZ-BF", "DNCE-BA");
        generateMajor("Dance", programDANC, M3_AUCC, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programECHE = List.of("ECHE-BS");
        generateMajor("Early Childhood Education", programECHE, M3_AUCC, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programENGL = Arrays.asList("ENGL-BA", "ENGL-CRWZ-BA", "ENGL-ENEZ-BA", "ENGL-LINZ-BA",
                "ENGL-LITZ-BA", "ENGL-WRIZ-BA", "ENGL-WRLZ-BA");
        generateMajor("English", programENGL, M3_AUCC, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programETST = Arrays.asList("ETST-BA", "ETST-COIZ-BA", "ETST-RPRZ-BA", "ETST-SOTZ-BA",
                "ETST-WSTZ-BA");
        generateMajor("Ethnic Studies", programETST, M3_AUCC, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programFACS = Arrays.asList("FACS-BS", "FACS-FCSZ-BS", "FACS-IDSZ-BS");
        generateMajor("Family and Consumer Sciences", programFACS, M3_AUCC, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programGEOG = List.of("GEOG-BS");
        generateMajor("Geography", programGEOG, M3_AUCC, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programHDFS = Arrays.asList("HDFS-BS", "HDFS-DD-BS", "HDFS-DECZ-BS", "HDFS-DHDZ-BS",
                "HDFS-DLEZ-BS", "HDFS-DPHZ-BS", "HDFS-DPIZ-BS", "HDFS-ECPZ-BS", "HDFS-HDEZ-BS", "HDFS-LADZ-BS",
                "HDFS-PHPZ-BS", "HDFS-PISZ-BS");
        generateMajor("Human Development and Family Studies", programHDFS, M3_AUCC, 1, 3, allEnrollments,
                allStudentTerms);

        final List<String> programHEMG = List.of("HEMG-BS");
        generateMajor("Hospitality and Event Management", programHEMG, M3_AUCC, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programHIST = Arrays.asList("HIST-BA", "HIST-DPUZ-BA", "HIST-GENZ-BA", "HIST-LBAZ-BA",
                "HIST-LNGZ-BA", "HIST-SBSZ-BA", "HIST-SSTZ-BA");
        generateMajor("History", programHIST, M3_AUCC, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programILAR = Arrays.asList("ILAR-BA", "ILAR-DD-BA");
        generateMajor("Interdisciplinary Liberal Arts", programILAR, M3_AUCC, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programINST = Arrays.asList("INST-BA", "INST-ASTZ-BA", "INST-EUSZ-BA", "INST-GBLZ-BA",
                "INST-LTSZ-BA", "INST-MEAZ-BA");
        generateMajor("International Studies", programINST, M3_AUCC, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programJAMC = Arrays.asList("JAMC-BA", "JAMC-DD-BA");
        generateMajor("Journalism and Media Communication", programJAMC, M3_AUCC, 1, 3, allEnrollments,
                allStudentTerms);

        final List<String> programLDAR = List.of("LDAR-BS");
        generateMajor("Landscape Architecture", programLDAR, M3_AUCC, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programLLAC = Arrays.asList("LLAC-BA", "LLAC-LFRZ-BA", "LLAC-LGEZ-BA", "LLAC-LSPZ-BA",
                "LLAC-SPPZ-BA");
        generateMajor("Languages, Literatures, and Cultures", programLLAC, M3_AUCC, 1, 3, allEnrollments,
                allStudentTerms);

        final List<String> programMUSC = Arrays.asList("MUSC-COMZ-BM", "MUSC-MUEZ-BM", "MUSC-MUTZ-BM", "MUSC-PERZ-BM",
                "MUSI-BA");
        generateMajor("Music", programMUSC, M3_AUCC, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programPHIL = Arrays.asList("PHIL-BA", "PHIL-GNPZ-BA", "PHIL-GPRZ-BA", "PHIL-PHAZ-BA",
                "PHIL-PSAZ-BA");
        generateMajor("Philosophy", programPHIL, M3_AUCC, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programPOLS = Arrays.asList("POLS-BA", "POLS-DD-BA", "POLS-EPAZ-BA", "POLS-GPPZ-BA",
                "POLS-ULPZ-BA");
        generateMajor("Political Science", programPOLS, M3_AUCC, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programSOWK = List.of("SOWK-BSW");
        generateMajor("Social Work", programSOWK, M3_AUCC, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programTHTR = Arrays.asList("THTR-BA", "THTR-DIRZ-BA", "THTR-DTHZ-BA", "THTR-GTRZ-BA",
                "THTR-LDTZ-BA", "THTR-MUSZ-BA", "THTR-PDTZ-BA", "THTR-PRFZ-BA", "THTR-PWDZ-BA", "THTR-SDSZ-BA",
                "THTR-SDTZ-BA", "THTR-TDPZ-BA");
        generateMajor("Theatre", programTHTR, M3_AUCC, 1, 3, allEnrollments, allStudentTerms);

        final List<String> programWGST = List.of("WGST-BA");
        generateMajor("Women's and Gender Studies", programWGST, M3_AUCC, 1, 3, allEnrollments, allStudentTerms);
    }

    /**
     * Analysis of performance in each major.
     *
     * @param label           the label for the major
     * @param programCodes    the set of all program codes for the major of interest
     * @param courses         the lists of required terminal courses (each row is an option, all courses in a row need
     *                        to be completed)
     * @param expectSemesters the number of semesters of MATH expected in the program's degree completion map
     * @param expectCredits   the number of credits of MATH expected in the program's degree completion map
     * @param allEnrollments  a map from student ID to the list of all enrollments for that student
     * @param allStudentTerms a map from student ID to the list of all student term records for that student
     */
    private void generateMajor(final String label, final Collection<String> programCodes,
                               final String[][] courses,
                               final int expectSemesters, final int expectCredits,
                               final Map<String, ? extends List<EnrollmentRec>> allEnrollments,
                               final Map<String, ? extends List<StudentTermRec>> allStudentTerms) {

        final List<String> studentsWithProgram = findStudentsInMajor(programCodes, allStudentTerms);

        // For each student, determine whether the program requirements were completed, and whether they remained in
        // the program as long as they were taking MATH courses

        final Map<String, Integer> attemptsByCourse = new TreeMap<>();
        final Map<String, Integer> dfwCountsByCourse = new HashMap<>(10);
        final Map<String, Integer> lastMathFailedIfNotFinished = new TreeMap<>();

        int studentsExamined = 0;
        int totalFinishedPersisted = 0;
        int totalUnfinishedPersisted = 0;
        int totalFinishedNotPersisted = 0;
        int totalUnfinishedNotPersisted = 0;
        int totalUnfinished = 0;

        int totalSemestersTaken = 0;
        int totalTransferOrExamCredits = 0;
        int totalLocalCredits = 0;
        int totalDFWCredits = 0;

        for (final String studentId : studentsWithProgram) {
            final List<EnrollmentRec> enrollments = allEnrollments.get(studentId);
            final List<StudentTermRec> studentTerms = allStudentTerms.get(studentId);

            if (enrollments != null && studentTerms != null) {
                ++studentsExamined;

                final boolean finished = isFinished(enrollments, courses);
                final boolean persisted = isPersisted(studentTerms, programCodes);

                if (finished) {
                    if (persisted) {
                        ++totalFinishedPersisted;
                    } else {
                        ++totalFinishedNotPersisted;
                    }
                } else if (persisted) {
                    ++totalUnfinishedPersisted;
                } else {
                    ++totalUnfinishedNotPersisted;
                }

                final int semestersMathTaken = countSemestersMathTaken(enrollments);
                final int transferOrExamCredits = countTransferOrExamCredits(enrollments);
                final int localCredits = countLocalCredits(enrollments);
                final int dfwCredits = countDFWCredits(enrollments);
                accumulateAttemptsByCourse(enrollments, attemptsByCourse);
                accumulateDFWCountByCourse(enrollments, dfwCountsByCourse);
                if (!finished) {
                    ++totalUnfinished;
                    accumulateLastMathFailed(enrollments, lastMathFailedIfNotFinished);
                }

                totalSemestersTaken += semestersMathTaken;
                totalTransferOrExamCredits += transferOrExamCredits;
                totalLocalCredits += localCredits;
                totalDFWCredits += dfwCredits;
            }
        }

        Log.info("Analyzed major: ", label, ", generating report...");

        final HtmlBuilder builder = new HtmlBuilder(10000);

        builder.addln("Analysis of ", label);
        builder.addln();

        builder.add("Program codes included:");
        for (final String programCode : programCodes) {
            builder.add("  ", programCode);
        }
        builder.addln();
        builder.addln();

        final String studentsExaminedStr = Integer.toString(studentsExamined);
        builder.add("Examined ", studentsExaminedStr, " students who participated in the program");
        builder.addln();

        final String totalFinishedPersistedStr = Integer.toString(totalFinishedPersisted);
        final double finishedPersistedPct = (double) totalFinishedPersisted / (double) studentsExamined * 100.0;
        final String finishedPersistedPctStr = this.format.format(finishedPersistedPct);
        builder.addln("    ", totalFinishedPersistedStr, " (", finishedPersistedPctStr,
                "%) finished program requirements and persisted in program until the MATH requirements were met");

        final String totalUnfinishedPersistedStr = Integer.toString(totalUnfinishedPersisted);
        final double unfinishedPersistedPct = (double) totalUnfinishedPersisted / (double) studentsExamined * 100.0;
        final String unfinishedPersistedPctStr = this.format.format(unfinishedPersistedPct);
        builder.addln("    ", totalUnfinishedPersistedStr, " (", unfinishedPersistedPctStr,
                "%) remained in program but never finished requirements");

        final String totalFinishedNotPersistedStr = Integer.toString(totalFinishedNotPersisted);
        final double finishedNotPersistedPct =
                (double) totalFinishedNotPersisted / (double) studentsExamined * 100.0;
        final String finishedNotPersistedPctStr = this.format.format(finishedNotPersistedPct);
        builder.addln("    ", totalFinishedNotPersistedStr, " (", finishedNotPersistedPctStr,
                "%) finished program requirements but exited program");

        final String totalUnfinishedNotPersistedStr = Integer.toString(totalUnfinishedNotPersisted);
        final double unfinishedNotPersistedPct =
                (double) totalUnfinishedNotPersisted / (double) studentsExamined * 100.0;
        final String unfinishedNotPersistedPctStr = this.format.format(unfinishedNotPersistedPct);
        builder.addln("    ", totalUnfinishedNotPersistedStr, " (", unfinishedNotPersistedPctStr,
                "%) exited program without finishing requirements");

        builder.addln();

        final double averageSemesters = (double) totalSemestersTaken / (double) studentsExamined;
        final String expectedSemestersStr = Integer.toString(expectSemesters);
        final String averageSemestersStr = this.format.format(averageSemesters);
        builder.addln("    Expected number of semesters of foundational MATH from catalog: ", expectedSemestersStr);
        builder.addln("    Average number of semesters of CSU foundational MATH needed:    ", averageSemestersStr);
        builder.addln();

        final double averageTransferOrExamCredits = (double) totalTransferOrExamCredits / (double) studentsExamined;
        final double averageLocalCredits = (double) totalLocalCredits / (double) studentsExamined;
        final double averageDFWCredits = (double) totalDFWCredits / (double) studentsExamined;
        final String expectCreditsStr = Integer.toString(expectCredits);
        final String averageTransferOrExamCreditsStr = this.format.format(averageTransferOrExamCredits);
        final String averageLocalCreditsStr = this.format.format(averageLocalCredits);
        final String averageDFWCreditsStr = this.format.format(averageDFWCredits);
        builder.addln("    Expected number of credits of foundational MATH from catalog: ", expectCreditsStr);
        builder.addln("    Average number of transfer credits:      ", averageTransferOrExamCreditsStr);
        builder.addln("    Average number of CSU MATH credits:      ", averageLocalCreditsStr);
        builder.addln("    Average number of DFW CSU MATH credits:  ", averageDFWCreditsStr);
        builder.addln();

        builder.addln("Foundational MATH outcomes by course:");
        for (final Map.Entry<String, Integer> entry : attemptsByCourse.entrySet()) {
            final String course = entry.getKey();
            final Integer count = entry.getValue();
            final Integer dfw = dfwCountsByCourse.get(course);

            if (dfw == null) {
                builder.addln("    ", course, " attempted ", count, " times, no DFW outcomes (0% DFW rate)");
            } else {
                final double failPercent = dfw.doubleValue() / count.doubleValue() * 100.0;
                final String failStr = this.format.format(failPercent);
                builder.addln("    ", course, " attempted ", count, " times, ", dfw,
                        " DFW outcomes (", failStr, "% DFW rate)");
            }
        }
        builder.addln();

        for (final Map.Entry<String, Integer> entry : attemptsByCourse.entrySet()) {
            final String course = entry.getKey();
            final Integer count = entry.getValue();
            final Integer dfw = dfwCountsByCourse.get(course);

            if (dfw == null) {
                builder.addln(course, ",", count, ",", dfw, ",0.00%");
            } else {
                final double failPercent = dfw.doubleValue() / count.doubleValue() * 100.0;
                final String failStr = this.format.format(failPercent);
                builder.addln(course, ",", count, ",", dfw, ",", failStr, "%");
            }
        }
        builder.addln();

        builder.addln("Last DFW foundational MATH course for students who did not complete program requirements:");
        for (final Map.Entry<String, Integer> entry : lastMathFailedIfNotFinished.entrySet()) {
            final String course = entry.getKey();
            final Integer count = entry.getValue();

            final double failPercent = count.doubleValue() / (double) totalUnfinished * 100.0;
            final String failStr = this.format.format(failPercent);
            builder.addln("    ", course, " appeared ", count, " times (for ", failStr,
                    "% of students who did not complete requirements)");
        }
        builder.addln();

        for (final Map.Entry<String, Integer> entry : lastMathFailedIfNotFinished.entrySet()) {
            final String course = entry.getKey();
            final Integer count = entry.getValue();

            final double failPercent = count.doubleValue() / (double) totalUnfinished * 100.0;
            final String failStr = this.format.format(failPercent);
            builder.addln(course, ",", count, ",", failStr, "%");
        }
        builder.addln();

        final String fileData = builder.toString();

        final String filename = label + " Major Statistics.txt";
        final File file = new File(this.targetDir, filename);
        try (final FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            writer.write(fileData);
        } catch (final IOException ex) {
            Log.warning("Failed to write file", ex);
        }
    }

    /**
     * Given a map from student ID to the list of student terms, and a set of program codes, finds the list of students
     * IDs who were in any of the program codes of interest during any semester they took a MATH course.
     *
     * @param programCodes the set of program codes of interest
     * @param studentTerms the map from student ID to list of student terms
     * @return the list of student IDs who were in any of the program codes of interest
     */
    private static List<String> findStudentsInMajor(final Collection<String> programCodes,
                                                    final Map<String, ? extends List<StudentTermRec>> studentTerms) {

        final List<String> studentsWithProgram = new ArrayList<>(500);

        for (final Map.Entry<String, ? extends List<StudentTermRec>> entry : studentTerms.entrySet()) {
            final String studentId = entry.getKey();
            final List<StudentTermRec> studentStudentTerms = entry.getValue();

            for (final StudentTermRec test : studentStudentTerms) {
                final String program = test.program();
                if (programCodes.contains(program)) {
                    studentsWithProgram.add(studentId);
                    break;
                }
            }
        }

        return studentsWithProgram;
    }

    /**
     * Tests whether a student finished required courses for a program.
     *
     * @param studentEnrollments the students complete enrollment record
     * @param courses            the lists of required terminal courses (each row is an option, all courses in a row
     *                           need to be completed); if the first course is blank, then "completion" means
     *                           accumulating credit for at least 3 total credits from any courses in the list
     * @return true if the student completed at least one required list of courses; false if not
     */
    private static boolean isFinished(final Iterable<EnrollmentRec> studentEnrollments, final String[][] courses) {

        boolean completed = false;

        for (final String[] courseArray : courses) {
            if (courseArray[0].isBlank()) {
                // "Completion" means completing 3 credits from any the courses in this row
                int credits = 0;
                for (final String course : courseArray) {
                    if (isCourseFinished(studentEnrollments, course)) {
                        credits += getMathCreditsByCourse(course);
                        break;
                    }
                }

                if (credits >= 3) {
                    completed = true;
                    break;
                }
            } else {
                // "Completion" means completing all the courses in this row
                boolean doneAll = true;
                for (final String course : courseArray) {
                    if (!isCourseFinished(studentEnrollments, course)) {
                        doneAll = false;
                        break;
                    }
                }

                if (doneAll) {
                    completed = true;
                    break;
                }
            }
        }

        return completed;
    }

    /**
     * Tests whether a student completed a single course.
     *
     * @param studentEnrollments the students complete enrollment record
     * @param course             the  course
     * @return true if the student completed the course; false if not
     */
    private static boolean isCourseFinished(final Iterable<EnrollmentRec> studentEnrollments, final String course) {

        boolean completed = false;

        for (final EnrollmentRec rec : studentEnrollments) {
            final String recCourse = rec.course();
            if (recCourse.equals(course)) {
                if (rec.isPassed() || rec.isTransfer() || rec.isApIbClep()) {
                    completed = true;
                    break;
                }

                if ("MATH340".equals(course) && "MATH345".equals(recCourse) && rec.isPassed()) {
                    completed = true;
                    break;
                }
            }
        }

        return completed;
    }

    /**
     * Tests whether the student was still in one of the program codes of interest as of their last MATH course.
     *
     * @param studentTerms the list of student terms
     * @param programCodes the set of program codes of interest
     * @return true if the student was still in the program of interest it the last student term
     */
    private static boolean isPersisted(final List<StudentTermRec> studentTerms,
                                       final Collection<String> programCodes) {

        final StudentTermRec last = studentTerms.getLast();
        final String lastProgram = last.program();

        return programCodes.contains(lastProgram);
    }

    /**
     * Counts the number of semesters in which the student was enrolled in MATH.
     *
     * @param enrollments the list of all student enrollments
     * @return the number semesters with a MATH enrollment
     */
    private static int countSemestersMathTaken(final Iterable<EnrollmentRec> enrollments) {

        final Collection<Integer> result = new HashSet<>(10);

        for (final EnrollmentRec rec : enrollments) {
            final String course = rec.course();
            if (course.startsWith("MATH")) {
                final int period = rec.academicPeriod();
                final Integer value = Integer.valueOf(period);
                result.add(value);
            }
        }

        return result.size();
    }

    /**
     * Counts the number of MATH credits the student has through transfer for AP/IB/CLEP exam.
     *
     * @param enrollments the list of all student enrollments
     * @return the number of transfer or exam credits
     */
    private static int countTransferOrExamCredits(final Iterable<EnrollmentRec> enrollments) {

        int result = 0;

        for (final EnrollmentRec rec : enrollments) {
            final String course = rec.course();
            if (course.startsWith("MATH")) {
                if (rec.isTransfer() || rec.isApIbClep()) {
                    result += getMathCreditsByCourse(course);
                }
            }
        }

        return result;
    }

    /**
     * Counts the number of MATH credits the student has that were taken at CSU.
     *
     * @param enrollments the list of all student enrollments
     * @return the number of local credits
     */
    private static int countLocalCredits(final Iterable<EnrollmentRec> enrollments) {

        int result = 0;

        for (final EnrollmentRec rec : enrollments) {
            final String course = rec.course();
            if (course.startsWith("MATH")) {
                if (!(rec.isTransfer() || rec.isApIbClep())) {
                    result += getMathCreditsByCourse(course);
                }
            }
        }

        return result;
    }

    /**
     * Counts the number of MATH credits the student has that were taken at CSU with e DFW result.
     *
     * @param enrollments the list of all student enrollments
     * @return the number of local credits
     */
    private static int countDFWCredits(final Iterable<EnrollmentRec> enrollments) {

        int result = 0;

        for (final EnrollmentRec rec : enrollments) {
            final String course = rec.course();
            if (course.startsWith("MATH")) {
                if (rec.isDfw()) {
                    result += getMathCreditsByCourse(course);
                }
            }
        }

        return result;
    }

    /**
     * Accumulates the total number of attempts made for each MATH course.
     *
     * @param enrollments      the list of all student enrollments
     * @param attemptsByCourse a map from course ID to the number of attempts
     */
    private void accumulateAttemptsByCourse(final List<EnrollmentRec> enrollments,
                                            final Map<String, Integer> attemptsByCourse) {

        for (final EnrollmentRec rec : enrollments) {
            if (rec.isTransfer() || rec.isApIbClep()) {
                continue;
            }

            String course = rec.course();
            if (course.startsWith("MATH")) {

                course = switch (course) {
                    case "MATH180A3" -> "MATH157";
                    case "MATH180A4" -> "MATH159";
                    case "MATH180A5" -> "MATH156";
                    case "MATH181A1" -> "MATH116";
                    case "MATH345" -> "MATH340";
                    default -> course;
                };

                if (FOUNDATIONAL.contains(course)) {
                    final Integer currentValue = attemptsByCourse.get(course);
                    if (currentValue == null) {
                        attemptsByCourse.put(course, ONE);
                    } else {
                        final int newInt = currentValue.intValue() + 1;
                        final Integer newValue = Integer.valueOf(newInt);
                        attemptsByCourse.put(course, newValue);
                    }
                }
            }
        }
    }

    /**
     * Accumulates the total number of attempts made for each MATH course that resulted in a DFW outcome.
     *
     * @param enrollments       the list of all student enrollments
     * @param dfwCountsByCourse a map from course ID to the number of DFW attempts
     */
    private void accumulateDFWCountByCourse(final List<EnrollmentRec> enrollments,
                                            final Map<String, Integer> dfwCountsByCourse) {

        for (final EnrollmentRec rec : enrollments) {
            if (rec.isTransfer() || rec.isApIbClep()) {
                continue;
            }

            String course = rec.course();
            if (course.startsWith("MATH") && rec.isDfw()) {

                if ("MATH180A3".equals(course)) {
                    course = "MATH157";
                } else if ("MATH180A4".equals(course)) {
                    course = "MATH159";
                } else if ("MATH180A5".equals(course)) {
                    course = "MATH156";
                } else if ("MATH181A1".equals(course)) {
                    course = "MATH116";
                } else if ("MATH345".equals(course)) {
                    course = "MATH340";
                }

                if (FOUNDATIONAL.contains(course)) {
                    final Integer currentValue = dfwCountsByCourse.get(course);
                    if (currentValue == null) {
                        dfwCountsByCourse.put(course, ONE);
                    } else {
                        final int newInt = currentValue.intValue() + 1;
                        final Integer newValue = Integer.valueOf(newInt);
                        dfwCountsByCourse.put(course, newValue);
                    }
                }
            }
        }
    }

    /**
     * Identifies the last MATH attempt with a DFW outcome and accumulates in a map.  NOTE that a student could take
     * multiple MATH courses with DFW outcomes in the same term, and we want to accumulate all that fall in the term in
     * which the last MATH DFW occurred.
     *
     * @param enrollments           the list of all student enrollments
     * @param countsByLastDFWCourse a map from course ID to the number of times it was the last MATH course with a DFW
     *                              result for a student
     */
    private void accumulateLastMathFailed(final List<EnrollmentRec> enrollments,
                                          final Map<String, Integer> countsByLastDFWCourse) {

        int lastPeriodWithDfw = 0;

        for (final EnrollmentRec rec : enrollments) {
            if (rec.isTransfer() || rec.isApIbClep()) {
                continue;
            }

            String course = rec.course();
            if (course.startsWith("MATH") && rec.isDfw()) {

                if ("MATH180A3".equals(course)) {
                    course = "MATH157";
                } else if ("MATH180A4".equals(course)) {
                    course = "MATH159";
                } else if ("MATH180A5".equals(course)) {
                    course = "MATH156";
                } else if ("MATH181A1".equals(course)) {
                    course = "MATH116";
                } else if ("MATH345".equals(course)) {
                    course = "MATH340";
                }

                if (FOUNDATIONAL.contains(course)) {
                    final int recPeriod = rec.academicPeriod();
                    if (recPeriod > lastPeriodWithDfw) {
                        lastPeriodWithDfw = recPeriod;
                    }
                }
            }
        }

        if (lastPeriodWithDfw > 0) {
            // Accumulate all DFW MATH courses in the term we found

            for (final EnrollmentRec rec : enrollments) {
                if (rec.isTransfer() || rec.isApIbClep()) {
                    continue;
                }

                String course = rec.course();
                if (course.startsWith("MATH") && rec.isDfw()) {

                    if ("MATH180A3".equals(course)) {
                        course = "MATH157";
                    } else if ("MATH180A4".equals(course)) {
                        course = "MATH159";
                    } else if ("MATH180A5".equals(course)) {
                        course = "MATH156";
                    } else if ("MATH181A1".equals(course)) {
                        course = "MATH116";
                    } else if ("MATH345".equals(course)) {
                        course = "MATH340";
                    }

                    if (FOUNDATIONAL.contains(course)) {
                        final int recPeriod = rec.academicPeriod();
                        if (recPeriod == lastPeriodWithDfw) {
                            final Integer currentValue = countsByLastDFWCourse.get(course);
                            if (currentValue == null) {
                                countsByLastDFWCourse.put(course, ONE);
                            } else {
                                final int newInt = currentValue.intValue() + 1;
                                final Integer newValue = Integer.valueOf(newInt);
                                countsByLastDFWCourse.put(course, newValue);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Gets the number of credits for a MATH course based on its ID.
     *
     * @param course the course ID
     * @return the number of credits
     */
    private static int getMathCreditsByCourse(final String course) {

        final int courseCredits;

        if ("MATH 117".equals(course)
            || "MATH118".equals(course)
            || "MATH124".equals(course)
            || "MATH125".equals(course)
            || "MATH126".equals(course)
            || "MATH116".equals(course) || "MATH181A1".equals(course)
            || "MATH151".equals(course)
            || "MATH152".equals(course)
            || "MATH158".equals(course)
            || "MATH192".equals(course)
            || "MATH384".equals(course)) {
            courseCredits = 1;
        } else if ("MATH229".equals(course)
                   || "MATH269".equals(course)
                   || "MATH235".equals(course)) {
            courseCredits = 2;
        } else if ("MATH127".equals(course)
                   || "MATH155".equals(course)
                   || "MATH156".equals(course) || "MATH180A5".equals(course)
                   || "MATH160".equals(course)
                   || "MATH161".equals(course)
                   || "MATH255".equals(course)
                   || "MATH256".equals(course)
                   || "MATH261".equals(course)
                   || "MATH271".equals(course)
                   || "MATH272".equals(course)
                   || "MATH340".equals(course)
                   || "MATH345".equals(course)
                   || "MATH348".equals(course)) {
            courseCredits = 4;
        } else {
            courseCredits = 3;
        }

        return courseCredits;
    }
}
