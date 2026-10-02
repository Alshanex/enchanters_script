package net.alshanex.enchanters_script.client;

import java.util.Map;

/**
 * The colors the mod's screens use, loaded from assets/enchanters_script/gui/colors.json
 * so resource packs can change them. Any color a pack leaves out keeps its default.
 */
public final class GuiColors {
    public static final String LABEL = "label";
    public static final String OFFER_NAME = "offer_name";
    public static final String OFFER_NAME_HOVERED = "offer_name_hovered";
    public static final String OFFER_NAME_DISABLED = "offer_name_disabled";
    public static final String OFFER_COST = "offer_cost";
    public static final String OFFER_COST_DISABLED = "offer_cost_disabled";
    public static final String BUTTON_TEXT = "button_text";
    public static final String BUTTON_TEXT_DISABLED = "button_text_disabled";
    public static final String KEY_TEXT_SELECTED = "key_text_selected";
    public static final String SLOT_TEXT = "slot_text";
    public static final String SLOT_HOVER = "slot_hover";
    public static final String CARD_TEXT = "card_text";
    public static final String CARD_TEXT_SELECTED = "card_text_selected";
    public static final String CARD_TEXT_DISABLED = "card_text_disabled";
    public static final String PRIMER_KNOWN = "primer_known";
    public static final String PRIMER_UNKNOWN = "primer_unknown";

    // ARGB values, the same as the colors.json shipped with the mod
    static final Map<String, Integer> DEFAULTS = Map.ofEntries(
            Map.entry(LABEL, 0xFF404040),
            Map.entry(OFFER_NAME, 0xFF685E4A),
            Map.entry(OFFER_NAME_HOVERED, 0xFFFFFF80),
            Map.entry(OFFER_NAME_DISABLED, 0xFF342F25),
            Map.entry(OFFER_COST, 0xFF80FF20),
            Map.entry(OFFER_COST_DISABLED, 0xFF407F10),
            Map.entry(BUTTON_TEXT, 0xFFFFFFFF),
            Map.entry(BUTTON_TEXT_DISABLED, 0xFFA0A0A0),
            Map.entry(KEY_TEXT_SELECTED, 0xFFFFFF80),
            Map.entry(SLOT_TEXT, 0xFFFFFFFF),
            Map.entry(SLOT_HOVER, 0x60FFFFFF),
            Map.entry(CARD_TEXT, 0xFFFFFFFF),
            Map.entry(CARD_TEXT_SELECTED, 0xFFFFFF80),
            Map.entry(CARD_TEXT_DISABLED, 0xFFA0A0A0),
            Map.entry(PRIMER_KNOWN, 0xFF404040),
            Map.entry(PRIMER_UNKNOWN, 0xFF8B8B8B)
    );

    private static volatile Map<String, Integer> colors = DEFAULTS;

    private GuiColors() {
    }

    public static int get(String key) {
        Integer color = colors.get(key);
        return color != null ? color : DEFAULTS.getOrDefault(key, 0xFFFFFFFF);
    }

    static boolean isKnown(String key) {
        return DEFAULTS.containsKey(key);
    }

    static void replace(Map<String, Integer> loaded) {
        colors = Map.copyOf(loaded);
    }

    /**
     * Reads "#RRGGBB" as opaque, or "#AARRGGBB" with its own alpha. Returns null if invalid.
     */
    static Integer parse(String value) {
        if (!value.startsWith("#")) {
            return null;
        }
        String hex = value.substring(1);
        try {
            if (hex.length() == 6) {
                return 0xFF000000 | Integer.parseInt(hex, 16);
            }
            if (hex.length() == 8) {
                // Long first, since values like FF404040 don't fit in a signed int when parsed directly
                return (int) Long.parseLong(hex, 16);
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }
}
