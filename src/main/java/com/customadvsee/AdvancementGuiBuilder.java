package com.customadvsee;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class AdvancementGuiBuilder {

    public static final int SLOTS_PER_ROW = 9;
    public static final int ROWS = 6; // 54 slots - renders identically to a double chest
    public static final int RESULTS_PER_PAGE = 45; // top 5 rows hold heads; bottom row is navigation

    public static Inventory build(String advancementKey, List<AdvancementResult> results, int page) {
        String friendlyName = AdvancementNames.friendlyName(advancementKey);
        int totalPages = Math.max(1, (int) Math.ceil(results.size() / (double) RESULTS_PER_PAGE));
        page = Math.max(0, Math.min(page, totalPages - 1));

        String title = "Advancement: " + trim(friendlyName, 24)
                + (totalPages > 1 ? " (" + (page + 1) + "/" + totalPages + ")" : "");

        AdvancementGuiHolder holder = new AdvancementGuiHolder(advancementKey, results, page);
        Inventory inventory = Bukkit.createInventory(holder, ROWS * SLOTS_PER_ROW, title);
        holder.setInventory(inventory);

        int start = page * RESULTS_PER_PAGE;
        int end = Math.min(start + RESULTS_PER_PAGE, results.size());

        int slot = 0;
        for (int i = start; i < end; i++) {
            inventory.setItem(slot++, buildHead(results.get(i), friendlyName));
        }

        if (totalPages > 1) {
            if (page > 0) {
                inventory.setItem(45, navItem(Material.ARROW, "Previous Page"));
            }
            inventory.setItem(49, navItem(Material.BOOK,
                    "Page " + (page + 1) + " / " + totalPages + " - " + results.size() + " player(s)"));
            if (page < totalPages - 1) {
                inventory.setItem(53, navItem(Material.ARROW, "Next Page"));
            }
        }

        return inventory;
    }

    private static ItemStack buildHead(AdvancementResult result, String friendlyName) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(result.uuid());
            meta.setOwningPlayer(offline);
            meta.setDisplayName(ChatColorCompat.gold(result.playerName()));

            List<String> lore = new ArrayList<>();
            lore.add(ChatColorCompat.aqua(friendlyName));
            lore.add(ChatColorCompat.gray(result.timeAchieved()));
            meta.setLore(lore);

            head.setItemMeta(meta);
        }
        return head;
    }

    private static ItemStack navItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColorCompat.yellow(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    private static String trim(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
