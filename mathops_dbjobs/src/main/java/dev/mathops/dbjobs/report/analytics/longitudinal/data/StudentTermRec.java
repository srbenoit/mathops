package dev.mathops.dbjobs.report.analytics.longitudinal.data;

import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.json.JSONObject;

/**
 * A container for the data about a student in a single term.
 *
 * @param studentId      the student ID
 * @param academicPeriod the  academic period, such as "202410" for Spring, 2024
 * @param college        the student's primary college
 * @param department     the student's primary department
 * @param major          the student's primary major
 * @param program        the student's program of study
 * @param studentType    the student type
 * @param gradTerm       the student's estimated graduation term
 */
public record StudentTermRec(String studentId, int academicPeriod, String college, String department, String major,
                             String program, String studentType, int gradTerm) implements Comparable<StudentTermRec> {

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
        final String t = json.getStringProperty("t");
        final Double g = json.getNumberProperty("g");

        final int pInt = p == null ? 0 : p.intValue();
        final int gInt = g == null ? 0 : g.intValue();

        return new StudentTermRec(i, pInt, c, d, m, r, t, gInt);
    }

    /**
     * Generates the JSON representation of the record
     *
     * @return the JSON representation
     */
    public String toJson() {

        final HtmlBuilder builder = new HtmlBuilder(100);

        builder.add("{",
                "\"i\":\"", studentId(), "\",",
                "\"p\":", academicPeriod(), ",",
                "\"c\":\"", college(), "\",",
                "\"d\":\"", department(), "\",",
                "\"m\":\"", major(), "\",",
                "\"r\":\"", program(), "\",",
                "\"t\":\"", studentType(), "\",",
                "\"g\":", gradTerm(), "}");

        return builder.toString();
    }

    /**
     * Compares terms for order, based on academic period.
     *
     * @param o the object to be compared
     * @return zero if academic period are equal; a value less than 0 if this object's academic period is earlier than
     *         that in {@code o} and a value greater than 0 otherwise
     */
    @Override
    public int compareTo(final StudentTermRec o) {

        final int other = o.academicPeriod();

        return Integer.compare(this.academicPeriod, other);
    }
}

