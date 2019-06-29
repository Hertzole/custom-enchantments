package se.hertzole.customenchantments;

import org.apache.commons.lang.NullArgumentException;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.PluginManager;
import se.hertzole.customenchantments.commands.CommandHandler;
import se.hertzole.customenchantments.enchantments.AutoReplanterEnchantment;
import se.hertzole.customenchantments.enchantments.VeinMinerEnchantment;
import se.hertzole.customenchantments.listeners.BlockBreakListener;
import se.hertzole.mchertzlib.HertzPlugin;
import se.hertzole.mchertzlib.commands.BaseCommandHandler;
import se.hertzole.mchertzlib.messages.Messenger;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CustomEnchantments extends HertzPlugin {

    private Map<String, String> enchantsMap;
    private Material[] autoReplanterItems;
    private Material[] veinMinerItems;

    public static CustomEnchantments instance;

    public static Enchantment AUTO_REPLANTER;
    public static Enchantment VEIN_MINER;

    private BlockBreakListener blockBreakListener;

    @Override
    protected void preOnEnable() {
        instance = this;
    }

    protected void onEnabled() {
        getLogger().info("Did the second thing");
    }

    @Override
    protected BaseCommandHandler getCommandHandler() {
        return new CommandHandler(this);
    }

    @Override
    protected String getCommandPrefix() {
        return "ce";
    }

    @Override
    protected void onSetup() {
        try {
            setupListeners();
            setupEnchantments();
        } catch (NullArgumentException e) {
            getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + e.getMessage()));
        }
    }

    private void setupListeners() {
        blockBreakListener = new BlockBreakListener(this);

        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(blockBreakListener, this);
    }

    private void setupEnchantments() {
        AUTO_REPLANTER = new AutoReplanterEnchantment();
        VEIN_MINER = new VeinMinerEnchantment();

        registerEnchantment(AUTO_REPLANTER);
        registerEnchantment(VEIN_MINER);
    }

    private void registerEnchantment(Enchantment enchantment) {
        boolean registered = true;
        try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
            Enchantment.registerEnchantment(enchantment);
        } catch (Exception e) {
            registered = false;
        }
        if (registered) {
            getLogger().info("Registered " + enchantment.getName() + " enchantment!");
        }
    }

    @Override
    protected void onReloadConfig(FileConfiguration config) {
        //TODO: Make sure required lore actually exists, and if it doesn't, log it.

        enchantsMap = new HashMap<>();
        enchantsMap.put("auto_replanter", config.getString("enchantments.auto_replanter.required_lore", "&7Auto Replanter"));
        enchantsMap.put("vein_miner", config.getString("enchantments.vein_miner.required_lore", "&7Vein Miner"));

        List<String> autoReplanterItemsList = config.getStringList("enchantments.auto_replanter.items");

        autoReplanterItems = new Material[autoReplanterItemsList.size()];

        for (int i = 0; i < autoReplanterItemsList.size(); i++) {
            try {
                Material mat = Material.valueOf(autoReplanterItemsList.get(i).toUpperCase());
                autoReplanterItems[i] = mat;
            } catch (IllegalArgumentException e) {
                messenger.tellConsole("&cAuto replanter items: The material '" + autoReplanterItemsList.get(i) + "' is not valid.&r");
            }
        }

        List<String> veinMinerItemsList = config.getStringList("enchantments.vein_miner.items");
        if (veinMinerItemsList.size() > 0) {
            veinMinerItems = new Material[veinMinerItemsList.size()];
            for (int i = 0; i < veinMinerItemsList.size(); i++) {
                try {
                    veinMinerItems[i] = Material.valueOf(veinMinerItemsList.get(i).toUpperCase());
                } catch (Exception e) {
                    messenger.tellConsole("&cVein miner items: The material '" + veinMinerItemsList.get(i) + "' is not valid.&r");
                }
            }
        }

        blockBreakListener.load();
    }

    protected void reloadGlobalMessenger() {
        String prefix = config.getString("prefix", "&f[&9Custom Enchantments&f]");

        messenger = new Messenger(this, prefix);
    }

    public Map<String, String> getEnchantsMap() {
        return enchantsMap;
    }

    public Material[] getAutoReplanterItems() {
        return autoReplanterItems;
    }

    public Material[] getVeinMinerItems() {
        return veinMinerItems;
    }
}
