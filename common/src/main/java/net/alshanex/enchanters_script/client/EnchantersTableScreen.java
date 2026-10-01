package net.alshanex.enchanters_script.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.alshanex.enchanters_script.enchanting.EnchantersTableMenu;
import net.alshanex.enchanters_script.enchanting.OfferPreview;
import net.alshanex.enchanters_script.minigame.BonusButtons;
import net.alshanex.enchanters_script.minigame.BonusPreview;
import net.alshanex.enchanters_script.minigame.PickSelection;
import net.alshanex.enchanters_script.minigame.WritingView;
import net.alshanex.enchanters_script.network.WritingActionPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

public class EnchantersTableScreen extends AbstractContainerScreen<EnchantersTableMenu> {

    // Vanilla's art, so the table looks like the one players know
    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/enchanting_table.png");
    private static final ResourceLocation SLOT_SPRITE =
            ResourceLocation.withDefaultNamespace("container/enchanting_table/enchantment_slot");
    private static final ResourceLocation SLOT_HIGHLIGHTED_SPRITE =
            ResourceLocation.withDefaultNamespace("container/enchanting_table/enchantment_slot_highlighted");
    private static final ResourceLocation SLOT_DISABLED_SPRITE =
            ResourceLocation.withDefaultNamespace("container/enchanting_table/enchantment_slot_disabled");
    private static final ResourceLocation[] LEVEL_SPRITES = {
            ResourceLocation.withDefaultNamespace("container/enchanting_table/level_1"),
            ResourceLocation.withDefaultNamespace("container/enchanting_table/level_2"),
            ResourceLocation.withDefaultNamespace("container/enchanting_table/level_3")
    };
    private static final ResourceLocation[] LEVEL_DISABLED_SPRITES = {
            ResourceLocation.withDefaultNamespace("container/enchanting_table/level_1_disabled"),
            ResourceLocation.withDefaultNamespace("container/enchanting_table/level_2_disabled"),
            ResourceLocation.withDefaultNamespace("container/enchanting_table/level_3_disabled")
    };
    private static final ResourceLocation BUTTON_SPRITE = ResourceLocation.withDefaultNamespace("widget/button");
    private static final ResourceLocation BUTTON_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/button_highlighted");
    private static final ResourceLocation BUTTON_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("widget/button_disabled");
    private static final Style GALACTIC = Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("alt"));

    private static final ResourceLocation EMPTY_SLOT_AMETHYST_SHARD =
            ResourceLocation.withDefaultNamespace("item/empty_slot_amethyst_shard");

    // Offer buttons, taken from vanilla's EnchantmentScreen
    private static final int BUTTON_X = 60;
    private static final int BUTTON_Y = 14;
    private static final int BUTTON_WIDTH = 108;
    private static final int BUTTON_HEIGHT = 19;
    private static final int BUTTON_ROWS = 3;
    private static final int TEXT_OFFSET = 20;
    private static final int TEXT_AREA_WIDTH = 86;

    // Offer names, drawn smaller than normal text
    private static final float NAME_SCALE = 0.85f;
    private static final int MAX_NAME_LINES = 2;
    private static final float SMALL_NAME_SCALE = 0.75f;
    private static final int MAX_SMALL_NAME_LINES = 3;

    // Bonus view: shards and Use on the left, cards on the right, picks and confirm below
    private static final int SHARD_X = 16;
    private static final int SHARD_Y = 24;
    private static final int USE_X = 10;
    private static final int USE_Y = 46;
    private static final int USE_WIDTH = 30;
    private static final int USE_HEIGHT = 14;
    private static final int REVEALS_CENTER_X = 25;
    private static final int REVEALS_Y = 64;
    private static final int CARD_X = 46;
    private static final int CARD_Y = 18;
    private static final int CARD_WIDTH = 122;
    private static final int CARD_HEIGHT = 26;
    private static final int CARD_PITCH = 30;
    private static final int CARD_TEXT_WIDTH = 114;
    private static final int PICKS_Y = 112;
    private static final int CONFIRM_X = 58;
    private static final int CONFIRM_Y = 130;
    private static final int CONFIRM_WIDTH = 60;
    private static final int CONFIRM_HEIGHT = 16;

    // Colors
    private static final int PANEL_COLOR = 0xFFC6C6C6;
    private static final int LABEL_COLOR = 0x404040;
    private static final int NAME_COLOR = 0x685E4A;
    private static final int NAME_HOVERED_COLOR = 0xFFFF80;
    private static final int NAME_DISABLED_COLOR = 0x342F25;
    private static final int COST_COLOR = 0x80FF20;
    private static final int COST_DISABLED_COLOR = 0x407F10;

    @Nullable
    private WritingPage page;
    @Nullable
    private WritingView pageView;

    public EnchantersTableScreen(EnchantersTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    // Drawing

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        switch (menu.view()) {
            case EnchantersTableMenu.VIEW_OFFERS -> {
                for (int row = 0; row < BUTTON_ROWS; row++) {
                    renderButton(guiGraphics, row, mouseX, mouseY);
                }
            }
            case EnchantersTableMenu.VIEW_WRITING -> {
                WritingPage current = currentPage();
                if (current != null) {
                    current.render(guiGraphics, leftPos, topPos, mouseX, mouseY, partialTick);
                }
            }
            case EnchantersTableMenu.VIEW_BONUS -> renderBonus(guiGraphics, mouseX, mouseY);
            default -> {
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // The writing page covers the whole panel, labels included
        if (menu.view() == EnchantersTableMenu.VIEW_WRITING) {
            return;
        }
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, LABEL_COLOR, false);
        // The inventory only shows in the offers view
        if (menu.view() == EnchantersTableMenu.VIEW_OFFERS) {
            guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, LABEL_COLOR, false);
        }
    }

    // Offers

    private void renderButton(GuiGraphics guiGraphics, int row, int mouseX, int mouseY) {
        int x = leftPos + BUTTON_X;
        int y = topPos + BUTTON_Y + BUTTON_HEIGHT * row;

        // Rows without an offer look like vanilla's empty buttons
        List<OfferPreview> previews = menu.previews();
        if (row >= previews.size()) {
            RenderSystem.enableBlend();
            guiGraphics.blitSprite(SLOT_DISABLED_SPRITE, x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
            RenderSystem.disableBlend();
            return;
        }

        OfferPreview preview = previews.get(row);
        boolean enabled = canAfford(preview);
        boolean hovered = enabled && isInside(mouseX, mouseY, x, y, BUTTON_WIDTH, BUTTON_HEIGHT);

        ResourceLocation sprite = !enabled ? SLOT_DISABLED_SPRITE
                : hovered ? SLOT_HIGHLIGHTED_SPRITE
                : SLOT_SPRITE;
        RenderSystem.enableBlend();
        guiGraphics.blitSprite(sprite, x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
        RenderSystem.disableBlend();

        // Level icon: 1 to 3 dots, from the vanilla slot the offer came from
        int iconIndex = Math.min(preview.slot(), LEVEL_SPRITES.length - 1);
        guiGraphics.blitSprite(enabled ? LEVEL_SPRITES[iconIndex] : LEVEL_DISABLED_SPRITES[iconIndex],
                x + 1, y + 1, 16, 16);

        // The cost decides how much room is left for the name
        String costText = String.valueOf(preview.cost());
        int textWidth = TEXT_AREA_WIDTH - font.width(costText);
        int textX = x + TEXT_OFFSET;

        int nameColor = !enabled ? NAME_DISABLED_COLOR
                : hovered ? NAME_HOVERED_COLOR
                : NAME_COLOR;
        renderName(guiGraphics, preview.name(), textX, y, textWidth, nameColor);

        guiGraphics.drawString(font, costText, textX + textWidth, y + 9,
                enabled ? COST_COLOR : COST_DISABLED_COLOR, true);
    }

    private void renderName(GuiGraphics guiGraphics, Component name, int x, int buttonY, int width, int color) {
        // Split for the scaled size: smaller text fits more per line
        float scale = NAME_SCALE;
        int maxLines = MAX_NAME_LINES;
        List<FormattedCharSequence> lines = font.split(name, (int) (width / scale));

        if (lines.size() > MAX_NAME_LINES) {
            scale = SMALL_NAME_SCALE;
            maxLines = MAX_SMALL_NAME_LINES;
            lines = font.split(name, (int) (width / scale));
        }

        int count = Math.min(lines.size(), maxLines);

        // Center the lines vertically in the button; the last line doesn't need its gap below
        float blockHeight = (count * font.lineHeight - 1) * scale;
        float textY = buttonY + (BUTTON_HEIGHT - blockHeight) / 2f;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, textY, 0);
        guiGraphics.pose().scale(scale, scale, 1f);
        for (int i = 0; i < count; i++) {
            guiGraphics.drawString(font, lines.get(i), 0, i * font.lineHeight, color, false);
        }
        guiGraphics.pose().popPose();
    }

    private boolean canAfford(OfferPreview preview) {
        Player player = minecraft.player;

        // Creative players skip both requirements, as in vanilla
        if (player.getAbilities().instabuild) {
            return true;
        }

        // Any amount of lapis works, since the amount only sets the writing time
        return menu.getSlot(1).hasItem() && player.experienceLevel >= preview.cost();
    }

    // Writing

    /**
     * The page for the current minigame, created when its data first arrives.
     */
    @Nullable
    private WritingPage currentPage() {
        WritingView writing = menu.writing();
        if (writing == null) {
            return null;
        }
        // A new minigame sends a new WritingView, so a different object means a new page
        if (writing != this.pageView) {
            this.pageView = writing;
            this.page = new WritingPage(font, writing, minecraft.level.getGameTime(),
                    (action, slot, key) -> minecraft.getConnection().send(new ServerboundCustomPayloadPacket(
                            new WritingActionPayload(menu.containerId, action, slot, key))));
        }
        return this.page;
    }

    // Bonuses

    private void renderBonus(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Cover the whole inside of the panel; the title is drawn on top afterwards
        guiGraphics.fill(leftPos + 7, topPos + 7, leftPos + 169, topPos + 159, PANEL_COLOR);

        // The shards Use can still spend: what the player carries, capped at the reveals left
        int shardX = leftPos + SHARD_X;
        int shardY = topPos + SHARD_Y;
        renderSlotBox(guiGraphics, shardX, shardY);
        int usable = usableShards();
        if (usable > 0) {
            ItemStack shardStack = new ItemStack(Items.AMETHYST_SHARD, usable);
            guiGraphics.renderItem(shardStack, shardX + 1, shardY + 1);
            guiGraphics.renderItemDecorations(font, shardStack, shardX + 1, shardY + 1);
        } else {
            // Nothing to use: vanilla's faded icon, like an empty slot
            TextureAtlasSprite icon = minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(EMPTY_SLOT_AMETHYST_SHARD);
            guiGraphics.blit(shardX + 1, shardY + 1, 0, 16, 16, icon);
        }

        renderTextButton(guiGraphics, leftPos + USE_X, topPos + USE_Y, USE_WIDTH, USE_HEIGHT,
                Component.translatable("gui.enchanters_script.use"), usable > 0, mouseX, mouseY);

        PickSelection selection = menu.bonusSelection();
        if (selection == null) {
            return;
        }

        List<BonusPreview> previews = menu.bonusPreviews();
        for (int i = 0; i < previews.size(); i++) {
            int x = leftPos + CARD_X;
            int y = topPos + CARD_Y + i * CARD_PITCH;

            boolean selected = selection.isSelected(i);
            boolean available = selection.canSelect(i);
            boolean hovered = available && isInside(mouseX, mouseY, x, y, CARD_WIDTH, CARD_HEIGHT);

            // Selected cards stay highlighted; cards that can't be picked right now look disabled
            ResourceLocation sprite = !available ? BUTTON_DISABLED_SPRITE
                    : (selected || hovered) ? BUTTON_HIGHLIGHTED_SPRITE
                    : BUTTON_SPRITE;
            guiGraphics.blitSprite(sprite, x, y, CARD_WIDTH, CARD_HEIGHT);

            int color = selected ? 0xFFFF80 : available ? 0xFFFFFF : 0xA0A0A0;
            BonusPreview preview = previews.get(i);
            drawCardLine(guiGraphics, Component.literal(preview.hint()), x + 4, y + 5, color);
            drawCardLine(guiGraphics, galactic(preview.galactic()), x + 4, y + 15, color);
        }

        Component picks = Component.translatable("gui.enchanters_script.picks", selection.count(), selection.picks());
        guiGraphics.drawString(font, picks, leftPos + 88 - font.width(picks) / 2, topPos + PICKS_Y, LABEL_COLOR, false);

        renderTextButton(guiGraphics, leftPos + CONFIRM_X, topPos + CONFIRM_Y, CONFIRM_WIDTH, CONFIRM_HEIGHT,
                Component.translatable("gui.enchanters_script.confirm"), true, mouseX, mouseY);
    }

    private int usableShards() {
        Player player = minecraft.player;
        int carried = player.hasInfiniteMaterials()
                ? Integer.MAX_VALUE
                : player.getInventory().countItem(Items.AMETHYST_SHARD);
        return Math.min(carried, menu.bonusRevealsLeft());
    }

    /**
     * Draws one card line at full size, smaller if it would overflow the card.
     */
    private void drawCardLine(GuiGraphics guiGraphics, Component text, int x, int y, int color) {
        int width = font.width(text);
        float scale = width > CARD_TEXT_WIDTH ? (float) CARD_TEXT_WIDTH / width : 1f;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0);
        guiGraphics.pose().scale(scale, scale, 1f);
        guiGraphics.drawString(font, text, 0, 0, color, false);
        guiGraphics.pose().popPose();
    }

    private void renderTextButton(GuiGraphics guiGraphics, int x, int y, int width, int height,
                                  Component label, boolean enabled, int mouseX, int mouseY) {
        boolean hovered = enabled && isInside(mouseX, mouseY, x, y, width, height);
        ResourceLocation sprite = !enabled ? BUTTON_DISABLED_SPRITE : hovered ? BUTTON_HIGHLIGHTED_SPRITE : BUTTON_SPRITE;
        guiGraphics.blitSprite(sprite, x, y, width, height);
        guiGraphics.drawCenteredString(font, label, x + width / 2, y + (height - 8) / 2, enabled ? 0xFFFFFF : 0xA0A0A0);
    }

    /**
     * A slot box in vanilla's style, since the vanilla texture has none at this position.
     */
    private static void renderSlotBox(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + 18, y + 18, 0xFF8B8B8B);
        guiGraphics.fill(x, y, x + 17, y + 1, 0xFF373737);
        guiGraphics.fill(x, y, x + 1, y + 17, 0xFF373737);
        guiGraphics.fill(x + 1, y + 17, x + 18, y + 18, 0xFFFFFFFF);
        guiGraphics.fill(x + 17, y + 1, x + 18, y + 18, 0xFFFFFFFF);
    }

    // Input

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        switch (menu.view()) {
            case EnchantersTableMenu.VIEW_WRITING -> {
                WritingPage current = currentPage();
                if (current != null) {
                    current.mouseClicked(mouseX, mouseY, leftPos, topPos);
                }
                // Nothing else on the screen can be clicked while writing
                return true;
            }
            case EnchantersTableMenu.VIEW_BONUS -> {
                bonusClicked(mouseX, mouseY);
                // There are no slots in the bonus view, so nothing else to click
                return true;
            }
            default -> {
                if (offerClicked(mouseX, mouseY)) {
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean offerClicked(double mouseX, double mouseY) {
        List<OfferPreview> previews = menu.previews();
        for (int row = 0; row < previews.size(); row++) {
            int y = topPos + BUTTON_Y + BUTTON_HEIGHT * row;
            if (canAfford(previews.get(row)) && isInside(mouseX, mouseY, leftPos + BUTTON_X, y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                // Vanilla's button packet: the server receives it in clickMenuButton
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, row);
                return true;
            }
        }
        return false;
    }

    private void bonusClicked(double mouseX, double mouseY) {
        PickSelection selection = menu.bonusSelection();
        if (selection == null) {
            return;
        }

        List<BonusPreview> previews = menu.bonusPreviews();
        for (int i = 0; i < previews.size(); i++) {
            int y = topPos + CARD_Y + i * CARD_PITCH;
            if (isInside(mouseX, mouseY, leftPos + CARD_X, y, CARD_WIDTH, CARD_HEIGHT)) {
                if (selection.canSelect(i)) {
                    // Show it right away; the server applies the same toggle
                    selection.toggle(i, previews.size());
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, BonusButtons.TOGGLE_BASE + i);
                    playClick();
                }
                return;
            }
        }

        if (usableShards() > 0 && isInside(mouseX, mouseY, leftPos + USE_X, topPos + USE_Y, USE_WIDTH, USE_HEIGHT)) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, BonusButtons.USE);
            playClick();
            return;
        }

        if (isInside(mouseX, mouseY, leftPos + CONFIRM_X, topPos + CONFIRM_Y, CONFIRM_WIDTH, CONFIRM_HEIGHT)) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, BonusButtons.CONFIRM);
            playClick();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
            if (menu.view() == EnchantersTableMenu.VIEW_WRITING) {
                WritingPage current = currentPage();
                if (current != null) {
                    current.keyPressed(keyCode);
                }
                // Swallow every other key, so the inventory key can't close the screen by accident
                return true;
            }
            if (menu.view() == EnchantersTableMenu.VIEW_BONUS) {
                // Closing counts as picking no bonuses, so only Escape closes here
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // Helpers

    private static Component galactic(String symbols) {
        // Vanilla only draws lowercase with this font
        return Component.literal(symbols.toLowerCase(Locale.ROOT)).withStyle(GALACTIC);
    }

    private static boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private void playClick() {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}