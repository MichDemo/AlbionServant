package com.albionservant.elements;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** A market-investment row styled exclusively through JavaFX CSS classes. */
public class InvestmentItem extends HBox {

    public InvestmentItem(String itemName, int tier, String location, long price,
                          int enchantment, int demand, long cost, long profit, double roi) {
        getStyleClass().add("investment-item");
        setAlignment(Pos.CENTER_LEFT);

        ImageView icon = new ImageView();
        icon.setFitWidth(64);
        icon.setFitHeight(64);
        icon.getStyleClass().add("investment-icon");

        VBox details = new VBox();
        details.getStyleClass().add("investment-details");
        details.setAlignment(Pos.CENTER_LEFT);

        Label name = styledLabel(itemName + " (T" + tier + ")", "investment-name");
        Label loc = styledLabel("Location: " + location, "text-secondary");
        Label priceLabel = styledLabel(
                "Price: " + String.format("%,d", price) + " silver",
                "text-secondary"
        );
        details.getChildren().addAll(name, loc, priceLabel);

        VBox stats = new VBox();
        stats.getStyleClass().add("investment-stats");
        stats.setAlignment(Pos.CENTER_RIGHT);

        Label enchantmentLabel = styledLabel(
                "Enchantment: " + enchantment,
                "text-enchantment"
        );
        Label demandLabel = styledLabel("Demand (24h): " + demand, "text-secondary");
        Label costLabel = styledLabel(
                "Cost: " + String.format("%,d", cost) + " silver",
                "text-secondary"
        );
        Label profitLabel = styledLabel(
                "PROFIT: " + String.format("%,d", profit) + " silver",
                "investment-profit",
                profit > 0 ? "metric-positive" : "metric-negative"
        );
        Label roiLabel = styledLabel(
                "ROI: " + String.format("%.0f", roi) + "%",
                "investment-roi",
                roi > 0 ? "metric-positive" : "metric-negative"
        );
        stats.getChildren().addAll(
                enchantmentLabel,
                demandLabel,
                costLabel,
                profitLabel,
                roiLabel
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getChildren().addAll(icon, details, spacer, stats);
    }

    private static Label styledLabel(String text, String... styleClasses) {
        Label label = new Label(text);
        label.getStyleClass().addAll(styleClasses);
        return label;
    }
}
