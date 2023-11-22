package jwabbit.utilities;

import jwabbit.iface.Calc;
import jwabbit.log.LoggedObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * WABBITEMU SOURCE: utilities/exportvar.h, "MFILE" struct.
 */
public final class MFILE {

    /** The input stream. */
    private FileInputStream fis;

    /** The output stream. */
    private FileOutputStream fos;

    /** The index at which to write data. */
    private int pnt;

    /** The size. */
    private int size;

    /** The data. */
    private int[] data;

    /** The read flag. */
    private boolean read;

    /** The write flag. */
    private boolean write;

    /** The binary flag. */
    private boolean bin;

    /**
     * Constructs a new {@code MFILE}.
     */
    private MFILE() {

        super();

        this.fis = null;
        this.fos = null;
        this.pnt = 0;
        this.size = 0;
        this.data = new int[0];
        this.read = false;
        this.write = false;
        this.bin = false;
    }

    /**
     * Tests whether the file was opened for read.
     *
     * @return true if open for read
     */
    public boolean isRead() {

        return this.read;
    }

    /**
     * Tests whether the file was opened for write.
     *
     * @return true if open for write
     */
    public boolean isWrite() {

        return this.write;
    }

    /**
     * Tests whether the file was opened in binary mode.
     *
     * @return true if open in binary mode
     */
    public boolean isBin() {

        return this.bin;
    }

    /**
     * WABBITEMU SOURCE: utilities/exportvar.c, "mopen" function.
     *
     * @param filename the filename
     * @param mode     the mode, "r", "w", or "a" or any of these with a "b" on the end
     * @return the opened file; null on failure
     */
    private static MFILE mopen(final String filename, final String mode) {

        MFILE mf = new MFILE();

        if (filename == null) {
            LoggedObject.LOG.warning("Null MFILE filename provided to mopen", new IllegalArgumentException());
        } else {
            try {
                if ("r".equals(mode) || "rb".equals(mode)) {
                    mf.fis = new FileInputStream(filename);
                    mf.fos = null;
                    mf.read = true;
                    mf.bin = "rb".equals(mode);
                } else if ("w".equals(mode) || "wb".equals(mode)) {
                    mf.fis = null;
                    mf.fos = new FileOutputStream(filename, false);
                    mf.write = true;
                    mf.bin = "wb".equals(mode);
                } else if ("a".equals(mode) || "ab".equals(mode)) {
                    mf.fis = null;
                    mf.fos = new FileOutputStream(filename, true);
                    mf.write = true;
                    mf.bin = "ab".equals(mode);
                } else {
                    LoggedObject.LOG.warning("Unrecognized file mode: ", mode, new IllegalArgumentException());
                    mf = null;
                }
            } catch (final FileNotFoundException ex) {
                LoggedObject.LOG.warning("Unable to open MFILE '", filename, "' for mode ", mode, ex);
                mf = null;
            }
        }

        return mf;
    }

    /**
     * WABBITEMU SOURCE: utilities/exportvar.c, "mclose" function.
     */
    public void mclose() {

        try {
            if (this.fis != null) {
                this.fis.close();
            }
            if (this.fos != null) {
                this.fos.close();
            }
        } catch (final IOException ex) {
            LoggedObject.LOG.warning("Unable to close MFILE", ex);
        }

        this.fis = null;
        this.fos = null;
        this.data = null;

        this.read = false;
        this.write = false;
        this.bin = false;
    }

    /**
     * WABBITEMU SOURCE: utilities/exportvar.c, "mputc" function.
     *
     * @param byt the byte to put
     */
    private void mputc(final int byt) {

        if (this.fos != null) {
            try {
                this.fos.write(byt);
                ++this.size;
            } catch (final IOException ex) {
                LoggedObject.LOG.warning("Unable to write to open MFILE", ex);
            }
        } else {
            if (this.pnt >= this.size) {
                final int[] temp = new int[this.data.length + 1];
                System.arraycopy(this.data, 0, temp, 0, this.data.length);
                this.data = temp;
                ++this.size;
            }
            this.data[this.pnt] = byt;
            ++this.pnt;
        }
    }

    /**
     * WABBITEMU SOURCE: utilities/exportvar.c, "ExportRom" function.
     *
     * @param filename the filename
     * @param calc     the calculator
     * @return the MFILE of the exported ROM file
     */
    public static MFILE exportRom(final String filename, final Calc calc) {

        final MFILE file = mopen(filename, "wb");

        if (file == null) {
            return file;
        }

        final int[] rom = calc.getCPU().getMemoryContext().getFlash().asArray();

        if (rom != null) {
            final int size = calc.getCPU().getMemoryContext().getFlash().getSize();
            for (int i = 0; i < size; ++i) {
                file.mputc(rom[i]);
            }
        }

        return file;
    }
}
