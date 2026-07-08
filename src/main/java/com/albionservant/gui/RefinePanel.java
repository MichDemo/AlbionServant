package com.albionservant.gui;

import com.albionservant.AppConfig;
import com.albionservant.data.RefineData;
import com.albionservant.data.RefineData.MaterialFamily;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.function.Consumer;

/**
 * Refine panel — mirrors CraftPanel structure.
 *
 * Root state: 5 big centered material buttons (Metal Bars, Planks, Leather, Cloth, Stone Blocks)
 * Detail state: refining grid with tier/enchantment rows + config + quantity needed sidebar
 */
public class RefinePanel extends VBox {

    private static final String[] CITIES = {
            "Brecilien", "Bridgewatch", "Caerleon",
            "Fort Sterling", "Lymhurst", "Martlock", "Thetford"
    };

    private final HBox  topBar     = new HBox(15);
    private final VBox  contentArea = new VBox();
    private MaterialFamily currentMaterial = null;
    private Consumer<Boolean> onDetailModeListener;

    public RefinePanel() {
        setAlignment(Pos.TOP_CENTER);
        setPadding(new Insets(0));
        setStyle("-fx-background-color: #ef4444;");

        // ── Top bar ──────────────────────────────────────────────────────────
        Label breadcrumb = new Label("Refine");
        breadcrumb.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        HBox.setHgrow(breadcrumb, Priority.ALWAYS);

        Button backBtn = new Button("← Back");
        backBtn.setStyle(AppConfig.BTN_PRIMARY);
        backBtn.setOnMouseEntered(e -> backBtn.setStyle(AppConfig.BTN_PRIMARY_HOVER));
        backBtn.setOnMouseExited(e  -> backBtn.setStyle(AppConfig.BTN_PRIMARY));
        backBtn.setOnAction(e -> goBack());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topBar.getChildren().addAll(breadcrumb, spacer, backBtn);
        topBar.setPadding(new Insets(14, 28, 14, 28));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #ef4444;");

        contentArea.setFillWidth(true);
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(contentArea);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().addAll(topBar, scroll);
        showRoot();
    }

    public void setOnDetailModeListener(Consumer<Boolean> listener) {
        this.onDetailModeListener = listener;
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    private void goBack() {
        currentMaterial = null;
        if (onDetailModeListener != null) onDetailModeListener.accept(false);
        topBar.setVisible(true);
        topBar.setManaged(true);
        showRoot();
    }

    private void selectMaterial(MaterialFamily family) {
        currentMaterial = family;
        if (onDetailModeListener != null) onDetailModeListener.accept(true);
        topBar.setVisible(false);
        topBar.setManaged(false);
        showDetail(family);
    }

    // ── ROOT: 5 centered material buttons ────────────────────────────────────

    private void showRoot() {
        contentArea.getChildren().clear();

        VBox centerBox = new VBox(24);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(60, 40, 60, 40));
        centerBox.setStyle("-fx-background-color: #f1f5f9;");
        VBox.setVgrow(centerBox, Priority.ALWAYS);

        Label prompt = new Label("Choose a material to refine");
        prompt.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #555;");

        HBox btnRow = new HBox(20);
        btnRow.setAlignment(Pos.CENTER);

        for (MaterialFamily family : RefineData.ALL) {
            Button btn = new Button(family.displayName());
            btn.setPrefWidth(200);
            btn.setPrefHeight(70);
            btn.setStyle(AppConfig.BTN_ROOT);
            btn.setOnMouseEntered(e -> btn.setStyle(AppConfig.BTN_ROOT_HOVER));
            btn.setOnMouseExited(e  -> btn.setStyle(AppConfig.BTN_ROOT));
            btn.setOnAction(e -> selectMaterial(family));
            btnRow.getChildren().add(btn);
        }

        centerBox.getChildren().addAll(prompt, btnRow);
        contentArea.getChildren().add(centerBox);
    }

