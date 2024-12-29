package dev.mathops.app.document;

import com.formdev.flatlaf.FlatLightLaf;
import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.document.template.DocColumn;
import dev.mathops.assessment.document.template.DocColumnPanel;
import dev.mathops.assessment.document.template.DocFactory;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.xml.IElement;
import dev.mathops.text.parser.xml.NonemptyElement;
import dev.mathops.text.parser.xml.XmlContent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Objects;

/**
 * A document editor.
 */
public final class DocumentEditor implements Runnable, DocumentListener {

    /** A text area to edit XML. */
    private JTextArea xmlArea;

    /** A panel to receive the rendered image. */
    private JPanel imageArea;

    /** The current rendered doc column. */
    private DocColumnPanel currentDocColumnPanel;

    /** A status message. */
    private JLabel statusMessage;

    /**
     * Constructs a new {@code DocumentEditor}.
     */
    private DocumentEditor() {

        // No action
    }

    /**
     * Constructs the UI in the AWT event thread.
     */
    public void run() {

        final JFrame frame = new JFrame("Document Editor");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        final JPanel content = new JPanel(new BorderLayout());
        frame.setContentPane(content);

        content.setPreferredSize(new Dimension(1024, 768));

        this.xmlArea = new JTextArea(20, 40);
        this.xmlArea.setBorder(BorderFactory.createEtchedBorder());
        this.xmlArea.getDocument().addDocumentListener(this);
        final JScrollPane xmlScroll = new JScrollPane(this.xmlArea);
        content.add(xmlScroll, BorderLayout.LINE_START);

        final JPanel center = new JPanel(new BorderLayout());
        content.add(center, BorderLayout.CENTER);
        center.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        this.imageArea = new JPanel(new BorderLayout());
        center.add(this.imageArea, BorderLayout.CENTER);

        final JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
        content.add(statusBar, BorderLayout.PAGE_END);

        this.statusMessage = new JLabel(CoreConstants.SPC);
        statusBar.add(this.statusMessage);

        UIUtilities.packAndCenter(frame);
        frame.setVisible(true);
    }

    /**
     * Called when content is inserted into the document.
     *
     * @param e the document event
     */
    @Override
    public void insertUpdate(final DocumentEvent e) {

        updateDrawing();
    }

    /**
     * Called when content is removed from the document.
     *
     * @param e the document event
     */
    @Override
    public void removeUpdate(final DocumentEvent e) {

        updateDrawing();
    }

    /**
     * Called when content is updated in the document.
     *
     * @param e the document event
     */
    @Override
    public void changedUpdate(final DocumentEvent e) {

        updateDrawing();
    }

    /**
     * Attempts to update the drawing each time the XML document changes.
     */
    private void updateDrawing() {

        // Called only from the AWT event thread
        String status = CoreConstants.SPC;

        final String xml = this.xmlArea.getText();
        try {
            final XmlContent content = new XmlContent(xml, false, false);
            final IElement top = content.getToplevel();
            if (top == null) {
                status = "Unable to determine top-level element";
            } else if (top instanceof NonemptyElement nonempty) {
                final EvalContext evalContext = new EvalContext();

                final DocColumn column = DocFactory.parseDocColumn(evalContext, nonempty, EParserMode.ALLOW_DEPRECATED);

                if (column == null) {
                    status = "Unable to parse document.";
                } else {
                    final DocColumnPanel panel = new DocColumnPanel(column, evalContext);

                    if (Objects.nonNull(this.currentDocColumnPanel)) {
                        this.imageArea.removeAll();
                    }
                    final Dimension prefSize = panel.getPreferredSize();


                    this.imageArea.add(panel, BorderLayout.CENTER);
                    this.imageArea.invalidate();
                    this.imageArea.revalidate();
                    this.imageArea.repaint();

                    this.currentDocColumnPanel = panel;
                }
            } else {
                status = "Top-level element is not non-empty.";
            }
        } catch (final ParsingException ex) {
            status = ex.getMessage();
        }

        final String existing = this.statusMessage.getText();
        if (!existing.equals(status)) {
            this.statusMessage.setText(status);
        }
    }

    /**
     * Main method to run the application.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        FlatLightLaf.setup();

        SwingUtilities.invokeLater(new DocumentEditor());
    }
}
