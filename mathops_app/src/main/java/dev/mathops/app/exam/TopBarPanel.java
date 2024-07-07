package dev.mathops.app.exam;

import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.assessment.exam.ExamSection;
import dev.mathops.assessment.exam.ExamSession;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.ColorNames;
import dev.mathops.font.BundledFontManager;
import dev.mathops.font.FontSpec;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.Serial;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * A generalized top bar panel for use in testing and homework applications. This panel supports a title, username
 * message, current time clock, remaining time clock, and sections list. It supports a list of listeners to receive
 * notification of when the timer has expired, and acts as a listener for changes in the state of sections. It can read
 * its font, color and layout settings from a standardized skin definition.
 */
final class TopBarPanel extends JPanel implements Runnable, ComponentListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 2234221589647154598L;

    /** Left alignment constant for fields. */
    private static final int LEFT = 0;

    /** Right alignment constant for fields. */
    private static final int RIGHT = 1;

    /** Center alignment constant for fields. */
    private static final int CENTER = 2;

    /** The exam that provides the data model for this panel. */
    private final ExamObj exam;

    // Data used to generate the display.

    /** The expiration time for the countdown timer. */
    private long expiry;

    // Skin settings used to customize the display.

    /** The background color, as read from the skin. */
    private final Color backgroundColor;

    /** The format in which to display the time - null to omit time display. */
    private String clockFormat;

    /** The font to use for the clock. */
    private Font clockFont;

    /** The color in which to paint the clock. */
    private final Color clockColor;

    /** The color in which to paint the clock shadow, if present. */
    private final Color clockShadowColor;

    /** The X offset of the clock shadow. */
    private int clockShadowDx;

    /** The Y offset of the clock shadow. */
    private int clockShadowDy;

    /** The X position of the clock (0.0 to 1.0). */
    private double clockX;

    /** The alignment of the clock LEFT, RIGHT, or CENTER. */
    private final int clockAlignment;

    /** The Y position of the clock (0.0 to 1.0). */
    private int clockY;

    /** The format for the remaining time - null to omit remaining time. */
    private String timerFormat;

    /** The font to use for the timer. */
    private Font timerFont;

    /** The color in which to paint the timer. */
    private final Color timerColor;

    /** The color in which to paint the timer shadow, if present. */
    private final Color timerShadowColor;

    /** The X offset of the timer shadow. */
    private int timerShadowDx;

    /** The Y offset of the timer shadow. */
    private int timerShadowDy;

    /** The X position of the timer (0.0 to 1.0). */
    private double timerX;

    /** The alignment of the timer LEFT, RIGHT, or CENTER. */
    private final int timerAlignment;

    /** The Y position of the timer (0.0 to 1.0). */
    private int timerY;

    /** True to show sections list; false to omit. */
    private final boolean showSections;

    /** The font to use for the sections list. */
    private Font sectionFont;

    /** The color in which to paint the sections list. */
    private final Color sectionColor;

    // Data used internally by the class

    /** The offscreen back-buffer. */
    private BufferedImage offscreen;

    /** Size adjustment, -3 to +5. */
    private int sizeAdjustment;

    /** The base size for the font to use for the clock. */
    private float timerFontBaseSize;

    /** The title to display - null to omit the title. */
    private final String title;

    /** The username message to display - null to omit the username. */
    private String username;

    // Skin settings used to customize the display.

    /** The base size for the font to use for the title. */
    private float titleFontBaseSize;

    /** The font to use for the title. */
    private Font titleFont;

    /** The color in which to paint the title. */
    private final Color titleColor;

    /** The color in which to paint the title shadow, if present. */
    private final Color titleShadowColor;

    /** The X offset of the title shadow. */
    private int titleShadowDx;

    /** The Y offset of the title shadow. */
    private int titleShadowDy;

    /** The X position of the title (0.0 to 1.0). */
    private double titleX;

    /** The alignment of the title LEFT, RIGHT, or CENTER. */
    private final int titleAlignment;

    /** The Y position of the title (0.0 to 1.0). */
    private int titleY;

    /** The base size for the font to use for the username. */
    private float usernameFontBaseSize;

    /** The font to use for the username. */
    private Font usernameFont;

    /** The color in which to paint the username. */
    private final Color usernameColor;

    /** The color in which to paint the username shadow, if present. */
    private final Color usernameShadowColor;

    /** The X offset of the username shadow. */
    private int usernameShadowDx;

    /** The Y offset of the username shadow. */
    private int usernameShadowDy;

    /** The X position of the username (0.0 to 1.0). */
    private double usernameX;

    /** The alignment of the username LEFT, RIGHT, or CENTER. */
    private final int usernameAlignment;

    /** The Y position of the username (0.0 to 1.0). */
    private int usernameY;

    /** The base size for the font to use for the clock. */
    private float clockFontBaseSize;

    /** The base size for the font to use for the clock. */
    private float sectionFontBaseSize;

    /** The X position of the sections list (0.0 to 1.0). */
    private double sectionX;

    /** The Y position of the sections list (0.0 to 1.0). */
    private int sectionY;

    /** The Y offset of an optional horizontal divider. */
    private int dividerY;

    /** The color for the divider. */
    private final Color dividerColor;

    /** The starting X position of the divider. */
    private double dividerStartX;

    /** The end X position of the divider. */
    private double dividerEndX;

    // Data used internally by the class

    /** A list of titles for each of the sections to display. */
    private final String[] sectionTitles;

    /** A runnable class to update clock and timer displays. */
    private TopBarPanelClockUpdater clockUpdater;

    /** A runnable class to update section labels. */
    private TopBarPanelSectionUpdater sectionUpdater;

    /** A list of timer listeners to notify when time expires. */
    private final List<ExamContainerInt> listeners;

    /**
     * Constructs a new {@code TopBarPanel}.
     *
     * @param theExamSession the exam session
     * @param theUsername    the username to be displayed
     * @param practice       {@code true} if this is a practice assignment; {@code false} otherwise
     * @param zeroRequired   {@code true} if the required score on the assignment is zero
     * @param skin           the properties object containing the skin settings
     */
    TopBarPanel(final ExamSession theExamSession, final String theUsername, final boolean practice,
                final boolean zeroRequired, final Properties skin) {

        super();

        if (!SwingUtilities.isEventDispatchThread()) {
           Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        this.exam = theExamSession.getExam();

        final BundledFontManager fonts = BundledFontManager.getInstance();
        final FontSpec spec = new FontSpec();

        if (this.exam != null && this.exam.allowedSeconds != null) {
            // Add half second to exam timeout for roundoff
            this.expiry = System.currentTimeMillis() + (this.exam.allowedSeconds.longValue() * 1000L) + 500L;
        }

        this.listeners = new ArrayList<>(10);

        // Load the skin
        String prop;

        if (theExamSession.getState().showAnswers || theExamSession.getState().showSolutions) {
            prop = skin.getProperty("top-bar-title-show-answers");

            if (prop == null) {
                prop = skin.getProperty("top-bar-title");
            }
        } else {
            prop = skin.getProperty("top-bar-title");
        }

        if (this.exam == null) {
            this.title = "ERROR";
        } else {
            if ((prop == null) || (prop.isEmpty())) {
                this.title = this.exam.examName;
            } else {
                this.title = prop.replace("$EXAM_TITLE", this.exam.examName);
            }
        }

        if (theUsername != null) {
            prop = null;

            if (practice) {
                prop = skin.getProperty("top-bar-username-practice");
            }

            if (prop == null && zeroRequired) {
                prop = skin.getProperty("top-bar-username-zero-req");
            }

            if (prop == null && this.exam != null && this.exam.getNumSections() == 1) {
                prop = skin.getProperty("top-bar-username-one-section");
            }

            if (prop == null) {
                prop = skin.getProperty("top-bar-username");
            }

            if (prop == null || prop.isEmpty()) {
                this.username = theUsername;
            } else {
                this.username = prop.replace("$USERNAME", theUsername);
            }
        }

        prop = skin.getProperty("top-bar-background-color");
        this.backgroundColor = (prop != null) ? ColorNames.getColor(prop) : Color.WHITE;

        prop = skin.getProperty("top-bar-title-font");
        spec.fontName = (prop != null) ? prop : BundledFontManager.SANS;

        prop = skin.getProperty("top-bar-title-size");

        this.titleFontBaseSize = 0.0f;

        try {
            this.titleFontBaseSize = (prop != null) ? Float.parseFloat(prop) : 20.0f;
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            this.titleFontBaseSize = 20.0f;
        }

        spec.fontSize = (double) this.titleFontBaseSize;

        prop = skin.getProperty("top-bar-title-style");
        spec.fontStyle = Font.PLAIN;

        if (prop != null) {
            if (prop.contains("italic") || prop.contains("ITALIC")) {
                spec.fontStyle = spec.fontStyle | Font.ITALIC;
            }

            if (prop.contains("bold") || prop.contains("BOLD")) {
                spec.fontStyle = spec.fontStyle | Font.BOLD;
            }
        }

        this.titleFont = fonts.getFont(spec);

        prop = skin.getProperty("top-bar-title-color");
        this.titleColor = (prop != null) ? ColorNames.getColor(prop) : Color.BLACK;

        prop = skin.getProperty("top-bar-title-shadow-color");
        this.titleShadowColor = (prop != null) ? ColorNames.getColor(prop) : null;

        prop = skin.getProperty("top-bar-title-shadow-dx");

        try {
            this.titleShadowDx = (prop != null) ? Integer.parseInt(prop) : 0;
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            this.titleShadowDx = 0;
        }

        prop = skin.getProperty("top-bar-title-shadow-dy");

        try {
            this.titleShadowDy = (prop != null) ? Integer.parseInt(prop) : 0;
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            this.titleShadowDy = 0;
        }

        prop = skin.getProperty("top-bar-title-x");

        try {
            this.titleX = (prop != null) ? Double.parseDouble(prop) : 0.0;
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            this.titleX = 0.0;
        }

        prop = skin.getProperty("top-bar-title-y");

        try {
            this.titleY = (prop != null) ? Integer.parseInt(prop) : 0;
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            this.titleY = 0;
        }

        prop = skin.getProperty("top-bar-title-alignment");

        if (prop == null) {
            this.titleAlignment = LEFT;
        } else if ("LEFT".equalsIgnoreCase(prop)) {
            this.titleAlignment = LEFT;
        } else if ("RIGHT".equalsIgnoreCase(prop)) {
            this.titleAlignment = RIGHT;
        } else if ("CENTER".equalsIgnoreCase(prop)) {
            this.titleAlignment = CENTER;
        } else {
            Log.info("Title alignment invalid - using LEFT.");
            this.titleAlignment = LEFT;
        }

        prop = skin.getProperty("top-bar-username-font");
        spec.fontName = (prop != null) ? prop : BundledFontManager.SANS;

        prop = skin.getProperty("top-bar-username-size");

        this.usernameFontBaseSize = 0.0f;
        try {
            this.usernameFontBaseSize = (prop != null) ? Float.parseFloat(prop) : 20.0f;
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            this.usernameFontBaseSize = 20.0f;
        }

        spec.fontSize = (double) this.usernameFontBaseSize;

        prop = skin.getProperty("top-bar-username-style");
        spec.fontStyle = Font.PLAIN;

        if (prop != null) {

            if (prop.contains("italic") || prop.contains("ITALIC")) {
                spec.fontStyle = spec.fontStyle | Font.ITALIC;
            }

            if (prop.contains("bold") || prop.contains("BOLD")) {
                spec.fontStyle = spec.fontStyle | Font.BOLD;
            }
        }

        this.usernameFont = fonts.getFont(spec);

        prop = skin.getProperty("top-bar-username-color");
        this.usernameColor = (prop != null) ? ColorNames.getColor(prop) : Color.BLACK;

        prop = skin.getProperty("top-bar-username-shadow-color");
        this.usernameShadowColor = (prop != null) ? ColorNames.getColor(prop) : null;

        prop = skin.getProperty("top-bar-username-shadow-dx");

        try {
            this.usernameShadowDx = (prop != null) ? Integer.parseInt(prop) : 0;
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            this.usernameShadowDx = 0;
        }

        prop = skin.getProperty("top-bar-username-shadow-dy");

        try {
            this.usernameShadowDy = (prop != null) ? Integer.parseInt(prop) : 0;
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            this.usernameShadowDy = 0;
        }

        prop = skin.getProperty("top-bar-username-x");

        try {
            this.usernameX = (prop != null) ? Double.parseDouble(prop) : 0.0;
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            this.usernameX = 0.0;
        }

        prop = skin.getProperty("top-bar-username-y");

        try {
            this.usernameY = (prop != null) ? Integer.parseInt(prop) : 0;
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            this.usernameY = 0;
        }

        prop = skin.getProperty("top-bar-username-alignment");

        if (prop == null) {
            this.usernameAlignment = LEFT;
        } else if ("LEFT".equalsIgnoreCase(prop)) {
            this.usernameAlignment = LEFT;
        } else if ("RIGHT".equalsIgnoreCase(prop)) {
            this.usernameAlignment = RIGHT;
        } else if ("CENTER".equalsIgnoreCase(prop)) {
            this.usernameAlignment = CENTER;
        } else {
            Log.info("Username alignment invalid - using LEFT.");
            this.usernameAlignment = LEFT;
        }

        this.clockFormat = skin.getProperty("top-bar-clock-format");

        if (CoreConstants.EMPTY.equals(this.clockFormat)) {
            this.clockFormat = null;
        }

        prop = skin.getProperty("top-bar-clock-font");
        spec.fontName = (prop != null) ? prop : BundledFontManager.SANS;

        prop = skin.getProperty("top-bar-clock-size");

        this.clockFontBaseSize = 0.0f;

        try {
            this.clockFontBaseSize = (prop != null) ? Float.parseFloat(prop) : 20.0f;
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            this.clockFontBaseSize = 20.0f;
        }

        spec.fontSize = (double) this.clockFontBaseSize;

        prop = skin.getProperty("top-bar-clock-style");
        spec.fontStyle = Font.PLAIN;

        if (prop != null) {
            if (prop.contains("italic")
                    || prop.contains("ITALIC")) {
                spec.fontStyle = spec.fontStyle | Font.ITALIC;
            }

            if (prop.contains("bold")
                    || prop.contains("BOLD")) {
                spec.fontStyle = spec.fontStyle | Font.BOLD;
            }
        }

        this.clockFont = fonts.getFont(spec);

        prop = skin.getProperty("top-bar-clock-color");
        this.clockColor = (prop != null) ? ColorNames.getColor(prop) : Color.BLACK;

        prop = skin.getProperty("top-bar-clock-shadow-color");
        this.clockShadowColor = (prop != null) ? ColorNames.getColor(prop) : null;

        prop = skin.getProperty("top-bar-clock-shadow-dx");

        try {
            this.clockShadowDx = (prop != null) ? Integer.parseInt(prop) : 0;
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            this.usernameShadowDx = 0;
        }

        prop = skin.getProperty("top-bar-clock-shadow-dy");

        try {
            this.clockShadowDy = (prop != null) ? Integer.parseInt(prop) : 0;
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            this.clockShadowDy = 0;
        }

        prop = skin.getProperty("top-bar-clock-x");

        try {
            this.clockX = (prop != null) ? Double.parseDouble(prop) : 0.0;
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            this.clockX = 0.0;
        }

        prop = skin.getProperty("top-bar-clock-y");

        try {
            this.clockY = (prop != null) ? Integer.parseInt(prop) : 0;
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            this.clockY = 0;
        }

        prop = skin.getProperty("top-bar-clock-alignment");

        if (prop == null) {
            this.clockAlignment = LEFT;
        } else if ("LEFT".equalsIgnoreCase(prop)) {
            this.clockAlignment = LEFT;
        } else if ("RIGHT".equalsIgnoreCase(prop)) {
            this.clockAlignment = RIGHT;
        } else if ("CENTER".equalsIgnoreCase(prop)) {
            this.clockAlignment = CENTER;
        } else {
            Log.info("Clock alignment invalid - using LEFT.");
            this.clockAlignment = LEFT;
        }

        this.timerFormat = skin.getProperty("top-bar-timer-format");

        if (CoreConstants.EMPTY.equals(this.timerFormat)) {
            this.timerFormat = null;
        }

        prop = skin.getProperty("top-bar-timer-font");
        spec.fontName = (prop != null) ? prop : BundledFontManager.SANS;

        prop = skin.getProperty("top-bar-timer-size");

        this.timerFontBaseSize = 0.0f;
        try {
            this.timerFontBaseSize = (prop != null) ? Float.parseFloat(prop) : 20.0f;
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            this.timerFontBaseSize = 20.0f;
        }

        spec.fontSize = (double) this.timerFontBaseSize;

        prop = skin.getProperty("top-bar-timer-style");
        spec.fontStyle = Font.PLAIN;

        if (prop != null) {
            if (prop.contains("italic")
                    || prop.contains("ITALIC")) {
                spec.fontStyle = spec.fontStyle | Font.ITALIC;
            }

            if (prop.contains("bold")
                    || prop.contains("BOLD")) {
                spec.fontStyle = spec.fontStyle | Font.BOLD;
            }
        }

        this.timerFont = fonts.getFont(spec);

        prop = skin.getProperty("top-bar-timer-color");
        this.timerColor = (prop != null) ? ColorNames.getColor(prop) : Color.BLACK;

        prop = skin.getProperty("top-bar-timer-shadow-color");
        this.timerShadowColor = (prop != null) ? ColorNames.getColor(prop) : null;

        prop = skin.getProperty("top-bar-timer-shadow-dx");

        try {
            this.timerShadowDx = (prop != null) ? Integer.parseInt(prop) : 0;
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            this.timerShadowDx = 0;
        }

        prop = skin.getProperty("top-bar-timer-shadow-dy");

        try {
            this.timerShadowDy = (prop != null) ? Integer.parseInt(prop) : 0;
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            this.timerShadowDy = 0;
        }

        prop = skin.getProperty("top-bar-timer-x");

        try {
            this.timerX = (prop != null) ? Double.parseDouble(prop) : 0.0;
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            this.timerX = 0.0;
        }

        prop = skin.getProperty("top-bar-timer-y");

        try {
            this.timerY = (prop != null) ? Integer.parseInt(prop) : 0;
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            this.timerY = 0;
        }

        prop = skin.getProperty("top-bar-timer-alignment");

        if (prop == null) {
            this.timerAlignment = LEFT;
        } else if ("LEFT".equalsIgnoreCase(prop)) {
            this.timerAlignment = LEFT;
        } else if ("RIGHT".equalsIgnoreCase(prop)) {
            this.timerAlignment = RIGHT;
        } else if ("CENTER".equalsIgnoreCase(prop)) {
            this.timerAlignment = CENTER;
        } else {
            Log.info("Timer alignment invalid - using LEFT.");
            this.timerAlignment = LEFT;
        }

        if (this.exam != null && this.exam.getNumSections() == 1) {
            prop = skin.getProperty("top-bar-show-sections-if-one");
        } else if (practice) {
            prop = skin.getProperty("top-bar-show-sections");
            // prop = "false";
        } else {
            prop = skin.getProperty("top-bar-show-sections");
        }

        this.showSections = "true".equalsIgnoreCase(prop);

        prop = skin.getProperty("top-bar-section-font");
        spec.fontName = (prop != null) ? prop : BundledFontManager.SANS;

        prop = skin.getProperty("top-bar-section-size");

        this.sectionFontBaseSize = 0.0f;

        try {
            this.sectionFontBaseSize = (prop != null) ? Float.parseFloat(prop) : 20.0f;
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            this.sectionFontBaseSize = 20.0f;
        }

        spec.fontSize = (double) this.sectionFontBaseSize;

        prop = skin.getProperty("top-bar-section-style");
        spec.fontStyle = Font.PLAIN;

        if (prop != null) {
            if (prop.contains("italic")
                    || prop.contains("ITALIC")) {
                spec.fontStyle = spec.fontStyle | Font.ITALIC;
            }

            if (prop.contains("bold")
                    || prop.contains("BOLD")) {
                spec.fontStyle = spec.fontStyle | Font.BOLD;
            }
        }

        this.sectionFont = fonts.getFont(spec);

        prop = skin.getProperty("top-bar-section-color");
        this.sectionColor = (prop != null) ? ColorNames.getColor(prop) : Color.BLACK;

        prop = skin.getProperty("top-bar-section-x");

        try {
            this.sectionX = (prop != null) ? Double.parseDouble(prop) : 0.0;
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            this.sectionX = 0.0;
        }

        prop = skin.getProperty("top-bar-section-y");

        try {
            this.sectionY = (prop != null) ? Integer.parseInt(prop) : 0;
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            this.sectionY = 0;
        }

        prop = skin.getProperty("top-bar-border-size");

        int border;

        try {
            border = (prop != null) ? Integer.parseInt(prop) : 1;
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            border = 1;
        }

        prop = skin.getProperty("top-bar-border-inset");

        int inset;

        try {
            inset = (prop != null) ? Integer.parseInt(prop) : 1;
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            inset = 1;
        }

        prop = skin.getProperty("top-bar-divider-y");

        try {
            this.dividerY = (prop != null) ? Integer.parseInt(prop) : 0;
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            this.dividerY = 0;
        }

        prop = skin.getProperty("top-bar-divider-color");
        this.dividerColor = (prop != null) ? ColorNames.getColor(prop) : Color.BLACK;

        prop = skin.getProperty("top-bar-divider-start-x");

        try {
            this.dividerStartX = (prop != null) ? Double.parseDouble(prop) : 0.0;
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            this.dividerStartX = 0.0;
        }

        prop = skin.getProperty("top-bar-divider-end-x");

        try {
            this.dividerEndX = (prop != null) ? Double.parseDouble(prop) : 1.0;
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            this.dividerEndX = 1.0;
        }

        if (this.exam == null) {
            this.sectionTitles = new String[0];
        } else {
            final int numSect = this.exam.getNumSections();
            this.sectionTitles = new String[numSect];

            for (int i = 0; i < numSect; i++) {
                this.sectionTitles[i] = this.exam.getSection(i).sectionName;
            }
        }

        prop = skin.getProperty("top-bar-border-color");
        final String color = (prop != null) ? prop : "gray";

        // Construct the GUI, making sure it's done in the AWT thread.
        final Runnable builder =
                new TopBarPanelBuilder(this, border, inset, ColorNames.getColor(color));

        if (SwingUtilities.isEventDispatchThread()) {
            builder.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(builder);
            } catch (final Exception ex) {
                Log.warning(ex);
            }
        }
    }

    /**
     * Determines the preferred height of the top bar based on the displayed data.
     *
     * @return the preferred height
     */
    int computeHeight() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        Graphics grx = getGraphics();
        final float scale = (float) StrictMath.pow(2.5, (double) this.sizeAdjustment / 4.0);

        if (grx == null) {

            // We're not installed in a window yet, so build a Graphics
            final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            final GraphicsDevice[] gs = ge.getScreenDevices();

            if (gs.length == 0) {
                Log.info("Unable to get graphics device");
            }

            final GraphicsConfiguration gc = gs[0].getDefaultConfiguration();
            grx = gc.createCompatibleImage(1, 1).getGraphics();
        }

        // For each text element, get the baseline and the descent, and track the lowest of these
        // points
        int height = 0;

        if (this.titleFont != null) {
            final FontMetrics fm = grx.getFontMetrics(this.titleFont);

            if (((float) this.titleY * scale + (float) fm.getDescent()) > (float) height) {
                height = (int) ((float) this.titleY * scale) + fm.getDescent();
            }
        }

        if (this.usernameFont != null) {
            final FontMetrics fm = grx.getFontMetrics(this.usernameFont);

            if (((float) this.usernameY * scale + (float) fm.getDescent()) > (float) height) {
                height = (int) ((float) this.usernameY * scale) + fm.getDescent();
            }
        }

        if (this.clockFont != null) {
            final FontMetrics fm = grx.getFontMetrics(this.usernameFont);

            if (((float) this.clockY * scale + (float) fm.getDescent()) > (float) height) {
                height = (int) ((float) this.clockY * scale) + fm.getDescent();
            }
        }

        if (this.timerFont != null) {
            final FontMetrics fm = grx.getFontMetrics(this.usernameFont);

            if (((float) this.timerY * scale + (float) fm.getDescent()) > (float) height) {
                height = (int) ((float) this.timerY * scale) + fm.getDescent();
            }
        }

        // If sections are to be shown, see how many sections there are and compute the position
        // of the bottom section label.
        if ((this.showSections) && (this.sectionFont != null)) {

            // If the divider is lower, use that value (we only show the divider if sections are
            // being shown).
            if (this.dividerY > height) {
                height = (int) ((float) this.dividerY * scale);
            }

            final FontMetrics fm = grx.getFontMetrics(this.sectionFont);

            final int numSect = this.exam == null ? 1 : this.exam.getNumSections();
            final int bottom = (int) ((float) this.sectionY * scale) + (fm.getHeight() * numSect);

            if (bottom > height) {
                height = bottom;
            }

            // Add an extra pixel since sections get highlight
            height++;
        }

        // Add one empty pixel below everything
        height++;

        // Finally, add in the insets
        final Insets insets = getInsets();
        height += insets.top + insets.bottom;

        return height;
    }

    /**
     * Builds the offscreen buffered bitmap used to accelerate painting of the onscreen graphics.
     *
     * @param insets the insets for the panel
     */
    private void makeOffscreen(final Insets insets) {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        // Size and generate offscreen Image, and configure Graphics object.
        final int width = getWidth() - insets.left - insets.right;
        final int height = getPreferredSize().height - insets.top - insets.bottom;

        if ((width <= 0) || (height <= 0)) {
            return;
        }

        final BufferedImage off = (BufferedImage) createImage(width, height);
        final Graphics2D g2d = (Graphics2D) (off.getGraphics());
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setColor(this.backgroundColor);
        g2d.fillRect(0, 0, width, height);

        final float scale = (float) StrictMath.pow(2.5, (double) this.sizeAdjustment / 4.0);

        if ((this.showSections) && (this.dividerY != 0)) {
            g2d.setColor(this.dividerColor);
            g2d.drawLine((int) (this.dividerStartX * (double) width), (int) ((float) this.dividerY * scale),
                    (int) (this.dividerEndX * (double) width), (int) ((float) this.dividerY * scale));
        }

        drawField(g2d, this.title, width, this.titleX, (int) ((float) this.titleY * scale), this.titleFont,
                this.titleAlignment, this.titleShadowDx, this.titleShadowDy, this.titleColor,
                this.titleShadowColor, null, null, false);

        drawField(g2d, this.username, width, this.usernameX, (int) ((float) this.usernameY * scale),
                this.usernameFont, this.usernameAlignment, this.usernameShadowDx, this.usernameShadowDy,
                this.usernameColor, this.usernameShadowColor, null, null, false);

        // Make the offscreen buffer publicly available
        this.offscreen = off;

        if (this.exam != null) {
            g2d.setFont(this.sectionFont);

            final FontMetrics fm = g2d.getFontMetrics();
            final Rectangle rect = new Rectangle((int) ((double) width * this.sectionX),
                    (int) ((float) this.sectionY * scale), (int) ((double) width * (1.0 - this.sectionX)),
                    (int) ((float) this.exam.getNumSections() * (float) fm.getHeight() * scale));

            if (this.sectionUpdater == null) {
                this.sectionUpdater = new TopBarPanelSectionUpdater(this, rect, this.sectionTitles);
            } else {
                this.sectionUpdater.setRect(rect);
            }

            this.sectionUpdater.run();
        }

        if (this.clockUpdater == null) {
            this.clockUpdater = new TopBarPanelClockUpdater(this);
            this.clockUpdater.run();
        }
    }

    /**
     * Generates a String time display matching a given format string.
     *
     * @param format         the format string, containing HH, MM and SS tags
     * @param hours          the hours value
     * @param minutes        the minutes value
     * @param seconds        the seconds value
     * @param allowZeroHours {@code true} to display 0 as "0", {@code false} to show it as "12"
     * @return the generated time value
     */
    static String makeTime(final String format, final int hours, final int minutes, final int seconds,
                           final boolean allowZeroHours) {

        String str;

        if (hours == 0) {

            if (allowZeroHours) {
                str = format.replace("HH:", CoreConstants.EMPTY);
            } else {
                // Show "12" rather than "00"
                str = format.replace("HH", "12");
            }
        } else {
            str = format.replace("HH", Integer.toString(hours));
        }

        String value = Integer.toString(minutes / 10) + minutes % 10;
        str = str.replace("MM", value);

        value = Integer.toString(seconds / 10) + seconds % 10;
        str = str.replace("SS", value);

        return str;
    }

    /**
     * Draws a single field.
     *
     * @param g2d            the graphics to which to draw
     * @param txt            the text to draw
     * @param width          the width of the top bar panel
     * @param xPos           the X position (0.0 to 1.0) of the field
     * @param yPos           the Y position of the baseline of the field, in pixels
     * @param font           the font
     * @param alignment      the field alignment
     * @param shadowDx       the X offset of the shadow
     * @param shadowDy       the Y offset of the shadow
     * @param color          the text color
     * @param shadowColor    the shadow color
     * @param bgColor        the background color - if not {@code null}, the field should be blanked before drawing
     * @param highlightColor the highlight color - if not {@code null}, the field will be highlighted
     * @param blankFirst     {@code true} to blank the field area before drawing; {@code false} if the field area is
     *                       already known to be blank
     */
    void drawField(final Graphics2D g2d, final String txt, final int width, final double xPos, final int yPos,
                   final Font font, final int alignment, final int shadowDx, final int shadowDy, final Color color,
                   final Color shadowColor, final Color bgColor, final Color highlightColor, final boolean blankFirst) {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        if (txt == null) {
            return;
        }

        g2d.setFont(font);

        final FontMetrics fm = g2d.getFontMetrics();

        int left = (int) (xPos * (double) width);

        switch (alignment) {

            case RIGHT:
                left -= fm.stringWidth(txt);
                break;

            case CENTER:
                left -= fm.stringWidth(txt) / 2;
                break;

            default:
                break;
        }

        if (left < getInsets().left) {
            left = getInsets().left;
        }

        if ((bgColor != null) && blankFirst) {
            g2d.setColor(bgColor);

            switch (alignment) {

                case LEFT:
                    g2d.fillRect(left, yPos - fm.getAscent(), fm.stringWidth(txt) + 20,
                            fm.getAscent() + fm.getDescent());
                    break;

                case RIGHT:
                    g2d.fillRect(left - 20, yPos - fm.getAscent(), fm.stringWidth(txt) + 20,
                            fm.getAscent() + fm.getDescent());
                    break;

                case CENTER:
                    g2d.fillRect(left - 10, yPos - fm.getAscent(), fm.stringWidth(txt) + 20,
                            fm.getAscent() + fm.getDescent());
                    break;

                default:
                    break;
            }
        }

        if (highlightColor != null) {
            g2d.setColor(highlightColor);
            g2d.fillRect(left, yPos - fm.getAscent(), fm.stringWidth(txt), fm.getAscent() + fm.getDescent());
        }

        if ((shadowDx != 0) || (shadowDy != 0)) {
            g2d.setColor(shadowColor);
            g2d.drawString(txt, left + shadowDx, yPos + shadowDy);
        }

        g2d.setColor(color);
        g2d.drawString(txt, left, yPos);
    }

    /**
     * Gets the exam.
     *
     * @return the exam
     */
    public ExamObj getExam() {

        return this.exam;
    }

    /**
     * Gets the expiry.
     *
     * @return the expiry
     */
    long getExpiry() {

        return this.expiry;
    }

    /**
     * Gets the background color.
     *
     * @return the background color
     */
    Color getBackgroundColor() {

        return this.backgroundColor;
    }

    /**
     * Gets the clock format.
     *
     * @return the clock format
     */
    String getClockFormat() {

        return this.clockFormat;
    }

    /**
     * Gets the clock font.
     *
     * @return the clock font
     */
    Font getClockFont() {

        return this.clockFont;
    }

    /**
     * Gets the clock color.
     *
     * @return the clock color
     */
    Color getClockColor() {

        return this.clockColor;
    }

    /**
     * Gets the clock shadow color.
     *
     * @return the clock shadow color
     */
    Color getClockShadowColor() {

        return this.clockShadowColor;
    }

    /**
     * Gets the clock shadow x offset.
     *
     * @return the clock shadow x offset
     */
    int getClockShadowDx() {

        return this.clockShadowDx;
    }

    /**
     * Gets the clock shadow y offset.
     *
     * @return the clock shadow y offset
     */
    int getClockShadowDy() {

        return this.clockShadowDy;
    }

    /**
     * Gets the clock x position.
     *
     * @return the clock x position
     */
    double getClockX() {

        return this.clockX;
    }

    /**
     * Gets the clock alignment.
     *
     * @return the clock alignment
     */
    int getClockAlignment() {

        return this.clockAlignment;
    }

    /**
     * Gets the clock y position.
     *
     * @return the clock y position
     */
    int getClockY() {

        return this.clockY;
    }

    /**
     * Gets the timer format.
     *
     * @return the timer format
     */
    String getTimerFormat() {

        return this.timerFormat;
    }

    /**
     * Gets the timer font.
     *
     * @return the timer font
     */
    Font getTimerFont() {

        return this.timerFont;
    }

    /**
     * Gets the timer color.
     *
     * @return the timer color
     */
    Color getTimerColor() {

        return this.timerColor;
    }

    /**
     * Gets the timer shadow color.
     *
     * @return the timer shadow color
     */
    Color getTimerShadowColor() {

        return this.timerShadowColor;
    }

    /**
     * Gets the timer shadow x offset.
     *
     * @return the timer shadow x offset
     */
    int getTimerShadowDx() {

        return this.timerShadowDx;
    }

    /**
     * Gets the timer shadow y offset.
     *
     * @return the timer shadow y offset
     */
    int getTimerShadowDy() {

        return this.timerShadowDy;
    }

    /**
     * Gets the timer x coordinate.
     *
     * @return the timer x coordinate
     */
    double getTimerX() {

        return this.timerX;
    }

    /**
     * Gets the timer alignment.
     *
     * @return the timer alignment
     */
    int getTimerAlignment() {

        return this.timerAlignment;
    }

    /**
     * Gets the timer y coordinate.
     *
     * @return the timer y coordinate
     */
    int getTimerY() {

        return this.timerY;
    }

    /**
     * Gets the show sections flag.
     *
     * @return the show sections flag
     */
    boolean isShowSections() {

        return this.showSections;
    }

    /**
     * Gets the section font.
     *
     * @return the section font
     */
    Font getSectionFont() {

        return this.sectionFont;
    }

    /**
     * Gets the section color.
     *
     * @return the section color
     */
    Color getSectionColor() {

        return this.sectionColor;
    }

    /**
     * Gets the offscreen image.
     *
     * @return the offscreen image
     */
    public BufferedImage getOffscreen() {

        return this.offscreen;
    }

    /**
     * Gets the size adjustment.
     *
     * @return the size adjustment
     */
    int getSizeAdjustment() {

        return this.sizeAdjustment;
    }

    /**
     * Sets the index of the section being currently worked on.
     *
     * @param index the index of the active section
     */
    void setSelectedSectionIndex(final int index) {

        if (this.sectionUpdater != null) {
            this.sectionUpdater.setSelected(index);
            SwingUtilities.invokeLater(this.sectionUpdater);
        }
    }

    /**
     * Sets the title for a section.
     *
     * @param index    the index of the section
     * @param theTitle the new title for the section
     */
    void setSectionTitle(final int index, final String theTitle) {

        if ((this.sectionUpdater != null) && (this.exam.getNumSections() >= index)) {
            this.sectionTitles[index] = theTitle;
            this.sectionUpdater.setSelected(index);
            SwingUtilities.invokeLater(this.sectionUpdater);
        }
    }

    /**
     * Enables a section.
     *
     * @param index the index of the section to enable
     */
    void enableSection(final int index) {

        this.exam.getSection(index).enabled = true;

        if (this.sectionUpdater != null) {
            SwingUtilities.invokeLater(this.sectionUpdater);
        }
    }

    /**
     * Draws the component to the screen.
     *
     * @param g the {@code Graphics} to which to draw the component
     */
    @Override
    public void paintComponent(final Graphics g) {
        synchronized (this) {

            if (!SwingUtilities.isEventDispatchThread()) {
                Log.warning(Res.get(Res.NOT_AWT_THREAD));
            }

            final Insets insets = getInsets();

            if (this.offscreen == null) {
                makeOffscreen(insets);
            }

            super.paintComponent(g);
            g.drawImage(this.offscreen, insets.left, insets.top, this);
        }
    }

    /**
     * Keeps the clock and timer updated, and to fire an event at listeners when the timer expires.
     */
    @Override
    public void run() {

        // Find the containing frame
        Container parent = getParent();
        JFrame frame = null;

        while (parent != null) {
            if (parent instanceof JFrame) {
                frame = (JFrame) parent;
                break;
            }
            parent = parent.getParent();
        }

        if (frame == null) {
            return;
        }

        // Wait for the frame to become visible, if it is not yet...
        long now = System.currentTimeMillis();
        long prior = now / 1000L;

        while (!frame.isVisible()) {

            if (System.currentTimeMillis() > (now + 10000L)) {
                break;
            }

            try {
                Thread.sleep(50L);
            } catch (final InterruptedException e) {
                Log.warning(e);
            }
        }

        // While the frame is visible, update the timer/clock displays
        while (frame.isVisible()) {
            now = System.currentTimeMillis();

            if ((this.expiry != 0L) && (now > this.expiry)) {
                this.expiry = 0L;

                for (final ExamContainerInt listener : this.listeners) {
                    listener.timerExpired();
                }
            }

            final long seconds = now / 1000L;

            if (seconds != prior) {
                prior = seconds;

                final boolean ready;

                synchronized (this) {
                    ready = this.offscreen != null;
                }

                if (ready) {
                    SwingUtilities.invokeLater(this.clockUpdater);
                    repaint();
                }
            }

            try {
                Thread.sleep(50L);
            } catch (final InterruptedException e) {
                Log.warning(e);
            }
        }
    }

    /**
     * Registers a new listener to be notified when the timer expires.
     *
     * @param listener the listener to register
     */
    void addTimerListener(final ExamContainerInt listener) {

        this.listeners.add(listener);
    }

    /**
     * Handler for show events.
     *
     * @param e the component show event
     */
    @Override
    public void componentShown(final ComponentEvent e) { /* Empty */

    }

    /**
     * Handler for hide events.
     *
     * @param e the component hide event
     */
    @Override
    public void componentHidden(final ComponentEvent e) { /* Empty */

    }

    /**
     * Handler for move events.
     *
     * @param e the component move event
     */
    @Override
    public void componentMoved(final ComponentEvent e) { /* Empty */

    }

    /**
     * Handler for resize events.
     *
     * @param e the component resize event
     */
    @Override
    public void componentResized(final ComponentEvent e) {
        synchronized (this) {

            makeOffscreen(getInsets());
            repaint();
        }
    }

    /**
     * Make the window render larger, up to some limit.
     */
    void larger() {

        if (this.sizeAdjustment < 5) {
            ++this.sizeAdjustment;
            updateFonts();
        }
    }

    /**
     * Make the window render smaller, down to some limit.
     */
    void smaller() {

        if (this.sizeAdjustment > -3) {
            --this.sizeAdjustment;
            updateFonts();
        }
    }

    /**
     * Updates the fonts for buttons and labels based on an updated size factor.
     */
    private void updateFonts() {

        final float fontFactor = (float) StrictMath.exp((double) this.sizeAdjustment / 4.0);

        this.titleFont = this.titleFont.deriveFont(this.titleFontBaseSize * fontFactor);
        this.usernameFont = this.usernameFont.deriveFont(this.usernameFontBaseSize * fontFactor);
        this.clockFont = this.clockFont.deriveFont(this.clockFontBaseSize * fontFactor);
        this.timerFont = this.timerFont.deriveFont(this.timerFontBaseSize * fontFactor);
        this.sectionFont = this.sectionFont.deriveFont(this.sectionFontBaseSize * fontFactor);

        final int newHeight = computeHeight();

        setPreferredSize(new Dimension(getWidth(), newHeight));
        setSize(new Dimension(getWidth(), newHeight));

        makeOffscreen(getInsets());
        revalidate();
    }
}

