package com.strikesstg.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class PluginConfig {

    public record BanThreshold(int strikes, Duration duration, boolean forever, String rawDuration) {
    }

    private final List<String> bannedWords;
    private final List<BanThreshold> banThresholds;
    private final String strikeTitle;
    private final String strikeSubtitle;
    private final String banReason;
    private final FileConfiguration raw;

    public PluginConfig(FileConfiguration config) {
        this.raw = config;
        this.bannedWords = config.getStringList("banned-words").stream()
                .filter(word -> word != null && !word.isBlank())
                .map(String::trim)
                .toList();

        List<BanThreshold> thresholds = new ArrayList<>();
        List<?> list = config.getList("ban-thresholds");
        if (list != null) {
            for (Object entry : list) {
                if (entry instanceof Map<?, ?> map) {
                    Object strikesObj = map.get("strikes");
                    Object durationObj = map.get("duration");
                    if (strikesObj == null || durationObj == null) {
                        continue;
                    }
                    int strikes = Integer.parseInt(String.valueOf(strikesObj));
                    thresholds.add(parseThreshold(strikes, String.valueOf(durationObj)));
                } else if (entry instanceof ConfigurationSection section) {
                    thresholds.add(parseThreshold(section.getInt("strikes"), section.getString("duration", "forever")));
                }
            }
        }

        thresholds.sort(Comparator.comparingInt(BanThreshold::strikes));
        this.banThresholds = List.copyOf(thresholds);
        this.strikeTitle = config.getString("strike-title", "You got %count% strike%plural%!");
        this.strikeSubtitle = config.getString("strike-subtitle", "");
        this.banReason = config.getString("ban-reason", "You reached %strikes% strikes for swearing.");
    }

    private static BanThreshold parseThreshold(int strikes, String rawDuration) {
        String normalized = rawDuration == null ? "forever" : rawDuration.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("forever") || normalized.equals("permanent") || normalized.equals("-1")) {
            return new BanThreshold(strikes, null, true, "forever");
        }

        char unit = normalized.charAt(normalized.length() - 1);
        long amount = Long.parseLong(normalized.substring(0, normalized.length() - 1).trim());
        Duration duration = switch (unit) {
            case 'd' -> Duration.ofDays(amount);
            case 'h' -> Duration.ofHours(amount);
            case 'm' -> Duration.ofMinutes(amount);
            case 's' -> Duration.ofSeconds(amount);
            default -> throw new IllegalArgumentException("Invalid ban duration: " + rawDuration);
        };
        return new BanThreshold(strikes, duration, false, normalized);
    }

    public List<String> bannedWords() {
        return bannedWords;
    }

    public List<BanThreshold> banThresholds() {
        return banThresholds;
    }

    public String strikeTitle() {
        return strikeTitle;
    }

    public String strikeSubtitle() {
        return strikeSubtitle;
    }

    public String banReason() {
        return banReason;
    }

    public String message(String key, String def) {
        return raw.getString("messages." + key, def);
    }
}
