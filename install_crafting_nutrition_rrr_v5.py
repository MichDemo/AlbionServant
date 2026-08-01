#!/usr/bin/env python3
from __future__ import annotations

import argparse
import re
import shutil
import sys
import urllib.request
import xml.etree.ElementTree as ET
from datetime import datetime
from decimal import Decimal, InvalidOperation
from html.parser import HTMLParser
from pathlib import Path


ITEMS_XML_URL = (
    "https://raw.githubusercontent.com/ao-data/ao-bin-dumps/"
    "master/items.xml"
)
FORMATTED_ITEMS_URL = (
    "https://raw.githubusercontent.com/ao-data/ao-bin-dumps/"
    "master/formatted/items.txt"
)
PATCH_MARKER = "ALBIONSERVANT_NUTRITION_RRR_PATCH_V5"
NUTRITION_MULTIPLIER = Decimal("0.1125")
TYPES_PAGE_URL = "https://www.tools4albion.com/types.php"


CRAFTABLE_ID_PATTERNS = (
    re.compile(
        r"^T[2-8]_(?:HEAD|ARMOR|SHOES|MAIN|2H|OFF|BAG|CAPE(?:ITEM)?)_?"
    ),
    re.compile(r"^T[2-8]_MEAL_"),
    re.compile(r"^T[2-8]_POTION_"),
)

SOURCE_MARKERS = (
    b"T4_HEAD_",
    b"T3_MEAL_",
    b"T4_POTION_",
)


def strip_item_enchantment(item_id: str) -> str:
    return item_id.split("@", 1)[0].strip()


def is_supported_craftable_id(item_id: str | None) -> bool:
    if item_id is None or not item_id.strip():
        return False

    base = strip_item_enchantment(item_id).upper()
    return any(pattern.match(base) for pattern in CRAFTABLE_ID_PATTERNS)


def source_contains_craftable_markers(path: Path) -> bool:
    data = path.read_bytes()
    return all(marker in data for marker in SOURCE_MARKERS)


NUTRITION_JAVA = r'''package com.albionservant.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * Item nutrition values generated from ao-data/ao-bin-dumps.
 *
 * Station charge:
 * nutritionCost * feePer100 / 100 * craftedQuantity
 */
public final class CraftingNutritionData {

    private static final String NUTRITION_RESOURCE =
            "/data/crafting-nutrition.properties";
    private static final String NAME_IDS_RESOURCE =
            "/data/crafting-item-name-ids.tsv";

    private static final Properties NUTRITION = loadNutrition();
    private static final Map<String, String> ITEM_IDS_BY_NAME =
            loadItemIdsByName();

    private CraftingNutritionData() {
    }

    public static double nutritionCost(String exactItemId) {
        if (exactItemId == null || exactItemId.isBlank()) {
            return Double.NaN;
        }

        String raw = NUTRITION.getProperty(exactItemId.trim());

        if (raw == null || raw.isBlank()) {
            return Double.NaN;
        }

        try {
            double value = Double.parseDouble(raw);
            return value >= 0.0 ? value : Double.NaN;
        } catch (NumberFormatException ignored) {
            return Double.NaN;
        }
    }

    public static double nutritionCostForEnchant(
            String baseItemId,
            int enchantment
    ) {
        if (baseItemId == null || baseItemId.isBlank()) {
            return Double.NaN;
        }

        String base = stripEnchant(baseItemId.trim());
        int safeEnchant = Math.max(0, enchantment);

        if (safeEnchant == 0) {
            return nutritionCost(base);
        }

        List<String> candidates = List.of(
                base + "@" + safeEnchant,
                base + "_LEVEL" + safeEnchant + "@" + safeEnchant,
                base + "_LEVEL" + safeEnchant
        );

        for (String candidate : candidates) {
            double value = nutritionCost(candidate);

            if (Double.isFinite(value)) {
                return value;
            }
        }

        return Double.NaN;
    }

    public static double nutritionCostForDisplayName(
            String displayName,
            int enchantment
    ) {
        String itemId = itemIdForDisplayName(displayName);
        return nutritionCostForEnchant(itemId, enchantment);
    }

    public static String itemIdForDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return null;
        }

        return ITEM_IDS_BY_NAME.get(normalizeDisplayName(displayName));
    }

    public static double stationFee(
            String exactItemId,
            double feePer100,
            long craftedQuantity
    ) {
        double safeFee = Math.max(0.0, feePer100);
        long safeQuantity = Math.max(0L, craftedQuantity);

        if (safeFee == 0.0 || safeQuantity == 0L) {
            return 0.0;
        }

        double nutrition = nutritionCost(exactItemId);

        if (!Double.isFinite(nutrition)) {
            return Double.NaN;
        }

        return nutrition * safeFee / 100.0 * safeQuantity;
    }

    public static double stationFeeForEnchant(
            String baseItemId,
            int enchantment,
            double feePer100,
            long craftedQuantity
    ) {
        double safeFee = Math.max(0.0, feePer100);
        long safeQuantity = Math.max(0L, craftedQuantity);

        if (safeFee == 0.0 || safeQuantity == 0L) {
            return 0.0;
        }

        double nutrition = nutritionCostForEnchant(
                baseItemId,
                enchantment
        );

        if (!Double.isFinite(nutrition)) {
            return Double.NaN;
        }

        return nutrition * safeFee / 100.0 * safeQuantity;
    }

    public static double stationFeeForDisplayName(
            String displayName,
            int enchantment,
            double feePer100,
            long craftedQuantity
    ) {
        double safeFee = Math.max(0.0, feePer100);
        long safeQuantity = Math.max(0L, craftedQuantity);

        if (safeFee == 0.0 || safeQuantity == 0L) {
            return 0.0;
        }

        double nutrition = nutritionCostForDisplayName(
                displayName,
                enchantment
        );

        if (!Double.isFinite(nutrition)) {
            return Double.NaN;
        }

        return nutrition * safeFee / 100.0 * safeQuantity;
    }

    public static double parseNonNegative(String text) {
        if (text == null || text.isBlank()) {
            return 0.0;
        }

        String normalized = text
                .trim()
                .replace(" ", "")
                .replace("_", "")
                .replace(',', '.');

        try {
            return Math.max(0.0, Double.parseDouble(normalized));
        } catch (NumberFormatException ignored) {
            return 0.0;
        }
    }

    private static String stripEnchant(String itemId) {
        int at = itemId.indexOf('@');
        return at < 0 ? itemId : itemId.substring(0, at);
    }

    private static String normalizeDisplayName(String value) {
        return value
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");
    }

    private static Properties loadNutrition() {
        Properties properties = new Properties();

        try (InputStream input =
                     CraftingNutritionData.class.getResourceAsStream(
                             NUTRITION_RESOURCE
                     )) {

            if (input == null) {
                throw new IllegalStateException(
                        "Missing crafting nutrition resource: "
                                + NUTRITION_RESOURCE
                );
            }

            properties.load(input);
            return properties;

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to load crafting nutrition data",
                    exception
            );
        }
    }

    private static Map<String, String> loadItemIdsByName() {
        Map<String, String> result = new HashMap<>();

        try (InputStream input =
                     CraftingNutritionData.class.getResourceAsStream(
                             NAME_IDS_RESOURCE
                     )) {

            if (input == null) {
                throw new IllegalStateException(
                        "Missing crafting item name map: "
                                + NAME_IDS_RESOURCE
                );
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(input, StandardCharsets.UTF_8)
            )) {
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.isBlank() || line.startsWith("#")) {
                        continue;
                    }

                    int separator = line.indexOf('\t');

                    if (separator <= 0 || separator >= line.length() - 1) {
                        continue;
                    }

                    String name = line.substring(0, separator);
                    String itemId = line.substring(separator + 1);
                    result.putIfAbsent(name, itemId);
                }
            }

            return Map.copyOf(result);

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to load crafting item name map",
                    exception
            );
        }
    }
}
'''


