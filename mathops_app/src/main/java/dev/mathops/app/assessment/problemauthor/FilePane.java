package dev.mathops.app.assessment.problemauthor;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.document.template.AbstractDocContainer;
import dev.mathops.assessment.document.template.AbstractDocObjectTemplate;
import dev.mathops.assessment.document.template.AbstractDocPrimitiveContainer;
import dev.mathops.assessment.exam.ExamFactory;
import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.assessment.htmlgen.ProblemConverter;
import dev.mathops.assessment.problem.template.AbstractProblemMultipleChoiceTemplate;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.assessment.problem.template.ProblemChoiceTemplate;
import dev.mathops.assessment.problem.template.ProblemEmbeddedInputTemplate;
import dev.mathops.assessment.problem.template.ProblemTemplateFactory;
import dev.mathops.assessment.variable.AbstractVariable;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.xml.IElement;
import dev.mathops.text.parser.xml.INode;
import dev.mathops.text.parser.xml.NonemptyElement;
import dev.mathops.text.parser.xml.XmlContent;
import dev.mathops.text.parser.xml.XmlContentError;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A pane corresponding to a file. If the file can be parsed as an exam or problem, this pane will include components to
 * view/edit that object. If not, it will simply provide a text editor view.
 */
final class FilePane extends JPanel implements ActionListener {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = -7614822721830844526L;

    /** An action command. */
    private static final String OPEN_DIR_CMD = "OPEN_DIR";

    /** An action command. */
    private static final String RELOAD_CMD = "RELOAD";

    /** An action command. */
    private static final String GENERATE_CMD = "GENERATE";

    /** An action command. */
    private static final String LARGER_CMD = "LARGER";

    /** An action command. */
    private static final String SMALLER_CMD = "SMALLER";

    /** An action command. */
    private static final String HTML_CMD = "HTML";

    /** An action command. */
    private static final String SAVE_IMG_CMD = "SAVE_IMG";

    /** Style for HTML &lt;div&gt; to display problems. */
    private static final String DIV_STYLE =
            "style='padding:8px; border:1px solid #b3b3b3; background:#f8f8f8; font-family:serif; font-size:24.0px;'";

    /** The file this pane presents. */
    private final File file;

    /** The problem pane. */
    private ProblemPane problemPane;

    /** The exam pane. */
    private ExamPane examPane;

    /** The lines in the console pane. */
    private final List<String> consoleLines;

    /** The console pane. */
    private final JEditorPane console;

    /** The background color for panes. */
    private final Color bg;

    /** The tabbed pane. */
    private final JTabbedPane tabs;

    /** The content if the "Generated Problem" tab. */
    private GeneratedPane generated;

    /** Flag indicating the problem has not yet been generated. */
    private boolean yetToBeGenerated = true;

