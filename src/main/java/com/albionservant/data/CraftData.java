package com.albionservant.data;

import com.albionservant.elements.CraftCategory;

public class CraftData {

    public static CraftCategory getGearTree() {
        CraftCategory gear = new CraftCategory("Gear", "⚒️");

        // Level 2
        CraftCategory warrior = gear.addChild(new CraftCategory("Warrior", "🛡️"));
        CraftCategory hunter  = gear.addChild(new CraftCategory("Hunter", "🏹"));
        CraftCategory mage    = gear.addChild(new CraftCategory("Mage", "🔮"));
        CraftCategory toolmaker = gear.addChild(new CraftCategory("Toolmaker", "🔨"));

        // Level 3 - 10 subcategories for Warrior, Hunter, Toolmaker (9 for Mage)
        for (int i = 1; i <= 10; i++) {
            warrior.addChild(new CraftCategory("Warrior_Sub" + i, ""));
            hunter.addChild(new CraftCategory("Hunter_Sub" + i, ""));
            toolmaker.addChild(new CraftCategory("Toolmaker_Sub" + i, ""));
        }
        for (int i = 1; i <= 9; i++) {
            mage.addChild(new CraftCategory("Mage_Sub" + i, ""));
        }

        // Level 4 - proof of concept: 2 sub_sub for every sub-category
        for (CraftCategory sub : warrior.getChildren()) {
            sub.addChild(new CraftCategory(sub.getName() + "_A", ""));
            sub.addChild(new CraftCategory(sub.getName() + "_B", ""));
        }
        for (CraftCategory sub : hunter.getChildren()) {
            sub.addChild(new CraftCategory(sub.getName() + "_A", ""));
            sub.addChild(new CraftCategory(sub.getName() + "_B", ""));
        }
        for (CraftCategory sub : mage.getChildren()) {
            sub.addChild(new CraftCategory(sub.getName() + "_A", ""));
            sub.addChild(new CraftCategory(sub.getName() + "_B", ""));
        }
        for (CraftCategory sub : toolmaker.getChildren()) {
            sub.addChild(new CraftCategory(sub.getName() + "_A", ""));
            sub.addChild(new CraftCategory(sub.getName() + "_B", ""));
        }

        return gear;
    }

    // You can add FoodTree() and PotionTree() the same way later
}