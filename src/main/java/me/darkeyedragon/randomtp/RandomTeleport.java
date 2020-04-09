package me.darkeyedragon.randomtp;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import me.darkeyedragon.randomtp.command.TeleportCommand;
import me.darkeyedragon.randomtp.command.context.PlayerWorldContext;
import me.darkeyedragon.randomtp.config.ConfigHandler;
import me.darkeyedragon.randomtp.util.LocationHelper;
import me.darkeyedragon.randomtp.validator.ChunkValidator;
import me.darkeyedragon.randomtp.validator.ValidatorFactory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class RandomTeleport extends JavaPlugin {

    private HashMap<UUID, Long> cooldowns;
    private PaperCommandManager manager;
    private List<ChunkValidator> validatorList;
    private Map<World, BlockingQueue<Location>> worldQueueMap;
    private ConfigHandler configHandler;
    private LocationHelper locationHelper;

    @Override
    public void onEnable() {
        // Plugin startup logic
        manager = new PaperCommandManager(this);
        configHandler = new ConfigHandler(this);
        locationHelper = new LocationHelper(this);
        //check if the first argument is a world or player
        worldQueueMap = new HashMap<>();
        manager.getCommandContexts().registerContext(PlayerWorldContext.class, c -> {
            String arg1 = c.getArgs().get(0);
            World world = Bukkit.getWorld(arg1);
            Player player = Bukkit.getPlayer(arg1);
            if (world != null) {
                var context = new PlayerWorldContext();
                context.setWorld(true);
                context.setWorld(world);
                return context;
            } else if (player != null) {
                var context = new PlayerWorldContext();
                context.setPlayer(true);
                context.setPlayer(player);
                return context;
            }
            throw new InvalidCommandArgument(true);
        });
        cooldowns = new HashMap<>();
        saveDefaultConfig();
        manager.registerCommand(new TeleportCommand(this));
        validatorList = new ArrayList<>();
        configHandler.getPlugins().forEach(s -> {
            if (getServer().getPluginManager().getPlugin(s) != null) {
                var validator = ValidatorFactory.createFrom(s);
                if (validator != null) {
                    validatorList.add(validator);
                    getLogger().info(s + " loaded as validator.");
                }
            } else {
                getLogger().warning(s + " is not a valid plugin or is not loaded!");
            }
        });
        populateWorldQueue();
        worldQueueMap.forEach((world, locations) -> addToLocationQueue(configHandler.getQueueSize(), world));
    }

    private void populateWorldQueue() {
        for (World world : Bukkit.getWorlds()) {
            if (configHandler.getWorldsBlacklist().contains(world)) {
                if (configHandler.isWhitelist()) {
                    worldQueueMap.put(world, new ArrayBlockingQueue<>(configHandler.getQueueSize()));
                }
            } else {
                if (!configHandler.isWhitelist()) {
                    worldQueueMap.put(world, new ArrayBlockingQueue<>(configHandler.getQueueSize()));
                }
            }
        }
        getLogger().warning(worldQueueMap.size() + " size");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Unregistering commands...");
        manager.unregisterCommands();
        worldQueueMap.clear();
    }

public void addToLocationQueue(int amount, World world) {
    for (int i = 0; i < amount; i++) {
        Queue<Location> queue = worldQueueMap.get(world);
        int offsetX;
        int offsetZ;
        if(configHandler.useWorldBorder()){
            offsetX = world.getWorldBorder().getCenter().getBlockX();
            offsetZ = world.getWorldBorder().getCenter().getBlockZ();
        }else{
            offsetX = configHandler.getOffsetX();
            offsetZ = configHandler.getOffsetZ();
        }
        locationHelper.getRandomLocation(world, configHandler.getRadius(), offsetX, offsetZ).thenAccept(location -> {
            queue.offer(location);
            getLogger().info("Safe location added for " + world.getName() +"("+queue.size()+"/"+configHandler.getQueueSize()+")");
        });
    }
}

    public HashMap<UUID, Long> getCooldowns() {
        return cooldowns;
    }

    public List<ChunkValidator> getValidatorList() {
        return validatorList;
    }

    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    public Map<World, BlockingQueue<Location>> getWorldQueueMap() {
        return worldQueueMap;
    }
    public Location popLocation(World world){
        Queue<Location> queue = getQueue(world);
        Location location = queue.poll();
        getLogger().info("Location removed from "+ world.getName() +"("+queue.size()+"/"+configHandler.getQueueSize()+")");
        return location;
    }
    public Queue<Location> getQueue(World world) {
        return getWorldQueueMap().get(world);
    }

    public LocationHelper getLocationHelper() {
        return locationHelper;
    }
}
