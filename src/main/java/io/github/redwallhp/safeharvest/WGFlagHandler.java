package io.github.redwallhp.safeharvest;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Load custom WorldGuard flags
 */
public class WGFlagHandler {


    private SafeHarvest plugin;
    public StateFlag SAFE_HARVEST;


    public WGFlagHandler(SafeHarvest plugin) {
        this.plugin = plugin;
        registerFlags();
    }


    /**
     * Instantiate the flags and their default values, then load them into WorldGuard
     */
    private void registerFlags() {
        FlagRegistry fr = WGBukkit.getPlugin().getFlagRegistry();
        try {
            fr.register(SAFE_HARVEST = new StateFlag("safe-harvest", true));
            plugin.getLogger().info("Loaded WorldGuard flags.");
        } catch (FlagConflictException ex) {
            plugin.getLogger().warning("Failed to load WorldGuard flag: " + ex.getMessage());
        }
    }


    /**
     * Test if the "safe-harvest" flag is set to false for the region
     * @param player the player breaking the block
     * @param block the block being broken
     * @return true if any safe harvesting should be prevented
     */
    public boolean isDisabledInRegion(Player player, Block block) {
        ApplicableRegionSet set = WGBukkit.getPlugin().getRegionManager(block.getWorld()).getApplicableRegions(block.getLocation());
        return !set.testState(null, SAFE_HARVEST);
    }


}
