package org.PP.NuhDev.event;

import org.powernukkitx.event.plugin.PluginEvent;
import org.powernukkitx.IPlayer;
import org.powernukkitx.Server;
import org.powernukkitx.level.Level;

import org.PP.NuhDev.PurePerms;
import org.PP.NuhDev.PPGroup;

public class PPRankExpiredEvent extends PluginEvent{

	private final IPlayer player;

	private final String levelName;

	public PPRankExpiredEvent(PurePerms plugin, IPlayer player, String levelName){
		super(plugin);
		this.player = player;
		this.levelName = levelName;
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
