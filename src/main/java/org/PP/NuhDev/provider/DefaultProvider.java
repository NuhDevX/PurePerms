package org.PP.NuhDev.provider;

import org.powernukkitx.utils.Config;
import org.PP.NuhDev.PPGroup;
import org.PP.NuhDev.PurePerms;
import org.powernukkitx.IPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DefaultProvider implements ProviderInterface {

    private final Config groups;
    private final Config players;
    private final PurePerms plugin;

    public DefaultProvider(PurePerms plugin) {
        this.plugin = plugin;

        this.plugin.saveResource("groups.yml");
        this.groups = new Config(new File(this.plugin.getDataFolder(), "groups.yml"), Config.YAML);

        if (this.groups.getAll().isEmpty()) {
            throw new RuntimeException(this.plugin.getMessage("logger_messages.YAMLProvider_InvalidGroupsSettings"));
        }

        this.plugin.saveResource("players.yml");
        this.players = new Config(new File(this.plugin.getDataFolder(), "players.yml"), Config.YAML);
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

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getPlayerData(IPlayer player) {
        String userName = player.getName().toLowerCase();

        if (!this.players.exists(userName)) {
            Map<String, Object> defaultData = new HashMap<>();
            defaultData.put("group", this.plugin.getDefaultGroup().getName());
            defaultData.put("permissions", new ArrayList<String>());
            defaultData.put("worlds", new HashMap<String, Object>());
            defaultData.put("time", -1);
            return defaultData;
        }

        Object data = this.players.get(userName);
        if (data instanceof Map) {
            return (Map<String, Object>) data;
        }
        
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> getUsers() {
  /*
        Map<String, Object> allPlayers = this.players.getAll();
        if (allPlayers.isEmpty()) {
            return null;
        }
        return allPlayers;*/
        
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
        String userName = player.getName().toLowerCase();

        if (!this.players.exists(userName)) {
            Map<String, Object> defaultData = new HashMap<>();
            defaultData.put("group", this.plugin.getDefaultGroup().getName());
            defaultData.put("permissions", new ArrayList<String>());
            defaultData.put("worlds", new HashMap<String, Object>());
            defaultData.put("time", -1);
            
            this.players.set(userName, defaultData);
        }

        if (tempUserData.containsKey("userName")) {
            tempUserData.remove("userName");
        }

        this.players.set(userName, tempUserData);
        this.players.save();
    }

    @Override
    public void close() {
    }
}  
