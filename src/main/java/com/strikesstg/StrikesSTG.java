package com.strikesstg;

import com.strikesstg.command.StrikesCommand;
import com.strikesstg.config.PluginConfig;
import com.strikesstg.filter.WordFilter;
import com.strikesstg.listener.BookListener;
import com.strikesstg.listener.ChatListener;
import com.strikesstg.listener.SignListener;
import com.strikesstg.strike.BanService;
import com.strikesstg.strike.StrikeManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class StrikesSTG extends JavaPlugin {

    private PluginConfig pluginConfig;
    private WordFilter wordFilter;
    private StrikeManager strikeManager;
    private BanService banService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadPlugin();

        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new SignListener(this), this);
        getServer().getPluginManager().registerEvents(new BookListener(this), this);

        StrikesCommand command = new StrikesCommand(this);
        var pluginCommand = getCommand("strikes");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }

        getLogger().info("StrikesSTG enabled.");
    }

    @Override
    public void onDisable() {
        if (strikeManager != null) {
            strikeManager.save();
        }
        getLogger().info("StrikesSTG disabled.");
    }

    public void reloadPlugin() {
        reloadConfig();
        pluginConfig = new PluginConfig(getConfig());
        wordFilter = new WordFilter(pluginConfig.bannedWords());
        if (strikeManager == null) {
            strikeManager = new StrikeManager(this);
        } else {
            strikeManager.save();
        }
        strikeManager.load();
        banService = new BanService(this);
    }

    public PluginConfig pluginConfig() {
        return pluginConfig;
    }

    public WordFilter wordFilter() {
        return wordFilter;
    }

    public StrikeManager strikeManager() {
        return strikeManager;
    }

    public BanService banService() {
        return banService;
    }
}
