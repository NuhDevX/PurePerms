package org.PP.NuhDev.provider;

import org.PP.NuhDev.PPGroup;
import org.PP.NuhDev.PurePerms;
import org.PP.NuhDev.task.PPMySQLTask;
import org.powernukkitx.IPlayer;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

public class MySQLProvider implements ProviderInterface {

    private Connection db;
    private final PurePerms plugin;
    private final Map<String, Map<String, Object>> groupsData = new HashMap<>();

    @SuppressWarnings("unchecked")
    public MySQLProvider(PurePerms plugin) {
        this.plugin = plugin;

        Map<String, Object> mySQLSettings = (Map<String, Object>) this.plugin.getConfigValue("mysql-settings");

        if (mySQLSettings == null || !mySQLSettings.containsKey("host") || !mySQLSettings.containsKey("port") ||
            !mySQLSettings.containsKey("user") || !mySQLSettings.containsKey("password") || !mySQLSettings.containsKey("db")) {
            throw new RuntimeException("Failed to connect to the MySQL database: Invalid MySQL settings");
        }

        try {
            String url = "jdbc:mysql://" + mySQLSettings.get("host") + ":" + mySQLSettings.get("port") + "/" + mySQLSettings.get("db");
            this.db = DriverManager.getConnection(url, (String) mySQLSettings.get("user"), (String) mySQLSettings.get("password"));
            runSqlScript("mysql_deploy_01.sql");

            this.loadGroupsData();

            this.plugin.getScheduler().scheduleRepeatingTask(new PPMySQLTask(this.plugin, this.db), 1200);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to the MySQL database: " + e.getMessage());
        }
    }

    private void runSqlScript(String resourceName) {
        try (InputStream is = this.plugin.getResource(resourceName);
             Statement stmt = this.db.createStatement()) {
            if (is != null) {
                String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                String[] queries = sql.split(";");
                for (String query : queries) {
                    if (!query.trim().isEmpty()) {
                        stmt.execute(query.trim());
                    }
                }
            }
        } catch (Exception e) {
            this.plugin.getLogger().error("Error executing SQL script: " + resourceName, e);
        }
    }

    @Override
    public Map<String, Object> getGroupData(PPGroup group) {
        String groupName = group.getName();
        if (!this.groupsData.containsKey(groupName)) {
            return new HashMap<>();
        }
        return new HashMap<>(this.groupsData.get(groupName));
    }

    @Override
    public Map<String, Object> getGroupsData() {
        return new HashMap<>(this.groupsData);
    }

    @Override
    public Map<String, Object> getPlayerData(IPlayer player) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("userName", player.getName());
        userData.put("group", this.plugin.getDefaultGroup().getName());
        userData.put("permissions", new ArrayList<String>());
        Map<String, Object> worlds = new HashMap<>();
        userData.put("worlds", worlds);

