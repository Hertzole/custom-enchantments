package se.hertzole.customenchantments;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import se.hertzole.mchertzlib.messages.Message;

public enum Msg implements Message {

    NO_ITEM("&cYou need to have an item in your hand!"),
    NO_ENCHANTMENT("&cYou need to specify an enchantment!"),
    NO_MATCHING_ENCHANTMENT("&cThere's no enchantment with the name '{enchantment}'!"),
    NO_MATCHING_ITEM("&cThis item can not have this enchantment!"),
    ALREADY_ENCHANTED("&cThat item already has that enchantment!"),
    ENCHANTED("&aEnchanted your item with '&9{enchantment}&a'!"),
    MISC_NO_ACCESS("&cYou don't have access to this command."),
    MISC_MULTIPLE_MATCHES("Did you mean any of these commands?"),
    MISC_NO_MATCHES("Command not found. Type &9/ce help&r"),
    MISC_NOT_FROM_CONSOLE("You can't use this command from the console."),
    MISC_HELP("For a list of commands, type &9/ce help&r");

    private String value;

    Msg(String value) {
        set(value);
    }

    void set(String value) {
        this.value = value;
    }

    public String toString() {
        return ChatColor.translateAlternateColorCodes('&', value);
    }

    public String format(String s) {
        return (s == null) ? "" : toString().replaceAll("%", s);
    }

    static void load(ConfigurationSection config) {
        for (Msg msg : values()) {
            String key = msg.name().toLowerCase().replace("_", "-");
            msg.set(config.getString(key, ""));
        }
    }

    static YamlConfiguration toYaml() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Msg msg : values()) {
            String key = msg.name().replace("_", "-").toLowerCase();
            yaml.set(key, msg.value);
        }

        return yaml;
    }
}
