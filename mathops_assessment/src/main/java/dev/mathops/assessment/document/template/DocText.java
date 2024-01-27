package dev.mathops.assessment.document.template;

import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.inst.DocObjectInstStyle;
import dev.mathops.assessment.document.inst.DocTextInst;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.EqualityTests;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.ColorNames;
import dev.mathops.font.BundledFontManager;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.GlyphVector;
import java.io.File;
import java.io.PrintStream;
import java.io.Serial;
import java.util.Objects;
import java.util.Set;

/**
 * An item of text in a paragraph.
 */
public final class DocText extends AbstractDocObjectTemplate {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -4775680075445809973L;

    /** The text contents of the object. */
    private String text;

    /** Flag indicating glyph should come from STIX Text. */
    private boolean isStixText;

    /** Flag indicating glyph should come from STIX Math. */
    private boolean isStixMath;

    /**
     * Construct a new {@code DocText} object with an initial text value.
     *
     * @param theText the initial text
     */
    public DocText(final String theText) {

        super();

        setText(theText);
    }

    /**
     * Perform a deep clone of the object, cloning all contained sub-objects.
     *
     * @return the cloned object
     */
    @Override
    public DocText deepCopy() {

        final DocText copy = new DocText(this.text);

        copy.copyObjectFrom(this);

        return copy;
    }

    /**
     * Get the text.
     *
     * @return the text
     */
    public String getText() {

        return this.text;
    }

    /**
     * Set the text.
     *
     * @param theText the new text
     */
    private void setText(final String theText) {

        this.text = theText;

        if (theText.length() == 1) {
            final char ch = theText.charAt(0);

            this.isStixText = ch == '\u03C0' || ch == '\u03D1' || ch == '\u03D5' || ch == '\u03D6'
                    || ch == '\u03F0' || ch == '\u03F1' || ch == '\u03F5' || ch == '\u2034'
                    || ch == '\u2057';

            this.isStixMath = ch == '\u2218' || ch == '\u221D' || ch == '\u2220' || ch == '\u2221'
                    || ch == '\u2229' || ch == '\u222A' || ch == '\u2243' || ch == '\u2266'
                    || ch == '\u2267' || ch == '\u2268' || ch == '\u2269' || ch == '\u226A'
                    || ch == '\u226B' || ch == '\u226C' || ch == '\u226E' || ch == '\u226F'
                    || ch == '\u2270' || ch == '\u2271' || ch == '\u2272' || ch == '\u2273'
                    || ch == '\u2276' || ch == '\u2277' || ch == '\u227A' || ch == '\u227B'
                    || ch == '\u227C' || ch == '\u227D' || ch == '\u227E' || ch == '\u227F'
                    || ch == '\u2280' || ch == '\u2281' || ch == '\u22D6' || ch == '\u22D7'
                    || ch == '\u22DA' || ch == '\u22DB' || ch == '\u22DE' || ch == '\u22DF'
                    || ch == '\u22E0' || ch == '\u22E1' || ch == '\u22E6' || ch == '\u22E7'
                    || ch == '\u22E8' || ch == '\u22E9' || ch == '\u22EF' || ch == '\u2322'
                    || ch == '\u2323' || ch == '\u2329' || ch == '\u232A' || ch == '\u25B3'
                    || ch == '\u2713' || ch == '\u27CB' || ch == '\u27CD' || ch == '\u27F8'
                    || ch == '\u27F9' || ch == '\u2A7D' || ch == '\u2A7E' || ch == '\u2A85'
                    || ch == '\u2A86' || ch == '\u2A87' || ch == '\u2A88' || ch == '\u2A89'
                    || ch == '\u2A8A' || ch == '\u2A8B' || ch == '\u2A8C' || ch == '\u2A95'
                    || ch == '\u2A96' || ch == '\u2AA1' || ch == '\u2AA2' || ch == '\u2AAF'
                    || ch == '\u2AB0' || ch == '\u2AB5' || ch == '\u2AB6' || ch == '\u2AB7'
                    || ch == '\u2AB8' || ch == '\u2AB9' || ch == '\u2ABA' || ch == '\u2ADB';

        } else {
            this.isStixText = false;
            this.isStixMath = false;
        }
    }

    /**
     * Get the left alignment for the object.
     *
     * @return the object insets
     */
    @Override
    public int getLeftAlign() {

        return BASELINE;
    }

