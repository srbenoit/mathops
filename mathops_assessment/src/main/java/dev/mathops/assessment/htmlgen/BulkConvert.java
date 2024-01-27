package dev.mathops.assessment.htmlgen;

import dev.mathops.assessment.InstructionalCache;
import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Converts one generation each DocColumn associated with any exam or problem in the system, and appends all of them to
 * a single large HTML file for review (prefaced with the identifier of the source of the object).
 */
public enum BulkConvert {
    ;

    /**
     * Performs the conversion.
     */
    private static void convert() {

        final InstructionalCache cache = InstructionalCache.getInstance();
        cache.start();

        // Let the cache finish scanning
        while (cache.getNextRun() == 0L) {
            try {
                Thread.sleep(1000L);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        final int[] id = {1};
        final HtmlBuilder htm = new HtmlBuilder(4050000);
        htm.addln("<html>");
        htm.addln("<head></head>");
        htm.addln("<body style='font-size:24px'>");

        for (final String ref : cache.getExamFileRefs()) {
            final ExamObj exam = InstructionalCache.getExam(ref);
            htm.addln("Exam: ", exam.ref);

            // Log.info(exam.getIdentifierReference().ref);

            exam.realize(true, false, 123456L);
            ExamObjConverter.populateExamHtml(exam, id);
            final String instr = exam.instructionsHtml;
            if (instr != null) {
                htm.addln(instr);
            }

            htm.addln("<hr>");
        }

        int index = 1;

        for (final String ref : cache.getProblemFileRefs()) {

            if (htm.length() > 4000000) {
                htm.addln("</body></html>");

                final File dest = new File("/Users/benoit/Desktop/bulk_dump_" + index + ".html");
                try (final FileWriter out = new FileWriter(dest, StandardCharsets.UTF_8)) {
                    out.write(htm.toString());
                } catch (final IOException ex) {
                    Log.warning(ex);
                }
                htm.reset();
                htm.addln("<html>");
                htm.addln("<head></head>");
                htm.addln("<body style='font-size:24px'>");
                ++index;
            }

            final AbstractProblemTemplate problem = InstructionalCache.getProblem(ref);
            htm.addln("Problem: ", problem.ref).br();

            // Log.info(problem.getIdentifierReference().ref);

            boolean realized = false;
            for (int i = 0; i < 100; ++i) {
                if (problem.realize(problem.evalContext)) {
                    realized = true;
                    break;
                }
            }

            if (realized) {

                ProblemConverter.populateProblemHtml(problem, id);

                final String questionHtml = problem.questionHtml;
                if (questionHtml != null) {
                    htm.addln("Question:").br();
                    htm.addln(questionHtml);
                    htm.br();
                }

                final String answerHtml = problem.answerHtml;
                if (answerHtml != null) {
                    htm.addln("Answer:").br();
                    htm.addln(answerHtml);
                    htm.br();
                }

                final String solutionHtml = problem.solutionHtml;
                if (solutionHtml != null) {
                    htm.addln("Solution:").br();
                    htm.addln(solutionHtml);
                    htm.br();
                }

                htm.add("<hr>");
            } else {
                Log.warning("Unable to realize ", problem.ref);
            }
        }

        htm.addln("</body></html>");
        final File dest = new File("/Users/benoit/Desktop/bulk_dump_"
                + index + ".html");
        try (final FileWriter out = new FileWriter(dest, StandardCharsets.UTF_8)) {
            out.write(htm.toString());
        } catch (final IOException ex) {
            Log.warning(ex);
        }

        cache.die();
    }

    /**
     * Executes the conversion.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        convert();
    }
}
