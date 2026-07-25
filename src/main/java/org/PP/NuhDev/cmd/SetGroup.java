package org.PP.NuhDev.cmd;

import org.powernukkitx.IPlayer;
import org.powernukkitx.Player;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.ConsoleCommandSender;
import org.powernukkitx.command.PluginIdentifiableCommand;
import org.powernukkitx.level.Level;
import org.powernukkitx.plugin.Plugin;
import org.powernukkitx.utils.TextFormat;

import org.PP.NuhDev.PPGroup;
import org.PP.NuhDev.PurePerms;

import java.util.List;

public class SetGroup extends Command implements PluginIdentifiableCommand {

    private final PurePerms plugin;

    public SetGroup(PurePerms plugin, String name, String description) {
        super(name, description);
        this.plugin = plugin;
        this.setPermission("pperms.command.setgroup"); 
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender)) {
            return false;
        }

        if (args.length < 2 || args.length > 4) {
            sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + " " +
                    this.plugin.getMessage("cmds.setgroup.usage"));
            return true;
        }

        IPlayer player = this.plugin.getPlayer(args[0]);
        PPGroup group = this.plugin.getGroup(args[1]);

        if (group == null) {
            sender.sendMessage(TextFormat.RED + PurePerms.MAIN_PREFIX + " " +
                    this.plugin.getMessage("cmds.setgroup.messages.group_not_exist", new String[]{args[1]}));
            return true;
        }

        int expTime = -1;

        if (args.length > 2) {
            expTime = this.plugin.date2Int(args[2]);
        }

        String levelName = null;

        if (args.length > 3) {
            Level level = this.plugin.getServer().getLevelByName(args[3]);

            if (level == null) {
                sender.sendMessage(TextFormat.RED + PurePerms.MAIN_PREFIX + " " + 
                        this.plugin.getMessage("cmds.setgroup.messages.level_not_exist", new String[]{args[3]}));
                return true;
            }

            levelName = level.getFolderName();
        }

        @SuppressWarnings("unchecked")
        List<String> superAdminRanks = (List<String>) this.plugin.getConfigValue("superadmin-ranks");

        if (!(sender instanceof ConsoleCommandSender)) {
            if (superAdminRanks != null && superAdminRanks.contains(group.getName())) {
                sender.sendMessage(TextFormat.RED + PurePerms.MAIN_PREFIX + " " + 
                        this.plugin.getMessage("cmds.setgroup.messages.access_denied_01", new String[]{group.getName()}));
                return true;
            }

            PPGroup userGroup = this.plugin.getUserDataMgr().getGroup(player, levelName);

            if (userGroup != null && superAdminRanks != null && superAdminRanks.contains(userGroup.getName())) {
                sender.sendMessage(TextFormat.RED + PurePerms.MAIN_PREFIX + " " +
                        this.plugin.getMessage("cmds.setgroup.messages.access_denied_02", new String[]{userGroup.getName()}));
                return true;
            }
        }

        this.plugin.getUserDataMgr().setGroup(player, group, levelName, expTime);

        sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + " " + 
                this.plugin.getMessage("cmds.setgroup.messages.setgroup_successfully", new String[]{player.getName()}));

        if (player instanceof Player) {
            Player onlinePlayer = (Player) player;
            
            if (!(levelName != null && levelName.equals(onlinePlayer.getLevel().getFolderName()))) {
                onlinePlayer.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + " " + 
                        this.plugin.getMessage("cmds.setgroup.messages.on_player_group_change", new String[]{group.getName()}));
            }
        }

        return true;
    }

    @Override
    public Plugin getPlugin() {
        return this.plugin;
    }
}
                               
