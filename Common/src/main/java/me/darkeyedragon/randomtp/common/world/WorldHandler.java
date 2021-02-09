package me.darkeyedragon.randomtp.common.world;

import me.darkeyedragon.randomtp.api.config.RandomConfigHandler;
import me.darkeyedragon.randomtp.api.config.section.subsection.SectionWorldDetail;
import me.darkeyedragon.randomtp.api.plugin.RandomTeleportPlugin;
import me.darkeyedragon.randomtp.api.queue.LocationQueue;
import me.darkeyedragon.randomtp.api.queue.QueueListener;
import me.darkeyedragon.randomtp.api.queue.WorldQueue;
import me.darkeyedragon.randomtp.api.world.RandomWorld;
import me.darkeyedragon.randomtp.api.world.RandomWorldHandler;
import me.darkeyedragon.randomtp.api.world.location.RandomLocation;
import me.darkeyedragon.randomtp.common.world.location.search.LocationSearcherFactory;

public abstract class WorldHandler implements RandomWorldHandler {

    private final WorldQueue worldQueue;
    private final RandomTeleportPlugin<?> plugin;

    public WorldHandler(RandomTeleportPlugin<?> plugin) {
        this.plugin = plugin;
        worldQueue = new WorldQueue();
    }

    @Override
    public WorldQueue getWorldQueue() {
        return worldQueue;
    }

    @Override
    public void populateWorldQueue() {
        RandomConfigHandler configHandler = plugin.getConfigHandler();
        plugin.getLogger().info("Populating WorldQueue");
        long startTime = System.currentTimeMillis();
        for (RandomWorld world : configHandler.getSectionWorld().getWorlds()) {
            //Add a new world to the world queue and generate random locations
            LocationQueue locationQueue = new LocationQueue(configHandler.getSectionQueue().getSize(), LocationSearcherFactory.getLocationSearcher(world, plugin));

            //Subscribe to the locationqueue to be notified of changes
            subscribe(locationQueue, world);
            locationQueue.subscribe(new QueueListener<RandomLocation>() {
                @Override
                public void onAdd(RandomLocation element) {

                }

                @Override
                public void onRemove(RandomLocation element) {

                }
            });
            SectionWorldDetail sectionWorldDetail = configHandler.getSectionWorld().getSectionWorldDetail(world);
            locationQueue.generate(sectionWorldDetail);
            worldQueue.put(world, locationQueue);
        }
        plugin.getLogger().info("WorldQueue population finished in " + (System.currentTimeMillis() - startTime) + "ms");

    }

    public void subscribe(LocationQueue locationQueue, RandomWorld world) {
        if (plugin.getConfigHandler().getSectionDebug().isShowQueuePopulation()) {
            int size = plugin.getConfigHandler().getSectionQueue().getSize();
            locationQueue.subscribe(new QueueListener<RandomLocation>() {
                @Override
                public void onAdd(RandomLocation location) {
                    plugin.getLogger().info("Safe location added for " + world.getName() + " (" + locationQueue.size() + "/" + size + ")");
                }

                @Override
                public void onRemove(RandomLocation element) {
                    plugin.getLogger().info("Safe location consumed for " + world.getName() + " (" + locationQueue.size() + "/" + size + ")");
                }
            });
        }
    }

    @Override
    public abstract RandomWorld getWorld(String worldName);
}