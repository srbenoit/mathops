package dev.mathops.app.eos;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

/**
 * A panel that presents a checklist of tasks with their status. Each checklist item has a check status, a label, a
 * button to access detailed information, a button to toggle completed status, and a button to edit notes.
 */
final class PanelChecklist extends JPanel {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = -4188848026456784968L;

    /** A skin to style UI components. */
    private final Skin skin;

    /** Map from task ID to task state. */
    private final Map<String, TaskState> tasks;

    /**
     * Constructs a new {@code PanelChecklist}.
     */
    PanelChecklist() {

        super(new BorderLayout());

        this.skin = new Skin();
        this.tasks = new HashMap<>(10);

        setBackground(this.skin.panelBackground);

        final JPanel list = new JPanel();
        final LayoutManager box = new BoxLayout(list, BoxLayout.PAGE_AXIS);
        list.setLayout(box);
        add(list, BorderLayout.PAGE_START);

        // Load up the headings and tasks (with indentation to show sub-tasks)
        addHeading("Preparation:", list);

        addTask(0, "PREP_WATCHLIST",
                "1, Review watchlist students to see if Incompletes are warranted",
                "Review students in 'pace_appeals' who received some sort of accommodation to see if any did not "
                        + "finish, but would be candidates for an Incomplete.",
                null, list);
    }

    /**
     * Adds a heading to the checkbox list.
     *
     * @param label the label
     * @param list  the checkbox list to which to add the heading
     */
    private void addHeading(final String label, final JPanel list) {

        final JPanel headingFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 3));
        headingFlow.setBackground(this.skin.headingBackground);

        final JLabel headingLabel = new JLabel(label);
        headingLabel.setFont(this.skin.headingFont);
        headingLabel.setForeground(this.skin.headingForeground);
        headingFlow.add(headingLabel);

        list.add(headingFlow);
    }

    /**
     * Adds a task to the checkbox list.
     *
     * @param indent      the indentation level
     * @param taskId      the task ID, under which to persist task-related state and data
     * @param taskLabel   the task label
     * @param taskDetails the task details
     * @param taskNotes   the task notes (null if none)
     * @param list        the checkbox list to which to add the task
     * @return the task state
     */
    private TaskState addTask(final int indent, final String taskId, final String taskLabel,
                              final String taskDetails, final String taskNotes, final JPanel list) {

        final TaskState state = new TaskState(indent, taskLabel);
        this.tasks.put(taskId, state);
        state.details = taskDetails;
        state.notes = taskNotes;

        final PanelTask pane = new PanelTask(this.skin, state);
        list.add(pane);

        return state;
    }
}
