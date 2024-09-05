package io.github.toniidev.fantasiamoney.helpers;

import io.github.toniidev.fantasiamoney.enums.ItemType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ItemHelper {
    public static ItemStack createItem(Material material, String name, ItemType type){
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.setDisplayName(name);

        if(!type.equals(ItemType.DEFAULT)){
            itemMeta.setLore(Collections.singletonList("§r§f§l" +
                    String.valueOf(type)
                            .toUpperCase()
                            .replace("ItemType", "")));
        }

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static boolean isCustomItem(ItemStack itemStack){
        return !getLastLoreLine(itemStack).endsWith("MINECRAFT");
    }

    public static String getLastLoreLine(ItemStack itemStack){
        return itemStack
                .getItemMeta()
                .getLore()
                .get(itemStack.getItemMeta().getLore()
                        .size() - 1);
    }

    public static ItemStack setLore(ItemStack itemStack, String... lore){
        String finalLine = getLastLoreLine(itemStack);
        ItemMeta meta = itemStack.getItemMeta();

        List<String> actualLore = new ArrayList<>(Arrays.asList(lore));
        actualLore.add(finalLine);

        meta.setLore(actualLore);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public static ItemStack setUnsafeLore(ItemStack itemStack, String... lore){
        ItemMeta meta = itemStack.getItemMeta();
        meta.setLore(Arrays.asList(lore));
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
