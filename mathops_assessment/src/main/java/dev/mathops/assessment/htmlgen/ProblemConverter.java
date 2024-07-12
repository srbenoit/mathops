package dev.mathops.assessment.htmlgen;

import dev.mathops.assessment.document.template.AbstractDocObjectTemplate;
import dev.mathops.assessment.document.template.DocColumn;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.assessment.problem.template.ProblemAutoCorrectTemplate;
import dev.mathops.assessment.problem.template.ProblemChoiceTemplate;
import dev.mathops.assessment.problem.template.ProblemDummyTemplate;
import dev.mathops.assessment.problem.template.ProblemEmbeddedInputTemplate;
import dev.mathops.assessment.problem.template.ProblemMultipleChoiceTemplate;
import dev.mathops.assessment.problem.template.ProblemMultipleSelectionTemplate;
import dev.mathops.assessment.problem.template.ProblemNumericTemplate;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;

import java.util.Deque;
import java.util.LinkedList;

/**
 * Converts a {@code Problem} to a list of HTML files.
 */
public enum ProblemConverter {
    ;

    /**
     * Populates the html fields in a realized {@code Problem}.
     *
     * @param problem the problem to populate
     * @param id      a one-integer array that holds a value used to generate unique IDs for spans (element [0] is
     *                incremented each time a unique ID is called for)
     */
    public static void populateProblemHtml(final AbstractProblemTemplate problem, final int[] id) {

        synchronized (problem) {
            switch (problem) {
                case ProblemMultipleChoiceTemplate problemMultipleChoiceTemplate ->
                        populateMutipleChoice(problemMultipleChoiceTemplate, id);
                case ProblemMultipleSelectionTemplate problemMultipleSelectionTemplate ->
                        populateMutipleSelection(problemMultipleSelectionTemplate, id);
                case ProblemNumericTemplate problemNumericTemplate -> populateNumeric(problemNumericTemplate, id);
                case ProblemEmbeddedInputTemplate problemEmbeddedInputTemplate ->
                        populateEmbeddedInput(problemEmbeddedInputTemplate, id);
                case ProblemAutoCorrectTemplate problemAutoCorrectTemplate ->
                        populateAutoCorrect(problemAutoCorrectTemplate, id);
                case final ProblemDummyTemplate dummy -> {
                    if (dummy.id != null) {
                        Log.warning("Populating Dummy problem ", dummy.id);
                    } else {
                        Log.warning("Populating Dummy problem with null ref");
                    }
                }
                default -> {
                }
            }
        }
    }

