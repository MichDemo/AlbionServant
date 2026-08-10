package com.albionservant.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Specs panel — Destiny Board crafting specialization tracker.
 *
 * Specialization affects FOCUS EFFICIENCY only.
 * Higher spec = lower focus cost per craft.
 *
 * Values are auto-saved via java.util.prefs.Preferences on every slider
 * change and restored automatically on next launch — no manual save needed.
 *
 * Confirmed structure from Albion Online wiki:
 *   - Tool crafting specialization relies on armor items per profession.
 *   - Siege Hammer and Siege Banner have separate spec nodes.
 *   - Bag / Satchel of Insight have separate spec nodes; Riding Bag does not exist.
 *   - Avalonian tools/backpacks have no separate spec nodes.
 *   - Refining specs start at T4 only (no T2/T3 nodes).
 */
public class SpecsPanel extends VBox {

    private static final Preferences PREFS = Preferences.userRoot()
            .node("com/albionservant/specs");

    private static final LinkedHashMap<String, LinkedHashMap<String, List<String>>> TREE
            = new LinkedHashMap<>();

    static {
        // ── WARRIOR ──────────────────────────────────────────────────────────
        LinkedHashMap<String, List<String>> warrior = new LinkedHashMap<>();
        warrior.put("Swords",      List.of("Broadsword","Claymore","Dual Swords","Clarent Blade","Carving Sword","Galatine Pair","Kingmaker","Infinity Blade"));
        warrior.put("Axes",        List.of("Battleaxe","Greataxe","Halberd","Carrioncaller","Infernal Scythe","Bear Paws","Realmbreaker","Crystal Reaper"));
        warrior.put("Maces",       List.of("Mace","Heavy Mace","Morning Star","Bedrock Mace","Incubus Mace","Camlann Mace","Oathkeepers","Dreadstorm Monarch"));
        warrior.put("Hammers",     List.of("Polehammer","Great Hammer","Tombhammer","Forge Hammers","Grovekeeper","Hand of Justice","Truebolt Hammer"));
        warrior.put("War Gloves",  List.of("Brawler Gloves","Battle Bracers","Spiked Gauntlets","Ursine Maulers","Hellfire Hands","Ravenstrike Cestus","Fists of Avalon","Forcepulse Bracers"));
        warrior.put("Crossbows",   List.of("Crossbow","Heavy Crossbow","Light Crossbow","Weeping Repeater","Boltcasters","Siegebow","Energy Shaper","Arclight Blasters"));
        warrior.put("Shields",     List.of("Shield","Sarcophagus","Caitiff Shield","Facebreaker","Astral Aegis","Unbreakable Ward"));
        warrior.put("Plate Helmet",List.of("Knight Helmet","Guardian Helmet","Soldier Helmet","Graveguard Helmet","Royal Helmet","Demon Helmet","Helmet of Valor","Judicator Helmet"));
        warrior.put("Plate Armor", List.of("Knight Armor","Guardian Armor","Soldier Armor","Graveguard Armor","Royal Armor","Demon Armor","Armor of Valor","Judicator Armor"));
        warrior.put("Plate Boots", List.of("Knight Boots","Guardian Boots","Soldier Boots","Graveguard Boots","Royal Boots","Demon Boots","Boots of Valor","Judicator Boots"));
        TREE.put("Warrior", warrior);

        // ── HUNTER ───────────────────────────────────────────────────────────
        LinkedHashMap<String, List<String>> hunter = new LinkedHashMap<>();
        hunter.put("Bows",           List.of("Bow","Warbow","Longbow","Whispering Bow","Wailing Bow","Bow of Badon","Mistpiercer","Skystrider Bow"));
        hunter.put("Daggers",        List.of("Dagger","Dagger Pair","Claws","Bloodletter","Demonfang","Deathgivers","Bridled Fury","Twin Slayers"));
        hunter.put("Spears",         List.of("Spear","Pike","Glaive","Heron Spear","Spirithunter","Trinity Spear","Daybreaker","Rift Glaive"));
        hunter.put("Quarterstaves",  List.of("Quarterstaff","Iron-clad Staff","Double Bladed Staff","Black Monk Stave","Soulscythe","Staff of Balance","Grailseeker","Phantom Twinblade"));
        hunter.put("Shapeshifter",   List.of("Prowling Staff","Rootbound Staff","Primal Staff","Bloodmoon Staff","Hellspawn Staff","Earthrune Staff","Lightcaller","Stillgaze Staff"));
        hunter.put("Nature Staves",  List.of("Nature Staff","Great Nature Staff","Wild Staff","Druidic Staff","Blight Staff","Rampant Staff","Ironroot Staff","Forgebark Staff"));
        hunter.put("Torches",        List.of("Torch","Mistcaller","Leering Cane","Cryptcandle","Sacred Scepter","Blueflame Torch"));
        hunter.put("Leather Hood",   List.of("Hunter Hood","Stalker Hood","Assassin Hood","Mercenary Hood","Royal Hood","Hellion Hood","Hood of Tenacity","Specter Hood"));
        hunter.put("Leather Jacket", List.of("Hunter Jacket","Stalker Jacket","Assassin Jacket","Mercenary Jacket","Royal Jacket","Hellion Jacket","Jacket of Tenacity","Specter Jacket"));
        hunter.put("Leather Shoes",  List.of("Hunter Shoes","Stalker Shoes","Assassin Shoes","Mercenary Shoes","Royal Shoes","Hellion Shoes","Shoes of Tenacity","Specter Shoes"));
        TREE.put("Hunter", hunter);

        // ── MAGE ─────────────────────────────────────────────────────────────
        LinkedHashMap<String, List<String>> mage = new LinkedHashMap<>();
        mage.put("Fire Staves",   List.of("Fire Staff","Great Fire Staff","Infernal Staff","Wildfire Staff","Brimstone Staff","Blazing Staff","Dawnsong","Flamewalker Staff"));
        mage.put("Arcane Staves", List.of("Arcane Staff","Great Arcane Staff","Enigmatic Staff","Witchwork Staff","Occult Staff","Malevolent Locus","Evensong","Astral Staff"));
        mage.put("Frost Staves",  List.of("Frost Staff","Great Frost Staff","Glacial Staff","Hoarfrost Staff","Icicle Staff","Permafrost Prism","Chillhowl","Arctic Staff"));
        mage.put("Holy Staves",   List.of("Holy Staff","Great Holy Staff","Divine Staff","Lifetouch Staff","Fallen Staff","Redemption Staff","Hallowfall","Exalted Staff"));
        mage.put("Cursed Staves", List.of("Cursed Staff","Great Cursed Staff","Demonic Staff","Lifecurse Staff","Cursed Skull","Damnation Staff","Shadowcaller","Rotcaller Staff"));
        mage.put("Tomes",         List.of("Tome of Spells","Eye of Secrets","Muisak","Taproot","Celestial Censer","Timelocked Grimoire"));
        mage.put("Cloth Cowl",    List.of("Mage Cowl","Scholar Cowl","Cleric Cowl","Druid Cowl","Royal Cowl","Fiend Cowl","Cowl of Purity","Cultist Cowl"));
        mage.put("Cloth Robe",    List.of("Mage Robe","Scholar Robe","Cleric Robe","Druid Robe","Royal Robe","Fiend Robe","Robe of Purity","Cultist Robe"));
        mage.put("Cloth Sandals", List.of("Mage Sandals","Scholar Sandals","Cleric Sandals","Druid Sandals","Royal Sandals","Fiend Sandals","Sandals of Purity","Cultist Sandals"));
        TREE.put("Mage", mage);

        // ── TOOLMAKER ────────────────────────────────────────────────────────
        // Confirmed: tool crafting specialization relies on the armor items per profession.
        // Avalonian tools/backpacks have no separate spec node.
        // Riding Bag does not exist.
        LinkedHashMap<String, List<String>> toolmaker = new LinkedHashMap<>();
        toolmaker.put("Harvester",  List.of("Harvester Cap","Harvester Garb","Harvester Workboots","Harvester Backpack"));
        toolmaker.put("Lumberjack", List.of("Lumberjack Cap","Lumberjack Garb","Lumberjack Workboots","Lumberjack Backpack"));
        toolmaker.put("Miner",      List.of("Miner Cap","Miner Garb","Miner Workboots","Miner Backpack"));
        toolmaker.put("Quarrier",   List.of("Quarrier Cap","Quarrier Garb","Quarrier Workboots","Quarrier Backpack"));
        toolmaker.put("Skinner",    List.of("Skinner Cap","Skinner Garb","Skinner Workboots","Skinner Backpack"));
        toolmaker.put("Fisherman",  List.of("Fisherman Cap","Fisherman Garb","Fisherman Workboots","Fisherman Backpack"));
        // Siege Equipment: Siege Hammer and Siege Banner each have their own separate spec node
        toolmaker.put("Siege Equipment", List.of("Siege Hammer (regular)","Avalonian Siege Hammer","Siege Banner"));
        // Bags and Capes: separate spec nodes confirmed from wiki
        toolmaker.put("Bags",  List.of("Bag","Satchel of Insight"));
        toolmaker.put("Capes", List.of(
                "Cape","Thetford Cape","Fort Sterling Cape","Lymhurst Cape",
                "Bridgewatch Cape","Martlock Cape","Caerleon Cape","Brecilien Cape",
                "Avalonian Cape","Heretic Cape","Undead Cape","Keeper Cape",
                "Morgana Cape","Demon Cape","Smuggler Cape"
        ));
        TREE.put("Toolmaker", toolmaker);

        // ── REFINING ─────────────────────────────────────────────────────────
        // No T2/T3 spec nodes exist -- refining specs start at T4 only.
        LinkedHashMap<String, List<String>> refining = new LinkedHashMap<>();
        refining.put("Metal Bars",   List.of("Worked Metal Bar","Polished Metal Bar","Hardened Metal Bar","Reinforced Metal Bar","Infused Metal Bar"));
        refining.put("Planks",       List.of("Bloodoak Plank","Ashenbark Plank","Whitewood Plank","Ghostroot Plank","Sunbright Plank"));
        refining.put("Leather",      List.of("Heavy Leather","Worked Leather","Cured Leather","Hardened Leather","Fortified Leather"));
        refining.put("Cloth",        List.of("Fine Cloth","Ornate Cloth","Spun Cloth","Latent Cloth","Occult Cloth"));
        refining.put("Stone Blocks", List.of("Travertine Block","Granite Block","Slate Block","Basalt Block","Marble Block"));
        TREE.put("Refining", refining);

        // ── COOKING ──────────────────────────────────────────────────────────
        LinkedHashMap<String, List<String>> cooking = new LinkedHashMap<>();
        cooking.put("Food",    List.of("Soups","Salads","Omelettes","Pies","Stews","Roasts","Sandwiches"));
        cooking.put("Potions", List.of(
                "Healing Potions","Energy Potions","Gigantify Potions","Resistance Potions",
                "Sticky Potions","Poison Potions","Invisibility Potion","Cleansing Potions",
                "Calming Potions","Acid Potions","Berserk Potions","Hellfire Potions",
                "Tornado in a Bottle","Gathering Potions"
        ));
        TREE.put("Cooking", cooking);
    }

