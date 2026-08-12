package com.customadvsee;

import io.papermc.paper.advancement.AdvancementDisplay;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AdvancementNames {

    /** Turns user input into a namespaced key, defaulting to "minecraft:" when no namespace was given. */
    public static String normalizeKey(String input) {
        String trimmed = input.trim();
        if (trimmed.contains(":")) return trimmed;
        return "minecraft:" + trimmed;
    }

    /**
     * Best-effort friendly title for a namespaced advancement key. Falls back to the raw
     * key if the advancement isn't currently registered on the server (e.g. it belonged
     * to a datapack that's since been removed, but players still have it logged).
     */
    public static String friendlyName(String namespacedKey) {
        try {
            NamespacedKey nsKey = NamespacedKey.fromString(namespacedKey);
            if (nsKey == null) return namespacedKey;
            Advancement advancement = Bukkit.getAdvancement(nsKey);
            if (advancement == null) return namespacedKey;
            AdvancementDisplay display = advancement.getDisplay();
            if (display == null) return namespacedKey;
            return PlainTextComponentSerializer.plainText().serialize(display.title());
        } catch (Throwable t) {
            return namespacedKey;
        }
    }

    /** All advancement keys currently registered on the server, for tab completion. */
    public static List<String> allKeys() {
        List<String> keys = new ArrayList<>();
        Iterator<Advancement> it = Bukkit.advancementIterator();
        while (it.hasNext()) {
            keys.add(it.next().getKey().toString());
        }
        return keys;
    }
}
