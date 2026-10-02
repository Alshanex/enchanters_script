package net.alshanex.enchanters_script.client;

import net.alshanex.enchanters_script.minigame.SlotInput;
import net.alshanex.enchanters_script.minigame.WritingView;
import net.alshanex.enchanters_script.network.WritingActionPayload;
import net.alshanex.enchanters_script.primer.Primer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * The writing page: timer, name slots, keyboard and done button.
 * Knows nothing about tables, so ciphered books can reuse it; moves are reported through the sender.
 * All art comes from GuiTextures and GuiColors, so resource packs can restyle it.
 */
public class WritingPage {

    @FunctionalInterface
    public interface ActionSender {
        void send(int action, int slot, int key);
    }

    // Layout, relative to the 176x166 panel
    private static final int CENTER_X = 88;

    private static final int TIMER_X = 8;
    private static final int TIMER_Y = 9;
    private static final int TIMER_WIDTH = 160;
    private static final int TIMER_HEIGHT = 4;

    // The area between the timer and the done button, where slots and keys are centered
    private static final int AREA_TOP = 19;
    private static final int AREA_BOTTOM = 136;
    private static final int SECTION_GAP = 10;

    private static final int LARGE_SLOT = 13;
    private static final int LARGE_SLOT_PITCH = 14;
    private static final int LARGE_SLOTS_PER_ROW = 11;
    private static final int MAX_LARGE_SLOT_ROWS = 3;
    private static final int SMALL_SLOT = 10;
    private static final int SMALL_SLOT_PITCH = 11;
    private static final int SMALL_SLOTS_PER_ROW = 14;

    private static final int LARGE_KEY = 16;
    private static final int LARGE_KEY_PITCH = 18;
    private static final int LARGE_KEYS_PER_ROW = 9;
    private static final int SMALL_KEY = 12;
    private static final int SMALL_KEY_PITCH = 14;
    private static final int SMALL_KEYS_PER_ROW = 11;

    private static final int DONE_X = 64;
    private static final int DONE_Y = 142;
    private static final int DONE_WIDTH = 48;
    private static final int DONE_HEIGHT = 14;

    // How long the letters take to fade at the end of the reveal
    private static final float FADE_TICKS = 10f;
    // No tile selected
    private static final int NO_TILE = -1;

    private final Font font;
    private final WritingView view;
    private final SlotInput input;
    private final ActionSender sender;
    private final long startTime;

    // Positions relative to the panel, computed once
    private final int slotSize;
    private final int[] slotX;
    private final int[] slotY;
    private final int keySize;
    private final int[] keyX;
    private final int[] keyY;

    private int selectedTile = NO_TILE;
    private boolean finished;

    public WritingPage(Font font, WritingView view, long startTime, ActionSender sender) {
        this.font = font;
        this.view = view;
        this.startTime = startTime;
        this.sender = sender;
        // The encoded name has its spaces in the same places as the real one
        this.input = new SlotInput(view.tiles(), view.reveal());

        // Slots: large unless they would need more than 3 rows
        int slots = this.input.slotCount();
        this.slotX = new int[slots];
        this.slotY = new int[slots];
        int slotRows = layoutSlots(view.reveal(), LARGE_SLOTS_PER_ROW, LARGE_SLOT_PITCH, LARGE_SLOT);
        int slotPitch = LARGE_SLOT_PITCH;
        int size = LARGE_SLOT;
        if (slotRows > MAX_LARGE_SLOT_ROWS) {
            slotRows = layoutSlots(view.reveal(), SMALL_SLOTS_PER_ROW, SMALL_SLOT_PITCH, SMALL_SLOT);
            slotPitch = SMALL_SLOT_PITCH;
            size = SMALL_SLOT;
        }
        this.slotSize = size;
        int slotsHeight = sectionHeight(slotRows, slotPitch, size);

        // Keys: large if both sections still fit between the timer and the done button
        int keys = this.input.tileCount();
        int keysSpace = AREA_BOTTOM - AREA_TOP - slotsHeight - SECTION_GAP;
        int largeKeyRows = ceilDiv(keys, LARGE_KEYS_PER_ROW);
        boolean largeKeys = sectionHeight(largeKeyRows, LARGE_KEY_PITCH, LARGE_KEY) <= keysSpace;
        this.keySize = largeKeys ? LARGE_KEY : SMALL_KEY;
        int keyPitch = largeKeys ? LARGE_KEY_PITCH : SMALL_KEY_PITCH;
        this.keyX = new int[keys];
        this.keyY = new int[keys];
        int keyRows = layoutKeys(keys, largeKeys ? LARGE_KEYS_PER_ROW : SMALL_KEYS_PER_ROW, keyPitch);
        int keysHeight = sectionHeight(keyRows, keyPitch, this.keySize);

        // Center both sections together; the layouts above placed them starting at y = 0
        int contentHeight = slotsHeight + SECTION_GAP + keysHeight;
        int top = AREA_TOP + (AREA_BOTTOM - AREA_TOP - contentHeight) / 2;
        shift(this.slotY, top);
        shift(this.keyY, top + slotsHeight + SECTION_GAP);
    }

