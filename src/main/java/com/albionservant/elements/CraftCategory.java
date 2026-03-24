package com.albionservant.elements;

import java.util.ArrayList;
import java.util.List;

public class CraftCategory {
    private final String name;
    private final String icon;               // emoji for now, can be Image later
    private final List<CraftCategory> children;

    public CraftCategory(String name, String icon) {
        this.name = name;
        this.icon = icon;
        this.children = new ArrayList<>();
    }

    public CraftCategory addChild(CraftCategory child) {
        children.add(child);
        return this;
    }

    public String getName() { return name; }
    public String getIcon() { return icon; }
    public List<CraftCategory> getChildren() { return children; }
}