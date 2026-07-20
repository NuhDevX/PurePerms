package org.PP.NuhDev;

import org.powernukkitx.level.Level;

import java.util.*;

public class PPGroup {

    private final String name;
    private final PurePerms plugin;
    private final List<PPGroup> parents = new ArrayList<>();

    public PPGroup(PurePerms plugin, String name) {
        this.plugin = plugin;
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @SuppressWarnings("unchecked")
    public boolean addParent(PPGroup group) {
        Map<String, Object> tempGroupData = this.getData();

        if (this.equals(group) || this.getParentGroups().stream().anyMatch(g -> g.getName().equals(group.getName()))) {
            return false;
        }

        List<String> inheritance = (List<String>) tempGroupData.computeIfAbsent("inheritance", k -> new ArrayList<String>());
        
        if (!inheritance.contains(group.getName())) {
            inheritance.add(group.getName());
        }

        this.setData(tempGroupData);
        this.plugin.updatePlayersInGroup(this);

        return true;
    }

    @SuppressWarnings("unchecked")
    public void createWorldData(String levelName) {
        Map<String, Object> tempGroupData = this.getData();
        Map<String, Object> worlds = (Map<String, Object>) tempGroupData.computeIfAbsent("worlds", k -> new HashMap<String, Object>());

        if (!worlds.containsKey(levelName)) {
            Map<String, Object> worldData = new HashMap<>();
            worldData.put("isDefault", false);
            worldData.put("permissions", new ArrayList<String>());
            
            worlds.put(levelName, worldData);
            this.setData(tempGroupData);
        }
    }

    public String getAlias() {
        Object alias = this.getNode("alias");
        if (alias == null) {
            return this.name;
        }
        return String.valueOf(alias);
    }

    public Map<String, Object> getData() {
        return this.plugin.getProvider().getGroupData(this);
    }

    @SuppressWarnings("unchecked")
    public List<String> getGroupPermissions(String levelName) {
        Object permsObj = levelName != null ? this.getWorldData(levelName).get("permissions") : this.getNode("permissions");

        if (!(permsObj instanceof List)) {
            this.plugin.getLogger().critical("Invalid 'permissions' node given to getGroupPermissions()");
            return new ArrayList<>();
        }

        List<String> permissions = new ArrayList<>((List<String>) permsObj);

        for (PPGroup parentGroup : this.getParentGroups()) {
            List<String> parentPermissions = parentGroup.getGroupPermissions(levelName);
            
            if (parentPermissions != null) {
                for (String perm : parentPermissions) {
                    if (!permissions.contains(perm)) {
                        permissions.add(perm);
                    }
                }
            }
        }

        return permissions;
    }

    public String getName() {
        return this.name;
    }

    public Object getNode(String node) {
        return this.getData().get(node);
    }

    @SuppressWarnings("unchecked")
    public List<PPGroup> getParentGroups() {
        if (this.parents.isEmpty()) {
            Object inheritanceObj = this.getNode("inheritance");
            
            if (!(inheritanceObj instanceof List)) {
                this.plugin.getLogger().critical("Invalid 'inheritance' node given to getParentGroups()");
                return new ArrayList<>();
            }

            List<String> inheritance = (List<String>) inheritanceObj;
            for (String parentGroupName : inheritance) {
                PPGroup parentGroup = this.plugin.getGroup(parentGroupName);

                if (parentGroup != null && !this.parents.contains(parentGroup)) {
                    this.parents.add(parentGroup);
                }
            }
        }

        return this.parents;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getWorldData(String levelName) {
        if (levelName == null) return null;

        this.createWorldData(levelName);
        Map<String, Object> worlds = (Map<String, Object>) this.getData().get("worlds");
        return (Map<String, Object>) worlds.get(levelName);
    }

    public Object getWorldNode(String levelName, String node) {
        Map<String, Object> worldData = this.getWorldData(levelName);
        if (worldData == null) return null;
        
        return worldData.get(node);
    }

    public boolean isDefault(String levelName) {
        if (levelName == null) {
            Object isDef = this.getNode("isDefault");
            return isDef instanceof Boolean && (Boolean) isDef;
        } else {
            Map<String, Object> worldData = this.getWorldData(levelName);
            if (worldData == null) return false;
            
            Object isDef = worldData.get("isDefault");
            return isDef instanceof Boolean && (Boolean) isDef;
        }
    }

    public void removeNode(String node) {
        Map<String, Object> tempGroupData = this.getData();

        if (tempGroupData.containsKey(node)) {
            tempGroupData.remove(node);
            this.setData(tempGroupData);
        }
    }

    @SuppressWarnings("unchecked")
    public boolean removeParent(PPGroup group) {
        Map<String, Object> tempGroupData = this.getData();
        Object inheritanceObj = tempGroupData.get("inheritance");

        if (inheritanceObj instanceof List) {
            List<String> inheritance = (List<String>) inheritanceObj;
            if (inheritance.remove(group.getName())) {
                this.setData(tempGroupData);
                this.plugin.updatePlayersInGroup(this);
                this.parents.remove(group);
                return true;
            }
        }
        return false;
    }

    public void removeWorldNode(String levelName, String node) {
        Map<String, Object> worldData = this.getWorldData(levelName);

        if (worldData != null && worldData.containsKey(node)) {
            worldData.remove(node);
            this.setWorldData(levelName, worldData);
        }
    }

    public void setData(Map<String, Object> data) {
        this.plugin.getProvider().setGroupData(this, data);
    }

    public void setDefault(String levelName) {
        if (levelName == null) {
            this.setNode("isDefault", true);
        } else {
            Map<String, Object> worldData = this.getWorldData(levelName);
            if (worldData != null) {
                worldData.put("isDefault", true);
                this.setWorldData(levelName, worldData);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public boolean setGroupPermission(String permission, String levelName) {
        if (levelName == null) {
            Map<String, Object> tempGroupData = this.getData();
            List<String> permissions = (List<String>) tempGroupData.computeIfAbsent("permissions", k -> new ArrayList<String>());
            
            if (!permissions.contains(permission)) {
                permissions.add(permission);
                this.setData(tempGroupData);
            }
        } else {
            Map<String, Object> worldData = this.getWorldData(levelName);
            if (worldData != null) {
                List<String> permissions = (List<String>) worldData.computeIfAbsent("permissions", k -> new ArrayList<String>());
                
                if (!permissions.contains(permission)) {
                    permissions.add(permission);
                    this.setWorldData(levelName, worldData);
                }
            }
        }

        this.plugin.updatePlayersInGroup(this);
        return true;
    }

    public void setNode(String node, Object value) {
        Map<String, Object> tempGroupData = this.getData();
        tempGroupData.put(node, value);
        this.setData(tempGroupData);
    }

    @SuppressWarnings("unchecked")
    public void setWorldData(String levelName, Map<String, Object> worldData) {
        Map<String, Object> tempGroupData = this.getData();
        Map<String, Object> worlds = (Map<String, Object>) tempGroupData.get("worlds");

        if (worlds != null && worlds.containsKey(levelName)) {
            worlds.put(levelName, worldData);
            this.setData(tempGroupData);
        }
    }

    public void setWorldNode(String levelName, String node, Object value) {
        Map<String, Object> worldData = this.getWorldData(levelName);
        if (worldData != null) {
            worldData.put(node, value);
            this.setWorldData(levelName, worldData);
        }
    }

    @SuppressWarnings("unchecked")
    public void sortPermissions() {
        Map<String, Object> tempGroupData = this.getData();

        if (tempGroupData.containsKey("permissions")) {
            List<String> permissions = (List<String>) tempGroupData.get("permissions");
            Set<String> uniquePerms = new HashSet<>(permissions);
            List<String> sortedPerms = new ArrayList<>(uniquePerms);
            Collections.sort(sortedPerms);
            tempGroupData.put("permissions", sortedPerms);
        }

        this.setData(tempGroupData);
    }

    @SuppressWarnings("unchecked")
    public boolean unsetGroupPermission(String permission, String levelName) {
        if (levelName == null) {
            Map<String, Object> tempGroupData = this.getData();
            if (!tempGroupData.containsKey("permissions")) return false;

            List<String> permissions = (List<String>) tempGroupData.get("permissions");
            if (!permissions.contains(permission)) return false;

            permissions.remove(permission);
            this.setData(tempGroupData);
        } else {
            Map<String, Object> worldData = this.getWorldData(levelName);
            if (worldData == null || !worldData.containsKey("permissions")) return false;

            List<String> permissions = (List<String>) worldData.get("permissions");
            if (!permissions.contains(permission)) return false;

            permissions.remove(permission);
            this.setWorldData(levelName, worldData);
        }

        this.plugin.updatePlayersInGroup(this);
        return true;
    }
  }

