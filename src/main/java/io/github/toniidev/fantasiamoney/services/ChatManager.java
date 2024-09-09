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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatManager implements Listener {
    private Plugin plugin;

    private static Map<Player, InputType> watching = new HashMap<>(); // TODO: use this

    private static List<List<Player>> allLists = new ArrayList<>();
    private static List<Player> pwdInput = new ArrayList<>();
    private static List<Player> atmInput = new ArrayList<>();

    public static void startWatchingPlayer(InputType type, Player p){
        watching.put(p, type);

        /*if(type.equals(InputType.CREDIT_CARD_PASSWORD)) ChatManager.pwdInput.add(p);
        if(type.equals(InputType.ATM_PASSWORD)) ChatManager.atmInput.add(p);*/
    }

    public static void stopWatchingPlayer(Player p){
        watching.remove(p);

        /*if(type.equals(InputType.CREDIT_CARD_PASSWORD)) ChatManager.pwdInput.remove(p);
        if(type.equals(InputType.ATM_PASSWORD)) ChatManager.atmInput.remove(p);*/
    }

    public static Map<Player, InputType> getList(){
        return watching;

        /*if(type.equals(InputType.CREDIT_CARD_PASSWORD)) return pwdInput;
        if(type.equals(InputType.ATM_PASSWORD)) return atmInput;
        else return null;*/
    }

    public static boolean isBeingWatched(Player player){
        /*for(List<Player> list : allLists) {
            if (list.contains(player)) {
                return true;
            }
        }
        return false;*/
        return watching.containsKey(player);
    }

    private final MessageHandler messageHandler;

    public ChatManager(MessageHandler messageHandler, Plugin p){
        this.plugin = p;
        this.messageHandler = messageHandler;

        /*allLists.add(pwdInput);
        allLists.add(atmInput);*/
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e){
        String message = e.getMessage();

        if(isBeingWatched(e.getPlayer())){
            e.setCancelled(true);
            new BukkitRunnable(){
                @Override
                public void run() {
                    messageHandler.pinHandler(message, e.getPlayer());
                    e.getPlayer().resetTitle();
                }
            }.runTask(plugin);
        }

        /*for(List<Player> list : allLists){
            if(list.contains(e.getPlayer())){
                e.setCancelled(true);
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        messageHandler.pinHandler(message, e.getPlayer());
                    }
                }.runTask(plugin);
            }
        }*/
    }
}
