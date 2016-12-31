package io.github.redwallhp.safeharvest;

import com.google.common.collect.Lists;
import com.sk89q.worldguard.bukkit.WGBukkit;
import org.bukkit.CropState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Crops;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;


public class SafeHarvestListener implements Listener {


    private SafeHarvest plugin;
    private Set<ExpectedDrop> expectedDrops;


    public SafeHarvestListener(SafeHarvest plugin) {
        this.plugin = plugin;
        this.expectedDrops = new HashSet<ExpectedDrop>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    @EventHandler(priority = EventPriority.LOW)
    public void onBlockBreak(BlockBreakEvent event) {

        if (!isHarvestable(event.getBlock())) return;

        boolean canBuild = WGBukkit.getPlugin().canBuild(event.getPlayer(), event.getBlock());
        ItemStack playerTool = event.getPlayer().getInventory().getItemInMainHand();
        boolean usingConfTool = plugin.getTools().containsKey(playerTool.getType());

        if (!canBuild && plugin.getConfig().getBoolean("protected_harvest")) {
            if (!usingConfTool && plugin.getConfig().getBoolean("protected_harvest_with_hand")) {
                doSafeHarvest(event, playerTool);
            }
            else if (usingConfTool) {
                doSafeHarvest(event, playerTool);
            }
        }
        else if (canBuild && usingConfTool) {
            doSafeHarvest(event, playerTool);
        }

    }


    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        Iterator<ExpectedDrop> it = expectedDrops.iterator();
        Random chance = new Random();
        while (it.hasNext()) {
            ExpectedDrop expected = it.next();
            if (expected.isOld()) {
                it.remove();
                break;
            }
            if (expected.getLocation().distanceSquared(event.getLocation()) < 1) {
                ItemStack item = event.getEntity().getItemStack();
                //Decrement drops to account for automatic replant
                if (!isBlacklistedForDecrement(item) && !expected.isDecremented()) {
                    item.setAmount(item.getAmount() - 1);
                    if (item.getAmount() < 1) {
                        event.getEntity().remove();
                    }
                    expected.setDecremented(true);
                }
                //Apply configured chance of doubling the drop
                if (plugin.getTools().containsKey(expected.getTool().getType())) {
                    int percentage = plugin.getTools().get(expected.getTool().getType());
                    if (chance.nextInt() <= percentage) {
                        item.setAmount(item.getAmount() * 2);
                    }
                }
                break;
            }
        }
    }


    /**
     * Perform a safe harvest operation, cancelling the original event before
     * modifying drops and ensuring the blocks are safely updated.
     * @param event the BlockBreakEvent to work on
     * @param playerTool the tool the player is holding
     */
    private void doSafeHarvest(BlockBreakEvent event, ItemStack playerTool) {

        event.setCancelled(true);

        //harvest the blocks
        int blocks = setHarvestedBlock(event.getBlock(), playerTool);

        //damage the tool
        if (blocks > 0 && plugin.getTools().containsKey(playerTool.getType())) {
            playerTool.setDurability((short) (playerTool.getDurability() + 1));
            if (playerTool.getDurability() >= playerTool.getType().getMaxDurability()) {
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
                event.getPlayer().getInventory().setItemInMainHand(null);
            }
        }

    }


    /**
     * We don't need to process the event any further if the user isn't breaking
     * a block that is a harvestable crop.
     * @param block the block to check
     * @return true if this is a harvestable block
     */
    private boolean isHarvestable(Block block) {
        List<Material> materials = Lists.newArrayList(
                Material.SUGAR_CANE_BLOCK,
                Material.MELON_BLOCK,
                Material.PUMPKIN
        );
        boolean isCropInstance = block.getState().getData() instanceof Crops;
        return isCropInstance || materials.contains(block.getType());
    }


    /**
     * Items that shouldn't be decremented in drops, for auto replant.
     * e.g. wheat (only seeds should), poisonous potatoes, pumpkins
     * @param item the item to check
     * @return true if this item is protected
     */
    private boolean isBlacklistedForDecrement(ItemStack item) {
        List<Material> materials = Lists.newArrayList(
                Material.PUMPKIN,
                Material.MELON,
                Material.POISONOUS_POTATO,
                Material.WHEAT,
                Material.BEETROOT
        );
        return materials.contains(item.getType());
    }


    /**
     * Safely handle the removal and replacement of a block after the interaction is cancelled.
     * "Crops" (wheat, carrot, potato, etc.) will have their state set down to seed level.
     * Sugar cane will cascade upward, and the bottom-most one will not be removed.
     * Melons and pumpkins will be removed only if they're touching a stem.
     * @param block The block to remove.
     * @return the number of affected blocks
     */
    private int setHarvestedBlock(Block block, ItemStack playerTool) {

        //handle carrot, potato, wheat, etc. by resetting growth
        if (block.getState().getData() instanceof Crops) {
            Material type = block.getType();
            expectedDrops.add(new ExpectedDrop(block.getLocation(), playerTool));
            block.breakNaturally(playerTool);
            block.setType(type);
            block.setData(CropState.SEEDED.getData());
            return 1;
        }

        //handle sugar cane, naturally breaking top blocks but leaving the bottom one
        else if (block.getType().equals(Material.SUGAR_CANE_BLOCK)) {
            Location loc = block.getLocation().clone();
            int caneBlocks = 0;
            //don't break the clicked block unless it has cane under it
            Block underFirst = block.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY()-1, loc.getBlockZ());
            if (underFirst.getType().equals(Material.SUGAR_CANE_BLOCK)) {
                block.breakNaturally(playerTool);
                caneBlocks++;
            }
            //break all of the cane above the clicked block
            for (int y = loc.getBlockY()+1; y < loc.getBlockY()+3; y++) {
                Block b = block.getWorld().getBlockAt(loc.getBlockX(), y, loc.getBlockZ());
                if (b.getType().equals(Material.SUGAR_CANE_BLOCK)) {
                    b.breakNaturally();
                    caneBlocks++;
                }
            }
            return caneBlocks;
        }

        //handle melon and pumpkin, removing the block if it's attached to a stem
        else if (block.getType().equals(Material.MELON_BLOCK) || block.getType().equals(Material.PUMPKIN)) {
            boolean hasPlant = false;
            for (BlockFace face : BlockFace.values()) {
                Block facing = block.getRelative(face);
                if (facing.getType().equals(Material.MELON_STEM) || facing.getType().equals(Material.PUMPKIN_STEM)) {
                    hasPlant = true;
                    break;
                }
            }
            if (hasPlant) {
                block.breakNaturally(playerTool);
                return 1;
            }
        }

        return 0;

    }

}