    // Layout

    /**
     * Places the slots in rows that break between words. A space between two words on the
     * same row takes one slot's width as a gap; a space at a row break takes no room.
     * Rows start at y = 0; the constructor moves them into place. Returns the number of rows.
     */
    private int layoutSlots(String template, int perRow, int pitch, int size) {
        List<List<Integer>> rows = new ArrayList<>();
        List<Integer> row = new ArrayList<>();

        int i = 0;
        while (i < template.length()) {
            if (template.charAt(i) == ' ') {
                i++;
                continue;
            }

            // Collect one word's slot indices
            int start = i;
            List<Integer> word = new ArrayList<>();
            while (i < template.length() && template.charAt(i) != ' ') {
                word.add(i);
                i++;
            }

            if (!row.isEmpty() && row.size() + 1 + word.size() <= perRow) {
                // Fits on this row after a gap: the space slot before the word holds the gap
                row.add(start - 1);
                row.addAll(word);
            } else {
                if (!row.isEmpty()) {
                    rows.add(row);
                    row = new ArrayList<>();
                }
                // A word longer than a whole row is split across rows
                for (int index : word) {
                    if (row.size() == perRow) {
                        rows.add(row);
                        row = new ArrayList<>();
                    }
                    row.add(index);
                }
            }
        }
        if (!row.isEmpty()) {
            rows.add(row);
        }

        // Center each row
        for (int r = 0; r < rows.size(); r++) {
            List<Integer> current = rows.get(r);
            int rowWidth = current.size() * pitch - (pitch - size);
            int startX = CENTER_X - rowWidth / 2;
            for (int c = 0; c < current.size(); c++) {
                this.slotX[current.get(c)] = startX + c * pitch;
                this.slotY[current.get(c)] = r * pitch;
            }
        }
        return rows.size();
    }

    /**
     * Splits the keys into as few rows as needed, as evenly as possible, each row centered.
     * Rows start at y = 0; the constructor moves them into place. Returns the number of rows.
     */
    private int layoutKeys(int count, int perRow, int pitch) {
        if (count == 0) {
            return 0;
        }
        int rows = ceilDiv(count, perRow);
        int base = count / rows;
        int extra = count % rows;

        int index = 0;
        for (int row = 0; row < rows; row++) {
            int inRow = base + (row < extra ? 1 : 0);
            int rowWidth = inRow * pitch - (pitch - this.keySize);
            int startX = CENTER_X - rowWidth / 2;
            for (int i = 0; i < inRow; i++) {
                this.keyX[index] = startX + i * pitch;
                this.keyY[index] = row * pitch;
                index++;
            }
        }
        return rows;
    }

    /**
     * The height of a block of rows: every row but the last includes the gap below it.
     */
    private static int sectionHeight(int rows, int pitch, int size) {
        return rows == 0 ? 0 : rows * pitch - (pitch - size);
    }

    private static void shift(int[] values, int offset) {
        for (int i = 0; i < values.length; i++) {
            values[i] += offset;
        }
    }

    private static int ceilDiv(int a, int b) {
        return (a + b - 1) / b;
    }

    // Time

    private float elapsed(float partialTick) {
        return Minecraft.getInstance().level.getGameTime() - this.startTime + partialTick;
    }

    private boolean acceptsInput(float elapsed) {
        return !this.finished
                && elapsed >= this.view.revealTicks()
                && elapsed <= this.view.revealTicks() + this.view.writingTicks();
    }

    // Drawing

