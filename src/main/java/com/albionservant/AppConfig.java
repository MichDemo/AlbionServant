package com.albionservant;

import javafx.scene.paint.Color;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppConfig {

    // ── Backgrounds ───────────────────────────────────────────────────────────
    public static final Color BACKGROUND_MAIN    = Color.rgb(10, 20, 40);
    public static final Color BACKGROUND_PANEL   = Color.rgb(224, 78, 78);
    public static final Color BACKGROUND_TAB_BAR = Color.rgb(30, 41, 59);

    // ── Text ──────────────────────────────────────────────────────────────────
    public static final Color TEXT_PRIMARY   = Color.rgb(220, 230, 255);
    public static final Color TEXT_SECONDARY = Color.rgb(160, 174, 192);

    // ── Brand / Accent ────────────────────────────────────────────────────────
    public static final Color BRAND_RED       = Color.rgb(239, 68,  68);
    public static final Color BRAND_RED_DARK  = Color.rgb(220, 38,  38);

    // ── Tab bar ───────────────────────────────────────────────────────────────
    public static final Color TAB_ACTIVE   = BRAND_RED;
    public static final Color TAB_INACTIVE = Color.rgb(51, 65, 85);
    public static final Color TAB_TEXT     = Color.rgb(235, 235, 240);

    // =========================================================================
    //  BUTTON TOKENS  — every button in the app uses exactly one of these
    // =========================================================================

    // ── Primary / pagination (dark navy + subtle border) ─────────────────────
    public static final String BTN_PRIMARY =
            "-fx-background-color: #1e293b;" +
                    "-fx-text-fill: #e2e8f0;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 13px;" +
                    "-fx-background-radius: 8;" +
                    "-fx-padding: 9 22;" +
                    "-fx-border-color: #475569;" +
                    "-fx-border-radius: 8;" +
                    "-fx-border-width: 1;";

    public static final String BTN_PRIMARY_HOVER =
            "-fx-background-color: #334155;" +
                    "-fx-text-fill: #ffffff;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 13px;" +
                    "-fx-background-radius: 8;" +
                    "-fx-padding: 9 22;" +
                    "-fx-border-color: #64748b;" +
                    "-fx-border-radius: 8;" +
                    "-fx-border-width: 1;" +
                    "-fx-cursor: hand;";

    // ── Secondary / Back — alias of primary so all utility buttons match ──────
    public static final String BTN_SECONDARY       = BTN_PRIMARY;
    public static final String BTN_SECONDARY_HOVER = BTN_PRIMARY_HOVER;

    // ── Selected / accent (brand-red fill) ───────────────────────────────────
    public static final String BTN_SELECTED =
            "-fx-background-color: #ef4444;" +
                    "-fx-text-fill: #ffffff;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 13px;" +
                    "-fx-background-radius: 8;" +
                    "-fx-padding: 10 18;";

    // ── Tree navigator — active (next) column ────────────────────────────────
    public static final String BTN_ACTIVE =
            "-fx-background-color: #1e293b;" +
                    "-fx-text-fill: #e2e8f0;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 13px;" +
                    "-fx-background-radius: 8;" +
                    "-fx-padding: 10 18;";

    public static final String BTN_ACTIVE_HOVER =
            "-fx-background-color: #ef4444;" +
                    "-fx-text-fill: #ffffff;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 13px;" +
                    "-fx-background-radius: 8;" +
                    "-fx-padding: 10 18;" +
                    "-fx-cursor: hand;";

    // ── Tree navigator — traversed (side) columns ────────────────────────────
    public static final String BTN_TRAVERSED =
            "-fx-background-color: #334155;" +
                    "-fx-text-fill: #94a3b8;" +
                    "-fx-font-size: 13px;" +
                    "-fx-background-radius: 8;" +
                    "-fx-padding: 10 18;";

    // ── Root category large buttons (Gear / Food / Potion) ───────────────────
    public static final String BTN_ROOT =
            "-fx-background-color: #ef4444;" +
                    "-fx-text-fill: #ffffff;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 18px;" +
                    "-fx-background-radius: 10;" +
                    "-fx-padding: 18 32;";

    public static final String BTN_ROOT_HOVER =
            "-fx-background-color: #dc2626;" +
                    "-fx-text-fill: #ffffff;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 18px;" +
                    "-fx-background-radius: 10;" +
                    "-fx-padding: 18 32;" +
                    "-fx-cursor: hand;";

    // ── App metadata ──────────────────────────────────────────────────────────
    public static final String APP_TITLE = "AlbionServant";
    public static final String VERSION   = "0.1.0";
}