    // ── DETAIL: refining grid ─────────────────────────────────────────────────

    private void showDetail(MaterialFamily family) {
        contentArea.getChildren().clear();

        VBox detail = new VBox(0);
        detail.setFillWidth(true);
        VBox.setVgrow(detail, Priority.ALWAYS);

        // ── Red header ───────────────────────────────────────────────────────
        HBox redHeader = new HBox(15);
        redHeader.setPadding(new Insets(15, 40, 15, 40));
        redHeader.setStyle("-fx-background-color: #ef4444;");
        redHeader.setAlignment(Pos.CENTER_LEFT);
        Label headerLbl = new Label("Refine  ►  " + family.displayName());
        headerLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);
        Button backBtn = new Button("← Back");
        backBtn.setStyle(AppConfig.BTN_PRIMARY);
        backBtn.setOnMouseEntered(e -> backBtn.setStyle(AppConfig.BTN_PRIMARY_HOVER));
        backBtn.setOnMouseExited(e  -> backBtn.setStyle(AppConfig.BTN_PRIMARY));
        backBtn.setOnAction(e -> goBack());
        redHeader.getChildren().addAll(headerLbl, hSpacer, backBtn);

        // ── 3-column config (mirrors CraftDetailSubPanel) ────────────────────
        HBox configSection = buildConfigSection(family);

        // ── Main content area: grid on top, quantity bar below ────────────────
        VBox mainArea = new VBox(0);
        mainArea.setFillWidth(true);
        VBox.setVgrow(mainArea, Priority.ALWAYS);

        VBox gridSection = buildRefineGrid(family);
        HBox quantityBar = buildQuantityBar(family);

        mainArea.getChildren().addAll(gridSection, quantityBar);

        ScrollPane contentScroll = new ScrollPane(mainArea);
        contentScroll.setFitToWidth(true);
        contentScroll.setStyle("-fx-background-color: #ffffff;");
        VBox.setVgrow(contentScroll, Priority.ALWAYS);

