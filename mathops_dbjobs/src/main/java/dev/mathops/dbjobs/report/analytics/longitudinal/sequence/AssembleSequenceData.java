package dev.mathops.dbjobs.report.analytics.longitudinal.sequence;

import dev.mathops.commons.log.Log;
import dev.mathops.dbjobs.report.analytics.longitudinal.data.EnrollmentRec;
import dev.mathops.dbjobs.report.analytics.longitudinal.data.StudentTermRec;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class retrieves longitudinal data for students who participated in a course that is part of a math sequence and
 * who had a major that requires that sequence. All student term records are gathered for each such student through
 * their entire program, and their progress is analyzed to see whether they completed the sequence and the degree they
 * intended, or had to change degree programs or leave the university.
 *
 * <p>
 * This class assumes we already have queried, over a period of interest, the list of students who have taken any course
 * in a sequence, and we know their major.  This is used to build a list of students and to group them by which sequence
 * they need for their major at the time they took the first course in the sequence.
 */
public enum AssembleSequenceData {
    ;

    /**
     * Builds lists of students for each sequence.
     *
     * @param enrollments  a map from student ID to the list of all enrollments for that student
     * @param studentTerms a map from student ID to the list of all student term records for that student
     */
    public static void classifyStudents(final Map<String, ? extends List<EnrollmentRec>> enrollments,
                                        final Map<String, ? extends List<StudentTermRec>> studentTerms) {

        final Collection<String> studentIds = new HashSet<>(100000);

        final Set<String> enrollmentKeys = enrollments.keySet();
        studentIds.addAll(enrollmentKeys);

        final Set<String> termKeys = studentTerms.keySet();
        studentIds.addAll(termKeys);

        final Collection<String> programs = new TreeSet<>();

        for (final String studentId : studentIds) {
            final List<EnrollmentRec> stuEnrollments = enrollments.get(studentId);
            final List<StudentTermRec> stuTerms = studentTerms.get(studentId);

            if (stuEnrollments != null && stuTerms != null) {
                final String program = identifyProgram(stuEnrollments, stuTerms);

                if (program != null) {
                    programs.add(program);
                    final String sequence = identifySequence(program);
                }
            }
        }

        for (final String program : programs) {
            Log.fine("Found program: ", program);
        }
    }

    /**
     * Identifies the student's major at the time they first enrolled in a Math course.
     *
     * @param stuEnrollments the list of student enrollments
     * @param stuTerms       the list of student term records
     * @return the program; null if unable to determine
     */
    private static String identifyProgram(final Iterable<EnrollmentRec> stuEnrollments,
                                          final Iterable<StudentTermRec> stuTerms) {

        int earliestMathTerm = Integer.MAX_VALUE;
        for (final EnrollmentRec rec : stuEnrollments) {
            if (rec.course().startsWith("MATH")) {
                final int period = rec.academicPeriod();
                earliestMathTerm = Math.min(earliestMathTerm, period);
            }
        }

        String program = null;

        if (earliestMathTerm < Integer.MAX_VALUE) {
            for (final StudentTermRec rec : stuTerms) {
                if (rec.academicPeriod() == earliestMathTerm) {
                    program = rec.program();
                    break;
                }
            }
        }

        return program;
    }

    static final String M_117_118_124 = "M_117_118_124";
    static final String M_117_118_125 = "M_117_118_125";
    static final String M_117_118_124_125 = "M_117_118_124_125";
    static final String M_117_118_124_OR_120 = "M_117_118_124_OR_120";
    static final String M_117_118_124_141 = "M_117_118_124_141";
    static final String M_117_118_125_155 = "M_117_118_125_155";

    static final String M_118_124_125 = "M_118_124_125"; // Effectively M_117_118_124_125
    static final String M_124_125_126 = "M_124_125_126"; // Effectively M_117_118_124_125_126
    static final String M_124_125_126_155 = "M_124_125_126_155"; // Effectively M_117_118_124_125_126_155

    static final String M_141 = "M_141"; // Effectively M_117_118_141

    static final String M_141_OR_155_OR_160 = "M_141_OR_155_OR_160";

    static final String M_155 = "M_155"; // Effectively M_117_118_125_155
    static final String M_155_255 = "M_155_255";
    static final String M_155_255_OR_160_161 = "M_155_255_OR_160_161";
    static final String M_155_271_OR_155_161_OR_160_161_OR_160_271 = "M_155_271_OR_155_161_OR_160_161_OR_160_271";

