package org.PP.NuhDev.cmd;

import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.ConsoleCommandSender; 
import org.powernukkitx.command.PluginIdentifiableCommand;
import org.powernukkitx.plugin.Plugin;
import org.powernukkitx.utils.TextFormat;
import org.powernukkitx.level.Level;
import org.PP.NuhDev.PurePerms;
import org.PP.NuhDev.PPGroup;

import java.util.List;

public class ListGPerms extends Command implements PluginIdentifiableCommand {

    private final PurePerms plugin;

    public ListGPerms(PurePerms plugin, String name, String description) {
        super(name, description);
        this.plugin = plugin;
        this.setPermission("pperms.command.listgperms");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender)) {
            return false;
        }

        if (args.length < 1 || args.length > 3) {
            sender.sendMessage(TextFormat.BLUE + PurePerms.MAIN_PREFIX + " " +
                    this.plugin.getMessage("cmds.listgperms.usage"));
            return true;
        }

        PPGroup group = this.plugin.getGroup(args[0]);

        if (group == null) {
            sender.sendMessage(TextFormat.RED + PurePerms.MAIN_PREFIX + " " +
                    this.plugin.getMessage("cmds.listgperms.messages.group_not_exist", new String[]{args[0]}));
            return true;
        }

        String levelName = null;

        if (args.length > 2) {
            Level level = this.plugin.getServer().getWorldManager().getWorldByName(args[2]);

            if (level == null) {
                sender.sendMessage(TextFormat.RED + PurePerms.MAIN_PREFIX + " " +
                        this.plugin.getMessage("cmds.setgperm.messages.level_not_exist", new String[]{args[2]}));
                return true;
            }

            levelName = level.getFolderName();
        }

        List<String> permissions = group.getGroupPermissions(levelName);

        if (permissions == null || permissions.isEmpty()) {
            sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + " " +
                    this.plugin.getMessage("cmds.listgperms.messages.no_group_perms", new String[]{group.getName()}));
            return true;
        }

        int pageHeight = sender instanceof ConsoleCommandSender ? 24 : 6;
        
        int maxPageNumber = (int) Math.ceil((double) permissions.size() / pageHeight);
        int pageNumber = 1;

        if (args.length > 1) {
            try {
                pageNumber = Integer.parseInt(args[1]);
                if (pageNumber <= 0) {
                    pageNumber = 1;
                } else if (pageNumber > maxPageNumber) {
                    pageNumber = maxPageNumber;
                }
            } catch (NumberFormatException e) {
                pageNumber = 1;
            }
        }

        sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + " " +
                this.plugin.getMessage("cmds.listgperms.messages.group_perms_list", 
                new String[]{group.getName(), String.valueOf(pageNumber), String.valueOf(maxPageNumber)}));

        int startIndex = (pageNumber - 1) * pageHeight;
        int endIndex = Math.min(startIndex + pageHeight, permissions.size());

        for (int i = startIndex; i < endIndex; i++) {
            sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + " - " + permissions.get(i));
        }

        return true;
    }

    @Override
    public Plugin getPlugin() {
        return this.plugin;
    }
  }
              
