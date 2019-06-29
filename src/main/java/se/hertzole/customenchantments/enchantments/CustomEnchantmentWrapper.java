package se.hertzole.customenchantments.enchantments;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import se.hertzole.customenchantments.CustomEnchantments;

public abstract class CustomEnchantmentWrapper extends Enchantment {

    public CustomEnchantmentWrapper(String namespace) {
        super(new NamespacedKey(CustomEnchantments.instance, namespace));
    }
}
