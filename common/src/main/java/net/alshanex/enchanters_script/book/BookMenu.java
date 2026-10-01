package net.alshanex.enchanters_script.book;

import net.alshanex.enchanters_script.Constants;
import net.alshanex.enchanters_script.cipher.WorldCipher;
import net.alshanex.enchanters_script.data.CipherSavedData;
import net.alshanex.enchanters_script.minigame.*;
import net.alshanex.enchanters_script.network.WritingActionPayload;
import net.alshanex.enchanters_script.network.WritingStartPayload;
import net.alshanex.enchanters_script.platform.Services;
import net.alshanex.enchanters_script.registry.ModMenus;
import net.alshanex.enchanters_script.scoring.TranscriptionScore;
import net.alshanex.enchanters_script.word.EnchantmentWords;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Deciphering a ciphered book from the hand. No slots: the book stays in the player's hand,
 * and the menu only exists so the server can run the minigame, time it, and catch the screen closing.
 */
public class BookMenu extends AbstractContainerMenu implements WritingMenu {
    private final Player player;
    private final InteractionHand hand;

    // Server only
    @Nullable
    private Holder<Enchantment> enchantment;
    private int level;
    @Nullable
    private WritingSession session;
    private boolean finished;

    // On the server, built here; on the client, received through the packet
    @Nullable
    private WritingView writing;

    public BookMenu(int containerId, Inventory playerInventory) {
        // The client doesn't need to know which hand holds the book
        this(containerId, playerInventory, InteractionHand.MAIN_HAND);
    }

    public BookMenu(int containerId, Inventory playerInventory, InteractionHand hand) {
        super(ModMenus.BOOK, containerId);
        this.player = playerInventory.player;
        this.hand = hand;
    }

    // Opening

    /**
     * Opens the minigame for the ciphered book in the player's hand, if they can pay for it.
     */
    public static void tryOpen(ServerPlayer player, InteractionHand hand) {
        ItemStack book = player.getItemInHand(hand);
        Optional<CipheredBooks.BookEnchantment> main = CipheredBooks.mainEnchantment(book);
        if (main.isEmpty()) {
            return;
        }

        // Checked before opening, so nobody plays the whole minigame only to be refused
        int cost = CipheredBooks.xpCost(main.get().level());
        if (!player.getAbilities().instabuild && player.experienceLevel < cost) {
            player.displayClientMessage(Component.translatable("message.enchanters_script.not_enough_xp", cost), true);
            return;
        }

        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, p) -> new BookMenu(containerId, inventory, hand),
                book.getHoverName()));

        // Started after opening, so the client already has this menu when the packet arrives
        if (player.containerMenu instanceof BookMenu menu) {
            menu.start(player, main.get());
        }
    }

    private void start(ServerPlayer serverPlayer, CipheredBooks.BookEnchantment main) {
        this.enchantment = main.enchantment();
        this.level = main.level();

        String fullWord = EnchantmentWords.SERVER.fullWord(this.enchantment, this.level);
        String tiles = KeyboardLayout.join(KeyboardLayout.generate(fullWord, this.level, serverPlayer.getRandom()));

        // No lapis: the book's own timer
        int revealTicks = MinigameTimers.revealTicks(fullWord);
        int writingTicks = MinigameTimers.bookTicks(fullWord);

        this.session = WritingSession.start(fullWord, tiles, serverPlayer.level().getGameTime(), revealTicks, writingTicks);

        WorldCipher cipher = CipherSavedData.get(serverPlayer.server);
        this.writing = new WritingView(cipher.encode(fullWord), cipher.encode(tiles), revealTicks, writingTicks);
        Services.NETWORK.sendToPlayer(serverPlayer, new WritingStartPayload(this.containerId, this.writing));
    }

    // Writing

    @Nullable
    @Override
    public WritingView writing() {
        return this.writing;
    }

    @Override
    public void setWriting(WritingView writing) {
        this.writing = writing;
    }

    @Override
    public void handleWritingAction(Player player, int action, int slot, int key) {
        if (this.session == null) {
            return;
        }

        long now = player.level().getGameTime();
        if (this.session.isExpired(now)) {
            finish(false);
            return;
        }
        if (!this.session.acceptsInput(now)) {
            return;
        }

        switch (action) {
            case WritingActionPayload.PLACE -> this.session.input().place(slot, key);
            case WritingActionPayload.CLEAR -> this.session.input().clear(slot);
            case WritingActionPayload.DONE -> finish(false);
            default -> {
            }
        }
    }

    @Override
    public void broadcastChanges() {
        // Runs every server tick while the menu is open, so it doubles as the timer check
        if (this.session != null && this.session.isExpired(this.player.level().getGameTime())) {
            finish(false);
        }
        super.broadcastChanges();
    }

    // Finishing

    @Override
    public void removed(Player player) {
        super.removed(player);
        // Closing early scores whatever has been written so far
        finish(true);
    }

    /**
     * Scores the writing and applies the result. Runs once, however the minigame ends.
     */
    private void finish(boolean closing) {
        if (this.finished || this.session == null) {
            return;
        }
        this.finished = true;
        WritingSession done = this.session;
        this.session = null;

        float score = TranscriptionScore.score(done.input().text(), done.fullWord());
        BookResult result = BookResult.fromScore(score);

        // A level I book can't lose a level, so a bad result counts as a retry instead
        if (result == BookResult.BAD && this.level <= 1) {
            result = BookResult.RETRY;
        }

        if (this.player instanceof ServerPlayer serverPlayer) {
            applyResult(serverPlayer, result);
            // The screen is still open when the minigame ends by itself, so close it
            if (!closing) {
                serverPlayer.closeContainer();
            }
        }
    }

    private void applyResult(ServerPlayer player, BookResult result) {
        ItemStack book = player.getItemInHand(this.hand);
        // The book must still be the ciphered book this minigame was for
        if (!CipheredBooks.isCiphered(book) || this.enchantment == null) {
            return;
        }

        if (!result.deciphers()) {
            // Retry: nothing happens and nothing is charged
            player.displayClientMessage(Component.translatable("message.enchanters_script.book_retry"), true);
            return;
        }

        boolean creative = player.getAbilities().instabuild;
        int cost = CipheredBooks.xpCost(this.level);
        if (!creative && player.experienceLevel < cost) {
            player.displayClientMessage(Component.translatable("message.enchanters_script.not_enough_xp", cost), true);
            return;
        }
        if (!creative) {
            player.giveExperienceLevels(-cost);
        }

        int newLevel = this.level + result.levelChange();
        if (result.levelChange() > 0) {
            // Never past the max: vanilla's anvil would cap it there anyway as soon as the book is used
            int maxLevel = this.enchantment.value().getMaxLevel();
            newLevel = Math.min(newLevel, Math.max(this.level, maxLevel));
        }
        int finalLevel = newLevel;
        Holder<Enchantment> main = this.enchantment;
        // Level 0 removes the enchantment
        EnchantmentHelper.updateEnchantments(book, enchantments -> enchantments.set(main, finalLevel));

        CipheredBooks.markDeciphered(book);

        player.level().playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS,
                1.0F, player.getRandom().nextFloat() * 0.1F + 0.9F);
        player.displayClientMessage(Component.translatable(switch (result) {
            case PERFECT -> "message.enchanters_script.book_perfect";
            case DECENT -> "message.enchanters_script.book_decent";
            default -> "message.enchanters_script.book_bad";
        }), true);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // No slots, so nothing to shift-click
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        // Closes if the book leaves the hand, for example through a command
        return CipheredBooks.isCiphered(player.getItemInHand(this.hand));
    }
}