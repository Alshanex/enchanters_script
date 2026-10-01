package net.alshanex.enchanters_script.client;

import net.alshanex.enchanters_script.primer.Primer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

/**
 * The world's 26 cipher symbols, each with its letter once the player has learned it.
 */
public class PrimerScreen extends Screen {
    // Vanilla's table panel as the frame, painted over inside
    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/enchanting_table.png");
    private static final Style GALACTIC = Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("alt"));

    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 166;

    // 26 symbols in a grid of 7 per row: 7, 7, 7 and 5
    private static final int COLUMNS = 7;
    private static final int CELL_WIDTH = 22;
    private static final int CELL_HEIGHT = 30;
    private static final int GRID_TOP = 22;
    private static final int BOX_SIZE = 18;

    private static final int PANEL_COLOR = 0xFFC6C6C6;
    private static final int LABEL_COLOR = 0x404040;
    private static final int UNKNOWN_COLOR = 0x8B8B8B;

    private final String page;

    public PrimerScreen(String page) {
        super(Component.translatable("item.enchanters_script.primer"));
        this.page = page;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Draws the dimmed background behind the panel
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int left = (this.width - IMAGE_WIDTH) / 2;
        int top = (this.height - IMAGE_HEIGHT) / 2;
        guiGraphics.blit(TEXTURE, left, top, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        guiGraphics.fill(left + 7, top + 7, left + 169, top + 159, PANEL_COLOR);

        guiGraphics.drawString(this.font, this.title, left + 8, top + 6, LABEL_COLOR, false);

        int known = 0;
        for (int i = 0; i < this.page.length(); i++) {
            char letter = this.page.charAt(i);
            if (letter != Primer.UNKNOWN) {
                known++;
            }
            renderEntry(guiGraphics, left, top, i, letter);
        }

        Component progress = Component.translatable("gui.enchanters_script.primer_progress", known, this.page.length());
        guiGraphics.drawString(this.font, progress, left + (IMAGE_WIDTH - this.font.width(progress)) / 2,
                top + 148, LABEL_COLOR, false);
    }

    private void renderEntry(GuiGraphics guiGraphics, int left, int top, int index, char letter) {
        int row = index / COLUMNS;
        int column = index % COLUMNS;
        // Center rows with fewer than 7 entries, like the last one
        int inRow = Math.min(COLUMNS, this.page.length() - row * COLUMNS);
        int rowLeft = left + (IMAGE_WIDTH - inRow * CELL_WIDTH) / 2;

        int x = rowLeft + column * CELL_WIDTH + (CELL_WIDTH - BOX_SIZE) / 2;
        int y = top + GRID_TOP + row * CELL_HEIGHT;

        // The symbol, in a slot-style box
        renderBox(guiGraphics, x, y);
        char symbol = (char) ('A' + index);
        Component glyph = Component.literal(String.valueOf(symbol).toLowerCase(Locale.ROOT)).withStyle(GALACTIC);
        guiGraphics.drawString(this.font, glyph, x + (BOX_SIZE - this.font.width(glyph) + 1) / 2, y + 5, 0xFFFFFF, true);

        // Its letter below, or a question mark
        String text = String.valueOf(letter);
        int color = letter == Primer.UNKNOWN ? UNKNOWN_COLOR : LABEL_COLOR;
        guiGraphics.drawString(this.font, text, x + (BOX_SIZE - this.font.width(text) + 1) / 2, y + BOX_SIZE + 2, color, false);
    }

    private static void renderBox(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + BOX_SIZE, y + BOX_SIZE, 0xFF8B8B8B);
        guiGraphics.fill(x, y, x + BOX_SIZE - 1, y + 1, 0xFF373737);
        guiGraphics.fill(x, y, x + 1, y + BOX_SIZE - 1, 0xFF373737);
        guiGraphics.fill(x + 1, y + BOX_SIZE - 1, x + BOX_SIZE, y + BOX_SIZE, 0xFFFFFFFF);
        guiGraphics.fill(x + BOX_SIZE - 1, y + 1, x + BOX_SIZE, y + BOX_SIZE, 0xFFFFFFFF);
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
