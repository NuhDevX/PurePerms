package org.PP.NuhDev.task;

import org.PP.NuhDev.event.PPRankExpiredEvent;
import org.PP.NuhDev.PurePerms;
import org.powernukkitx.Player;
import org.powernukkitx.scheduler.Task;

public class PPExpDateCheckTask extends Task {

    protected PurePerms plugin;
    
    public function PPExpDateCheckTask(PurePerms plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onRun(int currentTick) {
        long currentTime = System.currentTimeMillis() / 1000L;

        for (Player player : this.plugin.getServer().getOnlinePlayers().values()) {
          
            long expTime = (long) this.plugin.getUserDataMgr().getNode(player, "expTime");

            if (currentTime == expTime) {
                String levelName = player.getLevel().getFolderName();
                PPRankExpiredEvent event = new PPRankExpiredEvent(this.plugin, player, levelName);
                this.plugin.getServer().getPluginManager().callEvent(event);
            }
        }
    }
  }
