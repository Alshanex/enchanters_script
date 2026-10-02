package net.alshanex.enchanters_script;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * The mod's settings, read once at startup from config/enchanters_script.properties.
 */
public final class EnchantersConfig {
    private static final String FILE_NAME = Constants.MOD_ID + ".properties";

    private static final String HOVER_HINTS = "hover_hints";
    private static final String BOOK_DECIPHERING = "book_deciphering";
    private static final String REVEAL_TIME_MULTIPLIER = "reveal_time_multiplier";
    private static final String WRITING_TIME_MULTIPLIER = "writing_time_multiplier";

    private static final double MIN_MULTIPLIER = 0.5;
    private static final double MAX_MULTIPLIER = 10.0;

    private static final String HEADER = """
            # Enchanter's Script settings.
            # Changes apply after restarting the game or the server.
            """;

    // Every setting with its explanation, in file order. Missing ones are appended to existing files.
    private static final Map<String, String> SETTINGS = new LinkedHashMap<>();

    static {
        SETTINGS.put(HOVER_HINTS, """

                # In the writing minigame, hovering a key shows its letter
                # if the player has already learned it. true or false.
                hover_hints=true
                """);
        SETTINGS.put(BOOK_DECIPHERING, """

                # Enchanted books from loot, fishing and trades arrive ciphered and must be deciphered before use.
                # false makes every book work as in vanilla.
                book_deciphering=true
                """);
        SETTINGS.put(REVEAL_TIME_MULTIPLIER, """

                # How long the name stays visible in the writing minigame.
                # 1.0 is normal, 2.0 doubles it. From 0.5 to 10.
                reveal_time_multiplier=1.0
                """);
        SETTINGS.put(WRITING_TIME_MULTIPLIER, """

                # How long the player has to write the name back, at the table and on books.
                # 1.0 is normal, 2.0 doubles it. From 0.5 to 10.
                writing_time_multiplier=1.0
                """);
    }

    private static boolean hoverHints = true;
    private static boolean bookDeciphering = true;
    private static boolean syncedBookDeciphering = true;
    private static double revealTimeMultiplier = 1.0;
    private static double writingTimeMultiplier = 1.0;

    private EnchantersConfig() {
    }

    public static void load(Path configDir) {
        Path file = configDir.resolve(FILE_NAME);
        try {
            Files.createDirectories(configDir);
            if (Files.notExists(file)) {
                Files.writeString(file, HEADER);
            }

            Properties properties = new Properties();
            try (Reader reader = Files.newBufferedReader(file)) {
                properties.load(reader);
            }

            // Settings the file doesn't have yet, including all of them in a new file,
            // are added with their explanations; existing values are never touched
            StringBuilder missing = new StringBuilder();
            for (Map.Entry<String, String> setting : SETTINGS.entrySet()) {
                if (!properties.containsKey(setting.getKey())) {
                    missing.append(setting.getValue());
                }
            }
            if (!missing.isEmpty()) {
                Files.writeString(file, missing, StandardOpenOption.APPEND);
            }

            hoverHints = readBoolean(properties, HOVER_HINTS, true);
            bookDeciphering = readBoolean(properties, BOOK_DECIPHERING, true);
            revealTimeMultiplier = readMultiplier(properties, REVEAL_TIME_MULTIPLIER);
            writingTimeMultiplier = readMultiplier(properties, WRITING_TIME_MULTIPLIER);
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

    /**
     * A multiplier between 0.5 and 10. Out-of-range values are clamped; invalid ones use 1.
     */
    private static double readMultiplier(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            return 1.0;
        }

        double parsed;
        try {
            parsed = Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            Constants.LOG.warn("Setting {} should be a number, but is '{}'; using 1.0", key, value);
            return 1.0;
        }

        // "NaN" parses as a number but isn't one; comparisons with it are always false
        if (Double.isNaN(parsed)) {
            Constants.LOG.warn("Setting {} should be a number, but is '{}'; using 1.0", key, value);
            return 1.0;
        }
        if (parsed < MIN_MULTIPLIER || parsed > MAX_MULTIPLIER) {
            double clamped = Math.max(MIN_MULTIPLIER, Math.min(MAX_MULTIPLIER, parsed));
            Constants.LOG.warn("Setting {} should be between {} and {}, but is {}; using {}",
                    key, MIN_MULTIPLIER, MAX_MULTIPLIER, parsed, clamped);
            return clamped;
        }
        return parsed;
    }

    public static boolean hoverHints() {
        return hoverHints;
    }

    /**
     * Whether books are ciphered. The client uses the server's setting, received when joining.
     */
    public static boolean bookDeciphering(boolean clientSide) {
        return clientSide ? syncedBookDeciphering : bookDeciphering;
    }

    public static void setSyncedBookDeciphering(boolean enabled) {
        syncedBookDeciphering = enabled;
    }

    public static double revealTimeMultiplier() {
        return revealTimeMultiplier;
    }

    public static double writingTimeMultiplier() {
        return writingTimeMultiplier;
    }
}