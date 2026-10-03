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
    @Nullable
    private WritingPage page;
    @Nullable
    private WritingView pageView;

    public BookScreen(BookMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        WritingPage current = currentPage();
        if (current != null) {
            // The page draws its own panel
            current.render(guiGraphics, leftPos, topPos, mouseX, mouseY, partialTick);
        } else {
            // Until the minigame data arrives, show the empty panel
            guiGraphics.blit(GuiTextures.WRITING_PANEL, leftPos, topPos, 0, 0, GuiTextures.PANEL_WIDTH, GuiTextures.PANEL_HEIGHT);
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
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        WritingPage current = currentPage();
        if (current != null) {
            current.mouseReleased(mouseX, mouseY, leftPos, topPos);
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
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