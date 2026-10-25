package cn.shumoon.cropsMaxGrowthHeight.configuration;

import cn.shumoon.cropsMaxGrowthHeight.CropsMaxGrowthHeight;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class Configuration {
    public static final int MIN_MAX_HEIGHT = 1;
    public static final int MAX_MAX_HEIGHT = 512;
    public static final int DEFAULT_MAX_HEIGHT = 256;

    public static final int DEFAULT_SUGAR_CANE_HEIGHT = 4;
    public static final int DEFAULT_BAMBOO_HEIGHT = 16;

    /**
     * Maximum total height of one sugar cane column (vanilla is 3).
     */
    public int sugarCaneMaxHeight = DEFAULT_MAX_HEIGHT;

    /**
     * Maximum total height of one bamboo column (vanilla is 16).
     */
    public int bambooMaxHeight = DEFAULT_MAX_HEIGHT;

    private static Path configPath() {
        return Path.of("config", CropsMaxGrowthHeight.MOD_ID + ".toml");
    }

    public static Configuration load() {
        Path path = configPath();
        try {
            if (Files.exists(path)) {
                Map<String, String> values = parseToml(Files.readString(path));
                Configuration config = new Configuration();
                config.sugarCaneMaxHeight = intValue(values, "sugarCaneMaxHeight", config.sugarCaneMaxHeight);
                config.bambooMaxHeight = intValue(values, "bambooMaxHeight", config.bambooMaxHeight);
                config.clamp();
                return config;
            }
        } catch (Exception e) {
            CropsMaxGrowthHeight.LOGGER.error("Failed to load config {}, using defaults", path, e);
        }

        Configuration config = new Configuration();
        config.save();
        return config;
    }

    public void save() {
        Path path = configPath();
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, toToml());
        } catch (Exception e) {
            CropsMaxGrowthHeight.LOGGER.error("Failed to save config {}", path, e);
        }
    }

    private String toToml() {
        StringBuilder toml = new StringBuilder();
        toml.append("# Configuration of ").append(CropsMaxGrowthHeight.MOD_ID).append('\n');
        toml.append("# Allowed range for every value: ").append(MIN_MAX_HEIGHT).append('-').append(MAX_MAX_HEIGHT).append('\n');
        toml.append('\n');
        toml.append("# Maximum total height of one sugar cane column (vanilla is 3)\n");
        toml.append("sugarCaneMaxHeight = ").append(sugarCaneMaxHeight).append('\n');
        toml.append('\n');
        toml.append("# Maximum total height of one bamboo column (vanilla is 16)\n");
        toml.append("bambooMaxHeight = ").append(bambooMaxHeight).append('\n');
        return toml.toString();
    }

    /**
     * Reads the key/value pairs of the top level TOML table, comments and other tables are skipped.
     */
    private static Map<String, String> parseToml(String content) {
        Map<String, String> values = new LinkedHashMap<>();
        boolean inRootTable = true;

        for (String line : content.split("\\R")) {
            String entry = stripComment(line).trim();
            if (entry.isEmpty()) {
                continue;
            }
            if (entry.charAt(0) == '[') {
                inRootTable = false;
                continue;
            }
            if (!inRootTable) {
                continue;
            }

            int separator = entry.indexOf('=');
            if (separator < 0) {
                CropsMaxGrowthHeight.LOGGER.warn("Ignoring malformed config line \"{}\"", entry);
                continue;
            }

            String key = unquote(entry.substring(0, separator).trim());
            String value = entry.substring(separator + 1).trim();
            if (key.isEmpty() || value.isEmpty()) {
                CropsMaxGrowthHeight.LOGGER.warn("Ignoring malformed config line \"{}\"", entry);
                continue;
            }
            values.put(key, unquote(value));
        }
        return values;
    }

    /**
     * Removes a trailing comment, a '#' inside a quoted value is kept.
     */
    private static String stripComment(String line) {
        boolean quoted = false;
        char quote = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (quoted) {
                if (c == quote) {
                    quoted = false;
                }
            } else if (c == '"' || c == '\'') {
                quoted = true;
                quote = c;
            } else if (c == '#') {
                return line.substring(0, i);
            }
        }
        return line;
    }

    private static String unquote(String text) {
        if (text.length() >= 2 && (text.charAt(0) == '"' || text.charAt(0) == '\'') && text.charAt(text.length() - 1) == text.charAt(0)) {
            return text.substring(1, text.length() - 1);
        }
        return text;
    }

    private static int intValue(Map<String, String> values, String name, int fallback) {
        String raw = values.get(name);
        if (raw == null) {
            CropsMaxGrowthHeight.LOGGER.warn("Missing config entry {}, using default {}", name, fallback);
            return fallback;
        }
        try {
            return Integer.parseInt(raw.replace("_", ""));
        } catch (NumberFormatException e) {
            CropsMaxGrowthHeight.LOGGER.warn("Config entry {} is not an integer ({}), using default {}", name, raw, fallback);
            return fallback;
        }
    }

    private void clamp() {
        sugarCaneMaxHeight = clampValue("sugarCaneMaxHeight", sugarCaneMaxHeight, MIN_MAX_HEIGHT, MAX_MAX_HEIGHT);
        bambooMaxHeight = clampValue("bambooMaxHeight", bambooMaxHeight, MIN_MAX_HEIGHT, MAX_MAX_HEIGHT);
    }

    private static int clampValue(String name, int value, int min, int max) {
        if (value < min || value > max) {
            int clamped = Math.clamp(value, min, max);
            CropsMaxGrowthHeight.LOGGER.warn("{} {} is out of range ({}-{}), clamped to {}",
                    name, value, min, max, clamped);
            return clamped;
        }
        return value;
    }
}
