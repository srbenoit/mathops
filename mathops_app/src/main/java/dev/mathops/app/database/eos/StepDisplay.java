package dev.mathops.app.database.eos;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

/**
 * A panel that includes step output and user notes.
 */
public final class StepDisplay extends JPanel implements DocumentListener {

    /** The report pane. */
    private final JTextArea reportPane;

    /** The notes pane. */
    private final JTextArea notesPane;

    /** The step whose results/notes are currently being displayed.  Edits to notes are stored in the step. */
    private AbstractStep currentStep;

    /**
     * Constructs a new {@code StepDisplay}.
     *
     * @param targetHeight the target height (each child is set to half this height)
     * @param targetWidth  the target width
     */
    StepDisplay(final int targetHeight, final int targetWidth) {

        super(new StackedBorderLayout());

        final Font headerFont = new Font(Font.DIALOG, Font.BOLD, 11);

        final JLabel reportHeader = new JLabel("Execution Results:");
        reportHeader.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        reportHeader.setFont(headerFont);

        final JLabel notesHeader = new JLabel("Notes:");
        notesHeader.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        notesHeader.setFont(headerFont);

        final Dimension size = reportHeader.getPreferredSize();
        final int h = (targetHeight - size.height - size.height) / 2;

        this.reportPane = new JTextArea();
        this.reportPane.setPreferredSize(new Dimension(targetWidth - 20, h));
        this.reportPane.setEditable(false);
        this.reportPane.setBackground(Color.WHITE);
        final JScrollPane reportScroll = new JScrollPane(this.reportPane);
        reportScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        this.notesPane = new JTextArea();
        this.notesPane.setPreferredSize(new Dimension(targetWidth - 20, h));
        this.notesPane.setEditable(true);
        this.notesPane.setBackground(Color.WHITE);
        final JScrollPane notesScroll = new JScrollPane(this.notesPane);
        notesScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        this.notesPane.getDocument().addDocumentListener(this);

        add(reportHeader, StackedBorderLayout.NORTH);
        add(reportScroll, StackedBorderLayout.NORTH);
        add(notesHeader, StackedBorderLayout.NORTH);
        add(notesScroll, StackedBorderLayout.CENTER);
    }

    /**
     * Sets the step whose results and notes to display
     *
     * @param step the step to show (null to clear results and notes)
     */
    public void setStep(final AbstractStep step) {

        // Prevent the "notes" document listener from trying to update the step
        this.currentStep = null;

        switch (step) {
            case null -> {
                this.reportPane.setText(CoreConstants.EMPTY);
                this.notesPane.setText(CoreConstants.EMPTY);
            }
            case final StepManual ignored -> {
                this.reportPane.setText("(this step is not automated)");
                this.notesPane.setText(step.notes);
            }
            default -> {
                this.reportPane.setText(step.results);
                this.notesPane.setText(step.notes);
            }
        }

        this.currentStep = step;
    }

    /**
     * Called when text is inserted into the "notes" document.
     *
     * @param e the document event
     */
    @Override
    public void insertUpdate(final DocumentEvent e) {
        updateNotes();
    }

    /**
     * Called when text is removed from the "notes" document.
     *
     * @param e the document event
     */
    @Override
    public void removeUpdate(final DocumentEvent e) {
        updateNotes();
    }

    /**
     * Called when text is updated in the "notes" document.
     *
     * @param e the document event
     */
    @Override
    public void changedUpdate(final DocumentEvent e) {
        updateNotes();
    }

    /**
     * Updates the notes in the current step (if there is a current step) to match the Notes text area.
     */
    private void updateNotes() {

        if (this.currentStep != null) {
            this.currentStep.notes = this.notesPane.getText();
        }
    }
}
