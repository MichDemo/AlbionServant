package com.albionservant.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps each artifact item name to the type of artifact material it requires.
 * Standard items need no entry — only artifact weapons/armor are listed here.
 *
 * Artifact tiers follow MAJOR tiers only (T4, T5, T6, T7, T8) — no enchanted
 * sub-tiers (.1 / .2 / .3) exist for artifact materials.
 */
public class ArtifactData {

    public enum ArtifactType {
        RUNE    ("Rune Artifact",      "Requires a Rune artifact (from Rune shards at Artifact Foundry)"),
        SOUL    ("Soul Artifact",      "Requires a Soul artifact (from Soul shards at Artifact Foundry)"),
        RELIC   ("Relic Artifact",     "Requires a Relic artifact (from Relic shards at Artifact Foundry)"),
        AVALON  ("Avalonian Artifact", "Requires an Avalonian artifact (from Avalonian shards)"),
        CRYSTAL ("Crystal Artifact",   "Requires a Crystal artifact (from Crystal shards)");

        public final String displayName;
        public final String tooltip;

        ArtifactType(String displayName, String tooltip) {
            this.displayName = displayName;
            this.tooltip = tooltip;
        }
    }

    private static final Map<String, ArtifactType> ARTIFACT_MAP = new HashMap<>();

