package net.homemod.command;

import net.homemod.HomeMod;
import net.homemod.config.HomeConfig;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HomeCommand {

    // Track last position before teleport (for /back)
    private static final Map<UUID, HomeConfig.HomeData> lastPositions = new HashMap<>();

    // Track pending home teleport for particle respawn
    // key = player UUID, value = tick of teleport
    private static final Map<UUID, Integer> homeTeleportTick = new HashMap<>();
    private static final Map<UUID, HomeConfig.HomeData> pendingHome = new HashMap<>();

    public static int execute(ServerCommandSource source, String name) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendFeedback(() -> Text.literal("\u00a7c[Home] Only players can use this command."), false);
            return 0;
        }

        String uuidStr = player.getUuid().toString();
        HomeConfig.HomeData home = HomeMod.CONFIG.getHome(uuidStr, name);

        if (home == null) {
            player.sendMessage(Text.literal(
                "\u00a7c[Home] Home \u00a7e'" + name + "'\u00a7c does not exist. Use \u00a7f/sethome\u00a7c first."
            ), false);
            return 0;
        }

        // Save current position for /back
        HomeConfig.HomeData currentPos = new HomeConfig.HomeData(
            player.getX(), player.getY(), player.getZ(),
            player.getYaw(), player.getPitch(),
            player.getWorld().getRegistryKey().getValue().toString()
        );
        lastPositions.put(player.getUuid(), currentPos);

        // Resolve destination world
        MinecraftServer server = player.getServer();
        RegistryKey<World> destKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.tryParse(home.world));
        ServerWorld destWorld = server.getWorld(destKey);
        if (destWorld == null) {
            destWorld = server.getOverworld();
        }

        // Teleport
        player.teleport(destWorld, home.x, home.y, home.z, home.yaw, home.pitch);

        // Particle burst on arrival
        Vec3d homePos = new Vec3d(home.x, home.y, home.z);
        spawnHomeArrivalEffect(destWorld, homePos);

        // Mark pending home for movement detection (persist for 8 seconds = ~160 ticks)
        pendingHome.put(player.getUuid(), new HomeConfig.HomeData(home.x, home.y, home.z, home.yaw, home.pitch, home.world));
        homeTeleportTick.put(player.getUuid(), (int) (server.getTicks() & 0x7FFFFFFF));

        player.sendMessage(Text.literal(
            "\u00a7a[Home] Teleporting to \u00a7e'" + name + "'\u00a7a..."
        ), false);

        return 1;
    }

    private static void spawnHomeArrivalEffect(ServerWorld world, Vec3d pos) {
        // Golden sparkle rising layers
        for (int i = 0; i < 3; i++) {
            world.spawnParticles(ParticleTypes.ENCHANTED_HIT,
                pos.x, pos.y + 1.0 + i * 0.5, pos.z,
                15, 0.4, 0.4, 0.4, 0.02);
        }
        // White sparkle burst
        world.spawnParticles(ParticleTypes.END_ROD,
            pos.x, pos.y + 1.0, pos.z,
            12, 0.3, 0.5, 0.3, 0.04);
        // Heart particles
        world.spawnParticles(ParticleTypes.HEART,
            pos.x, pos.y + 1.5, pos.z,
            6, 0.2, 0.2, 0.2, 0.01);
    }

    // Called on every PlayerMove event; respawns gentle particles if player stayed at home
    public static void onPlayerTick(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        HomeConfig.HomeData pending = pendingHome.get(uuid);
        if (pending == null) return;

        MinecraftServer server = player.getServer();
        int currentTick = (int) (server.getTicks() & 0x7FFFFFFF);
        int startTick = homeTeleportTick.getOrDefault(uuid, currentTick);

        // Expire after 160 ticks (8 seconds at 20tps)
        if (currentTick - startTick > 160) {
            pendingHome.remove(uuid);
            homeTeleportTick.remove(uuid);
            return;
        }

        double dx = Math.abs(player.getX() - pending.x);
        double dy = Math.abs(player.getY() - pending.y);
        double dz = Math.abs(player.getZ() - pending.z);

        if (dx > 0.15 || dy > 0.15 || dz > 0.15) {
            // Player moved — stop particle trail
            pendingHome.remove(uuid);
            homeTeleportTick.remove(uuid);
        } else {
            // Still at home — respawn gentle particles every 30 ticks (~1.5s)
            int elapsed = currentTick - startTick;
            if (elapsed > 20 && elapsed % 30 < 3) {
                if (!player.getWorld().isClient) {
                    ServerWorld world = (ServerWorld) player.getWorld();
                    Vec3d pos = new Vec3d(pending.x, pending.y, pending.z);
                    world.spawnParticles(ParticleTypes.END_ROD,
                        pos.x, pos.y + 1.0, pos.z,
                        4, 0.12, 0.25, 0.12, 0.008);
                    world.spawnParticles(ParticleTypes.HEART,
                        pos.x, pos.y + 1.5, pos.z,
                        2, 0.08, 0.1, 0.08, 0.005);
                }
            }
        }
    }

    public static HomeConfig.HomeData getLastPosition(UUID uuid) {
        return lastPositions.get(uuid);
    }

    public static void setLastPosition(UUID uuid, HomeConfig.HomeData pos) {
        lastPositions.put(uuid, pos);
    }
}
