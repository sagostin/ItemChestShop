package xyz.dec0de.itemchestshop.utils;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.inventory.Inventory;
import xyz.dec0de.itemchestshop.storage.ShopStorage;

import java.util.ArrayList;
import java.util.List;

public class ChestUtils {
    public static boolean blockShopSign(Block block) {
        List<Sign> sign = new ArrayList<>();
        for (BlockFace blockFace : BlockFace.values()) {
            Block br = block.getRelative(blockFace, 1);
            if (br.getBlockData().getMaterial().toString().contains("SIGN")) {
                Sign sign1 = (Sign) br.getState();
                sign.add(sign1);
            }
        }
        for (Sign signCheck : sign) {
            if (ChatColor.stripColor(signCheck.getLine(0)).equalsIgnoreCase("[Shop]")) {
                if (ShopStorage.isShop(signCheck.getLocation())) {
                    return true;
                }
            }
        }

        return false;
    }

    public static Location getChestLocation(Location signLocation) {
        for (BlockFace f : (new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST})) {
            Block relativeBlock = signLocation.getBlock().getRelative(f);
            if (relativeBlock.getType() == Material.CHEST || relativeBlock.getType() == Material.BARREL) {
                return relativeBlock.getLocation();
            }
        }
        return null;
    }

    public static Inventory getBlockInventory(Block block) {
        Inventory inv = null;
        if (block.getType() == Material.CHEST) {
            inv = ((Chest) block.getState()).getInventory();
        } else if (block.getType() == Material.BARREL) {
            inv = ((Barrel) block.getState()).getInventory();
        }

        return inv;
    }
}
