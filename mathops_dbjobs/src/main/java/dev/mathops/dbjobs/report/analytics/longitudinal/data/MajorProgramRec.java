package dev.mathops.dbjobs.report.analytics.longitudinal.data;

import dev.mathops.text.parser.json.JSONObject;

/**
 * A container for the data about a single major and program of study.
 *
 * @param major       the 4-letter code for the major
 * @param majorDesc   the text description of the major
 * @param program     the program code
 * @param programDesc the text description of the program
 */
public record MajorProgramRec(String major, String majorDesc, String program, String programDesc) {

    /**
     * Attempts to parse a {@code EnrollmentRecord} from a JSON object.
     *
     * @param json the JSON object
     * @return the parsed record
     * @throws IllegalArgumentException if the object could not be interpreted
     */
    public static MajorProgramRec parse(final JSONObject json) {

        final String m = json.getStringProperty("m");
        final String md = json.getStringProperty("md");
        final String p = json.getStringProperty("p");
        final String pd = json.getStringProperty("pd");

        return new MajorProgramRec(m, md, p, pd);
    }

    /**
     * Generates the JSON representation of the record.
     *
     * @return the JSON representation
     */
    public String toJson() {

        final JSONObject obj = new JSONObject();

        obj.setProperty("m",  this.major);
        obj.setProperty("md",  this.majorDesc);
        obj.setProperty("p",  this.program);
        obj.setProperty("pd",  this.programDesc);

        return obj.toJSONCompact();
    }
}

