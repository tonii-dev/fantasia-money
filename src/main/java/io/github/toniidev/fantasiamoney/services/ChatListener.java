package io.github.toniidev.fantasiamoney.services;

import io.github.toniidev.fantasiamoney.enums.InputType;
import io.github.toniidev.fantasiamoney.interfaces.MessageHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class ChatListener implements Listener {
    private Plugin plugin;

    private static List<List<Player>> allLists = new ArrayList<>();
    private static List<Player> pwdInput = new ArrayList<>();

    public static void startWatchingPlayer(InputType type, Player p){
        if(type.equals(InputType.CREDIT_CARD_PASSWORD)) {
            ChatListener.pwdInput.add(p);
        }
    }

    public static void stopWatchingPlayer(InputType type, Player p){
        if(type.equals(InputType.CREDIT_CARD_PASSWORD)) ChatListener.pwdInput.remove(p);
    }

    public static List<Player> getList(InputType type){
        if(type.equals(InputType.CREDIT_CARD_PASSWORD)) return pwdInput;
        else return null;
    }

    private final MessageHandler messageHandler;

    public ChatListener(MessageHandler messageHandler, Plugin p){
        this.plugin = p;
        this.messageHandler = messageHandler;

        allLists.add(pwdInput);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e){
        String message = e.getMessage();

        for(List<Player> list : allLists){
            if(list.contains(e.getPlayer())){
                e.setCancelled(true);
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        messageHandler.pinHandler(message, e.getPlayer());
                    }
                }.runTask(plugin);
            }
        }
    }
}
