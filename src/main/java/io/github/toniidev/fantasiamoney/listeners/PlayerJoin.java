package io.github.toniidev.fantasiamoney.listeners;

import io.github.toniidev.fantasiamoney.items.CreditCard;
import io.github.toniidev.fantasiamoney.items.prefab.MoneyInstances;
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

        for(ItemStack itemStack : MoneyInstances.getAll()){
            e.getPlayer().getInventory().addItem(itemStack);
        }

        e.getPlayer().getInventory().addItem(new CreditCard(plugin).create().toItemStack());
    }
}
