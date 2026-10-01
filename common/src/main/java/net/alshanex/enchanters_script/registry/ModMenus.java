package net.alshanex.enchanters_script.registry;

import net.alshanex.enchanters_script.Constants;
import net.alshanex.enchanters_script.book.BookMenu;
import net.alshanex.enchanters_script.enchanting.EnchantersTableMenu;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class ModMenus {
    public static final MenuType<EnchantersTableMenu> ENCHANTING_TABLE =
            new MenuType<>(EnchantersTableMenu::new, FeatureFlags.VANILLA_SET);

    public static final ResourceLocation ENCHANTING_TABLE_ID =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "enchanting_table");

    public static final MenuType<BookMenu> BOOK =
            new MenuType<>(BookMenu::new, FeatureFlags.VANILLA_SET);

    public static final ResourceLocation BOOK_ID =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "book");

    private ModMenus() {
    }
}
