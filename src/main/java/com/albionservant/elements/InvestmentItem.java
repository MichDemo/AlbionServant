package com.albionservant.elements;

import com.albionservant.AppConfig;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class InvestmentItem extends HBox {

    public InvestmentItem(String itemName, int tier, String location, long price,
                          int enchantment, int demand, long cost, long profit, double roi) {

        setPadding(new Insets(12, 16, 12, 16));
        setSpacing(16);
        setAlignment(Pos.CENTER_LEFT);

        // Icon
        ImageView icon = new ImageView();
        icon.setFitWidth(64);
        icon.setFitHeight(64);
        icon.setStyle("-fx-background-color: #333333; -fx-border-color: #555555;");

        // Left details
        VBox details = new VBox(5);
        details.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label(itemName + " (T" + tier + ")");
        name.setFont(Font.font("System", FontWeight.BOLD, 17));
        name.setTextFill(AppConfig.TEXT_PRIMARY);

        Label loc = new Label("Location: " + location);
        loc.setTextFill(AppConfig.TEXT_SECONDARY);

        Label priceLabel = new Label("Price: " + String.format("%,d", price) + " silver");
        priceLabel.setTextFill(AppConfig.TEXT_SECONDARY);

        details.getChildren().addAll(name, loc, priceLabel);

        // Right stats
        VBox stats = new VBox(6);
        stats.setAlignment(Pos.CENTER_RIGHT);

        Label ench = new Label("Enchantment: " + enchantment);
        ench.setTextFill(Color.rgb(80, 200, 255));

        Label demandLabel = new Label("Demand (24h): " + demand);
        demandLabel.setTextFill(AppConfig.TEXT_SECONDARY);

        Label costLabel = new Label("Cost: " + String.format("%,d", cost) + " silver");
        costLabel.setTextFill(AppConfig.TEXT_SECONDARY);

        Label profitLabel = new Label("PROFIT: " + String.format("%,d", profit) + " silver");
        profitLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        profitLabel.setTextFill(profit > 0 ? Color.rgb(0, 220, 100) : Color.RED);

        Label roiLabel = new Label("ROI: " + String.format("%.0f", roi) + "%");
        roiLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        roiLabel.setTextFill(roi > 0 ? Color.rgb(0, 220, 100) : Color.RED);

        stats.getChildren().addAll(ench, demandLabel, costLabel, profitLabel, roiLabel);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(icon, details, spacer, stats);
    }
}