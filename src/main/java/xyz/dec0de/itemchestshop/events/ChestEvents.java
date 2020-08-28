package xyz.dec0de.itemchestshop.events;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import xyz.dec0de.itemchestshop.storage.ShopStorage;
import xyz.dec0de.itemchestshop.utils.ChestUtils;

public class ChestEvents implements Listener {

    @EventHandler
    public void chestBreak(BlockBreakEvent e) {
        if (e.getBlock().getType() == Material.CHEST) {
            if (ChestUtils.blockShopSign(e.getBlock())) {
                Player player = e.getPlayer();
                player.sendMessage(ChatColor.RED + "You cannot break a shop's chest without breaking the sign first.");
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void chestOpen(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null)
            if (e.getClickedBlock().getType() == Material.CHEST) {
                if (e.getAction() == Action.RIGHT_CLICK_BLOCK)
                    if (ChestUtils.blockShopSign(e.getClickedBlock())) {
                        Player player = e.getPlayer();
                        if (player.isOp()) return;
                        ShopStorage shopStorage = ShopStorage.getShopStorageFromChest(e.getClickedBlock());

                        if (shopStorage.getOwner().equals(player.getUniqueId())) return;
                        player.sendMessage(ChatColor.RED + "You cannot open this chest if you do not own the shop.");
                        e.setCancelled(true);
                    }
            }

    }
}
