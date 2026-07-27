package org.PP.NuhDev.data;

import org.PP.NuhDev.event.PPGroupChangedEvent;
import org.PP.NuhDev.PPGroup;
import org.PP.NuhDev.PurePerms;
import org.powernukkitx.IPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDataManager {

    private final PurePerms plugin;

    public UserDataManager(PurePerms plugin) {
        this.plugin = plugin;
    }

    public Map<String, Object> getData(IPlayer player) {
        return this.plugin.getProvider().getPlayerData(player);
    }

    public Object getExpDate(IPlayer player, String levelName) {
        return levelName != null ? this.getWorldData(player, levelName).get("expTime") : this.getNode(player, "expTime");
    }

    public PPGroup getGroup(IPlayer player, String levelName) {
        String groupName = (String) (levelName != null ? this.getWorldData(player, levelName).get("group") : this.getNode(player, "group"));

        PPGroup group = this.plugin.getGroup(groupName);

        if (group == null) {
            this.plugin.getLogger().critical("Invalid group name found in " + player.getName() + "'s player data (World: " + (levelName == null ? "GLOBAL" : levelName) + ")");
            this.plugin.getLogger().critical("Restoring the group data to 'default'");

            PPGroup defaultGroup = this.plugin.getDefaultGroup(levelName);
            
            this.setGroup(player, defaultGroup, levelName, -1);

            return defaultGroup;
        }

        return group;
    }

    public Object getNode(IPlayer player, String node) {
        Map<String, Object> userData = this.getData(player);

        if (!userData.containsKey(node)) {
            return null;
        }

        return userData.get(node);
    }

    @SuppressWarnings("unchecked")
    public List<String> getUserPermissions(IPlayer player, String levelName) {
        Object permissions = levelName != null ? this.getWorldData(player, levelName).get("permissions") : this.getNode(player, "permissions");

        if (!(permissions instanceof List)) {
            this.plugin.getLogger().critical("Invalid 'permissions' node given to getUserPermissions()");
            return new ArrayList<>();
        }

        return (List<String>) permissions;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getWorldData(IPlayer player, String levelName) {
        if (levelName == null) {
            levelName = this.plugin.getServer().getDefaultLevel().getFolderName();
        }

        Map<String, Object> data = this.getData(player);
        Map<String, Object> worlds = (Map<String, Object>) data.get("worlds");

        if (worlds == null || !worlds.containsKey(levelName)) {
            Map<String, Object> defaultData = new HashMap<>();
            defaultData.put("group", this.plugin.getDefaultGroup(levelName).getName());
            defaultData.put("permissions", new ArrayList<String>());
            defaultData.put("expTime", -1);
            return defaultData;
        }

        return (Map<String, Object>) worlds.get(levelName);
    }

    public void removeNode(IPlayer player, String node) {
        Map<String, Object> tempUserData = this.getData(player);

        if (tempUserData.containsKey(node)) {
            tempUserData.remove(node);
            this.setData(player, tempUserData);
        }
    }

    public void setData(IPlayer player, Map<String, Object> data) {
        this.plugin.getProvider().setPlayerData(player, data);
    }

    public void setGroup(IPlayer player, PPGroup group, String levelName) {
        this.setGroup(player, group, levelName, -1);
    }

    public void setGroup(IPlayer player, PPGroup group, String levelName, int time) {
        if (levelName == null) {
            this.setNode(player, "group", group.getName());
            this.setNode(player, "expTime", time);
        } else {
            Map<String, Object> worldData = this.getWorldData(player, levelName);

            worldData.put("group", group.getName());
            worldData.put("expTime", time);

            this.setWorldData(player, levelName, worldData);
        }

        PPGroupChangedEvent event = new PPGroupChangedEvent(this.plugin, player, group, levelName);
        event.call();
    }

    public void setNode(IPlayer player, String node, Object value) {
        Map<String, Object> tempUserData = this.getData(player);
        tempUserData.put(node, value);
        this.setData(player, tempUserData);
    }

    @SuppressWarnings("unchecked")
    public void setPermission(IPlayer player, String permission, String levelName) {
        if (levelName == null) {
            Map<String, Object> tempUserData = this.getData(player);
            List<String> perms = (List<String>) tempUserData.computeIfAbsent("permissions", k -> new ArrayList<String>());
            
            if (!perms.contains(permission)) {
                perms.add(permission);
            }
            
            this.setData(player, tempUserData);
        } else {
            Map<String, Object> worldData = this.getWorldData(player, levelName);
            List<String> perms = (List<String>) worldData.computeIfAbsent("permissions", k -> new ArrayList<String>());
            
            if (!perms.contains(permission)) {
                perms.add(permission);
            }
            
            this.setWorldData(player, levelName, worldData);
        }

        this.plugin.updatePermissions(player);
    }

    @SuppressWarnings("unchecked")
    public void setWorldData(IPlayer player, String levelName, Map<String, Object> worldData) {
        Map<String, Object> tempUserData = this.getData(player);
        Map<String, Object> worlds = (Map<String, Object>) tempUserData.computeIfAbsent("worlds", k -> new HashMap<String, Object>());
        
        worlds.put(levelName, worldData);
        this.setData(player, tempUserData);
    }

    @SuppressWarnings("unchecked")
    public void unsetPermission(IPlayer player, String permission, String levelName) {
        if (levelName == null) {
            Map<String, Object> tempUserData = this.getData(player);
            List<String> perms = (List<String>) tempUserData.get("permissions");
            
            if (perms != null && perms.contains(permission)) {
                perms.remove(permission);
                this.setData(player, tempUserData);
            }
        } else {
            Map<String, Object> worldData = this.getWorldData(player, levelName);
            List<String> perms = (List<String>) worldData.get("permissions");
            
            if (perms != null && perms.contains(permission)) {
                perms.remove(permission);
                this.setWorldData(player, levelName, worldData);
            }
        }

        this.plugin.updatePermissions(player);
    }
}
              
