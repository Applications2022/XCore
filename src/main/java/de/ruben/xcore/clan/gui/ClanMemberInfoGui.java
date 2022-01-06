package de.ruben.xcore.clan.gui;

import de.ruben.xcore.XCore;
import de.ruben.xcore.clan.model.Clan;
import de.ruben.xcore.clan.model.ClanMember;
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
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ClanMemberInfoGui extends Gui {

    private PaginatedArrayList paginatedArrayList;

    private Clan clan;

    public ClanMemberInfoGui(Clan clan) {
        super(6, "§9§lClan §8| "+ XDevApi.getInstance().getxUtil().getStringUtil().fullyFormattedString(clan.getTagColor()+clan.getTag()) +" §8| §9§lMitglieder", Set.of(InteractionModifier.PREVENT_ITEM_PLACE, InteractionModifier.PREVENT_ITEM_SWAP, InteractionModifier.PREVENT_ITEM_TAKE));

        this.clan = clan;

        this.setDefaultClickAction(event -> event.setCancelled(true));

        this.paginatedArrayList = new PaginatedArrayList(getSortedMembers(clan), 28);

        this.getFiller().fillBorder(ItemPreset.fillItem(event -> {}));
        this.setItem(49, ItemPreset.closeItem(event -> event.getWhoClicked().closeInventory()));
        this.setItem(45, ItemPreset.backItem(event -> new ClanBrowseGui(SortType.NO, FilterType.ALL).open(event.getWhoClicked(), 0)));

    }


    public void open(@NotNull HumanEntity player, int page) {

        setPageItems((Player) player, page);

        super.open(player);
    }

    private void setPageItems(Player player, int page) {
        paginatedArrayList.gotoPage(page);

        if(clan.getClanStatus() == ClanStatus.OPEN && !new ClanPlayerService().isInClan(player.getUniqueId())){
            this.setItem(53, ItemBuilder.from(Material.GREEN_DYE).name(Component.text("§2Server direkt beitreten")).asGuiItem(event -> {
                new ClanService().joinClan(clan, player);
            }));
        }else if(clan.getClanStatus() == ClanStatus.ON_REQUEST && !new ClanPlayerService().isInClan(player.getUniqueId()) && !clan.getJoinRequests().contains(player.getUniqueId())){
            this.setItem(53, ItemBuilder.from(Material.YELLOW_DYE).name(Component.text("§2Beitrittsanfrage senden")).asGuiItem(event -> {
                new ClanService().sendJoinRequest(clan, player);
            }));
        }

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

                this.addItem(ItemBuilder.from(ClanMember.getClanMemberItemStack(((ClanMember) paginatedArrayList.get(i)), clan)).asGuiItem());

                Bukkit.getScheduler().runTask(XCore.getInstance(), () -> this.update());
            }
        });
    }

    private List<ClanMember> getSortedMembers(Clan clan){
        return clan.getClanMembers().values().stream().sorted((o1, o2) -> clan.getRanks().get(o2.getClanRankId().toString()).getWeight().compareTo(clan.getRanks().get(o1.getClanRankId().toString()).getWeight())).collect(Collectors.toList());
    }
}
