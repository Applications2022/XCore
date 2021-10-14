package de.ruben.xcore.thread;

import de.ruben.xcore.profile.model.PlayerProfile;
import de.ruben.xcore.profile.service.ProfileService;
import de.ruben.xdevapi.XDevApi;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RecentDataUpdateThread extends Thread{

    final int interval = 20*10;

    final ProfileService profileService = new ProfileService();

    private final Map<UUID, PlayerProfile> updatesMap;

    public RecentDataUpdateThread(){
        this.updatesMap = new ConcurrentHashMap<>();
    }

    @Override
    public void run() {
        while (true){
            try {
                sleep(interval* 50L);
                getUpdatesMap().keySet().forEach(uuid -> profileService.pushProfile(uuid));
            } catch (InterruptedException e) {
                XDevApi.getInstance().consoleMessage("RecentDataUpdateThread interrupted!", true);
            }
        }
    }

    public Map<UUID, PlayerProfile> getUpdatesMap() {
        return updatesMap;
    }
}
