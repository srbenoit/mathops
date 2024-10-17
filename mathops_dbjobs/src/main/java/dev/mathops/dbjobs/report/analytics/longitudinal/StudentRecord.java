package dev.mathops.dbjobs.report.analytics.longitudinal;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.parser.json.JSONObject;

/**
 * A container for the data about a single student.
 *
 * @param studentId the student ID
 */
record StudentRecord(String studentId) {

    /**
     * Attempts to parse a {@code StudentRecord} from a JSON object.
     *
     * @param json the JSON object
     * @return the parsed record
     * @throws IllegalArgumentException if the object could not be interpreted
     */
    static StudentRecord parse(final JSONObject json) {

        final String id = json.getStringProperty("id");

        return new StudentRecord(id);

    }

    /**
     * Generates the JSON representation of the record
     *
     * @return the JSON representation
     */
    String toJson() {

        final HtmlBuilder builder = new HtmlBuilder(100);

        builder.add("{\"id\":\"", studentId(), "}");

        return builder.toString();
    }
}

