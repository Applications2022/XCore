package de.ruben.xcore.thread;

import de.ruben.xcore.job.XJobs;
import de.ruben.xcore.job.model.JobTopPlayers;
import de.ruben.xcore.job.service.JobService;
import de.ruben.xcore.profile.model.PlayerProfile;
import de.ruben.xcore.profile.service.ProfileService;
import de.ruben.xdevapi.XDevApi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RecentDataUpdateThread extends Thread{

    final int interval = 20*10;

    final ProfileService profileService = new ProfileService();

    private final Map<UUID, PlayerProfile> updatesMap;
    private JobService jobService;

    public RecentDataUpdateThread(){
        this.updatesMap = new ConcurrentHashMap<>();
        this.jobService = new JobService();
    }

    @Override
    public void run() {
        while (true){
            try {
                sleep(interval* 50L);
                getUpdatesMap().keySet().forEach(uuid -> profileService.pushProfile(uuid));
                jobService.pushJobPlayerCache();

                JobTopPlayers jobTopPlayers = new JobTopPlayers(new HashMap<>());

                jobService.getAllJobPlayers().forEach(jobPlayer -> {
                    jobPlayer.getJobData().forEach((s, job) -> {
                        jobTopPlayers.addPlayer(s, jobPlayer);
                    });
                });

                jobTopPlayers.recalculateTopPlayers();

                XJobs.getInstance().setJobTopPlayers(jobTopPlayers);
            } catch (InterruptedException e) {
                XDevApi.getInstance().consoleMessage("RecentDataUpdateThread interrupted!", true);
            }
        }
    }

    public Map<UUID, PlayerProfile> getUpdatesMap() {
        return updatesMap;
    }
}
