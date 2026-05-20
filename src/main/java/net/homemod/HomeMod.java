package net.homemod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.homemod.command.HomeCommand;
import net.homemod.config.HomeConfig;
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
        registerCallbacks();
    }

    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // /sethome [name]
            dispatcher.register(
                net.minecraft.server.command.CommandManager.literal("sethome")
                    .then(net.minecraft.server.command.CommandManager.argument("name", com.mojang.brigadier.arguments.StringArgumentType.word())
                        .executes(ctx -> {
                            String name = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "name");
                            return net.homemod.command.SetHomeCommand.execute(ctx.getSource(), name);
                        })
                    )
                    .executes(ctx -> net.homemod.command.SetHomeCommand.execute(ctx.getSource(), "home"))
            );

            // /home [name]
            dispatcher.register(
                net.minecraft.server.command.CommandManager.literal("home")
                    .then(net.minecraft.server.command.CommandManager.argument("name", com.mojang.brigadier.arguments.StringArgumentType.word())
                        .executes(ctx -> {
                            String name = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "name");
                            return net.homemod.command.HomeCommand.execute(ctx.getSource(), name);
                        })
                    )
                    .executes(ctx -> net.homemod.command.HomeCommand.execute(ctx.getSource(), "home"))
            );

            // /back
            dispatcher.register(
                net.minecraft.server.command.CommandManager.literal("back")
                    .executes(ctx -> net.homemod.command.BackCommand.execute(ctx.getSource()))
            );

            // /listhomes
            dispatcher.register(
                net.minecraft.server.command.CommandManager.literal("listhomes")
                    .executes(ctx -> net.homemod.command.ListHomesCommand.execute(ctx.getSource()))
            );

            // /delhome <name>
            dispatcher.register(
                net.minecraft.server.command.CommandManager.literal("delhome")
                    .then(net.minecraft.server.command.CommandManager.argument("name", com.mojang.brigadier.arguments.StringArgumentType.word())
                        .executes(ctx -> {
                            String name = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "name");
                            return net.homemod.command.DelHomeCommand.execute(ctx.getSource(), name);
                        })
                    )
            );

            LOGGER.info("All home-mod commands registered");
        });
    }

    private void registerCallbacks() {
        // Poll for home particle respawn each server tick
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                HomeCommand.onPlayerTick(player);
            }
        });
    }
}