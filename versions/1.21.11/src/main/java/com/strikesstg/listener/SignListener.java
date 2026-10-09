package com.strikesstg.listener;

import com.strikesstg.StrikesSTG;
import com.strikesstg.filter.WordFilter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import java.util.ArrayList;
import java.util.List;

public final class SignListener implements Listener {

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    private final StrikesSTG plugin;

    public SignListener(StrikesSTG plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("strikesstg.bypass")) {
            return;
        }

        List<String> allMatches = new ArrayList<>();
        for (int i = 0; i < event.lines().size(); i++) {
            Component line = event.line(i);
            String plain = PLAIN.serialize(line == null ? Component.empty() : line);
            WordFilter.FilterResult result = plugin.wordFilter().filter(plain);
            if (!result.found()) {
                continue;
            }
            event.line(i, result.censored());
            allMatches.addAll(result.matchedWords());
        }

        if (!allMatches.isEmpty()) {
            plugin.banService().handleDetection(player, allMatches, "a sign");
        }
    }
}
