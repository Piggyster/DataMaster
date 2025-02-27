package me.piggyster.datamaster.plugin;

import me.piggyster.datamaster.api.DataMaster;
import me.piggyster.datamaster.api.util.DataMasterSettings;
import me.piggyster.datamaster.plugin.listener.PlayerListener;
import me.piggyster.datamaster.plugin.test.BalanceCommand;
import me.piggyster.datamaster.plugin.test.InventoryCommand;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class DataMasterPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadMaster();
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    private void loadMaster() {
        ConfigurationSection section = getConfig().getConfigurationSection("database");
        if(section == null) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        DataMasterSettings settings = DataMasterSettings.loadFromSection(section);
        DataMaster.initialize(settings);

        DataMaster master = DataMaster.get();

        master.setSyncField("economy.*");
        master.registerDefaultValue("economy.coins", 0);
        master.registerDefaultValue("economy.shit", 69);

        master.registerDefaultValue("inventory", new HashMap<>());



        getCommand("balance").setExecutor(new BalanceCommand());
        getCommand("inventory").setExecutor(new InventoryCommand());
    }

    @Override
    public void onDisable() {
        DataMaster.get().shutdown();
    }


}
