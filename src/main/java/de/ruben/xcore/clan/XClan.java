package de.ruben.xcore.clan;

import de.ruben.xcore.XCore;
import de.ruben.xcore.changelog.model.Changelog;
import de.ruben.xcore.clan.command.ClanCommand;
import de.ruben.xcore.clan.gui.conversation.ClanCreateConversation;
import de.ruben.xcore.clan.gui.conversation.ClanDeleteConversation;
import de.ruben.xcore.clan.listener.ClanListener;
import de.ruben.xcore.clan.model.Clan;
import de.ruben.xcore.clan.model.ClanPlayer;
import de.ruben.xcore.clan.model.ClanStatus;
import de.ruben.xcore.clan.service.ClanPlayerService;
import de.ruben.xcore.clan.service.ClanService;
import de.ruben.xcore.currency.account.CashAccount;
import de.ruben.xcore.subsystem.SubSystem;
import de.ruben.xdevapi.storage.MongoDBStorage;
import org.bukkit.Bukkit;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class XClan implements SubSystem {

    private static XClan instance;

    private Cache<UUID, Clan> clanCache;

    private Cache<UUID, ClanPlayer> clanPlayerCache;

    private ClanCreateConversation clanCreateConversation;

    private ClanDeleteConversation clanDeleteConversation;

    @Override
    public void onEnable() {
        this.instance = this;

        this.clanCreateConversation = new ClanCreateConversation();
        this.clanDeleteConversation = new ClanDeleteConversation();

        this.clanCache = Cache2kBuilder.of(UUID.class, Clan.class)
                .name("clanCache")
                .eternal(true)
                .entryCapacity(150)
                .build();

        this.clanPlayerCache = Cache2kBuilder.of(UUID.class, ClanPlayer.class)
                .name("clanPlayerCache")
                .eternal(true)
                .entryCapacity(150)
                .build();

        new ClanService().loadClans();

        Bukkit.getPluginManager().registerEvents(new ClanListener(), XCore.getInstance());

        XCore.getInstance().getCommand("clan").setExecutor(new ClanCommand());

//        for(int i = 0; i < 100; i++){
//            new ClanService().createClan(UUID.fromString("5196f1d6-c02a-4a1f-8da7-31d5d0a21fe0"), "OP Clan #"+i, "OP"+i);
//        }
    }

    @Override
    public void onDisable() {
        clanCache.clearAndClose();
    }

    public static XClan getInstance() {
        return instance;
    }

    public MongoDBStorage getMongoDBStorage(){
        return XCore.getInstance().getMongoDBStorage();
    }

    public Cache<UUID, Clan> getClanCache() {
        return clanCache;
    }

    public Cache<UUID, ClanPlayer> getClanPlayerCache() {
        return clanPlayerCache;
    }

    public ClanCreateConversation getClanCreateConversation() {
        return clanCreateConversation;
    }

    public ClanDeleteConversation getClanDeleteConversation(){
        return clanDeleteConversation;
    }
}
