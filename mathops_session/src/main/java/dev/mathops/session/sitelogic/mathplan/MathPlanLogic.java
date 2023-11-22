package dev.mathops.session.sitelogic.mathplan;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.TemporalUtils;
import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.DbContext;
import dev.mathops.db.cfg.ContextMap;
import dev.mathops.db.cfg.DbProfile;
import dev.mathops.db.cfg.ESchemaUse;
import dev.mathops.db.ifaces.ILiveCsuCredit;
import dev.mathops.db.ifaces.ILiveTransferCredit;
import dev.mathops.db.rawlogic.AbstractLogicModule;
import dev.mathops.db.rawlogic.RawFfrTrnsLogic;
import dev.mathops.db.rawlogic.RawStmathplanLogic;
import dev.mathops.db.rawlogic.RawStmpeLogic;
import dev.mathops.db.rawlogic.RawStudentLogic;
import dev.mathops.db.rawrecord.RawCourse;
import dev.mathops.db.rawrecord.RawFfrTrns;
import dev.mathops.db.rawrecord.RawRecordConstants;
import dev.mathops.db.rawrecord.RawStmathplan;
import dev.mathops.db.rawrecord.RawStmpe;
import dev.mathops.db.rawrecord.RawStudent;
import dev.mathops.db.rec.LiveCsuCredit;
import dev.mathops.db.rec.LiveTransferCredit;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.mathplan.data.CourseGroup;
import dev.mathops.session.sitelogic.mathplan.data.Major;
import dev.mathops.session.sitelogic.mathplan.data.MajorMathRequirement;
import dev.mathops.session.sitelogic.mathplan.data.RequiredPrereq;
import dev.mathops.session.sitelogic.mathplan.data.StudentData;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Logic module for the welcome web site. This class is thread-safe and may be queried by multiple servlet threads.
 *
 * <p>
 * LAST REVIEW AGAINST CIM/Catalog: April 15, 2021
 */
public final class MathPlanLogic {

    // /** The exam ID of the Math Placement Tool. */
    // private static final String PLACEMENT_TOOL = "POOOO"; 

    // /** The exam ID of the proctored Math Challenge Exam. */
    // private static final String CHALLENGE_EXAM = "PPPPP"; 

    /** The prefix for catalog URLs. */
    private static final String CAT = "https://catalog.colostate.edu/general-catalog/colleges/";

    /** A course ID. */
    private static final String M_101 = "M 101";

    /** A course ID. */
    private static final String M_105 = "M 105";

    /** A course ID. */
    private static final String S_100 = "S 100";

    /** A course ID. */
    private static final String S_201 = "S 201";

    /** A course ID. */
    private static final String S_204 = "S 204";

    /** A course ID. */
    private static final String M_141 = "M 141";

    /** A course ID. */
    private static final String M_151 = "M 151";

    /** A course ID. */
    private static final String M_152 = "M 152";

    /** A course ID. */
    private static final String M_155 = "M 155";

    /** A course ID. */
    private static final String M_156 = "M 156";

    /** A course ID. */
    private static final String M_157 = "M 157";

    /** A course ID. */
    private static final String M_158 = "M 158";

    /** A course ID. */
    private static final String M_159 = "M 159";

    /** A course ID. */
    private static final String M_160 = "M 160";

    /** A course ID. */
    private static final String M_161 = "M 161";

    /** A course ID. */
    private static final String M_192 = "M 192";

    /** A course ID. */
    private static final String M_229 = "M 229";

    /** A course ID. */
    private static final String M_230 = "M 230";

    /** A course ID. */
    private static final String M_235 = "M 235";

    /** A course ID. */
    private static final String M_255 = "M 255";

    /** A course ID. */
    private static final String M_256 = "M 256";

    /** A course ID. */
    private static final String M_261 = "M 261";

    /** A course ID. */
    private static final String M_271 = "M 271";

    /** A course ID. */
    private static final String M_272 = "M 272";

    /** A course ID. */
    private static final String M_301 = "M 301";

    /** A course ID. */
    private static final String M_317 = "M 317";

    /** A course ID. */
    private static final String M_331 = "M 331";

    /** A course ID. */
    private static final String M_332 = "M 332";

    /** A course ID. */
    private static final String M_340 = "M 340";

    /** A course ID. */
    private static final String M_345 = "M 345";

    /** A course ID. */
    private static final String M_348 = "M 348";

    /** A course ID. */
    private static final String M_360 = "M 360";

    /** A course ID. */
    private static final String M_366 = "M 366";

    /** A course ID. */
    private static final String M_369 = "M 369";

    /** A course ID. */
    private static final String D_369 = "D 369";

    /** A course ID. */
    private static final String M_384 = "M 384";

    /** A course ID. */
    private static final String M_405 = "M 405";

    /** A course ID. */
    private static final String M_417 = "M 417";

    /** A course ID. */
    private static final String M_418 = "M 418";

    /** A course ID. */
    private static final String M_419 = "M 419";

    /** A course ID. */
    private static final String M_425 = "M 425";

    /** A course ID. */
    private static final String M_430 = "M 430";

    /** A course ID. */
    private static final String M_435 = "M 435";

    /** A course ID. */
    private static final String M_450 = "M 450";

    /** A course ID. */
    private static final String M_451 = "M 451";

    /** A course ID. */
    private static final String M_455 = "M 455";

    /** A course ID. */
    private static final String M_460 = "M 460";

    /** A course ID. */
    private static final String M_466 = "M 466";

    /** A course ID. */
    private static final String M_467 = "M 467";

    /** A course ID. */
    private static final String M_469 = "M 469";

    /** A course ID. */
    private static final String M_470 = "M 470";

    /** A course ID. */
    private static final String M_472 = "M 472";

    /** A course ID. */
    private static final String M_474 = "M 474";

    /** A course ID. */
    private static final String M_476 = "M 476";

    /** A course ID. */
    private static final String M_484 = "M 484";

    /** A course ID. */
    private static final String M_487 = "M 487";

    /** A course ID. */
    private static final String M_495 = "M 495";

    /** A course ID. */
    private static final String M_498 = "M 498";

    /** A course group ID. */
    private static final String AGED3 = "AGED3";

    /** A course group ID. */
    private static final String ANIM3 = "ANIM3";

    /** A course group ID. */
    private static final String BIOL3 = "BIOL3";

    /** A course group ID. */
    private static final String BUSA3 = "BUSA3";

    /** A course group ID. */
    public static final String AUCC3 = "AUCC3";

    /** A course group ID. */
    private static final String FRRS3 = "FRRS3";

    /** A course group ID. */
    public static final String AUCC3SOC = "AUCC3SOC";

    /** A course group ID. */
    private static final String AUCC2 = "AUCC2";

    /** A course group ID. */
    private static final String CALC = "CALC";

    /** A course group ID. */
    private static final String CALC1BIO = "CALC1BIO";

    /** A course group ID. */
    private static final String CALC2BIO = "CALC2BIO";

    /** A course group ID. */
    private static final String CALC2CHM = "CALC2CHM";

    /** A course group ID. */
    private static final String CALC3CHM = "CALC3CHM";

    /** A course group ID. */
    private static final String CALC1CS = "CALC1CS";

    /** A course group ID. */
    private static final String LINALG = "LINALG";

    /** A course group ID. */
    private static final String LINALG369 = "LINALG369";

    /** A course group ID. */
    private static final String ODE = "ODE";

    /** A course group ID. */
    private static final String MATH2 = "MATH2";

    /** A course group ID. */
    private static final String MATH4 = "MATH4";

    /** A course group ID. */
    private static final String STAT1 = "STAT1";

    /** A course group ID. */
    private static final String STAT2 = "STAT2";

    /** A college ID. */
    private static final String AG = "AG";

    /** A college ID. */
    private static final String BU = "BU";

    /** A college ID. */
    private static final String EG = "EG";

    /** A college ID. */
    private static final String HS = "HS";

    /** A college ID. */
    private static final String LA = "LA";

    /** A college ID. */
    private static final String NR = "NR";

    /** A college ID. */
    private static final String NS = "NS";

    /** A college ID. */
    private static final String VM = "VM";

    /** The profile ID used to store the "majors of interest" response. */
    public static final String MAJORS_PROFILE = "WLCM1";

    /** The profile ID used to store the short representation of recommendations. */
    public static final String PLAN_PROFILE = "WLCM2";

    /** The profile ID used to store the "this is only a recommendation" response. */
    public static final String ONLY_RECOM_PROFILE = "WLCM3";

    /** The profile ID used to store "existing work" responses. */
    public static final String EXISTING_PROFILE = "WLCM4";

    /** The profile ID used to store "intentions" responses. */
    public static final String INTENTIONS_PROFILE = "WLCM5";

    /** The profile ID used to record when student accesses review materials. */
    public static final String REVIEWED_PROFILE = "WLCM6";

    /** The profile ID used to record when student checks their placement results. */
    public static final String CHECKED_RESULTS_PROFILE = "WLCM7";

    /** Object on which to synchronize member variable access. */
    private final Object synch;

    /** The database profile this module will use. */
    private final DbProfile dbProfile;

    /** The cached colleges. */
    private Map<String, String> colleges;

    /** The cached courses. */
    private Map<String, RawCourse> courses;

    /** The cached course groups. */
    private Map<String, CourseGroup> courseGroups;

    /** The cached list of majors and their requirements (sorted by major). */
    private Map<Major, MajorMathRequirement> majors;

    /** The subset of cached majors that require only 3 credits of AUCC. */
    private List<Major> majorsNeedingAUCC;

    /** The subset of cached majors that require nothing beyond precalculus. */
    private List<Major> majorsNeedingPrecalc;

    /** The subset of cached majors that require courses through a Calculus I. */
    private List<Major> majorsNeedingCalc1;

    /** The subset of cached majors that require courses beyond Calculus II. */
    private List<Major> majorsNeedingMore;

    /** The cached list of required prerequisites (map from course to its prerequisites). */
    private Map<String, List<RequiredPrereq>> requiredPrereqs;

    /** A cache of student data. */
    private final LinkedHashMap<String, StudentData> studentDataCache;

    /**
     * Constructs a new {@code MathPlanLogic}.
     *
     * @param theDbProfile the database profile this module will use
     */
    public MathPlanLogic(final DbProfile theDbProfile) {

        this.synch = new Object();
        this.dbProfile = theDbProfile;
        this.studentDataCache = new LinkedHashMap<>(1000);
    }

    /**
     * Gets a map from the course numbers used in {@code RawCourse} objects to the corresponding full course objects.
     *
     * @return the map
     */
    public Map<String, RawCourse> getCourses() {

        synchronized (this.synch) {
            if (this.courses == null) {
                this.courses = new HashMap<>();

                // No-prereq AUCC-1B courses

                this.courses.put(M_101, new RawCourse(M_101, Integer.valueOf(0), //
                        "Math in the Social Sciences (3 credits)",
                        Integer.valueOf(3), "N", "MATH 101", null,
                        "N", "N"));
                this.courses.put(M_105, new RawCourse(M_105, Integer.valueOf(0), //
                        "Patterns of Phenomena (3 credits)",
                        Integer.valueOf(3), "N", "MATH 105", null,
                        "N", "N"));
                this.courses.put(S_100, new RawCourse(S_100, Integer.valueOf(0), //
                        "Statistical Literacy (3 credits)",
                        Integer.valueOf(3), "N", "STAT 100", null,
                        "N", "N"));
                this.courses.put(S_201, new RawCourse(S_201, Integer.valueOf(0), //
                        "General Statistics (3 credits)",
                        Integer.valueOf(3), "N", "STAT 201", null,
                        "N", "N"));
                this.courses.put(S_204, new RawCourse(S_204, Integer.valueOf(0), //
                        "Statistics With Business Applications (3 credits)",
                        Integer.valueOf(3), "N", "STAT 204", null,
                        "N", "N"));

                // Precalculus

                this.courses.put(RawRecordConstants.M117,
                        new RawCourse(RawRecordConstants.M117, Integer.valueOf(4), //
                                "College Algebra in Context I (1 credit)",
                                Integer.valueOf(1), "Y", "MATH 117", null,
                                "N", "Y"));
                this.courses.put(RawRecordConstants.M118,
                        new RawCourse(RawRecordConstants.M118, Integer.valueOf(4), //
                                "College Algebra in Context II (1 credit)",
                                Integer.valueOf(1), "Y", "MATH 118", null,
                                "N", "Y"));
                this.courses.put(RawRecordConstants.M124,
                        new RawCourse(RawRecordConstants.M124, Integer.valueOf(4), //
                                "Logarithmic and Exponential Functions (1 credit)",
                                Integer.valueOf(1), "Y", "MATH 124", null,
                                "N", "Y"));
                this.courses.put(RawRecordConstants.M125,
                        new RawCourse(RawRecordConstants.M125, Integer.valueOf(4), //
                                "Numerical Trigonometry (1 credit)",
                                Integer.valueOf(1), "Y", "MATH 125", null,
                                "N", "Y"));
                this.courses.put(RawRecordConstants.M126,
                        new RawCourse(RawRecordConstants.M126, Integer.valueOf(4), //
                                "Analytic Trigonometry (1 credit)",
                                Integer.valueOf(1), "Y", "MATH 126", null,
                                "N", "Y"));
                this.courses.put(RawRecordConstants.M127,
                        new RawCourse(RawRecordConstants.M127, Integer.valueOf(0), //
                                "Precalculus (4 credit)",
                                Integer.valueOf(4), "Y", "MATH 127", null,
                                "N", "N"));

                // Other Math courses

                this.courses.put(M_141, new RawCourse(M_141, Integer.valueOf(0), //
                        "Calculus in Management Sciences (3 credits)",
                        Integer.valueOf(3), "N", "MATH 141", null,
                        "N", "N"));
                this.courses.put(M_151, new RawCourse(M_151, Integer.valueOf(0), //
                        "Mathematical Algorithms in Matlab I (1 credit)",
                        Integer.valueOf(1), "N", "MATH 151", null,
                        "N", "N"));
                this.courses.put(M_152, new RawCourse(M_152, Integer.valueOf(0), //
                        "Mathematical Algorithms in Maple (1 credit)",
                        Integer.valueOf(1), "N", "MATH 152", null,
                        "N", "N"));
                this.courses.put(M_155, new RawCourse(M_155, Integer.valueOf(0), //
                        "Calculus for Biological Scientists I (4 credits)",
                        Integer.valueOf(4), "N", "MATH 155", null,
                        "N", "N"));
                this.courses.put(M_156, new RawCourse(M_156, Integer.valueOf(0), //
                        "Mathematics for Computational Science I (4 credits)",
                        Integer.valueOf(4), "N", "MATH 156", null,
                        "N", "N"));
                this.courses.put(M_157, new RawCourse(M_157, Integer.valueOf(0), //
                        "One Year Calculus IA (3 credits)",
                        Integer.valueOf(3), "N", "MATH 157", null,
                        "N", "N"));
                this.courses.put(M_158, new RawCourse(M_158, Integer.valueOf(0), //
                        "Mathematical Algorithms in C (1 credit)",
                        Integer.valueOf(1), "N", "MATH 158", null,
                        "N", "N"));
                this.courses.put(M_159, new RawCourse(M_159, Integer.valueOf(0), //
                        "One Year Calculus IB (3 credits)",
                        Integer.valueOf(3), "N", "MATH 159", null,
                        "N", "N"));
                this.courses.put(M_160, new RawCourse(M_160, Integer.valueOf(0), //
                        "Calculus for Physical Scientists I (4 credits)",
                        Integer.valueOf(4), "N", "MATH 160", null,
                        "N", "N"));
                this.courses.put(M_161, new RawCourse(M_161, Integer.valueOf(0), //
                        "Calculus for Physical Scientists II (4 credits)",
                        Integer.valueOf(4), "N", "MATH 161", null,
                        "N", "N"));
                this.courses.put(M_192, new RawCourse(M_192, Integer.valueOf(0), //
                        "First Year Seminar in Mathematical Sciences (1 credit)",
                        Integer.valueOf(1), "N", "MATH 192", null,
                        "N", "N"));
                this.courses.put(M_229, new RawCourse(M_229, Integer.valueOf(0), //
                        "Matrices and Linear Equations (2 credits)",
                        Integer.valueOf(2), "N", "MATH 229", null,
                        "N", "N"));
                this.courses.put(M_230, new RawCourse(M_230, Integer.valueOf(0), //
                        "Discrete Mathematics for Educators (3 credits)",
                        Integer.valueOf(3), "N", "MATH 230", null,
                        "N", "N"));
                this.courses.put(M_235, new RawCourse(M_235, Integer.valueOf(0), //
                        "Introduction to Mathematical Reasoning (2 credits)",
                        Integer.valueOf(2), "N", "MATH 235", null,
                        "N", "N"));
                this.courses.put(M_255, new RawCourse(M_255, Integer.valueOf(0), //
                        "Calculus for Biological Scientists II (4 credits)",
                        Integer.valueOf(4), "N", "MATH 255", null,
                        "N", "N"));
                this.courses.put(M_256, new RawCourse(M_256, Integer.valueOf(0), //
                        "Mathematics for Computational Science II (4 credits)",
                        Integer.valueOf(4), "N", "MATH 256", null,
                        "N", "N"));
                this.courses.put(M_261, new RawCourse(M_261, Integer.valueOf(0), //
                        "Calculus for Physical Scientists III (4 credits)",
                        Integer.valueOf(4), "N", "MATH 261", null,
                        "N", "N"));
                this.courses.put(M_271, new RawCourse(M_271, Integer.valueOf(0), //
                        "Applied Mathematics for Chemists I (4 credits)",
                        Integer.valueOf(4), "N", "MATH 271", null,
                        "N", "N"));
                this.courses.put(M_272, new RawCourse(M_272, Integer.valueOf(0), //
                        "Applied Mathematics for Chemists II (4 credits)",
                        Integer.valueOf(4), "N", "MATH 272", null,
                        "N", "N"));
                this.courses.put(M_301, new RawCourse(M_301, Integer.valueOf(0), //
                        "Introduction to Combinatorial Theory (3 credits)",
                        Integer.valueOf(3), "N", "MATH 301", null,
                        "N", "N"));

                this.courses.put(M_317, new RawCourse(M_317, Integer.valueOf(0), //
                        "Advanced Calculus of One Variable (3 credits)",
                        Integer.valueOf(3), "N", "MATH 317", null,
                        "N", "N"));
                this.courses.put(M_331, new RawCourse(M_331, Integer.valueOf(0), //
                        "Introduction to Mathematical Modeling (3 credits)",
                        Integer.valueOf(3), "N", "MATH 331", null,
                        "N", "N"));
                this.courses.put(M_332, new RawCourse(M_332, Integer.valueOf(0), //
                        "Partial Differential Equations  Credits (3 credits)",
                        Integer.valueOf(3), "N", "MATH 332", null,
                        "N", "N"));
                this.courses.put(M_340, new RawCourse(M_340, Integer.valueOf(0), //
                        "Intro to Ordinary Differential Equations (4 credits)",
                        Integer.valueOf(4), "N", "MATH 340", null,
                        "N", "N"));
                this.courses.put(M_345, new RawCourse(M_345, Integer.valueOf(0), //
                        "Differential Equations (4 credits)",
                        Integer.valueOf(4), "N", "MATH 345", null,
                        "N", "N"));
                this.courses.put(M_348, new RawCourse(M_348, Integer.valueOf(0), //
                        "Theory of Population and Evolutionary Ecology (4 credits)",
                        Integer.valueOf(4), "N", "MATH 348", null,
                        "N", "N"));
                this.courses.put(M_360, new RawCourse(M_360, Integer.valueOf(0), //
                        "Mathematics of Information Security (3 credits)",
                        Integer.valueOf(3), "N", "MATH 360", null,
                        "N", "N"));
                this.courses.put(M_366, new RawCourse(M_366, Integer.valueOf(0), //
                        "Introduction to Abstract Algebra (3 credits)",
                        Integer.valueOf(3), "N", "MATH 366", null,
                        "N", "N"));
                this.courses.put(M_369, new RawCourse(M_369, Integer.valueOf(0), //
                        "Linear Algebra I (3 credits)",
                        Integer.valueOf(3), "N", "MATH 369", null,
                        "N", "N"));
                this.courses.put(D_369, new RawCourse(D_369, Integer.valueOf(0), //
                        "Linear Algebra for Data Science (3 credits)",
                        Integer.valueOf(3), "N", "DSCI 369", null,
                        "N", "N"));
                this.courses.put(M_384, new RawCourse(M_384, Integer.valueOf(0), //
                        "Supervised College Teaching (1 credit)",
                        Integer.valueOf(1), "N", "MATH 384", null,
                        "N", "N"));
                this.courses.put(M_405, new RawCourse(M_405, Integer.valueOf(0), //
                        "Introduction to Number Theory (3 credits)",
                        Integer.valueOf(3), "N", "MATH 405", null,
                        "N", "N"));
                this.courses.put(M_417, new RawCourse(M_417, Integer.valueOf(0), //
                        "Advanced Calculus I (3 credits)",
                        Integer.valueOf(3), "N", "MATH 417", null,
                        "N", "N"));
                this.courses.put(M_418, new RawCourse(M_418, Integer.valueOf(0), //
                        "Advanced Calculus II (3 credits)",
                        Integer.valueOf(3), "N", "MATH 418", null,
                        "N", "N"));
                this.courses.put(M_419, new RawCourse(M_419, Integer.valueOf(0), //
                        "Introduction to Complex Variables (3 credits)",
                        Integer.valueOf(3), "N", "MATH 419", null,
                        "N", "N"));
                this.courses.put(M_425, new RawCourse(M_425, Integer.valueOf(0), //
                        "History of Mathematics (3 credits)",
                        Integer.valueOf(3), "N", "MATH 425", null,
                        "N", "N"));
                this.courses.put(M_430, new RawCourse(M_430, Integer.valueOf(0), //
                        "Fourier and Wavelet Analysis with Apps (3 credits)",
                        Integer.valueOf(3), "N", "MATH 430", null,
                        "N", "N"));
                this.courses.put(M_435, new RawCourse(M_435, Integer.valueOf(0), //
                        "Projects in Applied Mathematics (3 credits)",
                        Integer.valueOf(3), "N", "MATH 435", null,
                        "N", "N"));
                this.courses.put(M_450, new RawCourse(M_450, Integer.valueOf(0), //
                        "Introduction to Numerical Analysis I (3 credits)",
                        Integer.valueOf(3), "N", "MATH 450", null,
                        "N", "N"));
                this.courses.put(M_451, new RawCourse(M_451, Integer.valueOf(0), //
                        "Introduction to Numerical Analysis II (3 credits)",
                        Integer.valueOf(3), "N", "MATH 451", null,
                        "N", "N"));
                this.courses.put(M_455, new RawCourse(M_455, Integer.valueOf(0), //
                        "Mathematics in Biology and Medicine (3 credits)",
                        Integer.valueOf(3), "N", "MATH 455", null,
                        "N", "N"));
                this.courses.put(M_460, new RawCourse(M_460, Integer.valueOf(0), //
                        "Information and Coding Theory (3 credits)",
                        Integer.valueOf(3), "N", "MATH 460", null,
                        "N", "N"));
                this.courses.put(M_466, new RawCourse(M_466, Integer.valueOf(0), //
                        "Abstract Algebra I (3 credits)",
                        Integer.valueOf(3), "N", "MATH 466", null,
                        "N", "N"));
                this.courses.put(M_467, new RawCourse(M_467, Integer.valueOf(0), //
                        "Abstract Algebra II (3 credits)",
                        Integer.valueOf(3), "N", "MATH 467", null,
                        "N", "N"));
                this.courses.put(M_469, new RawCourse(M_469, Integer.valueOf(0), //
                        "Linear Algebra II (3 credits)",
                        Integer.valueOf(3), "N", "MATH 469", null,
                        "N", "N"));
                this.courses.put(M_470, new RawCourse(M_470, Integer.valueOf(0), //
                        "Euclidean and Non-Euclidean Geometry (3 credits)",
                        Integer.valueOf(3), "N", "MATH 470", null,
                        "N", "N"));
                this.courses.put(M_472, new RawCourse(M_472, Integer.valueOf(0), //
                        "Introduction to Topology (3 credits)",
                        Integer.valueOf(3), "N", "MATH 472", null,
                        "N", "N"));
                this.courses.put(M_474, new RawCourse(M_474, Integer.valueOf(0), //
                        "Introduction to Differential Geometry (3 credits)",
                        Integer.valueOf(3), "N", "MATH 474", null,
                        "N", "N"));
                this.courses.put(M_476, new RawCourse(M_476, Integer.valueOf(0), //
                        "Topics in Mathematics (3 credits)",
                        Integer.valueOf(3), "N", "MATH 476", null,
                        "N", "N"));
                this.courses.put(M_484, new RawCourse(M_484, Integer.valueOf(0), //
                        "Supervised College Teaching (1-3 credits)",
                        Integer.valueOf(-1), "N", "MATH 484", null,
                        "N", "N"));
                this.courses.put(M_487, new RawCourse(M_487, Integer.valueOf(0), //
                        "Internship (1-16 credits)",
                        Integer.valueOf(-1), "N", "MATH 487", null,
                        "N", "N"));
                this.courses.put(M_495, new RawCourse(M_495, Integer.valueOf(0), //
                        "Independent Study (1-18 credits)",
                        Integer.valueOf(-1), "N", "MATH 495", null,
                        "N", "N"));
                this.courses.put(M_498, new RawCourse(M_498, Integer.valueOf(0), //
                        "Undergraduate Research in Mathematics (1-3 credits)",
                        Integer.valueOf(-1), "N", "MATH 498", null,
                        "N", "N"));
            }
        }

        return Collections.unmodifiableMap(this.courses);
    }

//    /**
//     * Gets a map from college code to college name.
//     *
//     * @return the map
//     */
//    public Map<String, String> getColleges() {
//
//        synchronized (this.synch) {
//            if (this.colleges == null) {
//                final Map<String, String> map = new HashMap<>(8);
//
//                map.put(AG, "College of Agricultural Sciences");
//                map.put(BU, "College of Business");
//                map.put(EG, "Walter Scott Jr. College of Engineering");
//                map.put(HS, "College of Health and Human Sciences");
//                map.put(LA, "College of Liberal Arts");
//                map.put(NR, "Warner College of Natural Resources");
//                map.put(NS, "College of Natural Sciences");
//                map.put(VM, "College of Veterinary Medicine and Biomedical Sciences");
//
//                this.colleges = new TreeMap<>(map);
//            }
//        }
//
//        return Collections.unmodifiableMap(this.colleges);
//    }