    public void render(GuiGraphics guiGraphics, int left, int top, int mouseX, int mouseY, float partialTick) {
        float elapsed = elapsed(partialTick);
        boolean active = acceptsInput(elapsed);

        guiGraphics.blit(GuiTextures.WRITING_PANEL, left, top, 0, 0, GuiTextures.PANEL_WIDTH, GuiTextures.PANEL_HEIGHT);
        renderTimer(guiGraphics, left, top, elapsed);
        renderSlots(guiGraphics, left, top, mouseX, mouseY, elapsed, active);
        renderKeys(guiGraphics, left, top, mouseX, mouseY, active);
        renderDone(guiGraphics, left, top, mouseX, mouseY, active);
    }

    private void renderTimer(GuiGraphics guiGraphics, int left, int top, float elapsed) {
        int x = left + TIMER_X;
        int y = top + TIMER_Y;
        guiGraphics.blitSprite(GuiTextures.TIMER_BACKGROUND, x, y, TIMER_WIDTH, TIMER_HEIGHT);

        // Full during the reveal, then empties over the writing time
        float writingElapsed = elapsed - this.view.revealTicks();
        float remaining = 1f - Math.max(0f, writingElapsed) / this.view.writingTicks();
        int width = Math.round(TIMER_WIDTH * Math.max(0f, Math.min(1f, remaining)));
        if (width > 0) {
            // Draws the left part of the sprite instead of stretching it, so patterns empty naturally
            guiGraphics.blitSprite(GuiTextures.TIMER_PROGRESS, TIMER_WIDTH, TIMER_HEIGHT, 0, 0, x, y, width, TIMER_HEIGHT);
        }
    }

    private void renderSlots(GuiGraphics guiGraphics, int left, int top, int mouseX, int mouseY,
                             float elapsed, boolean active) {
        boolean revealing = elapsed < this.view.revealTicks();
        // Fully visible, then fades during the last FADE_TICKS of the reveal
        float alpha = Math.max(0f, Math.min(1f, (this.view.revealTicks() - elapsed) / FADE_TICKS));
        int alphaByte = Math.round(alpha * 255);
        int textColor = GuiColors.get(GuiColors.SLOT_TEXT);

        for (int slot = 0; slot < this.input.slotCount(); slot++) {
            // Spaces are gaps: nothing to draw
            if (this.input.isSpace(slot)) {
                continue;
            }

            int x = left + this.slotX[slot];
            int y = top + this.slotY[slot];
            guiGraphics.blitSprite(GuiTextures.SLOT, x, y, this.slotSize, this.slotSize);

            if (revealing) {
                // During the reveal, each slot shows the letter that belongs there.
                // Very low alpha values are drawn fully opaque by the font, so stop drawing first
                if (alphaByte >= 8) {
                    drawGlyph(guiGraphics, this.view.reveal().charAt(slot), x, y, this.slotSize,
                            (alphaByte << 24) | (textColor & 0xFFFFFF));
                }
                continue;
            }

            // Highlight slots a click would change: filled ones empty, empty ones take the selected tile
            boolean filled = this.input.isFilled(slot);
            boolean clickable = filled || this.selectedTile != NO_TILE;
            if (active && clickable && isInside(mouseX, mouseY, x, y, this.slotSize, this.slotSize)) {
                guiGraphics.fill(x + 1, y + 1, x + this.slotSize - 1, y + this.slotSize - 1,
                        GuiColors.get(GuiColors.SLOT_HOVER));
            }

            if (filled) {
                drawGlyph(guiGraphics, this.input.tile(this.input.content(slot)), x, y, this.slotSize, textColor);
            }
        }
    }

    private void renderKeys(GuiGraphics guiGraphics, int left, int top, int mouseX, int mouseY, boolean active) {
        for (int tile = 0; tile < this.input.tileCount(); tile++) {
            int x = left + this.keyX[tile];
            int y = top + this.keyY[tile];

            // Placed tiles stay as empty keys, so the others don't jump around
            if (this.input.isUsed(tile)) {
                guiGraphics.blitSprite(GuiTextures.BUTTON_DISABLED, x, y, this.keySize, this.keySize);
                continue;
            }

            boolean selected = tile == this.selectedTile;
            boolean hovered = active && isInside(mouseX, mouseY, x, y, this.keySize, this.keySize);
            guiGraphics.blitSprite(GuiTextures.button(active, selected || hovered), x, y, this.keySize, this.keySize);

            int color = GuiColors.get(!active ? GuiColors.BUTTON_TEXT_DISABLED
                    : selected ? GuiColors.KEY_TEXT_SELECTED
                    : GuiColors.BUTTON_TEXT);

            // Hovering a key whose letter the player has learned shows the letter itself
            char hint = this.view.hints().charAt(tile);
            if (hovered && hint != Primer.UNKNOWN) {
                drawLetter(guiGraphics, hint, x, y, this.keySize, color);
            } else {
                drawGlyph(guiGraphics, this.input.tile(tile), x, y, this.keySize, color);
            }
        }
    }

