package com.albionservant.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Exact base crafting Fame and labor-journal progress for gear crafts.
 *
 * Fame is intentionally the base craft fame that fills labor journals. Premium,
 * seasonal and other character bonuses are not added to journal progress.
 * Enchantment multiplies base fame by 2^enchantment.
 */
// ALBIONSERVANT_CRAFTING_FAME_BOOKS_FIX_V2
public final class CraftingJournalData {

    private static final String RESOURCE = "/data/crafting-fame.properties";
    private static final Map<String, FameEntry> ENTRIES = loadEntries();

    private record FameEntry(
            long baseFame,
            String journalType,
            long journalCapacity
    ) {
    }

    private CraftingJournalData() {
    }

    public static long famePerItem(String exactItemId, int enchantment) {
        String baseId = canonicalBaseId(exactItemId);
        FameEntry entry = ENTRIES.get(baseId);
        long baseFame = entry != null ? entry.baseFame() : fallbackBaseFame(baseId);

        if (baseFame <= 0L) {
            return 0L;
        }

        int safeEnchantment = Math.max(0, Math.min(4, enchantment));
        return Math.multiplyExact(baseFame, 1L << safeEnchantment);
    }

    public static long totalFame(String exactItemId, int enchantment, long quantity) {
        long safeQuantity = Math.max(0L, quantity);
        try {
            return Math.multiplyExact(
                    famePerItem(exactItemId, enchantment),
                    safeQuantity
            );
        } catch (ArithmeticException overflow) {
            return Long.MAX_VALUE;
        }
    }

    public static String fameText(String exactItemId, int enchantment, long quantity) {
        long fame = totalFame(exactItemId, enchantment, quantity);
        return fame <= 0L ? "\u2014" : String.format(Locale.US, "%,d", fame);
    }

    public static String booksText(String exactItemId, int tier, long totalFame) {
        String journal = journalDisplayName(exactItemId);
        long maxFame = journalMaxFame(exactItemId, tier);

        if (journal == null || maxFame <= 0L || totalFame <= 0L) {
            return "\u2014";
        }

        double books = totalFame / (double) maxFame;
        return String.format(Locale.US, "%,.2f %s", books, journal);
    }

    public static String booksTooltip(String exactItemId, int tier, long totalFame) {
        String journal = journalDisplayName(exactItemId);
        long maxFame = journalMaxFame(exactItemId, tier);

        if (journal == null || maxFame <= 0L) {
            return "This craft does not map to a crafting labor journal.";
        }

        return "Fills "
                + journal
                + " labor journals"
                + "\nJournal capacity: "
                + String.format(Locale.US, "%,d", maxFame)
                + " base fame"
                + "\nBatch base fame: "
                + String.format(Locale.US, "%,d", Math.max(0L, totalFame))
                + "\nFilled journals: "
                + String.format(
                        Locale.US,
                        "%,.2f",
                        Math.max(0L, totalFame) / (double) maxFame
                );
    }

    public static String journalDisplayName(String exactItemId) {
        String type = journalType(exactItemId);
        if (type == null) {
            return null;
        }

        return switch (type) {
            case "WARRIOR" -> "Warrior";
            case "HUNTER" -> "Hunter";
            case "MAGE" -> "Mage";
            case "TOOLMAKER" -> "Toolmaker";
            default -> null;
        };
    }

    public static long journalMaxFame(String exactItemId, int tier) {
        FameEntry entry = ENTRIES.get(canonicalBaseId(exactItemId));
        if (entry != null && entry.journalCapacity() > 0L) {
            return entry.journalCapacity();
        }

        if (journalType(exactItemId) == null) {
            return 0L;
        }

        return fallbackJournalCapacity(tier);
    }

    public static String journalType(String exactItemId) {
        String id = canonicalBaseId(exactItemId);
        if (id == null || id.isBlank()) {
            return null;
        }

        FameEntry entry = ENTRIES.get(id);
        if (entry != null && !entry.journalType().isBlank()) {
            return entry.journalType();
        }

        return fallbackJournalType(id);
    }