    // ── State ─────────────────────────────────────────────────────────────────

    private final Map<String, Map<String, Map<String, Integer>>> specValues = new LinkedHashMap<>();
    private String selectedSection = "Warrior";
    private String selectedGroup   = "Swords";

    public SpecsPanel() {
        setStyle("-fx-background-color: #f0f1f3;");
        setFillWidth(true);
        VBox.setVgrow(this, Priority.ALWAYS);

        for (var sect : TREE.entrySet()) {
            Map<String, Map<String, Integer>> gMap = new LinkedHashMap<>();
            for (var grp : sect.getValue().entrySet()) {
                Map<String, Integer> iMap = new LinkedHashMap<>();
                for (String item : grp.getValue()) {
                    // Restore saved value, default 0
                    iMap.put(item, PREFS.getInt(prefKey(sect.getKey(), grp.getKey(), item), 0));
                }
                gMap.put(grp.getKey(), iMap);
            }
            specValues.put(sect.getKey(), gMap);
        }

        build();
    }

    // ── Persistence helpers ───────────────────────────────────────────────────

    private String prefKey(String section, String group, String item) {
        // Sanitise to a valid Preferences key (max 80 chars, no slashes)
        return (section + "." + group + "." + item)
                .replace("/", "_").replace(" ", "_");
    }

