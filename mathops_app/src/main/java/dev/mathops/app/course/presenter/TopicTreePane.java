package dev.mathops.app.course.presenter;

import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A panel that presents the topic tree and allows the user to select a single topic.
 */
final class TopicTreePane extends JPanel implements TreeSelectionListener {

    /** The preferred width. */
    private static final int PREF_WIDTH = 220;

    /** The preferred height. */
    private static final int PREF_HEIGHT = 300;

    /** The owning window. */
    private final MainWindow owner;

    /** The tree. */
    private final JTree tree;

    /**
     * Constructs a new {@code TopicTreePane}.
     *
     * @param theOwner           the owning window
     * @param theCourseDirectory the course directory
     */
    TopicTreePane(final MainWindow theOwner, final File theCourseDirectory) {

        super(new StackedBorderLayout());

        this.owner = theOwner;

        setPreferredSize(new Dimension(PREF_WIDTH, PREF_HEIGHT));

        final DefaultMutableTreeNode root = new DefaultMutableTreeNode();

        final List<String> subjectNames = new ArrayList<>(10);
        final File[] subjectDirs = theCourseDirectory.listFiles();
        if (subjectDirs != null) {
            for (final File subjectFile : subjectDirs) {
                final String name = subjectFile.getName();
                if (subjectFile.isDirectory() && MainWindow.isNumberedFilename(name)) {
                    subjectNames.add(name);
                }
            }
        }
        subjectNames.sort(null);

        final List<String> topicNames = new ArrayList<>(20);

        for (final String subjectName : subjectNames) {
            final DefaultMutableTreeNode subject = new DefaultMutableTreeNode(subjectName);
            root.add(subject);

            final File parent = new File(theCourseDirectory, subjectName);

            final File[] topicDirs = parent.listFiles();
            if (topicDirs != null) {
                for (final File topicFile : topicDirs) {
                    final String name = topicFile.getName();
                    if (topicFile.isDirectory() && MainWindow.isNumberedFilename(name)) {
                        topicNames.add(name);
                    }
                }
            }
            topicNames.sort(null);

            for (final String topicName : topicNames) {
                final MutableTreeNode topic = new DefaultMutableTreeNode(topicName);
                subject.add(topic);
            }

            topicNames.clear();
        }

        this.tree = new JTree(root);
        this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        this.tree.setRootVisible(false);
        this.tree.setShowsRootHandles(true);

        final JScrollPane scroll = new JScrollPane(this.tree);
        add(scroll, StackedBorderLayout.CENTER);
    }

    /**
     * Initializes the panel.  Called after the constructor since this method leaks 'this' and the object is not fully
     * constructed within the constructor.
     */
    void init() {

        this.tree.addTreeSelectionListener(this);
    }

    /**
     * Called when the tree selection changes.
     *
     * @param e the event that characterizes the change
     */
    @Override
    public void valueChanged(final TreeSelectionEvent e) {

        String subject = null;
        String topic = null;

        final TreePath path = e.getPath();

        // NOTE: component 0 is the root node (hidden) which has null user object - we want components 1 and 2.

        if (path != null) {
            final int count = path.getPathCount();
            if (count > 1) {
                final Object path1 = path.getPathComponent(1);

                if (path1 instanceof final DefaultMutableTreeNode subjectNode) {
                    if (subjectNode.getUserObject() instanceof final String subjectStr) {
                        subject = subjectStr;
                        if (count > 2) {
                            final Object path2 = path.getPathComponent(2);

                            if (path2 instanceof final DefaultMutableTreeNode topicNode) {
                                if (topicNode.getUserObject() instanceof final String topicStr) {
                                    topic = topicStr;
                                }
                            }
                        }
                    }
                }
            }
        }

        this.owner.topicSelected(subject, topic);
    }
}
