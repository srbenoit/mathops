package jwabbit.gui.wizard;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.EnumCalcModel;
import jwabbit.hardware.Link;
import jwabbit.iface.Calc;
import jwabbit.iface.Globals;
import jwabbit.log.LoggedObject;
import jwabbit.utilities.FileUtilities;
import jwabbit.utilities.MFILE;
import jwabbit.utilities.TIFILE;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * The Wonderful Wizard of Wabbit.
 *
 * <p>
 * Runs the ROM wizard and waits for the user to finish or cancel the wizard.
 */
public final class RomWizard implements Runnable, IWizardRunner {

    /** Flag indicating wizard has been completed or canceled. */
    private boolean complete;

    /** Flag indicating wizard was finished (false if canceled). */
    private boolean finished;

    /** Flag indicating user browsed for a ROM. */
    private boolean browsedForRom;

    /** The ROM browse path. */
    private String browsePath;

    /** Flag indicating user created a ROM. */
    private boolean createdRom;

    /** The selected calculator model. */
    private EnumCalcModel model;

    /** Flag indicating user downloaded an OS. */
    private boolean downloadedOs;

    /** The selected OS choice. */
    private String osChoice;

    /** The selected OS File. */
    private File osFile;

    /**
     * Constructs a new {@code RomWizard}.
     */
    private RomWizard() {

        super();

        this.complete = false;
        this.finished = false;
    }

