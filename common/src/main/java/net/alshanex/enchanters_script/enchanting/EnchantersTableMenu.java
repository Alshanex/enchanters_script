package net.alshanex.enchanters_script.enchanting;

import com.mojang.datafixers.util.Pair;
import net.alshanex.enchanters_script.registry.ModMenus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class EnchantersTableMenu extends AbstractContainerMenu {
    static final ResourceLocation EMPTY_SLOT_LAPIS_LAZULI = ResourceLocation.withDefaultNamespace("item/empty_slot_lapis_lazuli");
    static final ResourceLocation EMPTY_SLOT_AMETYST_SHARD = ResourceLocation.withDefaultNamespace("item/empty_slot_amethyst_shard");
    private final Container enchantSlots;
    private final ContainerLevelAccess access;

    public EnchantersTableMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    public EnchantersTableMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(ModMenus.ENCHANTING_TABLE, containerId);
        this.enchantSlots = new SimpleContainer(3) {
            public void setChanged() {
                super.setChanged();
                EnchantersTableMenu.this.slotsChanged(this);
            }
        };
        this.access = access;
        this.addSlot(new Slot(this.enchantSlots, 0, 10, 62) {
            public int getMaxStackSize() {
                return 1;
            }
            public boolean mayPlace(ItemStack stack) {
                return stack.isEnchantable();
            }
        });
        this.addSlot(new Slot(this.enchantSlots, 1, 28, 62) {
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.LAPIS_LAZULI);
            }

            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, EnchantersTableMenu.EMPTY_SLOT_LAPIS_LAZULI);
            }
        });

        this.addSlot(new Slot(this.enchantSlots, 2, 46, 62) {
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.AMETHYST_SHARD);
            }

            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, EnchantersTableMenu.EMPTY_SLOT_AMETYST_SHARD);
            }
        });

        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 48 + j * 18, 112 + i * 18));
            }
        }

        for(int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 48 + i * 18, 170));
        }
    }

    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, blockPos) -> this.clearContainer(player, this.enchantSlots));
    }

    public boolean stillValid(Player player) {
        return stillValid(this.access, player, Blocks.ENCHANTING_TABLE);
    }

    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if (index == 0) {
                if (!this.moveItemStackTo(itemStack2, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index == 1) {
                if (!this.moveItemStackTo(itemStack2, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index == 2) {
                if (!this.moveItemStackTo(itemStack2, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (itemStack2.is(Items.LAPIS_LAZULI)) {
                if (!this.moveItemStackTo(itemStack2, 1, 2, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (itemStack2.is(Items.AMETHYST_SHARD)) {
                if (!this.moveItemStackTo(itemStack2, 2, 3, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (((Slot)this.slots.get(0)).hasItem() || !((Slot)this.slots.get(0)).mayPlace(itemStack2)) {
                    return ItemStack.EMPTY;
                }

                ItemStack itemStack3 = itemStack2.copyWithCount(1);
                itemStack2.shrink(1);
                ((Slot)this.slots.get(0)).setByPlayer(itemStack3);
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
}
