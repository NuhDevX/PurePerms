package org.PP.NuhDev.cmd;
  
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.PluginIdentifiableCommand;
import org.powernukkitx.plugin.Plugin;
import org.powernukkitx.utils.TextFormat;

import org.PP.NuhDev.PurePerms

public class RmGroup extends Command implements PluginIdentifiableCommand{

	private final PurePerms plugin;

	public RmGroup(PurePerms plugin, String name, String description){
    super(name, description);
		this.plugin = plugin;
		this.setPermission("pperms.command.rmgroup");
	}

  @Override
	public boolean execute(CommandSender sender, String label, String[] args) : bool{
		if(this.testPermission($sender))
			return false;

		if(args == null || args.length > 1){
			sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + ' ' + 
      this.plugin.getMessage("cmds.rmgroup.usage"));
			return true;
		}

		Integer result = this.plugin.removeGroup(args[0]);

		if(result === PurePerms.SUCCESS){
			sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + ' ' + this.plugin.getMessage("cmds.rmgroup.messages.group_removed_successfully", new String[]{args[0]}));
		}elseif(result === PurePerms.INVALID_NAME){
			sender.sendMessage(TextFormat.RED + PurePerms.MAIN_PREFIX + ' ' + this.plugin.getMessage("cmds.rmgroup.messages.invalid_group_name", new String[]{args[0]}));
		}else{
			sender.sendMessage(TextFormat.RED + PurePerms.MAIN_PREFIX + ' ' + this.plugin.getMessage("cmds.rmgroup.messages.group_not_exist", new String[]{args[0]}));
		}

		return true;
	}

  @Override
	public Plugin getPlugin() {
		return this.plugin;
	}
}
