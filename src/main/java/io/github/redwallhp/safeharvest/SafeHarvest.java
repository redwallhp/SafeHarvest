package io.github.redwallhp.safeharvest;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class SafeHarvest extends JavaPlugin {


    private Map<Material, Integer> tools;


    @Override
    public void onEnable() {
        tools = new HashMap<Material, Integer>();
        loadConfig();
        new SafeHarvestListener(this);
    }


    public void loadConfig() {
        saveDefaultConfig();
        tools.clear();
        Set<String> keys = getConfig().getConfigurationSection("tools").getKeys(false);
        for (String key : keys) {
            Material material = Material.matchMaterial(key.toUpperCase());
            Integer percentage = getConfig().getConfigurationSection("tools").getInt(key);
            if (material != null) {
                tools.put(material, percentage);
            }
        }
    }


    public Map<Material, Integer> getTools() {
        return tools;
    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("safeharvest-reload")) {
            reloadConfig();
            loadConfig();
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Configuration reloaded.");
            return true;
        }
        return false;
    }


}
