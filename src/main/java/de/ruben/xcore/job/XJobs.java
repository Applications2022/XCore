package de.ruben.xcore.job;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import de.ruben.xcore.XCore;
import de.ruben.xcore.currency.codec.TransactionCodec;
import de.ruben.xcore.job.command.JobCommand;
import de.ruben.xcore.job.listener.JobListener;
import de.ruben.xcore.job.metrix.JobXpMetrix;
import de.ruben.xcore.job.metrix.LevelMetrix;
import de.ruben.xcore.job.model.JobPlayer;
import de.ruben.xcore.job.model.JobTopPlayers;
import de.ruben.xcore.stock.model.StockContainer;
import de.ruben.xcore.subsystem.SubSystem;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.custom.bossbar.XBossBar;
import de.ruben.xdevapi.storage.MongoDBStorage;
import org.bson.codecs.configuration.CodecRegistries;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Boss;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitTask;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class XJobs implements SubSystem {

    private Cache<UUID, JobPlayer> jobPlayerCache;

    private Cache<UUID, BossBar> jobPlayerBossbars;

    private static XJobs instance;

    private LinkedHashMap<Material, Integer> minerMap;

    private LinkedHashMap<Material, Integer> farmerMap;

    private LinkedHashMap<EntityType, Integer> hunterMap;

    private Map<Integer, Integer> levelMap;

    private LinkedHashMap<Material, Integer> gräberMap;

    private LinkedHashMap<Material, Integer> holzfällerMap;

    private ConcurrentHashMap<UUID, BukkitTask> bossBarHideTasks;

    private JobTopPlayers jobTopPlayers;

    @Override
    public void onEnable() {
        this.instance = this;

        this.jobPlayerCache = Cache2kBuilder.of(UUID.class, JobPlayer.class)
                .name("jobPlayerCache")
                .entryCapacity(150)
                .eternal(true)
                .build();

        this.jobPlayerBossbars = Cache2kBuilder.of(UUID.class, BossBar.class)
                .name("jobPlayerBossbars")
                .entryCapacity(150)
                .eternal(true)
                .build();

        this.jobTopPlayers = new JobTopPlayers(new HashMap<>());

        Bukkit.getPluginManager().registerEvents(new JobListener(), XCore.getInstance());

        XCore.getInstance().getCommand("job").setExecutor(new JobCommand());

        this.bossBarHideTasks = new ConcurrentHashMap<>();

        this.minerMap = new LinkedHashMap<>();

        minerMap.put(Material.STONE, 1);
        minerMap.put(Material.DIORITE, 1);
        minerMap.put(Material.ANDESITE, 1);
        minerMap.put(Material.GRANITE, 1);
        minerMap.put(Material.COBBLESTONE, 1);
        minerMap.put(Material.COAL_ORE, 2);
        minerMap.put(Material.REDSTONE_ORE, 5);
        minerMap.put(Material.IRON_ORE, 10);
        minerMap.put(Material.LAPIS_ORE, 15);
        minerMap.put(Material.GOLD_ORE, 20);
        minerMap.put(Material.GILDED_BLACKSTONE, 20);
        minerMap.put(Material.NETHER_GOLD_ORE, 20);
        minerMap.put(Material.OBSIDIAN, 25);
        minerMap.put(Material.CRYING_OBSIDIAN, 25);
        minerMap.put(Material.DIAMOND_ORE, 50);
        minerMap.put(Material.EMERALD_ORE, 75);
        minerMap.put(Material.ANCIENT_DEBRIS, 150);

        this.farmerMap = new LinkedHashMap<>();

        farmerMap.put(Material.WHEAT, 2);
        farmerMap.put(Material.CARROTS, 2);
        farmerMap.put(Material.POTATOES, 2);
        farmerMap.put(Material.BEETROOTS, 2);
        farmerMap.put(Material.SUGAR_CANE, 5);
        farmerMap.put(Material.COCOA_BEANS, 5);
        farmerMap.put(Material.PUMPKIN, 5);
        farmerMap.put(Material.MELON, 5);
        farmerMap.put(Material.CACTUS, 5);
        farmerMap.put(Material.BAMBOO, 5);
        farmerMap.put(Material.BROWN_MUSHROOM, 10);
        farmerMap.put(Material.RED_MUSHROOM, 10);
        farmerMap.put(Material.BROWN_MUSHROOM_BLOCK, 20);
        farmerMap.put(Material.RED_MUSHROOM_BLOCK, 20);

        this.hunterMap = new LinkedHashMap<>();

        hunterMap.put(EntityType.PIG, 2);
        hunterMap.put(EntityType.COW, 2);
        hunterMap.put(EntityType.MUSHROOM_COW, 2);
        hunterMap.put(EntityType.CHICKEN, 2);
        hunterMap.put(EntityType.SHEEP, 5);
        hunterMap.put(EntityType.LLAMA, 5);
        hunterMap.put(EntityType.PARROT, 5);
        hunterMap.put(EntityType.POLAR_BEAR, 5);
        hunterMap.put(EntityType.CAT, 5);
        hunterMap.put(EntityType.WOLF, 5);
        hunterMap.put(EntityType.PANDA, 5);
        hunterMap.put(EntityType.SKELETON, 10);
        hunterMap.put(EntityType.SPIDER, 10);
        hunterMap.put(EntityType.PIGLIN, 10);
        hunterMap.put(EntityType.ZOMBIFIED_PIGLIN, 10);
        hunterMap.put(EntityType.ENDERMITE, 10);
        hunterMap.put(EntityType.CREEPER, 15);
        hunterMap.put(EntityType.BEE, 15);
        hunterMap.put(EntityType.PHANTOM, 15);
        hunterMap.put(EntityType.ENDERMAN, 15);
        hunterMap.put(EntityType.BLAZE, 25);
        hunterMap.put(EntityType.WITHER_SKELETON, 25);

        this.levelMap = new HashMap<>();

        levelMap.put(1, 1000);
        levelMap.put(2, 2000);
        levelMap.put(3, 3000);
        levelMap.put(4, 4000);
        levelMap.put(5, 5000);
        levelMap.put(6, 7500);
        levelMap.put(7, 10000);
        levelMap.put(8, 15000);
        levelMap.put(9, 20000);
        levelMap.put(10, 25000);
        levelMap.put(11, 30000);
        levelMap.put(12, 35000);
        levelMap.put(13, 40000);
        levelMap.put(14, 45000);
        levelMap.put(15, 50000);
        levelMap.put(16, 60000);
        levelMap.put(17, 70000);
        levelMap.put(18, 80000);
        levelMap.put(19, 90000);
        levelMap.put(20, 100000);
        levelMap.put(21, 120000);
        levelMap.put(22, 140000);
        levelMap.put(23, 160000);
        levelMap.put(24, 180000);
        levelMap.put(25, 200000);
        levelMap.put(26, 220000);
        levelMap.put(27, 240000);
        levelMap.put(28, 260000);
        levelMap.put(29, 280000);
        levelMap.put(30, 300000);
        levelMap.put(31, 350000);
        levelMap.put(32, 400000);
        levelMap.put(33, 450000);
        levelMap.put(34, 500000);
        levelMap.put(35, 550000);
        levelMap.put(36, 600000);
        levelMap.put(37, 650000);
        levelMap.put(38, 700000);
        levelMap.put(39, 750000);
        levelMap.put(40, 800000);

        this.gräberMap = new LinkedHashMap<>();

        gräberMap.put(Material.DIRT, 1);
        gräberMap.put(Material.GRASS_BLOCK, 1);
        gräberMap.put(Material.SAND, 2);
        gräberMap.put(Material.RED_SAND, 2);
        gräberMap.put(Material.GRAVEL, 5);
        gräberMap.put(Material.SOUL_SAND, 5);
        gräberMap.put(Material.SOUL_SOIL, 5);

        this.holzfällerMap = new LinkedHashMap<>();

        for (Material value : Material.values()) {
            if(value.name().toLowerCase().contains("log")){
                holzfällerMap.put(value, 5);
            }
        }

        holzfällerMap.put(Material.CRIMSON_STEM, 10);
        holzfällerMap.put(Material.WARPED_STEM, 10);

    }

    @Override
    public void onDisable() {
        jobPlayerCache.clearAndClose();
    }

    public static XJobs getInstance() {
        return instance;
    }

    public MongoDBStorage getMongoDBStorage() {
        return XCore.getInstance().getMongoDBStorage();
    }

    public Cache<UUID, JobPlayer> getJobPlayerCache() {
        return jobPlayerCache;
    }

    public Cache<UUID, BossBar> getJobPlayerBossbars() {
        return jobPlayerBossbars;
    }

    public LinkedHashMap<Material, Integer> getMinerMap() {
        return minerMap;
    }

    public LinkedHashMap<Material, Integer> getFarmerMap() {
        return farmerMap;
    }

    public LinkedHashMap<EntityType, Integer> getHunterMap() {
        return hunterMap;
    }

    public Map<Integer, Integer> getLevelMap() {
        return levelMap;
    }

    public LinkedHashMap<Material, Integer> getHolzfällerMap() {
        return holzfällerMap;
    }

    public LinkedHashMap<Material, Integer> getGräberMap() {
        return gräberMap;
    }

    public void setJobTopPlayers(JobTopPlayers jobTopPlayers) {
        this.jobTopPlayers = jobTopPlayers;
    }

    public JobTopPlayers getJobTopPlayers() {
        return jobTopPlayers;
    }

    public ConcurrentHashMap<UUID, BukkitTask> getBossBarHideTasks() {
        return bossBarHideTasks;
    }
}