    /**
     * Constructs a new {@code FilePane}.
     *
     * @param theFile    the file this pane will present
     * @param windowSize the current size of the window
     */
    FilePane(final File theFile, final Dimension windowSize) {

        super(new BorderLayout());

        this.file = theFile;

        final int consoleHeight = Math.min(100, windowSize.height / 6);
        final int mainH = windowSize.height - consoleHeight - 10;

        this.consoleLines = new ArrayList<>();
        this.console = new JEditorPane();
        this.bg = this.console.getBackground();
        final Color bg2 = new Color(this.bg.getRed(), this.bg.getGreen(), Math.max(0, this.bg.getBlue() - 20));
        this.console.setEditable(false);
        this.console.setContentType("text/html");
        this.console.setBackground(bg2);
        this.console.setBorder(BorderFactory.createEtchedBorder());

        final JPanel inner = new JPanel(new BorderLayout());
        inner.setPreferredSize(new Dimension(1, mainH));

        this.tabs = new JTabbedPane();
        inner.add(this.tabs, BorderLayout.CENTER);

        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEADING, 12, 6));
        inner.add(buttons, BorderLayout.PAGE_END);

        final JButton openDir = new JButton("Open File Directory");
        openDir.setActionCommand(OPEN_DIR_CMD);
        openDir.addActionListener(this);
        buttons.add(openDir);

        final JButton reload = new JButton("Reload from Source XML File");
        reload.setActionCommand(RELOAD_CMD);
        reload.addActionListener(this);
        buttons.add(reload);

        final JButton generate = new JButton("Generate");
        generate.setActionCommand(GENERATE_CMD);
        generate.addActionListener(this);
        buttons.add(generate);

        JButton larger;
        final byte[] largerIcon = FileLoader.loadFileAsBytes(getClass(), "zoom-in-3.png", true);
        if (largerIcon == null) {
            larger = new JButton("Larger");
        } else {
            try {
                final BufferedImage img = ImageIO.read(new ByteArrayInputStream(largerIcon));
                larger = new JButton(new ImageIcon(img));
            } catch (final IOException ex) {
                Log.warning(ex);
                larger = new JButton("Larger");
            }
        }
        larger.setActionCommand(LARGER_CMD);
        larger.addActionListener(this);
        buttons.add(larger);

        JButton smaller;
        final byte[] smallerIcon = FileLoader.loadFileAsBytes(getClass(), "zoom-out-3.png", true);
        if (smallerIcon == null) {
            smaller = new JButton("Smaller");
        } else {
            try {
                final BufferedImage img = ImageIO.read(new ByteArrayInputStream(smallerIcon));
                smaller = new JButton(new ImageIcon(img));
            } catch (final IOException ex) {
                Log.warning(ex);
                smaller = new JButton("Smaller");
            }
        }
        smaller.setActionCommand(SMALLER_CMD);
        smaller.addActionListener(this);
        buttons.add(smaller);

        final JButton toHtml = new JButton("HTML View");
        toHtml.setActionCommand(HTML_CMD);
        toHtml.addActionListener(this);
        buttons.add(toHtml);

        // FIXME: Want a "save images" feature but the problem data model sucks.

        // final JButton saveImages = new JButton("Save Images");
        // saveImages.setActionCommand(SAVE_IMG_CMD);
        // saveImages.addActionListener(this);
        // buttons.add(saveImages);

        final JScrollPane consoleScroll = new JScrollPane(this.console);
        consoleScroll.setPreferredSize(new Dimension(1, consoleHeight));

        final JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inner, consoleScroll);
        add(split, BorderLayout.CENTER);

        loadFile();
    }

    /**
     * Gets the file this pane displays.
     *
     * @return the file
     */
    public File getFile() {

        return this.file;
    }

    /**
     * Updates the console.
     */
    private void updateConsole() {

        final HtmlBuilder htm = new HtmlBuilder(1000);

        for (final String line : this.consoleLines) {
            htm.addln(line);
        }

        this.console.setText(htm.toString());
    }

    /**
     * Loads the file and populates the tabs.
     */
    private void loadFile() {

        this.tabs.removeAll();
        this.problemPane = null;
        this.examPane = null;

        clearConsole();

        final String fileContent = FileLoader.loadFileAsString(this.file, true);
        if (fileContent == null) {
            this.consoleLines.add("<p color='red'>ERROR: Unable to load file.</p>");
        } else {
            // All file types get a "raw" contents panel
            final String nameLC = this.file.getName().toLowerCase(Locale.ROOT);
            final boolean isXml = nameLC.endsWith(".xml");

            if (isXml) {
                if (fileContent.contains("</exam>")) {
                    try {
                        final XmlContent source = new XmlContent(fileContent, false, false);
                        final ExamObj exam = ExamFactory.load(source, EParserMode.NORMAL);

                        appendErrors(source);

                        if (exam != null) {
                            this.examPane = new ExamPane(exam, this.bg);
                            final JScrollPane examScroll = new JScrollPane(this.examPane);
                            examScroll.getVerticalScrollBar().setUnitIncrement(20);
                            examScroll.getVerticalScrollBar().setUnitIncrement(200);
                            this.tabs.addTab("Exam", examScroll);

                            final String preXml = exam.toXmlString(0);
                            addProcessedXmlTab(preXml);

                            final XmlContent preXmlSource = new XmlContent(preXml, false, false);
                            final ExamObj parsedFromPreXml = ExamFactory.load(preXmlSource, EParserMode.NORMAL);
                            final String postXml = parsedFromPreXml.toXmlString(0);

                            if (!postXml.equals(preXml)) {
                                this.consoleLines.add(
                                        "<div color='red'>ERROR: Exam not identical after pre-realize transfer.</div>");
                                logDiff(postXml, preXml);
                            }
                        } else {
                            this.consoleLines.add("<div color='red'>ERROR: Unable to interpret Exam file XML.</div>");
                        }
                    } catch (final ParsingException ex) {
                        this.consoleLines.add("<div color='red'>ERROR: Unable to interpret Exam file XML: "
                                + ex.getMessage() + "</div>");
                    }
                } else if (fileContent.contains("</problem>")
                        || fileContent.contains("</problem-multiple-choice>")
                        || fileContent.contains("</problem-multiple-selection")
                        || fileContent.contains("</problem-numeric>")
                        || fileContent.contains("</problem-embedded-input>")
                        || fileContent.contains("</problem-auto-correct>")
                        || fileContent.contains("</problem-dummy>")) {
                    try {
                        final XmlContent source = new XmlContent(fileContent, false, false);
                        final AbstractProblemTemplate problem =
                                ProblemTemplateFactory.load(source, EParserMode.NORMAL);

                        appendErrors(source);

                        this.problemPane = new ProblemPane(problem, this.bg);
                        final JScrollPane problemScroll = new JScrollPane(this.problemPane);
                        problemScroll.getVerticalScrollBar().setUnitIncrement(20);
                        problemScroll.getVerticalScrollBar().setBlockIncrement(100);
                        this.tabs.addTab("Problem", problemScroll);

                        final String preXml = problem.toXmlString(0);
                        addProcessedXmlTab(preXml);

                        final XmlContent preXmlSource = new XmlContent(preXml, false, false);
                        final AbstractProblemTemplate parsedFromPreXml =
                                ProblemTemplateFactory.load(preXmlSource, EParserMode.NORMAL);
                        final String postXml = parsedFromPreXml.toXmlString(0);

                        if (!postXml.equals(preXml)) {
                            this.consoleLines.add(
                                    "<div color='red'>ERROR: Problem not identical after pre-realize transfer.</div>");
                            logDiff(postXml, preXml);
                        }
                    } catch (final ParsingException ex) {
                        this.consoleLines.add("<div color='red'>ERROR: Unable to interpret Problem file XML: "
                                + ex.getMessage() + "</div>");
                    }
                } else {
                    this.consoleLines.add(
                            "<div color='red'>File does not appear to be either a valid Exam or Problem.</div>");
                }
                addSourceXmlTab(fileContent);
            }

            addGeneratedTab();

            updateConsole();
            this.yetToBeGenerated = true;
        }
    }

    /**
     * Adds a tab with the source file content.
     *
     * @param fileContent the file content
     */
    private void addSourceXmlTab(final String fileContent) {

        final Font mono = new Font(Font.MONOSPACED, Font.PLAIN, 11);

        final JEditorPane editor = new JEditorPane("text/plain", fileContent);
        editor.setFont(mono);
        editor.setEditable(false);
        editor.setBackground(this.bg);
        final JScrollPane editorScroll = new JScrollPane(editor);
        final String rawTabName = "Source XML";

        this.tabs.addTab(rawTabName, editorScroll);
    }

    /**
     * Adds a tab with the XML generated by the parsed problem or exam.
     *
     * @param preXml the file content
     */
    private void addProcessedXmlTab(final String preXml) {

        final Font mono = new Font(Font.MONOSPACED, Font.PLAIN, 11);

        final JEditorPane preXmlPane = //
                new JEditorPane("text/plain", preXml);
        preXmlPane.setFont(mono);
        preXmlPane.setEditable(false);
        preXmlPane.setBackground(this.bg);
        final JScrollPane preXmlScroll = new JScrollPane(preXmlPane);

        this.tabs.addTab("Processed XML", preXmlScroll);
    }

    /**
     * Adds a tab with the realized problem itself.
     */
    private void addGeneratedTab() {

        this.generated = new GeneratedPane(this, this.bg);

        this.tabs.addTab("Generated Problem", this.generated);
    }

    /**
     * Gathers all errors from a {@code XmlContent} and appends them to the list of console lines.
     *
     * @param source the {@code XmlContent}
     */
    private void appendErrors(final XmlContent source) {

        final List<XmlContentError> allErrors = gatherErrors(source);
        if (!allErrors.isEmpty()) {
            this.consoleLines.add("<div color='red'>Errors in parsed pre-realize XML:</div>");
            this.consoleLines.add("<ul>");
            for (final XmlContentError error : allErrors) {
                this.consoleLines.add("<li>" + error + "</li>");
            }
            this.consoleLines.add("</ul>");
        }
    }

    /**
     * Gathers all errors from an {@code XmlContent} object.
     *
     * @param content the {@code XmlContent} object
     * @return the list of all errors
     */
    private static List<XmlContentError> gatherErrors(final XmlContent content) {

        final List<XmlContentError> allErrors = new ArrayList<>(10);
        final List<XmlContentError> mainErrors = content.getErrors();
        if (mainErrors != null) {
            allErrors.addAll(mainErrors);
        }

        final IElement top = content.getTopLevel();
        accumulateErrors(top, allErrors);

        return allErrors;
    }

    /**
     * Recursively accumulates errors from a node and its descendants.
     *
     * @param node   the node
     * @param target the list to which to add accumulated errors
     */
    private static void accumulateErrors(final INode node, final List<? super XmlContentError> target) {

        final List<XmlContentError> nodeErrors = node.getErrors();
        if (nodeErrors != null && !nodeErrors.isEmpty()) {
            if (node instanceof final IElement elem) {
                final String tag = elem.getTagName();
                final String prefix = "In <" + tag + ">: ";
                for (final XmlContentError error : nodeErrors) {
                    target.add(new XmlContentError(error.span, prefix + error.msg));
                }
            } else {
                final String prefix = "In text: ";
                for (final XmlContentError error : nodeErrors) {
                    target.add(new XmlContentError(error.span, prefix + error.msg));
                }
            }
        }

        if (node instanceof final NonemptyElement elem) {
            for (final INode child : elem.getChildrenAsList()) {
                accumulateErrors(child, target);
            }
        }
    }

    /**
     * Print the difference between two strings to a log.
     *
     * @param after  the "after" string
     * @param before the "before" string
     */
    private void logDiff(final String after, final String before) {

        this.consoleLines.add("<div color='blue' border-top='1px solid blue' border-bottom='1px solid blue'>");
        if (after.length() != before.length()) {
            this.consoleLines.add("Length from " + before.length() + " to " + after.length() + "<br/>");
        }

        final int len = Math.min(before.length(), after.length());

        for (int i = 0; i < len; ++i) {
            if (before.charAt(i) != after.charAt(i)) {
                final int start = Math.max(0, i - 40);

                this.consoleLines.add("After:<br/><pre>");
                int max = i + 100;
                if (max > after.length()) {
                    max = after.length();
                }
                final String afterRaw = after.substring(start, max);
                final String afterLt = afterRaw.replace("<", "&lt;");
                final String afterGt = afterLt.replace("<", "&lt;");
                this.consoleLines.add(afterGt);

                this.consoleLines.add("</pre><br/>Before:<br/><pre>");
                max = i + 100;
                if (max > before.length()) {
                    max = before.length();
                }

                final String beforeRaw = before.substring(start, max);
                final String beforeLt = beforeRaw.replace("<", "&lt;");
                final String beforeGr = beforeLt.replace("<", "&lt;");
                this.consoleLines.add(beforeGr);

                this.consoleLines.add("</pre>");
                break;
            }
        }
        this.consoleLines.add("</div>");
    }

    /**
     * Saves all images in the active document to PNG files.
     */
    private void saveImages() {

        final AbstractProblemTemplate problem = this.generated.getProblem();

        if (problem != null) {
            final JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(this.file.getParentFile());
            chooser.setDialogTitle("Choose directory to save document images");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                final String fname = this.file.getName();
                final int lastDot = fname.lastIndexOf('.');
                final String defaultPrefix = lastDot == -1 ? fname : fname.substring(0, lastDot);
                final String prefix =
                        JOptionPane.showInputDialog(this, "Choose a filename prefix", defaultPrefix);

                saveImages(problem, chooser.getSelectedFile(), prefix);
            }
        }
    }

    /**
     * Saves all images in the active document to PNG files.
     *
     * @param problem the realized problem
     * @param dir     the directory in which to save
     * @param prefix  the file prefix
     */
    private void saveImages(final AbstractProblemTemplate problem, final File dir,
                            final String prefix) {

        final EvalContext evalContext = problem.evalContext;

        final List<BufferedImage> images = new ArrayList<>(10);
        if (problem.question != null) {
            gatherImages(evalContext, problem.question, images);
        }
        if (problem.solution != null) {
            gatherImages(evalContext, problem.solution, images);
        }
        if (problem instanceof final ProblemEmbeddedInputTemplate embedded) {
            if (embedded.correctAnswer != null) {
                gatherImages(evalContext, embedded.correctAnswer, images);
            }
        }
        if (problem instanceof final AbstractProblemMultipleChoiceTemplate multChoice) {
            for (final ProblemChoiceTemplate choice : multChoice.getChoices()) {
                if (choice.doc != null) {
                    gatherImages(evalContext, choice.doc, images);
                }
            }
        }

        for (final AbstractVariable var : problem.evalContext.getVariables()) {
            final Object value = var.getValue();
            if (value instanceof final AbstractDocContainer span) {
                gatherImages(evalContext, span, images);
            }
        }

        Log.info("Found " + images.size() + " images");

        int suffix = 1;
        for (final BufferedImage img : images) {
            File target = new File(dir, prefix + "_" + suffix + ".png");
            while (target.exists()) {
                ++suffix;
                target = new File(dir, prefix + "_" + suffix + ".png");
            }

            try {
                ImageIO.write(img, "png", target);
            } catch (final IOException ex) {
                Log.warning("Failed to write image", ex);
            }
        }
    }

    /**
     * Recursively gathers all images in a document container element.
     *
     * @param evalContext the evaluation context
     * @param container   the container
     * @param images      the list to which to add images found
     */
    private static void gatherImages(final EvalContext evalContext, final AbstractDocContainer container,
                                     final List<? super BufferedImage> images) {

        Log.info("Scanning a " + container.getClass().getName());

        for (final AbstractDocObjectTemplate child : container.getChildren()) {
            Log.info("> Child is " + child.getClass().getName());

            if (child instanceof final AbstractDocPrimitiveContainer drawing) {

                BufferedImage offscreen = drawing.getOffscreen();
                if (offscreen == null) {
                    drawing.buildOffscreen(false, evalContext);
                    offscreen = drawing.getOffscreen();
                }

                Log.info("*** Found a " + drawing.getClass().getName() + ", image is " + offscreen);

                if (offscreen != null) {
                    images.add(offscreen);
                }
            }

            if (child instanceof final AbstractDocContainer inner) {
                gatherImages(evalContext, inner, images);
            }
        }
    }

    /**
     * Clears the console.
     */
    void clearConsole() {

        this.consoleLines.clear();
        updateConsole();
    }

    /**
     * Appends a message to the console.
     *
     * @param msg the message
     */
    void logToConsole(final String msg) {

        this.consoleLines.add(msg);
        updateConsole();
    }

    /**
     * Realizes a problem and displays both the results (in the "Realized Problem" pane) and any errors (in the
     * console).
     *
     * @param problem the problem
     */
    private void realize(final AbstractProblemTemplate problem) {

        this.generated.realize(problem, true, true);
        this.yetToBeGenerated = false;
        this.tabs.setSelectedIndex(3);
    }

    /**
     * Generates an HTML document that represents the problem
     *
     * @param problem the problem
     */
    private void generateHtml(final AbstractProblemTemplate problem) {

        if (this.yetToBeGenerated) {
            realize(problem);
        }

        final HtmlBuilder htm = new HtmlBuilder(500);
        htm.addln("<!DOCTYPE html>");
        htm.addln("<head>");

        final String css1 = FileLoader.loadFileAsString(ProblemAuthor.class, "basestyle.css", true);

        if (css1 != null) {
            htm.addln("<style>");
            htm.addln(css1);
            htm.addln("</style>");
        }

        final String css2 = FileLoader.loadFileAsString(ProblemAuthor.class, "style.css", true);

        if (css2 != null) {
            htm.addln("<style>");
            htm.addln(css2);
            htm.addln("</style>");
        }

        htm.addln("<meta charset='utf-8'>");
        htm.addln("<title>", problem.id, "</title>");
        htm.addln("</head>");

        final float fontSize = AbstractDocObjectTemplate.DEFAULT_BASE_FONT_SIZE;
        htm.addln("<body style='background:white;padding:10px;font-size:", Float.toString(fontSize), "px;'>");

        htm.sDiv("page-wrapper");

        htm.sH(3).add("Question:").eH(3);
        htm.addln("<form>");

        htm.sDiv(null, DIV_STYLE);
        if (problem.questionHtml == null) {
            ProblemConverter.populateProblemHtml(problem, new int[]{1});
        }
        htm.addln(problem.insertAnswers(problem.questionHtml));
        htm.eDiv();
        htm.addln("</form>");

        htm.hr();
        htm.sH(3).add("Question with Answer Shown:").eH(3);
        htm.addln("<form>");

        htm.sDiv(null, DIV_STYLE);
        if (problem.answerHtml != null) {
            htm.addln(problem.insertAnswers(problem.answerHtml));
        }
        htm.eDiv();
        htm.addln("</form>");

        htm.hr();
        htm.sH(3).add("Question with full Solution Shown:").eH(3);
        htm.addln("<form>");

        htm.sDiv(null, DIV_STYLE);
        if (problem.solutionHtml != null) {
            htm.addln(problem.insertAnswers(problem.solutionHtml));
        }
        htm.eDiv();
        htm.addln("</form>");

        htm.eDiv(); // page-wrapper

        htm.addln("</body>");
        htm.addln("</html>");

        try {
            final Path tempFile = Files.createTempFile("tempfiles", ".html");
            Files.writeString(tempFile, htm.toString());

            final File file1 = tempFile.toFile();
            Desktop.getDesktop().open(file1);
        } catch (final IOException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Called when a button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (OPEN_DIR_CMD.equals(cmd)) {
            final File dir = this.file.getParentFile();
            try {
                Desktop.getDesktop().open(dir);
            } catch (final IOException ex) {
                Log.warning("Failed to open directory.", ex);
            }
        } else if (RELOAD_CMD.equals(cmd)) {
            loadFile();
        } else if (GENERATE_CMD.equals(cmd)) {
            if (this.problemPane != null) {
                final AbstractProblemTemplate workingProblem = this.problemPane.getWorkingProblem();
                realize(workingProblem);
            } else if (this.examPane != null) {
                // TODO:
            }
        } else if (HTML_CMD.equals(cmd)) {
            if (this.problemPane != null) {
                final AbstractProblemTemplate workingProblem = this.problemPane.getWorkingProblem();
                generateHtml(workingProblem);
            } else if (this.examPane != null) {
                // TODO:
            }
        } else if (LARGER_CMD.equals(cmd)) {
            if (this.generated != null) {
                this.generated.larger();
            }
        } else if (SMALLER_CMD.equals(cmd)) {
            if (this.generated != null) {
                this.generated.smaller();
            }
        } else if (SAVE_IMG_CMD.equals(cmd)) {
            if (this.generated != null) {
                saveImages();
            }
        }
    }
}
