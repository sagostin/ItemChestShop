package xyz.dec0de.itemchestshop.events;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ShopEvents implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignRightClick(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.HAND) {
            Block block = event.getClickedBlock();
            if(block != null)
            if (block.getType().toString().contains("SIGN")) {
                Sign sign = (Sign) block.getState();

                // Read sign
                if (sign.getLine(0).equalsIgnoreCase("[Shop]")) {
                    if (!event.getPlayer().isSneaking()) {
                        String forSaleItemName = sign.getLine(1);
                        String costItemName = sign.getLine(3);

                        Material forSaleItem = Material.valueOf(forSaleItemName.toUpperCase());
                        Material costItem = Material.valueOf(costItemName.toUpperCase());

                        //Check if items are valid material
                        if (forSaleItem != null && costItem != null) {
                            try {
                                String[] costs = sign.getLine(2).split(":");
                                // Convert to separate values
                                int forSaleAmount = Integer.parseInt(costs[0]);
                                int costAmount = Integer.parseInt(costs[1]);

                                if (costAmount >= 1 && forSaleAmount >= 1)
                                    // Get chest attached to sign
                                    for (BlockFace f : (new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST})) {
                                        Block relativeBlock = block.getRelative(f);
                                        if (relativeBlock.getType() == Material.CHEST) {
                                            Chest chest = (Chest) relativeBlock.getState();

                                            Inventory chestInventory = chest.getInventory();

                                            Player player = event.getPlayer();
                                            Inventory playerInventory = player.getInventory();

                                            // Check if player has enough for the cost
                                            if (inventoryContains(playerInventory, costItem, costAmount)) {

                                                // Check if the chest has enough to sell
                                                if (inventoryContains(chestInventory, forSaleItem, forSaleAmount)) {

                                                    // A horrible thing
                                                    List<ItemStack> chestList = new ArrayList<>();
                                                    for (ItemStack itemStackFor : chestInventory.getContents()) {
                                                        if (itemStackFor != null)
                                                            chestList.add(itemStackFor);
                                                    }
                                                    ItemStack[] chestContents = chestList.toArray(new ItemStack[chestList.size()]);

                                                    // Check chest for items to be removed
                                                    for (ItemStack i : chestContents) {
                                                        if (i.getType() == forSaleItem && i.getAmount() >= forSaleAmount) {
                                                            // Check if chest has room for shop
                                                            if (chestInventory.firstEmpty() != -1) {

                                                                // A horrible thing
                                                                List<ItemStack> playerList = new ArrayList<>();
                                                                for (ItemStack itemStackFor : playerInventory.getContents()) {
                                                                    if (itemStackFor != null)
                                                                        playerList.add(itemStackFor);
                                                                }
                                                                ItemStack[] playerContents = playerList.toArray(new ItemStack[playerList.size()]);

                                                                // Check player inventory for items to be removed
                                                                for (ItemStack is : playerContents) {
                                                                    if (is.getType() == costItem && is.getAmount() >= costAmount) {

                                                                        // Give player item
                                                                        if (playerInventory.firstEmpty() != -1) {

                                                                            // Remove forSale from Chest
                                                                            if (i.getAmount() > forSaleAmount) {
                                                                                i.setAmount(i.getAmount() - forSaleAmount);
                                                                            } else {
                                                                                chestInventory.setItem(chestInventory.first(i), new ItemStack(Material.AIR));
                                                                            }

                                                                            // Remove cost from player inventory
                                                                            if (is.getAmount() > costAmount) {
                                                                                is.setAmount(is.getAmount() - costAmount);
                                                                            } else {
                                                                                playerInventory.setItem(playerInventory.first(is), new ItemStack(Material.AIR));
                                                                            }

                                                                            // Add cost to chest inventory
                                                                            chestInventory.addItem(new ItemStack(costItem, costAmount));

                                                                            // Add forSale to player inventory
                                                                            ItemStack newItem = i.clone();
                                                                            newItem.setAmount(forSaleAmount);

                                                                            playerInventory.addItem(newItem);

                                                                            player.updateInventory();
                                                                            player.sendMessage(
                                                                                    ChatColor.translateAlternateColorCodes('&',
                                                                                            "&aThank you for your business. Come again."));
                                                                        } else {
                                                                            //player.getWorld().dropItemNaturally(player.getLocation(), forSaleItem);

                                                                            player.sendMessage(
                                                                                    ChatColor.translateAlternateColorCodes('&',
                                                                                            "&cYou do not have any room in your inventory."));
                                                                        }
                                                                        break;
                                                                    }
                                                                }
                                                            } else {
                                                                // Chest does not have enough room
                                                                //player.getWorld().dropItemNaturally(player.getLocation(), forSaleItem);
                                                                player.sendMessage(
                                                                        ChatColor.translateAlternateColorCodes('&',
                                                                                "&cThis shop does not have enough room to accept your purchase."));
                                                            }


                                                            break;
                                                        }
                                                    }
                                                } else {
                                                    // Player does not have enough for the cost
                                                    player.sendMessage(
                                                            ChatColor.translateAlternateColorCodes('&',
                                                                    "&cThis shop does not have enough items to sell you."));
                                                }
                                            } else {
                                                // Player does not have enough for the cost
                                                player.sendMessage(
                                                        ChatColor.translateAlternateColorCodes('&',
                                                                "&cYou do not have enough items to purchase this."));
                                            }
                                            break;
                                        }
                                    }

                                event.setCancelled(true);

                                // Handle amount parse error
                            } catch (Exception exception) {
                            }
                        }
                    }
                }
            }
        }

    }

    public boolean inventoryContains(Inventory inventory, Material material, int amount) {
        List<ItemStack> itemStacks = new ArrayList<>();
        for (ItemStack itemStackFor : inventory.getContents()) {
            if (itemStackFor != null)
                itemStacks.add(itemStackFor);
        }
        ItemStack[] inventoryContents = itemStacks.toArray(new ItemStack[itemStacks.size()]);

        for (ItemStack items : inventoryContents) {
            if (items.getType() == material && items.getAmount() >= amount) {
                return true;
            }
        }
        return false;
    }
}
