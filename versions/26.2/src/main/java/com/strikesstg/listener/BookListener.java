package com.strikesstg.listener;

import com.strikesstg.StrikesSTG;
import com.strikesstg.filter.WordFilter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;

public final class BookListener implements Listener {

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    private final StrikesSTG plugin;

    public BookListener(StrikesSTG plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBookEdit(PlayerEditBookEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("strikesstg.bypass")) {
            return;
        }

        BookMeta meta = event.getNewBookMeta();
        List<String> allMatches = new ArrayList<>();
        boolean changed = false;

        List<Component> pages = new ArrayList<>(meta.pages());
        for (int i = 0; i < pages.size(); i++) {
            WordFilter.FilterResult result = plugin.wordFilter().filter(pages.get(i));
            if (!result.found()) {
                continue;
            }
            pages.set(i, result.censored());
            allMatches.addAll(result.matchedWords());
            changed = true;
        }

        if (changed) {
            meta.pages(pages);
        }

        if (meta.hasTitle()) {
            WordFilter.FilterResult titleResult = plugin.wordFilter().filter(meta.getTitle());
            if (titleResult.found()) {
                meta.setTitle(PLAIN.serialize(titleResult.censored()));
                allMatches.addAll(titleResult.matchedWords());
                changed = true;
            }
        }

        if (!allMatches.isEmpty()) {
            if (changed) {
                event.setNewBookMeta(meta);
            }
            String source = event.isSigning() ? "a signed book" : "a book";
            plugin.banService().handleDetection(player, allMatches, source);
        }
    }
}
