package dev.mathops.app.assessment.problemauthor;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.htmlgen.ProblemConverter;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.assessment.problem.template.ProblemTemplateFactory;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.UIUtilities;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The main window.
 */
final class MainWindow extends JFrame implements MouseListener, ActionListener {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = 6481165592946626144L;

    /** The prefix for action commands to close files. */
    private static final String CMD_CLOSE_PREFIX = "CLOSE:";

    /** The action command to refresh the files list. */
    private static final String CMD_REFRESH = "REFRESH";

    /** The action command to generate a collection of problems. */
    private static final String CMD_GENERATE = "GENERATE";

    /** The library directory. */
    private final File libraryDir;

    /** The library view tree. */
    private final JTree libraryTree;

    /** The library tree model. */
    private final DefaultTreeModel libraryTreeModel;

    /** Tabs for open files. */
    private final JTabbedPane tabs;

    /** Map from file to the open panel for that file. */
    private final Map<File, FilePane> openFiles;

    /**
     * Constructs a new {@code MainWindow}.
     *
     * @param theLibraryDir the library directory
     */
    MainWindow(final File theLibraryDir) {

        super("Problem Author");

        this.libraryDir = theLibraryDir;

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final JPanel content = new JPanel(new BorderLayout());
        setContentPane(content);

        final JLabel libPath = new JLabel("Problem Library: " + theLibraryDir.getAbsolutePath());
        final JPanel titleFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 3));
        titleFlow.add(libPath);
        content.add(titleFlow, BorderLayout.PAGE_START);

        final DefaultMutableTreeNode root = makeTreeNode(theLibraryDir);

        this.libraryTreeModel = new DefaultTreeModel(root);

        this.libraryTree = new JTree(this.libraryTreeModel);
        this.libraryTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        this.libraryTree.setCellRenderer(new MyTreeCellRenderer());
        this.libraryTree.setBorder(BorderFactory.createEtchedBorder());
        this.libraryTree.setRootVisible(false);
        this.libraryTree.expandRow(0);
        this.libraryTree.addMouseListener(this);

        final JPanel west = new JPanel(new BorderLayout());
        final JScrollPane treeScroll = new JScrollPane(this.libraryTree);
        treeScroll.setMinimumSize(new Dimension(300, 300));
        west.add(treeScroll, BorderLayout.CENTER);

        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));

        final JButton refresh = new JButton("Refresh");
        refresh.setActionCommand(CMD_REFRESH);
        refresh.addActionListener(this);
        buttons.add(refresh);

        final JButton generate = new JButton("Generate");
        generate.setActionCommand(CMD_GENERATE);
        generate.addActionListener(this);
        buttons.add(generate);

        west.add(buttons, BorderLayout.PAGE_END);

        this.tabs = new JTabbedPane();

        final JSplitPane treeSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, west, this.tabs);
        treeSplit.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final int w = Math.min(1400, (screenSize.width << 2) / 5);
        final int h = (screenSize.height << 2) / 5;
        treeSplit.setPreferredSize(new Dimension(w, h));

        content.add(treeSplit, BorderLayout.CENTER);

        UIUtilities.packAndCenter(this);

        this.openFiles = new HashMap<>(10);
    }

    /**
     * Creates a mutable tree node for a directory or file, recursively creating child nodes for any subdirectories.
     *
     * @param dir the directory
     * @return the node
     */
    private static DefaultMutableTreeNode makeTreeNode(final File dir) {

        final DefaultMutableTreeNode node = new DefaultMutableTreeNode(dir);

        if (dir.isDirectory()) {
            final File[] files = dir.listFiles();
            if (files != null) {
                for (final File file : files) {
                    final DefaultMutableTreeNode subnode = makeTreeNode(file);
                    node.add(subnode);
                }
            }
        }

        return node;
    }

    /**
     * Called when the mouse is clicked on the tree.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseClicked(final MouseEvent e) {

        if (e.getClickCount() == 2) {
            final Object last = this.libraryTree.getLastSelectedPathComponent();

            if (last instanceof final DefaultMutableTreeNode node) {
                final Object userObj = node.getUserObject();

                if (userObj instanceof final File file && !file.isDirectory()
                    && file.getName().toLowerCase(Locale.ROOT).endsWith(".xml")) {

                    final FilePane existing = this.openFiles.get(file);

                    if (existing == null) {
                        openFile(file);
                    } else {
                        this.tabs.setSelectedComponent(existing);
                    }
                }
            }
        }
    }

    /**
     * Called when the mouse is pressed on the tree.
     *
     * @param e the mouse event
     */
    @Override
    public void mousePressed(final MouseEvent e) {

        // No action
    }

    /**
     * Called when the mouse is released on the tree.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseReleased(final MouseEvent e) {

        // No action
    }

    /**
     * Called when the mouse enters the tree.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseEntered(final MouseEvent e) {

        // No action
    }

    /**
     * Called when the mouse exists the tree.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseExited(final MouseEvent e) {

        // No action
    }

    /**
     * Opens a file.
     *
     * @param file the file to open
     */
    private void openFile(final File file) {

        final Dimension size = this.tabs.getSize();
        size.height -= 30;
        size.width -= 10;

        final FilePane pane = new FilePane(file, size);
        final String title = file.getName();
        this.tabs.addTab(title, pane);

        final int index = this.tabs.indexOfTab(title);
        final JPanel pnlTab = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 0));
        pnlTab.setOpaque(false);
        pnlTab.add(new JLabel(title));

        JButton btnClose;
        final byte[] icon = FileLoader.loadFileAsBytes(getClass(), "document-close-3.png", true);
        if (icon == null) {
            btnClose = new JButton("x");
        } else {
            try {
                final BufferedImage img = ImageIO.read(new ByteArrayInputStream(icon));
                btnClose = new JButton(new ImageIcon(img));
                btnClose.setOpaque(false);
                btnClose.setContentAreaFilled(false);
                btnClose.setBorderPainted(false);
            } catch (final IOException ex) {
                Log.warning(ex);
                btnClose = new JButton("x");
            }
        }

        btnClose.setActionCommand(CMD_CLOSE_PREFIX + title);
        btnClose.addActionListener(this);
        pnlTab.add(btnClose);

        this.tabs.setTabComponentAt(index, pnlTab);

        this.openFiles.put(file, pane);
        this.tabs.setSelectedComponent(pane);
    }

    /**
     * Called when a button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (cmd != null && cmd.startsWith(CMD_CLOSE_PREFIX)) {
            final String toClose = cmd.substring(CMD_CLOSE_PREFIX.length());

            final int index = this.tabs.indexOfTab(toClose);

            if (index != -1) {
                // TODO: Test whether there are unsaved changes...

                final Component component = this.tabs.getComponentAt(index);
                if (component instanceof final FilePane filePane) {
                    final File file = filePane.getFile();
                    this.openFiles.remove(file);
                } else {
                    Log.warning("Selected pane component was ", component.getClass().getSimpleName());
                }

                this.tabs.remove(index);
            }
        } else if (CMD_REFRESH.equals(cmd)) {
            final DefaultMutableTreeNode root = makeTreeNode(this.libraryDir);
            this.libraryTreeModel.setRoot(root);
        } else if (CMD_GENERATE.equals(cmd)) {
            doGenerate();
        }
    }

    /**
     * Handles a click on the "Generate" button.
     */
    private void doGenerate() {

        final TreeSelectionModel model = this.libraryTree.getSelectionModel();
        final int numSelected = model.getSelectionCount();

        if (numSelected > 0) {
            final List<File> files = new ArrayList<>(numSelected);
            final TreePath[] paths = model.getSelectionPaths();

            for (final TreePath path : paths) {
                final Object last = path.getLastPathComponent();

                if (last instanceof final DefaultMutableTreeNode node) {
                    final Object userObj = node.getUserObject();

                    if (userObj instanceof final File file && !file.isDirectory()) {
                        final String name = file.getName().toLowerCase(Locale.ROOT);

                        if (name.endsWith(".xml") || name.endsWith(".item")) {
                            files.add(file);
                        }
                    }
                }
            }

            if (!files.isEmpty()) {
                Log.info("Generating " + files.size() + " problems...");
                final HtmlBuilder htm = new HtmlBuilder(1000);

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
                htm.addln("<title>Problem Author</title>");
                htm.addln("</head>");

                htm.addln("<body style='background:white;padding:10px;'>");

                htm.sDiv("page-wrapper");

                for (final File problemFile : files) {
                    generateProblem(problemFile, htm);
                }

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
        }
    }

    /**
     * Generates a problem and adds its HTML representation to an {@code HtmlBuilder}.
     *
     * @param problemFile the file to load
     * @param output      the {@code HtmlBuilder} to which to append
     */
    private void generateProblem(final File problemFile, final HtmlBuilder output) {

        final String absolutePath = problemFile.getAbsolutePath();
        final String fileContent = FileLoader.loadFileAsString(problemFile, true);

        if (fileContent == null) {
            output.addln("<p color='red'>ERROR: Unable to load file '", absolutePath, "'.</p>");
        } else {
            final String nameLC = problemFile.getName().toLowerCase(Locale.ROOT);
            final boolean isXml = nameLC.endsWith(".xml") || nameLC.endsWith(".item");

            if (isXml) {
                if (fileContent.contains("</exam>")) {
                    output.addln("<p color='red'>ERROR: '", absolutePath, "' appears to be an exam file.</p>");
                } else if (fileContent.contains("</problem>")
                           || fileContent.contains("</problem-multiple-choice>")
                           || fileContent.contains("</problem-multiple-selection")
                           || fileContent.contains("</problem-numeric>")
                           || fileContent.contains("</problem-embedded-input>")
                           || fileContent.contains("</problem-auto-correct>")
                           || fileContent.contains("</problem-dummy>")) {
                    try {
                        final XmlContent source = new XmlContent(fileContent, false, false);

                        final AbstractProblemTemplate problem = ProblemTemplateFactory.load(source, EParserMode.NORMAL);

                        problem.realize(problem.evalContext);

                        final List<XmlContentError> allErrors = gatherErrors(source);
                        final IElement top = source.getTopLevel();
                        accumulateErrors(top, allErrors);

                        if (!allErrors.isEmpty()) {
                            output.addln("<div color='red'>Errors in parsed pre-realize XML:</div>");
                            output.addln("<ul>");
                            for (final XmlContentError error : allErrors) {
                                output.addln("<li>" + error + "</li>");
                            }
                            output.addln("</ul>");
                        }

                        // Add the problem, answer, and solution

                        final int[] id = {1};
                        ProblemConverter.populateProblemHtml(problem, id);

                        output.addln("<hr/>");
                        output.sH(2).add(problem.id).eH(2);
                        output.addln("<hr/>");
                        output.addln(problem.questionHtml);
                        if (problem.answerHtml != null) {
                            output.addln("<hr/>");
                            output.addln(problem.answerHtml);
                        }
                        if (problem.solutionHtml != null) {
                            output.addln("<hr/>");
                            output.addln(problem.solutionHtml);
                        }
                        output.addln("<hr/>");

                    } catch (final ParsingException ex) {
                        final String msg = ex.getLocalizedMessage();
                        output.addln("<p color='red'>ERROR: Unable to parse '", absolutePath, "': ", msg, ".</p>");
                    }
                } else {
                    output.addln("<p color='red'>ERROR: '", absolutePath, "' does not appear to be an item file.</p>");
                }
            } else {
                output.addln("<p color='red'>ERROR: '", absolutePath, "' does not appear to be an item file.</p>");
            }
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
     * A tree cell renderer that prints the file's name.
     */
    private static final class MyTreeCellRenderer extends DefaultTreeCellRenderer {

        /** Version for serialization. */
        @Serial
        private static final long serialVersionUID = 3079033964388582606L;

        /**
         * Constructs a new {@code MyTreeCellRenderer}.
         */
        private MyTreeCellRenderer() {

            super();
        }

        /**
         * Creates a component that can render a cell.
         *
         * @param tree     the tree
         * @param value    the tree node user object
         * @param sel      true if selected
         * @param expanded true if expanded
         * @param leaf     true if a leaf node
         * @param row      the row number
         * @param hasFocus true if this window has focus
         */
        @Override
        public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean sel,
                                                      final boolean expanded, final boolean leaf, final int row,
                                                      final boolean hasFocus) {

            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            if (value instanceof final DefaultMutableTreeNode node && node.getUserObject() instanceof final File file) {
                setText(file.getName());
            }

            return this;
        }
    }

}
