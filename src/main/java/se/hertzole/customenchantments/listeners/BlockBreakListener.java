package se.hertzole.customenchantments.listeners;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import se.hertzole.customenchantments.CustomEnchantments;
import se.hertzole.customenchantments.runnables.VeinMiner;
import se.hertzole.customenchantments.utilities.EnchantmentCalculator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockBreakListener implements Listener {

    private final CustomEnchantments plugin;

    private String autoReplanterLore;
    //private Material[] autoReplanterItems;
    private HashMap<Material, Material> autoReplanterBlocks;
    private boolean autoReplanterDurability;
    private boolean autoReplanterAffectedUnbreaking;

    private String veinMinerLore;
    //private Material[] veinMinerItems;
    private Material[] veinMinerBlocks;
    private boolean veinMinerDurability;
    private boolean veinMinerUnbreaking;
    private boolean veinMinerSilkTouch;
    private int veinMinerMaxBlocks;

    public BlockBreakListener(CustomEnchantments plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        doAutoReplanter(player, event);
        doVeinMiner(player, event);
    }

    private void doAutoReplanter(Player player, BlockBreakEvent event) {
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        Block brokenBlock = event.getBlock();

        Material[] autoReplanterItems = plugin.getAutoReplanterItems();

        for (Material item : autoReplanterItems) {
            if (item != null && mainHandItem.getType() == item && mainHandItem.getItemMeta() != null && mainHandItem.getItemMeta().hasLore()) {
                List<String> lore = mainHandItem.getItemMeta().getLore();
                boolean hasLore = false;

                if (lore != null) {
                    for (String l : lore) {
                        if (ChatColor.translateAlternateColorCodes('&', l.trim()).equals(autoReplanterLore)) {
                            hasLore = true;
                            break;
                        }
                    }
                }

                boolean hasEnchantment = mainHandItem.getItemMeta().hasEnchant(CustomEnchantments.AUTO_REPLANTER);

                if (hasLore && hasEnchantment) {
                    if (autoReplanterBlocks.containsKey(brokenBlock.getType())) {
                        ItemStack seeds = null;
                        Material requiredSeeds = autoReplanterBlocks.get(brokenBlock.getType());

                        Inventory i = player.getInventory();
                        for (ItemStack invItem : i.getContents()) {
                            if (invItem != null && invItem.getType() == requiredSeeds && invItem.getAmount() > 0) {
                                seeds = invItem;
                                break;
                            }
                        }

                        if (player.getGameMode() == GameMode.CREATIVE || seeds != null) {
                            Material blockType = brokenBlock.getType();
                            brokenBlock.breakNaturally();
                            Block newBlock = event.getPlayer().getWorld().getBlockAt(brokenBlock.getLocation());
                            newBlock.setType(blockType);
                            event.setCancelled(true);
                            if (seeds != null && player.getGameMode() != GameMode.CREATIVE)
                                seeds.setAmount(seeds.getAmount() - 1);
                            if (player.getGameMode() != GameMode.CREATIVE && autoReplanterDurability && mainHandItem.getItemMeta() instanceof Damageable) {
                                Damageable dmg = (Damageable) mainHandItem.getItemMeta();
                                boolean doDamage = true;

                                if (autoReplanterAffectedUnbreaking) {
                                    doDamage = EnchantmentCalculator.doUnbreakingDurability(mainHandItem);
                                }

                                if (doDamage) {
                                    if (dmg.getDamage() >= mainHandItem.getType().getMaxDurability()) {
                                        player.getInventory().setItemInMainHand(null);
                                    } else {
                                        dmg.setDamage((short) (dmg.getDamage() + 1));
                                        mainHandItem.setItemMeta((ItemMeta) dmg);
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    private void doVeinMiner(Player player, BlockBreakEvent event) {
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        Block brokenBlock = event.getBlock();

        boolean finish = false;

        Material[] veinMinerItems = plugin.getVeinMinerItems();

        for (Material item : veinMinerItems) {
            if (item != null && mainHandItem.getType() == item && mainHandItem.getItemMeta() != null && mainHandItem.getItemMeta().hasLore()) {

                ItemMeta mainItemMeta = mainHandItem.getItemMeta();

                List<String> lore = mainItemMeta.getLore();
                boolean hasLore = false;

                if (lore != null) {
                    for (String l : lore) {
                        if (ChatColor.translateAlternateColorCodes('&', l.trim()).equals(veinMinerLore)) {
                            hasLore = true;
                            break;
                        }
                    }
                }

                boolean hasEnchantment = mainItemMeta.hasEnchant(CustomEnchantments.VEIN_MINER);

                if (hasLore && hasEnchantment) {
                    for (Material block : veinMinerBlocks) {
                        if (block != null && block == brokenBlock.getType()) {
                            Material blockToBreak = brokenBlock.getType();

                            event.setCancelled(true);

                            new VeinMiner(plugin, player, brokenBlock, brokenBlock.getWorld(),
                                    veinMinerDurability, veinMinerUnbreaking, veinMinerSilkTouch, blockToBreak, veinMinerMaxBlocks).runTaskAsynchronously(plugin);
                            finish = true;
                            break;
                        }
                    }
                }

                if (finish)
                    break;
            }
        }
    }

    public void load() {
        FileConfiguration config = plugin.getConfig();

        loadAutoReplanter(config);
        loadVeinMiner(config);
    }

    private void loadAutoReplanter(FileConfiguration config) {
        autoReplanterLore = ChatColor.translateAlternateColorCodes('&', config.getString("enchantments.auto_replanter.required_lore").trim());
        autoReplanterDurability = config.getBoolean("enchantments.auto_replanter.durability", true);
        autoReplanterAffectedUnbreaking = config.getBoolean("enchantments.auto_replanter.affected_by_unbreaking", true);
        List<String> itemNames = config.getStringList("enchantments.auto_replanter.items");

        ConfigurationSection blocksSection = config.getConfigurationSection("enchantments.auto_replanter.blocks");
        Map<String, Object> blockValues = blocksSection.getValues(true);

        int index = 0;

        autoReplanterBlocks = new HashMap<>();

        for (String key : blockValues.keySet()) {
            String value = blockValues.values().toArray()[index].toString();
            try {
                autoReplanterBlocks.put(Material.valueOf(key.toUpperCase()), Material.valueOf(value.toUpperCase()));
            } catch (Exception e) {
                plugin.getGlobalMessenger().tellConsole("&cAuto replanter blocks: The material '" + value + "' is not valid.&r");
            }
            index++;
        }
    }

    private void loadVeinMiner(FileConfiguration config) {
        veinMinerLore = ChatColor.translateAlternateColorCodes('&', config.getString("enchantments.vein_miner.required_lore").trim());
        veinMinerDurability = config.getBoolean("enchantments.vein_miner.durability", true);
        veinMinerUnbreaking = config.getBoolean("enchantments.vein_miner.affected_by_unbreaking", true);
        veinMinerSilkTouch = config.getBoolean("enchantments.vein_miner.affected_by_silk_touch", true);
        veinMinerMaxBlocks = config.getInt("enchantments.vein_miner.max_blocks", 16);

        List<String> blockList = config.getStringList("enchantments.vein_miner.blocks");
        if (blockList != null && blockList.size() > 0) {
            veinMinerBlocks = new Material[blockList.size()];
            for (int i = 0; i < blockList.size(); i++) {
                try {
                    veinMinerBlocks[i] = Material.valueOf(blockList.get(i).toUpperCase());
                } catch (Exception e) {
                    plugin.getGlobalMessenger().tellConsole("&cVein miner blocks: The material '" + blockList.get(i) + "' is not valid.&r");
                }
            }
        }
    }
}
