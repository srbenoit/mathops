package dev.mathops.app.problem;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.Serial;

/**
 * A transfer handler to process data transfers of a problem panel as an image.
 */
final class ProblemPanelTransferHandler extends TransferHandler {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -2814754398065371383L;

    /** The source component. */
    private AbstractProblemPanelBase srcComp;

    /**
     * Constructs a new {@code ProblemPanelTransferHandler}.
     */
    ProblemPanelTransferHandler() {

        super();
    }

    /**
     * Importer (import is not supported).
     *
     * @param comp the component
     * @param t the transferable to import
     * @return false since import is not supported
     */
    @Override
    public boolean importData(final JComponent comp, final Transferable t) {

        return false;
    }

    /**
     * Create the transferable from a component.
     *
     * @param c the component
     * @return the transferable
     */
    @Override
    protected Transferable createTransferable(final JComponent c) {

        this.srcComp = (AbstractProblemPanelBase) c;

        return new PictureTransferable(this.srcComp);
    }

    /**
     * Get the source actions supported.
     *
     * @param c the component
     */
    @Override
    public int getSourceActions(final JComponent c) {

        return COPY;
    }

    /**
     * Called when an export is complete.
     *
     * @param source   the component
     * @param data   the transferable
     * @param action the action taken
     */
    @Override
    protected void exportDone(final JComponent source, final Transferable data, final int action) {

        this.srcComp = null;
    }

    /**
     * Test whether any of a set of data flavors can be imported.
     *
     * @param comp    the component
     * @param transferFlavors the possible flavors
     * @return false since import is not supported
     */
    @Override
    public boolean canImport(final JComponent comp, final DataFlavor[] transferFlavors) {

        return false;
    }
}

/**
 * A transferable that contains an image.
 */
final class PictureTransferable implements Transferable {

    /** The image. */
    private final Image image;

    /**
     * Constructs a new {@code PictureTransferable}.
     *
     * @param pic The problem panel whose image to get.
     */
    PictureTransferable(final AbstractProblemPanelBase pic) {

        this.image = pic.getImage();
    }

    /**
     * Get the transfer data in a particular flavor.
     *
     * @param flavor the data flavor
     * @return the transfer data
     * @throws UnsupportedFlavorException if the requested flavor is not supported
     */
    @Override
    public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException {

        if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }

        return this.image;
    }

    /**
     * Get the set of supported data flavors.
     *
     * @return the array of flavors
     */
    @Override
    public DataFlavor[] getTransferDataFlavors() {

        return new DataFlavor[]{DataFlavor.imageFlavor};
    }

    /**
     * Test whether a particular data flavor is supported.
     *
     * @param flavor the data flavor to test
     * @return true if the data flavor is supported
     */
    @Override
    public boolean isDataFlavorSupported(final DataFlavor flavor) {

        return DataFlavor.imageFlavor.equals(flavor);
    }
}
