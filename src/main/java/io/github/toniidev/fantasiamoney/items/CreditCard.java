package io.github.toniidev.fantasiamoney.items;

import io.github.toniidev.fantasiamoney.enums.CCStatus;
import io.github.toniidev.fantasiamoney.enums.GlassType;
import io.github.toniidev.fantasiamoney.enums.InputType;
import io.github.toniidev.fantasiamoney.enums.ItemType;
import io.github.toniidev.fantasiamoney.helpers.ItemHelper;
import io.github.toniidev.fantasiamoney.interfaces.MessageHandler;
import io.github.toniidev.fantasiamoney.services.ChatListener;
import io.github.toniidev.fantasiamoney.services.InventoryBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class CreditCard implements Listener, MessageHandler {
    private String number;
    private String pwd;
    private Double money;
    private UUID owner;
    private CCStatus status;
    private Plugin plugin;
    private Inventory inv;

    //private static List<Inventory> invs = new ArrayList<>();
    private static Map<Inventory, CreditCard> invs = new HashMap<>();
    private static Map<ItemStack, CreditCard> db = new HashMap<>();

    public CreditCard(Plugin p){
        plugin = p;
    }

    public CreditCard create(){
        Random rnd = new Random();

        number = "5333 " +
                Math.addExact(rnd.nextInt(8999), 1000) +
                " " + Math.addExact(rnd.nextInt(8999), 1000) +
                " " + Math.addExact(rnd.nextInt(8999), 1000);

        money = 0.0;
        status = CCStatus.INACTIVE;

        return this;
    }

    public ItemStack toItemStack(){
        ItemStack stack = ItemHelper.createItem(Material.NETHER_BRICK, "§r§bCarta di credito", ItemType.PLUGIN);
        ItemHelper.setLore(stack,
                "§r§7Numero: §r§f" + number, "§r§7Stato: §r" +
                        (status.equals(CCStatus.ACTIVE) ? "§aAttiva" : "§cInattiva"),
                " ",
                "§r§6Gestione carta §r§e§lTASTO DESTRO",
                " ");

        db.put(stack, this);

        return stack;
    }

    private CreditCard getCreditCardFromPlayer(Player p){
        ItemStack itemInMainHand = p.getInventory().getItemInMainHand();
        if(!isCreditCard(itemInMainHand)) return null;

        /*return db.entrySet()
                .stream()
                .filter(x -> x.getKey().equals(itemInMainHand))
                .findFirst().orElse(null).getValue();*/
        return db.get(itemInMainHand);
    }

    public void activate(UUID player, String password){
        CreditCard creditCard = getCreditCardFromPlayer(Bukkit.getPlayer(player));

        creditCard.status = CCStatus.ACTIVE;
        creditCard.owner = player;
        creditCard.pwd = password;
    }

    public static boolean isCreditCard(ItemStack itemStack){
        return itemStack.getType().equals(Material.NETHER_BRICK)
                && ItemHelper.getLastLoreLine(itemStack).toUpperCase().endsWith("PLUGIN");
    }

    public static boolean isActive(ItemStack itemStack){
        if(!isCreditCard(itemStack)) return false;
        return itemStack
                .getItemMeta()
                .getLore()
                .get(1)
                .endsWith("Attiva");
    }

    private void refresh(Player p){
        CreditCard card = getCreditCardFromPlayer(p);

        // items
        ItemStack sign = ItemHelper.createItem(Material.OAK_SIGN,
                "§r§aPassword", ItemType.DEFAULT);
        ItemHelper.setUnsafeLore(sign,
                "§r§7Scegli una password per la",
                "§r§7tua carta di credito.", " ",
                "§r§7Password: §e" + card.pwd, " ", "§eClicca per sceglierne una!");

        ItemStack close = ItemHelper.createItem(Material.RED_STAINED_GLASS_PANE,
                "§r§cChiudi", ItemType.DEFAULT);

        ItemStack confirm = ItemHelper.createItem(Material.GREEN_STAINED_GLASS_PANE,
                "§r§aConferma", ItemType.DEFAULT);
        ItemHelper.setUnsafeLore(confirm,
                "§r§7La password verrà associata",
                "§r§7permanentemente a questa carta");

        Map<Integer, ItemStack> items = new HashMap<>();
        items.put(4, sign);
        items.put(7, close);
        items.put(8, confirm);

        inv = new InventoryBuilder(plugin).create(9, "Gestione")
                .disableClicks()
                .handleInput(4)
                .addItems(items)
                .fillWithGlass(GlassType.WHITE)
                .get();

        invs.put(inv, card);
    }

    @EventHandler
    public void setup(PlayerInteractEvent e){
        ItemStack itemStack = e.getPlayer().getInventory().getItemInMainHand();

        if(!isCreditCard(itemStack)) return;
        if(isActive(itemStack)) return;

        refresh(e.getPlayer());
        e.getPlayer().openInventory(inv);
    }

    @Override
    public void pinHandler(String message, Player sender) {
        if(!ChatListener.getList(InputType.CREDIT_CARD_PASSWORD).contains(sender)) return;

        CreditCard card = getCreditCardFromPlayer(sender);
        card.pwd = message;
        refresh(sender);
        sender.openInventory(inv);

        ChatListener.stopWatchingPlayer(InputType.CREDIT_CARD_PASSWORD, sender);
    }

    // handle inv
    @EventHandler
    public void close(InventoryClickEvent e){
        //if(!e.getClickedInventory().equals(inv)) return;
        if(!invs.containsKey(e.getClickedInventory())) return;
        if(e.getRawSlot() != 7) return;

        CreditCard card = getCreditCardFromPlayer((Player) e.getWhoClicked());
        card.pwd = null;

        e.getWhoClicked().closeInventory();
    }

    @EventHandler
    public void confirm(InventoryClickEvent e){
        //if(!e.getClickedInventory().equals(inv)) return;
        if(!invs.containsKey(e.getClickedInventory())) return;
        if(e.getRawSlot() != 8) return;

        /*CreditCard card = invs.entrySet()
                .stream()
                .filter(x -> x.getKey()
                        .equals(e.getClickedInventory()))
                .findFirst()
                .orElse(null)
                .getValue();*/

        CreditCard card = getCreditCardFromPlayer((Player) e.getWhoClicked());

        String password = card.getPwd();
        if(password == null) return;

        activate(e.getWhoClicked().getUniqueId(), password);

        e.getWhoClicked()
                .getInventory()
                .setItem(e.getWhoClicked()
                        .getInventory()
                        .getHeldItemSlot(), card.toItemStack());

        e.getWhoClicked().closeInventory();
    }

    public void disable(){
        status = CCStatus.INACTIVE;
    }

    public void load(double amount){
        money += amount;
    }

    public void charge(double amount){
        money -= amount;
    }

    public String getNumber() {
        return number;
    }

    public String getPwd() {
        return pwd;
    }

    public Double getMoney() {
        return money;
    }

    public UUID getOwner() {
        return owner;
    }

    public CCStatus getStatus(){
        return status;
    }

    public void setStatus(CCStatus s){
        status = s;
    }
}
