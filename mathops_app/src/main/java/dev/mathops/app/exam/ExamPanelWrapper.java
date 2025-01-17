package dev.mathops.app.exam;

import dev.mathops.app.ui.InternalPanelBase;
import dev.mathops.commons.log.Log;

import javax.swing.SwingUtilities;
import java.io.Serial;

/**
 * A container to wrap the exam panel in an internal frame.
 */
public class ExamPanelWrapper extends InternalPanelBase {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -7776138623651327154L;

    /** The panel content pane. */
    private final ExamPanel contentPane;

    /**
     * Constructs a new {@code ExamPanelWrapper}.
     *
     * @param content the {@code ExamPanel} that this panel is wrapping
     */
    public ExamPanelWrapper(final ExamPanel content) {

        super(null, content.getUsername());

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        setResOwner(content);
        this.contentPane = content;
    }

    /**
     * Gets the exam panel being wrapped by this frame.
     *
     * @return the exam panel
     */
    public ExamPanel getContent() {

        return this.contentPane;
    }

    /**
     * Constructs the exam delivery user interface.
     */
    public void buildUI() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        setContentPane(this.contentPane);
        this.contentPane.setSize(getSize());
        this.contentPane.buildUI();
    }
}
