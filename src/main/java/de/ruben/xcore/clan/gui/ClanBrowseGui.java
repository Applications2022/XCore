package de.ruben.xcore.clan.gui;

import de.ruben.xcore.clan.model.Clan;
import de.ruben.xcore.clan.model.gui.FilterType;
import de.ruben.xcore.clan.model.gui.SortType;
import de.ruben.xcore.clan.service.ClanService;
import de.ruben.xcore.pagination.PaginatedArrayList;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.custom.gui.ItemPreset;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.InteractionModifier;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ClanBrowseGui extends Gui {

    private SortType sortType;
    private FilterType filterType;
    private PaginatedArrayList paginatedArrayList;

    public ClanBrowseGui(SortType sortType, FilterType filterType) {
        super(6, "§9§lClans §8| §9§lDurchsuchen", Set.of(InteractionModifier.PREVENT_ITEM_PLACE, InteractionModifier.PREVENT_ITEM_SWAP, InteractionModifier.PREVENT_ITEM_TAKE));

        this.sortType = sortType;
        this.filterType = filterType;
        this.paginatedArrayList = new PaginatedArrayList(new ClanService().getClansFilteredAndSorted(filterType, sortType), 28);

        this.setDefaultClickAction(event -> event.setCancelled(true));

        this.getFiller().fillBorder(ItemPreset.fillItem(event -> {}));

        this.setItem(49, ItemPreset.closeItem(event -> event.getWhoClicked().closeInventory()));

        this.setItem(45, ItemPreset.backItem(event -> new NoClanGui().open(event.getWhoClicked())));


    }

    public void open(@NotNull HumanEntity player, int page) {
        super.open(player);
        setPageItems((Player) player, page);
    }

    private void setPageItems(Player player, int page){
        paginatedArrayList.gotoPage(page);

        if(paginatedArrayList.isNextPageAvailable()){
            this.setItem(50, ItemBuilder.from(Material.MAP).name(Component.text("§9Nächste Seite")).asGuiItem(inventoryClickEvent -> {
                new ClanBrowseGui(sortType, filterType).open(player, paginatedArrayList.getPageIndex()+1);
            }));
        }

        if(paginatedArrayList.isPreviousPageAvailable()){
            this.setItem(48, ItemBuilder.from(Material.MAP).name(Component.text("§9Letzte Seite")).asGuiItem(inventoryClickEvent -> {
                new ClanBrowseGui(sortType, filterType).open(player, paginatedArrayList.getPageIndex()-1);
            }));
        }

        List<Component> sortList = new ArrayList<>();
        sortList.add(Component.text(" "));
        sortList.addAll(Arrays.stream(FilterType.values()).map(filterType1 -> filterType.equals(filterType1) ? Component.text("§b"+filterType1.getDisplayName()) : Component.text("§7"+filterType1.getDisplayName())).collect(Collectors.toList()));
        sortList.add(Component.text(" "));

        this.setItem(52, ItemBuilder
                .from(Material.MAP)
                .name(Component.text("§bFilter"))
                .lore(sortList)
                .asGuiItem(event -> {
                    new ClanBrowseGui(sortType, FilterType.getNextFilterType(filterType)).open(player, 0);
                })
        );

        List<Component> filterList = new ArrayList<>();
        filterList.add(Component.text(" "));
        filterList.addAll(Arrays.stream(SortType.values()).map(filterType1 -> sortType.equals(filterType1) ? Component.text("§b"+filterType1.getDisplayName()) : Component.text("§7"+filterType1.getDisplayName())).collect(Collectors.toList()));
        filterList.add(Component.text(" "));

        this.setItem(53, ItemBuilder
                .from(Material.MAP)
                .name(Component.text("§bSortierung"))
                .lore(filterList)
                .asGuiItem(event -> {
                    new ClanBrowseGui(SortType.nextSortType(sortType), filterType).open(player, 0);
                })
        );

        XDevApi.getInstance().getxScheduler().async(() -> {
            for (int i = 0; i < 28; i++) {
                if (i >= paginatedArrayList.size()) {
                    break;
                }

                Clan clan = ((Clan) paginatedArrayList.get(i));
                this.addItem(ItemBuilder.from(Clan.getPreviewItem(clan)).asGuiItem(event -> new ClanMemberInfoGui(clan).open(player, 0)));

                this.update();

            }
        });
    }
}
