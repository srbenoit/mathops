package dev.mathops.db.old.logic.mathplan;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Contexts;
import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.ifaces.ILiveCsuCredit;
import dev.mathops.db.old.ifaces.ILiveTransferCredit;
import dev.mathops.db.old.logic.mathplan.data.MathPlanConstants;
import dev.mathops.db.old.rawlogic.AbstractLogicModule;
import dev.mathops.db.old.rawlogic.RawFfrTrnsLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStmathplanLogic;
import dev.mathops.db.old.rawlogic.RawStmpeLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawCourse;
import dev.mathops.db.old.rawrecord.RawFfrTrns;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStmathplan;
import dev.mathops.db.old.rawrecord.RawStmpe;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.rec.LiveCsuCredit;
import dev.mathops.db.old.rec.LiveTransferCredit;
import dev.mathops.db.old.logic.mathplan.data.CourseGroup;
import dev.mathops.db.old.logic.mathplan.data.Major;
import dev.mathops.db.old.logic.mathplan.data.MajorMathRequirement;
import dev.mathops.db.old.logic.mathplan.data.RequiredPrereq;
import dev.mathops.db.old.logic.mathplan.data.MathPlanStudentData;
import dev.mathops.db.old.schema.csubanner.ImplLiveCsuCredit;
import dev.mathops.db.old.schema.csubanner.ImplLiveTransferCredit;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
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
 * Logic module for the Math Plan website. This class is thread-safe and may be queried by multiple servlet threads.
 *
 * <p>
 * LAST REVIEW AGAINST CIM/Catalog: April 29, 2024
 */
public final class MathPlanLogic {

    /** Object on which to synchronize member variable access. */
    private final Object synch;

    /** The database profile this module will use. */
    private final DbProfile dbProfile;

    /** The cached courses. */
    private Map<String, RawCourse> courses = null;

    /** The cached course groups. */
    private Map<String, CourseGroup> courseGroups = null;

    /** The cached list of majors and their requirements (sorted by major). */
    private Map<Major, MajorMathRequirement> majors = null;

    /** The subset of cached majors that require only 3 credits of AUCC. */
    private List<Major> majorsNeedingAUCC = null;

    /** The subset of cached majors that require nothing beyond precalculus. */
    private List<Major> majorsNeedingPrecalc = null;

    /** The subset of cached majors that require courses through a Calculus I. */
    private List<Major> majorsNeedingCalc1 = null;

    /** The subset of cached majors that require courses beyond Calculus II. */
    private List<Major> majorsNeedingMore = null;

    /** The cached list of required prerequisites (map from course to its prerequisites). */
    private Map<String, List<RequiredPrereq>> requiredPrereqs = null;

    /** A cache of student data. */
    private final LinkedHashMap<String, MathPlanStudentData> studentDataCache;

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
                this.courses = new HashMap<>(100);

                // General AUCC-1B courses

