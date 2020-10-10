package me.darkeyedragon.randomtp.sponge;

import com.google.inject.Inject;
import me.darkeyedragon.randomtp.api.addon.PluginLocationValidator;
import me.darkeyedragon.randomtp.api.config.RandomConfigHandler;
import me.darkeyedragon.randomtp.api.queue.WorldQueue;
import me.darkeyedragon.randomtp.common.eco.EcoHandler;
import me.darkeyedragon.randomtp.common.plugin.RandomTeleportPluginImpl;
import me.darkeyedragon.randomtp.sponge.eco.SpongeEcoHandler;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;

import java.util.Optional;
import java.util.Set;

@Plugin(
        id = "sponge",
        name = "SpongeRandomTeleport",
        version = "1.0-SNAPSHOT"
)
public class SpongeRandomTeleport extends RandomTeleportPluginImpl {

    @Inject
    private Logger logger;

    @Inject
    private Game game;
    @Inject
    private PluginContainer plugin;
    private WorldQueue worldQueue;
    private EcoHandler ecoHandler;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        super.init();
        worldQueue = new WorldQueue();
    }

    @Override
    public EcoHandler getEcoHandler() {
        return ecoHandler;
    }

    @Override
    public Set<PluginLocationValidator> getValidatorList() {
        return null;
    }

    @Override
    public boolean setupEconomy() {
        Optional<EconomyService> serviceOpt = Sponge.getServiceManager().provide(EconomyService.class);
        if (!serviceOpt.isPresent()) {
            return false;
        }
        EconomyService economyService = serviceOpt.get();
        ecoHandler = new SpongeEcoHandler(plugin, economyService);
        return true;
    }

    @Override
    public RandomConfigHandler getConfigHandler() {
        return null;
    }

    @Override
    public WorldQueue getWorldQueue() {
        return null;
    }
}