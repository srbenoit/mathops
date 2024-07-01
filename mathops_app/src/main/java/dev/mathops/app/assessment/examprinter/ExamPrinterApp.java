package dev.mathops.app.assessment.examprinter;

import dev.mathops.app.ClientBase;
import dev.mathops.app.DirectoryFilter;
import dev.mathops.app.GuiBuilderRunner;
import dev.mathops.app.IGuiBuilder;
import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.FactoryBase;
import dev.mathops.assessment.InstructionalCache;
import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.template.AbstractDocObjectTemplate;
import dev.mathops.assessment.document.template.DocColumn;
import dev.mathops.assessment.exam.ExamFactory;
import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.assessment.exam.ExamProblem;
import dev.mathops.assessment.exam.ExamSection;
import dev.mathops.assessment.problem.template.AbstractProblemMultipleChoiceTemplate;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.assessment.problem.template.ProblemChoiceTemplate;
import dev.mathops.assessment.problem.template.ProblemEmbeddedInputTemplate;
import dev.mathops.assessment.problem.template.ProblemMultipleChoiceTemplate;
import dev.mathops.assessment.problem.template.ProblemMultipleSelectionTemplate;
import dev.mathops.assessment.problem.template.ProblemNumericTemplate;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.EPath;
import dev.mathops.commons.PathList;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.ParsingException;
import dev.mathops.commons.parser.xml.XmlContent;
import dev.mathops.commons.parser.xml.XmlContentError;
import dev.mathops.commons.parser.xml.XmlFileFilter;
import dev.mathops.font.BundledFontManager;
import dev.mathops.session.SessionCache;
import dev.mathops.session.txn.handlers.AbstractHandlerBase;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.PrintJob;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serial;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This is an application to open an exam XML file, generate a randomized exam and send it to the printer, including an
 * answer key.
 */
public final class ExamPrinterApp extends ClientBase {

    /** The application title. */
    private static final String APP_TITLE = Res.get(Res.APP_TITLE);

    /** The name of the exam LaTeX file. */
    private static final String EXAM_TEX_FILE_NAME = "exam.tex";

    /** The name of the answers LaTeX file. */
    private static final String ANS_TEX_FILE_NAME = "answers.tex";

    /** The name of the solutions LaTeX file. */
    private static final String SOL_TEX_FILE_NAME = "solutions.tex";

    /** A zero-length array used when allocating other arrays. */
    private static final String[] ZERO_LEN_STRING_ARR = new String[0];

    /** The progress frame. */
    private ProgressFrame frame;

    /** The presented exam the student is to take. */
    private ExamObj exam;

    /** The instruction directory. */
    private File instructionDir;

    /** The exam XML file. */
    private File examFile;

    /**
     * Constructs a new {@code ExamPrinterApp}.
     *
     * @throws UnknownHostException if the hostname could not be resolved into an IP address
     */
    private ExamPrinterApp() throws UnknownHostException {

        super("https", ClientBase.DEFAULT_HOST,
                ClientBase.DEFAULT_PORT, SessionCache.ANONYMOUS_SESSION);
    }

