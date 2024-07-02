package dev.mathops.app.assessment.instanceeditor;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.io.Serial;

/**
 * A pane that presents a DocColumn and supports editing.
 */
public final  class DocColumnPane extends JPanel {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = 2419565058496405126L;

    /** The XML text area. */
    private final JTextArea textArea;

    /**
     * Constructs a new {@code DocColumnPane}.
     */
    DocColumnPane() {

        super(new BorderLayout());

        this.textArea = new JTextArea(3, 30);
        add(this.textArea, BorderLayout.CENTER);
    }

    /**
     * Sets the text.
     *
     * @param theText the text
     */
    public void setText(final String theText) {

        this.textArea.setText(theText);
    }

    /**
     * Gets the text.
     *
     * @return the text
     */
    public String getText() {

        return this.textArea.getText();
    }
}
