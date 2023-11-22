package jwabbit.gui.wizard;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.EnumCalcModel;

/**
 * Interface for a class that runs a wizard.
 */
interface IWizardRunner {

    /**
     * Informs the runner that the wizard has been completed and closed, either by finishing or by canceling.
     *
     * @param isFinished true if finished; false if canceled
     */
    void complete(boolean isFinished);

    /**
     * Sets the flag that indicates whether the user browsed for a ROM.
     *
     * @param browsed true if user browsed for a ROM
     */
    void setBrowsedForRom(boolean browsed);

    /**
     * Sets the path of the ROM for which the user browsed.
     *
     * @param thePath the path
     */
    void setBrowsePath(String thePath);

    /**
     * Sets the flag that indicates whether the user created a ROM.
     *
     * @param created true if user created a ROM
     */
    void setCreatedRom(boolean created);

    /**
     * Sets the calculator model selected.
     *
     * @param theModel the model
     */
    void setModel(EnumCalcModel theModel);

    /**
     * Sets the flag that indicates whether the user downloaded an OS.
     *
     * @param downloaded true if user downloaded an OS
     */
    void setDownloadOs(boolean downloaded);
}
