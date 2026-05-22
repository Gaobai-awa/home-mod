package net.homemod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.homemod.command.*;
import net.homemod.config.HomeConfig;
import net.homemod.util.CountdownTask;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HomeMod implements ModInitializer {
    public static final String MOD_ID = "homemod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final com.google.gson.Gson GSON = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
    public static Path CONFIG_DIR;
    public static HomeConfig CONFIG;

    @Override
    public void onInitialize() {
        CONFIG_DIR = Paths.get("config", MOD_ID);
        try {
            Files.createDirectories(CONFIG_DIR);
        } catch (IOException e) {
            LOGGER.error("Failed to create config directory", e);
        }

        CONFIG = HomeConfig.load();

        LOGGER.info("Home_mod loaded! Commands: /sethome, /home, /back, /listhomes, /delhome");
        registerCommands();
        registerTick();
    }

    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            HomeCommand.register(dispatcher);
            SetHomeCommand.register(dispatcher);
            BackCommand.register(dispatcher);
            ListHomesCommand.register(dispatcher);
            DelHomeCommand.register(dispatcher);
            LOGGER.info("All home-mod commands registered");
        });
    }

    private void registerTick() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            CountdownTask.tickAll(server);
        });
    }
}