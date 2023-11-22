package jwabbit.gui;

import jwabbit.Launcher;
import jwabbit.gui.wizard.RomWizard;
import jwabbit.iface.Calc;
import jwabbit.iface.Globals;
import jwabbit.utilities.FileUtilities;
import jwabbit.utilities.Gif;

import javax.swing.JOptionPane;
import java.io.File;

/**
 * Manages a list of "CalcUI" objects and the parsed command line arguments. Each main window may have several frames
 * that it manages, all associated with a single calculator. The module also contains functions to look up a main window
 * based on one if its owned frames, and to search for any open windows of a particular type.
 * <p>
 * WABBITEMU SOURCE: gui/gui.h, "CWabbitemuModule" class.
 */
public final class WabbitemuModule {

    /** WABBITEMU SOURCE: gui/gui.h, "_Module" global. */
    public static final WabbitemuModule MODULE = new WabbitemuModule();

    /** The parsed command-line arguments. */
    private final ParsedCmdArgs parsedArgs;

    /**
     * Constructs a new {@code WabbitemuModule}.
     */
    private WabbitemuModule() {

        super();

        this.parsedArgs = new ParsedCmdArgs();
    }

    /**
     * Creates a new "CalcGui" object associated with a calculator and adds it to the list of active main windows.
     * <p>
     * WABBITEMU SOURCE: gui/gui.c, "CWabbitemuModule::CreateNewFrame" method.
     *
     * @param calc       the calculator
     * @param buildFrame true to construct a {@code MainFrame} and install the calculator panel in the frame; false to
     *                   simply construct the calculator panel, so it can be hosted within another component or frame
     * @return the calculator GUI object, null if the object could not be created
     */
    public static CalcUI createCalcUI(final Calc calc, final boolean buildFrame) {

        final CalcUI theCalcUI = buildFrame ? CalcUI.guiFrame(calc) : CalcUI.guiPanel(calc);

        if (theCalcUI == null) {
            JOptionPane.showMessageDialog(null, "Failed to create main window", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        return theCalcUI;
    }

    /**
     * WABBITEMU SOURCE: gui/gui.c, "CWabbitemuModule::ParseCommandLine" method.
     *
     * @param args the command line arguments
     */
    private void parseCommandLine(final String... args) {

        this.parsedArgs.parseCommandLineArgs(args);
    }

    /**
     * WABBITEMU SOURCE: gui/gui.c, "CWabbitemuModule::PreMessageLoop" method.
     */
    private void preMessageLoop() {

        if (this.parsedArgs.isNoCreateCalc()) {
            return;
        }

        // Create our appdata folder
        final String appData = FileUtilities.getStorageString();
        final File appDataFile = new File(appData);
        if (!appDataFile.exists()) {
            if (!appDataFile.mkdirs()) {
                JOptionPane.showMessageDialog(null, "Unable to create appdata folder", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        Globals.get().setAutoTurnOn("true".equalsIgnoreCase(
                Registry.asString(Registry.queryWabbitKey("auto_turn_on"))));

        // this is here,so we get our load_files_first setting
        final Object obj = Registry.queryWabbitKey("load_files_first");
        final boolean loadFilesFirst = obj instanceof Boolean && ((Boolean) obj).booleanValue();
        Globals.get().setNewCalcOnLoadFiles(loadFilesFirst || this.parsedArgs.isForceNewInstance());

        Gui.silentMode = this.parsedArgs.isSilentMode();
    }

    /**
     * WABBITEMU SOURCE: gui/gui.c, "CWabbitemuModule::RunMessageLoop" method.
     */
    private static void runMessageLoop() {

        if (Gui.createCalcRegisterEvents(true) == null) {

            final Calc calc = new Calc();
            if (RomWizard.doWizardSheet(calc)) {

                final int slot = Launcher.addCalc(calc, true);
                if (slot != -1) {
                    final CalcUI theCalcUI = Launcher.getCalcUI(slot);
                    Gui.registerEvents(calc, theCalcUI);
                    Launcher.getCalcThread(slot).start();
                }
            }
        }

        // Java handles the main loop - we just need to wait until the last calculator is closed, then leave
        while (Launcher.getNumCalcs() > 0) {
            try {
                Thread.sleep(500L);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * WABBITEMU SOURCE: gui/gui.c, "CWabbitemuModule::PostMessageLoop" method.
     */
    private static void postMessageLoop() {

        // Make sure the GIF has terminated
        if (Gif.get().gifWriteState == Gif.GIF_FRAME) {
            Gif.get().gifWriteState = Gif.GIF_END;
            Gif.get().handleScreenshot(null);
        }
    }

    /**
     * WABBITEMU SOURCE: gui/gui.h, "CWabbitemuModule::GetParsedCmdArgs" method.
     *
     * @return the ParsedCmdArgs
     */
    ParsedCmdArgs getParsedCmdArgs() {

        return this.parsedArgs;
    }

    /**
     * Run the program.
     *
     * @param args command line arguments
     */
    public void winMain(final String... args) {

        parseCommandLine(args);

        preMessageLoop();
        runMessageLoop();
        postMessageLoop();
    }
}
