package com.strikesstg.listener;

import com.strikesstg.StrikesSTG;
import com.strikesstg.filter.WordFilter;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class ChatListener implements Listener {

    private final StrikesSTG plugin;

    public ChatListener(StrikesSTG plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("strikesstg.bypass")) {
            return;
        }

        WordFilter.FilterResult result = plugin.wordFilter().filter(event.message());
        if (!result.found()) {
            return;
        }

        event.message(result.censored());
        plugin.banService().handleDetection(player, result.matchedWords(), "chat");
    }
}
