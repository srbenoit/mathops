package dev.mathops.app.adm.management;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.schema.legacy.RawCourse;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A panel that presents all configured exams (non-mastery) and allows the user to add, delete, and edit.
 */
final class AssessExamsPanel extends AdmPanelBase implements ActionListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 4844651125895554806L;

    /** Map from course ID to button. */
    private final Map<String, JToggleButton> courseButtons;

    /**
     * Constructs a new {@code AssessExamsPanel}.
     *
     * @param courses          the sorted list of courses
     */
    AssessExamsPanel(final Collection<RawCourse> courses) {

        super();
        setBackground(Skin.LIGHTEST);

        final JPanel buttonPanel = new JPanel(new StackedBorderLayout(3, 3));
        buttonPanel.setBackground(Skin.LIGHTEST);
        add(buttonPanel, StackedBorderLayout.WEST);

        this.courseButtons = new HashMap<>(courses.size());

        for (final RawCourse course : courses) {
            final String id = course.course;
            if ("M 384".equals(id) || "M 495".equals(id)) {
                continue;
            }

            final JToggleButton btn = new JToggleButton(id);
            buttonPanel.add(btn, StackedBorderLayout.NORTH);
            btn.addActionListener(this);
            this.courseButtons.put(id, btn);
        }

        final JPanel center = makeOffWhitePanel(new BorderLayout(0, 0));
        center.setBorder(BorderFactory.createEtchedBorder());
        center.setBackground(Skin.LIGHTEST);
        add(center, StackedBorderLayout.CENTER);
    }

    /**
     * Called when a button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        String selectedCourse = null;

        final Object src = e.getSource();
        if (src instanceof JToggleButton) {
            for (final Map.Entry<String, JToggleButton> entry : this.courseButtons
                    .entrySet()) {
                final JToggleButton btn = entry.getValue();
                if (btn == src) {
                    if (btn.isSelected()) {
                        selectedCourse = entry.getKey();
                    }
                } else if (btn.isSelected()) {
                    btn.setSelected(false);
                }
            }

            Log.info("Selected course is " + selectedCourse);
        }
    }
}
