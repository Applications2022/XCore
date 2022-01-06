package de.ruben.xcore.scoreboard;

import de.ruben.xdevapi.XDevApi;
import org.bukkit.craftbukkit.libs.org.eclipse.sisu.Priority;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinLeaveListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event){
        XDevApi.getInstance().getxScheduler().asyncSchedule(() -> {
            if(!new PlayerSideBar().hasScoreBoard(event.getPlayer())){
                new PlayerSideBar().sendNewScoreBoard(event.getPlayer());
            }
        }, 20);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLeave(PlayerQuitEvent event){
        if(new PlayerSideBar().hasScoreBoard(event.getPlayer())){
            XScoreBoard.getScoreBoardsbyUUID().remove(event.getPlayer().getUniqueId());
        }
    }
}
