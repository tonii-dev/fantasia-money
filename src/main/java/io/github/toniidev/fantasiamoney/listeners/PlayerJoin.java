package io.github.toniidev.fantasiamoney.listeners;

import io.github.toniidev.fantasiamoney.enums.BlockType;
import io.github.toniidev.fantasiamoney.enums.MoneyAmount;
import io.github.toniidev.fantasiamoney.items.CreditCard;
import io.github.toniidev.fantasiamoney.items.prefab.MoneyInstances;
import io.github.toniidev.fantasiamoney.services.CustomBlockBuilder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class PlayerJoin implements Listener {
    private Plugin plugin;

    public PlayerJoin(org.bukkit.plugin.Plugin p){
        plugin = p;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        MoneyInstances.initialize();

        /*for(ItemStack itemStack : MoneyInstances.getAll()){
            e.getPlayer().getInventory().addItem(itemStack);
        }*/

        new CustomBlockBuilder()
                .create(BlockType.ATM_LOWER)
                .give(e.getPlayer());
        new CustomBlockBuilder()
                .create(BlockType.ATM_UPPER)
                .addClickTooltip()
                .setCustomText("§r§bATM")
                .give(e.getPlayer());
        e.getPlayer().getInventory().addItem(new CreditCard(plugin).create().toItemStack());

        for(ItemStack i : MoneyInstances.getAll()){
            ItemStack s = i.clone();
            s.setAmount(5);
            e.getPlayer().getInventory().addItem(s);
        }
    }
}
