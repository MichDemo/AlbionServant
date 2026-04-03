package com.albionservant.data;

public class InvestmentData {

    public final String name;
    public final int tier;
    public final String location;
    public final long price;
    public final int enchantment;
    public final int demand;
    public final long cost;
    public final long profit;
    public final double roi;

    public InvestmentData(String name, int tier, String location, long price,
                          int enchantment, int demand, long cost, long profit, double roi) {

        this.name = name;
        this.tier = tier;
        this.location = location;
        this.price = price;
        this.enchantment = enchantment;
        this.demand = demand;
        this.cost = cost;
        this.profit = profit;
        this.roi = roi;
    }

    public String getName() {
        return name;
    }

    public int getTier() {
        return tier;
    }

    public String getLocation() {
        return location;
    }

    public long getPrice() {
        return price;
    }
    public int getEnchantment() {
        return enchantment;
    }
    public int getDemand() {
        return demand;
    }
    public long getCost() {
        return cost;
    }
    public long getProfit() {
        return profit;
    }
    public double getRoi() {
        return roi;
    }

    @Override
    public String toString() {
        return name + " (T" + tier + ") - Profit: " + profit + " silver";
    }
}