package net.homemod.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.homemod.HomeMod;
import net.homemod.config.HomeConfig;
import net.homemod.util.CountdownTask;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;

public class BackCommand {

    public static int execute(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        UUID uuid = player.getUuid();
        String uuidStr = uuid.toString();

        HomeConfig.HomeData last = HomeMod.CONFIG.getLastPosition(uuidStr);
        if (last == null) {
            player.sendMessage(Text.literal("§c[Home] 没有可返回的位置！"), false);
            return 0;
        }

        boolean started = CountdownTask.startCountdown(player,
            new CountdownTask.TeleportTarget(last.x, last.y, last.z, last.yaw, last.pitch, last.world),
            true);
        return started ? 1 : 0;
    }
}
