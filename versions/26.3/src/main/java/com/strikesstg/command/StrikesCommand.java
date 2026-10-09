package com.strikesstg.command;

import com.strikesstg.StrikesSTG;
import com.strikesstg.strike.StrikeManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class StrikesCommand implements CommandExecutor, TabCompleter {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final StrikesSTG plugin;

    public StrikesCommand(StrikesSTG plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(mini("usage"));
                return true;
            }
            if (!sender.hasPermission("strikesstg.strikes")) {
                sender.sendMessage(mini("no-permission"));
                return true;
            }
            showStrikes(sender, player.getUniqueId(), player.getName());
            return true;
        }

        if (args[0].equalsIgnoreCase("reset")) {
            if (!sender.hasPermission("strikesstg.reset")) {
                sender.sendMessage(mini("no-permission"));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(mini("usage"));
                return true;
            }

            ResolvedTarget target = resolve(args[1]);
            if (target == null) {
                sender.sendMessage(mini("player-not-found"));
                return true;
            }

            plugin.strikeManager().reset(target.uuid(), target.name());
            sender.sendMessage(mini("reset", "%player%", target.name()));

            Player online = Bukkit.getPlayer(target.uuid());
            if (online != null) {
                online.sendMessage(mini("reset-notify"));
            }
            return true;
        }

        if (!sender.hasPermission("strikesstg.strikes.others")) {
            sender.sendMessage(mini("no-permission"));
            return true;
        }

        ResolvedTarget target = resolve(args[0]);
        if (target == null) {
            sender.sendMessage(mini("player-not-found"));
            return true;
        }

        showStrikes(sender, target.uuid(), target.name());
        return true;
    }

    private void showStrikes(CommandSender sender, UUID uuid, String name) {
        StrikeManager.PlayerStrikes data = plugin.strikeManager().get(uuid);
        if (data.count() == 0) {
            boolean self = sender instanceof Player player && player.getUniqueId().equals(uuid);
            if (self) {
                sender.sendMessage(mini("none"));
            } else {
                sender.sendMessage(mini("none-other", "%player%", name));
            }
            return;
        }

        sender.sendMessage(mini(
                "header",
                "%player%", name,
                "%count%", String.valueOf(data.count())
        ));

        int index = 1;
        for (StrikeManager.StrikeEntry entry : data.entries()) {
            sender.sendMessage(mini(
                    "entry",
                    "%index%", String.valueOf(index++),
                    "%reason%", MiniMessage.miniMessage().escapeTags(entry.reason()),
                    "%when%", TIME.format(entry.at())
            ));
        }
    }

    private record ResolvedTarget(UUID uuid, String name) {
    }

    private ResolvedTarget resolve(String input) {
        Player online = Bukkit.getPlayerExact(input);
        if (online != null) {
            return new ResolvedTarget(online.getUniqueId(), online.getName());
        }

        for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
            if (offline.getName() != null && offline.getName().equalsIgnoreCase(input)) {
                return new ResolvedTarget(offline.getUniqueId(), offline.getName());
            }
        }

        StrikeManager.PlayerStrikes stored = plugin.strikeManager().findByName(input);
        if (stored != null) {
            return new ResolvedTarget(stored.uuid(), stored.lastKnownName());
        }
        return null;
    }

    private Component mini(String key, String... replacements) {
        String raw = switch (key) {
            case "no-permission" -> plugin.pluginConfig().message(key, "<red>You don't have permission to do that.");
            case "player-not-found" -> plugin.pluginConfig().message(key, "<red>Player not found.");
            case "usage" -> plugin.pluginConfig().message(key, "<yellow>Usage: /strikes [player] | /strikes reset <player>");
            case "none" -> plugin.pluginConfig().message(key, "<green>You have no strikes.");
            case "none-other" -> plugin.pluginConfig().message(key, "<green>%player% has no strikes.");
            case "header" -> plugin.pluginConfig().message(key, "<gold>Strikes for %player%: <white>%count%");
            case "entry" -> plugin.pluginConfig().message(key, "<gray>#%index% <white>%reason% <dark_gray>(%when%)");
            case "reset" -> plugin.pluginConfig().message(key, "<green>Reset strikes for %player%.");
            case "reset-notify" -> plugin.pluginConfig().message(key, "<yellow>Your strikes have been reset by an admin.");
            default -> plugin.pluginConfig().message(key, key);
        };

        for (int i = 0; i + 1 < replacements.length; i += 2) {
            raw = raw.replace(replacements[i], replacements[i + 1]);
        }
        return MINI.deserialize(raw);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            if (sender.hasPermission("strikesstg.reset") && "reset".startsWith(prefix)) {
                out.add("reset");
            }
            if (sender.hasPermission("strikesstg.strikes.others")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                        out.add(player.getName());
                    }
                }
            }
            return out;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("reset") && sender.hasPermission("strikesstg.reset")) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    out.add(player.getName());
                }
            }
            return out;
        }
        return List.of();
    }
}