    static {
        // ── WARRIOR ──────────────────────────────────────────────────────────

        // Swords
        ARTIFACT_MAP.put("Clarent Blade",   ArtifactType.RUNE);
        ARTIFACT_MAP.put("Carving Sword",   ArtifactType.SOUL);
        ARTIFACT_MAP.put("Galatine Pair",   ArtifactType.RELIC);
        ARTIFACT_MAP.put("Kingmaker",       ArtifactType.AVALON);
        ARTIFACT_MAP.put("Infinity Blade",  ArtifactType.CRYSTAL);

        // Axes
        ARTIFACT_MAP.put("Carrioncaller",   ArtifactType.RUNE);
        ARTIFACT_MAP.put("Infernal Scythe", ArtifactType.SOUL);
        ARTIFACT_MAP.put("Bear Paws",       ArtifactType.RELIC);
        ARTIFACT_MAP.put("Realmbreaker",    ArtifactType.AVALON);
        ARTIFACT_MAP.put("Crystal Reaper",  ArtifactType.CRYSTAL);

        // Maces
        ARTIFACT_MAP.put("Bedrock Mace",        ArtifactType.RUNE);
        ARTIFACT_MAP.put("Incubus Mace",         ArtifactType.SOUL);
        ARTIFACT_MAP.put("Camlann Mace",         ArtifactType.RELIC);
        ARTIFACT_MAP.put("Oathkeepers",          ArtifactType.AVALON);
        ARTIFACT_MAP.put("Dreadstorm Monarch",   ArtifactType.CRYSTAL);

        // Hammers
        ARTIFACT_MAP.put("Tombhammer",       ArtifactType.RUNE);
        ARTIFACT_MAP.put("Forge Hammers",    ArtifactType.SOUL);
        ARTIFACT_MAP.put("Grovekeeper",      ArtifactType.RELIC);
        ARTIFACT_MAP.put("Hand of Justice",  ArtifactType.AVALON);
        ARTIFACT_MAP.put("Truebolt Hammer",  ArtifactType.CRYSTAL);

        // War Gloves
        ARTIFACT_MAP.put("Ursine Maulers",      ArtifactType.RUNE);
        ARTIFACT_MAP.put("Hellfire Hands",       ArtifactType.SOUL);
        ARTIFACT_MAP.put("Ravenstrike Cestus",   ArtifactType.RELIC);
        ARTIFACT_MAP.put("Fists of Avalon",      ArtifactType.AVALON);
        ARTIFACT_MAP.put("Forcepulse Bracers",   ArtifactType.CRYSTAL);

        // Crossbows
        ARTIFACT_MAP.put("Weeping Repeater",  ArtifactType.RUNE);
        ARTIFACT_MAP.put("Boltcasters",        ArtifactType.SOUL);
        ARTIFACT_MAP.put("Siegebow",           ArtifactType.RELIC);
        ARTIFACT_MAP.put("Energy Shaper",      ArtifactType.AVALON);
        ARTIFACT_MAP.put("Arclight Blasters",  ArtifactType.CRYSTAL);

        // Shields
        ARTIFACT_MAP.put("Sarcophagus",       ArtifactType.RUNE);
        ARTIFACT_MAP.put("Caitiff Shield",     ArtifactType.SOUL);
        ARTIFACT_MAP.put("Facebreaker",        ArtifactType.RELIC);
        ARTIFACT_MAP.put("Astral Aegis",       ArtifactType.AVALON);
        ARTIFACT_MAP.put("Unbreakable Ward",   ArtifactType.CRYSTAL);

        // Plate Helmets
        ARTIFACT_MAP.put("Helmet of Valor",    ArtifactType.RUNE);
        ARTIFACT_MAP.put("Judicator Helmet",   ArtifactType.SOUL);
        ARTIFACT_MAP.put("Knight Helmet",      ArtifactType.RELIC);
        ARTIFACT_MAP.put("Royal Helmet",       ArtifactType.AVALON);

        // Plate Armor
        ARTIFACT_MAP.put("Armor of Valor",     ArtifactType.RUNE);
        ARTIFACT_MAP.put("Judicator Armor",    ArtifactType.SOUL);
        ARTIFACT_MAP.put("Knight Armor",       ArtifactType.RELIC);
        ARTIFACT_MAP.put("Royal Armor",        ArtifactType.AVALON);

        // Plate Boots
        ARTIFACT_MAP.put("Boots of Valor",     ArtifactType.RUNE);
        ARTIFACT_MAP.put("Judicator Boots",    ArtifactType.SOUL);
        ARTIFACT_MAP.put("Knight Boots",       ArtifactType.RELIC);
        ARTIFACT_MAP.put("Royal Boots",        ArtifactType.AVALON);

        // ── HUNTER ───────────────────────────────────────────────────────────

        // Bows
        ARTIFACT_MAP.put("Whispering Bow",  ArtifactType.RUNE);
        ARTIFACT_MAP.put("Wailing Bow",     ArtifactType.SOUL);
        ARTIFACT_MAP.put("Bow of Badon",    ArtifactType.RELIC);
        ARTIFACT_MAP.put("Mistpiercer",     ArtifactType.AVALON);
        ARTIFACT_MAP.put("Skystrider Bow",  ArtifactType.CRYSTAL);

        // Daggers
        ARTIFACT_MAP.put("Bloodletter",    ArtifactType.RUNE);
        ARTIFACT_MAP.put("Demonfang",      ArtifactType.SOUL);
        ARTIFACT_MAP.put("Deathgivers",    ArtifactType.RELIC);
        ARTIFACT_MAP.put("Bridled Fury",   ArtifactType.AVALON);
        ARTIFACT_MAP.put("Twin Slayers",   ArtifactType.CRYSTAL);

        // Spears
        ARTIFACT_MAP.put("Heron Spear",    ArtifactType.RUNE);
        ARTIFACT_MAP.put("Spirithunter",   ArtifactType.SOUL);
        ARTIFACT_MAP.put("Trinity Spear",  ArtifactType.RELIC);
        ARTIFACT_MAP.put("Daybreaker",     ArtifactType.AVALON);
        ARTIFACT_MAP.put("Rift Glaive",    ArtifactType.CRYSTAL);

        // Quarterstaves
        ARTIFACT_MAP.put("Black Monk Stave",     ArtifactType.RUNE);
        ARTIFACT_MAP.put("Soulscythe",            ArtifactType.SOUL);
        ARTIFACT_MAP.put("Staff of Balance",      ArtifactType.RELIC);
        ARTIFACT_MAP.put("Grailseeker",           ArtifactType.AVALON);
        ARTIFACT_MAP.put("Phantom Twinblade",     ArtifactType.CRYSTAL);

        // Shapeshifter Staves
        ARTIFACT_MAP.put("Bloodmoon Staff",   ArtifactType.RUNE);
        ARTIFACT_MAP.put("Hellspawn Staff",   ArtifactType.SOUL);
        ARTIFACT_MAP.put("Earthrune Staff",   ArtifactType.RELIC);
        ARTIFACT_MAP.put("Lightcaller",       ArtifactType.AVALON);
        ARTIFACT_MAP.put("Stillgaze Staff",   ArtifactType.CRYSTAL);

        // Nature Staves
        ARTIFACT_MAP.put("Druidic Staff",    ArtifactType.RUNE);
        ARTIFACT_MAP.put("Blight Staff",     ArtifactType.SOUL);
        ARTIFACT_MAP.put("Rampant Staff",    ArtifactType.RELIC);
        ARTIFACT_MAP.put("Ironroot Staff",   ArtifactType.AVALON);
        ARTIFACT_MAP.put("Forgebark Staff",  ArtifactType.CRYSTAL);

        // Torches
        ARTIFACT_MAP.put("Mistcaller",      ArtifactType.RUNE);
        ARTIFACT_MAP.put("Leering Cane",    ArtifactType.SOUL);
        ARTIFACT_MAP.put("Cryptcandle",     ArtifactType.RELIC);
        ARTIFACT_MAP.put("Sacred Scepter",  ArtifactType.AVALON);
        ARTIFACT_MAP.put("Blueflame Torch", ArtifactType.CRYSTAL);

        // Leather Hoods
        ARTIFACT_MAP.put("Hood of Tenacity",  ArtifactType.RUNE);
        ARTIFACT_MAP.put("Mercenary Hood",    ArtifactType.SOUL);
        ARTIFACT_MAP.put("Specter Hood",      ArtifactType.RELIC);
        ARTIFACT_MAP.put("Royal Hood",        ArtifactType.AVALON);

        // Leather Jackets
        ARTIFACT_MAP.put("Jacket of Tenacity",  ArtifactType.RUNE);
        ARTIFACT_MAP.put("Mercenary Jacket",    ArtifactType.SOUL);
        ARTIFACT_MAP.put("Specter Jacket",      ArtifactType.RELIC);
        ARTIFACT_MAP.put("Royal Jacket",        ArtifactType.AVALON);

        // Leather Shoes
        ARTIFACT_MAP.put("Shoes of Tenacity",  ArtifactType.RUNE);
        ARTIFACT_MAP.put("Mercenary Shoes",    ArtifactType.SOUL);
        ARTIFACT_MAP.put("Specter Shoes",      ArtifactType.RELIC);
        ARTIFACT_MAP.put("Royal Shoes",        ArtifactType.AVALON);

        // ── MAGE ─────────────────────────────────────────────────────────────

        // Fire Staves
        ARTIFACT_MAP.put("Wildfire Staff",     ArtifactType.RUNE);
        ARTIFACT_MAP.put("Brimstone Staff",    ArtifactType.SOUL);
        ARTIFACT_MAP.put("Blazing Staff",      ArtifactType.RELIC);
        ARTIFACT_MAP.put("Dawnsong",           ArtifactType.AVALON);
        ARTIFACT_MAP.put("Flamewalker Staff",  ArtifactType.CRYSTAL);

        // Holy Staves
        ARTIFACT_MAP.put("Lifetouch Staff",    ArtifactType.RUNE);
        ARTIFACT_MAP.put("Fallen Staff",       ArtifactType.SOUL);
        ARTIFACT_MAP.put("Redemption Staff",   ArtifactType.RELIC);
        ARTIFACT_MAP.put("Hallowfall",         ArtifactType.AVALON);
        ARTIFACT_MAP.put("Exalted Staff",      ArtifactType.CRYSTAL);

        // Arcane Staves
        ARTIFACT_MAP.put("Witchwork Staff",    ArtifactType.RUNE);
        ARTIFACT_MAP.put("Occult Staff",       ArtifactType.SOUL);
        ARTIFACT_MAP.put("Malevolent Locus",   ArtifactType.RELIC);
        ARTIFACT_MAP.put("Evensong",           ArtifactType.AVALON);
        ARTIFACT_MAP.put("Astral Staff",       ArtifactType.CRYSTAL);

        // Frost Staves
        ARTIFACT_MAP.put("Hoarfrost Staff",     ArtifactType.RUNE);
        ARTIFACT_MAP.put("Icicle Staff",        ArtifactType.SOUL);
        ARTIFACT_MAP.put("Permafrost Prism",    ArtifactType.RELIC);
        ARTIFACT_MAP.put("Chillhowl",           ArtifactType.AVALON);
        ARTIFACT_MAP.put("Arctic Staff",        ArtifactType.CRYSTAL);

        // Cursed Staves
        ARTIFACT_MAP.put("Lifecurse Staff",   ArtifactType.RUNE);
        ARTIFACT_MAP.put("Cursed Skull",      ArtifactType.SOUL);
        ARTIFACT_MAP.put("Damnation Staff",   ArtifactType.RELIC);
        ARTIFACT_MAP.put("Shadowcaller",      ArtifactType.AVALON);
        ARTIFACT_MAP.put("Rotcaller Staff",   ArtifactType.CRYSTAL);

        // Tomes
        ARTIFACT_MAP.put("Eye of Secrets",       ArtifactType.RUNE);
        ARTIFACT_MAP.put("Muisak",               ArtifactType.SOUL);
        ARTIFACT_MAP.put("Taproot",              ArtifactType.RELIC);
        ARTIFACT_MAP.put("Celestial Censer",     ArtifactType.AVALON);
        ARTIFACT_MAP.put("Timelocked Grimoire",  ArtifactType.CRYSTAL);

        // Cloth Cowls
        ARTIFACT_MAP.put("Cowl of Purity",  ArtifactType.RUNE);
        ARTIFACT_MAP.put("Cultist Cowl",    ArtifactType.SOUL);
        ARTIFACT_MAP.put("Fiend Cowl",      ArtifactType.RELIC);
        ARTIFACT_MAP.put("Royal Cowl",      ArtifactType.AVALON);

        // Cloth Robes
        ARTIFACT_MAP.put("Robe of Purity",  ArtifactType.RUNE);
        ARTIFACT_MAP.put("Cultist Robe",    ArtifactType.SOUL);
        ARTIFACT_MAP.put("Fiend Robe",      ArtifactType.RELIC);
        ARTIFACT_MAP.put("Royal Robe",      ArtifactType.AVALON);

        // Cloth Sandals
        ARTIFACT_MAP.put("Sandals of Purity",  ArtifactType.RUNE);
        ARTIFACT_MAP.put("Cultist Sandals",    ArtifactType.SOUL);
        ARTIFACT_MAP.put("Fiend Sandals",      ArtifactType.RELIC);
        ARTIFACT_MAP.put("Royal Sandals",      ArtifactType.AVALON);
    }

    /** Returns the artifact type for the given item, or null if it's a standard item. */
    public static ArtifactType getArtifactType(String itemName) {
        return ARTIFACT_MAP.get(itemName);
    }

    /** Returns true if the item requires an artifact material. */
    public static boolean isArtifact(String itemName) {
        return ARTIFACT_MAP.containsKey(itemName);
    }
}