    private static Map<String, FameEntry> loadEntries() {
        InputStream stream = CraftingJournalData.class.getResourceAsStream(RESOURCE);
        if (stream == null) {
            return Collections.emptyMap();
        }

        Map<String, FameEntry> result = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8)
        )) {
            String line;
            while ((line = reader.readLine()) != null) {
                String value = line.trim();
                if (value.isEmpty() || value.startsWith("#")) {
                    continue;
                }

                int equals = value.indexOf('=');
                if (equals <= 0 || equals >= value.length() - 1) {
                    continue;
                }

                String itemId = value.substring(0, equals).trim();
                String[] fields = value.substring(equals + 1).split("\\|", -1);
                if (fields.length < 3) {
                    continue;
                }

                try {
                    long baseFame = Long.parseLong(fields[0].trim());
                    String journalType = fields[1].trim().toUpperCase(Locale.ROOT);
                    long capacity = Long.parseLong(fields[2].trim());
                    result.put(
                            itemId,
                            new FameEntry(baseFame, journalType, capacity)
                    );
                } catch (NumberFormatException ignored) {
                    // Skip a malformed generated line; fallback formulas remain available.
                }
            }
        } catch (IOException ignored) {
            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(result);
    }

    private static long fallbackBaseFame(String exactItemId) {
        int tier = tierFromId(exactItemId);
        if (tier < 2 || tier > 8) {
            return 0L;
        }

        String fameClass = fallbackFameClass(exactItemId);
        return switch (fameClass) {
            case "TWO" -> switch (tier) {
                case 2 -> 48L;
                case 3 -> 240L;
                case 4 -> 720L;
                case 5 -> 2_880L;
                case 6 -> 8_640L;
                case 7 -> 20_640L;
                case 8 -> 44_640L;
                default -> 0L;
            };
            case "ONE" -> switch (tier) {
                case 2 -> 36L;
                case 3 -> 180L;
                case 4 -> 540L;
                case 5 -> 2_160L;
                case 6 -> 6_480L;
                case 7 -> 15_480L;
                case 8 -> 33_480L;
                default -> 0L;
            };
            case "ARMOR" -> switch (tier) {
                case 2 -> 24L;
                case 3 -> 120L;
                case 4 -> 360L;
                case 5 -> 1_440L;
                case 6 -> 4_320L;
                case 7 -> 10_320L;
                case 8 -> 22_320L;
                default -> 0L;
            };
            case "SMALL" -> switch (tier) {
                case 2 -> 12L;
                case 3 -> 60L;
                case 4 -> 180L;
                case 5 -> 720L;
                case 6 -> 2_160L;
                case 7 -> 5_160L;
                case 8 -> 11_160L;
                default -> 0L;
            };
            default -> 0L;
        };
    }

    private static String fallbackFameClass(String exactItemId) {
        String id = canonicalBaseId(exactItemId);
        if (id == null) {
            return "";
        }

        if (id.contains("_TOOL_")
                || id.contains("2H_TOOL")
                || id.contains("MAIN_TOOL")
                || id.contains("FISHINGROD")
                || id.contains("SIEGEHAMMER")) {
            return "SMALL";
        }
        if (id.contains("_HEAD_")
                || id.contains("_SHOES_")
                || id.contains("_OFF_")
                || id.contains("_CAPE")) {
            return "SMALL";
        }
        if (id.contains("_ARMOR_") || id.contains("_BAG")) {
            return "ARMOR";
        }
        if (id.contains("_2H_")) {
            return "TWO";
        }
        if (id.contains("_MAIN_")) {
            return "ONE";
        }
        return "";
    }

    private static String fallbackJournalType(String exactItemId) {
        String id = exactItemId.toUpperCase(Locale.ROOT);

        if (id.contains("_BAG")
                || id.contains("_CAPE")
                || id.contains("_TOOL_")
                || id.contains("2H_TOOL")
                || id.contains("MAIN_TOOL")
                || id.contains("FISHINGROD")
                || id.contains("SIEGEHAMMER")
                || id.contains("_GATHERER_")) {
            return "TOOLMAKER";
        }

        if (id.contains("_CLOTH_")
                || id.contains("FIRESTAFF")
                || id.contains("HOLYSTAFF")
                || id.contains("ARCANESTAFF")
                || id.contains("FROSTSTAFF")
                || id.contains("CURSEDSTAFF")
                || id.contains("_OFF_BOOK")
                || id.contains("_OFF_ORB")
                || id.contains("_OFF_DEMONSKULL")
                || id.contains("_OFF_TOTEM_KEEPER")
                || id.contains("_OFF_TALISMAN_AVALON")
                || id.contains("_OFF_CENSER_AVALON")) {
            return "MAGE";
        }

        if (id.contains("_LEATHER_")
                || id.contains("_BOW")
                || id.contains("_DAGGER")
                || id.contains("_SPEAR")
                || id.contains("_QUARTERSTAFF")
                || id.contains("NATURESTAFF")
                || id.contains("SHAPESHIFTER")
                || id.contains("_OFF_TORCH")
                || id.contains("_OFF_HORN_KEEPER")
                || id.contains("_OFF_LAMP_UNDEAD")) {
            return "HUNTER";
        }

        if (id.contains("_PLATE_")
                || id.contains("_SWORD")
                || id.contains("_AXE")
                || id.contains("_MACE")
                || id.contains("_HAMMER")
                || id.contains("_CROSSBOW")
                || id.contains("_RAPIER")
                || id.contains("_KNUCKLES")
                || id.contains("_OFF_SHIELD")
                || id.contains("_OFF_TOWERSHIELD")) {
            return "WARRIOR";
        }

        return null;
    }

    private static long fallbackJournalCapacity(int tier) {
        return switch (Math.max(2, Math.min(8, tier))) {
            case 2 -> 900L;
            case 3 -> 1_800L;
            case 4 -> 3_600L;
            case 5 -> 7_200L;
            case 6 -> 14_400L;
            case 7 -> 28_380L;
            case 8 -> 58_590L;
            default -> 0L;
        };
    }

    private static int tierFromId(String exactItemId) {
        String id = canonicalBaseId(exactItemId);
        if (id == null || id.length() < 2 || id.charAt(0) != 'T') {
            return 0;
        }

        char tier = id.charAt(1);
        return tier >= '0' && tier <= '9' ? tier - '0' : 0;
    }

    private static String canonicalBaseId(String exactItemId) {
        if (exactItemId == null) {
            return null;
        }

        String id = exactItemId.trim().toUpperCase(Locale.ROOT);
        int at = id.indexOf('@');
        if (at >= 0) {
            id = id.substring(0, at);
        }

        return id.replaceFirst("_LEVEL[1-4]$", "");
    }
}
