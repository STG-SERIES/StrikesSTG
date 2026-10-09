package com.strikesstg.strike;

import com.strikesstg.StrikesSTG;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class StrikeManager {

    public record StrikeEntry(String reason, Instant at, List<String> words, String source) {
    }

    public record PlayerStrikes(UUID uuid, String lastKnownName, List<StrikeEntry> entries) {
        public int count() {
            return entries.size();
        }
    }

    private final StrikesSTG plugin;
    private final File file;
    private final Map<UUID, PlayerStrikes> players = new LinkedHashMap<>();

    public StrikeManager(StrikesSTG plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "strikes.yml");
    }

    public void load() {
        players.clear();
        if (!file.exists()) {
            save();
            return;
        }

        FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = yaml.getConfigurationSection("players");
        if (root == null) {
            return;
        }

        for (String key : root.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(key);
            } catch (IllegalArgumentException ignored) {
                continue;
            }

            ConfigurationSection section = root.getConfigurationSection(key);
            if (section == null) {
                continue;
            }

            String name = section.getString("name", "Unknown");
            List<StrikeEntry> entries = new ArrayList<>();
            List<?> history = section.getList("history");
            if (history != null) {
                for (Object item : history) {
                    if (!(item instanceof Map<?, ?> map)) {
                        continue;
                    }
                    Object reasonObj = map.get("reason");
                    String reason = reasonObj == null ? "Unknown" : String.valueOf(reasonObj);
                    Instant at;
                    try {
                        Object atObj = map.get("at");
                        at = Instant.parse(atObj == null ? Instant.EPOCH.toString() : String.valueOf(atObj));
                    } catch (Exception ex) {
                        at = Instant.EPOCH;
                    }
                    Object wordsObj = map.get("words");
                    List<String> words = new ArrayList<>();
                    if (wordsObj instanceof List<?> wordList) {
                        for (Object word : wordList) {
                            words.add(String.valueOf(word));
                        }
                    }
                    Object sourceObj = map.get("source");
                    String source = sourceObj == null ? "unknown" : String.valueOf(sourceObj);
                    entries.add(new StrikeEntry(reason, at, List.copyOf(words), source));
                }
            }

            players.put(uuid, new PlayerStrikes(uuid, name, entries));
        }
    }

    public void save() {
        FileConfiguration yaml = new YamlConfiguration();
        for (PlayerStrikes data : players.values()) {
            String path = "players." + data.uuid();
            yaml.set(path + ".name", data.lastKnownName());
            yaml.set(path + ".strikes", data.count());

            List<Map<String, Object>> history = new ArrayList<>();
            for (StrikeEntry entry : data.entries()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("reason", entry.reason());
                map.put("at", entry.at().toString());
                map.put("source", entry.source());
                map.put("words", entry.words());
                history.add(map);
            }
            yaml.set(path + ".history", history);
        }

        try {
            if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
                plugin.getLogger().warning("Could not create plugin data folder.");
            }
            yaml.save(file);
        } catch (IOException ex) {
            plugin.getLogger().severe("Failed to save strikes.yml: " + ex.getMessage());
        }
    }

    public PlayerStrikes get(UUID uuid) {
        return players.getOrDefault(uuid, new PlayerStrikes(uuid, "Unknown", List.of()));
    }

    public int count(UUID uuid) {
        return get(uuid).count();
    }

    public synchronized int addStrikes(UUID uuid, String name, List<String> words, String source) {
        PlayerStrikes current = get(uuid);
        List<StrikeEntry> entries = new ArrayList<>(current.entries());

        Instant now = Instant.now();
        for (String word : words) {
            String reason = "Said '" + word + "' in " + source;
            entries.add(new StrikeEntry(reason, now, List.of(word), source));
        }

        players.put(uuid, new PlayerStrikes(uuid, name, List.copyOf(entries)));
        save();
        return words.size();
    }

    public synchronized void reset(UUID uuid, String name) {
        players.put(uuid, new PlayerStrikes(uuid, name, List.of()));
        save();
    }

    public PlayerStrikes findByName(String name) {
        for (PlayerStrikes data : players.values()) {
            if (data.lastKnownName().equalsIgnoreCase(name)) {
                return data;
            }
        }
        return null;
    }
}
