package me.darkeyedragon.randomtp;

import org.bukkit.plugin.java.JavaPlugin;

public class SpigotImpl extends JavaPlugin {

    private RandomTeleport randomTeleport;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        randomTeleport = new RandomTeleport(this);
        randomTeleport.init();
    }

    @Override
    public void onDisable() {

    }
}