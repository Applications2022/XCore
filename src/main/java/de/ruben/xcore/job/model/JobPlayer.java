package de.ruben.xcore.job.model;

import de.ruben.xcore.job.metrix.LevelMetrix;
import de.ruben.xdevapi.XDevApi;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.bson.Document;
import org.bukkit.entity.Player;

import javax.print.Doc;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class JobPlayer {

    private UUID uuid;
    private Map<String, Job> jobData;

    public Document toDocument(){
        Document document = new Document("_id", uuid);

        jobData.forEach((s, job) -> {
            document.append(s, new Document("_id", s).append("level", job.getLevel()).append("xp", job.getCurentXP()).append("prestige", job.getPrestige()));
        });

        return document;
    }

    public JobPlayer fromDocument(Document document){
        Map<String, Job> localJobMap = new HashMap<>();

        document.forEach((s, o) -> {
            if(!s.equals("_id")) {
                Document fetchedDocument = (Document) o;

                localJobMap.put(s, new Job(fetchedDocument.getInteger("level"), fetchedDocument.getDouble("xp"), fetchedDocument.getInteger("prestige")));
            }
        });

        this.uuid = document.get("_id", UUID.class);
        this.jobData = localJobMap;

        return this;
    }

    public Integer getJobLevel(String job){
        return getJob(job).getLevel();
    }

    public Double getJobXp(String job){

        return getJob(job).getCurentXP();
    }

    public Integer getJobPrestige(String job){
        return getJob(job).getPrestige();
    }

    public Job getJob(String job){
        if(getJobData().containsKey(job)) {
            return getJobData().get(job);
        }else{
            Map<String, Job> jobData = getJobData();

            jobData.putIfAbsent(job, new Job(1, 0.0, 1));

            setJobData(jobData);

            return new Job(1, 0.0, 1);

        }
    }

    public void setJobLevel(String jobName, Integer integer){
        Job job = getJob(jobName);
        job.setLevel(integer);
        getJobData().replace(jobName, job);
    }

    public void setJobPrestige(String jobName, Integer integer){
        Job job = getJob(jobName);
        job.setPrestige(integer);
        getJobData().replace(jobName, job);
    }

    public void addJobPrestige(String jobName, Integer integer){
        Job job = getJob(jobName);
        job.setPrestige(job.getPrestige()+integer);
        getJobData().replace(jobName, job);
    }

    public void setJobXp(String jobName, Double xp){
        Job job = getJob(jobName);
        job.setCurentXP(xp);
        getJobData().replace(jobName, job);
    }

    public void addJobLevel(String jobName, Integer integer){
        Job job = getJob(jobName);
        job.setLevel(job.getLevel()+integer);
        getJobData().replace(jobName, job);
    }


    public void addJobXp(String jobName, Double xp){
        int currentLevel = getJobLevel(jobName);
        Job job = getJob(jobName);

        double finalXp = job.getCurentXP() + xp;

        if(LevelMetrix.getXpNeeded(currentLevel) <= finalXp){
            finalXp = finalXp-LevelMetrix.getXpNeeded(currentLevel);
            addJobLevel(jobName, 1);
        }

        job.setCurentXP(finalXp);
        getJobData().replace(jobName, job);
    }

    public void addJobXp(String jobName, Double xp, Player player){
        int currentLevel = getJobLevel(jobName);
        Job job = getJob(jobName);

        double finalXp = job.getCurentXP() + xp;

        if(LevelMetrix.getXpNeeded(currentLevel) <= finalXp){
            finalXp = finalXp-LevelMetrix.getXpNeeded(currentLevel);

            if(player != null && player.isOnline()) {
                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "§7Du bist nun §b" + jobName + " §7lvl.§b" +(job.getLevel()+1)+"§7.");
            }
            addJobLevel(jobName, 1);
        }

        job.setCurentXP(finalXp);
        getJobData().replace(jobName, job);
    }
}
