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

    // Optional: nice toString for debugging
    @Override
    public String toString() {
        return name + " (T" + tier + ") - Profit: " + profit + " silver";
    }
}