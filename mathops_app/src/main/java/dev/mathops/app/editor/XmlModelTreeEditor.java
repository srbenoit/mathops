package dev.mathops.app.editor;

import com.formdev.flatlaf.FlatLightLaf;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.EDebugLevel;
import dev.mathops.commons.model.ModelTreeNode;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.model.ParsingLog;
import dev.mathops.text.model.ParsingLogEntry;
import dev.mathops.text.model.SimpleModelTreeNodeFactory;
import dev.mathops.text.model.XmlTreeParser;
import dev.mathops.text.model.XmlTreeWriter;
import dev.mathops.text.parser.LineOrientedParserInput;
import dev.mathops.text.parser.xml.XmlFileFilter;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * A simple editor that takes XML data, parses it as a model tree, then generates XML from that model tree to validate
 * parsing and XML generation.
 */
public final class XmlModelTreeEditor implements Runnable, ActionListener, CaretListener {

    /** An action command. */
    private static final String OPEN_CMD = "OPEN";

    /** An action command. */
    private static final String PARSE_CMD = "PARSE";

    /** The top-level frame. */
    private JFrame frame = null;

    /** The source text area. */
    private JTextArea source = null;

    /** The gutter for the source area. */
    private JTextArea sourceGutter = null;

    /** The target text area. */
    private JTextArea target = null;

    /** The console. */
    private JTextArea console = null;

    /** The status bar text */
    private JLabel status = null;

    /** The current source text. */
    private String sourceText = CoreConstants.EMPTY;

    /**
     * Private constructor to prevent instantiation
     */
    private XmlModelTreeEditor() {

        // No action
    }

    /**
     * Constructs the UI in the AWT event thread.
     */
    public void run() {

        final Font fixed = new Font(Font.MONOSPACED, Font.PLAIN, 11);

        this.frame = new JFrame("XML Model Tree Editor");
        this.frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final JPanel content = new JPanel(new StackedBorderLayout());
        this.frame.setContentPane(content);

        final JMenuBar menuBar = new JMenuBar();
        this.frame.add(menuBar, StackedBorderLayout.NORTH);

        final JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        final JMenuItem fileOpen = new JMenuItem("Open...");
        fileOpen.setActionCommand(OPEN_CMD);
        fileOpen.addActionListener(this);
        fileMenu.add(fileOpen);

        final JMenu actionMenu = new JMenu("Action");
        menuBar.add(actionMenu);

        final JMenuItem actionParse = new JMenuItem("Parse...");
        actionParse.setActionCommand(PARSE_CMD);
        actionParse.addActionListener(this);
        actionMenu.add(actionParse);

        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension halfSize = new Dimension(screen.width / 4, screen.height / 2);
        final Dimension consoleSize = new Dimension(screen.width / 2, screen.height / 6);

        this.source = new JTextArea();
        this.source.setFont(fixed);
        this.source.addCaretListener(this);

        this.sourceGutter = new JTextArea();
        this.sourceGutter.setEnabled(false);
        this.sourceGutter.setColumns(4);
        this.sourceGutter.setFont(fixed);

        final JPanel pane = new JPanel(new StackedBorderLayout());
        pane.add(this.sourceGutter, StackedBorderLayout.WEST);
        pane.add(this.source, StackedBorderLayout.CENTER);

        final JScrollPane sourceScroll = new JScrollPane(pane);
        sourceScroll.getVerticalScrollBar().setUnitIncrement(12);
        sourceScroll.setPreferredSize(halfSize);

        this.target = new JTextArea();
        this.target.setFont(fixed);
        final JScrollPane targetScroll = new JScrollPane(this.target);
        targetScroll.getVerticalScrollBar().setUnitIncrement(12);
        targetScroll.setPreferredSize(halfSize);

        final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setLeftComponent(sourceScroll);
        split.setRightComponent(targetScroll);
        content.add(split, StackedBorderLayout.CENTER);

        final JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 3));
        final Border topBorder = BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY);
        statusBar.setBorder(topBorder);
        this.status = new JLabel(CoreConstants.SPC);
        statusBar.add(this.status);
        content.add(statusBar, StackedBorderLayout.SOUTH);

        this.console = new JTextArea();
        final JScrollPane consoleScroll = new JScrollPane(this.console);
        consoleScroll.setPreferredSize(consoleSize);
        content.add(consoleScroll, StackedBorderLayout.SOUTH);

        UIUtilities.packAndCenter(this.frame);
        this.frame.setVisible(true);
    }

    /**
     * Called when an action in invoked.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (OPEN_CMD.equals(cmd)) {

            final JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("XML file to parse");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setFileFilter(new XmlFileFilter());

            if (chooser.showOpenDialog(this.frame) == JFileChooser.APPROVE_OPTION) {
                final File file = chooser.getSelectedFile();
                final String xml = FileLoader.loadFileAsString(file, true);
                if (xml == null) {
                    this.sourceText = CoreConstants.EMPTY;
                    this.sourceGutter.setText(CoreConstants.EMPTY);
                } else {
                    final LineOrientedParserInput lines = new LineOrientedParserInput(xml);
                    final int numLines = lines.getNumLines();
                    // Using 'toString' here normalizes newlines in the source file to '\n'
                    this.sourceText = lines.toString();

                    final StringBuilder builder = new StringBuilder(numLines * 5);
                    for (int i = 0; i < numLines; ++i) {
                        builder.append(i);
                        builder.append('\n');
                    }
                    final String gutterStr = builder.toString();
                    this.sourceGutter.setText(gutterStr);
                }
                this.source.setText(this.sourceText);
            }

        } else if (PARSE_CMD.equals(cmd)) {

            final String sourceXml = this.source.getText();

            final LineOrientedParserInput input = new LineOrientedParserInput(sourceXml);
            final ParsingLog log = new ParsingLog(20);

            final ModelTreeNode root = XmlTreeParser.parseXml(input, new SimpleModelTreeNodeFactory(), log);

            if (root == null) {
                this.target.setText(CoreConstants.EMPTY);
            } else {
                final int len = sourceXml.length() * 10 / 8;
                final HtmlBuilder builder = new HtmlBuilder(len);

                XmlTreeWriter.write(root, builder, 0, EDebugLevel.INFO, Boolean.FALSE);
                final String output = builder.toString();
                this.target.setText(output);
            }

            final HtmlBuilder errors = new HtmlBuilder(1000);
            final int count = log.getNumEntries();
            for (int i = 0; i < count; ++i) {
                final ParsingLogEntry entry = log.getEntry(i);
                final String entryStr = entry.toString();
                errors.addln(entryStr);
            }

            final String consoleText = errors.toString();
            this.console.setText(consoleText);
        }
    }

    /**
     * Called when the caret in the source XML is moved.
     *
     * @param e the caret event
     */
    @Override
    public void caretUpdate(final CaretEvent e) {

        final int pos = this.source.getCaretPosition();
        if (this.sourceText.length() >= pos) {
            int priorLinefeed = pos - 1;
            while (priorLinefeed > 0) {
                final char ch = this.sourceText.charAt(priorLinefeed);
                if (ch == '\n' || ch == '\r') {
                    break;
                }
                --priorLinefeed;
            }

            final int col = pos - priorLinefeed - 1;
            final String statusText = "Col: " + col;
            this.status.setText(statusText);
        } else {
            this.status.setText(CoreConstants.SPC);
        }
    }

    /**
     * Main method to launch the application.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        FlatLightLaf.setup();
        SwingUtilities.invokeLater(new XmlModelTreeEditor());
    }
}

