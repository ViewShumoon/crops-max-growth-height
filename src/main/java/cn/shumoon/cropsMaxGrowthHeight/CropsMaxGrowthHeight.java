package cn.shumoon.cropsMaxGrowthHeight;

import cn.shumoon.cropsMaxGrowthHeight.configuration.Configuration;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CropsMaxGrowthHeight implements ModInitializer {
    public static final String MOD_ID = "crops-max-growth-height";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static Configuration config;
    @Override
    public void onInitialize() {
        LOGGER.info("Initializing {}", MOD_ID);
        config = Configuration.load();
        LOGGER.info("Sugar cane maximum growth height is {}", config.maxHeight);
    }

    public static Configuration getConfig() {
        return config;
    }

}