        detail.getChildren().addAll(redHeader, configSection, contentScroll);
        contentArea.getChildren().add(detail);
    }

    // ── Config section (3-column, mirrors CraftDetailSubPanel) ──────────────

    private HBox buildConfigSection(MaterialFamily family) {
        HBox section = new HBox(30);
        section.setPadding(new Insets(20, 40, 20, 40));
        section.setAlignment(Pos.TOP_LEFT);
        section.setMaxWidth(Double.MAX_VALUE);
        section.setStyle("-fx-background-color: #ffffff;");

        // LEFT: material icon
        VBox left = new VBox(10);
        left.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(left, Priority.ALWAYS);

        Label iconLbl = new Label();
        StackPane iconPane = new StackPane(new ProgressIndicator());
        iconPane.setPrefSize(128, 128);
        iconPane.setMaxSize(128, 128);
        iconPane.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 8;");

        // Load refined T8 icon
        String iconId = family.refinedIconIds()[6]; // T8 index
        if (iconId != null) {
            Image img = new Image(
                    "https://render.albiononline.com/v1/item/" + iconId + ".png",
                    128, 128, true, true, true);
            img.progressProperty().addListener((obs, ov, nv) -> {
                if (nv.doubleValue() >= 1.0) {
                    Platform.runLater(() -> {
                        iconPane.getChildren().clear();
                        if (!img.isError()) {
                            ImageView iv = new ImageView(img);
                            iv.setFitWidth(128);
                            iv.setFitHeight(128);
                            iconPane.getChildren().add(iv);
                        }
                    });
                }
            });
        }

        Label matLabel = new Label(family.rawLabel() + "  →  " + family.displayName());
        matLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        left.getChildren().addAll(iconPane, matLabel);

        // CENTER: quantity, station fee, focus, bonus city
        VBox center = new VBox(8);
        center.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(center, Priority.ALWAYS);

        center.getChildren().addAll(
                cfgLabel("Quantity:"),       cfgField("25000"),
                cfgLabel("Station Fee:"),    cfgField("150"),
                cfgLabel("Demand Average:"), cfgField("7"),
                cfgCheckbox("Use Focus"),
                cfgLabel("Bonus City:"),     cfgCombo(new String[]{"Island","Royal City","Hideout"}, "Island"),
                cfgLabel("Daily Bonus:"),    cfgCombo(new String[]{"None","Small (+10%)","Large (+20%)"}, "None"),
                cfgCheckbox("No Tax")
        );

        // RIGHT: buy/sell locations
        VBox right = new VBox(8);
        right.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(right, Priority.ALWAYS);

        right.getChildren().addAll(
                cfgLabel("Buy Raw At:"),      cfgCombo(citiesArray(), "Fort Sterling"),
                cfgLabel("Sell Refined At:"), cfgCombo(citiesArray(), "Fort Sterling"),
                cfgLabel("Refine Tier:"),     cfgCombo(new String[]{"T2","T3","T4","T5","T6","T7","T8"}, "T5"),
                cfgLabel("Stack From:"),      cfgCombo(new String[]{"T2","T3","T4","T5","T6","T7","T8"}, "T2"),
                cfgLabel("Stack To:"),       cfgCombo(new String[]{"T2","T3","T4","T5","T6","T7","T8"}, "T8")
        );

        section.getChildren().addAll(left, center, right);
        return section;
    }

    // ── Refine grid ───────────────────────────────────────────────────────────
    //
    // Rows: T2(.0) | T3(.0,.1,.2,.3,.4) | T4(.0-.4) | ... | T8(.0-.4)  = 29 rows
    // Cols: Tier | Raw API | Raw Manual | PrevRef API | PrevRef Manual
    //       | Output API | Output Manual | Demand | Profit | SPF | Gain | Cost | Focus Cost

    private VBox buildRefineGrid(MaterialFamily family) {
        String[] resultHeaders = {"Demand", "Profit", "SPF", "Gain", "Cost", "Focus Cost"};
        // col 0: Tier  col 1-2: Raw API/Manual  col 3-4: Prev Refined API/Manual
        // col 5-6: Output API/Manual  col 7-12: results
        int totalCols = 1 + 2 + 2 + 2 + resultHeaders.length; // = 13

        GridPane grid = new GridPane();
        grid.setMaxWidth(Double.MAX_VALUE);
        grid.setVgap(4);
        grid.setHgap(0);
        grid.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 20 40 20 40;");

        double colPct = 100.0 / totalCols;
        for (int i = 0; i < totalCols; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(colPct);
            cc.setHalignment(HPos.CENTER);
            cc.setFillWidth(true);
            grid.getColumnConstraints().add(cc);
        }

        // ── Header row 0: group labels ──────────────────────────────────────
        grid.add(gridHdr("Tier", null),           0, 0);
        addSpanHeader(grid, family.rawLabel(),     1, 0, 2, "#ef4444");
        addSpanHeader(grid, "Prev. Refined",       3, 0, 2, "#64748b");
        addSpanHeader(grid, "Refined Output",      5, 0, 2, "#4ade80");
        int rc = 7;
        for (String h : resultHeaders) grid.add(gridHdr(h, null), rc++, 0);

        // ── Header row 1: API/Manual sub-headers ───────────────────────────
        grid.add(new Label(""), 0, 1);
        for (int c = 1; c <= 5; c += 2) {
            grid.add(subHdr("API"),    c,   1);
            grid.add(subHdr("Manual"), c+1, 1);
        }
        for (int i = 0; i < resultHeaders.length; i++) grid.add(new Label(""), 7 + i, 1);

        // ── Icon row: one icon per tier (span across raw cols) ──────────────
        int iconRow = 2;
        for (int ti = 0; ti < RefineData.TIERS.length; ti++) {
            int tier = RefineData.TIERS[ti];
            String iconId = family.rawIconIds()[ti];

            StackPane iconPane = new StackPane();
            iconPane.setPrefSize(40, 40);
            iconPane.setMaxSize(40, 40);
            iconPane.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 4;");

            if (iconId != null) {
                Image img = new Image(
                        "https://render.albiononline.com/v1/item/" + iconId + ".png",
                        40, 40, true, true, true);
                img.progressProperty().addListener((obs, ov, nv) -> {
                    if (nv.doubleValue() >= 1.0 && !img.isError()) {
                        Platform.runLater(() -> {
                            ImageView iv = new ImageView(img);
                            iv.setFitWidth(40);
                            iv.setFitHeight(40);
                            iconPane.getChildren().setAll(iv);
                        });
                    }
                });
            }

            Label tierLbl = new Label("T" + tier);
            tierLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

            VBox iconBox = new VBox(2, iconPane, tierLbl);
            iconBox.setAlignment(Pos.CENTER);

            int enchantCount = RefineData.enchantmentsForTier(tier).length;
            if (enchantCount > 1) {
                GridPane.setRowSpan(iconBox, enchantCount);
            }
            grid.add(iconBox, 0, iconRow);
            iconRow += enchantCount;
        }

        // ── Data rows ────────────────────────────────────────────────────────
        int dataRow = 2;
        for (int ti = 0; ti < RefineData.TIERS.length; ti++) {
            int tier = RefineData.TIERS[ti];
            boolean hasT2 = (tier == 2);
            int[] enchants = RefineData.enchantmentsForTier(tier);
            String rowTierBase = "T" + tier;

            for (int enc : enchants) {
                String tierLabel = enc == 0 ? rowTierBase + ".0" : rowTierBase + "." + enc;

                // Tier base colour — light tint of official Albion tier colour
                // T2=grey, T3=green, T4=blue, T5=red, T6=orange, T7=yellow, T8=silver
                String tierBg = switch (tier) {
                    case 2 -> "#f0f0f0";   // grey   — Beginner
                    case 3 -> "#edf4e8";   // green  — Journeyman  (#567043 tint)
                    case 4 -> "#e8f0f7";   // blue   — Adept       (#557E98 tint)
                    case 5 -> "#faeae8";   // red    — Expert      (#934038 tint)
                    case 6 -> "#fdf0e3";   // orange — Master      (#D8894C tint)
                    case 7 -> "#fdf8e3";   // yellow — Grandmaster (#E8C95F tint)
                    case 8 -> "#f5f5f5";   // silver — Elder
                    default -> "#ffffff";
                };
                String bg = switch (enc) {
                    case 0 -> tierBg;                    // .0 — no enchant tint
                    case 1 -> blend(tierBg, "#bbf7d0"); // .1 — green
                    case 2 -> blend(tierBg, "#bfdbfe"); // .2 — blue
                    case 3 -> blend(tierBg, "#e9d5ff"); // .3 — purple
                    case 4 -> blend(tierBg, "#fef08a"); // .4 — yellow
                    default -> tierBg;
                };

                Label encLbl = new Label(tierLabel);
                encLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8; -fx-padding: 1 4;");

                // Raw material — API (read-only) + Manual (editable)
                grid.add(apiInput(bg), 1, dataRow);
                grid.add(editInput(bg), 2, dataRow);

                // Previous refined — disabled for T2
                grid.add(hasT2 ? naField() : apiInput(bg), 3, dataRow);
                grid.add(hasT2 ? naField() : editInput(bg), 4, dataRow);

                // Output refined — API (read-only) + Manual
                grid.add(apiInput(bg), 5, dataRow);
                grid.add(editInput(bg), 6, dataRow);

                // Result columns — use same bg as the row
                String[] resultDefaults = {"—", "—", "—", "—", "—", "—"};
                for (int ri = 0; ri < resultDefaults.length; ri++) {
                    Label rl = new Label(resultDefaults[ri]);
                    rl.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569; -fx-alignment: center;"
                            + "-fx-background-color: " + bg + ";");
                    rl.setMaxWidth(Double.MAX_VALUE);
                    rl.setAlignment(Pos.CENTER);
                    grid.add(rl, 7 + ri, dataRow);
                }

                dataRow++;
            } // end for enc
        } // end for tier

        VBox wrapper = new VBox(grid);
        wrapper.setFillWidth(true);
        wrapper.setMaxWidth(Double.MAX_VALUE);
        return wrapper;
    }

    // ── Quantity Needed bar ───────────────────────────────────────────────────
    // Horizontal strip below the grid — one cell per tier (T2–T8), full width.

    private HBox buildQuantityBar(MaterialFamily family) {
        HBox bar = new HBox(0);
        bar.setFillHeight(true);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setStyle("-fx-background-color: #1e293b; -fx-padding: 14 40 14 40;");
        bar.setAlignment(Pos.CENTER_LEFT);

        // Title cell on the left
        Label title = new Label("Quantity\nNeeded");
        title.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #94a3b8;"
                + "-fx-text-alignment: center; -fx-alignment: center;");
        title.setPrefWidth(80);
        title.setMinWidth(80);
        title.setAlignment(Pos.CENTER);
        bar.getChildren().add(title);

        // One cell per tier
        for (int ti = 0; ti < RefineData.TIERS.length; ti++) {
            int    tier          = RefineData.TIERS[ti];
            String rawIconId     = family.rawIconIds()[ti];
            String refinedIconId = family.refinedIconIds()[ti];

            // Tier colours confirmed from Albion Online forum (eye-dropped in-game)
            // T2=grey, T3=green, T4=blue, T5=red, T6=orange, T7=yellow, T8=white/silver
            String tierBg = switch (tier) {
                case 2 -> "#3a3a3a";   // grey   — Beginner
                case 3 -> "#3D4D2F";   // green  — Journeyman
                case 4 -> "#335870";   // blue   — Adept
                case 5 -> "#6F2019";   // red    — Expert
                case 6 -> "#BE6A2A";   // orange — Master
                case 7 -> "#C8A940";   // yellow — Grandmaster
                case 8 -> "#5a5a5a";   // silver — Elder
                default -> "#1e293b";
            };

            VBox cell = new VBox(6);
            cell.setAlignment(Pos.CENTER);
            cell.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(cell, Priority.ALWAYS);
            cell.setStyle("-fx-background-color: " + tierBg + ";"
                    + "-fx-background-radius: 8; -fx-padding: 10 8 10 8; -fx-margin: 0 4 0 4;");

            // Tier label
            Label tierLbl = new Label("T" + tier);
            tierLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #e2e8f0;");

            // Icons row
            HBox iconsRow = new HBox(6);
            iconsRow.setAlignment(Pos.CENTER);

            StackPane rawIcon = tinyIcon(rawIconId);
            Label arrow = new Label("→");
            arrow.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");
            StackPane refIcon = tinyIcon(refinedIconId);

            iconsRow.getChildren().addAll(rawIcon, arrow, refIcon);

            // Quantity labels
            HBox qtyRow = new HBox(8);
            qtyRow.setAlignment(Pos.CENTER);
            Label rawQty = new Label("0");
            rawQty.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e2e8f0;");
            Label slash = new Label("/");
            slash.setStyle("-fx-text-fill: #475569; -fx-font-size: 14px;");
            Label refQty = new Label("0");
            refQty.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4ade80;");
            qtyRow.getChildren().addAll(rawQty, slash, refQty);

            cell.getChildren().addAll(tierLbl, iconsRow, qtyRow);

            // Small gap between cells
            Region gap = new Region();
            gap.setPrefWidth(6);
            bar.getChildren().addAll(cell, gap);
        }

        return bar;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Averages two hex colours for subtle tinting */
    private String blend(String hex1, String hex2) {
        int r1 = Integer.parseInt(hex1.substring(1,3), 16);
        int g1 = Integer.parseInt(hex1.substring(3,5), 16);
        int b1 = Integer.parseInt(hex1.substring(5,7), 16);
        int r2 = Integer.parseInt(hex2.substring(1,3), 16);
        int g2 = Integer.parseInt(hex2.substring(3,5), 16);
        int b2 = Integer.parseInt(hex2.substring(5,7), 16);
        return String.format("#%02x%02x%02x", (r1+r2)/2, (g1+g2)/2, (b1+b2)/2);
    }

    private Label gridHdr(String text, String color) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: "
                + (color != null ? color : "#ef4444")
                + "; -fx-font-size: 12px; -fx-padding: 6 4;");
        l.setMaxWidth(Double.MAX_VALUE);
        l.setAlignment(Pos.CENTER);
        return l;
    }

    private void addSpanHeader(GridPane grid, String text, int col, int row, int span, String color) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: " + color
                + "; -fx-font-size: 12px; -fx-padding: 6 4;");
        l.setMaxWidth(Double.MAX_VALUE);
        l.setAlignment(Pos.CENTER);
        GridPane.setColumnSpan(l, span);
        grid.add(l, col, row);
    }

    private Label subHdr(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8; -fx-padding: 2 4;");
        l.setMaxWidth(Double.MAX_VALUE);
        l.setAlignment(Pos.CENTER);
        return l;
    }

    private TextField apiInput(String bg) {
        TextField tf = new TextField("0");
        tf.setEditable(false);
        // Slightly darken the row bg to distinguish API (read-only) from Manual
        tf.setStyle("-fx-font-size: 12px; -fx-alignment: center; -fx-background-color: " + bg
                + "; -fx-opacity: 0.75;");
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private TextField apiInput() {
        return apiInput("#f0f0f0");
    }

    private TextField editInput(String bg) {
        TextField tf = new TextField("");
        tf.setStyle("-fx-font-size: 12px; -fx-alignment: center; -fx-background-color: " + bg + ";");
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private TextField naField() {
        TextField tf = new TextField("—");
        tf.setEditable(false);
        tf.setDisable(true);
        tf.setStyle("-fx-font-size: 12px; -fx-alignment: center;"
                + "-fx-background-color: #e8e8e8; -fx-text-fill: #aaaaaa;");
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private StackPane tinyIcon(String iconId) {
        StackPane pane = new StackPane();
        pane.setPrefSize(44, 44);
        pane.setMaxSize(44, 44);
        pane.setStyle("-fx-background-color: #1e3a5f; -fx-background-radius: 4;");
        if (iconId != null) {
            Image img = new Image(
                    "https://render.albiononline.com/v1/item/" + iconId + ".png",
                    44, 44, true, true, true);
            img.progressProperty().addListener((obs, ov, nv) -> {
                if (nv.doubleValue() >= 1.0 && !img.isError()) {
                    Platform.runLater(() -> {
                        ImageView iv = new ImageView(img);
                        iv.setFitWidth(44);
                        iv.setFitHeight(44);
                        pane.getChildren().setAll(iv);
                    });
                }
            });
        }
        return pane;
    }

    private Label cfgLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
        return l;
    }

    private TextField cfgField(String def) {
        TextField tf = new TextField(def);
        tf.setStyle("-fx-font-size: 13px;");
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private CheckBox cfgCheckbox(String label) {
        CheckBox cb = new CheckBox(label);
        cb.setStyle("-fx-font-size: 13px;");
        return cb;
    }

    private ComboBox<String> cfgCombo(String[] options, String defaultVal) {
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().addAll(options);
        cb.setValue(defaultVal);
        cb.setMaxWidth(Double.MAX_VALUE);
        return cb;
    }

    private String[] citiesArray() {
        return CITIES;
    }
}