package net.alshanex.enchanters_script;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * The mod's settings, read once at startup from config/enchanters_script.properties.
 */
public final class EnchantersConfig {
    private static final String FILE_NAME = Constants.MOD_ID + ".properties";
    private static final String HOVER_HINTS = "hover_hints";

    // Written when the file doesn't exist yet, so players can see every setting and what it does
    private static final String DEFAULT_FILE = """
            # Enchanter's Script settings.

            # In the writing minigame, hovering a key shows its letter if the player has already learned it. true or false.
            hover_hints=true
            """;

    private static boolean hoverHints = true;

    private EnchantersConfig() {
    }

    public static void load(Path configDir) {
        Path file = configDir.resolve(FILE_NAME);
        try {
            if (Files.notExists(file)) {
                Files.createDirectories(configDir);
                Files.writeString(file, DEFAULT_FILE);
            }

            Properties properties = new Properties();
            try (Reader reader = Files.newBufferedReader(file)) {
                properties.load(reader);
            }
            hoverHints = readBoolean(properties, HOVER_HINTS, true);
        } catch (IOException e) {
            Constants.LOG.error("Couldn't read {}, using the default settings", file, e);
        }
    }

    private static boolean readBoolean(Properties properties, String key, boolean fallback) {
        String value = properties.getProperty(key);
        if (value == null) {
            return fallback;
        }
        value = value.trim();
        if (value.equalsIgnoreCase("true")) {
            return true;
        }
        if (value.equalsIgnoreCase("false")) {
            return false;
        }
        Constants.LOG.warn("Setting {} should be true or false, but is '{}'; using {}", key, value, fallback);
        return fallback;
    }

    public static boolean hoverHints() {
        return hoverHints;
    }
}
