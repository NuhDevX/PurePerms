package org.PP.NuhDev.cmd; 

import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.PluginIdentifiableCommand;
import org.powernukkitx.plugin.Plugin;
import org.powernukkitx.utils.TextFormat;

import java.util.ArrayList;
import java.util.List;

import org.PP.NuhDev.PurePerms;
import org.PP.NuhDev.PPGroup;

public class Groups extends Command implements PluginIdentifiableCommand{

    private final PurePerms plugin;

    public Groups(PurePerms plugin, String name, String description) {
        super(name, description);
        this.plugin = plugin;
        this.setPermission("pperms.command.groups");
    }
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender)) {
            return false;
        }

        List<String> result = new ArrayList<>();

        for (PPGroup group : this.plugin.getGroups()) {
            result.add(group.getName());
        }

        String joinedGroups = String.join(", ", result);

        sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + " " + 
                this.plugin.getMessage("cmds.groups.messages.all_registered_groups", joinedGroups));

        return true;
    }

    public Plugin getPlugin() {
        return this.plugin;
    }
  }

