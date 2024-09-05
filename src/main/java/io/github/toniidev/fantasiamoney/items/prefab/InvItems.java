package io.github.toniidev.fantasiamoney.items.prefab;

import io.github.toniidev.fantasiamoney.enums.ItemType;
import io.github.toniidev.fantasiamoney.helpers.ItemHelper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class InvItems {
    public static ItemStack whiteGlass = ItemHelper
            .createItem(Material.WHITE_STAINED_GLASS_PANE, " ", ItemType.DEFAULT);
    public static ItemStack blackGlass = ItemHelper
            .createItem(Material.BLACK_STAINED_GLASS_PANE, " ", ItemType.DEFAULT);
}
