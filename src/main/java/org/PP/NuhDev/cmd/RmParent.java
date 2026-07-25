package org.PP.NuhDev.cmd;
  
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.PluginIdentifiableCommand;
import org.powernukkitx.plugin.Plugin;
import org.powernukkitx.utils.TextFormat;

import org.PP.NuhDev.PurePerms;
import org.PP.NuhDev.PPGroup;

public class RmParent extends Command implements PluginIdentifiableCommand{

	private final PurePerms plugin;

	public RmParent(PurePerms plugin, String name,  String description){
    super(name, description);
		this.plugin = plugin;
		this.setPermission("pperms.command.rmparent");
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) : bool{
		if(!this.testPermission(sender))
			return false;

		if(args.length < 2 || args.length > 3){
			sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + ' ' + this.plugin.getMessage("cmds.rmparent.usage"));
			return true;
		}

		PPGroup target_group = this.plugin.getGroup(args[0]);

		PPGroup parent_group = this.plugin.getGroup(args[1]);

		target_group.removeParent(parent_group);

		sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + ' ' + this.plugin.getMessage("cmds.rmparent.messages.rmparent_successfully", new String[]{parent_group.getName(), target_group.getName()}));

		return true;
	}

  @Override
	public Plugin getPlugin(){
		return this.plugin;
	}
}
