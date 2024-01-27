package dev.mathops.app.ops.snapin.messaging.tosend;

import dev.mathops.commons.CoreConstants;
import dev.mathops.app.ops.snapin.messaging.EMsg;
import dev.mathops.app.ops.snapin.messaging.MessagingFull;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.Serial;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A panel that accepts a {@code MessagePopulation} and presents the list of students, organized by message code.
 */
public final class MessageListPanel extends JPanel implements TreeSelectionListener {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = -5382626581434236507L;

    /** The list of messages due. */
    private final Map<String, MessageToSend> messagesDue;

    /** The tree that shows messages to send. */
    private final JTree tree;

    /** The data model for the tree that shows messages to send. */
    private final DefaultTreeModel treeModel;

    /** The root node in the tree model. */
    private final DefaultMutableTreeNode treeRoot;

    /** The details panel. */
    private final MessageDetailPanel details;

    /**
     * Constructs a new {@code MessageListPanel}.
     *
     * @param thePopulation the message population
     * @param theDetails    the details panel
     */
    public MessageListPanel(final MessagePopulation thePopulation, final MessageDetailPanel theDetails) {

        super(new BorderLayout());

        this.messagesDue = thePopulation.population.messagesDue;
        this.details = theDetails;

        setPreferredSize(new Dimension(350, 100));

        final TitledBorder middleTitle = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                " Selected Population ", TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION, MessagingFull.TITLE_FONT);

        setBorder(BorderFactory.createCompoundBorder(middleTitle,
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        this.treeRoot = new DefaultMutableTreeNode(CoreConstants.EMPTY);
        this.treeModel = new DefaultTreeModel(this.treeRoot);

        this.tree = new JTree(this.treeModel);
        this.tree.setCellRenderer(new MessagingFull.MyCellRenderer());
        this.tree.setBackground(Color.WHITE);
        this.tree.setRootVisible(true);
        this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        this.tree.addTreeSelectionListener(this);

        buildTreeContents();

        final JScrollPane treeScroll = new JScrollPane(this.tree);
        add(treeScroll, BorderLayout.CENTER);
    }

    /**
     * Builds the contents of the tree.
     */
    private void buildTreeContents() {

        final Set<EMsg> messageCodes = EnumSet.noneOf(EMsg.class);

        for (final MessageToSend msg : this.messagesDue.values()) {
            messageCodes.add(msg.msgCode);
        }

        int numRows = 0;
        for (final EMsg code : messageCodes) {

            final DefaultMutableTreeNode codeNode = new DefaultMutableTreeNode(code);
            ++numRows;

            for (final MessageToSend msg : this.messagesDue.values()) {
                if (msg.msgCode == code) {
                    final MutableTreeNode stuNode = new DefaultMutableTreeNode(msg);
                    codeNode.add(stuNode);
                    ++numRows;
                    this.treeModel.nodeChanged(stuNode);
                }
            }

            this.treeRoot.add(codeNode);
            this.treeModel.nodeChanged(codeNode);
        }

        for (int i = 0; i < numRows; ++i) {
            this.tree.expandRow(i);
        }
    }

    /**
     * Called when the tree selection changes.
     *
     * @param e the tree selection event
     */
    @Override
    public void valueChanged(final TreeSelectionEvent e) {

        final Map<MessageToSend, TreeReference> toSend = new HashMap<>(10);

        if (this.tree.getSelectionCount() > 0) {
            for (final TreePath path : Objects.requireNonNull(this.tree.getSelectionPaths())) {
                final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

                final Object obj = node.getUserObject();
                if (obj instanceof final MessageToSend msg) {
                    final TreeReference ref = new TreeReference(this.treeModel, node);
                    toSend.put(msg, ref);
                }

            }
        }

        this.details.setMessagesToSend(toSend);
    }

    /**
     * A reference to a node in a tree.
     */
    public static final class TreeReference {

        /** The tree model. */
        public final DefaultTreeModel model;

        /** The tree node. */
        public final DefaultMutableTreeNode node;

        /**
         * Constructs a new {@code TreeReference}.
         *
         * @param theTreeModel the tree model
         * @param theTreeNode  the tree node
         */
        TreeReference(final DefaultTreeModel theTreeModel, final DefaultMutableTreeNode theTreeNode) {

            this.model = theTreeModel;
            this.node = theTreeNode;
        }
    }
}
