package io.github.toniidev.fantasiamoney.services;

import io.github.toniidev.fantasiamoney.enums.InputType;
import io.github.toniidev.fantasiamoney.enums.GlassType;
import io.github.toniidev.fantasiamoney.items.prefab.InvItems;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryBuilder implements Listener {
    private Inventory inv;

    private Plugin plugin;

    private int handleStartSlot;
    private InputType inputType;
    private Player playerToWatch;

    private static Map<Inventory, Integer> invsToHandle = new HashMap<>();
    private static List<Inventory> invsToDisableClicks = new ArrayList<>();
    private static List<Inventory> invsToDisableClose = new ArrayList<>();

    public InventoryBuilder(Plugin p){
        plugin = p;
    }

    public InventoryBuilder create(int size, String title){
        inv = Bukkit.createInventory(null, size, title);
        return this;
    }

    public InventoryBuilder addItems(Map<Integer, ItemStack> items){
        for(Map.Entry<Integer, ItemStack> item : items.entrySet()){
            inv.setItem(item.getKey(), item.getValue());
        }
        return this;
    }

    public InventoryBuilder fillWithGlass(GlassType type){
        for(int i = 0; i < inv.getSize(); i++){
            if(type.equals(GlassType.WHITE)){
                if(inv.getItem(i) == null) inv.setItem(i, InvItems.whiteGlass);
            }

            if(type.equals(GlassType.BLACK)){
                if(inv.getItem(i) == null) inv.setItem(i, InvItems.blackGlass);
            }

            if(type.equals(GlassType.MIXED)){
                if(inv.getItem(i) == null){
                    if(i % 2 == 0) inv.setItem(i, InvItems.blackGlass);
                    else inv.setItem(i, InvItems.whiteGlass);
                    // if((i & 1) == 0)
                }
            }
        }

        return this;
    }

    public InventoryBuilder handleInput(int handleSlot){
        handleStartSlot = handleSlot;
        invsToHandle.put(inv, handleStartSlot);
        return this;
    }

    public InventoryBuilder disableClicks(){
        invsToDisableClicks.add(inv);
        return this;
    }

    public InventoryBuilder disableClose(){
        invsToDisableClose.add(inv);
        return this;
    }

    public void show(Player p){
        p.openInventory(inv);
    }

    public Inventory get(){
        return inv;
    }

    @EventHandler
    public void makeCloseImpossible(InventoryCloseEvent e){
        if(!invsToDisableClose.contains(e.getInventory())) return;

        new BukkitRunnable(){
            @Override
            public void run() {
                e.getPlayer().openInventory(inv);
            }
        }.runTaskLater(plugin, 1);
    }

    @EventHandler
    public void disableClicks(InventoryClickEvent e){
        if(!invsToDisableClicks.contains(e.getClickedInventory())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void startHandling(InventoryClickEvent e){
        if(!invsToHandle.containsKey(e.getClickedInventory())) return;
        if(e.getRawSlot() != invsToHandle
                .entrySet()
                .stream()
                .filter(x -> x.getKey().equals(e.getClickedInventory()))
                .findFirst()
                .orElse(null).getValue()) return;

        playerToWatch = (Player) e.getWhoClicked();
        ChatListener.startWatchingPlayer(InputType.CREDIT_CARD_PASSWORD, playerToWatch);
        e.getWhoClicked().closeInventory();
    }
}