GEAR_COST_LAMBDA = r'''                Runnable updateCostCell = () -> {
                    double grossReturnableCostPerCraft = 0.0;
                    double nonReturnableCostPerCraft = 0.0;
                    boolean hasEveryPrice = true;

                    for (int priceIndex = 0;
                         priceIndex < rowApiFields.size();
                         priceIndex++) {

                        TextField api = rowApiFields.get(priceIndex);
                        TextField manual = rowManualFields.get(priceIndex);

                        if (!LocalMarketPriceService.hasEffectivePrice(
                                api,
                                manual
                        )) {
                            hasEveryPrice = false;
                            break;
                        }

                        double lineCost =
                                LocalMarketPriceService
                                        .effectiveDisplayedPrice(api, manual)
                                        * rowMaterialQuantities.get(priceIndex);

                        boolean receivesReturns =
                                priceIndex < rowMaterialReturnable.size()
                                        && rowMaterialReturnable.get(priceIndex);

                        if (receivesReturns) {
                            grossReturnableCostPerCraft += lineCost;
                        } else {
                            nonReturnableCostPerCraft += lineCost;
                        }
                    }

                    if (!hasEveryPrice || rowApiFields.isEmpty()) {
                        costCell.setText("\u2014");
                        costCell.setTooltip(null);
                        rowBatchCost.set(Double.NaN);
                        return;
                    }

                    long crafts = requestedCrafts.getAsLong();
                    double rrr = reqPanel.getRrrFraction();

                    double returnedMaterialCostPerCraft =
                            grossReturnableCostPerCraft * (1.0 - rrr)
                                    + nonReturnableCostPerCraft;

                    double feePer100 =
                            com.albionservant.data.CraftingNutritionData
                                    .parseNonNegative(
                                            stationFeeField.getText()
                                    );

                    double nutritionPerItem =
                            com.albionservant.data.CraftingNutritionData
                                    .nutritionCost(craftedItemId);

                    double stationCost =
                            com.albionservant.data.CraftingNutritionData
                                    .stationFee(
                                            craftedItemId,
                                            feePer100,
                                            crafts
                                    );

                    if (!Double.isFinite(stationCost)) {
                        costCell.setText("\u2014");
                        costCell.setTooltip(new Tooltip(
                                "Missing nutritionCost for "
                                        + craftedItemId
                        ));
                        rowBatchCost.set(Double.NaN);
                        return;
                    }

                    double materialsAfterReturns =
                            returnedMaterialCostPerCraft * crafts;
                    double batchCost = materialsAfterReturns + stationCost;

                    rowBatchCost.set(batchCost);
                    costCell.setText(
                            LocalMarketPriceService.formatSilver(batchCost)
                    );

                    String nutritionText = Double.isFinite(nutritionPerItem)
                            ? LocalMarketPriceService.formatSilver(
                                    nutritionPerItem
                            )
                            : "\u2014";

                    costCell.setTooltip(new Tooltip(
                            "Materials after RRR: "
                                    + LocalMarketPriceService.formatSilver(
                                            materialsAfterReturns
                                    )
                                    + "\nRRR: "
                                    + String.format(
                                            java.util.Locale.US,
                                            "%.2f%%",
                                            rrr * 100.0
                                    )
                                    + "\nNutrition/item: "
                                    + nutritionText
                                    + "\nStation fee: "
                                    + LocalMarketPriceService.formatSilver(
                                            stationCost
                                    )
                    ));
                };'''


def read_text(path: Path) -> str:
    return path.read_text(encoding="utf-8-sig")


