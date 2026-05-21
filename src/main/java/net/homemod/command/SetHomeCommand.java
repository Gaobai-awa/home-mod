package net.homemod.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.homemod.HomeMod;
import net.homemod.config.HomeConfig;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class SetHomeCommand {

    public static int execute(ServerCommandSource source, String name) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        UUID uuid = player.getUuid();
        String uuidStr = uuid.toString();

        // Check limit: max 100 homes per player
        int homeCount = HomeMod.CONFIG.listHomes(uuidStr).size();
        if (homeCount >= 100) {
            player.sendMessage(Text.literal("\u00a7c[Home] \u4f60\u6700\u591a\u53ea\u80fd\u4fdd\u5b58 \u00a7e100\u00a7c \u4e2a\u5bb6\uff01\u5df2\u7528: " + homeCount), false);
            return 0;
        }

        HomeConfig.HomeData data = new HomeConfig.HomeData(
            player.getX(), player.getY(), player.getZ(),
            player.getYaw(), player.getPitch(),
            player.getWorld().getRegistryKey().getValue().toString()
        );

        HomeMod.CONFIG.setHome(uuidStr, name, data);

        player.sendMessage(Text.literal("\u00a7a[Home] \u6210\u529f\u8bbe\u7f6e\u5bb6 \"" + name + "\" \u00a7f[" + (int)data.x + ", " + (int)data.y + ", " + (int)data.z + "]"), false);

        // Golden particle effect
        if (player.getWorld() instanceof ServerWorld) {
            ServerWorld world = (ServerWorld) player.getWorld();
            Vec3d pos = player.getPos();
            world.spawnParticles(ParticleTypes.ENCHANTED_HIT,
                pos.x, pos.y + 0.5, pos.z,
                10, 0.2, 0.3, 0.2, 0.02);
            world.spawnParticles(ParticleTypes.HAPPY_VILLAGER,
                pos.x, pos.y + 0.5, pos.z,
                8, 0.2, 0.2, 0.2, 0.01);
        }

        return 1;
    }
}
