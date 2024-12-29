package dev.mathops.dbjobs.report.analytics.longitudinal.data;

import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.json.JSONObject;

/**
 * A container for the data about a single course enrollment.
 *
 * @param studentId      the student ID
 * @param academicPeriod the  academic period, such as "202410" for Spring, 2024
 * @param course         the course, such as "MATH117"
 * @param section        the section number, such as "001"
 * @param flags          the set of flags
 * @param grade          the student's final grade (null if no grade earned)
 * @param gradeValue     the numerical grade value on a 4.0 point scale (null if no grade earned)
 */
public record EnrollmentRec(String studentId, int academicPeriod, String course, String section,
                            int flags, String grade, Double gradeValue) {

    /** Flag whose presence indicates this is transfer credit. */
    public static final int TRANSFER = 0x0001;

    /** Flag whose presence indicates this is AP/IB/CLEP credit. */
    public static final int AP_IB_CLEP = 0x0002;

    /** Flag whose presence indicates the course was attempted. */
    public static final int ATTEMPTED = 0x0004;

    /** Flag whose presence indicates the student withdrew from the course. */
    public static final int WITHDRAWN = 0x0008;

    /** Flag whose presence indicates the course was passed. */
    public static final int PASSED = 0x0010;

    /** Flag whose presence indicates the course was failed. */
    public static final int FAILED = 0x0020;

    /** Flag whose presence indicates the course section is gradable. */
    public static final int GRADABLE = 0x0040;

    /**
     * Tests whether flags indicate this is transfer credit.
     *
     * @return true if transfer credit
     */
    public boolean isTransfer() {

        return (this.flags & TRANSFER) == TRANSFER;
    }

    /**
     * Tests whether flags indicate this is AP/IB/CLEP credit.
     *
     * @return true if AP/IB/CLEP
     */
    public boolean isApIbClep() {

        return (this.flags & AP_IB_CLEP) == AP_IB_CLEP;
    }

    /**
     * Tests whether flags indicate the course was attempted.
     *
     * @return true if course was attempted
     */
    boolean isAttempted() {

        return (this.flags & ATTEMPTED) == ATTEMPTED;
    }

    /**
     * Tests whether flags indicate student withdrew from course.
     *
     * @return true if course was withdrawn
     */
    public boolean isWithdraw() {

        return (this.flags & WITHDRAWN) == WITHDRAWN;
    }

    /**
     * Tests whether flags indicate student passed the course.
     *
     * @return true if course was passed
     */
    public boolean isPassed() {

        return (this.flags & PASSED) == PASSED;
    }

    /**
     * Tests whether flags indicate student failed the course.
     *
     * @return true if course was failed
     */
    boolean isFailed() {
        return (this.flags & FAILED) == FAILED;
    }

    /**
     * Tests whether flags indicate student passed the course.
     *
     * @return true if the course section is gradable
     */
    public boolean isGradable() {

        return (this.flags & GRADABLE) == GRADABLE;
    }

    /**
     * Tests whether this enrollment attempt is a "DFW'
     *
     * @return true if the course section is gradable
     */
    public boolean isDfw() {

        final boolean dfw;

        if (isTransfer() || isApIbClep()) {
            dfw = false;
        } else {
            dfw = isWithdraw() || this.gradeValue == null || this.gradeValue.doubleValue() < 1.5;
        }

        return dfw;
    }

    /**
     * Attempts to parse a {@code EnrollmentRecord} from a JSON object.
     *
     * @param json the JSON object
     * @return the parsed record
     * @throws IllegalArgumentException if the object could not be interpreted
     */
    public static EnrollmentRec parse(final JSONObject json) {

        final String id = json.getStringProperty("i");
        final Double pe = json.getNumberProperty("p");
        final String co = json.getStringProperty("c");
        final String se = json.getStringProperty("s");
        final Double fl = json.getNumberProperty("f");
        final String gr = json.getStringProperty("g");
        final Double gv = json.getNumberProperty("v");

        final double peDbl = pe == null ? 0.0 : pe.doubleValue();
        final int peInt = (int) Math.round(peDbl);

        final double flDbl = fl == null ? 0.0 : fl.doubleValue();
        final int flInt = (int) Math.round(flDbl);

        return new EnrollmentRec(id, peInt, co, se, flInt, gr, gv);
    }

    /**
     * Generates the JSON representation of the record.
     *
     * @return the JSON representation
     */
    public String toJson() {

        final String flagsStr = Integer.toString(this.flags);
        final String periodStr = Integer.toString(this.academicPeriod);

        final HtmlBuilder builder = new HtmlBuilder(100);

        builder.add("{\"i\":\"", this.studentId, "\",",
                "\"p\":", periodStr, ",",
                "\"c\":\"", this.course, "\",",
                "\"s\":\"", this.section, "\",",
                "\"f\":", flagsStr);

        if (grade() != null) {
            builder.add(",\"g\":\"", this.grade, "\"");
        }
        if (gradeValue() != null) {
            builder.add(",\"v\":", this.gradeValue);
        }
        builder.add("}");

        return builder.toString();
    }
}

