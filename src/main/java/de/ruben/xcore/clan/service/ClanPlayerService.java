package de.ruben.xcore.clan.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import de.ruben.xcore.XCore;
import de.ruben.xcore.clan.XClan;
import de.ruben.xcore.clan.model.Clan;
import de.ruben.xcore.clan.model.ClanPlayer;
import de.ruben.xdevapi.XDevApi;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.cache2k.Cache;

import java.util.UUID;

public class ClanPlayerService {

    public void setClan(UUID uuid, UUID clanID){
        ClanPlayer clanPlayer = getClanPlayer(uuid);
        clanPlayer.setClanId(clanID);

        if(Bukkit.getOfflinePlayer(uuid).isOnline()) {
            getCache().replace(uuid, clanPlayer);
        }

        XDevApi.getInstance().getxScheduler().async(() -> {
            getCollection().replaceOne(Filters.eq("_id", uuid), clanPlayer);
        });
    }

    public ClanPlayer createPlayer(UUID uuid){
        ClanPlayer clanPlayer = new ClanPlayer(uuid, null);

        getCache().putIfAbsent(uuid, clanPlayer);

        XDevApi.getInstance().getxScheduler().async(() -> {
            getCollection().insertOne(clanPlayer);
        });

        return clanPlayer;
    }

    public void loadIntoCache(UUID uuid){
        XDevApi.getInstance().getxScheduler().async(() -> {
            ClanPlayer clanPlayer = getCollection().find(Filters.eq("_id", uuid)).first();

            if(clanPlayer == null){
                createPlayer(uuid);
            }else{
                getCache().putIfAbsent(clanPlayer.getId(), clanPlayer);
            }

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);

            if(clanPlayer.getClanId() != null) {
                new ClanService().getClanChat(clanPlayer.getClanId()).sendLogJoin(offlinePlayer);
            }
        });
    }

    public void removeFromCache(UUID uuid){
        if(isInClan(uuid)) {
            new ClanService().getClanChat(getClan(uuid)).sendLogQuit(Bukkit.getOfflinePlayer(uuid));
        }
        getCache().remove(uuid);
    }

    public Clan getClan(UUID uuid){
        return new ClanService().getClan(getClanPlayer(uuid).getClanId());
    }

    public boolean isInClan(UUID uuid){
        return getClanPlayer(uuid).getClanId() == null ? false : true;
    }

    public ClanPlayer getClanPlayer(UUID uuid){
        if(getCache().containsKey(uuid)) {
            return getCache().get(uuid);
        }else{
            ClanPlayer clanPlayer = getCollection().find(Filters.eq("_id", uuid)).first();

            if(clanPlayer == null){
                return createPlayer(uuid);
            }else{
                return clanPlayer;
            }
        }
    }

    private MongoCollection<ClanPlayer> getCollection(){
        return XCore.getInstance().getMongoDBStorage().getMongoClient().getDatabase("Clan").getCollection("Data_ClanPlayers", ClanPlayer.class);
    }

    private Cache<UUID, ClanPlayer> getCache(){
        return XClan.getInstance().getClanPlayerCache();
    }
}
