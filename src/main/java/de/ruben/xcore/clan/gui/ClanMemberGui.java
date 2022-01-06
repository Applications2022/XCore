package de.ruben.xcore.clan.gui;

import de.ruben.xcore.XCore;
import de.ruben.xcore.clan.model.Clan;
import de.ruben.xcore.clan.model.ClanMember;
import de.ruben.xcore.clan.model.ClanRank;
import de.ruben.xcore.clan.model.ClanStatus;
import de.ruben.xcore.clan.model.gui.FilterType;
import de.ruben.xcore.clan.model.gui.SortType;
import de.ruben.xcore.clan.service.ClanPlayerService;
import de.ruben.xcore.clan.service.ClanService;
import de.ruben.xcore.pagination.PaginatedArrayList;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.custom.gui.ItemPreset;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.InteractionModifier;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class ClanMemberGui extends Gui {
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy 'um' HH:mm'Uhr'");

    private PaginatedArrayList paginatedArrayList;

    private Clan clan;

    public ClanMemberGui(Clan clan) {
        super(6, "§9§lClan §8| "+ XDevApi.getInstance().getxUtil().getStringUtil().fullyFormattedString(clan.getTagColor()+clan.getTag()) +" §8| §9§lMitglieder", Set.of(InteractionModifier.PREVENT_ITEM_PLACE, InteractionModifier.PREVENT_ITEM_SWAP, InteractionModifier.PREVENT_ITEM_TAKE));

        this.clan = clan;

        this.setDefaultClickAction(event -> event.setCancelled(true));

        this.paginatedArrayList = new PaginatedArrayList(getSortedMembers(clan), 28);

        this.getFiller().fillBorder(ItemPreset.fillItem(event -> {}));
        this.setItem(49, ItemPreset.closeItem(event -> event.getWhoClicked().closeInventory()));

    }


    public void open(@NotNull HumanEntity player, int page) {
        super.open(player);
        setPageItems((Player) player, page);
    }

    private void setPageItems(Player player, int page) {
        paginatedArrayList.gotoPage(page);

        this.setItem(45, ItemPreset.backItem(event -> new ClanGui().open(player, clan)));

        if (paginatedArrayList.isNextPageAvailable()) {
            this.setItem(50, ItemBuilder.from(Material.MAP).name(Component.text("§9Nächste Seite")).asGuiItem(inventoryClickEvent -> {
                new ClanMemberInfoGui(clan).open(player, paginatedArrayList.getPageIndex() + 1);
            }));
        }

        if (paginatedArrayList.isPreviousPageAvailable()) {
            this.setItem(48, ItemBuilder.from(Material.MAP).name(Component.text("§9Letzte Seite")).asGuiItem(inventoryClickEvent -> {
                new ClanMemberInfoGui(clan).open(player, paginatedArrayList.getPageIndex() - 1);
            }));
        }

        XDevApi.getInstance().getxScheduler().async(() -> {
            for (int i = 0; i < 28; i++) {
                if (i >= paginatedArrayList.size()) {
                    break;
                }

                ClanMember clanMember = (ClanMember) paginatedArrayList.get(i);
                ClanRank clanRank = clan.getRanks().get(clanMember.getClanRankId().toString());
                ClanRank openerRank = clan.getRanks().get(clan.getClanMembers().get(player.getUniqueId().toString()).getClanRankId().toString());


                this.addItem(ItemBuilder.from(ClanMember.getClanMemberItemStack((ClanMember) paginatedArrayList.get(i), clan, player)).asGuiItem(event -> {
                    if(event.isRightClick()){
                        if(openerRank.getPermissions().contains(ClanRank.ClanRankPermission.REMOVE_PLAYERS) && (openerRank.getWeight() > clanRank.getWeight())){
                            OfflinePlayer removePlayer = Bukkit.getOfflinePlayer(clanMember.getId());

                            new ClanService().removeClanMemberForced(clan.getId(), clanMember.getId(), player);
                            event.getWhoClicked().sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Du hast §b"+removePlayer.getName()+" §7erfolgreich aus dem Clan entfernt!");
                            event.getWhoClicked().closeInventory();
                        }
                    }else if(event.isLeftClick()){
                        if(openerRank.getPermissions().contains(ClanRank.ClanRankPermission.CLAN_ROLES_ASSIGN) && (openerRank.getWeight() > clanRank.getWeight())){
                            new SetRoleGui(clan, player).open(player, clanMember);
                        }
                    }

                    event.setCancelled(true);
                }));

                Bukkit.getScheduler().runTask(XCore.getInstance(), () -> this.update());
            }
        });
    }

    private List<ClanMember> getSortedMembers(Clan clan){
        return clan.getClanMembers().values().stream().sorted((o1, o2) -> clan.getRanks().get(o2.getClanRankId().toString()).getWeight().compareTo(clan.getRanks().get(o1.getClanRankId().toString()).getWeight())).collect(Collectors.toList());
    }
}
