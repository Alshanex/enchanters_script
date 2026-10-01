package net.alshanex.enchanters_script.client;

import net.alshanex.enchanters_script.book.BookMenu;
import net.alshanex.enchanters_script.minigame.WritingView;
import net.alshanex.enchanters_script.network.WritingActionPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;

/**
 * The writing page on its own, for deciphering a book from the hand.
 */
public class BookScreen extends AbstractContainerScreen<BookMenu> {
    // Vanilla's table panel as the frame; the page covers everything inside it
    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/enchanting_table.png");

    @Nullable
    private WritingPage page;
    @Nullable
    private WritingView pageView;

    public BookScreen(BookMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        WritingPage current = currentPage();
        if (current != null) {
            current.render(guiGraphics, leftPos, topPos, mouseX, mouseY, partialTick);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // The page covers the whole panel
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        WritingPage current = currentPage();
        if (current != null) {
            current.mouseClicked(mouseX, mouseY, leftPos, topPos);
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        WritingPage current = currentPage();
        if (current != null) {
            current.keyPressed(keyCode);
        }
        // Swallow every other key, so the inventory key can't close the screen by accident
        return true;
    }

    @Nullable
    private WritingPage currentPage() {
        WritingView writing = menu.writing();
        if (writing == null) {
            return null;
        }
        if (writing != this.pageView) {
            this.pageView = writing;
            this.page = new WritingPage(font, writing, minecraft.level.getGameTime(),
                    (action, slot, key) -> minecraft.getConnection().send(new ServerboundCustomPayloadPacket(
                            new WritingActionPayload(menu.containerId, action, slot, key))));
        }
        return this.page;
    }
}