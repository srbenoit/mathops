package dev.mathops.app.ops.snapin.messaging.tosend;

import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawStmsgLogic;
import dev.mathops.db.old.rawrecord.RawStmsg;
import dev.mathops.app.ops.snapin.messaging.CanvasMessageSend;
import dev.mathops.app.ops.snapin.messaging.MessagingFull;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.tree.MutableTreeNode;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * A panel that displays details of one or more messages, with the option to [ Send ]. If a single message is selected,
 * it is displayed and can be edited before sending.
 */
public final class MessageDetailPanel extends JPanel implements ActionListener {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = -6642886909716513296L;

    /** An action command. */
    private static final String SEND_CMD = "SEND";

    /** The data cache. */
    private final Cache cache;

    /** The owning messaging panel. */
    private final MessagingFull owner;

    /** The Canvas message senders. */
    private final CanvasMessageSenders senders;

    /** The central panel to which to add messages. */
    private final JPanel messagePane;

    /** The "Send" button. */
    private final JButton sendBtn;

    /**
     * A map from message to the tree node (in the "Selected Population" tree) that contains the message, so the node in
     * that tree can be deleted once sent.
     */
    private Map<MessageToSend, MessageListPanel.TreeReference> toSend;

    /**
     * Constructs a new {@code MessageDetailPanel}.
     *
     * @param theCache   the data cache
     * @param theOwner   the owning messaging panel
     * @param theSenders the senders
     */
    public MessageDetailPanel(final Cache theCache, final MessagingFull theOwner,
                              final CanvasMessageSenders theSenders) {

        super(new StackedBorderLayout());

        this.cache = theCache;
        this.owner = theOwner;
        this.senders = theSenders;

        setPreferredSize(new Dimension(350, 100));

        final TitledBorder middleTitle =
                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                        " Message Details ", TitledBorder.DEFAULT_JUSTIFICATION,
                        TitledBorder.DEFAULT_POSITION, MessagingFull.TITLE_FONT);

        setBorder(BorderFactory.createCompoundBorder(middleTitle,
                BorderFactory.createEmptyBorder(5, 5, 0, 5)));

        this.messagePane = new JPanel(new StackedBorderLayout());
        final JScrollPane messageScroll = new JScrollPane(this.messagePane);
        messageScroll.getVerticalScrollBar().setUnitIncrement(20);
        add(messageScroll, StackedBorderLayout.CENTER);

        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 8));
        this.sendBtn = new JButton("Send");
        this.sendBtn.setActionCommand(SEND_CMD);
        this.sendBtn.addActionListener(this);
        buttons.add(this.sendBtn);

        add(buttons, StackedBorderLayout.SOUTH);
    }

    /**
     * Sets the set of messages to send.
     *
     * @param theToSend a map from message to the tree node (in the "Selected Population" tree) that contains the
     *                  message, so the node in that tree can be deleted once sent.
     */
    void setMessagesToSend(final Map<MessageToSend, MessageListPanel.TreeReference> theToSend) {

        this.toSend = theToSend;

        final int numMessages = theToSend.size();

        this.messagePane.removeAll();

        for (final Map.Entry<MessageToSend, MessageListPanel.TreeReference> entry : theToSend.entrySet()) {
            final SingleMessagePane msgPane = new SingleMessagePane(entry.getKey());
            this.messagePane.add(msgPane, StackedBorderLayout.NORTH);
        }

        this.sendBtn.setEnabled(numMessages > 0);

        invalidate();
        revalidate();
        repaint();
    }

    /**
     * Called when a button generates an action.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (SEND_CMD.equals(cmd)) {
            final int count = this.toSend.size();
            int msgNumber = 0;

            for (final Map.Entry<MessageToSend, MessageListPanel.TreeReference> entry : this.toSend.entrySet()) {

                ++msgNumber;
                final MessageListPanel.TreeReference ref = entry.getValue();

                final MessageToSend msg = entry.getKey();
                final String subject = msg.subject;
                final String body = msg.body;
                boolean record = true;

                if (subject == null || subject.trim().isEmpty()) {
                    Log.info("Skipping send message with empty subject");
                } else if (body == null || body.trim().isEmpty()) {
                    Log.info("Skipping send message with empty body");
                } else {
                    final String course = msg.status.reg.course;
                    final String sect = msg.status.reg.sect;

                    final CanvasMessageSend sender = this.senders.getSender(course, sect);
                    if (sender == null) {
                        Log.warning("No sender found for ", course, " section ", sect);
                        record = false;
                    } else {
                        final String label = msgNumber + " of " + count;
                        final Collection<String> recipients = new ArrayList<>(1);
                        recipients.add(msg.status.reg.stuId);
                        record = sender.sendMessage(label, recipients, msg.subject, msg.body);
                    }
                }

                if (record) {
                    final MutableTreeNode parent = (MutableTreeNode) ref.node.getParent();

                    ref.model.removeNodeFromParent(ref.node);

                    if (parent.getChildCount() == 0) {
                        ref.model.removeNodeFromParent(parent);
                    }

                    this.owner.purgeMessage(msg);

                    final RawStmsg stmsg = new RawStmsg(msg.context.student.stuId,
                            msg.context.today, Integer.valueOf(msg.context.pace), msg.courseIndex,
                            msg.milestone.code, msg.msgCode.name(), msg.status.instructorName);

                    try {
                        RawStmsgLogic.insert(this.cache, stmsg);
                    } catch (final SQLException ex) {
                        Log.warning("Failed to insert STMSG record.", ex);
                    }
                }
            }
        }
    }
}
