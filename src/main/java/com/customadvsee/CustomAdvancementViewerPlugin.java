package com.customadvsee;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomAdvancementViewerPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new AdvancementGuiListener(), this);

        CustomAdvSeeCommand executor = new CustomAdvSeeCommand(getLogger());
        PluginCommand command = getCommand("customadvsee");
        if (command != null) {
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }
    }
}
