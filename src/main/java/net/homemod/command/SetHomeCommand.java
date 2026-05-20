package net.homemod.command;

import net.homemod.HomeMod;
import net.homemod.config.HomeConfig;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class SetHomeCommand {

    public static int execute(ServerCommandSource source, String name) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendFeedback(() -> Text.literal("\u00a7c[Home] Only players can use this command."), false);
            return 0;
        }

        String uuid = player.getUuid().toString();
        Vec3d pos = player.getPos();
        float yaw = player.getYaw();
        float pitch = player.getPitch();
        String worldId = player.getWorld().getRegistryKey().getValue().toString();

        HomeConfig.HomeData home = new HomeConfig.HomeData(
            pos.x, pos.y, pos.z, yaw, pitch, worldId
        );

        HomeMod.CONFIG.setHome(uuid, name, home);

        player.sendMessage(Text.literal(
            "\u00a7a[Home] \u00a7fHome \u00a7e'" + name + "'\u00a7a set at \u00a7f("
            + (int) pos.x + ", " + (int) pos.y + ", " + (int) pos.z + ")"
        ), false);

        // Sparkle particles
        spawnSetEffect(player, pos);

        return 1;
    }

    private static void spawnSetEffect(ServerPlayerEntity player, Vec3d pos) {
        if (player.getWorld().isClient) return;
        ServerWorld world = (ServerWorld) player.getWorld();
        world.spawnParticles(ParticleTypes.ENCHANTED_HIT,
            pos.x, pos.y + 1.0, pos.z, 30, 0.4, 0.6, 0.4, 0.02);
        world.spawnParticles(ParticleTypes.HAPPY_VILLAGER,
            pos.x, pos.y + 0.5, pos.z, 15, 0.3, 0.4, 0.3, 0.05);
    }
}
