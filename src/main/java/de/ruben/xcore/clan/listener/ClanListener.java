package de.ruben.xcore.clan.listener;

import de.ruben.xcore.clan.service.ClanPlayerService;
import de.ruben.xcore.clan.service.ClanService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ClanListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        new ClanPlayerService().loadIntoCache(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        new ClanPlayerService().removeFromCache(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event){
        String message = event.getMessage();
        if(message.toLowerCase().startsWith("#clan ") || message.toLowerCase().startsWith("#cc ")){
            event.setCancelled(true);

            String newMessage = message.toLowerCase().startsWith("#clan ") ? message.substring(6) : message.substring(4);

            new ClanService().getClanChat(new ClanPlayerService().getClan(event.getPlayer().getUniqueId())).sendPlayerMessage(event.getPlayer(), newMessage);
        }
    }
}
