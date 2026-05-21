package net.homemod.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.homemod.HomeMod;
import net.homemod.config.HomeConfig;
import net.homemod.util.CountdownTask;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;

public class HomeCommand {

    public static int execute(ServerCommandSource source, String name) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        UUID uuid = player.getUuid();
        String uuidStr = uuid.toString();

        HomeConfig.HomeData home = HomeMod.CONFIG.getHome(uuidStr, name);
        if (home == null) {
            player.sendMessage(Text.literal("§c[Home] 家 \"" + name + "\" 不存在！使用 /sethome " + name + " 创建"), false);
            return 0;
        }

        // Start countdown
        boolean started = CountdownTask.startCountdown(player,
            new CountdownTask.TeleportTarget(home.x, home.y, home.z, home.yaw, home.pitch, home.world),
            false);
        return started ? 1 : 0;
    }

    public static void setLastPosition(UUID uuid, HomeConfig.HomeData data) {
        HomeMod.CONFIG.setLastPosition(uuid.toString(), data);
    }
}