package io.github.toniidev.fantasiamoney.services;

import io.github.toniidev.fantasiamoney.enums.BlockType;
import io.github.toniidev.fantasiamoney.enums.ItemType;
import io.github.toniidev.fantasiamoney.helpers.BlockHelper;
import io.github.toniidev.fantasiamoney.helpers.ItemHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class CustomBlockBuilder implements Listener {
    // https://minecraft-heads.com/custom-heads/head/64094-atm-lower

    private ItemStack head;
    private String texture;
    private BlockType type;
    private boolean hasClickTooltip = false;
    private boolean hasCustomText = false;
    private String customText;

    //private static List<ItemStack> itemsToHandle = new ArrayList<>();
    private static Map<ItemStack, CustomBlockBuilder> itemsToHandle = new HashMap<>();
    private static Map<Block, BlockType> blocks = new HashMap<>();

    public CustomBlockBuilder setCustomText(String text){
        hasCustomText = true;
        customText = text;

        return this;
    }

    public CustomBlockBuilder addClickTooltip(){
        hasClickTooltip = true;
        return this;
    }

    public boolean hasCustomText(){
        return hasCustomText;
    }

    public boolean hasClickTooltip(){
        return hasClickTooltip;
    }

    public String getCustomText(){
        return customText;
    }

    public BlockType getType(){
        return type;
    }

    public static Map<Block, BlockType> getBlocks() {
        return blocks;
    }

    public CustomBlockBuilder create(BlockType t){
        type = t;
        if(type.equals(BlockType.ATM_LOWER)) {
            head = ItemHelper.createItem(Material.PLAYER_HEAD, "§r§eBancomat inferiore", ItemType.PLUGIN);
            texture = "http://textures.minecraft.net/texture/5f7d517c12ee1dcd90b6fe85fd6c70ab15ed8840bba5c0b5815a0d00674b68a9";
        }
        if(type.equals(BlockType.ATM_UPPER)){
            head = ItemHelper.createItem(Material.PLAYER_HEAD, "§r§eBancomat superiore", ItemType.PLUGIN);
            texture = "http://textures.minecraft.net/texture/b9830cc6ffa9c2e89c9881803800fb76a11de35eb588ba87ee8ac54d7f09fea1";
        }

        SkullMeta meta = (SkullMeta) head.getItemMeta();
        PlayerProfile profile = Bukkit.getServer().createPlayerProfile(UUID.randomUUID());
        PlayerTextures textures = profile.getTextures();

        try{
            textures.setSkin(new URL(texture));
        } catch(MalformedURLException ex){
            ex.printStackTrace();
            return null;
        }

        profile.setTextures(textures);
        meta.setOwnerProfile(profile);
        head.setItemMeta(meta);

        return this;
    }

    public ItemStack get(){
        itemsToHandle.put(head, this);
        return head;
    }

    public void give(Player player){
        player.getInventory().addItem(this.get());
    }

    public static ArmorStand setupArmorStand(ArmorStand stand, String text){
        stand.setInvulnerable(true);
        stand.setInvisible(true);
        stand.setGravity(false);
        stand.setCustomNameVisible(true);
        stand.setCustomName(text);

        return stand;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e){
        ItemStack itemStack = e.getPlayer().getInventory().getItemInMainHand();
        if(!itemsToHandle.containsKey(itemStack)) return;

        CustomBlockBuilder builder = itemsToHandle.get(itemStack);

        BlockHelper.spawnBlock(e.getBlock(), itemStack, e.getPlayer().getFacing());

        Location blockCenter = e.getBlockPlaced().getLocation()
                /*.add(0.5, 0, 0.5)*/;
        Location lowestTextPosition = blockCenter.clone()
                .add(0.5, -1.0, 0.5);
        Location highestTextPosition = lowestTextPosition.clone()
                .add(0, 0.5, 0);

        boolean firstOccupied = false;

        if(builder.hasClickTooltip()) {
            firstOccupied = true;

            setupArmorStand(blockCenter.getWorld()
                    .spawn(lowestTextPosition, ArmorStand.class), "§r§eClicca per aprire!");
        }

        if(builder.hasCustomText()) setupArmorStand(blockCenter.getWorld()
                .spawn(firstOccupied ? highestTextPosition : lowestTextPosition, ArmorStand.class), builder.getCustomText());

        blocks.put(e.getBlockPlaced(), builder.getType());
    }
}
