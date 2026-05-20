package net.homemod.command;

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

import java.util.UUID;

public class BackCommand {

    public static int execute(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendFeedback(() -> Text.literal("\u00a7c[Back] Only players can use this command."), false);
            return 0;
        }

        UUID uuid = player.getUuid();
        HomeConfig.HomeData lastPos = HomeCommand.getLastPosition(uuid);

        if (lastPos == null) {
            player.sendMessage(Text.literal(
                "\u00a7c[Back] No previous position recorded.\n" +
                "\u00a77Positions are saved before: /home, death, /spawn, etc."
            ), false);
            return 0;
        }

        // Save current position for another /back
        HomeConfig.HomeData currentPos = new HomeConfig.HomeData(
            player.getX(), player.getY(), player.getZ(),
            player.getYaw(), player.getPitch(),
            player.getWorld().getRegistryKey().getValue().toString()
        );

        // Resolve world
        MinecraftServer server = player.getServer();
        RegistryKey<World> destKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.tryParse(lastPos.world));
        ServerWorld destWorld = server.getWorld(destKey);
        if (destWorld == null) {
            destWorld = server.getOverworld();
        }

        // Update last position to current (before teleport back)
        HomeCommand.setLastPosition(uuid, currentPos);

        // Teleport
        player.teleport(destWorld, lastPos.x, lastPos.y, lastPos.z, lastPos.yaw, lastPos.pitch);

        // Effect
        Vec3d pos = new Vec3d(lastPos.x, lastPos.y, lastPos.z);
        spawnBackEffect(destWorld, pos);

        player.sendMessage(Text.literal("\u00a7a[Back] Returning to previous location..."), false);

        return 1;
    }

    private static void spawnBackEffect(ServerWorld world, Vec3d pos) {
        // Cyan/blue effect for back command
        world.spawnParticles(ParticleTypes.END_ROD,
            pos.x, pos.y + 1.0, pos.z, 20, 0.4, 0.5, 0.4, 0.04);
        world.spawnParticles(ParticleTypes.BUBBLE_POP,
            pos.x, pos.y + 0.5, pos.z, 12, 0.3, 0.3, 0.3, 0.03);
        world.spawnParticles(ParticleTypes.PORTAL,
            pos.x, pos.y + 1.0, pos.z, 25, 0.5, 0.7, 0.5, 0.05);
    }
}