    /**
     * Retrieves the complete list of course options.
     *
     * @return a map from group ID to the course group
     */
    public Map<String, CourseGroup> getCourseGroups() {

        synchronized (this.synch) {
            if (this.courseGroups == null) {

                this.courseGroups = new HashMap<>(50);
                this.courseGroups.put(M_101, new CourseGroup(M_101, null, M_101, M_101));
                this.courseGroups.put(M_105, new CourseGroup(M_105, null, M_105, M_105));
                this.courseGroups.put(S_100, new CourseGroup(S_100, null, S_100, S_100));
                this.courseGroups.put(S_201, new CourseGroup(S_201, null, S_201, S_201));
                this.courseGroups.put(S_204, new CourseGroup(S_204, null, S_204, S_204));
                this.courseGroups.put(RawRecordConstants.M117, new CourseGroup(RawRecordConstants.M117, null,
                        RawRecordConstants.M117, RawRecordConstants.M117));
                this.courseGroups.put(RawRecordConstants.M118, new CourseGroup(RawRecordConstants.M118, null,
                        RawRecordConstants.M118, RawRecordConstants.M118));
                this.courseGroups.put(RawRecordConstants.M120, new CourseGroup(RawRecordConstants.M120, null,
                        RawRecordConstants.M120, RawRecordConstants.M120));
                this.courseGroups.put(RawRecordConstants.M124, new CourseGroup(RawRecordConstants.M124, null,
                        RawRecordConstants.M124, RawRecordConstants.M124));
                this.courseGroups.put(RawRecordConstants.M125, new CourseGroup(RawRecordConstants.M125, null,
                        RawRecordConstants.M125, RawRecordConstants.M125));
                this.courseGroups.put(RawRecordConstants.M126, new CourseGroup(RawRecordConstants.M126, null,
                        RawRecordConstants.M126, RawRecordConstants.M126));
                this.courseGroups.put(RawRecordConstants.M127, new CourseGroup(RawRecordConstants.M127, null,
                        RawRecordConstants.M127, RawRecordConstants.M127));
                this.courseGroups.put(M_141, new CourseGroup(M_141, null, M_141, M_141));
                this.courseGroups.put(M_151, new CourseGroup(M_151, null, M_151, M_151));
                this.courseGroups.put(M_152, new CourseGroup(M_152, null, M_152, M_152));
                this.courseGroups.put(M_155, new CourseGroup(M_155, null, M_155, M_155));
                this.courseGroups.put(M_156, new CourseGroup(M_156, null, M_156, M_156));
                this.courseGroups.put(M_157, new CourseGroup(M_157, null, M_157, M_157));
                this.courseGroups.put(M_158, new CourseGroup(M_158, null, M_158, M_158));
                this.courseGroups.put(M_159, new CourseGroup(M_159, null, M_159, M_159));
                this.courseGroups.put(M_160, new CourseGroup(M_160, null, M_160, M_160));
                this.courseGroups.put(M_161, new CourseGroup(M_161, null, M_161, M_161));
                this.courseGroups.put(M_192, new CourseGroup(M_192, null, M_192, M_192));
                this.courseGroups.put(M_229, new CourseGroup(M_229, null, M_229, M_229));
                this.courseGroups.put(M_230, new CourseGroup(M_230, null, M_230, M_230));
                this.courseGroups.put(M_235, new CourseGroup(M_235, null, M_235, M_235));
                this.courseGroups.put(M_255, new CourseGroup(M_255, null, M_255, M_255));
                this.courseGroups.put(M_256, new CourseGroup(M_256, null, M_256, M_256));
                this.courseGroups.put(M_261, new CourseGroup(M_261, null, M_261, M_261));
                this.courseGroups.put(M_271, new CourseGroup(M_271, null, M_271, M_271));
                this.courseGroups.put(M_272, new CourseGroup(M_272, null, M_272, M_272));
                this.courseGroups.put(M_301, new CourseGroup(M_301, null, M_301, M_301));
                this.courseGroups.put(M_317, new CourseGroup(M_317, null, M_317, M_317));
                this.courseGroups.put(M_331, new CourseGroup(M_331, null, M_331, M_331));
                this.courseGroups.put(M_332, new CourseGroup(M_332, null, M_332, M_332));
                this.courseGroups.put(M_340, new CourseGroup(M_340, null, M_340, M_340));
                this.courseGroups.put(M_345, new CourseGroup(M_345, null, M_345, M_345));
                this.courseGroups.put(M_348, new CourseGroup(M_348, null, M_348, M_348));
                this.courseGroups.put(M_360, new CourseGroup(M_360, null, M_360, M_360));
                this.courseGroups.put(M_366, new CourseGroup(M_366, null, M_366, M_366));
                this.courseGroups.put(M_369, new CourseGroup(M_369, null, M_369, M_369));
                this.courseGroups.put(M_384, new CourseGroup(M_384, null, M_384, M_384));
                this.courseGroups.put(M_425, new CourseGroup(M_425, null, M_425, M_425));
                this.courseGroups.put(M_435, new CourseGroup(M_435, null, M_435, M_435));
                this.courseGroups.put(M_450, new CourseGroup(M_450, null, M_450, M_450));
                this.courseGroups.put(M_451, new CourseGroup(M_451, null, M_451, M_451));
                this.courseGroups.put(M_460, new CourseGroup(M_460, null, M_460, M_460));
                this.courseGroups.put(M_470, new CourseGroup(M_470, null, M_470, M_470));
                this.courseGroups.put(M_495, new CourseGroup(M_495, null, M_495, M_495));

                this.courseGroups.put(AGED3, new CourseGroup(AGED3, Integer.valueOf(3), RawRecordConstants.M124,
                        RawRecordConstants.M117, RawRecordConstants.M118, RawRecordConstants.M124,
                        M_141, M_155, M_160));

                this.courseGroups.put(ANIM3, new CourseGroup(ANIM3, Integer.valueOf(3), RawRecordConstants.M124,
                        RawRecordConstants.M117, RawRecordConstants.M118, RawRecordConstants.M124,
                        RawRecordConstants.M125, RawRecordConstants.M126, M_141, M_155));

                this.courseGroups.put(BIOL3, new CourseGroup(BIOL3, Integer.valueOf(3), RawRecordConstants.M124,
                        RawRecordConstants.M117, RawRecordConstants.M118, RawRecordConstants.M124,
                        RawRecordConstants.M125, RawRecordConstants.M126, M_155, M_160));

                this.courseGroups.put(BUSA3, new CourseGroup(BUSA3, Integer.valueOf(3), RawRecordConstants.M124,
                        RawRecordConstants.M117, RawRecordConstants.M118, RawRecordConstants.M124,
                        RawRecordConstants.M125, RawRecordConstants.M126, M_141));

                this.courseGroups.put(AUCC3, new CourseGroup(AUCC3, Integer.valueOf(3), M_101, M_101, S_100, M_105,
                        S_201, S_204, RawRecordConstants.M117, RawRecordConstants.M118, RawRecordConstants.M124,
                        RawRecordConstants.M125, RawRecordConstants.M126, RawRecordConstants.M127, M_141, M_155, M_160,
                        M_161, M_255));

                this.courseGroups.put(FRRS3, new CourseGroup(FRRS3, Integer.valueOf(3), RawRecordConstants.M125,
                        RawRecordConstants.M117, RawRecordConstants.M118, RawRecordConstants.M125, M_141));

                this.courseGroups.put(AUCC3SOC, new CourseGroup(AUCC3SOC, Integer.valueOf(3), M_101, M_101, S_100,
                        M_105, S_201, S_204, RawRecordConstants.M117, RawRecordConstants.M118,
                        RawRecordConstants.M124, RawRecordConstants.M125, RawRecordConstants.M126,
                        RawRecordConstants.M127, M_141, M_155, M_160, M_161, M_255));

                this.courseGroups.put(AUCC2, new CourseGroup(AUCC2, Integer.valueOf(2), M_101, M_101, S_100, M_105,
                        M_105, S_201, S_204, RawRecordConstants.M117, RawRecordConstants.M118,
                        RawRecordConstants.M124, RawRecordConstants.M125, RawRecordConstants.M126,
                        RawRecordConstants.M127, M_141, M_155, M_160, M_161, M_255));

                this.courseGroups.put(CALC, new CourseGroup(CALC, null, M_141, M_141, M_155, M_160));

                this.courseGroups.put(CALC1BIO, new CourseGroup(CALC1BIO, null, M_155, M_155, M_160));

                this.courseGroups.put(CALC2BIO, new CourseGroup(CALC2BIO, null, M_255, M_255, M_161));

                this.courseGroups.put(CALC2CHM, new CourseGroup(CALC2CHM, null, M_161, M_161, M_271));

                this.courseGroups.put(CALC3CHM, new CourseGroup(CALC3CHM, null, M_261, M_261, M_272));

                this.courseGroups.put(CALC1CS, new CourseGroup(CALC1CS, null, M_156, M_156, M_160));

                this.courseGroups.put(LINALG, new CourseGroup(LINALG, null, M_229, M_229, M_369));

                this.courseGroups.put(LINALG369, new CourseGroup(LINALG369, null, M_369, M_369, D_369));

                this.courseGroups.put(ODE, new CourseGroup(ODE, null, M_340, M_340, M_345));

                this.courseGroups.put(MATH2, new CourseGroup(MATH2, null, M_360, M_360, M_366));

                this.courseGroups.put(MATH4, new CourseGroup(MATH4, null, M_417, M_417, M_418, M_466, M_467));

                this.courseGroups.put(STAT1, new CourseGroup(STAT1, null, M_301, M_301, M_317, M_331, M_340, M_345,
                        M_360, M_450, M_469));

                this.courseGroups.put(STAT2, new CourseGroup(STAT2, null, M_430, M_430, M_450, M_451, M_469));
            }
        }

        return Collections.unmodifiableMap(this.courseGroups);
    }

    /**
     * Gets the major with a specified program code.
     *
     * @param programCode the program code
     * @return the major; {@code null} if none matches the program code
     */
    public Major getMajor(final String programCode) {

        final Map<Major, MajorMathRequirement> allMajors = getMajors();
        Major result = null;

        for (final Major major : allMajors.keySet()) {
            if (major.programCode.equals(programCode)) {
                result = major;
                break;
            }
        }

        return result;
    }

