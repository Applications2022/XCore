package de.ruben.xcore.profile.service;

import com.mongodb.client.model.Filters;
import de.ruben.xcore.XCore;
import de.ruben.xcore.profile.XProfile;
import de.ruben.xcore.profile.model.PlayerProfile;
import de.ruben.xcore.profile.model.TransferData;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.storage.MongoDBStorage;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.cache2k.Cache;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ProfileService {

    private final String collection = "Data_Profile";

    public ProfileService() {
    }

    public void pushProfile(UUID uuid){
        if(getUpdatesMap().containsKey(uuid)) {
            Document document = new Document();
            document.append("$set", getUpdatesMap().get(uuid).toDocument());

            getMongoDBStorage().getMongoDatabase().getCollection(collection).findOneAndUpdate(Filters.eq("_id", uuid), document);

            getUpdatesMap().remove(uuid);
        }
    }

    public void updateProfile(UUID uuid, PlayerProfile playerProfile, Consumer<PlayerProfile> callback){
        XDevApi.getInstance().getxScheduler().async(() -> {
            putInCache(uuid, playerProfile);
            updateUpdatesMap(uuid, playerProfile);
            callback.accept(playerProfile);
        });
    }

    public PlayerProfile updateProfile(UUID uuid, PlayerProfile playerProfile){
        putInCache(uuid, playerProfile);
        updateUpdatesMap(uuid, playerProfile);
        return playerProfile;
    }

    public void getProfileAsync(UUID uuid, Consumer<PlayerProfile> callback){
        if(getCache().containsKey(uuid)){
            callback.accept(getCache().get(uuid));
        }else{
            getMongoDBStorage().getDocumentByBson(collection, Filters.eq("_id", uuid)).thenAccept(result -> {
                if(result == null){
                    callback.accept(createProfile(uuid));
                }else{

                    PlayerProfile playerProfile = new PlayerProfile().fromDocument(result);
                    putInCache(uuid, playerProfile);
                    updateUpdatesMap(uuid, playerProfile);
                    callback.accept(playerProfile);
                }
            });
        }
    }

    public PlayerProfile getProfile(UUID uuid){
        if(getCache().containsKey(uuid)){
            return getCache().get(uuid);
        }else{
            Document result = getMongoDBStorage().getMongoDatabase().getCollection(collection).find(Filters.eq("_id", uuid)).first();

            if(result == null){
                return createProfile(uuid);
            }else{
                PlayerProfile playerProfile = new PlayerProfile().fromDocument(result);
                putInCache(uuid, playerProfile);
                updateUpdatesMap(uuid, playerProfile);
                return playerProfile;
            }
        }
    }

    private PlayerProfile createProfile(UUID uuid){
        PlayerProfile playerProfile = new PlayerProfile(
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                0L,
                0L,
                new TransferData(0,0),
                0,
                0,
                0,
                0
        );

        putInCache(uuid, playerProfile);
        updateProfile(uuid, playerProfile);

        getMongoDBStorage().insertOneDocument(collection, playerProfile.toDocument(uuid));

        return playerProfile;
    }

    public void putInCache(UUID uuid, PlayerProfile playerProfile){

        if(Bukkit.getPlayer(uuid) == null) return;

        if(getCache().containsKey(uuid)){
            getCache().replace(uuid, playerProfile);
        }else{
            getCache().putIfAbsent(uuid, playerProfile);
        }
    }

    public void updateUpdatesMap(UUID uuid, PlayerProfile playerProfile){
        if(getUpdatesMap().containsKey(uuid)){
            getUpdatesMap().replace(uuid, playerProfile);
        }else{
            getUpdatesMap().put(uuid, playerProfile);
        }
    }

    public void removeFromCache(UUID uuid){
        if(getCache().containsKey(uuid)){
            getCache().remove(uuid);
        }
    }

    private PlayerProfile getCachedprofile(UUID uuid){
        if(getCache().containsKey(uuid)){
            return getCache().get(uuid);
        }else{
            return null;
        }
    }

    public Cache<UUID, PlayerProfile> getCache(){
        return XProfile.getInstance().getProfileCache();
    }

    public MongoDBStorage getMongoDBStorage(){
        return XProfile.getInstance().getMongoDBStorage();
    }

    public Map<UUID, PlayerProfile> getUpdatesMap(){
        return XCore.getInstance().getRecentDataUpdateThread().getUpdatesMap();
    }
    
}
