package dev.mathops.app.course.presenter;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A panel that presents the tree of subdirectories under a topic.
 */
final class DirectoryTreePane extends JPanel implements TreeSelectionListener {

    /** The preferred width. */
    private static final int PREF_WIDTH = 220;

    /** The preferred height. */
    private static final int PREF_HEIGHT = 300;

    /** The owning window. */
    private final MainWindow owner;

    /** The course directory. */
    private final File courseDirectory;

    /** The header to show the currently selected subject and topic. */
    private final JLabel header;

    /** The currently displayed 's scroll pane. */
    private JScrollPane currentScroll = null;

    /** The current subject. */
    private String currentSubject = null;

    /** The current topic. */
    private String currentTopic = null;

    /**
     * Constructs a new {@code DirectoryTreePane}.
     *
     * @param theOwner           the owning window
     * @param theCourseDirectory the course directory
     */
    DirectoryTreePane(final MainWindow theOwner, final File theCourseDirectory) {

        super(new StackedBorderLayout());

        this.owner = theOwner;
        this.courseDirectory = theCourseDirectory;

        setPreferredSize(new Dimension(PREF_WIDTH, PREF_HEIGHT));

        this.header = new JLabel(CoreConstants.SPC);
        final JPanel headerFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 3));
        headerFlow.add(this.header);

        add(headerFlow, StackedBorderLayout.NORTH);
    }

    /**
     * Initializes the panel.  Called after the constructor since this method leaks 'this' and the object is not fully
     * constructed within the constructor.
     */
    void init() {

        // No action
    }

    /**
     * Processes a selection of a subject and topic, refreshing the tree view for that topic's contents.
     *
     * @param subject the subject (a numbered directory name)
     * @param topic   the topic (a numbered directory name)
     */
    void processSelection(final String subject, final String topic) {

        final String headerString;
        if (subject == null) {
            headerString = CoreConstants.SPC;
        } else if (topic == null) {
            headerString = subject;
        } else {
            headerString = subject + " : " + topic;
        }
        this.header.setText(headerString);

        if (this.currentScroll != null) {
            remove(this.currentScroll);
        }

        final DefaultMutableTreeNode root = new DefaultMutableTreeNode();

        if (subject != null && topic != null) {
            final File subjectDir = new File(this.courseDirectory, subject);
            final File topicDir = new File(subjectDir, topic);

            buildNumberedDirTree(topicDir, root);

            final JTree tree = new JTree(root);
            tree.setRootVisible(false);
            tree.setShowsRootHandles(true);
            tree.addTreeSelectionListener(this);

            final JScrollPane scroll = new JScrollPane(tree);

            add(scroll, StackedBorderLayout.CENTER);
            this.currentScroll = scroll;
            this.currentSubject = subject;
            this.currentTopic = topic;
        } else {
            this.currentScroll = null;
        }

        invalidate();
        revalidate();
        repaint();
    }

    /**
     * Given a directory and a parent tree node, scans the directory for directories with numbered filenames and adds
     * nodes the parent tree node for each, recursively calling this method to build the subtree for each.
     *
     * @param dir        the directory to scan for directories with numbered filenames
     * @param parentNode the parent node to which to add constructed tree nodes
     */
    private static void buildNumberedDirTree(final File dir, final DefaultMutableTreeNode parentNode) {

        final List<String> fileNames = new ArrayList<>(10);

        final File[] files = dir.listFiles();
        if (files != null) {
            for (final File file : files) {
                final String name = file.getName();
                if (file.isDirectory() && MainWindow.isNumberedFilename(name)) {
                    fileNames.add(name);
                }
            }
        }
        fileNames.sort(null);

        for (final String fileName : fileNames) {
            final DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(fileName);
            parentNode.add(childNode);

            final File subdirectory = new File(dir, fileName);
            buildNumberedDirTree(subdirectory, childNode);
        }
    }

    /**
     * Called when the tree selection changes.
     *
     * @param e the event that characterizes the change
     */
    @Override
    public void valueChanged(final TreeSelectionEvent e) {

        if (this.currentSubject != null && this.currentTopic != null) {

            final Collection<String> selection = new ArrayList<>(8);
            selection.add(this.currentSubject);
            selection.add(this.currentTopic);

            final TreePath path = e.getPath();
            if (path != null) {
                // NOTE: component 0 is the root node (hidden) which has null user object - we want all others
                final int count = path.getPathCount();
                for (int i = 1; i < count; ++i) {
                    final Object component = path.getPathComponent(i);

                    if (component instanceof final DefaultMutableTreeNode componentNode) {
                        if (componentNode.getUserObject() instanceof final String pathStr) {
                            selection.add(pathStr);
                        }
                    }
                }
            }

            this.owner.dirSelected(selection);
        }
    }
}
