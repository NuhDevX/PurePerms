package org.PP.NuhDev.event;
  
import org.powernukkitx.event.plugin.PluginEvent;
import org.powernukkitx.IPlayer;
import org.powernukkitx.Server;
import org.powernukkitx.level.Level;

import org.PP.NuhDev.PurePerms;
import org.PP.NuhDev.PPGroup;
public class PPGroupChangedEvent extends PluginEvent{

	private final PPGroup group;

	private final String levelName;

	private final IPlayer player;

	public PPGroupChangedEvent(PurePerms plugin, IPlayer player, PPGroup group, String levelName){
		super(plugin);

		this.group = group;
		this.player = player;
		this.levelName = levelName;
	}

	public PPGroup getGroup(){
		return this.group;
	}

	public Level getLevel(){
		return Server.getInstance().getLevelByName(this.levelName);
	}

	public String getLevelName(){
		return this.levelName;
	}

	public IPlayer getPlayer(){
		return this.player;
	}
}
