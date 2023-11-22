package dev.mathops.app.ops.snapin.messaging.tosend;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.Serial;

/**
 * A panel that displays a single message ("to", "subject", and "body"), and allows the user to edit the subject and
 * body.
 */
final class SingleMessagePane extends JPanel implements DocumentListener {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = -6794074616426196955L;

    /** Color for panel background. */
    private static final Color PANE_BG = new Color(240, 240, 245);

    /** Color for panel background. */
    private static final Color BORDER_COLOR = new Color(210, 210, 220);

    /** The message this panel represents. */
    private final MessageToSend message;

    /** The subject field. */
    private final JTextField subjectField;

    /** The body area. */
    private final JTextArea body;

    /**
     * Constructs a new {@code SingleMessagePane}.
     *
     * @param theMessage the message to send
     */
    SingleMessagePane(final MessageToSend theMessage) {

        super(new StackedBorderLayout());

        this.message = theMessage;

        setBackground(PANE_BG);

        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        final JPanel toLine = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 4));
        toLine.setBackground(PANE_BG);

        final JLabel toLabel = new JLabel("To: ");
        toLine.add(toLabel);
        final String nameStr =
                SimpleBuilder.concat(CoreConstants.SPC, theMessage.context.student.getScreenName(),
                        " (", theMessage.context.student.stuId, ") ");
        final JTextField toField = new JTextField(nameStr, 25);
        toField.setBackground(Color.WHITE);

        toField.setEditable(false);
        toLine.add(toField);
        final JLabel idleLabel = new JLabel("    Idle days: " + theMessage.status.daysSinceLastActivity);
        toLine.add(idleLabel);
        final JLabel sinceMsgLabel = new JLabel("    Days since Msg: " + theMessage.status.daysSinceLastMessage);
        toLine.add(sinceMsgLabel);
        add(toLine, StackedBorderLayout.NORTH);

        final JPanel subjectLine = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 4));
        subjectLine.setBackground(PANE_BG);
        final JLabel subjectLabel = new JLabel("Subject: ");
        subjectLine.add(subjectLabel);
        this.subjectField = new JTextField(theMessage.subject, 25);
        this.subjectField.setBackground(Color.WHITE);
        this.subjectField.getDocument().addDocumentListener(this);
        subjectLine.add(this.subjectField);
        final JLabel urgencyLabel = new JLabel("    Urgency: " + theMessage.status.urgency);
        subjectLine.add(urgencyLabel);
        add(subjectLine, StackedBorderLayout.NORTH);

        this.body = new JTextArea(8, 8);
        this.body.setMinimumSize(new Dimension(100, 100));
        this.body.setBackground(Color.WHITE);
        this.body.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, BORDER_COLOR),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)));
        this.body.setWrapStyleWord(true);
        this.body.setLineWrap(true);
        this.body.getDocument().addDocumentListener(this);
        this.body.setText(theMessage.body);
        add(this.body, StackedBorderLayout.CENTER);
    }

    /**
     * Called when a field's document is changed.
     *
     * @param e the document event
     */
    @Override
    public void insertUpdate(final DocumentEvent e) {

        changedUpdate(e);
    }

    /**
     * Called when a field's document is changed.
     *
     * @param e the document event
     */
    @Override
    public void removeUpdate(final DocumentEvent e) {

        changedUpdate(e);
    }

    /**
     * Called when a field's document is changed.
     *
     * @param e the document event
     */
    @Override
    public void changedUpdate(final DocumentEvent e) {

        final Document src = e.getDocument();

        if (src == this.subjectField.getDocument()) {
            this.message.subject = this.subjectField.getText();
        } else if (src == this.body.getDocument()) {
            this.message.body = this.body.getText();
        }
    }
}
