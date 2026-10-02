package net.alshanex.enchanters_script.client;

import net.alshanex.enchanters_script.primer.Primer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * The world's 26 cipher symbols, each with its letter once the player has learned it.
 */
public class PrimerScreen extends Screen {
     // 26 symbols in a grid of 7 per row: 7, 7, 7 and 5
    private static final int COLUMNS = 7;
    private static final int CELL_WIDTH = 22;
    private static final int CELL_HEIGHT = 30;
    private static final int GRID_TOP = 22;
    private static final int BOX_SIZE = 18;

    private final String page;

    public PrimerScreen(String page) {
        super(Component.translatable("item.enchanters_script.primer"));
        this.page = page;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Draws the dimmed background behind the panel
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int left = (this.width - GuiTextures.PANEL_WIDTH) / 2;
        int top = (this.height - GuiTextures.PANEL_HEIGHT) / 2;
        guiGraphics.blit(GuiTextures.PRIMER_PANEL, left, top, 0, 0, GuiTextures.PANEL_WIDTH, GuiTextures.PANEL_HEIGHT);

        guiGraphics.drawString(this.font, this.title, left + 8, top + 6, GuiColors.get(GuiColors.LABEL), false);

        int known = 0;
        for (int i = 0; i < this.page.length(); i++) {
            char letter = this.page.charAt(i);
            if (letter != Primer.UNKNOWN) {
                known++;
            }
            renderEntry(guiGraphics, left, top, i, letter);
        }

        Component progress = Component.translatable("gui.enchanters_script.primer_progress", known, this.page.length());
        guiGraphics.drawString(this.font, progress, left + (GuiTextures.PANEL_WIDTH - this.font.width(progress)) / 2,
                top + 148, GuiColors.get(GuiColors.LABEL), false);
    }

    private void renderEntry(GuiGraphics guiGraphics, int left, int top, int index, char letter) {
        int row = index / COLUMNS;
        int column = index % COLUMNS;
        // Center rows with fewer than 7 entries, like the last one
        int inRow = Math.min(COLUMNS, this.page.length() - row * COLUMNS);
        int rowLeft = left + (GuiTextures.PANEL_WIDTH - inRow * CELL_WIDTH) / 2;

        int x = rowLeft + column * CELL_WIDTH + (CELL_WIDTH - BOX_SIZE) / 2;
        int y = top + GRID_TOP + row * CELL_HEIGHT;

        // The symbol, in a slot box
        guiGraphics.blitSprite(GuiTextures.SLOT, x, y, BOX_SIZE, BOX_SIZE);
        Component glyph = GuiTextures.galactic(String.valueOf((char) ('A' + index)));
        guiGraphics.drawString(this.font, glyph, x + (BOX_SIZE - this.font.width(glyph) + 1) / 2, y + 5,
                GuiColors.get(GuiColors.SLOT_TEXT), true);

        // Its letter below, or a question mark
        String text = String.valueOf(letter);
        int color = GuiColors.get(letter == Primer.UNKNOWN ? GuiColors.PRIMER_UNKNOWN : GuiColors.PRIMER_KNOWN);
        guiGraphics.drawString(this.font, text, x + (BOX_SIZE - this.font.width(text) + 1) / 2, y + BOX_SIZE + 2, color, false);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // The inventory key closes it too, like other screens
        if (this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