    /**
     * The application's main processing method. This should be called after object construction to run the program.
     */
    private void go() {

        final String[] options = {Res.get(Res.SEND_TO_PRINTER), Res.get(Res.GENERATE_LATEX)};

        try {
            if (chooseExam()) {
                final int rc = JOptionPane.showOptionDialog(null, Res.get(Res.WHAT_TO_DO),
                        APP_TITLE, JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                        options, null);

                if (rc == 0) {
                    printExam();
                } else if (rc == 1) {
                    latexExam();
                }
            }
        } catch (final Exception ex) {
            final List<String> list = new ArrayList<>(100);
            list.add("EXCEPTION: " + ex);

            Throwable current = ex;
            while (current != null) {
                for (final StackTraceElement trace : ex.getStackTrace()) {
                    list.add("    " + trace.toString());
                }
                current = current.getCause();
                if (current != null) {
                    list.add("  CAUSED BY: " + current);
                }
            }

            final String[] messages = list.toArray(ZERO_LEN_STRING_ARR);
            JOptionPane.showMessageDialog(null, messages, APP_TITLE, JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Creates the blocking window, which is a top-level frame that occupies the entire screen. After creating this
     * window, a thread is started that will ensure the window remains at the front, obscuring all other desktop
     * windows. The goal of this window is to prevent the student from accessing other applications (web browser, system
     * calculator, command line, etc.) during an exam.
     *
     * @return {@code true} if the blocking window was created; {@code false} otherwise
     */
    private boolean chooseExam() {

        // First, locate the directory that serves as the base
        final JFileChooser jfc = new JFileChooser();
        File file = PathList.getInstance().get(EPath.SOURCE_1_PATH);

        if ((file != null) && (file.exists())) {
            jfc.setCurrentDirectory(file);
        } else {
            file = PathList.getInstance().get(EPath.SOURCE_2_PATH);

            if ((file != null) && (file.exists())) {
                jfc.setCurrentDirectory(file);
            } else {
                file = PathList.getInstance().get(EPath.SOURCE_3_PATH);

                if ((file != null) && (file.exists())) {
                    jfc.setCurrentDirectory(file);
                } else {
                    // Last ditch - if there's a "P drive", try that
                    file = new File("P:\\");

                    if (file.exists()) {
                        jfc.setCurrentDirectory(file);
                    }
                }
            }
        }

        jfc.setFileFilter(new DirectoryFilter());
        jfc.setDialogTitle(Res.get(Res.FIND_INSTR_PARENT));
        jfc.setAcceptAllFileFilterUsed(false);
        jfc.setMultiSelectionEnabled(false);
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        final boolean success;

        if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File baseDir = jfc.getSelectedFile();

            // User may select the instruction folder inadvertently - if so, go up one.
            if ("instruction".equalsIgnoreCase(baseDir.getName())) {
                baseDir = baseDir.getParentFile();
            }

            // Now select an exam:
            this.instructionDir = new File(baseDir, "instruction");
            file = new File(this.instructionDir, "math");
            jfc.setCurrentDirectory(file);
            jfc.setFileFilter(new XmlFileFilter());
            jfc.setDialogTitle(Res.get(Res.SELECT_EXAM));
            jfc.setAcceptAllFileFilterUsed(false);
            jfc.setMultiSelectionEnabled(false);
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);

            if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                this.examFile = jfc.getSelectedFile();
                success = true;
            } else {
                success = false;
            }
        } else {
            success = false;
        }

        jfc.setVisible(false);

        return success;
    }

