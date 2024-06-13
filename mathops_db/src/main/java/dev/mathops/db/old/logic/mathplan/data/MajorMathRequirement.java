package dev.mathops.db.old.logic.mathplan.data;

import dev.mathops.commons.CoreConstants;
import dev.mathops.db.old.rawrecord.RawRecordConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A math requirement associated with a major (major or major/concentration).
 *
 * <p>
 * Requirements are lists of strings of comma-delimited tokens. Each string represents one option, where students should
 * choose one option to take. Once an option is selected, students should take all courses indicated by that group code.
 * Each token in one of these strings is one of the following:
 * <ul>
 * <li>A course group code (meaning students typically take the course(s) during the semester)
 * <li>A course group code followed by '.' (meaning the course is marked as "recommended")
 * <li>A course group code followed by '!' (meaning the course is marked as "critical")
 * </ul>
 */
public final class MajorMathRequirement {

    /** The program code from CIM (like 'MAJR-CONC-BS'). */
    public String programCode;

    /** The semester 1 critical requirements, a list of CourseGroups keys. */
    final List<String> sem1Critical;

    /** The semester 1 recommended requirements, a list of CourseGroups keys. */
    final List<String> sem1Recommended;

    /** The semester 1 other requirements, a list of CourseGroups keys. */
    final List<String> sem1Typical;

    /** The semester 2 critical requirements, a list of CourseGroups keys. */
    final List<String> sem2Critical;

    /** The semester 2 recommended requirements, a list of CourseGroups keys. */
    final List<String> sem2Recommended;

    /** The semester 2 other requirements, a list of CourseGroups keys. */
    final List<String> sem2Typical;

    /** Additional requirements, a list of CourseGroups keys. */
    public final List<String> additional;

    /** Additional requirements, a list of CourseGroups keys. */
    private final List<String> all;

    /**
     * Constructs a new {@code MajorMathRequirement}.
     */
    private MajorMathRequirement() {

        this.sem1Critical = new ArrayList<>(2);
        this.sem1Recommended = new ArrayList<>(2);
        this.sem1Typical = new ArrayList<>(2);
        this.sem2Critical = new ArrayList<>(2);
        this.sem2Recommended = new ArrayList<>(2);
        this.sem2Typical = new ArrayList<>(2);
        this.additional = new ArrayList<>(2);
        this.all = new ArrayList<>(5);
    }

    /**
     * Constructs a new {@code MajorMathRequirement}.
     *
     * @param theProgramCode the program code
     */
    public MajorMathRequirement(final String theProgramCode) {

        this();

        this.programCode = theProgramCode;
    }

    /**
     * Sets the semester requirements from three strings.
     *
     * @param semester1 the semester 1 string
     * @param semester2 the semester 2 string
     * @param semester3 the string with requirements beyond semester 2
     * @return this object
     */
    public MajorMathRequirement setSemesterCourses(final String semester1, final String semester2,
                                                   final String semester3) {

        if (semester1 != null) {
            final String[] e1 = semester1.split(CoreConstants.COMMA);
            for (final String entry : e1) {
                final int len = entry.length();
                if (!entry.isEmpty() && (int) entry.charAt(len - 1) == '!') {
                    final String substring = entry.substring(0, len - 1);
                    this.sem1Critical.add(substring);
                } else if (entry.endsWith(CoreConstants.DOT)) {
                    final String substring = entry.substring(0, len - 1);
                    this.sem1Recommended.add(substring);
                } else {
                    this.sem1Typical.add(entry);
                }
            }
        }

        if (semester2 != null) {
            final String[] e2 = semester2.split(CoreConstants.COMMA);
            for (final String entry : e2) {
                final int len = entry.length();
                if (!entry.isEmpty() && (int) entry.charAt(len - 1) == '!') {
                    final String substring = entry.substring(0, len - 1);
                    this.sem2Critical.add(substring);
                } else if (entry.endsWith(CoreConstants.DOT)) {
                    final String substring = entry.substring(0, len - 1);
                    this.sem2Recommended.add(substring);
                } else {
                    this.sem2Typical.add(entry);
                }
            }
        }

        if (semester3 != null) {
            final String[] e3 = semester3.split(CoreConstants.COMMA);
            this.additional.addAll(Arrays.asList(e3));
        }

        return this;
    }