/**
 * Configures the top bar panel from within the AWT event thread.
 */
final class TopBarPanelBuilder implements Runnable {

    /** The panel to be configured. */
    private final TopBarPanel panel;

    /** The size of border to add around the panel. */
    private final int border;

    /** The size of inset to add around the panel border. */
    private final int inset;

    /** Color for the border line. */
    private final Color color;

    /**
     * Constructs a new {@code TopBarPanelBuilder}.
     *
     * @param thePanel  the panel to be configured
     * @param theBorder the size of border to add around the panel
     * @param theInset  distance to inset the line border from the edge
     * @param theColor  color for the border outline
     */
    TopBarPanelBuilder(final TopBarPanel thePanel, final int theBorder, final int theInset, final Color theColor) {

        this.panel = thePanel;
        this.border = theBorder;
        this.inset = theInset;
        this.color = theColor;
    }

    /**
     * Performs the updates in the AWT event thread.
     */
    @Override
    public void run() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        this.panel.setBackground(this.panel.getBackgroundColor());

        if (this.inset > 0) {

            if (this.border > 0) {
                this.panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(this.inset, this.inset, this.inset, this.inset),
                        BorderFactory.createLineBorder(this.color, this.border)));
            } else {
                this.panel.setBorder(BorderFactory.createEmptyBorder(this.inset, this.inset, this.inset, this.inset));
            }
        } else if (this.border > 0) {
            this.panel.setBorder(BorderFactory.createLineBorder(this.color, this.border));
        }

        this.panel.setPreferredSize(new Dimension(0, this.panel.computeHeight()));
        this.panel.addComponentListener(this.panel);
    }
}

