package net.homemod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.homemod.HomeMod;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;

public class DelHomeCommand {

    public static final SuggestionProvider<ServerCommandSource> DEL_SUGGESTIONS =
        (context, builder) -> {
            try {
                ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                String uuid = player.getUuid().toString();
                Collection<String> homes = HomeMod.CONFIG.listHomes(uuid);
                String input = builder.getRemainingLowerCase();
                for (String name : homes) {
                    if (name.toLowerCase().startsWith(input)) {
                        builder.suggest(name);
                    }
                }
            } catch (CommandSyntaxException ignored) {
            }
            return builder.buildFuture();
        };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            net.minecraft.server.command.CommandManager.literal("delhome")
                .then(net.minecraft.server.command.CommandManager.argument("name", StringArgumentType.greedyString())
                    .suggests(DEL_SUGGESTIONS)
                    .executes(ctx -> execute(ctx.getSource(), StringArgumentType.getString(ctx, "name"))))
        );
    }

    public static int execute(ServerCommandSource source, String name) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        String uuid = player.getUuid().toString();

        if (!HomeMod.CONFIG.hasHome(uuid, name)) {
            player.sendMessage(Text.literal("§c[Home] 家 \"" + name + "\" 不存在"), false);
            return 0;
        }

        HomeMod.CONFIG.deleteHome(uuid, name);
        player.sendMessage(Text.literal("§a[Home] 家 \"" + name + "\" 已删除"), false);
        return 1;
    }
}
