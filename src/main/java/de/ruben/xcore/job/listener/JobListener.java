package de.ruben.xcore.job.listener;

import de.ruben.xcore.XCore;
import de.ruben.xcore.currency.service.CashService;
import de.ruben.xcore.job.metrix.JobXpMetrix;
import de.ruben.xcore.job.model.Job;
import de.ruben.xcore.job.service.BossbarDisplayService;
import de.ruben.xdevapi.XDevApi;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import de.ruben.xcore.job.service.JobService;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;

import javax.print.attribute.standard.JobSheets;

public class JobListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        if(event.isCancelled()){
            return;
        }

            Block block = event.getBlock();

            if (block != null && !block.hasMetadata("placed")) {
                Material material = block.getType();

                if (material != null && material != Material.AIR) {

                    JobService jobService = new JobService(event.getPlayer().getUniqueId());

                    String job = "";
                    Integer xp = 0;
                    if (JobXpMetrix.hasXpMiner(material)) {
                        job = "Miner";
                        xp = JobXpMetrix.getXpMiner(material);
                    } else if (JobXpMetrix.hasXpFarmer(material)) {
                        job = "Farmer";
                        xp = JobXpMetrix.getXpFarmer(material);
                    } else if (JobXpMetrix.hasXpGräber(material)) {
                        job = "Gräber";
                        xp = JobXpMetrix.getXpGräber(material);
                    } else if (JobXpMetrix.hasXpLumberjack(material)) {
                        job = "Holzfäller";
                        xp = JobXpMetrix.getXpLumberjack(material);
                    } else {
                        job = "";
                        xp = 0;
                    }

                    if (!job.equals("") && xp != 0) {
                        Job jobModel = jobService.getJobPlayer().getJob(job);
                        int level = jobModel.getLevel();
                        double money = ((level * 0.025) + (xp*0.025) ) * jobModel.getPrestige();
                        new CashService().addValue(event.getPlayer().getUniqueId(), money);
                        jobService.addJobXp(job, xp);
                        if (!block.hasMetadata("brokenBlock")) {
                            new BossbarDisplayService(event.getPlayer().getUniqueId(), jobService).showBossbar(job, xp);
                        }
                    }


                }
            }

    }

    @EventHandler
    public void playerKillEvent(EntityDeathEvent event){
        if(event.isCancelled()){
            return;
        }

            if (event.getEntity().getKiller() != null) {
                Player player = event.getEntity().getKiller();

                if (JobXpMetrix.hasXpHunter(event.getEntityType())) {
                    int xp = JobXpMetrix.getXpHunter(event.getEntityType());
                    JobService jobService = new JobService(player.getUniqueId());
                    jobService.addJobXp("Jäger", xp);
                    new BossbarDisplayService(player.getUniqueId(), jobService).showBossbar("Jäger", xp);
                }
            }
    }

    @EventHandler
    public void placeBlocks(BlockPlaceEvent event){
        if(event.isCancelled()){
            return;
        }

        if(!event.getPlayer().hasPermission("addictzone.jobs.place.admin")){
            XDevApi.getInstance().getxScheduler().async(() -> {
                event.getBlockPlaced().setMetadata("placed", new FixedMetadataValue(XCore.getInstance(), true));
            });
        }
    }
}
