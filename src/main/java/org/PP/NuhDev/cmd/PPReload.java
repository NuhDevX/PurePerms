package org.PP.NuhDev.cmd;

import org.PP.NuhDev.PurePerms;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.PluginIdentifiableCommand;
import org.powernukkitx.plugin.Plugin;
import org.powernukkitx.utils.TextFormat;

public class PPReload extends Command implements PluginIdentifiableCommand{

	private final PurePerms plugin;

	public PPReload(PurePerms plugin, String name, String description){
    super(name, description);
    this.plugin = plugin;
		this.setPermission("pperms.command.ppreload");
	}

  @Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if(!this.testPermission(sender))
			return false;

		this.plugin.reload();

		sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + ' ' + this.plugin.getMessage("cmds.ppreload.messages.successfully_reloaded"));

		return true;
	}

  @Override
	public Plugin getPlugin() {
		return this.plugin;
	}
}
