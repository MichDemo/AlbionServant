package com.albionservant.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CraftData {

    private static final Map<String, List<String>> categoryChildren = new HashMap<>();

    static {
        initializeCategories();
    }

    private static void initializeCategories() {
        categoryChildren.put("ROOT", List.of("Gear", "Food", "Potion"));

        categoryChildren.put("Gear", List.of("Warrior", "Hunter", "Mage", "Toolmaker"));

        categoryChildren.put("Food",    Collections.emptyList());
        categoryChildren.put("Potion",  Collections.emptyList());

        // ── WARRIOR ──────────────────────────────────────────────────────────
        categoryChildren.put("Warrior", List.of(
                "Swords", "Axes", "Maces", "Hammers", "War Gloves",
                "Crossbows", "Shields", "Plate Helmets", "Plate Armor", "Plate Boots"
        ));

        categoryChildren.put("Swords", List.of(
                "Broadsword", "Claymore", "Dual Swords",
                "Clarent Blade", "Carving Sword", "Galatine Pair",
                "Kingmaker", "Infinity Blade"
        ));

        categoryChildren.put("Axes", List.of(
                "Battleaxe", "Greataxe", "Halberd",
                "Carrioncaller", "Infernal Scythe", "Bear Paws",
                "Realmbreaker", "Crystal Reaper"
        ));

        categoryChildren.put("Maces", List.of(
                "Mace", "Heavy Mace", "Morning Star",
                "Bedrock Mace", "Incubus Mace", "Camlann Mace",
                "Oathkeepers", "Dreadstorm Monarch"
        ));

        categoryChildren.put("Hammers", List.of(
                "Hammer", "Polehammer", "Great Hammer",
                "Tombhammer", "Forge Hammers", "Grovekeeper",
                "Hand of Justice", "Truebolt Hammer"
        ));

        categoryChildren.put("War Gloves", List.of(
                "Brawler Gloves", "Battle Bracers", "Spiked Gauntlets",
                "Ursine Maulers", "Hellfire Hands", "Ravenstrike Cestus",
                "Fists of Avalon", "Forcepulse Bracers"
        ));

        categoryChildren.put("Crossbows", List.of(
                "Crossbow", "Heavy Crossbow", "Light Crossbow",
                "Weeping Repeater", "Boltcasters", "Siegebow",
                "Energy Shaper", "Arclight Blasters"
        ));

        categoryChildren.put("Shields", List.of(
                "Shield", "Sarcophagus", "Caitiff Shield",
                "Facebreaker", "Astral Aegis", "Unbreakable Ward"
        ));

        categoryChildren.put("Plate Helmets", List.of(
                "Demon Helmet", "Graveguard Helmet", "Guardian Helmet",
                "Helmet of Valor", "Judicator Helmet", "Knight Helmet",
                "Royal Helmet", "Soldier Helmet"
        ));

        categoryChildren.put("Plate Armor", List.of(
                "Demon Armor", "Graveguard Armor", "Guardian Armor",
                "Armor of Valor", "Judicator Armor", "Knight Armor",
                "Royal Armor", "Soldier Armor"
        ));

        categoryChildren.put("Plate Boots", List.of(
                "Demon Boots", "Graveguard Boots", "Guardian Boots",
                "Boots of Valor", "Judicator Boots", "Knight Boots",
                "Royal Boots", "Soldier Boots"
        ));

        // All leaf items (sub-sub level) have no children — resolved via getChildren() returning empty

        // ── HUNTER ───────────────────────────────────────────────────────────
        categoryChildren.put("Hunter", List.of(
                "Bows", "Daggers", "Spears", "Quarterstaves",
                "Shapeshifter Staves", "Nature Staves", "Torches",
                "Leather Hoods", "Leather Jackets", "Leather Shoes"
        ));

        categoryChildren.put("Bows", List.of(
                "Bow", "Warbow", "Longbow",
                "Whispering Bow", "Wailing Bow", "Bow of Badon",
                "Mistpiercer", "Skystrider Bow"
        ));

        categoryChildren.put("Daggers", List.of(
                "Dagger", "Dagger Pair", "Claws",
                "Bloodletter", "Demonfang", "Deathgivers",
                "Bridled Fury", "Twin Slayers"
        ));

        categoryChildren.put("Spears", List.of(
                "Spear", "Pike", "Glaive",
                "Heron Spear", "Spirithunter", "Trinity Spear",
                "Daybreaker", "Rift Glaive"
        ));

        categoryChildren.put("Quarterstaves", List.of(
                "Quarterstaff", "Iron-clad Staff", "Double Bladed Staff",
                "Black Monk Stave", "Soulscythe", "Staff of Balance",
                "Grailseeker", "Phantom Twinblade"
        ));

        categoryChildren.put("Shapeshifter Staves", List.of(
                "Prowling Staff", "Rootbound Staff", "Primal Staff",
                "Bloodmoon Staff", "Hellspawn Staff", "Earthrune Staff",
                "Lightcaller", "Stillgaze Staff"
        ));

        categoryChildren.put("Nature Staves", List.of(
                "Nature Staff", "Great Nature Staff", "Wild Staff",
                "Druidic Staff", "Blight Staff", "Rampant Staff",
                "Ironroot Staff", "Forgebark Staff"
        ));

        categoryChildren.put("Torches", List.of(
                "Torch", "Mistcaller", "Leering Cane",
                "Cryptcandle", "Sacred Scepter", "Blueflame Torch"
        ));

        categoryChildren.put("Leather Hoods", List.of(
                "Assassin Hood", "Hellion Hood", "Hood of Tenacity",
                "Hunter Hood", "Mercenary Hood", "Royal Hood",
                "Specter Hood", "Stalker Hood"
        ));

        categoryChildren.put("Leather Jackets", List.of(
                "Assassin Jacket", "Hellion Jacket", "Jacket of Tenacity",
                "Hunter Jacket", "Mercenary Jacket", "Royal Jacket",
                "Specter Jacket", "Stalker Jacket"
        ));

        categoryChildren.put("Leather Shoes", List.of(
                "Assassin Shoes", "Hellion Shoes", "Shoes of Tenacity",
                "Hunter Shoes", "Mercenary Shoes", "Royal Shoes",
                "Specter Shoes", "Stalker Shoes"
        ));

        // ── MAGE ─────────────────────────────────────────────────────────────
        categoryChildren.put("Mage", List.of(
                "Fire Staves", "Holy Staves", "Arcane Staves",
                "Frost Staves", "Cursed Staves", "Tomes",
                "Cloth Cowls", "Cloth Robes", "Cloth Sandals"
        ));

        categoryChildren.put("Fire Staves", List.of(
                "Fire Staff", "Great Fire Staff", "Infernal Staff",
                "Wildfire Staff", "Brimstone Staff", "Blazing Staff",
                "Dawnsong", "Flamewalker Staff"
        ));

        categoryChildren.put("Holy Staves", List.of(
                "Holy Staff", "Great Holy Staff", "Divine Staff",
                "Lifetouch Staff", "Fallen Staff", "Redemption Staff",
                "Hallowfall", "Exalted Staff"
        ));

        categoryChildren.put("Arcane Staves", List.of(
                "Arcane Staff", "Great Arcane Staff", "Enigmatic Staff",
                "Witchwork Staff", "Occult Staff", "Malevolent Locus",
                "Evensong", "Astral Staff"
        ));

        categoryChildren.put("Frost Staves", List.of(
                "Frost Staff", "Great Frost Staff", "Glacial Staff",
                "Hoarfrost Staff", "Icicle Staff", "Permafrost Prism",
                "Chillhowl", "Arctic Staff"
        ));

        categoryChildren.put("Cursed Staves", List.of(
                "Cursed Staff", "Great Cursed Staff", "Demonic Staff",
                "Lifecurse Staff", "Cursed Skull", "Damnation Staff",
                "Shadowcaller", "Rotcaller Staff"
        ));

        categoryChildren.put("Tomes", List.of(
                "Tome of Spells", "Eye of Secrets", "Muisak",
                "Taproot", "Celestial Censer", "Timelocked Grimoire"
        ));

        categoryChildren.put("Cloth Cowls", List.of(
                "Cleric Cowl", "Cowl of Purity", "Cultist Cowl",
                "Druid Cowl", "Fiend Cowl", "Mage Cowl",
                "Royal Cowl", "Scholar Cowl"
        ));

        categoryChildren.put("Cloth Robes", List.of(
                "Cleric Robe", "Robe of Purity", "Cultist Robe",
                "Druid Robe", "Fiend Robe", "Mage Robe",
                "Royal Robe", "Scholar Robe"
        ));

        categoryChildren.put("Cloth Sandals", List.of(
                "Cleric Sandals", "Sandals of Purity", "Cultist Sandals",
                "Druid Sandals", "Fiend Sandals", "Mage Sandals",
                "Royal Sandals", "Scholar Sandals"
        ));

        // ── TOOLMAKER ────────────────────────────────────────────────────────
        // Each gathering profession is one sub-category bundling:
        //   tool + cap + garb + workboots + backpack (bag)
        // Plus standalone: Demolition Hammer, Bags, Capes
        categoryChildren.put("Toolmaker", List.of(
                "Harvester",    // Fiber — Sickle
                "Lumberjack",   // Wood  — Wood Axe
                "Miner",        // Ore   — Pickaxe
                "Quarrier",     // Stone — Stone Hammer
                "Skinner",      // Hide  — Skinning Knife
                "Fisherman",    // Fish  — Fishing Rod
                "Demolition Hammer",
                "Bags",
                "Capes"
        ));

        // Harvester — Fiber gathering (Sickle + gear + bag)
        categoryChildren.put("Harvester", List.of(
                "Sickle",
                "Avalonian Sickle",
                "Harvester Cap",
                "Harvester Garb",
                "Harvester Workboots",
                "Harvester Backpack"
        ));

        // Lumberjack — Wood gathering (Wood Axe + gear + bag)
        categoryChildren.put("Lumberjack", List.of(
                "Axe",
                "Avalonian Axe",
                "Lumberjack Cap",
                "Lumberjack Garb",
                "Lumberjack Workboots",
                "Lumberjack Backpack"
        ));

        // Miner — Ore gathering (Pickaxe + gear + bag)
        categoryChildren.put("Miner", List.of(
                "Pickaxe",
                "Avalonian Pickaxe",
                "Miner Cap",
                "Miner Garb",
                "Miner Workboots",
                "Miner Backpack"
        ));

        // Quarrier — Stone gathering (Stone Hammer + gear + bag)
        categoryChildren.put("Quarrier", List.of(
                "Stone Hammer",
                "Avalonian Stone Hammer",
                "Quarrier Cap",
                "Quarrier Garb",
                "Quarrier Workboots",
                "Quarrier Backpack"
        ));

        // Skinner — Hide gathering (Skinning Knife + gear + bag)
        categoryChildren.put("Skinner", List.of(
                "Skinning Knife",
                "Avalonian Skinning Knife",
                "Skinner Cap",
                "Skinner Garb",
                "Skinner Workboots",
                "Skinner Backpack"
        ));

        // Fisherman — Fishing (Fishing Rod + gear + bag)
        categoryChildren.put("Fisherman", List.of(
                "Fishing Rod",
                "Avalonian Fishing Rod",
                "Fisherman Cap",
                "Fisherman Garb",
                "Fisherman Workboots",
                "Fisherman Backpack"
        ));

        // Demolition Hammer
        categoryChildren.put("Demolition Hammer", List.of(
           "Demolition Hammer",
           "Avalonian Demolition Hammer"
        ));

        // Bags (non-gathering bags)
        categoryChildren.put("Bags", List.of(
                "Bag",
                "Satchel of Insight"
        ));

        // Capes — city capes + faction capes
        categoryChildren.put("Capes", List.of(
                "Cape",
                "Thetford Cape",
                "Fort Sterling Cape",
                "Lymhurst Cape",
                "Bridgewatch Cape",
                "Martlock Cape",
                "Caerleon Cape",
                "Brecilien Cape",
                "Avalonian Cape",
                "Heretic Cape",
                "Undead Cape",
                "Keeper Cape",
                "Morgana Cape",
                "Demon Cape",
                "Smuggler Cape"
        ));
    }

    /**
     * Keeps placeholder names for trees not yet implemented,
     * so the navigation still works without crashing.
     */
    private static void addPlaceholderSubTree(String treeName, int subCount) {
        List<String> subs = new java.util.ArrayList<>();
        for (int i = 1; i <= subCount; i++) {
            String subKey = treeName + "_Sub" + i;
            subs.add(subKey);
            List<String> subSubs = List.of(subKey + "_Item1", subKey + "_Item2");
            categoryChildren.put(subKey, subSubs);
        }
        categoryChildren.put(treeName, subs);
    }

    public static List<String> getChildren(String parentKey) {
        return categoryChildren.getOrDefault(parentKey, Collections.emptyList());
    }
}