    private void saveSpec(String section, String group, String item, int value) {
        PREFS.putInt(prefKey(section, group, item), value);
    }

    private void build() {
        getChildren().clear();
        HBox layout = new HBox(0);
        layout.setFillHeight(true);
        VBox.setVgrow(layout, Priority.ALWAYS);
        layout.getChildren().addAll(buildLeftNav(), buildRightContent());
        getChildren().add(layout);
    }

    // ── Left navigation ───────────────────────────────────────────────────────

    private VBox buildLeftNav() {
        VBox nav = new VBox(0);
        nav.setStyle("-fx-background-color: #202328;");
        nav.setFillWidth(true);

        Label title = new Label("Destiny Board");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #e6e8eb;"
                + "-fx-padding: 18 16 14 16; -fx-background-color: #17191d;");
        title.setMaxWidth(Double.MAX_VALUE);
        nav.getChildren().add(title);

        for (String sectionName : TREE.keySet()) {
            Label sectionLbl = new Label(sectionName.toUpperCase());
            sectionLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;"
                    + "-fx-text-fill: #707782; -fx-padding: 12 16 4 16;");
            sectionLbl.setMaxWidth(Double.MAX_VALUE);
            nav.getChildren().add(sectionLbl);

            for (String groupName : TREE.get(sectionName).keySet()) {
                boolean isSelected = sectionName.equals(selectedSection)
                        && groupName.equals(selectedGroup);
                Button btn = new Button(groupName);
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setPrefHeight(36);
                applyNavStyle(btn, isSelected);
                btn.setOnAction(e -> {
                    selectedSection = sectionName;
                    selectedGroup   = groupName;
                    build();
                });
                nav.getChildren().add(btn);
            }
        }

        ScrollPane scroll = new ScrollPane(nav);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: #202328;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox wrapper = new VBox(scroll);
        wrapper.setFillWidth(true);
        VBox.setVgrow(wrapper, Priority.ALWAYS);
        wrapper.setStyle("-fx-background-color: #202328;");
        wrapper.setPrefWidth(220);
        return wrapper;
    }

    // ── Right content ─────────────────────────────────────────────────────────

    private VBox buildRightContent() {
        VBox right = new VBox(0);
        right.setFillWidth(true);
        VBox.setVgrow(right, Priority.ALWAYS);
        right.setStyle("-fx-background-color: #f7f8f9;");

        List<String> items = TREE.getOrDefault(selectedSection, new LinkedHashMap<>())
                .getOrDefault(selectedGroup, List.of());

        // Header
        HBox header = new HBox(10);
        header.setPadding(new Insets(20, 32, 16, 32));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #202328;");

        Label sectionLbl = new Label(selectedSection + "  \u25ba  ");
        sectionLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #707782;");
        Label groupLbl = new Label(selectedGroup);
        groupLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);

        Button resetBtn = new Button("Reset Group");
        resetBtn.getStyleClass().add("button-secondary");
        resetBtn.setOnAction(e -> resetGroup());
        header.getChildren().addAll(sectionLbl, groupLbl, hSpacer, resetBtn);

        ScrollPane itemScroll = new ScrollPane(buildItemGrid(items));
        itemScroll.setFitToWidth(true);
        itemScroll.setStyle("-fx-background-color: #f7f8f9;");
        VBox.setVgrow(itemScroll, Priority.ALWAYS);

        right.getChildren().addAll(header, itemScroll);
        return right;
    }

    // ── Item spec grid ────────────────────────────────────────────────────────

    private VBox buildItemGrid(List<String> items) {
        VBox grid = new VBox(8);
        grid.setPadding(new Insets(20, 32, 20, 32));
        grid.setFillWidth(true);

        // Column headers
        HBox headerRow = new HBox(0);
        headerRow.setPadding(new Insets(0, 0, 4, 0));
        Label itemHdr = styledLabel("Item",                   360, "#707782", true);
        Label specHdr = styledLabel("Spec Level  (0 - 100)", 200, "#707782", true);
        headerRow.getChildren().addAll(itemHdr, specHdr);
        grid.getChildren().add(headerRow);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #e6e8eb;");
        grid.getChildren().add(sep);

        Map<String, Integer> groupVals = specValues
                .getOrDefault(selectedSection, Map.of())
                .getOrDefault(selectedGroup, Map.of());

        for (int i = 0; i < items.size(); i++) {
            String item = items.get(i);
            int    spec = groupVals.getOrDefault(item, 0);
            String bg   = (i % 2 == 0) ? "#ffffff" : "#f7f8f9";

            HBox row = new HBox(0);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setMaxWidth(Double.MAX_VALUE);
            row.setStyle("-fx-background-color: " + bg + "; -fx-padding: 6 8;");

            Label nameLbl = styledLabel(item, 360, "#202328", false);

            Slider slider = new Slider(0, 100, spec);
            slider.setPrefWidth(160);
            slider.setSnapToTicks(false);

            Label specValLbl = new Label(String.valueOf(spec));
            specValLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: "
                    + specColour(spec) + "; -fx-min-width: 36; -fx-alignment: center;");

            HBox sliderBox = new HBox(8, slider, specValLbl);
            sliderBox.setAlignment(Pos.CENTER_LEFT);
            sliderBox.setPrefWidth(200);

            final String finalItem    = item;
            final String finalSection = selectedSection;
            final String finalGroup   = selectedGroup;
            slider.valueProperty().addListener((obs, ov, nv) -> {
                int v = (int) Math.round(nv.doubleValue());
                groupVals.put(finalItem, v);
                saveSpec(finalSection, finalGroup, finalItem, v);
                specValLbl.setText(String.valueOf(v));
                specValLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: "
                        + specColour(v) + "; -fx-min-width: 36; -fx-alignment: center;");
            });

            row.getChildren().addAll(nameLbl, sliderBox);
            grid.getChildren().add(row);
        }
        return grid;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void resetGroup() {
        specValues.getOrDefault(selectedSection, Map.of())
                .getOrDefault(selectedGroup, Map.of())
                .forEach((item, v) -> {
                    specValues.get(selectedSection).get(selectedGroup).put(item, 0);
                    saveSpec(selectedSection, selectedGroup, item, 0);
                });
        build();
    }

    private String specColour(int spec) {
        if (spec == 0)   return "#707782";
        if (spec <= 20)  return "#22c55e";
        if (spec <= 40)  return "#3b82f6";
        if (spec <= 60)  return "#a855f7";
        if (spec <= 80)  return "#f97316";
        if (spec < 100)  return "#50565f";
        return "#4b5058";
    }

    private void applyNavStyle(Button btn, boolean selected) {
        String bg    = selected ? "#3b82f6" : "transparent";
        String fg    = selected ? "#ffffff"  : "#cbd5e1";
        btn.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";"
                + "-fx-font-size: 12px; -fx-alignment: center-left; -fx-padding: 6 16;"
                + "-fx-background-radius: 0; -fx-border-width: 0;");
    }

    private Label styledLabel(String text, double prefWidth, String colour, boolean bold) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 13px; -fx-text-fill: " + colour + ";"
                + (bold ? " -fx-font-weight: bold;" : ""));
        l.setPrefWidth(prefWidth);
        return l;
    }

    /** Public API — returns the player's current spec level for any item. */
    public int getSpecLevel(String section, String group, String item) {
        return specValues.getOrDefault(section, Map.of())
                .getOrDefault(group, Map.of())
                .getOrDefault(item, 0);
    }
}