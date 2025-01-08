package dev.mathops.dbjobs.batch;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Determines the list of students who are eligible for the Winter Precalculus Tutorial. This is students who:
 *
 * <ul>
 * <li>Were registered in a course in Fall but did not pass that course.
 * <li>Attempted the Unit 1 review exam in that course.
 * </ul>
 */
final class DetermineWinterPrecalcElig {

    /** A commonly used value. */
    private static final Integer UNIT1 = Integer.valueOf(1);

    /** The database profile through which to access the database. */
    private final DbProfile fa21Profile;

    /** The data database context. */
    private final DbContext fa21Ctx;

    /** The database profile through which to access the database. */
    private final DbProfile prodProfile;

    /** The data database context. */
    private final DbContext prodCtx;

    /**
     * Constructs a new {@code DetermineWinterPrecalcElig}.
     */
    private DetermineWinterPrecalcElig() {

        final ContextMap map = ContextMap.getDefaultInstance();

        this.fa21Profile = map.getCodeProfile(Contexts.FA21);
        this.fa21Ctx = this.fa21Profile.getDbContext(ESchemaUse.PRIMARY);

        this.prodProfile = map.getCodeProfile(Contexts.BATCH_PATH);
        this.prodCtx = this.prodProfile.getDbContext(ESchemaUse.PRIMARY);
    }

