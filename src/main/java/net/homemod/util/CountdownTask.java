package net.homemod.util;

import net.homemod.HomeMod;
import net.homemod.config.HomeConfig;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;

public class CountdownTask {

    private static final Map<UUID, CountdownState> pending = new HashMap<>();

    public static class CountdownState {
        public int remainingTicks;
        public double originX, originY, originZ;
        public TeleportTarget target;
        public boolean isBackCommand;
        public boolean cancelled = false;
        public int particleCounter = 0;

        public CountdownState(int ticks, double x, double y, double z, TeleportTarget target, boolean isBack) {
            this.remainingTicks = ticks;
            this.originX = x; this.originY = y; this.originZ = z;
            this.target = target;
            this.isBackCommand = isBack;
        }
    }

    public static class TeleportTarget {
        public double x, y, z;
        public float yaw, pitch;
        public String world;

        public TeleportTarget(double x, double y, double z, float yaw, float pitch, String world) {
            this.x = x; this.y = y; this.z = z;
            this.yaw = yaw; this.pitch = pitch;
            this.world = world;
        }
    }

    public static boolean startCountdown(ServerPlayerEntity player, TeleportTarget target, boolean isBack) {
        UUID uuid = player.getUuid();
        if (pending.containsKey(uuid)) {
            player.sendMessage(Text.literal("\u00a7c[Home] \u4f60\u5df2\u7ecf\u5728\u4f20\u9001\u5012\u8ba1\u65f6\u4e2d\uff01"), false);
            return false;
        }

        if (!isBack) {
            HomeConfig.HomeData lastPos = new HomeConfig.HomeData(
                player.getX(), player.getY(), player.getZ(),
                player.getYaw(), player.getPitch(),
                player.getWorld().getRegistryKey().getValue().toString()
            );
            HomeMod.CONFIG.setLastPosition(uuid.toString(), lastPos);
        }

        pending.put(uuid, new CountdownState(60,
            player.getX(), player.getY(), player.getZ(), target, isBack));

        sendTitle(player,
            Text.literal("\u00a7e\u5373\u5c06\u4f20\u9001..."),
            Text.literal("\u00a77\u8bf7\u4fdd\u6301\u539f\u5730\u4e0d\u52a8"));
        return true;
    }

    public static void tickAll(MinecraftServer server) {
        if (pending.isEmpty()) return;

        Iterator<Map.Entry<UUID, CountdownState>> it = pending.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, CountdownState> entry = it.next();
            UUID uuid = entry.getKey();
            CountdownState state = entry.getValue();

            ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
            if (player == null || !player.isAlive()) {
                it.remove();
                continue;
            }

            // Check movement
            double dx = Math.abs(player.getX() - state.originX);
            double dy = Math.abs(player.getY() - state.originY);
            double dz = Math.abs(player.getZ() - state.originZ);
            if (dx > 0.2 || dy > 0.2 || dz > 0.2) {
                player.sendMessage(Text.literal("\u00a7c[Home] \u4f20\u9001\u53d6\u6d88\u2014\u2014\u4f60\u79fb\u52a8\u4e86\uff01"), false);
                sendTitle(player, Text.literal("\u00a7c\u4f20\u9001\u5931\u8d25"), Text.literal("\u00a77\u8bf7\u4fdd\u6301\u539f\u5730\u4e0d\u52a8"));
                it.remove();
                continue;
            }

            // Spawn particles rising up
            state.particleCounter++;
            if (state.particleCounter % 2 == 0) {
                ServerWorld world = (ServerWorld) player.getWorld();
                Vec3d p = player.getPos();
                double height = (60 - state.remainingTicks) / 60.0 * 3.0;
                world.spawnParticles(ParticleTypes.END_ROD,
                    p.x, p.y + 0.5 + height, p.z,
                    2, 0.1, 0.1, 0.1, 0.01);
                world.spawnParticles(ParticleTypes.ENCHANTED_HIT,
                    p.x, p.y + 0.5 + height, p.z,
                    1, 0.05, 0.05, 0.05, 0.0);
            }

            state.remainingTicks--;

            if (state.remainingTicks <= 0) {
                performTeleport(player, state);
                it.remove();
            } else if (state.remainingTicks == 20 || state.remainingTicks == 40 || state.remainingTicks == 60) {
                int seconds = state.remainingTicks / 20;
                sendTitle(player,
                    Text.literal("\u00a7e\u5373\u5c06\u4f20\u9001..."),
                    Text.literal("\u00a7f" + seconds));
            } else if (state.remainingTicks == 10) {
                sendTitle(player,
                    Text.literal("\u00a7a\u5373\u5c06\u4f20\u9001"),
                    Text.literal("\u00a7e1"));
            }
        }
    }

    private static void performTeleport(ServerPlayerEntity player, CountdownState state) {
        TeleportTarget target = state.target;

        MinecraftServer server = player.getServer();
        RegistryKey<World> destKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.tryParse(target.world));
        ServerWorld destWorld = server.getWorld(destKey);
        if (destWorld == null) destWorld = server.getOverworld();

        player.teleport(destWorld, target.x, target.y, target.z, target.yaw, target.pitch);

        Vec3d pos = new Vec3d(target.x, target.y, target.z);
        for (int i = 0; i < 3; i++) {
            destWorld.spawnParticles(ParticleTypes.ENCHANTED_HIT,
                pos.x, pos.y + 1.0 + i * 0.5, pos.z,
                15, 0.4, 0.4, 0.4, 0.02);
        }
        destWorld.spawnParticles(ParticleTypes.END_ROD,
            pos.x, pos.y + 1.0, pos.z, 12, 0.3, 0.5, 0.3, 0.04);

        if (state.isBackCommand) {
            destWorld.spawnParticles(ParticleTypes.BUBBLE_POP,
                pos.x, pos.y + 0.5, pos.z, 12, 0.3, 0.3, 0.3, 0.03);
            destWorld.spawnParticles(ParticleTypes.PORTAL,
                pos.x, pos.y + 1.0, pos.z, 25, 0.5, 0.7, 0.5, 0.05);
        } else {
            destWorld.spawnParticles(ParticleTypes.HEART,
                pos.x, pos.y + 1.5, pos.z, 6, 0.2, 0.2, 0.2, 0.01);
        }

        String msg = state.isBackCommand ? "\u00a7b\u8fd4\u56de\u6210\u529f" : "\u00a7a\u56de\u5bb6\u6210\u529f";
        sendTitle(player, Text.literal(msg),
            Text.literal("\u00a77" + (int)target.x + ", " + (int)target.y + ", " + (int)target.z));
    }

    private static void sendTitle(ServerPlayerEntity player, Text title, Text subtitle) {
        player.networkHandler.sendPacket(new TitleS2CPacket(title));
        player.networkHandler.sendPacket(new SubtitleS2CPacket(subtitle));
        player.networkHandler.sendPacket(new TitleFadeS2CPacket(5, 40, 10));
    }
}
