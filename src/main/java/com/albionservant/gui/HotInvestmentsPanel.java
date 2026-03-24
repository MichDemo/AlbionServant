package com.albionservant.gui;

import com.albionservant.data.InvestmentData;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

public class HotInvestmentsPanel extends VBox {

    private final VBox itemsContainer = new VBox(18);
    private final HBox paginationBox = new HBox(15);
    private int currentPage = 0;
    private List<InvestmentData> allInvestments;

    public HotInvestmentsPanel() {
        // Red card
        setBackground(new Background(new BackgroundFill(Color.rgb(224, 78, 78), new CornerRadii(12), null)));
        setPadding(new Insets(25, 30, 25, 30));
        setSpacing(20);
        setMaxWidth(1180);
        setMinHeight(580);
        setPrefHeight(520);
        setAlignment(Pos.TOP_CENTER);

        buildHeader();
        buildItemsContainer();
        buildPaginationControls();

        getChildren().addAll(itemsContainer, paginationBox);
    }

    private void buildHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Hot investments! Craft them now!");
        title.setFont(Font.font("System", FontWeight.BOLD, 23));
        title.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button adjustBtn = new Button("ADJUST");
        adjustBtn.setStyle("-fx-background-color: rgba(0,0,0,0.35); " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 10 30; " +
                "-fx-background-radius: 8;");

        header.getChildren().addAll(title, spacer, adjustBtn);
        getChildren().add(header);
    }

    private void buildItemsContainer() {
        itemsContainer.setMinHeight(360);
        itemsContainer.setPrefHeight(360);
    }

    private void buildPaginationControls() {
        Button prevBtn = new Button("← Previous");
        Button nextBtn = new Button("Next →");

        prevBtn.setStyle("-fx-background-color: rgba(0,0,0,0.3); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        nextBtn.setStyle("-fx-background-color: rgba(0,0,0,0.3); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");

        prevBtn.setOnAction(e -> previousPage());
        nextBtn.setOnAction(e -> nextPage());

        paginationBox.setAlignment(Pos.CENTER);
        paginationBox.setPadding(new Insets(60, 0, 0, 0));
        paginationBox.getChildren().addAll(prevBtn, nextBtn);
        paginationBox.setVisible(false);   // will show only when needed
    }

    public void setInvestments(List<InvestmentData> investments) {
        this.allInvestments = investments;
        currentPage = 0;
        updateDisplay();
    }

    private void updateDisplay() {
        itemsContainer.getChildren().clear();

        int start = currentPage * 3;
        int end = Math.min(start + 3, allInvestments.size());

        for (int i = start; i < end; i++) {
            HBox row = createInvestmentRow(allInvestments.get(i));
            itemsContainer.getChildren().add(row);
        }

        // Keep height stable
        while (itemsContainer.getChildren().size() < 3) {
            Region placeholder = new Region();
            placeholder.setMinHeight(92);
            itemsContainer.getChildren().add(placeholder);
        }

        // Show pagination only if there are more than 3 offers
        paginationBox.setVisible(allInvestments.size() > 3);
    }

    private HBox createInvestmentRow(InvestmentData data) {
        HBox row = new HBox(40);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox left = new VBox(5);
        Label name = new Label(data.name + " (T" + data.tier + ")");
        name.setFont(Font.font("System", FontWeight.BOLD, 18));
        name.setTextFill(Color.WHITE);

        Label loc = new Label("Location: " + data.location);
        loc.setTextFill(Color.rgb(255, 255, 255, 0.9));

        Label price = new Label("Price: " + String.format("%,d", data.price) + " silver");
        price.setTextFill(Color.rgb(255, 255, 255, 0.9));

        left.getChildren().addAll(name, loc, price);

        VBox right = new VBox(6);
        right.setAlignment(Pos.CENTER_RIGHT);

        right.getChildren().addAll(
                new Label("Enchantment: " + data.enchantment),
                new Label("Demand (24h): " + data.demand),
                new Label("Cost: " + String.format("%,d", data.cost) + " silver"),
                new Label("PROFIT: " + String.format("%,d", data.profit) + " silver"),
                new Label("ROI: " + String.format("%.0f", data.roi) + "%")
        );

        // Make profit and ROI green
        ((Label) right.getChildren().get(3)).setTextFill(Color.rgb(80, 255, 120));
        ((Label) right.getChildren().get(4)).setTextFill(Color.rgb(80, 255, 120));
        ((Label) right.getChildren().get(3)).setFont(Font.font("System", FontWeight.BOLD, 16));
        ((Label) right.getChildren().get(4)).setFont(Font.font("System", FontWeight.BOLD, 16));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(left, spacer, right);
        return row;
    }

    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            updateDisplay();
        }
    }

    private void nextPage() {
        if ((currentPage + 1) * 3 < allInvestments.size()) {
            currentPage++;
            updateDisplay();
        }
    }
}