package dev.mathops.dbjobs.report.analytics.longitudinal.data;

import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.json.JSONObject;

/**
 * A container for the data about a single student.
 *
 * @param studentId the student ID
 * @param gender    the gender
 * @param ethnicity flags indicating student's ethnicity
 */
public record StudentRec(String studentId, String gender, int ethnicity) {

    /** Flag whose presence indicates student has Hispanic/Latino ethnicity. */
    public static final int HISPANIC_LATINO = 0x0001;

    /** Flag whose presence indicates student has American Indian ethnicity. */
    public static final int AMERICAN_INDIAN = 0x0002;

    /** Flag whose presence indicates student has Asian ethnicity. */
    public static final int ASIAN = 0x0004;

    /** Flag whose presence indicates student has Black ethnicity. */
    public static final int BLACK = 0x0008;

    /** Flag whose presence indicates student has Hawaiian ethnicity. */
    public static final int HAWAIIAN = 0x0010;

    /** Flag whose presence indicates student has White ethnicity. */
    public static final int WHITE = 0x0020;

    /** Flag whose presence indicates student has multiple ethnicities. */
    public static final int MULTI = 0x0040;

    /**
     * Tests whether flags indicate student has Hispanic/Latino ethnicity.
     *
     * @return true if student has Hispanic/Latino ethnicity
     */
    boolean isHispanicLatino() {

        return (this.ethnicity & HISPANIC_LATINO) == HISPANIC_LATINO;
    }

    /**
     * Tests whether flags indicate student has American Indian ethnicity.
     *
     * @return true if student has American Indian ethnicity
     */
    boolean isAmericanIndian() {

        return (this.ethnicity & AMERICAN_INDIAN) == AMERICAN_INDIAN;
    }

    /**
     * Tests whether flags indicate student has Asian ethnicity.
     *
     * @return true if student has Asian ethnicity
     */
    boolean isAsian() {

        return (this.ethnicity & ASIAN) == ASIAN;
    }

    /**
     * Tests whether flags indicate student has Black ethnicity.
     *
     * @return true if student has Black ethnicity
     */
    boolean isBlack() {

        return (this.ethnicity & BLACK) == BLACK;
    }

    /**
     * Tests whether flags indicate student has Hawaiian ethnicity.
     *
     * @return true if student has Hawaiian ethnicity
     */
    boolean isHawaiian() {

        return (this.ethnicity & HAWAIIAN) == HAWAIIAN;
    }

    /**
     * Tests whether flags indicate student has White ethnicity.
     *
     * @return true if student has White ethnicity
     */
    boolean isWhite() {

        return (this.ethnicity & WHITE) == WHITE;
    }

    /**
     * Tests whether flags indicate student has multiple ethnicities.
     *
     * @return true if student has multiple ethnicities
     */
    boolean isMulti() {

        return (this.ethnicity & MULTI) == MULTI;
    }

    /**
     * Attempts to parse a {@code StudentRecord} from a JSON object.
     *
     * @param json the JSON object
     * @return the parsed record
     * @throws IllegalArgumentException if the object could not be interpreted
     */
    static StudentRec parse(final JSONObject json) {

        final String id = json.getStringProperty("i");
        final String gender = json.getStringProperty("g");
        final Double ethnicity = json.getNumberProperty("e");

        final double ethnicityDbl = ethnicity == null ? 0.0 : ethnicity.doubleValue();
        final int ethnicityInt = (int) Math.round(ethnicityDbl);

        return new StudentRec(id, gender, ethnicityInt);

    }

    /**
     * Generates the JSON representation of the record.
     *
     * @return the JSON representation
     */
    public String toJson() {

        final String ethnicityStr = Integer.toString(this.ethnicity);

        final HtmlBuilder builder = new HtmlBuilder(32);

        builder.add("{\"i\":\"", this.studentId, "\",",
                "\"g\":\"", this.gender, "\",",
                "\"e\":", ethnicityStr, "}");

        return builder.toString();
    }
}

