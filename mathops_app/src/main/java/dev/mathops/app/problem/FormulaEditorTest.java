package dev.mathops.app.problem;

import com.formdev.flatlaf.FlatLightLaf;
import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.formula.FormulaFactory;
import dev.mathops.assessment.formula.XmlFormulaFactory;
import dev.mathops.assessment.formula.edit.FEFormula;
import dev.mathops.assessment.formula.edit.FormulaEditorPanel;
import dev.mathops.assessment.formula.edit.IFormulaEditorListener;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.xml.IElement;
import dev.mathops.text.parser.xml.NonemptyElement;
import dev.mathops.text.parser.xml.XmlContent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Insets;

/**
 * A simple program that presents a formula edit panel and allows the user to edit a formula.
 */
public final class FormulaEditorTest implements Runnable, IFormulaEditorListener {

    /** An outline of the formula structure. */
    private JTextArea outline;

    /**
     * Constructs a new {@code FormulaEditorTest}.
     */
    private FormulaEditorTest() {

        // No action
    }

    /**
     * Constructs the UI on the AWT event thread.
     */
    @Override
    public void run() {

        final JFrame frame = new JFrame("Formula Editor Test");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final JPanel content = new JPanel(new BorderLayout());
        frame.setContentPane(content);

        final String testText = "test({cd}=1?'':test({cd}=-1?'-':'{cd}'))";

        final EvalContext ctx = new EvalContext();
        final Formula testFormula = FormulaFactory.parseFormulaString(ctx, testText, EParserMode.NORMAL);

        final HtmlBuilder htm = new HtmlBuilder(100);
        testFormula.appendXml(htm);
        final String xml = htm.toString();
        // Log.fine(xml);

        try {
            final XmlContent xmlContent = new XmlContent(xml, false, false);
            final IElement top = xmlContent.getTopLevel();
            if (top instanceof final NonemptyElement nonempty) {
                final Formula extracted = XmlFormulaFactory.extractFormula(ctx, nonempty, EParserMode.NORMAL);

                htm.reset();
                extracted.appendXml(htm);
                final String xml2 = htm.toString();
                if (!xml2.equals(xml)) {
                    Log.warning("XML changed!");
                    Log.warning("  From string: ", xml);
                    Log.warning("  From XML   : ", xml2);
                }
            }
        } catch (final ParsingException ex) {
            Log.warning(ex);
        }

        final EvalContext evalContext = new EvalContext();

        final Insets insets = new Insets(6, 8, 6, 8);
        final FormulaEditorPanel editor =
                new FormulaEditorPanel(16, insets, testFormula);
        editor.setBorder(BorderFactory.createEtchedBorder());
        content.add(editor, BorderLayout.CENTER);

        this.outline = new JTextArea(10, 30);
        this.outline.setBorder(BorderFactory.createEtchedBorder());
        this.outline.setEditable(false);
        content.add(this.outline, BorderLayout.EAST);

        final FormulaEditorPalette palette = new FormulaEditorPalette(editor);
        content.add(palette, BorderLayout.SOUTH);

        editor.addListener(this);
        editor.storeUndo();
        formulaEdited(editor.getRoot());

        UIUtilities.packAndCenter(frame);
        frame.setVisible(true);
    }

    /**
     * Called each time the formula is edited.
     *
     * @param formula the edited formula
     */
    @Override
    public void formulaEdited(final FEFormula formula) {

        final HtmlBuilder builder = new HtmlBuilder(100);

        formula.emitDiagnostics(builder, 0);

        this.outline.setText(builder.toString());
    }

    /**
     * Main method to run the program.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        FlatLightLaf.setup();
        EventQueue.invokeLater(new FormulaEditorTest());
    }
}
