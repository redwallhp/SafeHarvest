package io.github.redwallhp.safeharvest;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;


public class ExpectedDrop {

    private Location location;
    private ItemStack tool;
    private long time;
    private boolean decremented;

    public ExpectedDrop(Location loc, ItemStack tool) {
        this.location = loc;
        this.tool = tool;
        this.time = System.currentTimeMillis();
        this.decremented = false;
    }

    public Location getLocation() {
        return location;
    }

    public ItemStack getTool() {
        return tool;
    }

    public long getTime() {
        return time;
    }

    public boolean isDecremented() {
        return decremented;
    }

    public void setDecremented(boolean decremented) {
        this.decremented = decremented;
    }

    public boolean isOld() {
        return System.currentTimeMillis() - this.time > 1000;
    }

}
