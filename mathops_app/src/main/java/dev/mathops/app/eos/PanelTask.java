package dev.mathops.app.eos;

import dev.mathops.core.log.Log;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;

/**
 * A panel that presents a single task, with associated instructions and notes.
 */
final class PanelTask extends JPanel implements ActionListener {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = -2456374778088448129L;

    /** A panel to display task details */
    private final JPanel detailsPane;

    /** A Panel to allow the user to enter notes. */
    private final JPanel notesPane;

    /**
     * Constructs a new {@code PanelTask}.
     *
     * @param theSkin      the UI skin
     * @param theTaskState the task state
     */
    PanelTask(final Skin theSkin, final TaskState theTaskState) {

        super(new BorderLayout());

        setBackground(theSkin.taskBackground);

        final JPanel taskFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 3));
        taskFlow.setBackground(theSkin.taskBackground);

        final String indent = "    ".repeat(Math.max(0, theTaskState.indent));

        taskFlow.add(new JLabel(indent));

        final JCheckBox check = new JCheckBox(theTaskState.label);
        check.setActionCommand("CHECK");
        check.addActionListener(this);
        check.setSelected(theTaskState.whenCompleted != null);
        check.setBackground(theSkin.taskBackground);
        check.setForeground(theSkin.taskForeground);
        check.setFont(theSkin.taskFont);
        taskFlow.add(check);

        final JButton details = new JButton("Show Details");
        details.setFont(theSkin.taskFont);
        details.setActionCommand("DETAILS");
        details.addActionListener(this);
        taskFlow.add(details);

        final JButton notes = new JButton("Show Notes...");
        notes.setFont(theSkin.taskFont);
        notes.setActionCommand("NOTES");
        notes.addActionListener(this);
        taskFlow.add(notes);

        add(taskFlow, BorderLayout.PAGE_START);
        final String indent2 = indent + "        ";

        //

        this.detailsPane = new JPanel(new BorderLayout(2, 2));
        this.detailsPane.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        this.detailsPane.setBackground(theSkin.taskBackground);
        this.detailsPane.add(new JLabel(indent2), BorderLayout.LINE_START);
        this.detailsPane.setVisible(false);

        final JTextArea detailsArea = new JTextArea(1, 1);
        if (theTaskState.details != null) {
            detailsArea.setText(theTaskState.details);
        }
        detailsArea.setBorder(BorderFactory.createLoweredBevelBorder());
        detailsArea.setBackground(theSkin.detailsBackground);
        detailsArea.setForeground(theSkin.detailsForeground);
        detailsArea.setFont(theSkin.detailsFont);
        this.detailsPane.add(detailsArea, BorderLayout.CENTER);
        add(this.detailsPane, BorderLayout.CENTER);

        final JButton saveDetails = new JButton("Save");
        saveDetails.setActionCommand("SAVE_DETAILS");
        saveDetails.addActionListener(this);
        final JPanel detailsEast = new JPanel(new BorderLayout());
        detailsEast.setBackground(theSkin.taskBackground);
        detailsEast.add(saveDetails, BorderLayout.PAGE_START);
        this.detailsPane.add(detailsEast, BorderLayout.LINE_END);

        //

        this.notesPane = new JPanel(new BorderLayout(2, 2));
        this.notesPane.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        this.notesPane.setBackground(theSkin.taskBackground);
        this.notesPane.add(new JLabel(indent2), BorderLayout.LINE_START);
        this.notesPane.setVisible(false);

        final JTextArea notesArea = new JTextArea(1, 1);
        if (theTaskState.notes != null) {
            notesArea.setText(theTaskState.notes);
        }
        notesArea.setBorder(BorderFactory.createLoweredBevelBorder());
        notesArea.setBackground(theSkin.notesBackground);
        notesArea.setForeground(theSkin.notesForeground);
        notesArea.setFont(theSkin.notesFont);
        this.notesPane.add(notesArea, BorderLayout.CENTER);
        add(this.notesPane, BorderLayout.PAGE_END);

        final JButton saveNotes = new JButton("Save");
        saveNotes.setActionCommand("SAVE_NOTES");
        saveNotes.addActionListener(this);
        final JPanel notesEast = new JPanel(new BorderLayout());
        notesEast.add(saveNotes, BorderLayout.PAGE_START);
        notesEast.setBackground(theSkin.taskBackground);
        notesEast.add(saveNotes, BorderLayout.PAGE_START);
        this.notesPane.add(notesEast, BorderLayout.LINE_END);
    }

    /**
     * Called when a button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if ("CHECK".equals(cmd)) {
            Log.info("Checkbox");
        } else if ("DETAILS".equals(cmd)) {
            this.detailsPane.setVisible(!this.detailsPane.isVisible());
            invalidate();
            revalidate();
            repaint();
        } else if ("NOTES".equals(cmd)) {
            this.notesPane.setVisible(!this.notesPane.isVisible());
            invalidate();
            revalidate();
            repaint();
        } else if ("SAVE_DETAILS".equals(cmd)) {
            Log.info("Save Details");
        } else if ("SAVE_NOTES".equals(cmd)) {
            Log.info("Save Notes");
        }
    }
}
