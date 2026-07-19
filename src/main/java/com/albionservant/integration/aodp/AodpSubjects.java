package com.albionservant.integration.aodp;

import java.util.List;

public final class AodpSubjects {

    public static final String MARKET_ORDERS = "marketorders.ingest";
    public static final String MARKET_HISTORIES = "markethistories.ingest";
    public static final String GOLD_PRICES = "goldprices.ingest";
    public static final String MAP_DATA = "mapdata.ingest";
    public static final String BANDIT_EVENT = "banditevent.ingest";

    public static final List<String> STREAM_SUBJECTS = List.of(
            MARKET_ORDERS,
            MARKET_HISTORIES,
            GOLD_PRICES,
            MAP_DATA,
            BANDIT_EVENT
    );

    public static final List<String> CONSUMED_SUBJECTS = List.of(
            MARKET_ORDERS,
            MARKET_HISTORIES,
            GOLD_PRICES
    );

    private AodpSubjects() {
    }
}