    /**
     * Prints the exam.
     */
    private void printExam() {

        this.frame = new ProgressFrame(APP_TITLE, Res.get(Res.PRINTING));
        this.frame.setVisible(true);

        // Set the default size to something scaled for printing.
        AbstractDocObjectTemplate.setDefaultFontSize(16);

        this.frame.updateStatus(10, Res.get(Res.LOADING_EXAM));

        try {
            // Generate the realized exam
            final XmlContent content = FactoryBase.getSourceContent(this.examFile);
            this.exam = ExamFactory.load(content, EParserMode.ALLOW_DEPRECATED);

            if (this.exam == null) {
                dumpLoadErrors(content);
            } else {
                this.frame.updateStatus(20, Res.get(Res.LOADING_QUESTIONS));

                if (this.exam.ref != null) {
                    final String root = this.exam.refRoot;

                    final InstructionalCache cache =
                            InstructionalCache.getInstance(this.instructionDir);

                    // Now we must add the exam's problems, so it can be realized.
                    final int numSect = this.exam.getNumSections();

                    for (int onSect = 0; onSect < numSect; onSect++) {
                        final ExamSection esect = this.exam.getSection(onSect);
                        final int numProb = esect.getNumProblems();

                        for (int onProb = 0; onProb < numProb; onProb++) {
                            final ExamProblem eprob = esect.getProblem(onProb);
                            final int num = eprob.getNumProblems();

                            for (int i = 0; i < num; i++) {
                                AbstractProblemTemplate prob = eprob.getProblem(i);
                                String actualRef = prob.id.startsWith(root) ? prob.id : (root + "." + prob.id);
                                prob = cache.retrieveProblem(actualRef);

                                if (prob != null) {
                                    eprob.setProblem(i, prob);
                                }
                            }
                        }
                    }
                }

                this.frame.updateStatus(40, Res.get(Res.RANDOMIZING));

                final long ser = AbstractHandlerBase.generateSerialNumber(false);

                this.exam.getEvalContext().setPrintTarget(true);

                if (this.exam.realize(false, false, ser)) {

                    this.frame.updateStatus(60, Res.get(Res.LAYING_OUT));

                    // Now we have a randomized exam with known answers.
                    this.frame.updateStatus(70, Res.get(Res.STARTING_PRINT));
                    final PrintJob job = Toolkit.getDefaultToolkit().getPrintJob(this.frame,
                            this.exam.examName, null);

                    if (job != null) {
                        this.frame.updateStatus(80, Res.get(Res.PRINTING_EXAM));
                        doPrintExam(job);
                        this.frame.updateStatus(90, Res.get(Res.PRINTING_ANSWERS));
                        doPrintAnswerKey(job);
                        job.end();
                    }

                    this.frame.updateStatus(100, Res.get(Res.COMPLETE));
                } else {
                    JOptionPane.showMessageDialog(null, Res.get(Res.RANDOMIZING_FAILED), APP_TITLE,
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (final ParsingException ex) {
            Log.warning(ex);
            JOptionPane.showMessageDialog(null, "Failed to parse exam", APP_TITLE,
                    JOptionPane.ERROR_MESSAGE);
        }

        waitAndClose();
    }

    /**
     * Generate LaTeX output from the exam.
     */
    private void latexExam() {

        this.frame = new ProgressFrame(APP_TITLE, Res.get(Res.GENERATING_LATEX));
        this.frame.setVisible(true);

        // Choose the location for the generated LaTeX output.
        final JFileChooser jfc = new JFileChooser();
        jfc.setFileFilter(new DirectoryFilter());
        jfc.setDialogTitle(Res.get(Res.SAVE_LATEX_FILES));
        jfc.setAcceptAllFileFilterUsed(false);
        jfc.setMultiSelectionEnabled(false);
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (jfc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {

            final File dir = jfc.getSelectedFile();

            this.frame.updateStatus(10, Res.get(Res.LOADING_EXAM));

            try {
                final XmlContent content = FactoryBase.getSourceContent(this.examFile);
                this.exam = ExamFactory.load(content, EParserMode.ALLOW_DEPRECATED);

                if (this.exam == null) {
                    dumpLoadErrors(content);
                } else {
                    this.frame.updateStatus(20, Res.get(Res.LOADING_QUESTIONS));

                    if (this.exam.ref != null) {

                        final InstructionalCache cache =
                                InstructionalCache.getInstance(this.instructionDir);

                        // Now we must add the exam's problems, so it can be realized.
                        final int numSect = this.exam.getNumSections();

                        for (int onSect = 0; onSect < numSect; onSect++) {
                            final ExamSection esect = this.exam.getSection(onSect);

                            final int numProb = esect.getNumProblems();

                            for (int onProb = 0; onProb < numProb; onProb++) {
                                final ExamProblem eprob = esect.getProblem(onProb);

                                final int num = eprob.getNumProblems();

                                for (int i = 0; i < num; i++) {
                                    AbstractProblemTemplate prob = eprob.getProblem(i);
                                    prob = cache.retrieveProblem(prob.id);

                                    if (prob != null) {
                                        eprob.setProblem(i, prob);
                                    }
                                }
                            }
                        }
                    }

                    this.frame.updateStatus(40, Res.get(Res.RANDOMIZING));

                    final long ser = AbstractHandlerBase.generateSerialNumber(false);

                    if (this.exam.realize(false, false, ser)) {
                        final int[] fileIndex = {0};
                        final boolean[] overwriteAll = {false};

                        this.frame.updateStatus(60, Res.get(Res.LAYING_OUT));

                        this.frame.updateStatus(70, Res.get(Res.GENERTING_EXAM_TEX));
                        if (doLaTeXExam(dir, fileIndex, overwriteAll)) {
                            this.frame.updateStatus(80, Res.get(Res.GENERTING_ANSWER_TEX));
                            doLaTeXAnswerKey(dir, fileIndex, overwriteAll);
                            this.frame.updateStatus(90, Res.get(Res.GENERTING_SOLUTION_TEX));
                            doLaTeXSolutions(dir, fileIndex, overwriteAll);

                            this.frame.updateStatus(100, Res.get(Res.COMPLETE));
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, Res.get(Res.RANDOMIZING_FAILED),
                                APP_TITLE, JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (final ParsingException ex) {
                Log.warning(ex);
                JOptionPane.showMessageDialog(null, "Failed to parse exam", APP_TITLE,
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        waitAndClose();
    }

    /**
     * Shows a popup with load errors.
     *
     * @param content the XML content with error messages
     */
    private static void dumpLoadErrors(final XmlContent content) {

        final List<XmlContentError> messages = content.getErrors();
        final String[] display = new String[messages.size() + 1];
        display[0] = Res.get(Res.EXAM_LOAD_FAILED);

        final int count = messages.size();
        for (int i = 0; i < count; ++i) {
            display[i + 1] = messages.get(i).toString();
        }

        JOptionPane.showMessageDialog(null, display, APP_TITLE, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Waits 0.5 seconds so the "complete" message can be seen, then closes the frame.
     */
    private void waitAndClose() {

        try {
            Thread.sleep(500L);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        this.frame.setVisible(false);
        this.frame.dispose();
    }

    /**
     * Prints out the exam.
     *
     * @param job the print job to print to
     */
    private void doPrintExam(final PrintJob job) {

        final BundledFontManager fonts = BundledFontManager.getInstance();
        Graphics grx = job.getGraphics();

        fonts.setGraphics(grx);

        final Dimension size = job.getPageDimension();
        final int res = job.getPageResolution();
        int x = res; // 1" left margin
        int y = res; // 1" top margin

        // Print the exam title
        Font font = new Font("Serif", Font.BOLD, 22);
        grx.setFont(font);

        FontMetrics fm = grx.getFontMetrics();
        grx.drawString(this.exam.examName, x, y + fm.getAscent());
        y += fm.getHeight() + fm.getLeading();

        // Print the page number
        final Font pagefont = new Font("SansSerif", Font.PLAIN, 12);
        grx.setFont(pagefont);
        fm = grx.getFontMetrics();

        String str = Res.get(Res.PAGE) + CoreConstants.SPC + "1";
        int width = fm.stringWidth(str);
        grx.drawString(str, (size.width - width) / 2, size.height - (res / 2));

        font = new Font("Serif", Font.PLAIN, 14);
        grx.setFont(font);
        fm = grx.getFontMetrics();

        // Print the time limit if one is set.
        if (this.exam.allowedSeconds != null) {
            int time = this.exam.allowedSeconds.intValue();
            final HtmlBuilder tmlimit = new HtmlBuilder(50);
            tmlimit.add(Res.get(Res.TIME_LIMIT));

            boolean comma = false;

            if (time > (2 * 60 * 60)) {
                tmlimit.add(CoreConstants.SPC, Integer.toString(time / (60 * 60)),
                        CoreConstants.SPC, Res.get(Res.HOURS));
                comma = true;
            } else if (time > (60 * 60)) {
                tmlimit.add(" 1 ", Res.get(Res.HOUR));
                comma = true;
            }

            time = time % (60 * 60);

            if (time > 0) {
                if (comma) {
                    tmlimit.add(CoreConstants.COMMA_CHAR);
                }

                tmlimit.add(CoreConstants.SPC, Integer.toString(time / 60));

                if (time == 1) {
                    tmlimit.add(Res.get(Res.MINUTE));
                } else {
                    tmlimit.add(Res.get(Res.MINUTES));
                }
            }

            grx.drawString(tmlimit.toString(), x, y + fm.getAscent());
        }

        x = size.width / 2;
        grx.drawString(Res.get(Res.SERIAL) + " P" + this.exam.serialNumber, x,
                y + fm.getAscent());
        x = res;
        y += fm.getHeight() + fm.getLeading();

        y += res / 4; // .25" space before instructions

        grx.drawLine(x, y, size.width - x, y);

        // Print the exam instructions, if they will fit.
        DocColumn col = this.exam.instructions;

        if (col != null) {
            col.setColumnWidth(size.width - (2 * res)); // 1" margin on each side
            col.doLayout(this.exam.getEvalContext(), ELayoutMode.TEXT);

            if ((y + col.getHeight()) > (size.height - res)) {

                // Won't fit on one page (!), so dump. (We can't split yet)
                grx.dispose();
                grx = job.getGraphics();
                fonts.setGraphics(grx);
                y = res;
            }

            col.setX(x);
            col.setY(y);
            col.paintComponent(grx, ELayoutMode.TEXT);

            y += col.getHeight();
            grx.drawLine(x, y, size.width - x, y);
        }

        y += res / 2; // .5" space after instructions

        // Now print all the questions, paging as needed
        font = new Font("SansSerif", Font.BOLD, 14);
        grx.setFont(font);
        fm = grx.getFontMetrics();

        int number = 1;
        int pageNum = 1;
        final int numSect = this.exam.getNumSections();

        for (int onSect = 0; onSect < numSect; onSect++) {
            final ExamSection sect = this.exam.getSection(onSect);

            final int numProb = sect.getNumProblems();

            for (int onProb = 0; onProb < numProb; onProb++) {
                final ExamProblem prob = sect.getProblem(onProb);

                // Get the problem number/name
                grx.setFont(font);
                str = (prob.problemName != null) ? prob.problemName : Integer.toString(number);

                final AbstractProblemTemplate selected = prob.getSelectedProblem();

                // Lay out the problem and any associated choices, accumulating
                // total height, so we can paginate if needed.
                col = selected.question;

                if (col == null) {
                    Log.warning(selected.id + CoreConstants.SPC + Res.get(Res.HAD_NO_QUESTION));
                    continue;
                }

                col.setColumnWidth(size.width - (2 * res));
                col.doLayout(selected.evalContext, ELayoutMode.TEXT);
                int height = fm.getHeight() + fm.getLeading() + col.getHeight();

                // Based on the problem type, print additional fields
                if (selected instanceof ProblemNumericTemplate) {
                    height += (res / 6) + (res / 3);
                } else if ((selected instanceof ProblemMultipleChoiceTemplate)
                        || (selected instanceof ProblemMultipleSelectionTemplate)) {
                    height += res / 6;

                    final AbstractProblemMultipleChoiceTemplate probmult =
                            (AbstractProblemMultipleChoiceTemplate) selected;

                    final int count = probmult.getNumPresentedChoices();

                    for (int i = 0; i < count; i++) {
                        final ProblemChoiceTemplate choice = probmult.getPresentedChoice(i);
                        if (choice != null) {
                            col = choice.doc;
                            col.setColumnWidth(size.width - (3 * res));
                            col.doLayout(selected.evalContext, ELayoutMode.TEXT);
                            height += col.getHeight() + (res / 6);
                        }
                    }
                }

                // See if we need to go to the next page.
                if ((y + height) > (size.height - res)) {

                    // New page.
                    grx.dispose();
                    grx = job.getGraphics();
                    grx.setFont(font);
                    fonts.setGraphics(grx);
                    x = res;
                    y = res;

                    // Print the page number
                    ++pageNum;

                    final String str2 = Res.get(Res.PAGE) + CoreConstants.SPC + pageNum;
                    grx.setFont(pagefont);
                    fm = grx.getFontMetrics();
                    width = fm.stringWidth(str2);
                    grx.drawString(str2, (size.width - width) / 2, size.height - (res / 2));
                    grx.setFont(font);
                    fm = grx.getFontMetrics();
                }

                // Print the problem number/name
                grx.drawString(str, x, y + fm.getAscent());

                // Print the question
                y += fm.getHeight() + fm.getLeading();
                col = selected.question;
                col.setX(x);
                col.setY(y);

                col.paintComponent(grx, ELayoutMode.TEXT);
                y += col.getHeight();

                // Based on the problem type, print additional fields
                if (selected instanceof ProblemNumericTemplate) {
                    str = Res.get(Res.ENTER_ANS_HERE) + CoreConstants.SPC;

                    // Draw a 1" wide x 1/3" tall box
                    y += res / 6;

                    int xx = (size.width / 2) - (res / 2) - (res + (fm.stringWidth(str) / 2));
                    grx.drawString(str, xx, y + (res / 6) + (fm.getAscent() / 2) - 1);
                    xx += fm.stringWidth(str);
                    grx.drawRect(xx, y, res, res / 3);
                    y += res / 3;
                } else if ((selected instanceof ProblemMultipleChoiceTemplate)
                        || (selected instanceof ProblemMultipleSelectionTemplate)) {
                    y += res / 6;

                    final AbstractProblemMultipleChoiceTemplate probmult =
                            (AbstractProblemMultipleChoiceTemplate) selected;
                    final int count = probmult.getNumPresentedChoices();
                    x += res;

                    for (int inx = 0; inx < count; inx++) {
                        final ProblemChoiceTemplate choice = probmult.getPresentedChoice(inx);
                        if (choice != null) {
                            col = choice.doc;
                            col.setColumnWidth(size.width - (3 * res));
                            col.doLayout(selected.evalContext, ELayoutMode.TEXT);

                            // See if we need to go to the next page.
                            if ((y + col.getHeight()) > (size.height - res)) {

                                // New page.
                                grx.dispose();
                                grx = job.getGraphics();
                                grx.setFont(font);
                                fonts.setGraphics(grx);
                                x = 2 * res;
                                y = res;

                                // Print the page number
                                pageNum++;

                                final String str2 = Res.get(Res.PAGE) + CoreConstants.SPC + pageNum;
                                grx.setFont(pagefont);
                                fm = grx.getFontMetrics();
                                width = fm.stringWidth(str2);
                                grx.drawString(str2, (size.width - width) / 2, size.height - (res / 2));
                                grx.setFont(font);
                                fm = grx.getFontMetrics();
                            }

                            grx.drawOval(x - (res / 3), y + (col.getHeight() / 2) - (res / 16), res / 8,
                                    res / 8);
                            col.setX(x);
                            col.setY(y);
                            col.paintComponent(grx, ELayoutMode.TEXT);
                            y += col.getHeight() + (res / 6);
                        }
                    }

                    x -= res;
                }

                y += res / 2;
                number++;
            }
        }

        // Make sure the last page gets sent to the printer.
        grx.dispose();
    }

    /**
     * Prints out the answer key to the exam.
     *
     * @param job the print job to print to
     */
    private void doPrintAnswerKey(final PrintJob job) {

        final BundledFontManager fonts = BundledFontManager.getInstance();

        Graphics grx = job.getGraphics();
        fonts.setGraphics(grx);

        final Dimension size = job.getPageDimension();
        final int res = job.getPageResolution();
        int x = res; // 1" left margin
        int y = res; // 1" top margin

        // Print the exam title
        final Font title = new Font("Serif", Font.BOLD, 22);
        grx.setFont(title);

        FontMetrics fm = grx.getFontMetrics();
        grx.drawString(Res.get(Res.ANSWER_KEY_FOR) + CoreConstants.SPC + this.exam.examName, x,
                y + fm.getAscent());
        y += fm.getHeight() + fm.getLeading();

        // Print the exam version and the date it was generated
        final Font plain = new Font("Serif", Font.PLAIN, 14);
        grx.setFont(plain);
        fm = grx.getFontMetrics();
        grx.drawString(Res.get(Res.EXAM_ID) + CoreConstants.SPC + this.exam.examVersion, x,
                y + fm.getAscent());
        x = size.width / 2;
        grx.drawString(Res.get(Res.SERIAL) + " P" + this.exam.serialNumber, x,
                y + fm.getAscent());
        y += fm.getHeight() + fm.getLeading();
        x = res;

        String str = new Date(this.exam.realizationTime).toString();
        grx.drawString(Res.get(Res.GENERATED) + CoreConstants.SPC + str, x, y + fm.getAscent());
        y += fm.getHeight() + fm.getLeading();

        grx.drawLine(x, y, size.width - x, y);
        y += res / 4; // .25" space before answers

        // Now print all the answers, paging as needed
        final Font bold = new Font("SansSerif", Font.BOLD, 14);
        grx.setFont(bold);
        fm = grx.getFontMetrics();

        int number = 1;
        final int numSect = this.exam.getNumSections();

        for (int onSect = 0; onSect < numSect; onSect++) {
            final ExamSection sect = this.exam.getSection(onSect);

            final int numProb = sect.getNumProblems();

            for (int onProb = 0; onProb < numProb; onProb++) {
                final ExamProblem prob = sect.getProblem(onProb);

                // Get the problem number/name
                grx.setFont(bold);
                str = (prob.problemName != null) ? prob.problemName : Integer.toString(number);

                final AbstractProblemTemplate selected = prob.getSelectedProblem();

                // Lay out the problem and any associated choices, accumulating total height, so
                // we can paginate if needed.
                int height = fm.getHeight() + fm.getLeading();

                // Based on the problem type, print additional fields
                if (selected instanceof ProblemNumericTemplate) {
                    height += 2 * (fm.getHeight() + fm.getLeading());
                } else if ((selected instanceof ProblemMultipleChoiceTemplate)
                        || (selected instanceof ProblemMultipleSelectionTemplate)) {
                    final AbstractProblemMultipleChoiceTemplate probmult =
                            (AbstractProblemMultipleChoiceTemplate) selected;

                    final int count = probmult.getNumPresentedChoices();

                    for (int i = 0; i < count; i++) {
                        final ProblemChoiceTemplate choice = probmult.getPresentedChoice(i);
                        if (choice != null) {
                            final Object correct = choice.correct.evaluate(probmult.evalContext);

                            if (Boolean.TRUE.equals(correct)) {
                                final DocColumn col = choice.doc;
                                col.setColumnWidth(size.width - (3 * res));
                                col.doLayout(selected.evalContext, ELayoutMode.TEXT);
                                height += col.getHeight() + (res / 6);
                            }
                        }
                    }
                } else if (selected instanceof ProblemEmbeddedInputTemplate) {
                    final DocColumn ans = ((ProblemEmbeddedInputTemplate) selected).correctAnswer;
                    if (ans != null) {
                        ans.setColumnWidth(size.width - (3 * res));
                        ans.doLayout(selected.evalContext, ELayoutMode.TEXT);
                        height += ans.getHeight() + (res / 6);
                    }
                }

                // See if we need to go to the next page.
                if ((y + height) > (size.height - res)) {
                    // New page.
                    grx.dispose();
                    grx = job.getGraphics();
                    grx.setFont(plain);
                    fonts.setGraphics(grx);
                    x = res;
                    y = res;
                }

                // Print the problem number/name
                grx.setFont(bold);
                grx.drawString(str, x, y + fm.getAscent());
                y += fm.getHeight() + fm.getLeading();
                grx.setFont(plain);

                // Based on the problem type, print additional fields
                if (selected instanceof ProblemNumericTemplate) {

                    Object correct = ((ProblemNumericTemplate) selected).acceptNumber
                            .getCorrectAnswerValue(selected.evalContext);

                    if (correct instanceof Long) {
                        final DecimalFormat fmt = new DecimalFormat();
                        str = Res.get(Res.CORRECT_ANS_IS) + CoreConstants.SPC
                                + fmt.format(((Long) correct).longValue());
                    } else if (correct instanceof Double) {
                        // By default, we truncate reals at 8 decimal points
                        final DecimalFormat fmt = new DecimalFormat();
                        fmt.setMaximumFractionDigits(8);
                        str = Res.get(Res.CORRECT_ANS_IS) + CoreConstants.SPC
                                + fmt.format(((Double) correct).doubleValue());
                    } else {
                        str = Res.get(Res.CORRECT_ANS_IS) + CoreConstants.SPC + correct.toString();
                    }

                    grx.drawString(str, x + res, y + fm.getAscent());
                    y += fm.getHeight() + fm.getLeading();
                    correct = ((ProblemNumericTemplate) selected).acceptNumber
                            .getVarianceValue(selected.evalContext);

                    if (correct != null) {
                        str = Res.get(Res.ALLOWED_VARIANCE_IS) + CoreConstants.SPC + correct;
                        grx.drawString(str, x + res, y + fm.getAscent());
                        y += fm.getHeight() + fm.getLeading();
                    }
                } else if ((selected instanceof ProblemMultipleChoiceTemplate)
                        || (selected instanceof ProblemMultipleSelectionTemplate)) {
                    final AbstractProblemMultipleChoiceTemplate probmult =
                            (AbstractProblemMultipleChoiceTemplate) selected;

                    final int count = probmult.getNumPresentedChoices();
                    x += res;

                    for (int i = 0; i < count; i++) {
                        final ProblemChoiceTemplate choice = probmult.getPresentedChoice(i);
                        if (choice != null) {
                            final Object correct = choice.correct.evaluate(probmult.evalContext);

                            if (Boolean.TRUE.equals(correct)) {
                                final DocColumn col = choice.doc;
                                col.setColumnWidth(size.width - res - x);
                                col.doLayout(selected.evalContext, ELayoutMode.TEXT);
                                col.setX(x);
                                col.setY(y);
                                col.paintComponent(grx, ELayoutMode.TEXT);
                                y += col.getHeight() + (res / 6);
                            }
                        }
                    }

                    x -= res;
                } else if (selected instanceof ProblemEmbeddedInputTemplate) {
                    final DocColumn ans = ((ProblemEmbeddedInputTemplate) selected).correctAnswer;

                    if (ans != null) {
                        ans.setColumnWidth(size.width - res - x);
                        ans.doLayout(selected.evalContext, ELayoutMode.TEXT);
                        ans.setX(x);
                        ans.setY(y);
                        ans.paintComponent(grx, ELayoutMode.TEXT);
                        y += ans.getHeight() + (res / 6);
                    }
                }

                y += res / 4;
                number++;
            }
        }

        // Make sure the last page gets sent to the printer.
        grx.dispose();
    }

    /**
     * Generates the LaTeX output for the exam.
     *
     * @param dir          the directory in which to save the output
     * @param fileIndex    a 1-integer array containing an index used to uniquely name files to be included by the LaTeX
     *                     file. This value should be updated if the method writes any files
     * @param overwriteAll a 1-boolean array whose only entry contains True if the user has selected "overwrite all";
     *                     false to ask the user each time. This method can update this value to true if it is false and
     *                     the user is asked "Overwrite? [YES] [ALL] [NO]" and chooses [ALL]
     * @return {@code true} if successful; {@code false} on error or user cancellation
     */
    private boolean doLaTeXExam(final File dir, final int[] fileIndex, final boolean[] overwriteAll) {

        boolean ok = true;
        final File file = new File(dir, EXAM_TEX_FILE_NAME);

        if (file.exists()) {
            final String[] options = {Res.get(Res.OVERWRITE), Res.get(Res.OVERWRITE_ALL), Res.get(Res.CANCEL)};

            final int rc = JOptionPane.showOptionDialog(null, Res.fmt(Res.FILE_EXISTS, EXAM_TEX_FILE_NAME),
                    Res.get(Res.LATEX_FILE_GEN), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    options, Long.valueOf(2L));

            if (rc == 1) {
                overwriteAll[0] = true;
            } else if (rc == 2) {
                return false;
            }
        }

        final String tex = this.exam.examToLaTeX(dir, fileIndex, overwriteAll);

        try (final FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
            fw.write(tex);
        } catch (final IOException e) {
            final String[] msg = {Res.get(Res.CANT_WRITE_LATEX), e.getMessage()};
            JOptionPane.showMessageDialog(null, msg);
            ok = false;
        }

        return ok;
    }

    /**
     * Generates the LaTeX output for the answer key for the exam.
     *
     * @param dir          the directory in which to save the output
     * @param fileIndex    a 1-integer array containing an index used to uniquely name files to be included by the LaTeX
     *                     file. This value should be updated if the method writes any files
     * @param overwriteAll a 1-boolean array whose only entry contains True if the user has selected "overwrite all";
     *                     false to ask the user each time. This method can update this value to true if it is false and
     *                     the user is asked "Overwrite? [YES] [ALL] [NO]" and chooses [ALL]
     */
    private void doLaTeXAnswerKey(final File dir, final int[] fileIndex, final boolean[] overwriteAll) {

        final File file = new File(dir, ANS_TEX_FILE_NAME);

        if (file.exists() && (!overwriteAll[0])) {
            final String[] options = {Res.get(Res.OVERWRITE), Res.get(Res.OVERWRITE_ALL), Res.get(Res.CANCEL)};

            final int rc = JOptionPane.showOptionDialog(null, Res.fmt(Res.FILE_EXISTS, ANS_TEX_FILE_NAME),
                    Res.get(Res.LATEX_FILE_GEN), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    options, Long.valueOf(2L));

            if (rc == 1) {
                overwriteAll[0] = true;
            } else if (rc == 2) {
                return;
            }
        }

        final String tex = this.exam.answersToLaTeX(dir, fileIndex, overwriteAll);

        try (final FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
            fw.write(tex);
        } catch (final IOException e) {
            final String[] msg = {Res.get(Res.CANT_WRITE_LATEX), e.getMessage()};
            JOptionPane.showMessageDialog(null, msg);
        }
    }

    /**
     * Generates the LaTeX output for the detailed solutions to the exam.
     *
     * @param dir          the directory in which to save the output
     * @param fileIndex    a 1-integer array containing an index used to uniquely name files to be included by the LaTeX
     *                     file. This value should be updated if the method writes any files
     * @param overwriteAll a 1-boolean array whose only entry contains True if the user has selected "overwrite all";
     *                     false to ask the user each time. This method can update this value to true if it is false and
     *                     the user is asked "Overwrite? [YES] [ALL] [NO]" and chooses [ALL]
     */
    private void doLaTeXSolutions(final File dir, final int[] fileIndex, final boolean[] overwriteAll) {

        final File file = new File(dir, SOL_TEX_FILE_NAME);

        if (file.exists() && (!overwriteAll[0])) {
            final String[] options = {Res.get(Res.OVERWRITE), Res.get(Res.OVERWRITE_ALL), Res.get(Res.CANCEL)};

            final int rc = JOptionPane.showOptionDialog(null, Res.fmt(Res.FILE_EXISTS, SOL_TEX_FILE_NAME),
                    Res.get(Res.LATEX_FILE_GEN), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    options, Long.valueOf(2L));

            if (rc == 1) {
                overwriteAll[0] = true;
            } else if (rc == 2) {
                return;
            }
        }

        final String tex = this.exam.solutionsToLaTeX(dir, fileIndex, overwriteAll);

        try (final FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
            fw.write(tex);
        } catch (final IOException e) {
            final String[] msg = {Res.get(Res.CANT_WRITE_LATEX), e.getMessage()};
            JOptionPane.showMessageDialog(null, msg);
        }
    }

    /**
     * Main method that launches the remote testing application.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        try {
            new ExamPrinterApp().go();
        } catch (final Exception ex) {
            Log.warning(ex);
        }
    }

    /**
     * A dialog to indicate progress in printing.
     */
    private static final class ProgressFrame extends JFrame implements IGuiBuilder, Runnable {

        /** Version number for serialization. */
        @Serial
        private static final long serialVersionUID = -1376302000917196242L;

        /** The header text. */
        private final String header;

        /** The progress bar. */
        private JProgressBar progress;

        /** The status text. */
        private JLabel text;

        /** The percentage to which to update progress bar. */
        private int newPercent;

        /** The text to which to update status. */
        private String newText;

        /**
         * Constructs a new {@code ProgressFrame} (should be called from the AWT event thread).
         *
         * @param title  the frame title
         * @param theHeader the frame header
         */
        ProgressFrame(final String title, final String theHeader) {

            super(title);

            this.header = theHeader;

            new GuiBuilderRunner(this).buildUI(this);
        }

        /**
         * Updates status.
         *
         * @param thePercent the percent complete
         * @param theText    the text message
         */
        void updateStatus(final int thePercent, final String theText) {

            synchronized (this) {
                this.newPercent = thePercent;
                this.newText = theText;
            }

            SwingUtilities.invokeLater(this);
        }

        /**
         * Updates the progress percentage and status text.
         */
        @Override
        public void run() {

            synchronized (this) {
                this.progress.setIndeterminate(false);
                this.progress.setValue(this.newPercent);
                this.text.setText(this.newText);
            }
        }

        /**
         * Builds the user interface.
         *
         * @param frame the frame that will host the GUI
         */
        @Override
        public void buildUI(final JFrame frame) {

            setResizable(false);

            final JPanel content = new JPanel(new BorderLayout(10, 10));
            content.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            setContentPane(content);

            content.add(new JLabel(this.header), BorderLayout.PAGE_START);

            this.progress = new JProgressBar();
            this.progress.setMaximum(100);
            this.progress.setPreferredSize(new Dimension(500, 30));
            this.progress.setIndeterminate(true);
            content.add(this.progress, BorderLayout.CENTER);

            this.text = new JLabel();
            content.add(this.text, BorderLayout.PAGE_END);

            pack();

            final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            final Dimension size = getSize();
            setLocation((screen.width - size.width) / 2, (screen.height - size.height) / 3);
        }
    }
}
