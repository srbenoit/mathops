package jwabbit.gui;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.JWCoreConstants;
import jwabbit.log.LoggedObject;
import jwabbit.utilities.FileUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * NOTE: I stripped all the registry-based settings out.
 */
public final class Registry {

    /** WABBITEMU SOURCE: gui/registry.c, "verString" global. */
    private static final String VER_STRING = "1.8.2.26";

    /** WABBITEMU SOURCE: gui/registry.c, "regDefaults" global array. */
    private static final RegDef[] REG_DEFAULTS =
            {new RegDef("cutout", RegDef.BOOLEAN, Boolean.FALSE),
                    new RegDef("skin", RegDef.BOOLEAN, Boolean.TRUE),
                    new RegDef("alphablend_lcd", RegDef.BOOLEAN, Boolean.TRUE),
                    new RegDef("version", RegDef.STRING, VER_STRING),
                    new RegDef("rom_path", RegDef.STRING, "z.rom"),
                    new RegDef("shades", RegDef.DWORD, Integer.valueOf(6)),
                    new RegDef("gif_path", RegDef.STRING, "wabbitemu.gif"),
                    new RegDef("gif_autosave", RegDef.BOOLEAN, Boolean.FALSE),
                    new RegDef("gif_useinc", RegDef.BOOLEAN, Boolean.FALSE),
                    new RegDef("gif_framerate", RegDef.DWORD, Integer.valueOf(4)),
                    new RegDef("gif_gray_size", RegDef.DWORD, Integer.valueOf(2)),
                    new RegDef("gif_color_size", RegDef.DWORD, Integer.valueOf(1)),
                    new RegDef("lcd_mode", RegDef.DWORD, Integer.valueOf(0)),
                    new RegDef("lcd_freq", RegDef.DWORD, Integer.valueOf(JWCoreConstants.FPS)),
                    new RegDef("screen_scale", RegDef.DWORD, Integer.valueOf(2)),
                    new RegDef("skin_scale", RegDef.STRING, "0.0"),
                    new RegDef("faceplate_color", RegDef.DWORD, Integer.valueOf(0x838587)),
                    new RegDef("exit_save_state", RegDef.BOOLEAN, Boolean.TRUE),
                    new RegDef("load_files_first", RegDef.BOOLEAN, Boolean.FALSE),
                    new RegDef("do_backups", RegDef.BOOLEAN, Boolean.FALSE),
                    new RegDef("sync_cores", RegDef.BOOLEAN, Boolean.FALSE),
                    new RegDef("num_accel", RegDef.DWORD, Integer.valueOf(6)),
                    new RegDef("always_on_top", RegDef.BOOLEAN, Boolean.FALSE),
                    new RegDef("tios_debug", RegDef.BOOLEAN, Boolean.TRUE),
                    new RegDef("custom_skin", RegDef.BOOLEAN, Boolean.FALSE),
                    new RegDef("skin_path", RegDef.STRING, "TI-83P.png"),
                    new RegDef("keymap_path", RegDef.STRING, "TI-83PKeymap.png"),
                    new RegDef("startX", RegDef.DWORD, Integer.valueOf(-1)),
                    new RegDef("startY", RegDef.DWORD, Integer.valueOf(-1)),
                    new RegDef("break_on_exe_violation", RegDef.BOOLEAN, Boolean.FALSE),
                    new RegDef("break_on_invalid_flash", RegDef.BOOLEAN, Boolean.FALSE),
                    new RegDef("auto_turn_on", RegDef.BOOLEAN, Boolean.FALSE),
                    new RegDef("num_backup_per_sec", RegDef.DWORD, Integer.valueOf(2)),
                    new RegDef("ram_version", RegDef.DWORD, Integer.valueOf(0)),
                    new RegDef("lcd_delay", RegDef.DWORD, Integer.valueOf(60)),
                    new RegDef("check_updates", RegDef.BOOLEAN, Boolean.TRUE),
                    new RegDef("show_whats_new", RegDef.BOOLEAN, Boolean.FALSE),
                    // Debugger stuff
                    new RegDef("CPU Status", RegDef.DWORD, Integer.valueOf(0)),
                    new RegDef("Disp_Type", RegDef.DWORD, Integer.valueOf(0)),
                    new RegDef("Display", RegDef.DWORD, Integer.valueOf(0)),
                    new RegDef("Flags", RegDef.DWORD, Integer.valueOf(0)),
                    new RegDef("Interrupts", RegDef.DWORD, Integer.valueOf(0)),
                    new RegDef("Keyboard", RegDef.DWORD, Integer.valueOf(0)),
                    new RegDef("Mem0", RegDef.DWORD, Integer.valueOf(0)),
                    new RegDef("Mem1", RegDef.DWORD, Integer.valueOf(0)),
                    new RegDef("Mem2", RegDef.DWORD, Integer.valueOf(0)),
                    new RegDef("Mem3", RegDef.DWORD, Integer.valueOf(0)),
                    new RegDef("Mem4", RegDef.DWORD, Integer.valueOf(0)),
                    new RegDef("Mem5", RegDef.DWORD, Integer.valueOf(0)),
                    new RegDef("Memory Map", RegDef.DWORD, Integer.valueOf(0)),
                    new RegDef("MemSelIndex", RegDef.DWORD, Integer.valueOf(0)),
                    new RegDef("NumDisasmPane", RegDef.DWORD, Integer.valueOf(0)),
                    new RegDef("NumMemPane", RegDef.DWORD, Integer.valueOf(0)),
                    new RegDef("NumWatchKey", RegDef.DWORD, Integer.valueOf(0)),
                    new RegDef("Registers", RegDef.DWORD, Integer.valueOf(0)),
                    new RegDef("WatchLocsKey", RegDef.STRING, ""),};