/**
 * A class that can be run from the AWT event thread to update the section values.
 */
final class TopBarPanelSectionUpdater implements Runnable {

    /** The offscreen back-buffer to which to draw. */
    private final TopBarPanel panel;

    /** The rectangle in which to draw the sections list. */
    private Rectangle rect;

    /** Section titles array. */
    private final String[] titles;

    /** Color for selected items. */
    private final Color colorSelected;

    /** Color for enabled items. */
    private final Color colorEnabled;

    /** Color for disabled items. */
    private final Color colorDisabled;

    /** Color for highlighting the selected. */
    private final Color colorHighlight;

    /** The index of the selected section. */
    private int selected;

    /**
     * Constructs a new {@code TopBarPanelSectionUpdater}.
     *
     * @param thePanel  the owning top bar panel
     * @param theRect   the rectangle in which the sections list is drawn
     * @param theTitles array of section titles as they should appear
     */
    TopBarPanelSectionUpdater(final TopBarPanel thePanel, final Rectangle theRect, final String[] theTitles) {

        this.panel = thePanel;
        this.rect = theRect;
        this.titles = theTitles;

        this.colorSelected = this.panel.getSectionColor();

        final int r = this.colorSelected.getRed();
        final int g = this.colorSelected.getGreen();
        final int b = this.colorSelected.getBlue();

        final Color bg = this.panel.getBackgroundColor();
        this.colorEnabled = this.colorSelected;
        this.colorDisabled = new Color((bg.getRed() + r) / 2, (bg.getGreen() + g) / 2, (bg.getBlue() + b) / 2);

        this.colorHighlight = new Color(255 - r, 255 - g, 255 - b);
    }

