package com.albionservant.gui;

import com.albionservant.AppConfig;
import com.albionservant.integration.market.LocalMarketQueryService;
import com.albionservant.integration.market.LocalMarketRow;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class MarketPanel extends VBox {

    private static final int ROW_LIMIT = 1000;

    private final LocalMarketQueryService marketService =
            new LocalMarketQueryService();

    private final TextField searchField = new TextField();
    private final ComboBox<String> locationBox = new ComboBox<>();
    private final ComboBox<String> typeBox = new ComboBox<>();
    private final Button refreshButton = new Button("ODSWIEZ");
    private final Label statusLabel = new Label();
    private final TableView<LocalMarketRow> table = new TableView<>();
    private final AtomicBoolean loading = new AtomicBoolean(false);

    public MarketPanel() {
        setSpacing(14);
        setPadding(new Insets(22));
        setFillWidth(true);
        setBackground(new Background(
                new BackgroundFill(AppConfig.BACKGROUND_MAIN, null, null)
        ));

        Label title = new Label("Lokalny rynek");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(AppConfig.TEXT_PRIMARY);

        Label subtitle = new Label(
                "Dane AODP zapisane lokalnie w SQLite"
        );
        subtitle.setTextFill(AppConfig.TEXT_SECONDARY);

        configureFilters();
        configureTable();

        VBox header = new VBox(4, title, subtitle);
        HBox filters = new HBox(
                10,
                searchField,
                locationBox,
                typeBox,
                refreshButton
        );

        filters.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        statusLabel.setTextFill(AppConfig.TEXT_SECONDARY);
        statusLabel.setWrapText(true);

        getChildren().addAll(header, filters, statusLabel, table);
        VBox.setVgrow(table, Priority.ALWAYS);

        refreshButton.setOnAction(event -> refresh());
        searchField.setOnAction(event -> refresh());
        locationBox.setOnAction(event -> refresh());
        typeBox.setOnAction(event -> refresh());

        refresh();
    }

    public void refresh() {
        if (!loading.compareAndSet(false, true)) {
            return;
        }

        refreshButton.setDisable(true);
        statusLabel.setText("Wczytywanie danych rynku...");

        String search = searchField.getText();
        String location = locationBox.getValue();
        String type = typeBox.getValue();

        Task<Snapshot> task = new Task<>() {
            @Override
            protected Snapshot call() throws Exception {
                return new Snapshot(
                        marketService.findLatest(
                                search,
                                location,
                                type,
                                ROW_LIMIT
                        ),
                        marketService.findLocations(),
                        marketService.countLatestRows()
                );
            }
        };

        task.setOnSucceeded(event -> {
            Snapshot snapshot = task.getValue();
            table.getItems().setAll(snapshot.rows());
            updateLocations(snapshot.locations());

            statusLabel.setText(
                    "Wyswietlono: " + snapshot.rows().size()
                            + " | Rekordy latest: " + snapshot.totalRows()
                            + " | Baza: " + marketService.databasePath()
            );

            finishLoading();
        });

        task.setOnFailed(event -> {
            Throwable error = task.getException();

            statusLabel.setText(
                    "Blad odczytu rynku: "
                            + (error == null ? "nieznany" : error.getMessage())
                            + " | Baza: " + marketService.databasePath()
            );

            finishLoading();
        });

        Thread thread = new Thread(task, "local-market-query");
        thread.setDaemon(true);
        thread.start();
    }

    private void configureFilters() {
        searchField.setPromptText(
                "Szukaj ItemTypeId, lokacji lub typu zlecenia..."
        );
        searchField.setPrefHeight(36);

        locationBox.getItems().setAll("WSZYSTKIE");
        locationBox.setValue("WSZYSTKIE");
        locationBox.setPrefWidth(180);
        locationBox.setPrefHeight(36);

        typeBox.getItems().setAll("WSZYSTKIE", "offer", "request");
        typeBox.setValue("WSZYSTKIE");
        typeBox.setPrefWidth(150);
        typeBox.setPrefHeight(36);

        refreshButton.setPrefHeight(36);
        refreshButton.setStyle(
                "-fx-background-color: " + toHex(AppConfig.TAB_ACTIVE) + ";"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 7;"
                        + "-fx-padding: 0 18;"
        );
    }

    private void configureTable() {
        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );
        table.setPlaceholder(new Label(
                "Brak danych. Otworz market w Albion Online i odswiez."
        ));

        TableColumn<LocalMarketRow, String> item =
                textColumn("Przedmiot", LocalMarketRow::itemTypeId);
        item.setPrefWidth(280);

        TableColumn<LocalMarketRow, String> location =
                textColumn("Lokacja", LocalMarketRow::locationId);

        TableColumn<LocalMarketRow, Number> quality =
                new TableColumn<>("Jakosc");
        quality.setCellValueFactory(data ->
                new ReadOnlyIntegerWrapper(
                        data.getValue().qualityLevel()
                )
        );

        TableColumn<LocalMarketRow, Number> enchantment =
                new TableColumn<>("Zaklecie");
        enchantment.setCellValueFactory(data ->
                new ReadOnlyIntegerWrapper(
                        data.getValue().enchantmentLevel()
                )
        );

        TableColumn<LocalMarketRow, String> type =
                textColumn("Typ", LocalMarketRow::auctionType);

        TableColumn<LocalMarketRow, Number> price =
                new TableColumn<>("Cena");
        price.setCellValueFactory(data ->
                new ReadOnlyLongWrapper(
                        data.getValue().unitPriceRaw()
                )
        );
        price.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Number value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null || getTableRow() == null) {
                    setText(null);
                    setTooltip(null);
                    return;
                }

                LocalMarketRow row =
                        (LocalMarketRow) getTableRow().getItem();

                if (row == null) {
                    setText(null);
                    return;
                }

                setText(formatSilver(row) + " silver");
                setTooltip(new Tooltip(
                        "Raw UnitPriceSilver: " + row.unitPriceRaw()
                ));
            }
        });

        TableColumn<LocalMarketRow, Number> amount =
                new TableColumn<>("Ilosc");
        amount.setCellValueFactory(data ->
                new ReadOnlyLongWrapper(data.getValue().amount())
        );

        TableColumn<LocalMarketRow, String> updated =
                textColumn("Aktualizacja", LocalMarketRow::updatedAt);
        updated.setPrefWidth(190);

        table.getColumns().setAll(
                item,
                location,
                quality,
                enchantment,
                type,
                price,
                amount,
                updated
        );

        table.setRowFactory(view -> {
            TableRow<LocalMarketRow> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    ClipboardContent content = new ClipboardContent();
                    content.putString(row.getItem().itemTypeId());
                    Clipboard.getSystemClipboard().setContent(content);

                    statusLabel.setText(
                            "Skopiowano ItemTypeId: "
                                    + row.getItem().itemTypeId()
                    );
                }
            });

            return row;
        });

        table.setStyle(
                "-fx-background-color: " + toHex(AppConfig.BACKGROUND_MAIN) + ";"
                        + "-fx-control-inner-background: "
                        + toHex(AppConfig.BACKGROUND_MAIN) + ";"
                        + "-fx-table-cell-border-color: rgba(255,255,255,0.08);"
                        + "-fx-text-background-color: white;"
        );
    }

    private TableColumn<LocalMarketRow, String> textColumn(
            String title,
            Function<LocalMarketRow, String> extractor
    ) {
        TableColumn<LocalMarketRow, String> column =
                new TableColumn<>(title);

        column.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        extractor.apply(data.getValue())
                )
        );

        return column;
    }

    private void updateLocations(List<String> locations) {
        String selected = locationBox.getValue();

        locationBox.getItems().setAll("WSZYSTKIE");
        locationBox.getItems().addAll(locations);

        if (selected != null
                && locationBox.getItems().contains(selected)) {
            locationBox.setValue(selected);
        } else {
            locationBox.setValue("WSZYSTKIE");
        }
    }

    private String formatSilver(LocalMarketRow row) {
        NumberFormat formatter = NumberFormat.getNumberInstance(
                Locale.forLanguageTag("pl-PL")
        );

        formatter.setMaximumFractionDigits(2);
        formatter.setMinimumFractionDigits(0);

        return formatter.format(row.unitPriceSilver());
    }

    private void finishLoading() {
        loading.set(false);
        refreshButton.setDisable(false);
    }

    private String toHex(Color color) {
        return String.format(
                "#%02x%02x%02x",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255)
        );
    }

    private record Snapshot(
            List<LocalMarketRow> rows,
            List<String> locations,
            long totalRows
    ) {
    }
}
