package dev.mathops.app.exam;

import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.assessment.exam.ExamProblem;
import dev.mathops.assessment.exam.ExamSection;
import dev.mathops.assessment.exam.ExamSession;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.core.log.Log;
import dev.mathops.core.ui.ColorNames;
import dev.mathops.font.BundledFontManager;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.io.Serial;

/**
 * A panel to present a list of questions, with section headings.
 * <p>
 * This panel interacts with the "ExamObj" data model. It sends events to that data model when the user selects a new
 * problem; it can query section and problems from the model, as well as a flag that determines whether correctness
 * marks should be added to problems.
 */
class ProblemListPanel extends JPanel implements MouseListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 6428785750280514422L;

    /** The exam session. */
    private final ExamSession examSession;

    /** Insets. */
    private final Insets margins = new Insets(10, 10, 5, 10);

    /** Spacing between sections. */
    private final int sectionSpacing = 5;

    /** Spacing between questions. */
    private final int questionSpacing = 1;

    /** Background color. */
    private final Color backgroundColor;

    /** Highlight color. */
    private final Color highlightColor;

    /** Color for check marks. */
    private final Color checkmarkColor;

    /** Color for right answers. */
    private final Color rightColor;

    /** Color for wrong answers. */
    private final Color wrongColor;

    /** Color for section labels. */
    private final Color sectionColor;

    /** Color for question labels. */
    private final Color questionColor;

    /** Base size for the font for section labels. */
    private final float sectionFontBaseSize;

    /** Font for section labels. */
    private Font sectionFont;

    /** Base size for the font for question labels. */
    private final float questionFontBaseSize;

    /** Font for question labels. */
    private Font questionFont;

    /** Base size for the font for arrow. */
    private final float arrowFontBaseSize;

    /** Font for arrow. */
    private Font arrowFont;

    /** Base size for the font for checkmark. */
    private final float checkmarkFontBaseSize;

    /** Font for checkmark. */
    private Font checkmarkFont;

    /** Base size for the font for right answers. */
    private final float rightFontBaseSize;

    /** The font for right answers. */
    private Font rightFont;

    /** Base size for the font for wrong answers. */
    private final float wrongFontBaseSize;

    /** The font for wrong answers. */
    private Font wrongFont;

    /** Arrow string. */
    private final String arrowString;

    /** Checkmark string. */
    private final String checkString;

    /** String for right answers. */
    private final String rightString;

    /** String for wrong answers. */
    private final String wrongString;

    /** Owning exam panel. */
    private final ExamPanelInt owner;

    /** Off-screen image. */
    private Image offscreen;

    /** Maximum panel width. */
    private final int maxWidth;

    /** Panel width. */
    private int width;

    /** Panel height. */
    private int height;

    /** Laid out flag. */
    private boolean laidOut;

    /** Flag indicating repaint is needed. */
    private boolean dirty;

    /**
     * The scale factor to apply to all labels (the first index is the section index, then within that array, the [0]
     * element is the section title and the remaining elements are the problem names for the section).
     */
    private AffineTransform[][] labelScales;

    /**
     * The bounding boxes of all labels in the question panel (indexing is as described for {@code labelScales}).
     */
    private Rectangle[][] labelBounds;

    /** The section the user is clicking on. */
    private int clickSection = -1;

    /** The problem the user is clicking on. */
    private int clickProblem = -1;

    /** The size adjustment. */
    private int size;

    /**
     * Constructs a new {@code ProblemListPanel}.
     *
     * @param theOwner       the test application that owns this panel
     * @param theExamSession the exam session
     * @param theBfm         the font manager used to obtain fonts
     * @param theWidth       the width to make the panel
     */
    ProblemListPanel(final ExamPanelInt theOwner, final ExamSession theExamSession,
                     final BundledFontManager theBfm, final int theWidth) {
        super();

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        this.owner = theOwner;
        this.examSession = theExamSession;

        // Load skin settings
        this.backgroundColor = ColorNames.getColor("alice blue");
        this.highlightColor = ColorNames.getColor("light green");
        this.sectionColor = ColorNames.getColor("SteelBlue4");
        this.questionColor = ColorNames.getColor("black");
        this.checkmarkColor = ColorNames.getColor("dark green");
        this.rightColor = ColorNames.getColor("dark green");
        this.wrongColor = ColorNames.getColor("dark red");

        final Color outlineColor = ColorNames.getColor("steel blue");

        this.sectionFontBaseSize = 22.0f;
        this.questionFontBaseSize = 18.0f;
        this.arrowFontBaseSize = 18.0f;
        this.checkmarkFontBaseSize = 18.0f;
        this.rightFontBaseSize = 15.0f;
        this.wrongFontBaseSize = 15.0f;

        this.sectionFont = theBfm.getFont(BundledFontManager.SANS, (double) this.sectionFontBaseSize, Font.PLAIN);
        this.questionFont = theBfm.getFont(BundledFontManager.SERIF, (double) this.questionFontBaseSize, Font.PLAIN);
        this.arrowFont = theBfm.getFont("ESSTIXOne", (double) this.arrowFontBaseSize, Font.PLAIN);
        this.checkmarkFont = theBfm.getFont("ESSTIXTwo", (double) this.checkmarkFontBaseSize, Font.PLAIN);
        this.rightFont = theBfm.getFont("Martin_Vogels_Symbole", (double) this.rightFontBaseSize, Font.PLAIN);
        this.wrongFont = theBfm.getFont("Martin_Vogels_Symbole", (double) this.wrongFontBaseSize, Font.PLAIN);

        this.arrowString = "\u003c";
        this.checkString = "\u0023 ";
        this.rightString = "\u0056 ";
        this.wrongString = "\u0058 ";

        setBackground(this.backgroundColor);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1),
                BorderFactory.createLineBorder(outlineColor)));

        this.maxWidth = theWidth / 5;

        Dimension prefSize = computeSize();
        final Insets insets = getInsets();
        prefSize = new Dimension(prefSize.width + insets.left + insets.right,
                prefSize.height + insets.top + insets.bottom);
        setPreferredSize(prefSize);

        addMouseListener(this);
    }

    /**
     * Refreshes the display, rebuilding the offscreen image.
     */
    final void refresh() {

        this.dirty = true;
        repaint();
    }

    /**
     * Determines the preferred height of the problem list on the displayed data.
     *
     * @return the preferred size
     */
    private Dimension computeSize() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        Graphics grx = getGraphics();

        if (grx == null) {

            // We're not installed in a window yet, so build a Graphics
            final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            final GraphicsDevice[] gs = ge.getScreenDevices();

            if (gs.length == 0) {
                return new Dimension(this.maxWidth, 1);
            }

            final GraphicsConfiguration gc = gs[0].getDefaultConfiguration();
            grx = gc.createCompatibleImage(1, 1).getGraphics();
        }

        final FontMetrics sectionNameMetrics = grx.getFontMetrics(this.sectionFont);
        final FontMetrics questionNameMetrics = grx.getFontMetrics(this.questionFont);
        final FontMetrics checkmarkMetrics = grx.getFontMetrics(this.checkmarkFont);
        final int questionIndent = checkmarkMetrics.stringWidth(this.checkString);

        int theHeight = this.margins.top + this.margins.bottom;

        int maxSectionWidth = 0;
        int maxQuestionWidth = 0;

        final ExamObj exam = this.examSession.getExam();

        for (int i = 0; i < exam.getNumSections(); i++) {
            final ExamSection section = exam.getSection(i);
            int strWidth = sectionNameMetrics.stringWidth(section.sectionName) + this.margins.left
                    + this.margins.right;

            if (strWidth > maxSectionWidth) {
                maxSectionWidth = strWidth;
            }

            theHeight += sectionNameMetrics.getHeight() + this.questionSpacing;

            for (int j = 0; j < section.getNumProblems(); j++) {
                final ExamProblem problem = section.getProblem(j);
                strWidth = questionNameMetrics.stringWidth(problem.problemName) + this.margins.left
                        + this.margins.right + questionIndent;

                if (strWidth > maxQuestionWidth) {
                    maxQuestionWidth = strWidth;
                }

                theHeight += questionNameMetrics.getHeight() + this.questionSpacing;
            }

            theHeight += this.sectionSpacing;
        }

        // If section names will overflow width, scale them, or if not, do
        // not store any AffineTransform for the section name
        int max = this.maxWidth - (this.margins.left + this.margins.right);

        if (maxSectionWidth > max) {
            maxSectionWidth = max;
        }

        max = this.maxWidth - (this.margins.left + this.margins.right + questionIndent);

        if (maxQuestionWidth > max) {
            maxQuestionWidth = max;
        }

        maxSectionWidth += this.margins.left + this.margins.right;
        maxQuestionWidth += this.margins.left + this.margins.right + questionIndent;

        final int theWidth = Math.max(maxSectionWidth, maxQuestionWidth);

        return new Dimension(theWidth, theHeight);
    }

    /**
     * Builds the offscreen bitmap to accelerate painting.
     *
     * @param grx the {@code Graphics} the offscreen image should match
     */
    private void makeOffscreen(final Graphics grx) {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        if (grx != null && this.examSession != null) {

            final FontMetrics sectionNameMetrics = grx.getFontMetrics(this.sectionFont);
            final FontMetrics questionNameMetrics = grx.getFontMetrics(this.questionFont);
            final FontMetrics arrowFontMetrics = grx.getFontMetrics(this.arrowFont);
            final FontMetrics checkmarkMetrics = grx.getFontMetrics(this.checkmarkFont);
            final int questionIndent = checkmarkMetrics.stringWidth(this.checkString);

            final ExamObj exam = this.examSession.getExam();

            if (!this.laidOut) {

                // Compute the size of the panel using font sizes and section data
                this.height = this.margins.top + this.margins.bottom;

                int maxSectionWidth = 0;
                int maxQuestionWidth = 0;

                for (int i = 0; i < exam.getNumSections(); i++) {
                    final ExamSection section = exam.getSection(i);

                    int strWidth = sectionNameMetrics.stringWidth(section.sectionName)
                            + this.margins.left + this.margins.right;

                    if (strWidth > maxSectionWidth) {
                        maxSectionWidth = strWidth;
                    }

                    this.height += sectionNameMetrics.getHeight() + this.questionSpacing;

                    for (int j = 0; j < section.getNumProblems(); j++) {
                        final ExamProblem problem = section.getProblem(j);

                        strWidth = questionNameMetrics.stringWidth(problem.problemName)
                                + this.margins.left + this.margins.right + questionIndent;

                        if (strWidth > maxQuestionWidth) {
                            maxQuestionWidth = strWidth;
                        }

                        this.height += questionNameMetrics.getHeight() + this.questionSpacing;
                    }

                    this.height += this.sectionSpacing;
                }

                // Allocate buffers for label scale factors and bounds.
                this.labelScales = new AffineTransform[exam.getNumSections()][];
                this.labelBounds = new Rectangle[exam.getNumSections()][];

                for (int i = 0; i < exam.getNumSections(); i++) {
                    final ExamSection section = exam.getSection(i);
                    this.labelScales[i] = new AffineTransform[1 + section.getNumProblems()];
                    this.labelBounds[i] = new Rectangle[1 + section.getNumProblems()];
                }

                // If section names will overflow width, scale them, or if not, do
                // not store any AffineTransform for the section name
                int max = this.maxWidth - (this.margins.left + this.margins.right);

                final double sectionXScale;

                if (maxSectionWidth > max) {
                    sectionXScale = (double) max / (double) maxSectionWidth;

                    final AffineTransform sectionXform =
                            AffineTransform.getScaleInstance(sectionXScale, 1.0);
                    maxSectionWidth = max;

                    for (int i = 0; i < exam.getNumSections(); i++) {
                        this.labelScales[i][0] = sectionXform;
                    }
                }

                // If question names will overflow width, scale them, or if not, do
                // not store any AffineTransform for the question name
                max = this.maxWidth - (this.margins.left + this.margins.right + questionIndent);

                final double questionXScale;

                if (maxQuestionWidth > max) {
                    questionXScale = (double) max / (double) maxQuestionWidth;

                    final AffineTransform questionXform =
                            AffineTransform.getScaleInstance(questionXScale, 1.0);
                    maxQuestionWidth = max;

                    for (int i = 0; i < exam.getNumSections(); i++) {
                        final ExamSection section = exam.getSection(i);

                        for (int j = 0; j < section.getNumProblems(); j++) {
                            this.labelScales[i][1 + j] = questionXform;
                        }
                    }
                }

                maxSectionWidth += this.margins.left + this.margins.right;
                maxQuestionWidth += this.margins.left + this.margins.right + questionIndent;
                this.width = Math.max(maxSectionWidth, maxQuestionWidth);

                this.laidOut = true;

                if ((this.width <= 0) || (this.height <= 0)) {
                    return;
                }

                // Now that we have the size, build the offscreen image
                this.offscreen = createImage(this.width, this.height);

                final Insets insets = getInsets();
                final Dimension overall = new Dimension(this.width + insets.left + insets.right,
                        this.height + insets.top + insets.bottom);
                setPreferredSize(overall);
                setSize(getPreferredSize());

                // Compute bounds boxes of all components
                int y = this.margins.top;

                for (int i = 0; i < exam.getNumSections(); i++) {
                    final ExamSection section = exam.getSection(i);

                    Rectangle rect = new Rectangle();
                    rect.x = this.margins.left;
                    rect.y = y;
                    rect.width = this.width - (this.margins.left + this.margins.right);
                    rect.height = sectionNameMetrics.getAscent() + sectionNameMetrics.getDescent();
                    this.labelBounds[i][0] = rect;

                    y += sectionNameMetrics.getHeight() + this.questionSpacing;

                    for (int j = 0; j < section.getNumProblems(); j++) {
                        rect = new Rectangle();
                        rect.x = this.margins.left;
                        rect.y = y;
                        rect.width = this.width - (this.margins.left + this.margins.right);
                        rect.height =
                                sectionNameMetrics.getAscent() + sectionNameMetrics.getDescent();
                        this.labelBounds[i][1 + j] = rect;

                        y += questionNameMetrics.getHeight() + this.questionSpacing;
                    }

                    y += this.sectionSpacing;
                }
            }

            final Graphics2D g2d = (Graphics2D) (this.offscreen.getGraphics());
            final AffineTransform originalXform = g2d.getTransform();

            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2d.setColor(this.backgroundColor);
            g2d.fillRect(0, 0, this.width, this.height);

            // Draw the contents
            for (int i = 0; i < exam.getNumSections(); i++) {
                final ExamSection section = exam.getSection(i);
                g2d.setFont(this.sectionFont);
                g2d.setColor(this.sectionColor);

                Rectangle rect = this.labelBounds[i][0];

                int spaceAboveLabel =
                        (rect.height - sectionNameMetrics.getAscent() - sectionNameMetrics.getDescent())
                                / 2;

                if (this.labelScales[i][0] != null) {
                    g2d.setTransform(this.labelScales[i][0]);
                    g2d.drawString(section.sectionName,
                            (int) ((double) this.margins.left / this.labelScales[i][0].getScaleX()),
                            rect.y + spaceAboveLabel + sectionNameMetrics.getAscent());
                    g2d.setTransform(originalXform);
                } else {
                    g2d.drawString(section.sectionName, this.margins.left,
                            rect.y + spaceAboveLabel + sectionNameMetrics.getAscent());
                }

                for (int j = 0; j < section.getNumProblems(); j++) {
                    final ExamProblem problem = section.getProblem(j);
                    g2d.setFont(this.questionFont);
                    rect = this.labelBounds[i][1 + j];

                    spaceAboveLabel = (rect.height - questionNameMetrics.getAscent()
                            - questionNameMetrics.getDescent()) / 2;

                    // Draw the highlight and arrow if the question is current
                    if (exam.isCurrentProblem(i, j)) {
                        g2d.setColor(this.highlightColor);
                        g2d.fillRect(rect.x, rect.y, rect.width, rect.height);

                        g2d.setFont(this.arrowFont);
                        g2d.setColor(this.questionColor);
                        g2d.drawString(
                                this.arrowString, rect.x + rect.width
                                        - arrowFontMetrics.stringWidth(this.arrowString) - 4,
                                rect.y + spaceAboveLabel + questionNameMetrics.getAscent());
                    }

                    // Draw the checkmark if question has been answered
                    final AbstractProblemTemplate selected = problem.getSelectedProblem();

                    final boolean showCorrect = this.examSession.getState().showCorrectness;

                    if (selected.isAnswered()) {

                        if (showCorrect) {

                            if (selected.isCorrect(selected.getAnswer())) {
                                g2d.setFont(this.rightFont);
                                g2d.setColor(this.rightColor);
                                g2d.drawString(this.rightString, rect.x + 1,
                                        2 + rect.y + spaceAboveLabel + questionNameMetrics.getAscent());
                            } else {
                                g2d.setFont(this.wrongFont);
                                g2d.setColor(this.wrongColor);
                                g2d.drawString(this.wrongString, rect.x + 1,
                                        2 + rect.y + spaceAboveLabel + questionNameMetrics.getAscent());
                            }
                        } else {
                            g2d.setFont(this.checkmarkFont);
                            g2d.setColor(this.checkmarkColor);
                            g2d.drawString(this.checkString, rect.x + 2,
                                    rect.y + spaceAboveLabel + questionNameMetrics.getAscent());
                        }
                    } else if (showCorrect) {

                        // Mark unanswered as incorrect
                        g2d.setFont(this.wrongFont);
                        g2d.setColor(this.wrongColor);
                        g2d.drawString(this.wrongString, rect.x + 1,
                                2 + rect.y + spaceAboveLabel + questionNameMetrics.getAscent());
                    }

                    g2d.setFont(this.questionFont);
                    g2d.setColor(this.questionColor);

                    if (this.labelScales[i][1 + j] != null) {
                        g2d.setTransform(this.labelScales[i][1 + j]);
                        g2d.drawString(problem.problemName,
                                (int) ((double) (this.margins.left + questionIndent)
                                        / this.labelScales[i][1 + j].getScaleX()),
                                rect.y + spaceAboveLabel + questionNameMetrics.getAscent());
                        g2d.setTransform(originalXform);
                    } else {
                        g2d.drawString(problem.problemName, this.margins.left + questionIndent,
                                rect.y + spaceAboveLabel + questionNameMetrics.getAscent());
                    }
                }
            }

            this.owner.revalidate();
        }
    }

    /**
     * Draws the component to the screen.
     *
     * @param g the {@code Graphics} to which to draw
     */
    @Override
    public void paintComponent(final Graphics g) {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        super.paintComponent(g);

        if (this.offscreen == null || this.dirty) {
            makeOffscreen(g);
        }

        g.setColor(this.backgroundColor);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (this.offscreen != null) {
            g.drawImage(this.offscreen, 0, 0, this);
        }
    }

    /**
     * Invoked when the mouse button has been clicked (pressed and released) on a component.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseClicked(final MouseEvent e) {

        // No action
    }

    /**
     * Handler for mouse press events.
     *
     * @param e the mouse event to be processed
     */
    @Override
    public void mousePressed(final MouseEvent e) {

        if (this.examSession != null) {
            this.clickSection = -1;
            this.clickProblem = -1;

            // See if the click was in a question

            final int x = e.getX();
            final int y = e.getY();

            final ExamObj exam = this.examSession.getExam();

            for (int i = 0; i < exam.getNumSections(); i++) {
                final ExamSection section = exam.getSection(i);

                for (int j = 0; j < section.getNumProblems(); j++) {
                    final Rectangle rect = this.labelBounds[i][1 + j];

                    if (rect.contains(x, y)) {
                        this.clickSection = i;
                        this.clickProblem = j;

                        break;
                    }
                }
            }
        }
    }

    /**
     * Handler for mouse released events.
     *
     * @param e the mouse event to be processed
     */
    @Override
    public void mouseReleased(final MouseEvent e) {

        if (this.examSession != null) {

            // See if the click was in a question
            final int x = e.getX();
            final int y = e.getY();

            final ExamObj exam = this.examSession.getExam();

            for (int i = 0; i < exam.getNumSections(); i++) {
                final ExamSection section = exam.getSection(i);

                for (int j = 0; j < section.getNumProblems(); j++) {
                    final Rectangle rect = this.labelBounds[i][1 + j];

                    if (rect.contains(x, y)) {

                        if ((i == this.clickSection) && (j == this.clickProblem)) {
                            this.owner.pickProblem(i, j);

                            break;
                        }
                    }
                }
            }

            this.clickSection = -1;
            this.clickProblem = -1;
        }
    }

    /**
     * Invoked when the mouse enters a component.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseEntered(final MouseEvent e) {

        // No action
    }

    /**
     * Handler for mouse exited events.
     *
     * @param e the mouse event to be processed
     */
    @Override
    public void mouseExited(final MouseEvent e) {

        this.clickSection = -1;
        this.clickProblem = -1;
    }

    /**
     * Sets the question that is to be displayed in the panel.
     *
     * @param sectionIndex the index of the section
     * @param problemIndex the index of the problem
     */
    final void setCurrentProblem(final int sectionIndex, final int problemIndex) {

        if (this.examSession != null) {
            final ExamObj exam = this.examSession.getExam();

            exam.setCurrentProblem(Integer.valueOf(sectionIndex), Integer.valueOf(problemIndex));
            this.dirty = true;
            repaint();
        }
    }

    /**
     * Make the window render larger, up to some limit.
     */
    final void larger() {

        if (this.size < 5) {
            ++this.size;
            updateFonts();
        }
    }

    /**
     * Make the window render smaller, down to some limit.
     */
    final void smaller() {

        if (this.size > -3) {
            --this.size;
            updateFonts();
        }
    }

    /**
     * Updates the fonts for buttons and labels based on an updated size factor.
     */
    private void updateFonts() {

        final float fontFactor = (float) StrictMath.pow(2.5, (double) this.size / 4.0);

        this.sectionFont = this.sectionFont.deriveFont(this.sectionFontBaseSize * fontFactor);
        this.questionFont = this.questionFont.deriveFont(this.questionFontBaseSize * fontFactor);
        this.arrowFont = this.arrowFont.deriveFont(this.arrowFontBaseSize * fontFactor);
        this.checkmarkFont = this.checkmarkFont.deriveFont(this.checkmarkFontBaseSize * fontFactor);
        this.rightFont = this.rightFont.deriveFont(this.rightFontBaseSize * fontFactor);
        this.wrongFont = this.wrongFont.deriveFont(this.wrongFontBaseSize * fontFactor);

        this.laidOut = false;
        refresh();
    }
}
