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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.*;

public class InventoryBuilder implements Listener {
    private Inventory inv;
    private final Plugin plugin;

    private static final Map<Inventory, InventoryBuilder> info = new HashMap<>();
    private static final Map<Inventory, Map<Integer, InputType>> invsToHandle = new HashMap<>();
    private static final List<Inventory> invsToDisableClicks = new ArrayList<>();
    private static final List<Inventory> invsToDisableClose = new ArrayList<>();
    private static final Map<InputType, String[]> titles = new HashMap<>();

    public InventoryBuilder(Plugin plugin) {
        this.plugin = plugin;

        String[] ccp = new String[]{"Inserisci password", "Scrivi in chat la password della tua carta di credito"};
        String[] bca = new String[]{"Scegli l'importo", "Scrivi in chat la somma che vuoi prelevare"};
        String[] cps = new String[]{"Scegli password", "Scrivi in chat una password per la tua carta di credito"};

        titles.put(InputType.CREDIT_CARD_PASSWORD, cps);
        titles.put(InputType.ATM_WITHDRAW_PASSWORD, ccp);
        titles.put(InputType.ATM_DEPOSIT_PASSWORD, ccp);
        titles.put(InputType.ATM_CHOOSE_AMOUNT, bca);
    }

    public InventoryBuilder create(int size, String title) {
        inv = Bukkit.createInventory(null, size, title);
        return this;
    }

    public InventoryBuilder addItems(Map<Integer, ItemStack> items) {
        items.forEach(inv::setItem);
        return this;
    }

    public InventoryBuilder fillWithGlass(GlassType type) {
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                if (type == GlassType.WHITE) {
                    inv.setItem(i, InvItems.whiteGlass);
                } else if (type == GlassType.BLACK) {
                    inv.setItem(i, InvItems.blackGlass);
                } else if (type == GlassType.MIXED) {
                    inv.setItem(i, (i % 2 == 0) ? InvItems.blackGlass : InvItems.whiteGlass);
                }
            }
        }
        return this;
    }

    public InventoryBuilder handleInput(Map<Integer, InputType> map) {
        invsToHandle.put(inv, map);
        return this;
    }

    public InventoryBuilder disableClicks() {
        invsToDisableClicks.add(inv);
        return this;
    }

    public InventoryBuilder disableClose() {
        invsToDisableClose.add(inv);
        return this;
    }

    public static InventoryBuilder getBuilder(Inventory inv) {
        return info.get(inv);
    }

    public void show(Player player) {
        player.openInventory(inv);
    }

    public Inventory get() {
        info.put(inv, this);
        return inv;
    }

    @EventHandler
    public void makeCloseImpossible(InventoryCloseEvent event) {
        if (!invsToDisableClose.contains(event.getInventory())) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                event.getPlayer().openInventory(inv);
            }
        }.runTaskLater(plugin, 1);
    }

    @EventHandler
    public void disableClicks(InventoryClickEvent event) {
        if (invsToDisableClicks.contains(event.getClickedInventory())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void startHandling(InventoryClickEvent event) {
        Inventory clicked = event.getClickedInventory();

        //if (invsToHandle.containsKey(clicked) && invsToHandle.get(clicked).containsKey(event.getRawSlot()))

        if (invsToHandle.containsKey(clicked) && invsToHandle.get(clicked).containsKey(event.getRawSlot())) {
            Player p = (Player) event.getWhoClicked();
            ChatManager.startWatchingPlayer(invsToHandle.get(clicked).get(event.getRawSlot()), p);
            event.getWhoClicked().closeInventory();

            String title = titles.entrySet().stream()
                            .filter(x -> x.getKey().equals(invsToHandle.get(clicked).get(event.getRawSlot())))
                                    .findFirst()
                                            .orElse(null)
                                                    .getValue()[0];

            String subtitle = titles.entrySet().stream()
                            .filter(x -> x.getKey().equals(invsToHandle.get(clicked).get(event.getRawSlot())))
                                    .findFirst()
                                            .orElse(null)
                                                    .getValue()[1];

            p.sendTitle(title, subtitle, 1, 1000, 1);
        }
    }

    @EventHandler
    public void disableHotbarSwap(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (ChatManager.isBeingWatched(player)) {
            event.setCancelled(true);
            player.sendMessage("§r§e[FUNZIONE]§r§a Input:§r§f Non puoi cambiare oggetto se ti è stato chiesto un input in chat!!");
        }
    }

    @EventHandler
    public void disableItemSwap(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (ChatManager.isBeingWatched(player)) {
            event.setCancelled(true);
            player.sendMessage("§r§e[FUNZIONE]§r§a Input:§r§f Non puoi cambiare oggetto se ti è stato chiesto un input in chat!!");
        }
    }

    @EventHandler
    public void disableInteractions(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (ChatManager.isBeingWatched(player)) {
            event.setCancelled(true);
            player.sendMessage("§r§e[FUNZIONE]§r§a Input:§r§f Non puoi cambiare oggetto se ti è stato chiesto un input in chat!!");
        }
    }
}
