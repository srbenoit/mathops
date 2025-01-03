package dev.mathops.app.course.presenter;

import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.EDebugLevel;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.model.ModelTreeNode;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.text.model.ETreeParserMode;
import dev.mathops.text.model.XmlTreeParser;
import dev.mathops.text.parser.ParsingException;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.io.File;

/**
 * A panel that presents a presentation for editing.
 */
final class PresentationPane extends JPanel {

    /** The preferred width. */
    private static final int PREF_WIDTH = 220;

    /** The preferred height. */
    private static final int PREF_HEIGHT = 300;

    /** The presentation file. */
    private final File presFile;

    /** The root node of the presentation model tree. */
    private final ModelTreeNode presentation;

    /**
     * Constructs a new {@code PresentationPane}.
     *
     * @param thePresFile the presentation file
     */
    PresentationPane(final File thePresFile) {

        super(new StackedBorderLayout(1, 1));

        this.presFile = thePresFile;

        setPreferredSize(new Dimension(PREF_WIDTH, PREF_HEIGHT));

        final String fileContent = FileLoader.loadFileAsString(thePresFile, true);
        ModelTreeNode thePresentation = null;
        if (fileContent != null) {
            final char[] chars = fileContent.toCharArray();
            try {
                thePresentation = XmlTreeParser.parse(chars, ETreeParserMode.ELEMENTS_ONLY, new PresentationFormat(),
                        EDebugLevel.INFO);
            } catch (final ParsingException ex) {
                Log.warning(ex);
            }
        }

        this.presentation = thePresentation;
    }

    /**
     * Initializes the panel.  Called after the constructor since this method leaks 'this' and the object is not fully
     * constructed within the constructor.
     */
    void init() {

        // No action
    }
}
