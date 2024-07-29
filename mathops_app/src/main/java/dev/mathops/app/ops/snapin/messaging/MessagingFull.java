package dev.mathops.app.ops.snapin.messaging;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawStmsgLogic;
import dev.mathops.app.ops.snapin.AbstractFullPanel;
import dev.mathops.app.ops.snapin.messaging.epf.EPFScanWorker;
import dev.mathops.app.ops.snapin.messaging.tosend.CanvasMessageSenders;
import dev.mathops.app.ops.snapin.messaging.tosend.MessageDetailPanel;
import dev.mathops.app.ops.snapin.messaging.tosend.MessageListPanel;
import dev.mathops.app.ops.snapin.messaging.tosend.MessagePopulation;
import dev.mathops.app.ops.snapin.messaging.tosend.MessagePopulationBuilder;
import dev.mathops.app.ops.snapin.messaging.tosend.MessageScanWorker;
import dev.mathops.app.ops.snapin.messaging.tosend.MessageToSend;
import dev.mathops.app.ops.snapin.messaging.tosend.Population;
import dev.mathops.app.ops.snapin.messaging.tosend.PopulationSection;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;

/**
 * A full-screen panel for this snap-in.
 */
public final class MessagingFull extends AbstractFullPanel implements ActionListener, TreeSelectionListener {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = -2214030453502567032L;

    /** Font for group titles. */
    public static final Font TITLE_FONT = new Font(Font.DIALOG, Font.BOLD, 14);

    /** Font for field labels. */
    private static final Font LABEL_FONT = new Font(Font.DIALOG, Font.BOLD, 12);

    /** Font for ordinary labels. */
    private static final Font PLAIN_FONT = new Font(Font.DIALOG, Font.PLAIN, 15);

    /** Button highlight color. */
    private static final Color BTN_HIGHLIGHT = new Color(120, 120, 150);

    /** Button action command. */
    private static final String CMD_CANVAS_CFG = "CCFG";

    /** Button action command. */
    private static final String CMD_OUTLOOK_CFG = "OCFG";

    /** Button action command. */
    private static final String CMD_EMAIL_CFG = "ECFG";

    /** Button action command. */
    private static final String CMD_IMPORT_CANVAS_ID = "ICID";

    /** Button action command. */
    private static final String CMD_IMPORT_CANVAS_CONV = "ICCO";

    /** Button action command. */
    private static final String CMD_IMPORT_OUTLOOK_CONV = "IOCO";

    /** Button action command. */
    private static final String CMD_IMPORT_EMAIL_CONV = "IECO";

    /** Button action command. */
    private static final String CMD_GEN_MESSAGES = "GENMSG";

    /** Button action command. */
    private static final String CMD_GEN_EPF = "GENEPF";

    /** The data cache. */
    private final Cache cache;

    /** The snap-in. */
    private final MessagingSnapIn snapIn;

    /** The owning frame. */
    private final JFrame frame;

    /** The message senders. */
    private final CanvasMessageSenders senders;

    /** The total messages sent. */
    private int totalMessages;

    /** Field to display total messages sent to date. */
    private final JTextField totalMessagesField;

    /** Field to display date last message was sent. */
    private final JTextField lastMessageDateField;

    /** Progress bar. */
    private final JProgressBar progress;

    /** A button in the UI. */
    private final JButton scanCanvasIds;

    /** A button in the UI. */
    private final JButton importCanvasConversations;

    /** A button in the UI. */
    private final JButton importOutlookConversations;

    /** A button in the UI. */
    private final JButton importPersonalEmail;

    /** A button in the UI. */
    private final JButton generateMessages;

    /** A button in the UI. */
    private final JButton generateEPF;

    /** A button in the UI. */
    private final JButton canvasConfig;

    /** A button in the UI. */
    private final JButton outlookConfig;

    /** A button in the UI. */
    private final JButton emailConfig;

    /** The label on the status bar. */
    private final JLabel statusBarLabel;

    /** The tree that shows message populations. */
    private final JTree tree;

