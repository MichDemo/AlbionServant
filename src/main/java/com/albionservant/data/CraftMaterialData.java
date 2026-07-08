package com.albionservant.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps each craftable item to its primary (material1) and secondary (material2) refined materials.
 * Items that use only one material have material2 = N/A.
 *
 * Refined material names used:
 *   Metal Bars | Planks | Leather | Cloth
 */
public class CraftMaterialData {

    public static final String NA = "N/A";

    public record Materials(String material1, String material2) {}

    private static final Map<String, Materials> MATERIAL_MAP = new HashMap<>();

    static {
        // ── WARRIOR ──────────────────────────────────────────────────────────

        // Swords — Metal Bars + Planks
        m("Broadsword",         "Metal Bars", "Planks");
        m("Claymore",           "Metal Bars", "Planks");
        m("Dual Swords",        "Metal Bars", "Planks");
        m("Clarent Blade",      "Metal Bars", "Planks");
        m("Carving Sword",      "Metal Bars", "Planks");
        m("Galatine Pair",      "Metal Bars", "Planks");
        m("Kingmaker",          "Metal Bars", "Planks");
        m("Infinity Blade",     "Metal Bars", "Planks");

        // Axes — Metal Bars + Planks
        m("Battleaxe",          "Metal Bars", "Planks");
        m("Greataxe",           "Metal Bars", "Planks");
        m("Halberd",            "Metal Bars", "Planks");
        m("Carrioncaller",      "Metal Bars", "Planks");
        m("Infernal Scythe",    "Metal Bars", "Planks");
        m("Bear Paws",          "Metal Bars", "Planks");
        m("Realmbreaker",       "Metal Bars", "Planks");
        m("Crystal Reaper",     "Metal Bars", "Planks");

        // Maces — Metal Bars + Planks
        m("Mace",               "Metal Bars", "Planks");
        m("Heavy Mace",         "Metal Bars", "Planks");
        m("Morning Star",       "Metal Bars", "Planks");
        m("Bedrock Mace",       "Metal Bars", "Planks");
        m("Incubus Mace",       "Metal Bars", "Planks");
        m("Camlann Mace",       "Metal Bars", "Planks");
        m("Oathkeepers",        "Metal Bars", "Planks");
        m("Dreadstorm Monarch", "Metal Bars", "Planks");

        // Hammers — Metal Bars only (1H hammers) / Metal Bars + Planks (2H)
        m("Hammer",             "Metal Bars", NA);
        m("Polehammer",         "Metal Bars", "Planks");
        m("Great Hammer",       "Metal Bars", "Planks");
        m("Tombhammer",         "Metal Bars", "Planks");
        m("Forge Hammers",      "Metal Bars", "Planks");
        m("Grovekeeper",        "Metal Bars", "Planks");
        m("Hand of Justice",    "Metal Bars", "Planks");
        m("Truebolt Hammer",    "Metal Bars", "Planks");

        // War Gloves — Metal Bars + Leather
        m("Brawler Gloves",      "Metal Bars", "Leather");
        m("Battle Bracers",      "Metal Bars", "Leather");
        m("Spiked Gauntlets",    "Metal Bars", "Leather");
        m("Ursine Maulers",      "Metal Bars", "Leather");
        m("Hellfire Hands",      "Metal Bars", "Leather");
        m("Ravenstrike Cestus",  "Metal Bars", "Leather");
        m("Fists of Avalon",     "Metal Bars", "Leather");
        m("Forcepulse Bracers",  "Metal Bars", "Leather");

        // Crossbows — Metal Bars + Planks
        m("Crossbow",           "Metal Bars", "Planks");
        m("Heavy Crossbow",     "Metal Bars", "Planks");
        m("Light Crossbow",     "Metal Bars", "Planks");
        m("Weeping Repeater",   "Metal Bars", "Planks");
        m("Boltcasters",        "Metal Bars", "Planks");
        m("Siegebow",           "Metal Bars", "Planks");
        m("Energy Shaper",      "Metal Bars", "Planks");
        m("Arclight Blasters",  "Metal Bars", "Planks");

        // Shields — Metal Bars + Planks
        m("Shield",             "Metal Bars", "Planks");
        m("Sarcophagus",        "Metal Bars", "Planks");
        m("Caitiff Shield",     "Metal Bars", "Planks");
        m("Facebreaker",        "Metal Bars", "Planks");
        m("Astral Aegis",       "Metal Bars", "Planks");
        m("Unbreakable Ward",   "Metal Bars", "Planks");

        // Plate Helmets — Metal Bars only
        m("Demon Helmet",       "Metal Bars", NA);
        m("Graveguard Helmet",  "Metal Bars", NA);
        m("Guardian Helmet",    "Metal Bars", NA);
        m("Helmet of Valor",    "Metal Bars", NA);
        m("Judicator Helmet",   "Metal Bars", NA);
        m("Knight Helmet",      "Metal Bars", NA);
        m("Royal Helmet",       "Metal Bars", NA);
        m("Soldier Helmet",     "Metal Bars", NA);

        // Plate Armor — Metal Bars only
        m("Demon Armor",        "Metal Bars", NA);
        m("Graveguard Armor",   "Metal Bars", NA);
        m("Guardian Armor",     "Metal Bars", NA);
        m("Armor of Valor",     "Metal Bars", NA);
        m("Judicator Armor",    "Metal Bars", NA);
        m("Knight Armor",       "Metal Bars", NA);
        m("Royal Armor",        "Metal Bars", NA);
        m("Soldier Armor",      "Metal Bars", NA);

        // Plate Boots — Metal Bars only
        m("Demon Boots",        "Metal Bars", NA);
        m("Graveguard Boots",   "Metal Bars", NA);
        m("Guardian Boots",     "Metal Bars", NA);
        m("Boots of Valor",     "Metal Bars", NA);
        m("Judicator Boots",    "Metal Bars", NA);
        m("Knight Boots",       "Metal Bars", NA);
        m("Royal Boots",        "Metal Bars", NA);
        m("Soldier Boots",      "Metal Bars", NA);

        // ── HUNTER ───────────────────────────────────────────────────────────

        // Bows — Planks only (all bows are 2H, wood only)
        m("Bow",                "Planks", NA);
        m("Warbow",             "Planks", NA);
        m("Longbow",            "Planks", NA);
        m("Whispering Bow",     "Planks", NA);
        m("Wailing Bow",        "Planks", NA);
        m("Bow of Badon",       "Planks", NA);
        m("Mistpiercer",        "Planks", NA);
        m("Skystrider Bow",     "Planks", NA);

        // Daggers — Metal Bars + Leather
        m("Dagger",             "Metal Bars", "Leather");
        m("Dagger Pair",        "Metal Bars", "Leather");
        m("Claws",              "Metal Bars", "Leather");
        m("Bloodletter",        "Metal Bars", "Leather");
        m("Demonfang",          "Metal Bars", "Leather");
        m("Deathgivers",        "Metal Bars", "Leather");
        m("Bridled Fury",       "Metal Bars", "Leather");
        m("Twin Slayers",       "Metal Bars", "Leather");

        // Spears — Metal Bars + Planks
        m("Spear",              "Metal Bars", "Planks");
        m("Pike",               "Metal Bars", "Planks");
        m("Glaive",             "Metal Bars", "Planks");
        m("Heron Spear",        "Metal Bars", "Planks");
        m("Spirithunter",       "Metal Bars", "Planks");
        m("Trinity Spear",      "Metal Bars", "Planks");
        m("Daybreaker",         "Metal Bars", "Planks");
        m("Rift Glaive",        "Metal Bars", "Planks");

        // Quarterstaves — Planks only
        m("Quarterstaff",           "Planks", NA);
        m("Iron-clad Staff",        "Planks", NA);
        m("Double Bladed Staff",    "Planks", NA);
        m("Black Monk Stave",       "Planks", NA);
        m("Soulscythe",             "Planks", NA);
        m("Staff of Balance",       "Planks", NA);
        m("Grailseeker",            "Planks", NA);
        m("Phantom Twinblade",      "Planks", NA);

        // Shapeshifter Staves — Planks + Leather
        m("Prowling Staff",     "Planks", "Leather");
        m("Rootbound Staff",    "Planks", "Leather");
        m("Primal Staff",       "Planks", "Leather");
        m("Bloodmoon Staff",    "Planks", "Leather");
        m("Hellspawn Staff",    "Planks", "Leather");
        m("Earthrune Staff",    "Planks", "Leather");
        m("Lightcaller",        "Planks", "Leather");
        m("Stillgaze Staff",    "Planks", "Leather");

        // Nature Staves — Planks + Leather
        m("Nature Staff",       "Planks", "Leather");
        m("Great Nature Staff", "Planks", "Leather");
        m("Wild Staff",         "Planks", "Leather");
        m("Druidic Staff",      "Planks", "Leather");
        m("Blight Staff",       "Planks", "Leather");
        m("Rampant Staff",      "Planks", "Leather");
        m("Ironroot Staff",     "Planks", "Leather");
        m("Forgebark Staff",    "Planks", "Leather");

        // Torches — Planks + Cloth
        m("Torch",              "Planks", "Cloth");
        m("Mistcaller",         "Planks", "Cloth");
        m("Leering Cane",       "Planks", "Cloth");
        m("Cryptcandle",        "Planks", "Cloth");
        m("Sacred Scepter",     "Planks", "Cloth");
        m("Blueflame Torch",    "Planks", "Cloth");

        // Leather Hoods — Leather only
        m("Assassin Hood",      "Leather", NA);
        m("Hellion Hood",       "Leather", NA);
        m("Hood of Tenacity",   "Leather", NA);
        m("Hunter Hood",        "Leather", NA);
        m("Mercenary Hood",     "Leather", NA);
        m("Royal Hood",         "Leather", NA);
        m("Specter Hood",       "Leather", NA);
        m("Stalker Hood",       "Leather", NA);

        // Leather Jackets — Leather only
        m("Assassin Jacket",    "Leather", NA);
        m("Hellion Jacket",     "Leather", NA);
        m("Jacket of Tenacity", "Leather", NA);
        m("Hunter Jacket",      "Leather", NA);
        m("Mercenary Jacket",   "Leather", NA);
        m("Royal Jacket",       "Leather", NA);
        m("Specter Jacket",     "Leather", NA);
        m("Stalker Jacket",     "Leather", NA);

        // Leather Shoes — Leather only
        m("Assassin Shoes",     "Leather", NA);
        m("Hellion Shoes",      "Leather", NA);
        m("Shoes of Tenacity",  "Leather", NA);
        m("Hunter Shoes",       "Leather", NA);
        m("Mercenary Shoes",    "Leather", NA);
        m("Royal Shoes",        "Leather", NA);
        m("Specter Shoes",      "Leather", NA);
        m("Stalker Shoes",      "Leather", NA);

        // ── MAGE ─────────────────────────────────────────────────────────────

        // Fire Staves — Planks + Cloth
        m("Fire Staff",         "Planks", "Cloth");
        m("Great Fire Staff",   "Planks", "Cloth");
        m("Infernal Staff",     "Planks", "Cloth");
        m("Wildfire Staff",     "Planks", "Cloth");
        m("Brimstone Staff",    "Planks", "Cloth");
        m("Blazing Staff",      "Planks", "Cloth");
        m("Dawnsong",           "Planks", "Cloth");
        m("Flamewalker Staff",  "Planks", "Cloth");

        // Holy Staves — Planks + Cloth
        m("Holy Staff",         "Planks", "Cloth");
        m("Great Holy Staff",   "Planks", "Cloth");
        m("Divine Staff",       "Planks", "Cloth");
        m("Lifetouch Staff",    "Planks", "Cloth");
        m("Fallen Staff",       "Planks", "Cloth");
        m("Redemption Staff",   "Planks", "Cloth");
        m("Hallowfall",         "Planks", "Cloth");
        m("Exalted Staff",      "Planks", "Cloth");

        // Arcane Staves — Planks + Cloth
        m("Arcane Staff",       "Planks", "Cloth");
        m("Great Arcane Staff", "Planks", "Cloth");
        m("Enigmatic Staff",    "Planks", "Cloth");
        m("Witchwork Staff",    "Planks", "Cloth");
        m("Occult Staff",       "Planks", "Cloth");
        m("Malevolent Locus",   "Planks", "Cloth");
        m("Evensong",           "Planks", "Cloth");
        m("Astral Staff",       "Planks", "Cloth");

        // Frost Staves — Planks + Cloth
        m("Frost Staff",        "Planks", "Cloth");
        m("Great Frost Staff",  "Planks", "Cloth");
        m("Glacial Staff",      "Planks", "Cloth");
        m("Hoarfrost Staff",    "Planks", "Cloth");
        m("Icicle Staff",       "Planks", "Cloth");
        m("Permafrost Prism",   "Planks", "Cloth");
        m("Chillhowl",          "Planks", "Cloth");
        m("Arctic Staff",       "Planks", "Cloth");

        // Cursed Staves — Planks + Cloth
        m("Cursed Staff",       "Planks", "Cloth");
        m("Great Cursed Staff", "Planks", "Cloth");
        m("Demonic Staff",      "Planks", "Cloth");
        m("Lifecurse Staff",    "Planks", "Cloth");
        m("Cursed Skull",       "Planks", "Cloth");
        m("Damnation Staff",    "Planks", "Cloth");
        m("Shadowcaller",       "Planks", "Cloth");
        m("Rotcaller Staff",    "Planks", "Cloth");

        // Tomes — Planks + Cloth
        m("Tome of Spells",         "Planks", "Cloth");
        m("Eye of Secrets",         "Planks", "Cloth");
        m("Muisak",                 "Planks", "Cloth");
        m("Taproot",                "Planks", "Cloth");
        m("Celestial Censer",       "Planks", "Cloth");
        m("Timelocked Grimoire",    "Planks", "Cloth");

        // Cloth Cowls — Cloth only
        m("Cleric Cowl",        "Cloth", NA);
        m("Cowl of Purity",     "Cloth", NA);
        m("Cultist Cowl",       "Cloth", NA);
        m("Druid Cowl",         "Cloth", NA);
        m("Fiend Cowl",         "Cloth", NA);
        m("Mage Cowl",          "Cloth", NA);
        m("Royal Cowl",         "Cloth", NA);
        m("Scholar Cowl",       "Cloth", NA);

        // Cloth Robes — Cloth only
        m("Cleric Robe",        "Cloth", NA);
        m("Robe of Purity",     "Cloth", NA);
        m("Cultist Robe",       "Cloth", NA);
        m("Druid Robe",         "Cloth", NA);
        m("Fiend Robe",         "Cloth", NA);
        m("Mage Robe",          "Cloth", NA);
        m("Royal Robe",         "Cloth", NA);
        m("Scholar Robe",       "Cloth", NA);

        // Cloth Sandals — Cloth only
        m("Cleric Sandals",     "Cloth", NA);
        m("Sandals of Purity",  "Cloth", NA);
        m("Cultist Sandals",    "Cloth", NA);
        m("Druid Sandals",      "Cloth", NA);
        m("Fiend Sandals",      "Cloth", NA);
        m("Mage Sandals",       "Cloth", NA);
        m("Royal Sandals",      "Cloth", NA);
        m("Scholar Sandals",    "Cloth", NA);

        // ── TOOLMAKER ────────────────────────────────────────────────────────

        // Gathering Tools — metal head + wooden handle = Metal Bars + Planks
        // Exception: Fishing Rod is wood-only, Siege Hammer is metal-only
        m("Sickle",                 "Metal Bars", "Planks");
        m("Wood Axe",               "Metal Bars", "Planks");
        m("Pickaxe",                "Metal Bars", "Planks");
        m("Stone Hammer",           "Metal Bars", "Planks");
        m("Skinning Knife",         "Metal Bars", "Planks");
        m("Fishing Rod",            "Planks",     NA);
        m("Siege Hammer (regular)", "Metal Bars", "Planks");

        // Harvester Gear — gathers Fiber → refined to Cloth → gear uses Cloth only
        m("Harvester Cap",          "Cloth",        NA);
        m("Harvester Garb",         "Cloth",        NA);
        m("Harvester Workboots",    "Cloth",        NA);
        m("Harvester Backpack",     "Cloth",        NA);

        // Lumberjack Gear — gathers Logs → refined to Planks → gear uses Planks only
        m("Lumberjack Cap",         "Planks",       NA);
        m("Lumberjack Garb",        "Planks",       NA);
        m("Lumberjack Workboots",   "Planks",       NA);
        m("Lumberjack Backpack",    "Planks",       NA);

        // Miner Gear — gathers Ore → refined to Metal Bars → gear uses Metal Bars only
        m("Miner Cap",              "Metal Bars",   NA);
        m("Miner Garb",             "Metal Bars",   NA);
        m("Miner Workboots",        "Metal Bars",   NA);
        m("Miner Backpack",         "Metal Bars",   NA);

        // Quarrier Gear — gathers Stone → refined to Stone Blocks → gear uses Metal Bars only
        m("Quarrier Cap",           "Metal Bars", NA);
        m("Quarrier Garb",          "Metal Bars", NA);
        m("Quarrier Workboots",     "Metal Bars", NA);
        m("Quarrier Backpack",      "Metal Bars", NA);

        // Skinner Gear — gathers Hide → refined to Leather → gear uses Leather only
        m("Skinner Cap",            "Leather",      NA);
        m("Skinner Garb",           "Leather",      NA);
        m("Skinner Workboots",      "Leather",      NA);
        m("Skinner Backpack",       "Leather",      NA);

        // Fisherman Gear — uses Cloth only (same as Harvester, fishing is fibre-adjacent)
        m("Fisherman Cap",          "Cloth",        NA);
        m("Fisherman Garb",         "Cloth",        NA);
        m("Fisherman Workboots",    "Cloth",        NA);
        m("Fisherman Backpack",     "Cloth",        NA);

        // Bags — Cloth + Leather
        m("Bag",                    "Cloth",  "Leather");
        m("Satchel of Insight",     "Cloth",  "Leather");

        // Capes — Cloth + Leather
        m("Cape",               "Cloth", "Leather");
        m("Thetford Cape",      "Cloth", "Leather");
        m("Fort Sterling Cape", "Cloth", "Leather");
        m("Lymhurst Cape",      "Cloth", "Leather");
        m("Bridgewatch Cape",   "Cloth", "Leather");
        m("Martlock Cape",      "Cloth", "Leather");
        m("Caerleon Cape",      "Cloth", "Leather");
        m("Brecilien Cape",     "Cloth", "Leather");
        m("Avalonian Cape",     "Cloth", "Leather");
        m("Heretic Cape",       "Cloth", "Leather");
        m("Undead Cape",        "Cloth", "Leather");
        m("Keeper Cape",        "Cloth", "Leather");
        m("Morgana Cape",       "Cloth", "Leather");
        m("Demon Cape",         "Cloth", "Leather");
        m("Smuggler Cape",      "Cloth", "Leather");

        // ── TOOLMAKER — Avalonian tools ───────────────────────────────────────
        // Identical base materials to their regular counterpart.
        // Avalonian Energy appears as material3 — handled via ArtifactData.
        m("Avalonian Sickle",         "Metal Bars", "Planks");
        m("Avalonian Axe",            "Metal Bars", "Planks");
        m("Avalonian Pickaxe",        "Metal Bars", "Planks");
        m("Avalonian Stone Hammer",   "Metal Bars", "Planks");
        m("Avalonian Skinning Knife", "Metal Bars", "Planks");
        m("Avalonian Fishing Rod",    "Planks",     NA);
        m("Avalonian Siege Hammer",   "Metal Bars", "Planks");
    }

    private static void m(String item, String mat1, String mat2) {
        MATERIAL_MAP.put(item, new Materials(mat1, mat2));
    }

    /** Returns the material pair for an item, or null if unknown. */
    public static Materials getMaterials(String itemName) {
        return MATERIAL_MAP.getOrDefault(itemName, new Materials("Unknown", NA));
    }
}