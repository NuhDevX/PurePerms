package org.PP.NuhDev;

import org.PP.NuhDev.event.PPGroupChangedEvent;
import org.PP.NuhDev.event.PPRankExpiredEvent;
import org.powernukkitx.Player;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.EventPriority;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.entity.EntityTeleportEvent;
import org.powernukkitx.event.player.PlayerCommandPreprocessEvent;
import org.powernukkitx.event.player.PlayerLoginEvent;
import org.powernukkitx.event.player.PlayerQuitEvent;
import org.powernukkitx.lang.TranslationContainer;
import org.powernukkitx.level.Location;
import org.powernukkitx.utils.TextFormat;

public class PPListener implements Listener {

    private final PurePerms plugin;

    public PPListener(PurePerms plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onGroupChanged(PPGroupChangedEvent event) {
        Player player = event.getPlayer();
        this.plugin.updatePermissions(player);
    }
  
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLevelChange(EntityTeleportEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getLevel().getFolderName().equals(to.getLevel().getFolderName())) {
            return;
        }

        if (event.getEntity() instanceof Player player) {
            this.plugin.updatePermissions(player, to.getLevel().getFolderName());
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        if (!message.startsWith("/")) return;

        String command = message.substring(1).split(" ")[0].toLowerCase();
        Player player = event.getPlayer();

        if (!this.plugin.getNoeulAPI().isAuthed(player)) {
            event.setCancelled(true);
            if (command.equals("ppsudo") || command.equals("help")) {
                this.plugin.getServer().dispatchCommand(player, message.substring(1));
            } else {
                this.plugin.getNoeulAPI().sendAuthMsg(player);
            }
        } else {
            boolean disableOp = this.plugin.getConfigValue("disable-op");
            if (disableOp && command.equals("op")) {
                event.setCancelled(true);
                player.sendMessage(new TranslationContainer(TextFormat.RED + "%commands.generic.permission"));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        this.plugin.registerPlayer(player);

        if (this.plugin.getNoeulAPI().isNoeulEnabled()) {
            this.plugin.getNoeulAPI().deAuth(player);
        }

        if (!this.plugin.getNoeulAPI().isAuthed(player)) {
            this.plugin.getNoeulAPI().sendAuthMsg(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.plugin.unregisterPlayer(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRankExpired(PPRankExpiredEvent event) {
        Player player = event.getPlayer();
        this.plugin.setGroup(player, this.plugin.getDefaultGroup());
    }
}

