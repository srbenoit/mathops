package dev.mathops.assessment.htmlgen;

import dev.mathops.assessment.document.template.AbstractDocObjectTemplate;
import dev.mathops.assessment.document.template.DocColumn;
import dev.mathops.assessment.exam.ExamObj;

import java.util.Deque;
import java.util.LinkedList;

/**
 * Converts an {@code ExamObj} to a list of HTML files.
 */
public enum ExamObjConverter {
    ;

    /**
     * Populates the html fields in a realized {@code ExamObj}.
     *
     * @param exam the exam to populate
     * @param id   a one-integer array that holds a value used to generate unique IDs for spans (element [0] is
     *             incremented each time a unique ID is called for)
     */
    public static void populateExamHtml(final ExamObj exam, final int[] id) {

        final Deque<Style> styleStack = new LinkedList<>();
        styleStack.push(new Style(AbstractDocObjectTemplate.DEFAULT_BASE_FONT_SIZE, "black"));

        final DocColumn instructions = exam.instructions;

        exam.instructionsHtml = instructions == null ? null : DocObjectConverter
                .convertDocColumn(instructions, styleStack, false, id, exam.getEvalContext());
    }
}
