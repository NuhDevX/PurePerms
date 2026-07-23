package org.PP.NuhDev;

import org.powernukkitx.IPlayer;
import org.powernukkitx.Player;
import org.powernukkitx.Server;
import org.powernukkitx.command.SimpleCommandMap;
import org.powernukkitx.level.Level;
import org.powernukkitx.permission.Permission;
import org.powernukkitx.permission.PermissionAttachment;
import org.powernukkitx.plugin.Plugin;
import org.powernukkitx.plugin.PluginBase;
import org.powernukkitx.utils.Config;

import org.PP.NuhDev.cmd.*;
import org.PP.NuhDev.data.UserDataManager;
import org.PP.NuhDev.noeul.NoeulAPI;
import org.PP.NuhDev.provider.DefaultProvider;
import org.PP.NuhDev.provider.MySQLProvider;
import org.PP.NuhDev.provider.ProviderInterface;
import org.PP.NuhDev.provider.YamlV1Provider;
import org.PP.NuhDev.task.PPExpDateCheckTask;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PurePerms extends PluginBase {

    private static PurePerms instance;

    public static final String MAIN_PREFIX = "§a[PUREPERMS] §f>> ";
    public static final String CORE_PERM = "pperms.command.ppinfo"; 

    public static final Integer NOT_FOUND = null;
    public static final int INVALID_NAME = -1;
    public static final int ALREADY_EXISTS = 0;
    public static final int SUCCESS = 1;

    private boolean isGroupsLoaded = false;

    private PPMessages messages;
    private NoeulAPI noeulAPI;
    private ProviderInterface provider;
    private UserDataManager userDataMgr;

    private final Map<UUID, PermissionAttachment> attachments = new HashMap<>();
    private Map<String, PPGroup> groups = new HashMap<>();
    private final List<Permission> pmDefaultPerms = new ArrayList<>();

    public static PurePerms getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
        this.saveDefaultConfig();

        this.fixConfig();

        this.messages = new PPMessages(this);
        this.noeulAPI = new NoeulAPI(this);
        this.userDataMgr = new UserDataManager(this);
    }

    @Override
    public void onEnable() {
        this.registerCommands();
        this.setProvider(true);
        this.registerPlayers();

        this.getServer().getPluginManager().registerEvents(new PPListener(this), this);
        this.getServer().getScheduler().scheduleRepeatingTask(this, new PPExpDateCheckTask(this), 72000);
    }

    @Override
    public void onDisable() {
        this.unregisterPlayers();

        if (this.isValidProvider()) {
            this.provider.close();
        }
    }

    private void fixConfig() {
        Config config = this.getConfig();
        boolean changed = false;

        if (!config.exists("default-language")) {
            config.set("default-language", "en"); changed = true;
        }
        if (!config.exists("disable-op")) {
            config.set("disable-op", true); changed = true;
        }
        if (!config.exists("enable-noeul-sixtyfour")) {
            config.set("enable-noeul-sixtyfour", false); changed = true;
        }
        if (!config.exists("noeul-minimum-pw-length")) {
            config.set("noeul-minimum-pw-length", 6); changed = true;
        }
        if (!config.exists("superadmin-ranks")) {
            config.set("superadmin-ranks", Arrays.asList("OP")); changed = true;
        }

        if (changed) {
            this.saveConfig();
            this.getConfig().reload();
        }
    }

    private void registerCommands() {
        SimpleCommandMap commandMap = this.getServer().getCommandMap();

        if (this.getNoeulAPI().isNoeulEnabled()) {
            commandMap.register("pureperms", new PPSudo(this, "ppsudo", this.getMessage("cmds.ppsudo.desc") + " #64FF00"));
        }

        commandMap.register("pureperms", new AddGroup(this, "addgroup", this.getMessage("cmds.addgroup.desc") + " #64FF00"));
        commandMap.register("pureperms", new AddParent(this, "addparent", this.getMessage("cmds.addparent.desc") + " #64FF00"));
        commandMap.register("pureperms", new DefGroup(this, "defgroup", this.getMessage("cmds.defgroup.desc") + " #64FF00"));
        commandMap.register("pureperms", new FPerms(this, "fperms", this.getMessage("cmds.fperms.desc") + " #64FF00"));
        commandMap.register("pureperms", new Groups(this, "groups", this.getMessage("cmds.groups.desc") + " #64FF00"));
        commandMap.register("pureperms", new GrpInfo(this, "grpinfo", this.getMessage("cmds.grpinfo.desc") + " #64FF00"));
        commandMap.register("pureperms", new ListGPerms(this, "listgperms", this.getMessage("cmds.listgperms.desc") + " #64FF00"));
        commandMap.register("pureperms", new ListUPerms(this, "listuperms", this.getMessage("cmds.listuperms.desc") + " #64FF00"));
        commandMap.register("pureperms", new PPInfo(this, "ppinfo", this.getMessage("cmds.ppinfo.desc") + " #64FF00"));
        commandMap.register("pureperms", new PPReload(this, "ppreload", this.getMessage("cmds.ppreload.desc") + " #64FF00"));
        commandMap.register("pureperms", new RmGroup(this, "rmgroup", this.getMessage("cmds.rmgroup.desc") + " #64FF00"));
        commandMap.register("pureperms", new RmParent(this, "rmparent", this.getMessage("cmds.rmparent.desc") + " #64FF00"));
        commandMap.register("pureperms", new SetGPerm(this, "setgperm", this.getMessage("cmds.setgperm.desc") + " #64FF00"));
        commandMap.register("pureperms", new SetGroup(this, "setgroup", this.getMessage("cmds.setgroup.desc") + " #64FF00"));
        commandMap.register("pureperms", new SetUPerm(this, "setuperm", this.getMessage("cmds.setuperm.desc") + " #64FF00"));
        commandMap.register("pureperms", new UnsetGPerm(this, "unsetgperm", this.getMessage("cmds.unsetgperm.desc") + " #64FF00"));
        commandMap.register("pureperms", new UnsetUPerm(this, "unsetuperm", this.getMessage("cmds.unsetuperm.desc") + " #64FF00"));
        commandMap.register("pureperms", new UsrInfo(this, "usrinfo", this.getMessage("cmds.usrinfo.desc") + " #64FF00"));
    }

    private void setProvider() {
        this.setProvider(true);
    }

    private void setProvider(boolean onEnable) {
        String providerName = this.getConfig().getString("data-provider", "yamlv2").toLowerCase();
        ProviderInterface newProvider;

        switch (providerName) {
            case "mysql":
                newProvider = new MySQLProvider(this);
                if (onEnable) this.getLogger().notice(this.getMessage("logger_messages.setProvider_MySQL"));
                break;
            case "yamlv1":
                newProvider = new YamlV1Provider(this);
                if (onEnable) this.getLogger().notice(this.getMessage("logger_messages.setProvider_YAMLv1"));
                break;
            case "yamlv2":
                newProvider = new DefaultProvider(this);
                if (onEnable) this.getLogger().notice(this.getMessage("logger_messages.setProvider_YAMLv2"));
                break;
            default:
                newProvider = new DefaultProvider(this);
                if (onEnable) this.getLogger().warning(this.getMessage("logger_messages.setProvider_NotFound", providerName));
                break;
        }

        if (newProvider != null) {
            this.provider = newProvider;
        }
        this.updateGroups();
    }

    public int addGroup(String groupName) {
        Map<String, Object> groupsData = this.getProvider().getGroupsData();

        if (!this.isValidGroupName(groupName)) return INVALID_NAME;
        if (groupsData.containsKey(groupName)) return ALREADY_EXISTS;

        Map<String, Object> groupInfo = new HashMap<>();
        groupInfo.put("alias", "");
        groupInfo.put("isDefault", false);
        groupInfo.put("inheritance", new ArrayList<>());
        groupInfo.put("permissions", new ArrayList<>());
        groupInfo.put("worlds", new HashMap<>());

        groupsData.put(groupName, groupInfo);

        this.getProvider().setGroupsData(groupsData);
        this.updateGroups();

        return SUCCESS;
    }

    public int date2Int(String date) {
        Pattern pattern = Pattern.compile("([0-9]+)d([0-9]+)h([0-9]+)m");
        Matcher matcher = pattern.matcher(date);

        if (matcher.matches()) {
            long currentTime = System.currentTimeMillis() / 1000L;
            long days = Long.parseLong(matcher.group(1));
            long hours = Long.parseLong(matcher.group(2));
            long mins = Long.parseLong(matcher.group(3));

            return (int) (currentTime + (days * 86400) + (hours * 3600) + (mins * 60));
        }
        return -1;
    }

    public PermissionAttachment getAttachment(Player player) {
        UUID uniqueId = player.getUniqueId();

        if (!this.attachments.containsKey(uniqueId)) {
            throw new RuntimeException("Tried to calculate permissions on " + player.getName() + " using null attachment");
        }
        return this.attachments.get(uniqueId);
    }

    public Object getConfigValue(String key) {
        Object value = this.getConfig().get(key);
        if (value == null) {
            this.getLogger().warning(this.getMessage("logger_messages.getConfigValue_01", key));
            return null;
        }
        return value;
    }

    public PPGroup getDefaultGroup() {
        return getDefaultGroup(null);
    }

    public PPGroup getDefaultGroup(String levelName) {
        List<PPGroup> defaultGroups = new ArrayList<>();

        for (PPGroup defaultGroup : this.getGroups()) {
            if (defaultGroup.isDefault(levelName)) {
                defaultGroups.add(defaultGroup);
            }
        }

        if (defaultGroups.size() == 1) {
            return defaultGroups.get(0);
        } else {
            if (defaultGroups.size() > 1) {
                this.getLogger().warning(this.getMessage("logger_messages.getDefaultGroup_01"));
            } else {
                this.getLogger().warning(this.getMessage("logger_messages.getDefaultGroup_02"));
            }

            this.getLogger().info(this.getMessage("logger_messages.getDefaultGroup_03"));

            for (PPGroup tempGroup : this.getGroups()) {
                if (tempGroup.getParentGroups().isEmpty()) {
                    this.setDefaultGroup(tempGroup, levelName);
                    return tempGroup;
                }
            }
        }
        return null;
    }

    public PPGroup getGroup(String groupName) {
        if (!this.groups.containsKey(groupName)) {
            for (PPGroup group : this.groups.values()) {
                if (group.getAlias().equals(groupName)) {
                    return group;
                }
            }
            this.getLogger().debug(this.getMessage("logger_messages.getGroup_01", groupName));
            return null;
        }

        PPGroup group = this.groups.get(groupName);

        if (group.getData().isEmpty()) {
            this.getLogger().warning(this.getMessage("logger_messages.getGroup_02", groupName));
            return null;
        }
        return group;
    }

    public Collection<PPGroup> getGroups() {
        if (!this.isGroupsLoaded) {
            throw new RuntimeException("No groups loaded, maybe a provider error?");
        }
        return this.groups.values();
    }

    public String getMessage(String node) {
        return this.messages.getMessage(node, new String[0]);
    }

    public String getMessage(String node, String... vars) {
        return this.messages.getMessage(node, vars);
    }

    public NoeulAPI getNoeulAPI() {
        return this.noeulAPI;
    }

    public List<Player> getOnlinePlayersInGroup(PPGroup group) {
        List<Player> users = new ArrayList<>();

        for (Player player : this.getServer().getOnlinePlayers().values()) {
            for (Level level : this.getServer().getLevels().values()) {
                String levelName = level.getFolderName();
                if (this.userDataMgr.getGroup(player, levelName).equals(group)) {
                    users.add(player);
                }
            }
        }
        return users;
    }

    public List<String> getPermissions(IPlayer player, String levelName) {
        PPGroup group = this.userDataMgr.getGroup(player, levelName);

        List<String> groupPerms = group.getGroupPermissions(levelName);
        List<String> userPerms = this.userDataMgr.getUserPermissions(player, levelName);

        List<String> combined = new ArrayList<>(groupPerms);
        combined.addAll(userPerms);
        return combined;
    }

    public IPlayer getPlayer(String userName) {
        Player player = this.getServer().getPlayerExact(userName);
        if (player != null) {
            return player;
        }
        return this.getServer().getOfflinePlayer(userName);
    }

    public List<Permission> getPNXPerms() {
        if (this.pmDefaultPerms.isEmpty()) {
            for (Permission permission : Server.getInstance().getPluginManager().getPermissions().values()) {
                if (permission.getName().contains("powernukkitx.command") || permission.getName().contains("powernukkitx.broadcast")) {
                    this.pmDefaultPerms.add(permission);
                }
            }
        }
        return this.pmDefaultPerms;
    }

    public String getPPVersion() {
        return this.getDescription().getVersion();
    }

    public ProviderInterface getProvider() {
        if (!this.isValidProvider()) {
            this.setProvider(false);
        }
        return this.provider;
    }

    public UserDataManager getUserDataMgr() {
        return this.userDataMgr;
    }

    public boolean isValidGroupName(String groupName) {
        return groupName.matches("^[0-9a-zA-Z\\xA1-\\xFE]+$");
    }

    public boolean isValidProvider() {
        return this.provider != null;
    }

    public void registerPlayer(Player player) {
        this.getLogger().debug(this.getMessage("logger_messages.registerPlayer", player.getName()));

        UUID uniqueId = player.getUniqueId();

        if (!this.attachments.containsKey(uniqueId)) {
            PermissionAttachment attachment = player.addAttachment(this);
            this.attachments.put(uniqueId, attachment);
            this.updatePermissions(player, null);
        }
    }

    public void registerPlayers() {
        for (Player player : this.getServer().getOnlinePlayers().values()) {
            this.registerPlayer(player);
        }
    }

    public void reload() {
        this.getConfig().reload();
        this.saveDefaultConfig();

        this.messages.reloadMessages();
        this.setProvider(false);

        for (Player player : this.getServer().getOnlinePlayers().values()) {
            this.updatePermissions(player, null);
        }
    }

    public Integer removeGroup(String groupName) {
        if (!this.isValidGroupName(groupName)) return INVALID_NAME;

        Map<String, Object> groupsData = this.getProvider().getGroupsData();
        if (!groupsData.containsKey(groupName)) return NOT_FOUND;

        groupsData.remove(groupName);
        this.getProvider().setGroupsData(groupsData);
        this.updateGroups();

        return SUCCESS;
    }

    public void setDefaultGroup(PPGroup group, String levelName) {
        for (PPGroup currentGroup : this.getGroups()) {
            if (levelName == null) {
                if (currentGroup.getNode("isDefault") instanceof Boolean && (Boolean) currentGroup.getNode("isDefault")) {
                    currentGroup.removeNode("isDefault");
                }
            } else {
                if (currentGroup.getWorldNode(levelName, "isDefault") instanceof Boolean && (Boolean) currentGroup.getWorldNode(levelName, "isDefault")) {
                    currentGroup.removeWorldNode(levelName, "isDefault");
                }
            }
        }
        group.setDefault(levelName);
    }

    public void setGroup(IPlayer player, PPGroup group, String levelName, int time) {
        this.userDataMgr.setGroup(player, group, levelName, time);
    }

    public void sortGroupData() {
        for (PPGroup ppGroup : this.groups.values()) {
            ppGroup.sortPermissions();
        }
    }

    public void updateGroups() {
        if (!this.isValidProvider()) {
            throw new RuntimeException("Failed to load groups: Invalid data provider");
        }

        this.groups.clear();

        for (String groupName : this.getProvider().getGroupsData().keySet()) {
            this.groups.put(groupName, new PPGroup(this, groupName));
        }

        if (this.groups.isEmpty()) {
            throw new RuntimeException("No groups found, I guess there's definitely something wrong with your data provider... *cough cough*");
        }

        this.isGroupsLoaded = true;
        this.sortGroupData();
    }

    public void updatePermissions(IPlayer player, String levelName) {
        if (player instanceof Player) {
            Player p = (Player) player;

            String levelName = p.getLevel().getFolderName();

            Map<String, Boolean> permissions = new HashMap<>();

            for (String permission : this.getPermissions(p, levelName)) {
                if (permission.equals("*")) {
                    for (Permission tmp : Server.getInstance().getPluginManager().getPermissions().values()) {
                        permissions.put(tmp.getName(), true);
                    }
                } else {
                    boolean isNegative = permission.startsWith("-");
                    if (isNegative) {
                        permission = permission.substring(1);
                    }
                    permissions.put(permission, !isNegative);
                }
            }

            permissions.put(CORE_PERM, true);

            PermissionAttachment attachment = this.getAttachment(p);
            attachment.clearPermissions();
            attachment.setPermissions(permissions);
        }
    }

    public void updatePlayersInGroup(PPGroup group) {
        for (Player player : this.getServer().getOnlinePlayers().values()) {
            if (this.userDataMgr.getGroup(player, null).equals(group)) {
                this.updatePermissions(player, null);
            }
        }
    }

    public void unregisterPlayer(Player player) {
        this.getLogger().debug(this.getMessage("logger_messages.unregisterPlayer", player.getName()));

        UUID uniqueId = player.getUniqueId();
        
        if (this.attachments.containsKey(uniqueId)) {
            player.removeAttachment(this.attachments.get(uniqueId));
            this.attachments.remove(uniqueId);
        }
    }

    public void unregisterPlayers() {
        for (Player player : this.getServer().getOnlinePlayers().values()) {
            this.unregisterPlayer(player);
        }
    }
}
