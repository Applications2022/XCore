package de.ruben.xcore.clan.service;

import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import de.ruben.xcore.XCore;
import de.ruben.xcore.clan.XClan;
import de.ruben.xcore.clan.model.*;
import de.ruben.xcore.clan.model.gui.FilterType;
import de.ruben.xcore.clan.model.gui.SortType;
import de.ruben.xdevapi.XDevApi;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.cache2k.Cache;

import java.util.*;
import java.util.stream.Collectors;

public class ClanService {

    public void sendJoinRequest(Clan clan, Player player){
        player.closeInventory();
        if(!clan.getJoinRequests().contains(player.getUniqueId())){
            if(!new ClanPlayerService().isInClan(player.getUniqueId())) {
                addJoinRequest(clan.getId(), player.getUniqueId());
                player.sendMessage("§7Du hast dem §b" + XDevApi.getInstance().getxUtil().getStringUtil().fullyFormattedString(clan.getTagColor() + clan.getName()) + " §7erfolgreich deine Beitrittsanfrage gesendet!");
                getClanChat(clan).sendLogMessage("§b"+player.getName()+" §7hat dem Clan eine Beitrittsanfrage gesendet!");
            }
        }else{
            player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Du hast diesem Clan bereits eine Beitrittsanfrage gesendet!");
        }
    }

