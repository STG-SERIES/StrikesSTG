package com.strikesstg.strike;

import com.strikesstg.StrikesSTG;
import com.strikesstg.config.PluginConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.List;

public final class BanService {

    private final StrikesSTG plugin;

    public BanService(StrikesSTG plugin) {
        this.plugin = plugin;
    }

    public void handleDetection(Player player, List<String> matchedWords, String source) {
        if (matchedWords.isEmpty()) {
            return;
        }

        Runnable task = () -> {
            int before = plugin.strikeManager().count(player.getUniqueId());
            plugin.strikeManager().addStrikes(
                    player.getUniqueId(),
                    player.getName(),
                    matchedWords,
                    source
            );
            int added = matchedWords.size();
            int after = before + added;

            for (String word : matchedWords) {
                String reason = "Said '" + word + "' in " + source;
                plugin.getLogger().info(player.getName() + " got 1 strike for " + reason
                        + " (total: " + after + ")");
            }

            showStrikeTitle(player, added);
            applyBanIfNeeded(player, before, after);
        };

        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    private void showStrikeTitle(Player player, int added) {
        String plural = added == 1 ? "" : "s";
        String titleText = plugin.pluginConfig().strikeTitle()
                .replace("%count%", String.valueOf(added))
                .replace("%plural%", plural);
        String subtitleText = plugin.pluginConfig().strikeSubtitle()
                .replace("%count%", String.valueOf(added))
                .replace("%plural%", plural);

        Component title = Component.text(titleText, NamedTextColor.RED);
        Component subtitle = subtitleText.isBlank()
                ? Component.empty()
                : Component.text(subtitleText, NamedTextColor.GRAY);

        player.showTitle(Title.title(
                title,
                subtitle,
                Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(3), Duration.ofMillis(500))
        ));
    }

    private void applyBanIfNeeded(Player player, int before, int after) {
        PluginConfig.BanThreshold chosen = null;
        for (PluginConfig.BanThreshold threshold : plugin.pluginConfig().banThresholds()) {
            if (before < threshold.strikes() && after >= threshold.strikes()) {
                chosen = threshold;
            }
        }
        if (chosen == null) {
            return;
        }

        String reasonText = plugin.pluginConfig().banReason()
                .replace("%strikes%", String.valueOf(after))
                .replace("%threshold%", String.valueOf(chosen.strikes()))
                .replace("%duration%", chosen.rawDuration());

        if (chosen.forever()) {
            player.ban(reasonText, (Duration) null, "StrikesSTG", true);
            plugin.getLogger().info(player.getName() + " was permanently banned for reaching "
                    + after + " strikes.");
        } else {
            player.ban(reasonText, chosen.duration(), "StrikesSTG", true);
            plugin.getLogger().info(player.getName() + " was banned for " + chosen.rawDuration()
                    + " for reaching " + after + " strikes.");
        }
    }
}
