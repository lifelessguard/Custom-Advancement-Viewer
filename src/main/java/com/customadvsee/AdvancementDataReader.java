package com.customadvsee;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdvancementDataReader {

    // Vanilla's on-disk timestamp format, e.g. "2024-06-01 12:34:56 +0000"
    private static final String STORED_PATTERN = "yyyy-MM-dd HH:mm:ss Z";
    private static final String DISPLAY_PATTERN = "MMM d, yyyy HH:mm";

    private final Logger logger;

    public AdvancementDataReader(Logger logger) {
        this.logger = logger;
    }

    /** The folder Minecraft stores per-player advancement progress in, e.g. world/advancements/. */
    public File advancementsFolder() {
        File mainWorldFolder = Bukkit.getWorlds().get(0).getWorldFolder();
        return new File(mainWorldFolder, "advancements");
    }

    /**
     * Finds every player who has completed the given advancement key.
     * <p>
     * Online players are checked live via the Bukkit API first (this gives an exact
     * awarded timestamp and doesn't depend on the data having been flushed to disk).
     * Offline players - and online players too, as a fallback if the key isn't a
     * currently-registered advancement - are found by reading each player's
     * advancements .json file directly.
     */
    public List<AdvancementResult> findPlayersWithAdvancement(String key) {
        Map<UUID, AdvancementResult> combined = new LinkedHashMap<>();

        NamespacedKey nsKey = NamespacedKey.fromString(key);
        Advancement advancement = nsKey != null ? Bukkit.getAdvancement(nsKey) : null;

        if (advancement != null) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                AdvancementProgress progress = online.getAdvancementProgress(advancement);
                if (!progress.isDone()) continue;

                Date latest = null;
                for (String criterion : progress.getAwardedCriteria()) {
                    Date awarded = progress.getDateAwarded(criterion);
                    if (awarded != null && (latest == null || awarded.after(latest))) {
                        latest = awarded;
                    }
                }
                String time = latest != null ? new SimpleDateFormat(DISPLAY_PATTERN).format(latest) : "Unknown";
                combined.put(online.getUniqueId(), new AdvancementResult(online.getUniqueId(), online.getName(), time));
            }
        }

        for (AdvancementResult fromFile : scanFiles(key)) {
            combined.putIfAbsent(fromFile.uuid(), fromFile);
        }

        return new ArrayList<>(combined.values());
    }

    private List<AdvancementResult> scanFiles(String advancementKey) {
        List<AdvancementResult> results = new ArrayList<>();
        File folder = advancementsFolder();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) return results;

        for (File file : files) {
            String fileName = file.getName();
            String uuidString = fileName.substring(0, fileName.length() - ".json".length());
            UUID uuid;
            try {
                uuid = UUID.fromString(uuidString);
            } catch (IllegalArgumentException ex) {
                continue; // not a player advancement file
            }

            try {
                JsonObject root = JsonParser.parseString(Files.readString(file.toPath())).getAsJsonObject();
                if (!root.has(advancementKey)) continue;

                JsonObject entry = root.getAsJsonObject(advancementKey);
                boolean done = entry.has("done") && entry.get("done").getAsBoolean();
                if (!done) continue;

                String time = latestCriterionTimestamp(entry);
                OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
                String name = offline.getName() != null ? offline.getName() : uuid.toString();

                results.add(new AdvancementResult(uuid, name, time));
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Could not read advancement file " + file.getName(), ex);
            } catch (RuntimeException ex) {
                logger.log(Level.WARNING, "Could not parse advancement file " + file.getName(), ex);
            }
        }
        return results;
    }

    private String latestCriterionTimestamp(JsonObject entry) {
        if (!entry.has("criteria")) return "Unknown";
        JsonObject criteria = entry.getAsJsonObject("criteria");

        Date latest = null;
        String latestRaw = null;
        SimpleDateFormat storedFormat = new SimpleDateFormat(STORED_PATTERN);

        for (String criterionName : criteria.keySet()) {
            JsonElement value = criteria.get(criterionName);
            if (value == null || !value.isJsonPrimitive()) continue;
            String raw = value.getAsString();
            try {
                Date parsed = storedFormat.parse(raw);
                if (latest == null || parsed.after(latest)) {
                    latest = parsed;
                }
            } catch (ParseException ignored) {
                if (latestRaw == null) latestRaw = raw;
            }
        }

        if (latest != null) return new SimpleDateFormat(DISPLAY_PATTERN).format(latest);
        return latestRaw != null ? latestRaw : "Unknown";
    }
}