    /**
     * Displays the wizard, and if completed successfully, creates a {@code CalcUI} for the loaded calculator.
     *
     * @param calc the calculator into which to load the selected ROM
     * @return true on success
     */
    public static boolean doWizardSheet(final Calc calc) {

        final boolean success;

        final RomWizard wizard = new RomWizard();
        if (wizard.doWizard()) {

            if (wizard.browsedForRom) {
                final String path = wizard.browsePath;
                success = calc.romLoad(path);
            } else if (wizard.createdRom) {
                final String[] buffer = new String[1];
                final File[] osPath = new File[1];
                if (FileUtilities.saveFile(buffer, new String[]{"ROMs"}, new String[][]{{"rom", "bin"}},
                        "Wabbitemu Export Rom") != 0) {
                    return false;
                }

                final EnumCalcModel model = wizard.model;
                if (wizard.downloadedOs) {
                    final boolean succeeded = downloadOS(osPath, model, wizard.osChoice == null);
                    if (!succeeded) {
                        JOptionPane.showMessageDialog(null, "Unable to download file", "Download failed",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    osPath[0] = wizard.osFile;
                }

                final String[] hexFile = new String[1];
                extractBootFree(model, hexFile);
                modelInit(calc, model);

                calc.setRomPath(osPath[0].getAbsolutePath());

                calc.setModel(model);
                calc.getCPU().getPIOContext().setModel(model);

                try (final FileInputStream file = new FileInputStream(hexFile[0])) {
                    Link.writeboot(file, calc.getCPU().getMemoryContext(), -1);
                } catch (final IOException ex) {
                    JOptionPane.showMessageDialog(null, "Error: Unable to write boot file", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                if (hexFile[0] != null) {
                    new File(hexFile[0]).delete();
                }

                if (osPath[0] != null && osPath[0].length() > 0L) {
                    final TIFILE tifile = TIFILE.importvar(osPath[0].getAbsolutePath(), false);
                    if (tifile == null || tifile.getType() != TIFILE.FLASH_TYPE) {
                        JOptionPane.showMessageDialog(null, "Error: OS file is corrupt", "Error",
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        Link.forceloadOS(calc.getCPU(), tifile);
                        if (wizard.downloadedOs) {
                            osPath[0].delete();
                        }
                    }
                }

                Globals.calcEraseCertificate(calc.getCPU().getMemoryContext().getFlash(),
                        calc.getCPU().getMemoryContext().getFlash().getSize());
                calc.calcReset();

                final MFILE romfile = MFILE.exportRom(buffer[0], calc);
                romfile.mclose();
                success = true;
            } else {
                LoggedObject.LOG.warning("Unimplemented wizard selection");
                success = false;
            }
        } else {
            // user cancel
            success = false;
        }

        return success;
    }

    /**
     * Informs the runner that the wizard has been completed and closed, either by finishing or by canceling.
     *
     * @param isFinished true if finished; false if canceled
     */
    @Override
    public void complete(final boolean isFinished) {

        synchronized (this) {
            this.finished = isFinished;
            this.complete = true;
            this.notifyAll();
        }
    }

    /**
     * Sets the flag that indicates whether the user browsed for a ROM.
     *
     * @param browsed true if user browsed for a ROM
     */
    @Override
    public void setBrowsedForRom(final boolean browsed) {

        this.browsedForRom = browsed;
    }

    /**
     * Sets the path of the ROM for which the user browsed.
     *
     * @param thePath the path
     */
    @Override
    public void setBrowsePath(final String thePath) {

        this.browsePath = thePath;
    }

    /**
     * Sets the flag that indicates whether the user created a ROM.
     *
     * @param created true if user created a ROM
     */
    @Override
    public void setCreatedRom(final boolean created) {

        this.createdRom = created;
    }

    /**
     * Sets the calculator model selected.
     *
     * @param theModel the model
     */
    @Override
    public void setModel(final EnumCalcModel theModel) {

        this.model = theModel;
    }

    /**
     * Sets the flag that indicates whether the user downloaded an OS.
     *
     * @param downloaded true if user downloaded an OS
     */
    @Override
    public void setDownloadOs(final boolean downloaded) {

        this.downloadedOs = downloaded;
    }

    /**
     * Displays the ROM wizard and waits for the user to complete it or cancel. This method can be called from the user
     * or event dispatch thread.
     *
     * @return true if the wizard is finished, false if canceled.
     */
    private boolean doWizard() {

        if (SwingUtilities.isEventDispatchThread()) {
            run();
        } else {
            SwingUtilities.invokeLater(this);
        }

        boolean isFinished = false;

        final Thread curThread = Thread.currentThread();

        synchronized (this) {
            for (; ; ) {
                if (this.complete) {
                    isFinished = this.finished;
                    break;
                }
                try {
                    this.wait();
                } catch (final InterruptedException ex) {
                    curThread.interrupt();
                    break;
                }
            }
        }

        return isFinished;
    }

    /**
     * WABBITEMU SOURCE: gui/guiwizard.c "ExtractBootFree" function.
     *
     * @param model   the calculator model
     * @param hexFile array to populate with the name of the hex file
     */
    public static void extractBootFree(final EnumCalcModel model, final String[] hexFile) {

        hexFile[0] = FileUtilities.getStorageString() + "/boot.hex";

        final String resName;

        switch (model) {
            case TI_73:
                resName = "jwabbit/gui/rom/bf73.hex";
                break;
            case TI_83P:
                resName = "jwabbit/gui/rom/bf83pbe.hex";
                break;
            case TI_83PSE:
                resName = "jwabbit/gui/rom/bf83pse.hex";
                break;
            case TI_84P:
                resName = "jwabbit/gui/rom/bf84pbe.hex";
                break;
            case TI_84PSE:
                resName = "jwabbit/gui/rom/bf84pse.hex";
                break;
            case TI_84PCSE:
                resName = "jwabbit/gui/rom/bf84pcse.hex";
                break;

            case INVALID_MODEL, TI_81, TI_82, TI_83, TI_85, TI_86:
            default:
                return;
        }

        // Read the contents of the file
        byte[] content;
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (final InputStream input = loader.getResourceAsStream(resName);
             final ByteArrayOutputStream baos = new ByteArrayOutputStream(40000)) {

            if (input != null) {
                final byte[] buffer = new byte[2048];
                int numRead = input.read(buffer);
                while (numRead > 0) {
                    baos.write(buffer, 0, numRead);
                    numRead = input.read(buffer);
                }
            }

            content = baos.toByteArray();
        } catch (final IOException ex) {
            LoggedObject.LOG.warning("Failed to load bootfree hex resource ", resName, ex);
            content = null;
        }

        extractResource(hexFile[0], content);
    }

    /**
     * Extracts the bytes from a resource file and writes it to a file.
     *
     * @param filename the name of the file to which to write
     * @param content  the file content to write
     */
    private static void extractResource(final String filename, final byte[] content) {

        try (final FileOutputStream output = new FileOutputStream(filename)) {
            output.write(content);
        } catch (final IOException ex) {
            LoggedObject.LOG.warning("Failed to extract resource to file", ex);
        }
    }

    /**
     * SOURCE: gui/wizard/romwizard.cpp, "RomWizard::ModelInit" method.
     *
     * @param calc  the calculator
     * @param model the calculator model
     */
    private static void modelInit(final Calc calc, final EnumCalcModel model) {

        switch (model) {
            case TI_73, TI_83P:
                calc.calcInit83p();
                break;
            case TI_83PSE, TI_84PSE:
                calc.calcInit83pse();
                break;
            case TI_84P:
                calc.calcInit84p();
                break;
            case TI_84PCSE:
                calc.calcInit84pcse();
                break;

            case INVALID_MODEL, TI_81, TI_82, TI_83, TI_85, TI_86:
            default:
                LoggedObject.LOG.warning("Unhandled case");
                break;
        }
    }

    /**
     * SOURCE: gui/wizard/romwizard.cpp, "RomWizard::DownloadOS" method.
     *
     * @param osFilePath a file array into which to insert the OS file path
     * @param model      the calculator model
     * @param version    the version to download
     * @return true if successful; false on error
     */
    private static boolean downloadOS(final File[] osFilePath, final EnumCalcModel model,
                                      final boolean version) {

        try {
            final File tempFile = File.createTempFile("wabbit", "tmp").getParentFile();
            osFilePath[0] = new File(tempFile, "/os.8xu");
        } catch (final IOException ex) {
            return false;
        }

        final String urlStr = switch (model) {
            case TI_73 -> "https://education.ti.com/downloads/files/73/TI73_OS.73u";
            case TI_83P, TI_83PSE -> "https://education.ti.com/downloads/files/83plus/TI83Plus_OS.8Xu";
            case TI_84P, TI_84PSE -> version
                    ? "https://education.ti.com/downloads/files/83plus/TI84Plus_OS243.8Xu"
                    : "https://education.ti.com/downloads/files/83plus/TI84Plus_OS.8Xu";
            default -> null;
        };

        try {
            if (urlStr == null) {
                return false;
            }

            final URL url = new URL(urlStr);

            try (final InputStream inStr = url.openStream();
                 final FileOutputStream fos = new FileOutputStream(osFilePath[0])) {

                int data = inStr.read();
                while (data != -1) {
                    fos.write(data);
                    data = inStr.read();
                }
            } catch (final IOException ex) {
                return false;
            }
        } catch (final MalformedURLException ex) {
            return false;
        }

        return true;
    }

    /**
     * Runs in the AWT event thread to construct the wizard frame.
     */
    @Override
    public void run() {

        final RomWizardFrame frame = new RomWizardFrame(this);
        frame.begin();
    }
}
