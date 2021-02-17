package me.darkeyedragon.randomtp.failsafe;

import me.darkeyedragon.randomtp.api.failsafe.DeathTracker;
import me.darkeyedragon.randomtp.api.plugin.RandomTeleportPlugin;
import me.darkeyedragon.randomtp.api.world.RandomPlayer;

import java.util.HashMap;
import java.util.Map;

public class SpigotDeathTracker implements DeathTracker {

    RandomTeleportPlugin<?> randomTeleport;
    Map<RandomPlayer, Long> trackedPlayers;

    public SpigotDeathTracker(RandomTeleportPlugin<?> plugin) {
        this.randomTeleport = plugin;
        trackedPlayers = new HashMap<>();
    }


    @Override
    public Long add(RandomPlayer player, long time) {
        return trackedPlayers.put(player, time);
    }

    @Override
    public boolean contains(RandomPlayer player) {
        return trackedPlayers.containsKey(player);
    }

    @Override
    public Long remove(RandomPlayer player) {
        return trackedPlayers.remove(player);
    }
}