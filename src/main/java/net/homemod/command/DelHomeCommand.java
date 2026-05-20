package net.homemod.command;

import net.homemod.HomeMod;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class DelHomeCommand {

    public static int execute(ServerCommandSource source, String name) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendFeedback(() -> Text.literal("\u00a7c[Home] Only players can use this command."), false);
            return 0;
        }

        String uuid = player.getUuid().toString();

        if (!HomeMod.CONFIG.hasHome(uuid, name)) {
            player.sendMessage(Text.literal(
                "\u00a7c[Home] Home \u00a7e'" + name + "'\u00a7c does not exist."
            ), false);
            return 0;
        }

        HomeMod.CONFIG.deleteHome(uuid, name);
        player.sendMessage(Text.literal(
            "\u00a7a[Home] Home \u00a7e'" + name + "'\u00a7a deleted."
        ), false);

        return 1;
    }
}