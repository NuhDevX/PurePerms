package org.PP.NuhDev.cmd;

import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.ConsoleCommandSender;
import org.powernukkitx.command.PluginIdentifiableCommand; 
import org.powernukkitx.plugin.Plugin;
import org.powernukkitx.utils.TextFormat; 
import org.PP.NuhDev.PurePerms;

public class PPInfo extends Command implements PluginIdentifiableCommand {

    private final PurePerms plugin;

    public PPInfo(PurePerms plugin, String name, String description) {
        super(name, description);
        this.plugin = plugin;
        this.setPermission("pperms.command.ppinfo");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender)) {
            return false;
        }

        String author = this.plugin.getDescription().getAuthors().get(0);
        String version = this.plugin.getDescription().getVersion();

        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + " " +
                    this.plugin.getMessage("cmds.ppinfo.messages.ppinfo_console", new String[]{version, author}));
        } else {
            sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + " " +
                    this.plugin.getMessage("cmds.ppinfo.messages.ppinfo_player", new String[]{version, author}));
        }

        return true;
    }

    @Override
    public Plugin getPlugin() {
        return this.plugin;
    }
}
