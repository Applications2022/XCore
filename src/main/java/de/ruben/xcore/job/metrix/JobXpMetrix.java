package de.ruben.xcore.job.metrix;

import com.codepoetics.protonpack.maps.MapStream;
import de.ruben.xcore.job.XJobs;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.*;
import java.util.stream.Collectors;

public class JobXpMetrix {


    public static Integer getXpMiner(Material material){
        return getXpMapMiner().getOrDefault(material, 0);
    }

    public static Integer getXpLumberjack(Material material){
        if(material.name().toUpperCase().contains("LOG")){
            return 5;
        }else if(material == Material.CRIMSON_STEM || material == Material.WARPED_STEM){
            return 10;
        }else{
            return 0;
        }
    }

    public static Integer getXpGräber(Material material){
        if(material == Material.DIRT || material == Material.GRASS_BLOCK){
            return 1;
        }else if(material == Material.SAND || material == Material.RED_SAND){
            return 2;
        }else if(material == Material.GRAVEL || material == Material.SOUL_SAND || material == Material.SOUL_SOIL){
            return 5;
        }else{
            return 0;
        }
    }


    public static Integer getXpFarmer(Material material){
        return getXpMapFarmer().getOrDefault(material, 0);
    }

    public static Integer getXpHunter(EntityType entityType){
        return getXpMapHunter().getOrDefault(entityType, 0);
    }

    public static boolean hasXpMiner(Material material){
        return getXpMapMiner().containsKey(material);
    }

    public static boolean hasXpLumberjack(Material material){
        return getXpLumberjack(material) != 0;
    }

    public static boolean hasXpGräber(Material material){
        return getXpGräber(material) != 0;
    }

    public static boolean hasXpFarmer(Material material){
        return getXpMapFarmer().containsKey(material);
    }

    public static boolean hasXpHunter(EntityType material){
        return getXpMapHunter().containsKey(material);
    }

    private static LinkedHashMap<Material, Integer> getXpMapMiner(){
        return XJobs.getInstance().getMinerMap();

    }

    private static LinkedHashMap<Material, Integer> getXpMapFarmer(){

        return XJobs.getInstance().getFarmerMap();

    }

    private static LinkedHashMap<EntityType, Integer> getXpMapHunter(){
        return XJobs.getInstance().getHunterMap();
    }

    public static LinkedHashMap<?, Integer> getMap(String job){
        if(job.equalsIgnoreCase("miner")){
            return getXpMapMiner();

        }else if(job.equalsIgnoreCase("farmer")){
            return getXpMapFarmer();
        }else if(job.equalsIgnoreCase("jäger")){
            return getXpMapHunter();
        }else if(job.equalsIgnoreCase("gräber")){
            return XJobs.getInstance().getGräberMap();
        }else if(job.equalsIgnoreCase("holzfäller")){
            return XJobs.getInstance().getHolzfällerMap();
        }else{
            return null;
        }
    }
}
