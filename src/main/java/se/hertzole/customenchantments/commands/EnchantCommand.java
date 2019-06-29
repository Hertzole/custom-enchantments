package se.hertzole.customenchantments.commands;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import se.hertzole.customenchantments.CustomEnchantments;
import se.hertzole.customenchantments.Msg;
import se.hertzole.customenchantments.utilities.Enchanter;
import se.hertzole.mchertzlib.HertzPlugin;
import se.hertzole.mchertzlib.commands.Command;
import se.hertzole.mchertzlib.commands.CommandInfo;
import se.hertzole.mchertzlib.utils.CommandsUtil;

import java.util.*;

@CommandInfo(name = "enchant", pattern = "enchant", usage = "/ce enchant <enchantment>", desc = "Enchants your current held item.", permission = "customenchantments.enchant", console = false)
public class EnchantCommand implements Command {

    @Override
    public boolean execute(HertzPlugin plugin, CommandSender sender, String... args) {
        if (!CommandsUtil.isPlayer(sender)) {
            plugin.getGlobalMessenger().tell(sender, Msg.MISC_NOT_FROM_CONSOLE);
            return true;
        }

        Player player = CommandsUtil.unwrap((sender));

        ItemStack mainItem = player.getInventory().getItemInMainHand();

        if (mainItem.getType() == Material.AIR) {
            plugin.getGlobalMessenger().tell(player, Msg.NO_ITEM);
            return true;
        }

        if (args.length == 0) {
            plugin.getGlobalMessenger().tell(player, Msg.NO_ENCHANTMENT);
            return true;
        }

        boolean enchantmentExists = false;
        Set<String> enchantments = CustomEnchantments.instance.getEnchantsMap().keySet();
        for (String s : enchantments) {
            if (args[0].trim().equals(s)) {
                enchantmentExists = true;
                break;
            }
        }

        if (!enchantmentExists) {
            plugin.getGlobalMessenger().tell(sender, Msg.NO_MATCHING_ENCHANTMENT.toString().replace("{enchantment}", args[0]));
            return true;
        }

        Enchantment enchant = null;
        if (args[0].equals("auto_replanter")) {
            enchant = CustomEnchantments.AUTO_REPLANTER;
        } else if (args[0].equals("vein_miner")) {
            enchant = CustomEnchantments.VEIN_MINER;
        }

        if (enchant == null)
            enchant = Enchantment.BINDING_CURSE;

        if (Enchanter.enchant((CustomEnchantments) plugin, player, mainItem, args[0]))
            plugin.getGlobalMessenger().tell(sender, Msg.ENCHANTED.toString().replace("{enchantment}", enchant.getName()));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, int argIndex) {
        List<String> suggestions = new ArrayList<>();

        if (argIndex == 0) {
            Map<String, String> map = CustomEnchantments.instance.getEnchantsMap();
            suggestions.addAll(map.keySet());
        }

        Collections.sort(suggestions);

        return suggestions;
    }
}