    /**
     * Gets the majors and their math requirements for the first semester.
     *
     * @return a map from a major to a list of its requirements (each list entry is a string with a course number or a
     *         comma-separated list of course option keys. Keys ending in "!" are marked as "critical", keys ending in
     *         '.' are marked as "recommended").
     */
    public Map<Major, MajorMathRequirement> getMajors() {

        synchronized (this.synch) {
            if (this.majors == null) {
                final Map<Major, MajorMathRequirement> map = new HashMap<>(50);

                // *** Last reviewed May 26, 2022 ***

                // ================================
                // College of Agricultural Sciences
                // ================================

                // *** Major in Agricultural Biology (with three concentrations)

                final Major mAGBI = new Major(1090, "AGBI-BS",
                        Boolean.TRUE, "Agricultural Biology",
                        CAT + "agricultural-sciences/"
                                + "agricultural-biology/agricultural-biology-major");
                final MajorMathRequirement rAGBI = new MajorMathRequirement("AGBI-BS")
                        .setSemesterCourses("M 117!,M 118!,M 124!,M 125!", null, M_155);
                map.put(mAGBI, rAGBI);

                final Major mAGBIENTZ = new Major(1091, "AGBI-ENTZ-BS",
                        Boolean.TRUE, "Agricultural Biology",
                        "Entomology",
                        CAT + "agricultural-sciences/agricultural-biology/"
                                + "agricultural-biology-major-entomology-concentration/");
                final MajorMathRequirement rAGBIENTZ = //
                        new MajorMathRequirement("AGBI-ENTZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124!,M 125!", null, M_155);
                map.put(mAGBIENTZ, rAGBIENTZ);

                final Major mAGBIPLPZ = new Major(1092, "AGBI-PLPZ-BS",
                        Boolean.TRUE, "Agricultural Biology",
                        "Plant Pathology",
                        CAT + "agricultural-sciences/agricultural-biology/"
                                + "agricultural-biology-major-plant-pathology-concentration/");
                final MajorMathRequirement rAGBIPLPZ = //
                        new MajorMathRequirement("AGBI-PLPZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124!,M 125!", null, M_155);
                map.put(mAGBIPLPZ, rAGBIPLPZ);

                final Major mAGBIWEEZ = new Major(1093, "AGBI-WEEZ-BS",
                        Boolean.TRUE, "Agricultural Biology",
                        "Weed Science",
                        CAT + "agricultural-sciences/agricultural-biology/"
                                + "agricultural-biology-major-weed-science-concentration/");
                final MajorMathRequirement rAGBIWEEZ = //
                        new MajorMathRequirement("AGBI-WEEZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124!,M 125!", null, M_155);
                map.put(mAGBIWEEZ, rAGBIWEEZ);

                // *** Major in Agricultural Business (with two concentrations)

                final Major mAGBU = new Major(1000, "AGBU-BS",
                        Boolean.TRUE, "Agricultural Business",
                        CAT + "agricultural-sciences/"
                                + "agricultural-resource-economics/business-major/");
                final MajorMathRequirement rAGBU = new MajorMathRequirement("AGBU-BS")
                        .setSemesterCourses("M 117!,M 118!,M 124", null, M_141);
                map.put(mAGBU, rAGBU);

                final Major mAGBUAECZ = new Major(1001, "AGBU-AECZ-BS",
                        Boolean.TRUE, "Agricultural Business",
                        "Agricultural Economics",
                        CAT + "agricultural-sciences/agricultural-resource-economics/"
                                + "business-agricultural-economics-concentration/");
                final MajorMathRequirement rAGBUAECZ = //
                        new MajorMathRequirement("AGBU-AECZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124", null, M_141);
                map.put(mAGBUAECZ, rAGBUAECZ);

                final Major mAGBUFRCZ = new Major(1002, "AGBU-FRCZ-BS",
                        Boolean.TRUE, "Agricultural Business",
                        "Farm and Ranch Management",
                        CAT + "agricultural-sciences/agricultural-resource-economics/"
                                + "business-farm-ranch-management-concentration/");
                final MajorMathRequirement rAGBUFRCZ = //
                        new MajorMathRequirement("AGBU-FRCZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124", null, M_141);
                map.put(mAGBUFRCZ, rAGBUFRCZ);

                final Major mAGBUFSSZ = new Major(1003, "AGBU-FSSZ-BS",
                        Boolean.TRUE, "Agricultural Business",
                        "Food Systems",
                        CAT + "agricultural-sciences/agricultural-resource-economics/"
                                + "business-major/food-systems-concentration/");
                final MajorMathRequirement rAGBUFSSZ = //
                        new MajorMathRequirement("AGBU-FSSZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124", null, M_141);
                map.put(mAGBUFSSZ, rAGBUFSSZ);

                // *** Major in Agricultural Education (with two concentrations)

                final Major mAGED = new Major(1010, "AGED-BS", Boolean.FALSE,
                        "Agricultural Education",
                        CAT + "agricultural-sciences/agricultural-resource-economics/"
                                + "education-major/");
                final MajorMathRequirement rAGED = new MajorMathRequirement("AGED-BS")
                        .setSemesterCourses("AGED3!", null, null);
                map.put(mAGED, rAGED);

                final Major mAGEDAGLZ = new Major(1011, "AGED-AGLZ-BS",
                        Boolean.TRUE, "Agricultural Education",
                        "Agricultural Literacy",
                        CAT + "agricultural-sciences/agricultural-resource-economics/"
                                + "education-literacy-concentration/");
                final MajorMathRequirement rAGEDAGLZ = //
                        new MajorMathRequirement("AGED-AGLZ-BS")
                                .setSemesterCourses("M 117,M 118,M 124", null, null);
                map.put(mAGEDAGLZ, rAGEDAGLZ);

                final Major mAGEDTDLZ = new Major(1012, "AGED-TDLZ-BS",
                        Boolean.TRUE, "Agricultural Education",
                        "Teacher Development",
                        CAT + "agricultural-sciences/agricultural-resource-economics/"
                                + "education-teacher-development-concentration/");
                final MajorMathRequirement rAGEDTDLZ = //
                        new MajorMathRequirement("AGED-TDLZ-BS")
                                .setSemesterCourses(null, "AGED3!", null);
                map.put(mAGEDTDLZ, rAGEDTDLZ);

                // *** Major in Animal Science

                final Major mANIM = new Major(1020, "ANIM-BS", Boolean.TRUE,
                        "Animal Science",
                        CAT + "agricultural-sciences/animal-sciences/"
                                + "animal-science-major/");
                final MajorMathRequirement rANIM = new MajorMathRequirement("ANIM-BS")
                        .setSemesterCourses("ANIM3!", null, null);
                map.put(mANIM, rANIM);

                // *** Major in Environmental and Natural Resource Economics

                final Major mENRE = new Major(1030, "ENRE-BS", Boolean.TRUE,
                        "Environmental and Natural Resource Economics",
                        CAT + "agricultural-sciences/agricultural-resource-economics/"
                                + "environmental-natural-major/");
                final MajorMathRequirement rENRE = new MajorMathRequirement("ENRE-BS")
                        .setSemesterCourses("M 117!,M 118,M 124", null, M_141);
                map.put(mENRE, rENRE);

                // *** Major in Environmental Horticulture (with three concentrations)

                final Major mENHR = new Major(1040, "ENHR-BS", Boolean.FALSE,
                        "Environmental Horticulture",
                        CAT + "agricultural-sciences/horticulture-landscape-architecture/"
                                + "environmental-horticulture-major/");
                final MajorMathRequirement rENHR = new MajorMathRequirement("ENHR-BS")
                        .setSemesterCourses("M 117!,M 118!", null, null);
                map.put(mENHR, rENHR);

                // DEACTIVATED (commented to preserve what the historical number represents):

                // final Major mENHRLNBZ = new Major( 1041, "ENHR-LNBZ-BS", 
                // Boolean.TRUE, "Environmental Horticulture", 
                // "Landscape Business", 
                // CAT + "agricultural-sciences/horticulture-landscape-architecture/" 
                // + "environmental-major-business-concentration/"); 
                // final MajorMathRequirement rENHRLDAZ = //
                // new MajorMathRequirement("ENHR-LNBZ-BS") 
                // .setSemesterCourses("M 117!,M 118!,M 125!", null, null); 
                // map.put(mENHRLNBZ, rENHRLDAZ);

                final Major mENHRLDAZ = new Major(1042, "ENHR-LDAZ-BS",
                        Boolean.TRUE, "Environmental Horticulture",
                        "Landscape Design and Contracting",
                        CAT + "agricultural-sciences/horticulture-landscape-architecture/"
                                + "environmental-major-design-contracting-concentration/");
                final MajorMathRequirement rENHRLNBZ = //
                        new MajorMathRequirement("ENHR-LDAZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 125!", null, null);
                map.put(mENHRLDAZ, rENHRLNBZ);

                final Major mENHRNALZ = new Major(1043, "ENHR-NALZ-BS",
                        Boolean.TRUE, "Environmental Horticulture",
                        "Nursery and Landscape Management",
                        CAT + "agricultural-sciences/horticulture-landscape-architecture/"
                                + "environmental-major-nursery-management-concentration/");
                final MajorMathRequirement rENHRNALZ = //
                        new MajorMathRequirement("ENHR-NALZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124!", null, null);
                map.put(mENHRNALZ, rENHRNALZ);

                final Major mENHRTURZ = new Major(1044, "ENHR-TURZ-BS",
                        Boolean.TRUE, "Environmental Horticulture",
                        "Turf Management",
                        CAT + "agricultural-sciences/horticulture-landscape-architecture/"
                                + "environmental-major-turf-management-concentration/");
                final MajorMathRequirement rENHRTURZ = //
                        new MajorMathRequirement("ENHR-TURZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124!", null, null);
                map.put(mENHRTURZ, rENHRTURZ);

                // *** Major in Equine Science

                final Major mEQSC = new Major(1050, "EQSC-BS", Boolean.TRUE,
                        "Equine Science",
                        CAT + "agricultural-sciences/animal-sciences/"
                                + "equine-science-major/");
                final MajorMathRequirement rEQSC = new MajorMathRequirement("EQSC-BS")
                        .setSemesterCourses(ANIM3, null, null);
                map.put(mEQSC, rEQSC);

                // *** Major in Horticulture (with five concentrations)

                final Major mHORT = new Major(1060, "HORT-BS", Boolean.FALSE,
                        "Horticulture",
                        CAT + "agricultural-sciences/horticulture-landscape-architecture/"
                                + "horticulture-major/");
                final MajorMathRequirement rHORT = new MajorMathRequirement("HORT-BS")
                        .setSemesterCourses("M 117!,M 118!,M 124", null, null);
                map.put(mHORT, rHORT);

                final Major mHORTCEHZ = new Major(1066, "HORT-CEHZ-BS",
                        Boolean.TRUE, "Horticulture",
                        "Controlled Environmental Horticulture",
                        CAT + "agricultural-sciences/horticulture-landscape-architecture/"
                                + "horticulture-major-controlled-environment-horticulture-concentration/");
                final MajorMathRequirement rHORTCEHZ = //
                        new MajorMathRequirement("HORT-CEHZ-BS")
                                .setSemesterCourses("M 117!,M 118,M 124", null, null);
                map.put(mHORTCEHZ, rHORTCEHZ);

                // DEACTIVATED (commented to preserve what the historical number represents):

                // final Major mHORTFLOZ = new Major(1061, "HORT-FLOZ-BS", 
                // Boolean.TRUE, "Horticulture", 
                // "Floriculture", 
                // CAT + "agricultural-sciences/horticulture-landscape-architecture/" 
                // + "horticulture-major-floriculture-concentration/"); 
                // final MajorMathRequirement rHORTFLOZ = //
                // new MajorMathRequirement("HORT-FLOZ-BS") 
                // .setSemesterCourses("M 117!,M 118,M 124", null, null); 
                // map.put(mHORTFLOZ, rHORTFLOZ);

                final Major mHORTHBMZ = new Major(1062, "HORT-HBMZ-BS",
                        Boolean.TRUE, "Horticulture",
                        "Horticultural Business Management",
                        CAT + "agricultural-sciences/horticulture-landscape-architecture/"
                                + "horticulture-major-business-management-concentration/");
                final MajorMathRequirement rHORTHBMZ = //
                        new MajorMathRequirement("HORT-HBMZ-BS")
                                .setSemesterCourses("M 117!,M 118,M 124", null, null);
                map.put(mHORTHBMZ, rHORTHBMZ);

                final Major mHORTHFCZ = new Major(1063, "HORT-HFCZ-BS",
                        Boolean.TRUE, "Horticulture",
                        "Horticultural Food Crops",
                        CAT + "agricultural-sciences/horticulture-landscape-architecture/"
                                + "horticulture-major-food-crops-concentration/");
                final MajorMathRequirement rHORTHFCZ = //
                        new MajorMathRequirement("HORT-HFCZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124!", null, null);
                map.put(mHORTHFCZ, rHORTHFCZ);

                final Major mHORTHOSZ = new Major(1064, "HORT-HOSZ-BS",
                        Boolean.TRUE, "Horticulture",
                        "Horticultural Science",
                        CAT + "agricultural-sciences/horticulture-landscape-architecture/"
                                + "horticulture-major-science-concentration/");
                final MajorMathRequirement rHORTHOSZ = //
                        new MajorMathRequirement("HORT-HOSZ-BS")
                                .setSemesterCourses("M 124,M 125", RawRecordConstants.M126, M_155);
                map.put(mHORTHOSZ, rHORTHOSZ);

                // DEACTIVATED (commented to preserve what the historical number represents):

                // final Major mHORTHTHZ = new Major(1065, "HORT-HTHZ-BS", 
                // Boolean.TRUE, "Horticulture", 
                // "Horticultural Therapy", 
                // CAT + "agricultural-sciences/horticulture-landscape-architecture/" 
                // + "horticulture-major-therapy-concentration/"); 
                // final MajorMathRequirement rHORTHTHZ = //
                // new MajorMathRequirement("HORT-HTHZ-BS") 
                // .setSemesterCourses("M 117!,M 118,M 124", null, null); 
                // map.put(mHORTHTHZ, rHORTHTHZ);

                // *** Major in Landscape Architecture

                final Major mLDAR = new Major(1070, "LDAR-BS", Boolean.TRUE,
                        "Landscape Architecture",
                        CAT + "agricultural-sciences/horticulture-landscape-architecture/"
                                + "landscape-architecture-major/");
                final MajorMathRequirement rLDAR = new MajorMathRequirement("LDAR-BS")
                        .setSemesterCourses(AUCC2, RawRecordConstants.M126, null);
                map.put(mLDAR, rLDAR);

                // *** Major in Soil and Crop Sciences (with six concentrations)

                final Major mSOCR = new Major(1080, "SOCR-BS", Boolean.TRUE,
                        "Soil and Crop Sciences",
                        CAT + "agricultural-sciences/soil-crop-sciences/"
                                + "soil-crop-sciences-major/");
                final MajorMathRequirement rSOCR = new MajorMathRequirement("SOCR-BS")
                        .setSemesterCourses("M 117!,M 118!,M 124!", null, null);
                map.put(mSOCR, rSOCR);

                // DEACTIVATED (commented to preserve what the historical number represents):

                // final Major mSOCRAPMZ = new Major(1081, "SOCR-APMZ-BS", 
                // Boolean.TRUE, "Soil and Crop Sciences", 
                // "Agronomic Production Management", 
                // CAT + "agricultural-sciences/soil-crop-sciences/" 
                // + "soil-crop-sciences-major-agronomic-production-" 
                // + "management-concentration/"); 
                // final MajorMathRequirement rSOCRAPMZ = //
                // new MajorMathRequirement("SOCR-APMZ-BS") 
                // .setSemesterCourses("M 117!,M 118!,M 124!", null, null); 
                // map.put(mSOCRAPMZ, rSOCRAPMZ);

                // DEACTIVATED (commented to preserve what the historical number represents):

                // final Major mSOCRAPIZ = new Major(1082, "SOCR-APIZ-BS", 
                // Boolean.TRUE, "Soil and Crop Sciences", 
                // "Applied Information Technology", 
                // CAT + "agricultural-sciences/soil-crop-sciences/" 
                // + "soil-crop-sciences-major-applied-information-" 
                // + "technology-concentration/"); 
                // final MajorMathRequirement rSOCRAPIZ = //
                // new MajorMathRequirement("SOCR-APIZ-BS") 
                // .setSemesterCourses("M 117!,M 118!,M 124!", null, 
                // M_141);
                // map.put(mSOCRAPIZ, rSOCRAPIZ);

                // DEACTIVATED (commented to preserve what the historical number represents):

                // final Major mSOCRISCZ = new Major(1083, "SOCR-ISCZ-BS", 
                // Boolean.TRUE, "Soil and Crop Sciences", 
                // "International Soil and Crop Sciences", 
                // CAT + "agricultural-sciences/soil-crop-sciences/" 
                // + "soil-crop-sciences-international-concentration/"); 
                // final MajorMathRequirement rSOCRISCZ = //
                // new MajorMathRequirement("SOCR-ISCZ-BS") 
                // .setSemesterCourses("M 117!,M 118!,M 124!", null, null); 
                // map.put(mSOCRISCZ, rSOCRISCZ);

                // DEACTIVATED (commented to preserve what the historical number represents):

                // final Major mSOCRPBGZ = new Major(1084, "SOCR-PBGZ-BS", 
                // Boolean.TRUE, "Soil and Crop Sciences", 
                // "Plant Biotechnology, Genetics, and Breeding", 
                // CAT + "agricultural-sciences/soil-crop-sciences/" 
                // + "soil-crop-sciences-major-plant-biotechnology-" 
                // + "genetics-breeding-concentration/"); 
                // final MajorMathRequirement rSOCRPBGZ = //
                // new MajorMathRequirement("SOCR-PBGZ-BS") 
                // .setSemesterCourses("M 124!,M 125!", 
                // "M 126,M 155", null); 
                // map.put(mSOCRPBGZ, rSOCRPBGZ);

                // DEACTIVATED (commented to preserve what the historical number represents):

                // final Major mSOCRSOEZ = new Major(1085, "SOCR-SOEZ-BS", 
                // Boolean.TRUE, "Soil and Crop Sciences", 
                // "Soil Ecology", 
                // CAT + "agricultural-sciences/soil-crop-sciences/" 
                // + "soil-crop-sciences-major-ecology-concentration/"); 
                // final MajorMathRequirement rSOCRSOEZ = //
                // new MajorMathRequirement("SOCR-SOEZ-BS") 
                // .setSemesterCourses("M 155!", null, null); 
                // map.put(mSOCRSOEZ, rSOCRSOEZ);

                // DEACTIVATED (commented to preserve what the historical number represents):

                // final Major mSOCRSRNZ = new Major(1086, "SOCR-SRNZ-BS", 
                // Boolean.TRUE, "Soil and Crop Sciences", 
                // "Soil Restoration and Conservation", 
                // CAT + "agricultural-sciences/soil-crop-sciences/" 
                // + "soil-crop-sciences-major-restoration-conservation-" 
                // + "concentration/"); 
                // final MajorMathRequirement rSOCRSRNZ = //
                // new MajorMathRequirement("SOCR-SRNZ-BS") 
                // .setSemesterCourses("M 117!,M 118!,M 124!", null, null); 
                // map.put(mSOCRSRNZ, rSOCRSRNZ);

                final Major mSOCRPBTZ = new Major(1087, "SOCR-PBTZ-BS",
                        Boolean.TRUE, "Soil and Crop Sciences",
                        "Plant Biotechnology",
                        CAT + "agricultural-sciences/soil-crop-sciences/"
                                + "soil-crop-sciences-major-plant-biotechnology-concentration/");
                final MajorMathRequirement rSOCRPBTZ = //
                        new MajorMathRequirement("SOCR-PBTZ-BS")
                                .setSemesterCourses("M 117,M 118", RawRecordConstants.M124,
                                        RawRecordConstants.M125);
                map.put(mSOCRPBTZ, rSOCRPBTZ);

                final Major mSOCRSESZ = new Major(1088, "SOCR-SESZ-BS",
                        Boolean.TRUE, "Soil and Crop Sciences",
                        "Soil Science and Environmental Solutions",
                        CAT + "agricultural-sciences/soil-crop-sciences/"
                                + "soil-crop-sciences-major-science-environmental-solutions-concentration/");
                final MajorMathRequirement rSOCRSESZ = //
                        new MajorMathRequirement("SOCR-SESZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124!", null, null);
                map.put(mSOCRSESZ, rSOCRSESZ);

                final Major mSOCRSAMZ = new Major(1089, "SOCR-SAMZ-BS",
                        Boolean.TRUE, "Soil and Crop Sciences",
                        "Sustainable Agricultural Management",
                        CAT + "agricultural-sciences/soil-crop-sciences/"
                                + "soil-crop-sciences-major-sustainable-agricultural-management-concentration/");
                final MajorMathRequirement rSOCRSAMZ = //
                        new MajorMathRequirement("SOCR-SAMZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124!", null, null);
                map.put(mSOCRSAMZ, rSOCRSAMZ);

                // ===================
                // College of Business
                // ===================

                // *** Major in Business Administration (with nine concentrations)

                final Major mBUSA = new Major(2000, "BUSA-BS", Boolean.FALSE,
                        "Business Administration",
                        CAT + "business/business-administration/"
                                + "business-administration-major/");
                final MajorMathRequirement rBUSA = new MajorMathRequirement("BUSA-BS")
                        .setSemesterCourses(BUSA3, null, null);
                map.put(mBUSA, rBUSA);

                final Major mBUSAACCZ = new Major(2001, "BUSA-ACCZ-BS",
                        Boolean.TRUE, "Business Administration",
                        "Accounting",
                        CAT + "business/accounting/business-administration-major-"
                                + "accounting-concentration/");
                final MajorMathRequirement rBUSAACCZ = //
                        new MajorMathRequirement("BUSA-ACCZ-BS")
                                .setSemesterCourses(BUSA3, null, null);
                map.put(mBUSAACCZ, rBUSAACCZ);

                final Major mBUSAFINZ = new Major(2002, "BUSA-FINZ-BS",
                        Boolean.TRUE, "Business Administration", "Finance",
                        CAT + "business/finance-real-estate/business-administration-"
                                + "major-finance-concentration/");
                final MajorMathRequirement rBUSAFINZ = //
                        new MajorMathRequirement("BUSA-FINZ-BS")
                                .setSemesterCourses(BUSA3, null, null);
                map.put(mBUSAFINZ, rBUSAFINZ);

                final Major mBUSAFPLZ = new Major(2003, "BUSA-FPLZ-BS",
                        Boolean.TRUE, "Business Administration",
                        "Financial Planning",
                        CAT + "business/finance-real-estate/business-administration-"
                                + "major-financial-planning-concentration/#text");
                final MajorMathRequirement rBUSAFPLZ = //
                        new MajorMathRequirement("BUSA-FPLZ-BS")
                                .setSemesterCourses(BUSA3, null, null);
                map.put(mBUSAFPLZ, rBUSAFPLZ);

                final Major mBUSAHRMZ = new Major(2004, "BUSA-HRMZ-BS",
                        Boolean.TRUE, "Business Administration",
                        "Human Resource Management",
                        CAT + "business/management/business-administration-major-human-"
                                + "resource-management-concentration/");
                final MajorMathRequirement rBUSAHRMZ = //
                        new MajorMathRequirement("BUSA-HRMZ-BS")
                                .setSemesterCourses(BUSA3, null, null);
                map.put(mBUSAHRMZ, rBUSAHRMZ);

                final Major mBUSAINSZ = new Major(2005, "BUSA-INSZ-BS",
                        Boolean.TRUE, "Business Administration",
                        "Information Systems",
                        CAT + "business/computer-information-systems/business-"
                                + "administration-major-information-systems-concentration/");
                final MajorMathRequirement rBUSAINSZ = //
                        new MajorMathRequirement("BUSA-INSZ-BS")
                                .setSemesterCourses(BUSA3, null, null);
                map.put(mBUSAINSZ, rBUSAINSZ);

                final Major mBUSAMINZ = new Major(2010, "BUSA-MINZ-BS",
                        Boolean.TRUE, "Business Administration",
                        "Management and Innovation",
                        CAT + "business/management/business-administration-major"
                                + "-management-innovation-concentration/");
                final MajorMathRequirement rBUSAMINZ = //
                        new MajorMathRequirement("BUSA-MINZ-BS")
                                .setSemesterCourses(BUSA3, null, null);
                map.put(mBUSAMINZ, rBUSAMINZ);

                final Major mBUSAMKTZ = new Major(2006, "BUSA-MKTZ-BS",
                        Boolean.TRUE, "Business Administration",
                        "Marketing",
                        CAT + "business/marketing/business-administration-major-"
                                + "marketing-concentration/");
                final MajorMathRequirement rBUSAMKTZ = //
                        new MajorMathRequirement("BUSA-MKTZ-BS")
                                .setSemesterCourses(BUSA3, null, null);
                map.put(mBUSAMKTZ, rBUSAMKTZ);

                // DEACTIVATED (commented to preserve what the historical number represents):

                // final Major mBUSAOIMZ = new Major(2007, "BUSA-OIMZ-BS", 
                // Boolean.TRUE, "Business Administration", 
                // "Organization and Innovation Management", 
                // CAT + "business/management/business-administration-major-" 
                // + "organization-innovation-management-concentration/"); 
                // final MajorMathRequirement rBUSAOIMZ = //
                // new MajorMathRequirement("BUSA-OIMZ-BS") 
                // .setSemesterCourses(BUSA3, null, null);
                // map.put(mBUSAOIMZ, rBUSAOIMZ);

                final Major mBUSAREAZ = new Major(2008, "BUSA-REAZ-BS",
                        Boolean.TRUE, "Business Administration",
                        "Real Estate",
                        CAT + "business/finance-real-estate/business-administration-"
                                + "major-real-estate-concentration/");
                final MajorMathRequirement rBUSAREAZ = //
                        new MajorMathRequirement("BUSA-REAZ-BS")
                                .setSemesterCourses(BUSA3, null, null);
                map.put(mBUSAREAZ, rBUSAREAZ);

                final Major mBUSASCMZ = new Major(2009, "BUSA-SCMZ-BS",
                        Boolean.TRUE, "Business Administration",
                        "Supply Chain Management",
                        CAT + "business/management/business-administration-major-"
                                + "supply-chain-management-concentration/");
                final MajorMathRequirement rBUSASCMZ = //
                        new MajorMathRequirement("BUSA-SCMZ-BS")
                                .setSemesterCourses(BUSA3, null, null);
                map.put(mBUSASCMZ, rBUSASCMZ);

                // ======================
                // College of Engineering
                // ======================

                // *** Dual-Degree programs in Biomedical Engineering

                final Major mCBEGDUAL = new Major(3000, "CBEG-DUAL",
                        Boolean.FALSE, "Biomedical Engineering, Dual Degree",
                        CAT + "engineering/biomedical/");
                final MajorMathRequirement rCBEGDUAL = //
                        new MajorMathRequirement("CBEG-DUAL")
                                .setSemesterCourses("M 160!", "M 161!",
                                        "M 261,M 340");
                map.put(mCBEGDUAL, rCBEGDUAL);

                final Major mCBEGBMEC = new Major(3001, "CBEG-BMEC-BS",
                        Boolean.TRUE, "Biomedical Engineering, Dual Degree",
                        "With Chemical and Biological Engineering",
                        CAT + "engineering/biomedical/"
                                + "chemical-biological-dual-degree-program/");
                final MajorMathRequirement rCBEGBMEC = //
                        new MajorMathRequirement("CBEG-BMEC-BS")
                                .setSemesterCourses("M 160!", "M 161!",
                                        "M 261,M 340");
                map.put(mCBEGBMEC, rCBEGBMEC);

                final Major mCPEGBMEP = new Major(3005, "CPEG-BMEP-BS",
                        Boolean.TRUE, "Biomedical Engineering, Dual Degree",
                        "With Computer Engineering",
                        CAT + "engineering/biomedical/computer-dual-degree-program/");
                final MajorMathRequirement rCPEGBMEP = //
                        new MajorMathRequirement("CPEG-BMEP-BS")
                                .setSemesterCourses("M 160!", "M 161!",
                                        "M 261,M 340");
                map.put(mCPEGBMEP, rCPEGBMEP);

                final Major mELEGBMEE = new Major(3002, "ELEG-BMEE-BS",
                        Boolean.TRUE, "Biomedical Engineering, Dual Degree",
                        "With Electrical Engineering (Electrical Engineering)",
                        CAT + "engineering/biomedical/electrical-dual-degree-program/");
                final MajorMathRequirement rELEGBMEE = //
                        new MajorMathRequirement("ELEG-BMEE-BS")
                                .setSemesterCourses("M 160!", "M 161!",
                                        "M 261,M 340");
                map.put(mELEGBMEE, rELEGBMEE);

                final Major mELEGBMEL = new Major(3003, "ELEG-BMEL-BS",
                        Boolean.TRUE, "Biomedical Engineering, Dual Degree",
                        "With Electrical Engineering (Lasers and Optical)",
                        CAT + "engineering/biomedical/electrical-lasers-optical-"
                                + "concentration-dual-degree-program/");
                final MajorMathRequirement rELEGBMEL = //
                        new MajorMathRequirement("ELEG-BMEL-BS")
                                .setSemesterCourses("M 160!", "M 161!",
                                        "M 261,M 340");
                map.put(mELEGBMEL, rELEGBMEL);

                final Major mMECHBMEM = new Major(3004, "MECH-BMEM-BS",
                        Boolean.TRUE, "Biomedical Engineering, Dual Degree",
                        "With Mechanical Engineering",
                        CAT + "engineering/biomedical/mechanical-dual-degree-program/");
                final MajorMathRequirement rMECHBMEM = //
                        new MajorMathRequirement("MECH-BMEM-BS")
                                .setSemesterCourses("M 160!", "M 161!",
                                        "M 261,M 340");
                map.put(mMECHBMEM, rMECHBMEM);

                // *** Major in Chemical and Biological Engineering

                final Major mCBEG = new Major(3010, "CBEG-BS", Boolean.TRUE,
                        "Chemical and Biological Engineering",
                        CAT + "engineering/chemical-biological/chemical-biological-"
                                + "engineering-major/");
                final MajorMathRequirement rCBEG = new MajorMathRequirement("CBEG-BS")
                        .setSemesterCourses("M 160!", "M 161!",
                                "M 261,M 340");
                map.put(mCBEG, rCBEG);

                // *** Major in Civil Engineering

                final Major mCIVE = new Major(3020, "CIVE-BS", Boolean.TRUE,
                        "Civil Engineering",
                        CAT + "engineering/civil-environmental/civil-engineering-major/");
                final MajorMathRequirement rCIVE = new MajorMathRequirement("CIVE-BS")
                        .setSemesterCourses("M 160!", "M 161!",
                                "M 261,M 340");
                map.put(mCIVE, rCIVE);

                // *** Major in Computer Engineering

                final Major mCPEG = new Major(3030, "CPEG-BS", Boolean.TRUE,
                        "Computer Engineering",
                        CAT + "engineering/electrical-computer/computer-"
                                + "engineering-major/");
                final MajorMathRequirement rCPEG = new MajorMathRequirement("CPEG-BS")
                        .setSemesterCourses("M 160!", "M 161!",
                                "M 261,M 340");
                map.put(mCPEG, rCPEG);

                final Major mCPEGAESZ = new Major(3031, "CPEG-AESZ-BS",
                        Boolean.TRUE, "Computer Engineering",
                        "Aerospace Systems",
                        CAT + "engineering/electrical-computer/computer-"
                                + "engineering-major-aerospace-systems-concentration/");
                final MajorMathRequirement rCPEGAESZ = //
                        new MajorMathRequirement("CPEG-AESZ-BS")
                                .setSemesterCourses("M 160!", "M 161!",
                                        "M 261,M 340");
                map.put(mCPEGAESZ, rCPEGAESZ);

                final Major mCPEGEISZ = new Major(3032, "CPEG-EISZ-BS",
                        Boolean.TRUE, "Computer Engineering",
                        "Embedded and IoT Systems",
                        CAT + "engineering/electrical-computer/computer-"
                                + "engineering-major-embedded-iot-systems-concentration/");
                final MajorMathRequirement rCPEGEISZ = //
                        new MajorMathRequirement("CPEG-EISZ-BS")
                                .setSemesterCourses("M 160!", "M 161!",
                                        "M 261,M 340");
                map.put(mCPEGEISZ, rCPEGEISZ);

                final Major mCPEGNDTZ = new Major(3033, "CPEG-NDTZ-BS",
                        Boolean.TRUE, "Computer Engineering",
                        "Networks and Data",
                        CAT + "engineering/electrical-computer/computer-"
                                + "engineering-major-networks-data-concentration/");
                final MajorMathRequirement rCPEGNDTZ = //
                        new MajorMathRequirement("CPEG-NDTZ-BS")
                                .setSemesterCourses("M 160!", "M 161!",
                                        "M 261,M 340");
                map.put(mCPEGNDTZ, rCPEGNDTZ);

                final Major mCPEGVICZ = new Major(3034, "CPEG-VICZ-BS",
                        Boolean.TRUE, "Computer Engineering",
                        "VLSI and Integrated Circuits",
                        CAT + "engineering/electrical-computer/computer-"
                                + "engineering-major-vlsi-integrated-circuits-concentration/");
                final MajorMathRequirement rCPEGVICZ = //
                        new MajorMathRequirement("CPEG-VICZ-BS")
                                .setSemesterCourses("M 160!", "M 161!",
                                        "M 261,M 340");
                map.put(mCPEGVICZ, rCPEGVICZ);

                // *** Major in Electrical Engineering (with two concentrations)

                final Major mELEG = new Major(3040, "ELEG-BS", Boolean.FALSE,
                        "Electrical Engineering",
                        CAT + "engineering/electrical-computer/electrical-"
                                + "engineering-major/");
                final MajorMathRequirement rELEG = new MajorMathRequirement("ELEG-BS")
                        .setSemesterCourses("M 160!", "M 161!",
                                "M 261,M 340");
                map.put(mELEG, rELEG);

                final Major mELEGELEZ = new Major(3041, "ELEG-ELEZ-BS",
                        Boolean.TRUE, "Electrical Engineering",
                        "Electrical Engineering",
                        CAT + "engineering/electrical-computer/electrical-engineering-"
                                + "major-electrical-engineering-concentration/");
                final MajorMathRequirement rELEGELEZ = //
                        new MajorMathRequirement("ELEG-ELEZ-BS")
                                .setSemesterCourses("M 160!", "M 161!",
                                        "M 261,M 340");
                map.put(mELEGELEZ, rELEGELEZ);

                final Major mELEGLOEZ = new Major(3042, "ELEG-LOEZ-BS",
                        Boolean.TRUE, "Electrical Engineering",
                        "Lasers and Optical Engineering",
                        CAT + "engineering/electrical-computer/electrical-engineering-"
                                + "major-lasers-optical-concentration/");
                final MajorMathRequirement rELEGLOEZ = //
                        new MajorMathRequirement("ELEG-LOEZ-BS")
                                .setSemesterCourses("M 160!", "M 161!",
                                        "M 261,M 340");
                map.put(mELEGLOEZ, rELEGLOEZ);

                final Major mELEGASPZ = new Major(3043, "ELEG-ASPZ-BS",
                        Boolean.TRUE, "Electrical Engineering",
                        "Aerospace",
                        CAT + "engineering/electrical-computer/electrical-engineering-"
                                + "major-aerospace-systems-concentration/");
                final MajorMathRequirement rELEGASPZ = //
                        new MajorMathRequirement("ELEG-ASPZ-BS")
                                .setSemesterCourses("M 160!", "M 161!",
                                        "M 261,M 340");
                map.put(mELEGASPZ, rELEGASPZ);

                // *** Major in Engineering Science (with three concentrations)

                // DEACTIVATED (commented to preserve what the historical number represents):

                // final Major mEGSC = new Major( 3050, "EGSC-BS", Boolean.FALSE, 
                // "Engineering Science", 
                // CAT + "engineering/engineering-science-major/"); 
                // final MajorMathRequirement rEGSC = new MajorMathRequirement("EGSC-BS")
                // 
                // .setSemesterCourses("M 160!", "M 161!",  
                // "M 261,M 340"); 
                // map.put(mEGSC, rEGSC);

                // DEACTIVATED (commented to preserve what the historical number represents):

                // final Major mEGSCEGPZ = new Major( 3051, "EGSC-EGPZ-BS", 
                // Boolean.TRUE, "Engineering Science", 
                // "Engineering Physics", 
                // CAT + "engineering/engineering-science-major-physics-" 
                // + "concentration/"); 
                // final MajorMathRequirement rEGSCEGPZ = //
                // new MajorMathRequirement("EGSC-EGPZ-BS") 
                // .setSemesterCourses("M 160!", "M 161!",  
                // "M 261,M 340"); 
                // map.put(mEGSCEGPZ, rEGSCEGPZ);

                // DEACTIVATED (commented to preserve what the historical number represents):

                // final Major mEGSCSPEZ = new Major( 3052, "EGSC-SPEZ-BS", 
                // Boolean.TRUE, "Engineering Science", 
                // "Space Engineering", 
                // CAT + "engineering/engineering-science-major-space-" 
                // + "concentration/"); 
                // final MajorMathRequirement rEGSCSPEZ = //
                // new MajorMathRequirement("EGSC-SPEZ-BS") 
                // .setSemesterCourses("M 160!", "M 161!",  
                // "M 261,M 340"); 
                // map.put(mEGSCSPEZ, rEGSCSPEZ);

                // DEACTIVATED (commented to preserve what the historical number represents):

                // final Major mEGSCTCEZ = new Major( 3053, "EGSC-TCEZ-BS", 
                // Boolean.TRUE, "Engineering Science", 
                // "Teacher Education", 
                // CAT + "engineering/engineering-science-major-teacher-" 
                // + "education-concentration/"); 
                // final MajorMathRequirement rEGSCTCEZ = //
                // new MajorMathRequirement("EGSC-TCEZ-BS") 
                // .setSemesterCourses("M 160!", "M 161!",  
                // "M 261,M 340"); 
                // map.put(mEGSCTCEZ, rEGSCTCEZ);

                // *** Dual-Degree programs in Engineering Science

                // DEACTIVATED (commented to preserve what the historical number represents):

                // final Major mEGISDUAL = new Major( 3060, "EGIS-DUAL", 
                // Boolean.FALSE, "Engineering Science Dual Degree", null); 
                // final MajorMathRequirement rEGISDUAL = //
                // new MajorMathRequirement("EGIS-DUAL") 
                // .setSemesterCourses("M 160!", "M 161!",  
                // "M 261,M 340"); 
                // map.put(mEGISDUAL, rEGISDUAL);

                // DEACTIVATED (commented to preserve what the historical number represents):

                // final Major mILES = new Major(LA, 3061, "ILES-BA", Boolean.TRUE, 
                // "Engineering Science Dual Degree", 
                // "With Interdisciplinary Liberal Arts", 
                // CAT + "liberal-arts/engineering-science-interdisciplinary-" 
                // + "liberal-arts-dual-degree-program/"); 
                // final MajorMathRequirement rILES = new MajorMathRequirement("ILES-BA")
                // 
                // .setSemesterCourses("M 160.", null, 
                // "M 161,M 261,M 340"); 
                // map.put(mILES, rILES);

                // DEACTIVATED (commented to preserve what the historical number represents):

                // final Major mEGIS = new Major( 3062, "EGIS-BS", Boolean.TRUE, 
                // "Engineering Science Dual Degree", 
                // "With International Studies", 
                // CAT + "liberal-arts/engineering-science-international-" 
                // + "studies-dual-degree-program/"); 
                // final MajorMathRequirement rEGIS = new MajorMathRequirement("EGIS-BS")
                // 
                // .setSemesterCourses("M 160!", "M 161!",  
                // "M 261,M 340"); 
                // map.put(mEGIS, rEGIS);

                // *** Major in Environmental Engineering

                final Major mENVE = new Major(3070, "ENVE-BS", Boolean.TRUE,
                        "Environmental Engineering",
                        CAT + "engineering/civil-environmental/environmental-"
                                + "engineering-major/");
                final MajorMathRequirement rENVE = new MajorMathRequirement("ENVE-BS")
                        .setSemesterCourses("M 160!", "M 161!",
                                "M 261,M 340");
                map.put(mENVE, rENVE);

                // *** Major in Mechanical Engineering

                final Major mMECH = new Major(3080, "MECH-BS", Boolean.TRUE,
                        "Mechanical Engineering",
                        CAT + "engineering/mechanical/mechanical-engineering-major/");
                final MajorMathRequirement rMECH = new MajorMathRequirement("MECH-BS")
                        .setSemesterCourses("M 160!", "M 161!",
                                "M 261,M 340");
                map.put(mMECH, rMECH);

                final Major mMECHACEZ = new Major(3081, "MECH-ACEZ-BS",
                        Boolean.TRUE, "Electrical Engineering",
                        "Aerospace Engineering",
                        CAT + "engineering/mechanical/mechanical-engineering-major-"
                                + "aerospace-engineering-concentration/");
                final MajorMathRequirement rMECHACEZ = //
                        new MajorMathRequirement("MECH-ACEZ-BS")
                                .setSemesterCourses("M 160!", "M 161!",
                                        "M 261,M 340");
                map.put(mMECHACEZ, rMECHACEZ);

                final Major mMECHADMZ = new Major(3082, "MECH-ADMZ-BS",
                        Boolean.TRUE, "Electrical Engineering",
                        "Aerospace Engineering",
                        CAT + "engineering/mechanical/mechanical-engineering-major-"
                                + "advanced-manufacturing-concentration/");
                final MajorMathRequirement rMECHADMZ = //
                        new MajorMathRequirement("MECH-ADMZ-BS")
                                .setSemesterCourses("M 160!", "M 161!",
                                        "M 261,M 340");
                map.put(mMECHADMZ, rMECHADMZ);

                // ====================================
                // College of Health and Human Sciences
                // ====================================

                // *** Major in Apparel and Merchandising (with three concentrations)

                final Major mAPAM = new Major(4000, "APAM-BS", Boolean.FALSE,
                        "Apparel and Merchandising",
                        CAT + "health-human-sciences/design-merchandising/apparel-"
                                + "merchandising-major/");
                final MajorMathRequirement rAPAM = new MajorMathRequirement("APAM-BS")
                        .setSemesterCourses("M 117!,M 118!", RawRecordConstants.M124,
                                null);
                map.put(mAPAM, rAPAM);

                final Major mAPAMADAZ = new Major(4001, "APAM-ADAZ-BS",
                        Boolean.TRUE, "Apparel and Merchandising",
                        "Apparel Design and Production",
                        CAT + "health-human-sciences/design-merchandising/apparel-"
                                + "merchandising-major-design-production-concentration/");
                final MajorMathRequirement rAPAMADAZ = //
                        new MajorMathRequirement("APAM-ADAZ-BS")
                                .setSemesterCourses("M 117!,M 118!", RawRecordConstants.M124,
                                        null);
                map.put(mAPAMADAZ, rAPAMADAZ);

                final Major mAPAMMDSZ = new Major(4002, "APAM-MDSZ-BS",
                        Boolean.TRUE, "Apparel and Merchandising",
                        "Merchandising",
                        CAT + "health-human-sciences/design-merchandising/apparel-"
                                + "merchandising-major-merchandising-concentration/");
                final MajorMathRequirement rAPAMMDSZ = //
                        new MajorMathRequirement("APAM-MDSZ-BS")
                                .setSemesterCourses("M 117!,M 118!", RawRecordConstants.M124,
                                        null);
                map.put(mAPAMMDSZ, rAPAMMDSZ);

                final Major mAPAMPDVZ = new Major(4003, "APAM-PDVZ-BS",
                        Boolean.TRUE, "Apparel and Merchandising",
                        "Product Development",
                        CAT + "health-human-sciences/design-merchandising/apparel-"
                                + "merchandising-major-product-development-concentration/");
                final MajorMathRequirement rAPAMPDVZ = //
                        new MajorMathRequirement("APAM-PDVZ-BS")
                                .setSemesterCourses("M 117!,M 118!", RawRecordConstants.M124,
                                        null);
                map.put(mAPAMPDVZ, rAPAMPDVZ);

                // *** Major in Construction Management

                final Major mCTMG = new Major(4010, "CTMG-BS", Boolean.TRUE,
                        "Construction Management",
                        CAT + "health-human-sciences/construction-management/construction"
                                + "-management-major/");
                final MajorMathRequirement rCTMG = new MajorMathRequirement("CTMG-BS")
                        .setSemesterCourses("M 117!,M 118!,M 125!", null, M_141);
                map.put(mCTMG, rCTMG);

                // *** Major in Early Childhood Education

                final Major mECHE = new Major(4020, "ECHE-BS", Boolean.TRUE,
                        "Early Childhood Education",
                        CAT + "health-human-sciences/human-development-family-studies/"
                                + "early-childhood-education-major/");
                final MajorMathRequirement rECHE = new MajorMathRequirement("ECHE-BS")
                        .setSemesterCourses(AUCC3, null, null);
                map.put(mECHE, rECHE);

                // *** Major in Family and Consumer Sciences (with two concentrations)

                final Major mFACS = new Major(4030, "FACS-BS", Boolean.FALSE,
                        "Family and Consumer Sciences",
                        CAT + "health-human-sciences/education/family-consumer-"
                                + "sciences-major/");
                final MajorMathRequirement rFACS = new MajorMathRequirement("FACS-BS")
                        .setSemesterCourses("AUCC3!", null, null);
                map.put(mFACS, rFACS);

                final Major mFACSFACZ = new Major(4031, "FACS-FACZ-BS",
                        Boolean.TRUE, "Family and Consumer Sciences",
                        "Family and Consumer Sciences",
                        CAT + "health-human-sciences/education/family-consumer-"
                                + "sciences-major-family-consumer-sciences-concentration/");
                final MajorMathRequirement rFACSFACZ = //
                        new MajorMathRequirement("FACS-FACZ-BS")
                                .setSemesterCourses("AUCC3!", null, null);
                map.put(mFACSFACZ, rFACSFACZ);

                final Major mFACSFCSZ = new Major(4032, "FACS-FCSZ-BS",
                        Boolean.TRUE, "Family and Consumer Sciences",
                        "Family and Consumer Sciences Education",
                        CAT + "health-human-sciences/education/family-consumer-"
                                + "sciences-major-education-concentration/");
                final MajorMathRequirement rFACSFCSZ = //
                        new MajorMathRequirement("FACS-FCSZ-BS")
                                .setSemesterCourses("AUCC3!", null, null);
                map.put(mFACSFCSZ, rFACSFCSZ);

                // *** Major in Fermentation Science and Technology

                final Major mFMST = new Major(4040, "FMST-BS", Boolean.TRUE,
                        "Fermentation Science and Technology",
                        CAT + "health-human-sciences/food-science-human-nutrition/"
                                + "fermentation-science-technology-major/");
                final MajorMathRequirement rFMST = new MajorMathRequirement("FMST-BS")
                        .setSemesterCourses("M 117!,M 118!,M 124!",
                                "M 125!", null);
                map.put(mFMST, rFMST);

                // *** Major in Health and Exercise Science (with two concentrations)

                final Major mHAES = new Major(4050, "HAES-BS", Boolean.FALSE,
                        "Health and Exercise Science",
                        CAT + "health-human-sciences/health-exercise-science/health-"
                                + "exercise-science-major/");
                final MajorMathRequirement rHAES = new MajorMathRequirement("HAES-BS")
                        .setSemesterCourses("M 118.,M 124.", "M 125!",
                                null);
                map.put(mHAES, rHAES);

                final Major mHAESHPRZ = new Major(4051, "HAES-HPRZ-BS",
                        Boolean.TRUE, "Health and Exercise Science",
                        "Health Promotion",
                        CAT + "health-human-sciences/health-exercise-science/"
                                + "health-exercise-science-major-promotion-concentration/");
                final MajorMathRequirement rHAESHPRZ = //
                        new MajorMathRequirement("HAES-HPRZ-BS")
                                .setSemesterCourses("M 118.,M 124.", "M 125!",
                                        null);
                map.put(mHAESHPRZ, rHAESHPRZ);

                final Major mHAESSPMZ = new Major(4052, "HAES-SPMZ-BS",
                        Boolean.TRUE, "Health and Exercise Science",
                        "Sports Medicine",
                        CAT + "health-human-sciences/health-exercise-science/"
                                + "health-exercise-science-major-sports-medicine-"
                                + "concentration/");
                final MajorMathRequirement rHAESSPMZ = //
                        new MajorMathRequirement("HAES-SPMZ-BS")
                                .setSemesterCourses("M 118.,M 124.", "M 125!",
                                        null);
                map.put(mHAESSPMZ, rHAESSPMZ);

                // *** Major in Hospitality Management

                final Major mHSMG = new Major(4060, "HSMG-BS", Boolean.TRUE,
                        "Hospitality Management",
                        CAT + "health-human-sciences/food-science-human-nutrition/"
                                + "hospitality-management-major/");
                final MajorMathRequirement rHSMG = new MajorMathRequirement("HSMG-BS")
                        .setSemesterCourses("M 101,M 117!", null, null);
                map.put(mHSMG, rHSMG);

                // *** Major in Human Development and Family Studies (with five concentrations)

                final Major mHDFS = new Major(4070, "HDFS-BS", Boolean.FALSE,
                        "Human Development and Family Studies",
                        CAT + "health-human-sciences/human-development-family-studies/"
                                + "human-development-family-studies-major/");
                final MajorMathRequirement rHDFS = new MajorMathRequirement("HDFS-BS")
                        .setSemesterCourses(AUCC3, null, null);
                map.put(mHDFS, rHDFS);

                final Major mHDFSECPZ = new Major(4071, "HDFS-ECPZ-BS",
                        Boolean.TRUE, "Human Development and Family Studies",
                        "Early Childhood Professions",
                        CAT + "health-human-sciences/human-development-family-studies/"
                                + "human-development-family-studies-major-early-childhood-"
                                + "professions-concentration/");
                final MajorMathRequirement rHDFSECPZ = //
                        new MajorMathRequirement("HDFS-ECPZ-BS")
                                .setSemesterCourses(AUCC3, null, null);
                map.put(mHDFSECPZ, rHDFSECPZ);

                final Major mHDFSHDEZ = new Major(4072, "HDFS-HDEZ-BS",
                        Boolean.TRUE, "Human Development and Family Studies",
                        "Human Development and Family Studies",
                        CAT + "health-human-sciences/human-development-family-studies/"
                                + "human-development-family-studies-major-human-development-"
                                + "family-studies-concentration/");
                final MajorMathRequirement rHDFSHDEZ = //
                        new MajorMathRequirement("HDFS-HDEZ-BS")
                                .setSemesterCourses(AUCC3, null, null);
                map.put(mHDFSHDEZ, rHDFSHDEZ);

                final Major mHDFSLADZ = new Major(4076, "HDFS-LADZ-BS",
                        Boolean.TRUE, "Human Development and Family Studies",
                        "Leadership and Advocacy",
                        CAT + "health-human-sciences/human-development-family-studies/"
                                + "human-development-family-studies-major-leadership-"
                                + "advocacy-concentration/");
                final MajorMathRequirement rHDFSLADZ = //
                        new MajorMathRequirement("HDFS-LADZ-BS")
                                .setSemesterCourses(AUCC3, null, null);
                map.put(mHDFSLADZ, rHDFSLADZ);

                // DEACTIVATED (commented to preserve what the historical number represents):

                // final Major mHDFSPHPZ = new Major(4073, "HDFS-PHPZ-BS", 
                // Boolean.TRUE, "Human Development and Family Studies", 
                // "Leadership and Entrepreneurial Professions", 
                // CAT + "health-human-sciences/human-development-family-studies/" 
                // + "human-development-family-studies-major-leadership-" 
                // + "entrepreneurial-professions-concentration/"); 
                // final MajorMathRequirement rHDFSPHPZ = //
                // new MajorMathRequirement("HDFS-PHPZ-BS") 
                // .setSemesterCourses(AUCC3, null, null);
                // map.put(mHDFSPHPZ, rHDFSPHPZ);

                final Major mHDFSPHPZ = new Major(4074, "HDFS-PHPZ-BS",
                        Boolean.TRUE, "Human Development and Family Studies",
                        "Pre-Health Professions",
                        CAT + "health-human-sciences/human-development-family-studies/"
                                + "human-development-family-studies-major-pre-health-"
                                + "professions-concentration/");
                final MajorMathRequirement rHDFSPHPZ = //
                        new MajorMathRequirement("HDFS-PHPZ-BS")
                                .setSemesterCourses(AUCC3, null, null);
                map.put(mHDFSPHPZ, rHDFSPHPZ);

                final Major mHDFSPISZ = new Major(4075, "HDFS-PISZ-BS",
                        Boolean.TRUE, "Human Development and Family Studies",
                        "Prevention and Intervention Sciences",
                        CAT + "health-human-sciences/human-development-family-studies/" +
                                "human-development-family-studies-major-prevention-"
                                + "intervention-sciences-concentration/");
                final MajorMathRequirement rHDFSPISZ = //
                        new MajorMathRequirement("HDFS-PISZ-BS")
                                .setSemesterCourses(AUCC3, null, null);
                map.put(mHDFSPISZ, rHDFSPISZ);

                // *** Major in Interior Architecture and Design

                // DEACTIVATED (commented to preserve what the historical number represents):

                // final Major mINTD = new Major(4080, "INTD-BS", Boolean.TRUE, 
                // "Interior Architecture and Design", 
                // CAT + "health-human-sciences/design-merchandising/" 
                // + "interior-architecture-design-major/"); 
                // final MajorMathRequirement rINTD = new MajorMathRequirement("INTD-BS")
                // 
                // .setSemesterCourses("M 117.,M 118.,M 124.", null, null); 
                // map.put(mINTD, rINTD);

                final Major mIARD = new Major(4081, "IARD-BS", Boolean.TRUE,
                        "Interior Architecture and Design",
                        CAT + "health-human-sciences/design-merchandising/"
                                + "interior-architecture-design-major/");
                final MajorMathRequirement rIARD = new MajorMathRequirement("IARD-BS")
                        .setSemesterCourses("M 117.,M 118.", "M 124.", null);
                map.put(mIARD, rIARD);

                // *** Major in Nutrition and Food Science (with five concentrations)

                final Major mNAFS = new Major(4090, "NAFS-BS", Boolean.FALSE,
                        "Nutrition and Food Science",
                        CAT + "health-human-sciences/food-science-human-nutrition/"
                                + "nutrition-food-science-major/");
                final MajorMathRequirement rNAFS = new MajorMathRequirement("NAFS-BS")
                        .setSemesterCourses("M 117!,M 118!,M 124!", null, null);
                map.put(mNAFS, rNAFS);

                final Major mNAFSDNMZ = new Major(4091, "NAFS-DNMZ-BS",
                        Boolean.TRUE, "Nutrition and Food Science",
                        "Dietetics and Nutrition Management",
                        CAT + "health-human-sciences/food-science-human-nutrition/"
                                + "nutrition-food-science-major-dietetics-management-"
                                + "concentration/");
                final MajorMathRequirement rNAFSDNMZ = //
                        new MajorMathRequirement("NAFS-DNMZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124!", null, null);
                map.put(mNAFSDNMZ, rNAFSDNMZ);

                final Major mNAFSFSNZ = new Major(4092, "NAFS-FSNZ-BS",
                        Boolean.TRUE, "Nutrition and Food Science",
                        "Food Safety and Nutrition",
                        CAT + "health-human-sciences/food-science-human-nutrition/"
                                + "nutrition-food-science-major-safety-concentration/");
                final MajorMathRequirement rNAFSFSNZ = //
                        new MajorMathRequirement("NAFS-FSNZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124!", null, null);
                map.put(mNAFSFSNZ, rNAFSFSNZ);

                final Major mNAFSFSYZ = new Major(4095, "NAFS-FSYZ-BS",
                        Boolean.TRUE, "Nutrition and Food Science",
                        "Food Systems",
                        CAT + "health-human-sciences/food-science-human-nutrition/"
                                + "nutrition-food-science-major-food-systems-concentration/index.html");
                final MajorMathRequirement rNAFSFSYZ = //
                        new MajorMathRequirement("NAFS-FSYZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 125", null, null);
                map.put(mNAFSFSYZ, rNAFSFSYZ);

                final Major mNAFSNFTZ = new Major(4093, "NAFS-NFTZ-BS",
                        Boolean.TRUE, "Nutrition and Food Science",
                        "Nutrition and Fitness",
                        CAT + "health-human-sciences/food-science-human-nutrition/"
                                + "nutrition-food-science-major-fitness-concentration/");
                final MajorMathRequirement rNAFSNFTZ = //
                        new MajorMathRequirement("NAFS-NFTZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 125", null, null);
                map.put(mNAFSNFTZ, rNAFSNFTZ);

                final Major mNAFSNUSZ = new Major(4094, "NAFS-NUSZ-BS",
                        Boolean.TRUE, "Nutrition and Food Science",
                        "Nutritional Sciences",
                        CAT + "health-human-sciences/food-science-human-nutrition/"
                                + "nutrition-food-science-major-nutritional-sciences-"
                                + "concentration/");
                final MajorMathRequirement rNAFSNUSZ = //
                        new MajorMathRequirement("NAFS-NUSZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124!",
                                        "M 125!", M_155);
                map.put(mNAFSNUSZ, rNAFSNUSZ);

                // *** Major in Social Work

                final Major mSOWK = new Major(4100, "SOWK-BSW", Boolean.TRUE,
                        "Social Work",
                        CAT + "health-human-sciences/school-of-social-work/"
                                + "social-work-major/");
                final MajorMathRequirement rSOWK = //
                        new MajorMathRequirement("SOWK-BSW")
                                .setSemesterCourses("AUCC3!", null, null);
                map.put(mSOWK, rSOWK);

                final Major mSOWKADSZ = new Major(4101, "SOWK-ADSZ-BSW",
                        Boolean.TRUE, "Social Work",
                        "Addictions Counceling",
                        CAT + "health-human-sciences/school-of-social-work/"
                                + "social-work-major/addictions-counseling-concentration/");
                final MajorMathRequirement rSOWKADSZ = //
                        new MajorMathRequirement("SOWK-ADSZ-BS")
                                .setSemesterCourses("AUCC3!", null, null);
                map.put(mSOWKADSZ, rSOWKADSZ);

                // =======================
                // College of Liberal Arts
                // =======================

                // *** Major in Anthropology (with three concentrations)

                final Major mANTH = new Major(5000, "ANTH-BA", Boolean.TRUE,
                        "Anthropology",
                        CAT + "liberal-arts/anthropology/anthropology-major/");
                final MajorMathRequirement rANTH = new MajorMathRequirement("ANTH-BA")
                        .setSemesterCourses("AUCC3!", null, null);
                map.put(mANTH, rANTH);

                final Major mANTHARCZ = new Major(5001, "ANTH-ARCZ-BA",
                        Boolean.TRUE, "Anthropology", "Archaeology",
                        CAT + "liberal-arts/anthropology/"
                                + "anthropology-major-archaeology-concentration/");
                final MajorMathRequirement rANTHARCZ = //
                        new MajorMathRequirement("ANTH-ARCZ-BA")
                                .setSemesterCourses("AUCC3.", null, null);
                map.put(mANTHARCZ, rANTHARCZ);

                final Major mANTHBIOZ = new Major(5002, "ANTH-BIOZ-BA",
                        Boolean.TRUE, "Anthropology",
                        "Biological Anthropology",
                        CAT + "liberal-arts/anthropology/"
                                + "anthropology-major-biological-concentration/");
                final MajorMathRequirement rANTHBIOZ = //
                        new MajorMathRequirement("ANTH-BIOZ-BA")
                                .setSemesterCourses("AUCC3.", null, null);
                map.put(mANTHBIOZ, rANTHBIOZ);

                final Major mANTHCLTZ = new Major(5003, "ANTH-CLTZ-BA",
                        Boolean.TRUE, "Anthropology",
                        "Cultural Anthropology",
                        CAT + "liberal-arts/anthropology/anthropology-major-"
                                + "cultural-concentration/");
                final MajorMathRequirement rANTHCLTZ = //
                        new MajorMathRequirement("ANTH-CLTZ-BA")
                                .setSemesterCourses("AUCC3.", null, null);
                map.put(mANTHCLTZ, rANTHCLTZ);

                // *** Major in Art, B.A. (with two concentrations)

                final Major mARTI = new Major(5010, "ARTI-BA", Boolean.FALSE,
                        "Art, B.A.",
                        CAT + "liberal-arts/art-history/art-major-ba/");
                final MajorMathRequirement rARTI = new MajorMathRequirement("ARTI-BA")
                        .setSemesterCourses(null, AUCC3, null);
                map.put(mARTI, rARTI);

                final Major mARTIARTZ = new Major(5012, "ARTI-ARTZ-BA",
                        Boolean.TRUE, "Art, B.A.", "Art History",
                        CAT + "liberal-arts/art-history/art-major-ba/art-major-art-"
                                + "history/");
                final MajorMathRequirement rARTMARTZ = //
                        new MajorMathRequirement("ARTI-ARTZ-BA")
                                .setSemesterCourses(null, AUCC3, null);
                map.put(mARTIARTZ, rARTMARTZ);

                final Major mARTIIVSZ = new Major(5013, "ARTI-IVSZ-BA",
                        Boolean.TRUE, "Art, B.A.",
                        "Integrated Visual Studies",
                        CAT + "liberal-arts/art-history/"
                                + "art-major-ba/art-major-integrated-visual-studies/");
                final MajorMathRequirement rARTMIVSZ = //
                        new MajorMathRequirement("ARTI-IVSZ-BA")
                                .setSemesterCourses(null, "AUCC3.", null);
                map.put(mARTIIVSZ, rARTMIVSZ);

                // *** Major in Art, B.F.A. (with eleven concentrations)

                final Major mARTM = new Major(5020, "ARTM-BFA", Boolean.FALSE,
                        "Art, B.F.A.",
                        CAT + "liberal-arts/art-history/art-major-bfa/");
                final MajorMathRequirement rARTM = //
                        new MajorMathRequirement("ARTM-BFA")
                                .setSemesterCourses(null, AUCC3, null);
                map.put(mARTM, rARTM);

                final Major mARTMDRAZ = new Major(5021, "ARTM-DRAZ-BF",
                        Boolean.TRUE, "Art, B.F.A.", "Drawing",
                        CAT + "liberal-arts/art-history/art-major-drawing-"
                                + "concentration/");
                final MajorMathRequirement rARTMDRAZ = //
                        new MajorMathRequirement("ARTM-DRAZ-BF")
                                .setSemesterCourses(null, AUCC3, null);
                map.put(mARTMDRAZ, rARTMDRAZ);

                final Major mARTMELAZ = new Major(5022, "ARTM-ELAZ-BF",
                        Boolean.TRUE, "Art, B.F.A.", "Electronic Art",
                        CAT + "liberal-arts/art-history/art-major-electronic-"
                                + "concentration/");
                final MajorMathRequirement rARTMELAZ = //
                        new MajorMathRequirement("ARTM-ELAZ-BF")
                                .setSemesterCourses(null, AUCC3, null);
                map.put(mARTMELAZ, rARTMELAZ);

                final Major mARTMFIBZ = new Major(5023, "ARTM-FIBZ-BF",
                        Boolean.TRUE, "Art, B.F.A.", "Fibers",
                        CAT + "liberal-arts/art-history/art-major-fibers-"
                                + "concentration/");
                final MajorMathRequirement rARTMFIBZ = //
                        new MajorMathRequirement("ARTM-FIBZ-BF")
                                .setSemesterCourses(null, AUCC3, null);
                map.put(mARTMFIBZ, rARTMFIBZ);

                final Major mARTMGRDZ = new Major(5024, "ARTM-GRDZ-BF",
                        Boolean.TRUE, "Art, B.F.A.", "Graphic Design",
                        CAT + "liberal-arts/art-history/art-major-graphic-design-"
                                + "concentration/");
                final MajorMathRequirement rARTMGRDZ = //
                        new MajorMathRequirement("ARTM-GRDZ-BF")
                                .setSemesterCourses(null, AUCC3, null);
                map.put(mARTMGRDZ, rARTMGRDZ);

                final Major mARTMMETZ = new Major(5025, "ARTM-METZ-BF",
                        Boolean.TRUE, "Art, B.F.A.", "Metalsmithing",
                        CAT + "liberal-arts/art-history/art-major-metalsmithing-"
                                + "concentration/");
                final MajorMathRequirement rARTMMETZ = //
                        new MajorMathRequirement("ARTM-METZ-BF")
                                .setSemesterCourses(null, AUCC3, null);
                map.put(mARTMMETZ, rARTMMETZ);

                final Major mARTMPNTZ = new Major(5027, "ARTM-PNTZ-BF",
                        Boolean.TRUE, "Art, B.F.A.", "Painting",
                        CAT + "liberal-arts/art-history/art-major-painting-"
                                + "concentration/");
                final MajorMathRequirement rARTMPNTZ = //
                        new MajorMathRequirement("ARTM-PNTZ-BF")
                                .setSemesterCourses(null, AUCC3, null);
                map.put(mARTMPNTZ, rARTMPNTZ);

                final Major mARTMPHIZ = new Major(5026, "ARTM-PHIZ-BF",
                        Boolean.TRUE, "Art, B.F.A.", "Photo Image Making",
                        CAT + "liberal-arts/art-history/art-major-photo-image-making-"
                                + "concentration/");
                final MajorMathRequirement rARTMPHIZ = //
                        new MajorMathRequirement("ARTM-PHIZ-BF")
                                .setSemesterCourses(null, AUCC3, null);
                map.put(mARTMPHIZ, rARTMPHIZ);

                final Major mARTMPOTZ = new Major(5028, "ARTM-POTZ-BF",
                        Boolean.TRUE, "Art, B.F.A.", "Pottery",
                        CAT + "liberal-arts/art-history/art-major-pottery-"
                                + "concentration/");
                final MajorMathRequirement rARTMPOTZ = //
                        new MajorMathRequirement("ARTM-POTZ-BF")
                                .setSemesterCourses(null, AUCC3, null);
                map.put(mARTMPOTZ, rARTMPOTZ);

                final Major mARTMPRTZ = new Major(5029, "ARTM-PRTZ-BF",
                        Boolean.TRUE, "Art, B.F.A.", "Printmaking",
                        CAT + "liberal-arts/art-history/art-major-printmaking-"
                                + "concentration/");
                final MajorMathRequirement rARTMPRTZ = //
                        new MajorMathRequirement("ARTM-PRTZ-BF")
                                .setSemesterCourses(null, AUCC3, null);
                map.put(mARTMPRTZ, rARTMPRTZ);

                final Major mARTMSCLZ = new Major(5030, "ARTM-SCLZ-BF",
                        Boolean.TRUE, "Art, B.F.A.", "Sculpture",
                        CAT + "liberal-arts/art-history/art-major-sculpture-"
                                + "concentration/");
                final MajorMathRequirement rARTMSCLZ = //
                        new MajorMathRequirement("ARTM-SCLZ-BF")
                                .setSemesterCourses(null, AUCC3, null);
                map.put(mARTMSCLZ, rARTMSCLZ);

                final Major mARTIAREZ = new Major(5031, "ARTM-AREZ-BF",
                        Boolean.TRUE, "Art, B.F.A.", "Art Education",
                        CAT + "liberal-arts/art-history/art-major-art-education-concentration/");
                final MajorMathRequirement rARTMAREZ = //
                        new MajorMathRequirement("ARTM-AREZ-BA")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mARTIAREZ, rARTMAREZ);

                // *** Major in Communication Studies (with one concentration)

                final Major mCMST = new Major(5040, "CMST-BA", Boolean.TRUE,
                        "Communication Studies",
                        CAT + "liberal-arts/communication-studies/communication-"
                                + "studies-major/");
                final MajorMathRequirement rCMST = new MajorMathRequirement("CMST-BA")
                        .setSemesterCourses(null, "AUCC3!", null);
                map.put(mCMST, rCMST);

                final Major mCMSTTCLZ = new Major(5041, "CMST-TCLZ-BA",
                        Boolean.TRUE, "Communication Studies",
                        "Speech Teacher Licensure",
                        CAT + "liberal-arts/communication-studies/communication-studies-"
                                + "major-speech-teacher-licensure-concentration/");
                final MajorMathRequirement rCMSTTCLZ = //
                        new MajorMathRequirement("CMST-TCLZ-BA")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mCMSTTCLZ, rCMSTTCLZ);

                // *** Major in Dance (BA)

                final Major mDNCE = new Major(5050, "DNCE-BA", Boolean.TRUE,
                        "Dance",
                        CAT + "liberal-arts/music-theatre-dance/dance/dance-ba/");
                final MajorMathRequirement rDNCE = new MajorMathRequirement("DNCE-BA")
                        .setSemesterCourses("AUCC3.", null, null);
                map.put(mDNCE, rDNCE);

                // *** Major in Dance (BFA)

                final Major mDANC = new Major(5050, "DANC-BFA", Boolean.TRUE,
                        "Dance",
                        CAT + "liberal-arts/music-theatre-dance/dance/dance-bfa/");

                final MajorMathRequirement rDANC = new MajorMathRequirement("DANC-BFA")
                        .setSemesterCourses(null, "AUCC3!", null);
                map.put(mDANC, rDANC);

                // *** Major in Economics

                final Major mECON = new Major(5060, "ECON-BA", Boolean.TRUE,
                        "Economics",
                        CAT + "liberal-arts/economics/economics-major/");
                final MajorMathRequirement rECON = new MajorMathRequirement("ECON-BA")
                        .setSemesterCourses(null, "CALC!", null);
                map.put(mECON, rECON);

                // *** Major in English (with five concentrations)
                final Major mENGL = new Major(5070, "ENGL-BA", Boolean.FALSE,
                        "English",
                        CAT + "liberal-arts/english/english-major/");
                final MajorMathRequirement rENGL = new MajorMathRequirement("ENGL-BA")
                        .setSemesterCourses("AUCC3.", null, null);
                map.put(mENGL, rENGL);

                final Major mENGLCRWZ = new Major(5071, "ENGL-CRWZ-BA",
                        Boolean.TRUE, "English", "Creative Writing",
                        CAT + "liberal-arts/english/english-major-creative-writing-"
                                + "concentration/");
                final MajorMathRequirement rENGLCRWZ = //
                        new MajorMathRequirement("ENGL-CRWZ-BA")
                                .setSemesterCourses("AUCC3.", null, null);
                map.put(mENGLCRWZ, rENGLCRWZ);

                final Major mENGLENEZ = new Major(5072, "ENGL-ENEZ-BA",
                        Boolean.TRUE, "English", "English Education",
                        CAT + "liberal-arts/english/english-major-education-"
                                + "concentration/");
                final MajorMathRequirement rENGLENEZ = //
                        new MajorMathRequirement("ENGL-ENEZ-BA")
                                .setSemesterCourses("AUCC3.", null, null);
                map.put(mENGLENEZ, rENGLENEZ);

                // DEACTIVATED (commented to preserve what the historical number represents):

                // final Major mENGLLANZ = new Major(5073, "ENGL-LANZ-BA", 
                // Boolean.TRUE, "English", "Language",  
                // CAT + "liberal-arts/english/english-major-language-" 
                // + "concentration/"); 
                // final MajorMathRequirement rENGLLANZ = //
                // new MajorMathRequirement("ENGL-LANZ-BA") 
                // .setSemesterCourses("AUCC3.", null, null); 
                // map.put(mENGLLANZ, rENGLLANZ);

                final Major mENGLLINZ = new Major(5076, "ENGL-LINZ-BA",
                        Boolean.TRUE, "English",
                        "Linguistics",
                        CAT + "liberal-arts/english/english-major-"
                                + "linguistics-concentration/");
                final MajorMathRequirement rENGLLINZ = //
                        new MajorMathRequirement("ENGL-LINZ-BA")
                                .setSemesterCourses("AUCC3.", null, null);
                map.put(mENGLLINZ, rENGLLINZ);

                final Major mENGLLITZ = new Major(5074, "ENGL-LITZ-BA",
                        Boolean.TRUE, "English", "Literature",
                        CAT + "liberal-arts/english/english-major-literature-"
                                + "concentration/");
                final MajorMathRequirement rENGLLITZ = //
                        new MajorMathRequirement("ENGL-LITZ-BA")
                                .setSemesterCourses("AUCC3.", null, null);
                map.put(mENGLLITZ, rENGLLITZ);

                final Major mENGLWRLZ = new Major(5075, "ENGL-WRLZ-BA",
                        Boolean.TRUE, "English",
                        "Writing, Rhetoric and Literacy",
                        CAT + "liberal-arts/english/english-major-writing-rhetoric-"
                                + "literacy-concentration/");
                final MajorMathRequirement rENGLWRLZ = //
                        new MajorMathRequirement("ENGL-WRLZ-BA")
                                .setSemesterCourses("AUCC3.", null, null);
                map.put(mENGLWRLZ, rENGLWRLZ);

                // *** Major in Ethnic Studies (with one concentration)

                final Major mETST = new Major(5080, "ETST-BA", Boolean.TRUE,
                        "Ethnic Studies",
                        CAT + "liberal-arts/ethnic-studies/ethnic-studies-major/");
                final MajorMathRequirement rETST = new MajorMathRequirement("ETST-BA")
                        .setSemesterCourses("AUCC3.", null, null);
                map.put(mETST, rETST);

                final Major mETSTCOIZ = new Major(5082, "ETST-COIZ-BA",
                        Boolean.TRUE, "Ethnic Studies",
                        "Community Organizing and Institutional Change",
                        CAT + "liberal-arts/ethnic-studies/ethnic-studies-major"
                                + "-social-studies-teaching-concentration/");
                final MajorMathRequirement rETSTCOIZ = //
                        new MajorMathRequirement("ETST-COIZ-BA")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mETSTCOIZ, rETSTCOIZ);

