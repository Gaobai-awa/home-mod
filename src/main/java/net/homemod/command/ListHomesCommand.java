package net.homemod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.homemod.HomeMod;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;

public class ListHomesCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            net.minecraft.server.command.CommandManager.literal("listhomes")
                .executes(ctx -> execute(ctx.getSource()))
        );
    }

    public static int execute(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        String uuidStr = player.getUuid().toString();

        Collection<String> homes = HomeMod.CONFIG.listHomes(uuidStr);
        if (homes.isEmpty()) {
            player.sendMessage(Text.literal("§7[Home] 你还没有设置任何家。使用 §f/sethome <名字>§7 创建一个。"), false);
            return 0;
        }

        player.sendMessage(Text.literal("§6========== §e你的家 §6=========="), false);
        int i = 1;
        for (String name : homes) {
            player.sendMessage(Text.literal("§e" + i + ". §a" + name), false);
            i++;
        }
        player.sendMessage(Text.literal("§6============================"), false);
        return homes.size();
    }
}