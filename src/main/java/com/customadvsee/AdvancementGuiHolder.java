package com.customadvsee;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.List;

public class AdvancementGuiHolder implements InventoryHolder {

    private final String advancementKey;
    private final List<AdvancementResult> results;
    private final int page;
    private Inventory inventory;

    public AdvancementGuiHolder(String advancementKey, List<AdvancementResult> results, int page) {
        this.advancementKey = advancementKey;
        this.results = results;
        this.page = page;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public String getAdvancementKey() {
        return advancementKey;
    }

    public List<AdvancementResult> getResults() {
        return results;
    }

    public int getPage() {
        return page;
    }
}
