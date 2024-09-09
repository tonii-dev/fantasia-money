package io.github.toniidev.fantasiamoney.blocks;

import io.github.toniidev.fantasiamoney.enums.BlockType;
import io.github.toniidev.fantasiamoney.enums.GlassType;
import io.github.toniidev.fantasiamoney.enums.InputType;
import io.github.toniidev.fantasiamoney.enums.ItemType;
import io.github.toniidev.fantasiamoney.helpers.ItemHelper;
import io.github.toniidev.fantasiamoney.interfaces.MessageHandler;
import io.github.toniidev.fantasiamoney.items.CreditCard;
import io.github.toniidev.fantasiamoney.items.prefab.MoneyInstances;
import io.github.toniidev.fantasiamoney.services.ChatManager;
import io.github.toniidev.fantasiamoney.services.CustomBlockBuilder;
import io.github.toniidev.fantasiamoney.services.DiscordWebhook;
import io.github.toniidev.fantasiamoney.services.InventoryBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ATM implements Listener, MessageHandler {
    private final Plugin plugin;

    private final static Map<Player, Inventory> deposits = new HashMap<>();
    private final static Map<Player, Double> withdrawals = new HashMap<>();

    public ATM(Plugin p){
        plugin = p;
    }

    private Inventory createHomeGUI(CreditCard cc){
        Inventory inv;

        ItemStack withdraw = ItemHelper.createItem(Material.ORANGE_STAINED_GLASS_PANE,
                "§r§6Preleva", ItemType.DEFAULT);
        ItemHelper.setUnsafeLore(withdraw,
                "§r§7Preleva soldi da questa",
                "§r§bcarta di credito§r§7 in",
                "§r§7tempo reale.", " ",
                "§r§eClicca per inserire la password!");

        ItemStack deposit = ItemHelper.createItem(Material.CYAN_STAINED_GLASS_PANE,
                "§aDeposita", ItemType.DEFAULT);
        ItemHelper.setUnsafeLore(deposit,
                "§r§7Deposita soldi su questa",
                "§r§bcarta di credito§r§7 in",
                "§r§7tempo reale.", " ",
                "§r§7Il bancomat tratterrà il §r§c5%",
                "§r§7della somma depositata.", " ",
                "§r§eClicca per inserire la password!");

        Map<Integer, ItemStack> items = new HashMap<>();
        items.put(2, withdraw);
        items.put(4, deposit);
        items.put(6, cc.getTransactionsItemStack());

        Map<Integer, InputType> handling = new HashMap<>();
        handling.put(2, InputType.ATM_WITHDRAW_PASSWORD);
        handling.put(4, InputType.ATM_DEPOSIT_PASSWORD);

        inv = new InventoryBuilder(plugin)
                .create(9, "ATM")
                .disableClicks()
                .addItems(items)
                .fillWithGlass(GlassType.WHITE)
                .handleInput(handling)
                .get();

        return inv;
    }

    private Inventory createDepositGUI(){
        Inventory inv;

        ItemStack tooltip = ItemHelper.createItem(Material.GRAY_STAINED_GLASS_PANE,
                "§r§bInserisci banconota", ItemType.DEFAULT);
        ItemHelper.setUnsafeLore(tooltip,
                "§r§7Clicca su una banconota",
                "§r§7per depositarla");

        ItemStack confirm = ItemHelper.createItem(Material.GREEN_STAINED_GLASS_PANE,
                "§r§aConferma", ItemType.DEFAULT);
        ItemHelper.setUnsafeLore(confirm,
                "§r§7Deposita la banconota");

        ItemStack close = ItemHelper.createItem(Material.RED_STAINED_GLASS_PANE,
                "§r§cIndietro", ItemType.DEFAULT);

        Map<Integer, ItemStack> items = new HashMap<>();
        items.put(0, close);
        items.put(4, tooltip);
        items.put(8, confirm);

        inv = new InventoryBuilder(plugin)
                .create(9, "Deposita")
                .disableClicks()
                .addItems(items)
                .fillWithGlass(GlassType.WHITE)
                .get();

        return inv;
    }

    private Inventory createWithdrawGUI(@Nullable Double amount){
        Inventory inv;

        ItemStack close = ItemHelper.createItem(Material.RED_STAINED_GLASS_PANE,
                "§r§cIndietro", ItemType.DEFAULT);

        ItemStack confirm = ItemHelper.createItem(Material.GREEN_STAINED_GLASS_PANE,
                "§r§aConferma", ItemType.DEFAULT);
        ItemHelper.setUnsafeLore(confirm,
                "§r§7Preleva la somma desiderata");

        ItemStack input = ItemHelper.createItem(Material.CYAN_STAINED_GLASS_PANE,
                "§r§bSeleziona importo", ItemType.DEFAULT);
        ItemHelper.setUnsafeLore(input,
                "§r§7Scegli l'importo che",
                "§r§7vuoi prelevare", " ",
                "§r§7Importo scelto: §e" + (amount == null ? 0.0 : amount) + "€", " ", "§eClicca per scegliere!");

        Map<Integer, ItemStack> items = new HashMap<>();
        items.put(0, close);
        items.put(4, input);
        items.put(8, confirm);

        Map<Integer, InputType> handling = new HashMap<>();
        handling.put(4, InputType.ATM_CHOOSE_AMOUNT);

        inv = new InventoryBuilder(plugin)
                .create(9, "Preleva")
                .handleInput(handling)
                .addItems(items)
                .fillWithGlass(GlassType.WHITE)
                .disableClicks()
                .get();

        return inv;
    }

    /*@EventHandler
    public void deposit(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();

        if (!deposits.containsKey(p)) return;

        if(e.getClickedInventory() == null) return;
        if(e.getCurrentItem() == null) return;
        if(e.getClickedInventory().equals(deposits.get(p))) return;

        e.setCancelled(true);

        ItemStack clickedItem = e.getCurrentItem();
        if (!ItemHelper.isCustomItem(clickedItem) ||
                !ItemHelper.getLastLoreLine(e.getCurrentItem()).toUpperCase().endsWith("VALUTA")) return;

        ItemStack s = clickedItem.clone();
        ItemStack eventual = s.clone();
        eventual.setAmount(s.getAmount() - 1);

        ItemStack nis = s.getAmount() - 1 == 0 ? new ItemStack(Material.AIR) : eventual;

        e.getClickedInventory().setItem(e.getSlot(), nis);
        if (deposits.get(p).getItem(4).getType().equals(s.getType())) {
            s.setAmount(s.getAmount() + 1);
        }
        else s.setAmount(1);
        deposits.get(p).setItem(4, s);

        double amount = MoneyInstances.getReal().get(s.getType()) * s.getAmount();
        double finalAmount = amount * 0.95;

        ItemStack confirm = ItemHelper.createItem(Material.GREEN_STAINED_GLASS_PANE, "§r§aConferma", ItemType.DEFAULT);
        ItemHelper.setUnsafeLore(confirm, "§r§eSomma effettiva: §r§b" + finalAmount + "€ §r§7[" + amount + "€ - §c5%§7]");
        deposits.get(p).setItem(8, confirm);
    }*/

    @EventHandler
    public void deposit(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();

        if (!deposits.containsKey(p)) return;
        if(e.getClickedInventory() == null) return;
        if(e.getCurrentItem() == null) return;
        if(!e.getView().getTitle().equals("Deposita")) return;

        Inventory atm = deposits.get(p);

        if(e.getClickedInventory().equals(atm)) return;

        e.setCancelled(true);

        ItemStack clickedItem = e.getCurrentItem();
        if (!ItemHelper.isMoney(clickedItem)) return;

        if(atm.getItem(4).getType().equals(clickedItem.getType())){
            ItemStack previous = atm.getItem(4);
            previous.setAmount(previous.getAmount() + 1);
        }
        else{
            ItemStack current = atm.getItem(4);
            if(ItemHelper.isMoney(current)) e.getWhoClicked().getInventory().addItem(atm.getItem(4));
            ItemStack next = clickedItem.clone();
            next.setAmount(1);
            atm.setItem(4, next);
        }

        clickedItem.setAmount(clickedItem.getAmount() - 1);

        ItemStack s = atm.getItem(4);

        /*ItemStack s = clickedItem.clone();
        ItemStack eventual = s.clone();
        eventual.setAmount(s.getAmount() - 1);

        ItemStack nis = s.getAmount() - 1 == 0 ? new ItemStack(Material.AIR) : eventual;

        e.getClickedInventory().setItem(e.getSlot(), nis);
        if (deposits.get(p).getItem(4).getType().equals(s.getType())) {
            s.setAmount(s.getAmount() + 1);
        }
        else s.setAmount(1);
        deposits.get(p).setItem(4, s);*/

        double amount = MoneyInstances.getReal().get(s.getType()) * s.getAmount();
        double finalAmount = amount * 0.95;

        ItemStack confirm = ItemHelper.createItem(Material.GREEN_STAINED_GLASS_PANE, "§r§aConferma", ItemType.DEFAULT);
        ItemHelper.setUnsafeLore(confirm, "§r§eSomma effettiva: §r§b" + finalAmount + "€ §r§7[" + amount + "€ - §c5%§7]");
        deposits.get(p).setItem(8, confirm);
    }

    @EventHandler
    public void withdrawConfirm(InventoryClickEvent e) throws IOException {
        if (e.getClickedInventory() == null) return;

        Player p = (Player) e.getWhoClicked();
        Double amount = withdrawals.get(p);

        if (!withdrawals.containsKey(p) || e.getRawSlot() != 8 || amount == null) return;

        CreditCard card = CreditCard.getCreditCardFromPlayer(p);
        String loc = Math.round(p.getLocation().getX()) + ", " + Math.round(p.getLocation().getY()) + ", " + Math.round(p.getLocation().getZ());
        DiscordWebhook wh = new DiscordWebhook("https://discord.com/api/webhooks/1281979191801151540/RLF4EYbFEoXnrNScp7pp2z6j6ERvLMBlLsVb4YEl7ylLB6clTh2v4WLpqosnxUkfctaE");

        if (card.getMoney() < amount) {
            p.closeInventory();
            p.sendMessage("§r§e[GIOCO]§r§a Bancomat:§r§f Somma maggiore della disponibilità.");
            wh.addEmbed(new DiscordWebhook.EmbedObject()
                    .setTitle("Prelievo")
                    .addField("Coordinate", loc, false)
                    .addField("Numero carta", card.getNumber(), false)
                    .addField("Somma", amount + "€", true)
                    .addField("Stato", "Non riuscito", true)
                    .setColor(Color.red)
                    .setImage("https://mineskin.eu/helm/" + Bukkit.getPlayer(card.getOwner()).getDisplayName() + "/100.png")
                    .setFooter("Le telecamere hanno fotografato", "https://mineskin.eu/helm/" + p.getDisplayName() + "/100.png"));
        } else {
            p.closeInventory();
            card.charge(amount);
            for (ItemStack s : MoneyInstances.getExact(amount, new ArrayList<>())) {
                p.getInventory().addItem(s);
            }
            p.sendMessage("§r§e[GIOCO]§r§a Bancomat:§r§f Prelievo eseguito con successo.");
            wh.addEmbed(new DiscordWebhook.EmbedObject()
                    .setTitle("Prelievo")
                    .addField("Coordinate", loc, false)
                    .addField("Numero carta", card.getNumber(), false)
                    .addField("Somma", amount + "€", true)
                    .addField("Stato", "Riuscito", true)
                    .setColor(Color.green)
                    .setImage("https://mineskin.eu/helm/" + Bukkit.getPlayer(card.getOwner()).getDisplayName() + "/100.png")
                    .setFooter("Le telecamere hanno fotografato", "https://mineskin.eu/helm/" + p.getDisplayName() + "/100.png"));
        }

        withdrawals.remove(p);
        ChatManager.stopWatchingPlayer(p);
        wh.execute();
    }

    @EventHandler
    public void atmBack(InventoryClickEvent e){
        Player p = (Player) e.getWhoClicked();

        boolean isWithdrawing = withdrawals.containsKey(p);
        boolean isDepositing = deposits.containsKey(p);

        if(!isWithdrawing && !isDepositing) return;
        if(e.getRawSlot() != 0) return;

        CreditCard card = CreditCard.getCreditCardFromPlayer(p);
        if(card == null) return;

        p.closeInventory();
        if(isWithdrawing) withdrawals.remove(p);
        if(isDepositing) deposits.remove(p);
        p.openInventory(this.createHomeGUI(card));
    }

    @EventHandler
    public void depositConfirm(InventoryClickEvent e) throws IOException {
        if (e.getClickedInventory() == null || !deposits.containsValue(e.getClickedInventory()) || e.getRawSlot() != 8) return;

        ItemStack s = e.getClickedInventory().getItem(4);
        if (s.getType().equals(Material.GRAY_STAINED_GLASS_PANE)) return;

        double amount = MoneyInstances.getReal().get(s.getType()) * s.getAmount();
        double finalAmount = amount * 0.95;

        CreditCard card = CreditCard.getCreditCardFromPlayer((Player) e.getWhoClicked());
        ItemStack tooltip = ItemHelper.createItem(Material.GRAY_STAINED_GLASS_PANE, "§r§bInserisci banconota", ItemType.DEFAULT);
        ItemHelper.setUnsafeLore(tooltip, "§r§7Clicca su una banconota", "§r§7per depositarla");

        e.getClickedInventory().setItem(4, tooltip);

        if(card == null) return;

        Player p = (Player) e.getWhoClicked();
        p.closeInventory();
        card.load(finalAmount);
        p.sendMessage("§r§e[GIOCO]§r§a Bancomat:§r§f Carta ricaricata con successo.");

        String loc = Math.round(p.getLocation().getX()) + ", " + Math.round(p.getLocation().getY()) + ", " + Math.round(p.getLocation().getZ());
        deposits.remove(p);

        DiscordWebhook wh = new DiscordWebhook("https://discord.com/api/webhooks/1281979191801151540/RLF4EYbFEoXnrNScp7pp2z6j6ERvLMBlLsVb4YEl7ylLB6clTh2v4WLpqosnxUkfctaE");
        wh.addEmbed(new DiscordWebhook.EmbedObject()
                .setTitle("Deposito")
                .addField("Coordinate", loc, false)
                .addField("Commissioni", "5%", true)
                .addField("Stato", "Riuscito", true)
                .addField("Numero carta", card.getNumber(), false)
                .addField("Importo", amount + "€", true)
                .addField("Deposito effettivo", finalAmount + "€", true)
                .setImage("https://mineskin.eu/helm/" + Bukkit.getPlayer(card.getOwner()).getDisplayName() + "/100.png")
                .setColor(Color.green)
                .setFooter("Le telecamere hanno fotografato", "https://mineskin.eu/helm/" + p.getDisplayName() + "/100.png"));
        wh.execute();
    }

    @EventHandler
    public void depositClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();

        if(!deposits.containsKey(p)) return;

        Inventory inv = deposits.get(p);
        deposits.remove(p);

        ItemStack item = inv.getItem(4);
        if (ItemHelper.isMoney(item)) {
            p.getInventory().addItem(item);
        }
    }

    @EventHandler
    public void withdrawClose(InventoryCloseEvent e) {
        withdrawals.remove((Player) e.getPlayer());
    }

    @EventHandler
    public void open(PlayerInteractEvent e) {
        if (ChatManager.isBeingWatched(e.getPlayer()) || !e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

        Block clickedBlock = e.getClickedBlock();
        if (!CustomBlockBuilder.getBlocks().containsKey(clickedBlock)) return;

        BlockType blockType = CustomBlockBuilder.getBlocks().get(clickedBlock);
        if (!blockType.equals(BlockType.ATM_LOWER) && !blockType.equals(BlockType.ATM_UPPER)) return;

        ItemStack mainHandItem = e.getPlayer().getInventory().getItemInMainHand();
        if (!CreditCard.isCreditCard(mainHandItem) || !CreditCard.isActive(mainHandItem)) return;

        CreditCard card = CreditCard.getCreditCardFromPlayer(e.getPlayer());
        if(card == null) return;
        e.getPlayer().openInventory(this.createHomeGUI(card));
    }

    @Override
    public void pinHandler(String message, Player sender) {
        if (!ChatManager.getList().containsKey(sender)) return;

        InputType type = ChatManager.getList().get(sender);
        CreditCard card = CreditCard.getCreditCardFromPlayer(sender);

        if(card == null) return;

        if (type.equals(InputType.ATM_WITHDRAW_PASSWORD) || type.equals(InputType.ATM_DEPOSIT_PASSWORD)) {
            handlePasswordInput(message, sender, type, card);
        } else if (type.equals(InputType.ATM_CHOOSE_AMOUNT)) {
            handleAmountInput(message, sender);
        }
    }

    private void handlePasswordInput(String message, Player sender, InputType type, CreditCard card) {
        if (!message.equals(card.getPwd())) {
            ChatManager.stopWatchingPlayer(sender);
            sender.sendMessage("§r§e[GIOCO]§r§a Bancomat:§r§f Password errata.");
            sender.openInventory(this.createHomeGUI(card));
        } else {
            ChatManager.stopWatchingPlayer(sender);
            Inventory inv = type.equals(InputType.ATM_DEPOSIT_PASSWORD) ? this.createDepositGUI() : this.createWithdrawGUI(null);
            deposits.put(sender, inv);
            sender.openInventory(inv);
        }
    }

    private void handleAmountInput(String message, Player sender) {
        double amount;
        String numericString = message.replaceAll("[^0-9.]", "");

        try {
            amount = Double.parseDouble(numericString);
        } catch (NumberFormatException e) {
            sender.sendMessage("§r§e[GIOCO]§r§a Bancomat:§r§f Somma invalida.");
            sender.openInventory(this.createWithdrawGUI(0.0));
            withdrawals.put(sender, null);
            return;
        }

        if (amount % 0.50 != 0) {
            sender.sendMessage("§r§e[GIOCO]§r§a Bancomat:§r§f Somma invalida, inserisci un importo che sia multiplo di 0.50€.");
            sender.openInventory(this.createWithdrawGUI(0.0));
            withdrawals.put(sender, null);
            return;
        }

        sender.openInventory(this.createWithdrawGUI(amount));
        withdrawals.put(sender, amount);
    }
}
