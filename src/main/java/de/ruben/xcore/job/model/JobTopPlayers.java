package de.ruben.xcore.job.model;

import de.ruben.xcore.job.XJobs;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
@Setter
public class JobTopPlayers {

    private HashMap<String, List<JobPlayer>> topPlayerMap;

    public JobTopPlayers(){
        this.topPlayerMap = XJobs.getInstance().getJobTopPlayers().getTopPlayerMap();
    }

    public List<JobPlayer> getTopPlayers(String job){
        return topPlayerMap.getOrDefault(job, new ArrayList<>());
    }

    public void addPlayer(String job, JobPlayer jobPlayer){
        List<JobPlayer> jobPlayers = new ArrayList<>();
        if(topPlayerMap.containsKey(job)){
            jobPlayers = topPlayerMap.get(job);
        }

        jobPlayers.add(jobPlayer);

        if(topPlayerMap.containsKey(job)){
            topPlayerMap.replace(job, jobPlayers);
        }else{
            topPlayerMap.putIfAbsent(job, jobPlayers);
        }
    }

    public void recalculateTopPlayers(){
        topPlayerMap.forEach((s, jobPlayers) -> {
            topPlayerMap.replace(s, jobPlayers.stream().sorted((o1, o2) -> o2.getJobLevel(s).compareTo(o1.getJobLevel(s))).limit(10).collect(Collectors.toList()));
        });
    }
}