    /**
     * Populates the HTML fields of a realized {@code ProblemMultipleChoice}.
     *
     * <p>
     * In all forms, it is assumed that JavaScript will be emitted after the item with the following code to store the
     * user's current response, if any.
     *
     * <pre>
     * &lt;script&gt;
     * document.getElementById("CHOICE_{id-of-selected-choice}").checked = true;
     * &lt;/script&gt;
     * </pre>
     *
     * @param problem the {@code ProblemMultipleChoice} to populate
     * @param id      a one-integer array that holds a value used to generate unique IDs for spans (element [0] is
     *                incremented each time a unique ID is called for)
     */
    private static void populateMutipleChoice(final ProblemMultipleChoiceTemplate problem, final int[] id) {

        final Deque<Style> styleStack = new LinkedList<>();
        styleStack.push(new Style(AbstractDocObjectTemplate.DEFAULT_BASE_FONT_SIZE, "black"));

        final DocColumn question = problem.question;
        final String questionHtml = DocObjectConverter.convertDocColumn(question, styleStack, false,
                id, problem.evalContext);

        final DocColumn solution = problem.solution;
        final String solutionHtml = solution == null ? null : DocObjectConverter
                .convertDocColumn(solution, styleStack, false, id, problem.evalContext);

        final int[] choiceOrder = problem.choiceOrder;
        final int len = choiceOrder.length;

        final String[] choiceHtml = new String[len];
        final String[] choiceId = new String[len];
        final boolean[] correct = new boolean[len];

        for (int i = 0; i < len; ++i) {
            final ProblemChoiceTemplate choice = problem.getChoices().get(choiceOrder[i]);
            choiceHtml[i] = DocObjectConverter.convertDocColumn(choice.doc, styleStack, false, id, problem.evalContext);
            choiceId[i] = Integer.toString(choice.choiceId);
            correct[i] = Boolean.TRUE.equals(choice.correct.evaluate(problem.evalContext));
        }

        final HtmlBuilder content = new HtmlBuilder(1000);

        // Generate version with the question and choices that the user will interact with

        final Style peeked = styleStack.peek();
        final float floatFontSize = peeked == null ? 0.0f : peeked.getSize();
        final String boxSizeStr = Integer.toString(Math.round(floatFontSize * 0.8f));
        // final String fontSizeStr = Float.toString(floatFontSize);

        // htm.add("<input type='radio' style='width:", boxSizeStr, "px;height:",
        // boxSizeStr, "px;font-size:",
        // fontSizeStr, "px;'");

        content.addln(questionHtml);
        content.addln("<table style='font-size:inherit;font-family:inherit;border-collapse: collapse;'>");
        final int numChoice = choiceHtml.length;
        for (int i = 0; i < numChoice; ++i) {
            final String idstr = "CHOICE_" + choiceId[i];
            content.addln("<tr><td style='font-size:inherit;font-family:inherit;'>",
                    "<input type='radio' name='CHOICE' id='CHOICE_", choiceId[i], "' style='width:", boxSizeStr,
                    "px;height:", boxSizeStr, "px;' value='", choiceId[i],
                    "'></td><td style='font-size:inherit;font-family:inherit;padding-right:12px;'>",
                    "<label for='", idstr, "'>", choiceHtml[i], "</label>").eTd().eTr();
        }
        content.eTable();
        problem.questionHtml = content.toString();
        content.reset();

        content.addln(questionHtml);
        content.addln("<table style='font-size:inherit;font-family:inherit;border-collapse: collapse;'>");
        for (int i = 0; i < len; ++i) {
            content.addln("<tr><td style='font-size:inherit;font-family:inherit;'>",
                    "<input disabled type='radio' name='CHOICE' id='CHOICE_", choiceId[i], "' value='", choiceId[i],
                    "'></td><td style='font-size:inherit;font-family:inherit;padding-right:12px;'>",
                    choiceHtml[i]).eTd().eTr();
        }
        content.eTable();
        problem.disabledHtml = content.toString();
        content.reset();

        // Generate version with the student's responses plus correct answers shown

        content.addln(questionHtml);
        content.addln("<table style='font-size:inherit;font-family:inherit;border-collapse: collapse;'>");
        for (int i = 0; i < len; ++i) {
            content.add(correct[i] //
                    ? "<tr style='background:#AAE1AA; border:1px solid #649664'>"
                    : "<tr>");

            content.add("<td style='font-size:inherit;font-family:inherit;'>");
            content.add("<input disabled type='radio' name='CHOICE' id='CHOICE_", choiceId[i], "' value='",
                    choiceId[i], "'>").eTd();

            content.add("<td style='font-size:inherit;font-family:inherit;padding-right:12px;'>");
            content.addln(choiceHtml[i]).eTd().eTr();
        }
        content.eTable();
        problem.answerHtml = content.toString();
        content.reset();

        // Generate version with the student's responses plus correct answers and solution shown

        content.addln(questionHtml);
        content.addln("<table style='font-size:inherit;font-family:inherit;border-collapse: collapse;'>");
        for (int i = 0; i < len; ++i) {
            content.add(correct[i] ? "<tr style='background:#AAE1AA; border:1px solid #649664;'>" : "<tr>");
            content.add("<td style='font-size:inherit;font-family:inherit;'>");
            content.add("<input disabled type='radio' name='CHOICE' id='CHOICE_", choiceId[i], "' value='",
                    choiceId[i], "'>").eTd();

            content.add("<td style='font-size:inherit;font-family:inherit;;padding-right:12px'>");
            content.addln(choiceHtml[i]).eTd().eTr();
        }
        content.eTable();
        if (solutionHtml != null) {
            content.addln("<div style='background:#eee; border:1px solid #777; padding:5px; margin-top:13px;'>")
                    .addln(solutionHtml).eDiv();
        }
        problem.solutionHtml = content.toString();
    }

