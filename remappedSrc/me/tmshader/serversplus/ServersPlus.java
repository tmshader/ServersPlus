package me.tmshader.serversplus;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class ServersPlus implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();
    public static Path configPath = FabricLoader.getInstance().getConfigDir().resolve("ServersPlus/settings.json");
    public static Path configDir = FabricLoader.getInstance().getConfigDir().resolve("ServersPlus/");
    public static ConfigurationNode config;

    @Override
    public void onInitialize() {
        File dir = new File(configDir.toString());
        File file = new File(configPath.toString());

        if (!dir.exists()) dir.mkdirs();

        if (!file.exists()) {
            try {
                file.createNewFile();
                Files.copy(
                        Objects.requireNonNull(
                                getClass().getResourceAsStream("/assets/serversplus/default.json")
                        ),
                        configPath,
                        StandardCopyOption.REPLACE_EXISTING
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        loadConfig();
        LOGGER.info("ServersPlus Initialised");
    }

    public static void loadConfig() {
        final GsonConfigurationLoader loader = GsonConfigurationLoader.builder()
                .path(ServersPlus.configPath)
                .build();

        try {
            ServersPlus.config = loader.load();
        } catch (ConfigurateException e) {
            e.printStackTrace();
        }
    }
}
