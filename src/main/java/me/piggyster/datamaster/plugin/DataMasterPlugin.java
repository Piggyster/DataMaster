package me.piggyster.datamaster.plugin;

import me.piggyster.datamaster.api.DataMaster;
import me.piggyster.datamaster.api.util.DataMasterSettings;
import me.piggyster.datamaster.plugin.listener.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public class DataMasterPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        loadMaster();
    }

    private void loadMaster() {
        ConfigurationSection section = getConfig().getConfigurationSection("database");
        if(section == null) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        DataMasterSettings settings = DataMasterSettings.loadFromSection(section);
        DataMaster.initialize(settings);
    }

    @Override
    public void onDisable() {
        DataMaster.get().shutdown();
    }


}
