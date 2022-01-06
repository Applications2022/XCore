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

    private static XProfile instance;

    private Cache<UUID, PlayerProfile> profileCache;

    @Override
    public void onEnable(){
        instance = this;

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

        profileCache.close();
    }

    public MongoDBStorage getMongoDBStorage() {
        return XCore.getInstance().getMongoDBStorage();
    }

    public static XProfile getInstance() {
        return instance;
    }

    public Cache<UUID, PlayerProfile> getProfileCache() {
        return profileCache;
    }
}
