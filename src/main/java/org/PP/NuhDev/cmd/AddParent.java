package org.PP.NuhDev.cmd; 

import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.PluginIdentifiableCommand;
import org.powernukkitx.plugin.Plugin;
import org.powernukkitx.utils.TextFormat;
import org.PP.NuhDev.PurePerms;
import org.PP.NuhDev.PPGroup;

public class AddParent extends Command implements PluginIdentifiableCommand {

    private final PurePerms plugin;

    public AddParent(PurePerms plugin, String name, String description) {
        super(name, description);
        this.plugin = plugin;
        this.setPermission("pperms.command.addparent");
    }
    
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender)) {
            return false;
        }

        if (args.length < 2 || args.length > 3) {
            sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + " " +
                this.plugin.getMessage("cmds.addparent.usage"));
            return true;
        }
      
        PPGroup targetGroup = this.plugin.getGroup(args[0]);
        PPGroup parentGroup = this.plugin.getGroup(args[1]);

        if (targetGroup == null || parentGroup == null) {
            sender.sendMessage(PurePerms.MAIN_PREFIX + TextFormat.RED + "Group not found!");
            return true;
        }

        if (targetGroup.addParent(parentGroup)) {
            sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + " " +
                this.plugin.getMessage("cmds.addparent.messages.addparent_successfully", 
                    new String[]{parentGroup.getName(), targetGroup.getName()}));
        } else {
            sender.sendMessage(TextFormat.RED + PurePerms.MAIN_PREFIX + " " + 
                this.plugin.getMessage("cmds.addparent.messages.target_already_inherits", 
                    new String[]{parentGroup.getName(), targetGroup.getName()}));
        }

        return true;
    }

    @Override
    public Plugin getPlugin() {
        return this.plugin;
    }
  }
  
