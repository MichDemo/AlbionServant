package com.albionservant.gui;

import com.albionservant.data.InvestmentData;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Hot-investment overview whose complete appearance is owned by JavaFX CSS.
 * Java only supplies structure, content and semantic state classes.
 */
public class HotInvestmentsPanel extends VBox {

    private static final int ITEMS_PER_PAGE = 3;
    private static final double ITEMS_AREA_HEIGHT = 306.0;

    private final VBox itemsContainer = new VBox(18);
    private final HBox paginationBox = new HBox(20);

    private Button prevButton;
    private Button nextButton;
    private Label pageLabel;
    private int currentPage;
    private List<InvestmentData> allInvestments = List.of();

    public HotInvestmentsPanel() {
        getStyleClass().add("hot-investments-panel");
        setPadding(new Insets(25, 30, 20, 30));
        setAlignment(Pos.TOP_CENTER);

        VBox headerBox = buildHeader();

        itemsContainer.getStyleClass().add("hot-investments-items");
        itemsContainer.setAlignment(Pos.TOP_CENTER);
        itemsContainer.setPadding(new Insets(15, 0, 15, 0));
        itemsContainer.setFillWidth(true);
        itemsContainer.setMinHeight(ITEMS_AREA_HEIGHT);
        itemsContainer.setPrefHeight(ITEMS_AREA_HEIGHT);
        itemsContainer.setMaxHeight(ITEMS_AREA_HEIGHT);

        buildPaginationControls();
        paginationBox.setPadding(new Insets(12, 0, 18, 0));

        getChildren().addAll(headerBox, itemsContainer, paginationBox);
    }

    private VBox buildHeader() {
        Label title = styledLabel("🔥 Hot Investments", "hot-investments-title");
        Label subtitle = styledLabel(
                "Best crafting / flipping opportunities right now",
                "hot-investments-subtitle"
        );

        VBox headerBox = new VBox(5, title, subtitle);
        headerBox.getStyleClass().add("hot-investments-header");
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(0, 0, 15, 0));
        return headerBox;
    }

    private void buildPaginationControls() {
        prevButton = new Button("← Previous");
        prevButton.getStyleClass().addAll("button-secondary", "hot-investments-page-button");
        prevButton.setOnAction(event -> previousPage());

        nextButton = new Button("Next →");
        nextButton.getStyleClass().addAll("button-primary", "hot-investments-page-button");
        nextButton.setOnAction(event -> nextPage());

        pageLabel = styledLabel("1 / 1", "hot-investments-page-label");

        paginationBox.getStyleClass().add("hot-investments-pagination");
        paginationBox.setAlignment(Pos.CENTER);
        paginationBox.getChildren().addAll(prevButton, pageLabel, nextButton);
    }

    public void setInvestments(List<InvestmentData> investments) {
        allInvestments = investments != null ? investments : List.of();
        currentPage = 0;
        updateDisplay();
        updateButtonStates();
    }

    private void updateDisplay() {
        itemsContainer.getChildren().clear();

        if (allInvestments.isEmpty()) {
            Label empty = styledLabel(
                    "No investments loaded yet.",
                    "hot-investments-empty"
            );
            itemsContainer.getChildren().add(empty);
            return;
        }

        int start = currentPage * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, allInvestments.size());

        for (int index = start; index < end; index++) {
            itemsContainer.getChildren().add(createInvestmentRow(allInvestments.get(index)));
        }
    }

    private HBox createInvestmentRow(InvestmentData data) {
        HBox row = new HBox(20);
        row.getStyleClass().add("hot-investment-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 20, 12, 20));

        VBox nameBox = new VBox(2);
        nameBox.getStyleClass().add("hot-investment-name-box");

        Label nameLabel = styledLabel(data.getName(), "hot-investment-name");
        Label tierLabel = styledLabel(
                "T" + data.getTier(),
                "hot-investment-tier",
                tierStyleClass(data.getTier())
        );
        nameBox.getChildren().addAll(nameLabel, tierLabel);

        Label routeLabel = styledLabel(data.getLocation(), "hot-investment-route");
        Label buyLabel = styledLabel(
                String.format("%,d", data.getPrice()),
                "hot-investment-price"
        );

        long profit = data.getProfit();
        Label profitLabel = styledLabel(
                (profit >= 0 ? "+" : "") + String.format("%,d", profit),
                "hot-investment-profit",
                profit >= 0 ? "metric-positive" : "metric-negative"
        );

        double roi = data.getRoi();
        Label roiLabel = styledLabel(
                String.format("%.1f%%", roi),
                "hot-investment-roi",
                roi >= 0 ? "metric-warning" : "metric-negative"
        );

        HBox.setHgrow(nameBox, Priority.ALWAYS);
        HBox.setHgrow(routeLabel, Priority.ALWAYS);
        row.getChildren().addAll(nameBox, routeLabel, buyLabel, profitLabel, roiLabel);
        return row;
    }

    private static String tierStyleClass(int tier) {
        return switch (tier) {
            case 2 -> "tier-2-text";
            case 3 -> "tier-3-text";
            case 4 -> "tier-4-text";
            case 5 -> "tier-5-text";
            case 6 -> "tier-6-text";
            case 7 -> "tier-7-text";
            case 8 -> "tier-8-text";
            default -> "text-secondary";
        };
    }

    private static Label styledLabel(String text, String... styleClasses) {
        Label label = new Label(text);
        label.getStyleClass().addAll(styleClasses);
        return label;
    }

    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            updateDisplay();
            updateButtonStates();
        }
    }

    private void nextPage() {
        int totalPages = (int) Math.ceil((double) allInvestments.size() / ITEMS_PER_PAGE);
        if (currentPage + 1 < totalPages) {
            currentPage++;
            updateDisplay();
            updateButtonStates();
        }
    }

    private void updateButtonStates() {
        int totalPages = (int) Math.ceil((double) allInvestments.size() / ITEMS_PER_PAGE);
        if (totalPages == 0) {
            totalPages = 1;
        }

        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable(currentPage >= totalPages - 1);
        pageLabel.setText((currentPage + 1) + " / " + totalPages);
    }
}