    /**
     * Populates the HTML fields of a {@code ProblemMultipleSelection}.
     *
     * <p>
     * In all forms, it is assumed that JavaScript will be emitted after the item with the following code to store the
     * user's current responses, if any.
     *
     * <pre>
     * &lt;script&gt;
     * document.getElementById("CHOICE_{id-of-selected-choice-1}").checked = true;
     * document.getElementById("CHOICE_{id-of-selected-choice-2}").checked = true;
     * &lt;/script&gt;
     * </pre>
     *
     * @param problem the {@code ProblemMultipleChoice} to populate
     * @param id      a one-integer array that holds a value used to generate unique IDs for spans (element [0] is
     *                incremented each time a unique ID is called for)
     */
    private static void populateMutipleSelection(final ProblemMultipleSelectionTemplate problem,
                                                 final int[] id) {

        final Deque<Style> styleStack = new LinkedList<>();
        styleStack.push(new Style(AbstractDocObjectTemplate.DEFAULT_BASE_FONT_SIZE, "black"));

        final DocColumn question = problem.question;
        final String questionHtml = DocObjectConverter.convertDocColumn(question, styleStack, false, id,
                problem.evalContext);

        final DocColumn solution = problem.solution;
        final String solutionHtml = solution == null ? null : DocObjectConverter
                .convertDocColumn(solution, styleStack, false, id, problem.evalContext);

        final int[] choiceOrder = problem.choiceOrder;
        final int len = choiceOrder.length;

        final String[] choiceHtml = new String[len];
        final String[] choiceId = new String[len];
        final boolean[] correct = new boolean[len];

        for (int i = 0; i < len; ++i) {
            final ProblemChoiceTemplate choice = problem.getChoices().get(problem.choiceOrder[i]);
            choiceHtml[i] = DocObjectConverter.convertDocColumn(choice.doc, styleStack, false, id, problem.evalContext);
            choiceId[i] = Integer.toString(choice.choiceId);
            correct[i] = Boolean.TRUE.equals(choice.correct.evaluate(problem.evalContext));
        }

        final HtmlBuilder content = new HtmlBuilder(1000);

        // Generate version with the question and choices that the user will interact with

        final Style peeked = styleStack.peek();
        final float floatFontSize = peeked == null ? 0.0f : peeked.getSize();
        final String boxSizeStr = Integer.toString(Math.round(floatFontSize * 0.7f));
        // final String fontSizeStr = Float.toString(floatFontSize);

        content.addln(questionHtml);
        content.addln("<table style='font-size:inherit;font-family:inherit;border-collapse: collapse;'>");
        for (int i = 0; i < len; ++i) {
            final String idstr = "CHOICE_" + choiceId[i];
            content.addln("<tr><td style='font-size:inherit;font-family:inherit;'>",
                    "<input type='checkbox' name='", idstr, "' id='", idstr, "' style='width:", boxSizeStr,
                    "px;height:", boxSizeStr, "px;'></td>",
                    "<td style='font-size:inherit;font-family:inherit;padding-right:12px;'><label for='", idstr, "'>",
                    choiceHtml[i], "</label>").eTd().eTr();
        }
        content.eTable();
        problem.questionHtml = content.toString();
        content.reset();

        content.addln(questionHtml);
        content.addln("<table style='font-size:inherit;font-family:inherit;border-collapse: collapse;'>");
        for (int i = 0; i < len; ++i) {
            content.addln("<tr><td style='font-size:inherit;font-family:inherit;'>",
                    "<input disabled type='checkbox' name='CHOICE_", choiceId[i], "' id='CHOICE_", choiceId[i],
                    "'></td><td style='font-size:inherit;font-family:inherit;padding-right:12px;'>",
                    choiceHtml[i]).eTd().eTr();
        }
        content.eTable();
        problem.disabledHtml = content.toString();
        content.reset();

        // Generate version with the student's responses plus correct answers shown

        content.addln(questionHtml);
        content.addln("<table style='font-size:inherit;font-family:inherit;border-collapse: collapse;'>");
        for (int i = 0; i < len; ++i) {
            content.add(correct[i] ? "<tr style='background:#AAE1AA; border:1px solid #649664;'>" : "<tr>");
            content.add("<td style='font-size:inherit;font-family:inherit;'>");
            content.add("<input disabled type='checkbox' name='CHOICE_", choiceId[i], "' id='CHOICE_", choiceId[i],
                    "'>").eTd();

            content.add("<td style='font-size:inherit;font-family:inherit;padding-right:12px;'>");
            content.addln(choiceHtml[i]).eTd().eTr();
        }
        content.eTable();
        problem.answerHtml = content.toString();
        content.reset();

        // Generate version with the student's responses plus correct answers and solution shown

        content.addln(questionHtml);
        content.addln("<table style='font-size:inherit;font-family:inherit;border-collapse: collapse;'>");
        for (int i = 0; i < len; ++i) {
            content.add(correct[i] ? "<tr style='background:#AAE1AA; border:1px solid #649664;'>" : "<tr>");
            content.add("<td style='font-size:inherit;font-family:inherit;'>");
            content.add("<input dislabled type='checkbox' name='CHOICE_", choiceId[i], "' id='CHOICE_", choiceId[i],
                    "'>").eTd();

            content.add("<td style='font-size:inherit;font-family:inherit;padding-right:12px;'>");
            content.addln(choiceHtml[i]).eTd().eTr();
        }
        content.eTable();
        if (solutionHtml != null) {
            content.addln("<div style='background:#eee; border:1px solid #777; padding:5px; margin-top:13px;'>")
                    .addln(solutionHtml).eDiv();
        }
        problem.solutionHtml = content.toString();
    }

