package dev.mathops.app.ops.snapin.messaging.epf;

import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.app.ops.snapin.messaging.tosend.MessageToSend;
import dev.mathops.text.builder.HtmlBuilder;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.Serial;
import java.util.Map;

/**
 * A dialog to show the results of an EPF report.
 */
final class EPFResultsDialog extends JDialog {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = -3466488106018190882L;

    /** Dialog background color. */
    private static final Color BACKGROUND = new Color(90, 100, 90);

    /** Dialog background color. */
    private static final Color BORDER = new Color(130, 150, 130);

    /** Dialog background color. */
    private static final Color AREA_BG = new Color(50, 60, 50);

    /**
     * Constructs a new {@code EPFResultsDialog}.
     *
     * @param frame the frame that owns the dialog
     * @param epf   the EPF report
     */
    EPFResultsDialog(final JFrame frame, final Map<String, MessageToSend> epf) {

        super(frame, "Early Performance Feedback Results");

        final Rectangle frameBounds = frame.getBounds();

        final JPanel content = new JPanel(new StackedBorderLayout());
        content.setBackground(BACKGROUND);
        content.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 2, 2, 2, BORDER),
                        BorderFactory.createEmptyBorder(3, 3, 3, 3)));

        setContentPane(content);
        content.setPreferredSize(
                new Dimension(Math.min(frameBounds.width, 300), Math.min(frameBounds.height, 500)));

        //

        final HtmlBuilder htm = new HtmlBuilder(100);
        for (final MessageToSend row : epf.values()) {
            htm.addln(row.context.student.stuId);
        }
        if (htm.length() == 0) {
            htm.addln("(no students to report)");
        }

        final JTextArea area = new JTextArea(htm.toString());
        area.setBackground(AREA_BG);
        area.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        final JScrollPane scroll = new JScrollPane(area);

        content.add(scroll, StackedBorderLayout.CENTER);

        pack();

        final Dimension size = getSize();
        setLocation(frameBounds.x + (frameBounds.width - size.width) / 2,
                frameBounds.y + (frameBounds.height - size.height) / 2);
    }
}
