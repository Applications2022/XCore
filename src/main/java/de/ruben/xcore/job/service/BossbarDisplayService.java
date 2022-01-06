package de.ruben.xcore.job.service;

import de.ruben.xcore.XCore;
import de.ruben.xcore.job.XJobs;
import de.ruben.xcore.job.metrix.LevelMetrix;
import de.ruben.xcore.stock.XStocks;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.custom.bossbar.XBossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BossbarDisplayService {

    private UUID uuid;
    private JobService jobService;

    private ConcurrentHashMap<UUID, BukkitTask> hideBossbarTasks;

    public BossbarDisplayService(UUID uuid){
        this.uuid = uuid;
        this.jobService = new JobService(uuid);
        this.hideBossbarTasks = XJobs.getInstance().getBossBarHideTasks();
    }

    public BossbarDisplayService(UUID uuid, JobService jobService){
        this.uuid = uuid;
        this.jobService = jobService;
        this.hideBossbarTasks = XJobs.getInstance().getBossBarHideTasks();
    }

    public void showBossbar(String job, int xp){
        XDevApi.getInstance().getxScheduler().async(() -> {
            int jobLevel = jobService.getJobPlayer().getJobLevel(job);
            double jobXp = jobService.getJobPlayer().getJobXp(job);

            BossBar bossBar =
                    XJobs.getInstance().getJobPlayerBossbars().containsKey(uuid)
                            ? XJobs.getInstance().getJobPlayerBossbars().get(uuid)
                            : Bukkit.createBossBar("§7"+job+": §aLevel§7: §b"+jobLevel+" §7➜ §b"+(jobLevel+1) + " §7- §eXP: §b"+jobXp+"§7/§b"+LevelMetrix.getXpNeeded(jobLevel), BarColor.BLUE, BarStyle.SEGMENTED_20, BarFlag.CREATE_FOG);

            bossBar.removeFlag(BarFlag.CREATE_FOG);

            bossBar.setColor(getBarColor(job));

            bossBar.setTitle("§7"+job+": §aLevel§7: §b"+jobLevel+" §7➜ §b"+(jobLevel+1) + " §7- §eXP: §b"+jobXp+"§7/§b"+LevelMetrix.getXpNeeded(jobLevel));

            float progress = ((Double) (jobXp / LevelMetrix.getXpNeeded(jobLevel))).floatValue();

            bossBar.setProgress(progress);

            if((!XJobs.getInstance().getJobPlayerBossbars().containsKey(uuid)) ){
                bossBar.addPlayer(Bukkit.getPlayer(uuid));
                XJobs.getInstance().getJobPlayerBossbars().putIfAbsent(uuid, bossBar);
            }

            setHideTime(20*5);

            sendActionBar(job, xp);
        });

    }

    private void sendActionBar(String job, int xp){
        int level = jobService.getJobPlayer().getJobLevel(job);
        double money = ((level * 0.025) + (xp*0.025) ) * jobService.getJobPlayer().getJobPrestige(job);
        Bukkit.getPlayer(uuid).sendActionBar("§7"+job+": §e+"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(xp)+"XP §8| §a+"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(money)+"€");
    }

    private void setHideTime(long time){
        XDevApi.getInstance().getxScheduler().async(() -> {
            if(hideBossbarTasks.containsKey(uuid)){
                stopHideTask(uuid);

                hideBossbarTasks.replace(uuid, Bukkit.getScheduler().runTaskLater(XCore.getInstance(), () -> {

                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {


                        if (XJobs.getInstance().getJobPlayerBossbars().containsKey(uuid)) {
                            XJobs.getInstance().getJobPlayerBossbars().get(uuid).removePlayer(player);
                        }
                    }

                    stopHideTask(uuid);

                    XJobs.getInstance().getJobPlayerBossbars().remove(uuid);
                    hideBossbarTasks.remove(uuid);
                }, time));
            }else{
                hideBossbarTasks.put(uuid, Bukkit.getScheduler().runTaskLater(XCore.getInstance(), () -> {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        if (XJobs.getInstance().getJobPlayerBossbars().containsKey(uuid)) {
                            XJobs.getInstance().getJobPlayerBossbars().get(uuid).removePlayer(player);
                        }
                    }

                    stopHideTask(uuid);

                    XJobs.getInstance().getJobPlayerBossbars().remove(uuid);
                    hideBossbarTasks.remove(uuid);
                }, time));
            }



        });
    }

    private void stopHideTask(UUID uuid) {
        if (!hideBossbarTasks.containsKey(uuid))
            return;
        BukkitTask task = hideBossbarTasks.get(uuid);
        if (!task.isCancelled())
            task.cancel();
    }
    private BarColor getBarColor(String job){
        if(job.equalsIgnoreCase("miner")){
            return BarColor.BLUE;
        }else if(job.equalsIgnoreCase("farmer")){
            return BarColor.YELLOW;
        }else if(job.equalsIgnoreCase("holzfäller")){
            return BarColor.WHITE;
        }else if(job.equalsIgnoreCase("jäger")){
            return BarColor.RED;
        }else if(job.equalsIgnoreCase("gräber")){
            return BarColor.GREEN;
        }else{
            return BarColor.BLUE;
        }

    }


}
