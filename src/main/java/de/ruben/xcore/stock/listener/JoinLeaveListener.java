package de.ruben.xcore.stock.listener;

import de.ruben.xcore.stock.XStocks;
import de.ruben.xcore.stock.service.HoldingService;
import de.ruben.xdevapi.XDevApi;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinLeaveListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onLeave(PlayerQuitEvent event){
        XDevApi.getInstance().getxScheduler().async(() -> {
            new HoldingService(event.getPlayer().getUniqueId()).trimHoldings();
            new HoldingService(event.getPlayer().getUniqueId()).removePlayerFromCache();
            XStocks.getInstance().getHeadsCache().remove(event.getPlayer().getUniqueId());
        });
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent event){
        XDevApi.getInstance().getxScheduler().async(() ->{
            XStocks.getInstance().getHeadsCache().put(event.getPlayer().getUniqueId(), ItemBuilder.from(Material.PLAYER_HEAD).setSkullOwner(Bukkit.getOfflinePlayer(event.getPlayer().getUniqueId())).build());
        } );

    }
}
