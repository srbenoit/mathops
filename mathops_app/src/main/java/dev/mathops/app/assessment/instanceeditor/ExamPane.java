package dev.mathops.app.assessment.instanceeditor;

import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.FlowLayout;
import java.io.Serial;

/**
 * A pane that presents the selected exam for editing.
 */
final class ExamPane extends JPanel {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -7297568000527838815L;

    /**
     * Constructs a new {@code ExamPane}.
     *
     * @param theExam the exam this pane will render
     * @param bg      the background color
     */
    ExamPane(final ExamObj theExam, final Color bg) {

        super(new StackedBorderLayout());
        setBackground(bg);

        // this.exam = theExam;

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        final JTextField ref = new JTextField(25);
        ref.setEditable(false);
        if (theExam.ref != null) {
            ref.setText(theExam.ref);
        }

        final JPanel flow1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 0));
        flow1.setBackground(bg);
        flow1.add(new JLabel("ID:"));
        flow1.add(ref);

        add(flow1, StackedBorderLayout.NORTH);
    }
}
