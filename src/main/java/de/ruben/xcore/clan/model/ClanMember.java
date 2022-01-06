package de.ruben.xcore.clan.model;

import de.ruben.xcore.clan.service.ClanService;
import de.ruben.xdevapi.XDevApi;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import io.netty.handler.codec.xml.XmlCdata;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClanMember {
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy 'um' HH:mm'Uhr'");

    private UUID id, clanRankId;
    private Long joinedAt;


    public ClanRank getClanRank(Clan clan){
        return clan.getRanks().get(clanRankId.toString());
    }

    public static final ItemStack getClanMemberItemStack(ClanMember clanMember, Clan clan){
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));

        ClanRank clanRank = clan.getRanks().get(clanMember.getClanRankId().toString());
        return ItemBuilder
                .from(Material.PLAYER_HEAD)
                .name(Component.text(XDevApi.getInstance().getxUtil().getStringUtil().fullyFormattedString(clanRank.getColorTag()+ Bukkit.getOfflinePlayer(clanMember.getId()).getName())))
                .lore(
                        Component.text(" "),
                        Component.text(XDevApi.getInstance().getxUtil().getStringUtil().fullyFormattedString("§7➥ Rang: §b"+clanRank.getColorTag()+clanRank.getName())),
                        Component.text(" "),
                        Component.text("§7Beigetreten am §b"+simpleDateFormat.format(new Date(clanMember.getJoinedAt().longValue())))
                )
                .setSkullOwner(Bukkit.getOfflinePlayer(clanMember.getId()))
                .build();
    }

    public static final ItemStack getClanMemberItemStack(ClanMember clanMember, Clan clan, Player player){
        ClanRank clanRank = clan.getRanks().get(clanMember.getClanRankId().toString());

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(clanMember.getId());

        String onlineString = offlinePlayer.isOnline() ? "§2Online" : "§cOffline";

        ItemStack clanMemberItem = ClanMember.getClanMemberItemStack(clanMember, clan);

        List<Component> lore = new ArrayList<>();

        lore.addAll(Arrays.asList(
                Component.text(" "),
                Component.text(XDevApi.getInstance().getxUtil().getStringUtil().fullyFormattedString("§7➥ Rang: §b"+clanRank.getColorTag()+clanRank.getName())),
                Component.text("§7➥ Status: "+onlineString),
                Component.text(" "),
                Component.text("§7Beigetreten am §b"+simpleDateFormat.format(new Date(clanMember.getJoinedAt().longValue()))),
                Component.text(" ")
        ));

        ClanRank openerRank = clan.getRanks().get(clan.getClanMembers().get(player.getUniqueId().toString()).getClanRankId().toString());

        if(openerRank.getPermissions().contains(ClanRank.ClanRankPermission.REMOVE_PLAYERS) && (openerRank.getWeight() > clanRank.getWeight())){
            lore.add(Component.text("§7➜ Rechtsklick: §bHerauswerfen"));
        }

        if(openerRank.getPermissions().contains(ClanRank.ClanRankPermission.CLAN_ROLES_ASSIGN) && (openerRank.getWeight() > clanRank.getWeight())){
            lore.add(Component.text("§7➜ Linksklick: §bRolle setzen"));
        }

        clanMemberItem.editMeta(itemMeta -> {
            itemMeta.lore(lore);
            clanMemberItem.setItemMeta(itemMeta);
        });

        return clanMemberItem;
    }
}
