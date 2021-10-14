package de.ruben.xcore.profile.listener;

import de.ruben.xcore.profile.gui.ProfileGui;
import de.ruben.xcore.profile.model.PlayerProfile;
import de.ruben.xcore.profile.service.ProfileService;
import de.ruben.xdevapi.XDevApi;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.*;

public class PlayerListener implements Listener {

    private final ProfileService profileService = new ProfileService();

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        XDevApi.getInstance().getxScheduler().async(() -> profileService.pushProfile(event.getPlayer().getUniqueId()));
        profileService.removeFromCache(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        PlayerProfile playerProfile = profileService.getProfile(event.getPlayer().getUniqueId());
        playerProfile.setLastJoin(System.currentTimeMillis());
        profileService.updateProfile(event.getPlayer().getUniqueId(), playerProfile);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event){
        String message = event.getMessage();

        if(!message.startsWith("/")){
            PlayerProfile playerProfile = profileService.getProfile(event.getPlayer().getUniqueId());
            playerProfile.setMessages(playerProfile.getMessages()+1);
            profileService.updateProfile(event.getPlayer().getUniqueId(), playerProfile);
        }
    }

    @EventHandler
    public void onCommandPreProcess(PlayerCommandPreprocessEvent event){
        if(event.getMessage().startsWith("/")){
            PlayerProfile playerProfile = profileService.getProfile(event.getPlayer().getUniqueId());
            playerProfile.setCommands(playerProfile.getCommands()+1);
            profileService.updateProfile(event.getPlayer().getUniqueId(), playerProfile);
        }
    }

    @EventHandler
    public void onKill(EntityDeathEvent event){
        if(event.getEntity() instanceof Player){
            PlayerProfile playerProfile = profileService.getProfile(event.getEntity().getUniqueId());
            playerProfile.setDied(playerProfile.getDied()+1);
            profileService.updateProfile(event.getEntity().getUniqueId(), playerProfile);

            if(event.getEntity().getKiller() instanceof Player){
                PlayerProfile killedProfile = profileService.getProfile(event.getEntity().getKiller().getUniqueId());
                killedProfile.setPlayerKills(killedProfile.getPlayerKills()+1);
                profileService.updateProfile(event.getEntity().getKiller().getUniqueId(), killedProfile);
            }
        }else{
            if(event.getEntity().getKiller() instanceof Player){
                PlayerProfile playerProfile = profileService.getProfile(event.getEntity().getKiller().getUniqueId());
                playerProfile.setMonsterKills(playerProfile.getMonsterKills()+1);
                profileService.updateProfile(event.getEntity().getKiller().getUniqueId(), playerProfile);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event){
        Player player = event.getPlayer();

        if(event.getRightClicked() != null && event.getRightClicked() instanceof Player){
            if(player.isSneaking()){
                Player target = (Player) event.getRightClicked();
                new ProfileGui(player, target.getUniqueId());
            }
        }
    }



}
