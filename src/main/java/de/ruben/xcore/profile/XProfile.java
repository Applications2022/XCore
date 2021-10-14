package de.ruben.xcore.profile;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import de.ruben.xcore.XCore;
import de.ruben.xcore.currency.codec.TransactionCodec;
import de.ruben.xcore.profile.command.AdminProfileCommand;
import de.ruben.xcore.profile.listener.PlayerListener;
import de.ruben.xcore.profile.model.PlayerProfile;
import de.ruben.xcore.profile.service.ProfileService;
import de.ruben.xcore.subsystem.SubSystem;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.storage.MongoDBStorage;
import org.bson.codecs.configuration.CodecRegistries;
import org.bukkit.Bukkit;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;

import java.util.UUID;

public class XProfile implements SubSystem {

    private MongoDBStorage mongoDBStorage;

    private static XProfile instance;

    private Cache<UUID, PlayerProfile> profileCache;

    @Override
    public void onEnable(){
        instance = this;
//        this.mongoDBStorage = new MongoDBStorage(XDevApi.getInstance(), "localhost", "Profile", 27017, MongoClientOptions.builder().codecRegistry(MongoClient.getDefaultCodecRegistry()).build());
        this.mongoDBStorage = new MongoDBStorage(XDevApi.getInstance(), 10, "localhost", "Currency", 27017, "currency", "wrgO4FTbV6UyLwtMzfsp", MongoClientOptions.builder().codecRegistry(CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(), CodecRegistries.fromCodecs(new TransactionCodec()))).build());

        mongoDBStorage.connect();

        this.profileCache = Cache2kBuilder.of(UUID.class, PlayerProfile.class)
                .name("profileCache")
                .eternal(true)
                .entryCapacity(150)
                .build();

        Bukkit.getPluginManager().registerEvents(new PlayerListener(), XCore.getInstance());

        XCore.getInstance().getCommand("adminprofile").setExecutor(new AdminProfileCommand());
    }

    @Override
    public void onDisable(){
        ProfileService profileService = new ProfileService();

        profileService.getCache().keys().forEach(profileService::pushProfile);

        mongoDBStorage.disconnect();
        profileCache.close();
    }

    public MongoDBStorage getMongoDBStorage() {
        return mongoDBStorage;
    }

    public static XProfile getInstance() {
        return instance;
    }

    public Cache<UUID, PlayerProfile> getProfileCache() {
        return profileCache;
    }
}
