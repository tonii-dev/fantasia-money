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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MoneyInstances {
    private static HashMap<MoneyAmount, ItemStack> moneyInstances = new HashMap<>();
    private static Map<Material, Double> real = new HashMap<>();

    public static Map<Material, Double> getReal() {
        return real;
    }

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
        moneyInstances.put(MoneyAmount.ONE_HUNDRED_EUROS, ItemHelper.createItem(Material.NETHER_BRICK,
                "§r100€", ItemType.VALUTA));
        moneyInstances.put(MoneyAmount.FIVE_HUNDRED_EUROS, ItemHelper.createItem(Material.EMERALD,
                "§r500€", ItemType.VALUTA));

        Pattern pattern = Pattern.compile("\\d+(\\.\\d+)?");

        for(Map.Entry<MoneyAmount, ItemStack> entry : moneyInstances.entrySet()){
            Matcher matcher = pattern.matcher(entry.getValue()
                    .getItemMeta().getDisplayName());

            if(matcher.find()){
                real.put(entry.getValue().getType(),
                        Double.parseDouble(matcher.group()));
            }
        }
    }

    public static ItemStack get(MoneyAmount amount){
        return moneyInstances.get(amount);
    }

    public static List<ItemStack> getExact(double amount, List<ItemStack> actual){
        List<ItemStack> items = actual;

        if(amount >= 500){
            amount -= 500;
            items.add(MoneyInstances.get(MoneyAmount.FIVE_HUNDRED_EUROS));
            return getExact(amount, items);
        }
        else if (amount >= 100){
            amount -= 100;
            items.add(MoneyInstances.get(MoneyAmount.ONE_HUNDRED_EUROS));
            return getExact(amount, items);
        }
        else if(amount >= 50){
            amount -= 50;
            items.add(MoneyInstances.get(MoneyAmount.FIFTY_EUROS));
            return getExact(amount, items);
        }
        else if(amount >= 20){
            amount -= 20;
            items.add(MoneyInstances.get(MoneyAmount.TWENTY_EUROS));
            return getExact(amount, items);
        }
        else if(amount >= 10){
            amount -= 10;
            items.add(MoneyInstances.get(MoneyAmount.TEN_EUROS));
            return getExact(amount, items);
        }
        else if(amount >= 5){
            amount -= 5;
            items.add(MoneyInstances.get(MoneyAmount.FIVE_EUROS));
            return getExact(amount, items);
        }
        else if(amount >= 2){
            amount -= 2;
            items.add(MoneyInstances.get(MoneyAmount.TWO_EUROS));
            return getExact(amount, items);
        }
        else if(amount >= 1){
            amount -= 1;
            items.add(MoneyInstances.get(MoneyAmount.ONE_EURO));
            return getExact(amount, items);
        }
        else if(amount >= 0.50){
            amount -= 0.50;
            items.add(MoneyInstances.get(MoneyAmount.FIFTY_CENTS));
            return getExact(amount, items);
        }

        return items;
    }

    public static List<ItemStack> getAll(){
        List<ItemStack> itemStacks = new ArrayList<>();

        for(Map.Entry<MoneyAmount, ItemStack> e : moneyInstances.entrySet()){
            itemStacks.add(e.getValue());
        }

        return itemStacks;
    }
}
