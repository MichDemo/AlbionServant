package com.albionservant.data;

import java.util.ArrayList;
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
        // Top level
        categoryChildren.put("ROOT", List.of("Gear", "Food", "Potion"));

        // Gear main trees
        categoryChildren.put("Gear", List.of("Warrior", "Hunter", "Mage", "Toolmaker"));

        // Food / Potion (placeholder for now)
        categoryChildren.put("Food", Collections.emptyList());
        categoryChildren.put("Potion", Collections.emptyList());

        // Build sub-trees for Gear
        addSubTree("Warrior", 10);
        addSubTree("Hunter", 10);
        addSubTree("Mage", 9);
        addSubTree("Toolmaker", 10);
    }

    private static void addSubTree(String treeName, int subCount) {
        List<String> subs = new ArrayList<>();
        for (int i = 1; i <= subCount; i++) {
            subs.add(treeName + "_Sub" + i);
        }
        categoryChildren.put(treeName, subs);

        // 2 sub-sub categories for every sub-category (proof of concept)
        for (String sub : subs) {
            List<String> subSubs = List.of(sub + "_SubSub1", sub + "_SubSub2");
            categoryChildren.put(sub, subSubs);
        }
    }

    /**
     * Returns the direct children of any category (or empty list if none).
     * Easy to extend – just add more put() calls or new addSubTree() lines.
     */
    public static List<String> getChildren(String parentKey) {
        return categoryChildren.getOrDefault(parentKey, Collections.emptyList());
    }
}