    /**
     * Populates the HTML fields of a {@code ProblemNumeric}.
     *
     * @param problem the {@code ProblemNumeric} to populate
     * @param id      a one-integer array that holds a value used to generate unique IDs for spans (element [0] is
     *                incremented each time a unique ID is called for)
     */
    private static void populateNumeric(final ProblemNumericTemplate problem, final int[] id) {

        final Deque<Style> styleStack = new LinkedList<>();
        styleStack.push(new Style(AbstractDocObjectTemplate.DEFAULT_BASE_FONT_SIZE, "black"));

        final DocColumn question = problem.question;
        final String questionHtml = DocObjectConverter.convertDocColumn(question, styleStack, false, id,
                problem.evalContext);

        final DocColumn solution = problem.solution;
        final String solutionHtml = solution == null ? null : DocObjectConverter
                .convertDocColumn(solution, styleStack, false, id, problem.evalContext);

        final HtmlBuilder content = new HtmlBuilder(1000);

        // Generate version with the question and numeric entry box that the user will interact with

        final Style peeked = styleStack.peek();
        final float floatFontSize = peeked == null ? 0.0f : peeked.getSize();

        content.addln(questionHtml);
        content.addln("<div style='text-align:center;'>");
        content.addln("<input type='text' data-lpignore='true' autocomplete='off' size='6' ",
                "oninput=\"this.value = this.value.replace(/[^0-9./\\-\u03c0]/g, '');\" ",
                "style='font-size:", Float.toString(floatFontSize), "px;' name='ANSWER' id='ANSWER'>");
        content.eDiv();
        problem.questionHtml = content.toString();
        content.reset();

        content.addln(questionHtml);
        content.addln("<div style='text-align:center;'>");
        content.addln("<input disabled type='text' data-lpignore='true' autocomplete='off' size='6' style='font-size:",
                Float.toString(floatFontSize), "px;' name='ANSWER' id='ANSWER'>");
        content.eDiv();
        problem.disabledHtml = content.toString();
        content.reset();

        // Generate version with the student's responses plus correct answers shown

        content.addln(questionHtml);
        content.addln("<div style='text-align:center;'>");
        content.addln("<input disabled type='text' data-lpignore='true' autocomplete='off' size='6' style='font-size:",
                Float.toString(floatFontSize), "px;' name='ANSWER' id='ANSWER'>");
        content.eDiv();
        content.addln("<div style='background:#eee; border:1px solid #777; padding:5px; ",
                "margin-top:3px; text-align:center;'>");
        content.add("The correct answer is ");
        // TODO: Apply number format
        final Number ans = problem.acceptNumber.getCorrectAnswerValue(problem.evalContext);

        if (ans == null) {
            content.add("Unable to compute answer");
        } else if (ans instanceof Double) {
            final float value = ans.floatValue();
            content.add(value < 0.0f ? "<span class='sr-only'>negative</span>&minus;"
                    + Math.abs(value) : Float.toString(value));
        } else {
            final long value = ans.longValue();
            content.add(value < 0L ? "<span class='sr-only'>negative</span>&minus;"
                    + Math.abs(value) : Long.toString(value));
        }
        content.eDiv();
        problem.answerHtml = content.toString();
        content.reset();

        // Generate version with the student's responses plus correct answers and solution shown

        content.addln(questionHtml);
        content.addln("<div style='text-align:center;'>");
        content.addln("<input disabled type='text' data-lpignore='true' autocomplete='off' size='6' style='font-size:",
                Float.toString(floatFontSize), "px;' name='ANSWER' id='ANSWER'>");
        content.eDiv();
        content.addln("<div style='text-align:center; margin-top:3px;'>");
        content.add("The correct answer is ");

        // TODO: Apply number format

        if (ans == null) {
            content.add("Unable to compute answer");
        } else if (ans instanceof Double) {
            final float value = ans.floatValue();
            content.add(value < 0.0f ? "<span class='sr-only'>negative</span>&minus;"
                    + Math.abs(value) : Float.toString(value));
        } else {
            final long value = ans.longValue();
            content.add(value < 0L ? "<span class='sr-only'>negative</span>&minus;"
                    + Math.abs(value) : Long.toString(value));
        }

        content.eDiv();
        if (solutionHtml != null) {
            content.add("<div style='background:#eee; border:1px solid #777; padding:5px; margin-top:13px;'>")
                    .addln(solutionHtml).eDiv();
        }
        problem.solutionHtml = content.toString();
    }

