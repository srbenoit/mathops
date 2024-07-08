package dev.mathops.app.placement.results;

import dev.mathops.commons.CoreConstants;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.Serial;
import java.net.URL;

/**
 * The first panel in the placement results wizard.
 */
class ResultsPanel1 extends JPanel {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 430554685185625566L;

    /**
     * Construct a new {@code SurveyPanel1}.
     */
    ResultsPanel1() {
        super();

        setLayout(new BorderLayout());

        final JPanel content = getContentPanel();
        content.setBorder(new EmptyBorder(10, 10, 10, 10));

        final JLabel iconLabel = new JLabel();
        final ImageIcon icon = getImageIcon();
        iconLabel.setIcon(icon);

        add(iconLabel, BorderLayout.WEST);

        final JPanel inner = new JPanel(new BorderLayout());
        inner.add(content, BorderLayout.NORTH);
        add(inner, BorderLayout.CENTER);
    }

    /**
     * Build the content panel.
     *
     * @return the constructed panel
     */
    private static JPanel getContentPanel() {

        final JPanel content = new JPanel();
        content.setLayout(new BorderLayout());

        final JLabel label = new JLabel("Math Challenge Exam Results");
        label.setFont(new Font("Dialog", Font.BOLD, 16));
        content.add(label, BorderLayout.NORTH);

        final JPanel inner = new JPanel();
        inner.setLayout(new GridLayout(0, 1));

        inner.add(new JLabel(CoreConstants.EMPTY));
        inner.add(new JLabel("Your Math Challenge Exam has been scored."));
        inner.add(new JLabel(CoreConstants.EMPTY));
        inner.add(new JLabel("Math Challenge Exam results are sent to the University records"));
        inner.add(new JLabel("system every evening at 10pm MST.  Placement results for exams"));
        inner.add(new JLabel("that are completed after 10pm MST are sent to the University"));
        inner.add(new JLabel("records system at 10pm on the following day."));
        inner.add(new JLabel(CoreConstants.EMPTY));

        content.add(inner, BorderLayout.CENTER);

        return content;
    }

    /**
     * Get the image for the left side of the dialog.
     *
     * @return the loaded left-side image
     */
    private static ImageIcon getImageIcon() {

        return new ImageIcon(getResource("sidebar.jpg"));
    }

    /**
     * Use a class loader to obtain a resource.
     *
     * @param key the resource key
     * @return the URL of the resource
     */
    private static URL getResource(final String key) {

        return (key == null) ? null : Results.class.getResource(key);
    }
}
