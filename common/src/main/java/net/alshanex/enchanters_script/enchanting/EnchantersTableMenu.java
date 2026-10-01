package net.alshanex.enchanters_script.enchanting;

import com.mojang.datafixers.util.Pair;
import net.alshanex.enchanters_script.book.CipheredBooks;
import net.alshanex.enchanters_script.cipher.WorldCipher;
import net.alshanex.enchanters_script.data.CipherSavedData;
import net.alshanex.enchanters_script.minigame.*;
import net.alshanex.enchanters_script.network.BonusViewPayload;
import net.alshanex.enchanters_script.network.OfferPreviewsPayload;
import net.alshanex.enchanters_script.network.WritingActionPayload;
import net.alshanex.enchanters_script.network.WritingStartPayload;
import net.alshanex.enchanters_script.platform.Services;
import net.alshanex.enchanters_script.primer.Primer;
import net.alshanex.enchanters_script.registry.ModMenus;
import net.alshanex.enchanters_script.scoring.TranscriptionScore;
import net.alshanex.enchanters_script.word.EnchantmentWords;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class EnchantersTableMenu extends AbstractContainerMenu implements WritingMenu {
    // Which view the screen shows; synced to the client through a data slot
    public static final int VIEW_OFFERS = 0;
    public static final int VIEW_WRITING = 1;
    public static final int VIEW_BONUS = 2;

    // Button ids sent by the client; 0 to 2 are the offer buttons
    public static final int OFFER_BUTTONS = 3;

    static final ResourceLocation EMPTY_SLOT_LAPIS_LAZULI = ResourceLocation.withDefaultNamespace("item/empty_slot_lapis_lazuli");

    private final Container enchantSlots;
    private final ContainerLevelAccess access;
    private final Player player;
    private final DataSlot view = DataSlot.standalone();

    // Server only
    private List<Offer> offers = List.of();
    @Nullable
    private WritingSession session;
    @Nullable
    private BonusChoices bonus;

    // On the server, built here; on the client, received through packets
    private List<OfferPreview> previews = List.of();
    @Nullable
    private WritingView writing;

    // Client only
    private List<BonusPreview> bonusPreviews = List.of();
    @Nullable
    private PickSelection bonusSelection;
    private int bonusRevealsLeft;

    @Nullable
    private Offer chosenOffer;

    public EnchantersTableMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    public EnchantersTableMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(ModMenus.ENCHANTING_TABLE, containerId);
        this.enchantSlots = new SimpleContainer(2) {
            @Override
            public void setChanged() {
                super.setChanged();
                EnchantersTableMenu.this.slotsChanged(this);
            }
        };

        this.access = access;
        this.player = playerInventory.player;

        // Item: only usable in the offers view, locked during the minigame
        this.addSlot(new Slot(this.enchantSlots, 0, 15, 47) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return isView(VIEW_OFFERS) && stack.isEnchantable();
            }

            @Override
            public boolean mayPickup(Player player) {
                return isView(VIEW_OFFERS);
            }

            @Override
            public boolean isActive() {
                return isView(VIEW_OFFERS);
            }
        });

        // Lapis: same rules as the item
        this.addSlot(new Slot(this.enchantSlots, 1, 35, 47) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isView(VIEW_OFFERS) && stack.is(Items.LAPIS_LAZULI);
            }

            @Override
            public boolean mayPickup(Player player) {
                return isView(VIEW_OFFERS);
            }

            @Override
            public boolean isActive() {
                return isView(VIEW_OFFERS);
            }

            @Override
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_SLOT_LAPIS_LAZULI);
            }
        });

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new InventorySlot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int i = 0; i < 9; ++i) {
            this.addSlot(new InventorySlot(playerInventory, i, 8 + i * 18, 142));
        }

        this.addDataSlot(this.view);
    }

    /**
     * Player inventory slots: only shown in the offers view.
     */
    private class InventorySlot extends Slot {
        InventorySlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean isActive() {
            return isView(VIEW_OFFERS);
        }
    }

    // Views

    public int view() {
        return this.view.get();
    }

    private boolean isView(int view) {
        return this.view.get() == view;
    }

    // Closing

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, blockPos) -> {
            // Closing mid-minigame or during bonuses: main enchantment only
            if (this.chosenOffer != null) {
                this.bonus = null;
                applyEnchantments(player, level, blockPos, List.of());
            }
            this.clearContainer(player, this.enchantSlots);
        });
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, Blocks.ENCHANTING_TABLE);
    }

    // Shift-clicking

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if (index < 2) {
                // From the table to the inventory
                if (!this.moveItemStackTo(itemStack2, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (itemStack2.is(Items.LAPIS_LAZULI)) {
                if (!this.moveItemStackTo(itemStack2, 1, 2, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (this.slots.get(0).hasItem() || !this.slots.get(0).mayPlace(itemStack2)) {
                    return ItemStack.EMPTY;
                }

                ItemStack itemStack3 = itemStack2.copyWithCount(1);
                itemStack2.shrink(1);
                this.slots.get(0).setByPlayer(itemStack3);
            }

            if (itemStack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemStack2);
        }

        return itemStack;
    }

    // Offers

    @Override
    public void slotsChanged(Container container) {
        // Offers only change in the offers view; during the minigame and bonuses the slots are locked
        if (container == this.enchantSlots && isView(VIEW_OFFERS)) {
            refreshOffers(container.getItem(0));
        }
    }

    private void refreshOffers(ItemStack itemStack) {
        this.access.execute((level, blockPos) -> {
            List<Offer> newOffers = List.of();
            List<OfferPreview> newPreviews = List.of();

            if (!itemStack.isEmpty() && itemStack.isEnchantable()) {
                int shelves = OfferGenerator.countBookshelves(level, blockPos);
                newOffers = OfferGenerator.generate(level.registryAccess(), itemStack, shelves, this.player.getEnchantmentSeed());

                List<OfferPreview> built = new ArrayList<>();
                for (Offer offer : newOffers) {
                    built.add(OfferPreview.from(offer));
                }
                newPreviews = built;
            }

            this.offers = newOffers;

            if (!newPreviews.equals(this.previews)) {
                this.previews = newPreviews;
                if (this.player instanceof ServerPlayer serverPlayer) {
                    Services.NETWORK.sendToPlayer(serverPlayer, new OfferPreviewsPayload(this.containerId, newPreviews));
                }
            }
        });
    }

    public List<OfferPreview> previews() {
        return this.previews;
    }

    public void setPreviews(List<OfferPreview> previews) {
        this.previews = List.copyOf(previews);
    }

    // Buttons (offers and bonuses)

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id >= 0 && id < OFFER_BUTTONS) {
            return startWriting(player, id);
        }
        if (isView(VIEW_BONUS) && this.bonus != null) {
            return pressBonusButton(id);
        }
        return false;
    }

    // Starting the minigame

    private boolean startWriting(Player player, int index) {
        // Check everything again: the client's buttons are only a suggestion
        if (!isView(VIEW_OFFERS) || index >= this.offers.size()) {
            return false;
        }

        ItemStack item = this.enchantSlots.getItem(0);
        ItemStack lapis = this.enchantSlots.getItem(1);
        Offer offer = this.offers.get(index);
        boolean creative = player.getAbilities().instabuild;

        if (item.isEmpty()) {
            return false;
        }
        if (!creative && (lapis.isEmpty() || player.experienceLevel < offer.cost())) {
            return false;
        }

        this.access.execute((level, blockPos) -> {
            // Switch views first, so the slot changes below don't regenerate offers
            this.view.set(VIEW_WRITING);

            // Pay: all the lapis, then 1 to 3 levels, which also rerolls the seed
            int lapisCount = lapis.getCount();
            lapis.consume(lapisCount, player);
            if (lapis.isEmpty()) {
                this.enchantSlots.setItem(1, ItemStack.EMPTY);
            }
            player.onEnchantmentPerformed(item, offer.slot() + 1);

            String fullWord = EnchantmentWords.SERVER.fullWord(offer.enchantment(), offer.level());

            String tiles = KeyboardLayout.join(KeyboardLayout.generate(fullWord, offer.level(), level.getRandom()));

            int revealTicks = MinigameTimers.revealTicks(fullWord);
            int writingTicks = MinigameTimers.writingTicks(fullWord, lapisCount);

            this.chosenOffer = offer;
            this.session = WritingSession.start(fullWord, tiles, level.getGameTime(), revealTicks, writingTicks);

            // Encode everything the client will draw
            WorldCipher cipher = CipherSavedData.get(level.getServer());
            this.writing = new WritingView(cipher.encode(fullWord), cipher.encode(tiles), revealTicks, writingTicks);

            if (player instanceof ServerPlayer serverPlayer) {
                Services.NETWORK.sendToPlayer(serverPlayer, new WritingStartPayload(this.containerId, this.writing));
            }
        });
        return true;
    }

    @Nullable
    @Override
    public WritingView writing() {
        return this.writing;
    }

    @Override
    public void setWriting(WritingView writing) {
        this.writing = writing;
    }

    // Writing

    /**
     * A writing move from the client's packet. Every move is checked before it's applied.
     */
    @Override
    public void handleWritingAction(Player player, int action, int slot, int key) {
        if (!isView(VIEW_WRITING) || this.session == null) {
            return;
        }

        long now = player.level().getGameTime();
        if (this.session.isExpired(now)) {
            finishWriting();
            return;
        }
        if (!this.session.acceptsInput(now)) {
            return;
        }

        switch (action) {
            case WritingActionPayload.PLACE -> this.session.input().place(slot, key);
            case WritingActionPayload.CLEAR -> this.session.input().clear(slot);
            case WritingActionPayload.DONE -> finishWriting();
            default -> {
            }
        }
    }

    @Override
    public void broadcastChanges() {
        // Runs every server tick while the menu is open, so it doubles as the timer check
        if (this.session != null && isView(VIEW_WRITING)
                && this.session.isExpired(this.player.level().getGameTime())) {
            finishWriting();
        }
        super.broadcastChanges();
    }

    private void finishWriting() {
        WritingSession finished = this.session;
        String written = finished.input().text();
        float score = TranscriptionScore.score(written, finished.fullWord());
        TableRating rating = TableRating.fromScore(score);

        if (rating.teachesLetters() && this.player instanceof ServerPlayer serverPlayer) {
            Primer.learnFrom(serverPlayer, finished.input(), finished.fullWord());
        }

        this.access.execute((level, blockPos) -> {
            this.writing = null;

            List<EnchantmentInstance> choices = BonusGenerator.generate(level.registryAccess(),
                    this.enchantSlots.getItem(0), this.chosenOffer, rating.bonusChoices(), level.getRandom());


            // Nothing to choose from: finish right away
            if (choices.isEmpty() || rating.picks() == 0) {
                completeEnchant(level, blockPos, List.of());
                return;
            }

            List<String> words = new ArrayList<>();
            for (EnchantmentInstance choice : choices) {
                words.add(EnchantmentWords.SERVER.fullWord(choice.enchantment, choice.level));
            }

            this.bonus = new BonusChoices(choices, words, Math.min(rating.picks(), choices.size()));
            this.view.set(VIEW_BONUS);
            sendBonus(true);
        });
    }

    // Bonuses

    private boolean pressBonusButton(int id) {
        if (id == BonusButtons.CONFIRM) {
            List<EnchantmentInstance> picked = this.bonus.selectedChoices();
            this.access.execute((level, blockPos) -> completeEnchant(level, blockPos, picked));
            return true;
        }
        if (id == BonusButtons.USE) {
            if (this.bonus.canReveal() && takeShard()) {
                this.bonus.revealOne(this.player.getRandom());
                sendBonus(false);
            }
            return true;
        }
        if (BonusButtons.isToggle(id)) {
            this.bonus.selection().toggle(id - BonusButtons.TOGGLE_BASE, this.bonus.size());
            return true;
        }
        return false;
    }

    /**
     * Takes one amethyst shard from the player's inventory. Creative players don't need any.
     */
    private boolean takeShard() {
        if (this.player.hasInfiniteMaterials()) {
            return true;
        }
        Inventory inventory = this.player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(Items.AMETHYST_SHARD)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private void sendBonus(boolean initial) {
        if (this.player instanceof ServerPlayer serverPlayer) {
            WorldCipher cipher = CipherSavedData.get(serverPlayer.server);
            Services.NETWORK.sendToPlayer(serverPlayer, new BonusViewPayload(this.containerId,
                    this.bonus.previews(cipher), this.bonus.selection().picks(), this.bonus.revealsLeft(), initial));
        }
    }

    public List<BonusPreview> bonusPreviews() {
        return this.bonusPreviews;
    }

    @Nullable
    public PickSelection bonusSelection() {
        return this.bonusSelection;
    }

    public int bonusRevealsLeft() {
        return this.bonusRevealsLeft;
    }

    public void setBonus(List<BonusPreview> previews, int picks, int revealsLeft, boolean initial) {
        this.bonusPreviews = List.copyOf(previews);
        this.bonusRevealsLeft = revealsLeft;
        // Reveal updates keep the player's selection; only a new bonus view starts fresh
        if (initial || this.bonusSelection == null) {
            this.bonusSelection = new PickSelection(picks);
        }
    }

    // Finishing

    /**
     * Applies everything and goes back to the offers view.
     */
    private void completeEnchant(Level level, BlockPos blockPos, List<EnchantmentInstance> extras) {
        // Clear the bonus state first, so nothing below can act on it again
        this.bonus = null;
        this.writing = null;
        applyEnchantments(this.player, level, blockPos, extras);

        this.view.set(VIEW_OFFERS);
        // The view was not offers while the item changed, so refresh by hand; this clears the offers
        refreshOffers(this.enchantSlots.getItem(0));
    }

    private void applyEnchantments(Player player, Level level, BlockPos blockPos, List<EnchantmentInstance> extras) {
        Offer offer = this.chosenOffer;
        this.chosenOffer = null;
        this.session = null;

        ItemStack item = this.enchantSlots.getItem(0);
        if (item.isEmpty()) {
            return;
        }

        // A book becomes an enchanted book, like in vanilla
        ItemStack result = item.is(Items.BOOK) ? item.transmuteCopy(Items.ENCHANTED_BOOK) : item;
        result.enchant(offer.enchantment(), offer.level());
        for (EnchantmentInstance extra : extras) {
            result.enchant(extra.enchantment, extra.level);
        }
        // The minigame was already played here, so table books are never ciphered
        if (result.is(Items.ENCHANTED_BOOK)) {
            CipheredBooks.markDeciphered(result);
        }
        this.enchantSlots.setItem(0, result);

        player.awardStat(Stats.ENCHANT_ITEM);
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.ENCHANTED_ITEM.trigger(serverPlayer, result, offer.slot() + 1);
        }
        level.playSound(null, blockPos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS,
                1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
    }


}