                final Major mETSTRPRZ = new Major(5083, "ETST-RPRZ-BA",
                        Boolean.TRUE, "Ethnic Studies",
                        "Global Race, Power, and Resistance",
                        CAT + "liberal-arts/ethnic-studies/ethnic-studies-major-"
                                + "global-race-power-resistance-concentration/");
                final MajorMathRequirement rETSTRPRZ = //
                        new MajorMathRequirement("ETST-RPRZ-BA")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mETSTRPRZ, rETSTRPRZ);

                final Major mETSTSOTZ = new Major(5081, "ETST-SOTZ-BA",
                        Boolean.TRUE, "Ethnic Studies",
                        "Social Studies Teaching",
                        CAT + "liberal-arts/ethnic-studies/ethnic-studies-major"
                                + "-social-studies-teaching-concentration/");
                final MajorMathRequirement rETSTSOTZ = //
                        new MajorMathRequirement("ETST-SOTZ-BA")
                                .setSemesterCourses(AUCC3, null, null);
                map.put(mETSTSOTZ, rETSTSOTZ);

                // *** Major in Geography

                final Major mGEOG = new Major(5085, "GEOG-BS", Boolean.TRUE,
                        "Geography",
                        CAT + "liberal-arts/anthropology/geography-major/");
                final MajorMathRequirement rGEOG = new MajorMathRequirement("GEOG-BS")
                        .setSemesterCourses(AUCC3, null, null);
                map.put(mGEOG, rGEOG);

                // *** Major in History (with five concentrations)

                final Major mHIST = new Major(5090, "HIST-BA", Boolean.FALSE,
                        "History",
                        CAT + "liberal-arts/history/history-major/");
                final MajorMathRequirement rHIST = new MajorMathRequirement("HIST-BA")
                        .setSemesterCourses(AUCC3, null, null);
                map.put(mHIST, rHIST);

                final Major mHISTGENZ = new Major(5091, "HIST-GENZ-BA",
                        Boolean.TRUE, "History", "General History",
                        CAT + "liberal-arts/history/history-major-general-"
                                + "concentration/");
                final MajorMathRequirement rHISTGENZ = //
                        new MajorMathRequirement("HIST-GENZ-BA")
                                .setSemesterCourses(AUCC3, null, null);
                map.put(mHISTGENZ, rHISTGENZ);

                final Major mHISTLNGZ = new Major(5092, "HIST-LNGZ-BA",
                        Boolean.TRUE, "History", "Language",
                        CAT + "liberal-arts/history/history-major-language-"
                                + "concentration/");
                final MajorMathRequirement rHISTLNGZ = //
                        new MajorMathRequirement("HIST-LNGZ-BA")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mHISTLNGZ, rHISTLNGZ);

                final Major mHISTSBSZ = new Major(5093, "HIST-SBSZ-BA",
                        Boolean.TRUE, "History",
                        "Social and Behavioral Sciences",
                        CAT + "liberal-arts/history/history-major-social-behavioral-"
                                + "sciences-concentration/");
                final MajorMathRequirement rHISTSBSZ = //
                        new MajorMathRequirement("HIST-SBSZ-BA")
                                .setSemesterCourses(AUCC3, null, null);
                map.put(mHISTSBSZ, rHISTSBSZ);

                final Major mHISTSSTZ = new Major(5094, "HIST-SSTZ-BA",
                        Boolean.TRUE, "History", "Social Studies Teaching",
                        CAT + "liberal-arts/history/history-major-social-studies-"
                                + "teaching-concentration/");
                final MajorMathRequirement rHISTSSTZ = //
                        new MajorMathRequirement("HIST-SSTZ-BA")
                                .setSemesterCourses(AUCC3, null, null);
                map.put(mHISTSSTZ, rHISTSSTZ);

                final Major mHISTDPUZ = new Major(5095, "HIST-DPUZ-BA",
                        Boolean.TRUE, "History", "Digital and Public History",
                        CAT + "liberal-arts/history/history-major-digital-public-"
                                + "history-concentration/");
                final MajorMathRequirement rHISTDPUZ = //
                        new MajorMathRequirement("HIST-DPUZ-BA")
                                .setSemesterCourses(AUCC3, null, null);
                map.put(mHISTDPUZ, rHISTDPUZ);

                // *** Major in Journalism and Media Communication

                final Major mJAMC = new Major(5100, "JAMC-BA", Boolean.TRUE,
                        "Journalism and Media Communication",
                        CAT + "liberal-arts/journalism-media-communication/media-major/");
                final MajorMathRequirement rJAMC = new MajorMathRequirement("JAMC-BA")
                        .setSemesterCourses(null, "AUCC3!", null);
                map.put(mJAMC, rJAMC);

                // *** Major in Languages, Literatures and Cultures (with three concentrations)

                final Major mLLAC = new Major(5110, "LLAC-BA", Boolean.FALSE,
                        "Languages, Literatures and Cultures",
                        CAT + "liberal-arts/foreign-languages-literatures/languages-"
                                + "literature-cultures-major/");
                final MajorMathRequirement rLLAC = new MajorMathRequirement("LLAC-BA")
                        .setSemesterCourses(null, null, AUCC3);
                map.put(mLLAC, rLLAC);

                final Major mLLACLFRZ = new Major(5111, "LLAC-LFRZ-BA",
                        Boolean.FALSE, "Languages, Literatures and Cultures",
                        "French",
                        CAT + "liberal-arts/foreign-languages-literatures/languages-"
                                + "literature-cultures-major-french-concentration/");
                final MajorMathRequirement rLLACLFRZ = //
                        new MajorMathRequirement("LLAC-LFRZ-BA")
                                .setSemesterCourses(null, null, AUCC3);
                map.put(mLLACLFRZ, rLLACLFRZ);

                final Major mLLACLGEZ = new Major(5112, "LLAC-LGEZ-BA",
                        Boolean.FALSE, "Languages, Literatures and Cultures",
                        "German",
                        CAT + "liberal-arts/foreign-languages-literatures/languages-"
                                + "literature-cultures-major-german-concentration/");
                final MajorMathRequirement rLLACLGEZ = //
                        new MajorMathRequirement("LLAC-LGEZ-BA")
                                .setSemesterCourses(null, null, AUCC3);
                map.put(mLLACLGEZ, rLLACLGEZ);

                final Major mLLACLSPZ = new Major(5113, "LLAC-LSPZ-BA",
                        Boolean.FALSE, "Languages, Literatures and Cultures",
                        "Spanish",
                        CAT + "liberal-arts/foreign-languages-literatures/languages-"
                                + "literature-cultures-major-spanish-concentration/");
                final MajorMathRequirement rLLACLSPZ = //
                        new MajorMathRequirement("LLAC-LSPZ-BA")
                                .setSemesterCourses(null, null, AUCC3);
                map.put(mLLACLSPZ, rLLACLSPZ);

                final Major mLLACSPPZ = new Major(5114, "LLAC-SPPZ-BA",
                        Boolean.FALSE, "Languages, Literatures and Cultures",
                        "Spanish for the Professions",
                        CAT + "liberal-arts/foreign-languages-literatures/languages-"
                                + "literature-cultures-major-spanish-for-professions-"
                                + "concentration/");
                final MajorMathRequirement rLLACSPPZ = //
                        new MajorMathRequirement("LLAC-SPPZ-BA")
                                .setSemesterCourses(null, null, AUCC3);
                map.put(mLLACSPPZ, rLLACSPPZ);

                // *** Major in Music, B.A.

                final Major mMUSI = new Major(5120, "MUSI-BA", Boolean.TRUE,
                        "Music (B.A.)",
                        CAT + "liberal-arts/music-theatre-dance/music-ba/");
                final MajorMathRequirement rMUSI = new MajorMathRequirement("MUSI-BA")
                        .setSemesterCourses("AUCC3!", null, null);
                map.put(mMUSI, rMUSI);

                // *** Major in Music, B.M. (with four concentrations)

                final Major mMUSC = new Major(5130, "MUSC-BM", Boolean.FALSE,
                        "Music (B.M.)",
                        CAT + "liberal-arts/music-theatre-dance/music-bm/");
                final MajorMathRequirement rMUSC = new MajorMathRequirement("MUSC-BM")
                        .setSemesterCourses(null, "AUCC3!", null);
                map.put(mMUSC, rMUSC);

                final Major mMUSCCOMZ = new Major(5131, "MUSC-COMZ-BM",
                        Boolean.TRUE, "Music (B.M.)", "Composition",
                        CAT + "liberal-arts/music-theatre-dance/music-bm-composition-"
                                + "concentration/");
                final MajorMathRequirement rMUSCCOMZ = //
                        new MajorMathRequirement("MUSC-COMZ-BM")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mMUSCCOMZ, rMUSCCOMZ);

                final Major mMUSCMUEZ = new Major(5132, "MUSC-MUEZ-BM",
                        Boolean.TRUE, "Music (B.M.)", "Music Education",
                        CAT + "liberal-arts/music-theatre-dance/music-bm-education-"
                                + "concentration/");
                final MajorMathRequirement rMUSCMUEZ = //
                        new MajorMathRequirement("MUSC-MUEZ-BM")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mMUSCMUEZ, rMUSCMUEZ);

                final Major mMUSCMUTZ = new Major(5133, "MUSC-MUTZ-BM",
                        Boolean.TRUE, "Music (B.M.)", "Music Therapy",
                        CAT + "liberal-arts/music-theatre-dance/music-bm-therapy-"
                                + "concentration/");
                final MajorMathRequirement rMUSCMUTZ = //
                        new MajorMathRequirement("MUSC-MUTZ-BM")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mMUSCMUTZ, rMUSCMUTZ);

                final Major mMUSCPERZ = new Major(5134, "MUSC-PERZ-BM",
                        Boolean.TRUE, "Music (B.M.)", "Performance",
                        CAT + "liberal-arts/music-theatre-dance/music-bm-performance-"
                                + "concentration/");
                final MajorMathRequirement rMUSCPERZ = //
                        new MajorMathRequirement("MUSC-PERZ-BM")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mMUSCPERZ, rMUSCPERZ);

                // *** Major in Philosophy (with three concentrations)

                final Major mPHIL = new Major(5140, "PHIL-BA", Boolean.FALSE,
                        "Philosophy",
                        CAT + "liberal-arts/philosophy/philosophy-major/");
                final MajorMathRequirement rPHIL = new MajorMathRequirement("PHIL-BA")
                        .setSemesterCourses("AUCC3.", null, null);
                map.put(mPHIL, rPHIL);

                final Major mPHILGNPZ = new Major(5141, "PHIL-GNPZ-BA",
                        Boolean.TRUE, "Philosophy", "General Philosophy",
                        CAT + "liberal-arts/philosophy/philosophy-major-general-"
                                + "concentration/");
                final MajorMathRequirement rPHILGNPZ = //
                        new MajorMathRequirement("PHIL-GNPZ-BA")
                                .setSemesterCourses("AUCC3.", null, null);
                map.put(mPHILGNPZ, rPHILGNPZ);

                final Major mPHILGPRZ = new Major(5142, "PHIL-GPRZ-BA",
                        Boolean.TRUE, "Philosophy",
                        "Global Philosophies and Religions",
                        CAT + "liberal-arts/philosophy/philosophy-major-global-"
                                + "philosophies-religions-concentration/");
                final MajorMathRequirement rPHILGPRZ = //
                        new MajorMathRequirement("PHIL-GPRZ-BA")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mPHILGPRZ, rPHILGPRZ);

                final Major mPHILPSAZ = new Major(5143, "PHIL-PSAZ-BA",
                        Boolean.TRUE, "Philosophy",
                        "Philosophy, Science, and Technology",
                        CAT + "liberal-arts/philosophy/philosophy-major-science-"
                                + "technology-concentration/");
                final MajorMathRequirement rPHILPSAZ = //
                        new MajorMathRequirement("PHIL-PSAZ-BA")
                                .setSemesterCourses("AUCC3.", null, null);
                map.put(mPHILPSAZ, rPHILPSAZ);

                // *** Major in Political Science (with three concentrations)

                final Major mPOLS = new Major(5150, "POLS-BA", Boolean.TRUE,
                        "Political Science",
                        CAT + "liberal-arts/political-science/political-science-major/");
                final MajorMathRequirement rPOLS = new MajorMathRequirement("POLS-BA")
                        .setSemesterCourses(null, "AUCC3!", null);
                map.put(mPOLS, rPOLS);

                final Major mPOLSEPAZ = new Major(5151, "POLS-EPAZ-BA",
                        Boolean.TRUE, "Political Science",
                        "Environmental Politics and Policy",
                        CAT + "liberal-arts/political-science/political-science-major-"
                                + "environmental-politics-policy-concentration/");
                final MajorMathRequirement rPOLSEPAZ = //
                        new MajorMathRequirement("POLS-EPAZ-BA")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mPOLSEPAZ, rPOLSEPAZ);

                final Major mPOLSGPPZ = new Major(5152, "POLS-GPPZ-BA",
                        Boolean.TRUE, "Political Science",
                        "Global Politics and Policy",
                        CAT + "liberal-arts/political-science/political-science-major-"
                                + "global-politics-policy-concentration/");
                final MajorMathRequirement rPOLSGPPZ = //
                        new MajorMathRequirement("POLS-GPPZ-BA")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mPOLSGPPZ, rPOLSGPPZ);

                final Major mPOLSULPZ = new Major(5153, "POLS-ULPZ-BA",
                        Boolean.TRUE, "Political Science",
                        "U.S. Government, Law, and Policy",
                        CAT + "liberal-arts/political-science/political-science-major-"
                                + "us-government-law-policy-concentration/");
                final MajorMathRequirement rPOLSULPZ = //
                        new MajorMathRequirement("POLS-ULPZ-BA")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mPOLSULPZ, rPOLSULPZ);

                // *** Major in Sociology (with three concentrations)

                final Major mSOCI = new Major(5160, "SOCI-BA", Boolean.FALSE,
                        "Sociology",
                        CAT + "liberal-arts/sociology/sociology-major/");
                final MajorMathRequirement rSOCI = new MajorMathRequirement("SOCI-BA")
                        .setSemesterCourses(null, "AUCC3SOC!", null);
                map.put(mSOCI, rSOCI);

                final Major mSOCICRCZ = new Major(5161, "SOCI-CRCZ-BA",
                        Boolean.TRUE, "Sociology",
                        "Criminology and Criminal Justice",
                        CAT + "liberal-arts/sociology/sociology-major-criminology-"
                                + "criminal-justice-concentration/");
                final MajorMathRequirement rSOCICRCZ = //
                        new MajorMathRequirement("SOCI-CRCZ-BA")
                                .setSemesterCourses(null, "AUCC3SOC!", null);
                map.put(mSOCICRCZ, rSOCICRCZ);

                final Major mSOCIENSZ = new Major(5162, "SOCI-ENSZ-BA",
                        Boolean.TRUE, "Sociology",
                        "Environmental Sociology",
                        CAT + "liberal-arts/sociology/sociology-major-environmental-"
                                + "concentration/");
                final MajorMathRequirement rSOCIENSZ = //
                        new MajorMathRequirement("SOCI-ENSZ-BA")
                                .setSemesterCourses(null, "AUCC3SOC!", null);
                map.put(mSOCIENSZ, rSOCIENSZ);

                final Major mSOCIGNSZ = new Major(5163, "SOCI-GNSZ-BA",
                        Boolean.TRUE, "Sociology", "General Sociology",
                        CAT + "liberal-arts/sociology/sociology-major-general-"
                                + "concentration/");
                final MajorMathRequirement rSOCIGNSZ = //
                        new MajorMathRequirement("SOCI-GNSZ-BA")
                                .setSemesterCourses(null, "AUCC3SOC!", null);
                map.put(mSOCIGNSZ, rSOCIGNSZ);

                // *** Major in Theatre (with three concentrations)

                final Major mTHTR = new Major(5170, "THTR-BA", Boolean.FALSE,
                        "Theatre",
                        CAT + "liberal-arts/music-theatre-dance/theatre/");
                final MajorMathRequirement rTHTR = new MajorMathRequirement("THTR-BA")
                        .setSemesterCourses(null, "AUCC3!", null);
                map.put(mTHTR, rTHTR);

                // DEACTIVATED (commented to preserve what the historical number represents):

                // final Major mTHTRDTHZ = new Major(5171, "THTR-DTHZ-BA", 
                // Boolean.TRUE, "Theatre", "Design and Technology",  
                // CAT + "liberal-arts/music-theatre-dance/theatre-design-" 
                // + "technology-concentration/"); 
                // final MajorMathRequirement rTHTRDTHZ = //
                // new MajorMathRequirement("THTR-DTHZ-BA") 
                // .setSemesterCourses(null, "AUCC3!", null); 
                // map.put(mTHTRDTHZ, rTHTRDTHZ);

                // DEACTIVATED (commented to preserve what the historical number represents):

                // final Major mTHTRGTRZ = new Major(5172, "THTR-GTRZ-BA", 
                // Boolean.TRUE, "Theatre", "General Theatre",  
                // CAT + "liberal-arts/music-theatre-dance/theatre-general-" 
                // + "theatre-concentration/"); 
                // final MajorMathRequirement rTHTRGTRZ = //
                // new MajorMathRequirement("THTR-GTRZ-BA") 
                // .setSemesterCourses(null, "AUCC3!", null); 
                // map.put(mTHTRGTRZ, rTHTRGTRZ);

                final Major mTHTRPRFZ = new Major(5173, "THTR-PRFZ-BA",
                        Boolean.TRUE, "Theatre", "Performance",
                        CAT + "liberal-arts/music-theatre-dance/theatre-performance-"
                                + "concentration/");
                final MajorMathRequirement rTHTRPRFZ = //
                        new MajorMathRequirement("THTR-PRFZ-BA")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mTHTRPRFZ, rTHTRPRFZ);

                final Major mTHTRLDTZ = new Major(5174, "THTR-LDTZ-BA",
                        Boolean.TRUE, "Theatre",
                        "Lighting Design and Technology",
                        CAT + "liberal-arts/music-theatre-dance/theatre-lighting-"
                                + "design-technology-concentration/");
                final MajorMathRequirement rTHTRLDTZ = //
                        new MajorMathRequirement("THTR-LDTZ-BA")
                                .setSemesterCourses(null, "AUCC3.", null);
                map.put(mTHTRLDTZ, rTHTRLDTZ);

                final Major mTHTRMUSZ = new Major(5175, "THTR-MUSZ-BA",
                        Boolean.TRUE, "Theatre",
                        "Lighting Design and Technology",
                        CAT + "liberal-arts/music-theatre-dance/theatre-musical-"
                                + "theatre-concentration/");
                final MajorMathRequirement rTHTRMUSZ = //
                        new MajorMathRequirement("THTR-MUSZ-BA")
                                .setSemesterCourses(null, "AUCC3.", null);
                map.put(mTHTRMUSZ, rTHTRMUSZ);

                final Major mTHTRPDTZ = new Major(5176, "THTR-PDTZ-BA",
                        Boolean.TRUE, "Theatre",
                        "Projection Design and Technology",
                        CAT + "liberal-arts/music-theatre-dance/theatre-projection-"
                                + "design-technology-concentration/");
                final MajorMathRequirement rTHTRPDTZ = //
                        new MajorMathRequirement("THTR-PDTZ-BA")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mTHTRPDTZ, rTHTRPDTZ);

                final Major mTHTRSDSZ = new Major(5177, "THTR-SDSZ-BA",
                        Boolean.TRUE, "Theatre",
                        "Set Design",
                        CAT + "liberal-arts/music-theatre-dance/theatre-set-design-"
                                + "technology-concentration/");
                final MajorMathRequirement rTHTRSDSZ = //
                        new MajorMathRequirement("THTR-SDSZ-BA")
                                .setSemesterCourses("AUCC3!", null, null);
                map.put(mTHTRSDSZ, rTHTRSDSZ);

                final Major mTHTRSDTZ = new Major(5178, "THTR-SDTZ-BA",
                        Boolean.TRUE, "Theatre",
                        "Sound Design and Technology",
                        CAT + "liberal-arts/music-theatre-dance/theatre-sound-design-"
                                + "technology-concentration/");
                final MajorMathRequirement rTHTRSDTZ = //
                        new MajorMathRequirement("THTR-SDTZ-BA")
                                .setSemesterCourses("AUCC3!", null, null);
                map.put(mTHTRSDTZ, rTHTRSDTZ);

                // *** Major in Women's and Gender Studies

                final Major mWGST = new Major(5180, "WGST-BA", Boolean.TRUE,
                        "Women's and Gender Studies",
                        CAT + "liberal-arts/ethnic-studies/womens-gender-studies-major/");
                final MajorMathRequirement rWGST = new MajorMathRequirement("WGST-BA")
                        .setSemesterCourses(null, "AUCC3!", null);
                map.put(mWGST, rWGST);

                // *** Major in International Studies (with four concentrations)

                final Major mINST = new Major(5190, "INST-BS", Boolean.FALSE,
                        "International Studies",
                        CAT + "liberal-arts/international-studies-major/");
                final MajorMathRequirement rINST = new MajorMathRequirement("INST-BS")
                        .setSemesterCourses(null, "AUCC3!", null);
                map.put(mINST, rINST);

                final Major mINSTASTZ = new Major(5191, "INST-ASTZ-BS",
                        Boolean.TRUE, "International Studies",
                        "Asian Studies",
                        CAT + "liberal-arts/international-studies-major-asian-"
                                + "concentration/");
                final MajorMathRequirement rINSTASTZ = //
                        new MajorMathRequirement("INST-ASTZ-BS")
                                .setSemesterCourses(null, "AUCC3", null);
                map.put(mINSTASTZ, rINSTASTZ);

                final Major mINSTEUSZ = new Major(5192, "INST-EUSZ-BS",
                        Boolean.TRUE, "International Studies",
                        "European Studies",
                        CAT + "liberal-arts/international-studies-major-european-"
                                + "concentration/");
                final MajorMathRequirement rINSTEUSZ = //
                        new MajorMathRequirement("INST-EUSZ-BS")
                                .setSemesterCourses(null, "AUCC3.", null);
                map.put(mINSTEUSZ, rINSTEUSZ);

                final Major mINSTLTSZ = new Major(5193, "INST-LTSZ-BS",
                        Boolean.TRUE, "International Studies",
                        "Latin American Studies",
                        CAT + "liberal-arts/international-studies-major-latin-american-"
                                + "concentration/");
                final MajorMathRequirement rINSTLTSZ = //
                        new MajorMathRequirement("INST-LTSZ-BS")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mINSTLTSZ, rINSTLTSZ);

                final Major mINSTMEAZ = new Major(5194, "INST-MEAZ-BS",
                        Boolean.TRUE, "International Studies",
                        "Middle East and North African Studies",
                        CAT + "liberal-arts/international-studies-major-middle-east-"
                                + "north-african-concentration/");
                final MajorMathRequirement rINSTMEAZ = //
                        new MajorMathRequirement("INST-MEAZ-BS")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mINSTMEAZ, rINSTMEAZ);

                final Major mINSTGBLZ = new Major(5195, "INST-GBLZ-BS",
                        Boolean.TRUE, "International Studies",
                        "Global Studies",
                        CAT + "liberal-arts/international-studies-major-global-"
                                + "studies-concentration/");
                final MajorMathRequirement rINSTGBLZ = //
                        new MajorMathRequirement("INST-GBLZ-BS")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mINSTGBLZ, rINSTGBLZ);

                // *** Major in Interdisciplinary Liberal Arts

                final Major mILAR = new Major(5200, "ILAR-BA", Boolean.TRUE,
                        "Interdisciplinary Liberal Arts",
                        CAT + "liberal-arts/interdisciplinary-liberal-arts-major/");
                final MajorMathRequirement rILAR = new MajorMathRequirement("ILAR-BA")
                        .setSemesterCourses("AUCC3.", null, null);
                map.put(mILAR, rILAR);

                // ============================
                // College of Natural Resources
                // ============================

                // *** Major in Ecosystem Science and Sustainability

                final Major mECSS = new Major(6000, "ECSS-BA", Boolean.TRUE,
                        "Ecosystem Science and Sustainability",
                        CAT + "natural-resources/ecosystem-science-sustainability/"
                                + "ecosystem-science-sustainability-major/");
                final MajorMathRequirement rECSS = new MajorMathRequirement("ECSS-BA")
                        .setSemesterCourses(null, CALC1BIO, null);
                map.put(mECSS, rECSS);

                // *** Major in Fish, Wildlife and Conservation Biology (with three concentrations)

                final Major mFWCB = new Major(6010, "FWCB-BS", Boolean.FALSE,
                        "Fish, Wildlife and Conservation Biology",
                        CAT + "natural-resources/fish-wildlife-conservation-biology/"
                                + "fish-wildlife-conservation-biology-major/");
                final MajorMathRequirement rFWCB = new MajorMathRequirement("FWCB-BS")
                        .setSemesterCourses("M 117!,M 118!,M 124!",
                                "M 125!", CALC1BIO);
                map.put(mFWCB, rFWCB);

                final Major mFWCBCNVZ = new Major(6011, "FWCB-CNVZ-BS",
                        Boolean.TRUE, "Fish, Wildlife and Conservation Biology",
                        "Conservation Biology",
                        CAT + "natural-resources/fish-wildlife-conservation-biology/"
                                + "fish-wildlife-conservation-biology-major-conservation-"
                                + "concentration/");
                final MajorMathRequirement rFWCBCNVZ = //
                        new MajorMathRequirement("FWCB-CNVZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124!",
                                        "M 125!", CALC1BIO);
                map.put(mFWCBCNVZ, rFWCBCNVZ);

                final Major mFWCBFASZ = new Major(6012, "FWCB-FASZ-BS",
                        Boolean.TRUE, "Fish, Wildlife and Conservation Biology",
                        "Fisheries and Aquatic Sciences",
                        CAT + "natural-resources/fish-wildlife-conservation-biology/"
                                + "fish-wildlife-conservation-biology-major-fisheries-"
                                + "aquatic-sciences-concentration/");
                final MajorMathRequirement rFWCBFASZ = //
                        new MajorMathRequirement("FWCB-FASZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124!",
                                        "M 125!", CALC1BIO);
                map.put(mFWCBFASZ, rFWCBFASZ);

                final Major mFWCBWDBZ = new Major(6013, "FWCB-WDBZ-BS",
                        Boolean.TRUE, "Fish, Wildlife and Conservation Biology",
                        "Wildlife Biology",
                        CAT + "natural-resources/fish-wildlife-conservation-biology/"
                                + "fish-wildlife-conservation-biology-major-wildlife-"
                                + "concentration/");
                final MajorMathRequirement rFWCBWDBZ = //
                        new MajorMathRequirement("FWCB-WDBZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124!",
                                        "M 125!", CALC1BIO);
                map.put(mFWCBWDBZ, rFWCBWDBZ);

                // *** Major in Forest and Rangeland Stewardship (with five concentrations)

                final Major mFRRS = new Major(6080, "FRRS-BS", Boolean.FALSE,
                        "Forest and Rangeland Stewardship",
                        CAT + "natural-resources/forest-rangeland-stewardship/"
                                + "forest-rangeland-stewardship-major/");
                final MajorMathRequirement rFRRS = new MajorMathRequirement("FRRS-BS")
                        .setSemesterCourses("M 117!,M 118!,M 125", null, null);
                map.put(mFRRS, rFRRS);

                final Major mFRRSFRBZ = new Major(6081, "FRRS-FRBZ-BS",
                        Boolean.TRUE, "Forest and Rangeland Stewardship",
                        "Forest Biology",
                        CAT + "natural-resources/forest-rangeland-stewardship/"
                                + "forest-rangeland-stewardship-major/"
                                + "forest-biology-concentration/");
                final MajorMathRequirement rFRRSFRBZ = //
                        new MajorMathRequirement("FRRS-FRBZ-BS")
                                .setSemesterCourses("M 124!,M 125!", "M 155!",
                                        null);
                map.put(mFRRSFRBZ, rFRRSFRBZ);

                final Major mFRRSFRFZ = new Major(6082, "FRRS-FRFZ-BS",
                        Boolean.TRUE, "Forest and Rangeland Stewardship",
                        "Forest Fire Science",
                        CAT + "natural-resources/forest-rangeland-stewardship/"
                                + "forest-rangeland-stewardship-major/"
                                + "forest-fire-science-concentration/");
                final MajorMathRequirement rFRRSFRFZ = //
                        new MajorMathRequirement("FRRS-FRFZ-BS")
                                .setSemesterCourses("M 141!", null, null);
                map.put(mFRRSFRFZ, rFRRSFRFZ);

                final Major mFRRSFMGZ = new Major(6083, "FRRS-FMGZ-BS",
                        Boolean.TRUE, "Forest and Rangeland Stewardship",
                        "Forest Management",
                        CAT + "natural-resources/forest-rangeland-stewardship/"
                                + "forest-rangeland-stewardship-major/"
                                + "forest-management-concentration/");
                final MajorMathRequirement rFRRSFMGZ = //
                        new MajorMathRequirement("FRRS-FMGZ-BS")
                                .setSemesterCourses("M 117!,M 118!", "M 141!",
                                        null);
                map.put(mFRRSFMGZ, rFRRSFMGZ);

                final Major mFRRSRFMZ = new Major(6084, "FRRS-RFMZ-BS",
                        Boolean.TRUE, "Forest and Rangeland Stewardship",
                        "Rangeland and Forest Management",
                        CAT + "natural-resources/forest-rangeland-stewardship/"
                                + "forest-rangeland-stewardship-major/"
                                + "rangeland-forest-management-concentration/");
                final MajorMathRequirement rFRRSRFMZ = //
                        new MajorMathRequirement("FRRS-RFMZ-BS")
                                .setSemesterCourses("M 141!", null, null);
                map.put(mFRRSRFMZ, rFRRSRFMZ);

                final Major mFRRSRCMZ = new Major(6085, "FRRS-RCMZ-BS",
                        Boolean.TRUE, "Forest and Rangeland Stewardship",
                        "Rangeland Conservation and Management",
                        CAT + "natural-resources/forest-rangeland-stewardship/"
                                + "forest-rangeland-stewardship-major/"
                                + "rangeland-conservation-management-concentration/");
                final MajorMathRequirement rFRRSRCMZ = //
                        new MajorMathRequirement("FRRS-RCMZ-BS")
                                .setSemesterCourses(FRRS3, null, null);
                map.put(mFRRSRCMZ, rFRRSRCMZ);

                // *** Major in Geology (with four concentrations)

                final Major mGEOL = new Major(6020, "GEOL-BS", Boolean.FALSE,
                        "Geology",
                        CAT + "natural-resources/geosciences/geology-major/");
                final MajorMathRequirement rGEOL = new MajorMathRequirement("GEOL-BS")
                        .setSemesterCourses("M 124!,M 125!,M 126.",
                                "M 160.", M_161);
                map.put(mGEOL, rGEOL);

                final Major mGEOLEVGZ = new Major(6021, "GEOL-EVGZ-BS",
                        Boolean.TRUE, "Geology", "Environmental Geology",
                        CAT + "natural-resources/geosciences/geology-major-"
                                + "environmental-concentration/");
                final MajorMathRequirement rGEOLEVGZ = //
                        new MajorMathRequirement("GEOL-EVGZ-BS")
                                .setSemesterCourses("M 160.", null, M_161);
                map.put(mGEOLEVGZ, rGEOLEVGZ);

                final Major mGEOLGEOZ = new Major(6022, "GEOL-GEOZ-BS",
                        Boolean.TRUE, "Geology", "Geology",
                        CAT + "natural-resources/geosciences/geology-major-"
                                + "geology-concentration/");
                final MajorMathRequirement rGEOLGEOZ = //
                        new MajorMathRequirement("GEOL-GEOZ-BS")
                                .setSemesterCourses("M 124!,M 125!,M 126.",
                                        "M 160.", M_161);
                map.put(mGEOLGEOZ, rGEOLGEOZ);

                final Major mGEOLGPYZ = new Major(6023, "GEOL-GPYZ-BS",
                        Boolean.TRUE, "Geology", "Geophysics",
                        CAT + "natural-resources/geosciences/geology-major-"
                                + "geophysics-concentration/");
                final MajorMathRequirement rGEOLGPYZ = //
                        new MajorMathRequirement("GEOL-GPYZ-BS")
                                .setSemesterCourses(null, "M 160.",
                                        "M 151,M 161,M 261,M 340");
                map.put(mGEOLGPYZ, rGEOLGPYZ);

                final Major mGEOLHYDZ = new Major(6024, "GEOL-HYDZ-BS",
                        Boolean.TRUE, "Geology", "Hydrogeology",
                        CAT + "natural-resources/geosciences/geology-major-"
                                + "hydrogeology-concentration/");
                final MajorMathRequirement rGEOLHYDZ = //
                        new MajorMathRequirement("GEOL-HYDZ-BS")
                                .setSemesterCourses("M 124!,M 125!,M 126.",
                                        "M 160!", "M 161,M 261,M 340");
                map.put(mGEOLHYDZ, rGEOLHYDZ);

                // *** Major in Human Dimensions of Natural Resources

                final Major mHDNR = new Major(6030, "HDNR-BS", Boolean.TRUE,
                        "Human Dimensions of Natural Resources",
                        CAT + "natural-resources/human-dimensions-natural-resources/"
                                + "human-dimensions-natural-resources-major/");
                final MajorMathRequirement rHDNR = new MajorMathRequirement("HDNR-BS")
                        .setSemesterCourses("M 117!,M 118!,M 124.", null, null);
                map.put(mHDNR, rHDNR);

                // *** Major in Natural Resource Tourism (with two concentrations)

                final Major mNRRT = new Major(6040, "NRRT-BS", Boolean.FALSE,
                        "Natural Resource Tourism",
                        CAT + "natural-resources/human-dimensions-natural-resources/"
                                + "natural-resource-tourism-major/");
                final MajorMathRequirement rNRRT = new MajorMathRequirement("NRRT-BS")
                        .setSemesterCourses("M 117!,M 118!,M 124.", null, null);
                map.put(mNRRT, rNRRT);

                final Major mNRRTGLTZ = new Major(6041, "NRRT-GLTZ-BS",
                        Boolean.TRUE, "Natural Resource Tourism",
                        "Global Tourism",
                        CAT + "natural-resources/human-dimensions-natural-resources/"
                                + "natural-resource-tourism-major-global-concentration/");
                final MajorMathRequirement rNRRTGLTZ = //
                        new MajorMathRequirement("NRRT-GLTZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124.", null, null);
                map.put(mNRRTGLTZ, rNRRTGLTZ);

                final Major mNRRTNRTZ = new Major(6042, "NRRT-NRTZ-BS",
                        Boolean.TRUE, "Natural Resource Tourism",
                        "Natural Resource Tourism",
                        CAT + "natural-resources/human-dimensions-natural-resources/"
                                + "natural-resource-tourism-major-natural-resource-tourism-"
                                + "concentration/");
                final MajorMathRequirement rNRRTNRTZ = //
                        new MajorMathRequirement("NRRT-NRTZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124.", null, null);
                map.put(mNRRTNRTZ, rNRRTNRTZ);

                // *** Major in Natural Resources Management

                final Major mNRMG = new Major(6050, "NRMG-BS", Boolean.TRUE,
                        "Natural Resources Management",
                        CAT + "natural-resources/forest-rangeland-stewardship/natural-"
                                + "resources-management-major/");
                final MajorMathRequirement rNRMG = new MajorMathRequirement("NRMG-BS")
                        .setSemesterCourses("M 117!,M 118!,M 125!", null, null);
                map.put(mNRMG, rNRMG);

                // *** Major in Restoration Ecology

                final Major mRECO = new Major(6090, "RECO-BS", Boolean.TRUE,
                        "Restoration Ecology",
                        CAT + "natural-resources/forest-rangeland-stewardship/"
                                + "restoration-ecology-major/");
                final MajorMathRequirement rRECO = new MajorMathRequirement("RECO-BS")
                        .setSemesterCourses(FRRS3, null, null);
                map.put(mRECO, rRECO);

                // *** Major in Watershed Science and Sustsainability

                final Major mWRSC = new Major(6070, "WRSC-BS", Boolean.TRUE,
                        "Watershed Science and Sustainability",
                        CAT + "natural-resources/ecosystem-science-sustainability/"
                                + "watershed-science-sustainability-major/");
                final MajorMathRequirement rWRSC = new MajorMathRequirement("WRSC-BS")
                        .setSemesterCourses(null, "CALC1BIO!", CALC2BIO);
                map.put(mWRSC, rWRSC);

                final Major mWRSCWSDZ = new Major(6071, "WRSC-WSDZ-BS",
                        Boolean.TRUE, "Watershed Science and Sustainability",
                        "Watershed Data",
                        CAT + "natural-resources/ecosystem-science-sustainability/"
                                + "watershed-science-sustainability-major-watershed-"
                                + "data-concentration/");
                final MajorMathRequirement rWRSCWSDZ = //
                        new MajorMathRequirement("WRSC-WSDZ-BS")
                                .setSemesterCourses("CALC1BIO!", null, null);
                map.put(mWRSCWSDZ, rWRSCWSDZ);

                final Major mWRSCWSSZ = new Major(6072, "WRSC-WSSZ-BS",
                        Boolean.TRUE, "Watershed Science and Sustainability",
                        "Watershed Science",
                        CAT + "natural-resources/ecosystem-science-sustainability/"
                                + "watershed-science-sustainability-major-watershed-"
                                + "science-concentration/");
                final MajorMathRequirement rWRSCWSSZ = //
                        new MajorMathRequirement("WRSC-WSSZ-BS")
                                .setSemesterCourses(null, null, CALC1BIO);
                map.put(mWRSCWSSZ, rWRSCWSSZ);

                final Major mWRSCWSUZ = new Major(6073, "WRSC-WSUZ-BS",
                        Boolean.TRUE, "Watershed Science and Sustainability",
                        "Watershed Sustainability",
                        CAT + "natural-resources/ecosystem-science-sustainability/"
                                + "watershed-science-sustainability-major-watershed-"
                                + "sustainability-concentration/");
                final MajorMathRequirement rWRSCWSUZ = //
                        new MajorMathRequirement("WRSC-WSUZ-BS")
                                .setSemesterCourses(null, null, CALC);
                map.put(mWRSCWSUZ, rWRSCWSUZ);

                // ===========================
                // College of Natural Sciences
                // ===========================

                // *** Major in Biochemistry (with three concentrations)

                final Major mBCHM = new Major(7010, "BCHM-BS", Boolean.FALSE,
                        "Biochemistry",
                        CAT + "natural-sciences/biochemistry-molecular-biology/"
                                + "biochemistry-major/");
                final MajorMathRequirement rBCHM = new MajorMathRequirement("BCHM-BS")
                        .setSemesterCourses("CALC1BIO!", "CALC2BIO!", null);
                map.put(mBCHM, rBCHM);

                final Major mBCHMASBZ = new Major(7014, "BCHM-ASBZ-BS",
                        Boolean.TRUE, "Biochemistry", "ASBMB",
                        CAT + "natural-sciences/biochemistry-molecular-biology/"
                                + "biochemistry-major-asbmb-concentration/");
                final MajorMathRequirement rBCHMASBZ = //
                        new MajorMathRequirement("BCHM-ASBZ-BS")
                                .setSemesterCourses("CALC1BIO!", "CALC2BIO!",
                                        null);
                map.put(mBCHMASBZ, rBCHMASBZ);

                final Major mBCHMDTSZ = new Major(7015, "BCHM-DTSZ-BS",
                        Boolean.TRUE, "Biochemistry", "Data Science",
                        CAT + "natural-sciences/biochemistry-molecular-biology/"
                                + "biochemistry-major-data-science-concentration/");
                final MajorMathRequirement rBCHMDTSZ = //
                        new MajorMathRequirement("BCHM-DTSZ-BS")
                                .setSemesterCourses("M 155!", "M 255!", null);
                map.put(mBCHMDTSZ, rBCHMDTSZ);

                final Major mBCHMHMSZ = new Major(7012, "BCHM-HMSZ-BS",
                        Boolean.TRUE, "Biochemistry",
                        "Health and Medical Sciences",
                        CAT + "natural-sciences/biochemistry-molecular-biology/"
                                + "biochemistry-major-health-medical-sciences-"
                                + "concentration/");
                final MajorMathRequirement rBCHMHMSZ = //
                        new MajorMathRequirement("BCHM-HMSZ-BS")
                                .setSemesterCourses("CALC1BIO!", "CALC2BIO!",
                                        null);
                map.put(mBCHMHMSZ, rBCHMHMSZ);

                final Major mBCHMPPHZ = new Major(7013, "BCHM-PPHZ-BS",
                        Boolean.TRUE, "Biochemistry", "Pre-Pharmacy",
                        CAT + "natural-sciences/biochemistry-molecular-biology/"
                                + "biochemistry-major-prepharmacy-concentration/");
                final MajorMathRequirement rBCHMPPHZ = //
                        new MajorMathRequirement("BCHM-PPHZ-BS")
                                .setSemesterCourses("CALC1BIO!", "CALC2BIO!",
                                        null);
                map.put(mBCHMPPHZ, rBCHMPPHZ);

                // *** Major in Biological Science (with two concentrations)

                final Major mBLSC = new Major(7020, "BLSC-BS", Boolean.FALSE,
                        "Biological Science",
                        CAT + "natural-sciences/biology/biological-science-major/");
                final MajorMathRequirement rBLSC = new MajorMathRequirement("BLSC-BS")
                        .setSemesterCourses("M 117.,M 118.",
                                "M 124!,M 125!,CALC1BIO.", null);
                map.put(mBLSC, rBLSC);

                final Major mBLSCBLSZ = new Major(7021, "BLSC-BLSZ-BS",
                        Boolean.TRUE, "Biological Science",
                        "Biological Science",
                        CAT + "natural-sciences/biology/biological-science-major-"
                                + "biological-science-concentration/");
                final MajorMathRequirement rBLSCBLSZ = //
                        new MajorMathRequirement("BLSC-BLSZ-BS")
                                .setSemesterCourses("M 117.,M 118.",
                                        "M 124!,M 125!,CALC1BIO.", null);
                map.put(mBLSCBLSZ, rBLSCBLSZ);

                final Major mBLSCBTNZ = new Major(7022, "BLSC-BTNZ-BS",
                        Boolean.TRUE, "Biological Science", "Botany",
                        CAT + "natural-sciences/biology/biological-science-major-"
                                + "botany-concentration/");
                final MajorMathRequirement rBLSCBTNZ = //
                        new MajorMathRequirement("BLSC-BTNZ-BS")
                                .setSemesterCourses("M 117.,M 118.",
                                        "M 124!,M 125!,CALC1BIO", null);
                map.put(mBLSCBTNZ, rBLSCBTNZ);

                // *** Major in Chemistry (with two concentrations)

                final Major mCHEM = new Major(7030, "CHEM-BS", Boolean.TRUE,
                        "Chemistry",
                        CAT + "natural-sciences/chemistry/chemistry-major/");
                final MajorMathRequirement rCHEM = new MajorMathRequirement("CHEM-BS")
                        .setSemesterCourses(null, "M 160!",
                                "CALC2CHM,CALC3CHM");
                map.put(mCHEM, rCHEM);

                // DEACTIVATED (commented to preserve what the historical number represents):

                // final Major mCHEMACSZ = new Major(7031, "CHEM-ACSZ-BS", 
                // Boolean.TRUE, "Chemistry", "ACS Certified",  
                // CAT + "natural-sciences/chemistry/chemistry-major-acs-" 
                // + "certified-concentration/"); 
                // final MajorMathRequirement rCHEMACSZ = //
                // new MajorMathRequirement("CHEM-ACSZ-BS") 
                // .setSemesterCourses("M 160!", null, 
                // "CALC2CHM,CALC3CHM"); 
                // map.put(mCHEMACSZ, rCHEMACSZ);

                // DEACTIVATED (commented to preserve what the historical number represents):

                // final Major mCHEMNACZ = new Major(7032, "CHEM-NACZ-BS", 
                // Boolean.TRUE, "Chemistry", "Non-ACS Certified",  
                // CAT + "natural-sciences/chemistry/chemistry-major-non-acs-" 
                // + "certified-concentration/"); 
                // final MajorMathRequirement rCHEMNACZ = //
                // new MajorMathRequirement("CHEM-NACZ-BS") 
                // .setSemesterCourses("M 160!", null, 
                // "CALC2CHM,CALC3CHM"); 
                // map.put(mCHEMNACZ, rCHEMNACZ);

                final Major mCHEMECHZ = new Major(7033, "CHEM-ECHZ-BS",
                        Boolean.TRUE, "Chemistry",
                        "Environmental Chemistry",
                        CAT + "natural-sciences/chemistry/chemistry-major/"
                                + "environmental-chemistry-concentration/");
                final MajorMathRequirement rCHEMECHZ = //
                        new MajorMathRequirement("CHEM-ECHZ-BS")
                                .setSemesterCourses(null, "CALC1BIO!",
                                        "CALC2CHM,CALC3CHM");
                map.put(mCHEMECHZ, rCHEMECHZ);

                final Major mCHEMFCHZ = new Major(7034, "CHEM-FCHZ-BS",
                        Boolean.TRUE, "Chemistry",
                        "Forensic Chemistry",
                        CAT + "natural-sciences/chemistry/chemistry-major/"
                                + "forensic-chemistry-concentration/");
                final MajorMathRequirement rCHEMFCHZ = //
                        new MajorMathRequirement("CHEM-FCHZ-BS")
                                .setSemesterCourses(null, "CALC1BIO!",
                                        "CALC2CHM,CALC3CHM");
                map.put(mCHEMFCHZ, rCHEMFCHZ);

                final Major mCHEMHSCZ = new Major(7035, "CHEM-HSCZ-BS",
                        Boolean.TRUE, "Chemistry", "Health Sciences",
                        CAT + "natural-sciences/chemistry/chemistry-major/"
                                + "health-sciences-concentration/");
                final MajorMathRequirement rCHEMHSCZ = //
                        new MajorMathRequirement("CHEM-HSCZ-BS")
                                .setSemesterCourses(null, "CALC1BIO!",
                                        "CALC2CHM,CALC3CHM");
                map.put(mCHEMHSCZ, rCHEMHSCZ);

                final Major mCHEMSCHZ = new Major(7036, "CHEM-SCHZ-BS",
                        Boolean.TRUE, "Chemistry", "Sustainable Chemistry",
                        CAT + "natural-sciences/chemistry/chemistry-major/"
                                + "sustainable-chemistry-concentration/");
                final MajorMathRequirement rCHEMSCHZ = //
                        new MajorMathRequirement("CHEM-SCHZ-BS")
                                .setSemesterCourses(null, "CALC1BIO!",
                                        "CALC2CHM,CALC3CHM");
                map.put(mCHEMSCHZ, rCHEMSCHZ);

                // *** Major in Computer Science (with five concentrations)

                final Major mCPSC = new Major(7040, "CPSC-BS", Boolean.TRUE,
                        "Computer Science",
                        CAT + "natural-sciences/computer-science/"
                                + "computer-science-major/");
                final MajorMathRequirement rCPSC = new MajorMathRequirement("CPSC-BS")
                        .setSemesterCourses("M 124!,M 126!", CALC1CS,
                                LINALG369);
                map.put(mCPSC, rCPSC);

                final Major mCPSCCPSZ = new Major(7041, "CPSC-CPSZ-BS",
                        Boolean.TRUE, "Computer Science",
                        "Computer Science",
                        CAT + "natural-sciences/computer-science/computer-science-major/"
                                + "computer-science-concentration/");
                final MajorMathRequirement rCPSCCPSZ = //
                        new MajorMathRequirement("CPSC-CPSZ-BS")
                                .setSemesterCourses("M 124!,M 126!",
                                        CALC1CS, LINALG369);
                map.put(mCPSCCPSZ, rCPSCCPSZ);

                final Major mCPSCHCCZ = new Major(7042, "CPSC-HCCZ-BS",
                        Boolean.TRUE, "Computer Science",
                        "Human-Centered Computing",
                        CAT + "natural-sciences/computer-science/computer-science-major/"
                                + "human-centered-computing-concentration/");
                final MajorMathRequirement rCPSCHCCZ = //
                        new MajorMathRequirement("CPSC-HCCZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124!",
                                        "M 125!,M 126!", "CALC1CS,LINALG369");
                map.put(mCPSCHCCZ, rCPSCHCCZ);

                final Major mCPSCAIMZ = new Major(7043, "CPSC-AIMZ-BS",
                        Boolean.TRUE, "Computer Science",
                        "Artificial Intelligence and Machine Learning",
                        CAT + "natural-sciences/computer-science/"
                                + "computer-science-major-artificial-intelligence-"
                                + "machine-learning-concentration/");
                final MajorMathRequirement rCPSCAIMZ = //
                        new MajorMathRequirement("CPSC-AIMZ-BS")
                                .setSemesterCourses("M 124!,M 126!", CALC1CS,
                                        LINALG369);
                map.put(mCPSCAIMZ, rCPSCAIMZ);

                final Major mCPSCCSYZ = new Major(7044, "CPSC-CSYZ-BS",
                        Boolean.TRUE, "Computer Science",
                        "Computing Systems",
                        CAT + "natural-sciences/computer-science/"
                                + "computer-science-major-computing-systems-concentration/");
                final MajorMathRequirement rCPSCCSYZ = //
                        new MajorMathRequirement("CPSC-CSYZ-BS")
                                .setSemesterCourses("M 124!,M 126!", CALC1CS,
                                        LINALG369);
                map.put(mCPSCCSYZ, rCPSCCSYZ);

                final Major mCPSCNSCZ = new Major(7045, "CPSC-NSCZ-BS",
                        Boolean.TRUE, "Computer Science",
                        "Networks and Security",
                        CAT + "natural-sciences/computer-science/"
                                + "computer-science-major-networks-security-concentration/");
                final MajorMathRequirement rCPSCNSCZ = //
                        new MajorMathRequirement("CPSC-NSCZ-BS")
                                .setSemesterCourses("M 124!,M 126!", CALC1CS,
                                        LINALG369);
                map.put(mCPSCNSCZ, rCPSCNSCZ);

                final Major mCPSCSEGZ = new Major(7046, "CPSC-SEGZ-BS",
                        Boolean.TRUE, "Computer Science",
                        "Software Engineering",
                        CAT + "natural-sciences/computer-science/"
                                + "computer-science-major-software-engineering-concentration/");
                final MajorMathRequirement rCPSCSEGZ = //
                        new MajorMathRequirement("CPSC-SEGZ-BS")
                                .setSemesterCourses("M 124!,M 126!", CALC1CS,
                                        LINALG369);
                map.put(mCPSCSEGZ, rCPSCSEGZ);

                final Major mCPSCCSEZ = new Major(7047, "CPSC-CSEZ-BS",
                        Boolean.TRUE, "Computer Science",
                        "Computer Science Education",
                        CAT + "natural-sciences/computer-science/computer-science-major/"
                                + "computer-science-education-concentration/");
                final MajorMathRequirement rCPSCCSEZ = //
                        new MajorMathRequirement("CPSC-CSEZ-BS")
                                .setSemesterCourses("M 124!,M 126!", CALC1CS,
                                        LINALG369);
                map.put(mCPSCCSEZ, rCPSCCSEZ);

                // *** Major in Data Science (with four concentrations)

                final Major mDSCI = new Major(7050, "DSCI-BS", Boolean.FALSE,
                        "Data Science",
                        CAT + "natural-sciences/data-science-major/");
                final MajorMathRequirement rDSCI = new MajorMathRequirement("DSCI-BS")
                        .setSemesterCourses(M_160, M_161, "M 151,M 261");
                map.put(mDSCI, rDSCI);

                final Major mDSCICSCZ = new Major(7051, "DSCI-CSCZ-BS",
                        Boolean.TRUE, "Data Science", "Computer Science",
                        CAT + "natural-sciences/data-science-major/computer-science-"
                                + "concentration/");
                final MajorMathRequirement rDSCICSCZ = //
                        new MajorMathRequirement("DSCI-CSCZ-BS")
                                .setSemesterCourses(M_160, M_161, "M 151,M 261");
                map.put(mDSCICSCZ, rDSCICSCZ);

                final Major mDSCIECNZ = new Major(7052, "DSCI-ECNZ-BS",
                        Boolean.TRUE, "Data Science", "Economics",
                        CAT + "natural-sciences/data-science-major/economics-"
                                + "concentration/");
                final MajorMathRequirement rDSCIECNZ = //
                        new MajorMathRequirement("DSCI-ECNZ-BS")
                                .setSemesterCourses(M_160, M_161, "M 151,M 261");
                map.put(mDSCIECNZ, rDSCIECNZ);

                final Major mDSCIMATZ = new Major(7053, "DSCI-MATZ-BS",
                        Boolean.TRUE, "Data Science", "Mathematics",
                        CAT + "natural-sciences/data-science-major/mathematics-"
                                + "concentration/");
                final MajorMathRequirement rDSCIMATZ = //
                        new MajorMathRequirement("DSCI-MATZ-BS")
                                .setSemesterCourses(M_160, M_161, "M 151,M 261");
                map.put(mDSCIMATZ, rDSCIMATZ);

                final Major mDSCISTSZ = new Major(7054, "DSCI-STSZ-BS",
                        Boolean.TRUE, "Data Science", "Statistics",
                        CAT + "natural-sciences/data-science-major/statistics-"
                                + "concentration/");
                final MajorMathRequirement rDSCISTSZ = //
                        new MajorMathRequirement("DSCI-STSZ-BS")
                                .setSemesterCourses(M_160, M_161, "M 151,M 261");
                map.put(mDSCISTSZ, rDSCISTSZ);

                final Major mDSCINEUZ = new Major(7055, "DSCI-NEUZ-BS",
                        Boolean.TRUE, "Data Science", "Neuroscience",
                        CAT + "natural-sciences/data-science-major/"
                                + "neuroscience-concentration/");
                final MajorMathRequirement rDSCINEUZ = //
                        new MajorMathRequirement("DSCI-NEUZ-BS")
                                .setSemesterCourses(M_160, M_161, "M 151,M 261");
                map.put(mDSCINEUZ, rDSCINEUZ);

                // *** Major in Mathematics (with four concentrations)

                final Major mMATH = new Major(7060, "MATH-BS", Boolean.FALSE,
                        "Mathematics",
                        CAT + "natural-sciences/mathematics/mathematics-major/");
                final MajorMathRequirement rMATH = new MajorMathRequirement("MATH-BS")
                        .setSemesterCourses("M 124!,M 126!,M 160.,M 192",
                                "M 161.", "M 235,M 261,M 317,M 369,ODE");
                map.put(mMATH, rMATH);

                final Major mMATHALSZ = new Major(7061, "MATH-ALSZ-BS",
                        Boolean.TRUE, "Mathematics", "Actuarial Sciences",
                        CAT + "natural-sciences/mathematics/mathematics-major-actuarial-"
                                + "science-concentration/");
                final MajorMathRequirement rMATHALSZ = //
                        new MajorMathRequirement("MATH-ALSZ-BS")
                                .setSemesterCourses("M 160!,M 192", "M 161.",
                                        "M 235,M 261,M 317,ODE,M 369,M 495");
                map.put(mMATHALSZ, rMATHALSZ);

                final Major mMATHAMTZ = new Major(7062, "MATH-AMTZ-BS",
                        Boolean.TRUE, "Mathematics", "Applied Mathematics",
                        CAT + "natural-sciences/mathematics/mathematics-major-applied-"
                                + "concentration/");
                final MajorMathRequirement rMATHAMTZ = //
                        new MajorMathRequirement("MATH-AMTZ-BS")
                                .setSemesterCourses("M 124!,M 126!,M 160.,M 192",
                                        "M 161.",
                                        "M 235,M 261,M 317,ODE,M 369,M 435,M 450,M 451");
                map.put(mMATHAMTZ, rMATHAMTZ);

                final Major mMATHGNMZ = new Major(7064, "MATH-GNMZ-BS",
                        Boolean.TRUE, "Mathematics", "General Mathematics",
                        CAT + "natural-sciences/mathematics/mathematics-major-"
                                + "general-concentration/");
                final MajorMathRequirement rMATHGNMZ = //
                        new MajorMathRequirement("MATH-GNMZ-BS")
                                .setSemesterCourses("M 124!,M 126!,M 160.,M 192",
                                        "M 161.",
                                        "M 235,M 261,M 317,MATH2,M 369,ODE,MATH4");
                map.put(mMATHGNMZ, rMATHGNMZ);

                final Major mMATHMTEZ = new Major(7065, "MATH-MTEZ-BS",
                        Boolean.TRUE, "Mathematics",
                        "Mathematics Education",
                        CAT + "natural-sciences/mathematics/mathematics-major-"
                                + "education-concentration/");
                final MajorMathRequirement rMATHMTEZ = //
                        new MajorMathRequirement("MATH-MTEZ-BS")
                                .setSemesterCourses("M 124!,M 126!,M 160.,M 192",
                                        "M 161.",
                                        "M 230,M 261,M 317,M 366,M 369,M 425,M 470");
                map.put(mMATHMTEZ, rMATHMTEZ);

                // *** Major in Natural Sciences (with five concentrations)

                final Major mNSCI = new Major(7070, "NSCI-BS", Boolean.FALSE,
                        "Natural Sciences",
                        CAT + "natural-sciences/natural-sciences-major/");
                final MajorMathRequirement rNSCI = new MajorMathRequirement("NSCI-BS")
                        .setSemesterCourses("M 117!,M 118!,CALC1BIO.",
                                "CALC2BIO.", null);
                map.put(mNSCI, rNSCI);

                final Major mNSCIBLEZ = new Major(7071, "NSCI-BLEZ-BS",
                        Boolean.TRUE, "Natural Sciences",
                        "Biology Education",
                        CAT + "natural-sciences/natural-sciences-major-biology-"
                                + "education-concentration/");
                final MajorMathRequirement rNSCIBLEZ = //
                        new MajorMathRequirement("NSCI-BLEZ-BS")
                                .setSemesterCourses("M 117!,M 118!",
                                        "CALC1BIO.", null);
                map.put(mNSCIBLEZ, rNSCIBLEZ);

                final Major mNSCICHEZ = new Major(7072, "NSCI-CHEZ-BS",
                        Boolean.TRUE, "Natural Sciences",
                        "Chemistry Education",
                        CAT + "natural-sciences/natural-sciences-major-chemistry-"
                                + "education-concentration/");
                final MajorMathRequirement rNSCICHEZ = //
                        new MajorMathRequirement("NSCI-CHEZ-BS")
                                .setSemesterCourses("M 117!,M 118!,CALC1BIO.",
                                        "CALC2BIO.", null);
                map.put(mNSCICHEZ, rNSCICHEZ);

                final Major mNSCIGLEZ = new Major(7073, "NSCI-GLEZ-BS",
                        Boolean.TRUE, "Natural Sciences",
                        "Geology Education",
                        CAT + "natural-sciences/natural-sciences-major-geology-"
                                + "education-concentration/");
                final MajorMathRequirement rNSCIGLEZ = //
                        new MajorMathRequirement("NSCI-GLEZ-BS")
                                .setSemesterCourses("M 117!,M 118!,CALC1BIO.",
                                        "CALC2BIO.", null);
                map.put(mNSCIGLEZ, rNSCIGLEZ);

                final Major mNSCIPHSZ = new Major(7074, "NSCI-PHSZ-BS",
                        Boolean.TRUE, "Natural Sciences",
                        "Physical Science",
                        CAT + "natural-sciences/natural-sciences-major-physical-"
                                + "science-concentration/");
                final MajorMathRequirement rNSCIPHSZ = //
                        new MajorMathRequirement("NSCI-PHSZ-BS")
                                .setSemesterCourses("M 117!,M 118!,CALC1BIO.",
                                        "CALC2BIO.", null);
                map.put(mNSCIPHSZ, rNSCIPHSZ);

                final Major mNSCIPHEZ = new Major(7075, "NSCI-PHEZ-BS",
                        Boolean.TRUE, "Natural Sciences",
                        "Physics Education",
                        CAT + "natural-sciences/natural-sciences-major-physics-"
                                + "education-concentration/");
                final MajorMathRequirement rNSCIPHEZ = //
                        new MajorMathRequirement("NSCI-PHEZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 160.",
                                        "M 161.", M_261);
                map.put(mNSCIPHEZ, rNSCIPHEZ);

                // *** Major in Physics (with two concentrations)

                final Major mPHYS = new Major(7080, "PHYS-BS", Boolean.FALSE,
                        "Physics",
                        CAT + "natural-sciences/physics/physics-major/");
                final MajorMathRequirement rPHYS = new MajorMathRequirement("PHYS-BS")
                        .setSemesterCourses("M 160.", "M 161.",
                                "M 261,ODE");
                map.put(mPHYS, rPHYS);

                final Major mPHYSAPPZ = new Major(7081, "PHYS-APPZ-BS",
                        Boolean.TRUE, "Physics", "Applied Physics",
                        CAT + "natural-sciences/physics/physics-major-applied-"
                                + "concentration/");
                final MajorMathRequirement rPHYSAPPZ = //
                        new MajorMathRequirement("PHYS-APPZ-BS")
                                .setSemesterCourses("M 160.", "M 161.",
                                        "M 261,ODE");
                map.put(mPHYSAPPZ, rPHYSAPPZ);

                final Major mPHYSPHYZ = new Major(7082, "PHYS-PHYZ-BS",
                        Boolean.TRUE, "Physics", "Physics",
                        CAT + "natural-sciences/physics/physics-major-physics-"
                                + "concentration/");
                final MajorMathRequirement rPHYSPHYZ = //
                        new MajorMathRequirement("PHYS-PHYZ-BS")
                                .setSemesterCourses("M 160.", "M 161.",
                                        "M 261,ODE");
                map.put(mPHYSPHYZ, rPHYSPHYZ);

                // *** Major in Psychology (with five concentrations)

                final Major mPSYC = new Major(7090, "PSYC-BS", Boolean.FALSE,
                        "Psychology",
                        CAT + "natural-sciences/psychology/psychology-major/");
                final MajorMathRequirement rPSYC = new MajorMathRequirement("PSYC-BS")
                        .setSemesterCourses("M 117!", "M 118!,M 124!",
                                null);
                map.put(mPSYC, rPSYC);

                final Major mPSYCADCZ = new Major(7091, "PSYC-ADCZ-BS",
                        Boolean.TRUE, "Psychology", "Addictions Counseling",
                        CAT + "natural-sciences/psychology/psychology-major-"
                                + "addictions-counseling-concentration/");
                final MajorMathRequirement rPSYCADCZ = //
                        new MajorMathRequirement("PSYC-ADCZ-BS")
                                .setSemesterCourses("M 117!", "M 118!,M 124!",
                                        null);
                map.put(mPSYCADCZ, rPSYCADCZ);

                final Major mPSYCCCPZ = new Major(7092, "PSYC-CCPZ-BS",
                        Boolean.TRUE, "Psychology",
                        "Clinical/Counseling Psychology",
                        CAT + "natural-sciences/psychology/psychology-major-"
                                + "clinical-counseling-concentration/index.html");
                final MajorMathRequirement rPSYCCCPZ = //
                        new MajorMathRequirement("PSYC-CCPZ-BS")
                                .setSemesterCourses("M 117!", "M 118!,M 124!",
                                        null);
                map.put(mPSYCCCPZ, rPSYCCCPZ);

                final Major mPSYCGPSZ = new Major(7093, "PSYC-GPSZ-BS",
                        Boolean.TRUE, "Psychology", "General Psychology",
                        CAT + "natural-sciences/psychology/psychology-major-"
                                + "general-concentration/");
                final MajorMathRequirement rPSYCGPSZ = //
                        new MajorMathRequirement("PSYC-GPSZ-BS")
                                .setSemesterCourses("M 117!", "M 118!,M 124!",
                                        null);
                map.put(mPSYCGPSZ, rPSYCGPSZ);

                final Major mPSYCIOPZ = new Major(7094, "PSYC-IOPZ-BS",
                        Boolean.TRUE, "Psychology",
                        "Industrial/Organizational",
                        CAT + "natural-sciences/psychology/psychology-major-"
                                + "industrial-organizational-concentration/");
                final MajorMathRequirement rPSYCIOPZ = //
                        new MajorMathRequirement("PSYC-IOPZ-BS")
                                .setSemesterCourses("M 117!", "M 118!,M 124!",
                                        null);
                map.put(mPSYCIOPZ, rPSYCIOPZ);

                final Major mPSYCMBBZ = new Major(7095, "PSYC-MBBZ-BS",
                        Boolean.TRUE, "Psychology",
                        "Mind, Brain, and Behavior",
                        CAT + "natural-sciences/psychology/psychology-major-"
                                + "mind-brain-behavior-concentration/");
                final MajorMathRequirement rPSYCMBBZ = //
                        new MajorMathRequirement("PSYC-MBBZ-BS")
                                .setSemesterCourses("M 117!", "M 118!,M 124!",
                                        null);
                map.put(mPSYCMBBZ, rPSYCMBBZ);

                // *** Major in Statistics

                final Major mSTAT = new Major(7100, "STAT-BS", Boolean.FALSE,
                        "Statistics",
                        CAT + "natural-sciences/statistics/statistics-major/");
                final MajorMathRequirement rSTAT = new MajorMathRequirement("STAT-BS")
                        .setSemesterCourses("M 160!", "M 161!",
                                "M 261,M 235,M 369");
                map.put(mSTAT, rSTAT);

                // DEACTIVATED (commented to preserve what the historical number represents):

                // final Major mSTATGSTZ = new Major(7101, "STAT-GSTZ-BS", 
                // Boolean.TRUE, "Statistics", 
                // "General Statistics", 
                // CAT + "natural-sciences/statistics/statistics-major/" 
                // + "general-statistics-concentration/"); 
                // final MajorMathRequirement rSTATGSTZ = //
                // new MajorMathRequirement("STAT-GSTZ-BS") 
                // .setSemesterCourses("M 160!", "M 161!",  
                // "M 261,M 235,M 369,STAT1"); 
                // map.put(mSTATGSTZ, rSTATGSTZ);

                // DEACTIVATED (commented to preserve what the historical number represents):

                // final Major mSTATMSTZ = new Major(7102, "STAT-MSTZ-BS", 
                // Boolean.TRUE, "Statistics", 
                // "Mathematical Statistics", 
                // CAT + "natural-sciences/statistics/statistics-major/" 
                // + "mathematical-statistics-concentration/"); 
                // final MajorMathRequirement rSTATMSTZ = //
                // new MajorMathRequirement("STAT-MSTZ-BS") 
                // .setSemesterCourses("M 160.", "M 161.",  
                // "M 261,M 235,M 317,M 345,M 369,M 417,STAT2"); 
                // map.put(mSTATMSTZ, rSTATMSTZ);

                // *** Major in Zoology

                final Major mZOOL = new Major(7110, "ZOOL-BS", Boolean.TRUE,
                        "Zoology",
                        CAT + "natural-sciences/biology/zoology-major/");
                final MajorMathRequirement rZOOL = new MajorMathRequirement("ZOOL-BS")
                        .setSemesterCourses("M 117!,M 118!", CALC1BIO, null);
                map.put(mZOOL, rZOOL);

                // ======================================================
                // College of Veterinary Medicine and Biomedical Sciences
                // ======================================================

                // *** Major in Biomedical Sciences (with three concentrations)

                final Major mBIOM = new Major(8000, "BIOM-BS", Boolean.FALSE,
                        "Biomedical Sciences",
                        CAT + "veterinary-medicine-biomedical-sciences/"
                                + "biomedical-sciences/biomedical-sciences-major/");
                final MajorMathRequirement rBIOM = new MajorMathRequirement("BIOM-BS")
                        .setSemesterCourses("M 124,M 125,M 126", CALC1BIO, null);
                map.put(mBIOM, rBIOM);

                final Major mBIOMAPHZ = new Major(8001, "BIOM-APHZ-BS",
                        Boolean.TRUE, "Biomedical Sciences",
                        "Anatomy and Physiology",
                        CAT + "veterinary-medicine-biomedical-sciences/"
                                + "biomedical-sciences/"
                                + "biomedical-sciences-major-anatomy-physiology-concentration/");
                final MajorMathRequirement rBIOMAPHZ = //
                        new MajorMathRequirement("BIOM-APHZ-BS")
                                .setSemesterCourses("M 124,M 125,M 126", CALC1BIO, null);
                map.put(mBIOMAPHZ, rBIOMAPHZ);

                final Major mBIOMEPHZ = new Major(8002, "BIOM-EPHZ-BS",
                        Boolean.TRUE, "Biomedical Sciences",
                        "Environmental Public Health",
                        CAT + "veterinary-medicine-biomedical-sciences/"
                                + "environmental-radiological-health-sciences/"
                                + "biomedical-sciences-major-environmental-public-health-concentration/");
                final MajorMathRequirement rBIOMEPHZ = //
                        new MajorMathRequirement("BIOM-EPHZ-BS")
                                .setSemesterCourses("M 117,M 118,M 124", CALC1BIO, null);
                map.put(mBIOMEPHZ, rBIOMEPHZ);

                final Major mBIOMMIDZ = new Major(8003, "BIOM-MIDZ-BS",
                        Boolean.TRUE, "Biomedical Sciences",
                        "Microbiology and Infectious Disease",
                        CAT + "veterinary-medicine-biomedical-sciences/"
                                + "microbiology-immunology-pathology/"
                                + "biomedical-sciences-major-microbiology-infectious-disease-concentration/");
                final MajorMathRequirement rBIOMMIDZ = //
                        new MajorMathRequirement("BIOM-MIDZ-BS")
                                .setSemesterCourses("M 117,M 118,M 124", CALC1BIO, null);
                map.put(mBIOMMIDZ, rBIOMMIDZ);

                // *** Major in Environmental Health

                // DEACTIVATED (commented to preserve what the historical number represents):

                // final Major mEVHL = new Major(8010, "EVHL-BS", Boolean.TRUE, 
                // "Environmental Health", 
                // CAT + "veterinary-medicine-biomedical-sciences/" 
                // + "environmental-radiological-health-sciences/" 
                // + "environmental-health-major/"); 
                // final MajorMathRequirement rEVHL = new MajorMathRequirement("EVHL-BS")
                // 
                // .setSemesterCourses("BIOL3", null, null); 
                // map.put(mEVHL, rEVHL);

                // *** Major in Microbiology

                // DEACTIVATED (commented to preserve what the historical number represents):

                // final Major mMICR = new Major(8020, "MICR-BS", Boolean.TRUE, 
                // "Microbiology", 
                // CAT + "veterinary-medicine-biomedical-sciences/microbiology-" 
                // + "immunology-pathology/microbiology-major/"); 
                // final MajorMathRequirement rMICR = new MajorMathRequirement("MICR-BS")
                // 
                // .setSemesterCourses("BIOL3", null, null); 
                // map.put(mMICR, rMICR);

                // *** Major in Neuroscience (with two concentrations)

                final Major mNERO = new Major(8030, "NERO-BS", Boolean.FALSE,
                        "Neuroscience",
                        CAT + "veterinary-medicine-biomedical-sciences/biomedical-"
                                + "sciences/neuroscience-major/");
                final MajorMathRequirement rNERO = new MajorMathRequirement("NERO-BS")
                        .setSemesterCourses("M 124!,M 125!,M 126!",
                                M_155, null);
                map.put(mNERO, rNERO);

                final Major mNEROBCNZ = new Major(8031, "NERO-BCNZ-BS",
                        Boolean.TRUE, "Neuroscience",
                        "Behavioral and Cognitive Neuroscience",
                        CAT + "veterinary-medicine-biomedical-sciences/biomedical-"
                                + "sciences/neuroscience-major-behavioral-cognitive-"
                                + "concentration/");
                final MajorMathRequirement rNEROBCNZ = //
                        new MajorMathRequirement("NERO-BCNZ-BS")
                                .setSemesterCourses("M 124!,M 125!,M 126!",
                                        M_155, null);
                map.put(mNEROBCNZ, rNEROBCNZ);

                final Major mNEROCMNZ = new Major(8032, "NERO-CMNZ-BS",
                        Boolean.TRUE, "Neuroscience",
                        "Cell and Molecular Neuroscience",
                        CAT + "veterinary-medicine-biomedical-sciences/biomedical-"
                                + "sciences/neuroscience-major-cell-molecular-"
                                + "concentration/");
                final MajorMathRequirement rNEROCMNZ = //
                        new MajorMathRequirement("NERO-CMNZ-BS")
                                .setSemesterCourses("M 124!,M 125!,M 126!",
                                        M_155, M_255);
                map.put(mNEROCMNZ, rNEROCMNZ);

                this.majors = new TreeMap<>(map);
            }
        }

        return this.majors;
    }

    /**
     * Gets the majors that require only 3 credits of AUCC mathematics.
     *
     * @return the list of majors
     */
    public List<Major> getMajorsRequiringAUCC() {

        categorizeMajors();

        return this.majorsNeedingAUCC;
    }

    /**
     * Gets the majors that require more than just any 3 credits of AUCC mathematics, but nothing higher level than a
     * pre-calculus course.
     *
     * @return the list of majors
     */
    public List<Major> getMajorsRequiringPrecalc() {

        categorizeMajors();

        return this.majorsNeedingPrecalc;
    }

    /**
     * Gets the majors that require a Calculus I course, but nothing higher.
     *
     * @return the list of majors
     */
    public List<Major> getMajorsRequiringCalc1() {

        categorizeMajors();

        return this.majorsNeedingCalc1;
    }

    /**
     * Gets the majors that require courses beyond a Calculus I course.
     *
     * @return the list of majors
     */
    public List<Major> getMajorsRequiringBeyondCalc1() {

        categorizeMajors();

        return this.majorsNeedingMore;
    }

