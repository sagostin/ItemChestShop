package xyz.dec0de.itemchestshop.storage;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.dec0de.itemchestshop.Main;
import xyz.dec0de.itemchestshop.utils.ChestUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShopStorage {

    private File file;
    private YamlConfiguration config;

    public ShopStorage(Location signLocation) {
        String signLoc = signLocation.getWorld().getName() + ";" +
                signLocation.getBlockX() + ";" +
                signLocation.getBlockY() + ";" +
                signLocation.getBlockZ();
        file = new File(Main.getPlugin().getDataFolder() +
                File.separator + "shops" +
                File.separator, signLoc + ".yml");

        if (!(file.exists())) {
            try {
                YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
                configuration.set("sign_location", signLocation);
                configuration.set("chest_location", ChestUtils.getChestLocation(signLocation));

                configuration.save(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public static boolean isShop(Location signLocation) {
        String signLoc = signLocation.getWorld().getName() + ";" +
                signLocation.getBlockX() + ";" +
                signLocation.getBlockY() + ";" +
                signLocation.getBlockZ();

        File file = new File(Main.getPlugin().getDataFolder() +
                File.separator + "shops" +
                File.separator, signLoc + ".yml");

        return file.exists();
    }

    public static ShopStorage getShopStorageFromChest(Block block) {
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
                    return new ShopStorage(signCheck.getLocation());
                }
            }
        }

        return null;
    }

    public UUID getOwner() {
        return UUID.fromString(config.getString("owner"));
    }

    public void setOwner(UUID shopOwner) throws IOException {
        config.set("owner", shopOwner.toString());
        config.save(file);
    }

    public Integer getItemCount() {
        return config.getInt("item_count");
    }

    public void setItemCount(int count) throws IOException {
        config.set("item_count", count);
        config.save(file);
    }

    public Integer getCurrencyCount() {
        return config.getInt("currency_count");
    }

    public void setCurrencyCount(int count) throws IOException {
        config.set("currency_count", count);
        config.save(file);
    }

    public boolean isCurrencyItemSet() {
        if (config.contains("currency_item") && config.get("currency_item") instanceof ItemStack) return true;
        return false;
    }

    public boolean isForSaleItemSet() {
        if (config.contains("for_sale_item") && config.get("for_sale_item") instanceof ItemStack) return true;
        return false;
    }

    public ItemStack getCurrencyItem() {
        return (ItemStack) config.get("currency_item");
    }

    public void setCurrencyItem(ItemStack currencyItem) throws IOException {
        config.set("currency_item", currencyItem);
        config.save(file);
    }

    public ItemStack getForSaleItem() {
        return (ItemStack) config.get("for_sale_item");
    }

    public void setForSaleItem(ItemStack forSaleItem) throws IOException {
        config.set("for_sale_item", forSaleItem);
        config.save(file);
    }

    public boolean isOwner(Player player) {
        if (getOwner().equals(player.getUniqueId())) return true;

        return false;
    }

    public Block getBlock() {
        Location chestLocation = (Location) config.get("chest_location");
        Block block = chestLocation.getBlock();
        return block;
    }

    public void removeShop() {
        //FlyingItemUtils.removeByLocation();
        file.delete();
    }
}
