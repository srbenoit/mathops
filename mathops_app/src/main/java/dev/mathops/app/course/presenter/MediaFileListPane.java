package dev.mathops.app.course.presenter;

import dev.mathops.commons.file.ExtensionFileFilter;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A panel that lists all files in a directory.
 */
final class MediaFileListPane extends JPanel implements TreeSelectionListener, ActionListener {

    /** The preferred width. */
    private static final int PREF_WIDTH = 220;

    /** The preferred height. */
    private static final int PREF_HEIGHT = 300;

    /** An action command. */
    private static final String ADD_PRESENTATION_CMD = "ADD_PRESENTATION";

    /** The file extension for presentation files. */
    private static final String PRES_EXTENSION = ".pres";

    /** The owning media pane. */
    private final MediaPane owner;

    /** The course directory. */
    private final File dir;

    /** The tree. */
    private final JTree tree;

    /** A button to add a presentation. */
    private final JButton addPresentationBtn;

    /**
     * Constructs a new {@code MediaFileListPane}.
     *
     * @param theOwner the owning window
     * @param theDir   the directory
     */
    MediaFileListPane(final MediaPane theOwner, final File theDir) {

        super(new StackedBorderLayout());

        this.owner = theOwner;
        this.dir = theDir;

        setPreferredSize(new Dimension(PREF_WIDTH, PREF_HEIGHT));

        final DefaultMutableTreeNode root = new DefaultMutableTreeNode();

        buildFileTree(theDir, root);

        this.tree = new JTree(root);
        this.tree.setRootVisible(false);
        this.tree.setShowsRootHandles(true);

        final JScrollPane scroll = new JScrollPane(this.tree);

        add(scroll, StackedBorderLayout.CENTER);

        final JPanel buttons = new JPanel(new StackedBorderLayout());
        final Border etched = BorderFactory.createEtchedBorder();
        buttons.setBorder(etched);

        final JPanel flow1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 3));
        final String label = Res.get(Res.ADD_PRESENTATION_BTN_LBL);
        this.addPresentationBtn = new JButton(label);
        this.addPresentationBtn.setActionCommand(ADD_PRESENTATION_CMD);
        flow1.add(this.addPresentationBtn);
        buttons.add(flow1);
        add(buttons, StackedBorderLayout.SOUTH);
    }

    /**
     * Initializes the panel.  Called after the constructor since this method leaks 'this' and the object is not fully
     * constructed within the constructor.
     */
    void init() {

        this.tree.addTreeSelectionListener(this);
        this.addPresentationBtn.addActionListener(this);
    }

    /**
     * Given a directory and a parent tree node, scans the directory for directories with numbered filenames and adds
     * nodes the parent tree node for each, recursively calling this method to build the subtree for each.
     *
     * @param dir        the directory to scan for directories with numbered filenames
     * @param parentNode the parent node to which to add constructed tree nodes
     */
    private static void buildFileTree(final File dir, final DefaultMutableTreeNode parentNode) {

        final List<String> fileNames = new ArrayList<>(10);

        final File[] files = dir.listFiles();
        if (files != null) {
            for (final File file : files) {
                final String name = file.getName();
                fileNames.add(name);
            }
        }
        fileNames.sort(null);

        for (final String fileName : fileNames) {
            final DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(fileName);
            parentNode.add(childNode);

            final File subdirectory = new File(dir, fileName);
            if (subdirectory.isDirectory()) {
                buildFileTree(subdirectory, childNode);
            }
        }
    }

    /**
     * Called when the tree selection changes.
     *
     * @param e the event that characterizes the change
     */
    @Override
    public void valueChanged(final TreeSelectionEvent e) {

        File selection = this.dir;

        final TreePath path = e.getPath();
        if (path != null) {
            // NOTE: component 0 is the root node (hidden) which has null user object - we want all others
            final int count = path.getPathCount();
            for (int i = 1; i < count; ++i) {
                final Object component = path.getPathComponent(i);

                if (component instanceof final DefaultMutableTreeNode componentNode) {
                    if (componentNode.getUserObject() instanceof final String pathStr) {
                        selection = new File(selection, pathStr);
                    }
                }
            }
        }

        this.owner.fileSelected(selection);
    }

    /**
     * Called when a button is activated.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (ADD_PRESENTATION_CMD.equals(cmd)) {
            addPresentation();
        }
    }

    /**
     * Adds a presentation, which prompts the user for a filename, creates an empty presentation file, and opens an
     * editor.
     */
    private void addPresentation() {

        final JFileChooser chooser = new JFileChooser(this.dir);
        final String chooserTitle = Res.get(Res.NEW_PRES_DIALOG_TITLE);
        chooser.setDialogTitle(chooserTitle);
        final String fileTypeName = Res.get(Res.PRES_FILE_TYPE);
        final FileFilter presFilter = new ExtensionFileFilter(PRES_EXTENSION, fileTypeName);
        chooser.addChoosableFileFilter(presFilter);

        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File newPresFile = chooser.getSelectedFile();
            final String name = newPresFile.getName();
            if (!name.toLowerCase().endsWith(PRES_EXTENSION)) {
                final String newName = name + PRES_EXTENSION;
                newPresFile = new File(newPresFile.getParentFile(), newName);
            }

            Log.info("Creating ", newPresFile.getAbsolutePath());
        }
    }
}
