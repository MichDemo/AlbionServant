package com.albionservant.data;

import java.util.List;

/**
 * Refining data for all 5 material families in Albion Online.
 *
 * Refining ratios (confirmed from wiki):
 *   T2: 4 raw → 1 refined            (no secondary material)
 *   T3+: 2 raw (this tier) + 1 refined (previous tier) → 1 refined (this tier)
 *
 * Enchanted refining (.1/.2/.3/.4):
 *   Same ratio — uses enchanted raw + unenchanted previous-tier refined
 *   → outputs the same enchantment level refined material
 *
 * Each MaterialFamily defines:
 *   - displayName: shown in UI headers
 *   - rawNames[T2..T8]: name of the raw resource per tier
 *   - refinedNames[T2..T8]: name of the refined output per tier
 *   - iconId: Albion render API ID for the refined material icon
 */
public class RefineData {

    public record MaterialFamily(
            String   displayName,
            String   rawLabel,         // e.g. "Ore", "Logs"
            String[] rawNames,         // index 0 = T2, index 6 = T8
            String[] refinedNames,     // index 0 = T2, index 6 = T8
            String[] rawIconIds,       // render API IDs for each raw tier
            String[] refinedIconIds    // render API IDs for each refined tier
    ) {}

    // ── Refining ratios ────────────────────────────────────────────────────────
    public static final int T2_RAW_PER_OUTPUT     = 4;  // 4 raw T2 → 1 refined T2
    public static final int T3PLUS_RAW_PER_OUTPUT = 2;  // 2 raw T(n) + 1 refined T(n-1) → 1 refined T(n)
    public static final int PREV_REFINED_PER_OUTPUT = 1;

    // ── Material families ──────────────────────────────────────────────────────

    public static final MaterialFamily METAL_BARS = new MaterialFamily(
            "Metal Bars", "Ore",
            new String[]{"Iron Ore",       "Titanium Ore",  "Steel Ore",
                    "Runite Ore",     "Meteorite Ore", "Adamantine Ore", "Nightstone Ore"},
            new String[]{"Rough Metal Bar","Metal Bar",     "Worked Metal Bar",
                    "Polished Metal Bar","Hardened Metal Bar","Reinforced Metal Bar","Infused Metal Bar"},
            new String[]{"T2_ORE","T3_ORE","T4_ORE","T5_ORE","T6_ORE","T7_ORE","T8_ORE"},
            new String[]{"T2_METALBAR","T3_METALBAR","T4_METALBAR","T5_METALBAR",
                    "T6_METALBAR","T7_METALBAR","T8_METALBAR"}
    );

    public static final MaterialFamily PLANKS = new MaterialFamily(
            "Planks", "Logs",
            new String[]{"Pine Log",       "Cedar Log",    "Bloodoak Log",
                    "Ashenbark Log",  "Whitewood Log","Ghostroot Log","Sunbright Log"},
            new String[]{"Pine Plank",     "Cedar Plank",  "Bloodoak Plank",
                    "Ashenbark Plank","Whitewood Plank","Ghostroot Plank","Sunbright Plank"},
            new String[]{"T2_WOOD","T3_WOOD","T4_WOOD","T5_WOOD","T6_WOOD","T7_WOOD","T8_WOOD"},
            new String[]{"T2_PLANKS","T3_PLANKS","T4_PLANKS","T5_PLANKS",
                    "T6_PLANKS","T7_PLANKS","T8_PLANKS"}
    );

    public static final MaterialFamily LEATHER = new MaterialFamily(
            "Leather", "Hide",
            new String[]{"Rugged Hide",   "Stiff Hide",   "Thick Hide",
                    "Resilient Hide","Robust Hide",  "Fortified Hide","Reinforced Hide"},
            new String[]{"Thin Leather",  "Medium Leather","Heavy Leather",
                    "Worked Leather","Cured Leather","Hardened Leather","Fortified Leather"},
            new String[]{"T2_HIDE","T3_HIDE","T4_HIDE","T5_HIDE","T6_HIDE","T7_HIDE","T8_HIDE"},
            new String[]{"T2_LEATHER","T3_LEATHER","T4_LEATHER","T5_LEATHER",
                    "T6_LEATHER","T7_LEATHER","T8_LEATHER"}
    );

    public static final MaterialFamily CLOTH = new MaterialFamily(
            "Cloth", "Fiber",
            new String[]{"Cotton",         "Wool",         "Undine's Touch",
                    "Sunset Yarrow",  "Pearl Cotton", "Ghoul Yarrow",  "Nightthread Hemp"},
            new String[]{"Simple Cloth",   "Neat Cloth",   "Fine Cloth",
                    "Ornate Cloth",   "Spun Cloth",   "Latent Cloth",  "Occult Cloth"},
            new String[]{"T2_FIBER","T3_FIBER","T4_FIBER","T5_FIBER","T6_FIBER","T7_FIBER","T8_FIBER"},
            new String[]{"T2_CLOTH","T3_CLOTH","T4_CLOTH","T5_CLOTH",
                    "T6_CLOTH","T7_CLOTH","T8_CLOTH"}
    );

    public static final MaterialFamily STONE_BLOCKS = new MaterialFamily(
            "Stone Blocks", "Stone",
            new String[]{"Limestone",      "Sandstone",    "Travertine",
                    "Granite",        "Slate",        "Basalt","Marble"},
            new String[]{"Limestone Block","Sandstone Block","Travertine Block",
                    "Granite Block",  "Slate Block",  "Basalt Block","Marble Block"},
            new String[]{"T2_ROCK","T3_ROCK","T4_ROCK","T5_ROCK","T6_ROCK","T7_ROCK","T8_ROCK"},
            new String[]{"T2_STONEBLOCK","T3_STONEBLOCK","T4_STONEBLOCK","T5_STONEBLOCK",
                    "T6_STONEBLOCK","T7_STONEBLOCK","T8_STONEBLOCK"}
    );

    public static final List<MaterialFamily> ALL = List.of(
            METAL_BARS, PLANKS, LEATHER, CLOTH, STONE_BLOCKS
    );

    /** Tiers available: T2–T8 (index 0 = T2) */
    public static final int[] TIERS = {2, 3, 4, 5, 6, 7, 8};

    /** Enchantment suffixes per tier row.
     *  T2 has only .0; T3-T8 have .0 / .1 / .2 / .3 / .4 */
    public static int[] enchantmentsForTier(int tier) {
        return tier == 2 ? new int[]{0} : new int[]{0, 1, 2, 3, 4};
    }

    /** Raw material quantity needed per 1 refined output */
    public static int rawPerOutput(int tier) {
        return tier == 2 ? T2_RAW_PER_OUTPUT : T3PLUS_RAW_PER_OUTPUT;
    }

    /** How many previous-tier refined are needed per 1 output (0 for T2) */
    public static int prevRefinedPerOutput(int tier) {
        return tier == 2 ? 0 : PREV_REFINED_PER_OUTPUT;
    }
}