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
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ATM implements Listener, MessageHandler {
    private Plugin plugin;

    private static Map<Player, Inventory> deposits = new HashMap<>();
    private static Map<Player, Double> withdrawals = new HashMap<>();

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

    @EventHandler
    public void deposit(InventoryClickEvent e){
        Player p = (Player) e.getWhoClicked();

        if(!deposits.containsKey(p)) return;

        e.setCancelled(true);

        if(!ItemHelper.isCustomItem(e.getClickedInventory().getItem(e.getRawSlot()))) return;
        if(!ItemHelper.getLastLoreLine(e.getCurrentItem()).toUpperCase().endsWith("VALUTA")) return;
        if(e.getCurrentItem().equals(deposits.get(p).getItem(4))) return;

        ItemStack s = e.getCurrentItem();
        e.getClickedInventory().setItem(e.getSlot(), new ItemStack(Material.AIR));
        if(deposits.get(p).getItem(4).getType().equals(s.getType())) s.setAmount(s.getAmount() +
                deposits.get(p).getItem(4).getAmount());
        deposits.get(p).setItem(4, s);

        double amount = MoneyInstances.getReal().get(s.getType()) * s.getAmount();
        double finalAmount = amount - (0.05 * amount);

        ItemStack confirm = ItemHelper.createItem(Material.GREEN_STAINED_GLASS_PANE,
                "§r§aConferma", ItemType.DEFAULT);
        ItemHelper.setUnsafeLore(confirm,
                "§r§eSomma effettiva: §r§b" + finalAmount + "€ §r§7[" + amount + "€ - §c5%§7]");
        deposits.get(p).setItem(8, confirm);
    }

    @EventHandler
    public void withdrawConfirm(InventoryClickEvent e) throws IOException {
        if(e.getClickedInventory() == null) return;

        Player p = (Player) e.getWhoClicked();
        Double amount = withdrawals.get(p);

        if(!withdrawals.containsKey(p)) return;
        if(e.getRawSlot() != 8) return;
        if(amount == null) return;

        CreditCard card = CreditCard.getCreditCardFromPlayer(p);

        String loc = Math.round(p.getLocation().getX()) + ", " +
                Math.round(p.getLocation().getY()) + ", " +
                Math.round(p.getLocation().getZ());

        DiscordWebhook wh = new DiscordWebhook("https://discord.com/api/webhooks/1281979191801151540/RLF4EYbFEoXnrNScp7pp2z6j6ERvLMBlLsVb4YEl7ylLB6clTh2v4WLpqosnxUkfctaE");

        if(card.getMoney() < withdrawals.get(p)){
            p.closeInventory();
            p.sendMessage("§r§e[GIOCO]§r§a Bancomat:§r§f Somma maggiore della disponibilità.");

            wh.addEmbed(new DiscordWebhook.EmbedObject()
                    .setTitle("Prelievo")
                    .addField("Coordinate", loc, false)
                    .addField("Numero carta", card.getNumber(), false)
                    .addField("Somma", amount + "€", true)
                    .addField("Stato", "Non riuscito", true)
                    .setColor(Color.red)
                    .setImage("https://mineskin.eu/helm/" + p.getDisplayName() + "/100.png")
                    .setFooter("Le telecamere hanno fotografato", "https://mineskin.eu/helm/" + p.getDisplayName() + "/100.png"));
        }
        else{
            p.closeInventory();
            card.charge(amount);

            for(ItemStack s : MoneyInstances.getExact(amount, new ArrayList<>())){
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
                    .setImage("https://mineskin.eu/helm/" + p.getDisplayName() + "/100.png")
                    .setFooter("Le telecamere hanno fotografato", "https://mineskin.eu/helm/" + p.getDisplayName() + "/100.png"));
        }

        withdrawals.remove(p);
        ChatManager.stopWatchingPlayer(p);
        wh.execute();
    }

    @EventHandler
    public void depositConfirm(InventoryClickEvent e) throws IOException {
        if(e.getClickedInventory() == null) return;
        if(!deposits.containsValue(e.getClickedInventory())) return;
        if(e.getRawSlot() != 8) return;
        if(e.getClickedInventory().getItem(4).getType().equals(Material.GRAY_STAINED_GLASS_PANE)) return;

        ItemStack s = e.getClickedInventory().getItem(4);

        double amount = MoneyInstances.getReal().get(s.getType()) * s.getAmount();
        double finalAmount = amount - (0.05 * amount);

        CreditCard card = CreditCard.getCreditCardFromPlayer((Player) e.getWhoClicked());

        ItemStack tooltip = ItemHelper.createItem(Material.GRAY_STAINED_GLASS_PANE,
                "§r§bInserisci banconota", ItemType.DEFAULT);
        ItemHelper.setUnsafeLore(tooltip,
                "§r§7Clicca su una banconota",
                "§r§7per depositarla");

        e.getClickedInventory().setItem(4, tooltip);

        Player p = (Player) e.getWhoClicked();

        p.closeInventory();
        card.load(finalAmount);

        p.sendMessage("§r§e[GIOCO]§r§a Bancomat:§r§f Carta ricaricata con successo.");

        String loc = Math.round(p.getLocation().getX()) + ", " +
                Math.round(p.getLocation().getY()) + ", " +
                Math.round(p.getLocation().getZ());

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
                .setImage("https://mineskin.eu/helm/" + p.getDisplayName() + "/100.png")
                .setColor(Color.green)
                .setFooter("Le telecamere hanno fotografato", "https://mineskin.eu/helm/" + p.getDisplayName() + "/100.png"));
        wh.execute();
    }

    @EventHandler
    public void depositClose(InventoryCloseEvent e){
        if(!deposits.containsValue(e.getInventory())) return;

        Map.Entry<Player, Inventory> entry = deposits.entrySet()
                .stream()
                .filter(x -> x.getValue().equals(e.getInventory()))
                .findFirst()
                .orElse(null);

        deposits.remove(entry.getKey());

        ItemStack item = entry.getValue().getItem(4);

        if(item.getType().equals(Material.GRAY_STAINED_GLASS_PANE)) return;
        entry.getKey().getInventory().addItem(item);
    }

    @EventHandler
    public void withdrawClose(InventoryCloseEvent e){
        if(!withdrawals.containsKey((Player) e.getPlayer())) return;

        withdrawals.remove((Player) e.getPlayer());
    }

    @EventHandler
    public void open(PlayerInteractEvent e){
        if(ChatManager.isBeingWatched(e.getPlayer())) return;
        if(!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if(!CustomBlockBuilder.getBlocks().containsKey(e.getClickedBlock())) return;
        if(!CustomBlockBuilder.getBlocks().get(e.getClickedBlock()).equals(BlockType.ATM_LOWER) &&
            !CustomBlockBuilder.getBlocks().get(e.getClickedBlock()).equals(BlockType.ATM_UPPER)) return;

        if(!CreditCard.isCreditCard(e.getPlayer().getInventory().getItemInMainHand())) return;
        if(!CreditCard.isActive(e.getPlayer().getInventory().getItemInMainHand())) return;

        CreditCard card = CreditCard.getCreditCardFromPlayer(e.getPlayer());

        e.getPlayer().openInventory(this.createHomeGUI(card));
    }

    @Override
    public void pinHandler(String message, Player sender) {
        if(!ChatManager.getList().containsKey(sender)) return;

        InputType type = ChatManager.getList().get(sender);

        if(type.equals(InputType.ATM_WITHDRAW_PASSWORD) ||
                type.equals(InputType.ATM_DEPOSIT_PASSWORD)) {
            CreditCard card = CreditCard.getCreditCardFromPlayer(sender);
            if(!message.equals(card.getPwd())){
                ChatManager.stopWatchingPlayer(sender);
                sender.sendMessage("§r§e[GIOCO]§r§a Bancomat:§r§f Password errata.");
                sender.openInventory(this.createHomeGUI(card));
            }
            if(message.equals(card.getPwd())){
                ChatManager.stopWatchingPlayer(sender);

                if(type.equals(InputType.ATM_DEPOSIT_PASSWORD)){
                    Inventory inv = this.createDepositGUI();
                    deposits.put(sender, inv);
                    sender.openInventory(inv);
                }
                else {
                    Inventory inv = this.createWithdrawGUI(null);
                    sender.openInventory(inv);
                }
            }
        }
        else if(type.equals(InputType.ATM_CHOOSE_AMOUNT)){
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

            if(amount % 0.50 != 0){
                sender.sendMessage("§r§e[GIOCO]§r§a Bancomat:§r§f Somma invalida, inserisci un importo che sia multiplo di 0.50€.");
                sender.openInventory(this.createWithdrawGUI(0.0));
                withdrawals.put(sender, null);
                return;
            }

            sender.openInventory(this.createWithdrawGUI(amount));
            withdrawals.put(sender, amount);
        }
    }
}
