package dev.mathops.app.course.presenter;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.util.Objects;

/**
 * A panel that presents all media in a selected path.
 */
final class MediaPane extends JPanel {

    /** The owning window. */
    private final MainWindow owner;

    /** The course directory. */
    private final File courseDirectory;

    /** The header to show the currently selected subject and topic. */
    private final JLabel header;

    /** The center panel. */
    private final JPanel center;

    /** The current displayed file list. */
    private MediaFileListPane fileList = null;

    /** The currently selected file. */
    private File currentFile;

    /** The current displayed presentation. */
    private PresentationPane presentation = null;

    /**
     * Constructs a new {@code MediaPane}.
     *
     * @param theOwner           the owning window
     * @param theCourseDirectory the course directory
     */
    MediaPane(final MainWindow theOwner, final File theCourseDirectory) {

        super(new StackedBorderLayout(1, 1));

        this.owner = theOwner;
        this.courseDirectory = theCourseDirectory;

        setPreferredSize(new Dimension(250, 300));

        this.header = new JLabel(CoreConstants.SPC);
        final JPanel headerFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 3));
        headerFlow.add(this.header);

        add(headerFlow, StackedBorderLayout.NORTH);

        final Color bg = getBackground();
        final Color accent = MainWindow.getAccent(bg);

        this.center = new JPanel(new StackedBorderLayout(1, 1));

        final Border etched = BorderFactory.createEtchedBorder();
        this.center.setBorder(etched);
        this.center.setBackground(accent);
        add(this.center, StackedBorderLayout.CENTER);

    }

    /**
     * Initializes the panel.  Called after the constructor since this method leaks 'this' and the object is not fully
     * constructed within the constructor.
     */
    void init() {

        // No action
    }

    /**
     * Called when a directory is selected in the directory tree pane.
     *
     * @param selection the list of directory names below the course directory (at least length 2)
     */
    void processSelection(final Iterable<String> selection) {

        if (this.fileList != null) {
            this.center.remove(this.fileList);
        }

        File dir = this.courseDirectory;

        final StringBuilder headerBuilder = new StringBuilder(100);
        boolean colon = false;
        for (final String sel : selection) {
            if (colon) {
                headerBuilder.append(" : ");
            }
            headerBuilder.append(sel);
            dir = new File(dir, sel);
            colon = true;
        }
        final String headerString = headerBuilder.toString();
        this.header.setText(headerString);

        this.fileList = new MediaFileListPane(this, dir);
        this.fileList.init();
        this.center.add(this.fileList, StackedBorderLayout.WEST);
    }

    /**
     * Called when a file is selected in the file list tree pane.
     *
     * @param selection the selected file
     */
    void fileSelected(final File selection) {

        if (!Objects.equals(this.currentFile, selection)) {

            if (this.presentation != null) {
                this.center.remove(this.presentation);
                this.presentation = null;
            }

            final String filename = selection.getName();
            if (filename.endsWith(".pres")) {
                this.presentation = new PresentationPane(selection);
                this.center.add(this.presentation, StackedBorderLayout.CENTER);

            }

            this.currentFile = selection;
        }
    }
}
