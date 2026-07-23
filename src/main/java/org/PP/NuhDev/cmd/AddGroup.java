package org.PP.NuhDev.cmd; 

import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.PluginIdentifiableCommand;
import org.powernukkitx.plugin.Plugin;
import org.powernukkitx.utils.TextFormat;
import org.PP.NuhDev.PurePerms; 

public class AddGroup extends Command implements PluginIdentifiableCommand {

    private final PurePerms plugin;

    public AddGroup(PurePerms plugin, String name, String description) {
        super(name, description);
        this.plugin = plugin;
        this.setPermission("pperms.command.addgroup");
    }
    
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender)) {
            return false;
        }

        if (args.length == 0 || args.length > 1) {
            sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + " " + 
                this.plugin.getMessage("cmds.addgroup.usage"));
            return true;
        }

        String groupName = args[0];
        
        int result = this.plugin.addGroup(groupName);

        if (result == PurePerms.SUCCESS) {
            sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + " " + 
                this.plugin.getMessage("cmds.addgroup.messages.group_added_successfully", groupName));
        } else if (result == PurePerms.ALREADY_EXISTS) {
            sender.sendMessage(TextFormat.RED + PurePerms.MAIN_PREFIX + " " + 
                this.plugin.getMessage("cmds.addgroup.messages.group_already_exists", groupName));
        } else {
            sender.sendMessage(TextFormat.RED + PurePerms.MAIN_PREFIX + " " + 
                this.plugin.getMessage("cmds.addgroup.messages.invalid_group_name", groupName));
        }

        return true;
    }

    @Override
    public Plugin getPlugin() {
        return this.plugin;
    }
}

