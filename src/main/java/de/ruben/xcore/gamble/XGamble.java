package de.ruben.xcore.gamble;

import de.ruben.xcore.XCore;
import de.ruben.xcore.clan.XClan;
import de.ruben.xcore.gamble.command.AdminGambleCommand;
import de.ruben.xcore.gamble.command.GambleCommand;
import de.ruben.xcore.gamble.model.GambleParticipant;
import de.ruben.xcore.gamble.model.GambleState;
import de.ruben.xcore.gamble.model.Game;
import de.ruben.xcore.gamble.model.GameState;
import de.ruben.xcore.gamble.service.GambleLocationService;
import de.ruben.xcore.gamble.service.GambleService;
import de.ruben.xcore.gamble.thread.GameThread;
import de.ruben.xcore.subsystem.SubSystem;
import de.ruben.xdevapi.storage.MongoDBStorage;
import me.filoghost.holographicdisplays.api.beta.hologram.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class XGamble implements SubSystem {

    private static XGamble instance;


    public ConcurrentHashMap<String, List<Location>> gambleLocations;
    public List<Hologram> gameHolograms;
    public List<Hologram> participantHolograms;
    public Game game;

    @Override
    public void onEnable() {
        this.instance = this;
        this.gambleLocations = new ConcurrentHashMap<String, List<Location>>();
        this.gameHolograms = new GambleService().spawnGameDisplayHolograms();
        this.participantHolograms = new GambleService().spawnParticipantDisplayHolograms();
        this.game = new Game(10, 1.0, 10.0, GameState.BEFORE_GAME, new HashMap<>(), Game.MultiplierState.UNKNOWN);

        XCore.getInstance().getCommand("admingamble").setExecutor(new AdminGambleCommand());
        XCore.getInstance().getCommand("gamble").setExecutor(new GambleCommand());

        Bukkit.getScheduler().runTaskLater(XCore.getInstance(), () -> {
            new GambleLocationService().loadLocations();
        }, 20*3);
    }

    @Override
    public void onDisable() {

        // TODO: Give all people money back!
    }

    public static XGamble getInstance() {
        return instance;
    }

    public MongoDBStorage getMongoDBStorage(){
        return XCore.getInstance().getMongoDBStorage();
    }

    public ConcurrentHashMap<String, List<Location>> getGambleLocations() {
        return gambleLocations;
    }

    public Game getGame() {
        return game;
    }

    public List<Hologram> getGameHolograms() {
        return gameHolograms;
    }

    public List<Hologram> getParticipantHolograms() {
        return participantHolograms;
    }
}
