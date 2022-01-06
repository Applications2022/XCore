package de.ruben.xcore.job.metrix;

import de.ruben.xcore.job.XJobs;

import java.util.HashMap;
import java.util.Map;

public class LevelMetrix {

    public static Integer getXpNeeded(int level){
        if(level <= 40){
            return getXpNeeded().get(level);
        }else{
            return 1000000;
        }
    }


    private static Map<Integer, Integer> getXpNeeded(){
        return XJobs.getInstance().getLevelMap();
    }

}
