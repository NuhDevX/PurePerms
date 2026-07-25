package org.PP.NuhDev.cmd;

import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.PluginIdentifiableCommand;
import org.powernukkitx.Player;
import org.powernukkitx.plugin.Plugin;
import org.powernukkitx.utils.TextFormat;

import org.PP.NuhDev.PurePerms;
import org.PP.NuhDev.noeul.NoeulAPI;

public class PPSudo extends Command implements PluginIdentifiableCommand {

    private final PurePerms plugin;

    public PPSudo(PurePerms plugin, String name, String description) {
        super(name, description);
        this.plugin = plugin;
        this.setPermission("pperms.noeul.ppsudo");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender)) {
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + " " +
                    this.plugin.getMessage("cmds.ppsudo.messages.invalid_sender"));
            return true;
        }

        if (args == null || args.length == 0 || args.length > 2) {
            sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + " " +
                    this.plugin.getMessage("cmds.ppsudo.usage"));
            return true;
        }

        NoeulAPI noeulAPI = this.plugin.getNoeulAPI();

        switch (args[0].toLowerCase()) {
            case "login":
                if (!noeulAPI.isRegistered(sender)) {
                    sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + " " +
                            this.plugin.getMessage("cmds.ppsudo.messages.not_registered"));
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + " " +
                            this.plugin.getMessage("cmds.ppsudo.messages.login_usage"));
                    return true;
                }

                Object hash = this.plugin.getUserDataMgr().getNode(sender, "noeulPW");

                if (noeulAPI.hashEquals(args[1], hash)) {
                    noeulAPI.auth(sender);
                }
                break;

            case "register":
                if (noeulAPI.isRegistered(sender)) {
                    sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + " " +
                            this.plugin.getMessage("cmds.ppsudo.messages.already_registered"));
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage(TextFormat.GREEN + PurePerms.MAIN_PREFIX + " " +
                            this.plugin.getMessage("cmds.ppsudo.messages.register_usage"));
                    return true;
                }

                int mpl = (int) this.plugin.getConfigValue("noeul-minimum-pw-length");

                if (args[1].length() < mpl) {
                    sender.sendMessage(TextFormat.RED + PurePerms.MAIN_PREFIX + " " +
                            this.plugin.getMessage("cmds.ppsudo.messages.password_too_short", new String[]{String.valueOf(mpl)}));
                    return true;
                }

                if (noeulAPI.register(sender, args[1])) {
                    noeulAPI.auth(sender);
                }
                break;
        }

        return true;
    }

    @Override
    public Plugin getPlugin() {
        return this.plugin;
    }
}
                    
