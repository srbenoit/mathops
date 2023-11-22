package jwabbit.utilities;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

/**
 * File utilities.
 */
public enum FileUtilities {
    ;

    /**
     * WABBITEMU SOURCE: utilities/fileutilities.c, "SaveFile" function.
     *
     * @param filename         on input, element [0] contains the default selected file; on output, [0] contains the
     *                         selected file
     * @param filterDesc       a file extension filter description ([0] is default)
     * @param filterExtensions a list of file extensions
     * @param title            the dialog title
     * @return 0 if file selected, 1 if canceled
     */
    public static int saveFile(final String[] filename, final String[] filterDesc, final String[][] filterExtensions,
                               final String title) {

        final JFileChooser chooser = new JFileChooser();

        chooser.setDialogTitle(title);
        if (filename[0] != null) {
            chooser.setSelectedFile(new File(filename[0]));
        }
        if (filterDesc != null) {
            final int len = filterDesc.length;
            for (int i = 0; i < len; ++i) {
                final FileNameExtensionFilter filter = new FileNameExtensionFilter(filterDesc[i], filterExtensions[i]);
                chooser.addChoosableFileFilter(filter);
            }
            chooser.setFileFilter(chooser.getChoosableFileFilters()[0]);
        }
        chooser.setAcceptAllFileFilterUsed(true);

        final int result = chooser.showSaveDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            filename[0] = chooser.getSelectedFile().getAbsolutePath();
            return 0;
        }

        return 1;
    }

    /**
     * WABBITEMU SOURCE: utilities/fileutilities.c, "GetStorageString" function.
     *
     * @return the storage directory
     */
    public static String getStorageString() {

        return System.getProperty("user.home") + "/.wabbitemu";
    }
}
