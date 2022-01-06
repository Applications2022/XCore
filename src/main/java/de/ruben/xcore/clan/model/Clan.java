package de.ruben.xcore.clan.model;

import de.ruben.xcore.XCore;
import de.ruben.xcore.clan.XClan;
import de.ruben.xcore.clan.service.ClanChat;
import de.ruben.xdevapi.XDevApi;
import dev.triumphteam.gui.builder.item.ItemBuilder;
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
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Clan {
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy 'um' HH:mm'Uhr'");
    // clanplots, clan-goals, active-clan-rabbats
    private UUID id, ownerId, standardRank;
    private Long createdAt;
    private String name, tag, tagColor;
    private Integer slots;
    private Double bankAmount;
    private List<UUID> joinRequests;
    private HashMap<String, Long> clanInvites;
    private HashMap<String, ClanRank> ranks;
    private ClanStatus clanStatus;
    private HashMap<String, ClanMember> clanMembers;
    private Safe safe;

    public boolean isMember(Player player){
        return clanMembers.containsKey(player.getUniqueId().toString());
    }

    public LinkedHashMap<String, ClanRank> getFilteredRanks(Player player) {
        return ranks
                .values()
                .stream()
                .filter(clanRank -> clanRank.getWeight() < getRanks().get(clanMembers.get(player.getUniqueId().toString()).getClanRankId().toString()).getWeight())
                .sorted((o1, o2) -> o2.getWeight().compareTo(o1.getWeight()))
                .collect(Collectors.toMap(clanRank -> clanRank.getUuid().toString(), clanRank -> clanRank, (o, o2) -> {throw new IllegalStateException(String.format("Duplicate key %s", o.getName()));}, LinkedHashMap::new));
    }

    public boolean isOwner(Player player){
        return player.getUniqueId().toString().equals(ownerId.toString());
    }

    public boolean isMember(UUID uuid){
        return clanMembers.containsKey(uuid.toString());
    }

    public boolean isOwner(UUID uuid){
        return uuid.toString().equals(ownerId.toString());
    }

    public static final ItemStack getPreviewItem(Clan clan){
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(clan.getOwnerId());

        return ItemBuilder
                .from(Material.PLAYER_HEAD)
                .name(Component.text(XDevApi.getInstance().getxUtil().getStringUtil().fullyFormattedString((clan.getTagColor()+clan.getName()+" §7["+clan.getTagColor()+clan.getTag()+"§7]"))))
                .lore(
                        Component.text(" "),
                        Component.text("§7➥ Ersteller: §b"+ offlinePlayer.getName()),
                        Component.text("§7➥ Mitglieder: §b"+clan.getClanMembers().size()+"§7/§b"+clan.getSlots()),
                        Component.text("§7➥ Status: "+getStatusString(clan.getClanStatus())),
                        Component.text("§7➥ Geld: §b"+ XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(clan.getBankAmount())+"€"),
                        Component.text(" "),
                        Component.text("§7Erstellt am §b"+simpleDateFormat.format(new Date(clan.getCreatedAt().longValue())))
                )
                .setSkullOwner(offlinePlayer)
                .build();
    }

    private static String getStatusString(ClanStatus clanStatus){
        switch (clanStatus){
            case OPEN:
                return "§2Geöffnet";
            case CLOSED:
                return "§cGeschlossen";
            default:
                return "§aAuf Einladung";
        }
    }

}
