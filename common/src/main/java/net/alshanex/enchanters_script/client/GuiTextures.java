package net.alshanex.enchanters_script.client;

import net.alshanex.enchanters_script.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

/**
 * Every texture, sprite and font the mod's screens draw with.
 */
public final class GuiTextures {
    public static final int PANEL_WIDTH = 176;
    public static final int PANEL_HEIGHT = 166;

    // Full panels: 176x166 in a 256x256 file
    public static final ResourceLocation WRITING_PANEL = id("textures/gui/writing.png");
    public static final ResourceLocation BONUS_PANEL = id("textures/gui/bonus.png");
    public static final ResourceLocation PRIMER_PANEL = id("textures/gui/primer.png");

    // Sprites from textures/gui/sprites
    public static final ResourceLocation SLOT = id("slot");
    public static final ResourceLocation BUTTON = id("button");
    public static final ResourceLocation BUTTON_HIGHLIGHTED = id("button_highlighted");
    public static final ResourceLocation BUTTON_DISABLED = id("button_disabled");
    public static final ResourceLocation TIMER_BACKGROUND = id("writing/timer_background");
    public static final ResourceLocation TIMER_PROGRESS = id("writing/timer_progress");

    // Own font, which by default refers to vanilla's Galactic font
    private static final Style GALACTIC = Style.EMPTY.withFont(id("galactic"));

    private GuiTextures() {
    }

    /**
     * The button sprite for a state: disabled, highlighted, or normal.
     */
    public static ResourceLocation button(boolean enabled, boolean highlighted) {
        return !enabled ? BUTTON_DISABLED : highlighted ? BUTTON_HIGHLIGHTED : BUTTON;
    }

    public static Component galactic(String symbols) {
        // Vanilla's Galactic font only has lowercase glyphs
        return Component.literal(symbols.toLowerCase(Locale.ROOT)).withStyle(GALACTIC);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, path);
    }
}
