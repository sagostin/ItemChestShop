package xyz.dec0de.itemchestshop.events;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import xyz.dec0de.itemchestshop.storage.ShopStorage;
import xyz.dec0de.itemchestshop.utils.ChestUtils;
import xyz.dec0de.itemchestshop.utils.NumberUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ShopEvents implements Listener {

    @EventHandler
    public void shopCreate(SignChangeEvent e) throws IOException {
        Player player = e.getPlayer();

        if (!e.getLine(0).equalsIgnoreCase("[Shop]")) return;
        if (!e.getLine(2).contains(":") && e.getLine(2).split(":").length == 2) {
            player.sendMessage(ChatColor.RED + "The sign is formatted incorrectly.");
            return;
        }

        String[] counts = e.getLine(2).split(":");
        // Convert to separate values
        if (!NumberUtils.isParsable(counts[0]) || !NumberUtils.isParsable(counts[1])) {
            player.sendMessage(ChatColor.RED + "The sign is formatted incorrectly.");
            return;
        }

        int itemCount = Integer.parseInt(counts[0]);
        int currencyCount = Integer.parseInt(counts[1]);

        if (ChestUtils.getChestLocation(e.getBlock().getLocation()) == null) return;

        ShopStorage shopStorage = new ShopStorage(e.getBlock().getLocation());
        shopStorage.setOwner(player.getUniqueId());
        shopStorage.setItemCount(itemCount);
        shopStorage.setCurrencyCount(currencyCount);

        player.sendMessage(ChatColor.AQUA +
                "" + ChatColor.UNDERLINE +
                "Please right click the sign with the item you with to sell.");

    }

    @EventHandler
    public void shopBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();
        if (!block.getType().toString().contains("SIGN")) return;
        Sign sign = (Sign) block.getState();

        if (!sign.getLine(0).equalsIgnoreCase("[Shop]")) return;
        if (!ShopStorage.isShop(block.getLocation())) return;

        ShopStorage shopStorage = new ShopStorage(block.getLocation());
        if (!shopStorage.getOwner().equals(player.getUniqueId()) && !player.isOp()) {
            player.sendMessage(ChatColor.RED + "You cannot break shops you do not own.");
            e.setCancelled(true);
            return;
        }

        shopStorage.removeShop();
    }

    @EventHandler
    public void setupShopItems(PlayerInteractEvent e) throws IOException {
        Player player = e.getPlayer();
        Block block = e.getClickedBlock();
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!block.getType().toString().contains("SIGN")) return;
        Sign sign = (Sign) block.getState();

        if (!sign.getLine(0).equalsIgnoreCase("[Shop]")) return;
        if (!ShopStorage.isShop(block.getLocation())) return;

        ShopStorage shopStorage = new ShopStorage((block.getLocation()));

        if (shopStorage.isCurrencyItemSet() && shopStorage.isCurrencyItemSet()) return;

        if (!shopStorage.isOwner(player)) {
            player.sendMessage(ChatColor.RED + "You cannot setup a shop you do not own.");
            return;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand().clone();
        if (emptyMainHand(player)) return;

        String itemName = itemInHand.getItemMeta().hasDisplayName() ?
                itemInHand.getItemMeta().getDisplayName() : itemInHand.getType().name();

        //check if shop has for sale item set
        if (!shopStorage.isForSaleItemSet()) {
            itemInHand.setAmount(1);
            shopStorage.setForSaleItem(itemInHand);

            sign.setLine(1, itemName);
            sign.update();

            player.sendMessage(ChatColor.GREEN +
                    "You have set the shop to sell " + itemName
                    +
                    ChatColor.GREEN + ".");
            if (!shopStorage.isCurrencyItemSet()) {
                player.sendMessage(ChatColor.LIGHT_PURPLE +
                        "" + ChatColor.UNDERLINE +
                        "Please right click the sign with the item you want players to use as currency.");
            }
            e.setCancelled(true);
            return;
        }

        //check if shop has currency item set
        if (!shopStorage.isCurrencyItemSet()) {
            if (emptyMainHand(player)) return;
            itemInHand.setAmount(1);
            shopStorage.setCurrencyItem(itemInHand);

            sign.setLine(3, itemName);
            sign.update();

            player.sendMessage(ChatColor.GREEN +
                    "You have set the shop to use " + itemName +
                    ChatColor.GREEN + " as the currency.");
            player.sendMessage(ChatColor.GREEN +
                    "" + ChatColor.BOLD + "" +
                    "Shop has been created!");
            e.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void useShop(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Block block = e.getClickedBlock();
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!block.getType().toString().contains("SIGN")) return;
        Sign sign = (Sign) block.getState();

        if (!sign.getLine(0).equalsIgnoreCase("[Shop]")) return;
        if (!ShopStorage.isShop(block.getLocation())) return;

        ShopStorage shopStorage = new ShopStorage((block.getLocation()));

        if ((!shopStorage.isCurrencyItemSet() || !shopStorage.isCurrencyItemSet()) && shopStorage.isOwner(player))
            return;

        if (shopStorage.isOwner(player)) {
            player.sendMessage(ChatColor.RED + "You cannot use your own shop.");
            return;
        }

        if ((!shopStorage.isCurrencyItemSet() || !shopStorage.isCurrencyItemSet()) && !shopStorage.isOwner(player)) {
            player.sendMessage(ChatColor.RED + "This shop has not been setup yet.");
            return;
        }

        if (!hasEnoughCurrency(player, shopStorage)) {
            player.sendMessage(ChatColor.RED + "You do not have enough to purchased this.");
            return;
        }

        if (!hasEnoughForSale(shopStorage)) {
            player.sendMessage(ChatColor.RED + "This shop is out of stock.");
            return;
        }

        if (!playerAndShopHaveRoom(player, shopStorage)) {
            player.sendMessage(ChatColor.RED + "Your inventory OR the shop does not have" +
                    "enough room to accept the purchase.");
            return;
        }

        String itemNameCurrency = shopStorage.getCurrencyItem().getItemMeta().hasDisplayName() ?
                shopStorage.getCurrencyItem().getItemMeta().getDisplayName() : shopStorage.getCurrencyItem().getType().name();
        String itemNameForSale = shopStorage.getForSaleItem().getItemMeta().hasDisplayName() ?
                shopStorage.getForSaleItem().getItemMeta().getDisplayName() : shopStorage.getForSaleItem().getType().name();

        swapItems(player, shopStorage);
        player.sendMessage(ChatColor.GREEN + "You have purchased " +
                shopStorage.getItemCount() + "x " + itemNameForSale
                + ChatColor.GREEN + " for " +
                shopStorage.getCurrencyCount() + "x " +
                itemNameCurrency);
    }

    public void swapItems(Player player, ShopStorage shop) {

        Inventory chestInventory = ChestUtils.getBlockInventory(shop.getBlock());
        Inventory playerInventory = player.getInventory();

        List<ItemStack> playerCurrency = new ArrayList<>();
        List<ItemStack> shopItems = new ArrayList<>();

        int currencyAmount = shop.getCurrencyCount();

        Iterator<ItemStack> playerItems = playerInventory.iterator();
        while (playerItems.hasNext()) {
            ItemStack item = playerItems.next();
            if (item != null)
                if (item.isSimilar(shop.getCurrencyItem())) {
                    if (item.getAmount() <= currencyAmount) {
                        currencyAmount -= item.getAmount();
                        playerCurrency.add(item.clone());
                        playerInventory.setItem(playerInventory.first(item), new ItemStack(Material.AIR));
                    } else if (item.getAmount() > currencyAmount) {
                        ItemStack modified = item.clone();
                        modified.setAmount(item.getAmount() - currencyAmount);

                        ItemStack itemToPut = item.clone();
                        itemToPut.setAmount(currencyAmount);

                        currencyAmount = 0;
                        playerCurrency.add(itemToPut);
                        playerInventory.setItem(playerInventory.first(item), modified);
                    }

                    player.updateInventory();
                }
        }

        int forSaleAmount = shop.getItemCount();

        for (ItemStack item : chestInventory) {
            if (item != null)
                if (item.isSimilar(shop.getForSaleItem())) {
                    if (item.getAmount() <= forSaleAmount) {
                        forSaleAmount -= item.getAmount();
                        shopItems.add(item.clone());
                        chestInventory.setItem(chestInventory.first(item), new ItemStack(Material.AIR));
                    } else if (item.getAmount() > forSaleAmount) {
                        ItemStack modified = item.clone();
                        modified.setAmount(item.getAmount() - forSaleAmount);

                        ItemStack itemToPut = item.clone();
                        itemToPut.setAmount(forSaleAmount);

                        forSaleAmount = 0;
                        shopItems.add(itemToPut.clone());
                        chestInventory.setItem(chestInventory.first(item), modified);
                    }
                }
        }

        for (ItemStack currency : playerCurrency) {
            chestInventory.addItem(currency);
        }

        for (ItemStack purchased : shopItems) {
            playerInventory.addItem(purchased);
            player.updateInventory();
        }
    }

    public boolean playerAndShopHaveRoom(Player player, ShopStorage shop) {
        Inventory chestInventory = ChestUtils.getBlockInventory(shop.getBlock());
        Inventory playerInventory = player.getInventory();

        return chestInventory.firstEmpty() != -1 || playerInventory.firstEmpty() != -1;
    }

    private boolean hasEnoughForSale(ShopStorage shop) {
        Inventory inventory = ChestUtils.getBlockInventory(shop.getBlock());
        int forSaleAmount = 0;
        List<ItemStack> itemList = new ArrayList<>();
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack != null)
                itemList.add(itemStack);
        }

        ItemStack[] contents = itemList.toArray(new ItemStack[itemList.size()]);
        for (ItemStack item : contents) {
            if (item.isSimilar(shop.getForSaleItem())) {
                forSaleAmount += item.getAmount();
            }
        }

        return forSaleAmount >= shop.getItemCount();
    }

    private boolean hasEnoughCurrency(Player player, ShopStorage shop) {
        Inventory inventory = player.getInventory();
        int currencyAmount = 0;
        List<ItemStack> itemList = new ArrayList<>();
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack != null)
                itemList.add(itemStack);
        }

        ItemStack[] contents = itemList.toArray(new ItemStack[itemList.size()]);
        for (ItemStack item : contents) {
            if (item.isSimilar(shop.getCurrencyItem())) {
                currencyAmount += item.getAmount();
            }
        }

        return currencyAmount >= shop.getCurrencyCount();
    }

    private boolean emptyMainHand(Player player) {
        if (player.getInventory().getItemInMainHand() == null) {
            player.sendMessage(ChatColor.RED + "Please click the sign with the item you wish to use.");
            return true;
        }

        return false;
    }
}
