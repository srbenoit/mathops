package dev.mathops.dbjobs.report.analytics.longitudinal.data;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.parser.json.JSONObject;

/**
 * A container for the data about a student in a single term.
 *
 * @param studentId      the student ID
 * @param academicPeriod the  academic period, such as "202410" for Spring, 2024
 * @param college        the student's primary college
 * @param department     the student's primary department
 * @param major          the student's primary major
 * @param program        the student's program of study
 * @param level          the student's level
 * @param studentType    the student type
 */
public record StudentTermRec(String studentId, int academicPeriod, String college, String department, String major,
                             String program, String level, String studentType) {

    /**
     * Attempts to parse a {@code StudentTermRecord} from a JSON object.
     *
     * @param json the JSON object
     * @return the parsed record
     * @throws IllegalArgumentException if the object could not be interpreted
     */
    public static StudentTermRec parse(final JSONObject json) {

        final String i = json.getStringProperty("i");
        final Double p = json.getNumberProperty("p");
        final String c = json.getStringProperty("c");
        final String d = json.getStringProperty("d");
        final String m = json.getStringProperty("m");
        final String r = json.getStringProperty("r");
        final String v = json.getStringProperty("v");
        final String t = json.getStringProperty("t");

        final int peInt = p == null ? 0 : p.intValue();

        return new StudentTermRec(i, peInt, c, d, m, r, v, t);
    }

    /**
     * Generates the JSON representation of the record
     *
     * @return the JSON representation
     */
    public String toJson() {

        final HtmlBuilder builder = new HtmlBuilder(100);

        builder.add("{\"i\":\"", studentId(), "\",",
                "\"p\":", academicPeriod(), ",",
                "\"c\":\"", college(), "\",",
                "\"d\":\"", department(), "\",",
                "\"m\":\"", major(), "\",",
                "\"r\":\"", program(), "\",",
                "\"v\":\"", level(), "\",",
                "\"t\":\"", studentType(), "\"}");

        return builder.toString();
    }
}

