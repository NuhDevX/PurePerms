package org.PP.NuhDev.cmd; 

import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.ConsoleCommandSender;
import org.powernukkitx.command.PluginIdentifiableCommand;
import org.powernukkitx.plugin.Plugin;
import org.powernukkitx.permission.Permission;
import org.powernukkitx.utils.TextFormat;
import org.PP.NuhDev.PurePerms; 

import java.util.ArrayList;
import java.util.List;

public class FPerms extends Command implements PluginIdentifiableCommand{

    private final PurePerms plugin;

    public FPerms(PurePerms plugin, String name, String description) {
        super(name, description);
        this.plugin = plugin;
        
        this.setPermission("pperms.command.fperms");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender)) {
            return false;
        }

        if (args.length == 0 || args.length > 2) {
            sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + " " + this.plugin.getMessage("cmds.fperms.usage"));
            return true;
        }

        String targetName = args[0].toLowerCase();
        boolean isServer = targetName.equals("powernukkitx") || targetName.equals("pnx") || targetName.equals("nukkit");
        
        Plugin targetPlugin = isServer ? null : this.plugin.getServer().getPluginManager().getPlugin(args[0]);

        if (targetPlugin == null && !isServer) {
            sender.sendMessage(TextFormat.RED + PurePerms.MAIN_PREFIX + " " + this.plugin.getMessage("cmds.fperms.messages.plugin_not_exist", args[0]));
            return true;
        }

        List<Permission> permissions = new ArrayList<>();
        
        if (!isServer && targetPlugin != null) {
            List<Permission> pluginPerms = targetPlugin.getDescription().getPermissions();
            if (pluginPerms != null) {
                permissions.addAll(pluginPerms);
            }
        } else {
            List<Permission> serverPerms = this.plugin.getPNXPerms();
            if (serverPerms != null) {
                permissions.addAll(serverPerms);
            }
        }

        if (permissions.isEmpty()) {
            String pName = isServer ? "PowerNukkitX" : targetPlugin.getName();
            sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + " " + this.plugin.getMessage("cmds.fperms.messages.no_plugin_perms", pName));
            return true;
        }

        int pageHeight = sender instanceof ConsoleCommandSender ? 48 : 6;
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

        String pName = isServer ? "PowerNukkitX" : targetPlugin.getName();
        sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + " " + 
                this.plugin.getMessage("cmds.fperms.messages.plugin_perms_list", pName, String.valueOf(pageNumber), String.valueOf(maxPageNumber)));

        int startIndex = (pageNumber - 1) * pageHeight;
        int endIndex = Math.min(startIndex + pageHeight, permissions.size());

        for (int i = startIndex; i < endIndex; i++) {
            sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + " - " + permissions.get(i).getName());
        }

        return true;
    }

    public Plugin getPlugin() {
        return this.plugin;
    }
  }
              
