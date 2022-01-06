package de.ruben.xcore.scoreboard;

import de.ruben.xcore.XCore;
import de.ruben.xcore.subsystem.SubSystem;
import net.minecraft.server.v1_16_R3.Scoreboard;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class XScoreBoard implements SubSystem {

    private static Map<UUID, Scoreboard> scoreBoardsbyUUID;

    @Override
    public void onEnable() {
        this.scoreBoardsbyUUID = new HashMap<>();

        Bukkit.getPluginManager().registerEvents(new JoinLeaveListener(), XCore.getInstance());
    }

    @Override
    public void onDisable() {

    }

    public static Map<UUID, Scoreboard> getScoreBoardsbyUUID() {
        return scoreBoardsbyUUID;
    }
}
