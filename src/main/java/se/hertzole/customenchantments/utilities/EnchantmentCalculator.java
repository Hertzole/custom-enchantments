package se.hertzole.customenchantments.utilities;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public final class EnchantmentCalculator {

    public static boolean doUnbreakingDurability(ItemStack item) {
        if (item == null)
            return false;

        if (!item.getItemMeta().hasEnchant(Enchantment.DURABILITY)) {
            return true;
        }

        int unbreakingLevel = item.getItemMeta().getEnchantLevel(Enchantment.DURABILITY);
        double unbreakChance = (double) ((100 / (unbreakingLevel + 1)));
        double random = Math.random() * 100;
        if (random > unbreakChance) {
            return false;
        }

        return true;
    }
}
