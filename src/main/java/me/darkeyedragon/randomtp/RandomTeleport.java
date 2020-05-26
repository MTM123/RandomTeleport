package me.darkeyedragon.randomtp;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import me.darkeyedragon.randomtp.command.TeleportCommand;
import me.darkeyedragon.randomtp.command.context.PlayerWorldContext;
import me.darkeyedragon.randomtp.config.ConfigHandler;
import me.darkeyedragon.randomtp.listener.WorldLoadListener;
import me.darkeyedragon.randomtp.location.LocationFactory;
import me.darkeyedragon.randomtp.location.LocationSearcher;
import me.darkeyedragon.randomtp.validator.ChunkValidator;
import me.darkeyedragon.randomtp.validator.ValidatorFactory;
import me.darkeyedragon.randomtp.world.WorldQueue;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class RandomTeleport extends JavaPlugin {

    private HashMap<UUID, Long> cooldowns;
    private PaperCommandManager manager;
    private List<ChunkValidator> validatorList;
    //private Map<World, BlockingQueue<Location>> worldQueueMap;
    private WorldQueue worldQueue;
    private ConfigHandler configHandler;
    private LocationSearcher locationSearcher;
    private LocationFactory locationFactory;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        manager = new PaperCommandManager(this);
        configHandler = new ConfigHandler(this);
        locationFactory = new LocationFactory(configHandler);
        locationSearcher = new LocationSearcher(this);
        //check if the first argument is a world or player
        worldQueue = new WorldQueue(locationSearcher);
        manager.getCommandContexts().registerContext(PlayerWorldContext.class, c -> {
            String arg1 = c.popFirstArg();
            World world = Bukkit.getWorld(arg1);
            Player player = Bukkit.getPlayer(arg1);
            if (world != null) {
                PlayerWorldContext context = new PlayerWorldContext();
                context.setWorld(world);
                return context;
            } else if (player != null) {
                PlayerWorldContext context = new PlayerWorldContext();
                context.setPlayer(player);
                return context;
            } else {
                throw new InvalidCommandArgument(true);
            }
        });
        cooldowns = new HashMap<>();
        manager.registerCommand(new TeleportCommand(this));
        getServer().getPluginManager().registerEvents(new WorldLoadListener(this), this);
        validatorList = new ArrayList<>();
        configHandler.getPlugins().forEach(s -> {
            if (getServer().getPluginManager().getPlugin(s) != null) {
                try {
                    ChunkValidator validator = ValidatorFactory.createFrom(s);
                    if (validator != null) {
                        validatorList.add(validator);
                        getLogger().info(s + " loaded as validator.");
                    }
                } catch (IllegalArgumentException ignored) {
                    getLogger().warning(s + " is not a valid validator. Make sure it is spelled correctly.");
                }

            } else {
                getLogger().warning(s + " is not a valid plugin or is not loaded!");
            }
        });
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Unregistering commands...");
        manager.unregisterCommands();
        worldQueue.clear();
    }

    /*private void populateWorldQueue() {
        for (World world : configHandler.getWorlds()) {
            worldQueue.put(world, new ArrayBlockingQueue<>(configHandler.getQueueSize()));
        }
    }

    public void populateQueue() {
        populateWorldQueue();
        worldQueueMap.forEach((world, locations) -> addToLocationQueue(configHandler.getQueueSize(), world));
    }

    public void addToLocationQueue(int amount, World world) {
        Queue<Location> queue = worldQueueMap.get(world);
        WorldConfigSection worldConfigSection = locationFactory.getWorldConfigSection(world);
        if(worldConfigSection == null) return;
        if (queue.size() + amount > configHandler.getQueueSize()) {
            getLogger().info("Skipped searching for location in " + world.getName() + " queue already full.");
            return;
        }
        for (int i = 0; i < amount; i++) {
            locationHelper.getRandomLocation(worldConfigSection).thenAccept(location -> {
                queue.offer(location);
                if (configHandler.getDebugShowQueuePopulation())
                    getLogger().info("Safe location added for " + world.getName() + " (" + queue.size() + "/" + configHandler.getQueueSize() + ")");
            });
        }
    }*/

    public HashMap<UUID, Long> getCooldowns() {
        return cooldowns;
    }

    public List<ChunkValidator> getValidatorList() {
        return validatorList;
    }

    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    public WorldQueue getWorldQueue() {
        return worldQueue;
    }

    /*public Map<World, BlockingQueue<Location>> getWorldQueueMap() {
        return worldQueueMap;
    }

    public void clearWorldQueueMap() {
        worldQueueMap.clear();
    }

    public Location popLocation(World world) {
        Queue<Location> queue = getQueue(world);
        Location location = queue.poll();
        if (configHandler.getDebugShowQueuePopulation())
            getLogger().info("Location removed from " + world.getName() + "(" + queue.size() + "/" + configHandler.getQueueSize() + ")");
        return location;
    }*/

    public Queue<Location> getQueue(World world) {
        return worldQueue.get(world);
    }

    public LocationSearcher getLocationSearcher() {
        return locationSearcher;
    }

    public PaperCommandManager getManager() {
        return manager;
    }

    public LocationFactory getLocationFactory() {
        return locationFactory;
    }
}
