package dev.mathops.session.txn.handlers;

import dev.mathops.core.builder.HtmlBuilder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A record of a single student's attempt on a single exam.
 */
public final class StudentExamRec {

    /** The ID of the student taking the exam. */
    public String studentId;

    /** The name of the course the exam applies to. */
    public String course;

    /** The number of the course unit. */
    public Integer unit;

    /** The exam ID. */
    public String examId;

    /** Flag indicating exam was proctored. */
    public boolean proctored;

    /** The date/time the student began the exam. */
    public LocalDateTime start;

    /** The date/time the student finished the exam. */
    public LocalDateTime finish;

    /** The date/time the exam was recovered. */
    public LocalDateTime recovered;

    /** The serial number of the exam answer sheet or realized version. */
    public Long serialNumber;

    /** A sorted map of StudentExamAnswer objects keyed on subtest name + problem ID. */
    public final SortedMap<String, StudentExamAnswerRec> answers;

    /** A sorted map of StudentSurveyAnswer objects keyed on problem ID. */
    public final SortedMap<String, StudentSurveyAnswer> surveys;

    /** A hash table of scores, key is subtest name, value is score. */
    public final Map<String, Integer> subtestScores;

    /** A hash table of grades, key is grading rule name, value is grade. */
    public final Map<String, Object> examGrades;

    /** Map of missed questions, key is question number, value is objective question relates to. */
    public final SortedMap<Integer, String> missed;

    /** A list of names of placements earned. */
    public final SortedSet<String> earnedPlacement;

    /** A list of names of credit earned. */
    public final SortedSet<String> earnedCredit;

    /**
     * A map whose keys are the names of placement to be denied, and values set to the reason for denial.
     */
    public final SortedMap<String, String> deniedPlacement;

    /**
     * A map whose keys are the names of credit to be denied, and values set to the reason for denial.
     */
    public final SortedMap<String, String> deniedCredit;

    /** How the information was validated. */
    public char howValidated = '?';

    /** The exam type. */
    public String examType;

    /** The mastery score for the exam. */
    public Integer masteryScore;

    /** A hash table of lent resources, key is resource type, value is ID. */
    private final Map<String, String> resources;

    /** The exam source. */
    public String examSource;

    /**
     * Constructs a new {@code StudentExamRec}.
     */
    public StudentExamRec() {

        this.resources = new HashMap<>(0);
        this.answers = new TreeMap<>();
        this.surveys = new TreeMap<>();
        this.subtestScores = new HashMap<>(1);
        this.examGrades = new HashMap<>(1);
        this.missed = new TreeMap<>();
        this.earnedPlacement = new TreeSet<>();
        this.earnedCredit = new TreeSet<>();
        this.deniedPlacement = new TreeMap<>();
        this.deniedCredit = new TreeMap<>();
    }

    /**
     * Generate a string representation of the object, in XML format.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder sb = new HtmlBuilder(1000);

        sb.addln("<student-exam>");

        if (this.studentId != null) {
            sb.addln(" <student-id>", this.studentId, "</student-id>");
        }

        if (this.course != null) {
            sb.addln(" <course>", this.course, "</course>");
        }

        if (this.unit != null) {
            sb.addln(" <unit>", this.unit, "</unit>");
        }

        if (this.examId != null) {
            sb.addln(" <version>", this.examId, "</version>");
        }

        if (this.start != null) {
            sb.addln(" <start>", this.start, "</start>");
        }

        if (this.finish != null) {
            sb.addln(" <finish>", this.finish, "</finish>");
        }

        if (this.recovered != null) {
            sb.addln(" <recovered>", this.recovered, "</recovered>");
        }

        if (this.serialNumber != null) {
            sb.addln(" <serial>", this.serialNumber, "</serial>");
        }

        if (!this.resources.isEmpty()) {
            sb.addln(" <resources>");

            for (final Map.Entry<String, String> entry : this.resources.entrySet()) {
                sb.addln(" <resource>");
                sb.addln("  <type>", entry.getKey(), "</type>");
                sb.addln("  <id>", entry.getValue(),
                        "</id>");
                sb.addln(" </resource>");
            }

            sb.addln(" </resources>");
        }

        if (!this.answers.isEmpty()) {
            sb.addln(" <answers>");

            for (final Map.Entry<String, StudentExamAnswerRec> entry : this.answers.entrySet()) {
                sb.addln("  <key>", entry.getKey(), "</key>");
                sb.addln("  <answer>", entry.getValue(),
                        "</answer>");
            }

            sb.addln(" </answers>");
        }

        if (!this.surveys.isEmpty()) {
            sb.addln(" <survey-answers>");

            for (final Map.Entry<String, StudentSurveyAnswer> entry : this.surveys.entrySet()) {
                sb.addln("  <key>", entry.getKey(), "</key>");
                sb.addln("  <answer>", entry.getValue(),
                        "</answer>");
            }

            sb.addln(" </survey-answers>");
        }

        if (!this.subtestScores.isEmpty()) {
            sb.addln(" <subtest-scores>");

            for (final Map.Entry<String, Integer> entry : this.subtestScores.entrySet()) {
                sb.addln(" <subtest>");
                sb.addln("   <name>", entry.getKey(), "</name>");
                sb.addln("   <score>", entry.getValue(),
                        "</score>");
                sb.addln(" </subtest>");
            }

            sb.addln(" </subtest-scores>");
        }

        if (!this.examGrades.isEmpty()) {
            sb.addln(" <grades>");

            for (final String subtest : this.examGrades.keySet()) {
                sb.addln(" <grade>");
                sb.addln("   <name>", subtest, "</name>");
                sb.addln("   <score>", this.subtestScores.get(subtest),
                        "</score>");
                sb.addln(" </subtest>");
            }

            sb.addln(" </grades>");
        }

        if (!this.missed.isEmpty()) {
            sb.addln(" <missed>");

            for (final Map.Entry<Integer, String> entry : this.missed.entrySet()) {
                sb.addln("  <question>", entry.getKey(), "</question>");
                sb.addln("  <subobjective>", entry.getValue(),
                        "  </subobjective>");
            }

            sb.addln(" </missed>");
        }

        if (!this.earnedPlacement.isEmpty()) {
            sb.addln(" <earned-placement>");

            for (final String result : this.earnedPlacement) {
                sb.addln("  <course>", result, "</course>");
            }

            sb.addln(" </earned-placement>");
        }

        if (!this.earnedCredit.isEmpty()) {
            sb.addln(" <earned-credit>");

            for (final String result : this.earnedCredit) {
                sb.addln("  <course>", result, "</course>");
            }

            sb.addln(" </earned-credit>");
        }

        if (!this.deniedPlacement.isEmpty()) {
            sb.addln(" <denied-placement>");

            for (final Map.Entry<String, String> entry : this.deniedPlacement.entrySet()) {
                sb.addln("  <course>", entry.getKey(), "</course>");
                sb.addln("  <reason>", entry.getValue(),
                        "</reason>");
            }

            sb.addln(" </denied-placement>");
        }

        if (!this.deniedCredit.isEmpty()) {
            sb.addln(" <denied-credit>");

            for (final Map.Entry<String, String> entry : this.deniedCredit.entrySet()) {
                sb.addln("  <course>", entry.getKey(), "</course>");
                sb.addln("  <reason>", entry.getValue(),
                        "</reason>");
            }

            sb.addln(" </denied-credit>");
        }

        sb.addln(" <how-validated>", Character.toString(this.howValidated),
                "</how-validated>");

        sb.addln("</student-exam>");

        return sb.toString();
    }
}
