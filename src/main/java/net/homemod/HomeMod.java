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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HomeMod implements ModInitializer {
    public static final String MOD_ID = "homemod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final com.google.gson.Gson GSON = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
    public static Path CONFIG_DIR;
    public static HomeConfig CONFIG;

    // Track player health for death detection
    private static final Map<UUID, Float> PREV_HEALTH = new HashMap<>();

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
        });
    }

    private void registerTick() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            CountdownTask.tickAll(server);

            // Death detection: check when health drops from >0 to 0
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                UUID uuid = player.getUuid();
                float currentHealth = player.getHealth();
                Float prev = PREV_HEALTH.get(uuid);

                if (prev != null && prev > 0.0f && currentHealth <= 0.0f) {
                    // Player just died
                    HomeConfig.HomeData deathPos = new HomeConfig.HomeData(
                        player.getX(), player.getY(), player.getZ(),
                        player.getYaw(), player.getPitch(),
                        player.getWorld().getRegistryKey().getValue().toString()
                    );
                    CONFIG.setLastPosition(uuid.toString(), deathPos);
                    LOGGER.info("Recorded death position for {}", player.getName().getString());
                }

                PREV_HEALTH.put(uuid, currentHealth);
            }
        });
    }
}