package de.ruben.xcore.clan.service;

import de.ruben.xcore.clan.model.Clan;
import de.ruben.xcore.clan.model.ClanMember;
import de.ruben.xcore.clan.model.ClanRank;
import de.ruben.xdevapi.XDevApi;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class ClanChat {

    private Clan clan;

    public ClanChat(Clan clan){
        this.clan = clan;
    }

    public void sendPlayerMessage(Player player, String message){
        ClanRank clanRank = clan.getClanMembers().get(player.getUniqueId().toString()).getClanRank(clan);
        message = "§9§lClan §8| §8["+clanRank.getColorTag()+clanRank.getName()+"§8] §7"+player.getName()+" §7➜ "+message;
        message = XDevApi.getInstance().getxUtil().getStringUtil().fullyFormattedString(message);

        String finalMessage = message;
        clan.getClanMembers().values().forEach(clanMember -> {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(clanMember.getId());
            if(offlinePlayer.isOnline()){
                offlinePlayer.getPlayer().sendMessage(finalMessage);
            }
        });
    }

    public void sendLogMessage(String message){
        message = "§9§lClan §8| §bLOG §7➜ "+message;
        message = XDevApi.getInstance().getxUtil().getStringUtil().fullyFormattedString(message);

        String finalMessage = message;
        clan.getClanMembers().values().forEach(clanMember -> {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(clanMember.getId());
            if(offlinePlayer.isOnline()){
                System.out.println(offlinePlayer.getName());
                offlinePlayer.getPlayer().sendMessage(finalMessage);
            }
        });
    }


    public void sendLogJoin(OfflinePlayer offlinePlayer2){
        String message = "§9§lClan §8| §8[§2+§8] §7"+offlinePlayer2.getName();
        message = XDevApi.getInstance().getxUtil().getStringUtil().fullyFormattedString(message);

        String finalMessage = message;
        clan.getClanMembers().values().forEach(clanMember -> {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(clanMember.getId());
            if(offlinePlayer.isOnline()){
                offlinePlayer.getPlayer().sendMessage(finalMessage);
            }
        });
    }

    public void sendLogQuit(OfflinePlayer offlinePlayer2){
        String message = "§9§lClan §8| §8[§c-§8] §7"+offlinePlayer2.getName();
        message = XDevApi.getInstance().getxUtil().getStringUtil().fullyFormattedString(message);

        String finalMessage = message;
        clan.getClanMembers().values().forEach(clanMember -> {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(clanMember.getId());
            if(offlinePlayer.isOnline()){
                offlinePlayer.getPlayer().sendMessage(finalMessage);
            }
        });
    }

}
