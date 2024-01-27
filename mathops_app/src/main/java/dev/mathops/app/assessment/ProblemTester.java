package dev.mathops.app.assessment;

import dev.mathops.app.exam.CurrentProblemPanel;
import dev.mathops.app.exam.ExamContainerInt;
import dev.mathops.app.problem.AbstractProblemPanelBase;
import dev.mathops.app.problem.AnswerListener;
import dev.mathops.app.problem.ProblemEmbeddedInputPanel;
import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.document.template.AbstractDocObjectTemplate;
import dev.mathops.assessment.exam.EExamSessionState;
import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.assessment.exam.ExamProblem;
import dev.mathops.assessment.exam.ExamSection;
import dev.mathops.assessment.exam.ExamSession;
import dev.mathops.assessment.htmlgen.ProblemConverter;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.assessment.problem.template.ProblemTemplateFactory;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.EPath;
import dev.mathops.commons.PathList;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.ParsingException;
import dev.mathops.commons.parser.xml.XmlContent;
import dev.mathops.commons.parser.xml.XmlContentError;
import dev.mathops.commons.ui.ColorNames;
import dev.mathops.font.BundledFontManager;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serial;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * An application to edit and test problem files.
 */
public final class ProblemTester extends JFrame
        implements Runnable, DocumentListener, UndoableEditListener, AnswerListener, ExamContainerInt {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 1941785184205109930L;

    /** The styled document containing the source XML. */
    private final StyledDocument sourceDoc;

    /** A list of errors encountered in the source XML. */
    private final JList<String> sourceErrors;

    /** A panel in which the generated problem document will be viewed. */
    private final CurrentProblemPanel view;

    /** A list of errors encountered generating the realized problem view. */
    private final JList<String> viewErrors;

    /** The file currently being edited. */
    private File file;

    /** The set of menu items, keyed by item name. */
    private final Map<String, JMenuItem> menuItems;

    /** A manager to support Undo/Redo in text editing. */
    private final UndoManager undo;

    /** A tabbed pane to house source and generated views. */
    private final JTabbedPane tabs;

    /** The split panes used to divide content from errors lists. */
    private final JSplitPane[] splits;

    /** A text panel in which to view and edit the source XML. */
    private final JTextPane source;

    /** The current working directory. */
    private File dir;

    /** The problem currently being edited/tested. */
    private AbstractProblemTemplate prob;

    /** An HTML pane in which the generated HTML will be presented. */
    private final JEditorPane html;

    /** An HTML pane in which the diagnostic tree will be presented. */
    private final JEditorPane diagnostic;

    /** Flag indicating the text area changed and needs to be parsed again. */
    private boolean dirty;

    /** The background color setting. */
    private String background;

    /**
     * Construct a new ProblemTester.
     */
    private ProblemTester() {

        super("Problem Tester (7/17/2008)");

        this.menuItems = new HashMap<>(19);

        final ProblemTesterBuilder builder = new ProblemTesterBuilder(this, this.menuItems);
        try {
            SwingUtilities.invokeAndWait(builder);
        } catch (final InterruptedException | InvocationTargetException ex) {
            Log.warning(ex);
        }

        setFocusableWindowState(true);

        this.tabs = builder.getBuilderTabs();
        this.splits = builder.getBuilderSplits();
        this.undo = builder.getBuilderUndo();
        this.background = builder.getBuilderBackground();
        this.source = builder.getBuilderSource();
        this.sourceDoc = builder.getBuilderSourceDoc();
        this.sourceErrors = builder.getBuilderSourceErrors();
        this.view = builder.getBuilderView();
        this.viewErrors = builder.getBuilderViewErrors();
        this.html = builder.getBuilderHtml();
        this.diagnostic = builder.getBuilderDiagnostic();

        final Runnable resetDividers = new ResetDividers(this.splits);
        try {
            SwingUtilities.invokeAndWait(resetDividers);
        } catch (final InterruptedException | InvocationTargetException ex) {
            Log.warning(ex);
        }

        // Set the format defaults
        AbstractDocObjectTemplate.setDefaultFontName(BundledFontManager.SERIF);
        AbstractDocObjectTemplate.setDefaultFontSize(24);

        // DocObject.setShowBounds(true);
        // DocObject.setShowBaseline(true);
        // DocObject.setShowCenterline(true);
        // DocObject.setShowAligner(true);

        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(new Dimension(screen.width / 2, (screen.height << 3) / 10));
    }

    /**
     * Get the problem that is currently being edited.
     *
     * @return The open problem.
     */
    private AbstractProblemTemplate getProblem() {

        return this.prob;
    }

    /**
     * Handler for actions caused by menu item selections.
     *
     * @param e The action event to be processed.
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        if ("New".equals(cmd)) {
            doNew();
        } else if ("Open...".equals(cmd)) {
            doOpen();
        } else if ("Close".equals(cmd)) {
            doClose();
        } else if ("Save".equals(cmd)) {
            doSave();
        } else if ("Save As...".equals(cmd)) {
            doSaveAs();
        } else if ("Export".equals(cmd)) {
            doExport();
        } else if ("Cut".equals(cmd)) {
            this.source.cut();
        } else if ("Copy".equals(cmd)) {
            this.source.copy();
        } else if ("Paste".equals(cmd)) {
            this.source.paste();
        } else if ("Randomize".equals(cmd)) {
            randomize();
        } else if ("Editor".equals(cmd)) {
            this.tabs.setSelectedIndex(0);

            if (this.file != null) {
                this.menuItems.get("Undo").setEnabled(this.undo.canUndo());
                this.menuItems.get("Redo").setEnabled(this.undo.canRedo());
                this.menuItems.get("Cut").setEnabled(true);
                this.menuItems.get("Copy").setEnabled(true);
                this.menuItems.get("Paste").setEnabled(true);
            }
        } else if ("Generated View".equals(cmd) || "HTML".equals(cmd)) {
            this.tabs.setSelectedIndex(1);
            this.menuItems.get("Undo").setEnabled(false);
            this.menuItems.get("Redo").setEnabled(false);
            this.menuItems.get("Cut").setEnabled(false);
            this.menuItems.get("Copy").setEnabled(false);
            this.menuItems.get("Paste").setEnabled(false);
        } else if ("Diagnostics".equals(cmd)) {
            this.tabs.setSelectedIndex(2);
            this.menuItems.get("Undo").setEnabled(false);
            this.menuItems.get("Redo").setEnabled(false);
            this.menuItems.get("Cut").setEnabled(false);
            this.menuItems.get("Copy").setEnabled(false);
            this.menuItems.get("Paste").setEnabled(false);
        } else if ("Export Answers".equals(cmd)) {
            exportAnswers();
        } else if ("Stress-Test".equals(cmd)) {
            stressTest();
        } else if ("Undo".equals(cmd)) {

            try {
                this.undo.undo();
            } catch (final CannotUndoException ex) {
                // No action
            }

            this.menuItems.get("Undo")
                    .setEnabled(this.undo.canUndo());
            this.menuItems.get("Redo")
                    .setEnabled(this.undo.canRedo());
        } else if ("Redo".equals(cmd)) {

            try {
                this.undo.redo();
            } catch (final CannotRedoException ex) {
                // No action
            }

            this.menuItems.get("Undo")
                    .setEnabled(this.undo.canUndo());
            this.menuItems.get("Redo")
                    .setEnabled(this.undo.canRedo());
        } else if ("Exit".equals(cmd)) {
            setVisible(false);
            dispose();
        } else if ("ShowEntry".equals(cmd)) {
            doToggleEntry();
        } else if ("ShowAnswers".equals(cmd) || "ShowSolutions".equals(cmd)) {
            doToggleAnswersSolutions();
        } else if ("Font Size 10".equals(cmd)) {
            doSetFontSize(10);
        } else if ("Font Size 12".equals(cmd)) {
            doSetFontSize(12);
        } else if ("Font Size 18".equals(cmd)) {
            doSetFontSize(18);
        } else if ("Font Size 24".equals(cmd)) {
            doSetFontSize(24);
        } else if ("Font Size 30".equals(cmd)) {
            doSetFontSize(30);
        } else if ("Font Size 36".equals(cmd)) {
            doSetFontSize(36);
        } else if ("Font Size 42".equals(cmd)) {
            doSetFontSize(42);
        } else if ("Color White".equals(cmd)) {
            doSetColor("white");
        } else if ("Color Green".equals(cmd)) {
            doSetColor("DarkSeaGreen1");
        } else if ("Color Pink".equals(cmd)) {
            doSetColor("misty rose");
        } else if ("Color Yellow".equals(cmd)) {
            doSetColor("LemonChiffon");
        } else if ("Color Blue".equals(cmd)) {
            doSetColor("LightSteelBlue1");
        } else if ("Larger".equals(cmd)) {
            this.view.larger();
        } else if ("Smaller".equals(cmd)) {
            this.view.smaller();
        }
    }

    /**
     * Handle selection of the "File-New" menu item.
     */
    private void doNew() {

        final int rc;

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        // If the loaded file has changed, confirm discard of changes
        if (getTitle().endsWith(" *")) {
            rc = JOptionPane.showConfirmDialog(this, "Discard changes to open document?", "Create New File",
                    JOptionPane.YES_NO_CANCEL_OPTION);

            if (rc != JOptionPane.YES_OPTION) {
                return;
            }
        }

        this.file = null;
        setTitle("untitled");

        final HtmlBuilder builder = new HtmlBuilder(200);
        builder.addln("<?xml version=\"1.0\"?>");
        builder.addln("<problem type=\"\">");
        builder.addln("</problem>");

        this.menuItems.get("Save As...").setEnabled(true);
        this.menuItems.get("Export").setEnabled(true);
        this.menuItems.get("Close").setEnabled(true);
        this.menuItems.get("Editor").setEnabled(true);
        this.menuItems.get("Generated View").setEnabled(true);
        this.menuItems.get("HTML").setEnabled(true);
        this.menuItems.get("Diagnostics").setEnabled(true);
        this.menuItems.get("Randomize").setEnabled(true);
        this.menuItems.get("Export Answers").setEnabled(true);
        this.menuItems.get("Stress-Test").setEnabled(true);
        this.menuItems.get("Cut").setEnabled(true);
        this.menuItems.get("Copy").setEnabled(true);
        this.menuItems.get("Paste").setEnabled(true);

        this.source.setBackground(Color.WHITE);
        this.source.setText(builder.toString());
        this.dirty = true;

        this.tabs.setSelectedIndex(0);
        this.splits[0].setDividerLocation(0.95);
    }

    /**
     * Handle selection of the "File-Open" menu item.
     */
    private void doOpen() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        // If the loaded file has changed, confirm discard of changes
        if (getTitle().endsWith(" *")) {
            final int rc = JOptionPane.showConfirmDialog(this, "Discard changes to open document?",
                    "Open File", JOptionPane.YES_NO_CANCEL_OPTION);

            if (rc != JOptionPane.YES_OPTION) {
                return;
            }
        }

        final JFileChooser jfc = makeFileChooser();

        final int rc = jfc.showOpenDialog(this);

        if (rc == JFileChooser.APPROVE_OPTION) {
            this.file = jfc.getSelectedFile();
            this.dir = this.file.getParentFile();
            reload();
            setTitle(this.file.getName());
        }

        this.menuItems.get("Save").setEnabled(false);
        this.menuItems.get("Save As...").setEnabled(true);
        this.menuItems.get("Export").setEnabled(true);
        this.menuItems.get("Close").setEnabled(true);
        this.menuItems.get("Editor").setEnabled(true);
        this.menuItems.get("Generated View").setEnabled(true);
        this.menuItems.get("HTML").setEnabled(true);
        this.menuItems.get("Diagnostics").setEnabled(true);
        this.menuItems.get("Randomize").setEnabled(true);
        this.menuItems.get("Export Answers").setEnabled(true);
        this.menuItems.get("Stress-Test").setEnabled(true);
        this.menuItems.get("Cut").setEnabled(true);
        this.menuItems.get("Copy").setEnabled(true);
        this.menuItems.get("Paste").setEnabled(true);
    }

    /**
     * Handle selection of the "File-Close" menu item.
     */
    private void doClose() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        if (getTitle().endsWith(" *")) {
            final int rc = JOptionPane.showConfirmDialog(this, "Discard changes to open document?",
                    "Open File", JOptionPane.YES_NO_CANCEL_OPTION);

            if (rc != JOptionPane.YES_OPTION) {
                return;
            }
        }

        this.source.setText(CoreConstants.EMPTY);
        this.source.setBackground(Color.LIGHT_GRAY);
        setTitle(CoreConstants.EMPTY);
        this.dirty = false;
        this.file = null;

        this.tabs.setSelectedIndex(0);
        this.splits[0].setDividerLocation(0.95);

        this.menuItems.get("Save").setEnabled(false);
        this.menuItems.get("Save As...").setEnabled(false);
        this.menuItems.get("Export").setEnabled(false);
        this.menuItems.get("Close").setEnabled(false);
        this.menuItems.get("Editor").setEnabled(false);
        this.menuItems.get("Generated View").setEnabled(false);
        this.menuItems.get("HTML").setEnabled(false);
        this.menuItems.get("Diagnostics").setEnabled(false);
        this.menuItems.get("Randomize").setEnabled(false);
        this.menuItems.get("Export Answers").setEnabled(true);
        this.menuItems.get("Stress-Test").setEnabled(false);
        this.menuItems.get("Cut").setEnabled(false);
        this.menuItems.get("Copy").setEnabled(false);
        this.menuItems.get("Paste").setEnabled(false);
    }

    /**
     * Handle selection of the "File-Save" menu item.
     */
    private void doSave() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        if (this.file == null) {
            doSaveAs();
        } else {

            try (final FileWriter fw = new FileWriter(this.file, StandardCharsets.UTF_8)) {
                fw.write(this.source.getText());

                setTitle(this.file.getName());
            } catch (final IOException ex) {
                Log.warning(ex);
            }
        }
    }

    /**
     * Handle selection of the "File-Save-As" menu item.
     */
    private void doSaveAs() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        final JFileChooser jfc = makeFileChooser();
        if (jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            this.file = jfc.getSelectedFile();
            doSave();
        }
    }

    /**
     * Handle selection of the "File-Export" menu item.
     */
    private void doExport() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        if (this.view.getVisiblePanel() instanceof final AbstractProblemPanelBase panel) {

            final JFileChooser jfc = makeFileChooser();
            if (jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                panel.export(jfc.getSelectedFile());
            }
        }
    }

    /**
     * Handle changes to the choice / entry visibility selection.
     */
    private void doToggleEntry() {

        boolean visible = false;

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        final JCheckBoxMenuItem item = (JCheckBoxMenuItem) this.menuItems.get("ShowEntry");

        if (item != null) {
            visible = item.isSelected();
        }

        if (this.view.getVisiblePanel() instanceof final AbstractProblemPanelBase panel) {
            panel.setEntryVisibility(visible);
            panel.setEnabled(true);
        }
    }

    /**
     * Handle changes to the answer / solution visibility selection.
     */
    private void doToggleAnswersSolutions() {

        JCheckBoxMenuItem item;
        boolean answers = false;
        boolean solutions = false;

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        item = (JCheckBoxMenuItem) this.menuItems.get("ShowAnswers");

        if (item != null) {
            answers = item.isSelected();
        }

        item = (JCheckBoxMenuItem) this.menuItems.get("ShowSolutions");

        if (item != null) {
            solutions = item.isSelected();
        }

        this.view.setShowAnswersAndSolutions(answers, solutions);
    }

    /**
     * Set the default font size for document objects. This will be used on the next re-generation of the problem.
     *
     * @param size The new font size.
     */
    private static void doSetFontSize(final int size) {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        AbstractDocObjectTemplate.setDefaultFontSize(size);
    }

    /**
     * Set the background color for the generated view.
     *
     * @param colorName The name of the new color to set as the background.
     */
    private void doSetColor(final String colorName) {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        this.background = colorName;

        // If we have a view, change it's color.
        if (this.view != null) {
            this.view.getExamSession().getExam().setBackgroundColor(this.background,
                    ColorNames.getColor(this.background));
            this.view.setCurrentProblem(0, 0, this, false, false);
        }
    }

    /**
     * Writes the current exam state to disk.
     */
    @Override
    public void doCacheExamState() {

        // Empty
    }

    /**
     * Selects a problem to present in the current problem panel.
     *
     * @param sectionIndex the index of the section
     * @param problemIndex the index of the problem
     */
    @Override
    public void pickProblem(final int sectionIndex, final int problemIndex) {

        // Empty
    }

    /**
     * Called when a timer expires.
     */
    @Override
    public void timerExpired() {

        // Empty
    }

    /**
     * Gets the {@code JFrame}.
     *
     * @return the frame
     */
    @Override
    public JFrame getFrame() {

        return null;
    }

    /**
     * Make a file chooser object that is set to the most recent directory used, or to a default directory if this is
     * the first use.
     *
     * @return The file chooser.
     */
    private JFileChooser makeFileChooser() {

        final JFileChooser jfc = new JFileChooser();

        if (this.dir != null) {
            jfc.setCurrentDirectory(this.dir);
        } else {

            // Set a default directory if possible
            final PathList paths = PathList.getInstance();
            File theDir = paths.get(EPath.SOURCE_1_PATH);

            if (theDir != null && theDir.exists()) {
                jfc.setCurrentDirectory(theDir);
            } else {
                theDir = paths.get(EPath.SOURCE_2_PATH);

                if (theDir != null && theDir.exists()) {
                    jfc.setCurrentDirectory(theDir);
                } else {
                    theDir = paths.get(EPath.SOURCE_3_PATH);

                    if (theDir != null && theDir.exists()) {
                        jfc.setCurrentDirectory(theDir);
                    }
                }
            }
        }

        return jfc;
    }

    /**
     * Handle selection of the "File-Reload" menu item.
     */
    private void reload() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        if (this.file != null) {
            this.tabs.setSelectedIndex(0);
            this.splits[0].setDividerLocation(0.95);

            final HtmlBuilder builder = new HtmlBuilder((int) this.file.length() * 10 / 9);

            try (final FileReader filr = new FileReader(this.file, StandardCharsets.UTF_8)) {

                try (final BufferedReader bufr = new BufferedReader(filr)) {
                    for (String line = bufr.readLine(); line != null; line = bufr.readLine()) {
                        builder.addln(line);
                    }
                }

                this.source.setBackground(Color.WHITE);
                this.source.setText(builder.toString());
                this.undo.discardAllEdits();
            } catch (final IOException ex) {
                this.source.setText(CoreConstants.EMPTY);
                Log.warning(ex);
            }

            this.dirty = true;
        }
    }

    /**
     * Generate a realized version of the problem and place the resulting panel in the user interface.
     */
    private void randomize() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        if (this.prob == null) {
            return;
        }

        // Simulate network transfer
        final String xml = this.prob.toXmlString(0);
        // Log.info(xml);

        try {
            final XmlContent content = new XmlContent(xml, false, false);
            this.prob = ProblemTemplateFactory.load(content, EParserMode.NORMAL);

            // Clear out any existing data in the generated view tab.
            this.view.showInstructions();
            this.viewErrors.setListData(new Vector<>(10));
            this.html.setText(CoreConstants.EMPTY);
            this.diagnostic.setText(CoreConstants.EMPTY);

            final EvalContext params = this.prob.evalContext;
            if (params == null) {
                Log.warning("No parameter set in problem");
            } else {
                this.prob.realize(this.prob.evalContext);

                final boolean trees = this.menuItems.get("Include Document Trees").isSelected();

                final int[] id = {123};
                ProblemConverter.populateProblemHtml(this.prob, id);
                this.html.setText(this.prob.questionHtml);

                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try (final PrintStream ps = new PrintStream(baos, false, StandardCharsets.UTF_8)) {
                    this.prob.printDiagnostics(ps, trees);
                }
                this.diagnostic.setText(baos.toString());

                final ExamObj exam = this.view.getExamSession().getExam();

                exam.getSection(0).getProblem(0).clearProblems();
                exam.getSection(0).getProblem(0).addProblem(this.prob);
                exam.getSection(0).getProblem(0).setSelectedProblem(this.prob);

                this.view.setCurrentProblem(0, 0, this, false, false);

                final JPanel panel = this.view.getVisiblePanel();

                if (panel instanceof AbstractProblemPanelBase) {
                    ((AbstractProblemPanelBase) panel).setEntryVisibility(true);
                    panel.setEnabled(true);
                    ((AbstractProblemPanelBase) panel).addAnswerListener(this);
                }
            }

            doToggleAnswersSolutions();
            doToggleEntry();

            if (this.tabs.getSelectedIndex() == 0) {
                this.tabs.setSelectedIndex(1);
                this.menuItems.get("Undo").setEnabled(false);
                this.menuItems.get("Redo").setEnabled(false);
                this.menuItems.get("Cut").setEnabled(false);
                this.menuItems.get("Copy").setEnabled(false);
                this.menuItems.get("Paste").setEnabled(false);
            }
        } catch (final ParsingException ex) {
            Log.warning("Unable to parse problem", ex);
        }

        this.splits[0].setDividerLocation(0.95);
        this.splits[1].setDividerLocation(0.95);
        this.tabs.revalidate();
        this.tabs.repaint();
    }

    /**
     * Build the answer update that the problem would generate in a real exam, and display it.
     */
    private void exportAnswers() {

        final Object[] ans = this.prob.getAnswer();
        final HtmlBuilder builder = new HtmlBuilder(50);

        if (ans != null) {
            final int len = ans.length;
            for (int i = 0; i < len; ++i) {
                if (i > 0) {
                    builder.add(", ");
                }

                if (ans[i] != null) {
                    builder.add(ans[i].toString());
                } else {
                    builder.add("null");
                }
            }
        } else {
            builder.add("(no answer)");
        }

        if (this.prob.isCorrect(ans)) {
            builder.add(" [correct]");
        } else {
            builder.add(" [incorrect]");
        }

        JOptionPane.showMessageDialog(null, builder.toString());
    }

    /**
     * Generate a realized version of the problem and place the resulting panel in the user interface.
     */
    private static void stressTest() {

        JOptionPane.showMessageDialog(null, "This feature is not yet implemented");
    }

    /**
     * Handler for changes to the document content.
     *
     * @param e The document change event.
     */
    @Override
    public void changedUpdate(final DocumentEvent e) {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        if (!this.dirty) {
            setMenus();
            this.dirty = true;
        }
    }

    /**
     * Handler for insertions of text to the document content.
     *
     * @param e The document change event.
     */
    @Override
    public void insertUpdate(final DocumentEvent e) {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        if (!this.dirty) {
            setMenus();
            this.dirty = true;
        }
    }

    /**
     * Handler for removals of text from the document content.
     *
     * @param e The document change event.
     */
    @Override
    public void removeUpdate(final DocumentEvent e) {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        if (!this.dirty) {
            setMenus();
            this.dirty = true;
        }
    }

    /**
     * Routine to set the state of the menus and title bar. To be called from within the AWT event thread.
     */
    private void setMenus() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        if (SwingUtilities.isEventDispatchThread()) {

            if (this.file != null) {
                setTitle(this.file.getName() + " *");
                this.menuItems.get("Save").setEnabled(true);
            } else {
                setTitle("untitled *");
            }

            this.menuItems.get("Undo")
                    .setEnabled(this.undo.canUndo());
            this.menuItems.get("Redo")
                    .setEnabled(this.undo.canRedo());
        }
    }

    /**
     * Implementation of the {@code UndoableEditListener} interface to handle undoable edits.
     *
     * @param e The event associated with the undoable edit.
     */
    @Override
    public void undoableEditHappened(final UndoableEditEvent e) {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        // Styles are added by parsing operations, we don't want to undo them
        if ("style change".equals(e.getEdit().getPresentationName())) {
            return;
        }

        this.undo.addEdit(e.getEdit());
        setMenus();
        this.dirty = true;
    }

    /**
     * The main thread loop, which watches for the content of the source pane to change, triggering a new parse of the
     * source file, and an update to the displayed errors.
     */
    @Override
    public void run() {

        final Runnable parser = new Reparser(this);

        while (isVisible()) {

            if (SwingUtilities.isEventDispatchThread()) {
                parser.run();
            } else {
                SwingUtilities.invokeLater(parser);
            }

            try {
                Thread.sleep(250L);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Parse the document source again, regenerating the errors list. This runs in the run method thread, not the AWT
     * event thread, so any updates to the GUI need to be invoked by {@code SwingUtilities}.
     */
    private void reparse() {

        // Clear the generated view tab and file parse errors list
        final Runnable clear = new ClearDocuments(this);

        if (SwingUtilities.isEventDispatchThread()) {
            clear.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(clear);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (final InvocationTargetException ex) {
                Log.warning(ex);
            }
        }

        // If the document is empty, do nothing. Otherwise, parse it.
        final String txt = this.source.getText();
        if (txt == null || txt.isEmpty()) {
            this.prob = null;
            return;
        }

        try {
            final XmlContent content = new XmlContent(txt, false, false);
            this.prob = ProblemTemplateFactory.load(content, EParserMode.NORMAL);

            // Now update the GUI
            final boolean trees = this.menuItems.get("Include Document Trees").isSelected();
            final Runnable display = new DisplayParseOutcome(this, content, this.diagnostic, trees, true);

            if (SwingUtilities.isEventDispatchThread()) {
                display.run();
            } else {
                try {
                    SwingUtilities.invokeAndWait(display);
                } catch (final InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (final InvocationTargetException ex) {
                    Log.warning(ex);
                }
            }
        } catch (final ParsingException ex) {
            Log.warning("Unable to parse problem.", ex);
        }
    }

    /**
     * Get the current value of the dirty flag.
     *
     * @return The dirty flag value.
     */
    private boolean isDirty() {

        return this.dirty;
    }

    /**
     * Clear the dirty flag.
     */
    private void setClean() {

        this.dirty = false;
    }

    /**
     * Record a student's answer.
     *
     * @param answer A list of answer objects, whose type depends on the type of problem for which the answer is being
     *               submitted. The answers will be passed directly into the {@code PresentedProblem} object.
     */
    @Override
    public void recordAnswer(final Object[] answer) {

        final JPanel pnl = this.view.getVisiblePanel();

        if (pnl instanceof ProblemEmbeddedInputPanel) {
            ((ProblemEmbeddedInputPanel) pnl).updateCorrectnessLabel();
        }
    }

    /**
     * Clear a student's answer.
     */
    @Override
    public void clearAnswer() { /* Empty */

    }

    /**
     * Main method that launches the application.
     *
     * @param args Command-line arguments (currently ignored).
     */
    public static void main(final String... args) {

        final ProblemTester obj = new ProblemTester();

        obj.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        obj.run();
    }

    /**
     * A runnable class that clears the contents of the generated document window and the errors lists, as part of the
     * new parse operation.
     */
    private final class ProblemTesterBuilder implements Runnable {

        /** A tabbed pane to house source and generated views. */
        private JTabbedPane builderTabs;

        /** The split panes used to divide content from errors lists. */
        private JSplitPane[] builderSplits;

        /** A manager to support Undo/Redo in text editing. */
        private UndoManager builderUndo;

        /** The background color setting. */
        private String builderBackground;

        /** A text panel in which to view and edit the source XML. */
        private JTextPane builderSource;

        /** The styled document containing the source XML. */
        private StyledDocument builderSourceDoc;

        /** A list of errors encountered in the source XML. */
        private JList<String> builderSourceErrors;

        /** A panel in which the generated problem document will be viewed. */
        private CurrentProblemPanel builderView;

        /** A list of errors encountered generating the realized problem view. */
        private JList<String> builderViewErrors;

        /** A panel in which to display the generated HTML. */
        private JEditorPane builderHtml;

        /** An HTML pane in which the diagnostic tree will be presented. */
        private JEditorPane builderDiagnostic;

        /** A hash table to which to add menu items. */
        private final Map<? super String, ? super JMenuItem> builderMenuItems;

        /** The listener that is to receive action events. */
        private final ProblemTester listener;

        /**
         * Create a new {@code ProblemTesterBuilder}.
         *
         * @param theListener  the program to receive events
         * @param theMenuItems a hash table to which to add menu items
         */
        ProblemTesterBuilder(final ProblemTester theListener,
                             final Map<? super String, ? super JMenuItem> theMenuItems) {

            this.listener = theListener;
            this.builderMenuItems = theMenuItems;
        }

        /**
         * Gets the tabbed pane to house source and generated views.
         *
         * @return the pane
         */
        JTabbedPane getBuilderTabs() {

            return this.builderTabs;
        }

        /**
         * Gets the split pane used to divide content from errors lists.
         *
         * @return the pane
         */
        JSplitPane[] getBuilderSplits() {

            return this.builderSplits;
        }

        /**
         * Gets the manager to support Undo/Redo in text editing.
         *
         * @return the manager
         */
        UndoManager getBuilderUndo() {

            return this.builderUndo;
        }

        /**
         * Gets the background color setting.
         *
         * @return the color
         */
        String getBuilderBackground() {

            return this.builderBackground;
        }

        /**
         * Gets the text pane in which to view and edit the source XML.
         *
         * @return the pane
         */
        JTextPane getBuilderSource() {

            return this.builderSource;
        }

        /**
         * Gets the styled document containing the source XML.
         *
         * @return the document
         */
        StyledDocument getBuilderSourceDoc() {

            return this.builderSourceDoc;
        }

        /**
         * Gets the list of errors encountered in the source XML.
         *
         * @return the list
         */
        JList<String> getBuilderSourceErrors() {

            return this.builderSourceErrors;
        }

        /**
         * Gets the panel in which the generated problem document will be viewed.
         *
         * @return the panel
         */
        CurrentProblemPanel getBuilderView() {

            return this.builderView;
        }

        /**
         * Gets the list of errors encountered generating the realized problem view.
         *
         * @return the list
         */
        JList<String> getBuilderViewErrors() {

            return this.builderViewErrors;
        }

        /**
         * Gets the HTML pane in which the generated HTML will be presented.
         *
         * @return the pane
         */
        JEditorPane getBuilderHtml() {

            return this.builderHtml;
        }

        /**
         * Gets the HTML pane in which the diagnostic tree will be presented.
         *
         * @return the pane
         */
        JEditorPane getBuilderDiagnostic() {

            return this.builderDiagnostic;
        }

        /**
         * Method to run in the AWT event thread to construct the GUI.
         */
        @Override
        public void run() {

            if (!SwingUtilities.isEventDispatchThread()) {
                Log.warning(Res.get(Res.NOT_AWT_THREAD));
            }

            final ExamObj exam = new ExamObj();
            final ExamSection sect = new ExamSection();
            final ExamProblem problem = new ExamProblem(exam);
            exam.addSection(sect);
            sect.addProblem(problem);
            final ExamSession examSession = new ExamSession(EExamSessionState.INTERACTING, exam);

            final int[] order = new int[1];
            sect.setProblemOrder(order);

            this.builderSplits = new JSplitPane[2];
            this.builderUndo = new UndoManager();
            this.builderTabs = new JTabbedPane();

            this.builderTabs.setOpaque(true);
            this.builderTabs.setPreferredSize(new Dimension(700, 700));
            setContentPane(this.builderTabs);

            final JMenuBar bar = new JMenuBar();
            setJMenuBar(bar);

            JMenu menu = new JMenu("File");
            menu.setMnemonic('F');
            bar.add(menu);

            JMenuItem item = new JMenuItem("New", 'N');
            item.addActionListener(this.listener);
            item.setAccelerator(KeyStroke.getKeyStroke('N', InputEvent.CTRL_DOWN_MASK));
            menu.add(item);
            this.builderMenuItems.put(item.getActionCommand(), item);

            item = new JMenuItem("Open...", 'O');
            item.addActionListener(this.listener);
            item.setAccelerator(KeyStroke.getKeyStroke('O', InputEvent.CTRL_DOWN_MASK));
            menu.add(item);
            this.builderMenuItems.put(item.getActionCommand(), item);

            item = new JMenuItem("Close", 'C');
            item.addActionListener(this.listener);
            item.setEnabled(false);
            menu.add(item);
            this.builderMenuItems.put(item.getActionCommand(), item);

            menu.add(new JSeparator());

            item = new JMenuItem("Save", 'S');
            item.addActionListener(this.listener);
            item.setAccelerator(KeyStroke.getKeyStroke('S', InputEvent.CTRL_DOWN_MASK));
            item.setEnabled(false);
            menu.add(item);
            this.builderMenuItems.put(item.getActionCommand(), item);

            item = new JMenuItem("Save As...", 'A');
            item.addActionListener(this.listener);
            item.setAccelerator(KeyStroke.getKeyStroke('S',
                    InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
            item.setEnabled(false);
            menu.add(item);
            this.builderMenuItems.put(item.getActionCommand(), item);

            menu.add(new JSeparator());

            item = new JMenuItem("Export as image...");
            item.setActionCommand("Export");
            item.addActionListener(this.listener);
            item.setEnabled(false);
            menu.add(item);
            this.builderMenuItems.put(item.getActionCommand(), item);

            menu.add(new JSeparator());

            item = new JMenuItem("Exit", 'E');
            item.addActionListener(this.listener);
            menu.add(item);
            this.builderMenuItems.put(item.getActionCommand(), item);

            menu = new JMenu("Edit");
            menu.setMnemonic('E');
            bar.add(menu);

            item = new JMenuItem("Undo", 'U');
            item.addActionListener(this.listener);
            item.setAccelerator(KeyStroke.getKeyStroke('Z', InputEvent.CTRL_DOWN_MASK));
            item.setEnabled(false);
            menu.add(item);
            this.builderMenuItems.put(item.getActionCommand(), item);

            item = new JMenuItem("Redo", 'R');
            item.addActionListener(this.listener);
            item.setAccelerator(KeyStroke.getKeyStroke('Y', InputEvent.CTRL_DOWN_MASK));
            item.setEnabled(false);
            menu.add(item);
            this.builderMenuItems.put(item.getActionCommand(), item);

            menu.add(new JSeparator());

            item = new JMenuItem("Cut", 'T');
            item.addActionListener(this.listener);
            item.setAccelerator(KeyStroke.getKeyStroke('X', InputEvent.CTRL_DOWN_MASK));
            item.setEnabled(false);
            menu.add(item);
            this.builderMenuItems.put(item.getActionCommand(), item);

            item = new JMenuItem("Copy", 'C');
            item.addActionListener(this.listener);
            item.setAccelerator(KeyStroke.getKeyStroke('C', InputEvent.CTRL_DOWN_MASK));
            item.setEnabled(false);
            menu.add(item);
            this.builderMenuItems.put(item.getActionCommand(), item);

            item = new JMenuItem("Paste", 'P');
            item.addActionListener(this.listener);
            item.setAccelerator(KeyStroke.getKeyStroke('V', InputEvent.CTRL_DOWN_MASK));
            item.setEnabled(false);
            menu.add(item);
            this.builderMenuItems.put(item.getActionCommand(), item);

            item = new JMenuItem("Delete", 'D');
            item.addActionListener(this.listener);
            item.setEnabled(false);
            menu.add(item);
            this.builderMenuItems.put(item.getActionCommand(), item);

            menu.add(new JSeparator());

            item = new JMenuItem("Select All", 'A');
            item.addActionListener(this.listener);
            item.setAccelerator(KeyStroke.getKeyStroke('A', InputEvent.CTRL_DOWN_MASK));
            item.setEnabled(false);
            menu.add(item);
            this.builderMenuItems.put(item.getActionCommand(), item);

            menu = new JMenu("Test");
            menu.setMnemonic('T');
            bar.add(menu);

            item = new JMenuItem("Randomize", 'R');
            item.addActionListener(this.listener);
            item.setAccelerator(KeyStroke.getKeyStroke('R', InputEvent.CTRL_DOWN_MASK));
            item.setEnabled(false);
            menu.add(item);
            this.builderMenuItems.put(item.getActionCommand(), item);

            item = new JMenuItem("Export Answers", 'E');
            item.addActionListener(this.listener);
            item.setEnabled(false);
            menu.add(item);
            this.builderMenuItems.put(item.getActionCommand(), item);

            item = new JMenuItem("Stress-Test", 'S');
            item.setActionCommand("Stress-Test");
            item.addActionListener(this.listener);
            item.setAccelerator(KeyStroke.getKeyStroke('T', InputEvent.CTRL_DOWN_MASK));
            item.setEnabled(true);
            menu.add(item);
            this.builderMenuItems.put(item.getActionCommand(), item);

            menu = new JMenu("View");
            menu.setMnemonic('V');
            bar.add(menu);

            item = new JMenuItem("Editor", 'E');
            item.addActionListener(this.listener);
            item.setAccelerator(KeyStroke.getKeyStroke('E', InputEvent.CTRL_DOWN_MASK));
            item.setEnabled(false);
            menu.add(item);
            this.builderMenuItems.put(item.getActionCommand(), item);

            item = new JMenuItem("Generated View", 'G');
            item.addActionListener(this.listener);
            item.setAccelerator(KeyStroke.getKeyStroke('G', InputEvent.CTRL_DOWN_MASK));
            item.setEnabled(false);
            menu.add(item);
            this.builderMenuItems.put(item.getActionCommand(), item);

            item = new JMenuItem("HTML", 'H');
            item.addActionListener(this.listener);
            item.setAccelerator(KeyStroke.getKeyStroke('H', InputEvent.CTRL_DOWN_MASK));
            item.setEnabled(false);
            menu.add(item);
            this.builderMenuItems.put(item.getActionCommand(), item);

            item = new JMenuItem("Diagnostics", 'D');
            item.addActionListener(this.listener);
            item.setAccelerator(KeyStroke.getKeyStroke('D', InputEvent.CTRL_DOWN_MASK));
            item.setEnabled(false);
            menu.add(item);
            this.builderMenuItems.put(item.getActionCommand(), item);

            menu.add(new JSeparator());

            item = new JCheckBoxMenuItem("Include Document Trees");
            item.addActionListener(this.listener);
            item.setEnabled(true);
            menu.add(item);
            this.builderMenuItems.put(item.getActionCommand(), item);

            item = new JCheckBoxMenuItem("Include choices / entry field");
            item.setActionCommand("ShowEntry");
            item.setSelected(true);
            item.addActionListener(this.listener);
            item.setEnabled(true);
            menu.add(item);
            this.builderMenuItems.put(item.getActionCommand(), item);

            item = new JCheckBoxMenuItem("Include answers");
            item.setActionCommand("ShowAnswers");
            item.setSelected(true);
            item.addActionListener(this.listener);
            item.setEnabled(true);
            menu.add(item);
            this.builderMenuItems.put(item.getActionCommand(), item);

            item = new JCheckBoxMenuItem("Include solutions");
            item.setActionCommand("ShowSolutions");
            item.setSelected(true);
            item.addActionListener(this.listener);
            item.setEnabled(true);
            menu.add(item);
            this.builderMenuItems.put(item.getActionCommand(), item);

            menu.add(new JSeparator());

            JMenu sub = new JMenu("Font Size");
            menu.add(sub);

            item = new JRadioButtonMenuItem("10");
            item.setEnabled(true);
            item.setActionCommand("Font Size 10");
            item.addActionListener(this.listener);
            sub.add(item);

            if (Math.abs(AbstractDocObjectTemplate.getDefaultFontSize() - 10.0f) < 0.01f) {
                item.setSelected(true);
            }

            ButtonGroup group = new ButtonGroup();
            this.builderMenuItems.put(item.getActionCommand(), item);
            group.add(item);

            item = new JRadioButtonMenuItem("12");
            item.setEnabled(true);
            item.setActionCommand("Font Size 12");
            item.addActionListener(this.listener);
            sub.add(item);

            if (Math.abs(AbstractDocObjectTemplate.getDefaultFontSize() - 12.0f) < 0.01f) {
                item.setSelected(true);
            }

            this.builderMenuItems.put(item.getActionCommand(), item);
            group.add(item);

            item = new JRadioButtonMenuItem("18");
            item.setEnabled(true);
            item.setActionCommand("Font Size 18");
            item.addActionListener(this.listener);
            sub.add(item);

            if (Math.abs(AbstractDocObjectTemplate.getDefaultFontSize() - 18.0f) < 0.01f) {
                item.setSelected(true);
            }

            this.builderMenuItems.put(item.getActionCommand(), item);
            group.add(item);

            item = new JRadioButtonMenuItem("24");
            item.setEnabled(true);
            item.setActionCommand("Font Size 24");
            item.addActionListener(this.listener);
            sub.add(item);

            if (Math.abs(AbstractDocObjectTemplate.getDefaultFontSize() - 24.0f) < 0.01f) {
                item.setSelected(true);
            }

            this.builderMenuItems.put(item.getActionCommand(), item);
            group.add(item);

            item = new JRadioButtonMenuItem("30");
            item.setEnabled(true);
            item.setActionCommand("Font Size 30");
            item.addActionListener(this.listener);
            sub.add(item);

            if (Math.abs(AbstractDocObjectTemplate.getDefaultFontSize() - 30.0f) < 0.01f) {
                item.setSelected(true);
            }

            this.builderMenuItems.put(item.getActionCommand(), item);
            group.add(item);

            item = new JRadioButtonMenuItem("36");
            item.setEnabled(true);
            item.setActionCommand("Font Size 36");
            item.addActionListener(this.listener);
            sub.add(item);

            if (Math.abs(AbstractDocObjectTemplate.getDefaultFontSize() - 36.0f) < 0.01f) {
                item.setSelected(true);
            }

            this.builderMenuItems.put(item.getActionCommand(), item);
            group.add(item);

            item = new JRadioButtonMenuItem("42");
            item.setEnabled(true);
            item.setActionCommand("Font Size 42");
            item.addActionListener(this.listener);
            sub.add(item);

            if (Math.abs(AbstractDocObjectTemplate.getDefaultFontSize() - 42.0f) < 0.01f) {
                item.setSelected(true);
            }

            this.builderMenuItems.put(item.getActionCommand(), item);
            group.add(item);

            sub = new JMenu("Background Color");
            menu.add(sub);
            group = new ButtonGroup();

            item = new JRadioButtonMenuItem("White (MATH 117)");
            item.setEnabled(true);
            item.setActionCommand("Color White");
            item.addActionListener(this.listener);
            sub.add(item);
            item.setSelected(true);
            this.builderBackground = "white";
            this.builderMenuItems.put(item.getActionCommand(), item);
            group.add(item);

            item = new JRadioButtonMenuItem("Green (MATH 118)");
            item.setEnabled(true);
            item.setActionCommand("Color Green");
            item.addActionListener(this.listener);
            sub.add(item);
            this.builderMenuItems.put(item.getActionCommand(), item);
            group.add(item);

            item = new JRadioButtonMenuItem("Pink (MATH 124)");
            item.setEnabled(true);
            item.setActionCommand("Color Pink");
            item.addActionListener(this.listener);
            sub.add(item);
            this.builderMenuItems.put(item.getActionCommand(), item);
            group.add(item);

            item = new JRadioButtonMenuItem("Yellow (MATH 125)");
            item.setEnabled(true);
            item.setActionCommand("Color Yellow");
            item.addActionListener(this.listener);
            sub.add(item);
            this.builderMenuItems.put(item.getActionCommand(), item);
            group.add(item);

            item = new JRadioButtonMenuItem("Blue (MATH 126)");
            item.setEnabled(true);
            item.setActionCommand("Color Blue");
            item.addActionListener(this.listener);
            sub.add(item);
            this.builderMenuItems.put(item.getActionCommand(), item);
            group.add(item);

            this.builderSource = new JTextPane();
            this.builderSource.setBackground(Color.LIGHT_GRAY);
            this.builderSourceDoc = this.builderSource.getStyledDocument();
            this.builderSourceDoc.addDocumentListener(this.listener);
            this.builderSourceDoc.addUndoableEditListener(this.listener);
            this.builderSource.setFont(new Font("Monospaced", Font.PLAIN, 11));
            this.builderSourceErrors = new JList<>();
            this.builderSourceErrors.setFont(new Font("Monospaced", Font.PLAIN, 10));
            this.builderSourceErrors.setForeground(Color.RED);
            final JScrollPane scroll = new JScrollPane(this.builderSource);
            scroll.getVerticalScrollBar().setUnitIncrement(36);
            scroll.setWheelScrollingEnabled(true);
            this.builderSplits[0] = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scroll,
                    new JScrollPane(this.builderSourceErrors));
            this.builderSplits[0].setOneTouchExpandable(true);
            this.builderTabs.addTab("Source File Editor", this.builderSplits[0]);

            this.builderView = new CurrentProblemPanel(examSession, null, true);
            this.builderViewErrors = new JList<>();
            this.builderViewErrors.setFont(new Font("Monospaced", Font.PLAIN, 10));
            this.builderViewErrors.setForeground(Color.RED);
            this.builderSplits[1] = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.builderView,
                    new JScrollPane(this.builderViewErrors));
            this.builderSplits[1].setOneTouchExpandable(true);
            this.builderTabs.addTab("Generated View", this.builderSplits[1]);

            this.builderHtml = new JEditorPane("text/html", CoreConstants.EMPTY);
            this.builderHtml.setBorder(BorderFactory.createLoweredBevelBorder());
            this.builderHtml.setFont(new Font("Monospaced", Font.PLAIN, 10));
            this.builderTabs.addTab("HTML", new JScrollPane(this.builderHtml));

            this.builderDiagnostic = new JEditorPane("text/html", CoreConstants.EMPTY);
            this.builderDiagnostic.setBorder(BorderFactory.createLoweredBevelBorder());
            this.builderDiagnostic.setFont(new Font("Monospaced", Font.PLAIN, 10));
            this.builderDiagnostic.setEditable(false);

            this.builderTabs.addTab("Diagnostic Data", new JScrollPane(this.builderDiagnostic));

            this.builderSplits[0].setResizeWeight(0.95);
            this.builderSplits[1].setResizeWeight(0.95);

            pack();
            setVisible(true);
        }
    }

    /**
     * A runnable class that clears the contents of the generated document window and the errors lists, as part of the
     * new parse operation.
     */
    private static final class ClearDocuments implements Runnable {

        /** An array of zero strings. */
        private static final String[] ZERO_LEN_STRING_ARR = new String[0];

        /** The owning object. */
        private final ProblemTester tester;

        /**
         * Construct a new {@code ClearDocuments} object.
         *
         * @param theTester The owning problem tester to be managed.
         */
        ClearDocuments(final ProblemTester theTester) {

            this.tester = theTester;
        }

        /**
         * Main thread method that will be executed in the AWT event thread.
         */
        @Override
        public void run() {

            if (!SwingUtilities.isEventDispatchThread()) {
                Log.warning(Res.get(Res.NOT_AWT_THREAD));
            }

            this.tester.view.removeAll();
            this.tester.viewErrors.setListData(ZERO_LEN_STRING_ARR);
            this.tester.sourceErrors.setListData(ZERO_LEN_STRING_ARR);
        }
    }

    /**
     * A runnable class that displays the outcome of a new parse of the text window, including setting colors on errors
     * and setting the contents of the error list.
     */
    private static class DisplayParseOutcome implements Runnable {

        /** The owning object. */
        private final ProblemTester tester;

        /** The content that was parsed. */
        private final XmlContent source;

        /** The editor pane that shows diagnostic data. */
        private final JEditorPane diag;

        /** True to show trees in diagnostic display. */
        private final boolean trees;

        /** True if problem parameters have values. */
        private final boolean hasValues;

        /**
         * Construct a new {@code DisplayParseOutcome} object.
         *
         * @param theTester    The owning problem tester to be managed.
         * @param theContent   The content that was parsed.
         * @param diagnostic   The editor pane that displays diagnostic data.
         * @param theTrees     True to show trees in diagnostic display; false otherwise.
         * @param theHasValues True if the parameters have values, meaning that a student view and diagnostic view
         *                     should be generated as well.
         */
        DisplayParseOutcome(final ProblemTester theTester, final XmlContent theContent,
                            final JEditorPane diagnostic, final boolean theTrees, final boolean theHasValues) {

            this.tester = theTester;
            this.source = theContent;
            this.diag = diagnostic;
            this.trees = theTrees;
            this.hasValues = theHasValues;
        }

        /**
         * Main thread method that will be executed in the AWT event thread.
         */
        @Override
        public final void run() {

            if (!SwingUtilities.isEventDispatchThread()) {
                Log.warning(Res.get(Res.NOT_AWT_THREAD));
            }

            final MutableAttributeSet errset = new SimpleAttributeSet();
            this.tester.sourceDoc.setCharacterAttributes(0, this.tester.sourceDoc.getLength(), errset, true);

            errset.addAttribute(StyleConstants.Foreground, new Color(192, 0, 0));
            errset.addAttribute(StyleConstants.Background, new Color(255, 255, 128));

            List<XmlContentError> errors = this.source.getErrors();
            if (errors == null) {
                errors = new ArrayList<>(0);
            }

            final String[] array = new String[errors.size()];

            final int len = array.length;
            for (int i = 0; i < len; ++i) {

                final XmlContentError msg = errors.get(i);
                Log.warning(msg);

                // TODO: Highlight the error in the source document
                // if (parse.start != -1 && parse.end != -1) {
                //
                // if (parse.end >= parse.start) {
                // this.tester.sourceDoc.setCharacterAttributes(parse.start,
                // parse.end + 1 - parse.start, errset, false);
                // }
                // } else if (parse.start != -1) {
                // this.tester.sourceDoc.setCharacterAttributes(parse.start, 1, errset, false);
                // }
            }

            this.tester.sourceErrors.setListData(array);

            if (this.hasValues) {

                final AbstractProblemTemplate prob = this.tester.getProblem();
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try (final PrintStream ps = new PrintStream(baos, false, StandardCharsets.UTF_8)) {
                    prob.printDiagnostics(ps, this.trees);
                }
                this.diag.setText(baos.toString());
            }
        }
    }

    /**
     * A runnable class that clears the contents of the generated document window and the errors lists, as part of the
     * new parse operation.
     */
    private static final class ResetDividers implements Runnable {

        /** The split panes used to divide content from errors lists. */
        private final JSplitPane[] dividers;

        /**
         * Constructs a new {@code ResetDividers}.
         *
         * @param theDividers the split pane dividers to set the positions of
         */
        ResetDividers(final JSplitPane[] theDividers) {

            this.dividers = theDividers;
        }

        /**
         * Method to run in the AWT event thread to set the divider positions.
         */
        @Override
        public void run() {

            if (!SwingUtilities.isEventDispatchThread()) {
                Log.warning(Res.get(Res.NOT_AWT_THREAD));
            }

            this.dividers[0].setDividerLocation(0.95);
            this.dividers[1].setDividerLocation(0.95);
        }
    }

    /**
     * A runnable class that parses the input text again in the AWT event thread.
     */
    private static final class Reparser implements Runnable {

        /** The owning problem tester. */
        private final ProblemTester owner;

        /**
         * Constructs a new {@code Reparser}.
         *
         * @param theOwner the owning problem tester
         */
        Reparser(final ProblemTester theOwner) {

            this.owner = theOwner;
        }

        /**
         * Method to run in the AWT event thread to set the divider positions.
         */
        @Override
        public void run() {

            if (!SwingUtilities.isEventDispatchThread()) {
                Log.warning(Res.get(Res.NOT_AWT_THREAD));
            }

            if (this.owner.isDirty()) {
                this.owner.reparse();
                this.owner.setClean();
            }
        }
    }
}