    /**
     * Tests whether the major requires only 3 credits of AUCC mathematics.
     *
     * @return {@code true} if the major requires only 3 credits AUCC math
     */
    public boolean isOnlyAUCC3() {

        populateAll();

        boolean onlyAUCC = true;

        for (final String s : this.all) {
            if ("AUCC2".equals(s) || "AUCC3".equals(s) || "AUCC3SOC".equals(s)) {
                continue;
            }
            onlyAUCC = false;
            break;
        }

        return onlyAUCC;
    }

    /**
     * Tests whether the major requires no mathematics beyond pre-calculus.
     *
     * @return {@code true} if the major requires no mathematics beyond pre-calculus
     */
    public boolean isNothingBeyondPrecalc() {

        populateAll();

        boolean onlyPrecalc = true;

        for (final String s : this.all) {
            if ("AUCC2".equals(s) || "AUCC3".equals(s) || "AUCC3SOC".equals(s) || "AGED3A".equals(s)
                    || "AGED3B".equals(s) || "ANIM3".equals(s) || "BIOM1".equals(s) || "BIOM2".equals(s)
                    || "BIOM3".equals(s) || "BUSA3".equals(s) || "FRRS3".equals(s) || "M 101".equals(s)
                    || "S 100".equals(s) || RawRecordConstants.M117.equals(s) || RawRecordConstants.M118.equals(s)
                    || RawRecordConstants.M124.equals(s) || RawRecordConstants.M125.equals(s)
                    || RawRecordConstants.M126.equals(s) || RawRecordConstants.M120.equals(s)
                    || RawRecordConstants.M127.equals(s)) {
                continue;
            }
            onlyPrecalc = false;
            break;
        }

        return onlyPrecalc;
    }

    /**
     * Tests whether the major requires no mathematics beyond calculus I.
     *
     * @return {@code true} if the major requires no mathematics beyond calculus I
     */
    public boolean isNothingBeyondCalc1() {

        populateAll();

        boolean onlyCalc1 = true;

        for (final String s : this.all) {
            if ("AUCC2".equals(s) || "AUCC3".equals(s) || "AUCC3SOC".equals(s) || "AGED3A".equals(s)
                    || "AGED3B".equals(s) || "ANIM3".equals(s) || "BIOM1".equals(s) || "BIOM2".equals(s)
                    || "BIOM3".equals(s) || "BUSA3".equals(s) || "FRRS3".equals(s) || "M 101".equals(s)
                    || "S 100".equals(s) || RawRecordConstants.M117.equals(s) || RawRecordConstants.M118.equals(s)
                    || RawRecordConstants.M124.equals(s) || RawRecordConstants.M125.equals(s)
                    || RawRecordConstants.M126.equals(s) || RawRecordConstants.M120.equals(s)
                    || RawRecordConstants.M127.equals(s) || "ECON".equals(s) || "CALC".equals(s)
                    || "CALC1BIO".equals(s) || "CALC1CS".equals(s) || "M 141".equals(s) || "M 155".equals(s)
                    || "M 160".equals(s)) {
                continue;
            }
            onlyCalc1 = false;
            break;
        }

        return onlyCalc1;
    }

    /**
     * Populates an aggregate list of all requirements.
     */
    private void populateAll() {

        if (this.all.isEmpty()) {
            this.all.addAll(this.sem1Critical);
            this.all.addAll(this.sem1Recommended);
            this.all.addAll(this.sem1Typical);
            this.all.addAll(this.sem2Critical);
            this.all.addAll(this.sem2Recommended);
            this.all.addAll(this.sem2Typical);
            this.all.addAll(this.additional);
        }
    }

    /**
     * Generates the string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return this.programCode;
    }
}