    /**
     * Sets the index of the currently selected section.
     *
     * @param index the selected section index
     */
    public void setSelected(final int index) {

        this.selected = index;
    }

    /**
     * Sets the rectangle in which to draw sections.
     *
     * @param theRect the rectangle in which to draw
     */
    public void setRect(final Rectangle theRect) {

        this.rect = theRect;
    }

    /**
     * Updates the section information in the AWT event thread.
     */
    @Override
    public void run() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        synchronized (this.panel) {

            // If not configured to show sections, return.
            if (this.panel.isShowSections()) {

                // If no exam to display, don't waste time.
                final ExamObj exam = this.panel.getExam();

                if (exam != null) {

                    // Blank the sections region
                    final Graphics2D g2d = (Graphics2D) (this.panel.getOffscreen().getGraphics());
                    g2d.setColor(this.panel.getBackgroundColor());
                    g2d.fillRect(this.rect.x, this.rect.y, this.rect.width, this.rect.height);

                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                    g2d.setFont(this.panel.getSectionFont());

                    final FontMetrics fm = g2d.getFontMetrics();

                    int y = this.rect.y + fm.getAscent();

                    final int numSect = exam.getNumSections();
                    for (int i = 0; i < numSect; i++) {

                        final ExamSection sect = exam.getSection(i);
                        String name = this.titles[i];
                        final Color color;

                        if (i == this.selected) {
                            name = "\u25ba " + name;

                            g2d.setColor(this.colorHighlight);
                            g2d.fillRect(this.rect.x, y - fm.getAscent(), fm.stringWidth(name) + 2,
                                    fm.getAscent() + fm.getDescent());

                            color = this.colorSelected;
                        } else if (sect.enabled) {
                            color = this.colorEnabled;
                        } else {
                            color = this.colorDisabled;
                        }

                        g2d.setColor(color);
                        g2d.drawString(name, this.rect.x, y);

                        y += fm.getHeight();
                    }

                    this.panel.repaint();
                }
            }
        }
    }
}