                this.courses.put(MathPlanConstants.M_101, new RawCourse(MathPlanConstants.M_101, MathPlanConstants.ZERO,
                        "Math in the Social Sciences (3 credits)",
                        MathPlanConstants.THREE, "N", "MATH 101", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_105, new RawCourse(MathPlanConstants.M_105, MathPlanConstants.ZERO,
                        "Patterns of Phenomena (3 credits)",
                        MathPlanConstants.THREE, "N", "MATH 105", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.S_100, new RawCourse(MathPlanConstants.S_100, MathPlanConstants.ZERO,
                        "Statistical Literacy (3 credits)",
                        MathPlanConstants.THREE, "N", "STAT 100", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.S_201, new RawCourse(MathPlanConstants.S_201, MathPlanConstants.ZERO,
                        "General Statistics (3 credits)",
                        MathPlanConstants.THREE, "N", "STAT 201", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.S_204, new RawCourse(MathPlanConstants.S_204, MathPlanConstants.ZERO,
                        "Statistics With Business Applications (3 credits)",
                        MathPlanConstants.THREE, "N", "STAT 204", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.F_200, new RawCourse(MathPlanConstants.F_200, MathPlanConstants.ZERO,
                        "Personal Finance and Investing (3 credits)",
                        MathPlanConstants.THREE, "N", "FIN 200", null,
                        "N", "N"));

                // Precalculus

                this.courses.put(RawRecordConstants.M116,
                        new RawCourse(RawRecordConstants.M116, MathPlanConstants.ZERO,
                                "Precalculus Supplement for Success in Math (1 credit)",
                                MathPlanConstants.ONE, "N", "MATH 116", null,
                                "N", "Y"));
                this.courses.put(RawRecordConstants.M117,
                        new RawCourse(RawRecordConstants.M117, MathPlanConstants.FOUR,
                                "College Algebra in Context I (1 credit)",
                                MathPlanConstants.ONE, "Y", "MATH 117", null,
                                "N", "Y"));
                this.courses.put(RawRecordConstants.M118,
                        new RawCourse(RawRecordConstants.M118, MathPlanConstants.FOUR,
                                "College Algebra in Context II (1 credit)",
                                MathPlanConstants.ONE, "Y", "MATH 118", null,
                                "N", "Y"));
                this.courses.put(RawRecordConstants.M120,
                        new RawCourse(RawRecordConstants.M120, MathPlanConstants.ZERO,
                                "College Algebra (3 credit)",
                                MathPlanConstants.THREE, "Y", "MATH 120", null,
                                "N", "N"));
                this.courses.put(RawRecordConstants.M124,
                        new RawCourse(RawRecordConstants.M124, MathPlanConstants.FOUR,
                                "Logarithmic and Exponential Functions (1 credit)",
                                MathPlanConstants.ONE, "Y", "MATH 124", null,
                                "N", "Y"));
                this.courses.put(RawRecordConstants.M125,
                        new RawCourse(RawRecordConstants.M125, MathPlanConstants.FOUR,
                                "Numerical Trigonometry (1 credit)",
                                MathPlanConstants.ONE, "Y", "MATH 125", null,
                                "N", "Y"));
                this.courses.put(RawRecordConstants.M126,
                        new RawCourse(RawRecordConstants.M126, MathPlanConstants.FOUR,
                                "Analytic Trigonometry (1 credit)",
                                MathPlanConstants.ONE, "Y", "MATH 126", null,
                                "N", "Y"));
                this.courses.put(RawRecordConstants.M127,
                        new RawCourse(RawRecordConstants.M127, MathPlanConstants.ZERO,
                                "Precalculus (4 credit)",
                                MathPlanConstants.FOUR, "Y", "MATH 127", null,
                                "N", "N"));

                // Other Math courses

                this.courses.put(MathPlanConstants.M_141, new RawCourse(MathPlanConstants.M_141, MathPlanConstants.ZERO,
                        "Calculus in Management Sciences (3 credits)",
                        MathPlanConstants.THREE, "N", "MATH 141", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_151, new RawCourse(MathPlanConstants.M_151, MathPlanConstants.ZERO,
                        "Mathematical Algorithms in Matlab I (1 credit)",
                        MathPlanConstants.ONE, "N", "MATH 151", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_152, new RawCourse(MathPlanConstants.M_152, MathPlanConstants.ZERO,
                        "Mathematical Algorithms in Maple (1 credit)",
                        MathPlanConstants.ONE, "N", "MATH 152", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_155, new RawCourse(MathPlanConstants.M_155, MathPlanConstants.ZERO,
                        "Calculus for Biological Scientists I (4 credits)",
                        MathPlanConstants.FOUR, "N", "MATH 155", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_156, new RawCourse(MathPlanConstants.M_156, MathPlanConstants.ZERO,
                        "Mathematics for Computational Science I (4 credits)",
                        MathPlanConstants.FOUR, "N", "MATH 156", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_157, new RawCourse(MathPlanConstants.M_157, MathPlanConstants.ZERO,
                        "One Year Calculus IA (3 credits)",
                        MathPlanConstants.THREE, "N", "MATH 157", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_158, new RawCourse(MathPlanConstants.M_158, MathPlanConstants.ZERO,
                        "Mathematical Algorithms in C (1 credit)",
                        MathPlanConstants.ONE, "N", "MATH 158", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_159, new RawCourse(MathPlanConstants.M_159, MathPlanConstants.ZERO,
                        "One Year Calculus IB (3 credits)",
                        MathPlanConstants.THREE, "N", "MATH 159", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_160, new RawCourse(MathPlanConstants.M_160, MathPlanConstants.ZERO,
                        "Calculus for Physical Scientists I (4 credits)",
                        MathPlanConstants.FOUR, "N", "MATH 160", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_161, new RawCourse(MathPlanConstants.M_161, MathPlanConstants.ZERO,
                        "Calculus for Physical Scientists II (4 credits)",
                        MathPlanConstants.FOUR, "N", "MATH 161", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_192, new RawCourse(MathPlanConstants.M_192, MathPlanConstants.ZERO,
                        "First Year Seminar in Mathematical Sciences (1 credit)",
                        MathPlanConstants.ONE, "N", "MATH 192", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_229, new RawCourse(MathPlanConstants.M_229, MathPlanConstants.ZERO,
                        "Matrices and Linear Equations (2 credits)",
                        MathPlanConstants.TWO, "N", "MATH 229", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_230, new RawCourse(MathPlanConstants.M_230, MathPlanConstants.ZERO,
                        "Discrete Mathematics for Educators (3 credits)",
                        MathPlanConstants.THREE, "N", "MATH 230", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_235, new RawCourse(MathPlanConstants.M_235, MathPlanConstants.ZERO,
                        "Introduction to Mathematical Reasoning (2 credits)",
                        MathPlanConstants.TWO, "N", "MATH 235", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_255, new RawCourse(MathPlanConstants.M_255, MathPlanConstants.ZERO,
                        "Calculus for Biological Scientists II (4 credits)",
                        MathPlanConstants.FOUR, "N", "MATH 255", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_256, new RawCourse(MathPlanConstants.M_256, MathPlanConstants.ZERO,
                        "Mathematics for Computational Science II (4 credits)",
                        MathPlanConstants.FOUR, "N", "MATH 256", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_261, new RawCourse(MathPlanConstants.M_261, MathPlanConstants.ZERO,
                        "Calculus for Physical Scientists III (4 credits)",
                        MathPlanConstants.FOUR, "N", "MATH 261", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_269, new RawCourse(MathPlanConstants.M_269, MathPlanConstants.ZERO,
                        "Geometric Introduction to Linear Algebra (2 credits)",
                        MathPlanConstants.TWO, "N", "MATH 269", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_271, new RawCourse(MathPlanConstants.M_271, MathPlanConstants.ZERO,
                        "Applied Mathematics for Chemists I (4 credits)",
                        MathPlanConstants.FOUR, "N", "MATH 271", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_272, new RawCourse(MathPlanConstants.M_272, MathPlanConstants.ZERO,
                        "Applied Mathematics for Chemists II (4 credits)",
                        MathPlanConstants.FOUR, "N", "MATH 272", null,
                        "N", "N"));

                this.courses.put(MathPlanConstants.M_301, new RawCourse(MathPlanConstants.M_301, MathPlanConstants.ZERO,
                        "Introduction to Combinatorial Theory (3 credits)",
                        MathPlanConstants.THREE, "N", "MATH 301", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_317, new RawCourse(MathPlanConstants.M_317, MathPlanConstants.ZERO,
                        "Advanced Calculus of One Variable (3 credits)",
                        MathPlanConstants.THREE, "N", "MATH 317", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_331, new RawCourse(MathPlanConstants.M_331, MathPlanConstants.ZERO,
                        "Introduction to Mathematical Modeling (3 credits)",
                        MathPlanConstants.THREE, "N", "MATH 331", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_332, new RawCourse(MathPlanConstants.M_332, MathPlanConstants.ZERO,
                        "Partial Differential Equations (3 credits)",
                        MathPlanConstants.THREE, "N", "MATH 332", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_340, new RawCourse(MathPlanConstants.M_340, MathPlanConstants.ZERO,
                        "Intro to Ordinary Differential Equations (4 credits)",
                        MathPlanConstants.FOUR, "N", "MATH 340", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_345, new RawCourse(MathPlanConstants.M_345, MathPlanConstants.ZERO,
                        "Differential Equations (4 credits)",
                        MathPlanConstants.FOUR, "N", "MATH 345", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_348, new RawCourse(MathPlanConstants.M_348, MathPlanConstants.ZERO,
                        "Theory of Population and Evolutionary Ecology (4 credits)",
                        MathPlanConstants.FOUR, "N", "MATH 348", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_360, new RawCourse(MathPlanConstants.M_360, MathPlanConstants.ZERO,
                        "Mathematics of Information Security (3 credits)",
                        MathPlanConstants.THREE, "N", "MATH 360", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_366, new RawCourse(MathPlanConstants.M_366, MathPlanConstants.ZERO,
                        "Introduction to Abstract Algebra (3 credits)",
                        MathPlanConstants.THREE, "N", "MATH 366", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_369, new RawCourse(MathPlanConstants.M_369, MathPlanConstants.ZERO,
                        "Linear Algebra I (3 credits)",
                        MathPlanConstants.THREE, "N", "MATH 369", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.D_369, new RawCourse(MathPlanConstants.D_369, MathPlanConstants.ZERO,
                        "Linear Algebra for Data Science (4 credits)",
                        MathPlanConstants.FOUR, "N", "DSCI 369", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_384, new RawCourse(MathPlanConstants.M_384, MathPlanConstants.ZERO,
                        "Supervised College Teaching (1 credit)",
                        MathPlanConstants.ONE, "N", "MATH 384", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_405, new RawCourse(MathPlanConstants.M_405, MathPlanConstants.ZERO,
                        "Introduction to Number Theory (3 credits)",
                        MathPlanConstants.THREE, "N", "MATH 405", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_417, new RawCourse(MathPlanConstants.M_417, MathPlanConstants.ZERO,
                        "Advanced Calculus I (3 credits)",
                        MathPlanConstants.THREE, "N", "MATH 417", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_418, new RawCourse(MathPlanConstants.M_418, MathPlanConstants.ZERO,
                        "Advanced Calculus II (3 credits)",
                        MathPlanConstants.THREE, "N", "MATH 418", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_419, new RawCourse(MathPlanConstants.M_419, MathPlanConstants.ZERO,
                        "Introduction to Complex Variables (3 credits)",
                        MathPlanConstants.THREE, "N", "MATH 419", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_425, new RawCourse(MathPlanConstants.M_425, MathPlanConstants.ZERO,
                        "History of Mathematics (3 credits)",
                        MathPlanConstants.THREE, "N", "MATH 425", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_430, new RawCourse(MathPlanConstants.M_430, MathPlanConstants.ZERO,
                        "Fourier and Wavelet Analysis with Apps (3 credits)",
                        MathPlanConstants.THREE, "N", "MATH 430", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_435, new RawCourse(MathPlanConstants.M_435, MathPlanConstants.ZERO,
                        "Projects in Applied Mathematics (3 credits)",
                        MathPlanConstants.THREE, "N", "MATH 435", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_450, new RawCourse(MathPlanConstants.M_450, MathPlanConstants.ZERO,
                        "Introduction to Numerical Analysis I (3 credits)",
                        MathPlanConstants.THREE, "N", "MATH 450", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_451, new RawCourse(MathPlanConstants.M_451, MathPlanConstants.ZERO,
                        "Introduction to Numerical Analysis II (3 credits)",
                        MathPlanConstants.THREE, "N", "MATH 451", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_455, new RawCourse(MathPlanConstants.M_455, MathPlanConstants.ZERO,
                        "Mathematics in Biology and Medicine (3 credits)",
                        MathPlanConstants.THREE, "N", "MATH 455", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_460, new RawCourse(MathPlanConstants.M_460, MathPlanConstants.ZERO,
                        "Information and Coding Theory (3 credits)",
                        MathPlanConstants.THREE, "N", "MATH 460", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_463, new RawCourse(MathPlanConstants.M_463, MathPlanConstants.ZERO,
                        "Post-Quantum Cryptography (3 credits)",
                        MathPlanConstants.THREE, "N", "MATH 463", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_466, new RawCourse(MathPlanConstants.M_466, MathPlanConstants.ZERO,
                        "Abstract Algebra I (3 credits)",
                        MathPlanConstants.THREE, "N", "MATH 466", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_467, new RawCourse(MathPlanConstants.M_467, MathPlanConstants.ZERO,
                        "Abstract Algebra II (3 credits)",
                        MathPlanConstants.THREE, "N", "MATH 467", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_469, new RawCourse(MathPlanConstants.M_469, MathPlanConstants.ZERO,
                        "Linear Algebra II (3 credits)",
                        MathPlanConstants.THREE, "N", "MATH 469", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_470, new RawCourse(MathPlanConstants.M_470, MathPlanConstants.ZERO,
                        "Euclidean and Non-Euclidean Geometry (3 credits)",
                        MathPlanConstants.THREE, "N", "MATH 470", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_472, new RawCourse(MathPlanConstants.M_472, MathPlanConstants.ZERO,
                        "Introduction to Topology (3 credits)",
                        MathPlanConstants.THREE, "N", "MATH 472", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_474, new RawCourse(MathPlanConstants.M_474, MathPlanConstants.ZERO,
                        "Introduction to Differential Geometry (3 credits)",
                        MathPlanConstants.THREE, "N", "MATH 474", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_476, new RawCourse(MathPlanConstants.M_476, MathPlanConstants.ZERO,
                        "Topics in Mathematics (3 credits)",
                        MathPlanConstants.THREE, "N", "MATH 476", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_484, new RawCourse(MathPlanConstants.M_484, MathPlanConstants.ZERO,
                        "Supervised College Teaching (1-3 credits)",
                        MathPlanConstants.NEG_ONE, "N", "MATH 484", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_487, new RawCourse(MathPlanConstants.M_487, MathPlanConstants.ZERO,
                        "Internship (1-16 credits)",
                        MathPlanConstants.NEG_ONE, "N", "MATH 487", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_495, new RawCourse(MathPlanConstants.M_495, MathPlanConstants.ZERO,
                        "Independent Study (1-18 credits)",
                        MathPlanConstants.NEG_ONE, "N", "MATH 495", null,
                        "N", "N"));
                this.courses.put(MathPlanConstants.M_498, new RawCourse(MathPlanConstants.M_498, MathPlanConstants.ZERO,
                        "Undergraduate Research in Mathematics (1-3 credits)",
                        MathPlanConstants.NEG_ONE, "N", "MATH 498", null,
                        "N", "N"));
            }

            return Collections.unmodifiableMap(this.courses);
        }
    }

    /**
     * Retrieves the complete list of course options.
     *
     * @return a map from group ID to the course group
     */
    public Map<String, CourseGroup> getCourseGroups() {

        synchronized (this.synch) {
            if (this.courseGroups == null) {

                this.courseGroups = new HashMap<>(100);

                this.courseGroups.put(MathPlanConstants.M_101, new CourseGroup(MathPlanConstants.M_101, null,
                        MathPlanConstants.M_101, MathPlanConstants.M_101));
                this.courseGroups.put(MathPlanConstants.M_105, new CourseGroup(MathPlanConstants.M_105, null,
                        MathPlanConstants.M_105, MathPlanConstants.M_105));
                this.courseGroups.put(MathPlanConstants.S_100, new CourseGroup(MathPlanConstants.S_100, null,
                        MathPlanConstants.S_100, MathPlanConstants.S_100));
                this.courseGroups.put(MathPlanConstants.S_201, new CourseGroup(MathPlanConstants.S_201, null,
                        MathPlanConstants.S_201, MathPlanConstants.S_201));
                this.courseGroups.put(MathPlanConstants.S_204, new CourseGroup(MathPlanConstants.S_204, null,
                        MathPlanConstants.S_204, MathPlanConstants.S_204));
                this.courseGroups.put(MathPlanConstants.F_200, new CourseGroup(MathPlanConstants.F_200, null,
                        MathPlanConstants.F_200, MathPlanConstants.F_200));

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

                this.courseGroups.put(MathPlanConstants.M_141, new CourseGroup(MathPlanConstants.M_141, null,
                        MathPlanConstants.M_141, MathPlanConstants.M_141));
                this.courseGroups.put(MathPlanConstants.M_151, new CourseGroup(MathPlanConstants.M_151, null,
                        MathPlanConstants.M_151, MathPlanConstants.M_151));
                this.courseGroups.put(MathPlanConstants.M_152, new CourseGroup(MathPlanConstants.M_152, null,
                        MathPlanConstants.M_152, MathPlanConstants.M_152));
                this.courseGroups.put(MathPlanConstants.M_155, new CourseGroup(MathPlanConstants.M_155, null,
                        MathPlanConstants.M_155, MathPlanConstants.M_155));
                this.courseGroups.put(MathPlanConstants.M_156, new CourseGroup(MathPlanConstants.M_156, null,
                        MathPlanConstants.M_156, MathPlanConstants.M_156));
                this.courseGroups.put(MathPlanConstants.M_157, new CourseGroup(MathPlanConstants.M_157, null,
                        MathPlanConstants.M_157, MathPlanConstants.M_157));
                this.courseGroups.put(MathPlanConstants.M_158, new CourseGroup(MathPlanConstants.M_158, null,
                        MathPlanConstants.M_158, MathPlanConstants.M_158));
                this.courseGroups.put(MathPlanConstants.M_159, new CourseGroup(MathPlanConstants.M_159, null,
                        MathPlanConstants.M_159, MathPlanConstants.M_159));
                this.courseGroups.put(MathPlanConstants.M_160, new CourseGroup(MathPlanConstants.M_160, null,
                        MathPlanConstants.M_160, MathPlanConstants.M_160));
                this.courseGroups.put(MathPlanConstants.M_161, new CourseGroup(MathPlanConstants.M_161, null,
                        MathPlanConstants.M_161, MathPlanConstants.M_161));
                this.courseGroups.put(MathPlanConstants.M_192, new CourseGroup(MathPlanConstants.M_192, null,
                        MathPlanConstants.M_192, MathPlanConstants.M_192));

                this.courseGroups.put(MathPlanConstants.M_229, new CourseGroup(MathPlanConstants.M_229, null,
                        MathPlanConstants.M_229, MathPlanConstants.M_229));
                this.courseGroups.put(MathPlanConstants.M_230, new CourseGroup(MathPlanConstants.M_230, null,
                        MathPlanConstants.M_230, MathPlanConstants.M_230));
                this.courseGroups.put(MathPlanConstants.M_235, new CourseGroup(MathPlanConstants.M_235, null,
                        MathPlanConstants.M_235, MathPlanConstants.M_235));
                this.courseGroups.put(MathPlanConstants.M_255, new CourseGroup(MathPlanConstants.M_255, null,
                        MathPlanConstants.M_255, MathPlanConstants.M_255));
                this.courseGroups.put(MathPlanConstants.M_256, new CourseGroup(MathPlanConstants.M_256, null,
                        MathPlanConstants.M_256, MathPlanConstants.M_256));
                this.courseGroups.put(MathPlanConstants.M_261, new CourseGroup(MathPlanConstants.M_261, null,
                        MathPlanConstants.M_261, MathPlanConstants.M_261));
                this.courseGroups.put(MathPlanConstants.M_269, new CourseGroup(MathPlanConstants.M_269, null,
                        MathPlanConstants.M_269, MathPlanConstants.M_269));
                this.courseGroups.put(MathPlanConstants.M_271, new CourseGroup(MathPlanConstants.M_271, null,
                        MathPlanConstants.M_271, MathPlanConstants.M_271));
                this.courseGroups.put(MathPlanConstants.M_272, new CourseGroup(MathPlanConstants.M_272, null,
                        MathPlanConstants.M_272, MathPlanConstants.M_272));

                this.courseGroups.put(MathPlanConstants.M_301, new CourseGroup(MathPlanConstants.M_301, null,
                        MathPlanConstants.M_301, MathPlanConstants.M_301));
                this.courseGroups.put(MathPlanConstants.M_317, new CourseGroup(MathPlanConstants.M_317, null,
                        MathPlanConstants.M_317, MathPlanConstants.M_317));
                this.courseGroups.put(MathPlanConstants.M_331, new CourseGroup(MathPlanConstants.M_331, null,
                        MathPlanConstants.M_331, MathPlanConstants.M_331));
                this.courseGroups.put(MathPlanConstants.M_332, new CourseGroup(MathPlanConstants.M_332, null,
                        MathPlanConstants.M_332, MathPlanConstants.M_332));
                this.courseGroups.put(MathPlanConstants.M_340, new CourseGroup(MathPlanConstants.M_340, null,
                        MathPlanConstants.M_340, MathPlanConstants.M_340));
                this.courseGroups.put(MathPlanConstants.M_345, new CourseGroup(MathPlanConstants.M_345, null,
                        MathPlanConstants.M_345, MathPlanConstants.M_345));
                this.courseGroups.put(MathPlanConstants.M_348, new CourseGroup(MathPlanConstants.M_348, null,
                        MathPlanConstants.M_348, MathPlanConstants.M_348));
                this.courseGroups.put(MathPlanConstants.M_360, new CourseGroup(MathPlanConstants.M_360, null,
                        MathPlanConstants.M_360, MathPlanConstants.M_360));
                this.courseGroups.put(MathPlanConstants.M_366, new CourseGroup(MathPlanConstants.M_366, null,
                        MathPlanConstants.M_366, MathPlanConstants.M_366));
                this.courseGroups.put(MathPlanConstants.M_369, new CourseGroup(MathPlanConstants.M_369, null,
                        MathPlanConstants.M_369, MathPlanConstants.M_369));
                this.courseGroups.put(MathPlanConstants.M_384, new CourseGroup(MathPlanConstants.M_384, null,
                        MathPlanConstants.M_384, MathPlanConstants.M_384));

                this.courseGroups.put(MathPlanConstants.M_405, new CourseGroup(MathPlanConstants.M_405, null,
                        MathPlanConstants.M_405, MathPlanConstants.M_405));
                this.courseGroups.put(MathPlanConstants.M_417, new CourseGroup(MathPlanConstants.M_417, null,
                        MathPlanConstants.M_417, MathPlanConstants.M_417));
                this.courseGroups.put(MathPlanConstants.M_418, new CourseGroup(MathPlanConstants.M_418, null,
                        MathPlanConstants.M_418, MathPlanConstants.M_418));
                this.courseGroups.put(MathPlanConstants.M_419, new CourseGroup(MathPlanConstants.M_419, null,
                        MathPlanConstants.M_419, MathPlanConstants.M_419));
                this.courseGroups.put(MathPlanConstants.M_425, new CourseGroup(MathPlanConstants.M_425, null,
                        MathPlanConstants.M_425, MathPlanConstants.M_425));
                this.courseGroups.put(MathPlanConstants.M_430, new CourseGroup(MathPlanConstants.M_430, null,
                        MathPlanConstants.M_430, MathPlanConstants.M_430));
                this.courseGroups.put(MathPlanConstants.M_435, new CourseGroup(MathPlanConstants.M_435, null,
                        MathPlanConstants.M_435, MathPlanConstants.M_435));
                this.courseGroups.put(MathPlanConstants.M_450, new CourseGroup(MathPlanConstants.M_450, null,
                        MathPlanConstants.M_450, MathPlanConstants.M_450));
                this.courseGroups.put(MathPlanConstants.M_451, new CourseGroup(MathPlanConstants.M_451, null,
                        MathPlanConstants.M_451, MathPlanConstants.M_451));
                this.courseGroups.put(MathPlanConstants.M_455, new CourseGroup(MathPlanConstants.M_455, null,
                        MathPlanConstants.M_455, MathPlanConstants.M_455));
                this.courseGroups.put(MathPlanConstants.M_460, new CourseGroup(MathPlanConstants.M_460, null,
                        MathPlanConstants.M_460, MathPlanConstants.M_460));
                this.courseGroups.put(MathPlanConstants.M_463, new CourseGroup(MathPlanConstants.M_463, null,
                        MathPlanConstants.M_463, MathPlanConstants.M_463));
                this.courseGroups.put(MathPlanConstants.M_466, new CourseGroup(MathPlanConstants.M_466, null,
                        MathPlanConstants.M_466, MathPlanConstants.M_466));
                this.courseGroups.put(MathPlanConstants.M_467, new CourseGroup(MathPlanConstants.M_467, null,
                        MathPlanConstants.M_467, MathPlanConstants.M_467));
                this.courseGroups.put(MathPlanConstants.M_469, new CourseGroup(MathPlanConstants.M_469, null,
                        MathPlanConstants.M_469, MathPlanConstants.M_469));
                this.courseGroups.put(MathPlanConstants.M_470, new CourseGroup(MathPlanConstants.M_470, null,
                        MathPlanConstants.M_470, MathPlanConstants.M_470));
                this.courseGroups.put(MathPlanConstants.M_472, new CourseGroup(MathPlanConstants.M_472, null,
                        MathPlanConstants.M_472, MathPlanConstants.M_472));
                this.courseGroups.put(MathPlanConstants.M_474, new CourseGroup(MathPlanConstants.M_474, null,
                        MathPlanConstants.M_474, MathPlanConstants.M_474));
                this.courseGroups.put(MathPlanConstants.M_476, new CourseGroup(MathPlanConstants.M_476, null,
                        MathPlanConstants.M_476, MathPlanConstants.M_476));
                this.courseGroups.put(MathPlanConstants.M_484, new CourseGroup(MathPlanConstants.M_484, null,
                        MathPlanConstants.M_484, MathPlanConstants.M_484));
                this.courseGroups.put(MathPlanConstants.M_487, new CourseGroup(MathPlanConstants.M_487, null,
                        MathPlanConstants.M_487, MathPlanConstants.M_487));
                this.courseGroups.put(MathPlanConstants.M_495, new CourseGroup(MathPlanConstants.M_495, null,
                        MathPlanConstants.M_495, MathPlanConstants.M_495));
                this.courseGroups.put(MathPlanConstants.M_498, new CourseGroup(MathPlanConstants.M_498, null,
                        MathPlanConstants.M_498, MathPlanConstants.M_498));

                // Pick-list in AGED (Ag Literacy), LSBM
                this.courseGroups.put(MathPlanConstants.AGED3A, new CourseGroup(MathPlanConstants.AGED3A,
                        MathPlanConstants.THREE, RawRecordConstants.M124,
                        RawRecordConstants.M117, RawRecordConstants.M118, RawRecordConstants.M124));

                // Pick-list in AGED (Teacher Development)
                this.courseGroups.put(MathPlanConstants.AGED3B, new CourseGroup(MathPlanConstants.AGED3B,
                        MathPlanConstants.THREE, RawRecordConstants.M124,
                        RawRecordConstants.M117, RawRecordConstants.M118, RawRecordConstants.M124,
                        MathPlanConstants.M_141, MathPlanConstants.M_155,
                        MathPlanConstants.M_160));

                // Pick-list in ANIM, EQSC
                this.courseGroups.put(MathPlanConstants.ANIM3, new CourseGroup(MathPlanConstants.ANIM3,
                        MathPlanConstants.THREE, RawRecordConstants.M124,
                        RawRecordConstants.M117, RawRecordConstants.M118, RawRecordConstants.M124,
                        RawRecordConstants.M125, RawRecordConstants.M126, MathPlanConstants.M_141,
                        MathPlanConstants.M_155));

                // Pick-list in BIOM
                this.courseGroups.put(MathPlanConstants.BIOM1, new CourseGroup(MathPlanConstants.BIOM1,
                        MathPlanConstants.ONE, RawRecordConstants.M118,
                        RawRecordConstants.M118, RawRecordConstants.M124, RawRecordConstants.M125,
                        RawRecordConstants.M126));

                // Pick-list in BIOM
                this.courseGroups.put(MathPlanConstants.BIOM2, new CourseGroup(MathPlanConstants.BIOM2,
                        MathPlanConstants.TWO, RawRecordConstants.M117,
                        RawRecordConstants.M118, RawRecordConstants.M124, RawRecordConstants.M125,
                        RawRecordConstants.M126, MathPlanConstants.M_155, MathPlanConstants.M_160));

                // Pick-list in BIOM
                this.courseGroups.put(MathPlanConstants.BIOM3, new CourseGroup(MathPlanConstants.BIOM3,
                        MathPlanConstants.TWO, RawRecordConstants.M124,
                        RawRecordConstants.M125, RawRecordConstants.M126, MathPlanConstants.M_155,
                        MathPlanConstants.M_160));

                // Pick-list in BUSA
                this.courseGroups.put(MathPlanConstants.BUSA3, new CourseGroup(MathPlanConstants.BUSA3,
                        MathPlanConstants.THREE, RawRecordConstants.M124,
                        RawRecordConstants.M117, RawRecordConstants.M118, RawRecordConstants.M124,
                        RawRecordConstants.M125, RawRecordConstants.M126, MathPlanConstants.M_141,
                        MathPlanConstants.M_155, MathPlanConstants.M_156, MathPlanConstants.M_160));

                this.courseGroups.put(MathPlanConstants.AUCC3, new CourseGroup(MathPlanConstants.AUCC3,
                        MathPlanConstants.THREE, MathPlanConstants.M_101,
                        MathPlanConstants.M_101, MathPlanConstants.S_100, MathPlanConstants.M_105,
                        RawRecordConstants.M117, RawRecordConstants.M118, RawRecordConstants.M124,
                        RawRecordConstants.M125, RawRecordConstants.M126, RawRecordConstants.M120,
                        RawRecordConstants.M127, MathPlanConstants.S_201, MathPlanConstants.S_204,
                        MathPlanConstants.F_200, MathPlanConstants.M_141, MathPlanConstants.M_155,
                        MathPlanConstants.M_160, MathPlanConstants.M_161, MathPlanConstants.M_255));

                this.courseGroups.put(MathPlanConstants.FRRS3, new CourseGroup(MathPlanConstants.FRRS3,
                        MathPlanConstants.THREE, RawRecordConstants.M125,
                        RawRecordConstants.M117, RawRecordConstants.M118, RawRecordConstants.M125,
                        MathPlanConstants.M_141));

                this.courseGroups.put(MathPlanConstants.AUCC2, new CourseGroup(MathPlanConstants.AUCC2,
                        MathPlanConstants.TWO, MathPlanConstants.M_101,
                        MathPlanConstants.M_101, MathPlanConstants.S_100, MathPlanConstants.M_105,
                        RawRecordConstants.M117, RawRecordConstants.M118, RawRecordConstants.M124,
                        RawRecordConstants.M125, RawRecordConstants.M126, RawRecordConstants.M120,
                        RawRecordConstants.M127, MathPlanConstants.S_201, MathPlanConstants.S_204,
                        MathPlanConstants.F_200, MathPlanConstants.M_141, MathPlanConstants.M_155,
                        MathPlanConstants.M_160, MathPlanConstants.M_161, MathPlanConstants.M_255));

                // Pick-list in ECON
                this.courseGroups.put(MathPlanConstants.CALC, new CourseGroup(MathPlanConstants.CALC, null,
                        MathPlanConstants.M_141,
                        MathPlanConstants.M_141, MathPlanConstants.M_155, MathPlanConstants.M_160));

                // Pick-list in ECSS, FWCB, WRSC, BCHM, BLSC, CHEM, NSCI, ZOOL, BIOM
                this.courseGroups.put(MathPlanConstants.CALC1BIO, new CourseGroup(MathPlanConstants.CALC1BIO, null,
                        MathPlanConstants.M_155,
                        MathPlanConstants.M_155, MathPlanConstants.M_160));

                // Pick-list in WRSC, BCHM, NSCI
                this.courseGroups.put(MathPlanConstants.CALC2BIO, new CourseGroup(MathPlanConstants.CALC2BIO, null,
                        MathPlanConstants.M_255,
                        MathPlanConstants.M_255, MathPlanConstants.M_161));

                // Pick-list in CHEM
                this.courseGroups.put(MathPlanConstants.CALC2CHM, new CourseGroup(MathPlanConstants.CALC2CHM, null,
                        MathPlanConstants.M_161,
                        MathPlanConstants.M_161, MathPlanConstants.M_271));

                // Pick-list in CHEM
                this.courseGroups.put(MathPlanConstants.CALC3CHM, new CourseGroup(MathPlanConstants.CALC3CHM, null,
                        MathPlanConstants.M_261,
                        MathPlanConstants.M_261, MathPlanConstants.M_272));

                // Pick-list in CPSC
                this.courseGroups.put(MathPlanConstants.CALC1CS, new CourseGroup(MathPlanConstants.CALC1CS, null,
                        MathPlanConstants.M_156,
                        MathPlanConstants.M_156, MathPlanConstants.M_160));

                // Pick-list in CPSC
                this.courseGroups.put(MathPlanConstants.LINALG369, new CourseGroup(MathPlanConstants.LINALG369, null,
                        MathPlanConstants.M_369,
                        MathPlanConstants.M_369, MathPlanConstants.D_369));

                // Pick-list in MATH, PHYS
                this.courseGroups.put(MathPlanConstants.ODE, new CourseGroup(MathPlanConstants.ODE, null,
                        MathPlanConstants.M_340,
                        MathPlanConstants.M_340, MathPlanConstants.M_345));

                // Pick-list in MATH
                this.courseGroups.put(MathPlanConstants.MATH2, new CourseGroup(MathPlanConstants.MATH2, null,
                        MathPlanConstants.M_340,
                        MathPlanConstants.M_340, MathPlanConstants.M_345, MathPlanConstants.M_360,
                        MathPlanConstants.M_366));

                // Pick-list in MATH
                this.courseGroups.put(MathPlanConstants.MATH3, new CourseGroup(MathPlanConstants.MATH3, null,
                        MathPlanConstants.M_360,
                        MathPlanConstants.M_360, MathPlanConstants.M_366));

                // Pick-list in MATH
                this.courseGroups.put(MathPlanConstants.MATH4, new CourseGroup(MathPlanConstants.MATH4, null,
                        MathPlanConstants.M_417,
                        MathPlanConstants.M_417, MathPlanConstants.M_435, MathPlanConstants.M_466));

                // Pick-list in MATH
                this.courseGroups.put(MathPlanConstants.MATH5, new CourseGroup(MathPlanConstants.MATH5, null,
                        MathPlanConstants.M_435,
                        MathPlanConstants.M_435, MathPlanConstants.M_460));
            }

            return Collections.unmodifiableMap(this.courseGroups);
        }
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

        if (result == null) {
            Log.warning("No major found with program code '", programCode, "' (scanned " + allMajors.size() + ")");
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
                final Map<Major, MajorMathRequirement> map = new HashMap<>(400);

                // *** Last reviewed April 29, 2024 ***

                // ================================
                // College of Agricultural Sciences
                // ================================

                // *** Major in Agricultural Biology (with three concentrations)

                final Major mAGBI = new Major(1090, "AGBI-BS",
                        Boolean.TRUE, "Agricultural Biology",
                        MathPlanConstants.CAT + "agricultural-sciences/agricultural-biology/agricultural-biology" +
                                "-major");
                final MajorMathRequirement rAGBI = new MajorMathRequirement("AGBI-BS")
                        .setSemesterCourses("M 117!,M 118!,M 124!,M 125!", null, MathPlanConstants.M_155);
                map.put(mAGBI, rAGBI);

                final Major mAGBIENTZ = new Major(1091, "AGBI-ENTZ-BS",
                        Boolean.TRUE, "Agricultural Biology",
                        "Entomology",
                        MathPlanConstants.CAT + "agricultural-sciences/agricultural-biology/"
                                + "agricultural-biology-major-entomology-concentration/");
                final MajorMathRequirement rAGBIENTZ =
                        new MajorMathRequirement("AGBI-ENTZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124!,M 125!", null, MathPlanConstants.M_155);
                map.put(mAGBIENTZ, rAGBIENTZ);

                final Major mAGBIPLPZ = new Major(1092, "AGBI-PLPZ-BS",
                        Boolean.TRUE, "Agricultural Biology",
                        "Plant Pathology",
                        MathPlanConstants.CAT + "agricultural-sciences/agricultural-biology/"
                                + "agricultural-biology-major-plant-pathology-concentration/");
                final MajorMathRequirement rAGBIPLPZ =
                        new MajorMathRequirement("AGBI-PLPZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124!,M 125!", null, MathPlanConstants.M_155);
                map.put(mAGBIPLPZ, rAGBIPLPZ);

                final Major mAGBIWEEZ = new Major(1093, "AGBI-WEEZ-BS",
                        Boolean.TRUE, "Agricultural Biology",
                        "Weed Science",
                        MathPlanConstants.CAT + "agricultural-sciences/agricultural-biology/"
                                + "agricultural-biology-major-weed-science-concentration/");
                final MajorMathRequirement rAGBIWEEZ =
                        new MajorMathRequirement("AGBI-WEEZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124!,M 125!", null, MathPlanConstants.M_155);
                map.put(mAGBIWEEZ, rAGBIWEEZ);

                // *** Major in Agricultural Business (with two concentrations)

                final Major mAGBU = new Major(1000, "AGBU-BS",
                        Boolean.TRUE, "Agricultural Business",
                        MathPlanConstants.CAT + "agricultural-sciences/agricultural-resource-economics/business-major" +
                                "/");
                final MajorMathRequirement rAGBU = new MajorMathRequirement("AGBU-BS")
                        .setSemesterCourses("M 117!,M 118!,M 124", null, MathPlanConstants.M_141);
                map.put(mAGBU, rAGBU);

                final Major mAGBUAECZ = new Major(1001, "AGBU-AECZ-BS",
                        Boolean.TRUE, "Agricultural Business",
                        "Agricultural Economics",
                        MathPlanConstants.CAT + "agricultural-sciences/agricultural-resource-economics/"
                                + "business-agricultural-economics-concentration/");
                final MajorMathRequirement rAGBUAECZ =
                        new MajorMathRequirement("AGBU-AECZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124", null, MathPlanConstants.M_141);
                map.put(mAGBUAECZ, rAGBUAECZ);

                final Major mAGBUFRCZ = new Major(1002, "AGBU-FRCZ-BS",
                        Boolean.TRUE, "Agricultural Business",
                        "Farm and Ranch Management",
                        MathPlanConstants.CAT + "agricultural-sciences/agricultural-resource-economics/"
                                + "business-farm-ranch-management-concentration/");
                final MajorMathRequirement rAGBUFRCZ =
                        new MajorMathRequirement("AGBU-FRCZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124", null, MathPlanConstants.M_141);
                map.put(mAGBUFRCZ, rAGBUFRCZ);

                final Major mAGBUFSSZ = new Major(1003, "AGBU-FSSZ-BS",
                        Boolean.TRUE, "Agricultural Business",
                        "Food Systems",
                        MathPlanConstants.CAT + "agricultural-sciences/agricultural-resource-economics/"
                                + "business-major/food-systems-concentration/");
                final MajorMathRequirement rAGBUFSSZ =
                        new MajorMathRequirement("AGBU-FSSZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124", null, MathPlanConstants.M_141);
                map.put(mAGBUFSSZ, rAGBUFSSZ);

                // *** Major in Agricultural Education (with two concentrations)

                final Major mAGED = new Major(1010, "AGED-BS", Boolean.FALSE,
                        "Agricultural Education",
                        MathPlanConstants.CAT + "agricultural-sciences/agricultural-resource-economics/education" +
                                "-major/");
                final MajorMathRequirement rAGED = new MajorMathRequirement("AGED-BS")
                        .setSemesterCourses(MathPlanConstants.AGED3A, null, null);
                map.put(mAGED, rAGED);

                final Major mAGEDAGLZ = new Major(1011, "AGED-AGLZ-BS",
                        Boolean.TRUE, "Agricultural Education",
                        "Agricultural Literacy",
                        MathPlanConstants.CAT + "agricultural-sciences/agricultural-resource-economics/"
                                + "education-literacy-concentration/");
                final MajorMathRequirement rAGEDAGLZ =
                        new MajorMathRequirement("AGED-AGLZ-BS")
                                .setSemesterCourses(MathPlanConstants.AGED3A, null, null);
                map.put(mAGEDAGLZ, rAGEDAGLZ);

                final Major mAGEDTDLZ = new Major(1012, "AGED-TDLZ-BS",
                        Boolean.TRUE, "Agricultural Education",
                        "Teacher Development",
                        MathPlanConstants.CAT + "agricultural-sciences/agricultural-resource-economics/"
                                + "education-teacher-development-concentration/");
                final MajorMathRequirement rAGEDTDLZ =
                        new MajorMathRequirement("AGED-TDLZ-BS")
                                .setSemesterCourses(null, MathPlanConstants.AGED3B, null);
                map.put(mAGEDTDLZ, rAGEDTDLZ);

                // *** Major in Animal Science

                final Major mANIM = new Major(1020, "ANIM-BS", Boolean.TRUE,
                        "Animal Science",
                        MathPlanConstants.CAT + "agricultural-sciences/animal-sciences/animal-science-major/");
                final MajorMathRequirement rANIM = new MajorMathRequirement("ANIM-BS")
                        .setSemesterCourses("ANIM3!", null, null);
                map.put(mANIM, rANIM);

                // *** Major in Environmental and Natural Resource Economics

                final Major mENRE = new Major(1030, "ENRE-BS", Boolean.TRUE,
                        "Environmental and Natural Resource Economics",
                        MathPlanConstants.CAT + "agricultural-sciences/agricultural-resource-economics/environmental" +
                                "-natural-major/");
                final MajorMathRequirement rENRE = new MajorMathRequirement("ENRE-BS")
                        .setSemesterCourses("M 117!,M 118,M 124", null, MathPlanConstants.M_141);
                map.put(mENRE, rENRE);

                // *** Major in Environmental Horticulture (with three concentrations)

                final Major mENHR = new Major(1040, "ENHR-BS", Boolean.FALSE,
                        "Environmental Horticulture",
                        MathPlanConstants.CAT + "agricultural-sciences/horticulture-landscape-architecture/"
                                + "environmental-horticulture-major/");
                final MajorMathRequirement rENHR = new MajorMathRequirement("ENHR-BS")
                        .setSemesterCourses("M 117!,M 118!", null, null);
                map.put(mENHR, rENHR);

                // DEACTIVATED (commented to preserve what the historical number represents):
                // 1041: ENHR-LNBZ-BS, Environmental Horticulture / Landscape Business

                final Major mENHRLDAZ = new Major(1042, "ENHR-LDAZ-BS",
                        Boolean.TRUE, "Environmental Horticulture",
                        "Landscape Design and Contracting",
                        MathPlanConstants.CAT + "agricultural-sciences/horticulture-landscape-architecture/"
                                + "environmental-major-design-contracting-concentration/");
                final MajorMathRequirement rENHRLNBZ =
                        new MajorMathRequirement("ENHR-LDAZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 125!", null, null);
                map.put(mENHRLDAZ, rENHRLNBZ);

                final Major mENHRNALZ = new Major(1043, "ENHR-NALZ-BS",
                        Boolean.TRUE, "Environmental Horticulture",
                        "Nursery and Landscape Management",
                        MathPlanConstants.CAT + "agricultural-sciences/horticulture-landscape-architecture/"
                                + "environmental-major-nursery-management-concentration/");
                final MajorMathRequirement rENHRNALZ =
                        new MajorMathRequirement("ENHR-NALZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124!", null, null);
                map.put(mENHRNALZ, rENHRNALZ);

                final Major mENHRTURZ = new Major(1044, "ENHR-TURZ-BS",
                        Boolean.TRUE, "Environmental Horticulture",
                        "Turf Management",
                        MathPlanConstants.CAT + "agricultural-sciences/horticulture-landscape-architecture/"
                                + "environmental-major-turf-management-concentration/");
                final MajorMathRequirement rENHRTURZ =
                        new MajorMathRequirement("ENHR-TURZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124!", null, null);
                map.put(mENHRTURZ, rENHRTURZ);

                // *** Major in Equine Science

                final Major mEQSC = new Major(1050, "EQSC-BS", Boolean.TRUE,
                        "Equine Science",
                        MathPlanConstants.CAT + "agricultural-sciences/animal-sciences/equine-science-major/");
                final MajorMathRequirement rEQSC = new MajorMathRequirement("EQSC-BS")
                        .setSemesterCourses(MathPlanConstants.ANIM3, null, null);
                map.put(mEQSC, rEQSC);

                // *** Major in Horticulture (with five concentrations)

                final Major mHORT = new Major(1060, "HORT-BS", Boolean.FALSE,
                        "Horticulture",
                        MathPlanConstants.CAT + "agricultural-sciences/horticulture-landscape-architecture" +
                                "/horticulture-major/");
                final MajorMathRequirement rHORT = new MajorMathRequirement("HORT-BS")
                        .setSemesterCourses("M 117!,M 118,M 124", null, null);
                map.put(mHORT, rHORT);

                final Major mHORTCEHZ = new Major(1066, "HORT-CEHZ-BS",
                        Boolean.TRUE, "Horticulture",
                        "Controlled Environmental Horticulture",
                        MathPlanConstants.CAT + "agricultural-sciences/horticulture-landscape-architecture/"
                                + "horticulture-major-controlled-environment-horticulture-concentration/");
                final MajorMathRequirement rHORTCEHZ =
                        new MajorMathRequirement("HORT-CEHZ-BS")
                                .setSemesterCourses("M 117!,M 118,M 124", null, null);
                map.put(mHORTCEHZ, rHORTCEHZ);

                // DEACTIVATED (commented to preserve what the historical number represents):
                // 1061: HORT-FLOZ-BS, Horticulture / Floriculture

                final Major mHORTHBMZ = new Major(1062, "HORT-HBMZ-BS",
                        Boolean.TRUE, "Horticulture",
                        "Horticultural Business Management",
                        MathPlanConstants.CAT + "agricultural-sciences/horticulture-landscape-architecture/"
                                + "horticulture-major-business-management-concentration/");
                final MajorMathRequirement rHORTHBMZ =
                        new MajorMathRequirement("HORT-HBMZ-BS")
                                .setSemesterCourses("M 117!,M 118,M 124", null, null);
                map.put(mHORTHBMZ, rHORTHBMZ);

                final Major mHORTHFCZ = new Major(1063, "HORT-HFCZ-BS",
                        Boolean.TRUE, "Horticulture",
                        "Horticultural Food Crops",
                        MathPlanConstants.CAT + "agricultural-sciences/horticulture-landscape-architecture/"
                                + "horticulture-major-food-crops-concentration/");
                final MajorMathRequirement rHORTHFCZ =
                        new MajorMathRequirement("HORT-HFCZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124!", null, null);
                map.put(mHORTHFCZ, rHORTHFCZ);

                final Major mHORTHOSZ = new Major(1064, "HORT-HOSZ-BS",
                        Boolean.TRUE, "Horticulture",
                        "Horticultural Science",
                        MathPlanConstants.CAT + "agricultural-sciences/horticulture-landscape-architecture/"
                                + "horticulture-major-science-concentration/");
                final MajorMathRequirement rHORTHOSZ =
                        new MajorMathRequirement("HORT-HOSZ-BS")
                                .setSemesterCourses("M 124,M 125", RawRecordConstants.M126, MathPlanConstants.M_155);
                map.put(mHORTHOSZ, rHORTHOSZ);

                // DEACTIVATED (commented to preserve what the historical number represents):
                // 1065: HORT-HTHZ-BS, Horticulture/Horticultural Therapy

                // *** Major in Landscape Architecture

                final Major mLDAR = new Major(1070, "LDAR-BS", Boolean.TRUE,
                        "Landscape Architecture",
                        MathPlanConstants.CAT + "agricultural-sciences/horticulture-landscape-architecture/"
                                + "landscape-architecture-major/");
                final MajorMathRequirement rLDAR = new MajorMathRequirement("LDAR-BS")
                        .setSemesterCourses(MathPlanConstants.AUCC2, RawRecordConstants.M126, null);
                map.put(mLDAR, rLDAR);

                // *** Major in Livestock Business Management

                final Major mLSBM = new Major(1075, "LSBM-BS", Boolean.TRUE,
                        "Livestock Business Management",
                        MathPlanConstants.CAT + "agricultural-sciences/agricultural-resource-economics/"
                                + "livestock-business-management-major/");
                final MajorMathRequirement rLSBM = new MajorMathRequirement("LSBM-BS")
                        .setSemesterCourses("AGED3A!", RawRecordConstants.M126, null);
                map.put(mLSBM, rLSBM);

                // *** Major in Soil and Crop Sciences (with six concentrations)

                final Major mSOCR = new Major(1080, "SOCR-BS", Boolean.TRUE,
                        "Soil and Crop Sciences",
                        MathPlanConstants.CAT + "agricultural-sciences/soil-crop-sciences/soil-crop-sciences-major/");
                final MajorMathRequirement rSOCR = new MajorMathRequirement("SOCR-BS")
                        .setSemesterCourses("M 117!,M 118!,M 124!", null, null);
                map.put(mSOCR, rSOCR);

                // DEACTIVATED (commented to preserve what the historical number represents):
                // 1081: SOCR-APMZ-BS, Soil and Crop Sciences/Agronomic Production Management

                // DEACTIVATED (commented to preserve what the historical number represents):
                // 1082: SOCR-APIZ-BS, Soil and Crop Sciences/Applied Information Technology

                // DEACTIVATED (commented to preserve what the historical number represents):
                // 1083: SOCR-ISCZ-BS, Soil and Crop Sciences/International Soil and Crop Sciences

                // DEACTIVATED (commented to preserve what the historical number represents):
                // 1084: SOCR-PBGZ-BS, Soil and Crop Sciences/Plant Biotechnology, Genetics, and Breeding

                // DEACTIVATED (commented to preserve what the historical number represents):
                // 1085: SOCR-SOEZ-BS, Soil and Crop Sciences/Soil Ecology

                // DEACTIVATED (commented to preserve what the historical number represents):
                // 1086: SOCR-SRNZ-BS, Soil and Crop Sciences/Soil Restoration and Conservation

                final Major mSOCRPBTZ = new Major(1087, "SOCR-PBTZ-BS",
                        Boolean.TRUE, "Soil and Crop Sciences",
                        "Plant Biotechnology",
                        MathPlanConstants.CAT + "agricultural-sciences/soil-crop-sciences/"
                                + "soil-crop-sciences-major-plant-biotechnology-concentration/");
                final MajorMathRequirement rSOCRPBTZ =
                        new MajorMathRequirement("SOCR-PBTZ-BS")
                                .setSemesterCourses("M 117,M 118", RawRecordConstants.M124, RawRecordConstants.M125);
                map.put(mSOCRPBTZ, rSOCRPBTZ);

                final Major mSOCRSESZ = new Major(1088, "SOCR-SESZ-BS",
                        Boolean.TRUE, "Soil and Crop Sciences",
                        "Soil Science and Environmental Solutions",
                        MathPlanConstants.CAT + "agricultural-sciences/soil-crop-sciences/"
                                + "soil-crop-sciences-major-science-environmental-solutions-concentration/");
                final MajorMathRequirement rSOCRSESZ =
                        new MajorMathRequirement("SOCR-SESZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124!", null, null);
                map.put(mSOCRSESZ, rSOCRSESZ);

                final Major mSOCRSAMZ = new Major(1089, "SOCR-SAMZ-BS",
                        Boolean.TRUE, "Soil and Crop Sciences",
                        "Sustainable Agricultural Management",
                        MathPlanConstants.CAT + "agricultural-sciences/soil-crop-sciences/"
                                + "soil-crop-sciences-major-sustainable-agricultural-management-concentration/");
                final MajorMathRequirement rSOCRSAMZ =
                        new MajorMathRequirement("SOCR-SAMZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124!", null, null);
                map.put(mSOCRSAMZ, rSOCRSAMZ);

                // ===================
                // College of Business
                // ===================

                // *** Major in Business Administration (with nine concentrations)
                // NOTE: Catalog shows this math in semester 2 - Business has requested we list it in semester 1

                final Major mBUSA = new Major(2000, "BUSA-BS", Boolean.FALSE,
                        "Business Administration",
                        MathPlanConstants.CAT + "business/business-administration/business-administration-major/");
                final MajorMathRequirement rBUSA = new MajorMathRequirement("BUSA-BS")
                        .setSemesterCourses(MathPlanConstants.BUSA3, null, null);
                map.put(mBUSA, rBUSA);

                final Major mBUSAACCZ = new Major(2001, "BUSA-ACCZ-BS",
                        Boolean.TRUE, "Business Administration",
                        "Accounting",
                        MathPlanConstants.CAT + "business/accounting/business-administration-major-accounting" +
                                "-concentration/");
                final MajorMathRequirement rBUSAACCZ =
                        new MajorMathRequirement("BUSA-ACCZ-BS")
                                .setSemesterCourses(MathPlanConstants.BUSA3, null, null);
                map.put(mBUSAACCZ, rBUSAACCZ);

                final Major mBUSAFINZ = new Major(2002, "BUSA-FINZ-BS",
                        Boolean.TRUE, "Business Administration",
                        "Finance",
                        MathPlanConstants.CAT + "business/finance-real-estate/business-administration-major-finance" +
                                "-concentration/");
                final MajorMathRequirement rBUSAFINZ =
                        new MajorMathRequirement("BUSA-FINZ-BS")
                                .setSemesterCourses(MathPlanConstants.BUSA3, null, null);
                map.put(mBUSAFINZ, rBUSAFINZ);

                final Major mBUSAFPLZ = new Major(2003, "BUSA-FPLZ-BS",
                        Boolean.TRUE, "Business Administration",
                        "Financial Planning",
                        MathPlanConstants.CAT + "business/finance-real-estate/business-administration-"
                                + "major-financial-planning-concentration/#text");
                final MajorMathRequirement rBUSAFPLZ =
                        new MajorMathRequirement("BUSA-FPLZ-BS")
                                .setSemesterCourses(MathPlanConstants.BUSA3, null, null);
                map.put(mBUSAFPLZ, rBUSAFPLZ);

                final Major mBUSAHRMZ = new Major(2004, "BUSA-HRMZ-BS",
                        Boolean.TRUE, "Business Administration",
                        "Human Resource Management",
                        MathPlanConstants.CAT + "business/management/business-administration-major-human-"
                                + "resource-management-concentration/");
                final MajorMathRequirement rBUSAHRMZ =
                        new MajorMathRequirement("BUSA-HRMZ-BS")
                                .setSemesterCourses(MathPlanConstants.BUSA3, null, null);
                map.put(mBUSAHRMZ, rBUSAHRMZ);

                final Major mBUSAINSZ = new Major(2005, "BUSA-INSZ-BS",
                        Boolean.TRUE, "Business Administration",
                        "Information Systems",
                        MathPlanConstants.CAT + "business/computer-information-systems/business-"
                                + "administration-major-information-systems-concentration/");
                final MajorMathRequirement rBUSAINSZ =
                        new MajorMathRequirement("BUSA-INSZ-BS")
                                .setSemesterCourses(MathPlanConstants.BUSA3, null, null);
                map.put(mBUSAINSZ, rBUSAINSZ);

                final Major mBUSAMINZ = new Major(2010, "BUSA-MINZ-BS",
                        Boolean.TRUE, "Business Administration",
                        "Management and Innovation",
                        MathPlanConstants.CAT + "business/management/business-administration-major-management" +
                                "-innovation-concentration/");
                final MajorMathRequirement rBUSAMINZ =
                        new MajorMathRequirement("BUSA-MINZ-BS")
                                .setSemesterCourses(MathPlanConstants.BUSA3, null, null);
                map.put(mBUSAMINZ, rBUSAMINZ);

                final Major mBUSAMKTZ = new Major(2006, "BUSA-MKTZ-BS",
                        Boolean.TRUE, "Business Administration",
                        "Marketing",
                        MathPlanConstants.CAT + "business/marketing/business-administration-major-marketing" +
                                "-concentration/");
                final MajorMathRequirement rBUSAMKTZ =
                        new MajorMathRequirement("BUSA-MKTZ-BS")
                                .setSemesterCourses(MathPlanConstants.BUSA3, null, null);
                map.put(mBUSAMKTZ, rBUSAMKTZ);

                // DEACTIVATED (commented to preserve what the historical number represents):
                // 2007: BUSA-OIMZ-BS, Business Administration/Organization and Innovation Management

                final Major mBUSAREAZ = new Major(2008, "BUSA-REAZ-BS",
                        Boolean.TRUE, "Business Administration",
                        "Real Estate",
                        MathPlanConstants.CAT + "business/finance-real-estate/business-administration-major-real" +
                                "-estate-concentration/");
                final MajorMathRequirement rBUSAREAZ =
                        new MajorMathRequirement("BUSA-REAZ-BS")
                                .setSemesterCourses(MathPlanConstants.BUSA3, null, null);
                map.put(mBUSAREAZ, rBUSAREAZ);

                final Major mBUSASCMZ = new Major(2009, "BUSA-SCMZ-BS",
                        Boolean.TRUE, "Business Administration",
                        "Supply Chain Management",
                        MathPlanConstants.CAT + "business/management/business-administration-major-"
                                + "supply-chain-management-concentration/");
                final MajorMathRequirement rBUSASCMZ =
                        new MajorMathRequirement("BUSA-SCMZ-BS")
                                .setSemesterCourses(MathPlanConstants.BUSA3, null, null);
                map.put(mBUSASCMZ, rBUSASCMZ);

                // ======================
                // College of Engineering
                // ======================

                // *** Dual-Degree programs in Biomedical Engineering

                final Major mCBEGDUAL = new Major(3000, "CBEG-DUAL",
                        Boolean.FALSE, "Biomedical Engineering, Dual Degree",
                        MathPlanConstants.CAT + "engineering/biomedical/");
                final MajorMathRequirement rCBEGDUAL =
                        new MajorMathRequirement("CBEG-DUAL")
                                .setSemesterCourses("M 160!", "M 161!", "M 261,M 340");
                map.put(mCBEGDUAL, rCBEGDUAL);

                final Major mCBEGBMEC = new Major(3001, "CBEG-BMEC-BS",
                        Boolean.TRUE, "Biomedical Engineering, Dual Degree",
                        "With Chemical and Biological Engineering",
                        MathPlanConstants.CAT + "engineering/biomedical/chemical-biological-dual-degree-program/");
                final MajorMathRequirement rCBEGBMEC =
                        new MajorMathRequirement("CBEG-BMEC-BS")
                                .setSemesterCourses("M 160!", "M 161!", "M 261,M 340");
                map.put(mCBEGBMEC, rCBEGBMEC);

                final Major mCPEGBMEP = new Major(3005, "CPEG-BMEP-BS",
                        Boolean.TRUE, "Biomedical Engineering, Dual Degree",
                        "With Computer Engineering",
                        MathPlanConstants.CAT + "engineering/biomedical/computer-dual-degree-program/");
                final MajorMathRequirement rCPEGBMEP =
                        new MajorMathRequirement("CPEG-BMEP-BS")
                                .setSemesterCourses("M 160!", "M 161!", "M 261,M 340");
                map.put(mCPEGBMEP, rCPEGBMEP);

                final Major mELEGBMEE = new Major(3002, "ELEG-BMEE-BS",
                        Boolean.TRUE, "Biomedical Engineering, Dual Degree",
                        "With Electrical Engineering (Electrical Engineering)",
                        MathPlanConstants.CAT + "engineering/biomedical/electrical-dual-degree-program/");
                final MajorMathRequirement rELEGBMEE =
                        new MajorMathRequirement("ELEG-BMEE-BS")
                                .setSemesterCourses("M 160!", "M 161!", "M 261,M 340");
                map.put(mELEGBMEE, rELEGBMEE);

                final Major mELEGBMEL = new Major(3003, "ELEG-BMEL-BS",
                        Boolean.TRUE, "Biomedical Engineering, Dual Degree",
                        "With Electrical Engineering (Lasers and Optical)",
                        MathPlanConstants.CAT + "engineering/biomedical/electrical-lasers-optical-concentration-dual" +
                                "-degree-program/");
                final MajorMathRequirement rELEGBMEL =
                        new MajorMathRequirement("ELEG-BMEL-BS")
                                .setSemesterCourses("M 160!", "M 161!", "M 261,M 340");
                map.put(mELEGBMEL, rELEGBMEL);

                final Major mMECHBMEM = new Major(3004, "MECH-BMEM-BS",
                        Boolean.TRUE, "Biomedical Engineering, Dual Degree",
                        "With Mechanical Engineering",
                        MathPlanConstants.CAT + "engineering/biomedical/mechanical-dual-degree-program/");
                final MajorMathRequirement rMECHBMEM =
                        new MajorMathRequirement("MECH-BMEM-BS")
                                .setSemesterCourses("M 160!", "M 161!", "M 261,M 340");
                map.put(mMECHBMEM, rMECHBMEM);

                // *** Major in Chemical and Biological Engineering

                final Major mCBEG = new Major(3010, "CBEG-BS", Boolean.TRUE,
                        "Chemical and Biological Engineering",
                        MathPlanConstants.CAT + "engineering/chemical-biological/chemical-biological-engineering" +
                                "-major/");
                final MajorMathRequirement rCBEG = new MajorMathRequirement("CBEG-BS")
                        .setSemesterCourses("M 160!", "M 161!", "M 261,M 340");
                map.put(mCBEG, rCBEG);

                // *** Major in Civil Engineering

                final Major mCIVE = new Major(3020, "CIVE-BS", Boolean.TRUE,
                        "Civil Engineering",
                        MathPlanConstants.CAT + "engineering/civil-environmental/civil-engineering-major/");
                final MajorMathRequirement rCIVE = new MajorMathRequirement("CIVE-BS")
                        .setSemesterCourses("M 160!", "M 161!", "M 261,M 340");
                map.put(mCIVE, rCIVE);

                // *** Major in Computer Engineering

                final Major mCPEG = new Major(3030, "CPEG-BS", Boolean.TRUE,
                        "Computer Engineering",
                        MathPlanConstants.CAT + "engineering/electrical-computer/computer-engineering-major/");
                final MajorMathRequirement rCPEG = new MajorMathRequirement("CPEG-BS")
                        .setSemesterCourses("M 160!", "M 161!", "M 261,M 340");
                map.put(mCPEG, rCPEG);

                final Major mCPEGAESZ = new Major(3031, "CPEG-AESZ-BS",
                        Boolean.TRUE, "Computer Engineering",
                        "Aerospace Systems",
                        MathPlanConstants.CAT + "engineering/electrical-computer/computer-"
                                + "engineering-major-aerospace-systems-concentration/");
                final MajorMathRequirement rCPEGAESZ =
                        new MajorMathRequirement("CPEG-AESZ-BS")
                                .setSemesterCourses("M 160!", "M 161!", "M 261,M 340");
                map.put(mCPEGAESZ, rCPEGAESZ);

                final Major mCPEGEISZ = new Major(3032, "CPEG-EISZ-BS",
                        Boolean.TRUE, "Computer Engineering",
                        "Embedded and IoT Systems",
                        MathPlanConstants.CAT + "engineering/electrical-computer/computer-"
                                + "engineering-major-embedded-iot-systems-concentration/");
                final MajorMathRequirement rCPEGEISZ =
                        new MajorMathRequirement("CPEG-EISZ-BS")
                                .setSemesterCourses("M 160!", "M 161!", "M 261,M 340");
                map.put(mCPEGEISZ, rCPEGEISZ);

                final Major mCPEGNDTZ = new Major(3033, "CPEG-NDTZ-BS",
                        Boolean.TRUE, "Computer Engineering",
                        "Networks and Data",
                        MathPlanConstants.CAT + "engineering/electrical-computer/computer-"
                                + "engineering-major-networks-data-concentration/");
                final MajorMathRequirement rCPEGNDTZ =
                        new MajorMathRequirement("CPEG-NDTZ-BS")
                                .setSemesterCourses("M 160!", "M 161!", "M 261,M 340");
                map.put(mCPEGNDTZ, rCPEGNDTZ);

                final Major mCPEGVICZ = new Major(3034, "CPEG-VICZ-BS",
                        Boolean.TRUE, "Computer Engineering",
                        "VLSI and Integrated Circuits",
                        MathPlanConstants.CAT + "engineering/electrical-computer/computer-"
                                + "engineering-major-vlsi-integrated-circuits-concentration/");
                final MajorMathRequirement rCPEGVICZ =
                        new MajorMathRequirement("CPEG-VICZ-BS")
                                .setSemesterCourses("M 160!", "M 161!", "M 261,M 340");
                map.put(mCPEGVICZ, rCPEGVICZ);

                // *** Major in Electrical Engineering (with two concentrations)

                final Major mELEG = new Major(3040, "ELEG-BS", Boolean.FALSE,
                        "Electrical Engineering",
                        MathPlanConstants.CAT + "engineering/electrical-computer/electrical-engineering-major/");
                final MajorMathRequirement rELEG = new MajorMathRequirement("ELEG-BS")
                        .setSemesterCourses("M 160!", "M 161!", "M 261,M 340");
                map.put(mELEG, rELEG);

                final Major mELEGELEZ = new Major(3041, "ELEG-ELEZ-BS",
                        Boolean.TRUE, "Electrical Engineering",
                        "Electrical Engineering",
                        MathPlanConstants.CAT + "engineering/electrical-computer/electrical-engineering-"
                                + "major-electrical-engineering-concentration/");
                final MajorMathRequirement rELEGELEZ =
                        new MajorMathRequirement("ELEG-ELEZ-BS")
                                .setSemesterCourses("M 160!", "M 161!", "M 261,M 340");
                map.put(mELEGELEZ, rELEGELEZ);

                final Major mELEGLOEZ = new Major(3042, "ELEG-LOEZ-BS",
                        Boolean.TRUE, "Electrical Engineering",
                        "Lasers and Optical Engineering",
                        MathPlanConstants.CAT + "engineering/electrical-computer/electrical-engineering-"
                                + "major-lasers-optical-concentration/");
                final MajorMathRequirement rELEGLOEZ =
                        new MajorMathRequirement("ELEG-LOEZ-BS")
                                .setSemesterCourses("M 160!", "M 161!", "M 261,M 340");
                map.put(mELEGLOEZ, rELEGLOEZ);

                final Major mELEGASPZ = new Major(3043, "ELEG-ASPZ-BS",
                        Boolean.TRUE, "Electrical Engineering",
                        "Aerospace",
                        MathPlanConstants.CAT + "engineering/electrical-computer/"
                                + "electrical-engineering-major-aerospace-concentration/");
                final MajorMathRequirement rELEGASPZ =
                        new MajorMathRequirement("ELEG-ASPZ-BS")
                                .setSemesterCourses("M 160!", "M 161!", "M 261,M 340");
                map.put(mELEGASPZ, rELEGASPZ);

                // *** Major in Engineering Science (with three concentrations)

                // DEACTIVATED (commented to preserve what the historical number represents):
                // 3050: EGSC-BS, Engineering Science

                // DEACTIVATED (commented to preserve what the historical number represents):
                // 3051: EGSC-EGPZ-BS, Engineering Science/Engineering Physics

                // DEACTIVATED (commented to preserve what the historical number represents):
                // 3052: EGSC-SPEZ-BS, Engineering Science/Space Engineering

                // DEACTIVATED (commented to preserve what the historical number represents):
                // 3053: EGSC-TCEZ-BS, Engineering Science/Teacher Education

                // *** Dual-Degree programs in Engineering Science

                // DEACTIVATED (commented to preserve what the historical number represents):
                // 3060: EGIS-DUAL, Engineering Science Dual Degree

                // DEACTIVATED (commented to preserve what the historical number represents):
                // 3061: ILES-BA, Engineering Science Dual Degree/With Interdisciplinary Liberal Arts

                // DEACTIVATED (commented to preserve what the historical number represents):
                // 3062: EGIS-BS, Engineering Science Dual Degree/With International Studies

                // *** Major in Environmental Engineering

                final Major mENVE = new Major(3070, "ENVE-BS", Boolean.TRUE,
                        "Environmental Engineering",
                        MathPlanConstants.CAT + "engineering/civil-environmental/environmental-engineering-major/");
                final MajorMathRequirement rENVE = new MajorMathRequirement("ENVE-BS")
                        .setSemesterCourses("M 160!", "M 161!", "M 261,M 340");
                map.put(mENVE, rENVE);

                // *** Major in Mechanical Engineering

                final Major mMECH = new Major(3080, "MECH-BS", Boolean.TRUE,
                        "Mechanical Engineering",
                        MathPlanConstants.CAT + "engineering/mechanical/mechanical-engineering-major/");
                final MajorMathRequirement rMECH = new MajorMathRequirement("MECH-BS")
                        .setSemesterCourses("M 160!", "M 161!", "M 261,M 340");
                map.put(mMECH, rMECH);

                final Major mMECHACEZ = new Major(3081, "MECH-ACEZ-BS",
                        Boolean.TRUE, "Mechanical Engineering",
                        "Aerospace Engineering",
                        MathPlanConstants.CAT + "engineering/mechanical/mechanical-engineering-major-"
                                + "aerospace-engineering-concentration/");
                final MajorMathRequirement rMECHACEZ =
                        new MajorMathRequirement("MECH-ACEZ-BS")
                                .setSemesterCourses("M 160!", "M 161!", "M 261,M 340");
                map.put(mMECHACEZ, rMECHACEZ);

                final Major mMECHADMZ = new Major(3082, "MECH-ADMZ-BS",
                        Boolean.TRUE, "Mechanical Engineering",
                        "Advanced Manufacturing",
                        MathPlanConstants.CAT + "engineering/mechanical/mechanical-engineering-major-"
                                + "advanced-manufacturing-concentration/");
                final MajorMathRequirement rMECHADMZ =
                        new MajorMathRequirement("MECH-ADMZ-BS")
                                .setSemesterCourses("M 160!", "M 161!", "M 261,M 340");
                map.put(mMECHADMZ, rMECHADMZ);

                // ====================================
                // College of Health and Human Sciences
                // ====================================

                // *** Major in Apparel and Merchandising (with three concentrations)

                final Major mAPAM = new Major(4000, "APAM-BS", Boolean.FALSE,
                        "Apparel and Merchandising",
                        MathPlanConstants.CAT + "health-human-sciences/design-merchandising/apparel-merchandising" +
                                "-major/");
                final MajorMathRequirement rAPAM = new MajorMathRequirement("APAM-BS")
                        .setSemesterCourses("M 117!,M 118!", RawRecordConstants.M124, null);
                map.put(mAPAM, rAPAM);

                final Major mAPAMADAZ = new Major(4001, "APAM-ADAZ-BS",
                        Boolean.TRUE, "Apparel and Merchandising",
                        "Apparel Design and Production",
                        MathPlanConstants.CAT + "health-human-sciences/design-merchandising/apparel-"
                                + "merchandising-major-design-production-concentration/");
                final MajorMathRequirement rAPAMADAZ =
                        new MajorMathRequirement("APAM-ADAZ-BS")
                                .setSemesterCourses("M 117!,M 118!", RawRecordConstants.M124, null);
                map.put(mAPAMADAZ, rAPAMADAZ);

                final Major mAPAMMDSZ = new Major(4002, "APAM-MDSZ-BS",
                        Boolean.TRUE, "Apparel and Merchandising",
                        "Merchandising",
                        MathPlanConstants.CAT + "health-human-sciences/design-merchandising/apparel-"
                                + "merchandising-major-merchandising-concentration/");
                final MajorMathRequirement rAPAMMDSZ =
                        new MajorMathRequirement("APAM-MDSZ-BS")
                                .setSemesterCourses("M 117!,M 118!", RawRecordConstants.M124, null);
                map.put(mAPAMMDSZ, rAPAMMDSZ);

                final Major mAPAMPDVZ = new Major(4003, "APAM-PDVZ-BS",
                        Boolean.TRUE, "Apparel and Merchandising",
                        "Product Development",
                        MathPlanConstants.CAT + "health-human-sciences/design-merchandising/apparel-"
                                + "merchandising-major-product-development-concentration/");
                final MajorMathRequirement rAPAMPDVZ =
                        new MajorMathRequirement("APAM-PDVZ-BS")
                                .setSemesterCourses("M 117!,M 118!", RawRecordConstants.M124, null);
                map.put(mAPAMPDVZ, rAPAMPDVZ);

                // *** Major in Construction Management

                final Major mCTMG = new Major(4010, "CTMG-BS", Boolean.TRUE,
                        "Construction Management",
                        MathPlanConstants.CAT + "health-human-sciences/construction-management/construction" +
                                "-management-major/");
                final MajorMathRequirement rCTMG = new MajorMathRequirement("CTMG-BS")
                        .setSemesterCourses("M 117!,M 118!,M 125!", null, MathPlanConstants.M_141);
                map.put(mCTMG, rCTMG);

                // *** Major in Early Childhood Education

                final Major mECHE = new Major(4020, "ECHE-BS", Boolean.TRUE,
                        "Early Childhood Education",
                        MathPlanConstants.CAT + "health-human-sciences/human-development-family-studies/"
                                + "early-childhood-education-major/");
                final MajorMathRequirement rECHE = new MajorMathRequirement("ECHE-BS")
                        .setSemesterCourses(MathPlanConstants.AUCC3, null, null);
                map.put(mECHE, rECHE);

                // *** Major in Family and Consumer Sciences (with two concentrations)

                final Major mFACS = new Major(4030, "FACS-BS", Boolean.FALSE,
                        "Family and Consumer Sciences",
                        MathPlanConstants.CAT + "health-human-sciences/education/family-consumer-sciences-major/");
                final MajorMathRequirement rFACS = new MajorMathRequirement("FACS-BS")
                        .setSemesterCourses("AUCC3!", null, null);
                map.put(mFACS, rFACS);

                // DEACTIVATED (commented to preserve what the historical number represents):
                // 4031: FACS-FACZ-BS, Family and Consumer Sciences/Family and Consumer Sciences

                final Major mFACSFCSZ = new Major(4032, "FACS-FCSZ-BS",
                        Boolean.TRUE, "Family and Consumer Sciences",
                        "Family and Consumer Sciences Education",
                        MathPlanConstants.CAT + "health-human-sciences/education/family-consumer-"
                                + "sciences-major-education-concentration/");
                final MajorMathRequirement rFACSFCSZ =
                        new MajorMathRequirement("FACS-FCSZ-BS")
                                .setSemesterCourses("AUCC3!", null, null);
                map.put(mFACSFCSZ, rFACSFCSZ);

                final Major mFACSIDSZ = new Major(4033, "FACS-IDSZ-BS",
                        Boolean.TRUE, "Family and Consumer Sciences",
                        "Interdisciplinary",
                        MathPlanConstants.CAT + "health-human-sciences/education/family-consumer-"
                                + "sciences-major-interdisciplinary-concentration/");
                final MajorMathRequirement rFACSIDSZ =
                        new MajorMathRequirement("FACS-IDSZ-BS")
                                .setSemesterCourses("AUCC3!", null, null);
                map.put(mFACSIDSZ, rFACSIDSZ);

                // *** Major in Fermentation Science and Technology

                final Major mFMST = new Major(4040, "FMST-BS", Boolean.TRUE,
                        "Fermentation Science and Technology",
                        MathPlanConstants.CAT + "health-human-sciences/food-science-human-nutrition/"
                                + "fermentation-science-technology-major/");
                final MajorMathRequirement rFMST = new MajorMathRequirement("FMST-BS")
                        .setSemesterCourses("M 117!,M 118!,M 124!", "M 125!", null);
                map.put(mFMST, rFMST);

                // *** Major in Health and Exercise Science (with two concentrations)

                final Major mHAES = new Major(4050, "HAES-BS", Boolean.FALSE,
                        "Health and Exercise Science",
                        MathPlanConstants.CAT + "health-human-sciences/health-exercise-science/health-exercise" +
                                "-science-major/");
                final MajorMathRequirement rHAES = new MajorMathRequirement("HAES-BS")
                        .setSemesterCourses("M 118.,M 124.", "M 125!", null);
                map.put(mHAES, rHAES);

                final Major mHAESHPRZ = new Major(4051, "HAES-HPRZ-BS",
                        Boolean.TRUE, "Health and Exercise Science",
                        "Health Promotion",
                        MathPlanConstants.CAT + "health-human-sciences/health-exercise-science/"
                                + "health-exercise-science-major-promotion-concentration/");
                final MajorMathRequirement rHAESHPRZ =
                        new MajorMathRequirement("HAES-HPRZ-BS")
                                .setSemesterCourses("M 118.,M 124.", "M 125!", null);
                map.put(mHAESHPRZ, rHAESHPRZ);

                final Major mHAESSPMZ = new Major(4052, "HAES-SPMZ-BS",
                        Boolean.TRUE, "Health and Exercise Science",
                        "Sports Medicine",
                        MathPlanConstants.CAT + "health-human-sciences/health-exercise-science/"
                                + "health-exercise-science-major-sports-medicine-concentration/");
                final MajorMathRequirement rHAESSPMZ =
                        new MajorMathRequirement("HAES-SPMZ-BS")
                                .setSemesterCourses("M 118.,M 124.", "M 125!", null);
                map.put(mHAESSPMZ, rHAESSPMZ);

                // *** Major in Hospitality Management

                final Major mHSMG = new Major(4060, "HSMG-BS", Boolean.TRUE,
                        "Hospitality and Event Management",
                        MathPlanConstants.CAT + "health-human-sciences/food-science-human-nutrition/"
                                + "hospitality-and-event-management-major/");
                final MajorMathRequirement rHSMG = new MajorMathRequirement("HSMG-BS")
                        .setSemesterCourses("M 101,M 117!", null, null);
                map.put(mHSMG, rHSMG);

                // *** Major in Human Development and Family Studies (with five concentrations)

                final Major mHDFS = new Major(4070, "HDFS-BS", Boolean.FALSE,
                        "Human Development and Family Studies",
                        MathPlanConstants.CAT + "health-human-sciences/human-development-family-studies/"
                                + "human-development-family-studies-major/");
                final MajorMathRequirement rHDFS = new MajorMathRequirement("HDFS-BS")
                        .setSemesterCourses(MathPlanConstants.AUCC3, null, null);
                map.put(mHDFS, rHDFS);

                final Major mHDFSECPZ = new Major(4071, "HDFS-ECPZ-BS",
                        Boolean.TRUE, "Human Development and Family Studies",
                        "Early Childhood Professions",
                        MathPlanConstants.CAT + "health-human-sciences/human-development-family-studies/"
                                + "human-development-family-studies-major-early-childhood-professions-concentration/");
                final MajorMathRequirement rHDFSECPZ =
                        new MajorMathRequirement("HDFS-ECPZ-BS")
                                .setSemesterCourses(MathPlanConstants.AUCC3, null, null);
                map.put(mHDFSECPZ, rHDFSECPZ);

                final Major mHDFSHDEZ = new Major(4072, "HDFS-HDEZ-BS",
                        Boolean.TRUE, "Human Development and Family Studies",
                        "Human Development and Family Studies",
                        MathPlanConstants.CAT + "health-human-sciences/human-development-family-studies/"
                                + "human-development-family-studies-major-human-development-"
                                + "family-studies-concentration/");
                final MajorMathRequirement rHDFSHDEZ =
                        new MajorMathRequirement("HDFS-HDEZ-BS")
                                .setSemesterCourses(MathPlanConstants.AUCC3, null, null);
                map.put(mHDFSHDEZ, rHDFSHDEZ);

                final Major mHDFSLADZ = new Major(4076, "HDFS-LADZ-BS",
                        Boolean.TRUE, "Human Development and Family Studies",
                        "Leadership and Advocacy",
                        MathPlanConstants.CAT + "health-human-sciences/human-development-family-studies/"
                                + "human-development-family-studies-major-leadership-advocacy-concentration/");
                final MajorMathRequirement rHDFSLADZ =
                        new MajorMathRequirement("HDFS-LADZ-BS")
                                .setSemesterCourses(MathPlanConstants.AUCC3, null, null);
                map.put(mHDFSLADZ, rHDFSLADZ);

                // DEACTIVATED (commented to preserve what the historical number represents):
                // 4073: HDFS-PHPZ-BS, Human Development and Family Studies/Leadership and Entrepreneurial Professions

                final Major mHDFSPHPZ = new Major(4074, "HDFS-PHPZ-BS",
                        Boolean.TRUE, "Human Development and Family Studies",
                        "Pre-Health Professions",
                        MathPlanConstants.CAT + "health-human-sciences/human-development-family-studies/"
                                + "human-development-family-studies-major-pre-health-professions-concentration/");
                final MajorMathRequirement rHDFSPHPZ =
                        new MajorMathRequirement("HDFS-PHPZ-BS")
                                .setSemesterCourses(MathPlanConstants.AUCC3, null, null);
                map.put(mHDFSPHPZ, rHDFSPHPZ);

                final Major mHDFSPISZ = new Major(4075, "HDFS-PISZ-BS",
                        Boolean.TRUE, "Human Development and Family Studies",
                        "Prevention and Intervention Sciences",
                        MathPlanConstants.CAT + "health-human-sciences/human-development-family-studies/"
                                + "human-development-family-studies-major-prevention-"
                                + "intervention-sciences-concentration/");
                final MajorMathRequirement rHDFSPISZ =
                        new MajorMathRequirement("HDFS-PISZ-BS")
                                .setSemesterCourses(MathPlanConstants.AUCC3, null, null);
                map.put(mHDFSPISZ, rHDFSPISZ);

                // *** Major in Interior Architecture and Design

                // DEACTIVATED (commented to preserve what the historical number represents):
                // 4080: INTD-BS, Interior Architecture and Design

                final Major mIARD = new Major(4081, "IARD-BS", Boolean.TRUE,
                        "Interior Architecture and Design",
                        MathPlanConstants.CAT + "health-human-sciences/design-merchandising/interior-architecture" +
                                "-design-major/");
                final MajorMathRequirement rIARD = new MajorMathRequirement("IARD-BS")
                        .setSemesterCourses("M 117.,M 118.", "M 124.", null);
                map.put(mIARD, rIARD);

                // *** Major in Nutrition and Food Science (with five concentrations)

                final Major mNAFS = new Major(4090, "NAFS-BS", Boolean.FALSE,
                        "Nutrition and Food Science",
                        MathPlanConstants.CAT + "health-human-sciences/food-science-human-nutrition/nutrition-food" +
                                "-science-major/");
                final MajorMathRequirement rNAFS = new MajorMathRequirement("NAFS-BS")
                        .setSemesterCourses("M 117!,M 118!,M 124!", null, null);
                map.put(mNAFS, rNAFS);

                // DEACTIVATED (commented to preserve what the historical number represents):

                final Major mNAFSDNMZ = new Major(4091, "NAFS-DNMZ-BS",
                        Boolean.TRUE, "Nutrition and Food Science",
                        "Dietetics and Nutrition Management",
                        MathPlanConstants.CAT + "health-human-sciences/food-science-human-nutrition/"
                                + "nutrition-food-science-major-dietetics-management-concentration/");
                final MajorMathRequirement rNAFSDNMZ =
                        new MajorMathRequirement("NAFS-DNMZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124!", null, null);
                map.put(mNAFSDNMZ, rNAFSDNMZ);

                // DEACTIVATED (commented to preserve what the historical number represents):
                // 4092: NAFS-FSNZ-BS, Nutrition and Food Science/Food Safety and Nutrition

                // DEACTIVATED (commented to preserve what the historical number represents):
                // 4095: NAFS-FSYZ-BS, Nutrition and Food Science/Food Systems

                final Major mNAFSFSCZ = new Major(4096, "NAFS-FSCZ-BS",
                        Boolean.TRUE, "Nutrition and Food Science",
                        "Food Science",
                        MathPlanConstants.CAT + "health-human-sciences/food-science-human-nutrition/"
                                + "nutrition-food-science-major-food-science-concentration/");
                final MajorMathRequirement rNAFSFSCZ =
                        new MajorMathRequirement("NAFS-FSCZ-BS")
                                .setSemesterCourses("M 117!,M 118!", "M 124!", null);
                map.put(mNAFSFSCZ, rNAFSFSCZ);

                final Major mNAFSNFTZ = new Major(4093, "NAFS-NFTZ-BS",
                        Boolean.TRUE, "Nutrition and Food Science",
                        "Nutrition and Fitness",
                        MathPlanConstants.CAT + "health-human-sciences/food-science-human-nutrition/"
                                + "nutrition-food-science-major-fitness-concentration/");
                final MajorMathRequirement rNAFSNFTZ =
                        new MajorMathRequirement("NAFS-NFTZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 125", null, null);
                map.put(mNAFSNFTZ, rNAFSNFTZ);

                // DEACTIVATED (commented to preserve what the historical number represents):
                // 4094: NAFS-NUSZ-BS, Nutrition and Food Science/Nutritional Sciences

                final Major mNAFSPHNZ = new Major(4097, "NAFS-PHNZ-BS",
                        Boolean.TRUE, "Nutrition and Food Science",
                        "Pre-Health Nutrition",
                        MathPlanConstants.CAT + "health-human-sciences/food-science-human-nutrition/"
                                + "nutrition-food-science-major-pre-health-nutrition-concentration/");
                final MajorMathRequirement rNAFSPHNZ =
                        new MajorMathRequirement("NAFS-PHNZ-BS")
                                .setSemesterCourses("M 117!,M 118!", "M 124!,M 125!", MathPlanConstants.M_155);
                map.put(mNAFSPHNZ, rNAFSPHNZ);

                // *** Major in Social Work

                final Major mSOWK = new Major(4100, "SOWK-BSW", Boolean.TRUE,
                        "Social Work",
                        MathPlanConstants.CAT + "health-human-sciences/school-of-social-work/social-work-major/");
                final MajorMathRequirement rSOWK =
                        new MajorMathRequirement("SOWK-BSW")
                                .setSemesterCourses("AUCC3!", null, null);
                map.put(mSOWK, rSOWK);

                final Major mSOWKADSZ = new Major(4101, "SOWK-ADSZ-BSW",
                        Boolean.TRUE, "Social Work",
                        "Addictions Counseling",
                        MathPlanConstants.CAT + "health-human-sciences/school-of-social-work/"
                                + "social-work-major/addictions-counseling-concentration/");
                final MajorMathRequirement rSOWKADSZ =
                        new MajorMathRequirement("SOWK-ADSZ-BSW")
                                .setSemesterCourses("AUCC3!", null, null);
                map.put(mSOWKADSZ, rSOWKADSZ);

                // =======================
                // College of Liberal Arts
                // =======================

                // *** Major in Anthropology (with three concentrations)

                final Major mANTH = new Major(5000, "ANTH-BA", Boolean.TRUE,
                        "Anthropology",
                        MathPlanConstants.CAT + "liberal-arts/anthropology-geography/anthropology-major/");
                final MajorMathRequirement rANTH = new MajorMathRequirement("ANTH-BA")
                        .setSemesterCourses("AUCC3.", null, null);
                map.put(mANTH, rANTH);

                final Major mANTHARCZ = new Major(5001, "ANTH-ARCZ-BA",
                        Boolean.TRUE, "Anthropology", "Archaeology",
                        MathPlanConstants.CAT + "liberal-arts/anthropology-geography/anthropology-major-archaeology" +
                                "-concentration/");
                final MajorMathRequirement rANTHARCZ =
                        new MajorMathRequirement("ANTH-ARCZ-BA")
                                .setSemesterCourses("AUCC3.", null, null);
                map.put(mANTHARCZ, rANTHARCZ);

                final Major mANTHBIOZ = new Major(5002, "ANTH-BIOZ-BA",
                        Boolean.TRUE, "Anthropology",
                        "Biological Anthropology",
                        MathPlanConstants.CAT + "liberal-arts/anthropology-geography/"
                                + "major-anthropology-biological-anthropology-concentration/");
                final MajorMathRequirement rANTHBIOZ =
                        new MajorMathRequirement("ANTH-BIOZ-BA")
                                .setSemesterCourses("AUCC3.", null, null);
                map.put(mANTHBIOZ, rANTHBIOZ);

                final Major mANTHCLTZ = new Major(5003, "ANTH-CLTZ-BA",
                        Boolean.TRUE, "Anthropology",
                        "Cultural Anthropology",
                        MathPlanConstants.CAT + "liberal-arts/anthropology-geography/"
                                + "anthropology-major-cultural-anthropology-concentration/");
                final MajorMathRequirement rANTHCLTZ =
                        new MajorMathRequirement("ANTH-CLTZ-BA")
                                .setSemesterCourses("AUCC3.", null, null);
                map.put(mANTHCLTZ, rANTHCLTZ);

                // *** Major in Art, B.A. (with two concentrations)

                final Major mARTI = new Major(5010, "ARTI-BA", Boolean.FALSE,
                        "Art, B.A.",
                        MathPlanConstants.CAT + "liberal-arts/art-history/art-major-ba/");
                final MajorMathRequirement rARTI = new MajorMathRequirement("ARTI-BA")
                        .setSemesterCourses(null, MathPlanConstants.AUCC3, null);
                map.put(mARTI, rARTI);

                final Major mARTIARTZ = new Major(5012, "ARTI-ARTZ-BA",
                        Boolean.TRUE, "Art, B.A.",
                        "Art History",
                        MathPlanConstants.CAT + "liberal-arts/art-history/art-major-ba/art-major-art-history/");
                final MajorMathRequirement rARTMARTZ =
                        new MajorMathRequirement("ARTI-ARTZ-BA")
                                .setSemesterCourses(null, MathPlanConstants.AUCC3, null);
                map.put(mARTIARTZ, rARTMARTZ);

                final Major mARTIIVSZ = new Major(5013, "ARTI-IVSZ-BA",
                        Boolean.TRUE, "Art, B.A.",
                        "Integrated Visual Studies",
                        MathPlanConstants.CAT + "liberal-arts/art-history/art-major-ba/art-major-integrated-visual" +
                                "-studies/");
                final MajorMathRequirement rARTMIVSZ =
                        new MajorMathRequirement("ARTI-IVSZ-BA")
                                .setSemesterCourses(null, "AUCC3.", null);
                map.put(mARTIIVSZ, rARTMIVSZ);

                // *** Major in Art, B.F.A. (with eleven concentrations)

                final Major mARTM = new Major(5020, "ARTM-BFA", Boolean.FALSE,
                        "Art, B.F.A.",
                        MathPlanConstants.CAT + "liberal-arts/art-history/art-major-bfa/");
                final MajorMathRequirement rARTM =
                        new MajorMathRequirement("ARTM-BFA")
                                .setSemesterCourses(null, MathPlanConstants.AUCC3, null);
                map.put(mARTM, rARTM);

                final Major mARTMDRAZ = new Major(5021, "ARTM-DRAZ-BF",
                        Boolean.TRUE, "Art, B.F.A.",
                        "Drawing",
                        MathPlanConstants.CAT + "liberal-arts/art-history/art-major-drawing-concentration/");
                final MajorMathRequirement rARTMDRAZ =
                        new MajorMathRequirement("ARTM-DRAZ-BF")
                                .setSemesterCourses(null, MathPlanConstants.AUCC3, null);
                map.put(mARTMDRAZ, rARTMDRAZ);

                final Major mARTMELAZ = new Major(5022, "ARTM-ELAZ-BF",
                        Boolean.TRUE, "Art, B.F.A.",
                        "Electronic Art",
                        MathPlanConstants.CAT + "liberal-arts/art-history/art-major-electronic-concentration/");
                final MajorMathRequirement rARTMELAZ =
                        new MajorMathRequirement("ARTM-ELAZ-BF")
                                .setSemesterCourses(null, MathPlanConstants.AUCC3, null);
                map.put(mARTMELAZ, rARTMELAZ);

                final Major mARTMFIBZ = new Major(5023, "ARTM-FIBZ-BF",
                        Boolean.TRUE, "Art, B.F.A.",
                        "Fibers",
                        MathPlanConstants.CAT + "liberal-arts/art-history/art-major-fibers-concentration/");
                final MajorMathRequirement rARTMFIBZ =
                        new MajorMathRequirement("ARTM-FIBZ-BF")
                                .setSemesterCourses(null, MathPlanConstants.AUCC3, null);
                map.put(mARTMFIBZ, rARTMFIBZ);

                final Major mARTMGRDZ = new Major(5024, "ARTM-GRDZ-BF",
                        Boolean.TRUE, "Art, B.F.A.",
                        "Graphic Design",
                        MathPlanConstants.CAT + "liberal-arts/art-history/art-major-graphic-design-concentration/");
                final MajorMathRequirement rARTMGRDZ =
                        new MajorMathRequirement("ARTM-GRDZ-BF")
                                .setSemesterCourses(null, MathPlanConstants.AUCC3, null);
                map.put(mARTMGRDZ, rARTMGRDZ);

                final Major mARTMMETZ = new Major(5025, "ARTM-METZ-BF",
                        Boolean.TRUE, "Art, B.F.A.",
                        "Metalsmithing",
                        MathPlanConstants.CAT + "liberal-arts/art-history/art-major-metalsmithing-concentration/");
                final MajorMathRequirement rARTMMETZ =
                        new MajorMathRequirement("ARTM-METZ-BF")
                                .setSemesterCourses(null, MathPlanConstants.AUCC3, null);
                map.put(mARTMMETZ, rARTMMETZ);

                final Major mARTMPNTZ = new Major(5027, "ARTM-PNTZ-BF",
                        Boolean.TRUE, "Art, B.F.A.",
                        "Painting",
                        MathPlanConstants.CAT + "liberal-arts/art-history/art-major-painting-concentration/");
                final MajorMathRequirement rARTMPNTZ =
                        new MajorMathRequirement("ARTM-PNTZ-BF")
                                .setSemesterCourses(null, MathPlanConstants.AUCC3, null);
                map.put(mARTMPNTZ, rARTMPNTZ);

                final Major mARTMPHIZ = new Major(5026, "ARTM-PHIZ-BF",
                        Boolean.TRUE, "Art, B.F.A.",
                        "Photo Image Making",
                        MathPlanConstants.CAT + "liberal-arts/art-history/art-major-photo-image-making-concentration/");
                final MajorMathRequirement rARTMPHIZ =
                        new MajorMathRequirement("ARTM-PHIZ-BF")
                                .setSemesterCourses(null, MathPlanConstants.AUCC3, null);
                map.put(mARTMPHIZ, rARTMPHIZ);

                final Major mARTMPOTZ = new Major(5028, "ARTM-POTZ-BF",
                        Boolean.TRUE, "Art, B.F.A.",
                        "Pottery",
                        MathPlanConstants.CAT + "liberal-arts/art-history/art-major-pottery-concentration/");
                final MajorMathRequirement rARTMPOTZ =
                        new MajorMathRequirement("ARTM-POTZ-BF")
                                .setSemesterCourses(null, MathPlanConstants.AUCC3, null);
                map.put(mARTMPOTZ, rARTMPOTZ);

                final Major mARTMPRTZ = new Major(5029, "ARTM-PRTZ-BF",
                        Boolean.TRUE, "Art, B.F.A.",
                        "Printmaking",
                        MathPlanConstants.CAT + "liberal-arts/art-history/art-major-printmaking-concentration/");
                final MajorMathRequirement rARTMPRTZ =
                        new MajorMathRequirement("ARTM-PRTZ-BF")
                                .setSemesterCourses(null, MathPlanConstants.AUCC3, null);
                map.put(mARTMPRTZ, rARTMPRTZ);

                final Major mARTMSCLZ = new Major(5030, "ARTM-SCLZ-BF",
                        Boolean.TRUE, "Art, B.F.A.",
                        "Sculpture",
                        MathPlanConstants.CAT + "liberal-arts/art-history/art-major-sculpture-concentration/");
                final MajorMathRequirement rARTMSCLZ =
                        new MajorMathRequirement("ARTM-SCLZ-BF")
                                .setSemesterCourses(null, MathPlanConstants.AUCC3, null);
                map.put(mARTMSCLZ, rARTMSCLZ);

                final Major mARTIAREZ = new Major(5031, "ARTM-AREZ-BF",
                        Boolean.TRUE, "Art, B.F.A.",
                        "Art Education",
                        MathPlanConstants.CAT + "liberal-arts/art-history/art-major-art-education-concentration/");
                final MajorMathRequirement rARTMAREZ =
                        new MajorMathRequirement("ARTM-AREZ-BA")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mARTIAREZ, rARTMAREZ);

                // *** Major in Communication Studies (with one concentration)

                final Major mCMST = new Major(5040, "CMST-BA", Boolean.TRUE,
                        "Communication Studies",
                        MathPlanConstants.CAT + "liberal-arts/communication-studies/communication-studies-major/");
                final MajorMathRequirement rCMST = new MajorMathRequirement("CMST-BA")
                        .setSemesterCourses(null, "AUCC3!", null);
                map.put(mCMST, rCMST);

                final Major mCMSTTCLZ = new Major(5041, "CMST-TCLZ-BA",
                        Boolean.TRUE, "Communication Studies",
                        "Speech Teacher Licensure",
                        MathPlanConstants.CAT + "liberal-arts/communication-studies/communication-studies-"
                                + "major-speech-teacher-licensure-concentration/");
                final MajorMathRequirement rCMSTTCLZ =
                        new MajorMathRequirement("CMST-TCLZ-BA")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mCMSTTCLZ, rCMSTTCLZ);

                // *** Major in Dance (BA)

                final Major mDNCE = new Major(5050, "DNCE-BA", Boolean.TRUE,
                        "Dance",
                        MathPlanConstants.CAT + "liberal-arts/music-theatre-dance/dance/dance-ba/");
                final MajorMathRequirement rDNCE = new MajorMathRequirement("DNCE-BA")
                        .setSemesterCourses("AUCC3.", null, null);
                map.put(mDNCE, rDNCE);

                // *** Major in Dance (BFA)

                final Major mDANC = new Major(5050, "DANC-BFA", Boolean.TRUE,
                        "Dance",
                        MathPlanConstants.CAT + "liberal-arts/music-theatre-dance/dance/dance-bfa/");
                final MajorMathRequirement rDANC = new MajorMathRequirement("DANC-BFA")
                        .setSemesterCourses(null, "AUCC3!", null);
                map.put(mDANC, rDANC);

                // *** Major in Economics

                final Major mECON = new Major(5060, "ECON-BA", Boolean.TRUE,
                        "Economics",
                        MathPlanConstants.CAT + "liberal-arts/economics/economics-major/");
                final MajorMathRequirement rECON = new MajorMathRequirement("ECON-BA")
                        .setSemesterCourses(null, "CALC!", null);
                map.put(mECON, rECON);

                // *** Major in English (with five concentrations)

                final Major mENGL = new Major(5070, "ENGL-BA", Boolean.FALSE,
                        "English",
                        MathPlanConstants.CAT + "liberal-arts/english/english-major/");
                final MajorMathRequirement rENGL = new MajorMathRequirement("ENGL-BA")
                        .setSemesterCourses("AUCC3.", null, null);
                map.put(mENGL, rENGL);

                final Major mENGLCRWZ = new Major(5071, "ENGL-CRWZ-BA",
                        Boolean.TRUE, "English",
                        "Creative Writing",
                        MathPlanConstants.CAT + "liberal-arts/english/english-major-creative-writing-concentration/");
                final MajorMathRequirement rENGLCRWZ =
                        new MajorMathRequirement("ENGL-CRWZ-BA")
                                .setSemesterCourses("AUCC3.", null, null);
                map.put(mENGLCRWZ, rENGLCRWZ);

                final Major mENGLENEZ = new Major(5072, "ENGL-ENEZ-BA",
                        Boolean.TRUE, "English",
                        "English Education",
                        MathPlanConstants.CAT + "liberal-arts/english/english-major-education-concentration/");
                final MajorMathRequirement rENGLENEZ =
                        new MajorMathRequirement("ENGL-ENEZ-BA")
                                .setSemesterCourses("AUCC3.", null, null);
                map.put(mENGLENEZ, rENGLENEZ);

                // DEACTIVATED (commented to preserve what the historical number represents):
                // 5073: ENGL-LANZ-BA, English/Language

                final Major mENGLLINZ = new Major(5076, "ENGL-LINZ-BA",
                        Boolean.TRUE, "English",
                        "Linguistics",
                        MathPlanConstants.CAT + "liberal-arts/english/english-major-linguistics-concentration/");
                final MajorMathRequirement rENGLLINZ =
                        new MajorMathRequirement("ENGL-LINZ-BA")
                                .setSemesterCourses("AUCC3.", null, null);
                map.put(mENGLLINZ, rENGLLINZ);

                final Major mENGLLITZ = new Major(5074, "ENGL-LITZ-BA",
                        Boolean.TRUE, "English",
                        "Literature",
                        MathPlanConstants.CAT + "liberal-arts/english/english-major-literature-concentration/");
                final MajorMathRequirement rENGLLITZ =
                        new MajorMathRequirement("ENGL-LITZ-BA")
                                .setSemesterCourses("AUCC3.", null, null);
                map.put(mENGLLITZ, rENGLLITZ);

                final Major mENGLWRLZ = new Major(5075, "ENGL-WRLZ-BA",
                        Boolean.TRUE, "English",
                        "Writing, Rhetoric and Literacy",
                        MathPlanConstants.CAT + "liberal-arts/english/english-major-writing-rhetoric-literacy" +
                                "-concentration/");
                final MajorMathRequirement rENGLWRLZ =
                        new MajorMathRequirement("ENGL-WRLZ-BA")
                                .setSemesterCourses("AUCC3.", null, null);
                map.put(mENGLWRLZ, rENGLWRLZ);

                // *** Major in Ethnic Studies (with one concentration)

                final Major mETST = new Major(5080, "ETST-BA", Boolean.TRUE,
                        "Ethnic Studies",
                        MathPlanConstants.CAT + "liberal-arts/ethnic-studies/ethnic-studies-major/");
                final MajorMathRequirement rETST = new MajorMathRequirement("ETST-BA")
                        .setSemesterCourses("AUCC3.", null, null);
                map.put(mETST, rETST);

                final Major mETSTCOIZ = new Major(5082, "ETST-COIZ-BA",
                        Boolean.TRUE, "Ethnic Studies",
                        "Community Organizing and Institutional Change",
                        MathPlanConstants.CAT + "liberal-arts/ethnic-studies/"
                                + "ethnic-studies-major-community-organizing-institutional-change-concentration/");
                final MajorMathRequirement rETSTCOIZ =
                        new MajorMathRequirement("ETST-COIZ-BA")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mETSTCOIZ, rETSTCOIZ);

                final Major mETSTRPRZ = new Major(5083, "ETST-RPRZ-BA",
                        Boolean.TRUE, "Ethnic Studies",
                        "Global Race, Power, and Resistance",
                        MathPlanConstants.CAT + "liberal-arts/ethnic-studies/"
                                + "ethnic-studies-major-global-race-power-resistance-concentration/");
                final MajorMathRequirement rETSTRPRZ =
                        new MajorMathRequirement("ETST-RPRZ-BA")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mETSTRPRZ, rETSTRPRZ);

                final Major mETSTSOTZ = new Major(5081, "ETST-SOTZ-BA",
                        Boolean.TRUE, "Ethnic Studies",
                        "Social Studies Teaching",
                        MathPlanConstants.CAT + "liberal-arts/ethnic-studies/"
                                + "ethnic-studies-major-social-studies-teaching-concentration/");
                final MajorMathRequirement rETSTSOTZ =
                        new MajorMathRequirement("ETST-SOTZ-BA")
                                .setSemesterCourses(MathPlanConstants.AUCC3, null, null);
                map.put(mETSTSOTZ, rETSTSOTZ);

                // *** Major in Geography

                final Major mGEOG = new Major(5085, "GEOG-BS", Boolean.TRUE,
                        "Geography",
                        MathPlanConstants.CAT + "liberal-arts/anthropology-geography/geography-major/");
                final MajorMathRequirement rGEOG = new MajorMathRequirement("GEOG-BS")
                        .setSemesterCourses(MathPlanConstants.AUCC3, null, null);
                map.put(mGEOG, rGEOG);

                // *** Major in History (with five concentrations)

                final Major mHIST = new Major(5090, "HIST-BA", Boolean.FALSE,
                        "History",
                        MathPlanConstants.CAT + "liberal-arts/history/history-major/");
                final MajorMathRequirement rHIST = new MajorMathRequirement("HIST-BA")
                        .setSemesterCourses(MathPlanConstants.AUCC3, null, null);
                map.put(mHIST, rHIST);

                final Major mHISTGENZ = new Major(5091, "HIST-GENZ-BA",
                        Boolean.TRUE, "History",
                        "General History",
                        MathPlanConstants.CAT + "liberal-arts/history/history-major-general-concentration/");
                final MajorMathRequirement rHISTGENZ =
                        new MajorMathRequirement("HIST-GENZ-BA")
                                .setSemesterCourses(MathPlanConstants.AUCC3, null, null);
                map.put(mHISTGENZ, rHISTGENZ);

                final Major mHISTLNGZ = new Major(5092, "HIST-LNGZ-BA",
                        Boolean.TRUE, "History",
                        "Language",
                        MathPlanConstants.CAT + "liberal-arts/history/history-major-language-concentration/");
                final MajorMathRequirement rHISTLNGZ =
                        new MajorMathRequirement("HIST-LNGZ-BA")
                                .setSemesterCourses(null, MathPlanConstants.AUCC3, null);
                map.put(mHISTLNGZ, rHISTLNGZ);

                final Major mHISTSBSZ = new Major(5093, "HIST-SBSZ-BA",
                        Boolean.TRUE, "History",
                        "Social and Behavioral Sciences",
                        MathPlanConstants.CAT + "liberal-arts/history/history-major-social-behavioral-sciences" +
                                "-concentration/");
                final MajorMathRequirement rHISTSBSZ =
                        new MajorMathRequirement("HIST-SBSZ-BA")
                                .setSemesterCourses("AUCC3!", null, null);
                map.put(mHISTSBSZ, rHISTSBSZ);

                final Major mHISTSSTZ = new Major(5094, "HIST-SSTZ-BA",
                        Boolean.TRUE, "History",
                        "Social Studies Teaching",
                        MathPlanConstants.CAT + "liberal-arts/history/history-major-social-studies-teaching" +
                                "-concentration/");
                final MajorMathRequirement rHISTSSTZ =
                        new MajorMathRequirement("HIST-SSTZ-BA")
                                .setSemesterCourses(MathPlanConstants.AUCC3, null, null);
                map.put(mHISTSSTZ, rHISTSSTZ);

                final Major mHISTDPUZ = new Major(5095, "HIST-DPUZ-BA",
                        Boolean.TRUE, "History",
                        "Digital and Public History",
                        MathPlanConstants.CAT + "liberal-arts/history/history-major-digital-public-history" +
                                "-concentration/");
                final MajorMathRequirement rHISTDPUZ =
                        new MajorMathRequirement("HIST-DPUZ-BA")
                                .setSemesterCourses("AUCC3!", null, null);
                map.put(mHISTDPUZ, rHISTDPUZ);

                // *** Major in Journalism and Media Communication

                final Major mJAMC = new Major(5100, "JAMC-BA", Boolean.TRUE,
                        "Journalism and Media Communication",
                        MathPlanConstants.CAT + "liberal-arts/journalism-media-communication/media-major/");
                final MajorMathRequirement rJAMC = new MajorMathRequirement("JAMC-BA")
                        .setSemesterCourses(null, "AUCC3!", null);
                map.put(mJAMC, rJAMC);

                // *** Major in Languages, Literatures and Cultures (with four concentrations)

                final Major mLLAC = new Major(5110, "LLAC-BA", Boolean.FALSE,
                        "Languages, Literatures and Cultures",
                        MathPlanConstants.CAT + "liberal-arts/foreign-languages-literatures/languages-literature" +
                                "-cultures-major/");
                final MajorMathRequirement rLLAC = new MajorMathRequirement("LLAC-BA")
                        .setSemesterCourses(null, null, MathPlanConstants.AUCC3);
                map.put(mLLAC, rLLAC);

                final Major mLLACLFRZ = new Major(5111, "LLAC-LFRZ-BA",
                        Boolean.FALSE, "Languages, Literatures and Cultures",
                        "French",
                        MathPlanConstants.CAT + "liberal-arts/foreign-languages-literatures/"
                                + "languages-literature-cultures-major-french-concentration/");
                final MajorMathRequirement rLLACLFRZ =
                        new MajorMathRequirement("LLAC-LFRZ-BA")
                                .setSemesterCourses(null, null, MathPlanConstants.AUCC3);
                map.put(mLLACLFRZ, rLLACLFRZ);

                final Major mLLACLGEZ = new Major(5112, "LLAC-LGEZ-BA",
                        Boolean.FALSE, "Languages, Literatures and Cultures",
                        "German",
                        MathPlanConstants.CAT + "liberal-arts/foreign-languages-literatures/"
                                + "languages-literature-cultures-major-german-concentration/");
                final MajorMathRequirement rLLACLGEZ =
                        new MajorMathRequirement("LLAC-LGEZ-BA")
                                .setSemesterCourses(null, null, MathPlanConstants.AUCC3);
                map.put(mLLACLGEZ, rLLACLGEZ);

                final Major mLLACLSPZ = new Major(5113, "LLAC-LSPZ-BA",
                        Boolean.FALSE, "Languages, Literatures and Cultures",
                        "Spanish",
                        MathPlanConstants.CAT + "liberal-arts/foreign-languages-literatures/"
                                + "languages-literature-cultures-major-spanish-concentration/");
                final MajorMathRequirement rLLACLSPZ =
                        new MajorMathRequirement("LLAC-LSPZ-BA")
                                .setSemesterCourses(null, null, MathPlanConstants.AUCC3);
                map.put(mLLACLSPZ, rLLACLSPZ);

                final Major mLLACSPPZ = new Major(5114, "LLAC-SPPZ-BA",
                        Boolean.FALSE, "Languages, Literatures and Cultures",
                        "Spanish for the Professions",
                        MathPlanConstants.CAT + "liberal-arts/foreign-languages-literatures/"
                                + "languages-literature-cultures-major-spanish-for-professions-concentration/");
                final MajorMathRequirement rLLACSPPZ =
                        new MajorMathRequirement("LLAC-SPPZ-BA")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mLLACSPPZ, rLLACSPPZ);

                // *** Major in Music, B.A.

                final Major mMUSI = new Major(5120, "MUSI-BA", Boolean.TRUE,
                        "Music (B.A.)",
                        MathPlanConstants.CAT + "liberal-arts/music-theatre-dance/music-ba/");
                final MajorMathRequirement rMUSI = new MajorMathRequirement("MUSI-BA")
                        .setSemesterCourses("AUCC3!", null, null);
                map.put(mMUSI, rMUSI);

                // *** Major in Music, B.M. (with four concentrations)

                final Major mMUSC = new Major(5130, "MUSC-BM", Boolean.FALSE,
                        "Music (B.M.)",
                        MathPlanConstants.CAT + "liberal-arts/music-theatre-dance/music-bm/");
                final MajorMathRequirement rMUSC = new MajorMathRequirement("MUSC-BM")
                        .setSemesterCourses(null, "AUCC3!", null);
                map.put(mMUSC, rMUSC);

                final Major mMUSCCOMZ = new Major(5131, "MUSC-COMZ-BM",
                        Boolean.TRUE, "Music (B.M.)", "Composition",
                        MathPlanConstants.CAT + "liberal-arts/music-theatre-dance/music-bm-composition-concentration/");
                final MajorMathRequirement rMUSCCOMZ =
                        new MajorMathRequirement("MUSC-COMZ-BM")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mMUSCCOMZ, rMUSCCOMZ);

                final Major mMUSCMUEZ = new Major(5132, "MUSC-MUEZ-BM",
                        Boolean.TRUE, "Music (B.M.)", "Music Education",
                        MathPlanConstants.CAT + "liberal-arts/music-theatre-dance/music-bm-education-concentration/");
                final MajorMathRequirement rMUSCMUEZ =
                        new MajorMathRequirement("MUSC-MUEZ-BM")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mMUSCMUEZ, rMUSCMUEZ);

                final Major mMUSCMUTZ = new Major(5133, "MUSC-MUTZ-BM",
                        Boolean.TRUE, "Music (B.M.)", "Music Therapy",
                        MathPlanConstants.CAT + "liberal-arts/music-theatre-dance/music-bm-therapy-concentration/");
                final MajorMathRequirement rMUSCMUTZ =
                        new MajorMathRequirement("MUSC-MUTZ-BM")
                                .setSemesterCourses(MathPlanConstants.S_100, null, null);
                map.put(mMUSCMUTZ, rMUSCMUTZ);

                final Major mMUSCPERZ = new Major(5134, "MUSC-PERZ-BM",
                        Boolean.TRUE, "Music (B.M.)", "Performance",
                        MathPlanConstants.CAT + "liberal-arts/music-theatre-dance/music-bm-performance-concentration/");
                final MajorMathRequirement rMUSCPERZ =
                        new MajorMathRequirement("MUSC-PERZ-BM")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mMUSCPERZ, rMUSCPERZ);

                // *** Major in Philosophy (with three concentrations)

                final Major mPHIL = new Major(5140, "PHIL-BA", Boolean.FALSE,
                        "Philosophy",
                        MathPlanConstants.CAT + "liberal-arts/philosophy/philosophy-major/");
                final MajorMathRequirement rPHIL = new MajorMathRequirement("PHIL-BA")
                        .setSemesterCourses(MathPlanConstants.AUCC3, null, null);
                map.put(mPHIL, rPHIL);

                final Major mPHILGNPZ = new Major(5141, "PHIL-GNPZ-BA",
                        Boolean.TRUE, "Philosophy", "General Philosophy",
                        MathPlanConstants.CAT + "liberal-arts/philosophy/philosophy-major-general-concentration/");
                final MajorMathRequirement rPHILGNPZ =
                        new MajorMathRequirement("PHIL-GNPZ-BA")
                                .setSemesterCourses(MathPlanConstants.AUCC3, null, null);
                map.put(mPHILGNPZ, rPHILGNPZ);

                final Major mPHILGPRZ = new Major(5142, "PHIL-GPRZ-BA",
                        Boolean.TRUE, "Philosophy",
                        "Global Philosophies and Religions",
                        MathPlanConstants.CAT + "liberal-arts/philosophy/philosophy-major-global-philosophies" +
                                "-religions-concentration/");
                final MajorMathRequirement rPHILGPRZ =
                        new MajorMathRequirement("PHIL-GPRZ-BA")
                                .setSemesterCourses(MathPlanConstants.AUCC3, null, null);
                map.put(mPHILGPRZ, rPHILGPRZ);

                final Major mPHILPSAZ = new Major(5143, "PHIL-PSAZ-BA",
                        Boolean.TRUE, "Philosophy",
                        "Philosophy, Science, and Technology",
                        MathPlanConstants.CAT + "liberal-arts/philosophy/philosophy-major-science-technology" +
                                "-concentration/");
                final MajorMathRequirement rPHILPSAZ =
                        new MajorMathRequirement("PHIL-PSAZ-BA")
                                .setSemesterCourses(MathPlanConstants.AUCC3, null, null);
                map.put(mPHILPSAZ, rPHILPSAZ);

                // *** Major in Political Science (with three concentrations)

                final Major mPOLS = new Major(5150, "POLS-BA", Boolean.TRUE,
                        "Political Science",
                        MathPlanConstants.CAT + "liberal-arts/political-science/political-science-major/");
                final MajorMathRequirement rPOLS = new MajorMathRequirement("POLS-BA")
                        .setSemesterCourses(null, "AUCC3!", null);
                map.put(mPOLS, rPOLS);

                final Major mPOLSEPAZ = new Major(5151, "POLS-EPAZ-BA",
                        Boolean.TRUE, "Political Science",
                        "Environmental Politics and Policy",
                        MathPlanConstants.CAT + "liberal-arts/political-science/political-science-major-"
                                + "environmental-politics-policy-concentration/");
                final MajorMathRequirement rPOLSEPAZ =
                        new MajorMathRequirement("POLS-EPAZ-BA")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mPOLSEPAZ, rPOLSEPAZ);

                final Major mPOLSGPPZ = new Major(5152, "POLS-GPPZ-BA",
                        Boolean.TRUE, "Political Science",
                        "Global Politics and Policy",
                        MathPlanConstants.CAT + "liberal-arts/political-science/political-science-major-"
                                + "global-politics-policy-concentration/");
                final MajorMathRequirement rPOLSGPPZ =
                        new MajorMathRequirement("POLS-GPPZ-BA")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mPOLSGPPZ, rPOLSGPPZ);

                final Major mPOLSULPZ = new Major(5153, "POLS-ULPZ-BA",
                        Boolean.TRUE, "Political Science",
                        "U.S. Government, Law, and Policy",
                        MathPlanConstants.CAT + "liberal-arts/political-science/political-science-major-"
                                + "us-government-law-policy-concentration/");
                final MajorMathRequirement rPOLSULPZ =
                        new MajorMathRequirement("POLS-ULPZ-BA")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mPOLSULPZ, rPOLSULPZ);

                // *** Major in Sociology (with three concentrations)

                final Major mSOCI = new Major(5160, "SOCI-BA", Boolean.FALSE,
                        "Sociology",
                        MathPlanConstants.CAT + "liberal-arts/sociology/sociology-major/");
                final MajorMathRequirement rSOCI = new MajorMathRequirement("SOCI-BA")
                        .setSemesterCourses(null, "AUCC3!", null);
                map.put(mSOCI, rSOCI);

                final Major mSOCICRCZ = new Major(5161, "SOCI-CRCZ-BA",
                        Boolean.TRUE, "Sociology",
                        "Criminology and Criminal Justice",
                        MathPlanConstants.CAT + "liberal-arts/sociology/sociology-major-criminology-criminal-justice" +
                                "-concentration/");
                final MajorMathRequirement rSOCICRCZ =
                        new MajorMathRequirement("SOCI-CRCZ-BA")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mSOCICRCZ, rSOCICRCZ);

                final Major mSOCIENSZ = new Major(5162, "SOCI-ENSZ-BA",
                        Boolean.TRUE, "Sociology",
                        "Environmental Sociology",
                        MathPlanConstants.CAT + "liberal-arts/sociology/sociology-major-environmental-concentration/");
                final MajorMathRequirement rSOCIENSZ =
                        new MajorMathRequirement("SOCI-ENSZ-BA")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mSOCIENSZ, rSOCIENSZ);

                final Major mSOCIGNSZ = new Major(5163, "SOCI-GNSZ-BA",
                        Boolean.TRUE, "Sociology", "General Sociology",
                        MathPlanConstants.CAT + "liberal-arts/sociology/sociology-major-general-concentration/");
                final MajorMathRequirement rSOCIGNSZ =
                        new MajorMathRequirement("SOCI-GNSZ-BA")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mSOCIGNSZ, rSOCIGNSZ);

                // *** Major in Theatre (with seven concentrations)

                final Major mTHTR = new Major(5170, "THTR-BA", Boolean.FALSE,
                        "Theatre",
                        MathPlanConstants.CAT + "liberal-arts/music-theatre-dance/theatre/");
                final MajorMathRequirement rTHTR = new MajorMathRequirement("THTR-BA")
                        .setSemesterCourses(null, "AUCC3!", null);
                map.put(mTHTR, rTHTR);

                // DEACTIVATED (commented to preserve what the historical number represents):
                // 5171: THTR-DTHZ-BA, Theatre/Design and Technology

                // DEACTIVATED (commented to preserve what the historical number represents):
                // 5172: THTR-GTRZ-BA, Theatre/General Theatre

                final Major mTHTRMUSZ = new Major(5175, "THTR-MUSZ-BA",
                        Boolean.TRUE, "Theatre",
                        "Musical Theatre",
                        MathPlanConstants.CAT + "liberal-arts/music-theatre-dance/theatre-musical-theatre" +
                                "-concentration/");
                final MajorMathRequirement rTHTRMUSZ =
                        new MajorMathRequirement("THTR-MUSZ-BA")
                                .setSemesterCourses(null, "AUCC3.", null);
                map.put(mTHTRMUSZ, rTHTRMUSZ);

                final Major mTHTRPRFZ = new Major(5173, "THTR-PRFZ-BA",
                        Boolean.TRUE, "Theatre",
                        "Performance",
                        MathPlanConstants.CAT + "liberal-arts/music-theatre-dance/theatre-performance-concentration/");
                final MajorMathRequirement rTHTRPRFZ =
                        new MajorMathRequirement("THTR-PRFZ-BA")
                                .setSemesterCourses(MathPlanConstants.AUCC3, null, null);
                map.put(mTHTRPRFZ, rTHTRPRFZ);

                final Major mTHTRCDTZ = new Major(5179, "THTR-CDTZ-BA",
                        Boolean.TRUE, "Theatre",
                        "Costume Design and Technology",
                        MathPlanConstants.CAT + "liberal-arts/music-theatre-dance/theatre-costume-design-technology" +
                                "-concentration/");
                final MajorMathRequirement rTHTRCDTZ =
                        new MajorMathRequirement("THTR-CDTZ-BA")
                                .setSemesterCourses(null, MathPlanConstants.AUCC3, null);
                map.put(mTHTRCDTZ, rTHTRCDTZ);

                final Major mTHTRLDTZ = new Major(5174, "THTR-LDTZ-BA",
                        Boolean.TRUE, "Theatre",
                        "Lighting Design and Technology",
                        MathPlanConstants.CAT + "liberal-arts/music-theatre-dance/theatre-lighting-design-technology" +
                                "-concentration/");
                final MajorMathRequirement rTHTRLDTZ =
                        new MajorMathRequirement("THTR-LDTZ-BA")
                                .setSemesterCourses(null, "AUCC3.", null);
                map.put(mTHTRLDTZ, rTHTRLDTZ);

                final Major mTHTRPDTZ = new Major(5176, "THTR-PDTZ-BA",
                        Boolean.TRUE, "Theatre",
                        "Projection Design and Technology",
                        MathPlanConstants.CAT + "liberal-arts/music-theatre-dance/theatre-projection-design" +
                                "-technology-concentration/");
                final MajorMathRequirement rTHTRPDTZ =
                        new MajorMathRequirement("THTR-PDTZ-BA")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mTHTRPDTZ, rTHTRPDTZ);

                final Major mTHTRSDSZ = new Major(5177, "THTR-SDSZ-BA",
                        Boolean.TRUE, "Theatre",
                        "Set Design",
                        MathPlanConstants.CAT + "liberal-arts/music-theatre-dance/theatre-set-design-technology" +
                                "-concentration/");
                final MajorMathRequirement rTHTRSDSZ =
                        new MajorMathRequirement("THTR-SDSZ-BA")
                                .setSemesterCourses("AUCC3!", null, null);
                map.put(mTHTRSDSZ, rTHTRSDSZ);

                final Major mTHTRSDTZ = new Major(5178, "THTR-SDTZ-BA",
                        Boolean.TRUE, "Theatre",
                        "Sound Design and Technology",
                        MathPlanConstants.CAT + "liberal-arts/music-theatre-dance/theatre-sound-design-technology" +
                                "-concentration/");
                final MajorMathRequirement rTHTRSDTZ =
                        new MajorMathRequirement("THTR-SDTZ-BA")
                                .setSemesterCourses(null, "AUCC3.", null);
                map.put(mTHTRSDTZ, rTHTRSDTZ);

                // *** Major in Women's and Gender Studies

                final Major mWGST = new Major(5180, "WGST-BA", Boolean.TRUE,
                        "Women's and Gender Studies",
                        MathPlanConstants.CAT + "liberal-arts/ethnic-studies/womens-gender-studies-major/");
                final MajorMathRequirement rWGST = new MajorMathRequirement("WGST-BA")
                        .setSemesterCourses(null, "AUCC3!", null);
                map.put(mWGST, rWGST);

                // *** Major in International Studies (with five concentrations)

                final Major mINST = new Major(5190, "INST-BA", Boolean.FALSE,
                        "International Studies",
                        MathPlanConstants.CAT + "liberal-arts/international-studies-major/");
                final MajorMathRequirement rINST = new MajorMathRequirement("INST-BA")
                        .setSemesterCourses(null, "AUCC3!", null);
                map.put(mINST, rINST);

                final Major mINSTASTZ = new Major(5191, "INST-ASTZ-BA",
                        Boolean.TRUE, "International Studies",
                        "Asian Studies",
                        MathPlanConstants.CAT + "liberal-arts/international-studies-major-asian-concentration/");
                final MajorMathRequirement rINSTASTZ =
                        new MajorMathRequirement("INST-ASTZ-BA")
                                .setSemesterCourses(null, MathPlanConstants.AUCC3, null);
                map.put(mINSTASTZ, rINSTASTZ);

                final Major mINSTEUSZ = new Major(5192, "INST-EUSZ-BA",
                        Boolean.TRUE, "International Studies",
                        "European Studies",
                        MathPlanConstants.CAT + "liberal-arts/international-studies-major-european-concentration/");
                final MajorMathRequirement rINSTEUSZ =
                        new MajorMathRequirement("INST-EUSZ-BA")
                                .setSemesterCourses(null, "AUCC3.", null);
                map.put(mINSTEUSZ, rINSTEUSZ);

                final Major mINSTGBLZ = new Major(5195, "INST-GBLZ-BA",
                        Boolean.TRUE, "International Studies",
                        "Global Studies",
                        MathPlanConstants.CAT + "liberal-arts/international-studies-major-global-studies" +
                                "-concentration/");
                final MajorMathRequirement rINSTGBLZ =
                        new MajorMathRequirement("INST-GBLZ-BA")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mINSTGBLZ, rINSTGBLZ);

                final Major mINSTLTSZ = new Major(5193, "INST-LTSZ-BA",
                        Boolean.TRUE, "International Studies",
                        "Latin American Studies",
                        MathPlanConstants.CAT + "liberal-arts/international-studies-major-latin-american" +
                                "-concentration/");
                final MajorMathRequirement rINSTLTSZ =
                        new MajorMathRequirement("INST-LTSZ-BA")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mINSTLTSZ, rINSTLTSZ);

                final Major mINSTMEAZ = new Major(5194, "INST-MEAZ-BA",
                        Boolean.TRUE, "International Studies",
                        "Middle East and North African Studies",
                        MathPlanConstants.CAT + "liberal-arts/international-studies-major-middle-east-north-african" +
                                "-concentration/");
                final MajorMathRequirement rINSTMEAZ =
                        new MajorMathRequirement("INST-MEAZ-BA")
                                .setSemesterCourses(null, "AUCC3!", null);
                map.put(mINSTMEAZ, rINSTMEAZ);

                // *** Major in Interdisciplinary Liberal Arts

                final Major mILAR = new Major(5200, "ILAR-BA", Boolean.TRUE,
                        "Interdisciplinary Liberal Arts",
                        MathPlanConstants.CAT + "liberal-arts/interdisciplinary-liberal-arts-major/");
                final MajorMathRequirement rILAR = new MajorMathRequirement("ILAR-BA")
                        .setSemesterCourses("AUCC3.", null, null);
                map.put(mILAR, rILAR);

                // ============================
                // College of Natural Resources
                // ============================

                // *** Major in Ecosystem Science and Sustainability

                final Major mECSS = new Major(6000, "ECSS-BS", Boolean.TRUE,
                        "Ecosystem Science and Sustainability",
                        MathPlanConstants.CAT + "natural-resources/ecosystem-science-sustainability/"
                                + "ecosystem-science-sustainability-major/");
                final MajorMathRequirement rECSS = new MajorMathRequirement("ECSS-BS")
                        .setSemesterCourses(null, MathPlanConstants.CALC1BIO, null);
                map.put(mECSS, rECSS);

                // *** Major in Fish, Wildlife and Conservation Biology (with three concentrations)

                final Major mFWCB = new Major(6010, "FWCB-BS", Boolean.FALSE,
                        "Fish, Wildlife and Conservation Biology",
                        MathPlanConstants.CAT + "natural-resources/fish-wildlife-conservation-biology/"
                                + "fish-wildlife-conservation-biology-major/");
                final MajorMathRequirement rFWCB = new MajorMathRequirement("FWCB-BS")
                        .setSemesterCourses("M 117!,M 118!,M 124!", "M 125!", MathPlanConstants.CALC1BIO);
                map.put(mFWCB, rFWCB);

                final Major mFWCBCNVZ = new Major(6011, "FWCB-CNVZ-BS",
                        Boolean.TRUE, "Fish, Wildlife and Conservation Biology",
                        "Conservation Biology",
                        MathPlanConstants.CAT + "natural-resources/fish-wildlife-conservation-biology/"
                                + "fish-wildlife-conservation-biology-major-conservation-concentration/");
                final MajorMathRequirement rFWCBCNVZ =
                        new MajorMathRequirement("FWCB-CNVZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124!", "M 125!", MathPlanConstants.CALC1BIO);
                map.put(mFWCBCNVZ, rFWCBCNVZ);

                final Major mFWCBFASZ = new Major(6012, "FWCB-FASZ-BS",
                        Boolean.TRUE, "Fish, Wildlife and Conservation Biology",
                        "Fisheries and Aquatic Sciences",
                        MathPlanConstants.CAT + "natural-resources/fish-wildlife-conservation-biology/"
                                + "fish-wildlife-conservation-biology-major-fisheries-aquatic-sciences-concentration/");
                final MajorMathRequirement rFWCBFASZ =
                        new MajorMathRequirement("FWCB-FASZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124!", "M 125!", MathPlanConstants.CALC1BIO);
                map.put(mFWCBFASZ, rFWCBFASZ);

                final Major mFWCBWDBZ = new Major(6013, "FWCB-WDBZ-BS",
                        Boolean.TRUE, "Fish, Wildlife and Conservation Biology",
                        "Wildlife Biology",
                        MathPlanConstants.CAT + "natural-resources/fish-wildlife-conservation-biology/"
                                + "fish-wildlife-conservation-biology-major-wildlife-concentration/");
                final MajorMathRequirement rFWCBWDBZ =
                        new MajorMathRequirement("FWCB-WDBZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124!", "M 125!", MathPlanConstants.CALC1BIO);
                map.put(mFWCBWDBZ, rFWCBWDBZ);

                // *** Major in Forest and Rangeland Stewardship (with five concentrations)

                final Major mFRRS = new Major(6080, "FRRS-BS", Boolean.FALSE,
                        "Forest and Rangeland Stewardship",
                        MathPlanConstants.CAT + "natural-resources/forest-rangeland-stewardship/forest-rangeland" +
                                "-stewardship-major/");
                final MajorMathRequirement rFRRS = new MajorMathRequirement("FRRS-BS")
                        .setSemesterCourses("M 117!,M 118!,M 125", "M 141", null);
                map.put(mFRRS, rFRRS);

                final Major mFRRSFRBZ = new Major(6081, "FRRS-FRBZ-BS",
                        Boolean.TRUE, "Forest and Rangeland Stewardship",
                        "Forest Biology",
                        MathPlanConstants.CAT + "natural-resources/forest-rangeland-stewardship/forest-rangeland" +
                                "-stewardship-major/"
                                + "forest-biology-concentration/");
                final MajorMathRequirement rFRRSFRBZ =
                        new MajorMathRequirement("FRRS-FRBZ-BS")
                                .setSemesterCourses("M 124!,M 125!", "M 155!", null);
                map.put(mFRRSFRBZ, rFRRSFRBZ);

                final Major mFRRSFRFZ = new Major(6082, "FRRS-FRFZ-BS",
                        Boolean.TRUE, "Forest and Rangeland Stewardship",
                        "Forest Fire Science",
                        MathPlanConstants.CAT + "natural-resources/forest-rangeland-stewardship/forest-rangeland" +
                                "-stewardship-major/"
                                + "forest-fire-science-concentration/");
                final MajorMathRequirement rFRRSFRFZ =
                        new MajorMathRequirement("FRRS-FRFZ-BS")
                                .setSemesterCourses("M 141!", null, null);
                map.put(mFRRSFRFZ, rFRRSFRFZ);

                final Major mFRRSFMGZ = new Major(6083, "FRRS-FMGZ-BS",
                        Boolean.TRUE, "Forest and Rangeland Stewardship",
                        "Forest Management",
                        MathPlanConstants.CAT + "natural-resources/forest-rangeland-stewardship/forest-rangeland" +
                                "-stewardship-major/"
                                + "forest-management-concentration/");
                final MajorMathRequirement rFRRSFMGZ =
                        new MajorMathRequirement("FRRS-FMGZ-BS")
                                .setSemesterCourses("M 117!,M 118!", "M 141!", null);
                map.put(mFRRSFMGZ, rFRRSFMGZ);

                final Major mFRRSRFMZ = new Major(6084, "FRRS-RFMZ-BS",
                        Boolean.TRUE, "Forest and Rangeland Stewardship",
                        "Rangeland and Forest Management",
                        MathPlanConstants.CAT + "natural-resources/forest-rangeland-stewardship/forest-rangeland" +
                                "-stewardship-major/"
                                + "rangeland-forest-management-concentration/");
                final MajorMathRequirement rFRRSRFMZ =
                        new MajorMathRequirement("FRRS-RFMZ-BS")
                                .setSemesterCourses("M 141!", null, null);
                map.put(mFRRSRFMZ, rFRRSRFMZ);

                final Major mFRRSRCMZ = new Major(6085, "FRRS-RCMZ-BS",
                        Boolean.TRUE, "Forest and Rangeland Stewardship",
                        "Rangeland Conservation and Management",
                        MathPlanConstants.CAT + "natural-resources/forest-rangeland-stewardship/forest-rangeland" +
                                "-stewardship-major/"
                                + "rangeland-conservation-management-concentration/");
                final MajorMathRequirement rFRRSRCMZ =
                        new MajorMathRequirement("FRRS-RCMZ-BS")
                                .setSemesterCourses(MathPlanConstants.FRRS3, null, null);
                map.put(mFRRSRCMZ, rFRRSRCMZ);

                // *** Major in Geology (with four concentrations)

                final Major mGEOL = new Major(6020, "GEOL-BS", Boolean.FALSE,
                        "Geology",
                        MathPlanConstants.CAT + "natural-resources/geosciences/geology-major/");
                final MajorMathRequirement rGEOL = new MajorMathRequirement("GEOL-BS")
                        .setSemesterCourses("M 124!,M 125!,M 126.", "M 160.", MathPlanConstants.M_161);
                map.put(mGEOL, rGEOL);

                final Major mGEOLEVGZ = new Major(6021, "GEOL-EVGZ-BS",
                        Boolean.TRUE, "Geology",
                        "Environmental Geology",
                        MathPlanConstants.CAT + "natural-resources/geosciences/geology-major-environmental" +
                                "-concentration/");
                final MajorMathRequirement rGEOLEVGZ =
                        new MajorMathRequirement("GEOL-EVGZ-BS")
                                .setSemesterCourses("M 160.", null, MathPlanConstants.M_161);
                map.put(mGEOLEVGZ, rGEOLEVGZ);

                final Major mGEOLGEOZ = new Major(6022, "GEOL-GEOZ-BS",
                        Boolean.TRUE, "Geology",
                        "Geology",
                        MathPlanConstants.CAT + "natural-resources/geosciences/geology-major-geology-concentration/");
                final MajorMathRequirement rGEOLGEOZ =
                        new MajorMathRequirement("GEOL-GEOZ-BS")
                                .setSemesterCourses("M 124!,M 125!,M 126.", "M 160.", MathPlanConstants.M_161);
                map.put(mGEOLGEOZ, rGEOLGEOZ);

                final Major mGEOLGPYZ = new Major(6023, "GEOL-GPYZ-BS",
                        Boolean.TRUE, "Geology",
                        "Geophysics",
                        MathPlanConstants.CAT + "natural-resources/geosciences/geology-major-geophysics-concentration" +
                                "/");
                final MajorMathRequirement rGEOLGPYZ =
                        new MajorMathRequirement("GEOL-GPYZ-BS")
                                .setSemesterCourses(null, "M 160.", "M 151,M 161,M 261,M 340");
                map.put(mGEOLGPYZ, rGEOLGPYZ);

                final Major mGEOLHYDZ = new Major(6024, "GEOL-HYDZ-BS",
                        Boolean.TRUE, "Geology",
                        "Hydrogeology",
                        MathPlanConstants.CAT + "natural-resources/geosciences/geology-major-hydrogeology" +
                                "-concentration/");
                final MajorMathRequirement rGEOLHYDZ =
                        new MajorMathRequirement("GEOL-HYDZ-BS")
                                .setSemesterCourses("M 124!,M 125!,M 126.", "M 160!", "M 161,M 261,M 340");
                map.put(mGEOLHYDZ, rGEOLHYDZ);

                // *** Major in Human Dimensions of Natural Resources

                final Major mHDNR = new Major(6030, "HDNR-BS", Boolean.TRUE,
                        "Human Dimensions of Natural Resources",
                        MathPlanConstants.CAT + "natural-resources/human-dimensions-natural-resources/"
                                + "human-dimensions-natural-resources-major/");
                final MajorMathRequirement rHDNR = new MajorMathRequirement("HDNR-BS")
                        .setSemesterCourses("M 117!,M 118!,M 124.", null, null);
                map.put(mHDNR, rHDNR);

                // *** Major in Natural Resource Tourism (with two concentrations)

                final Major mNRRT = new Major(6040, "NRRT-BS", Boolean.FALSE,
                        "Natural Resource Tourism",
                        MathPlanConstants.CAT + "natural-resources/human-dimensions-natural-resources/natural" +
                                "-resource-tourism-major/");
                final MajorMathRequirement rNRRT = new MajorMathRequirement("NRRT-BS")
                        .setSemesterCourses("M 117!,M 118!,M 124.", null, null);
                map.put(mNRRT, rNRRT);

                final Major mNRRTGLTZ = new Major(6041, "NRRT-GLTZ-BS",
                        Boolean.TRUE, "Natural Resource Tourism",
                        "Global Tourism",
                        MathPlanConstants.CAT + "natural-resources/human-dimensions-natural-resources/"
                                + "natural-resource-tourism-major-global-concentration/");
                final MajorMathRequirement rNRRTGLTZ =
                        new MajorMathRequirement("NRRT-GLTZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124.", null, null);
                map.put(mNRRTGLTZ, rNRRTGLTZ);

                final Major mNRRTNRTZ = new Major(6042, "NRRT-NRTZ-BS",
                        Boolean.TRUE, "Natural Resource Tourism",
                        "Natural Resource Tourism",
                        MathPlanConstants.CAT + "natural-resources/human-dimensions-natural-resources/"
                                + "natural-resource-tourism-major-natural-resource-tourism-concentration/");
                final MajorMathRequirement rNRRTNRTZ =
                        new MajorMathRequirement("NRRT-NRTZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124.", null, null);
                map.put(mNRRTNRTZ, rNRRTNRTZ);

                // *** Major in Natural Resources Management

                final Major mNRMG = new Major(6050, "NRMG-BS", Boolean.TRUE,
                        "Natural Resources Management",
                        MathPlanConstants.CAT + "natural-resources/forest-rangeland-stewardship/natural-resources" +
                                "-management-major/");
                final MajorMathRequirement rNRMG = new MajorMathRequirement("NRMG-BS")
                        .setSemesterCourses("M 117!,M 118!,M 125!", null, null);
                map.put(mNRMG, rNRMG);

                // *** Major in Restoration Ecology

                final Major mRECO = new Major(6090, "RECO-BS", Boolean.TRUE,
                        "Restoration Ecology",
                        MathPlanConstants.CAT + "natural-resources/forest-rangeland-stewardship/restoration-ecology" +
                                "-major/");
                final MajorMathRequirement rRECO = new MajorMathRequirement("RECO-BS")
                        .setSemesterCourses("FRRS3!", null, null);
                map.put(mRECO, rRECO);

                // *** Major in Watershed Science and Sustainability

                final Major mWRSC = new Major(6070, "WRSC-BS", Boolean.FALSE,
                        "Watershed Science and Sustainability",
                        MathPlanConstants.CAT + "natural-resources/ecosystem-science-sustainability/"
                                + "watershed-science-sustainability-major/");
                final MajorMathRequirement rWRSC = new MajorMathRequirement("WRSC-BS")
                        .setSemesterCourses("CALC1BIO!", null, null);
                map.put(mWRSC, rWRSC);

                final Major mWRSCWSDZ = new Major(6071, "WRSC-WSDZ-BS",
                        Boolean.TRUE, "Watershed Science and Sustainability",
                        "Watershed Data",
                        MathPlanConstants.CAT + "natural-resources/ecosystem-science-sustainability/"
                                + "watershed-science-sustainability-major-watershed-data-concentration/");
                final MajorMathRequirement rWRSCWSDZ =
                        new MajorMathRequirement("WRSC-WSDZ-BS")
                                .setSemesterCourses("CALC1BIO!", null, null);
                map.put(mWRSCWSDZ, rWRSCWSDZ);

                final Major mWRSCWSSZ = new Major(6072, "WRSC-WSSZ-BS",
                        Boolean.TRUE, "Watershed Science and Sustainability",
                        "Watershed Science",
                        MathPlanConstants.CAT + "natural-resources/ecosystem-science-sustainability/"
                                + "watershed-science-sustainability-major-watershed-science-concentration/");
                final MajorMathRequirement rWRSCWSSZ =
                        new MajorMathRequirement("WRSC-WSSZ-BS")
                                .setSemesterCourses(null, null, MathPlanConstants.CALC1BIO);
                map.put(mWRSCWSSZ, rWRSCWSSZ);

                final Major mWRSCWSUZ = new Major(6073, "WRSC-WSUZ-BS",
                        Boolean.TRUE, "Watershed Science and Sustainability",
                        "Watershed Sustainability",
                        MathPlanConstants.CAT + "natural-resources/ecosystem-science-sustainability/"
                                + "watershed-science-sustainability-major-watershed-sustainability-concentration/");
                final MajorMathRequirement rWRSCWSUZ =
                        new MajorMathRequirement("WRSC-WSUZ-BS")
                                .setSemesterCourses(null, null, MathPlanConstants.CALC);
                map.put(mWRSCWSUZ, rWRSCWSUZ);

                // ===========================
                // College of Natural Sciences
                // ===========================

                // *** Major in Biochemistry (with four concentrations)

                final Major mBCHM = new Major(7010, "BCHM-BS", Boolean.FALSE,
                        "Biochemistry",
                        MathPlanConstants.CAT + "natural-sciences/biochemistry-molecular-biology/"
                                + "biochemistry-major/");
                final MajorMathRequirement rBCHM = new MajorMathRequirement("BCHM-BS")
                        .setSemesterCourses("CALC1BIO!", "CALC2BIO!", null);
                map.put(mBCHM, rBCHM);

                final Major mBCHMASBZ = new Major(7014, "BCHM-ASBZ-BS",
                        Boolean.TRUE, "Biochemistry",
                        "ASBMB",
                        MathPlanConstants.CAT + "natural-sciences/biochemistry-molecular-biology/"
                                + "biochemistry-major-asbmb-concentration/");
                final MajorMathRequirement rBCHMASBZ =
                        new MajorMathRequirement("BCHM-ASBZ-BS")
                                .setSemesterCourses("CALC1BIO!", "CALC2BIO!", null);
                map.put(mBCHMASBZ, rBCHMASBZ);

                final Major mBCHMDTSZ = new Major(7015, "BCHM-DTSZ-BS",
                        Boolean.TRUE, "Biochemistry",
                        "Data Science",
                        MathPlanConstants.CAT + "natural-sciences/biochemistry-molecular-biology/"
                                + "biochemistry-major-data-science-concentration/");
                final MajorMathRequirement rBCHMDTSZ =
                        new MajorMathRequirement("BCHM-DTSZ-BS")
                                .setSemesterCourses("M 155!", "M 255!", null);
                map.put(mBCHMDTSZ, rBCHMDTSZ);

                final Major mBCHMHMSZ = new Major(7012, "BCHM-HMSZ-BS",
                        Boolean.TRUE, "Biochemistry",
                        "Health and Medical Sciences",
                        MathPlanConstants.CAT + "natural-sciences/biochemistry-molecular-biology/"
                                + "biochemistry-major-health-medical-sciences-concentration/");
                final MajorMathRequirement rBCHMHMSZ =
                        new MajorMathRequirement("BCHM-HMSZ-BS")
                                .setSemesterCourses("CALC1BIO!", "CALC2BIO!", null);
                map.put(mBCHMHMSZ, rBCHMHMSZ);

                final Major mBCHMPPHZ = new Major(7013, "BCHM-PPHZ-BS",
                        Boolean.TRUE, "Biochemistry",
                        "Pre-Pharmacy",
                        MathPlanConstants.CAT + "natural-sciences/biochemistry-molecular-biology/"
                                + "biochemistry-major-prepharmacy-concentration/");
                final MajorMathRequirement rBCHMPPHZ =
                        new MajorMathRequirement("BCHM-PPHZ-BS")
                                .setSemesterCourses("CALC1BIO!", "CALC2BIO!", null);
                map.put(mBCHMPPHZ, rBCHMPPHZ);

                // *** Major in Biological Science (with two concentrations)

                final Major mBLSC = new Major(7020, "BLSC-BS", Boolean.FALSE,
                        "Biological Science",
                        MathPlanConstants.CAT + "natural-sciences/biology/biological-science-major/");
                final MajorMathRequirement rBLSC = new MajorMathRequirement("BLSC-BS")
                        .setSemesterCourses("M 117.,M 118.", "M 124.,M 125.,CALC1BIO!", null);
                map.put(mBLSC, rBLSC);

                final Major mBLSCBLSZ = new Major(7021, "BLSC-BLSZ-BS",
                        Boolean.TRUE, "Biological Science",
                        "Biological Science",
                        MathPlanConstants.CAT + "natural-sciences/biology/biological-science-major-biological-science" +
                                "-concentration/");
                final MajorMathRequirement rBLSCBLSZ =
                        new MajorMathRequirement("BLSC-BLSZ-BS")
                                .setSemesterCourses("M 117.,M 118.", "M 124.,M 125.,CALC1BIO!", null);
                map.put(mBLSCBLSZ, rBLSCBLSZ);

                final Major mBLSCBTNZ = new Major(7022, "BLSC-BTNZ-BS",
                        Boolean.TRUE, "Biological Science",
                        "Botany",
                        MathPlanConstants.CAT + "natural-sciences/biology/biological-science-major-botany" +
                                "-concentration/");
                final MajorMathRequirement rBLSCBTNZ =
                        new MajorMathRequirement("BLSC-BTNZ-BS")
                                .setSemesterCourses("M 117.,M 118.", "M 124.,M 125.,CALC1BIO!", null);
                map.put(mBLSCBTNZ, rBLSCBTNZ);

                // *** Major in Chemistry (with four concentrations)

                final Major mCHEM = new Major(7030, "CHEM-BS", Boolean.TRUE,
                        "Chemistry",
                        MathPlanConstants.CAT + "natural-sciences/chemistry/chemistry-major/");
                final MajorMathRequirement rCHEM = new MajorMathRequirement("CHEM-BS")
                        .setSemesterCourses(null, "CALC1BIO!", "CALC2CHM,CALC3CHM");
                map.put(mCHEM, rCHEM);

                // DEACTIVATED (commented to preserve what the historical number represents):
                // 7031: CHEM-ACSZ-BS, Chemistry/ACS Certified

                // DEACTIVATED (commented to preserve what the historical number represents):
                // 7032: CHEM-NACZ-BS, Chemistry/Non-ACS Certified

                final Major mCHEMECHZ = new Major(7033, "CHEM-ECHZ-BS",
                        Boolean.TRUE, "Chemistry",
                        "Environmental Chemistry",
                        MathPlanConstants.CAT + "natural-sciences/chemistry/chemistry-major/environmental-chemistry" +
                                "-concentration/");
                final MajorMathRequirement rCHEMECHZ =
                        new MajorMathRequirement("CHEM-ECHZ-BS")
                                .setSemesterCourses(null, "CALC1BIO!", "CALC2CHM,CALC3CHM");
                map.put(mCHEMECHZ, rCHEMECHZ);

                final Major mCHEMFCHZ = new Major(7034, "CHEM-FCHZ-BS",
                        Boolean.TRUE, "Chemistry",
                        "Forensic Chemistry",
                        MathPlanConstants.CAT + "natural-sciences/chemistry/chemistry-major/forensic-chemistry" +
                                "-concentration/");
                final MajorMathRequirement rCHEMFCHZ =
                        new MajorMathRequirement("CHEM-FCHZ-BS")
                                .setSemesterCourses(null, "CALC1BIO!", "CALC2CHM,CALC3CHM");
                map.put(mCHEMFCHZ, rCHEMFCHZ);

                final Major mCHEMHSCZ = new Major(7035, "CHEM-HSCZ-BS",
                        Boolean.TRUE, "Chemistry",
                        "Health Sciences",
                        MathPlanConstants.CAT + "natural-sciences/chemistry/chemistry-major/health-sciences" +
                                "-concentration/");
                final MajorMathRequirement rCHEMHSCZ =
                        new MajorMathRequirement("CHEM-HSCZ-BS")
                                .setSemesterCourses(null, "CALC1BIO!", "CALC2CHM,CALC3CHM");
                map.put(mCHEMHSCZ, rCHEMHSCZ);

                final Major mCHEMSCHZ = new Major(7036, "CHEM-SCHZ-BS",
                        Boolean.TRUE, "Chemistry",
                        "Sustainable Chemistry",
                        MathPlanConstants.CAT + "natural-sciences/chemistry/chemistry-major/sustainable-chemistry" +
                                "-concentration/");
                final MajorMathRequirement rCHEMSCHZ =
                        new MajorMathRequirement("CHEM-SCHZ-BS")
                                .setSemesterCourses(null, "CALC1BIO!", "CALC2CHM,CALC3CHM");
                map.put(mCHEMSCHZ, rCHEMSCHZ);

                // *** Major in Computer Science (with five concentrations)

                final Major mCPSC = new Major(7040, "CPSC-BS", Boolean.FALSE,
                        "Computer Science",
                        MathPlanConstants.CAT + "natural-sciences/computer-science/computer-science-major/");
                final MajorMathRequirement rCPSC = new MajorMathRequirement("CPSC-BS")
                        .setSemesterCourses("M 124!,M 126!", MathPlanConstants.CALC1CS, MathPlanConstants.LINALG369);
                map.put(mCPSC, rCPSC);

                final Major mCPSCCPSZ = new Major(7041, "CPSC-CPSZ-BS",
                        Boolean.TRUE, "Computer Science",
                        "Computer Science",
                        MathPlanConstants.CAT + "natural-sciences/computer-science/computer-science-major/"
                                + "computer-science-concentration/");
                final MajorMathRequirement rCPSCCPSZ =
                        new MajorMathRequirement("CPSC-CPSZ-BS")
                                .setSemesterCourses("M 124!,M 126!", MathPlanConstants.CALC1CS,
                                        MathPlanConstants.LINALG369);
                map.put(mCPSCCPSZ, rCPSCCPSZ);

                final Major mCPSCHCCZ = new Major(7042, "CPSC-HCCZ-BS",
                        Boolean.TRUE, "Computer Science",
                        "Human-Centered Computing",
                        MathPlanConstants.CAT + "natural-sciences/computer-science/computer-science-major/"
                                + "human-centered-computing-concentration/");
                final MajorMathRequirement rCPSCHCCZ =
                        new MajorMathRequirement("CPSC-HCCZ-BS")
                                .setSemesterCourses("M 117!,M 118!,M 124!", "M 125!,M 126!", "CALC1CS,LINALG369");
                map.put(mCPSCHCCZ, rCPSCHCCZ);

                final Major mCPSCAIMZ = new Major(7043, "CPSC-AIMZ-BS",
                        Boolean.TRUE, "Computer Science",
                        "Artificial Intelligence and Machine Learning",
                        MathPlanConstants.CAT + "natural-sciences/computer-science/"
                                + "computer-science-major-artificial-intelligence-machine-learning-concentration/");
                final MajorMathRequirement rCPSCAIMZ =
                        new MajorMathRequirement("CPSC-AIMZ-BS")
                                .setSemesterCourses("M 124!,M 126!", MathPlanConstants.CALC1CS,
                                        MathPlanConstants.LINALG369);
                map.put(mCPSCAIMZ, rCPSCAIMZ);

                final Major mCPSCCSYZ = new Major(7044, "CPSC-CSYZ-BS",
                        Boolean.TRUE, "Computer Science",
                        "Computing Systems",
                        MathPlanConstants.CAT + "natural-sciences/computer-science/"
                                + "computer-science-major-computing-systems-concentration/");
                final MajorMathRequirement rCPSCCSYZ =
                        new MajorMathRequirement("CPSC-CSYZ-BS")
                                .setSemesterCourses("M 124!,M 126!", MathPlanConstants.CALC1CS,
                                        MathPlanConstants.LINALG369);
                map.put(mCPSCCSYZ, rCPSCCSYZ);

                final Major mCPSCNSCZ = new Major(7045, "CPSC-NSCZ-BS",
                        Boolean.TRUE, "Computer Science",
                        "Networks and Security",
                        MathPlanConstants.CAT + "natural-sciences/computer-science/"
                                + "computer-science-major-networks-security-concentration/");
                final MajorMathRequirement rCPSCNSCZ =
                        new MajorMathRequirement("CPSC-NSCZ-BS")
                                .setSemesterCourses("M 124!,M 126!", MathPlanConstants.CALC1CS,
                                        MathPlanConstants.LINALG369);
                map.put(mCPSCNSCZ, rCPSCNSCZ);

                final Major mCPSCSEGZ = new Major(7046, "CPSC-SEGZ-BS",
                        Boolean.TRUE, "Computer Science",
                        "Software Engineering",
                        MathPlanConstants.CAT + "natural-sciences/computer-science/"
                                + "computer-science-major-software-engineering-concentration/");
                final MajorMathRequirement rCPSCSEGZ =
                        new MajorMathRequirement("CPSC-SEGZ-BS")
                                .setSemesterCourses("M 124!,M 126!", MathPlanConstants.CALC1CS,
                                        MathPlanConstants.LINALG369);
                map.put(mCPSCSEGZ, rCPSCSEGZ);

                final Major mCPSCCSEZ = new Major(7047, "CPSC-CSEZ-BS",
                        Boolean.TRUE, "Computer Science",
                        "Computer Science Education",
                        MathPlanConstants.CAT + "natural-sciences/computer-science/computer-science-major/"
                                + "computer-science-education-concentration/");
                final MajorMathRequirement rCPSCCSEZ =
                        new MajorMathRequirement("CPSC-CSEZ-BS")
                                .setSemesterCourses("M 124!,M 126!", MathPlanConstants.CALC1CS,
                                        MathPlanConstants.LINALG369);
                map.put(mCPSCCSEZ, rCPSCCSEZ);

                // *** Major in Data Science (with five concentrations)

                final Major mDSCI = new Major(7050, "DSCI-BS", Boolean.FALSE,
                        "Data Science",
                        MathPlanConstants.CAT + "natural-sciences/data-science-major/");
                final MajorMathRequirement rDSCI = new MajorMathRequirement("DSCI-BS")
                        .setSemesterCourses(MathPlanConstants.M_156, MathPlanConstants.D_369, "M 151,M 256");
                map.put(mDSCI, rDSCI);

                final Major mDSCICSCZ = new Major(7051, "DSCI-CSCZ-BS",
                        Boolean.TRUE, "Data Science",
                        "Computer Science",
                        MathPlanConstants.CAT + "natural-sciences/data-science-major/computer-science-concentration/");
                final MajorMathRequirement rDSCICSCZ =
                        new MajorMathRequirement("DSCI-CSCZ-BS")
                                .setSemesterCourses(MathPlanConstants.M_156, MathPlanConstants.D_369, "M 151,M 256");
                map.put(mDSCICSCZ, rDSCICSCZ);

                final Major mDSCIECNZ = new Major(7052, "DSCI-ECNZ-BS",
                        Boolean.TRUE, "Data Science",
                        "Economics",
                        MathPlanConstants.CAT + "natural-sciences/data-science-major/economics-concentration/");
                final MajorMathRequirement rDSCIECNZ =
                        new MajorMathRequirement("DSCI-ECNZ-BS")
                                .setSemesterCourses(MathPlanConstants.M_156, MathPlanConstants.D_369, "M 151,M 256");
                map.put(mDSCIECNZ, rDSCIECNZ);

                final Major mDSCIMATZ = new Major(7053, "DSCI-MATZ-BS",
                        Boolean.TRUE, "Data Science",
                        "Mathematics",
                        MathPlanConstants.CAT + "natural-sciences/data-science-major/mathematics-concentration/");
                final MajorMathRequirement rDSCIMATZ =
                        new MajorMathRequirement("DSCI-MATZ-BS")
                                .setSemesterCourses(MathPlanConstants.M_156, MathPlanConstants.D_369, "M 151,M 256");
                map.put(mDSCIMATZ, rDSCIMATZ);

                final Major mDSCISTSZ = new Major(7054, "DSCI-STSZ-BS",
                        Boolean.TRUE, "Data Science",
                        "Statistics",
                        MathPlanConstants.CAT + "natural-sciences/data-science-major/statistics-concentration/");
                final MajorMathRequirement rDSCISTSZ =
                        new MajorMathRequirement("DSCI-STSZ-BS")
                                .setSemesterCourses(MathPlanConstants.M_156, MathPlanConstants.D_369, "M 151,M 256");
                map.put(mDSCISTSZ, rDSCISTSZ);

                final Major mDSCINEUZ = new Major(7055, "DSCI-NEUZ-BS",
                        Boolean.TRUE, "Data Science",
                        "Neuroscience",
                        MathPlanConstants.CAT + "natural-sciences/data-science-major/neuroscience-concentration/");
                final MajorMathRequirement rDSCINEUZ =
                        new MajorMathRequirement("DSCI-NEUZ-BS")
                                .setSemesterCourses(MathPlanConstants.M_156, MathPlanConstants.D_369, "M 151,M 256");
                map.put(mDSCINEUZ, rDSCINEUZ);

                // *** Major in Mathematics (with five concentrations)

                final Major mMATH = new Major(7060, "MATH-BS", Boolean.FALSE,
                        "Mathematics",
                        MathPlanConstants.CAT + "natural-sciences/mathematics/mathematics-major/");
                final MajorMathRequirement rMATH = new MajorMathRequirement("MATH-BS")
                        .setSemesterCourses("M 124!,M 126!,M 160.,M 192", "M 161.", "M 235,M 261,M 317,M 369,ODE");
                map.put(mMATH, rMATH);

                final Major mMATHALSZ = new Major(7061, "MATH-ALSZ-BS",
                        Boolean.TRUE, "Mathematics",
                        "Actuarial Sciences",
                        MathPlanConstants.CAT + "natural-sciences/mathematics/mathematics-major-actuarial-science" +
                                "-concentration/");
                final MajorMathRequirement rMATHALSZ =
                        new MajorMathRequirement("MATH-ALSZ-BS")
                                .setSemesterCourses("M 160!,M 192", "M 161.", "M 261,M 317,ODE,M 369,M 495");
                map.put(mMATHALSZ, rMATHALSZ);

                final Major mMATHAMTZ = new Major(7062, "MATH-AMTZ-BS",
                        Boolean.TRUE, "Mathematics",
                        "Applied Mathematics",
                        MathPlanConstants.CAT + "natural-sciences/mathematics/mathematics-major-applied-concentration" +
                                "/");
                final MajorMathRequirement rMATHAMTZ =
                        new MajorMathRequirement("MATH-AMTZ-BS")
                                .setSemesterCourses("M 160.,M 192", "M 161.",
                                        "M 261,M 317,ODE,LINALG369,M 435,M 450,M 451");
                map.put(mMATHAMTZ, rMATHAMTZ);

                final Major mMATHGNMZ = new Major(7064, "MATH-GNMZ-BS",
                        Boolean.TRUE, "Mathematics",
                        "General Mathematics",
                        MathPlanConstants.CAT + "natural-sciences/mathematics/mathematics-major-general-concentration" +
                                "/");
                final MajorMathRequirement rMATHGNMZ =
                        new MajorMathRequirement("MATH-GNMZ-BS")
                                .setSemesterCourses("M 160.,M 192", "M 161.", "M 261,M 317,MATH2,LINALG369,MATH4");
                map.put(mMATHGNMZ, rMATHGNMZ);

                final Major mMATHMTEZ = new Major(7065, "MATH-MTEZ-BS",
                        Boolean.TRUE, "Mathematics",
                        "Mathematics Education",
                        MathPlanConstants.CAT + "natural-sciences/mathematics/mathematics-major-education" +
                                "-concentration/");
                final MajorMathRequirement rMATHMTEZ =
                        new MajorMathRequirement("MATH-MTEZ-BS")
                                .setSemesterCourses("M 160.,M 192", "M 161.",
                                        "M 230,M 261,M 317,M 366,M 369,M 425,M 470");
                map.put(mMATHMTEZ, rMATHMTEZ);

                final Major mMATHCPMZ = new Major(7066, "MATH-CPMZ-BS",
                        Boolean.TRUE, "Mathematics",
                        "Computational Mathematics",
                        MathPlanConstants.CAT + "natural-sciences/mathematics/mathematics-major-computational" +
                                "-concentration/");
                final MajorMathRequirement rMATHCPMZ =
                        new MajorMathRequirement("MATH-CPMZ-BS")
                                .setSemesterCourses("CALC1CS!,M 192", "CALC2CS!",
                                        "MATH3,LINALG369,MATH5");
                map.put(mMATHCPMZ, rMATHCPMZ);

                // *** Major in Natural Sciences (with five concentrations)

                final Major mNSCI = new Major(7070, "NSCI-BS", Boolean.FALSE,
                        "Natural Sciences",
                        MathPlanConstants.CAT + "natural-sciences/natural-sciences-major/");
                final MajorMathRequirement rNSCI = new MajorMathRequirement("NSCI-BS")
                        .setSemesterCourses("CALC1BIO.", "CALC2BIO.", null);
                map.put(mNSCI, rNSCI);

                final Major mNSCIBLEZ = new Major(7071, "NSCI-BLEZ-BS",
                        Boolean.TRUE, "Natural Sciences",
                        "Biology Education",
                        MathPlanConstants.CAT + "natural-sciences/natural-sciences-major-biology-education" +
                                "-concentration/");
                final MajorMathRequirement rNSCIBLEZ =
                        new MajorMathRequirement("NSCI-BLEZ-BS")
                                .setSemesterCourses("M 117!,M 118!", "CALC1BIO.", null);
                map.put(mNSCIBLEZ, rNSCIBLEZ);

                final Major mNSCICHEZ = new Major(7072, "NSCI-CHEZ-BS",
                        Boolean.TRUE, "Natural Sciences",
                        "Chemistry Education",
                        MathPlanConstants.CAT + "natural-sciences/natural-sciences-major-chemistry-education" +
                                "-concentration/");
                final MajorMathRequirement rNSCICHEZ =
                        new MajorMathRequirement("NSCI-CHEZ-BS")
                                .setSemesterCourses("CALC1BIO.", "CALC2BIO.", null);
                map.put(mNSCICHEZ, rNSCICHEZ);

                final Major mNSCIGLEZ = new Major(7073, "NSCI-GLEZ-BS",
                        Boolean.TRUE, "Natural Sciences",
                        "Geology Education",
                        MathPlanConstants.CAT + "natural-sciences/natural-sciences-major-geology-education" +
                                "-concentration/");
                final MajorMathRequirement rNSCIGLEZ =
                        new MajorMathRequirement("NSCI-GLEZ-BS")
                                .setSemesterCourses("CALC1BIO.", "CALC2BIO.", null);
                map.put(mNSCIGLEZ, rNSCIGLEZ);

                final Major mNSCIPHSZ = new Major(7074, "NSCI-PHSZ-BS",
                        Boolean.TRUE, "Natural Sciences",
                        "Physical Science",
                        MathPlanConstants.CAT + "natural-sciences/natural-sciences-major-physical-science" +
                                "-concentration/");
                final MajorMathRequirement rNSCIPHSZ =
                        new MajorMathRequirement("NSCI-PHSZ-BS")
                                .setSemesterCourses("CALC1BIO.", "CALC2BIO.", null);
                map.put(mNSCIPHSZ, rNSCIPHSZ);

                final Major mNSCIPHEZ = new Major(7075, "NSCI-PHEZ-BS",
                        Boolean.TRUE, "Natural Sciences",
                        "Physics Education",
                        MathPlanConstants.CAT + "natural-sciences/natural-sciences-major-physics-education" +
                                "-concentration/");
                final MajorMathRequirement rNSCIPHEZ =
                        new MajorMathRequirement("NSCI-PHEZ-BS")
                                .setSemesterCourses("M 160.", "M 161.", MathPlanConstants.M_261);
                map.put(mNSCIPHEZ, rNSCIPHEZ);

                // *** Major in Physics (with two concentrations)

                final Major mPHYS = new Major(7080, "PHYS-BS", Boolean.FALSE,
                        "Physics",
                        MathPlanConstants.CAT + "natural-sciences/physics/physics-major/");
                final MajorMathRequirement rPHYS = new MajorMathRequirement("PHYS-BS")
                        .setSemesterCourses("M 160.", "M 161.", "M 261,ODE");
                map.put(mPHYS, rPHYS);

                final Major mPHYSAPPZ = new Major(7081, "PHYS-APPZ-BS",
                        Boolean.TRUE, "Physics",
                        "Applied Physics",
                        MathPlanConstants.CAT + "natural-sciences/physics/physics-major-applied-concentration/");
                final MajorMathRequirement rPHYSAPPZ =
                        new MajorMathRequirement("PHYS-APPZ-BS")
                                .setSemesterCourses("M 160.", "M 161.", "M 261,ODE,M 369");
                map.put(mPHYSAPPZ, rPHYSAPPZ);

                final Major mPHYSPHYZ = new Major(7082, "PHYS-PHYZ-BS",
                        Boolean.TRUE, "Physics",
                        "Physics",
                        MathPlanConstants.CAT + "natural-sciences/physics/physics-major-physics-concentration/");
                final MajorMathRequirement rPHYSPHYZ =
                        new MajorMathRequirement("PHYS-PHYZ-BS")
                                .setSemesterCourses("M 160.", "M 161.", "M 261,ODE,M 369");
                map.put(mPHYSPHYZ, rPHYSPHYZ);

                // *** Major in Psychology (with five concentrations)

                final Major mPSYC = new Major(7090, "PSYC-BS", Boolean.FALSE,
                        "Psychology",
                        MathPlanConstants.CAT + "natural-sciences/psychology/psychology-major/");
                final MajorMathRequirement rPSYC = new MajorMathRequirement("PSYC-BS")
                        .setSemesterCourses("M 117!", "M 118!,M 124!", null);
                map.put(mPSYC, rPSYC);

                final Major mPSYCADCZ = new Major(7091, "PSYC-ADCZ-BS",
                        Boolean.TRUE, "Psychology",
                        "Addictions Counseling",
                        MathPlanConstants.CAT + "natural-sciences/psychology/psychology-major-addictions-counseling" +
                                "-concentration/");
                final MajorMathRequirement rPSYCADCZ =
                        new MajorMathRequirement("PSYC-ADCZ-BS")
                                .setSemesterCourses("M 117!", "M 118!,M 124!", null);
                map.put(mPSYCADCZ, rPSYCADCZ);

                final Major mPSYCCCPZ = new Major(7092, "PSYC-CCPZ-BS",
                        Boolean.TRUE, "Psychology",
                        "Clinical/Counseling Psychology",
                        MathPlanConstants.CAT + "natural-sciences/psychology/psychology-major-"
                                + "clinical-counseling-concentration/index.html");
                final MajorMathRequirement rPSYCCCPZ =
                        new MajorMathRequirement("PSYC-CCPZ-BS")
                                .setSemesterCourses("M 117!", "M 118!,M 124!", null);
                map.put(mPSYCCCPZ, rPSYCCCPZ);

                final Major mPSYCGPSZ = new Major(7093, "PSYC-GPSZ-BS",
                        Boolean.TRUE, "Psychology",
                        "General Psychology",
                        MathPlanConstants.CAT + "natural-sciences/psychology/psychology-major-general-concentration/");
                final MajorMathRequirement rPSYCGPSZ =
                        new MajorMathRequirement("PSYC-GPSZ-BS")
                                .setSemesterCourses("M 117!", "M 118!,M 124!", null);
                map.put(mPSYCGPSZ, rPSYCGPSZ);

                final Major mPSYCIOPZ = new Major(7094, "PSYC-IOPZ-BS",
                        Boolean.TRUE, "Psychology",
                        "Industrial/Organizational",
                        MathPlanConstants.CAT + "natural-sciences/psychology/psychology-major-industrial" +
                                "-organizational-concentration/");
                final MajorMathRequirement rPSYCIOPZ =
                        new MajorMathRequirement("PSYC-IOPZ-BS")
                                .setSemesterCourses("M 117!", "M 118!,M 124!", null);
                map.put(mPSYCIOPZ, rPSYCIOPZ);

                final Major mPSYCMBBZ = new Major(7095, "PSYC-MBBZ-BS",
                        Boolean.TRUE, "Psychology",
                        "Mind, Brain, and Behavior",
                        MathPlanConstants.CAT + "natural-sciences/psychology/psychology-major-mind-brain-behavior" +
                                "-concentration/");
                final MajorMathRequirement rPSYCMBBZ =
                        new MajorMathRequirement("PSYC-MBBZ-BS")
                                .setSemesterCourses("M 117!", "M 118!,M 124!", null);
                map.put(mPSYCMBBZ, rPSYCMBBZ);

                // *** Major in Statistics

                final Major mSTAT = new Major(7100, "STAT-BS", Boolean.TRUE,
                        "Statistics",
                        MathPlanConstants.CAT + "natural-sciences/statistics/statistics-major/");
                final MajorMathRequirement rSTAT = new MajorMathRequirement("STAT-BS")
                        .setSemesterCourses("M 160.", "M 161.", "M 261,LINALG369");
                map.put(mSTAT, rSTAT);

                // DEACTIVATED (commented to preserve what the historical number represents):
                // 7101: STAT-GSTZ-BS, Statistics/General Statistics

                // DEACTIVATED (commented to preserve what the historical number represents):
                // 7102: STAT-MSTZ-BS, Statistics/Mathematical Statistics

                // *** Major in Zoology

                final Major mZOOL = new Major(7110, "ZOOL-BS", Boolean.TRUE,
                        "Zoology",
                        MathPlanConstants.CAT + "natural-sciences/biology/zoology-major/");
                final MajorMathRequirement rZOOL = new MajorMathRequirement("ZOOL-BS")
                        .setSemesterCourses("M 117!,M 118!", MathPlanConstants.CALC1BIO, null);
                map.put(mZOOL, rZOOL);

                // ======================================================
                // College of Veterinary Medicine and Biomedical Sciences
                // ======================================================

                // *** Major in Biomedical Sciences (with three concentrations)

                final Major mBIOM = new Major(8000, "BIOM-BS", Boolean.FALSE,
                        "Biomedical Sciences",
                        MathPlanConstants.CAT + "veterinary-medicine-biomedical-sciences/biomedical-sciences-major/");
                final MajorMathRequirement rBIOM = new MajorMathRequirement("BIOM-BS")
                        .setSemesterCourses("M 124,M 125,M 126", MathPlanConstants.CALC1BIO, null);
                map.put(mBIOM, rBIOM);

                final Major mBIOMAPHZ = new Major(8001, "BIOM-APHZ-BS",
                        Boolean.TRUE, "Biomedical Sciences",
                        "Anatomy and Physiology",
                        MathPlanConstants.CAT + "veterinary-medicine-biomedical-sciences/biomedical-sciences/"
                                + "biomedical-sciences-major-anatomy-physiology-concentration/");
                final MajorMathRequirement rBIOMAPHZ =
                        new MajorMathRequirement("BIOM-APHZ-BS")
                                .setSemesterCourses("M 124,M 125,M 126", MathPlanConstants.CALC1BIO, null);
                map.put(mBIOMAPHZ, rBIOMAPHZ);

                final Major mBIOMEPHZ = new Major(8002, "BIOM-EPHZ-BS",
                        Boolean.TRUE, "Biomedical Sciences",
                        "Environmental Public Health",
                        MathPlanConstants.CAT + "veterinary-medicine-biomedical-sciences/"
                                + "environmental-radiological-health-sciences/"
                                + "biomedical-sciences-major-environmental-public-health-concentration/");
                final MajorMathRequirement rBIOMEPHZ =
                        new MajorMathRequirement("BIOM-EPHZ-BS")
                                .setSemesterCourses(MathPlanConstants.BIOM1, MathPlanConstants.BIOM2, null);
                map.put(mBIOMEPHZ, rBIOMEPHZ);

                final Major mBIOMMIDZ = new Major(8003, "BIOM-MIDZ-BS",
                        Boolean.TRUE, "Biomedical Sciences",
                        "Microbiology and Infectious Disease",
                        MathPlanConstants.CAT + "veterinary-medicine-biomedical-sciences/microbiology-immunology" +
                                "-pathology/"
                                + "biomedical-sciences-major-microbiology-infectious-disease-concentration/");
                final MajorMathRequirement rBIOMMIDZ =
                        new MajorMathRequirement("BIOM-MIDZ-BS")
                                .setSemesterCourses(MathPlanConstants.BIOM1, MathPlanConstants.BIOM3, null);
                map.put(mBIOMMIDZ, rBIOMMIDZ);

                // *** Major in Environmental Health

                // DEACTIVATED (commented to preserve what the historical number represents):
                // 8010: EVHL-BS, Environmental Health

                // *** Major in Microbiology

                // DEACTIVATED (commented to preserve what the historical number represents):
                // 8020: MICR-BS, Microbiology

                // *** Major in Neuroscience (with two concentrations)

                final Major mNERO = new Major(8030, "NERO-BS", Boolean.FALSE,
                        "Neuroscience",
                        MathPlanConstants.CAT + "veterinary-medicine-biomedical-sciences/biomedical-sciences" +
                                "/neuroscience-major/");
                final MajorMathRequirement rNERO = new MajorMathRequirement("NERO-BS")
                        .setSemesterCourses("M 124!,M 125!,M 126!", MathPlanConstants.M_155, null);
                map.put(mNERO, rNERO);

                final Major mNEROBCNZ = new Major(8031, "NERO-BCNZ-BS",
                        Boolean.TRUE, "Neuroscience",
                        "Behavioral and Cognitive Neuroscience",
                        MathPlanConstants.CAT + "veterinary-medicine-biomedical-sciences/biomedical-"
                                + "sciences/neuroscience-major-behavioral-cognitive-concentration/");
                final MajorMathRequirement rNEROBCNZ =
                        new MajorMathRequirement("NERO-BCNZ-BS")
                                .setSemesterCourses("M 124!,M 125!,M 126!", MathPlanConstants.M_155, null);
                map.put(mNEROBCNZ, rNEROBCNZ);

                final Major mNEROCMNZ = new Major(8032, "NERO-CMNZ-BS",
                        Boolean.TRUE, "Neuroscience",
                        "Cell and Molecular Neuroscience",
                        MathPlanConstants.CAT + "veterinary-medicine-biomedical-sciences/biomedical-"
                                + "sciences/neuroscience-major-cell-molecular-concentration/");
                final MajorMathRequirement rNEROCMNZ =
                        new MajorMathRequirement("NERO-CMNZ-BS")
                                .setSemesterCourses("M 124!,M 125!,M 126!", MathPlanConstants.M_155,
                                        MathPlanConstants.M_255);
                map.put(mNEROCMNZ, rNEROCMNZ);

                // ***
                // ***
                // ***
                // ***
                // Add records for "fake" program codes so we can display something sensible for Current declared major
                // ***
                // ***
                // ***
                // ***
                // ***
                // ***

                final Major mAGBUDD = new Major(9000, "AGBU-DD-BS", Boolean.FALSE,
                        "Agricultural Business",
                        MathPlanConstants.CAT + "agricultural-sciences/agricultural-resource-economics/business-major/",
                        true);
                final MajorMathRequirement rAGBUDD = new MajorMathRequirement("AGBU-DD-BS")
                        .setSemesterCourses("M 117!,M 118!,M 124", null, MathPlanConstants.M_141);
                map.put(mAGBUDD, rAGBUDD);

                final Major mANTHDD = new Major(9000, "ANTH-DD-BA", Boolean.FALSE,
                        "Anthropology",
                        MathPlanConstants.CAT + "liberal-arts/anthropology-geography/anthropology-major/",
                        true);
                final MajorMathRequirement rANTHDD = new MajorMathRequirement("ANTH-DD-BA")
                        .setSemesterCourses("AUCC3.", null, null);
                map.put(mANTHDD, rANTHDD);

                final Major mAPCTCPTZ = new Major(9000, "APCT-CPTZ-BS", Boolean.FALSE,
                        "Computer Science",
                        MathPlanConstants.CAT + "natural-sciences/computer-science/",
                        true);
                final MajorMathRequirement rAPCTCPTZ = new MajorMathRequirement("APCT-CPTZ-BS")
                        .setSemesterCourses("M 124!,M 126!", MathPlanConstants.CALC1CS, null);
                map.put(mAPCTCPTZ, rAPCTCPTZ);

                final Major mBCHMGBCZ = new Major(9000, "BCHM-GBCZ-BS", Boolean.FALSE,
                        "Biochemistry",
                        MathPlanConstants.CAT + "natural-sciences/biochemistry-molecular-biology/biochemistry-major/",
                        true);
                final MajorMathRequirement rBCHMGBCZ = new MajorMathRequirement("BCHM-GBCZ-S")
                        .setSemesterCourses("CALC1BIO!", "CALC2BIO!", null);
                map.put(mBCHMGBCZ, rBCHMGBCZ);

                final Major mBUSADACZ = new Major(9000, "BUSA-DACZ-BS", Boolean.FALSE,
                        "Business Administration",
                        MathPlanConstants.CAT + "business/business-administration/business-administration-major/",
                        true);
                final MajorMathRequirement rBUSADACZ = new MajorMathRequirement("BUSA-DACZ-BS")
                        .setSemesterCourses(MathPlanConstants.BUSA3, null, null);
                map.put(mBUSADACZ, rBUSADACZ);

                final Major mBUSAOIMZ = new Major(9000, "BUSA-OIMZ-BS", Boolean.FALSE,
                        "Business Administration",
                        MathPlanConstants.CAT + "business/business-administration/business-administration-major/",
                        true);
                final MajorMathRequirement rBUSAOIMZ = new MajorMathRequirement("BUSA-OIMZ-BS")
                        .setSemesterCourses(MathPlanConstants.BUSA3, null, null);
                map.put(mBUSAOIMZ, rBUSAOIMZ);

                final Major mCHEMACSZ = new Major(9000, "CHEM-ACSZ-BS", Boolean.FALSE,
                        "Chemistry",
                        MathPlanConstants.CAT + "natural-sciences/chemistry/chemistry-major/",
                        true);
                final MajorMathRequirement rCHEMACSZ = new MajorMathRequirement("CHEM-ACSZ-BS")
                        .setSemesterCourses(null, "CALC1BIO!", "CALC2CHM,CALC3CHM");
                map.put(mCHEMACSZ, rCHEMACSZ);

                final Major mCMSTDD = new Major(9000, "CMST-DD-BA", Boolean.FALSE,
                        "Communication Studies",
                        MathPlanConstants.CAT + "liberal-arts/communication-studies/communication-studies-major/",
                        true);
                final MajorMathRequirement rCMSTDD = new MajorMathRequirement("CMST-DD-BA")
                        .setSemesterCourses(null, "AUCC3", null);
                map.put(mCMSTDD, rCMSTDD);

                final Major mCPSCCFCZ = new Major(9000, "CPSC-CFCZ-BS", Boolean.FALSE,
                        "Computer Science",
                        MathPlanConstants.CAT + "natural-sciences/computer-science/computer-science-major/",
                        true);
                final MajorMathRequirement rCPSCCFCZ = new MajorMathRequirement("CPSC-CFCZ-BS")
                        .setSemesterCourses("M 124!,M 126!", MathPlanConstants.CALC1CS, MathPlanConstants.LINALG369);
                map.put(mCPSCCFCZ, rCPSCCFCZ);

                final Major mCPSCDAIZ = new Major(9000, "CPSC-DAIZ-BS", Boolean.FALSE,
                        "Computer Science",
                        MathPlanConstants.CAT + "natural-sciences/computer-science/computer-science-major/",
                        true);
                final MajorMathRequirement rCPSCDAIZ = new MajorMathRequirement("CPSC-DAIZ-BS")
                        .setSemesterCourses("M 124!,M 126!", MathPlanConstants.CALC1CS, MathPlanConstants.LINALG369);
                map.put(mCPSCDAIZ, rCPSCDAIZ);

                final Major mCPSCDCSZ = new Major(9000, "CPSC-DCSZ-BS", Boolean.FALSE,
                        "Computer Science",
                        MathPlanConstants.CAT + "natural-sciences/computer-science/computer-science-major/",
                        true);
                final MajorMathRequirement rCPSCDCSZ = new MajorMathRequirement("CPSC-DCSZ-BS")
                        .setSemesterCourses("M 124!,M 126!", MathPlanConstants.CALC1CS, MathPlanConstants.LINALG369);
                map.put(mCPSCDCSZ, rCPSCDCSZ);

                final Major mCPSCDCYZ = new Major(9000, "CPSC-DCYZ-BS", Boolean.FALSE,
                        "Computer Science",
                        MathPlanConstants.CAT + "natural-sciences/computer-science/computer-science-major/",
                        true);
                final MajorMathRequirement rCPSCDCYZ = new MajorMathRequirement("CPSC-DCYZ-BS")
                        .setSemesterCourses("M 124!,M 126!", MathPlanConstants.CALC1CS, MathPlanConstants.LINALG369);
                map.put(mCPSCDCYZ, rCPSCDCYZ);

                final Major mCPSCDHCZ = new Major(9000, "CPSC-DHCZ-BS", Boolean.FALSE,
                        "Computer Science",
                        MathPlanConstants.CAT + "natural-sciences/computer-science/computer-science-major/",
                        true);
                final MajorMathRequirement rCPSCDHCZ = new MajorMathRequirement("CPSC-DHCZ-BS")
                        .setSemesterCourses("M 124!,M 126!", MathPlanConstants.CALC1CS, MathPlanConstants.LINALG369);
                map.put(mCPSCDHCZ, rCPSCDHCZ);

                final Major mCPSCDNSZ = new Major(9000, "CPSC-DNSZ-BS", Boolean.FALSE,
                        "Computer Science",
                        MathPlanConstants.CAT + "natural-sciences/computer-science/computer-science-major/",
                        true);
                final MajorMathRequirement rCPSCDNSZ = new MajorMathRequirement("CPSC-DNSZ-BS")
                        .setSemesterCourses("M 124!,M 126!", MathPlanConstants.CALC1CS, MathPlanConstants.LINALG369);
                map.put(mCPSCDNSZ, rCPSCDNSZ);

                final Major mCPSCDSEZ = new Major(9000, "CPSC-DSEZ-BS", Boolean.FALSE,
                        "Computer Science",
                        MathPlanConstants.CAT + "natural-sciences/computer-science/computer-science-major/",
                        true);
                final MajorMathRequirement rCPSCDSEZ = new MajorMathRequirement("CPSC-DSEZ-BS")
                        .setSemesterCourses("M 124!,M 126!", MathPlanConstants.CALC1CS, MathPlanConstants.LINALG369);
                map.put(mCPSCDSEZ, rCPSCDSEZ);

                final Major mCTM0 = new Major(9000, "CTM0", Boolean.FALSE,
                        "Pre-Construction Management",
                        MathPlanConstants.CAT + "health-human-sciences/construction-management/",
                        true);
                final MajorMathRequirement rCTM0 = new MajorMathRequirement("CTM0")
                        .setSemesterCourses("M 117!,M 118!,M 125!", null, MathPlanConstants.M_141);
                map.put(mCTM0, rCTM0);

                final Major mDNC0 = new Major(9000, "DNC0", Boolean.FALSE,
                        "Pre-Dance",
                        MathPlanConstants.CAT + "liberal-arts/music-theatre-dance/dance/dance-ba/",
                        true);
                final MajorMathRequirement rDNC0 = new MajorMathRequirement("DNC0")
                        .setSemesterCourses("AUCC3.", null, null);
                map.put(mDNC0, rDNC0);

                final Major mDANCDEDZ = new Major(9000, "DANC-DEDZ-BF", Boolean.FALSE,
                        "Dance",
                        MathPlanConstants.CAT + "liberal-arts/music-theatre-dance/dance/dance-bfa/",
                        true);
                final MajorMathRequirement rDANCDEDZ = new MajorMathRequirement("DANC-DEDZ-BF")
                        .setSemesterCourses(null, "AUCC3!", null);
                map.put(mDANCDEDZ, rDANCDEDZ);

                final Major mECONDD = new Major(9000, "ECON-DD-BA", Boolean.FALSE,
                        "Economics",
                        MathPlanConstants.CAT + "liberal-arts/economics/economics-major/",
                        true);
                final MajorMathRequirement rECONDD = new MajorMathRequirement("ECON-DD-BA")
                        .setSemesterCourses(null, "CALC!", null);
                map.put(mECONDD, rECONDD);

                final Major mENGLLANZ = new Major(9000, "ENGL-LANZ-BA", Boolean.FALSE,
                        "English",
                        "Language",
                        MathPlanConstants.CAT + "liberal-arts/english/english-major-creative-writing-concentration/",
                        true);
                final MajorMathRequirement rENGLLANZ = new MajorMathRequirement("ENGL-LANZ-BA")
                        .setSemesterCourses("AUCC3.", null, null);
                map.put(mENGLLANZ, rENGLLANZ);

                final Major mENREDD = new Major(9000, "ENRE-DD-BS", Boolean.FALSE,
                        "Environmental and Natural Resource Economics",
                        MathPlanConstants.CAT + "agricultural-sciences/agricultural-resource-economics/",
                        true);
                final MajorMathRequirement rENREDD = new MajorMathRequirement("ENRE-DD-BS")
                        .setSemesterCourses("M 117!,M 118,M 124", null, MathPlanConstants.M_141);
                map.put(mENREDD, rENREDD);

                final Major mEVHL = new Major(9000, "EVHL-BS", Boolean.FALSE,
                        "Environmental Health",
                        MathPlanConstants.CAT + "veterinary-medicine-biomedical-sciences/"
                                + "environmental-radiological-health-sciences/",
                        true);
                final MajorMathRequirement rEVHL = new MajorMathRequirement("EVHL-BS")
                        .setSemesterCourses("M 124!,M 125!,M 126!", MathPlanConstants.M_155, null);
                map.put(mEVHL, rEVHL);

                final Major mEXAD = new Major(9000, "EXAD", Boolean.FALSE,
                        "Exploratory Studies", // (assuming Arts, Humanities, and Design)
                        MathPlanConstants.CAT2 + "/academic-standards/advising/#Exploratory%20Studies%20Advising",
                        true);
                final MajorMathRequirement rEXAD = new MajorMathRequirement("EXAD")
                        .setSemesterCourses("AUCC3.", null, null);
                map.put(mEXAD, rEXAD);

                final Major mEXCO = new Major(9000, "EXCO", Boolean.FALSE,
                        "Exploratory Studies", // (assuming Journalism and Communication)
                        MathPlanConstants.CAT2 + "/academic-standards/advising/#Exploratory%20Studies%20Advising",
                        true);
                final MajorMathRequirement rEXCO = new MajorMathRequirement("EXCO")
                        .setSemesterCourses("AUCC3.", null, null);
                map.put(mEXCO, rEXCO);

                final Major mEXGS = new Major(9000, "EXGS", Boolean.FALSE,
                        "Exploratory Studies", // (assuming Global and Social Sciences)
                        MathPlanConstants.CAT2 + "/academic-standards/advising/#Exploratory%20Studies%20Advising",
                        true);
                final MajorMathRequirement rEXGS = new MajorMathRequirement("EXGS")
                        .setSemesterCourses("AUCC3.", null, null);
                map.put(mEXGS, rEXGS);

                final Major mEXHF = new Major(9000, "EXHF", Boolean.FALSE,
                        "Exploratory Studies", // (assuming Health, Life, and Food Sciences)
                        MathPlanConstants.CAT2 + "/academic-standards/advising/#Exploratory%20Studies%20Advising",
                        true);
                final MajorMathRequirement rEXHF = new MajorMathRequirement("EXHF")
                        .setSemesterCourses("M 117,M 118,M 124", "M 125,M 126", null);
                map.put(mEXHF, rEXHF);

                final Major mEXLA = new Major(9000, "EXLA", Boolean.FALSE,
                        "Exploratory Studies", // (assuming Arts, Humanities, and Design)
                        MathPlanConstants.CAT2 + "/academic-standards/advising/#Exploratory%20Studies%20Advising",
                        true);
                final MajorMathRequirement rEXLA = new MajorMathRequirement("EXLA")
                        .setSemesterCourses("AUCC3.", null, null);
                map.put(mEXLA, rEXLA);

                final Major mEXNR = new Major(9000, "EXNR", Boolean.FALSE,
                        "Exploratory Studies", // (assuming Environment and Natural Resources)
                        MathPlanConstants.CAT2 + "/academic-standards/advising/#Exploratory%20Studies%20Advising",
                        true);
                final MajorMathRequirement rEXNR = new MajorMathRequirement("EXNR")
                        .setSemesterCourses("M 117,M 118,M 124", "M 125,M 126", null);
                map.put(mEXNR, rEXNR);

                final Major mEXPE = new Major(9000, "EXPE", Boolean.FALSE,
                        "Exploratory Studies", // (assuming Physical Sciences and Engineering)
                        MathPlanConstants.CAT2 + "/academic-standards/advising/#Exploratory%20Studies%20Advising",
                        true);
                final MajorMathRequirement rEXPE = new MajorMathRequirement("EXPE")
                        .setSemesterCourses("M 124!,M 126!", MathPlanConstants.CALC1BIO, null);
                map.put(mEXPE, rEXPE);

                final Major mEXPO = new Major(9000, "EXPO", Boolean.FALSE,
                        "Exploratory Studies", // (assuming Global and Social Sciences)
                        MathPlanConstants.CAT2 + "/academic-standards/advising/#Exploratory%20Studies%20Advising",
                        true);
                final MajorMathRequirement rEXPO = new MajorMathRequirement("EXPO")
                        .setSemesterCourses("AUCC3.", null, null);
                map.put(mEXPO, rEXPO);

                final Major mEXPL = new Major(9000, "EXPL", Boolean.FALSE,
                        "Exploratory Studies", // (assuming Land, Plant, and Animal Sciences)
                        MathPlanConstants.CAT2 + "/academic-standards/advising/#Exploratory%20Studies%20Advising",
                        true);
                final MajorMathRequirement rEXPL = new MajorMathRequirement("EXPL")
                        .setSemesterCourses("M 117,M 118,M 124", "M 125,M 126", null);
                map.put(mEXPL, rEXPL);

                final Major mEXTC = new Major(9000, "EXTC", Boolean.FALSE,
                        "Exploratory Studies", // (assuming Education and Teaching)
                        MathPlanConstants.CAT2 + "/academic-standards/advising/#Exploratory%20Studies%20Advising",
                        true);
                final MajorMathRequirement rEXTC = new MajorMathRequirement("EXTC")
                        .setSemesterCourses("AUCC3.", null, null);
                map.put(mEXTC, rEXTC);

                final Major mFESVDD = new Major(9000, "FESV-DD-BS", Boolean.FALSE,
                        "Fire and Emergency Services Administration",
                        MathPlanConstants.CAT + "/natural-resources/forest-rangeland-stewardship/"
                                + "fire-emergency-services-administration-major/",
                        true);
                final MajorMathRequirement rFESVDD = new MajorMathRequirement("FESV-DD-BS")
                        .setSemesterCourses("AUCC3.", null, null);
                map.put(mFESVDD, rFESVDD);

                final Major mFRSTFMGZ = new Major(6083, "FRST-FMGZ-BS", Boolean.FALSE,
                        "Forestry",
                        MathPlanConstants.CAT + "natural-resources/forest-rangeland-stewardship/",
                        true);
                final MajorMathRequirement rFRSTFMGZ = new MajorMathRequirement("FRST-FMGZ-BS")
                        .setSemesterCourses("M 117!,M 118!", "M 141!", null);
                map.put(mFRSTFMGZ, rFRSTFMGZ);

                final Major mHDFSDECZ = new Major(9000, "HDFS-DECZ-BS", Boolean.FALSE,
                        "Human Development and Family Studies",
                        MathPlanConstants.CAT + "health-human-sciences/human-development-family-studies/",
                        true);
                final MajorMathRequirement rHDFSDECZ = new MajorMathRequirement("HDFS-DECZ-BS")
                        .setSemesterCourses(MathPlanConstants.AUCC3, null, null);
                map.put(mHDFSDECZ, rHDFSDECZ);

                final Major mHDFSDHDZ = new Major(9000, "HDFS-DHDZ-BS", Boolean.FALSE,
                        "Human Development and Family Studies",
                        MathPlanConstants.CAT + "health-human-sciences/human-development-family-studies/",
                        true);
                final MajorMathRequirement rHDFSDHDZ = new MajorMathRequirement("HDFS-DHDZ-BS")
                        .setSemesterCourses(MathPlanConstants.AUCC3, null, null);
                map.put(mHDFSDHDZ, rHDFSDHDZ);

                final Major mHDFSDPHZ = new Major(9000, "HDFS-DPHZ-BS", Boolean.FALSE,
                        "Human Development and Family Studies",
                        MathPlanConstants.CAT + "health-human-sciences/human-development-family-studies/",
                        true);
                final MajorMathRequirement rHDFSDPHZ = new MajorMathRequirement("HDFS-DPHZ-BS")
                        .setSemesterCourses(MathPlanConstants.AUCC3, null, null);
                map.put(mHDFSDPHZ, rHDFSDPHZ);

                final Major mHDFSDPIZ = new Major(9000, "HDFS-DPIZ-BS", Boolean.FALSE,
                        "Human Development and Family Studies",
                        MathPlanConstants.CAT + "health-human-sciences/human-development-family-studies/",
                        true);
                final MajorMathRequirement rHDFSDPIZ = new MajorMathRequirement("HDFS-DPIZ-BS")
                        .setSemesterCourses(MathPlanConstants.AUCC3, null, null);
                map.put(mHDFSDPIZ, rHDFSDPIZ);

                final Major mHDFSDLAZ = new Major(9000, "HDFS-DLAZ-BS", Boolean.FALSE,
                        "Human Development and Family Studies",
                        MathPlanConstants.CAT + "health-human-sciences/human-development-family-studies/",
                        true);
                final MajorMathRequirement rHDFSDLAZ = new MajorMathRequirement("HDFS-DLAZ-BS")
                        .setSemesterCourses(MathPlanConstants.AUCC3, null, null);
                map.put(mHDFSDLAZ, rHDFSDLAZ);

                final Major mHDFSDLEZ = new Major(9000, "HDFS-DLEZ-BS", Boolean.FALSE,
                        "Human Development and Family Studies",
                        MathPlanConstants.CAT + "health-human-sciences/human-development-family-studies/",
                        true);
                final MajorMathRequirement rHDFSDLEZ = new MajorMathRequirement("HDFS-DLEZ-BS")
                        .setSemesterCourses(MathPlanConstants.AUCC3, null, null);
                map.put(mHDFSDLEZ, rHDFSDLEZ);

                final Major mHDFSLPEZ = new Major(9000, "HDFS-LEPZ-BS", Boolean.FALSE,
                        "Human Development and Family Studies",
                        MathPlanConstants.CAT + "health-human-sciences/human-development-family-studies/",
                        true);
                final MajorMathRequirement rHDFSLPEZ = new MajorMathRequirement("HDFS-LEPZ-BS")
                        .setSemesterCourses(MathPlanConstants.AUCC3, null, null);
                map.put(mHDFSLPEZ, rHDFSLPEZ);

                final Major mHORTDHBZ = new Major(9000, "HORT-DHBZ-BS", Boolean.FALSE,
                        "Horticulture",
                        MathPlanConstants.CAT + "agricultural-sciences/horticulture-landscape-architecture" +
                                "/horticulture-major/",
                        true);
                final MajorMathRequirement rHORTDHBZ = new MajorMathRequirement("HORT-DHBZ-BS")
                        .setSemesterCourses("M 117!,M 118,M 124", null, null);
                map.put(mHORTDHBZ, rHORTDHBZ);

                final Major mILARDD = new Major(9000, "ILAR-DD-BA", Boolean.FALSE,
                        "Interdisciplinary Liberal Arts",
                        MathPlanConstants.CAT + "liberal-arts/interdisciplinary-liberal-arts-major/",
                        true);
                final MajorMathRequirement rILARDD = new MajorMathRequirement("ILAR-DD-BA")
                        .setSemesterCourses("AUCC3.", null, null);
                map.put(mILARDD, rILARDD);

                final Major mJAMCDD = new Major(9000, "JAMC-DD-BA", Boolean.FALSE,
                        "Journalism and Media Communication",
                        MathPlanConstants.CAT + "liberal-arts/journalism-media-communication/media-major/",
                        true);
                final MajorMathRequirement rJAMCDD = new MajorMathRequirement("JAMC-DD-BA")
                        .setSemesterCourses(null, "AUCC3!", null);
                map.put(mJAMCDD, rJAMCDD);

                final Major mMICR = new Major(9000, "MICR-BS", Boolean.FALSE,
                        "Biomedical Sciences",
                        "Microbiology and Infectious Disease",
                        MathPlanConstants.CAT + "veterinary-medicine-biomedical-sciences/microbiology-immunology" +
                                "-pathology/",
                        true);
                final MajorMathRequirement rMICR =
                        new MajorMathRequirement("MICR-BS")
                                .setSemesterCourses(MathPlanConstants.BIOM1, MathPlanConstants.BIOM3, null);
                map.put(mMICR, rMICR);

                final Major mMUS0 = new Major(9000, "MUS0", Boolean.FALSE,
                        "Pre-Music",
                        MathPlanConstants.CAT + "liberal-arts/music-theatre-dance/",
                        true);
                final MajorMathRequirement rMUS0 = new MajorMathRequirement("MUS0")
                        .setSemesterCourses(null, "AUCC3!", null);
                map.put(mMUS0, rMUS0);

                final Major mNAFSFSNZ = new Major(9000, "NAFS-FSNZ-BS", Boolean.FALSE,
                        "Nutrition and Food Science",
                        MathPlanConstants.CAT + "health-human-sciences/food-science-human-nutrition/nutrition-food" +
                                "-science-major/",
                        true);
                final MajorMathRequirement rNAFSFSNZ = new MajorMathRequirement("NAFS-FSNZ-BS")
                        .setSemesterCourses("M 117!,M 118!,M 124!", null, null);
                map.put(mNAFSFSNZ, rNAFSFSNZ);

                final Major mNAFSFSYZ = new Major(9000, "NAFS-FSYZ-BS", Boolean.FALSE,
                        "Nutrition and Food Science",
                        MathPlanConstants.CAT + "health-human-sciences/food-science-human-nutrition/nutrition-food" +
                                "-science-major/",
                        true);
                final MajorMathRequirement rNAFSFSYZ = new MajorMathRequirement("NAFS-FSYZ-BS")
                        .setSemesterCourses("M 117!,M 118!,M 124!", null, null);
                map.put(mNAFSFSYZ, rNAFSFSYZ);

                final Major mNAFSNUSZ = new Major(9000, "NAFS-NUSZ-BS", Boolean.FALSE,
                        "Nutrition and Food Science",
                        MathPlanConstants.CAT + "health-human-sciences/food-science-human-nutrition/nutrition-food" +
                                "-science-major/",
                        true);
                final MajorMathRequirement rNAFSNUSZ = new MajorMathRequirement("NAFS-NUSZ-BS")
                        .setSemesterCourses("M 117!,M 118!,M 124!", null, null);
                map.put(mNAFSNUSZ, rNAFSNUSZ);

                final Major mNAFSDNRZ = new Major(9000, "NAFS-DNRZ-BS", Boolean.FALSE,
                        "Nutrition and Food Science",
                        MathPlanConstants.CAT + "health-human-sciences/food-science-human-nutrition/nutrition-food" +
                                "-science-major/",
                        true);
                final MajorMathRequirement rNAFSDNRZ = new MajorMathRequirement("NAFS-DNRZ-BS")
                        .setSemesterCourses("M 117!,M 118!,M 124!", null, null);
                map.put(mNAFSDNRZ, rNAFSDNRZ);

                final Major mNAFSGLTZ = new Major(9000, "NAFS-GLTZ-BS", Boolean.FALSE,
                        "Nutrition and Food Science",
                        MathPlanConstants.CAT + "health-human-sciences/food-science-human-nutrition/nutrition-food" +
                                "-science-major/",
                        true);
                final MajorMathRequirement rNAFSGLTZ = new MajorMathRequirement("NAFS-GLTZ-BS")
                        .setSemesterCourses("M 117!,M 118!,M 124!", null, null);
                map.put(mNAFSGLTZ, rNAFSGLTZ);

                final Major mNAFSNRTZ = new Major(9000, "NAFS-NRTZ-BS", Boolean.FALSE,
                        "Nutrition and Food Science",
                        MathPlanConstants.CAT + "health-human-sciences/food-science-human-nutrition/nutrition-food" +
                                "-science-major/",
                        true);
                final MajorMathRequirement rNAFSNRTZ = new MajorMathRequirement("NAFS-NRTZ-BS")
                        .setSemesterCourses("M 117!,M 118!,M 124!", null, null);
                map.put(mNAFSNRTZ, rNAFSNRTZ);

                final Major mNAFSCPSY = new Major(9000, "NAFS-CPSY-BS", Boolean.FALSE,
                        "Nutrition and Food Science",
                        MathPlanConstants.CAT + "health-human-sciences/food-science-human-nutrition/nutrition-food" +
                                "-science-major/",
                        true);
                final MajorMathRequirement rNAFSCPSY = new MajorMathRequirement("NAFS-CPSY-BS")
                        .setSemesterCourses("M 117!,M 118!,M 124!", null, null);
                map.put(mNAFSCPSY, rNAFSCPSY);

                final Major mPOLSDD = new Major(9000, "POLS-DD-BA", Boolean.FALSE,
                        "Political Science",
                        MathPlanConstants.CAT + "liberal-arts/political-science/political-science-major/",
                        true);
                final MajorMathRequirement rPOLSDD = new MajorMathRequirement("POLS-DD-BA")
                        .setSemesterCourses(null, "AUCC3!", null);
                map.put(mPOLSDD, rPOLSDD);

                final Major mPSYCGDSZ = new Major(9000, "PSYC-GDSZ-BS", Boolean.FALSE,
                        "Psychology",
                        MathPlanConstants.CAT + "natural-sciences/psychology/",
                        true);
                final MajorMathRequirement rPSYCGDSZ = new MajorMathRequirement("PSYC-GDSZ-BS")
                        .setSemesterCourses("M 117!", "M 118!,M 124!", null);
                map.put(mPSYCGDSZ, rPSYCGDSZ);

                final Major mSOCIDGSZ = new Major(9000, "SOCI-DGSZ-BA", Boolean.FALSE,
                        "Sociology",
                        MathPlanConstants.CAT + "liberal-arts/sociology/",
                        true);
                final MajorMathRequirement rSOCIDGSZ = new MajorMathRequirement("SOCI-DGSZ-BA")
                        .setSemesterCourses(null, "AUCC3!", null);
                map.put(mSOCIDGSZ, rSOCIDGSZ);

                final Major mSOCRAPMZ = new Major(9000, "SOCR-APMZ-BS", Boolean.FALSE,
                        "Soil and Crop Sciences",
                        MathPlanConstants.CAT + "agricultural-sciences/soil-crop-sciences/soil-crop-sciences-major/",
                        true);
                final MajorMathRequirement rSOCRAPMZ = new MajorMathRequirement("SOCR-APMZ-BS")
                        .setSemesterCourses("M 117!,M 118!,M 124!", null, null);
                map.put(mSOCRAPMZ, rSOCRAPMZ);

                final Major mSOCRDSAZ = new Major(9000, "SOCR-DSAZ-BS", Boolean.FALSE,
                        "Soil and Crop Sciences",
                        MathPlanConstants.CAT + "agricultural-sciences/soil-crop-sciences/soil-crop-sciences-major/",
                        true);
                final MajorMathRequirement rSOCRDSAZ = new MajorMathRequirement("SOCR-DSAZ-BS")
                        .setSemesterCourses("M 117!,M 118!,M 124!", null, null);
                map.put(mSOCRDSAZ, rSOCRDSAZ);

                final Major mSOCRISCZ = new Major(9000, "SOCR-ISCZ-BS", Boolean.FALSE,
                        "Soil and Crop Sciences",
                        MathPlanConstants.CAT + "agricultural-sciences/soil-crop-sciences/soil-crop-sciences-major/",
                        true);
                final MajorMathRequirement rSOCRISCZ = new MajorMathRequirement("SOCR-ISCZ-BS")
                        .setSemesterCourses("M 117!,M 118!,M 124!", null, null);
                map.put(mSOCRISCZ, rSOCRISCZ);

                final Major mSOCRPBGZ = new Major(9000, "SOCR-PBGZ-BS", Boolean.FALSE,
                        "Soil and Crop Sciences",
                        MathPlanConstants.CAT + "agricultural-sciences/soil-crop-sciences/soil-crop-sciences-major/",
                        true);
                final MajorMathRequirement rSOCRPBGZ = new MajorMathRequirement("SOCR-PBGZ-BS")
                        .setSemesterCourses("M 117!,M 118!,M 124!", null, null);
                map.put(mSOCRPBGZ, rSOCRPBGZ);

                final Major mSOCRSOEZ = new Major(9000, "SOCR-SOEZ-BS", Boolean.FALSE,
                        "Soil and Crop Sciences",
                        MathPlanConstants.CAT + "agricultural-sciences/soil-crop-sciences/soil-crop-sciences-major/",
                        true);
                final MajorMathRequirement rSOCRSOEZ = new MajorMathRequirement("SOCR-SOEZ-BS")
                        .setSemesterCourses("M 117!,M 118!,M 124!", null, null);
                map.put(mSOCRSOEZ, rSOCRSOEZ);

                final Major mSOWKADSZ2 = new Major(9000, "SOWK-ADSZ-BW", Boolean.FALSE,
                        "Social Work",
                        "Addictions Counseling",
                        MathPlanConstants.CAT + "health-human-sciences/school-of-social-work/"
                                + "social-work-major/addictions-counseling-concentration/",
                        true);
                final MajorMathRequirement rSOWKADSZ2 =
                        new MajorMathRequirement("SOWK-ADSZ-BW")
                                .setSemesterCourses("AUCC3!", null, null);
                map.put(mSOWKADSZ2, rSOWKADSZ2);

                final Major mSPCMTCLZ = new Major(9000, "SPCM-TCLZ-BA", Boolean.FALSE,
                        "Communication Studies",
                        MathPlanConstants.CAT + "liberal-arts/communication-studies/",
                        true);
                final MajorMathRequirement rSPCMTCLZ = new MajorMathRequirement("SPCM-TCLZ-BA")
                        .setSemesterCourses(null, "AUCC3!", null);
                map.put(mSPCMTCLZ, rSPCMTCLZ);

                final Major mTHTRDTHZ = new Major(9000, "THTR-DTHZ-BA", Boolean.FALSE,
                        "Theatre",
                        "Design and Technology",
                        MathPlanConstants.CAT + "liberal-arts/music-theatre-dance/theatre/",
                        true);
                final MajorMathRequirement rTHTRDTHZ = new MajorMathRequirement("THTR-DTHZ-BA")
                        .setSemesterCourses(null, "AUCC3", null);
                map.put(mTHTRDTHZ, rTHTRDTHZ);

                final Major mTHR0 = new Major(9000, "THR0", Boolean.FALSE,
                        "Pre-Theatre",
                        MathPlanConstants.CAT + "liberal-arts/music-theatre-dance/theatre/",
                        true);
                final MajorMathRequirement rTHR0 = new MajorMathRequirement("THR0")
                        .setSemesterCourses(null, "AUCC3", null);
                map.put(mTHR0, rTHR0);

                final Major mUNLA = new Major(9000, "UNLA", Boolean.FALSE,
                        "Undeclared - Liberal Arts",
                        MathPlanConstants.CAT + "liberal-arts/",
                        true);
                final MajorMathRequirement rUNLA = new MajorMathRequirement("UNLA")
                        .setSemesterCourses(null, "AUCC3", null);
                map.put(mUNLA, rUNLA);

                final Major mUSBS = new Major(9000, "USBS", Boolean.FALSE,
                        "Exploratory Studies", // (assuming Business Interest)
                        MathPlanConstants.CAT2 + "/academic-standards/advising/#Exploratory%20Studies%20Advising",
                        true);
                final MajorMathRequirement rUSBS = new MajorMathRequirement("USBS")
                        .setSemesterCourses("M 117,M 118,M 124", null, null);
                map.put(mUSBS, rUSBS);

                final Major mUSBU = new Major(9000, "USBU", Boolean.FALSE,
                        "Exploratory Studies", // (assuming Business Interest)
                        MathPlanConstants.CAT2 + "/academic-standards/advising/#Exploratory%20Studies%20Advising",
                        true);
                final MajorMathRequirement rUSBU = new MajorMathRequirement("USBU")
                        .setSemesterCourses("M 117,M 118,M 124", null, null);
                map.put(mUSBU, rUSBU);

                final Major mUSCS = new Major(9000, "USCS", Boolean.FALSE,
                        "Exploratory Studies", // (assuming Physical Sciences and Engineering)
                        MathPlanConstants.CAT2 + "/academic-standards/advising/#Exploratory%20Studies%20Advising",
                        true);
                final MajorMathRequirement rUSCS = new MajorMathRequirement("USCS")
                        .setSemesterCourses("M 124!,M 126!", MathPlanConstants.CALC1BIO, null);
                map.put(mUSCS, rUSCS);

                final Major mUSEG = new Major(9000, "USEG", Boolean.FALSE,
                        "Exploratory Studies", // (assuming Physical Sciences and Engineering)
                        MathPlanConstants.CAT2 + "/academic-standards/advising/#Exploratory%20Studies%20Advising",
                        true);
                final MajorMathRequirement rUSEG = new MajorMathRequirement("USEG")
                        .setSemesterCourses("M 124!,M 126!", MathPlanConstants.CALC1BIO, null);
                map.put(mUSEG, rUSEG);

                final Major mUSJC = new Major(9000, "USJC", Boolean.FALSE,
                        "Exploratory Studies", // (assuming Journalism and Communication)
                        MathPlanConstants.CAT2 + "/academic-standards/advising/#Exploratory%20Studies%20Advising",
                        true);
                final MajorMathRequirement rUSJC = new MajorMathRequirement("USJC")
                        .setSemesterCourses("AUCC3.", null, null);
                map.put(mUSJC, rUSJC);

                final Major mWSSS = new Major(9000, "WSSS-BS", Boolean.FALSE,
                        "Watershed Science and Sustainability",
                        MathPlanConstants.CAT + "natural-resources/ecosystem-science-sustainability/"
                                + "watershed-science-sustainability-major/",
                        true);
                final MajorMathRequirement rWSSS = new MajorMathRequirement("WSSS-BS")
                        .setSemesterCourses("CALC1BIO!", null, null);
                map.put(mWSSS, rWSSS);

                final Major mWSSSWSDZ = new Major(9000, "WSSS-WSDZ-BS", Boolean.FALSE,
                        "Watershed Science and Sustainability",
                        MathPlanConstants.CAT + "natural-resources/ecosystem-science-sustainability/"
                                + "watershed-science-sustainability-major/",
                        true);
                final MajorMathRequirement rWSSSWSDZ = new MajorMathRequirement("WSSS-WSDZ-BS")
                        .setSemesterCourses("CALC1BIO!", null, null);
                map.put(mWSSSWSDZ, rWSSSWSDZ);

                final Major mWSSSWSSZ = new Major(9000, "WSSS-WSSZ-BS", Boolean.FALSE,
                        "Watershed Science and Sustainability",
                        MathPlanConstants.CAT + "natural-resources/ecosystem-science-sustainability/"
                                + "watershed-science-sustainability-major/",
                        true);
                final MajorMathRequirement rWSSSWSSZ = new MajorMathRequirement("WSSS-WSSZ-BS")
                        .setSemesterCourses("CALC1BIO!", null, null);
                map.put(mWSSSWSSZ, rWSSSWSSZ);

                final Major mWSSSWSUZ = new Major(9000, "WSSS-WSUZ-BS", Boolean.FALSE,
                        "Watershed Science and Sustainability",
                        MathPlanConstants.CAT + "natural-resources/ecosystem-science-sustainability/"
                                + "watershed-science-sustainability-major/",
                        true);
                final MajorMathRequirement rWSSSWSUZ = new MajorMathRequirement("WSSS-WSUZ-BS")
                        .setSemesterCourses("CALC1BIO!", null, null);
                map.put(mWSSSWSUZ, rWSSSWSUZ);

                this.majors = new TreeMap<>(map);
            }

            // Below are not in catalog

//            "IAD0", // Unknown - assume it needs some math...
//            "EGOP", // Unknown - assume it needs some math...
//            "CSOR", // Unknown - assume it needs some math...
//            "N2IE-SI", // Unknown - assume it needs some math...
//            "GUES-CEUG", // Unknown - assume it needs some math...
//            "N2EG-ENGX-UG", // Unknown - assume it needs some math...

//            "GRAD-UG", // Unknown - assume it needs some math...
//            "SPCL-UG", // Unknown - assume it needs some math...
//            "CTED-UG", // Unknown - assume it needs some math...
//            "FCST-UG",  // Unknown - assume it needs some math...
//            "SSAS-UG"  // Unknown - assume it needs some math...

            return Collections.unmodifiableMap(this.majors);
        }
    }

    /**
     * Gets the majors that require only 3 credits of AUCC mathematics.
     *
     * @return the list of majors
     */
    public List<Major> getMajorsRequiringAUCC() {

        synchronized (this.synch) {
            categorizeMajors();
            return Collections.unmodifiableList(this.majorsNeedingAUCC);
        }
    }

    /**
     * Gets the majors that require more than just any 3 credits of AUCC mathematics, but nothing higher level than a
     * pre-calculus course.
     *
     * @return the list of majors
     */
    public List<Major> getMajorsRequiringPrecalc() {

        synchronized (this.synch) {
            categorizeMajors();
            return Collections.unmodifiableList(this.majorsNeedingPrecalc);
        }
    }

    /**
     * Gets the majors that require a Calculus I course, but nothing higher.
     *
     * @return the list of majors
     */
    public List<Major> getMajorsRequiringCalc1() {

        synchronized (this.synch) {
            categorizeMajors();
            return Collections.unmodifiableList(this.majorsNeedingCalc1);
        }
    }

    /**
     * Gets the majors that require courses beyond a Calculus I course.
     *
     * @return the list of majors
     */
    public List<Major> getMajorsRequiringBeyondCalc1() {

        synchronized (this.synch) {
            categorizeMajors();
            return Collections.unmodifiableList(this.majorsNeedingMore);
        }
    }

    /**
     * Generates maps with majors sorted into categories by math requirement.
     */
    private void categorizeMajors() {

        // Called only from a block synchronized on "synch"

        if (this.majorsNeedingPrecalc == null) {
            final Map<Major, MajorMathRequirement> allMajors = getMajors();

            // Go ahead and create all categories, since we're looping and testing anyway
            this.majorsNeedingAUCC = new ArrayList<>(50);
            this.majorsNeedingPrecalc = new ArrayList<>(50);
            this.majorsNeedingCalc1 = new ArrayList<>(50);
            this.majorsNeedingMore = new ArrayList<>(50);

            for (final Map.Entry<Major, MajorMathRequirement> entry : allMajors.entrySet()) {
                final MajorMathRequirement req = entry.getValue();
                final Major key = entry.getKey();

                if (req.isOnlyAUCC3()) {
                    this.majorsNeedingAUCC.add(key);
                } else if (req.isNothingBeyondPrecalc()) {
                    this.majorsNeedingPrecalc.add(key);
                } else if (req.isNothingBeyondCalc1()) {
                    this.majorsNeedingCalc1.add(key);
                } else {
                    this.majorsNeedingMore.add(key);
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

                this.requiredPrereqs = new TreeMap<>();

                this.requiredPrereqs.put(RawRecordConstants.M118,
                        List.of(new RequiredPrereq(RawRecordConstants.M118, Boolean.TRUE, RawRecordConstants.M117,
                                RawRecordConstants.M120)));

                this.requiredPrereqs.put(RawRecordConstants.M124,
                        List.of(new RequiredPrereq(RawRecordConstants.M124, Boolean.TRUE, RawRecordConstants.M118,
                                RawRecordConstants.M120)));

                this.requiredPrereqs.put(RawRecordConstants.M125,
                        List.of(new RequiredPrereq(RawRecordConstants.M125, Boolean.TRUE, RawRecordConstants.M118,
                                RawRecordConstants.M120)));

                this.requiredPrereqs.put(RawRecordConstants.M126,
                        List.of(new RequiredPrereq(RawRecordConstants.M126, Boolean.TRUE, RawRecordConstants.M125)));

                this.requiredPrereqs.put(MathPlanConstants.M_141,
                        List.of(new RequiredPrereq(MathPlanConstants.M_141, Boolean.FALSE, RawRecordConstants.M118,
                                RawRecordConstants.M120)));

                this.requiredPrereqs.put(MathPlanConstants.M_151, List.of(new RequiredPrereq(MathPlanConstants.M_151,
                        Boolean.FALSE,
                        MathPlanConstants.M_141, MathPlanConstants.M_155, MathPlanConstants.M_160)));

                this.requiredPrereqs.put(MathPlanConstants.M_152, List.of(new RequiredPrereq(MathPlanConstants.M_152,
                        Boolean.FALSE,
                        MathPlanConstants.M_141, MathPlanConstants.M_155, MathPlanConstants.M_160)));

                this.requiredPrereqs.put(MathPlanConstants.M_155,
                        Arrays.asList(new RequiredPrereq(MathPlanConstants.M_155, Boolean.FALSE,
                                        RawRecordConstants.M120,
                                        RawRecordConstants.M124, RawRecordConstants.M127),
                                new RequiredPrereq(MathPlanConstants.M_155, Boolean.FALSE, RawRecordConstants.M125,
                                        RawRecordConstants.M127)));

                this.requiredPrereqs.put(MathPlanConstants.M_157,
                        Arrays.asList(new RequiredPrereq(MathPlanConstants.M_157, Boolean.FALSE,
                                        RawRecordConstants.M118,
                                        RawRecordConstants.M120, RawRecordConstants.M127),
                                new RequiredPrereq(MathPlanConstants.M_157, Boolean.TRUE, RawRecordConstants.M124,
                                        RawRecordConstants.M120, RawRecordConstants.M127),
                                new RequiredPrereq(MathPlanConstants.M_157, Boolean.FALSE, RawRecordConstants.M125,
                                        RawRecordConstants.M127),
                                new RequiredPrereq(MathPlanConstants.M_157, Boolean.TRUE, RawRecordConstants.M126,
                                        RawRecordConstants.M127)));

                this.requiredPrereqs.put(MathPlanConstants.M_158,
                        Arrays.asList(new RequiredPrereq(MathPlanConstants.M_158, Boolean.FALSE,
                                        MathPlanConstants.M_151),
                                new RequiredPrereq(MathPlanConstants.M_158, Boolean.FALSE, MathPlanConstants.M_160)));

                this.requiredPrereqs.put(MathPlanConstants.M_159, List.of(new RequiredPrereq(MathPlanConstants.M_159,
                        Boolean.FALSE,
                        MathPlanConstants.M_157)));

                this.requiredPrereqs.put(MathPlanConstants.M_160,
                        Arrays.asList(new RequiredPrereq(MathPlanConstants.M_160, Boolean.FALSE,
                                        RawRecordConstants.M124,
                                        RawRecordConstants.M120, RawRecordConstants.M127),
                                new RequiredPrereq(MathPlanConstants.M_160, Boolean.FALSE, RawRecordConstants.M126,
                                        RawRecordConstants.M127)));

                this.requiredPrereqs.put(MathPlanConstants.M_161,
                        Arrays.asList(new RequiredPrereq(MathPlanConstants.M_161, Boolean.FALSE,
                                        RawRecordConstants.M124,
                                        RawRecordConstants.M120, RawRecordConstants.M127),
                                new RequiredPrereq(MathPlanConstants.M_161, Boolean.FALSE, MathPlanConstants.M_159,
                                        MathPlanConstants.M_160)));

                this.requiredPrereqs.put(MathPlanConstants.M_229,
                        List.of(new RequiredPrereq(MathPlanConstants.M_229, Boolean.FALSE, MathPlanConstants.M_141,
                                MathPlanConstants.M_155, MathPlanConstants.M_160)));

                this.requiredPrereqs.put(MathPlanConstants.M_230, List.of(new RequiredPrereq(MathPlanConstants.M_230,
                        Boolean.FALSE,
                        MathPlanConstants.M_161)));

                this.requiredPrereqs.put(MathPlanConstants.M_235, List.of(new RequiredPrereq(MathPlanConstants.M_235,
                        Boolean.FALSE,
                        MathPlanConstants.M_156, MathPlanConstants.M_161, MathPlanConstants.M_271)));

                this.requiredPrereqs.put(MathPlanConstants.M_255,
                        Arrays.asList(new RequiredPrereq(MathPlanConstants.M_255, Boolean.TRUE,
                                        RawRecordConstants.M126),
                                new RequiredPrereq(MathPlanConstants.M_255, Boolean.FALSE, MathPlanConstants.M_155)));

                this.requiredPrereqs.put(MathPlanConstants.M_256,
                        Arrays.asList(new RequiredPrereq(MathPlanConstants.M_256, Boolean.FALSE,
                                        MathPlanConstants.M_156,
                                        MathPlanConstants.M_161),
                                new RequiredPrereq(MathPlanConstants.M_256, Boolean.FALSE, MathPlanConstants.D_369,
                                        MathPlanConstants.M_369)));

                this.requiredPrereqs.put(MathPlanConstants.M_261, List.of(new RequiredPrereq(MathPlanConstants.M_261,
                        Boolean.FALSE,
                        MathPlanConstants.M_161)));

                this.requiredPrereqs.put(MathPlanConstants.M_269, List.of(new RequiredPrereq(MathPlanConstants.M_261,
                        Boolean.FALSE,
                        RawRecordConstants.M117, RawRecordConstants.M120, RawRecordConstants.M127)));

                this.requiredPrereqs.put(MathPlanConstants.M_271, List.of(new RequiredPrereq(MathPlanConstants.M_271,
                        Boolean.FALSE,
                        MathPlanConstants.M_155, MathPlanConstants.M_159, MathPlanConstants.M_160)));

                this.requiredPrereqs.put(MathPlanConstants.M_272, List.of(new RequiredPrereq(MathPlanConstants.M_272,
                        Boolean.FALSE,
                        MathPlanConstants.M_271)));

                this.requiredPrereqs.put(MathPlanConstants.M_301, List.of(new RequiredPrereq(MathPlanConstants.M_301,
                        Boolean.FALSE,
                        MathPlanConstants.M_161)));

                this.requiredPrereqs.put(MathPlanConstants.M_317,
                        Arrays.asList(new RequiredPrereq(MathPlanConstants.M_317, Boolean.FALSE,
                                        MathPlanConstants.M_156,
                                        MathPlanConstants.M_161),
                                new RequiredPrereq(MathPlanConstants.M_317, Boolean.FALSE, MathPlanConstants.M_230,
                                        MathPlanConstants.M_235)));

                this.requiredPrereqs.put(MathPlanConstants.M_331,
                        Arrays.asList(new RequiredPrereq(MathPlanConstants.M_331, Boolean.TRUE,
                                        MathPlanConstants.M_161),
                                new RequiredPrereq(MathPlanConstants.M_331, Boolean.TRUE, MathPlanConstants.M_229,
                                        MathPlanConstants.D_369, MathPlanConstants.M_369)));

                this.requiredPrereqs.put(MathPlanConstants.M_332, List.of(new RequiredPrereq(MathPlanConstants.M_332,
                        Boolean.FALSE,
                        MathPlanConstants.M_340, MathPlanConstants.M_345)));

                this.requiredPrereqs.put(MathPlanConstants.M_340, List.of(new RequiredPrereq(MathPlanConstants.M_340,
                        Boolean.FALSE,
                        MathPlanConstants.M_255, MathPlanConstants.M_261)));

                this.requiredPrereqs.put(MathPlanConstants.M_345,
                        Arrays.asList(new RequiredPrereq(MathPlanConstants.M_345, Boolean.FALSE,
                                        MathPlanConstants.M_229,
                                        MathPlanConstants.D_369, MathPlanConstants.M_369),
                                new RequiredPrereq(MathPlanConstants.M_345, Boolean.FALSE, MathPlanConstants.M_255,
                                        MathPlanConstants.M_261)));

                this.requiredPrereqs.put(MathPlanConstants.M_348, List.of(new RequiredPrereq(MathPlanConstants.M_348,
                        Boolean.FALSE,
                        MathPlanConstants.M_155, MathPlanConstants.M_160)));

                this.requiredPrereqs.put(MathPlanConstants.M_360,
                        Arrays.asList(new RequiredPrereq(MathPlanConstants.M_360, Boolean.FALSE,
                                        MathPlanConstants.M_229, MathPlanConstants.D_369, MathPlanConstants.M_369),
                                new RequiredPrereq(MathPlanConstants.M_360, Boolean.FALSE, MathPlanConstants.M_156,
                                        MathPlanConstants.M_161)));

                this.requiredPrereqs.put(MathPlanConstants.M_366, List.of(new RequiredPrereq(MathPlanConstants.M_366,
                        Boolean.FALSE, MathPlanConstants.M_156, MathPlanConstants.M_161, MathPlanConstants.M_271)));

                this.requiredPrereqs.put(MathPlanConstants.M_369, List.of(new RequiredPrereq(MathPlanConstants.M_369,
                        Boolean.FALSE, MathPlanConstants.M_156, MathPlanConstants.M_161, MathPlanConstants.M_255,
                        MathPlanConstants.M_271)));

                this.requiredPrereqs.put(MathPlanConstants.D_369, List.of(new RequiredPrereq(MathPlanConstants.D_369,
                        Boolean.FALSE, MathPlanConstants.M_159, MathPlanConstants.M_155, MathPlanConstants.M_156,
                        MathPlanConstants.M_160, MathPlanConstants.M_161)));

                this.requiredPrereqs.put(MathPlanConstants.M_405, List.of(new RequiredPrereq(MathPlanConstants.M_405,
                        Boolean.FALSE, MathPlanConstants.M_360, MathPlanConstants.M_366)));

                this.requiredPrereqs.put(MathPlanConstants.M_417,
                        Arrays.asList(new RequiredPrereq(MathPlanConstants.M_417, Boolean.FALSE,
                                        MathPlanConstants.D_369,
                                        MathPlanConstants.M_369),
                                new RequiredPrereq(MathPlanConstants.M_417, Boolean.FALSE, MathPlanConstants.M_317)));

                this.requiredPrereqs.put(MathPlanConstants.M_418, List.of(new RequiredPrereq(MathPlanConstants.M_418,
                        Boolean.FALSE,
                        MathPlanConstants.M_417)));

                this.requiredPrereqs.put(MathPlanConstants.M_419, List.of(new RequiredPrereq(MathPlanConstants.M_419,
                        Boolean.FALSE,
                        MathPlanConstants.M_261)));

                this.requiredPrereqs.put(MathPlanConstants.M_425,
                        Arrays.asList(new RequiredPrereq(MathPlanConstants.M_425, Boolean.FALSE,
                                        MathPlanConstants.M_317),
                                new RequiredPrereq(MathPlanConstants.M_425, Boolean.FALSE, MathPlanConstants.M_366),
                                new RequiredPrereq(MathPlanConstants.M_425, Boolean.FALSE, MathPlanConstants.D_369,
                                        MathPlanConstants.M_369)));

                this.requiredPrereqs.put(MathPlanConstants.M_430, List.of(new RequiredPrereq(MathPlanConstants.M_430,
                        Boolean.FALSE,
                        MathPlanConstants.M_340, MathPlanConstants.M_345)));

                this.requiredPrereqs.put(MathPlanConstants.M_435,
                        Arrays.asList(new RequiredPrereq(MathPlanConstants.M_435, Boolean.FALSE,
                                        MathPlanConstants.M_229,
                                        MathPlanConstants.D_369, MathPlanConstants.M_369),
                                new RequiredPrereq(MathPlanConstants.M_435, Boolean.FALSE, MathPlanConstants.M_340,
                                        MathPlanConstants.M_345)));

                this.requiredPrereqs.put(MathPlanConstants.M_450, List.of(new RequiredPrereq(MathPlanConstants.M_450,
                        Boolean.FALSE,
                        MathPlanConstants.M_255, MathPlanConstants.M_261)));

                this.requiredPrereqs.put(MathPlanConstants.M_451, List.of(new RequiredPrereq(MathPlanConstants.M_451,
                        Boolean.FALSE,
                        MathPlanConstants.M_340, MathPlanConstants.M_345)));

                this.requiredPrereqs.put(MathPlanConstants.M_455,
                        List.of(new RequiredPrereq(MathPlanConstants.M_455, Boolean.FALSE, MathPlanConstants.M_255,
                                MathPlanConstants.M_340, MathPlanConstants.M_345, MathPlanConstants.M_348)));

                this.requiredPrereqs.put(MathPlanConstants.M_460,
                        Arrays.asList(new RequiredPrereq(MathPlanConstants.M_460, Boolean.FALSE,
                                        MathPlanConstants.M_360,
                                        MathPlanConstants.M_366),
                                new RequiredPrereq(MathPlanConstants.M_460, Boolean.FALSE, MathPlanConstants.D_369,
                                        MathPlanConstants.M_369)));

                this.requiredPrereqs.put(MathPlanConstants.M_463,
                        Arrays.asList(new RequiredPrereq(MathPlanConstants.M_463, Boolean.FALSE,
                                        MathPlanConstants.M_161),
                                new RequiredPrereq(MathPlanConstants.M_463, Boolean.FALSE, MathPlanConstants.D_369,
                                        MathPlanConstants.M_369)));

                this.requiredPrereqs.put(MathPlanConstants.M_466,
                        List.of(new RequiredPrereq(MathPlanConstants.M_466, Boolean.FALSE, MathPlanConstants.M_235,
                                MathPlanConstants.M_360, MathPlanConstants.M_366)));

                this.requiredPrereqs.put(MathPlanConstants.M_467,
                        Arrays.asList(new RequiredPrereq(MathPlanConstants.M_467, Boolean.FALSE,
                                        MathPlanConstants.M_466),
                                new RequiredPrereq(MathPlanConstants.M_467, Boolean.TRUE, MathPlanConstants.D_369,
                                        MathPlanConstants.M_369)));

                this.requiredPrereqs.put(MathPlanConstants.M_469, List.of(new RequiredPrereq(MathPlanConstants.M_469,
                                Boolean.FALSE,
                                MathPlanConstants.M_161),
                        new RequiredPrereq(MathPlanConstants.M_469, Boolean.FALSE, MathPlanConstants.D_369,
                                MathPlanConstants.M_369)));

                this.requiredPrereqs.put(MathPlanConstants.M_470,
                        Arrays.asList(new RequiredPrereq(MathPlanConstants.M_470, Boolean.FALSE,
                                        MathPlanConstants.M_229,
                                        MathPlanConstants.D_369, MathPlanConstants.M_369),
                                new RequiredPrereq(MathPlanConstants.M_470, Boolean.FALSE, MathPlanConstants.M_261)));

                this.requiredPrereqs.put(MathPlanConstants.M_472, List.of(new RequiredPrereq(MathPlanConstants.M_472,
                        Boolean.FALSE,
                        MathPlanConstants.M_317)));

                this.requiredPrereqs.put(MathPlanConstants.M_474,
                        Arrays.asList(new RequiredPrereq(MathPlanConstants.M_474, Boolean.FALSE,
                                        MathPlanConstants.M_261),
                                new RequiredPrereq(MathPlanConstants.M_474, Boolean.FALSE, MathPlanConstants.D_369,
                                        MathPlanConstants.M_369)));
            }

            return this.requiredPrereqs;
        }
    }

    /**
     * Retrieves a cached student data record. If no record is cached for the student (or the cached record has
     * expired), the student record is queried and a new {@code StudentData} object is created and cached.
     *
     * @param cache           the data cache
     * @param studentId       the student ID
     * @param now             the date/time to consider "now"
     * @param loginSessionTag the login session tag
     * @param writeChanges    {@code true} to write profile value changes (used when the student is accessing the site);
     *                        {@code false} to skip writing changes (used when an administrator or adviser is acting as
     *                        a student)
     * @return the student data object; {@code null} if there is no cached data and the student record cannot be
     *         queried
     * @throws SQLException if there is an error accessing the database
     */
    public MathPlanStudentData getStudentData(final Cache cache, final String studentId, final ZonedDateTime now,
                                              final long loginSessionTag, final boolean writeChanges) throws SQLException {

        synchronized (this.synch) {
            expireCache();

            MathPlanStudentData result = this.studentDataCache.get(studentId);

            if (result == null) {
                final RawStudent student = RawStudentLogic.query(cache, studentId, true);

                if (student != null) {
                    result = new MathPlanStudentData(cache, student, this, now, loginSessionTag,
                            writeChanges);
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

        final Iterator<MathPlanStudentData> iter = this.studentDataCache.values().iterator();
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
                    final ILiveCsuCredit impl = ImplLiveCsuCredit.INSTANCE;
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
            final int size = list.size();
            result = new ArrayList<>(size);

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

                final ILiveTransferCredit impl = ImplLiveTransferCredit.INSTANCE;
                result = impl.query(bannerConn, studentId);
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
     * Tests whether the student identified by a PIDM has completed their math plan.
     *
     * @param cache the data cache
     * @param pidm  the PIDM
     * @return the student ID if the student has completed their math plan, {@code null} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static String getMathPlanStatus(final Cache cache, final int pidm) throws SQLException {

        final Integer pidmObj = Integer.valueOf(pidm);
        final List<RawStmathplan> responses = RawStmathplanLogic.queryLatestByStudentPage(cache, pidmObj,
                MathPlanConstants.INTENTIONS_PROFILE);

        String studentId = null;

        if (!responses.isEmpty()) {
            studentId = responses.getFirst().stuId;
        }

        return studentId;
    }

    /**
     * Tests whether the student identified by a PIDM has completed the Math Placement process.
     *
     * @param cache     the data cache
     * @param studentId the student ID
     * @return 0 if the student's Math Plan indicates they do not need to complete placement; 1 if the student should
     *         complete math placement but has not yet done so; 2 is math placement has been completed
     * @throws SQLException if there is an error accessing the database
     */
    public static MathPlanPlacementStatus getMathPlacementStatus(final Cache cache, final String studentId)
            throws SQLException {

        boolean planSaysPlacementNeeded;
        boolean satisfiedByPlacement = false;
        boolean satisfiedByTransfer = false;
        boolean satisfiedByCourse = false;

        // See of their latest recommendation was "3 cr. of Core Math*"
        final Map<Integer, RawStmathplan> profile = getMathPlanResponses(cache, studentId,
                MathPlanConstants.PLAN_PROFILE);
        final RawStmathplan rec = profile.get(MathPlanConstants.TWO);

        if (rec == null || rec.stuAnswer == null) {
            planSaysPlacementNeeded = true;
        } else if (rec.stuAnswer.startsWith("(none)")) {
            planSaysPlacementNeeded = false;
        } else if (rec.stuAnswer.startsWith("3 cr. of Core")) {
            planSaysPlacementNeeded = false;
        } else if (rec.stuAnswer.startsWith("2 cr. of Core")) {
            planSaysPlacementNeeded = false;
        } else {
            planSaysPlacementNeeded = !rec.stuAnswer.startsWith("1 cr. of Core");
        }

        final List<RawStmpe> attempts = RawStmpeLogic.queryLegalByStudent(cache, studentId);
        if (!attempts.isEmpty()) {
            satisfiedByPlacement = true;
            planSaysPlacementNeeded = false;
        }

        if (!satisfiedByPlacement) {
            final List<RawFfrTrns> xfers = RawFfrTrnsLogic.queryByStudent(cache, studentId);
            for (final RawFfrTrns xfer : xfers) {
                if (RawRecordConstants.M117.equals(xfer.course)
                        || RawRecordConstants.M118.equals(xfer.course)
                        || RawRecordConstants.M124.equals(xfer.course)
                        || RawRecordConstants.M125.equals(xfer.course)
                        || RawRecordConstants.M126.equals(xfer.course)
                        || "M 160".equals(xfer.course)
                        || "M 155".equals(xfer.course)
                        || "M 141".equals(xfer.course)
                        || "M 120".equals(xfer.course)
                        || "M 127".equals(xfer.course)
                        || "M 161".equals(xfer.course)
                        || RawRecordConstants.M002.equals(xfer.course)) {
                    // M 002 is a community college course that clears prereqs for 117
                    satisfiedByTransfer = true;
                    planSaysPlacementNeeded = false;
                    break;
                }
            }
        }

        if (!satisfiedByTransfer) {
            final List<RawStcourse> regs = RawStcourseLogic.queryByStudent(cache, studentId, false, false);
            for (final RawStcourse reg : regs) {
                if ("Y".equals(reg.completed)) {
                    if (RawRecordConstants.M117.equals(reg.course)
                            || RawRecordConstants.M118.equals(reg.course)
                            || RawRecordConstants.M124.equals(reg.course)
                            || RawRecordConstants.M125.equals(reg.course)
                            || RawRecordConstants.M126.equals(reg.course)
                            || RawRecordConstants.MATH117.equals(reg.course)
                            || RawRecordConstants.MATH118.equals(reg.course)
                            || RawRecordConstants.MATH124.equals(reg.course)
                            || RawRecordConstants.MATH125.equals(reg.course)
                            || RawRecordConstants.MATH126.equals(reg.course)) {
                        satisfiedByCourse = true;
                        planSaysPlacementNeeded = false;
                        break;
                    }
                }
            }
        }

        final MathPlanPlacementStatus result;

        if (satisfiedByPlacement) {
            result = new MathPlanPlacementStatus(planSaysPlacementNeeded, true,
                    EHowSatisfiedPlacement.MATH_PLACEMENT_COMPLETED);
        } else if (satisfiedByTransfer) {
            result = new MathPlanPlacementStatus(planSaysPlacementNeeded, true, EHowSatisfiedPlacement.TRANSFER_CREDIT);
        } else if (satisfiedByCourse) {
            result = new MathPlanPlacementStatus(planSaysPlacementNeeded, true, EHowSatisfiedPlacement.COURSE_CREDIT);
        } else {
            result = new MathPlanPlacementStatus(planSaysPlacementNeeded, false, null);
        }

        return result;
    }

    /**
     * Deletes all responses for a student for a specific page.
     *
     * @param cache           the data cache
     * @param student         the student
     * @param pageId          the page ID
     * @param now             the date/time to consider "now"
     * @param loginSessionTag the login session tag
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public boolean deleteMathPlanResponses(final Cache cache, final RawStudent student, final String pageId,
                                           final ZonedDateTime now, final long loginSessionTag) throws SQLException {

        final String studentId = student.stuId;

        final boolean result = RawStmathplanLogic.deleteAllForPage(cache, studentId, pageId);

        if (result) {
            synchronized (this.synch) {
                // Rebuild student data
                this.studentDataCache.put(student.stuId, new MathPlanStudentData(cache, student, this, now,
                        loginSessionTag, false));
            }
        }

        return result;
    }

    /**
     * Stores a set of profile answers and updates the cached student plan based on the new profile responses.
     *
     * @param cache           the data cache
     * @param student         the student
     * @param pageId          the page ID
     * @param questions       the question numbers
     * @param answers         the answers
     * @param now             the date/time to consider "now"
     * @param loginSessionTag a unique tag for a login session
     * @throws SQLException if there is an error accessing the database
     */
    public void storeMathPlanResponses(final Cache cache, final RawStudent student, final String pageId,
                                       final List<Integer> questions, final List<String> answers,
                                       final ZonedDateTime now, final long loginSessionTag) throws SQLException {

        final LocalDateTime when = now.toLocalDateTime();
        final Integer finishTime = Integer.valueOf(TemporalUtils.minuteOfDay(when));

        final String aplnTermStr = student.aplnTerm == null ? null : student.aplnTerm.shortString;

        // Dummy record to test for existing
        RawStmathplan resp = new RawStmathplan(student.stuId, student.pidm, aplnTermStr, pageId, when.toLocalDate(),
                MathPlanConstants.ZERO, CoreConstants.EMPTY, finishTime, Long.valueOf(loginSessionTag));

        // Query for any existing answers with the same date and finish time
        final List<RawStmathplan> latest =
                RawStmathplanLogic.queryLatestByStudentPage(cache, student.stuId, pageId);
        final LocalDate today = now.toLocalDate();
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
                    ans, finishTime, Long.valueOf(loginSessionTag));

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
            this.studentDataCache.put(student.stuId, new MathPlanStudentData(cache, student, this, now, loginSessionTag,
                    false));
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

                final String status1 = getMathPlanStatus(cache, 12370959);
                Log.info("Student 836959005 plan status: " + status1);

                final MathPlanPlacementStatus status2 = getMathPlacementStatus(cache, "836959005");
                Log.info("Student 836959005 placement status: ");
                Log.info("    Placement needed:   ", status2.isPlacementNeeded);
                Log.info("    Placement complete: ", status2.isPlacementComplete);
                Log.info("    How satisfied:      ", status2.howSatisfied);

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
        }
    }
}