    /**
     * Executes the job.
     */
    private void execute() {

        if ((this.fa21Profile == null) || (this.fa21Ctx == null)) {
            Log.warning("Unable to create fa21 context.");
        } else if ((this.prodProfile == null) || (this.prodCtx == null)) {
            Log.warning("Unable to create production context.");
        } else {
            try {
                final DbConnection connfa21 = this.fa21Ctx.checkOutConnection();

                try {
                    final Cache cachefa21 = new Cache(this.fa21Profile, connfa21);

                    final DbConnection connProd = this.prodCtx.checkOutConnection();

                    try {
                        final Cache cacheProd = new Cache(this.prodProfile, connProd);
                        exec(cachefa21, cacheProd);
                    } finally {
                        this.prodCtx.checkInConnection(connProd);
                    }
                } finally {
                    this.fa21Ctx.checkInConnection(connfa21);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
            }
        }
    }

    /**
     * Executes the job.
     *
     * @param cachefa21 the data cache for the FA21 database
     * @param cacheProd the data cache for the production database
     * @throws SQLException if there is an error accessing the database
     */
    private static void exec(final Cache cachefa21, final Cache cacheProd) throws SQLException {

        final TermKey term = new TermKey("FA21");
        final List<RawStcourse> regs = RawStcourseLogic.queryByTerm(cachefa21, term, false, false);

        Log.info("Found " + regs.size() + " regs");

        // Remove those that were passed, count the rest
        final Map<String, Integer> map = new HashMap<>(10);
        final Iterator<RawStcourse> iter = regs.iterator();
        while (iter.hasNext()) {
            final RawStcourse reg = iter.next();

            String grade = reg.courseGrade;
            if (!"U".equals(grade)) {
                iter.remove();
            }
            if (grade == null) {
                Log.info("Null grade for ", reg.stuId, " in ", reg.course);
                grade = CoreConstants.SPC;
            }
            final Integer count = map.get(grade);
            if (count == null) {
                map.put(grade, Integer.valueOf(1));
            } else {
                map.put(grade, Integer.valueOf(count.intValue() + 1));
            }
        }

        Log.info(regs.size() + " regs without passing grades:");
        for (final Map.Entry<String, Integer> entry : map.entrySet()) {
            Log.info("    ", entry.getKey(), ": ", entry.getValue());
        }

        final List<RawStcourse> regsPassedUnit1Exam = new ArrayList<>(regs.size() / 2);
        final List<RawStcourse> regsTriedUnit1Review = new ArrayList<>(regs.size() / 2);

        // For each reg, see if the student attempted the unit 1 review
        for (final RawStcourse reg : regs) {

            final List<RawStexam> exams = RawStexamLogic.queryByStudentCourse(cachefa21, reg.stuId, reg.course,
                    false);

            boolean passedUnit1Exam = false;
            boolean triedUnit1Review = false;
            for (final RawStexam exam : exams) {
                if (!UNIT1.equals(exam.unit)) {
                    continue;
                }

                if ("U".equals(exam.examType) && "Y".equals(exam.passed)) {
                    passedUnit1Exam = true;
                } else if ("R".equals(exam.examType)) {
                    triedUnit1Review = true;
                }
            }

            if (passedUnit1Exam) {
                regsPassedUnit1Exam.add(reg);
            } else if (triedUnit1Review) {
                regsTriedUnit1Review.add(reg);
            }
        }

        Log.info("There are  " + regsPassedUnit1Exam.size() + " who passed unit 1 exam");
        Log.info("There are  " + regsTriedUnit1Review.size()
                + " who tried unit 1 review but did not pass unit 1 exam");

        // Compare these lists to the authorized special stus
        final List<RawSpecialStus> specials = RawSpecialStusLogic.queryAll(cacheProd);

        final Iterator<RawSpecialStus> iter2 = specials.iterator();
        while (iter2.hasNext()) {
            final String type = iter2.next().stuType.trim();

            if ("M 1170W".equals(type) || "M 1180W".equals(type) || "M 1240W".equals(type) || "M 1250W".equals(type)
                    || "M 1260W".equals(type)) {
                continue;
            }
            iter2.remove();
        }

        final List<RawStcourse> toAdd = new ArrayList<>(regs.size());

        Log.info("There are  " + specials.size() + " special student records");

        // See who should get a new special_stus row...
        for (final RawStcourse reg : regsPassedUnit1Exam) {
            boolean searching = true;
            final String type = reg.course + "0W";
            for (final RawSpecialStus test : specials) {
                if (reg.stuId.equals(test.stuId) && type.equals(test.stuType)) {
                    searching = false;
                    break;
                }
            }

            if (searching) {
                toAdd.add(reg);
            }
        }

        // See who should get a new special_stus row...
        for (final RawStcourse reg : regsTriedUnit1Review) {
            boolean searching = true;
            final String type = reg.course + "0W";
            for (final RawSpecialStus test : specials) {
                if (reg.stuId.equals(test.stuId) && type.equals(test.stuType)) {
                    searching = false;
                    break;
                }
            }

            if (searching) {
                toAdd.add(reg);
            }
        }

        Log.info("Need to add " + toAdd.size() + " special student records");

        for (final RawStcourse reg : toAdd) {
            final RawSpecialStus newRow = new RawSpecialStus(reg.stuId, reg.course + "0W", null, null);
            RawSpecialStusLogic.insert(cacheProd, newRow);
        }

        Log.fine("Last Name          First Name    Student ID   Course ");
        Log.fine(CoreConstants.EMPTY);
        final HtmlBuilder line = new HtmlBuilder(100);

        for (final RawStcourse reg : toAdd) {
            final RawStudent stu = RawStudentLogic.query(cacheProd, reg.stuId, false);

            final int lastLen = stu.lastName.length();
            if (lastLen >= 18) {
                line.add(stu.lastName.substring(0, 18));
            } else {
                line.add(stu.lastName);
                for (int i = lastLen; i < 18; ++i) {
                    line.add(' ');
                }
            }
            line.add(' ');

            final int firstLen = stu.firstName.length();
            if (firstLen >= 13) {
                line.add(stu.firstName.substring(0, 13));
            } else {
                line.add(stu.firstName);
                for (int i = firstLen; i < 13; ++i) {
                    line.add(' ');
                }
            }
            line.add(' ');

            line.add(reg.stuId);
            line.add("   MATH ");
            line.add(reg.course.substring(2));

            Log.fine(line.toString());
            line.reset();
        }

        Log.fine(CoreConstants.EMPTY);
        Log.fine(CoreConstants.EMPTY);
        Log.fine(CoreConstants.EMPTY);

        for (final RawStcourse reg : toAdd) {
            Log.fine(reg.stuId);
        }
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        final DetermineWinterPrecalcElig job = new DetermineWinterPrecalcElig();

        job.execute();
    }
}
