package se.hertzole.customenchantments.commands;

import se.hertzole.customenchantments.Msg;
import se.hertzole.mchertzlib.HertzPlugin;
import se.hertzole.mchertzlib.commands.BaseCommandHandler;

public class CommandHandler extends BaseCommandHandler {

    public CommandHandler(HertzPlugin plugin) {
        super(plugin);
    }

    @Override
    public void registerCommands() {
        register(EnchantCommand.class);
    }

    @Override
    protected String getReloadPermission() {
        return "customenchantments.admin.reload";
    }

    @Override
    protected String getHelpMessage() {
        return Msg.MISC_HELP.toString();
    }

    @Override
    protected String getMultipleMatchesMessage() {
        return Msg.MISC_MULTIPLE_MATCHES.toString();
    }

    @Override
    protected String getUnknownCommandMessage() {
        return Msg.MISC_NO_MATCHES.toString();
    }

    @Override
    protected String getNoPermissionMessage() {
        return Msg.MISC_NO_ACCESS.toString();
    }

    @Override
    protected String getNoConsoleMessage() {
        return Msg.MISC_NOT_FROM_CONSOLE.toString();
    }
}
