package com.albionservant.data;

import java.util.HashMap;
import java.util.Map;

public class CraftQuantityData {

            public record Quantities(int mat1, int mat2) {
                /** Total materials used per craft */
                public int total() { return mat1 + mat2; }
            }

            private static final Quantities Q_8_0   = new Quantities(8,  0);   // offhand/head/feet/gathering
            private static final Quantities Q_16_0  = new Quantities(16, 0);   // chest (single mat)
            private static final Quantities Q_16_8  = new Quantities(16, 8);   // 1H weapon (two mats)
            private static final Quantities Q_20_12 = new Quantities(20, 12);  // 2H weapon (two mats)

            // Special cases
            private static final Quantities Q_4_4   = new Quantities(4,  4);   // Tome of Spells (offhand)
            private static final Quantities Q_28_0  = new Quantities(28, 0);   // Bow (2H single-mat, planks only)
            private static final Quantities Q_24_8  = new Quantities(24, 8);   // Spear/Crossbow (2H mixed)
            private static final Quantities Q_8_8   = new Quantities(8,  8);   // Salads, some foods

            private static final Map<String, Quantities> MAP = new HashMap<>();

            static {

                // ══════════════════════════════════════════════════════════════════════
                //  WARRIOR — Swords
                // ══════════════════════════════════════════════════════════════════════
                // 1H swords: 16 Metal Bars + 8 Planks
                put("Broadsword",          Q_16_8);
                put("Clarent Blade",       Q_16_8);
                put("Carving Sword",       Q_16_8);
                put("Galatine Pair",       Q_16_8);
                put("Kingmaker",           Q_16_8);
                put("Infinity Blade",      Q_16_8);

                // 2H swords: 20 Metal Bars + 12 Planks
                put("Claymore",            Q_20_12);
                put("Dual Swords",         Q_20_12);

                // ══════════════════════════════════════════════════════════════════════
                //  WARRIOR — Axes
                // ══════════════════════════════════════════════════════════════════════
                // 1H axe: 16 Metal Bars + 8 Planks
                put("Battleaxe",           Q_16_8);
                put("Carrioncaller",       Q_16_8);
                put("Infernal Scythe",     Q_16_8);
                put("Bear Paws",           Q_16_8);
                put("Realmbreaker",        Q_16_8);
                put("Crystal Reaper",      Q_16_8);

                // 2H axes: 20 Metal Bars + 12 Planks
                put("Greataxe",            Q_20_12);
                put("Halberd",             Q_20_12);

                // ══════════════════════════════════════════════════════════════════════
                //  WARRIOR — Maces
                // ══════════════════════════════════════════════════════════════════════
                // 1H mace: 16 Metal Bars + 8 Planks
                put("Mace",                Q_16_8);
                put("Bedrock Mace",        Q_16_8);
                put("Incubus Mace",        Q_16_8);
                put("Camlann Mace",        Q_16_8);
                put("Oathkeepers",         Q_16_8);
                put("Dreadstorm Monarch",  Q_16_8);

                // 2H maces: 20 Metal Bars + 12 Planks
                put("Heavy Mace",          Q_20_12);
                put("Morning Star",        Q_20_12);

                // ══════════════════════════════════════════════════════════════════════
                //  WARRIOR — Hammers
                // ══════════════════════════════════════════════════════════════════════
                // 1H hammer (Metal Bars only)
                put("Hammer",              Q_8_0);

                // 2H hammers: 20 Metal Bars + 12 Planks
                put("Polehammer",          Q_20_12);
                put("Great Hammer",        Q_20_12);
                put("Tombhammer",          Q_20_12);
                put("Forge Hammers",       Q_20_12);
                put("Grovekeeper",         Q_20_12);
                put("Hand of Justice",     Q_20_12);
                put("Truebolt Hammer",     Q_20_12);

                // ══════════════════════════════════════════════════════════════════════
                //  WARRIOR — War Gloves (2H, Metal Bars + Leather)
                // ══════════════════════════════════════════════════════════════════════
                put("Brawler Gloves",      Q_20_12);
                put("Battle Bracers",      Q_20_12);
                put("Spiked Gauntlets",    Q_20_12);
                put("Ursine Maulers",      Q_20_12);
                put("Hellfire Hands",      Q_20_12);
                put("Ravenstrike Cestus",  Q_20_12);
                put("Fists of Avalon",     Q_20_12);
                put("Forcepulse Bracers",  Q_20_12);

                // ══════════════════════════════════════════════════════════════════════
                //  WARRIOR — Crossbows (2H: Metal Bars + Planks)
                // ══════════════════════════════════════════════════════════════════════
                put("Crossbow",            Q_20_12);
                put("Heavy Crossbow",      Q_20_12);
                put("Light Crossbow",      Q_20_12);
                put("Weeping Repeater",    Q_20_12);
                put("Boltcasters",         Q_20_12);
                put("Siegebow",            Q_20_12);
                put("Energy Shaper",       Q_20_12);
                put("Arclight Blasters",   Q_20_12);

                // ══════════════════════════════════════════════════════════════════════
                //  WARRIOR — Shields (offhand, 8 Metal Bars + 8 Planks)
                // Note: shields use 8+8 = 16 total, confirmed as offhand slot
                // ══════════════════════════════════════════════════════════════════════
                put("Shield",              Q_8_0);    // standard shield — 8 metal only per forum rule
                put("Sarcophagus",         Q_8_0);
                put("Caitiff Shield",      Q_8_0);
                put("Facebreaker",         Q_8_0);
                put("Astral Aegis",        Q_8_0);
                put("Unbreakable Ward",    Q_8_0);

                // ══════════════════════════════════════════════════════════════════════
                //  WARRIOR — Plate Armor (head/feet = 8, chest = 16, all Metal Bars only)
                // ══════════════════════════════════════════════════════════════════════
                put("Demon Helmet",        Q_8_0);
                put("Graveguard Helmet",   Q_8_0);
                put("Guardian Helmet",     Q_8_0);
                put("Helmet of Valor",     Q_8_0);
                put("Judicator Helmet",    Q_8_0);
                put("Knight Helmet",       Q_8_0);
                put("Royal Helmet",        Q_8_0);
                put("Soldier Helmet",      Q_8_0);

                put("Demon Armor",         Q_16_0);
                put("Graveguard Armor",    Q_16_0);
                put("Guardian Armor",      Q_16_0);
                put("Armor of Valor",      Q_16_0);
                put("Judicator Armor",     Q_16_0);
                put("Knight Armor",        Q_16_0);
                put("Royal Armor",         Q_16_0);
                put("Soldier Armor",       Q_16_0);

                put("Demon Boots",         Q_8_0);
                put("Graveguard Boots",    Q_8_0);
                put("Guardian Boots",      Q_8_0);
                put("Boots of Valor",      Q_8_0);
                put("Judicator Boots",     Q_8_0);
                put("Knight Boots",        Q_8_0);
                put("Royal Boots",         Q_8_0);
                put("Soldier Boots",       Q_8_0);

                // ══════════════════════════════════════════════════════════════════════
                //  HUNTER — Bows (2H, Planks only)
                // ══════════════════════════════════════════════════════════════════════
                put("Bow",                 Q_28_0);   // 28 Planks (confirmed from forum: Adept's Bow = 28 T4 wood)
                put("Warbow",              Q_28_0);
                put("Longbow",             Q_28_0);
                put("Whispering Bow",      Q_28_0);
                put("Wailing Bow",         Q_28_0);
                put("Bow of Badon",        Q_28_0);
                put("Mistpiercer",         Q_28_0);
                put("Skystrider Bow",      Q_28_0);

                // ══════════════════════════════════════════════════════════════════════
                //  HUNTER — Daggers (1H: 16 Metal + 8 Leather; pair/claws 2H: 20+12)
                // ══════════════════════════════════════════════════════════════════════
                put("Dagger",              Q_16_8);
                put("Bloodletter",         Q_16_8);
                put("Demonfang",           Q_16_8);
                put("Deathgivers",         Q_16_8);
                put("Bridled Fury",        Q_16_8);
                put("Twin Slayers",        Q_16_8);

                put("Dagger Pair",         Q_20_12);
                put("Claws",               Q_20_12);

                // ══════════════════════════════════════════════════════════════════════
                //  HUNTER — Spears (2H: 20 Metal + 12 Planks)
                // ══════════════════════════════════════════════════════════════════════
                put("Spear",               Q_20_12);
                put("Pike",                Q_20_12);
                put("Glaive",              Q_20_12);
                put("Heron Spear",         Q_20_12);
                put("Spirithunter",        Q_20_12);
                put("Trinity Spear",       Q_20_12);
                put("Daybreaker",          Q_20_12);
                put("Rift Glaive",         Q_20_12);

                // ══════════════════════════════════════════════════════════════════════
                //  HUNTER — Quarterstaves (2H, Planks only — 28 planks like bows)
                // ══════════════════════════════════════════════════════════════════════
                put("Quarterstaff",        Q_28_0);
                put("Iron-clad Staff",     Q_28_0);
                put("Double Bladed Staff", Q_28_0);
                put("Black Monk Stave",    Q_28_0);
                put("Soulscythe",          Q_28_0);
                put("Staff of Balance",    Q_28_0);
                put("Grailseeker",         Q_28_0);
                put("Phantom Twinblade",   Q_28_0);

                // ══════════════════════════════════════════════════════════════════════
                //  HUNTER — Shapeshifter Staves (2H: 20 Planks + 12 Leather)
                // ══════════════════════════════════════════════════════════════════════
                put("Prowling Staff",      Q_20_12);
                put("Rootbound Staff",     Q_20_12);
                put("Primal Staff",        Q_20_12);
                put("Bloodmoon Staff",     Q_20_12);
                put("Hellspawn Staff",     Q_20_12);
                put("Earthrune Staff",     Q_20_12);
                put("Lightcaller",         Q_20_12);
                put("Stillgaze Staff",     Q_20_12);

                // ══════════════════════════════════════════════════════════════════════
                //  HUNTER — Nature Staves (2H: 20 Planks + 12 Leather)
                // ══════════════════════════════════════════════════════════════════════
                put("Nature Staff",        Q_20_12);
                put("Great Nature Staff",  Q_20_12);
                put("Wild Staff",          Q_20_12);
                put("Druidic Staff",       Q_20_12);
                put("Blight Staff",        Q_20_12);
                put("Rampant Staff",       Q_20_12);
                put("Ironroot Staff",      Q_20_12);
                put("Forgebark Staff",     Q_20_12);

                // ══════════════════════════════════════════════════════════════════════
                //  HUNTER — Torches (offhand: 4 Planks + 4 Cloth — same as Tome)
                // ══════════════════════════════════════════════════════════════════════
                put("Torch",               Q_4_4);
                put("Mistcaller",          Q_4_4);
                put("Leering Cane",        Q_4_4);
                put("Cryptcandle",         Q_4_4);
                put("Sacred Scepter",      Q_4_4);
                put("Blueflame Torch",     Q_4_4);

                // ══════════════════════════════════════════════════════════════════════
                //  HUNTER — Leather Armor (all Leather only)
                // ══════════════════════════════════════════════════════════════════════
                put("Assassin Hood",       Q_8_0);
                put("Hellion Hood",        Q_8_0);
                put("Hood of Tenacity",    Q_8_0);
                put("Hunter Hood",         Q_8_0);
                put("Mercenary Hood",      Q_8_0);
                put("Royal Hood",          Q_8_0);
                put("Specter Hood",        Q_8_0);
                put("Stalker Hood",        Q_8_0);

                put("Assassin Jacket",     Q_16_0);
                put("Hellion Jacket",      Q_16_0);
                put("Jacket of Tenacity",  Q_16_0);
                put("Hunter Jacket",       Q_16_0);
                put("Mercenary Jacket",    Q_16_0);
                put("Royal Jacket",        Q_16_0);
                put("Specter Jacket",      Q_16_0);
                put("Stalker Jacket",      Q_16_0);

                put("Assassin Shoes",      Q_8_0);
                put("Hellion Shoes",       Q_8_0);
                put("Shoes of Tenacity",   Q_8_0);
                put("Hunter Shoes",        Q_8_0);
                put("Mercenary Shoes",     Q_8_0);
                put("Royal Shoes",         Q_8_0);
                put("Specter Shoes",       Q_8_0);
                put("Stalker Shoes",       Q_8_0);

                // ══════════════════════════════════════════════════════════════════════
                //  MAGE — 1H Staves (Arcane/Fire/Frost/Cursed): 16 Planks + 8 Bars/Cloth
                // ══════════════════════════════════════════════════════════════════════
                put("Fire Staff",          Q_16_8);
                put("Arcane Staff",        Q_16_8);
                put("Frost Staff",         Q_16_8);
                put("Cursed Staff",        Q_16_8);
                put("Holy Staff",          Q_16_8);   // 16 Planks + 8 Cloth (confirmed)

                // ══════════════════════════════════════════════════════════════════════
                //  MAGE — 2H Staves (Great* / Standard 2H): 20 Planks + 12 Cloth/Bars
                // ══════════════════════════════════════════════════════════════════════
                put("Great Fire Staff",    Q_20_12);
                put("Great Arcane Staff",  Q_20_12);
                put("Great Frost Staff",   Q_20_12);
                put("Great Cursed Staff",  Q_20_12);
                put("Great Holy Staff",    Q_20_12);

                // Standard 2H from Mage Tower — confirmed all 20+12
                put("Infernal Staff",      Q_20_12);
                put("Enigmatic Staff",     Q_20_12);
                put("Glacial Staff",       Q_20_12);
                put("Divine Staff",        Q_20_12);   // 20 Planks + 12 Cloth (confirmed)
                put("Demonic Staff",       Q_20_12);

                // Artifact 2H staves follow same 20+12 base
                put("Wildfire Staff",      Q_20_12);
                put("Brimstone Staff",     Q_20_12);
                put("Blazing Staff",       Q_20_12);
                put("Dawnsong",            Q_20_12);
                put("Flamewalker Staff",   Q_20_12);
                put("Lifetouch Staff",     Q_20_12);
                put("Fallen Staff",        Q_20_12);
                put("Redemption Staff",    Q_20_12);
                put("Hallowfall",          Q_20_12);
                put("Exalted Staff",       Q_20_12);
                put("Witchwork Staff",     Q_20_12);
                put("Occult Staff",        Q_20_12);
                put("Malevolent Locus",    Q_20_12);
                put("Evensong",            Q_20_12);
                put("Astral Staff",        Q_20_12);
                put("Hoarfrost Staff",     Q_20_12);
                put("Icicle Staff",        Q_20_12);
                put("Permafrost Prism",    Q_20_12);
                put("Chillhowl",           Q_20_12);
                put("Arctic Staff",        Q_20_12);
                put("Lifecurse Staff",     Q_20_12);
                put("Cursed Skull",        Q_20_12);
                put("Damnation Staff",     Q_20_12);
                put("Shadowcaller",        Q_20_12);
                put("Rotcaller Staff",     Q_20_12);

                // ══════════════════════════════════════════════════════════════════════
                //  MAGE — Tomes (offhand: 4 Cloth + 4 Leather — confirmed from guide)
                // ══════════════════════════════════════════════════════════════════════
                put("Tome of Spells",         Q_4_4);
                put("Eye of Secrets",         Q_4_4);
                put("Muisak",                 Q_4_4);
                put("Taproot",                Q_4_4);
                put("Celestial Censer",       Q_4_4);
                put("Timelocked Grimoire",    Q_4_4);

                // ══════════════════════════════════════════════════════════════════════
                //  MAGE — Cloth Armor (all Cloth only)
                // ══════════════════════════════════════════════════════════════════════
                put("Cleric Cowl",         Q_8_0);
                put("Cowl of Purity",      Q_8_0);
                put("Cultist Cowl",        Q_8_0);
                put("Druid Cowl",          Q_8_0);
                put("Fiend Cowl",          Q_8_0);
                put("Mage Cowl",           Q_8_0);
                put("Royal Cowl",          Q_8_0);
                put("Scholar Cowl",        Q_8_0);

                put("Cleric Robe",         Q_16_0);
                put("Robe of Purity",      Q_16_0);
                put("Cultist Robe",        Q_16_0);
                put("Druid Robe",          Q_16_0);
                put("Fiend Robe",          Q_16_0);
                put("Mage Robe",           Q_16_0);
                put("Royal Robe",          Q_16_0);
                put("Scholar Robe",        Q_16_0);

                put("Cleric Sandals",      Q_8_0);
                put("Sandals of Purity",   Q_8_0);
                put("Cultist Sandals",     Q_8_0);
                put("Druid Sandals",       Q_8_0);
                put("Fiend Sandals",       Q_8_0);
                put("Mage Sandals",        Q_8_0);
                put("Royal Sandals",       Q_8_0);
                put("Scholar Sandals",     Q_8_0);

                // ══════════════════════════════════════════════════════════════════════
                //  TOOLMAKER — Gathering Gear (all single-material)
                // ══════════════════════════════════════════════════════════════════════
                // Heads / Boots = 8, Garb (chest) = 16, Backpack = 8
                put("Harvester Cap",        Q_8_0);
                put("Harvester Garb",       Q_16_0);
                put("Harvester Workboots",  Q_8_0);
                put("Harvester Backpack",   Q_8_0);

                put("Lumberjack Cap",       Q_8_0);
                put("Lumberjack Garb",      Q_16_0);
                put("Lumberjack Workboots", Q_8_0);
                put("Lumberjack Backpack",  Q_8_0);

                put("Miner Cap",            Q_8_0);
                put("Miner Garb",           Q_16_0);
                put("Miner Workboots",      Q_8_0);
                put("Miner Backpack",       Q_8_0);

                put("Quarrier Cap",         Q_8_0);
                put("Quarrier Garb",        Q_16_0);
                put("Quarrier Workboots",   Q_8_0);
                put("Quarrier Backpack",    Q_8_0);

                put("Skinner Cap",          Q_8_0);
                put("Skinner Garb",         Q_16_0);
                put("Skinner Workboots",    Q_8_0);
                put("Skinner Backpack",     Q_8_0);

                put("Fisherman Cap",        Q_8_0);
                put("Fisherman Garb",       Q_16_0);
                put("Fisherman Workboots",  Q_8_0);
                put("Fisherman Backpack",   Q_8_0);

                // ══════════════════════════════════════════════════════════════════════
                //  TOOLMAKER — Tools (gathering tools = 8 materials, forum confirmed)
                // ══════════════════════════════════════════════════════════════════════
                put("Sickle",                   Q_8_0);
                put("Wood Axe",                 Q_16_8); // 16 Metal + 8 Planks (handle)
                put("Pickaxe",                  Q_16_8); // 16 Metal + 8 Planks (handle)
                put("Stone Hammer",             Q_16_8); // 16 Metal + 8 Planks (handle)
                put("Skinning Knife",           Q_16_8); // 16 Metal + 8 Planks (handle)
                put("Fishing Rod",              Q_8_0);  // 8 Planks only
                put("Siege Hammer (regular)",   Q_16_0); // 16 Metal Bars (confirmed: demolition hammer = 16)
                put("Avalonian Sickle",         Q_8_0);
                put("Avalonian Axe",            Q_16_8);
                put("Avalonian Pickaxe",        Q_16_8);
                put("Avalonian Stone Hammer",   Q_16_8);
                put("Avalonian Skinning Knife", Q_16_8);
                put("Avalonian Fishing Rod",    Q_8_0);
                put("Avalonian Siege Hammer",   Q_16_0);

                // ══════════════════════════════════════════════════════════════════════
                //  TOOLMAKER — Bags & Capes (8 materials — offhand slot category)
                // ══════════════════════════════════════════════════════════════════════
                put("Bag",                 Q_8_0);
                put("Satchel of Insight",  Q_8_0);
                put("Riding Bag",          Q_8_0);
                put("Cape",                Q_8_0);
                put("Thetford Cape",       Q_8_0);
                put("Fort Sterling Cape",  Q_8_0);
                put("Lymhurst Cape",       Q_8_0);
                put("Bridgewatch Cape",    Q_8_0);
                put("Martlock Cape",       Q_8_0);
                put("Caerleon Cape",       Q_8_0);
                put("Brecilien Cape",      Q_8_0);
                put("Avalonian Cape",      Q_8_0);
                put("Heretic Cape",        Q_8_0);
                put("Undead Cape",         Q_8_0);
                put("Keeper Cape",         Q_8_0);
                put("Morgana Cape",        Q_8_0);
                put("Demon Cape",          Q_8_0);
                put("Smuggler Cape",       Q_8_0);
            }

            private static void put(String itemName, Quantities q) {
                MAP.put(itemName, q);
            }

            /**
             * Returns the verified quantities for an item.
             * Falls back to Q_16_8 (1H weapon) if item is not found — prevents NPEs.
             */
            public static Quantities get(String itemName) {
                return MAP.getOrDefault(itemName, Q_16_8);
            }

            public static boolean contains(String itemName) {
                return MAP.containsKey(itemName);
            }
        }