    private void renderDone(GuiGraphics guiGraphics, int left, int top, int mouseX, int mouseY, boolean active) {
        int x = left + DONE_X;
        int y = top + DONE_Y;
        boolean hovered = active && isInside(mouseX, mouseY, x, y, DONE_WIDTH, DONE_HEIGHT);
        guiGraphics.blitSprite(GuiTextures.button(active, hovered), x, y, DONE_WIDTH, DONE_HEIGHT);
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.enchanters_script.done"),
                x + DONE_WIDTH / 2, y + (DONE_HEIGHT - 8) / 2,
                GuiColors.get(active ? GuiColors.BUTTON_TEXT : GuiColors.BUTTON_TEXT_DISABLED));
    }

    /**
     * Text centered in a box, shrunk to fit small boxes.
     */
    private void drawInBox(GuiGraphics guiGraphics, Component text, int x, int y, int size, int color) {
        float scale = Math.min(1f, (size - 4) / 8f);
        float width = this.font.width(text) * scale;
        float height = 7 * scale;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x + (size - width) / 2f, y + (size - height) / 2f, 0);
        guiGraphics.pose().scale(scale, scale, 1f);
        guiGraphics.drawString(this.font, text, 0, 0, color, true);
        guiGraphics.pose().popPose();
    }

    private void drawGlyph(GuiGraphics guiGraphics, char letter, int x, int y, int size, int color) {
        drawInBox(guiGraphics, GuiTextures.galactic(String.valueOf(letter)), x, y, size, color);
    }

    private void drawLetter(GuiGraphics guiGraphics, char letter, int x, int y, int size, int color) {
        drawInBox(guiGraphics, Component.literal(String.valueOf(letter)), x, y, size, color);
    }

    private static boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    // Input

    public boolean mouseClicked(double mouseX, double mouseY, int left, int top) {
        if (!acceptsInput(elapsed(0f))) {
            return false;
        }

        // Keys: select, or deselect when clicked again
        for (int tile = 0; tile < this.input.tileCount(); tile++) {
            if (isInside(mouseX, mouseY, left + this.keyX[tile], top + this.keyY[tile], this.keySize, this.keySize)) {
                if (!this.input.isUsed(tile)) {
                    this.selectedTile = this.selectedTile == tile ? NO_TILE : tile;
                    playClick();
                }
                return true;
            }
        }

        // Slots: empty a filled one, or place the selected tile in an empty one
        for (int slot = 0; slot < this.input.slotCount(); slot++) {
            if (this.input.isSpace(slot)) {
                continue;
            }
            if (isInside(mouseX, mouseY, left + this.slotX[slot], top + this.slotY[slot], this.slotSize, this.slotSize)) {
                if (this.input.isFilled(slot)) {
                    this.input.clear(slot);
                    this.sender.send(WritingActionPayload.CLEAR, slot, 0);
                    playClick();
                } else if (this.selectedTile != NO_TILE && this.input.place(slot, this.selectedTile)) {
                    this.sender.send(WritingActionPayload.PLACE, slot, this.selectedTile);
                    playClick();
                    // The placed tile left the keyboard, so nothing stays selected
                    this.selectedTile = NO_TILE;
                }
                return true;
            }
        }

        if (isInside(mouseX, mouseY, left + DONE_X, top + DONE_Y, DONE_WIDTH, DONE_HEIGHT)) {
            done();
            return true;
        }
        return false;
    }

    /**
     * Enter finishes, like the done button.
     */
    public boolean keyPressed(int keyCode) {
        if (acceptsInput(elapsed(0f)) && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)) {
            done();
            return true;
        }
        return false;
    }

    private void done() {
        this.finished = true;
        this.selectedTile = NO_TILE;
        this.sender.send(WritingActionPayload.DONE, 0, 0);
        playClick();
    }

    private static void playClick() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}