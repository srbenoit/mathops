package dev.mathops.dbjobs.report.analytics.longitudinal.data;

import dev.mathops.text.parser.json.JSONObject;

/**
 * A container for the data about a single registration status.
 *
 * @param regStatus     the registration status code
 * @param regStatusDesc the text description of the registration status
 */
public record RegistrationStatusRec(String regStatus, String regStatusDesc) {

    /**
     * Attempts to parse a {@code EnrollmentRecord} from a JSON object.
     *
     * @param json the JSON object
     * @return the parsed record
     * @throws IllegalArgumentException if the object could not be interpreted
     */
    public static RegistrationStatusRec parse(final JSONObject json) {

        final String s = json.getStringProperty("s");
        final String sd = json.getStringProperty("sd");

        return new RegistrationStatusRec(s, sd);
    }

    /**
     * Generates the JSON representation of the record.
     *
     * @return the JSON representation
     */
    public String toJson() {

        final JSONObject obj = new JSONObject();

        obj.setProperty("s", this.regStatus);
        obj.setProperty("sd", this.regStatusDesc);

        return obj.toJSONCompact();
    }
}