    /**
     * Recompute the size of the object's bounding box.
     *
     * @param context  the evaluation context
     * @param mathMode text mode, inline-math mode, or display-math mode
     */
    @Override
    public void doLayout(final EvalContext context, final ELayoutMode mathMode) {

        String txt = this.text;

        final BundledFontManager bfm = BundledFontManager.getInstance();
        Font font = getFont();

        if (this.isStixText) {
            font = bfm.getFont("STIX Two Text Regular", (double) font.getSize(), font.getStyle());
        } else if (this.isStixMath) {
            font = bfm.getFont("STIX Two Math Regular", (double) font.getSize(), font.getStyle());
        } else if (mathMode != ELayoutMode.TEXT) {
            if (CoreConstants.DASH.equals(this.text)) {
                txt = "\u2013";
            } else if ("'".equals(this.text)) {
                txt = "\u2032";
            } else if ("''".equals(this.text)) {
                txt = "\u2033";
            } else if ("'''".equals(this.text)) {
                txt = "\u2034";
                font = bfm.getFont("STIX Two Text Regular", (double) font.getSize(), font.getStyle());
            } else if ("''''".equals(this.text)) {
                txt = "\u2057";
                font = bfm.getFont("STIX Two Text Regular", (double) font.getSize(), font.getStyle());
            } else if (txt.length() == 1) {
                final char ch = txt.charAt(0);

                if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '\u03b1' && ch <= '\u03f5')) {
                    font = font.deriveFont(Font.ITALIC);
                }
            } else {
                // Print any extended run of text in italics
                font = font.deriveFont(Font.ITALIC);
            }
        }

        FontMetrics fm = bfm.getFontMetrics(font);
        final GlyphVector gv = font.createGlyphVector(fm.getFontRenderContext(), "My");
        final int asc = (int) Math.round(-gv.getGlyphOutline(0).getBounds2D().getMinY() * 1.2);
        int w;

        if (this.text == null) {
            w = 0;
            setBaseLine(0);
            setCenterLine(0);
        } else {
            if (txt.length() == 1) {
                final char ch = txt.charAt(0);

                if (ch == '\u2147' || ch == '\u2148') {
                    font = font.deriveFont(Font.ITALIC);
                    fm = bfm.getFontMetrics(font);

                    if (ch == '\u2147') {
                        w = fm.stringWidth("e");
                    } else {
                        w = fm.stringWidth("i");
                    }
                } else {
                    w = fm.stringWidth(this.text);
                }
            } else {
                w = fm.stringWidth(txt);
            }

            setBaseLine(asc);
            setCenterLine((asc << 1) / 3);
        }

        if (isBoxed()) {
            w += 4; // Allow 2 pixels on either end for box
        }

        setWidth(w);

        final int desc = (int) Math.round(gv.getGlyphOutline(1).getBounds2D().getMaxY() * 1.2);
        final int h = asc + desc;
        setHeight(h);
    }

    /**
     * Draw the object.
     *
     * @param grx the {@code Graphics} to draw to
     */
    @Override
    public void paintComponent(final Graphics grx, final ELayoutMode mathMode) {

        prePaint(grx);

        innerPaintComponent(grx);

        Graphics2D g2d = null;
        Object origHints = null;
        if (grx instanceof Graphics2D) {
            g2d = (Graphics2D) grx;
            origHints = g2d.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }

        final BundledFontManager bfm = BundledFontManager.getInstance();
        Font font = getFont();

        String txt = this.text;
        while (txt.endsWith(CoreConstants.SPC)) {
            txt = txt.substring(0, txt.length() - 1);
        }

        if (this.isStixText) {
            font = bfm.getFont("STIX Two Text Regular", (double) font.getSize(), font.getStyle());
        } else if (this.isStixMath) {
            font = bfm.getFont("STIX Two Math Regular", (double) font.getSize(), font.getStyle());
        } else if (mathMode != ELayoutMode.TEXT) {
            if (CoreConstants.DASH.equals(this.text)) {
                txt = "\u2013";
            } else if ("'".equals(this.text)) {
                txt = "\u2032";
            } else if ("''".equals(this.text)) {
                txt = "\u2033";
            } else if ("'''".equals(this.text)) {
                txt = "\u2034";
                font = bfm.getFont("STIX Two Text Regular", (double) font.getSize(), font.getStyle());
            } else if ("''''".equals(this.text)) {
                txt = "\u2057";
                font = bfm.getFont("STIX Two Text Regular", (double) font.getSize(), font.getStyle());
            } else if (txt.length() == 1) {
                final char ch = txt.charAt(0);

                if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '\u03b1' && ch <= '\u03f5')) {
                    font = font.deriveFont(Font.ITALIC);
                }
            }
        }

        grx.setFont(font);
        final FontMetrics fm = grx.getFontMetrics();
        final int lwidth = fm.stringWidth(txt);

        if (getColorName() == null) {
            grx.setColor(Color.BLACK);
        } else {
            grx.setColor(ColorNames.getColor(getColorName()));
        }

        int x = 0;
        int y = getBaseLine();

        // For boxed, bounds are 4 pixels wider to accommodate box
        if (isBoxed()) {
            x += 2;
        }

        if (!isHidden()) {
            if (txt.length() == 1) {
                final char ch = txt.charAt(0);

                if (ch == '\u2147' || ch == '\u2148') {
                    grx.setFont(font.deriveFont(Font.ITALIC));
                    if (ch == '\u2147') {
                        grx.drawString("e", x, y);
                    } else {
                        grx.drawString("i", x, y);
                    }
                } else {
                    grx.drawString(txt, x, y);
                }
            } else {
                grx.drawString(txt, x, y);
            }

            if (isUnderline()) {
                x = 0;
                y = getBaseLine() + 1;
                grx.drawLine(x, y, x + lwidth, y);
            }

            if (isOverline()) {
                x = 0;
                y = 1;
                grx.drawLine(x, y, x + lwidth, y);
            }

            if (isStrikethrough()) {
                x = 0;
                y = getCenterLine();
                grx.drawLine(x, y, x + lwidth, y);
            }

            if (isBoxed()) {
                x = 0;
                y = 0;
                final int y2 = getHeight() - 1;
                grx.drawLine(x, y, x + getWidth() - 1, y);
                grx.drawLine(x, y2, x + getWidth() - 1, y2);
                grx.drawLine(x, y2, x + getWidth() - 1, y2);
                grx.drawLine(x, y, x, y2);
                grx.drawLine(x + getWidth() - 1, y, x + getWidth() - 1, y2);
            }
        }

        // Restore state of Graphics
        if (g2d != null) {
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, origHints);
        }

        postPaint(grx);
    }

    /**
     * Add any parameter names referenced by the object or its children to a set of names.
     *
     * @param set the set of parameter names
     */
    @Override
    public void accumulateParameterNames(final Set<String> set) {

        // No action
    }

    /**
     * Generates an instance of this document object based on a realized evaluation context.
     *
     * <p>
     * All variable references are replaced with their values from the context. Formulas may remain that depend on input
     * variables, but no references to non-input variables should remain.
     *
     * @param evalContext the evaluation context
     * @return the instance document object; null if unable to create the instance
     */
    @Override
    public DocTextInst createInstance(final EvalContext evalContext) {

        final DocObjectInstStyle objStyle = new DocObjectInstStyle(getColorName(), getFontName(), (float) getFontSize(),
                getFontStyle());

        return new DocTextInst(objStyle, null, this.text);
    }

    /**
     * Write the XML representation of the object to a {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void toXml(final HtmlBuilder xml, final int indent) {

        int inx;
        char ch;

        // TODO: If the text has a format attached, wrap it in a "nonwrap" (or in the future,
        // "text") tag with the format. Need to combine it with adjacent texts with the same format
        // - use a span object?

        if ("\n".equals(this.text)) {
            xml.add("<br/>");
        } else {
            for (inx = 0; inx < this.text.length(); inx++) {
                ch = this.text.charAt(inx);

                if (ch == '<') {
                    // xml.add("\\u003c");
                    xml.add("&lt;");
                } else if (ch == '>') {
                    // xml.add("\\u003e");
                    xml.add("&gt;");
                } else if (ch == '\"') {
                    xml.add("\\u0022");
                } else if (ch == '&') {
                    // xml.add("\\u0026");
                    // xml.add("&amp;");
                } else if (ch == '{') {
                    xml.add("\\u007b");
                } else if (ch == '}') {
                    xml.add("\\u007d");
                } else if (ch == '\t') {
                    xml.add("\\u0009");
                } else if (ch < 0x80) {
                    xml.add(ch);
                } else if (ch == '\u00A0') {
                    xml.add("{\\nbsp}");
                } else if (ch == '\u00B0') {
                    xml.add("{\\degree}");
                } else if (ch == '\u00B1') {
                    xml.add("{\\pm}");
                } else if (ch == '\u00B7') {
                    xml.add("{\\cdot}");
                } else if (ch == '\u00D7') {
                    xml.add("{\\times}");
                } else if (ch == '\u00F7') {
                    xml.add("{\\div}");
                } else if (ch == '\u0192') {
                    xml.add("{\\fnof}");
                } else if (ch == '\u0393') {
                    xml.add("{\\Gamma}");
                } else if (ch == '\u0394') {
                    xml.add("{\\Delta}");
                } else if (ch == '\u0398') {
                    xml.add("{\\Theta}");
                } else if (ch == '\u039B') {
                    xml.add("{\\Lamda}");
                } else if (ch == '\u039E') {
                    xml.add("{\\Xi}");
                } else if (ch == '\u03A0') {
                    xml.add("{\\Pi}");
                } else if (ch == '\u03A3') {
                    xml.add("{\\Sigma}");
                } else if (ch == '\u03A5') {
                    xml.add("{\\Upsilon}");
                } else if (ch == '\u03A6') {
                    xml.add("{\\Phi}");
                } else if (ch == '\u03A8') {
                    xml.add("{\\Psi}");
                } else if (ch == '\u03A9') {
                    xml.add("{\\Omega}");
                } else if (ch == '\u03B1') {
                    xml.add("{\\alpha}");
                } else if (ch == '\u03B2') {
                    xml.add("{\\beta}");
                } else if (ch == '\u03B3') {
                    xml.add("{\\gamma}");
                } else if (ch == '\u03B4') {
                    xml.add("{\\delta}");
                } else if (ch == '\u03B5') {
                    xml.add("{\\varepsilon}");
                } else if (ch == '\u03B6') {
                    xml.add("{\\zeta}");
                } else if (ch == '\u03B7') {
                    xml.add("{\\eta}");
                } else if (ch == '\u03B8') {
                    xml.add("{\\theta}");
                } else if (ch == '\u03B9') {
                    xml.add("{\\iota}");
                } else if (ch == '\u03BA') {
                    xml.add("{\\kappa}");
                } else if (ch == '\u03BB') {
                    xml.add("{\\lamda}");
                } else if (ch == '\u03BC') {
                    xml.add("{\\mu}");
                } else if (ch == '\u03BD') {
                    xml.add("{\\nu}");
                } else if (ch == '\u03BE') {
                    xml.add("{\\xi}");
                } else if (ch == '\u03BF') {
                    xml.add("{\\omicron}");
                } else if (ch == '\u03C0') {
                    xml.add("{\\pi}");
                } else if (ch == '\u03C1') {
                    xml.add("{\\rho}");
                } else if (ch == '\u03C2') {
                    xml.add("{\\varsigma}");
                } else if (ch == '\u03C3') {
                    xml.add("{\\sigma}");
                } else if (ch == '\u03C4') {
                    xml.add("{\\tau}");
                } else if (ch == '\u03C5') {
                    xml.add("{\\upsilon}");
                } else if (ch == '\u03C6') {
                    xml.add("{\\varphi}");
                } else if (ch == '\u03C7') {
                    xml.add("{\\chi}");
                } else if (ch == '\u03C8') {
                    xml.add("{\\psi}");
                } else if (ch == '\u03C9') {
                    xml.add("{\\omega}");
                } else if (ch == '\u03D1') {
                    xml.add("{\\vartheta}");
                } else if (ch == '\u03D5') {
                    xml.add("{\\phi}");
                } else if (ch == '\u03D6') {
                    xml.add("{\\varpi}");
                } else if (ch == '\u03F0') {
                    xml.add("{\\varkappa}");
                } else if (ch == '\u03F1') {
                    xml.add("{\\varrho}");
                } else if (ch == '\u03F5') {
                    xml.add("{\\epsilon}");
                } else if (ch == '\u2212') {
                    xml.add("{\\minus}");
                } else if (ch == '\u2014') {
                    xml.add("{\\textemdash}");
                } else if (ch == '\u2018') {
                    xml.add("{\\textquoteleft}");
                } else if (ch == '\u2019') {
                    xml.add("{\\textquoteright}");
                } else if (ch == '\u201C') {
                    xml.add("{\\textquotedblleft}");
                } else if (ch == '\u201D') {
                    xml.add("{\\textquotedblright}");
                } else if (ch == '\u2022') {
                    xml.add("{\\bullet}");
                } else if (ch == '\u2032') {
                    xml.add("{\\prime}");
                } else if (ch == '\u2033') {
                    xml.add("{\\dprime}");
                } else if (ch == '\u2034') {
                    xml.add("{\\tprime}");
                } else if (ch == '\u2057') {
                    xml.add("{\\qprime}");
                } else if (ch == '\u2147') {
                    xml.add("{\\e}");
                } else if (ch == '\u2148') {
                    xml.add("{\\i}");
                } else if (ch == '\u2190') {
                    xml.add("{\\leftarrow}");
                } else if (ch == '\u2191') {
                    xml.add("{\\uparrow}");
                } else if (ch == '\u2192') {
                    xml.add("{\\rightarrow}");
                } else if (ch == '\u2193') {
                    xml.add("{\\downarrow}");
                } else if (ch == '\u2194') {
                    xml.add("{\\leftrightarrow}");
                } else if (ch == '\u2195') {
                    xml.add("{\\updownarrow}");
                } else if (ch == '\u2218') {
                    xml.add("{\\circ}");
                } else if (ch == '\u221D') {
                    xml.add("{\\varpropto}");
                } else if (ch == '\u221E') {
                    xml.add("{\\infty}");
                } else if (ch == '\u2220') {
                    xml.add("{\\angle}");
                } else if (ch == '\u2221') {
                    xml.add("{\\measuredangle}");
                } else if (ch == '\u2229') {
                    xml.add("{\\cap}");
                } else if (ch == '\u222A') {
                    xml.add("{\\cup}");
                } else if (ch == '\u222B') {
                    xml.add("{\\int}");
                } else if (ch == '\u2243') {
                    xml.add("{\\simeq}");
                } else if (ch == '\u2248') {
                    xml.add("{\\approx}");
                } else if (ch == '\u2260') {
                    xml.add("{\\neq}");
                } else if (ch == '\u2264') {
                    xml.add("{\\leq}");
                } else if (ch == '\u2265') {
                    xml.add("{\\geq}");
                } else if (ch == '\u2266') {
                    xml.add("{\\leqq}");
                } else if (ch == '\u2267') {
                    xml.add("{\\geqq}");
                } else if (ch == '\u2268') {
                    xml.add("{\\lneqq}");
                } else if (ch == '\u2269') {
                    xml.add("{\\gneqq}");
                } else if (ch == '\u226A') {
                    xml.add("{\\ll}");
                } else if (ch == '\u226B') {
                    xml.add("{\\gg}");
                } else if (ch == '\u226C') {
                    xml.add("{\\between}");
                } else if (ch == '\u226E') {
                    xml.add("{\\nless}");
                } else if (ch == '\u226F') {
                    xml.add("{\\ngtr}");
                } else if (ch == '\u2270') {
                    xml.add("{\\nleq}");
                } else if (ch == '\u2271') {
                    xml.add("{\\ngeq}");
                } else if (ch == '\u2272') {
                    xml.add("{\\lesssim}");
                } else if (ch == '\u2273') {
                    xml.add("{\\gtrsim}");
                } else if (ch == '\u2276') {
                    xml.add("{\\lessgtr}");
                } else if (ch == '\u2277') {
                    xml.add("{\\gtrless}");
                } else if (ch == '\u227A') {
                    xml.add("{\\prec}");
                } else if (ch == '\u227B') {
                    xml.add("{\\succ}");
                } else if (ch == '\u227C') {
                    xml.add("{\\preccurlyeq}");
                } else if (ch == '\u227D') {
                    xml.add("{\\succcurlyeq}");
                } else if (ch == '\u227E') {
                    xml.add("{\\precsim}");
                } else if (ch == '\u227F') {
                    xml.add("{\\succsim}");
                } else if (ch == '\u2280') {
                    xml.add("{\\nprec}");
                } else if (ch == '\u2281') {
                    xml.add("{\\nsucc}");
                } else if (ch == '\u22D6') {
                    xml.add("{\\lessdot}");
                } else if (ch == '\u22D7') {
                    xml.add("{\\gtrdot}");
                } else if (ch == '\u22DA') {
                    xml.add("{\\lesseqgtr}");
                } else if (ch == '\u22DB') {
                    xml.add("{\\gtreqless}");
                } else if (ch == '\u22DE') {
                    xml.add("{\\curlyeqprec}");
                } else if (ch == '\u22DF') {
                    xml.add("{\\curlyeqsucc}");
                } else if (ch == '\u22E0') {
                    xml.add("{\\npreceq}");
                } else if (ch == '\u22E1') {
                    xml.add("{\\nsucceq}");
                } else if (ch == '\u22E6') {
                    xml.add("{\\lnsim}");
                } else if (ch == '\u22E7') {
                    xml.add("{\\gnsim}");
                } else if (ch == '\u22E8') {
                    xml.add("{\\precnsim}");
                } else if (ch == '\u22E9') {
                    xml.add("{\\succnsim}");
                } else if (ch == '\u22EF') {
                    xml.add("{\\cdots}");
                } else if (ch == '\u2322') {
                    xml.add("{\\smallfrown}");
                } else if (ch == '\u2323') {
                    xml.add("{\\smallsmile}");
                } else if (ch == '\u2329') {
                    xml.add("{\\langle}");
                } else if (ch == '\u232A') {
                    xml.add("{\\rangle}");
                } else if (ch == '\u25A0') {
                    xml.add("{\\blacksquare}");
                } else if (ch == '\u25B2') {
                    xml.add("{\\blacktriangle}");
                } else if (ch == '\u25B3') {
                    xml.add("{\\triangle}");
                } else if (ch == '\u25BA') {
                    xml.add("{\\blacktriangleright}");
                } else if (ch == '\u25BC') {
                    xml.add("{\\blacktriangledown}");
                } else if (ch == '\u25C4') {
                    xml.add("{\\blacktriangleleft}");
                } else if (ch == '\u2660') {
                    xml.add("{\\spadesuit}");
                } else if (ch == '\u2663') {
                    xml.add("{\\clubsuit}");
                } else if (ch == '\u2665') {
                    xml.add("{\\heartsuit}");
                } else if (ch == '\u2666') {
                    xml.add("{\\diamondsuit}");
                } else if (ch == '\u2713') {
                    xml.add("{\\checkmark}");
                } else if (ch == '\u27CB') {
                    xml.add("{\\diagup}");
                } else if (ch == '\u27CD') {
                    xml.add("{\\diagdown}");
                } else if (ch == '\u27F8') {
                    xml.add("{\\Longleftarrow}");
                } else if (ch == '\u27F9') {
                    xml.add("{\\Longrightarrow}");
                } else if (ch == '\u2A7D') {
                    xml.add("{\\leqslant}");
                } else if (ch == '\u2A7E') {
                    xml.add("{\\geqslant}");
                } else if (ch == '\u2A85') {
                    xml.add("{\\lessapprox}");
                } else if (ch == '\u2A86') {
                    xml.add("{\\gtrapprox}");
                } else if (ch == '\u2A87') {
                    xml.add("{\\lneq}");
                } else if (ch == '\u2A88') {
                    xml.add("{\\gneq}");
                } else if (ch == '\u2A89') {
                    xml.add("{\\lnapprox}");
                } else if (ch == '\u2A8A') {
                    xml.add("{\\gnapprox}");
                } else if (ch == '\u2A8B') {
                    xml.add("{\\lesseqqgtr}");
                } else if (ch == '\u2A8C') {
                    xml.add("{\\gtreqqless}");
                } else if (ch == '\u2A95') {
                    xml.add("{\\eqslantless}");
                } else if (ch == '\u2A96') {
                    xml.add("{\\eqslantgtr}");
                } else if (ch == '\u2AA1') {
                    xml.add("{\\lll}");
                } else if (ch == '\u2AA2') {
                    xml.add("{\\ggg}");
                } else if (ch == '\u2AAF') {
                    xml.add("{\\preceq}");
                } else if (ch == '\u2AB0') {
                    xml.add("{\\succeq}");
                } else if (ch == '\u2AB5') {
                    xml.add("{\\precneqq}");
                } else if (ch == '\u2AB6') {
                    xml.add("{\\succneqq}");
                } else if (ch == '\u2AB7') {
                    xml.add("{\\precapprox}");
                } else if (ch == '\u2AB8') {
                    xml.add("{\\succapprox}");
                } else if (ch == '\u2AB9') {
                    xml.add("{\\precnapprox}");
                } else if (ch == '\u2ABA') {
                    xml.add("{\\succnapprox}");
                } else if (ch == '\u2ADB') {
                    xml.add("{\\pitchfork}");
                } else {
                    xml.add("\\u", Integer.toHexString((ch >> 12) & 0x0F),
                            Integer.toHexString((ch >> 8) & 0x0F),
                            Integer.toHexString((ch >> 4) & 0x0F), Integer.toHexString(ch & 0x0F));
                }
            }
        }
    }

    /**
     * Write the LaTeX representation of the object to a string buffer.
     *
     * @param dir          the directory in which the LaTeX source files are being written
     * @param fileIndex    a 1-integer array containing an index used to uniquely name files to be included by the LaTeX
     *                     file; the value should be updated if the method writes any files
     * @param overwriteAll a 1-boolean array whose only entry contains True if the user has selected "overwrite all";
     *                     false to ask the user each time (this method can update this value to true if it is false and
     *                     the user is asked "Overwrite? [YES] [ALL] [NO]" and chooses [ALL])
     * @param builder      the {@code HtmlBuilder} to which to write the LaTeX
     * @param showAnswers  true to show answers in any inputs embedded in the document; false if answers should not be
     *                     shown
     * @param mode         the current LaTeX mode (T=text, $=in-line math, M=math)
     */
    @Override
    public void toLaTeX(final File dir, final int[] fileIndex,
                        final boolean[] overwriteAll, final HtmlBuilder builder, final boolean showAnswers,
                        final char[] mode, final EvalContext context) {

        final String theFontName = getFont().getFontName();
        char ch;
        int inx;

        for (inx = 0; inx < this.text.length(); inx++) {
            ch = this.text.charAt(inx);

            if (ch == '$') {
                builder.add("\\$");
            } else if (ch == '&') {
                builder.add("\\&");
            } else if (ch == '%') {
                builder.add("\\%");
            } else if (ch == '_') {
                builder.add("\\_");
            } else if (ch == '#') {
                builder.add("\\#");
            } else if (ch == '|') {
                if (mode[0] == 'T') {
                    builder.add("$|$");
                } else {
                    builder.add("|");
                }
            } else if ((theFontName.contains("Times"))
                    || (theFontName.contains("Arial"))) {

                if (ch == '\u00a0') {
                    builder.add("\\hspace*{2 mm}");
                } else if (ch == '\u00b0') {
                    builder.add((mode[0] == 'T') ? "$^\\circ$ "
                            : "^\\circ");
                } else if (ch == '\u00b1') {

                    if (mode[0] == 'T') {
                        builder.add("$\\pm$ ");
                    } else {
                        builder.add("\\pm ");
                    }
                } else if (ch == '\u00b7') {

                    if (mode[0] == 'T') {
                        builder.add("$\\cdot$ ");
                    } else {
                        builder.add("\\cdot ");
                    }
                } else if (ch == '\u00d7') {

                    if (mode[0] == 'T') {
                        builder.add("$\\times$ ");
                    } else {
                        builder.add("\\times ");
                    }
                } else if (ch == '\u003c') {

                    if (mode[0] == 'T') {
                        builder.add("$<$");
                    } else {
                        builder.add("<");
                    }
                } else if (ch == '\u003e') {

                    if (mode[0] == 'T') {
                        builder.add("$>$");
                    } else {
                        builder.add(">");
                    }
                } else if (ch == '\u0192') {

                    if (mode[0] == 'T') {
                        builder.add("$\\mathnormal{f}$ ");
                    } else {
                        builder.add("\\mathnormal{f} ");
                    }
                } else if (ch == '\u0394') {

                    if (mode[0] == 'T') {
                        builder.add("$\\Delta$ ");
                    } else {
                        builder.add("\\Delta ");
                    }
                } else if (ch == '\u2013') {

                    if (mode[0] == 'T') {
                        builder.add("\\textendash ");
                    } else {
                        builder.add("\text{\\textendash} ");
                    }
                } else if (ch == '\u2014') {

                    if (mode[0] == 'T') {
                        builder.add("\\textemdash ");
                    } else {
                        builder.add("\text{\\textemdash} ");
                    }
                } else if (ch == '\u2022') {

                    if (mode[0] == 'T') {
                        builder.add("\\textbullet ");
                    } else {
                        builder.add("\\text{\\textbullet} ");
                    }
                } else if (ch == '\u2190') {

                    if (mode[0] == 'T') {
                        builder.add("$\\leftarrow$ ");
                    } else {
                        builder.add("\\leftarrow ");
                    }
                } else if (ch == '\u2191') {

                    if (mode[0] == 'T') {
                        builder.add("$\\uparrow$ ");
                    } else {
                        builder.add("\\uparrow ");
                    }
                } else if (ch == '\u2192') {

                    if (mode[0] == 'T') {
                        builder.add("$\\rightarrow$ ");
                    } else {
                        builder.add("\\rightarrow ");
                    }
                } else if (ch == '\u2193') {

                    if (mode[0] == 'T') {
                        builder.add("$\\downarrow$ ");
                    } else {
                        builder.add("\\downarrow ");
                    }
                } else if (ch == '\u2194') {

                    if (mode[0] == 'T') {
                        builder.add("$\\leftrightarrow$ ");
                    } else {
                        builder.add("\\leftrightarrow ");
                    }
                } else if (ch == '\u2195') {

                    if (mode[0] == 'T') {
                        builder.add("$\\updownarrow$ ");
                    } else {
                        builder.add("\\updownarrow ");
                    }
                } else if (ch == '\u2212') {
                    builder.add('-');
                } else if (ch == '\u2248') {
                    builder.add((mode[0] == 'T') ? "$\\approx$ " :
                            "\\approx ");
                } else if (ch == '\u2260') {
                    builder.add((mode[0] == 'T') ? "$\\neq$ " : "\\neq$ ");
                } else if (ch == '\u2264') {
                    builder.add((mode[0] == 'T') ? "$\\leq$ " : "\\leq$ ");
                } else if (ch == '\u2265') {
                    builder.add((mode[0] == 'T') ? "$\\geq$ " : "\\geq$ ");
                } else if (ch == '\u25a0') {
                    builder.add("\\blacksquare ");
                } else if (ch == '\u25ac') {
                    builder.add("\\emdash ");
                } else if (ch == '\u25b2') {
                    builder.add("\\blacktriangle ");
                } else if (ch == '\u25ba') {
                    builder.add("\\blacktriangleright ");
                } else if (ch == '\u25bc') {
                    builder.add("\\blacktriangledown ");
                } else if (ch == '\u25c4') {
                    builder.add("\\blacktriangleleft ");
                } else {
                    builder.add(ch);
                }
            } else if (theFontName.contains("ESSTIXThree")) {

                if (ch == '\u0021') {
                    builder.add("<");
                } else if (ch == '\u0023') {
                    builder.add(" \\leqslant ");
                } else if (ch == '\u0024') {
                    builder.add(" \\eqslantless ");
                } else if (ch == '\u0025') {
                    builder.add(" \\leq ");
                } else if (ch == '\u0026') {
                    builder.add(" \\leqq ");
                } else if (ch == '\u0028') {
                    builder.add(" \\lesssim ");
                } else if (ch == '\u0029') {
                    builder.add(" \\lessapprox ");
                } else if (ch == '\u002b') {
                    builder.add(" \\lessgtr ");
                } else if (ch == '\u002c') {
                    builder.add(" \\lesseqgtr ");
                } else if (ch == '\u002d') {
                    builder.add(" \\lesseqqgtr ");
                } else if (ch == '\u002f') {
                    builder.add(" \\ll ");
                } else if (ch == '\u0030') {
                    builder.add(" \\lll ");
                } else if (ch == '\u0031') {
                    builder.add(" \\lessdot ");
                } else if (ch == '\u0033') {
                    builder.add(" \\prec ");
                } else if (ch == '\u0034') {
                    builder.add(" \\precsim ");
                } else if (ch == '\u0035') {
                    builder.add(" \\precapprox ");
                } else if (ch == '\u0036') {
                    builder.add(" \\preceq ");
                } else if (ch == '\u0037') {
                    builder.add(" \\preccurlyeq ");
                } else if (ch == '\u0038') {
                    builder.add(" \\curlyeqprec ");
                } else if (ch == '\u003a') {
                    builder.add((mode[0] == 'T') ? " $\\angle$ " :
                            " \\angle ");
                } else if (ch == '\u003b') {
                    builder.add(" \\measuredangle ");
                } else if (ch == '\u003e') {
                    builder.add(" \\nless ");
                } else if (ch == '\u003f') {
                    builder.add(" \\nleq ");
                } else if (ch == '\u0040') {
                    builder.add(" \\lneq ");
                } else if (ch == '\u0041') {
                    builder.add(" \\lneqq ");
                } else if (ch == '\u0042') {
                    builder.add(" \\lnsim ");
                } else if (ch == '\u0043') {
                    builder.add(" \\lnapprox ");
                } else if (ch == '\u0046') {
                    builder.add(" \\nleq ");
                } else if (ch == '\u0047') {
                    builder.add(" \\nleqq ");
                } else if (ch == '\u0048') {
                    builder.add(" \\between ");
                } else if (ch == '\u0049') {
                    builder.add(" \\nprec ");
                } else if (ch == '\u004a') {
                    builder.add(" \\precnsim ");
                } else if (ch == '\u004b') {
                    builder.add(" \\precnapprox ");
                } else if (ch == '\u004c') {
                    builder.add(" \\precneqq ");
                } else if (ch == '\u004d') {
                    builder.add(" \\npreceq ");
                } else if (ch == '\u004e') {
                    builder.add(" \\infty ");
                } else if (ch == '\u004f') {
                    builder.add(">");
                } else if (ch == '\u0050') {
                    builder.add(" \\geqslant ");
                } else if (ch == '\u0051') {
                    builder.add(" \\eqslantgtr ");
                } else if (ch == '\u0052') {
                    builder.add(" \\geq ");
                } else if (ch == '\u0053') {
                    builder.add(" \\geqq ");
                } else if (ch == '\u0054') {
                    builder.add(" \\gtrsim ");
                } else if (ch == '\u0055') {
                    builder.add(" \\gtrapprox ");
                } else if (ch == '\u0057') {
                    builder.add(" \\gtrless ");
                } else if (ch == '\u0058') {
                    builder.add(" \\gtreqless ");
                } else if (ch == '\u0059') {
                    builder.add(" \\gtreqqless ");
                } else if (ch == '\u005b') {
                    builder.add(" \\gg ");
                } else if (ch == '\\') {
                    builder.add(" \\ggg ");
                } else if (ch == '\u005d') {
                    builder.add(" \\gtrdot ");
                } else if (ch == '\u005f') {
                    builder.add(" \\succ ");
                } else if (ch == '\u0061') {
                    builder.add(" \\succsim ");
                } else if (ch == '\u0062') {
                    builder.add(" \\succapprox ");
                } else if (ch == '\u0063') {
                    builder.add(" \\succeq ");
                } else if (ch == '\u0064') {
                    builder.add(" \\succcurlyeq ");
                } else if (ch == '\u0065') {
                    builder.add(" \\curlyeqsucc ");
                } else if (ch == '\u0066') {
                    builder.add(" \\varpropto ");
                } else if (ch == '\u0067') {
                    builder.add(" \\smallsmile ");
                } else if (ch == '\u0068') {
                    builder.add(" \\smallfrown ");
                } else if (ch == '\u0069') {
                    builder.add(" \\pitchfork ");
                } else if (ch == '\u006a') {
                    builder.add(" \\ngrt ");
                } else if (ch == '\u006b') {
                    builder.add(" \\ngeq ");
                } else if (ch == '\u006c') {
                    builder.add(" \\gneq ");
                } else if (ch == '\u006d') {
                    builder.add(" \\gneqq ");
                } else if (ch == '\u006e') {
                    builder.add(" \\gnsim ");
                } else if (ch == '\u006f') {
                    builder.add(" \\gnapprox ");
                } else if (ch == '\u0070') {
                    builder.add(" \\gnapprox ");
                } else if (ch == '\u0072') {
                    builder.add(" \\ngeq ");
                } else if (ch == '\u0073') {
                    builder.add(" \\ngeqq ");
                } else if (ch == '\u0074') {
                    builder.add(" \\nsucc ");
                } else if (ch == '\u0075') {
                    builder.add(" \\succnsim ");
                } else if (ch == '\u0076') {
                    builder.add(" \\succnapprox ");
                } else if (ch == '\u0077') {
                    builder.add(" \\succneqq ");
                } else if (ch == '\u0078') {
                    builder.add(" \\nsucceq ");
                } else if (ch == '\u0079') {
                    builder.add(" \\diagup ");
                } else if (ch == '\u007a') {
                    builder.add(" \\diagdown ");
                } else if (ch == '\u2010') {
                    builder.add(" \\lesseqqgtr ");
                } else {
                    builder.add(ch);
                }
            } else if (theFontName.contains("ESSTIXFour")) {

                if (ch == '\u0021') {
                    builder.add("`");
                } else if (ch == '\u0023') {
                    builder.add('\'');
                } else if (ch == '\u0024') {
                    builder.add(CoreConstants.QUOTE);
                } else if (ch == '\u0025') {
                    builder.add("'\"");
                } else if (ch == '\u0026') {
                    builder.add("\"\"");
                } else if (ch == '\u0028') {
                    builder.add(" \\ ");
                } else if (ch == '\u002b') {
                    builder.add(" \\circ ");
                } else {
                    builder.add(ch);
                }
            } else if (theFontName.contains("ESSTIXSeven")) {

                if (ch == '\u0030') {

                    if (mode[0] == 'T') {
                        builder.add("$($");
                    } else {
                        builder.add("(");
                    }
                } else if (ch == '\u0031') {

                    if (mode[0] == 'T') {
                        builder.add("$)$");
                    } else {
                        builder.add(")");
                    }
                } else {
                    builder.add(ch);
                }
            } else if (theFontName.contains("ESSTIXNine")) {

                if (ch == '\u0070') {

                    if (mode[0] == 'T') {
                        builder.add("$\\pi$");
                    } else {
                        builder.add("\\pi");
                    }
                } else if (ch == '\u0071') {

                    if (mode[0] == 'T') {
                        builder.add("$\\theta$");
                    } else {
                        builder.add("\\theta");
                    }
                } else if (ch == '\u0072') {

                    if (mode[0] == 'T') {
                        builder.add("$\\rho$");
                    } else {
                        builder.add("\\rho");
                    }
                } else {
                    builder.add(ch);
                }
            } else {
                builder.add(ch);
            }
        }
    }

    /**
     * Print the contained object tree in HTML format, as a set of nested unordered list tags.
     *
     * @param ps the {@code PrintStream} to which to print the tree
     */
    @Override
    public void printTree(final PrintStream ps) {

        ps.print("<li>Text '");
        ps.print(this.text);
        ps.println('\'');
        ps.print("</li>");
    }

    /**
     * Generate a {@code String} representation of the paragraph (for diagnostic purposes).
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return this.text;
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return docObjectHashCode() + Objects.hashCode(this.text);
    }

    /**
     * Implementation of {@code equals} to compare two {@code DocObject} objects for equality.
     *
     * @param obj the object to be compared to this object
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final DocText txt) {
            equal = docObjectEquals(txt) && Objects.equals(this.text, txt.text);
        } else {
            equal = false;
        }

        return equal;
    }
}
