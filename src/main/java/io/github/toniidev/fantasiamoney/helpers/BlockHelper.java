package io.github.toniidev.fantasiamoney.helpers;

import io.github.toniidev.fantasiamoney.enums.BlockType;
import io.github.toniidev.fantasiamoney.services.CustomBlockBuilder;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;

import java.util.HashMap;
import java.util.Map;

public class BlockHelper {
    public static boolean checkAffinity(ItemStack itemStack, BlockType type){
        return itemStack.isSimilar(new CustomBlockBuilder()
                .create(type)
                .get());
    }

    public static ItemDisplay spawnBlock(Block block, ItemStack item, BlockFace face) {
        Location location = block.getLocation();
        ItemDisplay display = (ItemDisplay) block.getWorld().spawnEntity(location, EntityType.ITEM_DISPLAY);

        Transformation transformation = display.getTransformation();
        transformation.getScale().set(2, 2, 2);
        transformation.getTranslation().add(0.5F, 1, 0.5F);
        transformation.getLeftRotation().setAngleAxis(getAngle(face), 0, 1, 0);

        display.setTransformation(transformation);
        display.setItemStack(item);
        return display;
    }

    public static ItemDisplay spawnBlock(Location location, ItemStack item, BlockFace face) {
        ItemDisplay display = (ItemDisplay) location.getWorld().spawnEntity(location, EntityType.ITEM_DISPLAY);

        Transformation transformation = display.getTransformation();
        transformation.getScale().set(2, 2, 2);
        transformation.getTranslation().add(0.5F, 1, 0.5F);
        transformation.getLeftRotation().setAngleAxis(getAngle(face), 0, 1, 0);

        display.setTransformation(transformation);
        display.setItemStack(item);
        return display;
    }

    public static double getAngle(BlockFace face) {
        Map<BlockFace, Double> map = new HashMap<>();
        map.put(BlockFace.NORTH, Math.toRadians(180));
        map.put(BlockFace.WEST, Math.toRadians(270));
        map.put(BlockFace.SOUTH, 0.0);
        map.put(BlockFace.EAST, Math.toRadians(90));

        return map.getOrDefault(face, 0.0);
    }
}
