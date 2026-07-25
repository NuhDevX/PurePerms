package org.PP.NuhDev.noeul;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.powernukkitx.Server;
import org.powernukkitx.IPlayer;
import org.powernukkitx.Player;
import org.powernukkitx.permission.Permission;
import org.powernukkitx.permission.PermissionAttachment;
import org.powernukkitx.utils.TextFormat;

import org.PP.NuhDev.PurePerms;

public class NoeulAPI {

    public static final String NOEUL_VERSION = "1.0.0";

    private final Map<UUID, PermissionAttachment> needAuth = new HashMap<>();
    private final PurePerms plugin;

    public NoeulAPI(PurePerms plugin) {
        this.plugin = plugin;
    }

    public boolean auth(Player player) {
        if (this.isAuthed(player)) {
            return true;
        }

        UUID uuid = player.getUniqueId();
        if (this.needAuth.containsKey(uuid)) {
            PermissionAttachment attachment = this.needAuth.get(uuid);
            player.removeAttachment(attachment);
            this.needAuth.remove(uuid);
        }

        player.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + " " +
                this.plugin.getMessage("cmds.ppsudo.messages.successfully_logged_in"));
        return true;
    }

    public boolean deAuth(Player player) {
        PermissionAttachment attachment = player.addAttachment(this.plugin);
        this.removePermissions(attachment);

        this.needAuth.put(player.getUniqueId(), attachment);
        this.sendAuthMsg(player);

        return true;
    }

    public String hash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hashEquals(String password, String hash) {
        if (hash == null) return false;
        return hash.equals(this.hash(password));
    }

    public boolean isAuthed(Player player) {
        return !this.needAuth.containsKey(player.getUniqueId());
    }

    public boolean isNoeulEnabled() {
        return (boolean) this.plugin.getConfigValue("enable-noeul-sixtyfour");
    }

    public boolean isRegistered(IPlayer player) {
        return this.plugin.getUserDataMgr().getNode(player, "noeulPW") != null;
    }

    public boolean register(IPlayer player, String password) {
        if (!this.isRegistered(player)) {
            String hash = this.hash(password);
            this.plugin.getUserDataMgr().setNode(player, "noeulPW", hash);
            return true;
        }
        return false;
    }

    private void removePermissions(PermissionAttachment attachment) {
        Map<String, Boolean> permissions = new HashMap<>();

        for (Permission permission : Server.getInstance().getPluginManager().getPermissions().values()) {
            permissions.put(permission.getName(), false);
        }

        permissions.put("powernukkitx.command.help", true);
        permissions.put("pperms.noeul.ppsudo", true);

        attachment.setPermissions(permissions);
    }

    public void sendAuthMsg(Player player) {
        player.sendMessage(TextFormat.RED + PurePerms.MAIN_PREFIX + " " +
                this.plugin.getMessage("cmds.ppsudo.messages.deauth_01", new String[]{NOEUL_VERSION}));
        player.sendMessage(TextFormat.RED + PurePerms.MAIN_PREFIX + " " +
                this.plugin.getMessage("cmds.ppsudo.messages.deauth_02"));
        player.sendMessage(TextFormat.RED + PurePerms.MAIN_PREFIX + " " +
                this.plugin.getMessage("cmds.ppsudo.messages.deauth_03"));
    }

    public boolean unregister(IPlayer player) {
        if (this.isRegistered(player)) {
            this.plugin.getUserDataMgr().removeNode(player, "noeulPW");
            return true;
        }
        return false;
    }
}