    /**
     * Populates the HTML fields of a {@code ProblemEmbeddedInput}.
     *
     * @param problem the {@code ProblemEmbeddedInput} to populate
     * @param id      a one-integer array that holds a value used to generate unique IDs for spans (element [0] is
     *                incremented each time a unique ID is called for)
     */
    private static void populateEmbeddedInput(final ProblemEmbeddedInputTemplate problem, final int[] id) {

        final Deque<Style> styleStack = new LinkedList<>();
        styleStack.push(new Style(AbstractDocObjectTemplate.DEFAULT_BASE_FONT_SIZE, "black"));

        final DocColumn question = problem.question;
        final String questionHtml = DocObjectConverter.convertDocColumn(question, styleStack, true,
                id, problem.evalContext);
        final String questionDisabledHtml = DocObjectConverter.convertDocColumn(question,
                styleStack, false, id, problem.evalContext);

        final DocColumn answer = problem.correctAnswer;
        final String answerHtml = answer == null ? null : DocObjectConverter
                .convertDocColumn(answer, styleStack, false, id, problem.evalContext);

        final DocColumn solution = problem.solution;
        final String solutionHtml = solution == null ? null : DocObjectConverter
                .convertDocColumn(solution, styleStack, false, id, problem.evalContext);

        // Generate version with the question that the user will interact with
        problem.questionHtml = questionHtml;
        problem.disabledHtml = questionDisabledHtml;

        final HtmlBuilder content = new HtmlBuilder(1000);

        // Generate version with the student's responses plus correct answers shown
        content.addln(questionDisabledHtml);
        if (answerHtml != null) {
            content.addln("<div style='background:#eee; border:1px solid #777; padding:5px;'>")
                    .addln(answerHtml).eDiv();
        }
        problem.answerHtml = content.toString();
        content.reset();

        // Generate version with the student's responses plus correct answers and solution shown
        content.addln(questionDisabledHtml);
        if (solutionHtml != null) {
            content.addln("<div style='background:#eee; border:1px solid #777; padding:5px; margin-top:13px;'>")
                    .addln(solutionHtml).eDiv();
        }
        problem.solutionHtml = content.toString();
    }

    /**
     * Populates the HTML fields of a {@code ProblemAutoCorrect}.
     *
     * @param problem the {@code ProblemAutoCorrect} to populate
     * @param id      a one-integer array that holds a value used to generate unique IDs for spans (element [0] is
     *                incremented each time a unique ID is called for)
     */
    private static void populateAutoCorrect(final ProblemAutoCorrectTemplate problem, final int[] id) {

        final Deque<Style> styleStack = new LinkedList<>();
        styleStack.push(new Style(AbstractDocObjectTemplate.DEFAULT_BASE_FONT_SIZE, "black"));

        final DocColumn question = problem.question;
        final String questionHtml = DocObjectConverter.convertDocColumn(question, styleStack, true, id,
                problem.evalContext);
        final String questionDisabledHtml = DocObjectConverter.convertDocColumn(question, styleStack, false, id,
                problem.evalContext);

        // Generate version with the question that the user will interact with
        problem.questionHtml = questionHtml;
        problem.disabledHtml = questionDisabledHtml;
        problem.answerHtml = questionDisabledHtml;
        problem.solutionHtml = questionDisabledHtml;
    }
}