    public void joinClan(Clan clan, Player player){
        player.closeInventory();
        if(!new ClanPlayerService().isInClan(player.getUniqueId())) {
            addnewClanMember(clan.getId(), player);
            new ClanPlayerService().setClan(player.getUniqueId(), clan.getId());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user "+player.getName()+" meta setsuffix 10000 §8["+ XDevApi.getInstance().getxUtil().getStringUtil().getLuckPermsFormattedString(clan.getTagColor()+clan.getTag())+"§8]");
            player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "§7Du bist dem §b" + XDevApi.getInstance().getxUtil().getStringUtil().fullyFormattedString(clan.getTagColor() + clan.getName()) + " §7Clan erfolgreich beigetreten!");
            getClanChat(clan).sendLogMessage("§b"+player.getName()+" §7ist dem Clan §2beigetreten§7!");
        }
    }

    public void leaveClan(Player player){
        player.closeInventory();
        if(new ClanPlayerService().isInClan(player.getUniqueId())) {
            Clan clan = new ClanPlayerService().getClan(player.getUniqueId());
            if (clan.getClanMembers().containsKey(player.getUniqueId().toString())) {
                if (clan.isOwner(player)) {
                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "§7Du bist der Besitzer dieses Clans! Um den Clan zu verlassen musst du ihn Löschen!");
                } else {
                    removeClanMember(clan.getId(), player.getUniqueId());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user "+player.getName()+" meta removesuffix 10000");

                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "§7Du hast den §b" + XDevApi.getInstance().getxUtil().getStringUtil().fullyFormattedString(clan.getTagColor() + clan.getName()) + " §7Clan erfolgreich verlassen!");
                    getClanChat(clan).sendLogMessage("§b"+player.getName()+" §7hat den Clan §cverlassen§7!");
                }
            }
        }else{
            player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Du kannst keinen Clan verlassen, da du in keinem Clan bist!");
        }
    }

    public void createClan(UUID ownerId, String name, String tag){
        createClan(UUID.randomUUID(), ownerId, name, tag);
    }

    public void createClan(UUID clanId, UUID ownerId, String name, String tag){
        Clan clan = new Clan(clanId, ownerId, UUID.randomUUID(), System.currentTimeMillis(), name, tag, "§7", 25, 0.0, new ArrayList<>(), new HashMap<>(), new HashMap<>(), ClanStatus.OPEN, new HashMap<>(), new Safe(1, ""));
        createClan(clan);
    }

    public void createClan(Clan clan){
        if(!existClan(clan.getId())){
            getCollection().insertOne(clan);
            getCache().putIfAbsent(clan.getId(),clan);

            ClanRank ownerRank = new ClanRank(UUID.randomUUID(), clan.getOwnerId(), System.currentTimeMillis(), "Besitzer", "&4", 10000, Arrays.stream(ClanRank.ClanRankPermission.values()).toList());
            ClanRank defaultRank = new ClanRank(UUID.randomUUID(), clan.getOwnerId(), System.currentTimeMillis(), "Mitglied", "&7", 1, new ArrayList<>());

            HashMap<String, ClanRank> ranks = clan.getRanks();
            ranks.put(ownerRank.getUuid().toString(), ownerRank);
            ranks.put(defaultRank.getUuid().toString(), defaultRank);

            clan.setRanks(ranks);

            HashMap<String, ClanMember> clanMembers = clan.getClanMembers();
            clanMembers.put(clan.getOwnerId().toString(), new ClanMember(clan.getOwnerId(), ownerRank.getUuid(), System.currentTimeMillis()));

            clan.setClanMembers(clanMembers);

            clan.setStandardRank(defaultRank.getUuid());

            modifyClan(clan);

            new ClanPlayerService().setClan(clan.getOwnerId(), clan.getId());

            OfflinePlayer owner = Bukkit.getOfflinePlayer(clan.getOwnerId());

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user "+owner.getName()+" meta setsuffix 10000 §8["+ XDevApi.getInstance().getxUtil().getStringUtil().getLuckPermsFormattedString(clan.getTagColor()+clan.getTag())+"§8]");

        }
    }

    public void deleteClan(UUID uuid){
        if(existClan(uuid)){
            Clan clan = getClan(uuid);

            for(ClanMember clanMember : clan.getClanMembers().values()){
                new ClanPlayerService().setClan(clanMember.getId(), null);

                OfflinePlayer player = Bukkit.getOfflinePlayer(clanMember.getId());

                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user "+player.getName()+" meta removesuffix 10000");

                if(player.isOnline()){
                    player.getPlayer().sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cDein Clan wurde gelöscht!");
                }

            }


            getCache().remove(uuid);
            getCollection().deleteOne(Filters.eq("_id", uuid));
        }
    }

    public void updateClanRank(UUID clanID, ClanMember clanMember, ClanRank clanRank){
        Clan clan = getClan(clanID);

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(clanMember.getId());

        if(offlinePlayer.isOnline()){
            offlinePlayer.getPlayer().sendMessage(XDevApi.getInstance().getxUtil().getStringUtil().fullyFormattedString(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Du bist in deinem Clan nun "+clanRank.getColorTag()+clanRank.getName()+"§7!"));
            getClanChat(clanID).sendLogMessage("§b"+offlinePlayer.getName()+" §7hat nun den "+clanRank.getColorTag()+clanRank.getName()+" §7Rang!");
        }

        clanMember.setClanRankId(clanRank.getUuid());

        updateClanMember(clan, clanMember);
    }


    public void updateClanMember(Clan clan, ClanMember clanMember){

        HashMap<String, ClanMember> clanMembers = clan.getClanMembers();
        clanMembers.replace(clanMember.getId().toString(), clanMember);

        clan.setClanMembers(clanMembers);

        modifyClan(clan);
    }

    public void addnewClanMember(UUID uuid, Player player){
        Clan clan = getClan(uuid);

        HashMap<String, ClanMember> clanMembers = clan.getClanMembers();
        clanMembers.put(player.getUniqueId().toString(), new ClanMember(player.getUniqueId(), clan.getStandardRank(), System.currentTimeMillis()));

        clan.setClanMembers(clanMembers);

        modifyClan(clan);
    }

    public void addClanMember(UUID uuid, ClanMember clanMember){
        Clan clan = getClan(uuid);

        HashMap<String, ClanMember> clanMembers = clan.getClanMembers();
        clanMembers.put(clanMember.getId().toString(), clanMember);

        clan.setClanMembers(clanMembers);

        modifyClan(clan);
    }

    public void removeClanMemberForced(UUID uuid, UUID memberUUID, Player remover){
        removeClanMember(uuid, memberUUID);
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(memberUUID);
        if(offlinePlayer.isOnline()){
            offlinePlayer.getPlayer().sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cDu wurdest von §b"+remover.getName()+" §caus deinem Clan entfernt!");
            getClanChat(uuid).sendLogMessage("§b"+remover.getName()+" §7hat §b"+offlinePlayer.getName()+" aus dem Clan §centfernt§7!");
        }
    }

    public void removeClanMember(UUID uuid, UUID memberUUID){
        Clan clan = getClan(uuid);

        HashMap<String, ClanMember> clanMembers = clan.getClanMembers();
        clanMembers.remove(memberUUID.toString());

        clan.setClanMembers(clanMembers);

        modifyClan(clan);

        new ClanPlayerService().setClan(memberUUID, null);
    }

    public void addClanRank(UUID uuid, ClanRank clanRank){
        Clan clan = getClan(uuid);

        HashMap<String, ClanRank> clanRanks = clan.getRanks();
        clanRanks.putIfAbsent(clanRank.getUuid().toString(), clanRank);

        clan.setRanks(clanRanks);

        modifyClan(clan);
    }

    public void removeClanRank(UUID uuid, UUID clanRankUUID){
        Clan clan = getClan(uuid);

        HashMap<String, ClanRank> clanRanks = clan.getRanks();
        clanRanks.remove(clanRankUUID.toString());

        clan.setRanks(clanRanks);

        modifyClan(clan);
    }

    public void addJoinRequest(UUID uuid, UUID requestUUID){
        Clan clan = getClan(uuid);

        List<UUID> joinRequests = clan.getJoinRequests();

        if(!joinRequests.contains(requestUUID)){
            joinRequests.add(requestUUID);
        }

        clan.setJoinRequests(joinRequests);

        modifyClan(clan);
    }

    public void removeJoinRequest(UUID uuid, UUID requestUUID){
        Clan clan = getClan(uuid);

        List<UUID> joinRequests = clan.getJoinRequests();

        if(joinRequests.contains(requestUUID)){
            joinRequests.remove(requestUUID);
        }

        clan.setJoinRequests(joinRequests);

        modifyClan(clan);
    }

    public void addClanInvite(UUID uuid, UUID requestUUID, Long expiry){
        Clan clan = getClan(uuid);

        HashMap<String, Long> invites = clan.getClanInvites();

        invites.putIfAbsent(requestUUID.toString(), expiry);

        clan.setClanInvites(invites);

        modifyClan(clan);
    }

    public void removeClanInvite(UUID uuid, UUID requestUUID){
        Clan clan = getClan(uuid);

        HashMap<String, Long> invites = clan.getClanInvites();

        invites.remove(requestUUID.toString());

        clan.setClanInvites(invites);

        modifyClan(clan);
    }

    public void setClanTag(UUID uuid, String tag){
        Clan clan = getClan(uuid);
        clan.setTag(tag);
        modifyClan(clan);

        for(ClanMember clanMember : clan.getClanMembers().values()){
            OfflinePlayer player = Bukkit.getOfflinePlayer(clanMember.getId());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user "+player.getName()+" meta setsuffix 10000 §8["+ XDevApi.getInstance().getxUtil().getStringUtil().getLuckPermsFormattedString(clan.getTagColor()+clan.getTag())+"§8]");
        }
    }

    public void setClanName(UUID uuid, String name){
        Clan clan = getClan(uuid);
        clan.setName(name);
        modifyClan(clan);
    }

    public void setClanStatus(UUID uuid, ClanStatus clanStatus){
        Clan clan = getClan(uuid);
        clan.setClanStatus(clanStatus);
        modifyClan(clan);
    }

    public void setClanTagColor(UUID uuid, String tagColor){
        Clan clan = getClan(uuid);
        clan.setTagColor(tagColor);
        modifyClan(clan);

        for(ClanMember clanMember : clan.getClanMembers().values()){
            OfflinePlayer player = Bukkit.getOfflinePlayer(clanMember.getId());

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user "+player.getName()+" meta setsuffix 10000 §8["+ XDevApi.getInstance().getxUtil().getStringUtil().getLuckPermsFormattedString(clan.getTagColor()+clan.getTag())+"§8]");
        }
    }

    public ClanChat getClanChat(UUID uuid){
        return getClanChat(getClan(uuid));
    }

    public ClanChat getClanChat(Clan clan){
        return new ClanChat(clan);
    }

    public void modifyClan(Clan clan){
        getCache().replace(clan.getId(), clan);

        XDevApi.getInstance().getxScheduler().async(() -> {
            getCollection().replaceOne(Filters.eq("_id", clan.getId()), clan);
        });
    }

    public Clan getClan(UUID uuid){
        return getCache().get(uuid);
    }

    public boolean existClan(UUID uuid){
        return getCache().containsKey(uuid);
    }

    public boolean existClanName(String name){
        return getCache().asMap().entrySet().stream().filter(uuidClanEntry -> uuidClanEntry.getValue().getName().toLowerCase().equals(name.toLowerCase())).count() != 0;
    }

    public boolean existClanTag(String tag){
        return getCache().asMap().entrySet().stream().filter(uuidClanEntry -> uuidClanEntry.getValue().getTag().toLowerCase().equals(tag.toLowerCase())).count() != 0;
    }

    public Collection<Clan> getClansFilteredAndSorted(FilterType filterType, SortType sortType){
        return getClansSorted(getClansFiltered(filterType), sortType);
    }

    public Collection<Clan> getClansFiltered(FilterType filterType){
        return getClansFiltered(getCache().asMap().values(), filterType);
    }

    public Collection<Clan> getClansFiltered(Collection<Clan> allClans, FilterType filterType){
        switch (filterType){
            case CLOSED_ONLY:
                return allClans
                        .stream()
                        .filter(clan -> clan.getClanStatus() == ClanStatus.CLOSED)
                        .collect(Collectors.toList());
            case OPEN_ONLY:
                return allClans
                        .stream()
                        .filter(clan -> clan.getClanStatus() == ClanStatus.OPEN)
                        .collect(Collectors.toList());
            case JOIN_ONLY:
                return allClans
                        .stream()
                        .filter(clan -> clan.getClanStatus() == ClanStatus.ON_REQUEST)
                        .collect(Collectors.toList());
            default:
                return allClans;
        }
    }

    public Collection<Clan> getClansSorted(SortType sortType){
        return getClansSorted(getCache().asMap().values(), sortType);
    }

    public Collection<Clan> getClansSorted(Collection<Clan> allClans, SortType sortType){
        switch (sortType){
            case MONEY:
                return allClans
                        .stream()
                        .sorted((o1, o2) -> o2.getBankAmount().compareTo(o1.getBankAmount()))
                        .collect(Collectors.toList());
            case MEMBERS:
                return allClans
                        .stream()
                        .sorted((o1, o2) -> Integer.compare(o2.getClanMembers().size(), o1.getClanMembers().size()))
                        .collect(Collectors.toList());
            case CREATED_AT_ASC:
                return allClans
                        .stream()
                        .sorted(Comparator.comparing(Clan::getCreatedAt))
                        .collect(Collectors.toList());
            case CREATED_AT_DESC:
                    return allClans
                            .stream()
                            .sorted(((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt())))
                            .collect(Collectors.toList());
            default:
                return allClans;
        }
    }

    public void loadClans(){
        getCollection().find().forEach((Block<? super Clan>) clan -> {
            getCache().putIfAbsent(clan.getId(), clan);
        });
    }

    private MongoCollection<Clan> getCollection(){
        return XCore.getInstance().getMongoDBStorage().getMongoClient().getDatabase("Clan").getCollection("Data_Clan", Clan.class);
    }

    private Cache<UUID, Clan> getCache(){
        return XClan.getInstance().getClanCache();
    }
}
