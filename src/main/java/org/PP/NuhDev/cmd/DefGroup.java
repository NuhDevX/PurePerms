package org.PP.NuhDev.cmd; 

import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.PluginIdentifiableCommand;
import org.powernukkitx.plugin.Plugin;
import org.powernukkitx.utils.TextFormat;
import org.powernukkitx.level.Level; 

import org.PP.NuhDev.PurePerms; 
import org.PP.NuhDev.PPGroup;

public class DefGroup extends Command implements PluginIdentifiableCommand {

    private final PurePerms plugin;

    public DefGroup(PurePerms plugin, String name, String description) {
        super(name, description);
        this.plugin = plugin;
        this.setPermission("pperms.command.defgroup");
    }
    
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender)) {
            return false;
        }

        if (args.length == 0 || args.length > 2) {
            sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + " " +
                this.plugin.getMessage("cmds.defgroup.usage"));
            return true;
        }

        PPGroup group = this.plugin.getGroup(args[0]);

        if (group == null) {
            sender.sendMessage(TextFormat.RED + PurePerms.MAIN_PREFIX + " " +
                this.plugin.getMessage("cmds.defgroup.messages.group_not_exist", new String[]{args[0]}));
            return true;
        }

        String levelName = null;

        if (args.length > 1) {
            Level level = this.plugin.getServer().getLevelByName(args[1]);

            if (level == null) {
                sender.sendMessage(TextFormat.RED + PurePerms.MAIN_PREFIX + " " +
                    this.plugin.getMessage("cmds.defgroup.messages.level_not_exist", new String[]{args[1]}));
                return true;
            }

            levelName = level.getFolderName();
        }

        this.plugin.setDefaultGroup(group, levelName);

        sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + " " +
            this.plugin.getMessage("cmds.defgroup.messages.defgroup_successfully", new String[]{args[0]}));
        return true;
    }

    @Override
    public Plugin getPlugin() {
        return this.plugin;
    }
  }
          
