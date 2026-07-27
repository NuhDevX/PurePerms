package org.PP.NuhDev.provider;

import org.powernukkitx.utils.Config;
import org.PP.NuhDev.PPGroup;
import org.PP.NuhDev.PurePerms;
import org.powernukkitx.IPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class YamlV1Provider implements ProviderInterface {

    private final Config groups;
    private final File userDataFolder;
    protected final PurePerms plugin;

    public YamlV1Provider(PurePerms plugin) {
        this.plugin = plugin;

        this.plugin.saveResource("groups.yml");
        this.groups = new Config(new File(this.plugin.getDataFolder(), "groups.yml"), Config.YAML);

        this.userDataFolder = new File(this.plugin.getDataFolder(), "players/");

        if (!this.userDataFolder.exists()) {
            this.userDataFolder.mkdirs();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getGroupData(PPGroup group) {
        String groupName = group.getName();
        Map<String, Object> groupsData = this.getGroupsData();

        if (!groupsData.containsKey(groupName) || !(groupsData.get(groupName) instanceof Map)) {
            return new HashMap<>();
        }

        return (Map<String, Object>) groupsData.get(groupName);
    }

    public Config getGroupsConfig() {
        return this.groups;
    }

    @Override
    public Map<String, Object> getGroupsData() {
        return this.groups.getAll();
    }

    public Object getPlayerConfig(IPlayer player, boolean onUpdate) {
        String userName = player.getName();
        File userFile = new File(this.userDataFolder, userName.toLowerCase() + ".yml");

        if (onUpdate) {
            if (!userFile.exists()) {
                Config config = new Config(userFile, Config.YAML);
                config.set("userName", userName);
                config.set("group", this.plugin.getDefaultGroup().getName());
                config.set("permissions", new ArrayList<String>());
                config.set("worlds", new HashMap<String, Object>());
                config.set("time", -1);
                return config;
            } else {
                return new Config(userFile, Config.YAML);
            }
        } else {
            if (userFile.exists()) {
                return new Config(userFile, Config.YAML);
            } else {
                Map<String, Object> defaultData = new HashMap<>();
                defaultData.put("userName", userName);
                defaultData.put("group", this.plugin.getDefaultGroup().getName());
                defaultData.put("permissions", new ArrayList<String>());
                defaultData.put("worlds", new HashMap<String, Object>());
                defaultData.put("time", -1);
                return defaultData;
            }
        }
    }
    
    public Object getPlayerConfig(IPlayer player) {
        return this.getPlayerConfig(player, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getPlayerData(IPlayer player) {
        Object userConfig = this.getPlayerConfig(player);

        if (userConfig instanceof Config) {
            return ((Config) userConfig).getAll();
        } else {
            return (Map<String, Object>) userConfig;
        }
    }

    @Override
    public Map<String, Object> getUsers() {
        // TODO
        return null;
    }

    @Override
    public void setGroupData(PPGroup group, Map<String, Object> tempGroupData) {
        String groupName = group.getName();
        this.groups.set(groupName, tempGroupData);
        this.groups.save();
    }

    @Override
    public void setGroupsData(Map<String, Object> tempGroupsData) {
        this.groups.setAll(new HashMap<>(tempGroupsData));
        this.groups.save();
    }

    @Override
    public void setPlayerData(IPlayer player, Map<String, Object> tempUserData) {
        Object userData = this.getPlayerConfig(player, true);

        if (!(userData instanceof Config)) {
            throw new RuntimeException("Failed to update player data: Invalid data type (" + userData.getClass().getSimpleName() + ")");
        }

        Config config = (Config) userData;
        config.setAll(new HashMap<>(tempUserData));
        config.save();
    }

    @Override
    public void close() {
    }
}