    /** A Properties object with the default values. */
    private static final Properties DEF_PROPERTIES;

    static {
        DEF_PROPERTIES = new Properties();

        for (final RegDef regDefault : REG_DEFAULTS) {

            switch (regDefault.getType()) {
                case RegDef.DWORD:
                    DEF_PROPERTIES.setProperty(regDefault.getValueName(), regDefault.getIntegerValue().toString());
                    break;
                case RegDef.BOOLEAN:
                    DEF_PROPERTIES.setProperty(regDefault.getValueName(), regDefault.getBooleanValue().toString());
                    break;
                case RegDef.STRING:
                    DEF_PROPERTIES.setProperty(regDefault.getValueName(), regDefault.getStringValue());
                    break;
                default:
                    LoggedObject.LOG.warning("Unsupported preference type: " + regDefault.getType());
                    break;
            }
        }
    }

    /**
     * Constructs a new {@code Registry}.
     */
    public Registry() {

        super();
    }

    /**
     * Gets the registry default object for a particular preference name.
     *
     * <p>
     * WABBITEMU SOURCE: gui/registry.c, "GetDefaultData" function.
     *
     * @param name the preference name
     * @return the default value if found; null if not
     */
    private static RegDef getDefaultData(final String name) {

        RegDef def = null;
        for (final RegDef regDefault : REG_DEFAULTS) {
            if (regDefault.getValueName().equals(name)) {
                def = regDefault;
                break;
            }
        }

        return def;
    }

    /**
     * Loads the properties object. If the properties file does not exist, creates it and populates it with default
     * values.
     *
     * @return the loaded properties object
     */
    public static Properties getProperties() {

        final String storePath = FileUtilities.getStorageString();
        final File propsFile = new File(storePath, "wabbitemu.properties");
        final Properties props = new Properties(DEF_PROPERTIES);

        if (!propsFile.exists()) {
            if (propsFile.getParentFile().exists() || propsFile.getParentFile().mkdirs()) {
                // Write out the default properties to make it easier for a user to edit
                try (final FileOutputStream output = new FileOutputStream(propsFile)) {
                    DEF_PROPERTIES.store(output, "Wabbitemu (Java version) settings");
                } catch (final IOException ex) {
                    LoggedObject.LOG.warning("Failed to load properties file.", ex);
                }
            } else {
                LoggedObject.LOG.warning("Failed to create properties file.");
            }
        }

        try (final FileInputStream input = new FileInputStream(propsFile)) {
            props.load(input);
        } catch (final IOException ex) {
            LoggedObject.LOG.warning("Failed to load properties file.", ex);
        }

        return props;
    }

    /**
     * Writes the contents of a properties object to the properties file.
     *
     * @param props the properties file to store
     */
    private static void storeProperties(final Properties props) {

        final String storePath = FileUtilities.getStorageString();
        final File propsFile = new File(storePath, "wabbitemu.properties");

        if (!propsFile.getParentFile().exists()) {
            if (!propsFile.getParentFile().mkdirs()) {
                LoggedObject.LOG.warning("Failed to create properties file parent directory.");
            }
        }

        try (final FileOutputStream output = new FileOutputStream(propsFile)) {
            props.store(output, "Wabbitemu (Java version) settings");
        } catch (final IOException ex) {
            LoggedObject.LOG.warning("Failed to write properties file.", ex);
        }
    }

    /**
     * Retrieves an integer preference value associated with a name.
     *
     * <p>
     * WABBITEMU SOURCE: gui/registry.c, "QueryWabbitKey" function.
     *
     * @param name the preference name
     * @return the value
     */
    static Object queryWabbitKey(final String name) {

        final RegDef defValue = getDefaultData(name);
        final Properties props = getProperties();

        final String valueStr = props.getProperty(name);
        Object value;

        switch (defValue.getType()) {
            case RegDef.DWORD:
                try {
                    value = Integer.valueOf(valueStr);
                } catch (final NumberFormatException ex) {
                    LoggedObject.LOG.warning("Integer property value '", valueStr, "' not readable as integer");
                    value = null;
                }
                break;
            case RegDef.BOOLEAN:
                value = Boolean.valueOf(valueStr);
                break;
            case RegDef.STRING:
                value = valueStr;
                break;
            default:
                value = null;
        }

        return value;
    }

    /**
     * If a given object is a String, returns that object cast to String. If not, returns null.
     *
     * @param obj the object
     * @return the string
     */
    static String asString(final Object obj) {

        return obj == null ? null : obj.toString();
    }

    /**
     * If a given object is a Boolean, returns that object's boolean value. If not, returns false.
     *
     * @param obj the object
     * @return the boolean value
     */
    static boolean asBoolean(final Object obj) {

        return obj instanceof Boolean && ((Boolean) obj).booleanValue();
    }

    /**
     * If a given object is an Integer, returns that object's int value. If not, returns a provided default value.
     *
     * @param obj the object
     * @param def the value to return if object is not an Integer
     * @return the int value
     */
    static int asInteger(final Object obj, final int def) {

        return obj instanceof Integer ? ((Integer) obj).intValue() : def;
    }

    /**
     * Stores a preference value associated with a name.
     *
     * <p>
     * WABBITEMU SOURCE: gui/registry.c, "SaveWabbitKey" function.
     *
     * @param name  the name
     * @param value the value
     */
    public static void saveWabbitKey(final String name, final Object value) {

        final Properties props = getProperties();
        props.setProperty(name, value.toString());
        storeProperties(props);
    }
}
