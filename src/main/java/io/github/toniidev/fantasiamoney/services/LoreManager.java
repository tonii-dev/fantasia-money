package io.github.toniidev.fantasiamoney.services;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChangedMainHandEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class LoreManager implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        addLoreToItems(event.getPlayer().getInventory().getContents());
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        addLoreToItem(event.getItem().getItemStack());
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            addLoreToItem(event.getItem().getItemStack());
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        addLoreToItem(event.getItemDrop().getItemStack());
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        addLoreToItem(event.getCurrentItem());
    }

    @EventHandler
    public void onHotbarChange(PlayerChangedMainHandEvent event){
        addLoreToItem(event.getPlayer().getItemInUse());
    }

    private void addLoreToItems(ItemStack[] items) {
        for (ItemStack item : items) {
            if (item != null) {
                addLoreToItem(item);
            }
        }
    }

    private void addLoreToItem(ItemStack item) {
        if (item != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                if(meta.getLore() == null){
                    meta.setLore(Collections.singletonList("§r§f§lMINECRAFT"));
                    item.setItemMeta(meta);
                }
            }
        }
    }
}
