package se.hertzole.customenchantments.runnables;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import se.hertzole.customenchantments.CustomEnchantments;
import se.hertzole.customenchantments.utilities.EnchantmentCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class VeinMiner extends BukkitRunnable {

    private Player player;
    private Block startBlock;
    private World world;
    private CustomEnchantments plugin;

    private boolean durability;
    private boolean unbreaking;
    private boolean silkTouch;

    private Material blockToBreak;

    private List<Block> blocks = new ArrayList<>();

    private int maxBlocks;
    private int currentBlocks;

    private ItemStack mainHandItem;
    private Damageable itemDamage;

    public VeinMiner(CustomEnchantments plugin, Player player, Block startBlock, World world,
                     boolean durability, boolean unbreaking, boolean silkTouch, Material blockToBreak, int maxBlocks) {
        this.plugin = plugin;
        this.player = player;
        this.startBlock = startBlock;
        this.world = world;

        this.durability = durability;
        this.unbreaking = unbreaking;
        this.silkTouch = silkTouch;

        this.blockToBreak = blockToBreak;

        this.maxBlocks = maxBlocks;
        if (maxBlocks <= 0)
            currentBlocks = -1;
        else
            currentBlocks = 1; // One because of the start block.

        blocks.add(startBlock);

        mainHandItem = player.getInventory().getItemInMainHand();
        if (mainHandItem.getItemMeta() instanceof Damageable) {
            itemDamage = (Damageable) mainHandItem.getItemMeta();
        }
    }

    private void getBlocks(Block startBlock) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Block b2 = startBlock.getRelative(x, y, z);
                    if (b2 != null && b2.getType() == blockToBreak && !blocks.contains(b2) && currentBlocks <= maxBlocks - 1) {
                        blocks.add(b2);
                        if (maxBlocks > 0) {
                            currentBlocks++;
                        }
                        this.getBlocks(b2);
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        getBlocks(startBlock);
        if (blocks.size() > 0) {
            blocks = blocks.stream().sorted((b, b2) -> b2.getY() - b.getY()).collect(Collectors.toList());
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (int i = 0; i < blocks.size(); i++) {
                        Location bLoc = blocks.get(i).getLocation();
                        if (!breakBlock(blocks.get(i), world, bLoc.getBlockX(), bLoc.getBlockY(), bLoc.getBlockZ())) {
                            break;
                        }
                    }
                }
            }.runTask(plugin);
        }
    }

    private boolean breakBlock(Block block, World world, int x, int y, int z) {
        Material blockType = block.getType();
        Location location = block.getLocation();

        if (player.getGameMode() != GameMode.CREATIVE) {

            if (mainHandItem.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH) && silkTouch) {
                if (world.getBlockAt(x, y, z).getType() == blockToBreak) {
                    world.getBlockAt(x, y, z).setType(Material.AIR);
                    world.dropItem(location, new ItemStack(blockType, 1));
                }
            } else {
                boolean couldBreak = world.getBlockAt(x, y, z).breakNaturally();

                if (!couldBreak) {
                    plugin.getLogger().warning("Could not break block at " + x + ", " + y + ", " + z);
                    return true;
                }
            }

            if (durability) {
                boolean doDamage = true;

                if (unbreaking) {
                    doDamage = EnchantmentCalculator.doUnbreakingDurability(mainHandItem);
                }

                if (doDamage) {
                    if (itemDamage.getDamage() >= mainHandItem.getType().getMaxDurability()) {
                        player.getInventory().setItemInMainHand(null);
                        return false;
                    } else {
                        itemDamage.setDamage((short) (itemDamage.getDamage() + 1));
                        mainHandItem.setItemMeta((ItemMeta) itemDamage);
                    }
                }
            }

            //TODO: Support Fortune enchantment.
        } else {
            world.getBlockAt(x, y, z).setType(Material.AIR);
        }

        world.playEffect(location, Effect.STEP_SOUND, blockType);

        return true;
    }
}
