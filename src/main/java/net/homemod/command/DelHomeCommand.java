package net.homemod.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.homemod.HomeMod;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class DelHomeCommand {

    public static int execute(ServerCommandSource source, String name) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        String uuidStr = player.getUuid().toString();

        if (!HomeMod.CONFIG.hasHome(uuidStr, name)) {
            player.sendMessage(Text.literal("\u00a7c[Home] \u5bb6 \u00a7e'" + name + "'\u00a7c \u4e0d\u5b58\u5728"), false);
            return 0;
        }

        HomeMod.CONFIG.deleteHome(uuidStr, name);
        player.sendMessage(Text.literal("\u00a7a[Home] \u5df2\u5220\u9664\u5bb6 \u00a7e'" + name + "'\u00a7a"), false);
        return 1;
    }
}
