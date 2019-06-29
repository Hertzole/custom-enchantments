package se.hertzole.customenchantments.utilities;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import se.hertzole.customenchantments.CustomEnchantments;
import se.hertzole.customenchantments.Msg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Enchanter {

    public static boolean enchant(CustomEnchantments plugin, Player player, ItemStack item, String enchantmentName) {

        if (item.getType() == Material.AIR) {
            plugin.getGlobalMessenger().tell(player, Msg.NO_ITEM);
            return false;
        }

        String enchantArg = enchantmentName.toLowerCase().trim();
        Map<String, String> map = plugin.getEnchantsMap();
        String lore = map.get(enchantArg);
        List<String> itemLore;
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            itemLore = item.getItemMeta().getLore();
        } else {
            itemLore = new ArrayList<>();
        }

        if (lore.isEmpty()) {
            plugin.getGlobalMessenger().tell(player, Msg.NO_MATCHING_ENCHANTMENT.toString().replace("{enchantment}", enchantArg));
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        Enchantment enchant = null;
        if (enchantArg.equals("auto_replanter")) {
            enchant = CustomEnchantments.AUTO_REPLANTER;
        } else if (enchantArg.equals("vein_miner")) {
            enchant = CustomEnchantments.VEIN_MINER;
        }

        boolean validItem = false;
        Material[] items = new Material[0];
        if (enchant != null) {
            if (enchant.equals(CustomEnchantments.AUTO_REPLANTER)) {
                items = plugin.getAutoReplanterItems();
            } else if (enchant.equals(CustomEnchantments.VEIN_MINER)) {
                items = plugin.getVeinMinerItems();
            }

            for (int i = 0; i < items.length; i++) {
                if (items[i] == item.getType()) {
                    validItem = true;
                    break;
                }
            }
        }

        if (!validItem) {
            plugin.getGlobalMessenger().tell(player, Msg.NO_MATCHING_ITEM);
            return false;
        }

        if (itemLore.contains(ChatColor.translateAlternateColorCodes('&', lore)) || meta.hasEnchant(enchant)) {
            plugin.getGlobalMessenger().tell(player, Msg.ALREADY_ENCHANTED);
            return false;
        }

        itemLore.add(ChatColor.translateAlternateColorCodes('&', lore));

        if (enchant == null)
            enchant = Enchantment.BINDING_CURSE;

        meta.addEnchant(enchant, 0, true);

        meta.setLore(itemLore);

        item.setItemMeta(meta);

        //plugin.getGlobalMessenger().tell(player, Msg.ENCHANTED.toString().replace("{enchantment}", enchant.getName()));

        return true;
    }
}
