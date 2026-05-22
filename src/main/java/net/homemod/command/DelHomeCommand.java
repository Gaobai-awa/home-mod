package net.homemod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.homemod.HomeMod;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class DelHomeCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            net.minecraft.server.command.CommandManager.literal("delhome")
                .then(net.minecraft.server.command.CommandManager.argument("name", StringArgumentType.string())
                    .suggests(HomeCommand.HOME_SUGGESTIONS)
                    .executes(ctx -> execute(ctx.getSource(), StringArgumentType.getString(ctx, "name"))))
                .executes(ctx -> {
                    ctx.getSource().sendError(Text.literal("\u00a7c\u7528\u6cd5: /delhome <\u540d\u5b57>"));
                    return 0;
                })
        );
    }

    public static int execute(ServerCommandSource source, String name) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        String uuidStr = player.getUuid().toString();

        if (!HomeMod.CONFIG.hasHome(uuidStr, name)) {
            player.sendMessage(Text.literal("§c[Home] 家 \"" + name + "\" 不存在"), false);
            return 0;
        }

        HomeMod.CONFIG.deleteHome(uuidStr, name);
        player.sendMessage(Text.literal("§a[Home] 已删除家 \"" + name + "\"§a"), false);
        return 1;
    }
}