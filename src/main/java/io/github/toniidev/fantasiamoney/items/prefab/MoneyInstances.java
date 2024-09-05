package io.github.toniidev.fantasiamoney.items.prefab;

import io.github.toniidev.fantasiamoney.enums.ItemType;
import io.github.toniidev.fantasiamoney.enums.MoneyAmount;
import io.github.toniidev.fantasiamoney.helpers.ItemHelper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoneyInstances {
    private static HashMap<MoneyAmount, ItemStack> moneyInstances = new HashMap<>();

    public static void initialize(){
        moneyInstances.put(MoneyAmount.FIFTY_CENTS, ItemHelper.createItem(Material.IRON_NUGGET,
                "§r0.50€", ItemType.VALUTA));
        moneyInstances.put(MoneyAmount.ONE_EURO, ItemHelper.createItem(Material.GOLD_NUGGET,
                "§r1€", ItemType.VALUTA));
        moneyInstances.put(MoneyAmount.TWO_EUROS, ItemHelper.createItem(Material.COPPER_INGOT,
                "§r2€", ItemType.VALUTA));
        moneyInstances.put(MoneyAmount.FIVE_EUROS, ItemHelper.createItem(Material.COAL,
                "§r5€", ItemType.VALUTA));
        moneyInstances.put(MoneyAmount.TEN_EUROS, ItemHelper.createItem(Material.IRON_INGOT,
                "§r10€", ItemType.VALUTA));
        moneyInstances.put(MoneyAmount.TWENTY_EUROS, ItemHelper.createItem(Material.GOLD_INGOT,
                "§r20€", ItemType.VALUTA));
        moneyInstances.put(MoneyAmount.FIFTY_EUROS, ItemHelper.createItem(Material.LAPIS_LAZULI,
                "§r50€", ItemType.VALUTA));
        moneyInstances.put(MoneyAmount.ONE_HUNDRED_EUROS, ItemHelper.createItem(Material.PRISMARINE_SHARD,
                "§r100€", ItemType.VALUTA));
        moneyInstances.put(MoneyAmount.FIVE_HUNDRED_EUROS, ItemHelper.createItem(Material.EMERALD,
                "§r500€", ItemType.VALUTA));
    }

    public static ItemStack get(MoneyAmount amount){
        return moneyInstances.get(amount);
    }

    public static List<ItemStack> getAll(){
        List<ItemStack> itemStacks = new ArrayList<>();

        for(Map.Entry<MoneyAmount, ItemStack> e : moneyInstances.entrySet()){
            itemStacks.add(e.getValue());
        }

        return itemStacks;
    }
}
