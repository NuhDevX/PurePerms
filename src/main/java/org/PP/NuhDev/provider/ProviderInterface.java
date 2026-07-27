package org.PP.NuhDev.provider;

import org.PP.NuhDev.PPGroup;
import org.powernukkitx.IPlayer;

import java.util.Map;

public interface ProviderInterface {

    Map<String, Object> getGroupData(PPGroup group);

    Map<String, Object> getGroupsData();

    Map<String, Object> getPlayerData(IPlayer player);

    Map<String, Object> getUsers();

    void setGroupData(PPGroup group, Map<String, Object> tempGroupData);

    void setGroupsData(Map<String, Object> tempGroupsData);

    void setPlayerData(IPlayer player, Map<String, Object> tempPlayerData);

    void close();
}

