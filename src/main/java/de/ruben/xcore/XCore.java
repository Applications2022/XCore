package de.ruben.xcore;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import de.ruben.xcore.changelog.XChangelog;
import de.ruben.xcore.clan.XClan;
import de.ruben.xcore.currency.XCurrency;
import de.ruben.xcore.customenchantment.XEnchantment;
import de.ruben.xcore.gamble.XGamble;
import de.ruben.xcore.gamble.thread.GameThread;
import de.ruben.xcore.itemstorage.XItemStorage;
import de.ruben.xcore.job.XJobs;
import de.ruben.xcore.nextevent.NextEventCommand;
import de.ruben.xcore.placeholder.DataPlaceHolderExpansion;
import de.ruben.xcore.placeholder.EventPlaceHolderExpansion;
import de.ruben.xcore.profile.XProfile;
import de.ruben.xcore.scoreboard.XScoreBoard;
import de.ruben.xcore.stock.XStocks;
import de.ruben.xcore.subsystem.SubSystem;
import de.ruben.xcore.thread.RecentDataUpdateThread;
import de.ruben.xcore.thread.ScoreboardUpdateThread;
import de.ruben.xcore.tutorialcenter.XTutorialCenter;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.storage.MongoDBStorage;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Score;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import java.util.List;

public final class XCore extends JavaPlugin {

    private static XCore instance;

    private ScoreboardUpdateThread scoreboardThread;

    private RecentDataUpdateThread recentDataUpdateThread;

    private GameThread gameThread;

    private List<SubSystem> subSystems;

    private MongoDBStorage mongoDBStorage;

    private RedissonClient redissonClient;


    @Override
    public void onEnable() {
        instance = this;

//        this.mongoDBStorage = new MongoDBStorage(XDevApi.getInstance(), "localhost", "Currency", 27017, MongoClientOptions.builder().codecRegistry(CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()))).build());
        this.mongoDBStorage = new MongoDBStorage(XDevApi.getInstance(), 10, "localhost", "admin", 27017, "currency", "rni1PbUbxYp4JTrPaMb8",  MongoClientOptions.builder().codecRegistry(CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()))).build());

        mongoDBStorage.connect();

        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379");

        this.redissonClient = Redisson.create(config);

        System.out.println("Redis connected successfully!");

        this.subSystems = setSubSystems();

        subSystems.forEach(SubSystem::onEnable);

        this.scoreboardThread = new ScoreboardUpdateThread();

        this.recentDataUpdateThread = new RecentDataUpdateThread();

        this.gameThread = new GameThread();

        saveConfig();

        EventPlaceHolderExpansion.setDate2Long(getConfig().getLong("nextEvent.date"));
        EventPlaceHolderExpansion.setTitle(getConfig().getString("nextEvent.title"));
        EventPlaceHolderExpansion.setInfo(getConfig().getString("nextEvent.info"));

        new DataPlaceHolderExpansion().register();
        new EventPlaceHolderExpansion().register();

        getCommand("nextevent").setExecutor(new NextEventCommand());

        Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
            scoreboardThread.start();
            recentDataUpdateThread.start();
            gameThread.start();
            System.out.println("Threads Started!");
        }, 20*5);



    }

    @Override
    public void onDisable() {
        scoreboardThread.interrupt();
        recentDataUpdateThread.interrupt();
        gameThread.interrupt();

        subSystems.forEach(SubSystem::onDisable);

        mongoDBStorage.disconnect();
        redissonClient.shutdown();
    }

    public static XCore getInstance() {
        return instance;
    }

    public RecentDataUpdateThread getRecentDataUpdateThread() {
        return recentDataUpdateThread;
    }

    public Thread getScoreboardThread() {
        return scoreboardThread;
    }

    public List<SubSystem> setSubSystems(){
        return List.of(new XItemStorage(), new XCurrency(), new XProfile(), new XEnchantment(), new XChangelog(), new XStocks(), new XScoreBoard(), new XJobs(), new XTutorialCenter(), new XClan(), new XGamble());
    }

    public void setNextEventDate(Long date){
        getConfig().set("nextEvent.date", date);
        saveConfig();
    }

    public void setNextEventTitle(String title){
        getConfig().set("nextEvent.title", title);
        saveConfig();
    }

    public void setNextEventInfo(String title){
        getConfig().set("nextEvent.info", title);
        saveConfig();
    }

    public MongoDBStorage getMongoDBStorage() {
        return mongoDBStorage;
    }

    public RedissonClient getRedissonClient() {
        return redissonClient;
    }
}
