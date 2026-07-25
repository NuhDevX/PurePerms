package org.PP.NuhDev.cmd;

import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.PluginIdentifiableCommand;
import org.powernukkitx.plugin.Plugin;
import org.powernukkitx.utils.TextFormat;
import org.powernukkitx.level.Level;

import org.PP.NuhDev.PurePerms;
import org.PP.NuhDev.PPGroup;

public class SetGPerm extends Command implements PluginIdentifiableCommand{

	private final PurePerms plugin;

	public SetGPerm(PurePerms plugin, String name, String description){
    super(name, description);
		this.plugin = plugin;
		this.setPermission("pperms.command.setgperm");
	}

  @Override
	public boolean execute(CommandSender sender, String label, String[] args) : bool{
		if(!this.testPermission(sender)){
			return false;
		}

		if(args.length < 2 || args.length > 3){
			sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + ' ' + 
      this.plugin.getMessage("cmds.setgperm.usage"));
			return true;
		}

	  PPGroup group = this.plugin.getGroup(args[0]);

		if(group == null){
			sender.sendMessage(TextFormat.RED + PurePerms.MAIN_PREFIX + ' ' + 
      this.plugin.getMessage("cmds.setgperm.messages.group_not_exist", new String[]{args[0]}));
			return true;
		}

		String permission = args[1];

		String levelName = null;

		if(args.length > 2){
			Level level = this.plugin.getServer().getWorldManager().getWorldByName(args[2]);
			if(level == null){
				sender.sendMessage(TextFormat.RED + PurePerms.MAIN_PREFIX + ' ' + 
        this.plugin.getMessage("cmds.setgperm.messages.level_not_exist", new String[]{args[2]}));
				return true;
			}

			levelName = level.getFolderName();
		}

		group.setGroupPermission(permission, levelName);
		sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + ' ' + this.plugin.getMessage("cmds.setgperm.messages.gperm_added_successfully", new String[]{permission}));
		return true;
	}

	public function getPlugin() : Plugin{
		return $this->plugin;
	}
}
