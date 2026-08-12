package com.customadvsee;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CustomAdvSeeCommand implements CommandExecutor, TabCompleter {

    private final AdvancementDataReader reader;

    public CustomAdvSeeCommand(Logger logger) {
        this.reader = new AdvancementDataReader(logger);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can open the advancement viewer GUI.");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /customadvsee <advancement>");
            return true;
        }

        String rawKey = String.join(" ", args);
        String key = AdvancementNames.normalizeKey(rawKey);

        List<AdvancementResult> results = reader.findPlayersWithAdvancement(key);
        if (results.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No players have earned \""
                    + AdvancementNames.friendlyName(key) + "\" (" + key + ").");
            return true;
        }

        player.openInventory(AdvancementGuiBuilder.build(key, results, 0));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String partial = String.join(" ", args).toLowerCase();
        List<String> out = new ArrayList<>();
        for (String key : AdvancementNames.allKeys()) {
            if (key.toLowerCase().contains(partial)) {
                out.add(key);
            }
        }
        return out;
    }
}
