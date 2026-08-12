package com.customadvsee;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class AdvancementGuiListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        InventoryHolder holder = topInventory.getHolder();
        if (!(holder instanceof AdvancementGuiHolder guiHolder)) return;

        event.setCancelled(true); // this GUI is read-only

        if (event.getClickedInventory() != topInventory) return; // ignore clicks in the viewer's own inventory

        int slot = event.getSlot();
        int totalPages = Math.max(1, (int) Math.ceil(
                guiHolder.getResults().size() / (double) AdvancementGuiBuilder.RESULTS_PER_PAGE));

        if (slot == 45 && guiHolder.getPage() > 0) {
            reopen(event, guiHolder, guiHolder.getPage() - 1);
        } else if (slot == 53 && guiHolder.getPage() < totalPages - 1) {
            reopen(event, guiHolder, guiHolder.getPage() + 1);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof AdvancementGuiHolder) {
            event.setCancelled(true);
        }
    }

    private void reopen(InventoryClickEvent event, AdvancementGuiHolder holder, int newPage) {
        HumanEntity viewer = event.getWhoClicked();
        Inventory newInventory = AdvancementGuiBuilder.build(holder.getAdvancementKey(), holder.getResults(), newPage);
        viewer.closeInventory();
        viewer.openInventory(newInventory);
    }
}