    static final String M_155_OR_160 = "M_155_OR_160";

    static final String M_156_OR_160 = "M_156_OR_160";
    static final String M_156_256 = "M_156_256";
    static final String M_156_256_OR_160_161_OR_160_256 = "M_156_256_OR_160_161_OR_160_256";
    static final String M_156_256_OR_160_161_261 = "M_156_256_OR_160_161_261";

    static final String M_160 = "M_160";
    static final String M_160_161 = "M_160_161";
    static final String M_160_161_261 = "M_160_161_261";
    static final String M_160_161_261_OR_160_271 = "M_160_161_261_OR_160_271";
    static final String M_160_161_261_OR_160_271_272 = "M_160_161_261_OR_160_271_272";
    static final String M_160_161_261_340 = "M_160_161_261_340";

    static final String M3_117_118_120_124_141_155_160 = "M3_117_118_120_124_141_155_160";
    static final String M3_117_118_120_124_125_126_127_141_155_160 = "M3_117_118_120_124_125_126_127_141_155";
    static final String M3_118_124_125_126_155_160 = "M3_118_124_125_126_155_160";
    static final String M3_117_118_125_141 = "M3_117_118_125_141";

    /**
     * Identifies which math sequence this student's major requires.
     *
     * @param program a major code
     * @return the key for the sequence needed; null of major does not require a sequence
     */
    private static String identifySequence(final String program) {

        String sequence = null;

        // Programs that don't require a sequence:
        // AIMS, ANTH-BA, ANTH-ARCZ-BA, ANTH-BIOZ-BA, ANTH-CLTZ-BA, ANTH-DD-BA, ANTH-GRPZ-BA, ARTI-BA, ARTI-AREZ-BA,
        // ARTI-ARTZ-BA, ARTI-IVSZ-BA, ARTI-STDZ-BA, ARTM-BFA, ARTM-AREZ-BF, ARTM-DRAZ-BF, ARTM-ELAZ-BF, ARTM-FIBZ-BF,
        // ARTM-GRDZ-BF, ARTM-METZ-BF, ARTM-PHIZ-BF, ARTM-PNTZ-BF, ARTM-POTZ-BF, ARTM-PRTZ-BF, ARTM-SCLZ-BF,
        // BUSN-IMZ-MBA, CIVE_MS, CMST-BA, CMST-DD-BA, CMST-TCLZ-BA, CSOR, CSUR, CTED-UG, DANC-BFA, DANC-DEDZ-BF,
        // DNCE-BA, ECHE-BS, ENGL-BA, ENGL-CRWZ-BA, ENGL-ENEZ-BA, ENGL-LANZ-BA, ENGL-LINZ-BA, ENGL-LITZ-BA,
        // ENGL-WRIZ-BA, ENGL-WRLZ-BA, ETST-BA, ETST-COIZ-BA, ETST-RPRZ-BA, ETST-SOTZ-BA, ETST-WSTZ-BA, EXAD, EXCO,
        // EXGS, EXPL, EXPO, EXTC, FACS-BS, FACS-FACZ-BS, FACS-FCSZ-BS, FACS-IDSZ-BS, FCST-UG, FESA-DD-BS, FESV-DD-BS,
        // FSBI-BS, GEOG-BS, GRAD-UG, GUES-CEGR, GUES-CEUG, GUES-GATEWAY, HDFS-BS, HDFS-DD-BS, HDFS-DECZ-BS,
        // HDFS-DHDZ-BS, HDFS-DLEZ-BS, HDFS-DPHZ-BS, HDFS-DPIZ-BS, HDFS-ECPZ-BS, HDFS-HDEZ-BS, HDFS-LADZ-BS,
        // HDFS-LEPZ-BS, HDFS-PHPZ-BS, HDFS-PISZ-BS, HEMG-BS, HIST-BA, HIST-DPUZ-BA, HIST-GENZ-BA, HIST-LBAZ-BA,
        // HIST-LNGZ-BA, HIST-SBSZ-BA, HIST-SSTZ-BA, HSMG-BS, HSPS, ILAR-BA, ILAR-DD-BA, INEX-UG, INST-ASTZ-BA, INST-BA,
        // INST-EUSZ-BA, INST-GBLZ-BA, INST-LTSZ-BA, INST-MEAZ-BA, JAMC-BA, JAMC-DD-BA, JATC-BA, JATC-CMCZ-BA,
        // JATC-NWEZ-BA, JATC-PBRZ-BA, JATC-STCZ-BA, JATC-TNVZ-BA, LBAR-BA, LBAR-DD-BA, LBAR-ISTZ-BA, LBAR-SOSZ-BA,
        // LDA0, LDAR-BS, LLAC-BA, LLAC-LFRZ-BA, LLAC-LGEZ-BA, LLAC-LSPZ-BA, MUS0, MUSC-COMZ-BM, MUSC-MUEZ-BM,
        // MUSC-MUTZ-BM, MUSC-PERZ-BM, MUSI-BA, N2AG-AGSX-UG, N2AG-ASAX-UG, N2BU-BUAX-UG, N2BU-BUSC-UG, N2BU-BUST-UG,
        // N2BU-BUSX-UG, N2CA-UG, N2CP-CPSA-UG, N2CP-CPSC-UG, N2CP-CPST-UG, N2CP-CPSX-UG, N2EG-ENAX-UG, N2EG-ENGC-UG,
        // N2EG-ENGX-UG, N2LA-LBAX-UG, N2LA-LBOC-UG, N2LA-LBOT-UG, N2LA-LBOX-UG, N2LA-LBOY-UG, N2MA-MATA-UG,
        // N2NS-CPSX-UG, N2NS-LSHX-UG, N2NS-NSAX-UG, N2NS-NSCC-UG, N2NS-NSCX-UG, OPTJ, OPUN, PHIL-BA, PHIL-GNPZ-BA,
        // PHIL-GPRZ-BA, PHIL-PHAZ-BA, PHIL-PSAZ-BA , POLS-BA, POLS-DD-BA, POLS-EPAZ-BA, POLS-GPPZ-BA, POLS-ULPZ-BA,
        // PRFA-DANZ-BA, PRFA-THTZ-BA, RARM-BS, OCI-BA, OCI-CRCZ-BA, OCI-ENSZ-BA, OCI-GNSZ-BA, SOWK-BSW, SPCL-UG,
        // TCLI-UG-GN, TCLI-UG-MA, TCLN-UG-GN, TCLN-UG-MA, TCLN-UG-SS, THTR-BA, THTR-DIRZ-BA, THTR-DTHZ-BA,
        // THTR-GTRZ-BA, THTR-LDTZ-BA, THTR-MUSZ-BA, THTR-PDTZ-BA, THTR-PRFZ-BA, THTR-PWDZ-BA, THTR-SDSZ-BA,
        // THTR-SDTZ-BA, THTR-TDPZ-BA, UNAG, UNDL, UNHS, UNLA, USAR, USBS, USCS, USJC, VIPS-UG, WGST-BA

        if ("CTM0".equals(program)
            || "CTMG-BS".equals(program)
            || "FRRS-BS".equals(program)
            || "FRRS-FMGZ-BS".equals(program)
            || "FRRS-FRFZ-BS".equals(program)
            || "FRRS-RFMZ-BS".equals(program)
            || "FRST-BS".equals(program)
            || "FRST-FBUZ-BS".equals(program)
            || "FRST-FMGZ-BS".equals(program)
            || "FRST-FRFZ-BS".equals(program)
            || "RGEC-RAFZ-BS".equals(program)
            || "SOCR-APIZ-BS".equals(program)) {
            sequence = M_141;
        } else if ("AGBU-BS".equals(program)
                   || "AGBU-DD-BS".equals(program)
                   || "AGBU-AECZ-BS".equals(program)
                   || "AGEC-BS".equals(program)
                   || "AGEC-AGEZ-BS".equals(program)
                   || "AGEC-FRMZ-BS".equals(program)
                   || "AGEC-NREZ-BS".equals(program)) {
            sequence = M_117_118_124_141;
        } else if ("AGBU-FRCZ-BS".equals(program)
                   || "AGBU-FSSZ-BS".equals(program)
                   || "AGED-BS".equals(program)
                   || "APAM-BS".equals(program)
                   || "APAM-ADAZ-BS".equals(program)
                   || "APAM-MDSZ-BS".equals(program)
                   || "APAM-PDVZ-BS".equals(program)
                   || "ENHR-NALZ-BS".equals(program)
                   || "ENHR-TURZ-BS".equals(program)
                   || "ENRE-BS".equals(program)
                   || "ENRE-DD-BS".equals(program)
                   || "EXHF".equals(program)
                   || "EXLA".equals(program)
                   || "EXNR".equals(program)
                   || "EXOM".equals(program)
                   || "HDNR-BS".equals(program)
                   || "HORT-BS".equals(program)
                   || "HORT-CEHZ-BS".equals(program)
                   || "HORT-DHBZ-BS".equals(program)
                   || "HORT-FLOZ-BS".equals(program)
                   || "HORT-HBMZ-BS".equals(program)
                   || "HORT-HFCZ-BS".equals(program)
                   || "HORT-HTHZ-BS".equals(program)
                   || "HORT-VTEZ-BS".equals(program)
                   || "IAD0".equals(program)
                   || "IARD-BS".equals(program)
                   || "IARD-IADZ-BS".equals(program)
                   || "IARD-IPRZ-BS".equals(program)
                   || "INT0".equals(program)
                   || "INTD-BS".equals(program)
                   || "LSBM-BS".equals(program)
                   || "NAFS-BS".equals(program)
                   || "NAFS-DNMZ-BS".equals(program)
                   || "NAFS-DTCZ-BS".equals(program)
                   || "NAFS-FSNZ-BS".equals(program)
                   || "NRRT-BS".equals(program)
                   || "NRRT-EVCZ-BS".equals(program)
                   || "NRRT-GLTZ-BS".equals(program)
                   || "NRRT-NRTZ-BS".equals(program)
                   || "NRRT-PPAZ-BS".equals(program)
                   || "NRTM-DNRZ-BS".equals(program)
                   || "NRTM-GLTZ-BS".equals(program)
                   || "NRTM-NRTZ-BS".equals(program)
                   || "PSYC-BS".equals(program)
                   || "PSYC-ADCZ-BS".equals(program)
                   || "PSYC-CCPZ-BS".equals(program)
                   || "PSYC-GDSZ-BS".equals(program)
                   || "PSYC-GPSZ-BS".equals(program)
                   || "PSYC-IOPZ-BS".equals(program)
                   || "PSYC-MBBZ-BS".equals(program)
                   || "SOCR-BS".equals(program)
                   || "SOCR-APMZ-BS".equals(program)
                   || "SOCR-BMBZ-BS".equals(program)
                   || "SOCR-EVSZ-BS".equals(program)
                   || "SOCR-ISCZ-BS".equals(program)
                   || "SOCR-PBTZ-BS".equals(program)
                   || "SOCR-SAMZ-BS".equals(program)
                   || "SOCR-SESZ-BS".equals(program)
                   || "SOCR-SRNZ-BS".equals(program)
                   || "UNLS".equals(program)
                   || "UNNR".equals(program)) {
            sequence = M_117_118_124;
        } else if ("ANIM-BS".equals(program)
                   || "BUSA-BS".equals(program)
                   || "BUSA-ACCZ-BS".equals(program)
                   || "BUSA-FINZ-BS".equals(program)
                   || "BUSA-FPLZ-BS".equals(program)
                   || "BUSA-HRMZ-BS".equals(program)
                   || "BUSA-INSZ-BS".equals(program)
                   || "BUSA-MINZ-BS".equals(program)
                   || "BUSA-MKTZ-BS".equals(program)
                   || "BUSA-OIMZ-BS".equals(program)
                   || "BUSA-REAZ-BS".equals(program)
                   || "BUSA-SCMZ-BS".equals(program)
                   || "EQSC-BS".equals(program)
                   || "EVHL-BS".equals(program)
                   || "OPBU".equals(program)
                   || "UNBU".equals(program)
                   || "USBU".equals(program)) {
            sequence = M3_117_118_120_124_125_126_127_141_155_160;
        } else if ("AGED-AGLZ-BS".equals(program)) {
            sequence = M_117_118_124_OR_120;
        } else if ("AGED-TDLZ-BS".equals(program)) {
            sequence = M3_117_118_120_124_141_155_160;
        } else if ("APCT-BS".equals(program)
                   || "APCT-CHFZ-BS".equals(program) // ???
                   || "APCT-CPEZ-BS".equals(program)
                   || "APCT-CPTZ-BS".equals(program)) {
            sequence = M_160;
        } else if ("FRRS-FRBZ-BS".equals(program)
                   || "FRST-FRBZ-BS".equals(program)
                   || "NERO-BS".equals(program)
                   || "NERO-BCNZ-BS".equals(program)
                   || "NEUR-BS".equals(program)
                   || "SOCR-SLCZ-BS".equals(program)
                   || "SOCR-SOEZ-BS".equals(program)) {
            sequence = M_155;
        } else if ("APCT-HCCZ-BS".equals(program)
                   || "CPSC-BS".equals(program)
                   || "GEOL-BS".equals(program)
                   || "GEOL-EVGZ-BS".equals(program)
                   || " GEOL-GEOZ-BS".equals(program)) {
            sequence = M_160_161;
        } else if ("BCHM-BS".equals(program)
                   || "BCHM-ASBZ-BS".equals(program)
                   || "BCHM-GBCZ-BS".equals(program)
                   || "BCHM-HMSZ-BS".equals(program)
                   || "BCHM-PPHZ-BS".equals(program)
                   || "EXPE".equals(program)
                   || "NSCI-GLEZ-BS".equals(program)
                   || "NSCI-PHSZ-BS".equals(program)
                   || "WSSS-WSSZ-BS".equals(program)) {
            sequence = M_155_255_OR_160_161;
        } else if ("BIOM-APHZ-BS".equals(program)
                   || "BLSC-BS".equals(program)
                   || "BLSC-BLSZ-BS".equals(program)
                   || "BLSC-BTNZ-BS".equals(program)
                   || "ECSS-BS".equals(program)
                   || "FWCB-BS".equals(program)
                   || "FWCB-CNVZ-BS".equals(program)
                   || "FWCB-FASZ-BS".equals(program)
                   || "FWCB-WDBZ-BS".equals(program)
                   || "NSCI-BS".equals(program)
                   || "NSCI-BLEZ-BS".equals(program)
                   || "WDBI-BS".equals(program)
                   || "WRSC-BS".equals(program)
                   || "WSSS-BS".equals(program)
                   || "WSSS-WSDZ-BS".equals(program)
                   || "ZOOL-BS".equals(program)) {
            sequence = M_155_OR_160;
        } else if ("BIOM-BS".equals(program)
                   || "BIOM-EPHZ-BS".equals(program)
                   || "BIOM-MIDZ-BS".equals(program)
                   || "MICR-BS".equals(program)) {
            sequence = M3_118_124_125_126_155_160;
        } else if ("CBEG-BS".equals(program)
                   || "CBEG-AVMZ-BS".equals(program)
                   || "CBEG-BIMZ-BS".equals(program)
                   || "CBEG-BMEC-BS".equals(program)
                   || "CBEG-MLMZ-BS".equals(program)
                   || "CBEG-SSEZ-BS".equals(program)
                   || "CIVE-BS".equals(program)
                   || "CIVE-CIVZ-BS".equals(program)
                   || "CIVE-SWRZ-BS".equals(program)
                   || "CPEG-BS".equals(program)
                   || "CPEG-AESZ-BS".equals(program)
                   || "CPEG-BMEP-BS".equals(program)
                   || "CPEG-EISZ-BS".equals(program)
                   || "CPEG-NDTZ-BS".equals(program)
                   || "CPEG-VICZ-BS".equals(program)
                   || "EGOP".equals(program)
                   || "EGSC-BS".equals(program)
                   || "EGSC-EGPZ-BS".equals(program)
                   || "EGSC-IEIZ-BS".equals(program)
                   || "EGSC-SPEZ-BS".equals(program)
                   || "EGSC-TCEZ-BS".equals(program)
                   || "ELEG-BS".equals(program)
                   || "ELEG-ASPZ-BS".equals(program)
                   || "ELEG-BMEE-BS".equals(program)
                   || "ELEG-BMEL-BS".equals(program)
                   || "ELEG-ELEZ-BS".equals(program)
                   || "ELEG-LOEZ-BS".equals(program)
                   || "ENVE-BS".equals(program)
                   || "ENVE-ECOZ-BS".equals(program)
                   || "ENVE-ENVZ-BS".equals(program)
                   || "GEOL-GPYZ-BS".equals(program)
                   || "GEOL-HYDZ-BS".equals(program)
                   || "MATH-BS".equals(program)
                   || "MATH-ALSZ-BS".equals(program)
                   || "MATH-AMTZ-BS".equals(program)
                   || "MATH-GNMZ-BS".equals(program)
                   || "MATH-MTOZ-BS".equals(program)
                   || "MATH-STAZ-BS".equals(program)
                   || "MECH-BS".equals(program)
                   || "MECH-ACEZ-BS".equals(program)
                   || "MECH-ADMZ-BS".equals(program)
                   || "MECH-BMEM-BS".equals(program)
                   || "PHYS-BS".equals(program)
                   || "PHYS-APPZ-BS".equals(program)
                   || "PHYS-PHYZ-BS".equals(program)
                   || "UNEG".equals(program)
                   || "USEG".equals(program)) {
            sequence = M_160_161_261_340;
        } else if ("CHEM-BS".equals(program)
                   || "CHEM-ECHZ-BS".equals(program)
                   || "CHEM-FCHZ-BS".equals(program)
                   || "CHEM-HSCZ-BS".equals(program)
                   || "CHEM-MTRZ-BS".equals(program)
                   || "NSCI-CHEZ-BS".equals(program)) {
            sequence = M_155_271_OR_155_161_OR_160_161_OR_160_271;
        } else if ("CHEM-ACSZ-BS".equals(program)
                   || "CHEM-NACZ-BS".equals(program)) {
            sequence = M_160_161_261_OR_160_271_272;
        } else if ("CHEM-SCHZ-BS".equals(program)) {
            sequence = M_160_161_261_OR_160_271;
        } else if ("CPSC-AIMZ-BS".equals(program)
                   || "MATH-CPMZ-BS".equals(program)) {
            sequence = M_156_256_OR_160_161_OR_160_256;
        } else if ("CPSC-CPSZ-BS".equals(program)
                   || "CPSC-CSYZ-BS".equals(program)
                   || "CPSC-DCSZ-BS".equals(program) // ???
                   || "CPSC-DNSZ-BS".equals(program) // ???
                   || "CPSC-HCCZ-BS".equals(program)
                   || "CPSC-NSCZ-BS".equals(program)
                   || "CPSC-SEGZ-BS".equals(program)) {
            sequence = M_156_OR_160;
        } else if ("DSCI-BS".equals(program)
                   || "DSCI-CSCZ-BS".equals(program)
                   || "DSCI-ECNZ-BS".equals(program)
                   || "DSCI-MATZ-BS".equals(program)
                   || "DSCI-NEUZ-BS".equals(program)
                   || "DSCI-STSZ-BS".equals(program)) {
            sequence = M_156_256;
        } else if ("ECON-BA".equals(program)
                   || "ECON-DD-BA".equals(program)
                   || "WSSS-WSUZ-BS".equals(program)) {
                        sequence = M_141_OR_155_OR_160;
        } else if ("ENHR-BS".equals(program)
                   || "ENHR-LDAZ-BS".equals(program)
                   || "ENHR-LNBZ-BS".equals(program)
                   || "LAND-LDAZ-BS".equals(program)
                   || "LAND-NALZ-BS".equals(program)
                   || "LAND-TURZ-BS".equals(program)
                   || "NAFS-FSYZ-BS".equals(program)
                   || "NAFS-NFTZ-BS".equals(program)
                   || "NRMG-BS".equals(program)) {
            sequence = M_117_118_125;
        } else if ("FMST-BS".equals(program)) {
            sequence = M_117_118_124_125;
        } else if ("FRRS-RCMZ-BS".equals(program)
                   || "RECO-BS".equals(program)
                   || "RGEC-BS".equals(program)
                   || "RGEC-CRMZ-BS".equals(program)
                   || "RGEC-RSEZ-BS".equals(program)) {
            sequence = M3_117_118_125_141;
        } else if ("HAES-BS".equals(program)
                   || "HAES-EXSZ-BS".equals(program)
                   || "HAES-HPRZ-BS".equals(program)
                   || "HAES-SPMZ-BS".equals(program)) {
            sequence = M_118_124_125;
        } else if ("HORT-HOSZ-BS".equals(program)) {
            sequence = M_124_125_126;
        } else if ("MATH-MTEZ-BS".equals(program)
                   || "NSCI-PHEZ-BS".equals(program)
                   || "STAT-GSTZ-BS".equals(program)) {
            sequence = M_160_161_261;
        } else if ("NAFS-NUSZ-BS".equals(program)
                   || "NAFS-PHNZ-BS".equals(program)) {
            sequence = M_117_118_125_155;
        } else if ("NERO-CMNZ-BS".equals(program)) {
            sequence = M_155_255;
        } else if ("SOCR-PBGZ-BS".equals(program)) {
            sequence = M_124_125_126_155;
        } else if ("STAT-BS".equals(program) || "STAT-MSTZ-BS".equals(program)) {
            sequence = M_156_256_OR_160_161_261;
        }

        return sequence;
    }
}