        String sql1 = "SELECT * FROM players WHERE userName = ?";
        try (PreparedStatement ps1 = this.db.prepareStatement(sql1)) {
            ps1.setString(1, player.getName());
            try (ResultSet rs1 = ps1.executeQuery()) {
                if (rs1.next()) {
                    userData.put("group", rs1.getString("userGroup"));
                    userData.put("permissions", new ArrayList<>(Arrays.asList(rs1.getString("permissions").split(","))));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String sql2 = "SELECT * FROM players_mw WHERE userName = ?";
        try (PreparedStatement ps2 = this.db.prepareStatement(sql2)) {
            ps2.setString(1, player.getName());
            try (ResultSet rs2 = ps2.executeQuery()) {
                while (rs2.next()) {
                    String worldName = rs2.getString("worldName");
                    Map<String, Object> worldData = new HashMap<>();
                    worldData.put("group", rs2.getString("userGroup"));
                    worldData.put("permissions", new ArrayList<>(Arrays.asList(rs2.getString("permissions").split(","))));
                    worlds.put(worldName, worldData);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return userData;
    }

    @Override
    public Map<String, Object> getUsers() {
        // TODO
        return null;
    }

    @SuppressWarnings("unchecked")
    public void loadGroupsData() {
        this.groupsData.clear();

        try (Statement stmt = this.db.createStatement();
             ResultSet rsCheck = stmt.executeQuery("SELECT COUNT(*) FROM groups")) {
            
            if (rsCheck.next() && rsCheck.getInt(1) <= 0) {
                this.plugin.getLogger().notice("No groups found in table 'groups', loading groups defined in default SQL script");
                runSqlScript("mysql_deploy_02.sql");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (Statement stmt = this.db.createStatement();
             ResultSet rs1 = stmt.executeQuery("SELECT * FROM groups")) {
            while (rs1.next()) {
                String groupName = rs1.getString("groupName");
                Map<String, Object> gData = new HashMap<>();
                gData.put("alias", rs1.getString("alias"));
                gData.put("isDefault", rs1.getString("isDefault").equals("1"));
                
                String inheritance = rs1.getString("inheritance");
                gData.put("inheritance", inheritance.isEmpty() ? new ArrayList<String>() : Arrays.asList(inheritance.split(",")));
                
                gData.put("permissions", Arrays.asList(rs1.getString("permissions").split(",")));
                gData.put("worlds", new HashMap<String, Object>());
                
                this.groupsData.put(groupName, gData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (Statement stmt = this.db.createStatement();
             ResultSet rs2 = stmt.executeQuery("SELECT * FROM groups_mw")) {
            while (rs2.next()) {
                String groupName = rs2.getString("groupName");
                String worldName = rs2.getString("worldName");
                
                if (this.groupsData.containsKey(groupName)) {
                    Map<String, Object> worlds = (Map<String, Object>) this.groupsData.get(groupName).get("worlds");
                    Map<String, Object> wData = new HashMap<>();
                    wData.put("isDefault", rs2.getString("isDefault").equals("1"));
                    wData.put("permissions", Arrays.asList(rs2.getString("permissions").split(",")));
                    worlds.put(worldName, wData);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeGroupData(String groupName) {
        String sql1 = "DELETE FROM groups WHERE groupName = ?";
        String sql2 = "DELETE FROM groups_mw WHERE groupName = ?"; 
        
        try (PreparedStatement ps1 = this.db.prepareStatement(sql1);
             PreparedStatement ps2 = this.db.prepareStatement(sql2)) {
            ps1.setString(1, groupName);
            ps1.executeUpdate();
            
            ps2.setString(1, groupName);
            ps2.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setGroupData(PPGroup group, Map<String, Object> tempGroupData) {
        this.updateGroupData(group.getName(), tempGroupData);
        this.loadGroupsData();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setGroupsData(Map<String, Object> tempGroupsData) {
        List<String> toRemove = new ArrayList<>();
        for (String currentGroup : this.groupsData.keySet()) {
            if (!tempGroupsData.containsKey(currentGroup)) {
                toRemove.add(currentGroup);
            }
        }
        
        for (String groupName : toRemove) {
            this.removeGroupData(groupName);
        }

        for (Map.Entry<String, Object> entry : tempGroupsData.entrySet()) {
            this.updateGroupData(entry.getKey(), (Map<String, Object>) entry.getValue());
        }

        this.loadGroupsData();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setPlayerData(IPlayer player, Map<String, Object> tempUserData) {
        if (tempUserData.containsKey("group") && tempUserData.containsKey("permissions")) {
            String userName = player.getName();
            String userGroup = (String) tempUserData.get("group");
            String permissions = String.join(",", (List<String>) tempUserData.get("permissions"));

            String sql = "INSERT INTO players (userName, userGroup, permissions) VALUES (?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE userGroup = VALUES(userGroup), permissions = VALUES(permissions)";
            
            try (PreparedStatement ps = this.db.prepareStatement(sql)) {
                ps.setString(1, userName);
                ps.setString(2, userGroup);
                ps.setString(3, permissions);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (tempUserData.containsKey("worlds")) {
                Map<String, Map<String, Object>> worlds = (Map<String, Map<String, Object>>) tempUserData.get("worlds");
                String sqlMw = "INSERT INTO players_mw (userName, worldName, userGroup, permissions) VALUES (?, ?, ?, ?) " +
                               "ON DUPLICATE KEY UPDATE userGroup = VALUES(userGroup), permissions = VALUES(permissions)";
                
                try (PreparedStatement psMw = this.db.prepareStatement(sqlMw)) {
                    for (Map.Entry<String, Map<String, Object>> entry : worlds.entrySet()) {
                        psMw.setString(1, userName);
                        psMw.setString(2, entry.getKey());
                        psMw.setString(3, (String) entry.getValue().get("group"));
                        psMw.setString(4, String.join(",", (List<String>) entry.getValue().get("permissions")));
                        psMw.executeUpdate();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void updateGroupData(String groupName, Map<String, Object> tempGroupData) {
        if (tempGroupData.containsKey("isDefault") && tempGroupData.containsKey("inheritance") && tempGroupData.containsKey("permissions")) {
            String alias = (String) tempGroupData.getOrDefault("alias", "");
            String isDefault = (Boolean) tempGroupData.get("isDefault") ? "1" : "0";
            String inheritance = String.join(",", (List<String>) tempGroupData.get("inheritance"));
            String permissions = String.join(",", (List<String>) tempGroupData.get("permissions"));

            String sql = "INSERT INTO groups (groupName, alias, isDefault, inheritance, permissions) VALUES (?, ?, ?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE alias = VALUES(alias), isDefault = VALUES(isDefault), inheritance = VALUES(inheritance), permissions = VALUES(permissions)";
            
            try (PreparedStatement ps = this.db.prepareStatement(sql)) {
                ps.setString(1, groupName);
                ps.setString(2, alias);
                ps.setString(3, isDefault);
                ps.setString(4, inheritance);
                ps.setString(5, permissions);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (tempGroupData.containsKey("worlds")) {
                Map<String, Map<String, Object>> worlds = (Map<String, Map<String, Object>>) tempGroupData.get("worlds");
                String sqlMw = "INSERT INTO groups_mw (groupName, isDefault, worldName, permissions) VALUES (?, ?, ?, ?) " +
                               "ON DUPLICATE KEY UPDATE isDefault = VALUES(isDefault), worldName = VALUES(worldName), permissions = VALUES(permissions)";
                
                try (PreparedStatement psMw = this.db.prepareStatement(sqlMw)) {
                    for (Map.Entry<String, Map<String, Object>> entry : worlds.entrySet()) {
                        String isDefWorld = (Boolean) entry.getValue().get("isDefault") ? "1" : "0";
                        String worldPerms = String.join(",", (List<String>) entry.getValue().get("permissions"));
                        
                        psMw.setString(1, groupName);
                        psMw.setString(2, isDefWorld);
                        psMw.setString(3, entry.getKey());
                        psMw.setString(4, worldPerms);
                        psMw.executeUpdate();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void close() {
        try {
            if (this.db != null && !this.db.isClosed()) {
                this.db.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

