package dev.mathops.app.eos;

import java.awt.Color;
import java.awt.Font;

/**
 * Skin with fonts and colors for use in UI components.
 */
final class Skin {

    /** A background color for panels. */
    final Color panelBackground;

    /** A font for headings. */
    final Font headingFont;

    /** A background color for headings. */
    final Color headingBackground;

    /** A foreground color for headings. */
    final Color headingForeground;

    /** A font for tasks. */
    final Font taskFont;

    /** A background color for tasks. */
    final Color taskBackground;

    /** A foreground color for tasks. */
    final Color taskForeground;

    /** A font for details. */
    final Font detailsFont;

    /** A background color for details. */
    final Color detailsBackground;

    /** A foreground color for details. */
    final Color detailsForeground;

    /** A font for notes. */
    final Font notesFont;

    /** A background color for notes. */
    final Color notesBackground;

    /** A foreground color for notes. */
    final Color notesForeground;

    /**
     * Constructs a new {@code Skin}.
     */
    Skin() {

        this.panelBackground = new Color(80, 80, 80);

        this.headingFont = new Font(Font.DIALOG, Font.BOLD, 12);
        this.headingBackground = new Color(50, 50, 50);
        this.headingForeground = new Color(255, 255, 100);

        this.taskFont = new Font(Font.DIALOG, Font.PLAIN, 12);
        this.taskBackground = new Color(80, 80, 80);
        this.taskForeground = new Color(240, 240, 240);

        this.detailsFont = new Font(Font.DIALOG, Font.BOLD, 12);
        this.detailsBackground = new Color(50, 50, 70);
        this.detailsForeground = new Color(220, 220, 255);

        this.notesFont = new Font(Font.MONOSPACED, Font.BOLD, 12);
        this.notesBackground = new Color(50, 70, 50);
        this.notesForeground = new Color(220, 255, 220);
    }
}
