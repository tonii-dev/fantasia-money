package io.github.toniidev.fantasiamoney;

import io.github.toniidev.fantasiamoney.blocks.ATM;
import io.github.toniidev.fantasiamoney.items.CreditCard;
import io.github.toniidev.fantasiamoney.listeners.PlayerJoin;
import io.github.toniidev.fantasiamoney.services.ChatManager;
import io.github.toniidev.fantasiamoney.services.CustomBlockBuilder;
import io.github.toniidev.fantasiamoney.services.InventoryBuilder;
import io.github.toniidev.fantasiamoney.services.LoreManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class FantasiaMoney extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic

        Bukkit.getPluginManager().registerEvents(new LoreManager(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryBuilder(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoin(this), this);
        Bukkit.getPluginManager().registerEvents(new CreditCard(this), this);
        Bukkit.getPluginManager().registerEvents(new ChatManager(new CreditCard(this), this), this);
        Bukkit.getPluginManager().registerEvents(new ChatManager(new ATM(this), this), this);
        Bukkit.getPluginManager().registerEvents(new ATM(this), this);
        Bukkit.getPluginManager().registerEvents(new CustomBlockBuilder(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