//    /**
//     * Gets the majors that are in a specified college.
//     *
//     * @param collegeCode the college code
//     * @return the list of majors
//     */
//     List<Major> getMajorsInCollege(final String collegeCode) {
//
//     List<Major> result = new ArrayList<>(20);
//
//     for (Major test : this.majors.keySet()) {
//     if (test.getCollege().equals(collegeCode)) {
//     result.add(test);
//     }
//     }
//
//     return result;
//     }

    /**
     * Generates maps with majors sorted into categories by math requirement.
     */
    private void categorizeMajors() {

        synchronized (this.synch) {
            if (this.majorsNeedingPrecalc == null) {
                final Map<Major, MajorMathRequirement> allMajors = getMajors();

                // Go ahead and create all categories, since we're looping and testing anyway
                this.majorsNeedingAUCC = new ArrayList<>(50);
                this.majorsNeedingPrecalc = new ArrayList<>(50);
                this.majorsNeedingCalc1 = new ArrayList<>(50);
                this.majorsNeedingMore = new ArrayList<>(50);

                for (final Map.Entry<Major, MajorMathRequirement> entry : allMajors.entrySet()) {
                    final MajorMathRequirement req = entry.getValue();

                    if (req.isOnlyAUCC3()) {
                        this.majorsNeedingAUCC.add(entry.getKey());
                    } else if (req.isNothingBeyondPrecalc()) {
                        this.majorsNeedingPrecalc.add(entry.getKey());
                    } else if (req.isNothingBeyondCalc1()) {
                        this.majorsNeedingCalc1.add(entry.getKey());
                    } else {
                        this.majorsNeedingMore.add(entry.getKey());
                    }
                }

                // Now make sure that whenever a concentration appears in a block that the raw major
                // containing that concentration also appears.
                final Set<Major> majorSet = allMajors.keySet();
                verifyPresent(this.majorsNeedingAUCC, majorSet);
                verifyPresent(this.majorsNeedingPrecalc, majorSet);
                verifyPresent(this.majorsNeedingCalc1, majorSet);
                verifyPresent(this.majorsNeedingMore, majorSet);

                Collections.sort(this.majorsNeedingAUCC);
                Collections.sort(this.majorsNeedingPrecalc);
                Collections.sort(this.majorsNeedingCalc1);
                Collections.sort(this.majorsNeedingMore);
            }
        }
    }

    /**
     * Ensures that the raw major is present for every concentration in a list of majors.
     *
     * @param list      the list of majors
     * @param allMajors the set of all majors
     */
    private static void verifyPresent(final List<Major> list, final Iterable<Major> allMajors) {

        final Iterable<Major> copy = new ArrayList<>(list);
        for (final Major test : copy) {
            if (test.concentrationName != null) {
                final String name = test.majorName;

                for (final Major maj : allMajors) {
                    if (maj.concentrationName == null && name.equals(maj.majorName)) {
                        if (!list.contains(maj)) {
                            list.add(maj);
                        }
                        break;
                    }
                }
            }
        }

    }

    /**
     * Gets the list of all required prerequisites.
     *
     * @return a map from course code to its list of required prerequisites.
     */
    public Map<String, List<RequiredPrereq>> getRequiredPrereqs() {

        synchronized (this.synch) {
            if (this.requiredPrereqs == null) {
                // Cache the required prerequisites once - data is assumed to be constant
                // final MajorsCache cache = MajorsCache.get(this.context.getPrimaryDbContext());
                //
                // final List<RequiredPrereq> allPrereqs = cache.getAllRequiredPrereqs();
                //
                // this.requiredPrereqs = new TreeMap<>();
                //
                // for (RequiredPrereq req : allPrereqs) {
                // List<RequiredPrereq> list = this.requiredPrereqs.get(req.getCourse());
                // if (list == null) {
                // list = new ArrayList<>(2);
                // this.requiredPrereqs.put(req.getCourse(), list);
                // }
                // list.add(req);
                // }

                this.requiredPrereqs = new TreeMap<>();

                this.requiredPrereqs.put(RawRecordConstants.M118,
                        List.of(new RequiredPrereq(RawRecordConstants.M118, Boolean.TRUE, RawRecordConstants.M117)));

                this.requiredPrereqs.put(RawRecordConstants.M124,
                        List.of(new RequiredPrereq(RawRecordConstants.M124, Boolean.TRUE, RawRecordConstants.M118)));

                this.requiredPrereqs.put(RawRecordConstants.M125,
                        List.of(new RequiredPrereq(RawRecordConstants.M125, Boolean.TRUE, RawRecordConstants.M118)));

                this.requiredPrereqs.put(RawRecordConstants.M126,
                        List.of(new RequiredPrereq(RawRecordConstants.M126, Boolean.TRUE, RawRecordConstants.M125)));

                this.requiredPrereqs.put(M_141,
                        List.of(new RequiredPrereq(M_141, Boolean.FALSE, RawRecordConstants.M118)));

                this.requiredPrereqs.put(M_151, List.of(new RequiredPrereq(M_151, Boolean.FALSE, M_141, M_155, M_160)));

                this.requiredPrereqs.put(M_152, List.of(new RequiredPrereq(M_152, Boolean.FALSE, M_141, M_155, M_160)));

                this.requiredPrereqs.put(M_155,
                        Arrays.asList(new RequiredPrereq(M_155, Boolean.FALSE, RawRecordConstants.M124),
                                new RequiredPrereq(M_155, Boolean.FALSE, RawRecordConstants.M125)));

                this.requiredPrereqs.put(M_157,
                        Arrays.asList(new RequiredPrereq(M_157, Boolean.TRUE, RawRecordConstants.M124),
                                new RequiredPrereq(M_157, Boolean.FALSE, RawRecordConstants.M126)));

                this.requiredPrereqs.put(M_158,
                        Arrays.asList(new RequiredPrereq(M_158, Boolean.FALSE, M_151),
                                new RequiredPrereq(M_158, Boolean.FALSE, M_160)));

                this.requiredPrereqs.put(M_159, List.of(new RequiredPrereq(M_159, Boolean.FALSE, M_157)));

                this.requiredPrereqs.put(M_160,
                        Arrays.asList(new RequiredPrereq(M_160, Boolean.FALSE, RawRecordConstants.M124),
                                new RequiredPrereq(M_160, Boolean.FALSE, RawRecordConstants.M126)));

                this.requiredPrereqs.put(M_161,
                        Arrays.asList(new RequiredPrereq(M_161, Boolean.FALSE, RawRecordConstants.M124),
                                new RequiredPrereq(M_161, Boolean.FALSE, M_160)));

                this.requiredPrereqs.put(M_229,
                        List.of(new RequiredPrereq(M_229, Boolean.FALSE, M_141, M_155, M_160)));

                this.requiredPrereqs.put(M_230, List.of(new RequiredPrereq(M_230, Boolean.FALSE, M_161)));

                this.requiredPrereqs.put(M_235, List.of(new RequiredPrereq(M_235, Boolean.FALSE, M_161, M_271)));

                this.requiredPrereqs.put(M_255,
                        Arrays.asList(new RequiredPrereq(M_255, Boolean.TRUE, RawRecordConstants.M126),
                                new RequiredPrereq(M_255, Boolean.FALSE, M_155)));

                this.requiredPrereqs.put(M_261, List.of(new RequiredPrereq(M_261, Boolean.FALSE, M_161)));

                this.requiredPrereqs.put(M_271, List.of(new RequiredPrereq(M_271, Boolean.FALSE, M_155, M_160)));

                this.requiredPrereqs.put(M_272, List.of(new RequiredPrereq(M_272, Boolean.FALSE, M_271)));

                this.requiredPrereqs.put(M_301, List.of(new RequiredPrereq(M_301, Boolean.FALSE, M_160)));

                this.requiredPrereqs.put(M_317,
                        Arrays.asList(new RequiredPrereq(M_317, Boolean.FALSE, M_161),
                                new RequiredPrereq(M_317, Boolean.FALSE, M_230, M_235)));

                this.requiredPrereqs.put(M_331,
                        Arrays.asList(new RequiredPrereq(M_331, Boolean.TRUE, M_161),
                                new RequiredPrereq(M_331, Boolean.TRUE, M_229, M_369)));

                this.requiredPrereqs.put(M_332, List.of(new RequiredPrereq(M_332, Boolean.FALSE, M_340, M_345)));

                this.requiredPrereqs.put(M_340, List.of(new RequiredPrereq(M_340, Boolean.FALSE, M_255, M_261)));

                this.requiredPrereqs.put(M_345,
                        Arrays.asList(new RequiredPrereq(M_345, Boolean.FALSE, M_229, M_369),
                                new RequiredPrereq(M_345, Boolean.FALSE, M_255, M_261)));

                this.requiredPrereqs.put(M_348, List.of(new RequiredPrereq(M_348, Boolean.FALSE, M_155, M_160)));

                this.requiredPrereqs.put(M_360,
                        Arrays.asList(new RequiredPrereq(M_360, Boolean.FALSE, M_229, M_369),
                                new RequiredPrereq(M_360, Boolean.FALSE, M_161)));

                this.requiredPrereqs.put(M_366, List.of(new RequiredPrereq(M_366, Boolean.FALSE, M_161, M_271)));

                this.requiredPrereqs.put(M_369, List.of(new RequiredPrereq(M_369, Boolean.FALSE, M_161, M_255, M_271)));

                this.requiredPrereqs.put(M_405, List.of(new RequiredPrereq(M_405, Boolean.FALSE, M_360, M_366)));

                this.requiredPrereqs.put(M_417,
                        Arrays.asList(new RequiredPrereq(M_417, Boolean.FALSE, M_369),
                                new RequiredPrereq(M_417, Boolean.FALSE, M_317)));

                this.requiredPrereqs.put(M_418, List.of(new RequiredPrereq(M_418, Boolean.FALSE, M_417)));

                this.requiredPrereqs.put(M_419, List.of(new RequiredPrereq(M_419, Boolean.FALSE, M_261)));

                this.requiredPrereqs.put(M_425,
                        Arrays.asList(new RequiredPrereq(M_425, Boolean.FALSE, M_317),
                                new RequiredPrereq(M_425, Boolean.FALSE, M_366),
                                new RequiredPrereq(M_425, Boolean.FALSE, M_369)));

                this.requiredPrereqs.put(M_430, List.of(new RequiredPrereq(M_430, Boolean.FALSE, M_340, M_345)));

                this.requiredPrereqs.put(M_435,
                        Arrays.asList(new RequiredPrereq(M_435, Boolean.FALSE, M_229, M_369),
                                new RequiredPrereq(M_435, Boolean.FALSE, M_340, M_345)));

                this.requiredPrereqs.put(M_450, List.of(new RequiredPrereq(M_450, Boolean.FALSE, M_255, M_261)));

                this.requiredPrereqs.put(M_451, List.of(new RequiredPrereq(M_451, Boolean.FALSE, M_340, M_345)));

                this.requiredPrereqs.put(M_455,
                        List.of(new RequiredPrereq(M_455, Boolean.FALSE, M_255, M_340, M_345, M_348)));

                this.requiredPrereqs.put(M_460,
                        Arrays.asList(new RequiredPrereq(M_460, Boolean.FALSE, M_360),
                                new RequiredPrereq(M_460, Boolean.FALSE, M_369, M_366)));

                this.requiredPrereqs.put(M_466,
                        List.of(new RequiredPrereq(M_466, Boolean.FALSE, M_235, M_360, M_366)));

                this.requiredPrereqs.put(M_467,
                        Arrays.asList(new RequiredPrereq(M_467, Boolean.FALSE, M_466),
                                new RequiredPrereq(M_467, Boolean.TRUE, M_369)));

                this.requiredPrereqs.put(M_469, List.of(new RequiredPrereq(M_469, Boolean.FALSE, M_369)));

                this.requiredPrereqs.put(M_470,
                        Arrays.asList(new RequiredPrereq(M_470, Boolean.FALSE, M_229, M_369),
                                new RequiredPrereq(M_470, Boolean.FALSE, M_261)));

                this.requiredPrereqs.put(M_472, List.of(new RequiredPrereq(M_472, Boolean.FALSE, M_317)));

                this.requiredPrereqs.put(M_474,
                        Arrays.asList(new RequiredPrereq(M_474, Boolean.FALSE, M_261),
                                new RequiredPrereq(M_474, Boolean.FALSE, M_369)));
            }
        }

        return this.requiredPrereqs;
    }

    /**
     * Retrieves a cached student data record. If no record is cached for the student (or the cached record has
     * expired), the student record is queried and a new {@code StudentData} object is created and cached.
     *
     * @param cache     the data cache
     * @param studentId the student ID
     * @param session   the login session
     * @return the student data object; {@code null} if there is no cached data and the student record cannot be
     *         queried
     * @throws SQLException if there is an error accessing the database
     */
    public StudentData getStudentData(final Cache cache, final String studentId,
                                      final ImmutableSessionInfo session) throws SQLException {

        synchronized (this.synch) {
            expireCache();

            StudentData result = this.studentDataCache.get(studentId);

            if (result == null) {
                final RawStudent student = RawStudentLogic.query(cache, studentId, true);

                if (student != null) {
                    result = new StudentData(cache, student, this, session, session.actAsUserId == null);
                    this.studentDataCache.put(studentId, result);
                }
            }

            return result;
        }
    }

    /**
     * Scans the student data cache and removes any expired entries. Entries are inserted in a map that preserves
     * insertion order, and all have the same expiration duration, so they will always be in order of increasing
     * expiration timestamp. So we can scan the cache removing expired items until we find one that is not expired, and
     * stop scanning.
     */
    private void expireCache() {

        // Called only from within synchronized block

        final Iterator<StudentData> iter = this.studentDataCache.values().iterator();
        while (iter.hasNext() && iter.next().isExpired()) {
            iter.remove();
        }
    }

    /**
     * Retrieves all completed courses on the student's record.
     *
     * @param studentId the student ID
     * @return the list of transfer credit entries; empty if none
     */
    public List<LiveCsuCredit> getCompletedCourses(final String studentId) {

        List<LiveCsuCredit> result;

        if (AbstractLogicModule.isBannerDown()) {
            result = new ArrayList<>(0);
        } else {
            try {
                final DbContext banner = this.dbProfile.getDbContext(ESchemaUse.LIVE);
                final DbConnection conn = banner.checkOutConnection();
                try {
                    final ILiveCsuCredit impl = conn.getImplementation(ILiveCsuCredit.class);
                    if (impl == null) {
                        result = new ArrayList<>(0);
                    } else {
                        result = impl.query(conn, studentId);
                    }
                } finally {
                    banner.checkInConnection(conn);
                }
            } catch (final Exception ex) {
                AbstractLogicModule.indicateBannerDown();
                Log.warning(ex);
                result = new ArrayList<>(0);
            }
        }

        return result;
    }

    /**
     * Retrieves all transfer credit entries on the student's record.
     *
     * @param cache               the data cache
     * @param studentId           the student ID
     * @param reconcileLWithLocal if true, records that are found but don't exist in "ffr_trns" are inserted into
     *                            ffr_trns
     * @return the list of transfer credit entries; empty if none
     * @throws SQLException if there is an error accessing the database
     */
    public List<LiveTransferCredit> getStudentTransferCredit(final Cache cache, final String studentId,
                                                             final boolean reconcileLWithLocal) throws SQLException {

        List<LiveTransferCredit> result;

        if (studentId.startsWith("99")) {

            // The following will return test data - convert to "live" data.
            final List<RawFfrTrns> list = RawFfrTrnsLogic.queryByStudent(cache, studentId);
            result = new ArrayList<>(list.size());

            for (final RawFfrTrns tc : list) {
                final LiveTransferCredit ltc = new LiveTransferCredit(studentId, null, tc.course, null, null);
                result.add(ltc);
            }
        } else if (AbstractLogicModule.isBannerDown()) {
            result = new ArrayList<>(0);
        } else {
            try {
                final DbContext banner = this.dbProfile.getDbContext(ESchemaUse.LIVE);
                final DbConnection bannerConn = banner.checkOutConnection();

                final ILiveTransferCredit impl = bannerConn.getImplementation(ILiveTransferCredit.class);

                if (impl == null) {
                    Log.warning("No ILiveTransferCredit implementation found");
                    result = new ArrayList<>(0);
                } else {
                    result = impl.query(bannerConn, studentId);
                }
                banner.checkInConnection(bannerConn);
            } catch (final Exception ex) {
                AbstractLogicModule.indicateBannerDown();
                Log.warning(ex);
                result = new ArrayList<>(0);
            }

            final Iterator<LiveTransferCredit> iter = result.iterator();
            while (iter.hasNext()) {
                final LiveTransferCredit row = iter.next();
                final String courseId = row.courseId;

                if (!courseId.startsWith("MATH1++")) {
                    if (courseId.startsWith("MATH") || courseId.startsWith("M ") || "STAT 100".equals(courseId)
                            || "STAT100".equals(courseId)) {
                        continue;
                    }
                }
                iter.remove();
            }

            if (reconcileLWithLocal) {
                final List<RawFfrTrns> existing = RawFfrTrnsLogic.queryByStudent(cache, studentId);

                for (final LiveTransferCredit live : result) {

                    boolean searching = true;
                    for (final RawFfrTrns exist : existing) {
                        if (exist.course.equals(live.courseId)) {
                            searching = false;
                            break;
                        }
                    }

                    if (searching) {
                        Log.info("Adding ", live.courseId, " transfer credit for student ", studentId);

                        final RawFfrTrns toAdd = new RawFfrTrns(live.studentId, live.courseId, "T", LocalDate.now(),
                                null);

                        RawFfrTrnsLogic.INSTANCE.insert(cache, toAdd);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Gets the latest responses to the profile for the student.
     *
     * @param cache     the data cache
     * @param studentId the student ID
     * @param pageId    the page ID
     * @return a map from question number to student response
     * @throws SQLException if there is an error accessing the database
     */
    public static Map<Integer, RawStmathplan> getMathPlanResponses(final Cache cache, final String studentId,
                                                                   final String pageId) throws SQLException {

        final List<RawStmathplan> latest = RawStmathplanLogic.queryLatestByStudentPage(cache, studentId, pageId);

        final Map<Integer, RawStmathplan> map = new HashMap<>(latest.size());
        for (final RawStmathplan response : latest) {
            map.put(response.surveyNbr, response);
        }

        return map;
    }

    /**
     * Tests whether the student has taken a placement exam.
     *
     * @param cache     the data cache
     * @param studentId the student ID
     * @return {@code true} if the student has taken a placement exam
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean hasTakenPlacement(final Cache cache, final String studentId) throws SQLException {

        return !RawStmpeLogic.queryLegalByStudent(cache, studentId).isEmpty();
    }

    /**
     * Tests whether the student identified by a PIDM has completed their math plan.
     *
     * @param cache the data cache
     * @param pidm  the PIDM
     * @return the student ID if the student has completed their math plan, {@code null} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static String getMathPlanStatus(final Cache cache, final int pidm) throws SQLException {

        final List<RawStmathplan> responses = RawStmathplanLogic.queryLatestByStudentPage(cache,
                Integer.valueOf(pidm), INTENTIONS_PROFILE);

        String studentId = null;

        if (!responses.isEmpty()) {
            studentId = responses.get(0).stuId;
        }

        return studentId;
    }

    /**
     * Tests whether the student identified by a PIDM has completed the Math Placement process.
     *
     * @param cache the data cache
     * @param pidm  the PIDM
     * @return 0 if the student's Math Plan indicates they do not need to complete placement; 1 if the student should
     *         complete math placement but has not yet done so; 2 is math placement has been completed
     * @throws SQLException if there is an error accessing the database
     */
    public int getMathPlacementStatus(final Cache cache, final int pidm) throws SQLException {

        int result;

        final String studentId = getMathPlanStatus(cache, pidm);

        if (studentId == null) {
            // Question is not yet relevant for this student until Math Plan has been completed
            result = 0;
        } else {
            final Map<Integer, RawStmathplan> responses = getMathPlanResponses(cache, studentId, INTENTIONS_PROFILE);

            final RawStmathplan record = responses.get(Integer.valueOf(2));
            final String response = record == null ? null : record.stuAnswer;

            final boolean shouldDoPlacement = "Y".equals(response);

            if (shouldDoPlacement) {
                // Clear flag if placement has been attempted
                final List<RawStmpe> attempts = RawStmpeLogic.queryLegalByStudent(cache, studentId);

                result = attempts.isEmpty() ? 1 : 2;

                if (result == 1) {
                    // Also check for new transfer credit in any Precalculus course
                    getStudentTransferCredit(cache, studentId, true);

                    final List<RawFfrTrns> xfers = RawFfrTrnsLogic.queryByStudent(cache, studentId);
                    for (final RawFfrTrns xfer : xfers) {
                        if (RawRecordConstants.M117.equals(xfer.course)
                                || RawRecordConstants.M118.equals(xfer.course)
                                || RawRecordConstants.M124.equals(xfer.course)
                                || RawRecordConstants.M125.equals(xfer.course)
                                || RawRecordConstants.M126.equals(xfer.course)
                                || RawRecordConstants.M002.equals(xfer.course)) {
                            // M 002 is a community college course that clears prereqs for 117
                            result = 2;
                            break;
                        }
                    }
                }
            } else {
                result = 0;
            }
        }

        return result;
    }

    /**
     * Deletes all responses for a student for a specific page.
     *
     * @param cache   the data cache
     * @param student the student
     * @param pageId  the page ID
     * @param session the login session
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public boolean deleteMathPlanResponses(final Cache cache, final RawStudent student, final String pageId,
                                           final ImmutableSessionInfo session) throws SQLException {

        final String studentId = student.stuId;

        final boolean result = RawStmathplanLogic.deleteAllForPage(cache, studentId, pageId);

        if (result) {
            synchronized (this.synch) {
                // Rebuild student data
                this.studentDataCache.put(student.stuId, new StudentData(cache, student, this, session, false));
            }
        }

        return result;
    }

    /**
     * Stores a set of profile answers and updates the cached student plan based on the new profile responses.
     *
     * @param cache     the data cache
     * @param student   the student
     * @param pageId    the page ID
     * @param questions the question numbers
     * @param answers   the answers
     * @param session   the login session
     * @throws SQLException if there is an error accessing the database
     */
    public void storeMathPlanResponses(final Cache cache, final RawStudent student, final String pageId,
                                       final List<Integer> questions, final List<String> answers,
                                       final ImmutableSessionInfo session) throws SQLException {

        final LocalDateTime when = session.getNow().toLocalDateTime();
        final Integer finishTime = Integer.valueOf(TemporalUtils.minuteOfDay(when));

        final String aplnTermStr = student.aplnTerm == null ? null : student.aplnTerm.shortString;

        // Dummy record to test for existing
        RawStmathplan resp = new RawStmathplan(student.stuId, student.pidm, aplnTermStr, pageId, when.toLocalDate(),
                Integer.valueOf(0), CoreConstants.EMPTY, finishTime, Long.valueOf(session.loginSessionTag));

        // Query for any existing answers with the same date and finish time
        final List<RawStmathplan> latest =
                RawStmathplanLogic.queryLatestByStudentPage(cache, student.stuId, pageId);
        final LocalDate today = session.getNow().toLocalDate();
        final Integer minutes = resp.finishTime;
        final Iterator<RawStmathplan> iter = latest.iterator();
        while (iter.hasNext()) {
            final RawStmathplan test = iter.next();
            if (today.equals(test.examDt) && minutes.equals(test.finishTime)) {
                continue;
            }
            iter.remove();
        }

        final int count = Math.min(questions.size(), answers.size());

        for (int i = 0; i < count; ++i) {
            final String ans = answers.get(i);
            final Integer questionNum = questions.get(i);

            resp = new RawStmathplan(student.stuId, student.pidm, aplnTermStr, pageId, when.toLocalDate(), questionNum,
                    ans, finishTime, Long.valueOf(session.loginSessionTag));

            // See if there is an existing answer at the same time
            RawStmathplan existing = null;
            for (final RawStmathplan test : latest) {
                if (test.surveyNbr.equals(questionNum)) {
                    existing = test;
                    break;
                }
            }

            if (ans == null) {
                // Old record had answer, new does not, so delete old record
                if (existing != null) {
                    RawStmathplanLogic.INSTANCE.delete(cache, existing);
                }
            } else {
                RawStmathplanLogic.INSTANCE.insert(cache, resp);
            }
        }

        synchronized (this.synch) {
            // Responses have changed - rebuild student data
            this.studentDataCache.put(student.stuId, new StudentData(cache, student, this, session, false));
        }
    }

    /**
     * Main method to test placement status.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        final ContextMap map = ContextMap.getDefaultInstance();
        final DbProfile dbProfile = map.getCodeProfile(Contexts.BATCH_PATH);
        final DbContext ctx = dbProfile.getDbContext(ESchemaUse.PRIMARY);

        try {
            final DbConnection conn = ctx.checkOutConnection();
            try {
                final Cache cache = new Cache(dbProfile, conn);

                // Student 823251213 PIDM 10567708
                final MathPlanLogic logic = new MathPlanLogic(dbProfile);

                final String status1 = getMathPlanStatus(cache, 10567708);
                Log.info("Student 823251213 plan status: " + status1);

                final int status2 = logic.getMathPlacementStatus(cache, 11862174);
                Log.info("Student 823251213 placement status: " + status2);
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
        }
    }
}
