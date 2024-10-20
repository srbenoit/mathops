package dev.mathops.dbjobs.report.analytics.longitudinal;

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
 */
public record StudentTermRec(String studentId, int academicPeriod, String college, String department, String major,
                             String program, String level) {

    /**
     * Attempts to parse a {@code StudentTermRecord} from a JSON object.
     *
     * @param json the JSON object
     * @return the parsed record
     * @throws IllegalArgumentException if the object could not be interpreted
     */
    public static StudentTermRec parse(final JSONObject json) {

        final String id = json.getStringProperty("i");
        final Double pe = json.getNumberProperty("p");
        final String co = json.getStringProperty("c");
        final String de = json.getStringProperty("d");
        final String ma = json.getStringProperty("m");
        final String pr = json.getStringProperty("r");
        final String lv = json.getStringProperty("v");

        final int peInt = pe == null ? 0 : pe.intValue();

        return new StudentTermRec(id, peInt, co, de, ma, pr, lv);
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
                "\"v\":\"", level(), "\"}");

        return builder.toString();
    }
}