/**
 * A class that can be run from the AWT event thread to update the clock and timer displays.
 */
final class TopBarPanelClockUpdater implements Runnable {

    /** Color to use to flash warning. */
    private static final Color WARN_COLOR = Color.yellow;

    /** Color to use to flash urgent warning. */
    private static final Color URGENT_COLOR = new Color(255, 110, 150);

    /** The offscreen back-buffer to which to draw. */
    private final TopBarPanel panel;

    /**
     * Constructs a new {@code TopBarPanelClockUpdater}.
     *
     * @param thePanel the owning top bar panel
     */
    TopBarPanelClockUpdater(final TopBarPanel thePanel) {

        this.panel = thePanel;
    }

    /**
     * Performs the clock update in the AWT event thread..
     */
    @Override
    public void run() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        if ((this.panel == null) || (this.panel.getOffscreen() == null)) {
            return;
        }

        // Blank the clock and timer regions
        synchronized (this.panel) {

            final int width = this.panel.getOffscreen().getWidth();

            final Graphics2D g2d = (Graphics2D) (this.panel.getOffscreen().getGraphics());
            g2d.setColor(this.panel.getBackgroundColor());

            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            final float scale = (float) Math.pow(2.5, (double) this.panel.getSizeAdjustment() / 4.0);

            if (this.panel.getClockFormat() != null) {
                final LocalTime now = LocalTime.now();

                final String txt = TopBarPanel.makeTime(this.panel.getClockFormat(), now.getHour() % 12,
                                now.getMinute(), now.getSecond(), false);
                this.panel.drawField(g2d, txt, width, this.panel.getClockX(),
                        (int) ((float) this.panel.getClockY() * scale), this.panel.getClockFont(),
                        this.panel.getClockAlignment(), this.panel.getClockShadowDx(),
                        this.panel.getClockShadowDy(), this.panel.getClockColor(),
                        this.panel.getClockShadowColor(), this.panel.getBackgroundColor(), null, true);
            }

            if (this.panel.getTimerFormat() != null) {
                int remain = (int) (this.panel.getExpiry() - System.currentTimeMillis()) / 1000;

                if (remain < 0) {
                    remain = 0;
                }

                // Make the background flash when 60 seconds or less remains
                final Color hilite;

                if (remain <= 20) {
                    hilite = ((remain & 0x01) == 1) ? URGENT_COLOR : null;
                } else if (remain <= 60) {
                    hilite = ((remain & 0x01) == 1) ? WARN_COLOR : null;
                } else {
                    hilite = null;
                }

                final String txt = TopBarPanel.makeTime(this.panel.getTimerFormat(), remain / 3600,
                        (remain % 3600) / 60, remain % 60, true);
                this.panel.drawField(g2d, txt, width, this.panel.getTimerX(),
                        (int) ((float) this.panel.getTimerY() * scale), this.panel.getTimerFont(),
                        this.panel.getTimerAlignment(), this.panel.getTimerShadowDx(),
                        this.panel.getTimerShadowDy(), this.panel.getTimerColor(),
                        this.panel.getTimerShadowColor(), this.panel.getBackgroundColor(), hilite,
                        true);
            }
        }
    }
}
