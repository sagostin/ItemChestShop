package xyz.dec0de.itemchestshop.utils;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
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
            if (relativeBlock.getType() == Material.CHEST) {
                return relativeBlock.getLocation();
            }
        }
        return null;
    }
}
