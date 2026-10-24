package cn.shumoon.cropsMaxGrowthHeight.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cn.shumoon.cropsMaxGrowthHeight.CropsMaxGrowthHeight;

import java.nio.file.Files;
import java.nio.file.Path;

public class Configuration {
    public static final int DEFAULT_MAX_HEIGHT = 256;
    public static final int MIN_MAX_HEIGHT = 3;
    public static final int MAX_MAX_HEIGHT = 256;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Maximum total height of one sugar cane column (vanilla is 3).
     */
    public int maxHeight = DEFAULT_MAX_HEIGHT;

    private static Path configPath() {
        return Path.of("config", CropsMaxGrowthHeight.MOD_ID + ".json");
    }

    public static Configuration load() {
        Path path = configPath();
        try {
            if (Files.exists(path)) {
                Configuration config = GSON.fromJson(Files.readString(path), Configuration.class);
                if (config != null) {
                    config.clamp();
                    return config;
                }
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
            Files.writeString(path, GSON.toJson(this));
        } catch (Exception e) {
            CropsMaxGrowthHeight.LOGGER.error("Failed to save config {}", path, e);
        }
    }

    private void clamp() {
        if (maxHeight < MIN_MAX_HEIGHT || maxHeight > MAX_MAX_HEIGHT) {
            int clamped = Math.clamp(maxHeight, MIN_MAX_HEIGHT, MAX_MAX_HEIGHT);
            CropsMaxGrowthHeight.LOGGER.warn("maxHeight {} is out of range ({}-{}), clamped to {}",
                    maxHeight, MIN_MAX_HEIGHT, MAX_MAX_HEIGHT, clamped);
            maxHeight = clamped;
        }
    }
}
