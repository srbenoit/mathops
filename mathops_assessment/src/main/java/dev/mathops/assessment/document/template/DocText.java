package dev.mathops.assessment.document.template;

import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.inst.DocObjectInstStyle;
import dev.mathops.assessment.document.inst.DocTextInst;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.ui.ColorNames;
import dev.mathops.font.BundledFontManager;
import dev.mathops.text.builder.HtmlBuilder;

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
            final int ch = (int) theText.charAt(0);

            this.isStixText = ch == 0x03C0 || ch == 0x03D1 || ch == 0x03D5 || ch == 0x03D6 || ch == 0x03F0
                              || ch == 0x03F1 || ch == 0x03F5 || ch == 0x2034 || ch == 0x2057;

            this.isStixMath = ch == 0x21D0 || ch == 0x21D1 || ch == 0x21D2 || ch == 0x21D3 || ch == 0x21D4
                              || ch == 0x21D5 || ch == 0x2218 || ch == 0x221D || ch == 0x2220 || ch == 0x2221
                              || ch == 0x2229 || ch == 0x222A || ch == 0x2243 || ch == 0x2266 || ch == 0x2267
                              || ch == 0x2268 || ch == 0x2269 || ch == 0x226A || ch == 0x226B || ch == 0x226C
                              || ch == 0x226E || ch == 0x226F || ch == 0x2270 || ch == 0x2271 || ch == 0x2272
                              || ch == 0x2273 || ch == 0x2276 || ch == 0x2277 || ch == 0x227A || ch == 0x227B
                              || ch == 0x227C || ch == 0x227D || ch == 0x227E || ch == 0x227F || ch == 0x2280
                              || ch == 0x2281 || ch == 0x22D6 || ch == 0x22D7 || ch == 0x22DA || ch == 0x22DB
                              || ch == 0x22DE || ch == 0x22DF || ch == 0x22E0 || ch == 0x22E1 || ch == 0x22E6
                              || ch == 0x22E7 || ch == 0x22E8 || ch == 0x22E9 || ch == 0x22EF || ch == 0x2322
                              || ch == 0x2323 || ch == 0x2329 || ch == 0x232A || ch == 0x25B3 || ch == 0x2713
                              || ch == 0x27CB || ch == 0x27CD || ch == 0x27F8 || ch == 0x27F9 || ch == 0x27FA
                              || ch == 0x2A7D || ch == 0x2A7E || ch == 0x2A85 || ch == 0x2A86 || ch == 0x2A87
                              || ch == 0x2A88 || ch == 0x2A89 || ch == 0x2A8A || ch == 0x2A8B || ch == 0x2A8C
                              || ch == 0x2A95 || ch == 0x2A96 || ch == 0x2AA1 || ch == 0x2AA2 || ch == 0x2AAF
                              || ch == 0x2AB0 || ch == 0x2AB5 || ch == 0x2AB6 || ch == 0x2AB7 || ch == 0x2AB8
                              || ch == 0x2AB9 || ch == 0x2ABA || ch == 0x2ADB;
        } else {
            this.isStixText = false;
            this.isStixMath = false;
        }
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
            font = bfm.getFont("STIX Two Text Regular", font.getSize(), font.getStyle());
        } else if (this.isStixMath) {
            font = bfm.getFont("STIX Two Math Regular", font.getSize(), font.getStyle());
        } else if (mathMode != ELayoutMode.TEXT) {
            if (CoreConstants.DASH.equals(this.text)) {
                txt = "\u2013";
            } else if ("'".equals(this.text)) {
                txt = "\u2032";
            } else if ("''".equals(this.text)) {
                txt = "\u2033";
            } else if ("'''".equals(this.text)) {
                txt = "\u2034";
                font = bfm.getFont("STIX Two Text Regular", font.getSize(), font.getStyle());
            } else if ("''''".equals(this.text)) {
                txt = "\u2057";
                font = bfm.getFont("STIX Two Text Regular", font.getSize(), font.getStyle());
            } else if (txt.length() == 1) {
                final int ch = (int) txt.charAt(0);

                if ((ch >= (int) 'a' && ch <= (int) 'z') || (ch >= (int) 'A' && ch <= (int) 'Z')
                    || (ch >= 0x03b1 && ch <= 0x03f5)) {
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
                final int ch = (int)txt.charAt(0);

                if (ch == 0x2147 || ch == 0x2148) {
                    font = font.deriveFont(Font.ITALIC);
                    fm = bfm.getFontMetrics(font);

                    if (ch == 0x2147) {
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
            font = bfm.getFont("STIX Two Text Regular", font.getSize(), font.getStyle());
        } else if (this.isStixMath) {
            font = bfm.getFont("STIX Two Math Regular", font.getSize(), font.getStyle());
        } else if (mathMode != ELayoutMode.TEXT) {
            if (CoreConstants.DASH.equals(this.text)) {
                txt = "\u2013";
            } else if ("'".equals(this.text)) {
                txt = "\u2032";
            } else if ("''".equals(this.text)) {
                txt = "\u2033";
            } else if ("'''".equals(this.text)) {
                txt = "\u2034";
                font = bfm.getFont("STIX Two Text Regular", font.getSize(), font.getStyle());
            } else if ("''''".equals(this.text)) {
                txt = "\u2057";
                font = bfm.getFont("STIX Two Text Regular", font.getSize(), font.getStyle());
            }
        }

        grx.setFont(font);
        final FontMetrics fm = grx.getFontMetrics();
        final int lwidth = fm.stringWidth(txt);

        grx.setColor(ColorNames.getColor(getColorName()));

        int x = 0;
        int y = getBaseLine();

        // For boxed, bounds are 4 pixels wider to accommodate box
        if (isBoxed()) {
            x += 2;
        }

        if (isVisible()) {
            if (txt.length() == 1) {
                final int ch = (int)txt.charAt(0);

                if (ch == 0x2147 || ch == 0x2148) {
                    grx.setFont(font.deriveFont(Font.ITALIC));
                    if (ch == 0x2147) {
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
     * @return the instance document object
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
        int ch;

        // TODO: If the text has a format attached, wrap it in a "nonwrap" (or in the future, "text") tag with the
        //  format. Need to combine it with adjacent texts with the same format - use a span object?

        if ("\n".equals(this.text)) {
            xml.add("<br/>");
        } else {
            for (inx = 0; inx < this.text.length(); inx++) {
                ch = (int) this.text.charAt(inx);

                if (ch == (int) '<') {
                    // xml.add("\\u003c");
                    xml.add("&lt;");
                } else if (ch == (int) '>') {
                    // xml.add("\\u003e");
                    xml.add("&gt;");
                } else if (ch == (int) '\"') {
                    xml.add("&quot;");
                } else if (ch == (int) '&') {
                    // xml.add("\\u0026");
                    // xml.add("&amp;");
                } else if (ch == (int) '{') {
                    xml.add("\\u007b");
                } else if (ch == (int) '}') {
                    xml.add("\\u007d");
                } else if (ch == (int) '\t') {
                    xml.add("\\u0009");
                } else if (ch < 0x80) {
                    xml.add((char) ch);
                } else if (ch == 0x00A0) {
                    xml.add("{\\nbsp}");
                } else if (ch == 0x00B0) {
                    xml.add("{\\degree}");
                } else if (ch == 0x00B1) {
                    xml.add("{\\pm}");
                } else if (ch == 0x00B7) {
                    xml.add("{\\cdot}");
                } else if (ch == 0x00D7) {
                    xml.add("{\\times}");
                } else if (ch == 0x00F7) {
                    xml.add("{\\div}");
                } else if (ch == 0x0192) {
                    xml.add("{\\fnof}");
                } else if (ch == 0x0393) {
                    xml.add("{\\Gamma}");
                } else if (ch == 0x0394) {
                    xml.add("{\\Delta}");
                } else if (ch == 0x0398) {
                    xml.add("{\\Theta}");
                } else if (ch == 0x039B) {
                    xml.add("{\\Lamda}");
                } else if (ch == 0x039E) {
                    xml.add("{\\Xi}");
                } else if (ch == 0x03A0) {
                    xml.add("{\\Pi}");
                } else if (ch == 0x03A3) {
                    xml.add("{\\Sigma}");
                } else if (ch == 0x03A5) {
                    xml.add("{\\Upsilon}");
                } else if (ch == 0x03A6) {
                    xml.add("{\\Phi}");
                } else if (ch == 0x03A8) {
                    xml.add("{\\Psi}");
                } else if (ch == 0x03A9) {
                    xml.add("{\\Omega}");
                } else if (ch == 0x03B1) {
                    xml.add("{\\alpha}");
                } else if (ch == 0x03B2) {
                    xml.add("{\\beta}");
                } else if (ch == 0x03B3) {
                    xml.add("{\\gamma}");
                } else if (ch == 0x03B4) {
                    xml.add("{\\delta}");
                } else if (ch == 0x03B5) {
                    xml.add("{\\varepsilon}");
                } else if (ch == 0x03B6) {
                    xml.add("{\\zeta}");
                } else if (ch == 0x03B7) {
                    xml.add("{\\eta}");
                } else if (ch == 0x03B8) {
                    xml.add("{\\theta}");
                } else if (ch == 0x03B9) {
                    xml.add("{\\iota}");
                } else if (ch == 0x03BA) {
                    xml.add("{\\kappa}");
                } else if (ch == 0x03BB) {
                    xml.add("{\\lamda}");
                } else if (ch == 0x03BC) {
                    xml.add("{\\mu}");
                } else if (ch == 0x03BD) {
                    xml.add("{\\nu}");
                } else if (ch == 0x03BE) {
                    xml.add("{\\xi}");
                } else if (ch == 0x03BF) {
                    xml.add("{\\omicron}");
                } else if (ch == 0x03C0) {
                    xml.add("{\\pi}");
                } else if (ch == 0x03C1) {
                    xml.add("{\\rho}");
                } else if (ch == 0x03C2) {
                    xml.add("{\\varsigma}");
                } else if (ch == 0x03C3) {
                    xml.add("{\\sigma}");
                } else if (ch == 0x03C4) {
                    xml.add("{\\tau}");
                } else if (ch == 0x03C5) {
                    xml.add("{\\upsilon}");
                } else if (ch == 0x03C6) {
                    xml.add("{\\varphi}");
                } else if (ch == 0x03C7) {
                    xml.add("{\\chi}");
                } else if (ch == 0x03C8) {
                    xml.add("{\\psi}");
                } else if (ch == 0x03C9) {
                    xml.add("{\\omega}");
                } else if (ch == 0x03D1) {
                    xml.add("{\\vartheta}");
                } else if (ch == 0x03D5) {
                    xml.add("{\\phi}");
                } else if (ch == 0x03D6) {
                    xml.add("{\\varpi}");
                } else if (ch == 0x03F0) {
                    xml.add("{\\varkappa}");
                } else if (ch == 0x03F1) {
                    xml.add("{\\varrho}");
                } else if (ch == 0x03F5) {
                    xml.add("{\\epsilon}");
                } else if (ch == 0x2212) {
                    xml.add("{\\minus}");
                } else if (ch == 0x2013) {
                    xml.add("{\\textendash}");
                } else if (ch == 0x2014) {
                    xml.add("{\\textemdash}");
                } else if (ch == 0x2018) {
                    xml.add("{\\textquoteleft}");
                } else if (ch == 0x2019) {
                    xml.add("{\\textquoteright}");
                } else if (ch == 0x201C) {
                    xml.add("{\\textquotedblleft}");
                } else if (ch == 0x201D) {
                    xml.add("{\\textquotedblright}");
                } else if (ch == 0x2022) {
                    xml.add("{\\bullet}");
                } else if (ch == 0x2032) {
                    xml.add("{\\prime}");
                } else if (ch == 0x2033) {
                    xml.add("{\\dprime}");
                } else if (ch == 0x2034) {
                    xml.add("{\\tprime}");
                } else if (ch == 0x2057) {
                    xml.add("{\\qprime}");
                } else if (ch == 0x2147) {
                    xml.add("{\\e}");
                } else if (ch == 0x2148) {
                    xml.add("{\\i}");
                } else if (ch == 0x2190) {
                    xml.add("{\\leftarrow}");
                } else if (ch == 0x2191) {
                    xml.add("{\\uparrow}");
                } else if (ch == 0x2192) {
                    xml.add("{\\rightarrow}");
                } else if (ch == 0x2193) {
                    xml.add("{\\downarrow}");
                } else if (ch == 0x2194) {
                    xml.add("{\\leftrightarrow}");
                } else if (ch == 0x2195) {
                    xml.add("{\\updownarrow}");
                } else if (ch == 0x21D0) {
                    xml.add("{\\Leftarrow}");
                } else if (ch == 0x21D1) {
                    xml.add("{\\Uparrow}");
                } else if (ch == 0x21D2) {
                    xml.add("{\\Rightarrow}");
                } else if (ch == 0x21D3) {
                    xml.add("{\\Downarrow}");
                } else if (ch == 0x21D4) {
                    xml.add("{\\Leftrightarrow}");
                } else if (ch == 0x21D5) {
                    xml.add("{\\Updownarrow}");
                } else if (ch == 0x2218) {
                    xml.add("{\\circ}");
                } else if (ch == 0x221D) {
                    xml.add("{\\varpropto}");
                } else if (ch == 0x221E) {
                    xml.add("{\\infty}");
                } else if (ch == 0x2220) {
                    xml.add("{\\angle}");
                } else if (ch == 0x2221) {
                    xml.add("{\\measuredangle}");
                } else if (ch == 0x2229) {
                    xml.add("{\\cap}");
                } else if (ch == 0x222A) {
                    xml.add("{\\cup}");
                } else if (ch == 0x222B) {
                    xml.add("{\\int}");
                } else if (ch == 0x2243) {
                    xml.add("{\\simeq}");
                } else if (ch == 0x2248) {
                    xml.add("{\\approx}");
                } else if (ch == 0x2260) {
                    xml.add("{\\neq}");
                } else if (ch == 0x2264) {
                    xml.add("{\\leq}");
                } else if (ch == 0x2265) {
                    xml.add("{\\geq}");
                } else if (ch == 0x2266) {
                    xml.add("{\\leqq}");
                } else if (ch == 0x2267) {
                    xml.add("{\\geqq}");
                } else if (ch == 0x2268) {
                    xml.add("{\\lneqq}");
                } else if (ch == 0x2269) {
                    xml.add("{\\gneqq}");
                } else if (ch == 0x226A) {
                    xml.add("{\\ll}");
                } else if (ch == 0x226B) {
                    xml.add("{\\gg}");
                } else if (ch == 0x226C) {
                    xml.add("{\\between}");
                } else if (ch == 0x226E) {
                    xml.add("{\\nless}");
                } else if (ch == 0x226F) {
                    xml.add("{\\ngtr}");
                } else if (ch == 0x2270) {
                    xml.add("{\\nleq}");
                } else if (ch == 0x2271) {
                    xml.add("{\\ngeq}");
                } else if (ch == 0x2272) {
                    xml.add("{\\lesssim}");
                } else if (ch == 0x2273) {
                    xml.add("{\\gtrsim}");
                } else if (ch == 0x2276) {
                    xml.add("{\\lessgtr}");
                } else if (ch == 0x2277) {
                    xml.add("{\\gtrless}");
                } else if (ch == 0x227A) {
                    xml.add("{\\prec}");
                } else if (ch == 0x227B) {
                    xml.add("{\\succ}");
                } else if (ch == 0x227C) {
                    xml.add("{\\preccurlyeq}");
                } else if (ch == 0x227D) {
                    xml.add("{\\succcurlyeq}");
                } else if (ch == 0x227E) {
                    xml.add("{\\precsim}");
                } else if (ch == 0x227F) {
                    xml.add("{\\succsim}");
                } else if (ch == 0x2280) {
                    xml.add("{\\nprec}");
                } else if (ch == 0x2281) {
                    xml.add("{\\nsucc}");
                } else if (ch == 0x22D6) {
                    xml.add("{\\lessdot}");
                } else if (ch == 0x22D7) {
                    xml.add("{\\gtrdot}");
                } else if (ch == 0x22DA) {
                    xml.add("{\\lesseqgtr}");
                } else if (ch == 0x22DB) {
                    xml.add("{\\gtreqless}");
                } else if (ch == 0x22DE) {
                    xml.add("{\\curlyeqprec}");
                } else if (ch == 0x22DF) {
                    xml.add("{\\curlyeqsucc}");
                } else if (ch == 0x22E0) {
                    xml.add("{\\npreceq}");
                } else if (ch == 0x22E1) {
                    xml.add("{\\nsucceq}");
                } else if (ch == 0x22E6) {
                    xml.add("{\\lnsim}");
                } else if (ch == 0x22E7) {
                    xml.add("{\\gnsim}");
                } else if (ch == 0x22E8) {
                    xml.add("{\\precnsim}");
                } else if (ch == 0x22E9) {
                    xml.add("{\\succnsim}");
                } else if (ch == 0x22EF) {
                    xml.add("{\\cdots}");
                } else if (ch == 0x2322) {
                    xml.add("{\\smallfrown}");
                } else if (ch == 0x2323) {
                    xml.add("{\\smallsmile}");
                } else if (ch == 0x2329) {
                    xml.add("{\\langle}");
                } else if (ch == 0x232A) {
                    xml.add("{\\rangle}");
                } else if (ch == 0x25A0) {
                    xml.add("{\\blacksquare}");
                } else if (ch == 0x25B2) {
                    xml.add("{\\blacktriangle}");
                } else if (ch == 0x25B3) {
                    xml.add("{\\triangle}");
                } else if (ch == 0x25BA) {
                    xml.add("{\\blacktriangleright}");
                } else if (ch == 0x25BC) {
                    xml.add("{\\blacktriangledown}");
                } else if (ch == 0x25C4) {
                    xml.add("{\\blacktriangleleft}");
                } else if (ch == 0x2660) {
                    xml.add("{\\spadesuit}");
                } else if (ch == 0x2663) {
                    xml.add("{\\clubsuit}");
                } else if (ch == 0x2665) {
                    xml.add("{\\heartsuit}");
                } else if (ch == 0x2666) {
                    xml.add("{\\diamondsuit}");
                } else if (ch == 0x2713) {
                    xml.add("{\\checkmark}");
                } else if (ch == 0x27CB) {
                    xml.add("{\\diagup}");
                } else if (ch == 0x27CD) {
                    xml.add("{\\diagdown}");
                } else if (ch == 0x27F8) {
                    xml.add("{\\Longleftarrow}");
                } else if (ch == 0x27F9) {
                    xml.add("{\\Longrightarrow}");
                } else if (ch == 0x27FA) {
                    xml.add("{\\Longleftrightarrow}");
                } else if (ch == 0x2A7D) {
                    xml.add("{\\leqslant}");
                } else if (ch == 0x2A7E) {
                    xml.add("{\\geqslant}");
                } else if (ch == 0x2A85) {
                    xml.add("{\\lessapprox}");
                } else if (ch == 0x2A86) {
                    xml.add("{\\gtrapprox}");
                } else if (ch == 0x2A87) {
                    xml.add("{\\lneq}");
                } else if (ch == 0x2A88) {
                    xml.add("{\\gneq}");
                } else if (ch == 0x2A89) {
                    xml.add("{\\lnapprox}");
                } else if (ch == 0x2A8A) {
                    xml.add("{\\gnapprox}");
                } else if (ch == 0x2A8B) {
                    xml.add("{\\lesseqqgtr}");
                } else if (ch == 0x2A8C) {
                    xml.add("{\\gtreqqless}");
                } else if (ch == 0x2A95) {
                    xml.add("{\\eqslantless}");
                } else if (ch == 0x2A96) {
                    xml.add("{\\eqslantgtr}");
                } else if (ch == 0x2AA1) {
                    xml.add("{\\lll}");
                } else if (ch == 0x2AA2) {
                    xml.add("{\\ggg}");
                } else if (ch == 0x2AAF) {
                    xml.add("{\\preceq}");
                } else if (ch == 0x2AB0) {
                    xml.add("{\\succeq}");
                } else if (ch == 0x2AB5) {
                    xml.add("{\\precneqq}");
                } else if (ch == 0x2AB6) {
                    xml.add("{\\succneqq}");
                } else if (ch == 0x2AB7) {
                    xml.add("{\\precapprox}");
                } else if (ch == 0x2AB8) {
                    xml.add("{\\succapprox}");
                } else if (ch == 0x2AB9) {
                    xml.add("{\\precnapprox}");
                } else if (ch == 0x2ABA) {
                    xml.add("{\\succnapprox}");
                } else if (ch == 0x2ADB) {
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
        int ch;
        int inx;

        for (inx = 0; inx < this.text.length(); inx++) {
            ch = (int) this.text.charAt(inx);

            if (ch == (int) '$') {
                builder.add("\\$");
            } else if (ch == (int) '&') {
                builder.add("\\&");
            } else if (ch == (int) '%') {
                builder.add("\\%");
            } else if (ch == (int) '_') {
                builder.add("\\_");
            } else if (ch == (int) '#') {
                builder.add("\\#");
            } else if (ch == (int) '|') {
                if ((int) mode[0] == (int) 'T') {
                    builder.add("$|$");
                } else {
                    builder.add("|");
                }
            } else if ((theFontName.contains("Times")) || (theFontName.contains("Arial"))) {

                if (ch == 0x00a0) {
                    builder.add("\\hspace*{2 mm}");
                } else if (ch == 0x00b0) {
                    builder.add(((int) mode[0] == (int) 'T') ? "$^\\circ$ " : "^\\circ");
                } else if (ch == 0x00b1) {

                    if ((int) mode[0] == (int) 'T') {
                        builder.add("$\\pm$ ");
                    } else {
                        builder.add("\\pm ");
                    }
                } else if (ch == 0x00b7) {

                    if ((int) mode[0] == (int) 'T') {
                        builder.add("$\\cdot$ ");
                    } else {
                        builder.add("\\cdot ");
                    }
                } else if (ch == 0x00d7) {

                    if ((int) mode[0] == (int) 'T') {
                        builder.add("$\\times$ ");
                    } else {
                        builder.add("\\times ");
                    }
                } else if (ch == 0x003c) {

                    if ((int) mode[0] ==(int)  'T') {
                        builder.add("$<$");
                    } else {
                        builder.add("<");
                    }
                } else if (ch == 0x003e) {

                    if ((int) mode[0] == (int) 'T') {
                        builder.add("$>$");
                    } else {
                        builder.add(">");
                    }
                } else if (ch == 0x0192) {

                    if ((int) mode[0] == (int) 'T') {
                        builder.add("$\\mathnormal{f}$ ");
                    } else {
                        builder.add("\\mathnormal{f} ");
                    }
                } else if (ch == 0x0394) {

                    if ((int) mode[0] ==(int)  'T') {
                        builder.add("$\\Delta$ ");
                    } else {
                        builder.add("\\Delta ");
                    }
                } else if (ch == 0x2013) {

                    if ((int) mode[0] ==(int)  'T') {
                        builder.add("\\textendash ");
                    } else {
                        builder.add("\text{\\textendash} ");
                    }
                } else if (ch == 0x2014) {

                    if ((int) mode[0] == (int) 'T') {
                        builder.add("\\textemdash ");
                    } else {
                        builder.add("\text{\\textemdash} ");
                    }
                } else if (ch == 0x2022) {

                    if ((int) mode[0] == (int) 'T') {
                        builder.add("\\textbullet ");
                    } else {
                        builder.add("\\text{\\textbullet} ");
                    }
                } else if (ch == 0x2190) {

                    if ((int) mode[0] == (int) 'T') {
                        builder.add("$\\leftarrow$ ");
                    } else {
                        builder.add("\\leftarrow ");
                    }
                } else if (ch == 0x2191) {

                    if ((int) mode[0] == (int) 'T') {
                        builder.add("$\\uparrow$ ");
                    } else {
                        builder.add("\\uparrow ");
                    }
                } else if (ch == 0x2192) {

                    if ((int) mode[0] ==(int)  'T') {
                        builder.add("$\\rightarrow$ ");
                    } else {
                        builder.add("\\rightarrow ");
                    }
                } else if (ch == 0x2193) {

                    if ((int) mode[0] == (int) 'T') {
                        builder.add("$\\downarrow$ ");
                    } else {
                        builder.add("\\downarrow ");
                    }
                } else if (ch == 0x2194) {

                    if ((int) mode[0] ==(int)  'T') {
                        builder.add("$\\leftrightarrow$ ");
                    } else {
                        builder.add("\\leftrightarrow ");
                    }
                } else if (ch == 0x2195) {

                    if ((int) mode[0] == (int) 'T') {
                        builder.add("$\\updownarrow$ ");
                    } else {
                        builder.add("\\updownarrow ");
                    }
                } else if (ch == 0x21D0) {

                    if ((int) mode[0] == (int) 'T') {
                        builder.add("$\\Leftarrow ");
                    } else {
                        builder.add("\\Leftarrow ");
                    }
                } else if (ch == 0x21D1) {

                    if ((int) mode[0] == (int) 'T') {
                        builder.add("$\\Uparrow ");
                    } else {
                        builder.add("\\Uparrow ");
                    }
                } else if (ch == 0x21D2) {

                    if ((int) mode[0] == (int) 'T') {
                        builder.add("$\\Rightarrow ");
                    } else {
                        builder.add("\\Rightarrow ");
                    }
                } else if (ch == 0x21D3) {

                    if ((int) mode[0] == (int) 'T') {
                        builder.add("$\\Downarrow ");
                    } else {
                        builder.add("\\Downarrow ");
                    }
                } else if (ch == 0x21D4) {

                    if ((int) mode[0] == (int) 'T') {
                        builder.add("$\\Leftrightarrow ");
                    } else {
                        builder.add("\\Leftrightarrow ");
                    }
                } else if (ch == 0x21D5) {

                    if ((int) mode[0] == (int) 'T') {
                        builder.add("$\\Updownarrow ");
                    } else {
                        builder.add("\\Updownarrow ");
                    }

                } else if (ch == 0x2212) {
                    builder.add('-');
                } else if (ch == 0x2248) {
                    builder.add(((int) mode[0] == (int) 'T') ? "$\\approx$ " : "\\approx ");
                } else if (ch == 0x2260) {
                    builder.add(((int) mode[0] == (int) 'T') ? "$\\neq$ " : "\\neq$ ");
                } else if (ch == 0x2264) {
                    builder.add(((int) mode[0] == (int) 'T') ? "$\\leq$ " : "\\leq$ ");
                } else if (ch == 0x2265) {
                    builder.add(((int) mode[0] == (int) 'T') ? "$\\geq$ " : "\\geq$ ");
                } else if (ch == 0x25a0) {
                    builder.add("\\blacksquare ");
                } else if (ch == 0x25ac) {
                    builder.add("\\emdash ");
                } else if (ch == 0x25b2) {
                    builder.add("\\blacktriangle ");
                } else if (ch == 0x25ba) {
                    builder.add("\\blacktriangleright ");
                } else if (ch == 0x25bc) {
                    builder.add("\\blacktriangledown ");
                } else if (ch == 0x25c4) {
                    builder.add("\\blacktriangleleft ");
                } else {
                    builder.add((char) ch);
                }
            } else if (theFontName.contains("ESSTIXThree")) {

                if (ch == 0x0021) {
                    builder.add("<");
                } else if (ch == 0x0023) {
                    builder.add(" \\leqslant ");
                } else if (ch == 0x0024) {
                    builder.add(" \\eqslantless ");
                } else if (ch == 0x0025) {
                    builder.add(" \\leq ");
                } else if (ch == 0x0026) {
                    builder.add(" \\leqq ");
                } else if (ch == 0x0028) {
                    builder.add(" \\lesssim ");
                } else if (ch == 0x0029) {
                    builder.add(" \\lessapprox ");
                } else if (ch == 0x002b) {
                    builder.add(" \\lessgtr ");
                } else if (ch == 0x002c) {
                    builder.add(" \\lesseqgtr ");
                } else if (ch == 0x002d) {
                    builder.add(" \\lesseqqgtr ");
                } else if (ch == 0x002f) {
                    builder.add(" \\ll ");
                } else if (ch == 0x0030) {
                    builder.add(" \\lll ");
                } else if (ch == 0x0031) {
                    builder.add(" \\lessdot ");
                } else if (ch == 0x0033) {
                    builder.add(" \\prec ");
                } else if (ch == 0x0034) {
                    builder.add(" \\precsim ");
                } else if (ch == 0x0035) {
                    builder.add(" \\precapprox ");
                } else if (ch == 0x0036) {
                    builder.add(" \\preceq ");
                } else if (ch == 0x0037) {
                    builder.add(" \\preccurlyeq ");
                } else if (ch == 0x0038) {
                    builder.add(" \\curlyeqprec ");
                } else if (ch == 0x003a) {
                    builder.add(((int) mode[0] == (int) 'T') ? " $\\angle$ " : " \\angle ");
                } else if (ch == 0x003b) {
                    builder.add(" \\measuredangle ");
                } else if (ch == 0x003e) {
                    builder.add(" \\nless ");
                } else if (ch == 0x003f) {
                    builder.add(" \\nleq ");
                } else if (ch == 0x0040) {
                    builder.add(" \\lneq ");
                } else if (ch == 0x0041) {
                    builder.add(" \\lneqq ");
                } else if (ch == 0x0042) {
                    builder.add(" \\lnsim ");
                } else if (ch == 0x0043) {
                    builder.add(" \\lnapprox ");
                } else if (ch == 0x0046) {
                    builder.add(" \\nleq ");
                } else if (ch == 0x0047) {
                    builder.add(" \\nleqq ");
                } else if (ch == 0x0048) {
                    builder.add(" \\between ");
                } else if (ch == 0x0049) {
                    builder.add(" \\nprec ");
                } else if (ch == 0x004a) {
                    builder.add(" \\precnsim ");
                } else if (ch == 0x004b) {
                    builder.add(" \\precnapprox ");
                } else if (ch == 0x004c) {
                    builder.add(" \\precneqq ");
                } else if (ch == 0x004d) {
                    builder.add(" \\npreceq ");
                } else if (ch == 0x004e) {
                    builder.add(" \\infty ");
                } else if (ch == 0x004f) {
                    builder.add(">");
                } else if (ch == 0x0050) {
                    builder.add(" \\geqslant ");
                } else if (ch == 0x0051) {
                    builder.add(" \\eqslantgtr ");
                } else if (ch == 0x0052) {
                    builder.add(" \\geq ");
                } else if (ch == 0x0053) {
                    builder.add(" \\geqq ");
                } else if (ch == 0x0054) {
                    builder.add(" \\gtrsim ");
                } else if (ch == 0x0055) {
                    builder.add(" \\gtrapprox ");
                } else if (ch == 0x0057) {
                    builder.add(" \\gtrless ");
                } else if (ch == 0x0058) {
                    builder.add(" \\gtreqless ");
                } else if (ch == 0x0059) {
                    builder.add(" \\gtreqqless ");
                } else if (ch == 0x005b) {
                    builder.add(" \\gg ");
                } else if (ch == '\\') {
                    builder.add(" \\ggg ");
                } else if (ch == 0x005d) {
                    builder.add(" \\gtrdot ");
                } else if (ch == 0x005f) {
                    builder.add(" \\succ ");
                } else if (ch == 0x0061) {
                    builder.add(" \\succsim ");
                } else if (ch == 0x0062) {
                    builder.add(" \\succapprox ");
                } else if (ch == 0x0063) {
                    builder.add(" \\succeq ");
                } else if (ch == 0x0064) {
                    builder.add(" \\succcurlyeq ");
                } else if (ch == 0x0065) {
                    builder.add(" \\curlyeqsucc ");
                } else if (ch == 0x0066) {
                    builder.add(" \\varpropto ");
                } else if (ch == 0x0067) {
                    builder.add(" \\smallsmile ");
                } else if (ch == 0x0068) {
                    builder.add(" \\smallfrown ");
                } else if (ch == 0x0069) {
                    builder.add(" \\pitchfork ");
                } else if (ch == 0x006a) {
                    builder.add(" \\ngrt ");
                } else if (ch == 0x006b) {
                    builder.add(" \\ngeq ");
                } else if (ch == 0x006c) {
                    builder.add(" \\gneq ");
                } else if (ch == 0x006d) {
                    builder.add(" \\gneqq ");
                } else if (ch == 0x006e) {
                    builder.add(" \\gnsim ");
                } else if (ch == 0x006f) {
                    builder.add(" \\gnapprox ");
                } else if (ch == 0x0070) {
                    builder.add(" \\gnapprox ");
                } else if (ch == 0x0072) {
                    builder.add(" \\ngeq ");
                } else if (ch == 0x0073) {
                    builder.add(" \\ngeqq ");
                } else if (ch == 0x0074) {
                    builder.add(" \\nsucc ");
                } else if (ch == 0x0075) {
                    builder.add(" \\succnsim ");
                } else if (ch == 0x0076) {
                    builder.add(" \\succnapprox ");
                } else if (ch == 0x0077) {
                    builder.add(" \\succneqq ");
                } else if (ch == 0x0078) {
                    builder.add(" \\nsucceq ");
                } else if (ch == 0x0079) {
                    builder.add(" \\diagup ");
                } else if (ch == 0x007a) {
                    builder.add(" \\diagdown ");
                } else if (ch == 0x2010) {
                    builder.add(" \\lesseqqgtr ");
                } else {
                    builder.add((char) ch);
                }
            } else if (theFontName.contains("ESSTIXFour")) {

                if (ch == 0x0021) {
                    builder.add("`");
                } else if (ch == 0x0023) {
                    builder.add('\'');
                } else if (ch == 0x0024) {
                    builder.add(CoreConstants.QUOTE);
                } else if (ch == 0x0025) {
                    builder.add("'\"");
                } else if (ch == 0x0026) {
                    builder.add("\"\"");
                } else if (ch == 0x0028) {
                    builder.add(" \\ ");
                } else if (ch == 0x002b) {
                    builder.add(" \\circ ");
                } else {
                    builder.add((char) ch);
                }
            } else if (theFontName.contains("ESSTIXSeven")) {

                if (ch == 0x0030) {

                    if ((int) mode[0] == (int) 'T') {
                        builder.add("$($");
                    } else {
                        builder.add("(");
                    }
                } else if (ch == 0x0031) {

                    if ((int) mode[0] == (int) 'T') {
                        builder.add("$)$");
                    } else {
                        builder.add(")");
                    }
                } else {
                    builder.add((char) ch);
                }
            } else if (theFontName.contains("ESSTIXNine")) {

                if (ch == 0x0070) {

                    if ((int) mode[0] == (int) 'T') {
                        builder.add("$\\pi$");
                    } else {
                        builder.add("\\pi");
                    }
                } else if (ch == 0x0071) {

                    if ((int) mode[0] == (int) 'T') {
                        builder.add("$\\theta$");
                    } else {
                        builder.add("\\theta");
                    }
                } else if (ch == 0x0072) {

                    if ((int) mode[0] == (int) 'T') {
                        builder.add("$\\rho$");
                    } else {
                        builder.add("\\rho");
                    }
                } else {
                    builder.add((char) ch);
                }
            } else {
                builder.add((char) ch);
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
