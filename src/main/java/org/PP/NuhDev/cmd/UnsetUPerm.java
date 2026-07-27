package org.PP.NuhDev.cmd;

import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.PluginIdentifiableCommand;
import org.powernukkitx.plugin.Plugin;
import org.powernukkitx.utils.TextFormat;
import org.powernukkitx.IPlayer;
import org.powernukkitx.level.Level;
import org.PP.NuhDev.PurePerms;
public class UnsetUPerm extends Command implements PluginIdentifiableCommand{

	private final PurePerms plugin;

	public UnsetUPerm(PurePerms plugin, String name, String description){
    super(name, description);
		this.plugin = plugin;
		this.setPermission("pperms.command.unsetuperm");
	}

	@Override 
	public boolean execute(CommandSender sender, String label, String[] args) {
		if(!this.testPermission(sender))
			return false;

		if(args.length < 2 || args.length > 3){
			sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + ' ' +
      this.plugin.getMessage("cmds.unsetuperm.usage"));
			return true;
		}

		IPlayer player = this.plugin.getPlayer(args[0]);

		String permission = args[1];

		String levelName = null;

		if(args.length > 2){
			Level level = this.plugin.getServer().getLevelByName(args[2]);

			if(level === null){
				sender.sendMessage(TextFormat.RED + PurePerms.MAIN_PREFIX + ' ' +
        this.plugin.getMessage("cmds.unsetgperm.messages.level_not_exist", new String[]{args[2]}));
				return true;
			}

			levelName = level.getFolderName();
		}

		this.plugin.getUserDataMgr().unsetPermission(player, permission, levelName);
		sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + ' ' + 
    this.plugin.getMessage("cmds.unsetuperm.messages.uperm_removed_successfully", new String[]{permission, player.getName()}));
		return true;
	}

  @Override
	public Plugin getPlugin(){
		return this.plugin;
	}
}
