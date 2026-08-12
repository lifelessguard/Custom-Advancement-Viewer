package com.customadvsee;

import java.util.UUID;

/**
 * A single player who has completed the searched advancement.
 */
public record AdvancementResult(UUID uuid, String playerName, String timeAchieved) {
}
