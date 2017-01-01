#SafeHarvest

SafeHarvest allows players to harvest crops in WorldGuard protected regions, automatically ensuring that the crops are replanted. It can also (optionally) allow the auto-replant behaviour to be used if a player harvests crops with a specific tool such as a hoe, with configurable chances for the drops to be multiplied.


##Behaviour

* When harvesting protected Crops (wheat, carrot, potatoes, etc.), the drop will be reduced by one and the block will be replanted.

* When harvesting melons or pumpkins, the plugin will check that a stem is attached to one side (to prevent grief of cosmetic blocks) and allow the block to be broken.

* When harvesting sugar cane, the player cannot break the bottom block. Harvesting of sugar cane will only be allowed if it is on top of another cane block.

* Cocoa plants are automatically restored to their first stage of growth when broken.

* When configured, a tool such as a hoe can allow the safe harvesting behaviour even when a player is allowed to build in a region. The item's durability will be expended, and drops for Crops (not melons, pumpkins or cane) have a configurable chance of being buffed.


##Configuration

### WorldGuard Protected Harvesting
```
# Allow safe harvesting in WorldGuard regions
protected_harvest: true
protected_harvest_with_hand: true
```
When `protected_harvest` is set to true, players can harvest in WorldGuard regions where they would not otherwise have build permissions. When `protected_harvest_with_hand` is set, they can use their bare hand instead of needing a tool.

###Tools
```
# Map of tools that will allow safe harvesting and chance (percentage) of doubling drops
tools:
  diamond_hoe: 10
  iron_hoe: 5
  stone_hoe: 0
  wood_hoe: 0
```
The `tools` block contains a mapping of Bukkit Material names that will be allowed to trigger safe harvesting outside of a WorldGuard region, along with a percentage (integer, 0-100) for an individual ItemStack's quantity to double. (Items drops in stacks of one when a block is broken.) Setting the key to an empty value with `tools: []` will disable this feature.
