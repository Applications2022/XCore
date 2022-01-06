package de.ruben.xcore.stock.gui;

import de.ruben.xcore.currency.XCurrency;
import de.ruben.xcore.currency.account.gui.OtherBanksGui;
import de.ruben.xcore.pagination.PaginatedArrayList;
import de.ruben.xcore.stock.model.Holding;
import de.ruben.xcore.stock.model.HoldingSortType;
import de.ruben.xcore.stock.model.StockType;
import de.ruben.xcore.stock.service.HoldingService;
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

import java.util.Set;

public class DepotGui extends Gui {
    private PaginatedArrayList paginatedArrayList;
    private StockType stockType;
    private HoldingSortType holdingSortType;

    public DepotGui(Player player, StockType stockType, HoldingSortType type) {
        super(6, "§9Aktien §8| §9Depot", Set.of(InteractionModifier.PREVENT_ITEM_PLACE, InteractionModifier.PREVENT_ITEM_SWAP, InteractionModifier.PREVENT_ITEM_TAKE));

        this.stockType = stockType;
        this.paginatedArrayList = new PaginatedArrayList(new HoldingService(player.getUniqueId()).getHoldingPlayer().getHoldings(type).values(), 28);
        this.holdingSortType = type;

    }

    public void open(@NotNull HumanEntity player, int page) {

        HoldingService holdingService = new HoldingService(player.getUniqueId());

        this.setDefaultClickAction(event -> {
            event.setCancelled(true);
        });

        this.getFiller().fillBorder(ItemPreset.fillItem(event -> {}));

        this.setItem(45, ItemPreset.backItem(Material.ARROW, "§9Zurück", event -> {
            new StockGui().open(player, (stockType == null) ? StockType.CRYPTOCURRENCY : stockType);
        }));

        this.setItem(49, ItemPreset.closeItem(event -> {
            this.close(player);
        }));

        this.setItem(52, ItemBuilder
                .from(Material.MAP)
                .name(Component.text("§bSortierung"))
                .lore(
                        Component.text((holdingSortType == HoldingSortType.ALL ? "§b" : "§7")+"Alle"),
                        Component.text((holdingSortType == HoldingSortType.CRYPTOONLY ? "§b" : "§7")+"Nur Crypto Währungen"),
                        Component.text((holdingSortType == HoldingSortType.STOCKONLY ? "§b" : "§7")+"Nur Aktien")
                )
                .asGuiItem(event -> {
                    new DepotGui((Player) player, stockType, holdingSortType.getNextSortyType(holdingSortType)).open(player, 0);
                }));

        this.setItem(53,
                ItemBuilder
                        .from(Material.WHITE_BANNER)
                        .name(Component.text("§bKauf/Verkauf Verlauf"))
                        .asGuiItem(event -> {
                            new HoldingHistoryGui((Player) player, stockType, holdingSortType).open(player, 0);
                        })
                );

        super.open(player);

        setPageItems((Player) player, page);
    }

    private void setPageItems(Player player, int page){
        paginatedArrayList.gotoPage(page);

        XDevApi.getInstance().getxScheduler().async(() -> {
            for (int i = 0; i < 28; i++) {
                if (i >= paginatedArrayList.size()) {
                    break;
                }

                Holding holding = (Holding) paginatedArrayList.get(i);

                if(holding.getAmount() > 0){
                    this.addItem(ItemBuilder.from(holding.toItemStack()).asGuiItem(event -> {
                        new SingleStockGui(holding.getSymbol()).open(player);
                    }));
                }
            }

            if (paginatedArrayList.isNextPageAvailable()) {
                this.setItem(50, ItemBuilder.from(Material.MAP).name(Component.text("§9Nächste Seite")).asGuiItem(inventoryClickEvent -> {
                    new DepotGui(player, stockType, holdingSortType).open(player, paginatedArrayList.getPageIndex() + 1);
                }));
            }

            if (paginatedArrayList.isPreviousPageAvailable()) {
                this.setItem(48, ItemBuilder.from(Material.MAP).name(Component.text("§9Letzte Seite")).asGuiItem(inventoryClickEvent -> {
                    new DepotGui(player, stockType, holdingSortType).open(player, paginatedArrayList.getPageIndex() - 1);
                }));
            }





            this.update();

        });
    }
}
