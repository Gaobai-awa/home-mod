package net.homemod.command;

import net.homemod.HomeMod;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;

public class ListHomesCommand {

    public static int execute(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendFeedback(() -> Text.literal("\u00a7c[Home] Only players can use this command."), false);
            return 0;
        }

        String uuid = player.getUuid().toString();
        Collection<String> homes = HomeMod.CONFIG.listHomes(uuid);

        if (homes.isEmpty()) {
            player.sendMessage(Text.literal(
                "\u00a77You have no homes set.\n\u00a77Use \u00a7f/sethome\u00a77 to set your first home."
            ), false);
            return 0;
        }

        player.sendMessage(Text.literal("\u00a7a=== Your Homes ==="), false);
        for (String name : homes) {
            player.sendMessage(Text.literal("  \u00a7e>\u00a7f " + name), false);
        }
        player.sendMessage(Text.literal("\u00a77Total: \u00a7f" + homes.size() + "\u00a77 home(s)"), false);

        return 1;
    }
}