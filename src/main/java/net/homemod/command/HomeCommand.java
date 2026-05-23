package net.homemod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.homemod.HomeMod;
import net.homemod.config.HomeConfig;
import net.homemod.util.CountdownTask;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.UUID;

public class HomeCommand {

    // Tab: suggest player's existing home names (case-insensitive)
    public static final SuggestionProvider<ServerCommandSource> HOME_SUGGESTIONS =
        (context, builder) -> {
            try {
                ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                Collection<String> homes = HomeMod.CONFIG.listHomes(player.getUuid().toString());
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

    // Entry called by HomeMod
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            net.minecraft.server.command.CommandManager.literal("home")
                .then(net.minecraft.server.command.CommandManager.argument("name", StringArgumentType.greedyString())
                    .suggests(HOME_SUGGESTIONS)
                    .executes(ctx -> execute(ctx.getSource(), StringArgumentType.getString(ctx, "name"))))
                .executes(ctx -> execute(ctx.getSource(), "home"))
        );
    }

    public static int execute(ServerCommandSource source, String name) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        HomeConfig.HomeData home = HomeMod.CONFIG.getHome(player.getUuid().toString(), name);
        if (home == null) {
            player.sendMessage(Text.literal("§c[Home] 家 \"" + name + "\" 不存在！使用 §f/sethome §c创建"), false);
            return 0;
        }
        boolean started = CountdownTask.startCountdown(player,
            new CountdownTask.TeleportTarget(home.x, home.y, home.z, home.yaw, home.pitch, home.world),
            false);
        return started ? 1 : 0;
    }
}
