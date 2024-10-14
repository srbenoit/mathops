package dev.mathops.dbjobs.report.analytics.longitudinal;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.parser.json.JSONObject;

/**
 * A container for the data about a single course registration.
 *
 * @param studentId      the student ID
 * @param academicPeriod the  academic period, such as "202410" for Spring, 2024
 * @param course         the course, such as "MATH117"
 * @param section        the section number, such as "001"
 * @param transfer       true if this is a transfer
 * @param apIbClep       true if this is a transfer and came from AP, IP, or CLEP
 * @param regStatus      the registration status
 * @param withdrawn      true if student withdrew
 * @param passed         true if student passed
 * @param failed         true if student failed
 * @param grade          the student's final grade (null if no grade earned)
 * @param gradeValue     the numerical grade value (null if no grade earned) {A+ = 950, A = 900, A- = 800, B+ = 750, B =
 *                       700, B- = 600, C+ = 550, C = 500, C- = 400, D+ = 350, D = 300, D- = 200, F = 150, RD = 42, RF =
 *                       40, W  = 30}
 */
record StudentCourseRecord(String studentId, int academicPeriod, String course, String section, boolean transfer,
                           boolean apIbClep, String regStatus, boolean withdrawn, boolean passed, boolean failed,
                           String grade, Double gradeValue) {

    /**
     * Attempts to parse a {@code StudentCourseRecord} from a JSON object.
     *
     * @param json the JSON object
     * @return the parsed record
     * @throws IllegalArgumentException if the object could not be interpreted
     */
    static StudentCourseRecord parse(final JSONObject json) {

        final String id = json.getStringProperty("id");
        final Double pe = json.getNumberProperty("pe");
        final String co = json.getStringProperty("co");
        final String se = json.getStringProperty("se");
        final Boolean tr = json.getBooleanProperty("tr");
        final Boolean ap = json.getBooleanProperty("ap");
        final String rs = json.getStringProperty("rs");
        final Boolean wi = json.getBooleanProperty("wi");
        final Boolean pa = json.getBooleanProperty("pa");
        final Boolean fa = json.getBooleanProperty("fa");
        final String gr = json.getStringProperty("gr");
        final Double gv = json.getNumberProperty("gv");

        final int peInt = pe == null ? 0 : pe.intValue();

        final boolean isTr = tr.equals(Boolean.TRUE);
        final boolean isAp = ap.equals(Boolean.TRUE);
        final boolean isWi = wi.equals(Boolean.TRUE);
        final boolean isPa = pa.equals(Boolean.TRUE);
        final boolean isFa = fa.equals(Boolean.TRUE);

        return new StudentCourseRecord(id, peInt, co, se, isTr, isAp, rs, isWi, isPa, isFa, gr, gv);

    }

    /**
     * Generates the JSON representation of the record
     *
     * @return the JSON representation
     */
    String toJson() {

        final HtmlBuilder builder = new HtmlBuilder(100);

        builder.add("{\"id\":\"", studentId(), "\",",
                "\"pe\":", academicPeriod(), ",",
                "\"co\":\"", course(), "\",",
                "\"se\":\"", section(), "\",",
                "\"tr\":", transfer(), ",",
                "\"ap\":", apIbClep(), ",",
                "\"rs\":\"", regStatus(), "\",",
                "\"wi\":", withdrawn(), ",",
                "\"pa\":", passed(), ",",
                "\"fa\":", failed());

        if (grade() != null) {
            builder.add(",\"gr\":\"", grade(), "\"");
        }
        if (gradeValue() != null) {
            builder.add(",\"gv\":", gradeValue());
        }
        builder.add("}");

        return builder.toString();
    }
}