def write_text(path: Path, text: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8", newline="\n")


def find_matching_brace(text: str, opening_brace: int) -> int:
    depth = 0
    in_string = False
    in_char = False
    escaped = False
    line_comment = False
    block_comment = False
    index = opening_brace

    while index < len(text):
        char = text[index]
        nxt = text[index + 1] if index + 1 < len(text) else ""

        if line_comment:
            if char == "\n":
                line_comment = False
            index += 1
            continue

        if block_comment:
            if char == "*" and nxt == "/":
                block_comment = False
                index += 2
                continue
            index += 1
            continue

        if in_string:
            if escaped:
                escaped = False
            elif char == "\\":
                escaped = True
            elif char == '"':
                in_string = False
            index += 1
            continue

        if in_char:
            if escaped:
                escaped = False
            elif char == "\\":
                escaped = True
            elif char == "'":
                in_char = False
            index += 1
            continue

        if char == "/" and nxt == "/":
            line_comment = True
            index += 2
            continue

        if char == "/" and nxt == "*":
            block_comment = True
            index += 2
            continue

        if char == '"':
            in_string = True
            index += 1
            continue

        if char == "'":
            in_char = True
            index += 1
            continue

        if char == "{":
            depth += 1
        elif char == "}":
            depth -= 1
            if depth == 0:
                return index

        index += 1

    raise RuntimeError("Matching Java brace was not found")


def replace_braced_declaration(
        text: str,
        marker: str,
        replacement: str,
        start_from: int = 0,
) -> str:
    start = text.find(marker, start_from)
    if start < 0:
        raise RuntimeError(f"Java block not found: {marker}")

    opening = text.find("{", start)
    if opening < 0:
        raise RuntimeError(f"Opening brace not found: {marker}")

    closing = find_matching_brace(text, opening)
    end = closing + 1

    while end < len(text) and text[end] in " \t":
        end += 1
    if end < len(text) and text[end] == ";":
        end += 1

    return text[:start] + replacement + text[end:]


def add_import(text: str, import_line: str) -> str:
    if import_line in text:
        return text

    imports = list(re.finditer(r"(?m)^import\s+[^;]+;\s*$", text))
    if not imports:
        raise RuntimeError("Java import section was not found")

    position = imports[-1].end()
    return text[:position] + "\n" + import_line + text[position:]


def backup_file(path: Path, backup_root: Path, project_root: Path) -> None:
    if not path.exists():
        return

    relative = path.relative_to(project_root)
    destination = backup_root / relative
    destination.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(path, destination)


def download_items_xml(cache_path: Path) -> Path:
    cache_path.parent.mkdir(parents=True, exist_ok=True)
    print("Downloading current ao-bin-dumps items.xml...")

    request = urllib.request.Request(
        ITEMS_XML_URL,
        headers={"User-Agent": "AlbionServant nutrition installer"},
    )

    with urllib.request.urlopen(request, timeout=120) as response:
        with cache_path.open("wb") as output:
            shutil.copyfileobj(response, output)

    return cache_path


def download_formatted_items(cache_path: Path) -> Path:
    cache_path.parent.mkdir(parents=True, exist_ok=True)
    print("Downloading current formatted/items.txt...")

    request = urllib.request.Request(
        FORMATTED_ITEMS_URL,
        headers={"User-Agent": "AlbionServant nutrition installer"},
    )

    with urllib.request.urlopen(request, timeout=120) as response:
        with cache_path.open("wb") as output:
            shutil.copyfileobj(response, output)

    return cache_path



def download_types_page(cache_path: Path) -> Path:
    cache_path.parent.mkdir(parents=True, exist_ok=True)
    print("Downloading the current bulk item-value table...")

    request = urllib.request.Request(
        TYPES_PAGE_URL,
        headers={
            "User-Agent": "AlbionServant nutrition installer",
            "Accept": "text/html,application/xhtml+xml",
        },
    )

    with urllib.request.urlopen(request, timeout=120) as response:
        data = response.read()

    if b"T4_HEAD_PLATE_SET1" not in data or b"T4_POTION_" not in data:
        raise RuntimeError(
            "The downloaded item-value table is incomplete or has changed."
        )

    cache_path.write_bytes(data)
    return cache_path


def normalize_display_name(value: str) -> str:
    return " ".join(value.strip().lower().split())


def is_excluded_craftable_entry(item_id: str, display_name: str) -> bool:
    normalized_id = item_id.upper()
    normalized_name = normalize_display_name(display_name)

    forbidden_id_parts = (
        "FURNITURE",
        "PLAYERISLAND",
        "VANITY",
        "_SKIN",
        "SKIN_",
        "DECOR",
        "TROPHY",
    )

    forbidden_name_parts = (
        "decorative",
        "wardrobe skin",
        "vanity",
        "furniture",
        "trophy",
    )

    return (
        any(part in normalized_id for part in forbidden_id_parts)
        or any(part in normalized_name for part in forbidden_name_parts)
    )


def extract_item_name_ids(formatted_items: Path) -> dict[str, str]:
    result: dict[str, str] = {}
    line_pattern = re.compile(
        r"^\s*\d+\s*:\s*(\S+)\s*:\s*(.*?)\s*$"
    )

    with formatted_items.open(
        "r",
        encoding="utf-8-sig",
        errors="replace",
    ) as source:
        for line in source:
            match = line_pattern.match(line)
            if not match:
                continue

            item_id = match.group(1).strip()
            display_name = match.group(2).strip()

            # Only Gear, Food and Potions used by the crafting panels.
            # Furniture, decorative capes, island items, skins and trophies
            # are intentionally excluded.
            if (
                    not display_name
                    or "@" in item_id
                    or not is_supported_craftable_id(item_id)
                    or is_excluded_craftable_entry(item_id, display_name)
            ):
                continue

            key = normalize_display_name(display_name)
            current = result.get(key)

            if current is None:
                result[key] = item_id
                continue

            # Prefer ordinary tiered item IDs over vanity/unique aliases.
            if item_id.startswith("T") and not current.startswith("T"):
                result[key] = item_id

    return result

def write_item_name_ids(path: Path, values: dict[str, str]) -> None:
    lines = [
        "# normalized display name\texact unenchanted UniqueName",
        f"# entries={len(values)}",
    ]

    for name in sorted(values):
        lines.append(f"{name}\t{values[name]}")

    write_text(path, "\n".join(lines) + "\n")



class TypesTableParser(HTMLParser):
    def __init__(self) -> None:
        super().__init__(convert_charrefs=True)
        self.rows: list[list[str]] = []
        self._row: list[str] | None = None
        self._cell: list[str] | None = None

    def handle_starttag(self, tag: str, attrs) -> None:
        lowered = tag.lower()

        if lowered == "tr":
            self._row = []
        elif lowered in {"td", "th"} and self._row is not None:
            self._cell = []
        elif lowered == "br" and self._cell is not None:
            self._cell.append(" ")

    def handle_data(self, data: str) -> None:
        if self._cell is not None:
            self._cell.append(data)

    def handle_endtag(self, tag: str) -> None:
        lowered = tag.lower()

        if lowered in {"td", "th"} and self._cell is not None:
            value = " ".join("".join(self._cell).split())

            if self._row is not None:
                self._row.append(value)

            self._cell = None
        elif lowered == "tr" and self._row is not None:
            if self._row:
                self.rows.append(self._row)

            self._row = None
            self._cell = None


def supported_item_ids(formatted_items: Path) -> list[str]:
    result: list[str] = []
    seen: set[str] = set()
    line_pattern = re.compile(
        r"^\s*\d+\s*:\s*(\S+)\s*:\s*(.*?)\s*$"
    )

    with formatted_items.open(
        "r",
        encoding="utf-8-sig",
        errors="replace",
    ) as source:
        for line in source:
            match = line_pattern.match(line)

            if not match:
                continue

            item_id = match.group(1).strip().upper()
            display_name = match.group(2).strip()

            if (
                    not is_supported_craftable_id(item_id)
                    or is_excluded_craftable_entry(item_id, display_name)
            ):
                continue

            if item_id not in seen:
                seen.add(item_id)
                result.append(item_id)

    return result


def parse_item_value(value: str | None) -> Decimal | None:
    if value is None:
        return None

    normalized = value.strip().replace(",", "")

    if not normalized:
        return None

    try:
        parsed = Decimal(normalized)
    except InvalidOperation:
        return None

    if parsed <= 0 or parsed != parsed.to_integral_value():
        return None

    return parsed


def gear_group(item_id: str) -> str | None:
    base = strip_item_enchantment(item_id).upper()
    match = re.match(r"^T[2-8]_([A-Z0-9]+)", base)

    if not match:
        return None

    token = match.group(1)

    if token == "CAPEITEM":
        return "CAPE"

    if token in {
        "HEAD",
        "ARMOR",
        "SHOES",
        "MAIN",
        "2H",
        "OFF",
        "BAG",
        "CAPE",
    }:
        return token

    return None


def item_tier(item_id: str) -> int | None:
    match = re.match(r"^T([2-8])_", item_id.upper())
    return int(match.group(1)) if match else None


def item_enchantment(item_id: str) -> int:
    if "@" not in item_id:
        return 0

    try:
        return max(0, min(4, int(item_id.rsplit("@", 1)[1])))
    except ValueError:
        return 0


def most_common(values: list[Decimal]) -> Decimal | None:
    if not values:
        return None

    counts: dict[Decimal, int] = {}

    for value in values:
        counts[value] = counts.get(value, 0) + 1

    return max(counts, key=lambda value: (counts[value], value))


def special_gear_base_value(group: str, tier: int) -> Decimal | None:
    # Bags and capes are missing from the bulk table because they inherit
    # their values in the game data. Their base values follow exact powers
    # of two; enchantment is applied separately below.
    if group == "CAPE":
        return Decimal(2 ** (tier + 3))

    if group == "BAG":
        return Decimal(2 ** (tier + 4))

    return None


def extract_nutrition(
        types_page: Path,
        formatted_items: Path,
) -> dict[str, Decimal]:
    parser = TypesTableParser()
    parser.feed(
        types_page.read_text(
            encoding="utf-8-sig",
            errors="replace",
        )
    )

    direct_base_values: dict[str, Decimal] = {}
    profile_values: dict[tuple[int, str], list[Decimal]] = {}

    for cells in parser.rows:
        item_index = next(
            (
                index
                for index, cell in enumerate(cells)
                if is_supported_craftable_id(cell)
            ),
            None,
        )

        if item_index is None:
            continue

        item_id = cells[item_index].strip().upper()

        if "@" in item_id:
            continue

        # The bulk table columns are:
        # T4A ID | Unique | Name | Value | Category | Silver | Time | Focus | Return
        value = None

        if item_index + 2 < len(cells):
            value = parse_item_value(cells[item_index + 2])

        if value is None and len(cells) > 3:
            value = parse_item_value(cells[3])

        if value is None:
            continue

        direct_base_values[item_id] = value

        tier = item_tier(item_id)
        group = gear_group(item_id)

        if tier is not None and group is not None:
            profile_values.setdefault((tier, group), []).append(value)

    profiles = {
        key: most_common(values)
        for key, values in profile_values.items()
    }

    all_ids = supported_item_ids(formatted_items)
    nutrition: dict[str, Decimal] = {}
    missing_bases: set[str] = set()

    for exact_id in all_ids:
        base_id = strip_item_enchantment(exact_id).upper()
        base_value = direct_base_values.get(base_id)

        if base_value is None:
            tier = item_tier(base_id)
            group = gear_group(base_id)

            if tier is not None and group is not None:
                base_value = profiles.get((tier, group))

                if base_value is None:
                    base_value = special_gear_base_value(group, tier)

        if base_value is None:
            missing_bases.add(base_id)
            continue

        enchantment = item_enchantment(exact_id)
        item_value = base_value * Decimal(2 ** enchantment)
        nutrition[exact_id] = item_value * NUTRITION_MULTIPLIER

    # Also keep every directly parsed base item, even if an upstream formatted
    # list temporarily omits it.
    for base_id, base_value in direct_base_values.items():
        nutrition.setdefault(
            base_id,
            base_value * NUTRITION_MULTIPLIER,
        )

    if missing_bases:
        examples = ", ".join(sorted(missing_bases)[:20])
        print(
            "WARNING: no item value for "
            f"{len(missing_bases)} base IDs. First: {examples}"
        )

    return nutrition


def format_decimal(value: Decimal) -> str:
    normalized = value.normalize()
    text = format(normalized, "f")
    if "." in text:
        text = text.rstrip("0").rstrip(".")
    return text or "0"


def write_nutrition_properties(
        path: Path,
        nutrition: dict[str, Decimal],
) -> None:
    lines = [
        "# Generated from ao-data/ao-bin-dumps items.xml",
        "# Only Gear, Food and Potions used by AlbionServant are included.",
        "# Decorative/furniture/island items are intentionally excluded.",
        "# nutritionCost = direct crafting nutrition or itemValue * 0.1125",
        f"# entries={len(nutrition)}",
    ]

    for item_id in sorted(nutrition):
        lines.append(f"{item_id}={format_decimal(nutrition[item_id])}")

    write_text(path, "\n".join(lines) + "\n")


def patch_requirements_panel(path: Path) -> bool:
    text = read_text(path)
    original = text

    text = add_import(
        text,
        "import javafx.beans.property.ReadOnlyDoubleProperty;",
    )
    text = add_import(
        text,
        "import javafx.beans.property.ReadOnlyDoubleWrapper;",
    )

    if "private final ReadOnlyDoubleWrapper rrrFraction" not in text:
        pattern = re.compile(
            r"(?m)^(?P<indent>\s*)private\s+final\s+Label\s+rrrLabel\s*=\s*new\s+Label\(\);\s*$"
        )
        match = pattern.search(text)
        if not match:
            raise RuntimeError("RequirementsCalculatorPanel rrrLabel field not found")

        indent = match.group("indent")
        addition = (
            match.group(0)
            + "\n"
            + indent
            + "private final ReadOnlyDoubleWrapper rrrFraction =\n"
            + indent
            + "        new ReadOnlyDoubleWrapper(0.0);"
        )
        text = text[:match.start()] + addition + text[match.end():]

    if "public double getRrrFraction()" not in text:
        getter_pattern = re.compile(
            r"public\s+Label\s+getRrrLabel\(\)\s*\{\s*return\s+rrrLabel;\s*\}"
        )
        match = getter_pattern.search(text)
        if not match:
            raise RuntimeError("RequirementsCalculatorPanel getRrrLabel() not found")

        methods = r'''

    public double getRrrFraction() {
        return rrrFraction.get();
    }

    public ReadOnlyDoubleProperty rrrFractionProperty() {
        return rrrFraction.getReadOnlyProperty();
    }'''
        text = text[:match.end()] + methods + text[match.end():]

    if "rrrFraction.set(rrr);" not in text:
        pattern = re.compile(
            r"(?m)^(?P<indent>\s*)double\s+rrr\s*=\s*lpb\s*/\s*\(100\.0\s*\+\s*lpb\);\s*$"
        )
        match = pattern.search(text)
        if not match:
            raise RuntimeError("RequirementsCalculatorPanel RRR calculation not found")

        indent = match.group("indent")
        replacement = match.group(0) + "\n" + indent + "rrrFraction.set(rrr);"
        text = text[:match.start()] + replacement + text[match.end():]

    if PATCH_MARKER not in text:
        text = text.replace(
            "public class RequirementsCalculatorPanel extends VBox {",
            f"// {PATCH_MARKER}\npublic class RequirementsCalculatorPanel extends VBox {{",
            1,
        )

    if text != original:
        write_text(path, text)
        return True
    return False


def patch_item_render_data(path: Path) -> bool:
    text = read_text(path)
    original = text

    if "public static String getFoodUniqueId(" not in text:
        anchor = "    public static String getFoodImageUrl(String itemName) {"
        position = text.find(anchor)
        if position < 0:
            raise RuntimeError("ItemRenderData getFoodImageUrl() not found")

        method = r'''    public static String getFoodUniqueId(String itemName) {
        if (itemName == null || itemName.isBlank()) {
            return null;
        }

        String cleaned = cleanDisplayName(itemName);

        if (looksLikeUniqueId(cleaned)) {
            return cleaned;
        }

        return FOOD_IDS.get(key(cleaned));
    }

'''
        text = text[:position] + method + text[position:]

    if "public static String getPotionUniqueId(" not in text:
        anchor = "    public static String getPotionImageUrl(String itemName) {"
        position = text.find(anchor)
        if position < 0:
            raise RuntimeError("ItemRenderData getPotionImageUrl() not found")

        method = r'''    public static String getPotionUniqueId(String itemName) {
        if (itemName == null || itemName.isBlank()) {
            return null;
        }

        String cleaned = cleanDisplayName(itemName);

        if (looksLikeUniqueId(cleaned)) {
            return cleaned;
        }

        return POTION_IDS.get(key(cleaned));
    }

'''
        text = text[:position] + method + text[position:]

    if text != original:
        write_text(path, text)
        return True
    return False


def patch_gear_costs(text: str) -> str:
    if "TextField stationFeeField" not in text:
        old_call = (
            "buildTable(itemName, reqPanel, quantity, buy1, buy2, buy3, "
            "sellLocation, sellQuality)"
        )
        new_call = (
            "buildTable(itemName, reqPanel, quantity, buy1, buy2, buy3, "
            "sellLocation, sellQuality, stationFee)"
        )

        if old_call not in text:
            raise RuntimeError("Gear buildTable call with Sell Quality not found")
        text = text.replace(old_call, new_call, 1)

        signature_pattern = re.compile(
            r"(private\s+VBox\s+buildTable\(.*?"
            r"ComboBox<String>\s+sellQuality)(\s*\))",
            re.DOTALL,
        )
        match = signature_pattern.search(text)
        if not match:
            raise RuntimeError("Gear buildTable signature not found")

        replacement = (
            match.group(1)
            + ",\n                TextField stationFeeField\n        )"
        )
        text = text[:match.start()] + replacement + text[match.end():]

    if "List<Boolean> rowMaterialReturnable" not in text:
        anchor = "List<Integer> rowMaterialQuantities = new ArrayList<>();"
        if anchor not in text:
            raise RuntimeError("Gear rowMaterialQuantities list not found")
        text = text.replace(
            anchor,
            anchor
            + "\n                List<Boolean> rowMaterialReturnable = "
            + "new ArrayList<>();",
            1,
        )

        regular_anchor = "rowMaterialQuantities.add(materialQuantity);"
        if regular_anchor not in text:
            raise RuntimeError("Gear material quantity append not found")
        text = text.replace(
            regular_anchor,
            regular_anchor
            + "\n                        rowMaterialReturnable.add(true);",
            1,
        )

        artifact_anchor = "rowMaterialQuantities.add(1);"
        if artifact_anchor in text:
            text = text.replace(
                artifact_anchor,
                artifact_anchor
                + "\n                    rowMaterialReturnable.add(!isAvalonEnergy);",
                1,
            )

    crafted_position = text.find("String craftedItemId")
    if crafted_position < 0:
        raise RuntimeError("Gear craftedItemId block not found")

    if "grossReturnableCostPerCraft" not in text[crafted_position:]:
        text = replace_braced_declaration(
            text,
            "Runnable updateCostCell = () -> {",
            GEAR_COST_LAMBDA,
            crafted_position,
        )

    listener_match = re.search(
        r"(?m)^\s*sellPriceApi\.textProperty\(\)\.addListener\(",
        text[crafted_position:],
    )
    if not listener_match:
        raise RuntimeError("Gear sell price listener anchor not found")
    listener_position = crafted_position + listener_match.start()

    if "stationFeeField.textProperty().addListener" not in text[crafted_position:listener_position]:
        listeners = r'''                stationFeeField.textProperty().addListener(
                        (observable, oldValue, newValue) ->
                                updateCostCell.run()
                );

                reqPanel.rrrFractionProperty().addListener(
                        (observable, oldValue, newValue) ->
                                updateCostCell.run()
                );

'''
        text = text[:listener_position] + listeners + text[listener_position:]

    return text


def patch_food_costs(text: str) -> str:
    call_start = text.find("Runnable updateResults = () -> updateFoodResults(")
    if call_start < 0:
        raise RuntimeError("updateFoodResults call not found")

    call_end = text.find(");", call_start)
    if call_end < 0:
        raise RuntimeError("updateFoodResults call end not found")

    call_block = text[call_start:call_end]
    if re.search(r"recipe,\s*quantity,\s*stationFee,", call_block) is None:
        patched = re.sub(
            r"(recipe,\s*quantity,)",
            r"\1\n                stationFee,",
            call_block,
            count=1,
        )
        text = text[:call_start] + patched + text[call_end:]

    method_start = text.find("private void updateFoodResults(")
    if method_start < 0:
        raise RuntimeError("updateFoodResults method not found")
    method_open = text.find("{", method_start)
    signature = text[method_start:method_open]

    if "TextField stationFeeField" not in signature:
        patched_signature = re.sub(
            r"(TextField\s+quantityField\s*,)",
            r"\1\n            TextField stationFeeField,",
            signature,
            count=1,
        )
        text = text[:method_start] + patched_signature + text[method_open:]

    listener_anchor = (
        "quantity.textProperty().addListener((obs, oldVal, newVal) -> "
        "updateResults.run());"
    )
    listener_position = text.find(listener_anchor, call_start)
    if listener_position < 0:
        raise RuntimeError("Food quantity listener not found")

    if "stationFee.textProperty().addListener" not in text[listener_position:method_start]:
        insert_at = listener_position + len(listener_anchor)
        addition = (
            "\n        stationFee.textProperty().addListener("
            "(obs, oldVal, newVal) -> updateResults.run());"
        )
        text = text[:insert_at] + addition + text[insert_at:]

    best_sell_match = re.search(
        r"(?m)^\s*double\s+bestSell\s*=\s*bestSellPrice\(sellPrices\);\s*$",
        text[method_start:],
    )
    if not best_sell_match:
        raise RuntimeError("Food bestSell calculation not found")
    best_sell_position = method_start + best_sell_match.start()
    best_sell_end = method_start + best_sell_match.end()

    if "double feePer100" not in text[best_sell_position:method_start + 5000]:
        addition = r'''
        double feePer100 =
                com.albionservant.data.CraftingNutritionData
                        .parseNonNegative(stationFeeField.getText());'''
        insert_at = best_sell_end
        text = text[:insert_at] + addition + text[insert_at:]

    old_total = "            double totalCost = costPerItem * netCostMultiplier * quantity;"
    method_end = find_matching_brace(text, text.find("{", method_start))
    method_text = text[method_start:method_end]

    if "stationFeeForDisplayName" not in method_text:
        total_position = text.find(old_total, method_start, method_end)
        if total_position < 0:
            raise RuntimeError("Food totalCost calculation not found")

        new_total = r'''            double stationCost =
                    com.albionservant.data.CraftingNutritionData
                            .stationFeeForDisplayName(
                                    recipe.name(),
                                    row.enchant(),
                                    feePer100,
                                    quantity
                            );

            if (!Double.isFinite(stationCost)) {
                row.cost().setText("-");
                row.cost().setTooltip(new Tooltip(
                        "Missing nutritionCost for "
                                + recipe.name()
                                + " enchant "
                                + row.enchant()
                ));
                row.profit().setText("-");
                row.spf().setText("-");
                continue;
            }

            double materialCostAfterReturns =
                    costPerItem * netCostMultiplier * quantity;
            double totalCost = materialCostAfterReturns + stationCost;
            row.cost().setTooltip(new Tooltip(
                    "Materials after RRR: "
                            + formatSilver(materialCostAfterReturns)
                            + "\nRRR: "
                            + String.format(
                                    java.util.Locale.US,
                                    "%.2f%%",
                                    rrr * 100.0
                            )
                            + "\nStation fee: "
                            + formatSilver(stationCost)
            ));'''
        text = text[:total_position] + new_total + text[total_position + len(old_total):]

    return text


def patch_potion_costs(text: str) -> str:
    call_start = text.find("Runnable updateResults = () -> updatePotionResults(")
    if call_start < 0:
        raise RuntimeError("updatePotionResults call not found")

    call_end = text.find(");", call_start)
    if call_end < 0:
        raise RuntimeError("updatePotionResults call end not found")

    call_block = text[call_start:call_end]
    if re.search(r"recipe,\s*quantity,\s*stationFee,", call_block) is None:
        patched = re.sub(
            r"(recipe,\s*quantity,)",
            r"\1\n                stationFee,",
            call_block,
            count=1,
        )
        text = text[:call_start] + patched + text[call_end:]

    method_start = text.find("private void updatePotionResults(")
    if method_start < 0:
        raise RuntimeError("updatePotionResults method not found")
    method_open = text.find("{", method_start)
    signature = text[method_start:method_open]

    if "TextField stationFeeField" not in signature:
        patched_signature = re.sub(
            r"(TextField\s+quantityField\s*,)",
            r"\1\n            TextField stationFeeField,",
            signature,
            count=1,
        )
        text = text[:method_start] + patched_signature + text[method_open:]

    listener_anchor = (
        "quantity.textProperty().addListener((obs, oldVal, newVal) -> "
        "updateResults.run());"
    )
    listener_position = text.find(listener_anchor, call_start)
    if listener_position < 0:
        raise RuntimeError("Potion quantity listener not found")

    if "stationFee.textProperty().addListener" not in text[listener_position:method_start]:
        insert_at = listener_position + len(listener_anchor)
        addition = (
            "\n        stationFee.textProperty().addListener("
            "(obs, oldVal, newVal) -> updateResults.run());"
        )
        text = text[:insert_at] + addition + text[insert_at:]

    best_sell_match = re.search(
        r"(?m)^\s*double\s+bestSell\s*=\s*bestSellPrice\(sellPrices\);\s*$",
        text[method_start:],
    )
    if not best_sell_match:
        raise RuntimeError("Potion bestSell calculation not found")
    best_sell_position = method_start + best_sell_match.start()
    best_sell_end = method_start + best_sell_match.end()

    if "double feePer100" not in text[best_sell_position:method_start + 5000]:
        addition = r'''
        double feePer100 =
                com.albionservant.data.CraftingNutritionData
                        .parseNonNegative(stationFeeField.getText());'''
        insert_at = best_sell_end
        text = text[:insert_at] + addition + text[insert_at:]

    old_total = "            double totalCost = costPerItem * netCostMultiplier * quantity;"
    method_end = find_matching_brace(text, text.find("{", method_start))
    method_text = text[method_start:method_end]

    if "stationFeeForDisplayName" not in method_text:
        total_position = text.find(old_total, method_start, method_end)
        if total_position < 0:
            raise RuntimeError("Potion totalCost calculation not found")

        new_total = r'''            double stationCost =
                    com.albionservant.data.CraftingNutritionData
                            .stationFeeForDisplayName(
                                    recipe.name(),
                                    row.enchant(),
                                    feePer100,
                                    quantity
                            );

            if (!Double.isFinite(stationCost)) {
                row.cost().setText("-");
                row.cost().setTooltip(new Tooltip(
                        "Missing nutritionCost for "
                                + recipe.name()
                                + " enchant "
                                + row.enchant()
                ));
                row.profit().setText("-");
                row.spf().setText("-");
                continue;
            }

            double materialCostAfterReturns =
                    costPerItem * netCostMultiplier * quantity;
            double totalCost = materialCostAfterReturns + stationCost;
            row.cost().setTooltip(new Tooltip(
                    "Materials after RRR: "
                            + formatSilver(materialCostAfterReturns)
                            + "\nRRR: "
                            + String.format(
                                    java.util.Locale.US,
                                    "%.2f%%",
                                    rrr * 100.0
                            )
                            + "\nStation fee: "
                            + formatSilver(stationCost)
            ));'''
        text = text[:total_position] + new_total + text[total_position + len(old_total):]

    return text


def patch_craft_panel(path: Path) -> bool:
    text = read_text(path)
    original = text

    text = add_import(
        text,
        "import com.albionservant.data.CraftingNutritionData;",
    )

    text = text.replace(
        'new Label("Station Fee:")',
        'new Label("Fee / 100 Nutrition:")',
    )

    text = patch_gear_costs(text)
    text = patch_food_costs(text)
    text = patch_potion_costs(text)

    if PATCH_MARKER not in text:
        class_pattern = re.compile(
            r"public\s+class\s+CraftPanel\s+extends\s+VBox\s*\{"
        )
        match = class_pattern.search(text)
        if not match:
            raise RuntimeError("CraftPanel class declaration not found")
        text = text[:match.start()] + f"// {PATCH_MARKER}\n" + text[match.start():]

    if text != original:
        write_text(path, text)
        return True
    return False


def main() -> int:
    parser = argparse.ArgumentParser(
        description=(
            "Adds item nutrition station fees and RRR-adjusted costs to "
            "Gear/Food/Potion crafting. Maven is not executed."
        )
    )
    parser.add_argument(
        "project_root",
        nargs="?",
        default=".",
        help="AlbionServant directory containing pom.xml",
    )
    parser.add_argument(
        "--types-page",
        help="Use a local saved tools4albion types.php HTML page",
    )
    parser.add_argument(
        "--formatted-items",
        help="Use a local formatted/items.txt instead of downloading it",
    )
    parser.add_argument(
        "--refresh-data",
        action="store_true",
        help="Download the item-value table and formatted item list again",
    )
    args = parser.parse_args()

    project_root = Path(args.project_root).expanduser().resolve()
    if not (project_root / "pom.xml").is_file():
        print("ERROR: pom.xml not found in:", project_root)
        return 1

    java_root = project_root / "src" / "main" / "java" / "com" / "albionservant"
    resources_root = project_root / "src" / "main" / "resources"

    craft_panel = java_root / "gui" / "CraftPanel.java"
    requirements_panel = java_root / "gui" / "RequirementsCalculatorPanel.java"
    nutrition_java = java_root / "data" / "CraftingNutritionData.java"
    nutrition_properties = (
        resources_root / "data" / "crafting-nutrition.properties"
    )
    item_name_ids = (
        resources_root / "data" / "crafting-item-name-ids.tsv"
    )

    required = [craft_panel, requirements_panel]
    missing = [path for path in required if not path.is_file()]
    if missing:
        print("ERROR: required source files are missing:")
        for path in missing:
            print(" -", path)
        return 1

    cache_dir = project_root / ".build" / "albion-nutrition-data"
    types_cache_path = cache_dir / "tools4albion-types.html"
    formatted_cache_path = cache_dir / "formatted-items.txt"

    try:
        if args.types_page:
            types_page = Path(args.types_page).expanduser().resolve()
            if not types_page.is_file():
                raise RuntimeError(f"types page not found: {types_page}")
        elif types_cache_path.is_file() and not args.refresh_data:
            types_page = types_cache_path
            print("Using cached item-value table:", types_page)
        else:
            types_page = download_types_page(types_cache_path)

        if args.formatted_items:
            formatted_items = Path(args.formatted_items).expanduser().resolve()
            if not formatted_items.is_file():
                raise RuntimeError(
                    f"formatted/items.txt not found: {formatted_items}"
                )
        elif formatted_cache_path.is_file() and not args.refresh_data:
            formatted_items = formatted_cache_path
            print("Using cached formatted items:", formatted_items)
        else:
            formatted_items = download_formatted_items(
                formatted_cache_path
            )

        print(
            "Reading item values for Gear, Food and Potions only "
            "(decorations are excluded by the ID allow-list)..."
        )
        nutrition = extract_nutrition(types_page, formatted_items)
        print("Reading filtered display-name mapping (no decorations)...")
        names_to_ids = extract_item_name_ids(formatted_items)

        if len(nutrition) < 500:
            raise RuntimeError(
                "Too few filtered nutrition entries were generated "
                f"({len(nutrition)}). The bulk item-value table may have changed."
            )

        category_patterns = {
            "gear": CRAFTABLE_ID_PATTERNS[0],
            "food": CRAFTABLE_ID_PATTERNS[1],
            "potions": CRAFTABLE_ID_PATTERNS[2],
        }

        validated_categories: dict[str, list[str]] = {}

        for category, pattern in category_patterns.items():
            matches = sorted(
                item_id
                for item_id in nutrition
                if pattern.match(strip_item_enchantment(item_id).upper())
            )
            validated_categories[category] = matches
            print(f"Extracted {category}: {len(matches)} entries")

        missing_categories = [
            category
            for category, matches in validated_categories.items()
            if not matches
        ]

        if missing_categories:
            raise RuntimeError(
                "Nutrition extraction validation failed for categories: "
                + ", ".join(missing_categories)
                + ". Decorative and furniture IDs are excluded by the allow-list. "
                + "The cached item-value table may be incomplete or changed."
            )

        unrelated_ids = [
            item_id
            for item_id in nutrition
            if not is_supported_craftable_id(item_id)
        ]

        if unrelated_ids:
            raise RuntimeError(
                "Internal filter error: unrelated item IDs were extracted."
            )

        expected_names = {
            "chicken omelette",
            "minor gigantify potion",
            "major cleansing potion",
        }
        missing_names = sorted(expected_names.difference(names_to_ids))
        if missing_names:
            print(
                "WARNING: display-name mapping does not contain: "
                + ", ".join(missing_names)
            )

        timestamp = datetime.now().strftime("%Y%m%d-%H%M%S")
        backup_root = (
            project_root
            / ".albionservant-backups"
            / f"nutrition-rrr-v5-{timestamp}"
        )

        for path in [
            craft_panel,
            requirements_panel,
            nutrition_java,
            nutrition_properties,
            item_name_ids,
        ]:
            backup_file(path, backup_root, project_root)

        write_text(nutrition_java, NUTRITION_JAVA)
        write_nutrition_properties(nutrition_properties, nutrition)
        write_item_name_ids(item_name_ids, names_to_ids)
        requirements_changed = patch_requirements_panel(requirements_panel)
        craft_changed = patch_craft_panel(craft_panel)

        print()
        print("GOTOWE")
        print("Nutrition entries:", len(nutrition))
        print("Display-name mappings:", len(names_to_ids))
        print("Validated nutrition categories:")
        for category, examples in validated_categories.items():
            preview = ", ".join(examples[:8])
            suffix = " ..." if len(examples) > 8 else ""
            print(
                f"  {category}: {len(examples)} entries "
                f"({preview}{suffix})"
            )
        print("CraftPanel changed:", craft_changed)
        print("RequirementsCalculatorPanel changed:", requirements_changed)
        print("Nutrition data:", nutrition_properties)
        print("Name map:", item_name_ids)
        print("Backup:", backup_root)
        print()
        print("Applied cost formula:")
        print("  materialsAfterRRR = returnableMaterials * (1 - RRR)")
        print("  stationFee = nutritionCost * FeePer100 / 100 * quantity")
        print("  totalCost = materialsAfterRRR + nonReturnable + stationFee")
        print()
        print("Maven was NOT executed.")
        return 0

    except Exception as exception:
        print("ERROR:", exception)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
