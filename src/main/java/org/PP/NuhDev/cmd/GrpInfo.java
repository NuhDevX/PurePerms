package org.PP.NuhDev.cmd; 

import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.PluginIdentifiableCommand;
import org.powernukkitx.plugin.Plugin;
import org.powernukkitx.utils.TextFormat;
import org.powernukkitx.level.Level; 

import org.PP.NuhDev.PurePerms; 
import org.PP.NuhDev.PPGroup; 

import java.util.ArrayList;
import java.util.List;

public class GrpInfo extends Command implements PluginIdentifiableCommand {

    private final PurePerms plugin;

    public GrpInfo(PurePerms plugin, String name, String description) {
        super(name, description);
        this.plugin = plugin;
        this.setPermission("pperms.command.grpinfo");
    }
    
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender)) {
            return false;
        }

        if (args.length < 1 || args.length > 2) {
            sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + " " +
                this.plugin.getMessage("cmds.grpinfo.usage"));
            return true;
        }

        PPGroup group = this.plugin.getGroup(args[0]);

        if (group == null) {
            sender.sendMessage(TextFormat.RED + PurePerms.MAIN_PREFIX + " " +
                this.plugin.getMessage("cmds.grpinfo.messages.group_not_exist", new String[]{args[0]}));
            return true;
        }

        String levelName = null;

        if (args.length == 2) {
            Level level = this.plugin.getServer().getWorldManager().getWorldByName(args[1]);

            if (level == null) {
                sender.sendMessage(TextFormat.RED + PurePerms.MAIN_PREFIX + " " + 
                    this.plugin.getMessage("cmds.grpinfo.messages.level_not_exist", new String[]{args[1]}));
                return true;
            }

            levelName = level.getFolderName();
        }

        sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + " " +
            this.plugin.getMessage("cmds.grpinfo.messages.grpinfo_header", new String[]{group.getName()}));

        String alias = TextFormat.DARK_GREEN + group.getAlias();
        sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + " " +
            this.plugin.getMessage("cmds.grpinfo.messages.grpinfo_alias", new String[]{alias}));

        String isDefault = group.isDefault(levelName) ? TextFormat.DARK_GREEN + "YES" : TextFormat.RED + "NO";
        sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + " " +
            this.plugin.getMessage("cmds.grpinfo.messages.grpinfo_default", new String[]{isDefault}));

        String result = TextFormat.DARK_GREEN + "...";
        
        List<String> parents = new ArrayList<>();

        for (PPGroup tempGroup : group.getParentGroups()) {
            parents.add(tempGroup.getName());
        }

        if (!parents.isEmpty()) {
            result = TextFormat.DARK_GREEN + String.join(",", parents);
        }

        sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + " " +
            this.plugin.getMessage("cmds.grpinfo.messages.grpinfo_parents", new String[]{result}));
            
        return true;
    }

    @Override
    public Plugin getPlugin() {
        return this.plugin;
    }
}
      