    /** The data model for the tree that shows message populations. */
    private final DefaultTreeModel treeModel;

    /** The root node in the tree model. */
    private final DefaultMutableTreeNode treeRoot;

    /** The center region. */
    private final JPanel center;

    /**
     * Constructs a new {@code MessagingFull}.
     *
     * @param theCache  the data cache
     * @param theSnapIn the snap-in
     * @param theFrame  the owning frame
     */
    MessagingFull(final Cache theCache, final MessagingSnapIn theSnapIn, final JFrame theFrame) {

        super();

        this.cache = theCache;
        this.snapIn = theSnapIn;
        this.frame = theFrame;

        this.senders = new CanvasMessageSenders(theCache);

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        final JPanel west = new JPanel(new StackedBorderLayout(16, 16));
        add(west, StackedBorderLayout.WEST);

        final JPanel west1 = new JPanel(new StackedBorderLayout());
        final TitledBorder west1Title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " Status ",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, TITLE_FONT);
        west1.setBorder(BorderFactory.createCompoundBorder(west1Title, BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        west.add(west1, StackedBorderLayout.NORTH);

        this.totalMessagesField = new JTextField(10);
        this.totalMessagesField.setEditable(false);
        this.totalMessagesField.setFont(PLAIN_FONT);
        final JLabel totalMessagesLabel = new JLabel("Total messages sent to date:");
        totalMessagesLabel.setFont(LABEL_FONT);
        west1.add(totalMessagesLabel, StackedBorderLayout.NORTH);
        west1.add(this.totalMessagesField, StackedBorderLayout.NORTH);

        west1.add(new Spacer(6), StackedBorderLayout.NORTH);

        this.lastMessageDateField = new JTextField(10);
        this.lastMessageDateField.setEditable(false);
        this.lastMessageDateField.setFont(PLAIN_FONT);
        final JLabel lastMessageDateLabel = new JLabel("Date last message was sent:");
        lastMessageDateLabel.setFont(LABEL_FONT);
        west1.add(lastMessageDateLabel, StackedBorderLayout.NORTH);
        west1.add(this.lastMessageDateField, StackedBorderLayout.NORTH);

        //

        this.generateMessages = new JButton("Generate Messages");
        this.generateMessages.setActionCommand(CMD_GEN_MESSAGES);
        this.generateMessages.addActionListener(this);
        this.generateMessages.setFont(PLAIN_FONT);

        final JPanel genMessagesPanel = new JPanel(new BorderLayout());
        genMessagesPanel.setBorder(BorderFactory.createLineBorder(BTN_HIGHLIGHT));
        genMessagesPanel.add(this.generateMessages, BorderLayout.CENTER);
        west.add(genMessagesPanel, StackedBorderLayout.NORTH);

        this.generateEPF = new JButton("Generate EPF list");
        this.generateEPF.setActionCommand(CMD_GEN_EPF);
        this.generateEPF.addActionListener(this);
        this.generateEPF.setFont(PLAIN_FONT);

        final JPanel generateEpfPanel = new JPanel(new BorderLayout());
        generateEpfPanel.setBorder(BorderFactory.createLineBorder(BTN_HIGHLIGHT));
        generateEpfPanel.add(this.generateEPF, BorderLayout.CENTER);
        west.add(generateEpfPanel, StackedBorderLayout.NORTH);

        //

        final JPanel west2 = new JPanel(new StackedBorderLayout());
        final TitledBorder west2Title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " Canvas ",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, TITLE_FONT);
        west2.setBorder(BorderFactory.createCompoundBorder(west2Title, BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        west.add(west2, StackedBorderLayout.NORTH);

        this.scanCanvasIds = new JButton("Import Canvas User IDs");
        this.scanCanvasIds.setActionCommand(CMD_IMPORT_CANVAS_ID);
        this.scanCanvasIds.addActionListener(this);
        this.scanCanvasIds.setFont(PLAIN_FONT);
        west2.add(this.scanCanvasIds, StackedBorderLayout.NORTH);

        west2.add(new Spacer(6), StackedBorderLayout.NORTH);

        this.importCanvasConversations = new JButton("Import Canvas Conversations");
        this.importCanvasConversations.setActionCommand(CMD_IMPORT_CANVAS_CONV);
        this.importCanvasConversations.addActionListener(this);
        this.importCanvasConversations.setFont(PLAIN_FONT);
        west2.add(this.importCanvasConversations, StackedBorderLayout.NORTH);

        //

        final JPanel west3 = new JPanel(new StackedBorderLayout());
        final TitledBorder west3Title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                " Email Systems", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, TITLE_FONT);
        west3.setBorder(BorderFactory.createCompoundBorder(west3Title, BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        west.add(west3, StackedBorderLayout.NORTH);

        this.importOutlookConversations = new JButton("Import Outlook Conversations");
        this.importOutlookConversations.setActionCommand(CMD_IMPORT_OUTLOOK_CONV);
        this.importOutlookConversations.addActionListener(this);
        this.importOutlookConversations.setFont(PLAIN_FONT);
        west3.add(this.importOutlookConversations, StackedBorderLayout.NORTH);

        west3.add(new Spacer(6), StackedBorderLayout.NORTH);

        this.importPersonalEmail = new JButton("Import Personal Email");
        this.importPersonalEmail.setActionCommand(CMD_IMPORT_EMAIL_CONV);
        this.importPersonalEmail.addActionListener(this);
        this.importPersonalEmail.setFont(PLAIN_FONT);
        west3.add(this.importPersonalEmail, StackedBorderLayout.NORTH);

        //

        final JPanel west4 = new JPanel(new StackedBorderLayout());
        final TitledBorder west4Title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                " Integrations ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, TITLE_FONT);
        west4Title.setTitleColor(Color.LIGHT_GRAY);
        west4.setBorder(BorderFactory.createCompoundBorder(west4Title, BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        west.add(west4, StackedBorderLayout.NORTH);

        this.canvasConfig = new JButton("Canvas Configuration");
        this.canvasConfig.setActionCommand(CMD_CANVAS_CFG);
        this.canvasConfig.addActionListener(this);
        this.canvasConfig.setFont(PLAIN_FONT);
        west4.add(this.canvasConfig, StackedBorderLayout.NORTH);

        west4.add(new Spacer(6), StackedBorderLayout.NORTH);

        this.outlookConfig = new JButton("MS Outlook Configuration");
        this.outlookConfig.setActionCommand(CMD_OUTLOOK_CFG);
        this.outlookConfig.addActionListener(this);
        this.outlookConfig.setFont(PLAIN_FONT);
        west4.add(this.outlookConfig, StackedBorderLayout.NORTH);

        west4.add(new Spacer(6), StackedBorderLayout.NORTH);

        this.emailConfig = new JButton("Personal Email Configuration");
        this.emailConfig.setActionCommand(CMD_EMAIL_CFG);
        this.emailConfig.addActionListener(this);
        this.emailConfig.setFont(PLAIN_FONT);
        west4.add(this.emailConfig, StackedBorderLayout.NORTH);

        add(new Spacer(6), StackedBorderLayout.WEST);

        final JPanel middle = new JPanel(new StackedBorderLayout(16, 16));
        middle.setPreferredSize(new Dimension(350, 100));
        final TitledBorder middleTitle = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                " Message Populations ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, TITLE_FONT);
        middle.setBorder(BorderFactory.createCompoundBorder(middleTitle, BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        add(middle, StackedBorderLayout.WEST);

        this.treeRoot = new DefaultMutableTreeNode(CoreConstants.EMPTY);
        this.treeModel = new DefaultTreeModel(this.treeRoot);

        this.tree = new JTree(this.treeModel);
        this.tree.setCellRenderer(new MyCellRenderer());
        this.tree.setRootVisible(true);
        this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        this.tree.addTreeSelectionListener(this);

        final JScrollPane treeScroll = new JScrollPane(this.tree);
        middle.add(treeScroll, StackedBorderLayout.CENTER);

        this.center = new JPanel(new StackedBorderLayout());
        add(this.center, StackedBorderLayout.CENTER);

        //

        this.progress = new JProgressBar(0, 1000);
        this.progress.setBorder(BorderFactory.createLoweredBevelBorder());
        this.progress.setStringPainted(true);
        this.progress.setFont(TITLE_FONT);
        this.progress.setString(CoreConstants.EMPTY);
        add(this.progress, StackedBorderLayout.SOUTH);

        this.statusBarLabel = new JLabel(CoreConstants.SPC);
        this.statusBarLabel.setFont(LABEL_FONT);
        this.statusBarLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        add(this.statusBarLabel, StackedBorderLayout.SOUTH);

        update();
    }

    /**
     * Performs an update of the display.
     */
    private void update() {

        try {
            final Integer numMessages = RawStmsgLogic.count(this.cache);
            final LocalDate lastDate = RawStmsgLogic.getLatest(this.cache);

            this.totalMessages = numMessages.intValue();
            this.totalMessagesField.setText(numMessages.toString());
            this.lastMessageDateField.setText(lastDate == null ? "(never)" : TemporalUtils.FMT_MDY.format(lastDate));
        } catch (final SQLException ex) {
            Log.warning("Error querying database.", ex);
            this.totalMessagesField.setText("(error)");
            this.lastMessageDateField.setText("(error)");
        }
    }

//    /**
//     * Gets the Canvas message senders.
//     *
//     * @return the senders
//     */
//    public CanvasMessageSenders getSenders() {
//
//        return this.senders;
//    }

    /**
     * Called when a button click generates an action event.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (CMD_CANVAS_CFG.equals(cmd)) {
            doCanvasConfig();
        } else if (CMD_IMPORT_CANVAS_ID.equals(cmd)) {
            doImportCanvasIds();
        } else if (CMD_IMPORT_CANVAS_CONV.equals(cmd)) {
            doImportCanvasConversations();
        } else if (CMD_OUTLOOK_CFG.equals(cmd)) {
            doOutlookConfig();
        } else if (CMD_IMPORT_OUTLOOK_CONV.equals(cmd)) {
            doImportOutlookConversations();
        } else if (CMD_EMAIL_CFG.equals(cmd)) {
            doEmailConfig();
        } else if (CMD_IMPORT_EMAIL_CONV.equals(cmd)) {
            doImportEmailConversations();
        } else if (CMD_GEN_MESSAGES.equals(cmd)) {
            doGenerateMessages();
        } else if (CMD_GEN_EPF.equals(cmd)) {
            doGenerateEpf();
        }
    }

    /**
     * Configure connection to Canvas.
     */
    private void doCanvasConfig() {

        // TODO:
    }

    /**
     * Import canvas IDs and map to CSU IDs.
     */
    private void doImportCanvasIds() {

        this.statusBarLabel.setText("Importing Canvas User IDs...");
        this.scanCanvasIds.setEnabled(false);

        // TODO: Get these from settings

        final String canvasHost = "https://colostate.instructure.com";

        // Steve Benoit:
        // final String accessToken = "3716~6HH7du2ATvBTrFrekY4Ha5CpYdd4ICzANKBRcTsAKSdR9N7gVcJ2wG7H6Us0ysGW";

        // Anita Pattison:
        final String accessToken = "3716~gJUDduijP2xqicfn1oKYZom5s5Tji1P4G4pxLy8xmLuRGh5R4tHw645GFcCNHgmB";

        final CanvasUserIDScanner scanner = new CanvasUserIDScanner(this.snapIn.getCache(), canvasHost, accessToken,
                this.progress, this.scanCanvasIds, this.statusBarLabel);
        scanner.execute();
    }

    /**
     * Import canvas conversations.
     */
    private void doImportCanvasConversations() {

        this.statusBarLabel.setText("Importing Canvas Conversations...");
        this.importCanvasConversations.setEnabled(false);

        // TODO: Get these from settings

        final String canvasHost = "https://colostate.instructure.com";

        // Steve Benoit:
        // final String accessToken = "3716~6HH7du2ATvBTrFrekY4Ha5CpYdd4ICzANKBRcTsAKSdR9N7gVcJ2wG7H6Us0ysGW";

        // Anita Pattison:
        final String accessToken = "3716~gJUDduijP2xqicfn1oKYZom5s5Tji1P4G4pxLy8xmLuRGh5R4tHw645GFcCNHgmB";

        final CanvasConversationImporter importer =
                new CanvasConversationImporter(this.snapIn.getCache(), canvasHost, accessToken, this.progress,
                        this.importCanvasConversations, this.statusBarLabel);
        importer.execute();
    }

    /**
     * Configure connection to Outlook.
     */
    private void doOutlookConfig() {

        // TODO:
    }

    /**
     * Import conversations from MS Outlook.
     */
    private void doImportOutlookConversations() {

        // TODO:
    }

    /**
     * Configure connection to personal Email.
     */
    private void doEmailConfig() {

        // TODO:
    }

    /**
     * Import conversations from personal email account.
     */
    private void doImportEmailConversations() {

        // TODO:
    }

    /**
     * Generates messages.
     */
    private void doGenerateMessages() {

        this.statusBarLabel.setText("Generating Messages...");
        this.generateMessages.setEnabled(false);

        final MessageScanWorker scanWorker = new MessageScanWorker(this.snapIn.getCache(), this, this.progress,
                this.generateMessages, this.statusBarLabel);
        scanWorker.execute();
    }

    /**
     * Called when the tree selection changes.
     *
     * @param e the tree selection event
     */
    @Override
    public void valueChanged(final TreeSelectionEvent e) {

        MessagePopulation selection = null;

        if (this.tree.getSelectionCount() == 1) {
            final DefaultMutableTreeNode selectedNode =
                    (DefaultMutableTreeNode) this.tree.getLastSelectedPathComponent();
            if (selectedNode != null) {
                final Object userObj = selectedNode.getUserObject();
                if (userObj instanceof MessagePopulation) {
                    selection = (MessagePopulation) userObj;
                }
            }
        }

        this.center.removeAll();

        if (selection != null) {
            final MessageDetailPanel details = new MessageDetailPanel(this.cache, this, this.senders);
            this.center.add(details, StackedBorderLayout.CENTER);

            final MessageListPanel selPanel = new MessageListPanel(selection, details);
            this.center.add(selPanel, StackedBorderLayout.WEST);
        }

        invalidate();
        revalidate();
        repaint();
    }

    /**
     * Called when the scan for messages is complete.
     *
     * @param scanner the scanner
     */
    public void messageScanFinished(final MessagePopulationBuilder scanner) {

        Log.info("Scan finished");

        this.treeRoot.removeAllChildren();

        int total = 0;
        int numMessages = 0;
        int numRows = 0;

        // if (!scanner.nonCountedIncomplete.isEmpty()) {
        // final int count = scanner.nonCountedIncomplete.countStudents();
        // total += count;
        // final int msg = scanner.nonCountedIncomplete.countMessages();
        // numMessages += msg;
        //
        // final DefaultMutableTreeNode node =
        // new DefaultMutableTreeNode("Incompletes (" + count + " stus, " + msg + " msgs)");
        // numRows += addMessagePopulations(node, scanner.nonCountedIncomplete);
        // this.treeModel.nodeChanged(node);
        // }

        if (scanner.five.hasSections()) {
            final int count = scanner.five.countStudents();
            total += count;
            final int msg = scanner.five.countMessages();
            numMessages += msg;

            final DefaultMutableTreeNode node =
                    new DefaultMutableTreeNode("5-Course (" + count + " stus, " + msg + " msgs)");
            numRows += addMessagePopulations(node, scanner.five);
            this.treeModel.nodeChanged(node);
        }

        if (scanner.fiveWithForfeit.hasSections()) {
            final int count = scanner.fiveWithForfeit.countStudents();
            total += count;
            final int msg = scanner.fiveWithForfeit.countMessages();
            numMessages += msg;

            final DefaultMutableTreeNode node = new DefaultMutableTreeNode(
                    "5-Course with forfeit (" + count + " stus, " + msg + " msgs)");
            numRows += addMessagePopulations(node, scanner.fiveWithForfeit);
            this.treeModel.nodeChanged(node);
        }

        if (scanner.four.hasSections()) {
            final int count = scanner.four.countStudents();
            total += count;
            final int msg = scanner.four.countMessages();
            numMessages += msg;

            final DefaultMutableTreeNode node =
                    new DefaultMutableTreeNode("4-Course (" + count + " stus, " + msg + " msgs)");
            numRows += addMessagePopulations(node, scanner.four);
            this.treeModel.nodeChanged(node);
        }

        if (scanner.fourWithForfeit.hasSections()) {
            final int count = scanner.fourWithForfeit.countStudents();
            total += count;
            final int msg = scanner.fourWithForfeit.countMessages();
            numMessages += msg;

            final DefaultMutableTreeNode node = new DefaultMutableTreeNode(
                    "4-Course with forfeit (" + count + " stus, " + msg + " msgs)");
            numRows += addMessagePopulations(node, scanner.fourWithForfeit);
            this.treeModel.nodeChanged(node);
        }

        if (scanner.three.hasSections()) {
            final int count = scanner.three.countStudents();
            total += count;
            final int msg = scanner.three.countMessages();
            numMessages += msg;

            final DefaultMutableTreeNode node =
                    new DefaultMutableTreeNode("3-Course (" + count + " stus, " + msg + " msgs)");
            numRows += addMessagePopulations(node, scanner.three);
            this.treeModel.nodeChanged(node);
        }

        if (scanner.threeWithForfeit.hasSections()) {
            final int count = scanner.threeWithForfeit.countStudents();
            total += count;
            final int msg = scanner.threeWithForfeit.countMessages();
            numMessages += msg;

            final DefaultMutableTreeNode node = new DefaultMutableTreeNode(
                    "3-Course with forfeit (" + count + " stus, " + msg + " msgs)");
            numRows += addMessagePopulations(node, scanner.threeWithForfeit);
            this.treeModel.nodeChanged(node);
        }

        if (scanner.twoA.hasSections()) {
            final int count = scanner.twoA.countStudents();
            total += count;
            final int msg = scanner.twoA.countMessages();
            numMessages += msg;

            final DefaultMutableTreeNode node =
                    new DefaultMutableTreeNode("2-Course A (" + count + " stus, " + msg + " msgs)");
            numRows += addMessagePopulations(node, scanner.twoA);
            this.treeModel.nodeChanged(node);
        }

        if (scanner.twoAWithForfeit.hasSections()) {
            final int count = scanner.twoAWithForfeit.countStudents();
            total += count;
            final int msg = scanner.twoAWithForfeit.countMessages();
            numMessages += msg;

            final DefaultMutableTreeNode node = new DefaultMutableTreeNode(
                    "2-Course A with forfeit (" + count + " stus, " + msg + " msgs)");
            numRows += addMessagePopulations(node, scanner.twoAWithForfeit);
            this.treeModel.nodeChanged(node);
        }

        if (scanner.twoB.hasSections()) {
            final int count = scanner.twoB.countStudents();
            total += count;
            final int msg = scanner.twoB.countMessages();
            numMessages += msg;

            final DefaultMutableTreeNode node =
                    new DefaultMutableTreeNode("2-Course B (" + count + " stus, " + msg + " msgs)");
            numRows += addMessagePopulations(node, scanner.twoB);
            this.treeModel.nodeChanged(node);
        }

        if (scanner.twoBWithForfeit.hasSections()) {
            final int count = scanner.twoBWithForfeit.countStudents();
            total += count;
            final int msg = scanner.twoBWithForfeit.countMessages();
            numMessages += msg;

            final DefaultMutableTreeNode node = new DefaultMutableTreeNode(
                    "2-Course B with forfeit (" + count + " stus, " + msg + " msgs)");
            numRows += addMessagePopulations(node, scanner.twoBWithForfeit);
            this.treeModel.nodeChanged(node);
        }

        if (scanner.twoC.hasSections()) {
            final int count = scanner.twoC.countStudents();
            total += count;
            final int msg = scanner.twoC.countMessages();
            numMessages += msg;

            final DefaultMutableTreeNode node =
                    new DefaultMutableTreeNode("2-Course C (" + count + " stus, " + msg + " msgs)");
            numRows += addMessagePopulations(node, scanner.twoC);
            this.treeModel.nodeChanged(node);
        }

        if (scanner.twoCWithForfeit.hasSections()) {
            final int count = scanner.twoCWithForfeit.countStudents();
            total += count;
            final int msg = scanner.twoCWithForfeit.countMessages();
            numMessages += msg;

            final DefaultMutableTreeNode node = new DefaultMutableTreeNode(
                    "2-Course C with forfeit (" + count + " stus, " + msg + " msgs)");
            numRows += addMessagePopulations(node, scanner.twoCWithForfeit);
            this.treeModel.nodeChanged(node);
        }

        if (scanner.oneA.hasSections()) {
            final int count = scanner.oneA.countStudents();
            total += count;
            final int msg = scanner.oneA.countMessages();
            numMessages += msg;

            final DefaultMutableTreeNode node =
                    new DefaultMutableTreeNode("1-Course A (" + count + " stus, " + msg + " msgs)");
            numRows += addMessagePopulations(node, scanner.oneA);
            this.treeModel.nodeChanged(node);
        }

        if (scanner.oneB.hasSections()) {
            final int count = scanner.oneB.countStudents();
            total += count;
            final int msg = scanner.oneB.countMessages();
            numMessages += msg;

            final DefaultMutableTreeNode node =
                    new DefaultMutableTreeNode("1-Course B (" + count + " stus, " + msg + " msgs)");
            numRows += addMessagePopulations(node, scanner.oneB);
            this.treeModel.nodeChanged(node);
        }

        if (scanner.oneC.hasSections()) {
            final int count = scanner.oneC.countStudents();
            total += count;
            final int msg = scanner.oneC.countMessages();
            numMessages += msg;

            final DefaultMutableTreeNode node =
                    new DefaultMutableTreeNode("1-Course C (" + count + " stus, " + msg + " msgs)");
            numRows += addMessagePopulations(node, scanner.oneC);
            this.treeModel.nodeChanged(node);
        }

        for (int i = 0; i < numRows; ++i) {
            this.tree.expandRow(i);
        }

        this.treeRoot.setUserObject("All Categories (" + total + " stus, " + numMessages + " msgs)");
        this.treeModel.nodeChanged(this.treeRoot);

        // Log.info("Tree Root: " + this.treeRoot.getUserObject());

        this.tree.invalidate();
        this.tree.revalidate();
        this.tree.repaint();
    }

    /**
     * Adds the message populations.
     *
     * @param categoryNode the category node to which to add populations
     * @param population   the map whose entries to add
     * @return the number of rows added to the tree
     */
    private int addMessagePopulations(final DefaultMutableTreeNode categoryNode, final Population population) {

        int count = 1;

        for (final Map.Entry<String, PopulationSection> entry : population.sections.entrySet()) {

            final String sect = entry.getKey();
            final PopulationSection popSect = entry.getValue();

            final MessagePopulation pop = new MessagePopulation(sect, popSect);

            final MutableTreeNode sectNode = new DefaultMutableTreeNode(pop);
            categoryNode.add(sectNode);
            ++count;
        }

        this.treeRoot.add(categoryNode);

        return count;
    }

    /**
     * Generates EPF list.
     */
    private void doGenerateEpf() {

        this.statusBarLabel.setText("Generating Early Performance Feedback Report...");
        this.generateEPF.setEnabled(false);

        final EPFScanWorker scanWorker = new EPFScanWorker(this.snapIn.getCache(), this.frame, this.progress,
                this.generateEPF, this.statusBarLabel);
        scanWorker.execute();
    }

    /**
     * Purges a message to send from the "message populations" tree and updates that tree's labels. Called after that
     * message has been successfully sent. This method also increments the "total messages sent to date" field.
     *
     * @param msg the message to be purged
     */
    public void purgeMessage(final MessageToSend msg) {

        ++this.totalMessages;
        this.totalMessagesField.setText(Integer.toString(this.totalMessages));

        final int numRootChildren = this.treeRoot.getChildCount();
        for (int i = 0; i < numRootChildren; ++i) {

            // Children of "treeRoot" are things like "5-course (N stus, M msgs)"
            final DefaultMutableTreeNode child = (DefaultMutableTreeNode) this.treeRoot.getChildAt(i);

            final int numGrandchindren = child.getChildCount();
            boolean childChanged = false;

            for (int j = 0; j < numGrandchindren; ++j) {
                // Grand-children are populations, like "Sect 001 (S students, T messages)
                final DefaultMutableTreeNode grandchild = (DefaultMutableTreeNode) child.getChildAt(j);

                final Object userObj = grandchild.getUserObject();

                if (userObj instanceof final MessagePopulation population) {
                    if (population.remove(msg)) {
                        childChanged = true;
                        this.treeModel.nodeChanged(grandchild);
                        break;
                    }
                } else {
                    Log.warning("Grandchild node in tree's user object was not MessagePopulation");
                }
            }

            if (childChanged) {
                // Re-calculate the tree node label for the child
                int numStus = 0;
                int numMsgs = 0;

                for (int j = 0; j < numGrandchindren; ++j) {
                    // Grand-children are populations, like "Sect 001 (S students, T messages)
                    final DefaultMutableTreeNode grandchild =
                            (DefaultMutableTreeNode) child.getChildAt(j);

                    final Object userObj = grandchild.getUserObject();

                    if (userObj instanceof final MessagePopulation population) {

                        numStus += population.population.countStudents();
                        numMsgs += population.population.countMessages();

                        final String oldTitle = child.getUserObject().toString();
                        final int openParen = oldTitle.indexOf('(');
                        if (openParen != -1) {
                            final String newTitle = oldTitle.substring(0, openParen + 1) + numStus + " stus, "
                                    + numMsgs + " msgs)";
                            child.setUserObject(newTitle);
                            this.treeModel.nodeChanged(child);
                        }
                    } else {
                        Log.warning("Grandchild node in tree's user object was not MessagePopulation");
                    }
                }
                break;
            }
        }
    }

    /**
     * Called on a timer thread to periodically refresh displays.
     */
    @Override
    public void tick() {

        // No action
    }

    /**
     * A cell renderer for the population tree.
     */
    public static final class MyCellRenderer extends DefaultTreeCellRenderer {

        /** Version for serialization. */
        @Serial
        private static final long serialVersionUID = -1033793602850546427L;

        /** Background color for fields. */
        static final Color SEL_BG = new Color(200, 200, 250);

        /**
         * Constructs a new {@code MyCellRenderer}.
         */
        public MyCellRenderer() {

            super();
        }

        /**
         * Get the background for a non-selected node.
         *
         * @return the background
         */
        @Override
        public Color getBackgroundNonSelectionColor() {

            return Color.WHITE;
        }

        /**
         * Get the background for a selected node.
         *
         * @return the background
         */
        @Override
        public Color getBackgroundSelectionColor() {

            return SEL_BG;
        }

        /**
         * Get the background for a selected node.
         *
         * @return the background
         */
        @Override
        public Color getTextNonSelectionColor() {

            return Color.BLACK;
        }

        /**
         * Get the background for a selected node.
         *
         * @return the background
         */
        @Override
        public Color getTextSelectionColor() {

            return Color.BLACK;
        }
    }

    /**
     * A spacer panel.
     */
    private static final class Spacer extends JPanel {

        /** Version for serialization. */
        @Serial
        private static final long serialVersionUID = -5364292701677070773L;

        /**
         * Constructs a new {@code Spacer} of a specified size (width and height).
         *
         * @param size the size
         */
        private Spacer(final int size) {

            super();

            setPreferredSize(new Dimension(size, size));
        }
    }
}
